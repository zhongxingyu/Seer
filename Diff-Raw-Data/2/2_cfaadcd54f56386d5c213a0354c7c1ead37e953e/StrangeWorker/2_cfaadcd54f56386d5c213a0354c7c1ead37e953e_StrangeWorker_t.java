 package org.thirdfoundation;
 public class StrangeWorker implements Worker {
 
 	public static void main(String[] args) {
 		System.out.println(new StrangeWorker().getStringValue());
 	}
 
 	@Override
 	public String getStringValue() {
		return "hi, so strange but ok yea yea...";
 	}
 	
 }
