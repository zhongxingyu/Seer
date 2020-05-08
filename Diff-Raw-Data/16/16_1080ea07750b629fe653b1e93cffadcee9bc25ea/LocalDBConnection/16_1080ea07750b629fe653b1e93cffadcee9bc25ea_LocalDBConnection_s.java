 package org.publicmain.sql;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.PreparedStatement;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.common.Config;
 import org.publicmain.common.ConfigData;
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.MSGCode;
 import org.publicmain.common.NachrichtenTyp;
 import org.publicmain.common.Node;
 import org.publicmain.nodeengine.NodeEngine;
 import org.resources.Help;
 
 /**
  * Die Klasse DBConnection stellt die Verbindung zu dem Lokalen DB-Server her.
  * Sie legt weiterhin alle zwingend notwendigen Datenbanken(1) und Tabellen an.
  */
 public class LocalDBConnection {
 
 	private Connection con;
 	private Statement stmt;
 	private PreparedStatement checkoutHistoryPrpStmt;
 	//private ResultSet rs;
 	private String url;
 	private String dbName;
 	private String user;
 	private String passwd;
 	
 	//Tabellen der Loc DB
 	private String usrTbl;
 	private String nodeTbl;
 	private String msgTbl;
 	private Calendar cal;
 	private SimpleDateFormat splDateFormt;
 	private Set<Node> allnodes = Collections.synchronizedSet(new HashSet<Node>());
 	
 	
 	//--------neue Sachen------------
 	private BlockingQueue<MSG> locDBInbox;
 	private int dbStatus;					// 0=nicht bereit	1=con besteht	2=DB und Tabellen angelegt 3=mit db verbunden
 	private int maxVersuche;
 	private long warteZeitInSec;
 	private Thread connectToDBThread;
 	private final static int LOCAL_DATABASE_VERSION = 4;
 	private DatabaseEngine databaseEngine;
 	private boolean ceReadyForWritingSettings;
 	//--------neue Sachen ende------------
 	
 	// verbindungssachen
 	private static LocalDBConnection me;
 	
 	private void addnodez(Collection<Node> node){
 		synchronized (allnodes) {
 			allnodes.removeAll(node);
 			allnodes.addAll(node);
 		}
 	}
 	
  	private LocalDBConnection(DatabaseEngine databaseEngine) {
 		this.url 					= "jdbc:mysql://localhost:"+Config.getConfig().getLocalDBPort()+"/";
 		this.user 					= Config.getConfig().getLocalDBUser();
 		this.passwd 				= Config.getConfig().getLocalDBPw();
 		this.dbName 				= Config.getConfig().getLocalDBDatabasename();
 		this.usrTbl					= "t_user";
 		this.nodeTbl				= "t_nodes";
 		this.msgTbl					= "t_messages";
 		this.cal					= Calendar.getInstance();
 		this.splDateFormt			= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 		this.databaseEngine			= databaseEngine;
 
 		//--------neue Sachen------------
 		this.locDBInbox = new LinkedBlockingQueue<MSG>();;
 		this.dbStatus	= 0;				// 0=nicht bereit	1=con besteht	2=Tables wurden angelegt 3=use db_publicmain erfolgreich / hardWorkingThread luft
 		this.maxVersuche = 5;
 		this.warteZeitInSec = 10;
 //		this.writtenStandardUser = false;
 		this.connectToDBThread = new Thread(new firstConnectToLocDBServerBot());
 		this.ceReadyForWritingSettings = false;
 		//--------neue Sachen ende------------
 		connectToDBThread.start();
 	}
 	public static LocalDBConnection getDBConnection(DatabaseEngine databaseEngine) {
 		if (me == null) {
 			me = new LocalDBConnection(databaseEngine);
 		}
 		return me;
 	}
 
 	private class firstConnectToLocDBServerBot implements Runnable { // wird nur vom Construktor aufgerufen
 
 		public void run() {
 			int versuche = 0;
 			while ((versuche < maxVersuche) && (dbStatus == 0)) { // hatte da erst con==null drin aber das ist ein problem beim reconnecten unten.
 				if(connectToLocDBServer()){
 					versuche = 0;
 				} else {
 					versuche ++;
 				}
 			}
 			if(dbStatus >= 1){
 				try {
 					synchronized (stmt) {
 						stmt.executeQuery("use " + dbName);
 					}
 					if(Config.getConfig().getLocalDBVersion() != LOCAL_DATABASE_VERSION){
 						createDbAndTables();
 						run();
 					} else {
 						dbStatus = 3;
 						databaseEngine.go();
 						LogEngine.log(this, "DB-Status: " + dbStatus, LogEngine.INFO);
 					}
 				} catch (SQLException e1) {
 					createDbAndTables();
 					run();
 				}
 			} else {
 				LogEngine.log(this,	versuche +". Versuch zum DB-Verbindungsaufbau fehlgeschlagen -> fr Zugriff auf locDB sorgen und neu starten! ", LogEngine.ERROR);
 			}
 		}
 	}
 	
 	private synchronized boolean connectToLocDBServer(){
 		try {
 			con = DriverManager.getConnection(url, user, passwd);
 			stmt = con.createStatement();
 			LogEngine.log(this, "DB-ServerVerbindung hergestellt", LogEngine.INFO);
 			dbStatus = 1;
 			return true;
 		} catch (SQLException e) {
 			try {
 				Thread.sleep(warteZeitInSec * 1000);
 			} catch (InterruptedException e1) {
 				LogEngine.log(this,"Fehler beim Warten: " + e1.getMessage(),LogEngine.ERROR);
 			}
 			dbStatus = 0;
 		}
 		
 		return false;
 	}
 	
 	public boolean getStatus(){
 		if (dbStatus==3){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void reconnectToLocDBServer(){
 		new Thread(new Runnable() {
 			public void run() {
 				int reconnectVersuche = 1;
 				while (dbStatus < 3 && maxVersuche >= reconnectVersuche){
 					LogEngine.log(this, "Lost connetion to locDBSvr. Try to reconnect in 10 seconds " + reconnectVersuche + "/" + maxVersuche +".", LogEngine.ERROR);
 					try {
 						Thread.sleep(warteZeitInSec * 1000);
 					} catch (InterruptedException e1) {
 					}
 					if (connectToLocDBServer()){
 						try {
 							synchronized (stmt) {
 								stmt.executeQuery("use " + dbName);
 							}
 							dbStatus = 3;
 							databaseEngine.go();
 							reconnectVersuche = 0;
 						} catch (SQLException e) {
 						}
 					}
 					reconnectVersuche++;
 					if (reconnectVersuche == maxVersuche) LogEngine.log(this, "Lost connection to locDBSvr conclusively - will not try again.",LogEngine.ERROR);
 				}
 			}
 		}).start();
 		
 	}
 	
 	private void createDbAndTables (){	// wird nur vom Construktor aufgerufen
 		String read=null;
 		try (BufferedReader in = new BufferedReader(new FileReader(Help.getFile("create_LocalDB.sql")))){
 			while((read = in.readLine()) != null) {
 				while (!read.endsWith(";") && !read.endsWith("--")){
 					read = read + in.readLine();
 				}
 				synchronized (stmt) {
 					stmt.execute(read);
 				}
 			}
 			//INSERT PROCEDURES
 			synchronized (stmt) {
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_messages_savePrivateMessage`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_savePrivateMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newTxt VARCHAR(200), IN newFk_t_users_userID_empfaenger BIGINT(20)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender, txt,fk_t_users_userID_empfaenger) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newTxt, newFk_t_users_userID_empfaenger); END;");
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_messages_saveGroupMessage`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveGroupMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newTxt VARCHAR(200), IN newFk_t_groups_groupName VARCHAR(20)) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp, fk_t_users_userID_sender, txt, fk_t_groups_groupName) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newTxt, newFk_t_groups_groupName); END;");
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_messages_saveSystemMessage`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_messages_saveSystemMessage` (IN newMsgID INT(11), IN newTimestmp BIGINT(20), IN newFk_t_users_userID_sender BIGINT(20), IN newFk_t_users_userID_empfaenger BIGINT(20),IN newFk_t_msgType_ID INT) BEGIN INSERT IGNORE INTO t_messages (msgID,timestmp,fk_t_users_userID_sender, fk_t_users_userID_empfaenger,fk_t_msgType_ID) VALUES (newMsgID, newTimestmp, newFk_t_users_userID_sender, newFk_t_users_userID_empfaenger, newFk_t_msgType_ID); END;");
 				
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_groups_saveGroups`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_groups_saveGroups` (IN newGroupName VARCHAR(20),IN newFk_t_users_userID BIGINT(20)) BEGIN insert into t_groups (groupName, fk_t_users_userID) values (newGroupName,newFk_t_users_userID) ON DUPLICATE KEY UPDATE fk_t_users_userID=VALUES(fk_t_users_userID); END;");
 				
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_user_saveUsers`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_user_saveUsers` (IN newUserID BIGINT(20),IN newDisplayName VARCHAR(45),IN newUserName VARCHAR(45)) BEGIN insert into t_users (userID, displayName, userName) values (newUserID,newDisplayName,newUserName) ON DUPLICATE KEY UPDATE displayName=VALUES(displayName),userName=VALUES(userName); END;");
 				
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_msgType`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_msgType`(IN newMsgTypeID INT,IN newName VARCHAR(45),IN newDescription VARCHAR(45)) BEGIN INSERT INTO t_msgType (msgTypeID, name, description) VALUES (newMsgTypeID,newName,newDescription) ON DUPLICATE KEY UPDATE name=VALUES(name),description=VALUES(description); END;");
 				
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_nodes_saveNodes`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_nodes_saveNodes` (IN newNodeID BIGINT(20), IN newComputerName VARCHAR(45), IN newFk_t_users_userID_2 BIGINT(20), IN newFk_t_nodes_nodeID BIGINT(20)) BEGIN INSERT INTO t_nodes (nodeID, computerName, fk_t_users_userID_2, fk_t_nodes_nodeID) VALUES (newNodeID, newComputerName, newFk_t_users_userID_2, newFk_t_nodes_nodeID) ON DUPLICATE KEY UPDATE computerName=VALUES(computerName), fk_t_users_userID_2=VALUES(fk_t_users_userID_2), fk_t_nodes_nodeID=VALUES(fk_t_nodes_nodeID); END;");
 				
 				stmt.addBatch("DROP PROCEDURE IF EXISTS `db_publicmain`.`p_t_settings_saveSettings`;");
 				stmt.addBatch("CREATE PROCEDURE `db_publicmain`.`p_t_settings_saveSettings` (IN newSettingsKey VARCHAR(45),IN newFk_t_users_userID_3 BIGINT(20), IN newSettingsValue VARCHAR(100)) BEGIN INSERT INTO t_settings (settingsKey, fk_t_users_userID_3, settingsValue) VALUES (newsettingsKey, newFk_t_users_userID_3, newSettingsValue) ON DUPLICATE KEY UPDATE fk_t_users_userID_3=VALUES(fk_t_users_userID_3), settingsValue=VALUES(settingsValue); END;");
 				//TRIGGER
 				stmt.addBatch("DROP TRIGGER IF EXISTS `db_publicmain`.`tr_t_messages`;");
 				stmt.addBatch("CREATE TRIGGER `db_publicmain`.`tr_t_messages` BEFORE INSERT ON t_messages FOR EACH ROW SET new.displayName = (SELECT displayName FROM t_users WHERE userID = new.fk_t_users_userID_sender);");
 				
 				//Hier wird die Tabelle t_msgType automatisch befllt!
 				for (MSGCode c : MSGCode.values()){
 					stmt.addBatch("CALL p_t_msgType(" + c.ordinal() + ",'" + c.name() + "','" +  c.getDescription() + "')");
 				}
 				stmt.executeBatch();
 				dbStatus = 2;
 				Config.getConfig().setLocalDBVersion(LOCAL_DATABASE_VERSION);
 				Config.write();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 		
 	public ResultSet pull_msgs(){
 		if (dbStatus >= 3){
 			synchronized (stmt) {
 				try {
 					return stmt.executeQuery("SELECT * FROM 'v_pullAll_t_messages';");
 				} catch (SQLException e) {
 					//TODO: evtl. fehlermeldung + in DatabaseEngine fr reconnect sorgen!
 					dbStatus = 0;
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public ResultSet pull_users() {
 		if (dbStatus >= 3) {
 			synchronized (stmt) {
 				try {
					return stmt.executeQuery("SELECT * FROM 'v_pullAll_t_users';");
 				} catch (SQLException e) {
 					// TODO: evtl. fehlermeldung + in DatabaseEngine fr
 					// reconnect sorgen!
 					dbStatus = 0;
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 
 	public ResultSet pull_settings() {
 		if (dbStatus >= 3) {
 			synchronized (stmt) {
 				try {
 					return stmt.executeQuery("SELECT * FROM 'v_pullALL_t_settings';");
 				} catch (SQLException e) {
 					// TODO: evtl. fehlermeldung + in DatabaseEngine fr
 					// reconnect sorgen!
 					dbStatus = 0;
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public boolean push_msgs(ResultSet backup){
 		return false;
 	}
 	
 	public boolean push_users(ResultSet backup){
 		return false;
 	}
 	
 	public synchronized boolean writeAllUsersToDB(Collection<Node> allNodesFormDBE){
 		addnodez(allNodesFormDBE);
 		if (dbStatus >= 3){
 			if (allNodesFormDBE != null){
 				StringBuffer saveUserStmt; 
 				Iterator<Node> it = allNodesFormDBE.iterator();
 				boolean alldone=true;
 				while (it.hasNext()){
 					Node tmpNode = (Node) it.next();
 					saveUserStmt = new StringBuffer();
 					saveUserStmt.append("CALL p_t_user_saveUsers(");
 					saveUserStmt.append(tmpNode.getUserID() + ",");
 					saveUserStmt.append("'" + tmpNode.getAlias() + "',");
 					saveUserStmt.append("'" + tmpNode.getUsername() + "')");
 					try {
 						synchronized (stmt) {
 							stmt.execute(saveUserStmt.toString());
 							if (!ceReadyForWritingSettings){
 								ceReadyForWritingSettings = true;
 								Config.write();				
 							}
 						}
 					} catch (SQLException e) {
 						alldone=false;
 						dbStatus = 0;
 						LogEngine.log(LocalDBConnection.this,"Fehler beim eintragen in : "+ usrTbl + " or " + nodeTbl	 + " "+ e.getMessage(),LogEngine.ERROR);
 						//hier falls whrend der schreibvorgnge die verbind verloren geht.
 					}
 				}
 				return alldone;
 			}
 			return true;
 		}
 		return false;
 	}
 	//Settings werden ins beziehungsweise aus dem ConfigObjekt geladen nicht hier	
 	
 	public synchronized boolean writeAllGroupsToDB(Collection<String> groupsSet){
 		if (dbStatus >= 3) {
 			if (groupsSet != null) {
 				StringBuffer saveGroupsStmt;
 				Iterator<String> it = groupsSet.iterator();
 				while (it.hasNext()) {
 					String tmpGrpStr = it.next();
 
 					saveGroupsStmt = new StringBuffer();
 					saveGroupsStmt.append("CALL p_t_groups_saveGroups(");
 					saveGroupsStmt.append("'" + tmpGrpStr + "',");
 					saveGroupsStmt.append(ChatEngine.getCE().getUserID() + ")");
 					try {
 
 						synchronized (stmt) {
 							stmt.execute(saveGroupsStmt.toString());
 						}
 					} catch (SQLException e) {
 						LogEngine.log(this, "dumping groups failed: " + e.getMessage(),LogEngine.ERROR);
 						dbStatus = 0;
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	
 	public synchronized boolean writeMsgToDB(MSG m) {
 		if (dbStatus >= 3) {
 			long uid_empfnger = -1;
 			if (m.getEmpfnger() != -1){
 				
 				uid_empfnger = getUIDforNID(m.getEmpfnger());
 			}
 			long uid_sender = getUIDforNID(m.getSender());
 			Object data = m.getData();
 			MSGCode code = m.getCode();
 			long timestamp = m.getTimestamp();
 			int id = m.getId();
 			String group = m.getGroup();
 			NachrichtenTyp typ = m.getTyp();
 			return writeMSG(uid_empfnger, uid_sender, data, code, timestamp, id, group, typ);
 		}
 		return false;
 	}
 	
 	private Node getNode(long nid){
 		synchronized (allnodes) {
 			for (Node node : allnodes) {
 				if (node.getNodeID()==nid) return node;
 			}
 		}
 		return null;
 	}
 	
 	private long getUIDforNID(long nid) {
 		return getNode(nid).getUserID();
 	}
 	
 
 	private synchronized boolean writeMSG(long uid_empfnger, long uid_sender, Object data, MSGCode code, long timestamp, int id, String group, NachrichtenTyp typ) {
 		StringBuffer saveMsgStmt = new StringBuffer();
 		if (typ == NachrichtenTyp.GROUP) {
 			// fgt Nachicht hinzu
 			saveMsgStmt.append("CALL p_t_messages_saveGroupMessage(");
 			saveMsgStmt.append(id + ",");
 			saveMsgStmt.append(timestamp + ",");
 			saveMsgStmt.append(uid_sender + ",");
 			saveMsgStmt.append("'" + data + "',");
 			saveMsgStmt.append("'" + group + "')");
 		}
 		else if (typ == NachrichtenTyp.PRIVATE) {
 			// fgt Nachicht hinzu
 			saveMsgStmt.append("CALL p_t_messages_savePrivateMessage(");
 			saveMsgStmt.append(id + ",");
 			saveMsgStmt.append(timestamp + ",");
 			saveMsgStmt.append(uid_sender + ",");
 			saveMsgStmt.append("'" + data + "',");
 			saveMsgStmt.append(uid_empfnger + ")");
 		} else if (typ == NachrichtenTyp.SYSTEM) {
 			String toWriteUID_empfnger = String.valueOf(uid_empfnger);
 			if (uid_empfnger == -1) {
 				toWriteUID_empfnger = "null";
 			}
 			// fgt Nachicht hinzu
 			saveMsgStmt.append("CALL p_t_messages_saveSystemMessage(");
 			saveMsgStmt.append(id + ",");
 			saveMsgStmt.append(timestamp + ",");
 			saveMsgStmt.append(uid_sender + ",");
 			saveMsgStmt.append(toWriteUID_empfnger + ",");
 			saveMsgStmt.append(code.ordinal() + ")");
 		} else
 			return true;
 
 		if (saveMsgStmt.length() > 0) { // && saveGrpStmt.length() > 0){
 			synchronized (stmt) {
 				try {
 					stmt.execute(saveMsgStmt.toString());
 					return true;
 				} catch (Exception e) {
 					dbStatus = 0;
 					LogEngine.log(this, "Fehler beim schreiben von: " + saveMsgStmt.toString() + " " + e.getMessage(), LogEngine.ERROR);
 					return false;
 					// hier falls whrend der schreibvorgnge die verbind
 					// verloren geht.
 				}
 			}
 		}
 		return true;
 	}
 	
 	public synchronized boolean writeRoutingTableToDB(Long nIDZiel, String hostNameZiel, Long uIDZiel, Long nIDGateWay) {
 		StringBuffer saveNodeStmt = new StringBuffer();
 		if (dbStatus >= 3){
 			saveNodeStmt.append("p_t_nodes_saveNodes(");
 			saveNodeStmt.append(nIDZiel + ",");
 			saveNodeStmt.append("'" + hostNameZiel + "',");
 			saveNodeStmt.append(uIDZiel+ "',");
 			saveNodeStmt.append(nIDGateWay+ "')");
 		}
 		try {
 			synchronized (stmt) {
 				stmt.execute(saveNodeStmt.toString());
 			}
 			return true;
 		} catch (SQLException e) {
 			LogEngine.log(LocalDBConnection.this,"Fehler beim eintragen in: t_nodes "+ e.getMessage(),LogEngine.ERROR);
 			dbStatus = 0;
 			return false;
 		}
 	}
 
 	public synchronized void writeAllSettingsToDB(final ConfigData settings) {
 		if (ceReadyForWritingSettings){
 			new Thread(new Runnable() {
 				public void run() {
 					if (dbStatus >= 3) {
 						synchronized (stmt) {
 							for (String key : settings.stringPropertyNames()) {
 								StringBuffer saveSettingsStmt = new StringBuffer();
 								if (!settings.getProperty(key).equals("")){
 									saveSettingsStmt.append("CALL p_t_settings_saveSettings(");
 									saveSettingsStmt.append("'" + key + "',");
 									saveSettingsStmt.append(ChatEngine.getCE().getUserID() + ",");
 									saveSettingsStmt.append("'"+ settings.getProperty(key) + "');");
 									try {
 										stmt.addBatch(saveSettingsStmt.toString());
 									} catch (SQLException e) {
 										LogEngine.log(this, "Error by 'addBatch: " + e.getMessage(), LogEngine.ERROR);
 									}
 								}
 							}
 							try {
 								stmt.executeBatch();
 							} catch (SQLException e) {
 								LogEngine.log(LocalDBConnection.this,"Communication with LocDB failed while 'writeAllSettingsToDB': " + e.getMessage(), LogEngine.ERROR);
 								dbStatus = 0;
 							}
 						}
 					}
 				}
 			}).start();
 		}
 	}
 	
 	public boolean deleteAllMsgs () {
 		try {
 			synchronized (stmt) {
 				return stmt.execute("DELETE * FROM " + msgTbl);
 			}
 		} catch (SQLException e) {
 			dbStatus = 0;
 			return false;
 		}
 	}
 	
 	public ResultSet searchInHistory (String userID, String alias, String groupName, long begin, long end, String msgTxt){
 		if (dbStatus >= 3){
 			try {
				PreparedStatement searchInHistStmt = con.prepareStatement("SELECT * from t_messages WHERE (fk_t_users_userID_sender LIKE '?' OR fk_t_users_userID_empfaenger LIKE '?') AND displayName LIKE '?' AND fk_t_groups_groupName LIKE '?' AND (timestmp BETWEEN '?' AND '?') AND txt LIKE '?' ");
 				searchInHistStmt.setString(1, userID);
 				searchInHistStmt.setString(2, userID);
 				searchInHistStmt.setString(3, alias);
 				searchInHistStmt.setString(4, groupName);
 				searchInHistStmt.setLong(5, begin);
 				searchInHistStmt.setLong(6, end);
 				searchInHistStmt.setString(7, msgTxt);
 				
 				return searchInHistStmt.executeQuery();
 				
 			} catch (SQLException e) {
 				LogEngine.log(this, "Error while executing 'searchInHistStmt' PreparedStatment: " + e.getMessage(), LogEngine.ERROR);
 				return null;
 			}
 		}
 		return null;
 		
 //		if (isDBConnected){
 //			try {
 //				String tmpStmtStr = (
 //						"SELECT * " +
 //						"FROM " + chatLogTbl + " " +
 //						"WHERE timestamp BETWEEN '" + fromDateTime.getTime() + "' AND '" + toDateTime.getTime() + "'");
 //				if (!chosenNTyp.equals("ALL")){
 //					tmpStmtStr = tmpStmtStr + " AND typ = '" + chosenNTyp + "'";
 //				}
 //				if (!chosenAliasOrGrpName.equals("...type in here.")){
 //					//TODO: hier die bersetung von nr in string einbinden
 ////					tmpStmtStr = tmpStmtStr + " AND ";
 //				}
 //				Statement searchStmt = con.createStatement();
 //				ResultSet rs = searchStmt.executeQuery(tmpStmtStr);
 //				
 //				
 //				
 //				//TODO bei mehrfachen aufruf muss das HTML-Doc wieder geleert werden!
 //				historyContentTxt.setText("");
 //				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<table>" + "<th><b>ID</b></th>"
 //						+ "<th><b>msgID</th>" + "<th><b>Timestamp</th>"
 //						+ "<th><b>Sender</th>" + "<th><b>Empfnger</th>" + "<th><b>Typ</th>"
 //						+ "<th><b>Gruppe</th>" + "<th><b>Daten</th>", 0, 0, null);
 //				while (rs.next()) {
 //					
 //					cal.setTimeInMillis(rs.getLong("timestamp"));
 //					htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),
 //							"<tr>" + "<td>" + rs.getInt("id") + "<td>"
 //									+ rs.getInt("msgID") + "<td>"
 //									+ splDateFormt.format(cal.getTime())+ "<td>"
 //									+ rs.getLong("sender") + "<td>"
 //									+ rs.getLong("empfaenger") + "<td>"
 //									+ rs.getString("typ") + "<td>"
 //									+ rs.getString("grp") + "<td>"
 //									+ rs.getString("data"), 0, 0, null);
 //					htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "</tr>", 0, 0,
 //							null);
 //				}
 //				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "</table>", 0, 0,
 //						null);
 //			} catch (BadLocationException e) {
 //				e.printStackTrace();
 //			} catch (IOException e) {
 //				e.printStackTrace();
 //			} catch (SQLException e) {
 //				e.printStackTrace();
 //			} 
 //		}else {
 //			JOptionPane.showMessageDialog(null,
 //	                "Fr diese Operation ist eine Verbindung zum lokalen DB-Server zwingend notwendig",
 //	                "Hinweis",
 //	                JOptionPane.INFORMATION_MESSAGE);
 //			askForConnectionRetry = true;
 //			if (connectToLocDBServer()) {
 //				isDBConnected = true;
 //				searchInHistory(historyContentTxt, chosenNTyp, chosenAliasOrGrpName, fromDateTime, toDateTime,  htmlKit, htmlDoc);
 //				
 //			} else {
 //				//System.out.println("Erneuter versuch der Verbindungsherstellung erfolglos!");
 //			}
 //		}
 	}
 	
 	public void shutdownLocDB() {
 		// TODO alle verbindungen trennen
 		try {
 			if(stmt!=null){
 				stmt.close();
				LogEngine.log(LocalDBConnection.this,"stmt des LocDBServers closed",LogEngine.INFO);
 			}
 		} catch (SQLException e) {
 			LogEngine.log(this, e);
 		}
 		try {
 			if(con!=null){
 				con.close();
 				LogEngine.log(LocalDBConnection.this,"Connection to LocDBServer closed",LogEngine.INFO);
 			}
 			
 			// TODO What else?
 		} catch (SQLException e) {
 			LogEngine.log(this, e);
 		}
 	}
 
 	
 }
