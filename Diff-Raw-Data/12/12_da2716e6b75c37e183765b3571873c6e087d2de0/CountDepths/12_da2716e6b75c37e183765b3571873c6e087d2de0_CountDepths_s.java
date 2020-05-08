 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.sf.samtools.SAMRecord;
 
 import org.biojava.bio.BioError;
 import org.biojava.bio.BioException;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 import cern.jet.random.Poisson;
 import cern.jet.random.engine.RandomEngine;
 
 @NMExtraApp(launchName = "ngdepth", vm = VirtualMachine.SERVER)
 @App(overview = "Output sequencing depth inside a window.", generateStub = true)
 public class CountDepths extends SAMProcessor {
 	public enum Format {
 		SQLITE,
 		TSV
 	}
 	
 	private Format format = Format.TSV;
 	private int windowIndex;
 	private Map<String, Poisson> nullDistributions = new HashMap<String, Poisson>();
 	
 	private File outputFile;
 	private Connection connection;
 	private PreparedStatement insertDepthEntryStatement;
 	
 	private RandomEngine randomEngine = RandomEngine.makeDefault();
 	
 	@Override
 	@Option(help="Reference sequence lengths")
 	public void setRefLengths(File f) throws BioException, IOException {
 		try {
 			super.setRefLengths(f);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	@Option(help="Read counts per reference sequence")
 	public void setReadCounts(File f) {
 		super.setReadCounts(f);
 	}
 	
 	@Option(help="Output format")
 	public void setFormat(Format format) {
 		this.format = format;
 	}
 	
 	@Option(help="Output file " +
 			"(suffixed with _x where x is LSB job index " +
 			"if run on LSF as part of a job array)")
 	public void setOut(File f) {
 		if (jobIndex() >= 0) {
 			this.outputFile = new File(String.format("%s_%d",f.getPath(),this.jobIndex()));
 		} else {
 			this.outputFile = f;
 		}
 	}
 	
 	@Override
 	@Option(help="Mapped reads")
 	public void setMap(String in) {
 		super.setMap(in);
 	}
 	
 	@Override
 	@Option(help="Index file for mapped reads")
 	public void setIndex(File f) {
 		super.setIndex(f);
 	}
 	
 	@Override
 	@Option(help="Extended length")
 	public void setExtendTo(int i) {
 		super.setExtendTo(i);
 	}
 	
 	private Connection connection() throws SQLException {
 		if (this.connection == null) {
 			this.connection = 
 				DriverManager.getConnection(
 					String.format(
 						"jdbc:sqlite:%s",
 						this.outputFile.getPath()));
 
 			this.connection.setAutoCommit(false);
 		}
 		return this.connection;
 	}
 	
 	private PreparedStatement insertDepthEntryStatement() throws SQLException {
 		if (this.insertDepthEntryStatement == null) {
 			this.insertDepthEntryStatement = CountDepths.insertDepthEntryStatement(this.connection());
 		}
 		return insertDepthEntryStatement;
 	}
 	
 	public static PreparedStatement insertDepthEntryStatement(Connection conn) throws SQLException {
 		return conn.prepareStatement(
         	"insert into window values (?, ?, ?, ?, ?, ?);");
 	}
 		
 	private void initNullDistributions() {
 		for (String name : this.refSeqLengths.keySet()) {
 			System.err.printf("%d %d %d %d ", 
 					readCounts.get(name),
 					this.extendedLength,
 					this.refSeqLengths.get(name),
 					this.windowSize);
 			double lambda = 
 				(double)this.readCounts.get(name) * this.extendedLength /
 				(double)this.refSeqLengths.get(name)
 				 * 
 				(double)this.windowSize;
 			
 			System.err.println("lambda:" + lambda);
 			nullDistributions.put(
 					name, 
 					new Poisson(lambda, randomEngine));
 		}	
 	}
 	@Override
 	public void main(String[] args) throws BioException, ClassNotFoundException, SQLException {
 
 		initNullDistributions();
 		
 		setIterationType(IterationType.MOVING_WINDOW);
 		setQueryType(QueryType.OVERLAP);
 		initializeSAMReader();
 		
 		Class.forName("org.sqlite.JDBC");
 		if (format == Format.SQLITE) {
 			CountDepths.createDepthDatabase(this.connection());
 		}
 		
 		this.windowIndex = 0;
 		
 		connection().setAutoCommit(false);
 		
 		process();
 		insertDepthEntryStatement().executeBatch();
 		connection().setAutoCommit(false);
 		
 		this.connection().close();
 	}
 	
 	public static void createDepthDatabase(Connection conn) throws SQLException {
 		Statement stat = conn.createStatement();
 		stat.executeUpdate("DROP TABLE if exists window;");
 		stat.executeUpdate(
 			"CREATE TABLE window (" +
 				"id integer primary key," +
 				"ref_name varchar," +
 				"begin_coord integer," +
 				"end_coord integer," +
 				"depth float," +
 				"pvalue float);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_end_idx ON window(ref_name,begin_coord,end_coord);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_idx ON window(ref_name,begin_coord);");
 	}
 	
 
 	@Override
 	public void process(final List<SAMRecord> recs, String refName, int begin, int end, int seqLength) {
 		double avg = 0.0;
 		int depth = recs.size();
 		double pvalue = 1.0 - this.nullDistributions.get(refName).cdf(depth);
 		
 		if (depth > 0) {
 			if (format == Format.TSV) {
 				System.out.printf(
 					"%s\t%d\t%d\t%d\t%d\t%.8f%n", 
 					refName, 
 					this.windowIndex, 
 					begin, 
 					end, 
 					depth, 
 					pvalue);
 				
 			} else {
 				PreparedStatement stat;
 				//System.err.printf("%s\t%d\t%d\t%d\t%d\t%.8f%n", refName, this.windowIndex, begin, end, depth, pvalue);				
 				try {
 
 					stat = insertDepthEntryStatement();
 
 					stat.setInt(1, this.windowIndex);
 					stat.setString(2, refName);
 					stat.setInt(3, begin);
 					stat.setInt(4, end);
 					stat.setFloat(5, depth);
 					stat.setDouble(6, pvalue);
					stat.addBatch();
 					
					if ((this.windowIndex % 10) == 0) {
						stat.executeBatch();
						
					}
 				} catch (SQLException e) {
 					throw new BioError(e);
 				}
 			}
 		}
 		
 		this.windowIndex++;
 	}
 }
