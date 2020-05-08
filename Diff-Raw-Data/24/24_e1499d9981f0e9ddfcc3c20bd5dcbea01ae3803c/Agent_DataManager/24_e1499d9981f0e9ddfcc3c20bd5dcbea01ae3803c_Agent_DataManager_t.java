 package pikater;
 
 import jade.content.lang.Codec;
 import jade.content.lang.Codec.CodecException;
 import jade.content.lang.sl.SLCodec;
 import jade.content.onto.Ontology;
 import jade.content.onto.OntologyException;
 import jade.content.onto.basic.Action;
 import jade.content.onto.basic.Result;
 import jade.core.Agent;
 import jade.core.Profile;
 import jade.domain.FIPAAgentManagement.NotUnderstoodException;
 import jade.domain.FIPAAgentManagement.RefuseException;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.proto.AchieveREResponder;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Iterator;
 import jade.util.leap.List;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import java.util.LinkedList;
 import java.util.regex.Pattern;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import pikater.ontology.messages.DeleteTempFiles;
 
 import pikater.ontology.messages.Eval;
 import pikater.ontology.messages.GetAllMetadata;
 import pikater.ontology.messages.GetFileInfo;
 import pikater.ontology.messages.GetFiles;
 import pikater.ontology.messages.GetTheBestAgent;
 import pikater.ontology.messages.ImportFile;
 import pikater.ontology.messages.LoadResults;
 import pikater.ontology.messages.MessagesOntology;
 import pikater.ontology.messages.Metadata;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.SaveMetadata;
 import pikater.ontology.messages.SaveResults;
 import pikater.ontology.messages.SavedResult;
 import pikater.ontology.messages.Task;
 import pikater.ontology.messages.TranslateFilename;
 import pikater.ontology.messages.UpdateMetadata;
 
 public class Agent_DataManager extends Agent {
 
     private static final long serialVersionUID = 1L;
     Connection db;
     Logger log;
     Codec codec = new SLCodec();
     Ontology ontology = MessagesOntology.getInstance();
     
     String db_url;
     String db_user;
     String db_password;
     
     private void openDBConnection() throws SQLException{
     	// db = DriverManager.getConnection("jdbc:mysql://174.120.245.222/marp_pikater", "marp_pikater", "pikater");
     	db = DriverManager.getConnection(db_url, db_user, db_password);    	
     }
     
     @Override
     protected void setup() {
         
     	try {
             //db = DriverManager.getConnection(
             //        "jdbc:hsqldb:file:data/db/pikaterdb", "", "");
 
         	Object[] args = getArguments();
         	System.out.println(args);
     		if (args != null && args.length > 0) {
     			int i = 0;
     			while (i < args.length){
     				System.out.println(args[i]);
     				if (args[i].equals("url")){
     					db_url = (String)args[i+1];    					
     				}
     				if (args[i].equals("user")){
     					db_user = (String)args[i+1];    					
     				}
     				if (args[i].equals("password")){
     					db_password = (String)args[i+1];    					
     				}
     				i++;
     			}
     			if (db_user == null){
 					db_user = "";    					
 				}
     			if (db_password == null){
 					db_password = "";    					
 				}			
     		}
     		else{
     		    db_url = "jdbc:mysql://174.120.245.222/marp_pikater";
     		    db_user = "marp_pikater";
     		    db_password = "pikater";
     		}
         	
     		System.out.println("Connecting to " + db_url + ".");
     		// System.out.println("user " + db_user);
     		// System.out.println("password " + db_password);
     		openDBConnection();
         	
     		String hostAddress = this.getProperty(Profile.MAIN_HOST, null);
     		
             Logger.getRootLogger().addAppender(
                     new FileAppender(new PatternLayout(
                     "%r [%t] %-5p %c - %m%n"), "log_" + hostAddress));
 
             log = Logger.getLogger(Agent_DataManager.class);
             log.setLevel(Level.TRACE);
 
         } catch (SQLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     	
         getContentManager().registerLanguage(codec);
         getContentManager().registerOntology(ontology);
         
 //        try {
 //    		loadMetadataFromFile("metadata");
 //    	} catch (IOException e1) {
 //    		// TODO Auto-generated catch block
 //    		e1.printStackTrace();
 //    	} catch (SQLException e1) {
 //    		// TODO Auto-generated catch block
 //    		e1.printStackTrace();
 //    	}
 
         
         LinkedList<String> tableNames = new LinkedList<String>();
         LinkedList<String> triggerNames = new LinkedList<String>();
         try {
             String[] types = {"TABLE", "VIEW"};
             ResultSet tables = db.getMetaData().getTables(null, null, "%",
                     types);
             while (tables.next()) {
                 tableNames.add(tables.getString(3).toUpperCase());
             }
 
             ResultSet triggers = db.createStatement().executeQuery(
                     "SELECT trigger_name FROM INFORMATION_SCHEMA.TRIGGERS");
             while (triggers.next()) {
                 triggerNames.add(triggers.getString("trigger_name"));
             }
 
         } catch (SQLException e) {
             log.error("Error getting tables list: " + e.getMessage());
             e.printStackTrace();
         }
 
         log.info("Found the following tables: ");
         for (String s : tableNames) {
             log.info(s);
         }
 
         log.info("Found the following triggers: ");
         for (String s : triggerNames) {
             log.info(s);
         }
 
         File data = new File("data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + "temp");
         if (!data.exists()) {
             log.info("Creating directory data/files");
             if (data.mkdirs()) {
                 log.info("Succesfully created directory data/files");
             } else {
                 log.error("Error creating directory data/files");
             }
         }
 
         try {
             if (!tableNames.contains("FILEMAPPING")) {
                 log.info("Creating table FILEMAPPING");
                 db.createStatement().executeUpdate(
                         "CREATE TABLE filemapping (userID INTEGER NOT NULL, externalFilename VARCHAR(256) NOT NULL, internalFilename CHAR(32) NOT NULL, PRIMARY KEY (userID, externalFilename))");
             }
         } catch (SQLException e) {
             log.fatal("Error creating table FILEMAPPING: " + e.getMessage());
             e.printStackTrace();
         }
 
         try {
             if (!tableNames.contains("METADATA")) {
                 log.info("Creating table METADATA");
                 db.createStatement().executeUpdate(
                         "CREATE TABLE metadata (" + "externalFilename VARCHAR(256) NOT NULL, " + "internalFilename CHAR(32) NOT NULL, " + "defaultTask VARCHAR(256), " + "attributeType VARCHAR(256), " + "numberOfInstances INTEGER, " + "numberOfAttributes INTEGER, " + "missingValues BOOLEAN, " + "PRIMARY KEY (internalFilename))");
             }
         } catch (SQLException e) {
             log.fatal("Error creating table METADATA: " + e.getMessage());
             e.printStackTrace();
         }
     
         try {
             if (!tableNames.contains("RESULTS")) {
                 log.info("Creating table RESULTS");
                 db.createStatement().executeUpdate(
 						"CREATE TABLE results ("
 								+ "userID INTEGER NOT NULL, "
 								+ "agentName VARCHAR (256), "
 								+ "agentType VARCHAR (256), "
                                                                 + "options VARCHAR (256), "
                                                                 + "dataFile VARCHAR (50), "
                                                                 + "testFile VARCHAR (50), "
                                                                 + "errorRate DOUBLE, "
                                                                 + "kappaStatistic DOUBLE, "
                                                                 + "meanAbsoluteError DOUBLE, "
                                                                 + "rootMeanSquaredError DOUBLE, "
                                                                 + "relativeAbsoluteError DOUBLE," 
 								+ "rootRelativeSquaredError DOUBLE, "
 								
 								+ "objectFilename VARCHAR(256), "
 								
 								+ "start TIMESTAMP, "
 								+ "finish TIMESTAMP, " 
 								+ "duration INTEGER, "
 								+ "durationLR DOUBLE, "
 								+ "experimentID VARCHAR (256), "
 								+ "experimentName VARCHAR (256), "
 								+ "note VARCHAR (256) "
 								+ ")");
             }
         } catch (SQLException e) {
             log.fatal("Error creating table RESULTS: " + e.getMessage());
         }
 
         try {
             if (!tableNames.contains("FILEMETADATA")) {
                 log.info("Creating view FILEMETADATA");
                 db.createStatement().executeUpdate(
                         "CREATE VIEW filemetadata AS " + "SELECT userid, filemapping.internalfilename, filemapping.externalfilename, " + "defaulttask, attributetype, numberofattributes, numberofinstances, missingvalues " + "FROM filemapping JOIN metadata " + "ON filemapping.internalfilename = metadata.internalfilename");
             }
         } catch (SQLException e) {
             log.fatal("Error creating table FILEMETADATA: " + e.getMessage());
         }
 
         try {
             if (!tableNames.contains("RESULTSEXTERNAL")) {
                 log.info("Creating view RESULTSEXTERNAL");
                 db.createStatement().executeUpdate("CREATE VIEW RESULTSEXTERNAL AS SELECT results.*,filemapping.externalFilename AS trainFileExt, filemapping2.externalFilename AS testFileExt FROM results JOIN filemapping ON results.userID = filemapping.userID AND results.dataFile = filemapping.internalFilename JOIN filemapping AS filemapping2 ON results.userID = filemapping2.userID AND results.testFile = filemapping2.internalFilename");
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         try {
             if (!triggerNames.contains("PREPAREMETADATA")) {
                 db.createStatement().execute(
                         "CREATE TRIGGER prepareMetadata AFTER INSERT ON filemapping " + "REFERENCING NEW ROW AS newrow FOR EACH ROW " + "INSERT INTO metadata (internalfilename, externalfilename) " + "VALUES (newrow.internalfilename, newrow.externalfilename)");
             }
         } catch (SQLException e) {
             log.fatal("Error creating trigger prepareMetadata: " + e.getMessage());
         }
 
         
         try {
 			db.close();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         
         MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchOntology(ontology.getName()), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
 
         addBehaviour(new AchieveREResponder(this, mt) {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             protected ACLMessage handleRequest(ACLMessage request)
                     throws NotUnderstoodException, RefuseException {
 
                 //log.info("Agent " + getLocalName() + " received request: " + request.getContent());
 
                 try {
                     Action a = (Action) getContentManager().extractContent(
                             request);
 
                     if (a.getAction() instanceof ImportFile) {
                         ImportFile im = (ImportFile) a.getAction();
 
                         String pathPrefix = System.getProperty("user.dir") + System.getProperty("file.separator") + "data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + "temp" + System.getProperty("file.separator");
                         
                         if (im.isTempFile()) {
 
                             FileWriter fw = new FileWriter(pathPrefix + im.getExternalFilename());
                             fw.write(im.getFileContent());
                             fw.close();
                             
                             ACLMessage reply = request.createReply();
                             reply.setPerformative(ACLMessage.INFORM);
 
                             Result r = new Result(im, pathPrefix + im.getExternalFilename());
                             getContentManager().fillContent(reply, r);
 
                             return reply;
 
                         }
 
 
                         if (im.getFileContent() == null) {
                             String path = System.getProperty("user.dir") + System.getProperty("file.separator");
                             path += "incoming" + System.getProperty("file.separator") + im.getExternalFilename();
 
                             String internalFilename = md5(path);
 
                             emptyMetadataToDB(internalFilename, im.getExternalFilename());
                             
                             File f = new File(path);
                             
                             openDBConnection();
                             Statement stmt = db.createStatement();
                             String query = "SELECT COUNT(*) AS num FROM filemapping WHERE internalFilename = \'" + internalFilename + "\'";
                             log.info("Executing query " + query);
 
                             ResultSet rs = stmt.executeQuery(query);
 
                             rs.next();
                             int count = rs.getInt("num");
 
                             stmt.close();
 
                             if (count > 0) {
                                 f.delete();
                                 log.info("File " + internalFilename + " already present in the database");
                             } else {
 
                                 stmt = db.createStatement();
                               
                                 query = "INSERT into filemapping (userId, externalFilename, internalFilename) VALUES (" + im.getUserID() + ",\'" + im.getExternalFilename() + "\',\'" + internalFilename + "\')";
                                 log.info("Executing query: " + query);
                                 
                                 stmt.executeUpdate(query);
                                 
                                 stmt.close();
 
                                 // move the file to db\files directory
                                 String newName = System.getProperty("user.dir") + System.getProperty("file.separator") + "data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + internalFilename;
                                 move(f, new File(newName));
                                 
                             }
                                                        
                             ACLMessage reply = request.createReply();
                             reply.setPerformative(ACLMessage.INFORM);
 
                             Result r = new Result(im, internalFilename);
                             getContentManager().fillContent(reply, r);
                             
                             db.close();
                             return reply;
                         } else {
 
                             String fileContent = im.getFileContent();
                             String fileName = im.getExternalFilename();
                             String internalFilename = DigestUtils.md5Hex(fileContent);
                             
                             emptyMetadataToDB(internalFilename, fileName);
 
                             openDBConnection();
                             Statement stmt = db.createStatement();
                             String query = "SELECT COUNT(*) AS num FROM filemapping WHERE internalFilename = \'" + internalFilename + "\'";
                             log.info("Executing query " + query);
 
                             ResultSet rs = stmt.executeQuery(query);
 
                             rs.next();
                             int count = rs.getInt("num");
 
                             stmt.close();
 
                             if (count > 0) {
                                 log.info("File " + internalFilename + " already present in the database");
                             } else {
 
                                 stmt = db.createStatement();
 
                                 log.info("Executing query: " + query);
                                 query = "INSERT into filemapping (userId, externalFilename, internalFilename) VALUES (" + im.getUserID() + ",\'" + im.getExternalFilename() + "\',\'" + internalFilename + "\')";
 
                                 stmt.executeUpdate(query);
                                 stmt.close();
 
                                 String newName = System.getProperty("user.dir") + System.getProperty("file.separator") + "data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + internalFilename;
 
                                 FileWriter file = new FileWriter(newName);
                                 file.write(fileContent);
                                 file.close();
 
                                 log.info("Created file: " + newName);
                             }
                             
                             ACLMessage reply = request.createReply();
                             reply.setPerformative(ACLMessage.INFORM);
 
                             Result r = new Result(im, internalFilename);
                             getContentManager().fillContent(reply, r);
                            
                             db.close();
                             return reply;
                         }
 
                     }
                     if (a.getAction() instanceof TranslateFilename) {
 
                         TranslateFilename tf = (TranslateFilename) a.getAction();
 
                         openDBConnection();
                         Statement stmt = db.createStatement();
 
                         String query = null;
                         if (tf.getInternalFilename() == null) {
                             query = "SELECT internalFilename AS filename FROM filemapping WHERE userID=" + tf.getUserID() + " AND externalFilename=\'" + tf.getExternalFilename() + "\'";
                         } else {
                             query = "SELECT externalFilename AS filename FROM filemapping WHERE userID=" + tf.getUserID() + " AND internalFilename=\'" + tf.getInternalFilename() + "\'";
                         }
 
                         log.info("Executing query: " + query);
 
                         ResultSet rs = stmt.executeQuery(query);
                        
                         
                         String internalFilename = "error";
 
                         if (rs.next()) { // should return single line (or none,
                             // if file does not exist)
                             internalFilename = rs.getString("filename");
 
                         }
                         else
                         {
                             String pathPrefix = System.getProperty("user.dir") + System.getProperty("file.separator") + "data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + "temp" + System.getProperty("file.separator");
                             
                             String tempFileName = pathPrefix + tf.getExternalFilename();
                             if (new File(tempFileName).exists())
                                 internalFilename = "temp" + System.getProperty("file.separator") + tf.getExternalFilename();
                         }
 
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
 
                         Result r = new Result(tf, internalFilename);
                         getContentManager().fillContent(reply, r);
                         
                         db.close();
                         return reply;
 
                     }
                     if (a.getAction() instanceof SaveResults) {
                         SaveResults sr = (SaveResults) a.getAction();
                         Task res = sr.getTask();
 
                         if (!(new File("studentMode").exists()) ) {
 
                         	openDBConnection();
                             Statement stmt = db.createStatement();
 
                             String query = "INSERT INTO results (userID, agentName, agentType, options, dataFile, testFile,"
                                     + "errorRate, kappaStatistic, meanAbsoluteError, rootMeanSquaredError, relativeAbsoluteError,"
                                     + "rootRelativeSquaredError, start, finish, duration, durationLR, objectFilename, experimentID, experimentName, note) VALUES ( 1, ";
                             query += "\'" + res.getAgent().getName() + "\',";
                             query += "\'" + res.getAgent().getType() + "\',";
                             query += "\'" + res.getAgent().optionsToString() + "\',";
                             query += "\'" + (res.getData().getTrain_file_name().split(Pattern.quote(System.getProperty("file.separator"))))[2] + "\',";
                             query += "\'" + (res.getData().getTest_file_name().split(Pattern.quote(System.getProperty("file.separator"))))[2] + "\',";
                                                         
             				float Error_rate = Float.MAX_VALUE;
             				float Kappa_statistic = Float.MAX_VALUE;
             				float Mean_absolute_error = Float.MAX_VALUE;
             				float Root_mean_squared_error = Float.MAX_VALUE;
             				float Relative_absolute_error = Float.MAX_VALUE; // percent
             				float Root_relative_squared_error = Float.MAX_VALUE; // percent
             				int duration = Integer.MAX_VALUE; // miliseconds
             				float durationLR = Float.MAX_VALUE;
 
                     		Iterator itr = res.getResult().getEvaluations().iterator();
                     		while (itr.hasNext()) {
     							Eval next_eval = (Eval) itr.next();
     							if (next_eval.getName().equals("error_rate")){ 
     								Error_rate = next_eval.getValue();
     							}
     							
     							if (next_eval.getName().equals("kappa_statistic")){ 
     								Kappa_statistic = next_eval.getValue();
     							}
 
     							if (next_eval.getName().equals("mean_absolute_error")){ 
     								Mean_absolute_error = next_eval.getValue();
     							}
     							
     							if (next_eval.getName().equals("root_mean_squared_error")){ 
     								Root_mean_squared_error = next_eval.getValue();
     							}
     							
     							if (next_eval.getName().equals("relative_absolute_error")){ 
     								Relative_absolute_error = next_eval.getValue();
     							}
     							
     							if (next_eval.getName().equals("root_relative_squared_error")){ 
     								Root_relative_squared_error = next_eval.getValue();
     							}
     							
     							if (next_eval.getName().equals("duration")){ 
     								duration = (int)next_eval.getValue();
     							}
     							if (next_eval.getName().equals("durationLR")){ 
     								durationLR = (float)next_eval.getValue();
     							}
                     		}
                     		
                             query += Error_rate + ",";
                             query += Kappa_statistic + ",";
                             query += Mean_absolute_error + ",";
                             query += Root_mean_squared_error + ",";
                             query += Relative_absolute_error + ",";
                             query += Root_relative_squared_error;                            
 							
                             
                     		Timestamp currentTimestamp =
                             new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
 
                             query += ",";
                             query += "\'" + java.sql.Timestamp.valueOf(res.getStart()) + "\',";
                             query += "\'" + java.sql.Timestamp.valueOf(res.getFinish()) + "\',";
                             
                             // query += "\'" + currentTimestamp + "\',";
                             query += "\'" + duration + "\',";
                             query += "\'" + durationLR + "\',";
                             
                             query += "\'" + res.getResult().getObject_filename() + "\', ";
                             query += "\'" + res.getId().getIdentificator() + "\',";  // TODO - pozor - neni jednoznacne, pouze pro jednoho managera
                             query += "\'" + res.getProblem_name() + "\',";
                             query += "\'" + res.getNote() + "\'";
                             query += ")";
 
                             log.info("Executing query: " + query);
 
                             stmt.executeUpdate(query);
                             db.close();
                         }
 
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
                         return reply;
                     }
                     if (a.getAction() instanceof SaveMetadata) {
 
                         SaveMetadata saveMetadata = (SaveMetadata) a.getAction();
                         Metadata metadata = saveMetadata.getMetadata();
 
                         openDBConnection();
                         Statement stmt = db.createStatement();
 
                         String query = "UPDATE metadata SET ";
                         query += "numberOfInstances=" + metadata.getNumber_of_instances() + ", ";
                         query += "numberOfAttributes=" + metadata.getNumber_of_attributes() + ", ";
                         query += "missingValues=" + metadata.getMissing_values();
                         if (metadata.getAttribute_type() != null) {
                             query += ", attributeType=\'" + metadata.getAttribute_type() + "\' ";
                         }
                         if (metadata.getDefault_task() != null) {
                             query += ", defaultTask=\'" + metadata.getDefault_task() + "\' ";
                         }
 
                         // the external file name contains part o the path
                         // (db/files/name) -> split and use only the [2] part
                         query += " WHERE internalFilename=\'" + metadata.getInternal_name().split(
                                 Pattern.quote(System.getProperty("file.separator")))[2] + "\'";
 
                         log.info("Executing query: " + query);
 
                         stmt.executeUpdate(query);
                         
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
                         
                         db.close();
                         return reply;
                     }
                     if (a.getAction() instanceof GetAllMetadata) {
                     	openDBConnection();
                     	Statement stmt = db.createStatement();
 
                         String query = "SELECT * FROM metadata";
 
                         List allMetadata = new ArrayList();
 
                         ResultSet rs = stmt.executeQuery(query);
 
                         Statement stmt1 = db.createStatement();
 
                         while (rs.next()) {
                             System.out.println("aaa");
                         	Metadata m = new Metadata();
                             m.setAttribute_type(rs.getString("attributeType"));
                             m.setDefault_task(rs.getString("defaultTask"));
                             m.setExternal_name(rs.getString("externalFilename"));
                             m.setInternal_name(rs.getString("internalFilename"));
                             m.setMissing_values(rs.getBoolean("missingValues"));
                             m.setNumber_of_attributes(rs.getInt("numberOfAttributes"));
                             m.setNumber_of_instances(rs.getInt("numberOfInstances"));
 
                             // get the number of task with this file as training
                             // set in the db
                             query = "SELECT COUNT(*) AS n FROM results WHERE dataFile=\'" + rs.getString("internalFilename") + "\'";
                             System.out.println(query);
                             ResultSet rs_number = stmt1.executeQuery(query);
                             rs_number.next();
 
                             m.setNumber_of_tasks_in_db(rs_number.getInt("n"));
                             allMetadata.add(m);
                         }
 
                         log.info("Executing query: " + query);
                        
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
 
                         Result _result = new Result(a.getAction(), allMetadata);
                         getContentManager().fillContent(reply, _result);
 
                         db.close();
                         return reply;
                     }
 
                     if (a.getAction() instanceof GetTheBestAgent) {
                         GetTheBestAgent g = (GetTheBestAgent) a.getAction();
                         String name = g.getNearest_file_name();
                         
                         openDBConnection();
                         Statement stmt = db.createStatement();
 
                         String query = "SELECT * FROM results " + "WHERE dataFile =\'" + name + "\'" + "AND errorRate = (SELECT MIN(errorRate) FROM results " + "WHERE dataFile =\'" + name + "\')";
 
                         ResultSet rs = stmt.executeQuery(query);
                         if (rs == null) {
                             ACLMessage reply = request.createReply();
                             reply.setPerformative(ACLMessage.FAILURE);
                             reply.setContent("There are no results for this file in the database.");
 
                             return reply;
                         }
                         rs.next();
 
                         pikater.ontology.messages.Agent agent = new pikater.ontology.messages.Agent();
                         agent.setName(rs.getString("agentName"));
                         agent.setType(rs.getString("agentType"));
                         System.out.println("**** options: " + rs.getString("options"));
                         agent.setOptions(agent.stringToOptions(rs.getString("options")));
 
                         log.info("Executing query: " + query);
 
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
 
                         Result _result = new Result(a.getAction(), agent);
                         getContentManager().fillContent(reply, _result);
 
                         db.close();
                         return reply;
                     }
                     if (a.getAction() instanceof GetFileInfo) {
 
                         GetFileInfo gfi = (GetFileInfo) a.getAction();
 
                         String query = "SELECT * FROM filemetadata WHERE " + gfi.toSQLCondition();
 
                         System.err.println(query);
 
                         openDBConnection();
                         Statement stmt = db.createStatement();
 
                         log.info("Executing query: " + query);
 
                         ResultSet rs = stmt.executeQuery(query);
 
                         List fileInfos = new ArrayList();
 
                         while (rs.next()) {
                             Metadata m = new Metadata();
                             m.setAttribute_type(rs.getString("attributeType"));
                             m.setDefault_task(rs.getString("defaultTask"));
                             m.setExternal_name(rs.getString("externalFilename"));
                             m.setInternal_name(rs.getString("internalFilename"));
                             m.setMissing_values(rs.getBoolean("missingValues"));
                             m.setNumber_of_attributes(rs.getInt("numberOfAttributes"));
                             m.setNumber_of_instances(rs.getInt("numberOfInstances"));
                             fileInfos.add(m);
                         }
 
                         Result r = new Result(a.getAction(), fileInfos);
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
 
                         getContentManager().fillContent(reply, r);
 
                         System.err.println("Sending reply");
                         
                         db.close();
                         return reply;
                     }
 
                     if (a.getAction() instanceof UpdateMetadata) {
 
                         UpdateMetadata updateMetadata = (UpdateMetadata) a.getAction();
                         Metadata metadata = updateMetadata.getMetadata();
 
                         openDBConnection();
                         Statement stmt = db.createStatement();
 
                         String query = "UPDATE metadata SET ";
                         query += "numberOfInstances=" + metadata.getNumber_of_instances() + ", ";
                         query += "numberOfAttributes=" + metadata.getNumber_of_attributes() + ", ";
                         query += "missingValues=" + metadata.getMissing_values() + "";
                         if (metadata.getAttribute_type() != null) {
                             query += ", attributeType=\'" + metadata.getAttribute_type() + "\' ";
                         }
                         if (metadata.getDefault_task() != null) {
                             query += ", defaultTask=\'" + metadata.getDefault_task() + "\' ";
                         }
                         query += " WHERE internalFilename =\'" + metadata.getInternal_name() + "\'";
 
                         log.info("Executing query: " + query);
 
                         stmt.executeUpdate(query);
 
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
                         
                         db.close();
                         return reply;
                     }
                     if (a.getAction() instanceof GetFiles) {
 
                         GetFiles gf = (GetFiles) a.getAction();
 
                         String query = "SELECT * FROM filemapping WHERE userid = " + gf.getUserID();
 
                         log.info("Executing query: " + query);
                         
                         openDBConnection();
                         Statement stmt = db.createStatement();
                         ResultSet rs = stmt.executeQuery(query);
 
                         ArrayList files = new ArrayList();
 
                         while (rs.next()) {
                             files.add(rs.getString("externalFilename"));
                         }
 
                         Result r = new Result(a.getAction(), files);
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
 
                         getContentManager().fillContent(reply, r);
                         
                         db.close();
                         return reply;
                     }
 
                     if (a.getAction() instanceof LoadResults) {
 
                         LoadResults lr = (LoadResults) a.getAction();
 
                         System.err.println(lr.asText());
 
                         String query = "SELECT * FROM resultsExternal " + lr.asSQLCondition();
 
                         System.err.println(query);
                         log.info(query);
 
                         openDBConnection();
                         Statement stmt = db.createStatement();
                         ResultSet rs = stmt.executeQuery(query);
 
                         ArrayList results = new ArrayList();
 
                         //errorRate, kappaStatistic, meanAbsoluteError,
                         //rootMeanSquaredError, relativeAbsoluteError,
                         //rootRelativeSquaredError
 
                         while (rs.next()) {
                             
                             SavedResult sr = new SavedResult();
 
                             sr.setAgentType(rs.getString("agentType"));
                             sr.setAgentOptions(rs.getString("options"));
                             sr.setTrainFile(rs.getString("trainFileExt"));
                             sr.setTestFile(rs.getString("testFileExt"));
                             sr.setErrorRate(rs.getDouble("errorRate"));
                             sr.setKappaStatistic(rs.getDouble("kappaStatistic"));
                             sr.setMeanAbsError(rs.getDouble("meanAbsoluteError"));
                             sr.setRMSE(rs.getDouble("rootMeanSquaredError"));
                             sr.setRootRelativeSquaredError(rs.getDouble("rootRelativeSquaredError"));
                             sr.setRelativeAbsoluteError(rs.getDouble("relativeAbsoluteError"));
                             sr.setDate("nodate");
 
                             results.add(sr);
                         }
 
                         Result r = new Result(a.getAction(), results);
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
 
                         getContentManager().fillContent(reply, r);
 
                         db.close();
                         return reply;
                     }
 
                     if (a.getAction() instanceof DeleteTempFiles) {
 
                         String path = "data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + "temp" + System.getProperty("file.separator");
 
                         File tempDir = new File(path);
                         String[] files = tempDir.list();
 
                         if (files != null) {
                             for (String file : files) {
                                 File d = new File(path + file);
                                 d.delete();
                             }
                         }
 
                         ACLMessage reply = request.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
 
                         return reply;
 
                     }
 
                 } catch (OntologyException e) {
                     e.printStackTrace();
                     log.error("Problem extracting content: " + e.getMessage());
                 } catch (CodecException e) {
                     e.printStackTrace();
                     log.error("Codec problem: " + e.getMessage());
                 } catch (SQLException e) {
                     e.printStackTrace();
                     log.error("SQL error: " + e.getMessage());
                 } catch (FileNotFoundException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
 
                 ACLMessage failure = request.createReply();
                 failure.setPerformative(ACLMessage.FAILURE);
                 log.error("Failure responding to request: " + request.getContent());
                 return failure;
             }
         });
 
     }
 
     private String md5(String path) {
 
         StringBuffer sb = null;
 
         try {
             FileInputStream fs = new FileInputStream(path);
             sb = new StringBuffer();
 
             int ch;
             while ((ch = fs.read()) != -1) {
                 sb.append((char) ch);
             }
             fs.close();
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             log.error("File not found: " + path + " -- " + e.getMessage());
         } catch (IOException e) {
             e.printStackTrace();
             log.error("Error reading file: " + path + " -- " + e.getMessage());
         }
 
         String md5 = DigestUtils.md5Hex(sb.toString());
 
         log.info("MD5 hash of file " + path + " is " + md5);
 
         return md5;
     }
 
 	private void loadMetadataFromFile(String fileName) throws IOException, SQLException{		
 		String query = "";
 		
 		BufferedReader bufRdr  = new BufferedReader(new FileReader(fileName));
 
 		// read first line
 		String line = bufRdr.readLine();
 		String captions[] = line.split(";");
 				
 		while((line = bufRdr.readLine()) != null){
 			String values[] = line.split(";");
 			 query += "UPDATE metadata SET ";
 			 for (int i=0; i<captions.length-2; i++) {
 				 query += captions[i]+"='"+values[i]+"', ";
 			 }
 			 query += captions[captions.length-2]+"=\'"+values[captions.length-2]+"\' ";
 			 query += "WHERE externalFilename=\'"+values[captions.length-1]+"\'; ";			 
 		}
 		openDBConnection();		
 		Statement stmt = db.createStatement();
 		log.info("Executing query: " + query);
 		stmt.executeUpdate(query);
 		stmt.close();
 		db.close();
 	}
     
     // Move file (src) to File/directory dest.
     public static synchronized void move(File src, File dest)
             throws FileNotFoundException, IOException {
         copy(src, dest);
         src.delete();
     }
 
     // Copy file (src) to File/directory dest.
     public static synchronized void copy(File src, File dest)
             throws IOException {
         InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dest);
 
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }
     
     private void emptyMetadataToDB(String internalFilename, String externalFilename) throws SQLException{ 
     	openDBConnection();
 	    Statement stmt = db.createStatement();
 	    
 	    String query  = "SELECT COUNT(*) AS number FROM metadata WHERE internalFilename = \'" + internalFilename + "\'";
 	    String query1 = "SELECT COUNT(*) AS number FROM filemapping WHERE internalFilename = \'" + internalFilename + "\'";
 	    
 	    log.info("Executing query " + query);
 	    log.info("Executing query " + query1);
 	    
 	    ResultSet rs = stmt.executeQuery(query);
 	    rs.next();
 	    int isInMetadata = rs.getInt("number");
	    
	    ResultSet rs1 = stmt.executeQuery(query1);
		rs1.next();	    
 	    int isInFileMapping = rs1.getInt("number");
 	
 	    if (isInMetadata == 0 && isInFileMapping == 1) {
 	        log.info("Executing query: " + query);
 	        query = "INSERT into metadata (externalFilename, internalFilename, defaultTask, " +
 	        		"attributeType, numberOfInstances, numberOfAttributes, missingValues)" +
 	        		"VALUES (\'" + externalFilename + "\',\'" + internalFilename + "\', null, " +
 	        				"null, 0, 0, false)";
 	        stmt.executeUpdate(query);       
 	    }	        
        // stmt.close();
         db.close();
 	}
     
 }
