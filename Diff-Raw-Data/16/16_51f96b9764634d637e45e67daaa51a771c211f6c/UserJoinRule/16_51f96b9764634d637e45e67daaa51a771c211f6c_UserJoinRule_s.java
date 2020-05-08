 package de.fernuni.pi3.interactionmanager.rules.sensingeventmanager;
 
 import org.springframework.stereotype.Service;
 
 import de.fernuni.pi3.interactionmanager.Event;
 import de.fernuni.pi3.interactionmanager.InstanceVars;
 
 @Service
 public class UserJoinRule extends AbstractSensingEventManagerRule {
 
 	@Override
 	public int getIndex() {
 		return 8;
 	}
 
 	@Override
 	protected boolean ruleCondition(Event in, Event out, InstanceVars var) {
		return (in.getName().equals("participant") && "userJoin".equals(in.getCustomVar("eventType")));
 	}
 
 	@Override
 	protected void ruleBody(Event in, Event out, InstanceVars var) {
 		incrVar(var, "PARTICIPANT_COUNT");
 	}
 }
