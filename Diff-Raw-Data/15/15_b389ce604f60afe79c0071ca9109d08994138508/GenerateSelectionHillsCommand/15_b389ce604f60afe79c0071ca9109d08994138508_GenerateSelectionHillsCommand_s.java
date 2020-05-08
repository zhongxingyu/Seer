 package nl.naxanria.selgen.command;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import nl.naxanria.selgen.GenerateHills;
 import nl.naxanria.selgen.Part;
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 public class GenerateSelectionHillsCommand extends PlayerCommand
 {
 
 	public GenerateSelectionHillsCommand(IOutput console, GenerateHills generateHills, IScheduler scheduler)
 	{
 		super("hills", "generate hills", "selgen.generate.hills");
 		worldEdit = RunsafeServer.Instance.getPlugin("WorldEdit");
 		this.console = console;
 		this.generateHills = generateHills;
 		this.scheduler = scheduler;
 
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, Map<String, String> parameters)
 	{
 
 		if(currentUser != null)
 			return "Please wait a moment. There is another generation going on at the moment";
 
 
 		Selection selection =  worldEdit.getSelection(executor.getRawPlayer());
 		if(selection == null)
 			return "Make a selection first!";
 
 
 		currentUser = executor;
 		parts = new ArrayList<Part>();
 
 
 		int startX = selection.getMinimumPoint().getBlockX();
 		int endX = selection.getMaximumPoint().getBlockX();
 		int startZ = selection.getMinimumPoint().getBlockZ();
 		int endZ = selection.getMaximumPoint().getBlockZ();
 
 		int width = Math.abs(startX - endX);
 		int depth = Math.abs(startZ - endZ);
 
 		console.write(String.format("[%d - %d]" , width, depth));
 
 
 		int x = 0;
 
 
 		while (x < width)
 		{
 			int z = 0;
 			int fromX = startX + x ;
 			int toX = startX + x + stepSize;
 			if (x + stepSize > width)
 				toX = startX + width;
 
 			while (z < depth)
 			{
 				int fromZ = startZ + z;
 				int toZ = startZ + z + stepSize;
 				if (z + stepSize > depth)
 					toZ = startZ + depth;
 
 				parts.add(
						new Part(generateHills, fromX, toX, fromZ, toZ, executor.getWorld().getRaw().getSeed(), 0.5, 0.3, 1 / 64, 64.0, 64.0, true, executor.getWorld())
 				);
 
 				z += stepSize;
 			}
 
 			x += stepSize;
 		}
 
 		scheduler.startSyncTask(new Runnable() {
 			@Override
 			public void run() {
 				next();
 
 			}
 		}, 10l);
 
 		console.write("Total parts: " + parts.size());
 		currentUser.sendColouredMessage("&f[&3SelGen&f] Total parts: &2" + parts.size());
 		return "&f[&3SelGen&f] &2Generation started";
 	}
 
 	private void next()
 	{
 		if (index < parts.size())
 		{
 			parts.get(index).generate();
 			index++;
 
 			double percent = index / (double) parts.size() * 100;
 			currentUser.sendColouredMessage("&f[&3SelGen&f] &2" + Math.round(percent) + "%");
 			console.write(Math.round(percent) + "%");
 			scheduler.startSyncTask(new Runnable() {
 				@Override
 				public void run() {
 					next();
 
 				}
 			}, 5l);
 		}
 		else
 		{
 			currentUser.sendColouredMessage("&f[&3SelGen&f] &aDone!");
 			currentUser = null;
 			index = 0;
 		}
 	}
 
 	private WorldEditPlugin worldEdit;
 	private final IOutput console;
 	private final GenerateHills generateHills;
 
 	private ArrayList<Part> parts;
 	private int index = 0;
 	private RunsafePlayer currentUser = null;
 
 	private IScheduler scheduler;
 
 	private int stepSize = 16;
 
 
 
 }
