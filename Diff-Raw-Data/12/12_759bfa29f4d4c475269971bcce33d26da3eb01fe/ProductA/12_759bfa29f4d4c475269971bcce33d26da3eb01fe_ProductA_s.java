 package org.tdt4240.group20.sunstruck.gameobject;
 
 /**
  * Example class for products of the GameObjectFactory
  * @author DiaAWAY
  *
  */
 public class ProductA extends GameObject { // TODO remove test class
 
 	String testStr = "";
 	
 	public ProductA() {
		this("");
 	}
 	
 	public ProductA(String str) {
 		testStr = str;
 	}
 	
 	public String getTestStr() {
 		return testStr;
 	}
 
 	@Override
 	public void update() {
		System.out.println("PRODUCT A UPDATING! type:"+getType());
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 }
