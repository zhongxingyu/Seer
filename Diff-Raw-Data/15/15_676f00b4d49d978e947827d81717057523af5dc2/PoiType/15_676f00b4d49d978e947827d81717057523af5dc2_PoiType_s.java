 package com.nearme;
 
 /**
  * Class encapsulating a type of Point of Interest, e.g. "restaurants"
  * 
  * @author twhume
  *
  */
 public class PoiType {
 	private int id;
 	private String name;
 	
 	public static final PoiType FRIEND = new PoiType("Friends", 0);
 	
 	public PoiType(String n, int i) {
 		this.name = n;
 		this.id = i;
 	}
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 }
