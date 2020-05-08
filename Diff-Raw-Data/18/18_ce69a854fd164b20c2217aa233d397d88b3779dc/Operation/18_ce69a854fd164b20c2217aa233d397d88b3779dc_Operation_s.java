 package br.com.while42.argentum.model;
 
 import java.util.Calendar;
 
 public final class Operation {
 	
 	private final double value; // TODO: Change to BigDecimal
	private final int volume;
 	private final Calendar date; 
 		
	public Operation(double value, int volume, Calendar date) {
 		this.value = value;
		this.volume = volume;
 		this.date = date;
 	}
 
 	public double getValue() {
 		return value;
 	}
 	 
	public int getVolume() {
		return volume;
 	}
 	 
 	public Calendar getDate() {
 		return date;
 	}
 }
