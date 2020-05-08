 package model.persistence;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import model.Action;
 import model.Action.ActionType;
 import model.Barcode;
 import model.HomeInventoryTracker;
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductGroup;
 import model.ProductQuantity;
 import model.StorageUnit;
 import model.Unit;
 import model.report.ExpiredItemsReport;
 import model.report.NMonthSupplyReport;
 import model.report.NoticesReport;
 import model.report.ProductStatisticsReport;
 import model.report.RemovedItemsReport;
 
 /**
  * Observes the model and persists all changes made to it an a local MySQL database
  * 
  * @author Matthew
  * @version 1.0 -- Snell CS 340 Phase 4.0
  * 
  */
 public class RdbDao extends InventoryDao implements Observer {
 	private static final String dbFile = "inventory.sqlite";
 
 	// Used when persisting to DB
 	private Map<Object, Integer> referenceToId;
 
 	// Used when loading from DB
 	private Map<Integer, Product> productIdToReference;
 	private Map<Integer, Item> itemIdToReference;
 	private Map<Integer, ProductContainer> productContainerIdToReference;
 	private Map<Integer, ProductQuantity> productQuantityIdToReference;
 	private Map<Integer, Unit> unitIdToReference;
 	private Map<String, Integer> unitToId;
 
 	public RdbDao() {
 		referenceToId = new HashMap<Object, Integer>();
 
 		productIdToReference = new HashMap<Integer, Product>();
 		itemIdToReference = new HashMap<Integer, Item>();
 		productContainerIdToReference = new HashMap<Integer, ProductContainer>();
 		productQuantityIdToReference = new HashMap<Integer, ProductQuantity>();
 		unitIdToReference = new HashMap<Integer, Unit>();
 
 		int index = 0;
 		unitToId = new HashMap<String, Integer>();
 		unitToId.put(Unit.COUNT.toDBString(), index++);
 		unitToId.put(Unit.POUNDS.toDBString(), index++);
 		unitToId.put(Unit.OUNCES.toDBString(), index++);
 		unitToId.put(Unit.GRAMS.toDBString(), index++);
 		unitToId.put(Unit.KILOGRAMS.toDBString(), index++);
 		unitToId.put(Unit.GALLONS.toDBString(), index++);
 		unitToId.put(Unit.QUARTS.toDBString(), index++);
 		unitToId.put(Unit.PINTS.toDBString(), index++);
 		unitToId.put(Unit.FLUID_OUNCES.toDBString(), index++);
 		unitToId.put(Unit.LITERS.toDBString(), index++);
 	}
 
 	@Override
 	public void applicationClose(HomeInventoryTracker hit) {
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
 
 			results = statement.executeQuery("SELECT * FROM ProductContainer "
 					+ "INNER JOIN ProductGroup "
 					+ "ON ProductContainer.ProductContainer_id = "
 					+ "ProductGroup.ProductGroup_id");
 			while (results.next()) {
 				Integer id = results.getInt("ProductGroup_id");
 				String name = results.getString("name");
 				Integer threeMonthSupplyId = results.getInt("ProductQuantity_id");
 				ProductQuantity threeMonthSupply = productQuantityIdToReference
 						.get(threeMonthSupplyId);
 				Integer parentId = results.getInt("parent");
 				ProductContainer parent = productContainerIdToReference.get(parentId);
 
 				ProductGroup pg = new ProductGroup(name, threeMonthSupply,
 						threeMonthSupply.getUnits(), parent, hit.getProductContainerManager());
 
 				productContainerIdToReference.put(id, pg);
 				referenceToId.put(pg, id);
 			}
 
 			Map<Integer, Set<ProductContainer>> productToContainer = new HashMap<Integer, Set<ProductContainer>>();
 			results = statement.executeQuery("SELECT * FROM Product_has_ProductContainer");
 			while (results.next()) {
 				Integer productId = results.getInt("Product_id");
 				Integer containerId = results.getInt("ProductContainer_id");
 
 				ProductContainer container = productContainerIdToReference.get(containerId);
 
 				Set<ProductContainer> containers = productToContainer.get(productId);
 				if (containers == null)
 					containers = new HashSet<ProductContainer>();
 
 				containers.add(container);
 				productToContainer.put(productId, containers);
 			}
 
 			results = statement.executeQuery("SELECT * FROM Product");
 			while (results.next()) {
 				Integer id = results.getInt("Product_id");
 				Date creationDate = results.getDate("creationDate");
 				String barcode = results.getString("barcode");
 				String description = results.getString("description");
 				Integer quantityId = results.getInt("ProductQuantity_id");
 				Integer shelfLife = results.getInt("shelfLife");
 				Integer tms = results.getInt("threeMonthSupply");
 				ProductQuantity quantity = productQuantityIdToReference.get(quantityId);
 
 				Set<ProductContainer> containers = productToContainer.get(id);
 
 				Product p = null;
 				if (containers == null) {
 					ProductContainer container = new StorageUnit("INVALID",
 							hit.getProductContainerManager());
 					p = new Product(barcode, description, creationDate, shelfLife, tms,
 							quantity, container, hit.getProductManager());
 					// make sure to remove from default containers
 					for (ProductContainer pc : p.getProductContainers()) {
 						pc.remove(p);
 						p.removeContainer(pc);
 					}
 					hit.getProductContainerManager().unmanage(container);
 				} else {
 					for (ProductContainer pc : containers) {
 						if (p == null) {
 							p = new Product(barcode, description, creationDate, shelfLife,
 									tms, quantity, pc, hit.getProductManager());
 						} else {
 							pc.add(p);
 							p.addContainer(pc);
 						}
 					}
 				}
 				productIdToReference.put(id, p);
 				referenceToId.put(p, id);
 			}
 
 			results = statement.executeQuery("SELECT * FROM Item");
 			while (results.next()) {
 				Integer itemId = results.getInt("Item_id");
 				Date entryDate = results.getDate("entryDate");
 				Date exitTime = results.getDate("exitTime");
 				String barcode = results.getString("barcode");
 				Date expirationDate = results.getDate("expirationDate");
 				Integer productId = results.getInt("Product_id");
 				Integer containerId = results.getInt("ProductContainer_id");
 				Product product = productIdToReference.get(productId);
 				ProductContainer container = productContainerIdToReference.get(containerId);
 				Barcode code = new Barcode(barcode);
 
 				boolean removeProductFromContainer = !container.contains(product);
 
 				Item i = new Item(code, product, container, entryDate, hit.getItemManager());
 				i.setExpirationDate(expirationDate, this);
 
 				if (exitTime != null) {
 					container.remove(i, hit.getItemManager());
 					i.setExitDate(exitTime, this);
 				}
 
 				if (removeProductFromContainer) {
 					container.remove(product);
 					product.removeContainer(container);
 				}
 
 				itemIdToReference.put(itemId, i);
 				referenceToId.put(i, itemId);
 			}
 
 			results = statement.executeQuery("SELECT * FROM Report");
 			while (results.next()) {
 				Integer reportId = results.getInt("Report_id");
 				Date runtime = results.getDate("Report_runtime");
 
 				switch (reportId) {
 				case 0: // ExpiredItemsReport
 					ExpiredItemsReport eir = new ExpiredItemsReport(
 							hit.getProductContainerManager());
 					eir.setLastRuntime(runtime, this);
 					hit.getReportManager().setExpiredItemsReport(eir, this);
 					break;
 				case 1:// NMonthSupplyReport
 					NMonthSupplyReport nmsr = new NMonthSupplyReport(hit.getProductManager(),
 							hit.getProductContainerManager());
 					nmsr.setLastRuntime(runtime, this);
 					hit.getReportManager().setNMonthSupplyReport(nmsr, this);
 					break;
 
 				case 2:// NoticesReport
 					NoticesReport nr = new NoticesReport(hit.getProductContainerManager());
 					nr.setLastRuntime(runtime, this);
 					hit.getReportManager().setNoticesReport(nr, this);
 
 					break;
 				case 3:// ProductStatisticsReport
 					ProductStatisticsReport psr = new ProductStatisticsReport(
 							hit.getItemManager(), hit.getProductManager());
 					psr.setLastRuntime(runtime, this);
 					hit.getReportManager().setProductStatiscticsReport(psr, this);
 					break;
 
 				case 4: // RemovedItemsReport
 					RemovedItemsReport rir = new RemovedItemsReport(hit.getItemManager());
 					rir.setLastRuntime(runtime, this);
 					hit.getReportManager().setRemovedItemsReport(rir, this);
 					break;
 
 				default:
 					throw new Exception("Unknown report id: " + reportId);
 				}
 			}
 
 			hit.getItemManager().addObserver(this);
 			hit.getProductContainerManager().addObserver(this);
 			hit.getProductManager().addObserver(this);
 
 			return hit;
 		} catch (Exception e) {
 			if (f != null) {
 				f.delete();
 			}
 			e.printStackTrace();
 
 			HomeInventoryTracker h = new HomeInventoryTracker();
 
 			h.getItemManager().addObserver(this);
 			h.getProductContainerManager().addObserver(this);
 			h.getProductManager().addObserver(this);
 
 			return h;
 		}
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		Action action = (Action) arg1;
 		ActionType type = action.getAction();
 		Object obj = action.getObject();
 
 		switch (type) {
 		case CREATE:
 			if (obj instanceof Item)
 				insertItem((Item) obj);
 
 			if (obj instanceof Product)
 				insertProduct((Product) obj);
 
 			if (obj instanceof ProductContainer)
 				insertProductContainer((ProductContainer) obj);
 
 			break;
 		case DELETE:
 			if (obj instanceof Item)
 				updateItem((Item) obj); // don't delete the item, just update the row.
 
 			if (obj instanceof Product)
 				deleteProduct((Product) obj);
 
 			if (obj instanceof ProductContainer)
 				deleteProductContainer((ProductContainer) obj);
 			break;
 		case EDIT:
 			if (obj instanceof Item)
 				updateItem((Item) obj);
 
 			if (obj instanceof Product)
 				updateProduct((Product) obj);
 
 			if (obj instanceof ProductContainer)
 				updateProductContainer((ProductContainer) obj);
 			break;
 		case INVISIBLE_EDIT:
 			break;
 		case MOVE:
 			if (obj instanceof Item)
 				moveItem((Item) obj);
 
 			if (obj instanceof Product)
 				moveProduct((Product) obj);
 
 			if (obj instanceof ProductContainer)
 				moveProductContainer((ProductContainer) obj);
 			break;
 		case DEEP_DELETE:
 			if (obj instanceof Item)
 				deleteItem((Item) obj);
 
 			if (obj instanceof Product)
 				deepDeleteProduct((Product) obj);
 
 			if (obj instanceof ProductContainer)
 				throw new IllegalArgumentException("Cannot deep delete a product container");
 		}
 
 	}
 
 	private void createSchema() throws SQLException {
 		Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 
 		Statement statement = connection.createStatement();
 		statement.setQueryTimeout(30);
 
 		statement.executeUpdate("DROP TABLE IF EXISTS `Unit`");
 		statement.executeUpdate("CREATE TABLE IF NOT EXISTS `Unit` (`Unit_id` INTEGER "
 				+ "NOT NULL PRIMARY KEY AUTOINCREMENT , `Unit_name` VARCHAR(45) NULL )");
 
 		Set<String> units = unitToId.keySet();
 		for (String unitString : units) {
 			statement.executeUpdate("INSERT INTO Unit VALUES(" + unitToId.get(unitString)
 					+ ",\"" + unitString + "\")");
 		}
 
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
 				+ "  `ProductGroup_id` INTEGER NOT NULL PRIMARY KEY ,"
 				+ "  `ProductQuantity_id` INTEGER NULL ," + "  `parent` INTEGER NOT NULL ,"
 				+ "    FOREIGN KEY (`ProductQuantity_id` )"
 				+ "    REFERENCES `ProductQuantity` (`ProductQuantity_id` ),"
 				+ "    FOREIGN KEY (`parent` )"
 				+ "    REFERENCES `ProductContainer` (`ProductContainer_id` ),"
 				+ "    FOREIGN KEY (`ProductGroup_id` )"
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
 
 	private void deepDeleteProduct(Product p) {
 		try {
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			PreparedStatement statement = connection
 					.prepareStatement("DELETE FROM Product_has_ProductContainer "
 							+ "WHERE Product_id=?");
 
 			Integer productId = referenceToId.get(p);
 			statement.setInt(1, productId);
 			statement.execute();
 
 			statement = connection.prepareStatement("DELETE FROM Product WHERE Product_id=?");
 			statement.setInt(1, productId);
 			statement.execute();
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private void deleteItem(Item i) {
 		try {
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			PreparedStatement statement = connection
 					.prepareStatement("DELETE FROM Item WHERE Item_id=?");
 			Integer itemId = referenceToId.get(i);
 			statement.setInt(1, itemId);
 			statement.execute();
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private void deleteProduct(Product p) {
 		try {
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			PreparedStatement statement = connection
 					.prepareStatement("DELETE FROM Product_has_ProductContainer "
 							+ "WHERE Product_id=? AND ProductContainer_id=?");
 
 			Integer productId = referenceToId.get(p);
 			// get all containers the model finds this product in
 			Set<ProductContainer> newContainers = p.getProductContainers();
 
 			// get all containers the db finds this product in
 			PreparedStatement st = connection
 					.prepareStatement("SELECT * FROM Product_has_ProductContainer");
 			st.execute();
 			ResultSet rs = st.getResultSet();
 			List<ProductContainer> oldContainers = new ArrayList<ProductContainer>();
 			while (rs.next()) {
 				ProductContainer container = productContainerIdToReference.get(rs.getInt(1));
 				if (container != null)
 					oldContainers.add(container);
 			}
 
 			if (oldContainers.size() == 0)
 				return;
 
 			// remove all the containers that should still be there
 			for (ProductContainer container : newContainers) {
 				oldContainers.remove(container);
 			}
 
 			// get the only one left -- that's the one to remove
 			ProductContainer containerToRemove = oldContainers.get(0);
 			Integer containerId = referenceToId.get(containerToRemove);
 
 			statement.setInt(1, productId);
 			statement.setInt(2, containerId);
 
 			statement.execute();
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private void deleteProductContainer(ProductContainer pc) {
 		Integer containerId = referenceToId.get(pc);
 		try {
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			PreparedStatement statement = connection
 					.prepareStatement("DELETE FROM ProductContainer WHERE ProductContainer_id=?");
 			statement.setInt(1, containerId);
 			statement.execute();
 
 			if (pc instanceof StorageUnit) {
 				statement = connection
 						.prepareStatement("DELETE FROM StorageUnit WHERE StorageUnit_id=?");
 				statement.setInt(1, containerId);
 				statement.execute();
 			}
 
 			if (pc instanceof ProductGroup) {
 				statement = connection
 						.prepareStatement("DELETE FROM ProductGroup WHERE ProductGroup_id=?");
 				statement.setInt(1, containerId);
 				statement.execute();
 			}
 
 			referenceToId.remove(pc);
 			productContainerIdToReference.remove(containerId);
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 
 	}
 
 	private void insertItem(Item i) {
 		try {
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 
 			PreparedStatement selectStmt = connection
 					.prepareStatement("SELECT * FROM Item WHERE barcode=?");
 			selectStmt.setString(1, i.getBarcode());
 			selectStmt.execute();
 			ResultSet results = selectStmt.getResultSet();
 			if (results.next()) {
 				Integer itemId = results.getInt(1);
 				results.close();
 				Integer pcId = referenceToId.get(i.getContainer());
 
 				PreparedStatement updateStatement = connection
 						.prepareStatement("UPDATE Item SET ProductContainer_id=?"
 								+ "WHERE Item_id=?");
 
 				updateStatement.setInt(1, pcId);
 				updateStatement.setInt(2, itemId);
 				updateStatement.execute();
 				return;
 			}
 			results.close();
 
 			PreparedStatement statement = connection
 					.prepareStatement("INSERT INTO Item VALUES(null,?,?,?,?,?,?)");
 
 			Integer productId = referenceToId.get(i.getProduct());
 			Integer containerId = referenceToId.get(i.getContainer());
 
 			statement.setLong(1, i.getEntryDate().getTime());
 			if (i.getExitTime() != null) {
 				statement.setLong(2, i.getExitTime().getTime());
 			} else {
 				statement.setNull(2, java.sql.Types.DATE);
 			}
 			statement.setString(3, i.getBarcode());
 			if (i.getExpirationDate() != null) {
 				statement.setLong(4, i.getExpirationDate().getTime());
 			} else {
 				statement.setNull(4, java.sql.Types.DATE);
 			}
 			statement.setInt(5, productId);
 			statement.setInt(6, containerId);
 
 			statement.execute();
 
 			ResultSet set = statement.getGeneratedKeys();
 			int key = -1;
 			while (set.next())
 				key = set.getInt(1);
 			referenceToId.put(i, key);
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	private void insertProduct(Product p) {
 		long creationDate = p.getCreationDate().getTime();
 		String barcode = p.getBarcode();
 		String description = p.getDescription();
 		ProductQuantity pq = p.getProductQuantity();
 		int shelfLife = p.getShelfLife();
 		int tms = p.getThreeMonthSupply();
 
 		try {
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			insertProductQuantity(pq, conn);
 
 			int pqId = referenceToId.get(pq);
 
 			String insertStatement = "INSERT INTO Product VALUES(null, " + creationDate + ","
 					+ "\"" + barcode + "\"," + "\"" + description + "\"," + pqId + ","
 					+ shelfLife + "," + tms + ")";
 			Statement statement = conn.createStatement();
 			statement.executeUpdate(insertStatement);
 			ResultSet set = statement.getGeneratedKeys();
 			int key = -1;
 			while (set.next())
 				key = set.getInt(1);
 			referenceToId.put(p, key);
 
 			Set<ProductContainer> containers = p.getProductContainers();
 			for (ProductContainer pc : containers) {
 				Integer containerId = referenceToId.get(pc);
 				insertStatement = "INSERT INTO Product_has_ProductContainer VALUES(" + key
 						+ "," + containerId + ")";
 				statement.executeUpdate(insertStatement);
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void insertProductContainer(ProductContainer pc) {
 		String name = pc.getName();
 		String insertStatement = "INSERT INTO ProductContainer VALUES(null, \"" + name + "\")";
 		Connection conn;
 		try {
 			conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			Statement statement = conn.createStatement();
 			statement.executeUpdate(insertStatement);
 			ResultSet set = statement.getGeneratedKeys();
 			int key = -1;
 			while (set.next())
 				key = set.getInt(1);
 			if (pc instanceof StorageUnit) {
 				insertStatement = "INSERT INTO StorageUnit VALUES (" + key + ")";
 				statement.executeUpdate(insertStatement);
 				referenceToId.put(pc, key);
 				productContainerIdToReference.put(key, pc);
 			} else if (pc instanceof ProductGroup) {
 				insertProductQuantity(((ProductGroup) pc).getThreeMonthSupply(), conn);
 
 				Integer parentId = referenceToId.get(((ProductGroup) pc).getContainer());
 				insertStatement = "INSERT INTO ProductGroup VALUES(" + key + ", "
 						+ referenceToId.get(((ProductGroup) pc).getThreeMonthSupply()) + ", "
 						+ parentId + ")";
 				statement.executeUpdate(insertStatement);
 				referenceToId.put(pc, key);
 				productContainerIdToReference.put(key, pc);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void insertProductQuantity(ProductQuantity pq, Connection conn) {
 		try {
 			Statement statement = conn.createStatement();
 			String insertStatement = "INSERT INTO ProductQuantity VALUES(null, "
 					+ pq.getQuantity() + ", " + unitToId.get(pq.getUnits().toDBString()) + ")";
 			statement.executeUpdate(insertStatement);
 			ResultSet set = statement.getGeneratedKeys();
 			int key = -1;
 			while (set.next())
 				key = set.getInt(1);
 
 			referenceToId.put(pq, key);
 			productQuantityIdToReference.put(key, pq);
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void moveItem(Item item) {
 		System.out.println(item);
 	}
 
 	private void moveProduct(Product product) {
 		try {
 			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 
 			PreparedStatement statement = connection
 					.prepareStatement("DELETE FROM Product_has_ProductContainer "
 							+ "WHERE Product_id=? AND ProductContainer_id=?");
 
 			Integer productId = referenceToId.get(product);
 
 			// get all containers the model finds this product in
 			Set<ProductContainer> newContainers = product.getProductContainers();
 
 			// get all containers the db finds this product in
 			PreparedStatement st = connection
 					.prepareStatement("SELECT * FROM Product_has_ProductContainer");
 			st.execute();
 			ResultSet rs = st.getResultSet();
 			List<ProductContainer> oldContainers = new ArrayList<ProductContainer>();
 			while (rs.next()) {
 				ProductContainer container = productContainerIdToReference.get(rs.getInt(1));
 				if (container != null)
 					oldContainers.add(container);
 			}
 
 			// remove all the containers that should still be there
 			for (ProductContainer container : newContainers) {
 				oldContainers.remove(container);
 			}
 
 			// get the only one left -- that's the one to remove
 			ProductContainer containerToRemove = oldContainers.get(0);
 			Integer containerId = referenceToId.get(containerToRemove);
 
 			statement.setInt(1, productId);
 			statement.setInt(2, containerId);
 
 			statement.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		System.out.println(product);
 	}
 
 	private void moveProductContainer(ProductContainer pc) {
 		System.out.println(pc);
 	}
 
 	private void updateItem(Item i) {
 		Integer itemId = referenceToId.get(i);
 		if (itemId != null) {
 			try {
 				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 				PreparedStatement statement;
 				if (i.getContainer() != null) { // moved containers
 					statement = connection
 							.prepareStatement("UPDATE Item SET entryDate=?, exitTime=?,"
 									+ "barcode=?, expirationDate=?, "
 									+ "Product_id=?, ProductContainer_id=?"
 									+ "WHERE Item_id=?");
 
 					Integer containerId = referenceToId.get(i.getContainer());
 					statement.setInt(6, containerId);
 					statement.setInt(7, itemId);
 				} else { // removed -- update everything but leave the reference to container
 					statement = connection
 							.prepareStatement("UPDATE Item SET entryDate=?, exitTime=?,"
 									+ "barcode=?, expirationDate=?, Product_id=?"
 									+ "WHERE Item_id=?");
 					statement.setInt(6, itemId);
 				}
 
 				Integer productId = referenceToId.get(i.getProduct());
 
 				statement.setLong(1, i.getEntryDate().getTime());
 				if (i.getExitTime() != null) {
 					statement.setLong(2, i.getExitTime().getTime());
 				} else {
 					statement.setNull(2, java.sql.Types.DATE);
 				}
 				statement.setString(3, i.getBarcode());
 				if (i.getExpirationDate() != null) {
 					statement.setLong(4, i.getExpirationDate().getTime());
 				} else {
 					statement.setNull(4, java.sql.Types.DATE);
 				}
 				statement.setInt(5, productId);
 
 				statement.execute();
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		} else {
 			System.out.println("Tried to edit nonexistant Item " + i.getBarcode());
 		}
 	}
 
 	private void updateProduct(Product p) {
 		Integer productId = referenceToId.get(p);
 		if (productId != null) {
 			try {
 				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 
 				// check if we need to update the product quantity
 				PreparedStatement quantityStatement = connection
 						.prepareStatement("SELECT * FROM Product WHERE Product_id=?");
 				quantityStatement.setInt(1, productId);
 				quantityStatement.execute();
 				ResultSet quantityResults = quantityStatement.getResultSet();
 				Integer pqId = null;
 				while (quantityResults.next()) {
 					pqId = quantityResults.getInt(5);
 					ProductQuantity quantity = productQuantityIdToReference.get(pqId);
 					if (quantity.getQuantity() == p.getProductQuantity().getQuantity()
 							&& quantity.getUnits() == p.getProductQuantity().getUnits()) {
 						// no change to product quantity
 					} else {
 						insertProductQuantity(p.getProductQuantity(), connection);
 						pqId = referenceToId.get(p.getProductQuantity());
 					}
 				}
 
 				PreparedStatement statement = connection
 						.prepareStatement("UPDATE Product SET creationDate=?,"
 								+ "description=?,"
 								+ "ProductQuantity_id=?, shelfLife=?, threeMonthSupply=? "
 								+ "WHERE Product_id=?");
 
 				statement.setLong(1, p.getCreationDate().getTime());
 				statement.setString(2, p.getDescription());
 				statement.setInt(3, pqId);
 				statement.setInt(4, p.getShelfLife());
 				statement.setInt(5, p.getThreeMonthSupply());
 				statement.setInt(6, productId);
 				statement.execute();
 
 				// update products in product containers
 				PreparedStatement st = connection
 						.prepareStatement("SELECT * FROM Product_has_ProductContainer");
 				st.execute();
 				ResultSet rs = st.getResultSet();
 				List<ProductContainer> oldContainers = new ArrayList<ProductContainer>();
 				while (rs.next()) {
 					ProductContainer container = productContainerIdToReference.get(rs
 							.getInt(1));
 					oldContainers.add(container);
 				}
 
 				Set<ProductContainer> newContainers = p.getProductContainers();
 
 				// remove all the containers that should still be there
 				for (ProductContainer container : oldContainers) {
 					if (container != null)
 						newContainers.remove(container);
 				}
 
 				// add the last newcontainer
 				for (ProductContainer newContainer : newContainers) {
 					Integer newContainerId = referenceToId.get(newContainer);
 					PreparedStatement insertStatement = connection
 							.prepareStatement("INSERT INTO Product_has_ProductContainer "
 									+ "VALUES(?, ?)");
 					insertStatement.setInt(1, productId);
 					insertStatement.setInt(2, newContainerId);
 					try {
 						insertStatement.execute();
 					} catch (SQLException ex) {// link already exists
 						System.out.println("p_id=" + productId + " pc_id=" + newContainerId);
 					}
 				}
 
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		} else {
 			System.out.println("Tried to update nonexistant Product " + p.getDescription());
 		}
 	}
 
 	private void updateProductContainer(ProductContainer pc) {
 		Integer containerId = referenceToId.get(pc);
 		if (containerId != null) {
 			try {
 				Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 
 				// check if we need to update the product quantity
 				PreparedStatement statement = connection
 						.prepareStatement("UPDATE ProductContainer SET name=?"
 								+ " WHERE ProductContainer_id=?");
 				statement.setString(1, pc.getName());
 				statement.setInt(2, containerId);
 				statement.execute();
 
 				if (pc instanceof ProductGroup) {
 					// modify ProductGroup
 					ProductGroup pg = (ProductGroup) pc;
 					PreparedStatement quantityStatement = connection
 							.prepareStatement("SELECT * FROM ProductGroup"
 									+ " WHERE ProductGroup_id=?");
 					quantityStatement.setInt(1, containerId);
 					quantityStatement.execute();
 					ResultSet quantityR = quantityStatement.getResultSet();
 					Integer quantityId = null;
 					while (quantityR.next()) {
 						quantityId = quantityR.getInt(2);
 						ProductQuantity quantity = productQuantityIdToReference
 								.get(quantityId);
 						if (quantity.getQuantity() == pg.getThreeMonthSupply().getQuantity()
 								&& quantity.getUnits() == pg.getThreeMonthSupply().getUnits()) {
 							// no change to product quantity
 						} else {
 							insertProductQuantity(pg.getThreeMonthSupply(), connection);
 							quantityId = referenceToId.get(pg.getThreeMonthSupply());
 						}
 					}
 
 					Integer parentId = referenceToId.get(pg.getContainer());
 
 					statement = connection.prepareStatement("UPDATE ProductGroup"
 							+ " SET ProductQuantity_id=?, parent=?"
 							+ " WHERE ProductGroup_id=?");
 					statement.setInt(1, quantityId);
 					statement.setInt(2, parentId);
 					statement.setInt(3, containerId);
 					statement.execute();
 				}
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		} else {
 			System.out.println("Editing a nonexistant productcontainer " + pc.getName());
 		}
 	}
 }
