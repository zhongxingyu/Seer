 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.awe.afp.loaders;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.awe.afp.AfpNeighbourSubType;
 import org.amanzi.awe.afp.executors.AfpProcessExecutor;
 import org.amanzi.awe.afp.files.ControlFile;
 import org.amanzi.awe.afp.providers.AbstractTxFileHandler;
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.amanzi.neo.core.utils.importer.CommonImporter;
 import org.amanzi.neo.loader.AbstractLoader;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.amanzi.neo.services.utils.RunnableWithResult;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 // TODO: Auto-generated Javadoc
 /**
  * <p>
  * AFP files loader
  * </p>
  * .
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class AfpLoader extends AbstractLoader {
 
     /** The Constant CELL_IND. */
     public static final Integer CELL_IND = 1;
 
     /** The file. */
     private final ControlFile file;
 
     /** The root name. */
     private final String rootName;
 
     /** The afp root. */
     private Node afpRoot;
 
     /** The afp cell. */
     private Node afpCell;
 
     /** The lucene ind. */
     private LuceneIndexService luceneInd;
 
     /**
      * Instantiates a new afp loader.
      * 
      * @param rootName the root name
      * @param file the file
      * @param service the service
      */
     public AfpLoader(final String rootName, final ControlFile file, final GraphDatabaseService service) {
         this.rootName = rootName;
         this.file = file;
         this.neo = service;
         luceneInd = NeoServiceProviderUi.getProvider().getIndexService();
 
     }
 
     /**
      * Define root.
      */
     protected void defineRoot() {
     	defineRoot(null);
     }
     protected void defineRoot(String projectName) {
         RunnableWithResult<Node> creater = new RunnableWithResult<Node>() {
 
             private Node node = null;
 
             @Override
             public void run() {
 
                 Transaction tx = neo.beginTx();
                 try {
                     node = neo.createNode();
                     NodeTypes.NETWORK.setNodeType(node, neo);
                     NeoUtils.setNodeName(node, rootName, neo);
                     for (Map.Entry<String, String> entry : file.getPropertyMap().entrySet()) {
                         node.setProperty(entry.getKey(), entry.getValue());
                     }
                     tx.success();
                 } finally {
                     tx.finish();
                 }
 
             }
 
             @Override
             public Node getValue() {
                 return node;
             }
         };
         if(projectName == null) {
         	projectName = ApplicationGIS.getActiveProject().getName();
         }
         afpRoot = NeoUtils.findorCreateRootInActiveProject(projectName, rootName, creater, neo);
         
     }
     
     private boolean isMonitorCancelled(IProgressMonitor monitor){
     	if (monitor.isCanceled()){
     		mainTx.failure();
     		commit(false);
     		return true;
     	}
     	
     	return false;
     }
 
     /**
      * Run.
      * 
      * @param monitor the monitor
      * @throws IOException Signals that an I/O exception has occurred.
      */
     @Override
     public void run(IProgressMonitor monitor) throws IOException {
         monitor.beginTask("Load AFP data", 7);
         runAfpLoader(monitor,null);
     }
     
     public void runAfpLoader(IProgressMonitor monitor, String projectName) {
         if (file.getCellFile() == null) {
             error("Not found Cite file");
             return;
         }
         mainTx = neo.beginTx();
         NeoUtils.addTransactionLog(mainTx, Thread.currentThread(), "AfpLoader");
         try {
             defineRoot(projectName);
             if (file.getCellFile() != null) {
             	if (isMonitorCancelled(monitor))
             		return; 	
                 loadCellFile(file.getCellFile());
             }
             commit(true);
             monitor.worked(1);
             if (file.getForbiddenFile() != null) {
             	if (isMonitorCancelled(monitor))
             		return;
                 loadForbiddenFile(file.getForbiddenFile());
             }
 
             commit(true);
             monitor.worked(1);
             if (file.getNeighbourFile() != null) {
             	if (isMonitorCancelled(monitor))
             		return;
                 loadNeighbourFile(file.getNeighbourFile(), afpCell);
             }
             
             commit(true);
             monitor.worked(1);
             if (file.getExceptionFile() != null) {
             	if (isMonitorCancelled(monitor))
             		return;
                 loadExceptionFile(file.getExceptionFile(), afpCell);
             }
             commit(true);
             monitor.worked(1);
             if (file.getInterferenceFile() != null) {
             	if (isMonitorCancelled(monitor))
             		return;
                 loadInterferenceFile(file.getInterferenceFile(), afpCell);
             }
             commit(true);
             monitor.worked(1);
            
             saveProperties();
         } finally {
             commit(false);
         }
 
         
 
     }
 
     /**
      * @param interferenceFile
      * @param monitor
      */
     private void loadInterferenceFile(File interferenceFile, Node afpCell) {
     	this.afpCell = afpCell;
          Node afpInterference = NeoUtils.findNeighbour(afpCell, interferenceFile.getName(), neo);
          if (afpInterference == null) {
 	         Transaction tx = neo.beginTx();
 	         try {
 		         afpInterference = neo.createNode();
 		         afpInterference.setProperty(INeoConstants.PROPERTY_TYPE_NAME,
 		         NodeTypes.NEIGHBOUR.getId());
 		         AfpNeighbourSubType.INTERFERENCE.setTypeToNode(afpInterference, neo);
 		         afpInterference.setProperty(INeoConstants.PROPERTY_NAME_NAME, interferenceFile.getName());
 		         afpCell.createRelationshipTo(afpInterference, NetworkRelationshipTypes.INTERFERENCE_DATA);
 		         tx.success();
 	         } finally {
 	        	 tx.finish();
 	         }
          }
          CommonImporter importer = new CommonImporter(new InterferenceFileHandler(afpInterference,
          neo), new TxtFileImporter(interferenceFile));
          importer.process();
     }
 
     /**
      * @param exceptionFile
      * @param monitor
      */
     private void loadExceptionFile(File exceptionFile, Node afpCell) {
     	this.afpCell = afpCell;
         Node afpException = NeoUtils.findNeighbour(afpCell, exceptionFile.getName(), neo);
         if (afpException == null) {
             Transaction tx = neo.beginTx();
             try {
                 afpException = neo.createNode();
                 afpException.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.NEIGHBOUR.getId());
                 AfpNeighbourSubType.EXCEPTION.setTypeToNode(afpException, neo);
                 afpException.setProperty(INeoConstants.PROPERTY_NAME_NAME, exceptionFile.getName());
                 afpCell.createRelationshipTo(afpException, NetworkRelationshipTypes.EXCEPTION_DATA);
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
         CommonImporter importer = new CommonImporter(new ExceptionFileHandler(afpException, neo), new TxtFileImporter(exceptionFile));
         importer.process();
     }
 
     /**
      * Load neighbour file.
      * 
      * @param neighbourFile the neighbour file
      * @param monitor the monitor
      */
     public void loadNeighbourFile(File neighbourFile, Node afpCell) {
     	this.afpCell = afpCell;
         Node afpNeigh = NeoUtils.findNeighbour(afpCell, neighbourFile.getName(), neo);
         if (afpNeigh == null) {
 //            Transaction tx = neo.beginTx();
             try {
                 afpNeigh = neo.createNode();
                 afpNeigh.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.NEIGHBOUR.getId());
                 afpNeigh.setProperty(INeoConstants.PROPERTY_NAME_NAME, neighbourFile.getName());
                 afpCell.createRelationshipTo(afpNeigh, NetworkRelationshipTypes.NEIGHBOUR_DATA);
                 mainTx.success();
             } catch (Exception e) {
             	mainTx.failure();
             }
         }
         CommonImporter importer = new CommonImporter(new NeighbourFileHandler(afpNeigh, neo), new TxtFileImporter(neighbourFile));
         importer.process();
     }
 
     /**
      * Load forbidden file.
      * 
      * @param forbiddenFile the forbidden file
      * @param monitor the monitor
      */
     private void loadForbiddenFile(File forbiddenFile) {
 
         CommonImporter importer = new CommonImporter(new ForbiddenFileHandler(afpCell, neo), new TxtFileImporter(forbiddenFile));
         importer.process();
     }
 
     /**
      * Find root.
      * 
      * @param name the name
      * @return the node
      */
     private Node findRoot(String name) {
         Transaction tx = neo.beginTx();
         try {
             // afpRoot
             return null;
         } finally {
 
         }
     }
 
     /**
      * Load cell file.
      * 
      * @param cellFile the cell file
      * @param monitor the monitor
      */
     private void loadCellFile(File cellFile) {
         // TODO define root of cell file. If we create virtual dataset for it what we should store
         // in main part?
         afpCell = afpRoot;
         CommonImporter importer = new CommonImporter(new CellFileHandler(afpCell, neo), new TxtFileImporter(cellFile));
         importer.process();
     }
 
     /**
      * Need parce headers.
      * 
      * @return true, if successful
      */
     @Override
     protected boolean needParceHeaders() {
         return false;
     }
 
     /**
      * Parses the line.
      * 
      * @param line the line
      */
     @Override
    protected int parseLine(String line) {
        return 0;
     }
 
     /**
      * Gets the prymary type.
      * 
      * @param key the key
      * @return the prymary type
      */
     @Override
     protected String getPrymaryType(Integer key) {
         if (key.equals(CELL_IND)) {
             return NodeTypes.M.getId();
         }
         return null;
     }
 
     /**
      * Gets the storing node.
      * 
      * @param key the key
      * @return the storing node
      */
     @Override
     protected Node getStoringNode(Integer key) {
         if (key.equals(CELL_IND)) {
             return afpCell;
         }
         return null;
     }
 
     /**
      * Flush indexes.
      */
     @Override
     protected void flushIndexes() {
     }
 
     /**
      * Gets the root nodes.
      * 
      * @return the root nodes
      */
     @Override
     public Node[] getRootNodes() {
         return new Node[] {afpCell};
     }
 
     /**
      * Adds the child.
      * 
      * @param parent the parent
      * @param type the type
      * @param name the name
      * @param indexName the index name
      * @return the node
      */
     private Node addChild(Node parent, NodeTypes type, String name, String indexName) {
         Node child = null;
         child = neo.createNode();
         child.setProperty(INeoConstants.PROPERTY_TYPE_NAME, type.getId());
         child.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
         luceneInd.index(child, NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, type), indexName);
         if (parent != null) {
             parent.createRelationshipTo(child, NetworkRelationshipTypes.CHILD);
             debug("Added '" + name + "' as child of '" + parent.getProperty(INeoConstants.PROPERTY_NAME_NAME));
         }
         return child;
     }
     
     
     /**
      * @param sector the sector whose proxy is to be created
      * @param lastSector sector whose proxy was created last 
      * @param rootNode the list(neighbours/interference/exception) node corresponding to this proxy
      * @param type the relationship type for proxySector
      * @return
      */
     private Node createProxySector(Node sector, Node lastSector, Node rootNode, NetworkRelationshipTypes type){
     	
         Node proxySector;
     		
             Transaction tx = neo.beginTx();
             try {
             	proxySector = neo.createNode();
             	String sectorName = sector.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
             	String proxySectorName = NeoUtils.getNodeName(rootNode, neo) + "/" + sectorName;
             	proxySector.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS.getId());                    	
             	proxySector.setProperty(INeoConstants.PROPERTY_NAME_NAME, proxySectorName);
             	
             	//TODO: bad way. fix it to check lastSector.equals(rootNode)
             	if (lastSector == null || lastSector.equals(rootNode))
             		rootNode.createRelationshipTo(proxySector, NetworkRelationshipTypes.CHILD);
             	else 
             		lastSector.createRelationshipTo(proxySector, NetworkRelationshipTypes.NEXT);
                 
             	sector.createRelationshipTo(proxySector, type);
             	
             	luceneInd.index(proxySector, NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS), proxySectorName);
             	
             	tx.success();
             } finally {
                 tx.finish();
             }
         	
         	return proxySector;
     }
 
     /**
      * <p>
      * CellFileHandler handle import of Cell File
      * </p>
      * .
      * 
      * @author TsAr
      * @since 1.0.0
      */
     public class CellFileHandler extends AbstractTxFileHandler {
 
         /** The header. */
         private LinkedHashMap<String, Header> header;
 
         /**
          * Instantiates a new cell file handler.
          * 
          * @param rootNode the root node
          * @param service the service
          */
         public CellFileHandler(Node rootNode, GraphDatabaseService service) {
             super(rootNode, service);
             header = getHeaderMap(CELL_IND).headers;
         }
 
         /**
          * Store line.
          * 
          * @param line the line
          */
         @Override
         protected void storeLine(String line) {
             try {
                 String[] field = line.split("\\s");
                 int i = 0;
                 String siteName = field[i++];
                 Integer sectorNo = Integer.valueOf(field[i++]);
                 Integer nonrelevant = Integer.valueOf(field[i++]);
                 Integer numberoffreqenciesrequired = Integer.valueOf(field[i++]);
                 Integer numberoffrequenciesgiven = Integer.valueOf(field[i++]);
                 Integer[] frq = new Integer[numberoffrequenciesgiven];
                 for (int j = 0; j < frq.length; j++) {
                     frq[j] = Integer.valueOf(field[i++]);
                 }
                 Transaction tx = service.beginTx();
                 try {
                     Node site = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SITE), siteName);
                     if (site == null) {
                         site = addChild(afpCell, NodeTypes.SITE, siteName, siteName);
                     }
                     String sectorName = siteName + field[1];
                     Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
                     if (sector == null) {
                         sector = addChild(site, NodeTypes.SECTOR, sectorName, sectorName);
                     }
                     setIndexProperty(header, sector, "nonrelevant", nonrelevant);
                     setIndexProperty(header, sector, "numberoffreqenciesrequired", numberoffreqenciesrequired);
                     setIndexProperty(header, sector, "numberoffrequenciesgiven", numberoffrequenciesgiven);
                     sector.setProperty("frq", frq);
                     tx.success();
                 } finally {
                     tx.finish();
                 }
             } catch (Exception e) {
                 String errStr = String.format("Can't parse line: %s", line);
                 AweConsolePlugin.error(errStr);
                 Logger.getLogger(this.getClass()).error(errStr, e);
             }
         }
 
     }
 
     /**
      * <p>
      * ForbiddenFileHandler handle import of Forbidden File
      * </p>
      * .
      * 
      * @author TsAr
      * @since 1.0.0
      */
     public class ForbiddenFileHandler extends AbstractTxFileHandler {
 
         /** The header. */
         private LinkedHashMap<String, Header> header;
 
         /**
          * Instantiates a new cell file handler.
          * 
          * @param rootNode the root node
          * @param service the service
          */
         public ForbiddenFileHandler(Node rootNode, GraphDatabaseService service) {
             super(rootNode, service);
             header = getHeaderMap(CELL_IND).headers;
         }
 
         /**
          * Store line.
          * 
          * @param line the line
          */
         @Override
         protected void storeLine(String line) {
             try {
                 // TODO debug - in example do not have necessary file
                 String[] field = line.split("\\s");
                 int i = 0;
                 String siteName = field[i++];
                 Integer sectorNo = Integer.valueOf(field[i++]);
                 Integer numberofforbidden = Integer.valueOf(field[i++]);
                 Integer[] forbList = new Integer[numberofforbidden];
                 for (int j = 0; j < forbList.length; j++) {
                     forbList[j] = Integer.valueOf(field[i++]);
                 }
                 String sectorName = siteName + field[1];
                 Transaction tx = service.beginTx();
                 try {
                     Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
                     if (sector == null) {
                         error("Forbidden Frquencies File. Not found in network sector " + sectorName);
                         return;
                     }
                     setIndexProperty(header, sector, "numberofforbidden", numberofforbidden);
                     sector.setProperty("forb_fr_list", forbList);
                     tx.success();
                 } finally {
                     tx.finish();
                 }
             } catch (Exception e) {
                 String errStr = String.format("Can't parse line: %s", line);
                 AweConsolePlugin.error(errStr);
                 Logger.getLogger(this.getClass()).error(errStr, e);
             }
         }
 
     }
 
     /**
      * <p>
      * NeighbourFileHandler handle import of Forbidden File
      * </p>
      * .
      * 
      * @author TsAr
      * @since 1.0.0
      */
     public class NeighbourFileHandler extends AbstractTxFileHandler {
 
         /** The serve. */
         private Node serve;
         private Set<String> numericProp;
         private Set<String> allProp;
         private String neighName;
         private Node lastSector;
 
         /**
          * Instantiates a new cell file handler.
          * 
          * @param rootNode the root node
          * @param service the service
          */
         public NeighbourFileHandler(Node rootNode, GraphDatabaseService service) {
             super(rootNode, service);
             neighName = NeoUtils.getNodeName(rootNode, service);
         }
 
         /**
          * Inits the.
          */
         @Override
         public void init() {
             super.init();
             serve = null;
             numericProp = new HashSet<String>();
             allProp = new HashSet<String>();
             lastSector = rootNode;
             
         }
 
         @Override
         public void finish() {
             super.finish();
             rootNode.setProperty(INeoConstants.LIST_NUMERIC_PROPERTIES, numericProp.toArray(new String[0]));
             rootNode.setProperty(INeoConstants.LIST_NUMERIC_PROPERTIES, allProp.toArray(new String[0]));
         }
 
         /**
          * Store line.
          * 
          * @param line the line
          */
         @Override
         protected void storeLine(String line) {
             try {
                 String[] field = line.split("\\s");
                 int i = 0;
                 String name = field[i++].trim();
                 String siteName = field[i++];
                 Integer sectorNo = Integer.valueOf(field[i++]);
                 if (name.equals("CELL")) {
                     serve = defineServe(siteName, field[2]);
                 } else {
                     if (serve == null) {
                         error("Not found serve cell for neighbours: " + line);
                         return;
                     } else {
                         defineNeigh(siteName, field[2]);
                     }
                 }
             } catch (Exception e) {
                 String errStr = String.format("Can't parse line: %s", line);
                 AweConsolePlugin.error(errStr);
                 Logger.getLogger(this.getClass()).error(errStr, e);
             }
         }
 
         /**
          * Define neigh.
          * 
          * @param siteName the site name
          * @param field the field
          */
         private void defineNeigh(String siteName, String field) {
             String sectorName = siteName.trim() + field.trim();
             String proxySectorName = neighName + "/" + sectorName;
             
             Node proxySector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS), proxySectorName);
         	if (proxySector == null) {
         		Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
                 if (sector == null) {
                     error(". Neighbours File. Not found sector " + sectorName);
                     return;
                 }
                 proxySector = createProxySector(sector, lastSector, rootNode, NetworkRelationshipTypes.NEIGHBOURS);
                 lastSector = proxySector;
         	}
             
             serve.createRelationshipTo(proxySector, NetworkRelationshipTypes.NEIGHBOUR);
         }
 
         /**
          * Define serve.
          * 
          * @param siteName the site name
          * @param field the field
          * @return the node
          */
         private Node defineServe(String siteName, String field) {
             String sectorName = siteName.trim() + field.trim();
             String proxySectorName = neighName + "/" + sectorName;
             
             Node proxySector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS), proxySectorName);
             	if (proxySector == null) {
             		Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
                     if (sector == null) {
                         error(". Neighbours File. Not found sector " + sectorName);
                         return null;
                     }
                     proxySector = createProxySector(sector, lastSector, rootNode, NetworkRelationshipTypes.NEIGHBOURS);
                     lastSector = proxySector;
             	}           
           
             return proxySector;
         }
 
     }
 
      /**
      * <p>
      * InterferenceFileHandler handle import of Interference File
      * </p>
      * .
      *
      * @author TsAr
      * @since 1.0.0
      */
      public class InterferenceFileHandler extends AbstractTxFileHandler {
     
      /** The serve. */
      private Node serve;
      private Set<String> numericProp;
      private Set<String> allProp;
      private String interferenceName;
      private Node lastSector;
     
      /**
      * Instantiates a new cell file handler.
      *
      * @param rootNode the root node
      * @param service the service
      */
      public InterferenceFileHandler(Node rootNode, GraphDatabaseService service) {
      super(rootNode, service);
      interferenceName = NeoUtils.getNodeName(rootNode, service);
      }
     
      /**
      * Inits the.
      */
      @Override
      public void init() {
      super.init();
      serve = null;
      numericProp = new HashSet<String>();
      allProp = new HashSet<String>();
      }
     
      @Override
      public void finish() {
      super.finish();
      rootNode.setProperty(INeoConstants.LIST_NUMERIC_PROPERTIES, numericProp.toArray(new
      String[0]));
      rootNode.setProperty(INeoConstants.LIST_NUMERIC_PROPERTIES, allProp.toArray(new String[0]));
      }
     
      /**
      * Store line.
      *
      * @param line the line
      */
      @Override
      protected void storeLine(String line) {
 	     try {
 		     String[] field = line.split("\\s");
 		     int i = 0;
 		     String name = field[i++].trim();
 		     if (name.equals("SUBCELL")) {
 		    	 String sectorName = field[6];
 		    	 String sectorNo = sectorName.substring(sectorName.length() - 1); 
 		    	 if (!sectorNo.matches("\\d")){
 		    		 int diff = Character.getNumericValue(sectorName.charAt(sectorName.length() - 1)) - Character.getNumericValue('A') + 1;
 		    		 sectorName = sectorName.substring(0, sectorName.length() - 1) + diff;
 		    	 }		    		 
 		    	 serve = defineServe(sectorName);
 		    	 serve.setProperty("nonrelevant1", Integer.valueOf(field[i++]));
 		    	 serve.setProperty("nonrelevant2", Integer.valueOf(field[i++]));
 		    	 serve.setProperty("total-cell-area", Double.valueOf(field[i++]));
 		    	 serve.setProperty("total-cell-traffic", Double.valueOf(field[i++]));
 		    	 serve.setProperty("numberofinterferers", Integer.valueOf(field[i++]));
 		     } 
 		     else if (name.equals("INT")) {    	 
 			     if (serve == null) {
 				     error("Not found serve cell for neighbours: " + line);
 				     return;
 			     } 
 			     else {
 			    	 String sectorName = field[7];
 			    	 if (!sectorName.substring(sectorName.length() - 1).matches("\\d")){
 			    		 int sectorNo = Character.getNumericValue(sectorName.charAt(sectorName.length() - 1)) - Character.getNumericValue('A') + 1;
 			    		 sectorName = sectorName.substring(0, sectorName.length() - 1) + sectorNo;
 			    	 }
 			    	 Relationship relation = defineInterferer(sectorName);
 			    	 relation.setProperty("nonrelevant1", Integer.valueOf(field[i++]));
 			    	 relation.setProperty("nonrelevant2", Integer.valueOf(field[i++]));
 			    	 relation.setProperty("co-channel-interf-area", Double.valueOf(field[i++]));
 			    	 relation.setProperty("co-channel-interf-traffic", Double.valueOf(field[i++]));
 			    	 relation.setProperty("adj-channel-interf-area", Double.valueOf(field[i++]));
 			    	 relation.setProperty("adj-channel-interf-traffic", Double.valueOf(field[i++]));
 			     }
 		     }
 	     } catch (Exception e) {
 		     String errStr = String.format("Can't parse line: %s", line);
 		     AweConsolePlugin.error(errStr);
 		     Logger.getLogger(this.getClass()).error(errStr, e);
 	     }
      }
     
      /**
       * Define neigh.
       * 
       * @param siteName the site name
       * @param field the field
       */
      private Relationship defineInterferer(String sectorName) {
         String proxySectorName = interferenceName + "/" + sectorName;
          
         Node proxySector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS), proxySectorName);
      	if (proxySector == null) {
      		Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
              if (sector == null) {
                  error(". Interference File. Not found sector " + sectorName);
                  return null;
              }
              proxySector = createProxySector(sector, lastSector, rootNode, NetworkRelationshipTypes.INTERFERENCE);
              lastSector = proxySector;
      	}
          
          Relationship relation = serve.createRelationshipTo(proxySector, NetworkRelationshipTypes.INTERFERS);
          return relation;
      }
 
      /**
       * Define serve.
       * 
       * @param siteName the site name
       * @param field the field
       * @return the node
       */
      private Node defineServe(String sectorName) {
          String proxySectorName = interferenceName + "/" + sectorName;
          
          Node proxySector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS), proxySectorName);
          	if (proxySector == null) {
          		Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
                  if (sector == null) {
                      error(". Interference File. Not found sector " + sectorName);
                      return null;
                  }
                  proxySector = createProxySector(sector, lastSector, rootNode, NetworkRelationshipTypes.INTERFERENCE);
                  lastSector = proxySector;
          	}           
        
          return proxySector;
      }
     
      }
 
     /**
      * <p>
      * NeighbourFileHandler handle import of Forbidden File
      * </p>
      * .
      * 
      * @author TsAr
      * @since 1.0.0
      */
     public class ExceptionFileHandler extends AbstractTxFileHandler {
 
         /** The serve. */
         private Node serve;
         private Set<String> numericProp;
         private Set<String> allProp;
         private String exceptionName;
         Node lastSector;
         
 
         /**
          * Instantiates a new cell file handler.
          * 
          * @param rootNode the root node
          * @param service the service
          */
         public ExceptionFileHandler(Node rootNode, GraphDatabaseService service) {
             super(rootNode, service);
             exceptionName = NeoUtils.getNodeName(rootNode, service);
         }
 
         /**
          * Inits the.
          */
         @Override
         public void init() {
             super.init();
             serve = null;
             numericProp = new HashSet<String>();
             allProp = new HashSet<String>();
         }
 
         @Override
         public void finish() {
             super.finish();
             rootNode.setProperty(INeoConstants.LIST_NUMERIC_PROPERTIES, numericProp.toArray(new String[0]));
             rootNode.setProperty(INeoConstants.LIST_NUMERIC_PROPERTIES, allProp.toArray(new String[0]));
         }
 
         /**
          * Store line.
          * 
          * @param line the line
          */
         @Override
         protected void storeLine(String line) {
             try {
                 String[] field = line.split("\\s");
                 int i = 0;
                 String siteName = field[i++];
                 Integer sectorNo = Integer.valueOf(field[i++]);
                 String sectorName = siteName +field[1];
                 serve = defineServe(sectorName);
                 siteName = field[i++];
                 sectorNo = Integer.valueOf(field[i++]);
                 sectorName = siteName +field[3];
                 Relationship relation = defineException(sectorName);
                 relation.setProperty("new_spacing", field[i++]);
             } catch (Exception e) {
                 String errStr = String.format("Can't parse line: %s", line);
                 AweConsolePlugin.error(errStr);
                 Logger.getLogger(this.getClass()).error(errStr, e);
             }
         }
 
 
 
         
         /**
          * Define Exception.
          * 
          * @param siteName the site name
          * @param field the field
          */
         private Relationship defineException(String sectorName) {
            String proxySectorName = exceptionName + "/" + sectorName;
             
            Node proxySector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS), proxySectorName);
         	if (proxySector == null) {
         		Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
                 if (sector == null) {
                     error(". Exception File. Not found sector " + sectorName);
                     return null;
                 }
                 proxySector = createProxySector(sector, lastSector, rootNode, NetworkRelationshipTypes.EXCEPTIONS);
                 lastSector = proxySector;
         	}
             
             Relationship relation = serve.createRelationshipTo(proxySector, NetworkRelationshipTypes.EXCEPTION);
 //            if (numericProp.isEmpty()) {
 //                numericProp.add("new_spacing");
 //                allProp.add("new_spacing");
 //            }
             return relation;
         }
 
 
 
         /**
          * Define serve.
          * 
          * @param siteName the site name
          * @param field the field
          * @return the node
          */
         private Node defineServe(String sectorName) {
             String proxySectorName = exceptionName + "/" + sectorName;
             
             Node proxySector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR_SECTOR_RELATIONS), proxySectorName);
             	if (proxySector == null) {
             		Node sector = luceneInd.getSingleNode(NeoUtils.getLuceneIndexKeyByProperty(afpCell, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.SECTOR), sectorName);
                     if (sector == null) {
                         error(". Exceptions File. Not found sector " + sectorName);
                         return null;
                     }
                     proxySector = createProxySector(sector, lastSector, rootNode, NetworkRelationshipTypes.EXCEPTIONS);
                     lastSector = proxySector;
             	}           
           
             return proxySector;
         }
 
     }
 
 }
