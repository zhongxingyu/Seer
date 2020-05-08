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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 
 import javax.xml.parsers.DocumentBuilder;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.CMDocumentFactoryTLD;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDDocument;
 import org.eclipse.jst.jsp.core.taglib.ITaglibRecord;
 import org.eclipse.jst.jsp.core.taglib.TaglibIndex;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.editors.palette.IEntryChangeListener;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteConstants;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemManager;
 import org.eclipse.jst.pagedesigner.editors.palette.TagToolPaletteEntry;
 import org.eclipse.jst.pagedesigner.utils.XMLUtil;
 import org.eclipse.wst.html.core.internal.contentmodel.HTMLCMDocumentFactory;
 import org.eclipse.wst.xml.core.internal.provisional.contentmodel.CMDocType;
 import org.osgi.framework.Bundle;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * TODO: C.B.: parts of this class need a re-write.  This is very complex.
  *  
  * @author mengbo
  */
 public class PaletteItemManager implements IPaletteItemManager,
 		IPaletteConstants {
 	
 	private static Logger _log = PDPlugin.getLogger(PaletteItemManager.class);
 	private static Map _managers = new HashMap();
 	private List _categories = new ArrayList();
 	// if _curProject is null, then is the default manager.
 	private IProject _curProject = null;
 //	private Map _paletteEntryMap = new HashMap();
 	private String _filename;
 	protected IEntryChangeListener[] _listeners;
 
 	// the default manager is for those _curProject == null
 	private static PaletteItemManager _defaultManager = null;
 
 	private static PaletteItemManager _currentInstance;
 
 	private boolean isUpdateNeeded;
 	private Timer _refreshTimer;
 
 //	private IPath classpath, webinf;
 
 
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
 		_currentInstance = manager;
 		return manager;
 	}
 	
 	public static synchronized PaletteItemManager getCurrentInstance(){
 		return _currentInstance != null ? _currentInstance : null;
 	}
 
 	/**
 	 * 
 	 */
 	public void dispose() {
 		
 	}
 
 	private IProject getCurProject() {
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
 
 //		classpath = _curProject.getFullPath().append(".classpath");
 //		IFolder webroot = WebrootUtil.getWebContentFolder(_curProject);
 //		webinf = webroot.getFolder(IFileFolderConstants.FOLDER_WEBINF)
 //				.getFullPath();
 
 		init();
 //		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 //				getResourceTracker(), IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#getAllCategories()
 	 */
 	public List getAllCategories() {
 		if (_categories == null) {
 			_categories = new ArrayList();
 		}
 		return _categories;
 	}
 
 	protected synchronized void init() {
 		getAllCategories().clear();
 		initFromProject(_curProject);
 
 		loadUserCustomizations();
 	}
 
 	public void reset() {
 		init();
 
 		fireModelChanged(null, null);
 	}
 
 	private void initFromProject(IProject project) {
 		registerHTMLCategory();
 		registerJSPCategory();
 		registerTldFromClasspath(project);
 	}
 
 	private void registerHTMLCategory() {
 		PaletteHelper.configPaletteItemsByTLD(this, getCurProject(), HTMLCMDocumentFactory
 				.getCMDocument(CMDocType.HTML_DOC_TYPE));
 	}
 
 	private void registerJSPCategory() {
 		PaletteHelper.configPaletteItemsByTLD(this, getCurProject(), HTMLCMDocumentFactory
 				.getCMDocument(CMDocType.JSP11_DOC_TYPE));		
 	}
 
 	/**
 	 * Search Classpath entry list to find if the entry is jar libraray and the
 	 * libray have the tld descriptor, if have ,build a palette category mapping
 	 * the tld descriptor.
 	 * 
 	 * @param project
 	 */
 	private void registerTldFromClasspath(IProject project) {
 		ITaglibRecord[] tldrecs = TaglibIndex.getAvailableTaglibRecords(project.getFullPath());
 		CMDocumentFactoryTLD factory = new CMDocumentFactoryTLD();
 		for (int i=0;i<tldrecs.length;i++){
 //			System.out.println("TLD: "+tldrecs[i].getDescriptor().getURI());
 			TLDDocument doc = (TLDDocument)factory.createCMDocument(tldrecs[i]);
 			PaletteHelper.configPaletteItemsByTLD(this, getCurProject(), doc);			
 		}
 	}
 
 	/**
 	 * Load user customizations
 	 */
 	protected void loadUserCustomizations() {
 		//FIX ME
 //		loadPaletteItemState();
 		
 	}
 
 	/**
 	 * @param id (most likely the uri)
 	 * @param label 
 	 * @return TaglibPaletteDrawer
 	 */
 	public TaglibPaletteDrawer findOrCreateCategory(String id, String label) {
 		TaglibPaletteDrawer category;
 		for (Iterator iter = getAllCategories().iterator(); iter.hasNext();) {
 			category = (TaglibPaletteDrawer) iter.next();
 			if (id.equals(category.getId())) {
 				return category;
 			}
 		}
 		category = createTaglibPaletteDrawer(id, label);
 		return category;
 	}
 
 	/**
 	 * @param uri
 	 * @return TaglibPaletteDrawer
 	 */
 	public TaglibPaletteDrawer findCategoryByURI(String uri) {
 		TaglibPaletteDrawer category;
 		for (Iterator iter = getAllCategories().iterator(); iter.hasNext();) {
 			category = (TaglibPaletteDrawer) iter.next();
 			if (uri.equals(category.getURI())) {
 				return category;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#createCategory(java.lang.String)
 	 */
 	public TaglibPaletteDrawer createTaglibPaletteDrawer(String uri, String label) {
 		TaglibPaletteDrawer r = new TaglibPaletteDrawer(uri, label);
 		getAllCategories().add(r);
 		return r;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.pagedesigner.editors.palette.IPaletteItemManager#getCategoryByURI(java.lang.String)
 	 */
 	public TaglibPaletteDrawer getTaglibPalletteDrawer(String uri) {
 		for (Iterator iter = getAllCategories().iterator(); iter.hasNext();) {
 			TaglibPaletteDrawer cat = (TaglibPaletteDrawer) iter.next();
 			if (uri.equals(cat.getId())) {
 				return cat;
 			}
 		}
 		return null;
 	}
 
 
 
 	private String getCustomizationFilename() {
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
 	 * FIX ME
 	 * Save palette item state
 	 */
 	public void save() {
 		Document document = XMLUtil.getDocumentBuilder().getDOMImplementation()
 				.createDocument(null, IPaletteConstants.ROOT, null);
 		try {
 			FileOutputStream ostream = null;
 			String defaultfilename = getCustomizationFilename();
 			int index = defaultfilename.lastIndexOf(File.separator);
 			String foldername = defaultfilename.substring(0, index); //$NON-NLS-1$
 			File folder = new File(foldername);
 			if (folder != null && !folder.exists()) {
 				folder.mkdir();
 			}
 			ostream = new FileOutputStream(getCustomizationFilename());
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
 
 			for (Iterator iter = getAllCategories().iterator(); iter.hasNext();) {
 				TaglibPaletteDrawer category = (TaglibPaletteDrawer) iter
 						.next();
 				PaletteEntry entry = (TaglibPaletteDrawer)category;//.getPaletteEntry();
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
 //					if (entry.getSmallIcon() != null
 //							&& entry.getSmallIcon().toString() != null) {
 //						categoryElement.setAttribute(SMALLICON, entry
 //								.getSmallIcon().toString());
 //					}
 //					if (entry.getLargeIcon() != null
 //							&& entry.getLargeIcon().toString() != null) {
 //						categoryElement.setAttribute(LARGEICON, entry
 //								.getLargeIcon().toString());
 //					}
 					if (entry instanceof TaglibPaletteDrawer) {
 						int state = ((TaglibPaletteDrawer) entry).getInitialState();
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
 				List tags = category.getChildren();//getPaletteItems();
 				for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
 					TagToolPaletteEntry tag = (TagToolPaletteEntry) iterator
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
 					if (tag.getSmallIcon() != null) {
 						tagElement.setAttribute(SMALLICON, tag
 								.getSmallIcon().toString());
 					}
 					if (tag.getLargeIcon() != null) {
 						tagElement.setAttribute(LARGEICON, tag
 								.getLargeIcon().toString());
 					}
 //					PaletteEntry tagEntry = tag.getPaletteEntry();
 					if (tag != null) {
 						if (tag.getId() != null) {
 							tagElement.setAttribute(ID, tag.getId());
 						}
 						if (tag.getDescription() != null) {
 							tagElement.setAttribute(SHORTDESC, tag
 									.getDescription());
 						}
 						if (tag.getLabel() != null) {
 							tagElement.setAttribute(LABEL, tag.getLabel());
 						}
 						if (tag.isVisible()) {
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
 	 * FIX ME - not working and call is disabled
 	 * Load the list of categories that aren't to be visible
 	 * 
 	 * @return
 	 */
 	protected void loadPaletteItemState() {
 //		_paletteEntryMap.clear();
 		List newOrderCatList = new ArrayList();
 		Document document = null;
 		try {
 			DocumentBuilder builder = XMLUtil.getDocumentBuilder();
 			if (builder != null) {
 				document = builder.parse(getCustomizationFilename());
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
 
 						TaglibPaletteDrawer cat = findCategoryByURI(categoryElement.getAttribute(IPaletteConstants.ID));//createPaletteCategoryFromElement(categoryElement);
 						if (cat != null){
 								//apply customizations here
 								int initialState = Integer.parseInt(categoryElement.getAttribute(IPaletteConstants.INITIALSTATE));
 								cat.setInitialState(initialState);
 								
 								cat.setDescription(categoryElement.getAttribute(IPaletteConstants.SHORTDESC));
 								cat.setLabel(categoryElement.getAttribute(IPaletteConstants.LABEL));
 								cat.setVisible(Boolean.valueOf(categoryElement.getAttribute(IPaletteConstants.ISVISIBLE)).booleanValue());
 							//_paletteEntryMap.put(cat.getId(), cat);
 	//						TaglibPaletteDrawer newCat = getTaglibPalletteDrawer(cat.getId());
 	//						if (newCat != null) {
 	//							newOrderCatList.add(newCat);
 	//						}
 	
 //							NodeList tagList = categoryElement.getChildNodes();
 //							for (int j = 0, m = tagList.getLength(); j < m; j++) {
 //								Node tagNode = tagList.item(j);
 //								if (tagNode.getNodeType() == Node.ELEMENT_NODE
 //										&& tagNode.getNodeName().equals(
 //												IPaletteConstants.ITEM_TAG)) {
 //									Element tagElement = (Element) tagNode;
 //									TaglibPaletteDrawer tag = createPaletteCategoryFromElement(tagElement);
 //									_paletteEntryMap.put(tag.getId(), tag);
 //								}
 //							}
 						}
 					}
 				}
 //DO SORT HERE!
 				// add left categories(not in state file) to the last
 				for (Iterator iter = getAllCategories().iterator(); iter.hasNext();) {
 					TaglibPaletteDrawer cat = (TaglibPaletteDrawer) iter
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
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemManager#getProject()
 	 */
 	public IProject getProject() {
 		return getCurProject();
 	}
 }
