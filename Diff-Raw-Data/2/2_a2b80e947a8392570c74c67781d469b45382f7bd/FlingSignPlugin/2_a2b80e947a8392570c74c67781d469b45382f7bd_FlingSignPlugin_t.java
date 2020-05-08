 package com.ricochet1k.bukkit.powersigns.plugins;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.util.BlockVector;
 import org.bukkit.util.Vector;
 
 import com.ricochet1k.bukkit.powersigns.PowerSigns;
 
 public class FlingSignPlugin extends AimedSign
 {
 	public FlingSignPlugin()
 	{
 		super("\\s+([fblr])");
 	}
 
 	public static void register()
 	{
 		PowerSigns.register("fling", AimedSign.syntax + " [fblr]  /  (0-999) (0-99)", new FlingSignPlugin());
 	}
 
 	static final Pattern flingBallisticsPattern = Pattern.compile("(\\d{1,3})\\s+(\\d{1,2})");
 
 	@Override
 	public boolean doPowerSign(PowerSigns plugin, Block signBlock, String action, Matcher argsm, Boolean isOn,
 			BlockFace signDir, BlockFace forward, Block startBlock)
 	{
 		Sign signState = (Sign) signBlock.getState();
 
		BlockFace flingDir = PowerSigns.strToDirection(argsm.group(1), signDir);
 
 		// get the target area
 		Block targArea = startBlock;
 
 		// check the target area is empty
 		String ballisticsLine = signState.getLine(1);
 		if (!PowerSigns.isFlingable(targArea)) return plugin.debugFail("target blocked");
 
 		// get the ballistics
 		Matcher m = flingBallisticsPattern.matcher(ballisticsLine);
 		if (!m.matches()) return plugin.debugFail("parse ballistics");
 		int power = Integer.parseInt(m.group(1));
 		if (power > PowerSigns.maxFlingPower) power = PowerSigns.maxFlingPower;
 		double powerd = power / 100.0;
 		double angle = Integer.parseInt(m.group(2));
 		if (Math.abs(angle) > 90) return plugin.debugFail("bad angle: " + Double.toString(angle));
 		boolean launched = false;
 
 		// use int modifiers to fling in the right direction later
 		int x = flingDir.getModX(), z = flingDir.getModZ();
 
 		BlockVector velocity = new Vector(powerd * Math.cos(angle * Math.PI / 180) * x, powerd
 				* Math.sin(angle * Math.PI / 180), powerd * Math.cos(angle * Math.PI / 180) * z).toBlockVector();
 
 		for (Entity ent : PowerSigns.entitiesNearBlock(targArea, 1.0))
 		{
 			ent.setVelocity(velocity);
 
 			launched = true;
 		}
 		if (!launched) return plugin.debugFail("no target");
 		return true;
 	}
 
 }
