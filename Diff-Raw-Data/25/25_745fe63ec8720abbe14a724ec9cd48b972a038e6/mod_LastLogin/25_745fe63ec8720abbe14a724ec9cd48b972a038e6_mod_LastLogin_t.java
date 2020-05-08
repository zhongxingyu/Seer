 package btwmod.lastlogin;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.Set;
 import java.util.logging.Level;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.PlayerAPI;
 import btwmods.io.Settings;
 import btwmods.player.IPlayerInstanceListener;
 import btwmods.player.PlayerInstanceEvent;
 import btwmods.util.CaselessKey;
 
 public class mod_LastLogin implements IMod, IPlayerInstanceListener {
 	
	private static final String SECTION_NAME = "LastLogin";
	
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMMM d, yyyy");
 	private static final SimpleDateFormat timezoneFormat = new SimpleDateFormat("zzz (Z)");
 	
 	private int failures = 0;
 	private File lastLogin = new File(ModLoader.modDataDir, "lastlogin.txt");
 	private Settings data;
 
 	@Override
 	public String getName() {
 		return "Last Login";
 	}
 
 	@Override
 	public void init(Settings settings, Settings data) throws Exception {
 		this.data = data;
 		
 		if (settings.hasKey("lastLoginFile"))
 			lastLogin = new File(settings.get("lastLoginFile"));
 		
 		PlayerAPI.addListener(this);
 		
 		save();
 	}
 
 	@Override
 	public void unload() throws Exception {
 		PlayerAPI.removeListener(this);
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 
 	@Override
 	public void onPlayerInstanceAction(PlayerInstanceEvent event) {
 		if (lastLogin == null)
 			return;
 		
 		if (event.getType() == PlayerInstanceEvent.TYPE.LOGIN) {
			data.setLong(SECTION_NAME, event.getPlayerInstance().username, System.currentTimeMillis());
 			data.saveSettings(this);
 			save();
 		}
 	}
 	
 	private void save() {
 		JsonObject json = new JsonObject();
 		Date now = new Date();
 		json.addProperty("updated", dateFormat.format(now));
 		json.addProperty("timezone", timezoneFormat.format(now));
 		
 		JsonArray players = new JsonArray();
		Set<CaselessKey> keys = data.getSectionKeys(SECTION_NAME);
		if (keys != null) {
			for (CaselessKey key : keys) {
				long lastLogin = data.getLong(SECTION_NAME, key.key, -1);
				if (data.isLong(SECTION_NAME, key.key)) {
					JsonObject player = new JsonObject();
					player.addProperty("name", key.key);
					player.addProperty("date", dateFormat.format(new Date(lastLogin)));
					players.add(player);
				}
 			}
 		}
 		
 		json.add("players", players);
 		
 		BufferedWriter writer = null;
 		try {
 			writer = new BufferedWriter(new FileWriter(lastLogin, false));
 			writer.write(json.toString());
 			
 		} catch (IOException e) {
 			ModLoader.outputError(e, getName() + " failed to save:" + e.getMessage(), Level.SEVERE);
 			failures++;
 			
 			if (failures >= 10) {
 				ModLoader.outputError(e, getName() + " failed to save 10 times and has been disabled.", Level.SEVERE);
 				try {
 					lastLogin = null;
 					unload();
 				} catch (Exception e2) {
 					
 				}
 			}
 		}
 		
 		finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException e) {
 					
 				}
 			}
 		}
 	}
 
 }
