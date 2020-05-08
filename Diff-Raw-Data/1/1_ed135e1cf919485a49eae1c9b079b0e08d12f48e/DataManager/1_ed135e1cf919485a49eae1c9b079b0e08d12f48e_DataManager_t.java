 package se.niclasolofsson.tddd24.server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import se.niclasolofsson.tddd24.shared.Category;
 import se.niclasolofsson.tddd24.shared.Customer;
 import se.niclasolofsson.tddd24.shared.Order;
 import se.niclasolofsson.tddd24.shared.Product;
 import se.niclasolofsson.tddd24.shared.ShoppingCartEntry;
 
 public class DataManager {
 	private Connection conn;
 	
 	public void connect() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection("jdbc:sqlite:webshop.db");
 
 		} catch (ClassNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				
 		} catch (SQLException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 		}
 	}
 	
 	public void close() {
         try {
 			conn.close();
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void create() {
 		Statement s;
 		try {
 			s = conn.createStatement();
 			
 			s.executeUpdate("DROP TABLE IF EXISTS categories");
 			s.executeUpdate("CREATE TABLE categories (id INTEGER PRIMARY KEY, name STRING)");
 			s.executeUpdate("INSERT INTO categories (name) VALUES ('Giraffes')");
 			s.executeUpdate("INSERT INTO categories (name) VALUES ('Aligators')");
 			
 			s.executeUpdate("DROP TABLE IF EXISTS products");
 			s.executeUpdate("CREATE TABLE products (id INTEGER PRIMARY KEY, name STRING, description TEXT, price FLOAT, stock INTEGER, category INTEGER)");
 			s.executeUpdate("INSERT INTO products (name, description, price, stock, category) VALUES ('Big Aligator', 'A massive one!', 1255, 2, 2)");
 			s.executeUpdate("INSERT INTO products (name, description, price, stock, category) VALUES ('Small Aligator', 'For the small home', 620, 4, 2)");
 
 			s.executeUpdate("DROP TABLE IF EXISTS customers");
 			s.executeUpdate("CREATE TABLE customers (id INTEGER PRIMARY KEY, name STRING, street STRING, postalCode STRING, city STRING)");
 			
 			s.executeUpdate("DROP TABLE IF EXISTS orderEntries");
 			s.executeUpdate("CREATE TABLE orderEntries (entryId INTEGER PRIMARY KEY, amount INTEGER, product INTEGER, orderId INTEGER)");
 
 			s.executeUpdate("DROP TABLE IF EXISTS orders");
 			s.executeUpdate("CREATE TABLE orders (orderId INTEGER PRIMARY KEY, customer INTEGER)");
 			
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public Category[] getCategories() {
 		PreparedStatement cs, ps;
 		Category category;
 		ArrayList<Category> res = new ArrayList<Category>();
 		
 		try {
 			cs = conn.prepareStatement("SELECT * FROM categories");
 			ResultSet crs = cs.executeQuery();
 			
 			while (crs.next()) {
 				category = new Category(crs.getString("name"), crs.getInt("id"));
 				
 				ArrayList<Product> products = new ArrayList<Product>();
 				ps = conn.prepareStatement("SELECT * FROM products WHERE category = ?");
 				ps.setInt(1, category.getId());
 				ResultSet prs = ps.executeQuery();
 				
 				while(prs.next()) {
 					products.add(new Product(prs.getInt("id"), prs.getString("name"), prs.getString("description"), prs.getFloat("price"), prs.getInt("stock")));
 				}
 				
 				category.addProducts(products.toArray(new Product[products.size()]));
 				res.add(category);
 				prs.close();
 			}
 			
 	        crs.close();
         
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return res.toArray(new Category[res.size()]);
 	}
 	
 	public Product[] getProducts() {
 		PreparedStatement s;
 		ArrayList<Product> res = new ArrayList<Product>();
 		
 		try {
 			s = conn.prepareStatement("SELECT * FROM products");
 			ResultSet rs = s.executeQuery();
 			
 			while(rs.next()) {
 				res.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getFloat("price"), rs.getInt("stock")));
 			}
 	        rs.close();
         
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return res.toArray(new Product[res.size()]);
 	}
 	
 
 	public Order[] getOrders() {
 		PreparedStatement s;
 		ArrayList<Order> res = new ArrayList<Order>();
 		ArrayList<ShoppingCartEntry> entries = new ArrayList<ShoppingCartEntry>();
 		Customer customer;
 		int orderId = 0;
 		
 		try {
 			s = conn.prepareStatement("SELECT * FROM orders, customers WHERE orders.customer = customers.id");
 			ResultSet ors = s.executeQuery();
 			
 			// For each order
 			while(ors.next()) {
 				// Get the order id and customer data
 				orderId = ors.getInt("orderId");
 				customer = new Customer(ors.getString("name"), ors.getString("street"), ors.getString("postalCode"), ors.getString("city"));
 				
 				// Get all products for the order
 				s = conn.prepareStatement("SELECT * FROM orderEntries, products WHERE orderEntries.product = products.id AND orderEntries.orderId = ?");
 				s.setInt(1, orderId);
 				ResultSet rs = s.executeQuery();
 				
 				while(rs.next()) {
 					entries.add(new ShoppingCartEntry(new Product(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getFloat("price"), rs.getInt("stock")), rs.getInt("amount")));
 				}
 		        rs.close();
 		        
 		        res.add(new Order(customer, entries.toArray(new ShoppingCartEntry[entries.size()]), orderId));
		        entries.clear();
 			}
 			
 	        ors.close();
         
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return res.toArray(new Order[res.size()]);
 	}
 
 
 	public void saveProduct(Product p) {
 		PreparedStatement s;
 		
 		try {
 			s = conn.prepareStatement("INSERT INTO products (name, description, price, stock, category) VALUES (?, ?, ?, ?, ?)");
 			s.setString(1, p.getName());
 			s.setString(2, p.getDescription());
 			s.setFloat(3, p.getPrice());
 			s.setInt(4, p.getStock());
 			s.setInt(5, p.getCategoryId());
 			s.executeUpdate();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void updateStock(Product p) {
 		PreparedStatement s;
 		
 		try {
 			s = conn.prepareStatement("UPDATE Products SET stock = ? WHERE id = ?");
 			s.setInt(1, p.getStock());
 			s.setInt(2, p.getId());
 			s.executeUpdate();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void saveOrder(Customer customer, ShoppingCartEntry[] entries) {
 		PreparedStatement s;
 		
 		try {
 			s = conn.prepareStatement("INSERT INTO customers (name, street, postalCode, city) VALUES (?, ?, ?, ?)", 
 									  PreparedStatement.RETURN_GENERATED_KEYS);
 			s.setString(1, customer.getName());
 			s.setString(2, customer.getStreet());
 			s.setString(3, customer.getPostalCode());
 			s.setString(4, customer.getCity());
 			s.executeUpdate();
 			
 			int customerId = s.getGeneratedKeys().getInt(1);
 			
 			s = conn.prepareStatement("INSERT INTO orders (customer) VALUES (?)", 
 	  				  PreparedStatement.RETURN_GENERATED_KEYS);
 			s.setInt(1, customerId);
 			s.executeUpdate();
 			
 			int orderId = s.getGeneratedKeys().getInt(1);
 			
 			s = conn.prepareStatement("INSERT INTO orderEntries (orderId, amount, product) VALUES (?, ?, ?)", 
 					  				  PreparedStatement.RETURN_GENERATED_KEYS);
 			s.setInt(1, orderId);
 			
 			
 			for (ShoppingCartEntry entry : entries) {
 				s.setInt(2, entry.getAmount());
 				s.setInt(3, entry.getProduct().getId());
 				s.executeUpdate();
 			}
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Category saveCategory(Category c) {
 		PreparedStatement s;
 		Category res = null;
 		
 		try {
 			s = conn.prepareStatement("INSERT INTO categories (name) VALUES (?)",
 									  PreparedStatement.RETURN_GENERATED_KEYS);
 			s.setString(1, c.getName());
 			s.executeUpdate();
 			
 			ResultSet keys = s.getGeneratedKeys();
 			res = new Category(c.getName(), keys.getInt(1));
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return res;
 	}
 
 }
