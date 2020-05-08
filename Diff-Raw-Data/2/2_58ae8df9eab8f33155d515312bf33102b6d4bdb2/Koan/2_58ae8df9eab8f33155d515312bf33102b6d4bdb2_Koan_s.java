 package com.tdl.oodpkoans.structural.proxy;
 
 /*
  * 
  * The Proxy Pattern provides a surrogate or
  * placeholder for another object to control access to it.
  * 
  */
 
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Proxy;
 import java.util.Hashtable;
 
 import org.junit.Test;
 
 import com.tdl.oodpkoans.COUT;
 import com.tdl.oodpkoans.TDLKoan;
 
 
 
 public class Koan extends TDLKoan{	
 	
	Hashtable datingDB = new Hashtable();
  	
 	@Test
 	public void talk_to_master(){
 		initializeDatabase();
 		drive();
 	}
 
 	public void drive() {
 		PersonBean joe = getPersonFromDatabase("Joe Javabean"); 
 		PersonBean ownerProxy = getOwnerProxy(joe);
 		COUT.println("Name is " + ownerProxy.getName());
 		ownerProxy.setInterests("bowling, Go");
 		COUT.println("Interests set from owner proxy");
 		try {
 			ownerProxy.setHotOrNotRating(10);
 		} catch (Exception e) {
 			COUT.println("Can't set rating from owner proxy");
 		}
 		COUT.println("Rating is " + ownerProxy.getHotOrNotRating());
 		assertEquals(___, COUT.getLastLines(4));
 
 		PersonBean nonOwnerProxy = getNonOwnerProxy(joe);
 		COUT.println("Name is " + nonOwnerProxy.getName());
 		try {
 			nonOwnerProxy.setInterests("bowling, Go");
 		} catch (Exception e) {
 			COUT.println("Can't set interests from non owner proxy");
 		}
 		nonOwnerProxy.setHotOrNotRating(3);
 		COUT.println("Rating set from non owner proxy");
 		COUT.println("Rating is " + nonOwnerProxy.getHotOrNotRating());
 		assertEquals(___, COUT.getLastLines(4));
 	}
 
 	PersonBean getOwnerProxy(PersonBean person) {
  		
         return (PersonBean) Proxy.newProxyInstance( 
             	person.getClass().getClassLoader(),
             	person.getClass().getInterfaces(),
                 new OwnerInvocationHandler(person));
 	}
 
 	PersonBean getNonOwnerProxy(PersonBean person) {
 		
         return (PersonBean) Proxy.newProxyInstance(
             	person.getClass().getClassLoader(),
             	person.getClass().getInterfaces(),
                 new NonOwnerInvocationHandler(person));
 	}
 
 	PersonBean getPersonFromDatabase(String name) {
 		return (PersonBean)datingDB.get(name);
 	}
 
 	void initializeDatabase() {
 		PersonBean joe = new PersonBeanImpl();
 		joe.setName("Joe Javabean");
 		joe.setInterests("cars, computers, music");
 		joe.setHotOrNotRating(7);
 		datingDB.put(joe.getName(), joe);
 
 		PersonBean kelly = new PersonBeanImpl();
 		kelly.setName("Kelly Klosure");
 		kelly.setInterests("ebay, movies, music");
 		kelly.setHotOrNotRating(6);
 		datingDB.put(kelly.getName(), kelly);
 	}
 }
