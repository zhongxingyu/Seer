 package jk_5.nailed.server.command;
 
 import jk_5.nailed.api.map.Map;
 import jk_5.nailed.api.map.teleport.TeleportOptions;
import jk_5.nailed.api.player.Player;
 import jk_5.nailed.map.teleport.TeleportHelper;
 import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
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
    public void processCommandPlayer(Player sender, Map map, String[] args){
        if(args.length == 0){
            TeleportOptions options = new TeleportOptions();
            options.setCoordinates(map.getRandomSpawnpoint());
            TeleportHelper.travelEntity(sender.getEntity(), options);
        }else{
            this.processCommandWithMap(sender.getEntity(), map, args);
         }
     }

    @Override
    public void processCommandWithMap(ICommandSender sender, Map map, String[] args){
        if(args.length == 1){
            EntityPlayerMP[] players = getPlayersList(sender, args[0]);
            for(EntityPlayerMP player : players){
                TeleportOptions options = new TeleportOptions();
                options.setCoordinates(map.getRandomSpawnpoint());
                TeleportHelper.travelEntity(player, options);
            }
        }else throw new WrongUsageException(this.getCommandUsage(sender));
    }
 }
