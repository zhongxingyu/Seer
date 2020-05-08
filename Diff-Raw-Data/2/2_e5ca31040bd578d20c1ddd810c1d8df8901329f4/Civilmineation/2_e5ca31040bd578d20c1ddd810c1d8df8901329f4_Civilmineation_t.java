 package net.croxis.plugins.civilmineation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.CivilizationComponent;
 import net.croxis.plugins.civilmineation.components.Component;
 import net.croxis.plugins.civilmineation.components.EconomyComponent;
 import net.croxis.plugins.civilmineation.components.Ent;
 import net.croxis.plugins.civilmineation.components.PermissionComponent;
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.research.Tech;
 import net.croxis.plugins.research.TechManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.milkbowl.vault.economy.Economy;
 
 public class Civilmineation extends JavaPlugin implements Listener {
 	public static CivAPI api;
 	private static final Logger logger = Logger.getLogger("Minecraft");
 	
 	public static void log(String message){
 		logger.info("[Civ] " + message);
 	}
 	
 	public static void log(Level level, String message){
 		logger.info("[Civ] " + message);
 	}
 	
 	public static void logDebug(String message){
 		logger.info("[Civ][Debug] " + message);
 	}
 	
     public void onDisable() {
         // TODO: Place any custom disable code here.
     }
     
     private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         CivAPI.econ = rsp.getProvider();
         return CivAPI.econ != null;
     }
 
     public void onEnable() {
     	setupDatabase();
     	
     	api = new CivAPI(this);
     	if (!setupEconomy() ) {
             log(Level.SEVERE, "Disabled due to no Vault dependency found!");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         getServer().getPluginManager().registerEvents(this, this);
         getServer().getPluginManager().registerEvents(new ActionPermissionListener(), this);
         getServer().getPluginManager().registerEvents(new SignInteractListener(), this);
         
         getCommand("tech").setExecutor(new TechCommand(this));
         
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
         	public void run(){
         		logDebug("Running Turn");
         		for (Player player : getServer().getOnlinePlayers()){
         			ResidentComponent resident = CivAPI.getResident(player);
         			if (resident.getCity() != null){
         				CivAPI.addCulture(resident.getCity(), 1);
         				CivAPI.addResearch(resident.getCity(), 1);
         			} else {
         				Tech learned = TechManager.addPoints(player, 1);
         				if(learned != null)
         					player.sendMessage("You have learned a technology!");
         			}
         		}
         	}
         }, 36000, 36000);
     }
     
     public Ent createEntity(){
     	Ent ent = new Ent();
     	getDatabase().save(ent);
     	return ent;
     }
     
     public Ent createEntity(String name){
     	Ent ent = new Ent();
     	ent.setDebugName(name);
     	getDatabase().save(ent);
     	return ent;
     }
     
     public void addComponent(Ent ent, Component component){
     	component.setEntityID(ent);
     	getDatabase().save(component);
     	return;
     }
     
     @Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(Ent.class);
 		list.add(ResidentComponent.class);
         list.add(CityComponent.class);
         list.add(CivilizationComponent.class);
         list.add(PlotComponent.class);
         list.add(PermissionComponent.class);
         list.add(EconomyComponent.class);
 		return list;
 	}
     
     private void setupDatabase()
 	{
 		try
 		{
 			getDatabase().find(Ent.class).findRowCount();
 		}
 		catch(PersistenceException ex)
 		{
 			System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
 			installDDL();
 		}
 		//Ent ent = createEntity();
 		//CivilizationComponent wilderness = new CivilizationComponent();
 		//wilderness.setName("Wilderness");
 		//addComponent(ent, wilderness);
 	}
     
     @EventHandler
     public void onSignChangeEvent(SignChangeEvent event){
     	if (event.getLine(0).equalsIgnoreCase("[New Civ]")){
     		ResidentComponent resident = CivAPI.getResident(event.getPlayer().getName());
     		if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty()){
     			event.getPlayer().sendMessage("Civ name on second line, Capital name on third line");
     			event.setCancelled(true);
     			return;
     		}
 			CivilizationComponent civComponent = getDatabase().find(CivilizationComponent.class).where().ieq("name", event.getLine(1)).findUnique();
 			CityComponent cityComponent = getDatabase().find(CityComponent.class).where().ieq("name", event.getLine(2)).findUnique();
 			if (civComponent != null || cityComponent != null){
 				event.getPlayer().sendMessage("That civ or city name already exists");
 				event.setCancelled(true);
 				return;
 			}			
 			if (resident.getCity() != null){
 				event.getPlayer().sendMessage("You must leave your city first.");
 				event.setCancelled(true);
 				return;
 			}
 			PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
 			if (plot != null){
 				if (plot.getCity() != null){
 					event.getPlayer().sendMessage("That plot is part of a city.");
 					event.setCancelled(true);
 					return;
 				}
 			}
 			//TODO: Distance check to another city
 			//TODO: Check for room for interface placements
 			Ent civEntity = createEntity("Civ " + event.getLine(1));
 			CivilizationComponent civ = new CivilizationComponent();
 			civ.setName(event.getLine(1));
 			//addComponent(civEntity, civ);
 			civ.setEntityID(civEntity);
 			civ.setRegistered(System.currentTimeMillis());
 			civ.setTaxes(0);
 	    	getDatabase().save(civ);
 	    	
 	    	EconomyComponent civEcon = new EconomyComponent();
 	    	civEcon.setName(event.getLine(1));
 	    	civEcon.setEntityID(civEntity);
 	    	getDatabase().save(civEcon);
 	    	CivAPI.econ.createPlayerAccount(event.getLine(1));
 			
 			PermissionComponent civPerm = new PermissionComponent();
 			civPerm.setAll(false);
 			civPerm.setResidentBuild(true);
 			civPerm.setResidentDestroy(true);
 			civPerm.setResidentItemUse(true);
 			civPerm.setResidentSwitch(true);
 			civPerm.setName(event.getLine(1) + " permissions");
 			
 			//addComponent(civEntity, civPerm);
 			civPerm.setEntityID(civEntity);
 	    	getDatabase().save(civPerm);
 	    	
 	    	ResidentComponent mayor = CivAPI.getResident(event.getPlayer());
 			
	    	CityComponent city = CivAPI.createCity(event.getLine(2), event.getPlayer(), mayor, event.getBlock(), civ, true);
 			
 			if (plot == null){
 				Ent plotEnt = createEntity();
 				plot = new PlotComponent();
 				plot.setX(event.getBlock().getChunk().getX());
 				plot.setZ(event.getBlock().getChunk().getZ());
 				//addComponent(plotEnt, plot);
 				plot.setEntityID(plotEnt);
 			}
 			plot.setCity(city);
 			plot.setName(city.getName() + " Founding Square");
 			event.getBlock().getRelative(BlockFace.UP).setTypeIdAndData(68, city.getCharterRotation(), true);
 			Sign plotSign = (Sign) event.getBlock().getRelative(BlockFace.UP).getState();
 			plotSign.setLine(0, city.getName());
 			plotSign.update();
 			plot.setSignX(plotSign.getX());
 			plot.setSignY(plotSign.getY());
 			plot.setSignZ(plotSign.getZ());
 			getDatabase().save(plot);
 			
 			event.setLine(0, ChatColor.BLUE + "City Charter");
 			event.setLine(3, "Mayor " + event.getPlayer().getName());
 			event.getBlock().getRelative(BlockFace.DOWN).setTypeIdAndData(68, city.getCharterRotation(), true);
 			//event.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setTypeIdAndData(68, rotation, true);
 			CivAPI.updateCityCharter(city);
 			
 			
 			
     	} else if (event.getLine(0).equalsIgnoreCase("[claim]")){
     		ResidentComponent resident = CivAPI.getResident(event.getPlayer());
     		if (resident.getCity() == null){
     			event.setCancelled(true);
     			event.getPlayer().sendMessage("You must be part of a city admin");
     			return;
     		} else if (!resident.isCityAssistant()
     				&& !resident.isMayor()){
     			event.setCancelled(true);
     			event.getPlayer().sendMessage("You must be a city admin");
     			return;
     		}
     		PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
     		if (plot != null){
     			if (plot.getCity() != null){
     				event.setCancelled(true);
     				event.getPlayer().sendMessage("A city has already claimed this chunk");
     				return;
     			}
     		} else if (resident.getCity().getCulture() < Math.pow(CivAPI.getPlots(resident.getCity()).size(), 1.5)){
     			event.setCancelled(true);
 				event.getPlayer().sendMessage("You do not have enough culture");
 				return;
     		}
     		//CivAPI.addCulture(resident.getCity(), (int) -(Math.pow(CivAPI.getPlots(resident.getCity()).size(), 1.5)));
     		if (plot == null){
 				Ent plotEnt = createEntity();
 				plot = new PlotComponent();
 				plot.setX(event.getBlock().getChunk().getX());
 				plot.setZ(event.getBlock().getChunk().getZ());
 				//addComponent(plotEnt, plot);
 				plot.setEntityID(plotEnt);
 			}
 			plot.setCity(resident.getCity());
 			plot.setName(resident.getCity().getName());
 			plot.setSignX(event.getBlock().getX());
 			plot.setSignY(event.getBlock().getY());
 			plot.setSignZ(event.getBlock().getZ());
 			getDatabase().save(plot);
 			event.setLine(0, resident.getCity().getName());
     	} else if (event.getLine(0).equalsIgnoreCase("[new city]")){
     		if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty()){
     			event.getPlayer().sendMessage("City name on second line, Mayor name on third line");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent resident = CivAPI.getResident(event.getPlayer());
     		if (!CivAPI.isNationalAdmin(resident)){
     			event.getPlayer().sendMessage("You must be a national leader or assistant.");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent mayor = CivAPI.getResident(event.getLine(2));
     		if (mayor == null){
     			event.getPlayer().sendMessage("That player does not exist.");
     			event.setCancelled(true);
     			return;
     		}
 			CityComponent cityComponent = getDatabase().find(CityComponent.class).where().ieq("name", event.getLine(2)).findUnique();
 			if (cityComponent != null){
 				event.getPlayer().sendMessage("That city name already exists");
 				event.setCancelled(true);
 				return;
 			}			
 			if (mayor.getCity() == null){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (!mayor.getCity().getCivilization().getName().equalsIgnoreCase(resident.getCity().getCivilization().getName())){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (mayor.isMayor()){
 				event.getPlayer().sendMessage("That player can not be am existing mayor.");
 				event.setCancelled(true);
 				return;
 			}
 			PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
 			if (plot != null){
 				if (plot.getCity() != null){
 					event.getPlayer().sendMessage("That plot is part of a city.");
 					event.setCancelled(true);
 					return;
 				}
 			}
 			CityComponent city = CivAPI.createCity(event.getLine(1), event.getPlayer(), mayor, event.getBlock(), mayor.getCity().getCivilization(), false);
 			if (plot == null){
 				Ent plotEnt = createEntity();
 				plot = new PlotComponent();
 				plot.setX(event.getBlock().getChunk().getX());
 				plot.setZ(event.getBlock().getChunk().getZ());
 				//addComponent(plotEnt, plot);
 				plot.setEntityID(plotEnt);
 			}
 			plot.setCity(city);
 			plot.setName(city.getName() + " Founding Square");
 			getDatabase().save(plot);
 			
 			event.setLine(0, ChatColor.BLUE + "City Charter");
 			event.setLine(1, city.getCivilization().getName());
 			event.setLine(2, city.getName());
 			event.setLine(3, "Mayor " + mayor.getName());
 			event.getBlock().getRelative(BlockFace.DOWN).setTypeIdAndData(68, city.getCharterRotation(), true);
 			//event.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setTypeIdAndData(68, rotation, true);
 			CivAPI.updateCityCharter(city);
     	} else if (event.getLine(0).equalsIgnoreCase("[civ assist]")){
     		ResidentComponent king = CivAPI.getResident(event.getPlayer());
     		event.getBlock().breakNaturally();
     		if (event.getLine(1).isEmpty()){
     			event.getPlayer().sendMessage("Assistant name on the second line.");
     			event.setCancelled(true);
     			return;
     		} else if (!CivAPI.isKing(king)){
     			event.getPlayer().sendMessage("You must be a king.");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent assistant = CivAPI.getResident(event.getLine(1));
     		
     		if (assistant == null){
     			event.getPlayer().sendMessage("That player does not exist.");
     			event.setCancelled(true);
     			return;
     		}						
 			if (assistant.getCity() == null){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (!king.getCity().getCivilization().getName().equalsIgnoreCase(assistant.getCity().getCivilization().getName())){
 				event.getPlayer().sendMessage("That player must be in your civ!.");
 				event.setCancelled(true);
 				return;
 			}
 			assistant.setCivAssistant(!assistant.isCivAssistant());
 			getDatabase().save(assistant);
 			if(assistant.isCivAssistant())
 				CivAPI.broadcastToCiv(assistant.getName() + " is now a civ assistant!", king.getCity().getCivilization());
 			else
 				CivAPI.broadcastToCiv(assistant.getName() + " is no longer a civ assistant!", king.getCity().getCivilization());
 			return;
     	} else if (event.getLine(0).equalsIgnoreCase("[city assist]")){
     		ResidentComponent mayor = CivAPI.getResident(event.getPlayer());
     		event.getBlock().breakNaturally();
     		if (event.getLine(1).isEmpty()){
     			event.getPlayer().sendMessage("Assistant name on the second line.");
     			event.setCancelled(true);
     			return;
     		} else if (!mayor.isMayor()){
     			event.getPlayer().sendMessage("You must be a mayor.");
     			event.setCancelled(true);
     			return;
     		}
     		ResidentComponent assistant = CivAPI.getResident(event.getLine(1));
     		if (assistant == null){
     			event.getPlayer().sendMessage("That player does not exist.");
     			event.setCancelled(true);
     			return;
     		}						
 			if (assistant.getCity() == null){
 				event.getPlayer().sendMessage("That player must be in your city!.");
 				event.setCancelled(true);
 				return;
 			}
 			if (!mayor.getCity().getName().equalsIgnoreCase(assistant.getCity().getName())){
 				event.getPlayer().sendMessage("That player must be in your city!.");
 				event.setCancelled(true);
 				return;
 			}
 			assistant.setCityAssistant(!assistant.isCityAssistant());
 			getDatabase().save(assistant);
 			if(assistant.isCityAssistant())
 				CivAPI.broadcastToCity(assistant.getName() + " is now a city assistant!", mayor.getCity());
 			else
 				CivAPI.broadcastToCity(assistant.getName() + " is no longer a city assistant!", mayor.getCity());
 			return;
     	}
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
     	ResidentComponent resident = CivAPI.getResident(event.getPlayer());
     	if (resident == null){
     		Ent entity = createEntity();
     		
     		EconomyComponent civEcon = new EconomyComponent();
 	    	civEcon.setName(event.getPlayer().getName());
 	    	civEcon.setEntityID(entity);
 	    	getDatabase().save(civEcon);
     		
     		resident = new ResidentComponent();
     		resident.setRegistered(System.currentTimeMillis());
     		resident.setName(event.getPlayer().getName());
     		//addComponent(entity, resident);
     		resident.setEntityID(entity);
         	getDatabase().save(resident);
     	}
     	String title = "";
     	if (resident.isMayor()){
     		title = "Mayor ";
 	    	if (resident.getCity().isCapital())
 	    		title = "King ";
     	} else if (resident.getCity() == null)
     		title = "Wild ";
         event.getPlayer().sendMessage("Welcome, " + title + event.getPlayer().getDisplayName() + "!");
     }
     
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event){
     	if (event.getFrom().getWorld().getChunkAt(event.getFrom()).getX() 
     			!= event.getTo().getWorld().getChunkAt(event.getTo()).getX() 
     			|| event.getFrom().getWorld().getChunkAt(event.getFrom()).getZ() 
     			!= event.getTo().getWorld().getChunkAt(event.getTo()).getZ()){
     		//event.getPlayer().sendMessage("Message will go here");
     		PlotComponent plot = CivAPI.getPlot(event.getTo().getChunk());
     		PlotComponent plotFrom = CivAPI.getPlot(event.getFrom().getChunk());
     		if (plot == null && plotFrom != null){
     			event.getPlayer().sendMessage("Wilds");
     		} else if (plot == null && plotFrom == null){
     			// Needed to prevent future NPES
     		} else if (plot != null && plotFrom == null){
     			// TODO: City enter event
     			event.getPlayer().sendMessage(plot.getName());
     		} else if (!plot.getName().equalsIgnoreCase(plotFrom.getName())){
     			event.getPlayer().sendMessage(plot.getName());
     		}
     	}
     	
     }
 }
 
