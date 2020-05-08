 package game;
 
 /**
  *
  * @author Michael
  */
 public class MapObjectTTL extends MapObject {
 
    final static private int TIME_TO_LIVE = 4;
     private int ttl;
 
     MapObjectTTL(float[] velocity, float heading, int[] coordinates, float acceleration, GameState gameState) {
         super(velocity, heading, coordinates, acceleration, gameState);
         this.ttl = TIME_TO_LIVE;
     }
 
     /**
      * Returns the value of the Time to Live property.
      *
      * @return value of the time to live
      */
     public int getTTL() {
         return this.ttl;
     }
 
     /**
      * Sets the Time to Live property.
      *
      * @param ttl
      */
     public void setTTL(int ttl) {
         this.ttl = ttl;
     }
 
     @Override
     public void destroy() {
         getGameState().removeExplosion(this);
     }
 }
