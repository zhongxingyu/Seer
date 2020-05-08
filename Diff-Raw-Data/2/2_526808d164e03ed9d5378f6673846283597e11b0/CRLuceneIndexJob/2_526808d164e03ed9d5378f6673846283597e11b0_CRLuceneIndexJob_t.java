 package com.gentics.cr.lucene.indexer.index;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.document.Field.TermVector;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermDocs;
 
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRError;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.events.EventManager;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.lucene.events.IndexingFinishedEvent;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexer.IndexerUtil;
 import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
 import com.gentics.cr.monitoring.MonitorFactory;
 import com.gentics.cr.monitoring.UseCase;
 import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
 import com.gentics.cr.util.indexing.IndexLocation;
 /**
  * CRLuceneIndexJob handles the indexing of a Gentics ContentRepository into
  * Lucene.
  * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
  * @version $Revision: 180 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRLuceneIndexJob extends AbstractUpdateCheckerJob {
 	/**
 	 * static log4j {@link Logger} to log errors and debug.
 	 */
 	private static Logger log = Logger.getLogger(CRLuceneIndexJob.class);
 
 	/**
 	 * Name of class to use for IndexLocation, must extend
 	 * {@link com.gentics.cr.util.indexing.IndexLocation}.
 	 */
 	public static final String INDEXLOCATIONCLASS =
 		"com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation";
 
 	/**
 	 * Variable for RequestProcessor which gets us the objects for updating the
 	 * index.
 	 */
 	private RequestProcessor rp = null;
 
 	/**
 	 * indicates if the lucene index should be optimized after indexing.
 	 */
 	private boolean optimize = false;
 
 	/**
 	 * indicates the maximum amount of segments (files) used storing the index.
 	 */
 	private String maxSegmentsString = null;
 
 	/**
 	 * Create new instance of IndexJob.
 	 * @param config configuration for the index job
 	 * @param indexLoc location of the lucene index
 	 * @param configmap TODO add javadoc comment here
 	 */
 	public CRLuceneIndexJob(final CRConfig config, final IndexLocation indexLoc,
 			final Hashtable<String, CRConfigUtil> configmap) {
 		super(config, indexLoc, configmap);
 		String ignoreoptimizeString = config.getString(OPTIMIZE_KEY);
 		if (ignoreoptimizeString != null) {
 			optimize = Boolean.parseBoolean(ignoreoptimizeString);
 		}
 		maxSegmentsString = config.getString(MAXSEGMENTS_KEY);
 		String storeVectorsString = config.getString(STORE_VECTORS_KEY);
 		if (storeVectorsString != null) {
 			storeVectors = Boolean.parseBoolean(storeVectorsString);
 		}
 		try {
 			rp = config.getNewRequestProcessorInstance(1);
 		} catch (CRException e) {
 			log.error("Could not create RequestProcessor instance.", e);
 		}
 
 		String timestampattributeString = config.getString(TIMESTAMP_ATTR_KEY);
 		if (timestampattributeString != null
 				&& !"".equals(timestampattributeString)) {
 			this.timestampattribute = timestampattributeString;
 		}
 	}
 
 	/**
 	 * Key to be used for saving state to contentstatus.
 	 */
 	public static final	String PARAM_LASTINDEXRUN = "lastindexrun";
 	/**
 	 * Key to be used for saving state to contentstatus.
 	 */
 	public static final String PARAM_LASTINDEXRULE = "lastindexrule";
 
 	/**
 	 * Configuration key for the rule of objects to index.
 	 */
 	private static final String RULE_KEY = "rule";
 	
 	/**
 	 * Configuration key for the attributes stored in the index.
 	 */
 	private static final String BOOSTED_ATTRIBUTES_KEY = "BOOSTEDATTRIBUTES";
 
 	/**
 	 * Configuration key for the attributes stored in the index.
 	 */
 	private static final String CONTAINED_ATTRIBUTES_KEY = "CONTAINEDATTRIBUTES";
 
 	/**
 	 * Configuration key for the attributes which are indexed.
 	 */
 	private static final String INDEXED_ATTRIBUTES_KEY = "INDEXEDATTRIBUTES";
 
 	/**
 	 * Configuration key defines if the index should be optimized.
 	 */
 	private static final String OPTIMIZE_KEY = "optimize";
 
 	/**
 	 * Configuration key for {@link #maxSegmentsString}.
 	 */
 	private static final String MAXSEGMENTS_KEY = "maxsegments";
 
 	/**
 	 * Configuration key to define if vectors are stored in the index or not.
 	 */
 	private static final String STORE_VECTORS_KEY = "storeVectors";
 
 	/**
 	 * Configuration key to define the size of a single batch to index the files.
 	 * e.g. 100 means 100 files are indexes at once.
 	 */
 	private static final String BATCH_SIZE_KEY = "BATCHSIZE";
 
 	/**
 	 * TODO javadoc.
 	 */
 	private static final String CR_FIELD_KEY = "CRID";
 
 	/**
 	 * Configuration key to define which attribute is tested to decide if the
 	 * element is newer than the one in the index.
 	 */
 	public static final String TIMESTAMP_ATTR_KEY = "updateattribute";
 
 	/**
 	 * Default batch size is set to 1000 elements.
 	 */
 	private int batchSize = 1000;
 
 
 	/**
 	 * Attribute to check if the element is newer than the one in the index.
 	 * @see #TIMESTAMP_ATTR_KEY
 	 */
 	private String timestampattribute = "";
 	/**
 	 * Flag if TermVectors should be stored in the index or not.
 	 */
 	private boolean storeVectors = true;
 	
 	private HashMap<String,Float> boostvalue = new HashMap<String,Float>();
 	
 	/**
 	 * Fills the boostvalue map with the according values from "boostedattributes"
 	 * @param booststring
 	 */
 	private void fillBoostValues(String booststring)
 	{
 		if(booststring!=null)
 		{
 			try
 			{
 				String[] boostterms = booststring.split(",");
 				for(String term:boostterms) {
 					String[] t = term.split("\\^");
 					boostvalue.put(t[0], Float.parseFloat(t[1]));
 				}
 			} catch(Exception e)
 			{
 				log.error("Could not create boostvalues. Check your config! ("+booststring+")",e);
 			}
 		}
 	}
 	
 	/**
 	 * Index a single configured ContentRepository.
 	 * @param indexLocation TODO javadoc
 	 * @param config TODO javadoc
 	 * @throws CRException TODO javadoc
 	 */
 	@SuppressWarnings("unchecked")
 	protected void indexCR(final IndexLocation indexLocation,
 			final CRConfigUtil config) throws CRException {
 
 		String crid = config.getName();
 		if (crid == null) {
 			crid = this.identifyer;
 		}
 		fillBoostValues(config.getString(BOOSTED_ATTRIBUTES_KEY));
 
 		IndexAccessor indexAccessor = null;
 		IndexWriter indexWriter = null;
 		IndexReader indexReader = null;
 		LuceneIndexUpdateChecker luceneIndexUpdateChecker = null;
 		boolean finishedIndexJobSuccessfull = false;
 		boolean finishedIndexJobWithError = false;
 
 		try {
 			indexLocation.checkLock();
 			Collection<CRResolvableBean> slice = null;
 			try {
 				status.setCurrentStatusString("Writer accquired. Starting index job.");
 
 				if (rp == null) {
					throw new CRException("FATAL ERROR", "RequestProcessor not available");
 				}
 
 				String bsString = (String) config.get(BATCH_SIZE_KEY);
 
 				int crBatchSize = batchSize;
 
 				if (bsString != null) {
 					try {
 						crBatchSize = Integer.parseInt(bsString);
 					} catch (NumberFormatException e) {
 						log.error("The configured " + BATCH_SIZE_KEY + " for the Current CR"
 								+ " did not contain a parsable integer. " + e.getMessage());
 					}
 				}
 
 				// and get the current rule
 				String rule = (String) config.get(RULE_KEY);
 
 				if (rule == null) {
 					rule = "";
 				}
 				if (rule.length() == 0) {
 					rule = "(1 == 1)";
 				} else {
 					rule = "(" + rule + ")";
 				}
 
 				List<ContentTransformer> transformerlist =
 					ContentTransformer.getTransformerList(config);
 
 				boolean create = true;
 
 				if (indexLocation.isContainingIndex()) {
 					create = false;
 					log.debug("Index already exists.");
 				}
 				if (indexLocation instanceof LuceneIndexLocation) {
 					luceneIndexUpdateChecker = new LuceneIndexUpdateChecker(
 							(LuceneIndexLocation) indexLocation, CR_FIELD_KEY, crid,
 							idAttribute);
 				} else {
 					log.error("IndexLocation is not created for Lucene. Using the "
 							+ CRLuceneIndexJob.class.getName() + " requires that you use the "
 							+ LuceneIndexLocation.class.getName() + ". You can configure "
 							+ "another Job by setting the " + IndexLocation.UPDATEJOBCLASS_KEY
 							+ " key in your config.");
 					throw new CRException(
 							new CRError("Error", "IndexLocation is not created for Lucene."));
 				}
 				Collection<CRResolvableBean> objectsToIndex = null;
 				//Clear Index and remove stale Documents
 				//if (!create) {
 					log.debug("Will do differential index.");
 					try {
 						CRRequest req = new CRRequest();
 						req.setRequestFilter(rule);
 						req.set(CR_FIELD_KEY, crid);
 						status
 							.setCurrentStatusString("Get objects to update in the index ...");
 						objectsToIndex = getObjectsToUpdate(req, rp, false,
 								luceneIndexUpdateChecker);
 					} catch (Exception e) {
 						log.error("ERROR while cleaning index", e);
 					}
 				//}
 				//Obtain accessor and writer after clean
 				if (indexLocation instanceof LuceneIndexLocation) {
 					indexAccessor = ((LuceneIndexLocation) indexLocation).getAccessor();
 					indexWriter = indexAccessor.getWriter();
 					indexReader = indexAccessor.getReader(false);
 				} else {
 					log.error("IndexLocation is not created for Lucene. Using the "
 							+ CRLuceneIndexJob.class.getName() + " requires that you use the "
 							+ LuceneIndexLocation.class.getName()
 							+ ". You can configure another Job by setting the "
 							+ IndexLocation.UPDATEJOBCLASS_KEY + " key in your config.");
 				}
 				log.debug("Using rule: " + rule);
 				// prepare the map of indexed/stored attributes
 				Map<String, Boolean> attributes = new HashMap<String, Boolean>();
 				List<String> containedAttributes = IndexerUtil.getListFromString(
 						config.getString(CONTAINED_ATTRIBUTES_KEY), ",");
 				List<String> indexedAttributes = IndexerUtil.getListFromString(
 						config.getString(INDEXED_ATTRIBUTES_KEY), ",");
 				List<String> reverseAttributes =
 					((LuceneIndexLocation) indexLocation).getReverseAttributes();
 				// first put all indexed attributes into the map
 				for (String name : indexedAttributes) {
 					attributes.put(name, Boolean.FALSE);
 				}
 
 				// now put all contained attributes
 				for (String name : containedAttributes) {
 					attributes.put(name, Boolean.TRUE);
 				}
 				// finally, put the "contentid" (always contained)
 				attributes.put(idAttribute, Boolean.TRUE);
 
 				// get all objects to index
 				/*
 				 * THIS SNIPPED HAS BEEN REMOVED BECAUSE IT CAUSED A DEADLOCK IF 
 				 * THE DB CONNECTION IS BROKEN
 				 * if (objectsToIndex == null) {
 					CRRequest req = new CRRequest();
 					req.setRequestFilter(rule);
 					req.set(CR_FIELD_KEY, crid);
 					objectsToIndex =
 						getObjectsToUpdate(req, rp, true, luceneIndexUpdateChecker);
 				}*/
 				if (objectsToIndex == null) {
 					log.debug("Rule returned no objects to index. Skipping run");
 					return;
 				}
 
 				status.setObjectCount(objectsToIndex.size());
 				log.debug(" index job with " + objectsToIndex.size()
 						+ " objects to index.");
 				// now get the first batch of objects from the collection
 				// (remove them from the original collection) and index them
 				slice = new Vector(crBatchSize);
 				int sliceCounter = 0;
 
 				status.setCurrentStatusString("Starting to index slices.");
 				boolean interrupted = Thread.currentThread().isInterrupted();
 				for (Iterator<CRResolvableBean> iterator = objectsToIndex.iterator();
 						iterator.hasNext();) {
 					CRResolvableBean obj = iterator.next();
 					slice.add(obj);
 					iterator.remove();
 					sliceCounter++;
 					if (Thread.currentThread().isInterrupted()) {
 						interrupted = true;
 						break;
 					}
 					if (sliceCounter == crBatchSize) {
 						// index the current slice
 						log.debug("Indexing slice with " + slice.size() + " objects.");
 						indexSlice(crid, indexWriter, indexReader, slice, attributes, rp, create, config,
 								transformerlist, reverseAttributes);
 						//status.setObjectsDone(status.getObjectsDone() + slice.size());
 						// clear the slice and reset the counter
 						slice.clear();
 						sliceCounter = 0;
 					}
 				}
 
 				if (!slice.isEmpty()) {
 					// index the last slice
 					indexSlice(crid, indexWriter, indexReader, slice, attributes, rp, create, config,
 							transformerlist, reverseAttributes);
 					//status.setObjectsDone(status.getObjectsDone() + slice.size());
 				}
 				if (!interrupted) {
 					//Only Optimize the Index if the thread has not been interrupted
 					if (optimize) {
 						log.debug("Executing optimize command.");
 						UseCase uc = MonitorFactory.startUseCase("optimize(" + crid + ")");
 						try {
 							indexWriter.optimize();
 						} finally {
 							uc.stop();
 						}
 					} else if (maxSegmentsString != null) {
 						log.debug("Executing optimize command with max segments: "
 								+ maxSegmentsString);
 						int maxs = Integer.parseInt(maxSegmentsString);
 						UseCase uc = MonitorFactory.startUseCase("optimize(" + crid + ")");
 						try {
 							indexWriter.optimize(maxs);
 						} finally {
 							uc.stop();
 						}
 					}
 				} else {
 					log.debug("Job has been interrupted and will now be closed. Missing objects "
 							+ "will be reindexed next run.");
 				}
 				finishedIndexJobSuccessfull = true;
 			} catch (Exception ex) {
 				log.error("Could not complete index run... indexed Objects: " + status
 						.getObjectsDone() + ", trying to close index and remove lock.", ex);
 				finishedIndexJobWithError = true;
 			} finally {
 				if (!finishedIndexJobSuccessfull && !finishedIndexJobWithError) {
 					log.fatal("There seems to be a run time exception from this"
 							+ " index job.\nLast slice was: " + slice);
 				}
 				//Set status for job if it was not locked
 				status.setCurrentStatusString("Finished job.");
 				int objectCount = status.getObjectsDone();
 				log.debug("Indexed " + objectCount + " objects...");
 
 				if (indexAccessor != null && indexWriter != null) {
 						indexAccessor.release(indexWriter);
 				}
 				if (indexAccessor != null && indexWriter != null) {
 						indexAccessor.release(indexReader, false);
 				}
 				
 
 				if (objectCount > 0) {
 					indexLocation.createReopenFile();
 				}
 				EventManager.getInstance().fireEvent(
 						new IndexingFinishedEvent(indexLocation));
 			}
 		} catch (LockedIndexException ex) {
 			log.debug("LOCKED INDEX DETECTED. TRYING AGAIN IN NEXT JOB.");
 			if (this.indexLocation != null && !this.indexLocation.hasLockDetection())
 			{
 				log.error("IT SEEMS THAT THE INDEX HAS UNEXPECTEDLY BEEN LOCKED. "
 						+ "TRYING TO REMOVE THE LOCK", ex);
 				((LuceneIndexLocation) this.indexLocation).forceRemoveLock();
 			}
 		} catch (Exception ex) {
 			log.debug("ERROR WHILE CHECKING LOCK", ex);
 		}
 	}
 
 	/**
 	 * Index a single slice.
 	 * @param crid TODO javadoc
 	 * @param indexWriter TODO javadoc
  * @param indexReader 
 	 * @param slice TODO javadoc
 	 * @param attributes TODO javadoc
 	 * @param rp TODO javadoc
 	 * @param create TODO javadoc
 	 * @param config TODO javadoc
 	 * @param transformerlist TODO javadoc
 	 * @param reverseattributes TODO javadoc
 	 * @throws CRException TODO javadoc
 	 * @throws IOException TODO javadoc
 	 */
 	private void indexSlice(final String crid, final IndexWriter indexWriter,
 			final IndexReader indexReader, final Collection<CRResolvableBean> slice,
 			final Map<String, Boolean> attributes, final RequestProcessor rp,
 			final boolean create, final CRConfigUtil config,
 			final List<ContentTransformer> transformerlist,
 			final List<String> reverseattributes) throws CRException, IOException {
 		// prefill all needed attributes
 		UseCase uc = MonitorFactory.startUseCase("indexSlice(" + crid + ")");
 		try {
 			CRRequest req = new CRRequest();
 			String[] prefillAttributes = attributes.keySet().toArray(new String[0]);
 			req.setAttributeArray(prefillAttributes);
 			UseCase prefillCase = MonitorFactory.startUseCase("indexSlice(" + crid
 					+ ").prefillAttributes");
 			rp.fillAttributes(slice, req, idAttribute);
 			prefillCase.stop();
 			for (Resolvable objectToIndex : slice) {
 				CRResolvableBean bean =
 					new CRResolvableBean(objectToIndex, prefillAttributes);
 				UseCase bcase = MonitorFactory.startUseCase("indexSlice(" + crid
 						+ ").indexBean");
 				try {
 					//CALL PRE INDEX PROCESSORS/TRANSFORMERS
 					//TODO This could be optimized for multicore servers with a map/reduce
 					//algorithm
 					if (transformerlist != null) {
 						for (ContentTransformer transformer : transformerlist) {
 							try {
 								status.setCurrentStatusString("TRANSFORMING... TRANSFORMER: "
 										+ transformer.getTransformerKey() + "; BEAN: "
 										+ bean.get(idAttribute));
 								if (transformer.match(bean)) {
 									transformer.processBeanWithMonitoring(bean, indexWriter);
 								}
 							} catch (Exception e) {
 								//TODO Remember broken files
 								log.error("Error while Transforming Contentbean with id: "
 										+ bean.get(idAttribute) + " Transformer: "
 										+ transformer.getTransformerKey() + " "
 										+ transformer.getClass().getName(), e);
 							}
 						}
 					}
 					Term idTerm =
 						new Term(idAttribute, bean.getString(idAttribute));
 					Document docToUpdate = getUniqueDocument(indexReader,
 							idTerm, crid);
 					if (!create && docToUpdate != null) {
 						indexWriter.updateDocument(idTerm, getDocument(
 								docToUpdate, bean, attributes, config,
 								reverseattributes));
 					} else {
 						indexWriter.addDocument(getDocument(null, bean, attributes, config,
 								reverseattributes));
 					}
 				} finally {
 					bcase.stop();
 				}
 				//Stop Indexing when thread has been interrupted
 				if (Thread.currentThread().isInterrupted()) {
 					break;
 				}
 				this.status.setObjectsDone(this.status.getObjectsDone()+1);
 			}
 		} finally {
 			uc.stop();
 		}
 	}
 
 
 	private Document getUniqueDocument(IndexReader indexReader, Term idTerm,
 		String searchCRID) {
 	try {
 		TermDocs docs = indexReader.termDocs(idTerm);
 		while (docs.next()) {
 			Document doc = indexReader.document(docs.doc());
 			String crID = doc.get(CR_FIELD_KEY);
 			if (crID != null && crID.equals(searchCRID)) {
 				return doc;
 			}
 		}
 	} catch (IOException e) {
 		log.error("An error happend while searching the document in the index.",
 				e);
 	}
 	return null;
 }
 
 	/**
 	 * Convert a resolvable to a Lucene Document.
 	 * @param newDoc 
 	 * @param resolvable Contains the resolvable to be indexed
 	 * @param attributes A map of attribute names, which values are true if the
 	 * attribute should be stored or fales if the attribute should only be
 	 * indexed. Only attributes configured in this map will be indexed
 	 * @param config The name of this config will be used as CRID
 	 * (ContentRepository Identifyer). The ID-Attribute should also be configured
 	 * in this config (usually contentid).
 	 * @param reverseattributes Attributes that should be indexed in reverse order
 	 * . This can be used to search faster for words ending with *ing.
 	 * @return Returns a Lucene Document, ready to be added to the index.
 	 */
 	private Document getDocument(final Document doc, final Resolvable resolvable,
 			final Map<String, Boolean> attributes, final CRConfigUtil config,
 			final List<String> reverseattributes) {
 		Document newDoc;
 		if (doc == null) {
 			newDoc = new Document();
 		} else {
 			newDoc = doc;
 		}
 		String crID = (String) config.getName();
 		if (crID != null) {
 			//Add content repository identification
 			newDoc.add(new Field(CR_FIELD_KEY, crID, Field.Store.YES,
 					Field.Index.NOT_ANALYZED));
 		}
 		if (!"".equals(timestampattribute)) {
 			Object updateTimestampObject = resolvable.get(timestampattribute);
 			String updateTimestamp = updateTimestampObject.toString();
 			if (updateTimestamp != null && !"".equals(updateTimestamp)) {
 				newDoc.removeField(timestampAttribute);
 				newDoc.add(new Field(timestampattribute,
 						updateTimestamp.toString(), Field.Store.YES,
 						Field.Index.NOT_ANALYZED));
 			}
 		}
 		for (Entry<String, Boolean> entry : attributes.entrySet()) {
 			String attributeName = (String) entry.getKey();
 			boolean filled = (newDoc.get(attributeName) != null);
 			Boolean storeField = (Boolean) entry.getValue();
 
 			Object value = resolvable.getProperty(attributeName);
 
 			if (idAttribute.equalsIgnoreCase(attributeName) && !filled) {
 				newDoc.add(new Field(idAttribute, value.toString(),
 						Field.Store.YES, Field.Index.NOT_ANALYZED));
 			} else if (value != null) {
 				if (filled) {
 					newDoc.removeField(attributeName);
 				}
 				Store storeFieldStore;
 				if (storeField) {
 					storeFieldStore = Store.YES;
 				} else {
 					storeFieldStore = Store.NO;
 				}
 				TermVector storeTermVector;
 				if (storeVectors) {
 					storeTermVector = TermVector.WITH_POSITIONS_OFFSETS;
 				} else {
 					storeTermVector = TermVector.NO;
 				}
 				if (value instanceof String || value instanceof Number) {
 					Field f = new Field(attributeName, value.toString(), storeFieldStore,
 										Field.Index.ANALYZED, storeTermVector);
 					Float boost_value = boostvalue.get(attributeName);
 						if (boost_value!=null) {
 							f.setBoost(boost_value);
 						}
 						newDoc.add(f);
 					//ADD REVERSEATTRIBUTE IF NEEDED
 					if (reverseattributes != null
 							&& reverseattributes.contains(attributeName)) {
 						String reverseAttributeName = attributeName
 							+ LuceneAnalyzerFactory.REVERSE_ATTRIBUTE_SUFFIX;
 						Field revField = new Field(reverseAttributeName, value.toString(),
 										storeFieldStore, Field.Index.ANALYZED, storeTermVector);
 						Float rev_boost_value = boostvalue.get(reverseAttributeName);
 						if(rev_boost_value!=null)revField.setBoost(rev_boost_value);
 						newDoc.add(revField);
 					}
 				}
 			}
 		}
 		return newDoc;
 	}
 }
 
