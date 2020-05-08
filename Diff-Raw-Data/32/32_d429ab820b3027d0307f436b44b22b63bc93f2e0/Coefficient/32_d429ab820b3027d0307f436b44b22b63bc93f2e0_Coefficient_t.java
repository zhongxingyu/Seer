package com.bookfork.Entities;

 class Coefficient
 {
 	private String type;
 	private double value;
 	
 	public Coefficient(String type, double value)
 	{
 		this.type = type;
 		this.value = value;
 	}
 	
 	public String getType()
 	{
 		return type;
 	}
 	
 	public double getValue()
 	{
 		return value;
 	}
 }
