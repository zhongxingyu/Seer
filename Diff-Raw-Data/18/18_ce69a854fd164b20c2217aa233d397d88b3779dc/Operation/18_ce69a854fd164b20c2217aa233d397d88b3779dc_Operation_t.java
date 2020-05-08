 package br.com.while42.argentum.model;
 
 import java.util.Calendar;
 
 public final class Operation {
 	
 	private final double value; // TODO: Change to BigDecimal
	private final int ammount;
 	private final Calendar date; 
 		
	public Operation(double value, int ammount, Calendar date) {
 		this.value = value;
		this.ammount = ammount;
 		this.date = date;
 	}
 
 	public double getValue() {
 		return value;
 	}
	
	public int getAmmount() {
		return ammount;
	}
 	 
	public double getVolume() {
		return value * ammount;
 	}
 	 
 	public Calendar getDate() {
 		return date;
 	}
 }
