 package com.db.ncsu.command;
 
 import com.db.database.DatabaseManager;
 
 public class ShowAllCustomerBills extends Command {
 	public CommandArgument[] getArguments() {
		CommandArgument args[] = new CommandArgument[3];
 		args[0] = new CommandArgument("StoreID","Int","StoreID",false);
		args[1] = new CommandArgument("Start Date", "Date", "Start Date", true);
		args[2] = new CommandArgument("End Date", "Date", "End Date", true);
 		return args;
 	}
 
 	
 	
 	@Override
 	public void run(CommandArgument[] args) {
 		//SELECT storeID, sum(price * quantity) AS totalBill
 		//FROM CustomerBillingCycle cb, CustomerBill b, CustomerBillItems bi
 		//WHERE b.id = bi.customerBillID AND cb.id = b.id AND cb.customerID = 1 AND dateTime > 01-JAN-03 AND dateTime < 01-JAN-04 AND status = billed
 		//GROUP BY storeID
 		String sql = "SELECT customerID, status, sum(price * quantity) AS TotalBalance " +
 				"FROM CustomerBillingCycle cb, CustomerBill b, CustomerBillItems bi " +
				"WHERE cb.id = b.customerBillcycleid  AND b.id = bi.customerbillid and StoreId = ?  AND dateTime > ? AND dateTime < ?" +
 				"GROUP BY status, customerID";
 		System.out.println(sql);
 		DatabaseManager.runPreparedStatement(sql,args,true);
 		
 	}
 
 	@Override
 	public String getCommandName() {
 		return "Show All Customer Bills Grouped By Customer";
 	}
 }
