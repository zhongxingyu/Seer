 package com.freakz.hokan_ng.core_engine.command.handlers;
 
 import com.freakz.hokan_ng.common.exception.HokanException;
 import com.freakz.hokan_ng.common.rest.EngineRequest;
 import com.freakz.hokan_ng.common.rest.EngineResponse;
 import com.freakz.hokan_ng.common.updaters.UpdaterData;
 import com.freakz.hokan_ng.common.updaters.UpdaterManagerService;
 import com.freakz.hokan_ng.common.updaters.horo.HoroHolder;
 import com.freakz.hokan_ng.common.updaters.horo.HoroUpdater;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.UnflaggedOption;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  * User: petria
  * Date: 11/21/13
  * Time: 1:43 PM
  *
  * @author Petri Airio <petri.j.airio@gmail.com>
  */
 @Component
 public class HoroCmd extends Cmd {
 
   @Autowired
   private UpdaterManagerService updaterManagerService;
 
   private final static String ARG_HORO = "horo";
 
   public HoroCmd() {
     super();
 
     UnflaggedOption opt = new UnflaggedOption(ARG_HORO)
         .setRequired(true)
         .setGreedy(false);
     registerParameter(opt);
 
   }
 
   @Override
   public String getMatchPattern() {
     return "!horo.*";
   }
 
   @Override
   public void handleRequest(EngineRequest request, EngineResponse response, JSAPResult results) throws HokanException {
     String horo = results.getString(ARG_HORO);
     HoroUpdater horoUpdater = (HoroUpdater) updaterManagerService.getUpdater("horoUpdater");
 
     UpdaterData updaterData = new UpdaterData();
     horoUpdater.getData(updaterData, horo);
     HoroHolder hh = (HoroHolder) updaterData.getData();
     if (hh != null) {
       response.setResponseMessage(hh.toString());
     } else {
      response.setResponseMessage("Saat dildoo perään et pääse pylsimään!");
     }
   }
 
 }
