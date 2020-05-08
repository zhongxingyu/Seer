 package org.fofo.entity;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  * @author josepma
  */
 public abstract class Competition {
     
     private Category category;
     private Date inici;
     private List<Team> teams = new ArrayList<Team>();
     private int maxTeams;
     private int minTeams;
     private Type type;
     private String name;
     
    public Competition(){
         create(this.type);
    }
     
     public static Competition create(Type type){
         if(type.equals(Type.LEAGUE)) return new CompetitionLeague();
         else return new CompetitionCup();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
     
     public void addTeam(Team team){
         teams.add(team);
     }
 
     public Category getCategory() {
         return category;
     }
 
     public void setCategory(Category category) {
         this.category = category;
     }
 
     public Date getInici() {
         return inici;
     }
 
     public void setInici(Date inici) {
         this.inici = inici;
     }
 
     public List<Team> getTeams() {
         return teams;
     }
 
     public void setTeams(List<Team> teams) {
         this.teams = teams;
     }
 
     public int getMaxTeams() {
         return maxTeams;
     }
 
     public abstract void setMaxTeams(int maxTeams);
 
     public int getMinTeams() {
         return minTeams;
     }
 
     public abstract void setMinTeams(int minTeams);
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public int getNumberOfTeams() {
         return teams.size();
     }
     
     
     
 }
