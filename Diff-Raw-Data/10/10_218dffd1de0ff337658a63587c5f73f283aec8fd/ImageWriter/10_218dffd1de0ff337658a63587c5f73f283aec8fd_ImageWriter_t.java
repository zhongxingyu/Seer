 package controller.writer;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
 
 import javax.imageio.ImageIO;
 
 import util.Output;
 
 public class ImageWriter
 {
 	public static void printImage(BufferedImage image, String path, String name, String extension)
 	{
 		try
 		{
			String username = System.getProperty("user.name");
			
			DateFormat dateFormat = new SimpleDateFormat("HH_mm_ss");
			String time = dateFormat.format(new Date());
			File outputfile = new File(path + name + " - " + username + " - " + time + extension);
 			ImageIO.write(image, "png", outputfile);
 		}
 		catch(Exception e)
 		{
 			Output.showException(e);
 		}
 	}
 }
