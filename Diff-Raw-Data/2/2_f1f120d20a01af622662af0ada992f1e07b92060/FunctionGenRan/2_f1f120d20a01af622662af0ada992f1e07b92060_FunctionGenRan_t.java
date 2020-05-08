 package net.acomputerdog.TerrainEdit.functions;
 
 import net.acomputerdog.BlazeLoader.api.block.ENotificationType;
 import net.acomputerdog.BlazeLoader.api.chat.EChatColor;
 import net.acomputerdog.TerrainEdit.config.Config;
 import net.acomputerdog.TerrainEdit.cuboid.Cuboid;
 import net.acomputerdog.TerrainEdit.cuboid.CuboidTable;
 import net.acomputerdog.TerrainEdit.main.CommandTE;
 import net.acomputerdog.TerrainEdit.main.ModTerrainEdit;
 import net.acomputerdog.TerrainEdit.undo.UndoList;
 import net.minecraft.src.ICommandSender;
 
 import java.util.Random;
 
 /**
  * Function that generates terrain based on random numbers and a percent chance.  Uses the seed "TerrainEdit".
  */
 public class FunctionGenRan extends Function {
     private Random random = new Random("TerrainEdit".hashCode());
 
     public FunctionGenRan(ModTerrainEdit baseMod, CommandTE baseCommand) {
         super(baseMod, baseCommand);
     }
 
     /**
      * Gets the name of the function.
      *
      * @return Return the name of the function.
      */
     @Override
     public String getFunctionName() {
         return "genran";
     }
 
     /**
      * Executes the command.
      *
      * @param user The user executing the command.
      * @param args The arguments passed to the module.
      *             -WARNING: args[0] is always the name of the sub-command!  Skip it!-
      */
     @Override
     public void execute(ICommandSender user, String[] args) {
         if(args.length < 3){
             sendChatLine(user, EChatColor.COLOR_RED + "Not enough args!  Use /te genran <chance> <block_id> [metadata]");
         }else{
             try{
                 Cuboid cuboid = CuboidTable.getCuboidForPlayer(user.getCommandSenderName());
                 if(cuboid.getIsSet()){
                     int chance = Integer.parseInt(args[1]);
                     int id = Integer.parseInt(args[2]) ;
                     int meta = 0;
                     if(args.length >= 4){
                         meta = Integer.parseInt(args[3]);
                     }
                     UndoList.createUndoTask(user.getEntityWorld(), cuboid);
                     for(int x = Math.min(cuboid.getXPos1(), cuboid.getXPos2()); x <= Math.max(cuboid.getXPos1(), cuboid.getXPos2()); x++){
                         for(int y = Math.min(cuboid.getYPos1(), cuboid.getYPos2()); y <= Math.max(cuboid.getYPos1(), cuboid.getYPos2()); y++){
                             for(int z = Math.min(cuboid.getZPos1(), cuboid.getZPos2()); z <= Math.max(cuboid.getZPos1(), cuboid.getZPos2()); z++){
                                if(random.nextInt(100) < chance){
                                     user.getEntityWorld().setBlock(x, y, z, id, meta, ENotificationType.NOTIFY_CLIENTS.getType());
                                 }
                             }
                         }
                     }
                     if(Config.getConfigForPlayer(user.getCommandSenderName()).commandConfirmation){
                         sendChatLine(user, EChatColor.COLOR_YELLOW + "Done.");
                     }
                 }else{
                     sendChatLine(user, EChatColor.COLOR_RED + "You must select a cuboid first!  Use /te p1 and /te p2!");
                 }
             }catch(NumberFormatException e){
                 sendChatLine(user, EChatColor.COLOR_RED + "Invalid arguments!  Use /te genran <chance> <block_id> [metadata]");
             }catch(Exception e){
                 sendChatLine(user, EChatColor.COLOR_RED + "" + EChatColor.FORMAT_UNDERLINE + "" + EChatColor.FORMAT_BOLD + "An error occurred while generating blocks!");
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Gets a concise description of what the function does.
      *
      * @return Return a concise description of what the function does.
      */
     @Override
     public String getFunctionDescription() {
         return "Generates terrain based on random numbers";
     }
 }
