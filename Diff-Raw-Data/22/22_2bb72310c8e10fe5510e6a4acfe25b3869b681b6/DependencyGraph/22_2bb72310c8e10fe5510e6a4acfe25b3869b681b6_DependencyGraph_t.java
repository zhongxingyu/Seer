 package preprocessor;
 
 import java.util.ArrayList;
 
 import edu.stanford.nlp.ling.IndexedWord;
 import edu.stanford.nlp.semgraph.SemanticGraph;
 
 public class DependencyGraph {
 	ArrayList<DependencyNode> graph;
 
 	public DependencyGraph() {
 		this.graph = new ArrayList<>();
 	}
 
 	void addSentence(SemanticGraph dependencies, int sentenceId) {
 		//dependencies.prettyPrint();
 		if (dependencies.getRoots().size() != 1) {
 			System.err.println("Sentence " + sentenceId + " has " +
 								dependencies.getRoots().size() + " roots !!!");
 		} else {
 			ArrayList<DependencyNode> tmpList = new ArrayList<>();
 			IndexedWord root = dependencies.getFirstRoot();
 			generateGraph(dependencies, root, tmpList, sentenceId);
 
 			for (DependencyNode curentNode : tmpList) {
 				for (DependencyNode possibleChildNode : tmpList) {
 					if (!possibleChildNode.equals(curentNode)
 							&& dependencies.getChildList(curentNode.value)
 									.contains(possibleChildNode.value)) {
						
						if (curentNode.neighbours.get(dependencies.getEdge(curentNode.value,possibleChildNode.value).toString()) == null)
						{
							ArrayList<DependencyNode> nodeList = new ArrayList<>();
							nodeList.add(possibleChildNode);
							curentNode.neighbours.put(dependencies.getEdge(curentNode.value,possibleChildNode.value).toString(),nodeList);
						}
						else
						{
							curentNode.neighbours.get(dependencies.getEdge(curentNode.value,possibleChildNode.value).toString()).add(possibleChildNode);
						}
 						if (!possibleChildNode.parents.contains(curentNode))
 							possibleChildNode.parents.add(curentNode);
 					}
 				}
 			}
 			for (DependencyNode curentNode : tmpList){
 				if (curentNode.value.equals(root))
 					graph.add(curentNode);
 			}
 		}
 	}
 
 	void generateGraph(SemanticGraph dependencies, IndexedWord curentWord,
 			ArrayList<DependencyNode> tmpList, int sentenceId) {
 
 		DependencyNode curentNode = null;
 		boolean exists = false;
 		for (DependencyNode checkNode : tmpList) {
 			if (checkNode.value.equals(curentWord)) {
 				exists = true;
 			}
 		}
 		if (!exists) {
 			curentNode = new DependencyNode(curentWord, sentenceId);
 			curentNode.lemValue = curentWord.lemma();
 			curentNode.posTag = curentWord.tag();
 			tmpList.add(curentNode);
 		}
 
 		if (dependencies.getChildList(curentWord).size() != 0) {
 			for (IndexedWord child : dependencies.getChildList(curentWord))
 				generateGraph(dependencies, child, tmpList, sentenceId);
 		}
 	}
 	
 }
