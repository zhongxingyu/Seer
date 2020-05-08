 package com.homework.hw3.hsql;
 
 import java.util.ArrayList;
 
 public class DbItem {
 
 	public static ArrayList<DbItem> itemList = new ArrayList<DbItem>();
 	
 	private long id;
 	private String name;
 	private String code;
 	private long superior_id;
 	
	public DbItem(long id, String name, String code) {
 		this.id = id;
 		this.name = name;
 		this.code = code;
		//this.superior_id = superior;
 	}
 	
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getCode() {
 		return code;
 	}
 	public void setCode(String code) {
 		this.code = code;
 	}
 	
 	public long getSuperior() {
 		return superior_id;
 	}
 	public void setSuperior(long superior_id) {
 		this.superior_id = superior_id;
 	}
 }
