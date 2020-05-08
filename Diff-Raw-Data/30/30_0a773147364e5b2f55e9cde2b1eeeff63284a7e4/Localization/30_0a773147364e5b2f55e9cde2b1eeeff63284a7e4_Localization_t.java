 package net.amigocraft.TTT.localization;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
import net.amigocraft.TTT.TTT;

 import org.apache.commons.io.IOUtils;
 import org.bukkit.ChatColor;
 
 public class Localization {
 
 	public TTT plugin = TTT.plugin;
 
 	public String getMessage(String key){
		InputStream is = null;
		is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/" + TTT.lang + ".properties");
		if (is == null)
			is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/en-US.properties");
 		StringWriter writer = new StringWriter();
 		try {
 			IOUtils.copy(is, writer, "ISO-8859-1");
 			String contents = writer.toString();
 			String[] lines = contents.split("\n");
 			for (int i = 0; i < lines.length; i++){
 				String[] params = lines[i].split("\\|");
 				if (params[0].equalsIgnoreCase(key))
 					return params[1];
 			}
 		}
 		catch (IOException e){
 			e.printStackTrace();
 		}
 		return ChatColor.RED + "Could not get message from localization!";
 	}
 
 }
