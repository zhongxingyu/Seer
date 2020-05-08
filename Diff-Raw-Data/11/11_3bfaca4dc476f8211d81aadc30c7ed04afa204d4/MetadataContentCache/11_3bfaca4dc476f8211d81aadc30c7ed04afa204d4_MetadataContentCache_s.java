 package org.eclipse.dltk.core.caching;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.CRC32;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.caching.cache.CacheEntry;
 import org.eclipse.dltk.core.caching.cache.CacheEntryAttribute;
 import org.eclipse.dltk.core.caching.cache.CacheFactory;
 import org.eclipse.dltk.core.caching.cache.CacheIndex;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 /**
  * This class is designed to store any kind of information into metadata cache.
  */
 public class MetadataContentCache extends AbstractContentCache {
 	private static final int DAY_IN_MILIS = 1000 * 60 * 60 * 24;
 	private static final int SAVE_DELTA = 100;
 	private Resource indexResource = null;
 	private Map<String, CacheEntry> entryCache = new HashMap<String, CacheEntry>();
 	private IPath cacheLocation;
 	private CRC32 checksum = new CRC32();
 
 	public MetadataContentCache(IPath cacheLocation) {
 		this.cacheLocation = cacheLocation;
 	}
 
 	private void initialize() {
 		if (indexResource == null) {
 			File file = new File(cacheLocation.toOSString());
 			if (!file.exists()) {
 				file.mkdir();
 			}
 
 			IPath indexFile = cacheLocation.append("index");
 			indexResource = new XMIResourceImpl(URI.createFileURI(indexFile
 					.toOSString()));
 			File indexFileHandle = new File(indexFile.toOSString());
 			if (indexFileHandle.exists()) {
 				try {
 					indexResource.load(null);
 				} catch (Exception e) {
 					save(false);
 					if (DLTKCore.DEBUG) {
 						// e.printStackTrace();
 					}
 				}
 			}
 			EList<EObject> contents = indexResource.getContents();
 			for (EObject eObject : contents) {
 				CacheIndex index = (CacheIndex) eObject;
 				// String to entry cache.
 				EList<CacheEntry> entries = index.getEntries();
 				for (CacheEntry cacheEntry : entries) {
 					entryCache.put(makeKey(cacheEntry), cacheEntry);
 				}
 			}
 		}
 	}
 
 	private synchronized CacheEntry getEntry(IFileHandle handle) {
 		initialize();
 		String key = makeKey(handle);
 		if (entryCache.containsKey(key)) {
 			CacheEntry entry = (CacheEntry) entryCache.get(key);
 			long accessTime = entry.getLastAccessTime();
 			long timeMillis = System.currentTimeMillis();
 			if (timeMillis - accessTime > DAY_IN_MILIS) {
 				if (entry.getTimestamp() == handle.lastModified()) {
 					entry.setLastAccessTime(timeMillis);
 					return entry;
 				} else {
 					entry.setLastAccessTime(timeMillis);
 					removeCacheEntry(entry, key);
 				}
 			} else {
 				entry.setLastAccessTime(timeMillis);
 				return entry;
 			}
 		}
 		CacheIndex index = getCacheIndex(handle.getEnvironmentId());
 		CacheEntry entry = CacheFactory.eINSTANCE.createCacheEntry();
 		entry.setPath(handle.getPath().toPortableString());
 		entry.setTimestamp(handle.lastModified());
 		index.getEntries().add(entry);
 		entryCache.put(key, entry);
 		return entry;
 	}
 
 	private CacheIndex getCacheIndex(String environmentId) {
 		EList<EObject> contents = indexResource.getContents();
 		for (EObject eObject : contents) {
 			CacheIndex index = (CacheIndex) eObject;
 			if (index.getEnvironment().equals(environmentId)) {
 				return index;
 			}
 		}
 		CacheIndex index = CacheFactory.eINSTANCE.createCacheIndex();
 		index.setEnvironment(environmentId);
 		index.setLastIndex(0);
 		contents.add(index);
 		return index;
 	}
 
 	private void removeCacheEntry(CacheEntry entry, String key) {
 		if (entry == null || key == null) {
 			return;
 		}
 		// We need to remove old files
 		EList<CacheEntryAttribute> attributes = entry.getAttributes();
 		for (CacheEntryAttribute attr : attributes) {
 			removeAttribute(attr);
 		}
 		attributes.clear();
 		CacheIndex index = (CacheIndex) entry.eContainer();
 		index.getEntries().remove(entry);
 		entryCache.remove(key);
 	}
 
 	private void removeAttribute(CacheEntryAttribute attr) {
 		String location = attr.getLocation();
 		IPath cacheEntryFile = cacheLocation.append(location);
 		File file = new File(cacheEntryFile.toOSString());
 		if (file.exists()) {
 			file.delete();
 		}
 	}
 
 	private String makeKey(CacheEntry entry) {
 		CacheIndex index = (CacheIndex) entry.eContainer();
 		return entry.getPath() + ":" + index.getEnvironment();
 	}
 
 	private String makeKey(IFileHandle handle) {
 		return handle.getPath().toPortableString() + ":"
 				+ handle.getEnvironmentId();
 	}
 
 	long changeCount = 0;
 
 	public synchronized void save(boolean countSaves) {
 		if (indexResource == null) {
 			return;
 		}
 		if (countSaves) {
 			changeCount++;
 			if (changeCount > SAVE_DELTA) {
 				changeCount = 0;
 			} else {
 				return;
 			}
 		}
 		try {
 			Map options = new HashMap();
 			options.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Boolean.TRUE);
 			indexResource.save(options);
 		} catch (IOException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public InputStream getCacheEntryAttribute(IFileHandle handle,
 			String attribute) {
 		if (handle == null) {
 			return null;
 		}
 		File file = null;
 		CacheEntry entry = null;
 		synchronized (this) {
 			entry = getEntry(handle);
 			EList<CacheEntryAttribute> attributes = entry.getAttributes();
 			for (CacheEntryAttribute cacheEntryAttribute : attributes) {
 				if (cacheEntryAttribute.getName().equals(attribute)) {
 					file = new File(cacheLocation.append(
 							cacheEntryAttribute.getLocation()).toOSString());
 					break;
 				}
 			}
 		}
 		if (file != null && file.exists()) {
 			try {
 				return new BufferedInputStream(new FileInputStream(file), 4096);
 			} catch (FileNotFoundException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 		}
 
 		return null;
 	}
 
 	public synchronized OutputStream getCacheEntryAttributeOutputStream(
 			IFileHandle handle, String attribute) {
 		File file = getEntryAsFile(handle, attribute);
 		try {
 			return new BufferedOutputStream(new FileOutputStream(file), 4096);
 		} catch (FileNotFoundException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	public File getEntryAsFile(IFileHandle handle, String attribute) {
 		CacheEntry entry = getEntry(handle);
 		File file = null;
 		EList<CacheEntryAttribute> attributes = entry.getAttributes();
 		for (CacheEntryAttribute cacheEntryAttribute : attributes) {
 			if (cacheEntryAttribute.getName().equals(attribute)) {
 				file = new File(cacheLocation.append(
 						cacheEntryAttribute.getLocation()).toOSString());
 				return file;
 			}
 		}
 
 		IPath location = generateNewLocation(handle.getPath(), handle
 				.getEnvironmentId());
 		CacheEntryAttribute attrEntry = CacheFactory.eINSTANCE
 				.createCacheEntryAttribute();
 		attrEntry.setLocation(location.toPortableString());
 		attrEntry.setName(attribute);
 		entry.getAttributes().add(attrEntry);
 		save(true);
 		file = new File(cacheLocation.append(location).toOSString());
 		return file;
 	}
 
 	private IPath generateNewLocation(IPath path, String environment) {
 		checksum.reset();
 		checksum.update(environment.getBytes());
 		IPath indexPath = cacheLocation.append(Long.toString(checksum
 				.getValue()));
 		File indexFolderFile = new File(indexPath.toOSString());
 		if (!indexFolderFile.exists()) {
 			indexFolderFile.mkdir();
 		}
 		checksum.reset();
 		checksum.update(path.removeLastSegments(1).toPortableString()
 				.getBytes());
 		IPath folder = indexPath.append(Long.toString(checksum.getValue()));
 		File folderFile = new File(folder.toOSString());
 		if (!folderFile.exists()) {
 			folderFile.mkdir();
 		}
 		CacheIndex index = getCacheIndex(environment);
 		IPath location = null;
 		long i = index.getLastIndex() + 1;
 		while (true) {
 			location = folder.append(Long.toString(i++) + ".idx");
 			File file = new File(location.toOSString());
 			if (!file.exists()) {
 				index.setLastIndex(i);
 				return location.removeFirstSegments(cacheLocation
 						.segmentCount());
 			}
 		}
 	}
 
 	public synchronized void removeCacheEntryAttributes(IFileHandle handle,
 			String attribute) {
 		CacheEntry entry = getEntry(handle);
 		EList<CacheEntryAttribute> attributes = entry.getAttributes();
 		for (CacheEntryAttribute cacheEntryAttribute : attributes) {
 			if (cacheEntryAttribute.getName().equals(attribute)) {
 				removeAttribute(cacheEntryAttribute);
 				attributes.remove(cacheEntryAttribute);
 				save(true);
 				return;
 			}
 		}
 	}
 
 	public synchronized void clearCacheEntryAttributes(IFileHandle handle) {
 		String key = makeKey(handle);
 		if (entryCache.containsKey(key)) {
 			CacheEntry entry = (CacheEntry) entryCache.get(key);
 			removeCacheEntry(entry, key);
 			save(true);
 		}
 	}
 
 	public synchronized void clear() {
 		initialize();
 		Set<String> keySet = new HashSet<String>(entryCache.keySet());
 		for (String k : keySet) {
 			removeCacheEntry(entryCache.get(k), k);
 		}
 		save(true);
 	}
 }
