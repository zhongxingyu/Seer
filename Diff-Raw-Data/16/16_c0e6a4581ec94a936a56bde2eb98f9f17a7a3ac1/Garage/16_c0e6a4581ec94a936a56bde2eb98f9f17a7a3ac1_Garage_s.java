 package dk.aau.cs.giraf.cars.gamecode.GameObjects;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Point;
 import android.graphics.Rect;
 
 import dk.aau.cs.giraf.cars.R;
 import dk.aau.cs.giraf.cars.gamecode.GameObject;
 import dk.aau.cs.giraf.cars.gamecode.GameRenderer;
 import dk.aau.cs.giraf.cars.gamecode.ICollidable;
 import dk.aau.cs.giraf.cars.gamecode.IDrawable;
 import dk.aau.cs.giraf.cars.gamecode.IWorkable;
 import dk.aau.cs.giraf.cars.gamecode.MapDivider;
 
 public class Garage extends GameObject implements IDrawable, ICollidable, IWorkable {
 	Rect rectangle;
 	Rect backwall;
 	Rect doorOpen;
 	Rect frontwall;
 	Rect roof;
 
 	public Garage(int lane, int coloumn) {
 		rectangle = MapDivider.CalculateObstacle(lane, coloumn);
 
 		backwall = new Rect(rectangle.left + (int)(rectangle.width() * 0.05),
 							rectangle.top + (int)(rectangle.height() * 0.05),
 							rectangle.right - (int)(rectangle.width() * 0.24),
 							rectangle.bottom - (int)(rectangle.height() * 0.45));
 		
 		doorOpen = new Rect(rectangle.left + (int)(rectangle.width() * 0.02),
 		 					rectangle.top + (int)(rectangle.height() * 0.07),
 		 					rectangle.right - (int)(rectangle.width() * 0.08),
 		 					rectangle.bottom - (int)(rectangle.height() * 0.55));
 		
 		frontwall = new Rect(rectangle.left + (int)(rectangle.width() * 0.24),
 							 rectangle.top + (int)(rectangle.height() * 0.45),
 							 rectangle.right - (int)(rectangle.width() * 0.05),
 							 rectangle.bottom - (int)(rectangle.height() * 0.05));
 		
 		roof = new Rect(rectangle.left + (int)(rectangle.width() * 0.05),
 				 		rectangle.top + (int)(rectangle.height() * 0.05),
 				 		rectangle.right - (int)(rectangle.width() * 0.05),
 				 		rectangle.bottom - (int)(rectangle.height() * 0.55));
 	}
 
 	@Override
 	public void PerformWork() {
 		
 	}
 
 	@Override
 	public boolean collisionDetection() {
 		return false;
 	}
 
 	@Override
 	public Point[] calculateCollisionBox() {
 		Point[] collisionBox = new Point[4];
 		collisionBox[0] = new Point(-10,-10);
 		collisionBox[1] = new Point(-10,-10);
 		collisionBox[2] = new Point(-10,-10);
 		collisionBox[3] = new Point(-10,-10);
 		return collisionBox;
 	}
 
 	@Override
 	public void Draw(GL10 gl, GameRenderer spriteBatcher) {
 		spriteBatcher.draw(gl, R.drawable.garage_backwall, new Rect(0, 0, 414, 281), backwall);
 		spriteBatcher.draw(gl, R.drawable.garage_port_aaben, new Rect(0, 0, 511, 149), doorOpen);
 		spriteBatcher.draw(gl, R.drawable.garage_frontwall, new Rect(0, 0, 414, 281), frontwall);
 		spriteBatcher.draw(gl, R.drawable.garage_tag, new Rect(0, 0, 524, 164), roof);
 	}
 
 }
