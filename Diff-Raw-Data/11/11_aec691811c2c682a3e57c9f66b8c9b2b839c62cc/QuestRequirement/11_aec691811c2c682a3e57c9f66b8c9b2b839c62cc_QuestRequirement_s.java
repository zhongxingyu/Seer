 package net.jeebiss.questmanager.denizen.requirements;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import net.aufdemrand.denizen.exceptions.RequirementCheckException;
 import net.aufdemrand.denizen.npc.DenizenNPC;
 import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
 import net.aufdemrand.denizen.utilities.arguments.aH;
 import net.aufdemrand.denizen.utilities.debugging.dB;
 import net.jeebiss.questmanager.QuestManager;
 import net.jeebiss.questmanager.quests.QuestChapter;
 import net.jeebiss.questmanager.quests.QuestJournal;
 import net.jeebiss.questmanager.quests.QuestChapter.Status;
 
 public class QuestRequirement extends AbstractRequirement{
 	private enum ScriptType { FINISHED, STARTED, FAILED }
 	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
 
 		// Requirement will check if full Quest, or specific
 		// Quest Chapter is FINISHED, STARTED, or FAILED and
 		// will be compatible with the denizen scripts. Using
 		// a hyphen will denote that the requirement NOT be met.
 		
 		QuestManager QM = (QuestManager) Bukkit.getServer().getPluginManager().getPlugin("Quest Manager");
 
 		ScriptType scriptType = ScriptType.FINISHED;
 		
 		Integer quantity = 1;
 		String questName = null;
 		String chapterName = null;
 		
 		for (String arg : args) {
 			if (aH.matchesArg("FINISHED, STARTED, FAILED", arg)) {
 				try {
 					scriptType = ScriptType.valueOf(aH.getStringFrom(arg));
 					dB.echoDebug("...set TYPE to: " + scriptType.name());
 				} catch (Exception e) {e.printStackTrace();}
 			} else if (aH.matchesQuantity(arg)) {
 				quantity = aH.getIntegerFrom(arg);
 				dB.echoDebug("...set quantity to: " + quantity);
 			} else {
 				if (arg.contains(".")) {
 					String[] path = arg.split("\\.");
 					questName = path[0];
 					chapterName = path[1];
 				} else {
 					questName = arg;
 				}
 			}
 		}
 		
 		//
 		// Fetch the player's Quest Journal.
 		//
		QuestJournal	qj = QM.getQuestJournal(player);
 		
 		//
 		// If the quest journal does not have the quest, then the quest is not
 		// started.
 		//
 		if (qj.hasQuest(questName) == false) {
 			return false;
 		}
 		
 		//
 		// If there is no chapter, but we have the quest (which would be odd), then
 		// we'll assume that the quest has been started.  This would only return
 		// true if it's checked for being started, otherwise, this returns false.
 		//
 		if (chapterName == null) {
 			return scriptType == ScriptType.STARTED;
 		}
 		
 		//
 		// If the chapter does not exist, then the quest isn't started.
 		//
 		QuestChapter	qc = qj.getQuests().get(questName).getChapter(chapterName);
 		if (qc == null) {
 			return false;
 		}
 
 		//
 		// So the quest is "at least" in a "STARTED" state.  Return based on what
 		// the script wants.
 		//
 		switch (scriptType) {
 		case STARTED:
 			return true;
 		case FINISHED:
 			return qc.getStatus() == Status.FINISHED;
 		case FAILED:
 			return qc.getStatus () == Status.FAILED;
 		}
 		
 		return false;
 	}

 }
