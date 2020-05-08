 /*
  * This file is part of aion-emu <aion-emu.com>.
  *
  *  aion-emu is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-emu is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aionemu.gameserver.controllers;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Future;
 
 import com.aionemu.gameserver.controllers.SummonController.UnsummonType;
 import com.aionemu.gameserver.controllers.attack.AggroInfo;
 import com.aionemu.gameserver.controllers.attack.AttackResult;
 import com.aionemu.gameserver.controllers.attack.AttackUtil;
 import com.aionemu.gameserver.model.TaskId;
 import com.aionemu.gameserver.model.gameobjects.Creature;
 import com.aionemu.gameserver.model.gameobjects.Gatherable;
 import com.aionemu.gameserver.model.gameobjects.Kisk;
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.StaticObject;
 import com.aionemu.gameserver.model.gameobjects.Summon;
 import com.aionemu.gameserver.model.gameobjects.VisibleObject;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.model.gameobjects.player.SkillListEntry;
 import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
 import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
 import com.aionemu.gameserver.model.gameobjects.stats.PlayerGameStats;
 import com.aionemu.gameserver.model.group.PlayerGroup;
 import com.aionemu.gameserver.model.templates.quest.QuestItems;
 import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
 import com.aionemu.gameserver.model.templates.stats.PlayerStatsTemplate;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHERABLE_INFO;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_KISK_UPDATE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_LEVEL_UPDATE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_NEARBY_QUESTS;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_INFO;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_PRIVATE_STORE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_CANCEL;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_PANEL;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
 import com.aionemu.gameserver.questEngine.QuestEngine;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.restrictions.RestrictionsManager;
 import com.aionemu.gameserver.services.ClassChangeService;
 import com.aionemu.gameserver.services.DuelService;
 import com.aionemu.gameserver.services.GroupService;
 import com.aionemu.gameserver.services.ItemService;
 import com.aionemu.gameserver.services.QuestService;
 import com.aionemu.gameserver.services.SkillLearnService;
 import com.aionemu.gameserver.services.ZoneService;
 import com.aionemu.gameserver.services.ZoneService.ZoneUpdateMode;
 import com.aionemu.gameserver.skillengine.SkillEngine;
 import com.aionemu.gameserver.skillengine.model.HealType;
 import com.aionemu.gameserver.skillengine.model.Skill;
 import com.aionemu.gameserver.taskmanager.tasks.PacketBroadcaster.BroadcastMode;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 import com.aionemu.gameserver.utils.ThreadPoolManager;
 import com.aionemu.gameserver.utils.stats.StatFunctions;
 import com.aionemu.gameserver.world.World;
 import com.aionemu.gameserver.world.zone.ZoneInstance;
 import com.google.inject.internal.Nullable;
 
 /**
  * This class is for controlling players.
  * 
  * @author -Nemesiss-, ATracer (2009-09-29), xavier, Sarynth
  * @author RotO (Attack-speed hack protection)
  */
 public class PlayerController extends CreatureController<Player>
 {
 	private boolean			isInShutdownProgress;
 
 	/**
 	 * Zone update mask
 	 */
 	private volatile byte	zoneUpdateMask;
 
 	private long lastAttackMilis = 0;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void see(VisibleObject object)
 	{
 		super.see(object);
 		if(object instanceof Player)
 		{
 			PacketSendUtility.sendPacket(getOwner(), new SM_PLAYER_INFO((Player) object, getOwner().isEnemyPlayer((Player)object)));
 			getOwner().getEffectController().sendEffectIconsTo((Player) object);
 		}
 		else if (object instanceof Kisk)
 		{
 			Kisk kisk = ((Kisk) object);
 			PacketSendUtility.sendPacket(getOwner(), new SM_NPC_INFO(getOwner(), kisk));
 			if (getOwner().getCommonData().getRace() == kisk.getOwnerRace())
 				PacketSendUtility.sendPacket(getOwner(), new SM_KISK_UPDATE(kisk));
 		}
 		else if(object instanceof Npc)
 		{
 			boolean update = false;
 			Npc npc = ((Npc) object);
 
 			PacketSendUtility.sendPacket(getOwner(), new SM_NPC_INFO(npc, getOwner()));
 
 			for(int questId : QuestEngine.getInstance().getNpcQuestData(npc.getNpcId()).getOnQuestStart())
 			{
 				if(QuestService.checkStartCondition(new QuestEnv(object, getOwner(), questId, 0)))
 				{
 					if(!getOwner().getNearbyQuests().contains(questId))
 					{
 						update = true;
 						getOwner().getNearbyQuests().add(questId);
 					}
 				}
 			}
 			if(update)
 				updateNearbyQuestList();
 		}
 		else if(object instanceof Summon)
 		{
 			Summon npc = ((Summon) object);		
 			PacketSendUtility.sendPacket(getOwner(), new SM_NPC_INFO(npc));
 		}
 		else if(object instanceof Gatherable || object instanceof StaticObject)
 		{
 			PacketSendUtility.sendPacket(getOwner(), new SM_GATHERABLE_INFO(object));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void notSee(VisibleObject object, boolean isOutOfRange)
 	{
 		super.notSee(object, isOutOfRange);
 		if(object instanceof Npc)
 		{
 			boolean update = false;
 			for(int questId : QuestEngine.getInstance().getNpcQuestData(((Npc) object).getNpcId()).getOnQuestStart())
 			{
 				if(QuestService.checkStartCondition(new QuestEnv(object, getOwner(), questId, 0)))
 				{
 					if(getOwner().getNearbyQuests().contains(questId))
 					{
 						update = true;
 						getOwner().getNearbyQuests().remove(getOwner().getNearbyQuests().indexOf(questId));
 					}
 				}
 			}
 			if(update)
 				updateNearbyQuestList();
 		}
 
 		PacketSendUtility.sendPacket(getOwner(), new SM_DELETE(object, isOutOfRange ? 0 : 15));
 	}
 
 	public void updateNearbyQuests()
 	{
 		getOwner().getNearbyQuests().clear();
 		for(VisibleObject obj : getOwner().getKnownList())
 		{
 			if(obj instanceof Npc)
 			{
 				for(int questId : QuestEngine.getInstance().getNpcQuestData(((Npc) obj).getNpcId()).getOnQuestStart())
 				{
 					if(QuestService.checkStartCondition(new QuestEnv(obj, getOwner(), questId, 0)))
 					{
 						if(!getOwner().getNearbyQuests().contains(questId))
 						{
 							getOwner().getNearbyQuests().add(questId);
 						}
 					}
 				}
 			}
 		}
 		updateNearbyQuestList();
 	}
 
 	/**
 	 * Will be called by ZoneManager when player enters specific zone
 	 * 
 	 * @param zoneInstance
 	 */
 	public void onEnterZone(ZoneInstance zoneInstance)
 	{
 		QuestEngine.getInstance().onEnterZone(new QuestEnv(null, this.getOwner(), 0, 0), zoneInstance.getTemplate().getName());
 	}
 
 	/**
 	 * Will be called by ZoneManager when player leaves specific zone
 	 * 
 	 * @param zoneInstance
 	 */
 	public void onLeaveZone(ZoneInstance zoneInstance)
 	{
 
 	}
 
 	/**
 	 * Set zone instance as null (Where no zones defined)
 	 */
 	public void resetZone()
 	{
 		getOwner().setZoneInstance(null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * Should only be triggered from one place (life stats)
 	 */
 	@Override
 	public void onDie(@Nullable Creature lastAttacker)
 	{		
 		Player player = this.getOwner();
 		
 		Creature master = null;
 		if(lastAttacker != null)
 			master = lastAttacker.getMaster();
 		
 		if(master instanceof Player)
 		{
 			if(isDueling((Player) master))
 			{
 				DuelService.getInstance().onDie(player);
 				return;
 			}
 		}
 		
 		this.doReward();
 		
 		super.onDie(lastAttacker);
 		
 		if(master instanceof Npc || master == player)
 		{
 			if(player.getLevel() > 4)
 				player.getCommonData().calculateExpLoss();
 		}
 		
 		/**
 		 * Release summon
 		 */
 		Summon summon = player.getSummon();
 		if(summon != null)
 			summon.getController().release(UnsummonType.UNSPECIFIED);
 
 		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 13, 0, lastAttacker == null ? 0 :
 			lastAttacker.getObjectId()), true);
 		ReviveType reviveType = (player.getKisk() == null ? ReviveType.BIND_REVIVE : ReviveType.KISK_REVIVE);
 		PacketSendUtility.sendPacket(player, new SM_DIE(reviveType));
 		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.DIE);
 		QuestEngine.getInstance().onDie(new QuestEnv(null, player, 0, 0));
 	}
 	
 	@Override
 	public void doReward()
 	{
 		final Player victim = getOwner();
 		
 		// winner is the player that receives the kill count
 		final Player winner = victim.getAggroList().getMostPlayerDamage();
 		
 		int totalDamage = victim.getAggroList().getTotalDamage();
 		
 		if (totalDamage == 0 || winner == null)
 		{
 			return;
 		}
 		
 		// Add Player Kill to record.
 		winner.getAbyssRank().setAllKill();
 		
 		// Keep track of how much damage was dealt by players
 		// so we can remove AP based on player damage...
 		int playerDamage = 0;
 		
 		// Distribute AP to groups and players that had damage.
 		for(AggroInfo aggro : victim.getAggroList().getFinalDamageList(true))
 		{
 			if (aggro.getAttacker() instanceof Player)
 			{
 				// Reward Player
 				Player p = ((Player)aggro.getAttacker());
				
				// Don't Reward Player of Same Faction.
				if (p.getCommonData().getRace() == victim.getCommonData().getRace())
					continue;
				
 				// This needs to be unique for each player and group
 				int baseApReward = StatFunctions.calculatePvpApGained(victim,
 					winner.getAbyssRank().getRank().getId(), winner.getLevel());
 				
 				int apPlayerReward = (int)(aggro.getDamage() * baseApReward / totalDamage);
 				p.getCommonData().addAp(Math.round(apPlayerReward * winner.getRates().getApPlayerRate()));
 			}
 			else if (aggro.getAttacker() instanceof PlayerGroup)
 			{
 				// Reward Group
 				PlayerGroup pg = ((PlayerGroup)aggro.getAttacker());
 				
				// Don't Reward Player of Same Faction.
				if (pg.getGroupLeader().getCommonData().getRace() == victim.getCommonData().getRace())
					continue;
				
 				float groupApPercentage = (float)aggro.getDamage() / totalDamage;
 				GroupService.getInstance().doReward(victim, pg, groupApPercentage);
 			}
			
			// Add damage last, so we don't include damage from same race. (Duels, Arena)
			playerDamage += aggro.getDamage();
 		}
 		
 		// Apply lost AP to defeated player
 		final int apLost = StatFunctions.calculatePvPApLost(victim, winner);
 		final int apActuallyLost = (int)(apLost * playerDamage / totalDamage);
 		
 		if (apActuallyLost > 0)
 			victim.getCommonData().addAp(-apActuallyLost);
 		
 		// DP reward 
 		// TODO: Figure out what DP reward should be for PvP
 		//int currentDp = winner.getCommonData().getDp();
 		//int dpReward = StatFunctions.calculateSoloDPReward(winner, getOwner());
 		//winner.getCommonData().setDp(dpReward + currentDp);
 		
 	}
 	
 	@Override
 	public void onRespawn()
 	{
 		super.onRespawn();
 		startProtectionActiveTask();
 	}
 
 	@Override
 	public void attackTarget(Creature target)
 	{
 		Player player = getOwner();
 		
 		/**
 		 * Check all prerequisites
 		 */
 		if(target == null || !player.canAttack())
 			return;
 
 		PlayerGameStats gameStats = player.getGameStats();
 
 		// check player attack Z distance
 		if(Math.abs(player.getZ() - target.getZ()) > 6)
 			return;
 
 		if(!RestrictionsManager.canAttack(player, target))
 			return;
 
 		int attackSpeed = gameStats.getCurrentStat(StatEnum.ATTACK_SPEED);
 		long milis = System.currentTimeMillis();
 		if (milis - lastAttackMilis < attackSpeed)
 		{
 			/**
 			 * Hack!
 			 */
 			return;
 		}
 		lastAttackMilis = milis;
 
 		/**
 		 * notify attack observers
 		 */
 		super.attackTarget(target);
 		
 		/**
 		 * Calculate and apply damage
 		 */
 		List<AttackResult> attackResult = AttackUtil.calculateAttackResult(player, target);
 
 		int damage = 0;
 		for(AttackResult result : attackResult)
 		{
 			damage += result.getDamage();
 		}
 
 		long time = System.currentTimeMillis();
 		int attackType = 0; // TODO investigate attack types
 		PacketSendUtility.broadcastPacket(player, new SM_ATTACK(player, target, gameStats.getAttackCounter(),
 			(int) time, attackType, attackResult), true);
 
 		target.getController().onAttack(player, damage);
 
 		gameStats.increaseAttackCounter();
 	}
 
 	@Override
 	public void onAttack(Creature creature, int skillId, TYPE type, int damage)
 	{
 		if(getOwner().getLifeStats().isAlreadyDead())
 			return;
 		
 		// Reduce the damage to exactly what is required to ensure death.
 		// - Important that we don't include 7k worth of damage when the
 		//   creature only has 100 hp remaining. (For AggroList dmg count.)
 		if (damage > getOwner().getLifeStats().getCurrentHp())
 			damage = getOwner().getLifeStats().getCurrentHp() + 1;
 		
 		super.onAttack(creature, skillId, type, damage);
 
 		if(getOwner().isInvul())
 			damage = 0;
 
 		getOwner().getLifeStats().reduceHp(damage, creature);
 		PacketSendUtility.broadcastPacket(getOwner(), new SM_ATTACK_STATUS(getOwner(), type, skillId, damage), true);
 	}
 
 	/**
 	 * 
 	 * @param skillId
 	 */
 	@Override
 	public void useSkill(int skillId)
 	{
 		Player player = getOwner();
 
 		Skill skill = SkillEngine.getInstance().getSkillFor(player, skillId, player.getTarget());
 		if(skill != null)
 		{
 			if(!RestrictionsManager.canUseSkill(player, skill))
 				return;
 
 			skill.useSkill();
 		}
 	}
 
 	@Override
 	public void onMove()
 	{
 		super.onMove();
 		addZoneUpdateMask(ZoneUpdateMode.ZONE_UPDATE);
 	}
 
 	@Override
 	public void onStopMove()
 	{
 		super.onStopMove();
 	}
 
 	@Override
 	public void onStartMove()
 	{
 		cancelCurrentSkill();
 		super.onStartMove();
 	}
 	
 	/**
 	 * Cancel current skill and remove cooldown
 	 */
 	public void cancelCurrentSkill()
 	{
 		Player player = getOwner();
 		Skill castingSkill = player.getCastingSkill();
 		if(castingSkill != null)
 		{
 			player.removeSkillCoolDown(castingSkill.getSkillTemplate().getSkillId());
 			player.setCasting(null);
 			PacketSendUtility.sendPacket(player, new SM_SKILL_CANCEL(player));
 		}	
 	}
 
 	/**
 	 * 
 	 */
 	public void updatePassiveStats()
 	{
 		Player player = getOwner();
 		for(SkillListEntry skillEntry : player.getSkillList().getAllSkills())
 		{
 			Skill skill = SkillEngine.getInstance().getSkillFor(player, skillEntry.getSkillId(), player.getTarget());
 			if(skill != null && skill.isPassive())
 			{
 				skill.useSkill();
 			}
 		}
 	}
 
 	@Override
 	public Player getOwner()
 	{
 		return (Player) super.getOwner();
 	}
 
 	@Override
 	public void onRestore(HealType healType, int value)
 	{
 		super.onRestore(healType, value);
 		switch(healType)
 		{
 			case DP:
 				getOwner().getCommonData().addDp(value);
 				break;
 		}
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @return
 	 */
 	public boolean isDueling(Player player)
 	{
 		return DuelService.getInstance().isDueling(player.getObjectId(), getOwner().getObjectId());
 	}
 
 	public void updateNearbyQuestList()
 	{
 		getOwner().addPacketBroadcastMask(BroadcastMode.UPDATE_NEARBY_QUEST_LIST);
 	}
 
 	public void updateNearbyQuestListImpl()
 	{
 		PacketSendUtility.sendPacket(getOwner(), new SM_NEARBY_QUESTS(getOwner().getNearbyQuests()));
 	}
 
 	public boolean isInShutdownProgress()
 	{
 		return isInShutdownProgress;
 	}
 
 	public void setInShutdownProgress(boolean isInShutdownProgress)
 	{
 		this.isInShutdownProgress = isInShutdownProgress;
 	}
 
 	/**
 	 * Handle dialog
 	 */
 	@Override
 	public void onDialogSelect(int dialogId, Player player, int questId)
 	{
 		switch(dialogId)
 		{
 			case 2:
 				PacketSendUtility.sendPacket(player, new SM_PRIVATE_STORE(getOwner().getStore()));
 				break;
 		}
 	}
 
 	/**
 	 * @param level
 	 */
 	public void upgradePlayer(int level)
 	{
 		Player player = getOwner();
 
 		PlayerStatsTemplate statsTemplate = sp.getPlayerService().getPlayerStatsData().getTemplate(player);
 		player.setPlayerStatsTemplate(statsTemplate);
 		// update stats after setting new template
 		player.getGameStats().doLevelUpgrade();
 		player.getLifeStats().synchronizeWithMaxStats();
 
 		PacketSendUtility.broadcastPacket(player, new SM_LEVEL_UPDATE(player.getObjectId(), 0, level), true);
 
 		// Temporal
 		ClassChangeService.showClassChangeDialog(player);
 
 		QuestEngine.getInstance().onLvlUp(new QuestEnv(null, player, 0, 0));
 		updateNearbyQuests();
 		
 		PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
 
 		if(level == 10 && player.getSkillList().getSkillEntry(30001) != null)
 		{
 			int skillLevel = player.getSkillList().getSkillLevel(30001);
 			player.getSkillList().removeSkill(30001);
 			PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player));
 			player.getSkillList().addSkill(player, 30002, skillLevel, true);
 		}
 		// add new skills
 		SkillLearnService.addNewSkills(player, false);
 
 		/** update member list packet if player is legion member **/
 		if(player.isLegionMember())
 			sp.getLegionService().updateMemberInfo(player);
 	}
 
 	/**
 	 * After entering game player char is "blinking" which means that it's in under some protection, after making an
 	 * action char stops blinking. - Starts protection active - Schedules task to end protection
 	 */
 	public void startProtectionActiveTask()
 	{
 		getOwner().setVisualState(CreatureVisualState.BLINKING);
 		PacketSendUtility.broadcastPacket(getOwner(), new SM_PLAYER_STATE(getOwner()), true);
 		Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable(){
 
 			@Override
 			public void run()
 			{
 				stopProtectionActiveTask();
 			}
 		}, 60000);
 		addTask(TaskId.PROTECTION_ACTIVE, task);
 	}
 
 	/**
 	 * Stops protection active task after first move or use skill
 	 */
 	public void stopProtectionActiveTask()
 	{
 		cancelTask(TaskId.PROTECTION_ACTIVE);
 		Player player = getOwner();
 		if(player != null && player.isSpawned())
 		{
 			player.unsetVisualState(CreatureVisualState.BLINKING);
 			PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
 		}
 	}
 
 	/**
 	 * When player arrives at destination point of flying teleport
 	 */
 	public void onFlyTeleportEnd()
 	{
 		Player player = getOwner();
 		player.unsetState(CreatureState.FLIGHT_TELEPORT);
 		player.setFlightTeleportId(0);
 		player.setFlightDistance(0);
 		player.setState(CreatureState.ACTIVE);
 		addZoneUpdateMask(ZoneUpdateMode.ZONE_REFRESH);
 	}
 
 	/**
 	 * Zone update mask management
 	 * 
 	 * @param mode
 	 */
 	public final void addZoneUpdateMask(ZoneUpdateMode mode)
 	{
 		zoneUpdateMask |= mode.mask();
 		ZoneService.getInstance().add(getOwner());
 	}
 
 	public final void removeZoneUpdateMask(ZoneUpdateMode mode)
 	{
 		zoneUpdateMask &= ~mode.mask();
 	}
 
 	public final byte getZoneUpdateMask()
 	{
 		return zoneUpdateMask;
 	}
 
 	/**
 	 * Update zone taking into account the current zone
 	 */
 	public void updateZoneImpl()
 	{
 		ZoneService.getInstance().checkZone(getOwner());
 	}
 
 	/**
 	 * Refresh completely zone irrespective of the current zone
 	 */
 	public void refreshZoneImpl()
 	{
 		ZoneService.getInstance().findZoneInCurrentMap(getOwner());
 	}
 
 	/**
 	 * 
 	 */
 	public void ban()
 	{
 		// sp.getTeleportService().teleportTo(this.getOwner(), 510010000, 256f, 256f, 49f, 0);
 	}
 
 	/**
 	 * Check water level (start drowning) and map death level (die)
 	 */
 	public void checkWaterLevel()
 	{
 		Player player = getOwner();
 		World world = World.getInstance();
 		float z = player.getZ();
 		
 		if(player.getLifeStats().isAlreadyDead())
 			return;
 		
 		if(z < world.getWorldMap(player.getWorldId()).getDeathLevel())
 		{
 			die();
 			return;
 		}
 		
 		ZoneInstance currentZone = player.getZoneInstance();
 		if(currentZone != null && currentZone.isBreath())
 			return;
 		
 		//TODO need fix character height
 		float playerheight = player.getPlayerAppearance().getHeight() * 1.6f;
 		if(z < world.getWorldMap(player.getWorldId()).getWaterLevel() - playerheight)
 			ZoneService.getInstance().startDrowning(player);
 		else
 			ZoneService.getInstance().stopDrowning(player);
 	}
 
 	@Override
 	public void createSummon(int npcId, int skillLvl)
 	{
 		Player master = getOwner();
 		Summon summon = sp.getSpawnEngine().spawnSummon(master, npcId, skillLvl);
 		master.setSummon(summon);
 		PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL(summon));
 		PacketSendUtility.broadcastPacket(master, new SM_EMOTION(summon, 30));
 	}
 	
 	public boolean addItems(int itemId, int count)
 	{
 		return ItemService.addItems(getOwner(), Collections.singletonList(new QuestItems(itemId, count)));
 	}
 }
