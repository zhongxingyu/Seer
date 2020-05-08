 package swarm;
 
 public class Hitbox{
 	private int width;
 	private int height;
 	private int x;
 	private int y;
 	
 	public Hitbox(int x, int y, int width, int height)
 	{
 		setWidth(width);
 		setHeight(height);
 		setX(x);
 		setY(y);
 	}
 	
 	public boolean checkCollision(Hitbox box)
 	{
 		if(box==null)
 			return false;
 		if((box.x > x && box.x < x + width) ||(box.x + box.width > x && box.x + box.width < x + width)){
			if((box.y > y && box.y < y + height) ||(box.y + box.height > y && box.y + box.height < y + height)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 }
