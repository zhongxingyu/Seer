 package factory.graphics;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 class AnimatedObject
 {
 	protected int x, y;							// current position
 	protected int dx, dy;						// change in position
 	protected int theta;						// image angle
 	protected int dtheta;						// change in image angle
 	protected int imageWidth, imageHeight;		// image size
 	protected Image image;
 	protected ArrayList<GraphicItem> items;			// inventory of items
 	
 	// Constructors
 	public AnimatedObject()
 	{
 		
 	}
 	public AnimatedObject(int init_x, int init_y, int init_theta, int init_dx, int init_dy, int init_dtheta, int init_imageWidth, int init_imageHeight, String init_imagePath)
 	{
 		x = init_x;
 		y = init_y;
 		theta = init_theta;
 		dx = init_dx;
 		dy = init_dy;
 		dtheta = init_dtheta;
 		imageWidth = init_imageWidth;
 		imageHeight = init_imageHeight;
 		image = Toolkit.getDefaultToolkit().getImage(init_imagePath);
 	}
 	
 	// Set functions
 	public void setPosition(int init_x, int init_y)
 	{
 		x = init_x;
 		y = init_y;
 	}	
 	public void setSpeed(int init_dx, int init_dy)
 	{
 		dx = init_dx;
 		dy = init_dy;
 	}
 	public void setImage(String imagePath)
 	{
 		image = Toolkit.getDefaultToolkit().getImage(imagePath);
 	}
 	public void setAngle(int init_theta)
 	{
 		theta = init_theta;
 	}
 	
 	// Get functions
 	public int getX()
 	{
 		return x;
 	}
 	public int getY()
 	{
 		return y;
 	}
 	public Image getImage()
 	{
 		return image;
 	}
 	public int getAngle()
 	{
 		return theta;
 	}
 	public int getImageWidth()
 	{
 		return imageWidth;
 	}
 	public int getImageHeight()
 	{
 		return imageHeight;
 	}

 	// Update functions
 	public void move()
 	{
 		x += dx;
 		y += dy;
 		theta += dtheta;
 	}
 }
