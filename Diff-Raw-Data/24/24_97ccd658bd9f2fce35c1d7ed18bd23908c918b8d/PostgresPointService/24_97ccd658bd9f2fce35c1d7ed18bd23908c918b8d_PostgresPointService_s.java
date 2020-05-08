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
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.envirocar.analyse.entities.InMemoryPoint;
 import org.envirocar.analyse.entities.Point;
 import org.envirocar.analyse.properties.Properties;
 import org.envirocar.analyse.util.Utils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * <code>Pointservice</code> implementation using a Postgres database to store the aggregated measurements.
  * 
  * @author Benjamin Pross, Matthes Rieke
  *
  */
 public class PostgresPointService implements PointService {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(PostgresPointService.class);
 
 	private Connection conn = null;
 	private String connectionURL = null;
 	private String databaseName;
 	private String databasePath;
 															
 	private String username;
 	private String password;
 	public static String aggregated_MeasurementsTableName = (String) Properties.getProperty("aggregated_MeasurementsTableName");
 	public static String original_MeasurementsTableName = (String) Properties.getProperty("original_MeasurementsTableName");																				
 	public static String measurementRelationsTableName = (String) Properties.getProperty("measurement_relationsTableName");
 	public static String aggregatedTracksTableName = (String) Properties.getProperty("aggregatedTracksTableName");
 	
 	private String spatial_ref_sys = "4326";// TODO from properties
 	private String id_exp = "$id$";
 	private String distance_exp = "$distance$";
 	private String table_name_exp = "$tablename$";
 	private String geomFromText_exp = "$geom_from_text$";
 
 	private final String speed_value_exp = "$speed_value$";
 	private final String speedNumberOfContributingPoints_value_exp = "$speedNumberOfContributingPoints_value$";
 	private final String co2_value_exp = "$co2_value$";
 	private final String co2NumberOfContributingPoints_value_exp = "$co2NumberOfContributingPoints_value$";
 	private final String generalNumberOfContributingPoints_value_exp = "$generalNumberOfContributingPoints_value$";
 	private final String generalnumberOfContributingTracks_value_exp = "$generalnumberOfContributingTracks_value$";
 	private final String lastContributingTrack_value_exp = "$lastContributingTrack_value$";
 	private final String geometryEncoded_value_exp = "$geometryEncoded_value$";
 
 	private String idField = "id";
 	private String generalNumberOfContributingPointsField = "generalnumberofcontributingpoints";
 	private String generalnumberOfContributingTracksField = "generalnumberofcontributingtracks";
 	private String lastContributingTrackField = "lastcontributingtrack";
 	private String co2Field = "co2";
 	private String speedField = "speed";
 	private String co2NumberOfContributingPointsField = "co2numberofcontributingpoints";
 	private String speedNumberOfContributingPointsField = "speednumberofcontributingpoints";
 	private String trackIDField = "trackid";
 	private String geometryEncodedField = "the_geom";
 	private String geometryPlainTextField = "text_geom";
 	private String distField = "dist";
 	private String aggregated_measurement_idField = "aggregated_measurement_id";
 	private String aggregation_dateField = "aggregation_date";
 	
 	private SimpleDateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 	
 	private final String pgCreationString = "CREATE TABLE "
 			+ aggregated_MeasurementsTableName + " ("
			+ idField + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
 			+ generalNumberOfContributingPointsField + " INTEGER, "
 			+ generalnumberOfContributingTracksField + " INTEGER,"
 			+ lastContributingTrackField + " VARCHAR(24)," 
 			+ co2Field + " DOUBLE PRECISION," 
 			+ co2NumberOfContributingPointsField + " INTEGER, "
 			+ speedField + " DOUBLE PRECISION, " 
 			+ speedNumberOfContributingPointsField + " INTEGER)";
 	
 	private final String pgMeasurementRelationsTableCreationString = "CREATE TABLE "
 			+ measurementRelationsTableName + " ("
			+ idField + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
			+ aggregated_measurement_idField + " VARCHAR(24), "
 			+ "CONSTRAINT measurement_relations_aggregated_measurement_id_fkey FOREIGN KEY (" + aggregated_measurement_idField + ") "
 			+ "REFERENCES " + aggregated_MeasurementsTableName + " (" + idField + ") MATCH SIMPLE "
 			+ "ON UPDATE NO ACTION ON DELETE NO ACTION)";
 	
 	private final String pgAggregatedTracksTableCreationString = "CREATE TABLE "
 			+ aggregatedTracksTableName + " ("
 			+ idField + " VARCHAR(24) NOT NULL PRIMARY KEY, "
 			+ aggregation_dateField + " timestamp with time zone)";
 	
 	private String pgNearestNeighborCreationString = "select h." + idField + ", h." + speedField + ", h." + co2Field + ", h." + generalNumberOfContributingPointsField + ", h." + speedNumberOfContributingPointsField + ", h." + co2NumberOfContributingPointsField + ", h." + generalnumberOfContributingTracksField + ", h." + lastContributingTrackField + ", ST_AsText(h.the_geom) as " + geometryPlainTextField + ", ST_distance(" + geomFromText_exp + ",h.the_geom) as " + distField + " from " + aggregated_MeasurementsTableName + " h "
 			+ "where ST_DWithin(" + geomFromText_exp + ",h." + geometryEncodedField + ","
 			+ distance_exp + ") " + "order by " + distField + " ASC;";
 
 	private final String addGeometryColumnToTableString = "SELECT AddGeometryColumn( '"
 			+ table_name_exp
 			+ "', '" + geometryEncodedField + "', " + spatial_ref_sys + ", 'POINT', 2 );";
 	
 	private final String selectAllAggregatedMeasurementsString = "select h." + idField + ", h." + speedField + ", h." + co2Field + ", h." + generalNumberOfContributingPointsField + ", h." + speedNumberOfContributingPointsField + ", h." + co2NumberOfContributingPointsField + ", h." + generalnumberOfContributingTracksField + ", h." + lastContributingTrackField + ", ST_AsText(h.the_geom) as " + geometryPlainTextField + " from " + aggregated_MeasurementsTableName + " h; ";
 	
 	private final String deletePointFromTableString = "delete from " + table_name_exp + " where " + idField + "=";
 	
 	private final String updateAggregatedMeasurementString = "UPDATE " + aggregated_MeasurementsTableName + " SET " + speedField + " = " + speed_value_exp + ", " + speedNumberOfContributingPointsField + " = " + speedNumberOfContributingPoints_value_exp + ", " + co2Field + " = " + co2_value_exp + ", " + co2NumberOfContributingPointsField + " = " + co2NumberOfContributingPoints_value_exp + ", " + generalNumberOfContributingPointsField + " = " + generalNumberOfContributingPoints_value_exp + ", " + generalnumberOfContributingTracksField + " = " + generalnumberOfContributingTracks_value_exp + ", " + lastContributingTrackField + " = '" + lastContributingTrack_value_exp + "', " + geometryEncodedField + " = " + geometryEncoded_value_exp + " WHERE " + idField + " = '" + id_exp + "';";
 	
 	private Geometry bbox;
 	
 	public PostgresPointService() {
 		this(null);
 	}
 	
 	public PostgresPointService(Geometry bbox) {
 		this.bbox = bbox;
 		
 		try {
 			createConnection();
 		} catch (ClassNotFoundException e) {
 			throw new IllegalStateException(e);
 		}
 		createTable(pgCreationString, aggregated_MeasurementsTableName, true);
 		createTable(pgMeasurementRelationsTableCreationString, measurementRelationsTableName, false);		
 		createTable(pgAggregatedTracksTableCreationString, aggregatedTracksTableName, false);
 	}
 
 
 	@Override
 	public Point getNearestNeighbor(Point point, double distance) {
 
 		String queryString = pgNearestNeighborCreationString.replace(distance_exp, "" + distance).replace(geomFromText_exp, createST_GeometryFromTextStatement(point.getX(), point.getY()));
 
 		ResultSet resultSet = executeQueryStatement(queryString);
 
 		try {
 
 			if (resultSet != null) {
 
 				while (resultSet.next()) {
 
 					String resultID = Integer.toString(resultSet.getInt(idField));
 
 					Map<String, Object> propertyMap = new HashMap<>();
 					
 					Map<String, Integer> propertyPointsUsedForAggregationMap = new HashMap<>();
 
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
 						
 						int pointsUsedForAggregation = resultSet.getInt(propertyName
 									.toLowerCase() + "numberofcontributingpoints");
 								
 						propertyPointsUsedForAggregationMap.put(propertyName, pointsUsedForAggregation);
 						
 					}
 
 					String resultLastContributingTrack = resultSet
 							.getString(lastContributingTrackField);
 
 					int resultNumberOfContributingPoints = resultSet
 							.getInt(generalNumberOfContributingPointsField);
 
 					int resultNumberOfContributingTracks = resultSet
 							.getInt(generalnumberOfContributingTracksField);
 
 					String resultGeomAsText = resultSet.getString(geometryPlainTextField);
 
 					double[] resultXY = Utils
 							.convertWKTPointToXY(resultGeomAsText);
 
 					Point resultPoint = new InMemoryPoint(resultID,
 							resultXY[0], resultXY[1], propertyMap,
 							resultNumberOfContributingPoints,
 							resultNumberOfContributingTracks,
 							resultLastContributingTrack, propertyPointsUsedForAggregationMap);
 
 					return resultPoint;
 				}
 
 			}
 		} catch (SQLException e) {
 			LOGGER.error("Could not query nearest neighbor of " + point.getID(), e);
 		}
 
 		return null;
 	}
 	
 	@Override
 	public boolean updateResultSet(String idOfPointToBeUpdated,
 			Point newPoint) {
 		
 		String updateString = updateAggregatedMeasurementString;
 		
 		updateString = updateString.replace(speed_value_exp, "" + newPoint.getProperty("Speed"));
 		updateString = updateString.replace(speedNumberOfContributingPoints_value_exp, "" + newPoint.getNumberOfPointsUsedForAggregation("Speed"));
 		updateString = updateString.replace(co2_value_exp, "" + newPoint.getProperty("CO2"));
 		updateString = updateString.replace(co2NumberOfContributingPoints_value_exp, "" + newPoint.getNumberOfPointsUsedForAggregation("CO2"));
 		updateString = updateString.replace(generalNumberOfContributingPoints_value_exp, "" + newPoint.getNumberOfPointsUsedForAggregation());
 		updateString = updateString.replace(generalnumberOfContributingTracks_value_exp, "" + newPoint.getNumberOfTracksUsedForAggregation());
 		updateString = updateString.replace(lastContributingTrack_value_exp, newPoint.getLastContributingTrack());
 		updateString = updateString.replace(geometryEncoded_value_exp, createST_GeometryFromTextStatement(newPoint.getX(), newPoint.getY()));
 		updateString = updateString.replace(id_exp, idOfPointToBeUpdated);
 				
 		return executeUpdateStatement(updateString);
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
 	public Point aggregate(Point point, Point nearestNeighborPoint, String trackId) {
 		
 		Point aggregatedPoint = new InMemoryPoint(point);
 		
 		updateValues(aggregatedPoint, nearestNeighborPoint);
 		
 		/*
 		 * distance weighting
 		 */
 		double averagedX = ((nearestNeighborPoint.getX() * nearestNeighborPoint.getNumberOfPointsUsedForAggregation()) + aggregatedPoint.getX()) / (nearestNeighborPoint.getNumberOfPointsUsedForAggregation() + 1);
 		double averagedY = ((nearestNeighborPoint.getY() * nearestNeighborPoint.getNumberOfPointsUsedForAggregation()) + aggregatedPoint.getY()) / (nearestNeighborPoint.getNumberOfPointsUsedForAggregation() + 1);
 				
 		aggregatedPoint.setNumberOfPointsUsedForAggregation(nearestNeighborPoint.getNumberOfPointsUsedForAggregation()+1);
 		
 		aggregatedPoint.setX(averagedX);
 		aggregatedPoint.setY(averagedY);
 		
 		LOGGER.debug(aggregatedPoint.getLastContributingTrack() + " vs. " + nearestNeighborPoint.getLastContributingTrack());
 		
 		if(!aggregatedPoint.getLastContributingTrack().equals(nearestNeighborPoint.getLastContributingTrack())){
 			aggregatedPoint.setNumberOfTracksUsedForAggregation(nearestNeighborPoint.getNumberOfTracksUsedForAggregation() +1);
 		}else{
 			aggregatedPoint.setNumberOfTracksUsedForAggregation(nearestNeighborPoint.getNumberOfTracksUsedForAggregation());
 		}
 		
 		LOGGER.debug("Aggregated: " + aggregatedPoint.getID() + " and " + nearestNeighborPoint.getID());
 		LOGGER.debug("NumberOfPoints " + aggregatedPoint.getNumberOfPointsUsedForAggregation());
 		
 		insertMeasurementRelation(point.getID(), Integer.parseInt(nearestNeighborPoint.getID()));
 		
 		/*
 		 * store result in DB
 		 */
 		updateResultSet(nearestNeighborPoint.getID(),
 				aggregatedPoint);
 		
 		return aggregatedPoint;
 	}
 
 	@Override
 	public boolean isFitForAggregation(Point point) {
 
 		boolean result = false;
 		
 		for (String propertyName : Properties.getPropertiesOfInterestDatatypeMapping().keySet()) {
 			
 			Object numberObject = point.getProperty(propertyName);
 			
 			if(numberObject instanceof Number){
 				result = result || !isNumberObjectNullOrZero((Number) numberObject);
 			}else{
 				/*
 				 * not a number, we cannot aggregate this currently
 				 */
 				result = result || false;
 			}
 		}
 		
 		/*
 		 * also check for bbox
 		 */
 		if (bbox != null) {
 			Coordinate pointCoordinate = new Coordinate(point.getX(), point.getY());
 			
 			if (!bbox.contains(Utils.geometryFactory
 						.createPoint(pointCoordinate))) {
 				return false;
 			}
 		}
 		
 		return result;
 	}
 
 	@Override
 	public void addToResultSet(Point newPoint) {
 		
 		int newId;
 		try {
 			newId = insertPoint(newPoint);
 		} catch (SQLException e) {
 			LOGGER.warn(e.getMessage(), e);
 			return;
 		}
 		
 		insertMeasurementRelation(newPoint.getID(), newId);
 	}
 	
 	private String createST_GeometryFromTextStatement(double x, double y){
 		
 		return "ST_GeomFromText('POINT(" + x
 				+ " " + y + ")', " + spatial_ref_sys + ")";		
 	}
 	
 	private boolean insertMeasurementRelation(String originalID, int aggregatedID){
 	
 		String statement = "INSERT INTO " + measurementRelationsTableName
 				+ "(" + idField + ", " + aggregated_measurement_idField + ") VALUES ('"  + originalID + "', " + aggregatedID + ");";
 		
 		return executeUpdateStatement(statement);
 	}
 	
 	
 	private boolean isNumberObjectNullOrZero(Number numberObject) {
 
 		Number sourceValue = (Number) numberObject;
 
 		double value = sourceValue.doubleValue();
 
 		if (value == 0.0) {
 			return true;
 		}
 
 		return false;
 	}
 	
 	public boolean removePoint(String pointID, String tableName){
 		return executeUpdateStatement(deletePointFromTableString.replace(table_name_exp, tableName).concat("'" + pointID + "';"));
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
 			
 			double weightedAvg = getWeightedAverage(source, closestPointInRange, propertyName);
 			
 			LOGGER.debug("Average: " + weightedAvg);
 			
 			source.setProperty(propertyName, weightedAvg);
 		}
 		
 	}
 	
 	private double getWeightedAverage(Point source, Point closestPointInRange,
 			String propertyName) {
 
 		Object sourceNumberObject = source.getProperty(propertyName);
 
 		if (sourceNumberObject instanceof Number) {
 
 			Number sourceValue = (Number) sourceNumberObject;
 
 			double summedUpValues = sourceValue.doubleValue();
 
 			LOGGER.debug("Value of " + propertyName + " of point " + source.getID() + " = " + summedUpValues);
 			
 			Object pointNumberObject = closestPointInRange.getProperty(propertyName);
 			if (pointNumberObject instanceof Number) {
 				
 				double d = ((Number) pointNumberObject).doubleValue();
 				
 				int numberOfPointsUsedForAggregation = closestPointInRange.getNumberOfPointsUsedForAggregation(propertyName);
 				
 				/*
 				 * sort out values of 0.0
 				 */
 				if(d == 0.0){
 					LOGGER.debug("Value for aggregation is 0.0, will not use this.");
 					source.setNumberOfPointsUsedForAggregation(numberOfPointsUsedForAggregation, propertyName);
 					return summedUpValues;
 				}
 				
 				double weightedAvg = ((d * numberOfPointsUsedForAggregation) + summedUpValues) / (numberOfPointsUsedForAggregation + 1);
 				
 				source.setNumberOfPointsUsedForAggregation(numberOfPointsUsedForAggregation + 1, propertyName);
 				
 				LOGGER.debug("Value of " + propertyName + " of point " + closestPointInRange.getID() + " = " + d);
 
 				return weightedAvg;
 			}
 		}
 
 		LOGGER.debug("Source property " + propertyName + " is not a number.");
 		
 		return -1;
 	}
 	
 	private Point createPointFromCurrentResultSetEntry(ResultSet resultSet) throws SQLException{
 		
 		String resultID = resultSet.getString(idField);
 
 		Map<String, Object> propertyMap = new HashMap<>();
 		
 		Map<String, Integer> propertyPointsUsedForAggregationMap = new HashMap<>();
 
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
 			
 			try{
 			
 			int pointsUsedForAggregation = resultSet.getInt(propertyName
 						.toLowerCase() + "numberofcontributingpoints");
 					
 			propertyPointsUsedForAggregationMap.put(propertyName, pointsUsedForAggregation);
 			}catch(Exception e){
 				LOGGER.info("Column " + propertyName.toLowerCase() + "numberofcontributingpoints" + " not available.");
 				LOGGER.info(e.getMessage());
 			}
 			
 		}
 		
 		String resultLastContributingTrack = "";
 		
 		try {
 			resultLastContributingTrack = resultSet.getString(lastContributingTrackField);			
 		} catch (SQLException e) {
 			LOGGER.info("Column " + lastContributingTrackField + " not available.");
 			LOGGER.info(e.getMessage());
 		}
 		
 		int resultNumberOfContributingPoints = 1;
 		
 		try {			
 			resultNumberOfContributingPoints = resultSet.getInt(generalNumberOfContributingPointsField);
 		} catch (SQLException e) {
 			LOGGER.info("Column " + generalNumberOfContributingPointsField + " not available. Defaulting to 1.");
 			LOGGER.info(e.getMessage());
 		}
 		
 		int resultNumberOfContributingTracks = 1;
 		
 		try {			
 			resultNumberOfContributingTracks = resultSet.getInt(generalnumberOfContributingTracksField);			
 		} catch (SQLException e) {
 			LOGGER.info("Column " + generalnumberOfContributingTracksField + " not available. Defaulting to 1.");
 			LOGGER.info(e.getMessage());
 		}
 		
 		if(resultLastContributingTrack == null || resultLastContributingTrack.isEmpty()){
 			/*
 			 * point seems to be original point, try accessing trackid column 
 			 */
 			try {
 				resultLastContributingTrack = resultSet.getString(trackIDField);			
 			} catch (SQLException e) {
 				LOGGER.info("Column " + trackIDField + " not available.");
 				LOGGER.info(e.getMessage());
 			}
 		}
 		
 		double[] resultXY = new double[2];
 		
 		try {
 			
 			String resultGeomAsText = resultSet.getString(geometryPlainTextField);
 			
 			resultXY = Utils.convertWKTPointToXY(resultGeomAsText);
 			
 		} catch (SQLException e) {
 			LOGGER.info("Column " + geometryPlainTextField + " not available.");
 			LOGGER.error(e.getMessage());
 		}
 		
 		Point resultPoint = new InMemoryPoint(resultID, resultXY[0], resultXY[1], propertyMap, resultNumberOfContributingPoints, resultNumberOfContributingTracks, resultLastContributingTrack, propertyPointsUsedForAggregationMap);
 		
 		return resultPoint;
 		
 	}
 	
 	
 	private boolean createConnection() throws ClassNotFoundException {
 		Class.forName("org.postgresql.Driver");
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
 
 	private boolean createTable(String creationString, String tableName, boolean addToGeometryColumnTable) {
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
 					if(addToGeometryColumnTable){
 					    executeStatement(addGeometryColumnToTableString.replace(table_name_exp, tableName));
 					}
 					
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
 		Statement st = null;
 		try {
 			st = conn.createStatement();
 			st.execute(statement);
 
 		} catch (SQLException e) {
 			LOGGER.error("Execution of the following statement failed: "
 					+ statement + " cause: " + e.getMessage());
 			return false;
 		} finally {
 			try {
 				conn.commit();
 			} catch (SQLException e) {
 				LOGGER.warn(e.getMessage(), e);
 			}
 		}
 		return true;
 	}
 
 	private ResultSet executeQueryStatement(String statement) {
 		Statement st = null;
 		try {
 			st = conn.createStatement();
 			ResultSet resultSet = st.executeQuery(statement);
 
 			return resultSet;
 
 		} catch (SQLException e) {
 			LOGGER.error("Execution of the following statement failed: "
 					+ statement + " cause: " + e.getMessage());
 			return null;
 		} finally {
 			try {
 				conn.commit();
 			} catch (SQLException e) {
 				LOGGER.warn(e.getMessage(), e);
 			}
 		}
 	}
 
 	private boolean executeUpdateStatement(String statement) {
 		Statement st = null;
 		try {
 			st = conn.createStatement();
 			st.executeUpdate(statement);
 			
 		} catch (SQLException e) {
 			LOGGER.error("Execution of the following statement failed: "
 					+ statement + " cause: " + e.getMessage());
 			return false;
 		} finally {
 			try {
 				conn.commit();
 				st.close();
 			} catch (SQLException e) {
 				LOGGER.warn(e.getMessage(), e);
 			}
 		}
 		return true;
 	}
 
 	private int insertPoint(Point point) throws SQLException {
 		PreparedStatement statement = createInsertPointStatement(point);
 		int affectedRows = statement.executeUpdate();
 
         if (affectedRows == 0) {
             throw new SQLException("Creating user failed, no rows affected.");
         }
 
         try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
             if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
             }
             else {
                 throw new SQLException("Creating user failed, no ID obtained.");
             }
         }
 	}
 	
 
 	private PreparedStatement createInsertPointStatement(Point point) throws SQLException {
 		String columnNameString = "( "+
 			geometryEncodedField +", "+
 			generalNumberOfContributingPointsField +", "+
 			generalnumberOfContributingTracksField +", "+
 			lastContributingTrackField +", ";
 		
 		Iterator<String> propertyNameIterator = Properties
 				.getPropertiesOfInterestDatatypeMapping().keySet().iterator();
 		
 		List<Object> values = new ArrayList<>();
 		
 //		String valueString = "( ST_GeomFromText('POINT(" + point.getX() + " " + point.getY() + ")', " + spatial_ref_sys + "), " +
 //				point.getNumberOfPointsUsedForAggregation() + ", " +
 //				point.getNumberOfTracksUsedForAggregation() + ", '" +
 //				point.getLastContributingTrack() + "', ";
 		
		values.add("ST_GeomFromText('POINT(" + point.getX() + " " + point.getY() + ")', " + spatial_ref_sys + ")");
 		values.add(new Integer(point.getNumberOfPointsUsedForAggregation()));
 		values.add(new Integer(point.getNumberOfTracksUsedForAggregation()));
 		values.add(point.getLastContributingTrack());
 
 		while (propertyNameIterator.hasNext()) {
 			String propertyName = (String) propertyNameIterator.next();
 
 			columnNameString = columnNameString.concat(propertyName
 					.toLowerCase());
 			
 			columnNameString = columnNameString.concat(", ");
 			
 			columnNameString = columnNameString.concat(propertyName
 					.toLowerCase() + "numberofcontributingpoints");
 			
 			Object o = point.getProperty(propertyName);
 			
 			values.add(o == null? null : Double.parseDouble(o.toString()));
 			
 			int v = point.getNumberOfPointsUsedForAggregation(propertyName);
 			
 			values.add(new Integer(v));
 
 			if (propertyNameIterator.hasNext()) {
 				columnNameString = columnNameString.concat(", ");
 			} else {
 				columnNameString = columnNameString.concat(")");
 			}
 		}
 		
 		StringBuilder sb = new StringBuilder();
		sb.append("(");
 		for (int i = 0; i < values.size(); i++) {
 			sb.append("?,");
 		}
 		sb.delete(sb.length()-1, sb.length());
 		sb.append(")");
 		
 		String statement = "INSERT INTO " + aggregated_MeasurementsTableName
 				+ columnNameString + "VALUES" + sb.toString() + ";";
 		
 		PreparedStatement result = conn.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
 
		int index = 0;
 		for (Object object : values) {
 			if (object instanceof String) {
 				result.setString(index++, object.toString());
 			}
 			else if (object instanceof Integer) {
 				result.setInt(index++, (int) object);
 			}
 			else if (object instanceof Double) {
 				result.setDouble(index++, (double) object);
 			}
 		}
 		
 		return result;
 	}	
 	
 	public boolean insertTrackIntoAggregatedTracksTable(String trackID) {
 		if (trackAlreadyAggregated(trackID)) {
 			return false;
 		}
 		
 		String statement = "INSERT INTO " + aggregatedTracksTableName + "("
 				+ idField + ", " + aggregation_dateField + ") VALUES ('"
 				+ trackID + "', '" + iso8601DateFormat.format(new Date())
 				+ "');";
 
 		return executeUpdateStatement(statement);
 	}
 
 	@Override
 	public boolean trackAlreadyAggregated(String trackID) {
 		String alreadyThere = "SELECT * FROM "+ aggregatedTracksTableName
 				+" WHERE " + idField +" = '"+trackID+"'";
 		
 		ResultSet rs = executeQueryStatement(alreadyThere);
 		try {
 			if (rs.next()) {
 				return true;
 			}
 		} catch (SQLException e) {
 			LOGGER.warn(e.getMessage(), e);
 		}
 		
 		return false;
 	}
 	
 }
