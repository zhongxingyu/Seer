 //In this package
 package Model;
 import Model.CustomerPackage.*;
 import Model.ItemPackage.*;
 
 import java.util.Vector;
 
 
 //Import what this class use
 
 public class SearchHandler
 {
 	private Vector <Customer> customers;
 	private Vector <Item> items;
 	
 	public SearchHandler(ItemHandler ItemH, CustomerHandler CustomerH)
	{
 	    //Vectors
 	    this.customers = new Vector<Customer>();
 	    this.items = new Vector<Item>();
 	    
 	    this.customers = CustomerH.getListOfCustomers();
 	    this.items = ItemH.getListOfItems();
 	   
 	}
	public String[] SearchCustomer(String searchString)
 	{
 		String returnString = null;
 		int size = this.customers.size();
 		int counter = 0;
 		String Result[] = new String[size];
 		for (Customer cust : customers) 
 		{
 		   if(cust.getName().equals(searchString))
 		   {
 		       returnString = cust.toString();
 		       Result[counter] = returnString;
 		       counter++;
 		   }
 		   else if(cust.getId() == (Integer.parseInt(searchString)))
 		   {
 				returnString = cust.toString();
 				Result[counter] = returnString;
 				counter++;
 		   }
 		   else if(cust.getSsn().equals(searchString))
 		   {
 				returnString = cust.toString();
 				Result[counter] = returnString;
 				counter++;
 		   }
 		   else if(cust.getAddress().equals(searchString))
 		   {
 				returnString = cust.toString();
 				Result[counter] = returnString;
 				counter++;
 		   }
 		}
 		int newSize = counter;
 		String returnArr[] = new String[newSize];
 		for(int i = 0; i<newSize; i++){
 			returnArr[i] = Result[i];
 		}
 		return returnArr;
 	}
 	
 	public String[] SearchItem(String searchString)
 	{
 		String returnString = null;
 		int size = this.items.size();
 		int counter = 0;
 		String Result[] = new String[size];
 		
 		for(Item itm : items)
 		{
 			if(itm.getTitle().equals(searchString))
 			{
 			    returnString = itm.toString();
 			    Result[counter] = returnString;
 			    counter++;
 			}
 			else if(itm.getId() == (Integer.parseInt(searchString)))
 			{
 			    returnString = itm.toString();
 			    Result[counter] = returnString;
 			    counter++;
 			}
 		}
 		int newSize = counter;
 		String returnArr[] = new String[newSize];
 		for(int i = 0; i<newSize; i++){
 			returnArr[i] = Result[i];
 		}
 		return returnArr;
 	}
 	
 }
