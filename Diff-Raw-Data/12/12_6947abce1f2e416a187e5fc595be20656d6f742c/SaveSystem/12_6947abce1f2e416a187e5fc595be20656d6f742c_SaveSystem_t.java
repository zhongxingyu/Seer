 
 package com.codisimus.plugins.chunkown;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Holds ChunkOwn data and is used to load/save data
  * 
  * @author Cody
  */
 class SaveSystem {
     public static Object[][] matrix = new Object[100][100];
     public static HashMap chunkCounter = new HashMap();
     
     /**
      * Reads save file to load ChunkOwn data
      *
      */
     public static void load() {
         try {
             //Open save file in BufferedReader
             new File("plugins/ChunkOwn").mkdir();
             new File("plugins/ChunkOwn/chunkown.save").createNewFile();
             BufferedReader bReader = new BufferedReader(new FileReader("plugins/ChunkOwn/chunkown.save"));
 
             //Convert each line into data until all lines are read
             String line = "";
             while ((line = bReader.readLine()) != null) {
                 String[] data = line.split(";");
 
                 OwnedChunk ownedChunk = getOwnedChunk(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                 
                 ownedChunk.owner = data[3];
                 int owned = 0;
                 Object object = chunkCounter.get(ownedChunk.owner);
                 if (object != null)
                     owned = (Integer)object;
                 chunkCounter.put(ownedChunk.owner, owned + 1);
 
                 //Convert the coOwners data into a LinkedList for the OwnedChunk
                 if (!data[4].equals("none")) 
                    ownedChunk.coOwners = new LinkedList<String>(Arrays.asList(data[4].split(",")));
 
                 //Convert the groups data into a LinkedList for the OwnedChunk
                 if (!data[5].equals("none")) 
                    ownedChunk.groups = new LinkedList<String>(Arrays.asList(data[5].split(",")));
             }
         }
         catch (Exception loadFailed) {
             System.err.println("[ChunkOwn] Load Failed!");
             loadFailed.printStackTrace();
         }
     }
     
     /**
      * Writes data to save file
      * Old file is overwritten
      */
     public static void save() {
         try {
             //Open save file for writing data
             BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/ChunkOwn/chunkown.save"));
 
             for (int i=0; i<100; i++)
                 for (int j=0; j<100; j++) {
                     LinkedList<OwnedChunk> chunkList = (LinkedList<OwnedChunk>)matrix[i][j];
                     if (chunkList != null)
                         for (OwnedChunk ownedChunk: chunkList) {
                             //Write data in format "world;x;z;owner;coOwner1,coOwner2,...;group1,group2,...;
                             bWriter.write(ownedChunk.world.concat(";"));
                             bWriter.write(ownedChunk.x+";");
                             bWriter.write(ownedChunk.z+";");
                             bWriter.write(ownedChunk.owner.concat(";"));
 
                             if (ownedChunk.coOwners.isEmpty())
                                 bWriter.write("none");
                             else
                                 for (String coOwner: ownedChunk.coOwners)
                                     bWriter.write(coOwner.concat(","));
                             bWriter.write(";");
 
                             if (ownedChunk.groups.isEmpty())
                                 bWriter.write("none");
                             else
                                 for (String group: ownedChunk.groups)
                                     bWriter.write(group.concat(","));
                             bWriter.write(";");
 
                             //Write each OwnedChunk on its own line
                             bWriter.newLine();
                         }
                 }
             bWriter.close();
         }
         catch (Exception saveFailed) {
             System.err.println("[ChunkOwn] Save Failed!");
             saveFailed.printStackTrace();
         }
     }
     
     /**
      * Returns the OwnedChunk object for the given Chunk
      * It is created if it does not exist
      * 
      * @param world The name of the World the OwnedChunk is in
      * @param x The x-coordinate of the OwnedChunk
      * @param z The z-coordinate of the OwnedChunk
      * @return The OwnedChunk object for the given Chunk
      */
     public static OwnedChunk getOwnedChunk(String world, int x, int z) {
         int row = x % 100;
         int column = z % 100;
         
         LinkedList<OwnedChunk> chunkList = (LinkedList<OwnedChunk>)matrix[row][column];
         
         if (chunkList == null) {
             chunkList = new LinkedList<OwnedChunk>();
             matrix[row][column] = chunkList;
         }
         
         for (OwnedChunk tempChunk: chunkList)
             if (tempChunk.x == x && tempChunk.z == z && tempChunk.world.equals(world))
                 return tempChunk;
         
         OwnedChunk ownedChunk = new OwnedChunk(world, x, z);
         chunkList.add(ownedChunk);
         return ownedChunk;
     }
     
     /**
      * Returns the OwnedChunk object for the given Chunk
      * returns null if the Chunk is not claimed
      * 
      * @param world The name of the World the OwnedChunk is in
      * @param x The x-coordinate of the OwnedChunk
      * @param z The z-coordinate of the OwnedChunk
      * @return The OwnedChunk object for the given Chunk
      */
     public static OwnedChunk findOwnedChunk(String world, int x, int z) {
         LinkedList<OwnedChunk> chunkList = (LinkedList<OwnedChunk>)matrix[x % 100][z % 100];
         
         if (chunkList == null)
             return null;
         
         for (OwnedChunk ownedChunk: chunkList)
             if (ownedChunk.x == x && ownedChunk.z == z && ownedChunk.world.equals(world))
                 return ownedChunk;
        return null;
     }
     
     /**
      * Removes the OwnedChunk from the saved data
      * 
      * @param world The name of the World the OwnedChunk is in
      * @param x The x-coordinate of the OwnedChunk
      * @param z The z-coordinate of the OwnedChunk
      */
     public static void removeOwnedChunk(String world, int x, int z) {
         int row = x % 100;
         int column = z % 100;
         
         LinkedList<OwnedChunk> chunkList = (LinkedList<OwnedChunk>)matrix[row][column];
         
         if (chunkList == null)
             return;
         
         for (OwnedChunk ownedChunk: chunkList)
             if (ownedChunk.x == x && ownedChunk.z == z && ownedChunk.world.equals(world)) {
                 chunkList.remove(ownedChunk);
                 chunkCounter.put(ownedChunk.owner, (Integer)chunkCounter.get(ownedChunk.owner) - 1);
                 break;
             }
         
         if (chunkList.isEmpty())
             matrix[row][column] = null;
         
         save();
     }
     
     
 }
