 package com.ecnmelog.app;
 
 import java.util.*;
 
 /**
  * Classe du système de stockage de container
  * @author Clément Delafargue
  */
 public class Stockage
 {
 	/** Containers en attente d'être stockés*/
 	protected Vector<Container> attente;
 	
 	/** Containers stockés*/
 	protected Vector<Container> stockage;
 	
 	/** Contenance de l'espace de stockage*/
 	protected int contenance;
 	
 	/** Nombre d'emplacements frigorifiques disponibles */
 	protected int nbEmplacementsFrigoDispo;
 	
 	/** Nombre d'empacements sur-tarifés disponibles */
 	protected int nbEmplacementsSurtarifesDispo;
 	
 	/** Nombre d'empacements normaux disponibles */
 	protected int nbEmplacementsNormauxDispo;
 	
 	
 	/**
 	 * Initialise un espace de stockage 
 	 * @param contenance Contenance de la zone de stockage
 	 */
 	public Stockage(int contenance){
 		// Initialisation des espaces de stockage
		this.attente = new Vector<Container>();
		this.stockage = new Vector<Container>();
 		
 		// Initialisation des nombres d'emplacements libres
 		this.contenance = contenance;
 		
 		// Un emplacement sur 20 est frigorifique
 		this.nbEmplacementsFrigoDispo = (int) contenance / 20;
 		
 		// 65% des emplacements sont sur-tarifés
 		this.nbEmplacementsSurtarifesDispo = (int) (this.contenance * 0.65);
 		
 		// Le reste est normal
 		this.nbEmplacementsNormauxDispo = this.contenance - this.nbEmplacementsFrigoDispo - this.nbEmplacementsSurtarifesDispo;
 	}
 	
 	/**
 	 * Renvoie le nombre d'emplacements libres (tous types confondus) 
 	 * @return Nombre d'emplacements libres
 	 */
 	public int getNbEmplacementsDispo(){
 		return this.nbEmplacementsNormauxDispo + this.nbEmplacementsFrigoDispo + this.nbEmplacementsSurtarifesDispo;
 	}
 	
 	/**
 	 * Renvoie le nombre d'emplacements normaux libres
 	 * @return Nombre d'emplacements libres
 	 */
 	public int getNbEmplacementsNormauxDispo(){
 		return this.nbEmplacementsNormauxDispo;
 	}
 	
 	/**
 	 * Renvoie le nombre d'emplacements frigorifiques libres 
 	 * @return Nombre d'emplacements libres
 	 */
 	public int getNbEmplacementsFrigoDispo(){
 		return this.nbEmplacementsFrigoDispo;
 	}
 	
 	/**
 	 * Renvoie le nombre d'emplacements frigorifiques libres 
 	 * @return Nombre d'emplacements libres
 	 */
 	public int getNbEmplacementsSurtarifesDispo(){
 		return this.nbEmplacementsSurtarifesDispo;
 	}
 	
 	
 	/**
 	 * Ajoute un container à la zone d'attente
 	 * @param container Le container à ajouter à la zone d'attente
 	 */
 	public void addContainer(Container container){
		this.attente.addElement(container);
 	}
 	
 	/**
 	 * Traite les containers en attente. Stocke les containers qui peuvent l'être, laisse les autres dans la zone de stockage
 	 */
 	public void traiterAttente(){
 		// C'est là qu'est le gros du travail ;-)
 	}
 	
 	/**
 	 * Retire un container de la zone de stockage (on retire préférentiellement les containers placés sur des emplacements non prévus pour eux)
 	 * @param type Type du container à enlever.
 	 */
 	public void removeContainer(int type){
 		
 	}
 }
