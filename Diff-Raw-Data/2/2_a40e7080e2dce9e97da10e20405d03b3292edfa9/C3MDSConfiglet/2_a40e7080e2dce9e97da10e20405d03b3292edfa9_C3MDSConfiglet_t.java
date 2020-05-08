 package de.zib.gndms.infra.configlet;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import de.zib.gndms.c3resource.C3ResourceReader;
 import de.zib.gndms.c3resource.jaxb.Site;
 import de.zib.gndms.c3resource.jaxb.Workspace;
 import de.zib.gndms.kit.config.MandatoryOptionMissingException;
 import de.zib.gndms.kit.configlet.RegularlyRunnableConfiglet;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 
 /**
  * A C3MDSConfiglet is used to bulid a {@code C3Catalog} out of a given {@code monitoring and discovery system (mds)} URL.
  *
  * Therefore the option {@code 'mdsURL'} must be set in the configuration map, before {@link #threadRun()} is invoked.
  *
  * Reading and building the C3Catalog is done concurrently,
  * if this object is started properly (using {@link #init(org.apache.commons.logging.Log, String, java.io.Serializable)}).
  *
  * The option {@code 'requiredPrefix'} can be set to allow just those {@link Workspace.Archive}'s oidPrefix
  * (see {@link de.zib.gndms.c3resource.jaxb.Workspace.Archive#getOidPrefix()} in the C3Catalog,
  * which start with the denoted prefix.
  *
  *
  * @see de.zib.gndms.kit.configlet.RunnableConfiglet
  * @author  try ste fan pla nti kow zib
  * @version $Id$
  *
  *          User: stepn Date: 06.11.2008 Time: 18:33:18
  */
 @SuppressWarnings({ "ClassNamingConvention", "ReturnOfCollectionOrArrayField" })
 public class C3MDSConfiglet extends RegularlyRunnableConfiglet {
     /**
      * the url of the monitoring and discovery system
      */
 	private String mdsUrl;
 	private String requiredPrefix;
 	private C3Catalog catalog;
 
     private ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock(true);
     private Condition newState = stateLock.writeLock().newCondition();
 
     private ReentrantLock runLock = new ReentrantLock(true);
 
 	@Override
 	protected void threadInit() {
         stateLock.writeLock().lock();
         try {
 		    super.threadInit();
 		    configProperties();
         }
         finally {
             newState.signalAll();
             stateLock.writeLock().unlock();
         }
 	}
 
 	@Override
 	public void update(@NotNull final Serializable data) {
         stateLock.writeLock().lock();
         try {
             super.update(data);    // Overridden method
             configProperties();
         }
         finally {
             newState.signalAll();
             stateLock.writeLock().unlock();
         }
 	}
 
 	private void configProperties() {
         if (! stateLock.writeLock().isHeldByCurrentThread())
             throw new IllegalStateException("Must be called withe stateLock.wrLock held!");
 		try {
 			mdsUrl = getMapConfig().getOption("mdsUrl");
 			requiredPrefix = getMapConfig().getOption("requiredPrefix", "");
 		}
 		catch ( MandatoryOptionMissingException e) {
 			getLog().warn(e);
         }
 	}
 
     /**
      * Writes the {@code C3Catalog} from a {@code mds} into {@link #catalog}
      *
      * @see super#threadRun()  
      */
 	@Override
 	protected void threadRun() {
        getLog().info("Refreshing C3MDSCatalog...");
         try {
             if (runLock.tryLock(0, TimeUnit.SECONDS))
                  try {
                      final C3ResourceReader reader = new C3ResourceReader();
                      final String curRequiredPrefix = getRequiredPrefix();
                      final InputStream inputStream = openMdsInputStream();
                      C3Catalog newCatalog = null;
                      // pointless since MDS includes dynamically generated timestamps...
                      //final MD5InputStream checkedStream = new MD5InputStream(inputStream);
                      try {
                          final Iterator<Site> sites = reader.readXmlSites(curRequiredPrefix, inputStream);
                          newCatalog = new C3Catalog(curRequiredPrefix, sites);
                      }
                      finally {
                          try {
                              inputStream.close();
                              if (newCatalog == null)
                                  getLog().warn("No new C3MDSCatalog was created (unknown reason)");
                              setCatalog(newCatalog);
                          }
                          catch (IOException e)
                              { getLog().warn("Error closing MDS stream; new catalog *not* set"); }
                      }
                      getLog().debug("Finished Refreshing C3MDSCatalog");
                  }
                  catch (Exception e) {
                      getLog().warn(e);
                  }
                  finally { runLock.unlock();}
             else {
                 try {
                     // This fills in the stacktrace
                     throw new RuntimeException(
                             "Couldnt acquire log for reading C3MDSCatalog... Please increment delay time! ");
                 } catch (RuntimeException e) {
                     getLog().warn(e);
                     getLog().debug("Aborted Refreshing C3MDSCatalog");
                 }
             }
         } catch (InterruptedException e) {
             getLog().warn(e);
         }
     }
 
     /**
      * Returns an InputStream, connected to the mds.
      * The URL is retrieved from {@link #getMdsUrl()}
      * 
      * @return
      *
      * @throws IOException
      */
 	private InputStream openMdsInputStream() throws IOException {
 		final String urlStr = getMdsUrl();
 		final URL url = new URL(urlStr);
 		if (url.getProtocol().startsWith("http")) {
 			getLog().debug("Loading C3MDSCatalog via http core...");
 			// if http use http client from apache commons
 			final HttpClient client = new DefaultHttpClient();
 			final HttpGet get = new HttpGet(urlStr);
 			get.addHeader(new BasicHeader("Pragma", "no-cache"));
 			get.addHeader(new BasicHeader("Cache-Control",
 			                              "private, no-store, no-cache, must-revalidate, max-age=0"));
 			final HttpResponse resp = client.execute(get);
 			return resp.getEntity().getContent();
 		}
 		else {
 			getLog().debug("Loading C3MDSCatalog via java.net.URL.openStream...");
 			// defer to java-built in url handling otherwise
 			return url.openStream();
 		}
 	}
 
 
     /**
      * Waits until a {@code C3Catalog} is avaible and returns it.
      *
      * @return the C3Catalog of the Configlet
      */
 	public C3Catalog getCatalog() {
         boolean first = true;
         C3Catalog snapshot;
         do {
             if (first)
                 first = false;
             else
                 try { newState.await(); }
                 catch (InterruptedException ignored) {}
             stateLock.readLock().lock();
             try {
                 snapshot = catalog;
             }
             finally { stateLock.readLock().unlock(); }
         }
         while (snapshot == null);
         return snapshot;
 	}
 
 
 	private void setCatalog(final C3Catalog newCatalogParam) {
         stateLock.writeLock().lock();
         try {
             catalog = newCatalogParam;
             newState.signalAll();
         }
         finally { stateLock.writeLock().unlock(); }
 	}
 
 
 	@Override
 	protected void threadStop() {
 		getThread().interrupt();
 	}
 
 
 	public String getMdsUrl() {
         stateLock.readLock().lock();
         try {
 		    return mdsUrl;
         }
         finally { stateLock.readLock().unlock(); }
 	}
 
 
	public String getRequiredPrefix() {
         stateLock.readLock().lock();
         try {
             return requiredPrefix;
         }
         finally { stateLock.readLock().unlock(); }
 	}
 
 
     /**
      * A C3Catalog contains several Maps,
      * storing {@link Site}s and their corresponding {@link Workspace}s and {@link Workspace.Archive}s.
      *
      */
 	public static class C3Catalog {
 		/* forward maps */
 		private Map<String, Site> siteById = Maps.newConcurrentHashMap();
 		private Map<String, Set<Workspace.Archive>> archivesByOid = Maps.newTreeMap();
 
 		/* reverse maps */
 		private Map<Workspace.Archive, Workspace> workspaceByArchive = Maps.newIdentityHashMap();
 		private Map<Workspace, Site> siteByWorkspace = Maps.newIdentityHashMap();
 
 		private final String requiredPrefix;
 
 		@SuppressWarnings({ "FeatureEnvy" })
 		public C3Catalog(String requiredPrefixParam, Iterator<Site> sites) {
 			requiredPrefix = requiredPrefixParam;
 			fillMaps(sites);
 			protectMaps();
 		}
 
         /**
          * Fills the maps {@link #siteById}, {@link #siteByWorkspace}, {@link #workspaceByArchive}, {@link #archivesByOid} 
          * for all sites and their
          * corresponding Objects which are collected by the iterator {@code sites}.
          *
          * Every site is stored in {@link #siteById}, using its own id (see {@link Site#getId()}), but without the
          * prefix {@link #requiredPrefix}.
          * A {@code Workspace} is assigned to its {@code site} by {@code siteByWorkspace}.
          * The same applies to an Archive of an Workspace, using {@code workspaceByArchive}.
          *
          * Every {@code object id prefix} (see {@link Workspace.Archive#getOidPrefix()})
          * is assigned to all {@code Archive}s, where it is included ({@link #archivesByOid}).
          *
          * Every {@code object id prefix} is shortened by the prefix {@link #requiredPrefix}, before it is stored in the map.
          * Besides that, the entries of the list {@link Workspace.Archive#oidPrefix} are replaced with the new shorted Strings.
          * If an oid prefix does not start with {@link #requiredPrefix} it is removed from the {@code oidPrefix} list of
          * the corresponding {@code Workspace.Archive}.
          *
          * @param sites an Iterator containing {@link Site}s to be registered in the system.
          */
 		@SuppressWarnings({ "FeatureEnvy", "ObjectAllocationInLoop" })
 		private void fillMaps(final Iterator<Site> sites) {
             // Unused at the moment
 			// final Set<String> allOidPrefixes = Sets.newTreeSet();
 			while (sites.hasNext()) {
 				final Site site = sites.next();
 				// remove prefix
 				site.setId(site.getId().substring(requiredPrefix.length()));
 				siteById.put(site.getId(), site);
 				for (Workspace ws : site.getWorkspace()) {
 					siteByWorkspace.put(ws, site);
 
 					for (Workspace.Archive archive : ws.getArchive()) {
 						workspaceByArchive.put(archive, ws);
 
 						final List<String> newPrefixes = new LinkedList<String>();
 						for (final String curOidPrefix : archive.getOidPrefix()) {
 							if (curOidPrefix.startsWith(requiredPrefix)) {
 								final String oidPrefix = curOidPrefix.substring(requiredPrefix.length());
                                 // Unused at the moment
 								// allOidPrefixes.add(oidPrefix);
 								final Set<Workspace.Archive> set;
 								if (archivesByOid.containsKey(oidPrefix))
 									set  = archivesByOid.get(oidPrefix);
 								else {
 									final Map<Workspace.Archive, Boolean> amap = Maps.newConcurrentHashMap();
 								  set = Sets.newSetFromMap(amap);
 									archivesByOid.put(oidPrefix, set);
 								}
 								set.add(archive);
 								newPrefixes.add(oidPrefix);
 							}
 						}
 						archive.getOidPrefix().clear();
 						archive.getOidPrefix().addAll(newPrefixes);
 					}
 				}
 			}
 		}
 
         /**
          * Makes all maps of a C3Catalog instance immutable
          */
 		private void protectMaps() {
 			siteById = immutableMap(siteById);
 			archivesByOid = immutableMap(archivesByOid);
 			workspaceByArchive = immutableMap(workspaceByArchive);
 			siteByWorkspace = immutableMap(siteByWorkspace);
 		}
 
 		private static <K, V> Map<K, V> immutableMap(final Map<K, V> map) {
 			return Collections.unmodifiableMap(map);
 		}
 
 
 		public Map<String, Site> getSiteById() {
 			return siteById;
 		}
 
 
 		public Map<String, Set<Workspace.Archive>> getArchivesByOid() {
 			return archivesByOid;
 		}
 
 
 		public Map<Workspace.Archive, Workspace> getWorkspaceByArchive() {
 			return workspaceByArchive;
 		}
 
 
 		public Map<Workspace, Site> getSiteByWorkspace() {
 			return siteByWorkspace;
 		}
 
 
         /**
          * Returns the set of {@code Workspace.Archive}s to which {@code oidPrefixIn} or a prefix of it has been mapped.
          * If {@code oidPrefixIn} could not be found in {@link #archivesByOid}, the lookup is repeated with the String,
          * shortened by last character. This procedure is repeated until a prefix has been found as key of {@link #archivesByOid}.
          * Otherwise an exception will be thrown.
          *
          * @param oidPrefixIn either oidPrefixIn or a prefix of it, being a key for the map {@link #archivesByOid}
          *
          * @return the assigned set of {@code Archives} for the key oidPrefixIn or a prefix of the String using the map {@code archivesByOid}
          */
 		public @NotNull Set<Workspace.Archive> getArchivesByOid(final String oidPrefixIn) {
 			if (oidPrefixIn != null) {
 				final String oidPrefix = oidPrefixIn.trim();
 				for (int i = oidPrefix.length(); i > 0; i--) {
 					final String key = oidPrefix.substring(0, i);
 					if (archivesByOid.containsKey(key))
 						return archivesByOid.get(key);
 				}
 			}
 			throw new IllegalArgumentException("No archive found with oidPrefix: " +
 				  (oidPrefixIn == null ? "(null)" : oidPrefixIn));
 		}
 
         /**
          * Returns the set of all {@code Workspace.Archive}s, which are contained in an {@code Workspace} belong to the {@code Site},
          * which corresponds to {@code siteId} and where {@code oidPrefixIn} is a prefix of an {@code Archive}'s oid prefix.
          *
          * Note: the set contains only those archives, which have an oidprefix, being a prefix of {@code oidPrefixIn}
          * with maximum lenght.
          * TODO: check if maximum lenght is need, or should all Archives returned containing an oidPrefix, being an oidPrefixIn prefix. 
          *
          *
          * @param siteId constraint for the archives.
          *      Only Archives contained in a {@code Workspace} of the site corresponding to siteID are used for the lookup
          * @param oidPrefixIn an archive is added to the list, if it contains an oid prefix, being a prefix of oidPrefixIn
          *
          * @return a set of Archives corresponding to a chosen site, containing an oid prefix being an prefix of oidPrefixIn
          */
 		@SuppressWarnings({ "OverlyNestedMethod" })
 		public @NotNull Set<Workspace.Archive> getArchivesByOid(final @Nullable String siteId, final @NotNull String oidPrefixIn) {
 			if (siteId == null || siteId.trim().length() == 0)
 				return getArchivesByOid(oidPrefixIn);
 			else {
 				Site site = siteById.get(siteId.trim());
 				if (site == null)
 					throw new IllegalArgumentException("Unknown site: " + siteId);
 				else {
 					final String oidPrefix = oidPrefixIn.trim();
 					final Set<Workspace.Archive> result = Sets.newHashSet();
 					if (oidPrefix.length() > 0) {
 						int max_len = 0;
 						for (Workspace w: site.getWorkspace()) {
 							for (Workspace.Archive a: w.getArchive()) {
 								for (String prefix: a.getOidPrefix()) {
 									if (oidPrefix.startsWith(prefix)) {
 										final int cur_len = prefix.length();
 										if (cur_len > 0) {
 											if (max_len < cur_len) {
 												result.clear();
 												result.add(a);
 												max_len = cur_len;
 											}
 											if (max_len == cur_len) {
 												result.add(a);
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 					if (result.size() < 1)
 						throw new IllegalArgumentException("No archive found with oidPrefix: " + oidPrefix);
 					return result;
 				}
 			}
 		}
 
 		public @NotNull Set<Workspace.Archive> getArchivesByOids(final @Nullable String siteId, final @NotNull String[] oidPrefices) {
 			return getArchivesByOid(siteId, sharedPrefix(oidPrefices));
 		}
 
 
         /**
          * Returns {@code getArchivesByOid(prefix)},
          * where prefix is the longest common prefix of all Strings in {@code oidPrefices}.
          *
          * @param oidPrefices an array of oid prefices
          * @return {@code getArchivesByOid(prefix)},
          *         where prefix is the longest common prefix of all Strings in {@code oidPrefices}
          */
 		public @NotNull Set<Workspace.Archive> getArchivesByOids(final String[] oidPrefices) {
 			return getArchivesByOid(sharedPrefix(oidPrefices));
 		}
 
         /**
          * Returns the longest common prefix contained in all Strings of the array
          *
          * @param strings several Strings, whose longstest common prefix is seeked
          *
          * @return the longest common prexif contained in all String of the array
          */
 		private static @NotNull String sharedPrefix(String[] strings) {
 			if (strings == null || strings.length == 0) return "";
 			String prefix = strings[0].trim();
 			if (prefix == null)	return "";
 			for (int i = 1; i < strings.length; i++) {
 				String str = strings[i].trim();
 				if (str == null) return "";
 				prefix = sharedPrefix(prefix, str);
 				if (prefix.length() == 0) return "";
 			}
 			return prefix;
 		}
 
 
         /**
          * Returns the longest common prefix for two Strings.
          *
          * @param smaller a String to be compared with another String.
          * @param larger a second String to be compared with the first String.
          * @return the longest common prefix in both Strings
          */
 		@SuppressWarnings({ "TailRecursion" })
 		private static @NotNull String sharedPrefix(
 			  final @NotNull String smaller, final @NotNull String larger) {
 			if (larger.length() < smaller.length()) return sharedPrefix(larger, smaller);
 			for (int i = 0; i < smaller.length(); i++) {
 				if (smaller.charAt(i) != larger.charAt(i)) {
 					if (i == 0)
 						return "";
 					else
 						return smaller.substring(0, i - 1);
 				}
 			}
 			return smaller;
 		}
 	}
 }
