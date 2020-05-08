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
 package com.delcyon.capo.controller.elements;
 
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.controller.AbstractControl;
 import com.delcyon.capo.controller.ControlElementProvider;
 import com.delcyon.capo.datastream.NullOutputStream;
 import com.delcyon.capo.datastream.RegexFilterOutputStream;
 import com.delcyon.capo.datastream.StreamUtil;
 import com.delcyon.capo.datastream.TriggerFilterOutputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterOutputStream;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.resourcemanager.types.FileResourceType;
 import com.delcyon.capo.server.CapoServer;
 
 @ControlElementProvider(name="sync")
 public class SyncElement extends AbstractControl
 {
 	
 	public enum FilterType
 	{
 		triggerFilter, regexFilter
 	}
 	
 	private static HashMap<String, FilterType> filterTypeHashMap = null;
 	static
 	{
 		filterTypeHashMap = new HashMap<String, FilterType>();
 		FilterType[] filterTypes = FilterType.values();
 		for (FilterType filterType : filterTypes)
 		{
 			filterTypeHashMap.put(filterType.toString(), filterType);
 		}
 	}
 	
 	public enum Attributes
 	{
 		name,src,dest,onCopy,recursive,prune, syncAttributes,maxRetries
 	}
 	
 	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
     private int maxRetries = 5;
 	
 	
 	
 	@Override
 	public Attributes[] getAttributes()
 	{
 		return Attributes.values();
 	}
 
 	
 	
 
 	
 	@Override
 	public Attributes[] getRequiredAttributes()
 	{
 		return new Attributes[]{Attributes.dest,Attributes.src};
 	}
 
 	
 	@Override
 	public String[] getSupportedNamespaces()
 	{
 		return supportedNamespaces;
 	}
 
 
 	
 	@Override
 	public Object processServerSideElement() throws Exception
 	{
 		
 	    if(getAttributeValue(Attributes.maxRetries).matches("\\d+"))
 	    {
 	        this.maxRetries = Integer.parseInt(getAttributeValue(Attributes.maxRetries));
 	    }
 	   
 	    
 		//make a copy to send to the client
 		Element tempCopyElement = (Element) getControlElementDeclaration().cloneNode(true);
 		
 		//make sure we replace all of the variables in any child element attributes before sending this back to the client
 		NodeList nodeList = tempCopyElement.getChildNodes();
 		for(int currentNode = 0;currentNode < nodeList.getLength();currentNode++)
 		{
 			getParentGroup().replaceVarsInAttributeValues(nodeList.item(currentNode));
 		}
 		
 		tempCopyElement = (Element) getParentGroup().replaceVarsInAttributeValues(tempCopyElement);
 		//this was trying to work off of temp copy element, but don't know why
 		ResourceDescriptor sourceResourceDescriptor = getParentGroup().getResourceDescriptor(this,getAttributeValue(Attributes.src));
 		
		if (sourceResourceDescriptor != null && sourceResourceDescriptor.getResourceMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())).exists())
 		{
 			
 //			String destMD5 = null;			
 //			String varMD5 = getVarMD5ForCopyElement(tempCopyElement);
 			//commented out until we can find a good reason to cache, other than for the sake of caching!
 //			if (varMD5 != null)
 //			{
 //				//use version control to cache this information, we don't want to process this if we don't have to
 //				Element cacheElement = TaskManagerThread.getTaskManagerThread().getTaskDataElement("test_monitor",null,varMD5);
 //				if (cacheElement == null)
 //				{
 //					destMD5 = getDestMD5ForCopyElement(sourceResourceDescriptor, tempCopyElement);
 //				}
 //				else
 //				{
 //					destMD5 = cacheElement.getAttribute("destMD5");
 //				}
 //
 //				//cacheing
 //				if (cacheElement == null)
 //				{
 //					cacheElement = CapoApplication.getDocumentBuilder().newDocument().createElement("SyncData");
 //					cacheElement.setAttribute("destMD5", destMD5);
 //					TaskManagerThread.getTaskManagerThread().setTaskDataElement("test_monitor",null,varMD5,cacheElement);
 //				}
 //			}
 //			else //were not doing anything complicated, so just use the src md5 as the expected dest md5;
 //			{
 				
 			//}
 			
 			ResourceDescriptor destinationResourceDescriptor =  getParentGroup().openResourceDescriptor(this, getAttributeValue(Attributes.dest));
 			syncTree(sourceResourceDescriptor,destinationResourceDescriptor);
 			
 		}
 		else
 		{
 			CapoServer.logger.log(Level.WARNING, "couldn't process sync element: "+tempCopyElement.getAttribute("id"));
 		}
 		
 		
 		return null;
 	}
 	
 	
 
 	public void syncTree(ResourceDescriptor sourceResourceDescriptor, ResourceDescriptor destinationResourceDescriptor) throws Exception
     {
 	    ContentMetaData sourceContentMetaData = sourceResourceDescriptor.getResourceMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration()));
 	    ContentMetaData destinationContentMetaData = destinationResourceDescriptor.getResourceMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration()));
 	    
 	    
         if (sourceContentMetaData.exists() == false)
         {
            throw new Exception(" src='"+ sourceContentMetaData.getResourceURI() +"' does not exist, can't sync.");
         }
        if (sourceContentMetaData.isContainer() == true )
         {
             
             if (destinationContentMetaData.exists() == true && destinationContentMetaData.isContainer() == false)
             {
                 CapoApplication.logger.log(Level.WARNING, "Removing dst resource: "+destinationResourceDescriptor.getResourceURI());
                 destinationResourceDescriptor.performAction(null, Action.DELETE);
                 destinationResourceDescriptor.performAction(null, Action.CREATE,new ResourceParameter(ResourceDescriptor.DefaultParameters.CONTAINER, "true"));
                 destinationResourceDescriptor.close(null);
                 destinationResourceDescriptor.open(null);
             }
             
             //see if the opposing container exists, and if not, create it.
             if (destinationContentMetaData.exists() == false)
             {
                 destinationResourceDescriptor.performAction(null, Action.CREATE,new ResourceParameter(ResourceDescriptor.DefaultParameters.CONTAINER, "true"));
                 destinationResourceDescriptor.close(null);
                 destinationResourceDescriptor.open(null);
             }
             
             if (getAttributeBooleanValue(Attributes.recursive) == true)
             {
                 //process children here
                 List<ContentMetaData> sourceContainedResources = sourceContentMetaData.getContainedResources();
                 List<ContentMetaData> destinationContainedResources = destinationContentMetaData.getContainedResources();
 
 
 
                 HashMap<String, ContentMetaData> destinationContainedResourceHashMap = new HashMap<String, ContentMetaData>();
                 for (ContentMetaData contentMetaData : destinationContainedResources)
                 {
                     String localName = contentMetaData.getResourceURI().getPath().replaceAll(destinationContentMetaData.getResourceURI().getPath(), "");                
                     destinationContainedResourceHashMap.put(localName, contentMetaData);
                 } 
 
 
                 for (ContentMetaData contentMetaData : sourceContainedResources)
                 {
                     String localName = contentMetaData.getResourceURI().getPath().replaceAll(sourceContentMetaData.getResourceURI().getPath(), "");                
                     ResourceDescriptor childDestinationResourceDescriptor = destinationResourceDescriptor.getChildResourceDescriptor(this, localName);
                     ResourceDescriptor childSourceResourceDescriptor = sourceResourceDescriptor.getChildResourceDescriptor(this, localName);
                     destinationContainedResourceHashMap.remove(localName);
                     syncTree(childSourceResourceDescriptor, childDestinationResourceDescriptor);
                     childSourceResourceDescriptor.release(null);
                     childDestinationResourceDescriptor.release(null);
                 }
 
                 if (getAttributeBooleanValue(Attributes.prune) == true)
                 {
                     Set<Entry<String, ContentMetaData>> unknownDestinationResourceEntrySet = destinationContainedResourceHashMap.entrySet();
                     for (Entry<String, ContentMetaData> entry : unknownDestinationResourceEntrySet)
                     {
                         ResourceDescriptor childDestinationResourceDescriptor = destinationResourceDescriptor.getChildResourceDescriptor(this, entry.getKey());
                         CapoApplication.logger.log(Level.WARNING, "Removing dst resource: "+childDestinationResourceDescriptor.getResourceURI().getBaseURI());
                         childDestinationResourceDescriptor.performAction(null, Action.DELETE);
                     }
                 }
                 
             }
             
             copyContentMetaData(sourceContentMetaData,destinationResourceDescriptor);
             
         }
         else
         {
             String srcMD5 = sourceResourceDescriptor.getResourceMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())).getMD5();
             String destMD5 = destinationResourceDescriptor.getResourceMetaData(getParentGroup(), ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())).getMD5();
             if (destMD5 == null || destMD5.equals(srcMD5) == false)
             {
                 int retryCount = 0;
                 while(retryCount < maxRetries)
                 {
                     CapoApplication.logger.log(Level.INFO, "Syncing file: "+sourceResourceDescriptor.getResourceURI().getBaseURI()+" ==> "+destinationResourceDescriptor.getResourceURI().getBaseURI());
                     ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
                     resourceParameterBuilder.addAll(getControlElementDeclaration());
                     resourceParameterBuilder.addParameter(FileResourceType.Parameters.USE_TEMP, "true");
                     OutputStream filteredOutputStream = wrapOutputStream(destinationResourceDescriptor.getOutputStream(getParentGroup(), resourceParameterBuilder.getParameters()),getControlElementDeclaration());
                     
                     Exception exception = null;                    
                     try
                     {
                         StreamUtil.readInputStreamIntoOutputStream(sourceResourceDescriptor.getInputStream(getParentGroup(),ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration())), filteredOutputStream);
                     }
                     catch (Exception exceptionLocal)
                     {
                         exception = exceptionLocal;
                     }
 
                     try
                     {
                         filteredOutputStream.flush();
                     }
                     catch (Exception exceptionLocal)
                     {
                         if(exception == null)
                         {
                             exception = exceptionLocal;
                         }                       
                     }
 
                     try
                     {                       
                         filteredOutputStream.close();
                     }
                     catch (Exception exceptionLocal)
                     {
                         if(exception == null)
                         {
                             exception = exceptionLocal;
                         }
                     }
                    
                     if(exception != null)
                     {
                         CapoApplication.logger.log(Level.WARNING, "There was an error trying to sync, restarting. ("+retryCount+")"+exception.getMessage());
                         
                     }
                     
                     //we'll wait here until this is done processing
                     CapoApplication.logger.log(Level.INFO, "waiting here on output metadata");
                     ContentMetaData outputMetaData = destinationResourceDescriptor.getOutputMetaData(getParentGroup());
                     if(outputMetaData != null && srcMD5.equals(outputMetaData.getMD5()))
                     {
                         destinationResourceDescriptor.performAction(getParentGroup(), Action.COMMIT);
                         break;
                     }
                     else
                     {
                         destinationResourceDescriptor.performAction(getParentGroup(), Action.ROLLBACK);
                         destinationResourceDescriptor.reset(State.OPEN);
                         sourceResourceDescriptor.reset(State.OPEN);
                         retryCount++;
                     }
                 }
                
                 if(retryCount == maxRetries)
                 {
                     destinationResourceDescriptor.close(getParentGroup());
                     sourceResourceDescriptor.close(getParentGroup());
                     throw new Exception("Couldn't sync "+sourceResourceDescriptor.getResourceURI().getResourceURIString());
                 }
 
                 
                 
                 copyContentMetaData(sourceContentMetaData,destinationResourceDescriptor);
                 
                 CapoApplication.logger.log(Level.INFO, "Done Syncing");
                 if(getAttributeValue(Attributes.onCopy).isEmpty() == false)
                 {
                     getParentGroup().set(getAttributeValue(Attributes.onCopy), "true");
                 }
             }
         }
     }
 
 
 
     private void copyContentMetaData(ContentMetaData sourceContentMetaData, ResourceDescriptor destinationResourceDescriptor) throws Exception
 	{
     	String keepAttributesValue = getAttributeValue(Attributes.syncAttributes);
         if (keepAttributesValue.isEmpty() == false)
         {
         	Vector<ContentMetaData.Attributes> copyAttributesList = new Vector<ContentMetaData.Attributes>();
         	if (keepAttributesValue.equalsIgnoreCase("true"))
         	{
         		Set<String> attributeSet = sourceContentMetaData.getAttributeMap().keySet();
         		for (String contentAttributeName : attributeSet)
 				{
         			try
         			{
         				copyAttributesList.add(ContentMetaData.Attributes.valueOf(contentAttributeName));
         			} catch (IllegalArgumentException illegalArgumentException)
         			{
         				CapoApplication.logger.log(Level.WARNING, "Couldn't find attribte name of "+contentAttributeName+" in content metadata");
         			}
 				}
         	}
         	else
         	{
         		String[] keepAttributesArray = keepAttributesValue.split(",");
         		for (String contentAttributeName : keepAttributesArray)
 				{
         			try
         			{
         				copyAttributesList.add(ContentMetaData.Attributes.valueOf(contentAttributeName));
         			} catch (IllegalArgumentException illegalArgumentException)
         			{
         				CapoApplication.logger.log(Level.WARNING, "Couldn't find attribte name of "+contentAttributeName+" in content metadata");
         			}
 				}
         	}
         	
         	for (ContentMetaData.Attributes attributes : copyAttributesList)
 			{            		
             	destinationResourceDescriptor.performAction(null, Action.SET_ATTRIBUTE, new ResourceParameter(attributes,sourceContentMetaData.getValue(attributes.toString())));	
 			}
         }
 		
 	}
 
 
 
 
 
 	private OutputStream wrapOutputStream(OutputStream outputStream, Element copyElement) throws Exception
 	{
 		
 		OutputStream lastOutputStream = outputStream;
 			if (copyElement != null)
 			{
 				
 				NodeList nodeList = copyElement.getChildNodes();
 				
 				for (int currentNode = nodeList.getLength() - 1; currentNode >= 0; currentNode--)
 				{
 					Node elementNode = nodeList.item(currentNode);
 					if (elementNode instanceof Element)
 					{
 					    if (elementNode.getNamespaceURI() == null || elementNode.getNamespaceURI().equals(CapoApplication.SERVER_NAMESPACE_URI) == false)
 					    {
 					        //skip this if it's the wrong name space, cause its not a filter
 					        continue;
 					    }
 						Element element = (Element) getParentGroup().replaceVarsInAttributeValues(elementNode);
 						String name = element.getLocalName();
 						
 						FilterType filterType = filterTypeHashMap.get(name);
 						if (filterType == null)
 						{
 							CapoServer.logger.log(Level.WARNING, "unknown filter type: " + name);
 							continue;
 						}
 
 						switch (filterType)
 						{
 							case triggerFilter:
 								lastOutputStream = new TriggerFilterOutputStream(lastOutputStream, element.getAttribute("trigger"), element.getAttribute("replacement"));
 								break;
 							case regexFilter:
 								lastOutputStream = new RegexFilterOutputStream(lastOutputStream, element.getAttribute("regex"), element.getAttribute("replacement"),1);
 								break;
 							default:
 								break;
 						}
 
 					}
 				}
 				
 				
 			}
 			return lastOutputStream;
 		
 	}
 
 	@SuppressWarnings("unused")
     private String getVarMD5ForCopyElement(Element tempCopyElement) throws Exception
 	{
 		String md5 = "";
 		MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(new NullOutputStream());
 
 		md5FilterOutputStream.write(tempCopyElement.getAttribute("src"));
 		md5FilterOutputStream.write(tempCopyElement.getAttribute("dest"));
 
 		NodeList nodeList = tempCopyElement.getChildNodes();
 		int relevantChildrenCount = 0;
 		
 		for (int currentNode = 0; currentNode < nodeList.getLength(); currentNode++)
 		{
 			Node elementNode = nodeList.item(currentNode);
 			if (elementNode instanceof Element)
 			{
 				String name = elementNode.getLocalName();
 				
 				FilterType filterType = filterTypeHashMap.get(name);
 				if (filterType == null)
 				{
 					//skip anything that won't effect the end result
 					continue;
 				}
 				else
 				{
 					relevantChildrenCount++;
 				}
 				NamedNodeMap attributeList = elementNode.getAttributes();
 				
 				for (int currentAttribute = 0; currentAttribute < attributeList.getLength(); currentAttribute++)
 				{
 					Node attributeNode = attributeList.item(currentAttribute);
 					if (attributeNode instanceof Attr)
 					{
 						Attr attr = (Attr) attributeNode;
 						md5FilterOutputStream.write(attr.getValue());
 					}
 				}
 			}
 		}
 		
 		md5FilterOutputStream.close();
 		md5 = md5FilterOutputStream.getMD5();
 		if (relevantChildrenCount > 0)
 		{		
 			return md5;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	//This runs sync through a quick test to determine what the final md5 will look like on the other side. We do this since local processing is cheaper than just saying we don't know, and running it over the network
 	@SuppressWarnings("unused")
     private String getDestMD5ForCopyElement(ResourceDescriptor fileResourceDescriptor, Element copyElement) throws Exception
 	{
 
 		NullOutputStream nullOutputStream = new NullOutputStream();
 		MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(nullOutputStream);
 		OutputStream lastOutputStream = wrapOutputStream(md5FilterOutputStream, copyElement);		
 		StreamUtil.readInputStreamIntoOutputStream(fileResourceDescriptor.getInputStream(getParentGroup(),ResourceParameterBuilder.getResourceParameters(copyElement)), lastOutputStream);		
 		return md5FilterOutputStream.getMD5();
 	}
 	
 	
 }
