 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
 * $Revision: 1.22 $
 * $Date: 2007-06-25 22:38:09 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTIDFactory;
 
 /**
  * ObjectTypeHandler
  * Class name and description
  *
  * Date created: Oct 4, 2004
  *
  * @author scott<p>
  *
  */
 public class ObjectTypeHandler extends ResourceTypeHandler
 {
 	TypeService typeService;	
 	String objectName = null;
 	String objectClassName = null;
 	String parentObjectName = null;
 	ResourceDefinition [] resources = null;
 	XMLDatabase xmlDB = null;
 	
 	public ObjectTypeHandler(TypeService typeService, XMLDatabase xmlDB)
 	{
 		super("object");
 		this.typeService = typeService;
 		this.xmlDB = xmlDB;
 	}
 	
 	public ObjectTypeHandler(
 			String objectName,
 			String objectClassName,
 			String parentObjectName,
 			ResourceDefinition [] resources,
 			TypeService typeService,
 			XMLDatabase xmlDB)
 	{
 		this(typeService, xmlDB);
 		this.objectName = objectName;
 		this.parentObjectName = parentObjectName;
 		this.resources = resources;
 		this.objectClassName = objectClassName;
		
		if(objectName == null){
			throw new RuntimeException("Cannot have a null objectName");
		}
 	}
 	
 	public String getObjectName()
 	{
 		return objectName;
 	}
 	
	public boolean isObjectReferenceHandler()
	{
		// objectName should only be null if this is the objectReferenceHandler 
		// this handler was created with the 2 arg constructor 
		return objectName == null;
	}
	
 	/* (non-Javadoc)
 	 * @see org.concord.portfolio.xml.ResourceTypeHandler#handleElement(org.w3c.dom.Element, java.util.Properties)
 	 */
 	public Object handleElement(OTXMLElement element, Properties elementProps,
 	        String relativePath, XMLDataObject parent)
 	{
 		String refid = element.getAttributeValue("refid");
 		if(refid != null && refid.length() > 0){
 			return new XMLDataObjectRef(refid, element);
 		}
 				
 		String idStr = element.getAttributeValue("id");
 		if(idStr != null && idStr.length() <= 0) {
 			idStr = null;
 		}
 
 		String localIdStr = element.getAttributeValue("local_id");
 		if(localIdStr != null && localIdStr.length() <= 0) {
 			localIdStr = null;
 		}
 		
 		XMLDataObject obj = null;
 		try {
 		    if(idStr == null && localIdStr == null && relativePath != null) {
 		        OTID pathId = OTIDFactory.createOTID(relativePath);
 		        // System.err.println(relativePath);
 		        obj = xmlDB.createDataObject(element, pathId);
 		    } else if(idStr == null && localIdStr != null) {
 		        OTID id = xmlDB.getOTIDFromLocalID(localIdStr);  
 		        obj = xmlDB.createDataObject(element, id);
 		    } else {
 		        obj = xmlDB.createDataObject(element, idStr);
 		    }
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		String objRelativePath = relativePath;
 		OTID objId = obj.getGlobalId();
 		if(idStr != null || localIdStr != null) {
 		    objRelativePath = objId.toString(); 
 		}
 		
 		obj.setResource(OTrunkImpl.RES_CLASS_NAME, getClassName());
 		
 		List attributes = element.getAttributes();
 		for(Iterator attIter = attributes.iterator(); attIter.hasNext(); ) {
 			OTXMLAttribute attrib = (OTXMLAttribute)attIter.next();
 			String attribName = attrib.getName();
 			if(attribName.equals("id") ||
 					attribName.equals("refid") ||
 					attribName.equals("local_id")) {
 				continue;
 			}
 			
			if(isObjectReferenceHandler()){
				System.err.println("Invalid <object> attribute: " +						
						TypeService.attributePath(attrib));
				System.err.println("  Only viable <object> attribute is \"refid\"");
				continue;
			}
			
 			try {
 			    
 				Object resValue = handleChildResource(element, attribName, 
 				        attrib.getValue(), objRelativePath, obj);
 				obj.setResource(attribName, resValue);
 			} catch (HandleElementException e) {
 				System.err.println(e.getMessage() + " in attribute: " +
 						TypeService.attributePath(attrib));
 			}
 		}
 		
 		List children = element.getChildren();		
 		for(Iterator childIter = children.iterator(); childIter.hasNext(); ) {
 		    OTXMLElement child = (OTXMLElement)childIter.next();
 
 			try {
 				Object resValue = handleChildResource(element, child.getName(), 
 				        child, objRelativePath, obj);
 				if(resValue == null) {
                     // this should be an option debug or log message
 					// System.out.println("null resource: " + TypeService.elementPath(child));
 				}
 				obj.setResource(child.getName(),resValue);
 			} catch (HandleElementException e) {
 				System.err.println("error in element: " +
 						TypeService.elementPath(child));
 				e.printStackTrace();				
 			}
 		}
 		
 		return obj;
 	}
 
 	public String getClassName()
 	{
 		return objectClassName;
 	}
 	
 	/**
 	 * This element comes with extra information.  The name of 
 	 * the element identifies it as a resource in the parentType
 	 * The resource in the parent type has type information.  So
 	 * if the element is "&lt;myText>hi this is my text&lt;/myText>"
 	 * and myText is defined as a "string" in the parentType then
 	 * this element will be turned into a string.
 	 * @param parentElement TODO
 	 * @param parentType
 	 * @param child
 	 * 
 	 * @return
 	 */
 	public Object handleChildResource(OTXMLElement parentElement, String childName, 
 	        Object childObj, String relativeParentPath, XMLDataObject parent)
 		throws HandleElementException
 	{
 		Properties elementProps;
 
 		ResourceDefinition resourceDef = getResourceDefinition(childName);
 		
 		if(resourceDef == null) {
 			System.err.println("error reading childName: " + childName +
 					" in type: " + getObjectName());
 			return null;
 		}
 		elementProps = getResourceProperties(resourceDef);
 		String resPrimitiveType = resourceDef.getType();	
 
 		String resourceType = resPrimitiveType;
 		if(resPrimitiveType.equals("object") &&
 		        childObj instanceof String){
 		    
 		    // this is an object reference
 			String refid = (String)childObj;
 			if(refid != null && refid.length() > 0){
 				return new XMLDataObjectRef(refid, parentElement);
 			}		    
 		}
 		
 		if(resPrimitiveType.equals("object")) {
 			if(!(childObj instanceof OTXMLElement)) {
 				System.err.println("child of type object must be an element or string");
 				return null;
 			}
 			OTXMLElement child = (OTXMLElement)childObj;
 
 			// This allows users to put refid on the attribute element:
 			// like: <OTGraph><xAxis refid="id_of_some_other_object"/></OTGraph>
 			String childRefId = child.getAttributeValue("refid");
 			
 			// If this element doesn't have a reference id then 
 			// then the first child of this element is object 
 			if(childRefId == null) {
 				List children = child.getChildren();
 				if(children.size() < 1) {
 					// empty object tag
 					// this happens a lot in the current xml so
 					// I'm taking this out for now
 					/*
 					System.err.println("empty object field: " + 
 							TypeService.elementPath(child));
 							*/
 					return null;
 				}
 				childObj = children.get(0);
                 if(children.size() > 1) {
                     System.err.println("Warning: Only the first element is returned from " +
                             TypeService.elementPath(child));
                 }
 				resourceType = ((OTXMLElement)childObj).getName();
 			}			
 		}
 		
 		ResourceTypeHandler resHandler = typeService.getElementHandler(resourceType);
 		
 		if(resHandler == null){
 			System.err.println("Can't find type handler for: " +
 					resourceType);
 			return null;
 		}
 		
 		if(childObj instanceof String) {
 			if(resHandler instanceof PrimitiveResourceTypeHandler){
 				return ((PrimitiveResourceTypeHandler)resHandler).
 					handleElement((String)childObj, elementProps);
 			} else {
 				throw new HandleElementException("Can't use an attribute for a non-primitive type");
 			}
 		} else {
 		    String childRelativePath = relativeParentPath + "/" + childName;
 			return resHandler.handleElement((OTXMLElement)childObj, elementProps,
 			        childRelativePath, parent);
 		}
 	}
 	
 	public ObjectTypeHandler getParentType()
 	{
 		ObjectTypeHandler parentType = null;
 		if(parentObjectName != null && parentObjectName.length() > 0) {
 
 			parentType = (ObjectTypeHandler)typeService.getElementHandler(parentObjectName);
 			if(parentType == null) {
 				System.err.println("can't find parent: " + parentObjectName +
 						" of: " + getObjectName());
 			}
 			return parentType;
 		}
 		return null;		
 	}
 	
 	public ResourceDefinition [] getResourceDefinitions()
 	{
 		return resources;
 	}
 	
 	public ResourceDefinition getResourceDefinition(String name)
 	{	
 		if(resources == null) {
 			throw new RuntimeException("null resource defs in: " + objectName);
 		}
 		for(int i=0; i<resources.length; i++) {
 			if(resources[i].getName().equals(name)) {
 				return resources[i];
 			}
 		}
 
 		ObjectTypeHandler parentType = getParentType(); 
 		if(parentType == null) {
 			return null;
 		}
 
 		return parentType.getResourceDefinition(name);		
 	}
 	
 	public Properties getResourceProperties(ResourceDefinition resType)
 	{
 		// Go through the resource type object and make a properties
 		// object that descriptes the type both the type of the resource and
 		// any extra paramaters of the type
 		Properties props = new Properties();
 		ResourceDefinition.Parameter []  params = resType.getParameters();
 		if(params == null) {
 			return null;
 		}
 		
 		for(int i=0; i<params.length; i++){
 			props.setProperty(params[i].name, params[i].value);
 		}
 		return props;		
 	}	
 }
