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
  * $Revision: 1.31 $
  * $Date: 2007-10-04 21:28:21 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.framework.otrunk.otcore.OTClassProperty;
 import org.concord.framework.otrunk.otcore.OTType;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTUUID;
 import org.concord.otrunk.xml.XMLReferenceInfo.XmlType;
 
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
 	private static final Logger logger = Logger.getLogger(ObjectTypeHandler.class.getName());
 	OTClass otClass;
 	TypeService typeService;	
 	String objectName = null;
 	String objectClassName = null;
 	String parentObjectName = null;
 	XMLDatabase xmlDB = null;
 	
 	public ObjectTypeHandler(TypeService typeService, XMLDatabase xmlDB)
 	{
 		super("object");
 		this.typeService = typeService;
 		this.xmlDB = xmlDB;
 	}
 	
 	public ObjectTypeHandler(
 		    OTClass otClass,
 			String objectName,
 			String objectClassName,
 			String parentObjectName,
 			TypeService typeService,
 			XMLDatabase xmlDB)
 	{
 		this(typeService, xmlDB);
 		this.otClass = otClass;
 		this.objectName = objectName;
 		this.parentObjectName = parentObjectName;
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
 	@Override
 	public Object handleElement(OTXMLElement element, String relativePath,
 	        XMLDataObject parent, String propertyName)
 	{
 		if(isObjectReferenceHandler()){
 			String refid = element.getAttributeValue("refid");
 			return handleRefid(refid, parent, element, propertyName);
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
 		        
 		        // if this object has an UUID idStr then mark it to be preserved
 		        if(idStr != null && obj.getGlobalId() instanceof OTUUID){
 		        	obj.setPreserveUUID(true);
 		        }
 		    }
 		    obj.setContainer(parent);
 		    obj.setContainerResourceKey(propertyName);
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Failed to created data object", e);
 			return null;
 		}
 
 		String objRelativePath = relativePath;
 		OTID objId = obj.getGlobalId();
 		if(idStr != null || localIdStr != null) {
 		    objRelativePath = objId.toExternalForm(); 
 		}
 
 		OTDataObjectType type = new OTDataObjectType(getClassName());
 		obj.setType(type);
 		
 		List<?> attributes = element.getAttributes();
 		for(Iterator<?> attIter = attributes.iterator(); attIter.hasNext(); ) {
 			OTXMLAttribute attrib = (OTXMLAttribute)attIter.next();
 			String attribName = attrib.getName();
 			if(attribName.equals("id") ||
 					attribName.equals("local_id")) {
 				continue;
 			}
 			
 			if(isObjectReferenceHandler()){
 				logger.warning("Invalid <object> attribute: " +						
 						TypeService.attributePath(attrib));
 				logger.warning("  Only viable <object> attribute is \"refid\"");
 				continue;
 			}
 			
 			if(attribName.equals("unset")){
 				obj.setSaveNulls(true);
 				String nullResourcesStr = attrib.getValue();
 				String[] nullResources = nullResourcesStr.split(" ");
 				for(int i=0; i<nullResources.length; i++){
 					obj.setResource(nullResources[i], null);
 				}
 				continue;
 			}
 			
 			try {
 			    
 				Object resValue = handleChildResource(element, attribName, 
 				        attrib.getValue(), objRelativePath, obj, XmlType.ATTRIBUTE,
 				        null);
 				obj.setResource(attribName, resValue);
 				
 				if(xmlDB.isTrackResourceInfo()){
 					XMLReferenceInfo info = obj.getReferenceInfo(attribName);
 					if(info == null){
 						info = new XMLReferenceInfo();
 						obj.setResourceInfo(attribName, info);
 					}
 					info.xmlType = XmlType.ATTRIBUTE;				
 				}
 			} catch (HandlerException e) {
 				logger.warning(e.getMessage() + " in attribute: " +
 						TypeService.attributePath(attrib));
 			}
 		}
 
 		List<?> content = element.getContent();
 		String previousComment = null;
 		
 		for(Iterator<?> childIter = content.iterator(); childIter.hasNext(); ) {
 		    OTXMLContent childContent = (OTXMLContent)childIter.next();
 		    if(childContent instanceof OTXMLComment){
 		    	previousComment = ((OTXMLComment) childContent).getText();
 		    }
 		    
 		    if(!(childContent instanceof OTXMLElement)){
 		    	continue;
 		    }
 		    
 		    OTXMLElement child = (OTXMLElement) childContent;
 			try {
 				Object resValue = handleChildResource(element, child.getName(), 
 				        child, objRelativePath, obj, XmlType.ELEMENT, previousComment);
 				if(resValue == null) {
                     // this should be an option debug or log message
 					// System.out.println("null resource: " + TypeService.elementPath(child));
 				}
 				String childName = child.getName();
 				obj.setResource(child.getName(),resValue);
 				if(xmlDB.isTrackResourceInfo()){
 					XMLReferenceInfo info = obj.getReferenceInfo(childName);
 					if(info == null){
 						info = new XMLReferenceInfo();
 						obj.setResourceInfo(childName, info);
 					}
 					info.xmlType = XmlType.ELEMENT;									
 				}
 			} catch (HandlerException e) {
 				logger.log(Level.WARNING, "error in element: " +
 						TypeService.elementPath(child), e);
 			}
 			
 			// Clear the previous comment so it isn't picked up by the next element
 			previousComment = null;
 		}
 		
 		if (parent != null) {
 			logger.finest("Processed child: " + obj.getGlobalId() + " of parent: " + parent.getGlobalId());
 			// FIXME Not sure what the property string is here...
			xmlDB.recordReference(parent, obj, propertyName);
 		}
 		
 		return obj;
 	}
 
 	public String getClassName()
 	{
 		return otClass.getName();
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
 	        Object childObj, String relativeParentPath, XMLDataObject parent, 
 	        XmlType xmlType, String comment)
 		throws HandlerException
 	{
 		OTClassProperty otProperty = otClass.getProperty(childName);
 		if(otProperty == null) {
 			logger.warning("error reading property \"" + childName + "\"" +
 					" it is not defined for type: " + getObjectName());
 			return null;
 		}
 		OTType otType = otProperty.getType();
 		
 		XMLReferenceInfo resInfo = null;
 		if(xmlDB.isTrackResourceInfo()){
 			resInfo = parent.getReferenceInfo(childName);
 			if(resInfo == null){
 				resInfo = new XMLReferenceInfo();
 				parent.setResourceInfo(childName, resInfo);
 			}
 			resInfo.xmlType = xmlType;
 			
 			resInfo.comment = comment;
 		}
 
 		// This should be refactored so this case is handled by the handleAttribute method
 		// of this class
 		if(otType instanceof OTClass &&
 				childObj instanceof String){
 		    
 		    // this is an object reference
 			String refid = (String)childObj;
 			
 			return handleRefid(refid, parent, parentElement, childName);
 		}
 		
 		if(otType instanceof OTClass) {
 			if(!(childObj instanceof OTXMLElement)) {
 				logger.warning("child of type object must be an element or string");
 				return null;
 			}
 			OTXMLElement child = (OTXMLElement)childObj;
 
 			// This allows users to put refid on the attribute element:
 			// like: <OTGraph><xAxis refid="id_of_some_other_object"/></OTGraph>
 			// FIXME this used to work but I don't see how it could work now.
 			// If it hasn't worked for a whiel it should be removed because it is 
 			// really unnecessary.
 			String childRefId = child.getAttributeValue("refid");
 			
 			// If this element doesn't have a reference id then 
 			// then the first child of this element is object 
 			if(childRefId == null) {
 				List<?> children = child.getChildren();
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
                     logger.warning("Warning: Only the first element is returned from " +
                             TypeService.elementPath(child));
                 }
 				String childElementName = ((OTXMLElement)childObj).getName();
 				
 				// Should figure out how to get the OTType for this class name it could be
 				// a shortcut name.  First check if it is a Fully Qualified name.
 				if(childElementName.equals("object")){
 					otType = OTrunkImpl.getOTClass("org.concord.framework.otrunk.OTObject");
 				} else {
 					otType = OTrunkImpl.getOTClass(childElementName);
 
 					if(otType == null){
 						otType = typeService.getClassByShortcut(childElementName);
 					}
 					
 					if(otType == null){
 						throw new IllegalStateException("Can't find OTClass for <" + childElementName + "> check the imports"); 						
 					}
 				}				
 			}			
 		}
 		
 		if(otType == null){
 			throw new IllegalStateException("Can't find otType for property: " + otProperty.getName() + 
 					" on class: " + otClass.getName()); 
 		}
 		
 		ResourceTypeHandler resHandler = typeService.getElementHandler(otType);		
 		if(resHandler == null){
 			logger.warning("Can't find type handler for: " +
 					otType.getName());
 			return null;
 		}
 		
 		if(childObj instanceof String) {
 			return resHandler.handleAttribute((String)childObj, childName, parent);
 		} else {
 		    String childRelativePath = relativeParentPath + "/" + childName;
 			return resHandler.handleElement((OTXMLElement)childObj, childRelativePath,
 			        parent, childName);
 		}		
 	}
 	
 	public ObjectTypeHandler getParentType()
 	{
 		ObjectTypeHandler parentType = null;
 		if(parentObjectName != null && parentObjectName.length() > 0) {
 
 			parentType = (ObjectTypeHandler)typeService.getElementHandler(parentObjectName);
 			if(parentType == null) {
 				logger.warning("can't find parent: " + parentObjectName +
 						" of: " + getObjectName());
 			}
 			return parentType;
 		}
 		return null;		
 	}
 
 	/**
 	 * FIXME this method isn't used yet, but using it should simplify the 
 	 * handleChildResource method.
 	 * 
 	 */
 	@Override
     public Object handleAttribute(String value, String name, XMLDataObject parent)
         throws HandlerException
     {
 		return handleRefid(value, parent, parent.getElement(), name);
     }
 	
 	private Object handleRefid(String refid, XMLDataObject referringObject, 
 		OTXMLElement element, String property)
 	{
 		if(refid != null && refid.length() > 0){
 			logger.finest("Handling refid: " + refid + " of parent: " + referringObject.getGlobalId());
 			return new XMLDataObjectRef(refid, element, referringObject, property);
 		}
 
 		logger.warning("id cannot be an empty string or null");
 		return null;
 	}
 	
 }
