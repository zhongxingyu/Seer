 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 /**
  * The grand strategy of the AI for the CTF game.
  * @author kviiri
  */
 public class Donkey {
     private HashMap<String, Lion> expendables;
     private ArrayList<Things.Enemy> charlies;
     private ArrayList<Things.Grenade> grenades;
     Scanner lue;
     
     private float[][][][] dist;       //fromX fromY toX toY
     private ArrayList<ArrayList<Boolean>> wallMap;       //true = passable, false = not passable
     private boolean team;           //true = A, false = B
     private int ourScore;
     private int theirScore;
     private Things.Flag ourFlag;
     private Things.EnemyFlag enemyFlag;
     public Donkey() {
         this.expendables = new HashMap<String, Lion>();
         this.charlies = new ArrayList<Things.Enemy>();
         lue = new Scanner(System.in);
         dist = floydWarshall(2, 2);
         
     }
     private void parseInitialEngineOutput() {
         if(lue.nextLine().equals("A")) team = true;
         else team = false;
         int rowNum = 0;
         while(lue.hasNextLine()) {
             String line = lue.nextLine();
             if(line.length() < 2) break;
             for(int i = 0; i < line.length(); i++) {
                 if(line.charAt(i) == '#') wallMap.get(rowNum).add(false);
             }
             rowNum++;
         }
         
     }
     private void parseTurnEngineOutPut() {
         String[] points = lue.nextLine().split(" ");    //Ourpoints Enemypoints
         ourScore = Integer.parseInt(points[0]);
         theirScore = Integer.parseInt(points[1]);
         String[] flag = lue.nextLine().split(" ");      //ourFlag.x ourFlag.y
         ourFlag = new Things.Flag(Integer.parseInt(flag[1]),Integer.parseInt(flag[2]));
         //Next up - our soldiers
         String[] next = lue.nextLine().split(" ");
         do {
             if(!next[0].equalsIgnoreCase("Soldier")) break;
             expendables.get(next[1]).soldier = new Things.Soldier(next[1], Integer.parseInt(next[2]), Integer.parseInt(next[3]),
                     Integer.parseInt(next[4]), Boolean.parseBoolean(next[5]), next[6]);
             next = lue.nextLine().split(" ");       //"Soldier" name x y cooldown alive flag
         } while(next[0].equalsIgnoreCase("Soldier"));
         //Grenades
         do {
             if(!next[0].equalsIgnoreCase("Grenade")) break;
             grenades.add(new Things.Grenade(Integer.parseInt(next[1]), Integer.parseInt(next[2]), Integer.parseInt(next[3])));
             next = lue.nextLine().split(" ");
         } while(next[0].equals("Grenade"));
         enemyFlag = new Things.EnemyFlag(Integer.parseInt(next[1]), Integer.parseInt(next[2]));
         do {
             next = lue.nextLine().split(" ");
             if(next.length == 0) break;
             charlies.add(new Things.Enemy(next[1], Integer.parseInt(next[2]), Integer.parseInt(next[3]),
                     Boolean.parseBoolean(next[4]), next[5]));
        }
         
         
     }
     private float[][][][] floydWarshall(int xSize, int ySize) {
         float[][][][] ret = new float[xSize][ySize][xSize][ySize];
         for(int x1 = 0; x1 < xSize; x1++) {
             for(int x2 = 0; x2 < xSize; x2++) {
                 for(int y1 = 0; y1 < ySize; y1++) {
                     for(int y2 = 0; y2 < ySize; y2++) {
                         if(x1 == x2 && y1 == y2) ret[x1][y1][x2][y2] = 0;
                         else ret[x1][y1][x2][y2] = Float.POSITIVE_INFINITY;
                     }
                 }
             }
         }
         for(int xBetween = 0; xBetween < xSize; xBetween++) {
             for(int yBetween = 0; yBetween < ySize; yBetween++) {
                 for(int xAt = 0; xAt < xSize; xAt++) {
                     for(int yAt = 0; yAt < ySize; yAt++) {
                         for(int xDest = 0; xDest < xSize; xDest++) {
                             for(int yDest = 0; yDest < ySize; yDest++) {
                                 //if blocked
                                 if(!map[xDest][yDest]) {
                                     dist[xAt][yAt][xDest][yDest] = Float.POSITIVE_INFINITY;
                                     continue;
                                 }
                                 //if neighbor, distance is 1
                                 else if(Math.abs(yAt - yDest) + Math.abs(xAt - xDest) == 1) {
                                     dist[xAt][yAt][xDest][yDest] = 1;
                                     continue;
                                 }
                                 else {
                                     dist[xAt][yAt][xDest][yDest] = Math.min(dist[xAt][yAt][xDest][yDest],
                                             dist[xAt][yAt][xBetween][yBetween] + dist[xBetween][yBetween][xDest][yDest]);
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return ret;
     }
 }
