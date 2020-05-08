 package telnet;
 import java.util.ArrayList;
 
 public class Trie {
 	private Node top;
 	private Node location;
 
 	public Trie() {
 		top = new Node();
 		location = top;
 	}
 
 	public void addWord(String word) {
 		for (Character c : (word+"\r").toCharArray()) {
 			location = location.addNode(location, c);
 		}
 		location = top;
 	}
 
 	public String autoComplete(String word) {
 		String toReturn = "";
 		for (Character c : word.toCharArray()) {
 			if (location != null) {
 				location = location.getNode(c);
 			}
 		}
 		if (location != null) {
 			while (location.branchCount() == 1) {
 				location = location.getNode(0);
 				toReturn += location.getCharacter();
 			}
 		}
 		location = top;
		return toReturn.substring(0,toReturn.length()-1);
 	}
 
 	public ArrayList<String> getMutations(String word) {
 		ArrayList<String> toReturn = new ArrayList<String>();
 		for (Character c : word.toCharArray()) {
 			if (location != null) {
 				location = location.getNode(c);
 			}
 		}
 		if (location != null) {
 			ArrayList<Node> nodes = new ArrayList<Node>();
 			nodes.add(location);
 			for (String s : getPossibleCombos(nodes)) {
 				toReturn.add(word + s.substring(1, s.length()-1));
 			}
 		}
 		location = top;
 		return toReturn;
 	}
 
 	private ArrayList<String> getPossibleCombos(ArrayList<Node> nodes) {
 		ArrayList<String> toReturn = new ArrayList<String>();
 		for (Node n:nodes.get(nodes.size()-1).getBranch())
 		{
 			ArrayList<Node> newFoundTiles = new ArrayList<Node>();
 			for (int i=0; i<nodes.size(); i++) newFoundTiles.add(nodes.get(i));
 			newFoundTiles.add(n);
 			if (n.branchCount()==0){
 				String toAdd = "";
 				for(Node nf:newFoundTiles){
 					toAdd+=nf.getCharacter();
 				}
 				toReturn.add(toAdd);
 			}
 			for (String s:getPossibleCombos(newFoundTiles)){
 			toReturn.add(s);
 			}
 		}
 		return toReturn;
 	}
 
 
 }
