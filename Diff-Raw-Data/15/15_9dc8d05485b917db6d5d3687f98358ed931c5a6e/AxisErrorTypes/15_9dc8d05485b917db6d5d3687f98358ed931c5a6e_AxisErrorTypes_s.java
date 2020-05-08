 package de.ptb.epics.eve.data.scandescription.errors;
 
 /**
  * Contains all possible error types of an axis.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld (-at-) ptb.de>
  * @author scherr
  *
  */
 public enum AxisErrorTypes {
 
 	/**
 	 * The start value is not set.
 	 */
 	START_NOT_SET,
 	
 	/**
 	 * The stop value is not set.
 	 */
 	STOP_NOT_SET,
 	
     /**
 	 * The step width value is not set.
 	 */
 	STEPWIDTH_NOT_SET,
 	
     /**
 	 * The step count value is not set.
 	 */
 	STEPCOUNT_NOT_SET,
 	
 	/**
 	 * The start value is invalid.
 	 */
 	START_VALUE_NOT_POSSIBLE,
 	
     /**
 	 * The stop value is invalid.
 	 */
 	STOP_VALUE_NOT_POSSIBLE,
 	
 	/**
 	 * The filename is not set.
 	 */
 	FILENAME_NOT_SET,
 	
     /**
 	 * The position list is not set.
 	 */
 	POSITIONLIST_NOT_SET,
 
     /**
 	 * the plug in contains an error.
 	 */
 	PLUGIN_ERROR
 }
