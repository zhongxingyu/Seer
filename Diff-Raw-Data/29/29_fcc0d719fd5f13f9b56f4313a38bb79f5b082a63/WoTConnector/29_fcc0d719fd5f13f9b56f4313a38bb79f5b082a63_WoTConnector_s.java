 package org.freenetproject.plugin.dvcs_webui.main;
 
 import freenet.pluginmanager.FredPluginTalker;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.pluginmanager.PluginTalker;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Maintains information from WoT.
  */
 public class WoTConnector implements FredPluginTalker {
 
 	private static final Logger log = LogManager.getLogger();
 
 	/**
 	 * Seconds between checking whether WoT is accessible during startup.
 	 */
 	private static final long WoT_STARTUP_POLL = 10;
 
 	/**
 	 * Seconds between checking WoT for updates, This concept will hopefully be obsoleted by the event-notifications
 	 * branch.
 	 */
 	private static final long WoT_POLL = 30;
 
 	/**
 	 * [nickname]@[identity ID] for each local identity.
 	 */
 	private final HashSet<String> local_identifiers;
 
 	/**
 	 * Create a connection with WoT, and keep information from it up to date.
 	 * @param pr used to connect to WoT.
 	 */
	public WoTConnector(PluginRespirator pr) {
		final PluginTalker pt = connect(pr);

		final SimpleFieldSet sfs = new SimpleFieldSet(false);
		sfs.putOverwrite("Message", "GetOwnIdentities");

 		local_identifiers = new HashSet<String>();
 
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.scheduleWithFixedDelay(new Runnable() {
 			@Override
 			public void run() {
				pt.send(sfs, null);
 			}
		}, 0, WoT_POLL, TimeUnit.SECONDS);
 	}
 
 	@Override
 	public void onReply(String pluginname, String identifier, SimpleFieldSet params, Bucket data) {
 		Iterator<String> keyIterator = params.keyIterator();
 		synchronized (local_identifiers) {
 			local_identifiers.clear();
 
 			while (keyIterator.hasNext()) {
 				final String key = keyIterator.next();
 				if (key.startsWith("Nickname")) {
 					final String nickname = params.get(key);
 					/*
 					 * Key is Nickname<number>, where the number is shared between elements for that identity.
 					 * The string "Nickname" is 8 characters long.
 					 */
 					final String index = key.substring(8);
 					final String identity_id = params.get("Identity" + index);
 					local_identifiers.add(nickname + "@" + identity_id);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return nickname@identity id for all local identities.
 	 */
 	public Collection<String> getLocalIdentifiers() {
 		synchronized (local_identifiers) {
 			return local_identifiers;
 		}
 	}
 
 	private PluginTalker connect(PluginRespirator pr) {
 		PluginTalker pt = null;
 
 		do {
 			try {
 				pt = pr.getPluginTalker(this, "plugins.WebOfTrust.WebOfTrust", "dvcs_webui");
 			} catch (PluginNotFoundException e) {
 				log.info("WoT is not started. Waiting {} seconds.", WoT_STARTUP_POLL);
 
 				// Wait until WoT is loaded.
 				try {
 					Thread.sleep(TimeUnit.SECONDS.toMillis(WoT_STARTUP_POLL));
 				} catch (InterruptedException f) {
 				}
 			}
 		} while (pt == null);
 
 		return pt;
 	}
 }
