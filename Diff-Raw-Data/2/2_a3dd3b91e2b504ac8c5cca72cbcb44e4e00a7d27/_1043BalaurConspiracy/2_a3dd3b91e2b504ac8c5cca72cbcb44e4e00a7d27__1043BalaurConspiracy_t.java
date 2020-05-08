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
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 import com.aionemu.gameserver.services.QuestService;
 import com.aionemu.gameserver.services.TeleportService;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 import com.aionemu.gameserver.utils.ThreadPoolManager;
 import com.aionemu.gameserver.world.WorldMapInstance;
 import com.aionemu.gameserver.world.WorldMapType;
 
 /**
  * @author Balthazar
  */
  
 public class _1043BalaurConspiracy extends QuestHandler
 {
 	private final static int	questId	= 1043;
 	
 	public _1043BalaurConspiracy()
 	{
 		super(questId);
 	}
 
 	@Override
 	public void register()
 	{
 		qe.addQuestLvlUp(questId);
 		qe.setNpcQuestData(203901).addOnTalkEvent(questId);
 		qe.setNpcQuestData(204020).addOnTalkEvent(questId);
 		qe.setNpcQuestData(204044).addOnTalkEvent(questId);
 		qe.setNpcQuestData(211629).addOnKillEvent(questId);
 	}
 	
 	@Override
 	public boolean onDialogEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 			
 		if(qs == null)
 			return false;
 			
 		if(qs.getStatus() == QuestStatus.START)
 		{
 			switch(targetId)
 			{
 				case 203901:
 				{
 					switch(env.getDialogId())
 					{
 						case 25:
 						{
 							if(qs.getQuestVarById(0) == 0)
 							{
 								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
 							}
 						}
 						case 10000:
 						{
 							qs.setQuestVarById( 0, qs.getQuestVarById(0) + 1 );
 							updateQuestStatus( player, qs );
 							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 							return true;
 						}
 					}
 				}
 				case 204020:
 				{
 					switch(env.getDialogId())
 					{
 						case 25:
 						{
 							if(qs.getQuestVarById(0) == 1)
 							{
 								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
 							}
 						}
 						case 10001:
 						{
 							qs.setQuestVarById( 0, qs.getQuestVarById(0) + 1 );
 							updateQuestStatus( player, qs );
 							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 							return true;
 						}
 					}
 				}
 				case 204044:
 				{
 					switch(env.getDialogId())
 					{
 						case 25:
 						{
 							switch(qs.getQuestVarById(0))
 							{
 								case 2:
 								{
 									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
 								}
 								case 4:
 								{
 									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
 								}
 							}
 						}
 						case 10002:
 						{
 							qs.setQuestVarById( 0, qs.getQuestVarById(0) + 1 );
 							updateQuestStatus( player, qs );
 							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 							return true;
 						}
 						case 10003:
 						{
 							qs.setQuestVar(4);
 							qs.setStatus(QuestStatus.REWARD);
 							updateQuestStatus( player, qs );
 							TeleportService.teleportTo(player, WorldMapType.ELTNEN.getId(), 2502.1948f, 782.9152f, 408.97723f, 0);
 							return true;
 						}
 					}
 				}
 			}
 		}
 		else if (qs.getStatus() == QuestStatus.REWARD)
 		{
 			if (targetId == 203901)
 			{
 				switch(env.getDialogId())
 				{
 					case 25:
 					{
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
 					}
 					case 1009:
 					{
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
 					}
 					default: return defaultQuestEndDialog(env);
 				}
 			}
 		}		
 		return false;
 	}
 	
 	@Override
 	public boolean onLvlUpEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		final QuestState qs = player.getQuestStateList().getQuestState( questId );
 			
 		if( qs == null || qs.getStatus() != QuestStatus.LOCKED)
 		{
 			return false;
 		}
 		
 		int[] quests = { 1300, 1031, 1032, 1033, 1034, 1036, 1037, 1035, 1038, 1039, 1040, 1041, 1042};
 		for (int id : quests)
         {
             QuestState qs2 = player.getQuestStateList().getQuestState(id);
             if (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
                 return false;
         }
 		
 		qs.setStatus( QuestStatus.START );
 		updateQuestStatus( player, qs );
 		return true;
 	}
 	
 	@Override
 	public boolean onKillEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		
 		if(qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVarById(0) != 3)
 		{
 			return false;
 		}
 		
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 			
 		if(targetId == 211629)
 		{
 			qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
 			updateQuestStatus(player, qs);
 			return true;
 		}
 		return false;
 	}
 }
