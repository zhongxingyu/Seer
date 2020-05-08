 package zone;
 
 import genotype.Genotype;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import statistic.GenotypeAgeDistribution;
 import statistic.GenotypeAgeNumberTrio;
 import statistic.StatisticPackage;
 
 import messaging.Messaging;
 
 import jade.core.AID;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.UnreadableException;
 
 public class ZoneBehaviour extends CyclicBehaviour implements Messaging{
 
 	private static final long serialVersionUID = 1L;
 	
 	private Zone myZone;
 	
 	private StatisticPackage currentPackage;
 	
	ZoneBehaviour(){
 		myZone = (Zone)myAgent;
 		
 	}
 	
 	@Override
 	public void action() {
 		String message = getMessageContent();
 		if (message.compareTo(SCENARIO_COMMANDS) == 0){
 			// scenarioCommandProcessing(); TODO Realise scenario process
 			currentPackage = createStatisticPackage();
 			myZone.iteration++;
 		}
 		else if (message.compareTo(START_DIE) == 0){
 			dieProcessing();
 		}
 		else if (message.compareTo(START_MOVE) == 0){
 			// moveProcessing(); TODO Realise move process
 		}
 		else if (message.compareTo(START_LAST_PHASE) == 0){
 			// lastPhaseProcessing(); TODO Realise last phase process
 			// TODO after all operations we have to send package to statistic Dispatcher (or Experiment) !!!!!!!
 		}
 		else if (message.compareTo(I_KILL_YOU) == 0){
 			killingSystemProcessing();
 		}
 		
 
 	}
 
 	private void scenarioCommandProcessing() {
 		//String message = getMessageContent();
 		// TODO 
 	}
 	
 	private void dieProcessing() {
 		sendMessageToIndividuals(START_DIE, ACLMessage.INFORM);
 		getAnswersOnDieMessage();
 	}
 
 	private void getAnswersOnDieMessage() {
 		ACLMessage message;
 		int individualCounter = myZone.getIndividualsNumber();
 		for (int i = individualCounter; i > 0; i--){	//warning
 			message = getMessage();
 			if (message.getContent().compareTo(YES) == 0){
 				killIndividual(message.getSender());
 			}
 		}
 	}
 
 	private void killIndividual(AID individual) {
 		myZone.males.remove(individual);
 		myZone.females.remove(individual);
 		myZone.immatures.remove(individual);
 	}
 
 	private void moveProcessing() {
 		sendMessageToIndividuals(START_MOVE, ACLMessage.INFORM);
 		// TODO
 	}
 	
 	private void lastPhaseProcessing() {
 		// TODO Auto-generated method stub
 	}
 	
 	private void killingSystemProcessing() {
 		sendMessageToIndividuals(I_KILL_YOU, ACLMessage.INFORM);
 		killMyself();
 	}
 	
 	private void killMyself() {
 		myAgent.doDelete();
 	}
 
 	private ACLMessage getMessage(){
 		return myAgent.blockingReceive();
 	}
 	
 	private String getMessageContent(){
 		ACLMessage message = myAgent.blockingReceive();
 		return message.getContent();
 	}	
 	
 	private void sendMessageToIndividuals(String message, int performative) {
 		for (AID individual : myZone.getIndividuals()){
 			sendMessage(individual, message, performative);
 		}
 	}
 	
 	private void sendMessage(AID individual, String messageContent, int performative) {
 		try {
 			ACLMessage message = new ACLMessage(performative);
 			message.setContentObject(messageContent);
 			message.addReceiver(individual);
 			myAgent.send(message);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 	}
 
 	private StatisticPackage createStatisticPackage(){
 		int experimentId = myZone.experimentId;
 		int zoneId = myZone.zoneId;
 		int iterationId = myZone.iteration;
 		GenotypeAgeDistribution gad = createGAD();
 		StatisticPackage statisticPackage = new StatisticPackage(experimentId, zoneId, iterationId, gad);
 		return statisticPackage;
 	}
 
 	private GenotypeAgeDistribution createGAD() {
 		GenotypeAgeDistribution gad = new GenotypeAgeDistribution();
 		Vector<AID> individuals = myZone.getIndividuals();
 		for (AID individualAID : individuals){
 			int age = getIndividualAge(individualAID);
 			int genotype = getIndividualGenotype(individualAID);
 			gad.addToGant(genotype, age);
 		}
 		return gad;
 	}
 
 	private int getIndividualAge(AID individualAID) {
 		sendMessage(individualAID, GIVE_ME_YOUR_AGE, ACLMessage.REQUEST);
 		String messageContent = getMessage().getContent();
 		int age = Integer.parseInt(messageContent);
 		return age;
 	}	
 	
 	private int getIndividualGenotype(AID individualAID) {	
 		int genotypeId = -1;
 		try {
 			sendMessage(individualAID, GIVE_ME_YOUR_GENOTYPE, ACLMessage.REQUEST);
 			Genotype messageContent  = (Genotype)getMessage().getContentObject();
 			genotypeId = Genotype.getIdOf(messageContent);
 		} catch (UnreadableException e) {
 			e.printStackTrace();
 		}
 		return genotypeId;
 	}
 }
