 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nationsmc.chunkrefresh.scheduler;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
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
     protected int chunkx,chunkz;
     protected Location loc;
     protected File datafile;
     protected Path path;
     protected List<String> raws;
     protected long lastUpdate;
     public ScheduleData(File flatfile)  {
             this.datafile = flatfile;
             //Since null is not valid as a comparison to a primitive...
             this.chunkx = Integer.MAX_VALUE;
             this.chunkz = Integer.MAX_VALUE;
             this.time = Long.MAX_VALUE;
             
     }
     public ScheduleData(Chunk chunk , long time) {
         this.datafile = new File(SchedulerPool.defaultFlatFileDir + chunk.getX() + "," + chunk.getZ() + ".sch");
         this.chunkz = chunk.getX();
         this.chunkx = chunk.getX();
         this.time = time;
     }
     public void save() throws IOException {
         if(this.datafile.canWrite()){
             this.path = Paths.get(this.datafile.getAbsolutePath());
             this.raws = Files.readAllLines(path, null);
            if(this.validate()){
            } else {
                BufferedWriter buff = Files.newBufferedWriter(path, null, (OpenOption) null);
                String out = "" + Integer.toString(chunkx) + "." + Integer.toString(chunkz) + "\n" + Long.toString(time) + "\n";
                buff.write(out, 0,out.length() );
                buff.flush();
                buff.close();
            }
 
         }
     }
     public void load() throws IOException {
         if(this.datafile.canRead()){
           this.path = Paths.get(this.datafile.getAbsolutePath());
           this.raws = Files.readAllLines(path, null);
         } else {
            throw new IOException();
         }
     }
     public void parse() {
         if(!raws.isEmpty()){
             for(String str : raws){
                 if(str.matches("(\\d+).*?(\\d+)\n")) {
                    String[] temp =  str.split(".*?");
                    this.chunkx = Integer.decode(temp[0]);
                    this.chunkz = Integer.decode(temp[1]);
                 } else if (str.matches("(\\d+)\n")){
                     this.time = Long.decode(str);
                 
                 } else {
                     Bukkit.getLogger().log(Level.INFO, "[INFO] unparsable raw string at file: {0}, Ignoring...", this.datafile.getAbsolutePath());
                 }
             }
             if(this.time == Long.MAX_VALUE || this.chunkx == Integer.MAX_VALUE || this.chunkz == Integer.MAX_VALUE){
                 Bukkit.getLogger().log(Level.INFO, "[SEVERE] raws from file {0} cannot be parsed!", this.datafile.getAbsolutePath());
             }
         }
     }
 
    public void reset(){
        this.time += 86400000;
    }
     public boolean validate() {
         List<Boolean> validItems = null;
         for(String str : raws){
             if(str.matches("(\\d+).*?(\\d_)\n")){
                 String[] temp = str.split(".*?");
                 if(temp[0].equals(Integer.toString(this.chunkx))){
                     validItems.add(Boolean.TRUE);
                 } else {
                     return false;
                 }
                 if(temp[1].equals(Integer.toString(this.chunkz))){
                     validItems.add(Boolean.TRUE);
                 } else {
                     return false;
                 }
                 
             } else if(str.matches("\\d+")){
                 if(str.equals(Long.toString(this.time))){
                     validItems.add(Boolean.TRUE);
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         }
         if(validItems.size() == 3){
             for(Boolean bool : validItems){
                 if(bool != true)
                 {
                     return false;
                 }
             }
         } else {
             return false;
         }
         return true;
     }
   
 }
