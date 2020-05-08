 //import java.awt.Point;
 import java.util.*;
 
 public class Board implements Cloneable {
   public enum CellTypes {
     Robot, Rock, Closed, Earth, Wall, Lambda, Open, Empty, Tramp, Target, Beard, TempBeard, Razor
   };
   public enum GameState {
     Win, Lose, Abort, Continue
   };
   
   public Robot robot;
   public int waterLevel;
   public int waterRate;
   public int growthRate;
   public int razorCount;
   public ArrayList<Point> lambdaPos;
   public Point liftLocation;
   public BoardRep layout;
   
   public ArrayList<Point> trampolines;
   public HashMap<Point, Point> trampToTargets;
   
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
     layout = new BoardRep(height, width);//new CellTypes[height][width];
     waterLevel = 0;
     waterRate = 0;
     growthRate = 25;
     razorCount = 0;
     lambdaPos = new ArrayList<Point>();
     
   }
 //needs to handle meta date!
   public Board(String map) {
     
     ticks = 0;
     layout = new BoardRep(layoutHeight, layoutWidth);//CellTypes[layoutHeight][layoutWidth];
     lambdaPos = new ArrayList<Point>();
     trampolines = new ArrayList<Point>();
     trampToTargets = new HashMap<Point,Point>();
     beards = new ArrayList<Point>();
     razors = new ArrayList<Point>();
     tempBeards = new ArrayList<Point>();
     
     
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
     growthRate = 25;
     razorCount = 0;
     robot.setWaterThreshold(0);
     
     HashMap<String, String> labelTolabel = new HashMap<String, String>();
     HashMap<Point, String> trampToLabel =  new HashMap<Point, String>();
     HashMap<String, Point> labelToTarget =  new HashMap<String, Point>();
     
     for (; i < lines.length; i++) {
       String[] words = lines[i].split(" ");
       String type = words[0];
       if (type == "Water")
         waterLevel = Integer.parseInt(words[1]);
       else if (type == "Flooding")
         waterRate = Integer.parseInt(words[1]);
       else if (type == "Waterproof")
         robot.setWaterThreshold(Integer.parseInt(words[1]));
       else if (type == "Growths")
         growthRate = Integer.parseInt(words[1]);
       else if (type == "Razors")
         razorCount = Integer.parseInt(words[1]);
       else if (type == "Trampoline")
         labelTolabel.put(words[1], words[3]);
     }
        
     int y = 0;
     for (String line : lines) {
       
       for (int x = 0; x < line.length(); ++x) {
         switch (line.charAt(x)) {
         case '*':
           layout.set(x,y,CellTypes.Rock);
           break;
         case '#':
           layout.set(x,y,CellTypes.Wall);
           break;
         case 'R':
           layout.set(x,y,CellTypes.Robot);
           robot = new Robot(x,y);
           break;
         case '.':
           layout.set(x,y,CellTypes.Earth);
           break;
         case '\\':
           layout.set(x,y,CellTypes.Lambda);
           lambdaPos.add(new Point(x, y)); // careful the order
           break;
         case 'L':
           layout.set(x,y,CellTypes.Closed);
           liftLocation = new Point(x,y);
           break;
         case ' ':
           layout.set(x,y,CellTypes.Empty);
           break;
         case 'O':
           layout.set(x,y,CellTypes.Open);
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
           layout.set(x,y,CellTypes.Tramp);
           trampToLabel.put(new Point(x,y),Character.toString(line.charAt(x)));
           trampolines.add(new Point(x,y));
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
           layout.set(x,y,CellTypes.Target);
           //conversion, so that tramps and targets have same labels. 
           labelToTarget.put(Character.toString(line.charAt(x)),new Point(x,y));
           
         break;
         
         case 'W':
           layout.set(x,y,CellTypes.Beard);
           beards.add(new Point(x,y));
           break;
         case '!':
           layout.set(x,y,CellTypes.Razor);
           razors.add(new Point(x,y));
           break;
         }
       }
       
       y++;
     }
     
    for (Point p : trampolines)
    {
      String trampLabel = trampToLabel.get(p);
      String targetLabel = labelTolabel.get(trampLabel);
      Point target = labelToTarget.get(targetLabel);
      
      trampToTargets.put(p,  target);
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
         layout.set(x,y,oldBoard.layout.get(x,y));
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
           if (layout.get(j,i) == CellTypes.Beard)
           {
             //temp beards are because we want to differentiate bettween new beards, and old. 
             //else, out of control growth. 
             layout.set(j,i,CellTypes.Empty);
             beards.remove(new Point(j,i));
           }
         }
       razorCount--;
       return GameState.Continue;
       
     }
 
     
     if (layout.get(xp,yp) == CellTypes.Razor)
     {
       razorCount += 1;
     }
     //if we get to the exit and it is open, we win
     if (layout.get(xp,yp) == CellTypes.Open) {
       return GameState.Win;
     }
     //cannot go through a wall, or a closed lift, or a beard
     if (layout.get(xp,yp) == CellTypes.Wall ||
         layout.get(xp,yp) == CellTypes.Closed ||
         layout.get(xp,yp) == CellTypes.Beard) {
       return GameState.Continue;
     }
     //we stumbled on a lambda! pick it up
     if (layout.get(xp,yp) == CellTypes.Lambda) {
      robot.gainLambda();
        lambdaPos.remove(new Point(xp, yp)); // careful order!
     }
     //if we can move the rock left/right or we just tried to run into it
     if (move == Robot.Move.Left && layout.get(xp,yp) == CellTypes.Rock && layout.get(xp-1, yp) == CellTypes.Empty)
     {
      layout.set(xp-1,yp, CellTypes.Rock);
     }
     else if (move == Robot.Move.Right && layout.get(xp,yp) == CellTypes.Rock && layout.get(xp+1, yp) == CellTypes.Empty)
     {
       layout.set(xp+1, yp,CellTypes.Rock); 
     }
     else if (layout.get(xp, yp) == CellTypes.Rock)
     {
       //cant move this rock
       return GameState.Continue;
     }
     
     //if we step on a tramp, find our coresponding target, and set that. 
  
     if (layout.get(xp, yp) == CellTypes.Tramp)
     {
           
       Point target = trampToTargets.get(new Point(xp, yp));
       
       trampToTargets.remove(new Point(xp, yp));
       trampolines.remove(new Point(xp, yp));
       
       //this is so we can remove all tramps that jump to this target. 
       if (trampToTargets.containsValue(target))
       {
         for (Point tramp : trampolines)
         {
           if (trampToTargets.get(tramp) == target)
           {
             trampToTargets.remove(tramp);
            trampolines.remove(tramp);
           }
         }
       }
       
       layout.set(xp,yp,CellTypes.Empty);
       xp = target.x;
       yp = target.y;
       
     }
     //update our position
     layout.set(x,y,CellTypes.Empty);
     layout.set(xp,yp,CellTypes.Robot);
     robot.setPosition(xp, yp);
     return GameState.Continue;
   }
 
   private GameState move(List<Robot.Move> moves) // same question as above, dont use
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
         
         if (layout.get(x,y) == CellTypes.Closed && lambdaPos.size() == 0) {
           layout.set(x,y,CellTypes.Open);
         }
         //grow beards
         if(ticks%growthRate == growthRate-1 && layout.get(x,y) == CellTypes.Beard)
         {
               for (int i = y-1; i < 3; ++i)
                 for (int j = x-1; j < 3; ++j)
                 {
                   if (layout.get(j,i) == CellTypes.Empty)
                   {
                     //temp beards are because we want to differentiate bettween new beards, and old. 
                     //else, out of control growth. 
                     layout.set(j,i,CellTypes.TempBeard);
                     tempBeards.add(new Point(j,i));
                     beards.add(new Point(j,i));
                   }
                 }
         }
         
         if (layout.get(x,y) == CellTypes.Rock) {
           
           int xp = 0, yp = 0;
           if (y-1 > 0 && layout.get(x,y-1) == CellTypes.Empty)
           {
             xp = x;
             yp = y-1;
           }
           if (y-1 > 0 && x+1 < layoutWidth-1 && 
               layout.get(x,y-1) == CellTypes.Rock && 
               layout.get(x+1, y) == CellTypes.Empty && 
               layout.get(x+1, y-1) == CellTypes.Empty )
           {
             xp = x+1;
             yp = y-1;
           }
           if (y-1 > 0 && x+1 < layoutWidth-1 && x-1 > 0 &&
               layout.get(x,y-1) == CellTypes.Rock && 
               (layout.get(x+1, y) != CellTypes.Empty || layout.get(x+1, y-1) != CellTypes.Empty) && 
               layout.get(x-1, y)== CellTypes.Empty &&
               layout.get(x-1, y-1) == CellTypes.Empty)
           {
             xp = x-1;
             yp = y-1;
           }
           if (y-1 > 0 && x+1 < layoutWidth-1 &&
               layout.get(x, y-1) == CellTypes.Lambda &&
               layout.get(x+1, y) == CellTypes.Empty &&
               layout.get(x+1, y-1) == CellTypes.Empty)
           {
             xp = x+1;
             yp = y-1;
           }
           
           layout.set(x,y,CellTypes.Empty);
           layout.set(xp, yp, CellTypes.Rock);
           if (layout.get(xp, yp-1) == CellTypes.Robot)
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
         layout.set(p.x, p.y, CellTypes.Beard);
         tempBeards.remove(p);
       }
     }
     return GameState.Continue;
   }
 
   public void displayBoard()
   {
     for (int y = 0; y < layoutHeight; ++y)
     {
       for(int x = 0; x < layoutWidth; ++x)
       {
         switch(layout.get(x,y))
         {
           case Robot:
             System.out.print('R');
             break;
           case Rock:
             System.out.print('*');
             break;
           case Empty:
             System.out.print(' ');
             break;
           case Earth:
             System.out.print('.');
             break;
           case Lambda:
             System.out.print('/');
             break;
           case Closed:
             System.out.print('L');
             break;
           case Open:
             System.out.print('O');
             break;
           case Beard:
             System.out.print('W');
             break;
           case Razor:
             System.out.print('!');
             break;
           case Wall:
             System.out.print('#');
             break;
           case Tramp:
             System.out.print('A');
             break;
           case Target:
             System.out.print('1');
             break;
         }
       }
       System.out.print("\n");
     }
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
