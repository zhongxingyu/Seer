 /**
  * Jamie Gaultois
  * jpg627
  * 11066502
  * 
  * Steven Heidel
  * sdh951
  * 11078053
  * 
  * Class to hold an individual article. Will store the article's source, date, city,
  * and country.
  */
 
 package corpus;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import main.Main;
 
 import utilities.Place;
 
 public class Article 
 {	
 	private String source;
 	
 	private Date dateWritten;
 	
 	private Place locationWritten;
 	
 	private String articleText;
 	
 	private String articleTextNoPunct;
 	
 	private String cities = "";
 	
 	private String countries = "";
 	
 	public Article (String source, Date dateWritten, Place location, String articleText)
 	{
 		this.source = source;
 		this.dateWritten = dateWritten;
 		this.locationWritten = location;
 		this.articleText = articleText;
 		this.articleTextNoPunct = this.articleText.replaceAll("[^A-Z0-9]", " ");
 		
 		HashMap<String, LinkedList<String>> allCities = Place.getCities();
 		HashSet<String> allCountries = Place.getCountries();
 		
 		if(allCities == null || allCountries == null)
 		{
 			System.out.println("Places should have been initialized already.");
 			System.exit(-1);
 		}
 		
 		// Look for cities and countries in the text, ignoring any blank ones
 		for(String s : allCities.keySet())
 		{
 			String temp = " " + s + " ";
 			if(this.articleTextNoPunct.contains(temp) && !s.equals(""))
 			{
 				cities += s + "|";
				LinkedList<String> tempCountries = allCities.get(s);
				for(String r : tempCountries)
				{
					countries += r + "|";
				}
 			}
 		}
 		
 		for(String s : allCountries)
 		{
 			String temp = " " + s + " ";
			if(this.articleTextNoPunct.contains(temp) && !s.equals("") && !countries.contains(s))
 			{
 				countries += s + "|";
 			}
 		}
 	}
 	
 	public String getSource()
 	{
 		return source;
 	}
 	
 	public void setSource(String newSource)
 	{
 		source = newSource;
 	}
 	
 	public Date getDateWritten()
 	{
 		return dateWritten;
 	}
 	
 	public void setDateWritten(Date newDateWritten)
 	{
 		dateWritten = newDateWritten;
 	}
 	
 	public Place getLocationWritten()
 	{
 		return locationWritten;
 	}
 	
 	public void setLocationWritten(Place newLocation)
 	{
 		locationWritten = newLocation;
 	}
 	
 	public String getArticleText()
 	{
 		return articleText;
 	}
 	
 	public void setArticleText(String newArticleText)
 	{
 		articleText = newArticleText;
 	}
 	
 	public String getCities()
 	{
 		return cities;
 	}
 	
 	public String getCountries()
 	{
 		return countries;
 	}
 	
 	/**
 	 * A method to determine whether this article contains a certain string of text.
 	 * @param text - The text we're hoping to find in the article.
 	 * @return True if the text is contained in the article, false otherwise.
 	 */
 	public boolean containsText(String text)
 	{
 		return articleText.contains(text);
 	}
 	
 	/**
 	 * A method to determine whether this article contains a certain date.
 	 * @param date - The date that we want to find.
 	 * @return	True if the article contains the date, false otherwise.
 	 */
 	public boolean containsDate(String date) throws IllegalArgumentException
 	{
 		SimpleDateFormat df = new SimpleDateFormat("dd MMM yy");
 		SimpleDateFormat noDay = new SimpleDateFormat("MMM yy");
 		Date givenDate = null;
 		
 		try
 		{
 			givenDate = df.parse(date);
 		}
 		catch (Exception e)
 		{
 			try
 			{
 				givenDate = noDay.parse(date);
 			}
 			catch (Exception e1)
 			{
 				throw new IllegalArgumentException("Given date was not in the correct format");
 			}
 		}
 		
 		if(dateWritten.equals(givenDate))
 		{
 			return true;
 		}
 		
 		LinkedList<String> dates = getDatesFromText();
 		for(String s : dates)
 		{
 			Date dateFromText = null;
 			try
 			{
 				dateFromText = df.parse(date);
 			}
 			catch (Exception e)
 			{
 				try
 				{
 					dateFromText = noDay.parse(date);
 				}
 				catch (Exception e1)
 				{
 					throw new IllegalArgumentException("Date from text was not in correct format");
 				}
 			}
 			if(dateFromText.equals(givenDate))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * A method to determine whether this article contains a certain place.
 	 * @param toFind - The place that we want to find.
 	 * @return	True if the article contains the place, false otherwise.
 	 */
 	public boolean containsPlace(Place toFind)
 	{
 		boolean containsCity = false;
 		boolean containsCountry = false;
 		
 		if(toFind.hasCity())
 		{
 			if(cities.contains(toFind.getCity()))
 			{
 				containsCity = true;
 			}
 			if(locationWritten.hasCity() && toFind.getCity().equals(locationWritten.getCity()))
 			{
 				containsCity = true;
 			}
 		}
 		else
 		{
 			// If no city was mentioned, then we just say that the article contains the required city
 			containsCity = true;
 		}
 		
 		if(toFind.hasCountry())
 		{
 			LinkedList<String> country = toFind.getCountry();
 			LinkedList<String> writtenCountry = null;
 			if(locationWritten.hasCountry())
 			{
 				writtenCountry = locationWritten.getCountry();
 			}
 			
 			for(String s : country)
 			{
 				if(countries.contains(s))
 				{
 					containsCountry = true;
 				}
 				if(writtenCountry != null)
 				{
 					for(String r : writtenCountry)
 					{
 						if(r.equals(s))
 						{
 							containsCountry = true;
 						}
 					}
 				}
 			}
 		}
 		
 		return containsCity && containsCountry;
 	}
 	
 	/**
 	 * A method to extract all dates from the article text.
 	 * @return A list of the dates in the article in String format.
 	 */
 	public LinkedList<String> getDatesFromText()
 	{
 		LinkedList<String> dateList = new LinkedList<String>();
 		
 		// Regular expression to match dates in the article formatted like the below examples:
 		// 5 JANUARY 89 or 5 JANUARY or JANUARY 89. Could also give 4 digit years 
 		String pattern = "(\\b[0-9]|[1-2][0-9]|3[0-1])?(-[0-9]|[1-2][0-9]|3[0-1])?" +
 				"[ ]+(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)" +
 				"[ ]?+([1-2]?[0-9]?[8-9][0-9])?(.{9})";
 		Matcher m = Pattern.compile(pattern).matcher(articleText);
 		
 		while(m.find())
 		{
 			String date = m.group(0);
 			String[] dateSplit = date.split(" ");
 			boolean hasYear = true;
 			boolean hasDay = true;
 			
 			// Check that we didn't parse 5 NOVEMBER DAM or 19 APRIL MOVEMENT
 			if(date.contains("MOVEMENT") || date.contains("DAM"))
 			{
 				continue;
 			}
 			
 			try
 			{
 				Integer.parseInt(dateSplit[0]);
 			}
 			catch (NumberFormatException e)
 			{
 				hasDay = false;
 			}
 			
 			try
 			{
 				Integer.parseInt(dateSplit[2]);
 				date = dateSplit[0] + " " + dateSplit[1] + " " + dateSplit[2];
 			}
 			catch (NumberFormatException e)
 			{
 				hasYear = false;
 				date = dateSplit[0] + " " + dateSplit[1] + " ";
 			}
 			
 			// Ensure that the day or the year are present
 			if(!(hasYear || hasDay))
 			{
 				continue;
 			}
 			
 			// If there was no year in the article, add one based on which month was written about
 			if(!hasYear)
 			{
 				String dateFormatString = "MMM";
 				SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
 				Date findYear = null;
 				try
 				{
 					findYear = df.parse(dateSplit[1]);
 					if(findYear.getMonth() > dateWritten.getMonth() + 2)
 					{
 						date += (dateWritten.getYear() - 1) + "(yearadded)";
 					}
 					else
 					{
 						date += dateWritten.getYear() + "(yearadded)";
 					}
 				}
 				catch(ParseException e)
 				{
 					System.out.println("Error parsing the date.");
 				}				
 			}
 			
 			dateList.add(date);
 		}
 		
 		return dateList;
 	}
 	
 	/**
 	 * A method to find how close a given date is to a given string in the article text.
 	 * @param	date - string containing the date how it was in the article
 	 * 			text - text contained in the article that we want to find the closeness to the date
 	 * @return 	The number of character positions the date and text are apart. -1 is returned if the date
 	 * 			or the text aren't in the article.
 	 */
 	public int closenessOfDateToText (String date, String text)
 	{
 		LinkedList<Integer> datePositions = new LinkedList<Integer>();
 		
 		// Find out if we added the year to the date or if it was given
 		String[] dateSplit = date.split(" ");
 		try
 		{
 			if(dateSplit[2].contains("(yearadded)"))
 			{
 				date = dateSplit[0] + " " + dateSplit[1];
 			}
 		}
 		catch (Exception e)
 		{
 			System.out.println("Date given in unexpected format.");
 			return -1;
 		}
 		
 		// Start looking through the article for the text and date
 		int textLocation = articleText.indexOf(text);
 		if(textLocation == -1)
 		{
 			return -1;
 		}
 		int textFinalLocation = textLocation + text.length();
 		
 		int dateLocation = articleText.indexOf(date);
 		if(dateLocation == -1)
 		{
 			return -1;
 		}
 		
 		while(dateLocation != -1)
 		{
 			datePositions.add(dateLocation);
 			dateLocation = articleText.indexOf(date, dateLocation+1);
 		}
 		
 		// Loop through the locations of dates and find the minimum distance between the dates and the text
 		// For every paragraph away from the current one, add 1000 to the distance. This way we favour dates in the
 		// same paragraph as the text
 		int minDistance = Integer.MAX_VALUE;
 		for(Integer i : datePositions)
 		{
 			String substring = "";
 			if(i < textLocation)
 			{
 				substring = articleText.substring(i, textLocation);
 			}
 			else if(i > textFinalLocation)
 			{
 				substring = articleText.substring(textFinalLocation, i);
 			}
 			i += 1000 * (substring.split("\n").length - 1);
 			
 			if (Math.min(Math.abs(i-textLocation), Math.abs(i-textFinalLocation)) < minDistance)
 			{
 				minDistance = Math.min(Math.abs(i-textLocation), Math.abs(i-textFinalLocation));
 			}
 		}
 		
 		return minDistance;
 	}
 	
 	/**
 	 * A method to find how close a given city or country is to a given string in the article text.
 	 * @param	place - string containing the city or country to find
 	 * 			text - text contained in the article that we want to find the closeness to the city or country
 	 * @return 	The number of character positions the date and text are apart. -1 is returned if the
 	 * 			city, country, or the text aren't in the article.
 	 */
 	public int closenessOfPlaceToText (String place, String text)
 	{
 		// Answers often given Salvador, Brazil when we want San Salvador, El Salvador
 		// so here is a quick fix
 		if(place.equals("SALVADOR") && articleTextNoPunct.contains("SAN SALVADOR"))
 		{
 			return Integer.MAX_VALUE;
 		}		
 
 		int minDistance = Integer.MAX_VALUE;
 		
 		// Make sure the place is a complete match by adding a space. We don't want
 		// Colombian in the text to match Colombia as a place, for example.
 		place += " ";
 		
 		LinkedList<Integer> placePositions = new LinkedList<Integer>();
 				
 		// Start looking through the article for the text and place
 		int textLocation = articleText.indexOf(text);
 		if(textLocation == -1)
 		{
			return Integer.MAX_VALUE;
 		}
 		int textFinalLocation = textLocation + text.length();
 		
 		int placeLocation = articleTextNoPunct.indexOf(place);
 		if(placeLocation == -1)
 		{
			return Integer.MAX_VALUE;
 		}
 		
 		while(placeLocation != -1)
 		{
 			placePositions.add(placeLocation);
 			placeLocation = articleTextNoPunct.indexOf(place, placeLocation+1);
 		}
 		
 		// Loop through the locations of dates and find the minimum distance between the dates and the text
 		// For every paragraph away from the current one, add 1000 to the distance. This way we favour places in the
 		// same paragraph as the text
 		for(Integer i : placePositions)
 		{
 			String substring = "";
 			if(i < textLocation)
 			{
 				substring = articleText.substring(i, textLocation);
 				int tempMin = (textLocation - i) + 1000 * (substring.split("\n").length-1);
 				if(tempMin < minDistance)
 				{
 					minDistance = tempMin;
 				}
 			}
 			else if(i > textFinalLocation)
 			{
 				substring = articleText.substring(textFinalLocation, i);
 				int tempMin = (i - textFinalLocation) + 1000 * (substring.split("\n").length-1);
 				if(tempMin < minDistance)
 				{
 					minDistance = tempMin;
 				}
 			}
 			else
 			{
 				if (Math.min(Math.abs(i-textLocation), Math.abs(i-textFinalLocation)) < minDistance)
 				{
 					minDistance = Math.min(Math.abs(i-textLocation), Math.abs(i-textFinalLocation));
 				}
 			}
 		}
 		
 		return minDistance;
 	}
 	
 	/**
 	 * Test method
 	 */
 	public static void main(String[] argv)
 	{
 		String corpus = "";
 		try
 		{
 			corpus = Main.readTextFile("corpus.txt");
 		}
 		catch (Exception e)
 		{
 			System.out.println("Error reading from the file");
 		}
 		
 		Corpus corp = new Corpus(corpus);
 		
 		LinkedList<Article> articles = corp.getArticlesWithText("THE");
 		
 		for (Article art : articles)
 		{
 			LinkedList<String> dates = art.getDatesFromText();
 			
 			for (String s : dates)
 			{
 				System.out.println(s);
 				System.out.println("Minimum distance between date and text: " + art.closenessOfDateToText(s, "THE"));
 			}
 		}
 	}
 }
