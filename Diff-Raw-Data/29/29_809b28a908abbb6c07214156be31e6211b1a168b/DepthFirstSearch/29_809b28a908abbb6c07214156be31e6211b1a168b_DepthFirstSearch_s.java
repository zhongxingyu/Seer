 package search;
 
 import agent.Action;
 import agent.Actions;
 
 public class DepthFirstSearch extends GeneralSearch {
 
 	public DepthFirstSearch(State startState, NodeInfo nodeInfo) {
 		super(startState, nodeInfo);
		// TODO Auto-generated constructor stub
 	}
 
 	private boolean isVisited(Node cur) {
 		boolean isinvisited = false;
 		boolean isinunvisited = false;
 		if(!this.visited.isEmpty() && cur != null) {
 			for(int x = 0; x < this.visited.size(); x++) {
				//System.out.println(this.visited.get(x).getState().toString() + " compared to " + cur.getState().toString());
 				if(this.visited.get(x).getState().toString().compareTo(cur.getState().toString()) == 0) {
 					isinvisited = true;
 				}
 			}
 		}
 		if(!this.unvisited.isEmpty() && cur != null) {
 			for(int x = 0; x < this.unvisited.size(); x++) {
				//System.out.println(this.visited.get(x).getState().toString() + " compared to " + cur.getState().toString());
 				if(this.unvisited.get(x).getState().toString().compareTo(cur.getState().toString()) == 0) {
 					isinunvisited = true;
 				}
 			}
 		}
 		
 		return isinvisited || isinunvisited;
 	}
 	
 	@Override
 	public void insert(Node node) {
		if(!this.unvisited.isEmpty()) {
			this.visited.add(this.unvisited.remove(0));
		}
 		
 		Actions children = node.getState().getActions();
 		
		
		
 		String result = "";
 
 		if(!children.isEmpty()) {
 			
 			for(int z = 0; z < children.size(); z++) {
				this.totalnodes++;
 				Node clone = ((Node)node.clone());
 				clone.update((Action)children.get(z));
 				if(!isVisited(clone)) {
 					this.unvisited.add(0,clone);
 					result += " : " + clone.getState().toString();
					//System.out.println(this.unvisited.get(z).getState().toString());
 				}
 				
 			}
 			System.out.println("Adding" + result);
			this.updateMaxStored(this.unvisited.size() + this.visited.size());
 		}
		if(children.isEmpty()) System.out.println("Loop of death!");
 		
 
 	}
 
 	@Override
 	public Node select() {
 		
 		if(!this.unvisited.isEmpty()) {
			return this.unvisited.get(0);
 		}
 		return null;
 	}
 	
 	public String toString() {
 		return "DepthFirstSearch";
 	}
 
 }
