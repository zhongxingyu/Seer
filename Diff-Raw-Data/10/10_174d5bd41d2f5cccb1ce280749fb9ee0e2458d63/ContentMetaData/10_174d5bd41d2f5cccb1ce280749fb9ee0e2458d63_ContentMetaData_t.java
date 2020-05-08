 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.resourcemanager.types;
 
 import java.util.HashMap;
 import java.util.List;
 
 import com.delcyon.capo.resourcemanager.ContentFormatType;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 
 /**
  * @author jeremiah
  *
  */
 public interface ContentMetaData
 {
 
 	public enum Attributes
 	{
 		exists,
 		executable,
 		readable,
 		writeable,
 		container,
 		lastModified,
 		path, 
		uri,
		MD5
 	}
 
 	public enum Parameters {
 		DEPTH, 
 		USE_RELATIVE_PATHS, 
 		ROOT_PATH,
 		MONITOR
 		
 	}
 	
     //BEGIN RESOURCE METADATA
 	/**
 	 * Controls whether this metadata should be refreshed each time each time it's asked for. 
 	 * @return
 	 */
 	public boolean isDynamic();
 	
 	public boolean isInitialized();
 	
 	public void setInitialized(boolean isInitialized);
 	
 	public  ContentFormatType getContentFormatType();
 	
 	public  void setContentFormatType(ContentFormatType contentFormatType);
 	
 	public  Boolean exists();
 
 	public  Boolean isReadable();
 
 	public  Long getLength();
 
 	public  Boolean isWriteable();
 
 	public  String getMD5();
 
 	public  Boolean isContainer();
 	
 	public  HashMap<String, String> getAttributeMap();
 
 	public Long getLastModified();
 	
 	public void setValue(String name, String value);
 	
 	public String getValue(String name);
 	
 	public boolean isSupported(String attributeName);
 	
 	public List<String> getSupportedAttributes();
 
 	public List<ContentMetaData> getContainedResources();
 
 	public void addContainedResource(ContentMetaData contentMetaData);
 
 	public ResourceURI getResourceURI();
 	
 }
