 package DD.SlickTools;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 /*****************************************************************************************************
  * DDImage will contain all the necessary information for a DD object to create a slick image;
  * 
  * @author Carlos Vasquez / Michael VanWie
  ******************************************************************************************************/
 
 public class DDImage implements Serializable
 {
 	/************************************ Class Constants *************************************/
 	private static final long serialVersionUID = 5614722491837796142L;
 	/************************************ Class Attributes *************************************/
 	private String spriteSheetPath = null;
	private boolean subImage; 				/* tells us if the DDImage should be a subImage */
 	private int x;
 	private int y;
 	private int width;
 	private int height;
 	private transient Image image = null;
 	
 	/************************************ Class Methods *************************************/
 	public DDImage(){}
 	
 	public DDImage(String spriteSheetPath, int x, int y, int width, int height)
 	{
 		this.spriteSheetPath = spriteSheetPath;
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
		this.subImage = true;
 	} /* end DDImage constructor */
 	
 	public DDImage(int x, int y)
 	{
 		this("Images/Test/DungeonCrawl_ProjectUtumnoTileset.png",x,y, 33, 34);
		this.subImage = true;
 	} /* end DDImage constructor */
 	
 	public DDImage(Image image)
 	{
 		this.image = image;
		subImage = false;
 	} /* end DDImage constructor */
 	
 	public DDImage(String path)
 	{
 		spriteSheetPath = path;
		subImage = false;
 		
 	} /* end DDImage constructor */
 	
 	
 	public DDImage getSubImage(int x, int y, int height, int width)
 	{
 		DDImage temp = new DDImage(spriteSheetPath, x, y, height, width);
 		return temp;
 	}
 	
 	public Image makeImage()
 	{
 		Image returner = null;
 		Image spriteSheet = null;
 		try 
 		{
 			if(spriteSheetPath == null)
 			{
 				spriteSheetPath ="Images/Test/DungeonCrawl_ProjectUtumnoTileset.png";
 				spriteSheet = new Image(spriteSheetPath);
 				returner = spriteSheet.getSubImage(x, y, width, height);
 			} /* end if */
 			else
 			{
 				spriteSheet = new Image(spriteSheetPath);
				if (this.subImage == true) returner = spriteSheet.getSubImage(x, y, width, height);
 				else returner = spriteSheet;
 			} /* end else */
 			
 		} /* end try */
 		catch (SlickException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} /* end catch */
 		
 		return returner;
 	} /* end makeImage method */
 	
 	public void draw(float x, float y, float scale)
 	{
 		if(getImage() != null) getImage().draw(x, y, scale);
 	} /* end draw method */
 	
 	public void draw(float x, float y)
 	{
 		if(getImage() != null) getImage().draw(x, y);
 	} /* end draw method */
 	
 	public void writeMe(){
 		try{
 			FileOutputStream fileOut = new FileOutputStream("C:/Program Files (x86)/DD/"+"test.ser");
 			ObjectOutputStream out =  new ObjectOutputStream(fileOut);
 			out.writeObject(this);
 			out.close();
 			fileOut.close();
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	/****************************************************************************************
 	 ************************************ Getter Methods ************************************
 	 ****************************************************************************************/
 	public int getWidth(){
 		return getImage().getWidth();
 	}
 	public int getHeight(){
 		return getImage().getHeight();
 	}	
 	
 	public DDImage getScaledCopy(float scale){
 		return new DDImage(getImage().getScaledCopy(scale));
 	}
 	
 	public Image getImage()
 	{
 		Image returner = null;
 		if(image == null) image = makeImage();
 		returner = image;
 		return returner;
 	} /* end getImage method */
 	
 	public int getX()
 	{
 		return x;
 	} /* end getX method */
 	
 	public int getY()
 	{
 		return y;
 	} /* end getY method */
 	
 	public boolean getSubImage()
 	{
		return subImage;
 		
 	} /* end getSubImage method */
 	
 	/****************************************************************************************
 	 ************************************ Setter Methods ************************************
 	 ****************************************************************************************/
 	public void setImage(Image image)
 	{
 		this.image = image;
 	} /* end setImage */
 	
 } /* end DDImage class */
