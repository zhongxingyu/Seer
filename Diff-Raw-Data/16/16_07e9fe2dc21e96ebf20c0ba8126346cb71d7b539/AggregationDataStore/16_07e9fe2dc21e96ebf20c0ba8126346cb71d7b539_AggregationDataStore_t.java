 package org.geotools.data.aggregation;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import nl.utwente.db.neogeo.preaggregate.PreAggregate;
 
 import org.geotools.data.Query;
 import org.geotools.data.store.ContentDataStore;
 import org.geotools.data.store.ContentEntry;
 import org.geotools.data.store.ContentFeatureSource;
 import org.geotools.feature.NameImpl;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.opengis.feature.type.Name;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 // Referenced classes of package org.geotools.data.aggregation:
 //            AggregationFeatureSource
 
 public class AggregationDataStore extends ContentDataStore {
 
 	private static final Logger LOGGER = Logger.getLogger("org.geotools.data.aggregation.AggregationDataStore");
 	//private static final String TYPE_NAMES_QUERY = "select tablename,label from pre_aggregate";
 	// first ? is the column name of the geometry column
 	// second ? is the table name of the indexed table
 	private static final String NATIVE_SRS_QUERY = "SELECT ST_SRID(COLUMN) FROM TABLE limit 1";
 	// first ? is the string tablename+"_"+label
 	private static final String GEOMETRY_COLUMN_QUERY = "SELECT tablename, substr(columnexpression,6,length(columnexpression)-6) FROM pre_aggregate_axis where tablename || '___' ||label=? and substr(columnexpression,1,4)='ST_X'";
 	private static final String BOUNDS_QUERY = "SELECT tablename, substr(columnexpression,1,4), low, high FROM pre_aggregate_axis where tablename || '___' ||label=? and substr(columnexpression,1,3)='ST_' order by columnexpression";
 	private static final String NAME = "aggregate";
 	//private static final String TOTAL_COUNT_QUERY = null;
 
 	private String hostname;
 	private int port;
 	private String username;
 	private String password;
 	private String database;
 	private Connection con;
 	private String schema;
 	private int xSize;
 	private int ySize;
 	private int timeSize;
 	private int mask;
 
 	//	/**
 	//	 * @deprecated Use {@link #AggregationDataStore(String,int,String,String,String,int,int,long)} instead
 	//	 */
 	//	public AggregationDataStore(String hostname, int port, String username, String password, String database){
 	//		this(hostname, port, username, password, database, 0,
 	//				0, 0);
 	//	}
 
 	public AggregationDataStore(String hostname, int port, String schema, String database, String username, String password, int xSize, int ySize, int timeSize, int mask){
 		this.hostname = hostname; 
 		this.port = port;
 		this.username = username;
 		this.password = password;
 		this.schema = schema;
 		this.database = database;
 		this.xSize = xSize;
 		this.ySize = ySize;
 		this.timeSize = timeSize;
 		this.mask = mask;
 		LOGGER.severe("AggregationDataStore created!!! ###########");
 	}
 
 	/**
 	 * return a connection object to a postgres database with aggregation index
 	 * @return
 	 */
 	public Connection getConnection(){
 		if(con==null)
 			con = _getConnection();
 		return con;
 	}
 
 	private Connection _getConnection(){
 		try{
 			Class.forName("org.postgresql.Driver");
 		} catch(ClassNotFoundException e)
 		{
 			LOGGER.severe("Where is your PostgreSQL JDBC Driver? Include in your library path!");
 			e.printStackTrace();
 			return null;
 		}
 		LOGGER.fine("PostgreSQL JDBC Driver Registered!");
 		Connection connection = null;
 		try
 		{
 			connection = DriverManager.getConnection((new StringBuilder()).append("jdbc:postgresql://").append(hostname).append(":").append(port).append("/").append(database).toString(), username, password);
 		}
 		catch(SQLException e)
 		{
 			LOGGER.severe("Connection Failed! Check output console");
 			e.printStackTrace();
 			return null;
 		}
 		if(connection == null)
 			LOGGER.severe("Failed to make connection!");
 		return connection;
 	}
 
 	@Override
 	protected List<Name> createTypeNames() throws IOException	{
 		List<Name> ret = null;
 		getConnection();
 		try {
 			List<String> names = PreAggregate.availablePreAggregates(con,schema);
 			ret = new Vector<Name>();
 			for(String name : names){
 				ret.add(new NameImpl(NAME+"_"+name));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	@Override
 	protected ContentFeatureSource createFeatureSource(ContentEntry entry) {
 		return new AggregationFeatureSource(entry, Query.ALL);
 	}
 
 	public int getCRSNumber(ContentEntry entry) {
 		int ret = 0;
 		String typename = entry.getTypeName();
 		typename = typename.substring((NAME+"_").length());
 		PreparedStatement stmt1;
 		try {
 			stmt1 = con.prepareStatement(GEOMETRY_COLUMN_QUERY);
 			stmt1.setString(1, typename);
 			LOGGER.finest("geometry column query:"+stmt1.toString());
 			stmt1.execute();
 			ResultSet rs1 = stmt1.getResultSet();
 			String tablename = "";		
 			String locColumn = "";
 			while(rs1.next()){
 				tablename = rs1.getString(1);
 				locColumn = rs1.getString(2);
 			}
 			rs1.close();
 			Statement stmt2;
 			try {
 				String query = NATIVE_SRS_QUERY.replaceFirst("COLUMN", locColumn).
 				replaceFirst("TABLE", tablename);
 				stmt2 = con.createStatement();			
 				LOGGER.finest("NATIVE SRS query:"+stmt2.toString());
 				stmt2.execute(query);
 				ResultSet rs2 = stmt2.getResultSet();
 				while(rs2.next()){
 					ret = rs2.getInt(1);
 				}
 				rs2.close();
 			} catch (SQLException e) {
 
 				e.printStackTrace();
 			}
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 
 		return ret;
 	}
 
 	public boolean hasOutputCount(){
 		return hasOutputCount(PreAggregate.AGGR_ALL);
 	}
 
 	public boolean hasOutputCount(int mask2){
 		return (mask & mask2 & PreAggregate.AGGR_COUNT)==PreAggregate.AGGR_COUNT;
 	}
 
 	public boolean hasOutputSum(){
 		return hasOutputSum(PreAggregate.AGGR_ALL);
 	}
 	
 	public boolean hasOutputSum(int mask2){
 		return (mask & mask2 & PreAggregate.AGGR_SUM)==PreAggregate.AGGR_SUM;
 	}
 
 	public boolean hasOutputMin(){
 		return hasOutputMin(PreAggregate.AGGR_ALL);
 	}
 
 	public boolean hasOutputMin(int mask2){
 		return (mask & mask2 & PreAggregate.AGGR_MIN)==PreAggregate.AGGR_MIN;
 	}
 
 	public boolean hasOutputMax(){
 		return hasOutputMax(PreAggregate.AGGR_ALL);
 	}
 	
 	public boolean hasOutputMax(int mask2){
 		return (mask & mask2 & PreAggregate.AGGR_MAX)==PreAggregate.AGGR_MAX;
 	}
 
 	public int getTotalCount(String typeName) {
 		//		int count = -1;
 		//		getConnection();
 		//		Statement stmt;
 		//		try {
 		//			stmt = con.createStatement();
 		//			stmt.execute(TOTAL_COUNT_QUERY);
 		//			ResultSet rs = stmt.getResultSet();
 		//			while(rs.next()){
 		//				count = rs.getInt(1);
 		//			}
 		//		} catch (SQLException e) {
 		//			e.printStackTrace();
 		//		}
 		//		return count;
 		return xSize*ySize;
 	}
 
 	public ReferencedEnvelope getReferencedEnvelope(ContentEntry entry, CoordinateReferenceSystem coordinateReferenceSystem) {
 		ReferencedEnvelope bounds = null;
 		String typename = entry.getTypeName();
 		typename = typename.substring((NAME+"_").length());
 		PreparedStatement stmt1;
 		try {
 			stmt1 = con.prepareStatement(BOUNDS_QUERY);
 			stmt1.setString(1, typename);
 			LOGGER.finest("bounds query:"+stmt1.toString());
 			stmt1.execute();
 			ResultSet rs1 = stmt1.getResultSet();
 			rs1.next();
 			double x1 = Double.valueOf(rs1.getString("low"));
 			double x2 = Double.valueOf(rs1.getString("high"));
 			rs1.next();
 			double y1 = Double.valueOf(rs1.getString("low"));
 			double y2 = Double.valueOf(rs1.getString("high"));
 			// parameters double x1, double x2, double y1, double y2, CoordinateReferenceSystem crs
 			bounds = new ReferencedEnvelope(x1,x2,y1,y2, coordinateReferenceSystem );
 			rs1.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}			
 		return bounds;
 	}
 
 	public int getXSize(){
 		return xSize;
 	}
 
 	public int getYSize(){
 		return ySize;
 	}
 
 	public int getTimeSize(){
 		return timeSize;
 	}
 
 	public int getMask(){
 		return mask;
 	}
	
	public String stripTypeName(String typename) {
		return typename.substring((NAME+"_").length());
	}
 
 	public PreAggregate createPreAggregate(String typename) throws SQLException{
 		String tablename = PreAggregate.getTablenameFromTypeName(typename);
 		String label = PreAggregate.getLabelFromTypeName(typename);
 		return new PreAggregate(getConnection(),schema,tablename,label);
 	}
 }
