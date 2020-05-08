 package game;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.Point;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import core.ImageLoader;
 import core.render.Renderable;
 
 /**
  * The plot class represents a plot with a type that contains different
  * production values
 * @author grant
 * @author trevor
  */
 public class Plot implements Renderable, Serializable
 {
 	private static final long serialVersionUID = 9074483871643315710L;
 
 	public static final int SIZE = 70;
 
 	private boolean isOwned;
 
 	private Color color;
 	private Point location;
 	private PlotType plotType;
 	private ImprovementType improvementType;
 
 	public Plot (PlotType type, int x, int y)
 	{
 		this.plotType = type;
 		improvementType = ImprovementType.EMPTY;
 		
 		location = new Point(y, x);
 	}
 	
     /**
      * The get PlotType method returns the current type
      * @return PlotType type - the plot type
      */
 	public PlotType getType()
 	{
 		return plotType;
 	}
 	
 	public int getFoodProduction()
 	{		
 		if (improvementType == ImprovementType.EMPTY)
 			return 0;
 		
 		return plotType.getFood();
 	}
 	
 	/**
      * the getEnergyProduction method returns the energy production 
      * @return int energyProduction - the possible energy production
      */
 	public int getEnergyProduction()
 	{
 		if (improvementType == ImprovementType.EMPTY)
 			return 0;
 		
 		return plotType.getEnergy();
 	}
 	
     /**
      * The getOreProduction method returns the ore production
      * @return int oreProduction - the possible ore production
      */
 	public int getOreProduction()
 	{
 		if (improvementType == ImprovementType.EMPTY)
 			return 0;
 		
 		return plotType.getOre();
 	}
 	
 	/**
      * the getBackgroundImage method gets the image of the land
      * @return Image - the background image
      */
 	public Image getBackgroundImage()
 	{
 		ImageLoader imageLoader = ImageLoader.getInstance();
 		return imageLoader.load("assets/images/plot/" + plotType.getImageName());
 	}
 	
     /**
      * the getImprovementImage method returns the corresponding image to an
      * improvement
      * 
      * @return Image - the improvement image
      */
 	public Image getImprovementImage()
 	{
 		ImageLoader imageLoader = ImageLoader.getInstance();
 		return imageLoader.load("assets/images/plot/" + improvementType.getImageName());
 	}
 	
 	public Image getBorderImage()
 	{
 		ImageLoader imageLoader = ImageLoader.getInstance();
 		
		String colorName = "red";
		
		if (color == Color.red)
 		{
 			colorName = "red";				
 		}
		else if (color == Color.blue)
 		{
 			colorName = "blue";
 		} 
		else if (color == Color.black)
 		{
 			colorName = "black";
 		}
		else if (color == Color.green) 
 		{
 			colorName = "green";
 		}
 		
 		return imageLoader.load("assets/images/plot/" + colorName + "Border.png");
 	}
 
 	public ArrayList<Image> getImages()
 	{
 		ArrayList<Image> images = new ArrayList<Image>();
 		
 		images.add(getBackgroundImage());
 		images.add(getImprovementImage());
 		
 		if (color != null)
 		{
 			images.add(getBorderImage());
 		}
 		
 		return images;
 	}
 
 	public int getX()
 	{
 		return (int)(location.getX() * SIZE);
 	}
 
 	public int getY()
 	{
 		return (int)(location.getY() * SIZE);
 	}
 	
 	public boolean isOwned()
 	{
 		return isOwned;
 	}
 	
 	public void setIsOwned(boolean bool)
 	{
 		isOwned = bool;
 	}
 	
 	public void setColor(Color color)
 	{
 		this.color = color;
 	}
 
 	@Override
 	public void setX(int x) {
 		location.x = x;
 		
 	}
 
 	@Override
 	public void setY(int y) {
 		location.y = y;
 		
 	}
 }
