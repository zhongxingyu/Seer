package com.bookfork.Entities;
 
import java.util.ArrayList;
import java.util.List;

public class Event
 {
 	private String name;
	private List<Coefficient> coefs;
 	
 	public Event(String name)
 	{
 		this.name = name;
		coefs = new ArrayList<Coefficient>();
 	}
 	
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	public String  getName()
 	{
 		return name;
 	}
 }
