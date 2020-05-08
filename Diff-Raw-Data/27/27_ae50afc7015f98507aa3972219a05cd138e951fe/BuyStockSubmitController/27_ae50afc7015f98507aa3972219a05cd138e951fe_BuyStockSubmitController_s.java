 package com.cs174.starrus.controller;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Vector;
 
 import com.cs174.starrus.model.Customer;
 import com.cs174.starrus.model.SysDate;
 import com.cs174.starrus.view.CustomerView;
 import com.cs174.starrus.view.BuyStockView;
 import com.cs174.starrus.view.IView;
 
 public class BuyStockSubmitController implements IController{
 	private boolean DEBUG 		= true;
 	private Connection conn 	= null;
 	private Customer c 			= Customer.getCustomer();
 	private BuyStockView bsV	= BuyStockView.getView();
 	private CustomerView cV		= CustomerView.getView();
 	private SysDate		 sD		= SysDate.getSysDate();
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void process(String model) {
 		Statement stmt;
 		ResultSet rs;
 		String ticker;
 		int quantity	= 0;
 		float balance	= 0;
 		float price		= 0;
 		float sales		= 0;
 		int total_share = 0;
 
 		try {
 			conn 	= DBconnector.getConnection();
 			stmt	= conn.createStatement();
 
 			quantity = Integer.parseInt(bsV.getTxtQuantity().getText());
 			ticker   = bsV.getTxtTicker().getText().toUpperCase();
 /*	
 			if(c.getS_account_id()==0){
 				bsV.getLblWarning().setText("You don't have an account, please deposit 1000");
 				throw new SQLException();
 			}
 */
 		// Grabbing balance from customer table to check if the user has enough to buy money to buy the shares
 			if (DEBUG == true){
 					System.out.println("SELECT BALANCE FROM CUSTOMER WHERE USERNAME = '"	+
 											c.getUsername()			+ "'"						
 										);
 			}
 			
 			rs = stmt.executeQuery(		"SELECT BALANCE FROM CUSTOMER WHERE USERNAME = '"	+
 											c.getUsername()			+ "'"					
 									);
 			if( rs.next()){
 				balance = rs.getFloat("BALANCE");	
 			}
 
 
 		// Grabbing the price of the stock
 			if (DEBUG == true){
 					System.out.println("SELECT CUR_PRICE FROM STOCK WHERE SYMBOL = '"		+
 											ticker					+ "'"					
 										);
 			}
 
 			rs = stmt.executeQuery(		"SELECT CUR_PRICE FROM STOCK WHERE SYMBOL = '"		+
 											ticker					+ "'"					
 									);
 			if( rs.next()){
 				price = rs.getFloat("CUR_PRICE");
 			}
 
 
 		// Checking if client has enough money
 			sales =  ( price * quantity ) + 20;
 			if(DEBUG == true){
 				System.out.println("COST: " + sales);
 			}
 			if( balance < sales){ 
 				bsV.getLblWarning().setText("You do not have enough money to complete this transaction");
 			}
 			else{	
 			// Complete and record the transaction
 			// Update manage to reflecat the new total shares
 				bsV.getLblWarning().setText("");
 				if( DEBUG == true){
 					System.out.println("SELECT TOTAL_SHARE FROM MANAGE_STOCK WHERE musername = " +
 									"'" + c.getUsername() + "' AND symbol=" +
 									"'" + ticker + "' AND PRICE = " + price );
 
 				}
 				
 				rs = stmt.executeQuery("SELECT TOTAL_SHARE FROM MANAGE_STOCK WHERE musername = " +
 									"'" + c.getUsername() + "' AND symbol=" +
 									"'" + ticker + "' AND PRICE = " + price );
 				if(rs.next()){
 					System.out.println("debug: " + rs.getInt("TOTAL_SHARE") + quantity);
 					int shares = rs.getInt("TOTAL_SHARE");
 					total_share = shares + quantity;
 					if( DEBUG==true ){
 						System.out.println(	"UPDATE MANAGE_STOCK SET TOTAL_SHARE = "	+
 											total_share												+	
 											" WHERE MUSERNAME = '" 	+ c.getUsername() + "'"			+
 											"AND SYMBOL = '"		+ 
 											bsV.getTxtTicker().getText().toUpperCase()				+
 											"'"
 											);
 					}
 
 
 						stmt.executeQuery(	"UPDATE MANAGE_STOCK SET TOTAL_SHARE =  "	+
 											total_share											+
 											"WHERE MUSERNAME = '" 	+ c.getUsername() + "'"		+
 											"AND SYMBOL = '"		+ 
 											bsV.getTxtTicker().getText().toUpperCase()			+
 											"'"
 											);
 				}
 				else{
 					total_share = quantity;
 					if( DEBUG==true ){
 						System.out.println(	"INSERT INTO MANAGE_STOCK (MUSERNAME, SYMBOL, TOTAL_SHARE, PRICE)" 
 											+ "VALUES ('" 	+ c.getUsername() + "','"  
 											+ ticker 		+ "',"  
 											+ total_share 	+ ","
 											+ price			+")");
 					}
 
 
 						stmt.executeQuery(	"INSERT INTO MANAGE_STOCK (MUSERNAME, SYMBOL, TOTAL_SHARE, PRICE)" 
 											+ "VALUES ('" 	+ c.getUsername() + "','"  
 											+ ticker 		+ "',"  
 											+ total_share 	+ ","
 											+ price			+")");
 				}
 				
 				
 
 			// Update Customer's balance
 				if(DEBUG == true){
 					System.out.println(		"UPDATE CUSTOMER SET BALANCE = BALANCE - "			+
 											sales												+
 											"WHERE USERNAME = '"								+
 											c.getUsername()										+
 											"'"
 										);
 				}
 				stmt.executeQuery(			"UPDATE CUSTOMER SET BALANCE = BALANCE - "			+
 											sales												+
 											"WHERE USERNAME = '"								+
 											c.getUsername()										+
 											"'"
 										);
 
 			// Update balance in customer view
 				rs	= stmt.executeQuery( 	"SELECT * FROM CUSTOMER WHERE USERNAME = '"			+
 											c.getUsername()										+
 											"'"
 										);
 				if( rs.next() ){
 					balance = rs.getFloat("BALANCE");
 					System.out.println("Float.toString(balance): " + Float.toString(balance));
 					cV.setBalancefield(Float.toString(balance));
 					c.setBalance(balance);
 				}
 
 			// Record transaction into stock_trans
 			// STYPE:	0 for Buy
 			// 			1 for Sell
 			sales = -sales;
 			if( DEBUG == true){
 				System.out.println(		"INSERT INTO STOCK_TRANS (TDATE,SUSERNAME, SYMBOL,STYPE,SHARES,PRICE,PROFIT) "	+
 										"VALUES( '" + sD.getDateStr() + "','" 	+ c.getUsername()	+ "','"		+
 										ticker		+ "',"			+ 0			+ ","				+	
										quantity	+ ","			+ price		+ ","				+ 0 +
 										")"
 							);
 
 			}
 
 			stmt.executeQuery(			"INSERT INTO STOCK_TRANS (TDATE,SUSERNAME, SYMBOL,STYPE,SHARES,PRICE,PROFIT) "	+
 										"VALUES( '" + sD.getDateStr() 	+ "','" 	+ c.getUsername()	+ "','"		+
 										ticker		+ "',"			+ 0			+ ","				+	
										quantity	+ ","			+ price		+ ","				+ 0	+
 										")"
 							);
 
 			// Record the monetary transaction into money_trans
 			// TTYPE: 	1 for deposit
 			//			2 for withdrawal
 			if( DEBUG == true ){
 				System.out.println(		"INSERT INTO MONEY_TRANS (TDATE,TUSERNAME,TTYPE,AMOUNT,BALANCE) VALUES"+
 										"('" 		+ sD.getDateStr()	+ "','"		+ c.getUsername()	+ "',"		+
 										2			+ ","				+ sales		+ ","				+ balance			+ 
 										")"
 							);
 
 
 			}
 			if(DEBUG == true){
 				System.out.println("END");
 			}
 
 			stmt.executeQuery(			"INSERT INTO MONEY_TRANS (TDATE,TUSERNAME,TTYPE,AMOUNT,BALANCE) VALUES"+
 										"('" 		+ sD.getDateStr()	+ "','"		+ c.getUsername()	+ "',"		+
 										2			+ ","				+ sales		+ ","				+ balance			+ 
 										")"
 							);
 
 
 			
 			// UPDATE CUSTOMER STOCK TABLE
 			System.out.println("SELECT * FROM MANAGE_STOCK WHERE musername = '" + 
 					c.getUsername() +"'");
 			
 			rs = stmt.executeQuery("SELECT * FROM MANAGE_STOCK WHERE musername = '" + 
 									c.getUsername() +"'");
 			
 			cV.getRow_myStock().clear();
 			while( rs.next() ){
 				Vector<String> newRow = new Vector<String>();
 				String symbol= rs.getString("SYMBOL");
 				float price1 = rs.getFloat("PRICE");
 				int shares	= rs.getInt("TOTAL_SHARE");	
 				newRow.add(symbol);
 				newRow.add(Integer.toString(shares));
 				newRow.add(Float.toString(price1));
 				cV.getRow_myStock().add(newRow);
 			}
 			cV.updateView(c);
 			bsV.dispose();
 		}
 
 
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			// TODO Auto-generated catch block
 			System.out.println("Exception in BuyStockSubmitController");
 		} // Specify the SQL Query to run
 	}
 }
