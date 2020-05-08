 package individual;
 
 import genotype.Genome;
 import genotype.Genotype;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.FailureException;
 import jade.domain.FIPAAgentManagement.NotUnderstoodException;
 import jade.domain.FIPAAgentManagement.SearchConstraints;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.lang.acl.UnreadableException;
 
 import java.io.NotActiveException;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import settings.ViabilityPair;
 
 public class Individual extends Agent implements Serializable{
 
 	private static final long serialVersionUID = 1L;
 	
 	private Genotype myGenotype;
 	protected int age;
 	
 	protected AID myZone;
 	
 	private final static int maxDFRequests = 20;
 	
 	ArrayList<ViabilityPair> uSettings;
 	
 	@Override
 	protected void setup() {
 		Object[] args = getArguments();
 		if (args.length < 3)
 			return;
 		myGenotype = (Genotype) args[0];
 		age = (Integer) args[1];
 		myZone = (AID) args[2];
 
 		GetSettings();
 		BehaviourRegister();
 	}
 	public AID getAID_Zone(){
 		return myZone;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void GetSettings() {
 		DFAgentDescription template = new DFAgentDescription();
   		ServiceDescription templateSd = new ServiceDescription();
   		templateSd.setType("Settings");
   		template.addServices(templateSd);
   		
   		SearchConstraints sc = new SearchConstraints();
   		// We want to receive 1 result
   		sc.setMaxResults(new Long(1));
   		
   		try {
   			DFAgentDescription[] results = null;
   			int DFRequestsCounter = 0;
   			while(results == null || results.length < 1) {  					
   				try {
   					results = DFService.search(this, template, sc);
   				}
   				catch(FailureException e) {
   					DFRequestsCounter++;
   	  				if(DFRequestsCounter > maxDFRequests) {
   	  					e.printStackTrace();
   	  					throw new NotActiveException("Cannot get DF service, " + maxDFRequests + " attempts");
   	  				}
   					
   					//System.err.println("DF search exception #" + DFRequestsCounter);
   					if (results == null || results.length < 1)
   						doWait(1000);
   				}
   			}
   			DFAgentDescription dfd = results[0];
   			AID provider = dfd.getName();
   			
   			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
 			msg.addReceiver(provider);
 			try {
 				msg.setContentObject(myGenotype);
 			}
 			catch (Exception ex) { ex.printStackTrace(); }
 			
			ACLMessage response = null;
 			int DFMsgResponses = 0;
 			do {
 				DFMsgResponses++;
   				if(DFMsgResponses > maxDFRequests)
   					throw new NotActiveException("Unexpected behaviour of DF, " + maxDFRequests + " attempts");
   				
  				send(msg);
  				response = blockingReceive(MessageTemplate.MatchSender( provider ));
 			}
			while(response.getPerformative() == ACLMessage.FAILURE);
 			
			if(response.getPerformative() != ACLMessage.CONFIRM) {
 				throw new NotUnderstoodException("Not understood");
 			}
 			
			uSettings = (ArrayList<ViabilityPair>) response.getContentObject();
 			
 		} catch (FIPAException e) {
 			e.printStackTrace();
 		} catch (UnreadableException e) {
 			e.printStackTrace();
 		} catch(NotActiveException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected Float getSetting(settings.Vocabulary.Param param) {
 		for(settings.ViabilityPair pair : uSettings) {
 			if(pair.getParam() == param) return pair.getValue();
 		}
 		return 0f;
 	}
 	
 	private void BehaviourRegister() {
 		if (myGenotype.getGender() == Genome.X)
 			addBehaviour(new FemaleBehaviour());
 		else addBehaviour(new MaleBehaviour());
 	}
 	
 
 	public void changeZone(AID aid){
 		myZone = aid;
 	}
 	
 	public Genotype getGenotype(){
 		return myGenotype;
 	}
 	
 	public int getAge(){
 		return age;
 	}
 }
