 package studieprogresjon;
 
 import studieprogresjon.Position;
 
 /**
  * Abstract Entity class.
  * Common stuff for game entities
  * 
  * @package studieprogresjon 
  * @author Raymond Julin
  */
 public abstract class Entity {
     Position pos;
     boolean collided = false;
     Entity collidedWith;
 
     public abstract char getSymbol();
 
     public Entity(Position p) {
         this.pos = p;
     }
 
     
     /**
      * Set position for Entity
      *
      * @return void
      * @param Position p
      */
     public void setPosition(Position p) {
         this.pos = p;
     }
 
     /**
      * Return position for Entity
      *
      * @return Position
      */
     public Position getPosition() {
         return this.pos;
     }
 
     /**
      * Return x position for Entity
      *
      * @return int
      */
     public int getX() {
         return pos.getX();
     }
 
     /**
      * Return y position for Entity
      *
      * @return int
      */
     public int getY() {
         return pos.getY();
     }
 
     /**
      * State whether Entity is collided or not
      *
      * @return boolean
      */
     public boolean isCollided() {
         return this.collided;
     }
 
     /**
      * Set Entity to be collided
      *
      * @return void
      * @param boolean state
      */
     public void setCollided(boolean state) {
         this.collided = state;
     }
 
     /**
      * Set Entity to be collided with following entity
      *
      * @return void
      * @param Entity e
      */
     public void setCollidedWith(Entity e) {
         this.collidedWith = e;
     }
 
     public Entity getCollidedWith() {
         return this.collidedWith;
     }
 
     /**
      * Check for collision against table of other Entities
      *
      * @return boolean
      * @param Entity[] entities
      */
     public boolean checkCollision(Entity[] entities) {
         for (Entity e: entities) {
            if (this.getPosition().equals(e.getPosition())) {
                if (this != e) {
                    this.setCollidedWith(e);
                    this.setCollided(true);
                    return true;
                 }
             }
         }
         return false;
     }
 }
