 package info.bytecraft.commands;
 
 import org.bukkit.GameMode;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 
 public class GameModeCommand extends AbstractCommand
 {
 
     public GameModeCommand(Bytecraft instance, String command)
     {
         super(instance, command);
     }
     
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         if(!player.getRank().canFill())return true;
         if("gamemode".equalsIgnoreCase(getCommand())){
             if(args.length == 0){
                 if(player.getGameMode() == GameMode.CREATIVE){
                     player.setGameMode(GameMode.SURVIVAL);
                 }else{
                     player.setGameMode(GameMode.CREATIVE);
                 }
             }else if(args.length == 1){
                player.setGameMode(byName(args[0]));
             }
         }else if("creative".equalsIgnoreCase(getCommand())){
             player.setGameMode(GameMode.CREATIVE);
         }else if("survival".equalsIgnoreCase(getCommand())){
             player.setGameMode(GameMode.SURVIVAL);
         }
         return true;
     }
    
    private GameMode byName(String args)
    {
        switch(args.toLowerCase())
        {
        case "survival":
            return GameMode.SURVIVAL;
        case "creative":
            return GameMode.CREATIVE;
        default:
            return GameMode.SURVIVAL;
        }
    }
 }
