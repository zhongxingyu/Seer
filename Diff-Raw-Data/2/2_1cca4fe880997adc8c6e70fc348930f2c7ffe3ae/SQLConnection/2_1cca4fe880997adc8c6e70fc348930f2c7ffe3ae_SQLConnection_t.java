 package ConnectToSQL;
 import java.sql.*;
 
 
 public class SQLConnection {
 
 String Host = "HSU1008KVR";
 String Username = "baoquang";
 String Password = "chapel";
String Database = "QuanLySinhVien";
 String Port = "1492";
 Connection connect = null;
 Statement statement = null;
 ResultSet result = null;
 
 public class SQLConnection(String Host,String Port,String Username,String Password,String Database)
 {
 	this.Host = Host;
 	this.Port = Port;
 	this.Username = Username;
 	this.Password = Password;
 	this.Database = Database;
 	}
 	// ham kiem tra driver co trong may cua chung ta hay chua
 	protected void driverTest () throws Exception
 	{
 	try
 	{
 	//kiem tra driver
 	Class.forName("com.microsoft.sqlserver.jdbc.SQLSer verDriver");
 	}
 	//neu ko thay nem loi ra ngoai
 	catch(java.lang.ClassNotFoundException e)
 	{
 	throw new Exception("My SQL JDBC Driver not found ...");
 	}
 	}
 	protected Connection getConnect() throws Exception
 	{
 	//neu connect = null thi khoi tao moi
 	if(this.connect == null)
 	{
 	//kiem tra driver
 	driverTest();
 	//tao chuoi ket noi
 	String url = "jdbc:sqlserver://"+this.Host+":"+this.Port+";databaseName="+this.Database+";user="+this.Username+";password="+this.Password ;
 	try
 	{
 	//tao connection thong qua chuoi ket noi
 	this.connect = DriverManager.getConnection(url);
 	}
 	// neu ko thanh cong nem loi ra ngoai
 	catch(java.sql.SQLException e)
 	{
 	throw new Exception("Khong The Ket Noi Den Database Server ..." + url + e.getMessage());
 	}
 
 	}
 
 	//tra connect ra ngoai
 
 	return this.connect;
 	}
 
 	protected Statement getStatement() throws SQLException, Exception
 	{
 	//kiem tra statemen = null hoac da dong thi mo lai
 	if(this.statement == null || this.statement.isClosed() == true)
 	{
 	//khoi tao 1 statement moi
 	this.statement = this.getConnect().createStatement();
 	}
 	//tra statement ra ngoai
 	return this.statement;
 	}
 	public ResultSet excuteQuery(String Query) throws Exception
 	{
 	try
 	{
 	//thuc thi cau lenh sql
 	this.result = getStatement().executeQuery(Query);
 	}
 	//neu ko thanh cong nem loi ra ngoai
 	catch(Exception e)
 	{
 	throw new Exception("Error: "+e.getMessage());
 	}
 	//thuc thi xong tra statement ve null
 	this.statement = null;
 	// tra ve result
 	return this.result;
 	}
 	public int excuteUpdate(String Query) throws Exception
 	{
 	int res = Integer.MIN_VALUE;
 	try
 	{
 	//thuc thi cau lenh sql
 	res = getStatement().executeUpdate(Query);
 	}
 	catch(Exception e)
 	{
 	throw new Exception("Error: "+e.getMessage());
 	}
 	finally
 	{
 	//dong ket noi
 	this.Close();
 	}
 	// tra ket qua ra ngoai
 	return res;
 	}
 	/* ham excuteQuery voi ham excuteUpdate khac nhau o cho la ham update se dong
 	ket noi co so du lieu sau khi da thuc thi còn excuteQuery thi ko dong ket noi
 	co so du lieu sau khi da thuc thi vi sau khi thuc thi xong cai resule
 	con su dung neu dong se bao loi
 	*
 	*/
 
 	public void Close() throws SQLException
 	{
 	if(this.result != null)
 	{
 	this.result.close();
 	this.result = null;
 	}
 	if(this.statement != null)
 	{
 	this.statement.close();
 	this.statement = null;
 	}
 
 	if(this.connect != null)
 	{
 	this.connect.close();
 	this.connect = null;
 	}
 	}
 
 	// ham dong ket noi co so du lieu thi fai dong tuan tu nhu tren
 }
 
 
