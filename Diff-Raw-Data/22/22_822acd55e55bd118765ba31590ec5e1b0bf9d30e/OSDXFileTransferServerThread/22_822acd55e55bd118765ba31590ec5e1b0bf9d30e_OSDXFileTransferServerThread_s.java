 package org.fnppl.opensdx.file_transfer;
 /*
  * Copyright (C) 2010-2012 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
  */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.Socket;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import javax.naming.NoInitialContextException;
 
 import org.fnppl.opensdx.common.Util;
 import org.fnppl.opensdx.file_transfer.helper.ClientSettings;
 import org.fnppl.opensdx.file_transfer.helper.FileTransferLog;
 import org.fnppl.opensdx.file_transfer.helper.RightsAndDuties;
 import org.fnppl.opensdx.file_transfer.model.RemoteFile;
 import org.fnppl.opensdx.file_transfer.trigger.Trigger;
 import org.fnppl.opensdx.file_transfer.trigger.TriggerContext;
 import org.fnppl.opensdx.security.AsymmetricKeyPair;
 import org.fnppl.opensdx.security.MD5;
 import org.fnppl.opensdx.security.OSDXKey;
 import org.fnppl.opensdx.security.SecurityHelper;
 import org.fnppl.opensdx.security.SymmetricKey;
 import org.fnppl.opensdx.xml.Document;
 
 public class OSDXFileTransferServerThread extends Thread {
 	public final static boolean DEBUG = true;
 	
 	private OSDXFileTransferServer server;
 	private Socket socket;
 	private BufferedInputStream in;
 	private BufferedOutputStream out;
 	private SecureConnection data = null;
 	private ClientSettings cs = null;
 	
 	private HashMap<Long, FileUploadInfo> uploads = new HashMap<Long, FileUploadInfo>();
 
 	
 	private OSDXKey mySigningKey;
 	private String clientID = "";
 	private String addr = "unknown";
 	private SymmetricKey key= null;
 	private String client_keyid = null; 
 	private byte[] client_nonce = null;
 	private byte[] server_nonce = null;
 	
 	private long maxFilelengthForMD5 = 100*1024*1024L; //100 MB
 	protected int maxPacketSize = 50*1024; //50kB
 	
 
 	public OSDXFileTransferServerThread(Socket socket, OSDXKey mySigningKey, OSDXFileTransferServer server) throws Exception {
 		this.socket = socket;
 		this.mySigningKey = mySigningKey;
 		this.server = server;
 		
 		in = new BufferedInputStream(socket.getInputStream());
 		out = new BufferedOutputStream(socket.getOutputStream());
 		data = new SecureConnection(null, in,out);
 		clientID = socket.getInetAddress().getHostAddress()+":"+socket.getPort();
 		addr = socket.getInetAddress().getHostAddress();
 		
 //		if(DEBUG) System.out.println("connected to client: "+clientID+ " at "+FileTransferLog.getDateString()); 
 		if(DEBUG) System.out.println("connected to client: "+clientID+ " at "+FileTransferLog.getDateString());
 		if (server!=null) server.log.logIncomingConnection(clientID, addr,"");
 	}
 
 
 	public void run() {
 		boolean run = initSecureConnection();
 		
 		//just wait and listen
 		while (run && socket.isConnected()) {
 			try {
 				run = data.receiveNextPackage();
 				if (run) {
 					onRequestReceived(data.id, data.num, data.type, data.content);
 					//String content = new String(data.content, "UTF-8");
 					//System.out.println("SERVER RECEIVED :: "+content);
 				}
 			} catch (Exception ex) {
 				run = false;
 				server.log.logError(clientID, addr, ex.getMessage());	
 				ex.printStackTrace();
 			}
 		}
 		if(DEBUG) System.out.println("closing socket to client: "+clientID+ " at "+FileTransferLog.getDateString());
 		server.log.logConnectionClose(clientID, socket.getInetAddress().getHostAddress(),"");
 	}
 
 	public HashMap<String, Object> buildContextParams(ClientSettings cs) {
 		HashMap<String, Object> context = new HashMap<String, Object>();
 		context.put(TriggerContext.USERNAME, cs.getUsername());
 		context.put(TriggerContext.KEYID, cs.getKeyID());
 		return context;
 	}
 
 	public HashMap<String, Object> buildContextParamsWithDate(ClientSettings cs) {
 		HashMap<String, Object> context = buildContextParams(cs);
 		long datetime = System.currentTimeMillis();
 		context.put(TriggerContext.RELATED_START_DATE, datetime);
 		context.put(TriggerContext.RELATED_END_DATE, datetime);
 		return context;
 	}
 	
 	public void onRequestReceived(long commandid, int num, byte code, byte[] content) {
 		try {
 			
 			if(DEBUG) System.out.print("RECEIVED REQUEST at "+FileTransferLog.getDateString()+" : id="+commandid+"\tCLIENTID["+clientID+"]");
 			
 			if (code == SecureConnection.TYPE_DATA && content !=null) {
 				if(DEBUG) System.out.println(", DATA len="+content.length);
 				//handle data
 				FileUploadInfo upload = uploads.get(commandid);
 				if (upload!=null) {
 					if (upload.out==null) {
 						upload.out = new FileOutputStream(upload.file, true);
 					}
 					
 					if(DEBUG) System.out.println("appending "+content.length+" bytes to "+upload.file.getAbsolutePath());
 					
 					upload.out.write(content);
 					upload.loaded += content.length;
 					upload.md5.update(content);
 					
 					if (upload.fromStream) {
 						
 					}
 					else {
 						if (upload.loaded>=upload.length) {
 							//ready with upload
 							if(DEBUG) System.out.println("upload ready: "+upload.file.getAbsolutePath());
 							upload.out.close();
 							if (upload.loaded == upload.length) {
 								boolean ok = true;
 								if (upload.md5String!=null) {
 									//check md5
 									try {
 										byte[] my_md5 = upload.md5.getMD5bytes();
 										byte[] your_md5 = SecurityHelper.HexDecoder.decode(upload.md5String);
 										if (!Arrays.equals(my_md5,your_md5)) {
 											ok = false;
 											System.out.println("MD5 check failed for "+upload.file.getAbsolutePath());
 										} else {
 											System.out.println("MD5 check ok for "+upload.file.getAbsolutePath());
 										}
 									} catch (Exception ex) {
 										ok = false;
 									}
 									if (!ok) {
 										data.setError(commandid, num, "md5 check failed");
 										data.sendPackage();
 									}
 								}
 								if (ok) {
 									data.setAckComplete(commandid, num);
 									data.sendPackage();
 								}
 							} else {
 								data.setError(commandid, num, "wrong filesize");
 								data.sendPackage();
 							}
 							
 							//trigger upload_end event
 							HashMap<String, Object> context = buildContextParams(cs);
 							context.put(TriggerContext.RELATED_FILE,upload.file);
 							context.put(TriggerContext.RELATED_FILENAME,upload.file.getAbsolutePath());
 							context.put(TriggerContext.RELATED_START_DATE,upload.uploadStartDatetime);
 							context.put(TriggerContext.RELATED_END_DATE,System.currentTimeMillis());
 							cs.triggerEvent(Trigger.TRIGGER_UPLOAD_END, context);
 						} else {
 							//dont ack every package ?
 						}
 					}
 				}
 				else {
 					if(DEBUG) System.out.println("No UploadInfo found for id:"+commandid);
 				}
 				
 			}
 			else if (content != null) { //if (code == SecureConnection.TYPE_TEXT) {
 				String text = new String(content,"UTF-8");
 				if(DEBUG)  System.out.println(", "+SecurityHelper.HexDecoder.encode(new byte[]{(byte)code})+" :: MSG: "+text);
 				
 				//parse command
 				String command = text;
 				String param = null;
 				int ind = text.indexOf(' ');
 				if (ind>0) {
 					command = text.substring(0,ind);
 					if (text.length()>ind+1) {
 						param = text.substring(ind+1);
 					}
 				}
 				//find right handler method
 				command = command.toLowerCase();
 				try {
 					Method commandHandler = getClass().getMethod("handle_"+command, Long.TYPE, Integer.TYPE, Byte.TYPE, String.class);
 					commandHandler.invoke(this, commandid, num, code, param);
 
 				} catch (NoSuchMethodException ex) {
 					if(DEBUG) System.out.println("NoSuchMethodException");
 					handle_command_not_implemented(commandid, num, code, command, param);
 				} catch (InvocationTargetException ex) {
 					if(DEBUG) System.out.println("InvocationTargetException");
 					handle_command_not_implemented(commandid, num, code, command, param);
 				} catch (IllegalAccessException ex) {
 					if(DEBUG) System.out.println("IllegalAccessException");
 					handle_command_not_implemented(commandid, num, code, command, param);
 				}
 			}
 			
 		} catch (Exception e) {
 			server.log.logError(clientID, addr, "onRequestReceived :: Exception :: "+e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	
 	public boolean initSecureConnection() {
 		try {
 			byte[] clientRequest = data.receiveRawBytesPackage();
 			String[] lines = new String(clientRequest,"UTF-8").split("\n");
 			if (lines!=null) {
 				boolean ok = true;
 				String clientVersion = lines[0];
 				String host = lines[1];
 
 				client_nonce = SecurityHelper.HexDecoder.decode(lines[2]);
 				client_keyid = lines[3];
 				byte[] client_mod = SecurityHelper.HexDecoder.decode(lines[4]);
 				byte[] client_exp = SecurityHelper.HexDecoder.decode(lines[5]);
 				byte[] client_signature = SecurityHelper.HexDecoder.decode(lines[6]);
 				AsymmetricKeyPair client_pubkey = new AsymmetricKeyPair(client_mod, client_exp, null);
 
 				byte[][] checks = SecurityHelper.getMD5SHA1SHA256(client_nonce);
 				boolean verifySig = client_pubkey.verify(client_signature, checks[1],checks[2],checks[3],0L);
 				if (verifySig) {
 					server.log.logDebug(clientID, addr, "initSecureConnection :: signature of client_nonce verified.");
 					//generate response
 					server_nonce = SecurityHelper.getRandomBytes(32);
 					StringBuffer msg = new StringBuffer();
 					msg.append(" 200 OSDXFileTransferServer "+OSDXFileTransferServer.VERSION+"\n");
 					msg.append(host+"\n");
 					msg.append(mySigningKey.getKeyID()+"\n");
 					msg.append(SecurityHelper.HexDecoder.encode(mySigningKey.getPublicModulusBytes(),':',-1)+"\n");
 					msg.append(SecurityHelper.HexDecoder.encode(mySigningKey.getPublicExponentBytes(),':',-1)+"\n");
 
 					//encrypted part
 					StringBuffer encmsg = new StringBuffer();
 					encmsg.append(SecurityHelper.HexDecoder.encode(client_nonce,':',-1)+"\n");
 					encmsg.append(SecurityHelper.HexDecoder.encode(server_nonce,':',-1)+"\n");
 					encmsg.append(lines[3]+"\n");
 					encmsg.append(lines[4]+"\n");
 					encmsg.append(lines[5]+"\n");
 					byte[] enc = client_pubkey.encryptBlocks(encmsg.toString().getBytes("UTF-8"));
 
 					//sign enc part
 					checks = SecurityHelper.getMD5SHA1SHA256(enc);				
 					byte[] sigOfEnc = mySigningKey.sign(checks[1],checks[2],checks[3],0L);
 
 					//add signature bytes and enc part
 					msg.append(SecurityHelper.HexDecoder.encode(sigOfEnc,':',-1)+"\n");
 					msg.append("ENC "+enc.length+"\n");
 
 					//build sym key of client_nonce and server nonce
 					byte[] concat_nonce = SecurityHelper.concat(client_nonce, server_nonce);
 					byte[] key_bytes = SecurityHelper.getSHA256(concat_nonce); 			//32 bytes = 256 bit
 					byte[] iv = Arrays.copyOf(SecurityHelper.getMD5(concat_nonce),16);	//16 bytes = 128 bit
 					key = new SymmetricKey(key_bytes, iv);
 					data.key = key;
 
 					//send packet
 					data.sendRawBytes(msg.toString().getBytes("UTF-8"));
 					data.sendRawBytes(enc);
 					
 				//	data.setAck(0, 0);
 
 				} else {
 					server.log.logError(clientID, addr, "initSecureConnection :: ERROR: verification of client_nonce signature faild!");
 					if(DEBUG) System.out.println("ERROR: verification of client_nonce signature failed!");
 					String debugMsg = "";
 					for (int i=0;i<lines.length;i++) {
 						if(DEBUG) System.out.println("("+(i+1)+")"+" "+lines[i]);
 						debugMsg += lines[i]+"\n";
 					}
 					server.log.logDebug(clientID, addr, debugMsg);
 					ok = false;
 					//				String msg = version+" 421 You are not authorized to make the connection\n";
 					//				msg += host+"\n";
 					socket.close();
 				}
 				return ok;
 			}
 		} catch (Exception e1) {
 			server.log.logError(clientID, addr, "initSecureConnection :: Exception :: "+e1.getMessage());
 			e1.printStackTrace();
 		}
 		return false;
 	}
 
 
 	// COMMAND IMPLEMENTATIONS
 	
 	public void handle_command_not_implemented(long commandid, int num, byte code, String command, String param) throws Exception {
 		if(DEBUG) System.out.println("COMMAND NOT UNDERSTOOD: "+command+" PARAM="+param);
 		data.setErrorCommandNotUnderstood(commandid, num);
 		data.sendPackage();
 		server.log.logError(clientID, addr, "COMMAND NOT UNDERSTOOD :: "+command);
 	}
 	
 	public void handle_login(long commandid, int num, byte code, String username) throws Exception {
 		if(DEBUG) System.out.println("handle_login :: "+username);
 		if (username!=null) {
 			String userid = username+"::"+client_keyid;
 			if(DEBUG) System.out.println("userid: "+userid);
 			cs  = server.getClientSetting(userid);
 			
 			if (cs!=null) {
 				//login ok -> ACK with rights and duties
 				String param = Util.makeParamsString(new String[]{client_keyid, Document.buildDocument(cs.getRightsAndDuties().toElement(true)).toStringCompact()});
 				if(DEBUG) System.out.println("SENDING: ACK :: "+param);
 				data.setAck(commandid, num, param);
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "LOGIN", username, param);
 				
 				//trigger login event
 				HashMap<String, Object> context = buildContextParamsWithDate(cs);
 				cs.triggerEvent(Trigger.TRIGGER_LOGIN, context);
 			}
 			else {
 				//login failed
 				data.setError(commandid, num, "ERROR IN LOGIN :: ACCESS DENIED");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "LOGIN", username, "ERROR IN LOGIN :: ACCESS DENIED");
 			}
 		} else {
 			//login failed
 			data.setError(commandid, num, "ERROR IN LOGIN :: MISSING USERNAME");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "LOGIN", username, "ERROR IN LOGIN :: MISSING USERNAME");
 		}
 	}
 	
 	public void handle_userpasslogin(long commandid, int num, byte code, String userauth) throws Exception {
 		if(DEBUG) System.out.println("handle_userpasslogin :: "+userauth);
 		String username = userauth.substring(0, userauth.indexOf("\t"));
 		String auth = userauth.substring(userauth.indexOf("\t")+1);
 		
 		if (username != null && auth != null) {
 			String userid = username+"::"+auth;
 			
 			if(DEBUG) System.out.println("userid: "+userid);
 			cs  = server.getClientSetting(userid);
 
 			if(cs != null) {
 				//login ok -> ACK with rights and duties
 				String param = Util.makeParamsString(new String[]{client_keyid, Document.buildDocument(cs.getRightsAndDuties().toElement(true)).toStringCompact()});
 				if(DEBUG) System.out.println("SENDING: ACK :: "+param);
 				data.setAck(commandid, num, param);
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "LOGIN", username, param);
 				
 				//trigger login event
 				HashMap<String, Object> context = buildContextParams(cs);
 				cs.triggerEvent(Trigger.TRIGGER_LOGIN, context);
 			}
 			else {
 				//login failed
 				data.setError(commandid, num, "ERROR IN LOGIN :: ACCESS DENIED");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "LOGIN", username, "ERROR IN LOGIN :: ACCESS DENIED");
 			}
 		} else {
 			//login failed
 			data.setError(commandid, num, "ERROR IN LOGIN :: MISSING USERNAME");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "LOGIN", username, "ERROR IN LOGIN :: MISSING USERNAME");
 		}
 	}
 	
 	public void handle_mkdir(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_MKDIR, commandid, num, "Sorry, no right to make directories.");
 		if (param!=null) {
 			File path = null;
 			if (param.startsWith("/")) {
 				path = new File(cs.getLocalRootPath()+param);
 				
 				if (!cs.isAllowed(path)) {
 					data.setError(commandid, num, "restricted path");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "MKDIR", param, "restricted path");
 				}
 				else if (path.exists()) {
 					data.setError(commandid, num, "path already exists");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "MKDIR", param, "path already exists");
 				}
 				else if (!cs.isAllowedDepthDir(path)) {
 					String msg = "max directory depth = "+cs.getRightsAndDuties().getMaxDirectoryDepth();
 					data.setError(commandid, num, msg);
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "MKDIR", param, msg);
 				}
 				else {
 					boolean ok = path.mkdirs();
 					if (ok) {
 						data.setAck(commandid, num);
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "MKDIR", param, "ACK");
 						//trigger mkdir event
 						HashMap<String, Object> context = buildContextParams(cs);
 						context.put(TriggerContext.RELATED_FILE,path);
 						context.put(TriggerContext.RELATED_FILENAME,path.getAbsolutePath());
 						cs.triggerEvent(Trigger.TRIGGER_MKDIR,context);
 					} else {
 						data.setError(commandid, num, "error in mkdir");
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "MKDIR", param, "error in mkdir");
 					}
 				}
 			} else {
 				data.setError(commandid, num, "path must be absolute");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "MKDIR", param, "path must be absolute");
 			}
 		} else {
 			data.setError(commandid, num, "missing path parameter");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "MKDIR", param, "missing path parameter");
 		}
 	}
 	
 	
 	public void handle_delete(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_DELETE, commandid, num, "Sorry, no right to delete files or directories.");
 		if (param!=null) {
 			File file = null;
 			if (param.startsWith("/")) {
 				file = new File(cs.getLocalRootPath()+param);
 				
 				if (!cs.isAllowed(file)) {
 					data.setError(commandid, num, "restricted file");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "DELETE", param, "restricted file");
 				}
 				else if (!file.exists()) {
 					data.setError(commandid, num, "file does not exist");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "DELETE", param, "file does not exist");
 				} else {
 					//delete
 					if(DEBUG) System.out.println("DELETING: "+file.getAbsolutePath());
 					if (file.isDirectory()) {
 						boolean ok = deleteDirectory(file);
 						if (ok) {
 							data.setAck(commandid, num);
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "DELETE", param,"ACK");
 							
 							//trigger delete event
 							HashMap<String, Object> context = buildContextParams(cs);
 							context.put(TriggerContext.RELATED_FILE,file);
 							context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 							cs.triggerEvent(Trigger.TRIGGER_DELETE, context);
 						} else {
 							data.setError(commandid, num, "directory \""+param+"\" could not be deleted");
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "DELETE", param, "directory \""+param+"\" could not be deleted");
 						}
 					} else {
 						boolean ok = file.delete();
 						if (ok) {
 							data.setAck(commandid, num);
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "DELETE", param, "ACK");
 							
 							//trigger delete event
 							HashMap<String, Object> context = buildContextParams(cs);
 							context.put(TriggerContext.RELATED_FILE,file);
 							context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 							cs.triggerEvent(Trigger.TRIGGER_DELETE, context);
 						} else {
 							data.setError(commandid, num, "file \""+param+"\" could not be deleted");
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "DELETE", param,"file \""+param+"\" could not be deleted");
 						}
 					}
 				}
 			}  else {
 				data.setError(commandid, num, "path must be absolute");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "DELETE", param, "path must be absolute");
 			}
 		} else {
 			data.setError(commandid, num, "missing path/file parameter");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "DELETE", param, "missing path/file parameter");
 		}
 	}
 	
 	private boolean deleteDirectory(File path) {
 		if( path.exists() ) {
 			File[] list = path.listFiles();
 			for(int i=0; i<list.length; i++) {
 				if(list[i].isDirectory()) {
 					deleteDirectory(list[i]);
 				}
 				else {
 					list[i].delete();
 				}
 			}
 		}
 		if (!path.equals(cs.getLocalRootPath())) {
 			return(path.delete());	
 		} else {
 			return true;
 		}
 	}
 	
 	public void handle_rename(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_RENAME, commandid, num, "Sorry, no right to rename files or directories.");
 		if (param!=null && param.length()>0) {
 			String[] p = Util.getParams(param);
 			if (p.length==2) {
 				if (p[1].contains("/") || p[1].contains("\\")) {
 					data.setError(commandid, num, "wrong destination filename");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "RENAME", param, "wrong destination filename" );
 				} else {
 					File file = null;
 					if (p[0].startsWith("/")) {
 						file = new File(cs.getLocalRootPath()+p[0]);
 						
 						if (!cs.isAllowed(file)) {
 							data.setError(commandid, num, "restricted file");
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "RENAME", param, "restricted file");
 						}
 						else if (!file.exists()) {
 							data.setError(commandid, num, "file does not exist");
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "RENAME", param, "file does not exist");
 						} else {
 							//init destination file
 							File dest = new File(file.getParentFile(),p[1]);
 							if (!cs.isAllowed(file)) {
 								data.setError(commandid, num, "restricted destination file");
 								data.sendPackage();
 								server.log.logCommand(clientID, addr, "RENAME", param, "restricted destination file");
 							}
 							else if (dest.exists()) {
 								data.setError(commandid, num, "destination file already exists");
 								data.sendPackage();
 								server.log.logCommand(clientID, addr, "RENAME", param, "destination file already exists");
 							}
 							else {
 								//rename
 								if(DEBUG) System.out.println("RENAMEING: "+file.getAbsolutePath()+" -> "+dest.getAbsolutePath());
 								
 								boolean ok = file.renameTo(dest);
 								if (ok) {
 									data.setAck(commandid, num);
 									data.sendPackage();
 									server.log.logCommand(clientID, addr, "RENAME", param, "ACK");
 									
 									//trigger rename event
 									HashMap<String, Object> context = buildContextParams(cs);
 									context.put(TriggerContext.RELATED_FILE,file);
 									context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 									cs.triggerEvent(Trigger.TRIGGER_RENAME, context);
 								} else {
 									data.setError(commandid, num, "file \""+p[0]+"\" could not be renamed to "+p[1]);
 									data.sendPackage();
 									server.log.logCommand(clientID, addr, "RENAME", param, "file \""+p[0]+"\" could not be renamed to "+p[1]);
 								}
 							}
 						}
 					}  else {
 						data.setError(commandid, num, "path must be absolute");
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RENAME", param, "path must be absolute");
 					}
 				}
 			} else {
 				data.setError(commandid, num, "missing destination filename parameter");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "RENAME", param, "missing destination filename parameter");
 			}
 		} else {
 			data.setError(commandid, num, "missing path/file parameter");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "RENAME", param,"missing path/file parameter" );
 		}
 	}
 	
 	public void handle_file(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_LIST, commandid, num, "Sorry, no right to read fileinfo.");
 		if (param!=null) {
 			
 			File file = null;
 			if (param.startsWith("/")) {
 				file = new File(cs.getLocalRootPath()+param);
 				
 				if (!cs.isAllowed(file)) {
 					data.setError(commandid, num, "restricted file");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "FILE", param, "restricted file");
 				}
 				else if (!file.exists()) {
 					data.setError(commandid, num, "file does not exist");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "FILE", param, "file does not exist" );
 				} else {
 					//ack -> send fileinfo
 					RemoteFile rf = cs.getAsRemoteFile(file);
 					if (rf!=null) {
 						data.setAck(commandid, num, rf.toParamString());
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "FILE", param, "ACK + file info");
 					} else {
 						data.setError(commandid, num, "error retrieving file information");
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "FILE", param, "error retrieving file information" );
 					}
 				}
 			}  else {
 				data.setError(commandid, num, "path must be absolute");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "FILE", param, "path must be absolute" );
 			}
 		} else {
 			data.setError(commandid, num, "missing path/file parameter");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "FILE", param, "missing path/file parameter"  );
 		}
 	}
 	
 	public void handle_list(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_LIST, commandid, num, "Sorry, no right to read directory info.");
 		if (param!=null) {
 			File file = null;
 			if (param.startsWith("/")) {
 				file = new File(cs.getLocalRootPath()+param);
 				if (!cs.isAllowed(file)) {
 					data.setError(commandid, num, "restricted file");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "LIST", param, "restricted file");
 				}
 				else if (!file.exists()) {
 					data.setError(commandid, num, "directory does not exist");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "LIST", param, "directory does not exist");
 				}
 				else if (!file.isDirectory()) {
 					data.setError(commandid, num, "this is not a directory");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "LIST", param, "this is not a directory");
 				}
 				else {
 					//ack -> send list
 					StringBuffer b = new StringBuffer();
 					
 					File[] list = file.listFiles();
 					for (int i=0;i<list.length;i++) {
 						RemoteFile rf = cs.getAsRemoteFile(list[i]);
 						if (rf!=null) {
 							b.append(rf.toParamString());
 						}
 					}
 					data.setAck(commandid, num, b.toString());
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "LIST", param, "ACK + list of files");
 				}
 			} else {
 				data.setError(commandid, num, "path must be absolute");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "LIST", param, "path must be absolute");
 			}
 		} else {
 			data.setError(commandid, num, "missing path parameter");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "LIST", param, "missing path parameter");
 		}
 	}
 	
 	
 	
 //	public void handle_put_stream(long commandid, int num, byte code, String param) throws Exception {
 //		ensureAllowed(RightsAndDuties.ALLOW_UPLOAD, commandid, num, "Sorry, no right to upload files.");
 //		if (param!=null) {
 //			String[] p = Util.getParams(param);
 //			File file = null;
 //			
 //			if (p[0].startsWith("/")) {
 //				file = new File(cs.getLocalRootPath()+p[0]);
 //				if (!cs.isAllowed(file)) {
 //					data.setError(commandid, num, "restricted file");
 //					data.sendPackage();
 //					server.log.logCommand(clientID, addr, "PUT_STREAM", param, "restricted file");
 //				}
 //				else if (file.exists()) {
 //					data.setError(commandid, num, "file already exists");
 //					data.sendPackage();
 //					server.log.logCommand(clientID, addr, "PUT_STREAM", param, "file already exists");
 //				}
 //				else if (!cs.isAllowedDepthFile(file)) {
 //					String msg = "max directory depth = "+cs.getRightsAndDuties().getMaxDirectoryDepth();
 //					data.setError(commandid, num, msg);
 //					data.sendPackage();
 //					server.log.logCommand(clientID, addr, "PUT_STREAM", param, msg);
 //				}
 //				else {
 //					//ensure directories exists
 //					file.getParentFile().mkdirs();
 //					
 //					//ack -> ready for upload
 //					data.setAck(commandid, num);
 //					data.sendPackage();
 //					server.log.logCommand(clientID, addr, "PUT_STREAM", param, "ACK");
 //					
 //					//trigger upload_start event
 //					
 //					HashMap<String, Object> context = buildContextParams(cs);
 //					context.put(TriggerContext.RELATED_FILE,file);
 //					context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 //					context.put(TriggerContext.RELATED_START_DATE,System.currentTimeMillis());
 //					cs.triggerEvent(Trigger.TRIGGER_UPLOAD_START, context);
 //					
 //					
 //					FileUploadInfo info = new FileUploadInfo();
 //					info.file = file;
 //					info.length = Long.MAX_VALUE;
 //					info.out = null;
 //					info.loaded = 0L;
 //					info.md5 = null;
 //					info.uploadStartDatetime = System.currentTimeMillis();
 //					info.fromStream = true;
 //					uploads.put(commandid,info);
 //					
 //				}
 //			} else {
 //				data.setError(commandid, num, "path must be absolute");
 //				data.sendPackage();
 //				server.log.logCommand(clientID, addr, "PUT_STREAM", param, "path must be absolute");
 //			}
 //		} else {
 //			data.setError(commandid, num, "missing path/file parameter");
 //			data.sendPackage();
 //			server.log.logCommand(clientID, addr, "PUT_STREAM", param, "missing path/file parameter");
 //		}
 //	}
 	
 	
 	/**
 	 * Command: PUT for file uploads
 	 * 
 	 * PUT [filename], [length], [MD5] <- OLD_STYLE_PUT
 	 * 
 	 * PUT [filename]
 	 * ..stream data packages..
 	 * PUT_EOF
 	 * 
 	 * Other control commands (use same commandid):
 	 * PUT_CANCEL  -> remove already uploaded part
 	 * PUT_BREAK   -> keep uploaded part (can be resumed)
 	 * 
 	 * 
 	 * PUT_RESUME [filename] -> resume previous upload
 	 * 
 	 */
 	public void handle_put(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_UPLOAD, commandid, num, "Sorry, no right to upload files.");
 		if (param!=null) {
 			String[] p = Util.getParams(param);
 			if (p.length<=0) {
 				data.setError(commandid, num, "missing path/file parameter");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "PUT", param, "missing path/file parameter");
 				return;
 			}
 			
 			boolean oldstylePUT = p.length>1;
 			File file = null;
 			long length = -1L;
 			
 			if (oldstylePUT) {
 				try {
 					length = Long.parseLong(p[1]);
 				} catch (Exception ex) {}
 				if (length<0) {
 					data.setError(commandid, num, "missing or wrong file length parameter");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "PUT", param, "missing or wrong file length parameter");
 					return;
 				}
 			}
 			
 			if (p[0].startsWith("/")) {
 				file = new File(cs.getLocalRootPath()+p[0]);
 				if (!cs.isAllowed(file)) {
 					data.setError(commandid, num, "restricted file");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "PUT", param, "restricted file");
 				}
 				else if (file.exists()) {
 					data.setError(commandid, num, "file already exists");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "PUT", param, "file already exists");
 				}
 				else if (!cs.isAllowedDepthFile(file)) {
 					String msg = "max directory depth = "+cs.getRightsAndDuties().getMaxDirectoryDepth();
 					data.setError(commandid, num, msg);
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "PUT", param, msg);
 				}
 				else {
 					//ensure directories exists
 					file.getParentFile().mkdirs();
 					
 					//ack -> ready for upload
 					data.setAck(commandid, num);
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "PUT", param, "ACK");
 					
 					//trigger upload_start event
 					long now = System.currentTimeMillis();
 					HashMap<String, Object> context = buildContextParams(cs);
 					context.put(TriggerContext.RELATED_FILE,file);
 					context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 					context.put(TriggerContext.RELATED_START_DATE,now);
 					cs.triggerEvent(Trigger.TRIGGER_UPLOAD_START, context);
 					
 					if (length==0) {
 						file.createNewFile();
 						context = buildContextParams(cs);
 						cs.triggerEvent(Trigger.TRIGGER_UPLOAD_END, context);
 					} else {
 						FileUploadInfo info = new FileUploadInfo();
 						info.file = file;
 						info.out = null;
 						info.loaded = 0L;
 						info.md5 = new MD5();
 						info.canCancel = true;
 						
 						if (oldstylePUT) {
 							info.length = length;
 							if (p.length>2) {
 								info.md5String = p[2];
 							} else {
 								info.md5String = null;
 							}
 						} else {
 							info.length = Long.MAX_VALUE;
 							info.md5String = null;
 							info.fromStream = true;
 						}
 						info.uploadStartDatetime = now;						
 						uploads.put(commandid,info);
 					}
 				}
 			} else {
 				data.setError(commandid, num, "path must be absolute");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "PUT", param, "path must be absolute");
 			}
 		}
 		
 	}
 	
 	public void handle_put_eof(long commandid, int num, byte code, String param) throws Exception {
 		FileUploadInfo upload = uploads.get(commandid);
 		if (upload!=null) {
 			upload.canCancel = false;
 			
 			//file length
 			String[] p = Util.getParams(param);	//0 = lenght, 1 = MD5
 			long length = 0L;
 			try {
 				length = Long.parseLong(p[0]);
 			} catch (Exception ex) {
 				data.setError(commandid, num, "missing or wrong file length parameter");
 				server.log.logCommand(clientID, addr, "PUT_EOF", param, "missing or wrong file length parameter");
 				data.sendPackage();
 				return;
 			}
 			
 			if (upload.out!=null) {
 				upload.out.close();
 			}
 				
 			if (length==0) {
 				//empty file (0 bytes)
 				upload.file.createNewFile();
 				data.setAckComplete(commandid, num);
 				data.sendPackage();
 				return;				
 			}
 			if (length>0) {	
 				if (upload.file.length()!=length) {
 					data.setError(commandid, num, "wrong filesize :: is="+upload.file.length()+", given="+length);
 					server.log.logCommand(clientID, addr, "PUT_EOF", param, "wrong filesize :: is="+upload.file.length()+", given="+length);
 					data.sendPackage();
 					return;
 				}
 				
 				//md5
 				upload.md5String = null;
 				if (p.length>=2) {
 					upload.md5String = p[1];
 				}
 				
 				boolean ok = true;
 				if (upload.md5String!=null) {
 					//check md5
 					try {
 						byte[] my_md5 = upload.md5.getMD5bytes();
 						byte[] your_md5 = SecurityHelper.HexDecoder.decode(upload.md5String);
 						if (!Arrays.equals(my_md5,your_md5)) {
 							ok = false;
 							System.out.println("MD5 check failed for "+upload.file.getAbsolutePath());
 							server.log.logCommand(clientID, addr, "PUT_EOF", param, "MD5 check failed for "+upload.file.getAbsolutePath());
 						}
 //						else {
 //							System.out.println("MD5 check ok for "+upload.file.getAbsolutePath()+" :: "+upload.md5String);
 //						}
 					} catch (Exception ex) {
 						ok = false;
 					}
 					if (!ok) {
 						data.setError(commandid, num, "md5 check failed");
 						data.sendPackage();
 					}
 				}
 				if (ok) {
 					data.setAckComplete(commandid, num);
 					data.sendPackage();
 				}
 			}
 		} else {
 			data.setError(commandid, num, "PUT_EOF :: no matching open stream id = "+commandid);
 			server.log.logCommand(clientID, addr, "PUT_EOF", param,  "no matching open stream id = "+commandid);
 			data.sendPackage();
 		}
 	}
 	
 	public void handle_put_cancel(long commandid, int num, byte code, String param) throws Exception {
 		FileUploadInfo upload = uploads.get(commandid);
 		if (upload!=null) {
 			if (upload.canCancel) {
 				if (upload.out!=null) {
 					upload.out.close();
 					upload.out = null;
 				}
 				if (upload.file.exists()) {
 					upload.file.delete();
 				}
 				data.setAck(commandid, num);
 				data.sendPackage();
 			} else {
 				data.setError(commandid, num, "PUT_CANCEL :: canceling upload not possible");
 				data.sendPackage();
 			}
 		} else {
 			data.setError(commandid, num, "PUT_CANCEL :: no matching open stream id = "+commandid);
 			data.sendPackage();
 		}
 	}
 	
 	public void handle_put_break(long commandid, int num, byte code, String param) throws Exception {
 		FileUploadInfo upload = uploads.get(commandid);
 		if (upload!=null) {
 			if (upload.canCancel) {
 				if (upload.out!=null) {
 					upload.out.close();
 					upload.out = null;
 				}
 				data.setAck(commandid, num);
 				data.sendPackage();
 			} else {
 				data.setError(commandid, num, "PUT_BREAK :: halting upload not possible");
 				data.sendPackage();
 			}
 		} else {
 			data.setError(commandid, num, "PUT_BREAK :: no matching open stream id = "+commandid);
 			data.sendPackage();
 		}
 	}
 	
 	//TODO
 	public void handle_put_resume(long commandid, int num, byte code, String param) throws Exception {
 		//if file exists && length > 0
 		//   -> ACK file.length and MD5 so far
 		//else
 		//	 -> ACK 0
 		
 		ensureAllowed(RightsAndDuties.ALLOW_UPLOAD, commandid, num, "Sorry, no right to upload files.");
 		if (param!=null) {
 			String[] p = Util.getParams(param);
 			if (p.length<=0) {
 				data.setError(commandid, num, "missing path/file parameter");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "PUT_RESUME", param, "missing path/file parameter");
 				return;
 			}
 			
 			File file = null;
 			
 			if (p[0].startsWith("/")) {
 				file = new File(cs.getLocalRootPath()+p[0]);
 				if (!cs.isAllowed(file)) {
 					data.setError(commandid, num, "restricted file");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "PUT_RESUME", param, "restricted file");
 				}
 				else if (!cs.isAllowedDepthFile(file)) {
 					String msg = "max directory depth = "+cs.getRightsAndDuties().getMaxDirectoryDepth();
 					data.setError(commandid, num, msg);
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "PUT_RESUME", param, msg);
 				}
 				else {
 					//ensure directories exists
 					file.getParentFile().mkdirs();
 					MD5 md5Local = new MD5(); //can only read md5 sum once!!! -> local copy
 					MD5 md5 = new MD5();
 					//ack -> ready for resume upload at length
 					if (file.exists()) {
 						long length = file.length();
 						FileInputStream in = new FileInputStream(file);
 						int read = 0;
 						byte[] buffer = new byte[16*1024];
 						while ((read = in.read(buffer))>=0) {
 							if (read>0) {
 								md5Local.update(buffer, read);
 								md5.update(buffer, read);
 							}
 						}
 						data.setAck(commandid, num, Util.makeParamsString(new String[] {""+length,md5Local.getMD5HexString()}));
 						in.close();
 					}
 					else {
 						data.setAck(commandid, num, Util.makeParamsString(new String[] {"0"}));
 					}
 					data.sendPackage();
 					
 					server.log.logCommand(clientID, addr, "PUT", param, "ACK");
 					
 					//trigger upload_start event
 					long now = System.currentTimeMillis();
 					HashMap<String, Object> context = buildContextParams(cs);
 					context.put(TriggerContext.RELATED_FILE,file);
 					context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 					context.put(TriggerContext.RELATED_START_DATE,now);
 					cs.triggerEvent(Trigger.TRIGGER_UPLOAD_START, context);
 					
 					
 					FileUploadInfo info = new FileUploadInfo();
 					info.file = file;
 					info.out = null;
 					info.loaded = file.length();
 					info.md5 = md5;
 					info.canCancel = true;
 					info.length = Long.MAX_VALUE;
 					info.md5String = null;
 					info.fromStream = true;
 					info.uploadStartDatetime = now;			
 					uploads.put(commandid,info);
 					
 				}
 			} else {
 				data.setError(commandid, num, "path must be absolute");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "PUT", param, "path must be absolute");
 			}
 		}
 	}
 	
 	
 //	public void handle_putpart(long commandid, int num, byte code, String param) throws Exception {
 //		ensureAllowed(RightsAndDuties.ALLOW_UPLOAD, commandid, num, "Sorry, no right to upload files.");
 //		if (param!=null) {
 //			String[] params = Util.getParams(param);
 //			String filename = null;
 //			long start = -1;
 //			int length = -1;
 //
 //			try {
 //				filename = params[0];
 //				start = Long.parseLong(params[1]);
 //				length = Integer.parseInt(params[2]);
 //			} catch (Exception ex) {
 //				String resp = "ERROR IN PUTPART :: WRONG FORMAT";
 //				//log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 //				//sender.sendEncryptedText(resp);
 //				data.setError(commandid, num, "wrong format");
 //				data.sendPackage();	
 //			}
 //			File f = state.getWriteFile();
 //			if (f==null || !filename.equals(f.getName())) {
 //				String resp = "ERROR IN PUTPART :: PLEASE SEND PUT OR RESUMEPUT COMMAND FIRST";
 //				log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 //				sender.sendEncryptedText(resp);
 //			}
 //
 //			if (f.exists()) {
 //				if (f.length()==start) {
 //					//append
 //					state.setNextFilePartStart(start);
 //					state.setNextFilePartLength(length);
 //					String resp = "ACK PUTPART :: WAITING FOR DATA";
 //					log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 //					sender.sendEncryptedText(resp);
 //				} else {
 //					String resp = "ERROR IN PUTPART :: WRONG START POSITION, EXPECTED POS="+f.length();
 //					log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 //					sender.sendEncryptedText(resp);
 //				}
 //			} else {
 //				state.setNextFilePartStart(start);
 //				state.setNextFilePartLength(length);
 //				String resp = "ACK PUTPART :: WAITING FOR DATA";
 //				log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 //				sender.sendEncryptedText(resp);
 //			}
 //		}
 //	}
 
 	public void handle_resumeput(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_UPLOAD, commandid, num, "Sorry, no right to upload files.");
 		if (param!=null) {
 			//RESUMEPUT "filename",length
 			String[] p = Util.getParams(param);
 			File file = null;
 			long length = -1L;
 			try {
 				length = Long.parseLong(p[1]);
 			} catch (Exception ex) {}
 			if (length<0) {
 				data.setError(commandid, num, "missing or wrong file length parameter");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "RESUMEPUT", param, "missing or wrong file length parameter");
 			} else {
 				if (p[0].startsWith("/")) {
 					file = new File(cs.getLocalRootPath()+p[0]);
 					if (!cs.isAllowed(file)) {
 						data.setError(commandid, num, "restricted file");
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RESUMEPUT", param, "restricted file");
 					}
 					else if (!cs.isAllowedDepthFile(file)) {
 						String msg = "max directory depth = "+cs.getRightsAndDuties().getMaxDirectoryDepth();
 						data.setError(commandid, num, msg);
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RESUMEPUT", param, msg);
 					}
 					else if (file.exists()) {
 						long loaded = file.length();
 						if (loaded>=length) {
 							data.setAck(commandid, num, "upload already complete");
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "RESUMEPUT", param, "upload already complete");
 						} else {
 							//ack -> ready for upload
 							data.setAck(commandid, num,""+loaded); //Startpos = file.length
 							data.sendPackage();
 							server.log.logCommand(clientID, addr, "RESUMEPUT", param, "ACK");
 							
 							//trigger upload_start event
 							HashMap<String, Object> context = buildContextParams(cs);
 							context.put(TriggerContext.RELATED_FILE,file);
 							context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 							context.put(TriggerContext.RELATED_START_DATE, System.currentTimeMillis());
 							cs.triggerEvent(Trigger.TRIGGER_UPLOAD_START, context);
 							
 							if (length==0) {
 								file.createNewFile();
 								context = buildContextParams(cs);
 								cs.triggerEvent(Trigger.TRIGGER_UPLOAD_END, context);
 							} else {
 								FileUploadInfo info = new FileUploadInfo();
 								info.file = file;
 								info.length = length;
 								info.out = null;
 								info.loaded = loaded;
 								if (p.length>2) {
 									info.md5String = p[2];
 								} else {
 									info.md5String = null;
 								}
 								info.uploadStartDatetime = System.currentTimeMillis();
 								uploads.put(commandid,info);
 							}
 						}
 					} else {
 						//file does not exists -> normal PUT
 						//ensure directories exists
 						file.getParentFile().mkdirs();
 						
 						//ack -> ready for upload
 						data.setAck(commandid, num,"0"); //Startpos 0
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RESUMEPUT", param, "ACK");
 						
 						//trigger upload_start event
 						HashMap<String, Object> context = buildContextParams(cs);
 						context.put(TriggerContext.RELATED_FILE,file);
 						context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 						context.put(TriggerContext.RELATED_START_DATE, System.currentTimeMillis());
 						cs.triggerEvent(Trigger.TRIGGER_UPLOAD_START, context);
 						
 						if (length==0) {
 							file.createNewFile();
 							context = buildContextParams(cs);
 							cs.triggerEvent(Trigger.TRIGGER_UPLOAD_END, context);
 						} else {
 							FileUploadInfo info = new FileUploadInfo();
 							info.file = file;
 							info.length = length;
 							info.out = null;
 							info.loaded = 0L;
 							if (p.length>2) {
 								info.md5String = p[2];
 							} else {
 								info.md5String = null;
 							}
 							info.uploadStartDatetime = System.currentTimeMillis();
 							uploads.put(commandid,info);
 						}
 					}
 				} else {
 					data.setError(commandid, num, "path must be absolute");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "RESUMEPUT", param, "path must be absolute");
 				}
 			}
 		}
 	}
 	
 	public void handle_get(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_DOWNLOAD, commandid, num, "Sorry, no right to download files.");
 		if (param!=null) {
 			
 			File file = null;
 			if (param.startsWith("/")) {
 				file = new File(cs.getLocalRootPath()+param);
 				
 				if (!cs.isAllowed(file)) {
 					data.setError(commandid, num, "restricted file");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "GET", param, "restricted file");
 				}
 				else if (!file.exists()) {
 					data.setError(commandid, num, "file does not exist");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "GET", param, "file does not exist");
 				}
 				else if (file.isDirectory()) {
 					data.setError(commandid, num, "directory download not implemented");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "GET", param, "directory download not implemented");
 				} else {
 					//send ack with file length and md5
 					String md5 = null;
 					if (file.length()<=maxFilelengthForMD5) {
 						try {
 							md5 = SecurityHelper.HexDecoder.encode(SecurityHelper.getMD5(file));
 						} catch (Exception ex) {
 							if(DEBUG) System.out.println("Error calculating md5 hash of "+file.getAbsolutePath());
 							ex.printStackTrace();
 							md5 = null;
 						}
 					}
 					String[] response;
 					if (md5==null) {
 						response = new String[]{""+file.length()};
 					} else {
 						response = new String[]{""+file.length(),md5};
 					}
 					data.setAck(commandid, num, Util.makeParamsString(response));
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "GET", param, "ACK + filelength + md5");
 					
 					//trigger download_start event
 					HashMap<String, Object> context = buildContextParams(cs);
 					context.put(TriggerContext.RELATED_FILE,file);
 					context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 					context.put(TriggerContext.RELATED_START_DATE, System.currentTimeMillis());
 					cs.triggerEvent(Trigger.TRIGGER_DOWNLOAD_START, context);
 					
 					
 					//send file data
 					FileInputStream fileIn = new FileInputStream(file);
 					int read = 0;
 					byte[] content = new byte[maxPacketSize];
 					while ((read = fileIn.read(content))>0) {
 						num++;
 						if (read<maxPacketSize) {
 							data.setData(commandid, num, Arrays.copyOf(content, read));
 						} else {
 							data.setData(commandid, num, content);
 						}
 						data.sendPackage();
 						//TODO it would be a good idea to check for error or cancel messages
 						//     when sending large files
 					}
 					
 					//trigger download_end event
 					context.put(TriggerContext.RELATED_END_DATE,System.currentTimeMillis());
 					cs.triggerEvent(Trigger.TRIGGER_DOWNLOAD_END, context);
 				}
 			} else {
 				data.setError(commandid, num, "path must be absolute");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "GET", param, "path must be absolute");
 			}
 		} else {
 			data.setError(commandid, num, "missing path/file parameter");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "GET", param, "missing path/file parameter");
 		}
 	}
 	
 	public void handle_resumeget(long commandid, int num, byte code, String param) throws Exception {
 		ensureAllowed(RightsAndDuties.ALLOW_DOWNLOAD, commandid, num, "Sorry, no right to download files.");
 		if (param!=null) {
 			//RESUMEPUT "filename",length
 			String[] p = Util.getParams(param);
 			File file = null;
 			long length = -1L;
 			try {
 				length = Long.parseLong(p[1]);
 			} catch (Exception ex) {}
 			if (length<0) {
 				data.setError(commandid, num, "missing or wrong file length parameter");
 				data.sendPackage();
 				server.log.logCommand(clientID, addr, "RESUMEGET", param, "missing or wrong file length parameter");
 			} else {
 				if (p[0].startsWith("/")) {
 					file = new File(cs.getLocalRootPath()+p[0]);
 					
 					if (!cs.isAllowed(file)) {
 						data.setError(commandid, num, "restricted file");
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RESUMEGET", param, "restricted file");
 					}
 					else if (!file.exists()) {
 						data.setError(commandid, num, "file does not exist");
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RESUMEGET", param, "file does not exist");
 					}
 					else if (file.isDirectory()) {
 						data.setError(commandid, num, "directory download not implemented");
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RESUMEGET", param, "directory download not implemented");
 					} else {
 						//send ack with file length and md5
 						String md5 = null;
 						if (file.length()<=maxFilelengthForMD5) {
 							try {
 								md5 = SecurityHelper.HexDecoder.encode(SecurityHelper.getMD5(file));
 							} catch (Exception ex) {
 								if(DEBUG) System.out.println("Error calculating md5 hash of "+file.getAbsolutePath());
 								ex.printStackTrace();
 								md5 = null;
 							}
 						}
 						String[] response;
 						if (md5==null) {
 							response = new String[]{""+file.length()};
 						} else {
 							response = new String[]{""+file.length(),md5};
 						}
 						data.setAck(commandid, num, Util.makeParamsString(response));
 						data.sendPackage();
 						server.log.logCommand(clientID, addr, "RESUMEGET", param, "ACK + filelength + md5");
 						
 						//trigger download_start event
 						HashMap<String, Object> context = buildContextParams(cs);
 						context.put(TriggerContext.RELATED_FILE,file);
 						context.put(TriggerContext.RELATED_FILENAME,file.getAbsolutePath());
 						context.put(TriggerContext.RELATED_START_DATE, System.currentTimeMillis());
 						cs.triggerEvent(Trigger.TRIGGER_DOWNLOAD_START, context);
 						
 						//send file data, starting at position "length"
 						FileInputStream fileIn = new FileInputStream(file);
 						fileIn.skip(length);
 						int read = 0;
 						byte[] content = new byte[maxPacketSize];
 						while ((read = fileIn.read(content))>0) {
 							num++;
 							if (read<maxPacketSize) {
 								data.setData(commandid, num, Arrays.copyOf(content, read));
 							} else {
 								data.setData(commandid, num, content);
 							}
 							data.sendPackage();
 							
 							//TODO it would be a good idea to check for error or cancel messages
 							//     when sending large files
 						}
 						
 						//trigger download_end event
 						context.put(TriggerContext.RELATED_END_DATE, System.currentTimeMillis());
 						cs.triggerEvent(Trigger.TRIGGER_DOWNLOAD_END, context);
 					}
 				} else {
 					data.setError(commandid, num, "path must be absolute");
 					data.sendPackage();
 					server.log.logCommand(clientID, addr, "RESUMEGET", param, "path must be absolute");
 				}
 			}
 		} else {
 			data.setError(commandid, num, "missing path/file parameter");
 			data.sendPackage();
 			server.log.logCommand(clientID, addr, "RESUMEGET", param,"missing path/file parameter" );
 		}
 	}
 	
 	public void handle_quit(long commandid, int num, byte code, String param) throws Exception {
 		//do nothing... socket closes automatically on client disconnection
 		server.log.logCommand(clientID, addr, "QUIT", param, "");
 		
 		//trigger logout event
 		HashMap<String, Object> context = buildContextParamsWithDate(cs);
 		cs.triggerEvent(Trigger.TRIGGER_LOGOUT, context);
 	}
 	
 	
 	private boolean ensureAllowed(int rightType, long id, int num, String msg) throws Exception {
 		if (cs==null || cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().hasRight(rightType)) {
 			data.setError(id, num, msg);
 			data.sendPackage();
 			return false;
 		}
 		return true;
 	}
 
 	private class FileUploadInfo {
 		public File file = null;
 		public long length = -1L;
 		public long loaded = 0L;
 		public FileOutputStream out = null;
 		public String md5String = null;
 		public MD5 md5 = null;
 		public long uploadStartDatetime = -1L;
 		public boolean fromStream = false;
 		public boolean canCancel = false;
 		
 	}
 }
