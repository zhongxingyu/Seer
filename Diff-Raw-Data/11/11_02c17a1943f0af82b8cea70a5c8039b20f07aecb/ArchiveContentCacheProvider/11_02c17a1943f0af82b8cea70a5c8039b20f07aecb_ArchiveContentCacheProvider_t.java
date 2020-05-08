 package org.eclipse.dltk.core.caching;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.caching.cache.CacheEntry;
 import org.eclipse.dltk.core.caching.cache.CacheEntryAttribute;
 import org.eclipse.dltk.core.caching.cache.CacheIndex;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 /**
  * This cache provider checks for folder .index files and load such files into
  * required cache.
  * 
  * @author Andrei Sobolev
  */
 public class ArchiveContentCacheProvider implements IContentCacheProvider {
 
 	private File archiveTempFile;
 	private IContentCache cache;
 
 	public ArchiveContentCacheProvider() {
 		IPath stateLocation = DLTKCore.getDefault().getStateLocation();
 		IPath arhiveTempLocation = stateLocation.append("archive_cache_temp");
 		archiveTempFile = new File(arhiveTempLocation.toOSString());
 		if (!archiveTempFile.exists()) {
 			archiveTempFile.mkdir();
 		}
 		// zipProcessor.start();
 	}
 
 	public InputStream getAttributeAndUpdateCache(IFileHandle handle,
 			String attribute) {
 		IFileHandle parent = handle.getParent();
 		String DLTK_INDEX_FILE = ".dltk.index";
 		IFileHandle indexFile = parent.getChild(DLTK_INDEX_FILE);
 		if (indexFile != null && indexFile.exists()) {
 			String stamp = cache.getCacheEntryAttributeString(indexFile,
 					"timestamp");
 			String fStamp = Long.toString(indexFile.lastModified());
 			if (stamp != null) {
 				if (fStamp.equals(stamp)) {
 					return null;
 				}
 			}
 			// Copy zip file into metadata temporaty location
 			try {
 				File zipFileHandle = cache.getEntryAsFile(indexFile, "handle");
 
 				if (!zipFileHandle.exists()) {
 					Util.copy(zipFileHandle, new BufferedInputStream(indexFile
 							.openInputStream(new NullProgressMonitor()), 4096));
 				}
 				ZipFile zipFile = new ZipFile(zipFileHandle);
 
 				ZipEntry entry = zipFile.getEntry(".index");
 				Resource indexResource = new XMIResourceImpl(URI
 						.createURI("dltk_cache://zipIndex"));
 				indexResource.load(zipFile.getInputStream(entry), null);
 				EList<EObject> contents = indexResource.getContents();
 				// InputStream resultStream = null;
 				for (EObject eObject : contents) {
 					CacheIndex cacheIndex = (CacheIndex) eObject;
 					EList<CacheEntry> entries = cacheIndex.getEntries();
 					for (CacheEntry cacheEntry : entries) {
 						String path = cacheEntry.getPath();
 						IFileHandle entryHandle = new WrapTimeStampHandle(
 								parent.getChild(path), cacheEntry
 										.getTimestamp());
 						// long lastModified = entryHandle.lastModified();
 						// if (entryHandle.exists()
 						// && lastModified == cacheEntry
 						// .getTimestamp()) {
 						EList<CacheEntryAttribute> attributes = cacheEntry
 								.getAttributes();
 						for (CacheEntryAttribute cacheEntryAttribute : attributes) {
 							OutputStream stream = null;
 							// ByteArrayOutputStream out = null;
 
 							stream = cache.getCacheEntryAttributeOutputStream(
 									entryHandle, cacheEntryAttribute.getName());
 							// if (handle.equals(entryHandle)
 							// && cacheEntryAttribute.getName().equals(
 							// attribute)) {
 							// out = new ByteArrayOutputStream();
 							// }
 							String location = cacheEntryAttribute.getLocation();
 							ZipEntry zipEntry = zipFile.getEntry(location);
 							zipFile.getInputStream(zipEntry);
 							InputStream inputStream;
 							try {
 								inputStream = zipFile.getInputStream(zipEntry);
 								// if (out != null) {
 								// Util.copy(inputStream, stream);
 								// // byte[] bytes = out.toByteArray();
 								// ByteArrayInputStream inp = new
 								// ByteArrayInputStream(
 								// bytes);
 								// // resultStream = new ByteArrayInputStream(
 								// // bytes);
 								// Util.copy(inp, stream);
 								// } else {
 								Util.copy(inputStream, stream);
 								// }
 								stream.close();
 								inputStream.close();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 						// }
 					}
 				}
				cache.setCacheEntryAttribute(indexFile, "timestamp", fStamp);
 				return cache.getCacheEntryAttribute(handle, attribute);
 				// return resultStream;
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 	public void setCache(IContentCache cache) {
 		this.cache = cache;
 	}
 }
