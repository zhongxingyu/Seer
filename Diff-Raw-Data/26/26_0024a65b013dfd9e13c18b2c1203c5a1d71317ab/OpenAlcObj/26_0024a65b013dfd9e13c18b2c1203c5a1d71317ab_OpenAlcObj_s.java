 package com.github.dreadslicer.tekkitrestrict.objects;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 import ee.AlchemyBagData;
 
 public class OpenAlcObj {
 	private AlchemyBagData bag;
 	private Player bagOwner;
 	private Player viewer;
 	private String viewerName;
 	private String bagOwnerName;
 	private static ArrayList<OpenAlcObj> allOpenAlcs = new ArrayList<OpenAlcObj>();
 	
 	public OpenAlcObj(AlchemyBagData bag, Player bagOwner, Player viewer){
 		this.bag = bag;
 		this.bagOwner = bagOwner;
 		this.bagOwnerName = bagOwner.getName();
 		this.viewer = viewer;
 		this.viewerName = viewer.getName();
 		
 		allOpenAlcs.add(this);
 	}
 	
 	public String getBagOwnerName(){
 		return bagOwnerName;
 	}
 	public String getViewerName(){
 		return viewerName;
 	}
 	public Player getViewer(){
 		return viewer;
 	}
 	public Player getBagOwner(){
 		return bagOwner;
 	}
 	public AlchemyBagData getBag(){
 		return bag;
 	}
 	
 	public static OpenAlcObj getOpenAlcByOwner(String owner){
 		for (OpenAlcObj current : allOpenAlcs){
 			if (current.bagOwnerName.equalsIgnoreCase(owner)) return current;
 		}
 		return null;
 	}
 	
 	public static OpenAlcObj getOpenAlcByViewer(String viewer){
 		for (OpenAlcObj current : allOpenAlcs){
 			if (current.viewerName.equalsIgnoreCase(viewer)) return current;
 		}
 		return null;
 	}
 	
 	public static boolean isViewing(String player){
 		if (getOpenAlcByViewer(player) != null) return true;
 		return false;
 	}
 	
 	public static boolean isViewed(String player){
 		if (getOpenAlcByOwner(player) != null) return true;
 		return false;
 	}
 }
