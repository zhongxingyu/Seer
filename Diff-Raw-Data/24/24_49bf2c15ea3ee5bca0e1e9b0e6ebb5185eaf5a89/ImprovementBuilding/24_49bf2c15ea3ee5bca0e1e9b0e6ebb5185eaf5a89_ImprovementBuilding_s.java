 package buildings;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class ImprovementBuilding extends Building {
 	public enum BuildingType {FARM, BARRACK, MINE, SAWMILL, TRADINGPOST, NONE}
 	private BuildingType buildingType;
 	public ImprovementBuilding(int locx, int locy, BuildingType buildingType, int wood, int stone, int gold) {
 		super(locx, locy, wood, stone, gold);
 		this.buildingType = buildingType;
 		
<<<<<<< HEAD
		switch(buildingType)
		{
			case FARM:
			case BARRACK:
			case MINE:
			case SAWMILL:
			case TRADINGPOST:
		}
=======
 		try 
 		 {
 			switch(buildingType)
 			{
 				case FARM: 			image = ImageIO.read(new File("Textures/Building/Farm")); break;
 				case BARRACK:		image = ImageIO.read(new File("Textures/Building/Barrack")); break;
 				case MINE:			image = ImageIO.read(new File("Textures/Building/Mine")); break;
				case SAWMIL:		image = ImageIO.read(new File("Textures/Building/SawMill")); break;
 				case TRADINGPOST:	image = ImageIO.read(new File("Textures/Building/TradingPost")); break;
 			}
 	     } catch (IOException ex) {
 	            // handle exception...
 	     }
>>>>>>> cdfcd3e4a58df7647ea962a1948c778376b1f26f
 	}
 	
 	public boolean hasResource()
 	{
 		return (buildingType == BuildingType.NONE) ? true: false;
 	}
 	public BuildingType getBuildingType()
 	{
 		return buildingType;
 	}
 }
