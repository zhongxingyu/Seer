 package database;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import model.authentication.User;
 
 public class DataBase {
 
	private static final String mdbFile = "C:\\Users\\ph.alves\\workspace\\t3\\T1_Seg_20132.mdb";
 	
 	private Connection connection;
 	
 	private static DataBase instance = null;
 
 	public static DataBase getDataBase(){
 		if (instance == null)
 			setInstance(new DataBase());
 		return getInstance();
 	}
 
 	public void connectToDataBase(){
 		Connection connection = null;
 		
 		try {
 			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
 			String connectionString = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + mdbFile + ";";
 			connection = DriverManager.getConnection(connectionString, "", "");
 
 			this.connection = connection;
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Couldn't stablish connection to " + mdbFile);
 		}
 	}
 
 	public void disconnectFromDataBase() {
 		try {
 			this.connection.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to disconnect from " + mdbFile);
 		}
 	}
 	
 	public static DataBase getInstance() {
 		return instance;
 	}
 
 	public static void setInstance(DataBase instance) {
 		DataBase.instance = instance;
 	}
 
 	public String selectName(String name) {
 		String returningName = "";
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				returningName = resultSet.getString("UserName");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return returningName;
 	}
 	
 	public User getUser(String name) {
 		User user = new User();
 		
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				user.setLoginName(resultSet.getString("UserName"));
 				user.setNomeProprio(resultSet.getString("Nome"));
 				user.setRole(resultSet.getString("Grupos_Id"));
 				user.setSALT(resultSet.getString("SALT"));
 				user.setPasswd(resultSet.getString("Passwd"));
 				
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return user;
 	}
 	
 	public boolean select_ONE_TIME_PASSWORD(String userName, String oneTimePassword, Integer oneTimePasswordIndex){
 		Integer returningPasswd = null;
 		String valueTan = null;
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + userName + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 			while(resultSet.next()) {
 				returningPasswd = resultSet.getInt("TanList");
 				break;
 			}
 			resultSet.close();
 			stmt.close();
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 		if(returningPasswd != 0)
 		{
 			String sql2 = "SELECT * FROM TamList WHERE ID = " + returningPasswd + ";";
 			try {
 				Statement stmt = connection.createStatement();
 				ResultSet resultSet = stmt.executeQuery(sql2);
 				while(resultSet.next()) {
 					valueTan = resultSet.getString(oneTimePasswordIndex.toString());
 					break;
 				}
 			}catch (SQLException e) {
 				e.printStackTrace();
 				System.err.println("Unable to realize '" + sql2 + "' command");
 			}
 	
 			if(valueTan.equals(oneTimePassword))
 			{
 				return true;
 			}
 			
 		}
 		return false;
 	}
 
 
 	public boolean isUserBlocked(String name) {
 		Date blockedTime = null;
 		Date blockedDate = null;
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				blockedDate = resultSet.getDate("BlockedDate");
 				blockedTime = resultSet.getTime("BlockedTime");
 				break;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		if( blockedTime != null && blockedDate != null ) {
 
 			Calendar dateBlocked = Calendar.getInstance();
 			Calendar timeBlocked = Calendar.getInstance();
 			timeBlocked.setTime( blockedTime );
 			dateBlocked.setTime( blockedDate );
 
 			dateBlocked.set(dateBlocked.get(Calendar.YEAR),
 					dateBlocked.get(Calendar.MONTH),
 					dateBlocked.get(Calendar.DAY_OF_MONTH),
 					timeBlocked.get(Calendar.HOUR_OF_DAY),
 					timeBlocked.get(Calendar.MINUTE),
 					timeBlocked.get(Calendar.SECOND));
 
 			Calendar dateTimeNow =  Calendar.getInstance();	
 			dateBlocked.add( Calendar.MINUTE, 2 );
 
 			if( dateBlocked.after( dateTimeNow ) ) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public String selectSALT(String name) {
 		String returningSALT = "";
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				returningSALT = resultSet.getString("SALT");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return returningSALT;
 	}
 	
 	public String selectPasswd(String name) {
 		String returningPasswd = "";
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				returningPasswd = resultSet.getString("Passwd");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return returningPasswd;
 	}
 	
 	public String selectLoginName(String name) {
 		String returningName = "";
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				returningName = resultSet.getString("Nome");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return returningName;
 	}
 
 	public String getUserRole(String name) {
 		String role = "";
 		String sql = "SELECT * FROM Usuarios " +
 				"INNER JOIN Grupos ON Usuarios.Grupos_Id = Grupos.Id " +
 				"WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				role = resultSet.getString("Description");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return role;
 	}
 	
 	public void blockUser(String name) {
 		Date now = new Date();
 		String date = new SimpleDateFormat("dd/MM/yyyy").format( now );
 		String time = new SimpleDateFormat("HH:mm:ss").format( now );
 		String sql = "UPDATE Usuarios SET BlockedDate = '" + date + "'," +
 				" BlockedTime = '" + time + "' WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 	
 	public int getNumberOfAccess(String name) {
 		int n = 0;
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				n = resultSet.getInt("Access");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return n;
 	}
 	
 	public int totalUsers() {
 		int n = 0;
 		String sql = "SELECT COUNT(*) AS N FROM Usuarios;";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				n = resultSet.getInt("N");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return n;
 	}
 	
 	public void saveUser(User user, int role_Id) {
 		String sql = "INSERT INTO Usuarios(UserName, Nome, SALT, Passwd, PublicKey, Grupos_Id, TanList)" +
 				" VALUES('"+ user.getLoginName() + "', '" + user.getNomeProprio() + "', " +
 				user.getSALT() + ", '" + user.getPasswd() + "', '" +
 				user.getPublicKey() + "', " + role_Id + ", '" + user.getIdTanList()+"' " + ");";
 		try {
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 	
 	public void storeTanList(ArrayList<String> tanList){
 		
 		String sql="INSERT INTO TamList (1,2,3,4,5,6,7,8,9,10) VALUES(";
 		for(int i=0;i<10; i++)
 		{				
 			if(i<tanList.size())
 				sql+="'"+tanList.get(i)+"'";
 			else
 				sql+="' '";				
 			
 			if(i==9)
 				sql+=");";
 			else
 				sql+=",";
 		}
 		try{
 			
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 			
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 	
 	public int lastIndex(){
 		int n = 0;
 		
 		String sql = "select MAX(ID) from TamList";
 		
 		try{
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				n = resultSet.getInt(1);
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 		return n;
 	}
 	
 	public int getNumOfQueries(String name) {
 		int numOfQueries = 0;
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				numOfQueries = resultSet.getInt("NumOfQueries");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return numOfQueries;
 	}
 	
 	public String selectPublicKey(String name) {
 		String returningPublicKey = "";
 		String sql = "SELECT * FROM Usuarios WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			ResultSet resultSet = stmt.executeQuery(sql);
 
 			while(resultSet.next()) {
 				returningPublicKey = resultSet.getString("PublicKey");
 				break;
 			}
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 
 		return returningPublicKey;
 	}
 	
 	public void logMessage(int messageCode, String userName) {
 		Date now = new Date();
 		String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format( now );
 		String sql = "INSERT INTO Registros(Mensagens_Code, Usuarios_UserName, Data)" +
 				" VALUES("+ messageCode + ", '" + userName + "', '" + date + "');";
 		try {
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 	
 	public void logMessage(int messageCode) {
 		Date now = new Date();
 		String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format( now );
 		String sql = "INSERT INTO Registros(Mensagens_Code, Data)" +
 				" VALUES("+ messageCode + ", '" + date + "');";
 		try {
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 	
 	public void logMessage(int messageCode, String userName, String arqName) {
 		Date now = new Date();
 		String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format( now );
 		String sql = "INSERT INTO Registros(Mensagens_Code, Usuarios_UserName, Data, ArqName)" +
 				" VALUES("+ messageCode + ", '" + userName + "', '" + date + "', '" + arqName + "');";
 		try {
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 	
 	public void incrementNumOfQueries(String name) {
 		String sql = "UPDATE Usuarios SET NumOfQueries = NumOfQueries + 1 WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 	
 	public void incrementAccess(String name) {
 		String sql = "UPDATE Usuarios SET Access = Access + 1 WHERE UserName = '" + name + "';";
 		try {
 			Statement stmt = connection.createStatement();
 			stmt.executeUpdate(sql);
 		}catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println("Unable to realize '" + sql + "' command");
 		}
 	}
 }
 
