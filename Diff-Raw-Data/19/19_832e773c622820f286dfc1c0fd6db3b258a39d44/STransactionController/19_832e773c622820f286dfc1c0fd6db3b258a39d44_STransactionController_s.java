 package com.cs174.starrus.controller;
 import com.cs174.starrus.view.IView;
 import com.cs174.starrus.view.STransactionView;
 import com.cs174.starrus.model.Customer;
 
 import java.util.Vector;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class STransactionController implements IController{
 	private boolean 	DEBUG 		= true;
 	private Connection 	conn		= null;
 	private Customer 	c			= Customer.getCustomer();
 	private STransactionView stV	= STransactionView.getView();
 	private ResultSet	rs			= null;
 		
 
 
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void process(String model) {
 		//System.out.println("do some shit after clicking");
 		STransactionView stsV = STransactionView.getView();
 
 		try{
 			conn			= DBconnector.getConnection();
 			Statement stmt	= conn.createStatement();
 			
 			if(DEBUG == true){
 				System.out.println("SELECT * FROM STOCK_TRANS WHERE SUSERNAME = '"	+
 										c.getUsername()			+ "'"
 									);
 
 			}
 			rs 		= stmt.executeQuery("SELECT * FROM STOCK_TRANS WHERE SUSERNAME = '"	+
 										c.getUsername()			+ "'"
 										);
 			while( rs.next() ){
 				if(DEBUG == true){
 					System.out.println("Getting Row");
 				}
 				Vector<String> newRow = new Vector<String>();
 				String date	= rs.getString("TDATE");
 				int id		= rs.getInt("S_TRANS_ID");
 				int type 	= rs.getInt("STYPE");
 				String tkr	= rs.getString("SYMBOL");
 				int qty		= rs.getInt("SHARES");
 				float price	= rs.getFloat("PRICE");
 
 				newRow.add(date);
 				newRow.add(Integer.toString(id));
 
 				if( type == 0 ){
 					newRow.add("Buy");
 				} else if ( type == 1 ){
 					newRow.add("Sell");
 				}
 				newRow.add(tkr);
 				newRow.add(Integer.toString(qty));
 				newRow.add(Float.toString(price));
 				
 				stV.getRow().add(newRow);
 
 			}
 			stV.updateView();
 
 		}catch (SQLException e){
 			System.out.println("Exception in STransactoinController");
 		}
 
 		//depo.setView();
 		stsV.setVisible(true);
 	}
 }
 
