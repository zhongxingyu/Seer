 package visitors;
 
 import java.util.ListIterator;
 
 import playChecker.PlayChecker;
 import playChecker.StealChecker;
 
 import codeGenerator.RosterSQLGenerator;
 import nba.*;
 import nba.play.*;
 import nba.playType.*;
 import nba.playType.block.*;
 import nba.playType.ejection.*;
 import nba.playType.foul.*;
 import nba.playType.freeThrow.*;
 import nba.playType.jumpBall.*;
 import nba.playType.rebound.*;
 import nba.playType.review.*;
 import nba.playType.shot.*;
 import nba.playType.steal.*;
 import nba.playType.substitution.*;
 import nba.playType.technical.*;
 import nba.playType.timeout.*;
 import nba.playType.turnover.*;
 import nba.playType.violation.*;
 import visitor.Visitor;
 
 public class PossessionVisitor implements Visitor {
 
 	
 	private RosterSQLGenerator rosters;
 	private Possession currentPossession;
 	private Play currentPlay;
 	
 	public PossessionVisitor(RosterSQLGenerator rosters)
 	{
 		this.rosters = rosters;
 	}
 	
 	private int getCurrentPlayTeam()
 	{
 		if (currentPlay.getContextInfo().getPlayRole().equals(PlayRole.HOME))
 			return rosters.getHomeID();
 		else if (currentPlay.getContextInfo().getPlayRole().equals(PlayRole.AWAY))
 			return rosters.getAwayID();
 		else
 			return -1;
 	}
 	
 	private boolean PossessionContinuation()
 	{
 		boolean defensiveFoul, timeout, sub, freeThrow, defTech;
 		
 		defensiveFoul =  ((currentPlay.getPlayType() instanceof Foul) 
 				&& (currentPossession.getDefenseID() == getCurrentPlayTeam() || getCurrentPlayTeam() == -1));
 		if (defensiveFoul)
 		{
 			Foul foul = (Foul)currentPlay.getPlayType();
 			if (foul.getFoulType() instanceof OffensiveFoulType || foul.getFoulType() instanceof OffensiveChargeFoulType)
 			{
 				defensiveFoul = false;
 			}
 		}
 		
 		timeout = currentPlay.getPlayType() instanceof Timeout;
 		sub = currentPlay.getPlayType() instanceof Substitution;
 		freeThrow = ((currentPlay.getPlayType() instanceof FreeThrow) &&
 				currentPossession.getOffenseID() == getCurrentPlayTeam());
 		defTech = ((currentPlay.getPlayType() instanceof Technical) 
 				&& (currentPossession.getDefenseID() == getCurrentPlayTeam() || getCurrentPlayTeam() == -1));
 		
 		return defensiveFoul || timeout || sub || freeThrow || defTech;
 	}
 	
 	private void assignTeamRoles()
 	{
 		if (currentPlay.getPlayType() instanceof JumpBall)
 		{
 			JumpBall jBall = (JumpBall)currentPlay.getPlayType();
 			Player player = jBall.getPossessionPlayer();
 			
 			if(player == null)
 			{
 				return;
 			}
 			
 			if (rosters.searchHomePlayers(player))
 			{
 				currentPossession.setTeamRoles(rosters.getHomeID(), 
 						rosters.getAwayID());
 			}
 			else if (rosters.searchAwayPlayers(player))
 			{
 				currentPossession.setTeamRoles(rosters.getAwayID(), 
 						rosters.getHomeID());
 			}
 		}
 		else
 		{
 			if (getCurrentPlayTeam() == rosters.getHomeID())
 			{
 				currentPossession.setTeamRoles(rosters.getHomeID(), 
 						rosters.getAwayID());
 			}
 			else if (getCurrentPlayTeam() == rosters.getAwayID())
 			{
 				currentPossession.setTeamRoles(rosters.getAwayID(), 
 						rosters.getHomeID());
 			}
 		}
 	}
 	
 	@Override
 	public void visit(ContextInfo contextInfo) {}
 
 	@Override
 	public void visit(Game game) 
 	{
 		for (Period p : game.getPeriods())
 		{
 			p.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(Period period) 
 	{
 		currentPossession = new Possession();
 		boolean madeShot = false;
 		boolean missedFirstFT = false;
 		ListIterator<Play> playIterator = period.getPlays().listIterator();
 		
 		while (playIterator.hasNext())
 		{
 			Play p = playIterator.next();
 			currentPlay = p;
 			
 			if (!currentPossession.teamsSet() && p.identifiesOffense())
 			{
 				assignTeamRoles();
 			}
 			
 			if (!madeShot)
 			{
 				if (p.getPlayType().terminatesPossession())
 				{
 					if ((p.getPlayType() instanceof Rebound) && missedFirstFT)
 					{
 						currentPossession.addPlay(p);
 						missedFirstFT = false;
 					}
 					else
 					{
 						currentPossession.addPlay(p);
 						madeShot = false;
 						missedFirstFT = false;
 						
 						if (p.getPlayType() instanceof Turnover)
 						{
 							if (!checkForPlay(new StealChecker(), p.getPlayID(), currentPossession))
 							{
 								if(playIterator.hasNext())
 								{
 									Play tempPlayer = playIterator.next();
 									if(checkSteal(tempPlayer, p.getPlayID(), new StealChecker()))
 									{
 										currentPossession.addPlay(tempPlayer);
 									}
 									else
 									{
 										playIterator.previous();
 									}
 								}
 							}
 						}
 						
 						period.addPossession(currentPossession);
 						currentPossession = new Possession();
 					}
 				}
 				else
 				{
 					if((p.getPlayType() instanceof Shot))
 					{
 						madeShot = !(p instanceof MissedPlay);
 					}
 					if((p.getPlayType() instanceof FreeThrow))
 					{
 						FreeThrow ft = (FreeThrow)p.getPlayType();
 						if (!ft.lastFreeThrow())
 						{
 							missedFirstFT = (p instanceof MissedPlay);
 						}
 					}
 					currentPossession.addPlay(p);
 				}
 			}
 			else
 			{
 				if (PossessionContinuation())
 				{
 					if((p.getPlayType() instanceof FreeThrow))
 					{
 						FreeThrow ft = (FreeThrow)p.getPlayType();
 						if (!ft.lastFreeThrow())
 						{
 							missedFirstFT = (p instanceof MissedPlay);
 						}
 					}
 					currentPossession.addPlay(p);
 				}
 				else
 				{
 					if ((p.getPlayType() instanceof Rebound) && missedFirstFT)
 					{
 						currentPossession.addPlay(p);
 						missedFirstFT = false;
 					}
 					else if (p.getPlayType().terminatesPossession())
 					{
 						period.addPossession(currentPossession);
 						currentPossession = new Possession();
 						madeShot = false;
 						missedFirstFT = false;
 						currentPossession.addPlay(p);
 						assignTeamRoles();
 						if (p.getPlayType() instanceof Turnover)
 						{
 							if (!checkForPlay(new StealChecker(), p.getPlayID(), currentPossession))
 							{
 								if(playIterator.hasNext())
 								{
 									Play tempPlayer = playIterator.next();
 									if(checkSteal(tempPlayer, p.getPlayID(), new StealChecker()))
 									{
 										currentPossession.addPlay(tempPlayer);
 									}
 									else
 									{
 										playIterator.previous();
 									}
 								}
 							}
 						}
 						period.addPossession(currentPossession);
 						currentPossession = new Possession();
 						
 					}
 					else
 					{
 						period.addPossession(currentPossession);
 						currentPossession = new Possession();
 						madeShot = false;
 						missedFirstFT = false;
 						currentPossession.addPlay(p);
 						if((p.getPlayType() instanceof Shot))
 						{
 							madeShot = !(p instanceof MissedPlay);
 						}
 						if (p.identifiesOffense())
 						{
 							assignTeamRoles();
 						}
 					}
 				}
 			}
 		}
 		if(currentPossession.getPossessionPlays().size() > 0)
 		{
 			period.addPossession(currentPossession);
 		}
 	}
 
 	@Override
 	public void visit(Player player) {}
 
 	@Override
 	public void visit(Play play) {}
 
 	@Override
 	public void visit(PlayerPlay play) {}
 
 	@Override
 	public void visit(MissedPlay play) {}
 
 	@Override
 	public void visit(PlayType playType) {}
 
 	@Override
 	public void visit(Block block) {}
 
 	@Override
 	public void visit(Ejection ejection) {}
 
 	@Override
 	public void visit(Foul foul) {}
 
 	@Override
 	public void visit(FreeThrow freeThrow) {}
 
 	@Override
 	public void visit(JumpBall jumpBall) {}
 
 	@Override
 	public void visit(Rebound rebound) {}
 
 	@Override
 	public void visit(Review review) {}
 
 	@Override
 	public void visit(Shot shot) {}
 
 	@Override
 	public void visit(Assist assist) {}
 
 	@Override
 	public void visit(Steal steal) {}
 
 	@Override
 	public void visit(Substitution sub) {}
 
 	@Override
 	public void visit(Technical technical) {}
 
 	@Override
 	public void visit(DoubleTechnical technical) {}
 
 	@Override
 	public void visit(TauntingTechnical technical) {}
 
 	@Override
 	public void visit(Timeout timeout) {}
 
 	@Override
 	public void visit(Turnover turnover) {}
 
 	@Override
 	public void visit(Violation violation) {}
 
 	@Override
 	public void visit(DoublePersonalFoul foul) {}
 
 	@Override
 	public void visit(Possession possession) {}
 	
 	private boolean checkForPlay(PlayChecker checker, int playID, Possession poss)
 	{
 		for (Play p : poss.getPossessionPlays())
 		{
 			if ((p.getPlayID() == playID) && checker.playTypeMatches(p))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	private boolean checkSteal(Play play, int playID, PlayChecker checker)
 	{
 		if (play.getPlayID() == playID && checker.playTypeMatches(play))
 			return true;
 		
 		return false;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 
 }
