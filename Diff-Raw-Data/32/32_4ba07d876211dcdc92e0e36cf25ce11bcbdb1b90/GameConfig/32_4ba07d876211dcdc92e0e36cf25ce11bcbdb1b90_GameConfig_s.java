 package GameConfig;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: User
  * Date: 20.08.13
  * Time: 17:45
  * To change this template use File | Settings | File Templates.
  */
 public class GameConfig {
     public static int windowXSize = 800;
     public static int windowYSize = 600;
     public static int gameFieldXSize = 400;
     public static int gameFieldYSize = 300;
     public static int helpMessageXSize = 400;
     public static int helpMessageYSize = 300;
     public static int helpMessageX = 100;
     public static int helpMessageY = 50;
     public static int barXSize = 100;
     public static int barYSize = 5;
     public static int barRotateSize = 34;
    public static int countMatches = 13;
 
     public static final Map<Integer, String> textureNames;
     static {
         textureNames = new HashMap<Integer, String>();
         textureNames.put(1, "../wood1.jpg");
         textureNames.put(2, "../wood2.jpg");
         textureNames.put(3, "../wood3.jpg");
         textureNames.put(4, "../wood4.jpg");
     }
 
     public static final Map<String, String> errorMessages;
     static {
         errorMessages = new HashMap<String, String>();
         errorMessages.put("countError", "Вы можете взять от одной до 10 спичек");
         errorMessages.put("remainedError", "У Вас уже нет такого количества спичек!");
     }
 
     public static final Map<Integer, String> whiteBasketTextureNames;
     static {
         whiteBasketTextureNames = new HashMap<Integer, String>();
         whiteBasketTextureNames.put(1, "../whiteBasket.jpg");
         whiteBasketTextureNames.put(2, "../whiteBasket2.jpg");
     }
 
     public static final Map<Integer, String> blackBasketTextureNames;
     static {
         blackBasketTextureNames = new HashMap<Integer, String>();
         blackBasketTextureNames.put(1, "../blackBasket.jpg");
         blackBasketTextureNames.put(2, "../blackBasket2.jpg");
     }
 
     public static final Map<String, Map> basketTextureNames;
     static {
         basketTextureNames = new HashMap<String, Map>();
         basketTextureNames.put("white", whiteBasketTextureNames);
         basketTextureNames.put("black", blackBasketTextureNames);
     }
 
     public static Integer rightBoxX = (windowXSize - gameFieldXSize)/2 + gameFieldXSize + 20;
     public static Integer rightBoxY = (windowYSize - gameFieldYSize)/2 + gameFieldYSize + 20;
     public static Integer leftBoxX = 0;
     public static Integer leftBoxY = (windowYSize - gameFieldYSize)/2 + gameFieldYSize + 20;
     public static Integer xBoxSize = gameFieldXSize/2 - 20;
     public static Integer yBoxSize = gameFieldYSize/2 - 20;
 
     public static final Map<String, Map<String, Integer>> basketCoordinates;
     static {
         basketCoordinates = new HashMap<String, Map<String, Integer>>();
 
         Map <String, Integer> basketWhiteCoordinates = new HashMap<String, Integer>();
         basketWhiteCoordinates.put("x", rightBoxX);
         basketWhiteCoordinates.put("y",rightBoxY);
         basketWhiteCoordinates.put("xSize", xBoxSize);
         basketWhiteCoordinates.put("ySize", yBoxSize);
         basketWhiteCoordinates.put("counterX", rightBoxX);
         basketWhiteCoordinates.put("counterY", rightBoxY-50);
 
         Map <String, Integer> basketBlackCoordinates = new HashMap<String, Integer>();
         basketBlackCoordinates.put("x", leftBoxX);
         basketBlackCoordinates.put("y", leftBoxY);
         basketBlackCoordinates.put("xSize", xBoxSize);
         basketBlackCoordinates.put("ySize", yBoxSize);
         basketBlackCoordinates.put("counterX", leftBoxX);
         basketBlackCoordinates.put("counterY", leftBoxY-50);
 
         basketCoordinates.put("white", basketWhiteCoordinates);
         basketCoordinates.put("black", basketBlackCoordinates);
     }
 
     public static final Map<String,String> strategyTitles;
     static {
         strategyTitles = new HashMap<String, String>();
         strategyTitles.put("userToUser", "Многопользовательский режим");
         strategyTitles.put("userToRobot", "Пользователь против компьютера");
         strategyTitles.put("robotToRobot", "Компьютер против компьютера");
     }
 }
