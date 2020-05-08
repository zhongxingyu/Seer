 package com.blazeloader.TerrainEdit.main;
 
 import com.blazeloader.TerrainEdit.functions.*;
import com.blazeloader.api.direct.server.api.chat.EChatColor;
 import com.blazeloader.api.direct.server.api.command.BLCommandBase;
 import net.minecraft.command.ICommandSender;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The base command for TerrainEdit.  Loads functions as sub-commands.
  */
 public class CommandTE extends BLCommandBase {
     protected ModTerrainEdit baseMod;
     public Map<String, Function> functionList = new HashMap<String, Function>();
 
     public CommandTE(ModTerrainEdit baseMod) {
         super();
         this.baseMod = baseMod;
         new FunctionHelp(baseMod, this);
         new FunctionP1(baseMod, this);
         new FunctionP2(baseMod, this);
         new FunctionSet(baseMod, this);
         new FunctionDelete(baseMod, this);
         new FunctionGenRan(baseMod, this);
         new FunctionReplace(baseMod, this);
         new FunctionSchemLoad(baseMod, this);
         new FunctionUndo(baseMod, this);
         new FunctionLayer(baseMod, this);
         //new FunctionShift(baseMod, this);  //Temporarily disabled
     }
 
     /**
      * Return the required permission level for this command.
      */
     @Override
     public int getRequiredPermissionLevel() {
         return 4;
     }
 
     @Override
     public String getCommandName() {
         return "te";
     }
 
     @Override
     public String getCommandUsage(ICommandSender var1) {
         return "/te [function [args]]";
     }
 
     @Override
     public void processCommand(ICommandSender user, String[] args) {
         if (args.length == 0) {
             sendChatLine(user, EChatColor.COLOR_RED + "Please specify a function using /te [function [args]].  Use /te help for more info.");
         } else {
             Function function = functionList.get(args[0]);
             if (function != null) {
                 function.execute(user, args);
             } else {
                 sendChat(user, EChatColor.COLOR_RED + "Unknown function!  Use \"/te help\" for a list.");
             }
         }
     }
 }
