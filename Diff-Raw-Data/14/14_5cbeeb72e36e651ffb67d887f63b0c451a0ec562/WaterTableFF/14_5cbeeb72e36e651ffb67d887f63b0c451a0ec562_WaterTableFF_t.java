 package flatFileData;
 
 import java.awt.Color;
 import java.awt.List;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import windows.MapWindow;
 
 
 public class WaterTableFF implements dataInterfaces.WaterTable{
 	
 	public static final int MAX_Y_PIXEL = 178; //max pixel in the Y direction
 	private final int id;
 	private final int earthModelId;
 	private final int startDepth;
 	private final int endDepth;
 	private final int xPos;
 	private final int yPos;
 	private int xLowest;
 	private int yLowest;
 	private int bottomWaterDepth;
 	private int waterElevation;
 	private String siteName;
 	private BufferedImage waterTableImage;
 	private int waterDepthIncrement;
 	
 	public WaterTableFF(int earthModelId, int startDepth, int endDepth, int xPos, int yPos){
 		this.id = -9999;
 		this.earthModelId = earthModelId;
 		this.startDepth = startDepth;
 		this.endDepth = endDepth;
 		this.xPos = xPos;
 		this.yPos = yPos;
 		waterElevation = -1;
 		waterDepthIncrement = 1;
 	}
 	
 	public WaterTableFF(int id, int earthModelId, int startDepth, int endDepth, int xPos, int yPos){
 		this.id = id;
 		this.earthModelId = earthModelId;
 		this.startDepth = startDepth;
 		this.endDepth = endDepth;
 		this.xPos = xPos;
 		this.yPos = yPos;
 		waterElevation = -1;
 		waterDepthIncrement = 1;
 	}
 	/*
 	public WaterTableFF(String siteName)
 	{
 		id = -1;
 		earthModelId = -1;
 		startDepth = -1;
 		endDepth = -1;
 		xPos = -1;
 		yPos = -1;
 		this.siteName = siteName;
 		waterElevation = -1;
 		waterDepthIncrement = 1;
 		//readWaterTableImage();
 	}
 	*/
 	public WaterTableFF(String siteName, int startDepth, int endDepth, int xPos, int yPos, SiteFF theSite)
 	{
 		id = -1;
 		earthModelId = -1;
 		this.startDepth = -1;
 		this.endDepth = -1;
 		this.xPos = xPos;
 		this.yPos = yPos;
 		this.siteName = siteName;
 		waterElevation = -1;
 		waterDepthIncrement = 1;
 		readWaterTableImage(theSite.getSiteFileExtension());
 		int temp = getWaterDepth();
 	}
 	
 	public WaterTableFF() {
 		id = -1;
 		earthModelId = -1;
 		startDepth = -1;
 		endDepth = -1;
 		xPos = -1;
 		yPos = -1;
 		waterElevation = -1;
 		waterDepthIncrement = 1;
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 	
 	public void readWaterTableImage(String siteFileExtension)
 	{
 		String temp = null;
		System.out.println("SITEFILEEXTENSION: " + siteFileExtension);
 		if(siteFileExtension.contains("Blytheville"))
 		{
 			xLowest = 30;
 			yLowest = 0;
 			//blytheville 1 and 2 are image NGW (north ground water)
 			//blytheville 3 and 4 are image EGW (east ground water)
 			//blytheville 5 and 6 are image SEGW (south-east ground water)
 			if(siteName.contains("1") || siteName.contains("2"))
 			{
 				temp = "ngw.gif";
 				bottomWaterDepth = 243;
 			}
 			if(siteName.contains("3") || siteName.contains("4"))
 			{
 				temp = "egw.gif";
 				bottomWaterDepth = 245;
 			}
 			if(siteName.contains("5") || siteName.contains("6"))
 			{
 				temp = "segw.gif";
 				bottomWaterDepth = 242;
 			}
 		}
 		else if (siteFileExtension.contains("Fort"))
 		{
 			xLowest = 0;
 			yLowest = 0;
 			//blytheville 1 and 2 are image NGW (north ground water)
 			//blytheville 3 and 4 are image EGW (east ground water)
 			//blytheville 5 and 6 are image SEGW (south-east ground water)
 			if(siteName.contains("1"))
 			{
 				temp = "gw-180.gif";
 				bottomWaterDepth = 6;
 			}
 			if(siteName.contains("2"))
 			{
 				temp = "gw-ua.gif";
 				bottomWaterDepth = 100;
 			}else{
 				//XXX setting a default right now so the program doesn't crash
 				temp = "gw-400.gif";
 				bottomWaterDepth = -17;
 			}
 		}
		else if (siteFileExtension.contains("Airport") || siteFileExtension.contains("Slaps"))
 		{

 			xLowest = 30;
 			yLowest = 0;
			//WHAT IS BLYTHEVILLE DOING HERE?
 			//blytheville 1 and 2 are image NGW (north ground water)
 			//blytheville 3 and 4 are image EGW (east ground water)
 			//blytheville 5 and 6 are image SEGW (south-east ground water)
 			/*
 			if(siteName.contains("1") || siteName.contains("2"))
 			{
 				temp = "ngw.gif";
 				bottomWaterDepth = 243;
 			}
 			if(siteName.contains("3") || siteName.contains("4"))
 			{
 				temp = "egw.gif";
 				bottomWaterDepth = 245;
 			}
 			if(siteName.contains("5") || siteName.contains("6"))
 			{
 				temp = "segw.gif";
 				bottomWaterDepth = 242;
 			}
 			*/
 			temp = "SLAPSLOWERGW.gif";
 			//XXX Just a default
 			bottomWaterDepth = 500;
 		}
 		else if (siteFileExtension.contains("Springfield"))
 		{
 			System.out.println("springfield watertableff called");
 			xLowest = 30;
 			yLowest = 0;
 			//blytheville 1 and 2 are image NGW (north ground water)
 			//blytheville 3 and 4 are image EGW (east ground water)
 			//blytheville 5 and 6 are image SEGW (south-east ground water)
 			/*
 			if(siteName.contains("1") || siteName.contains("2"))
 			{
 				temp = "ngw.gif";
 				bottomWaterDepth = 243;
 			}
 			if(siteName.contains("3") || siteName.contains("4"))
 			{
 				temp = "egw.gif";
 				bottomWaterDepth = 245;
 			}
 			if(siteName.contains("5") || siteName.contains("6"))
 			{
 				temp = "segw.gif";
 				bottomWaterDepth = 242;
 			}
 			*/
 			temp = "gwater.gif";
 			//XXX Just a default
 			bottomWaterDepth = 1165;
 			//bottomWaterDepth = 
 		}
 		else if (siteFileExtension.contains("Weldon"))
 		{
 			xLowest = 30;
 			yLowest = 0;
 			//blytheville 1 and 2 are image NGW (north ground water)
 			//blytheville 3 and 4 are image EGW (east ground water)
 			//blytheville 5 and 6 are image SEGW (south-east ground water)
 			/*
 			if(siteName.contains("1") || siteName.contains("2"))
 			{
 				temp = "ngw.gif";
 				bottomWaterDepth = 243;
 			}
 			if(siteName.contains("3") || siteName.contains("4"))
 			{
 				temp = "egw.gif";
 				bottomWaterDepth = 245;
 			}
 			if(siteName.contains("5") || siteName.contains("6"))
 			{
 				temp = "segw.gif";
 				bottomWaterDepth = 242;
 			}
 			*/
 			temp = "gwweldon.gif";
 			// XXX Just a default
 			bottomWaterDepth = 616;
 		}
 		
 		InputStream is = null;
		System.out.println("waterTableFF243 file: " + siteFileExtension + "/" + temp);
 		try {
 			is = new BufferedInputStream(
					
 					new FileInputStream(siteFileExtension + "/" + temp));
 		} catch (FileNotFoundException e) {
 			System.out.println("(WaterTableFF) could not find water table image");
 			e.printStackTrace();
 		}
 		try {
 			waterTableImage = ImageIO.read(is);
 		} catch (IOException e) {
 			System.out.println("(WaterTableFF) could not read water table image");
 			e.printStackTrace();
 		}
 	}
 	
 	public int getWaterDepth()
 	{
 		int xPixel = (int)(xPos*SiteFF.SCALE_TO_PIXEL_X_MULTIPLIER);
 		int yPixel = (int)((MapWindow.MAX_COORDINATE-yPos)*SiteFF.SCALE_TO_PIXEL_Y_MULTIPLIER);
 		//Color currentColor = new Color(waterTableImage.getRGB(xPixel,yPixel));
 		int depth = -1;
 		/*
 		if(SiteFF.siteFileExtension.contains("Blytheville"))
 		{
 			depth = CalculateDepth(xPixel, yPixel, 30, 0);
 		}
 		*/
 		depth = CalculateDepth(xPixel, yPixel, xLowest, yLowest);
 		return depth;
 	}
 	
 	/**
 	 * This function deals with pixel parameters
 	 * inputs the starting coordinates and increments
 	 * the depth counting to the goal coordinates
 	 * @param xInit
 	 * @param yInit
 	 * @return depth
 	 */
 	public int CalculateDepth(int xInit, int yInit, int xLow, int yLow)
 	{
 		//each watertable has a bottomDepth in the def file
 		//this is the lowest elevation of the aquifer
 		waterElevation = bottomWaterDepth;
 		int xGoal = xLow;
 		int yGoal = yLow;
 		int currentColor;
 		yInit = MAX_Y_PIXEL - yInit;
 		int xMove;
 		int yMove;
 		int x = xInit;
 		int y = yInit;
 		
 		//Sets the direction to traverse the image
 		if(xInit < xGoal)
 			xMove = 1;
 		else
 			xMove = -1;
 		if(yInit < yGoal)
 			yMove = 1;
 		else
 			yMove = -1;
 		
 		ArrayList<Integer> colorList = new ArrayList<Integer>();
 		
 		//adds starting color since it doesn't need to be counted on the addition
 		colorList.add(waterTableImage.getRGB(xInit, yInit));
 		int lastColor = waterTableImage.getRGB(xInit, yInit);
 		
 		//iterates through x coordinates
 		for(x = xInit; x != xGoal; x+= xMove)
 		{
 			//System.out.println("checking color at (" + x + "," + y + ")");
 			currentColor = waterTableImage.getRGB(x,y);
 			if(!(colorList.contains(currentColor))) //color is not yet in the list
 			{
 				//System.out.println("newColor at (" + x + "," + y + ")");
 				colorList.add(currentColor);
 				waterElevation += waterDepthIncrement; //go up
 				lastColor=currentColor;
 				//System.out.println("UP: checking color at (" + x + "," + y + ")");
 			}else if((colorList.contains(currentColor)) && //if list already contains the color
 					(colorList.indexOf(currentColor)<colorList.indexOf(lastColor))) //and the color has already been crossed up
 			{
 				waterElevation -= waterDepthIncrement; //go down
 				lastColor=currentColor;
 				//System.out.println("Down: checking color at (" + x + "," + y + ")");
 			}else if((colorList.contains(currentColor)) && //if list already contains the color
 					(colorList.indexOf(currentColor)>colorList.indexOf(lastColor))) //and the color has already been crossed down
 			{
 				waterElevation += waterDepthIncrement; //go up
 				lastColor=currentColor;
 				//System.out.println("UP: checking color at (" + x + "," + y + ")");
 			}
 			
 		}
 		//clear list and reset
 		//colorList.clear();
 		//colorList.add(waterTableImage.getRGB(xInit, yInit));
 		//lastColor = waterTableImage.getRGB(xInit, yInit);
 		//x = xInit;
 		//iterates through y coordinates
 		for(y = yInit; y != yGoal; y+= yMove)
 		{
 			//System.out.println("checking color at (" + x + "," + y + ")");
 			currentColor = waterTableImage.getRGB(x,y);
 			if(!(colorList.contains(currentColor))) //color is not yet in the list
 			{
 				//System.out.println("newColor at (" + x + "," + y + ")");
 				colorList.add(currentColor);
 				waterElevation += waterDepthIncrement; //go up
 				lastColor=currentColor;
 				//System.out.println("UP: checking color at (" + x + "," + y + ")");
 			}else if((colorList.contains(currentColor)) && //if list already contains the color
 					(colorList.indexOf(currentColor)<colorList.indexOf(lastColor))) //and the color has already been crossed up
 			{
 				waterElevation -= waterDepthIncrement; //go down
 				lastColor=currentColor;
 				//System.out.println("Down: checking color at (" + x + "," + y + ")");
 			}else if((colorList.contains(currentColor)) && //if list already contains the color
 					(colorList.indexOf(currentColor)>colorList.indexOf(lastColor))) //and the color has already been crossed down
 			{
 				waterElevation += waterDepthIncrement; //go up
 				lastColor=currentColor;
 				//System.out.println("UP: checking color at (" + x + "," + y + ")");
 			}
 			
 		}
 		//Blytheville
 		//brightGreen waterElevation = 248.5
 		//blue waterElevation = 247.5
 		//lightBlue waterElevation = 246.5
 		//red 245.4
 		//yellow 244.5
 		//darkGreen 243.5
 		//Purple 243.0
 		return -1;
 	}
 
 	public int getWaterElevation()
 	{
 		return waterElevation;
 	}
 	public int getEarthModelId() {
 		return earthModelId;
 	}
 
 	@Override
 	public int getStartDepth() {
 		return startDepth;
 	}
 
 	@Override
 	public int getEndDepth() {
 		return endDepth;
 	}
 
 	@Override
 	public int getxPos() {
 		return xPos;
 	}
 
 	@Override
 	public int getyPos() {
 		return yPos;
 	}
 
 	@Override
 	public int getSiteId() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	
 
 }
