 package fr.iutvalence.java.mp.navalbattle;
 
 /**
  * Class which represents a naval battle game
  */
 public class NavalBattle
 {    
     /**
      * table containing the boats of Player1
      */
     private Boat[] BoatP1; 
     
     /**
      * table containing the boats of Player2
      */
     private Boat[] BoatP2; 
     
     /** 
      * launches the game
      * currently, displays a grid to see current boat positions
      */
     public void play()
     {
         int i, j;
         for (i=0; i<10; i++)
         {
             for(j=0; j<10;j++)
             {
                 if(checkPosBoat(BoatP1, i, j))
                     System.out.print("O ");
                 else
                    System.out.print("¤ ");
             }
             System.out.println("");
         }
     }
     
     /**
      * Initialises manually some boats for the Player1
      */
     public NavalBattle()
    {
         this.BoatP1 = new Boat[3];
         
         Case[] boat1Cases = new Case[2];
         boat1Cases[0] = new Case(2,3);
         boat1Cases[1] = new Case(2,4);
         this.BoatP1[0] = new Boat(boat1Cases);
         
         Case[] boat2Cases = new Case[3];
         boat2Cases[0] = new Case(5,5);
         boat2Cases[1] = new Case(6,5);
         boat2Cases[2] = new Case(7,5);
         this.BoatP1[1] = new Boat(boat2Cases);
         
         Case[] boat3Cases = new Case[4];
         boat3Cases[0] = new Case(4,9);
         boat3Cases[1] = new Case(5,9);
         boat3Cases[2] = new Case(6,9);
         boat3Cases[3] = new Case(7,9);
         this.BoatP1[2] = new Boat(boat3Cases);
     }
     
     /**
      * checks if there is a boat for a specific Player on a given position
      * @param PlayerBoats : table containing the boats of a player
      * @param Xin : x position to check
      * @param Yin : y position to check
      * @return boolean : true if there is a boat, or false
      */
     public boolean checkPosBoat(Boat[] PlayerBoats, int Xin, int Yin)
     {
         boolean res = false;
         int i;
         for(i = 0; i < PlayerBoats.length; i++)
             res = res || PlayerBoats[i].isFull(Xin, Yin);
         return res;
     }
     
 }
