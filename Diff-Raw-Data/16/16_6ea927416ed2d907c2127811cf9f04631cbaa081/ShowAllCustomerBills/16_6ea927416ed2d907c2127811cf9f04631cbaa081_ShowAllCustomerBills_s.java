 package com.db.ncsu.command;
 
 import com.db.database.DatabaseManager;
 
 public class ShowAllCustomerBills extends Command {
 	public CommandArgument[] getArguments() {
		CommandArgument args[] = new CommandArgument[1];
 		args[0] = new CommandArgument("StoreID","Int","StoreID",false);

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
				"WHERE cb.id = b.customerBillcycleid  AND b.id = bi.customerbillid and StoreId = ? " +
 				"GROUP BY status, customerID";
 		System.out.println(sql);
 		DatabaseManager.runPreparedStatement(sql,args,true);
 		
 	}
 
 	@Override
 	public String getCommandName() {
 		return "Show All Customer Bills Grouped By Customer";
 	}
 }
