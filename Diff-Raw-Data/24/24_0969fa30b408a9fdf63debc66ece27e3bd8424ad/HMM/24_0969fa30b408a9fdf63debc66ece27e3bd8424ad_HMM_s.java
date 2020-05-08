 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 class HMM
 {	
 	HashMap<String, HMMState> graph;
 	
 	public HMM(){
 	
 		//tag -> state mapping for finding what we already have
 		graph = new HashMap<String, HMMState>(); 
 	}
 	
 	public void train(Vector<String> tokens, Vector<String> tags){
 		//create state graph from given data
 		int n = assignment5.ngram_length;
 		int ngram_counter = n;
 		Vector<String> ngram = new Vector<String>(n);
 		HMMState lastState = null;
 		
 		for(int i=0; i<tokens.size(); i++){
 			//we need enough tokens left, otherwise step backwards to overlap 
 			int shortage = (tokens.size()-i+1)-(n-ngram_counter); 
 			if (shortage < 0){
 				//shift left by amount of more tokens we need to get a complete ngram
 				i += shortage;
 			}
 			
 			String token = tokens.get(i);			
 			if (ngram_counter!=0){
 				ngram.add(token);
 				ngram_counter--;
 				
 				//TODO: maybe stop adding to ngram if senetence end?
 			} else{
 				//we have another full ngram, create a new state or add to existing
				List<String> ngram_tags = tags.subList(i-2, i+1);
 				HMMState state = graph.get(assignment5.join(ngram_tags, " "));
 				
 				if(state == null) {
					System.out.println("state #"+assignment5.join(ngram_tags, " ") +"# not found");
 					//create new state
 					state = new HMMState();
 					state.tags = new Vector<String>(ngram_tags);
 
 					state.probabilities = new HashMap<Vector<String>, Double>();
 					graph.put(assignment5.join(state.tags, " "), state);
 				}
 				else
 				{
					System.out.println("state #"+assignment5.join(ngram_tags, " ") +"# found");
 				}
 
 				//add absolute probability
 				Double prob = state.probabilities.get(ngram);
 				if(prob != null){
 					prob++;
 				} else {
 					state.probabilities.put(ngram, new Double(1));
 				}
 
 				//add or update edge to this state
 				if(lastState != null){
 					HMMEdge edge = lastState.outgoing.get(state.tags);
 					if (edge != null){
 						edge.probability++;
 					} else {
 						lastState.outgoing.put(state.tags, new HMMEdge(state, 1));
 					}
 				}
 				
 				lastState = state;
 				
 				//start over
 				ngram_counter = n;
 				ngram.clear();
 			}
 		}
 		
 		//normalize probabilities
 		for (String key: graph.keySet()){
 			HMMState state = graph.get(key);
 			
 			int total_emissions = state.probabilities.size();
 			for (Vector<String> p_key : state.probabilities.keySet()){
 				Double p = state.probabilities.get(p_key);
 				p /= total_emissions;
 			}
 
 			//get sum of all edges
 			int sum_edge_probs = 0;
 			for(Vector<String> e_key: state.outgoing.keySet()){
 				HMMEdge edge = state.outgoing.get(e_key);
 				sum_edge_probs += edge.probability;
 			}
 
 			//normalize edges
 			for(Vector<String> e_key: state.outgoing.keySet()){
 				HMMEdge edge = state.outgoing.get(e_key);
 				edge.probability /= sum_edge_probs;
 			}
 		}
 		
 	}
 	
 	public Vector<String> decode(Vector<String> tokens){return null;}
 	public void serialize(){}
 }
