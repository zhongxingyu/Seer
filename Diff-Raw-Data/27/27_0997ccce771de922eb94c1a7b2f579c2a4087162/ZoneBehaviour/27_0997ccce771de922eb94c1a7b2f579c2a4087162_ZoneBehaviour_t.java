 package zone;
 
 import jade.core.AID;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.UnreadableException;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import messaging.Messaging;
 import statistic.GenotypeAgeDistribution;
 import statistic.StatisticPackage;
 import experiment.ZoneCommand;
 import genotype.Genotype;
 
 
 
 public class ZoneBehaviour extends CyclicBehaviour implements Messaging{
 
 	private static final long serialVersionUID = 1L;
 	
 	private Zone myZone;
 	
 	private StatisticPackage currentPackage;
 	
 	protected Migration migrationExecutor = null; 
 	protected ScenarioExecutor scenarioExecutor = null;
 	
 	@Override
 	public void onStart(){
 		myZone = (Zone)myAgent;
 		migrationExecutor = new Migration(myZone);
 		scenarioExecutor = new ScenarioExecutor(myZone);
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
 			ACLMessage reply = message.createReply();
 			if (language.compareTo(START_DIE) == 0){
				myZone.iteration++;
 				refreshStatistic(); // TODO  !!!
 				dieProcessing();
 			}
 			else if(language.compareTo(START_MOVE) == 0){
 				moveProcessing();
 			}
 			else if(language.compareTo(SCENARIO) == 0){
 				scenarioCommandProcessing(message);
 			}
 			else if (language.compareTo(START_LAST_PHASE) == 0){
 				lastPhaseProcessing();
 				// TODO after all operations we have to send package to statistic Dispatcher (or Experiment) !!!!!!!
 			}
 			else if (language.compareTo(I_KILL_YOU) == 0){
 				killingSystemProcessing();
 			}
 			else if (language.compareTo(MIGRATION) == 0){
 				try{
 					Object individualParams[] = (Object[])message.getContentObject();
 					myZone.addIndividualToList((String)individualParams[0], (Genotype)individualParams[1], (Integer)individualParams[2]);
 				}
 				catch(UnreadableException e){
 					System.out.println("Unknown parameters for immigrating individuals");
 					e.printStackTrace();
 				}
 			}
 			myZone.send(reply);/*#*/
 		}
 	}
 
 	private void scenarioCommandProcessing(ACLMessage message) {
 		try {
 			scenarioExecutor.action((ZoneCommand)message.getContentObject());
 		} catch (UnreadableException e) {
 			System.out.println("getting content object error");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error command executing");
 			e.printStackTrace();
 		}
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
 			try{
 				if (message.getContent().equals(YES)/*#*/){
 					myZone.killIndividual(message.getSender());
 				}
 			}
 			catch(NullPointerException e){
 				System.out.println(message.getLanguage() + " " + message.getSender());
 				e.printStackTrace();
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
 	// LAO: WHY unnecessary? DMY, Implement IT, please
 	private void moveProcessing() {
 		//#LAO sendMessageToIndividuals(START_MOVE, ACLMessage.INFORM);
 		// TODO
 		/*#LAO
 		Object[] traveller;
 		try {
 			traveller = (Object[])message.getContentObject();
 			//traveller.changeZone(myZone.getAID());
 			myZone.addIndividualToList((String)traveller[0], (Genotype)traveller[1], (Integer)traveller[2]);
 			// refreshStatistic();
 			myZone.iteration++;
 		} catch (UnreadableException e) {
 			e.printStackTrace();
 		}*/
 		migrationExecutor.action(null);
 	}
 	
 	
 	private void lastPhaseProcessing() {
 		// TODO implement last phase process
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
		/*#LAO: you can take it with one Message object. >>Anton, remove it.
		 for (AID individual : myZone.getIndividuals()){
 			sendMessage(individual, message, performative);
 		}
		*/
		ACLMessage aclMessage = new ACLMessage(performative);
		for (AID individual : myZone.getIndividuals())
			aclMessage.addReceiver(individual);
		aclMessage.setLanguage(message);
		myAgent.send(aclMessage);
 	}

	/*#LAO: >>Anton, remove it.
 	private void sendMessage(AID individual, String messageLanguage, int performative) {
 		ACLMessage message = new ACLMessage(performative);
 		message.setLanguage(messageLanguage);
 		message.addReceiver(individual);
 		myAgent.send(message);		
	}*/
 
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
