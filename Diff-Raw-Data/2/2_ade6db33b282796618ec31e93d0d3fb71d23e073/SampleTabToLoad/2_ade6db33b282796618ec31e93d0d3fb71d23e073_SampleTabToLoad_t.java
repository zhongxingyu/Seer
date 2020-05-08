 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SCDNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNamedAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DerivedFromAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.NamedAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.arrayexpress2.sampletab.validator.LoadValidator;
 import uk.ac.ebi.arrayexpress2.sampletab.validator.SampleTabValidator;
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 
 public class SampleTabToLoad {
 
     @Option(name = "-h", usage = "display help")
     private boolean help;
 
     @Option(name = "-i", aliases={"--input"}, usage = "input filename or glob")
     private String inputFilename;
 
     @Option(name = "-o", aliases={"--output"}, usage = "output filename relative to input")
     private String outputFilename;
 
     @Option(name = "-f", aliases={"--force"}, usage = "force replace existing newer files")
     private boolean force;
 
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
 
     private final SampleTabValidator validator = new LoadValidator();
     private final SampleTabSaferParser parser = new SampleTabSaferParser(validator);
     private Accessioner accessioner;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     private int exitcode = 0;
     
     
     public SampleTabToLoad() {
         // do nothing
     }
 
     public SampleTabToLoad(String host, int port, String database, String username, String password)
             throws ClassNotFoundException {
         this();
         // Setup the connection with the DB
         this.username = username;
         this.password = password;
         this.hostname = host;
         this.port = port;
         this.database = database;
         
         try {
             this.accessioner = new Accessioner(hostname, 
                     port, database, username, password);
         } catch (ClassNotFoundException e) {
             log.error("Unable to create accessioner", e);
         } catch (SQLException e) {
             log.error("Unable to create accessioner", e);
         }
     }
 
     public SampleTabToLoad(Accessioner accessioner)
             throws ClassNotFoundException {
         this();
         this.accessioner = accessioner;
     }
     
     public Logger getLog() {
         return log;
     }
 
     public SampleData convert(String sampleTabFilename) throws IOException, ParseException, ClassNotFoundException, SQLException {
         return convert(new File(sampleTabFilename));
     }
 
     public SampleData convert(File sampleTabFile) throws IOException, ParseException, ClassNotFoundException, SQLException {
         return convert(parser.parse(sampleTabFile));
     }
 
     public SampleData convert(SampleData sampledata) throws ParseException, SQLException, ClassNotFoundException {
 
         // this is stuff for loading to BioSD
         // not actually part of SampleTab spec
         
         //make sure the msi contains no duplicates
         sampledata.msi.databases = new ArrayList<Database>(new HashSet<Database>(sampledata.msi.databases));
         sampledata.msi.publications = new ArrayList<Publication>(new HashSet<Publication>(sampledata.msi.publications));
         sampledata.msi.termSources = new ArrayList<TermSource>(new HashSet<TermSource>(sampledata.msi.termSources));
         
         //must have a description
         if (sampledata.msi.submissionDescription == null || sampledata.msi.submissionDescription.trim().length() == 0){
             sampledata.msi.submissionDescription = sampledata.msi.submissionTitle;
         }
         
         // All samples must be in a group
         // so create a new group and add all non-grouped samples to it
         GroupNode othergroup = new GroupNode("Other Group");
         for (SampleNode sample : sampledata.scd.getNodes(SampleNode.class)) {
             // check there is not an existing group first...
             boolean inGroup = false;
             //even if it has child nodes, both parent and child must be in a group
             //this will lead to some weird looking row duplications, but since this is an internal 
             //intermediate file it is not important
 //            for (Node n : sample.getChildNodes()){
 //                if (GroupNode.class.isInstance(n)){
 //                    inGroup = true;
 //                }
 //            }
             
             if (!inGroup){
                 log.debug("Adding sample " + sample.getNodeName() + " to group " + othergroup.getNodeName());
                 othergroup.addSample(sample);
             }
         }
         //only add the new group if it has any samples
         if (othergroup.getParentNodes().size() > 0){
             sampledata.scd.addNode(othergroup);
             log.info("Added Other group node");
             // also need to accession the new node
         }
         
         //copy MSI database attribute to each sample that does not have a link to that database
         // no need to do this, GUI will handle it
 //        for (Database databasemsi : sampledata.msi.databases){
 //            for (SampleNode sample : sampledata.scd.getNodes(SampleNode.class)) {
 //                boolean hasdb = false;
 //                for (SCDNodeAttribute attr : sample.getAttributes()){
 //                    if (DatabaseAttribute.class.isInstance(attr)){
 //                        DatabaseAttribute dbattr = (DatabaseAttribute) attr;
 //                        if (dbattr.getAttributeValue().equals(databasemsi.getName())){
 //                            hasdb = true;
 //                        }
 //                    }
 //                }
 //                if (!hasdb){
 //                    DatabaseAttribute dbattr = new DatabaseAttribute(databasemsi.getName(), databasemsi.getID(), databasemsi.getURI());
 //                    sample.addAttribute(dbattr);
 //                }
 //            }
 //        }
         
         //replace implicit derived from with explicit derived from relationships
         for (SampleNode sample : sampledata.scd.getNodes(SampleNode.class)) {
             if (sample.getParentNodes().size() > 0){
                for (Node parent : new HashSet<Node>(sample.getParentNodes())){
                     if (SampleNode.class.isInstance(parent)){
                         SampleNode parentsample = (SampleNode) parent;
                         DerivedFromAttribute attr = new DerivedFromAttribute(parentsample.getSampleAccession());
                         sample.addAttribute(attr);
                         sample.removeParentNode(parentsample);
                         parentsample.removeChildNode(sample);
                     }
                 }
             }
         }
 
         // Copy msi information on to each group node
         for (SCDNode group : sampledata.scd.getNodes(GroupNode.class)) {
             group.addAttribute(new NamedAttribute("Submission Title", sampledata.msi.submissionTitle));
             group.addAttribute(new NamedAttribute("Submission Description", sampledata.msi.submissionDescription));
             group.addAttribute(new NamedAttribute("Submission Identifier", sampledata.msi.submissionIdentifier));
             group.addAttribute(new NamedAttribute("Submission Release Date", sampledata.msi.getSubmissionReleaseDateAsString()));
             group.addAttribute(new NamedAttribute("Submission Update Date", sampledata.msi.getSubmissionUpdateDateAsString()));
             group.addAttribute(new NamedAttribute("Submission Version", sampledata.msi.submissionVersion));
             group.addAttribute(new NamedAttribute("Submission Reference Layer", sampledata.msi.submissionReferenceLayer.toString()));
             
             log.info("Added attributes to group "+group.getNodeName());
             
             // Have to do this for each group of tags (Person *, Database *, etc)
             // and complete each individual in each group before starting the next one
             // E.g. Person Last Name, Person First Name, Person Last Name, Person First Name
             // not E.g. Person Last Name, Person Last Name, Person First Name, Person First Name
             for(Person per : sampledata.msi.persons){
                 group.addAttribute(new NamedAttribute("Person Last Name", per.getLastName()));
                 if (per.getInitials() != null && per.getInitials().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Person Initials", per.getInitials()));
                 }
                 if (per.getFirstName() != null && per.getFirstName().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Person First Name", per.getFirstName()));
                 }
                 if (per.getEmail() != null && per.getEmail().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Person Email", per.getEmail()));
                 }
                 if (per.getRole() != null && per.getRole().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Person Role", per.getRole()));
                 }
             }
             
             log.debug("Added persons");
             
             for(Organization org : sampledata.msi.organizations){
                 group.addAttribute(new NamedAttribute("Organization Name", org.getName()));
                 if (org.getAddress() != null && org.getAddress().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Organization Address", org.getAddress()));
                 }
                 if (org.getURI() != null && org.getURI().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Organization URI", org.getURI()));
                 }
                 if (org.getEmail() != null && org.getEmail().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Organization Email", org.getEmail()));
                 }
                 if (org.getRole() != null && org.getRole().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Organization Role", org.getRole()));
                 }
             }
             log.debug("Added organizations");
             
             for(Publication pub: sampledata.msi.publications){
                 if (pub.getDOI() != null && pub.getDOI().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Publication DOI", pub.getDOI()));
                 }
                 if (pub.getPubMedID() != null && pub.getPubMedID().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Publication PubMed ID", pub.getPubMedID()));
                 }
             }
             log.debug("Added publications");
             
             for (TermSource ts : sampledata.msi.termSources) {
                 if (ts.getName() != null && ts.getName().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Term Source Name", ts.getName()));
                     if (ts.getURI() != null && ts.getURI().trim().length() > 0){
                         group.addAttribute(new NamedAttribute("Term Source URI", ts.getURI()));
                     }
                     if (ts.getVersion() != null && ts.getVersion().trim().length() > 0){
                         group.addAttribute(new NamedAttribute("Term Source Version", ts.getVersion()));
                     }
                 }
             }
             log.debug("Added termsources");
             
             for (Database db : sampledata.msi.databases){
                 if (db.getName() != null && db.getName().trim().length() > 0){
                     group.addAttribute(new NamedAttribute("Database Name", db.getName()));
                     group.addAttribute(new NamedAttribute("Database URI", db.getURI()));
                     group.addAttribute(new NamedAttribute("Database ID", db.getID()));
                 }
             }
             log.debug("Added databases");
         }
 
         log.info("completed initial conversion, re-accessioning...");
         
         // assign accession to any created groups
         sampledata = accessioner.convert(sampledata);
         
         return sampledata;
     }
 
     public void convert(SampleData st, Writer writer) throws IOException, ParseException, ClassNotFoundException, SQLException {
         st = convert(st);
         getLog().info("sampletab converted, preparing to output");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         getLog().info("created SampleTabWriter, preparing to write");
         sampletabwriter.write(st);
         sampletabwriter.close();
     }
 
     public void convert(SampleData st, String ouputfilename) throws IOException, ParseException, ClassNotFoundException, SQLException {
         convert(st, new File(ouputfilename));
     }
 
     public void convert(SampleData st, File outfile) throws IOException, ParseException, ClassNotFoundException, SQLException {
         convert(st, new FileWriter(outfile));
     }
 
     public void convert(File infile, File outfile) throws IOException, ParseException, ClassNotFoundException, SQLException {
         convert(parser.parse(infile), outfile);
     }
 
     public void convert(String infilename, String outfilename) throws IOException, ParseException, ClassNotFoundException, SQLException {
         convert(new File(infilename), new File(outfilename));
     }
 
     class ToLoadTask implements Runnable {
         private final File inputFile;
         private final File outputFile;
 
         private Logger log = LoggerFactory.getLogger(getClass());
 
         public ToLoadTask(File inputFile, File outputFile) {
             this.inputFile = inputFile;
             this.outputFile = outputFile;
         }
 
         public void run() {
             log.info("Processing " + inputFile);
 
             SampleData st = null;
             SampleTabToLoad toloader;
             try {
                 toloader = new SampleTabToLoad(hostname, port, database, username, password);
             } catch (ClassNotFoundException e) {
                 log.error("ClassNotFoundException converting " + inputFile, e);
                 exitcode = 1;
                 return;
             }
             
             // do initial parsing and conversion
             try {
                 st = toloader.convert(inputFile);
             } catch (ParseException e) {
                 log.error("ParseException converting " + inputFile, e);
                 exitcode = 1;
                 return;
             } catch (IOException e) {
                 log.error("IOException converting " + inputFile, e);
                 exitcode = 2;
                 return;
             } catch (ClassNotFoundException e) {
                 log.error("ClassNotFoundException converting " + inputFile, e);
                 exitcode = 3;
                 return;
             } catch (SQLException e) {
                 log.error("SQLException converting " + inputFile, e);
                 exitcode = 4;
                 return;
             }
 
             getLog().info("sampletab converted, preparing to output");
             
             // write back out
             FileWriter out = null;
             try {
                 out = new FileWriter(outputFile);
             } catch (IOException e) {
                 log.error("Error opening " + outputFile, e);
                 exitcode = 5;
                 return;
             }
 
             SampleTabWriter sampletabwriter = new SampleTabWriter(out);
             try {
                 sampletabwriter.write(st);
                 sampletabwriter.close();
             } catch (IOException e) {
                 log.error("Error writing " + outputFile, e);
                 exitcode = 6;
                 return;
             }
 
             log.debug("Processed " + inputFile);
 
         }
     }
 
     public static void main(String[] args) {
         new SampleTabToLoad().doMain(args);
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
             parser.printSingleLineUsage(System.err);
             System.err.println();
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
         
         if (accessioner == null){
             try {
                 accessioner = new Accessioner(hostname, 
                         port, database, username, password);
             } catch (ClassNotFoundException e) {
                 log.error("Unable to create accessioner", e);
             } catch (SQLException e) {
                 log.error("Unable to create accessioner", e);
             }
         }
 
         log.info("Looking for input files " + inputFilename);
         List<File> inputFiles = new ArrayList<File>();
         inputFiles = FileUtils.getMatchesGlob(inputFilename);
         log.info("Found " + inputFiles.size() + " input files");
         Collections.sort(inputFiles);
         
         if (inputFiles.size() == 0){
             log.error("No input files found");
             System.exit(3);
             return;
         }
 
         int nothreads = Runtime.getRuntime().availableProcessors();
         ExecutorService pool = Executors.newFixedThreadPool(nothreads);
 
         for (File inputFile : inputFiles) {
             // System.out.println("Checking "+inputFile);
             File outputFile = new File(inputFile.getParentFile(), outputFilename);
             if (force 
                     || !outputFile.exists()
                     || outputFile.lastModified() < inputFile.lastModified()) {
                 Runnable t = new ToLoadTask(inputFile, outputFile);
                 if (threaded) {
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
                 log.error("Interupted awaiting thread pool termination", e);
                 exitcode = 7;
             }
         }
         log.info("Finished processing");
         System.exit(exitcode);
     }
 }
