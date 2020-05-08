 package com.db.ncsu.command;
 
 import com.db.database.DatabaseManager;
 
 public class ShowCustomerBillsInBillingCustomer extends Command {
 
 	@Override
 	public CommandArgument[] getArguments() {
 		CommandArgument args[] = new CommandArgument[1];
		args[0] = new CommandArgument("Billing Cycle Id","Int","Billing Cycle ID",true);
 		return args;
 	}
 
 	@Override
 	public void run(CommandArgument[] args) {
 		//Select * from Staff
 		String sql = "Select datetime,merchandiseId,quantity,price "+ 
 				      "from CustomerBill cb, "+
 		"CustomerBillItems cbi, "+
 		"CustomerBillingCycle cbc  "+
 		"where cb.id = cbi.customerbillid  "+
 		"and cbc.id = cb.customerbillcycleid "+
 		"and cbc.id = ?";
 		
 		
 		DatabaseManager.runPreparedStatement(sql,args,true);
 			
 	}
 
 	@Override
 	public String getCommandName() {
 		return "Show Billing Cycle Activity";
 	}
 	
 }
