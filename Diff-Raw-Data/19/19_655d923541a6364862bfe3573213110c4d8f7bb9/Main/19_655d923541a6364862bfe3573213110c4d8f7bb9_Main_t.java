 import java.sql.*;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	
 	static String m_url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 	static String m_driverName = "oracle.jdbc.driver.OracleDriver";
 
 	static String m_userName = "schwehr";
 	static String m_password = "urarurar12";
 
 	static Connection m_con;
 	
 	static Statement stmt;
 	
 	static boolean menu;
 	
 	static String createString;
 	static String user;
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		
 		try
 		{
 
 		Class drvClass = Class.forName(m_driverName);
 		DriverManager.registerDriver((Driver)
 		drvClass.newInstance());
 
 		} catch(Exception e)
 		{
 
 		System.err.print("ClassNotFoundException: ");
 		System.err.println(e.getMessage());
 
 		}
 
 		System.out.println("Welcome! Please log in with your e-mail and password or choose the next option " +
 				"to register if you don't have an account yet. (Type the corresponding number for the option you want)");
 		
 		System.out.println("1. Login (for registered users)");
 		System.out.println("2. Register");
 		System.out.println("3. Exit");
 		
 		int input = Keyboard.in.readInteger().intValue();
 		menu = true;
 		
 		while(menu){
 			
 			switch(input){
 			case 1:
 				if(login()){
 					//UserPage nUser = new UserPage();
 					//nUser.startUp();
 					System.out.println("Login successful... Please wait to be directed to the user screen.");
 				}
 				else
 					System.out.println("Username or password is wrong. Please try again.");
 				break;
 			case 2:
 				register();
 				break;
 			case 3:
 				menu = false;
 				break;
 			default:
 				System.out.println("Please enter a valid value determined by the numbered options listed.");
 				break;
 			}
 			
 			if(menu){
 				System.out.println("1. Login (for registered users)");
 				System.out.println("2. Register");
 				System.out.println("3. Exit");
 				input = Keyboard.in.readInteger().intValue();
 			}
 			
 		}
 		
 		System.out.println("Goodbye!");
 		
 	}
 	
 	public static boolean login(){
 		
 		String pass;
 		
 		System.out.print("E-mail: ");
 		user = Keyboard.in.readString();
 		
 		System.out.print("Password: ");
 		pass = Keyboard.in.readString();
 		
 		createString = "select email, pwd from users where email ='"+user+"' and pwd ='"+pass+"'";
 		
 		boolean exists = false;
 		
 		try
 		{
 
 		m_con = DriverManager.getConnection(m_url, m_userName, m_password);
 
 		stmt = m_con.createStatement();
 		ResultSet rs = stmt.executeQuery(createString);		
 		
 		SQLException x = new SQLException();
 		
 		if(!rs.next());
 		else
 			exists = true;
 		
 		stmt.close();
 		m_con.close();
 
 		} catch(SQLException ex) {
 
 		System.err.println("SQLException: " +
 		ex.getMessage());
 
 		}
 		
 		if(exists)
 			return true;
 		else
 			return false;
 	}
 	
 	public static void register(){
 		String email, name, city, pass, pass2; 
 		char[] gender;
 		
 		System.out.println("Please enter your information as follows (type 1 and press enter if you have to go back " +
 				"to change your information):");
 		System.out.print("Please enter your e-mail: ");
 		email = Keyboard.in.readString();
 		System.out.print("Now your (full) name: ");
 		name = Keyboard.in.readString();
 		System.out.print("City: ");
 		city = Keyboard.in.readString();

		do
		{
 			System.out.print("Please enter either M or F for gender: ");
			gender = Keyboard.in.readString().toLowerCase().toCharArray();
		} while( !(gender.length == 1 && (gender[0] == 'm' || gender[0] == 'f')));

 		System.out.print("Finally, enter your password: ");
 		pass = Keyboard.in.readString();
 		System.out.print("Confirm your password: ");
 		pass2 = Keyboard.in.readString();
 		while(pass.compareTo(pass2) != 0){
 			System.out.print("Please re-enter your password: ");
 			pass = Keyboard.in.readString();
 			System.out.print("Confirm your password: ");
 			pass2 = Keyboard.in.readString();
 		}
 		System.out.println("Thank you. Entering information into database... Please wait a moment.");
 		
 		createString = "insert into users values ('"+email+"','"+name+"','"+city+"','"+gender[0]+"','"+pass+"')";
 		
 		try{
 			m_con = DriverManager.getConnection(m_url, m_userName, m_password);
 			stmt = m_con.createStatement();
 			stmt.executeUpdate(createString);
 			
 			stmt.close();
 			m_con.close();
 			
 			System.out.println("Congrats! You have created your account. You will now be directed to the login screen.");
 			Keyboard p = new Keyboard();
 			p.pause();
 		} catch(SQLException ex) {
 
 			System.err.println("SQLException: " +
 			ex.getMessage());
 
 		}
 	}
 
}
