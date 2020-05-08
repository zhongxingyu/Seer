 package com.cs2340.spacetrader;
 
 import java.io.Serializable;
import java.util.*;
 
 public class Map implements Serializable
 { 
 	private Planet[] planetArray;
 	
 	public Map()
 	{
 		planetArray = generatePlanetArray();
 	}
 	
 	/**
 	 * Generates all the planets on the map
 	 * @return array of planet objects
 	 */
 	public final Planet[] generatePlanetArray()
 	{
 		Random rand = new Random();
 		PlanetInventory inventory = new PlanetInventory(7);
 		Contract contract = new Contract();
 		planetArray = new Planet[8];
 		
 		int [] coord0 = {rand.nextInt(150),rand.nextInt(150)};
 		int [] coord1 = {rand.nextInt(150),rand.nextInt(150)};
 		int [] coord2 = {rand.nextInt(150),rand.nextInt(150)};
 		int [] coord3 = {rand.nextInt(150),rand.nextInt(150)};
 		int [] coord4 = {rand.nextInt(150),rand.nextInt(150)};
 		int [] coord5 = {rand.nextInt(150),rand.nextInt(150)};
 		int [] coord6 = {rand.nextInt(150),rand.nextInt(150)};
 		int [] coord7 = {rand.nextInt(150),rand.nextInt(150)};
 
 
 		Planet planet0 = new Planet ("Kashyyyk",coord0, inventory, contract, 6);
 		Planet planet1 = new Planet ("Tatooine",coord1,inventory, contract, 3);
 		Planet planet2 = new Planet ("Naboo",coord2,inventory, contract, 1);
 		Planet planet3 = new Planet ("Alderon",coord3,inventory, contract, 2); 
 		Planet planet4 = new Planet ("Xeon",coord4,inventory, contract, 5);
 		Planet planet5 = new Planet ("Zion",coord5,inventory, contract, 4);  
 		Planet planet6 = new Planet ("Mordor",coord6, inventory, contract, 7);
 		Planet planet7 = new Planet ("Krypton",coord7,inventory, contract, 0);
 		
 		planetArray[0] = planet0;
 		planetArray[1] = planet1;
 		planetArray[2] = planet2;
 		planetArray[3] = planet3;
 		planetArray[4] = planet4;
 		planetArray[5] = planet5;
 		planetArray[6] = planet6;
 		planetArray[7] = planet7;
 		return planetArray;
 	}
 	
 	public Planet[] getPlanetArray()
 	{
 		return planetArray;
 	}
 	
 	public Planet getPlanet(String name)
 	{
 		int numel = planetArray.length;
 		for (int i=0;i<numel;i++)
 		{
 			if (planetArray[i].getName().equals(name))
 			{
 				return planetArray[i];		
 			}
 		}
 		return null;
 	}
 }
