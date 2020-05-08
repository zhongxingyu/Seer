 package ar.edu.itba.sia;
 
 import com.google.common.collect.Lists;
 import ar.edu.itba.sia.domain.BoardImpl;
 import ar.edu.itba.sia.domain.Board;
 import ar.edu.itba.sia.domain.StatsHolderImpl;
 import ar.edu.itba.sia.gps.api.GPSProblem;
 import ar.edu.itba.sia.domain.Piece;
 import ar.edu.itba.sia.gps.api.StatsHolder;
 import ar.edu.itba.sia.gps.impl.*;
 import ar.edu.itba.sia.domain.persist.GameXML;
 import ar.edu.itba.sia.domain.persist.GameXML.GameNode;
 import ar.edu.itba.sia.domain.renderer.BoardRenderer;
 
 import java.awt.*;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 public class App {
 
 	public static void main(String[] args) throws Exception {
		GameXML game = GameXML.fromXml("random.xml");
 		Map<Point, GameNode> map =  game.nodes;
 		List<Piece> pieces = Lists.newArrayList();
         List<Point> points = Lists.newArrayList();
         points.addAll(map.keySet());
        Collections.reverse(points);
 		for(Point p: points) {
 			GameNode node = map.get(p);
             pieces.add(node.toPiece());
 		}

 		System.in.read();
 	    Board board = BoardImpl.withPieces(game.gameSize, game.gameSize, map);
         System.out.println("Showing the start level...");
         new BoardRenderer(board).render();
         StatsHolder holder = new StatsHolderImpl();
 		GPSProblem problem = new GPSProblemImpl(game.gameSize, game.gameSize, pieces, game.numberOfColors);
		GPSEngine engine = new DFSEngine(problem, null);
 		engine.engine(problem, null, holder);
 		System.out.println("-------------------------------------------");
 		System.out.println("Simulation time: " + holder.getSimulationTime()/(double)1000 + " seconds");
 		System.out.println("Exploded nodes: " + holder.getExplodedNodes());
 		System.out.println("Leaf nodes: " + holder.getLeafNodesNumber());
 		System.out.println("Solution depth: " + holder.getSolutionDepth());
 		System.out.println("Generated states: " + holder.getStatesNumber());
         System.out.println(GPSStateImpl.checkSumHit);
         System.out.println(GPSStateImpl.equalsHit);
     }
 
 	private static void sufflePieces(List<Piece> pieces) {
 		Random random = new Random();
 		for (int i = 0; i < 50000; i++) {
 			Piece auxA = null;
 			int a = random.nextInt(pieces.size());
 			auxA = pieces.get(a);
 			pieces.remove(a);
 			pieces.add(0, auxA);
 		}
 	}
 	
 }
