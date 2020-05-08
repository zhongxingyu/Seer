 package vooga.scroller.level_editor;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import util.Location;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.sprites.test_sprites.MarioLib;
 import vooga.scroller.util.Sprite;
 import vooga.scroller.view.View;
 
 
 public class LevelParser {
 
     private static final String NEW_LINE = System.getProperty("line.seperator");
     private static final String BEGIN_LEVEL = "/level";
     private static final String BEGIN_KEY = "/key";
     private static final String BEGIN_SETTINGS = "/settings";
     private static final char SPACE = ' ';
     private static final String START_POINT = "StartPoint";
     private Scanner myScanner;
     private Map<Character, String> myCharacterMap;
     private List<String> myLevelStrings;
     private Map<String, Sprite> myNameMap;
     private Location myStartPoint;
 
     /**
      * Initialize instances variables.
      */
     public LevelParser () {
         myLevelStrings = new ArrayList<String>();
         myCharacterMap = new HashMap<Character, String>();
         myNameMap = new HashMap<String, Sprite>();
     }
 
     public void setNameMap (Map<String, Sprite> nameMap) {
         myNameMap = nameMap;
     }
 
     public LEGrid loadFileToGrid (File file) {
         myLevelStrings = new ArrayList<String>();
         myCharacterMap = new HashMap<Character, String>();
         // TODO needs to be refactored. Design needs to be improved
         setNameMap((new ToolsManager(new MarioLib())).getNameMap());
         try {
             myScanner = new Scanner(file);
         }
         catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         parseLevel();
         myCharacterMap = parseKey();
         myStartPoint = parseStartPoint();
         return createGrid();
     }
 
     public Level loadFileToLevel (File file, int id, ScrollingManager sm, View v) {
         return loadFileToGrid(file).createLevel(id, sm, v);
     }
 
     private void parseLevel () {
         myScanner.findWithinHorizon(BEGIN_LEVEL + NEW_LINE, 0);
         String line = myScanner.nextLine();
         System.out.println(line);
         while (!line.equals(BEGIN_KEY)) {
             myLevelStrings.add(line);
             System.out.println("----------------");
             line = myScanner.nextLine();
             System.out.println(line);
         }
     }
 
     private Map<Character, String> parseKey () {
         Map<Character, String> result = new HashMap<Character, String>();
         String line = myScanner.nextLine();
         while (!line.equals(BEGIN_SETTINGS)) {
             result.put(line.charAt(0), line.substring(2));
             line = myScanner.nextLine();
         }
         return result;
     }
 
     private Location parseStartPoint () {
         String line = myScanner.nextLine();
         line = line.substring(START_POINT.length() + 1);
         String[] splitLine = line.split(String.valueOf(SPACE));
         return new Location(Integer.parseInt(splitLine[0]),
                             Integer.parseInt(splitLine[1]));
     }
 
     private LEGrid createGrid () {
         if (myLevelStrings.isEmpty()) { return null; }
         System.out.println("" + myLevelStrings.size() + " " + myLevelStrings.get(1).length());
         LEGrid grid = new LEGrid(myLevelStrings.get(1).length(), myLevelStrings.size());
         for (int i = 1; i < myLevelStrings.size(); i++) {
             for (int j = 0; j < myLevelStrings.get(1).length(); j++) {
                 char c = myLevelStrings.get(i).charAt(j);
                 System.out.println(c);
                 if (c != SPACE) {
                     String name = myCharacterMap.get(c);
                     Sprite spr = myNameMap.get(name).copy();
                     System.out.println(name);
                     System.out.println(spr);
                     grid.addSpriteToBox(j, i - 1, spr);
                 }
             }
         }
         grid.addStartPoint((int) myStartPoint.getX(), (int) myStartPoint.getY());
         return grid;
     }
 }
