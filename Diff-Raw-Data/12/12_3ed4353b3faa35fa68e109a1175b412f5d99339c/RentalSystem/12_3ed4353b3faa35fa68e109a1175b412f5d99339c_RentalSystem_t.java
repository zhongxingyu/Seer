 //Import all models
 package Controller;
 import Model.*;
 import Model.CustomerPackage.*;
 import Model.ItemPackage.*;
 import Model.NewsletterPackage.*;
 
 public class RentalSystem
 {
	private CustomerHandler 	CustomerH;
	private ItemHandler 		ItemH;
	private NewsletterHandler 	NewsletterH;
	private RentalHandler 		RentalH;
	private SearchHandler 		SearchH;
	private Database 			databse;
 	
 	//Construct
 	public RentalSystem(){
 		//Handlers and Database
 		this.CustomerH 		= new CustomerHandler();
 		this.ItemH 			= new ItemHandler();
 		this.NewsletterH	= new NewsletterHandler();
 		this.RentalH 		= new RentalHandler();
 		this.SearchH 		= new SearchHandler();
 		this.databse 		= new Database();
 		//---------------------------------------
 	}
 	
 	public static void main(String[] args)
 	{
 		RentalSystem test = new RentalSystem();
 
 	}
 
 }
