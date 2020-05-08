 package org.thirdfoundation;
 public class StrangeWorker implements Worker {
 
 	public static void main(String[] args) {
 		System.out.println(new StrangeWorker().getStringValue());
 	}
 
 	@Override
 	public String getStringValue() {
		return "she's so strange and she wore a black mustache";
 	}
 	
 }
