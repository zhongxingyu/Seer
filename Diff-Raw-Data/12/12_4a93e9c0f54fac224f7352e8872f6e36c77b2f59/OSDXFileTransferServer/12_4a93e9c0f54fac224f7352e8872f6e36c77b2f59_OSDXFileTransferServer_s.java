 package org.fnppl.opensdx.file_transfer;
 
 /*
  * Copyright (C) 2010-2011 
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
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.Vector;
 
 import org.fnppl.opensdx.common.Util;
 import org.fnppl.opensdx.securesocket.ClientSettings;
 import org.fnppl.opensdx.securesocket.OSDXSocketDataHandler;
 import org.fnppl.opensdx.securesocket.OSDXSocketSender;
 import org.fnppl.opensdx.securesocket.OSDXSocketServer;
 import org.fnppl.opensdx.securesocket.OSDXSocketServerThread;
 import org.fnppl.opensdx.security.MasterKey;
 import org.fnppl.opensdx.security.OSDXKey;
 import org.fnppl.opensdx.security.SecurityHelper;
 import org.fnppl.opensdx.security.Signature;
 import org.fnppl.opensdx.xml.Document;
 import org.fnppl.opensdx.xml.Element;
 
 public class OSDXFileTransferServer implements OSDXSocketDataHandler {
 
 	private OSDXSocketServer serverSocket;
 	private FileTransferLog log = null;
 	
 	private File configFile = new File("osdxserver_config.xml"); 
 	private File alterConfigFile = new File("src/org/fnppl/opensdx/file_transfer/resources/osdxfiletransferserver_config.xml"); 
 
 	protected int port = -1;
 	
 	protected InetAddress address = null;
 
 	private OSDXKey mySigningKey = null;
 	
 	private HashMap<OSDXSocketServerThread, FileTransferState> states = null;
 	private HashMap<String, ClientSettings> clients = null; //client id := username::keyid
 	private int maxByteLength = 4*1024*1024;
 	
 	public OSDXFileTransferServer(String pwSigning) {
 		readConfig();
 		try {
 			mySigningKey.unlockPrivateKey(pwSigning);
 			serverSocket = new OSDXSocketServer(port, mySigningKey);
 			serverSocket.setDataHandler(this);
 			states = new HashMap<OSDXSocketServerThread, FileTransferState>();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void handleNewText(String text, OSDXSocketSender sender) {
 		OSDXSocketServerThread serverThread = (OSDXSocketServerThread)sender;
 		
 		String command = text;
 		String param = null;
 		int ind = text.indexOf(' ');
 		if (ind>0) {
 			command = text.substring(0,ind);
 			if (text.length()>ind+1) {
 				param = text.substring(ind+1);
 			}
 		}
 		command = command.toLowerCase();
 		try {
 			Method commandHandler = getClass().getMethod("handle_"+command, String.class, OSDXSocketServerThread.class);
 			commandHandler.invoke(this, param, sender);
 			
 		} catch (NoSuchMethodException ex) {
 			handle_command_not_implemented(command, param, serverThread);
 		} catch (InvocationTargetException ex) {
 			handle_command_not_implemented(command, param, serverThread);
 		} catch (IllegalAccessException ex) {
 			handle_command_not_implemented(command, param, serverThread);
 		}
 		
 	}
 
 	private FileTransferState getState(OSDXSocketServerThread serverThread) {
 		FileTransferState s = states.get(serverThread);
 		if (s==null) {
 			s = new FileTransferState();
 			//System.out.println("looking for client key id: "+sender.getID());
 			ClientSettings settings = clients.get(serverThread.getID());
 			if (settings == null) {
 				return null;
 			}
 			s.setRootPath(settings.getLocalRootPath());
 			//s.setRootPath(new File(path_uploaded_files,sender.getID()));
 			states.put(serverThread,s);
 		}
 		return s;
 	}
 	
 	public void handleNewData(byte[] data, OSDXSocketSender sender) {
 		//if data arrives, it must be a file
 		FileTransferState state = getState((OSDXSocketServerThread)sender);
 		if (state.getWriteFile() == null) {
 			sender.sendEncryptedText("ERROR IN DATASTREAM :: PLEASE SEND PUT REQUEST FIRST");
 		}
 		else {
 			try {
 				File save = state.getWriteFile();
 				
 				if (state.getNextFilePartStart()<0) {
 					//one part upload
 					System.out.println("Saving to file: "+save.getAbsolutePath());
 					FileOutputStream fout = new FileOutputStream(save);
 					fout.write(data);
 					state.setWriteFile(null);
 					fout.close();
 					sender.sendEncryptedText("ACK FILE UPLOAD");
 					log.logFiledataUpload(((OSDXSocketServerThread)sender).getID(), ((OSDXSocketServerThread)sender).getRemoteIP(), save.getAbsolutePath(), 0, (int)save.length());
 				} else {
 					//multiple part upload or resume
 					if (save.length()!=state.getNextFilePartStart()) {
 						sender.sendEncryptedText("ERROR WRITING TO FILE :: WRONG FILEPART START");
 					}
 					FileOutputStream fout = new FileOutputStream(save,true);
 					fout.write(data);
 					fout.close();
 					sender.sendEncryptedText("ACK FILEPART UPLOAD");
 					log.logFiledataUpload(((OSDXSocketServerThread)sender).getID(), ((OSDXSocketServerThread)sender).getRemoteIP(), save.getAbsolutePath(), state.getNextFilePartStart(), data.length);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				sender.sendEncryptedText("ERROR WRITING TO FILE");
 			}
 		}
 	}
 	
 	public void startService() {
 		System.out.println("Starting Server at "+address.getHostAddress()+" on port " + port +"  at "+SecurityHelper.getFormattedDate(System.currentTimeMillis()));
 		log.logServerStart(address.getHostAddress(), port);
 		//System.out.println("directory for uploaded files : "+path_uploaded_files.getAbsolutePath());
 		try {
 			serverSocket.startService();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void readConfig() {
 		try {
 			if (!configFile.exists()) {
 				configFile = alterConfigFile;
 			}
 			if (!configFile.exists()) {
 				System.out.println("Sorry, uploadserver_config.xml not found.");
 				System.exit(0);
 			}
 			Element root = Document.fromFile(configFile).getRootElement();
 			
 			//uploadserver base
 			Element ks = root.getChild("osdxfiletransferserver");
 //			host = ks.getChildText("host");
 			port = ks.getChildInt("port");
 			String ip4 = ks.getChildText("ipv4");
 			try {
 				byte[] addr = new byte[4];
 				String[] sa = ip4.split("[.]");
 				for (int i=0;i<4;i++) {
 					int b = Integer.parseInt(sa[i]);
 					if (b>127) b = -256+b;
 					addr[i] = (byte)b;
 				}
 				address = InetAddress.getByAddress(addr);
 			} catch (Exception ex) {
 				System.out.println("CAUTION: error while parsing ip adress");
 				ex.printStackTrace();
 			}
 			
 			String logFile = ks.getChildText("logfile");
 			if (logFile==null) {
 				log = FileTransferLog.initNoLogging();
 			} else {
 				log = FileTransferLog.initLog(new File(logFile));
 			}
 			
 			///Clients
 			clients = new HashMap<String, ClientSettings>();
 			//System.out.println("init clients");
 			Element eClients = root.getChild("clients");
 			Vector<Element> ecClients = eClients.getChildren("client");
 			for (Element e : ecClients) {
 				try {
 					ClientSettings cs = ClientSettings.fromElement(e);
 					clients.put(cs.getSettingsID(),cs);
 					System.out.println("adding client: "+cs.getSettingsID()+" -> "+cs.getLocalRootPath().getAbsolutePath());
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 				
 	
 			//SigningKey
 			try {
 				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
 				if (k instanceof MasterKey) {
 					mySigningKey = (MasterKey)k;					
 				} else {
 					System.out.println("ERROR: no master signing key in config.");	
 				}
 			} catch (Exception e) {
 				System.out.println("ERROR: no master signing key in config."); 
 			}
 			//TODO check localproofs and signatures 
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	private static void makeConfig() {
 //		Console console = System.console();
 //	    if (console == null) {
 //	      return;
 //	    }
 //	    String host = console.readLine("host: ");
 //	    String port = console.readLine("port: ");
 //	    String prepath = console.readLine("prepath: ");
 //	    String ipv4 = console.readLine("ipv4: ");
 //	    String ipv6 = console.readLine("ipv6: ");
 //	    String mail_user = console.readLine("mail user: ");
 //	    String mail_sender = console.readLine("mail sender: ");
 //	    String mail_smtp_host = console.readLine("mail smtp host: ");
 //	    String id_email = console.readLine("id email: ");
 //	    String id_mnemonic = console.readLine("id mnemonic: ");
 //	    String pass = console.readLine("key password: ");
 //	    
 //	    Element root = new Element("opensdxkeyserver");
 //	    Element eKeyServer = new Element("keyserver");
 //	    eKeyServer.addContent("port", port);
 //	    eKeyServer.addContent("prepath",prepath);
 //	    eKeyServer.addContent("ipv4",ipv4);
 //	    eKeyServer.addContent("ipv6",ipv6);
 //	    Element eMail = new Element("mail");
 //	    eMail.addContent("user", mail_user);
 //	    eMail.addContent("sender", mail_sender);
 //	    eMail.addContent("smtp_host", mail_smtp_host);
 //	    eKeyServer.addContent(eMail);
 //	    root.addContent(eKeyServer);
 //	    
 //	    try {
 //	    	Element eSig = new Element("rootsigningkey");
 //	    	MasterKey key = MasterKey.buildNewMasterKeyfromKeyPair(AsymmetricKeyPair.generateAsymmetricKeyPair());
 //			Identity id = Identity.newEmptyIdentity();
 //			id.setIdentNum(1);
 //			id.setEmail(id_email);
 //			id.setMnemonic(id_mnemonic);
 //			key.addIdentity(id);
 //			key.setAuthoritativeKeyServer(host);
 //			key.createLockedPrivateKey("", pass);
 //			eSig.addContent(key.toElement(null));
 //			root.addContent(eSig);
 //			Document.buildDocument(root).writeToFile(new File("keyserver_config.xml"));
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 	}
 	
 	
 	
 	public static void main(String[] args) throws Exception {
 		if (args!=null && args.length==1 && args[0].equals("--makeconfig")) {
 			makeConfig();
 			return;
 		}
 		
		//debug
		String pwS = "upload";
 		
 		OSDXFileTransferServer s = new OSDXFileTransferServer(pwS);
 		s.startService();
 	}
 	
 	public void handle_command_not_implemented(String command, String param, OSDXSocketServerThread sender) {
 		
 		log.logCommand(sender.getID(),sender.getRemoteIP(), command, param, "COMMAND \""+command+"\" NOT IMPLEMENTED");
 		sender.sendEncryptedText("COMMAND \""+command+"\" NOT IMPLEMENTED");
 	}
 	
 	
 	// -- implementation of commands starts here --------------------------------------------
 	
 
 	
 	public void handle_login(String username, OSDXSocketServerThread sender) {
 		if (sender instanceof OSDXSocketServerThread) {
 			OSDXSocketServerThread sst = (OSDXSocketServerThread)sender;
 			if (username!=null) {
 				String userid = username+"::"+sst.getClientKeyID();
 				ClientSettings cs  = clients.get(userid);
 				if (cs!=null) {
 					sender.setID(userid);
 				}
 			}
 		}
 		if (sender.getID().equals("unknown_user")) {
 			String resp = "ERROR IN LOGIN :: ACCESS DENIED";
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "LOGIN", username, resp);
 			sender.sendEncryptedText(resp);
 		} else {
 			//respond with username and rights and duties
 			String param = Util.makeParamsString(new String[]{sender.getID(),Document.buildDocument(clients.get(sender.getID()).getRightsAndDuties().toElement()).toStringCompact()});
 			String resp = "ACK LOGIN :: "+param;
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "LOGIN", username, resp);
 			sender.sendEncryptedText(resp);
 		}
 	}
 	
 	
 	//echo command for testing
 	public void handle_echo(String param, OSDXSocketServerThread sender) {
 		if (param!=null) {
 			System.out.println("ECHO::"+param);
 			sender.sendEncryptedText(param);
 		}
 	}
 	
 	//change working directory: CWD directory_name
 	public void handle_cd(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			sender.sendEncryptedText("ERROR IN CD :: PLEASE LOGIN");
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "CD", param, "ERROR IN CD :: PLEASE LOGIN");
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsCD()) {
 			sender.sendEncryptedText("ERROR IN CD :: NOT ALLOWED");
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "CD", param, "ERROR IN CD :: NOT ALLOWED");
 			return;
 		}
 		if (param!=null) {
 			FileTransferState state = getState(sender);
 			File path = null; 
 			if (param.startsWith("/")) {
 				path = new File(state.getRootPath(),param.substring(1));
 			} else {
 				path = new File(state.getCurrentPath(),param);
 			}
 			if (path.exists()) {
 				state.setCurrentPath(path);
 				String resp = "ACK CD :: "+state.getRelativPath();
 				log.logCommand(sender.getID(),sender.getRemoteIP(), "CD", param, resp);
 				sender.sendEncryptedText(resp);
 			} else {
 				String resp = "ERROR IN CD :: PWD is "+state.getRelativPath();
 				log.logCommand(sender.getID(),sender.getRemoteIP(), "CD", param, resp);
 				sender.sendEncryptedText(resp);
 			}
 		}
 	}
 	
 	public void handle_mkdir(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			sender.sendEncryptedText("ERROR IN MKDIR :: PLEASE LOGIN");
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsMkdir()) {
 			sender.sendEncryptedText("ERROR IN MKDIR :: NOT ALLOWED");
 			return;
 		}
 		if (param!=null) {
 			FileTransferState state = getState(sender);
 			File path = null;
 			if (param.startsWith("/")) {
 				path = new File(state.getRootPath(),param.substring(1));
 			} else {
 				path = new File(state.getCurrentPath(),param);
 			}
 			if (!state.isAllowed(path)) {
 				sender.sendEncryptedText("ERROR IN MKDIR :: RESTRICTED PATH");
 			}
 			else if (path.exists()) {
 				sender.sendEncryptedText("ERROR IN MKDIR :: PATH ALREADY EXISTS");
 			}
 			else {
 				path.mkdirs();
 				sender.sendEncryptedText("ACK MKDIR :: "+state.getRelativPath(path));
 			}
 		}
 	}
 	
 	//change directory up
 	public void handle_cdup(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			sender.sendEncryptedText("ERROR IN CDUP :: PLEASE LOGIN");
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsCD()) {
 			sender.sendEncryptedText("ERROR IN CDUP :: NOT ALLOWED");
 			return;
 		}
 		FileTransferState state = getState(sender);
 		boolean ok = state.cdup();
 		if (ok) {
 			sender.sendEncryptedText("ACK CDUP :: "+state.getRelativPath());
 		} else {
 			sender.sendEncryptedText("ERROR IN CDUP :: PWD is "+state.getRelativPath());
 		}
 	}
 	
 	public void handle_pwd(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			sender.sendEncryptedText("ERROR IN PWD :: PLEASE LOGIN");
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsPWD()) {
 			sender.sendEncryptedText("ERROR IN PWD :: NOT ALLOWED");
 			return;
 		}
 		FileTransferState state = getState(sender);
 		System.out.println("ACK PWD :: "+state.getRelativPath());
 		sender.sendEncryptedText("ACK PWD :: "+state.getRelativPath());
 	}
 	
 
 	public void handle_list(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			sender.sendEncryptedText("ERROR IN LIST :: PLEASE LOGIN");
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsList()) {
 			sender.sendEncryptedText("ERROR IN LIST :: NOT ALLOWED");
 			return;
 		}
 		FileTransferState state = getState(sender);
 		File f = null;
 		if (param!=null) {
 			if (param.startsWith("/")) {
 				f = new File(state.getRootPath()+param);
 			} else {
 				f = new File(state.getCurrentPath(),param);
 			}
 			
 		} else {
 			f = state.getCurrentPath();
 		}
 		if (!f.exists() || !f.isDirectory()) {
 			sender.sendEncryptedText("ERROR IN LIST :: DIRECTORY \""+param+"\" DOES NOT EXIST.");
 		} else {
 			File[] list = f.listFiles();
 			String files = "";
 			for (int i=0;i<list.length;i++) {
 				if (i>0) files += ";;";
 				String path = RemoteFileSystem.makeEscapeChars(state.getRelativPath(list[i].getParentFile()));
 				String name = RemoteFileSystem.makeEscapeChars(list[i].getName());
 				files += path+",,"+name+",,"+list[i].length()+",,"+list[i].lastModified()+",,"+list[i].isDirectory();
 			}
 			//System.out.println("ACK LIST :: "+files);
 			sender.sendEncryptedText("ACK LIST :: "+files);
 		}
 	}
 	
 	public void handle_delete(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			sender.sendEncryptedText("ERROR IN DELETE :: PLEASE LOGIN");
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsDelete()) {
 			sender.sendEncryptedText("ERROR IN DELETE :: NOT ALLOWED");
 			return;
 		}
 		FileTransferState state = getState(sender);
 		File f = null;
 		if (param!=null) {
 			if (param.startsWith("/")) {
 				f = new File(state.getRootPath()+param);
 			} else {
 				f = new File(state.getCurrentPath(),param);
 			}
 		}
 		System.out.println("deleting "+f.getAbsolutePath());
 		if (f==null || !f.exists() || !state.isAllowed(f)) {
 			sender.sendEncryptedText("ERROR IN DELETE :: FILE \""+param+"\" DOES NOT EXIST.");
 		} else {
 			if (f.isDirectory()) {
 				boolean ok = deleteDirectory(f);
 				if (ok) {
 					sender.sendEncryptedText("ACK DELETE :: "+param);
 				} else {
 					sender.sendEncryptedText("ERROR IN DELETE :: DIRECTORY \""+param+"\" COULD NOT BE DELETED.");
 				}
 			} else {
 				boolean ok = f.delete();
 				if (ok) {
 					sender.sendEncryptedText("ACK DELETE :: "+param);
 				} else {
 					sender.sendEncryptedText("ERROR IN DELETE :: FILE \""+param+"\" COULD NOT BE DELETED.");
 				}
 			}
 		}
 	}
 	
 	public static boolean deleteDirectory(File path) {
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
 		return(path.delete());
 	}
 	
 	
 	
 	
 	public void handle_put(String param, OSDXSocketServerThread sender) {
 		//System.out.println("PUT "+param);
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			String resp = "ERROR IN PUT :: PLEASE LOGIN";
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "PUT", param, resp);
 			sender.sendEncryptedText(resp);
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsUpload()) {
 			String resp = "ERROR IN PUT :: NOT ALLOWED";
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "PUT", param, resp);
 			sender.sendEncryptedText(resp);
 			return;
 		}
 		FileTransferState state = getState(sender);
 		if (param!=null) {
 			String[] params = Util.getParams(param);
 			
 			File f = null;
 			if (params[0].startsWith("/")) {
 				f = new File(state.getRootPath()+params[0]);
 			} else {
 				f = new File(state.getCurrentPath(),params[0]);
 			}
 			if (f.exists()) {
 				String resp = "ERROR IN PUT :: FILE ALREADY EXISTS";
 				log.logCommand(sender.getID(), sender.getRemoteIP(), "PUT", param, resp);
 				sender.sendEncryptedText(resp);
 			} else {
 				//check if signature in param, extract and save
 				boolean hasSignature = false;
 				if (params.length>=2 && params[1].startsWith("<?xml")) {
 					//parse signature
 					try {
 						Document docSig = Document.fromString(params[1]); 
 						Signature sig = Signature.fromElement(docSig.getRootElement());					
 						//parsing successful without exception -> save signature 
 						File fsig = new File(f.getAbsolutePath()+"_signature.xml");
 						docSig.writeToFile(fsig);
 						//only if no error occurred -> hasSignature = true;
 						hasSignature = true;
 					} catch (Exception ex) {
 						log.logError(sender.getID(), sender.getRemoteIP(), "ERROR parsing signature during PUT file command :: "+ex.getMessage());
 						ex.printStackTrace();
 					}
 				}
 				boolean needsSignature = cs.getRightsAndDuties().needsSignature(f.getName());
 				if (needsSignature && !hasSignature) {
 					String resp = "ERROR IN PUT :: FILE NEEDS SIGNATURE";
 					log.logCommand(sender.getID(), sender.getRemoteIP(), "PUT", param, resp);
 					sender.sendEncryptedText(resp);
 				} else {
 					state.setWriteFile(f);
 					state.setNextFilePartStart(-1L); //this is only set by PUTPART
 					state.setNextFilePartLength(-1); //this is only set by PUTPART
 					String resp = "ACK PUT :: WAITING FOR DATA";
 					log.logCommand(sender.getID(), sender.getRemoteIP(), "PUT", param, resp);
 					sender.sendEncryptedText(resp);
 				}
 			}
 		}
 	}
 	
 	public void handle_putpart(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			String resp = "ERROR IN PUTPART :: PLEASE LOGIN";
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 			sender.sendEncryptedText(resp);
 			return;
 		}
 		FileTransferState state = getState(sender);
 		
 		if (param!=null) {
 			String[] params = Util.getParams(param);
 			String filename = null; 
 			long start = -1;
 			int length = -1;
 			
 			try {
 				filename = params[0];
 				start = Long.parseLong(params[1]);
 				length = Integer.parseInt(params[2]);
 			} catch (Exception ex) {
 				String resp = "ERROR IN PUTPART :: WRONG FORMAT";
 				log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 				sender.sendEncryptedText(resp);
 			}
 			File f = state.getWriteFile();
 			if (f==null || !filename.equals(f.getName())) {
 				String resp = "ERROR IN PUTPART :: PLEASE SEND PUT OR RESUMEPUT COMMAND FIRST";
 				log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 				sender.sendEncryptedText(resp);
 			}
 			
 			if (f.exists()) {
 				if (f.length()==start) {
 					//append
 					state.setNextFilePartStart(start);
 					state.setNextFilePartLength(length);
 					String resp = "ACK PUTPART :: WAITING FOR DATA";
 					log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 					sender.sendEncryptedText(resp);
 				} else {
 					String resp = "ERROR IN PUTPART :: WRONG START POSITION, EXPECTED POS="+f.length();
 					log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 					sender.sendEncryptedText(resp);	
 				}
 			} else {
 				state.setNextFilePartStart(start);
 				state.setNextFilePartLength(length);
 				String resp = "ACK PUTPART :: WAITING FOR DATA";
 				log.logCommand(sender.getID(), sender.getRemoteIP(), "PUTPART", param, resp);
 				sender.sendEncryptedText(resp);
 			}
 		}
 	}
 	
 	public void handle_resumeput(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			String resp = "ERROR IN RESUMEPUT :: PLEASE LOGIN";
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "RESUMEPUT", param, resp);
 			sender.sendEncryptedText(resp);
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsUpload()) {
 			String resp = "ERROR IN RESUMEPUT :: NOT ALLOWED";
 			log.logCommand(sender.getID(), sender.getRemoteIP(), "RESUMEPUT", param, resp);
 			sender.sendEncryptedText(resp);
 			return;
 		}
 		FileTransferState state = getState(sender);
 		if (param!=null) {
 			//RESUMEPUT "filename" 
 			String[] params = Util.getParams(param);
 			File f = null;
 			if (params[0].startsWith("/")) {
 				f = new File(state.getRootPath()+params[0]);
 			} else {
 				f = new File(state.getCurrentPath(),params[0]);
 			}
 			if (f.exists()) {
 				String resp = "ACK RESUMEPUT :: STARTPOS="+f.length();
 				state.setWriteFile(f);
 				log.logCommand(sender.getID(), sender.getRemoteIP(), "RESUMEPUT", param, resp);
 				sender.sendEncryptedText(resp);
 			} else {
 				String resp = "ERROR IN RESUMEPUT :: FILE DOES NOT EXIST, PLEASE USE PUT INSTEAD";
 				log.logCommand(sender.getID(), sender.getRemoteIP(), "RESUMEPUT", param, resp);
 				sender.sendEncryptedText(resp);
 			}
 		}
 	}
 	
 	public void handle_noop(String param, OSDXSocketServerThread sender) {
 		//DO NO OPERATION
 	}
 	
 	public void handle_get(String param, OSDXSocketServerThread sender) {
 		ClientSettings cs = clients.get(sender.getID());
 		if (cs==null) {
 			sender.sendEncryptedText("ERROR IN GET :: PLEASE LOGIN");
 			return;
 		}
 		if (cs.getRightsAndDuties()==null || !cs.getRightsAndDuties().allowsDownload()) {
 			sender.sendEncryptedText("ERROR IN GET :: NOT ALLOWED");
 			return;
 		}
 		FileTransferState state = getState(sender);
 		if (param!=null) {
 			File f = null;
 			if (param.startsWith("/")) {
 				f = new File(state.getRootPath()+param);
 			} else {
 				f = new File(state.getCurrentPath(),param);
 			}
 			if (f.exists()) {
 				if (!f.isDirectory()) {
 					try {
 						long filelenght = f.length();
 						sender.sendEncryptedText("ACK GET :: FILELENGTH="+filelenght);
 						if (filelenght<=maxByteLength) {
 							//send in one data package
 							try {
 								ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 								FileInputStream fin = new FileInputStream(f);
 								byte[] buffer = new byte[1024];
 								int read;
 								while ((read = fin.read(buffer))>0) {
 									bOut.write(buffer, 0, read);
 								}
 								sender.sendEncryptedData(bOut.toByteArray());
 							} catch (Exception ex) {
 								ex.printStackTrace();
 							}
 						} else {
 							//send in multiple data packages
 							long nextStart = 0;
 							try {
 								FileInputStream fin = new FileInputStream(f);
 								byte[] buffer = new byte[maxByteLength];
 								int read;
 								while ((read = fin.read(buffer))>0) {
 									ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 									bOut.write(buffer, 0, read);
 									sender.sendEncryptedData(bOut.toByteArray());
 									nextStart += read;
 								}
 							} catch (Exception ex) {
 								ex.printStackTrace();
 							}
 						}
 					} catch (Exception ex) {
 						sender.sendEncryptedText("ERROR IN GET :: ERROR READING FROM FILE");
 					}
 				} else {
 					sender.sendEncryptedText("ERROR IN GET :: GIVEN FILE IS A DIRECTORY");
 				}
 			} else {
 				sender.sendEncryptedText("ERROR IN GET :: FILE DOES NOT EXIST");
 			}
 		}
 	}
 
 }
