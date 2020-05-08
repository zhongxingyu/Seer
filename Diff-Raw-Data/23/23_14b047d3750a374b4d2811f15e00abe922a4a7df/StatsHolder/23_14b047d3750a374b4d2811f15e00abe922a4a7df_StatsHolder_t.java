 package com.ingemark.requestage;
 
 import java.io.Serializable;
 
 public class StatsHolder implements Serializable
 {
   public final Stats[] statsAry;
   public final int scriptsRunning;
 
  public StatsHolder(Stats[] statsAry, int scriptsRunning) {
    this.statsAry = statsAry;
     this.scriptsRunning = scriptsRunning;
   }
 }
