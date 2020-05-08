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
 
 package org.amanzi.awe.etsi.statistics;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.amanzi.neo.index.MultiPropertyIndex.MultiTimeIndexConverter;
 import org.amanzi.splash.swing.Cell;
 import org.amanzi.splash.utilities.SpreadsheetCreator;
 import org.eclipse.core.runtime.IPath;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * Class for calculating statistics on EADS data
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class ETSIStatistics extends SpreadsheetCreator {
 	
 	/**
 	 * A Periods for calculating Statistics
 	 * 
 	 * @author Lagutko_N
 	 * @since 1.0.0
 	 */
 	public enum StatisticsPeriod {
 		PER_HOUR(new SimpleDateFormat("yyyy-MM-dd HH-mm"), (long)1000 * 60 * 60, "per hour"),
 		PER_DAY(new SimpleDateFormat("yyyy-MM-dd"), (long)1000 * 60 * 60 * 24, "per day"),
 		PER_WEEK(new SimpleDateFormat("yyyy-MM-dd"), (long)1000 * 60 * 60 * 24 * 7, "per week"),
 		PER_MONTH(new SimpleDateFormat("yyyy MMMM"), (long)0, "per month") {
 			
 			/*
 			 * Period for Month is changeable (30, 31 days) so we should override some methods 
 			 */
 			
 			private Calendar calendar = Calendar.getInstance();
 			
 			@Override
 			public long getNextTime(long startTime) {
 				calendar.setTimeInMillis(startTime);
 				calendar.add(Calendar.MONTH, 1);
 				return calendar.getTimeInMillis();
 			}
 			
 			@Override
 			public long roundTime(long time) {
 				calendar.setTimeInMillis(time);
 				calendar.set(Calendar.DAY_OF_MONTH, 0);
 				calendar.set(Calendar.HOUR, 0);
 				calendar.set(Calendar.MINUTE, 0);
 				calendar.set(Calendar.SECOND, 0);
 				calendar.set(Calendar.MILLISECOND, 0);
 				return calendar.getTimeInMillis();
 			}
 			
 			@Override
 			public String getPeriodHeader(Date startDate, Date endDate) {
 				return PER_MONTH.format.format(endDate);
 			}
 		};
 		
 		/*
 		 * Format of date for this period
 		 */
 		private SimpleDateFormat format;
 		
 		/*
 		 * Period in milliseconds
 		 */
 		private long period;
 		
 		/*
 		 * Name of this period
 		 */
 		private String name;
 		
 		/**
 		 * Constructor 
 		 * 
 		 * @param format format of date
 		 * @param period period in milliseconds
 		 * @param name name of period
 		 */
 		private StatisticsPeriod(SimpleDateFormat format, long period, String name) {
 			this.format = format;
 			this.period = period;			
 			this.name = name;
 		}
 		
 		/**
 		 * Returns next time by this period
 		 *
 		 * @param startTime start time
 		 * @return next time
 		 */
 		public long getNextTime(long startTime) {
 			return startTime + period;
 		}
 		
 		/**
 		 * Rounds a time by this period
 		 *
 		 * @param time time to round
 		 * @return rounded time
 		 */
 		public long roundTime(long time) {
 			return (time / period) * period;
 		}
 		
 		/**
 		 * Returns name of Period
 		 *
 		 * @return name of Period
 		 */
 		public String getPeriodName() {
 			return name;
 		}
 		
 		/**
 		 * Searches for the period by it's name
 		 *
 		 * @param name name of period
 		 * @return StatisticsPeriod by name
 		 */
 		public static StatisticsPeriod getPeriodByName(String name) {
 			for (StatisticsPeriod singlePeriod : values()) {
 				if (singlePeriod.getPeriodName().equals(name)) {
 					return singlePeriod;
 				}
 			}
 			return null;
 		}
 		
 		/**
 		 * Returns a Header for start date and end date
 		 *
 		 * @param startDate start date
 		 * @param endDate end date
 		 * @return header
 		 */
 		public String getPeriodHeader(Date startDate, Date endDate) {
 			return format.format(startDate) + " - " + format.format(endDate);
 		}
 	}
 	
 	/*
 	 * Header of Call Terminated column
 	 */
 	private static final String CALL_TERMINATED = "call terminated";
 
 	/*
 	 * Header of Call Incoming column
 	 */
 	private static final String CALL_INCOMING = "call_incoming";
 
 	/*
 	 * Header of Call Accepted column
 	 */
 	private static final String CALL_ACCEPTED = "call accepted";
 
 	/*
 	 * Header of Call Terminate column
 	 */
 	private static final String CALL_TERMINATE = "call terminate";
 
 	/*
 	 * Header of Call Setup column
 	 */
 	private static final String CALL_SETUP = "call setup";
 	
 	/*
 	 * Header of Call Connected column
 	 */
 	private static final String CALL_CONNECTED = "call connected";
 	
 	/*
 	 * Index for timestamp
 	 */
 	private MultiPropertyIndex<Long> timestampIndex;
 	
 	/*
 	 * Headers of columns
 	 */
 	private ArrayList<String> columnHeaders = new ArrayList<String>();
 	
 	/**
 	 * Creates instance of this class
 	 * 
 	 * @param containerPath path to container that will contain new Spreadsheet
 	 * @param spreadsheetName name of new spreadsheet
 	 */
 	public ETSIStatistics(IPath containerPath, String spreadsheetName) {
 		super(containerPath, spreadsheetName);
 		
 		columnHeaders.add(CALL_SETUP);
 		columnHeaders.add(CALL_CONNECTED);
 		columnHeaders.add(CALL_TERMINATE);
 		columnHeaders.add(CALL_ACCEPTED);
 		columnHeaders.add(CALL_INCOMING);
 		columnHeaders.add(CALL_TERMINATED);
 	}
 	
 	/**
 	 * Calculates statistics for choosen dataset by choose period
 	 *
 	 * @param datasetName name of Dataset 
 	 * @param period period for calculating
 	 */
 	public void calculateStatistics(String datasetName, StatisticsPeriod period) {
 		Transaction tx = neoService.beginTx();
 		
 		try {
 			initializeIndex(datasetName);
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		try {
 			Pair<Long, Long> minMax = getMinMaxTimeOfDataset(datasetName);
 		
 			Long currentDate = minMax.getLeft();
 			Long maxDate = minMax.getRight();
 			
 			currentDate = period.roundTime(currentDate);
 			
 			int row = 1;
 			
 			addColumnHeaders();
 		
 			while (currentDate < maxDate) {
 				long endDate = period.getNextTime(currentDate);
 				HashMap<String, Integer> data = computeStatisticsByPeriod(currentDate, endDate);
 				
 				addPeriodToSheet(row, currentDate, endDate, period);
 				
 				int column = 1; 
 				for (String header : columnHeaders) {
 					Integer count = data.get(header);
 					
 					Cell cell = new Cell(row, column++, count.toString(), count.toString(), null);
 					saveCell(cell);
 				}
 				
 				currentDate = endDate;
 				row++;
 			}
 		}
		catch (Exception e) {
			e.printStackTrace();
		}
 		finally {
 			tx.success();
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Add header of Period to Spreadsheet
 	 * 
 	 * @param row row of Header cell
 	 * @param startDate start date
 	 * @param endDate end date
 	 * @param period period
 	 */
 	private void addPeriodToSheet(int row, long startDate, long endDate, StatisticsPeriod period) {
 		Date start = new Date(startDate);
 		Date end = new Date(endDate);
 		
 		String periodString = period.getPeriodHeader(start, end);
 		
 		Cell cell = new Cell(row, 0, periodString, periodString, null);
 		saveCell(cell);
 	}
 	
 	/**
 	 * Add Headers for columns
 	 */
 	private void addColumnHeaders() {
 		int row = 0;
 		
 		Cell periodCell = new Cell(0, 0, "Period", "Period", null);
 		saveCell(periodCell);
 		
 		int column = 1;
 		for (String header : columnHeaders) {
 			Cell headerCell = new Cell(row, column++, header, header, null);
 			saveCell(headerCell);
 		}
 	}
 	
 	/**
 	 * Returns next ms node
 	 *
 	 * @param msNode start ms node
 	 * @return next ms node
 	 */
 	private Node getNextMsNode(Node msNode) {
 		Iterator<Node> nodeIterator = msNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
 		
 		if (nodeIterator.hasNext()) {
 			return nodeIterator.next();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns first ms node of mp node
 	 *
 	 * @param mpNode mp node
 	 * @return first ms node 
 	 */
 	private Node getStartMsNode(Node mpNode) {
 		Iterator<Node> nodeIterator = mpNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 			
 			@Override
 			public boolean isReturnableNode(TraversalPosition currentPos) {
 				if (currentPos.notStartNode()) {
 					return !currentPos.currentNode().hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING);
 				}
 				
 				return false;
 			}
 		}, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
 		
 		if (nodeIterator.hasNext()) {
 			return nodeIterator.next();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns information about statistics by choosen period
 	 *
 	 * @param startDate start date of period
 	 * @param endDate end date of period
 	 * @return map that contains names of event and number of events
 	 */
 	private HashMap<String, Integer> computeStatisticsByPeriod(long startDate, long endDate) {
 		int callSetup = 0;
 		int callConnected = 0;
 		int callTerminate = 0;
 		int callAccepted = 0;
 		int callIncoming = 0;
 		int callTerminated = 0;
 		
 		Transaction tx = neoService.beginTx();
 		try {
 			for (Node mpNode : timestampIndex.find(new Long[] {startDate}, new Long[] {endDate})) {
 				Node firstMsNode = getStartMsNode(mpNode);
 				
 				if (firstMsNode == null) {
 					continue;
 				}
 				
 				String commandName = (String)firstMsNode.getProperty(INeoConstants.COMMAND_PROPERTY_NAME);
 				
 				if (commandName.equals("atd")) {
 					callSetup++;
 					
 					Node msNode = getNextMsNode(firstMsNode);
 					while (msNode != null) {
 						commandName = (String)msNode.getProperty(INeoConstants.COMMAND_PROPERTY_NAME);
 						
 						if (commandName.equals("CTOCP")) {
 							callConnected++;
 							break;
 						}
 						
 						msNode = getNextMsNode(msNode);
 					}
 				}
 				else if (commandName.equals("ATH")) {
 					Node msNode = getNextMsNode(firstMsNode);
 					while (msNode != null) {
 						commandName = (String)msNode.getProperty(INeoConstants.COMMAND_PROPERTY_NAME);
 						
 						if (commandName.equals("CTCR")) {
 							callTerminate++;
 							break;
 						}
 						
 						msNode = getNextMsNode(msNode);
 					}
 				}
 				else if (commandName.equals("ATA")) {
 					Node msNode = getNextMsNode(firstMsNode);
 					while (msNode != null) {
 						commandName = (String)msNode.getProperty(INeoConstants.COMMAND_PROPERTY_NAME);
 						
 						if (commandName.equals("CTCC")) {
 							callAccepted++;
 							break;
 						}
 						
 						msNode = getNextMsNode(msNode);
 					}
 				}
 				else {
 					Node msNode = firstMsNode;
 					do {
 						commandName = (String)msNode.getProperty(INeoConstants.COMMAND_PROPERTY_NAME);
 						if (commandName.equals("CTICN")) {
 							callIncoming++;
 						}
 						else if (commandName.equals("CTCR")) {
 							callTerminated++;
 						}
 						
 						msNode = getNextMsNode(msNode);
 					} while (msNode != null);
 				}
 			}
 		}
 		finally {
 			tx.finish();
 		}
 		
 		HashMap<String, Integer> result = getClearStatisticsMap();
 		result.put(CALL_SETUP, callSetup);
 		result.put(CALL_CONNECTED, callConnected);
 		result.put(CALL_TERMINATE, callTerminate);
 		result.put(CALL_ACCEPTED, callAccepted);
 		result.put(CALL_INCOMING, callIncoming);
 		result.put(CALL_TERMINATED, callTerminated);
 		
 		return result;
 	}
 	
 	/**
 	 * Returns a clear map for statistics
 	 *
 	 * @return clear map for statistics
 	 */
 	private HashMap<String, Integer> getClearStatisticsMap() {
 		HashMap<String, Integer> result = new HashMap<String, Integer>();
 		
 		result.put(CALL_SETUP, 0);
 		
 		return result;
 	}
 	
 	/**
 	 * Returns pair of min and max timestamps for this dataset
 	 *
 	 * @param datasetName name of dataset
 	 * @return pair of min and max timestamps
 	 */
 	private Pair<Long, Long> getMinMaxTimeOfDataset(String datasetName) {
 		Node gis = getGisNode(datasetName);
 		
 		Long min = null;
 		if (gis.hasProperty(INeoConstants.MIN_TIMESTAMP)) {
 			min = (Long)gis.getProperty(INeoConstants.MIN_TIMESTAMP);
 		}
 		
 		Long max = null;
 		if (gis.hasProperty(INeoConstants.MAX_TIMESTAMP)) {
 			max = (Long)gis.getProperty(INeoConstants.MAX_TIMESTAMP);
 		}
 		
 		return new Pair<Long, Long>(min, max);
 	}
 	
 	/**
 	 * Initializes timestamp index for dataset
 	 *
 	 * @param datasetName name of dataset
 	 * @throws IOException
 	 */
 	private void initializeIndex(String datasetName) throws IOException {
 		timestampIndex = new MultiPropertyIndex<Long>(INeoConstants.TIMESTAMP_INDEX_NAME + datasetName, new String[] {INeoConstants.PROPERTY_TIMESTAMP_NAME}, new MultiTimeIndexConverter(), 10);
 		timestampIndex.initialize(neoService, null);
 	}
 	
 	/**
 	 * Returns gis node by dataset name
 	 *
 	 * @param datasetName name of dataset
 	 * @return gis node
 	 */
 	public Node getGisNode(final String datasetName) {
 		Node root = neoService.getReferenceNode();
 		Iterator<Node> datasetIterator = root.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 
             @Override
             public boolean isStopNode(TraversalPosition currentPos) {
                 return currentPos.depth() > 1;
             }
         }, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
             	if (currentPos.depth() != 1) {
             		return false;
             	}
                 Node node = currentPos.currentNode();
                 boolean hasChild = node.hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 if (hasChild) {
                 	node = node.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getEndNode();
                 	
                 	return node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                     	   && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(INeoConstants.DATASET_TYPE_NAME) &&
                     	   node.hasProperty(INeoConstants.PROPERTY_NAME_NAME) &&
                     	   node.getProperty(INeoConstants.PROPERTY_NAME_NAME).equals(datasetName);
                 }
                 return false;                
             }
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
 		
 		if (datasetIterator.hasNext()) {
 			return datasetIterator.next();
 		}
 		return null;
 	}
 
 }
