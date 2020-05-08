 package me.naithantu.ArenaPVP.Commands;
 
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaState;
 import me.naithantu.ArenaPVP.Arena.ArenaTeam;
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.Util.Util;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.Collection;
 
 public class CommandJoin extends AbstractArenaCommand {
 
 	protected CommandJoin(CommandSender sender, String[] args, ArenaPVP plugin, ArenaManager arenaManager) {
 		super(sender, args, plugin, arenaManager);
 	}
 
 	@Override
     protected boolean handle() {
 		if(!testPermission(sender, "join") && !testPermission(sender, "player")){
 			this.noPermission(sender);
 			return true;
 		}
 		
 		if (!(sender instanceof Player)) {
 			this.msg(sender, "That command can only be used in-game.");
 			return true;
 		}
 		
 		if(arenaManager.getPlayerByName(sender.getName()) != null){
 			this.msg(sender, "You are already in a game, you can leave with /pvp leave!");
 			return true;
 		}
 
         this.executeCommand(getArenas());
 		return true;
 	}
 
     @Override
     protected Collection<Arena> getArenas() {
         return this.selectArena(args, ArenaState.LOBBY, ArenaState.WARMUP);
     }
 
     @Override
     protected void runCommand(Arena arena){
         Player player = (Player) sender;
 
         if(player.isInsideVehicle()){
             Util.msg(sender, "You can't join while inside a vehicle!");
             return;
         }
 
         ArenaTeam team = null;
        int index = -1;
        if(args.length == 1){
            index = 0;
        } else if (args.length == 2){
            index = 1;
        }

        if(index != -1){
            try{
                team  = arena.getTeam(Integer.parseInt(args[index]));
            } catch (NumberFormatException e){
                team = arena.getTeam(args[index]);
             }
         }
 
 		if(!arena.joinGame(player, team)){
 			this.msg(sender, "You were unable to join arena " + arena.getArenaName() + ", try a different arena!");
 		}
     }
 }
