 package com.gentics.cr.lucene.indexer;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermEnum;
 import org.apache.lucene.store.FSDirectory;
 
 import com.gentics.api.lib.exception.NodeException;
 import com.gentics.api.lib.expressionparser.ExpressionParser;
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.contentnode.content.GenticsContentFactory;
 import com.gentics.contentnode.datasource.CNWriteableDatasource;
 import com.gentics.cr.CRConfigFileLoader;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
 import com.gentics.cr.util.CRUtil;
 
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class CRIndexer {
 	/**
 	 * Key to be used for saving state to contentstatus
 	 */
 	public final static String PARAM_LASTINDEXRUN = "lastindexrun";
 	/**
 	 * Key to be used for saving state to contentstatus
 	 */
 	public final static String PARAM_LASTINDEXRULE = "lastindexrule";
 
 	protected String indexLocation = null;
 	
 	protected int interval = -1;
 	
 	protected String name = null;
 	
 	protected int batchSize = 1000;
 	
 	protected static Logger log = Logger.getLogger(CRIndexer.class);
 	
 	protected IndexerStatus status = new IndexerStatus();
 	
 	protected BackgroundJob indexerJob;
 	
 	protected Thread backgroundThread;
 	
 	protected boolean periodicalRun=false;
 	
 	protected CRConfigUtil crconfig;
 	
 	
 	private static final String INTERVAL_KEY = "INTERVAL";
 	private static final String BATCHSIZE_KEY = "BATCHSIZE";
 	private static final String PERIODICAL_KEY = "PERIODICAL";
 	private static final String INDEX_LOCATION_KEY = "indexLocation";
 	
 	
 	/**
 	 * Create new instance of CRIndexer
 	 * @param name name of the CRIndexer
 	 */
 	public CRIndexer(String name)
 	{
 		this.name = name;
 		crconfig = new CRConfigFileLoader(name, null);
 		postConfig();
 		indexerJob = new BackgroundJob(this.periodicalRun);
 		backgroundThread = new Thread(indexerJob);
 		//initializes background job
 	}
 	
 	private void postConfig()
 	{
 		String i = (String)crconfig.get(INTERVAL_KEY);
 		if(i!=null)this.interval = new Integer(i);
 		String bs = (String)crconfig.get(BATCHSIZE_KEY);
 		if(bs!=null)this.batchSize = new Integer(bs);
 		String p = (String)crconfig.get(PERIODICAL_KEY);
 		if(p!=null)this.periodicalRun = Boolean.parseBoolean(p);
 		indexLocation = (String)crconfig.get(INDEX_LOCATION_KEY);
 	}
 	
 	/**
 	 * @return true if the background index job has been started
 	 */
 	public boolean isStarted()
 	{
 		return(this.indexerJob.isStarted());
 	}
 	
 	
 	/**
 	 * starts the background index job
 	 */
 	public void startJob()
 	{
 		this.periodicalRun=true;
 		this.indexerJob.stop=false;
 		this.backgroundThread.start();
 		
 		try {
 			Thread.sleep(5);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * @return IndexerStatus of current Indexer
 	 */
 	public IndexerStatus getStatus()
 	{
 		return(this.status);
 	}
 	
 	/**
 	 * 
 	 * @return indexing interval 
 	 */
 	public int getInterval()
 	{
 		return(this.interval);
 	}
 	
 	/**
 	 * stops the background indexing job
 	 */
 	public void stopJob()
 	{
 		this.periodicalRun=false;
 		this.indexerJob.stop=true;
 	}
 	
 	/**
 	 * starts a single indexing run
 	 */
 	public void startSingleRun()
 	{
 		//TODO check if periodical backgroundjob is started and only execute if not
 		this.indexerJob.runSingle();
 	}
 	
 	
 	
 	protected class BackgroundJob implements Runnable{
 
 		/**
 		 * set to true if the backgroundjob has to stop
 		 */
 		public boolean stop = false;
 		
 		/**
 		 * Create new instace of Backgroundjob
 		 * @param startPeriodicalRun true if the Backgroundjob should be periodical
 		 */
 		public BackgroundJob(boolean startPeriodicalRun)
 		{
 			this.stop = !startPeriodicalRun;
 		}
 		
 		/**
 		 * 
 		 * @return true if the backgroundjob has been started
 		 */
 		public boolean isStarted()
 		{
 			return(!this.stop);
 		}
 		
 		/**
 		 * Run single index creation run
 		 */
 		public void runSingle()
 		{
 			Thread d = new Thread(new Runnable(){
 
 				public void run() {
 					recreateIndex();
 				}
 				
 			});
 			d.start();
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		/**
 		 * Run periodical index run
 		 */
 		public void run() {
 			boolean interrupted = false;
 
 			while (!interrupted && !stop && interval>0) {
 				try {
 					// recreate the search index
 					recreateIndex();
 					// Wait for next cycle
 					Thread.sleep(interval * 1000);
 				} catch (InterruptedException e) {
 					interrupted = true;
 				}
 			}
 			this.stop=true;
 		}
 
 		private static final String CR_KEY = "CR";
 		
 		
 		private void recreateIndex() {
 			status.setRunning(true);
 			long startTime = System.currentTimeMillis();
 			status.setStartTime(new Date());
 			
 			int timestamp = (int)(System.currentTimeMillis() / 1000);
 
 			
 			// create an index writer
 			File indexLoc = new File(indexLocation);
 					
 			GenericConfiguration CRc = (GenericConfiguration)crconfig.get(CR_KEY);
 			if(CRc!=null)
 			{
 				Hashtable<String,GenericConfiguration> configs = CRc.getSubConfigs();
 	
 				for (Entry<String,GenericConfiguration> e:configs.entrySet()) {
 					try {
 						indexCR(indexLoc, timestamp, new CRConfigUtil(e.getValue(),crconfig.getName()+"."+e.getKey()));
 					} catch (Exception ex){
 						log.error("Error while recreating index for "+crconfig.getName()+"."+e.getKey());
 						ex.printStackTrace();
 					}
 				}
 			}
 			else
 			{
 				log.error("THERE ARE NO CRs CONFIGURED FOR INDEXING.");
 			}
 
 				
 			
 			long endTime = System.currentTimeMillis();
 			status.setLastRunDuration(endTime-startTime);
 			status.reset();
 		}
 		
 		private static final String RULE_KEY = "rule";
 		private static final String ID_ATTRIBUTE_KEY = "IDATTRIBUTE";
 		private static final String STOP_WORD_FILE_KEY = "STOPWORDFILE";
 		private static final String CONTAINED_ATTRIBUTES_KEY = "CONTAINEDATTRIBUTES";
 		private static final String INDEXED_ATTRIBUTES_KEY = "INDEXEDATTRIBUTES";
 		private static final String STEMMING_KEY = "STEMMING";
 		private static final String STEMMER_NAME_KEY = "STEMMERNAME";
 
 		@SuppressWarnings("unchecked")
 		private void indexCR(File indexLocation, int timestamp, CRConfigUtil config)
 				throws NodeException, CorruptIndexException, IOException {
 			// get the datasource
 			CNWriteableDatasource ds = (CNWriteableDatasource)config.getDatasource();
 
 			// get the last index timestamp
 			int lastIndexRun = ds.getIntContentStatus(name + "."
 					+ PARAM_LASTINDEXRUN);
 
 			// get the last index rule
 			String lastIndexRule = ds.getStringContentStatus(name + "."
 					+ PARAM_LASTINDEXRULE);
 
 			if (lastIndexRule == null) {
 				lastIndexRule = "";
 			}
 				
 
 			// and get the current rule
 			String rule = (String)config.get(RULE_KEY);
 
 			if (rule == null) {
 				rule = "";
 			}
 			
 			Hashtable<String,ContentTransformer> transformertable = ContentTransformer.getTransformerTable(config);
 			
 			boolean create = true;
 			
 			if(indexLocation.exists() && indexLocation.isDirectory())
 				create=false;
 
 			// check whether the rule has changed, if yes, do a full index run
 			boolean doFullIndexRun = lastIndexRun <= 0 || !lastIndexRule.equals(rule);
 
 			if (rule.length() == 0) {
 				rule = "(1 == 1)";
 			} else {
 				rule = "(" + rule + ")";
 			}
 			
 			//Clear Index and remove stale Documents
 			if(!create)
 			{
 				
 				try {
 					cleanIndex(ds,rule,indexLocation,config);
 				} catch (Exception e) {
 					log.error("ERROR while cleaning index");
 					e.printStackTrace();
 				}
 				
 			}
 			
 			
 			
 			if (!doFullIndexRun) {
 				// do a differential index run, so just get the objects modified since the last index run
 				rule += " AND (object.updatetimestamp > " + lastIndexRun
 						+ " AND object.updatetimestamp <= " + timestamp + ")";
 			}
 			
 			
 			
 			//Update/add Documents
 			Analyzer analyzer;
 			boolean doStemming = Boolean.parseBoolean((String)config.get(STEMMING_KEY));
 			if(doStemming)
 			{
 				analyzer = new SnowballAnalyzer((String)config.get(STEMMER_NAME_KEY));
 			}
 			else
 			{
 				
 				//Load StopWordList
 				File stopWordFile = IndexerUtil.getFileFromPath((String)config.get(STOP_WORD_FILE_KEY));
 				
 				if(stopWordFile!=null)
 				{
 					//initialize Analyzer with stop words
 					analyzer = new StandardAnalyzer(stopWordFile);
 				}
 				else
 				{
 					//if no stop word list exists load fall back
 					analyzer = new StandardAnalyzer();
 				}
 			}
 			
 			
 
 			// prepare the map of indexed/stored attributes
 			Map<String,Boolean> attributes = new HashMap<String,Boolean>();
 			List<String> containedAttributes = IndexerUtil.getListFromString((String)config.get(CONTAINED_ATTRIBUTES_KEY), ",");
 			List<String> indexedAttributes = IndexerUtil.getListFromString((String)config.get(INDEXED_ATTRIBUTES_KEY), ",");
 
 			// first put all indexed attributes into the map
 			for (String name:indexedAttributes) {
 				attributes.put(name, Boolean.FALSE);
 			}
 
 			// now put all contained attributes
 			for (String name:containedAttributes) {
 				attributes.put(name, Boolean.TRUE);
 			}
 
 			String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
 			// finally, put the "contentid" (always contained)
 			attributes.put(idAttribute, Boolean.TRUE);
 
 			// get all objects to index
 			Collection<Resolvable> objectsToIndex = (Collection<Resolvable>) ds.getResult(ds
 					.createDatasourceFilter(ExpressionParser.getInstance()
 							.parse(rule)), null);
 
 			status.setObjectCount(objectsToIndex.size());
 			// TODO now get the first batch of objects from the collection
 			// (remove them from the original collection) and index them
 			Collection<Resolvable> slice = new Vector(batchSize);
 			int sliceCounter = 0;
 			
 			IndexWriter indexWriter = new IndexWriter(indexLocation,analyzer, create,IndexWriter.MaxFieldLength.LIMITED);
 
 			try
 			{
 				for (Iterator<Resolvable> iterator = objectsToIndex.iterator(); iterator.hasNext();) {
 					Resolvable obj = iterator.next();
 					slice.add(obj);
 					iterator.remove();
 					sliceCounter++;
 	
 					if (sliceCounter == batchSize) {
 						// index the current slice
 						indexSlice(indexWriter, slice, attributes, ds, create,config,transformertable);
 						status.setObjectsDone(status.getObjectsDone()+slice.size());
 						// clear the slice and reset the counter
 						slice.clear();
 						sliceCounter = 0;
 					}
 				}
 	
 				if (!slice.isEmpty()) {
 					// index the last slice
 					indexSlice(indexWriter, slice, attributes, ds, create,config, transformertable);
 				}
 				indexWriter.optimize();
 			}catch(Exception ex)
 			{
 				
 				log.error("Could not complete index run... indexed Objects: "+status.getObjectsDone()+", trying to close index and remove lock.");
 				ex.printStackTrace();
 			}finally{
 				log.debug("Indexed "+status.getObjectsDone()+" objects...");
 				indexWriter.close();
 			}
 			
 			
 		}
 
 		private void indexSlice(IndexWriter indexWriter, Collection<Resolvable> slice,
 				Map<String,Boolean> attributes, CNWriteableDatasource ds, boolean create, CRConfigUtil config, Hashtable<String,ContentTransformer> transformertable) throws NodeException,
 				CorruptIndexException, IOException {
 			// prefill all needed attributes
 			GenticsContentFactory.prefillContentObjects(ds, slice,
 					(String[]) attributes.keySet().toArray(
 							new String[attributes.keySet().size()]));
 			String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
 			for (Resolvable objectToIndex:slice) {
 				if(!create)
 				{
 					indexWriter.updateDocument(new Term(idAttribute, (String)objectToIndex.get(idAttribute)), getDocument(objectToIndex, attributes,config,transformertable));
 				}
 				else
 				{
 					indexWriter.addDocument(getDocument(objectToIndex, attributes, config,transformertable));
 				}
 			}
 		}
 		
 		
 		private Hashtable<String,ContentTransformer> getResolvableSpecifigTransformerMap(Resolvable resolvable, Hashtable<String,ContentTransformer> transformermap)
 		{
 			Hashtable<String,ContentTransformer> ret = new Hashtable<String,ContentTransformer>();
 			if(transformermap!=null)
 			{
 				for(Entry<String,ContentTransformer> e:transformermap.entrySet())
 				{
 					String att = e.getKey();
 					ContentTransformer t = e.getValue();
 					if(t.match(resolvable))ret.put(att, t);
 				}
 			}
 			return(ret);
 		}
 
 		private Document getDocument(Resolvable resolvable, Map<String,Boolean> attributes, CRConfigUtil config,Hashtable<String,ContentTransformer> transformertable) {
 			Document doc = new Document();
 			Hashtable<String,ContentTransformer> tmap = getResolvableSpecifigTransformerMap(resolvable,transformertable);
 			String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
 			
 			
 			for (Entry<String,Boolean> entry:attributes.entrySet()) {
 				
 				String attributeName = (String) entry.getKey();
 				Boolean storeField = (Boolean) entry.getValue();
 
 				Object value = resolvable.getProperty(attributeName);
 				
 				
 				if(idAttribute.equalsIgnoreCase(attributeName))
 				{
 					doc.add(new Field(idAttribute, (String)value, Field.Store.YES, Field.Index.NOT_ANALYZED));
 				}
 				else if(value!=null){
 					ContentTransformer t = tmap.get(attributeName);
 					if(t!=null)
 					{
 						if(storeField.booleanValue())
 						{
 							String v =  t.getStringContents(value);
 							doc.add(new Field(attributeName,v, storeField.booleanValue() ? Store.YES : Store.NO,Field.Index.ANALYZED));
 						}
 						else
 						{
 							Reader r = t.getContents(value);
 							doc.add(new Field(attributeName, r));
 						}
 					}
 					else
 					{
 						if(value instanceof String || value instanceof Number)
 						{
 							doc.add(new Field(attributeName, value.toString(),storeField.booleanValue() ? Store.YES : Store.NO,Field.Index.ANALYZED));
 						}
 					}
 				}
 			}
 			return doc;
 		}
 		
 		
 		
 		/**
 		 * Deletes all Objects from index, which are not returned from the datasource using the given rule
 		 * @param ds
 		 * @param rule
 		 * @param indexLocation
 		 * @throws Exception
 		 */
 		@SuppressWarnings("unchecked")
 		private void cleanIndex(CNWriteableDatasource ds, String rule, File indexLocation, CRConfigUtil config) throws Exception
 		{
 			String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
 			
 			IndexReader reader = IndexReader.open(FSDirectory.getDirectory(indexLocation), false);// open existing index
 			
 			TermEnum uidIter = reader.terms(new Term(idAttribute, "")); // init uid iterator
 			
 			Collection<Resolvable> objectsToIndex = (Collection<Resolvable>)ds.getResult(ds.createDatasourceFilter(ExpressionParser.getInstance().parse(rule)), null, 0, -1, CRUtil.convertSorting("contentid:asc"));
 			
 			Iterator<Resolvable> resoIT = objectsToIndex.iterator();
 			
 			Resolvable CRlem = resoIT.next();
 			String crElemID =(String) CRlem.get(idAttribute);
 			
 			//solange index id kleiner cr id delete from index
 			boolean finish=false;
 			
 			while(!finish)
 			{
 				
 				if(uidIter.term() != null && uidIter.term().field() == idAttribute && uidIter.term().text().compareTo(crElemID) == 0)
 				{
 					//step both
 					finish = !uidIter.next();
 					if(resoIT.hasNext())
 					{
 						CRlem = resoIT.next();
 						crElemID =(String) CRlem.get(idAttribute);
 					}
 				}
 				else if(uidIter.term() != null && uidIter.term().field() == idAttribute && uidIter.term().text().compareTo(crElemID) > 0 && resoIT.hasNext())
 				{
 					//step cr
 					CRlem = resoIT.next();
 					crElemID =(String) CRlem.get(idAttribute);
 					
 				}
 				else
 				{
 					//delete UIDITER
 					reader.deleteDocuments(uidIter.term());
 					finish = !uidIter.next();
 				}
 				
 			}
 			uidIter.close();  // close uid iterator
 		    reader.close();	//close reader
 		    
 				
 		}
 	}
 
 	/**
 	 * 
 	 * @return true if running periodical indexing jobs
 	 */
 	public boolean isPeriodicalRun() {
 		return periodicalRun;
 	}
 
 	/**
 	 * 
 	 * @param periodicalRun true when to run periodical indexing jobs
 	 */
 	public void setPeriodicalRun(boolean periodicalRun) {
 		this.periodicalRun = periodicalRun;
 	}
 }
