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
 package org.jboss.tools.jst.web.tld;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IResource;
 
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.event.XModelTreeEvent;
 import org.jboss.tools.common.model.event.XModelTreeListener;
 import org.jboss.tools.common.model.filesystems.FileSystemsHelper;
 import org.jboss.tools.common.model.filesystems.impl.FileAnyImpl;
 import org.jboss.tools.common.model.filesystems.impl.FolderImpl;
 import org.jboss.tools.common.model.filesystems.impl.JarSystemImpl;
 import org.jboss.tools.common.model.impl.XModelImpl;
 import org.jboss.tools.common.model.util.XModelObjectUtil;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.model.helpers.WebAppHelper;
 import org.jboss.tools.jst.web.project.WebProject;
 
 public class TaglibMapping implements ITaglibMapping {    
     private Map<String,String> taglibs = new HashMap<String,String>();
 	private Map<String,XModelObject> taglibObjects = new HashMap<String,XModelObject>();
 	private Map<String,XModelObject> taglibObjectsCopy = null;
 	private Properties resolvedURIs = new Properties();
 	private Properties declaredURIs = new Properties();
     private XModel model;
     private long timeStamp = -1;
     private long jarsTimeStamp = -1;
 	TaglibModelTreeListener listener = null;
 	boolean isLoading = false;
     
     public TaglibMapping(XModel model) {
         this.model = model;
         model.addModelTreeListener(listener = new TaglibModelTreeListener());
     }
     
     synchronized public void revalidate(XModelObject webxml) {
         if (webxml == null || isLoading) return;
         model = webxml.getModel();
         long jts = getJarsTimeStamp();
         if (timeStamp == webxml.getTimeStamp() && jts == jarsTimeStamp) return;
         isLoading = true;
         timeStamp = webxml.getTimeStamp();
         jarsTimeStamp = jts;
         if (model == null) model = webxml.getModel();
         taglibs.clear();
         taglibObjects.clear();
         resolvedURIs.clear();
         declaredURIs.clear();
         WebProject webprj = WebProject.getInstance(model);
         String base = webprj.getWebInfLocation() + "/web.xml";
         if (webxml.getChildren().length > 0) {
             XModelObject[] sz = WebAppHelper.getTaglibs(webxml);
             for (int i = 0; i < sz.length; i++) {
                 String uri = sz[i].getAttributeValue("taglib-uri");
                 String location = sz[i].getAttributeValue("taglib-location");
                 taglibs.put(uri, webprj.getAbsoluteLocation(location, base));
                 XModelObject taglibObject = XModelImpl.getByRelativePath(model, location);
                 if(taglibObject != null) {
                 	String resolvedURI = taglibObject.getAttributeValue("uri");
                 	if(resolvedURI != null) {
                 		resolvedURIs.setProperty(uri, resolvedURI);
                 		declaredURIs.setProperty(resolvedURI, uri);
                 	}
                 }
             }
         }
 		loadTldsInWebInf();
 		findTldsInJars();
 		taglibObjectsCopy = null;
 		isLoading = false;
     }
     
     public Map<String,XModelObject> getTaglibObjects() {
     	Map<String,XModelObject> result = taglibObjectsCopy;
     	if(result == null) {
     		synchronized (this) {
     			taglibObjectsCopy = new HashMap<String,XModelObject>();
     			taglibObjectsCopy.putAll(taglibObjects);
     			result = taglibObjectsCopy;
     		}
     	}
     	return result;
     }
     
     /**
      * Return uri declared in web.xml by uri of tag library 
      */    
     public String getDeclaredURI(String uri) {
     	if(declaredURIs.containsValue(uri)) return uri;
     	return declaredURIs.getProperty(uri);
     }
 
     public String resolveURI(String uri) {
     	String resolvedURI = resolvedURIs.getProperty(uri);
     	return resolvedURI == null ? uri : resolvedURI;
     }
 
     public XModelObject getTaglibObject(String uri) {
     	uri = resolveURI(uri);
     	XModelObject taglib = taglibObjects.get(uri);
     	if(taglib != null) return taglib;
         String location = (String)taglibs.get(uri);
         return (model == null || location == null) ? null
             : XModelImpl.getByRelativePath(model, location);
     }
 
     public String getTaglibPath(String uri) {
         return getTaglibPath(uri, null);
     }
 
     synchronized public String getTaglibPath(String uri, String base) {
         WebProject webprj = WebProject.getInstance(model);
         if (webprj.getWebRootLocation() == null || timeStamp == -1) {
             // this is project without StrutsNature
             if (base != null) {
                 // let's try to find webroot
                 File file = new File(base);
                 while (file != null && file.exists()) {
                     File webInf = new File(file, "WEB-INF");
                     if (webInf.exists() && webInf.isDirectory()) {
                         webprj.setWebRootLocation(file.getAbsolutePath());
                         revalidate(WebAppHelper.getWebApp(model));
                         break;
                     }
                     file = file.getParentFile();
                 }
             }
             if (webprj.getWebRootLocation() == null) return null;
         }
         if (uri.startsWith("urn:jsptld:")) {
             uri = uri.substring(11);
         } else if (uri.startsWith("urn:jsptagdir:")) {
             uri = uri.substring(14);
         }
         String location = (String)taglibs.get(uri);
         if (location == null) {
             // fallback to direct path to tld
             location = webprj.getAbsoluteLocation(uri, base);
             // Don't do this. This mapping is local for JSP.
             //taglibs.put(uri, location);
         }
         return location;
     }
 
 	private void loadTldsInWebInf() {
 		XModelObject webinf = (model == null) ? null : FileSystemsHelper.getWebInf(model);
 		if(webinf == null) return;
 		IResource r = (IResource)webinf.getAdapter(IResource.class);				
 		String url = null;
 		try {
 			if(r != null && r.getLocation() != null) {
 				url = r.getLocation().toFile().toURL().toString();
 			}
 		} catch (Exception e) {
 			WebModelPlugin.getPluginLog().logError(e);
 		}
 		if(url != null) findTldsInFolder(webinf, url);
 	}
 	
 	private void findTldsInJars() {
     	XModelObject fss = FileSystemsHelper.getFileSystems(model);
     	if(fss == null) return;
 		XModelObject[] fs = fss.getChildren("FileSystemJar");
 		for (int i = 0; i < fs.length; i++) {
 			String n = fs[i].getAttributeValue("name");
 			if(n.startsWith("lib-")) findTldsInJar(fs[i]);
 		}
 	}
 	
 	private long getJarsTimeStamp() {
     	XModelObject fss = FileSystemsHelper.getFileSystems(model);
     	if(fss == null) return -1;
     	long n = 0;
 		XModelObject[] fs = fss.getChildren("FileSystemJar");
 		for (int i = 0; i < fs.length; i++) {
 			n += fs[i].getTimeStamp();
 		}
 		return n;
 	}
 
 	private void findTldsInJar(XModelObject jar) {
 		XModelObject metainf = jar.getChildByPath("META-INF");
 		if(metainf == null) return;
 		String location = null;
 		if(jar instanceof JarSystemImpl) {
 			location = ((JarSystemImpl)jar).getTempLocation();
 		}
 		if(location == null) location = XModelObjectUtil.getExpandedValue(jar, "location", null);
 		String url = null;
 		try {
 			url = "jar:" + new File(location).toURL().toString() + "!/META-INF/";
 		} catch (Exception e) {
 			WebModelPlugin.getPluginLog().logError("TaglibMapping:findTldsInJar:" + e.getMessage(), e);
 			return;
 		}		
 		if(metainf != null) findTldsInFolder(metainf, url);
 	}
 
 	private void findTldsInFolder(XModelObject folder, String base) {
 		XModelObject[] cs = folder.getChildren();
 		for (int i = 0; i < cs.length; i++) {
 			if(cs[i].getFileType() == XModelObject.FOLDER) {
 				findTldsInFolder(cs[i], base + cs[i].getAttributeValue("name") + "/");
 			} else if(cs[i].getFileType() == XModelObject.FILE) {
 				String entity = cs[i].getModelEntity().getName();
 				if(!entity.startsWith("FileTLD")) continue;
 				String uri = cs[i].getAttributeValue("uri");
 				String location = base + FileAnyImpl.toFileName(cs[i]);
 				if(folder instanceof FolderImpl) {
 					String path = WebProject.getInstance(cs[i].getModel()).getPathInWebRoot(cs[i]);
 					if(path != null) resolvedURIs.put(path, uri);
 				}
 				addLocation(uri, location);
 				taglibObjects.put(uri, cs[i]);
 			}
 		}		
 	}
 
     public void addLocation(String uri, String location) {
         if (taglibs.get(uri) != null) {
         } else {
             taglibs.put(uri, location);
         }
     }
     
     public void invalidate() {
     	if(timeStamp == -1 || isLoading) return;
     	timeStamp = -1;
     	revalidate(WebAppHelper.getWebApp(model));
     }
     
     class TaglibModelTreeListener implements XModelTreeListener {
 
 		public void nodeChanged(XModelTreeEvent event) {}
 
 		public void structureChanged(XModelTreeEvent event) {
 			if(timeStamp == -1 || isLoading) return;
 			if(event.kind() == XModelTreeEvent.CHILD_ADDED) {
 				XModelObject c = (XModelObject)event.getInfo();
 				String extension = c.getAttributeValue("extension");
 				if("tld".equals(extension) || "jar".equals(extension)) {
 					invalidate();
 				} else if(c.getModelEntity().getName().equals("FileSystemJar")) {
 					invalidate();
 				}
 			} else if(event.kind() == XModelTreeEvent.CHILD_REMOVED) {
 				String path = event.getInfo().toString();
 				if(path.endsWith(".tld")||path.endsWith(".jar")) invalidate();
 			}			
 		}    	
     }
 
 }
