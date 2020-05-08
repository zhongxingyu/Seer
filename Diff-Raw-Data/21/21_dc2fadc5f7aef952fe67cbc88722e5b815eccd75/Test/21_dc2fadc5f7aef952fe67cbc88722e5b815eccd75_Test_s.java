 package com.niccholaspage.nSQL;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import com.niccholaspage.nSQL.connection.SQLiteConnection;
 import com.niccholaspage.nSQL.query.CreateQuery;
 import com.niccholaspage.nSQL.query.DeleteQuery;
 import com.niccholaspage.nSQL.query.InsertQuery;
 import com.niccholaspage.nSQL.query.SelectQuery;
 import com.niccholaspage.nSQL.query.UpdateQuery;
 
 public class Test {
 	public static void main(String[] args){
 		Connection connection = new SQLiteConnection(new File("database.db")).getConnection();
 		
 		Table table = new Table(connection, "nSQL_accounts");
 		
 		CreateQuery createQuery = table.create().create("INTEGER PRIMARY KEY AUTOINCREMENT").create("name varchar(64) NOT NULL");
 		
 		createQuery.execute();
 		
 		System.out.println(createQuery);
 		
 		SelectQuery selectQuery = table.select("*").where("name", "gabe").where("money", "100").where("gabe", "yolo");
 		
 		System.out.println(selectQuery);
 		
 		UpdateQuery updateQuery = table.update().set("name", "yolo").set("money", 1230).set("gabe", "gabe");
 		
 		System.out.println(updateQuery);
 		
		InsertQuery insertQuery = table.insert().insert("name").insert("money").value("yolo").value("212");
 		
 		System.out.println(insertQuery);
 		
 		DeleteQuery deleteQuery = table.delete().where("name", "gabe").where("money", 100.281);
 		
 		System.out.println(deleteQuery);
 		
 		try {
 			connection.close();
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 }
