 package Source;
 
 import Util.CPosition;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bohnp1
  * Date: 27.09.13
  * Time: 14:09
  */
 public abstract class CWalker {
    static Integer idCounter = 0;
 
     static Integer getNextId()
     {
         return idCounter++;
     }
 
 
    protected Integer id;
    protected CPosition currentPosition;
    protected CPosition targetPosition;
    protected CPosition startPosition;
    protected CPosition desiredNextPosition;
 
    protected Double halfWalkerSize = 5.0;
    protected Double stepSize = 2.0;
 
    protected LinkedList<CPosition> desiredPath = new LinkedList<CPosition>();
    protected CCollisionList collisionWith = null;
    protected boolean hasCollisionHandled = false;
 
    public CWalker(CPosition start, CPosition target) {
        this.startPosition = start;
        this.currentPosition = start;
        this.targetPosition = target;
        this.id = getNextId();
    }
 
    public CPosition getPosition()
    {
        return currentPosition;
    }
 
    public CPosition getTarget()
    {
        return targetPosition;
    }
 
    public CPosition getDesiredNextPosition() {
        if( desiredNextPosition == null) {
            return currentPosition;
        }
        else {
            return desiredNextPosition;
        }
    }
 
    public Double getHalfWalkerSize() {
        return halfWalkerSize;
    }
 
     public Boolean hasCollisions() {
         return this.collisionWith != null && this.collisionWith.hasCollisions();
     }
 
     public void setDesiredPath(LinkedList<CPosition> vertexes) {
         if(vertexes == null || vertexes.size() == 0) {
             throw new IllegalArgumentException("Der desiredPath darf nicht Null oder leer sein --> vermutlich kam Dijsktra zu keinem Ergebnis!");
         }
 
         this.desiredPath = vertexes;
 
         if(currentPosition.isNearBy(this.desiredPath.getFirst(), stepSize / 0.5)) {
             // the first checkpoint is the current position, remove it from our list
             this.desiredPath.removeFirst();
         }
     }
 
     public LinkedList<CPosition> getDesiredPath() {
         return this.desiredPath;
     }
 
     public void resetCollisions() {
         if(this.collisionWith != null) {
             this.collisionWith.clear();
         }
         hasCollisionHandled = false;
     }
 
     public CCollisionList getCollisionWith() {
         return this.collisionWith;
     }
 
     public void setCollisionWith(CCollisionList list) {
         this.collisionWith = list;
     }
 
     /**
      * checks if the walker has a collision with the desiredNextPosition of another walker and remembers the collision
      * @param other the walker
      * @param rememberCollision true if the walker should remember this collision
      * @return true if we have a collision or false if not
      */
     public boolean checkCollisionWith(CWalker other, boolean rememberCollision) {
 
         if(this.equals(other)) {
             return false;
         }
 
         boolean hasCollision = this.getDesiredNextPosition().getDistanceTo(other.getDesiredNextPosition()) < this.getHalfWalkerSize() + other.getHalfWalkerSize() + 1;
 
         if(hasCollision && rememberCollision) {
 
             CCollisionList newList;
             if( !this.hasCollisions() && !other.hasCollisions() ) { // case 1: none of both walker have a collision until now
                 newList = new CCollisionList();
             }
             else if( this.hasCollisions() && !other.hasCollisions() ) { // case 2: walker A has a collision, walker B not
                 newList = this.getCollisionWith();
             }
             else if( !this.hasCollisions() && other.hasCollisions() ) { // case 3: walker B has a collision, walker A not
                 newList = other.getCollisionWith();
             }
             else { // case 4: both walkers have a collision until now
                 newList = this.getCollisionWith();
                 newList.merge(other.getCollisionWith());
             }
 
             newList.addCollision(this, other);
 
             this.setCollisionWith(newList);
             other.setCollisionWith(newList);
         }
 
         return hasCollision;
     }
 
     /**
      * checks if the walker has a collision with an obstacle and remembers the collision
      * @param obstacle the obstacle
      * @param rememberCollision true if the walker should remember this collision
      * @return true if we have a collision or false if not
      */
     public boolean checkCollisionWith(CObstacle obstacle, boolean rememberCollision) {
 
        boolean hasCollision = obstacle.getDistanceTo(this.getDesiredNextPosition()) < this.getHalfWalkerSize() + 0.1;
 
         if(hasCollision && rememberCollision) {
 
             if(this.collisionWith == null) {
                 this.collisionWith = new CCollisionList();
             }
 
             this.collisionWith.addCollision(obstacle, this);
         }
 
         return hasCollision;
     }
 
     /**
      * calculates the next position and saves the result to the nextDesiredPosition member var
      * @param roundCount the calculation round count
      * @return true if the new position has no collision with others
      */
     public abstract boolean calcNextDesiredPosition(Integer roundCount);
 
     /**
      * walks a step to the next desired Position and resets desiredNextPosition member variable to NULL
      * @return true if ok or false if the walker is on the target
      */
     public boolean walkToNextDesiredPosition() {
 
         if(this.hasCollisions()) {
             throw new IllegalArgumentException("The Walker must no be blocked!");
         }
 
         if(this.desiredNextPosition == null) {
             throw new IllegalArgumentException("this.desiredNextPosition must not be NULL!");
         }
 
         if(this.desiredPath.size() > 0) {
             if(this.desiredNextPosition.isNearBy(this.desiredPath.getFirst(), stepSize / 0.5)) {
                 // Yes, we reached a checkpoint, remove it from our list
                 this.desiredPath.removeFirst();
             }
 
             this.currentPosition = this.desiredNextPosition;
         }
 
         this.desiredNextPosition = null;
         return this.desiredPath.size() == 0;
     }
 
     /**
      * Get the unique Identifier of the Walker
      * @return unique Identifier
      */
     public Integer getId ()
     {
         return id;
     }
 
     @Override
     public boolean equals (Object obj) {
         return obj.getClass() == this.getClass() && ((CWalker)obj).getId().equals(this.getId());
     }
 }
