 package com.cs174.starrus.controller;
 import com.cs174.starrus.view.IView;
 import com.cs174.starrus.view.ManagerView;
 import com.cs174.starrus.view.MonthlyStatementView;
 import com.cs174.starrus.model.Customer;
 
 import java.util.Vector;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class MonthlyStatementController implements IController{
 	private MonthlyStatementView msV = MonthlyStatementView.getView();
 	private ManagerView mV = ManagerView.getView();
 	private Connection conn = null;
 	boolean DEBUG = true;
 	private ResultSet	rs			= null;
 	private ResultSet   rs1			= null;
 	
 	
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void process(String model) {
 		// gets the username from input field
 		String username = mV.getTxtInputUsername().getText();
 		String email;
 		float finalBalance = 0;
 		float initialBalance = 0;
 		
 		try{
 			conn			= DBconnector.getConnection();
 			Statement stmt	= conn.createStatement();			
 			// ------------------------customer name and final balance---------------------
 			if(DEBUG == true){
 				System.out.println("SELECT * FROM CUTOMER WHERE USERNAME = '"	+
 										username			+ "'"
 									);
 
 			}
 			rs 		= stmt.executeQuery("SELECT * FROM CUSTOMER WHERE USERNAME = '"	+
 										username			+ "'"
 										);
 			if( rs.next() ){
 				if(DEBUG == true){
 					System.out.println("Getting Row");
 				}
 				Vector<String> newRow = new Vector<String>();
 				email = rs.getString("EMAIL");			// getting email
 				finalBalance = rs.getFloat("BALANCE");	// getting final balance
 				msV.getLblUsernameField().setText(username);
 				msV.getLblFinalbalancefield().setText(Float.toString(finalBalance));
 				msV.getLblEmailfield().setText(email);	
 			}
 			
 			
 			// -----------------------get initial balance-------------------------------
 			rs = stmt.executeQuery("SELECT BALANCE FROM MONEY_TRANS WHERE " + 
 					" M_TRANS_ID = ( SELECT MIN(M_TRANS_ID) FROM MONEY_TRANS)");
 			if(rs.next()){
 				initialBalance = rs.getFloat("BALANCE");
 			}
 			msV.getLblInitialbalancefield().setText(Float.toString(initialBalance));
 			
 			
 			//----------------------- calculate gain/loss from stock------------------------
 			if(DEBUG == true){
 				System.out.println("SELECT * FROM STOCK_TRANS WHERE SUSERNAME = '"	+
 										username			+ "'"
 									);
 
 			}
 			rs 		= stmt.executeQuery("SELECT * FROM STOCK_TRANS WHERE SUSERNAME = '"	+
 										username			+ "'"
 										);
 			float total_profit = 0;
 			while(rs.next()){
 				float profit = rs.getFloat("PROFIT");
 				total_profit += profit;
 			}
 			
 			//--------------------- add interest ----------------------------------------
 			if(DEBUG == true){
 				System.out.println("SELECT * FROM MONEY_TRANS WHERE TUSERNAME = '"	+
 										username			+ "'"
 									);
 
 			}
 			rs 		= stmt.executeQuery("SELECT * FROM MONEY_TRANS WHERE TUSERNAME = '"	+
 										username			+ "' AND TTYPE = 3"
 										);
 			
 			while(rs.next()){
 				float interest = rs.getFloat("AMOUNT");
 				total_profit += interest;
 			}
 			msV.getLblGainlossfield().setText(Float.toString(total_profit));
 			
 			
 			// --------------------------money trans table --------------------------
 			if(DEBUG == true){
 				System.out.println("SELECT * FROM MONEY_TRANS WHERE TUSERNAME = '"	+
 										username			+ "'"
 									);
 			}
 			rs 		= stmt.executeQuery("SELECT * FROM MONEY_TRANS WHERE TUSERNAME = '"	+
 										username			+ "'"
 										);
 			msV.getRow_mAccount().clear();
 			while( rs.next() ){
 				if(DEBUG == true){
 					System.out.println("Getting Row");
 				}
 				
 				int sid	= rs.getInt("M_TRANS_ID");
 				String date = rs.getString("TDATE");
 				int type = rs.getInt("TTYPE");
 				float amount= rs.getFloat("AMOUNT");
 				float balance = rs.getFloat("BALANCE");
 				
 				Vector<String> newRow = new Vector<String>();
 				newRow.add(Integer.toString(sid));
 				newRow.add(date);
 				if(type == 1)
 					newRow.add("Deposit");
 				else if(type == 2)
 					newRow.add("Withdraw");
 				else if(type == 3)
 					newRow.add("Interest");
 				newRow.add(Float.toString(amount));
 				newRow.add(Float.toString(balance));
 				msV.getRow_mAccount().add(newRow);
 			}
 		
 			//----------------------------stock trans table-----------------------------
 			if(DEBUG == true){
 				System.out.println("SELECT * FROM STOCK_TRANS WHERE TUSERNAME = '"	+
 										username			+ "'"
 									);
 
 			}
 			rs 		= stmt.executeQuery("SELECT * FROM STOCK_TRANS WHERE SUSERNAME = '"	+
 										username			+ "'"
 										);
 			
 			msV.getRow_sAccount().clear();
 			while( rs.next() ){
 				if(DEBUG == true){
 					System.out.println("Getting Row");
 				}
 				
 				int sid	= rs.getInt("S_TRANS_ID");
 				String date = rs.getString("TDATE");
 				String symbol = rs.getString("SYMBOL");
 				int type = rs.getInt("STYPE");
 				float shares = rs.getFloat("SHARES");
 				float price = rs.getFloat("PRICE");
 				
 				Vector<String> newRow = new Vector<String>();
 				newRow.add(Integer.toString(sid));
 				newRow.add(date);
 				newRow.add(symbol);
 				if(type == 1)
 					newRow.add("Buy");
 				else if(type == 0)
 					newRow.add("Sell");
 				newRow.add(Float.toString(shares));
 				newRow.add(Float.toString(price));
 				msV.getRow_sAccount().add(newRow);
 			}
			
			//-----------------------calculate commission----------------------
			if(DEBUG == true){
				System.out.println("SELECT COUNT(*) FROM STOCK_TRANS WHERE TUSERNAME = '"	+
										username			+ "'"
									);

			}
			rs 		= stmt.executeQuery("SELECT COUNT(*) FROM STOCK_TRANS WHERE SUSERNAME = '"	+
										username			+ "'"
										);
			int numOfTrans = 0;
			if(rs.next())
				numOfTrans = rs.getInt("COUNT(*)");
			float commission = numOfTrans * 20;
			System.out.println("commision test:  " + commission);
			msV.getLblCommission().setText(Float.toString(commission));
 			msV.updateView();
 
 		}catch (SQLException e){
 			e.printStackTrace();
 			System.out.println("Exception in MonthlyStatementController");
 		}
 
 		msV.setVisible(true);
 	}
 		
 }
 
