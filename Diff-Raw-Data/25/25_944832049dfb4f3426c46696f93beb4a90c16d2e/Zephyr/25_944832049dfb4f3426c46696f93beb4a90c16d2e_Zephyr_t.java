 package net.lordsofcode.zephyrus.spells;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.lordsofcode.zephyrus.Zephyrus;
 import net.lordsofcode.zephyrus.api.SpellTypes.Element;
 import net.lordsofcode.zephyrus.api.SpellTypes.Priority;
 import net.lordsofcode.zephyrus.api.SpellTypes.Type;
 import net.lordsofcode.zephyrus.effects.EffectType;
 import net.lordsofcode.zephyrus.utils.Lang;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Zephyrus
  * 
  * @author minnymin3
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class Zephyr extends Spell {
 
 	public Zephyr() {
 		Lang.add("zephyrus.spells.zephyr.warning", "$7You feel the air slow down...");
 		Lang.add("zephyrus.spells.zephyr.removed", "$7The air goes still around you...");
 		Lang.add("zephyrus.spells.zephyr.applied", "$Whirlwinds protect you for TIME seconds");
 	}
 	
 	@Override
 	public String getName() {
 		return "zephyr";
 	}
 
 	@Override
 	public String getDesc() {
 		return "Keep enemies away with a small whirlwind";
 	}
 
 	@Override
 	public int reqLevel() {
 		return 7;
 	}
 
 	@Override
 	public int manaCost() {
 		return 400;
 	}
 
 	@Override
 	public boolean run(Player player, String[] args, int power) {
 		int time = getConfig().getInt(getName() + ".duration");
 		time *= power;
		Zephyrus.getUser(player).applyEffect(EffectType.ZEPHYR, time*20);
 		player.sendMessage(Lang.get("zephyrus.spells.fireshield.applied").replace("TIME",
				Zephyrus.getUser(player).getEffectTime(EffectType.FIRESHIELD)/20 + ""));
 		return true;
 	}
 
 	@Override
 	public Set<ItemStack> items() {
 		Set<ItemStack> i = new HashSet<ItemStack>();
 		i.add(new ItemStack(Material.FEATHER, 8));
 		i.add(new ItemStack(Material.DIAMOND_CHESTPLATE));
 		return i;
 	}
 
 	@Override
 	public Map<String, Object> getConfiguration() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("duration", 60);
 		map.put("block-all", true);
 		map.put("power", 3);
 		return map;
 	}
 
 	@Override
 	public Type getPrimaryType() {
 		return Type.BUFF;
 	}
 
 	@Override
 	public Element getElementType() {
 		return Element.AIR;
 	}
 
 	@Override
 	public Priority getPriority() {
 		return Priority.LOW;
 	}
 
 	@Override
 	public boolean sideEffect(Player player, String[] args) {
 		return false;
 	}
 
 }
