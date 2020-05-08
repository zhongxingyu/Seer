 
 package edu.wustl.query.domain;
 
 
 
 /**
 * This class contains all the detailed information to required for workflow execution.
 *
  * @author maninder_randhawa
 *
  */
 public class WorkflowDetails
 {
 
 	protected Workflow workflow;
 	protected int workFlowExecId;
 	protected DirectedGraph dependencyGraph;
 
 	public DirectedGraph getDependencyGraph()
 	{
 		return dependencyGraph;
 	}
 
 	public void setDependencyGraph(DirectedGraph dependencyGraph)
 	{
 		this.dependencyGraph = dependencyGraph;
 	}
 
 	public WorkflowDetails(Workflow workflow)
 	{
 		super();
 		this.workflow = workflow;
//		createGraph(workflow);
 	}
 
 	/**
 	 * This method creates dependency graph by parsing workflow object.
 	 * @param workflow
 	 */
 	protected void createGraph(Workflow workflow)
 	{
 	}
 
 	/**
 	 * gets workFlow Execution Id
 	 * @return workFlowExecId
 	 */
 	public int getWorkFlowExecId()
 	{
 		return workFlowExecId;
 	}
 
 	/**
 	 * sets workFlow Execution Id
 	 * @param workFlowExecId
 	 */
 	public void setWorkFlowExecId(int workFlowExecId)
 	{
 		this.workFlowExecId = workFlowExecId;
 	}
 
 	/**
 	 * gets workflow.
 	 * @return workflow
 	 */
 	public Workflow getWorkflow()
 	{
 		return workflow;
 	}
 
 	/**
 	 * sets workflow.
 	 * @param workflow
 	 */
 	public void setWrflow(Workflow workflow)
 	{
 		this.workflow = workflow;
 	}
 
 }
