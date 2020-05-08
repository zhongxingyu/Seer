 package to.joe.j2mc.fun;
 
 import java.util.List;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.fun.command.ClearInventoryCommand;
 import to.joe.j2mc.fun.command.GameModeToggleCommand;
import to.joe.j2mc.fun.command.ItemCommand;
 import to.joe.j2mc.fun.command.SpawnCommand;
 
 public class J2MC_Fun extends JavaPlugin{
 
     public boolean AnyoneCanChangeGameMode;
     public List<Integer> BlockedForTrusted;
     public List<Integer> BlockedForNormals;
     public List<Integer> SummonBlackList;
     public List<Integer> SummonWatchList;
     
     @Override
     public void onEnable(){
         this.getConfig().options().copyDefaults(true);
         this.AnyoneCanChangeGameMode = this.getConfig().getBoolean("letanyonechangegamemode");
         this.BlockedForTrusted = this.getConfig().getIntegerList("blockfortrusted");
         this.BlockedForNormals = this.getConfig().getIntegerList("blockfornormals");
         this.SummonBlackList = this.getConfig().getIntegerList("summonblacklist");
         this.SummonWatchList = this.getConfig().getIntegerList("summonwatchlist");
         
         this.getCommand("spawn").setExecutor(new SpawnCommand(this));
         this.getCommand("gm").setExecutor(new GameModeToggleCommand(this));
         this.getCommand("ci").setExecutor(new ClearInventoryCommand(this));
        this.getCommand("item").setExecutor(new ItemCommand(this));
        this.getCommand("i").setExecutor(new ItemCommand(this));
        this.getCommand("give").setExecutor(new ItemCommand(this));
         
         this.getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
         this.getLogger().info("Fun module enabled!");
     }
     
     @Override
     public void onDisable(){
         this.getLogger().info("Fun module disabled.");
     }
     
     
 }
