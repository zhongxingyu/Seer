 package zone;
 
 import experiment.ZoneCommand;
 import genotype.Genotype;
 
 import individual.Individual;
 
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
 	
 	protected Migration m = null; 
 	protected ScenarioExecutor executor = null;
 	
 	@Override
 	public void onStart(){
 		myZone = (Zone)myAgent;
 		m = new Migration(myZone);
 		executor = new ScenarioExecutor(myZone);
 	}
 	
 	/*
 	 * 	Zone creates new statisticPackage at the beginning of each year
 	 *  (after getting a  SCENARIO_COMMANDS message)
 	 */
 	@Override
 	public void action() {
 		ACLMessage message = myAgent.blockingReceive();/*#*/
 		/*System.out.println("Zone " + myZone.zoneId + " in Experiment " + 
 							myZone.experimentId + " got " + message.getContent());#lao*/
 		if(message.getPerformative() == ACLMessage.REQUEST){
 			String language = message.getLanguage();
 			if(language.compareTo("immigration") == 0){
 				Object[] traveller;
 				try {
 					traveller = (Object[])message.getContentObject();
 					//traveller.changeZone(myZone.getAID());
 					myZone.addIndividualToList((String)traveller[0], (Genotype)traveller[1], (Integer)traveller[2]);
 				} catch (UnreadableException e) {
 					e.printStackTrace();
 				}
 			}
 			else if(language.compareTo(SCENARIO) == 0){
 				try {
 					executor.action((ZoneCommand)message.getContentObject());
 				} catch (UnreadableException e) {
 					System.out.println("getting content object error");
 					e.printStackTrace();
 				}
 			}
 		}
 		else{
 			//==========================================================
 			String content = message.getContent();/*#*/
 			ACLMessage reply = message.createReply();/*#*/
 			if (content.compareTo(SCENARIO_COMMANDS) == 0){
 				//refreshStatistic();
 				// scenarioCommandProcessing(); TODO Realise scenario process	
 				myZone.iteration++;
 			}
 			else if (content.compareTo(START_DIE) == 0){
 				refreshStatistic(); // TODO  !!!
 				dieProcessing();
 			}
 			else if (content.compareTo(START_MOVE) == 0){
 				// moveProcessing();
 				m.action(null);
 			}
 			else if (content.compareTo(START_LAST_PHASE) == 0){
 				// lastPhaseProcessing(); TODO Realise last phase process
 				// TODO after all operations we have to send package to statistic Dispatcher (or Experiment) !!!!!!!
 			}
 			else if (content.compareTo(I_KILL_YOU) == 0){
 				killingSystemProcessing();
 			}
 			myZone.send(reply);/*#*/
 			//==========================================================
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
 			if (message.getContent().equals(YES)/*#*/){
 				myZone.killIndividual(message.getSender());
 			}
 		}
 	}
 
 	// DMY: removed to Zone
 /*	private void killIndividual(AID individual) {
 		myZone.males.remove(individual);
 		myZone.females.remove(individual);
 		myZone.immatures.remove(individual);
 	}*/
 
 	// DMY: unnesessary now
 /*	private void moveProcessing() {
 		sendMessageToIndividuals(START_MOVE, ACLMessage.INFORM);
 		// TODO
 	}*/
 	
 	
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
 
 	private void sendMessageToIndividuals(String message, int performative) {
 		for (AID individual : myZone.getIndividuals()){
 			sendMessage(individual, message, performative);
 		}
 	}
 	
 	private void sendMessage(AID individual, String messageContent, int performative) {
 		ACLMessage message = new ACLMessage(performative);
 		message.setContent(messageContent);
 		message.addReceiver(individual);
 		myAgent.send(message);		
 	}
 
 	private void refreshStatistic() {
 		currentPackage  = createStatisticPackage();
 		sendStatisticPackage();
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
 	/* 			TODO Have to get message with individual genotype 
 		sendMessage(individualAID, GIVE_ME_YOUR_AGE, ACLMessage.REQUEST);
 		String messageContent = getMessage().getContent();
 		int age = Integer.parseInt(messageContent);
 */
 		return 88;
 	}	
 	
 	private int getIndividualGenotype(AID individualAID) {	
 		int genotypeId = -1;
 		/*try {
 		           TODO Have to get message with individual genotype
 			sendMessage(individualAID, GIVE_ME_YOUR_GENOTYPE, ACLMessage.REQUEST);
 			Genotype messageContent  = (Genotype)getMessage().getContentObject();
 			genotypeId = Genotype.getIdOf(messageContent);
 			*/
 			genotypeId = 99;
 			/*
 		} catch (UnreadableException e) {
 			e.printStackTrace();
 		}
 		*/
 		return genotypeId;
 	}	
 	
 	private void sendStatisticPackage() {		
 		try {
 			ACLMessage message = new ACLMessage(ACLMessage.INFORM);
 		//	message.setContent(STATISTIC);		#lao
 			message.setContentObject(currentPackage);		
 			message.addReceiver(myZone.statisticDispatcher);
 			myAgent.send(message);
 		//	System.out.println("Zone " + myZone.zoneId + " sent statistic");	#lao
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
