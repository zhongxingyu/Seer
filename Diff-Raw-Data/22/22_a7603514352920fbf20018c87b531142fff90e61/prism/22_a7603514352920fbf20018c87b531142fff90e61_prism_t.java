 package tv.mineinthebox.ManCo.utils;
 
 import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
 
 import org.bukkit.block.Block;
 
 public class prism {
 	public static void log(String p, Block block) {
		Prism.actionsRecorder.addToQueue(ActionFactory.create("block-place", block, p));
 	}
 
 }
