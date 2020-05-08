 package me.paulrose.lptc.simulator;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 
 
 public class Food extends Entity
 {
 	
 	private String type;
 	private int amount, density;
 	private float carrySize, centerX, centerY;
 	private boolean drawType;
 	private Color droppedColor;
 	
 
 	public Food(String n, int density, int amount, int x, int y, 
 			Color colour, Color droppedColor, World wo)
 	{	
 		super(n, x, y, 0, 0, wo);
 		
 		this.type = n;
 		this.amount = amount;
 		this.density = density;
 		this.colour = colour;
 		this.droppedColor = droppedColor;
 		
 		this.carrySize = 3;
 		exists = true;
 		
 		float size = amount / 50;
 		
 		// Set minimum size
 		if (size < 5)
 			size = 5;
 		
 		bounds.setWidth(size);
 		bounds.setHeight(size);
 		
		sizeRadius = (int) (size /2);
		
 		//Remember the center X and Y
 		centerX = bounds.getX();
 		centerY = bounds.getY();
 		
 		bounds.setCenterX(centerX);
 		bounds.setCenterY(centerY);
 		
 	}
 	
 	public Food harvest(int a)
 	{
 		
 		if(a > amount)
 			a = amount;
 
 		float shrinkageX = (bounds.getWidth() / 1000)  * a;
 		float shrinkageY = (bounds.getHeight() / 1000) * a ;
 		
 		bounds.setWidth(bounds.getWidth() - shrinkageX);
 		bounds.setHeight(bounds.getHeight() - shrinkageY);
 		
 		bounds.setCenterX(centerX);
 		bounds.setCenterY(centerY);
		
		sizeRadius = (int) (bounds.getWidth() /2);
 
 		update();
 
 		if(amount > a)
 		{
 			amount -= a;
 			return world.foodFactory.harvest(this, a);
 		}
 		else
 			return this;
 	}
 	
 	public void update()
 	{
 		if(amount <= 0)
 		{
 			delete();
 		}
 	}
 	
 	public String getType()
 	{
 		return type;
 	}
 
 
 	public int getAmount()
 	{
 		return amount;
 	}
 	
 	public int getDensity()
 	{
 		return density;
 	}
 
 	
 	public int getTotalFood()
 	{
 		return amount * density;
 	}
 	public void draw(Graphics g)
 	{
 		
 		// Draw the food!
 		if(!dropped)
 			g.setColor(colour);
 		else
 			g.setColor(droppedColor);
 		
 		if(!isBeingCarried())
 		{
 			g.fillOval(bounds.getX(), bounds.getY(), bounds.getWidth(), 
 					bounds.getHeight());
 			
 			
 			if(drawType)
 			{
 				g.setColor(Color.black);
 				g.drawString(type, bounds.getCenterX() - ((9 * type.length()) /2 ),
 						bounds.getY() + bounds.getHeight());
 			}
 			
 		}else{
 			
 			g.fillOval(bounds.getCenterX() - carrySize/2, bounds.getCenterY() - carrySize/2 , carrySize, 
 					carrySize);
 		}
 		
 		
 		// Draw entity specific settings
 		super.draw(g);
 		
 	}
 	
 	public Color getDroppedColor()
 	{
 		return droppedColor;
 	}
 	
 	public int eat(int a)
 	{
 		if(amount > a)
 		{
 			// there is more food then the ant can eat
 			float shrinkageX = (bounds.getWidth() / 100)  * a;
 			float shrinkageY = (bounds.getHeight() / 100) * a ;
 			
 			bounds.setWidth(bounds.getWidth() - shrinkageX);
 			bounds.setHeight(bounds.getHeight() - shrinkageY);
 			
 			amount -= a;
 			return a * density;
 			
 		}else
 		{
 			float shrinkageX = (bounds.getWidth() / 100)  * a;
 			float shrinkageY = (bounds.getHeight() / 100) * a ;
 			
 			bounds.setWidth(bounds.getWidth() - shrinkageX);
 			bounds.setHeight(bounds.getHeight() - shrinkageY);
 			
 			delete();
 			return amount * density;
 		}
 	}
 	
 	
 
 }
