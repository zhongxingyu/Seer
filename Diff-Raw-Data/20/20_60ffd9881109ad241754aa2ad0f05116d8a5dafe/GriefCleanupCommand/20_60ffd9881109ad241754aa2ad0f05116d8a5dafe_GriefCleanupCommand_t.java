 package no.runsafe.creativetoolbox.command;
 
 import com.google.common.collect.Lists;
 import no.runsafe.creativetoolbox.PlotCalculator;
 import no.runsafe.creativetoolbox.PlotFilter;
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.block.RunsafeBlock;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.worldeditbridge.WorldEditInterface;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.List;
 
 public class GriefCleanupCommand extends PlayerCommand
 {
	public GriefCleanupCommand(WorldGuardInterface worldGuard, WorldEditInterface worldEdit, PlotCalculator plotCalculator, PlotFilter filter)
 	{
 		super("griefcleanup", "Cleans up griefed plots.", "runsafe.creative.degrief", "what");
 		this.worldEdit = worldEdit;
 		this.worldGuard = worldGuard;
 		this.plotCalculator = plotCalculator;
 		this.filter = filter;
 	}
 
 	@Override
 	public List<String> getParameterOptions(String parameter)
 	{
 		if (parameter.equals("what"))
 			return Lists.newArrayList("road", "lava", "water", "cobblestone", "all");
 		return null;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> params)
 	{
 		Rectangle2D area = getArea(executor.getLocation());
 		String what = params.get("what");
 		if (what.equals("road"))
 			return regeneratePadding(executor, area);
 
 		if (what.equals("lava"))
 			return cleanup(executor, area, 10, 11);
 
 		if (what.equals("water"))
 			return cleanup(executor, area, 8, 9);
 
 		if (what.equals("cobblestone"))
 			return cleanup(executor, area, 4);
 
 		if (what.equals("all"))
 			return cleanup(executor, area, 4, 8, 9, 10, 11);
 
 		return String.format("Unsupported argument \"%s\"", what);
 	}
 
 	private Rectangle2D getArea(RunsafeLocation location)
 	{
 		List<String> candidate = filter.apply(worldGuard.getRegionsAtLocation(location));
 		if (candidate != null && candidate.size() == 1)
 			return plotCalculator.pad(worldGuard.getRectangle(location.getWorld(), candidate.get(0)));
 		else
 			return plotCalculator.getPlotArea(location, true);
 	}
 
 	private String cleanup(RunsafePlayer player, Rectangle2D area, Integer... remove)
 	{
 		if (remove.length == 0)
 			return "Nothing to clean";
 		List<Integer> removeIds = Lists.newArrayList(remove);
 		RunsafeLocation max = plotCalculator.getMaxPosition(player.getWorld(), area);
 		RunsafeLocation min = plotCalculator.getMinPosition(player.getWorld(), area);
 		RunsafeWorld world = player.getWorld();
 		int counter = 0;
 		for (int x = min.getBlockX(); x <= max.getBlockX(); ++x)
 			for (int y = max.getBlockY(); y >= min.getBlockY(); --y)
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z)
 				{
 					RunsafeBlock block = world.getBlockAt(x, y, z);
 					if (removeIds.contains(Integer.valueOf(block.getTypeId())))
 					{
 						block.setTypeId(0);
 						counter++;
 					}
 				}
 		return String.format("Removed %d blocks.", counter);
 	}
 
 	private String regeneratePadding(RunsafePlayer player, Rectangle2D plotArea)
 	{
 		List<Rectangle2D> padding = plotCalculator.getPaddingSelection(plotArea);
 		for (Rectangle2D area : padding)
 		{
 			worldEdit.regenerate(
 				player,
 				plotCalculator.getMinPosition(player.getWorld(), area),
 				plotCalculator.getMaxPosition(player.getWorld(), area),
 				false
 			);
 		}
 		return "Regenerated roads.";
 	}
 
 	private final WorldGuardInterface worldGuard;
 	private final WorldEditInterface worldEdit;
 	private final PlotCalculator plotCalculator;
 	private final PlotFilter filter;
 }
