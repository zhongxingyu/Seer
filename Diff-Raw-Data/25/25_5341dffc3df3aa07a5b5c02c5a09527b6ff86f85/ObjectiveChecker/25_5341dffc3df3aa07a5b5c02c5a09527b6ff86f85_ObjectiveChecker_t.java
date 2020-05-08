 package no.runsafe.survivalchallenge;
 
 import java.util.List;
 
 public class ObjectiveChecker
 {
 	public ObjectiveChecker(IObjective[] objectives)
 	{
		ObjectiveChecker.objectives = objectives;
 	}
 
	public static boolean hasCompletedAllObjectives(List<Integer> data)
 	{
 		for (IObjective objective : objectives) // Check every objective.
 			if (!data.contains((Object) objective.getObjective().ordinal()))
 				return false;
 
 		return true;
 	}
 
	private static IObjective[] objectives;
 }
