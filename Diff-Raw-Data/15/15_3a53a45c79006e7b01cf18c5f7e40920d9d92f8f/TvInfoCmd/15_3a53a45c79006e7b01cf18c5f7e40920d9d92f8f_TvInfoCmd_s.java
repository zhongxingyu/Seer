 package com.freakz.hokan_ng.core_engine.command.handlers;
 
 import com.freakz.hokan_ng.common.exception.HokanException;
 import com.freakz.hokan_ng.common.rest.EngineRequest;
 import com.freakz.hokan_ng.common.rest.EngineResponse;
 import com.freakz.hokan_ng.common.updaters.telkku.TelkkuProgram;
 import com.freakz.hokan_ng.common.updaters.telkku.TelkkuService;
 import com.freakz.hokan_ng.common.util.StringStuff;
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.UnflaggedOption;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.text.SimpleDateFormat;
 
 /**
  * User: petria
  * Date: 11/27/13
  * Time: 4:13 PM
  *
  * @author Petri Airio <petri.j.airio@gmail.com>
  */
 @Component
 public class TvInfoCmd extends Cmd {
 
   private static final String ARG_ID = "ID";
   @Autowired
   private TelkkuService tv;
 
   public TvInfoCmd() {
     super();
    setHelp("Shows info description of given tv program ID. Use !tvfind or !tvnow to find out program IDs.");
     addSeeAlso("TvFind");
     addSeeAlso("TvNow");
 
     UnflaggedOption opt = new UnflaggedOption(ARG_ID)
         .setStringParser(JSAP.INTEGER_PARSER)
         .setRequired(true)
         .setGreedy(false);
     registerParameter(opt);
   }
 
   @Override
   public void handleRequest(EngineRequest request, EngineResponse response, JSAPResult results) throws HokanException {
     if (!tv.isReady()) {
       response.addResponse("Telkku data not loaded yet! Try again later...");
       return;
     }
     int id = results.getInt(ARG_ID);
     TelkkuProgram prg = tv.findProgramById(id);
     if (prg != null) {
       SimpleDateFormat dateFormat;
       if (StringStuff.isDateToday(prg.getStartTimeD())) {
         dateFormat = StringStuff.STRING_STUFF_DF_HHMM;
       } else {
         dateFormat = StringStuff.STRING_STUFF_DF_DDMMHHMM;
       }
       String reply = StringStuff.formatTime(prg.getStartTimeD(), dateFormat) +
           " " + prg.getProgram() + ": " + prg.getDescription();
       response.addResponse(reply);
       return;
     }
     response.addResponse("No program found with id: " + id);
   }
 
 }
