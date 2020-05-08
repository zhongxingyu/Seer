 import java.util.LinkedList;
 import java.util.List;
 
 
 public class Generator {
 	
 	private Graph graph;
 	
 	public List<String> generateLadder(String start, int depth) {
 		List<String> ladder = new LinkedList<String>();
		if (depth == 0)
 			return ladder;
 		ladder.add(start);
 		graph = new Graph("dict" + start.length() + ".dat");
 		return findLadder(ladder, depth - 1);
 	}
 	
	private List<String> findLadder(List<String> ladder, int depth) {
		if (depth == 0)
 			return ladder;
 		
 		String current = ladder.get(ladder.size() - 1);
 		List<String> search = graph.getAdjacentWords(current);
 		
 		for (String s : search) {
			if (!ladder.contains(s)) {
				ladder.add(s);
				findLadder(ladder, depth - 1);
 				break;
			}
 		}
 		
 		return ladder;
 	}
 	
 	public static void main(String[] args) {
 		Generator g = new Generator();
		List<String> ladder = g.generateLadder("click", 100);
 		for (int i = 0; i < ladder.size(); i++) {
 			System.out.println((i + 1) + " - " + ladder.get(i));
 		}
 	}
 	
 }
