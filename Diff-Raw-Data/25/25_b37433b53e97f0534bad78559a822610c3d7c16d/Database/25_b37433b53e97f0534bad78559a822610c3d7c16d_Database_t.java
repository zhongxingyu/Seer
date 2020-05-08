 package db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.hsqldb.StatementTypes;
 
 public class Database {
 	private Connection connection;
 	private Statement statement;
 
 	public Database() {
 		connection = null;
 		statement = null;
 	}
 
 	public boolean openConnection(String database, String userName, String password) {
 		try {
 			Class.forName("org.hsqldb.jdbcDriver");
 			connection = DriverManager.getConnection("jdbc:hsqldb:file:" + database, userName, password);
 			// statement = connection.createStatement();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		// Initierar tabellerna
 		initDB();
 		return true;
 	}
 
 	private void initDB() {
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate(customerInitQuery);
 			statement.executeUpdate(articleInitQuery);
 			statement.executeUpdate(receiptInitQuery);
 			statement.executeUpdate(receiptItemInitQuery);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// Lgg till exempelvrden i databasen
 //		 initDBTestValues();
 	}
 
 	public void closeConnection() {
 		try {
 			if (connection != null) connection.close();
 			System.out.println("DATABASE: Connection Closed");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		connection = null;
 	}
 
 	public boolean isConnected() {
 		return connection != null;
 	}
 
 	public ArrayList<Integer> getPaidReceipts() {
 
 		String sql = "SELECT receiptId FROM Receipts WHERE isPaid = 'true'";
 		ArrayList<Integer> receipts = null;
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			receipts = new ArrayList<Integer>();
 			while (rs.next())
 				receipts.add(rs.getInt("receiptId"));
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		// listItems(receipts);
 		return receipts;
 	}
 
 	public ArrayList<Integer> getUnPaidReceipts() {
 		String sql = "SELECT receiptId FROM Receipts WHERE isPaid = 'false'";
 		ArrayList<Integer> receipts = null;
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			receipts = new ArrayList<Integer>();
 			while (rs.next())
 				receipts.add(rs.getInt("receiptId"));
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		// Printar alla kvitton som svaret innehller
 		// listItems(receipts);
 		return receipts;
 	}
 
 	public Customer getCustomerInfo(String receiptId) {
 		String sqlCustomerId = "SELECT customerId FROM RECEIPTS WHERE receiptId = " + receiptId;
 		String sqlCustomerInfo = "SELECT * FROM CUSTOMERS WHERE customerId = (" + sqlCustomerId + ")";
 		Customer c = null;
 		String firstName, lastName, phone, email;
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sqlCustomerInfo);
 
 			rs.next();
 			firstName = rs.getString("firstName");
 			lastName = rs.getString("lastName");
 			phone = rs.getString("phone");
 			email = rs.getString("email");
 			c = new Customer(firstName, lastName, phone, email);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		// Printar all info om den mottagna kunden
 		// listCustomer(c);
 		return c;
 	}
 
 	public Receipt getReceiptInfo(String receiptId) {
 		Receipt receipt = null;
 		String articleName = "";
 		double price = 0;
 		int amount = 0;
 		String receiptSql = "SELECT lastPayDate, isPaid, comments FROM Receipts WHERE receiptId = " + receiptId;
 		String articleSql = "SELECT articleName, price, amount FROM ReceiptItems, Articles WHERE ReceiptItems.receiptId = " + receiptId + " AND Articles.articleId = ReceiptItems.articleId";
 
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(receiptSql);
 			rs.next();
 			receipt = new Receipt(rs.getDate("lastPayDate"), rs.getBoolean("isPaid"), rs.getString("comments"));
 
 			rs = statement.executeQuery(articleSql);
 			while (rs.next()) {
 				articleName = rs.getString("articleName");
 				price = rs.getDouble("price");
 				amount = rs.getInt("amount");
 				receipt.addArticle(new ReceiptArticle(articleName, price, amount));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return receipt;
 	}
 
 	public Boolean isPaid(String receiptId) {
 		String sql = "SELECT isPaid FROM Receipts WHERE receiptId = " + receiptId;
 		Boolean isPaid = false;
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			rs.next();
 			isPaid = rs.getBoolean("isPaid");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return isPaid;
 	}
 
 	public void receiptIsPaidFor(String receiptId, boolean isPaidFor) {
 		String sql = "UPDATE Receipts SET isPaid = " + isPaidFor + " WHERE receiptId = " + receiptId;
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate(sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 
 	public boolean addCustomer(Customer c) {
 		String sql = "INSERT INTO Customers (firstName, lastName, phone, email) VALUES ('" + c.getFirstName() + "','" + c.getLastName() + "','" + c.getPhone();
 		if(c.getEmail().length() == 0) sql += "','')";
 		else sql += "','" + c.getEmail() + "')";
 		
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate(sql);
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public ArrayList<Customer> getCustomers() {
 		String sql = "SELECT firstName, lastName, phone, email FROM Customers;";
 		ArrayList<Customer> customers = new ArrayList<Customer>();
 		Customer c = null;
 		String firstName, lastName, phone, email;
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			
 			while(rs.next()){
 				firstName = rs.getString("firstName");
 				lastName = rs.getString("lastName");
 				phone = rs.getString("phone");
 				email = rs.getString("email");
 				c = new Customer(firstName, lastName, phone, email);
 				customers.add(c);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return customers;
 	}
 	
 	public ArrayList<ReceiptArticle> getAllArticles() {
 		String sql = "SELECT articleName, price FROM Articles";
 		ArrayList<ReceiptArticle> articles = new ArrayList<ReceiptArticle>();
 		
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			
 			while(rs.next()){
 				articles.add(new ReceiptArticle(rs.getString("articleName"), rs.getDouble("price"), 0));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return articles;
 	}
 
 	public boolean addArticle(String articleName, double price) {
 		String sql = "INSERT INTO Articles (articleName, price) VALUES ('" + articleName + "', " + price + ")";
 		boolean result = false;
 		
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate(sql);
 			result = true;
 		} catch (SQLException e) {
 			//e.printStackTrace();
 			result = false;
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return result;
 	}
 
 	public boolean receitpNbrExists(String receiptNbr) {
 		String sql = "SELECT * FROM Receipts WHERE receiptId = " + receiptNbr;
 		
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			return rs.next();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 	
 	public boolean customerExists(String firstName, String lastName, String phoneNbr) {
 		String sql = "SELECT * FROM Customers WHERE firstName = '" + firstName + "' AND lastName = '" + lastName + "' AND phone = '" + phoneNbr + "'";
 		
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			return rs.next();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 	
 	public int getCustomerId(Customer c) {
 		String sql = "SELECT CustomerId FROM Customers WHERE firstName ='" + c.getFirstName() + "' AND lastName = '" + c.getLastName() + "' AND phone = '" + c.getPhone() + "'";
 		
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			rs.next();
 			return rs.getInt("customerId");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return -1;
 	}
 	
 	public void addReceipt(int receiptId, int customerId, Receipt receipt) {
 		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
 		String date = s.format(receipt.getLastPayDate());
 		String comments = receipt.getComments();
 		boolean isPaid = receipt.isPaid();
 		
 		
 		String sql = "INSERT INTO Receipts (receiptId, customerId, lastPayDate, isPaid, comments) VALUES (" + receiptId + "," + customerId + ",'" + date + "'," + isPaid + ", '" + comments + "')";
 		
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate(sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		String insertSql = "INSERT INTO ReceiptItems (articleId, receiptId, amount) VALUES ";
 		for(ReceiptArticle article : receipt.getReceiptItems()){
 			String articleSql = "SELECT ArticleId FROM Articles WHERE articleName = '" + article.getArticleName() + "'";
 			int articleId = -1;
 			try {
 				statement =connection.createStatement();
 				ResultSet rs = statement.executeQuery(articleSql);
 				rs.next();
 				articleId = rs.getInt("articleId");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					statement.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			insertSql += "('" + articleId + "'," + receiptId + "," + article.getAmount() + "),";
 		}
 		insertSql = insertSql.substring(0, insertSql.length()-1);
 		
 		try {
 			System.out.println(insertSql);
 			statement = connection.createStatement();
 			statement.executeUpdate(insertSql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public ArrayList<Customer> customerSearch(String firstName, String lastName){
 		ArrayList<Customer> customers = new ArrayList<Customer>();
		String sql = "SELECT * FROM Customers WHERE firstName LIKE '%" + firstName + "%' AND lastName LIKE '%" + lastName + "%'";
 		String first, last, phone, email;
 
 		try {
 			statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery(sql);
 			
 			while(rs.next()) {
 				first = rs.getString("firstName");
 				last = rs.getString("lastName");
 				phone = rs.getString("phone");
 				email = rs.getString("email");
 				customers.add(new Customer(first, last, phone, email));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return customers;
 	}
 	
 	private String customerInitQuery = "CREATE TABLE IF NOT EXISTS Customers (" + "customerId integer IDENTITY," + "firstName varchar(255) NOT NULL," + "lastName varchar(255) NOT NULL," + "phone varchar(255) NOT NULL," + "email varchar(255)," + "UNIQUE (firstName, lastName, phone)" + ")";
 	private String articleInitQuery = "CREATE TABLE IF NOT EXISTS Articles (" + "articleId integer IDENTITY," + "articleName varchar(255) NOT NULL," + "price double NOT NULL," + "UNIQUE (articleName)" + ")";
 	private String receiptInitQuery = "CREATE TABLE IF NOT EXISTS Receipts (" + "receiptId integer NOT NULL," + "customerId integer NOT NULL," + "lastPayDate date NOT NULL," + "isPaid boolean NOT NULL," + "comments varchar(255)," + "PRIMARY KEY (receiptId)," + "FOREIGN KEY (customerId) REFERENCES Customers (customerId)" + ")";
 	private String receiptItemInitQuery = "CREATE TABLE IF NOT EXISTS ReceiptItems (" + "articleId integer NOT NULL," + "receiptId integer NOT NULL," + "amount integer NOT NULL," + "PRIMARY KEY (articleId, receiptId)," + "FOREIGN KEY (articleId) REFERENCES Articles (articleId)," + "FOREIGN KEY (receiptId) REFERENCES Receipts (receiptId)" + ")";
 
 	private void listItems(ArrayList<Integer> items) {
 		for (Integer i : items)
 			System.out.println("DATABASE: " + i);
 	}
 
 	private void listCustomer(Customer c) {
 		System.out.println("DATABASE: " + c.getFirstName());
 		System.out.println("DATABASE: " + c.getLastName());
 		System.out.println("DATABASE: " + c.getPhone());
 		System.out.println("DATABASE: " + c.getEmail());
 	}
 
 	public void initDBTestValues() {
 		String customerPatrick = "INSERT INTO Customers (firstName, lastName, phone, email) VALUES ('Patrick', 'Ivarsson', '0703464155', 'Patrick.Ivarsson@gmail.com')";
 		String customerEmelie = "INSERT INTO Customers (firstName, lastName, phone, email) VALUES ('Emelie', 'Larsson', '0768601632', 'Emeliel_90@hotmail.com')";
 		String articleHudkram1 = "INSERT INTO Articles (articleName, price) VALUES ('Hudkram 1', 59.50)";
 		String articleHudkram2 = "INSERT INTO Articles (articleName, price) VALUES ('Hudkram 2', 130.99)";
 		String receiptPatrick = "INSERT INTO Receipts (receiptId, customerId, lastPayDate, isPaid, comments) VALUES (0001, 0, '2012-03-23', 'false', 'TestBeskrivning')";
 		String receiptEmelie = "INSERT INTO Receipts (receiptId, customerId, lastPayDate, isPaid, comments) VALUES (4321, 1, '2012-05-12', 'true', 'TestigareBeskrivning')";
 		String receiptItemPatrick = "INSERT INTO ReceiptItems (articleId, receiptId, amount) VALUES (1, 0001, 2), (0, 0001,1)";
 		String receiptItemEmelie = "INSERT INTO ReceiptItems (articleId, receiptId, amount) VALUES (0, 4321, 15)";
 
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate(customerPatrick);
 			statement.executeUpdate(customerEmelie);
 			statement.executeUpdate(articleHudkram1);
 			statement.executeUpdate(articleHudkram2);
 			statement.executeUpdate(receiptPatrick);
 			statement.executeUpdate(receiptEmelie);
 			statement.executeUpdate(receiptItemPatrick);
 			statement.executeUpdate(receiptItemEmelie);
 
 			String sql = "SELECT price FROM Articles WHERE articleName = 'Hudkram 2'";
 			ResultSet rs = statement.executeQuery(sql);
 			rs.next();
 			System.out.println(rs.getDouble("price"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 
 	
 }
