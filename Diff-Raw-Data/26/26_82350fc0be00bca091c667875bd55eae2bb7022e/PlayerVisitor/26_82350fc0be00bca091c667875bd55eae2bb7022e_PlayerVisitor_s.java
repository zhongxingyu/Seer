 package visitors;
 
 import java.util.ArrayList;
 
 import codeGenerator.RosterSQLGenerator;
 import nba.ContextInfo;
 import nba.Game;
 import nba.Period;
 import nba.PlayRole;
 import nba.Player;
 import nba.Possession;
 import nba.play.*;
 import nba.playType.PlayType;
 import nba.playType.block.Block;
 import nba.playType.ejection.Ejection;
 import nba.playType.foul.*;
 import nba.playType.freeThrow.FreeThrow;
 import nba.playType.jumpBall.JumpBall;
 import nba.playType.rebound.Rebound;
 import nba.playType.review.Review;
 import nba.playType.shot.*;
 import nba.playType.steal.Steal;
 import nba.playType.substitution.Substitution;
 import nba.playType.technical.*;
 import nba.playType.timeout.Timeout;
 import nba.playType.turnover.Turnover;
 import nba.playType.violation.Violation;
 import visitor.Visitor;
 
 public abstract class PlayerVisitor implements Visitor
 {
 
 	protected RosterSQLGenerator rosters;
 	protected Player currentPlayer;
 	protected ContextInfo currentContext;
 	
 	public PlayerVisitor(RosterSQLGenerator rosters)
 	{
 		this.rosters = rosters;
 	}
 	
 	@Override
 	public void visit(ContextInfo contextInfo) {}
 
 	@Override
 	public void visit(Game game) 
 	{
 		for(Period p : game.getPeriods())
 		{
 			p.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(Period period) 
 	{
 		for(Play p : period.getPlays())
 		{
 			p.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(Player player) 
 	{
 		//TODO
 	}
 
 	@Override
 	public void visit(Play play) 
 	{
 		currentContext = play.getContextInfo();
 		currentPlayer = new Player("", -1);
		if (currentContext.getPlayID() == 227)
		{
			int x = 1;
		}
 		play.getPlayType().accept(this);
 	}
 
 	@Override
 	public void visit(PlayerPlay play) 
 	{
 		currentContext = play.getContextInfo();
		if (currentContext.getPlayID() == 227)
		{
			int x = 1;
		}
 		currentPlayer = play.getPlayer();
 		play.getPlayType().accept(this);
 	}
 
 	@Override
 	public void visit(MissedPlay play) 
 	{
 		currentContext = play.getContextInfo();
		if (currentContext.getPlayID() == 227)
		{
			int x = 1;
		}
 		currentPlayer = play.getPlayer();
 		play.getPlayType().accept(this);
 	}
 
 	@Override
 	public void visit(PlayType playType) {}
 
 	@Override
 	public void visit(Block block) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral blocks
 		}
 	}
 
 	@Override
 	public void visit(Ejection ejection) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral ejection
 		}
 	}
 
 	@Override
 	public void visit(Foul foul) 
 	{
 		
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral foul
 		}
		if (currentPlayer.getPlayerID() == 1713)
		{
			int x = 1;
		}
 	}
 
 	@Override
 	public void visit(DoublePersonalFoul foul) 
 	{
 		setPlayer(foul.getPlayer1(), rosters.getActive());
 		setPlayer(foul.getPlayer2(), rosters.getActive());
 	}
 
 	@Override
 	public void visit(FreeThrow freeThrow) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral freeThrow
 		}
 	}
 
 	@Override
 	public void visit(JumpBall jumpBall) 
 	{
 		setPlayer(jumpBall.getPlayer1(), rosters.getActive());
 		setPlayer(jumpBall.getPlayer2(), rosters.getActive());
 		if (jumpBall.getEnding().getTippedTo() != null)
 			setPlayer(jumpBall.getEnding().getTippedTo(), rosters.getActive());
 	}
 
 	@Override
 	public void visit(Rebound rebound) 
 	{
 		if(rebound.isPlayerRebound())
 		{
 			if (currentContext.getPlayRole() == PlayRole.HOME)
 				setPlayer(currentPlayer, rosters.getHomeActive());
 			else if (currentContext.getPlayRole() == PlayRole.AWAY)
 				setPlayer(currentPlayer, rosters.getAwayActive());
 			else
 			{
 				//TODO error, no neutral Rebound
 			}
 		}
 		else
 		{
 				currentPlayer.setPlayerID(-1);
 				currentPlayer.setPlayerName("#TEAM");
 		}
 	}
 
 	@Override
 	public void visit(Review review) {}
 
 	@Override
 	public void visit(Shot shot) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral shot
 		}
 		if (shot.getShotEnding().getAssist() != null)
 		{
 			shot.getShotEnding().getAssist().accept(this);
 		}
 	}
 
 	@Override
 	public void visit(Assist assist) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME && assist.getPlayer() != null)
 			setPlayer(assist.getPlayer(), rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY && assist.getPlayer() != null)
 			setPlayer(assist.getPlayer(), rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral assist
 		}
 	}
 
 	@Override
 	public void visit(Steal steal) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral steal
 		}
 	}
 
 	@Override
 	public void visit(Substitution sub) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 		{
 			setPlayer(sub.getIn(), rosters.getHomeActive());
 			setPlayer(sub.getOut(), rosters.getHomeActive());
 		}
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 		{
 			setPlayer(sub.getIn(), rosters.getAwayActive());
 			setPlayer(sub.getOut(), rosters.getAwayActive());
 		}
 		else
 		{
 			//TODO error, no neutral sub
 		}
 	}
 
 	@Override
 	public void visit(Technical technical) 
 	{
 		ThreeSecTechnical threeSecTec;
 		if (technical.getPredicate() != null)
 		{
 			if (technical.getPredicate().getTechType() != null)
 			{
 				if (technical.getPredicate().getTechType() instanceof ThreeSecTechnical)
 				{
 					threeSecTec = (ThreeSecTechnical)technical.getPredicate().getTechType();
 					if (currentContext.getPlayRole() == PlayRole.HOME)
 						setPlayer(threeSecTec.getPlayer(), rosters.getHomeActive());
 					else if (currentContext.getPlayRole() == PlayRole.AWAY)
 						setPlayer(threeSecTec.getPlayer(), rosters.getAwayActive());
 					else
 					{
 						//TODO error, no neutral technical
 					}
 					return;
 				}
 			}
 		}
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral technical
 		}
 	}
 
 	@Override
 	public void visit(DoubleTechnical technical) 
 	{
 		setPlayer(technical.getPlayer1(), rosters.getActive());
 		setPlayer(technical.getPlayer2(), rosters.getActive());
 	}
 
 	@Override
 	public void visit(TauntingTechnical technical) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral technical
 		}
 	}
 
 	@Override
 	public void visit(Timeout timeout) 
 	{
 		currentPlayer.setPlayerID(-1);
 		currentPlayer.setPlayerName("#TEAM");
 	}
 
 	@Override
 	public void visit(Turnover turnover) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral turnover
 		}
 	}
 
 	@Override
 	public void visit(Violation violation) 
 	{
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 			setPlayer(currentPlayer, rosters.getHomeActive());
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 			setPlayer(currentPlayer, rosters.getAwayActive());
 		else
 		{
 			//TODO error, no neutral violation
 		}
 	}
 
 	@Override
 	public void visit(Possession possession) {}
 	
 	protected abstract void setPlayer(Player player, ArrayList<Player> possiblePlayers);
 	
 }
