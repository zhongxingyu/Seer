 package codeTest;
 
 import java.util.Scanner;
 
 public class WeatherData {
 	
 	private int TRange = Integer.MAX_VALUE;
 	private int DyIndex, MxTIndex, MnTIndex;
 	private String Day;
 	
 	public boolean isInteger(String s) {
 		try {
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
 	}
 	
 	public String filterString(String s) {
 		if (s.contains("*"))
 			s = s.replace("*", "");
 		return s;
 	}
 	
 	public void procWeatherData(String s) {
 		s = s.replaceAll("\\s+", " ").trim();		/* Replace all whitespace with a single whitespace */
 		String[] weatherData = s.split(" ");
 		int tempRange;
 		
 		/*
 		 * Data column mapping
 		 */
 		for (int i=0; i<weatherData.length; i++) {
 			weatherData[i] = filterString(weatherData[i]);
 			if (weatherData[i].equals("Dy"))
 				DyIndex = i;
 			else if (weatherData[i].equals("MxT"))
 				MxTIndex = i;
 			else if (weatherData[i].equals("MnT"))
 				MnTIndex = i;
 		}
 		if (isInteger(weatherData[MxTIndex]) && isInteger(weatherData[MnTIndex])) {
 			tempRange = Integer.parseInt(weatherData[MxTIndex]) - Integer.parseInt(weatherData[MnTIndex]);
 			if (tempRange < TRange) {
 				TRange = tempRange;
 				Day = weatherData[DyIndex];
 			}
 		}	
 	}
 	
 	public void readWeatherData() throws Exception {
 		java.io.File file = new java.io.File("weather.dat");
 		Scanner input = new Scanner(file);
 		
 		while (input.hasNext()) {
 			if (input.next().equals("<pre>")) {
 				while (input.hasNext()) {
 					String line = input.nextLine();
 					if (!line.isEmpty() && !line.equals("</pre>")) {
 						procWeatherData(line);
 					}
 				}
 			}
 		}
 		input.close();
 	}
 	
 	public static void main(String[] args) throws Exception {
 		WeatherData wd = new WeatherData();
 		wd.readWeatherData();
 		System.out.println("The smallest temperature spread occured on day " + wd.Day + " with a range of " + wd.TRange + ".");	
 	}
 
 }
