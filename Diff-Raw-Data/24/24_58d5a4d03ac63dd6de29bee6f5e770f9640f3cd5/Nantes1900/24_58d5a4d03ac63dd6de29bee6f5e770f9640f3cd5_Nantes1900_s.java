 /**
  * 
  */
 package fr.nantes1900;
 
 import fr.nantes1900.control.GlobalController;
 
 /**
 * Implements a main class. Launch the UI.
  * @author Nicolas Bouillon, Camille Bouquet, Luc Jallerat, Daniel Lefevre, Siju
  *         Wu
  */
 public final class Nantes1900
 {
 
     /**
      * Private constructor.
      */
     private Nantes1900()
     {
     }
 
     /**
      * Main function.
      * @param args
      *            arguments (the program does not take them into account)
      */
     @SuppressWarnings("unused")
     public static void main(final String[] args)
     {
         new GlobalController();
     }
 }
