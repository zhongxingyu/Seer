 /**
  * Copyright (C) 2014
  * by 52 North Initiative for Geospatial Open Source Software GmbH
  *
  * Contact: Andreas Wytzisk
  * 52 North Initiative for Geospatial Open Source Software GmbH
  * Martin-Luther-King-Weg 24
  * 48155 Muenster, Germany
  * info@52north.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.envirocar.analyse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bson.types.ObjectId;
 import org.envirocar.analyse.entities.InMemoryPoint;
 import org.envirocar.analyse.entities.Point;
 import org.envirocar.analyse.properties.Properties;
 import org.envirocar.analyse.util.Utils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 
 public class PostgresPointService implements PointService {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(PostgresPointService.class);
 
 	private Connection conn = null;
 	private String connectionURL = null;
 	private String databaseName;
 	private String databasePath;
 															
 	private String username;
 	private String password;
 	private String aggregated_MeasurementsTableName = "aggregated_measurements";// TODO
 	private String original_MeasurementsTableName = "original_measurements";// TODO from
 																				// properties
 	private String spatial_ref_sys = "4326";// TODO from properties
 	private String id_exp = "$id$";
 	private String distance_exp = "$distance$";
 	private String table_name_exp = "$tablename$";
 	private AggregationAlgorithm algorithm;
 	private Map<String, ResultSet> trackDatabasePointsResultSetMap;
 
 	public final String pgCreationString = "CREATE TABLE "
 			+ aggregated_MeasurementsTableName + " ("
 			+ "ID VARCHAR(24) NOT NULL PRIMARY KEY, "
 			+ "NUMBEROFPOINTS INTEGER, "
 			+ "NUMBEROFCONTRIBUTINGTRACKS INTEGER,"
 			+ "LASTCONTRIBUTINGTRACK VARCHAR(24)," + "CO2 DOUBLE PRECISION,"
 			+ "SPEED DOUBLE PRECISION)";
 	
 	public final String pgOriginalMeasurementsTableCreationString = "CREATE TABLE "
 			+ original_MeasurementsTableName + " ("
 			+ "ID VARCHAR(24) NOT NULL PRIMARY KEY, "
 			+ "TRACKID VARCHAR(24)," + "CO2 DOUBLE PRECISION,"
 			+ "SPEED DOUBLE PRECISION)";
 
 	public String pgNearestNeighborCreationString = "select h.id, h.speed, h.co2, h.numberofpoints, h.numberofcontributingtracks, h.lastcontributingtrack, ST_AsText(h.the_geom) as text_geom, ST_distance(w.the_geom,h.the_geom) as dist from aggregated_measurements h, "
 			+ "(select * from original_measurements where id='"
 			+ id_exp
 			+ "') w "
 			+ "where ST_DWithin(w.the_geom,h.the_geom,"
 			+ distance_exp + ") " + "order by dist;";
 
 	public final String addGeometryColumnToAggregated_MeasurementsTableString = "SELECT AddGeometryColumn( '"
 			+ table_name_exp
 			+ "', 'the_geom', " + spatial_ref_sys + ", 'POINT', 2 );";
 	
 	public final String selectAllMeasurementsofTrackQueryString = "select h.id, h.speed, h.co2, h.trackid, ST_AsText(h.the_geom) as text_geom from original_measurements h where h.trackid = ";
 	
 	public final String selectAllAggregatedMeasurementsString = "select h.id, h.speed, h.co2, h.numberofpoints, h.numberofcontributingtracks, h.lastcontributingtrack, ST_AsText(h.the_geom) as text_geom from aggregated_measurements h; ";
 	
 	public final String deletePointFromAggregatedMeasurementsString = "delete from aggregated_measurements where id=";
 	
 	public PostgresPointService() {
 		this(null);
 	}
 	
 	public PostgresPointService(AggregationAlgorithm algorithm) {
 		this.algorithm = algorithm;
 		trackDatabasePointsResultSetMap = new HashMap<>();
 		
 		createConnection();
 		createTable(pgOriginalMeasurementsTableCreationString, original_MeasurementsTableName);
 		createTable(pgCreationString, aggregated_MeasurementsTableName);
 	}
 
 	@Override
 	public Point getNextPoint(String trackID) {
 		
 		ResultSet rs = trackDatabasePointsResultSetMap.get(trackID);
 		
 		try {
 			if(!rs.next()){
 				return null;
 			}
 		} catch (SQLException e) {
 			LOGGER.error("Could access current resultset entry.", e);
 		}
 		
 		try {
 			return createPointFromCurrentResultSetEntry(rs);
 		} catch (SQLException e) {
 			LOGGER.error("Could not create point from current resultset entry.", e);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void getMeasurementsOfTracks(List<String> trackIDs) {
 		
 		for (String trackID : trackIDs) {
 			
 			URL url = null;
 			try {
 				url = new URL(Properties.getRequestTrackURL() + trackID);
 				
 				InputStream in = url.openStream();
 
 				ObjectMapper objMapper = new ObjectMapper();
 
 				Map<?, ?> map = objMapper.readValue(in, Map.class);
 
 				ArrayList<?> features = null;
 
 				for (Object o : map.keySet()) {
 					Object entry = map.get(o);
 
 					if (o.equals("features")) {
 						features = (ArrayList<?>) entry;
 					}
 				}
 				
 				createPointsFromJSON(features, trackID, algorithm.getBbox());
 				
 			} catch (MalformedURLException e) {
 				LOGGER.error("Malformed URL: " + url == null ? null : url.toString(), e);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 	@Override
 	public void getMeasurementsOfTrack(String trackID) {
 		
 		URL url = null;
 		try {
 			url = new URL(Properties.getRequestTrackURL() + trackID);
 			
 			InputStream in = url.openStream();
 
 			ObjectMapper objMapper = new ObjectMapper();
 
 			Map<?, ?> map = objMapper.readValue(in, Map.class);
 
 			ArrayList<?> features = null;
 
 			for (Object o : map.keySet()) {
 				Object entry = map.get(o);
 
 				if (o.equals("features")) {
 					features = (ArrayList<?>) entry;
 				}
 			}
 			
 			createPointsFromJSON(features, trackID, algorithm.getBbox());
 			
 		} catch (MalformedURLException e) {
 			LOGGER.error("Malformed URL: " + url == null ? null : url.toString(), e);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public Point getNearestNeighbor(String pointID, double distance) {
 
 		String queryString = pgNearestNeighborCreationString.replace(id_exp,
 				pointID).replace(distance_exp, "" + distance);
 
 		ResultSet resultSet = executeQueryStatement(queryString);
 
 		try {
 
 			if (resultSet != null) {
 
 				while (resultSet.next()) {
 
 					String resultID = resultSet.getString("id");
 
 					Map<String, Object> propertyMap = new HashMap<>();
 
 					for (String propertyName : Properties
 							.getPropertiesOfInterestDatatypeMapping().keySet()) {
 
 						Class<?> propertyClass = Properties
 								.getPropertiesOfInterestDatatypeMapping().get(
 										propertyName);
 
 						Object value = null;
 
 						if (propertyClass.equals(Double.class)) {
 							value = resultSet.getDouble(propertyName
 									.toLowerCase());
 						}
 
 						propertyMap.put(propertyName, value);
 					}
 
 					String resultLastContributingTrack = resultSet
 							.getString("lastcontributingtrack");
 
 					int resultNumberOfContributingPoints = resultSet
 							.getInt("numberofpoints");
 
 					int resultNumberOfContributingTracks = resultSet
 							.getInt("numberofcontributingtracks");
 
 					String resultGeomAsText = resultSet.getString("text_geom");
 
 					double[] resultXY = Utils
 							.convertWKTPointToXY(resultGeomAsText);
 
 					LOGGER.debug(resultSet.getString("id"));// TODO remove
 					LOGGER.debug("" + resultSet.getDouble("dist"));// TODO
 																	// remove
 					LOGGER.debug(resultSet.getString("text_geom"));// TODO
 																	// remove
 
 					Point resultPoint = new InMemoryPoint(resultID,
 							resultXY[0], resultXY[1], propertyMap,
 							resultNumberOfContributingPoints,
 							resultNumberOfContributingTracks,
 							resultLastContributingTrack);
 
 					return resultPoint;
 				}
 
 			}
 		} catch (SQLException e) {
 			LOGGER.error("Could not query nearest neighbor of " + pointID, e);
 		}
 
 		return null;
 	}
 	
 	@Override
 	public boolean updateResultSet(String idOfPointToBeReplaced,
 			Point newPoint) {
 		
 		removePoint(idOfPointToBeReplaced);
 		
 		addToResultSet(newPoint);
 		
 		return true;
 	}
 
 	@Override
 	public List<Point> getResultSet() {
 		    	
 		List<Point> result = new ArrayList<>();
 		
     	ResultSet resultSet = executeQueryStatement(selectAllAggregatedMeasurementsString);
 		
     	try {
 
 			if (resultSet != null) {
 
 				while (resultSet.next()) {
 					
 					Point resultPoint = createPointFromCurrentResultSetEntry(resultSet);
 					
 					result.add(resultPoint);
 				}
 
 			}
 		} catch (SQLException e) {
 			LOGGER.error("Could not query resultset.", e);
 		}
     	
 		return result;
 	}
 
 	@Override
 	public Point aggregate(Point point, Point aggregationPoint) {
 		
 		updateValues(point, aggregationPoint);
 		
 		point.setID(new ObjectId().toString());
 		
 		point.setX(aggregationPoint.getX());
 		point.setY(aggregationPoint.getY());
 				
 		point.setNumberOfPointsUsedForAggregation(aggregationPoint.getNumberOfPointsUsedForAggregation()+1);
 		
 		LOGGER.debug(point.getLastContributingTrack() + " vs. " + aggregationPoint.getLastContributingTrack());
 		
 		if(!point.getLastContributingTrack().equals(aggregationPoint.getLastContributingTrack())){
 			point.setNumberOfTracksUsedForAggregation(aggregationPoint.getNumberOfTracksUsedForAggregation() +1);
 		}
 		
 		LOGGER.debug("Aggregated: " + point.getID() + " and " + aggregationPoint.getID());
 		LOGGER.debug("NumberOfPoints " + point.getNumberOfPointsUsedForAggregation());
 		
 		return point;
 	}
 
 	@Override
 	public void addToResultSet(Point newPoint) {		
 		insertPoint(newPoint.getID(), newPoint.getX(), newPoint.getY(), newPoint.getLastContributingTrack(), newPoint.getNumberOfPointsUsedForAggregation(), newPoint.getNumberOfTracksUsedForAggregation(), newPoint.getPropertyMap(), true);
 	}
 	
 	private boolean removePoint(String pointID){
 		return executeUpdateStatement(deletePointFromAggregatedMeasurementsString.concat("'" + pointID + "';"));
 	}
 	
 	private String getDatabaseName() {
 		
 		if(databaseName == null || databaseName.equals("")){			
 			this.databaseName = Properties.getProperty("databaseName").toString();
 		}
 		
 		return databaseName;
 	}
 
 	private String getDatabasePath() {
 		
 		if(databasePath == null || databasePath.equals("")){			
 			databasePath = Properties.getProperty("databasePath").toString();
 		}
 		
 		return databasePath;
 	}
 	
 	private String getDatabaseUsername() {
 		
 		if(username == null || username.equals("")){			
 			username = Properties.getProperty("username").toString();
 		}
 		
 		return username;
 	}
 	
 	private String getDatabasePassword() {
 		
 		if(password == null || password.equals("")){
 			this.password = Properties.getProperty("password").toString();
 		}
 		
 		return password;
 	}
 	
 	private void updateValues(Point source, Point closestPointInRange){
 		
 		for (String propertyName : Properties.getPropertiesOfInterestDatatypeMapping().keySet()) {
 			
 			double weightedAvg = getAverage(source, closestPointInRange, propertyName);
 			
 			LOGGER.debug("Average: " + weightedAvg);
 			
 			source.setProperty(propertyName, weightedAvg);
 		}
 		
 	}
 	
 	private double getAverage(Point source, Point point,
 			String propertyName) {
 
 		Object sourceNumberObject = source.getProperty(propertyName);
 
 		if (sourceNumberObject instanceof Number) {
 
 			Number sourceValue = (Number) sourceNumberObject;
 
 			double summedUpValues = sourceValue.doubleValue();
 
 			Object pointNumberObject = point.getProperty(propertyName);
 			if (pointNumberObject instanceof Number) {
 				summedUpValues = summedUpValues
 						+ ((Number) pointNumberObject).doubleValue();
 			}
 
 			return summedUpValues / 2;
 		}
 
 		LOGGER.debug("source property not a number");
 		
 		return -1;
 	}
 	
 	private Point createPointFromCurrentResultSetEntry(ResultSet resultSet) throws SQLException{
 		
 		String resultID = resultSet.getString("id");
 		
 		Map<String, Object> propertyMap = new HashMap<>();
 		
 		for (String propertyName : Properties.getPropertiesOfInterestDatatypeMapping().keySet()) {
 			
 			Class<?> propertyClass = Properties.getPropertiesOfInterestDatatypeMapping().get(propertyName);
 			
 			Object value = null;
 			
 			if(propertyClass.equals(Double.class)){
 				value = resultSet.getDouble(propertyName.toLowerCase());
 			}
 			
 			propertyMap.put(propertyName, value);
 		}
 		
 		String resultLastContributingTrack = "";
 		
 		try {
 			resultLastContributingTrack = resultSet.getString("lastcontributingtrack");			
 		} catch (SQLException e) {
 			LOGGER.info("Column lastcontributingtrack not available.");
 			LOGGER.error(e.getMessage());
 		}
 		
 		int resultNumberOfContributingPoints = 1;
 		
 		try {			
 			resultNumberOfContributingPoints = resultSet.getInt("numberofpoints");
 		} catch (SQLException e) {
 			LOGGER.info("Column id numberofpoints not available. Defaulting to 1.");
 			LOGGER.error(e.getMessage());
 		}
 		
 		int resultNumberOfContributingTracks = 1;
 		
 		try {			
 			resultNumberOfContributingTracks = resultSet.getInt("numberofcontributingtracks");			
 		} catch (SQLException e) {
 			LOGGER.info("Column id numberofcontributingtracks not available. Defaulting to 1.");
 			LOGGER.error(e.getMessage());
 		}
 		
 		if(resultLastContributingTrack == null || resultLastContributingTrack.isEmpty()){
 			/*
 			 * point seems to be original point, try accessing trackid column 
 			 */
 			try {
 				resultLastContributingTrack = resultSet.getString("trackid");			
 			} catch (SQLException e) {
 				LOGGER.info("Column trackid not available.");
 				LOGGER.error(e.getMessage());
 			}
 		}
 		
 		double[] resultXY = new double[2];
 		
 		try {
 			
 			String resultGeomAsText = resultSet.getString("text_geom");
 			
 			resultXY = Utils.convertWKTPointToXY(resultGeomAsText);
 			
 		} catch (SQLException e) {
 			LOGGER.info("Column text_geom not available.");
 			LOGGER.error(e.getMessage());
 		}
 		
 		Point resultPoint = new InMemoryPoint(resultID, resultXY[0], resultXY[1], propertyMap, resultNumberOfContributingPoints, resultNumberOfContributingTracks, resultLastContributingTrack);
 		
 		return resultPoint;
 		
 	}
 	
     private void createPointsFromJSON(ArrayList<?> features, String trackID, Geometry bbox) {
 	    	    	
     	for (Object object : features) {
 
 			if (object instanceof LinkedHashMap<?, ?>) {
 				LinkedHashMap<?, ?> featureMap = (LinkedHashMap<?, ?>) object;
 
 				Object geometryObject = featureMap.get("geometry");
 				
 				double[] coordinatesXY = new double[2];
 				
 				if (geometryObject instanceof LinkedHashMap<?, ?>) {
 					coordinatesXY = Utils.getCoordinatesXYFromJSON((LinkedHashMap<?, ?>) geometryObject);
 				}
 				
 				Coordinate pointCoordinate = new Coordinate(coordinatesXY[0], coordinatesXY[1]);
 				
 				if (bbox != null) {
 					if (!bbox.contains(Utils.geometryFactory
 							.createPoint(pointCoordinate))) {
 						continue;
 					}
 				}
 				Object propertiesObject = featureMap.get("properties");				
 				
 				if (propertiesObject instanceof LinkedHashMap<?, ?>) {
 					LinkedHashMap<?, ?> propertiesMap = (LinkedHashMap<?, ?>) propertiesObject;
 
 					String id = String.valueOf(propertiesMap.get("id"));
 					
 					Object phenomenonsObject = propertiesMap.get("phenomenons");
 
 					if (phenomenonsObject instanceof LinkedHashMap<?, ?>) {
 						LinkedHashMap<?, ?> phenomenonsMap = (LinkedHashMap<?, ?>) phenomenonsObject;
 
 						Map<String, Object> propertiesofInterestMap = Utils.getValuesFromFromJSON(phenomenonsMap);
 						
 						insertPoint(id, coordinatesXY[0], coordinatesXY[1], trackID, propertiesofInterestMap, true);
 					}
 				}
 			}
 		}
     	
     	String sqlQuery = selectAllMeasurementsofTrackQueryString + "'" + trackID + "';";
     	
     	ResultSet rs = executeQueryStatement(sqlQuery);
     	
     	trackDatabasePointsResultSetMap.put(trackID, rs);
     	
 	}
 	
 	private boolean createConnection() {
 
 		connectionURL = "jdbc:postgresql:" + getDatabasePath() + "/"
 				+ getDatabaseName();
 
 		java.util.Properties props = new java.util.Properties();
 
 		props.setProperty("create", "true");
 		props.setProperty("user", getDatabaseUsername());
 		props.setProperty("password", getDatabasePassword());
 		conn = null;
 		try {
 			conn = DriverManager.getConnection(connectionURL, props);
 			conn.setAutoCommit(false);
 			LOGGER.info("Connected to measurement database.");
 		} catch (SQLException e) {
 			LOGGER.error("Could not connect to or create the database.", e);
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean createTable(String creationString, String tableName) {
 		try {
 			ResultSet rs;
 			DatabaseMetaData meta = conn.getMetaData();
 			rs = meta
 					.getTables(null, null, tableName, new String[] { "TABLE" });
 			if (!rs.next()) {
 				LOGGER.info("Table " + tableName + " does not yet exist.");
 				Statement st = conn.createStatement();
 				st.executeUpdate(creationString);
 
 				conn.commit();
 
 				meta = conn.getMetaData();
 
 				rs = meta.getTables(null, null, tableName,
 						new String[] { "TABLE" });
 				if (rs.next()) {
 					LOGGER.info("Succesfully created table " + tableName + ".");
 					
 					/*
 					 * add geometry column
 					 */
 					executeStatement(addGeometryColumnToAggregated_MeasurementsTableString.replace(table_name_exp, tableName));
 					
 				} else {
 					LOGGER.error("Could not create table " + tableName + ".");
 					return false;
 				}
 			}
 		} catch (SQLException e) {
 			LOGGER.error("Connection to the Postgres database failed: "
 					+ e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	private boolean executeStatement(String statement) {
 		try {
 			Statement st = conn.createStatement();
 			st.execute(statement);
 
 			conn.commit();
 
 		} catch (SQLException e) {
 			LOGGER.error("Execution of the following statement failed: "
 					+ statement + " cause: " + e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	private ResultSet executeQueryStatement(String statement) {
 		try {
 			Statement st = conn.createStatement();
 			ResultSet resultSet = st.executeQuery(statement);
 
 			conn.commit();
 
 			return resultSet;
 
 		} catch (SQLException e) {
 			LOGGER.error("Execution of the following statement failed: "
 					+ statement + " cause: " + e.getMessage());
 			return null;
 		}
 	}
 
 	private boolean executeUpdateStatement(String statement) {
 		try {
 			Statement st = conn.createStatement();
 			st.executeUpdate(statement);
 
 			conn.commit();
 
 		} catch (SQLException e) {
 			LOGGER.error("Execution of the following statement failed: "
 					+ statement + " cause: " + e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	private boolean insertPoint(String id, double x, double y, String trackID,
 			int numberOfPoints, int numberOfTracks, Map<String, Object> propertiesofInterestMap, boolean checkIfExists) {
 
 		if(checkIfExists){
 			String statement = "select * from aggregated_measurements where id='" + id + "';";
 			
 			ResultSet rs = executeQueryStatement(statement);
 			
 			try {
 				if(rs.next()){
 					return false;
 				}
 			} catch (SQLException e) {
 				LOGGER.error("Could not check if row with id=" + id + " and trackID=" + trackID + " exists.", e);
 				return false;
 			}
 		}
 		
 		String statement = createInsertPointStatement(id, x, y, trackID,
 				numberOfPoints, numberOfTracks, propertiesofInterestMap);
 
 		return executeUpdateStatement(statement);
 	}
 
 	private String createInsertPointStatement(String id, double x, double y,
 			String trackID, int numberOfPoints, int numberOfTracks,
 			Map<String, Object> propertiesofInterestMap) {
 
 		String columnNameString = "( id, the_geom, numberofpoints, numberofcontributingtracks, lastcontributingtrack, ";
 		String valueString = "( '" + id + "', ST_GeomFromText('POINT(" + x
 				+ " " + y + ")', " + spatial_ref_sys + "), " + numberOfPoints
 				+ ", " + numberOfTracks + ", '" + trackID + "', ";
 
 		Iterator<String> propertyNameIterator = Properties
 				.getPropertiesOfInterestDatatypeMapping().keySet().iterator();
 
 		while (propertyNameIterator.hasNext()) {
 			String propertyName = (String) propertyNameIterator.next();
 
 			columnNameString = columnNameString.concat(propertyName
 					.toLowerCase());
 			valueString = valueString.concat(String
 					.valueOf(propertiesofInterestMap.get(propertyName)));
 
 			if (propertyNameIterator.hasNext()) {
 				columnNameString = columnNameString.concat(", ");
 				valueString = valueString.concat(", ");
 			} else {
 				columnNameString = columnNameString.concat(")");
 				valueString = valueString.concat(")");
 			}
 		}
 
 		String statement = "INSERT INTO " + aggregated_MeasurementsTableName
 				+ columnNameString + "VALUES" + valueString + ";";
 
 		return statement;
 	}
 
 	private boolean insertPoint(String id, double x, double y, String trackID, Map<String, Object> propertiesofInterestMap, boolean checkIfExists) {
 
 		if(checkIfExists){
 			String statement = "select * from original_measurements where id='" + id + "' and trackid='" + trackID + "';";
 			
 			ResultSet rs = executeQueryStatement(statement);
 			
 			try {
 				if(rs.next()){
 					return false;
 				}
 			} catch (SQLException e) {
 				LOGGER.error("Could not check if row with id=" + id + " and trackID=" + trackID + " exists.", e);
 				return false;
 			}
 		}
 		
 		String statement = createInsertPointStatement(id, x, y, trackID,
 				propertiesofInterestMap);
 
 		return executeUpdateStatement(statement);
 	}
 	
 	private String createInsertPointStatement(String id, double x, double y,
 			String trackID,	Map<String, Object> propertiesofInterestMap) {
 
 		String columnNameString = "( id, the_geom, trackid, ";
 		String valueString = "( '" + id + "', ST_GeomFromText('POINT(" + x
 				+ " " + y + ")', " + spatial_ref_sys + "), '" + trackID + "', ";
 
 		Iterator<String> propertyNameIterator = Properties
 				.getPropertiesOfInterestDatatypeMapping().keySet().iterator();
 
 		while (propertyNameIterator.hasNext()) {
 			String propertyName = (String) propertyNameIterator.next();
 
 			columnNameString = columnNameString.concat(propertyName
 					.toLowerCase());
 			valueString = valueString.concat(String
 					.valueOf(propertiesofInterestMap.get(propertyName)));
 
 			if (propertyNameIterator.hasNext()) {
 				columnNameString = columnNameString.concat(", ");
 				valueString = valueString.concat(", ");
 			} else {
 				columnNameString = columnNameString.concat(")");
 				valueString = valueString.concat(")");
 			}
 		}
 
 		String statement = "INSERT INTO " + original_MeasurementsTableName
 				+ columnNameString + "VALUES" + valueString + ";";
 
 		return statement;
 	}
 	
 	
 }
