 /*
 	http://www.vnc.biz
 	Copyright 2012, VNC - Virtual Network Consult GmbH
     Released under GPL Licenses.
 */
 package biz.vnc.zimbra.lighthistoryzimlet;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import biz.vnc.zimbra.util.JSPUtil;
 import biz.vnc.zimbra.util.ZLog;
 import com.zimbra.cs.account.soap.SoapProvisioning.Options;
 import com.zimbra.cs.account.soap.SoapProvisioning;
 import com.zimbra.cs.account.Domain;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class MailHistoryLogging {
 
 	public static final String DELIVERED = "1";
 	public static final String MOVE = "2";
 	public static final String DELETE = "3";
 	public static Thread internalthread = null,externalthread=null,availablethread=null;
 	public static FileInputStream fstream=null,filestream=null;
 	public static SoapProvisioning provisioning;
 
 	static {
 
 		try{
 			fstream=new FileInputStream("/opt/zimbra/log/mailbox.log");
 			filestream = new FileInputStream("/var/log/zimbra.log");
 			internalthread = new Thread(new RecipientExternalMailHistoryLogging(filestream));
 			externalthread = new Thread(new RecipientInternalMailHistoryLogging(fstream));
 			availablethread = new Thread(new ThreadAvailable(externalthread,internalthread));
 			Options options = new Options();
 			options.setLocalConfigAuth(true);
 			provisioning = new SoapProvisioning(options);
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while static method called", e);
 		}
 
 		internalthread.start();
 		externalthread.start();
 		availablethread.start();
 	}
 
 	public static String getDataFromBracket(String name) {
 		if (name == null) {
 			return null;
 		}
 		if (name.contains("<")) {
 			return name.substring(name.indexOf("<")+1, name.indexOf(">",name.indexOf("<")));
 		}
 		return name;
 	}
 }
 
 class RecipientExternalMailHistoryLogging extends Thread {
 
 	public static FileInputStream filestream=null;
 	public  RecipientExternalMailHistoryLogging (FileInputStream fstream) {
 		filestream = fstream;
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				getExternalMailEvents();
 				Thread.sleep(2000);
 				continue;
 			} catch (Exception e) {
 				ZLog.err("biz_vnc_lightweight_history", "Error in run method while ExternalMailHistoryLogging", e);
 			}
 		}
 	}
 
 	public void getExternalMailEvents() {
 		try {
 			DataInputStream in = new DataInputStream(filestream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String senderName=null;
 			String receiverName=null;
 			String message_id = null;
 			String strLine;
 			while ((strLine = br.readLine()) != null)  {
 				String messageId =null;
 				String receivers = null;
 				String sender = null;
 				if(strLine.contains("FWD via SMTP")) {
 					receivers = strLine.substring(strLine.indexOf("->"),strLine.indexOf(",BODY"));
 					sender = MailHistoryLogging.getDataFromBracket(strLine.split("->")[0].split(":")[4]);
 				}
 				if(strLine.contains("message-id")) {
 					message_id=MailHistoryLogging.getDataFromBracket(strLine.split("=")[1]);
 				}
 				if(message_id!=null && receivers!=null) {
 for(String receiver : receivers.split(",")) {
 						receiver = MailHistoryLogging.getDataFromBracket(receiver);
 						if(isExternalMail(receiver)) {
 							ZLog.info("biz_vnc_lightweight_history", "External Mail Deliver Event");
 							ZLog.info("biz_vnc_lightweight_history", "Sender : "+sender);
 							ZLog.info("biz_vnc_lightweight_history", "External Receiver : "+receiver);
 							ZLog.info("biz_vnc_lightweight_history", "Event : "+MailHistoryLogging.DELIVERED);
 							DataBaseUtil.writeHistory(message_id, sender, receiver, MailHistoryLogging.DELIVERED,"","");
 						}
 					}
 				}
 			}
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while getting External Mail Event ", e);
 		}
 	}
 
 	synchronized public boolean isExternalMail(String receiver) {
 		List<Domain> alldomains = null;
 		boolean result = false;
 		int flag =1;
 		String receiverdomain = receiver.split("@")[1];
 		try {
 			Options options = new Options();
 			options.setLocalConfigAuth(true);
 			SoapProvisioning provisioning = new SoapProvisioning(options);
 			alldomains = provisioning.getAllDomains();
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while getting Domains List ", e);
 		}
 		for(int i=0; i<alldomains.size(); i++) {
 			if(alldomains.get(i).getName().equals(receiverdomain)) {
 				flag =0;
 			}
 		}
 		if(flag == 1) {
 			result = true;
 		} else {
 			result = false;
 		}
 		return result;
 	}
 }
 
 class RecipientInternalMailHistoryLogging extends Thread {
 
 	public static FileInputStream filestream=null;
 
 	public RecipientInternalMailHistoryLogging(FileInputStream stream) {
 		filestream = stream;
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				getInternalMailEvents();
 				Thread.sleep(2000);
 				continue;
 			} catch (Exception e) {
 				ZLog.err("biz_vnc_lightweight_history", "Error while  run method in InternalMailHistoryLogging", e);
 			}
 		}
 	}
 
 	public void getInternalMailEvents() {
 		try {
 			DataInputStream in = new DataInputStream(filestream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine,msgId=null,receivermsgId;
 			String senderName=null;
 			String receiverName=null;
 			while ((strLine = br.readLine()) != null)  {
 				ZLog.info("biz_vnc_lightweight_history", "Read   mailbox.log    file ");
 				if(strLine.contains("lmtp - Delivering")) {
 					String senderdata[] = strLine.split(",");
 for(String sender:senderdata) {
 						if(sender.contains("sender")) {
 							senderName = sender.split("=")[1];
 						}
 						if(sender.contains("msgid")) {
 							msgId = MailHistoryLogging.getDataFromBracket(sender.split("=")[1]);
 						}
 					}
 				}
 
 				if(strLine.contains("mailop - Adding Message")) {
 					String receiverdata[] = strLine.split(",");
 for(String receive:receiverdata) {
 						if(receive.contains("name")) {
 							receiverName = receive.split("=")[1].split(";")[0];
 						}
 						if(receive.contains("Message-ID")) {
 							receivermsgId = MailHistoryLogging.getDataFromBracket(receive.split("=")[1]);
 							if(msgId !=null) {
 								if(msgId.equals(receivermsgId)) {
 									String smallMessageId = strLine.split("id=")[2].split(",")[0];
 									ZLog.info("biz_vnc_lightweight_history", "smallid : "+smallMessageId);
 									ZLog.info("biz_vnc_lightweight_history", "message_id : "+msgId);
 									ZLog.info("biz_vnc_lightweight_history", "Sender : "+senderName);
 									ZLog.info("biz_vnc_lightweight_history", "Receiver : "+receiverName);
 									ZLog.info("biz_vnc_lightweight_history", "Event : "+MailHistoryLogging.DELIVERED);
 									DataBaseUtil.writeHistory(msgId, senderName, receiverName, MailHistoryLogging.DELIVERED,smallMessageId,"");
 								}
 							}
 						}
 					}
 				}
 
 				if(strLine.contains("mailop - Moving Conversation")) {
 					String moveinfoid = strLine.split(",")[1].split("INFO")[0];
					String moveId = strLine.split("Affected message ids:")[1];
 					String receiver = strLine.split("]")[1].split("=")[1].split(";")[0];
 					moveId = moveId.substring(0,moveId.length()-1).trim();
 					String messageId=DataBaseUtil.getMsgId(moveId,receiver);
 					String folderName = strLine.split("Folder")[1].split("\\(")[0].trim();
 					ZLog.info("biz_vnc_lightweight_history", "Moving to : "+folderName);
 					ZLog.info("biz_vnc_lightweight_history", "message_id : "+messageId);
 					ZLog.info("biz_vnc_lightweight_history", "small_id : "+moveId);
 					ZLog.info("biz_vnc_lightweight_history", "info_id : "+moveinfoid);
 					ZLog.info("biz_vnc_lightweight_history", "Receiver : "+receiver);
 					ZLog.info("biz_vnc_lightweight_history", "Event : "+MailHistoryLogging.MOVE);
 					if(!messageId.equals("")) {
 						DataBaseUtil.writeMoveHistory(messageId, "", receiver, MailHistoryLogging.MOVE, moveId, moveinfoid,folderName);
 					}
 				}
 
 				if(strLine.contains("mailop - Moving Message")) {
 					String moveinfoid = strLine.split(",")[1].split("INFO")[0];
					String moveId = strLine.split("Affected message ids:")[1];
 					String receiver = strLine.split("]")[1].split("=")[1].split(";")[0];
 					moveId = moveId.substring(0,moveId.length()-1).trim();
 					String messageId=DataBaseUtil.getMsgId(moveId,receiver);
 					String folderName = strLine.split("Folder")[1].split("\\(")[0].trim();
 					ZLog.info("biz_vnc_lightweight_history", "Moving to : "+folderName);
 					ZLog.info("biz_vnc_lightweight_history", "message_id : "+messageId);
 					ZLog.info("biz_vnc_lightweight_history", "small_id : "+moveId);
 					ZLog.info("biz_vnc_lightweight_history", "info_id : "+moveinfoid);
 					ZLog.info("biz_vnc_lightweight_history", "Receiver : "+receiver);
 					ZLog.info("biz_vnc_lightweight_history", "Event : "+MailHistoryLogging.MOVE);
 					if(!messageId.equals("")) {
 						DataBaseUtil.writeMoveHistory(messageId, "", receiver, MailHistoryLogging.MOVE, moveId, moveinfoid,folderName);
 					}
 				}
 
 				if(strLine.contains("mailop - Moving VirtualConversation")) {
 					String moveinfoid = strLine.split(",")[1].split("INFO")[0];
					String moveId = strLine.split("Affected message ids:")[1];
 					String receiver = strLine.split("]")[1].split("=")[1].split(";")[0];
 					moveId = moveId.substring(0,moveId.length()-1).trim();
 					String messageId=DataBaseUtil.getMsgId(moveId,receiver);
 					String folderName = strLine.split("Folder")[1].split("\\(")[0].trim();
 					ZLog.info("biz_vnc_lightweight_history", "Moving to : "+folderName);
 					ZLog.info("biz_vnc_lightweight_history", "message_id : "+messageId);
 					ZLog.info("biz_vnc_lightweight_history", "small_id : "+moveId);
 					ZLog.info("biz_vnc_lightweight_history", "info_id : "+moveinfoid);
 					ZLog.info("biz_vnc_lightweight_history", "Receiver : "+receiver);
 					ZLog.info("biz_vnc_lightweight_history", "Event : "+MailHistoryLogging.MOVE);
 					if(!messageId.equals("")) {
 						DataBaseUtil.writeMoveHistory(messageId, "", receiver, MailHistoryLogging.MOVE, moveId, moveinfoid,folderName);
 					}
 				}
 
 
 				if(strLine.contains("Deleting items")) {
 					String deletedId = strLine.split(":")[4];
 					deletedId = deletedId.substring(0,deletedId.length()-1).trim();
 					String receiver =strLine.split("]")[1].split("=")[1].split(";")[0];
 					ZLog.info("biz_vnc_lightweight_history", "small ID : "+deletedId);
 					ZLog.info("biz_vnc_lightweight_history", "Receiver : "+receiver);
 					ZLog.info("biz_vnc_lightweight_history", "Event : "+MailHistoryLogging.DELETE);
 					DataBaseUtil.writeDeleteHistory(deletedId,receiver);
 				}
 
 				if(strLine.contains("mailop - Deleting Message")) {
 					String deletedId = strLine.split("id")[2];
 					deletedId = deletedId.substring(1,deletedId.length()-2).trim();
 					String receiver =strLine.split("]")[1].split("=")[1].split(";")[0];
 					ZLog.info("biz_vnc_lightweight_history", "small ID : "+deletedId);
 					ZLog.info("biz_vnc_lightweight_history", "Receiver : "+receiver);
 					ZLog.info("biz_vnc_lightweight_history", "Event : "+MailHistoryLogging.DELETE);
 					DataBaseUtil.writeDeleteHistory(deletedId,receiver);
 				}
 			}
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error while getting Internal Mail Event from Log file", e);
 		}
 	}
 }
 
 class ThreadAvailable extends Thread {
 	Thread externalthread=null, internalthread=null;
 	Timer timer = new Timer();
 	public ThreadAvailable(Thread availableexternalthread,Thread availableinternalthread) {
 		externalthread=availableexternalthread;
 		internalthread=availableinternalthread;
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				availableThread();
 				scheduleThread();
 				Thread.sleep(2000);
 				continue;
 			} catch (Exception e) {
 				ZLog.err("biz_vnc_lightweight_history", "Error in run method while ThreadAvailable", e);
 			}
 		}
 	}
 
 	public void scheduleThread() {
 		long delay =  18000000;
 		try {
 			Timer timer = new Timer();
 			ZLog.info("biz_vnc_lightweight_history", "scheduleThread method is called");
 			timer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					ZLog.info("biz_vnc_lightweight_history", "scheduleThread is called and restart the both the thread");
 					externalthread.interrupt();
 					internalthread.interrupt();
 					externalthread.start();
 					internalthread.start();
 				}
 			}, delay);
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error in scheduleThread method", e);
 		}
 	}
 	public void availableThread() {
 		try {
 			if(!externalthread.isAlive()) {
 				externalthread.start();
 			}
 			if(!internalthread.isAlive()) {
 				internalthread.start();
 			}
 			ZLog.info("biz_vnc_lightweight_history","ThreadAvailable Check InternalThread isAlive : "+internalthread.isAlive() +"   ExternalThread isAlive : "+externalthread.isAlive());
 		} catch(Exception e) {
 			ZLog.err("biz_vnc_lightweight_history", "Error in availableThread method while check availability of the thread", e);
 		}
 	}
 
 }
