 package org.spellcraft;
 
import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
 import org.getspout.spoutapi.keyboard.Keyboard;
 import org.getspout.spoutapi.material.MaterialData;
 import org.getspout.spoutapi.plugin.SpoutPlugin;
import org.spellcraft.castable.Spell;
 import org.spellcraft.executor.SpellCommandExecutor;
 import org.spellcraft.executor.SpellsKeyBindingExecutor;
 import org.spellcraft.item.Scepter;
 import org.spellcraft.listener.ScPlayerListener;
 import org.spellcraft.plugin.SpellCraftPlugin;
 
 public class SpellCraft extends SpoutPlugin {
 
 	public static Scepter goldenScepter;
 	private final SpellCommandExecutor spellCommandExecutor = new SpellCommandExecutor(this);
 	private final ScPlayerListener playerListener = new ScPlayerListener(this);
 	private final SpellsKeyBindingExecutor bindingExecutor = new SpellsKeyBindingExecutor(this);
 	private Logger log = Logger.getLogger("Minecraft");
 	public Logger getSpellCraftLogger() { return log; }
 	
 	private static ArrayList<Class<?>> masterSpellList = new ArrayList<Class<?>>();
 	public ArrayList<Class<?>> getSpellList() { return masterSpellList; }
 
 	private static HashMap<String, PlayerData> playerData = new HashMap<String, PlayerData>();
 	public PlayerData getPlayerData(Player player) { return playerData.get(player.getName()); }
 	public void deletePlayerData(Player player) { playerData.remove(player.getName()); }
 	public void newPlayerData(Player player) { playerData.put(player.getName(), new PlayerData(this,player)); }
 
 	public void onEnable()
 	{
 		log.info("SpellCraft loading...");
 		PluginManager pm = this.getServer().getPluginManager();
 		getCommand("spellinfo").setExecutor(spellCommandExecutor);
 		getCommand("listspells").setExecutor(spellCommandExecutor);
 		getCommand("setspell").setExecutor(spellCommandExecutor);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		SpoutManager.getKeyBindingManager().registerBinding("CAST_SPELL", Keyboard.KEY_C, "Cast Spell", bindingExecutor, this);
 		SpoutManager.getKeyBindingManager().registerBinding("SCROLL_SPELLS", Keyboard.KEY_X, "Scroll to Next Spell", bindingExecutor, this);
 		SpoutManager.getKeyBindingManager().registerBinding("TARGET", Keyboard.KEY_R, "Set Target", bindingExecutor, this);	
 		SpoutManager.getKeyBindingManager().registerBinding("SELF_TARGET", Keyboard.KEY_V, "Target Yourself", bindingExecutor, this);	
 		SpoutManager.getKeyBindingManager().registerBinding("UNLOCK_TARGET", Keyboard.KEY_B, "Unlock Target", bindingExecutor, this);	
 		SpoutManager.getKeyBindingManager().registerBinding("TOGGLE_CLICK_TO_CAST", Keyboard.KEY_Z, "Toggle ClickToCast", bindingExecutor, this);	
 
 		for (Player player : this.getServer().getOnlinePlayers()) { newPlayerData(player); } // Set values for online players.
 
 		SpoutManager.getFileManager().addToPreLoginCache(this,"http://images.7dunce.com/GoldenScepter.png");
 		goldenScepter = new Scepter(this, "Golden Scepter", "http://images.7dunce.com/GoldenScepter.png");
 		SpoutShapedRecipe recipe = new SpoutShapedRecipe(new SpoutItemStack(goldenScepter,1));
 		recipe.shape("SGS", "0S0", "0S0");
 		recipe.setIngredient('S', MaterialData.stick);
 		recipe.setIngredient('G', MaterialData.goldBlock);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
 
 		log.info("Spellcraft loaded."); // We've made it this far...
 	}
 
 	
 	public void registerSpell(Class<?> spellClass,SpellCraftPlugin plugin)
 	{
 		try {
 			if (Class.forName("org.spellcraft.castable.Spell").isAssignableFrom(spellClass))
 			{
 				masterSpellList.add(spellClass);
 				log.info("SpellCraft added " + spellClass.getSimpleName()  + " from " + plugin.getName() + ".");
 			}
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void onDisable()
 
 	{
 		log.info("Spells v2.0 disabled.");
 	}
 
 }
