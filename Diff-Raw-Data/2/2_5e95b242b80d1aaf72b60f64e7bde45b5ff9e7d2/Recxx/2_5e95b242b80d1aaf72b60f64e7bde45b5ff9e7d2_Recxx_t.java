 package org.recxx;
 
 import static java.lang.String.format;
 import static java.lang.String.valueOf;
 import static org.recxx.utils.ReconciliationMode.OW;
 import static org.recxx.utils.ReconciliationMode.TW;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import org.recxx.exception.PropertiesFileException;
 import org.recxx.facades.DatabaseFacadeWorker;
 import org.recxx.facades.FileFacadeWorker;
 import org.recxx.facades.RecxxWorker;
 import org.recxx.utils.ArrayUtils;
 import org.recxx.utils.CONSTANTS;
 import org.recxx.utils.CloseableUtils;
 import org.recxx.utils.ReconciliationMode;
 import org.recxx.utils.SuperProperties;
 import org.recxx.writer.BufferedWriterManager;
 import org.recxx.writer.CSVLogger;
 
 /**
  * Generic SQL based reconciliation tool to allow comparison between data sources which support the JDBC/SQL protocols,
  * as well as files and/or from a database and additionally, from delimited files. This tool currently only supports
  * comparison between 2 data sets,
  * <p/>
  * Properties for the 2 data sources are specified, and then the data sources are loaded in separate threads and placed
  * into keyed HashMaps to allow comparison. When both data sets are loaded, the reconciliation process then takes place.
  * The sets of sql specified for each data source must have the columns for comparison in the same order, even if their
  * names are different - every column not specified in the 'key' (see below) is compared. An aggregation property can
  * also be specified, to aggregate numerical columns in the data with the same key. This is of most use for aggregating
  * File data, as 'GROUP BY' can be used in the database sql instead.
  * <p/>
  * If all columns in the 2 rows match each other, then that row is considered as matched. If any column between the 2
  * rows doesn't match, then the whole row is considered as not matched.
  * <p/>
  * Differences can be logged in 2 different ways. Either simply log the difference to System.err, however this can
  * degrade performance for large volumes of data. An alternative is to turn on CSV logging (see below) which produces a
  * comma delimited file of the differences.
  * <p/>
  * <p/>
  * The properties that are needed to use this class are listed below:
  * <p/>
  * Data source independent properties
  * <p/>
  * <ul>
  * <li>*.rec.toleranceLevel = numeric value, as a percentage, which sets the absolute difference to be allowed when
  * comparing numeric values, and still allow that column to be classed as matched</li>
  * <li>*.rec.smallestAbsoluteValue = numeric value. If <b>both</b> abs(values) are smaller than this, then they are
  * classed as being compared successfully</li>
  * <li>*.rec.handleNullsAsDefault = if true, numeric columns which have null values, are defaulted to 0.0</li>
  * <li>*.rec.outputType = defaults to 'csv', which allows logging to csv file. Else, if set to 'err' logs to System.err</li>
  * <li>*.rec.logger.csv.file = if outputType set to 'csv', this property needs to be set, to specify the location of the
  * csv file</li>
  * </ul>
  * <p/>
  * Database properties
  * <p/>
  * <ul>
  * <li>*.rec.inputSource<i>n</i>.db.uid = Database user id</li>
  * <li>*.rec.inputSource<i>n</i>.db.pwd = Database password</li>
  * <li>*.rec.inputSource<i>n</i>.db.jdbc.url = Database JDBC url</li>
  * <li>*.rec.inputSource<i>n</i>.db.jdbc.driver = JDBC driver to use to connect to the database</li>
  * <li>*.rec.inputSource<i>n</i>.db.sql = SQL to run on the database</li>
  * <li>*.rec.inputSource<i>n</i>.db.key = Unique key for data</li>
  * </ul>
  * <p/>
  * File properties
  * <p/>
  * <ul>
  * <li>*.rec.inputSource<i>n</i>.file.filePath = location of the file to load</li>
  * <li>*.rec.inputSource<i>n</i>.file.delimiter = delimiter delimiting the columns</li>
  * <li>*.rec.inputSource<i>n</i>.file.firstRowColumns = is the first row of the file column headings?</li>
  * <li>*.rec.inputSource<i>n</i>.file.Columns = if firstRowColumns=false, then this must be added with all the column
  * names</li>
  * <li>*.rec.inputSource<i>n</i>.file.columnDataTypes = java data types for <i>all</i> the columns (java.lang.String,
  * java.lang.Double etc)</li>
  * <li>*.rec.inputSource<i>n</i>.file.columnDataTypes.date.format= Pattern to allow any date strings to be converted to
  * java.util.Date objects...ie yyyyMMdd</li>
  * <li>*.rec.inputSource<i>n</i>.file.key = Unique key for data</li>
  * <li>*.rec.inputSource<i>n</i>.file.columnsToCompare = In place of sql, the columns that are to be reconciled</li>
  * <li>*.rec.inputSource<i>n</i>.file.aggregate = if true, aggregates data rows with the same key, for the compare
  * columns</li>
  * </ul>
  */
 public class Recxx extends AbstractRecFeed implements Runnable {
 
 	private static final String m_appName = "rec";
 	public static final String DB_INPUT = "DB";
 	public static final String FILE_INPUT = "File";
 	public static final String COLUMNS = "Columns";
 	public static final String DATA = "Data";
 	public static final String PROPERTIES = "Props";
 
 	private String FILE_LOCATION;
 	private String FILE_DELIMITER;
 
 	// default reconciliation process it Two-Way (TW) as opposed to One-Way (OW)
 	private String reconciliationMode = "TW";
 	protected String m_delimiter = " ";
 
 	private String m_outputType = "";
 	private boolean m_loggerInit = false;
 	private CSVLogger m_logger;
 	private final DecimalFormat m_dPercentageFormatter = new DecimalFormat("#.00%");
 	public static DecimalFormat m_dpFormatter;
 
 	protected HashMap m_propertiesMap;
 	protected HashMap m_dataToCompare = new HashMap();
 	protected int m_dataToCompareKey = 0;
 	protected ThreadGroup m_workerGroup = new ThreadGroup("Worker Group");
 
 	Logger LOGGER = Logger.getLogger(Recxx.class.getName());
 
 	/**
 	 * Constructor for Rec2Inputs.
 	 * 
 	 * @param args
 	 *            arguments to the program ( properties stub and the path to the filename )
 	 */
 	public Recxx(String[] args) {
 		super();
 		LOGGER.info("running with " + Arrays.toString(args));
 		init(args[0], args[1]);
 	}
 
 	/**
 	 * Normal main method to start up the class from a command line
 	 * 
 	 * @param args
 	 *            main args
 	 * @throws Exception
 	 *             if there is a problem
 	 */
 	public static void main(String[] args) throws Exception {
 		if (args.length != 2) {
 			throw new Exception(format(
 			        "Usage: %s <prefix> <properties file> Example: %s\\properties\\system.properties",
 			        Recxx.class.getName(), Recxx.class.getName()));
 		}
 
 		Recxx rec = new Recxx(args);
 		Thread t = new Thread(rec);
 		t.start();
 	}
 
 	/**
 	 * run the class
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 
 		try {
 			// firstly load up the properties....
 			loadProperties();
 
 			// then load the data sources in separate threads....
 			startThreads();
 
 			// now wait for the threads to finish
 			waitForThreads();
 
 			// now rec the data calling the correct method according to the mode
 			if (reconciliationMode.equalsIgnoreCase("TW"))
 				recData();
 			else
 				oldRecData();
 
 			// tidy up any connections etc
 			close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 
 	}
 
 	/**
 	 * close the csv logger, if open.
 	 * 
 	 * @throws java.io.IOException
 	 *             if the file can't be closed
 	 */
 	private void close() throws IOException {
 		if (m_outputType.equals("csv") && m_logger != null)
 			m_logger.close();
 	}
 
 	/**
 	 * Method recData.
 	 * 
 	 * @throws Exception
 	 *             if an unequal number of columns is passed.
 	 */
 	private void recData() throws Exception {
 
 		String[] inputColumns1, inputColumns2;
 
 		HashMap inputData1, inputData2;
 
 		int inputData1Size, inputData2Size;
 
 		SuperProperties inputProperties1, inputProperties2;
 
 		String input1Alias, input2Alias;
 
 		int input1MatchedRows = 0;
 		float tolerancePercentage, smallestAbsoluteValue;
 
 		LOGGER.info("Starting to reconcile data sources...");
 
 		if (m_dataToCompare.size() >= 2) {
 			inputColumns1 = (String[]) ((HashMap) m_dataToCompare.get("1")).get(COLUMNS);
 			inputData1 = (HashMap) ((HashMap) m_dataToCompare.get("1")).get(DATA);
 			inputProperties1 = (SuperProperties) ((HashMap) m_dataToCompare.get("1")).get(PROPERTIES);
 			inputData1Size = inputData1.size();
 
 			inputColumns2 = (String[]) ((HashMap) m_dataToCompare.get("2")).get(COLUMNS);
 			inputData2 = (HashMap) ((HashMap) m_dataToCompare.get("2")).get(DATA);
 			inputProperties2 = (SuperProperties) ((HashMap) m_dataToCompare.get("2")).get(PROPERTIES);
 			inputData2Size = inputData2.size();
 
 			// need a position of the compare columns in the array - do this by
 			// making every column which isn't a
 			// key column, a compare column
 			int[] input1CompareColumnPosition =
 			        ArrayUtils.getCompareColumnsPosition(inputColumns1,
 			                ArrayUtils.convertStringKeyToArray((String) inputProperties1.get("key"), m_delimiter));
 			int[] input2CompareColumnPosition =
 			        ArrayUtils.getCompareColumnsPosition(inputColumns2,
 			                ArrayUtils.convertStringKeyToArray((String) inputProperties2.get("key"), m_delimiter));
 
 			input1Alias = (String) inputProperties1.get("alias");
 			input2Alias = (String) inputProperties2.get("alias");
 
 			if (input1CompareColumnPosition.length != input2CompareColumnPosition.length)
 				throw new Exception("Unequal number of columns to compare - " + input1CompareColumnPosition.length
 				        + " vs " + input2CompareColumnPosition.length);
 
 			// now set the tolerance level as a percentage
 			tolerancePercentage = Float.parseFloat(((String) inputProperties1.get("tolerance")));
 			smallestAbsoluteValue = Float.parseFloat(((String) inputProperties1.get("smallestAbsoluteValue")));
 
 			Iterator inputIterator = inputData1.keySet().iterator();
 
 			LOGGER.info("Comparing " + decimalFormatter.format(inputData1.size()) + " rows from " + input1Alias
 			        + " with " + decimalFormatter.format(inputData2.size()) + " rows from " + input2Alias + " over "
 			        + input1CompareColumnPosition.length + " column(s)");
 
 			while (inputIterator.hasNext()) {
 				String key = (String) inputIterator.next();
 
 				boolean matchedRow = true;
 				boolean unhandledRow = false;
 
 				if (inputData2.containsKey(key)) {
 					// loop round the input1 columns to compare - a row is only
 					// deemed as matched
 					// if _all_ the columns selected to compare, match..
 					for (int i = 0; i < input1CompareColumnPosition.length; i++) {
 						Object o1 = ((ArrayList) inputData1.get(key)).get(input1CompareColumnPosition[i]);
 						Object o2 = ((ArrayList) inputData2.get(key)).get(input2CompareColumnPosition[i]);
 
 						if (o1 instanceof Double && o2 instanceof Double) {
 							// only look at rows greater than the absolute
 							// smallest value specified
 							if ((Math.abs((Double) o1) > smallestAbsoluteValue)
 							        || (Math.abs((Double) o2) > smallestAbsoluteValue)) {
 								double percentageDiff;
 								percentageDiff = calculatePercentageDifference((Double) o1, (Double) o2);
 								if (percentageDiff > tolerancePercentage) {
 									double absDiff;
 									absDiff = Math.abs((Double) o1 - (Double) o2);
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof BigDecimal && o2 instanceof Double) {
 							// only look at rows greater than the absolute
 							// smallest value specified
 							// NSB - 16/6/04 - Added as Oracle returns Big
 							// Decimals
 							if ((Math.abs(((BigDecimal) o1).doubleValue()) > smallestAbsoluteValue)
 							        || (Math.abs((Double) o2) > smallestAbsoluteValue)) {
 								double percentageDiff =
 								        Math.abs(((((BigDecimal) o1).doubleValue() - (Double) o2) / ((BigDecimal) o1)
 								                .doubleValue()) * 100);
 								if (percentageDiff > tolerancePercentage) {
 									double absDiff = Math.abs(((BigDecimal) o1).doubleValue() - (Double) o2);
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof Double && o2 instanceof BigDecimal) {
 							// only look at rows greater than the absolute
 							// smallest value specified
 							// NSB - 16/6/04 - Added as Oracle returns Big
 							// Decimals
 							if ((Math.abs((Double) o1) > smallestAbsoluteValue)
 							        || (((BigDecimal) o2).abs().doubleValue() > smallestAbsoluteValue)) {
 								double percentageDiff =
 								        Math.abs(((((Double) o1) - ((BigDecimal) o2).doubleValue()) / (Double) o1) * 100);
 								if (percentageDiff > tolerancePercentage) {
 									double absDiff = Math.abs((Double) o1 - ((BigDecimal) o2).doubleValue());
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof BigDecimal && o2 instanceof BigDecimal) {
 
 							if (((BigDecimal) o1).abs().compareTo(BigDecimal.valueOf(smallestAbsoluteValue)) == 1
 							        || ((BigDecimal) o2).abs().compareTo(BigDecimal.valueOf(smallestAbsoluteValue)) == 1) {
                         		BigDecimal percentageDiff = ((BigDecimal) o1).subtract(((BigDecimal) o2)).divide(( ((BigDecimal) o1).compareTo(BigDecimal.valueOf(0)) == 0  ? (BigDecimal) o2 : (BigDecimal) o1),6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
 								if (percentageDiff.compareTo(BigDecimal.valueOf(tolerancePercentage)) == 1) {
 									BigDecimal absDiff = ((BigDecimal) o1).subtract((BigDecimal) o2).abs();
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof Integer && o2 instanceof Integer) {
 							try {
 								// only look at rows greater than the absolute
 								// smallest value specified
 								if (Math.abs(((Integer) o1).intValue()) > smallestAbsoluteValue
 								        || Math.abs(((Integer) o2).intValue()) > smallestAbsoluteValue) {
 									int percentageDiff = Math.abs((((Integer) o1 - (Integer) o2) / (Integer) o1) * 100);
 									if (percentageDiff > tolerancePercentage) {
 										int absDiff = Math.abs((Integer) o1 - (Integer) o2);
 										logDifference((String) inputProperties1.get("key"), key, input1Alias,
 										        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 										        inputColumns2[input2CompareColumnPosition[i]], o2,
 										        valueOf(percentageDiff), valueOf(absDiff));
 										matchedRow = false;
 									}
 								}
 							} catch (ArithmeticException ae) {
 								if (!o1.equals(o2)) {
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 									matchedRow = false;
 								}
 
 							}
 						} else if (o1 instanceof String && o2 instanceof String) {
 							if (!o1.equals(o2)) {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else if (o1 instanceof Boolean && o2 instanceof Boolean) {
 
 							if (!o1.equals(o2)) {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else if (o1 instanceof java.util.Date && o2 instanceof java.util.Date) {
 							if (!o1.equals(o2)) {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else if (o1 == null || o2 == null) {
 							if (o1 == null && o2 == null) {
 								// do nothing
 							} else {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else {
 							LOGGER.severe("Either encountered 2 different data types, or un-handled data type!");
 							LOGGER.severe("O1= " + o1.getClass().getName() + ", O2= " + o2.getClass().getName());
 							unhandledRow = true;
 						}
 					}
 
 					if (matchedRow)
 						input1MatchedRows++;
 
 					// At this point we have:
 					// - Found a matching row
 					// - Checked all the columns which needed to be checked
 					//
 					// Provide that 'unhandledRow' is not true we can remove
 					// this row entry from inputData1 & inputData2
 					if (!unhandledRow) {
 						// remove from inputData1
 						inputIterator.remove();
 						// remove from inputData2
 						inputData2.remove(key);
 					}
 				}
 			}
 
 			// At this point the data in inputData1 & inputData2 are unmatched
 			// items only. ie enteries which are in one
 			// data set and not in the other.
 			//
 			// Now we just need to traverse each in turn and display them
 			inputIterator = inputData1.keySet().iterator();
 			while (inputIterator.hasNext()) {
 				String key = (String) inputIterator.next();
 				// for keys that are missing,show all the values that are actually there, vs 'Missing'
 				for (int anInput1CompareColumnPosition : input1CompareColumnPosition) {
 					Object o1 = ((ArrayList) inputData1.get(key)).get(anInput1CompareColumnPosition);
 
 					if ((o1 instanceof Double || o1 instanceof Integer || o1 instanceof String)) {
 						// only log a difference here, if o1 is <> 0.0, even if
 						// 02 is actually missing..
 						logDifference((String) inputProperties1.get("key"), key, input1Alias,
 						        inputColumns1[anInput1CompareColumnPosition], o1, input2Alias, "Missing", "Missing",
 						        "", "");
 					}
 				}
 			}
 			inputIterator = inputData2.keySet().iterator();
 			while (inputIterator.hasNext()) {
 				String key = (String) inputIterator.next();
 				// for keys that are missing,show all the values that are actually there, vs 'Missing'
 				for (int anInput2CompareColumnPosition : input2CompareColumnPosition) {
 					Object o1 = ((ArrayList) inputData2.get(key)).get(anInput2CompareColumnPosition);
 					if (((o1 instanceof Double) || (o1 instanceof Integer) || (o1 instanceof String))) {
 						// only log a difference here, if o1 is <> 0.0, even if
 						// 02 is actually missing..
 						logDifference((String) inputProperties2.get("key"), key, input2Alias, "Missing", "Missing",
 						        input1Alias, inputColumns2[anInput2CompareColumnPosition], o1, "", "");
 					}
 				}
 			}
 		} else {
 			throw new Exception("A reconciliation requires 2 or more data inputs - current data inputs size is "
 			        + m_dataToCompare.size());
 		}
 
 		logSummary(input1Alias, inputData1Size, input2Alias, inputData2Size, input1MatchedRows);
 	}
 
 	private double calculatePercentageDifference(Double o1, Double o2) {
 		double percentageDiff;
 		percentageDiff = Math.abs(o1 - o2) / o1 * 100;
 		return percentageDiff;
 	}
 
 	/**
 	 * Method recData.
 	 * 
 	 * @throws Exception
 	 *             if there is a problem with the processing
 	 */
 	private void oldRecData() throws Exception {
 
 		String[] inputColumns1;
 		String[] inputColumns2;
 		HashMap inputData1;
 		HashMap inputData2;
 		SuperProperties inputProperties1;
 		SuperProperties inputProperties2;
 
 		String input1Alias;
 		String input2Alias;
 
 		int input1MatchedRows = 0;
 		float tolerancePercentage;
 		float smallestAbsoluteValue;
 
 		LOGGER.info("Starting to reconcile data sources...");
 
 		if (m_dataToCompare.size() >= 2) {
 			inputColumns1 = (String[]) ((HashMap) m_dataToCompare.get("1")).get(COLUMNS);
 			inputData1 = (HashMap) ((HashMap) m_dataToCompare.get("1")).get(DATA);
 			inputProperties1 = (SuperProperties) ((HashMap) m_dataToCompare.get("1")).get(PROPERTIES);
 
 			inputColumns2 = (String[]) ((HashMap) m_dataToCompare.get("2")).get(COLUMNS);
 			inputData2 = (HashMap) ((HashMap) m_dataToCompare.get("2")).get(DATA);
 			inputProperties2 = (SuperProperties) ((HashMap) m_dataToCompare.get("2")).get(PROPERTIES);
 
 			// need a position of the compare columns in the array - do this by
 			// making every column which isn't a
 			// key column, a compare column
 			int[] input1CompareColumnPosition =
 			        ArrayUtils.getCompareColumnsPosition(inputColumns1,
 			                ArrayUtils.convertStringKeyToArray((String) inputProperties1.get("key"), m_delimiter));
 			int[] input2CompareColumnPosition =
 			        ArrayUtils.getCompareColumnsPosition(inputColumns2,
 			                ArrayUtils.convertStringKeyToArray((String) inputProperties2.get("key"), m_delimiter));
 
 			input1Alias = (String) inputProperties1.get("alias");
 			input2Alias = (String) inputProperties2.get("alias");
 
 			if (input1CompareColumnPosition.length != input2CompareColumnPosition.length)
 				throw new Exception("Unequal number of columns to compare - " + input1CompareColumnPosition.length
 				        + " vs " + input2CompareColumnPosition.length);
 
 			// now set the tolerance level as a percentage
 			tolerancePercentage = Float.parseFloat(((String) inputProperties1.get("tolerance")));
 			smallestAbsoluteValue = Float.parseFloat(((String) inputProperties1.get("smallestAbsoluteValue")));
 
 			Iterator inputIterator = inputData1.keySet().iterator();
 
 			LOGGER.info("Comparing " + decimalFormatter.format(inputData1.size()) + " rows from " + input1Alias
 			        + " with " + decimalFormatter.format(inputData2.size()) + " rows from " + input2Alias + " over "
 			        + input1CompareColumnPosition.length + " column(s)");
 
 			while (inputIterator.hasNext()) {
 				String key = (String) inputIterator.next();
 
 				boolean matchedRow = true;
 
 				if (inputData2.containsKey(key)) {
 					// loop round the input1 columns to compare - a row is only
 					// deemed as matched
 					// if _all_ the columns selected to compare, match..
 					for (int i = 0; i < input1CompareColumnPosition.length; i++) {
 						Object o1 = ((ArrayList) inputData1.get(key)).get(input1CompareColumnPosition[i]);
 						Object o2 = ((ArrayList) inputData2.get(key)).get(input2CompareColumnPosition[i]);
 
 						if (o1 instanceof Double && o2 instanceof Double) {
 							// only look at rows greater than the absolute smallest value specified
 							if (Math.abs((Double) o1) > smallestAbsoluteValue
 							        || Math.abs((Double) o2) > smallestAbsoluteValue) {
 								double percentageDiff = Math.abs((((Double) o1 - (Double) o2) / ((Double) o1)) * 100);
 								if (percentageDiff > tolerancePercentage) {
 									double absDiff;
 									absDiff = Math.abs((Double) o1 - ((Double) o2));
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof BigDecimal && o2 instanceof Double) {
 							// only look at rows greater than the absolute smallest value specified
 							// NSB - 16/6/04 - Added as Oracle returns Big Decimals
 							if ((Math.abs(((BigDecimal) o1).doubleValue()) > smallestAbsoluteValue)
 									|| (Math.abs(((Double) o2)) > smallestAbsoluteValue)) {
 								double percentageDiff =
 								        Math.abs(((((BigDecimal) o1).doubleValue() - ((Double) o2)) / ((BigDecimal) o1)
 								                .doubleValue()) * 100);
 								if (percentageDiff > tolerancePercentage) {
 									double absDiff = Math.abs(((BigDecimal) o1).doubleValue() - ((Double) o2));
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof Double && o2 instanceof BigDecimal) {
 							// only look at rows greater than the absolute
 							// smallest value specified
 							// NSB - 16/6/04 - Added as Oracle returns Big
 							// Decimals
 							if ((Math.abs((Double) o1) > smallestAbsoluteValue)
 									|| (((BigDecimal) o2).abs().doubleValue() > smallestAbsoluteValue)) {
 								double percentageDiff =
 								        Math.abs((((Double) o1 - ((BigDecimal) o2).doubleValue()) / (Double) o1) * 100);
 								if (percentageDiff > tolerancePercentage) {
 									double absDiff = Math.abs((Double) o1 - ((BigDecimal) o2).doubleValue());
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof BigDecimal && o2 instanceof BigDecimal) {
 							if (((BigDecimal) o1).abs().compareTo(BigDecimal.valueOf(smallestAbsoluteValue)) == 1
 									|| ((BigDecimal) o2).abs().compareTo(BigDecimal.valueOf(smallestAbsoluteValue)) == 1) {
                         		BigDecimal percentageDiff = ((BigDecimal) o1).subtract(((BigDecimal) o2)).divide(( ((BigDecimal) o1).compareTo(BigDecimal.valueOf(0)) == 0  ? (BigDecimal) o2 : (BigDecimal) o1),6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
 								if (percentageDiff.compareTo(BigDecimal.valueOf(tolerancePercentage)) == 1) {
 									BigDecimal absDiff = ((BigDecimal) o1).subtract((BigDecimal) o2).abs();
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, valueOf(percentageDiff),
 									        valueOf(absDiff));
 									matchedRow = false;
 								}
 							}
 						} else if (o1 instanceof Integer && o2 instanceof Integer) {
 							try {
 								// only look at rows greater than the absolute
 								// smallest value specified
 								if (Math.abs(((Integer) o1).intValue()) > smallestAbsoluteValue
 										|| Math.abs(((Integer) o2).intValue()) > smallestAbsoluteValue) {
 									int percentageDiff = Math.abs((((Integer) o1 - (Integer) o2) / (Integer) o1) * 100);
 									if (percentageDiff > tolerancePercentage) {
 										int absDiff = Math.abs((Integer) o1 - (Integer) o2);
 										logDifference((String) inputProperties1.get("key"), key, input1Alias,
 										        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 										        inputColumns2[input2CompareColumnPosition[i]], o2,
 										        valueOf(percentageDiff), valueOf(absDiff));
 										matchedRow = false;
 									}
 								}
 							} catch (ArithmeticException ae) {
 								if (!o1.equals(o2)) {
 									logDifference((String) inputProperties1.get("key"), key, input1Alias,
 									        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 									        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 									matchedRow = false;
 								}
 
 							}
 						} else if (o1 instanceof String && o2 instanceof String) {
 							if (!(o1.equals(o2))) {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else if (o1 instanceof Boolean && o2 instanceof Boolean) {
 
 							if (!o1.equals(o2)) {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else if (o1 instanceof java.util.Date && o2 instanceof java.util.Date) {
 							if (!(o1.equals(o2))) {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else if (o1 == null || o2 == null) {
 							if (o1 == null && o2 == null) {
 								// do nothing
 							} else {
 								logDifference((String) inputProperties1.get("key"), key, input1Alias,
 								        inputColumns1[input1CompareColumnPosition[i]], o1, input2Alias,
 								        inputColumns2[input2CompareColumnPosition[i]], o2, "", "");
 								matchedRow = false;
 							}
 						} else {
 							LOGGER.severe("Either encountered 2 different data types, or un-handled data type!");
 							LOGGER.severe("O1= " + o1.getClass().getName() + ", O2= " + o2.getClass().getName());
 						}
 					}
 
 				} else {
 					// for keys that are missing,show all the values that are actually there, vs 'Missing'
 					for (int anInput1CompareColumnPosition : input1CompareColumnPosition) {
 						Object o1 = ((ArrayList) inputData1.get(key)).get(anInput1CompareColumnPosition);
 
 						if ((o1 instanceof Double || o1 instanceof Integer || o1 instanceof String)) {
 							// only log a difference here, if o1 is <> 0.0, even
 							// if 02 is actually missing..
 							logDifference((String) inputProperties1.get("key"), key, input1Alias,
 							        inputColumns1[anInput1CompareColumnPosition], o1, input2Alias, "Missing",
 							        "Missing", "", "");
 							matchedRow = false;
 						}
 					}
 
 				}
 
 				if (matchedRow)
 					input1MatchedRows++;
 			}
 		} else {
 			throw new Exception("A reconciliation requires 2 or more data inputs - current data inputs size is "
 			        + m_dataToCompare.size());
 		}
 
 		logSummary(input1Alias, inputData1.size(), input2Alias, inputData2.size(), input1MatchedRows);
 	}
 
 	/**
 	 * wait for all the worker threads to finish....only returns after all have completed.
 	 * 
 	 * @throws InterruptedException
 	 *             if there is a thread problem
 	 */
 	private void waitForThreads() throws InterruptedException {
 		Thread[] threads = new Thread[m_workerGroup.activeCount()];
 
 		m_workerGroup.enumerate(threads);
 
 		for (Thread thread : threads) {
 			if (thread.isAlive())
 				thread.join();
 
 			LOGGER.info("Thread " + thread.getName() + " finished");
 		}
 	}
 
 	/**
 	 * start up all the worker threads to start loading the data
 	 */
 	private void startThreads() {
 		// loop through the sources and start them loading...
 
 		for (Object o : m_propertiesMap.keySet()) {
 			String key = (String) o;
 			SuperProperties sourceProperties = (SuperProperties) m_propertiesMap.get((key));
 
 			String type = (String) sourceProperties.get("type");
 			RecxxWorker worker = null;
 
 			if (type.equals(DB_INPUT)) {
 				worker = new DatabaseFacadeWorker(prefix, propertiesFile);
 			} else if (type.equals(FILE_INPUT)) {
 				worker = new FileFacadeWorker(prefix, propertiesFile);
 			}
 			if (worker != null) {
 				worker.setRunTimeProperties(sourceProperties);
 				worker.setDataStore(this);
 
 				Thread t = new Thread(m_workerGroup, worker, key);
 				t.start();
 			}
 		}
 	}
 
 	/**
 	 * load most of the properties in when the class initialises, to make the log files ,look clearer
 	 * 
 	 * @throws Exception
 	 *             if there is a problem with the properties file supplied.
 	 */
 	private void loadProperties() throws Exception {
 		// load most of the parameters in at the beginning, as it looks neater in the log files
 		m_propertiesMap = new HashMap();
 		SuperProperties props;
 
 		String propertiesStub = format("%s.%s.", prefix, m_appName);
 
 		int numberOfInputs = 2;
 		String tolerance = superProps.getProperty(propertiesStub + "toleranceLevel", "0.0");
 		String handleNullsAsZero = superProps.getProperty(propertiesStub + "handleNullsAsDefault", "true");
 		String smallestAbsoluteValue = superProps.getProperty(propertiesStub + "smallestAbsoluteValue", "0.0001");
 
 		m_delimiter = superProps.getProperty(propertiesStub + "delimiter", " ");
 
 		m_dpFormatter =
 		        new DecimalFormat(superProps.getProperty(propertiesStub + "decimalPlacesPattern", "#.00000000000"));
 
 		m_outputType = superProps.getProperty(propertiesStub + "outputType", "csv");
 
 		// get the recMode property determining whether the reconciliation is one-way or two-way
 		reconciliationMode = superProps.getProperty(propertiesStub + "reconciliationMode", TW.toString());
 		switch (ReconciliationMode.valueOf(reconciliationMode)) {
 		case OW:
 			LOGGER.info("Performing one-way reconciliation...");
 			reconciliationMode = OW.toString();
 			break;
 		case TW:
 			LOGGER.info("Performing two-way reconciliation...");
			reconciliationMode = TW.toString();
 			break;
 		}
 
 		if (m_outputType.equals("csv")) {
 			FILE_LOCATION = superProps.getProperty(propertiesStub + "logger.csv.file");
 			FILE_DELIMITER = superProps.getProperty(propertiesStub + "logger.csv.file.delimiter", CONSTANTS.DELIMITER);
 		}
 
 		// TODO remove the redundancies here!!
 
 		for (int i = 1; i <= numberOfInputs; i++) {
 			String inputStub = propertiesStub + "inputSource" + i + ".";
 			String inputAlias = superProps.getProperty(inputStub + "name.alias");
 			if (inputAlias == null) {
 				throw new PropertiesFileException("No alias found for Reconciliation Source: " + inputStub
 				        + "name.alias");
 			}
 			String inputType = superProps.getProperty(inputStub + "name.type");
 			if (inputType == null) {
 				throw new PropertiesFileException("No type found for Reconciliation Source: " + inputStub + "name.type");
 			}
 
 			props = new SuperProperties();
 			props.setProperty("alias", inputAlias);
 			props.setProperty("type", inputType);
 			props.setProperty("tolerance", tolerance);
 			props.setProperty("handleNullsAsZero", handleNullsAsZero);
 			props.setProperty("smallestAbsoluteValue", smallestAbsoluteValue);
 			props.setProperty("order", valueOf(i));
 			props.setProperty("delimiter", m_delimiter);
 
 			if (inputType.equals(DB_INPUT)) {
 				// Database source
 				props.setProperty("uid", superProps.getProperty(inputStub + "db.uid"));
 				props.setProperty("pwd", superProps.getProperty(inputStub + "db.pwd"));
 				props.setProperty("url", superProps.getProperty(inputStub + "db.jdbc.url"));
 				props.setProperty("driver", superProps.getProperty(inputStub + "db.jdbc.driver"));
 				props.setProperty("sql", superProps.getProperty(inputStub + "db.sql"));
 				props.setProperty("key", superProps.getProperty(inputStub + "db.key"));
 				props.setProperty("aggregate", "false");
 
 				m_propertiesMap.put(inputAlias, props);
 			} else if (inputType.equals(FILE_INPUT)) {
 				// delimited file source
 				String filePath = superProps.getProperty(inputStub + "file.filePath");
 				if (filePath == null) {
 					throw new PropertiesFileException("Cannot continue as file hasn't been set: " + inputStub
 					        + "file.filePath");
 				}
 				props.setProperty("filePath", filePath);
 				props.setProperty("delimiter", superProps.getProperty(inputStub + "file.delimiter", ","));
 				props.setProperty("columnsSupplied", superProps.getProperty(inputStub + "file.firstRowColumns", "true"));
 				props.setProperty("dataTypesSupplied",
 				        superProps.getProperty(inputStub + "file.secondRowDataTypes", "false"));
 
 				if (!props.getProperty("columnsSupplied").equals("true")) {
 					// then the columns have to be specified in a property,
 					// separated by spaces (just like the key)
 					String fileColumns = superProps.getProperty(inputStub + "file.columns");
 					if (fileColumns == null) {
 						throw new PropertiesFileException(inputStub + "file.firstRowColumns has been set to false but "
 						        + inputStub + "file.columns hasn't been specified.");
 					}
 					props.setProperty("columns", fileColumns);
 				}
 				String columnDataTypes = superProps.getProperty(inputStub + "file.columnDataTypes");
 				if (columnDataTypes == null) {
 					throw new PropertiesFileException(inputStub
 					        + "file.columnDataTypes needs all the datatypes for the columns");
 				}
 				props.setProperty("columnDataTypes", columnDataTypes);
 				props.setProperty("dateFormat",
 				        superProps.getProperty(inputStub + "file.columnDataTypes.date.format", "yyyyMMdd"));
 				String key = superProps.getProperty(inputStub + "file.key");
 				if (key == null) {
 					throw new PropertiesFileException("No record key supplied " + inputStub + "file.key");
 				}
 				props.setProperty("key", key);
 				String columnsToCompare = superProps.getProperty(inputStub + "file.columnsToCompare");
 				if (columnsToCompare == null) {
 					throw new PropertiesFileException("No Columns To Compare supplied " + inputStub
 					        + "file.columnsToCompare");
 				}
 				props.setProperty("columnsToCompare", columnsToCompare);
 				props.setProperty("aggregate", superProps.getProperty(inputStub + "file.aggregate", "false"));
 				props.setProperty("appendDelimiter",
 				        superProps.getProperty(inputStub + "file.appendDelimiter", "false"));
 
 				m_propertiesMap.put(inputAlias, props);
 			} else {
 				throw new PropertiesFileException("Invalid input source	" + inputType + " - can only be File or DB");
 			}
 		}
 
 		if (m_propertiesMap.size() != numberOfInputs)
 			throw new PropertiesFileException(numberOfInputs + " were not loaded...!");
 	}
 
 	/**
 	 * if the propertyValue starts with the checkValue, ignoring the case of each, then treat the propertyValue as a
 	 * file path and load up from there, otherwise just return the propertyValue
 	 * 
 	 * @param propertyValue
 	 *            the property value
 	 * @param checkValue
 	 *            the check value
 	 * @return String return value
 	 * @throws Exception
 	 *             if there is a problem
 	 */
 	private String loadStringPropertyFromFile(String propertyValue, String checkValue) throws Exception {
 
 		if (!propertyValue.toLowerCase().startsWith(checkValue.toLowerCase())) {
 			// then assume its a path to a file, so treat it as such
 			FileReader fr = null;
 			BufferedReader br = null;
 			StringBuilder realPropertyValue = new StringBuilder();
 
 			try {
 				fr = new FileReader(propertyValue);
 				br = new BufferedReader(fr);
 
 				while (br.ready()) {
 					realPropertyValue.append(br.readLine());
 				}
 			} catch (FileNotFoundException e) {
 				LOGGER.severe("Property value file name " + propertyValue + " could not be found");
 				throw new Exception(e.getMessage());
 			} catch (IOException ioe) {
 				LOGGER.severe("Problem reading from file " + propertyValue);
 				throw new Exception(ioe.getMessage());
 			} finally {
 				// tidy up and close the file references
 				if (br != null)
 					br.close();
 
 				if (fr != null)
 					fr.close();
 			}
 
 			return realPropertyValue.toString();
 		} else {
 			// its the actual value we want, not a reference to a file which
 			// contains the data
 			// so just return the value
 			return propertyValue;
 		}
 	}
 
 	/**
 	 * Sets the dataToCompare.
 	 * 
 	 * @param dataToCompare
 	 *            The dataToCompare to set
 	 * @param key
 	 *            to use for the compare
 	 */
 	public synchronized void setDataToCompare(HashMap dataToCompare, String key) {
 		m_dataToCompare.put(key, dataToCompare);
 		m_dataToCompareKey++;
 	}
 
 	/**
 	 * Log a difference between the 2 data sets. Depending on m_outputType, the difference is logged to System.err.or a
 	 * specified csv file.
 	 * 
 	 * @param keyColumns
 	 *            keyColumns
 	 * @param key
 	 *            key
 	 * @param alias1
 	 *            alias1
 	 * @param columnName1
 	 *            columnName1
 	 * @param columnValue1
 	 *            columnValue1
 	 * @param alias2
 	 *            alias2
 	 * @param columnName2
 	 *            columnName2
 	 * @param columnValue2
 	 *            columnValue2
 	 * @param absDiff
 	 *            absDiff
 	 */
 	private void logDifference(String keyColumns, String key, String alias1, String columnName1, Object columnValue1,
 	        String alias2, String columnName2, Object columnValue2, String percentageDiff, String absDiff) {
 		if (m_outputType.equals("csv")) {
 			try {
 				initCsvFile(alias1, alias2, keyColumns);
 				logDifferenceToFile(key, columnName1, columnValue1, columnName2, columnValue2, percentageDiff, absDiff);
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 
 		} else if (m_outputType.equals("err")) {
 			logDifferenceToSystemErr(key, alias1, columnName1, columnValue1, alias2, columnName2, columnValue2);
 		}
 	}
 
 	/**
 	 * log a difference to csv file
 	 * 
 	 * @param key
 	 *            key
 	 * @param columnName1
 	 *            columnName1
 	 * @param columnValue1
 	 *            columnValue1
 	 * @param columnName2
 	 *            columnName2
 	 * @param columnValue2
 	 *            columnValue2
 	 * @param percentageDiff
 	 *            percentageDiff
 	 * @param absDiff
 	 *            absDiff
 	 * @throws java.io.IOException
 	 *             if there is a problem writing to a file
 	 */
 	private void logDifferenceToFile(String key, String columnName1, Object columnValue1, String columnName2,
 	        Object columnValue2, String percentageDiff, String absDiff) throws IOException {
 		StringTokenizer st = new StringTokenizer(key, "+");
 
 		while (st.hasMoreTokens()) {
 			m_logger.write(st.nextToken());
 		}
 
 		m_logger.write(columnName1);
 
 		if (columnValue1 != null)
 			m_logger.write(columnValue1.toString());
 		else
 			m_logger.write("null");
 
 		m_logger.write(columnName2);
 
 		if (columnValue2 != null)
 			m_logger.write(columnValue2.toString());
 		else
 			m_logger.write("null");
 
 		m_logger.write(percentageDiff);
 		m_logger.writeLine(absDiff);
 
 	}
 
 	/**
 	 * log a summary report to file detailing rows matched etc etc
 	 * 
 	 * @param alias1
 	 *            alias1
 	 * @param rowCount1
 	 *            rowCount1
 	 * @param alias2
 	 *            alias2
 	 * @param rowCount2
 	 *            rowCount2
 	 * @param rowsMatched
 	 *            rowsMatched
 	 * @throws IOException
 	 *             if there is a problem writing to file
 	 */
 	private void logSummaryToFile(String alias1, int rowCount1, String alias2, int rowCount2, int rowsMatched)
 	        throws IOException {
 		// 2 blank lines to separate out the summary from the rest of the results
 		m_logger.writeLine("");
 		m_logger.writeLine("");
 		m_logger.writeLine("=======================");
 		m_logger.writeLine("Reconciliation Report");
 		m_logger.writeLine("=======================");
 		m_logger.write(alias1 + " rows");
 		m_logger.writeLine(rowCount1);
 		m_logger.write(alias2 + " rows");
 		m_logger.writeLine(rowCount2);
 		m_logger.write(alias1 + " matched to " + alias2);
 		m_logger.writeLine(rowsMatched);
 		m_logger.write(alias1 + " matched to " + alias2 + " %");
 
 		Integer i = rowsMatched;
 		Integer ii = rowCount1;
 		Integer ii2 = rowCount2;
 
 		m_logger.writeLine(m_dPercentageFormatter.format(i.floatValue() / ii.floatValue()));
 		m_logger.write(alias2 + " matched to " + alias1 + " %");
 		m_logger.writeLine(m_dPercentageFormatter.format(i.floatValue() / ii2.floatValue()));
 
 		// loop through the sources and start them loading...
 		Iterator sourceIterator = m_propertiesMap.keySet().iterator();
 		SuperProperties props = new SuperProperties();
 
 		while (sourceIterator.hasNext()) {
 			String key = (String) sourceIterator.next();
 			props = (SuperProperties) m_propertiesMap.get((key));
 		}
 
 		// at some point, put this in the equivalent of a toString() method
 		// and loop out all the properties values but maybe not the passwords!
 		m_logger.writeLine("");
 		m_logger.writeLine("");
 		m_logger.writeLine("=======================");
 		m_logger.writeLine("Report Properties");
 		m_logger.writeLine("=======================");
 		m_logger.write("Rec File Date/Time");
 		m_logger.writeLine(m_logger.toString());
 		m_logger.write("Tolerance Level %");
 		m_logger.writeLine(props.getProperty("tolerance"));
 		m_logger.write("HandleNullsAsZero?");
 		m_logger.writeLine(props.getProperty("handleNullsAsZero"));
 		m_logger.write("SmallestAbsoluteValue");
 		m_logger.writeLine(props.getProperty("smallestAbsoluteValue"));
 
 	}
 
 	/**
 	 * after the rec has finished, log summary information
 	 * 
 	 * @param alias1
 	 *            alias1
 	 * @param rowCount1
 	 *            rowCount1
 	 * @param alias2
 	 *            alias2
 	 * @param rowCount2
 	 *            rowCount2
 	 * @param rowsMatched
 	 *            rowsMatched
 	 * @throws IOException
 	 *             if there is a problem
 	 */
 	private void logSummary(String alias1, int rowCount1, String alias2, int rowCount2, int rowsMatched)
 	        throws IOException {
 		if (m_outputType.equals("csv")) {
 			try {
 				initCsvFile(alias1, alias2, "");
 				logSummaryToFile(alias1, rowCount1, alias2, rowCount2, rowsMatched);
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 
 		}
 		// always log this anyway
 		LOGGER.info("Finished reconciliation: " + decimalFormatter.format(rowsMatched) + "/"
 		        + decimalFormatter.format(rowCount1) + " rows of " + alias1 + " matched with " + alias2 + " ("
 		        + decimalFormatter.format(rowCount2) + ")");
 	}
 
 	/**
 	 * log a difference to System.Err. WARNING: Slows performance down lots and lots...!
 	 * 
 	 * @param key
 	 *            key
 	 * @param alias1
 	 *            alias1
 	 * @param columnName1
 	 *            columnName1
 	 * @param columnValue1
 	 *            columnValue1
 	 * @param alias2
 	 *            alias2
 	 * @param columnName2
 	 *            columnName2
 	 * @param columnValue2
 	 *            columnValue2
 	 */
 	private void logDifferenceToSystemErr(String key, String alias1, String columnName1, Object columnValue1,
 	        String alias2, String columnName2, Object columnValue2) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("INFO: ").append("Key ").append(key).append(" | ");
 		sb.append(alias1).append(".").append(columnName1).append(" = ").append(columnValue1).append(", ");
 		sb.append(alias2).append(".").append(columnName2).append(" = ").append(columnValue2);
 		System.err.println(sb.toString());
 	}
 
 	/**
 	 * initialise the csv file given the data input alias names and the key column names
 	 * 
 	 * @param alias1
 	 *            alias1
 	 * @param alias2
 	 *            alias2
 	 * @param keyColumns
 	 *            keyColumns
 	 * @throws java.io.IOException
 	 *             if there is a problem
 	 */
 	private void initCsvFile(String alias1, String alias2, String keyColumns) throws IOException {
 		if (!m_loggerInit) {
 			m_logger = new CSVLogger();
 			m_logger.setBufferedWriterManager(new BufferedWriterManager(new CloseableUtils()));
 			m_logger.setFilename(FILE_LOCATION);
 			m_logger.setDelimiter(FILE_DELIMITER);
 			m_logger.open();
 
 			// write column headers
 
 			StringTokenizer st = new StringTokenizer(keyColumns, m_delimiter);
 
 			// recurse out all the keys as separate columns to make sorting in
 			// excel
 			// easier.
 			while (st.hasMoreTokens()) {
 				m_logger.write("Key(" + st.nextToken() + ")");
 			}
 			m_logger.write(alias1 + ".columnName");
 			m_logger.write(alias1 + ".columnValue");
 			m_logger.write(alias2 + ".columnName");
 			m_logger.write(alias2 + ".columnValue");
 			m_logger.write("% Diff");
 			m_logger.writeLine("Abs Diff");
 
 			m_loggerInit = true;
 		}
 	}
 
 }
