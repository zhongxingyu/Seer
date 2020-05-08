 /**
  * jizzer - a simple jabber bot for managing file transfers in multi user chats
  * Copyright (C) 2012  lopho, b1gmct5
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.lopho.jizzer;
 
 import java.util.HashMap;
 
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.filetransfer.FileTransferManager;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.lopho.jizzer.filetransfer.JizzFileTransferListener;
 import org.lopho.jizzer.filetransfer.JizzTransfer;
 import org.lopho.jizzer.message.JizzChatManagerListener;
 import org.lopho.jizzer.message.JizzCommand;
 import org.lopho.jizzer.message.JizzMessageListener;
 import org.lopho.jizzer.util.JizzConfig;
 import org.lopho.jizzer.util.JizzLogger;
 
 /**
  * jizzer - a simple jabber bot for managing file transfers in multi user chats
  * @author lopho
  * @author b1gmct5
  */
 public class Jizzer {
 	private JizzConfig conf;
 	private ConnectionConfiguration connConf;
 	private Connection conn;
 	private FileTransferManager ftm;
 	private JizzMessageListener ml;
 	private JizzChatManagerListener cml;
 	private JizzFileTransferListener ftl;
 	private MultiUserChat muc;
 	private HashMap<String, String> dft; //Discrete File Transfers
 	private JizzLogger log;
 	private boolean run;
 	private boolean isPaused;
 	private int delta;
 	private final int defaultDelta;
 	
 	/**
 	 * @param jizzConfig
 	 */
 	public Jizzer(JizzConfig jizzConfig) {
 		log = new JizzLogger();
 		log.add("[system][start] starting");
 		conf = jizzConfig;
 		connConf = new ConnectionConfiguration(conf.getServer(), 5222);
 		conn = new XMPPConnection(connConf);
 		ml = new JizzMessageListener(log);
 		cml = new JizzChatManagerListener(ml, log);
 		ftl = new JizzFileTransferListener(conf.getFolder(), log);		
 		run = true;
 		isPaused = false;
 		delta = 1000;
 		defaultDelta = delta;
 		dft = new HashMap<String, String>();
 	}
 	
 	/**
 	 * @throws XMPPException
 	 */
 	private void init() throws XMPPException {
 		log.add("[system][start] connecting to " + conf.getServer());
 		conn.connect();
 		ftm = new FileTransferManager(conn);
 		ftm.addFileTransferListener(ftl);
 		conn.getChatManager().addChatListener(cml);
 		
 		log.add("[system][start] login as " + conf.getUser() + "@" + conf.getServer());
 		conn.login(conf.getUser(), conf.getPassword(), "jizzer");
 		
 		muc = new MultiUserChat(conn, conf.getMUC());
 		log.add("[system][start] joining MUC " + conf.getMUC() + " as " + conf.getNick());
 		muc.join(conf.getNick());
 		log.add("[system] running...");
 		
 	}
 	
 	/**
 	 * @throws InterruptedException
 	 * @throws XMPPException
 	 */
 	public void run() throws InterruptedException, XMPPException {
 		init();
 		while (run)
 		{
 			update();
 			Thread.sleep(delta);
 		}
 		
 		conn.disconnect();
 	}
 	
 	/**
 	 * @throws XMPPException
 	 */
 	private void update() throws XMPPException {
 		for (JizzTransfer t : ftl.update()) {
 			String m = conf.getUrl() + t.getTransfer().getFileName();
 			conn.getChatManager().createChat(t.getPeer(), ml).sendMessage(m);
 			log.add("[sent][" + t.getPeer() + "] " + m);
 			m = t.getPeer().split("@")[0] + " sent file: " + conf.getUrl() + t.getTransfer().getFileName();
 		    if (dft.containsKey(t.getPeer())) {
 		    	conn.getChatManager().createChat(dft.get(t.getPeer()), ml).sendMessage(m);
 		    } else {
 		    	muc.sendMessage(m);	
 		    }
 		    log.add("[sent][" + conf.getMUC() + "] " + m);
 		}
 		
 		while (ml.hasNext()) {
 			JizzCommand jizzCommand = ml.next();
 			String command = jizzCommand.getCommand();
 			String[] options = jizzCommand.getOptions();
			boolean admin = conn.getRoster().getGroup("Jizzer").contains(jizzCommand.getPeer());
 			log.add("[command][" + jizzCommand.getPeer() + "][Admin: " + admin + "][" + command + "]");
 			if (command.equals("stop") && admin) {
 				stop();
 				break;
 			} else if (command.equals("resume") && isPaused && admin) {
 				resume();
 			} else if (!isPaused) {
 				if (command.equals("pause") && admin) {
 					pause();
 				} else if (command.equals("ping")) {
 					jizzCommand.getChat().sendMessage("**pong**");
 					log.add("[sent][" + jizzCommand.getPeer() + "] **pong**");
 				} else if (command.equals("next")) {
 					if (options != null) {
 						log.add("[next]["+jizzCommand.getPeer()+"] Next-Request to: " + options[0]);
 						dft.put(jizzCommand.getPeer(), options[0]);
 					}
 				}
 			}
 		}
 	}
 		
 	/**
 	 * @return config
 	 */
 	public JizzConfig getConfig() {
 		return conf.clone();
 	}
 	
 	/**
 	 * 
 	 */
 	public void stop() {
 		setDelta(0);
 		this.run = false;
 		log.add("[system] recieved stop signal - shutting down");
 	}
 	
 	/**
 	 * @param immediatly
 	 */
 	public void stop(boolean immediatly) {
 		stop();
 		if (immediatly) {
 			conn.disconnect();
 		}
 	}
 	
 	/**
 	 * @param delta
 	 */
 	public void setDelta(int delta) {
 		this.delta = delta;
 	}
 	
 	/**
 	 * 
 	 */
 	public void pause() {
 		setDelta(10000);
 		ftl.deny();
 		isPaused = true;
 		log.add("[system] recieved pause signal - pausing");
 	}
 	
 	/**
 	 * 
 	 */
 	public void resume() {
 		setDelta(defaultDelta);
 		ftl.allow();
 		isPaused = false;
 		log.add("[system] recieved resume signal - resuming");
 	}
 }
