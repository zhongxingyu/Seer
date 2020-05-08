 
 public class Camera {
 	private Character c;
 	
 	private int x;
 	private int y;
 	
 	public static final int SCREEN_WIDTH  = 1024;
 	public static final int SCREEN_HEIGHT = 768;
 	
 	public Camera(Character ch)
 	{
 		c = ch;
 		
 		centerCamera();
 	}
 	
 	public void centerCamera()
 	{
 		x = c.getX();
 		y = c.getY();
 	}
 
 	public int getX() {
 		return (x - SCREEN_WIDTH/2);
 	}
 
 	public void setX(Cursor cur) {
		this.x = (c.getX()+cur.getGameworldX())/2;
 	}
 
 	public int getY() {
 		return (y - SCREEN_HEIGHT/2);
 	}
 
 	public void setY(Cursor cur) {
		this.y =(c.getY()+cur.getGameworldY())/2;
 	}
 }
