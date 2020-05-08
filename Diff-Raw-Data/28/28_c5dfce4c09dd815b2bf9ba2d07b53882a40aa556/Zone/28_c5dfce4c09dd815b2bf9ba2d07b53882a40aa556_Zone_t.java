 package zone;
 
 import genotype.Genome;
 import genotype.Genotype;
 
 import java.util.Vector;
 
 import distribution.GenotypeAgeNumberTrio;
 import distribution.ZoneDistribution;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.wrapper.AgentController;
 import jade.wrapper.ContainerController;
 import jade.wrapper.StaleProxyException;
 
 public class Zone extends Agent {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final String INDIVIDUAL_CLASS_PATH = "individual.Individual";
 	
	Vector<AID> males = new Vector<AID>();
	Vector<AID> females = new Vector<AID>();;
	Vector<AID> immatures = new Vector<AID>();;
	Vector<AID> yarlings = new Vector<AID>();;
	
	int experimentId;
	int zoneId;
 	
 	int resources;
 	int iteration = -1;
 	
 	private int individualCounter = 0;
 	
 	
 	// private Vector<Individual> strangers;
 	
 	@Override
 	protected void setup(){
 		ZoneDistribution zoneDistribution = (ZoneDistribution)getArguments()[0];
		experimentId = (Integer)getArguments()[1];
		zoneId = (Integer)getArguments()[2];
		
 		createIndividuals(zoneDistribution);
 		individualCounter = 0;
 		addBehaviour(new ZoneBehaviour());
 	}
 	
 	private void createIndividuals(ZoneDistribution zoneDistribution) {
 		Vector<GenotypeAgeNumberTrio> gants = zoneDistribution.getGenotypeDistributions();
 		for (GenotypeAgeNumberTrio gant : gants){
 			createIndividualsByGant(gant);
 		}
 	}
 
 	private void createIndividualsByGant(GenotypeAgeNumberTrio gant) {
 		for (int i = 0; i < gant.getNumber(); i++){
 			createIndividual(gant.getGenotype(), gant.getAge());
 		}
 	}
 
 	private void createIndividual(Genotype genotype, int age) {
 		try {	
 			Object[] parameters = {genotype, age, getAttractivness()};
 			String agentName = getIndividualName();
 			ContainerController controller = getContainerController();
 			AgentController individualAgent = controller.createNewAgent(agentName, 
 																		INDIVIDUAL_CLASS_PATH, 
 																		parameters);
 			individualAgent.start();
 			addIndividualToList(agentName, genotype, age);
 		} catch (StaleProxyException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void addIndividualToList(String agentName, Genotype genotype, int age) {
 		if (age < 2) {		// TODO release check immature age from settings !!!
 			immatures.add(new AID(agentName, AID.ISLOCALNAME));
 		}
 		else {
 			if (genotype.getGender() == Genome.X){
 				males.add(new AID(agentName, AID.ISLOCALNAME));
 			}
 			else {
 				females.add(new AID(agentName, AID.ISLOCALNAME));
 			}
 		}
 		
 	}
 
 	private String getIndividualName(){
 		return "" + getLocalName() + "_Individual_" + individualCounter++;
 	}
 	
 	int getIndividualsNumber(){
 		return males.size() + females.size() + immatures.size();
 	}
 	
 	float getAttractivness(){
 		float attractivness = (float) resources / (float) getIndividualsNumber();
 		return attractivness;
 	}
 	
 	Vector<AID> getIndividuals(){
 		Vector<AID> individuals = new Vector<AID>(getIndividualsNumber());
 		individuals.addAll(males);
 		individuals.addAll(females);
 		individuals.addAll(immatures);
 		return individuals;
 	}
 }
