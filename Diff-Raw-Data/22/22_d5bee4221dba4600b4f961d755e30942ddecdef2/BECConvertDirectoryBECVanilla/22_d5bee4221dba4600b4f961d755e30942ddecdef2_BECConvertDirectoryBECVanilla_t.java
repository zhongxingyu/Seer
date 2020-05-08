 package nl.rutgerkok.BetterEnderChest.converter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 
 import net.minecraftwiki.wiki.NBTClass.Tag;
 
 public class BECConvertDirectoryBECVanilla extends BECConvertDirectory {
     private Thread thread;
     private File chestDirectory;
     private File playerDirectory;
     private float progress;
 
     public BECConvertDirectoryBECVanilla(File chestDirectory, File playerDirectory, boolean hasGUI) {
         super(chestDirectory, playerDirectory, hasGUI);
 
         System.out.println("Converting FROM BetterEnderChest to Vanilla...");
         System.out.println(chestDirectory.getPath());
         System.out.println(playerDirectory.getPath());
 
         this.chestDirectory = chestDirectory;
         this.playerDirectory = playerDirectory;
 
         thread = new Thread(this);
         thread.start();
     }
 
     public float getProgress() {
         return progress;
     }
 
     public void run() {
         String[] files = chestDirectory.list(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String fileName) {
                 return fileName.endsWith(".dat");
             }
         });
 
         // check if there are files
         if (files == null || files.length == 0) {
             message("Found no chest files in server directory. Did you select the level.dat of your main world?");
             System.exit(0);
             return;
         }
 
         for (int i = 0; i < files.length; i++) {
 
             progress = ((float) i) / ((float) files.length);// for progress bar
 
             System.out.println("(" + (int) (progress * 100) + "%) Converting " + files[i]);
 
             try { // convert
                 File chestFile = new File(chestDirectory + "/" + files[i]);
                 File playerFile = new File(playerDirectory + "/" + files[i]);
 
                 if (playerFile.exists()) {
                    // Get the Ender inventory from the chest file
                     Tag main = Tag.readFrom(new FileInputStream(chestFile));
                     Tag enderInventory = main.findTagByName("Inventory");

                    // Get the player from the player file
                     Tag player = Tag.readFrom(new FileInputStream(playerFile));
 
                    // Delete old EnderItems if they exist from the player file
                     if (player.findTagByName("EnderItems") != null)
                         player.removeSubTag(player.findTagByName("EnderItems"));
 
                    // Check if the Ender inventory (from the chest file) isn't
                    // empty, and add the stacks to the player file
                    if (enderInventory.findTagByName(null) != null) {
                         player.addTag(new Tag(Tag.Type.TAG_List, "EnderItems", enderInventory.getValue()));
                     }
                    
                    // Save
                    player.writeTo(new FileOutputStream(playerFile));
 
                 } else {
                     System.out.println("Cannot convert the inventory " + files[i] + " because there is no player with that name.");
                 }
             } catch (Exception e) {
                 message("Cannot convert file " + files[i] + "! \n\n" + e.getMessage() + "\n\nI said you should make a backup...");
                 e.printStackTrace();
             }
 
             try {
                 Thread.sleep(10);
             } catch (InterruptedException e) {
             }
         }
 
         progress = 1;
         message("Converted. Kept old chest files as a backup.");
         System.exit(0);
     }
 }
