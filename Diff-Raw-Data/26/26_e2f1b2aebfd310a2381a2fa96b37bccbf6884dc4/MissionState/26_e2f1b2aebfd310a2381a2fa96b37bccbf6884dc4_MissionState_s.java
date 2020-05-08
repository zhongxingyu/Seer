 package theresistance.core.state;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 
 import theresistance.core.Game;
 import theresistance.core.Mission.Result;
 import theresistance.core.Player;
 
 public class MissionState extends GameState<MissionResultAction>
 {
 	Map<Player, Result> results = new TreeMap<>();
 
 	public MissionState(Collection<Player> participants)
 	{
 		for (Player player : participants)
 		{
 			results.put(player, null);
 		}
 	}
 
 	@Override
 	public void act(MissionResultAction action)
 	{
 		results.put(action.getPlayer(), action.getResult());
 	}
 
 	@Override
 	public boolean isFinished()
 	{
 		return !results.values().contains(null);
 	}
 
 	@Override
 	public void advance(Game game)
 	{
 		List<Result> results = new ArrayList<>(this.results.values());
 		List<Result> shuffledResults = new ArrayList<>(results.size());
 
 		Random random = new Random();
 		while (!results.isEmpty())
 		{
 			int next = random.nextInt(results.size());
 			shuffledResults.add(results.remove(next));
 		}
 
 		game.getCurrentRound().setResults(shuffledResults);
 		game.completeRound();
 
 		game.setState(new ProposeState(game.getCurrentLeader()));
 	}
 }
