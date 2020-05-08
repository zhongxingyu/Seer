 package StockTradingServer;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import StockTradingCommon.Enumeration;
 
 public class DatabaseConnector {
 	private Connection con = null;
 
 	// connect to DB
 	public DatabaseConnector() {
 		// Basic configuration - to be moved to config file
 		String url = "jdbc:mysql://192.30.164.204:3306/repo6545";
 		String user = "repo6545";
 		String password = "MF4@2163G!8d2L4";
 
 		try {
 			Connection con = DriverManager.getConnection(url, user, password);
 			setCon(con);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public Connection getCon() {
 		return con;
 	}
 
 	public void setCon(Connection con) {
 		this.con = con;
 	}
 
 	/*
 	 * This function returns an array list of the brokerage firms
 	 */
 	public ArrayList<BrokerageFirm> selectBrokerageFirmsAll() {
 		ArrayList<BrokerageFirm> brokerageFirms = new ArrayList<BrokerageFirm>();
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM BROKERAGE_FIRM_INFO;";
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			while (res.next()) {
 
 				int id = res.getInt(1);
 				String name = res.getString(2);
 				String addressStreet = res.getString(3);
 				String addressCity = res.getString(4);
 				String addressState = res.getString(5);
 				String addressZip = res.getString(6);
 				String licenceNumber = res.getString(7);
 				int status = res.getInt(8);
 
 				BrokerageFirm brokerageFirm = new BrokerageFirm();
 				brokerageFirm.setId(id);
 				brokerageFirm.setName(name);
 				brokerageFirm.setAddressStreet(addressStreet);
 				brokerageFirm.setAddressCity(addressCity);
 				brokerageFirm.setAddressState(addressState);
 				brokerageFirm.setAddressZip(addressZip);
 				brokerageFirm.setLicenceNumber(licenceNumber);
 				brokerageFirm.setStatus(status);
 
 				brokerageFirms.add(brokerageFirm);
 
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return brokerageFirms;
 	}
 
 	/*
 	 * This function returns a single brokerage firm based on a given id MySQL
 	 * injection protection
 	 */
 	public BrokerageFirm selectBrokerageFirm(int idToSelect) {
 		BrokerageFirm brokerageFirm = new BrokerageFirm();
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM BROKERAGE_FIRM_INFO WHERE ID = ?;";
 
 		try {
 			st = this.con.prepareStatement(query);
 			st.setInt(1, idToSelect);
 
 			ResultSet res = st.executeQuery();
 
 			res.next();
 
 			int id = res.getInt(1);
 			String name = res.getString(2);
 			String addressStreet = res.getString(3);
 			String addressCity = res.getString(4);
 			String addressState = res.getString(5);
 			String addressZip = res.getString(6);
 			String licenceNumber = res.getString(7);
 			int status = res.getInt(8);
 
 			brokerageFirm.setId(id);
 			brokerageFirm.setName(name);
 			brokerageFirm.setAddressStreet(addressStreet);
 			brokerageFirm.setAddressCity(addressCity);
 			brokerageFirm.setAddressState(addressState);
 			brokerageFirm.setAddressZip(addressZip);
 			brokerageFirm.setLicenceNumber(licenceNumber);
 			brokerageFirm.setStatus(status);
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return brokerageFirm;
 	}
 
 	/*
 	 * This function adds a new brokerage firm to the database from the given
 	 * class instance MySQL injection checked
 	 */
 	public Validator insertNewBrokerageFirm(BrokerageFirm newFirm) {
 		// validate input
 		Validator v = newFirm.validate();
 		if (!v.isVerified()) {
 			return v;
 		}
 
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		String query = "INSERT INTO BROKERAGE_FIRM_INFO (NAME, ADDRESSSTREET, ADDRESSCITY, ADDRESSSTATE, ADDRESSZIP, LICENCENUMBER, STATUSID) VALUES (?, ?, ?, ?, ?, ?, ?)";
 
 		try {
 
 			st = this.con.prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 			st.setString(1, newFirm.getName());
 			st.setString(2, newFirm.getAddressStreet());
 			st.setString(3, newFirm.getAddressCity());
 			st.setString(4, newFirm.getAddressState());
 			st.setString(5, newFirm.getAddressZip());
 			st.setString(6, newFirm.getLicenceNumber());
 			st.setInt(7, newFirm.getStatus());
 
 			int affectedRows = st.executeUpdate();
 
 			if (affectedRows == 0) {
 				v.setVerified(false);
 				v.setStatus("Could not insert into the table");
 				return v;
 			}
 
 			// rs = st.getGeneratedKeys();
 			// if (rs.next()) {
 			// System.out.println(rs.getLong(1));
 			// } else {
 			// throw new SQLException(
 			// "Creating user failed, no generated key obtained.");
 			// }
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		v.setVerified(true);
 		v.setStatus("Success");
 
 		return v;
 	}
 
 	/*
 	 * This function updates a specified brokerage firm with provided
 	 * information from the brokerage class
 	 */
 	public Validator updateBrokerageFirm(int idToUpdate,
 			BrokerageFirm firmToUpdate) {
 		// validate input
 		Validator v = firmToUpdate.validate();
 		if (!v.isVerified()) {
 			return v;
 		}
 
 		InputValidation iv = new InputValidation();
 		Validator v2 = iv.validateIntGeneral(idToUpdate, "idToUpdate");
 
 		if (!v2.isVerified()) {
 			return v2;
 		}
 
 		PreparedStatement st = null;
 
 		String query = "UPDATE BROKERAGE_FIRM_INFO SET NAME = ?, ADDRESSSTREET = ?, ADDRESSCITY = ?, ADDRESSSTATE = ?, ADDRESSZIP = ?, LICENCENUMBER = ?, STATUSID = ? WHERE ID = ?;";
 
 		try {
 
 			st = this.con.prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 
 			st.setString(1, firmToUpdate.getName());
 			st.setString(2, firmToUpdate.getAddressStreet());
 			st.setString(3, firmToUpdate.getAddressCity());
 			st.setString(4, firmToUpdate.getAddressState());
 			st.setString(5, firmToUpdate.getAddressZip());
 			st.setString(6, firmToUpdate.getLicenceNumber());
 			st.setInt(7, firmToUpdate.getStatus());
 			st.setInt(8, idToUpdate);
 
 			int affectedRows = st.executeUpdate();
 
 			if (affectedRows == 0) {
 				v.setVerified(false);
 				v.setStatus("Update failed");
 				return v;
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return v;
 	}
 
 	/*
 	 * This function returns all broker users 0 - all 1,2 - with certain status
 	 */
 	public ArrayList<User> selectBrokersAll(int pStatusId) {
 		ArrayList<User> usersAll = new ArrayList<User>();
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM USERS WHERE ROLEID = 2";
 
 		if (pStatusId != Enumeration.Broker.BROKER_SELECT_PARAM_EMPTY) {
 			query += " AND STATUSID = \"" + pStatusId + "\"";
 		}
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			while (res.next()) {
 
 				int id = res.getInt(1);
 				String firstName = res.getString(2);
 				String lastName = res.getString(3);
 				String email = res.getString(4);
 				String ssn = res.getString(5);
 				String password = res.getString(6);
 				String salt = res.getString(7);
 				int roleId = res.getInt(8);
 				int statusId = res.getInt(9);
 				int brokerFirmId = res.getInt(10);
 
 				User user = new User();
 				user.setId(id);
 				user.setFirstName(firstName);
 				user.setLastName(lastName);
 				user.setEmail(email);
 				user.setEmail(ssn);
 				user.setPassword(password);
 				user.setSalt(salt);
 				user.setRoleId(roleId);
 				user.setStatusId(statusId);
 				user.setBrokerFirmId(brokerFirmId);
 
 				usersAll.add(user);
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return usersAll;
 	}
 
 	/*
 	 * This function returns all broker users for a given firm 0 - all 1,2 -
 	 * with certain status
 	 */
 	public ArrayList<User> selectBrokersAllbyFirm(int pFirmId) {
 		ArrayList<User> usersAll = new ArrayList<User>();
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM USERS WHERE ROLEID = 2";
 
 		if (pFirmId != Enumeration.Broker.BROKER_SELECT_PARAM_EMPTY) {
 			query += " AND FIRMID = \"" + pFirmId + "\"";
 		}
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			while (res.next()) {
 
 				int id = res.getInt(1);
 				String firstName = res.getString(2);
 				String lastName = res.getString(3);
 				String email = res.getString(4);
 				String ssn = res.getString(5);
 				String password = res.getString(6);
 				String salt = res.getString(7);
 				int roleId = res.getInt(8);
 				int statusId = res.getInt(9);
 				int brokerFirmId = res.getInt(10);
 
 				User user = new User();
 				user.setId(id);
 				user.setFirstName(firstName);
 				user.setLastName(lastName);
 				user.setEmail(email);
 				user.setEmail(ssn);
 				user.setPassword(password);
 				user.setSalt(salt);
 				user.setRoleId(roleId);
 				user.setStatusId(statusId);
 				user.setBrokerFirmId(brokerFirmId);
 
 				usersAll.add(user);
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return usersAll;
 	}
 
 	/*
 	 * This function returns a broker for a given userid
 	 */
 	public User selectBrokerUser(int idToSelect) {
 		User user = new User();
 
 		PreparedStatement st = null;
 		String query = "SELECT * FROM USERS WHERE ROLEID = 2 AND ID = ?";
 
 		try {
 			st = this.con.prepareStatement(query);
 			st.setInt(1, idToSelect);
 
 			ResultSet res = st.executeQuery();
 
 			res.next();
 
 			int id = res.getInt(1);
 			String firstName = res.getString(2);
 			String lastName = res.getString(3);
 			String email = res.getString(4);
 			String ssn = res.getString(5);
 			String password = res.getString(6);
 			String salt = res.getString(7);
 			int roleId = res.getInt(8);
 			int statusId = res.getInt(9);
 			int brokerFirmId = res.getInt(10);
 
 			user.setId(id);
 			user.setFirstName(firstName);
 			user.setLastName(lastName);
 			user.setEmail(email);
			user.setEmail(ssn);
 			user.setPassword(password);
 			user.setSalt(salt);
 			user.setRoleId(roleId);
 			user.setStatusId(statusId);
 			user.setBrokerFirmId(brokerFirmId);
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return user;
 	}
 
 	/*
 	 * This function inserts a new broker MySQL injection checked
 	 */
 	public Validator insertNewBroker(User newUser) {
 		// validate input
 		Validator v = newUser.validate();
 		if (!v.isVerified()) {
 			return v;
 		}
 
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		String query = "INSERT INTO USERS (FIRSTNAME, LASTNAME, EMAIL, SSN, PASSWORD, SALT, ROLEID, STATUSID, FIRMID) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
 
 		try {
 			st = this.con.prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 			st.setString(1, newUser.getFirstName());
 			st.setString(2, newUser.getLastName());
 			st.setString(3, newUser.getEmail());
 			st.setString(4, newUser.getSsn());
 			st.setString(5, newUser.getPassword());
 			st.setString(6, newUser.getSalt());
 			st.setInt(7, newUser.getRoleId());
 			st.setInt(8, newUser.getStatusId());
 			st.setInt(9, newUser.getBrokerFirmId());
 
 			int affectedRows = st.executeUpdate();
 
 			if (affectedRows == 0) {
 				v.setVerified(false);
 				v.setStatus("Could not insert into the table");
 				return v;
 			}
 
 			rs = st.getGeneratedKeys();
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		v.setVerified(true);
 		v.setStatus("Success");
 
 		return v;
 	}
 
 	public Validator updateBroker(int idToUpdate, User user) {
 		// validate input
 		Validator v = user.validate();
 		if (!v.isVerified()) {
 			return v;
 		}
 
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		String query = "UPDATE USERS SET FIRSTNAME = ?, LASTNAME = ?, EMAIL = ?, SSN = ?, PASSWORD = ?, SALT = ?, ROLEID = ?, STATUSID = ?, FIRMID = ? WHERE ID = ?";
 
 		try {
 			st = this.con.prepareStatement(query,
 					Statement.RETURN_GENERATED_KEYS);
 			st.setString(1, user.getFirstName());
 			st.setString(2, user.getLastName());
 			st.setString(3, user.getEmail());
 			st.setString(4, user.getSsn());
 			st.setString(5, user.getPassword());
 			st.setString(6, user.getSalt());
 			st.setInt(7, user.getRoleId());
 			st.setInt(8, user.getStatusId());
 			st.setInt(9, user.getBrokerFirmId());
 			st.setInt(10, idToUpdate);
 
 			int affectedRows = st.executeUpdate();
 
 			if (affectedRows == 0) {
 				v.setVerified(false);
 				v.setStatus("Could not update the table");
 				return v;
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		v.setVerified(true);
 		v.setStatus("Success");
 
 		return v;
 	}
 
 	public ArrayList<CustomerInfo> selectCustomerInfoAll() {
 		ArrayList<CustomerInfo> customerInfoAll = new ArrayList<CustomerInfo>();
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM CUSTOMER_INFO;";
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			while (res.next()) {
 
 				int id = res.getInt(1);
 				String firstName = res.getString(2);
 				String lastName = res.getString(3);
 				String email = res.getString(4);
 				String phone = res.getString(5);
 
 				CustomerInfo customer = new CustomerInfo();
 				customer.setId(id);
 				customer.setFirstName(firstName);
 				customer.setLastName(lastName);
 				customer.setEmail(email);
 				customer.setPhone(phone);
 
 				customerInfoAll.add(customer);
 
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return customerInfoAll;
 	}
 
 	public CustomerInfo selectCustomerInfo(int idToSelect) {
 		CustomerInfo customer = new CustomerInfo();
 
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM CUSTOMER_INFO WHERE id = \"" + idToSelect
 				+ "\";";
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			res.next();
 
 			int id = res.getInt(1);
 			String firstName = res.getString(2);
 			String lastName = res.getString(3);
 			String email = res.getString(4);
 			String phone = res.getString(5);
 
 			customer.setId(id);
 			customer.setFirstName(firstName);
 			customer.setLastName(lastName);
 			customer.setEmail(email);
 			customer.setPhone(phone);
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return customer;
 	}
 
 	public boolean insertNewCustomerInfo(CustomerInfo newCustomer) {
 		Statement st = null;
 		ResultSet rs = null;
 
 		String query = "INSERT INTO CUSTOMER_INFO (FIRSTNAME, LASTNAME, EMAIL, PHONE)"
 				+ " VALUES ("
 				+ "\""
 				+ newCustomer.getFirstName()
 				+ "\",\""
 				+ newCustomer.getLastName()
 				+ "\",\""
 				+ newCustomer.getEmail()
 				+ "\",\"" + newCustomer.getPhone() + "\")";
 
 		try {
 
 			st = this.con.createStatement();
 
 			int affectedRows = st.executeUpdate(query,
 					Statement.RETURN_GENERATED_KEYS);
 
 			if (affectedRows == 0) {
 				throw new SQLException("Insert failed");
 			}
 
 			rs = st.getGeneratedKeys();
 			if (rs.next()) {
 				System.out.println(rs.getLong(1));
 			} else {
 				throw new SQLException("No generated key obtained.");
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return true;
 	}
 
 	public boolean updateCustomerInfo(int idToUpdate,
 			CustomerInfo customerToUpdate) {
 		Statement st = null;
 		ResultSet rs = null;
 
 		String query = "UPDATE CUSTOMER_INFO SET" + " FIRSTNAME = \""
 				+ customerToUpdate.getFirstName() + "\", LASTNAME = \""
 				+ customerToUpdate.getLastName() + "\", EMAIL = \""
 				+ customerToUpdate.getEmail() + "\", PHONE = \""
 				+ customerToUpdate.getPhone() + "\" WHERE ID = \"" + idToUpdate
 				+ "\";";
 
 		try {
 
 			st = this.con.createStatement();
 
 			int affectedRows = st.executeUpdate(query);
 
 			if (affectedRows == 0) {
 				throw new SQLException("Update failed");
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return true;
 	}
 
 	public ArrayList<Stock> selectStockAll() {
 		ArrayList<Stock> stocksAll = new ArrayList<Stock>();
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM STOCKS;";
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			while (res.next()) {
 
 				int id = res.getInt(1);
 				String name = res.getString(2);
 				int amount = res.getInt(3);
 				int price = res.getInt(4);
 
 				Stock stock = new Stock();
 				stock.setId(id);
 				stock.setName(name);
 				stock.setAmount(amount);
 				stock.setPrice(price);
 
 				stocksAll.add(stock);
 
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return stocksAll;
 	}
 
 	public Stock selectStock(int idToSelect) {
 		Stock stock = new Stock();
 
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM STOCKS WHERE id = \"" + idToSelect
 				+ "\";";
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			res.next();
 
 			int id = res.getInt(1);
 			String name = res.getString(2);
 			int amount = res.getInt(3);
 			int price = res.getInt(4);
 
 			stock.setId(id);
 			stock.setName(name);
 			stock.setAmount(amount);
 			stock.setPrice(price);
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return stock;
 	}
 
 	public boolean insertNewStock(Stock newStock) {
 		Statement st = null;
 		ResultSet rs = null;
 
 		String query = "INSERT INTO STOCKS (NAME, AMOUNT, PRICE)" + " VALUES ("
 				+ "\"" + newStock.getName() + "\",\"" + newStock.getAmount()
 				+ "\",\"" + newStock.getPrice() + "\")";
 
 		try {
 
 			st = this.con.createStatement();
 
 			int affectedRows = st.executeUpdate(query,
 					Statement.RETURN_GENERATED_KEYS);
 
 			if (affectedRows == 0) {
 				throw new SQLException("Insert failed");
 			}
 
 			rs = st.getGeneratedKeys();
 			if (rs.next()) {
 				System.out.println(rs.getLong(1));
 			} else {
 				throw new SQLException("No generated key obtained.");
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return true;
 	}
 
 	public boolean updateStock(int idToUpdate, Stock stock) {
 		Statement st = null;
 		ResultSet rs = null;
 
 		String query = "UPDATE STOCKS SET" + " NAME = \"" + stock.getName()
 				+ "\", AMOUNT = \"" + stock.getAmount() + "\", PRICE = \""
 				+ stock.getPrice() + "\" WHERE ID = \"" + idToUpdate + "\";";
 
 		try {
 
 			st = this.con.createStatement();
 
 			int affectedRows = st.executeUpdate(query);
 
 			if (affectedRows == 0) {
 				throw new SQLException("Update failed");
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return true;
 	}
 
 	public ArrayList<Order> selectOrdersAll() {
 		ArrayList<Order> ordersAll = new ArrayList<Order>();
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM ORDERS;";
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			while (res.next()) {
 				int orderId = res.getInt(1);
 				int brokerId = res.getInt(2);
 				int stockId = res.getInt(3);
 				int amount = res.getInt(4);
 				Date dateIssued = res.getDate(5);
 				Date dateExpiration = res.getDate(6);
 				int statusId = res.getInt(7);
 				int typeId = res.getInt(8);
 
 				Order order = new Order();
 				order.setOrderId(orderId);
 				order.setBrokerId(brokerId);
 				order.setStockId(stockId);
 				order.setAmount(amount);
 				order.setDateIssued(dateIssued);
 				order.setDateExpiration(dateExpiration);
 				order.setStatusId(statusId);
 				order.setTypeId(typeId);
 
 				ordersAll.add(order);
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return ordersAll;
 	}
 
 	public Order selectOrder(int idToSelect) {
 		Order order = new Order();
 
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM ORDERS WHERE ORDERID = \"" + idToSelect
 				+ "\";";
 
 		try {
 			st = this.con.createStatement();
 			ResultSet res = st.executeQuery(query);
 
 			res.next();
 
 			int orderId = res.getInt(1);
 			int brokerId = res.getInt(2);
 			int stockId = res.getInt(3);
 			int amount = res.getInt(4);
 			Date dateIssued = res.getDate(5);
 			Date dateExpiration = res.getDate(6);
 			int statusId = res.getInt(7);
 			int typeId = res.getInt(8);
 
 			order.setOrderId(orderId);
 			order.setBrokerId(brokerId);
 			order.setStockId(stockId);
 			order.setAmount(amount);
 			order.setDateIssued(dateIssued);
 			order.setDateExpiration(dateExpiration);
 			order.setStatusId(statusId);
 			order.setTypeId(typeId);
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return order;
 	}
 
 	public boolean insertNewOrder(Order newOrder) {
 		Statement st = null;
 		ResultSet rs = null;
 
 		String query = "INSERT INTO ORDERS (BROKERID, STOCKID, AMOUNT, DATEISSUED, DATEEXPIRATION, STATUSID, TYPEID)"
 				+ " VALUES ("
 				+ "\""
 				+ newOrder.getBrokerId()
 				+ "\",\""
 				+ newOrder.getStockId()
 				+ "\",\""
 				+ newOrder.getAmount()
 				+ "\",\""
 				+ newOrder.getDateIssued()
 				+ "\",\""
 				+ newOrder.getDateExpiration()
 				+ "\",\""
 				+ newOrder.getStatusId()
 				+ "\",\""
 				+ newOrder.getTypeId()
 				+ "\")";
 
 		try {
 
 			st = this.con.createStatement();
 
 			int affectedRows = st.executeUpdate(query,
 					Statement.RETURN_GENERATED_KEYS);
 
 			if (affectedRows == 0) {
 				throw new SQLException("Insert failed");
 			}
 
 			rs = st.getGeneratedKeys();
 			if (rs.next()) {
 				System.out.println(rs.getLong(1));
 			} else {
 				throw new SQLException("No generated key obtained.");
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return true;
 	}
 
 	public boolean updateOrder(int idToUpdate, Order order) {
 		Statement st = null;
 		ResultSet rs = null;
 
 		String query = "UPDATE ORDERS SET" + " BROKERID = \""
 				+ order.getBrokerId() + "\", STOCKID = \"" + order.getStockId()
 				+ "\", AMOUNT = \"" + order.getAmount() + "\", DATEISSUED = \""
 				+ order.getDateIssued() + "\", DATEEXPIRATION = \""
 				+ order.getDateExpiration() + "\", STATUSID = \""
 				+ order.getStatusId() + "\", TYPEID = \"" + order.getTypeId()
 				+ "\" WHERE ORDERID = \"" + idToUpdate + "\";";
 
 		try {
 
 			st = this.con.createStatement();
 
 			int affectedRows = st.executeUpdate(query);
 
 			if (affectedRows == 0) {
 				throw new SQLException("Update failed");
 			}
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return true;
 	}
 
 	public ArrayList<StatusesOptions> selectAllStatuses() {
 
 		ArrayList<StatusesOptions> statusesList = new ArrayList<StatusesOptions>();
 
 		Connection con = this.con;
 		Statement st = null;
 		ResultSet rs = null;
 		String query = "SELECT * FROM DIC_STATUSES ORDER BY PRIORITY;";
 
 		try {
 			st = con.createStatement();
 			ResultSet res = st.executeQuery(query);
 			while (res.next()) {
 
 				int id = res.getInt(1);
 				String name = res.getString(2);
 
 				StatusesOptions status = new StatusesOptions();
 				status.setId(id);
 				status.setName(name);
 
 				statusesList.add(status);
 			}
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(DatabaseConnector.class.getName());
 			lgr.log(Level.WARNING, ex.getMessage(), ex);
 		}
 
 		return statusesList;
 
 	}
 
 }
