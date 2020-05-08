 import java.util.HashMap;
 
 /**
  * 
  * NOTE TO SELF
  * 
  * Object
  * 	hashmap - what we use to compare
  * 		case - from xml
  * 			datatype
  * 			purpose
  * 			recipients
  * 			retention
  * 			distance
  * 		context - object has compare function
  * 			distance
  * 			time
  * 			etc
  *	Action - object
  * 	
  * 		
  */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PolicyObject
 {
 	// INFORMATION ABOUT THE COMPANY
 	HashMap<String, String> entity;
 	
 	// CASE
 	private ArrayList<Purpose> purpose;
 	private ArrayList<Retention> retention;
 	private ArrayList<Recipient> recipient;
 	private ArrayList<Category> categories;
 
 	public PolicyObject()
 	{
 		entity = new HashMap<String, String>();
 		purpose = new ArrayList<Purpose>();
 		retention = new ArrayList<Retention>();
 		recipient = new ArrayList<Recipient>();
 		categories = new ArrayList<Category>();
 	}
 	
 	public void addPurpose(Purpose p)
 	{
 		purpose.add(p);
 	}
 	
 	public void addRetention(Retention t)
 	{
 		retention.add(t);
 	}
 	
 	public void addRecipient(Recipient r)
 	{
 		recipient.add(r);
 	}
 	public void addCategory(Category c)
 	{
 		categories.add(c);
 	}
 	
 	public void addEntityData(String key, String value)
 	{
 		entity.put(key, value);
 	}
 	
 	public void print_all()
 	{
 		System.out.println("ENTITY");
 		Set set = entity.entrySet();
 		Iterator it = set.iterator();
 		while(it.hasNext())
 		{
 			Map.Entry me = (Map.Entry)it.next();
 			System.out.println("KEY: " + me.getKey() + ", VAL: " + me.getValue() );
 		}
 		
 		for(int i=0; i < purpose.size(); i++)
 		{
 			System.out.println("PURPOSE: " + purpose.get(i));
 		}
 		
 		for(int i=0; i < recipient.size(); i++)
 		{
 			System.out.println("RECIPIENT: " + recipient.get(i));
 		}
 		
 		for(int i=0; i < retention.size(); i++)
 		{
 			System.out.println("RETENTION: " + retention.get(i));
 		}
 		
 		for(int i=0; i < categories.size(); i++)
 		{
 			System.out.println("CATEGORIES: " + categories.get(i));
 		}
 	}
 }
