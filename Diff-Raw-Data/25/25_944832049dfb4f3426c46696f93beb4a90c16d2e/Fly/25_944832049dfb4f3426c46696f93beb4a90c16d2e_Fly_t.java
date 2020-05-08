 package net.lordsofcode.zephyrus.spells;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.lordsofcode.zephyrus.Zephyrus;
 import net.lordsofcode.zephyrus.api.ISpell;
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
 
 public class Fly extends Spell {
 
 	Map<String, Integer> list;
 
 	public Fly() {
 		list = new HashMap<String, Integer>();
 		Lang.add("spells.fly.applied", "$7You can now fly for [TIME] seconds");
 		Lang.add("spells.fly.warning", "$7Your wings start to dissappear!");
 		Lang.add("spells.fly.end", "$7Your wings were taken!");
 	}
 
 	@Override
 	public String getName() {
 		return "fly";
 	}
 
 	@Override
 	public String getDesc() {
 		return "Allows you to fly for 30 seconds.";
 	}
 
 	@Override
 	public int reqLevel() {
 		return 4;
 	}
 
 	@Override
 	public int manaCost() {
 		return 200;
 	}
 
 	@Override
 	public boolean run(Player player, String[] args, int power) {
 		int t = getConfig().getInt(getName() + ".duration");
		t = t*20*power;
 		Zephyrus.getUser(player).applyEffect(EffectType.FLY, t);
		player.setAllowFlight(true);
		player.sendMessage(Lang.get("spells.fly.applied").replace("[TIME]",
				Zephyrus.getUser(player).getEffectTime(EffectType.FLY)/20 + ""));
 		return true;
 	}
 
 	@Override
 	public Map<String, Object> getConfiguration() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("duration", 120);
 		return map;
 	}
 
 	@Override
 	public Set<ItemStack> items() {
 		Set<ItemStack> i = new HashSet<ItemStack>();
 		i.add(new ItemStack(Material.FEATHER, 32));
 		return i;
 	}
 
 	@Override
 	public ISpell getRequiredSpell() {
 		return Spell.forName("feather");
 	}
 
 	@Override
 	public Type getPrimaryType() {
 		return Type.RESTORE;
 	}
 
 	@Override
 	public Element getElementType() {
 		return Element.GENERIC;
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
