 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class Test<E extends Skynet> {
   private ArrayList<String> maps;
   private ArrayList<String> mapNames;
   private ArrayList<Integer> scores;
   
   public Test() {
     maps = new ArrayList<String>();
     mapNames = new ArrayList<String>();
     scores = new ArrayList<Integer>();
   }
   
   public void readMaps() throws IOException {
     Scanner cmd = new Scanner(new InputStreamReader(Runtime.getRuntime().exec("ls ../maps/").getInputStream()));
     while(cmd.hasNext()) {
       String mapName = cmd.next();
       File fileName = new File("../maps/" + mapName);
       Scanner inFile = new Scanner(fileName);
       String newMap = "";
       while(inFile.hasNext()) {
         newMap += inFile.nextLine() + System.getProperty("line.separator");
       }
       addMap(newMap);
       mapNames.add(mapName);
     }
   }
   
   public void addMap(String map) {
     maps.add(map);
   }
   
   public void runTests(Class SkynetClass) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
     for (int i=0; i < maps.size(); i++) {
       String map = maps.get(i);
       String mapName = mapNames.get(i);
 
      Timer timer = new Timer();
       //long delay = 150*1000;
      long delay = 10*1000;
 
       Main.gotSIGINT = false;
 
       System.out.println("-----------------------------------------------------------------------------");
       System.out.println("     Map: " + mapName);
       System.out.println("-----------------------------------------------------------------------------");
       
       Skynet planner = (Skynet) SkynetClass.getDeclaredConstructor(String.class).newInstance(map);
 
       timer.schedule(new Reminder(), delay);
       
      
      planner.plan();
 
       int score = planner.score();
       System.out.println("Score: " + score);
       
       scores.add(score);
     }
     
     int overall = 0;
     for(int score: scores) {
       overall += score;
     }
     
     System.out.println("Average score: " + overall/scores.size());
   }
   
   class Reminder extends TimerTask {
     @Override
     public void run() {
       System.out.println("Sending SIGINT!");
       Main.gotSIGINT = true;
     } 
   }
   
   public static void main(String args[]) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
     Test<Skynet> t = new Test<Skynet>();
     
     t.readMaps();
 
    t.runTests(Skynet.GreedySkynet.class);
   }
   
 }
