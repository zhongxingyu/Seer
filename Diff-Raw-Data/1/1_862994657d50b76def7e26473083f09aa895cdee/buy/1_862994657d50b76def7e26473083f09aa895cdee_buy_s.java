 package nl.giantit.minecraft.GiantShop.core.Commands;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Database.db;
 import nl.giantit.minecraft.GiantShop.core.perm;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 
 /**
  *
  * @author Giant
  */
 public class buy {
 	static config conf = config.Obtain();
 	static db database = db.Obtain();
 	static perm perms = perm.Obtain();
 	static Messages mH = GiantShop.getPlugin().getMsgHandler();
 	
 	public static void buy(Player player, String[] args) {
 		Heraut.savePlayer(player);
 		if(perms.has(player, "giantshop.shop.buy")) {
 			Heraut.say("test");
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "buy");
 
 			Heraut.say(mH.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 	
 	public static void gift(Player player, String[] args) {
 		Heraut.savePlayer(player);
 		Heraut.say("test");
 	}
 }
