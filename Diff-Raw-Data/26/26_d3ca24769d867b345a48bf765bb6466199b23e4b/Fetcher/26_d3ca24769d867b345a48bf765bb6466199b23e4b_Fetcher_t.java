 package network;
 
 import dealwithcalendar.Course;
 import org.json.simple.*;
 import org.json.simple.parser.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 /**
  * Class for fetching the JSON content and parsing it. Should be able to isolate the blocks of data and create/modify course objects as needed
  * 
  * @author Deal With It Productions
  * @version 0.1
  */
 public class Fetcher
 {
     private URL address; //the address to be read
     private InputStreamReader reader; //reader for the content
     private boolean debug, debug2; // super duper debug madness
 
     /**
      * Constructor to initialize the URL and reader
      *
      * @param _URL The URL to be read
      */
     public Fetcher(String _URL)
     {
 	debug = false;
 	debug2 = false;
 
 	if(debug) System.out.println("foo 1");
 	try
 	{
 	    if(debug) System.out.println("foo 2");
 	    address = new URL(_URL); //create URL
 	    reader = new InputStreamReader(address.openStream()); //read from it
 	} catch (MalformedURLException MalfURL)
 	{	//bad URL, typo
 	    System.err.println("Error establishing address " + _URL + "; malformed URL");
 	} catch (IOException IOE)
 	{	//misc reader exception
 	    System.err.println("Error opening reader for " + _URL);
 	}
 
     }
 	
 	/**
 	 * Check if the Fetcher is ready
 	 * @return Whether the Fetcher is ready
 	 */
 	private boolean isReady()
 	{	//if everything is initialized
 		return address != null && reader != null;
 	}
 	
 	/**
 	 * Parse the content of the site and return it in an ArrayList
 	 *
 	 * @return The ArrayList of courses
 	 */
 	public ArrayList<dealwithcalendar.Course> read()
 	{ 
 		if (!isReady()) //if we encounter propelms
 			return null;
 		
 		JSONObject file = (JSONObject) JSONValue.parse(reader); //read the entire interface
 		if(file == null)
 			return null;
 		Set set = file.keySet(); //get the interface in Set form
 		Object[] setArray  = set.toArray(); //then as an array
 		if(debug) for(int i=0;i<setArray.length;i++)  //Debug listing all the objects
 		    System.out.println(setArray[i]);
 		JSONArray chunks = (JSONArray) JSONValue.parse(file.get("courses").toString()); //go into the courses, split them into individual course chunks
 		String block; //parsing tool
 		String[] split, split2, split3; //parsing tools
 		Calendar start, end; //start and end dates
 		String name; //course name
 		ArrayList<dealwithcalendar.Course> courses = new ArrayList<dealwithcalendar.Course>();
 		for(int i=0;i<chunks.size();i++) //for every course chunk
 		{
 			if(debug) System.out.println("i: " + (i+1) + " out of " + chunks.size());
 			block = chunks.get(i).toString(); //get the individual chunk as String
 			block = block.substring(1, block.length()-1); //chop off the brackets
			split = block.split("\","); //get the descriptors as an array
 			start = Calendar.getInstance(); //INSTANS PLS
 			end = Calendar.getInstance(); //MOAR INSTANS PLS
 			//start.clear();
 			//end.clear();
 			name = ""; //init just to be safe and avoid null wizardry
 			
 			for(int j=0;j<split.length;j++) //for every descriptor in the individual chunk
 			{
 				if(debug) System.out.println("j: " + (j+1) + " out of " + split.length);
 				split2 = split[j].split(":"); //split into key and value
				if(split2.length == 2 || split2[0].equals("\"course\"")) //if there are actually a key and value or it's a course
 				{
 				    if(split2[0].equals("\"course\"")) //if the key is "course"
 				    {
 						//START PUUKKO
 						split2 = split[j].split("\"course\":"); //PUUKKOPUUKKOPUUKKOPUUKKOPUUKKOPUUKKOPUUKKO
 						//END PUUKKO
 						if(debug2) System.out.println("Course name " + split2[1] + ", i" + i + ", j" + j + "\n" + block + "\n");
						name = split2[1].replace("\\\"", "'"); //then the value is the name
						name = name.replace("\"", ""); //and chop off the ""
 				    }
 					else if(split2[0].equals("\"start_date\"")) //or it might be the start date
 					{
 						 if(debug2) System.out.println("Course start date " + split2[1] + ", i" + i + ", j" + j + "\n" + block + "\n");
						 split2[1] = split2[1].replace("\"", ""); //chop off the ""
 						 split3 = split2[1].split("-"); //get the three vals (year-month-day) as array
 						 if(split3.length == 3) //if there are actually three vals
 						 {
 						     if(debug2) System.out.println("Entering date " + split2[1] + " for " + block + "\n");
 						     try
 						     {
 							     start.set(Integer.parseInt(split3[0]), Integer.parseInt(split3[1])-1, Integer.parseInt(split3[2]), 0, 0, 0);
 		                                             start.getTime();
 						     } catch (NumberFormatException NFEx) //awwwww ssshhhhit man
 						     {
 							 System.err.println("NumberFormatException parsing start date\n" + block + "\n");
 						     }
 						 } else if(debug2) System.out.println("split3 length " + split3.length + " is invalid for " + block);
 					}
 					else if(split2[0].equals("\"end_date\"")) //hey, it might be an end date
 				    {
 					if(debug2) System.out.println("Course end date " + split2[1] + ", i" + i + ", j" + j + "\n" + block + "\n");
				 	split2[1] = split2[1].replace("\"", ""); //chop off the ""
 					split3 = split2[1].split("-"); //{year,month,day}
 					if(split3.length == 3) //if the three vals are present
 					{
 					    if(debug2) System.out.println("Entering end date " + split2[1] + " for " + block + "\n");
 					    try
 					    {
 						    end.set(Integer.parseInt(split3[0]), Integer.parseInt(split3[1])-1, Integer.parseInt(split3[2]), 0, 0, 0);
 		                    end.getTime();
 					    } catch (NumberFormatException NFEx) //WHAT HAS SCIENCE DONE?
 					    {
 						 System.err.println("NumberFormatException parsing end date\n" + block);
 					    }
 					} else
 					{
 				    System.out.println("HURRRR FAIL " + name);
 				    if(debug2) System.out.println("split3 length " + split3.length + " is invalid for " + block);
 					}
 				}
 			} else if(debug2) System.out.println("Found invalid key " + split2[0] + " for block\n" + block + "\n");
 		}
 		Course c = new dealwithcalendar.Course(start, end, name, -1, -1);
 		boolean dontAdd = false;
 		
 		for (int k = 0; k < courses.size(); k++)
 		{
 		    if (courses.get(k).compareTo(c) == 0)
 		    {
 		        dontAdd = true;
 		        break;
 		    }
 		}
 		if (!dontAdd)
 		{
 		    courses.add(c);//add the new course
 		}
 		if(debug) System.out.println("Added " + name + "!");
 	}
 	for(int lol=0;lol<courses.size();lol++)
	    System.out.println(courses.get(lol).getName());
 
 
 	return courses;
 	}
 }
