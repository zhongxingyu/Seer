 package com.galineer.suzy.accountsim.config;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.joda.time.DateTime;
 
 public final class SimulationConfig {
   public final DateTime simulationStart;
   public final DateTime simulationEnd;
   public final TaxConfig tax;
 
   // Accounts and events can be cross-referenced by string ID in different
   // parts of the config
   private final Map<String,AccountConfig> accounts;
   private final Map<String,EventConfig> events;
 
   // ... whereas interest specs and schedules cannot
   private final List<InterestConfig> interests;
   private final List<ScheduleConfig> schedules;
 
  private SimulationConfig(DateTime simulationStart, DateTime simulationEnd,
                          TaxConfig taxConfig) {
     this.simulationStart = simulationStart;
     this.simulationEnd = simulationEnd;
     this.tax = taxConfig;
    this.accounts = new HashMap<String,AccountConfig>();
    this.events = new HashMap<String,EventConfig>();
    this.interests = new ArrayList<InterestConfig>();
    this.schedules = new ArrayList<ScheduleConfig>();
   }
 
   public Map<String,AccountConfig> getAccountConfigs() {
     return Collections.unmodifiableMap(this.accounts);
   }
 
   public Map<String,EventConfig> getEventConfigs() {
     return Collections.unmodifiableMap(this.events);
   }
 
   public List<InterestConfig> getInterestConfigs() {
     return Collections.unmodifiableList(this.interests);
   }
 
   public List<ScheduleConfig> getScheduleConfigs() {
     return Collections.unmodifiableList(this.schedules);
   }
 }
