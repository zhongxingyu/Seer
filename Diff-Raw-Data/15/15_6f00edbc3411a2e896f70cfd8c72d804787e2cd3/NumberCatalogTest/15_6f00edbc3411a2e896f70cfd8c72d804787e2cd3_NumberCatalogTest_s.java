 package org.katabank.app;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class NumberCatalogTest {
 	
 	private static NumberCatalog numberCatalog;
 	
 	//Digits
 	private List<String> one;
 	private List<String> two;
 	private List<String> three;
 	private List<String> four;
 	private List<String> five;
 	private List<String> six;
 	private List<String> seven;
 	private List<String> eigth;
 	private List<String> nine;
 	
 	@BeforeClass
 	public static void buildNumberCatalog() {
 		numberCatalog = NumberCatalog.buildNumberCatalog();
 	}
 	
 	@Before
 	public void setup() {
 		one = new ArrayList<String>();
		one.add("|");
		one.add("|");
 		
 		two = new ArrayList<String>();
 		two.add(" _");
 		two.add(" _|");
 		two.add("|_");
 		
 		three = new ArrayList<String>();
 		three.add("_");
 		three.add("_|");
 		three.add("_|");
 		
 		four = new ArrayList<String>();
 		four.add(" ");
 		four.add("|_|");
 		four.add("  |");
 		
 		five = new ArrayList<String>();
 		five.add(" _");
 		five.add("|_");
 		five.add(" _|");
 		
 		six = new ArrayList<String>();
 		six.add(" _");
 		six.add("|_");
 		six.add("|_|");
 		
 		seven = new ArrayList<String>();
 		seven.add(" _");
 		seven.add("  |");
 		seven.add("  |");
 		
 		eigth = new ArrayList<String>();
 		eigth.add(" _");
 		eigth.add("|_|");
 		eigth.add("|_|");
 		
 		nine = new ArrayList<String>();
 		nine.add(" _");
 		nine.add("|_|");
 		nine.add(" _|");
 	}
 
 	@Test
 	public void testGetOneDigit() {
 		int oneInt = numberCatalog.getNumberFromCatalog(one);
 		Assert.assertEquals(1, oneInt);
 	}
 	
 	@Test
 	public void testGetTwoDigit() {
 		int twoInt = numberCatalog.getNumberFromCatalog(two);
 		Assert.assertEquals(2, twoInt);
 	}
 	
 	@Test
 	public void testGetThreeDigit() {
 		int threeInt = numberCatalog.getNumberFromCatalog(three);
 		Assert.assertEquals(3, threeInt);
 	}
 	
 	@Test
 	public void testGetFourDigit() {
 		int fourInt = numberCatalog.getNumberFromCatalog(four);
 		Assert.assertEquals(4, fourInt);
 	}
 	
 	@Test
 	public void testGetFiveDigit() {
 		int fiveInt = numberCatalog.getNumberFromCatalog(five);
 		Assert.assertEquals(5, fiveInt);
 	}
 	
 	@Test
 	public void testGetSixDigit() {
 		int sixInt = numberCatalog.getNumberFromCatalog(six);
 		Assert.assertEquals(6, sixInt);
 	}
 	
 	@Test
 	public void testGetSevenDigit() {
 		int sevenInt = numberCatalog.getNumberFromCatalog(seven);
 		Assert.assertEquals(7, sevenInt);
 	}
 	
 	@Test
 	public void testGetEightDigit() {
 		int eightInt = numberCatalog.getNumberFromCatalog(eigth);
 		Assert.assertEquals(8, eightInt);
 	}
 	
 	@Test
 	public void testGetNineDigit() {
 		int nineInt = numberCatalog.getNumberFromCatalog(nine);
 		Assert.assertEquals(9, nineInt);
 	}
 }
