 package com.db.ncsu.user;
 
 import com.db.ncsu.command.Command;
 import com.db.ncsu.command.CreateAccount;
 import com.db.ncsu.command.CreateEmployee;
 import com.db.ncsu.command.*;
 
 public  class User {
 
 	private Command[] salesCommands;
 	private Command[] billingCommands;
 	private Command[] managerCommands;
 
 	
 	public User()
 	{
		salesCommands = new Command[]{ new InsertSpecialOrder(), new UpdateMerchandise(), new UpdateAccount(), new CreateAccount(), new CreateEmployee(), new ShowEmployees(), new CreateVendor(), new CreateStore(), new CreateMerchandise(), new AddVendorPayment(), new AddStoreItem(), new ReviewStoreInventory(), new CheckItemAtStore(), new CheckMerchandiseAvailability(), new ShowStore(), new ReviewBills(), new ViewAllCustomerSpecialOrders(), new GenerateCustomerPurchaseHistory(), new GenerateVendorPurchaseHistory(), new ReviewASpecialOrder(), new VendorStorePurchaseHistory(), new CustomerStorePurchaseHistory(), new ShowTotalInventory(), new ShowAllCustomerBills(), new ShowAllVendorBills(), new UpdateCustomerBillingCycle(), new UpdateSpecialOrderItemStatus(), new UpdateVendor(), new UpdateEmployee()};
 		billingCommands = new Command[]{ new CreateAccount()};
 		managerCommands = new Command[]{ new CreateEmployee()};
 
 	}
 	
 	public Command[] getSalesCommands() {
 		return salesCommands;
 	}
 	
 	public Command[] billingCommands() {
 		return billingCommands;
 	}
 	
 	public Command[] managerCommands() {
 		return managerCommands;
 	}
 	
 	public Command[] franchiseManagerCommands() {
 		return salesCommands;
 	}
 	
 	public Command[] stockingCommands() {
 		return salesCommands;
 	}
 	
 }
