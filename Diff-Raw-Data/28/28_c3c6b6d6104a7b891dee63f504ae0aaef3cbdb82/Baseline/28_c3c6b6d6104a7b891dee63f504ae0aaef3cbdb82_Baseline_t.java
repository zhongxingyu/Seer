 package net.praqma;
 
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
 class Baseline extends ClearBase
 {
 	private String fqobj                  = null;
 	private String user                   = null;
 	private Component component           = null;
 	private ArrayList<Baseline> depends_on_closure = null;
 	private Plevel plevel                 = null;
 	private String shortname              = null;
 	private Stream stream                 = null;
 	private String pvob                   = null;
 	
 	private boolean build_in_progess      = false;
 	private String diffs                  = "";
 	
 	
 	/**
 	 * 
 	 * @param fqobj
 	 * @param trusted
 	 */
 	public Baseline( String fqobj, boolean trusted )
 	{
 		logger.trace_function();
 		
 		
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
 	
 	/**
 	 * Loads the member variables from clear case
 	 */
 	public void Load()
 	{
 		logger.trace_function();
 		
 		String cmd = "desc -fmt %n" + delim + "%[component]p" + delim + "%[bl_stream]p" + delim + "%[plevel]p" + delim + "%u " + fqobj;
 		String result = Cleartool.run( cmd );
 		
 		//my ($shortname, $component, $stream, $plevel, $user) = split /$delim/, $retval;
 		String[] rs = result.split( delim );
 		this.shortname = rs[0];
 		this.component = new Component( rs[1] + "@" + this.pvob, true );
 		this.stream    = new Stream( rs[2] + "@" + this.pvob, true );
 		this.plevel    = GetPlevelFromString( rs[3] );
 		this.user      = rs[4];
 		
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
 			Baseline baseline = new Baseline( baseln, true );
 			
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
 	public String GetPlevel()
 	{
 		logger.trace_function();
 
 		// cleartool('desc -fmt %[plevel]p '.$self->get_fqname())
 		String cmd = "desc -fmt %[plevel]p " + this.GetFQName();
 		return Cleartool.run( cmd );
 	}
 	
 
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean BuildInProgess()
 	{
 		logger.trace_function();
 		
 		// cleartool('desc -fmt %['.ATTR_BUILD_IN_PROGRESS.']NSa '.$self->get_fqname());
 		String cmd = "desc -fmt %['.ATTR_BUILD_IN_PROGRESS.']NSa " + this.GetFQName();
 		String result = Cleartool.run( cmd );
 		
 		return ( result.matches( BUILD_IN_PROGRESS_ENUM_TRUE ) ) ? true : false;
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
 			String cmd = "mkattr -default " + ATTR_BUILD_IN_PROGRESS + " " + this.GetFQName();
 			Cleartool.run( cmd );
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
 			String cmd = "rmattr -default " + ATTR_BUILD_IN_PROGRESS + " " + this.GetFQName();
 			Cleartool.run( cmd );
 		}
 		
 		return 1;
 	}
 	
 	/* CHW: NOTICE! Inner for loop does not comply with baseline.pm 
 	 * The return type is NOT determined yet!!!
 	 * */
 	public static ArrayList<Baseline> StaticExpandBls( ArrayList<Baseline> bls )
 	{
 		logger.trace_function();
 		
 		HashMap<String, Baseline> exp_bls = new HashMap<String, Baseline>();
 
 		for( int i = 0 ; i < bls.size() ; i++ )
 		{
 			System.out.println( "Baseline:\t" + bls.get( i ) );
 			
 			exp_bls.put( bls.get( i ).GetComponentName(), bls.get( i ) );
 			
 			for( int j = 0 ; j < exp_bls.size() ; j++ )
 			{
 				exp_bls.put( exp_bls.get( j ).GetComponentName(), exp_bls.get( j ) );
 			}
 		}
 		
 		SortedSet<String> sortedset = new TreeSet<String>( exp_bls.keySet() );
 		Iterator<String> it = sortedset.iterator();
 		
 		/* May change */
 		ArrayList<Baseline> rbls = new ArrayList<Baseline>();
 	    while ( it.hasNext() )
 	    {
 	        rbls.add( exp_bls.get( it.next() ) );
 	    }
 
 	    return rbls;
 	}
 	
 	/**
 	 * 
 	 * @param plevel
 	 * @return
 	 */
 	public int SetPromotionLevel( Plevel plevel )
 	{
 		logger.trace_function();
 		
 		//cleartool('chbl -level '.$plevel.' '.$self->get_fqname() )
 		String cms = "chbl -level " + plevel.GetName() + " " + this.GetFQName();
 		
 		return 1;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Plevel GetPromotionLevel(  )
 	{
 		logger.trace_function();
 		
 		//cleartool('desc -fmt %[plevel]p '.$self->get_fqname()
 		String cmd = "desc -fmt %[plevel]p " + this.GetFQName();
 		String result = Cleartool.run( cmd );
 				
 		return GetPlevelFromString( result );
 	}
 	
 	/* Waiting for Lars. */
 	public void GetActivities()
 	{
 		logger.trace_function();
 	}
 	
 	/**
 	 * 
 	 * @param format
 	 * @param nmerge
 	 * @param viewroot
 	 * @return
 	 */
 	public ArrayList<String> GetDiffs( String format, boolean nmerge, String viewroot )
 	{
 		logger.trace_function();
 		
 		String sw_nmerge = ( nmerge ? " -nmerge " : "" );
 		
 		// cleartool('diffbl -pre -act -ver '.$sw_nmerge.$self->get_fqname );
 		//String cmd = "diffbl -pre -act -ver " + sw_nmerge + this.GetFQName();
 		//this.diffs = Cleartool.run( cmd );
 		this.diffs = CF.diffbl( sw_nmerge, this.GetFQName() ).trim();
 		
		//logger.debug( "DIFFS=\"" + this.diffs + "\"" );
 		
 		String msg = this.diffs;
 		
 		if( viewroot != null )
 		{
			/* CHW: Why is this performed?	my $snr = quotemeta($params{viewroot}); $msg =~ s/$snr//g; */
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
 	public String toString()
 	{
 		logger.trace_function();
 		
 		StringBuffer tostr = new StringBuffer();
 		tostr.append( "fqobj: " + this.fqobj );
 		tostr.append( "user: " + this.user );
 		tostr.append( "component: " + this.component.toString() );
 		tostr.append( "depends_on_closure: " + ( depends_on_closure != null ? depends_on_closure.size() : "None" ) );
 		if( depends_on_closure != null )
 		{
 			/* Let's hope there's no circular dependencies!!! */
 			for( int i = 0 ; i < depends_on_closure.size() ; i++ )
 			{
 				tostr.append( "["+i+"] " + depends_on_closure.get( i ).toString() );
 			}
 		}
 		tostr.append( "plevel: " + this.plevel.GetName() );
 		tostr.append( "shortname: " + this.shortname );
 		tostr.append( "stream: " + this.stream.toString() );
 		tostr.append( "pvob: " + this.pvob.toString() );
 		
 		tostr.append( "build_in_progess: " + this.build_in_progess );
 		tostr.append( "diffs: " + this.diffs.toString() );
 		
 		return tostr.toString();		
 	}
 	
 }
