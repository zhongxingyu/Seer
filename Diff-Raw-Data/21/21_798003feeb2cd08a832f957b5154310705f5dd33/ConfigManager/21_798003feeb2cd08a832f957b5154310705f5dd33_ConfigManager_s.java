 package com.mcnsa.mcnsachat2.util;
 
 import com.mcnsa.mcnsachat2.MCNSAChat2;
 
 public class ConfigManager {
 	// store the main plugin for later access
 	static MCNSAChat2 plugin = null;
 	public ConfigManager(MCNSAChat2 instance) {
 		plugin = instance;
 	}
 	
 	public void load(String file) {
 		// TODO: load the config!
 	}
 
 	// create a "class" in here to store config options!
 	private class ConfigOptions {
 		public HashMap<String, ChannelHardConfig> hardChannels = new HashMap<String, ChannelHardConfig>();
 		public Float localChatRadius = new Float(200);
 		public String defaultChannel = new String("");
 		public ColourHandler defaultColour = new ColourHandler("grey");
		public Boolean announceTimeouts = new Boolean(True);
		public SpamConfig spamConfig = new SpamConfig(5, 3, 5);
 	}
 
 	// hard-channel configurations
 	private class ChannelHardConfig {
 		public String name = new String("");
 		public ColourHandler colour = new ColourHandler("grey");
 		public String permissions = new String("channel");
		public Boolean local = new Boolean(false);
 		public String alias = new String("");
 
		public ChannelHardConfig(String _name, ColourHandler _colour, String _perms, Boolean _local, String _alias) {
 			name = _name;
 			colour = _colour;
 			permissions = _permissions;
 			local = _local;
 			alias = _alias;
 		}
 	}
 
 	// spam configurations
 	private class SpamConfig {
 		public Float messageLimit = new Float(5);
 		public Float messagePeriod = new Float(3);
 		public Float timeoutTime = new Float(5);
 
 		public SpamConfig(Float limit, Float period, Float time) {
 			messageLimit = limit;
 			messagePeriod = period;
 			timeoutTime = time;
 		}
 	}
 }
