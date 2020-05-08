 package no.runsafe.survivalchallenge;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.block.RunsafeBlock;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import org.bukkit.util.Vector;
 
 public class ChallengeHandler implements IConfigurationChanged, IPlayerInteractEvent
 {
 	public ChallengeHandler(IScheduler scheduler)
 	{
 		this.scheduler = scheduler;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		entryPad = configuration.getConfigValueAsLocation("entryPad");
 		finishLocation = configuration.getConfigValueAsLocation("finishLocation");
 		entryLocation = configuration.getConfigValueAsLocation("challengeLocation");
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
		if (entryPad == null)
 			return;
 
 		final RunsafePlayer player = event.getPlayer();
 		if (!finished)
 		{
 			RunsafeBlock block = event.getBlock();
 			if (block != null && block.is(Item.Redstone.PressurePlate.Stone))
 			{
 				RunsafeLocation padLocation = block.getLocation();
 				if (padLocation.distance(entryPad) < 1)
 				{
 					player.setVelocity(new Vector(0, 3, 0)); // Throw the bugger in the air.
 
 					// Three seconds later we should then port the player inside.
 					scheduler.startSyncTask(new Runnable() {
 						@Override
 						public void run() {
 							RunsafeWorld playerWorld = player.getWorld();
 
 							// Create a pretty explosion.
 							if (playerWorld != null)
 								playerWorld.createExplosion(player.getLocation(), 3, false, false);
 
 							player.teleport(entryLocation); // Teleport the player.
 							player.setVelocity(new Vector(0, 0, 0)); // Prevent splat.
 							new CustomEvent(player, "achievement.survivalChallenge").Fire(); // Achievement!
 						}
 					}, 3);
 				}
 			}
 		}
 		else
 		{
 			// The event is over, send a sad message to make them cry.
 			player.sendColouredMessage("&cThe Survival Challenge has ended, sorry!");
 		}
 	}
 
 	public void removePlayer(RunsafePlayer player)
 	{
 		player.teleport(finishLocation);
 	}
 
 	public void closeEvent()
 	{
 		finished = true;
 	}
 
 	public boolean isFinished()
 	{
 		return finished;
 	}
 
 	private boolean finished = false;
 	private RunsafeLocation entryPad;
 	private RunsafeLocation finishLocation;
 	private RunsafeLocation entryLocation;
 	private IScheduler scheduler;
 }
