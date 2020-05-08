 /**
  * @author Kuang Han
  */
 
 
 package collision;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import charactersprites.GameElement;
 
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.collision.CollisionShape;
 
 import physicsengine.NewtonianCollision;
 
 public class GameElementCollision extends NewtonianCollision{
     protected List<GameElement> myList;
 
     public GameElementCollision() {
         myList = new ArrayList<GameElement>();
     }
 
     public void addSpriteGroup(SpriteGroup group) {
         Sprite[] members = group.getSprites();
         for (Sprite s:members) {
             if (s != null) {
                 if (s instanceof GameElement) {
                     myList.add((GameElement) s);
                 }
                 else {
                     System.err.println("Not a game element!");
                 }
             }
         }
     }
     
     public void addSprite(GameElement e) {
         myList.add(e);
     }
 
     @Override
     public void checkCollision() {
         int size = myList.size();
         
         GameElement sprite1, sprite2; 
         CollisionShape shape1, shape2; 
 
         for (int i = 0; i < size; i++) {
             sprite1 = myList.get(i);
             if (!sprite1.isActive()
                     || (shape1 = this.getCollisionShape1(sprite1)) == null) {
                 continue;
             }
             
             for (int j = i+1; j < size; j++) {
                 sprite2 = myList.get(j);
                 if (!sprite2.isActive() || sprite1 == sprite2
                         || (shape2 = this.getCollisionShape2(sprite2)) == null) {
                     continue;
                 }
                 
                 if (sprite1.isUnmovable() && sprite2.isUnmovable()) {
                     continue;
                 }
                 
                 if (this.isCollide(sprite1, sprite2, shape1, shape2)) {
                     this.collided(sprite1, sprite2);
                     if (!sprite1.isActive()
                             || (shape1 = this.getCollisionShape1(sprite1)) == null) {
                         // collided sprite has been dead
                         break;
                     }
                 }
             }
         }
     }
 
 
     @Override
    public void collided(Sprite s1, Sprite s2) { 
         switch (this.collisionSide) {
         case RIGHT_LEFT_COLLISION:{
             ((GameElement)s1).beforeCollidedWith((GameElement)s2, RIGHT_LEFT_COLLISION);
             ((GameElement)s2).beforeCollidedWith((GameElement)s1, LEFT_RIGHT_COLLISION);
             super.collided(s1, s2);
             ((GameElement)s1).afterCollidedWith((GameElement)s2, RIGHT_LEFT_COLLISION);
             ((GameElement)s2).afterCollidedWith((GameElement)s1, LEFT_RIGHT_COLLISION);
             break;
         }
         case LEFT_RIGHT_COLLISION:{
             ((GameElement)s1).beforeCollidedWith((GameElement)s2, LEFT_RIGHT_COLLISION);
             ((GameElement)s2).beforeCollidedWith((GameElement)s1, RIGHT_LEFT_COLLISION);
             super.collided(s1, s2);
             ((GameElement)s1).afterCollidedWith((GameElement)s2, LEFT_RIGHT_COLLISION);
             ((GameElement)s2).afterCollidedWith((GameElement)s1, RIGHT_LEFT_COLLISION);
             break;
         }
         case BOTTOM_TOP_COLLISION:{
             ((GameElement)s1).beforeCollidedWith((GameElement)s2, BOTTOM_TOP_COLLISION);
             ((GameElement)s2).beforeCollidedWith((GameElement)s1, TOP_BOTTOM_COLLISION);
             super.collided(s1, s2);
             ((GameElement)s1).afterCollidedWith((GameElement)s2, BOTTOM_TOP_COLLISION);
             ((GameElement)s2).afterCollidedWith((GameElement)s1, TOP_BOTTOM_COLLISION);
             break;
         }
         case TOP_BOTTOM_COLLISION:{
             ((GameElement)s1).beforeCollidedWith((GameElement)s2, TOP_BOTTOM_COLLISION);
             ((GameElement)s2).beforeCollidedWith((GameElement)s1, BOTTOM_TOP_COLLISION);
             super.collided(s1, s2);
             ((GameElement)s1).afterCollidedWith((GameElement)s2, TOP_BOTTOM_COLLISION);
             ((GameElement)s2).afterCollidedWith((GameElement)s1, BOTTOM_TOP_COLLISION);
             break;
         }
         }
     }
     
     @Override
     public void setCollisionGroup(SpriteGroup group1, SpriteGroup group2) {
         System.err.println("Forbidden Method");
     }
     
     @Override
     public SpriteGroup getGroup1() {
         System.err.println("Forbidden Method");
         return null;
     }
     
     @Override
     public SpriteGroup getGroup2() {
         System.err.println("Forbidden Method");
         return null;
     }
     
 }
