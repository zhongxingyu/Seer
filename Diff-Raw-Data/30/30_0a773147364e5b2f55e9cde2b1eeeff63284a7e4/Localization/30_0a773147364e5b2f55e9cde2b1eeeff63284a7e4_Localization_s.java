 package net.amigocraft.TTT.localization;
 
import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import org.apache.commons.io.IOUtils;
 import org.bukkit.ChatColor;
 
import net.amigocraft.TTT.TTT;

 public class Localization {
 
 	public TTT plugin = TTT.plugin;
	public String lang = TTT.lang;
 
 	public String getMessage(String key){
		if (lang == null)
			lang = "en-US";
		InputStream is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/en-US.properties");
		if (new File("/net/amigocraft/TTT/localization") != null)
			is = Localization.class.getResourceAsStream("/net/amigocraft/TTT/localization/" + lang + ".properties");
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
