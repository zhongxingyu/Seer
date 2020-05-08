 package nl.dancingbear.visbase.rstorecontainer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import nl.cwi.sen1.gui.Studio;
 import nl.cwi.sen1.gui.plugin.DefaultStudioPlugin;
 import nl.cwi.sen1.relationstores.Factory;
 import nl.cwi.sen1.relationstores.types.RElem;
 import nl.cwi.sen1.relationstores.types.RStore;
 import nl.cwi.sen1.relationstores.types.RTuple;
 import nl.cwi.sen1.relationstores.types.relem.Tuple;
 import nl.dancingbear.visbase.rstorecontainer.datatypes.FactInfoList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import aterm.ATerm;
 import aterm.ATermList;
 import aterm.pure.PureFactory;
 
 /**
  * Contains the logic needed to communicate RStore data with other
  * ToolBus-tools.
  * 
  * @note Hidden methods are
  * @c protected instead of
  * @c private so they can be easily unit-tested from tests in the same package.
  * 
  * @author Ricardo Lindooren
  * @author Arend van Beelen (reviewer)
  * @date 12-02-2007
  */
 public class RStoreContainer extends DefaultStudioPlugin implements
         RStoreContainerTif {
 
     private RStoreContainerBridge m_bridge;
 
     private Studio m_metaStudio;
 
     /**
      * Holds the loaded/parsed RStores
      */
     // private Map<Integer, RStore> m_loadedRStoresMap;
     private Map<Integer, RStoreTracker> m_loadedRStoresMap;
 
     /**
      * Used to track which RStore File's where loaded earlier
      */
     private Map<File, Integer> m_earlierLoadedRStoreFilesMap;
 
     private static PureFactory m_pureFactory;
 
     private static final Log m_log = LogFactory.getLog(RStoreContainer.class);
 
     /**
      * The main method is called by the ToolBus to start the RStoreContainer.
      * 
      * @param args
      *            The arguments passed by the ToolBus.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 16-02-2007
      */
     public static void main(String[] args) {
         if (m_log.isDebugEnabled()) {
             if (args != null) {
                 StringBuilder debugMessage = new StringBuilder();
                 debugMessage
                         .append("Starting new RStoreContainer tool with args: ");
 
                 int numArgs = args.length;
                 for (int argNum = 0; argNum < numArgs; argNum++) {
                     debugMessage.append(args[argNum]);
 
                     if (argNum < numArgs - 1) {
                         debugMessage.append(", ");
                     }
                 }
 
                 m_log.debug(debugMessage.toString());
             } else {
                 m_log.debug("Starting new RStoreContainer tool without args.");
             }
         }
 
         new RStoreContainer(args);
     }
 
     /**
      * Default constructor.
      * 
      * Does not make a connection with the ToolBus.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 12-02-2007
      */
     public RStoreContainer() {
         super();
 
        if (m_log.isDebugEnabled()) {
             m_log.info("Default " + RStoreContainer.class.getName()
                     + " constructor called");
         }
 
         m_loadedRStoresMap = new LinkedHashMap<Integer, RStoreTracker>();
         m_earlierLoadedRStoreFilesMap = new LinkedHashMap<File, Integer>();
     }
 
     /**
      * Constructor used when started by the ToolBus.
      * 
      * Initializes connection to the ToolBus with the RStoreContainerBridge.
      * 
      * @param args
      *            The arguments passed by the ToolBus.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 17-02-2007
      */
     protected RStoreContainer(String[] args) {
         super();
 
         m_loadedRStoresMap = new LinkedHashMap<Integer, RStoreTracker>();
         m_earlierLoadedRStoreFilesMap = new LinkedHashMap<File, Integer>();
 
         if (m_log.isInfoEnabled()) {
             m_log.info("Running " + RStoreContainer.class.getSimpleName());
         }
 
         try {
             m_bridge = new RStoreContainerBridge(getPureFactory(), this);
             m_bridge.init(args);
             m_bridge.connect();
             m_bridge.run();
         } catch (Exception exception) {
             if (m_log.isFatalEnabled()) {
                 m_log
                         .fatal("Exception during the initialization of the RStoreContainerBridge, see attached cause (can be ignored during JUnit test when there is no ToolBus process present at the moment): "
                                 + exception);
             }
         }
     }
 
     /**
      * Returns the name to indentify this tool.
      * 
      * @return The string "rStoreContainer".
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 21-02-2007
      */
     public String getName() {
         return "rStoreContainer";
     }
 
     /**
      * Initializes and connects this tool when started from the
      * Meta-Environment.
      * 
      * @param metaStudio
      *            Reference to the Meta-Studio to connect to.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 21-02-2007
      */
     public void initStudioPlugin(Studio metaStudio) {
         m_metaStudio = metaStudio;
 
         m_bridge = new RStoreContainerBridge(m_metaStudio.getATermFactory(),
                 this);
 
         if (m_metaStudio.getATermFactory() instanceof PureFactory) {
             if (m_log.isDebugEnabled()) {
                 m_log
                         .debug("metaStudio.getATermFactory() is an instance of PureFactory. Using this one for the static pureFactory variable.");
             }
 
             m_pureFactory = (PureFactory) m_metaStudio.getATermFactory();
         } else {
             if (m_log.isDebugEnabled()) {
                 m_log
                         .debug("metaStudio.getATermFactory() isn't an instance of PureFactory");
             }
         }
 
         m_metaStudio.connect(getName(), m_bridge);
     }
 
     /**
      * Called by the RStoreContainerInterface ToolBus process to load an RStore
      * file.
      * 
      * @param filename
      *            Filename of the RStore file to load.
      * @return ATerm containing the generated ID for the RStore in the format
      * @c snd-value(rc-rstore-loaded(&lt;str filename&gt;,&lt;int ID&gt;)). The
      *    ID is set to -1 if loading fails.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 2007-02-16
      */
     public ATerm rcLoadRstore(String filename) {
         if (m_log.isDebugEnabled()) {
             m_log.debug("argument: " + filename);
         }
 
         File rStoreFile = new File(filename);
 
         // try to parse the input file to a RStore
         RStore parsedRStore = null;
         try {
             InputStream inputStream = inputStreamFromFile(rStoreFile);
             parsedRStore = parseRStore(inputStream);
         } catch (FileNotFoundException exception) {
             if (m_log.isErrorEnabled()) {
                 m_log.error("File not found!");
             }
         } catch (Exception exception) {
             if (m_log.isErrorEnabled()) {
                 m_log
                         .error(
                                 "Unexpected exception while trying to parse the RStore file (see cause): ",
                                 exception);
             }
         }
 
         // check parsed RStore result again for safety
         int rStoreId = -1;
         if (parsedRStore != null) {
 
             // add parsed RStore to loaded RStores so it can be retrieved later
             // on
             rStoreId = registerRStore(rStoreFile, parsedRStore);
 
             if (m_log.isInfoEnabled()) {
                 m_log.info("Registered RStore with id: " + rStoreId);
             }
 
         } else {
             if (m_log.isWarnEnabled()) {
                 m_log.warn("Could not register RStore, returning id: "
                         + rStoreId);
             }
         }
 
         ATerm result = getPureFactory().make(
                 "snd-value(rc-rstore-loaded(<str>,<int>))", filename,
                 new Integer(rStoreId));
         return result;
     }
 
     /**
      * Called by the RStoreContainerInterface ToolBus process to load the facts
      * from an earlier loaded RStore file.
      * 
      * @param id
      *            ID of the loaded RStore file.
      * @return ATermList containing the ID's of the facts in the loaded RStore
      *         in the format
      * @c snd-value(rc-rstore-facts(&lt;list ID's&gt;)). The list will be empty
      *    if the RStore was not loaded.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 2007-02-16
      */
     public ATerm rcGetRstoreFacts(int id) {
         if (m_log.isDebugEnabled()) {
             m_log.debug("argument: " + id);
         }
 
         RStoreTracker earlierLoadedRStoreTracker = m_loadedRStoresMap
                 .get(new Integer(id));
 
         ATermList factIds = null;
         if (earlierLoadedRStoreTracker != null) {
 
             FactInfoList factsList = earlierLoadedRStoreTracker
                     .getFactInfoFromRStore();
 
             factIds = factsList.toATermList();
         } else {
             if (m_log.isWarnEnabled()) {
                 m_log.warn("RStore didn't exist for ID: " + id
                         + " (returning empty facts list)");
             }
 
             // create empty list
             factIds = getPureFactory().makeList();
         }
 
         ATerm resultList = getPureFactory().make(
                 "snd-value(rc-rstore-facts(<list>))", factIds);
         return resultList;
     }
 
     /**
      * Called by the RStoreContainerInterface ToolBus process to load the data
      * belonging to a fact from an earlier loaded RStore file.
      * 
      * @param rStoreId
      *            ID of the loaded RStore file.
      * @param factId
      *            ID of the fact to load.
      * @return ATerm containing the fact data in the format
      * @c snd-value(rc-fact-data(&lt;term fact-data&gt;)). A fake empty data set
      *    is returned if the fact does not exist.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 2007-02-20
      */
     public ATerm rcGetFactData(int rStoreId, int factId) {
         if (m_log.isDebugEnabled()) {
             m_log.debug("arguments: " + rStoreId + ", " + factId);
         }
 
         RStoreTracker earlierLoadedRStoreTracker = m_loadedRStoresMap
                 .get(new Integer(rStoreId));
 
         ATerm factData = null;
         if (earlierLoadedRStoreTracker != null) {
 
             RTuple factTuple = earlierLoadedRStoreTracker.getRTuple(factId);
 
             if (factTuple != null) {
                 factData = factTuple.toTerm();
             } else {
                 if (m_log.isWarnEnabled()) {
                     m_log.warn("Fact (RTuple) didn't exist for ID: " + factId);
                 }
             }
         } else {
             if (m_log.isWarnEnabled()) {
                 m_log.warn("RStore didn't exist for ID: " + rStoreId + ". Valid ID's are: " + m_loadedRStoresMap.keySet());
             }
         }
 
         if (factData == null) {
             factData = createDummyFactData();
         }
 
         ATerm result = getPureFactory().make("snd-value(rc-fact-data(<term>))",
                 factData);
         return result;
     }
 
     
     /**
      * Called by the RStoreContainerInterface ToolBus process to unload an earlier loaded RStore
      * 
      * @param id the ID of the earlier loaded RStore
      * 
      * @return the message <code>snd-value(rc-rstore-unloaded(<int>))</code>
      * 
      * @author Ricardo Lindooren
      * @Date 2007-03-14
      */
     public ATerm rcUnloadRstore(int id) {
         if (m_log.isDebugEnabled()) {
             m_log.debug("argument: " + id);
         }
 
         Integer nrIdentifier = new Integer(id);
 
         if (m_loadedRStoresMap.containsKey(nrIdentifier)) {
 
             // Delete corresponding entry from earlier loader RStoreTrackers map
             m_loadedRStoresMap.remove(nrIdentifier);
 
             /* Delete corresponding entry from earlier loaded files map */
             
             Integer storedIdentifier = null;
             File storedFileReference = null;
             
             // search for entry
             for (File file : m_earlierLoadedRStoreFilesMap.keySet()) {
                 storedIdentifier = m_earlierLoadedRStoreFilesMap.get(file);
                 
                 // check if we got a RStore(Tracker) and this is the RStore(Tracker) we are looking for
                 if (storedIdentifier != null && storedIdentifier.equals(nrIdentifier)) {
                     storedFileReference = file;
                     break;
                 }
             }
             
             // remove the file reference (do this outside the for-loop)
             if (storedFileReference != null ) {
                 
                 if (m_log.isDebugEnabled()) {
                     m_log.debug("Unloading RStore with identifier, id: " + storedIdentifier + ", file: " + storedFileReference);
                 }
                 
                 m_earlierLoadedRStoreFilesMap.remove(storedFileReference);
             }
         }
         else
         {
             nrIdentifier = new Integer(-1);
             
             if (m_log.isWarnEnabled()){
                 m_log.warn("No earlier loaded RStore for identifier: " + nrIdentifier + ". *Returning -1*");
             }
         }
         
         ATerm resultList = getPureFactory().make(
                 "snd-value(rc-rstore-unloaded(<int>))", nrIdentifier);
         
         return resultList;
     }
     
     /**
      * Called by the RStoreContainerInterface ToolBus process when the fact-update event is received
      * 
      * @author Ricardo Lindooren
      * @Date 2007-03-14
      */
     public void recAckEvent(ATerm t0) {
         if (m_log.isDebugEnabled()) {
             m_log.debug("argument: " + t0);
         }
     }
 
     /**
      * Creates dummy fact data which is used when no data can be loaded.
      * 
      * @return An ATerm containing a tuple with two empty strings.
      * 
      * @author Arend van Beelen
      * @date 06-03-2007
      */
     public static ATerm createDummyFactData() {
         Factory factory = Factory.getInstance(getPureFactory());
 
         RElem emptyString = factory.makeRElem_Str("");
         Tuple tuple = factory.makeRElem_Tuple(factory.makeRElemElements(
                 emptyString, emptyString));
         RElem rElem = factory.makeRElem_Set(factory.makeRElemElements(tuple));
         ATerm factData = rElem.toTerm();
 
         return factData;
     }
     
     /**
      * Handles terminate message.
      * 
      * @param message
      *            The received message.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 20-02-2007
      */
     public void recTerminate(ATerm message) {
         if (m_log.isDebugEnabled()) {
             m_log.debug("Received terminate message: " + message + ".");
         }
 
         m_bridge = null;
         m_metaStudio = null;
     }
 
     /**
      * Returns the PureFactory object used by this class.
      * 
      * This class uses a single static instance of the PureFactory.
      * 
      * Reason for doing this is the Factory stays alive independently from this
      * class. The Factory doesn't like it when a new instance is created with a
      * new PureFactory.
      * 
      * @return The static PureFactory instance for this class.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 14-02-2007
      */
     public static PureFactory getPureFactory() {
         if (m_pureFactory == null) {
             if (m_log.isDebugEnabled()) {
                 m_log.debug("Created the static PureFactory.");
             }
 
             m_pureFactory = new PureFactory();
         }
         return m_pureFactory;
     }
 
     /**
      * Returns the mapping from ID's to the loaded RStoreTrackers.
      * 
      * @return The map containing all loaded RStores.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 14-02-2007
      */
     public Map<Integer, RStoreTracker> getLoadedRStoreTrackersMap() {
         return m_loadedRStoresMap;
     }
 
     /**
      * Returns the mapping from RStore filenames to ID's (belonging to the
      * loaded RStoreTrackers).
      * 
      * @see #getLoadedRStoreTrackersMap()
      * 
      * @return The map containing all loaded RStores.
      * 
      * @author Ricardo Lindooren
      * @date 2007-03-14
      */
     public Map<File, Integer> getLoadedRStoreFilesMap() {
         return m_earlierLoadedRStoreFilesMap;
     }
 
     /**
      * Creates an InputStream object for the given file.
      * 
      * This is very generic code, not limited to be used by this class only. It
      * could be promoted to a static method (in a Helper/Util kind of class for
      * example).
      * 
      * @param file
      *            File to create an input stream for.
      * @return A new FileInputStream object for the input File.
      * 
      * @throws RuntimeException
      *             if the input stream is null.
      * @throws FileNotFoundException
      *             if the input File cannot be found.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 12-02-2007
      */
     protected InputStream inputStreamFromFile(File file)
             throws FileNotFoundException {
         if (file == null) {
             throw new RuntimeException("Input file should not be null.");
         }
 
         InputStream inputStream = new FileInputStream(file);
         return inputStream;
     }
 
     /**
      * Parses RStore data from an input stream.
      * 
      * @param inputStream
      *            The input stream to parse.
      * @return The parsed RStore.
      * 
      * @throws RuntimeException
      *             if the input stream is null.
      * @throws RStoreParseException
      *             if parsing of RStore input fails.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 12-02-2007
      */
     protected RStore parseRStore(InputStream inputStream)
             throws RStoreParseException {
         if (inputStream == null) {
             throw new RuntimeException("Input stream should not be null.");
         }
 
         RStore parsedRStore = null;
 
         try {
             Factory factory = Factory.getInstance(getPureFactory());
 
             parsedRStore = factory.RStoreFromFile(inputStream);
         } catch (Exception exception) {
             throw new RStoreParseException(
                     "Parsing of RStore data failed (see cause)", exception);
         }
 
         return parsedRStore;
     }
 
     /**
      * Registers a loaded RStore in the RStores map.
      * 
      * @param rStoreFileReference
      *            the reference to the File that was used to create the RStore
      * @param rStore
      *            The RStore to register.
      * 
      * @return The ID assigned to the RStore in the map.
      * 
      * @throws RuntimeException
      *             if File and/or RStore input is null.
      * 
      * @author Ricardo Lindooren
      * @author Arend van Beelen (reviewer)
      * @date 14-02-2007
      */
     protected int registerRStore(File rStoreFileReference, RStore rStore) {
         if (rStoreFileReference == null) {
             throw new RuntimeException("File input should not be null");
         }
         if (rStore == null) {
             throw new RuntimeException("RStore input should not be null");
         }
 
         // Default is a fake value
         int result_id = -1;
 
         // Check if the File was loaded earlier
         if (m_earlierLoadedRStoreFilesMap.containsKey(rStoreFileReference)) {
             if (m_log.isInfoEnabled()) {
                 m_log.info("RStore file has been loaded earlier: "
                         + rStoreFileReference + ". *Updating if needed*");
             }
 
             // Get the identifier for this earlier loaded RStore
             Integer idOfEarlierLoadedRStore = m_earlierLoadedRStoreFilesMap
                     .get(rStoreFileReference);
 
             // Get the RStore Tracker
             RStoreTracker rStoreTrackerForEarlierLoadedRStore = m_loadedRStoresMap
                     .get(idOfEarlierLoadedRStore);
 
             // Update the Tracker with the newly parsed RStore data
             List<Integer> updatedFactIdsList = rStoreTrackerForEarlierLoadedRStore
                     .update(rStore);
 
             // Send updates for changed facts (if any)
             sendFactUpdatedEvents(idOfEarlierLoadedRStore, updatedFactIdsList);
 
             result_id = idOfEarlierLoadedRStore.intValue();
         } else {
             // find a unique ID for the RStore in the map
             int id = m_loadedRStoresMap.size() + 1;
             while (m_loadedRStoresMap.get(new Integer(id)) != null) {
                 if (m_log.isWarnEnabled()) {
                     m_log
                             .warn("Suggested ID for RStore already existed in loadedRStoresMap: "
                                     + id);
                 }
                 id++;
             }
 
             // Create a new RStoreTracker
             RStoreTracker newRStoreTracker = new RStoreTracker(rStore);
 
             Integer newNrIdentifier = new Integer(id);
 
             m_earlierLoadedRStoreFilesMap.put(rStoreFileReference,
                     newNrIdentifier);
             m_loadedRStoresMap.put(newNrIdentifier, newRStoreTracker);
 
             result_id = id;
         }
 
         return result_id;
     }
 
     /**
      * Sends <code>snd-event(rc-fact-updated(<int>,<int>))</code> messages
      * (RStore,FactId) over the toolbus
      * 
      * @param rStoreId
      *            the ID of the RStore to which the updated facts belong
      * @param updatedFactIds
      *            a list of ID's of updated facts
      * 
      * @throws RuntimeException
      *             if input is null.
      * 
      * @author Ricardo Lindooren
      * @date 2007-03-13
      */
     protected void sendFactUpdatedEvents(Integer rStoreId,
             List<Integer> updatedFactIds) {
         if (rStoreId == null) {
             throw new RuntimeException("rStoreId input should not be null");
         }
         if (updatedFactIds == null) {
             throw new RuntimeException(
                     "updatedFactIds input should not be null");
         }
         
         for (Integer updatedFactId : updatedFactIds) {
             
             ATerm updatedFactEvent = getPureFactory().make("rc-fact-updated(<int>,<int>)", rStoreId, updatedFactId);
                                                                
             if (m_bridge != null) {
                 if (m_log.isDebugEnabled()) {
                     m_log.debug("Sending this updatedFact-event as term: "
                             + updatedFactEvent);
                 }
 
                 m_bridge.postEvent(updatedFactEvent);
 
             } else {
                 if (m_log.isWarnEnabled()) {
                     m_log
                             .warn("There is no bridge, cannot send this updatedFact-event: "
                                     + updatedFactEvent);
                 }
             }
         }
     }
 }
