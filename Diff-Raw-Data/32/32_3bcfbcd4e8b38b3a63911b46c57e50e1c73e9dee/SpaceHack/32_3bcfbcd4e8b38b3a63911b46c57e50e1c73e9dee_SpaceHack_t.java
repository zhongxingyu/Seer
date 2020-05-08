 import java.io.*;
 import java.util.*;
 
 public class SpaceHack {
   
   private Level[] game;
   private int currLevel;
   private Unit player;
   private ArrayList<Unit> characters;
   private int turns;
   
   private static final int NUM_LEVELS = 5;
   
   public SpaceHack() {
     game = new Level[NUM_LEVELS];
     turns = 0;
     currLevel = 3;
     characters = new ArrayList<Unit>();
     
     setGame();
   }
   
   public void setGame() {
     game[3] = readLevel("TwilightDeck3.dat");
     char[][] map = fileToCharArray("TwilightDeck3.dat");
     MapNode playerStart = game[currLevel].getNode(0,0);
     outer:
     for(int r = 0; r < map.length; r++) {
       for(int c = 0; c < map[r].length; c++) {
           if(map[r][c] == Level.PLAYER_BED_CHAR) {
               playerStart = game[currLevel].getNode(c,r);
               break outer;
           }
       }
     }
     String ans = classChoosePrompt();
     if(ans.equals("Soldier")) {
         player = new Soldier(playerStart);
     }
     else if(ans.equals("Scout")) {
         player = new Soldier(playerStart);
     }
     else if(ans.equals("Sniper")) {
         player = new Soldier(playerStart);
     }
     else if(ans.equals("Medic")) {
         player = new Soldier(playerStart);
     }
     else {
         player = new Soldier(playerStart);
     }
   }
   
   // Reads a level from a text file.
   private Level readLevel(String filename) {
     
     Level level = new Level(null);
     char[][] map = fileToCharArray("TwilightDeck3.dat");
     // This line is iffy, but will work provided that the file has something in it.
     MapNode[][] grid = new MapNode[map.length][map[0].length];
     int y = 0;
     char curr;
     for(int r = 0; r < map.length; r++) {
       for(int c = 0; c < map[r].length; c++) {
         curr = map[r][c];
         if(curr == Level.WALL_CHAR)
           grid[r][c] = new Wall(level,c,r);
         else if(curr == Level.FLOOR_CHAR)
           grid[r][c] = new Floor(level,c,r);
         else if(curr == Level.DOOR_CHAR)
           grid[r][c] = new Door(level,c,r);
         else if(curr == Level.ELEVATOR_CHAR)
           grid[r][c] = new Elevator(level,c,r);
         else if(curr == Level.BLAST_DOOR_CHAR)
           grid[r][c] = new BlastDoor(level,c,r);
         else if(curr == Level.PLAYER_BED_CHAR)
           grid[r][c] = new PlayerBed(level,c,r);
         else if(curr == Level.CREW_SPAWN_CHAR) {
           grid[r][c] = new CrewSpawn(level,c,r);
           characters.add(new Crewman(grid[r][c]));
         }
         else
           grid[r][c] = new Space(level,c,r);
       }
     }
     level.setGrid(grid);
     return level;
   }
   
   public char[][] fileToCharArray(String filename) {
     String line;
     Scanner sc = null;
     try {
       sc = new Scanner(new File(filename));
     }
     catch (Exception e) {
       System.out.println("Can't read " + filename);
       System.exit(0);
     }
     
     int lines = 0;
     int lineLength = 0;
     while(sc.hasNextLine()) {
       lines++;
       lineLength = sc.nextLine().length();
     }
     
     try {
       sc = new Scanner(new File(filename));
     }
     catch (Exception e) {
       System.out.println("Can't read " + filename);
       System.exit(0);
     }
     
     char[][] charArray = new char[lines][lineLength];
     
     int y = 0;
     while(sc.hasNextLine()) {
       line = sc.nextLine();
       for(int x = 0; x < line.length(); x++) {
           charArray[y][x] = line.charAt(x);
       }
       y++;
     }
     return charArray;
   }
   
   public String printCurrLevel() {
       return game[currLevel].toString();
   }
   
   public String printPlayerStats() {
      return player.statString();
   }
   
   public String classChoosePrompt() {
       System.out.println("What class would you like to be?");
       System.out.println("(\"Soldier\",\"Scout\",\"Sniper\",\"Medic\",\"Heavy\")?");
       String ans = Keyboard.readWord();
       if(!ans.equals("Soldier") && !ans.equals("Scout") && !ans.equals("Sniper") && !ans.equals("Medic") && !ans.equals("Heavy")) {
           System.out.println("Sorry, that is not a valid answer.");
           return classChoosePrompt();
       }
       else {
           return ans;
       }
   }
   
   public boolean prompt() {
       String reading = Keyboard.readString();
       if(reading.equals("Quit") || reading.equals("quit")) {
           return false;
       }
       PromptGroups.isAskingForHelp(reading);
       PromptGroups.isMoving(player, reading);
       PromptGroups.isOpeningDoor(player, reading);
       PromptGroups.isClosingDoor(player, reading);
       return true;
   }
   
   public boolean clarificationPrompt() {
       System.out.println("Are you sure?");
       boolean ans = false;
       String reading = Keyboard.readWord();
       if(reading.equals("yes") || reading.equals("y") || reading.equals("Y") || reading.equals("Yes"))
         ans = true;
       return ans;
   }
   
   public static int directionPrompt() {
       String reading = Keyboard.readWord();
       if(reading.equals("1"))
           return MapNode.DOWN_LEFT;
       if(reading.equals("2") || reading.equals("s"))
           return MapNode.DOWN;
       if(reading.equals("3"))
           return MapNode.DOWN_RIGHT;
       if(reading.equals("4") || reading.equals("a"))
           return MapNode.LEFT;
       if(reading.equals("5"))
           return MapNode.THIS_SPACE;
       if(reading.equals("6") || reading.equals("d"))
           return MapNode.RIGHT;
       if(reading.equals("7"))
           return MapNode.UP_LEFT;
       if(reading.equals("8") || reading.equals("w"))
           return MapNode.UP;
       if(reading.equals("9"))
           return MapNode.UP_RIGHT;
       System.out.println("Nevermind.");
       return MapNode.THIS_SPACE;
   }
   
   public void doStuff() {
       System.out.println(printCurrLevel());
       System.out.println(printPlayerStats());
   }
   
   public void nextTurn() {
       turns++;
   }
 }
