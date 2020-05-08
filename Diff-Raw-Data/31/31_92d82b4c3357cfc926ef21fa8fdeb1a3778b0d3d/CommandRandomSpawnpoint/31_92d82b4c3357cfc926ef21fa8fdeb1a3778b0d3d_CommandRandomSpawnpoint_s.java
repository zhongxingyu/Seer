 package jk_5.nailed.server.command;
 
 import jk_5.nailed.api.map.Map;
 import jk_5.nailed.api.map.teleport.TeleportOptions;
 import jk_5.nailed.map.teleport.TeleportHelper;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayerMP;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class CommandRandomSpawnpoint extends NailedCommand {
 
     public CommandRandomSpawnpoint(){
         super("randomspawnpoint");
     }
 
     @Override
    public void processCommandWithMap(ICommandSender sender, Map map, String[] args){
        EntityPlayerMP[] players = getPlayersList(sender, args[0]);
        TeleportOptions options = new TeleportOptions();
        options.setDestination(map);
        options.setCoordinates(map.getRandomSpawnpoint());
        for(EntityPlayerMP player : players){
            TeleportHelper.travelEntity(player, options);
         }
     }
 }
