 /**
  *
  * @author Anthony
  */
 
 public class Projectile extends MapObject {
     public final static int[] PROJECTILE_VELOCITY = {10, 10};
     public final static int TIME_TO_LIVE = 5; //5 seconds
    public final static int PLAYER_OWNER = 1;
    public final static int ALIEN_OWNER = 2;
     
     private int velocity; // to be fixed
     private int ttl;
    private int owner;
     
     
    public Projectile(Ship ship, int[] velocity, int heading, int[] coordinates, GameState gameState){     
        super(velocity, heading, coordinates, 0, gameState);
        this.ttl = TIME_TO_LIVE;
        
        if(ship instanceof PlayerShip)
            this.owner = PLAYER_OWNER;
        else
            this.owner = ALIEN_OWNER;
     }
     
     public int getTTL(){
         return this.ttl;
     }
     
     public void setTTL(int ttl){
         this.ttl = ttl;
     }
 }
