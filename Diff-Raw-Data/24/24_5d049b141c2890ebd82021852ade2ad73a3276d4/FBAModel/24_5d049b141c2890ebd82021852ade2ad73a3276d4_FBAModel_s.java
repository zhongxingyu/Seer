 package edu.rutgers.MOST.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 public class FBAModel {
 
 	protected Vector<ModelReaction> reactions;
 	protected Vector<ModelMetabolite> metabolites;
 	protected Vector<Double> objective;
 	protected ArrayList<Map<Integer, Double>> sMatrix;
 	protected ArrayList<Integer> metaboliteInternalIdList;
 	protected ArrayList<Integer> reactionIdList;
 	protected Map<Object, Object> reactionsIdPositionMap;
 	
 	public Map<Object, Object> getReactionsIdPositionMap() {
 		return reactionsIdPositionMap;
 	}
 
 	public void setReactionsIdPositionMap(Map<Object, Object> reactionsIdPositionMap) {
 		this.reactionsIdPositionMap = reactionsIdPositionMap;
 	}
 
 	protected Map<Object, Object> metaboliteInternalIdMap;
 	
 	public FBAModel() {
 		ReactionFactory rFactory = new ReactionFactory("SBML");
 		this.reactions = rFactory.getAllReactions(); 
 		this.objective = rFactory.getObjective();
 		this.reactionIdList = rFactory.reactionIdList();
 		this.reactionsIdPositionMap = rFactory.getReactionsIdPositionMap();
 		
 		MetaboliteFactory mFactory = new MetaboliteFactory("SBML");
 		this.metabolites = mFactory.getAllInternalMetabolites();
 		this.metaboliteInternalIdList = mFactory.metaboliteInternalIdList();
 		this.metaboliteInternalIdMap = mFactory.getInternalMetabolitesIdPositionMap();
 
 		ReactantFactory reactantFactory = new ReactantFactory("SBML");
 		ArrayList<SBMLReactant> reactantList = reactantFactory.getAllReactants();		
 		ProductFactory productFactory = new ProductFactory("SBML");
 		ArrayList<SBMLProduct> productList = productFactory.getAllProducts();
 		
 		this.sMatrix = new ArrayList<Map<Integer, Double>>(metaboliteInternalIdList.size());
 		for (int i = 0; i < metaboliteInternalIdList.size(); i++) {
 			Map<Integer, Double> sRow = new HashMap<Integer, Double>();
 			sMatrix.add(sRow);
 		}
 		
 		for (int i = 0; i < reactantList.size(); i++) {
 			SBMLReactant reactant = (SBMLReactant) reactantList.get(i);
 			if (metaboliteInternalIdList.contains(reactant.getMetaboliteId()) && reactionIdList.contains(reactant.getReactionId())) {
 				sMatrix.get((Integer) metaboliteInternalIdMap.get(reactant.getMetaboliteId())).put((Integer) reactionsIdPositionMap.get(reactant.getReactionId()), -reactant.getStoic());
 			}
 		}
 		
 		for (int i = 0; i < productList.size(); i++) {
 			SBMLProduct product = (SBMLProduct) productList.get(i);
 			if (metaboliteInternalIdList.contains(product.getMetaboliteId()) && reactionIdList.contains(product.getReactionId())) {			
 				sMatrix.get((Integer) metaboliteInternalIdMap.get(product.getMetaboliteId())).put((Integer) reactionsIdPositionMap.get(product.getReactionId()), product.getStoic());
 			}
 		}
 
 		
 //		for (int i = 0; i < metabolites.size(); i++) {
 //			Iterator<Integer> iterator = sMatrix.get(i).keySet().iterator();
 //			
 //			while (iterator.hasNext()) {
 //				Integer j = iterator.next();
 //				Double s = sMatrix.get(i).get(j);
 //				
 //				System.out.println((i + 1) + "\t" + (j + 1) + "\t" + s);
 //			}
 //		}
 	}
 	
 	public Vector<ModelReaction> getReactions() {
 		return this.reactions;
 	}
 	
 	public int getNumMetabolites() {
 		return this.metabolites.size();
 	}
 	
 	public int getNumReactions() {
 		return this.reactions.size();
 	}
 	
 	public Vector<Double> getObjective() {
 	    return this.objective;
 	}
 	
 	public ArrayList<Map<Integer, Double>> getSMatrix() {
 		return this.sMatrix;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public String toString() {
 		return "FBAModel [reactions=" + reactions + ", metabolites="
 				+ metabolites + ", objective=" + objective + "]";
 	}
 
 }
