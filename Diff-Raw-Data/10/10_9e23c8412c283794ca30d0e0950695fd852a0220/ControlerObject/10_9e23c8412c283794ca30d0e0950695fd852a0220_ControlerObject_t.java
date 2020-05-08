 package medievalroguelike;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.io.*;
 import java.util.Hashtable;
 
 public class ControlerObject {
     
     private Board game;
     
     private Hashtable<String, Enemy> mobs = new Hashtable<String, Enemy>();
     private Hashtable<String, NPC> npcs = new Hashtable<String, NPC>();
     private Hashtable<String, Item> items = new Hashtable<String, Item>();
     
     public ControlerObject(Board board) {
         game = board;
     }
     
     public void doKeyStrokes(InputControle input) {
         
         //only for development!!
         if(input.buttons[input.ESCAPE]) {
             System.exit(0);
         }
         
         if(game.inGame) {
             if(game.inGameMenu) {
                 
             } else {
                 if(game.playerWindows) {
                     //navigate through player windows (use switch)
                     switch(game.playerWindowsType) {
                         case 0:
                             if(input.buttons[input.I]) {
                                 game.playerWindows = false;
                                 game.playerWindowsType = -1;
                             }
                             break;
                         case 1:
                             break;
                     }
                 } else {
                     //do player stuff here
                     
                     if(input.buttons[input.UP] && game.playerTurn) game.player.move(0);
                     if(input.buttons[input.RIGHT] && game.playerTurn) game.player.move(1);
                     if(input.buttons[input.DOWN] && game.playerTurn) game.player.move(2);
                     if(input.buttons[input.LEFT] && game.playerTurn) game.player.move(3);
                     
                     if(input.buttons[input.I]) {
                         game.playerWindows = true;
                         game.playerWindowsType = 0;
                     }
                     
                     if(input.buttons[input.CTRL]) {
                         game.player.setActionReady(!game.player.getActionReady());
                     }
                 }
             }
         } else if(game.inMultiGame){
             
         }else {
             //menu
             if(game.subMenu) {
                 switch(game.subMenuCount) {
                     case 0:
                         break;
                     case 1:
                         break;
                     case 30:
                         break;
                 }
             } else {
                 //do menu navigation here
                 
                 //development Only
                 if(input.buttons[input.SPACE]) {
                     game.inGame = true;
                 }
             }
         }
         
         input.releaseAllKeys();
         
     }
     
     public void writeToLog(String text) throws IOException {
         FileWriter write = new FileWriter(System.getProperty("user.dir") + "/debug.txt", true);
         PrintWriter print_line = new PrintWriter(write);
         
         print_line.printf("%s" + "%n", text);
         
         print_line.close();
     }
     
     public void cleanLog() throws IOException {
         FileWriter write = new FileWriter(System.getProperty("user.dir") + "/debug.txt", false);
         PrintWriter print_line = new PrintWriter(write);
         
         print_line.print("");
         
         print_line.close();
     }
     
     public void loadMobs() {
         try {
             String path = System.getProperty("user.dir") + "/data/living/mobs/";
             String files;
             File folder = new File(path);
             File[] listOfFiles = folder.listFiles();
             
             for (int i = 0; i < listOfFiles.length; i++) {
                 if (listOfFiles[i].isFile()) {
                     files = listOfFiles[i].getName();
                     if (files.endsWith(".dat") || files.endsWith(".DAT")) {
                         
                        Enemy tmp;
                         
                         FileReader fr = new FileReader(path + files);
                         BufferedReader textReader = new BufferedReader(fr);
                         
                         String textData = new String();
                         String delim = "[|]";
                         String[] token;
                         while((textData = textReader.readLine()) != null) {
                             token = new String[10];
 
                             token = textData.split(delim);
                             
                            tmp = new Enemy(game);
                            
                             tmp.setArea(files.substring(0, files.length() - 4));
                             tmp.setName(token[0]);
                             tmp.setDescription(token[1]);
                             tmp.setDangerLevel(Integer.parseInt(token[2]));
                             tmp.setHealth(Integer.parseInt(token[3]));
                             tmp.setMaxhealth(Integer.parseInt(token[3]));
                             tmp.setMana(Integer.parseInt(token[4]));
                             tmp.setMaxMana(Integer.parseInt(token[4]));
                             tmp.setStamina(Integer.parseInt(token[5]));
                             tmp.setMaxStamina(Integer.parseInt(token[5]));
                             tmp.setStrength(Integer.parseInt(token[6]));
                             tmp.setWisdom(Integer.parseInt(token[7]));
                             tmp.setConstitution(Integer.parseInt(token[8]));
                             tmp.setDexterity(Integer.parseInt(token[9]));
                             
                             mobs.put(tmp.getArea() + "_" + token[0], tmp);
                             //System.out.println(tmp.getArea() + "_" + token[0]);
                             game.bitmapFact.loadMobImage(tmp.getArea() + "/" + token[0], tmp.getArea() + "_" + token[0]);
                            System.out.println(tmp.getArea() + "_" + token[0]);
                         }
                     }
                 }
             }
             
         } catch (Exception e) {
             System.out.println("exception in loadMobs - " + e);
         }
     }
     
     public void loadNPCs() {
         try {
             String path = System.getProperty("user.dir") + "/data/living/mobs/";
             String files;
             File folder = new File(path);
             File[] listOfFiles = folder.listFiles();
             
             for (int i = 0; i < listOfFiles.length; i++) {
                 if (listOfFiles[i].isFile()) {
                     files = listOfFiles[i].getName();
                     if (files.endsWith(".dat") || files.endsWith(".DAT")) {
                         System.out.println(files);
                         
                         NPC tmp = new NPC(game);
                         
                         FileReader fr = new FileReader(path + files);
                         BufferedReader textReader = new BufferedReader(fr);
                         
                         String textData = new String();
                         String delim = "[|]";
                         String[] token;
                         while((textData = textReader.readLine()) != null) {
                             System.out.println("this: " + textData);
                             token = new String[2];
 
                             token = textData.split(delim);
                             
                             tmp.setName(token[0]);
                             tmp.setDescription(token[1]);
                             
                             //open deze naam met extentie .ques in SCRIPTS map
                             //quests in HashTable
                             for (int q = 0; q < Integer.parseInt(token[2]); q++) {
                                 //tmp.addQuest(nieuwToken0 (naam), nieuwToken1 (beschrijving), nieuwToken2 (watNodig?), nieuwToken3 (beloningItemKeuze 1), nieuwToken4(token 3, keuze2), nieuwToken5(token3, keuze3), nieuwTolken6(token3, keuze 4), nieuwToken7 (Geldbeloning), nieuwToken8 (xp beloning));
                                 
                             }
                             
                             npcs.put(tmp.getName(), tmp);
                             game.bitmapFact.loadNPCImage( "/npc/" + token[0], token[0]);
                         }
                     }
                 }
             }
             
         } catch (Exception e) {
             System.out.println("Exception loading NPCs: " + e);
         }
     }
     
     
     //from Mapper to here:
     // This is how mobs will be added to the maps!!
     public void addMob(String name, int xPos, int yPos) {
         Enemy tmp = mobs.get(name);
         tmp.setPosition(xPos, yPos);
         game.enemies.add(tmp);
        tmp = null;
     }
 }
