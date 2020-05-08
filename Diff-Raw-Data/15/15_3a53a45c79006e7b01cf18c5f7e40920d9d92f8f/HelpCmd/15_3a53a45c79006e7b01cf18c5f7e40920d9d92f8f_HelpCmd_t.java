 package com.freakz.hokan_ng.core_engine.command.handlers;
 
 import com.freakz.hokan_ng.common.exception.HokanException;
 import com.freakz.hokan_ng.common.rest.EngineRequest;
 import com.freakz.hokan_ng.common.rest.EngineResponse;
 import com.freakz.hokan_ng.common.service.AccessControlService;
 import com.freakz.hokan_ng.core_engine.command.CommandHandlerService;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.UnflaggedOption;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * User: petria
  * Date: 11/21/13
  * Time: 1:55 PM
  *
  * @author Petri Airio <petri.j.airio@gmail.com>
  */
 @Component
 public class HelpCmd extends Cmd {
 
   @Autowired
   private AccessControlService accessControlService;
 
   @Autowired
   private CommandHandlerService commandHandler;
 
   private final static String ARG_COMMAND = "Command";
 
   public HelpCmd() {
     super();
     UnflaggedOption flg = new UnflaggedOption(ARG_COMMAND)
         .setRequired(false)
         .setGreedy(false);
     registerParameter(flg);
   }
 
   @Override
   public String getMatchPattern() {
     return "!help.*";
   }
 
   @Override
   public void handleRequest(EngineRequest request, EngineResponse response, JSAPResult results) throws HokanException {
 
     String command = results.getString(ARG_COMMAND);
 
     StringBuilder sb = new StringBuilder();
 
     Comparator<Cmd> comparator = new Comparator<Cmd>() {
       @Override
       public int compare(Cmd cmd1, Cmd cmd2) {
         return cmd1.getName().compareTo(cmd2.getName());
       }
     };
 
     if (command == null) {
       boolean isMasterUser = accessControlService.isMasterUser(request.getIrcEvent());
       boolean isChannelOp = accessControlService.isChannelOp(request.getIrcEvent());
 
       List<Cmd> commands = commandHandler.getCommandHandlers();
       Collections.sort(commands, comparator);
 
       sb.append("== HELP: Command List");
       sb.append("\n");
 
       for (Cmd cmd : commands) {
 
         if (cmd.isChannelOpOnly() && (isChannelOp || isMasterUser)) {
           continue;
         }
         if (cmd.isMasterUserOnly() && (!isMasterUser)) {
           continue;
         }
         sb.append("  ");
         sb.append(cmd.getName());
       }
 
       sb.append("\nTry '!help <command>' to get detailed help\n");
 
     } else {
       List<Cmd> commands = commandHandler.getCommandHandlersByName(command);
       Collections.sort(commands, comparator);
       for (Cmd cmd : commands) {
        String seeAlsoHelp = "";
        for (String seeAlsoTxt : cmd.getSeeAlso()) {
           if (seeAlsoHelp.length() == 0) {
            seeAlsoHelp = " See also: ";
           } else {
             seeAlsoHelp += ", ";
           }
           seeAlsoHelp += seeAlsoTxt;
         }
         String help = String.format("HELP: %s (%s) -> %s%s",
             cmd.getName(), cmd.getMatchPattern(), cmd.getHelp(), seeAlsoHelp);
         sb.append(help);
         sb.append("\n");
       }
     }
     response.setResponseMessage(sb.toString());
 
   }
 }
