 package com.adamki11s.dialogue;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import com.adamki11s.ai.dataset.GenericRepLevel;
 import com.adamki11s.dialogue.triggers.EndTrigger;
 import com.adamki11s.dialogue.triggers.NoTrigger;
 import com.adamki11s.dialogue.triggers.QuestTrigger;
 import com.adamki11s.dialogue.triggers.TaskTrigger;
 import com.adamki11s.dialogue.triggers.Trigger;
 import com.adamki11s.dialogue.triggers.TriggerType;
 import com.adamki11s.io.FileLocator;
import com.adamki11s.questx.QuestX;
 import com.adamki11s.sync.io.writer.SyncWriter;
 
 
 public class DLGParser {
 
 	final File dlgFile;
 	final SyncWriter io;
 
 	Conversation c;
 
 	public DLGParser(Conversation c, File dlgFile) {
 		this.c = c;
 		this.dlgFile = dlgFile;
 		this.io = new SyncWriter(dlgFile);
 	}
 
 	public synchronized DialogueSet[] parse() {
 		io.read();
 		ArrayList<String> lines = io.getReadableContents();
 		ArrayList<DialogueSet> ds = new ArrayList<DialogueSet>();
 		// Load all single dialogue items first then we can add replys
 		int nodes = 0;
 		for (String line : lines) {
 			if(line.length() < 4){
 				continue;
 			}
 			nodes++;
 			System.out.println("Reading line " + nodes + " content = " + line);
 			int firstHashIndex = line.indexOf("#") + 1;
 			int secondHashIndex = line.indexOf("#", firstHashIndex + 1);
 			String responseType = (line.substring(firstHashIndex, secondHashIndex));
 			System.out.println("RESPONSE TYPE = " + responseType);
 			if (responseType.equalsIgnoreCase("reply")) {
 				
 				System.out.println("Line " + nodes + " is a reply, skip");
 				continue;
 			} else {
 				// SingleDialogueItem Create
 				String[] parts = line.split("#");
 				String nodeID = parts[0];
 				int options = Integer.parseInt(parts[2]);
 				String[] speechOptions = new String[options];
 				String[] gTagIds = new String[options];
 				String[] trigIds = new String[options];
				QuestX.logMSG(options + " dialogue options scanned");
 				for(int i = 1; i <= options; i++){
 					speechOptions[i - 1] = parts[2 + i];
 					gTagIds[i - 1] = parts[2 + (options) + i];
 					trigIds[i - 1] = parts[2 + (options * 2) + i];
 				}
 				
 				GenericRepLevel[] gRepLevels = new GenericRepLevel[options];
 				TriggerType[] triggerTypes = new TriggerType[options];
 				Trigger[] realTriggers = new Trigger[options];
 				
 				for(int i = 1; i <= options; i++){
 					gRepLevels[i - 1] = GenericRepLevel.parseRepLevel(gTagIds[i - 1]);
 					triggerTypes[i - 1] = TriggerType.parseTriggerType(trigIds[i - 1]);
 					//dialogues[i - 1] = new SingleDialogueItem(speechOptions[i - 1], gRepLevels[i - 1], triggerTypes[i - 1]);
 				}
 				
 				for(int i = 1; i <= options; i++){
 					TriggerType tt = triggerTypes[i - 1];
 					if(tt == TriggerType.NONE){
 						realTriggers[i - 1] = new NoTrigger(tt);
 					} else if(tt == TriggerType.END){
 						realTriggers[i - 1] = new EndTrigger(tt);
 					} else if(tt == TriggerType.TASK){
 						realTriggers[i - 1] = new TaskTrigger(tt, FileLocator.getNPCTaskFile(c.getConvoData().getSimpleNpc().getName()));
 					} else {
 						realTriggers[i - 1] = new QuestTrigger(tt, FileLocator.getNPCQuestLinkFile(c.getConvoData().getSimpleNpc().getName()));
 					}
 				}
 				
 				
 				
 				//SingleDialogueItem[] dialogues = new SingleDialogueItem[options];
 				
 				/*
 				 * Load in DialogueResponse for this dialogue
 				 */
 				
 				
 				
 				DialogueResponse dlgResponse = null;
 				
 				for(String dResp : lines){
 					//public DialogueResponse(String[] responses, GenericRepLevel[] repRequired, Trigger[] triggers) {
 					if(dResp.startsWith(nodeID + "#reply")){
 						//01#reply#1#"The World of Minecraft"#a#n
 						String[] resp_parts = dResp.split("#");
 						int resp_options = Integer.parseInt(resp_parts[2]);
 						String[] resp_speechOptions = new String[resp_options];
 						
 						for(int i = 1; i <= resp_options; i++){
 							resp_speechOptions[i - 1] = resp_parts[2 + i];
 						}
 						
 						dlgResponse = new DialogueResponse(resp_speechOptions);
 						
 						break;
 						
 					} else {
 						continue;
 					}
 				}
 				
 				DialogueItem[] dialogueItems = new DialogueItem[options];
 				
 				for(int i = 1; i <= options; i++){
 					dialogueItems[i - 1] = new DialogueItem(speechOptions[i - 1],gRepLevels[i - 1], realTriggers[i - 1]);
 				}
 				
 				DialogueSet dSet = new DialogueSet(dialogueItems, dlgResponse, nodeID);
 				ds.add(dSet);
 				
 			}
 		}
 		DialogueSet[] dSetArray = new DialogueSet[nodes];
 		
 		return ds.toArray(dSetArray);
 	}
 	/*
 	 * 
 	 * FORMAT ------
 	 * DialogueSet_ID#DialogueAction#OptionsNumber#"speech1"#"speech2"
 	 * #"speech3"#GTAG1#GTAG2#GTAG3#TRIG1#TRIG2#TRIG3
 	 * 
 	 * DialogueAction : say, reply Speech : String of text Generic Tag :
 	 * e[evil], b[bad], o[ordinary], g[good], h[hero], a[any] Trigger Tag :
 	 * n[none], q[quest], t[task], e[end_convo] ------
 	 */
 
 	/*
 	 * 
 	 * 
 	 * 0#say#2#"Hello There."#"Bye!"#a,a#n,e
 	 * 
	 * 0#reply#2#"Hi there %pname%, what can i do for you?"#"See you around"
 	 * 
 	 * 
 	 * 01#say#1#"Where am I?"#a#n
 	 * 
 	 * 01#reply#1#"The World of Minecraft"
 	 * 
 	 * 011#say#2#"Ok, thanks"#"Goodbye friend"#a,a#e,e
 	 */
 
 }
