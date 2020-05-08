 package com.ziodyne.sometrpg.logic.models;
 
 import java.util.EnumSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class ModelTestUtils {
   private ModelTestUtils() { }
   
   private static Set<UnitStat> homogeneousStats(int value) {
     EnumSet<Stat> stats = EnumSet.allOf(Stat.class);
     
     Set<UnitStat> results = Sets.newHashSet();
     
     for (Stat stat : stats) {
       results.add(new UnitStat(value, stat));
     }
     
     return results;  
   }
   
   public static Set<UnitStat> createStats() {
     return homogeneousStats(10);
   }
   
   public static Set<UnitStat> createMinStats() {
     return homogeneousStats(0);
   }
   
   public static Set<UnitStat> createMaxStats() {
     return homogeneousStats(40);
   }
   
   public static UnitGrowth createGrowth() {
     Map<Stat, Float> rawGrowths = Maps.newHashMap();
     
     for (Stat stat : EnumSet.allOf(Stat.class)) {
       rawGrowths.put(stat, 40f);
     }
     
     return new UnitGrowth(rawGrowths);
   }
   
   public static Unit createUnit() {
     return new Unit(createMaxStats(), createGrowth(), createStats(), "Test");
   }
   
   public static Unit createMaxedUnit() {
    return new Unit(homogeneousStats(50), createGrowth(), homogeneousStats(50), "Test");
   }
   
   public static Unit createCrappyUnit() {
     return new Unit(createMaxStats(), createGrowth(), createMinStats(), "Test");
   }
 }
