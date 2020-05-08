 package com.db.ncsu.command;
 
 import com.db.database.DatabaseManager;
 
 public class ShowVendorBills extends Command {
 	public CommandArgument[] getArguments() {
 		CommandArgument args[] = new CommandArgument[1];
		args[0] = new CommandArgument("Status","String","Status (open or received)",true);
 
 		return args;
 	}
 
 	@Override
 	public void run(CommandArgument[] args) {
 		//SELECT vendorID, sum(price * quantity), storeID
 		//FROM VendorBill, VendorBillItems
 		//WHERE vendorBillID = id AND dateTime > �01-JAN-03� AND dateTime < �01-JAN-04�
 		//GROUP BY vendorID,storeID
 		String sql = "SELECT vb.id,vendorID, v.name, dateTime, merchandiseId,quantity,price,paymentInformation, confirmationCode " +
 				"FROM VendorBill vb, VendorBillItems vi, Vendor v " +
 				"WHERE vi.vendorBillID = vb.id AND vb.vendorID = v.id and status = ?";
 		DatabaseManager.runPreparedStatement(sql,args,true);
 		
 	}
 
 	@Override
 	public String getCommandName() {
 		return "Show all vendor bills by type";
 	}
 }
