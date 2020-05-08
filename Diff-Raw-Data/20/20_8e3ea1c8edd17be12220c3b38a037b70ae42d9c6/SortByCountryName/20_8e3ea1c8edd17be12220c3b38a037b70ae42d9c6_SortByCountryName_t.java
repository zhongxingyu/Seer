 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pcricketstats;
 
 import java.util.Comparator;
 
 /**
  * This is the Comparator class, which is used to sort a list of players 
  * by their country name.
  * 
  * @author Rob Attfield
  */
 public class SortByCountryName implements Comparator<Player>  {
 
     /**
      * This is the compare method, which is from
      * the implementation of the Comparator class. 
      * 
      * @param p1 The first player to compare
      * @param p2 The second player to compare
      * @return An integer value, which determines whether the first player's 
      * country name is alphabetically greater than, equal to, or 
      * less than the country name of the second player.
      */
     @Override
     public int compare(Player p1, Player p2) {
         
         if(p1.getCountryName() != null && p2.getCountryName() != null)
         {
             //if the two country names are not equal
             if(p1.getCountryName().compareTo(p2.getCountryName()) != 0)
             {
                 return p1.getCountryName().compareTo(p2.getCountryName());
             }
             else
             {
                 //Since there will be players from the same country, 
                 //this sorts the players by name. The implementation below
                 //sorts each player by their last name - using the 
                 //the "space" as a delimiter, the substring and indexOf 
                 //string methods.
                return p1.getPlayerName().substring(p1.getPlayerName().indexOf(" ") + 1).compareTo(p2.getPlayerName().substring(p2.getPlayerName().indexOf(" ") + 1));
             }
         }
         else
         {
             return -1;
         }
     }
     
 }
