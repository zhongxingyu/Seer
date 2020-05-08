 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.webapp.model;
 
import java.util.Set;

 import org.w3c.dom.*;
 
 import org.jboss.tools.common.meta.*;
 import org.jboss.tools.common.model.*;
 import org.jboss.tools.common.model.impl.XModelObjectImpl;
 import org.jboss.tools.common.model.loaders.impl.SimpleWebFileLoader;
 import org.jboss.tools.common.model.util.*;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.jboss.tools.jst.web.model.helpers.WebAppHelper;
 
 public class FileWebAppLoader extends SimpleWebFileLoader {
 
     protected XModelObjectLoaderUtil createUtil() {
     	FWLoaderUtil util = new FWLoaderUtil();
     	if(!isCheckingDTD()) util.schema = true;
         return util;
     }
 
 }
 
 class FWLoaderUtil extends XModelObjectLoaderUtil {
 	boolean schema = false;
 
    protected Set<String> getAllowedChildren(XModelEntity entity) {
    	Set<String> children = super.getAllowedChildren(entity);
    	if("WebAppResourceCollection".equals(entity.getName())) {
    		children.add("url-pattern");
    		children.add("http-method");
    	} else if("WebAppServiceRef".equals(entity.getName())) {
    		children.add("port-component-ref");
    		children.add("handler");
    	} else if("WebAppFilterMapping24".equals(entity.getName())) {
    		children.add("dispatcher");
    	} else if(entity.getName().startsWith("FileWebApp")) {
    		children.add("distributable");
    	}    	
    	return children;
    }

     protected boolean isSaveable(XModelEntity entity, String n, String v, String dv) {
         if("load-on-startup".equals(n)) return false;
 		if(v == null) return false;
 		if(v.length() == 0 || v.equals(dv)) {
 			XAttribute attr = entity.getAttribute(n);
 			return (attr != null && "always".equals(attr.getProperty("save")));
 		}
 		return super.isSaveable(entity, n, v, dv);
     }
 
     public String getAttribute(Element element, String xmlname) {
         if("distributable".equals(xmlname))
           return (XMLUtil.getUniqueChild(element, "distributable") != null) ? "yes" : "no";
         if("role-names".equals(xmlname))
           return loadArray(element, "role-name");
         if("url-patterns".equals(xmlname))
           return loadArray(element, "url-pattern");
         if("http-methods".equals(xmlname))
           return loadArray(element, "http-method");
 		if("handlers".equals(xmlname))
 		  return loadArray(element, "handler");
 		if("port-component-refs".equals(xmlname))
 		  return loadArray(element, "port-component-ref");
 		if("dispatchers".equals(xmlname))
 		  return loadArray(element, "dispatcher");
         return super.getAttribute(element, xmlname);
     }
 
     public void saveAttributes(Element element, XModelObject o) {
         super.saveAttributes(element, o);
 		String entity = o.getModelEntity().getName();
 		if("WebAppErrorPage".equals(entity)) {
 			eitherOr(element, WebAppConstants.ERROR_CODE, WebAppConstants.EXCEPTION_TYPE);
 		} else if(WebAppHelper.FILTER_ENTITY.equals(entity) || entity.startsWith(WebAppHelper.FILTER_MAPPING_ENTITY)) {
 			eitherOr(element, WebAppConstants.URL_PATTERN, WebAppConstants.SERVLET_NAME);
 		} else if(WebAppHelper.SERVLET_ENTITY.equals(entity)) {
 			eitherOr(element, WebAppConstants.SERVLET_CLASS, WebAppConstants.JSP_FILE);
 		}
 		if(schema) {
 			if(o.getModelEntity().getAttribute("description") != null) {
 				makeChildrenFirst(element, new String[]{"description", "display-name", "icon"});
 			}
 			if("WebAppEnvEntry".equals(o.getModelEntity().getName())) {
 				Element e = XMLUtilities.getUniqueChild(element, "env-entry-value");
 				if(e != null) {
 					element.removeChild(e);
 					element.appendChild(e);
 				}
 			}
 		}
     }
 
     public void saveAttribute(Element element, String xmlname, String value) {
         if("distributable".equals(xmlname)) {
             if("yes".equals(value)) XMLUtil.createElement(element, "distributable");
         } else if("role-names".equals(xmlname)) {
             saveArray(element, "role-name", value);
         } else if("url-patterns".equals(xmlname)) {
             saveArray(element, "url-pattern", value);
         } else if("http-methods".equals(xmlname)) {
             saveArray(element, "http-method", value);
 		} else if("handlers".equals(xmlname)) {
 			saveArray(element, "handler", value);
 		} else if("port-component-refs".equals(xmlname)) {
 			saveArray(element, "port-component-ref", value);
 		} else if("dispatchers".equals(xmlname)) {
 			saveArray(element, "dispatcher", value);
         } else {
             super.saveAttribute(element, xmlname, value);
         }
     }
 
     public boolean saveChildren(Element element, XModelObject o) {
     	String entity = o.getModelEntity().getName();
     	if(entity.startsWith("FileWebApp")) {
     		return saveWebAppChildren(element, o);
     	} else if(WebAppHelper.FILTER_MAPPING_24_ENTITY.equals(entity)) {
     		
     		return true;
     	} else if(!"WebAppServlet".equals(entity)) {
     		return super.saveChildren(element, o);
     	}
         saveChildren(element, o, "WebAppInitParam");
         String l = o.getAttributeValue("load-on-startup");
         if(l.length() > 0) saveAttribute(element, "load-on-startup.#text", l);
         saveChildren(element, o, "WebAppRunAs");
         saveChildren(element, o, "WebAppSecurityRoleRef");
         return true;
     }
 
     protected void saveChildren(Element element, XModelObject o, String entity) {
         XModelObject[] os = o.getChildren(entity);
         for (int i = 0; i < os.length; i++) save(element, os[i]);
     }
     
     private boolean saveWebAppChildren(Element element, XModelObject o) {
         XModelObject[] os = o.getChildrenForSave();
         boolean b = true;
         for (int i = 0; i < os.length; i++) {
         	String xmlname = os[i].getModelEntity().getXMLSubPath();
         	if(xmlname == null || xmlname.length() == 0) {
         		if(!super.saveChildren(element, os[i])) b = false;
         	} else {
         		if(!save(element, os[i])) b = false;
         	}
         }
         return b;
     }
     
     public void loadChildren(Element element, XModelObject o) {
     	String entity = o.getModelEntity().getName();
     	if(entity.startsWith("FileWebApp")) {
 			addRequiredChildren(o, true);
     		XModelObject[] os = o.getChildren();
     		for (int i = 0; i < os.length; i++) {
             	String xmlname = os[i].getModelEntity().getXMLSubPath();
             	if(xmlname == null || xmlname.length() == 0) {
             		super.loadChildren(element, os[i]);
             	}
     		}
     		super.loadChildren(element, o);
     	} else {
     		super.loadChildren(element, o);
     	}
     }
     
    	private void makeChildrenFirst(Element element, String[] names) {
     	Element[] es = new Element[names.length + 1];
     	int[] indices = new int[names.length + 1];
     	for (int k = 0; k < indices.length; k++) indices[k] = -1;
    		NodeList l = element.getChildNodes();
    		for (int i = 0; i < l.getLength(); i++) {
     		Node n = l.item(i);
     		if(n.getNodeType() != Node.ELEMENT_NODE) continue;
     		int index = getIndex(n.getNodeName(), names);
     		if(index < 0) index = names.length;
     		if(es[index] == null) {
     			es[index] = (Element)n;
     			for (int k = index - 1; k >= 0; k--) {
     				if(es[k] == null && (indices[k] > index || indices[k] < 0)) {
     					indices[k] = index; 
     				}
     			}
     		}
     	}
    		for (int i = names.length - 1; i >= 0; i--) {
    			if(es[i] == null || indices[i] < 0) continue;
    	    	element.removeChild(es[i]);
    	    	element.insertBefore(es[i], es[indices[i]]);
 			for (int k = i - 1; k >= 0; k--) {
 				if(es[k] != null && indices[k] > indices[i]) indices[k] = indices[i]; 
 			}
    		}
     }
     
     private int getIndex(String name, String[] names) {
     	for (int i = 0; i < names.length; i++) {
     		if(names[i].equals(name)) return i;
     	}
     	return -1;
     }
 
     public boolean save(Element parent, XModelObject o) {
     	if(!needToSave(o)) return true;
     	return super.save(parent, o);
     }
 
     boolean needToSave(XModelObject o) {
     	if(o == null) return false;
     	String entity = o.getModelEntity().getName();
     	if("WebAppSessionConfig".equals(entity) || "WebAppLoginConfig".equals(entity)) {
     		return hasSetAttributes(o); 
     	} else if("WebAppWelcomFileList".equals(entity) || "WebAppLocaleEncodingMappingList".equals(entity)) {
     		return (o.getChildren().length > 0);
     	} else if("WebAppJspConfig".equals(entity)) {
     		return (o.getChildren().length > 0);
     	}
     	return true;
     }
     
     private boolean hasSetAttributes(XModelObject o) {
     	XAttribute[] as = o.getModelEntity().getAttributes();
     	for (int i = 0; i < as.length; i++) {
     		String xml = as[i].getXMLName();
     		// it would be more safe to check isSavable
     		if(xml == null || xml.length() == 0 || "NAME".equals(xml)) continue;
     		String v = o.getAttributeValue(as[i].getName());
     		if(v != null && v.length() > 0) return true;
     	}
     	String finalComment = o.get("#final-comment");
     	if(finalComment != null && finalComment.length() > 0) return true;
     	return false;
     }
 
 }
