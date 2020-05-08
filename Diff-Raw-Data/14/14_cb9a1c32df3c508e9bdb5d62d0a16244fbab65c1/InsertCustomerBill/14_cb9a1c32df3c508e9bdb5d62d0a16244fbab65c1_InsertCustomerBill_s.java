 package com.db.ncsu.command;
 
 import java.sql.PreparedStatement;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import com.db.database.DatabaseManager;
 import com.db.ncsu.Main;
 
 public class InsertCustomerBill extends Command {
 
 	@Override
 	public CommandArgument[] getArguments() {
 		CommandArgument args[] = new CommandArgument[4];
 		args[0] = new CommandArgument("Today","Date","Today",false);		
 		args[1] = new CommandArgument("StoreID","Int","StoreID",false);
 		args[2] = new CommandArgument("StaffID","Int","StaffID",false);
 		args[3] = new CommandArgument("CustomerBillingCycleID","Int","Customer Billing Cycle ID",true);
 		return args;
 	}
 
 	@Override
 	public void run(CommandArgument[] args) {
 
 		//Get Next Sequence
 		String seqSQL="select customerbill_seq.nextval from CustomerAccount";		
 		int seqNum = DatabaseManager.getSeqVal(seqSQL);
 
 		
 		//Make a list of PreparedStatements to Run
 		
 		ArrayList<PreparedStatement> preparedStatements = new ArrayList<PreparedStatement>();
 		
 		//Insert Top Level Special Order
 		String CustomerBillSQL="Insert into CustomerBill(id, dateTime, storeID, staffID, customerBillCycleID) VALUES ("+seqNum+",?,?,?,?)";
 		preparedStatements.add(DatabaseManager.makePreparedStatement(CustomerBillSQL,args));
 		
 		
 		
 		//Insert Each Order
 		String CustomerBillItemSQL="Insert into CustomerBillItems(customerBillID, merchandiseID, quantity, price) VALUES ("+seqNum+",?,?,?)";		
 
 		CommandArgument specialargs[] = new CommandArgument[3];
 		specialargs[0] = new CommandArgument("merchandiseID","Int","Merchandise ID", true);		
 		specialargs[1] = new CommandArgument("quantity","Int","Quantity", true);
 		specialargs[2] = new CommandArgument("price", "Float", "Price", true);
 	
 		String moreItems = "Y";
 		Scanner scanner = new Scanner(System.in);
 		int i=1;
 		while (moreItems.equals("Y"))
 		{
 			System.out.println("Entering Details of Item "+i);
 			for (CommandArgument arg : specialargs)
 			{
 				System.out.println(arg.getDescription());
 				String argString = scanner.nextLine();
 				arg.setValue(argString);
 			}
			preparedStatements.add(DatabaseManager.makePreparedStatement(CustomerBillSQL,specialargs));			
 			
 			String updateSQL = "update StoreItem set Quantity=Quantity - "+ specialargs[1].getValue()+" where storeID = "+Main.userStoreId+" AND merchandiseID ="+specialargs[0].getValue();
			System.out.println(updateSQL);
 			CommandArgument noargs[] = new CommandArgument[0];
 			preparedStatements.add(DatabaseManager.makePreparedStatement(updateSQL,noargs));			
 						
 			
 			System.out.println("Enter more Items? Y for Yes");
 			moreItems = scanner.nextLine();
 			i++;
 		}
 		scanner.close();
 
 		//Run Transaction
 		DatabaseManager.runTransaction(preparedStatements);
 		System.out.println("Customer Bill Added!!!");	
 	}
 
 	@Override
 	public String getCommandName() {
 		return "Customer Buys Items";
 	}
 	
 	
 	
 
 	
 }
 
