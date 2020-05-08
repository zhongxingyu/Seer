 package me.tehbeard.BeardStat.containers;
 
import java.util.Collection;
import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import me.tehbeard.utils.expressions.VariableProvider;
 
 /**
  * Represents a collection of player statistics
  * @author James
  *
  */
 public class PlayerStatBlob implements VariableProvider{
 
     private static Map<String,String> dynamics = new HashMap<String, String>();
     
     public static void addDynamicStat(String stat,String expr){
         dynamics.put(stat,expr);
     }
     
     private void addDynamics(){
         for(Entry<String, String> entry  : dynamics.entrySet()){
             
             System.out.println("Making " + (entry.getKey().split("\\.")[0] + " " +  entry.getKey().split("\\.")[1] + " = " + entry.getValue()));
             addStat(new DynamicPlayerStat(entry.getKey().split("\\.")[0], entry.getKey().split("\\.")[1],entry.getValue()));
         }
     }
     
    private Set<PlayerStat> stats;
     private String name;
 
     public String getName() {
         return name;
     }
 
     public int getPlayerID() {
         return playerID;
     }
     private int playerID;
 
     /**
      * 
      * @param name Players name
      * @param ID playerID in database
      */
     public PlayerStatBlob(String name,int ID){
         this.name = name;
         playerID=ID;
         stats = new HashSet<PlayerStat>();
         
         addDynamics();
     }
 
     /**
      * add stat
      * @param stat
      */
     public void addStat(PlayerStat stat){
         stats.add(stat);
         stat.setOwner(this);
     }
 
     /**
      * Get a players stat, creates new object if not found.
      * @param name
      * @return
      */
     public PlayerStat getStat(String cat,String name){
         for(PlayerStat ps: stats){
             if(ps.getName().equals(name) && ps.getCat().equals(cat)){
                 return ps;
             }
         }
         PlayerStat psn = new StaticPlayerStat(cat,name,0);
         psn.setValue(0);
         addStat(psn);
         return psn;
     }
     /**
      * Return all the stats!
      * @return
      */
     public Set<PlayerStat> getStats(){
        return stats;
     }
 
     public boolean hasStat(String cat,String stat){
         for(PlayerStat ps: stats){
             if(ps.getName().equals(stat) && ps.getCat().equals(cat)){
                 return true;
             }
         }
         return false;
     }
 
     public int resolveVariable(String var) {
         return getStat(var.split("\\.")[0],var.split("\\.")[1]).getValue();
     }
 
     /**
      * Expose create tag
      */
 
 }
