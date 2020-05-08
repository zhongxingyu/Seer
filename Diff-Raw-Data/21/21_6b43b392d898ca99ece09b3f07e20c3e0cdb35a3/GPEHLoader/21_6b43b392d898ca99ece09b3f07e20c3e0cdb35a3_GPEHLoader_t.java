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
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import org.amanzi.awe.l3messages.MessageDecoder;
 import org.amanzi.awe.l3messages.rrc.CellMeasuredResults;
 import org.amanzi.awe.l3messages.rrc.GSM_MeasuredResults;
 import org.amanzi.awe.l3messages.rrc.InterFreqMeasuredResults;
 import org.amanzi.awe.l3messages.rrc.InterFreqMeasuredResultsList;
 import org.amanzi.awe.l3messages.rrc.InterRATMeasuredResults;
 import org.amanzi.awe.l3messages.rrc.InterRATMeasuredResultsList;
 import org.amanzi.awe.l3messages.rrc.IntraFreqMeasuredResultsList;
 import org.amanzi.awe.l3messages.rrc.MeasuredResults;
 import org.amanzi.awe.l3messages.rrc.MeasurementReport;
 import org.amanzi.awe.l3messages.rrc.UE_InternalMeasuredResults;
 import org.amanzi.awe.l3messages.rrc.UL_DCCH_Message;
 import org.amanzi.awe.l3messages.rrc.UL_DCCH_MessageType;
 import org.amanzi.awe.l3messages.rrc.CellMeasuredResults.ModeSpecificInfoChoiceType.FddSequenceType;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.SectorIdentificationType;
 import org.amanzi.neo.core.enums.gpeh.Events;
 import org.amanzi.neo.core.enums.gpeh.Parameters;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.GpehReportUtil;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.gpeh.GPEHEvent;
 import org.amanzi.neo.loader.gpeh.GPEHMainFile;
 import org.amanzi.neo.loader.gpeh.GPEHParser;
 import org.amanzi.neo.loader.gpeh.GPEHEvent.Event;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.kc7bfi.jflac.io.BitInputStream;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 
 /**
  * <p>
  * GPHEHLoader
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class GPEHLoader extends DriveLoader {
     private static final Logger LOGGER = Logger.getLogger(GPEHLoader.class);
     
     /**
      * Window for Timestamp on correlation locations for events
      */
     private static final long TIMESTAMP_WINDOW = 1000;
     
     private static final double LATITUDE_FACTOR = Math.pow(2, 23) / 90;
     
     private static final double LONGITUDE_FACTOR = Math.pow(2, 24) / 360;
 	
 	/** int KEY_EVENT field */
     private static final int KEY_EVENT = 1;
     private final static Pattern mainFilePattern = Pattern.compile("(^.*)(_Mp0\\.)(.*$)");
     private static final int COUNT_LEN = 1000;
     private long timestampOfDay;
     private Node eventLastNode;
     private final LinkedHashMap<String, Header> headers;
     private int eventsCount;
     
     private final HashMap<String, Object> locationProperties = new HashMap<String, Object>();
     private Node mpNode = null;
     private long previousGoodTimestamp;
     
     private Node previousFileNode;
     
     /*
      * Index of RSCP Property in Event
      */
     private int rscpIndex;
     
     private final LuceneIndexService luceneInd;
     private final String luceneIndexName;
     private final String eventIndName;
     
     /**
      * Constructor
      * @param directory
      * @param datasetName
      * @param display
      */
     public GPEHLoader(String directory, String datasetName, Display display) {
         initialize("GPEH", null, directory, display, datasetName);
         driveType = DriveTypes.OSS;
         basename = datasetName;
         headers = getHeaderMap(KEY_EVENT).headers;
         luceneInd = NeoServiceProvider.getProvider().getIndexService();
         eventIndName =NeoUtils.getLuceneIndexKeyByProperty(basename, INeoConstants.PROPERTY_NAME_NAME, NodeTypes.GPEH_EVENT);
         luceneIndexName = NeoUtils.getLuceneIndexKeyByProperty(basename, INeoConstants.SECTOR_ID_PROPERTIES, NodeTypes.M);
         gisType = GisTypes.OSS;
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         return datasetNode;
     }
 
     @Override
     protected String getPrymaryType(Integer key) {
         return NodeTypes.M.getId();
     }
 
     @Override
     public void run(IProgressMonitor monitor) throws IOException {
         if (monitor != null)
             monitor.subTask(basename);
         addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(basename));
         addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(basename));
 
         ArrayList<File> fileList = getGPEHFiles(filename);
         mainTx = neo.beginTx();
         NeoUtils.addTransactionLog(mainTx, Thread.currentThread(), "GPEHLoader");
         try {
             initializeIndexes();
             
             datasetNode = findOrCreateDatasetNode(neo.getReferenceNode(), basename);
             eventsCount=0;
             int count=0;
             monitor.beginTask("Load GPEH data", fileList.size());            
             for (File gpehFile : fileList) {
                 try {
                     monitor.setTaskName(gpehFile.getName());
                     
                     this.filename = gpehFile.getName();
                     this.basename = this.filename;
                     this.file = null;
                     
                     if (mainFilePattern.matcher(gpehFile.getName()).matches()) {
                         GPEHMainFile root = GPEHParser.parseMainFile(gpehFile);
                         saveRoot(root);
                         monitor.worked(1);
                         continue;
                     }
                     eventLastNode = null;
                     long time;
                     
                     int cn=0;
                     long timeAll=System.currentTimeMillis();
                     long saveTime=0;
                     long parseTime=0;
                     
                     LOGGER.debug(gpehFile.getName());
                     
                     GPEHEvent result = new GPEHEvent();
                     
                     InputStream in =new FileInputStream(gpehFile);
                     if (Pattern.matches("^.+\\.gz$",gpehFile.getName())){
                         in = new GZIPInputStream(in); 
                     }
                     
                     BitInputStream input = new BitInputStream(in);
                     try {
                         while (true) {
                             time=System.currentTimeMillis();
                             int recordLen = input.readRawUInt(16)-3;
                             int recordType = input.readRawUInt(8);
                             if (recordType == 4) {
                                 GPEHParser.parseEvent(input, result,recordLen);
                                 eventsCount++;
                                 cn++;
                             }else if (recordType == 7) {
                                 GPEHParser.pareseFooter(input, result);
                             } else if (recordType == 6) {
                                 GPEHParser.pareseError(input, result);
                             } else {
                                 // wrong file format!
                                 throw new IllegalArgumentException();
                             }
                             parseTime+=System.currentTimeMillis()-time;
                             time=System.currentTimeMillis();
                             saveEvent(result);
                             saveTime+=System.currentTimeMillis()-time;
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
                         in.close();
                         monitor.worked(1);
                     }
                     timeAll=System.currentTimeMillis()-timeAll;
                     info(String.format("File %s: saved %s events", gpehFile.getName(), cn));                    
                     info(String.format("\ttotal time\t\t%s\n\t\tparce time\t%s\n\t\tsave time\t%s",timeAll,parseTime,saveTime));   
                 } catch (Exception e) {
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
         
         if (!isTest()) {
             finishUpGis();
         }
         addLayersToMap();
     }
 
 @Override
 public void printStats(boolean verbose) {
     info("Finished loading " + eventsCount + " events");
 }
     /**
      * save event subfile
      * @param eventFile - event file
      */
     private void saveEvent(GPEHEvent eventFile) {
         for (Event event : eventFile.getEvents()) {
             //TODO: Lagutko: fake - save only RRC Measurement Reports!!!
             if (event.getId() == 429) {
                 saveSingleEvent(event);
                 if (event.getId() == Events.INTERNAL_UE_POSITIONING_QOS.getId()) {
                     saveLocation(event);
                 }
             
                 if (event.getFullTime(timestampOfDay) < previousGoodTimestamp) {
                     createMPRelation(eventLastNode);
                 }
                 else {
                     previousGoodTimestamp = 0;
                     mpNode = null;
                 }            
             }
         }
     }
     
     private double convertLatitude(long latitude) {
     	return latitude / LATITUDE_FACTOR; 
     }
     
     private double convertLongitude(long longitude) {
     	return longitude / LONGITUDE_FACTOR;
     }
     
     private void saveLocation(Event event) {
     	long timestamp = event.getFullTime(timestampOfDay);
     	final long minWindow = timestamp - TIMESTAMP_WINDOW;
     	previousGoodTimestamp = timestamp + TIMESTAMP_WINDOW;
     	mpNode = null;
     	
     	locationProperties.clear();
     	locationProperties.put(INeoConstants.PROPERTY_LAT_NAME, convertLatitude((Long)event.getProperties().get(Parameters.EVENT_PARAM_LATITUDE)));
     	locationProperties.put(INeoConstants.PROPERTY_LON_NAME, convertLongitude((Long)event.getProperties().get(Parameters.EVENT_PARAM_LONGITUDE)));
     	locationProperties.put(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
     	
     	Transaction tx = neo.beginTx();
     	try {
     		Iterator<Node> previousNodes = eventLastNode.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 				
 				@Override
 				public boolean isStopNode(TraversalPosition currentPos) {
 					long timestamp = (Long)currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME);
 					return timestamp < minWindow;
 				}
 			}, ReturnableEvaluator.ALL, GeoNeoRelationshipTypes.NEXT, Direction.INCOMING).iterator();
     		
     		while (previousNodes.hasNext()) {
     			Node current = previousNodes.next();
     			
     			locationProperties.put(INeoConstants.PROPERTY_NAME_NAME, current.getProperty(INeoConstants.PROPERTY_EVENT_ID));
     			
     			createMPRelation(current);
     		}
     	}
     	catch (Exception e) {
     		LOGGER.error(e);
     		NeoLoaderPlugin.error(e.getMessage());
     	}
     	finally {
     		tx.success();
     		tx.finish();
     	}
     }
     
     private void createMPRelation(Node eventNode) {
     	Transaction tx = neo.beginTx();
     	try {
     		if (eventNode.getSingleRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING) == null) {
     			if (mpNode == null) {
     				mpNode = neo.createNode();
     				mpNode.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
     				for (String key : locationProperties.keySet()) {
     					mpNode.setProperty(key, locationProperties.get(key));
     				}
     				index(mpNode);
     				
     				GisProperties gisProperties = getGisProperties(dataset);
                     gisProperties.updateBBox((Double)locationProperties.get(INeoConstants.PROPERTY_LAT_NAME), (Double)locationProperties.get(INeoConstants.PROPERTY_LON_NAME));
                     gisProperties.checkCRS(((Double)locationProperties.get(INeoConstants.PROPERTY_LAT_NAME)).floatValue(), ((Double)locationProperties.get(INeoConstants.PROPERTY_LON_NAME)).floatValue(), null);
     			}
     			eventNode.createRelationshipTo(mpNode, GeoNeoRelationshipTypes.LOCATION);    			
     		}
     	}
     	catch (Exception e) {
     		LOGGER.error(e);
     		NeoLoaderPlugin.error(e.getMessage());
     	}
     	finally {
     		tx.success();
     		tx.finish();
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
             
             NodeTypes.M.setNodeType(eventNode, neo);
             String name = event.getType().name();
             setIndexProperty(headers, eventNode, INeoConstants.PROPERTY_NAME_NAME, name);
             eventNode.setProperty(INeoConstants.PROPERTY_EVENT_ID, event.getType().getId());
             for (Map.Entry<Parameters, Object> entry : event.getProperties().entrySet()) {
             	if (entry.getKey() == Parameters.EVENT_PARAM_C_ID_1) {
                 	luceneInd.index(eventNode, luceneIndexName, entry.getValue().toString());
                 }
                 //LN, 17.04.2010, if we parse a RRC Measurement Report that we should Decode Message
                 if (entry.getKey() == Parameters.EVENT_PARAM_MESSAGE_CONTENTS) {
                     if (event.getId() == Events.RRC_MEASUREMENT_REPORT.getId()) {
                         byte[] messageContent = (byte[])entry.getValue();
                         UL_DCCH_Message message = MessageDecoder.getInstance().parseRRCMeasurementReport(messageContent);
                         saveMessageInfo(message, eventNode);
                     }
                 }                
                 else {
                     setIndexProperty(headers, eventNode, entry.getKey().name(), entry.getValue());
                 }
             }
             Long timestamp = event.getFullTime(timestampOfDay);
             updateTimestampMinMax(KEY_EVENT, timestamp);
             eventNode.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
             
             if (eventLastNode != null) {
                 NeoUtils.addChild(file, eventNode, eventLastNode, neo);
             }
             else {
                 findOrCreateFileNode(eventNode);
             }
             luceneInd.index(eventNode, eventIndName, name);
             tx.success();
             index(eventNode);
             eventLastNode = eventNode;
            storingProperties.get(KEY_EVENT).incSaved();
            getGisProperties(dataset).incSaved();            
         } finally {
             tx.finish();
         }
     }
     
     int number = 0;
     
     /**
      * Save parsed result of RRC Measurement Report 
      * 
      * @param message parsed RRC Measurement Report 
      * @param eventNode node of Event
      */
     private void saveMessageInfo(UL_DCCH_Message message, Node eventNode) {
     	if (message == null) {
     		//message is null than stop
     		return;
     	}
     	
     	number++;
     	
     	UL_DCCH_MessageType messageType = message.getMessage();
     	
     	if (messageType == null) {
     		//if message type is null than stop
     		return;
     	}
     	
     	//initialize indexes of fields
     	rscpIndex = 0;
     	
     	if (messageType.isMeasurementReportSelected()) {
     		//get a MeasurementReport
     		MeasurementReport report = messageType.getMeasurementReport();
     		
     		if (report.isMeasuredResultsPresent()) {
     			//get MeasuredResults
     			MeasuredResults result = report.getMeasuredResults();
     			
     			if (result.isInterFreqMeasuredResultsListSelected()) {
     			    eventNode.setProperty(GpehReportUtil.MR_TYPE, GpehReportUtil.MR_TYPE_INTERF);
     				//process InterFreq Results
     				InterFreqMeasuredResultsList interFreqResultList = result.getInterFreqMeasuredResultsList();
     				
     				for (InterFreqMeasuredResults singleInterFreqResult : interFreqResultList.getValue()) {
     					if (singleInterFreqResult.isInterFreqCellMeasuredResultsListPresent()) {
     						for (CellMeasuredResults singleCellMeasuredResult : singleInterFreqResult.getInterFreqCellMeasuredResultsList().getValue()) {
     						    rscpIndex++;
     						    saveCellMeasuredResults(singleCellMeasuredResult, eventNode);
     						}
     					}
     				}
     			}
     			else if (result.isInterRATMeasuredResultsListSelected()) {
     			    eventNode.setProperty(GpehReportUtil.MR_TYPE, GpehReportUtil.MR_TYPE_IRAT);
     				//process InterRAT Results
     				InterRATMeasuredResultsList interRATResultList = result.getInterRATMeasuredResultsList();
     				for (InterRATMeasuredResults singleInterRatResults : interRATResultList.getValue()) {
     					if (singleInterRatResults.isGsmSelected()) {
     						for (GSM_MeasuredResults singleGSMResults : singleInterRatResults.getGsm().getValue()) {
     							if (singleGSMResults.getBsicReported().isVerifiedBSICSelected()) {
     							    rscpIndex++;
     								eventNode.setProperty(GpehReportUtil.GPEH_RRC_MR_BSIC_PREFIX + rscpIndex, singleGSMResults.getBsicReported().getVerifiedBSIC());
     							}
     						}
     					}
     				}
     			}
     			else if (result.isIntraFreqMeasuredResultsListSelected()) {
     			    eventNode.setProperty(GpehReportUtil.MR_TYPE, GpehReportUtil.MR_TYPE_INTRAF);
     				//process IntraFreq Results
     				IntraFreqMeasuredResultsList intraFreqResultList = result.getIntraFreqMeasuredResultsList();
     				for (CellMeasuredResults singleCellMeasuredResults : intraFreqResultList.getValue()) {
     				    rscpIndex++;
     				    saveCellMeasuredResults(singleCellMeasuredResults, eventNode);
     				}
     			}
     			else if (result.isUe_InternalMeasuredResultsSelected()) {
     			    eventNode.setProperty(GpehReportUtil.MR_TYPE, GpehReportUtil.MR_TYPE_UE_INTERNAL);
     				//process UE Internal Results
     				UE_InternalMeasuredResults ueInternalResults = result.getUe_InternalMeasuredResults();
     				if (ueInternalResults.getModeSpecificInfo().isFddSelected()) {
     					eventNode.setProperty(GpehReportUtil.GPEH_RRC_MR_UE_TX_POWER_PREFIX, ueInternalResults.getModeSpecificInfo().getFdd().getUe_TransmittedPowerFDD().getValue());
     				}
     			}
     			
     		}
     	}
     }
     
     @Override
     protected void findOrCreateFileNode(Node firstChild) {
         boolean update = file == null;
         super.findOrCreateFileNode(firstChild);
         
         if (update) {
             if (previousFileNode != null) {
                 previousFileNode.createRelationshipTo(file, GeoNeoRelationshipTypes.NEXT);                
             }            
         }
         previousFileNode = file;
         
     }
     
     /**
      * Saves CellMeasuredResults to Node
      * 
      * @param results Cell Measured Results to save
      * @param eventNode node of Event
      */
     private void saveCellMeasuredResults(CellMeasuredResults results, Node eventNode) {
     	if (results.getModeSpecificInfo().isFddSelected()) {
     		FddSequenceType fdd = results.getModeSpecificInfo().getFdd();
     		Integer scramblingCode = fdd.getPrimaryCPICH_Info().getPrimaryScramblingCode().getValue();
     		//store scramblingCode like string
     		eventNode.setProperty(GpehReportUtil.GPEH_RRC_SCRAMBLING_PREFIX + rscpIndex, scramblingCode.toString());               
     		if (fdd.isCpich_RSCPPresent()) {
     			Integer value = fdd.getCpich_RSCP().getValue();
     			
                 eventNode.setProperty(GpehReportUtil.GPEH_RRC_MR_RSCP_PREFIX + rscpIndex, value);    			
     		}
     		if (fdd.isCpich_Ec_N0Present()) {
     			Integer value = fdd.getCpich_Ec_N0().getValue();
                 eventNode.setProperty(GpehReportUtil.GPEH_RRC_MR_ECNO_PREFIX + rscpIndex, value);
     		}
     	}
     }
     
     /**
      * save main file to node
      * 
      * @param root - main file
      */
     private void saveRoot(GPEHMainFile root) {
         GPEHMainFile.Header header = root.getHeader();
         GregorianCalendar cl = new GregorianCalendar(header.getYear(), header.getMonth(), header.getDay());
         timestampOfDay = cl.getTimeInMillis();
         Transaction tx = neo.beginTx();
         try {
             Node node = neo.createNode();
                 
             findOrCreateFileNode(node);
                 
             node.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.OSS_MAIN.getId());
             node.setProperty(INeoConstants.PROPERTY_NAME_NAME, NodeTypes.OSS_MAIN.getId());
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
 
     /**
      * Gets map of gpeh files
      * 
      * @param filename - directory
      * @return Map<main file, List of subfiles>
      */
     private ArrayList<File> getGPEHFiles(String filename) {
         return getAllLogFilePathes(filename, null);
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
         getStoringNode(1).setProperty(INeoConstants.SECTOR_ID_TYPE, SectorIdentificationType.CI.toString());
         
         super.finishUp();
 
 //        super.cleanupGisNode();//(datasetNode == null ? file : datasetNode);
         if (!isTest()) {
             sendUpdateEvent(UpdateViewEventType.OSS);
         }
     }
 
 
 }
