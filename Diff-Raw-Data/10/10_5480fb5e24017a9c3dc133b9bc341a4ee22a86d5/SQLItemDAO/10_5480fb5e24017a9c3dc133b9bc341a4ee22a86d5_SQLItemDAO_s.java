 /**
  * 
  */
 package model.storage.SQLDAOs;
 
 import model.common.IModel;
 import model.item.Item;
 import model.item.ItemVault;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hamcrest.SelfDescribing;
 import org.joda.time.DateTime;
 
 import model.storage.IStorageDAO;
 import model.storage.SQLDAOFactory;
 
 import common.Result;
 
 /**
  * Provides the functionality of accessing the stored information for an item.
  */
 public class SQLItemDAO implements IStorageDAO {
 
 	private SQLDAOFactory _factory = new SQLDAOFactory();
 	private Connection _connection;
 	private ItemVault _vault = ItemVault.getInstance();
 	
 	/* (non-Javadoc)
 	 * @see model.storage.IStorageDAO#insert(model.common.IModel)
 	 */
 	@Override
 	public Result insert(IModel model) {
 		PreparedStatement statement;
 		Item item = (Item) model;
 		try {
 			String query = "INSERT INTO item (id,productId,barcode,entryTime,exitTime,deleted) VALUES (?,?,?,?,?,?);";
 			statement = _factory.getConnection().prepareStatement(query);
 			fillStatementFromItem(statement, item);
 			statement.executeUpdate();
 		} catch (SQLException e) {
 			return new Result(false, e.getMessage());
 		}
 		return new Result(true);
 	}
 
 	private void fillStatementFromItem(PreparedStatement statement, Item item) throws SQLException {
 		statement.setInt(1, item.getId());
 		statement.setInt(2, item.getProductId());
 		statement.setString(3, item.getBarcodeString());
 		statement.setLong(4, new Long(item.getEntryDate().getMillis()));
 		if(item.getExitDate() != null)
 			statement.setLong(5, new Long(item.getExitDate().getMillis()));
 		statement.setBoolean(6, item.getDeleted());
 	}
 
 	/* (non-Javadoc)
 	 * @see model.storage.IStorageDAO#update(model.common.IModel)
 	 */
 	@Override
 	public Result update(IModel model) {
 		PreparedStatement statement;
 		Item item = (Item) model;
 		try {
 			String query = "UPDATE item SET productId=?,barcode=?,entryTime=?,exitTime=?,deleted=? where id=?";
 			statement = _factory.getConnection().prepareStatement(query);
 			fillStatementFromItem(statement, item);
 			statement.executeUpdate();
 		} catch (SQLException e) {
 			return new Result(false, e.getMessage());
 		}
 		return new Result(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see model.storage.IStorageDAO#delete(model.common.IModel)
 	 */
 	@Override
 	public Result delete(IModel model) {
 		PreparedStatement statement;
 		Item item = (Item) model;
 		try {
 			String query = "DELETE FROM item WHERE id=?;";
 			statement = _factory.getConnection().prepareStatement(query);
 			statement.setInt(1, item.getId());
 			statement.executeUpdate();
 		} catch (SQLException e) {
 			return new Result(false, e.getMessage());
 		}
 		return new Result(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see model.storage.IStorageDAO#get(int)
 	 */
 	@Override
 	public IModel get(int id) {
 		PreparedStatement statement;
 		Item item = null;
 		try {
 			String query = "SELECT productId,entryTime,exitTime,deleted FROM item WHERE id=?;";
 			statement = _factory.getConnection().prepareStatement(query);
 			statement.setInt(1, id);
 			ResultSet rSet = statement.executeQuery();
 			while(rSet.next()){
 				item = new Item();
 				item.setId(id);
 				item.setProductId(rSet.getInt(1));
 				item.setEntryDate(new DateTime(rSet.getLong(2)));
 				item.setExitDate(new DateTime(rSet.getLong(3)));
 				item.setDeleted(rSet.getBoolean(4));
 				item.setValid(true);
 			}
 		} catch (SQLException e) {
 			return null;
 		}
 		return item;
 	}
 
 	@Override
 	public Result loadAllData() {
 		_vault.clear();
 		PreparedStatement statement;
 		try {
 			String query = "SELECT id,productId,entryTime,exitTime,deleted,barcode FROM item;";
 			statement = _factory.getConnection().prepareStatement(query);
 			ResultSet rSet = statement.executeQuery();
 			while(rSet.next()){
 				Item item = populateItemFromResultSet(rSet);
 				Result result = item.save();
 				assert(result.getStatus());
 			}
 		} catch (SQLException e) {
 			return new Result(false, e.getMessage());
 		}
 		return new Result(true);
 	}
 
 	private Item populateItemFromResultSet(ResultSet rSet) throws SQLException {
 		Item item = new Item();
 		item.setId(rSet.getInt(1));
 		item.setProductId(rSet.getInt(2));
 		item.setEntryDate(new DateTime(rSet.getLong(3)));
		DateTime exit;
 		Long exitValue = rSet.getLong(4);
		if (exitValue == 0) {
			exit = null;
		} else {
			exit = new DateTime(exitValue);
		}
		item.setExitDate(exit);
 		item.setDeleted(rSet.getBoolean(5));
 		item.generateBarcodeFromString(rSet.getString(6));
 		item.setValid(true);
 		return item;
 	}
 
 	@Override
 	public Result saveAllData() {
 		return new Result(true);
 	}
 }
