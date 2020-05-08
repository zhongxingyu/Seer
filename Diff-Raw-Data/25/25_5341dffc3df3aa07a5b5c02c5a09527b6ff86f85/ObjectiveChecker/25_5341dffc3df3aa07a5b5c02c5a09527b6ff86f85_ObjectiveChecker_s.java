 package no.runsafe.survivalchallenge;
 
import no.runsafe.framework.minecraft.player.RunsafePlayer;

 import java.util.List;
 
 public class ObjectiveChecker
 {
 	public ObjectiveChecker(IObjective[] objectives)
 	{
		this.objectives = objectives;
 	}
 
	public boolean hasCompletedAllObjectives(List<Integer> data)
 	{
 		for (IObjective objective : objectives) // Check every objective.
 			if (!data.contains((Object) objective.getObjective().ordinal()))
 				return false;
 
 		return true;
 	}
 
	private IObjective[] objectives;
 }
