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
 
 public class SampleTabAccessioner {
 
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
 
     // receives other command line parameters than options
     @Argument
     private List<String> arguments = new ArrayList<String>();
 
     private final SampleTabParser<SampleData> parser = new SampleTabParser<SampleData>();
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     // shared queue to cache instances in
     private static ConcurrentLinkedQueue<Connection> connectionQueue = new ConcurrentLinkedQueue<Connection>();
 
     public SampleTabAccessioner() throws ClassNotFoundException {
         // This will load the MySQL driver, each DB has its own driver
         Class.forName("com.mysql.jdbc.Driver");
     }
 
     public SampleTabAccessioner(String host, int port, String database, String username, String password)
             throws ClassNotFoundException {
         this();
         // Setup the connection with the DB
         this.username = username;
         this.password = password;
         this.hostname = host;
         this.port = port;
         this.database = database;
     }
 
     private Connection checkoutConnection() throws SQLException {
         //TODO fix this so there is a separate queue for different parameters
         //currently, it will mix and match freely if different params are used in the same VM
         Connection connect = connectionQueue.poll();
         if (connect == null) {
            log.info("Creating new connection");
             String connectionStr = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database;
             connect = DriverManager.getConnection(connectionStr, this.username, this.password);
         }
         return connect;
     }
 
     private void returnConnection(Connection connect) {
        try {
            connect.close();
        } catch (SQLException e) {
            //do nothing
         }
        return;
//        if (!connectionQueue.contains(connect)) {
//            log.info("Returning connection");
//            connectionQueue.add(connect);
//        }
     }
 
     public Logger getLog() {
         return log;
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
 
         String name;
         String submission = sampleIn.msi.submissionIdentifier;
         PreparedStatement statement;
         ResultSet results;
         int accessionID;
         String accession;
 
         Collection<SampleNode> samples = sampleIn.scd.getNodes(SampleNode.class);
 
         getLog().debug("got " + samples.size() + " samples.");
         Connection connect = checkoutConnection();
         try {
             for (SampleNode sample : samples) {
                 if (sample.sampleAccession == null) {
                     name = sample.getNodeName();
                     statement = connect
                             .prepareStatement("INSERT IGNORE INTO "
                                     + table
                                     + " (user_accession, submission_accession, date_assigned, is_deleted) VALUES (?, ?, NOW(), 0)");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     statement.executeUpdate();
 
                     statement = connect.prepareStatement("SELECT accession FROM " + table
                             + " WHERE user_accession = ? AND submission_accession = ?");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     results = statement.executeQuery();
                     results.first();
                     accessionID = results.getInt(1);
                     accession = prefix + accessionID;
 
                     getLog().debug("Assigning " + accession + " to " + name);
                     sample.sampleAccession = accession;
                 }
             }
 
             Collection<GroupNode> groups = sampleIn.scd.getNodes(GroupNode.class);
 
             getLog().debug("got " + groups.size() + " groups.");
             for (GroupNode group : groups) {
                 if (group.groupAccession == null) {
                     name = group.getNodeName();
                     statement = connect
                             .prepareStatement("INSERT IGNORE INTO sample_groups (user_accession, submission_accession, date_assigned, is_deleted) VALUES (?, ?, NOW(), 0)");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     statement.executeUpdate();
 
                     statement = connect
                             .prepareStatement("SELECT accession FROM sample_groups WHERE user_accession = ? AND submission_accession = ?");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     results = statement.executeQuery();
                     results.first();
                     accessionID = results.getInt(1);
                     accession = "SAMEG" + accessionID;
 
                     getLog().debug("Assigning " + accession + " to " + name);
                     group.groupAccession = accession;
                 }
             }
         } finally {
             returnConnection(connect);
         }
         
         Corrector.correct(sampleIn);
 
         return sampleIn;
     }
 
     public void convert(SampleData sampleIn, Writer writer) throws IOException, ParseException, SQLException {
         getLog().debug("recieved magetab, preparing to convert");
         SampleData sampleOut = convert(sampleIn);
         getLog().debug("sampletab converted, preparing to output");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         getLog().debug("created SampleTabWriter");
         sampletabwriter.write(sampleOut);
         sampletabwriter.close();
 
     }
 
     public void convert(File sampletabFile, Writer writer) throws IOException, ParseException, SQLException {
         getLog().debug("preparing to load SampleData");
         SampleTabParser<SampleData> stparser = new SampleTabParser<SampleData>();
         getLog().debug("created MAGETABParser<SampleData>");
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
         try {
             new SampleTabAccessioner().doMain(args);
         } catch (ClassNotFoundException e) {
             System.err.println("Unable to find com.mysql.jdbc.Driver");
             e.printStackTrace();
         }
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
             System.err.println();
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
                     System.exit(exitcode);
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
