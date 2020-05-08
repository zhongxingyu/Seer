 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.q1cc.cfs.tusync.struct;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.HashMap;
 
 /**
  *
  * @author claus
  */
 public class Title {
     public final static String[] attribNames = {"Track ID", "Persistent ID",
         "Album Rating Computed", "Kind", "Year", "File Folder Count", "Genre",
         "Date Modified", "Album Rating", "Artist", "Play Date", "Album", "Size",
         "Play Date UTC", "Comments", "Sample Rate", "Location", "Bit Rate",
         "Date Added", "Album Artist", "Name", "Library Folder Count", "Track Type",
         "Total Time", "Play Count","Skip Date", "Skip Count", "Track Number",
         "Artwork Count","Composer", "Track Count", "Disc Number", "Rating",
         "Sort Album", "Compilation", "BPM", "Volume Adjustment", "Sort Name",
         "Sort Artist", "Sort Album Artist", "Disc Count", "Grouping", "HD",
         "Video Height", "Video Width", "Movie", "Has Video", "TV Show", "Series",
         "Season", "Start Time", "Sort Composer", "Episode Order", "Content Rating",
         "Release Date", "Protected", "Purchased", "Episode", "Stop Time",
         "Equalizer", "Unplayed", "iTunesU", "Part Of Gapless Album", "Explicit",
        "Sort Series", "Podcast", "Rating Computed"
     }; //TODO find out names
    //TODO ignore some attributes?

     public final static HashMap<String,Integer> attribIndexes;
     
     static {
         attribIndexes = new HashMap<String, Integer>(attribNames.length);
         for(int i=0;i<attribNames.length;i++) {
             attribIndexes.put(attribNames[i], i);
         }
     }
     
     public static int getAttInd(String name) {
         Integer i = attribIndexes.get(name);
         if(i==null) {
             System.out.println("ERROR: could not find attribute for "+name);
             return -1;
         }
         return i;
     }
     
     public long id;
     public Object[] attribs;
     public boolean selected;
     public volatile boolean fileChecked;
     public static volatile String baseFolder;
     
     public Title(long id) {
         this.id = id;
         attribs = new Object[attribNames.length];
     }
     
     public long getSizeOnDisk() {
         if(fileChecked) {
             long l = (Long)attribs[getAttInd("Size")];
             if(l==0) {
                 selected=false;
             }
             return l;
         }
         
         Object kind = attribs[getAttInd("Kind")];
         if(kind != null && kind instanceof String && ((String)kind).endsWith("Stream")) {
             selected = false;
             attribs[getAttInd("Size")] = 0L;
             fileChecked = true;
             return 0;
         }
         Object loc = attribs[attribIndexes.get("Location")];
         if(loc == null) {
             System.out.println("no location for "+toString());
             attribs[getAttInd("Size")] = 0L;
             fileChecked = true;
             return 0;
         }
         if(!(loc instanceof String)) {
             System.out.println("location not valid "+toString());
             attribs[getAttInd("Size")] = 0L;
             fileChecked = true;
             return 0;
         }
         
         File f;
         String location = decodeLocation((String)loc);
         f = new File(location);
         if(!f.exists()) {
             System.out.println("not found "+f.getAbsolutePath()+" in "+toString());
             attribs[getAttInd("Size")] = 0L;
             fileChecked = true;
             return 0;
         }
         if(!f.canRead()) {
             System.out.println("can't read: "+toString());
             attribs[getAttInd("Size")] = 0L;
             attribs[attribIndexes.get("Location")] = location;
             fileChecked = true;
             return 0;
         }
         attribs[attribIndexes.get("Location")] = f.getAbsolutePath();
         long size = f.length();
         attribs[getAttInd("Size")] = size;
         fileChecked = true;
         return size;
     }
 
     public String getFile() {
         if(fileChecked)
         {
             return (String)attribs[attribIndexes.get("Location")];
         }
         getSizeOnDisk();
         return (String)attribs[attribIndexes.get("Location")];
     }
     
     public long getLength() {
         Object l = attribs[getAttInd("Total Time")];
         if(l instanceof Integer) {
             return (Integer)l;
         }
         if(l instanceof Long) {
             return (Long)l;
         }
         System.out.println("time: "+l+" for "+toString());
         
         return -1;
     }
     
     @Override
     public String toString() {
         StringBuilder b = new StringBuilder();
         b.append("[");
         for(int i=0;i<attribNames.length;i++) {
             b.append(attribNames[i]).append(":").append(attribs[i]).append(";");
         }
         b.append("]");
         return b.toString();
     }
 
     public static String decodeLocation(String location) {
         if(location.startsWith("file://localhost/")) {
             location = location.substring("file://localhost/".length());
         }
         try {
             return URLDecoder.decode(location.replaceAll("\\+","%2b"),"UTF-8");
         } catch (UnsupportedEncodingException ex) {
             ex.printStackTrace();
         }
         return location;
     }
     
     public String getPathRelative() {
         String pathAbsolute = getFile();
         if(pathAbsolute == null)
         {
             return null;
         }
         String without = pathAbsolute.replace(baseFolder, "");
         if(without.equals(pathAbsolute)) {
             System.out.println("relativePath: "+pathAbsolute+" does not contain "+baseFolder);
             selected = false;
         }
         return without;
     }
 }
