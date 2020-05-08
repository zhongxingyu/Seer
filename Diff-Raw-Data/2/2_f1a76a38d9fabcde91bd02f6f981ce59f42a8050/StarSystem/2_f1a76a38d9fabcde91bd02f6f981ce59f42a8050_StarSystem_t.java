 package edu.gatech.cs2340.risk.model;
 
 import java.util.*;
 
 /**
  * Class that defines a StarSystem - each contains 5 planets
  * @author Team 8
  */
 
 public class StarSystem {
     ArrayList<Planet> planets;
     private Player owner;
 
     /**
     * Defines that a Star Sytem has an owner that is a Player it takes in
     * @param Player that owns all the planets in the Star System initially
     */
 
     public StarSystem(Player owner){
     	this.owner = owner;
 		planets = new ArrayList<Planet>(5);
 		for (Planet planet : planets) {
             planet = new Planet(owner, owner.getFleets());
         }
     }
 
     /**
     * Checks to see if every planet in this Star System has an owner
     * @param Boolean returns true if every planet in the Star System has the same owner
     */
 
     public boolean hasOwner(){
     	for(Planet current: planets){
     		if(!current.getOwner().getName().equals(owner.getName())){
     			return false;
     		}
     	}
     	return true;
     }
 
     /**
     * If this Star System has an owner, set's the new owner to the paramater passed in
     * @param Player the new owner of the Star SYstem
     */
 
     public void newOwner(Player newOwner){
    	if(hasOwner()){
     		owner = newOwner;
     	}
     }
 
     /**
     * Returns the owner of the Star System
     * @return Player the current owner of the star system
     */
 
     public Player getOwner(){
     	return owner;
     }
 
 
 
 
 
 }
