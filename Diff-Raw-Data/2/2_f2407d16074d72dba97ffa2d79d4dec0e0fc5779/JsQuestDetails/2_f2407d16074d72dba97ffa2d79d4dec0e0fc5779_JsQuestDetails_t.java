 package com.theminequest.common.quest.js;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.requirement.QuestRequirement;
 import com.theminequest.api.targeted.QuestTarget;
 import com.theminequest.common.quest.CommonQuestDetails;
 
 /**
  * JsQuestDetails works differently than most details, in that
  * when it generates a Quest, it switches the detail attributes
  * to those in a Js Object.
  */
 public class JsQuestDetails extends CommonQuestDetails {
 	
 	private static final long serialVersionUID = 7749931627674838925L;
 	
 	public static final String JS_SOURCE = "js.source";
 	
 	public static final String JS_LINESTART = "js.linestart";
 
 	public JsQuestDetails(String name) {
 		super(name);
		super.setProperty(QUEST_LOADWORLD, true);
 		
 		// from V1
 		setProperty(QuestDetails.QUEST_DISPLAYNAME, name);
 		setProperty(QuestDetails.QUEST_DESCRIPTION, _("No description provided..."));
 		setProperty(QuestDetails.QUEST_ACCEPT, _("Quest Accepted!"));
 		setProperty(QuestDetails.QUEST_ABORT, _("Quest Aborted!"));
 		setProperty(QuestDetails.QUEST_COMPLETE, _("Quest Completed!"));
 		setProperty(QuestDetails.QUEST_FAIL, _("Quest Failed!"));
 		setProperty(QuestDetails.QUEST_SPAWNRESET, true);
 		
 		double[] spawnPoint = new double[3];
 		spawnPoint[0] = 0;
 		spawnPoint[1] = 64;
 		spawnPoint[2] = 0;
 		setProperty(QuestDetails.QUEST_SPAWNPOINT, spawnPoint);
 		
 		setProperty(QuestDetails.QUEST_EDITMESSAGE, _("&7You can't edit this part of the world."));
 		setProperty(QuestDetails.QUEST_WORLD, "world");
 		
 		setProperty(QuestDetails.QUEST_TASKS, new HashMap<Integer, Integer[]>(0));
 		setProperty(QuestDetails.QUEST_EVENTS, new HashMap<Integer, String>(0));
 		
 		setProperty(QuestDetails.QUEST_WORLDFLAGS, 0);
 		setProperty(QuestDetails.QUEST_REQUIREMENTDETAILS, new HashMap<Integer, QuestRequirement>());
 		setProperty(QuestDetails.QUEST_GETREQUIREMENTS, new LinkedList<Integer>());
 		setProperty(QuestDetails.QUEST_STARTREQUIREMENTS, new LinkedList<Integer>());
 		
 		setProperty(QuestDetails.QUEST_TARGET, new HashMap<Integer, QuestTarget>());
 		
 	}
 	
 	@Override
 	public <E> E setProperty(String key, Serializable property) {
 		if (key.equals(QUEST_LOADWORLD))
 			throw new IllegalArgumentException("Cannot set LOADWORLD");
 		
 		return super.setProperty(key, property);
 	}
 
 	@Override
 	public <E> E removeProperty(String key) {
 		if (key.equals(QUEST_LOADWORLD))
 			throw new IllegalArgumentException("Cannot remove LOADWORLD");
 
 		return super.removeProperty(key);
 	}
 
 	@Override
 	public Quest generateQuest(long questId, String questOwner) {
 		return new JsQuest(questId, questOwner, this);
 	}
 	
 }
