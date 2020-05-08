 package org.elsys.homework_19;
 
 import java.util.ArrayList;
 
 public class School {
 
 	private String name;
 	private int phone;
 	private String address;
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public int getPhone() {
 		return phone;
 	}
 	public void setPhone(int phone) {
 		this.phone = phone;
 	}
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	
 	
	static ArrayList<String> students = new ArrayList<String>(200);
 
        static ArrayList<String> teachers = new ArrayList<String>(20);
         
        static ArrayList<Integer> classes = new ArrayList<Integer>(5);
         	
 	}
 }
