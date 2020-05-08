 package com.jax.teamcraft.pronet;
 
 
 public class FormatKudos {
 
 	public String formatKudos(Network network) {
 		KudosCalculator kc = new KudosCalculator();
 		kc.calculate(network);
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append(formatLine("Name", "Kudos"));
 		
 		
 		for (Programmer programmer : network.getProgrammers()) {
 			sb.append(formatLine(programmer.getName(), programmer.getKudos()));
 		}
 		
 		return sb.toString();
 	}
 
 	private String formatLine(String f1, String f2) {
 		return String.format("%-10s %-20s \n", f1, f2);
 	}
 	
 	private String formatLine(String f1, double f2) {
		return String.format("%-10s %-4.2f \n", f1, f2);
 	}
 
 }
