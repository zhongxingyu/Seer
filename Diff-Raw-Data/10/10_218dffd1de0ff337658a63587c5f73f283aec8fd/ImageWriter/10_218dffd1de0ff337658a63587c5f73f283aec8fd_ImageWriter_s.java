 package controller.writer;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 
 import util.Output;
 
 public class ImageWriter
 {
 	public static void printImage(BufferedImage image, String path, String name, String extension)
 	{
 		try
 		{
			File outputfile = new File(path + name + extension);
 			ImageIO.write(image, "png", outputfile);
 		}
 		catch(Exception e)
 		{
 			Output.showException(e);
 		}
 	}
 }
