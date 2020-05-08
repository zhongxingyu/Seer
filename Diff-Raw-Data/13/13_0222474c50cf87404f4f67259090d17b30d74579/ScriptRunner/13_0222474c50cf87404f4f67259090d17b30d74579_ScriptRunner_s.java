 package com.soartech.bolt.testing;
 
 import java.util.List;
 
 import edu.umich.sbolt.ChatFrame;
 
 public class ScriptRunner extends Thread {
 	private Script script;
 	private List<String> chatMessages;
 	
 	public ScriptRunner(Script script, List<String> chatMessages) {
 		this.script = script;
 		this.chatMessages = chatMessages;
 	}
 
 	@Override
 	public void run() {
 		if(script == null) {
     		ChatFrame.Singleton().addMessage("No script loaded!");
     		return;
     	}
     	if(!script.hasNextAction()) {
     		ChatFrame.Singleton().addMessage("Script finished.");
     		return;
     	}
     	
     	String observed;
     	if(chatMessages.size() > 0) {
     		observed = chatMessages.get(chatMessages.size()-1);
     	} else {
     		observed = "";
     	}
     	
     	Action next = script.getNextAction();
     	
     	if(next.getType() == ActionType.Mentor) {
     		if(Settings.getInstance().isAutomated()) {
     			ChatFrame.Singleton().addMessage("Mentor: " + next.getAction(), ActionType.Mentor);
     	        ChatFrame.Singleton().sendSoarMessage(next.getAction());
     			Util.handleNextScriptAction(script, chatMessages);
     			return;
     		}
     		ChatFrame.Singleton().preSetMentorMessage(next.getAction());
     	}
     	if(next.getType() == ActionType.Agent) {
     		//check if response is correct
     		String expected = next.getAction();
     		if(!observed.contains("Agent:")) {
     			script.insertFirstAction(next);
     			return;
     		}
     		ChatFrame.Singleton().setWaiting(false);
     		if(!observed.contains(expected)) {
     			ChatFrame.Singleton().addMessage("    - Error - Expected: "+expected, ActionType.Incorrect);
     		} else {
     			ChatFrame.Singleton().addMessage("    - Correct -", ActionType.Correct);
     		}
     		if(observed.contains("Okay") && Settings.getInstance().isAutomated()) {
     			synchronized(this) {
     				try {
 						this.wait(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
     			}
     		}
     	}
     	if(next.getType() == ActionType.Comment) {
     		ChatFrame.Singleton().addMessage("Comment: "+next.getAction(), next.getType());
     	}
     	if(next.getType() == ActionType.AgentAction) {
     		ChatFrame.Singleton().addMessage("AgentAction: "+next.getAction(), next.getType());
     	}
     	if(next.getType() == ActionType.MentorAction) {
     		ChatFrame.Singleton().addMessage("MentorAction: "+next.getAction(), next.getType());
     	}
     	if(!script.actionRequiresMentorAttention(next))
     		Util.handleNextScriptAction(script, chatMessages);
     	else
     		ChatFrame.Singleton().setWaitingForScript(true);
 	}
 
 }
