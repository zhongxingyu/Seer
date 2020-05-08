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
 	public static enum Format {SQLITE, HSQLDB, MYSQL, TSV}
 
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
 	private int minDepth = 1;
 	private String dbHost;
 	private String dbPassword;
 	private String dbUser;
 	private String database;
 	private HashMap<String, Integer> safeStartingPointIds;
 
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
 			+ "(will be automatically suffixed with _x where x is LSB job index "
 			+ "if run on LSF as part of a job array)", optional=true)
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
 
 	@Option(help = "Minimum depth (default=1)",optional=true)
 	public void setMinDepth(int i) {
 		this.minDepth = i;
 	}
 
 	private Connection connection() throws Exception {
 		if (this.connection == null) {
 			if (this.format == Format.MYSQL) {
 				this.connection = CountDepths.mysqlConnection(dbHost,database,dbUser,dbPassword);
 			} else {
 				this.connection = CountDepths.connection(this.format, this.outputFile);
 			}
 		}
 		return this.connection;
 	}
 
 	public static Connection mysqlConnection(
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
 
 	public static Connection connection(Format format, File outputFile) throws SQLException, ClassNotFoundException {
 		Connection conn = null;
 		if (format == Format.HSQLDB) {
 			Class.forName("org.hsqldb.jdbcDriver");
 			conn = DriverManager.getConnection(String.format(
 						"jdbc:hsqldb:file:%s", outputFile.getPath()), "sa", "");
 		} else if (format == Format.SQLITE){
 			Class.forName("org.sqlite.JDBC");
 			conn =DriverManager.getConnection(String.format(
 						"jdbc:sqlite:%s", outputFile.getPath()), "sa", "");
 		} else {
 			throw new BioError("Unsupported format for writing to output file: " + outputFile);
 		}
 
 		conn.setAutoCommit(false);
 		return conn;
 	}
 
 	private PreparedStatement insertDepthEntryStatement() throws Exception {
 		if (this.insertDepthEntryStatement == null) {
 			this.insertDepthEntryStatement = CountDepths
 					.insertDepthEntryStatement(this.connection());
 		}
 		return this.insertDepthEntryStatement;
 	}
 
 	private PreparedStatement insertRefSeqNameStatement() throws Exception {
 		if (this.insertRefSeqNameStatement == null) {
 			this.insertRefSeqNameStatement = CountDepths
 				.insertRefSeqNameStatement(this.connection());
 		}
 
 		return this.insertRefSeqNameStatement;
 	}
 
 	public static PreparedStatement insertRefSeqNameStatement(Connection conn)
 		throws SQLException {
 		return conn.prepareStatement(
 				"INSERT INTO ref_seq VALUES (?,?,?)");
 	}
 
 	public static PreparedStatement insertDepthEntryStatement(Connection conn)
 			throws SQLException {
 		return conn
 				.prepareStatement("INSERT INTO depth VALUES (?, ?, ?, ?, ?);");
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
 
 	 public void shutdown() throws Exception {
 	        Statement st = connection().createStatement();
 	        st.execute("SHUTDOWN");
 	        connection().close();    // if there are no other open connection
 	    }
 
 
 
 	@Override
 	public void main(String[] args) throws Exception {
 		initNullDistributions();
 
 
 		this.windowIndex = 0;
 		System.err.println("Opening indexed reads...");
 		SAMFileReader reader = new SAMFileReader(new File(in), indexFile);
 		System.err.println("Reads OK.");
 		reader.setValidationStringency(ValidationStringency.SILENT);
 		System.err.println("SAM file and index read");
 
 		String chromoName = null;
 		int primaryId = 1;
 		if (System.getenv().get("LSB_JOBINDEX") != null) {
 			int index = Integer.parseInt(System.getenv().get("LSB_JOBINDEX")) - 1;
 			chromoName = this.refSeqNames.get(index);
 			System.err.println("The task is being run as part of an LSF job array. Will only calculate depth for " + chromoName);
 
 			for (String str : this.refSeqNames) {
 				if (str.equals(chromoName)) break;
				primaryId += this.readCounts.get(str)+1;
 			}
 		}
 
 		for (String name : this.refSeqLengths.keySet()) {
 			if (chromoName != null &! name.equals(chromoName)) continue;
 
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
 			PreparedStatement ins = this.insertDepthEntryStatement();
 
 			for (int i = 0, len = this.refSeqLengths.get(name); i < len; i=i+this.frequency) {
 				int depth = pileup.depthAt(i);
 
 				if (depth >= this.minDepth) {
 					ins.setInt(1, primaryId++);
 					ins.setInt(2, refId);
 					ins.setInt(3, i+1);
 					ins.setDouble(4, (double) depth);
 					ins.setDouble(5, 1.0 - nullDist.cdf(depth));
 					ins.executeUpdate();
 				}
 
 				if ((i % (len / 100)) == 0) {
 					System.err.printf(".");
 				}
 
 			}
 		}
 		this.insertDepthEntryStatement();
 		System.err.println("Done.");
 
 		this.shutdown();
 	}
 
 	public static void createDepthTable(Connection conn) throws SQLException {
 		Statement stat = conn.createStatement();
 
 		stat.executeUpdate("DROP TABLE if exists depth;");
 		stat.executeUpdate("CREATE TABLE depth ("
 							+ "id integer,"
 							+ "ref_id INTEGER,"
 							+ "coord integer,"
 							+ "depth DOUBLE,"
 							+ "pvalue DOUBLE,"
 							+ " PRIMARY KEY (id));");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_idx ON depth(ref_id,coord);");
 		stat.close();
 	}
 
 	public static void createRefSeqTable(Connection conn) throws SQLException {
 		Statement stat = conn.createStatement();
 		stat.executeUpdate("DROP TABLE if exists ref_seq;");
 		stat.executeUpdate("CREATE TABLE ref_seq ("
 				+ "id integer primary key,"
 				+ "name varchar(100),"
 				+ "read_count double);");
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
 
 			}
 		}
 
 		this.windowIndex++;
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
