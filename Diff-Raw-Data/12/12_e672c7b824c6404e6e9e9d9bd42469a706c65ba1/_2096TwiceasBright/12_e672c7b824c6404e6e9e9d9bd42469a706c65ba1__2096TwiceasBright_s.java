 /*
  * This file is part of aion-unique <aion-unique.org>.
  *
  *  aion-unique is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-unique is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
  */
 package quest.pandaemonium;
 
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 
 /**
  * @author Manu72
  *
  */
 public class _2096TwiceasBright extends QuestHandler
 {
 
 	private final static int	questId	= 2096;
 
 	public _2096TwiceasBright()
 	{
 		super(questId);
 	}
 
 	@Override
 	public void register()
 	{
 		qe.addQuestLvlUp(questId);
 		qe.setNpcQuestData(204206).addOnQuestStart(questId); //Cavalorn
 		qe.setNpcQuestData(204206).addOnTalkEvent(questId); //Cavalorn
 		qe.setNpcQuestData(204207).addOnTalkEvent(questId); //Kasir
 		qe.setNpcQuestData(203550).addOnTalkEvent(questId); //Munin
 	}
 	
 	@Override
 	public boolean onDialogEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 		final QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(targetId == 204206) //Cavalorn
 		{
			if(qs.getStatus() == QuestStatus.START)
 			{
 				if(env.getDialogId() == 25)
 					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
 				else if(env.getDialogId() == 10000)
 					{
 						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject()
 						.getObjectId(), 10));
 						return true;
 					}
 
 				else
 					return defaultQuestStartDialog(env);
 			}
 		}
 		
 		else if(targetId == 204207) //Munin
 		{
 
 			if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1)
 			{
 				if(env.getDialogId() == 25)
 					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
 				else if(env.getDialogId() == 10001)
 				{
 					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
 					updateQuestStatus(player, qs);
 					PacketSendUtility
 						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 					return true;
 				}
 				else
 					return defaultQuestStartDialog(env);
 			}
 
 		}
 		
 		else if(targetId == 203550) //Munin
 		{
 			if(qs != null && qs.getStatus() == QuestStatus.REWARD) //Reward
 			{
 				if(env.getDialogId() == 25)
 					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
 				else if(env.getDialogId() == 1009)
 				{
 					qs.setQuestVar(3);
 					qs.setStatus(QuestStatus.REWARD);
 					updateQuestStatus(player, qs);
 					return defaultQuestEndDialog(env);
 				}
 				else
 					return defaultQuestEndDialog(env);
 			}
 		}		
 		return false;
 	}
 	@Override
         public boolean onLvlUpEvent(QuestEnv env)
         {
                 Player player = env.getPlayer();
                 QuestState qs = player.getQuestStateList().getQuestState(questId);
                 if(qs == null || qs.getStatus() != QuestStatus.LOCKED)
                         return false;
                 int[] quests = { 2007, 2022, 2041, 2094, 2061, 2076, 2900};
                 for (int id : quests)
                 {
                         QuestState qs2 = player.getQuestStateList().getQuestState(id);
                         if (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
                                 return false;
                 }
 
                 qs.setStatus(QuestStatus.START);
                 updateQuestStatus(player, qs);
                 return true;
         }
 }
