 package game;
 
 /**
  *
  * @author Nikolaos
  */
 import java.awt.Polygon;
 
 public class BonusDrop extends MapObject {
     final public static int TIME_TO_LIVE = 3;
     final public static int BOMB_BONUS_DROP = 0;
     final public static int LIFE_BONUS_DROP = 1;
     final public static int SHIELD_THREE_POINTS_DROP = 2;
     final public static int SHIELD_TWO_POINTS_DROP = 3;
     final public static int SHIELD_ONE_POINT_DROP = 4;
     
     private int type;
     private int ttl;
     
    BonusDrop(int[] coordinates, GameState gameState, int type)
     {
         super(new int[] {0, 0} , 0, coordinates, 0, gameState);
         this.type = type;
         this.ttl = TIME_TO_LIVE;
     }
     
     public int getType()
     {
         return this.type;
     }
     
     public int getTTL()
     {
         return this.ttl;
     }
     
     public void setTTL(int ttl)
     {
         this.ttl = ttl;
     }
     
     public void destroy()
     {
         getGameState().removeBonusDrop(this);
     }
 }
