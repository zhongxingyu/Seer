 package com.rolflekang.kube95;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 
 @SuppressWarnings("serial")
 public class Pantekassa extends ArrayList<Pant>{
 	
 	
 	
 	public Pantekassa() {
 		
 	}
 	
 	public void parseStrings(String[] list) {
 		for (String s : list) {
 			String[] bits = s.split("\\|");
 			String[] d = bits[0].split("-");
 			Calendar cal = Calendar.getInstance();
			cal.set(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]));
 			this.add(new Pant(cal.getTime(), Double.parseDouble(bits[1]), bits[2]));	
 		}
 	}
 	public double getSum() { 
 		int sum = 0;
 		for (Pant p : this) sum += p.getAmount();
 		return sum;
 	}
 }
