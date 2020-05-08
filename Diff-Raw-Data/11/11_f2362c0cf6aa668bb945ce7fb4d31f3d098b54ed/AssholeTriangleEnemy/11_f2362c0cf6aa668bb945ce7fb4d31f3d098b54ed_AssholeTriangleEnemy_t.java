 package jam.ld26.entities;
 
 import infinitedog.frisky.physics.PhysicsManager;
 import jam.ld26.game.C;
 import jam.ld26.levels.Level;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 
 class AssholeTriangleEnemy extends Enemy {
     private State state;
     private static int ANGRY_TILES = 4;
    private double speed = .08;
     
     static enum State { 
         IDLE("idle", 1), 
         ANGRY("angry", 0);
         
         public String name;
         public Integer tileId;
 
         private State(String name, Integer tileId) {
             this.name = name;
             this.tileId = tileId;
         }
     };
          
     public AssholeTriangleEnemy(float x, float y, Level lvl) {
         super(x,y,lvl);
         this.state = State.IDLE;
     }
     
     public AssholeTriangleEnemy(Vector2f position, Level lvl) {
         this(position.x, position.y, lvl);
     }
      
     @Override
     public void render(GameContainer gc, Graphics g) {
         super.render(gc, g);
         tileSet.render(this.state.tileId, getX(), getY());
     }
 
     public void update(GameContainer gc, int delta) {
         super.update(gc, delta);
         Vector2f newPosition = new Vector2f(getX(), getY());
         Player player = lvl.getPlayer();
         PhysicsManager pm = PhysicsManager.getInstance();
        int distanceInTiles = (int) ((player.getX() - getX()) / lvl.getTileSize());
         
         if (state == State.ANGRY) {
            if (Math.abs(distanceInTiles) > ANGRY_TILES || Math.abs(getY()-player.getY()) > lvl.getTileSize()) {
                 state = State.IDLE;
             } else {
                 if (distanceInTiles > 0) {
                     newPosition.x = (float) (newPosition.x + speed * delta);
                 } else {
                     newPosition.x = (float) (newPosition.x - speed * delta);
                 }
                 this.setPosition(newPosition);
             }
         } else {
            if (Math.abs(distanceInTiles) <= ANGRY_TILES && Math.abs(getY()-player.getY()) < lvl.getTileSize()) {
                 state = State.ANGRY;
             }
         }
     }
             
     @Override
     public int getType() {
         return C.Enemies.ASSHOLE_TRIANGLE_ENEMY.id;
     }
 }
