 package com.theminequest.api.quest.event;
 
 import java.util.Collection;
 import java.util.Map;
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.platform.entity.MQPlayer;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.targeted.QuestTarget;
 
 public abstract class TargetedQuestEvent extends DelayedQuestEvent {
 	
 	private int targetID;
 	private long delayMS;
 	
 	public final void setupTarget(int targetID, long delayMS) {
 		this.targetID = targetID;
 		this.delayMS = delayMS;
 	}
 	
 	@Override
 	public final long getDelay() {
 		return delayMS;
 	}
 
 	@Override
 	public final boolean delayedConditions() {
 		return true;
 	}
 	
 	@Override
 	public final CompleteStatus action() {
 		Map<Integer, QuestTarget> targetMap = getQuest().getDetails().getProperty(QuestDetails.QUEST_TARGET);
 		if (!targetMap.containsKey(targetID))
			throw new RuntimeException("No such target ID " + targetID + "...");
 		QuestTarget t = targetMap.get(targetID);
 		return targetAction(t.getPlayers(getQuest()));
 	}
 	
 	public abstract CompleteStatus targetAction(Collection<MQPlayer> entities);
 	
 }
