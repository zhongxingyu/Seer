 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.mixin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
 import org.eclipse.dltk.core.mixin.IMixinRequestor.ElementInfo;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.internal.core.OverflowingLRUCache;
 import org.eclipse.dltk.internal.core.mixin.IInternalMixinElement;
 import org.eclipse.dltk.internal.core.mixin.MixinCache;
 import org.eclipse.dltk.internal.core.mixin.MixinManager;
 import org.eclipse.dltk.internal.core.util.LRUCache;
 
 public class MixinModel {
 	private static final long REQUEST_CACHE_EXPIRE_TIME = 2000;
 	private static final boolean DEBUG = false;
 	private static final boolean TRACE = false;
 
 	public static final String SEPARATOR = String
 			.valueOf(IIndexConstants.SEPARATOR);
 
 	private static final int CACHE_LIMIT = 250000;
 
 	private final MixinCache cache;
 
 	/**
 	 * Contains map of source modules to mixin elements.
 	 */
 	private Map<ISourceModule, List<MixinElement>> elementToMixinCache = new HashMap<ISourceModule, List<MixinElement>>();
 	private final IDLTKLanguageToolkit toolkit;
 
 	private final IScriptProject project;
 
 	private MixinRequestor mixinRequestor = new MixinRequestor();
 
 	private ISourceModule currentModule;
 
 	/**
 	 * modules required to be reparsed
 	 */
 	private Set modulesToReparse = new HashSet();
 	public long removes = 1;
 
 	/**
 	 * Creates workspace instance
 	 * 
 	 * @param toolkit
 	 */
 	public MixinModel(IDLTKLanguageToolkit toolkit) {
 		this(toolkit, null);
 	}
 
 	/**
 	 * Creates project instance
 	 * 
 	 * @param toolkit
 	 * @param project
 	 */
 	public MixinModel(IDLTKLanguageToolkit toolkit, IScriptProject project) {
 		this.toolkit = toolkit;
 		this.project = project;
 
 		// long maxMemory = Runtime.getRuntime().freeMemory();
 
 		this.cache = new MixinCache(CACHE_LIMIT);
 		DLTKCore.addElementChangedListener(changedListener,
 				ElementChangedEvent.POST_CHANGE);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 				changedListener);
 		MixinModelRegistry.register(this);
 	}
 
 	public void stop() {
 		DLTKCore.removeElementChangedListener(changedListener);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
 				changedListener);
 		MixinModelRegistry.unregister(this);
 	}
 
 	public IMixinElement get(String key) {
 		if (DLTKCore.VERBOSE) {
 			System.out.println("MixinModel.get(" + key + ')'); //$NON-NLS-1$
 		}
 		if (notExistKeysCache.contains(key)) {
 			return null;
 		}
 		if (removes == 0) {
 			if (cache.get(key) == null) {
 				return null;
 			}
 		}
 		MixinElement element = getCreateEmpty(key);
 		if (DLTKCore.VERBOSE) {
 			System.out.println("Filling ratio:" + this.cache.fillingRatio()); //$NON-NLS-1$
 			this.cache.printStats();
 		}
 		buildElementTree(element);
 		if (element.isFinal() && element.sourceModules.size() > 0) {
 			existKeysCache.add(key);
 			return element;
 		}
 		notExistKeysCache.add(key);
 		synchronized (this.cache) {
 			cache.remove(element.key);
 			cache.resetSpaceLimit(CACHE_LIMIT, element);
 		}
 		return null;
 	}
 
 	private IDLTKSearchScope createSearchScope() {
 		if (project != null) {
 			return SearchEngine.createSearchScope(project);
 		} else {
 			return SearchEngine.createWorkspaceScope(toolkit);
 		}
 	}
 
 	private static class RequestCacheEntry {
 		long expireTime;
 		String prefix = null;
 		Set<ISourceModule> modules = null;
 		Set<String> keys = null;
 	}
 
 	private static class RequestCache extends OverflowingLRUCache {
 
 		public RequestCache(int size) {
 			super(size);
 		}
 
 		public RequestCache(int size, int overflow) {
 			super(size, overflow);
 		}
 
 		protected boolean close(LRUCacheEntry entry) {
 			return true;
 		}
 
 		protected LRUCache newInstance(int size, int overflow) {
 			return new RequestCache(size, overflow);
 		}
 	};
 
 	private final RequestCache requestCache = new RequestCache(500);
 
 	/**
 	 * @deprecated
 	 */
 	public IMixinElement[] find(String pattern, long delta) {
 		return find(pattern, new NullProgressMonitor());
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public IMixinElement[] find(String pattern, IProgressMonitor monitor) {
 		long start = TRACE ? System.currentTimeMillis() : 0;
 
 		RequestCacheEntry entry = findFromMixin(pattern, monitor);
 
 		if (entry.modules == null || entry.modules.size() == 0) {
 			return new IMixinElement[0];
 		}
 
 		long parses = TRACE ? System.currentTimeMillis() : 0;
 		for (ISourceModule module : entry.modules) {
 			reportModule(module);
 		}
 		long parsee = TRACE ? System.currentTimeMillis() : 0;
 
 		Set<MixinElement> result = new HashSet<MixinElement>();
 
 		// int i = 0;
 		for (String key : entry.keys) {
 			MixinElement element = getCreateEmpty(key);
 			if (!monitor.isCanceled()) {
 				markElementAsFinal(element);
 			}
 			addKeyToSet(result, element, pattern);
 		}
 		if (TRACE) {
 			long end = System.currentTimeMillis();
 			System.out.println("MixinModel::find.time:" //$NON-NLS-1$
 					+ String.valueOf(end - start));
 			System.out.println("MixinModel::find.parsetime:" //$NON-NLS-1$
 					+ String.valueOf(parsee - parses));
 		}
 
 		return result.toArray(new IMixinElement[result.size()]);
 	}
 
 	private synchronized void addKeyToSet(Set<MixinElement> result,
 			MixinElement element, String pattern) {
 		// Skip all not matched keys
 		if (!CharOperation.match(pattern.toCharArray(), element.key
 				.toCharArray(), true)) {
 			return;
 		}
 		result.add(element);
 		existKeysCache.add(element.key);
 		notExistKeysCache.remove(element.key);
 		IMixinElement[] children = (IMixinElement[]) element.children
 				.toArray(new IMixinElement[element.children.size()]);
 		for (int i = 0; i < children.length; i++) {
 			addKeyToSet(result, (MixinElement) children[i], pattern);
 		}
 	}
 
 	private RequestCacheEntry findFromMixin(String pattern,
 			IProgressMonitor monitor) {
 		PerformanceNode p = RuntimePerformanceMonitor.begin();
 		RequestCacheEntry entry = (RequestCacheEntry) requestCache.get(pattern);
 		// Set modules = new HashSet();
 		if (entry == null || entry.expireTime < System.currentTimeMillis()) {
 			Map keys = new HashMap();
 			ISourceModule[] containedModules = null;
 			entry = new RequestCacheEntry();
 			try {
 				containedModules = SearchEngine.searchMixinSources(
 						createSearchScope(), pattern, toolkit, keys, monitor);
 			} catch (OperationCanceledException e) {
 				return entry;
 			}
 			entry.expireTime = System.currentTimeMillis()
 					+ REQUEST_CACHE_EXPIRE_TIME;
 			entry.modules = new HashSet<ISourceModule>(Arrays
 					.asList(containedModules));
 			entry.prefix = pattern;
 			Collection values = keys.values();
 			entry.keys = new HashSet<String>();
 			for (Iterator iterator = values.iterator(); iterator.hasNext();) {
 				Set vals = (Set) iterator.next();
 				entry.keys.addAll(vals);
 			}
 			if (!monitor.isCanceled()) {
 				this.requestCache.put(pattern, entry);
 			}
 		}
 		p.done(getNature(), "Mixin model search items", 0);
 		return entry;
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public IMixinElement[] find(String pattern) {
 		return find(pattern, new NullProgressMonitor());
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public String[] findKeys(String pattern) {
 		return findKeys(pattern, new NullProgressMonitor());
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public String[] findKeys(String pattern, IProgressMonitor monitor) {
 		RequestCacheEntry entry = findFromMixin(pattern, monitor);
 		return entry.keys.toArray(new String[entry.keys.size()]);
 	}
 
 	private Set<String> existKeysCache = new HashSet<String>();
 	private Set<String> notExistKeysCache = new HashSet<String>();
 
 	public boolean keyExists(String key) {
 		// TODO: For this version we cache all information, so should be 0.
 		if (removes == 0) {
 			return this.cache.get(key) != null;
 		}
 		MixinElement e = (MixinElement) this.cache.get(key);
 		if (e != null && e.sourceModules.size() > 0) {
 			return true;
 		}
 		if (existKeysCache.contains(key)) {
 			return true;
 		}
 		if (notExistKeysCache.contains(key)) {
 			return false;
 		}
 		boolean exist = get(key) != null;
 		// System.out.println("1");
 		if (exist) {
 			if (existKeysCache.size() > 500000) {
 				existKeysCache.clear();
 			}
 			existKeysCache.add(key);
 		} else {
 			if (notExistKeysCache.size() > 500000) {
 				notExistKeysCache.clear();
 			}
 			notExistKeysCache.add(key);
 		}
 
 		return exist;
 	}
 
 	private void buildElementTree(MixinElement element) {
 		// TODO: This is consistent cache stage
 		if (element.isFinal()) {
 			return;
 		}
 		ISourceModule[] containedModules = findModules(element.getKey());
 		if (containedModules.length == 0) {
 			synchronized (cache) {
 				cache.remove(element.key);
 				cache.resetSpaceLimit(CACHE_LIMIT, element);
 			}
 			return;
 		}
 		for (int i = 0; i < containedModules.length; ++i) {
 			reportModule(containedModules[i]);
 		}
 		// mark selected element and all subelements as finished.
 		markElementAsFinal(element);
 	}
 
 	private synchronized void markElementAsFinal(MixinElement element) {
 		element.bFinal = true;
 		for (Iterator i = element.children.iterator(); i.hasNext();) {
 			markElementAsFinal((MixinElement) i.next());
 		}
 	}
 
 	public synchronized void reportModule(ISourceModule sourceModule) {
 		if (!this.elementToMixinCache.containsKey(sourceModule)) {
 			this.elementToMixinCache.put(sourceModule,
 					new ArrayList<MixinElement>());
 		} else { // Module already in model. So we do not to rebuild it.
 			if (!this.modulesToReparse.remove(sourceModule)) {
 				return;
 			}
 			// We need to reparse module if some elements are moved from it.
 		}
 		IMixinParser mixinParser;
 		try {
 			mixinParser = MixinManager.getMixinParser(sourceModule);
 			if (mixinParser != null) {
 				this.currentModule = sourceModule;
 				mixinParser.setRequirestor(mixinRequestor);
 				mixinParser.parserSourceModule(true, sourceModule);
 				this.currentModule = null;
 			}
 		} catch (CoreException e) {
 			DLTKCore.error("Error in reportModule", e); //$NON-NLS-1$
 			return;
 		}
 	}
 
 	/**
 	 * Should find all elements source modules to be sure we build complete
 	 * child tree.
 	 * 
 	 * @param element
 	 * @return
 	 * @since 2.0
 	 */
 	public ISourceModule[] findModules(String key, IProgressMonitor monitor) {
 		RequestCacheEntry entry = findFromMixin(key, monitor);
 		return (ISourceModule[]) entry.modules
 				.toArray(new ISourceModule[entry.modules.size()]);
 	}
 
 	/**
 	 * @Deprecated
 	 */
 	public ISourceModule[] findModules(String key) {
 		return findModules(key, new NullProgressMonitor());
 	}
 
 	/**
 	 * Returns a mixin element from this.cache by it's key, or creates new one
 	 * if cache doesn't contain required element
 	 * 
 	 * @param key
 	 * @return
 	 */
 	private synchronized MixinElement getCreateEmpty(String key) {
 		MixinElement element = (MixinElement) MixinModel.this.cache.get(key);
 		if (element == null) {
 			element = new MixinElement(key, currentModule);
 			this.cache.put(key, element);
 			this.cache.ensureSpaceLimit(1, element);
 		}
 		return element;
 	}
 
 	private interface IMixinChangedListener extends IElementChangedListener,
 			IResourceChangeListener {
 	}
 
 	private IMixinChangedListener changedListener = new IMixinChangedListener() {
 		public void elementChanged(ElementChangedEvent event) {
 			IModelElementDelta delta = event.getDelta();
 			processDelta(delta);
 		}
 
 		private void processDelta(IModelElementDelta delta) {
 			IModelElement element = delta.getElement();
 			if (delta.getKind() == IModelElementDelta.REMOVED
 					|| delta.getKind() == IModelElementDelta.CHANGED
 					|| (delta.getFlags() & IModelElementDelta.F_REMOVED_FROM_BUILDPATH) != 0
 					|| (delta.getFlags() & IModelElementDelta.CHANGED) != 0) {
 				if (element.getElementType() != IModelElement.SOURCE_MODULE
 						&& element.getElementType() != IModelElement.PROJECT_FRAGMENT
 						&& element.getElementType() != IModelElement.SCRIPT_FOLDER
 						&& element.getElementType() != IModelElement.SCRIPT_MODEL
 						&& element.getElementType() != IModelElement.SCRIPT_PROJECT) {
 					ISourceModule module = (ISourceModule) element
 							.getAncestor(IModelElement.SOURCE_MODULE);
 					MixinModel.this.remove(module);
 				}
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 					MixinModel.this.remove((ISourceModule) element);
 				}
 			}
 
 			if (element.getElementType() == IModelElement.SCRIPT_PROJECT
 					&& delta.getKind() == IModelElementDelta.CHANGED
 					&& (delta.getFlags() & IModelElementDelta.F_BUILDPATH_CHANGED) != 0) {
 				clear();
 				return;
 			} else if ((delta.getKind() == IModelElementDelta.REMOVED || delta
 					.getKind() == IModelElementDelta.CHANGED)
 					&& (element.getElementType() == IModelElement.SCRIPT_FOLDER || element
 							.getElementType() == IModelElement.PROJECT_FRAGMENT)) {
 				if (delta.getAffectedChildren().length == 0) {
 					try {
 						element.accept(new IModelElementVisitor() {
 							public boolean visit(IModelElement element) {
 								if (element.getElementType() == ISourceModule.SOURCE_MODULE) {
 									remove((ISourceModule) element);
 									return false;
 								}
 								return true;
 							}
 						});
 					} catch (ModelException e) {
 						if (DLTKCore.DEBUG) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			if (delta.getKind() == IModelElementDelta.ADDED) {
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 					if (modulesToReparse.add(element)) {
 						reportModule((ISourceModule) element);
 					}
 				}
 				MixinModel.this.notExistKeysCache.clear();
 				requestCache.flush();
 			}
 
 			if ((delta.getFlags() & IModelElementDelta.F_CHILDREN) != 0) {
 				IModelElementDelta[] affectedChildren = delta
 						.getAffectedChildren();
 				for (int i = 0; i < affectedChildren.length; i++) {
 					IModelElementDelta child = affectedChildren[i];
 					processDelta(child);
 				}
 			} else if (delta.getKind() == IModelElementDelta.REMOVED
 					&& element.getElementType() == IModelElement.SCRIPT_FOLDER) {
 				/* folder delete delta has no children */
 				MixinModel.this.removeFolder((IScriptFolder) element);
 			}
 		}
 
 		public void resourceChanged(IResourceChangeEvent event) {
 			int eventType = event.getType();
 			IResource resource = event.getResource();
 			// IResourceDelta delta = event.getDelta();
 
 			switch (eventType) {
 			case IResourceChangeEvent.PRE_CLOSE:
 				if (resource.getType() == IResource.PROJECT
 						&& DLTKLanguageManager
 								.hasScriptNature((IProject) resource)) {
 					if (project != null
 							&& resource.equals(project.getProject())) {
 						clear();
 						// TODO destroy this model
 						return;
 					}
 				}
 				break;
 			case IResourceChangeEvent.PRE_DELETE:
 				if (resource.getType() == IResource.PROJECT
 						&& DLTKLanguageManager
 								.hasScriptNature((IProject) resource)) {
 					if (project != null
 							&& resource.equals(project.getProject())) {
 						clear();
 						// TODO destroy this model
 						return;
 					}
 					// remove all resources with given project from model.
 					List<ISourceModule> toRemove = new ArrayList<ISourceModule>();
 					synchronized (elementToMixinCache) {
 						IProject project = (IProject) resource;
 						for (ISourceModule module : elementToMixinCache
 								.keySet()) {
 							IScriptProject scriptProject = module
 									.getScriptProject();
 							if (scriptProject != null) {
 								IProject prj = scriptProject.getProject();
 								if ((prj != null && prj.equals(project))
 										|| prj == null) {
 									toRemove.add(module);
 								}
 							} else {
 								toRemove.add(module);
 							}
 						}
 					}
 					for (ISourceModule module : toRemove) {
 						remove(module);
 					}
 				}
 				return;
 			}
 		}
 	};
 
 	// private synchronized void clearAllElementsState() {
 	// Enumeration elements = cache.elements();
 	// while( elements.hasMoreElements() ) {
 	// MixinElement o = (MixinElement)elements.nextElement();
 	// o.bFinal = false;
 	// }
 	// }
 
 	private final String getLogContext() {
 		if (project == null) {
 			return "[MixinModel|$" + toolkit.getLanguageName() + "$]"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else {
 			return "[MixinModel|" + project.getElementName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	private final void log(String message) {
 		System.out.println(getLogContext() + " " + message); //$NON-NLS-1$
 	}
 
 	public synchronized void remove(ISourceModule element) {
 		if (DEBUG) {
 			log("remove " + element.getElementName()); //$NON-NLS-1$
 		}
 		if (this.elementToMixinCache.containsKey(element)) {
 			removeFromRequestCache(element);
 
 			List<MixinElement> elements = this.elementToMixinCache.get(element);
 			for (int i = 0; i < elements.size(); ++i) {
 				removes++;
 				MixinElement mixin = elements.get(i);
 				existKeysCache.remove(mixin.key);
 				notExistKeysCache.remove(mixin.key);
 				mixin.bFinal = false;
 				mixin.sourceModules.remove(element);
 				mixin.sourceModuleToObject.remove(element);
 				if (mixin.sourceModules.size() == 0) {
 					// Remove frob parent.
 					String parentKey = mixin.getParentKey();
 					if (parentKey != null) {
 						MixinElement parent = (MixinElement) this.cache
 								.get(parentKey);
 						if (parent != null) {
 							parent.children.remove(mixin);
 							parent.bFinal = false;
 						}
 					}
 					// Remove from cache
 					cache.remove(mixin.key);
 					cache.resetSpaceLimit(CACHE_LIMIT, mixin);
 				}
 			}
 			this.elementToMixinCache.remove(element);
 		}
 	}
 
 	/**
 	 * @param folder
 	 */
 	protected void removeFolder(IScriptFolder folder) {
 		final IPath folderPath = folder.getPath();
 		final List<ISourceModule> modulesToRemove = new ArrayList<ISourceModule>();
 		for (final ISourceModule module : elementToMixinCache.keySet()) {
 			final IPath path = module.getPath();
 			if (folderPath.isPrefixOf(path)) {
 				modulesToRemove.add(module);
 			}
 		}
 		for (ISourceModule module : modulesToRemove) {
 			remove(module);
 		}
 	}
 
 	private void removeFromRequestCache(ISourceModule element) {
 		// Clear requests cache.
 		List<String> keysToRemove = new ArrayList<String>();
 		@SuppressWarnings("unchecked")
 		Enumeration<RequestCacheEntry> enumeration = this.requestCache
 				.elements();
 		while (enumeration.hasMoreElements()) {
 			RequestCacheEntry entry = enumeration.nextElement();
 			if (entry.modules != null) {
 				if (entry.modules.contains(element)) {
 					keysToRemove.add(entry.prefix);
 				}
 			}
 		}
 		for (String key : keysToRemove) {
 			this.requestCache.remove(key);
 		}
 	}
 
 	/***************************************************************************
 	 * Then getObjects are called, special initialize listener are called.
 	 * 
 	 */
 	public interface IMixinObjectInitializeListener {
 		void initialize(IMixinElement element, Object object,
 				ISourceModule module);
 	}
 
 	private final ListenerList mixinObjectInitializeListeners = new ListenerList();
 
 	private class MixinElement implements IMixinElement, IInternalMixinElement {
 		private String key;
 		private boolean bFinal = false;
 		private List<ISourceModule> sourceModules = new ArrayList<ISourceModule>();
 		private Map<ISourceModule, Object> sourceModuleToObject = new HashMap<ISourceModule, Object>();
 
 		/**
 		 * List of Strings.
 		 */
 		private Set<IMixinElement> children = new HashSet<IMixinElement>();
 
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj instanceof MixinElement) {
 				return this.key.equals(((MixinElement) obj).key);
 			}
 			return false;
 		}
 
 		public int hashCode() {
 			return this.key.hashCode();
 		}
 
 		public MixinElement(String key) {
 			this.key = key;
 		}
 
 		public String toString() {
 			return this.getLastKeySegment() + " final[" + this.bFinal + "]" //$NON-NLS-1$ //$NON-NLS-2$
 					+ this.children + " "; //$NON-NLS-1$
 		}
 
 		public MixinElement(ElementInfo info, ISourceModule module) {
 			this(info.key, currentModule);
 			addInfo(info, module);
 		}
 
 		void addInfo(ElementInfo info, ISourceModule module) {
 			if (info.object != null) {
 				final Object object = this.sourceModuleToObject.get(module);
 				if (object != null) {
 					if (object instanceof List) {
 						((List) object).add(info.object);
 					} else {
 						List<Object> list = new ArrayList<Object>();
 						list.add(object);
 						list.add(info.object);
 						this.sourceModuleToObject.put(module, list);
 					}
 				} else {
 					List<Object> list = new ArrayList<Object>();
 					list.add(info.object);
 					this.sourceModuleToObject.put(module, list);
 				}
 			}
 		}
 
 		public MixinElement(String key, ISourceModule currentModule) {
 			this.key = key;
 			addModule(currentModule);
 		}
 
 		void addModule(ISourceModule currentModule) {
 			if (currentModule != null) {
 				if (!this.sourceModules.contains(currentModule)) {
 					this.sourceModules.add(currentModule);
 				}
 			}
 		}
 
 		public IMixinElement[] getChildren() {
 			this.validate();
 			return children.toArray(new IMixinElement[children.size()]);
 		}
 
 		public IMixinElement getChildren(String key) {
 			this.validate();
 			return MixinModel.this.get(this.key
 					+ IMixinRequestor.MIXIN_NAME_SEPARATOR + key);
 		}
 
 		public String getKey() {
 			return this.key;
 		}
 
 		protected String getParentKey() {
 			int pos = key.lastIndexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR);
 			if (pos == -1) {
 				return null;
 			}
 			return key.substring(0, pos);
 		}
 
 		public String getLastKeySegment() {
 			int pos = key.lastIndexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR);
 			if (pos == -1) {
 				return key;
 			}
 			return key.substring(pos + 1);
 		}
 
 		public IMixinElement getParent() {
 			String parentKey = this.getParentKey();
 			if (parentKey == null) {
 				return null;
 			}
 			return get(parentKey);
 
 		}
 
 		public ISourceModule[] getSourceModules() {
 			this.validate();
 			if (!this.isFinal()) {
 				get(this.key);
 			}
 			return this.sourceModules
 					.toArray(new ISourceModule[this.sourceModules.size()]);
 		}
 
 		public Object[] getObjects(ISourceModule module) {
 			this.validate();
 			Object o = this.sourceModuleToObject.get(module);
 			if (o instanceof List) {
 
 				Object[] objs = ((List) o).toArray();
 				for (int i = 0; i < objs.length; i++) {
 					notifyInitializeListener(this, module, objs[i]);
 				}
 				return objs;
 			}
 			if (o != null) {
 				notifyInitializeListener(this, module, o);
 				return new Object[] { o };
 			}
 			return new Object[0];
 		}
 
 		public Object[] getAllObjects() {
 			this.validate();
 			HashSet objects = new HashSet();
 			for (Iterator iterator = this.sourceModules.iterator(); iterator
 					.hasNext();) {
 				ISourceModule module = (ISourceModule) iterator.next();
 				Object[] objs = this.getObjects(module);
 				for (int j = 0; j < objs.length; ++j) {
 					objects.add(objs[j]);
 				}
 			}
 			return objects.toArray();
 		}
 
 		public boolean isFinal() {
 			return this.bFinal;
 		}
 
 		public void close() {
 			existKeysCache.remove(key);
 			notExistKeysCache.remove(key);
 			removes++;
 			this.bFinal = false;
 			for (int i = 0; i < sourceModules.size(); i++) {
 				Object module = sourceModules.get(i);
 				List list = (List) elementToMixinCache.get(module);
 				if (list != null) {
 					list.remove(this);
 					if (list.size() == 0) {
 						elementToMixinCache.remove(module);
 					}
 				}
 				if (!modulesToReparse.contains(module)
 						&& elementToMixinCache.containsKey(module)) {
 					modulesToReparse.add(module);
 				}
 			}
 			this.sourceModules.clear();
 			this.sourceModuleToObject.clear();
 
 			// Lets also clean parent data
 			// Remove frob parent.
 			String parentKey = getParentKey();
 			MixinElement element = this;
 			while (parentKey != null) {
 				MixinElement parent = (MixinElement) cache.get(parentKey);
 				if (parent != null) {
 					existKeysCache.remove(parent.key);
 					notExistKeysCache.remove(parent.key);
 					removes++;
 					parent.children.remove(element);
 					parent.bFinal = false;
 					element = parent;
 					parentKey = parent.getParentKey();
 				} else {
 					break;
 				}
 			}
 
 		}
 
 		private void validate() {
 			if (!isFinal()) {
 				buildElementTree(this);
 			}
 		}
 	};
 
 	private class MixinRequestor implements IMixinRequestor {
 		public void reportElement(ElementInfo info) {
 			// if( DLTKCore.VERBOSE_MIXIN ) {
 			// System.out.println("Append mixin:" + info.key);
 			// }
 			existKeysCache.add(info.key);
 			notExistKeysCache.remove(info.key);
 			String[] list = info.key.split("\\" //$NON-NLS-1$
 					+ IMixinRequestor.MIXIN_NAME_SEPARATOR);
 			MixinElement element = getCreateEmpty(info.key);
 			addElementToModules(element);
 			element.addModule(currentModule);
 			element.addInfo(info, currentModule);
 			// Append as childs for all other elements. Also append modules to
 			// all selected elements.
 			if (list.length != 1) {
 				for (int i = 0; i < list.length - 1; ++i) {
 					MixinElement parent = getCreateEmpty(element.getParentKey());
 					if (!parent.children.contains(element)) {
 						parent.children.add(element);
 					}
 					addElementToModules(parent);
 					element = parent;
 				}
 			}
 		}
 
 		private void addElementToModules(MixinElement element) {
 			List<MixinElement> elements = MixinModel.this.elementToMixinCache
 					.get(currentModule);
 			if (elements == null) {
 				elements = new ArrayList<MixinElement>();
 				MixinModel.this.elementToMixinCache
 						.put(currentModule, elements);
 			}
 			elements.add(element);
 		}
 	}
 
 	public void makeAllModuleElementsFinal(ISourceModule module) {
 		if (this.elementToMixinCache.containsKey(module)) {
 			List<MixinElement> elements = this.elementToMixinCache.get(module);
 			for (int i = 0; i < elements.size(); ++i) {
 				removes++;
 				MixinElement mixin = (MixinElement) elements.get(i);
 				mixin.bFinal = true;
 			}
 		}
 	}
 
 	public void makeAllElementsFinalIfNoCacheRemoves() {
 		if (removes != 0) {
 			return;
 		}
 		Enumeration elements = cache.elements();
 		while (elements.hasMoreElements()) {
 			MixinElement e = (MixinElement) elements.nextElement();
 			e.bFinal = true;
 		}
 
 	}
 
 	public void setRemovesToZero() {
 		removes = 0;
 	}
 
 	public void clearKeysCache(String key) {
 		existKeysCache.remove(key);
 		notExistKeysCache.remove(key);
 		requestCache.remove(key);
 		// MixinElement e = (MixinElement)this.cache.get(key);
 	}
 
 	public void clearKeysCache() {
 		existKeysCache.clear();
 		notExistKeysCache.clear();
		requestCache.clone();
 	}
 
 	// // Mixin object initialize listeners code
 	public void addObjectInitializeListener(
 			IMixinObjectInitializeListener mixinObjectInitializeListener) {
 		this.mixinObjectInitializeListeners.add(mixinObjectInitializeListener);
 	}
 
 	public void removeObjectInitializeListener(
 			IMixinObjectInitializeListener mixinObjectInitializeListener) {
 		this.mixinObjectInitializeListeners
 				.remove(mixinObjectInitializeListener);
 	}
 
 	private void notifyInitializeListener(IMixinElement element,
 			ISourceModule module, Object o) {
 		Object[] listeners = mixinObjectInitializeListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			((IMixinObjectInitializeListener) (listeners[i])).initialize(
 					element, o, module);
 		}
 	}
 
 	protected void clear() {
 		cache.flush();
 		elementToMixinCache.clear();
 		existKeysCache.clear();
 		notExistKeysCache.clear();
 		modulesToReparse.clear();
 		requestCache.flush();
 	}
 
 	public String getNature() {
 		return toolkit.getNatureId();
 	}
 }
