 package theresistance.core.state;
 
 import java.util.List;
 
 import theresistance.core.Game;
 import theresistance.core.Player;
 import theresistance.core.Proposal;
 import theresistance.core.Round;
 
 /**
  * The proposal portion of a game
  */
 public class ProposeState extends GameState<ProposeAction>
 {
 	private final Player leader;
 	private List<Player> participants;
 
 	public ProposeState(Player leader)
 	{
 		this.leader = leader;
 	}
 
 	@Override
 	public void act(ProposeAction action)
 	{
 		this.participants = action.getPlayers();
 	}
 
 	@Override
 	public boolean isFinished()
 	{
 		return participants != null;
 	}
	
	public String getLeader()
	{
		return leader.getName();
	}
	
 	@Override
 	public void advance(Game game)
 	{
 		Round round = game.getCurrentRound();
 		Proposal proposal = new Proposal(leader, game.getNumPlayers());
 		proposal.setParticipants(participants);
 		round.addProposal(proposal);
 
 		// check hammer
 		if (round.getProposalIndex() == 5)
 		{
 			round.setParticipants(proposal.getParticipants());
 			game.setState(new MissionState(proposal.getParticipants()));
 		}
 		else
 		{
 			game.setState(new VoteState(game.getPlayers(), proposal));
 		}
 	}
 }
