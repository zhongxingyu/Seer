 package com.stalkindustries.main.game;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.stalkindustries.main.TheStreet;
 
 public class Ressources {
 	
 	private static ArrayList<ArrayList<String>> location_ids;
 	
 	public static final int RASTERHEIGHT = 45; //Map Raster
 	public static final int TORSOCNT =4; //Anzahl verschiedener Torsos in Sprite Grafik
 	public static final int GAMESPEED = 40; // Zeitspanne in der eine Berechnung durchgefrt wird
 	
 	 
 	 
 	static{
 		//Initialisierung der location_ids
 		//TODO richtigen Dateipfad spter angeben
		location_ids=read_from_csv("C:/Users/Martika/Desktop/Dropbox/Software Engineering/Grafikdesign/Fertig/russland_map.csv");
 		
 	}
 	
 	
 	//Beschwerden an Sven und Miri
 		public static ArrayList<ArrayList<String>> read_from_csv(String dateiName){
 			ArrayList<ArrayList<String>> list_of_lists = new ArrayList<ArrayList<String>>();
 			
 			//File vorhanden?
 			File file = new File(dateiName);
 	        if (!file.canRead() || !file.isFile()){
 	        	System.out.print("No file found.");
 	            System.exit(0);
 	        }
 	        
 	        try {
 	            BufferedReader in = new BufferedReader(new FileReader(dateiName));
 	            String zeile = null;
 	            ArrayList<String> tmp;
 	            while ((zeile = in.readLine()) != null) {
 	            	tmp = new ArrayList<String>();
 	            	for(int i=0;i<zeile.split(";").length;i++){
 	            		tmp.add(zeile.split(";")[i]);	//Spalte hinzufgen
 	            	}
 	            	list_of_lists.add(tmp);	//Zeile hinzufgen
 	            }
 	            in.close();
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        } 
 			
 			return list_of_lists;
 		}
 	
 		
 	public static ArrayList<ArrayList<String>> getLocation_ids() {
 		return location_ids;
 	}
 	
 }
