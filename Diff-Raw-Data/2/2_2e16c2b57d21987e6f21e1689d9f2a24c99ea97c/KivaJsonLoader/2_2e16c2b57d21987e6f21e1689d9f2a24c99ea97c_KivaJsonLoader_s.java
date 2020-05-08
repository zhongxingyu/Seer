 package com.couchbase.kiva;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import com.couchbase.client.CouchbaseClient;
 import com.couchbase.client.protocol.views.View;
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 
 
 public class KivaJsonLoader extends CouchbaseJsonLoader{ 
 /*
 	  public static void main(String[] args)  {
 		  
 		  KivaJsonLoader kivaJsonLoader = new KivaJsonLoader();
 		  kivaJsonLoader.loadData("10.2.1.12","default","/Users/sharonbarr/Downloads/lenders/",null,1);
 		  
 	    System.exit(0);
 	  }
 */
 	  public  void loadData(String server, String bucket, String lenderPath, String loansPath, int multiplier)
 	  {
 		  this.serverVlaue =server;
 		  this.bucketValue = bucket;
 		  this.lenderPathValue = lenderPath;
 		  this.loansPathValue = loansPath;
 		  this.multiplierValue = multiplier;
 		  this.loadData();
 	  }
 	  public  void loadData()
 	  {
 
 		    try {
 			      URI local = new URI("http://"+this.serverVlaue+":8091/pools");
 			      List<URI> baseURIs = new ArrayList<URI>();
 			      baseURIs.add(local);
 		
 			      CouchbaseClient c = new CouchbaseClient(baseURIs, this.bucketValue, "");
 			      if (this.loansPathValue != null && !this.loansPathValue.isEmpty())
 			      {
 			    	  loadJsonData(this.loansPathValue, c, KivaLoans.class);
 			      }
 			      else
 			      {
 			    	  System.out.println("please provide loans files path");
 			    	  //System.exit(0);
 			      }
 
 			      
 			      if (this.lenderPathValue != null && !this.lenderPathValue.isEmpty())
 			      {
 			    	  loadJsonData(this.lenderPathValue, c, KivaLenders.class);
 			      }
 			      else
 			      {
 			    	  System.out.println("please provide lender files path");
 			    	  System.exit(0);
 			      }
 
 			      View view = c.getView("dev_lenders/", "country_count");
 			    	
 			      c.shutdown(3, TimeUnit.SECONDS);
 
 		    } catch (Exception e) {
 		      System.err.println("Error connecting to Couchbase: " + e.getMessage());
 		      e.printStackTrace();
 		      System.exit(0);
 		    }
 	 	    
   
 		  
 	  }
 	  
 	  private int loadJsonData(String dirPath, CouchbaseClient c, Class classtype) throws Exception
 	  {
 
 		  Gson headerGson = new Gson();
 		  Gson lendersGson = new Gson();
 		  Gson loansGson = new Gson();
 		  
 	    	int totalRecords = 0;
 	    	File[] files=null;
 
 	    	files = getListOfFiles(dirPath);
 	    	 	
 		    
 	    	for (File file : files)
 	    	{
 	    		System.out.print("file is "+file.getName()+", ");
 	    		int extensionIndex = file.getName().lastIndexOf(".");
 	    	    if (extensionIndex == -1)
 	    	        continue;
 
 	    	    String ext =  file.getName().substring(extensionIndex+1,file.getName().length());	
 	    	    
 	    	    if (!ext.equals("json"))
 	    	    	continue;
 
 		    	String filePath="";
 	    		filePath =dirPath+"/"+file.getName();
 	
 		    	System.out.print("filepath="+filePath+ ":  ");
 		    	
 						    	
 	
 		    	int counter =0;
 	          
 		    	if (classtype.equals(KivaLoans.class))
 		    	{
 			    	KivaLoans loans = loansGson.fromJson(new FileReader(filePath), KivaLoans.class);
 					
 			    	String headerJSONEntry = headerGson.toJson(loans.header);
 			    	c.set(loans.header.page, 0, headerJSONEntry).get();
 
 			    	
 		    		for (KivaLoans.Loan entry : loans.loans) {
 		    			entry.type="loan";
 		    			String JSONentry = lendersGson.toJson(entry);
 		    			if (entry == null || entry.id == null)
 		    			{
 		    				System.out.println("bad entry"+entry);
 		    				continue;
 		    			}
		    			for (int i=0;i<1;i++)
 		    			{
 		    				c.set(entry.id+"_"+i, 0, JSONentry).get();
 		        
 		    				counter++;
 		    				totalRecords++;
 		    			}
 		    		}
 		    	}
 		    	if (classtype.equals(KivaLenders.class))
 		    	{
 //		    		System.out.println("KivaLender class");	  
 			    	KivaLenders lenders = lendersGson.fromJson(new FileReader(filePath), KivaLenders.class);
 					
 			    	String headerJSONEntry = headerGson.toJson(lenders.header);
 			    	c.set(lenders.header.page, 0, headerJSONEntry).get();
 //		          	System.out.println("header: "+ lenders.header.page + " " + c.get(lenders.header.page));
 
 		    		for (KivaLenders.Lender entry : lenders.lenders) {
 		    			entry.type = "lender";
 		    			String JSONentry = lendersGson.toJson(entry);
 		    			if (entry == null || entry.lender_id == null)
 		    			{
 		    				System.out.println("bad entry"+entry);
 		    				continue;
 		    			}
 		    			for (int i=0;i<this.multiplierValue;i++)
 		    			{
 		    				 
 		    				c.set(entry.lender_id+"_"+i, 0, JSONentry).get();
 //				          	System.out.println("lender_id: " + entry.lender_id + " " + c.get(entry.lender_id+"_"+i));
 		        
 		    				counter++;
 		    				totalRecords++;
 		    			}
 		    		}
 		      
 	//	        System.out.println(entry.lender_id + " " + c.get(entry.lender_id));
 	//	        System.out.println(entry.lender_id +" inserted" );
 		      }
 		      System.out.println(counter + " records were entered");
 	
 	   	}
      System.out.println(totalRecords + " total number of records were entered");
      	return totalRecords;
 
 	  }
 	  private File[] getListOfFiles(String path)
 	  {
 		  File dir = new File(path);
 
 		  // The list of files can also be retrieved as File objects
 		  File[] files = dir.listFiles();
 		  // This filter only returns directories
 		return files;
 
 	  }
 
 }
 
 	
 	
 
