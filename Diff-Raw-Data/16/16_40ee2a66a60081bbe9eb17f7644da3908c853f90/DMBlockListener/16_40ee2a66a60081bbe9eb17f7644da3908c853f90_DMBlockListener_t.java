 package fghjconner.DonationMeter;
 
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.material.Sign;
 
 public class DMBlockListener extends BlockListener
 {
 	private DonationMeter plugin;
 	public DMBlockListener (DonationMeter plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		SimpleLoc loc = SimpleLoc.simplify(event.getBlock().getLocation());
 		ArrayList<Meter> affected = new ArrayList<Meter>(); 
 		for (Meter meter:plugin.meterList)
 		{
 			if (meter.has(loc))
 			{
 				affected.add(meter);
 			}
 		}
 		for (Meter marked:affected)
 		{
 			marked.destroy();
 			event.getPlayer().sendMessage(ChatColor.RED.toString()+"Meter destroyed");
 		}
 	}
 
 	public void onBlockBurn(BlockBurnEvent event)
 	{
 		SimpleLoc loc = SimpleLoc.simplify(event.getBlock().getLocation());
 		ArrayList<Meter> affected = new ArrayList<Meter>(); 
 		for (Meter meter:plugin.meterList)
 		{
 			if (meter.has(loc))
 			{
 				affected.add(meter);
 			}
 		}
 		for (Meter marked:affected)
 		{
 			marked.destroy();
 		}
 	}
 
 	public void onSignChange(SignChangeEvent event)
 	{
		if (event.isCancelled())
			return;
 		Boolean reverse;
 		Block sign = event.getBlock();
 		SimpleLoc loc = SimpleLoc.simplify(sign.getLocation());
 		Block base = sign.getFace(((Sign)sign.getState().getData()).getAttachedFace());
 		if (plugin.opPermissions ? !event.getPlayer().isOp() : !DonationMeter.permissionHandler.has(event.getPlayer(), "DonationMeter.admin"))
 			return;
 		if ((event.getLine(0).toLowerCase().contains("dmeter") || event.getLine(0).toLowerCase().contains("donations")) && base.getType().equals(Material.WOOL) && !isMeter(base))
 		{
 			reverse = event.getLine(0).toLowerCase().contains("-r");
 			plugin.meterList.add(new WoolMeter(base,reverse,event));
 			return;
 		}
 		Boolean isSignMeter = false;
 		for (int i = 0; i < 4; i++)
 		{
 			String line = event.getLine(i).toLowerCase();
 			if (line.contains("[have]") || line.contains("[need]") || line.contains("[extr]") || line.contains("[goal]") || line.contains("[perc]"))
 			{
 				isSignMeter = true;
 			}
 		}
 		if (isSignMeter)
 		{
 			String[] lines = new String[4];
 			System.arraycopy(event.getLines(), 0, lines, 0, 4);
 			plugin.meterList.add(new SignMeter(loc, lines));
 			event.getPlayer().sendMessage(ChatColor.GREEN.toString()+"SignMeter Created!");
 		}
 	}
 
 	private boolean isMeter(Block base)
 	{
 		for (Meter meter:plugin.meterList)
 		{
 			if (meter.has(SimpleLoc.simplify(base.getLocation())))
 				return true;
 		}
 		return false;
 	}
 }
