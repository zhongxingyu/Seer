 package net.praqma.clearcase.objects;
 
 import net.praqma.clearcase.cleartool.Cleartool;
 import net.praqma.debug.Debug;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * 
  * @author wolfgang
  *
  */
 public class Baseline extends ClearBase
 {
 	private String fqobj                  = null;
 	private String user                   = null;
 	private Component component           = null;
 	private ArrayList<Baseline> depends_on_closure = null;
 	private Plevel plevel                 = null;
 	private String shortname              = null;
 	private Stream stream                 = null;
 	private String pvob                   = null;
 	
 	private ArrayList<Activity> activities = null;
 	
 	private boolean build_in_progess      = false;
 	private String diffs                  = "";
 	
	private HashMap<String, Tag> tags = null;
	
 	/**
 	 * 
 	 * @param fqobj
 	 * @param trusted
 	 */
 	private Baseline( String fqobj, boolean trusted )
 	{
 		logger.trace_function();
 		logger.debug( "BASELINE2" );
 		
 		
 		String[] res = TestComponent( fqobj );
 		
 		/*  $fqobj = 'baseline:'.$fqobj;
     	 *	$fqobj =~ s/baseline:baseline:/baseline:/;
     	 * Prefix the object with baseline:
          */
 		if( !fqobj.startsWith( "baseline:" ) )
 		{
 			fqobj = "baseline:" + fqobj;
 		}
 		
 		this.fqobj   = fqobj;
 		this.fqname  = fqobj;
 		
 		this.pvob  = res[1];
 		
 		if( !trusted )
 		{
 			Load();
 		}
 	}
 	
 	/* The overridden "factory" method for creating Clearcase objects */
 	public static Baseline GetObject( String fqname, boolean trusted )
 	{
 		logger.trace_function();
 		
 		logger.log( "Retrieving Baseline " + fqname );
 		
 		if( objects.containsKey( fqname ) )
 		{
 			return (Baseline)objects.get( fqname );
 		}
 		
 		logger.log( "Creating the Baseline " + fqname );
 		Baseline obj = new Baseline( fqname, trusted );
 		objects.put( fqname, obj );
 		
 		return obj;
 	}
 	
 	/**
 	 * Loads the member variables from clear case
 	 */
 	public void Load()
 	{
 		logger.trace_function();
 		
 		logger.log( "Loading baseline " + this.fqname );
 		
 		//String cmd = "desc -fmt %n" + delim + "%[component]p" + delim + "%[bl_stream]p" + delim + "%[plevel]p" + delim + "%u " + fqobj;
 		//String result = Cleartool.run( cmd );
 		String result = CTF.LoadBaseline( this.fqname );
 		
 		//my ($shortname, $component, $stream, $plevel, $user) = split /$delim/, $retval;
 		String[] rs = result.split( delim );
 		
 		String c = rs[1].matches( ".*@\\\\.*$" ) ? rs[1] : rs[1] + "@" + this.pvob;
 		String s = rs[2].matches( ".*@\\\\.*$" ) ? rs[2] : rs[2] + "@" + this.pvob;
 		
 		logger.debug( "c="+c + "("+rs[1]+")" );
 		logger.debug( "s="+s + "("+rs[2]+")" );
 		
		this.tags = new HashMap<String, Tag>();
		
 		this.shortname = rs[0];
 		//this.component = new Component( c, true );
 		//this.stream    = new Stream( s, true );
 		/* Now with factory creation! */
 		this.component = Component.GetObject( c, true );
 		this.stream    = Stream.GetObject( s, true );
 		this.plevel    = GetPlevelFromString( rs[3] );
 		this.user      = rs[4];
 		
 		this.loaded = true;
 		
 	}
 	
	public String GetHlinkString()
	{
		return "";
	}

	
 	/**
 	 * CHW: This function is not implemented in the Perl code!!!
 	 * @return
 	 */
 	public boolean QueuedForTest( int queue_level )
 	{
 		return false;
 	}
 	
 	
 	/**
 	 * 
 	 * @return The Baselines user
 	 */
 	public String GetUser()
 	{
 		logger.trace_function();
 		
 		if( user == null )
 		{
 			Load();
 		}
 		
 		return user;
 	}
 	
 	/**
 	 * 
 	 * @return The Baselines Component
 	 */
 	public Component GetComponent()
 	{
 		logger.trace_function();
 		
 		if( component == null )
 		{
 			Load();
 		}
 		
 		return component;
 	}
 	
 	/**
 	 * 
 	 * @return The Baselines Stream
 	 */
 	public Stream GetStream()
 	{
 		logger.trace_function();
 		
 		if( stream == null )
 		{
 			Load();
 		}
 		
 		return stream;
 	}
 	
 	/**
 	 * 
 	 * @return ArrayList<Baseline>
 	 */
 	public ArrayList<Baseline> GetDependencies()
 	{
 		logger.trace_function();
 		
 		if( this.depends_on_closure == null )
 		{
 			return this.depends_on_closure;
 		}
 		
 		// cleartool('desc -fmt %[depends_on_closure]p '.$self->{'fqobj'});
 		String cmd = "desc -fmt %[depends_on_closure]p " + fqobj;
 		String result = Cleartool.run( cmd );
 		
 		this.depends_on_closure = new ArrayList<Baseline>();
 		
 		/* White space separated list */
 		String[] rs = result.split( " " );
 		
 		for( int i = 0 ; i < rs.length ; i++ )
 		{
 			/* Remove empty matches */
 			if( rs[i].matches( "/s+" ) )
 			{
 				continue;
 			}
 			
 			String baseln = rs[i].trim();
 			//Baseline baseline = new Baseline( baseln, true );
 			Baseline baseline = Baseline.GetObject( baseln, true );
 			
 			this.depends_on_closure.add( baseline );
 		}
 		
 		return this.depends_on_closure;
 	}
 	
 	/**
 	 * Get the next Plevel for the Baseline
 	 * CHW: This is not same as the Perl code! See declaration of Plevel
 	 * @return Plevel
 	 */
 	public Plevel GetNextPlevel()
 	{
 		logger.trace_function();
 		
 		for( int i = 0 ; i < Plevel.values().length ; i++ )
 		{
 			if( this.plevel.equals( Plevel.values()[i] ) )
 			{
 				try
 				{
 					this.plevel = Plevel.values()[i+1];
 				}
 				catch( Exception e )
 				{
 					/* Do nothing.. */
 				}
 				
 				break;
 			}
 		}
 		
 		return this.plevel;
 		
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String GetShortname()
 	{
 		logger.trace_function();
 		
 		if( shortname == null )
 		{
 			Load();
 		}
 		
 		return shortname;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Plevel GetPlevel()
 	{
 		logger.trace_function();
 
 		return this.plevel;
 	}
 	
 
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean BuildInProgess()
 	{
 		logger.trace_function();
 		
 		// cleartool('desc -fmt %['.ATTR_BUILD_IN_PROGRESS.']NSa '.$self->get_fqname());
 		//String cmd = "desc -fmt %[" + ATTR_BUILD_IN_PROGRESS + "]NSa " + this.GetFQName();
 		//String result = Cleartool.run( cmd );
 		
 		//return ( result.matches( BUILD_IN_PROGRESS_ENUM_TRUE ) ) ? true : false;
 		
 		return CTF.BuildInProgess( this.fqname );
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int MarkBuildInProgess()
 	{
 		logger.trace_function();
 		
 		if( !build_in_progess )
 		{
 			// cleartool('mkattr -default '.ATTR_BUILD_IN_PROGRESS.' '.$self->get_fqname());
 			//String cmd = "mkattr -default " + ATTR_BUILD_IN_PROGRESS + " " + this.GetFQName();
 			//Cleartool.run( cmd );
 			CTF.BaselineMakeAttribute( this.fqname, ATTR_BUILD_IN_PROGRESS );
 			/* ASK This is not in the Perl code */
 			this.build_in_progess = true;
 		}
 		
 		return 1;
 	}
 	
 	/* Not verified */
 	public String GetComponentName()
 	{
 		return this.component.GetName();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int UnMarkBuildInProgess()
 	{
 		logger.trace_function();
 		
 		if( build_in_progess )
 		{
 			// cleartool('mkattr -default '.ATTR_BUILD_IN_PROGRESS.' '.$self->get_fqname());
 			//String cmd = "rmattr -default " + ATTR_BUILD_IN_PROGRESS + " " + this.GetFQName();
 			//Cleartool.run( cmd );
 			CTF.BaselineRemoveAttribute( this.fqname, ATTR_BUILD_IN_PROGRESS );
 			/* ASK This is not in the Perl code */
 			this.build_in_progess = false;
 		}
 		
 		return 1;
 	}
 	
 	/* CHW: NOTICE! Inner for loop does not comply with baseline.pm 
 	 * The return type is NOT determined yet!!!
 	 * ASK Lars about inner for loop:
 	 * foreach my $dep_baseln (@bls, $baseln->get_dependencies()){
 	 * 		$exp_bls{$dep_baseln->get_componentname()} = $dep_baseln;
 	 * }
 	 * 
 	 * Is it to make the returning list unique? On components?
 	 * */
 	public static ArrayList<Baseline> StaticExpandBls( ArrayList<Baseline> bls )
 	{
 		logger.trace_function();
 		
 		HashMap<String, Baseline> exp_bls = new HashMap<String, Baseline>();
 
 		for( int i = 0 ; i < bls.size() ; i++ )
 		{
 			System.out.println( "Baseline:\t" + bls.get( i ) );
 		
 			exp_bls.put( bls.get( i ).GetComponentName(), bls.get( i ) );
 			
 			/* Inserting the dependent baselines as well?! */
 			ArrayList<Baseline> deps = bls.get( i ).GetDependencies();
 			for( int j = 0 ; j < deps.size() ; j++ )
 			{
 				exp_bls.put( deps.get( j ).GetComponentName(), deps.get( j ) );
 			}
 		}
 		
 		SortedSet<String> sortedset = new TreeSet<String>( exp_bls.keySet() );
 		Iterator<String> it = sortedset.iterator();
 		
 		/* UNTESTED sorting. Should sort on values(baselines) */
 		logger.log( "UNTESTED sorting. Should sort on values(baselines)", "experimental" );
 		ArrayList<Baseline> rbls = new ArrayList<Baseline>();
 	    while ( it.hasNext() )
 	    {
 	        rbls.add( exp_bls.get( it.next() ) );
 	    }
 
 	    return rbls;
 	}
 	
 	/**
 	 * ASK Lars if there's any need for checking the plevel!
 	 * @param plevel
 	 * @return
 	 */
 	public int SetPromotionLevel( Plevel plevel )
 	{
 		logger.trace_function();
 		
 		//cleartool('chbl -level '.$plevel.' '.$self->get_fqname() )
 		//String cmd = "chbl -level " + plevel.GetName() + " " + this.GetFQName();
 		//Cleartool.run( cmd );
 		CTF.SetPromotionLevel( this.fqname, plevel.GetName() );
 		this.plevel = plevel;
 		
 		return 1;
 	}
 	
 	/**
 	 * This method promotes a baseline.
 	 * If a baseline was rejected, it SHOULD be promoted to BUILT.
 	 * TODO Ask Lars if itsn't wrong to promote from rejected to released!?
 	 * @return
 	 */
 	public Plevel Promote( )
 	{
 		switch( this.plevel )
 		{
 		case INITIAL:
 			this.plevel = Plevel.BUILT;
 			break;
 		case BUILT:
 			this.plevel = Plevel.TESTED;
 			break;
 		case TESTED:
 			this.plevel = Plevel.RELEASED;
 			break;
 		case RELEASED:
 			this.plevel = Plevel.RELEASED;
 			break;
 		default:
 			this.plevel = Plevel.BUILT;
 		}
 		
		CTF.SetPromotionLevel( this.fqname, this.plevel.GetName() );
		
 		return this.plevel;
 	}
 	
 	public void Demote()
 	{
 		this.plevel = Plevel.REJECTED;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Plevel GetPromotionLevel(  )
 	{
 		logger.trace_function();
 		
 		//cleartool('desc -fmt %[plevel]p '.$self->get_fqname()
 		//String cmd = "desc -fmt %[plevel]p " + this.GetFQName();
 		//String result = Cleartool.run( cmd );
 		String result = CTF.GetPromotionLevel( this.fqname );
 		logger.debug( "RESULT=" + result );
 				
 		return GetPlevelFromString( result );
 	}
 	
 	
 	/**
 	 * Get the baselines activities
 	 * @return An ArrayList of Activity
 	 */
 	public ArrayList<Activity> GetActivities()
 	{
 		logger.trace_function();
 		
 		if( this.activities != null )
 		{
 			return this.activities;
 		}
 		
 		this.activities = new ArrayList<Activity>();
 		
 		ArrayList<String> acts = CTF.GetBaselineActivities( this.fqname );
 		
 		for( String s : acts )
 		{
 			this.activities.add( Activity.GetObject( s, true ) );
 		}
 		
 		return this.activities;
 	}
 	
 	/* Public GettDiffs overloads */
 	
 	public ArrayList<Version> GetDiffs( String format, boolean nmerge, String viewroot )
 	{
 		/* Argument correcting */
 		viewroot = viewroot.length() == 0 ? null : viewroot;
 		return _GetDiffs( format, nmerge, viewroot );
 	}
 	
 	public ArrayList<Version> GetDiffs( String format, boolean nmerge )
 	{
 		return _GetDiffs( format, nmerge, null );
 	}
 	
 	private ArrayList<Version> _GetDiffs( String format, boolean nmerge, String viewroot )
 	{
 		logger.trace_function();
 		
 		/* Argument correcting */
 		format   = format.equals( "list" ) || format.equals( "scalar" ) ? format : "list";
 		if( format.equalsIgnoreCase( "scalar" ) )
 		{
 			logger.warning( "Scalar not supported yet!" );
 			return null;
 		}
 		
 		String sw_nmerge = ( nmerge ? " -nmerge " : "" );
 		
 		// cleartool('diffbl -pre -act -ver '.$sw_nmerge.$self->get_fqname );
 		//String cmd = "diffbl -pre -act -ver " + sw_nmerge + this.GetFQName();
 		//this.diffs = Cleartool.run( cmd );
 		//this.diffs = CTF.diffbl( sw_nmerge, this.GetFQName() ).trim();
 		ArrayList<String> diffs = CTF.GetBaselineDiffsNmergePrevious( this.fqname );
 		
 		//logger.debug( "DIFFS=\"" + this.diffs + "\"" );
 		
 		String msg = this.diffs;
 		
 		/* Remove the viewroot from the path */
 		if( viewroot != null )
 		{
 			for( int i = 0 ; i < diffs.size() ; i++ )
 			{
 				diffs.set( i, diffs.get( i ).replaceFirst( java.util.regex.Pattern.quote( viewroot ), "" ) );
 			}
 		}
 		
 		ArrayList<Version> list = new ArrayList<Version>();
 		
 		/* Make versions */
 		for( String s : diffs )
 		{
 			list.add( Version.GetObject( s, true ) );
 		}
 		list = Version.GetUnique( list );
 
 
 		
 		return list;
 		//return null;
 	}
 	
 	/**
 	 * 
 	 * @param format
 	 * @param nmerge
 	 * @param viewroot
 	 * @return
 	 */
 	private ArrayList<String> _GetDiffsOLD( String format, boolean nmerge, String viewroot )
 	{
 		logger.trace_function();
 		
 		/* Argument correcting */
 		format   = format.equals( "list" ) || format.equals( "scalar" ) ? format : "list";
 		
 		String sw_nmerge = ( nmerge ? " -nmerge " : "" );
 		
 		// cleartool('diffbl -pre -act -ver '.$sw_nmerge.$self->get_fqname );
 		//String cmd = "diffbl -pre -act -ver " + sw_nmerge + this.GetFQName();
 		//this.diffs = Cleartool.run( cmd );
 		this.diffs = CTF.diffbl( sw_nmerge, this.GetFQName() ).trim();
 		
 		//logger.debug( "DIFFS=\"" + this.diffs + "\"" );
 		
 		String msg = this.diffs;
 		
 		if( viewroot != null )
 		{
 			msg = msg.replaceAll( java.util.regex.Pattern.quote( viewroot ), "" );
 		}
 		
 		ArrayList<String> list = new ArrayList<String>();
 		
 		logger.log( "Format = " + format );
 		
 		if( format.equals( "list" ) )
 		{
 			msg = msg.replaceAll( "(?m)^>>.*$", "" );
 			msg = msg.replaceAll( "(?m)\\@\\@.*$", "" );
 			msg = msg.replaceAll( "(?m)^\\s+", "" );
 
 			String[] groslist = msg.split( "\n" );
 			
 			/* Also removes duplicate files. */
 			HashMap<String, String> hash = new HashMap<String, String>();
 			for( int i = 0 ; i < groslist.length ; i++ )
 			{
 				hash.put( groslist[i], "" );
 			}
 
 			/* CHW: Experimental sorting. TESTED! */
 			logger.log( "Experimental sorting. TESTED!", "experimental" );
 			SortedSet<String> sortedset = new TreeSet<String>( hash.keySet() );
 			Iterator<String> it = sortedset.iterator();
 			
 		    while ( it.hasNext() )
 		    {
 		       list.add( it.next() );
 		    }
 		}
 		
 		if( format.equals( "scalar" ) )
 		{
 			list.add( msg );
 		}
 		
 		return list;
 		
 	}
 	
 	
 	/**
 	 * Pretty printing of the Baseline object.
 	 */
 	public String Stringify()
 	{
 		logger.trace_function();
 		
 		if( this.shortname == null )
 		{
 			this.Load();
 		}
 		
 		StringBuffer tostr = new StringBuffer();
 		tostr.append( "fqobj: " + this.fqobj + linesep );
 		tostr.append( "user: " + this.user + linesep );
 		tostr.append( "component: " + this.component.toString() + linesep );
 		tostr.append( "depends_on_closure: " + ( depends_on_closure != null ? depends_on_closure.size() : "None" ) + linesep );
 		if( depends_on_closure != null )
 		{
 			/* Let's hope there's no circular dependencies!!! */
 			for( int i = 0 ; i < depends_on_closure.size() ; i++ )
 			{
 				tostr.append( "["+i+"] " + depends_on_closure.get( i ).toString() + linesep );
 			}
 		}
 		tostr.append( "plevel: " + this.plevel.GetName() + "(" + plevel.ordinal() + ")" + linesep );
 		tostr.append( "shortname: " + this.shortname + linesep );
 		tostr.append( "stream: " + this.stream.toString() + linesep );
 		tostr.append( "pvob: " + this.pvob.toString() + linesep );
 		
 		tostr.append( "build_in_progess: " + this.build_in_progess + linesep );
 		//tostr.append( "diffs: " + this.diffs.toString() + linesep );
 
 
 		return tostr.toString();		
 	}
 	
 	public String toString()
 	{
 		return this.fqname;
 	}
 	
 }
