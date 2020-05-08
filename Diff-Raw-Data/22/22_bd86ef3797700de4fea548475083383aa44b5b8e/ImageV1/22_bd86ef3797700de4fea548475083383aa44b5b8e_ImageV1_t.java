 package com.redhat.topicindex.rest.entities;
 
 import com.redhat.topicindex.rest.entities.interfaces.RESTBaseEntityV1;
 
 /**
  * A REST representation of the ImageFile database entity
  */
 public class ImageV1 extends RESTBaseEntityV1<ImageV1>
 {
 	public static final String FILENAME_NAME = "filename";
 	public static final String IMAGEDATA_NAME = "imageData";
 	public static final String DESCRIPTION_NAME = "description";
 	
 	private String filename;
 	private byte[] imageData;
 	private byte[] thumbnail;
 	private byte[] imageDataBase64;
 	private String description;
 	
 	@Override
 	public ImageV1 clone(boolean deepCopy)
 	{
 		final ImageV1 retValue = new ImageV1();
 		
 		this.cloneInto(retValue, deepCopy);
 		
 		retValue.filename = this.filename;
 		retValue.description = this.description;
 		
		if (deepCopy)
		{
			/* use arraycopy() as a GWT compatible alternative to clone() */
			
			retValue.imageData = new byte[this.imageData.length]; 
			System.arraycopy(this.imageData, 0, retValue.imageData, 0, imageData.length);
			retValue.thumbnail = new byte[this.thumbnail.length];
			System.arraycopy(this.thumbnail, 0, retValue.thumbnail, 0, thumbnail.length);
			retValue.imageDataBase64 = new byte[this.imageDataBase64.length];
			System.arraycopy(this.imageDataBase64, 0, retValue.imageDataBase64, 0, imageDataBase64.length);
		}
		else
		{
			retValue.imageData = this.imageData;
			retValue.thumbnail = this.thumbnail;
			retValue.imageDataBase64 = this.imageDataBase64;
		}
		
 		return retValue;
 	}
 
 	public String getFilename()
 	{
 		return filename;
 	}
 
 	public void setFilename(final String filename)
 	{
 		this.filename = filename;
 	}
 	
 	public void setFilenameExplicit(final String filename)
 	{
 		this.filename = filename;
 		this.setParamaterToConfigured(FILENAME_NAME);
 	}
 
 	public byte[] getImageData()
 	{
 		return imageData;
 	}
 
 	public void setImageData(final byte[] imageData)
 	{
 		this.imageData = imageData;
 	}
 	
 	public void setImageDataExplicit(final byte[] imageData)
 	{
 		this.imageData = imageData;
 		this.setParamaterToConfigured(IMAGEDATA_NAME);
 	}
 
 	public byte[] getThumbnail()
 	{
 		return thumbnail;
 	}
 
 	public void setThumbnail(final byte[] thumbnail)
 	{
 		this.thumbnail = thumbnail;
 	}
 
 	public byte[] getImageDataBase64()
 	{
 		return imageDataBase64;
 	}
 
 	public void setImageDataBase64(final byte[] imageDataBase64)
 	{
 		this.imageDataBase64 = imageDataBase64;
 	}
 
 	public String getDescription()
 	{
 		return description;
 	}
 
 	public void setDescription(final String description)
 	{
 		this.description = description;
 	}
 	
 	public void setDescriptionExplicit(final String description)
 	{
 		this.description = description;
 		this.setParamaterToConfigured(DESCRIPTION_NAME);
 	}
 }
