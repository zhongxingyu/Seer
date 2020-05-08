 package at.ac.tuwien.secsyseng.sqlinjection;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import at.ac.tuwien.secsyseng.sqlinjection.dao.ConnectionFactory;
 import at.ac.tuwien.secsyseng.sqlinjection.gui.MainFrame;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		try {
 			initializeDb();
 
 			test(); // Later, remove this line and start GUI here
 
 			MainFrame.launch();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void test() throws SQLException {
 		Connection c = ConnectionFactory.getConnection();
 		Statement st = c.createStatement();
 
 		// account test
 		System.out.println("============");
 		System.out.println("account test");
 		System.out.println("============");
 		ResultSet rs = st.executeQuery("select * from account;");
 		while (rs.next()) {
 			System.out.println(rs.getInt("id") + "|" + rs.getString("user")
 					+ "|" + rs.getString("password") + "|"
 					+ rs.getDouble("amount"));
 		}
 		System.out.println("\n");
 
 		// var1data test
 		System.out.println("=============");
 		System.out.println("var1data test");
 		System.out.println("=============");
		rs = st.executeQuery("select * from var2data;");
 		while (rs.next()) {
 			System.out.println(rs.getInt("id") + "|" + rs.getString("product") + "|"
 					+ rs.getString("location") + "|" + rs.getDouble("price"));
 
 		}
 		System.out.println("\n");
 
 		// var2data test
 		System.out.println("=============");
 		System.out.println("var2data test");
 		System.out.println("=============");
 		rs = st.executeQuery("select * from var2data;");
 		while (rs.next()) {
 			System.out.println(rs.getInt("id") + "|" + rs.getInt("aid") + "|"
 					+ rs.getString("vz") + "|" + rs.getDouble("amount"));
 
 		}
 		System.out.println("\n");
 		
 		// group test
 		System.out.println("=============");
 		System.out.println("var3data test");
 		System.out.println("=============");
 		rs = st.executeQuery("select * from var3data;");
 		while (rs.next()) {
 			System.out.println(rs.getInt("id") + "|" + rs.getString("name"));
 		}
 	}
 
 	private static void initializeDb() throws SQLException {
 		Connection c = ConnectionFactory.getConnection();
 		Statement st = c.createStatement();
 
 		// Create table account
 		st.executeUpdate("create table account (" + "id integer primary key,"
 				+ "user varchar(255) not null,"
 				+ "password varchar(255) not null,"
 				+ "amount numeric not null);");
 
 		// Create table var1data
 		st.executeUpdate("create table var1data (" + "id integer primary key,"
 				+ "product varchar(255) not null,"
 				+ "location varchar(255) not null,"
 				+ "price numeric not null);");
 
 		// Create table var3data
 		st.executeUpdate("create table var3data (" + "id integer primary key,"
 				+ "name varchar(255) not null );");
 
 		// Create table var2data
 		st.executeUpdate("create table var2data (" + "id integer primary key,"
 				+ "aid integer," + "vz varchar(255) not null,"
 				+ "amount numeric not null,"
 				+ "foreign key(aid) references account(id));");
 
 		// Insert into account
 		st.executeUpdate("insert into account values (0, 'christian', 'pass1', 10000);");
 		st.executeUpdate("insert into account values (1, 'aljosha', 'pass2', 11000);");
 		st.executeUpdate("insert into account values (2, 'johannes', 'pass3', 9000);");
 		st.executeUpdate("insert into account values (3, 'klaus', 'pass4', 8000);");
 		st.executeUpdate("insert into account values (4, 'wong', 'dong', 666);");
 
 		// Insert into var1data
 		st.executeUpdate("insert into var1data values (0, 'Agile Custom Interceptor 7 String', 'America', 1325);");
 		st.executeUpdate("insert into var1data values (1, 'Wong Dong', 'Austria - InetSec', 25);");
 		st.executeUpdate("insert into var1data values (2, 'Matt Damon', 'America', 1);");
 		st.executeUpdate("insert into var1data values (3, 'Fiat Stilo', 'Austria', 7000);");
 
 		// Inserts into var3data
 		st.executeUpdate("insert into var3data values (1, 'Gruppe 1');");
 		st.executeUpdate("insert into var3data values (2, 'Gruppe 2');");
 		st.executeUpdate("insert into var3data values (3, 'Gruppe 3');");
 		st.executeUpdate("insert into var3data values (4, 'Gruppe 4');");
 		st.executeUpdate("insert into var3data values (5, 'Gruppe 5');");
 		// st.executeUpdate("insert into var3data values (6, 'Gruppe 6'); drop table var3data;");
 
 		// Insert into var2data
 		st.executeUpdate("insert into var2data values (0, 4, 'ebay - Bad Toy Boy', 666);");
 		st.executeUpdate("insert into var2data values (1, 4, 'Wien Energie', 1000);");
 		st.executeUpdate("insert into var2data values (2, 4, 'Studiengebhren fr TU Wien', 10000);");
 		st.executeUpdate("insert into var2data values (3, 4, 'Hacking activity', 666);");
 		st.executeUpdate("insert into var2data values (4, 3, 'Guitar amp', 800);");
 		st.executeUpdate("insert into var2data values (5, 3, 'Novarock', 140);");
 		st.executeUpdate("insert into var2data values (6, 2, 'Panasonic TX-P42GT50E', 1099);");
 		st.executeUpdate("insert into var2data values (7, 2, 'Panasonic TX-P50VT50E', 2087);");
 		st.executeUpdate("insert into var2data values (8, 1, 'Bier Einkauf', 50);");
 		st.executeUpdate("insert into var2data values (9, 1, 'BSC Abschlussfeier', 450);");
 		st.executeUpdate("insert into var2data values (10, 0, 'Agile Custom Interceptor 7-String Model', 1325);");
 		st.executeUpdate("insert into var2data values (11, 0, 'Klimagert', 400);");
 
 	}
 }
