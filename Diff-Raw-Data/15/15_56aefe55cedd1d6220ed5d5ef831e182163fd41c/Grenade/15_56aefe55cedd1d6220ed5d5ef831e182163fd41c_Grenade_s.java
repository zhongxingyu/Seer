 package net.lordsofcode.zephyrus.spells;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import net.lordsofcode.zephyrus.Zephyrus;
 import net.lordsofcode.zephyrus.hooks.PluginHook;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.WitherSkull;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class Grenade extends Spell {
 
 	public Grenade(Zephyrus plugin) {
 		super(plugin);
 	}
 
 	@Override
 	public String name() {
 		return "grenade";
 	}
 
 	@Override
 	public String bookText() {
 		return "Shoots a big explody thing :D";
 	}
 
 	@Override
 	public int reqLevel() {
 		return 8;
 	}
 
 	@Override
 	public int manaCost() {
		return 80;
 	}
 
 	@Override
 	public void run(Player player, String[] args) {
 		player.launchProjectile(WitherSkull.class);
 	}
 	
 	@Override
 	public boolean canRun(Player player, String[] args) {
 		if (PluginHook.canBuild(player, player.getTargetBlock(null, 1000))) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public String failMessage() {
 		return ChatColor.DARK_RED + "You can't shoot there!";
 	}
 
 	@Override
 	public Set<ItemStack> spellItems() {
 		Set<ItemStack> s = new HashSet<ItemStack>();
 		s.add(new ItemStack(Material.getMaterial(1), 3, (short) 1));
 		return s;
 	}
 	
 	@Override
 	public SpellType type() {
 		return SpellType.ELEMENTAL;
 	}
 
 }
