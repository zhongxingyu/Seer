 package me.frodenkvist.armoreditor;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.enchantment.EnchantItemEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 
 import Event.PBEntityDamageEntityEvent;
 import Event.PBEntityDamageEvent;
 import PvpBalance.PvpHandler;
 import Util.ItemUtils;
 
 public class PlayerListener implements Listener
 {
 	public ArmorEditor plugin;
 	private final int PLAYER_KILL_SCORE = 10;
 	private final int MOB_KILL_SCORE = 1;
 	private final int BLACK = 1644825;
 	private final int RED = 13388876;
 	private final int GREEN = 8375321;
 	private final int BLUE = 3368652;
 	private final int WHITE = 16777215;
 
 	private final int PURPLE = 13421772;
 	private final int TEAL = 52394;
 	private final int PINK = 14357690;
 	private final int ORANGE = 16737843;
 	private final int YELLOW = 16776960;
 	//private Location loc;
 	
 	public PlayerListener(ArmorEditor instance)
 	{
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event)
 	{
 		ItemStack is = event.getItem().getItemStack();
 		if(isLeatherArmor(is) && event.getPlayer().hasPermission("epicgear.admin"))
 		{
 			if(hasValidColor(is))
 				Store.addNameAndLore(is);
 		}
 		else if(plugin.getConfig().getInt("MoneyItem.ID") != -1 && is.getTypeId() == plugin.getConfig().getInt("MoneyItem.ID"))
 		{
 			is = Namer.setName(is, plugin.getConfig().getString("MoneyItem.Name"));
 			is = Namer.setLore(is, plugin.getConfig().getStringList("MoneyItem.Lore"));
 		}
 		
 	}
 	
 	@EventHandler
 	public void onCraftItemEvent(CraftItemEvent event)
 	{
 		Player player = (Player) event.getWhoClicked();
 		if(player.hasPermission("epicgear.craft") || player.hasPermission("epicgear.admin"))
 			return;
 		Inventory inv = event.getInventory();
 		boolean armor = false;
 		boolean dye = false;
 		for(ItemStack is : inv.getContents())
 		{
 			if(is.getTypeId() == 299)
 				armor = true;
 			if(is.getTypeId() == 298)
 				armor = true;
 			if(is.getTypeId() == 300)
 				armor = true;
 			if(is.getTypeId() == 301)
 				armor = true;
 			if(is.getTypeId() == 351)
 				dye = true;
 			if(armor && dye)
 				event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onEnchantItemEvent(EnchantItemEvent event)
 	{
 		//Bukkit.broadcastMessage("fork");
 		ItemStack is = event.getItem();
 		//EpicArmor ea = Store.getEpicArmor(is);
 		if(isLeatherArmor(is))
 		{
 			//Bukkit.broadcastMessage("" + ea.canEnchant());
 			
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
 	public void onPBEntityDamageByEntityEvent(PBEntityDamageEntityEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		if(event.getEntity() instanceof Player)
 		{
 			Player player = (Player)event.getEntity();
 			for(ItemStack is : player.getInventory().getArmorContents())
 			{
 				Store.changeLore(is);
 				if(player.getNoDamageTicks() > 10)
 					break;
 				EpicGear eg = Store.getEpicGear(is);
 				if(eg == null)
 					continue;
 				double dur = Store.getDurability(is);
 				if(is.getDurability() == 0)
 					dur = eg.getDurability();
 				if(dur == -1)
 				{
 					dur = eg.getDurability();
 					--dur;
 					List<String> list = new ArrayList<String>();
 					list.add("&kdur:" + dur);
 					Iterator<String> itr = ItemUtils.getLore(is).iterator();
 					while(itr.hasNext())
 					{
 						list.add(itr.next());
 					}
 					ItemUtils.setLore(is, list);
 					Store.setDurability(is, dur);
 					continue;
 				}
 				--dur;
 				if(dur <= 0)
 				{
 					is.setDurability(is.getType().getMaxDurability());
 					continue;
 				}
 				if(dur > eg.getDurability())
 					Store.setDurability(is, eg.getDurability());
 				else
 					Store.setDurability(is, dur);
 			}
 		}
 		//if(!(event.getEntity() instanceof Player))
 		//	loc = event.getEntity().getLocation();
 		if(!(event.getEntity() instanceof LivingEntity))
 			return;
 		if(((LivingEntity)event.getEntity()).getNoDamageTicks() > 10)
 			return;
 		if(!(event.getDamager() instanceof Player))
 			return;
 		Player damager = (Player)event.getDamager();
 		
 		for(ItemStack is : damager.getInventory().getArmorContents())
 		{
 			Store.changeLore(is);
 			EpicGear eg = Store.getEpicGear(is);
 			if(eg == null)
 				continue;
 			eg.addDrinkPotionEffect(damager);
 			if(event.getEntity() instanceof LivingEntity)
 				eg.addSplashPotionEffect((LivingEntity)event.getEntity());
 			/*double dur = Store.getDurability(is);
 			if(dur == -1)
 			{
 				dur = eg.getDurability();
 				--dur;
 				List<String> list = new ArrayList<String>();
 				list.add("&kdur:" + dur);
 				Iterator<String> itr = ItemUtils.getLore(is).iterator();
 				while(itr.hasNext())
 				{
 					list.add(itr.next());
 				}
 				ItemUtils.setLore(is, list);
 				Store.setDurability(is, dur);
 				continue;
 			}
 			--dur;
 			if(dur <= 0)
 			{
 				is.setDurability(is.getType().getMaxDurability());
 				continue;
 			}
 			if(dur > eg.getDurability())
 				Store.setDurability(is, eg.getDurability());
 			else
 				Store.setDurability(is, dur);*/
 		}
 		
 		ItemStack is = damager.getItemInHand();
 		EpicWeapon ew = Store.getEpicWeapon(is);
 		if(ew == null)
 		{
 			if(is.getType().equals(Material.WOOD_HOE) || is.getType().equals(Material.STONE_HOE) || is.getType().equals(Material.IRON_HOE) || is.getType().equals(Material.GOLD_HOE)
 					 || is.getType().equals(Material.DIAMOND_HOE))
 			{
 				Iterator<Enchantment> itr = is.getEnchantments().keySet().iterator();
 				while(itr.hasNext())
 				{
 					Enchantment temp = itr.next();
 					if(temp.getName().equalsIgnoreCase("durability"))
 					{
 						double chance = 100D/(is.getEnchantments().get(temp)+1);
 						chance = chance/100D;
 						if(Math.random() <= chance)
 						{
 							is.setDurability((short) (is.getDurability()+1));
 							if(is.getType().getMaxDurability() <= is.getDurability())
 								is = null;
 						}
 						return;
 					}
 				}
 				is.setDurability((short) (is.getDurability()+1));
 				if(is.getType().getMaxDurability() <= is.getDurability())
 					is = null;
 				return;
 			}
			return;
 		}
 		ew.addDrinkPotionEffect(damager);
 		if(event.getEntity() instanceof LivingEntity)
 			ew.addSplashPotionEffect((LivingEntity)event.getEntity());
 		double dur = Store.getDurability(is);
 		if(is.getDurability() == 0)
 			dur = ew.getDurability();
 		if(dur == -1)
 		{
 			dur = ew.getDurability();
 			--dur;
 			List<String> list = new ArrayList<String>();
 			list.add("&kdur:" + dur);
 			Iterator<String> itr = ItemUtils.getLore(is).iterator();
 			while(itr.hasNext())
 			{
 				list.add(itr.next());
 			}
 			ItemUtils.setLore(is, list);
 			Store.setDurability(is, dur);
 			return;
 		}
 		--dur;
 		if(dur <= 0)
 		{
 			is.setDurability(is.getType().getMaxDurability());
 			return;
 		}
 		if(dur > ew.getDurability())
 			Store.setDurability(is, ew.getDurability());
 		else
 			Store.setDurability(is, dur);
 	}
 	
 	@EventHandler
 	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		if(!(event.getDamager() instanceof Player))
 			return;
 		if(!(event.getEntity() instanceof LivingEntity))
 			return;
 		if((event.getEntity() instanceof Player))
 			return;
 		if(((LivingEntity)event.getEntity()).getNoDamageTicks() > 10)
 			return;
 		
 		Player damager = (Player)event.getDamager();
 		
 		for(ItemStack is : damager.getInventory().getArmorContents())
 		{
 			Store.changeLore(is);
 			EpicGear eg = Store.getEpicGear(is);
 			if(eg == null)
 				continue;
 			
 			eg.addDrinkPotionEffect(damager);
 			if(event.getEntity() instanceof LivingEntity)
 				eg.addSplashPotionEffect((LivingEntity)event.getEntity());
 			
 			/*double dur = Store.getDurability(is);
 			if(dur == -1)
 			{
 				dur = eg.getDurability();
 				--dur;
 				List<String> list = new ArrayList<String>();
 				list.add("&kdur:" + dur);
 				Iterator<String> itr = ItemUtils.getLore(is).iterator();
 				while(itr.hasNext())
 				{
 					list.add(itr.next());
 				}
 				ItemUtils.setLore(is, list);
 				Store.setDurability(is, dur);
 				continue;
 			}
 			--dur;
 			if(dur <= 0)
 			{
 				is.setDurability(is.getType().getMaxDurability());
 				continue;
 			}
 			if(dur > eg.getDurability())
 				Store.setDurability(is, eg.getDurability());
 			else
 				Store.setDurability(is, dur);*/
 		}
 		ItemStack is = damager.getItemInHand();
 		EpicWeapon ew = Store.getEpicWeapon(is);
 		if(ew == null)
 		{
 			if(is.getType().equals(Material.WOOD_HOE) || is.getType().equals(Material.STONE_HOE) || is.getType().equals(Material.IRON_HOE) || is.getType().equals(Material.GOLD_HOE)
 					 || is.getType().equals(Material.DIAMOND_HOE))
 			{
 				Iterator<Enchantment> itr = is.getEnchantments().keySet().iterator();
 				while(itr.hasNext())
 				{
 					Enchantment temp = itr.next();
 					if(temp.getName().equalsIgnoreCase("durability"))
 					{
 						double chance = 100D/(is.getEnchantments().get(temp)+1);
 						chance = chance/100D;
 						if(Math.random() <= chance)
 						{
 							is.setDurability((short) (is.getDurability()+1));
 							if(is.getType().getMaxDurability() <= is.getDurability())
 								is = null;
 						}
 						return;
 					}
 				}
 				is.setDurability((short) (is.getDurability()+1));
 				if(is.getType().getMaxDurability() <= is.getDurability())
 					is = null;
 				return;
 			}
			return;
 		}
 		ew.addDrinkPotionEffect(damager);
 		if(event.getEntity() instanceof LivingEntity)
 			ew.addSplashPotionEffect((LivingEntity)event.getEntity());
 		double dur = Store.getDurability(is);
 		if(is.getDurability() == 0)
 			dur = ew.getDurability();
 		if(dur == -1)
 		{
 			dur = ew.getDurability();
 			--dur;
 			List<String> list = new ArrayList<String>();
 			list.add("&kdur:" + dur);
 			Iterator<String> itr = ItemUtils.getLore(is).iterator();
 			while(itr.hasNext())
 			{
 				list.add(itr.next());
 			}
 			ItemUtils.setLore(is, list);
 			Store.setDurability(is, dur);
 			return;
 		}
 		--dur;
 		if(dur <= 0)
 		{
 			is.setDurability(is.getType().getMaxDurability());
 			return;
 		}
 		if(dur > ew.getDurability())
 			Store.setDurability(is, ew.getDurability());
 		else
 			Store.setDurability(is, dur);
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPBEntityDamageEvent(PBEntityDamageEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		if(event instanceof PBEntityDamageEntityEvent)
 			return;
 		if(!(event.getEntity() instanceof Player))
 			return;
 		if(event.getCause().equals(DamageCause.DROWNING) || event.getCause().equals(DamageCause.FALL) || event.getCause().equals(DamageCause.FALLING_BLOCK)
 				|| event.getCause().equals(DamageCause.MAGIC) || event.getCause().equals(DamageCause.POISON) || event.getCause().equals(DamageCause.STARVATION)
 				|| event.getCause().equals(DamageCause.SUFFOCATION) || event.getCause().equals(DamageCause.WITHER))
 			return;
 		Player player = (Player)event.getEntity();
 		if(player.getNoDamageTicks() > 10)
 			return;
 		if(event.getCause().equals(DamageCause.CONTACT) || event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.LAVA)
 				|| event.getCause().equals(DamageCause.FIRE_TICK))
 		{
 			event.setCancelled(true);
 			player.damage(0D);
 			PvpHandler.getPvpPlayer(player).uncheckedDamage(event.getDamage());
 			return;
 		}
 		
 		for(ItemStack is : player.getInventory().getArmorContents())
 		{
 			Store.changeLore(is);
 			EpicGear eg = Store.getEpicGear(is);
 			if(eg == null)
 				continue;
 			
 			double dur = Store.getDurability(is);
 			if(is.getDurability() == 0)
 				dur = eg.getDurability();
 			if(dur == -1)
 			{
 				dur = eg.getDurability();
 				--dur;
 				List<String> list = new ArrayList<String>();
 				list.add("&kdur:" + dur);
 				Iterator<String> itr = ItemUtils.getLore(is).iterator();
 				while(itr.hasNext())
 				{
 					list.add(itr.next());
 				}
 				ItemUtils.setLore(is, list);
 				Store.setDurability(is, dur);
 				continue;
 			}
 			--dur;
 			if(dur <= 0)
 			{
 				is.setDurability(is.getType().getMaxDurability());
 				continue;
 			}
 			if(dur > eg.getDurability())
 				Store.setDurability(is, eg.getDurability());
 			else
 				Store.setDurability(is, dur);
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event)
 	{
 		ItemStack is = event.getCurrentItem();
 	    if(is == null)
 	    	return;
 	    EpicGear eg = Store.getEpicGear(is);
 		if(eg == null)
 			return;
 		if(is.getDurability() == 0)
 		{
 			//Bukkit.broadcastMessage("" + is.getDurability());
 			Store.setDurability(is, eg.getDurability());
 			//Bukkit.broadcastMessage("" + is.getDurability());
 		}
 		String s = Store.getDecay(is);
 		if(s != null)
 		{
 			int dayNum = new Date().getDay();
 			String[] split = s.split(":");
 			if(split.length == 2)
 			{
 				if(dayNum != Integer.valueOf(split[1]))
 				{
 					if(split[0].equalsIgnoreCase("1"))
 					{
 						is = null;
 						return;
 					}
 					Store.setDecay(is, Integer.valueOf(split[0])-1, dayNum);
 				}
 			}
 		}
 		//if(!event.isShiftClick() || event.getSlotType().equals(SlotType.ARMOR))
 		//{
 		//	return;
     	//}
 		//Bukkit.broadcastMessage("" + is.getDurability());
 		//if(is.getDurability() != 0)
 		//	return;
 		//Store.setDurability(is, eg.getDurability());
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onPlayerItemHeldEvent(PlayerItemHeldEvent event)
 	{
 		ItemStack is = event.getPlayer().getInventory().getItem(event.getNewSlot());
 	    if(is == null)
 	    	return;
 	    String s = Store.getDecay(is);
 		if(s != null)
 		{
 			int dayNum = new Date().getDay();
 			String[] split = s.split(":");
 			if(split.length == 2)
 			{
 				if(dayNum != Integer.valueOf(split[1]))
 				{
 					if(split[0].equalsIgnoreCase("1"))
 					{
 						is = null;
 						return;
 					}
 					Store.setDecay(is, Integer.valueOf(split[0])-1, dayNum);
 				}
 			}
 		}
 	    EpicGear eg = Store.getEpicGear(is);
 		if(eg == null)
 			return;
 		if(is.getDurability() != 0)
 			return;
 		Store.setDurability(is, eg.getDurability());
 	}
 	
 	@EventHandler
 	public void onPlayerJoinEvent(PlayerJoinEvent event)
 	{
 		if(plugin.getConfig().getBoolean("UpdateCheckerEnabled"))
 		{
 			if(event.getPlayer().hasPermission("epicgear.update") || event.getPlayer().hasPermission("epicgear.admin"))
 			{
 				if(plugin.uc.isUpdateNeeded())
 				{
 					event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "New Version Of EpicGear Available: " + plugin.uc.getVersion());
 					event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Get The New Version At: " + plugin.uc.getLink());
 				}
 			}
 		}
 		AEHandler.addPlayer(new AEPlayer(event.getPlayer()));
 	}
 	
 	@EventHandler
 	public void onPlayerQuitEvent(PlayerQuitEvent event)
 	{
 		AEHandler.removePlayer(event.getPlayer().getName());
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onEntityDeathEvent(EntityDeathEvent event)
 	{
 		LivingEntity le = event.getEntity();
 		if(le instanceof Player)
 		{
 			Player killed = (Player)le;
 			final AEPlayer aePlayer = AEHandler.getPlayer(killed);
 			
 			double chance = AEHandler.getDeathRemoveChance();
 			try
 			{
 				Iterator<ItemStack> itr = event.getDrops().iterator();
 				while(itr.hasNext())
 				{
 					ItemStack is = itr.next();
 					EpicGear eg = Store.getEpicGear(is);
 					if(eg != null)
 					{
 						if(eg.getDontDropOnDeath())
 						{
 							killed.getEnderChest().addItem(is);
 						}
 						else if(Math.random() <= chance)
 							event.getDrops().remove(is);
 					}
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 			Player killer = killed.getKiller();
 			if(killer == null)
 				return;
 			if(!Store.isEpicWeapon(killer.getItemInHand()))
 				return;
 			AEPlayer aeKiller = AEHandler.getPlayer(killer);
 			aeKiller.addKillCounter(PLAYER_KILL_SCORE);
 			if(aeKiller.getKillCounter() >= 50)
 			{
 				killer.sendMessage(ChatColor.BLUE +"["+ChatColor.LIGHT_PURPLE + "EpicWeapon" + ChatColor.BLUE + "]: " + ChatColor.YELLOW + 
 						"Your charge is " + ChatColor.GREEN + "FULL" + ChatColor.YELLOW + "TO USE" +ChatColor.GREEN +  " Shift-RightClick");
 			}
 			else
 				killer.sendMessage(ChatColor.BLUE +"[" + ChatColor.LIGHT_PURPLE + "EpicWeapon" + ChatColor.BLUE+"]: " + ChatColor.YELLOW +
 						"Your charge is " + ChatColor.RED + aeKiller.getKillCounter() +" / 50" + ChatColor.YELLOW + " kill more things to increase it!");
 			Bukkit.getScheduler().scheduleSyncDelayedTask(ArmorEditor.plugin, new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					try
 					{
 						aePlayer.setKillCounter(0);
 					}
 					catch(Exception e)
 					{
 						
 					}
 				}
 			},1L);
 		}
 		else
 		{
 			Player killer = le.getKiller();
 			if(killer == null)
 				return;
 			if(!Store.isEpicWeapon(killer.getItemInHand()))
 				return;
 			AEPlayer aeKiller = AEHandler.getPlayer(killer);
 			aeKiller.addKillCounter(MOB_KILL_SCORE);
 			if(aeKiller.getKillCounter() >= 50)
 			{
 				killer.sendMessage(ChatColor.BLUE +"["+ChatColor.LIGHT_PURPLE + "EpicWeapon" + ChatColor.BLUE + "]: " + ChatColor.YELLOW + 
 						"Your charge is " + ChatColor.GREEN + "FULL" + ChatColor.YELLOW + "TO USE" +ChatColor.GREEN +  " Shift-RightClick");
 			}
 			else
 				killer.sendMessage(ChatColor.BLUE +"[" + ChatColor.LIGHT_PURPLE + "EpicWeapon" + ChatColor.BLUE+"]: " + ChatColor.YELLOW +
 						"Your charge is " + ChatColor.RED + aeKiller.getKillCounter() +" / 50" + ChatColor.YELLOW + " kill more things to increase it!");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEvent(PlayerInteractEvent event)
 	{
 		if(!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
 			return;
 		Player player = event.getPlayer();
 		if(!player.isSneaking())
 			return;
 		ItemStack is = player.getItemInHand();
 		if( is == null)
 			return;
 		EpicWeapon ew = Store.getEpicWeapon(is);
 		if(ew == null)
 			return;
 		if(ew.useSkill(AEHandler.getPlayer(player)))
 		{
 			for(Entity en : player.getNearbyEntities(50, 50, 50))
 			{
 				if(!(en instanceof Player))
 					continue;
 				((Player)en).sendMessage(ChatColor.BLUE +"[" + ChatColor.LIGHT_PURPLE + "EpicWeapon" + ChatColor.BLUE+"]: " + ChatColor.YELLOW + player.getName() + " Has Casted " + ew.getSkillName());
 			}
 			player.sendMessage(ChatColor.BLUE +"[" + ChatColor.LIGHT_PURPLE + "EpicWeapon" + ChatColor.BLUE+"]: " + ChatColor.YELLOW + player.getName() + " Has Casted " + ew.getSkillName());
 		}
 	}
 	
 	public int getColor(ItemStack item)
 	{
 		LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
 		return lam.getColor().asRGB();
 	}
 	
 	private ItemStack setColor(ItemStack item, int color)
 	{
 		LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
 		lam.setColor(Color.fromRGB(color));
 		item.setItemMeta(lam);
 		return item;
 	}
 
 	public boolean isLeatherArmor(ItemStack is)
 	{
 		if(is == null)
 			return false;
 		Material ma = is.getType();
 		if(ma == null)
 			return false;
 		if(ma.equals(Material.LEATHER_HELMET) || ma.equals(Material.LEATHER_CHESTPLATE) || ma.equals(Material.LEATHER_LEGGINGS) || ma.equals(Material.LEATHER_BOOTS))
 			return true;
 		return false;
 	}
 
 	public boolean compare(ItemStack is1, ItemStack is2)
 	{
 		LeatherArmorMeta lam1 = (LeatherArmorMeta) is1.getItemMeta();
 		LeatherArmorMeta lam2 = (LeatherArmorMeta) is2.getItemMeta();
 		
 		int co1 = lam1.getColor().asRGB();
 		int co2 = lam2.getColor().asRGB();
 		
 		if(co1 == co2)
 			return true;
 		else
 			return false;
 	}
 	
 	/*private boolean isWeapon(ItemStack is)
 	{
 		if(is == null)
 			return false;
 		int ID = is.getTypeId();
 		if(ID == 268 || ID == 267 || ID == 276 || ID == 283 || ID == 271 || ID == 275 || ID == 286 || ID == 279)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean isArmor(ItemStack is)
 	{
 		if(is == null)
 			return false;
 		switch(is.getTypeId())
 		{
 		case 298:
 		case 299:
 		case 300:
 		case 301:
 		case 302:
 		case 303:
 		case 304:
 		case 305:
 		case 306:
 		case 307:
 		case 308:
 		case 309:
 		case 310:
 		case 311:
 		case 312:
 		case 313:
 		case 314:
 		case 315:
 		case 316:
 		case 317:
 			return true;
 		default:
 			return false;	
 		}
 	}
 	
 	/*public void activateSkill(String skill, Location target, Player player)
 	{
 		if(skill.equalsIgnoreCase("lightningstorm"))
 		{
 			new SkillLightningStorm(target,player).run();
 		}
 		else if(skill.equalsIgnoreCase("firestorm"))
 		{
 			new SkillFireStorm(target,player).run();
 		}
 		else if(skill.equalsIgnoreCase("witherstorm"))
 		{
 			new SkillWitherStorm(target,player).run();
 		}
 	}*/
 	
 	public String getColorName(ItemStack is)
 	{
 		LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
 		int c = lam.getColor().asRGB();
 		switch(c)
 		{
 		case BLACK:
 			return "Black";
 		case RED:
 			return "Red";
 		case GREEN:
 			return "Green";
 		case BLUE:
 			return "Blue";
 		case WHITE:
 			return "White";
 		case PURPLE:
 			return "Purple";
 		case TEAL:
 			return "Teal";
 		case PINK:
 			return "Pink";
 		case ORANGE:
 			return "Orange";
 		case YELLOW:
 			return "Yellow";
 		default:
 			return null;
 		}
 	}
 	
 	public ItemStack setColor(ItemStack is, String color)
 	{
 		if(color.equalsIgnoreCase("black"))
 		{
 			return setColor(is,BLACK);
 		}
 		else if(color.equalsIgnoreCase("red"))
 		{
 			return setColor(is,RED);
 		}
 		else if(color.equalsIgnoreCase("green"))
 		{
 			return setColor(is,GREEN);
 		}
 		else if(color.equalsIgnoreCase("blue"))
 		{
 			return setColor(is,BLUE);
 		}
 		else if(color.equalsIgnoreCase("white"))
 		{
 			return setColor(is,WHITE);
 		}
 		else if(color.equalsIgnoreCase("purple"))
 		{
 			return setColor(is,PURPLE);
 		}
 		else if(color.equalsIgnoreCase("teal"))
 		{
 			return setColor(is,TEAL);
 		}
 		else if(color.equalsIgnoreCase("pink"))
 		{
 			return setColor(is,PINK);
 		}
 		else if(color.equalsIgnoreCase("orange"))
 		{
 			return setColor(is,ORANGE);
 		}
 		else if(color.equalsIgnoreCase("yellow"))
 		{
 			return setColor(is,YELLOW);
 		}
 		return null;
 	}
 	
 	public boolean isColor(String color)
 	{
 		if(color.equalsIgnoreCase("black"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("red"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("green"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("blue"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("white"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("purple"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("teal"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("pink"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("orange"))
 		{
 			return true;
 		}
 		else if(color.equalsIgnoreCase("yellow"))
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean hasValidColor(ItemStack is)
 	{
 		LeatherArmorMeta lam = (LeatherArmorMeta)is.getItemMeta();
 		if(lam.getColor().asRGB() == BLACK)
 			return true;
 		else if(lam.getColor().asRGB() == RED)
 			return true;
 		else if(lam.getColor().asRGB() == GREEN)
 			return true;
 		else if(lam.getColor().asRGB() == BLUE)
 			return true;
 		else if(lam.getColor().asRGB() == WHITE)
 			return true;
 		else if(lam.getColor().asRGB() == PURPLE)
 			return true;
 		else if(lam.getColor().asRGB() == TEAL)
 			return true;
 		else if(lam.getColor().asRGB() == PINK)
 			return true;
 		else if(lam.getColor().asRGB() == ORANGE)
 			return true;
 		else if(lam.getColor().asRGB() == YELLOW)
 			return true;
 		return false;
 	}
 }
