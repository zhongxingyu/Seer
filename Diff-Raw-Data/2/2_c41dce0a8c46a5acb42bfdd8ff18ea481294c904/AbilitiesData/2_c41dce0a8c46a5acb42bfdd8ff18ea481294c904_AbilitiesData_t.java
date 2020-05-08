 package simumatch.datamanager;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import simumatch.common.Action;
 import simumatch.common.Effect;
 import simumatch.common.Operator;
 import simumatch.common.Scope;
 import simumatch.common.Target;
 
 /**
  * A class that stores data about the abilities of the game, as a single map from <tt>Action</tt>s to <tt>Effect</tt>s.
  */
 public final class AbilitiesData {
 	
 	/** Effect comparator */
 	private static final Comparator<Effect> COMPARATOR = new EffectComparator();
 	
 	/** The data collected */
 	private final Map<Action,List<Effect>> data = new HashMap<Action,List<Effect>>();
 	
 	/**
 	 * Reads a file and adds its information to this object.
 	 * 
 	 * @param file
 	 *            The file to read from
 	 * @throws IOException
 	 *             if anything I/O related goes wrong
 	 * @throws NullPointerException
 	 *             if <tt>file</tt> is <tt>null</tt>
 	 */
 	public void loadFile ( File file ) throws IOException {
 		loadFromReader( new FileReader( file ) );
 	}
 	
 	/**
 	 * Reads a URL and adds its information to this object
 	 * 
 	 * @param url
 	 *            The url to read from
 	 * @throws IOException
 	 *             if anything goes wrong
 	 * @throws NullPointerException
 	 *             if <tt>url</tt> is <tt>null</tt>
 	 */
 	public void loadUrl ( URL url ) throws IOException {
 		loadFromReader( new InputStreamReader( url.openStream() ) );
 	}
 	
 	/**
 	 * Reads a reader and adds its information to this object
 	 * 
	 * @param reader
 	 *            The reader to read from
 	 * @throws IOException
 	 *             if anything goes wrong
 	 * @throws NullPointerException
 	 *             if <tt>reader</tt> is <tt>null</tt>
 	 */
 	public void loadFromReader ( Reader reader ) throws IOException {
 		if ( reader == null ) {
 			throw new NullPointerException();
 		}
 		
 		// Use a BufferedReader
 		BufferedReader br;
 		if ( reader instanceof BufferedReader ) {
 			br = (BufferedReader) reader;
 		} else {
 			br = new BufferedReader( reader );
 		}
 		
 		// Read everything
 		boolean eof = false;
 		while ( !eof ) {
 			Action action = readAction( br );
 			if ( action == null ) {
 				eof = true;
 				
 			} else {
 				List<Effect> effects = new ArrayList<Effect>();
 				
 				Effect effect;
 				while ( ( effect = readEffect( br ) ) != null ) {
 					effects.add( effect );
 				}
 				
 				addAction( action, Collections.unmodifiableList( effects ) );
 			}
 		}
 	}
 	
 	/**
 	 * Adds an action to this object. If the action already exists, the effects are replaced.
 	 * 
 	 * @param action
 	 *            The action to add
 	 * @param effects
 	 *            The effect to add
 	 * @throws NullPointerException
 	 *             if <tt>action</tt> or <tt>effects</tt> are <tt>null</tt> or <tt>effects</tt> contains a <tt>null</tt>
 	 */
 	public void addAction ( Action action, List<Effect> effects ) {
 		if ( action == null ) {
 			throw new NullPointerException( "action" );
 		}
 		if ( effects == null || effects.contains( null ) ) {
 			throw new NullPointerException( "effects" );
 		}
 		
 		data.put( action, Collections.unmodifiableList( new ArrayList<Effect>( effects ) ) );
 	}
 	
 	/** @return Retrieve the internal data */
 	public Map<Action,List<Effect>> getData () {
 		return Collections.unmodifiableMap( new HashMap<Action,List<Effect>>( data ) );
 	}
 	
 	/**
 	 * @param action
 	 *            Which action to retrieve effects
 	 * @param sorted
 	 *            Whether to sort the result list
 	 * @return The effects of the action
 	 */
 	public List<Effect> getEffects ( Action action, boolean sorted ) {
 		if ( data.containsKey( action ) ) {
 			if ( sorted ) {
 				List<Effect> effects = new ArrayList<Effect>( data.get( action ) );
 				Collections.sort( effects, COMPARATOR );
 				return Collections.unmodifiableList( effects );
 			} else {
 				return data.get( action );
 			}
 			
 		} else {
 			return Collections.emptyList();
 		}
 	}
 	
 	/**
 	 * @param actions
 	 *            The actions to retrieve effects from
 	 * @param sorted
 	 *            Whether to sort the result list
 	 * @return The effects of the actions
 	 */
 	public List<Effect> getEffects ( Collection<Action> actions, boolean sorted ) {
 		List<Effect> effects = new ArrayList<Effect>( data.size() * 6 );
 		
 		for ( Action action : actions ) {
 			effects.addAll( getEffects( action, false ) );
 		}
 		
 		if ( sorted ) {
 			Collections.sort( effects, COMPARATOR );
 		}
 		
 		return Collections.unmodifiableList( effects );
 	}
 	
 	/**
 	 * @param actions
 	 *            The actions to retrieve effects from and their cardinality
 	 * @param sorted
 	 *            Whether to sort the result list
 	 * @return The effects of the actions
 	 * @throws NullPointerException
 	 *             if <tt>actions</tt> contains a <tt>null</tt> value
 	 * @throws IllegalArgumentException
 	 *             if <tt>action</tt> contains a negative value.
 	 */
 	public List<Effect> getEffects ( Map<Action,? extends Number> actions, boolean sorted ) {
 		List<Effect> effects = new ArrayList<Effect>( data.size() * 6 );
 		
 		// Fill the effect list
 		for ( Map.Entry<Action,? extends Number> entry : actions.entrySet() ) {
 			int times = entry.getValue().intValue();
 			
 			for ( Effect effect : getEffects( entry.getKey(), false ) ) {
 				effects.add( effect.getScaled( times ) );
 			}
 		}
 		
 		// Sort if necessary
 		if ( sorted ) {
 			Collections.sort( effects, COMPARATOR );
 		}
 		return Collections.unmodifiableList( effects );
 	}
 	
 	// --- PRIVATE ---
 	
 	/**
 	 * @param reader
 	 *            The object used to read information
 	 * @return An action read from the <tt>reader</tt>
 	 * @throws IOException
 	 *             If anything goes wrong
 	 */
 	private static Action readAction ( BufferedReader reader ) throws IOException {
 		String line = reader.readLine();
 		
 		// If the line is null, EOF -- return null to signal that
 		if ( line == null ) {
 			return null;
 		}
 		
 		String name = line.trim();
 		Action action = Action.get( name );
 		if ( action == null ) {
 			throw new IOException( "'" + name + "' is not a valid action name" );
 		}
 		
 		return action;
 	}
 	
 	/**
 	 * @param reader
 	 *            The object used to read information
 	 * @return An effect read from the <tt>reader</tt>
 	 * @throws IOException
 	 *             If anything goes wrong
 	 */
 	private static Effect readEffect ( BufferedReader reader ) throws IOException {
 		String line = reader.readLine();
 		
 		// If line is null, the file is over -- return null to signal that
 		if ( line == null ) {
 			return null;
 		}
 		
 		// If line is empty, the list of strings is over -- return null to signal that
 		String effstr = line.trim();
 		if ( ( "" ).equals( effstr ) ) {
 			return null;
 		}
 		
 		// Split the line in parts: <scope> <op&bonus> <target> <perm>
 		String[] parts = effstr.split( "\\s+" );
 		if ( parts.length != 4 ) {
 			throw new IOException( "Invalid effect format" );
 		}
 		
 		// Parse everything -- these methods throw an exception when necessary
 		Scope scope = parseScope( parts[ 0 ] );
 		Operator op = parseOperator( parts[ 1 ].substring( 0, 1 ) );
 		double bonus = parseBonus( parts[ 1 ].substring( 1 ) );
 		Target target = parseTarget( parts[ 2 ] );
 		boolean perm = parsePermanent( parts[ 3 ] );
 		
 		// Return the effect at last
 		return new Effect( scope, target, op, bonus, perm );
 	}
 	
 	/**
 	 * @param string
 	 *            String to parse
 	 * @return A <tt>Scope</tt> object read from <tt>str</tt>
 	 * @throws IOException
 	 *             If the format is invalid
 	 */
 	private static Scope parseScope ( String string ) throws IOException {
 		Scope scope = Scope.get( string );
 		if ( scope == null ) {
 			throw new IOException( "Invalid scope name '" + string + "'" );
 		}
 		
 		return scope;
 	}
 	
 	/**
 	 * @param str
 	 *            String to parse
 	 * @return An <tt>Operator</tt> object read from <tt>str</tt>
 	 * @throws IOException
 	 *             If the format is invalid
 	 */
 	private static Operator parseOperator ( String string ) throws IOException {
 		Operator op = Operator.get( string );
 		if ( op == null ) {
 			throw new IOException( "Invalid operator '" + string + "'" );
 		}
 		
 		return op;
 	}
 	
 	/**
 	 * @param str
 	 *            String to parse
 	 * @return A <tt>Target</tt> object read from <tt>str</tt>
 	 * @throws IOException
 	 *             If the format is invalid
 	 */
 	private static Target parseTarget ( String string ) throws IOException {
 		Target target = Target.get( string );
 		if ( target == null ) {
 			throw new IOException( "Invalid target '" + string + "'" );
 		}
 		
 		return target;
 	}
 	
 	/**
 	 * @param str
 	 *            String to parse
 	 * @return A <tt>boolean</tt> value read from <tt>str</tt>
 	 * @throws IOException
 	 *             If the format is invalid
 	 */
 	private static boolean parsePermanent ( String string ) throws IOException {
 		string = string.toLowerCase();
 		
 		boolean perm;
 		if ( "perm".equals( string ) || "permanent".equals( string ) ) {
 			perm = true;
 			
 		} else if ( "temp".equals( string ) || "temporary".equals( string ) ) {
 			perm = false;
 			
 		} else {
 			throw new IOException( "Invalid permanent declaration '" + string + "'" );
 		}
 		
 		return perm;
 	}
 	
 	/**
 	 * @param str
 	 *            String to parse
 	 * @return A <tt>double</tt> value read from <tt>str</tt>
 	 * @throws IOException
 	 *             If the format is invalid
 	 */
 	private static double parseBonus ( String string ) throws IOException {
 		try {
 			return Double.parseDouble( string );
 		} catch ( NumberFormatException exc ) {
 			throw new IOException( exc );
 		}
 	}
 	
 }
