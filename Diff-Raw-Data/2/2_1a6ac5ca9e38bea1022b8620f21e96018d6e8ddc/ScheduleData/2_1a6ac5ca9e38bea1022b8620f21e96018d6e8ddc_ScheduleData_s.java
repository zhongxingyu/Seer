 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nationsmc.chunkrefresh.scheduler;
 
 import com.nationsmc.chunkrefresh.ChunkRefresh;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.OpenOption;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 
 /**
  *
  * @author tarlach
  */
 public class ScheduleData {
 
     protected long time;
     protected int chunkx, chunkz;
     protected Location loc;
     protected File datafile;
     protected Path path;
     protected List<String> raws;
     protected long lastUpdate;
     public ChunkRefresh plugin;
 
     public ScheduleData(File flatfile, ChunkRefresh plugin) {
         this.plugin = plugin;
 
         this.datafile = flatfile;
         //Since null is not valid as a comparison to a primitive...
         this.chunkx = Integer.MAX_VALUE;
         this.chunkz = Integer.MAX_VALUE;
         this.time = Long.MAX_VALUE;
 
     }
 
     public ScheduleData(Chunk chunk, long time) {
         plugin.debug(ChunkRefresh.defaultFlatFileDir);
         this.datafile = new File(ChunkRefresh.defaultFlatFileDir + "/");
         this.chunkx = chunk.getX();
         this.time = time;
     }
 
     public void save() throws IOException {
         if (this.datafile.canWrite()) {
             this.path = Paths.get(this.datafile.getAbsolutePath());
 //            this.raws = Files.readAllLines(path,);
            FileWriter fstream = new FileWriter("out.txt");
 //            BufferedWriter out = new BufferedWriter(fstream);s
             if (this.validate()) {
             } else {
                 
                 BufferedWriter buff = new BufferedWriter(fstream);;
                 String out = "" + Integer.toString(chunkx) + "." + Integer.toString(chunkz) + "\n" + Long.toString(time) + "\n";
                 buff.write(out, 0, out.length());
 //                buff.flush();
                 buff.close();
             }
 
         }
     }
 
     public void load() throws IOException {
         if (this.datafile.canRead()) {
             this.path = Paths.get(this.datafile.getAbsolutePath());
             this.raws = Files.readAllLines(path, null);
         } else {
             throw new IOException();
         }
     }
 
     public void parse() {
         if (!raws.isEmpty()) {
             for (String str : raws) {
                 if (str.matches("(\\d+).*?(\\d+)\n")) {
                     String[] temp = str.split(".*?");
                     this.chunkx = Integer.decode(temp[0]);
                     this.chunkz = Integer.decode(temp[1]);
                 } else if (str.matches("(\\d+)\n")) {
                     this.time = Long.decode(str);
 
                 } else {
                     Bukkit.getLogger().log(Level.INFO, "[ChunkRefresh] unparsable raw string at file: {0}, Ignoring...", this.datafile.getAbsolutePath());
                 }
             }
             if (this.time == Long.MAX_VALUE || this.chunkx == Integer.MAX_VALUE || this.chunkz == Integer.MAX_VALUE) {
                 Bukkit.getLogger().log(Level.SEVERE, "[ChunkRefersh] raws from file{0} cannot be parsed!", this.datafile.getAbsolutePath());
             }
         }
     }
 
     public void reset() {
         this.time += plugin.waitTime;
     }
 
     public boolean validate() {
         List<Boolean> validItems = null;
         for (String str : raws) {
             if (str.matches("(\\d+).*?(\\d_)\n")) {
                 String[] temp = str.split(".*?");
                 if (temp[0].equals(Integer.toString(this.chunkx))) {
                     validItems.add(Boolean.TRUE);
                 } else {
                     return false;
                 }
                 if (temp[1].equals(Integer.toString(this.chunkz))) {
                     validItems.add(Boolean.TRUE);
                 } else {
                     return false;
                 }
 
             } else if (str.matches("\\d+")) {
                 if (str.equals(Long.toString(this.time))) {
                     validItems.add(Boolean.TRUE);
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         }
         if (validItems.size() == 3) {
             for (Boolean bool : validItems) {
                 if (bool != true) {
                     return false;
                 }
             }
         } else {
             return false;
         }
         return true;
     }
 }
