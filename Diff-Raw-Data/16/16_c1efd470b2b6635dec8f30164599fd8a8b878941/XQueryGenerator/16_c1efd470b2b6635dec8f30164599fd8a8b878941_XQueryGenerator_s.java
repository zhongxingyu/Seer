 
 package edu.wustl.common.query.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BooleanTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DateTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DoubleTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.IntegerTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.LongTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.StringTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.entitymanager.DataTypeFactory;
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.wustl.common.query.exeptions.SQLXMLException;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.query.queryobject.impl.metadata.QueryOutputTreeAttributeMetadata;
 import edu.wustl.common.query.queryobject.util.InheritanceUtils;
 import edu.wustl.common.query.queryobject.util.QueryObjectProcessor;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.exceptions.SqlException;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.associations.IIntraModelAssociation;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.IExpression;
import edu.wustl.common.querysuite.queryobject.IOutputEntity;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.LogicalOperator;
 import edu.wustl.common.querysuite.queryobject.RelationalOperator;
import edu.wustl.common.querysuite.queryobject.impl.Expression;
 import edu.wustl.common.querysuite.queryobject.impl.JoinGraph;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.metadata.util.DyExtnObjectCloner;
 import edu.wustl.query.util.global.Constants;
import edu.wustl.query.util.querysuite.IQueryUpdationUtil;
 import edu.wustl.query.util.querysuite.QueryCSMUtil;
 
 public class XQueryGenerator extends QueryGenerator
 {
 
 	private int suffix = 0;
 	/**
 	 * the set of expressions whose entites have a separate XML file, where they are the root element
 	 */
 	private Set<IExpression> mainExpressions;
 
 	/**
 	 * map of expressions for entities and the xpath used to reach them
 	 * xpath could be a variable name or a path
 	 */
 	private Map<IExpression, String> entityPaths;
 
 	/**
 	 * the map of exprssions for which a variable is created in the for clause and the variables 
 	 */
 	private Map<IExpression, String> forVariables;
 
 	/**
 	 * the selected attributes (ie the ones going in SELECT part) and their aliases
 	 */
 	private Map<AttributeInterface, String> attributeAliases;
 
 	
 	private static org.apache.log4j.Logger logger =Logger.getLogger(XQueryGenerator.class);
 	/**
 	 * Generates SQL for the given Query Object.
 	 * 
 	 * @param query The Reference to Query Object.
 	 * @return the String representing SQL for the given Query object.
 	 * @throws MultipleRootsException 
 	 * @throws SqlException 
 	 * @see edu.wustl.common.querysuite.queryengine.ISqlGenerator#generateSQL(edu.wustl.common.querysuite.queryobject.IQuery)
 	 */
 
 	/*
 	 * The following function takes IQuery object as input for further processing
 	 * @parameters : IQuery query= The query object
 	 * @parameters : char QueryType = representing the kind of query whether aggregate or normal
 	 */
 	public String generateQuery(IQuery query) throws MultipleRootsException, SqlException
 	{
 		StringBuilder formedQuery = new StringBuilder();
 
 		try
 		{
 			IQuery queryClone = new DyExtnObjectCloner().clone(query);
 			// IQuery queryClone = query;
 			constraints = queryClone.getConstraints();
 			QueryObjectProcessor.replaceMultipleParents(constraints);
 			IExpression rootExpression = constraints.getRootExpression();
 
 			this.joinGraph = (JoinGraph) constraints.getJoinGraph();
 			aliasAppenderMap = new HashMap<IExpression, Integer>();
 			createAliasAppenderMap();
 			setMainExpressions();
 			createTree();
 
 			emptyExpressions = new HashSet<IExpression>();
 			checkForEmptyExpression(rootExpression.getExpressionId());
 			//isEmptyExpression(rootExpression.getExpressionId());
 
 			// Generating output tree.
 			//createTree();
 
 			formedQuery.append(buildSelectPart());
 			formedQuery.append(buildFromPart());
 
 		}
 		catch (SQLXMLException e)
 		{
 			throw new SqlException("problem while trying to build xquery", e);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			throw new SqlException("problem while trying to build xquery", e);
 		}
 		
 		log(formedQuery.toString());
 		return formedQuery.toString();
 	}
 	
 	
 	
 	private void log(String sql)
 	{
 		// TODO format.
 		try
 		{
 			new SQLLogger().log(sql);
 		}
 		catch (IOException e)
 		{
 			Logger.out.error("Error while logging sql.\n" + e);
 		}
 	}
 
 	/**
 	 * 
 	 * @return the From part of SQLXML
 	 * @throws SQLXMLException - Will be thrown when there is some SQLXML Exception
 	 * @throws DynamicExtensionsSystemException - Exception thrown by DynamicExtensions
 	 * @throws MultipleRootsException - thrown when there is more then one root element
 	 * @throws SqlException - Thrown when there is some SQL Exception
 	 */
 	private String buildFromPart() throws SQLXMLException, DynamicExtensionsSystemException,
 			MultipleRootsException, SqlException
 	{
 		StringBuilder fromPart = new StringBuilder();
 
 		fromPart.append(Constants.QUERY_FROM_XMLTABLE + Constants.QUERY_OPENING_PARENTHESIS + "'");
 		fromPart.append(buildXQuery());
 		fromPart.append("'");
 		fromPart.append(buildColumnsPart());
 		fromPart.append(Constants.QUERY_CLOSING_PARENTHESIS);
 
 		return fromPart.toString();
 	}
 
 	/**
 	 * To assign alias to each table name in the Expression. It will generate
 	 * alias that will be assigned to each entity in Expression.
 	 * 
 	 * @param expression the Root Expression of the Query.
 	 * @param currentAliasCount The count from which it will start to assign
 	 *            alias appender.
 	 * @param aliasToSet The alias to set for the current expression.
 	 * @param pathMap The map of path verses the ExpressionId. entry in this map
 	 *            means, for such path, there is already alias assigned to some
 	 *            Expression.
 	 * @return The integer representing the modified alias appender count that will
 	 *         be used for further processing.
 	 * @throws MultipleRootsException if there are multpile roots present in
 	 *             join graph.
 	 */
 	/*void createAliasAppenderMap() throws MultipleRootsException
 	{
 		for (IExpression expr : constraints)
 		{
 			aliasAppenderMap.put(expr, expr.getExpressionId());
 		}
 	}*/
 
 	/**
 	 * To check if the Expression is empty or not. It will simultaneously add
 	 * such empty expressions in the emptyExpressions set.
 	 * 
 	 * An expression is said to be empty when: - it contains no rule as operand. -
 	 * and all of its children(i.e subExpressions & their subExpressions & so
 	 * on) contains no rule
 	 * 
 	 * @param expressionId the reference to the expression id.
 	 * @return true if the expression is empty.
 	 */
 	/*private boolean isEmptyExpression(int expressionId)
 	{
 		Expression expression = (Expression) constraints.getExpression(expressionId);
 		List<IExpression> operandList = joinGraph.getChildrenList(expression);
 
 		boolean isEmpty = true;
 		if (!operandList.isEmpty()) // Check whether any of its children
 		// contains rule.
 		{
 			for (IExpression subExpression : operandList)
 			{
 				if (!isEmptyExpression(subExpression.getExpressionId()))
 				{
 					isEmpty = false;
 				}
 			}
 		}
 
 		isEmpty = isEmpty && !expression.containsRule();// check if there are
 		// rule present as
 		// subexpression.
 		// SRINATH
 		isEmpty = isEmpty && !expression.containsCustomFormula();
 		if (isEmpty)
 		{
 			emptyExpressions.add(expression); // Expression is empty.
 		}
 
 		return isEmpty;
 	}*/
 
 	/**
 	 * To create output tree for the given expression graph.
 	 * 
 	 * @throws MultipleRootsException When there exists multiple roots in
 	 *             joingraph.
 	 */
 	/*private void createTree() throws MultipleRootsException
 	{
 		IExpression rootExpression = joinGraph.getRoot();
 		rootOutputTreeNodeList = new ArrayList<OutputTreeDataNode>();
 		outputTreeNodeMap = new HashMap<Integer, OutputTreeDataNode>();
 		OutputTreeDataNode rootOutputTreeNode = null;
 		treeNo = 0;
 		if (rootExpression.isInView())
 		{
 			IOutputEntity rootOutputEntity = getOutputEntity(rootExpression);
 			rootOutputTreeNode = new OutputTreeDataNode(rootOutputEntity, rootExpression
 					.getExpressionId(), treeNo++);
 			rootOutputTreeNodeList.add(rootOutputTreeNode);
 			outputTreeNodeMap.put(aliasAppenderMap.get(rootExpression), rootOutputTreeNode);
 		}
 		completeTree(rootExpression, rootOutputTreeNode);
 	}*/
 
 	/**
 	 * TO create the output tree from the constraints.
 	 * 
 	 * @param expression The reference to Expression
 	 * @param parentOutputTreeNode The reference to parent output tree node.
 	 *            null if there is no parent.
 	 */
 	/*private void completeTree(IExpression expression, OutputTreeDataNode parentOutputTreeNode)
 	{
 		List<IExpression> children = joinGraph.getChildrenList(expression);
 		for (IExpression childExp : children)
 		{
 			OutputTreeDataNode childNode = parentOutputTreeNode;
 			/**
 			 * Check whether chid node is in view or not. if it is in view then
 			 * create output tree node for it. else look for their children node &
 			 * create the output tree heirarchy if required.
 			 
 			if (childExp.isInView())
 			{
 				IOutputEntity childOutputEntity = getOutputEntity(childExp);
 				Integer childAliasAppender = aliasAppenderMap.get(childExp);
 
 				/**
 				 * Check whether output tree node for expression with the same
 				 * alias already added or not. if its not added then need to add
 				 * it alias in the outputTreeNodeMap
 				 
 				childNode = outputTreeNodeMap.get(childAliasAppender);
 				if (childNode == null)
 				{
 					if (parentOutputTreeNode == null)
 					{
 						// New root node for output tree found, so create root
 						// node & add it in the rootOutputTreeNodeList.
 						childNode = new OutputTreeDataNode(childOutputEntity, childExp
 								.getExpressionId(), treeNo++);
 						rootOutputTreeNodeList.add(childNode);
 					}
 					else
 					{
 						childNode = parentOutputTreeNode.addChild(childOutputEntity, childExp
 								.getExpressionId());
 					}
 					outputTreeNodeMap.put(childAliasAppender, childNode);
 				}
 			}
 			completeTree(childExp, childNode);
 		}
 	}*/
 
 	/**
 	 * return the Select portion of SQLXML Query
 	 * throws
 	 * SQLXMLException
 	 */
 
 	private String buildSelectPart() throws SQLXMLException
 	{
 
 		StringBuilder selectClause = new StringBuilder(256);
 		selectClause.append(Constants.SELECT_DISTINCT);
 		attributeAliases = new HashMap<AttributeInterface, String>();
 		for (OutputTreeDataNode rootOutputTreeNode : attributeOutputTreeNodeList)
 		{
 			selectClause.append(setSelectedAtrributes(rootOutputTreeNode));
 		}
 		removeLastComma(selectClause);
 
 		return selectClause.toString();
 	}
 
 	/**
 	 * 
 	 * @return returns the XQuery formed from IQuery object
 	 * @throws SQLXMLException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws MultipleRootsException
 	 * @throws SqlException
 	 */
 	private String buildXQuery() throws SQLXMLException, DynamicExtensionsSystemException,
 			MultipleRootsException, SqlException
 
 	{
 		StringBuffer xQuery = new StringBuffer(1024);
 
 		xQuery.append(buildXQueryForClause());
 		//xQuery.append(buildXQueryLetClause());
 		xQuery.append(buildXQueryWhereClause());
 		xQuery.append(buildXQueryReturnClause());
 
 		return xQuery.toString();
 
 		//for repeated for's
 		/*for (IExpression expressions : constraints)
 		{
 			String XPath = "";
 			EntityInterface entity = expressions.getQueryEntity().getDynamicExtensionsEntity();
 			List<AssociationInterface> associationList = QueryCSMUtil
 					.getIncomingContainmentAssociations(entity);
 			if (!associationList.isEmpty())
 			{
 				for (AssociationInterface associations : associationList)
 				{
 					int i = associations.getTargetRole().getMaximumCardinality().getValue();
 					if (i > 1)
 					{
 						XPath = getAppropriatePath(entity, XPath, EntityList, actualPath);
 						xmlGetForQueryPart.append("$" + associations.getTargetEntity().getName()
 								+ " in " + XPath + "/" + associations.getTargetEntity().getName());
 					}
 				}
 				//xmlGetForQueryPart.append("$" + 
 			}
 
 		}*/
 
 	}
 
 	/**
 	 * 
 	 * @return the For Clause of XQuery
 	 * @throws MultipleRootsException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String buildXQueryForClause() throws MultipleRootsException,
 			DynamicExtensionsSystemException
 	{
 		entityPaths = new HashMap<IExpression, String>();
 		forVariables = new HashMap<IExpression, String>();
 
 		StringBuilder xqueryForClause = new StringBuilder(512);
 
 		xqueryForClause.append(Constants.QUERY_FOR);
 
 		for (IExpression mainExpression : mainExpressions)
 		{
 			String tableName = mainExpression.getQueryEntity().getDynamicExtensionsEntity()
 					.getTableProperties().getName();
 			String mainVariable = new StringBuilder().append(Constants.QUERY_DOLLAR).append(
 					getAliasName(mainExpression)).toString();
 
 			entityPaths.put(mainExpression, mainVariable);
 			forVariables.put(mainExpression, mainVariable);
 
 			String rootPath = new StringBuilder().append(Constants.QUERY_XMLCOLUMN).append(
 					Constants.QUERY_OPENING_PARENTHESIS).append("\"").append(tableName).append(
 					Constants.QUERY_DOT).append(Constants.QUERY_XMLDATA).append("\"").append(
 					Constants.QUERY_CLOSING_PARENTHESIS).append('/').append(
 					mainExpression.getQueryEntity().getDynamicExtensionsEntity().getName())
 					.toString();
 
 			xqueryForClause.append(mainVariable).append(' ').append(Constants.IN).append(' ')
 					.append(rootPath).append(Constants.QUERY_COMMA);
 
 			xqueryForClause.append(getForTree(mainExpression, mainVariable));
 		}
 
 		removeLastComma(xqueryForClause);
 		return xqueryForClause.toString();
 	}
 
 	/*
 	//for repeated for's
 	for (IExpression expressions : constraints)
 	{
 		EntityInterface entity = expressions.getQueryEntity().getDynamicExtensionsEntity();
 		List<AssociationInterface> associationList = QueryCSMUtil
 				.getIncomingContainmentAssociations(entity);
 		if (!associationList.isEmpty())
 		{
 			for (AssociationInterface associations : associationList)
 			{
 				int i = associations.getTargetRole().getMaximumCardinality().getValue();
 				if (i > 1)
 				{
 					XPath = getAppropriatePath(entity, XPath, EntityList, actualPath);
 					xmlGetForQueryPart.append("$" + associations.getTargetEntity().getName()
 							+ " in " + XPath + "/" + associations.getTargetEntity().getName());
 				}
 			}
 			//xmlGetForQueryPart.append("$" + 
 		}*/
 
 	private String buildXQueryLetClause()
 	{
 		StringBuilder xqueryLetClause = new StringBuilder();
 
 		xqueryLetClause.append(Constants.QUERY_LET);
 
 		for (Entry<IExpression, String> entry : entityPaths.entrySet())
 		{
 			IExpression expression = entry.getKey();
 			String path = entry.getValue();
 
 			for (AttributeInterface attribute : expression.getQueryEntity()
					.getDynamicExtensionsEntity().getAttributeCollection())
 			{
 				String attributeVariable = attributeAliases.get(attribute);
 
 				String statement = new StringBuilder().append(Constants.QUERY_DOLLAR).append(
 						attributeVariable).append(" := ").append(path).append('/').append(
 						attribute.getName()).append(Constants.QUERY_COMMA).toString();
 				xqueryLetClause.append(statement);
 			}
 
 		}
 
 		removeLastComma(xqueryLetClause);
 		return xqueryLetClause.toString();
 	}
 
 	/**
 	 * 
 	 * @return the Where portion of SQLXML
 	 * @throws SqlException
 	 * @throws MultipleRootsException
 	 * @throws SQLXMLException
 	 */
 	private String buildXQueryWhereClause() throws SqlException, MultipleRootsException,
 			SQLXMLException
 	{
 		StringBuilder xqueryWhereClause = new StringBuilder(Constants.WHERE);
 		IExpression parentExpression = joinGraph.getRoot();
 		String leftAlias = getAliasName(parentExpression);
 		Set<Integer> processedAlias = new HashSet<Integer>();
 
 		xqueryWhereClause.append(buildWherePart(constraints.getRootExpression(), null));
 		xqueryWhereClause.append(Constants.QUERY_AND);
 		//		xqueryWhereClause.append(getApplicationConditions());		
 		xqueryWhereClause.append(processChildExpressions(leftAlias, processedAlias,
 				parentExpression));
 
 		return removeLastAnd(xqueryWhereClause.toString());
 
 	}
 
 	/**
 	 * 
 	 * @return the Return Clause of SQLXML
 	 */
 	private String buildXQueryReturnClause()
 	{
 		StringBuilder xqueryReturnClause = new StringBuilder(Constants.QUERY_RETURN);
 		xqueryReturnClause.append("<return>");
 
 		for (String forVariable : forVariables.values())
 		{
 			String tagName = forVariable.substring(1);
 			xqueryReturnClause.append('<').append(tagName).append('>');
 			xqueryReturnClause.append('{').append(forVariable).append('}');
 			xqueryReturnClause.append("</").append(tagName).append('>');
 		}
 
 		xqueryReturnClause.append("</return>");
 		return xqueryReturnClause.toString();
 	}
 
 	/**
 	 * Adds an pseudo anded expression & all its child expressions to
 	 * pAndExpressions set.
 	 * 
 	 * @param expression pAnd expression
 	 */
 	/*private void addpAndExpression(IExpression expression)
 	{
 		List<IExpression> childList = joinGraph.getChildrenList(expression);
 		pAndExpressions.add(expression);
 
 		for (IExpression newExp : childList)
 		{
 			addpAndExpression(newExp);
 		}
 
 	}*/
 
 	/*
 	private String getAppropriatePath(EntityInterface entity, String path, List<String> EntityList,
 			String actualPath) throws DynamicExtensionsSystemException
 	{
 		String sourceEntity = "";
 		String tableName = "";
 		StringBuilder newActualPath = new StringBuilder(actualPath);
 		String newPath = path;
 
 		List<AssociationInterface> associationList = QueryCSMUtil
 				.getIncomingContainmentAssociations(entity);
 		if (!associationList.isEmpty())
 		{
 			for (AssociationInterface associations : associationList)
 			{
 				sourceEntity = associations.getEntity().getName();
 				if (EntityList.contains(sourceEntity))
 				{
 					try
 					{
 						tableName = entity.getTableProperties().getName();
 						if (tableName.substring(3).equalsIgnoreCase("DE_"))
 						{
 							newActualPath.append(sourceEntity);
 						}
 						else
 						{
 							newActualPath.insert(0, tableName);
 						}
 					}
 					catch (Exception e)
 					{
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 					newPath = "/" + sourceEntity;
 					EntityInterface newEntity = associations.getEntity();
 					newActualPath.append(newPath);
 					newActualPath.insert(0, getAppropriatePath(newEntity, newPath, EntityList,
 							newActualPath.toString()));
 
 				}
 			}
 		}
 		else
 		{
 			tableName = entity.getTableProperties().getName();
 			newActualPath.insert(0, tableName);
 		}
 
 		return newActualPath.toString();
 	}*/
 
 	/**
 	 * the recursive method to build multiple for parts that are required
 	 * in case of one to many associations.
 	 * 
 	 * it returns the appropriate path fragment for the given entity's child
 	 * hierarchy
 	 * 
 	 * @param expression the expression whose children are to  be processed
 	 * @param xpath the xpath or variable to be used as the parent xpath
 	 * @return the complete for expression for the children of the given expression
 	 *   
 	 */
 	private String getForTree(IExpression expression, String xpath)
 			throws DynamicExtensionsSystemException
 	{
 		StringBuilder forTree = new StringBuilder();
 
 		for (IExpression childExpression : joinGraph.getChildrenList(expression))
 		{
 			//skip main expressions
 			if (mainExpressions.contains(childExpression))
 			{
 				continue;
 			}
 
 			String childEntityName = deCapitalize(childExpression.getQueryEntity()
 					.getDynamicExtensionsEntity().getName());
 
 			IAssociation association = joinGraph.getAssociation(expression, childExpression);
 
 			AssociationInterface eavAssociation = ((IIntraModelAssociation) association)
 					.getDynamicExtensionsAssociation();
 
 			int cardinality = eavAssociation.getTargetRole().getMaximumCardinality().getValue();
 
 			if (cardinality > 1)
 			{
 				String variableName = new StringBuilder().append(Constants.QUERY_DOLLAR).append(
 						getAliasName(childExpression)).toString();
 				forTree.append(variableName).append(' ').append(Constants.IN).append(' ');
 				forTree.append(xpath).append('/').append(eavAssociation.getTargetRole().getName())
 						.append('/').append(childEntityName).append(Constants.QUERY_COMMA);
 
 				entityPaths.put(childExpression, variableName);
 				forVariables.put(childExpression, variableName);
 
 				forTree.append(getForTree(childExpression, variableName));
 			}
 			else
 			{
 				String newXPath = new StringBuilder(xpath).append('/').append(eavAssociation.getTargetRole().getName())
 						.toString();
 
 				entityPaths.put(childExpression, newXPath);
 				forTree.append(getForTree(childExpression, newXPath));
 			}
 		}
 
 		return forTree.toString();
 
 	}
 
 	/*
 	 StringBuilder forTree = new StringBuilder();
 	EntityInterface parentEntity = null;
 
 	List<AssociationInterface> associationList = QueryCSMUtil
 			.getIncomingContainmentAssociations(entity);
 	if (!associationList.isEmpty())
 	{
 		for (AssociationInterface association : associationList)
 		{
 			parentEntity = association.getEntity();
 			forTree.append(Constants.QUERY_FOR).append(Constants.QUERY_DOLLAR);
 			forTree.append(entityName);
 			forTree.append(Constants.IN).append(Constants.QUERY_DOLLAR);
 			forTree.append(parentEntity.getName());
 			forTree.append("/");
 			forTree.append(entityName);
 			forTree.insert(0, getForTree(parentEntity, rootVariable));
 
 		}
 	}
 	else
 	{
 		forTree.append(Constants.QUERY_FOR).append(Constants.QUERY_DOLLAR).append(entityName)
 				.append(Constants.IN).append(rootVariable).append("/").append(entityName);
 	}
 
 	return forTree.toString();*/
 
 	/**
 	 * return It will change the first letter of the Entity Name 
 	 * to lower case
 	 */
 	private String deCapitalize(String name)
 	{
 		StringBuilder builder = new StringBuilder(name);
 		String firstLetter = name.substring(0, 1).toLowerCase();
 		builder.replace(0, 1, firstLetter);
 		return builder.toString();
 	}
 
 	/**
 	 * 
 	 * @return The additional condition for Activity status,Research Opted, 
 	 * effective Start timestamp and effective End timestamp
 	 * @throws SQLXMLException
 	 */
 	private String getApplicationConditions() throws SQLXMLException
 	{
 		return "";
 		/*
 		StringBuilder operandRule = new StringBuilder();
 
 		operandRule.append(" ");
 			operandRule.append(Variables.properties
 					.getProperty("xquery.wherecondition.activeUpiFlag"));
 			operandRule.append(Constants.QUERY_AND);
 			operandRule.append(Variables.properties
 					.getProperty("xquery.wherecondition.researchOptOut"));
 			operandRule.append(Constants.QUERY_AND);
 		
 			operandRule.append(Variables.properties
 					.getProperty("xquery.wherecondition.person.startTimeStamp"));
 			operandRule.append(Constants.QUERY_AND);
 			operandRule.append(Variables.properties
 					.getProperty("xquery.wherecondition.person.endTimeStamp"));
 			operandRule.append(" ");
 		
 		return operandRule.toString(); */
 	}
 
 	/*
 	private String getActualValue(String operator, String attributeName, String value)
 	{
 		StringBuilder newOperator = new StringBuilder();
 
 		if (operator.equalsIgnoreCase("is NOT NULL"))
 		{
 			newOperator.append("exists").append(Constants.QUERY_OPENING_PARENTHESIS).append(
 					Constants.QUERY_DOLLAR).append(attributeName).append(
 					Constants.QUERY_CLOSING_PARENTHESIS);
 
 		}
 		else if (operator.equalsIgnoreCase("Contains"))
 		{
 			newOperator.append("contains").append(Constants.QUERY_OPENING_PARENTHESIS).append(
 					Constants.QUERY_DOLLAR).append(attributeName).append(Constants.QUERY_COMMA)
 					.append("\"").append(value).append("\"").append(
 							Constants.QUERY_CLOSING_PARENTHESIS);
 
 		}
 		else if (operator.equalsIgnoreCase("StartsWith"))
 		{
 			newOperator.append("starts-with").append(Constants.QUERY_OPENING_PARENTHESIS).append(
 					Constants.QUERY_DOLLAR).append(attributeName).append(Constants.QUERY_COMMA)
 					.append("\"").append(value).append("\"").append(
 							Constants.QUERY_CLOSING_PARENTHESIS);
 
 		}
 		else if (operator.equalsIgnoreCase("EndsWith"))
 		{
 			newOperator.append("ends-with").append(Constants.QUERY_OPENING_PARENTHESIS).append(
 					Constants.QUERY_DOLLAR).append(attributeName).append(Constants.QUERY_COMMA)
 					.append("\"").append(value).append("\"").append(
 							Constants.QUERY_CLOSING_PARENTHESIS);
 
 		}
 
 		return newOperator.toString();
 	}
 	
 	*/
 
 	/**
 	 * @return Will modify the DataType depending on input
 	 * according to the database
 	 */
 	protected String modifyValueForDataType(String value, AttributeTypeInformationInterface dataType)
 	{
 		StringBuilder actualValue = new StringBuilder();
 
 		if (dataType instanceof StringTypeInformationInterface)
 		{
 			actualValue.append("\"").append(value).append("\"");
 		}
 		else if (dataType instanceof DateTypeInformationInterface)
 		{
 			String actualYear = value.substring(6);
 			String actualMonth = value.substring(0, 2);
 			String actualDate = value.substring(3, 5);
 			StringBuilder newValue = new StringBuilder(actualYear);
 			newValue.append("-");
 			newValue.append(actualMonth);
 			newValue.append("-");
 			newValue.append(actualDate);
 			actualValue.append("xs:dateTime(\"").append(newValue.toString()).append("T23:59:59\")");
 		}
 		else
 		{
 			actualValue.append(value);
 		}
 		return actualValue.toString();
 	}

 	/**
 	 * @return Create a set of expressions corresponding to the root Element
 	 * of each XML
 	 */
 	private void setMainExpressions()
 	{
 		mainExpressions = new HashSet<IExpression>();
 
 		for (IExpression expression : constraints)
 		{
 			List<EntityInterface> mainEntityList = new ArrayList<EntityInterface>();
 			EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 
 			List<EntityInterface> mainEntities = QueryCSMUtil.getAllMainEntities(entity,
 					mainEntityList);
 
 			if (mainEntities.contains(expression.getQueryEntity().getDynamicExtensionsEntity()))
 			{
 				mainExpressions.add(expression);
 			}
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param treeNode
 	 * @return the set of attributes in the select clause
 	 */
 	private String setSelectedAtrributes(OutputTreeDataNode treeNode)
 	{
 		StringBuilder selectPart = new StringBuilder();
 		IExpression expression = constraints.getExpression(treeNode.getExpressionId());
 
 		Collection<AttributeInterface> attributes = expression.getQueryEntity()
				.getDynamicExtensionsEntity().getAttributeCollection();
 
 		for (AttributeInterface attribute : attributes)
 		{
 			String attributeAlias = getAliasFor(attribute, expression);
 			attributeAliases.put(attribute, attributeAlias);
 			selectPart.append(attributeAlias);
 			String columnAliasName = Constants.QUERY_COLUMN_NAME + suffix;
 			selectPart.append(" " + columnAliasName + Constants.QUERY_COMMA);
 			// code to get displayname. & pass it to the Constructor along with
 			// treeNode.
 			String displayNameForColumn = Utility.getDisplayNameForColumn(attribute);
 			treeNode.addAttribute(new QueryOutputTreeAttributeMetadata(attribute, columnAliasName,
 					treeNode, displayNameForColumn));
 			attributeColumnNameMap.put(attribute, columnAliasName);
 			suffix++;
 		}
 		return selectPart.toString();
 	}
 
 	/**
 	 * 
 	 * @return Columns part of SQLXML
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	private String buildColumnsPart() throws DataTypeFactoryInitializationException
 	{
 		StringBuilder columnsPart = new StringBuilder(512);
 		columnsPart.append(" columns ");
 
 		for (Entry<IExpression, String> entry : entityPaths.entrySet())
 		{
 			String entityPath = buildEntityPath(entry);
 
 			for (AttributeInterface attribute : entry.getKey().getQueryEntity()
					.getDynamicExtensionsEntity().getAttributeCollection())
 			{
 				String attributePath = new StringBuilder(entityPath).append('/').append(
 						attribute.getName()).toString();
 //				String attributeAlias = attributeAliases.get(attribute);
 				String attributeAlias = getAliasFor(attribute, entry.getKey());
 				String dataType = getDataTypeInformation(attribute);
 				columnsPart.append(attributeAlias).append(' ').append(dataType).append(" path '")
 						.append(attributePath).append("'").append(Constants.QUERY_COMMA);
 
 			}
 		}
 
 		removeLastComma(columnsPart);
 		return columnsPart.toString();
 	}
 
 	/**
 	 * 
 	 * @param attribute object of AttributeInterface
 	 * @return The datatype of column
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	private String buildEntityPath(Entry<IExpression, String> entry)
 	{
 		IExpression expression = entry.getKey();
 
 		StringBuilder path = new StringBuilder(entry.getValue()).delete(0, 1);
 		String mainEntityName = path.substring(0, path.indexOf("_"));
 
 		List<IExpression> parents = joinGraph.getParentList(expression);
 
 		//whether to deCapitalize the entity name or not
 		if (!parents.isEmpty())
 		{
 			while (true)
 			{
 				if (forVariables.containsKey(expression))
 				{
 					if (!mainExpressions.contains(expression))
 					{
 						mainEntityName = deCapitalize(mainEntityName);
 					}
 
 					break;
 				}
 
 				expression = joinGraph.getParentList(expression).get(0);
 			}
 		}
 
 		int insertIndex = path.indexOf("/");
 		if (insertIndex == -1)
 		{
 			insertIndex = path.length();
 		}
 
 		path = path.insert(insertIndex, '/' + mainEntityName);
 		return path.toString();
 
 	}
 
 	private String getDataTypeInformation(AttributeInterface attribute)
 			throws DataTypeFactoryInitializationException
 	{
 		String returnValue = null;
 
 		DataTypeFactory type = DataTypeFactory.getInstance();
 		AttributeTypeInformationInterface dataType = attribute.getAttributeTypeInformation();
 
 		if (dataType instanceof StringTypeInformationInterface)
 		{
 			returnValue = type
 					.getDatabaseDataType(EntityManagerConstantsInterface.STRING_ATTRIBUTE_TYPE);
 		}
 		else if (dataType instanceof DateTypeInformationInterface)
 		{
 			returnValue = type
 					.getDatabaseDataType(EntityManagerConstantsInterface.DATE_TIME_ATTRIBUTE_TYPE);
 		}
 		else if (dataType instanceof LongTypeInformationInterface)
 		{
 			returnValue = type
 					.getDatabaseDataType(EntityManagerConstantsInterface.LONG_ATTRIBUTE_TYPE);
 		}
 		else if (dataType instanceof DoubleTypeInformationInterface)
 		{
 			returnValue = type
 					.getDatabaseDataType(EntityManagerConstantsInterface.DOUBLE_ATTRIBUTE_TYPE);
 		}
 		else if (dataType instanceof IntegerTypeInformationInterface)
 		{
 			returnValue = type
 					.getDatabaseDataType(EntityManagerConstantsInterface.INTEGER_ATTRIBUTE_TYPE);
 		}
 		else if (dataType instanceof BooleanTypeInformationInterface)
 		{
 			returnValue = type
 					.getDatabaseDataType(EntityManagerConstantsInterface.BOOLEAN_ATTRIBUTE_TYPE);
 		}
 
 		return returnValue;
 	}
 
 	@Override
 	protected String getConditionAttributeName(AttributeInterface attribute, IExpression expression)
 	{
 		return entityPaths.get(expression) + '/' + attribute.getName();
 	}
 
 	private String getAliasFor(AttributeInterface attribute, IExpression expression)
 	{
 		return attribute.getName() + "_" + expression.getExpressionId();
 	}
 
 	@Override
 	protected String getDescriminatorCondition(EntityInterface entity, String aliasFor)
 	{
 		//for the time being null is sufficient 
 		return null;
 	}
 
 	protected boolean shouldAddNodeFor(IExpression expression)
 	{
 		return super.shouldAddNodeFor(expression) ;
 	}
 	
 	protected boolean isContainedExpresion(int expressionId)
 	{ 
 		boolean isMainExpression = false;
 		for (IExpression  exp: mainExpressions)
 		{
 			if(exp.getExpressionId()==expressionId)
 			{
 				isMainExpression = true;
 				break;
 			}
 		}
 		return !(isMainExpression);
 	}
 
 	protected String processBetweenOperator(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		StringBuilder builder = new StringBuilder("(");
 		List<String> values = condition.getValues();
 
 		if (values.size() != 2)
 		{
 			throw new SqlException("Incorrect number of operand for Between oparator in condition:"
 					+ condition);
 		}
 
 		AttributeTypeInformationInterface dataType = condition.getAttribute()
 				.getAttributeTypeInformation();
 
 		if (!(dataType instanceof DateTypeInformationInterface
 				|| dataType instanceof IntegerTypeInformationInterface
 				|| dataType instanceof LongTypeInformationInterface || dataType instanceof DoubleTypeInformationInterface))
 		{
 			throw new SqlException(
 					"Incorrect Data type of operand for Between oparator in condition:" + condition);
 		}
 
 		String firstValue = modifyValueForDataType(values.get(0), dataType);
 		String secondValue = modifyValueForDataType(values.get(1), dataType);
 
 		builder.append(attributeName);
 		builder.append("[. " + RelationalOperator.getSQL(RelationalOperator.GreaterThanOrEquals)
 				+ firstValue);
 
 		builder.append(" " + LogicalOperator.And.toString().toLowerCase() + " . "
 				+ RelationalOperator.getSQL(RelationalOperator.LessThanOrEquals) + secondValue
 				+ "])");
 
 		return builder.toString();
 
 	}
 
 	protected String processInOperator(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		StringBuilder builder = new StringBuilder(attributeName).append(' ').append(" = ").append(
 				' ').append(Constants.QUERY_OPENING_PARENTHESIS);
 
 		for (String value : condition.getValues())
 		{
 			AttributeTypeInformationInterface dataType = condition.getAttribute()
 			.getAttributeTypeInformation();
 			if(dataType instanceof StringTypeInformationInterface)
 			{
 				builder.append("\"").append(value).append("\"").append(Constants.QUERY_COMMA);
 			}
 			else
 			{
 				builder.append(value).append(Constants.QUERY_COMMA);
 			}
 		}
 
 		removeLastComma(builder);
 		builder.append(Constants.QUERY_CLOSING_PARENTHESIS);
 
 		if (condition.getRelationalOperator().equals(RelationalOperator.NotIn))
 		{
 			builder.insert(0, Constants.QUERY_OPENING_PARENTHESIS).insert(0, "not").append(
 					Constants.QUERY_CLOSING_PARENTHESIS);
 		}
 
 		return builder.toString();
 
 	}
 
 	protected String processNullCheckOperators(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		RelationalOperator operator = condition.getRelationalOperator();
 		StringBuilder builder = new StringBuilder();
 
 		if (operator.equals(RelationalOperator.IsNotNull))
 		{
 			builder.append("exists");
 		}
 		else if (operator.equals(RelationalOperator.IsNull))
 		{
 			builder.append("empty");
 		}
 
 		builder.append(Constants.QUERY_OPENING_PARENTHESIS).append(attributeName).append(
 				Constants.QUERY_CLOSING_PARENTHESIS);
 
 		return builder.toString();
 	}
 
 	protected String processLikeOperators(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		RelationalOperator operator = condition.getRelationalOperator();
 		String newOperator = null;
 		String value = condition.getValue();
 
 		if (operator.equals(RelationalOperator.Contains))
 		{
 			newOperator = "contains(string(" + attributeName + "),\"" + value + "\")";
 		}
 		else if (operator.equals(RelationalOperator.StartsWith))
 		{
 			newOperator = "starts-with(string(" + attributeName + "),\"" + value + "\")";
 		}
 		else if (operator.equals(RelationalOperator.EndsWith))
 		{
 			newOperator = "ends-with(string(" + attributeName + "),\"" + value + "\")";
 		}
 
 		return newOperator;
 
 	}
 
 	/**
 	 * To process all child expression of the given Expression & get their SQL
 	 * representation for where part.
 	 * 
 	 * @param leftAlias left table alias in join.
 	 * @param processedAlias The list of precessed alias.
 	 * @param parentExpressionId The reference to expression whose children to
 	 *            be processed.
 	 * @return the left join sql for children expression.
 	 * @throws SqlException when there is error in the passed IQuery object.
 	 */
 	private String processChildExpressions(String leftAlias, Set<Integer> processedAlias,
 			IExpression parentExpression) throws SqlException
 	{
 		StringBuffer buffer = new StringBuffer();
 		List<IExpression> children = joinGraph.getChildrenList(parentExpression);
 		if (!children.isEmpty())
 		{
 			// processing all outgoing edges/nodes from the current node in the
 			// joingraph.
 			for (IExpression childExpression : children)
 			{
 				if (mainExpressions.contains(childExpression))
 				{
 					IAssociation association = joinGraph.getAssociation(parentExpression,
 							childExpression);
 					AssociationInterface actualEavAssociation = ((IIntraModelAssociation) association)
 							.getDynamicExtensionsAssociation();
 					AssociationInterface eavAssociation = actualEavAssociation;
 					EntityInterface rightEntity = eavAssociation.getTargetEntity();
 					String actualRightAlias = getAliasFor(childExpression, rightEntity);
 					if (!processedAlias.contains(aliasAppenderMap.get(childExpression)))
 					{
 						if (InheritanceUtils.getInstance().isInherited(eavAssociation))
 						{
 							eavAssociation = InheritanceUtils.getInstance().getActualAassociation(
 									eavAssociation);
 							rightEntity = eavAssociation.getTargetEntity();
 							leftAlias = getAliasFor(parentExpression, eavAssociation.getEntity());
 							String rightAlias = getAliasFor(childExpression, eavAssociation
 									.getTargetEntity());
 						}
 						else
 						{
 							leftAlias = getAliasFor(parentExpression, eavAssociation.getEntity());
 						}
 
 						EntityInterface leftEntity = eavAssociation.getEntity();
 
 						ConstraintPropertiesInterface constraintProperties = eavAssociation
 								.getConstraintProperties();
 						if (constraintProperties.getSourceEntityKey() != null
 								&& constraintProperties.getTargetEntityKey() != null)
 						// Many to Many case
 						{
 
 							String leftAttribute = null;
 							String rightAttribute = null;
 
 							String middleTableName = constraintProperties.getName();
 							String middleTableAlias = getAliasForMiddleTable(childExpression,
 									middleTableName);
 
 							AttributeInterface primaryKey = getPrimaryKey(leftEntity);
 							leftAttribute = "$" + getAliasName(parentExpression) + "/"
 									+ primaryKey.getColumnProperties().getName();
 
 							rightAttribute = "$" + middleTableAlias + "/"
 									+ constraintProperties.getSourceEntityKey();
 							buffer.append("(" + leftAttribute + "=" + rightAttribute);
 
 							buffer.append(")");
 
 							// Forming join with child table.
 							leftAttribute = "$" + middleTableAlias + "/"
 									+ constraintProperties.getTargetEntityKey();
 							primaryKey = getPrimaryKey(rightEntity);
 							rightAttribute = "$" + getAliasName(childExpression) + "/"
 									+ primaryKey.getColumnProperties().getName();
 
 							buffer.append("(" + leftAttribute + "=" + rightAttribute);
 
 							buffer.append(")");
 						}
 						else
 						{
 							String leftAttribute = null;
 							String rightAttribute = null;
 							if (constraintProperties.getSourceEntityKey() != null)// Many
 							// Side
 							{
 								leftAttribute = "$" + getAliasName(parentExpression) + "/"
 										+ constraintProperties.getSourceEntityKey();
 								List<String> primaryKeyList = edu.wustl.query.util.global.Utility
 										.getPrimaryKey(rightEntity);
 								String primaryKey = (String) primaryKeyList.get(0);
 								rightAttribute = "$" + getAliasName(childExpression) + "/"
 										+ primaryKey;
 							}
 							else
 							// One Side
 							{
 								List<String> primaryKeyList = edu.wustl.query.util.global.Utility
 										.getPrimaryKey(leftEntity);
 								String primaryKey = (String) primaryKeyList.get(0);
 								leftAttribute = "$" + getAliasName(parentExpression) + "/"
 										+ primaryKey;
 								rightAttribute = "$" + getAliasName(childExpression) + "/"
 										+ constraintProperties.getTargetEntityKey();
 							}
 							buffer.append("(" + leftAttribute + "=" + rightAttribute);
 							buffer.append(")");
 						}
 						buffer.append(Constants.QUERY_AND);
 					}
 					// append from part SQLXML for the next Expressions.
 					buffer.append(processChildExpressions(actualRightAlias, processedAlias,
 							childExpression));
 				}
 				else
 				{
 					continue;
 				}
 			}
 		}
 		return buffer.toString();
 	}
 
 	private String getAliasForMiddleTable(IExpression childExpression, String middleTableName)
 	{
 		return getAliasForClassName(middleTableName) + "_" + aliasAppenderMap.get(childExpression);
 	}
 
 }
