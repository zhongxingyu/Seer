 package dinoForts;
 
 import main.Displayable;
 import main.LogicTicker;
 
 public abstract class Effect implements Displayable, LogicTicker{
 
     protected int lifeTime, x, y;
     
     protected float rotation;
     
     public Effect (int lifeTime) {
         this.x = 0;
         this.y = 0;
         this.rotation = 0;
         this.lifeTime = lifeTime;
     }
     
     @Override
     public final void tick(int delta) {
         this.lifeTime -= delta;
         tickEffect(delta);
     }
     
    protected void tickEffect(int delta){};
     
     @Override
     public float getX() {
         return x;
     }
 
     @Override
     public float getY() {
         return y;
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     public void setY(int y) {
         this.y = y;
     }
 
     public void setRotation(float rotation) {
         this.rotation = rotation;
     }
 
     @Override
     public float getRotation() {
         return rotation;
     }
 
     public boolean isDead () {
         return lifeTime <= 0;
     }
     
 }
