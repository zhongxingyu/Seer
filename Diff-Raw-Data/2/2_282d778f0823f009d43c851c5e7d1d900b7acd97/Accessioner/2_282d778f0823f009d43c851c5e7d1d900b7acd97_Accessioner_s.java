 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLRecoverableException;
 import java.util.Collection;
 
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jolbox.bonecp.BoneCPDataSource;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.arrayexpress2.sampletab.validator.SampleTabValidator;
 
 public class Accessioner {
 
     private String hostname;
 
     private int port;
 
     private String database;
 
     private String username;
 
     private String password;
 
     private final SampleTabValidator validator = new SampleTabValidator();
     
     private final SampleTabSaferParser parser = new SampleTabSaferParser(validator);
     
     private BoneCPDataSource ds = null;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public Accessioner(String host, int port, String database, String username, String password)
             throws ClassNotFoundException, SQLException {
         // Setup the connection with the DB
         this.username = username;
         this.password = password;
         this.hostname = host;
         this.port = port;
         this.database = database;
     }
     
     public void close() {
         if (ds != null) {
             ds.close();
         }
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
 
     protected void bulkSamples(SampleData sd, String submissionID, String prefix, String table, int retries, Connection connect, DataSource ds) throws SQLException {
 
         //TODO check there are unaccessioned samples first
         
         PreparedStatement statement = null;
         ResultSet results = null;
         
         try {
             //first do one query to retrieve all that have already got accessions
             statement = connect.prepareStatement("SELECT USER_ACCESSION, ACCESSION FROM " + table
                     + " WHERE SUBMISSION_ACCESSION LIKE ? AND IS_DELETED = 0");
             statement.setString(1, submissionID);
             log.trace(statement.toString());
             results = statement.executeQuery();
             while (results.next()){
                 String samplename = results.getString(1).trim();
                 int accessionID = results.getInt(2);
                 String accession = prefix + accessionID;
                 SampleNode sample = sd.scd.getNode(samplename, SampleNode.class);
                 log.trace(samplename+" : "+accession);
                 if (sample != null){
                     sample.setSampleAccession(accession);
                 } else {
                     log.warn("Unable to find SCD sample node "+samplename+" in submission "+submissionID);
                 }
             }
         } catch (SQLRecoverableException e) {
             log.warn("Trying to recover from exception", e);
             if (retries > 0){
                 bulkSamples(sd, submissionID, prefix, table, retries-1, connect, ds);
             } else {
                 throw e;
             }
         } finally {
             if (statement != null){
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
             if (results != null){
                 try {
                     results.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
         }
     }
 
     protected void bulkGroups(SampleData sd, String submissionID, int retries, Connection connect, DataSource ds) throws SQLException {
 
         PreparedStatement statement = null;
         ResultSet results = null;
         
         try {
             statement = connect.prepareStatement("SELECT USER_ACCESSION, ACCESSION FROM SAMPLE_GROUPS WHERE SUBMISSION_ACCESSION LIKE ? AND IS_DELETED = 0");
             statement.setString(1, submissionID);
             log.trace(statement.toString());
             results = statement.executeQuery();
             while (results.next()){
                 String groupname = results.getString(1).trim();
                 int accessionID = results.getInt(2);
                 String accession = "SAMEG" + accessionID;
                 GroupNode group = sd.scd.getNode(groupname, GroupNode.class);
                 log.trace(groupname+" : "+accession);
                 if (group != null){
                     group.setGroupAccession(accession);
                 } else {
                     log.warn("Unable to find SCD group node "+groupname+" in submission "+submissionID);
                 }
             }
         } catch (SQLRecoverableException e) {
             log.warn("Trying to recover from exception", e);
             if (retries > 0){
                 bulkGroups(sd, submissionID, retries-1, connect, ds);
             } else {
                 throw e;
             }
         } finally {
             if (statement != null){
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
             if (results != null){
                 try {
                     results.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
         }
     }
 
     protected void singleSample(SampleData sd, SampleNode sample, String submissionID, String prefix, String table, int retries, Connection connect, DataSource ds) throws SQLException{
 
         PreparedStatement statement = null;
         ResultSet results = null;
 
         if (sample.getSampleAccession() == null) {
             try {
                     String name = sample.getNodeName().trim();
                     String accession = null;
                     
                     statement = connect.prepareStatement("SELECT ACCESSION FROM " + table
                             + " WHERE USER_ACCESSION LIKE ? AND SUBMISSION_ACCESSION LIKE ?");
                     statement.setString(1, name);
                     statement.setString(2, submissionID);
                     log.trace(statement.toString());
                     results = statement.executeQuery();
                     if (results.next()){
                         accession = prefix + results.getInt(1);
                         statement.close();
                         results.close();
                     } else {
                         log.info("Assigning new accession for "+submissionID+" : "+name);
                         
                         //insert it if not exists
                         statement = connect
                                 .prepareStatement("INSERT INTO "
                                         + table
                                         + " (USER_ACCESSION, SUBMISSION_ACCESSION, DATE_ASSIGNED, IS_DELETED) VALUES ( ? , ? , SYSDATE, 0 )");
                         statement.setString(1, name);
                         statement.setString(2, submissionID);
                         log.trace(statement.toString());
                         statement.executeUpdate();
                         statement.close();
         
                         statement = connect.prepareStatement("SELECT ACCESSION FROM " + table
                                 + " WHERE USER_ACCESSION LIKE ? AND SUBMISSION_ACCESSION LIKE ?");
                         statement.setString(1, name);
                         statement.setString(2, submissionID);
                         log.trace(statement.toString());
                         results = statement.executeQuery();
                         results.next();
                         accession = prefix + results.getInt(1);
                         statement.close();
                         results.close();
                     }
                     log.debug("Assigning " + accession + " to " + name);
                     sample.setSampleAccession(accession);
             } catch (SQLRecoverableException e) {
                 log.warn("Trying to recover from exception", e);
                 if (retries > 0){
                     singleSample(sd, sample, submissionID, prefix, table, retries-1, connect, ds);
                 } else {
                     throw e;
                 }
             } finally {
                 if (statement != null){
                     try {
                         statement.close();
                     } catch (SQLException e) {
                         //do nothing
                     }
                 }
                 if (results != null){
                     try {
                         results.close();
                     } catch (SQLException e) {
                         //do nothing
                     }
                 }
             }
         }
     }
 
     protected void singleGroup(SampleData sd, GroupNode group, String submissionID, int retries, Connection connect, DataSource ds) throws SQLException{
 
         PreparedStatement statement = null;
         ResultSet results = null;
         
         try {
             if (group.getGroupAccession() == null) {
 
                 String name = group.getNodeName();
                 String accession = null;
                 
                 statement = connect
                     .prepareStatement("SELECT ACCESSION FROM SAMPLE_GROUPS WHERE USER_ACCESSION = ? AND SUBMISSION_ACCESSION = ?");
                 statement.setString(1, name);
                 statement.setString(2, submissionID);
                 log.trace(statement.toString());
                 results = statement.executeQuery();
                 
                 if (results.next()){
                     accession = "SAMEG" + results.getInt(1);
                     statement.close();
                     results.close();
                 } else {
                 
                     log.info("Assigning new accession for "+submissionID+" : "+name);
                     
                     statement = connect
                             .prepareStatement("INSERT INTO SAMPLE_GROUPS ( USER_ACCESSION , SUBMISSION_ACCESSION , DATE_ASSIGNED , IS_DELETED ) VALUES ( ? ,  ? , SYSDATE, 0 )");
                     statement.setString(1, name);
                     statement.setString(2, submissionID);
                     log.info(name);
                     log.info(submissionID);
                     statement.executeUpdate();
                     statement.close();
     
                     statement = connect
                             .prepareStatement("SELECT ACCESSION FROM SAMPLE_GROUPS WHERE USER_ACCESSION = ? AND SUBMISSION_ACCESSION = ?");
                     statement.setString(1, name);
                     statement.setString(2, submissionID);
                     log.trace(statement.toString());
                     results = statement.executeQuery();
                     results.next();
                     int accessionID = results.getInt(1);
                     accession = "SAMEG" + accessionID;
                     statement.close();
                     results.close();
     
                     log.debug("Assigning " + accession + " to " + name);
                 }
                 group.setGroupAccession(accession);
             }
         } catch (SQLRecoverableException e) {
             log.warn("Trying to recover from exception", e);
             if (retries > 0){
                 singleGroup(sd, group, submissionID, retries-1, connect, ds);
             } else {
                 throw e;
             }
         } finally {
             if (statement != null){
                 try {
                     statement.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
             if (results != null){
                 try {
                     results.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
             if (connect != null){
                 try {
                     connect.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
         }
     }
     
     public SampleData convert(SampleData sampleIn) throws ParseException, SQLException {
         String table = null;
         String prefix = null;
         if (sampleIn.msi.submissionReferenceLayer == true) {
             prefix = "SAME";
             table = "SAMPLE_REFERENCE";
         } else if (sampleIn.msi.submissionReferenceLayer == false) {
             prefix = "SAMEA";
             table = "SAMPLE_ASSAY";
         } else {
             throw new ParseException("Must specify a Submission Reference Layer MSI attribute.");
         }
 
         String submission = sampleIn.msi.submissionIdentifier;
         if (submission == null){
             throw new ParseException("Submission Identifier cannot be null");
         }
         submission = submission.trim();
         
         log.info("Starting accessioning");
         
         
         String connectURI = "jdbc:oracle:thin:@"+hostname+":"+port+":"+database;
 
         try {
             Class.forName("oracle.jdbc.driver.OracleDriver");
         } catch (ClassNotFoundException e) {
             log.error("Unable to find oracle.jdbc.driver.OracleDriver", e);
             return null;
         }
         
         synchronized(this) {
             if (ds == null) {
                BoneCPDataSource ds = new BoneCPDataSource();
                 ds.setJdbcUrl(connectURI);
                 ds.setUsername(username);
                 ds.setPassword(password);
             }
         }
 
         Connection connect = null;
         try {     
             
             connect = ds.getConnection();
             
             //first do one query to retrieve all that have already got accessions
             //log.info("Starting bulkSamples");
             //bulkSamples(sampleIn, submission, prefix, table, 0, connect, ds);   
             //log.info("Starting bulkGroups");      
             //bulkGroups(sampleIn, submission, 0, connect, ds);                      
             
             
             //now assign and retrieve accessions for samples that do not have them
             log.info("Starting singleSample");      
             Collection<SampleNode> samples = sampleIn.scd.getNodes(SampleNode.class);
             for (SampleNode sample : samples) {
                 singleSample(sampleIn, sample, submission, prefix, table, 0, connect, ds);
             }
 
             log.info("Starting singleGroup");      
             Collection<GroupNode> groups = sampleIn.scd.getNodes(GroupNode.class);
             log.debug("got " + groups.size() + " groups.");
             for (GroupNode group : groups) {
                 singleGroup(sampleIn, group, submission, 0, connect, ds);
             }
             
         } catch (SQLException e) {
             throw e;
         } finally {
             if (connect != null) {
                 connect.close();
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
         SampleTabSaferParser stparser = new SampleTabSaferParser();
         log.info("created SampleTabParser<SampleData>, beginning parse");
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
 }
