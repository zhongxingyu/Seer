 package starter;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Vector;
 
 import distribution.*;
 import experiment.Scenario;
 
 import jade.core.AID;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.wrapper.AgentController;
 import jade.wrapper.ContainerController;
 import jade.wrapper.StaleProxyException;
 
 
 public class Initiator extends OneShotBehaviour {
 
 	private static final long serialVersionUID = 1L;
 	
 	// TODO
 
 	private int iterationNum;
 	private String source = new String();
 	protected int years;
 	protected ExperimentDistribution exp = new ExperimentDistribution();
 	protected Scenario scenario = new Scenario();
 	
	Vector<AID> experimentAIDs;
 	
 	public Initiator()
 	{
 		super();
 		this.source = "src/starter/Initiation.hpsi";
 	}
 	
 	public Initiator(Scenario args)
 	{
 		super();
 		this.scenario = args;
 	}
 	
 	public Initiator(String Source)
 	{
 		super();
 		this.source = Source;
 	}
 	
 	public Initiator(Scenario scenario, String source)
 	{
 		super();
 		this.scenario = scenario;
 		this.source = source;
 	}
 	
 	public void action()
 	{
 			BufferedReader in = null;
 			try {
 				in = new BufferedReader(new FileReader(source));
 			} catch (FileNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
 			try
 			{
 				years = Integer.parseInt(in.readLine());
 				iterationNum = Integer.parseInt(in.readLine());			// number of distribution units
 				String res = new String();
 				String c = new String(in.readLine());
 				while(c != null){
 					res += c;
 					c = in.readLine();
 				}
 				
 				exp = ExperimentDistribution.parseExperiment(res);
 			}
 			catch(NumberFormatException e)
 			{
 				System.out.println("Incorrect file format");
 				System.exit(0);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 
 			//System.out.println(years + " " + iterationNum);
 			startExperiments(createExperiments());
 	}
 	
 	
 	private Vector<AgentController> createExperiments(){
 		ContainerController controller = this.myAgent.getContainerController();
 		Vector<AgentController> experimentAgents = new Vector<AgentController>();
 		for (int i = 0; i < iterationNum; i++) {
 			try {
 				experimentAgents.add(
 						controller.createNewAgent(
 								getExperimentName(i), 
 								"experiment.Experiment", 
 								new Object[]{exp, scenario, years}));			// agent created
 			} catch (StaleProxyException e) {
 				e.printStackTrace();// TODO Auto-generated catch block
 			}
 			experimentAIDs.add(new AID(getExperimentName(i), AID.ISLOCALNAME));		// agent ID saved to private list
 
 		}
 		return experimentAgents;
 	}
 	
 	private void startExperiments(Vector<AgentController> experimentAgents){
 		for (AgentController agent : experimentAgents){
 			try {
 				agent.start();									// agent behaviors started
 			} catch (StaleProxyException e) {
 				e.printStackTrace();// TODO Auto-generated catch block
 			}
 		}
 	}
 	
 	AID getExperimentAID(int experimentNumber){
 		return experimentAIDs.get(experimentNumber);					// if invalid zoneNumber, then ignore it.		
 	}
 	
 	private String getExperimentName(int i){
 		return "" + myAgent.getLocalName() + "_Experiment_" + i;
 	}
 
 }
