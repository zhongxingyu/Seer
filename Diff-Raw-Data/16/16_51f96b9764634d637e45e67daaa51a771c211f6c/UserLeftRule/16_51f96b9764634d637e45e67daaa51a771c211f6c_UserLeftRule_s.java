 package de.fernuni.pi3.interactionmanager.rules.sensingeventmanager;
 
 import org.springframework.stereotype.Service;
 
 import de.fernuni.pi3.interactionmanager.Event;
 import de.fernuni.pi3.interactionmanager.InstanceVars;
 
 @Service
 public class UserLeftRule extends AbstractSensingEventManagerRule {
 
 	@Override
 	public int getIndex() {
 		return 9;
 	}
 
 	@Override
 	protected boolean ruleCondition(Event in, Event out, InstanceVars var) {
		return (in.getName().equals("participant") && "userLeft".equals(in.getCustomVar("eventType")));
 	}
 
 	@Override
 	protected void ruleBody(Event in, Event out, InstanceVars var) {
 		decrVar(var, "PARTICIPANT_COUNT");
 	}
 }
