 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package RSLBench.Helpers.Utility;
 
 import RSLBench.Search.DistanceInterface;
 import RSLBench.Search.Graph;
 import RSLBench.Search.SearchAlgorithm;
 import RSLBench.Search.SearchFactory;
 import RSLBench.Search.SearchResults;
 import java.util.List;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import rescuecore2.config.Config;
 import rescuecore2.standard.entities.Blockade;
 import rescuecore2.standard.entities.Human;
 import rescuecore2.worldmodel.EntityID;
 
 /**
  * Utility function that mimicks the pre-utility functions evaluation.
  *
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class BlockadesUtilityFunction extends SecondUtilityFunction {
     private static final Logger Logger = LogManager.getLogger(BlockadesUtilityFunction.class);
 
     /** Key to the penalty applied when a target fire is blocked */
     public static final String KEY_BLOCKED_PENALTY = "util.blockade.penalty";
 
     private SearchAlgorithm search;
     private Graph connectivityGraph;
     private DistanceInterface distanceMatrix;
 
     @Override
     public void setConfig(Config config) {
         super.setConfig(config);
 
         // Instantiate the proper search algorithm
         search = SearchFactory.buildSearchAlgorithm(config);
         connectivityGraph = new Graph(world);
         distanceMatrix = new DistanceInterface(world);
     }
 
     @Override
     public double getFireUtility(EntityID agent, EntityID target) {
         Human hagent = (Human)world.getEntity(agent);
         EntityID position = hagent.getPosition();
         SearchResults results = search.search(position, target, connectivityGraph, distanceMatrix);
 
         double utility = super.getFireUtility(agent, target);
        Logger.debug("Base utility from fire brigade {} to fire {}: {}", agent, target, utility);
         List<Blockade> blockades = results.getPathBlocks();
         if (!blockades.isEmpty()) {
            Logger.debug("Agent {} blocked from reaching fire {} by {}", agent, target, blockades.get(0).getID());
             utility -= config.getFloatValue(KEY_BLOCKED_PENALTY);
         }
 
         return utility;
     }
 
 }
