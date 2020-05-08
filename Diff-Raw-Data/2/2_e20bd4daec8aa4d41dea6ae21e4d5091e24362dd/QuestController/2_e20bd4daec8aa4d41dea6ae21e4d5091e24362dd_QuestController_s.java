 package net.jeebiss.questmanager.quests;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import net.aufdemrand.denizen.Denizen;
 import net.aufdemrand.denizen.npc.DenizenNPC;
 import net.aufdemrand.denizen.scripts.ScriptBuilder;
 import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
 import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
 import net.aufdemrand.denizen.scripts.requirements.RequirementsMode;
 import net.aufdemrand.denizen.utilities.DenizenAPI;
 import net.aufdemrand.denizen.utilities.debugging.dB;
 import net.jeebiss.questmanager.QuestManager;
 import net.jeebiss.questmanager.quests.QuestChapter.Status;
 
 /**
  * This class is created when the dScript parser encounters the "QUEST"
  * command.
  * 
  * @author Jeebiss
  */
 public class QuestController {
 	Denizen denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
 	ScriptBuilder scriptBuilder = DenizenAPI.getCurrentInstance().getScriptEngine().getScriptBuilder();
 	QuestManager	qm = (QuestManager) Bukkit.getPluginManager().getPlugin ("Quest Manager");
 	
 	Set<String> chapters;
 	List<String> requirements;
 	
 	public QuestController(final String scriptName, final String questName, final Player player, final DenizenNPC npc) {
 		dB.echoDebug("Creating a new controller for " + scriptName + " as " + questName);
 		if (qm == null) {
 			throw new RuntimeException ("Unable to locate the QuestManager plugin.");
 		}
 		
 		//
 		// Get list of chapters from the script.
 		//
 		if (denizen.getScripts().getString(scriptName.toUpperCase() + ".CHAPTERS") != null 
 				&& denizen.getScripts().contains(scriptName.toUpperCase() + ".CHAPTERS")) {
 			chapters = denizen.getScripts()
 					.getConfigurationSection(scriptName.toUpperCase() + ".CHAPTERS").getKeys(false);
 		} else  {
 			dB.echoDebug("...no Chapters: node found");
 			return;
 		}
 		
 		//
 		// Fetch the player's quest journal.
 		//
 		QuestJournal qj = qm.getQuestJournal(player);
 		
 		//
 		// get current chapter.
 		//
 		final String currentChapter = getChapter(scriptName, questName, qj, player);
 		if (currentChapter == null) {
 			dB.echoDebug("...could not find a valid chapter for the given quest.");
 			return;
 		}
 		
 		//
 		// Build the list of goals.
 		//
 		List<String> goals = null;
 		if (denizen.getScripts().contains((scriptName + ".Chapters." + currentChapter + ".Goals").toUpperCase())) {
 			goals = denizen.getScripts().getStringList((scriptName + ".Chapters." + currentChapter + ".Goals").toUpperCase());
 			dB.echoDebug("...Goals: acquired.");
 		} else dB.echoDebug("...no goals commands found");
 		
 		//
 		// Get the quest from the player's quest journal.  If it does not exist,
 		// then we need to create the quest in the player's quest journal.
 		//
 		Quest	quest = qj.getQuests().get(questName);
 		if (quest == null) {
 			dB.echoDebug("Creating new quest: " + questName);
 			// TODO: get quest description.
 			quest = qj.addQuest(questName, scriptName, "");
 		}
 
 		//
 		// If the chapter does not exist, then create it and added it to the quest
 		// that it belongs to in an "uncompleted' status.  If there is an
 		// introduction script, then play that script as well.
 		//
 		QuestChapter chapter = quest.getChapter(currentChapter);
 		if (chapter == null) {
 			dB.echoDebug ("Creating new chapter: " + currentChapter);
 			chapter = quest.addChapter(currentChapter, Status.STARTED);
 			chapter.addPropertyChangeListener(new PropertyChangeListener() {
 				/**
 				 * This will be called when the chapter changes its status.  We are
 				 * monitoring this event so that we know when a player completes a
 				 * quest chapter.
 				 * 
 				 * @param	propertyChangeEvent	The event.
 				 */
 		    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
 		    	//
 		    	// Get the new status of the quest.
 		    	//
 		    	QuestChapter.Status newStatus = (QuestChapter.Status)propertyChangeEvent.getNewValue ();
 		    	
 		    	//
 		    	// We're only interested in the event where a quest chapter is
 		    	// finished and that the chapter has a "Conclusion" tag.
 		    	//
 		    	if (newStatus == QuestChapter.Status.FINISHED	&&
 		    			denizen.getScripts().contains((scriptName + ".Chapters." + currentChapter + ".Conclusion").toUpperCase())) {
 						//
 						// Queue the script in the player's queue.
 						//
 						scriptBuilder.queueScriptEntries (
 								player, 
 							scriptBuilder.buildScriptEntries (
 									player, 
 									npc, 
 									denizen.getScripts().getStringList((scriptName + ".Chapters." + currentChapter + ".Conclusion").toUpperCase()), 
 									scriptName, 
 									questName), 
 							QueueType.PLAYER);
 		    	}
 		    }
 			});
 
 			//
 			// Build the goals, and if there's an introduction script, play it.
 			//
 			new GoalBuilder(player, goals, quest, chapter);
 
 			//
 			// Since this is a new quest, does this chapter have an intorduction?  If
 			// so, queue up he script.
 			//
 			if (denizen.getScripts().contains((scriptName + ".Chapters." + currentChapter + ".Introduction").toUpperCase())) {
 				dB.echoDebug("...Introduction: commands acquired.");
 				scriptBuilder.queueScriptEntries (
 					player, 
 					scriptBuilder.buildScriptEntries (
 						player, 
 						npc, 
 						denizen.getScripts().getStringList((scriptName + ".Chapters." + currentChapter + ".Introduction").toUpperCase()), 
 						scriptName, 
 						null), 
 					QueueType.PLAYER);
 
 				dB.echoDebug("...executing Introduction commands");
 			} else dB.echoDebug("...no introduction commands found");
 		}
 		
 	}
 	
 	public String getChapter(String scriptName, String questName, QuestJournal journal, Player player) {
 		//parse over chapters
 		
 		for (String chapter : chapters) {
 			
 			if (journal.isQuestFinished(questName, chapter)) {
 				continue;
 			}
 			
 			//if there is no requirement list, rrturn the current chapter
			String scriptKey = (scriptName + "." + chapter + ".REQUIREMENTS.LIST").toUpperCase();
 			if (!denizen.getScripts().contains(scriptKey)) {
 				dB.echoDebug(scriptKey + " not found.");
 				return chapter;
 			}
 			
 			//if there is a List: node, get it.
 			requirements = denizen.getScripts().getStringList(scriptKey);
 			
 			//If the list is empty, return chapter
 			if (requirements.isEmpty() || requirements == null ) {
 				dB.echoDebug(scriptKey + " had no requirements.");
 				return chapter;
 			}
 
 			//
 			// Check if chapter passes listed requirements
 			//
 			if (denizen.getScriptEngine().getRequirementChecker().check(buildReqContext(scriptName, chapter, requirements, player))) {
 				return chapter;
 			}
 		}
 		return null;
 	}
 	
 	public RequirementsContext buildReqContext (String scriptName, String chapterName, List<String> requirements, Player player) {
 		RequirementsMode mode = new RequirementsMode(denizen.getScripts().getString(scriptName.toUpperCase() 
 				+ "." + chapterName + ".REQUIREMENTS.MODE"));
 		return new RequirementsContext(mode, requirements, scriptName).attachPlayer(player);
 	}
 }
