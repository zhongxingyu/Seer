 package me.tehbeard.BeardStat.containers;
 
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.utils.expressions.VariableProvider;
 
 /**
  * Represents a collection of player statistics
  * @author James
  *
  */
 public class PlayerStatBlob implements VariableProvider{
 
     private static Map<String,String> dynamics = new HashMap<String, String>();
 
     private static Map<String,String> dynamicsSaved = new HashMap<String, String>();
 
     public static void addDynamicStat(String stat,String expr){
         dynamics.put(stat,expr);
     }
 
     public static void addDynamicSavedStat(String stat,String expr){
         dynamicsSaved.put(stat,expr);
     }
 
     private void addDynamics(){
         for(Entry<String, String> entry  : dynamics.entrySet()){
 
             BeardStat.printDebugCon("Making temporary stat: " + (entry.getKey().split("\\.")[0] + " " +  entry.getKey().split("\\.")[1] + " = " + entry.getValue()));
             addStat(new DynamicPlayerStat(entry.getKey().split("\\.")[0], entry.getKey().split("\\.")[1],entry.getValue()));
         }
 
         //dynamics that will be saved to database
         for(Entry<String, String> entry  : dynamicsSaved.entrySet()){
 
             BeardStat.printDebugCon("Making custom stat: " + (entry.getKey().split("\\.")[0] + " " +  entry.getKey().split("\\.")[1] + " = " + entry.getValue()));
             addStat(new DynamicPlayerStat(entry.getKey().split("\\.")[0], entry.getKey().split("\\.")[1],entry.getValue(),true));
         }
     }
 
     private HashSet<PlayerStat> stats;
     private String name;
 
     public String getName() {
         return name;
     }
 
     public String getPlayerID() {
         return playerID;
     }
     private String playerID;
 
     /**
      * 
      * @param name Players name
      * @param ID playerID in database
      */
    public PlayerStatBlob(String name,String ID){
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
 
         return  (Set<PlayerStat>) stats.clone();
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
