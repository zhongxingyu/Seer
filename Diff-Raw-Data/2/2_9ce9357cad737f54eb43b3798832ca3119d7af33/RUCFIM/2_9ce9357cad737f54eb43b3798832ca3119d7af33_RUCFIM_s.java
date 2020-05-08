 package com.aerodynelabs.habtk.atmosphere;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 
 public class RUCFIM implements AtmosphereSource {
 
 	@Override
 	public File getAtmosphere(int time, double lat, double lon) {
 		int startTime = (time / 10800) * 10800;
 		double rlat = (lat / 0.5) * 0.5;
 		double rlon = (lon / 0.5) * 0.5;
 		String address = "http://rucsoundings.noaa.gov/get_soundings.cgi?data_source=FIM;airport=" +
 				rlat + "," + rlon + ";hydrometeors=false&startSecs=" +startTime +
				"&endSecs=" + (time+1);
 		
 		URL url;
 		InputStream is = null;
 		InputStreamReader isr = null;
 		try {
 			url = new URL(address);
 			is = url.openStream();
 		} catch(MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		} catch(IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		isr = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isr);
 		
 		File file = new File("wind/" + (int)(rlat*10) + "_" + (int)(rlon*10) + "_" + startTime + ".gsd");
 		
 		FileWriter fw;
 		try {
 			fw = new FileWriter(file);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			return null;
 		}
 		PrintWriter writer = new PrintWriter(new BufferedWriter(fw));
 		
 		String line = null;
 		try {
 			while((line = br.readLine()) != null) {
 				writer.println(line);
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		writer.flush();
 		return file;
 	}
 
 }
