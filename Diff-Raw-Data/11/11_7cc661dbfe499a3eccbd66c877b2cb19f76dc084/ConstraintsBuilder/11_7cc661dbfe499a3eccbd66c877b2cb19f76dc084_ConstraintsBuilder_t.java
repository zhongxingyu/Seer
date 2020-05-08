 package edu.wustl.cab2b.server.queryengine.querybuilders.dcql;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.util.TreeNode;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.queryengine.querybuilders.CategoryPreprocessorResult;
 import edu.wustl.cab2b.server.queryengine.querybuilders.dcql.constraints.AbstractAssociationConstraint;
 import edu.wustl.cab2b.server.queryengine.querybuilders.dcql.constraints.AttributeConstraint;
 import edu.wustl.cab2b.server.queryengine.querybuilders.dcql.constraints.DcqlConstraint;
 import edu.wustl.cab2b.server.queryengine.querybuilders.dcql.constraints.ForeignAssociationConstraint;
 import edu.wustl.cab2b.server.queryengine.querybuilders.dcql.constraints.LocalAssociationConstraint;
 import edu.wustl.cab2b.server.queryengine.utils.TransformerUtil;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.metadata.associations.IInterModelAssociation;
 import edu.wustl.common.querysuite.metadata.associations.IIntraModelAssociation;
 import edu.wustl.common.querysuite.queryobject.DataType;
 import edu.wustl.common.querysuite.queryobject.ICondition;
 import edu.wustl.common.querysuite.queryobject.IConnector;
 import edu.wustl.common.querysuite.queryobject.IConstraints;
 import edu.wustl.common.querysuite.queryobject.IExpression;
 import edu.wustl.common.querysuite.queryobject.IExpressionOperand;
 import edu.wustl.common.querysuite.queryobject.IJoinGraph;
 import edu.wustl.common.querysuite.queryobject.IRule;
 import edu.wustl.common.querysuite.queryobject.LogicalOperator;
 import edu.wustl.common.querysuite.queryobject.RelationalOperator;
 import gov.nih.nci.cagrid.cqlquery.Attribute;
 import gov.nih.nci.cagrid.dcql.Association;
 import gov.nih.nci.cagrid.dcql.ForeignAssociation;
 import gov.nih.nci.cagrid.dcql.ForeignPredicate;
 import gov.nih.nci.cagrid.dcql.JoinCondition;
 import gov.nih.nci.cagrid.dcql.Object;
 
 /**
  * Builds the {@link ConstraintsBuilderResult} for the given constraints. It
  * parses all the constraints, starting from the root expression. The processing
  * is as follows : <br>
  * 1. Form a
  * {@link edu.wustl.cab2b.server.queryengine.querybuilders.dcql.constraints.DcqlConstraint}
  * for each {@link edu.wustl.common.querysuite.queryobject.IExpression}. In this
  * step, the expressions are parsed in a bottom-up fashion, i.e. the constraint
  * for one expression is built only after all its children expressions have been
  * processed. This step results in each expression also being constrained by the
  * children expressions. <br>
  * 2. The expression tree is then traversed in a breadth-first fashion, and each
  * expression is now constrained by its parents. (this is temporary, and this
  * step may change when we move to multiple outputs).
  * 
  * @author srinath_k
  */
 public class ConstraintsBuilder {
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(ConstraintsBuilder.class);
 
     private static final String DCQL_WILDCARD = "%";
 
     /**
      * This is probably temporary (might be removed when multiple outputs are to
      * be supported)
      */
     private static final boolean constrainByParentExpressions = true;
 
     private ConstraintsBuilderResult result;
 
     private ICab2bQuery query;
 
     private CategoryPreprocessorResult categoryPreprocessorResult;
 
     private IExpression rootExpr;
 
     /**
      * @return the rootExpr.
      */
     public IExpression getRootExpr() {
         return rootExpr;
     }
 
     /**
      * @param rootExpr
      *            the rootExpr to set.
      */
     public void setRootExpr(IExpression rootExpr) {
         this.rootExpr = rootExpr;
     }
 
     /**
      * Constructor for ConstraintsBuilder
      * 
      * @param query
      * @param categoryPreprocessorResult
      */
     public ConstraintsBuilder(ICab2bQuery query, CategoryPreprocessorResult categoryPreprocessorResult) {
         logger.info("JJJ ConstraintsBuilder");
 
         setQuery(query);
         setCategoryPreprocessorResult(categoryPreprocessorResult);
         setResult(new ConstraintsBuilderResult());
         setRootExpr(findRootExpression());
     }
 
     /**
      * Build constraints
      * 
      * @return reference of ConstraintsBuilderResult
      */
     public ConstraintsBuilderResult buildConstraints() {
         createDcqlConstraintForExpression(getRootExpr());
         ConstraintsBuilderResult res = getResult();
         logger.info("JJJ buildConstraints. getResult()="+res);
 
         if (!constrainByParentExpressions) {
             return res;
         }
         constrainByParentExpressions();
         return getResult();
     }
 
     // BEGIN CONSTRAINING EACH EXPR BY PARENTS
     /**
      * Returns set of category output expressions.
      * @return
      */
     private Set<Integer> getCategoryOutputExpressionIds() {
         Set<TreeNode<IExpression>> outputExpressions =
                 getCategoryPreprocessorResult().getExprsSourcedFromCategories().get(getQuery().getOutputEntity());
         logger.info("JJJ getCategoryOutputExpIds:  getQUery().getOE.name="+getQuery().getOutputEntity().getName()+"  OE.id="+getQuery().getOutputEntity().getId()+" outputExpressions:"+outputExpressions);
         Set<Integer> outputExpressionIds = new HashSet<Integer>(outputExpressions.size());
 
         for (TreeNode<IExpression> exprNode : outputExpressions) {
             outputExpressionIds.add(exprNode.getValue().getExpressionId());
         }
         logger.info("JJJ getCatOEIds() returning"+outputExpressionIds);
         return outputExpressionIds;
     }
 
     private EntityInterface getOutputEntity() {
         return getQuery().getOutputEntity();
     }
 
     private List<Integer> exprIdList(List<IExpression> exprList) {
         List<Integer> res = new ArrayList<Integer>();
         for (IExpression expr : exprList) {
             res.add(expr.getExpressionId());
         }
         return res;
     }
 
     private void constrainByParentExpressions() {
         boolean isCategoryOutput = Utility.isCategory(getOutputEntity());
         logger.info("JJJ constrainByParentExpressions.  isCategory??"+isCategoryOutput);
 
         Set<Integer> outputExprIds = null;
         if (isCategoryOutput) {
             outputExprIds = getCategoryOutputExpressionIds();
         }
         Set<Integer> currExprIds = new HashSet<Integer>();
         currExprIds.add(getRootExpr().getExpressionId());
 
         while (!currExprIds.isEmpty()) {
             Set<Integer> nextExprIds = new HashSet<Integer>();
             for (Integer currExprId : currExprIds) {
                 IExpression currExpr = getExpression(currExprId);
                 List<Integer> parentExprIds = exprIdList(getJoinGraph().getParentList(currExpr));
 
                 // assert processedExpressions.containsAll(parentExprIds);
                 constrainChildByParents(currExprId, parentExprIds);
 
                 // do "constrain by parents" only till the output expressions.
                 boolean isOutputExpr =
                         (isCategoryOutput && outputExprIds.contains(currExprId))
                                 || currExpr.getQueryEntity().equals(getOutputEntity());
                 if (!isOutputExpr) {
                     nextExprIds.addAll(exprIdList(getJoinGraph().getChildrenList(currExpr)));
                 }
             }
             currExprIds = nextExprIds;
         }
         // assert
         // processedExpressions.equals(getResult().getExpressionToConstraintMap().keySet());
     }
 
     private void constrainChildByParents(int childExprId, List<Integer> parentExprIds) {
         IExpression childExpr = getExpression(childExprId);
         DcqlConstraint childConstraint = getResult().getConstraintForExpression(childExpr);
 
         LogicalOperator operator = LogicalOperator.And;
         if (query.isKeywordSearch()) {
             operator = LogicalOperator.Or;
         }
         Cab2bGroup group = new Cab2bGroup(operator);
         group.addConstraint(childConstraint);
 
         for (Integer parentExprId : parentExprIds) {
             IExpression parentExpr = getExpression(parentExprId);
             IAssociation association = getJoinGraph().getAssociation(parentExpr, childExpr);
             if (!association.isBidirectional()) {
                 logger.warn("Unidirectional association found " + association + ". Results could be incorrect.");
                 continue;
             }
             DcqlConstraint parentConstraint = getResult().getConstraintForExpression(parentExpr);
             AbstractAssociationConstraint associationConstraint = createAssociation(association.reverse());
             associationConstraint.addChildConstraint(parentConstraint);
             group.addConstraint(associationConstraint);
         }
         getResult().putConstraintForExpression(childExpr, group.getDcqlConstraint(), true);
     }
 
     // END CONSTRAINING EACH EXPR BY PARENTS
     private IExpression findRootExpression() {
         try {
             return getConstraints().getRootExpression();
         } catch (MultipleRootsException e) {
             String msg = "Invalid query object submitted; it's got multiple roots...";
             logger.error(msg);
             throw new RuntimeException(msg, e, ErrorCodeConstants.QUERY_INVALID_INPUT);
         }
     }
 
     private boolean isExprRedundant(int exprId) {
         return getCategoryPreprocessorResult().getRedundantExprs().contains(getExpression(exprId));
     }
 
     private DcqlConstraint createDcqlConstraintForExpression(IExpression expr) {
         List<DcqlConstraint> dcqlConstraintsList = new ArrayList<DcqlConstraint>(expr.numberOfOperands());
         logger.info("JJJ createDcqlConstForExp.  #operands="+expr.numberOfOperands());
         for (int i = 0; i < expr.numberOfOperands(); i++) {
             IExpressionOperand operand = expr.getOperand(i);
             if (operand instanceof IExpression) {
                 IExpression childExpr = (IExpression) operand;
                 logger.info("JJJ createDcqlConstForExp. adding expression with child="+childExpr);
 
                 dcqlConstraintsList.add(createDcqlConstraintForChildExpression(expr.getExpressionId(), childExpr
                     .getExpressionId()));
             } else if (operand instanceof IRule) {
                 logger.info("JJJ createDcqlConstForExp. adding IRule="+operand);
 
                 dcqlConstraintsList.add(createDcqlConstraintForRule((IRule) operand));
             } else {
                 throw new RuntimeException("uknown operand type.");
             }
         }
         DcqlConstraint dcqlConstraint = mergeConstraints(expr, dcqlConstraintsList);
         getResult().putConstraintForExpression(expr, dcqlConstraint, !constrainByParentExpressions);
 
         return dcqlConstraint;
     }
 
     private DcqlConstraint mergeConstraints(IExpression expr, List<DcqlConstraint> dcqlConstraintsList) {
         if (dcqlConstraintsList.size() == 0) {
             return new DcqlConstraint();
         }
         if (dcqlConstraintsList.size() == 1) {
             return dcqlConstraintsList.get(0);
         }
         return new GroupBuilder(getConnectors(expr), dcqlConstraintsList).buildGroup();
     }
 
     private List<IConnector<LogicalOperator>> getConnectors(IExpression expression) {
         List<IConnector<LogicalOperator>> conns = new ArrayList<IConnector<LogicalOperator>>();
         for (int i = 0; i < expression.numberOfOperands() - 1; i++) {
             conns.add(expression.getConnector(i, i + 1));
         }
         return conns;
     }
 
     private DcqlConstraint createDcqlConstraintForChildExpression(int parentExpressionId, int childExpressionId) {
         IExpression childExpr = getConstraints().getExpression(childExpressionId);
         DcqlConstraint childExprDcqlConstraint;
 
         logger.info("JJJ createDcqlConstForCHILDExp. adding parentID="+parentExpressionId);
 
         if (getResult().containsExpression(childExpr)) {
             childExprDcqlConstraint = getResult().getConstraintForExpression(childExpr);
         } else {
             childExprDcqlConstraint = createDcqlConstraintForExpression(childExpr);
         }
 
         DcqlConstraint dcqlConstraint;
         if (isExprRedundant(childExpressionId)) {
             dcqlConstraint = createAnyConstraint();
         } else {
             IAssociation association = getJoinGraph().getAssociation(getExpression(parentExpressionId), childExpr);
 
             AbstractAssociationConstraint associationConstraint = createAssociation(association);
             associationConstraint.addChildConstraint(childExprDcqlConstraint);
 
             dcqlConstraint = associationConstraint;
         }
         logger.info("JJJ createDcqlConstForCHILDExp. returning"+dcqlConstraint);
 
         return dcqlConstraint;
     }
 
     private DcqlConstraint createAnyConstraint() {
         return new DcqlConstraint();
     }
 
     /**
      * Creates association intermodel/intramodel 
      * @param association
      * @return association intermodel/intramodel
      */
     public static AbstractAssociationConstraint createAssociation(IAssociation association) {
         AbstractAssociationConstraint dcqlConstraint = null;
         if (association instanceof IIntraModelAssociation) {
             dcqlConstraint = createLocalAssociation((IIntraModelAssociation) association);
         } else if (association instanceof IInterModelAssociation) {
         	logger.info("JJJ calling CREATEASSOCIATIONFOREIGN"+association.toString());
 
             dcqlConstraint = createForeignAssociation((IInterModelAssociation) association);
 
         }
         return dcqlConstraint;
     }
 
     private static LocalAssociationConstraint createLocalAssociation(IIntraModelAssociation intraModelAssociation) {
         Association association = new Association();
 
         AssociationInterface deAssociation = intraModelAssociation.getDynamicExtensionsAssociation();
         association.setRoleName(deAssociation.getTargetRole().getName());
         association.setName(deAssociation.getTargetEntity().getName());
 
         return new LocalAssociationConstraint(association);
     }
 
     private static ForeignAssociationConstraint createForeignAssociation(
                                                                          IInterModelAssociation interModelAssociation) {
         // 1. create foreign object
         // TODO multiple urls...
         String rightServiceUrl = interModelAssociation.getTargetServiceUrl();
 
         Object foreignObject = new Object();
         ForeignAssociation association = new ForeignAssociation();
         JoinCondition joinCondition = new JoinCondition();
         joinCondition.setPredicate(ForeignPredicate.EQUAL_TO);
         logger.info("JJJ Set:"+interModelAssociation.getTargetEntity().getName()+"s URL to:"+rightServiceUrl);
         foreignObject.setName(interModelAssociation.getTargetEntity().getName());
         association.setTargetServiceURL(rightServiceUrl);
 
         // 2. create join condition
         joinCondition.setLocalAttributeName(interModelAssociation.getSourceAttribute().getName());
         joinCondition.setForeignAttributeName(interModelAssociation.getTargetAttribute().getName());
         association.setForeignObject(foreignObject);
         association.setJoinCondition(joinCondition);
 
         return new ForeignAssociationConstraint(association);
     }
 
     private DcqlConstraint createDcqlConstraintForRule(IRule rule) {
         LogicalOperator operator = LogicalOperator.And;
         if (query.isKeywordSearch()) {
             operator = LogicalOperator.Or;
         }
         Cab2bGroup group = new Cab2bGroup(operator);
 
         for (int i = 0; i < rule.size(); i++) {
             group.addConstraint((createConstraintForCondition(rule.getCondition(i))));
         }
         return group.getDcqlConstraint();
     }
 
     private AttributeConstraint createAttribute(String attributeName, RelationalOperator operator,
                                                 DataType dataType) {
         return createAttributeConstraint(attributeName, operator, null, dataType);
     }
 
     /**
      * Returns AttributeConstraint
      * @param attributeName
      * @param operator
      * @param value
      * @param dataType
      * @return AttributeConstraint
      */
     public static AttributeConstraint createAttributeConstraint(String attributeName, RelationalOperator operator,
                                                                 String value, DataType dataType) {
         Attribute attribute = new Attribute();
         attribute.setName(attributeName);
         attribute.setPredicate(TransformerUtil.getCqlPredicate(operator));
         if (value != null) {
             attribute.setValue(modifyValue(value, operator, dataType));
         }
         return new AttributeConstraint(attribute);
     }
 
     private static String modifyDateValue(String value) {
         // TODO this doesn't work... may need to follow up with Scott/ later
         // cagrid releases.
         // return DateUtils.getDateString(Date.valueOf(value));
         return value;
     }
 
     private static String modifyValue(String value, RelationalOperator operator, DataType dataType) {
         if (dataType == DataType.Date) {
             return modifyDateValue(value);
         }
         if (operator == RelationalOperator.StartsWith) {
             return value + DCQL_WILDCARD;
         }
         if (operator == RelationalOperator.EndsWith) {
             return DCQL_WILDCARD + value;
         }
         if (operator == RelationalOperator.Contains) {
             return DCQL_WILDCARD + value + DCQL_WILDCARD;
         }
         return value;
     }
 
     private DcqlConstraint createBetweenCondition(String attributeName, String value1, String value2,
                                                   DataType dataType) {
         Cab2bGroup group = new Cab2bGroup(LogicalOperator.And);
         boolean swapVals;
         switch (dataType) {
             case Integer:
             case Long: {
                 long long1 = Long.parseLong(value1);
                 long long2 = Long.parseLong(value2);
                 swapVals = long1 > long2;
                 break;
             }
             case Float:
             case Double: {
                 double d1 = Double.parseDouble(value1);
                 double d2 = Double.parseDouble(value2);
                 swapVals = d1 > d2;
                 break;
             }
             case Date: {
/*This does not work since format is YYYY/MM/DD
                 Date d1 = Date.valueOf(value1);
                 Date d2 = Date.valueOf(value2);
                 swapVals = d1.compareTo(d2) > 0;
*/
		swapVals = value1.compareTo(value2) > 0;
                 break;
             }
             default:
                 throw new RuntimeException("Invalid datatype for Between operator.");
         }
         if (swapVals) {
             String temp = value1;
             value1 = value2;
             value2 = temp;
         }
         group.addConstraint((createAttributeConstraint(attributeName, RelationalOperator.GreaterThanOrEquals,
                                                        value1, dataType)));
         group.addConstraint((createAttributeConstraint(attributeName, RelationalOperator.LessThanOrEquals, value2,
                                                        dataType)));
         return group.getDcqlConstraint();
     }
 
     private DcqlConstraint createInCondition(String attributeName, List<String> values, DataType dataType) {
         Cab2bGroup group = new Cab2bGroup(LogicalOperator.Or);
         for (String value : values) {
             group.addConstraint((createAttributeConstraint(attributeName, RelationalOperator.Equals, value,
                                                            dataType)));
         }
         return group.getDcqlConstraint();
     }
 
     private DcqlConstraint createNotInCondition(String attributeName, List<String> values, DataType dataType) {
         Cab2bGroup group = new Cab2bGroup(LogicalOperator.And);
         for (String value : values) {
             group.addConstraint((createAttributeConstraint(attributeName, RelationalOperator.NotEquals, value,
                                                            dataType)));
         }
         return group.getDcqlConstraint();
     }
 
     private DcqlConstraint createConstraintForCondition(ICondition condition) {
         String attributeName = condition.getAttribute().getName();
         RelationalOperator operator = condition.getRelationalOperator();
         DataType dataType = Utility.getDataType(condition.getAttribute().getAttributeTypeInformation());
 
         if (operator.numberOfValuesRequired() == 0) {
             return createAttribute(attributeName, operator, dataType);
         }
         if (operator.numberOfValuesRequired() == 1) {
             return createAttributeConstraint(attributeName, operator, condition.getValue(), dataType);
         }
 
         if (condition.getRelationalOperator() == RelationalOperator.Between) {
             return createBetweenCondition(attributeName, condition.getValues().get(0), condition.getValues()
                 .get(1), dataType);
         }
 
         if (condition.getRelationalOperator() == RelationalOperator.In) {
             return createInCondition(attributeName, condition.getValues(), dataType);
 
         }
         if (condition.getRelationalOperator() == RelationalOperator.NotIn) {
             return createNotInCondition(attributeName, condition.getValues(), dataType);
 
         }
         // will never occur.
         return null;
     }
 
     /**
      * @return the result.
      */
     private ConstraintsBuilderResult getResult() {
         return result;
     }
 
     /**
      * @param result the result to set.
      */
     private void setResult(ConstraintsBuilderResult result) {
         this.result = result;
     }
 
     /**
      * @return IConstraints
      */
     public IConstraints getConstraints() {
         return getQuery().getConstraints();
     }
 
     /**
      * @return IJoinGraph
      */
     private IJoinGraph getJoinGraph() {
         return getConstraints().getJoinGraph();
     }
 
     /**
      * @param expressionId expression Id
      * @return IExpression
      */
     private IExpression getExpression(int expressionId) {
         return getConstraints().getExpression(expressionId);
     }
 
     /**
      * @return category Preprocessor Result
      */
     public CategoryPreprocessorResult getCategoryPreprocessorResult() {
     	logger.info("JJJ getCategoryPreProcessResult returning:"+categoryPreprocessorResult);
         return categoryPreprocessorResult;
     }
 
     /**
      * @param categoryPreprocessorResult category Preprocessor Result to set
      */
     public void setCategoryPreprocessorResult(CategoryPreprocessorResult categoryPreprocessorResult) {
         this.categoryPreprocessorResult = categoryPreprocessorResult;
     }
 
     /**
      * @return The ICab2bQuery
      */
     public ICab2bQuery getQuery() {
         return query;
     }
 
     /**
      * @param query Query to set
      */
     public void setQuery(ICab2bQuery query) {
         this.query = query;
     }
 }
