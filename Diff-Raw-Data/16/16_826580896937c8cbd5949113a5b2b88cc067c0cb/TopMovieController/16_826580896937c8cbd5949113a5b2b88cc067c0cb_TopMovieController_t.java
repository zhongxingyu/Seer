 package com.cs174.starrus.controller;
 import com.cs174.starrus.view.IView;
 import com.cs174.starrus.view.CustomerView;
 import com.cs174.starrus.model.Customer;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import java.text.SimpleDateFormat;
 import java.text.DateFormat;
 import java.util.Date;
 
 import java.text.DecimalFormat;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.table.*;
 
 
 public class TopMovieController implements IController{
 	private boolean 	DEBUG		= true;
 	private Connection	conn		= null;
 	private CustomerView cV			= CustomerView.getView();
 	private Customer	c			= Customer.getCustomer();
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void process(String model) {
 		Statement 	stmt;
 		ResultSet	rs;
 
 		try{
 			conn 	= DBconnector.getConnection();
 			stmt	= conn.createStatement();
 			DateFormat format   = new SimpleDateFormat("dd-MMM-yy");
 			Date today          = new Date();
 			String dateString   = format.format(today);
 
 			if( DEBUG == true ){
 				System.out.println("SELECT * FROM MOVIES WHERE RATING = 5.0 AND PRODUCTION BETWEEN "	+
 									cV.getTextFromFieldString() + " AND " + cV.getTextToFieldString()
 									);
 
 			}
 			
 			rs 		= stmt.executeQuery("SELECT * FROM MOVIES WHERE RATING = 5.0 AND PRODUCTION BETWEEN "	+
 										cV.getTextFromFieldString() + " AND " + cV.getTextToFieldString()
 										);
 
 			Vector<Vector<String>> 	newTable = new Vector<Vector<String>>();
 			Vector<String> 			newEntry = new Vector<String>();
			
			cV.getRow_listMovie().clear();
 			while( rs.next()){
 
 				if( DEBUG == true ){
 					System.out.println(rs.getString("TITLE"));
 					System.out.println(Integer.toString(rs.getInt("PRODUCTION")));
 					System.out.println(rs.getString("ORGANIZATION"));
 					System.out.println(Float.toString(rs.getFloat("RATING")));
 		
 				}
 				newEntry.add(rs.getString("TITLE"));
 				newEntry.add(Integer.toString(rs.getInt("PRODUCTION")));
 				newEntry.add(rs.getString("ORGANIZATION"));
 				newEntry.add(Float.toString(rs.getFloat("RATING")));
				cV.getRow_listMovie().add(newEntry);
 			}
			
			cV.getTable_listMovie().revalidate();
			cV.getTable_listMovie().repaint();
 		}catch (SQLException e){
 			System.out.println("SQLException in DTERController ");
 			e.printStackTrace();
 		}
 		
 	}
 }
 
