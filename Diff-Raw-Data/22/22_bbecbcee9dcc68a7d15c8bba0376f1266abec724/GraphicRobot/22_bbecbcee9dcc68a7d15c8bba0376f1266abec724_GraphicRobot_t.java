 package factory.graphics;
 
 import java.awt.Graphics;
 import java.awt.Image;
 
 public class GraphicRobot extends GraphicAnimatedObject
 {
 	int fx, fy;		// final position (destination)
 	int destinationNest, destinationKit;
 	boolean arrived;
 	int state;
 	
 	public void setDestination(int init_fx, int init_fy)
 	{
 		fx = init_fx;
 		fy = init_fy;
 	}
 
 	public void setState(int init_state)
 	{
 		state = init_state;
 	}
 	public void adjustShift(int amount)
 	{
 		x -= amount;
 	}
 	public int getState()
 	{
 		return state;
 	}
 	public void move()
 	{
 		//System.out.println(fx + " " + fy);

		if(x < fx)
		{
			if(theta <= 180 && theta > 0)
				theta -= dtheta;
			else if(theta > 180 && theta < 360)
				theta += dtheta;
			else
				x += dx;
		}
		else if(y == fy && x == fx)	// robot has arrived at destination
 		{
 			if(state % 2 == 1)
 				state += 1;
 		}
 		else if(y > fy)
 		{
 			if(theta > 90 && theta < 270)
 				theta -= dtheta;
 			else if(theta < 90 || theta >= 270)
 				theta += dtheta;
 			else
 				y -= dy;
 		}
 		else if(y < fy)
 		{
 			if(theta > 90 && theta < 270)
 				theta += dtheta;
 			else if(theta <= 90 || theta > 270)
 				theta -= dtheta;
 			else
 				y += dy;
 		}
 		else if(x > fx)
 		{
 			if(theta < 180 && theta >= 0)
 				theta += dtheta;
 			else if(theta > 180 && theta <= 360)
 				theta -= dtheta;
 			else
 				x -= dx;
 		}
 		if(theta < 0) theta = 360;
 		else if(theta > 360) theta = 0;
 	}
 	public void paint(Graphics g)
 	{
 		// Draw the parts robot itself
 		g.drawImage(image, x, y, imageWidth, imageHeight, null);
 	}
 }
