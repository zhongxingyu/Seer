 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.editors.palette.impl;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.xml.parsers.DocumentBuilder;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.gef.palette.PaletteDrawer;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.jst.j2ee.internal.deployables.J2EEFlexProjDeployable;
 import org.eclipse.jst.jsf.common.ui.IFileFolderConstants;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsf.common.ui.internal.utils.WebrootUtil;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDDocument;
 import org.eclipse.jst.pagedesigner.IJMTConstants;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.editors.palette.IEntryChangeListener;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteConstants;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemCategory;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemManager;
 import org.eclipse.jst.pagedesigner.utils.XMLUtil;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDocumentFactory;
 import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
 import org.eclipse.wst.xml.core.internal.provisional.contentmodel.CMDocType;
 import org.osgi.framework.Bundle;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * @author mengbo
  */
 public class PaletteItemManager implements IPaletteItemManager,
 		IPaletteConstants {
 	private static Logger _log = PDPlugin.getLogger(PaletteItemManager.class);
 
 	private static Map _managers = new HashMap();
 
 //	private static TLDDocument jsfcoreTLD = null;
 //
 //	private static TLDDocument jsfhtmlTLD = null;
 
 	private List _categories = new ArrayList();
 
 	// if _curProject is null, then is the default manager.
 	private IProject _curProject = null;
 
 	private Map _paletteEntryMap = new HashMap();
 
 	private String _filename;
 
 	protected IEntryChangeListener[] _listeners;
 
 	// the default manager is for those _curProject == null
 	private static PaletteItemManager _defaultManager = null;
 
 	/** the resource tracker instance */
 	private ResourceTracker _resourceTracker = null;
 
 	private boolean isUpdateNeeded;
 
 	private IPath classpath, webinf;
 
 	private class ResourceTracker implements IResourceChangeListener,
 			IResourceDeltaVisitor {
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 		 */
 		public void resourceChanged(IResourceChangeEvent event) {
 			IResourceDelta delta = event.getDelta();
 
 			try {
 				if (delta != null)
 					delta.accept(this);
 			} catch (CoreException exception) {
 				// ResourcePageEditor.Error.ResourceChange = Failed in the
 				// resource change.
 				_log
 						.error("ResourcePageEditor.Error.ResourceChange",
 								exception);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 		 */
 		public boolean visit(IResourceDelta delta) {
 			isUpdateNeeded = false;
 			if (needUpdate(delta)) {
 				reset();
 			}
 
 			return false;
 		}
 	}
 
 	/**
 	 * Only update the palette when classpath is changed or the content under
 	 * web-inf is changed
 	 * 
 	 * @param delta
 	 * @return
 	 */
 	private boolean needUpdate(IResourceDelta delta) {
 		if (isUpdateNeeded)
 			return true;
 
 		switch (delta.getKind()) {
 		case IResourceDelta.ADDED:
 			break;
 		case IResourceDelta.REMOVED:
 			break;
 		case IResourceDelta.CHANGED:
 			IPath fullpath = delta.getFullPath();
 			if (classpath.isPrefixOf(fullpath) || (webinf.isPrefixOf(fullpath))) {
 				isUpdateNeeded = true;
 				return true;
 			}
 			break;
 		}
 
 		IResourceDelta[] children = delta.getAffectedChildren();
 		for (int i = 0, n = children.length; i < n; i++) {
 			boolean result = needUpdate(children[i]);
 			if (result) {
 				isUpdateNeeded = true;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the resource tracker instance
 	 * 
 	 * @return - Returns the resource tracker instance
 	 */
 	private ResourceTracker getResourceTracker() {
 		if (null == _resourceTracker) {
 			_resourceTracker = new ResourceTracker();
 
 		}
 
 		return _resourceTracker;
 	}
 
 	public static synchronized PaletteItemManager getInstance(IProject project) {
 		if (project == null) {
 			// sometimes when the editor is editing a file in jar file, may not
 			// be able to
 			// get the project.
 			return getDefaultPaletteItemManager();
 		}
 		PaletteItemManager manager = (PaletteItemManager) _managers
 				.get(project);
 		if (manager == null) {
 			manager = new PaletteItemManager(project);
 			_managers.put(project, manager);
 		}
 		return manager;
 	}
 
 	/**
 	 * 
 	 */
 	public void dispose() {
 		IWorkspace ws = ResourcesPlugin.getWorkspace();
 
 		if (_resourceTracker != null) {
 			ws.removeResourceChangeListener(_resourceTracker);
 		}
 	}
 
 	public IProject getCurProject() {
 		return _curProject;
 	}
 
 	public static synchronized void removePaletteItemManager(
 			PaletteItemManager manager) {
 		manager.dispose();
 		_managers.remove(manager.getCurProject());
 	}
 
 	public static synchronized void clearPaletteItemManager() {
 		_managers.clear();
 	}
 
 	/**
 	 * @return
 	 */
 	private static PaletteItemManager getDefaultPaletteItemManager() {
 		if (_defaultManager == null) {
 			_defaultManager = new PaletteItemManager(null);
 		}
 		return _defaultManager;
 	}
 
 	/**
 	 * 
 	 */
 	private PaletteItemManager(IProject project) {
 		_curProject = project;
 
 		classpath = _curProject.getFullPath().append(".classpath");
 		IFolder webroot = WebrootUtil.getWebContentFolder(_curProject);
 		webinf = webroot.getFolder(IFileFolderConstants.FOLDER_WEBINF)
 				.getFullPath();
 
 		init();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 				getResourceTracker(), IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#getAllCategories()
 	 */
 	public List getAllCategories() {
 		return _categories;
 	}
 
 	protected void init() {
 		if (_categories == null) {
 			_categories = new ArrayList();
 		}
 		_categories.clear();
 		PaletteHelper.readConfigFromPlugin(this);
 		PaletteHelper.loadFromCMDocument(findOrCreateCategory(
 				IJMTConstants.URI_HTML, null), HTMLCMDocumentFactory
 				.getCMDocument(CMDocType.HTML_DOC_TYPE));
 		PaletteHelper.loadFromCMDocument(findOrCreateCategory(
 				IJMTConstants.URI_JSP, null), HTMLCMDocumentFactory
 				.getCMDocument(CMDocType.JSP11_DOC_TYPE));
 		// if (jsfcoreTLD != null)
 		// {
 		// PaletteHelper.configPaletteItemsByTLD(this, jsfcoreTLD);
 		// }
 		// if (jsfhtmlTLD != null)
 		// {
 		// PaletteHelper.configPaletteItemsByTLD(this, jsfhtmlTLD);
 		// }
 
 		// the default system definied category(from plugin) is invisible
 		// except HTML and JSP category
 		for (int i = 0, size = _categories.size(); i < size; i++) {
 			IPaletteItemCategory category = (IPaletteItemCategory) _categories
 					.get(i);
 			category.setVisible(false);
 			if (category.getURI().equals(IJMTConstants.URI_HTML)) {
 				category.setVisible(true);
 			}
 			if (category.getURI().equals(IJMTConstants.URI_JSP)) {
 				category.setVisible(true);
 			}
 		}
 
 		// initForPluginExtension();
 		initFromProject(_curProject);
 		List newCategories = new ArrayList();
 		for (int i = 0, size = _categories.size(); i < size; i++) {
 			IPaletteItemCategory category = (IPaletteItemCategory) _categories
 					.get(i);
 			if (category.isVisible()) {
 				newCategories.add(category);
 			}
 		}
 		_categories.clear();
 		_categories = newCategories;
 		configureState();
 	}
 
 	public void reset() {
 		init();
 		fireModelChanged(null, null);
 	}
 
 	private void initFromProject(IProject project) {
 
 		registerTldFromClasspath(project);
 
 		IFolder webroot = WebrootUtil.getWebContentFolder(project);
 		IFolder webinf = webroot.getFolder(IFileFolderConstants.FOLDER_WEBINF);
 		// search tld file in web-inf folder
 		initFormFolder(webinf);
 	}
 
 	/**
 	 * Search Classpath entry list to find if the entry is jar libraray and the
 	 * libray have the tld descriptor, if have ,build a palette category mapping
 	 * the tld descriptor.
 	 * 
 	 * @param project
 	 */
 	private void registerTldFromClasspath(IProject project) {
 		J2EEFlexProjDeployable jdeploy = new J2EEFlexProjDeployable(project);
 		IPath[] paths = jdeploy.getClasspath();
 		for (int i = 0; i < paths.length; i++) {
 			IPath path = paths[i];
 			String jarFile = path.toOSString();
 			if (jarFile.toLowerCase().endsWith(".jar")) {
 				ZipFile zFile = null;
 				// Need to check if the jar file has a .tld file anywhere under
 				// the
 				// META-INF directory. If there is, add a taglib for the uri
 				// that
 				// is inside the TLD file.
 				try {
 					zFile = new ZipFile(jarFile);
 					ZipEntry[] entries = findTLDEntriesInZip(zFile);
 
 					for (int z = 0; z < entries.length; z++) {
 						ZipEntry entry = entries[z];
 						if (entry != null) {
 							// most entries can be skipped this way
 							String entryName = entry.getName();
 							if (entryName == null
 									|| entryName.indexOf("META-INF") < 0 || !entryName.toLowerCase().endsWith("tld")) //$NON-NLS-1$
 							{
 								continue;
 							}
 							DocumentFactoryTLD factory = new DocumentFactoryTLD();
 							TLDDocument doc = (TLDDocument) factory
 									.buildCMDocumentFromJar(jarFile, entry
 											.getName());
 							PaletteHelper.configPaletteItemsByTLD(this, doc);
 						}
 					}
 					zFile.close();
 				} catch (IOException e) {
                     PDPlugin.getDefault().getLog().log
                         (new Status(IStatus.ERROR, PDPlugin.getPluginId(), 0, "Error accessing zip file", e));
 				}
                 finally
                 {
                     if (zFile != null)
                     {
                         try
                         {
                             zFile.close();
                         }
                         catch (IOException ioe)
                         {
                             PDPlugin.getDefault().getLog().log
                                 (new Status(IStatus.ERROR, PDPlugin.getPluginId(), 0, "Error closing zip file", ioe));
                         }
                     }
                 }
 
 			}
 		}
 	}
 
 	private void initFormFolder(IFolder folder) {
 		// webinf.get
 		IResource[] members;
 		try {
 			members = folder.members();
 			for (int i = 0; i < members.length; i++) {
 				IResource res = members[i];
 				if (res instanceof IFolder) {
 					initFormFolder((IFolder) res);
 				}
 				if ("tld".equalsIgnoreCase(res.getFileExtension())) {
 					TLDDocument doc = (TLDDocument) ContentModelManager
 							.getInstance().createCMDocument(
 									res.getLocation().toOSString(), "tld"); //$NON-NLS-1$
 					PaletteHelper.configPaletteItemsByTLD(this, doc);
 				}
 			}
 		} catch (CoreException e) {
             PDPlugin.getDefault().getLog().log
                 (new Status(IStatus.ERROR, PDPlugin.getPluginId(), 0, "Error initalizing form folder", e));
 		}
 
 	}
 
 	public static ZipEntry[] findTLDEntriesInZip(ZipFile zFile) {
 		Enumeration entries = zFile.entries();
 		ArrayList results = new ArrayList();
 
 		while (entries.hasMoreElements()) {
 			ZipEntry entry = (ZipEntry) entries.nextElement();
 			if (!entry.isDirectory()) {
 				// Look for the first .tld file found in the META-INF directory.
 				results.add(entry);
 			}
 		}
 		return (ZipEntry[]) results.toArray(new ZipEntry[results.size()]);
 	}
 
 	/**
 	 * 
 	 */
     // TODO: dead?
 //	private void initForPluginExtension() {
 //		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 //
 //		IExtensionPoint point = extensionRegistry.getExtensionPoint(
 //				IPaletteConstants.BUNDLE_ID,
 //				IPaletteConstants.EXTENSION_POINT_ID);
 //
 //		IConfigurationElement[] elements = point.getConfigurationElements();
 //		for (int i = 0; i < elements.length; i++) {
 //			try {
 //				IConfigurationElement element = elements[i];
 //				Object obj = element.createExecutableExtension("class"); //$NON-NLS-1$
 //
 //				if (obj instanceof IResourceManager && _curProject != null) {
 //					IResourceManager manager = (IResourceManager) obj;
 //					List resources = manager.getResourceList(_curProject);
 //					for (Iterator iter = resources.iterator(); iter.hasNext();) {
 //						String path = (String) iter.next();
 //						try {
 //							URL url = new URL(path);
 //							url = FileLocator.toFileURL(url);
 //							String filepath = url.toExternalForm();
 //							filepath = filepath.replace('\\', '/');
 //							TLDDocument doc = null;
 //
 //							if (path.indexOf("!") > 0) //$NON-NLS-1$
 //							{
 //
 //								DocumentFactoryTLD factory = new DocumentFactoryTLD();
 //								String jarFile = path.substring(0, path
 //										.indexOf("!")); //$NON-NLS-1$
 //								if (jarFile.startsWith(JARPROTO)) {
 //									jarFile = jarFile.substring(JARPROTO
 //											.length());
 //								}
 //								String contentName = path.substring(path
 //										.indexOf("!") + 1, path.length()); //$NON-NLS-1$
 //								if (contentName.startsWith("/")) {
 //									contentName = contentName.substring(1);
 //								}
 //								// first get resource from project, if not exist
 //								// , from eclipse plugin.
 //								String jarpathofproject = null;
 //								try {
 //									jarpathofproject = jarFile.substring(0,
 //											jarFile.toUpperCase().indexOf(
 //													".JAR") + 4);
 //									jarpathofproject = jarpathofproject
 //											.substring(jarpathofproject
 //													.lastIndexOf("/") + 1);
 //									jarpathofproject = getRelativeProjectFile(
 //											_curProject,
 //											IFileFolderConstants.FOLDER_WEBINF
 //													+ "/"
 //													+ IFileFolderConstants.FOLDER_LIB
 //													+ "/" + jarpathofproject);
 //								} catch (Exception e) {
 //									jarpathofproject = null;
 //								}
 //								if (jarpathofproject != null) {
 //									jarpathofproject = jarpathofproject
 //											.replace('\\', '/');
 //									doc = (TLDDocument) factory
 //											.buildCMDocumentFromJar(
 //													jarpathofproject,
 //													contentName);
 //								} else {
 //									doc = (TLDDocument) factory
 //											.buildCMDocumentFromJar(jarFile,
 //													contentName);
 //								}
 //							} else {
 //								if (filepath.startsWith(FILEPROTO))// if
 //								// filepath
 //								// starts
 //								// with
 //								// "file://"
 //								// will have
 //								// problem
 //								// to be
 //								// paremeter.
 //								{
 //									filepath = filepath.substring(FILEPROTO
 //											.length());
 //								}
 //								String tldpathofproject = null;
 //								try {
 //									tldpathofproject = filepath.substring(0,
 //											filepath.toUpperCase().indexOf(
 //													".TLD") + 4);
 //									tldpathofproject = tldpathofproject
 //											.substring(tldpathofproject
 //													.lastIndexOf("/") + 1);
 //									tldpathofproject = getRelativeProjectFile(
 //											_curProject,
 //											IFileFolderConstants.FOLDER_WEBINF
 //													+ "/"
 //													+ IFileFolderConstants.FOLDER_TAGLIB
 //													+ "/" + tldpathofproject);
 //								} catch (Exception e) {
 //									tldpathofproject = null;
 //								}
 //								if (tldpathofproject != null) {
 //									tldpathofproject = tldpathofproject
 //											.replace('\\', '/');
 //									doc = (TLDDocument) ContentModelManager
 //											.getInstance().createCMDocument(
 //													tldpathofproject, "tld"); //$NON-NLS-1$
 //								} else {
 //									doc = (TLDDocument) ContentModelManager
 //											.getInstance().createCMDocument(
 //													filepath, "tld"); //$NON-NLS-1$
 //								}
 //							}
 //
 //							PaletteHelper.configPaletteItemsByTLD(this, doc);
 //						} catch (MalformedURLException e1) {
 //							_log
 //									.error(
 //											"PaletteItemManager.initForPluginExtension.error.MalformedURLException", e1); //$NON-NLS-1$
 //						}
 //
 //						catch (IOException e2) {
 //							_log
 //									.error(
 //											"PaletteItemManager.initForPluginExtension.error.IOException", e2); //$NON-NLS-1$
 //						}
 //					}
 //				}
 //			} catch (CoreException e) {
 //				_log
 //						.error(
 //								"PaletteItemManager.initForPluginExtension.error.InstantiationException", e); //$NON-NLS-1$
 //			}
 //		}
 //	}
 
 	/**
 	 * 
 	 */
 	private void configureState() {
 		loadPaletteItemState();
 		for (Iterator iter = _categories.iterator(); iter.hasNext();) {
 			IPaletteItemCategory element = (IPaletteItemCategory) iter.next();
 
 			String id = element.getId();
 			IPaletteItemCategory entry = (IPaletteItemCategory) _paletteEntryMap
 					.get(id);
 			if (entry != null) {
 				element.setVisible(entry.isVisible());
 				element.setId(entry.getId());
 				element.setDescription(entry.getDescription());
 				element.setLabel(entry.getLabel());
 				element.setInitialState(entry.getInitialState());
 			}
 			List cList = element.getPaletteItems();
 			for (Iterator iterator = cList.iterator(); iterator.hasNext();) {
 				IPaletteItemDescriptor cItem = (IPaletteItemDescriptor) iterator
 						.next();
 				String cid = cItem.getId();
 				String clabel = cItem.getLabel();
 				if (clabel == null) {
 					clabel = cItem.getTagName();
 				}
 				IPaletteItemCategory cEntry = (IPaletteItemCategory) _paletteEntryMap
 						.get(cid);// cEntry of type IPaletteItemCategory just
 				// IPaletteItemEntry Implement
 				if (cEntry != null) {
 					cItem.setVisible(cEntry.isVisible());
 					cItem.setId(cEntry.getId());
 					cItem.setDescription(cEntry.getDescription());
 					cItem.setLabel(cEntry.getLabel());
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param uri2
 	 * @return
 	 */
 	public IPaletteItemCategory findOrCreateCategory(String uri, String label) {
 		for (Iterator iter = _categories.iterator(); iter.hasNext();) {
 			PaletteItemCategory cate = (PaletteItemCategory) iter.next();
 			if (uri.equals(cate.getURI())) {
 				return cate;
 			}
 		}
 		PaletteItemCategory r = new PaletteItemCategory(uri, label);
 		_categories.add(r);
 		// TODO: fire event?
 		return r;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#createCategory(java.lang.String)
 	 */
 	public IPaletteItemCategory createCategory(String tldURI) {
 		PaletteItemCategory r = new PaletteItemCategory(tldURI, tldURI);
 		_categories.add(r);
 		return r;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#getCategoryByURI(java.lang.String)
 	 */
 	public IPaletteItemCategory getCategoryByURI(String tldURI) {
 		for (Iterator iter = _categories.iterator(); iter.hasNext();) {
 			IPaletteItemCategory cat = (IPaletteItemCategory) iter.next();
 			if (tldURI.equals(cat.getURI())) {
 				return cat;
 			}
 		}
 		return null;
 	}
 
 	public void moveup(IPaletteItemCategory cat) {
 		int i = _categories.indexOf(cat);
 		IPaletteItemCategory upCat = (IPaletteItemCategory) _categories
 				.get(i - 1);
 		movedown(upCat);
 	}
 
 	public void movedown(IPaletteItemCategory cat) {
 		int i = _categories.indexOf(cat);
 		_categories.add(i + 2, cat);
 		_categories.remove(i);
 	}
 
 	public String getFilename() {
 		if (_filename == null || _filename.trim().equals("")) {
 			String name = null;
 			try {
 				Bundle bundle = Platform.getBundle(PDPlugin.getPluginId());
 				name = Platform.getStateLocation(bundle).toString() + FILENAME; //$NON-NLS-1$
 			} catch (Exception e) {
 				name = FILENAME;
 			}
 			name = name.replace('/', File.separatorChar);
 			name = name.replace('\\', File.separatorChar);
 			return name;
 		}
         _filename = _filename.replace('/', File.separatorChar);
         _filename = _filename.replace('\\', File.separatorChar);
         return _filename;
 	}
 
 	public void setFilename(String filename) {
 		_filename = filename;
 	}
 
 	/**
 	 * Save palette item state
 	 */
 	public void save() {
 		Document document = XMLUtil.getDocumentBuilder().getDOMImplementation()
 				.createDocument(null, IPaletteConstants.ROOT, null);
 		try {
 			FileOutputStream ostream = null;
 			String defaultfilename = getFilename();
 			int index = defaultfilename.lastIndexOf(File.separator);
 			String foldername = defaultfilename.substring(0, index); //$NON-NLS-1$
 			File folder = new File(foldername);
 			if (folder != null && !folder.exists()) {
 				folder.mkdir();
 			}
 			ostream = new FileOutputStream(getFilename());
 			Map categoryMap = new HashMap();
 			Element root = document.getDocumentElement();
 			if (root != null) {
 				NodeList clist = root.getChildNodes();
 				for (int i = 0, length = clist.getLength(); i < length; i++) {
 					Node cNode = clist.item(i);
 					NamedNodeMap attrs = cNode.getAttributes();
 					if (attrs != null) {
 						Node attrNode = attrs.getNamedItem(ID);
 						if (attrNode != null) {
 							String value = attrNode.getNodeValue();
 							categoryMap.put(value, cNode);
 						}
 					}
 				}
 			}
 
 			for (Iterator iter = _categories.iterator(); iter.hasNext();) {
 				IPaletteItemCategory category = (IPaletteItemCategory) iter
 						.next();
 				PaletteEntry entry = category.getPaletteEntry();
 				Element categoryElement = document.createElement(CATEGORY_TAG);
 				Node existNode = (Node) categoryMap.get(entry.getId());
 				if (existNode != null) {
 					root.removeChild(existNode);
 				}
 				if (entry != null) {
 					if (entry.getId() != null) {
 						categoryElement.setAttribute(ID, entry.getId());
 					}
 
 					if (entry.getDescription() != null) {
 						categoryElement.setAttribute(SHORTDESC, entry
 								.getDescription());
 					}
 					if (entry.getLabel() != null) {
 						categoryElement.setAttribute(LABEL, entry.getLabel());
 					}
 					if (entry.getSmallIcon() != null
 							&& entry.getSmallIcon().toString() != null) {
 						categoryElement.setAttribute(SMALLICON, entry
 								.getSmallIcon().toString());
 					}
 					if (entry.getLargeIcon() != null
 							&& entry.getLargeIcon().toString() != null) {
 						categoryElement.setAttribute(LARGEICON, entry
 								.getLargeIcon().toString());
 					}
 					if (entry instanceof PaletteDrawer) {
 						int state = ((PaletteDrawer) entry).getInitialState();
 						categoryElement.setAttribute(INITIALSTATE, String
 								.valueOf(state));
 					}
 					if (entry.isVisible()) {
 						categoryElement.setAttribute(ISVISIBLE, Boolean.FALSE
 								.toString());
 					} else {
 						categoryElement.setAttribute(ISVISIBLE, Boolean.TRUE
 								.toString());
 					}
 
 				}
 				List tags = category.getPaletteItems();
 				for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
 					IPaletteItemDescriptor tag = (IPaletteItemDescriptor) iterator
 							.next();
 					Element tagElement = document.createElement(ITEM_TAG);
 					if (tag.getTagName() != null) {
 						tagElement.setAttribute(TAGNAME, tag.getTagName());
 					}
 					if (tag.getLabel() != null) {
 						tagElement.setAttribute(LABEL, tag.getLabel());
 					}
 					if (tag.getDescription() != null) {
 						tagElement
 								.setAttribute(SHORTDESC, tag.getDescription());
 					}
 					if (tag.getSmallIconString() != null) {
 						tagElement.setAttribute(SMALLICON, tag
 								.getSmallIconString());
 					}
 					if (tag.getLargeIconString() != null) {
 						tagElement.setAttribute(LARGEICON, tag
 								.getLargeIconString());
 					}
 					PaletteEntry tagEntry = tag.getPaletteEntry();
 					if (tagEntry != null) {
 						if (tagEntry.getId() != null) {
 							tagElement.setAttribute(ID, tagEntry.getId());
 						}
 						if (tagEntry.getDescription() != null) {
 							tagElement.setAttribute(SHORTDESC, tagEntry
 									.getDescription());
 						}
 						if (tagEntry.getLabel() != null) {
 							tagElement.setAttribute(LABEL, tagEntry.getLabel());
 						}
 						if (tagEntry.isVisible()) {
 							tagElement.setAttribute(ISVISIBLE, Boolean.FALSE
 									.toString());
 						} else {
 							tagElement.setAttribute(ISVISIBLE, Boolean.TRUE
 									.toString());
 						}
 
 					}
 					categoryElement.appendChild(tagElement);
 				}
 				document.getDocumentElement().appendChild(categoryElement);
 			}
 			XMLUtil.serialize(document, ostream);
 			ostream.close();
 		} catch (IOException e) {
 			_log.error("PaletteItemManager.save.error.IOException", e); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Load the list of categories that aren't to be visible
 	 * 
 	 * @return
 	 */
 	protected void loadPaletteItemState() {
 		_paletteEntryMap.clear();
 		List newOrderCatList = new ArrayList();
 		Document document = null;
 		try {
 			DocumentBuilder builder = XMLUtil.getDocumentBuilder();
 			if (builder != null) {
 				document = builder.parse(getFilename());
 			} else {
 				_log
 						.error("PaletteItemManager.loadPaletteItemState.error.getDocumentBuilderFail");// $
 				// //$NON-NLS-1$
 			}
 		} catch (FileNotFoundException e) {
 			// typical of new workspace, don't log it
 			document = null;
 		} catch (IOException e) {
 			// TaglibManager could not load hidden state
 			_log
 					.error(
 							"PaletteItemManager.loadPaletteItemState.error.IOException", e.toString(), e); //$NON-NLS-1$
 		} catch (SAXException e) {
 			// TaglibManager could not load hidden state
 			_log
 					.error(
 							"PaletteItemManager.loadPaletteItemState.error.SAXException", e.toString(), e); //$NON-NLS-1$
 		}
 		if (document != null) {
 			// List names = new ArrayList(0);
 			Element root = document.getDocumentElement();
 			if (root != null) {
 				NodeList catetorylist = root.getChildNodes();
 				for (int i = 0, n = catetorylist.getLength(); i < n; i++) {
 					Node childNode = catetorylist.item(i);
 					if (childNode.getNodeType() == Node.ELEMENT_NODE
 							&& childNode.getNodeName().equals(
 									IPaletteConstants.CATEGORY_TAG)) {
 						Element categoryElement = (Element) childNode;
 
 						IPaletteItemEntry cat = createPaletteCategoryFromElement(categoryElement);
 						_paletteEntryMap.put(cat.getId(), cat);
 						IPaletteItemEntry newCat = getCategoryByURI(cat.getId());
 						if (newCat != null) {
 							newOrderCatList.add(newCat);
 						}
 
 						NodeList tagList = categoryElement.getChildNodes();
 						for (int j = 0, m = tagList.getLength(); j < m; j++) {
 							Node tagNode = tagList.item(j);
 							if (tagNode.getNodeType() == Node.ELEMENT_NODE
 									&& tagNode.getNodeName().equals(
 											IPaletteConstants.ITEM_TAG)) {
 								Element tagElement = (Element) tagNode;
 								IPaletteItemEntry tag = createPaletteCategoryFromElement(tagElement);
 								_paletteEntryMap.put(tag.getId(), tag);
 							}
 						}
 					}
 				}
 
 				// add left categories(not in state file) to the last
 				for (Iterator iter = _categories.iterator(); iter.hasNext();) {
 					IPaletteItemCategory cat = (IPaletteItemCategory) iter
 							.next();
 					if (!newOrderCatList.contains(cat)) {
 						newOrderCatList.add(cat);
 					}
 				}
 
 				if (newOrderCatList.size() > 0) {
 					_categories = newOrderCatList;
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param categoryElement
 	 * @return
 	 */
 	private IPaletteItemEntry createPaletteCategoryFromElement(
 			Element categoryElement) {
 		String id = null;
 		String label = null;
 		String desc = null;
 		String uri = null;
 		boolean isVisible = true;
 		String state = null;
 		if (categoryElement.hasAttribute(IPaletteConstants.ID)) {
 			id = categoryElement.getAttribute(IPaletteConstants.ID);
 		}
 		if (categoryElement.hasAttribute(IPaletteConstants.SHORTDESC)) {
 			desc = categoryElement.getAttribute(IPaletteConstants.SHORTDESC);
 		}
 		if (categoryElement.hasAttribute(IPaletteConstants.LABEL)) {
 			label = categoryElement.getAttribute(IPaletteConstants.LABEL);
 		}
 		if (categoryElement.hasAttribute(IPaletteConstants.URI)) {
 			uri = categoryElement.getAttribute(IPaletteConstants.URI);
 		}
 		if (categoryElement.hasAttribute(IPaletteConstants.INITIALSTATE)) {
 			state = categoryElement
 					.getAttribute(IPaletteConstants.INITIALSTATE);
 		}
 		if (categoryElement.hasAttribute(IPaletteConstants.ISVISIBLE)) {
 			String visible = categoryElement
 					.getAttribute(IPaletteConstants.ISVISIBLE);
 			if ("true".equalsIgnoreCase(visible))//$NON-NLS-1$
 			{
 				isVisible = false;
 			}
 		}
 
 		PaletteItemCategory cat = new PaletteItemCategory(uri, label);
 		cat.setId(id);
 		cat.setDescription(desc);
 		cat.setVisible(isVisible);
 		int initState = PaletteDrawer.INITIAL_STATE_CLOSED;
 		try {
 			initState = Integer.parseInt(state);
 		} catch (Exception e) {
 			// ignore
 		}
 		cat.setInitialState(initState);
 		return cat;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#addEntryChangeListener(com.sybase.stf.jmt.pagedesigner.editors.palette.IEntryChangeListener)
 	 */
 	public void addEntryChangeListener(IEntryChangeListener listener) {
 
 		if (_listeners == null) {
 			_listeners = new IEntryChangeListener[] { listener };
 		} else {
 			IEntryChangeListener[] newListeners = new IEntryChangeListener[_listeners.length + 1];
 			newListeners[0] = listener;
 			System.arraycopy(_listeners, 0, newListeners, 1, _listeners.length);
 			_listeners = newListeners;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#removeEntryChangeListener(com.sybase.stf.jmt.pagedesigner.editors.palette.IEntryChangeListener)
 	 */
 	public void removeEntryChangeListener(IEntryChangeListener listener) {
 		if (_listeners == null) {
 			return;
 		}
 		if (_listeners.length == 1) {
 			_listeners = null;
 		} else {
 			List newListenersList = new ArrayList(Arrays.asList(_listeners));
 			newListenersList.remove(listener);
 			IEntryChangeListener[] newListeners = new IEntryChangeListener[newListenersList
 					.size() - 1];
 			newListeners = (IEntryChangeListener[]) newListenersList
 					.toArray(newListeners);
 			_listeners = newListeners;
 		}
 	}
 
 	/**
 	 * Notify model change event
 	 * 
 	 * @param oldDefinitions
 	 * @param newDefinitions
 	 */
 	private void fireModelChanged(List oldDefinitions, List newDefinitions) {
 		if (_listeners == null) {
 			return;
 		}
 		for (int i = 0; i < _listeners.length; i++) {
 			_listeners[i].modelChanged(oldDefinitions, newDefinitions);
 		}
 	}
 
 	/**
 	 * Return location path of the file which the relative path to web project
 	 * contextroot equal param path.
 	 * 
 	 * @param project
 	 *            web project
 	 * @param path
 	 *            relative path(example: WEB-INF/web.xml)
 	 * @return
 	 */
 	public String getRelativeProjectFile(IProject project, String path) {
 		IVirtualComponent wbModule = ComponentCore.createComponent(_curProject);
 		if (wbModule != null) {
 			IVirtualFile reFile = wbModule.getRootFolder().getFile(path);
 			if (reFile.exists()) {
 				return reFile.getUnderlyingFile().getLocation().toOSString()
 						+ "/";
 			}
 		}
 		return null;
 	}
 }
