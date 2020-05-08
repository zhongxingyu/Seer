 package JGame.GameObject;
 
 import JGame.Actions.CreateAction;
 import JGame.Actions.DestroyAction;
 import JGame.Actions.MoveAction;
 import JGame.Events.CollisionEvent;
 import JGame.Events.CustomEvent;
 import JGame.Events.KeyboardEvent;
 import JGame.Events.MouseEvent;
 import JGame.Room.Room;
 import java.awt.Image;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.event.KeyListener;
 import java.awt.geom.Rectangle2D;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Ryan
  */
 public class GameObject extends JPanel{
 
     protected Sprite sprite;
     protected boolean isSolid = false;
     protected boolean isPrecise = false;
     protected boolean canLeaveScreen = true;
     protected Object collider = null;
     protected int x = 0, y = 0;
     protected Room room;
     protected int startX = 0, startY = 0;
     protected String reference = "";
     protected GameObject collidedWith = null;
     // Preset Actions
     public CreateAction create = new CreateAction();
     public DestroyAction destroy;
     public MoveAction move;
     // Preset Events
     public CollisionEvent collide = new CollisionEvent();
     public KeyboardEvent keyboard;
     public MouseEvent mouse;
     public CustomEvent custevt;
     // X/Y Movement settings
     public boolean needsToMoveX = false, needsToMoveY = false;
     public int moveAmountX = 0, moveAmountY = 0;
     public int moveEndX = 0, moveEndY = 0;
     public int moveSpeedX = 0, moveSpeedY = 0;
     public boolean markedForDeletion = false;
 
     public GameObject(String reference, Sprite sprite, Room room){
         this.sprite = sprite;
         this.room = room;
         this.move = new MoveAction(this.room, this);
         this.destroy = new DestroyAction(this.room, this);
         this.reference = reference;
 
         this.keyboard = new KeyboardEvent(this.room);
         this.mouse = new MouseEvent(this.room);
         this.custevt = new CustomEvent(this.room);
     }
 
     public void setSolid(boolean isSolid){
         this.isSolid = isSolid;
     }
 
     public void addKeyEvent(KeyListener key){
         this.setFocusable(true);
         this.addKeyListener(key);
     }
 
     public void createCollider(boolean isPrecise){
         this.isPrecise = isPrecise;
         Object c;
         if(!this.isPrecise){
             c = new Polygon();
         }else{
             c = new Rectangle(this.sprite.getWidth(), this.sprite.getWidth());
         }
         this.collider = c;
     }
 
     public boolean collision(GameObject object){
         Rectangle2D cSelf = (Rectangle2D)this.collider;
         Rectangle2D cOther = (Rectangle2D)this.collider;
         if(cSelf.intersects(cOther)){
             return true;
         }else{
             return false;
         }
     }
 
     public void setReference(String reference){
         this.reference = reference;
     }
 
     public String getReference(){
         return this.reference;
     }
 
     @Override
     public int getWidth(){
         return this.sprite.sprite.getWidth();
     }
 
     @Override
     public int getHeight(){
         return this.sprite.getHeight();
     }
 
     public void setX(int x){
         this.x = x;
     }
 
     @Override
     public int getX(){
         return this.x;
     }
 
     public void setY(int y){
         this.y = y;
     }
 
     @Override
     public int getY(){
         return this.y;
     }
 
     public Image getSprite(){
         return sprite.sprite;
     }
 
     public void setLeaveScreen(boolean canLeave){
         this.canLeaveScreen = canLeave;
     }
 
     public boolean getLeaveScreen(){
         return this.canLeaveScreen;
     }
 
    public GameObject getColliededWith(){
         return this.collidedWith;
     }
 
    public void setColliededWith(GameObject go){
         this.collidedWith = go;
     }
 }
