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
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 
 /**
  * @author jeremiah
  *
  */
 public class SimpleContentMetaData extends AbstractContentMetaData
 {
 	
 	@SuppressWarnings("unchecked")
 	private transient List<Enum> supportedAttributeList = new Vector<Enum>();
	private ResourceURI resourceURI = null;
 	
 	@SuppressWarnings("unused")
 	private SimpleContentMetaData()
 	{
 		
 	}
 	
 	public SimpleContentMetaData(ResourceURI resourceURI, ResourceParameter... resourceParameters)
 	{
		this.resourceURI = resourceURI;
 	}
 	
 	public void refresh()
 	{
 		init();
 	}
 	
 	private void init()
 	{
 		getAttributeMap().put(Attributes.exists.toString(), false+"");
 
 		getAttributeMap().put(Attributes.executable.toString(), false+"");
 
 		getAttributeMap().put(Attributes.exists.toString(), false+"");
 
 		getAttributeMap().put(Attributes.readable.toString(), false+"");
 
 		getAttributeMap().put(Attributes.writeable.toString(), false+"");
 
 		getAttributeMap().put(Attributes.container.toString(), false+"");
 
 		getAttributeMap().put(Attributes.lastModified.toString(),null);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#exists()
 	 */
 	@Override
 	public Boolean exists()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.exists.toString()));
 	}
 
 	/* (non-Javadoc)
 	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#getLastModified()
 	 */
 	@Override
 	public Long getLastModified()
 	{		
 		return Long.parseLong(getAttributeMap().get(Attributes.lastModified.toString()));
 	}
 
 	/* (non-Javadoc)
 	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#isContainer()
 	 */
 	@Override
 	public Boolean isContainer()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.container.toString()));
 	}
 
 	/* (non-Javadoc)
 	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#isReadable()
 	 */
 	@Override
 	public Boolean isReadable()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.readable.toString()));
 	}
 
 	/* (non-Javadoc)
 	 * @see com.delcyon.capo.resourcemanager.types.ContentMetaData#isWriteable()
 	 */
 	@Override
 	public Boolean isWriteable()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.writeable.toString()));
 	}
 
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public Enum[] getAdditionalSupportedAttributes()
 	{
 		return supportedAttributeList.toArray(new Enum[]{});
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void addSupportedAttribute(Enum... name)
 	{
 		supportedAttributeList.addAll(Arrays.asList(name));
 	}
 
	@Override
	public ResourceURI getResourceURI()
	{		
		return resourceURI;
	}

 	
 
 }
