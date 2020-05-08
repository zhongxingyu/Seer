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
 import net.derkholm.nmica.extra.seq.nextgen.SAMPileup;
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 import net.sf.samtools.util.CloseableIterator;
 
 import org.biojava.bio.BioError;
 import org.biojava.bio.BioException;
 import org.biojava.utils.JDBCPooledDataSource;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 import cern.jet.random.Poisson;
 import cern.jet.random.engine.RandomEngine;
 
 @NMExtraApp(launchName = "ngdepth", vm = VirtualMachine.SERVER)
 @App(overview = "Output sequencing depth inside a window.", generateStub = true)
 public class CountDepths extends SAMProcessor {
 	public enum Format {SQLITE, HSQLDB, MYSQL, TSV}
 
 	private String dbURL = "jdbc:mysql://ens-research/mp4_pileup";
 	private String dbUser = "ensadmin";
 	private String dbPass = "ensembl";
 
 	private Format format = Format.TSV;
 	private int windowIndex;
 	private Map<String, Poisson> nullDistributions = new HashMap<String, Poisson>();
 
 	private File outputFile;
 	private Connection connection;
 	private PreparedStatement insertDepthEntryStatement;
 
 	private RandomEngine randomEngine = RandomEngine.makeDefault();
 	private HashMap<String, Integer> refIds;
 	private List<String> refSeqNames;
 	private PreparedStatement insertRefSeqNameStatement;
 	private int minDepth = 3;
 
 	@Override
 	@Option(help = "Reference sequence lengths")
 	public void setRefLengths(File f) throws BioException, IOException {
 		try {
 			super.setRefLengths(f);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		this.refSeqNames = SAMProcessor.parseRefNamesFromRefLengthFile(f);
 	}
 
 	@Override
 	@Option(help = "Read counts per reference sequence")
 	public void setReadCounts(File f) {
 		super.setReadCounts(f);
 	}
 
 	@Option(help = "Output format")
 	public void setFormat(Format format) {
 		this.format = format;
 	}
 
 	@Option(help = "Output file "
 			+ "(suffixed with _x where x is LSB job index "
 			+ "if run on LSF as part of a job array)")
 	public void setOut(File f) {
 		if (jobIndex() >= 0) {
 			this.outputFile = new File(String.format("%s_%d", f.getPath(), this
 					.jobIndex()));
 		} else {
 			this.outputFile = f;
 		}
 	}
 
 	@Override
 	@Option(help = "Mapped reads")
 	public void setMap(String in) {
 		super.setMap(in);
 	}
 
 	@Override
 	@Option(help = "Index file for mapped reads")
 	public void setIndex(File f) {
 		super.setIndex(f);
 	}
 
 	@Override
 	@Option(help = "Extended length")
 	public void setExtendTo(int i) {
 		super.setExtendTo(i);
 	}
 	
 	@Option(help = "Minimum depth (default=3)",optional=true)
 	public void setMinDepth(int i) {
 		this.minDepth = i;
 	}
 
 	private Connection connection() throws SQLException, ClassNotFoundException {
 		if (this.connection == null) {
 			if (this.format == Format.HSQLDB) {
 				Class.forName("org.hsqldb.jdbcDriver");				
 				this.connection = DriverManager.getConnection(String.format(
 						"jdbc:hsqldb:file:%s", this.outputFile.getPath()), "sa", "");
 			} else if (this.format == Format.SQLITE){
 				Class.forName("org.sqlite.JDBC");				
 				this.connection = DriverManager.getConnection(String.format(
 						"jdbc:sqlite:%s", this.outputFile.getPath()), "sa", "");
 			} else if (this.format == Format.MYSQL) {
 				try {
 					this.connection = JDBCPooledDataSource.getDataSource(
 							"org.gjt.mm.mysql.Driver",
 							dbURL,
 							dbUser, 
 							dbPass).getConnection();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 			this.connection.setAutoCommit(false);
 		}
 		return this.connection;
 	}
 
 	private PreparedStatement insertDepthEntryStatement() throws SQLException, ClassNotFoundException {
 		if (this.insertDepthEntryStatement == null) {
 			this.insertDepthEntryStatement = CountDepths
 					.insertDepthEntryStatement(this.connection());
 		}
 		return this.insertDepthEntryStatement;
 	}
 
 	private PreparedStatement insertRefSeqNameStatement() throws SQLException, ClassNotFoundException {
 		if (this.insertRefSeqNameStatement == null) {
 			this.insertRefSeqNameStatement = CountDepths
 				.insertRefSeqNameStatement(this.connection());
 		}
 
 		return this.insertRefSeqNameStatement;
 	}
 
 	public static PreparedStatement insertRefSeqNameStatement(Connection conn)
 		throws SQLException {
 		return conn.prepareStatement(
 				"INSERT INTO ref_seq VALUES (?,?)");
 	}
 
 	public static PreparedStatement insertDepthEntryStatement(Connection conn)
 			throws SQLException {
 		return conn
 				.prepareStatement("INSERT INTO window VALUES (?, ?, ?, ?, ?, ?);");
 	}
 
 	private void initNullDistributions() {
 		for (String name : this.refSeqLengths.keySet()) {
 			System.err
 					.printf(
 							"%s count: %d read_extended_length:%d ref_length:%d window_size:%d ",
 							name, readCounts.get(name), this.extendedLength,
 							this.refSeqLengths.get(name), this.windowSize);
 			double lambda = (double) this.readCounts.get(name)
 					* this.extendedLength
 					/ (double) this.refSeqLengths.get(name)
 					* (double) this.windowSize;
 
 			System.err.println("lambda:" + lambda);
 			nullDistributions.put(name, new Poisson(lambda, randomEngine));
 		}
 	}
 
 	 public void shutdown() throws SQLException, ClassNotFoundException {
 
 	        Statement st = connection().createStatement();
 
 	        st.execute("SHUTDOWN");
 	        connection().close();    // if there are no other open connection
 	    }
 
 
 
 	@Override
 	public void main(String[] args) throws BioException,
 			ClassNotFoundException, SQLException {
 		initNullDistributions();
 
 		System.err.println("Creating output tables...");
 		if ((format == Format.HSQLDB) || (format == Format.SQLITE) || format == Format.MYSQL) {
 			CountDepths.createDepthTable(this.connection());
 			CountDepths.createRefSeqTable(this.connection());
 		}
 
 		this.windowIndex = 0;
 		System.err.println("Opening indexed reads...");
 		SAMFileReader reader = new SAMFileReader(new File(in), indexFile);
 		System.err.println("Reads OK.");
 		reader.setValidationStringency(ValidationStringency.SILENT);
 		System.err.println("SAM file and index read");
 		int id = 0;
 		for (String name : this.refSeqLengths.keySet()) {
 			System.err.printf("Calculating pileup for %s%n", name);
 			int refId = getRefId(name);
 			Poisson nullDist = this.nullDistributions.get(name);
 
 			SAMPileup pileup = new SAMPileup(name,
 					this.refSeqLengths.get(name), this.extendedLength);
 
 			CloseableIterator<SAMRecord> recIterator = reader.queryOverlapping(
 					name, 0, this.refSeqLengths.get(name));
 			while (recIterator.hasNext()) {
 				pileup.add(recIterator.next());
 			}
 			recIterator.close();
 
 			System.err.println("Storing pileup data to database...");
 			System.err.println("Iterating through");
 			int batchCount = 0;
 			int readCs = this.readCounts.get(name);
 			PreparedStatement ins = this.insertDepthEntryStatement();
 			
 			for (int i = 0, len = this.refSeqLengths.get(name); i < len; i=i+this.frequency) {
 				int depth = pileup.depthAt(i);
 
 				if (depth >= this.minDepth) {
 					/*
 					System.out.printf("%d\t%d\t%d\t%d\t%d\t%f%n",
 							id++,
 							refId,
 							i,
 							i+extendedLength,
 							depth,
 							nullDist.cdf(depth));*/
 										
 					ins.setInt(1, id++);
 					ins.setInt(2, refId);
 					ins.setInt(3, i);
 					ins.setInt(4, i + extendedLength);
 					ins.setDouble(5, (double) depth);
 					ins.setDouble(6, 1.0 - nullDist.cdf(depth));
 					ins.executeUpdate();
 					
 					/*
 					if ((batchCount % 1000000) == 0) {
 						ins.executeBatch();
 						connection().commit();
 						ins.clearBatch();
 						System.err.printf("~");
 					}*/
 					 
 				}
 				
 				if ((i % (len / 100)) == 0) {
 					System.err.printf(".");
 				}
 				
 			}
			/*
			ins.executeBatch();
			connection().commit();
			ins.clearBatch();*/
			ins.close();
			System.err.println("Done.");
 		}
 		// connection().setAutoCommit(false);
 
 		//process();
 		// insertDepthEntryStatement().executeBatch();
 		// insertDepthEntryStatement().close();
 		// connection().setAutoCommit(false);
 
 		this.shutdown();
 	}
 
 	public static void createDepthTable(Connection conn) throws SQLException {
 		Statement stat = conn.createStatement();
 		
 		stat.executeUpdate("DROP TABLE if exists window;");
 		stat.executeUpdate("CREATE TABLE window ("
 				+ "id integer primary key," + "ref_id integer,"
 				+ "begin_coord integer," + "end_coord integer,"
 				+ "depth float," + "pvalue float);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_end_idx ON window(ref_id,begin_coord,end_coord);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_idx ON window(ref_id,begin_coord);");
 		stat.executeUpdate("CREATE INDEX ref_name_end_idx ON window(ref_id,end_coord);");
 		stat.close();
 	}
 
 	public static void createRefSeqTable(Connection conn) throws SQLException {
 		Statement stat = conn.createStatement();
 		stat.executeUpdate("DROP TABLE if exists ref_seq;");
 		stat.executeUpdate("CREATE TABLE ref_seq ("
 				+ "id integer primary key,"
 				+ "name varchar(100));");
 		stat.close();
 	}
 
 	@Override
 	public void process(final List<SAMRecord> recs, String refName, int begin,
 			int end, int seqLength) {
 		double avg = 0.0;
 		int depth = recs.size();
 		double pvalue = 1.0 - this.nullDistributions.get(refName).cdf(depth);
 
 		if (depth > 0) {
 			if (format == Format.TSV) {
 				System.out.printf("%s\t%d\t%d\t%d\t%d\t%.8f%n", refName,
 						this.windowIndex, begin, end, depth, pvalue);
 			} else {
				PreparedStatement stat;
				// System.err.printf("%s\t%d\t%d\t%d\t%d\t%.8f%n", refName,
				// this.windowIndex, begin, end, depth, pvalue);
 
 			}
 		}
 
 		this.windowIndex++;
 	}
 
 	public int getRefId(String seqName) {
 		if (this.refIds == null) {
 			this.refIds = new HashMap<String, Integer>();
 
 			int i = 0;
 
 			PreparedStatement stat;
 			try {
 				stat = this.insertRefSeqNameStatement();
 
 				for (String name : this.refSeqNames) {
 					this.refIds.put(name, i);
 					stat.setInt(1, i);
 					stat.setString(2, name);
 					stat.addBatch();
 					stat.executeBatch();
 					stat.clearBatch();
 					connection().commit();
 					i++;
 				}
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return refIds.get(seqName);
 	}
 }
