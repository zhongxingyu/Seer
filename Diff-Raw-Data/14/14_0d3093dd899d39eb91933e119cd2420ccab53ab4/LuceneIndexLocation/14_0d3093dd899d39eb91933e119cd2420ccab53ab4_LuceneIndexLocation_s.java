 package com.gentics.cr.lucene.indexer.index;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.store.Directory;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessorFactory;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessorToken;
 import com.gentics.cr.lucene.information.SpecialDirectoryRegistry;
 import com.gentics.cr.util.Constants;
 import com.gentics.cr.util.indexing.IndexLocation;
 
 /**
  * 
  * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
  * 
  * @version $Revision: 131 $
  * @author $Author: supnig@constantinopel.at $
  * 
  */
 
 public abstract class LuceneIndexLocation extends com.gentics.cr.util.indexing.IndexLocation {
 	// STATIC MEMBERS
 	protected static final Logger log = Logger.getLogger(LuceneIndexLocation.class);
 
 	protected String name = null;
 
 	private boolean registered = false;
 
 	private IndexAccessorToken accessorToken = null;
 	
 	protected boolean useFacets = false;
 
 	protected Analyzer getConfiguredAnalyzer() {
 		return LuceneAnalyzerFactory.createAnalyzer((GenericConfiguration) config);
 	}
 
 	/**
 	 * Get a List of configured Attributes to be reversed.
 	 */
 	public List<String> getReverseAttributes() {
 		return LuceneAnalyzerFactory.getReverseAttributes((GenericConfiguration) config);
 	}
 
 	/**
 	 * Requests an optimize command on the index.
 	 */
 	public void optimizeIndex() {
 		IndexAccessor indexAccessor = getAccessor();
 		IndexWriter indexWriter;
 		try {
 			indexWriter = indexAccessor.getWriter();
 			indexWriter.optimize(true);
 			indexAccessor.release(indexWriter);
 		} catch (IOException e) {
 			log.error(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Forcibly removes locks from the subsequent directories This code should
 	 * only be used by failure recovery code... when it is certain that no other
 	 * thread is accessing the index.
 	 */
 	public void forceRemoveLock() {
 		Directory[] dirs = this.getDirectories();
 		if (dirs != null) {
 			for (Directory dir : dirs) {
 				try {
 					if (IndexWriter.isLocked(dir)) {
 						IndexWriter.unlock(dir);
 					}
 				} catch (IOException e) {
 					log.error(e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks Lock and throws Exception if Lock exists
 	 * 
 	 * @throws LockedIndexException
 	 */
 	public void checkLock() throws LockedIndexException {
 		Directory[] dirs = this.getDirectories();
 
 		if (dirs != null) {
 			for (Directory dir : dirs) {
 				try {
 					if (IndexWriter.isLocked(dir))
 						throw new LockedIndexException();
 				} catch (IOException e) {
 					log.error(e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	// TODO should be protected, is public because it is used in
 	// IndexLocation#createNewIndexLocation
 	/**
 	 * @param config
 	 * 
 	 */
 	public LuceneIndexLocation(CRConfig config) {
 		super(config);
 		accessorToken = IndexAccessorFactory.getInstance().registerConsumer();
 		name = config.getName();
 	}
 
 	/**
 	 * Gets the name of the index location.
 	 * @return name as String
 	 */
 	public String getName() {
 		return name;
 	}
 
 	public static synchronized LuceneIndexLocation getIndexLocation(CRConfig config) {
 		IndexLocation genericIndexLocation = IndexLocation.getIndexLocation(config);
 		if (genericIndexLocation instanceof LuceneIndexLocation) {
 			return (LuceneIndexLocation) genericIndexLocation;
 		} else {
 			log.error("IndexLocation is not created for Lucene. Using the " + CRLuceneIndexJob.class.getName()
 					+ " requires that you use the " + LuceneIndexLocation.class.getName()
 					+ ". You can configure another Job by setting the " + IndexLocation.UPDATEJOBCLASS_KEY
 					+ " key in your config.");
 			return null;
 		}
 	}
 
 	protected static String getFirstIndexLocation(CRConfig config) {
 		String path = "";
 		GenericConfiguration locs = (GenericConfiguration) config.get(INDEX_LOCATIONS_KEY);
 		if (locs != null) {
 			Map<String, GenericConfiguration> locationmap = locs.getSortedSubconfigs();
 			if (locationmap != null) {
 				for (GenericConfiguration locconf : locationmap.values()) {
 					String p = locconf.getString(INDEX_PATH_KEY);
 					if (p != null && !"".equals(p)) {
 						path = p;
 						return path;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Create a Lucene directory from a config (index path must be configured)
 	 * 
 	 * @param config
 	 */
 	public static Directory createDirectory(CRConfig config) {
 		String loc = getFirstIndexLocation(config);
 		return createDirectory(loc);
 	}
 
 	/**
 	 * Create a Lucene directory from a path.
 	 * 
 	 * @param indexLocation location
 	 * @return directory
 	 */
 	public static Directory createDirectory(final String indexLocation) {
 		return LuceneDirectoryFactory.getDirectory(indexLocation);
 	}
 
 	/**
 	 * @return the directory used by this index location.
 	 */
 	protected abstract Directory[] getDirectories();
 
 	public synchronized void registerDirectoriesSpecial() {
 		if (!registered) {
 			for (Directory d : getDirectories()) {
 				SpecialDirectoryRegistry.getInstance().register(d);
 			}
 			registered = true;
 		}
 
 	}
 
 	/**
 	 * Get number of documents in Index.
 	 * 
 	 * @return doccount number of documents in index
 	 */
 	public abstract int getDocCount();
 
 	protected abstract IndexAccessor getAccessorInstance();
 
 	/**
 	 * Returns an index Accessor, which can be used to share access to an index
 	 * over multiple threads.
 	 * 
 	 * @return IndexAccessor for this index
 	 */
 	public final IndexAccessor getAccessor() {
 		IndexAccessor indexAccessor = getAccessorInstance();
 		reopenCheck(indexAccessor, getTaxonomyAccessor());
 		return indexAccessor;
 	}
 
 	/**
 	 * Checks for reopen file and reopens indexAccessor.
 	 * 
 	 * @param indexAccessor
 	 *            indexAccessor for the index
 	 * @return true if indexAccessor has been reopened
 	 */
 	public abstract boolean reopenCheck(IndexAccessor indexAccessor, TaxonomyAccessor taxonomyAccessor);
 
 	/**
 	 * get the date when the index was modified.
 	 * 
 	 * @return last modified date
 	 */
 	public abstract Date lastModified();
 
 	/**
 	 * get index size in Bytes.
 	 * 
 	 * @return index size in Bytes
 	 */
 	public abstract long indexSize();
 
 	/**
 	 * get the index size in MegaBytes.
 	 * 
 	 * @return index size in MegaBytes
 	 */
 	public final double indexSizeMB() {
 		return indexSize() * Constants.MEGABYTES_PER_BYTE;
 	}
 
 	/**
 	 * Tests if the IndexLocation contains an existing Index and returns true if
 	 * it does.
 	 * 
 	 * @return true if index exists, otherwise false
 	 */
 	public final boolean isContainingIndex() {
 		return getDocCount() > 0;
 	}
 
 	@Override
 	public void finalize() {
 		if (registered) {
 			for (Directory d : getDirectories()) {
 				SpecialDirectoryRegistry.getInstance().unregister(d);
 			}
 		}
 		IndexAccessorFactory.getInstance().releaseConsumer(accessorToken);
 	}
 
 	/**
 	 * True when two LuceneIndexLocations have the same hashCode
 	 */
 	@Override
 	public boolean equals(Object o) {
 		if (o == null) {
 			return false;
 		}
 
 		if (this == o) {
 			return true;
 		}
 
 		if (!o.getClass().equals(this.getClass())) {
 			return false;
 		}
 		LuceneIndexLocation that = (LuceneIndexLocation) o;
 
 		if (this.hashCode() == that.hashCode()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public abstract int hashCode();
 	
 	protected abstract TaxonomyAccessor getTaxonomyAccessorInstance();
 
 	/**
 	 * Returns a {@link TaxonomyAccessor}, which can be used to share access to
	 * a taxonomy over multiple threads.
 	 * 
	 * @return taxonomy accessor for this index
 	 */
 	public final TaxonomyAccessor getTaxonomyAccessor() {
		TaxonomyAccessor accessor = getTaxonomyAccessorInstance();
		return accessor;
 	}
 
 	/**
 	 * indicates if facets are used for this index location and a taxonomy has
 	 * to be maintained
 	 * 
 	 * @return true if facets are used
 	 */
 	public boolean useFacets() {
 		return useFacets;
 	}
 
 }
