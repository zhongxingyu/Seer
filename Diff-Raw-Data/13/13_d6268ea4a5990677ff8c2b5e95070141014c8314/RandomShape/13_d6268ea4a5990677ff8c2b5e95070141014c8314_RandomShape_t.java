 package lazygames.trainyoureye;
 
 import android.graphics.Path;
 import java.util.Random;
 import android.graphics.drawable.shapes.PathShape;
 import android.util.Log;
 
 public class RandomShape{
 	private static int canvasWidth = 0, canvasHeight = 0;
 	private int x, y;
 	private int width, height;
 	private int corners;
 	private Path path;
 	private PathShape shape;
 	public RandomShape (int x, int y, int width, int height, int corners){
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		this.corners = corners;
 		this.CreateShape();
 	}
 	public RandomShape (int x, int y, RandomShape shape){
 		this.x = x;
 		this.y = y;
 		this.width = shape.getWidth();
 		this.height = shape.getHeight();
 		this.corners = shape.getCorners();
		this.path = new Path();
		this.path.addPath(shape.getPath(), -shape.getX(), -shape.getY());
		this.shape = new PathShape(this.path, (float)canvasWidth, (float)canvasHeight);
 	}
 	private void CreateShape(){
 		int startx, starty;
 		Random r = new Random();
 		path = new Path();
 		startx = r.nextInt(width)+this.x;
 		starty = r.nextInt(height)+this.y;
 		path.moveTo(startx, starty);
 		for(int i = 0; i < this.corners - 1; i++){
 			path.lineTo(r.nextInt(width)+this.x, r.nextInt(height)+this.y);
 		}
 		path.lineTo(startx, starty);
 		path.close();
 
 		this.shape = new PathShape(path, (float)canvasWidth, (float)canvasHeight);
 	}
 	public void setCorners(int corners){
 		this.corners = corners;
 		CreateShape();
 	}
 	public Path getPath(){
 		return this.path;
 	}
 	public PathShape getShape(){
 		return this.shape;
 	}
 	public int getX(){
 		return this.x;
 	}
 	public int getY(){
 		return this.y;
 	}
 	public int getWidth(){
 		return this.width;
 	}
 	public int getHeight(){
 		return this.height;
 	}
 	public int[] getPos(){
 		return new int[] {this.x, this.y};
 	}
 	public int[] getSize(){
 		return new int[] {this.width, this.height};
 	}
 	public int[] getInfo(){
 		return new int[] {this.x, this.y, this.width, this.height};
 	}
 	public int getCorners(){
 		return this.corners;
 	}
 	static void setCanvasWidth(int width){
 		canvasWidth = width;
 	}
 	static void setCanvasHeight(int height){
 		canvasHeight = height;
 	}
 	static int getCanvasWidth(){
 		return canvasWidth;
 	}
 	static int getCanvasHeight(){
 		return canvasHeight;
 	}
 	
 }
