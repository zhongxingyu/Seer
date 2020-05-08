 package Utilities;
 
 //Class created to read a file containing user inputs
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.net.URL;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 public class OnlineInput
 {
     private String[] SatelliteName; //Array of satellite names
     private String[] TempSatelliteName; //Temporary array - read off input file
     private String[] EphemerisLocation; //Arrary  of links to ephemeris files
     private String[] Co; //Array of color strings
     private boolean[] ModelCentered; //Array for model centered view booleans
     private String scenariotime; //Scenario time input
     BufferedReader input;
 	public OnlineInput(String path) throws IOException
 	{	
 		UpdateInput(path); //Call the read file method
 	}
         private void UpdateInput(String file) throws IOException
 	{ //reads a file - updates user input
 
         try {
 		URL url = new URL(file); //open URL file stream
 		InputStream in = url.openStream();
 		input = new BufferedReader(new InputStreamReader(in));
         } catch (FileNotFoundException ex) {
             Logger.getLogger(OnlineInput.class.getName()).log(Level.SEVERE, null, ex);
         }
 		String SatelliteNameLong = null;
 		String EphemerisLocationLong = null;
 		String ColorLong = null;
 		String ModelCenteredLong = null;               
                 String line = null;
 		//read lines - assign to a single string- parse later into the arrays
 		while ((line = input.readLine()) != null)
 		{
 			if (line.startsWith("satellitename")) //Satellite name input
 			{
 				SatelliteNameLong = line.substring(14).trim();
 			}
 			else if (line.startsWith("ephemerislocation")) //Ephemeris location input
 			{
 				EphemerisLocationLong = line.substring(18).trim();
 			}
 			else if (line.startsWith("get2Dsatcolor")) //Color input
 			{
 				ColorLong = line.substring(14).trim();
                                 if("".equals(ColorLong))
                                 {ColorLong = "null;";}
 			}
 			else if (line.startsWith("viewobject")) //View object
 			{
 				ModelCenteredLong = line.substring(11).trim();
 			}
                         else if (line.startsWith("scenariotime")) //Scenario time
                         {
                                 scenariotime = line.substring(13);
                         }
 		}
 		//seperate long strings into smaller arrays
 		TempSatelliteName = SatelliteNameLong.split(";");                
 		EphemerisLocation = EphemerisLocationLong.split(";");
 		String[] ModelCenteredArray = ModelCenteredLong.split(";");
                 ModelCentered = new boolean [ModelCenteredArray.length];
 		//convert each string in array to boolean
 		for (int i = 0; i < ModelCenteredArray.length; i++)
 		{
 			ModelCentered[i] = Boolean.parseBoolean(ModelCenteredArray[i]);
 		}
 		Co = ColorLong.split(";");
                 
                 //If there are more ephemeris files than satellite names, set names to unknown
                SatelliteName = new String[EphemerisLocation.length];
                 for(int i = 0; i < EphemerisLocation.length; i++)
                 {
                    if(i < TempSatelliteName.length)
                     {
                         SatelliteName[i] = TempSatelliteName[i];
                     }
                    else
                     {
                        SatelliteName[i] = "Unknown";
                     }
                 }
 	}
 	public void removeSatellite(int location)
 	{ //remove satellite from list
 		SatelliteName[location] = null;
 		EphemerisLocation[location] = null;
 		Co[location] = null;
 		ModelCentered[location] = false;
 	}
 	public String getSatelliteName(int location)
 	{ //return satellite name for given location in array
 		return SatelliteName[location];
 	}
 	public String getEphemerisLocation(int location)
 	{ //return ephemeris location
 		return EphemerisLocation[location];
 	}
 	public String getColor(int location)
 	{ //returns satellite color
                 return Co[location];
 	}
 	public boolean getModelCentered(int location)
 	{ //returns if 3D view should be model centered or not
 		return ModelCentered[location];
 	}
 	public void setSatelliteName(String name, int location)
 	{ //renames satellite
 		SatelliteName[location] = name;
 	}
 	public void setEphemerisLocation(String path, int location)
 	{ //change ephemeris location
 		EphemerisLocation[location] = path;
 	}
 	public void setColor(String c, int location)
 	{ //change satellite color
 		Co[location] = c;
 	}
 	public void setModelCentered(boolean model, int location)
 	{ //change if view is model-centered
 		ModelCentered[location] = model;
 	}
 	public int getSize()
 	{ //number of satellites
 		return SatelliteName.length;
 	}
         public double getTime()
         {
             try
             {
             return convertScenarioTimeString2JulianDate(scenariotime+" UTC");
             }
             catch(Exception e)
             {return 0.0;}
         }
         public static double convertScenarioTimeString2JulianDate(String scenarioTimeStr) throws Exception
         {
         GregorianCalendar currentTimeDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
 
         SimpleDateFormat dateformatShort1 = new SimpleDateFormat("dd MMM y H:m:s.S z");
         SimpleDateFormat dateformatShort2 = new SimpleDateFormat("dd MMM y H:m:s z"); // no Milliseconds
 
         try
         {
             currentTimeDate.setTime( dateformatShort1.parse(scenarioTimeStr) );
         }
         catch(Exception e2)
         {
             try
             {
                 // try reading without the milliseconds
                 currentTimeDate.setTime( dateformatShort2.parse(scenarioTimeStr) );
             }
             catch(Exception e3)
             {
                 // bad date input
                 throw new Exception("Scenario Date/Time format incorrect" + scenarioTimeStr);
             } // catch 2
 
         } // catch 1
 
         // if we get here the date was accepted
         Time t = new Time();
         t.set(currentTimeDate.getTimeInMillis());
 
         return t.getJulianDate();
         
     } // convertScenarioTimeString2JulianDate
 }
