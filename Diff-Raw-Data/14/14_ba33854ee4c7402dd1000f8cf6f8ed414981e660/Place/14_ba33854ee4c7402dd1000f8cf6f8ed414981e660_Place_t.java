 package utilities;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 public class Place
 {
 	private String city;
 	
 	private LinkedList<String> country;
 	
 	private static HashMap<String, LinkedList<String>> cities = null;
 	
 	private static HashSet<String> countries = null;
 	
 	private static HashSet<String> allCountries = null;
 	
 	public Place(String theCity, String theCountry)
 	{
 		initialize(theCity, theCountry);
 	}
 	
 	public Place(String unknown)
 	{
 		buildCountries();
 		buildCities();
 		
 		unknown = unknown.toUpperCase();
 		
 		if (allCountries.contains(unknown))
 			initialize(null, unknown);
 		else
 			initialize(unknown, null);
 	}
 	
 	private void initialize(String theCity, String theCountry)
 	{
 		if (theCity == null)
 			city = null;
 		else
 			city = theCity.toUpperCase();
 		
 		if (theCountry == null)
 			country = findCountry(city);
 		else
 		{
 			country = new LinkedList<String>();
 			country.add(theCountry.toUpperCase());
 		}
 	}
 	
 	private LinkedList<String> findCountry(String theCity)
 	{
 		buildCountries();
 		buildCities();
 		
 		
 		return cities.get(theCity);
 	}
 	
 	private void buildCities()
 	{
 		if(countries == null)
 			buildCountries();
 		if (cities != null)
 			return;
 		
 		cities = new HashMap<String, LinkedList<String>>();
 		
 		Scanner scanner = null;
 		
 		try {
 			scanner = new Scanner(new File("cities_countries.txt"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		while (scanner.hasNextLine())
 		{
 			String[] line = scanner.nextLine().split("\\|");
 			
 			if(line[0].equals("")) continue;
 			
 			if(countries.contains(line[1].toUpperCase()))
 			{
 				if(cities.containsKey(line[0].toUpperCase()))
 				{
 					LinkedList<String> temp = cities.get(line[0].toUpperCase());
 					temp.add(line[1].toUpperCase());
 					cities.put(line[0].toUpperCase(), temp);
 				}
 				else
 				{
 					LinkedList<String> temp = new LinkedList<String>();
 					temp.add(line[1].toUpperCase());
 					cities.put(line[0].toUpperCase(), temp);
 				}
 			}
 			else
 			{
 				allCountries.add(line[1].toUpperCase());
 			}
 		}
 	}
 	
 	private void buildCountries()
 	{
 		if (countries != null)
 			return;
 		
 		countries = new HashSet<String>();
 		allCountries = new HashSet<String>();
 		
 		Scanner scanner = null;
 		
 		try {
 			scanner = new Scanner(new File("latin_american_countries.txt"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		while (scanner.hasNextLine())
 		{
 			String line = scanner.nextLine();
 			
 			if(line.equals("")) continue;
 			
 			countries.add(line.toUpperCase());
 			allCountries.add(line.toUpperCase());
 		}
 	}
 	
 	public static HashMap<String,LinkedList<String>> getCities()
 	{
 		if(cities == null)
 		{
 			Place place = new Place("Saskatoon");
 		}
 		return cities;
 	}
 	
 	public static HashSet<String> getCountries()
 	{
 		if(countries == null)
 		{
 			Place place = new Place("Saskatoon");
 		}
 		return countries;
 	}
 	
 	public boolean hasCity()
 	{
 		return (city != null);
 	}
 	
 	public String getCity()
 	{
 		return city;
 	}
 	
 	public boolean hasCountry()
 	{
 		return (country != null);
 	}
 	
 	public LinkedList<String> getCountry()
 	{
 		return country;
 	}
 	
 	public String toString()
 	{
 		String toReturn = "";
 		
 		if(country != null)
 		{
 			for (String s : country)
 			{
				if (hasCity() && toReturn.equals(""))
				{
					toReturn += city + ", " + s;
				}
				else if(hasCity())
				{
					toReturn += " | " + city + ", " + s;
				}
 				else
				{
 					toReturn += s;
				}
 			}
 		}
 		else if(hasCity()) toReturn = city;
 		
 		return toReturn;
 	}
 	
 	public static void main(String[] args)
 	{
 		Place both = new Place("San Salvador", "El Salvador");
 		Place city = new Place("San Salvador", null);
 		Place country = new Place(null, "El Salvador");
 		Place unknown1 = new Place("San Salvador");
 		Place unknown2 = new Place("El Salvador");
 		Place fake = new Place("Fake");
 		
 		System.out.println(both);
 		System.out.println(city);
 		System.out.println(country);
 		System.out.println(unknown1);
 		System.out.println(unknown2);
 		System.out.println(fake);
 	}
 }
