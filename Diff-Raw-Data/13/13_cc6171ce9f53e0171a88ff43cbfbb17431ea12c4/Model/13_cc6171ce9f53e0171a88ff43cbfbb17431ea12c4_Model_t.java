 /*
  * Note this is only the alpha version of the model and can only connect to a database so far
  * While the other methods are only the bare skeleton of the class and do not contain any functioning code 
  * so far
  */
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.lang.ClassNotFoundException;
 
 
 public class Model 
 {
 
 	// This is the global user name and password.
  private String username,password;
  
 //These are the connection and sql statements classes and instances.
 	
 		Connection DBconnection = null;
 		Statement DBstatement = null;
 		ResultSet st_details = null;
 	
 	/*constructor: this will run when it is model created and will assign 
 	the username and password to the private variables upon creation of the class*/
 	
 	public Model(String uname, String pass)
 	{
 		username  = uname;
 		password  = pass;
 	}
 
 	private void connectDb()
 	{
 	  
 	  try
 		{
 			System.out.println("Connecting to Driver.....");
 			Class.forName("com.mysql.jdbc.Driver"); // this is connecting the java project to the driver Note very important
 			System.out.println("Connection Successful");
 			
 			System.out.println("Connecting to Database: DvdManagement");
 			DBconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dvdmanagement" , username,password);
 			System.out.println("Connection Successful");
 			
 		/*	System.out.println("Create dvd table");
 			DBstatement = DBconnection.createStatement();
 			DBstatement.execute("CREATE TABLE teseter(id INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, title VARCHAR(25)) ");
 			System.out.println("completed");
 		*/	
 			
 			
 		}
 		catch(ClassNotFoundException error)
 		{
 			System.out.println("Error : " +  error.getMessage());
 		}
 		
 	  catch(SQLException error)
 		{
 			System.out.println("Error : " +  error.getMessage());
 		}
 	/*	finally // 
 		{
 			
 		}*/	
 	}
 	
 	public void closeDB()
 	{
 		if(DBconnection != null)												//and this closes the DBconnection after it has been used
 		{
 			System.out.println("Closing connection.....");
 	
 			try{	DBconnection.close();}
 				catch(SQLException ignore)
 				{
 					System.out.println("Error : " +  ignore.getMessage());
 				}
 	
 			System.out.println("Connection closed");
 		}
 	}
 	
 	public void closeStm()
 	{
 		//this handles the closing of the connections to the database when finished
 		if(DBstatement != null)     											//this block closes the DBstatement connection if it has been used
 		{
 			System.out.println("Closing statement.....");
 			
 			try{	DBstatement.close();  }
 				catch(SQLException ignore)
 				{
 					System.out.println("Error : " +  ignore.getMessage());
 				}
 			System.out.println("Statement closed");
 		}
 	}
 	
 	public void closeResultSet()
 	{
 		if(st_details != null)     											//this block closes the DBstatement connection if it has been used
 		{
 			System.out.println("Closing Result set.....");
 			
 			try{	st_details.close();  }
 				catch(SQLException ignore)
 				{
 					System.out.println("Error : " +  ignore.getMessage());
 				}
 			System.out.println("Result Set closed");
 		}
 	}
 	
 	//Create a new employee 
 		public void NewEmp(String Un,String Pw)
 		{
 			connectDb(); //connect to the database
 			try{
 			System.out.println("Create new user");
 					DBstatement = DBconnection.createStatement();
					//DBstatement.execute("CREATE USER " + Un + " IDENTIFIED BY  '" + Pw + "'" ); //Creates the user
 					System.out.println("User " + Un + " has been created");
 					DBstatement.execute("GRANT SELECT, INSERT,UPDATE,DELETE,CREATE,DROP,ALTER ON codetest.* TO " + Un + " IDENTIFIED BY '" + Pw + "'"); //Grants employee privileges to the user  NOTE: need exact privileges for the DVDManagement system.
 					System.out.println("User " + Un + " has Employee level privileges granted");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 		}
 	
 		//Remove employee 
 		public void RemoveEmp(String Un)
 {
 			connectDb(); //connect to the database
 			try{
 			System.out.println("Drop user");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" REVOKE ALL PRIVILEGES ON  `codetest` . * FROM  " + Un + " " ); //Revokes the privileges from the user 
 					System.out.println("User " + Un + " has been revokes of privileges");
 					//DBstatement.execute("");
 					System.out.println("User " + Un + " has  been deleted");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 		}
 
 	
 	
 		//Customer table
 		
 		//Create new customer  //due for testing
 		public void NewCust( String custName, String custAddr, String cusPhone, String cusEmail)
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("Creating a new Customer account");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute("INSERT INTO Customer(custAddr, custName, custPhone, custEmail) VALUES('" + custAddr + "', '" + custName + "', '" + cusPhone + "', '" + cusEmail + "')"); 
 					System.out.println("Customer has been successfully created");
 					
 				//the program should querie the database to get the customer id and print it back to the user
 				
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 	
 		//update an existing customer
 		public void UpdateCust()
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
					DBstatement.execute(" "); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//Delete a customer   // needs to be tested and tided up
 		public void DeleteCust(int custID)
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute("DELETE FROM Customer WHERE custId = "+ custID +"" ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//View customer details
 		public void getCust(int custID) //view customer by id
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		public void getCust(String CustName)// view customer by name
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		public void getCust()//View all customers 
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 	
 		
 		
 		//Employee table
 		
 		//View employee privileges
 		public void getEmp(int custID) //view employee  by id
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		
 		public void getEmp(String CustName)// view employee  by name
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		public void getEmp()//View all employee 
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//update Employee privileges
 		public void UpdateEmp()
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 			
 		//DVD table
 		
 		//View DVD table details
 		public void getDVD()
 		{
 		
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//this updates the state of the DVD
 		public void UpdateDVD(char state)
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 					
 		}
 		
 		//delete DVD
 		public void DeleteDVD(int stockID)
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//returns the number of The dvd that are available to rent in stock
 		public int AvDVDs(int stockID)
 		{
 			int aval = (Integer) null; //  null
 			
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 			
 			return aval;
 		}
 
 		//Stock table
 		
 		//View DvD movie details
 		
 		public ResultSet DisplayStock()
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("Prepair to display all stock");
 					DBstatement = DBconnection.createStatement();
 					System.out.println("Executing cursor query");
 					st_details = DBstatement.executeQuery("select * from Stock");
 					/*System.out.println("Attempting output to console");
 					while(st_details.next())
 					{
 						int id = st_details.getInt("stockId");
 						String name = st_details.getString("dvdTitle");
 						System.out.println("Id : "+id+"	Name: "+name);
 					}*/
 					
 					System.out.println("output complete");
 					//DBstatement.execute("");
 					System.out.println("Attempting cursor passing");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 		
 			return st_details;
 		}
 		
 		
 		public void getMovie(int stockID) // searchs by id number
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		public void getMovie(String name)// searches by Movie name
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//Add to stock
 		
 		public void NewStock(String d_name,int D_year,String D_director, int rentFee,int qant)
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("Connecting to database");
 					DBstatement = DBconnection.createStatement();
 					System.out.println("Prepairing to execute database statement");
 				//	DBstatement.execute("INSERT INTO Stock(dvdYear, dvdTitle, dvdDirector, dvdrentFee) VALUES(1999, 'The Saints', 'W', 5)");
 					DBstatement.execute("INSERT INTO Stock(dvdYear, dvdTitle,dvdQuant, dvdDirector, dvdrentFee) VALUES("+D_year+",'"+d_name+"',"+qant+",'"+D_director+"', " +rentFee+")"); 
 					
 					//DBstatement.execute("INSERT INTO Stock(dvdYear, dvdTitle, dvdDirector, dvdrentFee) VALUES("+D_year+",'A','test', 6)"); 
 					System.out.println("Successfully completed");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//Update Stock details
 		public void UpStockDe(int StockId,int year,String name,String dir,int qua ,int refee)
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("Prepairing to update the stock details");
 					DBstatement = DBconnection.createStatement();
 					
 					//note these can be surrounded in if statements that check if the strings entered are not blanks
 					System.out.println("Atempting year update");
 					DBstatement.execute("UPDATE Stock SET dvdYear = "+year+" WHERE stockId = "+StockId);
 					System.out.println("Year update success");
 					
 					System.out.println("Atempting Dvd Name update");
 					DBstatement.execute("UPDATE Stock SET dvdTitle = '"+name+"' WHERE stockId = "+StockId);
 					System.out.println("DvdTitle update success");
 					
 					System.out.println("Atempting Director name update");
 					DBstatement.execute("UPDATE Stock SET dvdDirector = '"+dir+"' WHERE stockId = "+StockId);
 					System.out.println("Director name update success");
 					
 					System.out.println("Atempting Quantity update");
 					DBstatement.execute("UPDATE Stock SET dvdQuant = '"+qua+"' WHERE stockId = "+StockId);
 					System.out.println("Quantity update success");
 					
 					System.out.println("Atempting rental fee update");
 					DBstatement.execute("UPDATE Stock SET dvdRentFee = '"+refee+"' WHERE stockId = "+StockId);
 					System.out.println("Rental fee update success");
 					
 					System.out.println("Update successful");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//delete stock
 		public void DelStock(int del_s_id)
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("Prepairing to delete stock");
 					DBstatement = DBconnection.createStatement();
 					System.out.println("Attempting stock delete statement");
 					DBstatement.execute("DELETE FROM Stock WHERE stockId = "+del_s_id+"");
 					System.out.println("Statement successful");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 
 		//Rented table
 		
 		//New rent
 		public void RentNew()
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//Detailed rents
 		public void RentHist() // displays all the rental history
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		public void RentHist(int rentID) // displays the rental history of a single id
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//Returned dvd
 		public void Returned(int rentID)
 		{				
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 		
 		//list all the DVD's that are overdue
 		public void Overdue()
 		{
 			connectDb(); //connect to the database
 			
 			try
 			{
 					System.out.println("");
 					DBstatement = DBconnection.createStatement();
 					DBstatement.execute(" " ); 
 					System.out.println("");
 					//DBstatement.execute("");
 					System.out.println("");
 			}
 			
 			catch(SQLException error)
 			{
 				System.out.println("Error : " +  error.getMessage());
 			}
 			
 			closeStm();//closes the statement
 			
 			closeDB();// closes the database when finished
 			
 		}
 
 }
