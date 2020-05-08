 package com.vartala.soulofw0lf.rpgapi.chatapi;
 
 import com.vartala.soulofw0lf.rpgapi.RpgAPI;
 import com.vartala.soulofw0lf.rpgapi.playerapi.RpgPlayer;
 import com.vartala.soulofw0lf.rpgapi.util.ChatColors;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import java.util.regex.Pattern;
 
 
 /**
  * Created by: soulofw0lf
  * Date: 6/26/13
  * Time: 7:27 PM
  * <p/>
  * This file is part of the Rpg Suite Created by Soulofw0lf and Linksy.
  * <p/>
  * The Rpg Suite is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * The Rpg Suite is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with The Rpg Suite Plugin you have downloaded.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class ChatProcessor {
     public static String TitleString (String s, String name){
         Player p = Bukkit.getPlayer(name);
         RpgPlayer rp = RpgAPI.rpgPlayers.get(RpgAPI.activeNicks.get(name));
         String q = ChatColors.ChatString(s);
         String u = q.replaceAll("@World", p.getWorld().getName());
         if (RpgAPI.guildsOn){
             if (rp.isInGuild()){
                 u = u.replaceAll("@Guild", RpgAPI.guilds.get(rp.getGuild()).getDisplayName());
             } else {
                u = u.replace("@Guild", "");
             }
         }
         String w = u;
         if (RpgAPI.achievementsOn){
             if (!(rp.getActivePrefix().equalsIgnoreCase("None"))){
                 w = w.replaceAll("@Prefix", rp.getActivePrefix());
             } else {
                 w = w.replaceAll("@Prefix", "");
             }
             if (!(rp.getActiveSuffix().equalsIgnoreCase("None"))){
                 w = w.replaceAll("@Suffix", "");
             }else{
                 w = w.replaceAll(" @Suffix","");
             }
         }
         String t = w.replaceAll("@Pname", RpgAPI.playerColors.get(name) + RpgAPI.activeNicks.get(name) + "&F");
         if (RpgAPI.chatOn){
             t = t.replaceAll("@Channel", rp.getActiveChannel());
         }
         String r = ChatColors.ChatString(t);
        String m = r.replace("[]", "");
      return m + ": ";
     }
 }
