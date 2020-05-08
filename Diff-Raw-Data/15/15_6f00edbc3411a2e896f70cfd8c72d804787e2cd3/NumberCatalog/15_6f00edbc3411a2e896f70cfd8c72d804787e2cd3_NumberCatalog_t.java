 package org.katabank.app;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class NumberCatalog {
 	
 	private Map<List<String>, Integer> numberCatalog = new HashMap<List<String>, Integer>();
 	
 	private NumberCatalog() {
 		numberCatalog.put(buildOne(), 1);
 		numberCatalog.put(buildTwo(), 2);
 		numberCatalog.put(buildThree(), 3);
 		numberCatalog.put(buildFour(), 4);
 		numberCatalog.put(buildFive(), 5);
 		numberCatalog.put(buildSix(), 6);
 		numberCatalog.put(buildSeven(), 7);
 		numberCatalog.put(buildEigth(), 8);
 		numberCatalog.put(buildNine(), 9);
 	}
 	
 	public static NumberCatalog buildNumberCatalog() {
 		return new NumberCatalog();
 	}
 	
 	private static List<String> buildOne() {
 		List<String> one = new ArrayList<String>();
		one.add("   ");
		one.add("  |");
		one.add("  |");
 		return one;
 	}
 	
 	private static List<String> buildTwo() {
 		List<String> two = new ArrayList<String>();
 		two.add(" _");
 		two.add(" _|");
 		two.add("|_");
 		return two;
 	}
 
 	private static List<String> buildThree() {
 		List<String> three = new ArrayList<String>();
 		three.add("_");
 		three.add("_|");
 		three.add("_|");
 		return three;
 	}
 
 	private static List<String> buildFour() {
 		List<String> four = new ArrayList<String>();
 		four.add(" ");
 		four.add("|_|");
 		four.add("  |");
 		return four;
 	}
 	
 	private static List<String> buildFive() {
 		List<String> five = new ArrayList<String>();
 		five.add(" _");
 		five.add("|_");
 		five.add(" _|");
 		return five;
 	}
 	
 	private static List<String> buildSix() {
 		List<String> six = new ArrayList<String>();
 		six.add(" _");
 		six.add("|_");
 		six.add("|_|");
 		return six;
 	}
 	
 	private static List<String> buildSeven() {
 		List<String> seven = new ArrayList<String>();
 		seven.add(" _");
 		seven.add("  |");
 		seven.add("  |");
 		return seven;
 	}
 	
 	private static List<String> buildEigth() {
 		List<String> eigth = new ArrayList<String>();
 		eigth.add(" _");
 		eigth.add("|_|");
 		eigth.add("|_|");
 		return eigth;
 	}
 	
 	private static List<String> buildNine() {
 		List<String> nine = new ArrayList<String>();
 		nine.add(" _");
 		nine.add("|_|");
 		nine.add(" _|");
 		return nine;
 	}
 	
 	public int getNumberFromCatalog(List<String> digitComponents) {
 		Integer intDigit = numberCatalog.get(digitComponents);
 		if(intDigit == null) {
 			return -1;
 		} else {
 			return intDigit;
 		}
 	}
 }
