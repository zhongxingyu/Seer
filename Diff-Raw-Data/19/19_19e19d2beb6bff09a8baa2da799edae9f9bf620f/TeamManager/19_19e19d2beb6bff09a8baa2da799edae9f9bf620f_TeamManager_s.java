 /*
  * The MIT License
  *
  * Copyright 2013 Manuel Gauto.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.mgenterprises.java.bukkit.gmcfps.Core.Teams;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import org.bukkit.entity.Player;
 import org.mgenterprises.java.bukkit.gmcfps.Core.FPSCore;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Events.PlayerHurtByPlayerEvent;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Events.PlayerJoinedTeamEvent;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Events.PlayerLeftTeamEvent;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Listeners.PlayerHurtByPlayerListener;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Sources.PlayerJoinedTeamSource;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Sources.PlayerLeftTeamSource;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class TeamManager implements PlayerHurtByPlayerListener {
 
     private ArrayList<Team> teams = new ArrayList<Team>();
     private FPSCore core;
     private boolean teamsEnabled = false;
 
     public TeamManager(FPSCore core) {
         this.core = core;
         core.getEventManager().getPlayerHurtSource().addEventListener(this);
     }
 
     public void setTeamEnable(boolean status){
         this.teamsEnabled = status;
     }
     
     public void setFreeForAll(boolean isFreeForAll){
         if(isFreeForAll){
             teamsEnabled = false;
         }
         else{
             teamsEnabled = true;
         }
     }
     
     public boolean isFreeForAll(){
         if(this.teamsEnabled){
             return false;
         }
         else{
             return true;
         }
     }
     
     public void registerTeam(Team team) {
         this.teams.add(team);
     }
 
     public void unregisterTeam(Team t) {
         this.teams.remove(t);
     }
 
     public Player[] getAllPlayers() {
         ArrayList<Player> players = new ArrayList<Player>();
         for (Team team : teams) {
             players.addAll(Arrays.asList(team.getMembers()));
         }
         Player[] template = new Player[players.size()];
         return players.toArray(template);
     }
 
     public Player[] getAllPlayersOnTeam(Team team) {
         ArrayList<Player> players = new ArrayList<Player>();
         for (Team t : teams) {
             if (team.getName().equals(t.getName())) {
                 players.addAll(Arrays.asList(team.getMembers()));
             }
         }
         Player[] template = new Player[players.size()];
         return players.toArray(template);
     }
 
     public Team getPlayerTeam(Player p) {
         for (Team t : teams) {
             if (t.isMember(p)) {
                 return t;
             }
         }
         return null;
     }
 
     public boolean isParticipating(Player p) {
         for (Team t : teams) {
             if (t.isMember(p)) {
                 return true;
             }
         }
         return false;
     }
 
     public Team registerPlayer(Player p) {
         Team t = getSmallestTeam();
         t.addMember(p);
         PlayerJoinedTeamSource source = core.getEventManager().getPlayerJoinedTeamSource();
         PlayerJoinedTeamEvent event = new PlayerJoinedTeamEvent(source, p, t, core.getGameReference());
         source.fireEvent(event);
         return t;
     }
 
     public void unregisterPlayer(Player p) {
         for (Team t : teams) {
             if (t.isMember(p)) {
                 t.removeMember(p);
                 PlayerLeftTeamSource source = core.getEventManager().getPlayerLeftTeamSource();
                 PlayerLeftTeamEvent event = new PlayerLeftTeamEvent(source, p, t, core.getGameReference());
                 source.fireEvent(event);
             }
         }
     }
 
     public Team getSmallestTeam() {
         Team smallest = teams.get(0);
         int smallestSize = smallest.size();
 
         for (Team t : teams) {
             if (t.size() < smallestSize) {
                 smallest = t;
                 smallestSize = t.size();
             }
         }
 
         return smallest;
     }
 
     public ArrayList<Team> getAllTeams(){
         return this.teams;
     }
     
     public boolean canHurtEachother(Player p1, Player p2) {
         Team p1t = this.getPlayerTeam(p1);
         Team p2t = this.getPlayerTeam(p2);
 
         if (p1t.getName().equals(p2t.getName())) {
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public void onPlayerHurtByPlayerEvent(PlayerHurtByPlayerEvent event) {
         if(this.teamsEnabled){
             if(!canHurtEachother(event.getDamager(),event.getVictim())){
                 event.setCancelled(true);
             }
         }
     }
 }
