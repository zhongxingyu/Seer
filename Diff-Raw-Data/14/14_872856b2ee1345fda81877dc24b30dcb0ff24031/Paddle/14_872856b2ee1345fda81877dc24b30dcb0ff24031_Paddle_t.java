 package tutorials.slickout.gameplay.level;
 
 import org.newdawn.slick.Animation; import org.newdawn.slick.Input; import org.newdawn.slick.MouseListener; import org.newdawn.slick.geom.Shape; import org.newdawn.slick.geom.Vector2f;
 
 public class Paddle extends CollidableAnimationObject implements MouseListener {
 
 public static enum PAD_STATE {NORMAL, STICKY};
 
 private PAD_STATE currentState;
 
 public Paddle(String name, Animation animation, Vector2f position,
 		Shape collisionShape, int collisionType) {
 	super(name, animation, position, collisionShape, collisionType);
 	
 	currentState = PAD_STATE.STICKY;
 }
 
 public PAD_STATE getState(){
 	return currentState;
 }
 
 public void setState(PAD_STATE newState){
 	currentState = newState;
 }
 @Override
 public void mouseClicked(int button, int x, int y, int clickCount) {
 }
 @Override
 public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 	int change = newx - oldx;
	if(getPosition().x + change >= 10 && getPosition().x + change <= 790 - super.collisionShape.getWidth()){
 		setPosition(new Vector2f( getPosition().x + change, getPosition().y) );
 	}else if(getPosition().x + change < 10){
 		setPosition(new Vector2f( 10, getPosition().y) );
	}else if(getPosition().x + change > 790 - super.collisionShape.getWidth()){
		setPosition(new Vector2f( 790 - super.collisionShape.getWidth(), getPosition().y) );
 	}
 	//System.out.println("newx "+ newx + " padPosition "+getPosition().x);
 }
 @Override
 public void mousePressed(int button, int x, int y) {
 	if(currentState == PAD_STATE.STICKY){
 		currentState = PAD_STATE.NORMAL;
 	}
 }
 @Override
 public void mouseReleased(int button, int x, int y) {
 }
 @Override
 public void mouseWheelMoved(int change) {
 }
 @Override
 public void inputEnded() {
 
 }
 @Override
 public boolean isAcceptingInput() {
 	return true;
 }
 @Override
 public void setInput(Input arg0) {	
 }
 
 @Override
 public void inputStarted() {
 	// TODO Auto-generated method stub
 	
 }
 
 @Override
 public void mouseDragged(int oldx, int oldy, int newx, int newy) {
 	// TODO Auto-generated method stub
 	
 }
 }
