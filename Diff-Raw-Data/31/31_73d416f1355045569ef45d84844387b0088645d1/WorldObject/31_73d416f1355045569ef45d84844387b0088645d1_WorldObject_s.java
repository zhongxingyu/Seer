 package drinker;
 
 import java.util.ArrayDeque;
 
 public abstract class WorldObject {
     
     protected World world;
     protected int x;
     protected int y;
     
     protected ArrayDeque<ObjectEventHandler> onEnterEvent;
     
     
     public WorldObject(int x, int y){
         this.x = x;
         this.y = y;
         
         this.onEnterEvent = new ArrayDeque<ObjectEventHandler>();
     }
     
     public void addEnterHandler(ObjectEventHandler handler){
         this.onEnterEvent.add(handler);
     }
     
     public void removeEnterHandler(ObjectEventHandler handler){
         this.onEnterEvent.remove(handler);
     }
     
     public int getX() {
         return x;
     }
 
     public int getY() {
         return y;
     }
     
     public abstract char draw();
     
     public void onTick(){
         
     }
     
     public void onCollision(WorldObject o){
         
     }
     
     public void onEnter(WorldObject o){
         
         if(this.onEnterEvent.isEmpty())
             return;
         
         for (ObjectEventHandler h : this.onEnterEvent){
             h.onEvent(o);
         }
         
     }
     
     public void setWorld(World w){
         this.world = w;
     }
     
     public boolean isMovable(){
         return false;
     }
     
     public boolean isStopAble(){
         return false;
     }
     
     public boolean isSuspendAble(){
         return false;
     }
    
     public boolean isLatencySuspendAble(){
         return false;
     }
     
     public boolean isNeedPoliceHelp(){
         return false;
     }
    
    public boolean isTakePlace(){
        return true;
    }
    
     public boolean isSameLocation(WorldObject o){
         return (this.x == o.x) && (this.y == o.y);
     }
         
     public static void mutuallyCollision(WorldObject a, WorldObject b){
         a.onCollision(b);
         b.onCollision(a);
     }
        
 }
