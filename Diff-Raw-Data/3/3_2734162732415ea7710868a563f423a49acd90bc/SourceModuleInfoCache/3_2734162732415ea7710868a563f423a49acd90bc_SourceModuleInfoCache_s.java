 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.lang.ref.SoftReference;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IElementCacheListener;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceModuleInfoCache;
 
 /**
  * Used to cache some source module information. All information related to
  * source module are removed, then source module are changed.
  * 
  * @author haiodo
  * 
  */
 public class SourceModuleInfoCache implements ISourceModuleInfoCache {
 	private ElementCache cache = null;
 	static long allAccess = 0;
 	static long miss = 0;
 	static long closes = 0;
 
 	public SourceModuleInfoCache() {
 		// set the size of the caches in function of the maximum amount of
 		// memory available
 		// long maxMemory = Runtime.getRuntime().freeMemory();
 		// if max memory is infinite, set the ratio to 4d which corresponds to
 		// the 256MB that Eclipse defaults to
 		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=111299)
 		double ratio = 50; // 128000000
 
 		this.cache = new ElementCache(
 				(int) (ModelCache.DEFAULT_ROOT_SIZE * ratio));
 		this.cache.setLoadFactor(0.90);
 		this.cache.addListener(new IElementCacheListener() {
 			public void close(Object element) {
 				closes++;
 			}
 		});
 		DLTKCore.addElementChangedListener(changedListener);
 	}
 
 	public void stop() {
 		DLTKCore.removeElementChangedListener(changedListener);
 	}
 
 	private final ISourceModuleInfo cacheGet(ISourceModule module) {
 		allAccess++;
 		final SoftReference ref = (SoftReference) cache.get(module);
 		return ref != null ? (ISourceModuleInfo) ref.get() : null;
 	}
 
 	public ISourceModuleInfo get(ISourceModule module) {
 		if (DLTKCore.VERBOSE) {
 			System.out.println("Filling ratio:" + this.cache.fillingRatio()); //$NON-NLS-1$
 		}
 		ISourceModuleInfo info = cacheGet(module);
 		if (info == null) {
 			miss++;
 			info = new SourceModuleInfo();
 			cache.put(module, new SoftReference(info));
 			cache.ensureSpaceLimit(1, module);
 			return info;
 		}
 		// this.cache.printStats();
 		if (DLTKCore.PERFOMANCE) {
 			System.out.println("SourceModuleInfoCache: access:" + allAccess //$NON-NLS-1$
 					+ " ok:" + (100.0f * (allAccess - miss) / allAccess) //$NON-NLS-1$
 					+ "% closes:" + closes); //$NON-NLS-1$
 			System.out.println("Filling ratio:" + this.cache.fillingRatio()); //$NON-NLS-1$
 		}
 		return info;
 	}
 
 	private IElementChangedListener changedListener = new IElementChangedListener() {
 		public void elementChanged(ElementChangedEvent event) {
 			IModelElementDelta delta = event.getDelta();
 			processDelta(delta);
 		}
 
 		private void processDelta(IModelElementDelta delta) {
 			IModelElement element = delta.getElement();
 			if (delta.getKind() == IModelElementDelta.REMOVED
 					|| delta.getKind() == IModelElementDelta.CHANGED) {
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
					if (isContentChanged(delta) || isWorkingCopy(delta)) {
 						if (DEBUG) {
 							System.out
 									.println("[Cache] remove: kind=" + delta.getKind() + " flags=" + Integer.toHexString(delta.getFlags()) + " elementName=" + delta.getElement().getElementName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						}
 						SourceModuleInfoCache.this
 								.remove((ISourceModule) element);
 					} else if (DEBUG) {
 						System.out
 								.println("[Cache] skip delta: kind=" + delta.getKind() + " flags=" + Integer.toHexString(delta.getFlags()) + " elementName=" + delta.getElement().getElementName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					}
 				}
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
 
 		private final boolean isContentChanged(IModelElementDelta delta) {
 			return (delta.getFlags() & (IModelElementDelta.F_CONTENT | IModelElementDelta.F_FINE_GRAINED)) == IModelElementDelta.F_CONTENT;
 		}
 
 		private final boolean isWorkingCopy(IModelElementDelta delta) {
 			return (delta.getFlags() & IModelElementDelta.F_PRIMARY_WORKING_COPY) != 0;
 		}
 	};
 
 	private static class SourceModuleInfo implements ISourceModuleInfo {
 		private Map map;
 
 		public Object get(Object key) {
 			if (map == null) {
 				return null;
 			}
 			return map.get(key);
 		}
 
 		public void put(Object key, Object value) {
 			if (map == null) {
 				map = new HashMap();
 			}
 			map.put(key, value);
 		}
 
 		public void remove(Object key) {
 			if (map != null) {
 				map.remove(key);
 			}
 		}
 
 		public boolean isEmpty() {
 			if (this.map == null) {
 				return true;
 			}
 			return this.map.isEmpty();
 		}
 	}
 
 	public void remove(ISourceModule element) {
 		if (DEBUG) {
 			System.out.println("[Cache] remove " + element.getElementName()); //$NON-NLS-1$
 		}
 		cache.remove(element);
 		cache.resetSpaceLimit(ModelCache.DEFAULT_ROOT_SIZE, element);
 	}
 
 	private static final boolean DEBUG = false;
 }
