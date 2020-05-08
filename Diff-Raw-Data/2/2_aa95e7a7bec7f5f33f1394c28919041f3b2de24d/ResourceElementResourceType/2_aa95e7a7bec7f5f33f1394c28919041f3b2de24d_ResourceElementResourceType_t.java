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
 package com.delcyon.capo.xml.dom;
 
 import java.util.ArrayList;
 
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceType;
 import com.delcyon.capo.resourcemanager.ResourceTypeProvider;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
 
 /**
  * @author jeremiah
  *
  */
@ResourceTypeProvider(schemes="resource",defaultLifeCycle=LifeCycle.GROUP,providerClass=ResourceElementResourceDescriptor.class)
 public class ResourceElementResourceType implements ResourceType
 {
 
 	private String proxyedURI;
 	private ResourceType proxyedResourceType;
 
 	@Override
 	public LifeCycle getDefaultLifeCycle()
 	{
 		return proxyedResourceType.getDefaultLifeCycle();
 	}
 
 	@Override
 	public ArrayList<ArrayList<Integer>> getDefaultTokenLists()
 	{
 		return proxyedResourceType.getDefaultTokenLists();
 	}
 
 	@Override
 	public String getName()
 	{
 		return proxyedResourceType.getName();
 	}
 
 	@Override
 	public ResourceDescriptor getResourceDescriptor(String resourceURI) throws Exception
 	{
 		throw new UnsupportedOperationException("call without declaring resource");
 	}
 
 	@Override
 	public boolean isIterable()
 	{
 		return proxyedResourceType.isIterable();
 	}
 
 	@Override
 	public boolean runtimeDefineableTokenLists()
 	{
 		return proxyedResourceType.runtimeDefineableTokenLists();
 	}
 
 	public ResourceDescriptor getResourceDescriptor(ResourceElement declaringResourceElement, String resourceURI) throws Exception
 	{
 		ResourceElementResourceDescriptor resourceElementResourceDescriptor = new ResourceElementResourceDescriptor(declaringResourceElement);				
 		return resourceElementResourceDescriptor;
 	}
 
 }
