 package net.minecore.minepermit;
 
 import net.minecore.minepermit.miner.PermitMiner;
 import net.minecore.minepermit.miner.PermitMinerManager;
 import net.minecore.minepermit.permits.Permit;
 import net.minecore.minepermit.world.PermitArea;
 import net.minecore.minepermit.world.WorldPermitAreaManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 
 public class BlockListener implements Listener {
 
 	private final PermitMinerManager mm;
 	private final WorldPermitAreaManager am;
 
 	public BlockListener(MinePermit mp) {
 		mm = mp.getPermitMinerManager();
 		am = mp.getWorldPermitAreaManager();
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent e) {
 
 		Player p = e.getPlayer();
 
 		if (p.hasPermission("MinePermit.exempt"))
 			return;
 
 		PermitArea pa = am.getRelevantPermitArea(e.getBlock().getLocation());
 		if (pa.getEffectiveDepth() < e.getBlock().getY())
 			return;
 
 		if (!pa.requiresPermit(e.getBlock().getType())) {
 
 			if (!pa.getAllowMiningUnspecifiedBlocks()) {
 				e.setCancelled(true);
 				p.sendMessage(ChatColor.DARK_GRAY
 						+ "You may not mine these blocks! Mining blocks that cannot have permits bought for is not allowed here.");
 			}
 			return;
 		}
 
 		PermitMiner pm = mm.getPermitMiner(p);

 		Permit permit = pm.getPermit(pa, e.getBlock().getType());
 
 		if (permit == null) {
			permit = pm.getUniversalPermit(pa);
		}

		if (permit == null) {
 			e.setCancelled(true);
 			p.sendMessage(ChatColor.DARK_RED
 					+ "You may not mine these blocks! Use /permit buy <name> to buy a permit.");
 		} else {
 			permit.breakBlock(e.getBlock());
 		}
 
 	}
 }
