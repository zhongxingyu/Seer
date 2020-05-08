 package org.mdissjava.mdisscore.metadata.impl;
 
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 
 import org.mdissjava.mdisscore.metadata.MetadataExtractor;
 import org.mdissjava.mdisscore.metadata.SimpleImageInfo;
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
 			
 			System.out.println("X "+resolutionREAL.getWidth()+" Y "+resolutionREAL.getHeight());
 
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
 			metadata.setDateTaken(exifIFD0Directory.getDate(exifIFD0Directory.TAG_DATETIME));
 			metadata.setFormat(format);
 			metadata.setSize((float)bytesToMb(photo.length));
 			
 			return metadata;
 		}
 		catch(NullPointerException npe)
 		{
 			//npe.printStackTrace();
 			return null;
 		}
 		catch(ImageProcessingException ipe){
 			//ipe.printStackTrace();
 			return null;
 		}	
 		catch(IOException io){
 			//io.getStackTrace();
 			return null;
 		}
 	    catch(MetadataException me)
 	    {
 	    	System.out.println("No metadata available");
 	    	me.getStackTrace();
 	    	return null;
 	    }
 		
 	}
 
 
 	@Override
 	public Metadata getBasicMetadata(String format) throws MetadataException,
 			ImageProcessingException, IOException {
 		
 		Metadata metadata = new Metadata();
 		Resolution resolution = new Resolution();
 		
 		resolution.setWidth(this.simpleImage.getWidth());
 		resolution.setHeight(this.simpleImage.getHeight());
 		
 		metadata.setResolutionREAL(resolution);
 		metadata.setFormat(format);
 		metadata.setSize((float)bytesToMb(photo.length));
 		
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
 	 
 	 @Override
 	 public long bytesToMb(int bytes) {
 	  return bytes / MEGABYTE ;
 	 }
 
 }
 
