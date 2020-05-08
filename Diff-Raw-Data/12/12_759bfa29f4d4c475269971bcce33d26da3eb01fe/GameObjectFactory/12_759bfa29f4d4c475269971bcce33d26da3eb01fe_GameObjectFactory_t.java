 package org.tdt4240.group20.sunstruck.gameobject;
 
 import org.tdt4240.group20.sunstruck.gameobject.GameObject.TYPES;
 
 public class GameObjectFactory {
 
 	public GameObjectFactory() {
 
 	}
 	
 	public GameObject getProductA() {// TODO remove test method
 		System.out.println("PRODUCING PRODUCT A");
 		GameObject o = new ProductA();
 		o.setType(TYPES.ENTITY1);
 		o.setArmour(10);
		return o;
 	}
 	
 	public GameObject getProductA(String str) { // TODO remove test method
 		System.out.println("PRODUCING PRODUCT A");
 		GameObject o = new ProductA(str);
 		o.setType(TYPES.ENTITY1);
 		o.setArmour(15);
 		return o;
 	}
 	
 	public GameObject getProductB() { // TODO remove test method
 		System.out.println("PRODUCING PRODUCT B");
 		GameObject o = new ProductB();
 		o.setType(TYPES.ENTITY2);
 		return o;
 	}
 }
