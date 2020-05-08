 package com.tehbeard.BeardStat.DataProviders;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 
 import net.dragonzone.promise.Deferred;
 import net.dragonzone.promise.Promise;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import com.tehbeard.BeardStat.BeardStat;
 import com.tehbeard.BeardStat.NoRecordFoundException;
 import com.tehbeard.BeardStat.containers.IStat;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 
 
 /**
  * base class for JDBC based data providers
  * @author James
  *
  */
 public abstract class JDBCStatDataProvider implements IStatDataProvider {
 
 	protected Connection conn;
 
 	//protected static PreparedStatement prepGetPlayerStat;
 
 	//Load components
 	protected PreparedStatement getDomains;
 	protected PreparedStatement getWorlds;
 	protected PreparedStatement getCategories;
 	protected PreparedStatement getStatistics;
 
 	//save components
 	protected PreparedStatement saveDomain;
 	protected PreparedStatement saveWorld;
 	protected PreparedStatement saveCategory;
 	protected PreparedStatement saveStatistic;
 
 	//Load data from db
 	protected PreparedStatement loadEntity;
 	protected PreparedStatement loadEntityData;
 
 	//save to db
 	protected PreparedStatement saveEntity;
 	protected PreparedStatement saveEntityData;
 
 
 
 	//Maintenance
 	protected PreparedStatement keepAlive;
 	protected PreparedStatement listEntities;
 	protected PreparedStatement deleteEntity;
 	protected PreparedStatement createTable;
 
 
 	private HashMap<String,EntityStatBlob> writeCache = new HashMap<String,EntityStatBlob>();
 
 
 	//Connection related configuration
 	protected String connectionUrl = "";
 	protected Properties connectionProperties = new Properties();
 	protected String tblPrefix = "stats";
 	private String type = "sql";
 
 
 
 	//ID Cache
 	private Map<String,Integer> domains = new HashMap<String, Integer>();
 	private Map<String,Integer> worlds = new HashMap<String, Integer>();
 	private Map<String,Integer> categories = new HashMap<String, Integer>();
 	private Map<String,Integer> statistics = new HashMap<String, Integer>();
 
 
 	//private WorkQueue loadQueue = new WorkQueue(1);
 	private ExecutorService loadQueue = Executors.newSingleThreadExecutor();
 
 	public JDBCStatDataProvider(String type,String driverClass){
 		this.type = type;
 		try {
 			Class.forName(driverClass);
 		} catch (ClassNotFoundException e) {
 			BeardStat.printCon("JDBC "+ driverClass + "Library not found!");
 		}
 	}
 
 	protected void initialise(){
 		createConnection();
 
 		checkAndMakeTable();
 		prepareStatements();
 		cacheComponents();
 	}
 
 	/**
 	 * Connection to the database.
 	 * @throws SQLException
 	 */
 	private void createConnection() {
 
 		BeardStat.printCon("Connecting....");
 
 		try {
 			conn = DriverManager.getConnection(connectionUrl,connectionProperties);
 
 			//conn.setAutoCommit(false);
 		} catch (SQLException e) {
 			BeardStat.mysqlError(e);
 			conn = null;
 		}
 
 
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private synchronized boolean checkConnection(){
 		BeardStat.printDebugCon("Checking connection");
 		try {
 			if(conn == null || !conn.isValid(0)){
 				BeardStat.printDebugCon("Something is derp, rebooting connection.");
 				createConnection();
 				if(conn!=null){
 					BeardStat.printDebugCon("Rebuilding statements");
 					prepareStatements();
 				}
 				else
 				{
 					BeardStat.printDebugCon("Reboot failed!");
 				}
 
 			}
 		} catch (SQLException e) {
 			conn = null;
 			return false;
 		}catch(AbstractMethodError e){
 
 		}
 		BeardStat.printDebugCon("Checking is " + conn != null ? "up" : "down");
 		return conn != null;
 	}
 
 	protected void checkAndMakeTable(){
 		BeardStat.printCon("Constructing table as needed.");
 
 		try{
 
 			String[] creates = BeardStat.self().readSQL(type,"sql/maintenence/create.tables", tblPrefix).replaceAll("\n|\r", "").split(";");
 			for(String sql : creates){
				BeardStat.printDebugCon("Creating: " + sql);
 				conn.prepareStatement(sql).execute();
 			}
 		} catch (SQLException e) {
 			BeardStat.mysqlError(e);
 		}		
 	}
 
 	protected void prepareStatements(){
 		try{
 			BeardStat.printDebugCon("Preparing statements");
 
 			loadEntity     = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/load/getEntity", tblPrefix));
 			loadEntityData = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/load/getEntityData", tblPrefix));
 
 
 			//Load components
 			getDomains    = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/load/components/getDomains", tblPrefix));
 			getWorlds     = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/load/components/getWorlds", tblPrefix));
 			getCategories = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/load/components/getCategories", tblPrefix));
 			getStatistics = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/load/components/getStatistics", tblPrefix));
 
 			//save components
 			saveDomain    = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/save/components/saveDomain", tblPrefix),Statement.RETURN_GENERATED_KEYS);
 			saveWorld     = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/save/components/saveWorld", tblPrefix),Statement.RETURN_GENERATED_KEYS);
 			saveCategory  = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/save/components/saveCategory", tblPrefix),Statement.RETURN_GENERATED_KEYS);
 			saveStatistic = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/save/components/saveStatistic", tblPrefix),Statement.RETURN_GENERATED_KEYS);
 
 			//save to db
 			saveEntity     = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/save/saveEntity", tblPrefix),Statement.RETURN_GENERATED_KEYS);
 			saveEntityData = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/save/saveStat", tblPrefix));
 
 			//Maintenance
 			keepAlive      = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/maintenence/keepAlive", tblPrefix));
 			listEntities   = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/maintenence/listEntities", tblPrefix));
			deleteEntity   = conn.prepareStatement(BeardStat.self().readSQL(type,"sql/maintenence/deletePlayerFully", tblPrefix));
 
 
 			BeardStat.printDebugCon("Set player stat statement created");
 			BeardStat.printCon("Initaised MySQL Data Provider.");
 		} catch (SQLException e) {
 			BeardStat.mysqlError(e);
 		}
 	}
 
 
 	private void cacheComponents(){
 		try {
 			cacheComponent(domains,getDomains);
 			cacheComponent(worlds,getWorlds);
 			cacheComponent(categories,getCategories);
 			cacheComponent(statistics,getStatistics);
 		} catch (SQLException e) {
 			BeardStat.mysqlError(e);
 		}
 	}
 
 	private void cacheComponent(Map<String,Integer> mapTo,PreparedStatement statement) throws SQLException{
 		ResultSet rs = statement.executeQuery();
 		while(rs.next()){
 			mapTo.put(rs.getString(2),rs.getInt(1));
 		}
 
 		rs.close();
 	}
 
 	private int getComponentId(Map<String,Integer> mapTo,PreparedStatement statement,String name) throws SQLException{
 		if(!mapTo.containsKey(name)){
 			statement.setString(1, name);
 			statement.execute();
 			ResultSet rs = statement.getGeneratedKeys();
 			mapTo.put(name,rs.getInt(1));
 		}
 		return mapTo.get(name);
 	}
 
 	public Promise<EntityStatBlob> pullPlayerStatBlob(String player) {
 		return pullPlayerStatBlob(player,true);
 	}
 
 	public Promise<EntityStatBlob> pullPlayerStatBlob(final String player, final boolean create) {
 
 		final Deferred<EntityStatBlob> promise = new Deferred<EntityStatBlob>();
 
 		Runnable run = new Runnable() {
 
 			public void run() {
 				try {
 					if(!checkConnection()){
 						BeardStat.printCon("Database connection error!");
 						promise.reject(new SQLException("Error connecting to database"));
 						return;
 					}
 					long t1 = (new Date()).getTime();
 
 
 					//Ok, try to get entity from database
 					loadEntity.setString(1, player);
 					loadEntity.setString(2,"player");//TODO: ALLOW CHOICE OF ENTITY TYPE
 
 					ResultSet rs = loadEntity.executeQuery();
 					EntityStatBlob pb = null;
 
 					if(!rs.next()){
 						//No player found! Let's create an entry for them!
 						rs.close();
 						rs = null;
 						saveEntity.setString(1, player);
 						saveEntity.setString(2,"player");
 						saveEntity.executeUpdate();
 						rs = saveEntity.getGeneratedKeys();//get dat key
 						rs.next();
 
 					}
 
 					//make the player object, close out result set.
 					pb = new EntityStatBlob(player,rs.getInt(1),"player");
 					rs.close();
 					rs = null;
 
 					//load all stats data
 					loadEntityData.setInt(1, pb.getEntityID());
 					BeardStat.printDebugCon("executing " + loadEntityData);
 					rs = loadEntityData.executeQuery();
 
 					boolean foundStats = false;
 					while(rs.next()){
 						//`domain`,`world`,`category`,`statistic`,`value`
 						IStat ps = pb.getStat(
 								rs.getString(1),
 								rs.getString(2),
 								rs.getString(3),
 								rs.getString(4)
 								);
 						ps.setValue(rs.getInt(5));
 						ps.clearArchive();
 						foundStats = true;
 					}
 					rs.close();
 
 					BeardStat.printDebugCon("time taken to retrieve: "+((new Date()).getTime() - t1) +" Milliseconds");
 					if(!foundStats && create==false){
 						promise.reject(new NoRecordFoundException());
 						return;}
 
 					promise.resolve(pb);return;
 				} catch (SQLException e) {
 					BeardStat.mysqlError(e);
 					promise.reject(e);
 				}
 
 
 			}
 		};
 
 
 		loadQueue.execute(run);
 
 		return promise;
 
 	}
 
 	public void pushPlayerStatBlob(EntityStatBlob player) {
 
 		synchronized (writeCache) {
 
 
 			EntityStatBlob copy = player.cloneForArchive();
 
 
 
 			if(!writeCache.containsKey(player.getName())){
 				writeCache.put(player.getName(), copy);
 			}
 		}
 
 	}
 
 	private Runnable flush = new Runnable() {
 
 		public void run() {
 			synchronized (writeCache) {
 				try {
 					keepAlive.execute();
 				} catch (SQLException e1) {
 				}
 
 				if(!checkConnection()){
 					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not restablish connection, will try again later, WARNING: CACHE WILL GROW WHILE THIS HAPPENS");
 				}
 				else{
 					BeardStat.printDebugCon("Saving to database");
 					for(Entry<String, EntityStatBlob> entry : writeCache.entrySet()){
 						try {
 							EntityStatBlob pb = entry.getValue();
 
 							saveEntityData.clearBatch();
 							for(IStat stat : pb.getStats()){
 								saveEntityData.setInt(1, pb.getEntityID());
 								saveEntityData.setInt(2,getComponentId(domains, saveDomain, stat.getDomain()));
 								saveEntityData.setInt(3,getComponentId(worlds, saveWorld, stat.getWorld()));
 								saveEntityData.setInt(4,getComponentId(categories, saveCategory, stat.getCategory()));
 								saveEntityData.setInt(5,getComponentId(statistics, saveStatistic, stat.getStatistic()));
 								saveEntityData.setInt(6,stat.getValue());
 
 
 								saveEntityData.addBatch();
 							}
 							saveEntityData.executeBatch();
 
 						} catch (SQLException e) {
 							checkConnection();
 						}
 					}
 					BeardStat.printDebugCon("Clearing write cache");
 					writeCache.clear();
 				}
 			}
 
 		}
 	};
 
 	public void flushSync(){
 		BeardStat.printCon("Flushing in main thread! Game will lag!");
 		flush.run();
 		BeardStat.printCon("Flushed!");
 	}
 
 	public void flush() {
 
 		new Thread(flush).start();
 	}
 
 	public void deletePlayerStatBlob(String player) {
 		throw new UnsupportedOperationException();
 	}
 
 	public boolean hasStatBlob(String player) {
 		try {
 			loadEntity.clearParameters();
 			loadEntity.setString(1,player);
 			loadEntity.setString(2,"player");
 
 			ResultSet rs = loadEntity.executeQuery();
 			boolean found = rs.next();
 			rs.close();
 			return found;
 
 		} catch (SQLException e) {
 			checkConnection();
 		}
 		return false;
 	}
 
 	public List<String> getStatBlobsHeld() {
 		List<String> list = new ArrayList<String>();
 		try {
 			listEntities.setString(1, "player");
 
 			ResultSet rs = listEntities.executeQuery();
 			while(rs.next()){
 				list.add(rs.getString(1));
 			}
 			rs.close();
 
 		} catch (SQLException e) {
 			checkConnection();
 		}
 		return list;
 	}
 }
