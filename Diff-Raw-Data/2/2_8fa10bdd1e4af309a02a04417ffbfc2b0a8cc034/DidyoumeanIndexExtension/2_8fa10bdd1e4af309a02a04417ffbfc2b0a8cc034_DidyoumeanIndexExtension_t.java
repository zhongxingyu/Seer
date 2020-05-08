 package com.gentics.cr.lucene.didyoumean;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.search.spell.CustomSpellChecker;
 import org.apache.lucene.store.Directory;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.events.Event;
 import com.gentics.cr.events.EventManager;
 import com.gentics.cr.events.IEventReceiver;
//import com.gentics.cr.lucene.autocomplete.Autocompleter;
 import com.gentics.cr.lucene.events.IndexingFinishedEvent;
 import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
 import com.gentics.cr.lucene.information.SpecialDirectoryRegistry;
 import com.gentics.cr.util.indexing.AbstractIndexExtension;
 import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
 import com.gentics.cr.util.indexing.IReIndexStrategy;
 import com.gentics.cr.util.indexing.IndexExtension;
 import com.gentics.cr.util.indexing.IndexLocation;
 import com.gentics.cr.util.indexing.ReIndexNoSkipStrategy;
 import com.gentics.cr.util.indexing.SimpleIndexJobAdderThread;
 
 /**
  * This {@link IndexExtension} creates and maintains an autocomplete-index. The
  * {@link DidYouMeanProvider} uses the index created by this extension to provide
  * didyoumean search suggestions.
  * 
  */
 public class DidyoumeanIndexExtension extends AbstractIndexExtension implements
 		IEventReceiver {
 	protected static final Logger log = Logger
 			.getLogger(DidyoumeanIndexExtension.class);
 
 	private static final String REINDEX_JOB = "reIndex";
 	private static final String[] jobs = { REINDEX_JOB };
 
 	private CRConfig config;
 
 	private IReIndexStrategy reindexStrategy;
 
 	private boolean subscribeToIndexFinished = false;
 
 	private IndexLocation callingIndexLocation;
 
 	private LuceneIndexLocation source;
 	private Directory didyoumeanDirectory;
 
 	private static final String SOURCE_INDEX_KEY = "srcindexlocation";
 
 	private static final String DIDYOUMEAN_INDEX_KEY = "didyoumeanlocation";
 
 	private static final String DIDYOUMEAN_FIELD_KEY = "didyoumeanfields";
 
 	private static final String DIDYOUMEAN_MIN_DISTANCESCORE = "didyoumeanmindistancescore";
 
 	private static final String DIDYOUMEAN_MIN_DOCFREQ = "didyoumeanmindocfreq";
 
 	public static final String REINDEXSTRATEGYCLASS_KEY = "reindexStrategyClass";
 
 	public static final String DIDYOUMEAN_SUBSCRIBE_TO_INDEX_FINISHED = "reindexOnCRIndexFinished";
 
 	private String didyoumeanfield = "all";
 
 	private CustomSpellChecker spellchecker = null;
 
 	private boolean all = false;
 
 	private Collection<String> dym_fields = null;
 
 	private Float minDScore = (float) 0.0;
 
 	private Integer minDFreq = 0;
 
 	/**
 	 * The constructor is called in the {@link IndexLocation}
 	 * 
 	 * @see AbstractIndexExtension#AbstractIndexExtension(CRConfig,
 	 *      IndexLocation)
 	 * @param config
 	 * @param callingLocation
 	 */
 	public DidyoumeanIndexExtension(CRConfig config,
 			IndexLocation callingLocation) {
 		super(config, callingLocation);
 		this.config = config;
 		this.callingIndexLocation = callingLocation;
 
 		GenericConfiguration src_conf = (GenericConfiguration) config
 				.get(SOURCE_INDEX_KEY);
 		CRConfigUtil src_conf_util = new CRConfigUtil(src_conf,
 				"SOURCE_INDEX_KEY");
 		if (src_conf_util.getPropertySize() > 0) {
 			source = LuceneIndexLocation.getIndexLocation(src_conf_util);
 		}
 		if (source == null) {
 			source = (LuceneIndexLocation) callingLocation;
 		}
 
 		GenericConfiguration didyou_conf = (GenericConfiguration) config
 				.get(DIDYOUMEAN_INDEX_KEY);
 		didyoumeanDirectory = LuceneIndexLocation
 				.createDirectory(new CRConfigUtil(didyou_conf,
 						DIDYOUMEAN_INDEX_KEY));
 		SpecialDirectoryRegistry.getInstance().register(didyoumeanDirectory);
 
 		didyoumeanfield = config.getString(DIDYOUMEAN_FIELD_KEY,
 				didyoumeanfield);
 		minDScore = config.getFloat(DIDYOUMEAN_MIN_DISTANCESCORE, (float) 0.0);
 		minDFreq = config.getInteger(DIDYOUMEAN_MIN_DOCFREQ, 0);
 
 		// FETCH DYM FIELDS
 		if (this.didyoumeanfield.equalsIgnoreCase("ALL")) {
 			all = true;
 		} else if (this.didyoumeanfield.contains(",")) {
 			String[] arr = this.didyoumeanfield.split(",");
 			dym_fields = new ArrayList<String>(Arrays.asList(arr));
 		} else {
 			dym_fields = new ArrayList<String>(1);
 			dym_fields.add(this.didyoumeanfield);
 		}
 		reindexStrategy = initReindexStrategy(config);
 
 		// CHECK FOR EXISTING LOCK AND REMOVE IT
 		synchronized (this) {
 			try {
 				if (IndexWriter.isLocked(didyoumeanDirectory)) {
 					IndexWriter.unlock(didyoumeanDirectory);
 				}
 			} catch (IOException e) {
 				log.error(e.getMessage(), e);
 			}
 		}
 
 		subscribeToIndexFinished = config.getBoolean(
 				DIDYOUMEAN_SUBSCRIBE_TO_INDEX_FINISHED,
 				subscribeToIndexFinished);
 
 		if (subscribeToIndexFinished) {
 			EventManager.getInstance().register(this);
 		}
 
 		log.debug("Succesfully registered DidyoumeanIndexExtension");
 	}
 
 	/**
 	 * EventHandler: subscribes to the
 	 * {@link IndexingFinishedEvent#INDEXING_FINISHED_EVENT_TYPE}. <br>
 	 * If enabled in the config this method adds a reIndexing Job to the queue
 	 * of the {@link IndexLocation} which fired the event
 	 */
 	public void processEvent(Event event) {
 		if (subscribeToIndexFinished
 				&& IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE
 						.equals(event.getType())) {
 			IndexLocation il = (IndexLocation) event.getData();
 			if (!reindexStrategy.skipReIndex(il)) {
 				AbstractUpdateCheckerJob job = (AbstractUpdateCheckerJob) new DidyoumeanIndexJob(
 						config, il, this);
 				SimpleIndexJobAdderThread thread = new SimpleIndexJobAdderThread(
 						il, job);
 				thread.start();
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#stop()
 	 */
 	@Override
 	public void stop() {
 		source.stop();
 		try {
 			didyoumeanDirectory.close();
 		} catch (IOException e) {
 			log.error("Could not close didyoumean Directory", e);
 		}
 		if (subscribeToIndexFinished) {
 			EventManager.getInstance().unregister(this);
 		}
 	}
 
 	/**
 	 * Initialize a config class for the periodical execution flag of the
 	 * indexer. If init of the configured class fails, a fallback class is
 	 * returned.
 	 * 
 	 * @return configclass
 	 * @param config
 	 */
 	private IReIndexStrategy initReindexStrategy(final CRConfig config) {
 		String className = config.getString(REINDEXSTRATEGYCLASS_KEY);
 
 		if (className != null && className.length() != 0) {
 			try {
 				Class<?> clazz = Class.forName(className);
 				Constructor<?> constructor = clazz
 						.getConstructor(CRConfig.class);
 				return (IReIndexStrategy) constructor.newInstance(config);
 			} catch (Exception e) {
 				log.warn("Cound not init configured "
 						+ REINDEXSTRATEGYCLASS_KEY + ": " + className, e);
 			}
 		}
 		return new ReIndexNoSkipStrategy(config);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.gentics.cr.util.indexing.AbstractIndexExtension#addJob(java.lang.
 	 * String)
 	 */
 	@Override
 	public void addJob(String name) throws NoSuchMethodException {
 		addJob(name, null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.gentics.cr.util.indexing.AbstractIndexExtension#addJob(java.lang.
 	 * String, com.gentics.cr.util.indexing.IndexLocation)
 	 */
 	@Override
 	public void addJob(String name, IndexLocation indexLocation)
 			throws NoSuchMethodException {
 		IndexLocation actualLocation = callingIndexLocation;
 		if (indexLocation != null) {
 			actualLocation = indexLocation;
 		}
 		if (REINDEX_JOB.equalsIgnoreCase(name)) {
 			AbstractUpdateCheckerJob job = (AbstractUpdateCheckerJob) new DidyoumeanIndexJob(
 					this.config, actualLocation, this);
 			SimpleIndexJobAdderThread thread = new SimpleIndexJobAdderThread(
 					actualLocation, job);
 			thread.run();
 		} else {
 			throw new NoSuchMethodException("No Job-Method by the name: "
 					+ name);
 		}
 	}
 
 	public boolean isAll() {
 		return all;
 	}
 
 	public void setAll(boolean all) {
 		this.all = all;
 	}
 
 	public Collection<String> getDym_fields() {
 		return dym_fields;
 	}
 
 	public void setDym_fields(Collection<String> dym_fields) {
 		this.dym_fields = dym_fields;
 	}
 
 	public Directory getDidyoumeanDirectory() {
 		return didyoumeanDirectory;
 	}
 
 	public LuceneIndexLocation getSourceLocation() {
 		return source;
 	}
 
 	public CustomSpellChecker getSpellchecker() {
 		return spellchecker;
 	}
 
 	public void setSpellchecker(CustomSpellChecker spellchecker) {
 		this.spellchecker = spellchecker;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#getJobs()
 	 */
 	@Override
 	public String[] getJobs() {
 		return jobs;
 	}
 
 	public Float getMinDScore() {
 		return minDScore;
 	}
 
 	public Integer getMinDFreq() {
 		return minDFreq;
 	}
 
 }
