 package to.joe.j2mc.seniorfun.command;
 
 import java.util.HashSet;
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.Packet60Explosion;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.seniorfun.J2MC_SeniorFun;
 
 public class FireAllAdminscommand extends MasterCommand{
 
 	Random randomGen = new Random();
 	Plugin j2mc_Seniorfun;
 	
 	public FireAllAdminscommand(J2MC_SeniorFun j2mc_SeniorFun){
 		super(j2mc_SeniorFun);
 		this.j2mc_Seniorfun = j2mc_SeniorFun;
 	}
 	
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
     	if(player.hasPermission("j2mc.senior")){
    		plugin.getLogger().info(player.getName() + " is firing all admins");
    		for(Player plr: Bukkit.getServer().getOnlinePlayers()){
     			int x = plr.getLocation().getBlockX();
     			double y = plr.getLocation().getBlockY();
     			int z = plr.getLocation().getBlockZ();
    			for(int i = 0; i < 10; i++){
     				double xToStrike;
     				double zToStrike;
     				if(randomGen.nextBoolean()){
     					xToStrike = x + randomGen.nextInt(6);
     				}else{
     					xToStrike = x - randomGen.nextInt(6);
     				}
     				if(randomGen.nextBoolean()){
     					zToStrike = z + randomGen.nextInt(6);
     				}else{
     					zToStrike = z - randomGen.nextInt(6);
     				}
     				Location toStrike = new Location(plr.getWorld(),xToStrike, y, zToStrike);
    				plr.getWorld().strikeLightningEffect(toStrike);
     			}
     			if(plr.hasPermission("j2mc.admin")){
                     final Packet60Explosion boom = new Packet60Explosion(x, y, z, 10, new HashSet<Block>());
                     ((CraftPlayer) plr).getHandle().netServerHandler.sendPacket(boom);
                    final Vector newVelocity = new Vector(((randomGen.nextFloat() * 1.5) - 0.75) * 1, (randomGen.nextFloat() / 2.5) + (0.4 * 1), ((randomGen.nextFloat() * 1.5) - 0.75) * 1);
                     plr.setVelocity(newVelocity);
     			}
     		}
     		Bukkit.getScheduler().scheduleSyncDelayedTask(j2mc_Seniorfun, new Runnable() {
     			public void run(){
     				for(Player plr: J2MC_Manager.getVisibility().getOnlinePlayers(null)){
     					if(plr.hasPermission("j2mc.admin")){
     						plr.kickPlayer("Immediately fucking fired.");
    						plugin.getLogger().info("Kicked " + plr.getName() + " for being an admin.");
     					}
     				}
     			}
    		}, 200);
     	}
     }
 	
 }
