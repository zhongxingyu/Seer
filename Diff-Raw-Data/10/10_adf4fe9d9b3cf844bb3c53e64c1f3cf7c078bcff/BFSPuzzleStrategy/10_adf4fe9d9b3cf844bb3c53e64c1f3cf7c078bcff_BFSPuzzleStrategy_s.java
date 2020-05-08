 package org.kornicameister.sise.puzzle.stratagies;
 
 import org.kornicameister.sise.core.graph.GraphEdge;
 import org.kornicameister.sise.core.graph.GraphNode;
 import org.kornicameister.sise.core.graph.NodeAccessibleStrategy;
 import org.kornicameister.sise.core.strategies.BFSStrategy;
 import org.kornicameister.sise.puzzle.builder.PuzzleNeighborsBuilder;
 import org.kornicameister.sise.puzzle.node.PuzzleNode;
 
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public class BFSPuzzleStrategy extends BFSStrategy {
     private static final Logger LOGGER = Logger.getLogger(BFSPuzzleStrategy.class.getName());
     private final NodeAccessibleStrategy strategy;
     protected PuzzleNeighborsBuilder neighborsBuilder;
 
     public BFSPuzzleStrategy() {
         LOGGER.setLevel(Level.INFO);
        this.strategy = new InversionAccessibleNodeStrategy();
     }
 
     @Override
     public void init(List<GraphNode> nodes) {
         super.init(nodes);
         this.neighborsBuilder = new PuzzleNeighborsBuilder(nodes);
     }
 
     @Override
     public String getReport() {
         StringBuilder sb = new StringBuilder(super.getReport());
         sb.append("Generated nodes:\t").append(this.neighborsBuilder.getGeneratedNodes()).append("\n");
         return sb.toString();
     }
 
     @Override
     public GraphEdge getNextAvailableNode(GraphNode node) {
         if (node.getNeighbours().size() == 0) {
             if (node instanceof PuzzleNode) {
                 PuzzleNode puzzleNode = (PuzzleNode) node;
                 final Map<Character, GraphNode> possibleNeighbours = neighborsBuilder.getPossibleNeighbours(puzzleNode);
                 if (possibleNeighbours.size() > 0) {
                     for (Map.Entry<Character, GraphNode> pass : possibleNeighbours.entrySet()) {
                         puzzleNode.addNeighbour(pass.getValue(), this.strategy, pass.getKey());
                     }
                 }
             }
         }
         return super.getNextAvailableNode(node);
     }
 
 }
