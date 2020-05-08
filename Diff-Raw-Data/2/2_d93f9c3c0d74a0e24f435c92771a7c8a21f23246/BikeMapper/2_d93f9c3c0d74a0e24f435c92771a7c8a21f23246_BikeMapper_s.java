 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model.database;
 
 import model.object.Bike;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import resource.log.ProjectLogger;
 
 
 /**
  *
  * @author Yoldark34 <yoldark@gmail.com>
  */
 public class BikeMapper extends AbstractMapper {
 
 	public ArrayList<Bike> getAllBikes() throws SQLException, ClassNotFoundException {
 		DbConnection adapter = DbConnection.getDbConnection();
 		adapter.executeSelectQuery("Select * from " + DataBaseElements.BIKE);
 		return (ArrayList<Bike>) adapter.getModelsFromRequest(this);
 	}
 
 	public int save(Bike bike) {
 		int nbRows = 0;
 		String query = "";
 		if (bike.getId() != -1) {
 			//Update do not exist for now because Bike have only one field.
 		} else {
 			query = "INSERT INTO " + DataBaseElements.BIKE + " (";
 			//query += "`" + DataBaseElements.BIKE_ID + "`, `";
 			query += ") VALUES (";
 			//query += "'" + bike.getId() + "'";//Autoincrement
 			query += ")";
 		}
 
 		try {
 			DbConnection adapter = DbConnection.getDbConnection();
 			nbRows = adapter.executeUpdateQuery(query);
 		} catch (Exception e) {
 		}
 		return nbRows;
 	}
 
 	public int getAvailableBikesForThisTerminal(int terminalId) {
 		String query;
 		Bike result = new Bike();
 
 		query = "SELECT ";
 		query += "count( * ) AS 'numberOfBikes'";
 		query += " FROM ";
 		query += DataBaseElements.TERMINAL + " " + DataBaseElements.ALIAS_TERMINAL + ", ";
 		query += DataBaseElements.STOCK + " " + DataBaseElements.ALIAS_STOCK + ", ";
 		query += DataBaseElements.STORAGE + " " + DataBaseElements.ALIAS_STORAGE + ", ";
 		query += DataBaseElements.BIKEUSAGETYPE + " " + DataBaseElements.ALIAS_BIKEUSAGETYPE + ", ";
 		query += DataBaseElements.BIKEUSAGE + " " + DataBaseElements.ALIAS_BIKEUSAGE + " ";
 		query += " WHERE ";
 		query += DataBaseElements.ALIAS_STOCK + "." + DataBaseElements.STOCK_ID + " = " + DataBaseElements.ALIAS_STORAGE + "." + DataBaseElements.STORAGE_IDSTOCK;
 		query += " AND ";
 		query += DataBaseElements.ALIAS_STORAGE + "." + DataBaseElements.STORAGE_ID + " = " + DataBaseElements.ALIAS_BIKEUSAGE + "." + DataBaseElements.BIKEUSAGE_IDENDSTORAGE;
 		query += " AND ";
 		query += DataBaseElements.ALIAS_BIKEUSAGETYPE + "." + DataBaseElements.BIKEUSAGETYPE_ID + " = " + DataBaseElements.ALIAS_BIKEUSAGE + "." + DataBaseElements.BIKEUSAGE_IDBIKEUSAGETYPE;
 		query += " AND ";
 		query += DataBaseElements.ALIAS_BIKEUSAGETYPE + "." + DataBaseElements.BIKEUSAGETYPE_NAME + " = '" + DataBaseElements.BikeUsageType.STOCKING + "'";
 		query += " AND ";
 		query += DataBaseElements.ALIAS_BIKEUSAGE + "." + DataBaseElements.BIKEUSAGE_ENDDATE + " is NULL";
 		query += " AND ";
 		query += DataBaseElements.ALIAS_TERMINAL + "." + DataBaseElements.TERMINAL_ID + " = " + terminalId;
 
 		try {
 			DbConnection adapter = DbConnection.getDbConnection();
 			adapter.executeSelectQuery(query);
 			result = (Bike) adapter.getModelFromRequest(this);
 		} catch (SQLException | ClassNotFoundException ex) {
 			ProjectLogger.log(this, Level.SEVERE, "Erreur d'exécution de la requête de la fonction getAvailableBikesForThisTerminal", ex);
 		}
 
 		return result.getNumberOfBikes();
 	}
 
 	public ArrayList<Bike> getRentedBikesForThisTerminal(int terminalId) {
 		String query;
 		ArrayList<Bike> results = new ArrayList<>();
 
 		query = "SELECT ";
		query += DataBaseElements.ALIAS_BIKE + " " + DataBaseElements.BIKE_ID;
 		query += " FROM ";
 		query += DataBaseElements.TERMINAL + " " + DataBaseElements.ALIAS_TERMINAL + ", ";
 		query += DataBaseElements.STOCK + " " + DataBaseElements.ALIAS_STOCK + ", ";
 		query += DataBaseElements.STORAGE + " " + DataBaseElements.ALIAS_STORAGE + ", ";
 		query += DataBaseElements.BIKEUSAGETYPE + " " + DataBaseElements.ALIAS_BIKEUSAGETYPE + ", ";
 		query += DataBaseElements.BIKE + " " + DataBaseElements.ALIAS_BIKE + ", ";
 		query += DataBaseElements.BIKEUSAGE + " " + DataBaseElements.ALIAS_BIKEUSAGE + " ";
 		query += " WHERE ";
 		query += DataBaseElements.ALIAS_STOCK + "." + DataBaseElements.STOCK_ID + " = " + DataBaseElements.ALIAS_STORAGE + "." + DataBaseElements.STORAGE_IDSTOCK;
 		query += " AND ";
 		query += DataBaseElements.ALIAS_STORAGE + "." + DataBaseElements.STORAGE_ID + " = " + DataBaseElements.ALIAS_BIKEUSAGE + "." + DataBaseElements.BIKEUSAGE_IDENDSTORAGE;
 		query += " AND ";
 		query += DataBaseElements.ALIAS_BIKEUSAGETYPE + "." + DataBaseElements.BIKEUSAGETYPE_ID + " = " + DataBaseElements.ALIAS_BIKEUSAGE + "." + DataBaseElements.BIKEUSAGE_IDBIKEUSAGETYPE;
 		query += " AND ";
 		query += DataBaseElements.ALIAS_BIKE + "." + DataBaseElements.BIKE_ID + " = " + DataBaseElements.ALIAS_BIKEUSAGE + "." + DataBaseElements.BIKEUSAGE_IDBIKE;
 		query += " AND ";
 		query += DataBaseElements.ALIAS_BIKEUSAGETYPE + "." + DataBaseElements.BIKEUSAGETYPE_NAME + " = '" + DataBaseElements.BikeUsageType.RENTING + "'";
 		query += " AND ";
 		query += DataBaseElements.ALIAS_BIKEUSAGE + "." + DataBaseElements.BIKEUSAGE_ENDDATE + " is NULL";
 		query += " AND ";
 		query += DataBaseElements.ALIAS_TERMINAL + "." + DataBaseElements.TERMINAL_ID + " = " + terminalId;
 
 		try {
 			DbConnection adapter = DbConnection.getDbConnection();
 			adapter.executeSelectQuery(query);
 			results = (ArrayList<Bike>) adapter.getModelFromRequest(this);
 		} catch (SQLException | ClassNotFoundException ex) {
 			ProjectLogger.log(this, Level.SEVERE, "Erreur d'exécution de la requête de la fonction getAvailableBikesForThisTerminal", ex);
 		}
 
 		return results;
 	}
 
 	@Override
 	public Object populateModel(ResultSet row) throws SQLException {
 		Bike obj = new Bike();
 		if (this.hasColumn(DataBaseElements.BIKE_ID, row)) {
 			obj.setId(row.getInt(DataBaseElements.BIKE_ID));
 		}
 		if (this.hasColumn("numberOfBikes", row)) {
 			obj.setNumberOfBikes(row.getInt("numberOfBikes"));
 		}
 		return obj;
 	}
 
 	@Override
 	Object getEmptyModel() {
 		return new Bike();
 	}
 }
