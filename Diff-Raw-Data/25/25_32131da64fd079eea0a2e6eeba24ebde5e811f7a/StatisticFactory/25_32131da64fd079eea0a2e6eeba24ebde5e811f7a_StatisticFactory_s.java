 package models.statistics.statistics;
 
 import java.lang.String;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 
 public class StatisticFactory {
 
     private static StatisticFactory instance = null;
 
     public static StatisticFactory instance() {
         if(instance == null) instance = new StatisticFactory();
         return instance;
     }
 
     private Map<String, Factory> map;
 
     private StatisticFactory() {
         map = new HashMap<String, Factory>();
         map.put(Gender.name, new Factory() {
             public Statistic create() { return new Gender(); }
         });
         map.put(Score.name, new Factory() {
             public Statistic create() { return new Score(); }
         });
     }
 
     private static interface Factory {
         public Statistic create();
     }
 
    // TODO throw statisticCreationException
     public Statistic create(String type) {
         return map.get(type).create();
     }
 
     public Set<String> statistics() {
         return map.keySet();
     }
 
 }
