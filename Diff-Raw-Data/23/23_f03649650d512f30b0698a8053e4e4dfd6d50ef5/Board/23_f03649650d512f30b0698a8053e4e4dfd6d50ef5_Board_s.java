 import java.awt.Point;
 import java.util.ArrayList;
 
 public class Board {
   public enum CellTypes {
     Robot, Rock, Closed, Earth, Wall, Lambda, Open, Empty
   };
 
   public Robot robot;
   public int waterLevel;
   public int waterRate;
   public int totalPoints;
   public int totalLambdas;
   public ArrayList<Point> lambdaPos;
   public Point liftLocation;
   public CellTypes layout[][];
   public int layoutWidth;
   public int layoutHeight;
   public int ticks;
 
   public Board(int width, int height) {
     ticks = 0;
     layout = new CellTypes[height][width];
     totalPoints = 0;
     waterLevel = 0;
     waterRate = 0;
     lambdaPos = new ArrayList<Point>();
   }
 
   public static Board read(String map, int width, int height) {
     Board newMap = new Board(width, height);
     int index = 0;
     for (int i = 0; i < height; ++i) {
       for (int j = 0; j < width; ++j, ++index) {
         switch (map.toCharArray()[index]) {
         case '*':
           newMap.layout[i][j] = CellTypes.Rock;
           break;
         case '#':
           newMap.layout[i][j] = CellTypes.Wall;
           break;
         case 'R':
           newMap.layout[i][j] = CellTypes.Robot;
           break;
         case '.':
           newMap.layout[i][j] = CellTypes.Earth;
           break;
         case '\\':
           newMap.layout[i][j] = CellTypes.Lambda;
           newMap.lambdaPos.add(new Point(j, i)); // careful the order
           break;
         case 'L':
           newMap.layout[i][j] = CellTypes.Closed;
           break;
         case ' ':
           newMap.layout[i][j] = CellTypes.Empty;
           break;
         case 'O':
           newMap.layout[i][j] = CellTypes.Open;
           break;
         }
       }
     }
     return newMap;
   }
 
   public void move(char move) // should change internal state, or create a new
                               // one?
   {
    int x = robot.getPosition().x;
    int y = robot.getPosition().y;
     int xp = 0, yp = 0;
 
     switch (move) {
     case 'L':
       if (x - 1 > 0) {
         xp = x - 1;
         yp = y;
       }
       break;
     case 'R':
       if (x + 1 < layoutWidth) {
         xp = x + 1;
         yp = y;
       }
       break;
     case 'U':
       if (y + 1 < layoutHeight) {
         xp = x;
         yp = y + 1;
       }
       break;
     case 'D':
       if (y - 1 > 0) {
         xp = x;
         yp = y - 1;
       }
       break;
     }
 
     if (layout[yp][xp] == CellTypes.Open) {
       System.out.println("You won!");
     }
     if (layout[yp][xp] == CellTypes.Rock || layout[yp][xp] == CellTypes.Wall
         || layout[yp][xp] == CellTypes.Closed) {
       return;
     }
     if (layout[yp][xp] == CellTypes.Lambda) {
       totalPoints += 25;
       totalLambdas += 1;
       lambdaPos.remove(new Point(xp, yp)); // careful order!
     }
 
     layout[y][x] = CellTypes.Empty;
     layout[yp][xp] = CellTypes.Robot;
    robot.getPosition().x = xp;
    robot.getPosition().y = yp;
     totalPoints -= 1;
   }
 
   public void move(String move) // same question as above
   {
     for (char m : move.toCharArray()) {
       move(m);
     }
   }
 
   public void update() // again
   {
 
     if (ticks % waterRate == waterRate - 1) {
       waterLevel += 1;
     }
     for (int i = 0; i < layoutHeight; ++i) {
       for (int j = 0; j < layoutWidth; ++j) {
         
         if (layout[i][j] == CellTypes.Closed && lambdaPos.size() == 0) {
           layout[i][j] = CellTypes.Open;
         }
         
         if (layout[i][j] == CellTypes.Rock) {
           if (i-1 > 0 && layout[i-1][j] == CellTypes.Empty)
           {
             layout[i][j] = CellTypes.Empty;
             layout[i-1][j] = CellTypes.Empty;
             
           }
           if (i-1 > 0 && j+1 < layoutWidth-1 && 
               layout[i-1][j] == CellTypes.Rock && 
               layout[i][j+1] == CellTypes.Empty && 
               layout[i-1][j+1] == CellTypes.Empty )
           {
             layout[i][j] = CellTypes.Empty;
             layout[i-1][j+1] = CellTypes.Rock;
           }
           if (i-1 > 0 && j+1 < layoutWidth-1 && j-1 > 0 &&
               layout[i-1][j] == CellTypes.Rock && 
               (layout[i][j+1] != CellTypes.Empty || layout[i-1][j+1] != CellTypes.Empty) && 
               layout [i][j-1] == CellTypes.Empty &&
               layout[i-1][j-1] == CellTypes.Empty)
           {
             layout[i][j] = CellTypes.Empty;
             layout[i-1][j-1] = CellTypes.Rock;
           }
           if (i-1 > 0 && j+1 < layoutWidth-1 &&
               layout[i-1][j] == CellTypes.Lambda &&
               layout[i][j+1] == CellTypes.Empty &&
               layout[i-1][j+1] == CellTypes.Empty)
           {
             layout[i][j] = CellTypes.Empty;
             layout[i-1][j+1] = CellTypes.Rock;
           }
         }
       }
     }
   }
 
   public void checkConditions() {
 
   }
 
   public void tick() {
 
     // move(robot.getNextMove());
     update();
     checkConditions();
     ticks += 1;
   }
 
 }
