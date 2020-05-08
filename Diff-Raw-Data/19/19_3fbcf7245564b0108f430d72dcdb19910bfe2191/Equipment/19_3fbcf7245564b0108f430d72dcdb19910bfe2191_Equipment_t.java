 package rpisdd.rpgme.gamelogic.items;
 
 import rpisdd.rpgme.gamelogic.player.Player;
 import rpisdd.rpgme.gamelogic.player.Stats;
 import android.util.Log;
 
 public class Equipment extends Item {
 	public static enum Type {
 		WEAPON, ARMOR
 	}
 
 	public static final void loadEquipment() {
 	}
 
 	public static final Equipment SMALL_SWORD = new Equipment(Type.WEAPON,
 			"Small Sword", 100, "sword1_red.png", Stats.Mod.strMod(1));
 
	public static final Equipment ARMOR_1 = new Equipment(Type.ARMOR,
			"Steel Plate", 100, "armor1.png", Stats.Mod.strMod(1));

 	final Stats.Mod statMod;
 	final Type type;
 
 	private Equipment(Type type, String name, int price, String imageName,
 			Stats.Mod statMod) {
 		super(name, price, imageName);
 		this.statMod = statMod;
 		this.type = type;
 	}
 
 	@Override
 	public void useMe(Player p, int index) {
 		if (p.getInventory().getItems().get(index) != this) {
 			Log.wtf("items", "Item not in inventory before using it.");
 		}
 		if (type == Type.WEAPON) {
 			Equipment oldWeapon = p.getInventory().equipWeapon(this);
 			if (oldWeapon != null) {
 				p.getInventory().addItem(oldWeapon);
 			}
 		} else if (type == Type.ARMOR) {
 			Equipment oldArmor = p.getInventory().equipArmor(this);
 			if (oldArmor != null) {
 				p.getInventory().addItem(oldArmor);
 			}
 		}
 		p.getInventory().removeAt(index);
 	}
 
 	public Stats.Mod getMod() {
 		return this.statMod;
 	}
 
 }
