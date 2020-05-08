 package tuit.main;
 
 import blast.specification.BLASTIdentifier;
 import blast.specification.TUITBLASTIdentifier;
 import blast.specification.cutoff.TUITCutoffSet;
 import db.mysql.MySQL_Connector;
 import exception.TUITPropertyBadFormatException;
 import helper.NCBITablesDeployer;
 import io.file.NucleotideFastaTUITFileOperator;
 import io.file.TUTFileOperatorHelper;
 import io.properties.jaxb.Database;
 import io.properties.jaxb.SpecificationParameters;
 import io.properties.jaxb.TUITProperties;
 import io.properties.load.TUITPropertiesLoader;
 import logger.Log;
 import org.apache.commons.cli.*;
 import org.xml.sax.SAXException;
 import taxonomy.Ranks;
 
 import javax.xml.bind.JAXBException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 
 /**
  * A main class for the tuit module implementation
  */
 
 public class tuit {
 
     /**
      * The -in flag for the input file
      */
     private final static String IN = "i";
     /**
      * The -out flag for the output file
      */
     private final static String OUT = "o";
     /**
      * The -p flag for the properties file
      */
     private final static String P = "p";
     /**
      * The -b flag for the pre-blasted BLAST output file
      */
     private final static String B = "b";
     /**
      * The -v flag for the verbose output file
      */
     private final static String V = "v";
     /**
      * The -deploy flag for the verbose output file
      */
     private final static String DEPLOY = "deploy";
     /**
      * The -update flag for the verbose output file
      */
     private final static String UPDATE = "update";
     /**
      * tuit output file extension
      */
     private final static String TUIT_EXT = ".tuit";
 
     @SuppressWarnings("ConstantConditions")
     public static void main(String[] args) {
         //Declare variables
         File inputFile;
         File outputFile;
         File tmpDir;
         File blastnExecutable;
         File properties;
         File blastOutputFile = null;
         //
         TUITPropertiesLoader tuitPropertiesLoader;
         TUITProperties tuitProperties;
         //
         String[] parameters=null;
         //
         Connection connection = null;
         MySQL_Connector mySQL_connector;
         //
         Map<Ranks, TUITCutoffSet> cutoffMap;
         //
         BLASTIdentifier blastIdentifier = null;
 
         CommandLineParser parser = new GnuParser();
         Options options = new Options();
         options.addOption(tuit.IN, "input<file>", true, "Input file (currently fasta-formatted only)");
         options.addOption(tuit.OUT, "output<file>", true, "Output file (in " + tuit.TUIT_EXT + " format)");
         options.addOption(tuit.P, "prop<file>", true, "Properties file (XML formatted)");
         options.addOption(tuit.V, "verbose", false, "Enable verbose output");
         options.addOption(tuit.B, "blast_output<file>", true, "Perform on a pre-BLASTed output");
         options.addOption(tuit.DEPLOY, "deploy", false, "Deploy the taxonomic databases");
         options.addOption(tuit.UPDATE, "update", false, "Update the taxonomic databases");
         HelpFormatter formatter = new HelpFormatter();
 
         try {
 
             //Setup logger
             Log.getInstance().setLogName("tuit.log");
             //Read command line
             CommandLine commandLine = parser.parse(options, args, true);
             if (!commandLine.hasOption(tuit.P)) {
                 throw new ParseException("No properties file option found, exiting.");
             } else {
                 properties = new File(commandLine.getOptionValue(tuit.P));
             }
             //Load properties
             tuitPropertiesLoader = TUITPropertiesLoader.newInstanceFromFile(properties);
             tuitProperties = tuitPropertiesLoader.getTuitProperties();
             //Connect to the database
             mySQL_connector = MySQL_Connector.newDefaultInstance(
                     "jdbc:mysql://" + tuitProperties.getDBConnection().getUrl().trim() + "/",
                     tuitProperties.getDBConnection().getLogin().trim(),
                     tuitProperties.getDBConnection().getPassword().trim());
             mySQL_connector.connectToDatabase();
             connection = mySQL_connector.getConnection();
             //Create tmp directory and blastn executable
             tmpDir = new File(tuitProperties.getTMPDir().getPath());
             blastnExecutable = new File(tuitProperties.getBLASTNPath().getPath());
 
             //Check for deploy
             if (commandLine.hasOption(tuit.DEPLOY)) {
                 NCBITablesDeployer.fastDeployNCBIDatabasesFromNCBI(connection, tmpDir);
                 Log.getInstance().log(Level.FINE, "Task done, exiting...");
                 return;
             }
             //Check for update
             if (commandLine.hasOption(tuit.UPDATE)) {
                 NCBITablesDeployer.updateDatabasesFromNCBI(connection, tmpDir);
                 Log.getInstance().log(Level.FINE, "Task done, exiting...");
                 return;
             }
             if (commandLine.hasOption(tuit.B)) {
                 blastOutputFile = new File(commandLine.getOptionValue(tuit.B));
                 if (!blastOutputFile.exists() || !blastOutputFile.canRead()) {
                     throw new Exception("BLAST output file either does not exist, or is not readable.");
                 } else if (blastOutputFile.isDirectory()) {
                     throw new Exception("BLAST output file points to a directory.");
                 }
             }
             //Check vital parameters
             if (!commandLine.hasOption(tuit.IN)) {
                 throw new ParseException("No input file option found, exiting.");
             } else {
                 inputFile = new File(commandLine.getOptionValue(tuit.IN));
                 Log.getInstance().setLogName(inputFile.getName().split("\\.")[0] + ".tuit.log");
             }
             //Correct the output file option if needed
             if (!commandLine.hasOption(tuit.OUT)) {
                 outputFile = new File((inputFile.getPath()).split("\\.")[0] + tuit.TUIT_EXT);
             } else {
                 outputFile = new File(commandLine.getOptionValue(tuit.OUT));
             }
 
             //Adjust the output level
             if (commandLine.hasOption(tuit.V)) {
                 Log.getInstance().setLevel(Level.FINE);
                 Log.getInstance().log(Level.INFO, "Using verbose output for the log");
             } else {
                 Log.getInstance().setLevel(Level.INFO);
             }
             //Try all files
             if (inputFile != null) {
                 if (!inputFile.exists() || !inputFile.canRead()) {
                     throw new Exception("Input file either does not exist, or is not readable.");
                 } else if (inputFile.isDirectory()) {
                     throw new Exception("Input file points to a directory.");
                 }
             }
 
             if (!properties.exists() || !properties.canRead()) {
                 throw new Exception("Properties file either does not exist, or is not readable.");
             } else if (properties.isDirectory()) {
                 throw new Exception("Properties file points to a directory.");
             }
 
             //Create blast parameters
             StringBuilder stringBuilder = new StringBuilder();
             //stringBuilder.append("\"");
             for (Database database : tuitProperties.getBLASTNParameters().getDatabase()) {
                 stringBuilder.append(database.getUse());
                 stringBuilder.append(" ");//Gonna insert an extra space for the last database
             }
             //stringBuilder.append("\"");
             String remote;
             String entrez_query;
             if (tuitProperties.getBLASTNParameters().getRemote().getDelegate().equals("yes")) {
                 remote = "-remote";
                 entrez_query = "-entrez_query";
                 parameters = new String[]{
                         "-db", stringBuilder.toString(),
                         remote,
                         entrez_query, tuitProperties.getBLASTNParameters().getEntrezQuery().getValue(),
                         "-evalue", tuitProperties.getBLASTNParameters().getExpect().getValue()
                 };
             } else {
                 if (!commandLine.hasOption(tuit.B)) {
                     if (tuitProperties.getBLASTNParameters().getEntrezQuery().getValue().toUpperCase().startsWith("NOT") || tuitProperties.getBLASTNParameters().getEntrezQuery().getValue().toUpperCase().startsWith("ALL")) {
                         parameters = new String[]{
                                 "-db", stringBuilder.toString(),
                                 "-evalue", tuitProperties.getBLASTNParameters().getExpect().getValue(),
                                 "-negative_gilist", TUTFileOperatorHelper.restrictToEntrez(
                                 tmpDir, tuitProperties.getBLASTNParameters().getEntrezQuery().getValue().toUpperCase().replace("NOT", "OR")).getAbsolutePath()
                         };
                     } else {
                         parameters = new String[]{
                                 "-db", stringBuilder.toString(),
                                 "-evalue", tuitProperties.getBLASTNParameters().getExpect().getValue(),
                                 "-gilist", TUTFileOperatorHelper.restrictToEntrez(
                                 tmpDir, tuitProperties.getBLASTNParameters().getEntrezQuery().getValue()).getAbsolutePath()
                         };
                     }
                 }
             }
             //Prepare a cutoff Map
             if (tuitProperties.getSpecificationParameters() != null && tuitProperties.getSpecificationParameters().size() > 0) {
                 cutoffMap = new HashMap<Ranks, TUITCutoffSet>(tuitProperties.getSpecificationParameters().size());
                 for (SpecificationParameters specificationParameters : tuitProperties.getSpecificationParameters()) {
                     cutoffMap.put(Ranks.valueOf(specificationParameters.getCutoffSet().getRank()),
                             TUITCutoffSet.newDefaultInstance(
                                     Double.parseDouble(specificationParameters.getCutoffSet().getPIdentCutoff().getValue()),
                                     Double.parseDouble(specificationParameters.getCutoffSet().getQueryCoverageCutoff().getValue()),
                                     Double.parseDouble(specificationParameters.getCutoffSet().getEvalueRatioCutoff().getValue())));
                 }
             } else {
                 cutoffMap = new HashMap<Ranks, TUITCutoffSet>();
             }
             NucleotideFastaTUITFileOperator nucleotideFastaTUITFileOperator = NucleotideFastaTUITFileOperator.newInstance();
             nucleotideFastaTUITFileOperator.setInputFile(inputFile);
             nucleotideFastaTUITFileOperator.setOutputFile(outputFile);
             //Create blast identifier
             ExecutorService executorService = Executors.newSingleThreadExecutor();
             if (blastOutputFile == null) {
                 blastIdentifier = TUITBLASTIdentifier.newInstanceFromFileOperator(
                         tmpDir, blastnExecutable, parameters,
                         nucleotideFastaTUITFileOperator, connection,
                         cutoffMap, Integer.parseInt(tuitProperties.getBLASTNParameters().getMaxFilesInBatch().getValue()));
 
             } else {
                 try {
                     blastIdentifier = TUITBLASTIdentifier.newInstanceFromBLASTOutput(nucleotideFastaTUITFileOperator, connection,
                             cutoffMap, blastOutputFile, Integer.parseInt(tuitProperties.getBLASTNParameters().getMaxFilesInBatch().getValue()));
 
                 }catch (JAXBException e){
                     Log.getInstance().log(Level.SEVERE, "Error reading " + blastOutputFile.getName() + ", please check input. The file must be XML formatted.");
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             Future<?> runnableFuture = executorService.submit(blastIdentifier);
             runnableFuture.get();
             executorService.shutdown();
 
         } catch (ParseException pe) {
             Log.getInstance().log(Level.SEVERE, (pe.getMessage()));
             formatter.printHelp("tuit", options);
         } catch (SAXException saxe) {
             Log.getInstance().log(Level.SEVERE, saxe.getMessage());
         } catch (FileNotFoundException fnfe) {
             Log.getInstance().log(Level.SEVERE, fnfe.getMessage());
         } catch (TUITPropertyBadFormatException tpbfe) {
             Log.getInstance().log(Level.SEVERE, tpbfe.getMessage());
         } catch (JAXBException jaxbee) {
             Log.getInstance().log(Level.SEVERE, "The properties file is not well formatted. Please ensure that the XML is consistent with the io.properties.dtd schema.");
         } catch (ClassNotFoundException cnfe) {
             //Probably won't happen unless the library deleted from the .jar
             Log.getInstance().log(Level.SEVERE, cnfe.getMessage());
         } catch (SQLException sqle) {
             Log.getInstance().log(Level.SEVERE, "A database communication error occurred with the following message:\n" +
                     sqle.getMessage());
             if (sqle.getMessage().contains("Access denied for user")) {
                 Log.getInstance().log(Level.SEVERE, "Please use standard database login: " + NCBITablesDeployer.login + " and password: " + NCBITablesDeployer.password);
             }
         } catch (Exception e) {
             Log.getInstance().log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException sqle) {
                     Log.getInstance().log(Level.SEVERE, "Problem closing the database connection: " + sqle);
                 }
             }
             Log.getInstance().log(Level.FINE, "Task done, exiting...");
         }
     }
 }
