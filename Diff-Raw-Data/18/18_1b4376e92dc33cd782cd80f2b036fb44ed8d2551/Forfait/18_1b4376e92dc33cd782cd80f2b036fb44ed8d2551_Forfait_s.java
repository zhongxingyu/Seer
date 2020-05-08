 package ca.etsmtl.log660.entity;
 
 public class Forfait {
 	private int id;
 	private String nom;
	private float cout;
	private int locationMax;
	private int duree;
 	
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getNom() {
 		return nom;
 	}
 	public void setNom(String nom) {
 		this.nom = nom;
 	}
 	public float getCout() {
 		return cout;
 	}
 	public void setCout(float cout) {
 		this.cout = cout;
 	}
 	public int getLocationMax() {
 		return locationMax;
 	}
 	public void setLocationMax(int locationMax) {
 		this.locationMax = locationMax;
 	}
 	public int getDuree() {
 		return duree;
 	}
 	public void setDuree(int duree) {
 		this.duree = duree;
 	}
 }
