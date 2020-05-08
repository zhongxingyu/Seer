 package edu.usu.cs.search.incomplete;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import edu.usu.cs.heuristic.Heuristic;
 import edu.usu.cs.pddl.domain.ActionInstance;
 import edu.usu.cs.pddl.domain.incomplete.IncompleteActionInstance;
 import edu.usu.cs.pddl.domain.incomplete.Proposition;
 import edu.usu.cs.pddl.domain.incomplete.Risk;
 import edu.usu.cs.planner.Solver;
 import edu.usu.cs.planner.SolverOptions;
 import edu.usu.cs.search.StateNode;
 import edu.usu.cs.search.astar.AStarNode;
 
 /**
  * Node was previously named State so there may be references to State that are
  * actually Node. Node holds the state, risk sets for each proposition in the
  * state, and a set of critical risks used to get to this node.
  * 
  * @author student
  * 
  */
 public class FFRiskyNode  extends AStarNode {
 	protected HashMap<Proposition, Set<Risk>> propositions = null;
 	private Set<Risk> criticalRisks = null;
 	//protected IncompleteActionInstance action = null;
 	private int hash;
 	private boolean hashCodeInitialized = false;
 	protected SolverOptions solverOptions = null;
 
 
 
 	/**
 	 * Creates a node with no risk sets for the state.
 	 * 
 	 * @param propositions
 	 */
 	public FFRiskyNode(Set<Proposition> propositions, Heuristic heuristic, SolverOptions solverOptions) {
 		this.propositions = new HashMap<Proposition, Set<Risk>>();
 		for (Proposition proposition: propositions) {
 			this.propositions.put(proposition,new HashSet<Risk>());
 		}
 		this.criticalRisks = new HashSet<Risk>();
 		this.dimension = 2;
 		this.heuristic = heuristic;
 		this.criticalRisks = new HashSet<Risk>();
 		this.state = propositions;
 		this.solverOptions = solverOptions;
 	}
 
 	//	public FFRiskyNode(HashMap<Proposition, Set<Risk>> propositions,
 	//			Set<Risk> criticalRisks) {
 	//		this.propositions = propositions;
 	//		this.criticalRisks = criticalRisks;
 	//		this.dimension = 2;
 	//	}
 
 	public FFRiskyNode(){
 
 	}
 
 
 	public FFRiskyNode(FFRiskyNode node) {
 
 		// Copy the propositions with their associated risks
 		this.propositions = new HashMap<Proposition, Set<Risk>>();
 		for (Proposition prop : node.getPropositions().keySet()) {
 			Set<Risk> risks = new HashSet<Risk>();
 			for (Risk risk : node.getPropositions().get(prop)) {
 				risks.add(risk);
 			}
 			this.propositions.put(prop, risks);
 		}
 
 		// Copy the critical risks that were used to get to this node
 		this.criticalRisks = new HashSet<Risk>();
 		this.criticalRisks.addAll(node.getCriticalRisks());
 		this.parent = node.parent;
 		this.state = node.state;
 		this.action = node.action;		
 		this.dimension = node.dimension;
 		this.heuristic = node.heuristic;
 	}
 	public HashMap<Proposition, Set<Risk>> getPropositions() {
 		return propositions;
 	}
 
 	public void setPropositions(HashMap<Proposition, Set<Risk>> propositions) {
 		this.propositions = propositions;
 	}
 
 	public Set<Risk> getCriticalRisks() {
 		return criticalRisks;
 	}
 
 	public void setCriticalRisks(Set<Risk> criticalRisks) {
 		this.criticalRisks = criticalRisks;
 	}
 
 
 
 
 	@Override
 	public String toString() {
 		String str = "Node:\n";
 		str += "\tF: " + fvalue[0] + " " + fvalue[1] + //" G: " + gValues[0] + " H: " + hValues  +
 		"\n";
 		str += "\tState:\n";
 		for (Proposition prop : propositions.keySet()) {
 			str += "\t\t" + prop.getName() + ": ";
 			if (propositions.get(prop).size() == 0) {
 				str += "not vulnerable\n";
 			} else {
 				str += "vulnerable\n";
 				for (Risk risk : propositions.get(prop)) {
 					str += "\t\t\t" + risk.toString() + "\n";
 				}
 			}
 		}
 
 		str += "\tRisk set:\n";
 		for (Risk risk : criticalRisks) {
 			str += "\t\t" + risk.toString() + "\n";
 		}
 
 		return str;
 	}
 
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof FFRiskyNode)) {
 			return false;
 		}
 
 		FFRiskyNode objNode = (FFRiskyNode)obj;
 
 		// If the propositions are the same, return true. We don't care about
 		// the risk sets or the state's critical risk set
 		if(!this.getPropositions().keySet().equals(objNode.getPropositions().keySet())){
 			return false;
 		}
 
 		//otherwise need to check risks
 
 		for(Proposition p : propositions.keySet()){
 			Set<Risk> risks = propositions.get(p);
 			Set<Risk> objRisks = objNode.getPropositions().get(p);
 			if(!risks.containsAll(objRisks) || !objRisks.containsAll(risks)){
 			//if(risks.equals(objRisks)){
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 //	@Override
 //	public int hashCode() {
 //		if (!hashCodeInitialized) {
 //			String str = "";
 //
 //			// Sort the propositions first
 //			List<Proposition> props = new ArrayList<Proposition>(propositions
 //					.keySet());
 //			Collections.sort(props, new Comparator<Proposition>() {
 //				public int compare(Proposition first, Proposition second) {
 //					return first.getName().compareTo(second.getName());
 //				}
 //			});
 //
 //			// make them a big long string
 //			for (Proposition proposition : props) {
 //				str += proposition.toString() + " ";
 //			}
 //
 //			// cache hash
 //			hash = str.hashCode();
 //			hashCodeInitialized = true;
 //		}
 //
 //		return hash;
 //	}
 //
 	public double[] getGValue() {
 		if(this.gvalue == null){
 			this.gvalue = new double[dimension];
 			for(int i =0 ; i < dimension ; i++){
 				if(parent == null){
 					this.gvalue[i] = 0.0;
 				}
 				else{
 					if(i == 1){
 						this.gvalue[i] = parent.getGValue()[i] + this.action.getCost();
 					}
 					else if (i==0){
 						Set<Risk> risks = new HashSet<Risk>(this.getCriticalRisks());
 						risks.removeAll(((FFRiskyNode)parent).getCriticalRisks());
 						this.gvalue[i] = risks.size();
 					}
 				}
 			}
 		}
 		return this.gvalue;
 	}
 
 
 	public List<StateNode> createSubsequentNodes(
 			List<ActionInstance> subsequentActions){
 
 		boolean useHelpfulActions = solverOptions.isUseHelpfulActions();
 
 		if(subsequentNodes != null && useHelpfulActions){
 			//need to reset node after failing in local search
 			helpfulActions = null;
 			subsequentNodes = null;
 		}
 
 
 
 		subsequentNodes = new ArrayList<StateNode>();
 
 		List<ActionInstance> actsToExpand = null;
 
 		if(helpfulActions != null && useHelpfulActions){
 			actsToExpand =  new ArrayList(helpfulActions);
 		}
 		else{
 			actsToExpand = subsequentActions;
 		}
 
 		for(ActionInstance act : actsToExpand){
 			FFRiskyNode node = getSuccessorNode(act);
 			if(node != null && !node.equals(this.parent)){
 				subsequentNodes.add(node);
 			}
 		}
 		return subsequentNodes;
 	}
 
 
 
 	//	private int planLength = -1;
 	//
 	//	/**
 	//	 * Returns the length of the plan up to this point
 	//	 * @return
 	//	 */
 	//	public int getPlanLength() {
 	//		if(planLength == -1) {
 	//			if(parent == null) {
 	//				planLength = 1;
 	//			} else {
 	//				planLength = parent.getPlanLength() + 1;
 	//			}
 	//		}
 	//		return planLength;
 	//	}
 
 	//	public void computeFValues() {
 	//		for(int i = 0; i < fValues.length; i++){
 	//			fValues[i] = gValues[i] + hValues[i];
 	//		}
 	//	}
 
 	public int compareTo(FFRiskyNode second){
 		FFRiskyNode first = this;
 		boolean alphaCombo = false;
 		if(!alphaCombo){
 			Double[] diffs = new Double[2];
 			for(int i = 0; i < 2; i++){
 				diffs[i] = first.getFValue()[i] - second.getFValue()[i];
 			}
 			if(diffs[0] != 0) {
 				return diffs[0].intValue();
 			}
 			else{
 				return diffs[1].intValue(); //same num risks, so compare length
 			}
 		}
 		else{
 			double alpha = 0.5;
 			Double value = (alpha*first.getFValue()[0] + (1-alpha)*first.getFValue()[1]) - 
 			(alpha*second.getFValue()[0] + (1-alpha)*second.getFValue()[1]);
 			return value.intValue();
 		}
 	}
 
 
 
 	public  FFRiskyNode getSuccessorNode(ActionInstance action1) {
 		IncompleteActionInstance action = (IncompleteActionInstance)action1;
 		
 		// If the action isn't applicable to the node, return null
 		if (!isActionApplicable(action)) {
 			return null;
 		}
 
 		// Create the new node and copy all the values from the old one to the
 		// new one
 		FFRiskyNode node = new FFRiskyNode(this);
 
 		// Add all risks associated with the new node
 		node.addCriticalRisks(action, this);
 
 		// Remove any absolute delete effect from node
 		node.applyDeleteEffects(this, action);
 
 		// Apply possible delete effects
 		node.applyPossibleDeleteEffects(this, action);
 
 		// Apply absolute add effects
 		node.applyAddEffects(this, action);
 
 		// Apply possible add effects
 		node.applyPossibleAddEffects(this, action);
 
 		// addCriticalRisks(node, action);
 
 		node.setParent(this);
 		node.setAction(action);
 
 		node.setHeuristic(this.getHeuristic());
 		node.setState(node.getPropositions().keySet());
 		
 		node.setSolverOptions(solverOptions);
 
 		return node;
 	}
 
 	private void setSolverOptions(SolverOptions solverOptions2) {
 		solverOptions = solverOptions2;
 		
 	}
 
 	/**
 	 * Returns true if the specified action is applicable at the specified node.
 	 * @param action
 	 * 
 	 * @return
 	 */
 	public  boolean isActionApplicable(IncompleteActionInstance action) {
 		return propositions.keySet().containsAll(action.getPreconditions());
 	}
 
 	/**
 	 * Returns any open precondition risks that come from the specified node and
 	 * action.
 	 * 
 	 * @param node
 	 * @param action
 	 * @return
 	 */
 	private  Set<Risk> getPrecOpen(FFRiskyNode node, IncompleteActionInstance action) {
 		Set<Risk> precOpen = new HashSet<Risk>();
 
 		for (Proposition possPrec : action.getPossiblePreconditions()) {
 			// If the node doesn't contain the proposition then it is an open
 			// precondition risk
 			if (!node.getPropositions().containsKey(possPrec)) {
 				precOpen.add(Risk.getRiskFromIndex(Risk.PRECOPEN, action.getName(), possPrec
 						.getName()));
 			}
 		}
 
 		return precOpen;
 	}
 
 	// /**
 	// * Returns true if the action's risks and the node's propositions are not
 	// * mutually exclusive.
 	// *
 	// * @param node
 	// * @param action
 	// * @param proposition
 	// * @return
 	// */
 	// private static boolean isVulnerable(Node node, Proposition proposition,
 	// Set<Risk> actionCriticalRisks) {
 	// // Get the risks of the proposition
 	// Set<Risk> propRisks = node.getPropositions().get(proposition);
 	//
 	// // Get their intersection
 	// propRisks.retainAll(actionCriticalRisks);
 	//
 	// // If the intersection > 1, it is still vulnerable
 	// return propRisks.size() > 0 ? true : false;
 	// }
 
 	/**
 	 * Returns all risks in the risk sets in the propositions of the node that
 	 * support the action.
 	 * 
 	 * @param node
 	 * @param action
 	 * @return
 	 */
 	private  Set<Risk> getPrecRisks(FFRiskyNode node, IncompleteActionInstance action) {
 		Set<Risk> precRisks = new HashSet<Risk>();
 
 		for (Proposition prec : action.getPreconditions()) {
 			// if (node.getPropositions().containsKey(prec)) {
 			precRisks.addAll(node.getPropositions().get(prec));
 			// }
 		}
 
 		for (Proposition prec : action.getPossiblePreconditions()) {
 			if (node.getPropositions().containsKey(prec)) {
 				precRisks.addAll(node.getPropositions().get(prec));
 			}
 		}
 
 		return precRisks;
 	}
 
 	/**
 	 * Removes the specified action's delete effects from the specified node's
 	 * propositions.
 	 */
 	private  void applyDeleteEffects(FFRiskyNode initialNode, IncompleteActionInstance action) {
 		for (Proposition effect : action.getDeleteEffects()) {
 			if (initialNode.getPropositions().containsKey(effect)) {
 				propositions.remove(effect);
 			}
 		}
 	}
 
 	/**
 	 * Applies the possible delete effects of the specified action to the
 	 * specified node.
 	 */
 	private  void applyPossibleDeleteEffects(FFRiskyNode initialNode,
 			IncompleteActionInstance action) {
 		for (Proposition effect : action.getPossibleDeleteEffects()) {
 			// If the proposition isn't in the node, continue
 			if (!initialNode.getPropositions().containsKey(effect))
 				continue;
 
 			// // Remove the proposition if it is no longer vulnerable
 			// if(!isVulnerable(node, effect, getPrecRisks(node, action))) {
 			// node.getPropositions().remove(effect);
 			// continue;
 			// }
 
 			// Add possible clobbers to the risk set
 			propositions.get(effect).add(
 					Risk.getRiskFromIndex(Risk.POSSCLOB, action.getName(), effect.getName()));
 		}
 	}
 
 	/**
 	 * Applies the absolute add effects of the specified action to the specified
 	 * node
 	 */
 	private  void applyAddEffects(FFRiskyNode initialNode, IncompleteActionInstance action) {
 
 		Set<Risk> openPrecRisks = getPrecOpen(initialNode, action);
 		Set<Risk> precRisks = getPrecRisks(initialNode, action);
 		Set<Risk> actRisks = new HashSet<Risk>(openPrecRisks);
 		actRisks.addAll(precRisks);
 
 		for (Proposition effect : action.getAddEffects()) {
 			// If the proposition isn't in the node (it's absolutely false)
 			if (!initialNode.getPropositions().containsKey(effect)) {
 
 				// If it was false before, risk set consists of any risk to the
 				// current action
 				Set<Risk> riskSet = new HashSet<Risk>(actRisks);
 
 				propositions.put(effect, riskSet);
 				continue;
 			}
 
 			// If the proposition is absolutely true, just continue
 			if (initialNode.getPropositions().get(effect).size() == 0) {
 				continue;
 			}
 
 			// Get the intersection of the risk sets in propositions in action
 			// precondition.
 			Set<Risk> intersection = new HashSet<Risk>(actRisks);
 
 			// Intersect these with risks in the proposition to get the new risk
 			// set for the prop.
 			intersection.retainAll(initialNode.getPropositions().get(effect));
 
 			// Set the intersection as the new risk set for the proposition
 			propositions.put(effect, intersection);
 		}
 	}
 
 	/**
 	 * Applies the possible add effects of the specified action to the specified
 	 * node
 	 */
 	private  void applyPossibleAddEffects(FFRiskyNode initialNode,
 			IncompleteActionInstance action) {
 
 		Set<Risk> openPrecRisks = getPrecOpen(initialNode, action);
 		Set<Risk> precRisks = getPrecRisks(initialNode, action);
 		Set<Risk> actRisks = new HashSet<Risk>(openPrecRisks);
 		actRisks.addAll(precRisks);
 
 		for (Proposition effect : action.getPossibleAddEffects()) {
 			// If the proposition isn't in the node add it with an unlisted effect risk
 			if (!initialNode.getPropositions().containsKey(effect)) {
 
 				// If it was false before, risk set consists of any risk to the
 				// current action
 				Set<Risk> riskSet = new HashSet<Risk>(actRisks);
 				riskSet.add(Risk.getRiskFromIndex(Risk.UNLISTEDEFFECT, action.getName(),
 						effect.getName()));
 				
 				propositions.put(effect, riskSet);
 				continue;
 			}
 
 			// If the proposition is absolutely true, just continue
 			if (initialNode.getPropositions().get(effect).size() == 0) {
 				continue;
 			}
 
 			// Get the intersection of the risk sets in propositions in action
 			// precondition.
 			Set<Risk> intersection = new HashSet<Risk>(actRisks);
 
 			// Also add the UnlistedEffect risk
 			intersection.add(Risk.getRiskFromIndex(Risk.UNLISTEDEFFECT, action.getName(),
 					effect.getName()));
 
 			// Intersect these with risks in the proposition to get the new risk
 			// set for the prop.
 			intersection.retainAll(initialNode.getPropositions().get(effect));
 
 			// Set the intersection as the new risk set for the proposition
 			propositions.put(effect, intersection);
 		}
 	}
 
 	/**
 	 * Unions critical risks from the node with new critical risks from the
 	 * action
 	 */
 	private  void addCriticalRisks(IncompleteActionInstance action, FFRiskyNode initialNode) {
 		// Union the action's precondition risks with the node's critical risks
 		criticalRisks.addAll(getPrecRisks(initialNode, action));
 
 		// Union the action's prec open risks with the node's critical risks
 		criticalRisks.addAll(getPrecOpen(initialNode, action));
 	}
 }
