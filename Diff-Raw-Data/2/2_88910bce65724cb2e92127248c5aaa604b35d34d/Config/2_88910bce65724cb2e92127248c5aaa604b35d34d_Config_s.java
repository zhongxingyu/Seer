 package muCkk.DeathAndRebirth.config;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import muCkk.DeathAndRebirth.messages.Errors;
 
 public class Config {
 
 	private File configFile;
 	private CommentedConfiguration yml;
 	private DARProperties oldConfig;
 	
 	public Config(String dir, DARProperties oldConfig) {
 		this.configFile = new File(dir+"/config.yml");
 		this.oldConfig = oldConfig;
 		load();	
 	}
 	
 	/**
 	 * Loads the configuration
 	 */
 	public void load() {
 		if(!configFile.exists()){
 			InputStream in = getClass().getResourceAsStream("config.yml");
 			FileOutputStream out = null;
 			try {
 				out = new FileOutputStream(configFile);
 				byte [] buffer = new byte[1024];
 				int bytes;
 				
 				while((bytes = in.read(buffer)) != -1) {
 					out.write(buffer, 0, bytes);
 				} 
 			} catch (FileNotFoundException e) {
 				Errors.loadingConfig();
 				e.printStackTrace();
 			} catch (IOException e) {
 				Errors.loadingConfig();
 				e.printStackTrace();
 			} finally {
 				try {
 					in.close();
 					if (out != null) out.close();
 				} catch (IOException e) {
 					Errors.loadingConfig();
 					e.printStackTrace();
 				}
 			}
  
         } else {
         	// loaded
         }
 		try {
             yml = new CommentedConfiguration(configFile);
             yml.load();
         } catch (Exception e) {
         	Errors.loadingConfig();
 			e.printStackTrace();
         }
 	}
 
 	/**
 	 * Saves the current information to a file
 	 */
 	public void save() {
 		yml.addComment("BLOCK_GHOST_INTERACTION",
 				"###########################################################",
 				"#      Detailed information on how to edit the config     #",
 				"#                     can be found here:                  #",
 				"#  http://dev.bukkit.org/server-mods/death-and-rebirth/   #",
 				"###########################################################",
 				"",
 				"",
 				"###########################################################",
 				"#                         Ghost                           #",
 				"###########################################################");
 		yml.addComment("DROPPING", "",
 				"###########################################################",
 				"#                       Dropping                          #",
 				"###########################################################");
 		yml.addComment("HEALTH", "",
 				"###########################################################",
 				"#               Self-Resurrection effects                 #",
 				"###########################################################");
 		yml.addComment("MCMMO", "");
 		yml.addComment("NEED_ITEM", "",
 				"###########################################################",
 				"#                     Resurrection                        #",
 				"###########################################################");
 		yml.addComment("CORPSE_SPAWNING", "",
 				"###########################################################",
 				"#                       Spawning                          #",
 				"###########################################################");
 		yml.addComment("GRAVE_SIGNS", "",
 				"###########################################################",
 				"#                       Plugin                            #",
 				"###########################################################");
 		yml.addComment("GHOST_SOUND_EFFECTS", "",
 				"###########################################################",
 				"#                            Spout                        #",
 				"#                                                         #",
 				"# No need to edit this if you aren't using Spout.         #",
 				"# Remember to put a backslash infront of a colon in URLs. #",
 				"# Colors use RGB values, so 1;0;0 would be red.           #",
 				"###########################################################");
 		yml.addComment("SPOUT_ENABLED", "",
 				"###########################################################",
 				"#               Additional information                    #",
 				"#   These options are set by the plugin automatically     #",
 				"#             Do not edit below this line                 #",
 				"###########################################################");
 		
 		yml.save();
 	}
 		
 	/**
 	 * For boolean toggles of /dar
 	 * @param node which is toggled
 	 * @param b new value
 	 */
 	public void toggle(CFG node) {
 		if (yml.getBoolean(node.toString(), node.b())) yml.setProperty(node.toString(), false);
 		else yml.setProperty(node.toString(), true);
 //		save();
 	}
 	
 	// setter
 	public void set(CFG node, Object value) {
 		yml.setProperty(node.toString(), value);
 //		save();
 	}
 	public void set(String world, boolean b) {
 		yml.setProperty(world, b);
 //		save();
 	}
 	
 	// getter
 	public boolean getBoolean(CFG node) {
 		return yml.getBoolean(node.toString(), node.b());
 	}
 	/**
 	 * Trys to get the boolean value. Returns false if no value is found
 	 * @param path
 	 * @return
 	 */
 	public boolean getBoolean(String path) {
 		return yml.getBoolean(path, false);
 	}
 	public String getString(CFG node) {
		return yml.getString(node.toString());
 	}
 	public double getDouble(CFG node) {
 		return yml.getDouble(node.toString(), node.d());
 	}
 	public int getInt(CFG node) {
 		return yml.getInt(node.toString(), node.i());
 	}
 	public float [] getFloatColor(CFG node) {
 		float [] array = new float[3];
 		String [] strings = getString(node).split(";");
 		for(int i=0; i<strings.length; i++) {
 			array[i] = Float.valueOf(strings[i]);
 		}
 		return array;
 	}
 	
 	public List<String> getKeys(String path) {
 		return yml.getKeys(path);
 	}
 	
 	// checker for worlds
 	public boolean isEnabled(String world) {
 		return yml.getBoolean(world, false);
 	}
 	
 	/**
 	 * Check if the old config is converted
 	 * @return boolean
 	 */
 	public boolean isConverted() {
 		return yml.getBoolean("CONVERTED", false);
 	}
 	/**
 	 * Converts the old config into the new one
 	 */
 	public void convert() {
 		if (isConverted()) return;
 		
 		set(CFG.AMOUNT, oldConfig.getInteger("amount"));
 		set(CFG.BLOCK_GHOST_INTERACTION, oldConfig.isBlockGhostInteractionEnabled());
 		set(CFG.CORPSE_SPAWNING, !oldConfig.isReverseSpawningEnabled());
 		set(CFG.DISTANCE, oldConfig.getInteger("distance"));
 		set(CFG.DROPPING, oldConfig.isDroppingEnabled());
 		set(CFG.FLY, oldConfig.isFlyingEnabled());
 		set(CFG.FLY_SPEED, oldConfig.getFlySpeed());
 		set(CFG.GHOST_CHAT, oldConfig.isGhostChatEnabled());
 		set(CFG.GHOST_NAME, oldConfig.getGhostName());
 		set(CFG.GRAVE_SIGNS, oldConfig.isSignsEnabled());
 		set(CFG.HEALTH, oldConfig.getHealth());
 		set(CFG.ITEM_ID, oldConfig.getInteger("itemID"));
 		set(CFG.LIGHTNING_DEATH, oldConfig.isLightningDEnabled());
 		set(CFG.LIGHTNING_REBIRTH, oldConfig.isLightningREnabled());
 		set(CFG.NEED_ITEM, oldConfig.getBoolean("needItem"));
 		set(CFG.PERCENT, oldConfig.getPercent());
 		set(CFG.PVP_DROP, oldConfig.isPvPDropEnabled());
 		set(CFG.SHRINE_NOTE, oldConfig.isShrineMsgEnabled());
 		set(CFG.SHRINE_RADIUS, oldConfig.getShrineRadius());
 		set(CFG.SHRINE_ONLY, oldConfig.isShrineOnlyEnabled());
 		set(CFG.TIME, oldConfig.getTime());
 		set(CFG.VERSION_CHECK, oldConfig.isVersionCheckEnabled());
 		
 		// Spout
 		set(CFG.GHOST_SOUND_EFFECTS, oldConfig.isGhostSoundEnabled());
 		set(CFG.GHOST_SKIN, oldConfig.getGhostSkin());
 		set(CFG.DEATH_SOUND, oldConfig.getDeathSound());
 		set(CFG.REB_SOUND, oldConfig.getRebSound());
 		set(CFG.RES_SOUND, oldConfig.getResSound());
 		set(CFG.GHOST_TEXTPACK, oldConfig.getTextPack());
 		set(CFG.CHANGE_COLORS, oldConfig.changeColors());
 		set(CFG.GHOST_SKY, oldConfig.getString("ghostSky", CFG.GHOST_SKY.s()));
 		set(CFG.GHOST_FOG, oldConfig.getString("ghostFog", CFG.GHOST_FOG.s()));
 		set(CFG.GHOST_CLOUDS, oldConfig.getString("ghostClouds", CFG.GHOST_CLOUDS.s()));
 		
 		// third party plugins
 		set(CFG.SPOUT_ENABLED, oldConfig.isSpoutEnabled());
 		set(CFG.CITIZENS_ENABLED, oldConfig.isCitizensEnabled());
 		
 		yml.setProperty("CONVERTED", true);
 		save();
 	}
 }
