 package streamfish;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author HJ
  */
 public class StreamFish {
 
 	/**
 	 * @param args the command line arguments
 	 */
 	private String databasedriver;
 	private Connection con;
 	
 	public StreamFish() {
 		try {
 			databasedriver = "org.apache.derby.jdbc.ClientDriver";
 			Class.forName(databasedriver);  // laster inn driverklassen
 			String databasenavn = "jdbc:derby://db.stud.aitel.hist.no/streamfish;user=sfdb;password=XEnhdPy8";
 			con = DriverManager.getConnection(databasenavn);
 		} catch (SQLException ex) {
 			Logger.getLogger(StreamFish.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (ClassNotFoundException ex) {
 			Logger.getLogger(StreamFish.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	public Customer getCustomer(int i) {
 		Statement stm;
 		ResultSet res;
 		Customer customer;
 		
 		try {
 			stm = con.createStatement();
 			
 			res = stm.executeQuery("select * from customer where customer_id = " + i);
 			
 			while (res.next()) {
 				int customer_id = res.getInt("customer_id");
 				String customer_name = res.getString("customer_name");
 				String phone = res.getString("phone");
 				byte business = res.getByte("business");
 				boolean busi;
 				if (business == 0) {
 					busi = false;
 				} else {
 					busi = true;
 				}
 				customer = new Customer(customer_id, customer_name, Integer.parseInt(phone), busi);
 				return customer;
 			}
 			
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	public Menu[] getMenus() {
 		
 		Statement stm;
 		ResultSet res;
 		Menu[] menus;
 		int teller = 0;
 		
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from menu where menu.status = '1'");
 			res.next();
 			int ant = res.getInt("antall");
 			menus = new Menu[ant];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select * from menu where menu.status = '1'");
 			
 			while (res.next()) {
 				int menuId = res.getInt("menu_id");
 				String menuName = res.getString("menu_name");
 				int price = res.getInt("price");
 				String description = res.getString("description");
 				boolean busi = false;
 				menus[teller] = new Menu(menuId, menuName, price, description);
 				teller++;
 			}
 			return menus;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 	public Order[] getOrders() {
 		
 		Statement stm;
 		ResultSet res;
 		Order[] orders;
 		int teller = 0;
 		
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from orders where orders.status = '1'");
 			res.next();
 			int ant = res.getInt("antall");
 			orders = new Order[ant];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select * from orders where orders.status = '1'");
 			
 			while (res.next()) {
 				int orderID = res.getInt("order_id");
 				int menuID = res.getInt("menu_id");
 				int customerID = res.getInt("customer_id");
 				int emplID = res.getInt("empl_id");
 				int nrPersons = res.getInt("nr_persons");
 				String deliveryDate = res.getString("delivery_date");
 				String address = res.getString("address");
 				orders[teller] = new Order(orderID, menuID, customerID, emplID, nrPersons, deliveryDate, address);
 				teller++;
 			}
 			return orders;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	public Order[] getOrderCustomer(Customer customer) {
 		Statement stm;
 		ResultSet res;
 		Order[] orders;
 		int counter = 0;
 		
 		try {
 			stm = con.createStatement();
                         res = stm.executeQuery("SELECT COUNT (*) ordercustomer FROM ORDERS JOIN CUSTOMER ON orders.customer_id = customer.customer_id "
                                 + "WHERE orders.CUSTOMER_ID = " + customer.getCustomerID() + " AND orders.status = '1' AND customer.status = '1'");
 			res.next();
 			orders = new Order[res.getInt("ordercustomer")];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("SELECT * FROM ORDERS JOIN CUSTOMER ON orders.customer_id = customer.customer_id "
                                 + "WHERE orders.CUSTOMER_ID = " + customer.getCustomerID() + " AND orders.status = '1' AND customer.status = '1'");
 			while (res.next()) {
 				int order_id = res.getInt("ORDER_ID");
 				int menu_id = res.getInt("MENU_ID");
 				int customer_id = res.getInt("CUSTOMER_ID");
 				int empl_id = res.getInt("EMPL_ID");
 				int num_persons = res.getInt("NR_PERSONS");
 				String date = res.getString("DELIVERY_DATE");
 				String address = res.getString("ADDRESS");
 				
 				orders[counter] = new Order(order_id, menu_id, customer_id, empl_id, num_persons, date, address);
 				counter++;
 			}
 			return orders;
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 	public Customer[] getCustomers(String s, boolean status) {
 		Statement stm;
 		ResultSet res;
 		Customer[] customers;
 		String[] check = {s};
 		check = removeUnwantedSymbols(check);
 		int teller = 0;
                 String aktiv;
                 if (status) {
                     aktiv = "0";
                 } else {
                     aktiv = "1";
                 }
 		
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from customer where (upper(customer_name) like '"
 					+ check[0].toUpperCase() + "%' or phone like '" + check[0] + "%') and customer.status = '" + aktiv + "'");
 			res.next();
 			int ant = res.getInt("antall");
 			customers = new Customer[ant];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select * from customer where (upper(customer_name) like '"
 					+ check[0].toUpperCase() + "%' or phone like '" + check[0] + "%') and customer.status = '" + aktiv + "'");
 			
 			while (res.next()) {
 				int customerId = res.getInt("customer_id");
 				String customerName = res.getString("customer_name");
 				int phone = res.getInt("phone");
 				int business = Integer.parseInt(res.getString("business"));
 				boolean busi = false;
 				if (business == 1) {
 					busi = true;
 				}
 				customers[teller] = new Customer(customerId, customerName, phone, busi);
 				teller++;
 			}
 			return customers;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		} catch (NumberFormatException e2) {
 		}
 		return null;
 	}
 	
 	public Employee[] getEmployees() {
 		Statement stm;
 		ResultSet res;
 		Employee[] employees;
 		int teller = 0;
 		
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from employees where employees.status = '1'");
 			res.next();
 			int ant = res.getInt("antall");
 			employees = new Employee[ant];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select empl_id, user_type, username from employees where employees.status = '1'");
 			
 			while (res.next()) {
 				int emplID = res.getInt("empl_id");
 				byte userType = res.getByte("user_type");
 				String username = res.getString("username");
 				employees[teller] = new Employee(emplID, userType, username);
 				teller++;
 			}
 			return employees;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 	public Orderinfo[] getTodaysTasks() {
 		Statement stm;
 		ResultSet res;
 		int count = 0;
 		Orderinfo[] info;
 		
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("CREATE VIEW todaysTasks AS SELECT customer_name, address, "
 					+ "phone, menu_name, nr_persons, price, username FROM customer\n"
 					+ "LEFT JOIN orders ON customer.CUSTOMER_ID = orders.CUSTOMER_ID\n"
 					+ "LEFT JOIN menu ON orders.MENU_ID = menu.MENU_ID\n"
 					+ "LEFT JOIN employees ON orders.EMPL_ID = employees.EMPL_ID\n"
					+ "WHERE delivery_date = CURRENT DATE AND customer.status = '1';");
 			res.next();
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select count(*) count from todaysTasks");
 			res.next();
 			int ant = res.getInt("count");
 			info = new Orderinfo[ant];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select * from todaysTasks");
 			
 			while (res.next()) {
 				String customerName = res.getString(1);
 				String address = res.getString(2);
 				String phone = res.getString(3);
 				String menu = res.getString(4);
 				int numberOfPersons = res.getInt(5);
 				int price = res.getInt(6);
 				String salesperson = res.getString(7);
 				info[count] = new Orderinfo(customerName, address, phone, menu, numberOfPersons, price, salesperson);
 				count++;
 			}
 			Opprydder.lukkResSet(res);
 			stm.executeQuery("drop view todaysTasks");
 			return info;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return null;
 	}
         
         	public Orderinfo[] getOrderinfo(int orderID) {
 		Statement stm;
 		ResultSet res;
 		int count = 0;
 		Orderinfo[] info;
 		
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("CREATE VIEW orderinfo AS SELECT customer_name, address, "
 					+ "phone, menu_name, nr_persons, price, username FROM customer\n"
 					+ "LEFT JOIN orders ON customer.CUSTOMER_ID = orders.CUSTOMER_ID\n"
 					+ "LEFT JOIN menu ON orders.MENU_ID = menu.MENU_ID\n"
 					+ "LEFT JOIN employees ON orders.EMPL_ID = employees.EMPL_ID\n"
 					+ "WHERE orders.order_ID = '" + orderID + "' AND customer.status = '1' "
                                         + "AND orders.status = '1' ORDER BY delivery_date ASC;");
 			res.next();
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select count(*) count from orderinfo");
 			res.next();
 			int ant = res.getInt("count");
 			info = new Orderinfo[ant];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select * from orderinfo");
 			
 			while (res.next()) {
 				String customerName = res.getString(1);
 				String address = res.getString(2);
 				String phone = res.getString(3);
 				String menu = res.getString(4);
 				int numberOfPersons = res.getInt(5);
 				int price = res.getInt(6);
 				String salesperson = res.getString(7);
 				info[count] = new Orderinfo(customerName, address, phone, menu, numberOfPersons, price, salesperson);
 				count++;
 			}
 			Opprydder.lukkResSet(res);
 			stm.executeQuery("drop view orderinfo");
 			return info;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 	public int addOrder(Order order) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			String[] check = {order.getDeliveryDate(), order.getAddress()};
 			check = removeUnwantedSymbols(check);
 			int succ = stm.executeUpdate("insert into orders (DELIVERY_DATE, ADDRESS, NR_PERSONS, EMPL_ID, MENU_ID,CUSTOMER_ID) "
 					+ "values('" + check[0] + "' , '" + check[1] + "', " + order.getNrPersons() + ", " + order.getEmplId() + ", "
 					+ order.getMenuId() + " , " + order.getCustomerId() + ")");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int addDish(Dish dish) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			String[] check = {dish.getName()};
 			check = removeUnwantedSymbols(check);
 			int succ = stm.executeUpdate("insert into dish (DISH_NAME) "
 					+ "values('" + check[0] + ")");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int changeDishStatus(Dish dish) {
 		Statement stm;
                 ResultSet res;
                 int succ;
 		try {
 			stm = con.createStatement();
                         res = stm.executeQuery("select status from dish where dish_id = " + dish.getID());
 			res.next();
                         int status = Integer.parseInt(res.getString("status"));
                         if(status == 1){
                         succ = stm.executeUpdate("update dish set status ='"+0+"' where dish_id = " + dish.getID());
                         } else{
                         succ = stm.executeUpdate("update dish set status ='"+1+"' where dish_id = " + dish.getID());
                         }
                         Opprydder.lukkResSet(res);
                         Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int addIngredient(Ingredient ingredient) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			String[] check = {ingredient.getName(), ingredient.getExpDate()};
 			check = removeUnwantedSymbols(check);
 			int succ = stm.executeUpdate("insert into ingredients (INGREDIENT_NAME, AMOUNT, EXPIRY_DATE) "
 					+ "values('" + check[0] + "' , '" + ingredient.getAmount() + "' , '" + check[1] + ")");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int deleteIngredient(Ingredient ing) {
 		Statement stm;
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("delete from INGREDIENTS where INGREDIENT_ID = " + ing.getID());
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int addCustomer(Customer customer) {
 		Statement stm;
 		String[] check = {customer.getCustomerName()};
 		check = removeUnwantedSymbols(check);
 		
 		int isbusiness = 0;
 		if (customer.isBusiness()) {
 			isbusiness = 1;
 		}
 		
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into customer (CUSTOMER_NAME, PHONE, BUSINESS)"
 					+ " values('" + check[0] + "' , '" + customer.getPhoneNumber() + "', '" + isbusiness + "')");
 			Opprydder.lukkSetning(stm);
 			return succ;
 			
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int changeCustomerStatus(Customer customer) {
 		Statement stm;
                 ResultSet res;
                 int succ;
 		try {
 			stm = con.createStatement();
                         res = stm.executeQuery("select status from customer where customer_id = " + customer.getCustomerID());
 			res.next();
                         int status = Integer.parseInt(res.getString("status"));
                         if(status == 1){
                         succ = stm.executeUpdate("update customer set status ='"+0+"' where customer_id = " + customer.getCustomerID());
                         } else{
                         succ = stm.executeUpdate("update customer set status ='"+1+"' where customer_id = " + customer.getCustomerID());
                         }
                         Opprydder.lukkResSet(res);
                         Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 
 	public int updateCustomer(Customer customer) {
 		Statement stm;
 		String[] check = {customer.getCustomerName()};
 		check = removeUnwantedSymbols(check);
 		
 		int isbusiness = 0;
 		if (customer.isBusiness()) {
 			isbusiness = 1;
 		}
 		
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("update customer set CUSTOMER_NAME ='" + check[0] + "', PHONE ='" + customer.getPhoneNumber()
 					+ "', BUSINESS ='" + isbusiness + "' where CUSTOMER_ID =" + customer.getCustomerID());
 			Opprydder.lukkSetning(stm);
 			return succ;
 			
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int addCustomerAdress(CustomerAddress adress, Customer customer) {
 		Statement stm;
 		String[] check = {adress.getAdress(), adress.getCity()};
 		check = removeUnwantedSymbols(check);
 		
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into CUSTOMER_ADRESS (ADRESS,ZIP_CODE,CITY,CUSTOMER_ID)"
 					+ " values('" + check[0] + "' , '" + adress.getZipCode() + "', '" + check[1] + "', '" + adress.getCustomerID() + "')");
 			Opprydder.lukkSetning(stm);
 			return succ;
 			
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int addEmployee(Employee employee) {
 		Statement stm;
 		String[] check = {employee.getUsername(), employee.getPassword()};
 		check = removeUnwantedSymbols(check);
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into employee (USER_TYPE, USERNAME, PASSWORD) "
 					+ "values('" + employee.getUsertype() + "', '" + check[0] + "', '" + check[1] + "'");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int addMenu(Menu menu) {
 		Statement stm;
 		String[] check = {menu.getMenuName(), menu.getDescription()};
 		check = removeUnwantedSymbols(check);
 		try {
 			stm = con.createStatement();
 			int succ = stm.executeUpdate("insert into employee (MENU_NAME, PRICE, DESCRIPTION) "
 					+ "values('" + check[0] + "', '" + menu.getPrice() + "', '" + check[1] + "'");
 			Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int changeMenuStatus(Menu menu) {
 		Statement stm;
                 ResultSet res;
                 int succ;
 		try {
 			stm = con.createStatement();
                         res = stm.executeQuery("select status from menu where menu_id = " + menu.getMenuId());
 			res.next();
                         int status = Integer.parseInt(res.getString("status"));
                         if(status == 1){
                         succ = stm.executeUpdate("update menu set status ='"+0+"' where menu_id = " + menu.getMenuId());
                         } else{
                         succ = stm.executeUpdate("update menu set status ='"+1+"' where menu_id = " + menu.getMenuId());
                         }
                         Opprydder.lukkResSet(res);
                         Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public int changeEmployeeStatus(Employee employee) {
 		Statement stm;
                 ResultSet res;
                 int succ;
 		try {
 			stm = con.createStatement();
                         res = stm.executeQuery("select status from employees where empl_id = " + employee.getEmplID());
 			res.next();
                         int status = Integer.parseInt(res.getString("status"));
                         if(status == 1){
                         succ = stm.executeUpdate("update employees set status ='"+0+"' where empl_id = " + employee.getEmplID());
                         } else{
                         succ = stm.executeUpdate("update employees set status ='"+1+"' where empl_id = " + employee.getEmplID());
                         }
                         Opprydder.lukkResSet(res);
                         Opprydder.lukkSetning(stm);
 			return succ;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 		}
 		return -1;
 	}
 	
 	public String[] removeUnwantedSymbols(String[] table) {
 		String[] checkedTable = table;
 		for (int i = 0; i < table.length; i++) {
 			checkedTable[i] = checkedTable[i].replace("'", "");
 			checkedTable[i] = checkedTable[i].replace(";", " ");
 		}
 		return checkedTable;
 	}
 	
 	public void close() {
 		Opprydder.lukkForbindelse(con);
 	}
 }
