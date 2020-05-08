 package com.kickthebeat.andysiena.pokedex;
 
 import java.util.ArrayList;
 
 import android.net.Uri;
 
 public class Pokemon {
 	
 	private final String BASEPATH = "android.resource://com.kickthebeat.andysiena.pokedex/drawable/";
 	
 	int number; // universal pokedex number
 	String name;
 	String[] types; // make this an enum later I guess
 	String species; //ex. Lightning Pokemon
 	String info;
 	String evoData;
 	
 	public String getSpecies() {
 		return species;
 	}
 
 	public void setSpecies(String species) {
 		this.species = species;
 	}
 
 	public String getEvoData() {
 		return evoData;
 	}
 
 	public void setEvoData(String evoData) {
 		this.evoData = evoData;
 	}
 
 	public String getInfo() {
 		return info;
 	}
 
 	public void setInfo(String info) {
 		this.info = info;
 	}
 
 	ArrayList<String> moves; // make a Move class/struct for levels and types
 
 	public int getNumber() {
 		return number;
 	}
 
 	public void setNumber(int number) {
 		this.number = number;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String[] getTypes() {
 		return types;
 	}
 
 	public void setTypes(String[] types) {
 		this.types = types;
 	}
 
 	public Uri getImageUri() {
 		String path = BASEPATH + "pk_" + getNumber();
 		return Uri.parse(path);
 	}
 
 	public ArrayList<String> getMoves() {
 		return moves;
 	}
 
 	public void setMoves(ArrayList<String> moves) {
 		this.moves = moves;
 	}
 	
 	
 }
