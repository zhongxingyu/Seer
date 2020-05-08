 package tv.mineinthebox.ManCo.utils;
 
 import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.MatchRule;
import me.botsko.prism.actionlibs.QueryParameters;
 
import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 
 public class prism {
	
	@SuppressWarnings("deprecation")
 	public static void log(String p, Block block) {
		Prism prism = (Prism) Bukkit.getPluginManager().getPlugin("Prism");
		QueryParameters param = new QueryParameters();
		param.setWorld(block.getWorld().getName());
		param.addActionType("block-place", MatchRule.INCLUDE);
		param.setRadius(20);
        param.setLimit(100);
        param.addPlayerName(p);
        param.addSpecificBlockLocation(block.getLocation());
        param.addBlockFilter(block.getTypeId(), block.getData());
        ActionsQuery doQuery = new ActionsQuery(prism);
        doQuery.lookup(param);
 	}
 
 }
