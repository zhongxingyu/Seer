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
 
 package org.amanzi.neo.loader;
 
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.OssType;
 import org.amanzi.neo.core.enums.gpeh.Parameters;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.loader.gpeh.GPEHEvent;
 import org.amanzi.neo.loader.gpeh.GPEHMainFile;
 import org.amanzi.neo.loader.gpeh.GPEHParser;
 import org.amanzi.neo.loader.gpeh.GPEHEvent.Event;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 
 /**
  * <p>
  * GPHEHLoader
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class GPEHLoader extends AbstractLoader {
     /** int KEY_EVENT field */
     private static final int KEY_EVENT = 1;
     private final static Pattern mainFilePattern = Pattern.compile("(^.*)(_Mp0\\.)(.*$)");
     private static final int COUNT_LEN = 1000;
     private Node ossRoot;
     private Pair<Boolean, Node> mainNode;
     private final Map<Integer, Node> cellMap;
     private long timestampOfDay;
     private Node eventLastNode;
     private Node cellRoot;
     private Node lastCellNode;
     private final LinkedHashMap<String, Header> headers;
 
     /**
      * Constructor
      * @param directory
      * @param datasetName
      * @param display
      */
     public GPEHLoader(String directory, String datasetName, Display display) {
         initialize("GPEH", null, directory, display);
         basename = datasetName;
         headers = getHeaderMap(KEY_EVENT).headers;
         cellRoot = null;
         lastCellNode = null;
         cellMap=new HashMap<Integer,Node>();
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         return ossRoot;
     }
 
     @Override
     public void run(IProgressMonitor monitor) throws IOException {
         if (monitor != null)
             monitor.subTask(basename);
         cellMap.clear();
         addIndex(NodeTypes.GPEH_EVENT.getId(), NeoUtils.getTimeIndexProperty(basename));
 
         Map<String, List<String>> fileList = getGPEHFile(filename);
         mainTx = neo.beginTx();
         NeoUtils.addTransactionLog(mainTx, Thread.currentThread(), "GPEHLoader");
         try {
             initializeIndexes();
             ossRoot = LoaderUtils.findOrCreateOSSNode(OssType.GPEH, basename, neo);
             Pair<Node,Node> pair= LoaderUtils.findOrCreateGPEHCellRootNode(ossRoot, neo);
             cellRoot=pair.getLeft();
             lastCellNode=pair.getRight();
             if (lastCellNode!=null){
                 //TODO use index?
                for (Node cell:NeoUtils.getChildTraverser(cellRoot)){
                    cellMap.put((Integer)cell.getProperty(INeoConstants.PROPERTY_SECTOR_CI,null),cell); 
                }
             }
             int perc = 0;
             int count=0;
             for (Map.Entry<String, List<String>> entry : fileList.entrySet()) {
//                long len = new File(filename + File.separator + entry.getKey()).length();
                perc+= entry.getValue().size()+1;
            }
            monitor.beginTask("Load GPEH data", perc);
            for (Map.Entry<String, List<String>> entry : fileList.entrySet()) {
                 try {
                     String mainFile = entry.getKey();
                    monitor.setTaskName(mainFile);;
                     String rootFile = filename + File.separator + mainFile;
                     GPEHMainFile root = GPEHParser.parseMainFile(new File(rootFile));
                     saveRoot(root);
                    monitor.worked(1);
                     eventLastNode = null;
                     for (String subFile : entry.getValue()) {
                         monitor.setTaskName(subFile);
                         System.out.println(subFile);
                         GPEHEvent result = new GPEHEvent();
                         File file=new File(filename + File.separator + subFile);
                         InputStream in =new FileInputStream(file);
                         if (Pattern.matches("^.+\\.gz$",file.getName())){
                             in= new GZIPInputStream(in); 
                         }
                         DataInputStream input = new DataInputStream(in);
                             try {
                                 while (true) {
                                     int recordLen = input.readUnsignedShort()-3;
                                     int recordType = input.readByte();
                                     if (recordType == 4) {
                                         GPEHParser.parseEvent(input, result,recordLen);
                                     }else if (recordType == 7) {
                                         GPEHParser.pareseFooter(input, result);
                                     } else if (recordType == 6) {
                                         GPEHParser.pareseError(input, result);
                                     } else {
                                         // wrong file format!
                                         throw new IllegalArgumentException();
                                     }
                                     saveEvent(result);
                                     result.clearEvent();
                                     count++;
                                     if (count>COUNT_LEN){
                                         commit(true);
                                         count=0;
                                     }
                                 }
                             }catch (EOFException e) {
                                 //normal behavior
                             } finally {
                                 input.close();
                                monitor.worked(1);
                             }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                     // TODO add more information
                     NeoLoaderPlugin.error(e.getLocalizedMessage());
                 }
             }
             commit(true);
             saveProperties();
             finishUpIndexes();
             finishUp();
         } finally {
             commit(false);
         }
     }
 
 
     /**
      * save event subfile
      * @param eventFile - event file
      */
     private void saveEvent(GPEHEvent eventFile) {
         for (Event event : eventFile.getEvents()) {
             saveSingleEvent(event);
         }
     }
 
     /**
      * save event
      * @param event - event
      */
     private void saveSingleEvent(Event event) {
         Transaction tx = neo.beginTx();
         try {
             Node eventNode = neo.createNode();
             NodeTypes.GPEH_EVENT.setNodeType(eventNode, neo);
             setIndexProperty(headers, eventNode, INeoConstants.PROPERTY_NAME_NAME, event.getType().name());
             eventNode.setProperty(INeoConstants.PROPERTY_EVENT_ID, event.getType().getId());
             for (Map.Entry<Parameters, Object> entry : event.getProperties().entrySet()) {
                 setIndexProperty(headers, eventNode, entry.getKey().name(), entry.getValue());
             }
             Long timestamp = event.getFullTime(timestampOfDay);
             updateTimestampMinMax(KEY_EVENT, timestamp);
             eventNode.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
             NeoUtils.addChild(mainNode.getRight(), eventNode, eventLastNode, neo);
             tx.success();
             index(eventNode);
             eventLastNode = eventNode;
             createCells(event,eventNode);
         } finally {
             tx.finish();
         }
     }
 
     /**
      *Create Cells 
      *finds or create cells and links with event
      * @param event - event 
      * @param eventNode - event node
      */
     private void createCells(Event event, Node eventNode) {
         Transaction tx = neo.beginTx();
         try {
             LinkedHashSet<Integer> cellsId = event.getCellId();
             for (Integer cid : cellsId) {
                 Node cell = cellMap.get(cid);
                 if (cell == null) {
                     cell = neo.createNode();
                     NodeTypes.GPEH_CELL.setNodeType(cell, neo);
                     cell.setProperty(INeoConstants.PROPERTY_SECTOR_CI, cid);
                     cell.setProperty(INeoConstants.PROPERTY_NAME_NAME, cid.toString());
                     NeoUtils.addChild(cellRoot, cell, lastCellNode, neo);
                     lastCellNode = cell;
                     cellMap.put(cid, cell);
                 }
                 cell.createRelationshipTo(eventNode, GeoNeoRelationshipTypes.EVENTS);
             }
         } finally {
             tx.finish();
         }
     }
 
     /**
      * save main file to node
      * 
      * @param root - main file
      */
     private void saveRoot(GPEHMainFile root) {
         mainNode = NeoUtils.findOrCreateChildNode(neo, ossRoot, root.getName());
         GPEHMainFile.Header header = root.getHeader();
         GregorianCalendar cl = new GregorianCalendar(header.getYear(), header.getMonth(), header.getDay());
         timestampOfDay = cl.getTimeInMillis();
         if (mainNode.getLeft()) {
             Transaction tx = neo.beginTx();
             try {
                 Node node = mainNode.getRight();
                 node.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.OSS_MAIN.getId());
                 setProperty(node, INeoConstants.GPEH_FILE_VER, header.getFileVer());
                 setProperty(node, INeoConstants.GPEH_DAY, header.getDay());
                 setProperty(node, INeoConstants.GPEH_MONTH, header.getMonth());
                 setProperty(node, INeoConstants.GPEH_MINUTE, header.getMinute());
                 setProperty(node, INeoConstants.GPEH_SECOND, header.getSecond());
                 setProperty(node, INeoConstants.GPEH_YEAR, header.getYear());
                 setProperty(node, INeoConstants.GPEH_LOGIC_NAME, header.getNeLogicalName());
                 setProperty(node, INeoConstants.GPEH_USER_LABEL, header.getNeUserLabel());
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
     }
 
     /**
      * Gets map of gpeh files
      * 
      * @param filename - directory
      * @return Map<main file, List of subfiles>
      */
     private Map<String, List<String>> getGPEHFile(String filename) {
         File dir = new File(filename);
         Map<String, List<String>> result = new HashMap<String, List<String>>();
         String[] mainFileList = dir.list(new FilenameFilter() {
 
             @Override
             public boolean accept(File dir, String name) {
                 return mainFilePattern.matcher(name).matches();
             }
         });
         for (final String mainFile : mainFileList) {
             Matcher matcher = mainFilePattern.matcher(mainFile);
             matcher.find();
             final String mainPart = matcher.group(1);
             List<String> subFiles = new LinkedList<String>();
             for (String file : dir.list(new FilenameFilter() {
                 @Override
                 public boolean accept(File dir, String name) {
                     return name.startsWith(mainPart) && !name.equals(mainFile);
                 };
             })) {
                 subFiles.add(file);
             }
             result.put(mainFile, subFiles);
         }
         return result;
     }
 
     @Override
     protected boolean needParceHeaders() {
         return false;
     }
 
     @Override
     protected void parseLine(String line) {
     }
     @Override
     protected void finishUp() {
         super.finishUp();
         for (Map.Entry<Integer, Pair<Long, Long>> entry : timeStamp.entrySet()) {
             Node storeNode = getStoringNode(entry.getKey());
             if (storeNode != null) {
                 Long minTimeStamp = entry.getValue().getLeft();
                 if (minTimeStamp != null) {
                     storeNode.setProperty(INeoConstants.MIN_TIMESTAMP, minTimeStamp);
                 }
                 Long maxTimeStamp = entry.getValue().getRight();
                 if (maxTimeStamp != null) {
                     storeNode.setProperty(INeoConstants.MAX_TIMESTAMP, maxTimeStamp);
                 }
             }
         }
 
         super.cleanupGisNode();//(datasetNode == null ? file : datasetNode);
     }
 
 
 }
