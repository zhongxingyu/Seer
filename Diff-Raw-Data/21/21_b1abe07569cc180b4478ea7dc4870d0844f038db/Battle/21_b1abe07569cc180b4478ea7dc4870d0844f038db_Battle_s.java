 package burnedkirby.TurnBasedBattleMod;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import burnedkirby.TurnBasedBattleMod.CombatantInfo.Type;
 import burnedkirby.TurnBasedBattleMod.core.network.BattleCombatantPacket;
 import burnedkirby.TurnBasedBattleMod.core.network.BattleMessagePacket;
 import burnedkirby.TurnBasedBattleMod.core.network.BattlePhaseEndedPacket;
 import burnedkirby.TurnBasedBattleMod.core.network.BattleStatusPacket;
 import burnedkirby.TurnBasedBattleMod.core.network.CombatantHealthRatioPacket;
 import burnedkirby.TurnBasedBattleMod.core.network.InitiateBattlePacket;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityCreature;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.passive.EntityAnimal;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.MinecraftException;
 
 public class Battle{
 	private Map<Integer,CombatantInfo> combatants;
 	private Stack<CombatantInfo> newCombatantQueue;
 //	private Stack<CombatantInfo> removeCombatantQueue;
 	private int battleID;
 
 	protected enum BattleStatus {
 		PLAYER_PHASE, CALCULATIONS_PHASE, END_CHECK_PHASE
 	}
 	
 	private BattleStatus status;
 	
 //	private boolean phaseInProgress;
 	
 	protected boolean battleEnded;
 	
 	private short healthUpdateTick;
 	private final short healthUpdateTime = 4;
 	
 	public Battle(int id)
 	{
 		battleID = id;
 	}
 	
 	public Battle(int id, Stack<CombatantInfo> newCombatants)
 	{
 		battleID = id;
 		combatants = new TreeMap<Integer,CombatantInfo>();
 		newCombatantQueue = new Stack<CombatantInfo>();
 //		removeCombatantQueue = new Stack<CombatantInfo>();
 		
 		CombatantInfo combatant;
 		while(!newCombatants.isEmpty())
 		{
 			combatant = newCombatants.pop();
 			System.out.println("Initializing battle with combatant " + combatant.name);
 			if(combatant.isPlayer)
 			{
 				PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID,combatant).makePacket(), (Player)combatant.entityReference);
 			}
 			else if(combatant.entityReference instanceof EntityMob)
 			{
 				combatant.type = Type.ATTACK;
 			}
 			else if(combatant.entityReference instanceof EntityAnimal)
 			{
 				combatant.type = Type.FLEE;
 			}
 			combatants.put(combatant.id, combatant);
 		}
 		
 		status = BattleStatus.PLAYER_PHASE;
 		battleEnded = false;
 //		phaseInProgress = false;
 		healthUpdateTick = healthUpdateTime;
 	}
 	
 	public void addCombatant(CombatantInfo newCombatant)
 	{
 		if(status == BattleStatus.PLAYER_PHASE)
 		{
 			if(newCombatant.entityReference instanceof EntityMob)
 			{
 				newCombatant.type = Type.ATTACK;
 			}
 			
 			combatants.put(newCombatant.id, newCombatant);
 
 			if(newCombatant.isPlayer)
 				PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID,newCombatant).makePacket(), (Player)newCombatant.entityReference);
 			
 			notifyPlayers(true);
 			notifyPlayersWithMessage(newCombatant.name + " has entered battle!");
 		}
 		else
 		{
 			newCombatantQueue.push(newCombatant);
 		}
 	}
 	
 //	public void manageDeath(int id)
 //	{
 //		if(status == BattleStatus.PLAYER_PHASE)
 //		{
 //			CombatantInfo removed = combatants.remove(id);
 //			
 //			if(removed.isPlayer)
 //				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false, false, combatants.size()).makePacket(), (Player)removed.entityReference);
 //			
 //			notifyPlayers(true);
 //		}
 //		else
 //		{
 //			removeCombatantQueue.add(combatants.get(id));
 //		}
 //	}
 	
 	public CombatantInfo getCombatant(int combatantID)
 	{
 		return combatants.get(combatantID);
 	}
 	
 	public int getBattleID()
 	{
 		return battleID;
 	}
 	
 	public void updatePlayerStatus(CombatantInfo combatant)
 	{
 		if(this.status != BattleStatus.PLAYER_PHASE || !combatants.containsValue(combatant))
 		{
 			System.out.println("WARNING: Battle " + battleID + " failed updatePlayerStatus."); //TODO debug
 			return;
 		}
 		
 		if(combatants.containsKey(combatant.id) && combatants.containsKey(combatant.target))
 			combatants.get(combatant.id).updateBattleInformation(combatant);
 		else
 			notifyPlayers(false);
 	}
 	
 	public boolean isInBattle(int id)
 	{
 		return combatants.containsKey(id);
 	}
 	
 	private boolean getPlayersReady()
 	{
 		for(CombatantInfo combatant : combatants.values())
 		{
 			if(combatant.isPlayer && !combatant.ready)
 				return false;
 		}
 		return true;
 	}
 	
 	public synchronized boolean update()
 	{
 		if(!battleEnded)
 			switch(status)
 			{
 			case PLAYER_PHASE:
 				playerPhase();
 				break;
 			case CALCULATIONS_PHASE:
 				calculationsPhase();
 				break;
 			case END_CHECK_PHASE:
 				endCheckPhase();
 				break;
 			}
 		else
 			notifyPlayers(false);
 		
 		return battleEnded;
 	}
 
 	private void playerPhase()
 	{
 //		if(phaseInProgress)
 //			return;
 //		phaseInProgress = true;
 		
 		boolean forceUpdate = false;
 		
 		if(!newCombatantQueue.isEmpty())
 			forceUpdate = true;
 		while(!newCombatantQueue.isEmpty())
 		{
 			CombatantInfo combatant = newCombatantQueue.pop();
 			combatants.put(combatant.id, combatant);
 			notifyPlayersWithMessage(combatant.name + " has entered battle!");
 		}
 		
 		Iterator<CombatantInfo> iter = combatants.values().iterator();
 		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
 		CombatantInfo combatant;
 		while(iter.hasNext())
 		{
 			combatant = iter.next();
			if(combatant.entityReference.getHealth() <= 0)
 			{
 				iter.remove();
 				if(!combatant.isPlayer)
 					messageQueue.push(combatant);
 			}
 			
 		}
 		
 		while(!messageQueue.isEmpty())
 			notifyPlayersWithMessage(messageQueue.pop().name + " has died!");
 
 		notifyPlayers(forceUpdate);
 		
 		checkIfBattleEnded();
 		
 		
 		if(getPlayersReady())
 		{
 			status = BattleStatus.CALCULATIONS_PHASE;
 			System.out.println("PlayerPhase ended.");
 		}
 		
 //		phaseInProgress = false;
 		
 		if(--healthUpdateTick == 0)
 		{
 			notifyPlayersHealthInformation();
 			healthUpdateTick = healthUpdateTime;
 		}
 	}
 	
 	private void calculationsPhase()
 	{
 //		if(phaseInProgress)
 //			return;
 //		phaseInProgress = true;
 		
 		//Combatant flee phase
 		Iterator<CombatantInfo> iter = combatants.values().iterator();
 		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
 		CombatantInfo combatant;
 		while(iter.hasNext())
 		{
 			combatant = iter.next();
 			if(combatant.type != Type.FLEE)
 				continue;
 			
 			if(fleeCheck(combatant))
 			{
 				iter.remove();
 				if(combatant.isPlayer)
 					notifyPlayer(false, combatant, true);
 				messageQueue.push(combatant);
 			}
 		}
 		
 		while(!messageQueue.isEmpty())
 		{
 			notifyPlayersWithMessage(messageQueue.pop().name + " has fled battle!");
 		}
 		
 		//Combatant attack phase
 		EntityLiving combatantEntity;
 		EntityLiving targetEntity;
 		CombatantInfo[] combatantArray = combatants.values().toArray(new CombatantInfo[0]);
 		int rand;
 		for(int i=0; i < combatantArray.length; i++)
 		{
 			combatant = combatantArray[i];
 			if(combatant.type != Type.ATTACK)
 				continue;
 			
 			combatantEntity = combatant.entityReference;
			if(combatantEntity.getHealth() <= 0)
 				continue;
 			
 			synchronized(ModMain.bss.attackingLock)
 			{
 				if(combatant.isPlayer)
 				{
 					if(combatants.get(combatant.target) != null)
 						targetEntity = combatants.get(combatant.target).entityReference;
 					else
 						targetEntity = null;
 					
					if(targetEntity == null || targetEntity.getHealth() < 0 || !combatants.containsKey(targetEntity.entityId))
 						continue;
 					targetEntity.hurtResistantTime = 0;
 					notifyPlayersWithMessage(combatantEntity.getEntityName() + " attacks " + targetEntity.getEntityName() + "!");
 					ModMain.bss.attackingEntity = combatantEntity;
 					((EntityPlayer)combatantEntity).attackTargetEntityWithCurrentItem(targetEntity);
 				}
 				else if(combatantEntity instanceof EntityMob)
 				{
 					if(((EntityCreature)combatantEntity).getEntityToAttack() instanceof EntityLiving)
 						targetEntity = (EntityLiving) ((EntityMob)combatantEntity).getEntityToAttack();
 					else
 						targetEntity = null;
 					
 					if(targetEntity == null)
 					{
 						rand = ModMain.bss.random.nextInt(combatants.size()) + combatants.size();
 						int k=0,j=0,picked=0;
 						for(; j<rand; k++)
 						{
 							if(combatantArray[k % combatantArray.length].isPlayer)
 							{
 								j++;
 								picked = k % combatantArray.length;
 							}
 							if(k > combatants.size() && j == 0)
 							{
 								picked = -1;
 								break;
 							}
 						}
 						if(picked == -1)
 							continue;
 						targetEntity = combatantArray[picked].entityReference;
 					}
 					targetEntity.hurtResistantTime = 0;
 					notifyPlayersWithMessage(combatantEntity.getEntityName() + " attacks " + targetEntity.getEntityName() + "!");
 					ModMain.bss.attackingEntity = combatantEntity;
 					((EntityMob)combatantEntity).attackEntityAsMob(targetEntity);
 				}
 				else
 					System.out.println("Else triggered");//TODO
 				ModMain.bss.attackingEntity = null;
 			}
 		}
 		
 		status = BattleStatus.END_CHECK_PHASE;
 		System.out.println("Calculations phase ended.");
 		
 //		phaseInProgress = false;
 	}
 	
 	private void endCheckPhase()
 	{
 //		if(phaseInProgress)
 //			return;
 //		phaseInProgress = true;
 		CombatantInfo combatantRef;
 		Iterator<CombatantInfo> iter = combatants.values().iterator();
 		CombatantInfo defaultInfo = new CombatantInfo();
 		defaultInfo.ready = false;
 		defaultInfo.type = Type.DO_NOTHING;
 		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
 		
 		while(iter.hasNext())
 		{
 			combatantRef = iter.next();
			if(combatantRef.entityReference.getHealth() <= 0)
 			{
 				System.out.println("Entity is dead, removing");
 				if(combatantRef.isPlayer)
 					notifyPlayer(false, combatantRef, false);
 				else
 					messageQueue.push(combatantRef);
 				iter.remove();
 				continue;
 			}
 
 			if(combatantRef.isPlayer)
 			{
 				defaultInfo.target = combatantRef.target;
 				defaultInfo.id = combatantRef.id;
 				combatantRef.updateBattleInformation(defaultInfo);
 			}
 		}
 		
 		while(!messageQueue.isEmpty())
 			notifyPlayersWithMessage(messageQueue.pop().name + " has died!");
 		
 		checkIfBattleEnded();
 		
 //		notifyPlayersTurnEnded();
 		
 		status = BattleStatus.PLAYER_PHASE;
 		System.out.println("End phase ended.");
 
 		notifyPlayers(false);
 		
 //		phaseInProgress = false;
 	}
 	
 	private void checkIfBattleEnded()
 	{
 		if(battleEnded)
 			return;
 		
 		int players = 0;
 		int sideOne = 0;
 		int sideTwo = 0;
 		for(CombatantInfo combatant : combatants.values())
 		{
 			if(combatant.isSideOne)
 				sideOne++;
 			else
 				sideTwo++;
 			
 			if(combatant.isPlayer)
 				players++;
 		}
 		
 		if(sideOne == 0 || sideTwo == 0 || players == 0)
 		{
 			battleEnded = true;
 			System.out.println("Battle " + battleID + " ended.");
 		}
 	}
 	
 	private boolean fleeCheck(CombatantInfo fleeingCombatant)
 	{
 		int enemySide = 0;
 		int ownSide = 0;
 		int diff;
 		double rand;
 
 		if(enemySide == ownSide)
 		{
 			return BattleSystemServer.random.nextBoolean();
 		}
 		else
 		{
 			diff = Math.abs(ownSide - enemySide);
 			rand = BattleSystemServer.random.nextDouble() / Math.log((double)(diff + Math.E));
 			return ownSide > enemySide ? rand <= 0.67d : rand > 0.67d;
 		}
 	}
 	
 	protected void notifyPlayers(boolean forceUpdate)
 	{
 		for(CombatantInfo combatant : combatants.values())
 		{
 			if(combatant.isPlayer)
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(!battleEnded && !(combatant.entityReference.getHealth() <= 0), forceUpdate, combatants.size(), status == BattleStatus.PLAYER_PHASE, combatant.ready).makePacket(), (Player)combatant.entityReference);
 		}
 	}
 	
 	protected void notifyPlayer(boolean forceUpdate, CombatantInfo player, boolean fledBattle)
 	{
		PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(!battleEnded && !(player.entityReference.getHealth() <= 0) && !fledBattle, forceUpdate, combatants.size(), status == BattleStatus.PLAYER_PHASE, player.ready).makePacket(), (Player)player.entityReference);
		System.out.println("Sent packet with battle state (is over) " + (!battleEnded && !(player.entityReference.getHealth() <= 0) && !fledBattle));
 	}
 	
 	protected void notifyPlayersWithMessage(String message)
 	{
 		for(CombatantInfo combatant : combatants.values())
 		{
 			if(combatant.isPlayer)
 			{
 				PacketDispatcher.sendPacketToPlayer(new BattleMessagePacket(message).makePacket(), (Player) combatant.entityReference);
 			}
 		}
 	}
 	
 //	protected void notifyPlayersTurnEnded()
 //	{
 //		for(CombatantInfo combatant : combatants.values())
 //		{
 //			if(combatant.isPlayer)
 //			{
 //				PacketDispatcher.sendPacketToPlayer(new BattlePhaseEndedPacket().makePacket(), (Player)combatant.entityReference);
 //			}
 //		}
 //	}
 	
 	protected void notifyPlayerOfCombatants(EntityLiving player)
 	{
 		for(CombatantInfo combatant : combatants.values())
 		{
 			PacketDispatcher.sendPacketToPlayer(new BattleCombatantPacket(combatant).makePacket(), (Player)player);
 		}
 	}
 	
 	protected void notifyPlayersHealthInformation()
 	{
 		CombatantInfo[] combatantListCopy = combatants.values().toArray(new CombatantInfo[0]);
 		for(int i=0; i<combatantListCopy.length; i++)
 		{
 			combatantListCopy[i].updateHealthRatio((short)((float)combatantListCopy[i].entityReference.getHealth() / (float)combatantListCopy[i].entityReference.getMaxHealth() * 10.0f));
 		}
 		
 		for(int i=0; i<combatantListCopy.length; i++)
 		{
 			if(combatantListCopy[i].isPlayer)
 			{
 				for(int j=0; j<combatantListCopy.length; j++)
 				{
 					PacketDispatcher.sendPacketToPlayer(new CombatantHealthRatioPacket(combatantListCopy[j].id, combatantListCopy[j].healthRatio).makePacket(), (Player)combatantListCopy[i].entityReference);
 				}
 			}
 		}
 	}
 }
