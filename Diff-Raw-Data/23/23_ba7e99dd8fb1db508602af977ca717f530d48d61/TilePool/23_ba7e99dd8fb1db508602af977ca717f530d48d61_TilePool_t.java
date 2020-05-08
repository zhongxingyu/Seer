 package edu.aueb.cs.uw.core;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
import android.util.Log;


 public class TilePool {
 	
 	public final static String BONUS_LETTERS="ΘΞΦΧΨ";
 	
 	private Map<Character,Integer> tilesMap;
 	private int numOfTiles;
 	
 	public  TilePool(){
 		this.initializeMap();
 		this.setNumOfTiles(calculateNumOfTiles());
 	}
 	
 	public int calculateNumOfTiles(){
 		int tiles=0;
 		for(int t : tilesMap.values())
 			tiles+=t;
 		return tiles;
 	}
 	
 	public Tile getTile(){
 		Random rnd=new Random();
 		int p=rnd.nextInt(calculateNumOfTiles());
 		for (Map.Entry<Character, Integer> entry : tilesMap.entrySet()) {
 			if (p < entry.getValue()){
 				tilesMap.put(entry.getKey(),entry.getValue()-1);
				numOfTiles=calculateNumOfTiles();
 				return new Tile(entry.getKey());
 			}
 			else p -= entry.getValue();
 		}
 		return null;
 	}
 	
 	public Tile switchTile(Tile tile){
 		tilesMap.put(tile.getLetter(), (tilesMap.get(tile.getLetter())+1));
 		return getTile();
 	}
 
 	public boolean hasMoreTiles(){
 		return (numOfTiles>0)?true:false;
 	}
 	
 	public void setNumOfTiles(int numOfTiles) {
 		this.numOfTiles = numOfTiles;
 	}
 	
 	public int getNumOfTiles(){
 		return this.numOfTiles;
 	}
 	
 	private void initializeMap(){
 		tilesMap=new HashMap<Character, Integer>();
 		tilesMap.put('Α', 11);
 		tilesMap.put('Β', 1);
 		tilesMap.put('Γ', 2);
 		tilesMap.put('Δ', 2);
 		tilesMap.put('Ε', 8);
 		tilesMap.put('Ζ', 1);
 		tilesMap.put('Η', 7);
 		tilesMap.put('Θ', 1);
 		tilesMap.put('Ι', 8);
 		tilesMap.put('Κ', 4);
 		tilesMap.put('Λ', 3);
 		tilesMap.put('Μ', 3);
 		tilesMap.put('Ν', 6);
 		tilesMap.put('Ξ', 1);
 		tilesMap.put('Ο', 8);
 		tilesMap.put('Π', 4);
 		tilesMap.put('Ρ', 5);
 		tilesMap.put('Σ', 7);
 		tilesMap.put('Τ', 8);
 		tilesMap.put('Υ', 4);
 		tilesMap.put('Φ', 1);
 		tilesMap.put('Χ', 1);
 		tilesMap.put('Ψ', 1);
 		tilesMap.put('Ω', 3);
 	}
 }
