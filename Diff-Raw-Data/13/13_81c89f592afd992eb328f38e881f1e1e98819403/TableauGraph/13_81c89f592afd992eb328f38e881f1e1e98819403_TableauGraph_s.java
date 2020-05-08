 package edu.iastate.pdlreasoner.tableau.graph;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import edu.iastate.pdlreasoner.model.DLPackage;
 import edu.iastate.pdlreasoner.server.graph.GlobalNodeID;
 import edu.iastate.pdlreasoner.tableau.Blocking;
 import edu.iastate.pdlreasoner.tableau.branch.Branch;
 import edu.iastate.pdlreasoner.tableau.branch.BranchPoint;
 import edu.iastate.pdlreasoner.tableau.branch.BranchPointSet;
 import edu.iastate.pdlreasoner.util.CollectionUtil;
 
 public class TableauGraph {
 
 	private static final Logger LOGGER = Logger.getLogger(TableauGraph.class);
 	
 	//Constants
 	private DLPackage m_Package;
 	
 	//Variables
 	private Set<Node> m_Roots;
 	private Map<GlobalNodeID,Node> m_GlobalMap; /////////////PRUNE ME WHEN BACKTRACKING
 	private NodeFactory m_NodeFactory;
 	private List<Branch> m_Branches;
 	private Blocking m_Blocking;
 	
 	//Processors
 	private ClashCauseCollector m_ClashCollector;
 	private OpenNodesCollector m_OpenNodesCollector;
 	private PruneNodesCollector m_PruneNodesCollector;
 	private ConceptPruner m_ConceptPruner;
 
 	public TableauGraph(DLPackage dlPackage) {
 		m_Package = dlPackage;
 		m_Roots = CollectionUtil.makeSet();
 		m_GlobalMap = CollectionUtil.makeMap();
 		m_NodeFactory = new NodeFactory(this);
 		m_Branches = CollectionUtil.makeList();
 		m_Blocking = new Blocking();
 		m_ClashCollector = new ClashCauseCollector();
 		m_OpenNodesCollector = new OpenNodesCollector();
 		m_PruneNodesCollector = new PruneNodesCollector();
 		m_ConceptPruner = new ConceptPruner();
 	}
 	
 	public DLPackage getPackage() {
 		return m_Package;
 	}
 	
 	public void accept(NodeVisitor v) {
 		for (Node root : m_Roots) {
 			root.accept(v);
 		}
 	}
 	
 	public Node makeNode(BranchPointSet dependency) {
 		return m_NodeFactory.make(dependency);
 	}
 	
 	public Node makeRoot(BranchPointSet dependency) {
 		Node n = makeNode(dependency);
 		m_Roots.add(n);
 		
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(m_Package.toDebugString() + "added root " + n + ", all roots = " + m_Roots);
 		}
 		
 		return n;
 	}
 
 	public void put(GlobalNodeID globalID, Node node) {
 		m_GlobalMap.put(globalID, node);
 	}
 	
 	public Node get(GlobalNodeID globalID) {
 		return m_GlobalMap.get(globalID);
 	}
 
 	public void addBranch(Branch branch) {
 		m_Branches.add(branch);
 		
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(m_Package.toDebugString() + "added branch = " + branch);
 		}
 	}
 	
 	public boolean hasBranch(BranchPoint bp) {
 		for (int i = m_Branches.size() - 1; i >= 0; i--) {
 			int compare = bp.compareTo(m_Branches.get(i).getBranchPoint());
 			if (compare == 0) {
 				return true;
 			} else if (compare > 0) {
 				return false;
 			}			
 		}
 		return false;
 	}
 
 	public Branch getLastBranch() {
 		return m_Branches.get(m_Branches.size() - 1);
 	}
 	
 	public boolean isBlocked(Node n) {
 		return m_Blocking.isBlocked(n);
 	}
 	
 	public void pruneTo(BranchPoint restoreTarget) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(m_Package.toDebugString() + "pruning starts with target = " + restoreTarget);
 		}
 		
 		//Prune nodes
 		m_PruneNodesCollector.reset(restoreTarget);
 		accept(m_PruneNodesCollector);
 		for (Node n : m_PruneNodesCollector.getNodes()) {
 			if (!m_Roots.remove(n)) {
 				n.removeFromParent();
 			}
 			
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug(m_Package.toDebugString() + "removed node " + n);
 			}
 		}
 		
 		//Prune and reopen concepts on remaining node
 		m_ConceptPruner.reset(restoreTarget);
 		accept(m_ConceptPruner);
 		
 		//Prune branches
 		for (int i = m_Branches.size() - 1; i >= 0; i--) {
 			Branch iBranch = m_Branches.get(i);
 			if (iBranch.getDependency().hasSameOrAfter(restoreTarget)) {
 				m_Branches.remove(i);
 			} else {
 				break;
 			}
 		}
 		
		//Reopen remaining branches - those that do not depend on the restoreTarget
		//but still have to be pruned to make sure that restoreTarget is the latest branch.
 		for (int i = m_Branches.size() - 1; i >= 0; i--) {
 			Branch iBranch = m_Branches.get(i);
			if (restoreTarget.equals(iBranch.getBranchPoint())) break;
 			
 			m_Branches.remove(i);
 			iBranch.reopenConceptOnNode();
 		}
 
 		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(m_Package.toDebugString() + "branches = " + m_Branches);
 		}
 	}
 
 	public BranchPointSet getEarliestClashCause() {
 		m_ClashCollector.reset();
 		accept(m_ClashCollector);
 		Set<BranchPointSet> clashCauses = m_ClashCollector.getClashCauses();
 		return clashCauses.isEmpty() ? null : Collections.min(clashCauses, BranchPointSet.ORDER_BY_LATEST_BRANCH_POINT);
 	}
 	
 	public Set<Node> getOpenNodes() {
 		m_OpenNodesCollector.reset();
 		accept(m_OpenNodesCollector);
 		return m_OpenNodesCollector.getNodes();
 	}
 	
 	private static class ClashCauseCollector implements NodeVisitor {
 		
 		private Set<BranchPointSet> m_ClashCauses;
 		
 		public ClashCauseCollector() {
 			m_ClashCauses = CollectionUtil.makeSet();
 		}
 		
 		public void reset() {
 			m_ClashCauses.clear();
 		}
 		
 		public Set<BranchPointSet> getClashCauses() {
 			return m_ClashCauses;
 		}
 
 		@Override
 		public void visit(Node n) {
 			m_ClashCauses.addAll(n.getClashCauses());
 			n.clearClashCauses();
 		}
 		
 	}
 
 	private class OpenNodesCollector implements NodeVisitor {
 		
 		private Set<Node> m_Nodes;
 		
 		public OpenNodesCollector() {
 			m_Nodes = CollectionUtil.makeSet();
 		}
 		
 		public void reset() {
 			m_Nodes.clear();
 		}
 		
 		public Set<Node> getNodes() {
 			return m_Nodes;
 		}
 
 		@Override
 		public void visit(Node n) {
 			if (!n.isComplete() && !m_Blocking.isBlocked(n)) {
 				m_Nodes.add(n);
 			}
 		}
 		
 	}
 
 	private static class PruneNodesCollector implements NodeVisitor {
 		
 		private Set<Node> m_Nodes;
 		private BranchPoint m_RestoreTarget;
 		
 		public PruneNodesCollector() {
 			m_Nodes = CollectionUtil.makeSet();
 		}
 		
 		public void reset(BranchPoint restoreTarget) {
 			m_Nodes.clear();
 			m_RestoreTarget = restoreTarget;
 		}
 		
 		public Set<Node> getNodes() {
 			return m_Nodes;
 		}
 
 		@Override
 		public void visit(Node n) {
 			if (n.getDependency().hasSameOrAfter(m_RestoreTarget)) {
 				m_Nodes.add(n);
 			}
 		}
 		
 	}
 
 	private static class ConceptPruner implements NodeVisitor {
 		
 		private BranchPoint m_RestoreTarget;
 		
 		public void reset(BranchPoint restoreTarget) {
 			m_RestoreTarget = restoreTarget;
 		}
 		
 		@Override
 		public void visit(Node n) {
 			n.pruneAndReopenLabels(m_RestoreTarget);
 		}
 		
 	}
 
 }
