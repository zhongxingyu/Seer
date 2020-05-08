 /**
  * Okeke Emmanuel <emmanix2002@gmail.com>
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the 
  * Software is furnished to do so, subject to the following 
  * conditions:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
  * OTHER DEALINGS IN THE SOFTWARE.
  * 
  * This License shall be included in all copies or substantial 
  * portions of the Software.
  * 
  * The name(s) of the above copyright holders shall not be used 
  * in advertising or otherwise to promote the sale, use or other 
  * dealings in this Software without prior written authorization.
  * 
  * Sample Usage
  * **************************
  * Bitmap bmp1 = ImageLoader.getBitmap("splashscreen.png");
  * String bmp1_path = ImageLoader.getBitmapPath("splashscreen.png");
  * 		
  * 		could return something like:
  * 
  * 			img/640x480/splashscreen.png (for a 640x480 resolution device if it's found in the folder) 
  * 	or
  * 			img/default/splashscreen.png (if it's not found in the device specific folder)
  * 
  *  You could have a folder structure like so:
  *  	res/
  *  		img/
  *  			default/
  *  			320x240/
  *  			360x480/
  *  			480x320/
  *  			480x360/
  *  			480x640/
  *  			640x480/
  */
 package com.fcolimited.blackberry.utils.images;
 
 import java.io.InputStream;
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.Display;
 
 public class ImageLoader {
 	/**
 	 * The base path in your resources folder where images are stores
 	 * Defaults to: img
 	 */
 	private static String images_path;
 	/**
 	 * The default folder to look for images if they're not found in the device specific folder
 	 * Defaults to: default
 	 */
 	private static String images_subfolder_default;
 	
 	static {
 		ImageLoader.setBaseImagePath("img");
 		ImageLoader.setDefaultImageSubFolder("default");
 	}
 	
 	public ImageLoader(){
 		
 	}
 	/**
 	 * Returns the device's display width
 	 * @return int
 	 */
 	public static int getDisplayWidth(){
 		return Display.getWidth();
 	}
 	/**
 	 * Returns the device's display height
 	 * @return int
 	 */
 	public static int getDisplayHeight(){
 		return Display.getHeight();
 	}
 	/**
 	 * Returns the specific sub-folder under the main images folder where the device specific images whould be
 	 * @return String
 	 */
 	public static String getSpecificImageSubFolder(){
 		System.out.println("Specific Path: "+ImageLoader.getDisplayWidth()+"x"+ImageLoader.getDisplayHeight());
 		return ImageLoader.getDisplayWidth()+"x"+ImageLoader.getDisplayHeight();
 	}
 	/**
 	 * Sets a new value for the base images path
 	 * @param String new_path
 	 * @return
 	 */
 	public static String setBaseImagePath(String new_path){
		new_path = (new_path == null)? "":new_path+"/";
 		ImageLoader.images_path = new_path;
 		return ImageLoader.images_path;
 	}
 	/**
 	 * Sets a new value for the default sub-folder name in the event that the image is not found in a device 
 	 * specific folder
 	 * @param String folder_name
 	 * @return String
 	 */
 	public static String setDefaultImageSubFolder(String folder_name){
		folder_name = (folder_name == null)? "":folder_name+"/";
 		ImageLoader.images_subfolder_default = folder_name;
 		return ImageLoader.images_subfolder_default;
 	}
 	/**
 	 * Returns the best possible path for the specified image file
 	 * It searches to see if it can get a device specific version of the image else it falls back to the default image
 	 * @param String filename
 	 * @return String
 	 */
 	public static String getBitmapPath(String filename){
 		String bitmap_path = null;
 		try{
 			Class classx = Class.forName("com.fcolimited.blackberry.utils.images.ImageLoader");
 			if(classx == null){
 				throw new Exception("Could not get the class reference...");
 			}
			String img_specific = ImageLoader.images_path+ImageLoader.getSpecificImageSubFolder()+"/"+filename;
 			InputStream input_stream = classx.getResourceAsStream("/"+img_specific);
 			if(input_stream != null){
 				System.out.println("Using image path: "+img_specific);
 				bitmap_path = img_specific;
 			} else {
				String img_default = ImageLoader.images_path+ImageLoader.images_subfolder_default+filename;
 				input_stream = classx.getResourceAsStream("/"+img_default);
 				if(input_stream != null){
 					System.out.println("Using image path: "+img_default);
 					bitmap_path = img_default;
 				}
 			}			
 		} catch(Exception e){
 			System.out.println("Exception (loadBitmap): "+e.getMessage());
 		}
 		return bitmap_path;
 	}
 	/**
 	 * Loads a bitmap resource from the application.
 	 * It searches to see if it can get a device specific version of the image else it falls back to the default image
 	 * @param String filename
 	 * @return Bitmap
 	 */
 	public static Bitmap getBitmap(String filename){
 		Bitmap bitmap = null;
 		String bitmap_path = ImageLoader.getBitmapPath(filename);
 		if(bitmap_path != null){
 			bitmap = Bitmap.getBitmapResource(bitmap_path);
 		}
 		return bitmap;
 	}
 }
