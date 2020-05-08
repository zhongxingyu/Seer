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
 package org.jboss.tools.jst.web.project.helpers;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.FileLocator;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.jboss.tools.common.meta.action.impl.handlers.DefaultCreateHandler;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelConstants;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.impl.FileSystemsImpl;
import org.jboss.tools.common.model.filesystems.impl.Libs;
 import org.jboss.tools.common.model.impl.XModelImpl;
 import org.jboss.tools.common.model.util.XMLUtil;
 import org.jboss.tools.common.model.util.XModelObjectLoaderUtil;
 import org.jboss.tools.common.model.util.XModelObjectUtil;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.common.xml.XMLEntityResolver;
 import org.jboss.tools.common.model.project.Watcher;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.model.helpers.WebAppHelper;
 
 /**
  * @author  valera
  */
 public class NewWebProjectHelper {
     
     public NewWebProjectHelper() {}
     
     public void createProject(XModelObject object, Properties p) throws XModelException {
         XModel model = object.getModel();
 		((XModelImpl)model).waitForLoading();
         XModelObject webxml = WebAppHelper.getWebApp(model);
         
         if(webxml == null) throw new XModelException("Cannot find web.xml");
 		String location = ((IFile)webxml.getAdapter(IFile.class)).getLocation().toString();
 
 		XModelObject webinf = model.getByPath("FileSystems/WEB-INF"); //$NON-NLS-1$
 
 		XModelObject fss = model.getByPath("FileSystems"); //$NON-NLS-1$
         fss.setAttributeValue("application name", p.getProperty("name")); //$NON-NLS-1$ //$NON-NLS-2$
 
 		File webInfDir = ((IResource)webinf.getAdapter(IResource.class)).getLocation().toFile();
         Map<String,String> modules = getModules(location);
         Iterator<Map.Entry<String,String>> it = modules.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<String,String> entry = it.next();
             String module = (String)entry.getKey();
             String config = (String)entry.getValue();
             String fileName = config.substring(config.indexOf('/', 1) + 1);
             File configFile = new File(location, fileName);
             if (configFile.isFile() && fileName.toLowerCase().endsWith(".xml")) { //$NON-NLS-1$
             	//starts with a dot
                 File layoutFile = new File(location, "." + fileName.substring(0, fileName.length())+".strutsdia"); //$NON-NLS-1$ //$NON-NLS-2$
   				if (!layoutFile.exists())
                 	FileUtil.writeFile(layoutFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<PROCESS ENTITY=\"StrutsProcess\" MODULE=\""+module+"\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
             }
         }
 		String libName = getLibLocation(model);
         if (libName != null && libName.length() > 0) {
             File libDir = new File(libName);
             libName = (new File(libName).getParentFile().equals(webInfDir))
                 ? XModelConstants.WORKSPACE_REF + "/lib/" : libName.replace('\\', '/')+"/"; //$NON-NLS-1$ //$NON-NLS-2$
             File[] jars = libDir.listFiles(new FileFilter() {
                 public boolean accept(File file) {
                     if (!file.isFile()) return false;
                     String name = file.getName().toLowerCase();
                     return name.endsWith(".jar") || name.endsWith(".zip"); //$NON-NLS-1$ //$NON-NLS-2$
                 }
             });
             if (jars != null) {
                 for (int i = 0; i < jars.length; i++) {
                     String jarName = jars[i].getName();
                     Properties fsProp = new Properties();
                    fsProp.setProperty("name", Libs.LIB_PREFIX + jarName); //$NON-NLS-1$
                     fsProp.setProperty("location", libName+jarName); //$NON-NLS-1$
                     fsProp.setProperty("info", "hidden=yes"); //$NON-NLS-1$ //$NON-NLS-2$
                     XModelObject fsJar = XModelObjectLoaderUtil.createValidObject(model, "FileSystemJar", fsProp); //$NON-NLS-1$
                     if(fss.getChildByPath(fsJar.getPathPart()) == null) {
                     	DefaultCreateHandler.addCreatedObject(fss, fsJar, false, -1);
                     }
                 }
             }
         }
 
         model.save();
         model.update();
         updateOverlapped(model);
 		registerTLDs(model, p);
 		Watcher.getInstance(model).forceUpdate();
 		model.save();
     }
     
     public static String getLibLocation(XModel model) {
         XModelObject lib = model.getByPath("FileSystems/lib"); //$NON-NLS-1$
         if(lib != null) {
 			return ((IResource)lib.getAdapter(IResource.class)).getLocation().toString();
         } else {
         	XModelObject fs = model.getByPath("FileSystems/WEB-INF"); //$NON-NLS-1$
         	return ((IResource)fs.getAdapter(IResource.class)).getLocation().toString() + "/lib"; //$NON-NLS-1$
         }
     }
     
     public static Map<String,String> getTemplates(String version, String templ) {
         Map<String,String> map = new HashMap<String,String>();
         File dir = new File(templ, "struts/"+version); //$NON-NLS-1$
         File[] files = dir.listFiles();
         if (files == null) return map;
         for (int i = 0; i < files.length; i++) {
             File file = files[i];
             String name = file.getName();
             try {
                 if (file.isDirectory()) {
                     map.put(name, file.toURL().toString());
                 } else if (file.isFile()) {
                     String ext = name.substring(name.indexOf('.') + 1).toLowerCase();
                     if ("jar".equals(ext) || "zip".equals(ext)) { //$NON-NLS-1$ //$NON-NLS-2$
                         map.put(name.substring(0, ext.length()-1), file.toURL().toString());
                     }
                 }
             } catch (MalformedURLException e) {
             	WebModelPlugin.getPluginLog().logError(e);
             }
         }
         return map;
     }
 
     public static void updateOverlapped(XModel model) {
         FileSystemsImpl fs = (FileSystemsImpl)model.getByPath("FileSystems"); //$NON-NLS-1$
         if(fs != null) fs.updateOverlapped();
     }
 
     public static Map<String,String> getModules(String location) {
         Map<String,String> modules = new HashMap<String,String>();
         File webXML = new File(location, "web.xml"); //$NON-NLS-1$
         if(!webXML.isFile()) return modules;
         try {
 	        WebAppConfig config = new WebAppConfig(webXML);
 	        Element[] servlets = config.getServletsByClass("org.apache.struts.action.ActionServlet"); //$NON-NLS-1$
 	        for (int i = 0; i < servlets.length; i++) {
 	            Map<String,String> params = config.getInitParamsAsMap(servlets[i]);
 	            Iterator<String> it = params.keySet().iterator();
 	            while (it.hasNext()) {
 	                String name = it.next();
 	                if (!name.startsWith("config/") && !name.equals("config")) continue; //$NON-NLS-1$ //$NON-NLS-2$
 	                String value = params.get(name);
 	                modules.put(name.substring(6), value);
 	            }
 	        }
 	    } catch (FileNotFoundException e) {
 	    	WebModelPlugin.getPluginLog().logError(e);
 	    }
 
         return modules;
     }
     
     private void registerTLDs(XModel model, Properties p) {
     	String tlds = p.getProperty("TLDs"); //$NON-NLS-1$
     	XModelObject webxml = WebAppHelper.getWebApp(model);
     	if(webxml == null || tlds == null) return;
 		String[] ts = XModelObjectUtil.asStringArray(tlds);
 		for (int i = 0; i < ts.length; i++) {
 			XModelObject taglib = XModelObjectLoaderUtil.createValidObject(model, "WebAppTaglib"); //$NON-NLS-1$
 			String path = "/WEB-INF/" + ts[i], uri = path; //$NON-NLS-1$
 			if(uri.endsWith(".tld")) uri = uri.substring(0, uri.length() - 4); //$NON-NLS-1$
 			taglib.setAttributeValue("taglib-uri", uri); //$NON-NLS-1$
 			taglib.setAttributeValue("taglib-location", path); //$NON-NLS-1$
 			if(webxml.getChildByPath(taglib.getPathPart()) != null) continue;
 			WebAppHelper.getJSPConfig(webxml).addChild(taglib);
 			webxml.setModified(true);
 		}    	
     }
     
 }
 
 class WebAppConfig {
 
 	public static final String TAG_SERVLET       = "servlet"; //$NON-NLS-1$
 	public static final String TAG_SERVLET_NAME  = "servlet-name"; //$NON-NLS-1$
 	public static final String TAG_SERVLET_CLASS = "servlet-class"; //$NON-NLS-1$
 	public static final String TAG_INIT_PARAM    = "init-param"; //$NON-NLS-1$
 	public static final String TAG_PARAM_NAME    = "param-name"; //$NON-NLS-1$
 	public static final String TAG_PARAM_VALUE   = "param-value"; //$NON-NLS-1$
     
 	static {
 		try {
 			Class<?> c = WebAppConfig.class;
 			XMLEntityResolver.registerPublicEntity("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN", FileLocator.resolve(c.getResource("/meta/web-app_2_2.dtd")).toString()); //$NON-NLS-1$ //$NON-NLS-2$
 			XMLEntityResolver.registerPublicEntity("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN", FileLocator.resolve(c.getResource("/meta/web-app_2_3.dtd")).toString()); //$NON-NLS-1$ //$NON-NLS-2$
 		} catch (IOException e) {
 			WebModelPlugin.getPluginLog().logError(e);
 		}
 	}
     
 	private Document conf;
 	private Element webApp;
 
 	/** Creates a new instance of WebAppConfig */
 	public WebAppConfig() {
 	}
     
 	public WebAppConfig(File confFile) throws FileNotFoundException {
 		load(confFile);
 	}
     
 	public void load(File confFile) throws FileNotFoundException {
 		conf = XMLUtil.getDocument(new FileReader(confFile));
 		webApp = conf.getDocumentElement();
 	}
 
 	public Element[] getServlets() {
 		return XMLUtil.getChildren(webApp, TAG_SERVLET);
 	}
     
 	public Element getServletByName(String name) {
 		Element[] servlets = getServlets();
 		for (int i = 0; i < servlets.length; i++) {
 			String n = getTextElement(servlets[i], TAG_SERVLET_NAME);
 			if (name.equals(n)) return servlets[i];
 		}
 		return null;
 	}
     
 	public Element[] getServletsByClass(String className) {
 		Element[] servlets = getServlets();
 		List<Element> list = new ArrayList<Element>(servlets.length);
 		for (int i = 0; i < servlets.length; i++) {
 			String n = getTextElement(servlets[i], TAG_SERVLET_CLASS);
 			if (className.equals(n)) {
 				list.add(servlets[i]);
 			}
 		}
 		return list.toArray(new Element[list.size()]);
 	}
     
 	public Element[] getInitParams(Element servlet) {
 		return XMLUtil.getChildren(servlet, TAG_INIT_PARAM);
 	}
     
 	public Map<String,String> getInitParamsAsMap(Element servlet) {
 		Element[] params = getInitParams(servlet);
 		Map<String,String> map = new HashMap<String,String>();
 		for (int i = 0; i < params.length; i++) {
 			String name = getTextElement(params[i], TAG_PARAM_NAME);
 			String value = getTextElement(params[i], TAG_PARAM_VALUE);
 			map.put(name, value);
 		}
 		return map;
 	}
     
 	public String getTextElement(Element parent, String tag) {
 		Element child = XMLUtil.getFirstChild(parent, tag);
 		if (child != null) {
 			Node text = child.getFirstChild();
 			if (text != null) {
 				return text.getNodeValue().trim();
 			}
 		}
 		return null;
 	}
 
 }
