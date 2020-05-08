 /**
  * 
  */
 
 package edu.wustl.common.query.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DateTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DoubleTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.FileTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.IntegerTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.LongTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.StringTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.util.global.Constants.InheritanceStrategy;
 import edu.wustl.common.query.queryobject.impl.OutputTreeDataNode;
 import edu.wustl.common.query.queryobject.impl.metadata.QueryOutputTreeAttributeMetadata;
 import edu.wustl.common.query.queryobject.util.InheritanceUtils;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.exceptions.SqlException;
 import edu.wustl.common.querysuite.factory.QueryObjectFactory;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.associations.IIntraModelAssociation;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.ICustomFormula;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionAttribute;
 import edu.wustl.common.querysuite.queryobject.IExpressionOperand;
 import edu.wustl.common.querysuite.queryobject.IOutputEntity;
 import edu.wustl.common.querysuite.queryobject.IOutputTerm;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.IRule;
 import edu.wustl.common.querysuite.queryobject.ITerm;
 import edu.wustl.common.querysuite.queryobject.LogicalOperator;
 import edu.wustl.common.querysuite.queryobject.RelationalOperator;
 import edu.wustl.common.querysuite.queryobject.TermType;
 import edu.wustl.common.querysuite.queryobject.impl.Connector;
 import edu.wustl.common.querysuite.queryobject.impl.Expression;
 import edu.wustl.common.querysuite.queryobject.impl.JoinGraph;
 import edu.wustl.common.querysuite.utils.CustomFormulaProcessor;
 import edu.wustl.common.querysuite.utils.DatabaseSQLSettings;
 import edu.wustl.common.querysuite.utils.DatabaseType;
 import edu.wustl.common.querysuite.utils.TermProcessor;
 import edu.wustl.common.querysuite.utils.TermProcessor.IAttributeAliasProvider;
 import edu.wustl.common.querysuite.utils.TermProcessor.TermString;
 import edu.wustl.query.queryengine.impl.IQueryGenerator;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Utility;
 import edu.wustl.query.util.global.Variables;
 
 /**
  * @author juberahamad_patel
  *
  */
 public abstract class QueryGenerator implements IQueryGenerator
 {
 
 	/**
 	 * This map holds integer value that will be appended to each table alias in
 	 * the query.
 	 */
 	protected Map<IExpression, Integer> aliasAppenderMap = new HashMap<IExpression, Integer>();
 
 	/**
 	 * This map holds the alias name generated for each fully Qualified
 	 * className, where className id key & value is the aliasName generated for
 	 * that className.
 	 */
 	protected Map<String, String> aliasNameMap = new HashMap<String, String>();
 
 	/**
 	 * reference to the joingraph object present in the query object.
 	 */
 	protected JoinGraph joinGraph;
 
 	//PAND is not used anymore - juber 
 	//protected Set<IExpression> pAndExpressions;
 
 	/**
 	 * reference to the constraints object present in the query object.
 	 */
 	protected IConstraints constraints;
 
 	// Variables required for output tree.
 	/**
 	 * List of Roots of the output tree node.
 	 */
 	protected List<OutputTreeDataNode> rootOutputTreeNodeList;
 
 	// Variables required for output tree.
 	/**
 	 * List of Roots of the output tree node.
 	 */
 	protected List<OutputTreeDataNode> attributeOutputTreeNodeList;
 
 	/**
 	 * This map is used in output tree creation logic. It is map of alias
 	 * appender verses the output tree node. This map is used to ensure that no
 	 * duplicate output tree node is created for the expressions having same
 	 * alias appender.
 	 */
 	private Map<Integer, OutputTreeDataNode> outputTreeNodeMap;
 
 	/**
 	 * This map contains information about the tree node ids, attributes & their
 	 * correspoiding column names in the generated query. - Inner most map Map<AttributeInterface,
 	 * String> contains mapping of attribute interface verses the column name in
 	 * query. - The outer map Map<Long, Map<AttributeInterface, String>>
 	 * contains mapping of treenode Id verses the map in above step. This map
 	 * contains mapping required for one output tree. - The List contains the
 	 * mapping of all output trees that are formed by the query.
 	 */
 	// List<Map<Long, Map<AttributeInterface, String>>> columnMapList;
 	private int treeNo; // this count represents number of output trees formed.
 	private int allExpressionTreeNo;
 	protected Map<AttributeInterface, String> attributeColumnNameMap = new HashMap<AttributeInterface, String>();
 	protected boolean containsCLOBTypeColumn = false;
 
 	protected int selectIndex;
 
 	/**
 	 * This set will contain the expression ids of the empty expression. An
 	 * expression is empty expression when it does not contain any Rule & its
 	 * sub-expressions (also their subexpressions & so on) also does not contain
 	 * any Rule
 	 */
 	protected Set<IExpression> emptyExpressions;
 
 	private static final int ALIAS_NAME_LENGTH = 25;
 
 	protected Map<String, IOutputTerm> outputTermsColumns;
 
 	public QueryGenerator()
 	{
 		aliasAppenderMap = new HashMap<IExpression, Integer>();
 		emptyExpressions = new HashSet<IExpression>();
 
 	}
 
 	/**
 	 * @return the rootOutputTreeNodeList
 	 */
 	public List<OutputTreeDataNode> getRootOutputTreeNodeList()
 	{
 		return rootOutputTreeNodeList;
 	}
 
 	public Map<AttributeInterface, String> getAttributeColumnNameMap()
 	{
 		return attributeColumnNameMap;
 	}
 
 	public Map<String, IOutputTerm> getOutputTermsColumns()
 	{
 		return outputTermsColumns;
 	}
 
 	/**
 	 * build the where clause of the query
 	 * 
 	 * @param expression the Expression for which query is to be generated.
 	 * @param parentExpression The Parent Expression.
 	 * @return The query string
 	 * @throws SqlException When there is error in the passed IQuery object.
 	 */
 	protected String buildWherePart(IExpression expression, IExpression parentExpression)
 			throws SqlException
 	{
 		StringBuffer buffer = new StringBuffer();
 
 		EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 
 		if (parentExpression != null) // This will be true only for Expression
 		// which is not root Expression of the
 		// Query.
 		{
 			IAssociation association = joinGraph.getAssociation(parentExpression, expression);
 			AssociationInterface eavAssociation = ((IIntraModelAssociation) association)
 					.getDynamicExtensionsAssociation();
 
 			if (InheritanceUtils.getInstance().isInherited(eavAssociation))
 			{
 				eavAssociation = InheritanceUtils.getInstance().getActualAassociation(
 						eavAssociation);
 			}
 
 			/*if (isPAND)
 			{
 				// Adding Pseudo and condition in the where part.
 				pseudoAndSQL = createPseudoAndCondition(expression, parentExpression,
 						eavAssociation);
 			}*/
 		}
 
 		buffer.append(processOperands(expression));
 
 		/*
 		 * If the Query has only one Expression, which referes to an entity
 		 * having inheritance strategy as TABLE_PER_HEIRARCHY, then the
 		 * Descriminator column condition needs to be added in the WHERE part of
 		 * SQL as it can not be added in the FROM part of the query. This can be
 		 * identified by following checks 1. parentExpression is null & 2.
 		 * expression have no child expression.
 		 * 
 		 * If this expression is PseudoAnded then the same check should be made
 		 * which will be add descriminatorCondition in the innersql of pseudoAnd
 		 * query.
 		 */
 		if (parentExpression == null /*|| isPAND*/) // This will be true only for
 		// root Expression of the Query.
 		{
 			List<IExpression> childrenList = joinGraph.getChildrenList(expression);
 			if (childrenList == null || childrenList.isEmpty())
 			{
 				/*
 				 * No Child Expressions present for the root node, so this is
 				 * only Expression in the Query. So check for the Inheritance
 				 * strategy. If its derived entity with inheritance strategy as
 				 * TABLE_PER_HEIRARCHY, then append the descriminator condition
 				 * SQL in buffer.
 				 */
 				if (entity.getParentEntity() != null
 						&& InheritanceStrategy.TABLE_PER_HEIRARCHY.equals(entity
 								.getInheritanceStrategy()))
 				{
 					String descriminatorCondition = getDescriminatorCondition(entity, getAliasFor(
 							expression, entity));
 					buffer.insert(0, Constants.QUERY_OPENING_PARENTHESIS);
 					buffer.append(Constants.QUERY_CLOSING_PARENTHESIS);
 					buffer.append(descriminatorCondition);
 				}
 			}
 		}
 
 		/*if (isPAND) // Append Pseudo can sql if the expression is psuedo anded.
 		{
 			buffer.insert(0, pseudoAndSQL);
 		} */
 
 		return buffer.toString();
 	}
 
 	/**
 	 * To process all child operands of the expression.
 	 * 
 	 * @param expression the reference to Expression.
 	 * @return the query for the child operands.
 	 * @throws SqlException When there is error in the passed IQuery object.
 	 */
 	protected String processOperands(IExpression expression) throws SqlException, RuntimeException
 	{
 		StringBuffer buffer = new StringBuffer();
 		int currentNestingCounter = 0;// holds current nesting number count
 		// i.e. no of opening Braces that needs
 		// to be closed.
 
 		int noOfRules = expression.numberOfOperands();
 		for (int i = 0; i < noOfRules; i++)
 		{
 			IExpressionOperand operand = expression.getOperand(i);
 			String operandquery = "";
 			boolean isEmptyExppression = false;
 			if (operand instanceof IRule)
 			{
 				if(((IRule) operand).size() > 0)
 				{
 					operandquery = getQuery((IRule) operand); // Processing Rule.
 				}
 				else
 				{
 					continue;
 				}
 			}
 			else if (operand instanceof IExpression)
 			// Processing sub Expression.
 			{
 
 				isEmptyExppression = emptyExpressions.contains(operand);
 				if (!isEmptyExppression)
 				{
 					operandquery = buildWherePart((IExpression) operand, expression);
 				}
 				else
 				{
 					continue;
 				}
 			}
 			else
 			{
 				operandquery = getCustomFormulaString((ICustomFormula) operand);
 				operandquery = getTemporalCondition(operandquery);
 			}
 
 			if (!operandquery.equals("") && noOfRules != 1)
 			{
 				operandquery = edu.wustl.query.util.global.Constants.QUERY_OPENING_PARENTHESIS
 						+ operandquery
 						+ edu.wustl.query.util.global.Constants.QUERY_CLOSING_PARENTHESIS;
 				// putting the Rule's query in
 				// Braces so that it
 				// will not get mixed
 				// with other Rules.
 			}
 
 			if (i != noOfRules - 1)
 			{
 				Connector connector = (Connector) expression.getConnector(i, i + 1);
 				int nestingNumber = connector.getNestingNumber();
 
 				int nextIndex = i + 1;
 				IExpressionOperand nextOperand = expression.getOperand(nextIndex);
 				if (nextOperand instanceof IExpression && emptyExpressions.contains(nextOperand))
 				{
 					for (; nextIndex < noOfRules; nextIndex++)
 					{
 						nextOperand = expression.getOperand(nextIndex);
 						if (!(nextOperand instanceof IExpression && emptyExpressions
 								.contains(nextOperand)))
 						{
 							break;
 						}
 					}
 					if (nextIndex == noOfRules)// Expression over add closing
 					// parenthesis.
 					{
 						buffer.append(operandquery);
 						buffer.append(getParenthesis(currentNestingCounter,
 								Constants.QUERY_CLOSING_PARENTHESIS));
 						currentNestingCounter = 0;
 					}
 					else
 					{
 						Connector newConnector = (Connector) expression.getConnector(nextIndex - 1,
 								nextIndex);
 						int newNestingNumber = newConnector.getNestingNumber();
 						currentNestingCounter = attachOperandquery(buffer, currentNestingCounter,
 								operandquery, newNestingNumber);
 						buffer.append(Constants.SPACE).append(newConnector.getOperator().toString().toLowerCase());
 					}
 					i = nextIndex - 1;
 				}
 				else
 				{
 					currentNestingCounter = attachOperandquery(buffer, currentNestingCounter,
 							operandquery, nestingNumber);
 					buffer.append(Constants.SPACE).append(connector.getOperator().toString().toLowerCase());
 				}
 			}
 			else
 			{
 				buffer.append(operandquery);
 				buffer.append(' ');
 
 				buffer = new StringBuffer(Utility.removeLastAnd(buffer.toString()));
 				buffer.append(getParenthesis(currentNestingCounter,
 						Constants.QUERY_CLOSING_PARENTHESIS));
 				// Finishing
 				// query
 				// by
 				// adding
 				// closing
 				// parenthesis
 				// if
 				// any.
 				currentNestingCounter = 0;
 			}
 		}
 		return buffer.toString();
 	}
 	
 	/**
 	 * 
 	 * @param operandquery
 	 * @return Get the modified Temporal Condition according to SQL and XQuery Implementation
 	 */
 	protected abstract String getTemporalCondition(String operandquery);
 	
 	
 
 	protected abstract String getDescriminatorCondition(EntityInterface entity, String aliasFor);
 
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
 	protected boolean checkForEmptyExpression(int expressionId)
 	{
 		Expression expression = (Expression) constraints.getExpression(expressionId);
 		List<IExpression> operandList = joinGraph.getChildrenList(expression);
 
 		boolean isEmpty = true;
 		if (!operandList.isEmpty()) // Check whether any of its children
 		// contains rule.
 		{
 			for (IExpression subExpression : operandList)
 			{
 				if (!checkForEmptyExpression(subExpression.getExpressionId()))
 				{
 					isEmpty = false;
 				}
 			}
 		}
 
 		isEmpty = isEmpty && !containsCondition(expression) ;// check if there are
 		// rule present as
 		// subexpression.
 		// SRINATH
 		isEmpty = isEmpty && !expression.containsCustomFormula();
 		if (isEmpty)
 		{
 			emptyExpressions.add(expression); // Expression is empty.
 		}
 
 		return isEmpty;
 	}
 
 	private boolean containsCondition(Expression expression)
 	{
 		boolean result;
 		if(expression.containsRule())
 		{
 			IRule rule = (IRule)expression.getOperand(0);
 			if(rule.size()==0)
 			{
 				result = false;
 			}
 			else
 			{
 				result = true;
 			}
 		}
 		else
 		{
 			result = false;
 		}
 		
 		return result;
 	}
 
 	/**
 	 * To get the query for the Rule
 	 * 
 	 * @param rule The reference to Rule.
 	 * @return The query for the Rule.
 	 * @throws SqlException When there is error in the passed IQuery object.
 	 */
 	private String getQuery(IRule rule) throws SqlException
 	{
 		StringBuffer buffer = new StringBuffer();
 
 		//IExpression expression = rule.getContainingExpression();
 		addActivityStausCondition(rule);
 
 		int noOfConditions = rule.size();
 		if (noOfConditions == 0)
 		{
 			throw new SqlException("No conditions defined in the Rule!!!");
 		}
 		for (int i = 0; i < noOfConditions; i++) // Processing all conditions
 		// in Rule combining them
 		// with AND operator.
 		{
 			String condition = processOperator(rule.getCondition(i), rule.getContainingExpression());
 
 			if (i != noOfConditions - 1) // Intermediate Condition.
 			{
 				if (!condition.equals(""))
 				{
 					buffer.append(condition + " " + LogicalOperator.And.toString().toLowerCase()
 							+ " ");
 				}
 
 			}
 			else
 			{
 				// Last Condition, this will not followed by And logical
 				// operator.
 				buffer.append(condition);
 			}
 		}
 		return Utility.removeLastAnd(buffer.toString());
 	}
 
 	private void addActivityStausCondition(IRule rule)
 	{
 
 	}
 
 	protected String getCustomFormulaString(ICustomFormula formula)
 	{
 		return getCustomFormulaProcessor().asString(formula);
 	}
 
 	private CustomFormulaProcessor getCustomFormulaProcessor()
 	{
 		return new CustomFormulaProcessor(getAliasProvider(), getDatabaseSQLSettings(),Variables.properties.getProperty("queryType"));
 	}
 
 	protected DatabaseSQLSettings getDatabaseSQLSettings()
 	{
 		DatabaseType databaseType;
 		if (Variables.databaseName.equals(Constants.MYSQL_DATABASE))
 		{
 			databaseType = DatabaseType.MySQL;
 		}
 		else if (Variables.databaseName.equals(Constants.ORACLE_DATABASE))
 		{
 			databaseType = DatabaseType.Oracle;
 		}
 		else if (Variables.databaseName.equals(Constants.DB2_DATABASE))
 		{
 			databaseType = DatabaseType.DB2;
 		}
 		else
 		{
 			throw new UnsupportedOperationException("Custom formulas on " + Variables.databaseName
 					+ " are not supported.");
 		}
 		return new DatabaseSQLSettings(databaseType);
 	}
 
 	private IAttributeAliasProvider getAliasProvider()
 	{
 		return new IAttributeAliasProvider()
 		{
 
 			public String getAliasFor(IExpressionAttribute exprAttr)
 			{
 				return getConditionAttributeName(exprAttr.getAttribute(), exprAttr.getExpression());
 			}
 
 		};
 	}
 
 	// output terms
 
 	private TermProcessor getTermProcessor()
 	{
 		return new TermProcessor(getAliasProvider(), getDatabaseSQLSettings(),Variables.properties.getProperty("queryType"));
 	}
 
 	protected String getTermString(ITerm term)
 	{
 		// TODO this is a tad ugly now; if/when sqlGen moves to query project, it won't be.
 		TermString termString = getTermProcessor().convertTerm(term);
 		String s = termString.getString();
 		if (termString.getTermType() != TermType.DSInterval)
 		{
 			return s;
 		}
 
 		s = Constants.QUERY_OPENING_PARENTHESIS + s + Constants.QUERY_CLOSING_PARENTHESIS;
 		switch (getDatabaseSQLSettings().getDatabaseType())
 		{
 			case MySQL :
 				return s;
 			case Oracle :
 				Constants.getOracleTermString(s);
 			case DB2 :
 				Constants.getDB2TermString(s);	
 			default :
 				throw new RuntimeException("won't occur.");
 		}
 	}
 
 	/**
 	 * To append the operand query to the query buffer, with required number of
 	 * parenthesis.
 	 * 
 	 * @param buffer The reference to the String buffer containing query for query
 	 *            of operands of an expression.
 	 * @param currentNestingCounter The current nesting count.
 	 * @param operandquery The query of the operand to be appended to buffer
 	 * @param nestingNumber The nesting number for the current operand's
 	 *            operator.
 	 * @return The updated current nesting count.
 	 */
 	private int attachOperandquery(StringBuffer buffer, int currentNestingCounter,
 			String operandquery, int nestingNumber)
 	{
 		if (currentNestingCounter < nestingNumber)
 		{
 			buffer.append(getParenthesis(nestingNumber - currentNestingCounter,
 					Constants.QUERY_CLOSING_PARENTHESIS));
 			currentNestingCounter = nestingNumber;
 			buffer.append(operandquery);
 		}
 		else if (currentNestingCounter > nestingNumber)
 		{
 			buffer.append(operandquery);
 			buffer.append(getParenthesis(currentNestingCounter - nestingNumber,
 					Constants.QUERY_CLOSING_PARENTHESIS));
 			currentNestingCounter = nestingNumber;
 		}
 		else
 		{
 			buffer.append(operandquery);
 		}
 		return currentNestingCounter;
 	}
 
 	/**
 	 * To get n number of parenthesis.
 	 * 
 	 * @param n The positive integer value
 	 * @param parenthesis either Opening parenthesis or closing parenthesis.
 	 * @return The n number of parenthesis.
 	 */
 	public String getParenthesis(int n, String parenthesis)
 	{
 		StringBuilder string = new StringBuilder();
 		for (int i = 0; i < n; i++)
 		{
 			string.append(parenthesis);
 		}
 
 		return string.toString();
 	}
 
 	/**
 	 * To get the Output Entity for the given Expression.
 	 * 
 	 * @param expression The reference to the Expression.
 	 * @return The output entity for the Expression.
 	 */
 	private IOutputEntity getOutputEntity(IExpression expression)
 	{
 		EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 		IOutputEntity outputEntity = QueryObjectFactory.createOutputEntity(entity);
 		outputEntity.getSelectedAttributes().addAll(entity.getEntityAttributesForQuery());
 		return outputEntity;
 	}
 
 	/**
 	 * Get the query specific representation for Attribute ie LHS of a condition.
 	 * 
 	 * @param attribute The reference to AttributeInterface
 	 * @param expression The reference to Expression to which this attribute
 	 *            belongs.
 	 * @return The query specific representation for Attribute.
 	 */
 	protected abstract String getConditionAttributeName(AttributeInterface attribute,
 			IExpression expression);
 
 	/**
 	 * It will return the select part attributes for this node along with its
 	 * child nodes.
 	 * 
 	 * @param treeNode the output tree node.
 	 * @return The select part attributes for this node along with its child
 	 *         nodes.
 	 */
 	protected String getSelectAttributes(OutputTreeDataNode treeNode)
 	{
 		StringBuffer selectPart = new StringBuffer();
 		IExpression expression = constraints.getExpression(treeNode.getExpressionId());
 
 		IOutputEntity outputEntity = treeNode.getOutputEntity();
 		List<AttributeInterface> attributes = outputEntity.getSelectedAttributes();
 
 		for (AttributeInterface attribute : attributes)
 		{
 			selectPart.append(getConditionAttributeName(attribute, expression));
 			String columnAliasName = Constants.QUERY_COLUMN_NAME + selectIndex;
 			selectPart.append(Constants.SPACE).append(columnAliasName).append(Constants.QUERY_COMMA);
 			// code to get display name. & pass it to the Constructor along with
 			// treeNode.
 			String displayNameForColumn = Utility.getDisplayNameForColumn(attribute);
 			treeNode.addAttribute(new QueryOutputTreeAttributeMetadata(attribute, columnAliasName,
 					treeNode, displayNameForColumn));
 			attributeColumnNameMap.put(attribute, columnAliasName);
 			selectIndex++;
 			if (Constants.QUERY_FILE.equalsIgnoreCase(attribute.getDataType()))
 			{
 				containsCLOBTypeColumn = true;
 			}
 		}
 		List<OutputTreeDataNode> children = treeNode.getChildren();
 		for (OutputTreeDataNode childTreeNode : children)
 		{
 			selectPart.append(getSelectAttributes(childTreeNode));
 		}
 		return selectPart.toString();
 	}
 
 	/**
 	 * Adds an pseudo anded expression & all its child expressions to
 	 * pAndExpressions set.
 	 * 
 	 * @param expression pAnd expression
 	 */
 	/*protected void addpAndExpression(IExpression expression)
 	{
 		List<IExpression> childList = joinGraph.getChildrenList(expression);
 		pAndExpressions.add(expression);
 
 		for (IExpression newExp : childList)
 		{
 			addpAndExpression(newExp);
 		}
 
 	}*/
 
 	/**
 	 * To assign alias to each tablename in the Expression. It will generate
 	 * alias that will be assigned to each entity in Expression.
 	 * 
 	 * @param expression the Root Expression of the Query.
 	 * @param currentAliasCount The count from which it will start to assign
 	 *            alias appender.
 	 * @param aliasToSet The alias to set for the current expression.
 	 * @param pathMap The map of path verses the ExpressionId. entry in this map
 	 *            means, for such path, there is already alias assigned to some
 	 *            Expression.
 	 * @return The int representing the modified alias appender count that will
 	 *         be used for further processing.
 	 * @throws MultipleRootsException if there are multpile roots present in
 	 *             join graph.
 	 */
 	protected void createAliasAppenderMap() throws MultipleRootsException
 	{
 		for (IExpression expr : constraints)
 		{
 			aliasAppenderMap.put(expr, expr.getExpressionId());
 		}
 	}
 
 	protected boolean isContainedExpresion(int expressionId)
 	{
 		return false;
 	}
 
 	/**
 	 * TO create the output tree from the constraints.
 	 * 
 	 * @param expression The reference to Expression
 	 * @param parentOutputTreeNode The reference to parent output tree node.
 	 *            null if there is no parent.
 	 */
 	protected void completeTree(IExpression expression, OutputTreeDataNode parentOutputTreeNode)
 	{
 		List<IExpression> children = joinGraph.getChildrenList(expression);
 		boolean isContained = false;
 		for (IExpression childExp : children)
 		{
 			OutputTreeDataNode childNode = parentOutputTreeNode;
 
 			if (shouldAddNodeFor(childExp))
 			{
 				IOutputEntity childOutputEntity = getOutputEntity(childExp);
 				Integer childAliasAppender = aliasAppenderMap.get(childExp);
 
 				//Set containment object to true if expression is contained.
 				isContained = isContainedExpresion(childExp.getExpressionId());
 
 				/**
 				 * Check whether output tree node for expression with the same
 				 * alias already added or not. if its not added then need to add
 				 * it alias in the outputTreeNodeMap
 				 */
 				childNode = outputTreeNodeMap.get(childAliasAppender);
 				if (childNode == null)
 				{
 					if (parentOutputTreeNode == null)
 					{
 						//						 New root node for output tree found, so create root
 						//						 node & add it in the rootOutputTreeNodeList.
 
 						childNode = new OutputTreeDataNode(childOutputEntity, childExp
 								.getExpressionId(), treeNo++, isContained);
 						rootOutputTreeNodeList.add(childNode);
 					}
 					else
 					{
 						childNode = parentOutputTreeNode.addChild(childOutputEntity, childExp
 								.getExpressionId(), isContained);
 					}
 					outputTreeNodeMap.put(childAliasAppender, childNode);
 					attributeOutputTreeNodeList.add(childNode);
 
 				}
 			}
 			else
 			{
 				IOutputEntity childOutputEntity = getOutputEntity(childExp);
 				childNode = new OutputTreeDataNode(childOutputEntity, childExp.getExpressionId(),
 						allExpressionTreeNo++, isContained);
 				attributeOutputTreeNodeList.add(childNode);
 			}
 			completeTree(childExp, childNode);
 		}
 	}
 
 	/**
 	 * 
 	 * @param expression the expression 
 	 * @return whether a node corresponding to the given expression should be added to the node list 
 	 */
 	protected boolean shouldAddNodeFor(IExpression expression)
 	{
		return true;
 	}
 
 	/**
 	 * To get the primary key attribute of the given entity.
 	 * 
 	 * @param entity the DE entity.
 	 * @return The Primary key attribute of the given entity.
 	 * @throws SqlException If there is no such attribute present in the
 	 *             attribute list of the entity.
 	 */
 	protected AttributeInterface getPrimaryKey(EntityInterface entity) throws SqlException
 	{
 		Collection<AttributeInterface> attributes = entity.getEntityAttributesForQuery();
 		for (AttributeInterface attribute : attributes)
 		{
 			if (attribute.getIsPrimaryKey()
 					|| attribute.getName().equals(Constants.SYSTEM_IDENTIFIER))
 			{
 				return attribute;
 			}
 		}
 
 		EntityInterface parentEntity = entity.getParentEntity();
 
 		if (parentEntity != null)// &&
 		// entity.getInheritanceStrategy().equals(InheritanceStrategy.TABLE_PER_SUB_CLASS))
 		{
 			return getPrimaryKey(parentEntity);
 		}
 
 		throw new SqlException("No Primary key attribute found for Entity:" + entity.getName());
 	}
 
 	/**
 	 * To create output tree for the given expression graph.
 	 * 
 	 * @throws MultipleRootsException When there exists multiple roots in
 	 *             joingraph.
 	 */
 	protected void createTree() throws MultipleRootsException
 	{
 		IExpression rootExpression = joinGraph.getRoot();
 		rootOutputTreeNodeList = new ArrayList<OutputTreeDataNode>();
 		attributeOutputTreeNodeList = new ArrayList<OutputTreeDataNode>();
 		outputTreeNodeMap = new HashMap<Integer, OutputTreeDataNode>();
 		OutputTreeDataNode rootOutputTreeNode = null;
 		boolean isContained = false;
 		treeNo = 0;
 		allExpressionTreeNo = 0;
 		if (isContainedExpresion(rootExpression.getExpressionId()))
 		{
 			isContained = true;
 		}
 		if (rootExpression.isInView())
 		{
 			IOutputEntity rootOutputEntity = getOutputEntity(rootExpression);
 			rootOutputTreeNode = new OutputTreeDataNode(rootOutputEntity, rootExpression
 					.getExpressionId(), treeNo++, isContained);
 
 			rootOutputTreeNodeList.add(rootOutputTreeNode);
 			attributeOutputTreeNodeList.add(rootOutputTreeNode);
 			outputTreeNodeMap.put(aliasAppenderMap.get(rootExpression), rootOutputTreeNode);
 		}
 		completeTree(rootExpression, rootOutputTreeNode);
 	}
 
 	/**
 	 * To get the Alias Name for the given IExpression. It will return alias
 	 * name for the DE entity associated with constraint entity.
 	 * 
 	 * @param expression The reference to IExpression.
 	 * @return The Alias Name for the given Entity.
 	 */
 	protected String getAliasName(IExpression expression)
 	{
 		EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 		return getAliasFor(expression, entity);
 	}
 
 	/**
 	 * To get the aliasName for the given entity present which is associated
 	 * with Expression.
 	 * 
 	 * @param expression The reference to IExpression.
 	 * @param attributeEntity The reference to the Entity for which the alias to
 	 *            be searched.
 	 * @return The Alias Name for the given Entity.
 	 */
 	protected String getAliasFor(IExpression expression, EntityInterface attributeEntity)
 	{
 		EntityInterface entity = expression.getQueryEntity().getDynamicExtensionsEntity();
 		EntityInterface aliasEntity = entity;
 
 		EntityInterface parentEntity = entity.getParentEntity();
 
 		while (parentEntity != null && !attributeEntity.equals(entity))
 		{
 			InheritanceStrategy type = entity.getInheritanceStrategy();
 
 			if (type.equals(InheritanceStrategy.TABLE_PER_CONCRETE_CLASS))
 			{
 				aliasEntity = entity;
 				break;
 			}
 			else if (type.equals(InheritanceStrategy.TABLE_PER_SUB_CLASS))
 			{
 				entity = parentEntity;
 				aliasEntity = parentEntity;
 				parentEntity = parentEntity.getParentEntity();
 			}
 			else if (type.equals(InheritanceStrategy.TABLE_PER_HEIRARCHY))
 			{
 				while (parentEntity != null && type.equals(InheritanceStrategy.TABLE_PER_HEIRARCHY))
 				{
 					entity = parentEntity;
 					if (attributeEntity.equals(entity))
 					{
 						break;
 					}
 					type = entity.getInheritanceStrategy();
 					parentEntity = parentEntity.getParentEntity();
 				}
 				aliasEntity = entity;
 			}
 		}
 
 		// Need an extra check for the TABLE_PER_HEIRARCHY case.
 		// Because even if attribute belongs to this aliasEntity, but if its
 		// association with parent is of TABLE_PER_HEIRARCHY type, its alias
 		// will be one of its parent heirarchy.
 		parentEntity = aliasEntity.getParentEntity();
 		InheritanceStrategy type = aliasEntity.getInheritanceStrategy();
 		while (parentEntity != null && type.equals(InheritanceStrategy.TABLE_PER_HEIRARCHY))
 		{
 			aliasEntity = parentEntity;
 			type = aliasEntity.getInheritanceStrategy();
 			parentEntity = parentEntity.getParentEntity();
 		}
 
 		String aliasName = getAliasForClassName(aliasEntity.getName());
 		Integer aliasAppender = aliasAppenderMap.get(expression);
 		if (aliasAppender == null)// for Junits
 		{
 			aliasAppender = 0;
 		}
 		aliasName = aliasName + Constants.QUERY_UNDERSCORE + aliasAppender;
 		return aliasName;
 	}
 
 	/**
 	 * To get the alias for the given Class Name.
 	 * 
 	 * @param className The follyQualified class Name.
 	 * @return The alias name for the given class Name.
 	 */
 	protected String getAliasForClassName(String className)
 	{
 		String aliasName = aliasNameMap.get(className);
 
 		if (aliasName == null)
 		{
 			aliasName = className.substring(className.lastIndexOf('.') + 1, className.length());
 			if (aliasName.length() > ALIAS_NAME_LENGTH)
 			{
 				aliasName = aliasName.substring(0, ALIAS_NAME_LENGTH);
 			}
 
 			// aliasName = aliasName.replaceAll(Constants.REGEX_EXPRESION,
 			// Constants.REPLACEMENT);
 			aliasName = Utility.removeSpecialCharactersFromString(aliasName);
 			// get unique aliasName for the given class.
 			int count = 1;
 			String theAliasName = aliasName;
 			Collection<String> allAssignedAliases = aliasNameMap.values();
 			while (allAssignedAliases.contains(theAliasName))
 			{
 				theAliasName = aliasName + count++;
 			}
 			aliasName = theAliasName;
 			aliasNameMap.put(className, aliasName);
 		}
 		return aliasName;
 	}
 
 	private String processOperator(ICondition condition, IExpression expression)
 			throws SqlException
 	{
 		AttributeInterface attribute = condition.getAttribute();
 		Collection<TaggedValueInterface> taggedValues = attribute.getTaggedValueCollection();
 		for (TaggedValueInterface tagValue : taggedValues)
 		{
 			if (tagValue.getKey().equalsIgnoreCase(Constants.VI_IGNORE_PREDICATE))
 			{
 				return "";
 			}
 		}
 
 		String attributeName = getConditionAttributeName(attribute, expression);
 
 		RelationalOperator operator = condition.getRelationalOperator();
 		String sql = null;
 
 		if (operator.equals(RelationalOperator.Between))// Processing Between
 		// Operator, it will be
 		// treated as (op>=val1
 		// and op<=val2)
 		{
 			sql = processBetweenOperator(condition, attributeName);
 		}
 		else if (operator.equals(RelationalOperator.In)
 				|| operator.equals(RelationalOperator.NotIn)) // Processing
 		// In
 		// Operator
 		{
 
 			sql = processInOperator(condition, attributeName);
 		}
 		else if (operator.equals(RelationalOperator.IsNotNull)
 				|| operator.equals(RelationalOperator.IsNull)) // Processing
 		// isNull
 		// &
 		// isNotNull
 		// operator.
 		{
 
 			sql = processNullCheckOperators(condition, attributeName);
 		}
 		else if (operator.equals(RelationalOperator.Contains)
 				|| operator.equals(RelationalOperator.StartsWith)
 				|| operator.equals(RelationalOperator.EndsWith)) // Processing
 		// String
 		// related
 		// Operators.
 		{
 			sql = processLikeOperators(condition, attributeName);
 		}
 		else
 		// Processing rest operators like =, !=, <, > , <=, >= etc.
 		{
 			sql = processComparisionOperator(condition, attributeName);
 		}
 
 		return sql;
 	}
 
 	/**
 	* Processing operators like =, !=, <, > , <=, >= etc.
 	* 
 	* @param condition the condition.
 	* @param attributeName the Name of the attribute to returned in SQL.
 	* @return SQL representation for given condition.
 	* @throws SqlException when: 1. value list contains more/less than 1 value.
 	*             2. other than = ,!= operator present for String data type.
 	*/
 	protected String processComparisionOperator(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		AttributeTypeInformationInterface dataType = condition.getAttribute()
 				.getAttributeTypeInformation();
 		RelationalOperator operator = condition.getRelationalOperator();
 		List<String> values = condition.getValues();
 		if (values.size() != 1)
 		{
 			throw new SqlException("Incorrect number of values found for Operator '" + operator
 					+ "' for condition:" + condition);
 		}
 		String value = values.get(0);
 		if (dataType instanceof StringTypeInformationInterface)
 		{
 			if (!(operator.equals(RelationalOperator.Equals) || operator
 					.equals(RelationalOperator.NotEquals)))
 			{
 				throw new SqlException(
 						"Incorrect operator found for String datatype for condition:" + condition);
 			}
 		}
 
 		if (dataType instanceof BooleanAttributeTypeInformation)
 		{
 			if (!(operator.equals(RelationalOperator.Equals) || operator
 					.equals(RelationalOperator.NotEquals)))
 			{
 				throw new SqlException(
 						"Incorrect operator found for Boolean datatype for condition:" + condition);
 			}
 		}
 
 		value = modifyValueForDataType(value, dataType);
 		String sql = attributeName + RelationalOperator.getSQL(operator) + value;
 		return sql;
 	}
 
 	/**
 	* To process String operators. for Ex. starts with, contains etc.
 	* 
 	* @param condition the condition.
 	* @param attributeName the Name of the attribute to returned in SQL.
 	* @return SQL representation for given condition.
 	* @throws SqlException when 1. The datatype of attribute is not String. 2.
 	*             The value list empty or more than 1 value.
 	*/
 	protected String processLikeOperators(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		RelationalOperator operator = condition.getRelationalOperator();
 
 		if (!(condition.getAttribute().getAttributeTypeInformation() instanceof StringTypeInformationInterface || condition
 				.getAttribute().getAttributeTypeInformation() instanceof FileTypeInformationInterface))
 		{
 			throw new SqlException("Incorrect data type found for Operator '" + operator
 					+ "' for condition:" + condition);
 		}
 
 		List<String> values = condition.getValues();
 		if (values.size() != 1)
 		{
 			throw new SqlException("Incorrect number of values found for Operator '" + operator
 					+ "' for condition:" + condition);
 		}
 		String value = values.get(0);
 		if (operator.equals(RelationalOperator.Contains))
 		{
 			value = "'%" + value + "%'";
 		}
 		else if (operator.equals(RelationalOperator.StartsWith))
 		{
 			value = "'" + value + "%'";
 		}
 		else if (operator.equals(RelationalOperator.EndsWith))
 		{
 			value = "'%" + value + "'";
 		}
 		String str ;
 		switch (getDatabaseSQLSettings().getDatabaseType())
 		{
 			case MySQL :
 				str = attributeName + Constants.LIKE + value;
 				break;
 			case Oracle :
 				str = "lower(" + attributeName + ") like lower(" + value
 						+ Constants.QUERY_CLOSING_PARENTHESIS;
 				break;
 			default:
 				str="";
 				break;
 		}
 		return str;
 	}
 
 	/**
 	* To process 'Is Null' & 'Is Not Null' operator.
 	* 
 	* @param condition the condition.
 	* @param attributeName the Name of the attribute to returned in SQL.
 	* @return SQL representation for given condition.
 	* @throws SqlException when the value list is not empty.
 	*/
 	protected String processNullCheckOperators(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		String operatorStr = RelationalOperator.getSQL(condition.getRelationalOperator());
 		if (condition.getValues().size() > 0)
 		{
 			throw new SqlException("No value expected in value part for '" + operatorStr
 					+ "' operator !!!");
 		}
 
 		return attributeName + " " + operatorStr;
 
 	}
 
 	/**
 	* To process 'In' & 'Not In' operator.
 	* 
 	* @param condition the condition.
 	* @param attributeName the Name of the attribute to returned in SQL.
 	* @return SQL representation for given condition.
 	* @throws SqlException when the value list is empty or problem in parsing
 	*             any of the value.
 	*/
 	protected String processInOperator(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(attributeName + " "
 				+ RelationalOperator.getSQL(condition.getRelationalOperator()) + " "
 				+ Constants.QUERY_OPENING_PARENTHESIS);
 		List<String> valueList = condition.getValues();
 		AttributeTypeInformationInterface dataType = condition.getAttribute()
 				.getAttributeTypeInformation();
 
 		if (valueList.size() == 0)
 		{
 			throw new SqlException(
 					"atleast one value required for 'In' operand list for condition:" + condition);
 		}
 
 		if (dataType instanceof BooleanAttributeTypeInformation)
 		{
 			throw new SqlException("Incorrect operator found for Boolean datatype for condition:"
 					+ condition);
 		}
 		for (int i = 0; i < valueList.size(); i++)
 		{
 
 			String value = modifyValueForDataType(valueList.get(i), dataType);
 
 			if (i == valueList.size() - 1)
 			{
 				buffer.append(value).append(Constants.QUERY_CLOSING_PARENTHESIS);
 			}
 			else
 			{
 				buffer.append(value).append(Constants.QUERY_COMMA);
 			}
 		}
 		return buffer.toString();
 	}
 
 	/**
 	* To get the SQL for the given condition with Between operator. It will be
 	* treated as (op>=val1 and op<=val2)
 	* 
 	* @param condition The condition.
 	* @param attributeName the Name of the attribute to returned in SQL.
 	* @return SQL representation for given condition.
 	* @throws SqlException when: 1. value list does not have 2 values 2.
 	*             Datatype is not date 3. problem in parsing date.
 	*/
 	protected String processBetweenOperator(ICondition condition, String attributeName)
 			throws SqlException
 	{
 		StringBuffer buffer = new StringBuffer();
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
 
 		buffer.append(Constants.QUERY_OPENING_PARENTHESIS).append(attributeName);
 		buffer.append(RelationalOperator.getSQL(RelationalOperator.GreaterThanOrEquals))
 			.append(firstValue);
 		buffer.append(Constants.SPACE).append(LogicalOperator.And).append(Constants.SPACE)
 			  .append(attributeName).append(RelationalOperator.getSQL(RelationalOperator.LessThanOrEquals)).append(secondValue)
 			  .append(Constants.QUERY_CLOSING_PARENTHESIS);
 
 		return buffer.toString();
 	}
 
 	/**
 	 * This method will be used by Query Mock to set the join Graph externally.
 	 * 
 	 * @param joinGraph the reference to joinGraph.
 	 */
 	protected void setJoinGraph(JoinGraph joinGraph)
 	{
 		this.joinGraph = joinGraph;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.query.queryengine.impl.IQueryGenerator#generateQuery(edu.wustl.common.querysuite.queryobject.IQuery)
 	 */
 	public abstract String generateQuery(IQuery query) throws MultipleRootsException, SqlException;
 
 	/* (non-Javadoc)
 	 * @see edu.wustl.query.queryengine.impl.IQueryGenerator#getAttributeColumnNameMap()
 	 */
 
 	/**
 	 * modify the values of the data types to suit the database envioronment
 	 */
 	protected abstract String modifyValueForDataType(String value,
 			AttributeTypeInformationInterface dataType) throws SqlException;
 
 }
