 package com.aj3.kiss.models;
 
 public class Item {
 	public static final int NO_ID = -1;
 	private int id;
 	private String name;
 	private Category category;
 	private Unit unit;
 	private String upc;
 	
 	public Item() {
 		this.id = NO_ID;
 	}
 	
 	public Item(int id) {
 		this.id = id;
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
 	public Category getCategory() {
 		return category;
 	}
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	public Unit getUnit() {
 		return unit;
 	}
 
 	public void setUnit(Unit unit) {
 		this.unit = unit;
 
 	}
 	
 	public String getUpc() {
 		return upc;
 	}
 	
 	public void setUpc(String upc) {
 		this.upc = upc;
 	}	
 	
 	public String toString() {
		return name;
 	}
 	
 	public boolean equals(Item item) {
 		return this.getName().equals(item.getName());
 	}
 }
