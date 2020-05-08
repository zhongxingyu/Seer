 package no.runsafe.survivalchallenge;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.entity.RunsafeEntity;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.survivalchallenge.database.ObjectiveRepository;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class ObjectiveHandler implements IConfigurationChanged
 {
	public ObjectiveHandler(ObjectiveRepository database, ChallengeHandler handler)
 	{
 		this.database = database;
 		this.handler = handler;
 	}
 
 	public void awardPlayerObjective(RunsafePlayer player, IObjective objective)
 	{
 		if (handler.isFinished())
 		{
 			player.sendColouredMessage("&cThe event has already finished!");
 			handler.removePlayer(player);
 			return;
 		}
 
 		Objective objectiveID = objective.getObjective();
 		if (!playerHasCompletedObjective(player, objectiveID))
 		{
 			this.database.flagObjectiveComplete(player, objectiveID); // Persist in the DB.
 
 			String playerName = player.getName();
 			if (!this.data.containsKey(playerName)) // If we don't already have a container for the player..
 				this.data.put(playerName, new ArrayList<Integer>()); // .. make one.
 
 			this.data.get(playerName).add(objectiveID.ordinal()); // Flag the objective as complete.
 			player.sendColouredMessage("&eObjective complete: &f%s&e.", objective.getObjectiveTitle());
 
 			checkProgress(player); // Check the players progress thus far.
 		}
 	}
 
 	public void checkProgress(RunsafePlayer player)
 	{
 		String playerName = player.getName();
 		if (data.containsKey(playerName))
			if (!ObjectiveChecker.hasCompletedAllObjectives(data.get(playerName)))
 				return;
 
 
 		// If we're here, the player has won the challenge.
 		RunsafeWorld world = RunsafeServer.Instance.getWorld(challengeWorld);
 		if (world != null)
 			for (RunsafePlayer worldPlayer : world.getPlayers()) // Get every player in the world.
 				handler.removePlayer(worldPlayer); // Teleport the player away.
 
 		// Give the player the winning achievement.
 		new CustomEvent(player, "achievement.survivalChallengeWinner").Fire();
 
 		// Broadcast to the server.
 		RunsafeServer.Instance.broadcastMessage("&eThe Survival Challenge has been beaten by %s&e!", player.getPrettyName());
 		handler.closeEvent(); // Close the event to prevent further people entering.
 	}
 
 	public boolean entityInEligibleWorld(RunsafeEntity entity)
 	{
 		RunsafeWorld world = entity.getWorld();
 		return world != null && world.getName().equalsIgnoreCase(challengeWorld);
 	}
 
 	public boolean playerHasCompletedObjective(RunsafePlayer player, Objective objective)
 	{
 		String playerName = player.getName();
 		return data.containsKey(playerName) && data.get(playerName).contains(objective.ordinal());
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.data = this.database.getStoredData(); // Load stored data from the database.
 		this.challengeWorld = configuration.getConfigValueAsString("challengeLocation.world"); // Get the world for the event.
 
 		// Let's check to see if we need to close the event already.
 		for (String playerName : data.keySet())
 			checkProgress(RunsafeServer.Instance.getPlayerExact(playerName));
 	}
 
 	private HashMap<String, List<Integer>> data = new HashMap<String, List<Integer>>();
 	private ObjectiveRepository database;
 	private String challengeWorld;
 	private ChallengeHandler handler;
 }
