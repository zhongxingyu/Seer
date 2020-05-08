 package com.cs174.starrus.controller;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.lang.Integer;
 
 import com.cs174.starrus.model.Customer;
 import com.cs174.starrus.model.SysDate;
 import com.cs174.starrus.view.CustomerView;
 import com.cs174.starrus.view.SellStockView;
 import com.cs174.starrus.view.IView;
 
 import java.util.Vector;
 
 public class SellStockSubmitController implements IController{
 	private boolean DEBUG 		= true;
 	private Connection conn 	= null;
 	private Customer c 			= Customer.getCustomer();
 	private SellStockView ssV	= SellStockView.getView();
 	private CustomerView  cV	= CustomerView.getView();
 	private SysDate		  sD	= SysDate.getSysDate();
 
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void process(String model) {
 		int numStocksAvailable 	= 0;
 		float price				= 0;
 		float balance			= 0;
 		float profit			= 0;
 		float sales				= 0;
 		int	quantity			= 0;
 		Statement stmt;
 
 		if( DEBUG == true){
 			System.out.println("In SellStockSubmitController.process()");
 		}
 
 		// TODO Auto-generated method stub
 		try {
 			conn	= DBconnector.getConnection();
 			stmt	= conn.createStatement();
 
 			String 	ticker	= ssV.getTxtTicker().getText().toUpperCase();
 			float	buyPrice= Float.parseFloat(ssV.getTxtBuyPriceField().getText());
 
 
 			// Query is case sensitive
 			if ( DEBUG == true ){
 	            System.out.println( "SELECT * FROM MANAGE_STOCK WHERE MUSERNAME = "	
 			                                    + "'" + c.getUsername() + "'"					   
 												+ " AND SYMBOL = "							
 												+ "'"	+ ticker	+	"'"	
 												+ " AND PRICE = "	+ buyPrice
 											);    
 			}
 			
 			// Check how many shares this person has of the stock
 			ResultSet rs = stmt.executeQuery(	"SELECT * FROM MANAGE_STOCK WHERE MUSERNAME = "	
 			                                    + "'" + c.getUsername() + "'"					   
 												+ " AND SYMBOL = "							
 												+ "'"	+ ticker	+	"'"	
 												+ " AND PRICE = "	+ buyPrice
 											);    
 			
 
 			if( rs.next()){
 				numStocksAvailable	= rs.getInt("TOTAL_SHARE");
 				if( DEBUG == true){
 					System.out.println("NumStocks Available:\t" + numStocksAvailable);
 				}
 			}
 			
 			// Checking if there is any stock at the stated price	
 
 			// While the client is trying to sell more shares than he posses, 
 			// The system should loop waiting until he puts in a valid number
 			quantity	= Integer.parseInt(ssV.getTxtQuantityField().getText());

 				
			if( numStocksAvailable < quantity || quantity < 0){
				ssV.getLblWarning().setText("You do not have enough shares");
 			}
 			else{
 				ssV.getLblWarning().setText("");
 				// Update manage to reflect the change in stock
 					stmt.executeQuery( 	"UPDATE MANAGE_STOCK SET TOTAL_SHARE = TOTAL_SHARE - '"	+
 										quantity	+ "' "								+
 										"WHERE MUSERNAME = '"	+ c.getUsername()	+ "'"	+
 										"AND SYMBOL = '"		+ ticker			+ "'"	
 										);
 
 				// Pull price from stock table
 					rs	= stmt.executeQuery( "SELECT CUR_PRICE FROM STOCK WHERE SYMBOl ='"	+
 												ticker	+ "'"
 											);
 					if(rs.next()){
 						price = rs.getFloat("CUR_PRICE");
 					}
 
 				if(DEBUG == true){
 					System.out.println("CurrentPrice: " 	+ price);
 					System.out.println("quantity: " 	+ quantity);
 				} 
 								
 
				profit 	= ((price - buyPrice)* quantity ) -20;
 				sales	= (price * quantity ) - 20;
 
 				if( DEBUG == true){
 					System.out.println("BuyPrice: " + buyPrice);
 					System.out.println("Profit: " 	+ profit);
 					System.out.println("Sales: " 	+ sales);
 				}
 				// Update users balance to reflect the profit
 					if(DEBUG == true){
 						System.out.println("UPDATE CUSTOMER SET BALANCE = BALANCE + "	+
 											sales										+
 											"WHERE USERNAME = '"						+
 											c.getUsername()								+
 											"'"
 											);
 					}
 					stmt.executeQuery( "UPDATE CUSTOMER SET BALANCE = BALANCE + "	+
 										sales										+	
 										"WHERE USERNAME = '"						+
 										c.getUsername()								+
 										"'"
 									);
 
 				// Update balance in customer view
 				if( DEBUG == true){
 					System.out.println("SELECT * FROM customer where " +
 												"username = '" + c.getUsername() +"'");
 
 				}
 					rs = stmt.executeQuery (	"SELECT * FROM customer where " +
 												"username = '" + c.getUsername() +"'");
 					if( rs.next()){
 							balance = rs.getFloat("BALANCE");
 							System.out.println("Float.toString(balance): " + Float.toString(balance));
 							cV.setBalancefield(Float.toString(balance));
 							c.setBalance(balance);
 					}
 
 				// Update stock_trans table to capture change in stock
 				// STYPE: 	0 for buy
 				//			1 for sell
 					if(DEBUG == true){
 					System.out.println("INSERT INTO STOCK_TRANS (TDATE,SUSERNAME,SYMBOL,STYPE,SHARES,PRICE,PROFIT) "	+
 										"VALUES( '"	+ sD.getDateStr()	+ "','"		+ c.getUsername()	+ "','"		+ 
 										ticker		+ "'," 				+  1		+ ","				+ 
 										quantity	+ ","				+ price		+ ","				+ 
 										profit		+ ")" 
 										);
 
 					}
 					stmt.executeQuery(	"INSERT INTO STOCK_TRANS (TDATE,SUSERNAME,SYMBOL,STYPE,SHARES,PRICE,PROFIT) "	+
 										"VALUES( '"	+ sD.getDateStr()	+ "','"		+ c.getUsername()	+ "','"		+ 
 										ticker		+ "'," 				+  1		+ ","				+ 
 										quantity	+ ","				+ price		+ ","				+
 										profit		+ ")" 
 									);
 
 				// Record transaction into money_trans
 				if( DEBUG == true){
 					System.out.println(		"INSERT INTO MONEY_TRANS ( TDATE, TUSERNAME,TTYPE,AMOUNT,BALANCE) VALUES" +
 											"('"		+ sD.getDateStr()	+ "','"	+ c.getUsername()	+ "',"	+
											1			+ ","				+ sales	+ ","				+ balance			+ 
 											")"
 									);
 
 				}
 				stmt.executeQuery(		"INSERT INTO MONEY_TRANS ( TDATE, TUSERNAME,TTYPE,AMOUNT,BALANCE) VALUES" +
 										"('"		+ sD.getDateStr()	+ "','"	+ c.getUsername()	+ "',"	+
										1			+ ","				+ sales	+ ","				+ balance	+	
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
 					int shares	= rs.getInt("TOTAL_SHARE");	
 					float price1 = rs.getFloat("PRICE");
 					newRow.add(symbol);
 					newRow.add(Integer.toString(shares));
 					newRow.add(Float.toString(price1));
 					cV.getRow_myStock().add(newRow);
 				}
 				cV.updateView(c);
 				ssV.dispose();
 			}			
 		}	
 		catch (SQLException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Exception in SellStockSubmitController");
 		} // Specify the SQL Query to run
 		
 	}
 
 }
