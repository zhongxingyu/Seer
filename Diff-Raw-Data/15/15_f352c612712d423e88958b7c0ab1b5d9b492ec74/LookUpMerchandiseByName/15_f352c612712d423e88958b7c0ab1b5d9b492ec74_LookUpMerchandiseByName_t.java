 package com.db.ncsu.command;
 
 import com.db.database.DatabaseManager;
 
 public class LookUpMerchandiseByName extends Command {
 
 	@Override
 	public CommandArgument[] getArguments() {
 		CommandArgument args[] = new CommandArgument[1];
		args[0] = new CommandArgument("Name","String","Name",true);
 		return args;
 	}
 
 	@Override
 	public void run(CommandArgument[] args) {
 		//Select * from Store
 		String sql = "Select * from Merchandise m, Vendor v where m.vendorId = v.id and m.name like ?";
 		DatabaseManager.runPreparedStatement(sql,args,true);
 			
 	}
 
 	@Override
 	public String getCommandName() {
 		return "Look up Merchandise by Name";
 	}
 	
 }
