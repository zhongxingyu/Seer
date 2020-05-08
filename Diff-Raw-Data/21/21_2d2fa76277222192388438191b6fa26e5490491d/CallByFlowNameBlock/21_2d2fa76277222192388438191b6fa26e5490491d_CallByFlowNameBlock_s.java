 package org.neuro4j.logic.def.node;
 
 import java.util.List;
 import java.util.Map;
 
 import org.neuro4j.core.Entity;
 import org.neuro4j.logic.LogicContext;
 import org.neuro4j.logic.LogicProcessor;
 import org.neuro4j.logic.LogicProcessorException;
 import org.neuro4j.logic.def.FlowSet;
 import org.neuro4j.logic.def.LogicBlock;
 import org.neuro4j.logic.def.StartNodeAdapter;
 import org.neuro4j.logic.swf.FlowExecutionException;
 import org.neuro4j.logic.swf.FlowInitializationException;
 import org.neuro4j.logic.swf.SWFConstants;
 import org.neuro4j.logic.swf.SWFParametersConstants;
 import org.neuro4j.logic.swf.SimpleWorkflowEngine;
 
 public class CallByFlowNameBlock extends LogicBlock {
 	
 	private String flownName = null;
 	private String dynamicFlownName = null;
 
 	public CallByFlowNameBlock() {
 		super();
 	}
 
 	public CallByFlowNameBlock(String name) {
 		super();
 		lba.setName(name);
 		lba.setProperty(SWFConstants.N4J_CONSOLE_NODE_TYPE, "star"); 
 	}
 
 	final public void validate(LogicContext ctx) throws FlowExecutionException
 	{
 		
 		if (dynamicFlownName == null && flownName == null)
 		{
 			throw new FlowExecutionException("CallByFlowName node: Flow not defined.");
 		}		
 		
 			
 	}
 	
 
 	public int execute(LogicContext ctx) throws FlowExecutionException {
 		String flow = null;
 		if (dynamicFlownName != null )
 		{
 			flow = (String) ctx.get(dynamicFlownName);			
 		} else {
 			flow = flownName;
 		}
 
 		
 		if (flow == null)
 		{
 			throw new FlowExecutionException("CallNode: Flow not defined.");
 		}
 		
 		
 		
 		String[]  fArr = SimpleWorkflowEngine.parseFlowName(flow);
 		String flowName = fArr[0];
 		String startNodeName = fArr[1];
 
 		FlowSet network = SimpleWorkflowEngine.loadFlow(flowName);
 		if(network == null)
 		{
 			throw new FlowExecutionException(flowName +" not found.");
 		}
 			
 		StartNodeAdapter startNode = network.getStartNodeAdapter(startNodeName);
 		LogicProcessor logicProcessor = SimpleWorkflowEngine.getProcessor();
 		
 		checkNodeBeforeCall(startNode, ctx);
 		
 		if (startNode == null)
 		{
			throw new FlowExecutionException(startNodeName + " not found in flow " + flowName);
 		}
 		
 		
 		try {
 			
 			ctx.pushPackage(network.getFlowPackage());
 			
 			logicProcessor.action(startNode.getEntity(), network.getNetwork(), null, ctx);
 			
 			ctx.popPackage();
 		} catch (LogicProcessorException e1) {
 			 ctx.popPackage();
 		     throw new FlowExecutionException(e1.getCause());
 		}
 
 		Entity lastNode = (Entity) ctx.get(SWFConstants.AC_CURRENT_NODE);
 		
//		List<String> relations = getOutgoingRelationsUUID(lastNode.getName());
		List<String> relations = getOutgoingRelationsUUID(null);
 	
 		String nextActionUUID = null;
 		
		if(!relations.isEmpty())
 		{
 		    nextActionUUID = relations.get(0);
 			
 			if (nextActionUUID == null)
 			{
 				relations = getOutgoingRelationsUUID(SWFConstants.NEXT_RELATION_NAME);
 				if (relations != null && relations.size() == 1)
 				{
 					nextActionUUID = relations.get(0);
 				}
 			}
 			
		} 
 		
 		
 		if (nextActionUUID != null )
 		{
 			ctx.setNextRelation(nextActionUUID);
 		} else {
 			throw new FlowExecutionException("CallByFlowName node: Next Action is unknown.");
 		}
 		
 		return NEXT;
 	}
 	
 	private void checkNodeBeforeCall(StartNodeAdapter startNode, LogicContext ctx) throws FlowExecutionException
 	{
 	
 		
 		if (!startNode.isPublic())
 		{
 			if(!ctx.getCurrentPackage().equals(startNode.getPackage()))
 			{
				throw new FlowExecutionException("Node " + startNode.getName() + " in package " + startNode.getPackage() + " is private and can be used just inside package.");
 			}
 			
 		}
 	
 	}
 
 	public void load(Entity entity) throws FlowInitializationException
 	{
 		super.load(entity);
 		
 		flownName = getNotEmptyProperty(SWFParametersConstants.CAll_NODE_FLOW_NAME);
 		
 		dynamicFlownName = getNotEmptyProperty(SWFParametersConstants.CAll_NODE_DYNAMIC_FLOW_NAME);		
 
 	}
 
 
 }
