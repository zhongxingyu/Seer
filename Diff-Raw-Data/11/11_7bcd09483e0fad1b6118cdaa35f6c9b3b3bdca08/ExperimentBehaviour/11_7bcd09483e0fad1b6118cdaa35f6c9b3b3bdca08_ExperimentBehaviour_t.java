 package experiment;
 
 import individual.IndividualsManagerDispatcher;
 import jade.core.AID;
 import jade.core.behaviours.Behaviour;
 import jade.lang.acl.ACLMessage;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import messaging.Messaging;
 
 public class ExperimentBehaviour extends Behaviour implements Messaging {
 
 	private static final long serialVersionUID = 1L;
 	
 	private ACLMessage iFinished;
 	Experiment experiment;
 	private int countOfMessages;			// shit-code	* see another shit-code
 	int yearCursore;
 	
 
 	@Override
 	public void onStart(){
 		experiment = (Experiment)myAgent;
 		experiment.scenario.start();
 		iFinished = new ACLMessage(ACLMessage.INFORM);
 		iFinished.addReceiver(experiment.myProvider);
 		iFinished.setLanguage(I_FINISHED);
 		yearCursore = 0;
 	}
 
 	@Override
 	public void action() {
 		int capacityOfPull = IndividualsManagerDispatcher.getCapacityOfPull();
		System.out.println("YEAR NUMBER\t" + yearCursore + "\tSTARTED IN\tEXPERIMENT_" + experiment.experimentNumber);/*#*/
 		dieProcessing();
 		try {
 			scenarioCommandsProcessing();
 		} catch (IOException e) {e.printStackTrace();}
 		moveProcessing();
 		lastPhaseProcessing();
		System.out.println((capacityOfPull!=-1)?("  Capacity of individuals pull: " + capacityOfPull):"");
 		yearCursore++;
 	}
 
 	@Override
 	public boolean done() {
 		if (yearCursore < experiment.numberOfModelingYears)
 			return false;
 		return true;
 	}
 	
 	@Override
 	public int onEnd() {
 		ACLMessage message = getMessageForMassMailing();
 		message.setLanguage(I_KILL_YOU);
 		experiment.send(message);
 		killMySelf();
 		experiment.send(iFinished);
 		return 0;
 	}
 	
 	// method for reading scenario commands
 	private ACLMessage[] convertCommandsToACLMessages(Vector<Action> commands) throws IOException{
 		// TODO
 		countOfMessages=0;
 		int i=0;
 		ACLMessage[] messages = new ACLMessage[commands.size()];
 		for (Action command : commands){							// TODO merge it with dieOff tick 
 																	// TODO send to Zone array of ZoneCommand
 			countOfMessages += command.zonesNumbers.size();
 			messages[i] = new ACLMessage(ACLMessage.REQUEST);
 			messages[i].setLanguage(SCENARIO);
 			for (int j=0; j<command.zonesNumbers.size(); j++)
 				messages[i].addReceiver(experiment.getZoneAID(command.zonesNumbers.get(i)));
 			messages[i].setContentObject(command.command);
 			// may be should be append...
 		}
 		return messages;
 	}
 
 	private void scenarioCommandsProcessing() throws IOException{
 		Vector<Action> actions = experiment.scenario.getCommandsForNextYear(yearCursore);
 		if (actions.size() == 0)
 			return;
 		ACLMessage[] commands 
 					= convertCommandsToACLMessages(actions);
 		for (ACLMessage command : commands)				// send commands
 			experiment.send(command);
 		ignoreNMessages(countOfMessages);
 	}
 
 	private void dieProcessing(){
 		ACLMessage message = getMessageForMassMailing();
 		message.setLanguage(START_DIE);
 		experiment.send(message);
 		ignoreNMessages(experiment.zonesAIDs.size());
 	}
 
 	private void moveProcessing(){
 		ACLMessage message = getMessageForMassMailing();
 		message.setLanguage(START_MOVE);
 		experiment.send(message);
 		// TODO listening of migration requests
 		ignoreNMessages(experiment.zonesAIDs.size());
 	}
 
 	private void lastPhaseProcessing(){
 		ACLMessage message = getMessageForMassMailing();
 		message.setLanguage(START_LAST_PHASE);
 		experiment.send(message);
 		ignoreNMessages(experiment.zonesAIDs.size());
 	}
 	
 	private ACLMessage getMessageForMassMailing(){
 		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
 		for (AID zoneAID : experiment.zonesAIDs)
 			message.addReceiver(zoneAID);
 		return message;
 	}
 	
 	private void ignoreNMessages(int N){
 		for (int i=0; i<N; i++)				// waiting for reports 	!!!!BAD CODE! 
 			myAgent.blockingReceive();
 	}
 	
 	void killMySelf(){
 		experiment.doDelete();
 	}
 }
