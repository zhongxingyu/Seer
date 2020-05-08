 import java.util.*;
 import java.io.*;
 
 public class tweetSelect {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		
 		ArrayList<String> weatherGood = new ArrayList<String>();
 		ArrayList<String> weatherBad = new ArrayList<String>();
 		ArrayList<String> temperatureGood = new ArrayList<String>();
 		ArrayList<String> temperatureBad = new ArrayList<String>();
 		ArrayList<String> humidityGood = new ArrayList<String>();
 		ArrayList<String> humidityBad = new ArrayList<String>();
 		ArrayList<String> tiltGood = new ArrayList<String>();
 		ArrayList<String> tiltBad = new ArrayList<String>();
 		ArrayList<String> lightGood = new ArrayList<String>();
 		ArrayList<String> lightBad = new ArrayList<String>();
 		ArrayList<String> soundGood = new ArrayList<String>();
 		ArrayList<String> soundBad = new ArrayList<String>();
 		ArrayList<String> miscGood = new ArrayList<String>();
 		ArrayList<String> miscBad = new ArrayList<String>();
 		ArrayList<String> moistureGood = new ArrayList<String>();
 		ArrayList<String> moistureBad = new ArrayList<String>();
 
 		ArrayList<String> masterList = new ArrayList<String>();
 		
 		ArrayList<String> usedList = new ArrayList<String>();
 
 		
 		//Read in the file
 		//BufferedReader reader = new BufferedReader(new FileReader("PlantTweets.txt"));
 		BufferedReader reader = new BufferedReader(new FileReader("PlantTweetsSheet.tsv"));
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			
 			//=======================================================================================\\
 
 		///// WEATHER ///// WEATHER ///// WEATHER ///// WEATHER ///// WEATHER ///// WEATHER 
 			if(line.contains("WeatherGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					weatherGood.add(part);
 				}
 
 			}
 			if(line.contains("WeatherBad")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					weatherBad.add(part);
 				}
 			}
 		///// WEATHER ///// WEATHER ///// WEATHER ///// WEATHER ///// WEATHER ///// WEATHER
 			
 			//=======================================================================================\\
 
 		///// Temperature ///// Temperature ///// Temperature ///// Temperature ///// Temperature 
 			if(line.contains("TemperatureGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					temperatureGood.add(part);
 				}
 			}
 			if(line.contains("TemperatureBad")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					temperatureBad.add(part);
 				}
 			}
 		///// Temperature ///// Temperature ///// Temperature ///// Temperature ///// Temperature 
 			
 			//=======================================================================================\\
 
 		///// Humidity ///// Humidity ///// Humidity ///// Humidity ///// Humidity ///// Humidity 
 			if(line.contains("HumidityGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					humidityGood.add(part);
 				}
 			}
 			if(line.contains("HumidityBad")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					humidityBad.add(part);
 				}
 			}
 		///// Humidity ///// Humidity ///// Humidity ///// Humidity ///// Humidity ///// Humidity 
 			
 			//=======================================================================================\\
 
 		///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt 
 			if(line.contains("TiltGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					tiltGood.add(part);
 				}
 			}
 			if(line.contains("TiltBad")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					tiltBad.add(part);
 				}
 			}
 		///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt ///// Tilt 
 			
 		//=======================================================================================\\
 	
 		///// Light ///// Light ///// Light ///// Light ///// Light ///// Light ///// Light ///// Light 
 			if(line.contains("LightGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					lightGood.add(part);
 				}
 			}
 			if(line.contains("LightBad")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					lightBad.add(part);
 				}
 			}
 		///// Light ///// Light ///// Light ///// Light ///// Light ///// Light ///// Light ///// Light 
 		//=======================================================================================\\
 		///// Sound ///// Sound ///// Sound ///// Sound ///// Sound ///// Sound ///// Sound 
 			if(line.contains("SoundGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					soundGood.add(part);
 				}
 			}
 			if(line.contains("SoundBad")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					soundBad.add(part);
 				}
 			}
 		///// Sound ///// Sound ///// Sound ///// Sound ///// Sound ///// Sound ///// Sound 
 		//=======================================================================================\\
 		///// Misc ///// Misc ///// Misc ///// Misc ///// Misc ///// Misc ///// Misc ///// Misc 
 			if(line.contains("MiscGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					miscGood.add(part);
 				}
 			}
 			if(line.contains("MiscGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					miscBad.add(part);
 				}
 			}
 		///// Misc ///// Misc ///// Misc ///// Misc ///// Misc ///// Misc ///// Misc ///// Misc 
 		//=======================================================================================\\
 		///// Moisture ///// Moisture ///// Moisture ///// Moisture ///// Moisture ///// Moisture 
 			if(line.contains("MoistureGood")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					moistureGood.add(part);
 				}
 			}
 			if(line.contains("MoistureBad")){
 				String[] parts = line.split("\t");
 				for (String part : parts) {
 					moistureBad.add(part);
 				}
 			}
 		///// Moisture ///// Moisture ///// Moisture ///// Moisture ///// Moisture ///// Moisture 
 			
 
 		    //System.out.println(line);
 		}
 		//For each \n, create a new list with name of first element in the array.
 		
 		//GRAB DATA (WITH KNOWN THRESHOLDS)
 		
 		//Analyze data (check thresholds)
 		
 		//Check all boolean thresholds
 		//Add appropriate list to master list
 		
 		//randomly select from master list
 		
 		//send tweet.
 		
 //		System.out.println(weatherGood.toString());
 //		System.out.println(weatherGood.toString());
 		System.out.println(temperatureGood.toString());
 //		System.out.println(temperatureBad.toString());
 //		System.out.println(humidityGood.toString());
 //		System.out.println(humidityBad.toString());
 //		System.out.println(tiltGood.toString());
 //		System.out.println(tiltBad.toString());
 //		System.out.println(lightGood.toString());
 //		System.out.println(lightBad.toString());
 //		System.out.println(soundGood.toString());
 //		System.out.println(soundBad.toString());
 //		System.out.println(miscGood.toString());
 //		System.out.println(miscBad.toString());
 //		System.out.println(moistureGood.toString());
 //		System.out.println(moistureBad.toString());
 
 		System.out.println(temperatureGood.get(1).toString());
 
 		//Scanner scan = new Scanner(System.in);
 		
 		
 		
 		
 	}
 
 }
