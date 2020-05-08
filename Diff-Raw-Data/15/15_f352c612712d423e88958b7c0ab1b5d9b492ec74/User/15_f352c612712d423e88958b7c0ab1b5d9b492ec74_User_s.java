 package com.db.ncsu.user;
 
 import com.db.ncsu.command.*;
 
 public  class User {
 
 	private Command[] salesCommands;
 	private Command[] billingCommands;
 	private Command[] stockingCommands;
 	private Command[] managerCommands;
 	private Command[] franchiseCommands;
 
 	
 	public User()
 	{
 		
 		salesCommands = new Command[]{new UpdateAccount(), new CreateAccount(), new LookUpMerchandiseByName(), new CheckMerchandiseAvailability(),			
<<<<<<< HEAD
				new InsertSpecialOrder(),  new CreateBillingCycle(), new FindBillingCycle()};
 		
 
 		
=======
				new InsertSpecialOrder(),  new CreateBillingCycle(), new InsertCustomerBill(), new ViewAllCustomerSpecialOrders()};
				
>>>>>>> 54de4b95fbc8d0f23ee4b9069189e6cf44be214f
 		//TODO Insert CustomerPayment, ShowCustomerPayments		
 		billingCommands = new Command[]{ new CreateBillingCycle(), new UpdateCustomerBillingCycle(), 
 				new ShowCustomerBillingCycleTotalsByStatus(), new ShowCustomerBillingCycleTotalsByCustomer(), new ShowCustomerBillsInBillingCustomer()  };		
 				
 		//Add Insert VendorBills, so they can order	
 		//Update VendorBill Status 
 		//ViewAllCustomerSpecialOrdersByType ... type on open, order
 		stockingCommands = new Command[]{ new AddStoreItem(), new UpdateStoreMerchandise(), new CreateMerchandise(), new CreateVendor(),
 				new UpdateVendor(), new ReviewStoreInventory(), 
 				new ShowAllVendorBills(), new UpdateSpecialOrderItemStatus(), 
 				new UpdateVendorBillItemsStatus(), new ViewAllCustomerSpecialOrdersByType()};
 	
 
 		
 		managerCommands = new Command[]{ new CreateEmployee(), new UpdateEmployee(), new ShowEmployeesByStore(), new ShowAllCustomerBills(), new ShowAllVendorBills() };
 		franchiseCommands = new Command[]{ new CreateStore(),new ShowStore(), new CreateEmployee(), new ShowEmployees(), new ShowAllCustomerBillsByStore(), new ShowAllVendorBillsByStores()};
 
 	}
 	
 	public Command[] getSalesCommands() {
 		return salesCommands;
 	}
 
 	public Command[] getFranchiseCommands() {
 		return franchiseCommands;
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
 		return stockingCommands;
 	}
 	
 }
