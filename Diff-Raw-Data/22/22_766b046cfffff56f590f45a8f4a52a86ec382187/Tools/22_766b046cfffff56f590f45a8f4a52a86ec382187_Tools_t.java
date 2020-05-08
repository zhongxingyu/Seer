 package fap_java;
 
 import gui.Constants;
 import gui.GameSave;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import java.awt.image.RescaleOp;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import java.util.ArrayList;
 
 /**
  * This class contains several tools that can be used wherever.
  * They are all set to public static to be available everywhere, and are as generic as possible so it would be easier
  * to use them in several conditions
  */
 public class Tools {
     /**
      * Picks a random int between two value
      * @param min : min boundary
      * @param max : max boundary
      * @return : a random integer between min and max
      */
     public static int randRange(int min, int max) {
         int randomNum = (int)(Math.random() * (max - min + 1) + min);
         return randomNum;
     }
 
     /**
      * Picks a random int between two values, excluding the given int
      * Warning : can lead to an infinite loop if too much values are excluded
      * @param min : min boundary
      * @param max : max boundary
      * @exclude The ints that shall be excluded of the random picking
      * @return : a random integer between min and max
      */
     public static int randRange(int min, int max, int[] exclude){
         int rand;
         boolean b = true;
         do{
             b=true;
             // Pick a random number
             rand = randRange(min,max);
             // Check if it's supposed to be excluded
             for(int i=0;i<exclude.length;i++){
                 int a = exclude[i];
                 if(a==rand){
                     // If yes, start again
                     b = false;
                 }
             }
         }
         while(!b);
         return rand;
     }
     
     /**
      * Removes the null values from an ArrayList, and their slots
      * @param al The designated ArrayList to purge
      * @return The purged arrayList
      */
     public static ArrayList removeNull(ArrayList al){
         // Loop on
         while(al.contains(null)){
             // Read the AL
             for(int i=0;i<al.size();i++){
                 // Get the object
                 Object o = al.get(i);
                 if(o==null){
                     // If it's null, remove it
                     al.remove(o);
                 }
             }
         }
         return al;
     }
     
     public static boolean intTableContains(int[] table, int value){
         boolean b = false;
         for(int a=0;a<table.length;a++){
             if(table[a] == value){
                 b = true;
                 break;
             }
         }
         return b;
     }
     
     // Graphical tools
     
     public static BufferedImage getImageToFilter(Image image){
         BufferedImage img = (BufferedImage)image;
         
         int w = img.getWidth(null);
         int h = img.getHeight(null);
         BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         
         Graphics gBi = bi.getGraphics();
         gBi.drawImage(img, 0, 0, null);
         
         return bi;
     }
     
     public static void drawFilteredImage(BufferedImage img, float[] scales, float[] offsets, Graphics g, int x, int y){
         RescaleOp rop = new RescaleOp(scales, offsets, null);
         
         Graphics2D g2d = (Graphics2D) g;
         g2d.drawImage(img, rop, x, y);
     }
     
     public static void drawFilteredImage(BufferedImage img, float Rfactor, float Gfactor, float Bfactor, float alpha, Graphics g, int x, int y){
         float[] offsets = new float[4];
         float[] scales = {Rfactor,Gfactor,Bfactor,alpha};
         drawFilteredImage(img, scales, offsets, g,x,y);
     }
     
     public static void drawFilteredImage(BufferedImage img, float[] scales, float[] offsets, Graphics g, int x, int y, double scaleX, double scaleY){
         RescaleOp rop = new RescaleOp(scales, offsets, null);
         
         Graphics2D g2d = (Graphics2D) g;
         g2d.scale(scaleX, scaleY);
         g2d.drawImage(img, rop, (int)(x/scaleX), (int)(y/scaleY));
         g2d.scale(1/scaleX, 1/scaleY);
     }
     
     // ---------- Save and Load game
     
     public static void saveGame(GameSave gs){
         try {
             FileOutputStream fileOut = new FileOutputStream(Constants.savegameFile);
             ObjectOutputStream out = new ObjectOutputStream(fileOut);
             out.writeObject(gs);
             out.close();
             fileOut.close();
             System.out.println("Saved Game in "+Constants.savegameFile);
         } catch (IOException i) {
             i.printStackTrace();
         }
     }
     
     public static GameSave loadGame(){
         GameSave gs = null;
         try {
             FileInputStream fileIn = new FileInputStream(Constants.savegameFile);
             ObjectInputStream in = new ObjectInputStream(fileIn);
             gs = (GameSave)in.readObject();
             in.close();
             fileIn.close();
         } 
         catch(FileNotFoundException e){
             System.out.println("Couldn't load file, creating new one");
             gs = new GameSave();
             Tools.saveGame(gs);
             return gs;
         }
         catch (IOException i) {
             i.printStackTrace();
             return new GameSave();
         } catch (ClassNotFoundException c) {
             System.out.println("Impossibru, class not found");
             c.printStackTrace();
             return null;
         }
         return gs;
     }
     
     public static NPC checkNPCOnCell(Game game, Cell c) {
         NPC npc = null;
         if (game.getAdv() > 0) {
             ArrayList<NPC> listNPCs = game.getListNPCs();
             for (int i = 0; i < listNPCs.size(); i++) {
                 NPC np = listNPCs.get(i);
                 if (np.getPosition().equals(c)) {
                     npc = np;
                     break;
                 }
             }
         }
         return npc;
     }
 }
