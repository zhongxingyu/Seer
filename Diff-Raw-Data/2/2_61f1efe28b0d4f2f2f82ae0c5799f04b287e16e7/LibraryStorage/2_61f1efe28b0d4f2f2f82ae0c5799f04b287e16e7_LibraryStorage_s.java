 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.internal;
 
import java.util.Arrays;
import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IPath;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 
 /**
  * 
  * @author Viacheslav Kabanovich
  *
  */
 public class LibraryStorage {
 	private Set<ITagLibrary> allLibraries = new HashSet<ITagLibrary>();
 	private ITagLibrary[] allLibrariesArray = null;
 	Map<IPath, Set<ITagLibrary>> librariesBySource = new HashMap<IPath, Set<ITagLibrary>>();
 	Map<String, Set<ITagLibrary>> librariesByUri = new HashMap<String, Set<ITagLibrary>>();
 	private Map<String,ITagLibrary[]> librariesByUriArray = new HashMap<String, ITagLibrary[]>();
 
 	public void clear() {
 		synchronized(allLibraries) {
 			allLibraries.clear();
 			allLibrariesArray = null;
 		}
 		librariesBySource.clear();
 		synchronized (librariesByUri) {
 			librariesByUri.clear();
 			librariesByUriArray.clear();
 		}
 	}
 
 	public ITagLibrary[] getAllLibrariesArray() {
 		if(allLibrariesArray == null) {
 			synchronized(allLibraries) {
 				allLibrariesArray = allLibraries.toArray(new ITagLibrary[0]);
 			}
 		}
 		return allLibrariesArray;
 	}
 
 	private static final ITagLibrary[] EMPTY_LIB_ARRAY = new ITagLibrary[0];
 
 	public ITagLibrary[] getLibrariesArray(String uri) {
 		ITagLibrary[] result = librariesByUriArray.get(uri);
 		if(result == null) {
 			synchronized(librariesByUri) {
 				Set<ITagLibrary> libs = librariesByUri.get(uri);
 				if(libs!=null) {
 					result = libs.toArray(new ITagLibrary[0]);
 				} else {
 					result = EMPTY_LIB_ARRAY; 
 				}
 				librariesByUriArray.put(uri, result);
 			}
 		}
 		return result;
 	}
 
 	public Set<ITagLibrary> getLibrariesBySource(IPath path) {
 		return librariesBySource.get(path);
 	}
 
 	public ITagLibrary[] getLibrariesArray(IPath path) {
 		ITagLibrary[] result = EMPTY_LIB_ARRAY;
 		synchronized(librariesBySource) {
 			Set<ITagLibrary> libs = librariesBySource.get(path);
 			if(libs!=null) {
 				result = libs.toArray(new ITagLibrary[0]);
 			}
 		}
 		return result;
 	}
 
 	public void addLibrary(ITagLibrary f) {
 		synchronized(allLibraries) {
 			allLibraries.add(f);
 			allLibrariesArray = null;
 		}
 		IPath path = f.getSourcePath();
 		if(path != null) {
 			Set<ITagLibrary> fs = librariesBySource.get(path);
 			if(fs == null) {
 				fs = new HashSet<ITagLibrary>();
 				librariesBySource.put(path, fs);
 			}
 			fs.add(f);
 		}
 		String uri = f.getURI();
 		synchronized (librariesByUri) {
 			librariesByUriArray.remove(uri);
 			Set<ITagLibrary> ul = librariesByUri.get(uri);
 			if (ul == null) {
 				ul = new HashSet<ITagLibrary>();
 				librariesByUri.put(uri, ul);
 			}
 			ul.add(f);
 		}
 	}
 
 	public void removeLibrary(ITagLibrary f) {
 		synchronized(allLibraries) {
 			allLibraries.remove(f);
 			allLibrariesArray = null;
 		}
 		IPath path = f.getSourcePath();
 		if(path != null) {
 			Set<ITagLibrary> fs = librariesBySource.get(path);
 			if(fs != null) {
 				fs.remove(f);
 			}
 			if(fs.isEmpty()) {
 				librariesBySource.remove(fs);
 			}
 		}
 		String uri = f.getURI();
 		synchronized (librariesByUri) {
 			Set<ITagLibrary> ul = librariesByUri.get(uri);
 			librariesByUriArray.remove(uri);
 			if (ul != null) {
 				ul.remove(f);
 				if (ul.isEmpty()) {
 					librariesByUri.remove(uri);
 				}
 			}
 		}
 	}
 
 	public Set<ITagLibrary> removePath(IPath path) {
 		Set<ITagLibrary> fs = librariesBySource.get(path);
 		if(fs == null) return null;
 		for (ITagLibrary f: fs) {
 			synchronized(allLibraries) {
 				allLibraries.remove(f);
 				allLibrariesArray = null;
 			}
 			synchronized (librariesByUri) {
 				Set<ITagLibrary> s = librariesByUri.get(f.getURI());
 				if(s != null) s.remove(f);
 				if(s != null && s.isEmpty()) {
 					librariesByUri.remove(f.getURI());
 				}
 				librariesByUriArray.remove(f.getURI());
 			}
 		}
 		librariesBySource.remove(path);
 		return fs;
 	}
 
 }
