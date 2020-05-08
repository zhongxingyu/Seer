 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package polskaad1340;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import polskaad1340.window.LadowanieMapy;
 import polskaad1340.window.OknoMapy;
 import world.World;
 import agents.Agent;
 import clips.ClipsEnvironment;
 
 /**
  *
  * @author Kuba
  */
 public class PolskaAD1340 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
         //bugfix, patrz http://stackoverflow.com/questions/13575224/comparison-method-violates-its-general-contract-timsort-and-gridlayout
 
         // TODO code application logic here
         OknoMapy om = new OknoMapy();
         om.setVisible(true);
 
 		try {
 			LadowanieMapy lm = new LadowanieMapy("/maps/example.json");
 			om.importBackgroundTileGrid(lm.getMap());
 			om.setForegroundTileGrid(om.createTileGrid(lm.getMapSize(), 0));
 			om.drawAllTiles();
 			
 
 			ClipsEnvironment clipsEnv = new ClipsEnvironment();
 			World world = new World(clipsEnv, lm, om);
 
 			ArrayList<String> inferenceResults = new ArrayList<String>();
 			// glowna petla
			for (int i = 0; i < 10; i++) {
 				System.out.println("|ITERACJA " + (i + 1) + " |");
 
 				clipsEnv.getWorldEnv().reset();
 				// do swiata przekazujemy obiekty swiata oraz wywnioskowane
 				// przez agentow fakty
 				world.saveToClips();
 				for (int k = 0; k < inferenceResults.size(); k++) {
 					clipsEnv.getWorldEnv().assertString(inferenceResults.get(k));
 				}
 				clipsEnv.getWorldEnv().assertString("(iteracja " + (i + 1) + ")");
 				inferenceResults = new ArrayList<String>();
 
 				System.out.println("<wnioskowanie swiata>");
 				clipsEnv.getWorldEnv().run();
 				System.out.println("</wnioskowanie swiata>");
 				//clipsEnv.displayWorldFacts();
 				world.loadFromClips();
 
 				for (int j = 0; j < world.getAgents().size(); j++) {
 					Agent actualAgent = world.getAgents().get(j);
 					ArrayList<Object> visibleObjects = world.getVisibleWorld(actualAgent.getId());
 
 					clipsEnv.getAgentEnv().reset();
 					clipsEnv.getAgentEnv().load(actualAgent.getPathToClipsFile());
 
 					for (int k = 0; k < visibleObjects.size(); k++) {
 						clipsEnv.getAgentEnv().assertString(visibleObjects.get(k).toString());
 					}
 
 					// dany agent wnioskuje
 					//clipsEnv.displayAgentFacts();
 					System.out.println("<wnioskowanie agenta " + actualAgent.getId() + " >");
 					clipsEnv.getAgentEnv().run();
 					System.out.println("</wnioskowanie agenta " + actualAgent.getId() + " >");
 					
 
 					ArrayList<String> agentInferenceResults = actualAgent.getInferenceResults(clipsEnv.getAgentEnv());
 					// wywnioskowane przez agenta fakty dodajemy do wszystkich
 					// faktow, ktore po wnioskowaniu wszystkich agentow zosatna przekazane do swiata
 					for (String agentInferenceResult : agentInferenceResults) {
 						inferenceResults.add(agentInferenceResult);
 					}
 
 				}
 
 				System.out.println("|KONIEC ITERACJI " + (i + 1) + " |\n");
 			}
 
 		} catch (Exception ex) {
             Logger.getLogger(PolskaAD1340.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         System.out.println("done and done.");
 
     }
 }
