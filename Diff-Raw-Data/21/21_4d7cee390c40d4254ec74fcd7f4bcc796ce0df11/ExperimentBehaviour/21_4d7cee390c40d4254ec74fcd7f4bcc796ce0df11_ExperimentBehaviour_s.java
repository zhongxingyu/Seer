 package experiment;
 
 import jade.core.AID;
 import jade.core.behaviours.Behaviour;
 import jade.lang.acl.ACLMessage;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import messaging.Messaging;
 
 public class ExperimentBehaviour extends Behaviour implements Messaging {
 
 	private static final long serialVersionUID = 1L;
 	
 	Experiment experiment;
 	private int countOfMessages;		// shit-code	* see another shit-code
 	int yearCursore;
 	
	public ExperimentBehaviour(){
 		experiment = (Experiment)myAgent;
		experiment.scenario.start();
 		yearCursore = 0;
 	}
 
 	@Override
 	public void action() {
 		// TODO scenarioCommandsProcessing();
 		dieProcessing();
 		// TODO moveProcessing();
 		// TODO (in Zone) lastPhaseProcessing();
 		yearCursore++;
 	}
 
 	@Override
 	public boolean done() {
 		if (yearCursore < experiment.numberOfModelingYears)
 			return true;
 		ACLMessage message = getMessageForMassMailing();
 		message.setContent(I_KILL_YOU);
 		experiment.send(message);
 		killMySelf();
 		return false;
 	}
 	
 	// method for reading scenario commands
 	private ACLMessage[] convertCommandsToACLMessages(Vector<ExperimentCommand> commands) throws IOException{
 		// TODO
 		countOfMessages=0;
 		int i=0;
 		ACLMessage[] messages = new ACLMessage[commands.size()];
 		for (ExperimentCommand command : commands){
 			countOfMessages += command.zonesNumbers.length;
 			messages[i] = new ACLMessage(ACLMessage.REQUEST);
 			for (int j=0; j<command.zonesNumbers.length; j++)
 				messages[i].addReceiver(experiment.getZoneAID(command.zonesNumbers[i]));
 			messages[i].setContentObject(command.command);
 			// may be should be append...
 		}
 		return null;
 	}
 
 	private void scenarioCommandsProcessing() throws IOException{
 		ACLMessage[] commands 
 					= convertCommandsToACLMessages(experiment.scenario.getCommandsForNextYear(yearCursore));
 		for (ACLMessage command : commands)				// send commands
 			experiment.send(command);
 		ignoreNMessages(countOfMessages);
 	}
 
 	private void dieProcessing(){
 		ACLMessage message = getMessageForMassMailing();
 		message.setContent(START_DIE);
 		experiment.send(message);
 		ignoreNMessages(experiment.zonesAIDs.size());
 	}
 
 	private void moveProcessing(){
 		ACLMessage message = getMessageForMassMailing();
 		message.setContent(START_MOVE);
 		experiment.send(message);
 		// TODO listening of migration requests
 		//ignoreNMessages(experiment.zonesAIDs.size());
 	}
 
 	private void lastPhaseProcessing(){
 		ACLMessage message = getMessageForMassMailing();
 		message.setContent(START_LAST_PHASE);
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
