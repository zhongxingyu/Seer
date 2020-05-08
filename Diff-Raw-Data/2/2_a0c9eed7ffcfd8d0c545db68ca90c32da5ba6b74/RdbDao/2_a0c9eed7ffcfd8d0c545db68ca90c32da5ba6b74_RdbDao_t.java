 package model.persistence;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Observable;
 
 import model.HomeInventoryTracker;
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductQuantity;
 import model.StorageUnit;
 import model.Unit;
 
 /**
  * Observes the model and persists all changes made to it an a local MySQL database
  * 
  * @author Matthew
  * @version 1.0 -- Snell CS 340 Phase 4.0
  * 
  */
 public class RdbDao extends InventoryDao {
 	private static final String dbFile = "inventory.sqlite";
 
 	// Used when persisting to DB
 	private Map<Object, Integer> referenceToId;
 
 	// Used when loading from DB
 	private Map<Integer, Product> productIdToReference;
 	private Map<Integer, Item> itemIdToReference;
 	private Map<Integer, ProductContainer> productContainerIdToReference;
 	private Map<Integer, ProductQuantity> productQuantityIdToReference;
 	private Map<Integer, Unit> unitIdToReference;
 
 	public RdbDao() {
 		referenceToId = new HashMap<Object, Integer>();
 
 		productIdToReference = new HashMap<Integer, Product>();
 		itemIdToReference = new HashMap<Integer, Item>();
 		productContainerIdToReference = new HashMap<Integer, ProductContainer>();
 		productQuantityIdToReference = new HashMap<Integer, ProductQuantity>();
 		unitIdToReference = new HashMap<Integer, Unit>();
 	}
 
 	@Override
 	public void applicationClose(HomeInventoryTracker hit) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public HomeInventoryTracker loadHomeInventoryTracker() {
 		File f = null;
 		HomeInventoryTracker hit = new HomeInventoryTracker();
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			f = new File(dbFile);
 			if (!f.exists()) {
 				createSchema();
 			}
 
 			Class.forName("org.sqlite.JDBC");
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			Statement statement = connection.createStatement();
 			ResultSet results = statement.executeQuery("SELECT * FROM ProductContainer "
 					+ "INNER JOIN StorageUnit "
 					+ "ON ProductContainer.ProductContainer_id=StorageUnit.StorageUnit_id");
 			while (results.next()) {
 				Integer id = results.getInt("ProductContainer_id");
 				String name = results.getString("name");
 
 				StorageUnit su = new StorageUnit(name, hit.getProductContainerManager());
 
 				productContainerIdToReference.put(id, su);
 				referenceToId.put(su, id);
 			}
 
 			results = statement.executeQuery("SELECT * FROM Unit");
 			while (results.next()) {
 				String unitName = results.getString("Unit_name");
 				Integer unitId = results.getInt("Unit_id");
 				Unit u = Unit.valueOf(unitName);
 				unitIdToReference.put(unitId, u);
 			}
 
 			results = statement.executeQuery("SELECT * FROM ProductQuantity");
 			while (results.next()) {
 				Integer pqId = results.getInt("ProductQuantity_id");
 				float q = results.getFloat("quantity");
 				Integer unitId = results.getInt("Unit_id");
 
 				Unit u = unitIdToReference.get(unitId);
				ProductQuantity quantity = new ProductQuantity(q, u);
 				referenceToId.put(quantity, pqId);
 				productQuantityIdToReference.put(pqId, quantity);
 			}
 
 			return hit;
 		} catch (Exception e) {
 			if (f != null) {
 				f.delete();
 			}
 			e.printStackTrace();
 			return new HomeInventoryTracker();
 		}
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void createSchema() throws SQLException {
 		Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 
 		Statement statement = connection.createStatement();
 		statement.setQueryTimeout(30);
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `Unit`");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS `Unit` (`Unit_id` INTEGER "
 				+ "NOT NULL PRIMARY KEY AUTOINCREMENT , `Unit_name` VARCHAR(45) NULL )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `ProductQuantity`");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS `ProductQuantity` ("
 				+ "`ProductQuantity_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT ,"
 				+ "`quantity` FLOAT NULL ,`Unit_id` INTEGER NULL ,"
 				+ "FOREIGN KEY (`Unit_id` )" + "REFERENCES `Unit` (`Unit_id` ) )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `Product`");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS `Product` ("
 				+ "  `Product_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT ,"
 				+ "  `creationDate` DATE NULL ," + "  `barcode` VARCHAR(15) NULL ,"
 				+ "  `description` TEXT NULL ," + "  `ProductQuantity_id` INTEGER NULL ,"
 				+ "  `shelfLife` INTEGER NULL ," + "  `threeMonthSupply` INTEGER NULL ,"
 				+ "    FOREIGN KEY (`ProductQuantity_id` )"
 				+ "    REFERENCES `ProductQuantity` (`ProductQuantity_id` ) )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `ProductContainer`");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS `ProductContainer` ("
 				+ "  `ProductContainer_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT ,"
 				+ "  `name` TEXT NULL )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `Product_has_ProductContainer`");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS `Product_has_ProductContainer` ("
 				+ "  `Product_id` INTEGER NOT NULL ,"
 				+ "  `ProductContainer_id` INTEGER NOT NULL ,"
 				+ "  PRIMARY KEY (`Product_id`, `ProductContainer_id`) ,"
 				+ "    FOREIGN KEY (`Product_id` )"
 				+ "    REFERENCES `Product` (`Product_id` ),"
 				+ "    FOREIGN KEY (`ProductContainer_id` )"
 				+ "    REFERENCES `ProductContainer` (`ProductContainer_id` ) )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `Item`");
 
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS `Item` ("
 				+ "  `Item_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT ,"
 				+ "  `entryDate` DATE NULL ," + "  `exitTime` DATETIME NULL ,"
 				+ "  `barcode` VARCHAR(45) NULL ," + "  `expirationDate` DATE NULL ,"
 				+ "  `Product_id` INTEGER NULL ,"
 				+ "  `ProductContainer_id` INTEGER NOT NULL ,"
 				+ "    FOREIGN KEY (`Product_id` )"
 				+ "    REFERENCES `Product` (`Product_id` ),"
 				+ "    FOREIGN KEY (`ProductContainer_id` )"
 				+ "    REFERENCES `ProductContainer` (`ProductContainer_id` ) )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `ProductGroup`");
 		statement.executeUpdate("CREATE  TABLE IF NOT EXISTS `ProductGroup` ("
 				+ "  `ProductContainer_id` INTEGER NOT NULL PRIMARY KEY ,"
 				+ "  `ProductQuantity_id` INTEGER NULL ," + "  `parent` INTEGER NOT NULL ,"
 				+ "    FOREIGN KEY (`ProductQuantity_id` )"
 				+ "    REFERENCES `ProductQuantity` (`ProductQuantity_id` ),"
 				+ "    FOREIGN KEY (`parent` )"
 				+ "    REFERENCES `ProductContainer` (`ProductContainer_id` ),"
 				+ "    FOREIGN KEY (`ProductContainer_id` )"
 				+ "    REFERENCES `ProductContainer` (`ProductContainer_id` ) )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `StorageUnit`");
 		statement.executeUpdate("CREATE  TABLE IF NOT EXISTS `StorageUnit` ("
 				+ "  `StorageUnit_id` INTEGER NOT NULL PRIMARY KEY ,"
 				+ "    FOREIGN KEY (`StorageUnit_id` )"
 				+ "    REFERENCES `ProductContainer` (`ProductContainer_id` ) )");
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `Report`");
 		statement.executeUpdate("CREATE  TABLE IF NOT EXISTS `Report` ("
 				+ "  `Report_id` INTEGER NOT NULL PRIMARY KEY ,"
 				+ "  `Report_runtime` DATETIME NULL )");
 
 		connection.close();
 	}
 }
