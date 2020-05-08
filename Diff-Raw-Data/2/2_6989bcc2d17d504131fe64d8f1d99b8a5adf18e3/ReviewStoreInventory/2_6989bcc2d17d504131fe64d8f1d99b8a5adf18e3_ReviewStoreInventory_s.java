 package com.db.ncsu.command;
 
 import com.db.database.DatabaseManager;
 
 public class ReviewStoreInventory extends Command {
 	public CommandArgument[] getArguments() {
 		CommandArgument args[] = new CommandArgument[1];
 		args[0] = new CommandArgument("Store ID","Int","Store ID",true);
 		return args;
 	}
 
 	@Override
 	public void run(CommandArgument[] args) {
 		//SELECT storeID, merchandiseID, quantity, price
 		//FROM Store s, StoreItem si
 		//WHERE s.id = si.storeid AND storeId=1
		String sql = "SELECT storeID, merchandiseID, name, quantity, price " +
 				"FROM Store s, StoreItem si, Merchandise m " +
 				"WHERE s.id = si.storeid AND si.merchandiseID = m.id AND storeId = ?";
 		DatabaseManager.runPreparedStatement(sql,args,true);
 		
 	}
 
 	@Override
 	public String getCommandName() {
 		return "Review Store Inventory";
 	}
 }
