 package plugins.FLIC.freenetMagic;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.RSAPrivateKeySpec;
 import java.security.spec.RSAPublicKeySpec;
 import java.text.SimpleDateFormat;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.TimeZone;
 import java.util.concurrent.ConcurrentHashMap;
 
 import plugins.FLIC.Worker;
 import plugins.FLIC.storage.RAMstore;
 import plugins.FLIC.storage.RAMstore.Channel;
 import plugins.FLIC.storage.RAMstore.User;
 
 import freenet.client.InsertException;
 import freenet.keys.FreenetURI;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.support.Base64;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 
 public class FreenetFCPParser extends Thread {
 	private ConcurrentHashMap<String, fcp_ident> fcpIdents = new ConcurrentHashMap<String, fcp_ident>();
 	private List<PluginReplySender> fcpConnections = new ArrayList<PluginReplySender>();
 	private ArrayDeque<SimpleFieldSet> pendingFLICevents = new ArrayDeque<SimpleFieldSet>();
 	private ArrayDeque<fcp_command> pendingFCPclientCommands = new ArrayDeque<fcp_command>();
 
 	private long FLICeventsProcessingTime = 0;
 	private long FCPclientCommandsProcessingTime = 0;
 	private boolean isRunning = false;
 	private Worker mPtrWorker;
 	private RAMstore mStorage;
 	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	private Bucket tmpBucket;
 	private OutputStream tmpOutputStream;
 	private long lastActivityCheck = 0;
 	private long activityCheckPeriod = 1 * 60 * 1000;			// 1 minute
 	private long identityInsertInterval = 1 * 60 * 60 * 1000;	// 6 hours // FIXME: figure out why the fuck FLIP stops fetching idents which insert identity messages like every 6 hours.
 	private long keepAliveInsertInterval = 13 * 60 * 1000;		// 13 minutes
 	
 	public FreenetFCPParser(Worker ptrWorker, RAMstore mStorage) {
 		this.mPtrWorker = ptrWorker;
 		this.mStorage = mStorage;
 		this.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
 		this.setName("FLIC.EventParser");
 		loadStoredIdentityEditions();
 	}
 	
 	public class fcp_command {
 		public PluginReplySender replySender;
 		public SimpleFieldSet command;
 		public fcp_command(PluginReplySender replySender, SimpleFieldSet command) {
 			this.replySender = replySender;
 			this.command = command;
 		}
 	}
 	
 	public class fcp_ident {
 		public PluginReplySender replySender;
 		public String fn_key_public;
 		public String fn_key_private;
 		public String rsa_key_public;
 		public String rsa_key_private;
 		public String name;
 		public boolean notifyOnAnnounce;
 		public boolean announced;
 		public boolean stalker;
 		public boolean ghost;
 		public boolean govMode;
 		public long lastIdentityInsertTime;
 		public long lastIdentityInsertEdition;
 		public long lastMessageInsertTime;
 		public long lastMessageInsertEdition;
 		public fcp_ident(PluginReplySender replySender, String fn_key_public, String fn_key_private, String rsa_key_public, String rsa_key_private, String name, boolean notifyOnAnnounce, boolean stalker, boolean ghost, boolean govMode) {
 			this.replySender = replySender;
 			this.fn_key_public = fn_key_public;
 			this.fn_key_private = fn_key_private;
 			this.rsa_key_public = rsa_key_public;
 			this.rsa_key_private = rsa_key_private;
 			this.name = name;
 			this.notifyOnAnnounce = notifyOnAnnounce;
 			this.announced = false;
 			this.stalker = stalker;
 			this.ghost = ghost;
 			this.govMode = govMode;
 			this.lastMessageInsertEdition = -1;
 			this.lastIdentityInsertEdition = -1;
 			this.lastMessageInsertTime = 0;
 			this.lastIdentityInsertTime = new Date().getTime();
 		}
 	}
 	
 	public class InsertMessage {
 		public String type;
 		public String identifier;
 		public String insertKey;
 		public long edition;
 		public String content;
 		public String fn_key_public;
 		public FreenetFCPParser fcp_callback;
 		public Bucket bucket;
 		public long startedAt;
 		public InsertMessage(String type, String identifier, String insertKey, long edition, Bucket bucket, String fn_key_public, FreenetFCPParser fcp_callback) {
 			this.type = type;
 			this.identifier = identifier;
 			this.insertKey = insertKey;
 			this.edition = edition;
 			this.bucket = bucket;
 			this.fn_key_public = fn_key_public;
 			this.fcp_callback = fcp_callback;
 			this.startedAt = new Date().getTime();
 		}
 	}
 
 	public void terminate() {
 		isRunning = false;
 		this.interrupt();
 	}
 
 	@Override
 	public void run() {
 		isRunning = true;
 		long now;
 		while(!isInterrupted() && isRunning) {
 			try {
 				synchronized (this) {
 					if(pendingFCPclientCommands.size() == 0 && pendingFLICevents.size() == 0) {
 						this.wait();
 					}
 					if(pendingFCPclientCommands.size() > 0) {
 						now = new Date().getTime();
 						processCommand(pendingFCPclientCommands.pollFirst());
 						FCPclientCommandsProcessingTime = new Date().getTime() - now;
 					}
 					if(pendingFLICevents.size() > 0) {
 						now = new Date().getTime();
 						if(now - lastActivityCheck > activityCheckPeriod) {
 							checkActivity(now);
 							now = new Date().getTime();
 							lastActivityCheck = now;
 						}
 						parseEvent(pendingFLICevents.pollFirst());
 						FLICeventsProcessingTime = new Date().getTime() - now;
 					}
 				}
 			} catch (InterruptedException e) {
 				// ignore
 			}
 		}
 		saveIdentityEditions();
 		SimpleFieldSet message = new SimpleFieldSet(true);
 		message.putSingle("command", "ByeBye");
 		message.putSingle("description", "plugin unloading");
 		Iterator<PluginReplySender> iter = fcpConnections.iterator();
 		while(iter.hasNext()) {
 			try {
 				iter.next().send(message);
 			} catch (PluginNotFoundException e) {
 				// ignore
 			}
 		}
 	}
 	
 	private void saveIdentityEditions() {
 		Properties configProps = new Properties();
 		FileOutputStream out;
 		try {
 			configProps.put("firstStart", mStorage.config.firstStart.toString());
 			int i = 0;
 			Iterator<String> iter = fcpIdents.keySet().iterator();
 			fcp_ident fcpIdent;
 			while(iter.hasNext()) {
 				fcpIdent = fcpIdents.get(iter.next());
 				if(fcpIdent.announced) {
 					configProps.put("identity_" + i, fcpIdent.fn_key_public + "|" + mStorage.getCurrentDateString() + "|" + fcpIdent.lastIdentityInsertEdition + "|" + fcpIdent.lastMessageInsertEdition);
 					i++;
 				}
 			}
 			configProps.put("identityCount", "" + i);
 			out = new FileOutputStream("FLIC/config", false);
 			configProps.store(out, "configuration created by FLIC " + mStorage.config.version_major + "." + mStorage.config.version_minor + "." + mStorage.config.version_release);
 			out.close();
 		} catch (FileNotFoundException e) {
 			System.err.println("[FLIC] can't create configuration file freenet_directory/FLIC/config. please check your file permissions. " + e.getMessage());
 		} catch (IOException e) {
 			System.err.println("[FLIC] can't write configuration to freenet_directory/FLIC/config. please check your file permissions. " + e.getMessage());
 		} catch(ClassCastException e) {
 			System.err.println("[FLIC] at least one configuration property is invalid. please do not manually modify the configuration file. " + e.getMessage());
 		}
 	}
 	
 	private void loadStoredIdentityEditions() {
 		Properties configProps = new Properties();
 		FileInputStream in;
 		try {
 			in = new FileInputStream("FLIC/config");
 			configProps.load(in);
 			in.close();
 			int identityCount;
 			try {
 				identityCount = Integer.parseInt(configProps.getProperty("identityCount", "0"));
 			} catch (NumberFormatException e) {
 				identityCount = 0;
 			}
 			int i;
 			fcp_ident fcpIdent;
 			for(i = 0; i < identityCount; i++) {
 				String identity = configProps.getProperty("identity_" + i, "");
 				// fn_key_public|yyyy-mm-dd|identityEdition|messageEdition
 				String[] identDetails = identity.split("\\|");
 				if(identDetails.length != 4) {
 					continue;
 				}
 				if(!identDetails[1].equals(mStorage.getCurrentDateString())) {
 					continue;
 				}
 				//insert identity with ghost = true so it will not be inserted or announced until the user registers
 				fcpIdent = new fcp_ident(null, identDetails[0], "", "", "", "", false, false, true, false);
 				fcpIdent.announced = true;
 				try {
 					fcpIdent.lastIdentityInsertEdition = Long.parseLong(identDetails[2]);
 					fcpIdent.lastMessageInsertEdition = Long.parseLong(identDetails[3]);
 					fcpIdents.put(identDetails[0], fcpIdent);
 					mStorage.addNewUser(identDetails[0]);
 				} catch(NumberFormatException e) {
 					continue;
 				}
 			}
 		} catch (FileNotFoundException e) {
 			// file does not exist
 		} catch (IOException e) {
 			System.err.println("[FLIC] can't read configuration file freenet_directory/FLIC/config. please check your file permissions. " + e.getMessage());
 		} catch (IllegalArgumentException e) {
 			System.err.println("[FLIC] at least one configuration property found in your configuration file is invalid. please do not manually modify the configuration file. " + e.getMessage());
 		}
 		saveIdentityEditions();
 	}
 	
 	private InsertMessage getAnnounceInsertMessage(fcp_ident fcpIdent) {
 		String insertKey = "KSK@" + mStorage.getMessageBase() + "|"+ mStorage.getCurrentDateString() + "|Announce|";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(fcpIdent.fn_key_public.getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(fcpIdent.fn_key_public.getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			String identifier = insertKey; 
 			return new InsertMessage("announce", identifier, insertKey, mStorage.getLatestFinishedAnnounceEdition() + 1, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		}
 	}
 	
 	private InsertMessage getIdentityInsertMessage(fcp_ident fcpIdent) {
 		StringBuilder out = new StringBuilder();
 		out.append("lastmessageindex=");
 		if(fcpIdent.lastMessageInsertEdition < 0) {
 			out.append("0\n");
 		} else {
 			out.append(fcpIdent.lastMessageInsertEdition + "\n");
 		}
 		out.append("name=" + fcpIdent.name + "\n");
 		out.append("rsapublickey=" + fcpIdent.rsa_key_public + "\n");
 		// PMformat: RSA_FLIP, RSA_FLIP_SECRET, none
 		out.append("PMformat=none\n");
 		out.append("client=FLIC " + mStorage.config.version_major + "." + mStorage.config.version_minor + "." + mStorage.config.version_release + "\n\n");
 		fcpIdent.lastIdentityInsertTime = new Date().getTime();
 		fcpIdent.lastIdentityInsertEdition++;
 		String insertKey = fcpIdent.fn_key_private + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Identity-";
 		String identifier = fcpIdent.fn_key_public + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Identity-";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(out.toString().getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(out.toString().getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			return new InsertMessage("identity", identifier, insertKey, fcpIdent.lastIdentityInsertEdition, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		}
 	}
 	
 	private InsertMessage getChannelMessageInsertMessage(fcp_ident fcpIdent, String channel, String message) {
 		StringBuilder out = new StringBuilder();
 		out.append("channel=" + channel + "\n");
 		out.append("sentdate=" + sdf.format(new Date().getTime()) + "\n");
 		out.append("type=channelmessage\n\n");
 		out.append(message);
 		fcpIdent.lastMessageInsertTime = new Date().getTime();
 		fcpIdent.lastMessageInsertEdition++;
 		String insertKey = fcpIdent.fn_key_private + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		String identifier = fcpIdent.fn_key_public + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(out.toString().getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(out.toString().getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			return new InsertMessage("message", identifier, insertKey, fcpIdent.lastMessageInsertEdition, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		}
 	}
 	
 	private InsertMessage getPrivateMessageInsertMessage(fcp_ident fcpIdent, String destination, String message) {
 		StringBuilder out = new StringBuilder();
 		out.append("recipient=" + destination + "\n");
 		out.append("sentdate=" + sdf.format(new Date().getTime()) + "\n");
 		out.append("type=privatemessage\n\n");
 		//out.append(rsacrypter(fromRSAprivate, toRSApublic, message));
 		fcpIdent.lastMessageInsertTime = new Date().getTime();
 		fcpIdent.lastMessageInsertEdition++;
 		String insertKey = fcpIdent.fn_key_private + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		String identifier = fcpIdent.fn_key_public + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(out.toString().getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(out.toString().getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			return null;
 			//return new InsertMessage("message", identifier, insertKey, fcpIdent.lastMessageInsertEdition, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		}
 	}
 	
 	private InsertMessage getTopicChangeInsertMessage(fcp_ident fcpIdent, String channel, String newTopic) {
 		StringBuilder out = new StringBuilder();
 		out.append("channel=" + channel + "\n");
 		out.append("sentdate=" + sdf.format(new Date().getTime()) + "\n");
 		out.append("type=settopic\n\n");
 		out.append(newTopic);
 		fcpIdent.lastMessageInsertTime = new Date().getTime();
 		fcpIdent.lastMessageInsertEdition++;
 		String insertKey = fcpIdent.fn_key_private + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		String identifier = fcpIdent.fn_key_public + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(out.toString().getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(out.toString().getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			return new InsertMessage("message", identifier, insertKey, fcpIdent.lastMessageInsertEdition, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		} 
 	}
 	
 	private InsertMessage getChannelJoinInsertMessage(fcp_ident fcpIdent, String channel) {
 		StringBuilder out = new StringBuilder();
 		out.append("channel=" + channel + "\n");
 		out.append("sentdate=" + sdf.format(new Date().getTime()) + "\n");
 		out.append("type=joinchannel\n\n");
 		fcpIdent.lastMessageInsertTime = new Date().getTime();
 		fcpIdent.lastMessageInsertEdition++;
 		String insertKey = fcpIdent.fn_key_private + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		String identifier = fcpIdent.fn_key_public + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(out.toString().getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(out.toString().getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			return new InsertMessage("message", identifier, insertKey, fcpIdent.lastMessageInsertEdition, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		} 
 	}
 	
 	private InsertMessage getChannelPartInsertMessage(fcp_ident fcpIdent, String channel) {
 		StringBuilder out = new StringBuilder();
 		out.append("channel=" + channel + "\n");
 		out.append("sentdate=" + sdf.format(new Date().getTime()) + "\n");
 		out.append("type=partchannel\n\n");
 		fcpIdent.lastMessageInsertTime = new Date().getTime();
 		fcpIdent.lastMessageInsertEdition++;
 		String insertKey = fcpIdent.fn_key_private + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		String identifier = fcpIdent.fn_key_public + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(out.toString().getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(out.toString().getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			return new InsertMessage("message", identifier, insertKey, fcpIdent.lastMessageInsertEdition, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		}		
 	}
 	
 	private InsertMessage getKeepAliveInsertMessage(fcp_ident fcpIdent) {
 		// TODO: add topics
 		StringBuilder out = new StringBuilder();
 		out.append("channels=");
 		StringBuilder channelList = new StringBuilder(); 
 		if(!fcpIdent.stalker) {
 			Iterator<String> iter = mStorage.userMap.get(fcpIdent.fn_key_public).channels.iterator();
 			while(iter.hasNext()) {
 				channelList.append(iter.next() + " ");
 			}
 		}
 		out.append(channelList.toString().trim() + "\n");
 		out.append("sentdate=" + sdf.format(new Date().getTime()) + "\n");
 		out.append("type=keepalive\n\n");
 		fcpIdent.lastMessageInsertTime = new Date().getTime();
 		fcpIdent.lastMessageInsertEdition++;
 		String insertKey = fcpIdent.fn_key_private + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		String identifier = fcpIdent.fn_key_public + mStorage.getMessageBase() + "|" + mStorage.getCurrentDateString() + "|Message-";
 		try {
 			tmpBucket = mPtrWorker.getBucketFactory().makeBucket(out.toString().getBytes().length);
 			tmpOutputStream = tmpBucket.getOutputStream();
 			tmpOutputStream.write(out.toString().getBytes());
 			tmpOutputStream.close();
 			tmpBucket.setReadOnly();
 			return new InsertMessage("message", identifier, insertKey, fcpIdent.lastMessageInsertEdition, tmpBucket, fcpIdent.fn_key_public, this);
 		} catch (IOException e) {
 			System.err.println("can't create bucket: " + e.getMessage());
 			return null;
 		}		
 	}
 	
 	public synchronized void onInsertFailed(InsertMessage message, InsertException e) {
 		// TODO: implement other error modes as COLLISIONS. check Worker.java
 		if(message.type.equals("announce")) {
 			System.err.println("[FreenetFCPParser]::onInsertFailed() COLLISION for announce insert " + message.edition);
 			message.edition++;
 			// better ? message.edition = mStorage.getLatestFinishedAnnounceEdition() + 1;
 		} else if(message.type.equals("identity")) {
 			System.err.println("[FreenetFCPParser]::onInsertFailed() COLLISION for identity insert " + message.edition);
 			fcpIdents.get(message.fn_key_public).lastIdentityInsertEdition++;
 			message.edition = fcpIdents.get(message.fn_key_public).lastIdentityInsertEdition;
 		} else if(message.type.equals("message")) {
 			System.err.println("[FreenetFCPParser]::onInsertFailed() COLLISION for message insert " + message.edition);
 			fcpIdents.get(message.fn_key_public).lastMessageInsertEdition++;
 			message.edition = fcpIdents.get(message.fn_key_public).lastMessageInsertEdition;
 		}
 		// add the job on top of the queue again.
 		mPtrWorker.insertMessage(message, true);
 	}
 	
 	public synchronized void onInsertFinished(InsertMessage message) {
 		System.err.println("[FreenetFCPParser]::onInsertFinished() " + message.identifier + message.edition);
 		message.bucket.free();
 		fcp_ident fcpIdent = fcpIdents.get(message.fn_key_public);
 		if(fcpIdent != null) {
 			if ("identity".equals(message.type)) {
 				if(!fcpIdent.announced) {
 					fcpIdent.announced=true;
 					if(fcpIdent.notifyOnAnnounce) {
 						if(fcpIdent.replySender != null) {
 							SimpleFieldSet reply = new SimpleFieldSet(true);
 							reply.putSingle("command", "userRegistered");
 							reply.putSingle("fn_key_public", message.fn_key_public);
 							reply.putSingle("status", "1");
 							try {
 								fcpIdent.replySender.send(reply);
 							} catch (PluginNotFoundException e) {
 								// client disconnected
 								fcpConnections.remove(fcpIdent.replySender);
 								fcpIdent.replySender = null;
 							}
 						}
 					}
 				}
 			}
 		} else {
 			// uh?
 		}
 	}
 	
 	public int getCurrentFLICeventsQueueSize() {
 		return pendingFLICevents.size();
 	}
 
 	public int getCurrentFCPclientCommandsQueueSize() {
 		return pendingFCPclientCommands.size();
 	}
 	
 	public float getFLICeventsProcessingTime() {
 		return FLICeventsProcessingTime / 1000;
 	}
 	
 	public float getFCPclientCommandsProcessingTime() {
 		return FCPclientCommandsProcessingTime / 1000;
 	}
 	
 	public synchronized void addFLICevent(SimpleFieldSet event) {
 		pendingFLICevents.addLast(event);
 		this.notify();
 	}
 	
 	public synchronized void addFCPclientCommand(PluginReplySender replySender, SimpleFieldSet command) {
 		//System.err.println("addFCPclientCommand(): adding " + command.get("command") + " as last command in queue.");
 		pendingFCPclientCommands.addLast(new fcp_command(replySender, command));
 		this.notify();
 	}
 	
 	public synchronized void setAllEditionsToZero() {
 		fcp_ident fcpIdent;
 		Iterator<String> iter = fcpIdents.keySet().iterator();
 		while(iter.hasNext()) {
 			fcpIdent = fcpIdents.get(iter.next());
 			if(!fcpIdent.ghost && fcpIdent.replySender != null) {
 				fcpIdent.lastIdentityInsertEdition = -1;
 				fcpIdent.lastIdentityInsertTime = 0;
 				fcpIdent.lastMessageInsertEdition = -1;
 				fcpIdent.lastMessageInsertTime = 0;
 				fcpIdent.announced = false;
 				mPtrWorker.insertMessage(getAnnounceInsertMessage(fcpIdent));
 			}
 		}
 	}
 	
 	private void checkActivity(long now) {
 		fcp_ident fcpIdent;
 		Iterator<String> iter = fcpIdents.keySet().iterator();
 		while(iter.hasNext()) {
 			fcpIdent = fcpIdents.get(iter.next());
 			if(fcpIdent.replySender == null) {
 				continue;
 			}
 			if(!fcpConnections.contains(fcpIdent.replySender)) {
 				continue;
 			}
 			if(fcpIdent.ghost) {
 				continue;
 			}
 			if(now - fcpIdent.lastIdentityInsertTime > identityInsertInterval) {
 				mPtrWorker.insertMessage(getIdentityInsertMessage(fcpIdent));
 				// lastIdentityInsertTime is set to current date automatically
 			}
 			if(now - fcpIdent.lastMessageInsertTime > keepAliveInsertInterval) {
 				mPtrWorker.insertMessage(getKeepAliveInsertMessage(fcpIdent));
 				// lastMessageInsertTime is set to current date automatically
 			}
 		}
 	}
 	
 	private void processCommand(fcp_command fcpCommand) {
 		//System.err.println("FCPParser: processing " + fcpCommand.command.get("command"));
 		if("createUser".equals(fcpCommand.command.get("command"))) {
 			processCreateUser(fcpCommand);
 		} else if("registerUser".equals(fcpCommand.command.get("command"))) {
 			processRegisterUser(fcpCommand);
 		} else if("getChannelList".equals(fcpCommand.command.get("command"))) {
 			processGetChannelList(fcpCommand);
 		} else if("getUserList".equals(fcpCommand.command.get("command"))) {
 			processGetUserList(fcpCommand);
 		} else if("joinChannel".equals(fcpCommand.command.get("command"))) {
 			processJoinChannel(fcpCommand);
 		} else if("partChannel".equals(fcpCommand.command.get("command"))) {
 			processPartChannel(fcpCommand);
 		} else if("sendMessage".equals(fcpCommand.command.get("command"))) {
 			processSendMessage(fcpCommand);
 		} else if("changeTopic".equals(fcpCommand.command.get("command"))) {
 			processChangeTopic(fcpCommand);
 		} else if("changeNick".equals(fcpCommand.command.get("command"))) {
 			processChangeNick(fcpCommand);
 		} else if("ping".equals(fcpCommand.command.get("command"))) {
 			processPing(fcpCommand);
 		} else if("pong".equals(fcpCommand.command.get("command"))) {
 			processPong(fcpCommand);
 		} else {
 			fcpCommand.command.putSingle("oldCommand", fcpCommand.command.get("command"));
 			fcpCommand.command.putOverwrite("command", "notSupported");
 			try {
 				fcpCommand.replySender.send(fcpCommand.command);
 			} catch (PluginNotFoundException e) {
 				// FCP connection disconnected
 				// will be added again on next connection
 				fcpConnections.remove(fcpCommand.replySender);
 			}
 		}
 	}
 	
 	private void processCreateUser(fcp_command fcpCommand) {
 		SimpleFieldSet command = fcpCommand.command;
 		try {
 			KeyPairGenerator kpg;
 			KeyPair kp;
 			kpg = KeyPairGenerator.getInstance("RSA");
 			// 128 byte key
 			kpg.initialize(1024);
 			kp = kpg.genKeyPair();
 			KeyFactory fact = KeyFactory.getInstance("RSA");
 			RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
 			RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
 			// pubkey: create base64 encoded values of public modulus and public exponent
 			// flip format: length(modulus) + | + base64(modulus) + | + base64(exponent)
 			String rsa_key_public;
 			rsa_key_public = (pub.getModulus().bitLength() / 8) + "|";
 			rsa_key_public += Base64.encodeStandard(pub.getModulus().toByteArray()) + "|";
 			rsa_key_public += Base64.encodeStandard(pub.getPublicExponent().toByteArray());
 			rsa_key_public = rsa_key_public.replace("\n", "");
 			// private: create base64 encode values of private modulus (same as public modulus?), public exponent, 
 			// flip format: length(modulus) + | + base64(modulus) + | + base64(public exponent) + | + base64(private exponent) + | + ?
 			String rsa_key_private;
 			rsa_key_private = (priv.getModulus().bitLength() / 8) + "|";
 			rsa_key_private += Base64.encodeStandard(priv.getModulus().toByteArray()) + "|";
 			rsa_key_private += Base64.encodeStandard(pub.getPublicExponent().toByteArray()) + "|";
 			rsa_key_private += Base64.encodeStandard(priv.getPrivateExponent().toByteArray()) + "|";
 			// privateKey += ?
 			
 			FreenetURI keys[] = mPtrWorker.getHighLevelSimpleClient().generateKeyPair("");
 			String fn_key_public = keys[1].toString();
 			String fn_key_private = keys[0].toString();
 			
 			command.putOverwrite("command", "createdUser");
 			command.putOverwrite("fn_key_public", fn_key_public);
 			command.putOverwrite("fn_key_private", fn_key_private);
 			command.putOverwrite("rsa_key_public", rsa_key_public);
 			command.putOverwrite("rsa_key_private", rsa_key_private);
 			try {
 				fcpCommand.replySender.send(command);
 			} catch (PluginNotFoundException e) {
 				// client disconnected
 				fcpConnections.remove(fcpCommand.replySender);
 			}
 		} catch (NoSuchAlgorithmException e) {
 			System.err.println("[FLIC] can't find RSA keygenerator. " + e.getMessage());
 			e.printStackTrace();
 		} catch (InvalidKeySpecException e) {
 			System.err.println("[FLIC] can't find RSA specs. " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	private void processRegisterUser(fcp_command fcpCommand) {
 		// register an already created user
 		// FIXME: always check for != null if using command.get(). everywhere!
 		SimpleFieldSet command = fcpCommand.command;
 		PluginReplySender replySender = fcpCommand.replySender;
 		String fn_key_public = command.get("fn_key_public");
 		String fn_key_private = command.get("fn_key_private");
 		SimpleFieldSet reply = new SimpleFieldSet(true);
 		if(!fn_key_public.endsWith("/") || !fn_key_private.endsWith("/")) {
 			//wrong format. fn_key_* needs to end with /
 			String bla="error message bla bla";
 			System.err.println("processRegisterUser() failed: fn_key does not end with /");
 			return;
 		}
 		try {
 			new FreenetURI(fn_key_public);
 			new FreenetURI(fn_key_private);
 		} catch (MalformedURLException e) {
 			String bla="error message bla bla " + e.getMessage();
 			System.err.println("processRegisterUser() failed: fn_key is not valid: " + e.getMessage());
 			return;
 		}
 		String nick = command.get("username");
 		if("".equals(nick)) {
 			String bla="error message bla bla ";
 			System.err.println("processRegisterUser() failed: username is empty");
 			return;
 		} else if(nick.indexOf((char) '>') > -1) {
 			// TODO: check for other unallowed chars in IRC nicks
 			String bla="error message bla bla";
 			System.err.println("processRegisterUser() failed: username contains unallowed chars");
 			System.err.println();
 			return;
 		}
 		String rsa_key_public = command.get("rsa_key_public");
 		String rsa_key_private = command.get("rsa_key_private");
 		boolean notifyOnAnnounce = true;
 		if(command.get("notifyOnAnnounce") != null) {
 			notifyOnAnnounce = Boolean.parseBoolean(command.get("notifyOnAnnounce"));
 		}
 		boolean stalker = false;
 		if(command.get("stalker") != null) {
 			stalker = Boolean.parseBoolean(command.get("stalker"));
 		}
 		boolean ghost = false;
 		if(command.get("ghost") != null) {
 			ghost = Boolean.parseBoolean(command.get("ghost"));
 			notifyOnAnnounce = false;
 		}
 		boolean govMode = false;
 		if(command.get("govmode") != null) {
 			govMode = Boolean.parseBoolean(command.get("govmode"));
 		}
 		if(!mStorage.knownIdents.contains(fn_key_public)) {
 			mStorage.addNewUser(fn_key_public, true);
 		}
 		if(!fcpConnections.contains(replySender)) {
 			fcpConnections.add(replySender);
 		}
 		fcp_ident fcpIdent;
 		if(fcpIdents.containsKey(fn_key_public)) {
 			// public key already registered. update.
 			fcpIdent = fcpIdents.get(fn_key_public);
 			fcpIdent.fn_key_private = fn_key_private;
 			fcpIdent.ghost = ghost;
 			fcpIdent.govMode = govMode;
 			fcpIdent.name = nick;
 			fcpIdent.notifyOnAnnounce = notifyOnAnnounce;
 			fcpIdent.replySender = replySender;
 			fcpIdent.rsa_key_private = rsa_key_private;
 			fcpIdent.rsa_key_public = rsa_key_public;
 			fcpIdent.stalker = stalker;
 		} else {
 			fcpIdent = new fcp_ident(replySender, fn_key_public, fn_key_private, rsa_key_public, rsa_key_private, nick, notifyOnAnnounce, stalker, ghost, govMode);
 			fcpIdents.put(fn_key_public, fcpIdent);
 		}
 		reply.putSingle("command", "userRegistered");
 		reply.putSingle("fn_key_public", fn_key_public);
 		if(!fcpIdent.ghost) {
 			if(!mStorage.userMap.get(fcpIdent.fn_key_public).lastAnnounceFoundFor.equals(mStorage.getCurrentDateString())) {
 				mPtrWorker.insertMessage(getAnnounceInsertMessage(fcpIdent));
 				mPtrWorker.insertMessage(getIdentityInsertMessage(fcpIdent));
 				reply.putSingle("status", "0");
 			} else {
 				fcpIdent.announced = true;
 				mPtrWorker.insertMessage(getIdentityInsertMessage(fcpIdent));
 				reply.putSingle("status", "1");
 			}
 		} else {
 			reply.putSingle("status", "0");
 		}
 		if(replySender != null) {
 			try {
 				replySender.send(reply);
 			} catch (PluginNotFoundException e) {
 				// client disconnected
 				fcpConnections.remove(replySender);
 				fcpIdent.replySender = null;
 			}
 		}
 	}
 	
 	private void processGetChannelList(fcp_command fcpCommand) {
 		// return a list of all known channels, topics and usercount
 		SimpleFieldSet command = fcpCommand.command;
 		PluginReplySender replySender = fcpCommand.replySender;
 		command.putOverwrite("command", "ChannelList");
 		int i = 0;
 		Channel chan;
 		Iterator<Channel> iter = mStorage.channelList.iterator();
 		while(iter.hasNext()) {
 			i++;
 			chan = iter.next();
 			command.putSingle(i + ".name", chan.name.substring(1, chan.name.length()));
 			command.putSingle(i + ".topic", chan.topic);
 			command.put(i + ".users", chan.currentUserCount);
 		}
 		command.put("channelCount", i);
 		try {
 			replySender.send(command);
 		} catch (PluginNotFoundException e) {
 			// FCP connection disconnected
 			// will be added again on next connection
 			fcpConnections.remove(replySender);
 		}
 	}
 	
 	private void processGetUserList(fcp_command fcpCommand) {
 		// return a list of all known users with scope global or channel
 		SimpleFieldSet command = fcpCommand.command;
 		PluginReplySender replySender = fcpCommand.replySender;
 		command.putOverwrite("command", "ChannelList");
 		String channel = "";
 		boolean global = false;
 		if("global".equals(command.get("scope"))) {
 			global = true;
 		} else {
 			channel = "#" + command.get("channel");
 		}
 		User user;
 		int i = 0;
 		String fn_key_public;
 		Iterator<String> iter = mStorage.knownIdents.iterator();
 		while(iter.hasNext()) {
 			fn_key_public = iter.next();
 			if(global || mStorage.isUserInChannel(fn_key_public, channel)) {
 				// TODO: add still connected flag based on lastActivity
 				// TODO: ^ only include in list if still connected
 				// TODO: ^ channel list does this already, scope=global does not
 				user = mStorage.userMap.get(fn_key_public);
 				i++;
 				command.putSingle(i + ".name", user.nick);
 				command.putSingle(i + ".fn_key_public", fn_key_public);
 				command.put(i + ".lastActivity", user.lastActivity);
 				command.put(i + ".lastMessageTime", user.lastMessageTime);
 			}
 		}
 		command.put("userCount", i);
 		try {
 			replySender.send(command);
 		} catch (PluginNotFoundException e) {
 			// FCP connection disconnected
 			// will be added again on next connection
 			fcpConnections.remove(replySender);
 		}
 	}
 	
 	private void processJoinChannel(fcp_command fcpCommand) {
 		// join channel and return userlist of joined channel
 		SimpleFieldSet command = fcpCommand.command;
 		PluginReplySender replySender = fcpCommand.replySender;
 		String ident = command.get("fn_key_public");
 		String channel = "#" + command.get("channel");
 		if(!mStorage.addNewChannel(channel)) {
 			// reply error for not allowed chars in channel name
 		}
 		mStorage.addUserToChannel(ident, channel);
 		fcp_ident fcpIdent = fcpIdents.get(ident);
 		if(fcpIdent == null) {
 			System.err.println("processJoinChannel(): fcpIdent == null, wtf?");
 		}
 		// FIXME: returns new event with current user joined channel
 		// FIXME: ^ which is good to inform other fcp_idents. create a filter?
 		// FIXME: ^ problem if ghost or stalker
 		User user;
 		int i = 0;
 		String fn_key_public;
 		Iterator<String> iter = mStorage.knownIdents.iterator();
 		while(iter.hasNext()) {
 			fn_key_public = iter.next();
 			if(mStorage.isUserInChannel(fn_key_public, channel)) {
 				i++;
 				user = mStorage.userMap.get(fn_key_public);
 				command.putSingle(i + ".name", user.nick);
 				command.putSingle(i + ".fn_key_public", fn_key_public);
 				command.put(i + ".lastActivity", user.lastActivity);
 				command.put(i + ".lastMessageTime", user.lastMessageTime);
 			}
 		}
 		command.putOverwrite("command", "UserList");
 		command.putSingle("scope", "channel");
 		command.put("userCount", i);
 		try {
 			replySender.send(command);
 		} catch (PluginNotFoundException e) {
 			// FCP connection disconnected
 			// will be added again on next connection
 			fcpConnections.remove(replySender);
 			fcpIdent.replySender = null;
 		}
 		if(!fcpIdent.stalker && !fcpIdent.ghost) {
 			mPtrWorker.insertMessage(getChannelJoinInsertMessage(fcpIdent, channel));
 		}
 	}
 	
 	private void processPartChannel(fcp_command fcpCommand) {
 		SimpleFieldSet command = fcpCommand.command;
 		String channel = "#" + command.get("channel");
 		String ident = command.get("fn_key_public");
 		mStorage.removeUserFromChannel(ident, channel);
 		fcp_ident fcpIdent = fcpIdents.get(ident);
 		if(!fcpIdent.stalker && !fcpIdent.ghost) {
 			mPtrWorker.insertMessage(getChannelPartInsertMessage(fcpIdent, channel));
 		}
 	}
 	
 	private void processSendMessage(fcp_command fcpCommand) {
 		SimpleFieldSet command = fcpCommand.command;
 		String ident = command.get("fn_key_public");
 		fcp_ident fcpIdent = fcpIdents.get(ident);
 		boolean error = false;
 		if(fcpIdent.ghost) {
 			command.putOverwrite("oldCommand", command.get("command"));
 			command.putOverwrite("command", "error");
 			command.putOverwrite("description", "ever heard a ghost talking?");
 			error = true;
 		}
 		if(!"channel".equals(command.get("type"))) {
 			// private message
 			command.putOverwrite("oldCommand", command.get("command"));
 			command.putOverwrite("command", "notSupported");
 			error = true;
 		}
 		if(error) {
 			try {
 				fcpCommand.replySender.send(command);
 			} catch (PluginNotFoundException e) {
 				// FCP connection disconnected
 				// will be added again on next connection
 				fcpConnections.remove(fcpCommand.replySender);
 				fcpIdent.replySender = null;
 			}
 		} else {
 			// channel message
 			String channel = "#" + command.get("destination");
 			if(mStorage.addNewChannel(channel)) {
 				mStorage.addUserToChannel(ident, channel);
 			}
 			mPtrWorker.insertMessage(getChannelMessageInsertMessage(fcpIdent, channel, command.get("message")));
 			if(fcpIdent.stalker) {
 				mPtrWorker.insertMessage(getChannelPartInsertMessage(fcpIdent, channel));
 			}
 		}
 	}
 	
 	private void processChangeTopic(fcp_command fcpCommand) {
 		SimpleFieldSet command = fcpCommand.command;
 		fcp_ident fcpIdent = fcpIdents.get(command.get("fn_key_public"));
 		String channel = "#" + command.get("channel");
 		if(!fcpIdent.ghost) {
 			mPtrWorker.insertMessage(getTopicChangeInsertMessage(fcpIdent, channel, command.get("newTopic")));
 			if(fcpIdent.stalker) {
 				mPtrWorker.insertMessage(getChannelPartInsertMessage(fcpIdent, channel));
 			}
 		}
 		// ident get informed about topic change if his topic change message is received
 	}
 	
 	private void processChangeNick(fcp_command fcpCommand) {
 		// FIXME: validate newName for > whitespace < and stuff
 		SimpleFieldSet command = fcpCommand.command;
 		fcp_ident fcpIdent = fcpIdents.get(command.get("fn_key_public"));
 		fcpIdent.name = command.get("newName");
 		if(!fcpIdent.ghost) {
 			mPtrWorker.insertMessage(getIdentityInsertMessage(fcpIdent));
 		}
 	}
 	
 	private void processPing(fcp_command fcpCommand) {
 		SimpleFieldSet command = fcpCommand.command;
 		command.putOverwrite("command", "pong");
 		try {
 			fcpCommand.replySender.send(command);
 		} catch (PluginNotFoundException e) {
 			// FCP connection disconnected
 			// will be added again on next connection
 			fcpConnections.remove(fcpCommand.replySender);
 		}
 	}
 	
 	private void processPong(fcp_command fcpCommand) {
 		// TODO: update some timeout counter?
 	}
 	
 	private void parseEvent(SimpleFieldSet event) {
		// FIXME: add general validation based on command => event.get("x") != null
		// TODO: only use two functions (sendGlobal and sendSelective)?
 		if("gotIdentityFound".equals(event.get("command"))) {
 			parseGotIdentityFound(event);
 		} else if("gotChannelFound".equals(event.get("command"))) {
 			parseGotChannelFound(event);
 		} else if("gotNickChanged".equals(event.get("command"))) {
 			parseGotNickChanged(event);
 		} else if("gotChannelJoin".equals(event.get("command"))) {
 			parseGotChannelJoin(event);
 		} else if("gotChannelPart".equals(event.get("command"))) {
 			parseGotChannelPart(event);
 		} else if("gotMessage".equals(event.get("command"))) {
 			parseGotMessage(event);
 		} else if("gotTopicChange".equals(event.get("command"))) {
 			parseGotTopicChange(event);
 		} else if("gotKeepAlive".equals(event.get("command"))) {
 			parseGotKeepAlive(event);
 		} else {
 			// uh?
 		}
 	}
 	
 	private void parseGotIdentityFound(SimpleFieldSet event) {
 		// just send event to all FCP connections (not identities)
 		Iterator<PluginReplySender> iter = fcpConnections.iterator();
 		PluginReplySender sender;
 		while (iter.hasNext()) {
 			sender = iter.next();
 			if(sender != null) {
 				try {
 					sender.send(event);
 				} catch (PluginNotFoundException e) {
 					// FCP connection disconnected
 					// will be added again on next connection
 					iter.remove();
 				}
 			}
 		}
 	}
 	
 	private void parseGotChannelFound(SimpleFieldSet event) {
 		// just send event to all FCP connections (not identities)
 		Iterator<PluginReplySender> iter = fcpConnections.iterator();
 		PluginReplySender sender;
 		while (iter.hasNext()) {
 			sender = iter.next();
 			if(sender != null) {
 				try {
 					sender.send(event);
 				} catch (PluginNotFoundException e) {
 					// FCP connection disconnected
 					// will be added again on next connection
 					iter.remove();
 				}
 			}
 		}
 	}
 	
 	private void parseGotNickChanged(SimpleFieldSet event) {
 		// just send event to all FCP connections (not identities)
 		Iterator<PluginReplySender> iter = fcpConnections.iterator();
 		PluginReplySender sender;
 		while (iter.hasNext()) {
 			sender = iter.next();
 			if(sender != null) {
 				try {
 					sender.send(event);
 				} catch (PluginNotFoundException e) {
 					// FCP connection disconnected
 					// will be added again on next connection
 					iter.remove();
 				}
 			}
 		}
 	}
 	
 	private void parseGotChannelJoin(SimpleFieldSet event) {
 		// check for each fcp ident who is interested in this event
		String channel = "#" + event.get("channel");
 		fcp_ident fcpIdent;
 		String ident;
 		Iterator<String> iter = fcpIdents.keySet().iterator();
 		while(iter.hasNext()) {
 			ident = iter.next();
 			fcpIdent = fcpIdents.get(ident);
 			if (fcpIdent.replySender != null) {
				if(fcpIdent.govMode || mStorage.userMap.get(ident).channels.contains(channel)) {
 					event.putSingle("fn_key_public", ident);
 					try {
 						fcpIdent.replySender.send(event);
 					} catch (PluginNotFoundException e) {
 						// fcp connection disconnected
 						// replySender will be set again on next registerFCPidentity
 						fcpConnections.remove(fcpIdent.replySender);
 						fcpIdent.replySender = null;
 					}
 				}
 			}
 		}
 	}
 	
 	private void parseGotChannelPart(SimpleFieldSet event) {
 		// check for each fcp ident who is interested in this event
		String channel = "#" + event.get("channel");
 		fcp_ident fcpIdent;
 		String ident;
 		Iterator<String> iter = fcpIdents.keySet().iterator();
 		while(iter.hasNext()) {
 			ident = iter.next();
 			fcpIdent = fcpIdents.get(ident);
 			if (fcpIdent.replySender != null) {
				if(fcpIdent.govMode || mStorage.userMap.get(ident).channels.contains(channel)) {
 					event.putSingle("fn_key_public", ident);
 					try {
 						fcpIdent.replySender.send(event);
 					} catch (PluginNotFoundException e) {
 						// fcp connection disconnected
 						// replySender will be set again on next registerFCPidentity 
 						fcpConnections.remove(fcpIdent.replySender);
 						fcpIdent.replySender = null;
 					}
 				}
 			}
 		}
 	}
 	
 	private void parseGotMessage(SimpleFieldSet event) {
 		// check for each fcp ident who is interested in this event
 		// TODO: for govMode = true, also send private messages?
 		fcp_ident fcpIdent;
 		if("user".equals(event.get("type"))) {
 			// private message, only intended for one particular identity
 			String ident = event.get("destination");
 			if(fcpIdents.containsKey(ident))	{
 				fcpIdent = fcpIdents.get(ident);
				// TODO: rsa decrypt message based on senders public and fcpIdents private rsa_key
 				if (fcpIdent.replySender != null) {
 					event.putSingle("fn_key_public", ident);
 					try {
 						fcpIdent.replySender.send(event);
 					} catch (PluginNotFoundException e) {
 						// fcp connection disconnected
 						// replySender will be set again on next registerFCPidentity
 						fcpConnections.remove(fcpIdent.replySender);
 						fcpIdent.replySender = null;
 					}
 				}
 			}
		} else if("channel".equals(event.get("type"))) {
 			// channel message
			String channel = "#" + event.get("destination");
 			String ident;
 			Iterator<String> iter = fcpIdents.keySet().iterator();
 			while(iter.hasNext()) {
 				ident = iter.next();
 				fcpIdent = fcpIdents.get(ident);
 				if (fcpIdent.replySender != null) {
					if(fcpIdent.govMode || mStorage.userMap.get(ident).channels.contains(channel)) {
 						event.putSingle("fn_key_public", ident);
 						try {
 							fcpIdent.replySender.send(event);
 						} catch (PluginNotFoundException e) {
 							// fcp connection disconnected
 							// replySender will be set again on next registerFCPidentity
 							fcpConnections.remove(fcpIdent.replySender); 
 							fcpIdent.replySender = null;
 						}
 					}
 				}
 			}
		} else {
			// uh? type != channel && != user
 		}
 	}
 	
 	private void parseGotTopicChange(SimpleFieldSet event) {
 		// just send event to all FCP connections (not identities)
 		Iterator<PluginReplySender> iter = fcpConnections.iterator();
 		PluginReplySender sender;
 		while (iter.hasNext()) {
 			sender = iter.next();
 			if(sender != null) {
 				try {
 					sender.send(event);
 				} catch (PluginNotFoundException e) {
 					// FCP connection disconnected
 					// will be added again on next connection
 					iter.remove();
 				}
 			}
 		}
 	}
 	
 	private void parseGotKeepAlive(SimpleFieldSet event) {
 		// just send event to all FCP connections (not identities)
 		Iterator<PluginReplySender> iter = fcpConnections.iterator();
 		PluginReplySender sender;
 		while (iter.hasNext()) {
 			sender = iter.next();
 			if(sender != null) {
 				try {
 					sender.send(event);
 				} catch (PluginNotFoundException e) {
 					// FCP connection disconnected
 					// will be added again on next connection
 					iter.remove();
 				}
 			}
 		}
 	}
 }
