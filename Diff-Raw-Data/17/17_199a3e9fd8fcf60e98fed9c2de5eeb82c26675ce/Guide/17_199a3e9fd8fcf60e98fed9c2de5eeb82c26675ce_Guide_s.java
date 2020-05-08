 package net.dockter.sguide.guide;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 
 
 public class Guide {
 
 	public static Guide load(File file) {
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
 		return new Guide(config.getString("Name"),config.getString("Date"),config.getString("Text"));
 	}
 	private String name, date, text;
 	
 	public Guide(String name, String date, String text) {
 		this.name = name;
 		this.date = date;
 		this.text =  text;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getDate() {
 		return date;
 	}
 	
 	public String getText() {
 		return text;
 	}
 
 	public void save() {
		File toSave = new File("plugins"+File.separator+"SpoutGuide"+File.separator+"guides"+File.separator+name);
 		if(!toSave.exists())
 			try {
 			toSave.createNewFile();
 		} catch (IOException ex) {
 			Logger.getLogger(Guide.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(toSave);
 		config.set("Name", name);
 		config.set("Date", date);
 		config.set("Text", text);
 		try {
 			config.save(toSave);
 		} catch (IOException ex) {
 			Logger.getLogger(Guide.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		
 	}
 
 	public void delete() {
		File toSave = new File("plugins"+File.separator+"SpoutGuide"+File.separator+"guides"+name);
 		toSave.delete();
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public void setDate(String format) {
 		this.date = format;
 	}
 
 	public void setName(String text) {
 		this.name = text;
 	}
 }
