 package com.floor7.calfood;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.HashSet;
 import java.util.Calendar;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 
 public class ScrapeTest {
 	
 	public static final String XR = "01";
 	public static final String C3 = "03";
 	public static final String CK = "04";
 	public static final String FH = "06";
 	
 	public static void main(String[] args) throws IOException{
		ArrayList<Food> foods = getFoods(C3);
 		for (Food food : foods){
 			System.out.println(food);
 		}
 	}
 	
 	public static void updateFoods() {
 		int days_in_advance = 16;
 		int total_num_of_foods = 0;
 		//boolean isBreakfast = false;
 		//boolean isLunch = false;
 		//boolean isDinner = false;
 		HashSet<Food> foods = new HashSet<Food>();
 		//Pattern breakfast = Pattern.compile("");
 		Calendar cal = Calendar.getInstance();
 		String date = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
 		Date today = new Date(date);
 		date = today.toString();
 		String[] location = {XR, C3, CK, FH};
 		try {
 			PrintWriter out = new PrintWriter(new FileWriter("foods.txt"));
 			for (int i = 0; i < days_in_advance; i++) {
 				for (int j = 0; j < location.length; j++) {
 					String URL = getURL(location[j], date);
 					Matcher m = matchPattern(("openDescWin\\('','(.*?)'\\)"), URL);
 					//isBreakfast = true;
 					while (m.find()) {
 						total_num_of_foods += 1;
 						String s = m.group(1);
 						foods.add(new Food(s));
 					}
 				}
 				today = today.getTomorrow();
 				date = today.toString();
 			}
 			for (Food f : foods) {
 				out.println(f);
 			}
 			System.out.println("Found " + total_num_of_foods + " foods");
 			out.close();
 		} catch (IOException e) {
 			System.err.println("Error");
 		}
 	}
 	
	public static ArrayList<Food> getFoods(String location) throws IOException{
		String loc = "01";
		if (location == "XR"){
			loc = XR;
		}
		if (location == "C3"){
			loc = C3;
		}
		if (location == "CK"){
			loc = CK;
		}
		if (location == "FH"){
			loc = FH;
		}
		
 		Calendar cal = Calendar.getInstance();
 		String date = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
 		Date today = new Date(date);
 		date = today.toString();
 		String URL = getURL(loc, date);
 		Matcher m = matchPattern("openDescWin\\('','(.*?)'\\)", URL);
 		ArrayList<Food> foods = new ArrayList<Food>(15);
 		while (m.find()) {
 			foods.add(new Food(m.group(1)));
 		}
 		return foods;
 	}
 	
 	private static Matcher matchPattern(String regex, String URL) throws IOException{
 		Pattern foodpattern = Pattern.compile(regex);
 		Document doc = Jsoup.connect(URL).get();
 		Element body = doc.body();
 		Matcher m = foodpattern.matcher(body.html());
 		return m;
 	}
 	
 	
	// returns URL of location at current day
 	public static String getURL(String location, String date){
 		String URL1 = "http://services.housing.berkeley.edu/FoodPro/dining/static/diningmenus.asp?dtCurDate=";
 		String URL2 = "&strCurLocation=";
 		Calendar cal = Calendar.getInstance();
 		String URL = URL1 + date + URL2 + location;
 		return URL;
 	}
 }
 
 class Food {
 	public String name;
 	public ArrayList<FoodCoordinate> appearances = new ArrayList<FoodCoordinate>();
 	public int rating = 0;
 
 	public Food(String n) {
 		name = n;
 		rating = 0;
 	}
 
 	void addCoordinate(String l, Date d, String t) {
 		appearances.add(new FoodCoordinate(l, d, t));
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 		if (this.toString().equals(((Food) obj).toString())) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return this.toString().hashCode();
 	}
 }
 
 
 class FoodCoordinate {
 	private String location;
 	private Date date;
 	private String time;
 
 	FoodCoordinate(String l, Date d, String t) {
 		location = l;
 		date = d;
 		time = t;
 	}
 
 	String getLocation() {
 		return location;
 	}
 
 	Date getDate() {
 		return date;
 	}
 
 	String getTime() {
 		return time;
 	}
 }
