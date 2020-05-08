 import java.io.Closeable;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /*
 Add/remove component
 
 Register customer(First_name, Last_name, Identification_code?, Birthday?, Address?)
 
 Buy(Computer, Customer, Is_deliver)
 
 From computer:
 Add/remove component
 
 
 
 SEARCH:
 customer first/last name, with his purchases
 
 
 */
 
 public class Shop implements Closeable {
 	private Connection connection;
 	private PreparedStatement
 		stSelectComponents,
 		stSelectComputers,
 		stSelectCustomer,
 		stSelectCustomers,
 		stSelectComputerComponents,
 	
 		stInsertCustomer,
 		stInsertPurchase,
 		stInsertComponent,
 		stInsertComputer,
 		
 		stSelectBelonging,
 		stUpdateBelonging,
 		stDeleteBelonging,
 		stInsertBelonging;
 
 	public Shop(Connection connection) throws SQLException {
 		stSelectComponents = connection.prepareStatement("SELECT * FROM Component");
 		stSelectComputers = connection.prepareStatement("SELECT ID, Title, Description, Price FROM ComputerPrice");
 		stSelectCustomer = connection.prepareStatement("SELECT ID FROM Customer WHERE First_name=? AND Last_name=?");
 		stSelectCustomers = connection.prepareStatement("SELECT * FROM Customer");
 		stSelectComputerComponents = connection.prepareStatement("SELECT Component_id AS ID, Component as Title, Component_manufacturer AS Manufacturer, Count, Total_price FROM ComputerComponent WHERE Computer_id = ?");
 		
 		stInsertCustomer = connection.prepareStatement("INSERT INTO Customer(First_name, Last_name, Identification_code, Birthday, Address) VALUES (?, ?, ?, ?, ?)");
 		stInsertPurchase = connection.prepareStatement("INSERT INTO Purchase(Computer, Customer, Is_deliver) VALUES (?, ?, ?)");
 		stInsertComponent = connection.prepareStatement("INSERT INTO Component(Title, Manufacturer, Price) VALUES (?, ?, ?)");
 		stInsertComputer = connection.prepareStatement("INSERT INTO Computer(Title, Description, Additional_price) VALUES (?, ?, ?)");
 		
 		stSelectBelonging = connection.prepareStatement("SELECT count FROM Belonging WHERE Computer=? AND Component=?");
 		stUpdateBelonging = connection.prepareStatement("UPDATE Belonging SET Count=? WHERE Computer=? AND Component=?");
 		stDeleteBelonging = connection.prepareStatement("DELETE FROM Belonging WHERE Computer=? AND Component=?");
 		stInsertBelonging = connection.prepareStatement("INSERT INTO Belonging(Computer, Component, Count) VALUES(?, ?, ?)");
 		
 		this.connection = connection;
 	}
 
 	public void close() {
 		try {
 			stSelectComponents.close();
 			stSelectComputers.close();
 			stSelectCustomer.close();
 			stSelectCustomers.close();
 			stSelectComputerComponents.close();
 		
 			stInsertCustomer.close();
 			stInsertPurchase.close();
 			stInsertComponent.close();
 			stInsertComputer.close();
 			
 			stSelectBelonging.close();
 			stUpdateBelonging.close();
 			stDeleteBelonging.close();
 			stInsertBelonging.close();
 		} catch (SQLException e) {
 			System.out.println("Failed closing prepared statements.");
 			e.printStackTrace();
 		}
 	}
 
	public void buy(int computer, int customer, boolean deliver) throws SQLException {
 		stInsertPurchase.setInt(1,  computer);
 		stInsertPurchase.setInt(2,  customer);
 		stInsertPurchase.setBoolean(3, deliver);
 		stInsertPurchase.executeUpdate();
 	}
 	
 	public void addComponent(String title, String manufacturer, BigDecimal price) throws SQLException {
 		stInsertComponent.setString(1, title);
 		stInsertComponent.setString(2, manufacturer);
 		stInsertComponent.setBigDecimal(3, price);
 		stInsertComponent.executeUpdate();
 	}
 
 	public void register(String firstName, String lastName, long identificationCode, Date birthday, String address) throws SQLException {
 		stInsertCustomer.setString(1, firstName);
 		stInsertCustomer.setString(2, lastName);
 		stInsertCustomer.setLong(3, identificationCode);
 		stInsertCustomer.setDate(4, birthday);
 		stInsertCustomer.setString(5, address);
 		stInsertCustomer.executeUpdate();
 	}
 	
 	public void changeComputerComponent(int computer, int component, int amount) throws SQLException {
 		if (amount == 0)
 			return;
 		connection.setAutoCommit(false);
 
 		try {
 			stSelectBelonging.setInt(1, computer);
 			stSelectBelonging.setInt(2, component);
 			ResultSet rs = stSelectBelonging.executeQuery();
 			
 			if (rs.next()) {
 				//exists
 				int old = rs.getInt(1);
 				int now = old + amount;
 				System.out.println(now);
 				if (now > 0) {
 					//update
 					System.out.println("update");
 					System.out.println(amount);
 					System.out.println(computer);
 					System.out.println(component);
 					stUpdateBelonging.setInt(1, now);
 					stUpdateBelonging.setInt(2, computer);
 					stUpdateBelonging.setInt(3, component);
 					stUpdateBelonging.executeUpdate();
 				}
 				else {
 					//delete
 					stDeleteBelonging.setInt(1, computer);
 					stDeleteBelonging.setInt(2, component);
 					stDeleteBelonging.executeUpdate();
 				}
 			}
 			else {
 				//create new
 				if (amount > 0) {
 					System.out.println("create");
 					stInsertBelonging.setInt(1, computer);
 					stInsertBelonging.setInt(2, component);
 					stInsertBelonging.setInt(3, amount);
 					stInsertBelonging.executeUpdate();
 				}
 		}
 		} catch (SQLException e) {
 			connection.rollback();
 			throw e;
 		}
 		connection.setAutoCommit(true);
 	}
 	
 	public void findCustomer(String firstName, String lastName) throws SQLException {
 		stSelectCustomer.setString(1, firstName);
 		stSelectCustomer.setString(2, lastName);
 		ResultSet rs = stSelectCustomer.executeQuery();
 		if (rs.next()) {
 			System.out.print("Customer id: ");
 			System.out.println(rs.getInt(1));
 		}
 		else
 			System.out.println("Customer not found");
 	}
 	
 	public void addComputer(String title, String description, BigDecimal additionalPrice) throws SQLException {
 		stInsertComputer.setString(1, title);
 		stInsertComputer.setString(2, description);
 		stInsertComputer.setBigDecimal(3, additionalPrice);
 		stInsertComputer.executeUpdate();
 	}
 	
 	public void addComponent() {
 		
 	}
 
 	public void printComponents() throws SQLException {
 		printResultSet(stSelectComponents.executeQuery());
 	}
 	
 	public void printComputerComponents(int computer) throws SQLException {
 		stSelectComputerComponents.setInt(1, computer);
 		printResultSet(stSelectComputerComponents.executeQuery());
 	}
 	
 	public void printCustomers() throws SQLException {
 		printResultSet(stSelectCustomers.executeQuery());
 	}
 	
 	public void printComputers() throws SQLException {
 		printResultSet(stSelectComputers.executeQuery());
 	}
 	
 	private void printResultSet(ResultSet resultSet) throws SQLException {
 		StringBuilder builder = new StringBuilder();
 		ResultSetMetaData metaData = resultSet.getMetaData();
 		int columns = metaData.getColumnCount();
 		int[] len = new int[columns];
 		for (int i = 0; i < columns; ++i)
 			len[i] = metaData.getColumnLabel(i+1).length();
 		ArrayList<String[]> values = new ArrayList<String[]>();
 		while (resultSet.next()) {
 			String[] row = new String[columns];
 			for (int i = 0; i < columns; ++i) {
 				row[i] = resultSet.getString(i+1);
 				if (row[i] == null)
 					row[i] = "NULL";
 				len[i] = Math.max(len[i], row[i].length());
 			}
 			values.add(row);
 		}
 		//titles
 		for (int i = 0; i < columns; ++i) {
 			if (i > 0)
 				builder.append('|');
 			String label = metaData.getColumnLabel(i+1);
 			builder.append(' ');
 			builder.append(label);
 			builder.append(' ');
 			for (int j = label.length(); j < len[i]; ++j)
 				builder.append(' ');
 		}
 		builder.append('\n');
 		//splitter
 		for (int i = 0; i < columns; ++i) {
 			if (i > 0)
 				builder.append('+');
 			for (int j = 0; j < len[i]+2; ++j)
 				builder.append('-');
 		}
 		builder.append('\n');
 		//data
 		for (String[] row : values) {
 			for (int i = 0; i < columns; ++i) {
 				if (i > 0)
 					builder.append('|');
 				builder.append(' ');
 				builder.append(row[i]);
 				builder.append(' ');
 				for (int j = row[i].length(); j < len[i]; ++j)
 					builder.append(' ');
 			}
 			builder.append('\n');
 		}
 		System.out.println(builder.toString());
 	}
 }
