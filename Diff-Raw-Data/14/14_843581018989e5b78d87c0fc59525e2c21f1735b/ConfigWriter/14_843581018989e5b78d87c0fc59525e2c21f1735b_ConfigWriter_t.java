 package de.brightstorm.config;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
import java.io.Writer;
 
 import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
 import org.bukkit.craftbukkit.libs.com.google.gson.JsonIOException;
 import org.bukkit.plugin.Plugin;
 
 public class ConfigWriter {
 	private GsonBuilder gson;
 	private File config;
 	public ConfigWriter(Plugin pl) {
 		gson = new GsonBuilder();
 		gson.setPrettyPrinting();
 		config = new File(pl.getDataFolder() + System.getProperty("file.separator") + "config.json");
 	}
 	
 	public void write() throws JsonIOException, IOException {
 		Config c = new Config();
		Writer w = new FileWriter(config);
		gson.create().toJson(c, Config.class, w);
		w.close();
 	}
 }
