 /*******************************************************************************
  * Copyright (c) 2001, 2007 Physikalisch Technische Bundesanstalt.
  * All rights reserved.
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package de.ptb.epics.eve.data;
 
 /**
  * 
  * This enum defines the event types which are existing in the Application.
  * This could be a monitor or a shedule event.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @version 1.3
  * @see de.ptb.epics.eve.data.measuringstation.Event
  *
  */
 public enum EventTypes {
 
 	/**
 	 * Used for moitor events, this are events wich are defined in the scan
 	 * description and represents a state at the measuring station.
 	 */
 	MONITOR,
 	/**
 	 * Used for schedule events, this are events which are defined in a
 	 * relation to a Scan Modul an occures when the Scan Modul has finished.
 	 */
 	SCHEDULE,
 	
 	/**
 	 * Used for detector ready event, which may be sent if a detector is ready with taking data.
 	 */
 	DETECTOR;
 	
 	/**
 	 * This static method translates a String of a MethodType, like it's used
 	 * in the scan and mesauring station description into the correpondenting
 	 * Enum value. Possible values are: monitor and shedule.
 	 * 
 	 * @param name The String that should be translated. Must not be null!
 	 * @return The correspondenting EventType.
 	 */
 	public static EventTypes stringToType( final String name ) {
 		if( name == null ) {
 			throw new IllegalArgumentException( "The parameter 'name' must not be null!" );
 		}
 		
 		if( name.equals( "monitor" ) ) {
 			return EventTypes.MONITOR;
 		} else if( name.equals( "schedule" ) ) {
 			return EventTypes.SCHEDULE;
 		} else if( name.equals( "detector" ) ) {
 			return EventTypes.DETECTOR;
 		}
 		return null;
 	}
 	
 	/**
 	 * This static method translates a EventType into a String, like it's used in the measuring
 	 * station description or the scan description. 
 	 * 
 	 * @param type The type, that should be translated. Must not be null!
 	 * @return The correpondentin string. Null if the Type was invalid.
 	 */
 	public static String typeToString( final EventTypes type ) {
 		if( type == null ) {
 			throw new IllegalArgumentException( "The parameter 'type' must not be null!" );
 		}
 		
 		switch( type ) {
 			case MONITOR:
 				return "monitor";
 			case SCHEDULE:
 				return "schedule";
 			case DETECTOR:
 				return "detector";
 		}
 		return null;
 	}
 	
 }
