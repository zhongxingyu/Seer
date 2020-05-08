 package database;
 
 import java.sql.*;
 
 public class Connect {
 	private Statement statement;
 	private Connection conn;
 	public int totalNum;
 	public String[] name, id, idNum, faculty, pic;
 
 	public Connect() throws Exception {
 		String url = "jdbc:mysql://"
 				+ Configure.databaseAddress
 				+ "/"
 				+ Configure.database
 				+ "?characterEncoding=utf8&jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull";
 		Class.forName("com.mysql.jdbc.Driver");
 		conn = DriverManager.getConnection(url, Configure.user,
 				Configure.password);
 		statement = conn.createStatement();
 	}
 
 	public void close() {
 		try {
 			statement.close();
 			conn.close();
 		} catch (Exception e) {
 		}
 	}
 
 	public ResultSet get(String query) throws Exception {
 		ResultSet rs;
 		statement.setFetchSize(1001);
 		if ((query == null) || query.equals(""))
 			rs = statement
 					.executeQuery("select name,id,idnum,faculty,pic from "
 							+ Configure.table);
 		else
 			rs = statement
 					.executeQuery("select name,id,idnum,faculty,pic from "
 							+ Configure.table + " where " + query);
 		return rs;
 	}
 
 	public int getCount(String query) throws Exception {
 		ResultSet rs;
 		if ((query == null) || query.equals(""))
 			rs = statement.executeQuery("select count(*) from "
 					+ Configure.table);
 		else
 			rs = statement.executeQuery("select count(*) from "
 					+ Configure.table + " where " + query);
 		rs.next();
 		int countNum = rs.getInt(1);
 		rs.close();
 		return countNum;
 	}
 
 	public void delete(String id) throws Exception {
 		statement.executeUpdate("delete from " + Configure.table
 				+ " where id='" + id + "'");
 	}
 
 	public String[] getEnumList(int x, int y) {
 		String[] list;
 		try {
 			ResultSet rs = statement.executeQuery("show columns from "
 					+ Configure.table + " like '" + List.COLUMN_NAME[x][y]
 					+ "'");
 			rs.next();
 			String enums = rs.getString("Type");
 			int position = 0, count = 0;
			while ((position = enums.indexOf('\'', position)) > 0) {
				position = enums.indexOf('\'', position + 1) + 1;
 				count++;
 			}
 			position = 0;
 			list = new String[count + 1];
 			list[0] = "";
 			for (int i = 1; i <= count; i++) {
				position = enums.indexOf('\'', position);
				int secondPosition = enums.indexOf('\'', position + 1);
 				list[i] = enums.substring(position + 1, secondPosition);
 				position = secondPosition + 1;
 			}
 		} catch (Exception e) {
 			list = new String[0];
 		}
 		return list;
 	}
 
 	public ResultSet getOne(String id) throws Exception {
 		return statement.executeQuery("select * from " + Configure.table
 				+ " where id='" + id + "'");
 	}
 
 	public void getData(String query) throws Exception {
 		totalNum = getCount(query);
 		ResultSet rs = get(query);
 		int num = (totalNum > 1000) ? 1000 : totalNum;
 		name = new String[num];
 		id = new String[num];
 		idNum = new String[num];
 		faculty = new String[num];
 		pic = new String[num];
 		for (int i = 0; i < num; i++) {
 			rs.next();
 			name[i] = rs.getString("name");
 			id[i] = rs.getString("id");
 			idNum[i] = rs.getString("idnum");
 			faculty[i] = rs.getString("faculty");
 			pic[i] = rs.getString("pic");
 		}
 		rs.close();
 	}
 
 	public String[] getID(String query) throws Exception {
 		String[] id = new String[totalNum];
 		ResultSet rs = get(query);
 		for (int i = 0; i < totalNum; i++) {
 			rs.next();
 			id[i] = rs.getString("id");
 		}
 		rs.close();
 		return id;
 	}
 
 	public boolean exist(String id) throws Exception {
 		ResultSet rs = getOne(id);
 		boolean flag = false;
 		if (rs.next())
 			flag = true;
 		return flag;
 	}
 
 	public void insert(String data) throws Exception {
 		statement.executeUpdate("insert into " + Configure.table + " ("
 				+ List.COLUMN_STRING + ") values (" + data + ")");
 	}
 
 	public void update(String id, String col, String data) throws Exception {
 		statement.executeUpdate("update " + Configure.table + " set " + col
 				+ "=" + data + " where id='" + id + "'");
 	}
 }
