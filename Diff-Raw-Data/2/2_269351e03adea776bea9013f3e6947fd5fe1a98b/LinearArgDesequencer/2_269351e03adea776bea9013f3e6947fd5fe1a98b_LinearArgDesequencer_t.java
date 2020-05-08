 /*  Written by Andrew Keller
  *
  *  This file is licensed as BSD.  Copyright 2009 Andrew Keller.
  *
  *  Class LinearArgDesequencer
  *
  *  Accepts a String array of arguments that was
  *  passed to this program, and converts them
  *  into logical ideas.
  *
  *  Functions of interest:
  *    private void desequenceArgs( String [] args )
  *      - Analyzes the given arguments.  Called by the constructor.
  */
 
 package com.kfs.bsd.common.util;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 
 public class LinearArgDesequencer {
 	
 	// Error codes
 	
 	public static final int kErrorCodeNone = 0;
 	public static final int kErrorCodeInternal = 1;
 	public static final int kErrorCodeUnknownArgument = 2;
 	public static final int kErrorCodeMissingArgument = 3;
 	
 	// Argument IDs for arguments submitted by this class
 	
 	protected static final String kArgKeyHelp = "Help";
 	protected static final String kArgKeyVerbose = "Verbose";
 	
 	// Non-static class properties for storing results
 	
 	protected boolean _parseError = false;
 	protected int _parseErrorCode = 0;
 	protected String _parseErrorMsg = null;
 	
 	protected CLArgumentSet argSet = new CLArgumentSet();
 	protected ArrayList<String> requiredArgumentKeys = new ArrayList<String>();
 	
 	// The primary function to override in a subclass of this class
 	
 	protected void populateArgSet() {
 		
 		// This function is called by the constructor just before
 		// the arguments are parsed.  This is our chance to define
 		// the arguments we would like to find.  In your implementation,
 		// you may choose to override this function completely, or
 		// override it and call super.populateArgSet() to add these
 		// options to the argument pool.
 		
 		argSet.addArgumentInfo( kArgKeyHelp, 'h', "help", 0, false, "Help flag" );
 		argSet.addArgumentInfo( kArgKeyVerbose, 'v', "verbose", 0, true, "Verbose flag" );
 	}
 	
 	// The constructors
 	
 	public LinearArgDesequencer() {
 		
 		// Basic Constructor.
 		
 		super();
 		
 		populateArgSet();
 		
 		desequenceArgs( new String [0] );
 	}
 	
 	public LinearArgDesequencer( String [] args ) {
 		
 		// Standard Constructor.  Takes an argument array.
 		
 		super();
 		
 		populateArgSet();
 		
 		desequenceArgs( args );
 	}
 	
 	// Accessors for error codes
 	
 	public boolean sequenceError() {
 		
 		// An accessor that returns whether or not desequenceArgs returned an error.
 		
 		return _parseError;
 	}
 	
 	public int sequenceErrorCode() {
 		
 		// An accessor that returns the error code returned by desequenceArgs.
 		// Zero implies no error occurred.
 		
 		return _parseErrorCode;
 	}
 	
 	public String sequenceErrorMsg() {
 		
 		// An accessor that returns the error message returned by desequenceArgs.
 		// null implies no error code.
 		
 		return _parseErrorMsg;
 	}
 	
 	// Accessors for the Help and Verbosity arguments
 	
 	public boolean foundHelpFlag() {
 		
 		// A convenience function that returns whether or not the help flag was found.
 		
 		return ! argSet.makeGet( kArgKeyHelp ).parcels.isEmpty();
 	}
 	
 	public int verbosity() {
 		
 		// A convenience function that returns the verbosity described by the arguments.
 		
		return argSet.makeGet( kArgKeyVerbose ).parcels.size();
 	}
 	
 	// The primary desequencing function
 	
 	protected void desequenceArgs( String [] args ) {
 		
 		// This function parses the given argument array and places the
 		// results within the protected variables in this class instance.
 		
 		// Initialize the current unbounded parcel
 		
 		String unboundedParcelKey = null;
 		
 		// Initialize the queue of parcels.
 		
 		Queue<String> parcelQueue = new LinkedList<String>();
 		
 		// Add the required parcels to the queue.
 		
 		for( int row = 0 ; row < requiredArgumentKeys.size() ; row++ )
 			argSet.addRequiredParcelsToQueue( parcelQueue, requiredArgumentKeys.get( row ) );
 		
 		// Begin scanning the array.
 		
 		for( int index = 0 ; index < args.length ; index ++ ) {
 			
 			String arg = args[ index ];
 			
 			if( arg.length() > 0 ) {
 				
 				// The first character tells a lot.
 				
 				char fc = arg.charAt( 0 );
 				
 				if( fc == '-' ) {
 					
 					// This is an option argument.
 					
 					if( arg.length() > 1 ) {
 						
 						if( arg.charAt( 1 ) == '-' ) {
 							
 							// This is a word-argument.
 							
 							// Figure out which one it is
 							
 							String sswitch = arg.substring( 2 );
 							
 							String argKey = argSet.getArgKeyForSwitch( sswitch );
 							
 							if( argKey != null ) {
 								
 								argSet.foundParcel( argKey, null );
 								argSet.addRequiredParcelsToQueue( parcelQueue, argKey );
 							}
 							else {
 								
 								// This is an unknown argument.
 								
 								_parseError = true;
 								_parseErrorCode = kErrorCodeUnknownArgument;
 								_parseErrorMsg = "Unknown option: '" + arg + "'";
 								return;
 							}
 						}
 						else {
 							
 							// This is a character-argument.
 							
 							for( int i = 1 ; i < arg.length() ; i++ ) {
 								
 								char c = arg.charAt( i );
 								
 								String argKey = argSet.getArgKeyForFlag( c );
 								
 								if( argKey != null ) {
 									
 									argSet.foundParcel( argKey, null );
 									argSet.addRequiredParcelsToQueue( parcelQueue, argKey );
 								}
 								else {
 									
 									// This is an unknown argument.
 									
 									_parseError = true;
 									_parseErrorCode = kErrorCodeUnknownArgument;
 									_parseErrorMsg = "Unknown option: '" + c + "'";
 									return;
 								}
 							}
 						}
 					}
 				}
 				else if( parcelQueue.isEmpty() == false || unboundedParcelKey != null ) {
 					
 					// We are expecting a parcel.
 					
 					// Which one?
 					
 					String which = parcelQueue.isEmpty() ? unboundedParcelKey : parcelQueue.poll();
 					
 					if( which == null ) {
 						
 						_parseError = true;
 						_parseErrorCode = kErrorCodeInternal;
 						_parseErrorMsg = "Internal Error: An expected non-argument is null.";
 						return;
 					}
 					
 					// Set the current unboundedParcelKey
 					
 					if( argSet.makeGet( which ).isUnbounded ) unboundedParcelKey = which;
 					
 					// Add this parcel to the correct bin in argSet.
 					
 					if( ! argSet.foundParcel( which, arg ) ) {
 						
 						_parseError = true;
 						_parseErrorCode = kErrorCodeInternal;
 						_parseErrorMsg = "Internal Error: Unknown expected parcel: '" + which + "'";
 						return;
 					}
 				}
 				else {
 					
 					// This is an unexpected parcel.
 					
 					_parseError = true;
 					_parseErrorCode = kErrorCodeUnknownArgument;
 					_parseErrorMsg = "Unexpected argument: '" + arg + "'";
 					return;
 				}
 			}
 		}
 		
 		// The queue had better be empty at this point.
 		
 		if( ! parcelQueue.isEmpty() ) {
 			
 			_parseError = true;
 			_parseErrorCode = kErrorCodeMissingArgument;
 			_parseErrorMsg = "Expected argument: '" + parcelQueue.peek() + "'";
 			return;
 		}
 	}
 	
 	// Other functions and resources
 	
 	public String [] argsUsage() {
 		
 		// This function dynamically builds a textual version of
 		// the arguments currently loaded in this class instance.
 		
 		return argSet.argsUsage();
 	}
 	
 	public String toString() {
 		
 		// Standard toString.
 		
 		return argSet.toString();
 	}
 	
 	protected class CLArgumentSet {
 		
 		// This class contains a set of arguments, and provides
 		// some convenience functions relating to them.
 		
 		private Map<String,CLArg> argSet = new Hashtable<String,CLArg>();
 		
 		public class CLArg {
 			
 			// This class contains a single argument, and provides
 			// some convenience functions relating to it.
 			
 			// Properties for storing argument specifications
 			
 			public ArrayList<Character> flags = new ArrayList<Character>();
 			public ArrayList<String> switches = new ArrayList<String>();
 			public int parcelCount = 0;
 			public boolean isArray = false;
 			public boolean isUnbounded = false;
 			public String displayName = null;
 			
 			// Properties for storing argument instances from an Args array
 			
 			public ArrayList<String> parcels = new ArrayList<String>();
 			
 			// Convenience functions
 			
 			public String getMostRelevantParcel() {
 				
 				// Return the last non-null parcel.
 				
 				for( int row = parcels.size() -1 ; row >= 0 ; row-- )
 					if( parcels.get( row ) != null )
 						return parcels.get( row );
 				
 				return null;
 			}
 			
 			public String [] getRelevantParcels() {
 				
 				// Returns an array of the parcels that are relevant
 				// based on the isArray and parcelCount properties.
 				
 				String [] result;
 				
 				if( isArray ) {
 					
 					// Return all the parcels, minus the null slots.
 					
 					result = parcels.toArray( new String [0] );
 				}
 				else {
 					
 					// Return only the last <parcelCount> non-null slots.
 					
 					result = new String [ parcelCount ];
 					
 					int put = parcelCount -1;
 					for( int look = parcels.size() -1 ; look >= 0 ; look-- ) {
 						
 						if( parcels.get( look ) != null ) {
 							
 							result[ put ] = parcels.get( look );
 							
 							if( put == 0 ) return result;
 							
 							put--;
 						}
 					}
 					
 					for( put = put ; put >= 0 ; put-- )
 						result[ put ] = null;
 				}
 				
 				return prune( result );
 			}
 			
 			private String [] prune( String [] orig ) {
 				
 				// Returns a copy of the given array without any null cells.
 				
 				// First, figure out how many values we are keeping.
 				
 				int count = 0;
 				
 				for( String s : orig )
 					if( s != null )
 						count++;
 				
 				// Now, copy the valid values into the array.
 				
 				String [] result = new String [ count ];
 				
 				int row = 0;
 				
 				for( String s : orig )
 					if( s != null )
 						result[ ++row ] = s;
 				
 				return result;
 			}
 			
 			public String argsUsage() {
 				
 				// Returns a dynamically built string
 				// describing the usage of this argument.
 				
 				StringBuilder result = new StringBuilder();
 				
 				if( ! flags.isEmpty() ) {
 					
 					result.append( "-" );
 					
 					int size = flags.size();
 					for( int row = 0 ; row < size ; row++ )
 						result.append( flags.get( row ) );
 				}
 				if( ! switches.isEmpty() ) {
 					
 					int size = switches.size();
 					for( int row = 0 ; row < size ; row++ ) {
 						
 						if( result.length() > 0 )
 							result.append( ", " );
 						
 						result.append( "--" + switches.get( row ) );
 					}
 				}
 				
 				return result.toString() + " : " + displayName;
 			}
 			
 			public String toString() {
 				
 				// Standard toString.
 				
 				if( parcels.isEmpty() ) return displayName + ": [empty]";
 				
 				StringBuilder result = new StringBuilder();
 				
 				result.append( displayName + ": '" + parcels.get( 0 ) + "'" );
 				
 				int count = parcels.size();
 				
 				for( int row = 1 ; row < count ; row ++ )
 					
 					result.append( ", '" + parcels.get( row ) + "'" );
 				
 				return result.toString();
 			}
 		}
 		
 		public CLArg makeGet( String id ) {
 			
 			// A convenience function that creates if necessary
 			// and returns the CLArg in argSet at the given key.
 			
 			CLArg node = argSet.get( id );
 			
 			if( node == null ) {
 				
 				node = new CLArg();
 				argSet.put( id, node );
 			}
 			
 			return node;
 		}
 		
 		public void addArgumentFlag( String id, char flag ) {
 			
 			// A mutator method that adds the given information into the given argument.
 			
 			CLArg node = makeGet( id );
 			
 			if( node.flags.indexOf( flag ) < 0 )
 				node.flags.add( flag );
 		}
 		
 		public void addArgumentSwitch( String id, String sswitch ) {
 			
 			// A mutator method that adds the given information into the given argument.
 			
 			CLArg node = makeGet( id );
 			
 			if( node.switches.indexOf( sswitch ) < 0 )
 				node.switches.add( sswitch );
 		}
 		
 		public void setArgumentParcelCount( String id, int parcelCount ) {
 			
 			// A mutator method that adds the given information into the given argument.
 			
 			makeGet( id ).parcelCount = parcelCount;
 		}
 		
 		public void setArgumentIsArray( String id, boolean isArray ) {
 			
 			// A mutator method that adds the given information into the given argument.
 			
 			makeGet( id ).isArray = isArray;
 		}
 		
 		public void setArgumentIsUnbounded( String id, boolean isUnbounded ) {
 			
 			// A mutator method that adds the given information into the given argument.
 			
 			makeGet( id ).isUnbounded = isUnbounded;
 		}
 		
 		public void setArgumentDisplayName( String id, String displayName ) {
 			
 			// A mutator method that adds the given information into the given argument.
 			
 			makeGet( id ).displayName = displayName;
 		}
 		
 		public void addArgumentInfo( String id, char flag, String sswitch, int parcelCount, boolean isArray, boolean isUnbounded, String displayName ) {
 			
 			// A mutator function that adds a bunch of fields to the given argument at once.
 			
 			addArgumentFlag( id, flag );
 			addArgumentSwitch( id, sswitch );
 			setArgumentParcelCount( id, parcelCount );
 			setArgumentIsArray( id, isArray );
 			setArgumentIsUnbounded( id, isUnbounded );
 			setArgumentDisplayName( id, displayName );
 		}
 		
 		public void addArgumentInfo( String id, char flag, String sswitch, int parcelCount, boolean isArray, String displayName ) {
 			
 			// A mutator function that adds a bunch of fields to the given argument at once.
 			
 			addArgumentFlag( id, flag );
 			addArgumentSwitch( id, sswitch );
 			setArgumentParcelCount( id, parcelCount );
 			setArgumentIsArray( id, isArray );
 			setArgumentDisplayName( id, displayName );
 		}
 		
 		public void addArgumentInfo( String id, String sswitch, int parcelCount, boolean isArray, String displayName ) {
 			
 			// A mutator function that adds a bunch of fields to the given argument at once.
 			
 			addArgumentSwitch( id, sswitch );
 			setArgumentParcelCount( id, parcelCount );
 			setArgumentIsArray( id, isArray );
 			setArgumentDisplayName( id, displayName );
 		}
 		
 		public void addArgumentInfo( String id, char flag, int parcelCount, boolean isArray, String displayName ) {
 			
 			// A mutator function that adds a bunch of fields to the given argument at once.
 			
 			addArgumentFlag( id, flag );
 			setArgumentParcelCount( id, parcelCount );
 			setArgumentIsArray( id, isArray );
 			setArgumentDisplayName( id, displayName );
 		}
 		
 		public String getParcelFromKey( String id ) {
 			
 			// An accessor function that returns the most relevant parcel in the given key.
 			
 			return makeGet( id ).getMostRelevantParcel();
 		}
 		
 		public String [] getParcelArrayFromKey( String id ) {
 			
 			// An accessor function that returns an array of
 			// the most relevant parcels in the given argument.
 			
 			return makeGet( id ).getRelevantParcels();
 		}
 		
 		public int getParcelCountForKey( String id ) {
 			
 			// An accessor function that returns the number of found parcels,
 			// including the null ones, that are in the given argument.
 			
 			return getParcelCountForKey( id, false );
 		}
 		
 		public int getParcelCountForKey( String id, boolean prune ) {
 			
 			// An accessor function that returns the number of
 			// found parcels, optionally including the null
 			// ones or not, that are in the given argument.
 			
 			if( prune )
 				
 				return getParcelArrayFromKey( id ).length;
 			
 			else
 				
 				return makeGet( id ).parcels.size();
 		}
 		
 		public void removeArgument( String id ) {
 			
 			// Removes the given argument from this set.
 			
 			argSet.remove( id );
 		}
 		
 		public void clearArgumentSet() {
 			
 			// Removes all arguments from this set.
 			
 			argSet.clear();
 		}
 		
 		public String [] argsUsage() {
 			
 			// Generates and returns a String array containing
 			// the usage of each argument in this set.
 			
 			CLArg [] tmp = argSet.values().toArray( new CLArg [0] );
 			int size = tmp.length;
 			String [] result = new String [ size ];
 			
 			for( int row = 0 ; row < size ; row++ )
 				
 				result[ row ] = tmp[ row ].argsUsage();
 			
 			return result;
 		}
 		
 		public String getArgKeyForFlag( char flag ) {
 			
 			// Returns the ID of an argument that is
 			// set to be triggered by the given flag.
 			
 			// If you have duplicate flags, then this
 			// function won't crash... but only one of
 			// the arguments will receive that parcel.
 			
 			Iterator<String> curKey = argSet.keySet().iterator();
 			
 			while( curKey.hasNext() ) {
 				
 				String key = curKey.next();
 				
 				CLArg node = argSet.get( key );
 				
 				if( node != null )
 					if( node.flags.contains( flag ) )
 						return key;
 			}
 			
 			return null;
 		}
 		
 		public String getArgKeyForSwitch( String sswitch ) {
 			
 			// Returns the ID of an argument that is
 			// set to be triggered by the given switch.
 			
 			// If you have duplicate switches, then this
 			// function won't crash... but only one of
 			// the arguments will receive that parcel.
 			
 			Iterator<String> curKey = argSet.keySet().iterator();
 			
 			while( curKey.hasNext() ) {
 				
 				String key = curKey.next();
 				
 				CLArg node = argSet.get( key );
 				
 				if( node != null )
 					if( node.switches.contains( sswitch ) )
 						return key;
 			}
 			
 			return null;
 		}
 		
 		public void addRequiredParcelsToQueue( Queue<String> parcelQueue, String key ) {
 			
 			// This function adds the parcels defined by the given
 			// argument to the given queue.  Used when you have
 			// found an argument in an argument array.
 			
 			CLArg node = makeGet( key );
 			
 			for( int count = node.parcelCount ; count > 0 ; count-- )
 				
 				parcelQueue.add( key );
 		}
 		
 		public boolean foundParcel( String key, String parcel ) {
 			
 			// Adds the given parcel to the given argument.  Used
 			// when you have found a parcel and are expecting it
 			// to match up to an argument.
 			
 			makeGet( key ).parcels.add( parcel );
 			
 			return true;
 		}
 		
 		public String toString() {
 			
 			// Standard toString.
 			
 			Object [] tmp = argSet.values().toArray();
 			int count = tmp.length;
 			
 			if( count == 0 ) return "";
 			
 			StringBuilder result = new StringBuilder();
 			
 			result.append( tmp[ 0 ] );
 			
 			for( int row = 1 ; row < count ; row++ )
 				
 				result.append( "\n" + tmp[ row ] );
 			
 			result.append( "\nSequence Error: " + _parseError );
 			result.append( "\n    Error Code: " + _parseErrorCode );
 			result.append( "\n Error Message: " + _parseErrorMsg );
 			
 			return result.toString();
 		}
 	}
 }
