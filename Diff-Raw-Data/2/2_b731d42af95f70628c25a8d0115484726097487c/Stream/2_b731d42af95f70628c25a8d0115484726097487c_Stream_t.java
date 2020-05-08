 package net.praqma;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class Stream extends ClearBase
 {
 	private String fqstream                = null;
 	private ArrayList<Baseline> found_bls  = null;
 	private ArrayList<Baseline> rec_bls    = null;
 	private ArrayList<Baseline> latest_bls = null;
 	private String parent                  = null;
 	private String brtype                  = null;
 	private String viewroot                = null;
 	private ArrayList<Activity> activities = null;
 	private String shortname               = null;
 	private String pvob                    = null;	
 	
 	public Stream( String fqstream, boolean trusted )
 	{
 		logger.trace_function();
 		
 		/* Delete the object prefix, if it exists: */
 		if( fqstream.startsWith( "stream:" ) )
 		{
 			logger.debug( "Removing \"stream:\" from name" );
 			fqstream.substring( 0, 7 );
 		}
 		
 		this.fqstream = fqstream;
 		this.fqname   = fqstream;
 		String[] res  = TestComponent( fqstream );
 		
 		this.shortname = res[0];
 		this.pvob      = res[1];
 		
 		if( !trusted )
 		{
 			String cmd = "desc stream:" + fqstream;
 			Cleartool.run( cmd );
 		}
 		
 		
 	}
 	
 	public static Stream Create( String stream_fqname, Stream parent_stream, String comment, Baseline baseline, boolean readonly )
 	{
 		logger.trace_function();
 		
 		String args_bl = "";
 		String args_cm = " -nc ";
 		String args_ro = "";
 		
 		if( baseline != null )
 		{
 			args_bl = " -baseline " + baseline.GetFQName();
 		}
 		if( comment.length() > 0 )
 		{
 			args_cm = " -c " + comment;
 		}
 		if( readonly )
 		{
 			args_ro = " -readonly ";
 		}
 		
 		String cmd = "mkstream " + args_cm + " " + args_bl + " " + args_ro + " -in stream:" + parent_stream.GetFQName() + " " + stream_fqname;
 		Cleartool.run( cmd );
 		
 		return new Stream( stream_fqname, false );
 	}
 	
 	
 	public int Recommend( Baseline baseline, String comment )
 	{
 		comment = comment != null ? " -c \"" + comment + "\"" : " -nc";
 		// cleartool chstream " . $comment . " -recommend " . $baseline->get_fqname() . ' ' . $self->get_fqname() . ' 2>&1';
 		String cmd = "chstream " + comment + " -recommend " + baseline.GetFQName() + " " + this.GetFQName();
 		String result = Cleartool.run( cmd );
 		
 		/* If not, return 0 */
 		
 		return 1;
 	}
 	
 	/**
 	 * 
 	 * @param baseline
 	 * @param view
 	 * @param complete
 	 * @return
 	 */
 	public int Rebase( Baseline baseline, View view, String complete )
 	{
 		logger.trace_function();
 		
 		Snapview sview = (Snapview)view;
 		
 		if( baseline == null && view == null )
 		{
 			System.err.println( "required parameters are missing" );
 			logger.log( "required parameters are missing", "error" );
 			System.exit( 1 );
 		}
 		
 		if( complete != null )
 		{
 			complete = complete.length() == 0 ? " -complete " : complete;
 		}
 		else
 		{
 			complete = " -complete ";
 		}
 		
 		// cleartool( "rebase $complete -force -view " . $params{view}->get_viewtag(). " -stream " . $self->get_fqname(). " -baseline " . $params{baseline}->get_fqname()
 		String cmd = "rebase " + complete + " -force -view " + sview.GetViewTag() + " -stream " + this.GetFQName() + " -baseline " + baseline.GetFQName();
 		Cleartool.run( cmd );
 		
 		return 1;
 	}
 	
 	public int Remove()
 	{
 		logger.trace_function();
 		
 		// cleartool( "rmstream -force -nc " . $self->get_fqname() );
 		String cmd = "rmstream -force -nc " + this.GetFQName();
 		
 		return 1;
 	}
 	
 	public String GetPvob()
 	{
 		logger.trace_function();
 		return pvob;
 	}
 	
 	public Component GetSingleTopComponent()
 	{
 		logger.trace_function();
 		
 		ArrayList<Baseline> recbls = GetRecBls( false );
 		
 		if( recbls.size() != 1 )
 		{
 
 		}
 		
 		return recbls.get( 0 ).GetComponent();
 	}
 	
 	public Baseline GetSingleLatestBaseline()
 	{
 		logger.trace_function();
 		
 		// 'lsbl -s -component ' . $self->get_single_top_component->get_fqname . ' -stream  ' . $self->get_fqname;
 		String cmd = "lsbl -s -component " + this.GetSingleTopComponent().GetFQName() + " -stream  " + this.GetFQName();
 		String[] bls = Cleartool.run_a( cmd );
 		String latest = bls[bls.length-1].trim();
 		
 		return new Baseline( latest + "@" + this.GetPvob(), false );
 	}
 	
 	public ArrayList<Baseline> GetRecBls( boolean expanded )
 	{
 		logger.trace_function();
 		
 		ArrayList<Baseline> bls = new ArrayList<Baseline>();
 		
 		if( this.rec_bls != null )
 		{
 			bls = this.rec_bls;
 		}
 		else
 		{
 			// cleartool( 'desc -fmt %[rec_bls]p stream:' . $self->{'fqstream'} );
 			String cmd = "desc -fmt %[rec_bls]p stream:" + this.GetFQName();
 			String result = Cleartool.run( cmd );
 			String[] rs = result.split( " " );
 			
 			for( int i = 0 ; i < rs.length ; i++ )
 			{
 				/* There is something in the element. */
 				if( rs[i].matches( "\\S+" ) )
 				{
 					bls.add( new Baseline( rs[i] + "@" + this.pvob, true ) );
 				}
 			}
 			
 			this.rec_bls = bls;
 		}
 		
 		if( expanded )
 		{
 			bls = Baseline.StaticExpandBls( bls );
 		}
 		
 		return bls;
 	}
 	
 	
 	/**
 	 * 
 	 * @param expanded
 	 * @return
 	 */
 	public ArrayList<Baseline> GetLatestBls( boolean expanded )
 	{
 		logger.trace_function();
 		
 		ArrayList<Baseline> bls = new ArrayList<Baseline>();
 		
 		if( this.latest_bls != null )
 		{
 			bls = this.latest_bls;
 		}
 		else
 		{
 			// 'cleartool desc -fmt %[latest_bls]p stream:' . $self->{'fqstream'} . ' 2>&1';
 			String cmd = "desc -fmt %[latest_bls]p stream:" + this.fqstream;
 			String result = Cleartool.run( cmd );
 			
 			String[] rs = result.split( " " );
 			
 			for( int i = 0 ; i < rs.length ; i++ )
 			{
 				if( rs[i].matches( "\\S+" ) )
 				{
 					bls.add( new Baseline( rs[i].trim(), true ) );
 				}				
 			}
 		}
 				
 		if( expanded )
 		{
 			bls = Baseline.StaticExpandBls( bls );
 		}
 		
 		return bls;
 	}
 	
 	public ArrayList<Baseline> GetFoundBls( boolean expanded )
 	{
 		logger.trace_function();
 		
 		ArrayList<Baseline> bls = new ArrayList<Baseline>();
 		
 		if( this.found_bls != null )
 		{
 			bls = this.found_bls;
 		}
 		else
 		{
 			// cleartool desc -fmt %[found_bls]p stream:' . $self->{'fqstream'} . ' 2>&1';
 			String cmd = "desc -fmt %[found_bls]p stream:" + this.GetFQName();
 			String result = Cleartool.run( cmd );
 			String[] rs = result.split( " " );
 			
 			for( int i = 0 ; i < rs.length ; i++ )
 			{
 				if( rs[i].matches( "\\S+" ) )
 				{
 					bls.add( new Baseline( rs[i].trim(), true ) );
 				}
 			}
 			
 			this.found_bls = bls;
 		}
 		
 		if( expanded )
 		{
 			bls = Baseline.StaticExpandBls( bls );
 		}
 		
 		return bls;
 	}
 	
 	public String GetFQName()
 	{
 		logger.trace_function();
 		return this.fqstream;
 	}
 	
 	public String GetShortname()
 	{
 		logger.trace_function();
 		return this.shortname;
 	}
 	
 	public String BetBrType()
 	{
 		logger.trace_function();
 		
 		if( this.brtype != null )
 		{
 			return this.brtype;
 		}
 		
 		// cleartool( 'desc -ahlink IndependentGuard -s stream:' . $self->{'fqstream'} );
 		String cmd = "desc -ahlink IndependentGuard -s stream:" + this.GetFQName();
 		String result = Cleartool.run( cmd );
 		
 		Pattern p = Pattern.compile( "brtype:(.*)\\@.*$" );
 		Matcher match = p.matcher( result );
 		
 		/* There's a match */
 		if( match.find() )
 		{
 			this.brtype = match.group( 1 );
 			return this.brtype;
 		}
 		
 		return null;
 	}
 	
 	
 	public ArrayList<Activity> GetActivities()
 	{
 		logger.trace_function();
 		
 		if( this.activities != null )
 		{
 			return this.activities;
 		}
 		
 		// 'desc -fmt %[activities]p stream:' . $self->{'fqstream'} );
 		String cmd = "desc -fmt %[activities]p stream:" + this.fqstream;
 		String result = Cleartool.run( cmd );
 		this.activities = new ArrayList<Activity>();
 		
 		String[] rs = result.split( " " );
 		for( int i = 0 ; i < rs.length ; i++ )
 		{
 			this.activities.add( new Activity( rs[i] + "@" + this.pvob, true ) );
 		}
 		
 		return this.activities;
 	}
 	
 	public ArrayList<String> GetFullChangeSetAsElements()
 	{
 		logger.trace_function();
 		
 		HashMap<String, String> accumulated_filelist = new HashMap<String, String>();
 		
 		ArrayList<Activity> act = this.GetActivities();
 		for( int i = 0 ; i < act.size() ; i++ )
 		{
 			ArrayList<String> elem = act.get( i ).GetChangeSetAsElements();
 			for( int j = 0 ; j < elem.size() ; j++ )
 			{
 				accumulated_filelist.put( elem.get( j ), "" );
 			}
 		}
 		
 		/* CHW: Experimental sorting. UNTESTED! */
 		logger.debug( "Experimental sorting. UNTESTED!" );
 		SortedSet<String> sortedset = new TreeSet<String>( accumulated_filelist.keySet() );
 		Iterator<String> it = sortedset.iterator();
 		
 		ArrayList<String> r = new ArrayList<String>();
 		
 	    while ( it.hasNext() )
 	    {
	        r.add( it.next() );
 	    }
 		
 		return r;
 	}
 	
 	public ArrayList<Baseline> DiffRecLatest()
 	{
 		logger.trace_function();
 		
 		HashMap<String, Baseline> diff = new HashMap<String, Baseline>();
 		
 		ArrayList<Baseline> bl = GetLatestBls( true );
 		for( int i = 0 ; i < bl.size() ; i++ )
 		{
 			diff.put( bl.get( i ).GetComponentName(), bl.get( i ) );
 		}
 		
 		bl = GetRecBls( true );
 		for( int i = 0 ; i < bl.size() ; i++ )
 		{
 			/* The Baseline exists, identified by the component name */
 			if( diff.get( bl.get( i ).GetComponent() ) != null )
 			{
 				/* If the two shortnames are equal! */
 				if( diff.get( bl.get( i ).GetComponent() ).GetShortname().equals( bl.get( i ).GetShortname() ) )
 				{
 					/* Same baseline was also in rec_bls, remove from diff list */
 					diff.remove( bl.get( i ).GetComponent() );
 				}
 			}
 		}
 		
 		ArrayList<Baseline> bls = new ArrayList<Baseline>();
 		
 		/* CHW: Experimental sorting. UNTESTED! */
 		logger.debug( "Experimental sorting. UNTESTED!" );
 		SortedSet<String> sortedset = new TreeSet<String>( diff.keySet() );
 		Iterator<String> it = sortedset.iterator();
 		
 		ArrayList<Baseline> r = new ArrayList<Baseline>();
 		
 	    while ( it.hasNext() )
 	    {
 	        r.add( diff.get( it.next() ) );
 	    }
 		
 		return r;
 		
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public static String GetStreamOfWorkingView()
 	{
 		logger.trace_function();
 		
 		// cleartool('pwv -root');
 		String vw = Cleartool.run( "pwv -root" ).trim();
 		
 		String viewdotdatpname = vw + filesep + "view.dat";
 		
 		String file = "";
 		try
 		{
 			file = Utilities.GetFileToString( viewdotdatpname );
 		}
 		catch ( FileNotFoundException e )
 		{
 			logger.error( vw + " is not a valid snapshot view" );
 			System.err.println( vw + " is not a valid snapshot view" );
 			System.exit( 1 );
 		}
 		
 		/* CHW: May miss multiline regex!!! Check manual! */
 		Pattern p = Pattern.compile( "view_uuid:(.*)" );
 		Matcher match = p.matcher( file );
 		
 		if( !match.find() )
 		{
 			logger.error( "ERROR: " + viewdotdatpname + " is not valid - can't read view uuid." );
 			System.err.println( "ERROR: " + viewdotdatpname + " is not valid - can't read view uuid." );
 			System.exit( 1 );
 		}
 		
 		// cleartool("lsview -s -uuid $1");
 		String viewtag = Cleartool.run( "lsview -s -uuid " + match.group( 1 ) ).trim();
 		// cleartool( 'lsstream -fmt %Xn -view ' . $viewtag );
 		String result = Cleartool.run( "lsstream -fmt %Xn -view " + viewtag );
 		
 		return result.replace( "stream:", "" );
 	}
 
 	/**
 	 * Tells whether a Stream exists or not
 	 * @param fqstream
 	 * @return boolean
 	 */
 	public static boolean StreamExists( String fqstream )
 	{
 		logger.trace_function();
 		
 		// cleartool( "describe stream:" . $fqstream );
 		try
 		{
 			Cleartool.run( "describe stream:" + fqstream );
 			return true;
 		}
 		catch( CleartoolException e )
 		{
 			return false;
 		}
 	}
 	
 
 	
 
 
 	
 }
