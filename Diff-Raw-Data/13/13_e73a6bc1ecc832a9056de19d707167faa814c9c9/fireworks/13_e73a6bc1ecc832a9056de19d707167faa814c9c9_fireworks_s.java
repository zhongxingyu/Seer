 package com.republicasmp.dev.randomfireworks;
 
 
 import java.util.Random;
 import org.bukkit.Bukkit;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.event.EventHandler;
 
 
 
 public class random extends JavaPlugin implements Listener
 {
 	 @EventHandler
 	 public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		 if (cmd.getName().equalsIgnoreCase("firework")){
 
 			 Player p = (Player) sender;
 	        if (p.hasPermission("ziminiar.level")){
 
 	            //Spawn the Firework
 	            Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
 	            FireworkMeta fwm = fw.getFireworkMeta();
 
 	            //random
 	            Random r = new Random();
 
 	            //sets type
 	            int rt = r.nextInt(5) + 1;
 	            Type type = Type.BALL;
 	            if (rt == 1) type = Type.BALL;
 	            if (rt == 2) type = Type.BALL_LARGE;
 	            if (rt == 3) type = Type.BURST;
 	            if (rt == 4) type = Type.CREEPER;
 	            if (rt == 5) type = Type.STAR;
 
 	            //colors
 	            //To be Added
	            int r1i = r.nextInt(17) + 1;
	            int r2i = r.nextInt(17) + 1;
	            Color c1 = getColor(r1i);
	            Color c2 = getColor(r2i);
 
 
 	            //effect
 	            FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();
 
 	            //applied effects
 	            fwm.addEffect(effect);
 
 	            //random power! moar sulphur!
 	            int rp = r.nextInt(2) + 1;
 	            fwm.setPower(rp);
 
 	            //aaaaaand set it
 	            fw.setFireworkMeta(fwm);
 	        }
 	     return true;
 		 }
 		 return false;
 	    }
 
 
 }
