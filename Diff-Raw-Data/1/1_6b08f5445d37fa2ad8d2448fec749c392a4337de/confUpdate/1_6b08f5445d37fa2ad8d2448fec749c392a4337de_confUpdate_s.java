 package nl.giantit.minecraft.giantpm.core.Updater.Config;
 
 import nl.giantit.minecraft.giantpm.GiantPM;
 import nl.giantit.minecraft.giantpm.core.Updater.iUpdater;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.logging.Level;
 
 public class confUpdate implements iUpdater {
 	
 	private void export(File file, FileConfiguration c) {
 		try {
 			InputStream iS = new ByteArrayInputStream(c.saveToString().replace("\n", "\r\n").replace("  ", "    ").getBytes("UTF-8"));
 			GiantPM.getPlugin().extract(file, iS);
 		}catch(UnsupportedEncodingException e) {
 			GiantPM.getPlugin().getLogger().severe("Failed to update config file!");
 			if(c.getBoolean("GiantTitle.global.debug", true) == true) {
 				GiantPM.getPlugin().getLogger().log(Level.INFO, e.getMessage(), e);
 			}
 		}
 	}
 	
         public void update0_3(FileConfiguration c) {
             c.set("GiantPM.global.opHasPerms", null);
             c.set("GiantPM.permissions.permissionsEngine", null);
             
             c.set("GiantPM.permissions.Engine", "SPERM");
             c.set("GiantPM.permissions.opHasPerms", true);
             
             c.createSection("GiantPM.messaging.admin");
             c.set("GiantPM.messaging.admin.group", "admin");
             
             c.set("GiantPM.global.version", 0.3);
             this.export(new File(GiantPM.getPlugin().getDir(), "conf.yml"), c);
         }
         
 	public void Update(double curV, FileConfiguration c) {
             if (curV < 0.3) {
                 GiantPM.getPlugin().getLogger().info("Your conf.yml has ran out of date. Updating to 0.3!");
                 update0_3(c);
             }
         }
 }
