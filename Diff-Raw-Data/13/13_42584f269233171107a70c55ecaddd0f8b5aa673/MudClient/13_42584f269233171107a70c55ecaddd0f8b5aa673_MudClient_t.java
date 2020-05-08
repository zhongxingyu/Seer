 package cs3517.hale.mud;
 
 import java.rmi.*;
 import java.util.*;
 import javax.naming.*;
 
 /**
  * The MUD game client, which invokes a remote method.
  */
 public class MudClient
 {
   public static void main (String[] args) throws NamingException, RemoteException
   {
     Context namingContext = new InitialContext();
 
     String url = "rmi://localhost/mud_game_mud_manager";
     MudManager mudManager = (MudManager) namingContext.lookup( url );
 
     if (question("Create a new MUD? (y/N)").equals("y"))
       mudManager.makeMud( question("Type a unique name for your MUD") );
 
     System.out.println( mudManager.printableMudList() );
     String mudString = question("Pick a MUD game from the list above");
 
     Mud mudGame = mudManager.getGame( mudString );
 
     String loc = mudGame.startLocation();
 
     String player = question("What is your name?");
     for ( ; !mudGame.addPlayer(loc, player) ; player = question("What is your name?"))
       System.out.println("That name has already been taken.");
 
     /** Game Loop */
     for (String answer = question(); !answer.equals("exit"); answer=question())
     {
       if (answer.equals("help"))
       {
         System.out.println("Type one of the following commands at the prompt");
         System.out.println("\tlook\t\tShow a description of the current location.");
         System.out.println("\tnorth\t\tMove north");
         System.out.println("\teast\t\tMove east");
         System.out.println("\tsouth\t\tMove south");
         System.out.println("\twest\t\tMove west");
         System.out.println("\texit\t\tQuit the game");
         System.out.println("\tpickup <thing>\tAdd an item in the room to your inventory.");
         System.out.println("\tinventory\tView items you have picked up.");
       }
       else if (answer.equals("look"))
       {
         System.out.println(mudGame.locationInfo( loc ));
       }
       else if (answer.equals("north") || answer.equals("east")
           || answer.equals("south") || answer.equals("west"))
       {
         String newLoc = mudGame.moveThing( loc, answer, player );
        if (newLoc.equals(loc)) // location unchanged
           System.out.println("Can't do that.");
         else {
           loc = newLoc;
           System.out.println( mudGame.locationInfo( loc ));
         }
       }
       else if (answer.equals("inventory"))
       {
         for ( String item : mudGame.getInventory( player ))
           System.out.println( item );
       }
       else // currently, must be a 'pickup' command
       {
         String[] ansParts = answer.split("\\s+");
         if (ansParts.length != 2) continue;
         if (ansParts[0].equals("pickup"))
         {
           System.out.println( mudGame.pickupItem( player, ansParts[1], loc ));
         }
       }
     }
     mudGame.delPlayer( loc, player );
   }
 
   private static String question()
   {
     return question("Type an action, or 'help' for help");
   }
 
   private static String question(String question)
   {
     Scanner userIn = new Scanner( System.in );
     System.out.println(question);
     System.out.print(">> ");
     String answer = userIn.nextLine();
     return answer;
   }
 }
