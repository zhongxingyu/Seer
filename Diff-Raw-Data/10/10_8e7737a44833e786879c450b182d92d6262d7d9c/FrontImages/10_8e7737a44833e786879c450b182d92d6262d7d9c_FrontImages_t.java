 package fr.umlv.escape.front;
 
 import java.util.HashMap;
 
 import fr.umlv.escape.R;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 /**
  * This class contains all the images to draw
  */
 public class FrontImages {
 	private Resources resources;
 	private final HashMap<String,Bitmap> imagesMap = new HashMap<String, Bitmap>();
 	
 	public FrontImages(Resources resources)
 	{
 		this.resources = resources;
 	}
 	
 	/**
 	 * Add an {@link Image} to the frontImage
 	 * @param key the key to get the image
 	 * @param value The image to add
 	 * @return true if the image have been added else false
 	 */
 	/*public boolean addImages(String key){
 		if(key==null){
 			throw new IllegalArgumentException();
 		}
 		if(!imagesMap.containsKey(key)){
 			imagesMap.put(key, BitmapFactory.decodeResource(this.resources, this.resources.getIdentifier(key, "drawable", null)));
 			return true;
 		}
 		return false;
 	}*/
 
 	/**
 	 * Get the {@link Image} associated to the key
 	 * @param key the key of the image to get
 	 * @return the image associated to the key given
 	 */
 	public Bitmap getImage(String key){
 		if(!imagesMap.containsKey(key)){		
 			Bitmap image = BitmapFactory.decodeResource(this.resources, this.resources.getIdentifier(key, "drawable", "fr.umlv.escape"));
 			imagesMap.put(key, image);
 			return image;
 		}
 
 		return imagesMap.get(key);
 	}
 	
 	/**
 	 * Delete all images stored
 	 */
 	public void clearMap(){
 		imagesMap.clear();
 	}
 }
