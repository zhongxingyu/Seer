 package org.mdissjava.mdisscore.metadata.impl;
 
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.mdissjava.mdisscore.metadata.MetadataExtractor;
 import org.mdissjava.mdisscore.metadata.SimpleImageInfo;
 import org.mdissjava.mdisscore.model.pojo.Camera;
 import org.mdissjava.mdisscore.model.pojo.Metadata;
 import org.mdissjava.mdisscore.model.pojo.Resolution;
 
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.MetadataException;
 import com.drew.metadata.exif.ExifIFD0Directory;
 import com.drew.metadata.exif.ExifSubIFDDirectory;
 
 public class MetadataExtractorImpl implements MetadataExtractor {
 	
 	private byte[] photo = null;
 	private SimpleImageInfo simpleImage = null;
 	
 	@Override
 	public Metadata obtenerMetadata(byte[] photo) throws MetadataException, ImageProcessingException, IOException {
 		
 		this.photo = photo;
 		this.simpleImage = new SimpleImageInfo(photo);
         String format = getOnlyExtension(simpleImage.getMimeType());
         
 		if((format.equals("jpeg"))||(format.equals("tiff")))
 		{
 			return getEXIFMetadata(format);
 			
 		}
 		else if((format.equals("png"))||(format.equals("gif")))
 		{
 			return getBasicMetadata(format);
 			
 		}
 		else
 		{
 			System.out.println("EXIF metadata not supported for the format: "+format);
 			return getBasicMetadata(format);
 		}
 		
 			
 	}
 	
 	@Override
 	public Metadata getEXIFMetadata(String format) {
 
 		Metadata metadata = new Metadata();
 		Resolution resolutionPPI = new Resolution();
 					
 		ByteArrayInputStream photoInputStream = new ByteArrayInputStream(photo);
 		BufferedInputStream bufferedPhoto = new BufferedInputStream(photoInputStream);
 		
 		try{
 		
 			
 			com.drew.metadata.Metadata metadataFoto = ImageMetadataReader.readMetadata(bufferedPhoto, true);
 			
 			Resolution resolutionREAL = new Resolution();
 			resolutionREAL.setHeight(this.simpleImage.getHeight());
 			resolutionREAL.setWidth(this.simpleImage.getWidth());	
 
 			ExifIFD0Directory exifIFD0Directory = metadataFoto.getDirectory(ExifIFD0Directory.class);
 			ExifSubIFDDirectory exifSubIFDirectory = metadataFoto.getDirectory(ExifSubIFDDirectory.class);
 					
 			metadata.setShutterSpeed(exifSubIFDirectory.getString(ExifSubIFDDirectory.TAG_SHUTTER_SPEED));
 			metadata.setAperture(exifSubIFDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE));
 			metadata.setFocalLength(exifSubIFDirectory.getInt(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
 			metadata.setISOSpeed(exifSubIFDirectory.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
 			resolutionPPI.setHeight(exifIFD0Directory.getInt(ExifIFD0Directory.TAG_Y_RESOLUTION));
 			resolutionPPI.setWidth(exifIFD0Directory.getInt(ExifIFD0Directory.TAG_X_RESOLUTION));
 			metadata.setResolutionPPI(resolutionPPI);
 			metadata.setResolutionREAL(resolutionREAL);
 			metadata.setDateTaken(exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME));
 			//Save photo extension: jpeg, png, giff etc.
 			metadata.setFormat(format);
 			//Save photo size in kb
 			metadata.setSize((float)bytesToKb(photo.length));
 			
 			return metadata;
 		}
 		catch(NullPointerException npe)
 		{
 			//npe.printStackTrace();
 			return getBasicMetadata(format);
 		}
 		catch(ImageProcessingException ipe){
 			//ipe.printStackTrace();
 			return getBasicMetadata(format);
 		}	
 		catch(IOException io){
 			//io.getStackTrace();
 			return getBasicMetadata(format);
 		}
 	    catch(MetadataException me)
 	    {
 	    	System.out.println("No metadata available");
 	    	me.getStackTrace();
 	    	return getBasicMetadata(format);
 	    }
 		
 	}
 
 
 	//TODO How to store if photo size is in kb or mb. New String field or convert size to string.
 	
 	@Override
 	public Metadata getBasicMetadata(String format) {
 		
 		Metadata metadata = new Metadata();
 		Resolution resolution = new Resolution();
 		
 		resolution.setWidth(this.simpleImage.getWidth());
 		resolution.setHeight(this.simpleImage.getHeight());
 				
 		metadata.setResolutionREAL(resolution);
 		//Save photo extension: jpeg, png, giff etc.
 		metadata.setFormat(format);
 		//save photo size in kb
 		metadata.setSize((float)bytesToKb(photo.length));
 		
 		return metadata;
 	}
 
 	@Override
 	public String getOnlyExtension(String type) {
 		String ext = null;
 		
 		int i = type.lastIndexOf('/');
 
 		if (i > 0 && i < type.length() - 1)
 			
 			ext = type.substring(i+1).toLowerCase();
 
 		if(ext == null)
 			return "";
 		
 		return ext;
 	}
 	
 	 private static final long  MEGABYTE = 1024L * 1024L;
 	 private static final long KBYTE = 1024L;
 	 
 	 @Override
 	 public long bytesToMb(int bytes) {
 	  return bytes / MEGABYTE ;
 	 }
 	 
 	 @Override
 	 public long bytesToKb(int bytes) {
 	  return bytes / KBYTE ;
 	 }

	 @Override
	 public double getDecimal(int numberOfDecimals, double decimal){
		 
		 decimal = decimal*(java.lang.Math.pow(10, numberOfDecimals));
		 decimal = java.lang.Math.round(decimal);
		 decimal = decimal/java.lang.Math.pow(10, numberOfDecimals);

		 return decimal;  
	 } 
 	@Override
 	public Map<String, String> getMetadataFormatted(Metadata metadata) {
 		
 		Map<String, String> metadataMap = new HashMap<String, String>();
 		
 		// We need to call all, and all can threw a exception. This is a fucking shit of code but we need to treat 
 		// all variables differently
 		
 		try{
 			String aperture = metadata.getAperture();
 			if(aperture != null)
 				metadataMap.put("Aperture", aperture);
 		}catch(Exception e){
 		}try{	
 			Camera cam = metadata.getCamera();
 			if(cam != null)
 				metadataMap.put("map", cam.getBrand() +"-"+ cam.getId());
 		}catch(Exception e){
 		}try{
 			Date date = metadata.getDateTaken();
 			if(date != null)
 				metadataMap.put("Date taken", date.toString());
 		}catch(Exception e){
 		}try{
 			int focal = metadata.getFocalLength();
 			if(focal != 0)
 				metadataMap.put("Focal length", String.valueOf(focal));
 		}catch(Exception e){
 		}try{
 			String format = metadata.getFormat();
 			if(format != null)
 				metadataMap.put("Original format", format);
 		}catch(Exception e){
 		}try{
 			int isoSpeed = metadata.getISOSpeed();
 			if(isoSpeed != 0)
 				metadataMap.put("ISO speed", String.valueOf(isoSpeed));
 		}catch(Exception e){
 		}try{
 			Resolution resolutionPPI = metadata.getResolutionPPI();
 			if(resolutionPPI != null)
 				metadataMap.put("Resolution PPI", resolutionPPI.getWidth()+"x"+resolutionPPI.getHeight());
 		}catch(Exception e){
 		}try{
 			Resolution resolutionReal = metadata.getResolutionREAL();
 			if(resolutionReal != null)
 				metadataMap.put("Resolution", resolutionReal.getWidth()+"x"+resolutionReal.getHeight());
 		}catch(Exception e){
 		}try{
 			//int sensorSize = metadata.getSensorSize();
 			//if(sensorSize != 0)
 			//	metadataMap.put("Sensor size", String.valueOf(size));
 		}catch(Exception e){
 		}try{
 			String shutterSpeed = metadata.getShutterSpeed();
 			if(shutterSpeed != null)
 				metadataMap.put("Shutter speed", shutterSpeed);
 		}catch(Exception e){
 		}try{
 			Float size = metadata.getSize();
 
 			if(size != 0)
			{
				if(size < 1024)
				{	
					metadataMap.put("Original size", String.valueOf(size)+" kB");
				}
				else
				{
					metadataMap.put("Original size", String.valueOf(getDecimal(2, size/1024L))+" MB");
				}
			}
 		}catch(Exception e){
 		}
 		
 		return metadataMap;
 	}
 }
 
