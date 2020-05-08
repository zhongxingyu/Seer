 /******************************************************************************* 
  * Copyright (c) 2007 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.internal;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import java.util.Properties;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.project.ext.IValueInfo;
 import org.jboss.tools.common.model.project.ext.event.Change;
 import org.jboss.tools.common.model.project.ext.store.XMLStoreConstants;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.model.project.ext.store.XMLStoreHelper;
 import org.w3c.dom.Element;
 
 /**
  * @author Viacheslav Kabanovich
  */
 public class KbObject implements Cloneable {
 	/**
 	 * Object that allows to identify this object.
 	 */
 	protected Object id;
 
 	/**
 	 * Path of resource where this object is declared.
 	 */
 	protected IPath source;
 
 	/**
 	 * Resource where this object is declared.
 	 */
 	protected IResource resource = null;
 
 	/**
 	 * Parent kb object in the kb model.
 	 */
 	protected KbObject parent;
 	
 	//locations of xml attributes
	protected Map<String,IValueInfo> attributesInfo = new HashMap<String, IValueInfo>();
 
 	public KbObject() {}
 
 	public IKbProject getKbProject() {
 		return parent == null ? null : parent.getKbProject();
 	}
 
 	public Object getId() {
 		return id;
 	}
 	
 	/**
 	 * Sets id for this object.
 	 * For most objects it is object of Java or XML model 
 	 * from which this object is loaded.
 	 */
 	public void setId(Object id) {
 		this.id = id;
 	}
 
 	/**
 	 * Sets path of resource that declares this object.
 	 */
 	public void setSourcePath(IPath path) {
 		source = path;
 	}
 	
 	/**
 	 * Returns path of resource that declares this object.
 	 * @return
 	 */
 	public IPath getSourcePath() {
 		if(source == null && parent != null) return parent.getSourcePath();
 		return source;
 	}
 
 	public IResource getResource() {
 		if(resource != null) return resource;
 		if(resource == null && id instanceof IAdaptable) {
 			IResource r = (IResource)((IAdaptable)id).getAdapter(IResource.class);
 			resource = r;
 			if(resource != null) {
 				source = resource.getFullPath();
 			}
 		}
 		if(source != null) {
 			resource = ResourcesPlugin.getWorkspace().getRoot().getFile(source);
 		}
 		if(resource == null && parent != null) {
 			return parent.getResource();
 		}
 		return resource;
 	}
 
 	/**
 	 * Returns parent object of kb model.
 	 * @return
 	 */
 	public KbObject getParent() {
 		return parent;
 	}
 	
 	public void setParent(KbObject parent) {
 		this.parent = parent;
 	}
 	
 	protected void adopt(KbObject child) {
 		if(child.getKbProject() != null && child.getKbProject() != getKbProject()) return;
 		child.setParent(this);
 	}
 
 	
 	/**
 	 * Merges loaded object into current object.
 	 * If changes were done returns a list of changes.
 	 * If there are no changes, null is returned, 
 	 * which prevents creating a lot of unnecessary objects. 
 	 * @param f
 	 * @return list of changes
 	 */
 	public List<Change> merge(KbObject s) {
 		KbObject o = s;
 		source = o.source;
 		id = o.id;
 		resource = o.resource;
 		//If there are no changes, null is returned, 
 		//which prevents creating a lot of unnecessary objects.
 		//Subclasses and clients must check returned 
 		//value for null, before using it.		
 		o.attributesInfo = new HashMap<String, IValueInfo>();
 		o.attributesInfo.putAll(attributesInfo);
 		return null;
 	}
 	
 	public KbObject clone() throws CloneNotSupportedException {
 		KbObject c = (KbObject)super.clone();
 		c.parent = null;
 		//do not copy parent
 		return c;
 	}
 	
 	//Serializing to XML
 	
 	public String getXMLName() {
 		return "object"; //$NON-NLS-1$
 	}
 	
 	public String getXMLClass() {
 		return null;
 	}
 	
 	public Element toXML(Element parent, Properties context) {
 		Element element = XMLUtilities.createElement(parent, getXMLName());
 		if(getXMLClass() != null) {
 			element.setAttribute(XMLStoreConstants.ATTR_CLASS, getXMLClass());
 		}
 		if(source != null && !source.equals(context.get(XMLStoreConstants.ATTR_PATH))) {
 			element.setAttribute(XMLStoreConstants.ATTR_PATH, source.toString());
 		}
 		if(id != null) {
 			if(id instanceof String) {
 				Element eid = XMLUtilities.createElement(element, XMLStoreConstants.TAG_ID);
 				eid.setAttribute(XMLStoreConstants.ATTR_CLASS, XMLStoreConstants.CLS_STRING);
 				eid.setAttribute(XMLStoreConstants.ATTR_VALUE, id.toString());
 			} else if(id instanceof XModelObject) {
 				XModelObject o = (XModelObject)id;
 				XMLStoreHelper.saveModelObject(element, o, XMLStoreConstants.TAG_ID, context);
 			} else {
 				//consider other kinds of id
 			}
 		}
 		XModelObject old = pushModelObject(context);
 		saveAttributesInfo(element, context);
 		popModelObject(context, old);
 		return element;
 	}
 
 	protected void saveAttributesInfo(Element element, Properties context) {
 		XMLStoreHelper.saveMap(element, attributesInfo, "attributes", context); //$NON-NLS-1$
 	}
 
 	public void loadXML(Element element, Properties context) {
 		String s = element.getAttribute(XMLStoreConstants.ATTR_PATH);
 		if(s != null && s.length() > 0) {
 			source = new Path(s);
 		} else {
 			source = (IPath)context.get(XMLStoreConstants.ATTR_PATH);
 		}
 		Element e_id = XMLUtilities.getUniqueChild(element, XMLStoreConstants.TAG_ID);
 		if(e_id != null) {
 			String cls = e_id.getAttribute(XMLStoreConstants.ATTR_CLASS);
 			if(XMLStoreConstants.CLS_STRING.equals(cls)) {
 				id = e_id.getAttribute(XMLStoreConstants.ATTR_VALUE);
 			} else if(XMLStoreConstants.CLS_MODEL_OBJECT.equals(cls)) {
 				id = XMLStoreHelper.loadModelObject(e_id, context);
 			} else {
 				//consider other kinds of id
 			}
 		}
 		XModelObject old = pushModelObject(context);
 		loadAttributesInfo(element, context);
 		popModelObject(context, old);
 	}
 
 	protected void loadAttributesInfo(Element element, Properties context) {
 		XMLStoreHelper.loadMap(element, attributesInfo, "attributes", context); //$NON-NLS-1$
 	}
 
 	protected XModelObject pushModelObject(Properties context) {
 		XModelObject old = (XModelObject)context.get(XMLStoreConstants.KEY_MODEL_OBJECT);
 		
 		if(id instanceof XModelObject) {
 			context.put(XMLStoreConstants.KEY_MODEL_OBJECT, id);
 		}
 
 		return old;
 	}
 	
 	protected void popModelObject(Properties context, XModelObject old) {
 		if(old != null) {
 			context.put(XMLStoreConstants.KEY_MODEL_OBJECT, old);
 		} else {
 			context.remove(XMLStoreConstants.KEY_MODEL_OBJECT);
 		}
 	}
 	
 	protected boolean stringsEqual(String s1, String s2) {
 		return s1 == null ? s2 == null : s1.equals(s2);
 	}
 
 	public Object getAdapter(Class cls) {
 		return null;
 	}
 }
