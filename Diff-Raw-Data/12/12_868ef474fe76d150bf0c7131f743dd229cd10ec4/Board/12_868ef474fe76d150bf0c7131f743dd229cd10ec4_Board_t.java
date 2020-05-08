 //import java.awt.Point;
 import java.util.*;
 
 public class Board implements Cloneable {
   public enum CellTypes {
     Robot, Rock, Closed, Earth, Wall, Lambda, Open, Empty, Tramp, Target, Beard, TempBeard, Razor
   };
   public enum GameState {
     Win, Lose, Abort, Continue
   };
   
   public class Target
   {
     Target(Point pos, char l)
     {
       position = pos;
       label = l;
     }
     Point position;
     char label;
   }
   public class Trampoline
   {
     Trampoline(Point pos, char l)
     {
       position = pos;
       label = l;
     }
     Point position;
     char label;
   }
   
   public Robot robot;
   public int waterLevel;
   public int waterRate;
   public int growthRate;
   public int razorCount;
   public ArrayList<Point> lambdaPos;
   public Point liftLocation;
   public CellTypes layout[][];
   public ArrayList<Trampoline> trampolines;
   public ArrayList<Target> targets;
   public ArrayList<Point> tempBeards;
   public ArrayList<Point> beards;
   public ArrayList<Point> razors;
   public int layoutWidth;
   public int layoutHeight;
   public int ticks;
 
   private Board(int width, int height) {
     ticks = 0;
     layoutWidth = width;
     layoutHeight = height;
     layout = new CellTypes[height][width];
     waterLevel = 0;
     waterRate = 0;
     growthRate = 25;
     razorCount = 0;
     lambdaPos = new ArrayList<Point>();
     
   }
 
   public Board(String map) {
     String[] lines = map.split("\\r\\n|\\r|\\n");
 
     // Parse map
     layoutWidth = 0;
     int i;
     for (i = 0; i < lines.length; i++) {
       if (lines[i] == "")
         break;
 
       if (lines[i].length() > layoutWidth) {
         layoutWidth = lines[i].length();
       }
     }
     layoutHeight = i;
 
     // Parse metadata
     waterLevel = 0;
     waterRate = 0;
     for (; i < lines.length; i++) {
       String[] words = lines[i].split(" ");
       String type = words[0];
       if (type == "Water")
         waterLevel = Integer.parseInt(words[1]);
       else if (type == "Flooding")
         waterRate = Integer.parseInt(words[1]);
       else if (type == "Waterproof")
         robot.setWaterThreshold(Integer.parseInt(words[1]));
     }
 
     ticks = 0;
     layout = new CellTypes[layoutHeight][layoutWidth];
     lambdaPos = new ArrayList<Point>();
     trampolines = new ArrayList<Trampoline>();
     targets = new ArrayList<Target>();
     beards = new ArrayList<Point>();
     razors = new ArrayList<Point>();
     tempBeards = new ArrayList<Point>();
     
     waterLevel = 0;
     waterRate = 0;
     growthRate = 25;
     razorCount = 0;
       
     int y = 0;
     for (String line : lines) {
       for (int x = 0; x < line.length(); ++x) {
         switch (line.charAt(x)) {
         case '*':
           layout[y][x] = CellTypes.Rock;
           break;
         case '#':
           layout[y][x] = CellTypes.Wall;
           break;
         case 'R':
           layout[y][x] = CellTypes.Robot;
           robot = new Robot(x,y);
           break;
         case '.':
           layout[y][x] = CellTypes.Earth;
           break;
         case '\\':
           layout[y][x] = CellTypes.Lambda;
           lambdaPos.add(new Point(x, y)); // careful the order
           break;
         case 'L':
           layout[y][x] = CellTypes.Closed;
           liftLocation = new Point(x,y);
           break;
         case ' ':
           layout[y][x] = CellTypes.Empty;
           break;
         case 'O':
           layout[y][x] = CellTypes.Open;
           break;
         case 'A':
         case 'B':
         case 'C':
         case 'D':
         case 'E':
         case 'F':
         case 'G':
         case 'H':
         case 'I':
           layout[y][x] = CellTypes.Tramp;
           trampolines.add(new Trampoline(new Point(x,y), line.charAt(x)));
         break;
         case '1':
         case '2':
         case '3':
         case '4':
         case '5':
         case '6':
         case '7':
         case '8':
         case '9':
           layout[y][x] = CellTypes.Target;
           //conversion, so that tramps and targets have same labels. 
           targets.add(new Target(new Point(x,y), (char) (line.charAt(x)-'0' + 'A')));
         break;
         
         case 'W':
           layout[y][x] = CellTypes.Beard;
           beards.add(new Point(x,y));
           break;
         case '!':
           layout[y][x] = CellTypes.Razor;
           razors.add(new Point(x,y));
           break;
         }
       }
       y++;
     }
   }
 
 
   public Board(Board oldBoard) {
     robot = new Robot(oldBoard.robot);
     waterLevel = oldBoard.waterLevel;
     waterRate = oldBoard.waterRate;
     lambdaPos = new ArrayList<Point>(oldBoard.lambdaPos);
     liftLocation = oldBoard.liftLocation;
     layoutWidth = oldBoard.layoutWidth;
     layoutHeight = oldBoard.layoutHeight;
 
     // might want to use java's array copy
     for (int y = 0; y < layoutHeight; y++) {
       for (int x = 0; x < layoutWidth; x++) {
         layout[y][x] = oldBoard.layout[y][x];
       }
     }
     ticks = oldBoard.ticks;
   }
 
 
   public GameState move(Robot.Move move) // should change internal state, or create a new
   {
     int x = robot.getPosition().getX();
     int y = robot.getPosition().getY();
     int xp = 0, yp = 0;
 
     switch (move) {
     case Left:
         xp = x - 1;
         yp = y;
       break;
     case Right: 
         xp = x + 1;
         yp = y; 
       break;
     case Up:
         xp = x;
         yp = y + 1;
       break;
     case Down:
         xp = x;
         yp = y - 1;
       break;
     case Wait:
       return GameState.Continue;
     case Abort:
       return GameState.Abort;
     case Shave:
      if (razorCount < 1)
        return GameState.Continue;
       for (int i = y-1; i < 3; ++i)
         for (int j = x-1; j < 3; ++j)
         {
           if (layout[i][j] == CellTypes.Beard)
           {
             //temp beards are because we want to differentiate bettween new beards, and old. 
             //else, out of control growth. 
             layout[i][j] = CellTypes.Empty;
             beards.remove(new Point(j,i));
           }
         }
       razorCount--;
       return GameState.Continue;
       
     }
 
     
     if (layout[yp][xp] == CellTypes.Razor)
     {
      razorCount += 1;
     }
     //if we get to the exit and it is open, we win
     if (layout[yp][xp] == CellTypes.Open) {
       return GameState.Win;
     }
     //cannot go through a wall, or a closed lift, or a beard
     if (layout[yp][xp] == CellTypes.Wall ||
         layout[yp][xp] == CellTypes.Closed ||
         layout[yp][xp] == CellTypes.Beard) {
       return GameState.Continue;
     }
     //we stumbled on a lambda! pick it up
     if (layout[yp][xp] == CellTypes.Lambda) {
      robot.gainLambda();
        lambdaPos.remove(new Point(xp, yp)); // careful order!
     }
     //if we can move the rock left/right or we just tried to run into it
     if (move == Robot.Move.Left && layout[yp][xp] == CellTypes.Rock && layout[yp][xp-1] == CellTypes.Empty)
     {
      layout[yp][xp-1] = CellTypes.Rock;
     }
     else if (move == Robot.Move.Right && layout[yp][xp] == CellTypes.Rock && layout[yp][xp+1] == CellTypes.Empty)
     {
       layout[yp][xp+1] = CellTypes.Rock; 
     }
     else if (layout[yp][xp] == CellTypes.Rock)
     {
       //cant move this rock
       return GameState.Continue;
     }
     
     //if we step on a tramp, find our coresponding target, and set that. 
     if (layout[yp][xp] == CellTypes.Tramp)//poor performance
     {
       for (Trampoline tramp : trampolines)
       {
         if (tramp.position == new Point(xp,yp))
         {
           for (Target targ : targets)
           {
             if (targ.label == tramp.label)
             {
               yp = targ.position.y;
               xp = targ.position.x;
               layout[tramp.position.x][tramp.position.y]= CellTypes.Empty; 
               trampolines.remove(tramp);
               targets.remove(targ);
               break;
             }
           }
         }
       }
     }
     //update our position
     layout[y][x] = CellTypes.Empty;
     layout[yp][xp] = CellTypes.Robot;
     robot.setPosition(xp, yp);
     return GameState.Continue;
   }
 
   public GameState move(List<Robot.Move> moves) // same question as above, dont use
   {
     for (Robot.Move m : moves) {
       GameState state = move(m);
       if (state != GameState.Continue) {
         return state;
       }
     }
     return GameState.Continue;
   }
 
   public GameState update() // again
   {
 
     if (ticks % waterRate == waterRate - 1) {
       waterLevel += 1;
     }
     
     
     if(robot.getWaterTime() == robot.getWaterThreshold())
       return GameState.Lose; //is a drowning lose or abort?
     
     robot.stayInWater();//at what point do we want this called?
     
     for (int y = 0; y < layoutHeight; ++y) {
       for (int x = 0; x < layoutWidth; ++x) {
         
         if (layout[y][x] == CellTypes.Closed && lambdaPos.size() == 0) {
           layout[y][x] = CellTypes.Open;
         }
         //grow beards
         if(ticks%growthRate == growthRate-1 && layout[y][x] == CellTypes.Beard)
         {
               for (int i = y-1; i < 3; ++i)
                 for (int j = x-1; j < 3; ++j)
                 {
                   if (layout[i][j] == CellTypes.Empty)
                   {
                     //temp beards are because we want to differentiate bettween new beards, and old. 
                     //else, out of control growth. 
                     layout[i][j] = CellTypes.TempBeard;
                     tempBeards.add(new Point(j,i));
                     beards.add(new Point(j,i));
                   }
                 }
         }
         
         if (layout[y][x] == CellTypes.Rock) {
           
           int xp = 0, yp = 0;
           if (y-1 > 0 && layout[y-1][x] == CellTypes.Empty)
           {
             xp = x;
             yp = y-1;
           }
           if (y-1 > 0 && x+1 < layoutWidth-1 && 
               layout[y-1][x] == CellTypes.Rock && 
               layout[y][x+1] == CellTypes.Empty && 
               layout[y-1][x+1] == CellTypes.Empty )
           {
             xp = x+1;
             yp = y-1;
           }
           if (y-1 > 0 && x+1 < layoutWidth-1 && x-1 > 0 &&
               layout[y-1][x] == CellTypes.Rock && 
               (layout[y][x+1] != CellTypes.Empty || layout[y-1][x+1] != CellTypes.Empty) && 
               layout [y][x-1] == CellTypes.Empty &&
               layout[y-1][x-1] == CellTypes.Empty)
           {
             xp = x-1;
             yp = y-1;
           }
           if (y-1 > 0 && x+1 < layoutWidth-1 &&
               layout[y-1][x] == CellTypes.Lambda &&
               layout[y][x+1] == CellTypes.Empty &&
               layout[y-1][x+1] == CellTypes.Empty)
           {
             xp = x+1;
             yp = y-1;
           }
           
           layout[y][x] = CellTypes.Empty;
           layout[yp][xp] = CellTypes.Rock;
           if (layout[yp-1][xp] == CellTypes.Robot)
           {
             return GameState.Lose;
           }
           
         }
       }
     }
     
     //need to change the new beards for this round into permanent beards
     if (ticks%growthRate == growthRate-1)
     {
       for (Point p : tempBeards)
       {
         layout[p.y][p.x] = CellTypes.Beard;
         tempBeards.remove(p);
       }
     }
     return GameState.Continue;
   }
 
   
   public GameState tick(Robot.Move nextMove) {
     GameState state;
     if (nextMove == Robot.Move.Abort)
       return GameState.Abort;
     state = move(nextMove);
     if (state != GameState.Continue)
       return state;
     
     state = update();
     if (state != GameState.Continue)
       return state;
     ticks += 1;
    
     return GameState.Continue;
   }
 
 }
