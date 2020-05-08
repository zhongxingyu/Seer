 /**
  * Copyright (c) 2012 Busko Trust
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.busko.routemanager.web;
 
 import org.busko.routemanager.model.transit.gtfs.Agency;
 import org.busko.routemanager.model.transit.gtfs.Calendar;
 import org.busko.routemanager.model.transit.gtfs.Route;
 import org.busko.routemanager.model.transit.gtfs.Stop;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @RequestMapping("/testdata/**")
 @Controller
 public class TestDataController {
 
     @RequestMapping
     public @ResponseBody String init() throws Exception {
         initGtfsData();
         return "Done";
     }
 
     private void initGtfsData() {
         initGtfsAgencyData();
         initGtfsCalendarData();
 //        initGtfsRouteData();
 //        initGtfsStopData();
     }
 
     private void initGtfsAgencyData() {
         if (Agency.countAgencys() > 0) return;
 
         Agency goBus = new Agency();
         goBus.setAgencyId("GoBus");
         goBus.setAgencyName("Dunedin City GoBus");
         goBus.setAgencyUrl("http://www.orc.govt.nz/Information-and-Services/Buses/Bus-Information/");
         goBus.setAgencyTimezone("Pacific/Auckland");
         goBus.setAgencyPhone("+643 474 0287");
         goBus.setAgencyLang("en");
         goBus.setUniqueEncoding("10");
         goBus.persist();
 
         Agency theBlenheimBus = new Agency();
         theBlenheimBus.setAgencyId("TheBlenheimBus");
         theBlenheimBus.setAgencyName("The Blenheim Bus");
         theBlenheimBus.setAgencyUrl("http://www.marlborough.govt.nz/Services/Parking-Roads-and-Transport/Blenheim-Bus-Service.aspx");
         theBlenheimBus.setAgencyTimezone("Pacific/Auckland");
         theBlenheimBus.setAgencyPhone("+643 520 7400");
         theBlenheimBus.setAgencyLang("en");
         theBlenheimBus.setUniqueEncoding("20");
         theBlenheimBus.persist();
     }
 
     private void initGtfsCalendarData() {
         if (Calendar.countCalendars() > 0) return;
 
         Calendar mon_fri =      new Calendar(Calendar.MON_FRI, true, true, true, true, true, false, false);
         mon_fri.persist();
         Calendar mon_sat =      new Calendar(Calendar.MON_SAT, true, true, true, true, true, true, false);
         mon_sat.persist();
         Calendar mon_sun =      new Calendar(Calendar.MON_SUN, true, true, true, true, true, true, true);
         mon_sun.persist();
         Calendar sat_sun =      new Calendar(Calendar.SAT_SUN, false, false, false, false, false, true, true);
         sat_sun.persist();
         Calendar monday =       new Calendar(Calendar.MON, true, false, false, false, false, false, false);
         monday.persist();
         Calendar tuesday =      new Calendar(Calendar.TUE, false, true, false, false, false, false, false);
         tuesday.persist();
         Calendar wednesday =    new Calendar(Calendar.WED, false, false, true, false, false, false, false);
         wednesday.persist();
         Calendar thursday =     new Calendar(Calendar.THR, false, false, false, true, false, false, false);
         thursday.persist();
         Calendar friday =       new Calendar(Calendar.FRI, false, false, false, false, true, false, false);
         friday.persist();
         Calendar saturday =     new Calendar(Calendar.SAT, false, false, false, false, false, true, false);
         saturday.persist();
         Calendar sunday =       new Calendar(Calendar.SUN, false, false, false, false, false, false, true);
         sunday.persist();
     }
 
     private void initGtfsRouteData() {
         if (Route.countRoutes() > 0) return;
 
         Agency goBus = Agency.findAgencysByAgencyIdEquals("GoBus").getSingleResult();
         if (goBus == null) return;
 
         Route goBus28 = new Route(goBus, "DN28", "28", "DN28", "St Clair - Octagon", "DN28_9", "FF0000");
         goBus28.persist();
         Route goBus9 = new Route(goBus, "DN9", "09", "DN9", "Octagon - Normanby", "DN28_9", "FF0000");
         goBus9.persist();
 //        Route goBus28A = new Route(goBus, "DN28A", "281", "DN28A", "St Clair - Octagon", "DN28A_9", "FF0000");
 //        goBus28A.persist();
 //        Route goBus9A = new Route(goBus, "DN9A", "091", "DN9A", "Octagon - Normanby", "DN28A_9", "FF0000");
 //        goBus9A.persist();
         Route goBus8 = new Route(goBus, "DN8", "08", "DN8", "Normanby - Octagon", "DN8_29", "FF0000");
         goBus8.persist();
         Route goBus29 = new Route(goBus, "DN29", "29", "DN29", "Octagon - St Clair", "DN8_29", "FF0000");
         goBus29.persist();
     }
 
     private void initGtfsStopData() {
         if (Stop.countStops() > 0) return;
 
         Agency goBus = Agency.findAgencysByAgencyIdEquals("GoBus").getSingleResult();
         if (goBus == null) return;
 
         Stop goBusStand1 = new Stop(goBus.getUniqueEncoding() + "0001", "Octagon Stand 1", null, "-45.87480053758057", "170.5032499075278");
         goBusStand1.persist();
         Stop goBusStand2 = new Stop(goBus.getUniqueEncoding() + "0002", "Octagon Stand 2", null, "-45.8749402228972", "170.50316855255505");
         goBusStand2.persist();
         Stop goBusStand3 = new Stop(goBus.getUniqueEncoding() + "0003", "Octagon Stand 3", null, "-45.87426645681816", "170.50302215227168");
         goBusStand3.persist();
         Stop goBusStand4 = new Stop(goBus.getUniqueEncoding() + "0004", "Cumberland St. Stand 4", null, "-45.87392598380922", "170.50703807272362");
         goBusStand4.persist();
         Stop goBusStand5 = new Stop(goBus.getUniqueEncoding() + "0005", "Cumberland St. Stand 5", null, "-45.87117470208344", "170.50859003242851");
         goBusStand5.persist();
         Stop goBusStand7 = new Stop(goBus.getUniqueEncoding() + "0007", "Octagon Stand 7", null, "-45.8735052714479", "170.50398197776394");
         goBusStand7.persist();
         Stop goBusStand8 = new Stop(goBus.getUniqueEncoding() + "0007", "Octagon Stand 8", null, "-45.87359783715763", "170.5039306691184");
         goBusStand8.persist();
     }
 }
