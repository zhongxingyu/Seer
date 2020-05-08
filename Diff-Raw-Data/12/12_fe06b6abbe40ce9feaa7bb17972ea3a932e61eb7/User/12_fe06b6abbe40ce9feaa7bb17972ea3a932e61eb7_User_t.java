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
 				new InsertSpecialOrder(), new ViewACustomerSpecialOrders(), new InsertCustomerBill(), new ShowCustomerBillingCycleTotalsByCustomer(), new ShowCustomerBillsInBillingCustomer(), new InsertCustomerPayment(), new UpdateSpecialOrderItemStatus(), new LookUpCustomerByName(), new ShowAllCustomers()};
 
 
 		
 		billingCommands = new Command[]{ new CreateBillingCycle(), new UpdateCustomerBillingCycle(), 
 				new ShowCustomerBillingCycleTotalsByStatus(), new ShowCustomerBillingCycleTotalsByCustomer(), new ShowCustomerBillsInBillingCustomer(), new InsertCustomerPayment(), new ShowAllCustomerPayments()  };		
 				
 
 		stockingCommands = new Command[]{ new AddStoreItem(), new UpdateStoreMerchandise(), new CreateMerchandise(), new CreateVendor(),
 				new UpdateVendor(), new ReviewStoreInventory(), 
 				 new UpdateSpecialOrderItemStatus(), 
				new UpdateVendorBillItemsStatus(), new ViewAllSpecialOrdersByType(), new InsertVendorBill(), new ShowVendorBills(), new ShowVendors(), new SendSpecialOrderToCustomer(), new CheckMerchandiseAvailability()};
 	
 		managerCommands = new Command[]{ new CreateEmployee(), new UpdateEmployee(), new ShowEmployeesByStore(), new ShowAllCustomerBills(), new ShowAllVendorBills(), new ShowAllStaffBillsSpecialOrderVendorBills() };
		franchiseCommands = new Command[]{ new CreateStore(), new UpdateEmployee(),new ShowStore(), new CreateEmployee(), new ShowEmployees(), new ShowAllCustomerBillsByStore(), new ShowAllVendorBillsByStores(), new ShowAllStaffBillsSpecialOrderVendorBills()};
 
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
