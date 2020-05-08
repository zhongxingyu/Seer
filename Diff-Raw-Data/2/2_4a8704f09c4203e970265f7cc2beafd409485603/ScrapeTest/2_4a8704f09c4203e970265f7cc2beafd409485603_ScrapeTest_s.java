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
 	public static void main(String[] args) {
 		int days_in_advance = 16;
 		int total_num_of_foods = 0;
 		//boolean isBreakfast = false;
 		//boolean isLunch = false;
 		//boolean isDinner = false;
 		HashSet<Food> foods = new HashSet<Food>();
 		Calendar cal = Calendar.getInstance();
 		Pattern foodpattern = Pattern.compile("openDescWin\\('','(.*?)'\\)");
 		//Pattern breakfast = Pattern.compile("");
 		String URL1 = "http://services.housing.berkeley.edu/FoodPro/dining/static/diningmenus.asp?dtCurDate=";
 		String URL2 = "&strCurLocation=";
 		String date = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
 		Date today = new Date(date);
 		date = today.toString();
 		String[] location = {"01", "03", "04", "06"};
 		try {
 			PrintWriter out = new PrintWriter(new FileWriter("foods.txt"));
 			for (int i = 0; i < days_in_advance; i++) {
 				for (int j = 0; j < location.length; j++) {
 					String URL = URL1 + date + URL2 + location[j];
 					Document doc = Jsoup.connect(URL).get();
 					Element body = doc.body();
 					//isBreakfast = true;
 					Matcher m = foodpattern.matcher(body.html());
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
 }
 
 class Food {
 	public String name;
 	public ArrayList<FoodCoordinate> appearances = new ArrayList<FoodCoordinate>();
	public int rating;
 	
 	public Food(String n) {
 		name = n;
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
