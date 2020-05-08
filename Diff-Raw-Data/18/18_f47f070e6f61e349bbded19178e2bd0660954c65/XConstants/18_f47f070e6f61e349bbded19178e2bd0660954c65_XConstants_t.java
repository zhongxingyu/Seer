 package savi.hcat.common.util;
 
 public class XConstants {
 
 	
 	
 	/*	
 	 *
 	 * "object"
 	“status”: finished/unfinished
   	 “start-time”: 
   	 “end-time”:
   	 “cities”: [city1,city2]       => this is a particular attribute */
 	/**
 	 * Common parameters in query
 	 */
 	public static final String POST_KEY_START_TIME="start-time";
 	public static final String POST_KEY_END_TIME="end-time";
 	public static final String POST_KEY_REGIONS="regions"; // "c1,c2,c3"
 	public static final String POST_KEY_SPLIT_BY="split-by"; // This is to make the large query to smaller one, hope it is common, but it only works for time-series now
 	public static final String POST_KEY_SPLIT_NUM="split-num"; // How many you want to split
 	
 	/**
 	 *  specially for statistics query
 	 */
 	public static final String POST_KEY_OBJECT="object";
 	public static final String POST_KEY_CONDITION="condition";
 	public static final String POST_KEY_UNIT="unit"; // per m/w/d ==> month/week/day
 	public static final String POST_KEY_NUMBERATOR="numerator"; // per m/w/d ==> month/week/day
 	public static final String POST_KEY_DENOMINATOR="denominator"; // per m/w/d ==> month/week/day
 	/**
 	 * specially for spatial query
 	 */
 	public static final String POST_KEY_LOCATION="location"; // for range query
 	public static final String POST_KEY_DISTANCE="distance"; // for range query
 	public static final String POST_KEY_AREAS="areas"; //areas for windows query: [NW,NE,SW,SE]
 	public static final String POST_KEY_K="k"; //for knn query, the number of objects
 	
 	
 	// the object value
 	public static final String POST_VALUE_APPOINTMENT="appointment";
 	public static final String POST_VALUE_RECORD="record";  
 	public static final String POST_VALUE_SERVICE="service";
 	public static final String POST_VALUE_MEDIA="media";
 	public static final String POST_VALUE_HCA="hca";
 	public static final String POST_VALUE_PATIENT="patient";
 	
 	// query
 	public static final String QUERY_TYPE_WINDOW="window";
 	public static final String QUERY_TYPE_RANGE="range";
 	public static final String QUERY_TYPE_KNN="knn";
 	
 	// query statistics 
 	public static final String REQUEST_COPS_STAT="cops_stat";
 	public static final String REQUEST_STAT_COP_EXE_TIME="cop_time";
 	public static final String REQUEST_STAT_ROWS="num_of_rows";
 	public static final String REQUEST_STAT_CELLS="num_of_cells";
 	public static final String REQUEST_CELL_SIZE="cell_size";
	public static final String REQUEST_STAT_START_ROW="start_row";
	public static final String REQUEST_STAT_END_ROW="end_row";
 	public static final String REQUEST_STAT_RESPONSE_TIME="response_time";
 	
 	/**
 	 * HBase table
 	 */
 	public static final String TABLE_APPOINTMENT="appointment";
 	public static final String TABLE_PATIENT="patient";
 	
 	public static final String ROW_KEY_DELIMETER = "-";
 	
 	
 	/**
 	 * fields in JSON object in each table
 	 */
 	public static final String FIELD_NAME_LATTITUDE="lattd";
 	public static final String FIELD_NAME_LONGITUDE="longtd";
 	
 }
