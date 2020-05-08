 package hackathon.boxme.utils;
 	
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.List;
 
 import com.xerox.amazonws.sdb.Item;
 import com.xerox.amazonws.sdb.ItemAttribute;
 import com.xerox.amazonws.sdb.Domain;
 import com.xerox.amazonws.sdb.SDBException;
 import com.xerox.amazonws.sdb.SDBResult;
 import com.xerox.amazonws.sdb.SimpleDB;
 
 public class SimpleDBUtils {
   private static String AWS_PROPERTIES = "awscred.credentials";
  //  private static String AWS_ACCESSID = "accessKey";
   // private static String AWS_SECRETKEY = "secretKey";
    private static String domain_name = "summerofhack";
    private static SimpleDB simpleDB;
 
	   
 	   static {

 	        //    get the accessid and secretkey strings from the properties file
 	           Properties props = new Properties();
 	           try {
 				props.load(SimpleDBUtils.class.getClassLoader().getResourceAsStream(AWS_PROPERTIES));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 /*
 	           String awsAccessId = props.getProperty(AWS_ACCESSID);
 	           String awsSecretKey = props.getProperty(AWS_SECRETKEY);
 	           */
 			String awsAccessId = "AKIAIBZWUXL2WLB6ANUA";
 			String awsSecretKey = "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";
 
 	           if ((awsAccessId == null || awsAccessId.trim().length() == 0) ||
 	               (awsSecretKey == null || awsSecretKey.trim().length() == 0))
 	           {
 	               System.out.println("Either access key or Secret key not found or not set");
 	               System.exit((int) 1);
 	           }
 
 	          simpleDB = new SimpleDB(awsAccessId, awsSecretKey, true);
 		}
 	   
 	   
 
 
 	   public static void insert(String item, String name, String value) {
 		   List<ItemAttribute> attributes = new ArrayList<ItemAttribute>();
 		   attributes.add(new ItemAttribute(name, value, true));
 		   try{
 		   	  Domain dom = simpleDB.getDomain(domain_name);
 	          Item i = dom.getItem(item);
 	          SDBResult result = i.putAttributes(attributes);
 	          System.out.println("Item Identifier: " + i.getIdentifier());
 	          System.out.println("     Request ID: " + result.getRequestId());
 	          System.out.println("      Box Usage: " + result.getBoxUsage());
 
 	      } catch (SDBException ex) {
 	          System.err.println("message : " + ex.getMessage());
 	          System.err.println("requestID : " + ex.getRequestId());
 	      } catch (Exception e) {
 	              System.err.println("Error occured: " + e.getMessage());
 	              e.printStackTrace();
 	      }
 	   }
 
 	   public static HashMap<String,String> getAttributes(String item) {
 		   List<ItemAttribute> attrs = new ArrayList<ItemAttribute>();
 		   HashMap<String,String> hashmap = new HashMap<String,String>();
 		   try{
 		   	  Domain dom = simpleDB.getDomain(domain_name);
 	          Item i = dom.getItem(item);
 	          
 	          attrs = i.getAttributes();
 	          for (ItemAttribute attr : attrs) {
 	        	  hashmap.put(attr.getName(), attr.getValue());
 	          }
 
 	      } catch (SDBException ex) {
 	          System.err.println("message : " + ex.getMessage());
 	          System.err.println("requestID : " + ex.getRequestId());
 	      } catch (Exception e) {
 	              System.err.println("Error occured: " + e.getMessage());
 	              e.printStackTrace();
 	      }
 	      return hashmap;
 	   }
 	   
 	   public static List<String> getAttributeList(String item) {
 		   List<ItemAttribute> attrs = new ArrayList<ItemAttribute>();
 		   List<String> result = new ArrayList<String>();
 		   try{
 		   	  Domain dom = simpleDB.getDomain(domain_name);
 	          Item i = dom.getItem(item);
 	          
 	          attrs = i.getAttributes();
 	          for (ItemAttribute attr : attrs) {
 	              result.add(attr.getName());
 	          }
 
 	      } catch (SDBException ex) {
 	          System.err.println("message : " + ex.getMessage());
 	          System.err.println("requestID : " + ex.getRequestId());
 	      } catch (Exception e) {
 	              System.err.println("Error occured: " + e.getMessage());
 	              e.printStackTrace();
 	      }
 	      return result;
 	   }
 	   
 	   public static String getAttribute(String item, String attribute) {
 		   String result = "";
 		   try{
 		   	  Domain dom = simpleDB.getDomain(domain_name);
 	          Item i = dom.getItem(item);
 	          
 	          List<ItemAttribute> attrs = i.getAttributes();
 	          for (ItemAttribute attr : attrs) {
 	        	  if(attr.getName().equals(attribute)){
 	        		  return attr.getValue();
 	        	  }
 	          }
 	          throw new Exception();
 
 	      } catch (SDBException ex) {
 	          System.err.println("message : " + ex.getMessage());
 	          System.err.println("requestID : " + ex.getRequestId());
 	      } catch (Exception e) {
 	              System.err.println("Error occured: " + e.getMessage());
 	              e.printStackTrace();
 	      }
 		return result;
 	   }
 	   
 	   
 	   /*
 	   
 	   
 	public static void main(String[] args){		   
 	     try {
 	    	 
 	    	 ItemAttribute e = new ItemAttribute("check", "works", true);
 	    	 ItemAttribute e1 = new ItemAttribute("check1", "works1", true);
 	    	 String fbid = "12345";
 	    	 ArrayList<ItemAttribute> attributeinput = new ArrayList<ItemAttribute>();
 	    	 attributeinput.add(e);
 	    	 attributeinput.add(e1);
 	    	 SimpleDBUtils.insert(fbid, "test1", "test2");
 	    	 System.out.println("added");
 
 	    	 System.out.println(SimpleDBUtils.getAttribute(fbid, "check"));
 	    	 
 	          
 
 	       List<ItemAttribute> attributes = SimpleDBUtils.getAttributes(fbid);
 	       for (ItemAttribute attr : attributes) {
 	        	  System.out.println(attr.getName());
 	       }
 
 	       
 
 	      } catch (Exception e) {
 	              System.err.println("Error occured: " + e.getMessage());
 	              e.printStackTrace();
 	      }
 	   }
 	   */
 	   
 }
