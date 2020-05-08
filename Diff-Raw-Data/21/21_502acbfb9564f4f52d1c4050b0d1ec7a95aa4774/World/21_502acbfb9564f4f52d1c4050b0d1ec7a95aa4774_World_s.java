 package com.teatime.game.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.teatime.game.model.com.Orders;
 
 public class World {
 	
 	private List<Tribe> tribes;
 	private List<Province> provinces;
 	private static World instance;
 	
 	private World (List<Tribe> tribes, List<Province> provinces) {
 		this.tribes = tribes;
 		this.provinces = provinces;
 	}
 	
 	/*
 	 * The world will consist of size * size provinces
 	 * and a tribe put randomly at one of the provinces
 	 */
 	public static World createWorld(int size) {
 		
 		// Provinces
 		List<Province> newProvinces = new ArrayList<Province>();
 		int nrOfProvinces = size * size;
 		
 		Province newProvince;
 		for(int i = 0; i < nrOfProvinces; i++) {
 			newProvince = new Province();
 			newProvinces.add(newProvince);
 		}
 		
 		// Tribes
 		List<Tribe> newTribes = new ArrayList<Tribe>();
 		
 		Random r = new Random();
 		int homeProvinceIndex = r.nextInt(size);
 		Province homeProvince = newProvinces.get(homeProvinceIndex);
 		
 		// Create a single tribe
		Tribe newTribe = new Tribe(newProvinces, homeProvince, "myTribe");
 		newTribes.add(newTribe);
 		
 		instance = new World(newTribes, newProvinces);
 		return instance;
 	}
 	
 	public static World createWorld() {
 		
 		//Generate provinces
 		List<Province> newProvinces = new ArrayList<Province>();
 		//newProvinces.add(object);
 		
 		//Generate tribes
 		List<Tribe> newTribes = new ArrayList<Tribe>();
 		//newTribes.add(arg0)
 		
 		instance = new World(newTribes, newProvinces);
 		
 		return instance; 
 	}
 	
 	public static World getWorld() {
 		if (instance != null) {
 			return instance;
 		} else {
 			//TODO Raise exception
 			return null;
 		}
 	}
 	
 	public List<Province> getProvinces() {
 		return provinces;
 	}
 	
 	public void simulateWorld(Orders orders) {
 		for ( Tribe t : tribes) {
 			t.simulateNext(orders);
 		}
 	}
 	
 	public Tribe getTribe(String name) {
 		for ( Tribe t : tribes ) {
 			if ( t.getName().equals(name) ) {
 				return t;
 			}
 		}
 		return null;
 	}
 
 }
