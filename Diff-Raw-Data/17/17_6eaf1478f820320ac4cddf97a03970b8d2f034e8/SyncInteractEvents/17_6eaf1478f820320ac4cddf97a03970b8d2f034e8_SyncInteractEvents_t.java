 package no.runsafe.creativetoolbox.event;
 
 import no.runsafe.creativetoolbox.PlotCalculator;
 import no.runsafe.creativetoolbox.PlotManager;
import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.worldeditbridge.WorldEditInterface;
 import no.runsafe.worldgenerator.PlotChunkGenerator;
 
 import java.awt.geom.Rectangle2D;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class SyncInteractEvents implements IPlayerRightClickBlock
 {
	public SyncInteractEvents(PlotChunkGenerator plotGenerator, PlotCalculator calculator, WorldEditInterface worldEdit, PlotManager manager, IOutput output)
 	{
 		this.plotGenerator = plotGenerator;
 		this.calculator = calculator;
 		this.worldEdit = worldEdit;
 		this.manager = manager;
		this.output = output;
 	}
 
 	public void startRegeneration(RunsafePlayer executor, Rectangle2D area, PlotChunkGenerator.Mode mode)
 	{
 		regenerations.put(executor.getName(), area);
 
 		if (mode != null)
 			generator.put(executor.getName(), mode);
 		else if (generator.containsKey(executor.getName()))
 			generator.remove(executor.getName());
 	}
 
 	public void startDeletion(RunsafePlayer executor, Map<String, Rectangle2D> regions)
 	{
 		deletions.put(executor.getName(), regions);
 	}
 
 	@Override
 	public boolean OnPlayerRightClick(RunsafePlayer player, RunsafeMeta itemInHand, IBlock block)
 	{
 		return
 			deletions.isEmpty() && regenerations.isEmpty()
 				|| executeRegenerations(player, block.getLocation()) && executeDeletion(player, block.getLocation());
 	}
 
 	private boolean executeRegenerations(RunsafePlayer player, RunsafeLocation location)
 	{
 		if (regenerations.containsKey(player.getName()))
 		{
 			boolean changeMode = generator.containsKey(player.getName())
 				&& generator.get(player.getName()) != PlotChunkGenerator.Mode.NORMAL;
 			try
 			{
 				String playerName = player.getName();
 				Rectangle2D area = regenerations.get(playerName);
 				if (!area.contains(location.getX(), location.getZ()))
 				{
 					regenerations.remove(playerName);
 					if (changeMode)
 						generator.remove(playerName);
 					return true;
 				}
 				if (changeMode)
 				{
 					PlotChunkGenerator.Mode mode = generator.get(playerName);
 					plotGenerator.setMode(mode);
 				}
 
 				RunsafeWorld playerWorld = player.getWorld();
 				RunsafeLocation minPos = calculator.getMinPosition(playerWorld, area);
 				RunsafeLocation maxPos = calculator.getMaxPosition(playerWorld, area);
 				player.sendMessage(
 					worldEdit.regenerate(player, minPos, maxPos, false)
 						? "Plot regenerated."
 						: "Could not regenerate plot."
 				);
 				RunsafeServer.Instance.getLogger().info(String.format(
 					"%s regenerated area: %s, %s", playerName, minPos, maxPos
 				));
 				return false;
 			}
 			finally
 			{
 				if (changeMode)
 				{
 					plotGenerator.setMode(PlotChunkGenerator.Mode.NORMAL);
 					generator.remove(player.getName());
 				}
 				regenerations.remove(player.getName());
 			}
 		}
 		return true;
 	}
 
 	private boolean executeDeletion(RunsafePlayer player, RunsafeLocation location)
 	{
 		boolean nothing = true;
 		if (deletions.containsKey(player.getName()))
 		{
 			StringBuilder results = new StringBuilder();
 			Map<String, Rectangle2D> process = deletions.get(player.getName());
 			deletions.remove(player.getName());
 			for (String region : process.keySet())
 			{
 				Rectangle2D area = process.get(region);
 				if (area.contains(location.getX(), location.getZ()))
 				{
 					nothing = false;
 					RunsafeLocation minPos = calculator.getMinPosition(player.getWorld(), area);
 					RunsafeLocation maxPos = calculator.getMaxPosition(player.getWorld(), area);
 					manager.delete(player, region);
 					worldEdit.regenerate(player, minPos, maxPos, false);
 					results.append(String.format("Deleted plot '%s'.", region));
					output.logInformation(String.format("%s deleted plot %s", player.getName(), region));
 				}
 			}
 			if (!nothing)
 				player.sendColouredMessage(results.toString());
 		}
 		return nothing;
 	}
 
 	private final ConcurrentHashMap<String, Map<String, Rectangle2D>> deletions = new ConcurrentHashMap<String, Map<String, Rectangle2D>>();
 	private final ConcurrentHashMap<String, Rectangle2D> regenerations = new ConcurrentHashMap<String, Rectangle2D>();
 	private final ConcurrentHashMap<String, PlotChunkGenerator.Mode> generator = new ConcurrentHashMap<String, PlotChunkGenerator.Mode>();
 	private final PlotChunkGenerator plotGenerator;
 	private final PlotCalculator calculator;
 	private final WorldEditInterface worldEdit;
 	private final PlotManager manager;
	private final IOutput output;
 }
