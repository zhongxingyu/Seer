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
 	
     public Menu[] getMenus() {
 
         Statement stm;
         ResultSet res;
         Menu[] menus;
         int teller = 0;
 
         try {
             stm = con.createStatement();
             res = stm.executeQuery("select count(*) antall from menu");
             res.next();
             int ant = res.getInt("antall");
             menus = new Menu[ant];
             Opprydder.lukkResSet(res);
 
             res = stm.executeQuery("select * from menu");
 
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
             res = stm.executeQuery("select count(*) antall from orders");
             res.next();
             int ant = res.getInt("antall");
             orders = new Order[ant];
             Opprydder.lukkResSet(res);
 
             res = stm.executeQuery("select * from orders");
 
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
 
     public Customer[] getCustomers(String s){
 		Statement stm;
 		ResultSet res;
 		Customer[] customers;
 		int teller = 0;
 		
 		try {
 			stm = con.createStatement();
 			res = stm.executeQuery("select count(*) antall from customer where customer_name like '" + s.toLowerCase() 
                                 + "%' or customer_name like '" + s.toUpperCase() + "%' or phone like " + Integer.parseInt(s) + "%");
 			res = stm.executeQuery("select count(*) antall from customer where customer_name like '" + s.toLowerCase()
                                 + "%' or customer_name like '" + s.toUpperCase() + "%' or phone like '" + Integer.parseInt(s) + "%'");
 			res.next();
 			int ant = res.getInt("antall");
 			customers = new Customer[ant];
 			Opprydder.lukkResSet(res);
 			
 			res = stm.executeQuery("select * from customer where customer_name like '" + s.toLowerCase() 
                                 + "%' or customer_name like '" + s.toUpperCase() + "%' or phone like " + Integer.parseInt(s) + "%");
 			res = stm.executeQuery("select * from customer where customer_name like '" + s.toLowerCase()
                                 + "%' or customer_name like '" + s.toUpperCase() + "%' or phone like '" + Integer.parseInt(s) + "%'");
 			
 			while(res.next()){
 				int customerId = res.getInt("customer_id");
 				String customerName = res.getString("customer_name");
 				int phone = res.getInt("phone");
 				int business = Integer.parseInt(res.getString("business"));
 				boolean busi = false;
 				if(business == 1){
 					busi = true;
 				}
 				customers[teller] = new Customer(customerId, customerName, phone, busi);
 				teller++;
 			}
 			return customers;
 		} catch (SQLException ex) {
 			System.err.println(ex);
 			ex.printStackTrace();
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
             res = stm.executeQuery("select count(*) antall from employees");
             res.next();
             int ant = res.getInt("antall");
             employees = new Employee[ant];
             Opprydder.lukkResSet(res);
 
             res = stm.executeQuery("select empl_id, user_type, username from employees");
 
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
 
     public int addOrder(Order order) {
         Statement stm;
         try {
             stm = con.createStatement();
             String[] check = {order.getDeliveryDate(), order.getAddress()};
             check = removeUnwantedSymbols(check);
             int succ = stm.executeUpdate("insert into orders (DELIVERY_DATE, ADDRESS, NR_PERSONS, EMPL_ID, MENU_ID,CUSTOMER_ID) values('" + check[0] + "' , '" + check[1] + "', " + order.getNrPersons() + ", " + order.getEmplId() + ", " + order.getMenuId() + " , " + order.getCustomerId() + ")");
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
         System.out.println(check[0]);
         int isbusiness = 0;
         if (customer.isBusiness()) {
             isbusiness = 1;
         }
 
         try {
             stm = con.createStatement();
             int succ = stm.executeUpdate("insert into customer (CUSTOMER_NAME, PHONE, BUSINESS) values('" + check[0] + "' , '" + customer.getPhoneNumber() + "', '" + isbusiness + "')");
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
             System.out.println(checkedTable[i]);
         }
         return checkedTable;
     }
 
 	public void close() {
 		try {
 			con.close();
 
 		} catch (SQLException ex) {
 			Logger.getLogger(StreamFish.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 }
