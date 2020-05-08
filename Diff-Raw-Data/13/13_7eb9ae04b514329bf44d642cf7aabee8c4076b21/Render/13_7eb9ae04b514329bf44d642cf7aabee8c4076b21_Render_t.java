 package ui.render;
 
 import java.awt.Image;
import java.io.Serializable;
 import java.util.ArrayList;
 
 import core.ImageLoader;
 
public class Render implements Serializable
 {
	private static final long serialVersionUID = 6788552009617942691L;

 	public int x;
 	public int y;
 	public int width;
 	public int height;
 	
 	private ArrayList<String> imagePaths;
 
 	public Render()
 	{
 		imagePaths = new ArrayList<String>();
 	}
 	
 	public void addImage(String imagePath)
 	{		
 		imagePaths.add(imagePath);
 	}
 	
 	public void clearImages()
 	{
 		imagePaths = new ArrayList<String>();
 	}
 
 	public ArrayList<Image> getImages() 
 	{
 		ImageLoader imageLoader = ImageLoader.getInstance();
 		ArrayList<Image> images = new ArrayList<Image>();
 
 		for (String path : imagePaths)
 		{		
 			Image image = imageLoader.load(path);
 			images.add(image);
 		}
 		
 		return images;
 	}
 }
