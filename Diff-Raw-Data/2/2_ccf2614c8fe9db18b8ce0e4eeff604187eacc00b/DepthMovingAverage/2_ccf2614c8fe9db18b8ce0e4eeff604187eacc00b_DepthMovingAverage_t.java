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
 public class DepthMovingAverage extends SAMProcessor {
 	public enum Format {
 		SQLITE,
 		TSV
 	}
 	
 	private Format format = Format.TSV;
 	private int windowIndex;
 	private Map<String, Integer> readCounts;
 	private Map<String, Poisson> nullDistributions = new HashMap<String, Poisson>();
 	
 	private File outputFile;
 	private Connection connection;
 	private PreparedStatement insertDepthEntryStatement;
 	
 	private RandomEngine randomEngine = RandomEngine.makeDefault();
 	
 	@Override
 	@Option(help="Reference sequence lengths")
 	public void setRefLengths(File f) throws BioException, IOException {
 		super.setRefLengths(f);
 		for (String name : this.refSeqLengths.keySet()) {
 			nullDistributions.put(name, new Poisson(this.refSeqLengths.get(name) / this.windowSize, randomEngine));
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
 	
 	@Option(help="Output file")
 	public void setOut(File f) {
 		this.outputFile = f;
 	}
 	
 	@Override
 	@Option(help="Index file for mapped reads")
 	public void setIndex(File f) {
 		super.setIndex(f);
 	}
 	
 	private Connection connection() throws SQLException {
 		if (this.connection == null) {
 			this.connection = 
 				DriverManager.getConnection(
 					String.format(
 						"jdbc:sqlite:%s",
 						this.outputFile.getPath()));
 
 			this.connection.setAutoCommit(true);
 		}
 		return this.connection;
 	}
 	
 	PreparedStatement insertDepthEntryStatement() throws SQLException {
 		if (this.insertDepthEntryStatement == null) {
 			this.insertDepthEntryStatement = connection().prepareStatement(
 	         "insert into depth values (?, ?, ?, ?, ?, ?);");
 		}
 		return this.insertDepthEntryStatement;
 	}
 		
 	@Override
 	public void main(String[] args) throws BioException, ClassNotFoundException, SQLException {
 		setIterationType(IterationType.MOVING_WINDOW);
 		setQueryType(QueryType.OVERLAP);
 		initializeSAMReader();
 		
 		Class.forName("org.sqlite.JDBC");
 		if (format == Format.SQLITE) {this.createDepthDatabase();}
 		
 		this.windowIndex = 0;
 		
 		connection().setAutoCommit(false);
 		
 		process();
 		insertDepthEntryStatement().executeBatch();
 		connection().setAutoCommit(false);
 	}
 	
 	private void createDepthDatabase() throws SQLException {
 		Statement stat = connection().createStatement();
 		stat.executeUpdate("DROP TABLE if exists depth;");
 		stat.executeUpdate("CREATE TABLE window (" +
 				"id integer primary key," +
 				"ref_name varchar," +
 				"begin_coord integer," +
 				"end_coord integer," +
 				"depth float," +
 				"pvalue float);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_end_idx ON depth(ref_name,begin_coord,end_coord);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_idx ON depth(ref_name,begin_coord);");
 	}
 	
 	private void createPeakDatabase() throws SQLException {
 		Statement stat = connection().createStatement();
 		stat.executeUpdate("DROP TABLE if exists peak;");
 		stat.executeUpdate("CREATE TABLE peak (" +
 				"id integer primary key," +
 				"ref_name varchar," +
 				"begin_coord integer," +
 				"end_coord integer," +
 				"depth float," +
 				"depth_control float," +
 				"pvalue float," +
 				"fdr float);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_end_idx ON depth(ref_name,begin_coord,end_coord);");
 		stat.executeUpdate("CREATE INDEX ref_name_begin_idx ON depth(ref_name,begin_coord);");
 	}
 
 	@Override
 	public void process(final List<SAMRecord> recs, String refName, int begin, int end, int seqLength) {
 		double avg = 0.0;
 		int depth = recs.size();
 		double pvalue = this.nullDistributions.get(refName).cdf(depth);
 		
 		if (depth > 0) {
 			if (format == Format.TSV) {
				System.out.printf("%s\t%d\t%d\t%d\t%d\t%.3f%n", refName, this.windowIndex, begin, end, depth, pvalue);				
 			} else {
 				PreparedStatement stat;
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
 						stat.executeQuery();
 					}
 				} catch (SQLException e) {
 					throw new BioError(e);
 				}
 			}
 		}
 		
 		this.windowIndex++;
 	}
 }
