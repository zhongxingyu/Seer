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
 package org.jboss.tools.jst.jsp.support.kb;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.xml.core.internal.document.NodeContainer;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.jboss.tools.common.kb.KbConnector;
 import org.jboss.tools.common.kb.KbConnectorFactory;
 import org.jboss.tools.common.kb.KbConnectorType;
 import org.jboss.tools.common.kb.KbException;
 import org.jboss.tools.common.kb.KbResource;
 import org.jboss.tools.common.kb.TagDescriptor;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.web.tld.TaglibData;
 import org.jboss.tools.jst.web.tld.VpeTaglibListener;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.jboss.tools.jst.web.tld.VpeTaglibManagerProvider;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Jeremy
  */
 public class WTPTextJspKbConnector implements KbConnector, VpeTaglibListener {
 	private KbConnector kbConnector;
 	private IDocument fDocument;
 	private IEditorInput fEditorInput;
 	private VpeTaglibManagerProvider taglibManagerProvider;
 
 	protected IStructuredModel model;
 	private Document dom;
 	protected int timerTaskCount = 0;
 //	protected boolean isGrammarInferenceEnabled;
 	protected static Timer timer = new Timer(true);
 
 	// Dinamic resources
 	
  	private WTPKbdBundleNameResource fBundleNameResourceRegistered = null;
 	private WTPKbdBeanPropertyResource fBeanPropertyResourceRegistered = null;
 	private WTPKbdBundlePropertyResource fBundlePropertyResourceRegistered = null;
 	private WTPKbdActionResource fActionResourceRegistered = null;
 	private WTPKbdBeanMethodResource fBeanMethodResourceRegistered = null;
 	private WTPKbImageFileResource fImageFileResourced = null;
 	private WTPKbdManagedBeanNameResource fManagedBeanNameResourceRegistered = null;
 	private WTPKbJsfValuesResource fJSFValuesResource = null;
 	WTPKbdTaglibResource fTaglibResource = null;
 	private MyDocumentAdapter documentAdapter;
 
 	public WTPTextJspKbConnector(IEditorInput editorInput, IDocument document, IStructuredModel model) {
 		try {
 			this.fDocument = document;
 			this.fEditorInput = editorInput;
 			this.model = model;
 
 			this.dom = (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
 
 			if (dom != null) {
 				documentAdapter = new MyDocumentAdapter(dom);
 			}
 			kbConnector = KbConnectorFactory.getIntstance().createConnector(KbConnectorType.JSP_WTP_KB_CONNECTOR, document);
 			WTPKbdBundleNameResource bundleNameResource = new WTPKbdBundleNameResource(fEditorInput, this);
 			if (bundleNameResource.isReadyToUse()) {
 				fBundleNameResourceRegistered = bundleNameResource;
 				registerResource(fBundleNameResourceRegistered);
 			}
 			WTPKbdBeanPropertyResource beanPropertyResource = new WTPKbdBeanPropertyResource(fEditorInput, this);
 			if (beanPropertyResource.isReadyToUse()) {
 				fBeanPropertyResourceRegistered = beanPropertyResource;
 				registerResource(fBeanPropertyResourceRegistered);
 			}
 			WTPKbdBundlePropertyResource bundlePropertyResource = new WTPKbdBundlePropertyResource(fEditorInput, this);
 			if (bundlePropertyResource.isReadyToUse()) {
 				fBundlePropertyResourceRegistered = bundlePropertyResource;
 				registerResource(fBundlePropertyResourceRegistered);
 			}
 			WTPKbdActionResource actionResource = new WTPKbdActionResource(fEditorInput, this);
 			if (actionResource.isReadyToUse()) {
 				fActionResourceRegistered = actionResource;
 				registerResource(fActionResourceRegistered);
 			}
 			WTPKbdBeanMethodResource beanMethodResource = new WTPKbdBeanMethodResource(fEditorInput, this);
 			if (beanMethodResource.isReadyToUse()) {
 				fBeanMethodResourceRegistered = beanMethodResource;
 				registerResource(fBeanMethodResourceRegistered);
 			}
 			WTPKbImageFileResource imageFileResource = new WTPKbImageFileResource(fEditorInput);
 			if(imageFileResource.isReadyToUse()) {
 				fImageFileResourced = imageFileResource;
 				registerResource(fImageFileResourced);
 			}
 			WTPKbdManagedBeanNameResource managedBeanNameResource = new WTPKbdManagedBeanNameResource(fEditorInput, this);
 			if(managedBeanNameResource.isReadyToUse()) {
 				fManagedBeanNameResourceRegistered = managedBeanNameResource;
 				registerResource(fManagedBeanNameResourceRegistered);
 			}
 			WTPKbJsfValuesResource resource = new WTPKbJsfValuesResource(editorInput, this);
 			if(resource.isReadyToUse()) {
 				fJSFValuesResource = resource;
 				registerResource(fJSFValuesResource);
 			}
 			WTPKbdTaglibResource taglibResource = new WTPKbdTaglibResource(editorInput, this);
 			if(taglibResource.isReadyToUse()) {
 				fTaglibResource = taglibResource;
 				registerResource(fTaglibResource);
 			}
 		} catch (KbException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	public void setTaglibManagerProvider(VpeTaglibManagerProvider taglibManagerProvider) {
 		this.taglibManagerProvider = taglibManagerProvider;
 	}
 
 
 	/**
 	 * @see org.jboss.tools.common.kb.KbConnector#queryTagInformation(java.lang.String)
 	 */
 	public TagDescriptor getTagInformation(String query) throws KbException {
 		return kbConnector.getTagInformation(query);
 	}
 
 	/**
 	 * @see org.jboss.tools.common.kb.KbConnector#getProposals(java.lang.String)
 	 */
 	public Collection getProposals(String query) throws KbException {
 		return kbConnector.getProposals(query);
 	}
 
 	/**
 	 * @see org.jboss.tools.common.kb.KbConnector#registerResource(org.jboss.tools.common.kb.KbResource)
 	 */
 	public boolean registerResource(KbResource resource) {
 		return kbConnector.registerResource(resource);
 	}
 	
 	public KbConnector getConnector() {
 		return kbConnector;
 	}
 
 	/**
 	 * @see org.jboss.tools.common.kb.KbConnector#unregisterResource(org.jboss.tools.common.kb.KbResource)
 	 */
 	public void unregisterResource(KbResource resource) {
 		kbConnector.unregisterResource(resource);
 	}
 
 	// Dinamic support
 	private Map<String,Object> trackers = new HashMap<String,Object>();
 	private Map<String,LoadBundleInfo> loadedBundles = new HashMap<String,LoadBundleInfo>();
 
 	private final static String[] TRACKERS_TO_WATCH = {"http://java.sun.com/jsf/core",
		"https://ajax4jsf.dev.java.net/ajax"
 	};
     private boolean taglibTrackerListenerInstalled = false;
 
 	public void invokeDelayedUpdateKnownTagLists() {
 		// Previous code is 
 		// timer.schedule(new MyTimerTask(), 500);
 		initTaglibPrefixes();
 		updateKnownTagLists();
 	}
 
 	private boolean isTrackerToWatch(String trackerUri) {
 		for(int i=0; TRACKERS_TO_WATCH != null && i<TRACKERS_TO_WATCH.length; i++) {
 			if (TRACKERS_TO_WATCH[i].equals(trackerUri)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void initTaglibPrefixes() {
 		trackers.clear();
 		if(taglibManagerProvider==null || taglibManagerProvider.getTaglibManager()==null) {
 			TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(fDocument);
 			if(manager!=null) {
 				List list = manager.getTaglibTrackers();
 				for(int i=0; i<list.size(); i++) {
 					TaglibTracker tracker = (TaglibTracker)list.get(i);
 					if (isTrackerToWatch(tracker.getURI())) {
 						trackers.put(tracker.getPrefix(), tracker);
 					}
 				}
 			}
 		} else {
 			List list = taglibManagerProvider.getTaglibManager().getTagLibs();
 			for(int i=0; i<list.size(); i++) {
 				TaglibData data = (TaglibData)list.get(i);
 				if (isTrackerToWatch(data.getUri())) {
 					trackers.put(data.getPrefix(), data);
 				}
 			}
 		}
 	}
 
 	public void taglibPrefixChanged(String[] prefixs) {
 		trackers.clear();
 		if(taglibManagerProvider==null || taglibManagerProvider.getTaglibManager()==null) {
 			TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(fDocument);
 			List list = manager.getTaglibTrackers();
 			for(int i=0; i<list.size(); i++) {
 				TaglibTracker tracker = (TaglibTracker)list.get(i);
 				if (isTrackerToWatch(tracker.getURI())) {
 					trackers.put(tracker.getPrefix(), tracker);
 				}
 			}
 		} else {
 			List list = taglibManagerProvider.getTaglibManager().getTagLibs();
 			for(int i=0; i<list.size(); i++) {
 				TaglibData data = (TaglibData)list.get(i);
 				if (isTrackerToWatch(data.getUri())) {
 					trackers.put(data.getPrefix(), data);
 				}
 			}
 		}
 		invokeDelayedUpdateKnownTagLists();
 	}
 
 	public void addTaglib(String uri, String prefix) {
 	}
 	
 	public void removeTaglib(String uri, String prefix) {
 	}
 
     private boolean installTaglibTrackerListener() {
     	if (taglibTrackerListenerInstalled) {
 			return true;
     	}
 		if(taglibManagerProvider!=null) {
 			VpeTaglibManager manager = taglibManagerProvider.getTaglibManager();
 			if(manager!=null) {
 				manager.addTaglibListener(this);
 				taglibTrackerListenerInstalled = true;
 				return true;
 			}
 		}
 		return false;
     }
 
 	public void updateKnownTagLists() {
 		loadedBundles.clear();
 
 		installTaglibTrackerListener();
 		
 		if (dom != null) {
 			Element element = dom.getDocumentElement();
 			NodeList children = (NodeContainer)dom.getChildNodes();
 			if (element != null) {
 				for (int i = 0; children != null && i < children.getLength(); i++) {
 					IDOMNode xmlnode = (IDOMNode)children.item(i);
 					update((IDOMNode)xmlnode);
 				}
 			}
 		}
 	}
 
 	private void update(IDOMNode element) {
 		if (element !=  null) {
 			registerKbResourceForNode(element);
 			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
 				if (child instanceof IDOMNode) {
 					update((IDOMNode)child);
 				}
 			}
 		}
 	}
 
 	public class LoadBundleInfo {
 		IDOMNode loadBundleTag;
 		String basename;
 		String var;
 
 		LoadBundleInfo(IDOMNode element, String basename, String var) {
 			this.loadBundleTag = element;
 			this.basename = basename;
 			this.var = var;
 		}
 
 		int getLoadDeclarationOffset() {
 			return loadBundleTag.getStartOffset();
 		}
 
 		public String getBaseName() {
 			return basename;
 		}
 
 		String getVar() {
 			return var;
 		}
 	}
 
 	public Map getDeclaredBundles() {
     	if (!taglibTrackerListenerInstalled) {
     		initTaglibPrefixes();
 			updateKnownTagLists();
     	}
 		return loadedBundles;
 	}
 
 	private void registerKbResourceForNode(IDOMNode node) {
 		if (node == null) return;
 		String name = node.getNodeName();
 		if (name == null) return;
 		if (!name.endsWith("loadBundle")) return;
 		if (name.indexOf(':') == -1) return;
 		String prefix = name.substring(0, name.indexOf(':'));
 
 		if (!trackers.containsKey(prefix)) return;
 
 		NamedNodeMap attributes = node.getAttributes();
 		if (attributes == null) return;
 		String basename = (attributes.getNamedItem("basename") == null ? null : attributes.getNamedItem("basename").getNodeValue());
 		String var = (attributes.getNamedItem("var") == null ? null : attributes.getNamedItem("var").getNodeValue());
 		if (basename == null || basename.length() == 0 ||
 			var == null || var.length() == 0) return;
 
 		loadedBundles.put(var, new LoadBundleInfo(node, basename, var));
 	}
 
 	public void dispose() {
 		if (documentAdapter != null && dom != null) {
 			((INodeNotifier) dom).removeAdapter(documentAdapter);
 		}
 		documentAdapter=null;
 		dom=null;
 		KbConnectorFactory.getIntstance().removeConnector(KbConnectorType.JSP_WTP_KB_CONNECTOR, fDocument);
 	}
 	
 	/**
 	 * This class listens to the changes in the CMDocument and triggers a CMDocument load
 	 */
 	class MyDocumentAdapter extends DocumentAdapter {
 		MyDocumentAdapter(Document document) {
 			super(document);
 		}
 
 		public void notifyChanged(INodeNotifier notifier, int eventType, Object feature, Object oldValue, Object newValue, int index) {
 			switch (eventType) {
 				case INodeNotifier.ADD :
 					{
 						if (newValue instanceof Element) {
 							adapt((Element) newValue);
 						}
 						break;
 					}
 				case INodeNotifier.REMOVE: 
 					{
 						Node node = (Node) notifier;
 						if (node.getNodeType() == Node.ELEMENT_NODE) {
 						}
 						break;
 					}
 
 				case INodeNotifier.CHANGE :
 				case INodeNotifier.STRUCTURE_CHANGED :
 				case INodeNotifier.CONTENT_CHANGED :
 					{
 						Node node = (Node) notifier;
 						if (node.getNodeType() == Node.ELEMENT_NODE) {
 							switch (eventType) {
 								case INodeNotifier.CHANGE :
 									{
 										break;
 									}
 								case INodeNotifier.STRUCTURE_CHANGED :
 									{
 										// structure change          
 										break;
 									}
 								case INodeNotifier.CONTENT_CHANGED :
 									{
 										// some content changed
 									break;
 									}
 							}
 						}
 						else if (node.getNodeType() == Node.DOCUMENT_NODE) {
 						} else {
 						}
 						break;
 					}
 			}
 			invokeDelayedUpdateKnownTagLists();
 		}
 	}
 
 	//
 	protected class MyTimerTask extends TimerTask {
 		public MyTimerTask() {
 			super();
 			timerTaskCount++;
 		}
 
 		public void run() {
 			timerTaskCount--;
 			if (timerTaskCount == 0) {
 //				invokeCMDocumentLoad();
 				initTaglibPrefixes();
 				updateKnownTagLists();
 			}
 		}
 	}
 
 	// An abstract adapter that ensures that the children of a new Node are also adapted
 	abstract class DocumentAdapter implements INodeAdapter {
 		public DocumentAdapter(Document document) {
 			((INodeNotifier) document).addAdapter(this);
 			adapt(document.getDocumentElement());
 		}
 
 		public void adapt(Element element) {
 			if (element != null) {
 				if (((INodeNotifier) element).getExistingAdapter(this) == null) {
 					((INodeNotifier) element).addAdapter(this);
 
 					for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
 						if (child.getNodeType() == Node.ELEMENT_NODE) {
 							adapt((Element) child);
 						}
 					}
 				}
 			}
 		}
 
 		public boolean isAdapterForType(Object type) {
 			return type == this;
 		}
 
 		abstract public void notifyChanged(INodeNotifier notifier, int eventType, Object feature, Object oldValue, Object newValue, int index);
 	}
 
 }
