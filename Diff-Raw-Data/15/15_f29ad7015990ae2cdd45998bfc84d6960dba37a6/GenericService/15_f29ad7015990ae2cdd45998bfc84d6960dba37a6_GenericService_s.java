 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an
  * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.powertac.generic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.joda.time.Instant;
 import org.powertac.common.PluginConfig;
 import org.powertac.common.Timeslot;
 import org.powertac.common.interfaces.BrokerMessageListener;
import org.powertac.common.interfaces.CompetitionControl;
 import org.powertac.common.interfaces.TimeslotPhaseProcessor;
 import org.powertac.common.state.StateChange;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * Generic service class framework for Power TAC component. This is set up as
  * a "leaf" service, which means that other services cannot depend on it. If
  * you want to support such dependencies, then this service must implement an
  * interface located in server-interface.
  * @author John Collins
  */
 @Service // allows this service to be autowired into other services
public class GenericService
  extends  TimeslotPhaseProcessor
   implements BrokerMessageListener
 {
   /** logger for trace logging -- use log.info(), log.warn(), and log.error()
    *  appropriately. Use log.debug() for output you want to see in testing or
    *  debugging. */
   static private Logger log = Logger.getLogger(GenericService.class.getName());
 
   @Autowired // spring will fill this in for you
  private CompetitionControl competitionControlService;
   
   private List<Timeslot> workingData;
   private int defaultParameter = 0;
   private int parameter = 0;
   
  /** when to invoke this service in per-timeslot processing */
  private int simulationPhase = 3;
  
   /**
    * Most services need only a default constructor. Remember that this will be
    * called once when the server starts, before any application-specific
    * initialization has been done. It is NOT called once/game.
    */
   public GenericService ()
   {
     super();
     workingData = new ArrayList<Timeslot>();
   }
   
   /**
    * This is called once at the beginning of each game by the initialization
    * service. Here is where you do per-game setup. Package visibility is used
    * because this should only be called by GenericInititializationService and
    * by test code.
    */
   void init (PluginConfig config)
   {
     workingData.clear();
     setParameter(config.getIntegerValue("parameter", defaultParameter));
     super.init();
   }
   
   @StateChange // logs state change
   private void setParameter (Integer number)
   {
     parameter = number;
   }
 
   public void activate (Instant time, int phaseNumber)
   {
     // TODO Implement per-timeslot behavior
   }
 
   public void receiveMessage (Object msg)
   {
     // TODO Implement per-message behavior. Note that incoming messages
     // from brokers arrive in a JMS thread, so you need to synchronize
     // access to shared data structures. See AuctionService for an example.
     
     // If you need to handle a number of different message types, it may make
     // make sense to use a reflection-based dispatcher. Both
     // TariffMarketService and AccountingService work this way.
   }
 
 }
