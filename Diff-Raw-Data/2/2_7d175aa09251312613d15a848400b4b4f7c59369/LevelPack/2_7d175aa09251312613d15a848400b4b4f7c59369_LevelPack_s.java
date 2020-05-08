 package org.spacebar.escape.common;
 
 import java.io.*;
 
 public class LevelPack {
     /* Defines a format for packing levels into a single seekable file.
      * 
      * Integer format: MSB first
      * 
      * Format of file:
      * 
      * Bytes 0--3: Unsigned int representing number of files packed in
      *            and number of shorts in header
     * Bytes 4--(2*n): Header (described below)
      * After header: Standard .esx files, represented directly. There are n
      *               levels here.
      *               
      * Header size: 4*n
      *   Header contains n unsigned ints. Each int represents the number of
      *   bytes in each level.
      */
 
     
     private final String resource;
     private DataInputStream in;
     private int levelsRead;
     private final int sizes[];
     private byte currentData[];
     
     
     public LevelPack(String resource) throws IOException {
         this.resource = resource;
         initStream();
         int levelCount = in.readInt();
         sizes = new int[levelCount];
         initSizes();
     }
 
     private void initSizes() throws IOException {
         for (int i = 0; i < sizes.length; i++) {
             sizes[i] = in.readInt();
         }
     }
 
     public void reset() {
         try {
             initStream();
             
             // skip headers and sizes
             int bytesToSkip = 4 * (sizes.length + 1);
             skipBytesFully(bytesToSkip);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void skipBytesFully(int bytesToSkip) throws IOException {
         while (bytesToSkip > 0) {
             bytesToSkip -= in.skipBytes(bytesToSkip);
         }
     }
 
     public Level getNextLevel() throws IOException {
         if (levelsRead == sizes.length) {
             return null;
         }
 
         loadData();
         
         return new Level(new BitInputStream(new ByteArrayInputStream(currentData)));
     }
 
     public byte[] getNextLevelData() throws IOException {
         if (levelsRead == sizes.length) {
             return null;
         }
 
         loadData();
 
         return currentData;
     }
     
     private void loadData() throws IOException {
         currentData = new byte[sizes[levelsRead]];
         levelsRead++;
         in.readFully(currentData);
     }
     
     public Level.MetaData getNextLevelMetaData() throws IOException {
         if (levelsRead == sizes.length) {
             return null;
         }
 
         loadData();
         
         return Level.getMetaData(new BitInputStream(new ByteArrayInputStream(currentData)));
     }
     
     public void skipLevels(int num) throws IOException {
         while (num > 0 && levelsRead != sizes.length) {
             int numBytes = sizes[levelsRead];
             levelsRead++;
             skipBytesFully(numBytes);
             num--;
         }
     }
     
     private void initStream() throws IOException {
         if (in != null) {
             in.close();
         }
         levelsRead = 0;
         in = new BitInputStream(getClass().getResourceAsStream(resource));
     }
     
     public int getLevelCount() {
         return sizes.length;
     }
 
     public int getLevelsRead() {
         return levelsRead;
     }
     
     static public void pack(InputStream levels[], OutputStream pack) throws IOException {
         DataOutputStream out = new DataOutputStream(pack);
         
         byte data[][] = new byte[levels.length][];
         
         // write number of files
         out.writeInt(levels.length);
         
         // read in all levels
         for (int i = 0; i < levels.length; i++) {
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             int d;
             while ((d = levels[i].read()) != -1) {
                 b.write(d);
             }
             // got a level
             data[i] = b.toByteArray();
             
             // write the size
             out.writeInt(data[i].length);
         }
         
         // have all levels, now write the rest
         for (int i = 0; i < data.length; i++) {
             out.write(data[i]);
         }
         
         // done!
         out.flush();
     }
 }
