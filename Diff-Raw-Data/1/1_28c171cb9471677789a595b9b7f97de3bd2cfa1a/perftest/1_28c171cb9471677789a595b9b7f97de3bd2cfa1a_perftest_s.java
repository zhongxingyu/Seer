 import com.objy.db.DatabaseClosedException;
 import com.objy.db.DatabaseNotFoundException;
 import com.objy.db.DatabaseOpenException;
 import com.objy.db.ObjyRuntimeException;
 import com.objy.db.app.Connection;
 import com.objy.db.app.Session;
 import com.objy.db.app.oo;
 import com.objy.db.app.ooDBObj;
 import com.objy.db.app.ooFDObj;
 
 public class perftest
 {
   public static void main(String[] args)
   {
     String bootfile = "";
 	String testType = "";
 	boolean readTest = false;
 	
 	int numObjects = 0;
 	int numCommits = 0;
 	
 	
 	
     try
     {
       System.out.println("Performance Test");
       // default assumes running from Samples/java/helloWorld folder
       
 
       if (args.length != 4)
       {
 		System.out.println("Usage: perftest {read | write} <bootfile> <number of objects to create> <number of commits>");
 		System.exit(0);
       }
       else
       {
 		testType = args[0];
         bootfile = args[1];
 		
         numObjects = Integer.parseInt(args[2]);
 		numCommits = Integer.parseInt(args[3]);
 		
 		System.out.println("Test type: " + testType);
         System.out.println("Using FD Bootfile passed as argument: " + bootfile);
 		System.out.println("Number of objects: " + numObjects);
 		System.out.println("Number of commits: " + numCommits);
 		
 		if (testType.equals("read"))
 		{
 			readTest=true;
 		}
 		
       }
 
       //Open a read/write Connection to the Objectivity Federated Database
       //and catch the standard exceptions.
       Connection connection;
 
       try
       {
         connection = Connection.open(bootfile, oo.openReadWrite);
       }
       catch (DatabaseNotFoundException e1)
       {
         System.out.println("Federated database not found " + bootfile +
           "- use oonewfd to create federated database.");
         System.exit(0);
       }
       catch (DatabaseOpenException e2)
       {
         System.out.println("Federated database already open.");
         System.exit(0);
       }
 
       //Create a new Objectivity Session with client side cache
       //dimensions of at most and at least 200 pages (Page size
       //defaults to 8K on NT).
       Session session;
       session = new Session(20, 20);
 
       String dbName = "TestDB";
 
       session.setOpenMode(oo.openReadWrite);
       session.begin();
 
       ooFDObj fd = session.getFD();
       ooDBObj db;
 
       
       if (readTest)
       {
     	  if (fd.hasDB(dbName))
     	  {
     	        System.out.println("Database " + dbName +
     	          " exists; system go for read test");
     	        db = fd.lookupDB(dbName);
     	        
     	        
     	        final long startTime = System.nanoTime();
     	    	final long endTime;
     	    	
     	    	com.objy.db.app.Iterator itr =
     	    		fd.scan("myObject");
  	
 	    		
     	    		for(int i=0; i<numObjects && itr.hasNext(); i++) {
     	    			if(i % numCommits == 0) {
     	    				itr.next();
     	    				session.commit();
     	    				session.begin();
     	    			}
     	    		}
     	    		session.commit();
     	    		    	    		
 
     	    		endTime = System.nanoTime();
     	    	
     	    	final float duration = endTime - startTime;
     	    	    	    		
     	    		System.out.println("Time taken to read (nanoseconds): " + duration);
     	    		System.out.println("Time taken to read (seconds): " + String.format("%3f", duration/1000000000));
     	    		
     	      
     	  }
     	  else
     	  {
 				System.out.println("No DB for read test, exit and run a write test.");
 				session.abort();
 				/*
 		        try
 		        {
 		          connection.close();
 		        }
 		        catch (DatabaseClosedException e3)
 		        {
 		          System.out.println("Connection close error");
 		          
 		        }
 		        
 		        */
 				System.exit(0);
 			}
 
       }
 	  else
 	  {
 		  if (fd.hasDB(dbName))
 		  {
 			  System.out.println("Database " + dbName +
 	          " exists; will delete for write test");
 			  db = fd.lookupDB(dbName);		
 			  db.delete();
 		  }
 			  
 		   
 	      try
 	      {
 	        db = fd.newDB(dbName);
 	        //
 		      // create HelloObject and set message
 		      //
 		      System.out.println("Create objects");
 
 		      myObject hello;
 
 		      //
 		      //  and name it in the scope of the database
 		      //
 
 		      
 		    final long startTime = System.nanoTime();
 		    final long endTime;
 
 				// start timer
 
 				for(int i=0; i<numObjects; i++) 
 				{			
 					hello = new myObject();
 					hello.setHelloMsg("hello");
 					if(i % numCommits == 0) {
 						session.commit();
 						session.begin();
 					}
 				}
 				session.commit();
 				//	stop timer and terminate interaction with Objectivity/DB
 	    		endTime = System.nanoTime();
 	  	
 	    		final float duration = endTime - startTime;
 	    		System.out.println("Time taken to write (nanoseconds): " + duration);
 	    		System.out.println("Time taken to write (seconds): " + String.format("%3f", duration/1000000000));
 			  		
 		  
 	      }
 	      catch (ObjyRuntimeException e3)
 	      {
 				System.out.println("Database " + dbName + " could not be created--check free disk space.");
 				session.abort();
 				/*
 				try
 				{
 				  connection.close();
 				}
 				catch (DatabaseClosedException e4)
 				{
 				  System.out.println("Connection close error");
 				  
 				}
 				*/
 				System.exit(0);
 	      }
 	  }
     }
     finally
     {
 		System.out.println("Done");
     }	
   }
 }
