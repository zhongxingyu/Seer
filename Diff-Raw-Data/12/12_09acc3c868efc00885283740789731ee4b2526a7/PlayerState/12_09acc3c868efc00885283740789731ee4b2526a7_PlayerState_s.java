 package eu32k.ludumdare.ld26.state;
 
 import eu32k.ludumdare.ld26.Player;
 
 public class PlayerState extends GameState {
    private Player player;
    
    @Override
    public void init() {
       player = new Player(0, 0);
    }
 
   public void initLevel(float x, float y)
    {
      player.setPosition(x, y);
    }
    
    @Override
    public void enter() {
       
    }
 
    @Override
    public void destroy() {
       // TODO Auto-generated method stub
 
    }
 
    public Player getPlayer() {
       return player;
    }
 
    public void setPlayer(Player player) {
       this.player = player;
    }
 
 }
