 package net.croxis.plugins.civilmineation;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.CivilizationComponent;
 import net.croxis.plugins.civilmineation.components.Ent;
 import net.croxis.plugins.civilmineation.components.PermissionComponent;
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.civilmineation.events.DeleteCityEvent;
 import net.croxis.plugins.civilmineation.events.DeleteCivEvent;
 import net.croxis.plugins.civilmineation.events.NewCityEvent;
 import net.croxis.plugins.civilmineation.events.NewCivEvent;
 import net.croxis.plugins.research.Tech;
 import net.croxis.plugins.research.TechManager;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 public class CivAPI {
 	public static Civilmineation plugin;
 	public static Economy econ = null;
 	public CivAPI(Civilmineation p){
 		plugin = p;
 	}
 	
 	public static ResidentComponent getResident(String name){
     	return plugin.getDatabase().find(ResidentComponent.class).where().ieq("name", name).findUnique();
     }
     
     public static ResidentComponent getResident(Player player){
     	return plugin.getDatabase().find(ResidentComponent.class).where().ieq("name", player.getName()).findUnique();
     }
     
     public static HashSet<ResidentComponent> getResidents(CivilizationComponent civ){
     	Set<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", civ).findSet();
     	HashSet<ResidentComponent> residents = new HashSet<ResidentComponent>();
     	for (CityComponent city : cities){
     		residents.addAll(getResidents(city));
     	}
     	return residents;
     }
     
     public static Set<ResidentComponent> getResidents(CityComponent city){
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", city).findSet();
     }
     
     public static PlotComponent getPlot(Chunk chunk){
     	return getPlot(chunk.getX(), chunk.getZ());
     }
     
     public static PlotComponent getPlot(int x, int z){
     	return plugin.getDatabase().find(PlotComponent.class).where().eq("x", x).eq("z", z).findUnique();
     }
     
     public static PlotComponent getPlot(Sign sign){
     	return plugin.getDatabase().find(PlotComponent.class).where().eq("signX", sign.getX()).eq("signY", sign.getY()).eq("signZ", sign.getZ()).findUnique();
     }
     
     public static Sign getPlotSign(PlotComponent plot){
     	Block block = plugin.getServer().getWorld(plot.getWorld()).getBlockAt(plot.getSignX(), plot.getSignY(), plot.getSignZ());
     	if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST)
 			return (Sign) block.getState();
     	return null;
     }
     
     public static PermissionComponent getPermissions(Ent ent){
     	return plugin.getDatabase().find(PermissionComponent.class).where().eq("entityID", ent).findUnique();
     }
     
     public static CivilizationComponent getCiv(String name){
     	return plugin.getDatabase().find(CivilizationComponent.class).where().ieq("name", name).findUnique();
     }
     
     public static CityComponent getCity(Location charterLocation){
     	return plugin.getDatabase().find(CityComponent.class).where().eq("charter_x", charterLocation.getX())
     			.eq("charter_y", charterLocation.getY())
     			.eq("charter_z", charterLocation.getZ()).findUnique();
     }
     
     public static Set<CityComponent> getCities(){
     	return plugin.getDatabase().find(CityComponent.class).findSet();
     }
     
     public static Set<ResidentComponent> getAssistants(CityComponent city){
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", city).eq("cityAssistant", true).findSet();
     }
     
     public static Set<PlotComponent> getPlots(CityComponent city){
     	return plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).findSet();
     }
     
     public static ResidentComponent getMayor(ResidentComponent resident){
     	if (resident.getCity() == null)
     		return null;
 		return getMayor(resident.getCity());
     }
     
     public static ResidentComponent getMayor(CityComponent city){
 		return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", city).eq("mayor", true).findUnique();
     }
     
     public static ResidentComponent getKing(ResidentComponent resident){
     	CityComponent capital = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", resident.getCity().getCivilization()).eq("capital", true).findUnique();
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", capital).eq("mayor", true).findUnique();
     }
         
     public static ResidentComponent getKing(CityComponent city){
     	CityComponent capital = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", city.getCivilization()).eq("capital", true).findUnique();
     	return plugin.getDatabase().find(ResidentComponent.class).where().eq("city", capital).eq("mayor", true).findUnique();
     }
     
     public static boolean isKing(ResidentComponent resident){
     	return resident.isMayor() && resident.getCity().isCapital();
     }
     
     public static boolean isNationalAdmin(ResidentComponent resident){
     	if (resident.isCivAssistant())
     		return true;
     	return isKing(resident);
     }
     
     public static boolean isCityAdmin(ResidentComponent resident){
     	if (resident.isCityAssistant())
     		return true;
     	else if (resident.getCity() == null)
     		return false;
     	return resident.isMayor();
     }
     
     public static boolean isClaimed(PlotComponent plot){
     	if (plot == null){
 			return false;
 		} else if (plot.getCity() == null){
 			return false;
 		}
     	return true;
     }
     
     public static void addCulture(CityComponent city, int culture){
     	city.setCulture(city.getCulture() + culture);
     	plugin.getDatabase().save(city);
     	updateCityCharter(city);
     }
     
 	public static void addResearch(CityComponent city, int points) {
 		CityComponent capital = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", city.getCivilization())
 				.eq("capital", true).findUnique();
 		ResidentComponent king = plugin.getDatabase().find(ResidentComponent.class).where().eq("city", capital).eq("mayor", true).findUnique();
 		Tech learned = TechManager.addPoints(king.getName(), points);
 		if(learned != null){
 			List<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", capital.getCivilization()).findList();
 			for (CityComponent c : cities){
 				List<ResidentComponent> residents = plugin.getDatabase().find(ResidentComponent.class).where().eq("city", c).findList();
 				for (ResidentComponent r : residents){
 					TechManager.addTech(r.getName(), learned);
 				}
				broadcastToCity("You have learned " + learned.name + "!", c);
 			}
 		}
 		
 	}
     
     public static void updateCityCharter(CityComponent city){
     	if (city == null){
     		Civilmineation.log("Error. No city at that charter");
     		return;
     	}
     	Block charter = plugin.getServer().getWorld(city.getCharterWorld()).getBlockAt(city.getCharter_x(), city.getCharter_y(), city.getCharter_z());
     	Sign charterBlock = (Sign) charter.getState();
 		charterBlock.setLine(0, ChatColor.DARK_AQUA + "City Charter");
 		charterBlock.setLine(1, city.getCivilization().getName());
 		charterBlock.setLine(2, city.getName());
 		charterBlock.setLine(3, "Mayor " + getMayor(city).getName());
 		charterBlock.update();
     	Sign block = (Sign) charter.getRelative(BlockFace.DOWN).getState();
 		block.setLine(0, "=Demographics=");
 		block.setLine(1, "Population: " + Integer.toString(CivAPI.getResidents(city).size()));
 		block.setLine(2, "=Immigration=");
 		if (block.getLine(3).contains("Open"))
 			block.setLine(3, ChatColor.GREEN + "Open");
 		else
 			block.setLine(3, ChatColor.RED + "Closed");
 		block.update();
 		if (city.getCharterRotation() == 4 || city.getCharterRotation() == 5){
     		charter.getRelative(BlockFace.EAST).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.EAST).getState();
 			block.setLine(0, "Money: " + Double.toString(econ.getBalance(city.getName())));
 			Tech tech = TechManager.getCurrentResearch(getKing(city).getName());
 			if (tech == null){
 				block.setLine(1, "Research:");
 				block.setLine(2, "None");
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + " / 0");
 			} else {
 				block.setLine(1, "Research:");
 				block.setLine(2, ChatColor.BLUE + tech.name);
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + "/" + Integer.toString(TechManager.getCurrentResearch(getKing(city).getName()).cost));
 			}
 			block.update();
 			charter.getRelative(BlockFace.WEST).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.WEST).getState();
 			block.setLine(1, "Plots: " + Integer.toString(getPlots(city).size()));
 			block.setLine(2, "Culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(city.getCulture()));
 			block.update();
 		} else if (city.getCharterRotation() == 2 || city.getCharterRotation() == 3) {
 			charter.getRelative(BlockFace.NORTH).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.NORTH).getState();
 			block.setLine(0, "Money: N/A");
 			Tech tech = TechManager.getCurrentResearch(getKing(city).getName());
 			if (tech == null){
 				block.setLine(1, "Research:");
 				block.setLine(2, "None");
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + " / 0");
 			} else {
 				block.setLine(1, "Research:");
 				block.setLine(2, ChatColor.BLUE + tech.name);
 				block.setLine(3, ChatColor.BLUE + Integer.toString(TechManager.getPoints(getKing(city).getName())) + "/" + Integer.toString(TechManager.getCurrentResearch(getKing(city).getName()).cost));
 			}
 			block.update();
 			charter.getRelative(BlockFace.SOUTH).setTypeIdAndData(68, city.getCharterRotation(), true);
 			block = (Sign) charter.getRelative(BlockFace.SOUTH).getState();
 			block.setLine(1, "Plots: N/A");
 			block.setLine(2, "Culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(city.getCulture()));
 			block.update();
 		}
     }
     
     public static void setPlotSign(Sign plotSign){
     	PlotComponent plot = getPlot(plotSign.getChunk());
     	plot.setSignX(plotSign.getX());
 		plot.setSignY(plotSign.getY());
 		plot.setSignZ(plotSign.getZ());
 		plugin.getDatabase().save(plot);
     }
     
     public static void updatePlotSign(int x, int z) {
     	updatePlotSign(plugin.getDatabase().find(PlotComponent.class).where().eq("x", x).eq("z", z).findUnique());
     }
     
     public static void updatePlotSign(PlotComponent plot) {
     	Civilmineation.logDebug("Updating plot sign");
 		Sign sign = getPlotSign(plot);
 		if(plot.getResident()!=null){
 			if(plugin.getServer().getPlayer(plot.getResident().getName()).isOnline()){
 				sign.setLine(0, ChatColor.GREEN + plot.getResident().getName());
 				sign.update();
 				Civilmineation.logDebug("a");
 			} else {
 				sign.setLine(0, ChatColor.RED + plot.getResident().getName());
 				sign.update();
 				Civilmineation.logDebug("b");
 			}
 		} else { 
 			sign.setLine(0, plot.getCity().getName());
 			sign.update();
 			Civilmineation.logDebug("c");
 			sign.update();
 		}
 		Civilmineation.logDebug("New plot sign xyz: " + Integer.toString(sign.getX()) + ", " + Integer.toString(sign.getY()) + ", " + Integer.toString(sign.getZ()));
 		sign.update();
 	}
     
     public static void updatePlotSign(Sign sign, PlotComponent plot) {
 		plot.setSignX(sign.getX());
 		plot.setSignY(sign.getY());
 		plot.setSignZ(sign.getZ());
 		plugin.getDatabase().save(plot);
 		//updatePlotSign(plot.getX(), plot.getZ());
 		updatePlotSign(plot);
 	}
     
     public static boolean addResident(ResidentComponent resident, CityComponent city){
     	if (resident.getCity() != null)
     		return false;
     	resident.setCity(city);
     	plugin.getDatabase().save(resident);
     	HashSet<Tech> civtechs = TechManager.getResearched(getKing(resident).getName());
     	HashSet<Tech> restechs = TechManager.getResearched(resident.getName());
 		HashSet<Tech> difference = new HashSet<Tech>(restechs);
 		difference.removeAll(civtechs);
 		Player player = Bukkit.getServer().getPlayer(resident.getName());
 		restechs.addAll(civtechs);
 		TechManager.setTech(player, restechs);
 		HashSet<Tech> gaintech = new HashSet<Tech>();
 		HashSet<Tech> canResearch = TechManager.getAvailableTech(getKing(resident).getName());
 		for (Tech t : difference){
 			if(gaintech.size() > 2)
 				break;
 			if(canResearch.contains(t))
 				gaintech.add(t);
 		}
 		for (Tech t : gaintech){
 			for (ResidentComponent r : getResidents(resident.getCity().getCivilization())){
 				TechManager.addTech(r.getName(), t);
 			}
 		}
 		updateCityCharter(city);
     	return true;
     }
     
 	public static CityComponent createCity(String name, Player player, ResidentComponent mayor, Block charter,
 			CivilizationComponent civ, boolean capital) {
 		Ent cityEntity = createEntity("City " + name);			
 		PermissionComponent cityPerm = new PermissionComponent();
 		cityPerm.setAll(false);
 		cityPerm.setResidentBuild(true);
 		cityPerm.setResidentDestroy(true);
 		cityPerm.setResidentItemUse(true);
 		cityPerm.setResidentSwitch(true);
 		cityPerm.setName(name + " permissions");
 		//addComponent(cityEntity, cityPerm);
 		cityPerm.setEntityID(cityEntity);
     	plugin.getDatabase().save(cityPerm);
 		
 		CityComponent city = new CityComponent();
 		//addComponent(cityEntity, city);
 		city.setEntityID(cityEntity);
 		city.setName(name);
 		city.setCivilization(civ);
 		city.setCapital(capital);
 		city.setTaxes(0);
 		city.setRegistered(System.currentTimeMillis());
 		city.setTownBoard("Change me");
 		city.setCulture(10);
 		city.setSpawn_x(player.getLocation().getX());
 		city.setSpawn_y(player.getLocation().getY());
 		city.setSpawn_z(player.getLocation().getZ());
 		
 		city.setCharterWorld(player.getLocation().getWorld().getName());
 		city.setCharter_x(charter.getX());
 		city.setCharter_y(charter.getY());
 		city.setCharter_z(charter.getZ());
 		byte rotation = charter.getData();
 		city.setCharterRotation(rotation);
 		plugin.getDatabase().save(city);
 
 		mayor.setCity(city);
 		mayor.setMayor(true);
 		plugin.getDatabase().save(mayor);
 		
 		NewCityEvent nce = new NewCityEvent(city.getName(), city.getEntityID().getId());
 		Bukkit.getServer().getPluginManager().callEvent(nce);
 		
 		return city;
 	}
 	
 	public static CivilizationComponent createCiv(String name){
 		Ent civEntity = createEntity("Civ " + name);
 		CivilizationComponent civ = new CivilizationComponent();
 		civ.setName(name);
 		//addComponent(civEntity, civ);
 		civ.setEntityID(civEntity);
 		civ.setRegistered(System.currentTimeMillis());
 		civ.setTaxes(0);
     	plugin.getDatabase().save(civ);
 		
 		PermissionComponent civPerm = new PermissionComponent();
 		civPerm.setAll(false);
 		civPerm.setResidentBuild(true);
 		civPerm.setResidentDestroy(true);
 		civPerm.setResidentItemUse(true);
 		civPerm.setResidentSwitch(true);
 		civPerm.setName(name + " permissions");
 		
 		//addComponent(civEntity, civPerm);
 		civPerm.setEntityID(civEntity);
 		plugin.getDatabase().save(civPerm);
 		
 		NewCivEvent nce = new NewCivEvent(civ.getName(), civ.getEntityID().getId());
 		Bukkit.getServer().getPluginManager().callEvent(nce);
     	return civ;
 	}
 	
 	public static void claimPlot(int x, int z, Block plotSign, CityComponent city){
 		claimPlot(x, z, city.getName(), plotSign, city);
 	}
 	
 	public static void claimPlot(int x, int z, String name, Block plotSign, CityComponent city){
 		PlotComponent plot = getPlot(x, z);
 		if (plot == null){
 			Ent plotEnt = createEntity();
 			plot = new PlotComponent();
 			plot.setX(x);
 			plot.setZ(z);
 			//addComponent(plotEnt, plot);
 			plot.setEntityID(plotEnt);
 		}
 		plot.setCity(city);
 		plot.setName(name);
 		plot.setSignX(plotSign.getX());
 		plot.setSignY(plotSign.getY());
 		plot.setSignZ(plotSign.getZ());
 		plot.setWorld(plotSign.getWorld().getName());
 		plot.setType(CityPlotType.RESIDENTIAL);
 		plugin.getDatabase().save(plot);
 	}
     
     public static void broadcastToCity(String message, CityComponent city){
     	Set<ResidentComponent> residents = CivAPI.getResidents(city);
     	for (ResidentComponent resident : residents){
     		Player player = plugin.getServer().getPlayer(resident.getName());
     		if (player != null)
     			player.sendMessage(message);
     	}
     }
     
     public static void broadcastToCiv(String message, CivilizationComponent civ){
     	List<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civilization", civ).findList();
     	for (CityComponent city : cities){
     		broadcastToCity(message, city);
     	}
     }
     
     public static void loseTechs(ResidentComponent resident){
     	HashSet<Tech> techs = TechManager.getResearched(resident.getName());
 		System.out.println("Player leaving");
 		System.out.println("Known techs: " + Integer.toString(techs.size()));
 		HashSet<Tech> fiveToLose = new HashSet<Tech>();
 		HashSet<Tech> available;
 		try {
 			available = TechManager.getAvailableTech(resident.getName());
 			for (Tech t : available){
 				for (Tech c : t.children){
 					if (fiveToLose.size() >= 5)
 						break;
 					fiveToLose.add(c);
 				}
 			}
 			
 			if (fiveToLose.size() < 5){
 				for (Tech t : fiveToLose){
 					for (Tech c : t.children){
 						if (fiveToLose.size() >= 5)
 							break;
 						fiveToLose.add(c);
 					}
 				}
 			}
 			
 			techs.removeAll(fiveToLose);
 			OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(resident.getName());
 			TechManager.setTech((Player) player, techs);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
 
 	public static void disbandCity(CityComponent city) {
 		broadcastToCity("City disbanding", city);
 		String name = city.getName();
 		for (ResidentComponent resident : CivAPI.getResidents(city)){
 			resident.setCity(null);
 			resident.setMayor(false);
 			resident.setCityAssistant(false);
 			resident.setCivAssistant(false);
 			loseTechs(resident);
 			plugin.getDatabase().save(resident);
 		}
 		for (PlotComponent plot : getPlots(city)){
 			plot.setCity(null);
 			plot.setResident(null);
 			plugin.getDatabase().save(plot);
 		}
 		Ent civEnt = city.getCivilization().getEntityID();
 		CivilizationComponent civ = city.getCivilization();
 		
 		Ent cityEnt = city.getEntityID();
 		Bukkit.getServer().getPluginManager().callEvent(new DeleteCityEvent(city.getName(), city.getEntityID().getId()));
 		plugin.getDatabase().delete(city);
 		plugin.getDatabase().delete(plugin.getDatabase().find(PermissionComponent.class).where().eq("entityID", cityEnt).findUnique());
 		try{
 			plugin.getDatabase().delete(cityEnt);
 		} catch (Exception e){
 			Civilmineation.log(Level.WARNING, "A plugin did not properly let go of city entity " + Integer.toString(cityEnt.getId()));
 			Civilmineation.log(Level.WARNING, "Database maintenence will probably be needed");
 			e.printStackTrace();
 		}
 		
		List<CityComponent> cities = plugin.getDatabase().find(CityComponent.class).where().eq("civ", civ).findList();
		if (cities.isEmpty()){
 			Bukkit.getServer().getPluginManager().callEvent(new DeleteCivEvent(civ.getName(), civ.getEntityID().getId()));
 			plugin.getDatabase().delete(civ);
 			plugin.getDatabase().delete(plugin.getDatabase().find(PermissionComponent.class).where().eq("entityID", civEnt).findUnique());
 			try{
 				plugin.getDatabase().delete(civEnt);
 			} catch (Exception e){
 				Civilmineation.log(Level.WARNING, "A plugin did not properly let go of civ entity " + Integer.toString(civEnt.getId()));
 				Civilmineation.log(Level.WARNING, "Database maintenence will probably be needed");
 				e.printStackTrace();
 			}
 		} else {
			cities.get(0).setCapital(true);
			plugin.getDatabase().save(cities.get(0));
			broadcastToCiv(cities.get(0).getName() + " is now the Capital City!", civ);
 		}
 		
 		
 
 		plugin.getServer().broadcastMessage(name + " has fallen to dust!"); 
 	}
 	
 	public static void removeResident(ResidentComponent resident){
 		if (getResidents(resident.getCity()).size() == 1){
 			disbandCity(resident.getCity());
 			return;
 		}
 		loseTechs(resident);
 		CityComponent city = resident.getCity();
 		Set<PlotComponent> plots = plugin.getDatabase().find(PlotComponent.class).where().eq("city", city).eq("resident", resident).findSet();
 		for (PlotComponent plot : plots){
 			plot.setResident(null);
 			plugin.getDatabase().save(plot);
 			updatePlotSign(plot);
 		}
 		resident.setCity(null);
 		plugin.getDatabase().save(resident);
 		broadcastToCity(resident.getName() + " has left our city!", city);
 	}
 
 
 	public static Ent createEntity(){
     	Ent ent = new Ent();
     	plugin.getDatabase().save(ent);
     	return ent;
     }
 	
 	public static Ent createEntity(String name){
     	Ent ent = new Ent();
     	ent.setDebugName(name);
     	plugin.getDatabase().save(ent);
     	return ent;
     }
 
 	
 
 	
 
 }
