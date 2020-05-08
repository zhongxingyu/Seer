 package de.zib.gndms.infra.configlet;
 
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
 
 
 /**
  * ThingAMagic.
  *
  * @author Stefan Plantikow<plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 06.11.2008 Time: 18:33:18
  */
 @SuppressWarnings({ "ClassNamingConvention", "ReturnOfCollectionOrArrayField" })
 public class C3MDSConfiglet extends RegularlyRunnableConfiglet {
 	private String mdsUrl;
 	private String requiredPrefix;
 
 	private C3Catalog catalog;
 
 	@Override
 	protected synchronized void threadInit() {
 		super.threadInit();
 		configProperties();
 	}
 
 
 
 	@Override
 	public synchronized void update(@NotNull final Serializable data) {
 		super.update(data);    // Overridden method
 		configProperties();
 	}
 
 
 	private synchronized void configProperties() {
 		try {
 			mdsUrl = getMapConfig().getOption("mdsUrl");
 			requiredPrefix = getMapConfig().getOption("requiredPrefix", "");
 		}
 		catch ( MandatoryOptionMissingException e) {
 			getLog().warn(e);
 		}
 	}
 
 	@Override
 	protected synchronized void threadRun() {
 		try {
 			getLog().info("Refreshing C3MDSCatalog...");
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
 		catch (RuntimeException e) {
 			getLog().warn(e);
 		}
 		catch (IOException e) {
 			getLog().warn(e);
 		}
 	}
 
 
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
 
 
 	public synchronized C3Catalog getCatalog() {
		while (getCatalog() == null)
 			try {
 				wait();
 			}
 			catch (InterruptedException e) {
 				/* ignored */
 			}
 		return catalog;
 	}
 
 
 	private synchronized void setCatalog(final C3Catalog newCatalogParam) {
 		catalog = newCatalogParam;
 		notifyAll();
 	}
 
 
 	@Override
 	protected void threadStop() {
 		getThread().interrupt();
 	}
 
 
 	public synchronized String getMdsUrl() {
 		return mdsUrl;
 	}
 
 
 	public synchronized String getRequiredPrefix() {
 		return requiredPrefix;
 	}
 
 
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
 
 
 		@SuppressWarnings({ "FeatureEnvy", "ObjectAllocationInLoop" })
 		private void fillMaps(final Iterator<Site> sites) {
 			final Set<String> allOidPrefixes = Sets.newTreeSet();
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
 								allOidPrefixes.add(oidPrefix);
 								final Set<Workspace.Archive> set;
 								if (archivesByOid.containsKey(oidPrefix))
 									set  = archivesByOid.get(oidPrefix);
 								else {
 									set = Sets.newConcurrentHashSet();
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
 
 		public @NotNull Set<Workspace.Archive> getArchivesByOids(final String[] oidPrefices) {
 			return getArchivesByOid(sharedPrefix(oidPrefices));
 		}
 
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
