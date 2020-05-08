 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver;
 
 import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Defines the functions for writing the statistics to the database.  The interface allows the user to access the data 
  * without having to know the underlying database structure
  * @author etsai
  */
 public interface DataWriter {
     /**
      * Tuple storing steam community information
      * @author etsai
      */
     public static class SteamInfo {
         /** Player's unique steamID64 */
         public final String steamID64;
         /** Steam community name */
         public final String name;
         /** Steam community avatar */
         public final String avatar;
         
         /**
          * Constructs a SteamInfo tuple
          * @param steamID64 Player's unique steamID64
          * @param name Steam community name
          * @param avatar Steam community avatar
          */
         public SteamInfo(String steamID64, String name, String avatar) {
             this.steamID64= steamID64;
             this.name= name;
             this.avatar= avatar;
         }
        @Override
        public String toString() {
            return String.format("[%s, %s, %s]", steamID64, name, avatar);
        }
     }
     /**
      * Get the steamID64 of records that do not have any steam community information stored in the database
      * @return List of steamID64 that do not have steam community info
      */
     public List<String> getMissingSteamInfoIDs();
     /**
      * Write the collection of steam community information to the database.  This version 
      * of the function writes a collection of SteamInfo tuples
      * @param steamInfo Collection of steam community information to write
      */
     public void writeSteamInfo(Collection<SteamInfo> steamInfo);
     /**
      * Write the steam community information to the database.  This version writes only one 
      * SteamInfo tuple
      * @param steamInfo Tuple storing steam community information
      */
     public void writeSteamInfo(SteamInfo steamInfo);
     /**
      * Write the match information in the packet to the database
      * @param packet Packet containing the match information
      */
     public void writeMatchData(MatchPacket packet);
     /**
      * Write the player statistics to the database
      * @param content Completed player statistics content
      */
     public void writePlayerData(PlayerContent content);
 }
