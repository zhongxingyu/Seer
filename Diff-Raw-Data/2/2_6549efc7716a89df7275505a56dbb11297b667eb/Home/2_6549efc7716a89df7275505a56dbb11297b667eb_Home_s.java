 package net.lordsofcode.zephyrus.spells;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.lordsofcode.zephyrus.api.ISpell;
 import net.lordsofcode.zephyrus.api.SpellTypes.Type;
 import net.lordsofcode.zephyrus.api.SpellTypes.Element;
 import net.lordsofcode.zephyrus.api.SpellTypes.Priority;
 import net.lordsofcode.zephyrus.utils.Lang;
 import net.lordsofcode.zephyrus.utils.PlayerConfigHandler;
 import net.lordsofcode.zephyrus.utils.effects.Effects;
 import net.lordsofcode.zephyrus.utils.effects.ParticleEffects;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class Home extends Spell {
 
 	public Home() {
 		Lang.add("spells.home.set", "Your home has been set!");
 		Lang.add("spells.home.applied", "Welcome home!");
 		Lang.add("spells.home.fail", "No homes set! Set one with '/cast home set'");
 	}
 
 	@Override
 	public String getName() {
 		return "home";
 	}
 
 	@Override
 	public String getDesc() {
 		return "Set your home with" + ChatColor.BOLD + "/cast home set!" + ChatColor.RESET
 				+ "Then go to your home with " + ChatColor.BOLD + "/cast home!";
 	}
 
 	@Override
 	public int reqLevel() {
 		return 3;
 	}
 
 	@Override
 	public int manaCost() {
 		return 200;
 	}
 
 	@Override
 	public boolean run(Player player, String[] args, int power) {
 		if (args.length < 2 && !isHomeSet(player)) {
 			Lang.errMsg("spells.home.fail", player);
 			return false;
 		}
 		if (args.length == 2 && args[1].equalsIgnoreCase("set")) {
 			setHome(player);
 			Lang.msg("spells.home.set", player);
 		} else {
 			goHome(player);
 			Lang.msg("spells.home.applied", player);
 			player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 1);
 			Effects.playEffect(Sound.ENDERMAN_TELEPORT, player.getLocation(), 1, 10);
 		}
 		return true;
 	}
 
 	@Override
 	public Set<ItemStack> items() {
 		Set<ItemStack> s = new HashSet<ItemStack>();
		s.add(new ItemStack(Material.WOODEN_DOOR));
 		s.add(new ItemStack(Material.BED));
 		s.add(new ItemStack(Material.FURNACE));
 		return s;
 	}
 
 	private void setHome(Player player) {
 		FileConfiguration cfg = PlayerConfigHandler.getConfig(player);
 		Location loc = player.getLocation();
 		cfg.set("spell.home.x", loc.getX());
 		cfg.set("spell.home.y", loc.getY());
 		cfg.set("spell.home.z", loc.getZ());
 		cfg.set("spell.home.yaw", loc.getYaw());
 		cfg.set("spell.home.pitch", loc.getPitch());
 		cfg.set("spell.home.world", loc.getWorld().getName());
 		PlayerConfigHandler.saveConfig(player, cfg);
 	}
 
 	private boolean isHomeSet(Player player) {
 		FileConfiguration cfg = PlayerConfigHandler.getConfig(player);
 		return cfg.contains("spell.home.x");
 	}
 
 	private void goHome(Player player) {
 		FileConfiguration cfg = PlayerConfigHandler.getConfig(player);
 		int x = cfg.getInt("spell.home.x");
 		int y = cfg.getInt("spell.home.y");
 		int z = cfg.getInt("spell.home.z");
 		int yaw = cfg.getInt("spell.home.yaw");
 		int pitch = cfg.getInt("spell.home.pitch");
 		String world = cfg.getString("spell.home.world");
 		Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
 		player.teleport(loc);
 		Effects.playEffect(ParticleEffects.ENDER, loc, 0, 0, 0, 2, 20);
 		player.getWorld().playSound(loc, Sound.ENDERMAN_TELEPORT, 10, 1);
 	}
 
 	@Override
 	public Type getPrimaryType() {
 		return Type.TELEPORTATION;
 	}
 
 	@Override
 	public Element getElementType() {
 		return Element.ENDER;
 	}
 
 	@Override
 	public Priority getPriority() {
 		return Priority.LOW;
 	}
 
 	@Override
 	public Map<String, Object> getConfiguration() {
 		return null;
 	}
 
 	@Override
 	public boolean sideEffect(Player player, String[] args) {
 		return false;
 	}
 	
 	@Override
 	public ISpell getRequiredSpell() {
 		return Spell.forName("blink");
 	}
 
 }
