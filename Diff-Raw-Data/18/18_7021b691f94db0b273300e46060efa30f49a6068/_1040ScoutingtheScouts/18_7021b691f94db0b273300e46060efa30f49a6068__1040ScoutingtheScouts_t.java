 /*
  * This file is part of aion-unique <aion-unique.org>.
  *
  * aion-unique is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * aion-unique is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
  */
 package quest.eltnen;
 
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 import com.aionemu.gameserver.services.TeleportService;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 import com.google.inject.Inject;
 
 /**
  * @author Rhys2002
  * 
  */
 public class _1040ScoutingtheScouts extends QuestHandler
 {
 	private final static int	questId	= 1040;
 
 	@Inject
 	TeleportService teleportService;
 	
 	public _1040ScoutingtheScouts()
 	{
 		super(questId);
 	}
 
 	@Override
 	public void register()
 	{
 		qe.addQuestLvlUp(questId);
 		qe.setNpcQuestData(212010).addOnKillEvent(questId);
 		qe.setNpcQuestData(204046).addOnKillEvent(questId);
 		qe.setNpcQuestData(203989).addOnTalkEvent(questId);
 		qe.setNpcQuestData(203901).addOnTalkEvent(questId);
 		qe.setNpcQuestData(204020).addOnTalkEvent(questId);
 		qe.setNpcQuestData(204024).addOnTalkEvent(questId);
 	}
 
 	@Override
 	public boolean onLvlUpEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		final QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null || qs.getStatus() != QuestStatus.LOCKED  || player.getCommonData().getLevel() < 31)
 			return false;
 
 		QuestState qs2 = player.getQuestStateList().getQuestState(1300);
 		if(qs2 == null || qs2.getStatus() != QuestStatus.COMPLITE)
 			return false;
 		qs.setStatus(QuestStatus.START);
 		updateQuestStatus(player, qs);
 		return true;
 	}
 
 	@Override
 	public boolean onKillEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null)
 			return false;
 
 		int var = qs.getQuestVarById(0);
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 
 		if(qs.getStatus() != QuestStatus.START)
 			return false;
 		if(targetId == 212010)
 		{
 			if(var > 0 && var < 4)
 			{
 				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
 				updateQuestStatus(player, qs);
 				return true;
 			}
 		}
 		else if(targetId == 204046)
 		{
 			if(var > 7 && var < 9)
 			{
 				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 36));
 				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
 				updateQuestStatus(player, qs);
 				return true;
 			}
 		}
 		return false;
 	}	
 	
 	@Override
 	public boolean onDialogEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		final QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null)
 			return false;
 
 		int var = qs.getQuestVarById(0);
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 
 		if(qs.getStatus() == QuestStatus.REWARD)
 		{
 			if(targetId == 203989)
 					return defaultQuestEndDialog(env);
 		}
 		else if(qs.getStatus() != QuestStatus.START)
 		{
 			return false;
 		}
 		if(targetId == 203989)
 		{
 			switch(env.getDialogId())
 			{
 				case 25:
 					if(var == 0)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
 					else if(var == 4)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);					
 					return false;
 					
 				case 1013:
 					if(var == 0)
 						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 183));
 					return false;	
 				case 10000:
 					if(var == 0)
 					{
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 				case 10001:
 					if(var == 4)
 					{
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 					return false;					
 			}
 		}
 		else if(targetId == 203901)
 		{
 			switch(env.getDialogId())
 			{
 				case 25:
 					if(var == 5)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
 					return false;
 
 				case 10002:
 					if(var == 5)
 					{
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 					return false;
 			}
 		}
 		else if(targetId == 204020)
 		{
 			switch(env.getDialogId())
 			{
 				case 25:
 					if(var == 6)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
 					else if(var == 10)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);						
 					return false;
 
 				case 10003:
 					if(var == 6)
 					{
 						teleportService.teleportTo(player, 210020000, 2211, 811, 513, 0);
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						return true;
 
 					}
 				case 10006:
 					if(var == 10)
 					{
 						qs.setStatus(QuestStatus.REWARD);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}					
 					return false;
 			}
 		}
 		else if(targetId == 204024)
 		{
 			switch(env.getDialogId())
 			{
 				case 25:
 					if(var == 7)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
 					else if(var == 9)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);						
 					return false;
 
 				case 10004:
 					if(var == 7)
 					{
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 				case 10005:
 					if(var == 9)
 					{
 						teleportService.teleportTo(player, 210020000, 1606, 1529, 318, 0);
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						return true;
 					}					
 					return false;
 			}
 		}
 
 		return false;
 	}
 }
