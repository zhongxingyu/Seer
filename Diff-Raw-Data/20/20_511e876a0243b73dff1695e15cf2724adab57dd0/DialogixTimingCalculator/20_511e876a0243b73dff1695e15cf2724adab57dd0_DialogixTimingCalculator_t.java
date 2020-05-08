 package org.dialogix.timing;
 
 
 import java.sql.Timestamp; // FIXME - shouldn't we be moving away from sql Timestamps?
 import java.util.*;
 
 import org.dialogix.entities.*;
 import org.dialogix.session.DialogixEntitiesFacadeLocal;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import java.util.logging.*;
 
 /**
 This class consolidates all of the timing functionality, including processing events, and determining response times
  */
 public class DialogixTimingCalculator {
 
     private Logger logger = Logger.getLogger("org.dialogix.timing.DialogixTimingCalculator");
     private boolean initialized = false;
     private long priorTimeEndServerProcessing;
     private long timeBeginServerProcessing;
     private long timeEndServerProcessing;
     private int networkDuration;
     private int serverDuration;
     private int loadDuration;
     private int pageDuration;
     private int totalDuration;
     private InstrumentSession instrumentSession = null;
     private PageUsage pageUsage = null;
     private ArrayList<PageUsage> pageUsages = null;
     private ArrayList<PageUsageEvent> pageUsageEvents = null;
     private HashMap<String, DataElement> dataElementHash = null;
     private HashMap<Integer, Integer> groupNumVisits = null;
     private HashMap<String, ActionType> actionTypeHash = null;
     private HashMap<String, NullFlavor> nullFlavorHash = null;
     private HashMap<Integer, NullFlavor> nullFlavorIntegerHash = null;
     private HashMap<String, NullFlavorChange> nullFlavorChangeHash = null;
     private HashMap<String, ItemEventsBean> itemEventsHash = null;
     private int pageUsageCounter = 0;
     private int pageUsageEventCounter = 0;
     private int itemUsageCounter = 0;
     private boolean finished = false;
     private DialogixEntitiesFacadeLocal dialogixEntitiesFacade = null;
     private HashMap<String, ItemUsage> itemUsageHash = new HashMap<String, ItemUsage>();    
     private HashMap<String, NullFlavor> nullFlavorHistory = new HashMap<String, NullFlavor>();
     private ArrayList<String> excludedReserveds = new  ArrayList<String>();
     /**
     Empty constructor to avoid NullPointerException
      */
     public DialogixTimingCalculator() {
         lookupDialogixEntitiesFacadeLocal();
         initialized = false;
     }
 
     private void setExcludedReserveds() {
         excludedReserveds.add("__BROWSER_TYPE__");
         excludedReserveds.add("__COMPLETED_DIR__"); // may need this to implement study management
         excludedReserveds.add("__CONNECTION_TYPE__");
         excludedReserveds.add("__CURRENT_LANGUAGE__");
         excludedReserveds.add("__DISPLAY_COUNT__");
 //            excludedReserveds.add("__FILENAME__");    // may need this to implmenet study management
         excludedReserveds.add("__FLOPPY_DIR__");    // may need this to implmenet study management
         excludedReserveds.add("__IP_ADDRESS__");
         excludedReserveds.add("__LANGUAGES__");
         excludedReserveds.add("__LOADED_FROM__");
         excludedReserveds.add("__RECORD_EVENTS__");
         excludedReserveds.add("__SCHEDULE_DIR__");
         excludedReserveds.add("__SCHEDULE_SOURCE__");
         excludedReserveds.add("__SCHED_AUTHORS__");
         excludedReserveds.add("__SCHED_VERSION_MAJOR__");
         excludedReserveds.add("__SCHED_VERSION_MINOR__");  
         excludedReserveds.add("__STARTING_STEP__");
         excludedReserveds.add("__START_TIME__");
         excludedReserveds.add("__TRICEPS_FILE_TYPE__");
         excludedReserveds.add("__TRICEPS_VERSION_MAJOR__");
         excludedReserveds.add("__TRICEPS_VERSION_MINOR__");
         excludedReserveds.add("__WORKING_DIR__");        
     }
         
     private void initializeInstrumentSession(HashMap<String,String> reserveds) {
         try {
             pageUsages = new ArrayList<PageUsage>();            
             groupNumVisits = new HashMap<Integer, Integer>();
             
             int startingStep = instrumentSession.getCurrentVarNum();
 
             // Create InstrumentSession Bean - as side effect, sets all startup values (which may be inappropriate)
             Timestamp startTime = new Timestamp(System.currentTimeMillis());
             instrumentSession.setActionTypeId(parseActionType("START"));
             instrumentSession.setBrowser("-");
             instrumentSession.setCurrentGroup(0);   // default in case there are no questions
             instrumentSession.setDisplayNum(0);
             instrumentSession.setFinished(0);   
             instrumentSession.setInstrumentStartingGroup(0);  // default in case there are no questions
             instrumentSession.setIpAddress("-");
             instrumentSession.setLanguageCode("en");
             instrumentSession.setLastAccessTime(startTime);
             instrumentSession.setMaxGroupVisited(0);    // default in case there are no questions
             instrumentSession.setMaxVarNumVisited(startingStep);
             instrumentSession.setNumGroups(instrumentSession.getInstrumentVersionId().getInstrumentHashId().getNumGroups());
             instrumentSession.setNumVars(instrumentSession.getInstrumentVersionId().getInstrumentHashId().getNumVars());
             instrumentSession.setPersonId(null);  // FIXME  - means anonymous
             instrumentSession.setStartTime(startTime);
             instrumentSession.setStatusMsg("init");
 
             // Create the collection of DataElements
             ArrayList<DataElement> dataElements = new ArrayList<DataElement>();
 
             dataElementHash = new HashMap<String, DataElement>();
             Iterator<InstrumentContent> iterator = instrumentSession.getInstrumentVersionId().getInstrumentContentCollection().iterator();
             int lastVarNumVisited = -1;
             while (iterator.hasNext()) {
                 InstrumentContent instrumentContent = iterator.next();
                 DataElement dataElement = new DataElement();
                 dataElement.setDataElementSequence(instrumentContent.getItemSequence());
                 dataElement.setGroupNum(instrumentContent.getGroupNum());
                 dataElement.setItemUsageCollection(new ArrayList<ItemUsage>());
                 dataElement.setInstrumentContentId(instrumentContent);
                 dataElement.setInstrumentSessionId(instrumentSession);
                 dataElement.setItemVisits(0); // will be incremented again (setting it to 1) with fist call to writeNode()
                 dataElement.setVarNameId(instrumentContent.getVarNameId());
                 dataElements.add(dataElement);
                 
                 dataElementHash.put(instrumentContent.getVarNameId().getVarName(), dataElement);
                 if (instrumentContent.getItemSequence() == (startingStep+1)) {
                     instrumentSession.setCurrentGroup(instrumentContent.getGroupNum());
                     instrumentSession.setInstrumentStartingGroup(instrumentContent.getGroupNum());                    
                     instrumentSession.setMaxGroupVisited(instrumentContent.getGroupNum()); // may be called several times
                     lastVarNumVisited = startingStep;
                 }
                 groupNumVisits.put(dataElement.getGroupNum(), 0);
             }
 
             instrumentSession.setMaxVarNumVisited(lastVarNumVisited);  // last VarNum on the screen of maxGroup
             instrumentSession.setFinished(isFinished() ? 1 : 0);
             
             NullFlavor unaskedNullFlavor = parseNullFlavor("*UNASKED*");
             NullFlavor okNullFlavor = parseNullFlavor("*OK*");
             
             Iterator<String> reservedKeys = reserveds.keySet().iterator();
             while (reservedKeys.hasNext()) {
                 String varNameString = reservedKeys.next();
                 if (skipReservedWrite(varNameString)) {
                     continue;
                 }
                 String value = reserveds.get(varNameString);
                 DataElement dataElement = new DataElement();
                 
                 dataElement.setDataElementSequence(-1);
                 dataElement.setGroupNum(-1);
                 dataElement.setInstrumentContentId(null);   
                 dataElement.setInstrumentSessionId(instrumentSession);
                 dataElement.setItemVisits(1);
                 dataElement.setVarNameId(dialogixEntitiesFacade.findVarNameByName(varNameString));
                 dataElement.setItemUsageCollection(new ArrayList<ItemUsage>());
                 
                 /* Initialize it with the starting value */
                 ItemUsage itemUsage = new ItemUsage();
                 
                 itemUsage.setAnswerCode(value);
                 itemUsage.setAnswerId(null);
                 itemUsage.setAnswerString(null);
                 itemUsage.setComments(null);
                 itemUsage.setDataElementId(dataElement);
                 itemUsage.setDisplayNum(0);
                 itemUsage.setItemUsageSequence(++itemUsageCounter);
                 itemUsage.setItemVisit(dataElement.getItemVisits());
                 itemUsage.setLanguageCode(instrumentSession.getLanguageCode());
                 itemUsage.setNullFlavorId(okNullFlavor);
                 itemUsage.setNullFlavorChangeId(parseNullFlavorChange(unaskedNullFlavor, okNullFlavor));
                 itemUsage.setQuestionAsAsked(null);
                 itemUsage.setResponseDuration(0);
                 itemUsage.setResponseLatency(0);
                 itemUsage.setTimeStamp(startTime);
                 itemUsage.setWhenAsMs(startTime.getTime());
                 
                 dataElement.getItemUsageCollection().add(itemUsage);
                 itemUsageHash.put(varNameString, itemUsage);                 
                 nullFlavorHistory.put(varNameString, okNullFlavor);
                 
                 dataElements.add(dataElement);
                 dataElementHash.put(varNameString, dataElement);
             }            
             
             instrumentSession.setDataElementCollection(dataElements);
 
             dialogixEntitiesFacade.persist(instrumentSession);
 
             initialized = true;
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"", e);
         }
     }
     
     /**
      * Either load an instrument from a InstrumentVersion, or restore from an InstrumentSession
      * @param id
      * @param isRestore
      */
     public DialogixTimingCalculator(Long id, boolean isRestore, HashMap<String,String> reserveds) {   
         beginServerProcessing();
         setPriorTimeEndServerProcessing(getTimeBeginServerProcessing());
         setExcludedReserveds();
 
         if (isRestore == true) {
             restoreInstrumentSession(id); 
         }
         else {
             loadInstrumentVersion(id, reserveds);
         }
     }
 
     public String getInstrumentTitle() {
         if (initialized) {
             return instrumentSession.getInstrumentVersionId().getVersionString();
         }
         else {
             return "";
         }
     }
     
     /**
      * Load instrument from a InstrumentVersion entry.  This will also make available the source contents needed for the legacy org.dianexus.triceps code.
      * @param id
      */
     private void loadInstrumentVersion(Long id, HashMap<String,String> reserveds) {   // CHECK THIS
         lookupDialogixEntitiesFacadeLocal();            
 
         InstrumentVersion instrumentVersion = dialogixEntitiesFacade.getInstrumentVersion(id);
         if (instrumentVersion == null) {
             throw new RuntimeException("Unable to find InstrumentVersion " + id);
         }
 
         instrumentSession = new InstrumentSession();
         instrumentSession.setInstrumentVersionId(instrumentVersion);
         instrumentSession.setCurrentVarNum(0);            
 
         /* Overwrite default reserveds with those from the database - but this might duplicate load from instrument_content */
         Iterator<InstrumentHeader> iterator = instrumentSession.getInstrumentVersionId().getInstrumentHeaderCollection().iterator();
         while (iterator.hasNext()) {
             InstrumentHeader instrumentHeader = iterator.next();
             ReservedWord reservedWord = instrumentHeader.getReservedWordId();
             reserveds.put(reservedWord.getReservedWord(), instrumentHeader.getHeaderValue());
         }
         initializeInstrumentSession(reserveds);
     }
 
     private NullFlavorChange parseNullFlavorChange(NullFlavor null0,
                                                    NullFlavor null1) {
         int code0 = 1;  // FIXME - hack - means Unasked
         int code1 = 0;
         if (null0 != null) {
             code0 = null0.getNullFlavorId();
         }
         if (null1 != null) {
             code1 = null1.getNullFlavorId();
         }
         String code = new StringBuffer().append(code0).append(".").append(code1).toString();
         
         if (nullFlavorChangeHash.containsKey(code)) {
             return nullFlavorChangeHash.get(code);
         }
         else {
             return null;
         }
     }
 
     private void restoreInstrumentSession(Long id) {    // CHECK THIS
         try {
             lookupDialogixEntitiesFacadeLocal();            
             
             instrumentSession = dialogixEntitiesFacade.getInstrumentSession(id);
             if (instrumentSession == null) {
                 throw new Exception("Unable to find InstrumentSession " + id);
             }
             
             /* Once session is restored, what else needs to be set?
              * (1) Evidence
              * (2) Schedule
              */
 
             pageUsages = new ArrayList<PageUsage>();            
             groupNumVisits = new HashMap<Integer, Integer>();
             NullFlavor unaskedNullFlavor = parseNullFlavor("*UNASKED*");
             
             /* Re-create the hash of DataElements used to ... (what does it do? - keep track of old values?) */
             /* This should also restore the Reserved words? */
             dataElementHash = new HashMap<String, DataElement>();
             Iterator<DataElement> iterator = instrumentSession.getDataElementCollection().iterator();
             while (iterator.hasNext()) {
                 DataElement dataElement = iterator.next();
                 String varNameString = dataElement.getVarNameId().getVarName();
                 dataElementHash.put(varNameString, dataElement);
                 groupNumVisits.put(dataElement.getGroupNum(), dataElement.getItemVisits()); // FIXME - is this the correct value?
                 
                 /* Do I need to re-populate itemUsageHash?  I don't think so, but may be needed for nullFlavor, in which case could put currentNullFlavor into DataElement? */
 //                Iterator<ItemUsage> itemUsageIterator = dataElement.getItemUsageCollection().iterator();
 //                ItemUsage itemUsage = null;
 //                while (itemUsageIterator.hasNext()) {
 //                    itemUsage = itemUsageIterator.next();
 //                }
 //                itemUsageHash.put(varNameString, itemUsage);    // FIXME - what does this do?
 //                
 //                if (itemUsage == null) {
 //                    nullFlavorHistory.put(varNameString, unaskedNullFlavor);
 //                }
 //                else {
 //                    nullFlavorHistory.put(varNameString, itemUsage.getNullFlavorId());
 //                }
             }
             initialized = true;
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"", e);
         } 
     }
 
     /**
     This is called when the server receives a request.  It starts the the clock for server processing time.
     Side effects include:
     (1) Increasing displayCount
     (2) Setting displayCount, groupNum, and storing that information to pageHits
     @param timestamp	System time in milliseconds
      */
     public void beginServerProcessing() {
         setTimeBeginServerProcessing(System.currentTimeMillis());
         setPerPageParams();
     }
 
     public void logBrowserInfo(String ipAddress,
                         String userAgent) {
         try {
             if (initialized) {
                 instrumentSession.setBrowser(userAgent);
                 instrumentSession.setIpAddress(ipAddress);
                 pageUsage.setBrowser(userAgent);
                 pageUsage.setIpAddress(ipAddress);
             }
         } catch (Exception e) {
             logger.log(Level.SEVERE,"",e);
         }
     }
 
     private void setPerPageParams() {
         try {
             pageUsage = new PageUsage();
             pageUsageEvents = new ArrayList<PageUsageEvent>();
             pageUsageEventCounter = 0;
             pageUsage.setPageUsageEventCollection(pageUsageEvents);
             pageUsage.setPageUsageSequence(++pageUsageCounter);
             if (initialized == true) {
                 instrumentSession.setDisplayNum(instrumentSession.getDisplayNum() + 1);
                 pageUsage.setFromGroupNum(instrumentSession.getCurrentGroup());
                 pageUsage.setPageVisits(groupNumVisits.get(instrumentSession.getCurrentGroup()));
             } else {
                 pageUsage.setFromGroupNum(-1);  // so know hasn't yet been set
                 pageUsage.setPageVisits(0);                
             }
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"beginServerProcessing", e);
         }
     }
 
     /**
     This is called when the server is ready to return a response to the dialogixUser.
     ServerProcessingTime = (finishServerProcessing.timestamp - beginServerProcessing.timestamp)
     @param timestamp	Current system time
      */
     public void finishServerProcessing() {
         try {
             Timestamp ts = new Timestamp(System.currentTimeMillis());
             setTimeEndServerProcessing(System.currentTimeMillis());
             
             if (initialized == false) {
                 logger.log(Level.FINE,"DialogixTimingCalculator not yet initialized");
                 return;
             }
 //            int displayNum = instrumentSession.getDisplayNum() + 1;
 //            instrumentSession.setDisplayNum(displayNum);
 
             // Update Session State
             instrumentSession.setLastAccessTime(ts);
             
             // Need to determine GroupNum from VarNum
             Iterator<DataElement> iterator = instrumentSession.getDataElementCollection().iterator();
             while (iterator.hasNext()) {
                 DataElement dataElement = iterator.next();
                 if (dataElement.getDataElementSequence() == instrumentSession.getCurrentVarNum()) {
                     instrumentSession.setCurrentGroup(dataElement.getGroupNum());
                 }
             }
             instrumentSession.setLanguageCode(getLangCode());
 
             // Add information about this page-worth of usage
             pageUsage.setDisplayNum(instrumentSession.getDisplayNum());
             pageUsage.setLanguageCode(instrumentSession.getLanguageCode());
             pageUsage.setToGroupNum(instrumentSession.getCurrentGroup());
             pageUsage.setActionTypeId(instrumentSession.getActionTypeId());
             pageUsage.setStatusMsg(instrumentSession.getStatusMsg());
             pageUsage.setInstrumentSessionId(instrumentSession);
             pageUsage.setPageVisits(groupNumVisits.get(pageUsage.getFromGroupNum()));   
             pageUsage.setTimeStamp(instrumentSession.getLastAccessTime());
             
             Runtime rt = Runtime.getRuntime();
             pageUsage.setUsedJvmMemory(rt.totalMemory() - rt.freeMemory());
 
             setServerDuration((int) (getTimeEndServerProcessing() - getTimeBeginServerProcessing()));
             setTotalDuration((int) (getTimeBeginServerProcessing() - getPriorTimeEndServerProcessing()));
             setNetworkDuration(getTotalDuration() - getPageDuration());
 
             pageUsage.setLoadDuration(getLoadDuration());
             pageUsage.setNetworkDuration(getNetworkDuration());
             pageUsage.setPageDuration(getPageDuration());
             pageUsage.setServerDuration(getServerDuration());
             pageUsage.setTotalDuration(getTotalDuration());
 
 //            if (!instrumentSession.getStatusMsg().equals("init")) {
                 pageUsages.add(pageUsage);
 //            }
 
             setPriorTimeEndServerProcessing(getTimeEndServerProcessing());
             instrumentSession.setPageUsageCollection(pageUsages);
             instrumentSession.setFinished(isFinished() ? 1 : 0);
 
             dialogixEntitiesFacade.merge(instrumentSession);
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"", e);
         }
     }
 
     private Answer findAnswerId(DataElement dataElement, String encodedAnswer) {
         try {
             AnswerList answerList = dataElement.getInstrumentContentId().getItemId().getAnswerListId();
             if (answerList == null) {
                 return null;
             }
             Iterator<AnswerListContent> iterator = answerList.getAnswerListContentCollection().iterator();
             while (iterator.hasNext()) {
                 AnswerListContent answerListContent = iterator.next();
                 if (answerListContent.getAnswerCode().equals(encodedAnswer)) {
                     return answerListContent.getAnswerId();
                 }
             }
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"Unable to find AnswerId for " + encodedAnswer, e);
         }
         return null;
     }
     
     /**
      * This sets the values of a question before they are asked, so will not include the answer, but may include default (or prior) answers
      * @param VarNameString
      * @param questionAsAsked
      * @param answerCode
      * @param answerString
      * @param comment
      * @param timestamp
      * @param nullFlavor
      */
     public void writeNodePreAsking(String varNameString, String questionAsAsked, String answerCode, String answerString, String comment, Date timestamp, Integer nullFlavor) {
         try {
             DataElement dataElement = dataElementHash.get(varNameString);
             if (dataElement == null) {
                 logger.log(Level.SEVERE,"Attempt to write to unitialized DataElement " + varNameString);
                 return;
             }
 
             ItemUsage itemUsage = new ItemUsage();
             itemUsage.setDisplayNum(instrumentSession.getDisplayNum() + 1);
             itemUsage.setItemUsageSequence(++itemUsageCounter);
             itemUsage.setItemVisit(1);  // the default unless prior visits have been recorded.
             itemUsage.setLanguageCode(instrumentSession.getLanguageCode());
             itemUsage.setQuestionAsAsked(questionAsAsked);
            itemUsage.setTimeStamp(timestamp);
 
             itemUsage.setDataElementId(dataElement);
             dataElement.getItemUsageCollection().add(itemUsage);
             
             if (itemUsageHash.containsKey(varNameString)) {
                 ItemUsage oldItemUsage = itemUsageHash.get(varNameString);
                 itemUsage.setItemVisit(oldItemUsage.getItemVisit() + 1);
             }
             itemUsageHash.put(varNameString, itemUsage);
             
             groupNumVisits.put(dataElement.getGroupNum(), itemUsage.getItemVisit()); // to update visit counts  - only caveat is if an item is NA on a page             
 
             // Compute position relative to end
             if (dataElement.getGroupNum() > instrumentSession.getMaxGroupVisited()) {
                 instrumentSession.setMaxGroupVisited(dataElement.getGroupNum());
             }
             if (dataElement.getDataElementSequence() > instrumentSession.getMaxVarNumVisited()) {
                 instrumentSession.setMaxVarNumVisited(dataElement.getDataElementSequence());
             }
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"WriteNodePreAsking Error", e);
         }
     }    
 
     /**
      * Interface for writing values of data elements
      * @param varNameString
      * @param questionAsAsked
      * @param answerCode
      * @param answerString
      * @param comment
      * @param timestamp
      * @param nullFlavor
      */
     public void writeNode(String varNameString, String questionAsAsked, String answerCode, String answerString, String comment, Date timestamp, Integer nullFlavor) {
         if (!initialized) {
             return;
         }
         try {
             // Update in-memory (and persisted) data store
             DataElement dataElement = dataElementHash.get(varNameString);
             if (dataElement == null) {
                 logger.log(Level.SEVERE,"Attempt to write to unitialized DataElement " + varNameString);
                 return;
             }
 
             ItemUsage itemUsage;
             Long id = null;
             NullFlavor oldNullFlavor = null;
             if (itemUsageHash.containsKey(varNameString)) {
                 itemUsage = itemUsageHash.get(varNameString);
                 dataElement.setItemVisits(itemUsage.getItemVisit());  // this points to most recent, completed item_usage
                 oldNullFlavor = itemUsage.getNullFlavorId();
                 id = itemUsageHash.get(varNameString).getItemUsageId(); // needed to get original itemUsage from ejb store
             }
             if (dataElement.getItemVisits() == 0) { // Will this ever happen?
                 return; // don't write initial *UNASKED* values 
             }
 
             if (id == null) {
                 logger.log(Level.SEVERE,"null id for supposedly persisted ItemUsage " + varNameString);
                 return;
             }
             itemUsage = dialogixEntitiesFacade.getItemUsage(id);
             if (itemUsage == null) {
                 logger.log(Level.SEVERE,"Unable to retrieve ejb for ItemUsage " + varNameString);
                 return;
             }
             
             if (nullFlavorHistory.containsKey(varNameString)) {
                 oldNullFlavor = nullFlavorHistory.get(varNameString);
             }
 
             itemUsage.setAnswerCode(answerCode);
             itemUsage.setAnswerId(findAnswerId(dataElement,answerCode));
             itemUsage.setAnswerString(answerString);
             itemUsage.setComments(comment);
             itemUsage.setNullFlavorId(parseNullFlavor(nullFlavor));
             itemUsage.setNullFlavorChangeId(parseNullFlavorChange(oldNullFlavor, itemUsage.getNullFlavorId()));
             itemUsage.setTimeStamp(timestamp);
             itemUsage.setWhenAsMs(timestamp.getTime());
 
             itemUsage.setDataElementId(dataElement);
             dataElement.getItemUsageCollection().add(itemUsage);
             
             nullFlavorHistory.put(varNameString, itemUsage.getNullFlavorId());
             
             try {
                 ItemEventsBean itemEventsBean = itemEventsHash.get(varNameString);
                 if (itemEventsBean != null) {
                     itemUsage.setResponseLatency(itemEventsBean.getTotalResponseLatency());
                     itemUsage.setResponseDuration(itemEventsBean.getTotalResponseDuration());
                 }
                 else {
                     itemUsage.setResponseDuration(-1);
                     itemUsage.setResponseLatency(-1);
                 }    
             } catch (NullPointerException e) {
                 logger.log(Level.WARNING,"No events found for VarName " + varNameString);
             }
 
             if (dataElement.getGroupNum() > instrumentSession.getMaxGroupVisited()) {
                 instrumentSession.setMaxGroupVisited(dataElement.getGroupNum());
             }
             if (dataElement.getDataElementSequence() > instrumentSession.getMaxVarNumVisited()) {
                 instrumentSession.setMaxVarNumVisited(dataElement.getDataElementSequence());
             }
 
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"WriteNode Error", e);
         }
     }
 
     public void writeReserved(String reservedName, String value) {
         try {
             if (skipReservedWrite(reservedName)) {
                 return;
             }
           Timestamp timestamp = new Timestamp(System.currentTimeMillis());
             
             DataElement dataElement = dataElementHash.get(reservedName);
             if (dataElement == null) {
                 logger.log(Level.SEVERE,"Attempt to write to unitialized Reserved Word " + reservedName);
                 return;
             }
             int visits = dataElement.getItemVisits();
             dataElement.setItemVisits(visits + 1);
             
             ItemUsage newItemUsage = new ItemUsage();
             newItemUsage.setDisplayNum(instrumentSession.getDisplayNum());
             newItemUsage.setItemUsageSequence(++itemUsageCounter);
             newItemUsage.setItemVisit(dataElement.getItemVisits());
             newItemUsage.setLanguageCode(instrumentSession.getLanguageCode());
             newItemUsage.setQuestionAsAsked(null);
             
             newItemUsage.setAnswerCode(value);
             newItemUsage.setAnswerId(null);
             newItemUsage.setAnswerString(null);
             newItemUsage.setComments(null);
             newItemUsage.setNullFlavorId(parseNullFlavor("*OK*"));
             newItemUsage.setNullFlavorChangeId(parseNullFlavorChange(parseNullFlavor("*UNASKED*"), newItemUsage.getNullFlavorId()));
             newItemUsage.setTimeStamp(timestamp);
             newItemUsage.setWhenAsMs(timestamp.getTime());            
 
             newItemUsage.setDataElementId(dataElement);
             dataElement.getItemUsageCollection().add(newItemUsage);
         } catch (Throwable e) {
             logger.log(Level.SEVERE,"WriteReserved Error for (" + reservedName + "," + value +")", e);
         }  
     }
 
     /**
      * Takes parameter src which contains the event timing data from the
      * http request parameter EVENT_TIMINGS and creates an event timing bean for each
      * event.
      *
      * @param eventStrng
      * @return
      */
     public void processEvents(String eventString) {
         if (eventString == null || eventString.trim().length() == 0) {
             return;
         }
         if (initialized == false) {
             return;
         }
         itemEventsHash = new HashMap<String,ItemEventsBean>();
 
         StringTokenizer st = new StringTokenizer(eventString, "\t", false);
         int tokenCount = st.countTokens();
         int priorBlurTime = -1;
         int itemEventCount = 0;
         String priorVarName = null;
         for (int count = 1; st.hasMoreTokens(); ++count) {
             String src = st.nextToken();
             PageUsageEvent pageUsageEvent = tokenizeEventString(src);
             if (pageUsageEvent.getVarName() == null || pageUsageEvent.getVarName().trim().equals("") || pageUsageEvent.getVarName().trim().equals("null")) {
                 logger.log(Level.SEVERE,"Null Varname in " + src);
                 continue;
             }
             else if (pageUsageEvent.getVarName().trim().equals("undefined")) {
                 continue;
             } 
             pageUsageEvents.add(pageUsageEvent);
             
             
             if (!pageUsageEvent.getVarName().equals(priorVarName)) {
                 itemEventCount = 0;
             }
             ++itemEventCount;
 
             if (itemEventsHash.containsKey(pageUsageEvent.getVarName())) {
                 itemEventsHash.get(pageUsageEvent.getVarName()).processPageUsageEvent(pageUsageEvent,priorBlurTime,itemEventCount);
             }
             else {
                 itemEventsHash.put(pageUsageEvent.getVarName(), new ItemEventsBean(pageUsageEvent.getVarName()));
                 itemEventsHash.get(pageUsageEvent.getVarName()).processPageUsageEvent(pageUsageEvent,priorBlurTime,itemEventCount);                
             }
             
             if (count == 1) {
                 setLoadDuration(pageUsageEvent.getDuration());
             }
             if (count == tokenCount) {
                 setPageDuration(pageUsageEvent.getDuration());
             }
             priorBlurTime = pageUsageEvent.getDuration();
             priorVarName = pageUsageEvent.getVarName();
         }
     }
 
     /**
     Parses a single line from Event Timings, storing them within an PageUsageEvent
      * @param src
      * @return
      */
     private PageUsageEvent tokenizeEventString(String src) {
         PageUsageEvent pageUsageEvent = new PageUsageEvent();
 
         StringTokenizer str = new StringTokenizer(src, ",", false);
         int tokenCount = 0;
         while (str.hasMoreTokens()) {
             String token = str.nextToken();
             switch (tokenCount) {
                 case 0: {
                     pageUsageEvent.setVarName(token);
                     break;
                 }
                 case 1: {
                     pageUsageEvent.setGuiActionType(token);
                     break;
                 }
                 case 2: {
                     pageUsageEvent.setEventType(token);
                     break;
                 }
                 case 3: {
                     long ts = new Long(token).longValue();
                     Date d = new Date(ts);
                     Timestamp tms = new Timestamp(d.getTime());
                     pageUsageEvent.setTimeStamp(tms);
                     break;
                 }
                 case 4: {
                     pageUsageEvent.setDuration(new Integer(token).intValue());
                     break;
                 }
                 case 5: {
                     pageUsageEvent.setValue1(stripWhitespace(token));
                     break;
                 }
                 case 6: {
                     StringBuffer sb2 = new StringBuffer(token);
                     // remaining contents may contain commas, and thus be incorrectly treated as tokens
                     // so, merge remaining contents into a single value
                     while (str.hasMoreTokens()) {
                         sb2.append(",").append(str.nextToken());
                     }
                     token = sb2.toString();
 
                     pageUsageEvent.setValue2(stripWhitespace(token));
                     break;
                 }
                 default: {
                     logger.log(Level.SEVERE,"Should never get here, but got '" + token + "'");
                     break;
                 }
             }
 
             tokenCount++;
         }
         if (pageUsageEvent.getValue1() == null) {
             pageUsageEvent.setValue1("");
         }
         if (pageUsageEvent.getValue2() == null) {
             pageUsageEvent.setValue2("");
         }
         if (pageUsageEvent.getGuiActionType() == null) {
             pageUsageEvent.setGuiActionType("");
         }
         if (pageUsageEvent.getEventType() == null) {
             pageUsageEvent.setEventType("");
         }
 
         pageUsageEvent.setPageUsageId(pageUsage);
         pageUsageEvent.setPageUsageEventSequence(++pageUsageEventCounter);
 
         return pageUsageEvent;
     }
 
     public void setLastAction(String lastAction) {
         if (!initialized) {
             return;
         }
         instrumentSession.setActionTypeId(parseActionType(lastAction));  
     }
 
     public void setStatusMsg(String statusMsg) {
         if (!initialized) {
             return;
         }
         instrumentSession.setStatusMsg(statusMsg);
         if ("finished".equals(statusMsg)) {
             setFinished(true);
         }
     }
 
     private boolean isFinished() {
         if (finished == true ||
                 instrumentSession.getMaxGroupVisited() == instrumentSession.getNumGroups() ||
                 instrumentSession.getMaxVarNumVisited() == instrumentSession.getNumVars()) {
             return true;
         } else {
             return false;
         }
     }
 
     private void setFinished(boolean finished) {
         this.finished = finished;   // overrides MaxGroup & VarNum calculations -  for explicitly setting finished status
     }
 
     public void setToVarNum(int varNum) {
         if (!initialized) {
             return;
         }
         instrumentSession.setCurrentVarNum(varNum);
     }
 
     private void setTimeBeginServerProcessing(long time) {
         timeBeginServerProcessing = time;
     }
 
     private long getTimeBeginServerProcessing() {
         return timeBeginServerProcessing;
     }
 
     private void setPriorTimeEndServerProcessing(long time) {
         priorTimeEndServerProcessing = time;
     }
 
     private long getPriorTimeEndServerProcessing() {
         return priorTimeEndServerProcessing;
     }
 
     private void setTimeEndServerProcessing(long time) {
         timeEndServerProcessing = time;
     }
 
     private long getTimeEndServerProcessing() {
         return timeEndServerProcessing;
     }
 
     private void setNetworkDuration(int time) {
         networkDuration = time;
     }
 
     private int getNetworkDuration() {
         return networkDuration;
     }
 
     private int getTotalDuration() {
         return totalDuration;
     }
 
     private void setTotalDuration(int totalDuration) {
         this.totalDuration = totalDuration;
     }
 
     private int getLoadDuration() {
         return loadDuration;
     }
 
     private void setLoadDuration(int loadDuration) {
         this.loadDuration = loadDuration;
     }
 
     private void setServerDuration(int serverDuration) {
         this.serverDuration = serverDuration;
     }
 
     private int getServerDuration() {
         return serverDuration;
     }
 
     private int getPageDuration() {
         return pageDuration;
     }
 
     private void setPageDuration(int pageDuration) {
         this.pageDuration = pageDuration;
     }
 
     public void setLangCode(String langCode) {
         if (!initialized) {
             return;
         }
         if (langCode == null) {
             langCode = "en";
         } else if (langCode.length() > 2) {
             langCode = langCode.substring(0, 2);
         }
         instrumentSession.setLanguageCode(langCode);
     }
 
     public String getLangCode() {
         if (!initialized) {
             return "en";
         } else {
             return instrumentSession.getLanguageCode();
         }
     }
 
     private void lookupDialogixEntitiesFacadeLocal() {
         try {
             Context c = new InitialContext();
             dialogixEntitiesFacade = (DialogixEntitiesFacadeLocal) c.lookup("java:comp/env/DialogixEntitiesFacade_ejbref");
             init();
         } catch (Exception e) {
             logger.log(Level.SEVERE,"", e);
         }
     }
     
     private void init() {
         if (initialized) {
             return;
         }
         Iterator<ActionType> actionTypeIterator = dialogixEntitiesFacade.getActionTypes().iterator();
         actionTypeHash = new HashMap<String,ActionType>();
         while (actionTypeIterator.hasNext()) {
             ActionType actionType = actionTypeIterator.next();
             actionTypeHash.put(actionType.getActionName(), actionType);
         }
         
         Iterator<NullFlavor> nullFlavorIterator = dialogixEntitiesFacade.getNullFlavors().iterator();
         nullFlavorHash = new HashMap<String,NullFlavor>();
         nullFlavorIntegerHash = new HashMap<Integer,NullFlavor>();
         while (nullFlavorIterator.hasNext()) {
             NullFlavor nullFlavor = nullFlavorIterator.next();
             nullFlavorHash.put(nullFlavor.getNullFlavor(), nullFlavor);
             nullFlavorIntegerHash.put(nullFlavor.getNullFlavorId(), nullFlavor);
         } 
         
         Iterator<NullFlavorChange> nullFlavorChangeIterator = dialogixEntitiesFacade.getNullFlavorChanges().iterator();
         nullFlavorChangeHash = new HashMap<String,NullFlavorChange>();
         while (nullFlavorChangeIterator.hasNext()) {
             NullFlavorChange nullFlavorChange = nullFlavorChangeIterator.next();
             nullFlavorChangeHash.put(nullFlavorChange.getNullFlavorChangeCode(), nullFlavorChange);
         }                
     }
     
     private ActionType parseActionType(String token) {
         if (actionTypeHash.containsKey(token)) {
             return actionTypeHash.get(token);
         }
         else {
             return null;
         }
     }
     
     private NullFlavor parseNullFlavor(String token) {
         if (nullFlavorHash.containsKey(token)) {
             return nullFlavorHash.get(token);
         }
         else {
             return null;
         }
     }
     
     private NullFlavor parseNullFlavor(Integer token) {
         if (nullFlavorIntegerHash.containsKey(token)) {
             return nullFlavorIntegerHash.get(token);
         }
         else {
             return null;
         }
     }    
     
     private String stripWhitespace(String s) {
         StringBuffer sb = new StringBuffer();
         if (s == null) {
             return "";
         }
         char[] chars = s.toCharArray();
         char c;
 
         for (int i=0;i<chars.length;++i) {
                 c = chars[i];
                 if (Character.isWhitespace(c)) {
                         sb.append(' ');
                 }
                 else {
                         sb.append(c);
                 }
         }
         return sb.toString();
     }
     
     public boolean isInitialized() {
         return initialized;
     }
     
     public String getInstrumentAsSpreadsheet() {
         if (initialized) {
             return instrumentSession.getInstrumentVersionId().getInstrumentAsSpreadsheet();
         }
         else {
             return null;
         }
     }
     
     private boolean skipReservedWrite(String reservedName) {
         if (excludedReserveds.contains(reservedName)) {
             return true;
         }
         return false;
     }
     
 
     public String getCurrentVarNum() {
         if (initialized) {
             return "" + instrumentSession.getCurrentVarNum();
         }
         else {
             return "0";
         }
     }    
     
     public HashMap<String,String> getCurrentValues() {
         if (!initialized) {
             return null;
         }
         HashMap<String,String> currentValues = new HashMap<String,String>();
         
         Iterator<DataElement> dataElements = instrumentSession.getDataElementCollection().iterator();
         while (dataElements.hasNext()) {
             DataElement dataElement = dataElements.next();
             Iterator<ItemUsage> itemUsages = dataElement.getItemUsageCollection().iterator();
             ItemUsage itemUsage = null;
            int i = 0;
             while (itemUsages.hasNext()) {
                ItemUsage _itemUsage = itemUsages.next();
                if (++i == dataElement.getItemVisits()) {
                    itemUsage = _itemUsage;
                }
             }
             if (itemUsage != null) {
                 currentValues.put(dataElement.getVarNameId().getVarName(), itemUsage.getAnswerCode());
             }
             else {
                 currentValues.put(dataElement.getVarNameId().getVarName(), "*UNASKED*");
             }
         }
         return currentValues;
     }
 }
