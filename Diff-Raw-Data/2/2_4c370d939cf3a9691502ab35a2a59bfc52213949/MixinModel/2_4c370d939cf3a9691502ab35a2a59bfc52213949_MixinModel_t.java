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
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.mixin.IMixinRequestor.ElementInfo;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.internal.core.ModelCache;
 import org.eclipse.dltk.internal.core.mixin.IInternalMixinElement;
 import org.eclipse.dltk.internal.core.mixin.MixinCache;
 import org.eclipse.dltk.internal.core.mixin.MixinManager;
 
 public class MixinModel {
 	public static final String SEPARATOR = String.valueOf(IIndexConstants.SEPARATOR);
 	private final MixinCache cache;
 
 	/**
 	 * Contains map of source modules to mixin elements.
 	 */
 	private Map elementToMixinCache = new HashMap();
 	private final IDLTKLanguageToolkit toolkit;
 
 	private MixinRequestor mixinRequestor = new MixinRequestor();
 
 	private ISourceModule currentModule;
 
 	/**
 	 * modules required to be reparsed
 	 */
 	private Set modulesToReparse = new HashSet();
 	public long removes = 1;
 	private final double ratio = 10000;
 
 	public MixinModel(IDLTKLanguageToolkit toolkit) {
 		this.toolkit = toolkit;
 
 		// long maxMemory = Runtime.getRuntime().freeMemory();
 
 		this.cache = new MixinCache(
 				(int) (ModelCache.DEFAULT_ROOT_SIZE * ratio));
 		DLTKCore.addElementChangedListener(changedListener);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 				changedListener);
 	}
 
 	public void stop() {
 		DLTKCore.removeElementChangedListener(changedListener);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
 				changedListener);
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
 			this.cache.remove(element);
 			cache.resetSpaceLimit(ModelCache.DEFAULT_ROOT_SIZE, element);
 			this.cache.removeKey(element.key);
 		}
 		return null;
 	}
 
 	public IMixinElement[] find(String pattern, long delta) {
 		Map set = new HashMap();
 
 		ISourceModule[] containedModules = SearchEngine.searchMixinSources(
 				pattern, toolkit, set);
 		Set modules = new HashSet();
 		modules.addAll(Arrays.asList(containedModules));
 
 		if (modules.size() == 0) {
 			return new IMixinElement[0];
 		}
 
 		// long start = System.currentTimeMillis();
 		// for (int i = 0; i < containedModules.length; ++i) {
 		for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
 			ISourceModule module = (ISourceModule) iterator.next();
 			reportModule(module);
 			// if (delta != -1) {
 			// // if (System.currentTimeMillis() - start > delta) {
 			// // System.out.println("Mixin timeout break:"
 			// // + Long.toString(System.currentTimeMillis() - start)
 			// // + ":"
 			// // + Integer.toString(containedModules.length - i));
 			// // break;
 			// // }
 			// }
 		}
 
 		List result = new ArrayList();
 
 		// int i = 0;
 		for (Iterator iterator = set.keySet().iterator(); iterator.hasNext();) {
 			ISourceModule module = (ISourceModule) iterator.next();
 			if (this.elementToMixinCache.containsKey(module)) {
 				Set keys = (Set) set.get(module);
 				for (Iterator iterator2 = keys.iterator(); iterator2.hasNext();) {
 					String key = (String) iterator2.next();
 					MixinElement element = getCreateEmpty(key);
 					markElementAsFinal(element);
 					result.add(element);
 					existKeysCache.add(key);
 					notExistKeysCache.remove(key);
 				}
 			}
 		}
 
 		return (IMixinElement[]) result
 				.toArray(new IMixinElement[result.size()]);
 	}
 
 	public IMixinElement[] find(String pattern) {
 		return find(pattern, -1);
 	}
 
 	public String[] findKeys(String pattern) {
 		return SearchEngine.searchMixinPatterns(pattern, toolkit);
 	}
 
 	private Set existKeysCache = new HashSet();
 	private Set notExistKeysCache = new HashSet();
 
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
 				cache.remove(element);
 				cache.resetSpaceLimit(ModelCache.DEFAULT_ROOT_SIZE, element);
 				cache.removeKey(element.key);
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
 		// if (DLTKCore.VERBOSE) {
 		// System.out.println("Filling ratio:" + this.cache.fillingRatio());
 		// this.cache.printStats();
 		// }
 		if (!this.elementToMixinCache.containsKey(sourceModule)) {
 			this.elementToMixinCache.put(sourceModule, new ArrayList());
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
 				// System.out.println("Mixins: reporting " +
 				// sourceModule.getPath());
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
 	 */
 	private ISourceModule[] findModules(String key) {
 		ISourceModule[] searchMixinSources = SearchEngine.searchMixinSources(
 				key, toolkit);
 		return searchMixinSources;
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
 
 			if (delta.getKind() == IModelElementDelta.CHANGED
 					&& ((delta.getFlags() & IModelElementDelta.F_REMOVED_FROM_BUILDPATH) != 0)) {
 				MixinModel.this.cache.flush();
 				MixinModel.this.elementToMixinCache.clear();
 				MixinModel.this.existKeysCache.clear();
 				MixinModel.this.notExistKeysCache.clear();
 				MixinModel.this.modulesToReparse.clear();
 			}
 
 			if (delta.getKind() == IModelElementDelta.CHANGED
 					&& ((delta.getFlags() & IModelElementDelta.F_ADDED_TO_BUILDPATH) != 0)) {
 				MixinModel.this.notExistKeysCache.clear();
 			}
 			if (delta.getKind() == IModelElementDelta.ADDED) {
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 					if (modulesToReparse.add(element)) {
 						reportModule((ISourceModule) element);
 					}
 				}
 				MixinModel.this.notExistKeysCache.clear();
 			}
 
 			if ((delta.getFlags() & IModelElementDelta.F_CHILDREN) != 0) {
 				IModelElementDelta[] affectedChildren = delta
 						.getAffectedChildren();
 				for (int i = 0; i < affectedChildren.length; i++) {
 					IModelElementDelta child = affectedChildren[i];
 					processDelta(child);
 				}
 			}
 		}
 
 		public void resourceChanged(IResourceChangeEvent event) {
 			int eventType = event.getType();
 			IResource resource = event.getResource();
 			// IResourceDelta delta = event.getDelta();
 
 			switch (eventType) {
 			case IResourceChangeEvent.PRE_DELETE:
 				if (resource.getType() == IResource.PROJECT
 						&& DLTKLanguageManager
 								.hasScriptNature((IProject) resource)) {
 					// remove all resources with given project from model.
 					List toRemove = new ArrayList();
 					synchronized (elementToMixinCache) {
 						IProject project = (IProject) resource;
 						for (Iterator iterator = elementToMixinCache.keySet()
 								.iterator(); iterator.hasNext();) {
 							ISourceModule module = (ISourceModule) iterator
 									.next();
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
 					for (Iterator iterator = toRemove.iterator(); iterator
 							.hasNext();) {
 						ISourceModule module = (ISourceModule) iterator.next();
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
 
 	public synchronized void remove(ISourceModule element) {
 		if (this.elementToMixinCache.containsKey(element)) {
 			List elements = (List) this.elementToMixinCache.get(element);
 			for (int i = 0; i < elements.size(); ++i) {
 				removes++;
 				MixinElement mixin = (MixinElement) elements.get(i);
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
 					cache.remove(mixin);
 					cache.resetSpaceLimit(ModelCache.DEFAULT_ROOT_SIZE, mixin);
 					cache.removeKey(mixin.key);
 				}
 			}
 			this.elementToMixinCache.remove(element);
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
 		private List sourceModules = new ArrayList();
 		private Map sourceModuleToObject = new HashMap();
 
 		/**
 		 * List of Strings.
 		 */
 		private Set children = new HashSet();
 
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
 						List list = new ArrayList();
 						list.add(object);
 						list.add(info.object);
 						this.sourceModuleToObject.put(module, list);
 					}
 				} else {
 					List list = new ArrayList();
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
 			return (IMixinElement[]) this.children
 					.toArray(new IMixinElement[this.children.size()]);
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
 			return (ISourceModule[]) this.sourceModules
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
 			List elements = (List) MixinModel.this.elementToMixinCache
 					.get(currentModule);
 			if (elements == null) {
 				elements = new ArrayList();
 				MixinModel.this.elementToMixinCache
 						.put(currentModule, elements);
 			}
 			elements.add(element);
 		}
 	}
 
 	public void makeAllModuleElementsFinal(ISourceModule module) {
 		if (this.elementToMixinCache.containsKey(module)) {
 			List elements = (List) this.elementToMixinCache.get(module);
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
 		// MixinElement e = (MixinElement)this.cache.get(key);
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
 }
