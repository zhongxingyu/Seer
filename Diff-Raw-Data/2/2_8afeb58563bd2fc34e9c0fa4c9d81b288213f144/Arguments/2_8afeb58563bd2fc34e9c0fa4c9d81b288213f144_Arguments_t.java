 /*
  * Copyright 2011, Erik Lund
  *
  * This file is part of Voxicity.
  *
  *  Voxicity is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Voxicity is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Voxicity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package voxicity;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class Arguments
 {
 	// Map for pairs of string( --option argument )
 	Map< String, String > pairs = new HashMap< String, String >();
 
 	// Map for flags( -abc )
 	Set< Character > flags = new HashSet< Character >();
 
 	// Takes an array of Strings as an argument and constructs
 	// the maps of pairs and flags from it
 	public Arguments( String[] args ) throws Exception
 	{
 		parse_pairs( args );
 		parse_flags( args );
 	}
 
 	// Returns the value of this key in the pairs map
 	public String get_value( String key )
 	{
 		return pairs.get( key );
 	}
 
 	// Returns the value of this key in the pairs map
 	// If that value is null, return the default value
 	public String get_value( String key, String default_value )
 	{
 		String value = get_value( key );
		return ( value == null ? default_value : value );
 	}
 
 	// Return whether or not the flag is present.
 	public boolean get_flag( Character key )
 	{
 		return flags.contains( key );
 	}
 
 	// Return whether or not the flag is present
 	public boolean get_flag( char key )
 	{
 		return get_flag( new Character( key ) );
 	}
 
 	void parse_pairs( String[] args ) throws Exception
 	{
 		for ( int i = 0 ; i < args.length ; i++ )
 		{
 			// Check if the string matches two hyphens
 			// followed by any number of characters and
 			// then check that there is at least one more
 			// string in args.
 			if ( args[i].matches( "--.*" ) && ( i + 1 <= args.length ) )
 			{
 				if ( args[i + 1].matches( "--.*" ) )
 					throw new Exception( "\"" + args[i] + " " + args[i + 1] + "\" is not an option pair." );
 
 				// Put the new argument pair in the map,
 				// overwriting any previous pair with the
 				// same key.
 				pairs.put( args[i].substring( 2 ), args[i + 1] );
 
 				// Skip one string ahead over the pair
 				i++;
 			}
 		}
 	}
 
 	void parse_flags( String[] args ) throws Exception
 	{
 		for ( int i = 0 ; i < args.length ; i++ )
 		{
 			// Check that the string matches the flag option format
 			if ( args[i].matches( "-\\p{Alnum}?" ) )
 			{
 				// Add each flag in the string to the flags set
 				for ( int j = 1 ; j < args[i].length() ; j++ )
 					flags.add( args[i].charAt( j ) );
 			}
 		}
 	}
 }
