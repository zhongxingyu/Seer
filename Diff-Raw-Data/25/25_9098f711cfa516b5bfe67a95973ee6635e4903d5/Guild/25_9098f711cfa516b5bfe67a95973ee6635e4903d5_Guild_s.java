 package com.pjbollinger.guilds.database;
 
 import java.util.List;
 
 public class Guild {
 
     private String name;
     private String leader;
     private List<String> moderators;
     private List<String> members;
     private List<Guild> alliances;
     private int pool; // Total power
     private int claimed; // Power used to claim lands
     private List<Integer> claims;
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getLeader() {
         return this.leader;
     }
 
     public void setLeader(String leader) {
         this.leader = leader;
     }
 
     public List<String> getModerators() {
         return this.moderators;
     }
 
     public void setModerators(List<String> moderators) {
         this.moderators = moderators;
     }
 
     public void addModerator(String moderator) {
         this.moderators.add(moderator);
     }
 
     public void removeModerator(String moderator) {
         this.moderators.remove(moderator);
     }
 
     public List<String> getMembers() {
         return members;
     }
 
     public void setMembers(List<String> members) {
         this.members = members;
     }
 
     public void addMember(String member) {
         this.members.add(member);
     }
 
     public void removeMember(String member) {
         this.members.remove(member);
     }
 
     public int getPool() {
         return pool;
     }
 
     public void setPool(int pool) {
         this.pool = pool;
     }
 
     public int getClaimed() {
         return claimed;
     }
 
     public void setClaimed(int claimed) {
         this.claimed = claimed;
     }
 
     public List<Integer> getClaims() {
         return claims;
     }
 
     public void setClaims(List<Integer> claims) {
         this.claims = claims;
     }
 
     public void addClaim(int claim) {
         this.claims.add(claim);
     }
 
     public List<Guild> getAlliances() {
         return alliances;
     }
 
     public void setAlliances(List<Guild> alliances) {
         this.alliances = alliances;
     }
 }
