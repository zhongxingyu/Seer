 package com.gentics.cr.util.indexing;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.exceptions.WrongOrderException;
 import com.gentics.cr.monitoring.MonitorFactory;
 import com.gentics.cr.monitoring.UseCase;
 
 //TODO: complete JavaDoc when class is finished
 /**
  * This class is designed as an UpdateChecker for a ContentRepository. It checks a Gentics ContentRepository for Updates and gives updated Documents to some class
  * @author perhab
  *
  */
 public abstract class AbstractUpdateCheckerJob implements Runnable {
 
   protected static Logger log = Logger.getLogger(AbstractUpdateCheckerJob.class);
   
   /**
    * Name of class to use for IndexLocation, must extend {@link com.gentics.cr.util.indexing.IndexLocation}
    */
   public static final String INDEXLOCATIONCLASS = "com.gentics.cr.util.indexing.IndexLocation";
   
   protected static final String ID_ATTRIBUTE_KEY = "IDATTRIBUTE";
   protected static final String DEFAULT_IDATTRIBUTE = "contentid";
   /**
    * Timestamp attribute name
    */
   public static final String TIMESTAMP_ATTR = "";
  public static final String TIMESTAMP_ATTR_KEY = "updatettribute";
   
   protected CRConfig config;
   protected String identifyer;
   protected IndexerStatus status;
   
   protected String idAttribute;
   protected String timestampAttribute;
   
   private Hashtable<String,CRConfigUtil> configmap;
   protected IndexLocation indexLocation;
   
   private long duration = 0;
   
   
   /**
    * TODO comment
    * @param config
    * @param indexLoc
    * @param configmap
    */
   public AbstractUpdateCheckerJob(CRConfig config, IndexLocation indexLoc,Hashtable<String,CRConfigUtil> configmap)
   {
     this.config = config;
     this.configmap = configmap;
     if(this.configmap==null)log.debug("Configmap is empty");
     this.identifyer = (String) config.getName();
     this.indexLocation = indexLoc;
     status = new IndexerStatus();
     idAttribute = config.getString(ID_ATTRIBUTE_KEY);
     if(idAttribute == null)
       idAttribute = DEFAULT_IDATTRIBUTE;
 
     timestampAttribute = config.getString(TIMESTAMP_ATTR_KEY);
     if(timestampAttribute == null)
        timestampAttribute = TIMESTAMP_ATTR;
     
   }
   
   /**
    * Gets the Job Identifyer. In most cases this is the CR id.
    * @return identifyer as string
    */
   public String getIdentifyer()
   {
     return identifyer;
   }
   
   /**
    * Get job duration as ms;
    * @return
    */
   public long getDuration()
   {
     return duration;
   }
   
   /**
    * Get total count of objects to index
    * @return
    */
   public int getObjectsToIndex()
   {
     return status.getObjectCount();
   }
   
   /**
    * Get the number ob objects already indexed
    * @return
    */
   public int getObjectsDone()
   {
     return status.getObjectsDone();
   }
   
   /**
    * Get Current Status
    * @return
    */
   public String getStatusString()
   {
     return status.getCurrentStatusString();
   }
   
   /**
    * Tests if a {@link AbstractUpdateCheckerJob} has the same identifier as the given object being an instance of {@link AbstractUpdateCheckerJob}
    */
   @Override
   public boolean equals(Object obj)
   {
     if(obj instanceof AbstractUpdateCheckerJob)
     {
       if(this.identifyer.equalsIgnoreCase(((AbstractUpdateCheckerJob)obj).getIdentifyer()))
       {
         return true;
       }
     }
     return false;
   }
   
   protected abstract void indexCR(IndexLocation indexLocation, CRConfigUtil config) throws CRException;
   
   
   /**
    * Checks objects in {@link Datasource} that match the rule, if they are up to date in the index. Finally all the objects not checked for an update are removed from the index.
    * @param rule Rule describing the objects that should be indexed
    * @param ds {@link Datasource} providing the objects to index
    * @param forceFullUpdate boolean use to force a full update in the index
    * @param indexUpdateChecker {@link IIndexUpdateChecker} to check if the item is up to date in the index
    * @return {@link Collection} of {@link Resolvables} that need an update in the index.
    * 
    * @see {@link IIndexUpdateChecker#isUpToDate(String, int)}
    * @see {@link IIndexUpdateChecker#deleteStaleObjects()}
    */
   protected Collection<CRResolvableBean> getObjectsToUpdate(CRRequest request, RequestProcessor rp, boolean forceFullUpdate, IndexUpdateChecker indexUpdateChecker){
     Collection<CRResolvableBean> updateObjects = new Vector<CRResolvableBean>();
         
     UseCase objectsToUpdateCase = MonitorFactory.startUseCase(
         "AbstractUpdateCheck.getObjectsToUpdate(" + request.get("CRID") + ")");
     try
     {
       if(forceFullUpdate){
         try {
           updateObjects = (Collection<CRResolvableBean>) rp.getObjects(request);
         } catch (CRException e) {
           log.error("Error getting results for full index from requestprocessor",e);
         } 
       }
       else{
         //Sorted (by the idAttribute) list of Resolvables to check for Updates.
         Collection<CRResolvableBean> objectsToIndex;
         try{
           defaultizeRequest(request);
           objectsToIndex = (Collection<CRResolvableBean>) rp.getObjects(request);
         }
         catch (CRException e) {
           log.error("Error getting results for full index from requestprocessor",e);
           return null;
         }
         Iterator<CRResolvableBean> resolvableIterator =
           objectsToIndex.iterator();
         try {
           while (resolvableIterator.hasNext()) {
             CRResolvableBean crElement = resolvableIterator.next();
             Object o_crElementID = crElement.get(idAttribute);
             if(o_crElementID==null)log.error("IDAttribute is null!");
             String crElementID = o_crElementID.toString();
             Object crElementTimestamp =  crElement.get(timestampAttribute);
             if (!indexUpdateChecker.isUpToDate(crElementID, crElementTimestamp,
                 timestampAttribute,crElement)) {
               updateObjects.add(crElement);
             }
           }
         } catch (WrongOrderException e) {
           log.error("Got the objects from the datasource in the wrong order.",
               e);
           return null;
         }
       }
       //Finally delete all Objects from Index that are not checked for an Update
       indexUpdateChecker.deleteStaleObjects();
     }
     finally {
       objectsToUpdateCase.stop();
     }
     return updateObjects;
   }
 
   private void defaultizeRequest(CRRequest request) {
     String[] prefill = request.getAttributeArray(idAttribute);
     List<String> prefillList = Arrays.asList(prefill);
     if(!"".equals(timestampAttribute) && !prefillList.contains(timestampAttribute))
     {
       ArrayList<String> pf = new ArrayList<String>(prefillList);
       pf.add(timestampAttribute);
       request.setAttributeArray(pf.toArray(prefill));
     }
     String[] sorting = request.getSortArray();
     if (sorting == null) {
       request.setSortArray(new String[]{idAttribute + ":asc"});
     } else if (!Arrays.asList(sorting).contains(idAttribute + ":asc")) {
       ArrayList<String> sf = new ArrayList<String>(Arrays.asList(sorting));
       sf.add(idAttribute + ":asc");
       request.setSortArray(sf.toArray(sorting));
     }
   }
 
   /**
    * Executes the index process.
    */
   public void run() {
     long start = System.currentTimeMillis();
     try {
       indexCR(this.indexLocation, (CRConfigUtil) this.config);
     } catch (Exception e) {
       log.error(e.getMessage(), e);
     }
     long end = System.currentTimeMillis();
     this.duration = end - start;
   }
 
 }
