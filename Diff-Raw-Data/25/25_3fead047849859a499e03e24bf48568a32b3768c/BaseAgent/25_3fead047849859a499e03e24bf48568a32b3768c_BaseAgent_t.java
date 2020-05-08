 package fr.utc.nf28.moka.agents;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAException;
 
 import java.util.ArrayList;
 
 /**
  * A base agent that can register skills
  * and retrieve agents based on their skills
  */
 public class BaseAgent extends Agent {
 
	private static final String DEFAULT_TYPE = "DefaultServiceType";

 	/**
	 * Retrieve all the agents AID that have a skill
	 * that matches the name and the type
 	 *
 	 * @param skillName
 	 * @param skillType
 	 * @return an arrayList of AIDs
 	 */
	protected ArrayList<AID> getAgentsWithSkill(String skillName, String skillType) {
 		ArrayList<AID> result = new ArrayList<AID>();
 		DFAgentDescription agentDescription = new DFAgentDescription();
 		ServiceDescription serviceDescription = new ServiceDescription();
 		serviceDescription.setName(skillName);
 		serviceDescription.setType(skillType);
 		agentDescription.addServices(serviceDescription);
 
 		try {
 			DFAgentDescription[] agentDescriptions = DFService.search(this, agentDescription);
 			for(DFAgentDescription ad : agentDescriptions ) {
 				result.add(ad.getName());
 			}
 		} catch (FIPAException e) {
 			e.printStackTrace();
 		}
 
 		return result;
 
 	}
 
 	/**
	 * Register a skill with a name
 	 *
	 * @param skillName
 	 */
	protected void registerSkill(String skillName) {
		//use a default type
		// type is a mandatory field of a service-description
		registerSkill(skillName, DEFAULT_TYPE);
 	}
 
 	/**
 	 * Register a skill with a name and a type
 	 *
 	 * @param skillName
 	 * @param skillType
 	 */
	protected void registerSkill(String skillName, String skillType) {
 		DFAgentDescription agentDescription = new DFAgentDescription();
 		ServiceDescription serviceDescription = new ServiceDescription();
 		serviceDescription.setName(skillName);
 		serviceDescription.setType(skillType);
 		agentDescription.addServices(serviceDescription);
 
 		try {
 			DFService.register(this, agentDescription);
 		} catch (FIPAException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
