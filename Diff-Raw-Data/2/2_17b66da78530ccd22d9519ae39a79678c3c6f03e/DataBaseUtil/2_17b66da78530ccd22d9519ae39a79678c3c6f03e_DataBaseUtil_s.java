 /*
 	http://www.vnc.biz
     Copyright 2012, VNC - Virtual Network Consult GmbH
     Released under GPL Licenses.
 */
 package biz.vnc.zimbra.lighthistoryzimlet;
 import biz.vnc.zimbra.util.LocalConfig;
 import biz.vnc.zimbra.util.LocalDB;
 import biz.vnc.zimbra.util.ZLog;
 import java.awt.Stroke;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.Timestamp;
 import java.sql.Statement;
 
 public class DataBaseUtil {
 
 	synchronized public static void writeHistory(String messageId, String from, String to,String event,String movingId,String movinginfoId) {
 		Connection dbConnection = null;
 		PreparedStatement st=null;
 		String querydata=null;
 		ResultSet resultSet;
 		try {
 			String fromLocal ="-";
 			String fromDomain="-";
 			String toLocal="-";
 			String toDomain="-";
 			String movingmessageId = "-";
 			String moveinginfomessageId = "-";
 			String folderName = "-";
 			if (!from.equals("")) {
 				fromLocal= from.split("@")[0];
 				fromDomain = from.split("@")[1];
 			}
 			if (!to.equals("")) {
 				toLocal = to.split("@")[0];
 				toDomain = to.split("@")[1];
 			}
			if(!movingmessageId.equals("")) {
 				movingmessageId = movingId;
 			}
 			if(!movinginfoId.equals("")) {
 				moveinginfomessageId = movinginfoId;
 			}
 			dbConnection = LocalDB.connect(LocalConfig.get().db_name);
 			querydata = "select message_id from mail_history_mbox where message_id= ? and from_domain=? and from_localpart=? and event=? and to_domain=? and to_localpart=?";
 			st = dbConnection.prepareStatement(querydata);
 			st.setString(1, messageId);
 			st.setString(2, fromDomain);
 			st.setString(3, fromLocal);
 			st.setString(4, event);
 			st.setString(5, toDomain);
 			st.setString(6, toLocal);
 			resultSet = st.executeQuery();
 
 			if(!resultSet.next()) {
 				String query = "insert into mail_history_mbox(logtime,message_id,from_domain,from_localpart,to_domain,to_localpart,event,moveingId,movinginfoid,foldername) values(?,?,?,?,?,?,?,?,?,?)";
 				st = dbConnection.prepareStatement(query);
 				st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
 				st.setString(2, messageId.trim());
 				st.setString(3,fromDomain);
 				st.setString(4, fromLocal);
 				st.setString(5, toDomain);
 				st.setString(6, toLocal);
 				st.setString(7, event);
 				st.setString(8, movingmessageId);
 				st.setString(9, moveinginfomessageId);
 				st.setString(10,folderName);
 				st.execute();
 				ZLog.info("biz_vnc_lightweight_history", "Event Successfully Inserted");
 			}
 		} catch (Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while loging into mail history table", e);
 		}
 		finally {
 			try {
 				dbConnection.close();
 			} catch (Exception e) {
 				ZLog.err("biz_vnc_lightweight_history", "Error in writeHistory", e);
 			}
 		}
 	}
 
 	synchronized public static void writeMoveHistory(String messageId, String from, String to,String event,String movingId,String moveinfoid,String folderName) {
 		Connection dbConnection = null;
 		PreparedStatement st=null;
 		String querydata=null;
 		ResultSet resultSet;
 		try {
 			String fromLocal ="-";
 			String fromDomain="-";
 			String toLocal="-";
 			String toDomain="-";
 			String movingmessageId = "-";
 			String datamovingid = "";
 			String foldername ="-";
 			if (!from.equals("")) {
 				fromLocal= from.split("@")[0];
 				fromDomain = from.split("@")[1];
 			}
 			if (!to.equals("")) {
 				toLocal = to.split("@")[0];
 				toDomain = to.split("@")[1];
 			}
 			if(!movingmessageId.equals("")) {
 				movingmessageId = movingId;
 			}
 			if(!folderName.equals("")) {
 				foldername = folderName;
 			}
 
 			dbConnection = LocalDB.connect(LocalConfig.get().db_name);
 			querydata = "select movinginfoid from mail_history_mbox where message_id= ? and event='"+MailHistoryLogging.MOVE+"' and movinginfoid=? and foldername=?";
 			st = dbConnection.prepareStatement(querydata);
 			st.setString(1, messageId);
 			st.setString(2, moveinfoid);
 			st.setString(3,foldername);
 			resultSet = st.executeQuery();
 
 			if(resultSet.next()) {
 				datamovingid= resultSet.getString("movinginfoid");
 				if(!moveinfoid.equals(datamovingid)) {
 					ZLog.info("biz_vnc_lightweight_history", "Second Time Move Event record");
 					String query = "insert into mail_history_mbox(logtime,message_id,from_domain,from_localpart,to_domain,to_localpart,event,moveingId,movinginfoid,foldername) values(?,?,?,?,?,?,?,?,?,?)";
 					st = dbConnection.prepareStatement(query);
 					st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
 					st.setString(2, messageId.trim());
 					st.setString(3,fromDomain);
 					st.setString(4, fromLocal);
 					st.setString(5, toDomain);
 					st.setString(6, toLocal);
 					st.setString(7, event);
 					st.setString(8, movingId);
 					st.setString(9, moveinfoid);
 					st.setString(10,foldername);
 					st.execute();
 					ZLog.info("biz_vnc_lightweight_history", "Move Event Data Successfully Inserted");
 				}
 			} else {
 				ZLog.info("biz_vnc_lightweight_history", "First Time Move Event record");
 				String query = "insert into mail_history_mbox(logtime,message_id,from_domain,from_localpart,to_domain,to_localpart,event,moveingId,movinginfoid,foldername) values(?,?,?,?,?,?,?,?,?,?)";
 				st = dbConnection.prepareStatement(query);
 				st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
 				st.setString(2, messageId.trim());
 				st.setString(3,fromDomain);
 				st.setString(4, fromLocal);
 				st.setString(5, toDomain);
 				st.setString(6, toLocal);
 				st.setString(7, event);
 				st.setString(8, movingId);
 				st.setString(9, moveinfoid);
 				st.setString(10,foldername);
 				st.execute();
 				ZLog.info("biz_vnc_lightweight_history", "Data Successfully Inserted");
 			}
 		} catch (Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while loging into mail history table", e);
 		}
 		finally {
 			try {
 				dbConnection.close();
 			} catch (Exception e) {
 				ZLog.err("biz_vnc_lightweight_history", "Error in writeHistory", e);
 			}
 		}
 	}
 
 	synchronized public static void writeDeleteHistory(String msgid,String from) {
 		Connection dbConnection = null;
 		try {
 			String query = "select message_id from mail_history_mbox where event="+MailHistoryLogging.MOVE+" and moveingId = ?";
 			dbConnection = LocalDB.connect(LocalConfig.get().db_name);
 			PreparedStatement st = dbConnection.prepareStatement(query);
 			st.setString(1, msgid);
 			ResultSet resultSet=  st.executeQuery();
 			String messageId="";
 
 			if(resultSet.next()) {
 				messageId = resultSet.getString("message_id");
 				ZLog.info("biz_vnc_lightweight_history", "Delete Move event Catched");
 				writeHistory(messageId, "", from, MailHistoryLogging.DELETE, msgid,"");
 			}
 
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while loging into mail history table", e);
 		}
 		finally {
 			try {
 				dbConnection.close();
 			} catch (Exception e) {
 				ZLog.err("biz_vnc_lightweight_history", "Error in writeHistory", e);
 			}
 		}
 
 	}
 
 	synchronized public static String getMsgId(String messageId,String to) {
 		Connection dbConnection = null;
 		String toLocal="-";
 		String toDomain="-";
 		if (!to.equals("")) {
 			toLocal = to.split("@")[0];
 			toDomain = to.split("@")[1];
 		}
 		String message_Id  = new String();
 		try {
 			String query = "select message_id from mail_history_mbox where moveingId = ? and to_domain =? and to_localpart=?";
 			dbConnection = LocalDB.connect(LocalConfig.get().db_name);
 			PreparedStatement st = dbConnection.prepareStatement(query);
 			st.setString(1, messageId);
 			st.setString(2,toDomain);
 			st.setString(3,toLocal);
 			ResultSet resultSet=  st.executeQuery();
 			if(resultSet.next()) {
 				message_Id = resultSet.getString("message_id");
 			}
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while Getting MessageId from small Id", e);
 		}
 		finally {
 			try {
 				dbConnection.close();
 			} catch (Exception e) {
 				ZLog.err("biz_vnc_lightweight_history", "Error in Message Id", e);
 			}
 		}
 		return message_Id;
 	}
 }
