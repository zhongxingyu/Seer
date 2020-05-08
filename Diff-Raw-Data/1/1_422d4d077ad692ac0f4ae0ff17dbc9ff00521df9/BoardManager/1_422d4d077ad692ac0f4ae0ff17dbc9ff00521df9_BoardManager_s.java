 package se.persandstrom.ploxworm.core.worm.board;
 
 import se.persandstrom.ploxworm.core.Core;
 import se.persandstrom.ploxworm.core.worm.HumanWorm;
 import se.persandstrom.ploxworm.core.worm.Worm;
 import se.persandstrom.ploxworm.core.worm.ai.StupidWorm;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BoardManager {
 
     protected final static String TAG = "BoardManager";
 
     public final static int TOTAL_LEVELS = 7;
 
     public static Board getBoard(Core core, int level, BoardType type) {
 
         if (level == 0) {
 //            Log.e(TAG, "wtf not level 0 again...");
             level = 1;
             //XXX this should not be needed...
         }
 
         Board board;
         switch (level) {
             case 1:
                 board = level1(core);
                 break;
             case 2:
                 board = level2(core);
                 break;
             case 3:
                 board = level3(core);
                 break;
             case 4:
                 board = level4(core);
                 break;
             case 5:
                 board = level5(core);
                 break;
             case 6:
                 board = level6(core);
                 break;
             default:
                 throw new IllegalArgumentException("wtf level did not exist");
         }
 
         //XXX someday this must be made much more flexible, to allow like 3 humans vs 1 cpu etc
         Worm player1Worm = new HumanWorm(core, 0, board.getStartPositionList().get(0));
         board.addWorm(player1Worm);
         switch (type) {
             case SINGLE:
                 break;
             case VS_CPU:
                 Worm cpuWorm = new StupidWorm(core, 1, board.getStartPositionList().get(1));
                 board.addWorm(cpuWorm);
 
                 board.setAppleEatGoal(999);
                 break;
             case MULTI:
                 Worm player2Worm = new HumanWorm(core, 0, board.getStartPositionList().get(1));
                 board.addWorm(player2Worm);
 
                 board.setAppleEatGoal(999);
                 break;
         }
 
         for (Worm worm : board.getWormList()) {
             worm.init(board);
         }
 
         return board;
     }
 
     private static Board level1(Core core) {
         String title = "Journey begins!";
 
         ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
         ArrayList<Apple> appleList = new ArrayList<Apple>();
         List<StartPosition> startPositionList = new ArrayList<StartPosition>();
         int xSize = 800;
         int ySize = 800;
         int appleEatGoal = 4;
         int appleVisibleAtOnce = 2;
 
         //obstacles:
 
         //apples:
         appleList.add(new Apple(false, 200, 200));
         appleList.add(new Apple(false, 600, 200));
         appleList.add(new Apple(false, 200, 600));
         appleList.add(new Apple(false, 600, 600));
 
         //startPositions:
         startPositionList.add(new StartPosition(400, 400, 1, 1));
         startPositionList.add(new StartPosition(600, 600, 1, 1));
 
         return new Board(core, title, obstacleList, appleList, xSize, ySize, appleEatGoal, appleVisibleAtOnce,
                 startPositionList);
     }
 
     private static Board level2(Core core) {
         String title = "Obstacles in the way!";
 
         ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
         ArrayList<Apple> appleList = new ArrayList<Apple>();
         List<StartPosition> startPositionList = new ArrayList<StartPosition>();
         int xSize = 800;
         int ySize = 800;
         int appleEatGoal = 4;
         int appleVisibleAtOnce = 2;
 
         //obstacles:
         obstacleList.add(new ObstacleCircle(0, 0, 200));
         obstacleList.add(new ObstacleRectangle(600, 600, 800, 800));
 
         //apples:
         appleList.add(new Apple(false, 150, 550));
         appleList.add(new Apple(false, 250, 650));
         appleList.add(new Apple(false, 550, 150));
         appleList.add(new Apple(false, 650, 250));
 
         //startPositions:
         startPositionList.add(new StartPosition(400, 400, 1, 1));
         startPositionList.add(new StartPosition(200, 500, 1, 1));
 
         return new Board(core, title, obstacleList, appleList, xSize, ySize, appleEatGoal, appleVisibleAtOnce,
                 startPositionList);
     }
 
     private static Board level3(Core core) {
         String title = "Trancend the border!";
 
         ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
         ArrayList<Apple> appleList = new ArrayList<Apple>();
         List<StartPosition> startPositionList = new ArrayList<StartPosition>();
         int xSize = 800;
         int ySize = 800;
         int appleEatGoal = 4;
         int appleVisibleAtOnce = 1;
 
         //obstacles:
         obstacleList.add(new ObstacleRectangle(390, 0, 410, 800));
 
         //apples:
         appleList.add(new Apple(false, 200, 400));
         appleList.add(new Apple(false, 600, 400));
 
         //startPositions:
         startPositionList.add(new StartPosition(200, 200, 1, 1));
 
         return new Board(core, title, obstacleList, appleList, xSize, ySize, appleEatGoal, appleVisibleAtOnce,
                 startPositionList);
     }
 
     private static Board level4(Core core) {
         String title = "Dont trancend the border!";
 
         ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
         ArrayList<Apple> appleList = new ArrayList<Apple>();
         List<StartPosition> startPositionList = new ArrayList<StartPosition>();
         int xSize = 800;
         int ySize = 800;
         int appleEatGoal = 6;
         int appleVisibleAtOnce = 3;
 
         //obstacles:
         obstacleList.add(new ObstacleCircle(400, 400, 200));
         obstacleList.add(new ObstacleRectangle(0, 0, 10, 800));
         obstacleList.add(new ObstacleRectangle(0, 0, 800, 10));
         obstacleList.add(new ObstacleRectangle(0, 790, 800, 800));
         obstacleList.add(new ObstacleRectangle(790, 0, 800, 800));
 
         //apples:
         appleList.add(new Apple(false, 150, 150));
         appleList.add(new Apple(false, 150, 650));
         appleList.add(new Apple(false, 650, 150));
         appleList.add(new Apple(false, 650, 650));
 
         //startPositions:
         startPositionList.add(new StartPosition(120, 200, 1, 1));
         startPositionList.add(new StartPosition(120, 600, 1, 1));
 
         return new Board(core, title, obstacleList, appleList, xSize, ySize, appleEatGoal, appleVisibleAtOnce,
                 startPositionList);
     }
 
     private static Board level5(Core core) {
         String title = "Le Lind";
 
         ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
         ArrayList<Apple> appleList = new ArrayList<Apple>();
         List<StartPosition> startPositionList = new ArrayList<StartPosition>();
         int xSize = 800;
         int ySize = 800;
         int appleEatGoal = 10;
         int appleVisibleAtOnce = 2;
 
         //obstacles:
         obstacleList.add(new ObstacleRectangle(-100, -100, 10, 900));
         obstacleList.add(new ObstacleRectangle(-100, -100, 900, 10));
         obstacleList.add(new ObstacleRectangle(-100, 790, 900, 900));
         obstacleList.add(new ObstacleRectangle(790, -100, 900, 900));
         obstacleList.add(new ObstacleCircle(200, 200, 120));
         obstacleList.add(new ObstacleCircle(200, 600, 120));
         obstacleList.add(new ObstacleCircle(600, 200, 120));
         obstacleList.add(new ObstacleCircle(600, 600, 120));
 
         //apples:
         appleList.add(new Apple(false, 75, 75));
         appleList.add(new Apple(false, 350, 450));
         appleList.add(new Apple(false, 450, 350));
         appleList.add(new Apple(false, 725, 725));
 
         //startPositions:
         startPositionList.add(new StartPosition(200, 400, 1, 1));
         startPositionList.add(new StartPosition(600, 400, 1, 1));
 
         return new Board(core, title, obstacleList, appleList, xSize, ySize, appleEatGoal, appleVisibleAtOnce,
                 startPositionList);
     }
 
     private static Board level6(Core core) {
         String title = "Front screen";
 
         ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
         ArrayList<Apple> appleList = new ArrayList<Apple>();
         List<StartPosition> startPositionList = new ArrayList<StartPosition>();
         int xSize = 800;
         int ySize = 800;
         int appleEatGoal = 8;
         int appleVisibleAtOnce = 3;
 
         //obstacles:
         obstacleList.add(new ObstacleRectangle(0, 0, 10, 800));
         obstacleList.add(new ObstacleRectangle(0, 0, 800, 10));
         obstacleList.add(new ObstacleRectangle(0, 790, 800, 800));
         obstacleList.add(new ObstacleRectangle(790, 0, 800, 800));
 
         obstacleList.add(new ObstacleRectangle(100, 100, 700, 250));
         obstacleList.add(new ObstacleRectangle(100, 350, 400, 500));
         obstacleList.add(new ObstacleRectangle(500, 350, 700, 500));
         obstacleList.add(new ObstacleRectangle(100, 600, 700, 700));
 
         //apples:
         appleList.add(new Apple(false, 75, 75));
         appleList.add(new Apple(false, 725, 75));
         appleList.add(new Apple(false, 75, 725));
         appleList.add(new Apple(false, 725, 725));
         appleList.add(new Apple(false, 450, 425));
 
         //startPositions:
         startPositionList.add(new StartPosition(275, 300, 1, 1));
         startPositionList.add(new StartPosition(550, 550, 1, 1));
 
         return new Board(core, title, obstacleList, appleList, xSize, ySize, appleEatGoal, appleVisibleAtOnce, startPositionList);
     }
 
 }
