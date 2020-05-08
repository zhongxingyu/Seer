 package land;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import resource.Resource;
 import resource.Resource.ResourceType;
 import buildings.Building;
 
 public class Land {
 	public enum LandType {FOREST('F'), HILL('H'), WATER('W'), DESERT('D'), MOUNTAIN('M'), PLAIN('P'), NONE('N');
 		private char type;
 		private LandType(char type) 
 		{
 			this.type = type;
 		}
 		public char getType() {return type;}
 	}
 	private BufferedImage image;
 	LandType landType;
 	private ResourceType resource;
 	private Boolean haveRiver;
 	private int locX, locY;
 	
 	public Land(int locx, int locy, LandType type)
 	{
 		initialize(locx, locy, type, false, ResourceType.NONE);
 	}
 	
 	public Land(int locx, int locy, LandType type, boolean haveRiver)
 	{
 		initialize(locx, locy, type, haveRiver, ResourceType.NONE);
 	}
 	
 	public Land(int locx, int locy, LandType landType, boolean river, ResourceType resourceType)
 	{
 		initialize(locx, locy, landType, river, resourceType);
 	}
 	
 	private void initialize(int locx, int locy, LandType type, boolean river, ResourceType re)
 	{
 		this.locX = locx;
 		this.locY = locy;
 		this.landType = type;
 		this.haveRiver = river;
 		this.resource = re;
 		
 		
 		 try 
 		 {
 			 switch(landType)
 				{
 					case WATER: 	image = ImageIO.read(new File("Textures/Land/Water")); break;
 					case DESERT:	image = ImageIO.read(new File("Textures/Land/Desert")); break;
 					case HILL:		image = ImageIO.read(new File("Textures/Land/HILL")); break;
 					case FOREST:	image = ImageIO.read(new File("Textures/Land/Forest")); break;
 					case PLAIN:		image = ImageIO.read(new File("Textures/Land/PLain")); break;
 					case MOUNTAIN:	image = ImageIO.read(new File("Textures/Land/Water")); break;
 				}// end switch
 	     } catch (IOException ex) {
 	            // handle exception...
 	     }
 	}
 	
 	public LandType getLandType() {
 		return landType;
 	}
 
 	public boolean hasRiver() {
 		return haveRiver;
 	}
 
 	public boolean hasResource()
 	{
 		if (resource != null) return true;
 		return false;
 	}
 	
 	public ResourceType getResourceType()
 	{
 		return resource;
 	}
 
 
 	public BufferedImage getImage() {
 		return image;
 	}
 
 	public boolean canContainRiver()
 	{
 		switch(landType)
 		{
 		case MOUNTAIN:
 		case WATER:
 			return false;
 		default:
 			return true;
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		String rtn = "";
 		rtn += landType.getType();
 		if(haveRiver)
 			rtn += "r";
 		if(resource != null)
 			rtn += resource.getType();
 		return rtn;	
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Land other = (Land) obj;
 		if (locX != other.locX)
 			return false;
 		if (locY != other.locY)
 			return false;
 		return true;
 	}
 }
