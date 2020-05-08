 package fr.aumgn.bukkitutils.gconf;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 
 import org.bukkit.plugin.Plugin;
 
 import com.google.gson.Gson;
 import com.google.gson.stream.JsonReader;
 
 public class GConfLoader {
 
     private final Gson gson;
     private final Plugin plugin;
 
     public GConfLoader(Gson gson, Plugin plugin) {
         this.gson = gson;
         this.plugin = plugin;
     }
 
     private Charset charset() {
         if (Charset.isSupported("UTF-8")) {
             return Charset.forName("UTF-8");
         }
         return Charset.defaultCharset();
     }
 
     public <T> T loadOrCreate(String filename, Class<T> klass)
             throws GConfLoadException {
 
         try {
             File folder = plugin.getDataFolder();
             if (!folder.exists() && !folder.mkdirs()) {
                 throw new GConfLoadException(
                         "Impossible de créer le dossier :" + folder.getPath());
             }
 
            File file = new File(plugin.getDataFolder(), filename);
             T config;
             if (file.createNewFile()) {
                 config = klass.newInstance();
             } else {
                 config = load(file, klass);
             }
 
             // This ensures user file is updated with newer fields. 
             write(file, config);
 
             return config;
         } catch (IOException exc) {
             throw new GConfLoadException(exc);
         } catch (InstantiationException exc) {
             throw new GConfLoadException(exc);
         } catch (IllegalAccessException exc) {
             throw new GConfLoadException(exc);
         }
     }
 
     private <T> T load(File file, Class<T> klass) throws IOException {
         InputStreamReader isr = new InputStreamReader(
                 new FileInputStream(file), charset());
         JsonReader reader = new JsonReader(isr);
 
         try {
             return gson.fromJson(reader, klass);
         } finally {
             reader.close();
         }
     }
 
     private void write(File file, Object object) throws IOException {
         OutputStreamWriter osw = new OutputStreamWriter(
                 new FileOutputStream(file), charset());
         BufferedWriter writer = new BufferedWriter(osw);
 
         try {
             writer.write(gson.toJson(object));
         } finally {
             writer.close();
         }
     }
 }
