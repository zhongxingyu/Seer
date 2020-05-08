 package main;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Art {
 
 
 	public static Image getImageFromFile(String file)
 	{
 		try{
 			//File im = new File(file);
 			if(file.contains("./images/"))
 			{
 				file = file.replace("./images/","");
 			}
 			if(file.contains("images/"))
 			{
 				file = file.replace("images/","");
 			}
 			
 			
 			
 			Image title = ImageIO.read(Art.class.getResourceAsStream(("/"+file)));//(im);
 			return title;
 		}catch(Exception e){e.printStackTrace();}
 		return null;
 	}
 
 	public static Image getImage(String filename)
 	{
 
 		try {
 			return ImageIO.read(Art.class.getResourceAsStream((""+filename)));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println(filename);
 		return null;
 	}
 
 }
