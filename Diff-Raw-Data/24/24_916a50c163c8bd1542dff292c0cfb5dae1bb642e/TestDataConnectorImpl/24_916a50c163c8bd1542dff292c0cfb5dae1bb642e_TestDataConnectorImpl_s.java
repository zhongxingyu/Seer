 package eu.neq.mais.connector.impl;
 
 import java.util.ArrayList;
 
 import eu.neq.mais.connector.Connector;
 
 /**
  * Dummy class delivering some test data by simulating a "connection" to a backend.
  * Only useful for frontend/REST service testing.
  * @author seba
  *
  */
public class TestDataConnectorImpl extends Connector {
 
 	private static Connector instance = null;
 	
 	public static Connector getInstance(){
 		
 		if(instance == null){
 			instance = new TestDataConnectorImpl();
 		}
 		return instance;
 	}
 	
 	public String login(String username, String password) {
 		double ran = Math.random();
 		System.out.println("Fake Session ID: "+ran);
 		return String.valueOf(ran);
 	}
 
 	public void logout() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Object exec(Object o) {
 		if (String.class.isInstance(o)) {
 			return null;
 		}
 		return null;
 	}
 
 }
