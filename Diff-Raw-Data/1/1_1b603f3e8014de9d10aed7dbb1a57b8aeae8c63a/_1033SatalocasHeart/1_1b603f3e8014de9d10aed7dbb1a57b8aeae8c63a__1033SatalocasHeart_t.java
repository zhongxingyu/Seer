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
 import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 
 /**
  * @author Xitanium
  * 
  */
 public class _1033SatalocasHeart extends QuestHandler
 {
 
 	private final static int	questId	= 1033;
 	private final static int[]	mob_ids	= { 210799 }; //Archon Drake
 
 	public _1033SatalocasHeart()
 	{
 		super(questId);
 	}
 
 	@Override
 	public void register()
 	{
 		qe.setNpcQuestData(203900).addOnTalkEvent(questId); //Diomedes
 		qe.setNpcQuestData(203996).addOnTalkEvent(questId); //Kimeia
 		for(int mob_id : mob_ids)
 		qe.setNpcQuestData(mob_id).addOnKillEvent(questId);
 		qe.addQuestLvlUp(questId);
 	}
 	
 	@Override
 	public boolean onKillEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null || qs.getStatus() != QuestStatus.START)
 			return false;
 
 		int var = qs.getQuestVarById(0);
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 		switch(targetId)
 		{
 			case 210799:
 				if(var >= 10 && var < 11) //Archon Drake
 				{
 					qs.setQuestVarById(0, var + 1);
 					updateQuestStatus(player, qs);
 					return true;
 				}
 				
 				else if(var == 11)
 				{
 					qs.setQuestVar(11);
 					updateQuestStatus(player, qs);
 					return true;
 				}
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean onLvlUpEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null || player.getCommonData().getLevel() < 20 || qs.getStatus() != QuestStatus.LOCKED)
 			return false;
 		qs.setStatus(QuestStatus.START);
 		updateQuestStatus(player, qs);
 		return true;
 	}	
 	
 	@Override
 	public boolean onDialogEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null)
 		return false;
 		if(targetId == 203900) //Diomedes
 		{
 			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
 			{
 				if(env.getDialogId() == 25)
 					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
 				else if(env.getDialogId() == 10000)
 					{
 						qs.setQuestVar(1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject()
 						.getObjectId(), 10));
 						return true;
 					}
 				else
 					return defaultQuestStartDialog(env);
 			}
 			
 			else if(qs.getStatus() == QuestStatus.REWARD)
 			{
 				return defaultQuestEndDialog(env);
 			}
 		}
 		
 		else if(targetId == 203996) //Kimeia
 		{
 			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1)
 			{
 				if(env.getDialogId() == 25)
 					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
 				else if(env.getDialogId() == 10002)
 				{
 					qs.setQuestVar(10);
 					updateQuestStatus(player, qs);
 					PacketSendUtility
 						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 42));
 					return true;
 				}
 				else
 					return defaultQuestStartDialog(env);
 			}
 			
 			else if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 11)
 			{
 				if(env.getDialogId() == 25)
 					{
 						qs.setStatus(QuestStatus.REWARD);
 						updateQuestStatus(player, qs);
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2205);
 					}
 				else
 					return defaultQuestStartDialog(env);
 			}
 			
 		}
 		return false;
 
 	}
 }
