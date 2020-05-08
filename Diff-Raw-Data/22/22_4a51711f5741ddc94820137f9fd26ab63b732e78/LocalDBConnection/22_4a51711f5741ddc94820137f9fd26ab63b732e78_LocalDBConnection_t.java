 package org.publicmain.sql;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.print.attribute.standard.MediaSize.Other;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextPane;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.NachrichtenTyp;
 import org.publicmain.gui.GUI;
 
 import com.mysql.jdbc.PreparedStatement;
 
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
 	private String chatLogTbl;
 	private String msgTbl;
 	private String usrTbl;
 	private String nodeTbl;
 	private String groupTbl;
 	private String configTbl;
 	private String eventTypeTbl;
 	private String routingOverviewTbl;
 	private Calendar cal;
 	private SimpleDateFormat splDateFormt;
 	
 	
 	// verbindungssachen
 	private boolean isDBConnected;		// noch richtig implementieren oder weg lassen weil ehh jedesmal neu prfen?
 	private boolean askForConnectionRetry;
 	private static LocalDBConnection me;
 	
 	private LocalDBConnection() {
 		this.url 					= "jdbc:mysql://localhost:3306/";
 		this.user 					= "root";
 		this.passwd 				= "";
 		this.dbName 				= "db_publicMain";
 		this.chatLogTbl				= "t_chatLog";
 		this.msgTbl					= "t_msg";
 		this.usrTbl					= "t_usr";
 		this.nodeTbl				= "t_node";
 		this.groupTbl				= "t_group";
 		this.configTbl				= "t_config";
 		this.eventTypeTbl			= "t_eventType";
 		this.routingOverviewTbl		= "t_routingOverView";
 		this.isDBConnected 			= false;
 		this.askForConnectionRetry	= true;
 		this.cal					= Calendar.getInstance();
 		this.splDateFormt			= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 
 		if(connectToLocDBServer()){
 			isDBConnected = true;
 		}
 	}
 	public static LocalDBConnection getDBConnection() {
 		if (me == null) {
 			me = new LocalDBConnection();
 		}
 		return me;
 	}
 	private boolean connectToLocDBServer(){	// wird nur vom Construktor aufgerufen
 		try {
 			this.con = DriverManager.getConnection(url, user, passwd);
 			this.stmt = con.createStatement();
 			createDbAndTables();
 			LogEngine.log(this, "DB-ServerVerbindung hergestellt", LogEngine.INFO);
 			return true;
 		} catch (SQLException e) {
 			LogEngine.log(this, "DB-Verbindung fehlgeschlagen: " + e.getMessage(), LogEngine.ERROR);
 			isDBConnected = false;
 			if(askForConnectionRetry){
 				askForConnectionRetry = false;
 				int eingabe = JOptionPane.showConfirmDialog(null,
 		                "Es besteht keine Verbindung zum lokalen DB-Server!\n"
 						+ "Fehlerbeschreibung: " + e.getCause() + "\n"
 						+ "mchten sie einen erneuten versuch unternehmen?",
 		                "Fehler bei der DB-Verbindung.",
 		                JOptionPane.YES_NO_OPTION);
 				if (eingabe == 1){
 					askForConnectionRetry = false;	// nein ist 1
 						
 				} else if (eingabe == 0){
 					askForConnectionRetry = true;
 					connectToLocDBServer();	
 				}
 			}
 			
 			return false;
 		}
 	}
 	private void createDbAndTables (){	// wird nur vom Construktor aufgerufen
 		try {
 			this.stmt = con.createStatement();
 			stmt.addBatch("create database if not exists " + dbName);
 			stmt.addBatch("use " + dbName);
 			// erstellen der Message Tabelle
 			stmt.addBatch("create table if not exists "+ chatLogTbl + "(id INTEGER NOT NULL AUTO_INCREMENT," +		// hier autoincrement nutzen - kann ja mehrere mit der selben geben
 																	"msgID INTEGER NOT NULL," +
 																	"timestamp BIGINT NOT NULL," +
 																	"sender BIGINT NOT NULL," +
 																	"empfaenger BIGINT NOT NULL," +
 																	"typ varchar(30) NOT NULL," +
 																	"grp varchar(20) NOT NULL," +
 																	"data varchar(200) NOT NULL," +
 																	"primary key(id))" +
 																	"engine = INNODB");
 			// TODO Datentypen anpassen! 
 			// erstellen der user-Tabelle
 			stmt.addBatch("create table if not exists "+ usrTbl + "(id BIGINT NOT NULL," +		// hier die USER-ID
 					"alias VARCHAR(20) NOT NULL," +														// hier auf 20 Zeichen begrenzt
 					"primary key(id))" +
 					"engine = INNODB");
 			// erstellen der message-Tabelle was soll die machen? Verstehe ich nicht!??
 //						stmt.addBatch("create table if not exists "+ msgTbl + "(id BIGINT NOT NULL," +		// hier die USER-ID
 //								"alias VARCHAR(20)," +														// hier auf 20 Zeichen begrenzt
 //								"primary key(id))" +
 //								"engine = INNODB");
 			// erstellen der node-Tabelle fr ALLE nodes?
 			stmt.addBatch("create table if not exists "+ nodeTbl + "(ip VARCHAR(15) NOT NULL," +// Als sting abspeichern? 192.168.100.200 -> 15  
 					"hostname VARCHAR(20) NOT NULL," +											//TODO: wie lang max? 
 					"nodeID BIGINT NOT NULL," +
 					"primary key(nodeID))" +
 					"engine = INNODB");	
 			// erstellen der Gruppen-Tabelle
 			stmt.addBatch("create table if not exists "+ groupTbl + "(groupID BIGINT NOT NULL," +			
 					"name VARCHAR(20) NOT NULL," +													//TODO: wie lang max? 
 					"password VARCHAR(20) NOT NULL," +												//TODO: wie lang max?
 					"groupOwner BIGINT NOT NULL," +
 					"primary key(groupID))" +
 					"engine = INNODB");	
 			// erstellen der Config-Tabelle
 			stmt.addBatch("create table if not exists "+ configTbl + "(nodeID BIGINT NOT NULL," +			
 					"userID BIGINT NOT NULL," +														//TODO: wie lang max? 
 					"layout VARCHAR(20) NOT NULL," +												//TODO: wie lang max?
 					"remotDBSvrIP VARCHAR(15) NOT NULL," +											//TODO: Als sting abspeichern? 192.168.100.200 -> 15  
 					"Alias VARCHAR(20) NOT NULL," +
 					"primary key(nodeID,userID))" +
 					"engine = INNODB");	
 			// erstellen der EventTyp-Tabelle														//TODO: was macht das wo kommen daten her? Was wird genau gespeichert?!?
 			stmt.addBatch("create table if not exists "+ eventTypeTbl + "(id BIGINT NOT NULL," +			
 					"description VARCHAR(200) NOT NULL," +														//TODO: wie lang max? 
 					"primary key(ID))" +
 					"engine = INNODB");	
 			// erstellen die Routing-Tabelle
 			stmt.addBatch("create table if not exists "+ routingOverviewTbl + "(id BIGINT NOT NULL," +	//TODO: hier nochmal extrem nachdenken ;-)
 					"primary key(id))" +
 					"engine = INNODB");	
 			stmt.executeBatch();
 			LogEngine.log(this, "createDbAndTables erstellt", LogEngine.INFO);
 		} catch (SQLException e) {
 			LogEngine.log(this, "createDbAndTables fehlgeschlagen: "+ e.getMessage(), LogEngine.ERROR);
 		}
 	}
 	
 	// TODO in seperate Klasse auslagern! 
 	public void saveMsg (final MSG m){
 		Runnable tmp = new Runnable() {
 			public void run() {
 				if (isDBConnected) {
 					if (m.getTyp() == NachrichtenTyp.GROUP || m.getTyp() == NachrichtenTyp.PRIVATE) {
 						String saveStmt = ("insert into " + chatLogTbl + " (msgID,timestamp,sender,empfaenger,typ,grp,data)"
 								+ " VALUES ("
 								+ m.getId() + ","
 								+ m.getTimestamp() 	+ "," 
 								+ m.getSender() 	+ ","
 								+ m.getEmpfnger() 	+ ","
 								+ "'" + m.getTyp() 	+ "'"	+ ","
 								+ "'" + m.getGroup()+ "'"	+ ","
 								+ "'" + m.getData() + "'" + ")");
 						try {
 							//System.out.println(saveStmt);
 							stmt.execute(saveStmt);
 							LogEngine.log(LocalDBConnection.this,
 									"Nachicht in DB-Tabelle " + chatLogTbl
 											+ " eingetragen.", LogEngine.INFO);
 						} catch (Exception e) {
 							LogEngine.log(LocalDBConnection.this,
 									"Fehler beim eintragen in : " + chatLogTbl
 											+ " " + e.getMessage(),
 									LogEngine.ERROR);
 							isDBConnected = false;
 						}
 					}
 				} else {
 					//System.out.println("es besteht keine DB-Verbindung!");
 					if (askForConnectionRetry) {
 						if (connectToLocDBServer()){
 						isDBConnected = true;
 						createDbAndTables();
 						saveMsg(m);
 						}
 					} else {
 						// Erneuter Verbindungsversuch nicht gewollt
 					}
 				}
 			}
 		};
 		(new Thread(tmp)).start();
 	}
 	
 	public void deleteAllMsgs () {
 		if (isDBConnected) {
 			int eingabe = JOptionPane.showConfirmDialog(null,
 					"Youtre about to delete your ChatLog!\n"
 							+ "Are you really shore that you want to do it?",
 					"Delete confirmation", JOptionPane.YES_NO_OPTION);
 			if (eingabe == 1) {
 				// no, hes not shore!
 
 			} else if (eingabe == 0) {
 				askForConnectionRetry = true;
 				connectToLocDBServer();
 
 				try {
 					this.stmt = con.createStatement();
 					stmt.addBatch("DROP TABLE IF EXISTS " + chatLogTbl);
 					stmt.executeBatch();
 					createDbAndTables();
 
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}else {
 			askForConnectionRetry = true;
 			if (connectToLocDBServer()) {
 				isDBConnected = true;
 				deleteAllMsgs();
 			} else {
 				//System.out.println("Erneuter versuch der Verbindungsherstellung erfolglos!");
 			}
 		}
 	}
 	
 	public void searchInHistory (JTextPane historyContentTxt, String chosenNTyp, String chosenAliasOrGrpName, Date fromDateTime, Date toDateTime, HTMLEditorKit htmlKit, HTMLDocument htmlDoc){
 		if (isDBConnected){
 			try {
 				String tmpStmtStr = (
 						"SELECT * " +
 						"FROM " + chatLogTbl + " " +
 						"WHERE timestamp BETWEEN '" + fromDateTime.getTime() + "' AND '" + toDateTime.getTime() + "'");
 				if (!chosenNTyp.equals("ALL")){
 					tmpStmtStr = tmpStmtStr + " AND typ = '" + chosenNTyp + "'";
 				}
 				if (!chosenAliasOrGrpName.equals("...type in here.")){
 					//TODO: hier die bersetung von nr in string einbinden
 //					tmpStmtStr = tmpStmtStr + " AND ";
 				}
 				Statement searchStmt = con.createStatement();
 				ResultSet rs = searchStmt.executeQuery(tmpStmtStr);
 				
 				
 				
 				//TODO bei mehrfachen aufruf muss das HTML-Doc wieder geleert werden!
 				historyContentTxt.setText("");
 				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "<table>" + "<th><b>ID</b></th>"
 						+ "<th><b>msgID</th>" + "<th><b>Timestamp</th>"
 						+ "<th><b>Sender</th>" + "<th><b>Empfnger</th>" + "<th><b>Typ</th>"
 						+ "<th><b>Gruppe</th>" + "<th><b>Daten</th>", 0, 0, null);
 				while (rs.next()) {
 					
 					cal.setTimeInMillis(rs.getLong("timestamp"));
 					htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(),
 							"<tr>" + "<td>" + rs.getInt("id") + "<td>"
 									+ rs.getInt("msgID") + "<td>"
 									+ splDateFormt.format(cal.getTime())+ "<td>"
 									+ rs.getLong("sender") + "<td>"
 									+ rs.getLong("empfaenger") + "<td>"
 									+ rs.getString("typ") + "<td>"
 									+ rs.getString("grp") + "<td>"
 									+ rs.getString("data"), 0, 0, null);
 					htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "</tr>", 0, 0,
 							null);
 				}
 				htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), "</table>", 0, 0,
 						null);
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} 
 		}else {
 			JOptionPane.showMessageDialog(null,
 	                "Fr diese Operation ist eine Verbindung zum lokalen DB-Server zwingend notwendig",
 	                "Hinweis",
 	                JOptionPane.INFORMATION_MESSAGE);
 			askForConnectionRetry = true;
 			if (connectToLocDBServer()) {
 				isDBConnected = true;
 				searchInHistory(historyContentTxt, chosenNTyp, chosenAliasOrGrpName, fromDateTime, toDateTime,  htmlKit, htmlDoc);
 				
 			} else {
 				//System.out.println("Erneuter versuch der Verbindungsherstellung erfolglos!");
 			}
 		}
 	}
 	public void shutdownLocDB() {
 		// TODO alle verbindungen trennen
 		try {
			if(con!=null)con.close();
			
 			// TODO What else?
 		} catch (SQLException e) {
			LogEngine.log(this, e);
		}
		
		try {
			if(stmt!=null)stmt.close();
		} catch (SQLException e) {
			LogEngine.log(this, e);
			// TODO Auto-generated catch block
 		}
 	}
 
 }
 
