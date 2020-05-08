 package de.ptb.epics.eve.data.scandescription.errors;
 
 
 /**
  * This enumeration contains all possible errors of a channel.
  * 
  * 
  * @author Stephan Rehfeld <stephan.rehfeld (-at-) ptb.de>
  * @author Hartmut Scherr
  *
  */
 public enum ChannelErrorTypes {
 
 	/**
 	 * The maximum deviation is not a possible value.
 	 */
 	MAX_DEVIATION_NOT_POSSIBLE,
 	
 	/**
 	 * The minimum is not a possible value.
 	 */
 	MINIMUM_NOT_POSSIBLE,
 	
 	/**
	 * The minimum is not a possible value.
	 */
	MAX_ATTEMPTS_NOT_POSSIBLE,

	/**
 	 * In the plugin is an error.
 	 */
 	PLUGIN_ERROR
 }
