 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDataSource;
 import org.apache.commons.dbcp.PoolingDriver;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 
 public class Accessioner {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     @Option(name = "-i", aliases={"--input"}, usage = "input filename or glob")
     private String inputFilename;
 
     @Option(name = "-o", aliases={"--output"}, usage = "output filename")
     private String outputFilename;
 
     @Option(name = "-n", aliases={"--hostname"}, usage = "server hostname")
     private String hostname;
 
     @Option(name = "-t", aliases={"--port"}, usage = "server port")
     private int port = 3306;
 
     @Option(name = "-d", aliases={"--database"}, usage = "server database")
     private String database;
 
     @Option(name = "-u", aliases={"--username"}, usage = "server username")
     private String username;
 
     @Option(name = "-p", aliases={"--password"}, usage = "server password")
     private String password;
     
     @Option(name = "--threaded", usage = "use multiple threads?")
     private boolean threaded = false;
     
     private int exitcode = 0;
     
     private static boolean setup = false;
     private static ObjectPool connectionPool = new GenericObjectPool();
 
     // receives other command line parameters than options
     @Argument
     private List<String> arguments = new ArrayList<String>();
 
     private final SampleTabParser<SampleData> parser = new SampleTabParser<SampleData>();
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
     private BasicDataSource ds;
 
     public Accessioner() {
     }
     
     private void doSetup() throws ClassNotFoundException, SQLException{
         //this will only setup for the first instance
         //therefore do not try to mix and match different connections in the same VM
         if (setup){
             return;
         }
 
         String connectURI = "jdbc:mysql://" + hostname + ":" + port + "/" + database;
         ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);
         PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
         Class.forName("com.mysql.jdbc.Driver");
         Class.forName("org.apache.commons.dbcp.PoolingDriver");
         PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
         driver.registerPool("accessioner", connectionPool);
 
         //
         // Now we can just use the connect string "jdbc:apache:commons:dbcp:example"
         // to access our pool of Connections.
         //        
         
         setup = true;
     }
 
     public Accessioner(String host, int port, String database, String username, String password)
             throws ClassNotFoundException, SQLException {
         this();
         // Setup the connection with the DB
         this.username = username;
         this.password = password;
         this.hostname = host;
         this.port = port;
         this.database = database;
         
         doSetup();
     }
 
     public SampleData convert(String sampleTabFilename) throws IOException, ParseException, SQLException {
         return convert(new File(sampleTabFilename));
     }
 
     public SampleData convert(File sampleTabFile) throws IOException, ParseException, SQLException {
         return convert(parser.parse(sampleTabFile));
     }
 
     public SampleData convert(URL sampleTabURL) throws IOException, ParseException, SQLException {
         return convert(parser.parse(sampleTabURL));
     }
 
     public SampleData convert(InputStream dataIn) throws ParseException, SQLException {
         return convert(parser.parse(dataIn));
     }
 
     public SampleData convert(SampleData sampleIn) throws ParseException, SQLException {
         String table = null;
         String prefix = null;
         if (sampleIn.msi.submissionReferenceLayer == true) {
             prefix = "SAME";
             table = "sample_reference";
         } else if (sampleIn.msi.submissionReferenceLayer == false) {
             prefix = "SAMEA";
             table = "sample_assay";
         } else {
             exitcode = 1;
             throw new ParseException("Must specify a Submission Reference Layer MSI attribute.");
         }
 
 
         Collection<SampleNode> samples = sampleIn.scd.getNodes(SampleNode.class);
 
         log.debug("got " + samples.size() + " samples.");
         String name;
         String submission = sampleIn.msi.submissionIdentifier;
         if (submission == null){
             throw new ParseException("Submission Identifier cannot be null");
         }
         submission = submission.trim();
         int accessionID;
         String accession;
         
         Connection connect = null;
         PreparedStatement statement = null;
         ResultSet results = null;
         
         try {
             connect = DriverManager.getConnection("jdbc:apache:commons:dbcp:accessioner");
             log.info("Starting accessioning");
             
             
             //first do one query to retrieve all that have already got accessions
             long start = System.currentTimeMillis();
             statement = connect.prepareStatement("SELECT user_accession, accession FROM " + table
                     + " WHERE submission_accession LIKE ? AND is_deleted = 0");
             statement.setString(1, submission);
             log.trace(statement.toString());
             results = statement.executeQuery();
             long end = System.currentTimeMillis();
             log.debug("Time elapsed = "+(end-start)+"ms");
             while (results.next()){
                 String samplename = results.getString(1).trim();
                 accessionID = results.getInt(2);
                 accession = prefix + accessionID;
                 SampleNode sample = sampleIn.scd.getNode(samplename, SampleNode.class);
                 log.trace(samplename+" : "+accession);
                 if (sample != null){
                     sample.setSampleAccession(accession);
                 } else {
                     log.warn("Unable to find SCD sample node "+samplename);
                 }
             }
             results.close();
             
             //now assign and retrieve accessions for samples that do not have them
             for (SampleNode sample : samples) {
                 if (sample.getSampleAccession() == null) {
                     name = sample.getNodeName().trim();
 
                     log.info("Assigning new accession for "+submission+" : "+name);
                     
                     //insert it if not exists
                     start = System.currentTimeMillis();
                     statement = connect
                             .prepareStatement("INSERT INTO "
                                     + table
                                     + " (user_accession, submission_accession, date_assigned, is_deleted) VALUES (?, ?, NOW(), 0)");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.trace(statement.toString());
                     statement.executeUpdate();
                     statement.close();
                     end = System.currentTimeMillis();
                     log.info("Time elapsed = "+(end-start)+"ms");
 
                     statement = connect.prepareStatement("SELECT accession FROM " + table
                             + " WHERE user_accession LIKE ? AND submission_accession LIKE ? LIMIT 1");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.trace(statement.toString());
                     results = statement.executeQuery();
                     results.first();
                     
                     accessionID = results.getInt(1);
                     accession = prefix + accessionID;
                     statement.close();
                     results.close();
 
                     log.debug("Assigning " + accession + " to " + name);
                     sample.setSampleAccession(accession);
                 }
             }
 
             Collection<GroupNode> groups = sampleIn.scd.getNodes(GroupNode.class);
 
             log.debug("got " + groups.size() + " groups.");
             for (GroupNode group : groups) {
                 if (group.getGroupAccession() == null) {
                     name = group.getNodeName();
                     statement = connect
                             .prepareStatement("INSERT IGNORE INTO sample_groups (user_accession, submission_accession, date_assigned, is_deleted) VALUES (?, ?, NOW(), 0)");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.trace(statement.toString());
                     statement.executeUpdate();
                     statement.close();
 
                     statement = connect
                             .prepareStatement("SELECT accession FROM sample_groups WHERE user_accession = ? AND submission_accession = ?");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.trace(statement.toString());
                     results = statement.executeQuery();
                     results.first();
                     accessionID = results.getInt(1);
                     accession = "SAMEG" + accessionID;
                     statement.close();
                     results.close();
 
                     log.debug("Assigning " + accession + " to " + name);
                     group.setGroupAccession(accession);
                 }
             }
         } finally {
             if (results != null){
                 try {
                     results.close();
                 } catch (Exception e) {
                     //do nothing
                 }
             }
             if (statement != null) {
                 try {
                     statement.close();
                 } catch (Exception e) {
                     //do nothing
                 }
             }
             if (connect != null) {
                 try {
                     connect.close();
                 } catch (Exception e) {
                     //do nothing
                 }
             }
         }
 
         return sampleIn;
     }
 
     public void convert(SampleData sampleIn, Writer writer) throws IOException, ParseException, SQLException {
         log.info("recieved magetab, preparing to convert");
         SampleData sampleOut = convert(sampleIn);
         log.info("sampletab converted, preparing to output");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         log.info("created SampleTabWriter");
         sampletabwriter.write(sampleOut);
         sampletabwriter.close();
 
     }
 
     public void convert(File sampletabFile, Writer writer) throws IOException, ParseException, SQLException {
         log.info("preparing to load SampleData");
         SampleTabParser<SampleData> stparser = new SampleTabParser<SampleData>();
        log.info("created MAGETABParser<SampleData>, beginning parse");
         SampleData st = stparser.parse(sampletabFile);
         convert(st, writer);
     }
 
     public void convert(File inputFile, String outputFilename) throws IOException, ParseException, SQLException {
         convert(inputFile, new File(outputFilename));
     }
 
     public void convert(File inputFile, File outputFile) throws IOException, ParseException, SQLException {
         convert(inputFile, new FileWriter(outputFile));
     }
 
     public void convert(String inputFilename, Writer writer) throws IOException, ParseException, SQLException {
         convert(new File(inputFilename), writer);
     }
 
     public void convert(String inputFilename, File outputFile) throws IOException, ParseException, SQLException {
         convert(inputFilename, new FileWriter(outputFile));
     }
 
     public void convert(String inputFilename, String outputFilename) throws IOException, ParseException, SQLException {
         convert(inputFilename, new File(outputFilename));
     }
 
     public static void main(String[] args) {
         new Accessioner().doMain(args);
     }
 
     public void doMain(String[] args) {
 
         CmdLineParser parser = new CmdLineParser(this);
         try {
             // parse the arguments.
             parser.parseArgument(args);
             // TODO check for extra arguments?
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             help = true;
         }
 
         if (help) {
             // print the list of available options
             parser.printUsage(System.err);
             System.exit(1);
             return;
         }
 
         try {
             doSetup();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             System.exit(1);
             return;
         } catch (SQLException e) {
             e.printStackTrace();
             System.exit(1);
             return;
         }
         
         log.debug("Looking for input files");
         List<File> inputFiles = new ArrayList<File>();
         inputFiles = FileUtils.getMatchesGlob(inputFilename);
         log.info("Found " + inputFiles.size() + " input files from "+inputFilename);
         Collections.sort(inputFiles);
 
         class AccessionTask implements Runnable {
             private final File inputFile;
             private final File outputFile;
 
             public AccessionTask(File inputFile, File outputFile) {
                 this.inputFile = inputFile;
                 this.outputFile = outputFile;
             }
 
             public void run() {
                 SampleData st = null;
                 try {
                     st = convert(this.inputFile);
                 } catch (ParseException e) {
                     System.err.println("ParseException converting " + this.inputFile);
                     e.printStackTrace();
                     exitcode = 1;
                     return;
                 } catch (IOException e) {
                     System.err.println("IOException converting " + this.inputFile);
                     e.printStackTrace();
                     exitcode = 1;
                     return;
                 } catch (SQLException e) {
                     System.err.println("SQLException converting " + this.inputFile);
                     e.printStackTrace();
                     exitcode = 1;
                     return;
                 }
 
                 FileWriter out = null;
                 try {
                     out = new FileWriter(this.outputFile);
                 } catch (IOException e) {
                     System.out.println("Error opening " + this.outputFile);
                     e.printStackTrace();
                     exitcode = 1;
                     return;
                 }
 
                 SampleTabWriter sampletabwriter = new SampleTabWriter(out);
                 try {
                     sampletabwriter.write(st);
                 } catch (IOException e) {
                     System.out.println("Error writing " + this.outputFile);
                     e.printStackTrace();
                     exitcode = 1;
                     return;
                 }
             }
         }
 
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads);
 
         for (File inputFile : inputFiles) {
             // System.out.println("Checking "+inputFile);
             File outputFile = new File(inputFile.getParentFile(), outputFilename);
             if (!outputFile.exists() 
                     || outputFile.lastModified() < inputFile.lastModified()) {
                 Runnable t = new AccessionTask(inputFile, outputFile);
                 if (threaded){
                     pool.execute(t);
                 } else {
                     t.run();
                 }
             }
         }
         
         // run the pool and then close it afterwards
         // must synchronize on the pool object
         synchronized (pool) {
             pool.shutdown();
             try {
                 // allow 24h to execute. Rather too much, but meh
                 pool.awaitTermination(1, TimeUnit.DAYS);
             } catch (InterruptedException e) {
                 log.error("Interuppted awaiting thread pool termination");
                 e.printStackTrace();
                 exitcode = 1;
                 return;
             }
         }
         log.info("Finished processing");
 
         System.exit(exitcode);
     }
 }
