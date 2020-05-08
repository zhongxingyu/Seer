 //Import all models
 package Controller;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import Model.*;
 import Model.CustomerPackage.*;
 import Model.ItemPackage.*;
 import Model.NewsletterPackage.*;
 import View.*;
 
 public class RentalSystem
 {
 	private CustomerHandler 	CustomerH;
 	private ItemHandler 		ItemH;
 	private NewsletterHandler 	NewsletterH;
 	private RentalHandler 		RentalH;
 	private	SearchHandler 		SearchH;
 	private Database 			database;
 	private GUISystem			SystemGui;
 	
 	//Construct
 	public RentalSystem()
 	{
 		//Handlers and Database
 		this.CustomerH 		= new CustomerHandler();
 		this.ItemH 			= new ItemHandler();
 		this.NewsletterH	= new NewsletterHandler();
 		this.SearchH 		= new SearchHandler(this.ItemH, this.CustomerH);
 		this.RentalH 		= new RentalHandler(this.ItemH, this.CustomerH);
 		this.database 		= new Database();
 		this.SystemGui		= new GUISystem(this);
 		//---------------------------------------
 		
 		//Load
 		this.loadResult();
 	}
 	
 	/**
 	 * Get CustomerHandler
 	 * @return		CustomerHandler
 	 */
 	public CustomerHandler getCustomerHandler()
 	{
 		return this.CustomerH;
 	}
 	
 	/**
 	 * Get ItemHandler
 	 * @return		ItemHandler
 	 */
 	public ItemHandler getItemHandler()
 	{
 		return this.ItemH;
 	}
 	
 	/**
 	 * Get NewsletterHandler
 	 * @return		NewsletterHandler
 	 */
 	public NewsletterHandler getNewsletterHandler()
 	{
 		return this.NewsletterH;
 	}
 	
 	/**
 	 * Get RentalHandler
 	 * @return		RentalHandler
 	 */
 	public RentalHandler getRentalHandler()
 	{
 		return this.RentalH;
 	}
 	
 	/**
 	 * Get SearchHandler
 	 * @return		SearchHandler
 	 */
 	public SearchHandler getSerchHandler()
 	{
 		return this.SearchH;
 	}
 	
 	/**
 	 * Save to database
 	 * @return	true or false pending success
 	 */
 	public boolean save() throws ClassNotFoundException
 	{
 		boolean save = false;
 		this.database.addItemList(this.ItemH.getListOfItems());
 		this.database.addCustomerList(this.CustomerH.getListOfCustomers());
 		this.database.setIdItem(this.ItemH.getId());
 		this.database.setIdCustomer(this.CustomerH.getId());
 		try
 		{
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("database.dat"));
 			
 			out.writeObject(this.database);
 				
 			out.close();
 			save = true;
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return save;
 	}
 	
 	/**
 	 * Load result
 	 */
 	public void loadResult()
 	{	
 		try
 		{
 			File checkFile = new File("database.dat");
 			
			
 			if(checkFile.exists())
 			{
 				ObjectInputStream in = new ObjectInputStream(new FileInputStream(checkFile));
 				
 				Object obj = in.readObject();
 				
 				this.database = (Database) obj;
 				this.CustomerH.LoadedFromDb(this.database.getCustomerList());			
 				this.ItemH.LoadedFromDb(this.database.getItemList());
 				this.ItemH.setId(this.database.getIdItem());
 				this.CustomerH.setId(this.database.getIdCustomer());
 				in.close();
 			}
 			
 		}
 		catch (ClassNotFoundException | IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args)
 	{
 		GUILogin Login = new GUILogin();
 	}
 }
