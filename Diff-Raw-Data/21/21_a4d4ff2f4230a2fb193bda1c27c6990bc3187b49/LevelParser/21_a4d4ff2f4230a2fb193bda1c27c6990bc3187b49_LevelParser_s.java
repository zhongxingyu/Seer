 package vooga.scroller.level_editor;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import vooga.scroller.sprites.test_sprites.MarioLib;
 import vooga.scroller.util.Sprite;
 
 
 public class LevelParser {
 
     private static final String NEW_LINE = System.getProperty("line.seperator");
     private static final String BEGIN_LEVEL = "/level";
     private static final String BEGIN_KEY = "/key";
     private static final char SPACE = ' ';
     public Scanner myScanner;
     public Map<Character, String> myCharacterMap;
     public List<String> myLevelStrings;
     private Map<String, Sprite> myNameMap;
 
     public void setNameMap (Map<String, Sprite> nameMap) {
         myNameMap = nameMap;
     }
 
     public LEGrid loadFileToGrid (File file) {
         myLevelStrings = new ArrayList<String>();
         myCharacterMap = new HashMap<Character, String>();
         try {
             myScanner = new Scanner(file);
         }
         catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         parseLevel();
         parseKey();
         return createGrid();
     }
 
     public Level loadFileToLevel (File file, int id) {
         return loadFileToGrid(file).createLevel(id);
     }
 
     private void parseLevel () {
         myScanner.findWithinHorizon(BEGIN_LEVEL+NEW_LINE, 0);
         String line = myScanner.nextLine();
         System.out.println(line);
         while (!line.equals(BEGIN_KEY)) {
             myLevelStrings.add(line);
             System.out.println("----------------");
             line = myScanner.nextLine();
             System.out.println(line);
         }
     }
 
     private void parseKey () {
         while (myScanner.hasNextLine()) {
             String line = myScanner.nextLine();
             myCharacterMap.put(line.charAt(0), line.substring(2));
         }
     }
 
     private LEGrid createGrid () {
         if (myLevelStrings.isEmpty()) { return null; }
         System.out.println("" + myLevelStrings.size() + " " + myLevelStrings.get(1).length());
        LEGrid grid = new LEGrid(myLevelStrings.size(), myLevelStrings.get(1).length());
         for (int i = 1; i < myLevelStrings.size(); i++) {
             for (int j = 0; j < myLevelStrings.get(1).length(); j++) {
                 char c = myLevelStrings.get(i).charAt(j);
                 System.out.println(c);
                 if (c != SPACE) {
                     String name = myCharacterMap.get(c);
                     Sprite spr = myNameMap.get(name).copy();
                     System.out.println(name);
                     System.out.println(spr);
                     grid.addSpriteToBox(j, i, spr);
                 }
             }
         }
         return grid;
     }
 }
