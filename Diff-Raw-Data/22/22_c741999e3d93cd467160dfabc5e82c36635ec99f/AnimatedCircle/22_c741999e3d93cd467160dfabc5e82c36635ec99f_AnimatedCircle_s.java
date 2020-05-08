 package be.wide.dom;
 
 import java.awt.*;
 import java.util.*;
 
 import processing.core.PGraphics;
 
 /**
  * 
  * @author Maarten Taeymans
  *
  */
 public class AnimatedCircle 
 {
 	private float radius;
 	private float xPos;
 	private float yPos;
 	private Color col;
 	private float alpha;
 	private boolean exist = true;
 	private Random rand;
 	private boolean canDraw = false;
 	private float waitTimer = 0;
 
 	/**
 	 * Standard constructor.
 	 * @param width width of screen
 	 * @param height height of screen
 	 */
 	public AnimatedCircle(int width, int height)
 	{
 		rand = new Random();
 
 		xPos = rand.nextInt(width);
 		yPos = rand.nextInt(height);
 
 		col = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
 
 		radius = rand.nextInt(250) + 50;
 
 		alpha = 255;
 		
		waitTimer = rand.nextInt(200);
 	}
 
 	/**
 	 * Draws the circle on the specified canvas.
 	 * @param gl PGraphics canvas
 	 */
 	public void draw(PGraphics gl)
 	{
 		if (exist && canDraw)
 		{
 			gl.noStroke();
 			gl.fill(col.getRed(), col.getGreen(), col.getBlue(), alpha);
 			gl.ellipse(xPos, yPos, radius, radius);
 			alpha -= (rand.nextInt(8) + 2);
 
 			if (alpha <= 0)
 			{
 				exist = false;
 			}
 		}
 		waitTimer--;
 		
 		if (waitTimer <= 0)
 		{
 			activate();
 		}
 	}
 	
 	/**
 	 * Stops the circle from drawing.
 	 */
 	public void stop()
 	{
 		exist = false;
 		canDraw = false;
 	}
 	
 	/**
 	 * Activates the draw loop.
 	 */
 	private void activate()
 	{
 		canDraw = true;
 	}
 
 	/**
 	 * Gets the radius of the circle.
 	 * @return radius
 	 */
 	public float getRadius() {
 		return radius;
 	}
 
 	/**
 	 * Sets the radius of the circle.
 	 * @param radius radius
 	 */
 	public void setRadius(float radius) {
 		this.radius = radius;
 	}
 
 	/**
 	 * Gets the x-position of the circle.
 	 * @return x-position
 	 */
 	public float getxPos() {
 		return xPos;
 	}
 
 	/**
 	 * Sets the x-position of the circle.
 	 * @param xPos x-position
 	 */
 	public void setxPos(float xPos) {
 		this.xPos = xPos;
 	}
 
 	/**
 	 * Gets the y-position of the circle.
 	 * @return y-position
 	 */
 	public float getyPos() {
 		return yPos;
 	}
 
 	/**
 	 * Sets the y-position of the circle.
 	 * @param yPos y-position
 	 */
 	public void setyPos(float yPos) {
 		this.yPos = yPos;
 	}
 
 	/**
 	 * Gets the color of the circle.
 	 * @return color
 	 */
 	public Color getCol() {
 		return col;
 	}
 
 	/**
 	 * Sets the color of the circle.
 	 * @param col color
 	 */
 	public void setCol(Color col) {
 		this.col = col;
 	}
 
 	/**
 	 * Gets the transparency of the circle.
 	 * @return transparency
 	 */
 	public float getAlpha() {
 		return alpha;
 	}
 
 	/**
 	 * Sets the transparency of the circle.
 	 * @param alpha transparency
 	 */
 	public void setAlpha(float alpha) {
 		this.alpha = alpha;
 	}
 }
