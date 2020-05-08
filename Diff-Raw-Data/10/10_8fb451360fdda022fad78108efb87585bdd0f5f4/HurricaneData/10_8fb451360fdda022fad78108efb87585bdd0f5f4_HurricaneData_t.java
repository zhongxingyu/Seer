 package codeTest;
 
 import java.util.Scanner;
 
 public class HurricaneData {
 
 	private String HurricaneName = "";
 	private int MxW = 0, MnP = Integer.MAX_VALUE;
 	
 	public boolean isInteger(String s) {
 		try {
	        	Integer.parseInt(s); 
	    	} catch(NumberFormatException e) { 
	        	return false; 
		}
	    		return true;
 	}
 	
 	public double convertFromKnotsToKMpH (double knots) {
 		return 1.852*knots;
 	}
 	
 	public void printWeatherData() {
 		System.out.println("Storm name: " + HurricaneName + ". Maximum sustained wind: " + convertFromKnotsToKMpH(MxW) + " km/h. Minimum pressure: " + MnP + " mbar.");
 	}
 	
 	public void procWeatherData(String s) {
 		String[] weatherData = s.split(",");
 		if (weatherData.length < 4) {
 			if (!HurricaneName.isEmpty()) {
 				printWeatherData();
 				MxW = 0;
 				MnP = Integer.MAX_VALUE;
 			} 
 			HurricaneName = weatherData[1].trim();
 		} else {
 				int windspeed = 0;
 				int pressure = Integer.MAX_VALUE;
 				if (isInteger(weatherData[6].trim())) {
 					windspeed = Integer.parseInt(weatherData[6].trim());
 				}
 				if (windspeed > MxW) {
 					MxW = windspeed;
 				}
 				if (isInteger(weatherData[7].trim())) {
 					pressure = Integer.parseInt(weatherData[7].trim());
 				}
 				if (pressure < MnP) {
 					MnP = pressure;
 				}
 		}
 	}
 	
 	public void readWeatherData() throws Exception {
 		java.io.File file = new java.io.File("hurdat2-nencpac-1949-2012-040513.txt");
 		Scanner input = new Scanner(file);
 		while (input.hasNext()) {
 			String line = input.nextLine();
 			if (line.regionMatches(4, "2009", 0, 4) || line.startsWith("2009")) {
 				if (line.regionMatches(4, "2010", 0, 4)) {
 					break;
 				} else
 					procWeatherData(line);
 			}
 		}
 		input.close();
 	}
 	public static void main(String[] args) throws Exception {
 		HurricaneData hd = new HurricaneData();
 		hd.readWeatherData();
 	}
 
 }
