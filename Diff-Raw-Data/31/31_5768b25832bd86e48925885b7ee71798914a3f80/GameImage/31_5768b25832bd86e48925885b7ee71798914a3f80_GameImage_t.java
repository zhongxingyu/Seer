 
 /**
  * Routines for retrieving graphics images from a jar or from the filesystem directory.
  */
 
 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.awt.Image;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 import org.apache.log4j.Logger;
 
 
 public class GameImage {
 	
 	private static ClassLoader cl = GameImage.class.getClassLoader();
 	protected static final String AA_RESOURCE_FILE_PATH	= "edu/bu/cs673/AwesomeAlphabet/resources/";
 	static Logger log = Logger.getLogger(GameImage.class);
 	
 	/**
 	 * Constructor. This prepends the resource directory to the image's filename.
 	 * @param filename the name of the file containing the graphics
 	 * @return an Image containing the graphic read from the jar or the file system.
 	 */
 	public static Image getImage(String filename) 
 	{
 
 		InputStream is = cl.getResourceAsStream(AA_RESOURCE_FILE_PATH + filename);
 		
 		if(is == null){
 			System.out.println("Input stream is null");
 		}
 		
 		try {
 			return (ImageIO.read(is));
 		} catch (Exception e) {
 			log.error("An exception occurred while reading the file: "+filename);
 			log.error(e.getMessage());
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
