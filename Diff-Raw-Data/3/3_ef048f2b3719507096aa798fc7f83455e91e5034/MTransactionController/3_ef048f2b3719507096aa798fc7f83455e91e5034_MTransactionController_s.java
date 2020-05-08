 package com.cs174.starrus.controller;
 import com.cs174.starrus.view.IView;
 import com.cs174.starrus.view.MTransactionView;
 import com.cs174.starrus.model.Customer;
 
 import java.util.Vector;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class MTransactionController implements IController{
 	private boolean 	DEBUG 		= true;
 	private Connection 	conn		= null;
 	private Customer 	c			= Customer.getCustomer();
 	private MTransactionView mtV	= MTransactionView.getView();
 	private ResultSet	rs			= null;
 		
 
 
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void process(String model) {
 		//System.out.println("do some shit after clicking");
 		MTransactionView stsV = MTransactionView.getView();
 
 		try{
 			conn			= DBconnector.getConnection();
 			Statement stmt	= conn.createStatement();
 			
 			if(DEBUG == true){
 				System.out.println("SELECT * FROM MONEY_TRANS WHERE TUSERNAME = '"	+
 										c.getUsername()			+ "'"
 									);
 
 			}
 			rs 		= stmt.executeQuery("SELECT * FROM MONEY_TRANS WHERE TUSERNAME = '"	+
 										c.getUsername()			+ "'"
 										);
 			mtV.getRow().clear();
 			while( rs.next() ){
 				if(DEBUG == true){
 					System.out.println("Getting Row");
 				}
 				Vector<String> newRow = new Vector<String>();
 				String date	= rs.getString("TDATE");
 				int id		= rs.getInt("M_TRANS_ID");
 				int type 	= rs.getInt("TTYPE");
 				float amt	= rs.getFloat("AMOUNT");
 				float balance = rs.getFloat("BALANCE");
 				
 				newRow.add(Integer.toString(id));
 				newRow.add(date);
 				if( type == 2 ){
 					newRow.add("Withdraw");
 				} else if ( type == 1 ){
 					newRow.add("Deposit");
				}
 				newRow.add(Float.toString(amt));
 				newRow.add(Float.toString(balance));
 				mtV.getRow().add(newRow);
 			}
 			mtV.updateView();
 
 		}catch (SQLException e){
 			System.out.println("Exception in MTransactionController");
 		}
 
 		//depo.setView();
 		stsV.setVisible(true);
 	}
 }
 
