 package projet.main;
 
 import jade.core.Profile;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.wrapper.AgentContainer;
 import jade.wrapper.AgentController;
 import jade.wrapper.StaleProxyException;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import knowledgeBase.KBMachine;
 import knowledgeBase.QueryKnowledgeBase;
 import edu.uci.ics.jung.graph.Graph;
 
 public class MainClass {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		
 		try {
 			Runtime rt = Runtime.instance();
 			Profile p = new ProfileImpl("agent.properties");
 			AgentContainer mainC = rt.createMainContainer(p);
 			
 			//Lancement des agents switch
			//Map<String, List<String>> graphAgent=createSwitchsFromOntologie(mainC,"./network.n3");
			Map<String, List<String>> graphAgent=createSwitchsFromOntologie(mainC,QueryKnowledgeBase.KB_FILE_PATH);
			
 			
 			
 			//Lancement de l'agent master avec en argument la liste des agents switch+ leurs liens.
 			AgentController masterAgent = mainC.createNewAgent("Master", "agents.MasterAgent", new Object[]{graphAgent});
 			masterAgent.start();
 			
 			
 		} catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	
 	private static Map<String, List<String>> createSwitchsFromOntologie(AgentContainer ac,String path) throws StaleProxyException
 	{
 		
 		/*
 		AgentController switch1 = ac.createNewAgent("switch1", "agents.SwitchAgent", new Object[]{});
 		switch1.start();
 		*/
 		//Map<AID, List<AID>> a = new HashMap<AID, List<AID>>();
 //		for(AID key : a.keySet()){ 
 //			a.put(key,new ArrayList<AID>());
 //		}
 		
 		QueryKnowledgeBase kb = new QueryKnowledgeBase(path);
 		Graph<String, String> g = kb.getGraph();
 		Map<String, KBMachine> machineMap = kb.getMachineMap();
 		
 		List<String> vertices = new ArrayList<String>(g.getVertices());
 
 		for(int i = 0; i < vertices.size(); i++) {
 			String nom = vertices.get(i);
 			
 			if(!machineMap.containsKey(nom))
 				continue;
 			
 			AgentController agent;
 			
 			if(machineMap.get(nom).machineType == KBMachine.type.SWITCH)
 				agent = ac.createNewAgent(nom, "agents.SwitchAgent", new Object[]{i});
 			else {
 				// FIXME: pas de vérification que l'agent avec qui on communique est déjà instancié.
 				agent = ac.createNewAgent(nom, "agents.UserAgent", new Object[]{machineMap.get(nom).interlocuteur});
 			}
 			
 			agent.start();
 		}
 
 		
 //		for(Entry<String, List<String>> tmp : kb.getLinks().entrySet())
 //		{
 //			
 //			System.out.println("\t"+s);
 //		}
 //		
 		
 		return kb.getLinks();
 		
 	
 	}
 
 }
