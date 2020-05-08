 /**
  * 
  */
 
 package edu.wustl.common.query.queryobject.util;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.querysuite.exceptions.CyclicException;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.queryobject.IConnector;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionOperand;
 import edu.wustl.common.querysuite.queryobject.IRule;
 import edu.wustl.common.querysuite.queryobject.LogicalOperator;
 import edu.wustl.common.querysuite.queryobject.impl.JoinGraph;
 import edu.wustl.common.querysuite.queryobject.impl.Rule;
 
 /**
  * @author prafull_kadam
  * This class will contain Utility methods for Query objects.
  */
 public class QueryObjectProcessor
 {
 
 	// Following attributes will be used by replaceMultipleParent method. So that they need not to pass for each method called by that routine.
 	/**
 	 * holds constraints object of the Query that is currently being processed.
 	 */
 	private IConstraints constraints;
 
 	/**
 	 * holds joinGraph object of the Query that is currently being processed
 	 */
 	private JoinGraph joinGraph;
 
 	/**
 	 * contains the expression ids of the expressions having more than one parent.
 	 */
 	private List<IExpression> exprsToProcess;
 
 	/**
	 * Default constructor, made it protected to keep its implementation as singleton class.
 	 *
 	 */
 	protected QueryObjectProcessor()
 	{
 	}
 
 	/**
 	 * if there are any nodes with multiple parents, then it converts Expression graph to the tree. And returns true if processed any such nodes. 
 	 * @param constraints The reference to constraints.
 	 * @return true if there are any such node present in constraints.
 	 */
 	public static boolean replaceMultipleParents(IConstraints constraints)
 	{
 		return new QueryObjectProcessor().replaceMultipleParent(constraints);
 	}
 
 	/**
 	 * if there are any nodes with multiple parents, then it converts Expression graph to the tree. 
 	 * @param constraints The reference to constraints.
 	 * @return true if there are any such node present in constraints.
 	 */
 	private boolean replaceMultipleParent(IConstraints constraints)
 	{
 		boolean isAnyNodeProcessed = false;
 		this.constraints = constraints;
 		this.joinGraph = (JoinGraph) constraints.getJoinGraph();
 		exprsToProcess = joinGraph.getNodesWithMultipleParents();
 		try
 		{
 			for (IExpression expression : exprsToProcess)
 			{
 				replaceMultipleParent(expression);
 				isAnyNodeProcessed = true;
 			}
 		}
 		catch (CyclicException exp)
 		{
 			throw new RuntimeException("Unable to Process object, Exception:" + exp.getMessage());
 		}
 
 		return isAnyNodeProcessed;
 	}
 
 	/**
 	 * Creates another node for the expression having multiple parent. It will also create heirarcy below that node.
	 * @param expression The Expression id having multiple parent.
 	 * @throws CyclicException when adding an Edge in graph causes cycle in the graph.
 	 */
 	private void replaceMultipleParent(IExpression expression) throws CyclicException
 	{
 		joinGraph = (JoinGraph) constraints.getJoinGraph();
 		List<IExpression> parents = joinGraph.getParentList(expression);
 		for (int index = 1; index < parents.size(); index++) // iterating on all parent expression ids.
 		{
 			IExpression parentExpression = parents.get(index);
 			int childIndex = parentExpression.indexOfOperand(expression);
 
 			IExpression newExpression = constraints.addExpression(expression.getQueryEntity()); // creating new expression which will be copy of the given expression id.
 			newExpression.setInView(expression.isInView());
 			parentExpression.setOperand(childIndex, newExpression); // pointing the parent expression to the new expression.
 
 			// changing associations.
 			IAssociation association = joinGraph.getAssociation(parentExpression, expression);
 			joinGraph.removeAssociation(parentExpression, expression);
 			joinGraph.putAssociation(parentExpression, newExpression, association);
 
			// copying all expression info to new expression, including child expression hierarchy.
 			copy(expression, newExpression);
 		}
 	}
 
 	/**
 	 * To copy the expression data to new expression. It will copy all rules/expressions/Logical connectors to new expression, except the expressions having multiple parents.
 	 * @param fromExpression the expression to be copied.
 	 * @param toExpression The new empty expression.
 	 * @throws CyclicException when adding an Edge in graph causes cycle in the graph.
 	 */
 	private void copy(IExpression fromExpression, IExpression toExpression) throws CyclicException
 	{
 		int numberOfOperands = fromExpression.numberOfOperands();
 		for (int index = 0; index < numberOfOperands; index++)
 		{
 
 			IExpressionOperand operand = fromExpression.getOperand(index);
 			if (operand instanceof IExpression)
 			{
 				IExpression oldExpression = (IExpression) operand;
 				IAssociation association = joinGraph.getAssociation(fromExpression, oldExpression);
 				if (exprsToProcess.contains(operand))
 				{
 					// this node also have multiple parent. So just adding this operand in new expression's operand list & updating joingraph.
 					toExpression.addOperand(operand);
 					joinGraph.putAssociation(toExpression, oldExpression, association);
					// this will be handled separately in method replaceMultipleParent.
 				}
 				else
 				{
 					// Create new Expression
 					IExpression newExpression = constraints.addExpression(oldExpression
 							.getQueryEntity());
 					newExpression.setInView(oldExpression.isInView());
 					toExpression.addOperand(newExpression);
 					joinGraph.putAssociation(toExpression, newExpression, association);
 					copy(oldExpression, newExpression);
 				}
 			}
 			else if (operand instanceof IRule)
 			{
 				// create copy of rule & adds it to the operand list of the new expression.
 				IRule fromRule = (IRule) operand;
 				IRule toRule = ((Rule) fromRule).getCopy();
 				toExpression.addOperand(toRule);
 			}
 
 			// setting logical connector.
 			if (index != 0)
 			{
 				IConnector<LogicalOperator> logicalConnector = fromExpression.getConnector(
 						index - 1, index);
 				toExpression.setConnector(index - 1, index, logicalConnector);
 			}
 		}
 
 	}
 
 	/**
 	 * To get map of all Children nodes along with their ids under given output tree node.
 	 * @param root The root noe of the output tree.
 	 * @return map of all Children nodes along with their ids under given output tree node.
 	 */
 	public static Map<Long, OutputTreeDataNode> getAllChildrenNodes(OutputTreeDataNode root)
 	{
 		Map<Long, OutputTreeDataNode> map = new HashMap<Long, OutputTreeDataNode>();
 		map.put(root.getId(), root);
 		List<OutputTreeDataNode> children = root.getChildren();
 		for (OutputTreeDataNode childNode : children)
 		{
 			map.putAll(getAllChildrenNodes(childNode));
 		}
 		return map;
 	}
 
 	/**
 	 * To get map of all Children nodes along with their ids under given output tree node.
 	 * @param root The root noe of the output tree.
 	 * @param map of all Children nodes along with their ids under given output tree node.
 	 */
 	private static void addAllChildrenNodes(OutputTreeDataNode root,
 			Map<String, OutputTreeDataNode> map)
 	{
 		map.put(root.getUniqueNodeId(), root);
 		List<OutputTreeDataNode> children = root.getChildren();
 		for (OutputTreeDataNode childNode : children)
 		{
 			addAllChildrenNodes(childNode, map);
 		}
 	}
 
 	/**
 	 * It returns all the nodes present all tress in results. 
 	 * @param keys set of trees
 	 * @return Map of uniqueNodeId and tree node
 	 */
 	public static Map<String, OutputTreeDataNode> getAllChildrenNodes(Set<OutputTreeDataNode> keys)
 	{
 		Map<String, OutputTreeDataNode> map = new HashMap<String, OutputTreeDataNode>();
 		for (OutputTreeDataNode root : keys)
 		{
 			addAllChildrenNodes(root, map);
 		}
 		return map;
 	}
 
 	/**
 	 * It returns all the nodes present all tress in results. 
 	 * @param keys set of trees
 	 * @return Map of uniqueNodeId and tree node
 	 */
 	public static Map<String, OutputTreeDataNode> getAllChildrenNodes(List<OutputTreeDataNode> keys)
 	{
 		Map<String, OutputTreeDataNode> map = new HashMap<String, OutputTreeDataNode>();
 		for (OutputTreeDataNode root : keys)
 		{
 			addAllChildrenNodes(root, map);
 		}
 		return map;
 	}
 }
