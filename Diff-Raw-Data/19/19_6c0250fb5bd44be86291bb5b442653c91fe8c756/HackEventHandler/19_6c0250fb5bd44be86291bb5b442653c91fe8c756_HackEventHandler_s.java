 package sfs2x.extensions.projectsasha.game;
 import java.util.ArrayList;
 import java.util.List;
 
 import sfs2x.extensions.projectsasha.game.entities.GameWorld;
 import sfs2x.extensions.projectsasha.game.entities.Player;
 import sfs2x.extensions.projectsasha.game.entities.Quests;
 import sfs2x.extensions.projectsasha.game.entities.gateways.*;
 import sfs2x.extensions.projectsasha.game.entities.software.*;
 import sfs2x.extensions.projectsasha.game.ia.AIThread;
 import sfs2x.extensions.projectsasha.game.objectives.Objective;
 import sfs2x.extensions.projectsasha.game.utils.RoomHelper;
 
 
 import com.smartfoxserver.v2.entities.User;
 
 import com.smartfoxserver.v2.entities.data.ISFSObject;
 import com.smartfoxserver.v2.entities.data.SFSArray;
 import com.smartfoxserver.v2.entities.data.SFSObject;
 import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
 
 public class HackEventHandler extends BaseClientRequestHandler
 {
 	public void handleClientRequest(User sender, ISFSObject params)
 	{
 		boolean neutralize = false;
 		boolean success = false;
 		boolean isVictoryReached = false;
 		boolean isQuestComplete = false;
 		int attackRelevance;
 		List<Gateway> hackingPath = null;
 		GameWorld world = RoomHelper.getWorld(this);
 		Player p = RoomHelper.getPlayer(this, sender.getName());
 		
 		
 		Gateway from = world.gateways.get(params.getUtfString("gatewayFrom"));
 		Gateway to = world.gateways.get(params.getUtfString("gatewayTo"));
 		neutralize = params.getBool("neutralize");
 		
 		if(from.getOwner()!=null && from.getOwner().canHack() && 	//if i'm owner of starting node and i can hack
 			to.getDisabled() < System.currentTimeMillis() && 		//starting node is not disabled
 			from.getDisabled() < System.currentTimeMillis() 		//ending node i disabled
 		) 
 		{
 			if(to.getOwner()!=null && to.getOwner()!=p) // if there is an owner in attacked node and it's not me
 			{
 				attackRelevance = this.getAttackRelevance(from, to);
 				
 				if(from.getInstalledSoftware(GameConsts.PROXY) == null)
 				{
 					trace("There is no proxy");
 					for(Gateway g : from.getNeighboors())
 					{
 						if(g == to)
 						{
 							trace("I found a path");
 							hackingPath = new ArrayList<Gateway>();
 							hackingPath.add(from);
 							hackingPath.add(to);
 							break;
 						}
 					}							
 				}
 				else
 				{
 					trace("There is a proxy of level "+((Proxy)from.getInstalledSoftware(GameConsts.PROXY)).getAttackLevel());
 					hackingPath = from.tracePath(to, attackRelevance);					
 				}
 				
 				if(hackingPath == null)
 				{
 					sendError("NOPATH", sender);
 				}
 				
 				
 				if(neutralize) // if neutralize
 				{
 					trace("Neutralization request from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": PROCESSING");
 					success = this.neutralize(world, from, to);
 					trace("Neutralization request from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": " +  (success?"SUCCESS":"FAIL"));
 					
 					if(success)
 						 to.setDisabled(System.currentTimeMillis()+GameConsts.DISABLED_TIME*1000);
 				}
 				else	// if conquer
 				{	
 					trace("Hack request from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": PROCESSING");
 					success = this.hack(world, from, to, GameConsts.CONQUER_TIME_TRESHOLD);
 					trace("Hack request from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": " +  (success?"SUCCESS":"FAIL"));
 				}
 				from.setTrace(hackingPath, attackRelevance);
 			}
 			else // if there is an not an owner in attacked node
 			{
 				
 				attackRelevance = this.getAttackRelevance(from, to);
 				
 				if(from.getInstalledSoftware(GameConsts.PROXY) == null)
 				{
 					trace("There is no proxy");
 					for(Gateway g : from.getNeighboors())
 					{
 						if(g == to)
 						{
 							trace("I found a path");
 							hackingPath = new ArrayList<Gateway>();
 							break;
 						}
 					}							
 				}
 				else
 				{
 					hackingPath = from.tracePath(to, attackRelevance);
 					trace("There is a proxy of level "+((Proxy)from.getInstalledSoftware(GameConsts.PROXY)).getRange());
 				}
 				
 				if(hackingPath == null)
 				{
 					sendError("NOPATH", sender);
 					return;
 				}
 				
 				
 				sendPathInfo(hackingPath, sender); // send path info to the client
 				
 				success = this.hack(world, from, to);
 				trace("Hack request from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": " +  (success?"SUCCESS":"FAIL"));
 				from.setTrace(hackingPath, attackRelevance);
 				
 			}
 		}
 		else //if i'm not owner of starting node or i can't hack
 		{
 			sendError("HACKDISABLED", sender);
 			return;
 		}
 		
 		isVictoryReached = checkVictoryConditions(p);
 		ISFSObject reback = SFSObject.newInstance();
 		reback.putBool("success", success);
 		reback.putBool("victoryReached", isVictoryReached);
 		isQuestComplete = checkQuestComplete(p,to);
 		reback.putBool("questComplete", isQuestComplete);
 		send("hack", reback, sender);
 		
 	}
 	
 	public boolean hack(GameWorld world, Gateway from, Gateway to)
 	{
 		return hack(world, from, to, 0);
 	}
 	
 	public boolean hack(GameWorld world, Gateway from, Gateway to, int extraTime)
 	{
 		boolean ret = false;
 		if(changeStatus(from,to))
 		{
 			long startTime, endTime;
 			int difference = this.powerDifference(from, to);
 		
 			Software[] attackerSw = from.getInstalledSoftwares();
 			Software[] defenderSw = to.getInstalledSoftwares();
 		
 			/*
 			 * FIXME: togliere i fix per Sam
 			 */
 			for(Software sw: attackerSw)
 				if(sw!=null)
 					/*switch(sw.getType())
 					{
 					case GameConsts.DICTIONARY:
 						sw.runTriggeredAction(from, to);
 						break;
 					default:
 						break;
 				}*/
 					if(sw.getType() == GameConsts.DICTIONARY)
 						sw.runTriggeredAction(from, to);
 			for(Software sw: defenderSw)
 				if(sw!=null)
 				{
 					if(sw.getType() == GameConsts.IDS)
 						if(to.getOwner()!=null)
 							sw.runTriggeredAction(from, to);
 					if(sw.getType() == GameConsts.VIRUS)
 							sw.runTriggeredAction(from, to);
 					if(sw.getType() == GameConsts.DEEPTHROAT)
 							((DeepThroat)sw).runTriggeredAction(from, to, ((AIThread)world.ai).police);
 				}
 				/*switch(sw.getType())
 				{
 					case GameConsts.IDS:
 						if(to.getOwner()!=null)
 							sw.runTriggeredAction(from, to);
 						break;
 					case GameConsts.VIRUS:
 						sw.runTriggeredAction(from, to);
 						break;
 					case GameConsts.DEEPTHROAT:
 						sw.runTriggeredAction(from, to);
 					default:
 						break;
 				}*/
 			Player currentOwner = from.getOwner();
 			if(difference > 0)
 			{
 				int waitTime = this.hackTime(world, from, to) + extraTime;
 				startTime = System.currentTimeMillis();
 				endTime = startTime+(waitTime*1000);
 				from.getOwner().setCanHack(false);//disable hack for the following seconds
 				while(System.currentTimeMillis() != endTime)
 				{
 					if(currentOwner == null)
 						return ret;
 				/*BUSY WAIT*/
 				//currentTick = System.currentTimeMillis()-starTime;
 				//	if(currentTick%1000==0)
 				//		send a countdown to the player for remaining hack time??
 				}
 
				from.getOwner().setCanHack(true);
 				to.setOwner(from.getOwner());
 				freeStatus(from,to);
 				ret = true;
 			}
 			else
 			{
 				startTime = System.currentTimeMillis();
 				endTime = startTime+(GameConsts.FAILTIME*1000);
 				from.getOwner().setCanHack(false); //disable hack for the following seconds
 				while(System.currentTimeMillis() != endTime)
 				{
 					if(currentOwner == null)
 						return ret;
 				/*BUSY WAIT*/
 				//currentTick = System.currentTimeMillis()-starTime;
 				//	if(currentTick%1000==0)
 				//		send a countdown to the player??
 				}
 			
 				from.getOwner().setCanHack(true);
 				ret = false;
 				freeStatus(from,to);
 				}
 			}
 			return ret;
 		}
 	
 	public boolean neutralize(GameWorld world, Gateway from, Gateway to)
 	{
 		boolean ret = false;
 		long startTime, endTime;
 		int difference = this.powerDifference(from, to);
 		
 		Software[] attackerSw = from.getInstalledSoftwares();
 		Software[] defenderSw = to.getInstalledSoftwares();
 		/*
 		 * FIXME: togliere i fix per Sam
 		 */
 		for(Software sw: attackerSw)
 			if(sw!=null)
 				if(sw.getType() == GameConsts.DICTIONARY)
 					sw.runTriggeredAction(from, to);
 		/*
 				switch(sw.getType())
 				{
 					case GameConsts.DICTIONARY:
 						sw.runTriggeredAction(from, to);
 						break;
 					default:
 						break;
 				}
 		*/
 		for(Software sw: defenderSw)
 			if(sw!=null)
 			{
 				if(sw.getType() == GameConsts.IDS)
 					if(to.getOwner()!=null)
 						sw.runTriggeredAction(from, to);
 				if(sw.getType() == GameConsts.VIRUS)
 						sw.runTriggeredAction(from, to);
 				if(sw.getType() == GameConsts.DEEPTHROAT)
 						sw.runTriggeredAction(from, to);
 			}/*
 				switch(sw.getType())
 				{
 					case GameConsts.IDS:
 						if(to.getOwner()!=null)
 							sw.runTriggeredAction(from, to);
 						break;
 					case GameConsts.VIRUS:
 						sw.runTriggeredAction(from, to);
 						break;
 					case GameConsts.DEEPTHROAT:
 						sw.runTriggeredAction(from, to);
 					default:
 						break;
 				}
 */
 		Player currentOwner = from.getOwner();
 		if(difference > 0)
 		{
 			int waitTime = this.hackTime(world, from, to);
 			startTime = System.currentTimeMillis();
 			endTime = startTime+(waitTime*1000);
 			from.getOwner().setCanHack(false);//disable hack for the following seconds
 
 			while(System.currentTimeMillis() != endTime)
 			{
 				if(currentOwner == null)
 					return ret;
 				/*BUSY WAIT*/
 				//currentTick = System.currentTimeMillis()-starTime;
 				//	if(currentTick%1000==0)
 				//		send a countdown to the player for remaining hack time??
 			}
 
 			from.getOwner().setCanHack(true);
 			
 			ret = true;
 		}
 		else
 		{
 			startTime = System.currentTimeMillis();
 			endTime = startTime+(GameConsts.FAILTIME*1000);
 			from.getOwner().setCanHack(false); //disable hack for the following seconds
 			
 			while(System.currentTimeMillis() != endTime)
 			{
 				/*BUSY WAIT*/
 				//currentTick = System.currentTimeMillis()-starTime;
 				//	if(currentTick%1000==0)
 				//		send a countdown to the player??
 			}
 			
 			from.getOwner().setCanHack(true);
 			ret = false;
 		}
 		return ret;
 	}
 	
 	public int powerDifference(Gateway from, Gateway to)
 	{
 		return from.getAttackLevel()-to.getDefenceLevel();
 	}
 	
 	public int getAttackRelevance(Gateway from, Gateway to)
 	{
 		float result = ((from.getAttackLevel() + to.getDefenceLevel()) / 200f) *10;
 		Math.ceil(result);
 		return (int)result;
 	}
 	
 	public int hackTime(GameWorld world, Gateway from, Gateway to)
 	{
 		int bonus, govBonus = 0;
 		if(to.getOwner()!=null)
 			govBonus = GameConsts.GOV_BONUS_MULTIPLIER * to.getOwner().getConqueredGateway(world, GameConsts.GOV_GATEWAY);
 		int diff = this.powerDifference(from, to)+
 				GameConsts.MIL_BONUS_MULTIPLIER * from.getOwner().getConqueredGateway(world, GameConsts.MIL_GATEWAY)-
 				govBonus;
 		
 		int[] timeToLeave = {
 						15,15,15,15,15,15,15,15,15, 	// 1-9
 						25,25,25,25,25,25,25,25,25,25,	//10-19
 						25,25,25,25,25,25,25,25,25,25,	//20-29
 						35,35,35,35,35,35,35,35,35,35,	//30-39
 						35,35,35,35,35,35,35,35,35,35,	//40-49
 						45,45,45,45,45,45,45,45,45,45,	//50-59
 						45,45,45,45,45,45,45,45,45,45,	//60-69
 						55,55,55,55,55,55,55,55,55,55,	//70-79
 						55,55,55,55,55,					//80-84
 						65,65,65,65,65,65,65,65,65,65	//85-94
 						};
 		
 		bonus = GameConsts.SCI_BONUS_MULTIPLIER*from.getOwner().getConqueredGateway(world, GameConsts.SCI_GATEWAY);
 		
 		/* FIXME: rimuovere i 10 secondi */
 		return 10;//120 - timeToLeave[diff-1] - bonus;
 	}
 	
 	public boolean checkVictoryConditions(Player p)
 	{
 		Objective[] objectives = RoomHelper.getObjectives(this);
 		for(Objective o : objectives)
 		{
 			if(o.isObjectiveReached(o, p))
 				return true;
 		}
 		return false;
 	}
 	
 	public boolean checkQuestComplete(Player p, Gateway g)
 	{
 		for(int i = 0; i < p.getQuest().size();i++)
 		{
 			if(p.questComplete(p.getQuest().get(i), g))
 			{
 				if(p.getQuest().get(i).getRewardMoney()>0)
 					p.addMoney(p.getQuest().get(i).getRewardMoney());
 				else
 				{
 					p.getQuest().get(i).getRewardItem().setLock(false);
 					p.addInventory(p.getQuest().get(i).getRewardItem());
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	synchronized public static boolean changeStatus(Gateway from, Gateway to)
 	{
 		if(!(from.getBusy() || to.getBusy()))
 		{
 			from.setBusy(true);
 			to.setBusy(true);
 			return true;
 		}
 		return false;
 	}
 	
 	synchronized public static void freeStatus(Gateway from, Gateway to)
 	{
 		from.setBusy(false);
 		to.setBusy(false);
 	}
 
 	private void sendPathInfo(List<Gateway> hackingPath, User sender)
 	{
 		ISFSObject pathReback = SFSObject.newInstance();
 		SFSArray pathArray = new SFSArray();
 		for (int i = 0; i < hackingPath.size(); i++) 
 			pathArray.addUtfString(hackingPath.get(i).getX()+":"+hackingPath.get(i).getY());
 		
 		pathReback.putSFSArray("hackingPath", pathArray);
 		send("path", pathReback, sender);
 	}
 
 	private void sendError(String errorType, User sender)
 	{
 		ISFSObject reback = SFSObject.newInstance();
 		reback.putBool("success", false);
 		
 		//FIXME switch on errors
 		if(errorType == "NOPATH")
 		{
 			trace("No hacking path available");
 			reback.putUtfString("error", "NOPATH");
 		}
 		if(errorType == "HACKDISABLED")
 		{
 			trace("Hack request from " + sender.getName() + ": FAILED since the hack is disabled for this player OR gateway is disabled for some seconds");
 			reback.putUtfString("error", "HACKDISABLED");
 		}
 		send("error", reback, sender);	
 	}
 }
