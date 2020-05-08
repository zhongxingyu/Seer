 package net.stormdev.ucars.trade;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.stormdev.ucars.stats.HandlingDamagedStat;
 import net.stormdev.ucars.stats.HealthStat;
 import net.stormdev.ucars.stats.NameStat;
 import net.stormdev.ucars.stats.SpeedStat;
 import net.stormdev.ucars.stats.Stat;
 import net.stormdev.ucars.stats.StatType;
 import net.stormdev.ucars.utils.Car;
 import net.stormdev.ucars.utils.CarForSale;
 import net.stormdev.ucars.utils.CarGenerator;
 import net.stormdev.ucars.utils.CarValueCalculator;
 import net.stormdev.ucars.utils.Displays;
 import net.stormdev.ucars.utils.IconMenu;
 import net.stormdev.ucars.utils.InputMenu;
 import net.stormdev.ucars.utils.InputMenu.OptionClickEvent;
 import net.stormdev.ucars.utils.InputMenuClickEvent;
 import net.stormdev.ucars.utils.TradeBoothClickEvent;
 import net.stormdev.ucars.utils.TradeBoothMenuType;
 import net.stormdev.ucars.utils.UpgradeForSale;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Bat;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryAction;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.event.vehicle.VehicleUpdateEvent;
 import org.bukkit.inventory.AnvilInventory;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.util.Vector;
 
 import com.useful.uCarsAPI.uCarsAPI;
 import com.useful.ucars.CarHealthData;
 import com.useful.ucars.ClosestFace;
 import com.useful.ucars.Lang;
 import com.useful.ucars.PlaceManager;
 import com.useful.ucars.ucarDeathEvent;
 import com.useful.ucars.ucars;
 import com.useful.ucarsCommon.StatValue;
 
 public class UTradeListener implements Listener {
 	main plugin = null;
 	private double hovercarHeightLimit = 256;
 	public UTradeListener(main plugin){
 		this.plugin = plugin;
 		hovercarHeightLimit = main.config.getDouble("general.hoverCar.heightLimit");
 	}
 	@EventHandler
 	void displayUpgrades(VehicleUpdateEvent event){
 		Vehicle veh = event.getVehicle();
 		if(!(veh instanceof Minecart)){
 			return;
 		}
 		Minecart car = (Minecart) veh;
 		Location loc = car.getLocation();
 		Entity passenger = car.getPassenger();
 		if(passenger == null){
 			return;
 		}
 		if(passenger instanceof Boat){
 			//Float
 			Block b = loc.getBlock();
 			Vector vel = car.getVelocity();
 			Boolean inWater = false;
 			if(b.isLiquid()){
 				inWater = true;
 				if(vel.getY() < 0.5){
 					vel.setY(0.5);
 				}
 			}
 			else if(b.getRelative(BlockFace.DOWN).isLiquid()){
 				inWater = true;
 				vel.setY(0);
 			}
 			if(inWater){
 			BlockFace f = ClosestFace.getClosestFace(loc.getYaw());
 			if(f != BlockFace.UP && f != BlockFace.DOWN){
 				Block toGo = b.getRelative(f);
 				if(!toGo.isLiquid() && !toGo.isEmpty()){
 					//Let the car re-enter land
 					vel.setY(0.1);
 				}
 			}
 			}
 			car.setVelocity(vel);
 			return;
 		}
 		else if(passenger instanceof Bat && car.hasMetadata("trade.hover")){
 			if(passenger.getPassenger() == null){
 				//No empty hovercars allowed to fly
 				return;
 			}
 			//Hover
 			Block b = loc.getBlock();
 			Vector vel = car.getVelocity();
 			Block under = b.getRelative(BlockFace.DOWN);
 			Block under2 = b.getRelative(BlockFace.DOWN,2);
 			Boolean descending = car.hasMetadata("car.braking");
 			Boolean ascending = car.hasMetadata("car.action");
 			int count = 0;
 			if(!b.isEmpty()){
 				count++;
 			}
 			if(!under.isEmpty()){
 				count++;
 			}
 			if(!under2.isEmpty()){
 				count++;
 			}
 			switch(count){
 			case 0:vel.setY(-0.3);under.getWorld().playEffect(under.getLocation(), Effect.SMOKE, 1); break;
 			case 1:vel.setY(2); break;
 			case 2:vel.setY(1); break;
 			case 3:vel.setY(0.1);under.getWorld().playEffect(under.getLocation(), Effect.SMOKE, 1); break;
 			}
 			if(descending && ascending){
 				vel.setY(0);
 			}
 			else if(descending){
 				vel.setY(-0.5);
 			}
 			else if(ascending){
 			    vel.setY(0.5);	
 			}
 			if((loc.getY() < hovercarHeightLimit) || descending){
 				car.setVelocity(vel);
 			}
 			else{
 				Entity p = car.getPassenger();
 				while(p!=null && !(p instanceof Player) 
 						&& p.getPassenger() != null){
 					p = p.getPassenger();
 				}
 				if(p!=null && p instanceof Player){
					String msg = net.stormdev.ucars.trade.Lang.get("general.hovercar.heightLimit");
 					((Player)p).sendMessage(main.colors.getInfo()+msg);
 				}
 			}
 			return;
 		}
 		return;
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	void itemCraft(CraftItemEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		ItemStack recipe = event.getCurrentItem();
 		if(!(recipe.getType() == Material.MINECART)){
 			return;
 		}
 		if(!ChatColor.stripColor(recipe.getItemMeta().getDisplayName()).equalsIgnoreCase("car")){
 			return;
 		}
 		Car car = CarGenerator.gen();
         event.setCurrentItem(car.getItem());
         main.plugin.carSaver.cars.put(car.getId(), car);
         main.plugin.carSaver.save();
 		return;
 	}
 	@EventHandler (priority = EventPriority.MONITOR)
 	void carUpgradeAnvil(InventoryClickEvent event){
 		if(event.getAction()==InventoryAction.CLONE_STACK){
 			ItemStack cloned = event.getCursor();
 			if(cloned.getType() == Material.MINECART || 
 					cloned.getItemMeta() == null ||
 					cloned.getItemMeta().getLore() == null ||
 					cloned.getItemMeta().getLore().size() < 2){
 				event.setCancelled(true);
 				return;
 			}
 			return;
 		}
 		final Player player = (Player) event.getWhoClicked();
 		InventoryView view = event.getView();
 		final Inventory i = event.getInventory();
 		if(!(i instanceof AnvilInventory)){
 			return;
 		}
 		int slotNumber = event.getRawSlot();
 		if(!(slotNumber == view.convertSlot(slotNumber))){
 			//Not clicking in the anvil
 			return;
 		}
 		//AnvilInventory i = (AnvilInventory) inv;
 		Boolean update = true;
 		Boolean save = false;
 		Boolean pickup = false;
 		if(event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME){
 			update = false;
 			pickup = true;
 			if(slotNumber == 2){ //Result slot
 				save = true;
 			}
 		}
 		ItemStack carItem = null;
 		try {
 			carItem = i.getItem(0);
 		} catch (Exception e) {
 			return;
 		}
 		if(carItem == null){
 			return;
 		}
 		if(!(carItem.getType() == Material.MINECART) || 
 				carItem.getItemMeta().getLore().size() < 2){
 			return; //Not a car
 		}
 		//Anvil contains a car in first slot.
 		ItemMeta meta = carItem.getItemMeta();
 		List<String> lore = meta.getLore();
 		final UUID id;
 		try {
 			if(lore.size() < 1){
 				return;
 			}
 			id = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 		} catch (Exception e) {
 			return;
 		}
 		Car car = null;
 		if(!plugin.carSaver.cars.containsKey(id)){
 			return;
 		}
 		car = plugin.carSaver.cars.get(id);
 		final HashMap<String, Stat> stats = car.getStats();
         if(save && slotNumber ==2){
 			//They are renaming it
         	ItemStack result = event.getCurrentItem();
         	String name = ChatColor.stripColor(result.getItemMeta().getDisplayName());
         	stats.put("trade.name", new NameStat(name, plugin));
         	car.setStats(stats);
         	plugin.carSaver.cars.remove(id);
         	plugin.carSaver.cars.put(id, car);
         	plugin.carSaver.save();
         	player.sendMessage(main.colors.getSuccess()+"+"+main.colors.getInfo()+" Renamed car to: '"+name+"'");
         	return;
 		}
 		InventoryAction a = event.getAction();
 		ItemStack upgrade = null;
 		Boolean set = false;
 		final ItemStack up = upgrade;
 		final Boolean updat = update;
 		final Boolean sav = save;
 		final Car ca = car;
 		if(slotNumber == 1 && (a==InventoryAction.PLACE_ALL || a==InventoryAction.PLACE_ONE || a==InventoryAction.PLACE_SOME) && event.getCursor().getType()!=Material.AIR){
 			//upgrade = event.getCursor().clone();
 			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
 
 				public void run() {
 					ItemStack upgrade = up;
 					try {
 						upgrade = i.getItem(1); //Upgrade slot
 					} catch (Exception e) {
 						return;
 					}
 					if(upgrade == null){
 						return;
 					}
 					//A dirty trick to get the inventory to look correct on the client
 					applyUpgrades(upgrade, stats, ca, updat, sav, player, i, id);
 					return;
 				}}, 1l);
 			set = true;
 			return;
 		}
 		if(!set){
 		    try {
 				upgrade = i.getItem(1); //Upgrade slot
 			} catch (Exception e) {
 				return;
 			}
 		}
 		if(upgrade == null){
 			return;
 		}
 		if(pickup && slotNumber == 1){
 			return; //Don't bother tracking and updating, etc...
 		} 
 		applyUpgrades(upgrade, stats, car, update, save, player, i, id);
 		return;
 	}
 	@SuppressWarnings("deprecation")
 	public void applyUpgrades(ItemStack upgrade, HashMap<String, Stat> stats, Car car, Boolean update, Boolean save, Player player, Inventory i, UUID carId){
 		   String upgradeMsg = net.stormdev.ucars.trade.Lang.get("general.upgrade.msg");
 		   if(upgrade.getType() == Material.IRON_BLOCK){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 					health = stat.getHealth();
 				}
 				double bonus = (9*upgrade.getAmount());
 				health = health + bonus; //Add 9 to health stat
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
 				player.sendMessage(upgradeMsg);
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else if(upgrade.getType() == Material.IRON_INGOT){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 					health = stat.getHealth();
 				}
 				double bonus = 1*upgrade.getAmount();
 				health = health + bonus; //Add 1 to health stat
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
 				player.sendMessage(upgradeMsg);
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else if(upgrade.getType() == Material.LEVER){
 				stats.remove("trade.handling"); //Fix handling if broken
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), "Fixed");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "all damage to the car");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), "Undamaged");
 				player.sendMessage(upgradeMsg);
 				upgrade.setAmount(0);
 			}
 			else if(upgrade.getType() == Material.REDSTONE){
 				//Increment speed
 				double speed = 1;
 				SpeedStat speedStat = new SpeedStat(speed, plugin);
 				if(stats.containsKey("trade.speed")){
 					speedStat = (SpeedStat) stats.get("trade.speed");
 					speed = speedStat.getSpeedMultiplier();
 				}
 				speed = speed + (0.05*upgrade.getAmount());
 				speed = speed * 100; //0.05 -> 5
 				speed = Math.round(speed);
 				speed = speed / 100; //5 -> 0.05
 				if(speed > 5){
 					speed = 5;
 				}
 				speedStat.setSpeedMultiplier(speed);
 				stats.put("trade.speed", speedStat);
 				upgradeMsg = ucars.colorise(upgradeMsg);
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), (0.05*upgrade.getAmount())+"x");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
 				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+"x (Max: "+5+"x)");
 				player.sendMessage(upgradeMsg);
 				upgrade.setAmount(0);
 			}
 			else{
 				//Invalid item
 				return;
 			}
 			i.clear(1);
 			if(update){
 				car.setStats(stats);
 				i.setItem(0, car.getItem());
 				plugin.carSaver.cars.remove(carId);
 				plugin.carSaver.cars.put(carId, car);
 				plugin.carSaver.save();
 				player.updateInventory();
 			}
 			return;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	void carStackRemoval(VehicleDestroyEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Vehicle v = event.getVehicle();
 		if(v instanceof Minecart){
 			//Read up the stack and remove all
 			Minecart car = (Minecart)v;
 			Entity top = car;
 			Location loc = car.getLocation();
 			while(top.getPassenger() != null){
 				top = top.getPassenger();
 			}
 			while(top.getVehicle() != null){
 				Entity veh = top.getVehicle();
 				top.remove();
 				top = veh;
 			}
 			top.remove();
 			loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
 		}
 		return;
 	}
 	
 	@EventHandler (priority = EventPriority.HIGH)
 	void enterCar(PlayerInteractEntityEvent event){
 		//Enter things such as pigucarts
 		if(event.isCancelled()){
 			return;
 		}
 		Entity clicked = event.getRightClicked();
 		if(!(clicked instanceof Minecart)){
 			Minecart m = isEntityInCar(clicked);
 			if(m != null){
 				clicked = m;
 			}
 			else{
 				return;
 			}
 		}
 		Minecart car = (Minecart) clicked;
 		if(car.getPassenger() == null
 				|| car.getPassenger() instanceof Player){
 			return;
 		}
 		Entity top = car;
 		while(top.getPassenger() != null
 				&& !(top.getPassenger() instanceof Player)){
 			top = top.getPassenger();
 		}
 		top.setPassenger(event.getPlayer());
 		event.setCancelled(true);
 		return;
 	}
 	/*
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGHEST)
 	void carUpgradeRecipes(InventoryClickEvent event){
 		Inventory inv = event.getInventory();
 		if(!(inv instanceof CraftingInventory)){
 			return;
 		}
 		ItemStack[] items = inv.getContents().clone();
 		ArrayList<ItemStack> upgradeItems = new ArrayList<ItemStack>();
 		Boolean isUpgrade = false;
 		ItemStack car = null;
 		for(ItemStack item:items){
 			if(item.getType().equals(Material.MINECART)){
 				if(item.getDurability() > 19){
 					isUpgrade  = true;
 					car = item;
 				}
 			}
 			upgradeItems.add(item);
 		}
 		if(!isUpgrade){
 			return;
 		}
 		ItemMeta meta = car.getItemMeta();
 		List<String> lore = meta.getLore();
 		UUID id;
 		try {
 			if(lore.size() < 1){
 				return;
 			}
 			id = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 		} catch (Exception e) {
 			return;
 		}
 		Car c = null;
 		if(!plugin.carSaver.cars.containsKey(id)){
 			return;
 		}
 		c = plugin.carSaver.cars.get(id);
 		HashMap<String, Stat> stats = c.getStats();
 		upgradeItems.add(event.getCurrentItem());
 		for(ItemStack upgrade:upgradeItems){
 			if(upgrade.getType() == Material.MINECART || upgrade.getType() == Material.AIR){
 				//Allowed
 			}
 			else if(upgrade.getType() == Material.IRON_BLOCK){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 				}
 				health = health + (9*upgrade.getAmount()); //Add 9 to health stat
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else if(upgrade.getType() == Material.IRON_INGOT){
 				//Health upgrade
 				double health = ucars.config.getDouble("general.cars.health.default");
 				double maxHealth = ucars.config.getDouble("general.cars.health.max");
 				HealthStat stat = new HealthStat(health, plugin);
 				if(stats.containsKey("trade.health")){
 					stat = (HealthStat) stats.get("trade.health");
 				}
 				health = health + (1*upgrade.getAmount()); //Add 1 to health stat
 				if(health > maxHealth){
 					health = maxHealth;
 				}
 				upgrade.setAmount(0);
 				stat.setHealth(health);
 				stats.put("trade.health", stat);
 			}
 			else{
 				//Invalid item
 				return;
 			}
 		}
 		c.setStats(stats);
 	    CraftingInventory ci = (CraftingInventory) inv;
 	    ci.clear();
 	    ((Player)event.getView().getPlayer()).getInventory().addItem(c.getItem());
 	    ((Player)event.getView().getPlayer()).updateInventory();
 	    plugin.carSaver.cars.put(id, c);
 	    plugin.carSaver.save();
 		return;
 	}
 	*/
 	
 	@EventHandler(priority = EventPriority.HIGH) //Call first
  	void carDisplayDamage(EntityDamageEvent event){
 		if(event.getEntity() instanceof Player){
 			return;
 		}
 		Entity e = event.getEntity();
 		Entity v = isEntityInCar(e);
 		if(v == null){
 			return;
 		}
 		//Part of a car stack
 		event.setDamage(0);
 		event.setCancelled(true);
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	void carDisplayDeaths(EntityDeathEvent event){
 		try {
 			if(event.getEntity() instanceof Player){
 				return;
 			}
 			Entity e = event.getEntity();
 			Entity v = isEntityInCar(e);
 			if(v == null){
 				return;
 			}
 			//Part of a car stack
 			event.setDroppedExp(0);
 			event.getDrops().clear();
 		} catch (Exception e) {
 			//Entities already removed
 		}
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void carDestroy(VehicleDestroyEvent event){
 		if(event.getVehicle() instanceof Minecart){
 			return; //uCars can handle it
 		}
 		final Minecart car = isEntityInCar(event.getVehicle());
 		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
 			return;
 		}
 		event.setCancelled(true);
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void boatDestroy(EntityDamageByBlockEvent event){
 		final Minecart car = isEntityInCar(event.getEntity());
 		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
 			return;
 		}
 		event.setDamage(0);
 		event.setCancelled(true);
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void carDestroy(VehicleDamageEvent event){
 		if(event.getVehicle() instanceof Minecart){
 			return; //uCars can handle it
 		}
 		final Minecart car = isEntityInCar(event.getVehicle());
 		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
 			return;
 		}
 		event.setDamage(0);
 		event.setCancelled(true);
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.LOW) //Call second
 	void carDestroy(EntityDamageByEntityEvent event){
 		if(event.getEntity() instanceof Minecart
 				|| !(event.getDamager() instanceof Player)){
 			return; //uCars can handle it, or they're not a player
 		}
 		final Player player = (Player) event.getDamager();
 		final Minecart car = isEntityInCar(event.getEntity());
 		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
 			return;
 		}
 		Runnable onDeath = new Runnable() {
 			// @Override
 			public void run() {
 				plugin.getServer().getPluginManager()
 						.callEvent(new ucarDeathEvent(car));
 			}
 		};
 		CarHealthData health = new CarHealthData(
 				ucars.config.getDouble("general.cars.health.default"), onDeath,
 				plugin);
 		if (car.hasMetadata("carhealth")) {
 			List<MetadataValue> vals = car.getMetadata("carhealth");
 			for (MetadataValue val : vals) {
 				if (val instanceof CarHealthData) {
 					health = (CarHealthData) val;
 				}
 			}
 		}
 		event.setDamage(0);
 		event.setCancelled(true);
 		double damage = ucars.config
 				.getDouble("general.cars.health.punchDamage");
 		if (damage > 0) {
 			double max = ucars.config.getDouble("general.cars.health.default");
 			double left = health.getHealth() - damage;
 			ChatColor color = ChatColor.YELLOW;
 			if (left > (max * 0.66)) {
 				color = ChatColor.GREEN;
 			}
 			if (left < (max * 0.33)) {
 				color = ChatColor.RED;
 			}
 			if (left < 0) {
 				left = 0;
 			}
 			player.sendMessage(ChatColor.RED + "-" + damage + ChatColor.YELLOW
 					+ "[" + player.getName() + "]" + color + " (" + left + ")");
 			health.damage(damage);
 			car.setMetadata("carhealth", health);
 		}
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	void carPlace(PlayerInteractEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
 			return;
 		}
 		Block block = event.getClickedBlock();
 		ItemStack inHand = event.getPlayer().getItemInHand().clone();
 		if(inHand == null ||
 				inHand.getItemMeta() == null ||
 				inHand.getItemMeta().getLore() == null ||
 				inHand.getItemMeta().getLore().size() < 2 || inHand.getType() != Material.MINECART){ //Not a car
 			return;
 		}
 		// Its a minecart!
 		int iar = block.getTypeId();
 		if (iar == 66 || iar == 28 || iar == 27) {
 			return;
 		}
 		if(!PlaceManager.placeableOn(iar, block.getData())){
 			return;
 		}
 		if (!ucars.config.getBoolean("general.cars.enable")) {
 			return;
 		}
 		if (ucars.config.getBoolean("general.cars.placePerm.enable")) {
 			String perm = ucars.config
 					.getString("general.cars.placePerm.perm");
 			if (!event.getPlayer().hasPermission(perm)) {
 				String noPerm = Lang.get("lang.messages.noPlacePerm");
 				noPerm = noPerm.replaceAll("%perm%", perm);
 				event.getPlayer().sendMessage(
 						ucars.colors.getError() + noPerm);
 				return;
 			}
 		}
 		if (event.isCancelled()) {
 			event.getPlayer().sendMessage(
 					ucars.colors.getError()
 							+ Lang.get("lang.messages.noPlaceHere"));
 			return;
 		}
 		List<String> lore = inHand.getItemMeta().getLore();
 		Car c = null;
 		if(lore.size() > 0){
 		UUID carId = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 		if(!plugin.carSaver.cars.containsKey(carId)){
 			return;
 		}
 		c = plugin.carSaver.cars.get(carId);
 		}
 		else{
 			return;
 		}
 		HashMap<String, Stat> stats = c.getStats();
 		Location loc = block.getLocation().add(0, 1.5, 0);
 		loc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
 		final Minecart car = (Minecart) event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
 		double health = ucars.config.getDouble("general.cars.health.default");
 		if(stats.containsKey("trade.health")){
 			try {
 				health = (Double) stats.get("trade.health").getValue();
 			} catch (Exception e) {
 				//Leave health to default
 			}
 		}
 		Runnable onDeath = new Runnable(){
 			//@Override
 			public void run(){
 				plugin.getServer().getPluginManager().callEvent(new ucarDeathEvent((Minecart) car));
 			}
 		};
 		car.setMetadata("carhealth", new CarHealthData(health, onDeath, plugin));
 		/*
 		 * Location carloc = car.getLocation();
 		 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
 		 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
 		 * car.setVelocity(new Vector(0,0,0));
 		 */
 		UUID id = car.getUniqueId();
 		car.setMetadata("trade.car", new StatValue(true, plugin));
 			ItemStack placed = event.getPlayer().getItemInHand();
 			placed.setAmount(0);
 			event.getPlayer().getInventory().setItemInHand(placed);
 		event.setCancelled(true);
 		while(plugin.carSaver.cars.containsKey(id)){
 			Car cr = plugin.carSaver.cars.get(id);
 			plugin.carSaver.cars.remove(id);
 		    UUID newId = UUID.randomUUID();
 		    while(plugin.carSaver.cars.containsKey(newId)){
 		    	newId = UUID.randomUUID();
 		    }
 		    plugin.carSaver.cars.put(newId, cr);
 		}
 		plugin.carSaver.cars.remove(c.getId());
 		c.setId(id); //Bind car id to minecart id
 		c.isPlaced = true;
 		plugin.carSaver.cars.put(id, c);
 		plugin.carSaver.save();
 		String name = "Unnamed";
 		if(stats.containsKey("trade.name")){
 			name = stats.get("trade.name").getValue().toString();
 		}
 		String placeMsg = net.stormdev.ucars.trade.Lang.get("general.place.msg");
 		placeMsg = main.colors.getInfo() + placeMsg.replaceAll(Pattern.quote("%name%"), "'"+name+"'");
 		event.getPlayer().sendMessage(placeMsg);
 		//TODO V - Debug stat
 		c.stats.put("trade.display", new Stat(Displays.Upgrade_Hover, main.plugin));
 		DisplayManager.fillCar(car, c, event.getPlayer());
 		//TODO Put correct displays into stack
 		return;
 	}
 	@EventHandler (priority=EventPriority.HIGH)
 	void carRemoval(ucarDeathEvent event){
 		Minecart cart = event.getCar();
 		UUID id = cart.getUniqueId();
 		if(!plugin.carSaver.cars.containsKey(id)){
 			return;
 		}
 		Car car = plugin.carSaver.cars.get(id);
 		if(!car.isPlaced){
 			return;
 		}
 		car.isPlaced = false;
 		if(main.random.nextBoolean()){
 			if(main.random.nextBoolean()){
 				if(main.random.nextBoolean()){
 				    car.stats.put("trade.handling", new HandlingDamagedStat(true, plugin));
 				}
 			}
 		}
 		plugin.carSaver.cars.put(id, car);
 		plugin.carSaver.save();
 		Location loc = cart.getLocation();
 		Entity top = cart;
 		while(top.getPassenger() != null
 				&& !(top.getPassenger() instanceof Player)){
 			top = top.getPassenger();
 		}
 		if(top.getPassenger() instanceof Player){
 			top.eject();
 		}
 		while(top.getVehicle() != null){
 			Entity veh = top.getVehicle();
 			top.remove();
 			top = veh;
 		}
 		cart.eject();
 		cart.remove();
 		loc.getWorld().dropItemNaturally(loc, new ItemStack(car.getItem()));
 		//Remove car and get back item
 		event.setCancelled(true);
 		return;
 	}
 	//Add extra functions; eg. Car trade station, etc...
 	@EventHandler
 	public void signWriter(SignChangeEvent event){
 		String[] lines = event.getLines();
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Trade]")){
 			lines[0] = ChatColor.GREEN+"[Trade]";
 			lines[1] = ChatColor.RED + ChatColor.stripColor(lines[1]);
 			lines[2] = "Place chest";
 			lines[3] = "above";
 		}
 		return;
 	}
 	@EventHandler
 	public void tradeBooth(InventoryOpenEvent event){
 		if(!main.config.getBoolean("general.carTrading.enable")){
 			//Don't do car trading
 			return;
 		}
 		if(main.economy == null){
 	        Boolean installed = plugin.setupEconomy();
 	        if(!installed){
 	        	main.config.set("general.carTrading.enable", false);
 	        	main.logger.info(main.colors.getError()+"[Important] Unable to find an economy plugin:"
 	        			+ " trade booths have been closed.");
 	        	return;
 	        }
 		}
 		Inventory inv = event.getInventory();
 		if (!(inv.getHolder() instanceof Chest || inv.getHolder() instanceof DoubleChest)){
             return;
         }
 		//They opened a chest
 		Block block = null;
 		if(inv.getHolder() instanceof Chest){
 			block = ((Chest)inv.getHolder()).getBlock();
 		}
 		else{
 			block = ((DoubleChest)inv.getHolder()).getLocation().getBlock();
 		}
 		Block underBlock = block.getRelative(BlockFace.DOWN);
 		if(!(underBlock.getState() instanceof Sign)){
 			return;
 		}
 		Sign sign = (Sign) underBlock.getState();
 		if(!(ChatColor.stripColor(sign.getLines()[0])).equalsIgnoreCase("[Trade]") || !(ChatColor.stripColor(sign.getLines()[1])).equalsIgnoreCase("cars")){
 			return;
 		}
 		//A trade sign for cars
 		//Create a trade inventory
 		Player player = (Player) event.getPlayer(); //Get the player from the event
 		event.getView().close();
 		event.setCancelled(true); //Cancel the event
 		plugin.tradeMenu.open(player);
 		//Made the trade booth
 		return;
 	}
 	@SuppressWarnings("unchecked")
 	@EventHandler
 	public void tradeMenuSelect(final TradeBoothClickEvent event){
 		IconMenu.OptionClickEvent clickEvent = event.getClickEvent();
 		final Player player = clickEvent.getPlayer();
 		int position = clickEvent.getPosition();
 		Runnable doAfter = null;
 		if(event.getMenuType() == TradeBoothMenuType.MENU){ //They are selecting which menu to open
 			if(position == 0){ //Read tutorial
 				player.sendMessage(main.colors.getTitle()+"Tutorial coming soon!");
 				return;
 			}
 			else if(position == 1){ //Buy cars
 				doAfter = new Runnable(){
 					public void run() {
 						getCarsForSaleMenu(1).open(player);
 						return;
 				    }};
 			    //Don't return
 			}
 			else if(position == 2){ //Sell cars
 				doAfter = new Runnable(){
 					public void run() {
 						getSellCarsInputMenu().open(player);
 						return;
 				    }};
 				//Don't return
 			}
 			else if(position == 3){ //Buy upgrades
 				doAfter = new Runnable(){
 					public void run() {
 						getUpgradesForSaleMenu(1).open(player);
 						return;
 				    }};
 			    //Don't return
 			}
 			else if(position == 4){ //Sell upgrades
 				doAfter = new Runnable(){
 					public void run() {
 						getSellUpgradesInputMenu().open(player);
 						return;
 				    }};
 				//Don't return
 			}
 		}
 		else if(event.getMenuType() == TradeBoothMenuType.BUY_CARS){
 			if(position == 0){
 				//Return to menu
 				doAfter = new Runnable(){
 					public void run() {
 						plugin.tradeMenu.open(player);
 						return;
 				    }};
 			}
 			else if(position == 53){
 				//Next page
 				doAfter = new Runnable(){
 					public void run() {
 						getCarsForSaleMenu(event.getPage()+1).open(player);
 						return;
 				    }};
 			}
 			else if(position == 52){
 				int page = event.getPage();
 				if(page > 1){
 					page--;
 				}
 				final int p = page;
 				doAfter = new Runnable(){
 					public void run() {
 						getCarsForSaleMenu(p).open(player);
 						return;
 				    }};
 			}
 			else{
 				//Positions 1-> 51
 				int page = event.getPage();
 				int slot = clickEvent.getPosition() - 1; //Click on '1' = pos=0
 				int mapPos = (page-1)*51+slot; //Page one, slot 1 = mapPos: 0
 				HashMap<UUID, CarForSale> cars = null;
 				try {
 				    cars = (HashMap<UUID, CarForSale>) event.getArgs()[0];
 				} catch (Exception e) {
 					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
 					return;
 				}
 				CarForSale car = null;
 				try {
 					car = cars.get(cars.keySet().toArray()[mapPos]);
 				} catch (Exception e) {
 					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
 					return;
 				}
 				//We have selected the correct car
 				if(!plugin.salesManager.carsForSale.containsKey(car.getCarId())){
 					//It was just bought
 					return;
 				}
 				if(main.economy == null){
 					if(!plugin.setupEconomy()){
 						player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
 						return;
 					}
 				}
 				//Economy plugin successfully hooked
 				double price = car.getPrice();
 				double balance = main.economy.getBalance(player.getName());
 				if(balance < price){
 					String msg = net.stormdev.ucars.trade.Lang.get("general.buy.notEnoughMoney");
 					msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
 					player.sendMessage(main.colors.getError()+msg);
 					return;
 				}
 				plugin.salesManager.carsForSale.remove(car.getCarId());
 				EconomyResponse er = main.economy.withdrawPlayer(player.getName(), price);
 				balance = er.balance;
 				if(!er.transactionSuccess()){
 					player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
 					return;
 				}
 				double profit = car.getProfit();
 				EconomyResponse er2 = main.economy.depositPlayer(car.getSeller(), profit);
 				if(plugin.getServer().getPlayer(car.getSeller())!=null && plugin.getServer().getPlayer(car.getSeller()).isOnline()){
 					Player pl = plugin.getServer().getPlayer(car.getSeller());
 					pl.sendMessage(main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For car sale!");
 				}
 				else{
 					DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy@HH:mm]");
 					String time = dateFormat.format(new Date());
 					String msg = main.colors.getInfo()+time+" "+ChatColor.RESET+main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For car sale!";
 					plugin.alerts.put(car.getSeller(), msg);
 				}
 				if(!er2.transactionSuccess()){
 					main.logger.info(main.colors.getError()+"Failed to give seller money for seller: "+car.getSeller()+"!");
 				}
 				String msg = net.stormdev.ucars.trade.Lang.get("general.buy.success");
 				msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
 				msg = msg.replaceAll(Pattern.quote("%item%"), "1 car");
 				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+price));
 				//Give them the car and remove it from the list
 				UUID carId = car.getCarId();
 				plugin.salesManager.saveCars();
 				Car c = plugin.carSaver.cars.get(carId);
 				c.isPlaced = false;
 				player.getInventory().addItem(c.getItem());
 				player.sendMessage(main.colors.getSuccess()+msg);
 			}
 		}
 		else if(event.getMenuType() == TradeBoothMenuType.BUY_UPGRADES){
 			if(position == 0){
 				//Return to menu
 				doAfter = new Runnable(){
 					public void run() {
 						plugin.tradeMenu.open(player);
 						return;
 				    }};
 			}
 			else if(position == 53){
 				//Next page
 				doAfter = new Runnable(){
 					public void run() {
 						getUpgradesForSaleMenu(event.getPage()+1).open(player);
 						return;
 				    }};
 			}
 			else if(position == 52){
 				int page = event.getPage();
 				if(page > 1){
 					page--;
 				}
 				final int p = page;
 				doAfter = new Runnable(){
 					public void run() {
 						getUpgradesForSaleMenu(p).open(player);
 						return;
 				    }};
 			}
 			else{
 				//Positions 1-> 51
 				//Get upgrade and buy it using vault
 				int page = event.getPage();
 				int slot = clickEvent.getPosition() - 1; //Click on '1' = pos=0
 				int mapPos = (page-1)*51+slot; //Page one, slot 1 = mapPos: 0
 				HashMap<UUID, UpgradeForSale> cars = null;
 				try {
 				    cars = (HashMap<UUID, UpgradeForSale>) event.getArgs()[0];
 				} catch (Exception e) {
 					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
 					return;
 				}
 				UpgradeForSale car = null;
 				try {
 					car = cars.get(cars.keySet().toArray()[mapPos]);
 				} catch (Exception e) {
 					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
 					return;
 				}
 				//We have selected the correct car
 				if(!plugin.salesManager.upgradeForSale.containsKey(car.getSaleId())){
 					//It was just bought
 					return;
 				}
 				if(main.economy == null){
 					if(!plugin.setupEconomy()){
 						player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
 						return;
 					}
 				}
 				//Economy plugin successfully hooked
 				double price = car.getPrice();
 				double balance = main.economy.getBalance(player.getName());
 				if(balance < price){
 					String msg = net.stormdev.ucars.trade.Lang.get("general.buy.notEnoughMoney");
 					msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
 					player.sendMessage(main.colors.getError()+msg);
 					return;
 				}
 				plugin.salesManager.upgradeForSale.remove(car.getSaleId());
 				EconomyResponse er = main.economy.withdrawPlayer(player.getName(), price);
 				balance = er.balance;
 				if(!er.transactionSuccess()){
 					player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
 					return;
 				}
 				double profit = car.getProfit();
 				EconomyResponse er2 = main.economy.depositPlayer(car.getSeller(), profit);
 				if(plugin.getServer().getPlayer(car.getSeller())!=null && plugin.getServer().getPlayer(car.getSeller()).isOnline()){
 					Player pl = plugin.getServer().getPlayer(car.getSeller());
 					pl.sendMessage(main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For upgrade sale!");
 				}
 				else{
 					DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy@HH:mm]");
 					String time = dateFormat.format(new Date());
 					String msg = main.colors.getInfo()+time+" "+ChatColor.RESET+main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For upgrade sale!";
 					plugin.alerts.put(car.getSeller(), msg);
 				}
 				if(!er2.transactionSuccess()){
 					main.logger.info(main.colors.getError()+"Failed to give seller money for seller: "+car.getSeller()+"!");
 				}
 				String msg = net.stormdev.ucars.trade.Lang.get("general.buy.success");
 				msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
 				msg = msg.replaceAll(Pattern.quote("%item%"), "upgrades");
 				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+price));
 				//Give them the car and remove it from the list
 				plugin.salesManager.saveUpgrades();
 				ItemStack item = new ItemStack(Material.IRON_INGOT);
 				ItemMeta im = item.getItemMeta();
 				List<String> lore = new ArrayList<String>();
 		        StatType type = car.getUpgradeType();
 	        	if(type == StatType.HANDLING_DAMAGED){
 	        		item = new ItemStack(Material.LEVER);
 	        		im = item.getItemMeta();
 	        		im.setDisplayName("Repair Upgrade");
 	        		lore.add(main.colors.getInfo()+"Repairs all car damage");
 	        		im.setLore(lore);
 	        	    item.setItemMeta(im);
 	        	}
 	        	else if(type == StatType.HEALTH){
 	        		item = new ItemStack(Material.IRON_INGOT);
 	        		im = item.getItemMeta();
 	        		im.setDisplayName("Health Upgrade");
 	        		lore.add(main.colors.getInfo()+"Adds 1 health to your car");
 	        		im.setLore(lore);
 	        	    item.setItemMeta(im);
 	        	}
 	        	else if(type == StatType.SPEED){
 	        		item = new ItemStack(Material.REDSTONE);
 	        		im = item.getItemMeta();
 	        		im.setDisplayName("Speed Upgrade");
 	        		lore.add(main.colors.getInfo()+"Adds 0.05x speed to your car");
 	        		im.setLore(lore);
 	        	    item.setItemMeta(im);
 	        	}
 	        	item.setAmount(car.getQuantity());
 				player.getInventory().addItem(item);
 				player.sendMessage(main.colors.getSuccess()+msg);
 			}
 		}
 		if(doAfter != null){
 			plugin.getServer().getScheduler().runTaskLater(plugin, doAfter, 2l);
 		}
 		return;
 	}
 	@EventHandler
 	void sellStuff(InputMenuClickEvent event){
 		OptionClickEvent clickEvent = event.getClickEvent();
 		final Player player = clickEvent.getPlayer();
 		int position = clickEvent.getPosition();
 		Runnable doAfter = null;
 		if(event.getMenuType() == TradeBoothMenuType.SELL_CARS){
 			if(position == 0){
 				//Return to menu
 				doAfter = new Runnable(){
 					public void run() {
 						plugin.tradeMenu.open(player);
 						return;
 				    }};
 			}
 			else if(position == 8){
 				//Check if valid and try to sell it
 				Inventory i = clickEvent.getInventory();
 				if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
 					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 					return;
 				}
 				ItemStack carItem = i.getItem(4);
 				if(carItem.getType() != Material.MINECART
 						|| carItem.getItemMeta() == null
 						|| carItem.getItemMeta().getLore() == null
 						|| carItem.getItemMeta().getLore().size() < 2){
 					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 					return;
 				}
 				//Is a valid car to sell
 				List<String> lore = carItem.getItemMeta().getLore();
 				Car c = null;
 				if(lore.size() > 0){
 				UUID carId = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 				if(!plugin.carSaver.cars.containsKey(carId)){
 					return;
 				}
 				c = plugin.carSaver.cars.get(carId);
 				}
 				else{
 					player.sendMessage(main.colors.getInfo()+"Invalid item to sell!");
 					return;
 				}
 				double price = CarValueCalculator.getCarValueForSale(c);
 				if(main.economy == null){
 					return;
 				}
 				String msg = net.stormdev.ucars.trade.Lang.get("general.sell.msg");
 				msg = msg.replaceAll(Pattern.quote("%item%"), "a car");
 				String units = main.config.getString("general.carTrading.currencySign")+price;
 				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(units));
 				// Add to market for sale
 				double purchase = CarValueCalculator.getCarValueForPurchase(c);
 				CarForSale saleItem = new CarForSale(c.id, player.getName(), purchase, price);
 				if(!plugin.salesManager.carsForSale.containsKey(c.id)){
 					plugin.salesManager.carsForSale.put(c.id, saleItem);
 					plugin.salesManager.saveCars();
 					player.sendMessage(main.colors.getInfo()+msg); //Tell player they are selling it on the market
 				}
 			}
 			else if(position == 4){
 				//Check if car and if it is update the sale button
 				final Inventory inv = clickEvent.getInventory();
 				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
 
 					public void run() {
 						Inventory i = inv;
 						if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
 							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 							return;
 						}
 						ItemStack carItem = i.getItem(4);
 						if(carItem.getType() != Material.MINECART 
 								|| carItem.getItemMeta() == null
 								|| carItem.getItemMeta().getLore() == null
 								|| carItem.getItemMeta().getLore().size() < 2){
 							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 							return;
 						}
 						List<String> lore = carItem.getItemMeta().getLore();
 						Car c = null;
 						if(lore.size() > 0){
 						UUID carId = UUID.fromString(ChatColor.stripColor(lore.get(0)));
 						if(!plugin.carSaver.cars.containsKey(carId)){
 							return;
 						}
 						c = plugin.carSaver.cars.get(carId);
 						}
 						else{
 							player.sendMessage(main.colors.getInfo()+"Invalid item to sell!");
 							return;
 						}
 						double price = CarValueCalculator.getCarValueForSale(c);
 						if(main.economy == null){
 							return;
 						}
 						ItemStack sellItem = new ItemStack(Material.EMERALD);
 						ItemMeta im = sellItem.getItemMeta();
 						im.setDisplayName(main.colors.getTitle()+"Sell");
 						ArrayList<String> lre = new ArrayList<String>();
 					    lre.add(main.colors.getInfo()+"For: "+main.config.getString("general.carTrading.currencySign")+price);
 						im.setLore(lre);
 						sellItem.setItemMeta(im);
 						inv.setItem(8, sellItem);
 						return;
 					}}, 1l);
 			}
 		}
 		else if(event.getMenuType() == TradeBoothMenuType.SELL_UPGRADES){
 			if(position == 0){
 				//Return to menu
 				doAfter = new Runnable(){
 					public void run() {
 						plugin.tradeMenu.open(player);
 						return;
 				    }};
 			}
 			else if(position == 8){
 				//Check if valid and try to sell it
 				Inventory i = clickEvent.getInventory();
 				if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
 					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 					return;
 				}
 				ItemStack upgradeItem = i.getItem(4).clone();
 				i.clear(4);
 				Material type = upgradeItem.getType();
 				StatType upgradeType = StatType.SPEED;
 				if(type == Material.IRON_INGOT){ //Health Upgrade
 					upgradeType = StatType.HEALTH;
 				}
 				else if(type == Material.REDSTONE){ //Speed upgrade
 					upgradeType = StatType.SPEED;
 				}
 				else if(type == Material.LEVER){ //Repair upgrade
 					upgradeType = StatType.HANDLING_DAMAGED;
 				}
 				else{
 					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 					return;
 				}
 				double price = main.config.getDouble("general.carTrading.upgradeValue")*upgradeItem.getAmount();
 				double sellFor = price + (main.config.getDouble("general.carTrading.VATPercent")*price)/100;
 				sellFor = Math.round((sellFor*100));
 				sellFor = sellFor / 100;
 				UUID saleId = UUID.randomUUID();
 				UpgradeForSale saleItem = new UpgradeForSale(saleId, player.getName(), sellFor, upgradeType, upgradeItem.getAmount(), price);
 				String msg = net.stormdev.ucars.trade.Lang.get("general.sell.msg");
 				msg = msg.replaceAll(Pattern.quote("%item%"), "upgrades");
 				String units = main.config.getString("general.carTrading.currencySign")+price;
 				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(units));
 				// Add to market for sale
 				if(!plugin.salesManager.upgradeForSale.containsKey(saleId)){
 					plugin.salesManager.upgradeForSale.put(saleId, saleItem);
 					plugin.salesManager.saveUpgrades();
 					player.sendMessage(main.colors.getInfo()+msg); //Tell player they are selling it on the market
 				}
 				clickEvent.getMenu().destroy(); //Close the menu
 			}
 			else if(position == 4){
 				//Check if valid and if it is update the sale button
 				final Inventory inv = clickEvent.getInventory();
 				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
 
 					public void run() {
 						Inventory i = inv;
 						if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
 							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 							return;
 						}
 						Material type = i.getItem(4).getType();
 						if(type == Material.IRON_INGOT || type == Material.REDSTONE
 								|| type == Material.LEVER){ //Valid Upgrade
 						    //Update button	
 							ItemStack sellItem = new ItemStack(Material.EMERALD);
 							ItemMeta im = sellItem.getItemMeta();
 							im.setDisplayName(main.colors.getTitle()+"Sell");
 							ArrayList<String> lre = new ArrayList<String>();
 							double price = main.config.getDouble("general.carTrading.upgradeValue")*i.getItem(4).getAmount();
 						    lre.add(main.colors.getInfo()+"For: "+main.config.getString("general.carTrading.currencySign")+price);
 							im.setLore(lre);
 							sellItem.setItemMeta(im);
 							inv.setItem(8, sellItem);
 						}
 						else{
 							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
 							return;
 						}
 						return;
 					}}, 1l);
 			}
 		}
 		if(doAfter != null){
 			plugin.getServer().getScheduler().runTaskLater(plugin, doAfter, 2l);
 		}
 		return;
 	}
 	IconMenu getCarsForSaleMenu(final int page){
 		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.buyCars")+" Page: "+page;
 		if(title.length() > 32){
 			title = main.colors.getError()+"Buy Cars (ERROR:Too Long)";
 		}
 		@SuppressWarnings("unchecked")
 		final HashMap<UUID, CarForSale> cars = (HashMap<UUID, CarForSale>) plugin.salesManager.carsForSale.clone();
 		IconMenu menu = new IconMenu(title, 54, new IconMenu.OptionClickEventHandler() {
             public void onOptionClick(IconMenu.OptionClickEvent event) {
             	TradeBoothClickEvent evt = new TradeBoothClickEvent(event, TradeBoothMenuType.BUY_CARS, page, new Object[]{cars});
             	plugin.getServer().getPluginManager().callEvent(evt);
             	event.setWillClose(true);
             	event.setWillDestroy(true);
             }
         }, plugin);
 		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
 		menu.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
 		menu.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
 		//1-51 slots available on the page
 		int pos = 1;
 		int start = (page-1)*51;
 		Object[] keys = cars.keySet().toArray();
 		for(int i=start;i<keys.length;i++){
 			CarForSale car= cars.get(keys[i]);
 			double price = car.getPrice();
 	        String seller = car.getSeller();
 	        UUID carId = car.getCarId();
 	        ItemStack item = new ItemStack(Material.AIR);
 	        String name = "Car";
 	        List<String> lore = new ArrayList<String>();
 	        if(plugin.carSaver.cars.containsKey(carId)){
 	        	Car c = plugin.carSaver.cars.get(carId);
 	        	if(c.getStats().containsKey("trade.name")){
 	        		name = c.getStats().get("trade.name").getValue().toString();
 	        	}
 	        	item = c.getItem();
 	        	ItemMeta im = item.getItemMeta();
 	        	lore.add(main.colors.getInfo()+main.config.getString("general.carTrading.currencySign")+price);
 	        	lore.add(main.colors.getInfo()+"Seller: "+seller);
 	        	List<String> iml = im.getLore();
 	        	iml.remove(0);
 	        	lore.addAll(2, iml);
 	        	im.setLore(lore);
 	        	item.setItemMeta(im);
 	        }   
 	        if(pos < 52){
         		menu.setOption(pos, item, main.colors.getTitle()+name, lore);
         		pos++;
         	}
 		}
 		return menu;
 	}
 	IconMenu getUpgradesForSaleMenu(final int page){
 		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.buyUpgrades")+" Page: "+page;
 		if(title.length() > 32){
 			title = main.colors.getError()+"Buy Upgrades (ERROR:Too Long)";
 		}
 		@SuppressWarnings("unchecked")
 		final HashMap<UUID, UpgradeForSale> ups = (HashMap<UUID, UpgradeForSale>) plugin.salesManager.upgradeForSale.clone();
 		IconMenu menu = new IconMenu(title, 54, new IconMenu.OptionClickEventHandler() {
             public void onOptionClick(IconMenu.OptionClickEvent event) {
             	TradeBoothClickEvent evt = new TradeBoothClickEvent(event, TradeBoothMenuType.BUY_UPGRADES, page, new Object[]{ups});
             	plugin.getServer().getPluginManager().callEvent(evt);
             	event.setWillClose(true);
             	event.setWillDestroy(true);
             }
         }, plugin);
 		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
 		menu.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
 		menu.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
 		//Set option slots for all upgrades for sale
 		//1-51 slots available on the page
 				int pos = 1;
 				int start = (page-1)*51;
 				Object[] keys = ups.keySet().toArray();
 				for(int i=start;i<keys.length;i++){
 					UpgradeForSale car= ups.get(keys[i]);
 					double price = car.getPrice();
 			        String seller = car.getSeller();
 			        ItemStack item = new ItemStack(Material.AIR);
 			        ItemMeta im = item.getItemMeta();
 			        String name = "Upgrade";
 			        List<String> lore = new ArrayList<String>();
 			        item = new ItemStack(Material.IRON_INGOT);
 			        StatType type = car.getUpgradeType();
 		        	if(type == StatType.HANDLING_DAMAGED){
 		        		item = new ItemStack(Material.LEVER);
 		        		im = item.getItemMeta();
 		        		name = "Repair Upgrade";
 		        		lore.add(main.colors.getInfo()+"Repairs all car damage");
 		        		im.setLore(lore);
 		        	    item.setItemMeta(im);
 		        	}
 		        	else if(type == StatType.HEALTH){
 		        		item = new ItemStack(Material.IRON_INGOT);
 		        		im = item.getItemMeta();
 		        		name = "Health Upgrade";
 		        		lore.add(main.colors.getInfo()+"Adds 1 health to your car");
 		        		im.setLore(lore);
 		        	    item.setItemMeta(im);
 		        	}
 		        	else if(type == StatType.SPEED){
 		        		item = new ItemStack(Material.REDSTONE);
 		        		im = item.getItemMeta();
 		        		name = "Speed Upgrade";
 		        		lore.add(main.colors.getInfo()+"Adds 0.05x speed to your car");
 		        		im.setLore(lore);
 		        	    item.setItemMeta(im);
 		        	}
 		        	lore = new ArrayList<String>();
 		        	lore.add(main.colors.getInfo()+main.config.getString("general.carTrading.currencySign")+price);
 		        	lore.add(main.colors.getInfo()+"Seller: "+seller);
 		        	lore.addAll(2, im.getLore());
 		        	im.setLore(lore);
 		        	item.setItemMeta(im);	
 		        	item.setAmount(car.getQuantity());
 			        if(pos < 52){
 		        		menu.setOption(pos, item, main.colors.getTitle()+name, lore);
 		        		pos++;
 		        	}
 				}
 		return menu;
 	}
 	
 	InputMenu getSellCarsInputMenu(){
 		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.sellCars");
 		if(title.length() > 32){
 			title = main.colors.getError()+"Sell a car";
 		}
 		InputMenu menu = new InputMenu(title, 9, new InputMenu.OptionClickEventHandler() {
             public void onOptionClick(InputMenu.OptionClickEvent event) {
             	if(event.getPosition() == 0 || event.getPosition() == 8){
             		event.setWillClose(true);
             	}
             	InputMenuClickEvent evt = new InputMenuClickEvent(event, TradeBoothMenuType.SELL_CARS);
             	plugin.getServer().getPluginManager().callEvent(evt);
             }
         }, plugin);
 		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
 		menu.addButtonSlot(0);
 		menu.setOption(8, new ItemStack(Material.EMERALD), main.colors.getTitle()+"Sell", main.colors.getError()+"Unavailable");
 		menu.addButtonSlot(8);
 		menu.setOption(1, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(1);
 		menu.setOption(2, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(2);
 		menu.setOption(3, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(3);
 		menu.setOption(5, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(5);
 		menu.setOption(6, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(6);
 		menu.setOption(7, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(7);
 		//4 is the input box
 		return menu;
 	}
 	InputMenu getSellUpgradesInputMenu(){
 		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.sellUpgrades");
 		if(title.length() > 32){
 			title = main.colors.getError()+"Sell upgrades";
 		}
 		InputMenu menu = new InputMenu(title, 9, new InputMenu.OptionClickEventHandler() {
             public void onOptionClick(InputMenu.OptionClickEvent event) {
             	if(event.getPosition() == 0 || event.getPosition() == 8){
             		event.setWillClose(true);
             	}
             	InputMenuClickEvent evt = new InputMenuClickEvent(event, TradeBoothMenuType.SELL_UPGRADES);
             	plugin.getServer().getPluginManager().callEvent(evt);
             }
         }, plugin);
 		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
 		menu.addButtonSlot(0);
 		menu.setOption(8, new ItemStack(Material.EMERALD), main.colors.getTitle()+"Sell", main.colors.getError()+"Unavailable");
 		menu.addButtonSlot(8);
 		menu.setOption(1, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(1);
 		menu.setOption(2, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(2);
 		menu.setOption(3, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
 		menu.addButtonSlot(3);
 		menu.setOption(5, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(5);
 		menu.setOption(6, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(6);
 		menu.setOption(7, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
 		menu.addButtonSlot(7);
 		//4 is the input box
 		return menu;
 	}
 	@EventHandler (priority = EventPriority.MONITOR)
 	void alerts(PlayerJoinEvent event){
 		String name = event.getPlayer().getName();
 		if(!plugin.alerts.containsKey(name)){
 			return;
 		}
 		event.getPlayer().sendMessage(plugin.alerts.get(name));
 		plugin.alerts.remove(name);
 		return;
 	}
 	
 	public Minecart isEntityInCar(Entity e){
 		if(e.getVehicle() == null){
 			return null;
 		}
 		Entity v = e.getVehicle();
 		while(v!=null && v.getVehicle() != null && !(v instanceof Minecart)){
 			v = v.getVehicle();
 		}
 		if(v == null || !(v instanceof Minecart)){
 			return null;
 		}
 		return (Minecart) v;
 	}
 
 }
