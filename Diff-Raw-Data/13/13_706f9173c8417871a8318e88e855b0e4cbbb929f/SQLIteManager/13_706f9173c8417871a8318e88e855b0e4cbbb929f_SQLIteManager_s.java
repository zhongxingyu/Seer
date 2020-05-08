 package ch.epfl.bbcf.utility;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 
 import ch.epfl.bbcf.conversion.conf.Configuration;
 import ch.epfl.bbcf.conversion.sqltree.ScoreTree;
 
 
 
 
 public class SQLIteManager {
 
 	private static final Logger logger = Configuration.initLogger(SQLIteManager.class.getName());
 	private final static int LIMIT_QUERY_SIZE = 100000000;
 
 	public static Connection getConnection(String path) throws ClassNotFoundException, SQLException{
 		Class.forName("org.sqlite.JDBC");
 		Connection conn = DriverManager.getConnection("jdbc:sqlite:/"+path);
 		return conn;
 	}
 	public static ResultSet getResultSet(Statement stat,String query) throws SQLException{
 		return stat.executeQuery(query);
 	}
 
 	//////FIRST DATABASE//////
 
 
 	public static Float getMaxScoreForChr(String md5, String chr) {
 		try{
 			Connection conn = getConnection(md5);
 			Statement stat = conn.createStatement();
			String query = "SELECT max(score) FROM "+chr+";";
 			ResultSet rs = getResultSet(stat, query);
 			Float f = null;
 			while (rs.next()) {
 				f= rs.getFloat(1);
 			}
 			rs.close();
 			conn.close();
 			return f;
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		return null;
 	}
 	public static int getMaxLengthForChromosome(String chr,String md5) {
 		int f = 0;
 		try{
 			Connection conn = getConnection(md5);
 			Statement stat = conn.createStatement();
			String query = "SELECT max(end) FROM "+chr+";";
 			ResultSet rs = getResultSet(stat, query);
 			while (rs.next()) {
 				f= rs.getInt(1);
 			}
 			rs.close();
 			conn.close();
 			return f;
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		return f;
 	}
 	public static File getScoresForChromosome(String md5,String chrName){
 		try{
 			Connection conn = getConnection(md5);
 			Statement stat = conn.createStatement();
			String query ="select * from "+chrName+";";
 			ResultSet rs = getResultSet(stat, query);
 			//ChromosomeFeature feat = new ChromosomeFeature();
 			File file = new File(Configuration.getTmpDirectory()+"/"+UUID.randomUUID().toString()+".rset");
 			FileManagement fm = new FileManagement(file);
 			//logger.debug("writing on file : "+file.getAbsolutePath());
 			while (rs.next()) {
 				int start = rs.getInt("start");
 				int stop = rs.getInt("end");
 				float score = rs.getFloat("score");
 				fm.writeTo(start+"\t"+stop+"\t"+score+"\n");
 			}
 			rs.close();
 			conn.close();
 			fm.close();
 			//logger.debug("end writing");
 			return file;
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		} catch (SQLException e) {
 			logger.error(e);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			logger.error(e);
 		}
 		return null;
 	}
 
 	
 	public static List<String> getChromosomesNames(String dbName) {
 		List<String> chrNames = new ArrayList<String>();
 		Connection conn;
 		try {
 			conn = getConnection(dbName);
 			Statement stat = conn.createStatement();
 			String query = "SELECT name FROM sqlite_master where type='table'and name!='attributes';";
 			ResultSet rs = getResultSet(stat, query);
 			while (rs.next()) {
 				chrNames.add(rs.getString(1));
 			}
 			rs.close();
 			conn.close();
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		return chrNames;
 	}
 	public static Map<String, Integer> getChromosomesAndLength(String dbName) {
 		Map<String,Integer> result = new HashMap<String,Integer>();
 		Connection conn;
 		try {
 			conn = getConnection(dbName);
 			Statement stat = conn.createStatement();
 			String query = "SELECT * FROM chrNames;";
 			ResultSet rs = getResultSet(stat, query);
 			while (rs.next()) {
 				result.put(rs.getString("name"),
 						rs.getInt("length"));
 			}
 			rs.close();
 			conn.close();
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		return result;
 	}
 
 	public static String getStringAttribute(String md5, String key) {
 		String result = null;
 		Connection conn;
 		try {
 			conn = getConnection(md5);
 			Statement stat = conn.createStatement();
 			String query = "select * from attributes where key = ? ;";
 			PreparedStatement prep = conn.prepareStatement(query);
 			prep.setString(1, key);
 			prep.execute();
 			ResultSet rs = prep.getResultSet();//getResultSet(stat, prep);
 			while (rs.next()) {
 				result = rs.getString("value");
 			}
 			rs.close();
 			conn.close();
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		} catch (SQLException e) {
 			logger.error(e);
 		}
 		return result;
 	}
 
 
 
 
 
 	//////SECOND DATABASES///////
 	/**
 	 * SQLite JDBC does support only FORWARD resultset;
 	 */
 	public static float[] getMinMaxScoreForChr(String database) throws SQLException{
 
 		logger.debug("MINMAX FOR : "+database);
 		float[]minMax=new float[2];
 		try {
 			Connection conn = getConnection(database);
 			//create index
 			//			PreparedStatement stat = conn.prepareStatement("create index minmax on sc(score);");
 			//			stat.execute();
 			//get minmax
 			PreparedStatement stat = conn.prepareStatement(
 			"select min(score),max(score) from sc;");
 			ResultSet resultSet = stat.executeQuery();
 			if(resultSet.next()){
 				minMax[0] = resultSet.getFloat(1);
 				minMax[1] = resultSet.getFloat(2); 
 				conn.close();
 				return minMax;
 			}
 
 		} catch (ClassNotFoundException e) {
 			logger.error(e);
 		}
 		return null;
 	}
 
 
 
 	public static boolean floatEquals(float a,float b){
 		float epsilon = 0.0001f;
 		boolean z = (Math.abs(a-b)<epsilon);
 		return z;
 	}
 	public static void filldb(String chrName, float[] tab,
 			int imageNumber, int zoom, ConnectionStore connectionStore, boolean finish) {
 		String database = chrName+"_"+zoom+".db";
 		Connection conn = connectionStore.getConnection(database);
 		if(null!=conn){
 			PreparedStatement prep = connectionStore.getPreparedStatement(database);
 			float val = tab[0];
 			int pos = 0;
 			for(int i=1;i<tab.length;i++){
 				if(!floatEquals(val, tab[i])){
 					try {
 						prep.setInt(1, imageNumber);
 						prep.setInt(2,pos);
 						prep.setFloat(3, val);
 						prep.execute();
 					} catch (SQLException e) {
 						//logger.error(e);
 					}
 					pos=i;
 					val=tab[i];
 				}
 			}
 			try {
 				prep.setInt(1, imageNumber);
 				prep.setInt(2,pos);
 				prep.setFloat(3, val);
 				prep.execute();
 			} catch (SQLException e) {
 				//logger.error(e);
 			}
 			int nbQueries = connectionStore.getNbQueries(database);
 			if(nbQueries>LIMIT_QUERY_SIZE || finish){
 				try {
 					conn.commit();
 					nbQueries = -ScoreTree.TAB_WIDTH;
 				} catch (SQLException e1) {
 					logger.error(e1);
 				}
 			}
 			connectionStore.setNbQueries(database,nbQueries+ScoreTree.TAB_WIDTH);
 			if (finish){
 				try {
 					conn.close();
 				} catch (SQLException e1) {
 					logger.error(e1);
 				}
 			}
 		}
 		else {
 			logger.error("didn't find connection for "+database);
 		}
 	}
 	public static ConnectionStore createNewDatabase(String outdbName, String outdbPath,
 			String chromosome, int[] zooms) {
 		ConnectionStore connectionStore = new ConnectionStore();
 		boolean noerror = true;
 		//logger.debug("creating database (chrName_zoomLevel.db) for chromosome "+chromosome);
 		Connection conn;
 		try {
 			for(int z : zooms){
 				String database = chromosome+"_"+z+".db";
 				conn = getConnection(outdbPath+"/"+outdbName+"/"+database);
 				PreparedStatement stat = conn.prepareStatement(
 				"create table sc (number INT,pos INT,score REAL,PRIMARY KEY(number,pos));");
 				stat.execute();
 				PreparedStatement prep = conn.prepareStatement("insert into sc values (?,?,?);");
 				conn.setAutoCommit(false);
 				connectionStore.addDatabase(database,conn,prep);
 			}
 		} catch (ClassNotFoundException e) {
 			noerror = false;
 			logger.error(e);
 		} catch (SQLException e) {
 			noerror = false;
 			logger.error(e);
 		}
 		return connectionStore;
 	}
 
 
 
 

 
 
 
 
 
 }
