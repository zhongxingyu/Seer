 package fr.iutvalence.java.mp.navalbattle;
 
 /**
  * Represents each boat
  * @author barattoh
  *
  */
 public class Boat
 {
     /**
      * Table which contains every positions of the boat on the grid
      */
     private Case[] boatCases;
     
    /** 
      * Table which contains the state of each cases of the boat
      * False : not touched yet, True : touched
      */
     public boolean[] touchedCases;
 
     /**
      * Makes the boat : initializes boat positions, and length
      * @param boatCasesIn : table containing the boat coordinates
      */
     public Boat(Case[] boatCasesIn)
     {
        int i;
        this.boatCases = boatCasesIn;
        // TODO (FIXED) the following array is allocated but not initialized properly
        this.touchedCases = new boolean[boatCasesIn.length];
        for (i = 0; i < boatCasesIn.length; i++)
            this.touchedCases[i] = false;
     } 
     
     /**
      * Method that simply returns the position of the boat
      * @return : Table containing all boat positions
      */
     public Case[] getPosition()
     {
         return this.boatCases;
     }
     
     /**
      * method to check if the boat is concerned by a specific coordinate
      * @param Xin : X coord
      * @param Yin : Y coord
      * @return : true if the boat is present on these coordinates, false if it isn't 
      */
     public boolean isFull(int Xin,int Yin)
     {
         boolean res = false;
         int i;
         for(i = 0; i < ( this.boatCases.length); i++)
             res = res || (this.boatCases[i].getX() == Xin && this.boatCases[i].getY() == Yin);
         return res;
     }
 }
