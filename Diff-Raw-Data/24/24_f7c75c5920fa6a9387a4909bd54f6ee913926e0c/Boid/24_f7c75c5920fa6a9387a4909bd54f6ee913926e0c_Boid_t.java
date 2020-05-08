 package example.shared;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.Serializable;
 
 public class Boid implements Serializable
 {
 	private static final long serialVersionUID = -8819358195585385588L;
 
 	String name;
 
 	private int width, height;
 
 	private double xPos, yPos, speed, angle;
 
 	public Boid(String n, double x, double y, int w, int h, double s, double a)
 	{
 		name = n;
 
 		setXPos(x);
 		setYPos(y);
 		setWidth(w);
 		setHeight(h);
 		setSpeed(s);
 		angle = a;
 	}
 
 	// getters and setters
 	public int getHeight()
 	{
 		return height;
 	}
 
 	public void setHeight(int height)
 	{
 		this.height = height;
 	}
 
 	public int getWidth()
 	{
 		return width;
 	}
 
 	public void setWidth(int width)
 	{
 		this.width = width;
 	}
 
 	public double getXPos()
 	{
 		return xPos;
 	}
 
 	public void setXPos(double xPos)
 	{
 		this.xPos = xPos;
 	}
 
 	public double getYPos()
 	{
 		return yPos;
 	}
 
 	public void setYPos(double yPos)
 	{
 		this.yPos = yPos;
 	}
 
 	public double getSpeed()
 	{
 		return speed;
 	}
 
 	public void setSpeed(double speed)
 	{
 		this.speed = speed;
 	}
 
 	public double getFacing()
 	{
 		return angle;
 	}
 
 	public void setFacing(double facing)
 	{
 		this.angle = facing;
 	}
 
 	public void draw(Graphics2D g, BufferedImage i)
 	{
/*
		 AffineTransform at = new AffineTransform();
		 at.translate(-getXPos(), -getYPos());
		 at.scale(getWidth()/(double)i.getWidth(), getHeight()/(double)i.getHeight());
		 at.rotate(getFacing());
		 at.translate(getXPos(), getYPos());
		 g.drawImage(i, at, null);
*/

 		int[] xPoints = new int[3];
 		int[] yPoints = new int[3];
 
 		xPoints[0] = (int) Math.rint(xPos);
 		yPoints[0] = (int) Math.rint(yPos);
 		xPoints[1] = (int) Math.rint(xPos + width * Math.cos(getFacing()) / 2);
 		yPoints[1] = (int) Math.rint(yPos + height * Math.sin(getFacing()) / 3);
 		xPoints[2] = (int) Math.rint(xPos + width * Math.cos(getFacing() + Math.PI / 2) / 2);
 		yPoints[2] = (int) Math.rint(yPos + height * Math.sin(getFacing() + Math.PI / 2) / 3);
 
 		g.setColor(Color.MAGENTA);
 		g.fillPolygon(xPoints, yPoints, 3);
 		g.setColor(Color.ORANGE);
 		g.setFont(new Font("Times New Roman", Font.BOLD, 30));
 		g.drawString(name, (int) getXPos(), (int) getYPos());

 	}
 
 	public void changeAngle()
 	{
 		if(Math.random() < 0.95)
 		{
 			angle += Math.random() < 0.5 ? Math.PI / 2.0 : -Math.PI / 2.0;
 		}
 		while(angle < 0)
 		{
 			angle += 2 * Math.PI;
 		}
 		double quotient = (int) (angle / (Math.PI / 2.0));
 		angle %= Math.PI / 2.0;
 		if(angle > (Math.PI / 2.0) / 2.0)
 		{
 			angle = Math.abs(angle - Math.PI / 2.0);
 		}
 		angle += quotient * (Math.PI / 2.0);
 	}
 
 	public void move()
 	{
 		xPos += Math.cos(angle) * getSpeed();
 		yPos += Math.sin(angle) * getSpeed();
 	}
 
 	public String toString()
 	{
 		return "Boid:" + getXPos() + "|" + getYPos();
 	}
 }
