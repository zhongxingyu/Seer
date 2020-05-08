 /*
 This file is part of Salesmania.
 
     Salesmania is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Salesmania is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.invisioncraft.plugins.salesmania.worldgroups;
 
 import net.invisioncraft.plugins.salesmania.AuctionQueue;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 
 public class WorldGroup {
     private ArrayList<World> worldList;
     private Salesmania plugin;
     private String groupName;
     private AuctionQueue auctionQueue;
     private ArrayList<String> channelList;
 
     public WorldGroup(Salesmania plugin, ArrayList<String> worlds) {
         groupName = "NoName";
         worldList = new ArrayList<World>(worlds.size());
         this.plugin = plugin;
         auctionQueue = new AuctionQueue(plugin, this);
        channelList = new ArrayList<String>();
         updateWorlds(worlds);
     }
 
     public ArrayList<World> getWorlds() {
         return worldList;
     }
 
     public ArrayList<Player> getPlayers() {
         ArrayList<Player> playersInGroup = new ArrayList<Player>();
         for(World world : worldList) {
             playersInGroup.addAll(world.getPlayers());
         }
         return playersInGroup;
     }
 
     public boolean hasPlayer(String playerName) {
         Player player = plugin.getServer().getPlayerExact(playerName);
         return worldList.contains(player.getWorld());
     }
 
     public boolean hasPlayer(Player player) {
         return worldList.contains(player.getWorld());
     }
 
     public void updateWorlds(ArrayList<String> worlds) {
         for(String worldName : worlds) {
             World world = plugin.getServer().getWorld(worldName);
             if(world != null && !worldList.contains(world)) {
                 worldList.add(world);
             }
         }
     }
 
     public ArrayList<String> getChannels() {
         return channelList;
     }
 
     public void setChannels(ArrayList<String> channels) {
         channelList = channels;
     }
 
     public void addChannel(String channel) {
         if(!channelList.contains(channel)) {
             channelList.add(channel);
         }
     }
 
     public void removeChannel(String channel) {
         channelList.remove(channel);
     }
 
     public String getGroupName() {
         return groupName;
     }
 
     public void setGroupName(String groupName) {
         this.groupName = groupName;
     }
 
     public AuctionQueue getAuctionQueue() {
         return auctionQueue;
     }
 
     public void addWord(World world) {
         if(!worldList.contains(world)) {
             worldList.add(world);
         }
     }
 
     public void removeWorld(World world) {
         if(worldList.contains(world)) {
             worldList.remove(world);
         }
     }
 }
