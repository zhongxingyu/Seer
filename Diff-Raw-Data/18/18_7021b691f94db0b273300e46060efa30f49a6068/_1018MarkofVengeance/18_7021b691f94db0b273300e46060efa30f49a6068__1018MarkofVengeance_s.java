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
package quest.veteron;
 
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
 
 /**
  * @author Rhys2002 + MrPoke for Original code i massacred..
  * 
  */
 public class _1018MarkofVengeance extends QuestHandler
 {
 	private final static int questId = 1018;
 
 	public _1018MarkofVengeance()
 	{
 		super(questId);
 	}
 
 	@Override
 	public void register()
 	{
 		qe.addQuestLvlUp(questId);
 		qe.setNpcQuestData(203098).addOnTalkEvent(questId);
 	}
 
 	@Override
 	public boolean onLvlUpEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null || player.getCommonData().getLevel() < 13 || qs.getStatus() != QuestStatus.LOCKED)
 			return false;
 		qs.setStatus(QuestStatus.START);
 		updateQuestStatus(player, qs);
 		return true;
 	}
 
 	@Override
 	public boolean onDialogEvent(QuestEnv env)
 	{
 		 Player player = env.getPlayer();
 		 QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null)
 			return false;
 
 		int var = qs.getQuestVarById(0);
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 		
 		if(targetId == 203098 && qs.getStatus() == QuestStatus.START)
 		{
 			switch(env.getDialogId())
 			{
 				case 25:
 					if(var == 0)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
 				case 33:
 					if(collectItemCheck(env))
 						{
 						qs.setQuestVarById(0, var + 1);
 						qs.setStatus(QuestStatus.REWARD);
 						updateQuestStatus(player, qs);
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
 						}
 					else
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1097);	
 			}		
 		}
 		else if(qs.getStatus() == QuestStatus.REWARD)
 		{
 			if(targetId == 203098)
 				return defaultQuestEndDialog(env);
 		}
 		return false;		
 	}
 }
