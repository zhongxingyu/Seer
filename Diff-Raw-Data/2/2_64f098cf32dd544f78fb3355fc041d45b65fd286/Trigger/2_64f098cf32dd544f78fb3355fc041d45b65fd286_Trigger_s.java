 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.quest.trigger;
 
 import com.mahn42.anhalter42.quest.Quest;
 import com.mahn42.anhalter42.quest.QuestObject;
 import com.mahn42.anhalter42.quest.QuestPlugin;
 import com.mahn42.anhalter42.quest.action.Action;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 /**
  *
  * @author andre
  */
 public class Trigger extends QuestObject {
 
     public static void register() {
         Quest.triggerTypes.put("Trigger", Trigger.class);
         Quest.triggerTypes.put("SceneInitialized", SceneInitialized.class);
         Quest.triggerTypes.put("PlayerEnteredRegion", PlayerEnteredRegion.class);
         Quest.triggerTypes.put("TimerLapsed", TimerLapsed.class);
         Quest.triggerTypes.put("VariableReachedValue", VariableReachedValue.class);
         Quest.triggerTypes.put("PlayerCountReached", PlayerCountReached.class);
     }
 
     public String type;
     public ArrayList<Action> actions = new ArrayList<Action>();
     
     public void setActionsFromSectionValue(Object aValue) {
         if (aValue instanceof ArrayList) {
             for(Object lItem : ((ArrayList)aValue)) {
                 HashMap<String, Object> lMap = (HashMap)lItem;
                 String lType = (String) lMap.get("type");
                 Class lActionClass = Quest.actionTypes.get(lType);
                 if (lActionClass != null) {
                     try {
                         Action lAction = (Action) lActionClass.getConstructor().newInstance();
                         lAction.quest = quest;
                         lAction.fromSectionValue(lItem);
                         actions.add(lAction);
                     } catch (Exception ex) {
                         QuestPlugin.plugin.getLogger().log(Level.SEVERE, null, ex);
                     }
                 } else {
                     quest.log("unkown action type " + lType);
                 }
             }
         }
     }
 
     public void initialize() {
         for(Action lAction : actions) {
             lAction.initialize();
         }
     }
     
     public boolean check() {
         return false;
     }
     
     public void executeActions() {
         for(Action lAction : actions) {
             quest.log("action " + lAction.type + " executed.");
             lAction.execute();
         }
     }
 }
