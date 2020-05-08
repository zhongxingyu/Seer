 package com.MSSE.MobileDVR;
 
 import android.graphics.drawable.Drawable;
 
 public class ShowInfo
 {
 	private String title = "";
 	private String description = "";
 	private Drawable image = null;
 	
 	public ShowInfo()
 	{
 		this("", "", null);
 	}
 	
 	public ShowInfo(String title)
 	{
 		this(title, "", null);
 	}
 	
 	public ShowInfo(String title, String description)
 	{
 		this(title, description, null);
 	}
 	
 	public ShowInfo(String title, String description, Drawable image)
 	{
 		setTitle(title);
 		setDescription(description);
 		setImage(image);
 	}
 	
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	public String getDescription()
 	{
 		return description;
 	}
 	
 	public Drawable getImage()
 	{
 		return image;
 	}
 	
 	private void setTitle(String title)
 	{
 		this.title = title != null ? title : "";
 	}
 	
 	private void setDescription(String description)
 	{
 		this.description = description != null ? description : "";
 	}
 	
 	private void setImage(Drawable image)
 	{
 		this.image = image; // no check for null
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((description == null) ? 0 : description.hashCode());
 		result = prime * result + ((image == null) ? 0 : image.hashCode());
 		result = prime * result + ((title == null) ? 0 : title.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ShowInfo other = (ShowInfo) obj;
 		if (description == null)
 		{
 			if (other.description != null)
 				return false;
 		}
 		else if (!description.equals(other.description))
 			return false;
 		if (image == null)
 		{
 			if (other.image != null)
 				return false;
 		}
 		else if (!image.equals(other.image))
 			return false;
 		if (title == null)
 		{
 			if (other.title != null)
 				return false;
 		}
 		else if (!title.equals(other.title))
 			return false;
 		return true;
 	}
 
 }
