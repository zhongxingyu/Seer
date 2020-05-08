 
 import java.io.File;
 
 /**
  *
  * @author lennon
  */
 public class Search {
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 		try {
 			FileParser parser = new FileParser(new File("examples/" + args[0]));
 			parser.parse();
 			DebugFrame debugFrame = new DebugFrame();
 			debugFrame.setGraph(parser.getGraph());
 			debugFrame.setStartNode(parser.getStartNode());
 			debugFrame.setEndNodes(parser.getEndNodes());
 			debugFrame.setVisible(true);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
