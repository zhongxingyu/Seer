 package com.ecnmelog.app;
 
 
 /**
  * Classe de définition des containers
  * @author Clément Delafargue
  */
 public class Container
 {
 	/** 
 	 * Type du container.
 	 * O pour un container lambda
 	 * 1 pour un container surtarifé
 	 * 2 pour un container frigorifique
 	 */
 	protected int type = 0;
 	
 	/** Initialise un container lambda*/
	public Container(){}
 	
 	/**
 	 * Initialise un container du type demandé
 	 * @param type Le type de container
 	 */
 	public Container(int type){
 		this.type = type;
 	}
 	
 	/**
 	 * Renvoie le type du container
 	 * @return 0: lambda, 1: surtarifé, 2: frigorifique
 	 */
 	public int getType(){
 		return this.type;
 	}
 	
 	/**
 	 * Renvoie le container en ASCII Art
 	 */
 	public String toString(){
 		String retour = new String();
 		switch(this.type){
 			case 2:
 				retour = "[__F__]";
 				break;
 			case 1:
 				retour = "[__P__]";
 				break;
 			default:
 				retour = "[_____]";
 				break;
 				
 			}
 		return retour;
 	}
 }
