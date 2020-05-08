 /**
  * 
  */
 
 package edu.wustl.common.query.impl;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintKeyPropertiesInterface;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.wustl.common.query.impl.predicate.AbstractPredicate;
 import edu.wustl.common.query.impl.predicate.PredicateGenerator;
 import edu.wustl.common.query.impl.predicate.Predicates;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IOutputAttribute;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.Utility;
 
 /**
  * @author juberahamad_patel
  *
  */
 public class PassOneXQueryGenerator extends AbstractXQueryGenerator
 {
 
 	/**
 	 * map of for variables and corresponding expressions created in pass one xquery
 	 */
 	private Map<IExpression, String> passOneForVariables;
 
 	public PassOneXQueryGenerator()
 	{
 		passOneForVariables = new LinkedHashMap<IExpression, String>();
 	}
 
 	/**
 	 * build the for clause of xquery
 	 * @param predicateGenerator  
 	 * @return the For Clause of XQuery
 	 * @throws MultipleRootsException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	@Override
 	protected String buildXQueryForClause(PredicateGenerator predicateGenerator)
 			throws MultipleRootsException, DynamicExtensionsSystemException
 	{
 
 		StringBuilder xqueryForClause = new StringBuilder(1024);
 
 		setPassOneForVariables();
 
 		for (IExpression expression : getMainExpressions())
 		{
 			StringBuilder laterPart = new StringBuilder(1024);
 			String tableName = expression.getQueryEntity().getDynamicExtensionsEntity()
 					.getTableProperties().getName();
 
 			StringBuilder rootPath = new StringBuilder().append(Constants.QUERY_XMLCOLUMN).append(
 					Constants.QUERY_OPENING_PARENTHESIS).append("\"").append(tableName).append(
 					Constants.QUERY_DOT).append(Constants.QUERY_XMLDATA).append("\"").append(
 					Constants.QUERY_CLOSING_PARENTHESIS);
 
 			String variable = appendChildren(expression, predicateGenerator, laterPart);
 
 			xqueryForClause.append(Constants.QUERY_FOR).append(variable).append(' ').append(
 					Constants.IN).append(' ').append(rootPath.toString()).append(
 					laterPart.toString());
 
 		}
 
 		return xqueryForClause.toString();
 	}
 
 	/**
 	 * set the for variables used in pass one xquery
 	 * the assumption is that pass one for variables are present among general for variables
 	 */
 	private void setPassOneForVariables()
 	{
 		for (IExpression expression : getForVariables().keySet())
 		{
 			if (hasVersion(expression))
 			{
 				String variable = getForVariables().get(expression);
 				passOneForVariables.put(expression, variable);
 			}
 		}
 
 	}
 
 	/**
 	 * append xquery fragments in the for clause for given expression
 	 * and its children recursively 
 	 * @param expression
 	 * @param predicateGenerator
 	 * @param laterPart
 	 * @return
 	 */
 	private String appendChildren(IExpression expression, PredicateGenerator predicateGenerator,
 			StringBuilder laterPart)
 	{
 		String variable = "";
 		String targetRole = getTargetRoles().get(expression);
 
 		if (targetRole != null)
 		{
 			laterPart.append('/').append(targetRole);
 		}
 
 		String entityName = expression.getQueryEntity().getDynamicExtensionsEntity().getName();
 
 		if (!getMainExpressions().contains(expression))
 		{
 			entityName = deCapitalize(entityName);
 		}
 
 		if (getMainExpressions().contains(expression))
 		{
 			laterPart.append('/').append(entityName);
 		}
 
 		if (hasVersion(expression))
 		{
 			laterPart.append('/').append(entityName);
 			String localPredicates = getAllDownstreamPredicates(predicateGenerator, expression, "");
 			laterPart.append('[').append(localPredicates).append(']');
 			variable = passOneForVariables.get(expression);
 
 		}
 		else
 		{
 			Predicates predicates = predicateGenerator.getPredicates(expression);
 
 			if (predicates != null)
 			{
 				replaceRhsForVariables(predicates);
 				laterPart.append('[').append(predicates.assemble()).append(']');
 			}
 
 			List<IExpression> children = getNonMainNonEmptyChildren(expression);
 			for (IExpression child : children)
 			{
				/**
				 * added to fix bug 11656.
				 * Now we can possibly have branching before we encounter hasVersion becomes true.
				 * To avoid the wrong branch, check if child is childless and does not have version. 
				 */
				if (getNonMainNonEmptyChildren(child).isEmpty() && !hasVersion(child))
				{
					continue;
				}
 
				variable = appendChildren(child, predicateGenerator, laterPart);
 			}
 
 		}
 
 		return variable;
 	}
 
 	/**
 	 * build the right paths for all the predicates of given expression
 	 * and all its children relative to the expression, recursively
 	 * @param predicateGenerator
 	 * @param expression
 	 * @param prefix
 	 * @return
 	 */
 	private String getAllDownstreamPredicates(PredicateGenerator predicateGenerator,
 			IExpression expression, String prefix)
 	{
 		StringBuilder downStreamPredicates = new StringBuilder();
 		Predicates localPredicates = predicateGenerator.getPredicates(expression);
 
 		if (localPredicates == null)
 		{
 			return "";
 		}
 
 		replaceRhsForVariables(localPredicates);
 		downStreamPredicates.append(localPredicates.assemble(prefix)).append(Constants.QUERY_AND);
 
 		List<IExpression> children = getNonMainNonEmptyChildren(expression);
 
 		//end recursion
 		if (children.isEmpty())
 		{
 			return Utility.removeLastAnd(downStreamPredicates.toString());
 		}
 
 		for (IExpression child : children)
 		{
 			//skip children that do not have for variables
 			if (!getForVariables().containsKey(child))
 			{
 				continue;
 			}
 
 			String targetRole = getTargetRoles().get(child);
 			String entityName = deCapitalize(child.getQueryEntity().getDynamicExtensionsEntity()
 					.getName());
 			StringBuilder newPrefix = new StringBuilder(prefix);
 
 			if (targetRole != null)
 			{
 				newPrefix.append(targetRole).append('/');
 			}
 
 			newPrefix.append(entityName).append('/');
 			downStreamPredicates.append(
 					getAllDownstreamPredicates(predicateGenerator, child, newPrefix.toString()))
 					.append(Constants.QUERY_AND);
 		}
 
 		return Utility.removeLastAnd(downStreamPredicates.toString());
 
 	}
 
 	/**
 	 * build the let clause
 	 */
 	@Override
 	protected String buildXQueryLetClause(PredicateGenerator predicateGenerator)
 	{
 		StringBuilder xqueryLetClause = new StringBuilder();
 
 		xqueryLetClause.append(Constants.QUERY_LET);
 
 		for (Entry<IOutputAttribute, String> entry : getAttributeAliases().entrySet())
 		{
 			xqueryLetClause.append(Constants.QUERY_DOLLAR).append(entry.getValue()).append(" := ");
 
 			IExpression expression = entry.getKey().getExpression();
 
 			//traverse down the hierarchy of the expression to find an expresssion which is in passOneForVariables
 			//this could also be theoratically up the hierarchy but not in our xmls
 
 			StringBuilder relativePath = new StringBuilder();
 			while (true)
 			{
 				if (passOneForVariables.containsKey(expression))
 				{
 					xqueryLetClause.append(passOneForVariables.get(expression)).append('/');
 					xqueryLetClause.append(relativePath.toString()).append(
 							entry.getKey().getAttribute().getName()).append(Constants.QUERY_COMMA);
 					break;
 				}
 
 				expression = getNonMainNonEmptyChildren(expression).get(0);
 
 				//additional "../" for entities having target role eg. demographicsCollection 
 				if (getTargetRoles().containsKey(expression))
 				{
 					relativePath.append("../");
 				}
 
 				relativePath.append("../");
 
 			}
 		}
 
 		Utility.removeLastComma(xqueryLetClause);
 		return xqueryLetClause.toString();
 	}
 
 	/**
 	 * 
 	 * @return the Return Clause of SQLXML
 	 */
 	@Override
 	protected String buildXQueryReturnClause()
 	{
 		StringBuilder xqueryReturnClause = new StringBuilder(Constants.QUERY_RETURN);
 		xqueryReturnClause.append("<return>");
 
 		for (String attributeAlias : getAttributeAliases().values())
 		{
 			xqueryReturnClause.append('<').append(attributeAlias).append('>');
 			xqueryReturnClause.append('{').append(Constants.QUERY_DOLLAR).append(attributeAlias)
 					.append('}');
 			xqueryReturnClause.append("</").append(attributeAlias).append('>');
 		}
 
 		xqueryReturnClause.append("</return>");
 		return xqueryReturnClause.toString();
 	}
 
 	/**
 	 * 
 	 * @return Columns part of SQLXML
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@Override
 	protected String buildColumnsPart() throws DataTypeFactoryInitializationException
 	{
 		StringBuilder columnsPart = new StringBuilder(512);
 		columnsPart.append(" columns ");
 
 		for (Entry<IOutputAttribute, String> entry : getAttributeAliases().entrySet())
 		{
 			String attributeAlias = entry.getValue();
 			String dataType = getDataTypeInformation(entry.getKey().getAttribute());
 			columnsPart.append(attributeAlias).append(' ').append(dataType).append(" path '")
 					.append(attributeAlias).append('/').append(
 							entry.getKey().getAttribute().getName()).append("'").append(
 							Constants.QUERY_COMMA);
 		}
 
 		Utility.removeLastComma(columnsPart);
 		return columnsPart.toString();
 	}
 
 	/**
 	 * build the 'passing' clause
 	 */
 	@Override
 	protected String buildPassingPart()
 	{
 		return "";
 	}
 
 	/**
 	 *  replace the RHS for variables in (eg. in joining conditions) in each predicate with 
 	 *  a valid pass one for variable
 	 *    
 	 */
 	private void replaceRhsForVariables(Predicates predicates)
 	{
 
 		List<AbstractPredicate> predicateList = predicates.getPredicates();
 
 		for (AbstractPredicate predicate : predicateList)
 		{
 			String rhs = predicate.getRhs();
 			rhs = replaceRhsForVariable(rhs);
 			predicate.setRhs(rhs);
 
 		}
 	}
 
 	/**
 	 * replace the for variable in the rhs with appropriate pass one for variable.
 	 * the old for variable is replaced with an xpath expression involving an exisiting 
 	 * pass one for variable
 	 * 
 	 * @param rhs
 	 * @return
 	 */
 	private String replaceRhsForVariable(String rhs)
 	{
 		String newRhs = null;
 
 		//return unchanegd 
 		if (rhs == null || !rhs.startsWith(String.valueOf(Constants.QUERY_DOLLAR)))
 		{
 			newRhs = rhs;
 		}
 		else
 		{
 			String forVariable = rhs.substring(0, rhs.indexOf('/'));
 			String attribute = rhs.substring(rhs.indexOf('/') + 1);
 
 			//return unchanged
 			if (passOneForVariables.containsValue(forVariable))
 			{
 				newRhs = rhs;
 			}
 			else
 			{
 
 				//traverse down the hierarchy of the expression to find an expresssion which is in passOneForVariables
 				//this could also be theoratically up the hierarchy but not in our xmls
 				IExpression expression = null;
 				StringBuilder relativePath = new StringBuilder();
 				StringBuilder path = new StringBuilder();
 
 				//find the expression corresponding to the for variable
 				for (Entry<IExpression, String> entry : getForVariables().entrySet())
 				{
 					if (forVariable.equals(entry.getValue()))
 					{
 						expression = entry.getKey();
 						break;
 					}
 				}
 
 				//build the relative path and the complete replacement of for variable
 				while (true)
 				{
 					if (passOneForVariables.containsValue(forVariable))
 					{
 						path.append(forVariable).append('/');
 						path.append(relativePath.toString());
 						path.append(attribute);
 						newRhs = path.toString();
 						break;
 					}
 
 					expression = getNonMainNonEmptyChildren(expression).get(0);
 					forVariable = getForVariables().get(expression);
 
 					//additional "../" for entities having target role eg. demographicsCollection 
 					if (getTargetRoles().containsKey(expression))
 					{
 						relativePath.append("../");
 					}
 
 					relativePath.append("../");
 				}
 			}
 		}
 
 		return newRhs;
 
 	}
 
 	/**
 	 * decide whether the given expression has a version tagged value 
 	 * associated with it 
 	 * @param expression
 	 * @return
 	 */
 	public boolean hasVersion(IExpression expression)
 	{
 
 		Collection<TaggedValueInterface> taggedValues = expression.getQueryEntity()
 				.getDynamicExtensionsEntity().getTaggedValueCollection();
 
 		for (TaggedValueInterface taggedValue : taggedValues)
 		{
 			if (taggedValue.getKey().equalsIgnoreCase(Constants.VERSION))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	protected String buildXQueryWhereClause(PredicateGenerator predicateGenerator)
 	{
 		String xQueryWherePart = predicateGenerator.getXQueryWherePart();
 
 		if (xQueryWherePart == null)
 		{
 			return "";
 		}
 		else
 		{
 			return new StringBuilder(Constants.WHERE).append(xQueryWherePart).toString();
 		}
 	}

 	/**
 	 * to pass the left hand side of joining expression in case of One to Many case
 	 * associated with it 
 	 * @param parentExpression
 	 * entityPath - path of the entity
 	 * primaryKeyName - name of primary key
 	 * @return - left attribute of joining condition
 	 */
	protected String getOneToManyLeft(IExpression parentExpression, String entityPath,
			ConstraintKeyPropertiesInterface cnstrKeyProp)
 	{
 		return "$" + getAliasName(parentExpression) + entityPath + "/"
				+ cnstrKeyProp.getSrcPrimaryKeyAttribute().getName();
 	}
 
 	/**
 	 * to pass the left hand side of joining expression in case of Many to One case
 	 * associated with it 
 	 * @param parentExpression
 	 * entityPath - path of the entity
 	 * primaryKeyName - name of primary key
 	 * @return - left attribute of joining condition
 	 */
 	protected String getManyToOneLeft(IExpression parentExpression,
 			ConstraintKeyPropertiesInterface cnstrKeyProp)
 	{
 		return "$" + getAliasName(parentExpression) + "/"
				+ cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName();
 	}

 }
