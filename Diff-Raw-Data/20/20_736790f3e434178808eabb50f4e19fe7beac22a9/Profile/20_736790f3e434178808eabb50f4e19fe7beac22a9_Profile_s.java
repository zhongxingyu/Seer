 package stv6;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import joptsimple.OptionSpec;
 import stv6.database.Database;
 import stv6.database.MysqlDatabase;
 import stv6.database.SqliteDatabase;
 import stv6.episodes.EpisodeManager;
 import stv6.episodes.managers.FileSystemManager;
 import stv6.episodes.managers.MediaTombManager;
 import stv6.episodes.managers.TversityManager;
 import stv6.episodes.managers.UpnpManager;
 import stv6.handlers.util.StaticMessageHandler;
 import stv6.http.request.Request;
 import stv6.series.Series;
 import stv6.series.SeriesList;
 import stv6.series.TrackedSeries;
 import stv6.sync.IdUpdateData;
 import stv6.sync.SyncSettings;
 import stv6.sync.SyncSettings.SyncPage;
 import stv6.sync.SyncTrackHandler;
 import stv6.sync.Synchronizer;
 import stv6.sync.TrackData;
 
 public class Profile implements Reloadable, Runnable {
 	public final static String TEMPLATE_PATH = "template";  
 	
 	private final static Profile instance_ = new Profile();
 	private int id = -1; // profile ID in the db
 	
 	private User globalUser = null;
 	private final SeriesList series = new SeriesList();
 	
 	private Database db = null;
 	private EpisodeManager epmgr = null;	
 
 	// settings
 	private String localPlayer, userName = null;
 	private int port = 80, agedTime = 1814400;
 	private SyncSettings syncSettings = null;
 	private String templateName = null;
 	
 	// profile fun
 	private File[] possibleProfiles = null;
 	private File profileFile = null;	
 	private String profileName = null;
 	private boolean profileFromCommandLine = false;
 	private File pluginExe = null;
 
 	private final List<User> possibleUsers = new LinkedList<User>();
 
 	private Boolean reloading = Boolean.FALSE;
 	
 	private Profile() {
 	}
 	
 	public boolean addExistingSeries(List<Series> toInsert) {
 		if (toInsert.size() > 0) {
 			db.addExistingSeries(id, toInsert);
 			return true;
 		}
 		
 		return false;
 	}
 
 	public boolean addNewSeries(List<Series> toCreate) {
 		if (toCreate.size() > 0) {
 			db.addNewSeries(id, toCreate);			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public static Collection<String> asList(String...strings ) {
 		LinkedList<String> ret = new LinkedList<String>();
 		for (String s : strings)
 			ret.add(s);
 		return ret;
 	}
 	
 	private static String buildPath(String...names) {
 		StringBuffer buff = new StringBuffer();
 		for (String name : names) {
 			buff.append(name);
 			buff.append(File.separatorChar);
 		}
 		return buff.substring(0, buff.length()-1);
 	}
 	
 	public void createNewSeries(List<Series> toCreate) {
 		if (toCreate.size() > 0)
 			db.createNewSeries(toCreate);
 	}
 	
 	public void getAllSeries(SeriesList list) {
 		db.getAllSeries(list);
 	}
 
 	public void getAllSeries(SeriesList list, boolean overwrite) {
 		db.getAllSeries(list, overwrite);
 	}
 	
 
 	/**
 	 * @see Database#getAllSeriesAsUser(SeriesList, User)
 	 * @param user
 	 * @return
 	 */
 	public SeriesList getAllSeriesAsUser(User user) {
 		return db.getAllSeriesAsUser(series, user);
 	}
 	
 	/**
 	 * Return a list so that it's sorted for sure
 	 * @param user
 	 * @return
 	 */
 	public List<TrackedSeries> getRecentSeries(User user) {
 	    return db.getRecentSeries(series, user);
 	}
 	
 	/**
 	 * Load the series as requested in, well, the given Request. We
 	 * 	expect the Series' ID to be in the GET var "id" 
 	 * 
 	 * @param r
 	 * @return the Series requested as a TrackedSeries for the
 	 * 	specified user in the Request 
 	 */
 	public TrackedSeries getSeriesFromRequest(Request r) {
 
 		// make sure the ID's set
 		if (!r.getGetVars().isSet("id")) 
 			return null;
 		
 		// read the ID defensively and load the series
 		try {
 			int seriesId = Integer.parseInt( r.getGetVars().getValue("id") );			
 			return (seriesId < 0) ? null : db.getSeriesAsUser(seriesId, series, r.getUser());
 		} catch (NumberFormatException e) {
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * Note that this is NOT cached!
 	 * @return A SeriesList of all Series (as TrackedSeries)
 	 * 	available from the selected EpisodeManager 
 	 */
 	public SeriesList getAvailableSeries() {
 		return epmgr.getAvailableSeries();
 	}
 	
 	/**
 	 * @return A File pointing to the executable
 	 * 	with which to execute plugins
 	 */
 	public File getPluginExe() {
 		return pluginExe;
 	}
 	
 	/**
 	 * @return The port number ST should listen on
 	 */
 	public int getPort() {
 		return port;
 	}
 
 
 	public File[] getPossibleProfiles() {
 		return possibleProfiles;
 	}
 
 	public List<User> getPossibleUsers() {
 		return possibleUsers;
 	}
 	
 	/**
 	 * @return A SeriesList of all Series attached
 	 * 	to this profile
 	 */
 	public SeriesList getSeries() {
 		return series;
 	}
 
 	/**
 	 * @see Database#getSyncNewSeries(SyncSettings)
 	 */
 	public SeriesList getSyncNewSeries() { 
 		return db.getSyncNewSeries(syncSettings);
 	}
 
 	public List<TrackData> getSyncNewTracks() {
 		return db.getSyncNewTracks(syncSettings);
 	}
 
 	public List<User> getSyncNewUsers() {
 		return db.getSyncNewUsers(syncSettings);
 	}
 
 	public boolean fillUser(Request r) {
 		if (globalUser != null) {
 			r.setUser( globalUser );
 			return true;
 		}
 		
 		r.readHeaders();
 		if (r.getCookies().isSet(STClient.COOKIE_USERID)) {
 			// load the user. this is super lame, but I'm lazy :P
 			String strId;
 			for (User u : possibleUsers) {
 				strId = String.valueOf(u.getId());
 				if (r.getCookies().getValue(STClient.COOKIE_USERID).equals(strId)) {
 					r.setUser(u);
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public static Profile getInstance() {
 		return instance_;
 	}
 
 	public String getTemplatePathFor(Request r, String fileName) {
 		// get path from command line
 		
 		// check for GET var "template"
 		if (r.getGetVars().isSet("template"))
 			return buildPath(TEMPLATE_PATH, r.getGetVars().getValue("template"), fileName);
 		
 		// get template name from command line
 		if (templateName != null)
 			return buildPath(TEMPLATE_PATH, templateName, fileName);
 			
 		// just default
 		return buildPath(TEMPLATE_PATH, fileName);
 	}
 
 	/**
 	 * @return True if there are multiple profiles to pick from
 	 */
 	public boolean hasMultipleProfiles() {
 		return possibleProfiles != null && possibleProfiles.length > 1;
 	}
 
 	/**
 	 * @return True if there are multiple users to pick from
 	 */
 	public boolean hasMultipleUsers() {
 		return possibleUsers != null && possibleUsers.size() > 1;
 	}
 	
 	/**
 	 * @param args The ones from main()
 	 */
 	public void initialize(String[] args) {
 		// check for a specified profile
 		ArgumentParser p = ArgumentParser.getInstance();
 		OptionSet o = p.parse(args);
 		
 		if (o.has("?")) {
 			try {
 				p.parser.printHelpOn(System.out);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			System.exit(0);
 		}
 
 		if (o.has(p.profile)) {
 			File path = p.profile.value(o);
 			if (path.exists() && path.isFile() && path.canRead()) {
 				loadFromFile(path);
 				return;
 			} else {
 				System.err.println("Given profile path does not exist or cannot be read.");
 				System.err.println("Given: " + path.getAbsolutePath());
 				System.exit(1);
 			}
 		}
 		
 		// if sufficient args on command line, use them
 		if (o.has(p.emtype) || o.has(p.dbtype)) {
 			profileFromCommandLine = true;
 			loadFromOptions(o);
 			return;
 		}
 		
 		// look in the profile directory
 		File profileDir = p.profileDir.value(o);
 		File[] possibleProfiles = null;
 		if (profileDir.exists() && profileDir.isDirectory())
 			possibleProfiles = profileDir.listFiles(new FilenameFilter() {
 				@Override
 				public boolean accept(File dir, String name) {
 					return name.endsWith(".profile");
 				}				
 			});
 
 		if (possibleProfiles != null && possibleProfiles.length == 1) {
 			// there's just one; load it!
 			loadFromFile(possibleProfiles[0]);
 			return;
 		} else if (possibleProfiles != null && possibleProfiles.length > 1 ){		
 			// else, we'll prompt for a selection (there are multiple in directory)
 			this.possibleProfiles = possibleProfiles;
 			port = p.portArg.value(o); // get the default, anyway
 			return;
 		}
 		
 		// last case: just use defaults
 		loadFromOptions(o);
 	}
 
 	/**
 	 * @return True if we have a profile selected
 	 */
 	public boolean isSelected() {
 		return id > -1;
 	}
 	
 	/**
 	 * @return True if synchronization is on
 	 */
 	public boolean isSyncing() {
 		return syncSettings != null;
 	}
 	
 	public void loadFromFile(File file) {
 		StringBuffer b = new StringBuffer();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(file));
 			String line = null;
 			while ((line = in.readLine()) != null) {
 				b.append(line);
 				b.append(" "); // just in case
 			}
 		} catch (FileNotFoundException e) {
 			// this should never happen, but just in case...
 			System.err.println("Could not load profile file: " + file.getName());
 			System.exit(1);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.err.println("Error reading profile file: " + file.getName());
 			System.exit(1);
 		}
 		
 		String[] args = splitStringIntoArgs(b.toString());
 		ArgumentParser p = ArgumentParser.getInstance();
 		OptionSet o = p.parse(args);		
 		loadFromOptions(o);			
 		
 		// save for later
 		profileFile = file;
 		//possibleProfiles = null; // I guess?
 	}
 	
 	private static String[] splitStringIntoArgs(String string) {
 		List<String> matchList = new LinkedList<String>();
 		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
 		Matcher regexMatcher = regex.matcher(string);		
 		while (regexMatcher.find()) {
 		    if (regexMatcher.group(1) != null) {
 		        // Add double-quoted string without the quotes
 		        matchList.add(regexMatcher.group(1));
 		    } else if (regexMatcher.group(2) != null) {
 		        // Add single-quoted string without the quotes
 		        matchList.add(regexMatcher.group(2));
 		    } else {
 		        // Add unquoted word
 		        matchList.add(regexMatcher.group());
 		    }
 		}
 		
 		return matchList.toArray(new String[0]);
 	}
 
 	private void loadFromOptions(OptionSet o) {
 		ArgumentParser p = ArgumentParser.getInstance();
 		
 		pluginExe = p.pluginExe.value(o);
 		
 		agedTime = p.agedTime.value( o );
 		int oldPort = port;
 		port = p.portArg.value( o );
 		if (port != oldPort) {
 			// we probably switched profiles and the config is inconsistent...
 			try {
 				STServer.getInstance(port);
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.err.println("Could not set port to: " + port);
 				System.exit(1);
 			}
 		}
 		if (o.has(p.template))
 			templateName = p.template.value(o);
 		if (o.has(p.playerPath))
 			localPlayer = p.playerPath.value(o);
 //		if (o.has(p.profileName)) 
 			profileName = p.profileName.value(o); // we want to get default if not given
 		if (o.has(p.userName))
 			userName = p.userName.value(o);		
 		if (o.has(p.syncUrl))
 			syncSettings = new SyncSettings(p.syncUrl.value(o), p.syncPass.value(o));
 		
 		if (p.dbtype.value(o).equals("mysql")) {
 			// try to build a mysql db
 			String dbserver = p.mysqlserver.value(o);
 			String dbname = p.mysqldb.value(o);
 			String dbuser = p.mysqluser.value(o);
 			String dbpass = p.mysqlpass.value(o);
 			db = new MysqlDatabase(dbserver, dbname, dbuser, dbpass);
 		} else if (p.dbtype.value(o).equals("flat")) {
 			//String cfgFile = p.flatDbArg.value(o);
 			//db = new FlatFileDatabase(cfgFile);
 			System.err.println("Flat-file database support is not yet available.");
 			System.exit(1);
 		} else {
 			// default to sqlite...?
 			db = new SqliteDatabase( p.dbFileArg.value(o) );
 		} 
 		
 		if (p.emtype.value(o).equals("mediatomb")) {
 			int mtport = p.mtportArg.value(o);
 			boolean checkTr = o.has("mediatomb-tr-check");
 			String trProf = p.mtTrProfile.value(o);
 			
 			epmgr = new MediaTombManager(STServer.getBroadcastingIp(), 
 					mtport, trProf, checkTr,
 					p.mtTrForce.values(o)
 			);
 		} else if (p.emtype.value(o).equals("local")) {
 			if (!o.has(p.localArg)) {
 				System.err.println("Error: Local episodes requested, but local-folders " +
 						"not provided");
 				System.exit(1);
 			}
 			epmgr = new FileSystemManager(p.localArg.value(o));
 		} else if (p.emtype.value(o).equals("pms")) {
 		    // ps3 media server
 		    
 		    epmgr = new UpnpManager(STServer.getBroadcastingIp(), true);
		    System.out.println("Warning: PMS must be running on the same computer is STv6");
 		} else if (p.emtype.value(o).equals("upnp")) {
 		    int upnpPort = p.upnpPortArg.value(o);
 		    
 		    if (o.has(p.localArg)) {
 	            epmgr = new UpnpManager(STServer.getBroadcastingIp(), upnpPort,
 	                    p.localArg.value(o));
 		    } else {
 		        epmgr = new UpnpManager(STServer.getBroadcastingIp(), upnpPort);
 		    }
 		    System.out.println("Warning: UPNP support is ALPHA, and NOT automatic!");
 		    System.out.println("The server must be local, and you must specify the port...");
 		} else {
 			// default to tversity
 			
 			int tvport = p.tvportArg.value(o);		
 			epmgr = new TversityManager(STServer.getBroadcastingIp(), tvport);
 			System.out.println("Warning: TVersity support is UNTESTED!");
 			System.out.println("It *might* work, though :)");
 		}
 	}
 	
 	@Override
     public void reload() {
 		
 		// make sure we only try to reload once
 		synchronized(reloading) {
 			if (!reloading) {
 				reloading = true;
 				new Thread(this).start();
 			}
 		}
 	}
 
 	/**
 	 * Threaded reloading fun
 	 */
 	@Override
 	public void run() {
 		STHandlerManager.getInstance().setStaticMessage(true);
 		StaticMessageHandler.getInstance().setBody();
 		System.out.print("Reloading... ");
 		if (!profileFromCommandLine && !isSelected() && profileFile == null) {
 			System.out.println("Failed (No profile chosen; will prompt)");
 			System.out.println("Server listening on port: " + getPort());
 			synchronized(reloading) {
 				reloading = false;
 			}
 			return;
 		}
 		series.clear();
 		db.reload();
 		
 		globalUser = null; // clear it before anything
 		if (userName != null) {
 			globalUser = db.getUser(userName);
 		} else {
 			// no user specified; check for a single user environment
 			possibleUsers.clear();
 			db.getAllUsers(possibleUsers);
 			if (possibleUsers.size() == 1)
 				globalUser = possibleUsers.get(0);
 			else if (possibleUsers.size() == 0) 
 				globalUser = db.getUser(STClient.DEFAULT_USERNAME);
 		}
 		
 		// determine profile id
 		if (profileName != null) {
 			id = db.getProfileId(profileName);
 		} else if (profileFile != null) {
 			id = db.getProfileId(profileFile);
 		}
 		if (id < 0) {
 			System.err.println("Couldn't determine profile ID");
 			System.exit(1);
 		}
 
 		if (isSyncing()) {
 			if (globalUser != null && globalUser.getName().equals(STClient.DEFAULT_USERNAME)) {				
 				StaticMessageHandler.getInstance().setBody(
 						"To use synchronization, you must create a username. " +
 						"Either specify a user in the profile using \"-u\" or " +
 						"remove synchronization, restart SeriesTracker, and create " +
 						"a user in the Options menu.");
 				StaticMessageHandler.getInstance().setRefresh(null);
 				System.out.println("[No user; could not sync] Failed.");
 				return;
 			}
 			System.out.print("[Synchronizing... ");
 			StaticMessageHandler.getInstance().setBody("Synchronizing data with remote server");
 			try {
 				if (!Synchronizer.getInstance().synchronize(syncSettings))
 					return;
 			} catch (UnknownHostException e) {
 				System.out.print("Failure (no internet/invalid server) ");
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.out.print("Failure (???)");
 			}
 			
 			// reload users list, just in case...
 			// TODO: Have synchronize() return something more meaningful
 			//	so we don't have to do this every time
 			possibleUsers.clear();
 			db.getAllUsers(possibleUsers);
 			
 			System.out.print("Done.] ");
 			StaticMessageHandler.getInstance().setBody();
 		}
 
 		db.fillSeries(series, id);
 		
 		epmgr.reload();
 		epmgr.getAvailableSeries(series);	
 		System.out.println("Done.");
 		STHandlerManager.getInstance().setStaticMessage(false);
 		
 		synchronized(reloading) {
 			reloading = false;
 		}
 	}
 
 	public long getLastSyncFor(SyncPage syncPage) {
 		return db.getLastSyncFor(syncSettings, syncPage);
 	}
 
 	public String getLocalPlayer() {
 		return localPlayer;
 	}
 
 	
 	public static long getNowSeconds() {
 		return Calendar.getInstance().getTimeInMillis()/1000;
 	}
 
 	/**
 	 * Remove the given series from this profile. Assumes that
 	 * 	they are already in the DB and have ids
 	 * 
 	 * Does NOT the series from the DB! Just from this profile!
 	 * 
 	 * @param toRemove
 	 */
 	public void removeSeries(List<Series> toRemove) {
 		if (toRemove.size() > 0) {
 			db.removeSeries(id, toRemove);
 			for (Series s : toRemove)
 				series.remove(s);
 		}
 	}
 	
 	/**
 	 * @return True if the given TrackedSeries is considered "recent"
 	 * 	by the arguments given to us 
 	 */
 	public boolean seriesIsRecent(TrackedSeries s) {
 		long oldDateSecs = getNowSeconds();		
 		return (s.getLastView() >= oldDateSecs - agedTime);
 	}
 	
 	/**
 	 * To be called after saving tracking data, so we can send it to the server
 	 */
 	private void synchronizeTracks() {
 		try {			
 			SyncTrackHandler.handle(syncSettings);
 		} catch (UnknownHostException e) {
 			System.out.println("Sync Failure (no internet/invalid server) ");
 		} catch (IOException e) {
 			System.out.println("Sync Failure (???) ");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * A sync-related function; synchronizes local ids with ones
 	 * 	from the server. 
 	 * 
 	 * @param table
 	 * @param oldIds
 	 * @param newIds
 	 */
 	public void updateUserIds( List<IdUpdateData> ids ) {
 		db.updateUserIds(ids);
 	}
 	
 	public void updateSeriesIds( List<IdUpdateData> ids ) {
 		db.updateSeriesIds(ids);
 	}
 
 	/**
 	 * Update the tracking of series s for the given user
 	 * @param s
 	 * @param user
 	 */
 	public void updateSeriesTracking(TrackedSeries s, User user) {
 		db.updateSeriesTracking(s, user);
 		
 		if (isSyncing())
 			synchronizeTracks();
 	}
 
 
 	/**
 	 * @see Database#updateSeriesTracking(TrackData...)
 	 * @param data
 	 */
 	public void updateSeriesTracking(TrackData[] data) {
 		db.updateSeriesTracking(data);
 	}
 
 
 	public void updateSyncTime(SyncPage syncPage) {
 		db.updateSyncTime(syncSettings, syncPage);
 	}
 
 	private static class ArgumentParser {
 
         private static final ArgumentParser instance_ = new ArgumentParser();
 		
 		public OptionParser parser;
 		public OptionSpec<Integer> portArg, tvportArg, mtportArg, upnpPortArg, agedTime;
 		public OptionSpec<String> template, playerPath, userName, profileName,
 			dbtype, emtype, mysqldb, mysqlserver, mysqluser, mysqlpass, 
 			dbFileArg, mtTrForce, mtTrProfile, localArg, syncUrl, syncPass;
 		public OptionSpec<File> profile, profileDir, pluginExe;
 		
 		private ArgumentParser() {
 			parser = new OptionParser();
 			parser.acceptsAll( asList("?","h","help") , "show help");
 			profile = parser.accepts("profile", "Profile path")
 				.withRequiredArg()
 				.ofType(File.class);
 			profileName = parser.accepts("profile-name", "The NAME of the profile. This is " +
 					"associated with the series stored in it, and must be specified if " +
 					"you wish to separate series per profile. However, If the profile is loaded " +
 					"from a file, then this value is taken from the file name.")
 				.withRequiredArg()
 				.ofType(String.class)
 				.defaultsTo("Default");
 			profileDir = parser.accepts("profile-dir", "Profile directory")
 				.withRequiredArg()
 				.ofType(File.class)
 				.defaultsTo(new File("profiles"));
 			pluginExe = parser.accepts("plugin-exe", "Plugin Executor")
 				.withRequiredArg()
 				.ofType(File.class)
 				.defaultsTo(new File("/usr/bin/python"));
 			template = parser.accepts("template", "Template name")
 				.withRequiredArg()
 				.ofType(String.class);			
 			userName = parser.acceptsAll( asList("u", "username"), "Username" )
 				.withRequiredArg()
 				.ofType(String.class);
 			portArg = parser.acceptsAll( asList("p", "port"), "SeriesTracker port" )			
 				.withRequiredArg().ofType( Integer.class )
 				.defaultsTo(8080)
 				.describedAs("port");
 			playerPath = parser.acceptsAll( asList("l","local-player") )
 				.withRequiredArg().ofType( String.class )
 				.describedAs("player");
 			agedTime = parser.accepts("aged-time", "Time in seconds between views " +
 					"after which a series is considered \"old\".")
 				.withRequiredArg().ofType( Integer.class )
 				.describedAs("time (s)")
 				.defaultsTo(1814400); // 3 weeks
 			dbtype = parser.acceptsAll( asList("d", "database"), "database type")
 				.withRequiredArg().ofType( String.class )
 				.describedAs("(mysql/sqlite/flat)")
 				.defaultsTo("sqlite");
 			emtype = parser.acceptsAll( asList("e", "episodes"), "episode manager")
 				.withRequiredArg().ofType( String.class )
 				.describedAs("(tversity/mediatomb/local/upnp/pms)")
 				.defaultsTo("tversity");
 			mysqldb = parser.accepts("mysql-db","mysql database name")
 				.withRequiredArg().ofType( String.class )
 				.defaultsTo("seriestracker");
 			mysqlserver = parser.accepts("mysql-server", "mysql server")
 				.withRequiredArg().ofType( String.class )
 				.defaultsTo("localhost");
 			mysqluser = parser.accepts("mysql-user", "mysql user")
 				.withRequiredArg().ofType( String.class )
 				.defaultsTo("seriestracker");
 			mysqlpass = parser.accepts("mysql-pass", "mysql password")
 				.withRequiredArg().ofType( String.class )
 				.defaultsTo("");
 			dbFileArg = parser.accepts("db-file", "(sqlite) database filename")
 				.withRequiredArg().ofType( String.class )
 				.defaultsTo("seriestracker.db")
 				.describedAs("database filename");
 			tvportArg = parser.accepts("tversity-port", "tversity's operating port")
 				.withRequiredArg().ofType( Integer.class )
 				.defaultsTo(41952);
 			mtportArg = parser.accepts("mediatomb-port", "mediatomb's operating port")
 				.withOptionalArg().ofType( Integer.class )
 				.defaultsTo(49152);
 			mtTrForce = parser.accepts("mediatomb-tr-check", 
 					"If set, mediatomb will only transcode if a subtitle file is found, or is of " +
 					"one of the supplied file extensions")
 				.withOptionalArg()
 				.describedAs("ext1,ext2,...")
 				.withValuesSeparatedBy(',');
 			mtTrProfile = parser.accepts("mediatomb-tr-profile",
 					"The profile to use for transcoding")
 				.withRequiredArg().ofType( String.class )
 				.defaultsTo("video-common");
 			upnpPortArg = parser.accepts("upnp-port", "UPNP server's port")
 			    .withOptionalArg().ofType( Integer.class )
 			    .defaultsTo(5001);
 			localArg = parser.accepts("local-folders", "folders, separated" +
 					" by \";\", or a filename with folders on separate lines")
 				.withRequiredArg().ofType( String.class )
 				.describedAs("folders");	
 			syncUrl = parser.accepts("sync", "Base URL of synchronization server")
 				.withRequiredArg().ofType( String.class )
 				.describedAs("url");
 			syncPass = parser.accepts("sync-pass", "Password to use with synchronization server")
 				.withRequiredArg().ofType( String.class )
 				.describedAs("password");			
 		}
 		
 		public OptionSet parse(String[] args) {
 			return parser.parse(args);
 		}
 
 		public static ArgumentParser getInstance() {
 			return instance_;
 		}
 	}
 
 }
