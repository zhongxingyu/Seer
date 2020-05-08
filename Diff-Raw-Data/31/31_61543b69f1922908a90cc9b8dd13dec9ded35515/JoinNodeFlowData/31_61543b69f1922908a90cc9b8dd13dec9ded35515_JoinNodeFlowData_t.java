 package xsched.analysis.wala.schedule_extraction;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import com.ibm.wala.ssa.ISSABasicBlock;
 
 public final class JoinNodeFlowData extends NormalNodeFlowData {
 
 	private static final boolean DEBUG = false;
 	
 	private final EdgeFlowData[] incoming;
 	
 	JoinNodeFlowData(ISSABasicBlock basicBlock, int numIncomingEdges) {
 		super(basicBlock);
 		//this constructor is called when the initial flow data instances are created; so elements in incoming[] can be null before the meet operation ran
 		incoming = new EdgeFlowData[numIncomingEdges];
 	}
 	
 	JoinNodeFlowData(ISSABasicBlock basicBlock, EdgeFlowData[] incoming) {
 		super(basicBlock);
 		this.initEmpty();
 		this.incoming = incoming;
 		
 		//we do not create a new happensBeforeMap because we have to do the intersection of all
 		//and for that we need the null as a flag of "nothing happened yet"		
 		if(DEBUG)
 			System.out.println("JoinNodeFlowData: joining " + basicBlock);
 		for(int i = 0; i < incoming.length; i++) {
 			EdgeFlowData edge = incoming[i];
 			assert edge != null; //should not happen after we ran the meet operator
 			this.mergeState(edge);			
 		}
 		
 		//now we unioned all edges; check that for all edges if an incoming data "knows" about lhs and rhs, it also knows the edge;
 		//otherwise they disagree and we can't keep the edge
 		HashSet<HappensBeforeEdge> hbedges = new HashSet<HappensBeforeEdge>(this.happensBeforeEdges);
 		for(int i = 0; i < incoming.length; i++) {
 			EdgeFlowData edge = incoming[i];			
 			this.filterUnreliableEdges(edge, hbedges);			
 		}
 	}
 	
 	@Override
 	JoinNodeFlowData duplicate(ISSABasicBlock forBasicBlock) {
 		JoinNodeFlowData data = new JoinNodeFlowData(forBasicBlock, incoming);
 		data.copyState(this);
 		return data;
 	}
 	
 	@Override
 	public NormalNodeVisitor createNodeVisitor() {
 		return new JoinNodeVisitor(this);
 	}
 	
 	EdgeFlowData incomingEdgeAtPosition(int pos) {
 		return incoming[pos];
 	}
 	
 	NormalNodeFlowData incomingDataAtPosition(int pos) {
 		EdgeFlowData edge = incoming[pos];
 		if(edge != null) {
 			NormalNodeFlowData data = edge.getData();
 			assert ! data.isInitial();
 			return data;
 		} else {
 			return null;
 		}
 	}
 	
 	void addPhiVariable(PhiVariable phi, TaskVariable task) {
 		if (phiMappings == null)
 			phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
 		
 		Set<TaskVariable> tasks = phiMappings.get(phi);
 		if(tasks == null) {
 			tasks = new HashSet<TaskVariable>();
 			phiMappings.put(phi, tasks);
 			
 		}
 		tasks.add(task);		
 	}
 		
 	@Override
 	boolean stateEquals(FlowData otherData) {
 		return super.stateEquals(otherData);
 	}
 	
	protected void filterUnreliableEdges(EdgeFlowData edge, HashSet<HappensBeforeEdge> hbedges) { //iterate a copy of my hb edges and check whether the incoming data agrees
 		if(!edge.isInitial()) {
 			NormalNodeFlowData other = edge.getData();
 			
 			for(HappensBeforeEdge hbEdge : hbedges) {
 				if(other.scheduledTasks.contains(hbEdge.lhs) && other.scheduledTasks.contains(hbEdge.rhs)) {
 					if(! other.happensBeforeEdges.contains(hbEdge))
 						this.happensBeforeEdges.remove(hbEdge);
 				}				
 			}
 		}
 	}
 	
 	//called in the constructor
 	protected void mergeState(EdgeFlowData edge) {
 		
 		if(!edge.isInitial()) {
 			NormalNodeFlowData other = edge.getData();
 			assert ! other.isInitial();		
 			
 			this.loopContexts.addAll(other.loopContexts);
 			this.scheduledTasks.addAll(other.scheduledTasks);
 			this.happensBeforeEdges.addAll(other.happensBeforeEdges);
 			
 			if (other.phiMappings != null) {
 				for(Entry<PhiVariable, Set<TaskVariable>> entry : other.phiMappings.entrySet()) {
 					this.addAllPhiVariables(entry.getKey(), entry.getValue());
 				}
 			}
 			
 			//we saw this edge, so add it to the list of loop contexts
 			if(edge instanceof BackEdgeFlowData) {
 				BackEdgeFlowData backEdge = (BackEdgeFlowData)edge;
 				for(LoopContext lc : this.loopContexts) {
 					this.loopContexts.add(lc.contextByAddingLoop(backEdge));
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public void copyState(FlowData v) {
 		super.copyState(v);
 		assert(v instanceof JoinNodeFlowData);
 		JoinNodeFlowData other = (JoinNodeFlowData)v;
 		assert other.incoming.length == incoming.length;
 		
 		for(int i = 0; i < incoming.length; i++) {
 			incoming[i] = other.incoming[i];
 		}
 		
 	}
 	
 	@Override
 	public String toString() {
 		return "Join-" + super.toString();
 	}
 }
