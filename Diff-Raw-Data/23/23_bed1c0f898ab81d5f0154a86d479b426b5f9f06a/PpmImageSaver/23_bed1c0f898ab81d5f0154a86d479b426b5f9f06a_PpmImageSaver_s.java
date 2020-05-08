 package com.cateye.core.native_;
 
 import java.io.File;
 
 import com.cateye.core.IImageSaver;
 import com.cateye.core.Image;
 
 class PpmImageSaver implements IImageSaver
 {
 	@Override
 	public void save(String fileName, Image image)
 	{
 		fileName = new File(fileName).getAbsolutePath();
 		PreciseBitmap bitmap = (PreciseBitmap) image.getBitmap();
 		SaveImage(fileName, bitmap, 3);
 	}
 	
 	static native void SaveImage(String fileName, PreciseBitmap bitmap, double limit_a);
 	
 	static
 	{
		LibraryLoader.attach("ppm.CatEyeSaver");
 	}
 }
