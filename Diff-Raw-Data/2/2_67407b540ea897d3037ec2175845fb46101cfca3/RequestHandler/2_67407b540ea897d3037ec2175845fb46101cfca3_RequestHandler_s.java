 /*
  *  McDesktopInfo - A Bukkit plugin + Windows Sidebar Gadget
  *  Copyright (C) 2012  Damarus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.damarus.mcdesktopinfo;
 
 import java.util.HashMap;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 public class RequestHandler {
 
     public Server server;
     private HashMap<String, String> values;
 
     public RequestHandler(Server server) {
         this.server = server;
 
         values = new HashMap<String, String>();
 
         updateValues();
     }
 
     public void updateValues() {
         values.put("playerCount", server.getOnlinePlayers().length + " / " + server.getMaxPlayers());
        values.put("serverName", server.getName()); // AFAIK it always returns "craftbukkit"
         values.put("serverVersion", server.getVersion());
         values.put("pluginVersion", server.getPluginManager().getPlugin(Config.PLUGIN_NAME).getDescription().getVersion());
     }
 
     public String get(String request, HashMap<String, String> params) {
         // Handle more complex requests
         if(request.equals("kick")) doKick(params);
         if(request.equals("playerList")) return getPlayerList(params);
 
         // Handle simpler requests
         if(values.containsKey(request)) return values.get(request);
 
         // ...else return an empty string
         return "";
     }
 
     public boolean doKick(HashMap<String, String> params) {
         if(params.get("adminPw") == null) params.put("adminPw", "");
         
         // Report to serverlog that a kick was requested
         McDesktopInfo.log("The IP " + params.get("gadgetIp") + " requested to kick the player " + params.get("player") +
             " using the password " + params.get("adminPw"));
 
         if(PasswordSystem.checkAdminPW(params.get("adminPw"))) {
             Player player = server.getPlayer(params.get("player"));
             if(player != null) {
                 player.kickPlayer("Kicked with McDesktopInfo");
                 return true;
             }
         }
         return false;
     }
 
     public String getPlayerList(HashMap<String, String> params) {
         Player[] players = server.getOnlinePlayers();
 
         String output = "";
         for(Player p : players) {
             output += "+" + p.getName();
         }
 
         output = output.replaceFirst("[+]", "");
         return output;
     }
 }
