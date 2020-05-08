 package me.G4meM0ment.Chaintrain;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Chaintrain extends JavaPlugin {
 			
 	Chaintrain chaintrain;
 	
 	@Override
 	public void onEnable() {
 		
 		chaintrain = this;
 		new EventListener(this);
 		PluginDescriptionFile pdf = getDescription();
 		
 		this.getLogger().info("Successfully enabled!");
 		this.getLogger().info("Version: " + pdf.getVersion());
 		this.getLogger().info("By G4meM0ment (Originaly by T4sk)");
 		
 		//creates the config
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	}
 	
 	private final HashMap<String,String> data = new HashMap<String,String>();
	private final HashMap<String,Long> data2 = new HashMap<String,Long>();
 	
 	public boolean isChained(String chained)
 	{
 		return data.containsKey(chained);
 	}
 	public boolean isChained(Player chained)
 	{
 		return isChained(chained.getName());
 	}
 	
 	public void chainAll(Player[] all, Player chainer)
 	{
 		//for every player online which isn't filtered by permissions, chained will be set
 		for(Player chained:all)
 		{
 			if(!(chained.hasPermission("chaintrain.ignore") || chained.hasPermission("chaintrain.admin") || chained.isOp()))
 			chain(chained, chainer);
 		}
 	}
 	public void unchainAll(Player[] all)
 	{		
 		for(Player chained:all)
 	    {
 		   String player = chained.getName();
 		   unchain(player);
 	    }
 	}
 	
 	public void chain(Player chained,Player chainer)
 	{
 		chain(chained.getName(),chainer.getName());
 	}
 	public void chain(String player, String chainer)
 	{
 		data.put(player, chainer);
 	}
 	
 	public void chaintime(Player chained, Player chainer, String stringTime)
 	{
 		System.out.println("Angekommen");
 		chaintime(chained.getName(), chainer.getName(), stringTime);
 	}
 	public void chaintime(String player, String chainer, String stringTime)
 	{
 		int cooldown = Integer.parseInt(stringTime);
 		data.put(player, chainer);
		long diff = (System.currentTimeMillis() - data2.get(player))/60000;
		System.out.println("Debug: " + diff);
 	    if (diff < cooldown) {
 	    	unchain(player);
 	    }
 	}
 	
 	public void unchain(String player)
 	{
 		if(!isChained(player))
 			return;
 		data.remove(player);
 	}
 
 //The command block
     HashMap<String,String> allBoolean = new HashMap<String,String>();
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		String p = sender.getName();
 		Player chainer = (Player) sender;
 		
 		if(command.getName().equalsIgnoreCase("chaintrain") && args.length > 0 && args[0].equals("reload") && (chainer.hasPermission("chaintrain.command.reload") || chainer.hasPermission("chaintrain.admin") || chainer.isOp()))
 		{
 		 chaintrain.reloadConfig();
 		 chaintrain.saveConfig();
 		 chainer.sendMessage("Chaintrain reloaded!");
 		 this.getLogger().info("Reloaded config!");
 		 return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("chain") && args.length > 1 && (Bukkit.getPlayer(args[0]).isOnline() && (chainer.hasPermission("chaintrain.admin") || chainer.hasPermission("chaintrain.command.chain") || chainer.isOp())))
 		{
 			Player chained = chaintrain.getServer().getPlayer(args[0]);
 			String stringTime = args[1];
 			
			System.out.println("Debug: " + args[0] + p + stringTime);
 			
 			chaintime(args[0], p, stringTime);
			data2.put(chained.getName(), System.currentTimeMillis());
 			Messages.timeChained(chained, chainer, stringTime);
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("chain") && args.length > 0 && (Bukkit.getPlayer(args[0]).isOnline() && (chainer.hasPermission("chaintrain.admin") || chainer.hasPermission("chaintrain.command.chain") || chainer.isOp())))
 		{
 			Player chained = chaintrain.getServer().getPlayer(args[0]);
 			chaintrain.chain(args[0], p);
 			Messages.chained(chained, chainer);
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("unchain") && args.length > 0 && (Bukkit.getPlayer(args[0]).isOnline()  && (chainer.hasPermission("chaintrain.admin") || chainer.hasPermission("chaintrain.command.chain") || chainer.isOp())))
 		{
 			Player chained = chaintrain.getServer().getPlayer(args[0]);
 			if(chaintrain.isChained(args[0]))
 			{
 				chaintrain.unchain(args[0]);
 				Messages.unchained(chained, chainer);
 				return true;
 			}
 		}
 		
 		if(command.getName().equalsIgnoreCase("chain")  && (chainer.hasPermission("chaintrain.admin") || chainer.hasPermission("chaintrain.command.chain") || chainer.isOp()))
 		{
 			chaintrain.chain(p, p);
 			Messages.chained(chainer, chainer);
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("unchain")  && (chainer.hasPermission("chaintrain.admin") || chainer.hasPermission("chaintrain.command.chain") || chainer.isOp()))
 		{
 				chaintrain.unchain(p);
 				Messages.unchained(chainer, chainer);
 				return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("chainall")  && (chainer.hasPermission("chaintrain.admin") || chainer.hasPermission("chaintrain.command.chainall") || chainer.isOp()))
 		{
 			//sets chainall to active so if a player joins he will be chained too
 			allBoolean.put("chainAll", "true");
 			Player[] all = Bukkit.getServer().getOnlinePlayers();
 			chaintrain.chainAll(all, chainer);
 		}
 		
 		if(command.getName().equalsIgnoreCase("unchainall")  && (chainer.hasPermission("chaintrain.admin") || chainer.hasPermission("chaintrain.command.chainall") || chainer.isOp()))
 		{
 			allBoolean.put("chainAll", "false");
 			Player[] all = Bukkit.getServer().getOnlinePlayers();
 			chaintrain.unchainAll(all);
 		}
 		return false;		
 	}
 	
 	@Override
 	public void onDisable() {
 				
 		this.getLogger().info("Chaintrain disabled!");		
 	}
 
 }
