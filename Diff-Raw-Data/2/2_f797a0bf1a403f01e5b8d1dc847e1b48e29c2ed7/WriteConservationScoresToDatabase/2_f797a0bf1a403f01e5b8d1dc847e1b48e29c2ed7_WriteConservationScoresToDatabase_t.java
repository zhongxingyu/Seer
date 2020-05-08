 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.app.seq.nextgen.CountDepths.Format;
 
 import org.biojava.bio.BioException;
 import org.biojava.utils.JDBCPooledDataSource;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @NMExtraApp(launchName = "ngconservation", vm = VirtualMachine.SERVER)
 @App(overview = "Write conservation scores to a database", generateStub = true)
 public class WriteConservationScoresToDatabase {
 	
 	public static PreparedStatement insertDepthEntryStatement(Connection conn)
 		throws SQLException {
 		return conn.prepareStatement("INSERT INTO conservation VALUES (?, ?, ?, ?);");
 	}
 
 	private File[] files;
 	private Map<String, Integer> refSeqLengths;
 	private List<String> refSeqNames;
 	private String dbHost;
 	private String dbUser;
 	private String dbPassword;
 	private String database;
 	private Connection connection;
 	private HashMap<String, Integer> refIds;
 	
 	@Option(help="Input files. Files that do not fit the pattern ^chr(.*).data.gz will be ignored.")
 	public void setConservationScores(File[] f) {
 		this.files = f;
 	}
 	
 	@Option(help = "Reference sequence lengths")
 	public void setRefLengths(File f) throws NoSuchElementException, BioException, NumberFormatException, IOException {
 		this.refSeqLengths = SAMProcessor.parseRefLengths(f);
 		this.refSeqNames = SAMProcessor.parseRefNamesFromRefLengthFile(f);
 	}
 	
 
 	@Option(help="Database host")
 	public void setHost(String str) {
 		this.dbHost = str;
 	}
 
 	@Option(help="Database username")
 	public void setUser(String str) {
 		this.dbUser = str;
 	}
 
 	@Option(help="Database password")
 	public void setPassword(String str) {
 		this.dbPassword = str;
 	}
 
 	@Option(help="Database schema name")
 	public void setDatabase(String str) {
 		this.database = str;
 	}
 	
 	private Connection connection() throws SQLException, Exception {
 		if (this.connection == null) {
 			this.connection = WriteConservationScoresToDatabase.connection(this.dbHost, 
 																		   this.database, 
 																		   this.dbUser, 
 																		   this.dbPassword);
 		}
 		return this.connection;
 	}
 	
 	public static Connection connection(
 			String dbHost,
 			String database,
 			String dbUser,
 			String dbPassword) throws SQLException, Exception {
 		Connection con = JDBCPooledDataSource.getDataSource(
 				"org.gjt.mm.mysql.Driver",
 				String.format("jdbc:mysql://%s/%s", dbHost, database),
 				dbUser,
 				dbPassword).getConnection();
 		con.setAutoCommit(false);
 		return con;
 	}
 
 	public void main(String[] args) throws SQLException, Exception {
 		String chromoName = null;
 		int primaryId = 1;
 		if (System.getenv().get("LSB_JOBINDEX") != null) {
 			int index = Integer.parseInt(System.getenv().get("LSB_JOBINDEX")) - 1;
 			chromoName = this.refSeqNames.get(index);
 			System.err.println("The task is being run as part of an LSF job array. Will only calculate depth for " + chromoName);
 
 			for (String str : this.refSeqNames) {
 				if (str.equals(chromoName)) break;
 				
 				primaryId += this.refSeqLengths.get(str)+1;
 			}
 		}
 		
		Pattern headerPattern = Pattern.compile("^fixedStep chrom=chr(\\S)+ start=(\\d+) step=(\\d+)");
 		PreparedStatement insertStatement = WriteConservationScoresToDatabase.insertDepthEntryStatement(this.connection());
 		for (File f : this.files) {
 			System.err.printf("Handling file %s...%n",f.getPath());
 			String chrName = null;
 			Matcher randomM  = Pattern.compile("^chr(.*)\\_random.data.gz").matcher(f.getName());
 			Matcher m = Pattern.compile("^chr(.*).data.gz").matcher(f.getName());
 			
 			if (randomM.find()) {
 				System.err.printf("Ignoring file %s%n",f.getName());
 				continue;
 			} else if (m.find()) {
 				chrName = m.group(1);
 			}
 			if (refSeqLengths.get(chrName) == null) {
 				System.err.printf("No chromosome with name %s. Will ignore file.%n", refSeqLengths);
 				System.exit(1);
 			}
 			int refId = getRefId(chrName);
 			
 			BufferedReader in = new BufferedReader(
 									new InputStreamReader(
 										new GZIPInputStream(
 											new FileInputStream(f))));
 			String line = null;
 
 			int step = 1;
 			int i = 1; //positions start from 1 as this database is made for a DAS data source
 			while ((line = in.readLine()) != null) {
 				Matcher headerM = headerPattern.matcher(line);
 				
 				if (headerM.find()) {
 					System.err.printf("Found header line:%s%n",line);
 					if (!headerM.group(1).matches(String.format("chr%s",chrName))) {
 						System.err.printf(
 							"Chromosome name %s doesn't match expected value (%s)! Will exit.",headerM.group(1),chrName);
 						System.exit(1);
 					}
 					i = Integer.parseInt(headerM.group(2)); // the start position
 					step = Integer.parseInt(headerM.group(3));
 					continue;
 				}
 				
 				double consScore = Double.parseDouble(line);
 				insertStatement.setInt(1, primaryId++);
 				insertStatement.setInt(2, refId);
 				insertStatement.setInt(3, i);
 				insertStatement.setDouble(4, (double) consScore);
 				insertStatement.addBatch();
 				
 				i += step;
 			}
 			if ((i % 100) == 0) {
 				insertStatement.executeBatch();
 			}
 		}
 		insertStatement.execute();
 	}
 	
 	public int getRefId(String seqName) throws Exception {
 		if (this.refIds == null) {
 			this.refIds = new HashMap<String, Integer>();
 
 			int i = 0;
 			for (String name : this.refSeqNames) {
 				this.refIds.put(name, i++);
 			}
 		}
 		return refIds.get(seqName);
 	}
 }
