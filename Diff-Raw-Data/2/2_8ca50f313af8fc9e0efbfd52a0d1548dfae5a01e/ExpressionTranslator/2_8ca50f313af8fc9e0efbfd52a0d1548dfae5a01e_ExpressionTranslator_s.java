 package com.physion.ovation.gui.ebuilder.expression;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 
 import com.physion.ovation.gui.ebuilder.ExpressionBuilder;
 import com.physion.ovation.gui.ebuilder.datatypes.CollectionOperator;
 import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
 import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
 import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
 import com.physion.ovation.gui.ebuilder.datatypes.Type;
 import com.physion.ovation.gui.ebuilder.datatypes.Operator;
 import com.physion.ovation.gui.ebuilder.datamodel.RowData;
 import com.physion.ovation.gui.ebuilder.datamodel.DataModel;
 
 
 /**
  * TODO:  Split the Expression to RowData translator
  * code into a separate class from the
  * RowData to Expression translator code.
  */
 public class ExpressionTranslator {
 
 
     public static final String AE_VALUE = "value";
     public static final String AE_THIS = "this";
 
     public static final String OE_NOT = "not";
     public static final String OE_OR = "or";
     public static final String OE_AND = "and";
     public static final String OE_ALL = "all";
     public static final String OE_COUNT = "count";
     public static final String OE_AS = "as";
     public static final String OE_PARAMETER = "parameter";
     //public static final String OE_MY = "my";
     public static final String OE_ANY = "any";
     public static final String OE_ELEMENTS_OF_TYPE = "elementsOfType";
 
     public static final String OE_EQUALS = "==";
     public static final String OE_NOT_EQUALS = "!=";
     public static final String OE_LESS_THAN = "<";
     public static final String OE_GREATER_THAN = ">";
     public static final String OE_LESS_THAN_EQUALS = "<=";
     public static final String OE_GREATER_THAN_EQUALS = ">=";
     public static final String OE_MATCHES_CASE_SENSITIVE = "=~";
     public static final String OE_MATCHES_CASE_INSENSITIVE = "=~~";
     public static final String OE_DOES_NOT_MATCH_CASE_SENSITIVE = "!~";
     public static final String OE_DOES_NOT_MATCH_CASE_INSENSITIVE = "!~~";
 
     public static final String OE_IS_NULL = "isnull";
     // Note there is no OE_IS_NOT_NULL value.
     public static final String OE_DOT = ".";
 
 
     /**
      * This method turns the passed in ExpressionTree into a
      * RowData object.
      */
     public static RowData createRowData(ExpressionTree expressionTree) {
 
         /**
          * First create the root RowData object.
          * It simply contains the CUQ (Class Under Qualification)
          * and a CollectionOperator.
          *
          * Note that mapping an Expression collection operator into
          * a RowData CollectionOperator, is not always one-to-one.
          * These are one-to-one:
          *
          *      "or" -> CollectionOperator.ANY
          *      "and" -> CollectionOperator.ALL
          *
          * but the RowData CollectionOperator.NONE is represented by
          * the Expression tree having a "not" OperatorExpression with
          * an "or" OperatorExpression as its only operand:
          *
          *      "not(or)" -> CollectionOperator.NONE
          */
         RowData rootRow = RowData.createRootRow();
         rootRow.setClassUnderQualification(
             expressionTree.getClassUnderQualification());
 
         IOperatorExpression oe = (IOperatorExpression)expressionTree.
             getRootExpression();
 
         rootRow.setCollectionOperator(getCOForOE(oe));
 
         /**
          * Convert the oe into a list of child RowData objects and
          * add them to the rootRow.
          */
          /*
         ArrayList<RowData> childRows = getChildRows(getFirstChildOE(oe),
             expressionTree.getClassUnderQualification());
         rootRow.addChildRows(childRows);
         */
         createAndAddChildRows(rootRow, getFirstChildOE(oe),
             expressionTree.getClassUnderQualification());
 
         return(rootRow);
     }
 
 
     /**
      * This returns the first operator of the passed in tree that
      * is NOT the operator associated with the root RowData object.
      * For example, if the Expression starts like this:
      *
      *      OperatorExpression("and")
      *        OperatorExpression("==")
      *          ...
      *
      * this method would return the OperatorExpression("==") object.
      * If the Expression starts like this:
      *
      *      OperatorExpression("not")
      *        OperatorExpression("or")
      *          OperatorExpression("==")
      *            ...
      *  
      * this method would return the OperatorExpression("==") object.
      * If the Expression starts like this:
      *
      *      OperatorExpression("and")
      *        OperatorExpression("not")
      *          OperatorExpression("isnull")
      *            ...
      *
      * this method would return the OperatorExpression("not") object.
      *
      * I.e. this method returns the OperatorExpression that
      * will be used to create the first child row of the the
      * GUI's root row.  (Note, the GUI's root row might use
      * one or two of the first operators in the Expression tree,
      * as demonstrated in the examples above.
      */
     private static IOperatorExpression getFirstChildOE(IOperatorExpression oe) {
         
         if (oe.getOperandList().size() < 1)
             return(null);
 
         if (OE_NOT.equals(oe.getOperatorName())) {
             oe = (IOperatorExpression)(oe.getOperandList().get(0));
             if (oe.getOperandList().size() < 1)
                 return(null);
             return((IOperatorExpression)(oe.getOperandList().get(0)));
         }
         else {
             return((IOperatorExpression)(oe.getOperandList().get(0)));
         }
     }
 
 
     /**
      * Get the list of child RowData objects that describe the passed
      * in expression tree.  This method calls itself recursively to
      * generate the sub-tree of RowData objects.
      *
      * This method should NOT be called with the OperatorExpression
      * that is the root operator (or operators) of the Expression tree.
      * That operator(s) must be handled a bit differently to generate
      * the root RowData object.  See getFirstChildOE() and the method
      * that calls it for more information about generating the root
      * RowData object in the GUI.
      *
      * This method never returns null, but might return an empty list.
      *
      * @param classDescription The ClassDescription that is the "parent"
      * class for all the child rows that will be created.  So, if we
      * are creating child rows for the topmost row in the GUI, this
      * would be the Class Under Qualification.
      */
     private static ArrayList<RowData> getChildRows(IOperatorExpression oe,
         ClassDescription classDescription) {
 
         /*
         System.out.println("\nEnter getChildRows()");
         System.out.println("oe: "+(Expression)oe);
         System.out.println("classDescription: "+classDescription);
         */
         ArrayList<RowData> childRows = new ArrayList<RowData>();
 
         /**
          * If the oe is null, return an empty list.
          */
         if (oe == null)
             return(childRows);
 
         ArrayList<IExpression> ol = oe.getOperandList();
 
         RowData rowData = new RowData();
 
         CollectionOperator collectionOperator = getCOForOE(oe);
 
         /**
          * Handle the special case of the Count collection operator.
          */
         if (collectionOperator == null) {
             if (oe.getOperandList().size() > 0) {
                 IExpression ex = oe.getOperandList().get(0);
                 if (ex instanceof IOperatorExpression) {
                     IOperatorExpression oe2 = (IOperatorExpression)ex;
                     if (OE_COUNT.equals(oe2.getOperatorName())) {
                         collectionOperator = getCOForOE(oe2);
                     }
                 }
             }
         }
         //System.out.println("collectionOperator = "+collectionOperator);
 
         if (collectionOperator == CollectionOperator.NONE) {
             /**
              * We need to "skip ahead" one operator because
              * the NONE collection operator is implemented in
              * the Expression syntax as two OperatorExpressions:
              *
              *      OperatorExpression(not)
              *        OperatorExpression(or)
              */
             ol = ((IOperatorExpression)ol.get(0)).getOperandList();
         }
 
         Operator attributeOperator = getAOForOE(oe);
         //System.out.println("attributeOperator = "+attributeOperator);
 
         if (collectionOperator != null) {
             rowData.setCollectionOperator(collectionOperator);
 
             if (attributeOperator != null) {
                 setAttributeOperatorPathAndValue(rowData, ol, classDescription,
                                                  attributeOperator);
             }
 
             if (collectionOperator.isCompoundOperator()) {
 
                 int olIndex = 0;
                 ClassDescription childClass = classDescription;
                 if (ol.size() > 1) {
                     /**
                      * Convert the first operand into a RowData
                      * attributePath.
                      */
                     IExpression firstOperand = ol.get(olIndex++);
                     setAttributePath(rowData, firstOperand, classDescription);
                     //System.out.println("rowData so far: "+
                     //    rowData.getRowString());
                     Attribute childmostAttribute =
                         rowData.getChildmostAttribute();
                     childClass = childmostAttribute.getClassDescription();
                 }
                 //System.out.println("childClass: "+childClass);
 
                 /**
                  * When we get here, we know that the row is one
                  * of two kinds:
                  *
                  * Type 1)
                  *
                  *      The first operand was an Expression that told
                  *      us what attribute is being queried.  The operands
                  *      AFTER the first operand are the Expressions that 
                  *      are being tested.  For example, the first operand
                  *      might be AttributeExpression(responses).  The following
                  *      operands will be subtrees that test attributes
                  *      of a "response" attribute.
                  *
                  * Type 2)
                  *
                  *      The row is a compound row that only has a
                  *      collection operator in it.
                  *      For example, the row says:  "Any of the following",
                  *      "All of the following", "None of the following".
                  *      So, there is a list of operands that are the
                  *      children of this Any/All/None collection operator.
                  *
                  * In either case, we need to loop through the list
                  * of operands, calling ourselves recursively on each one.
                  */
                 for (; olIndex < ol.size(); olIndex++) {
 
                     //System.out.println("operand = "+
                     //    ((Expression)ol.get(olIndex)).toString(""));
                     IOperatorExpression operand =
                         (IOperatorExpression)ol.get(olIndex);
                     createAndAddChildRows(rowData, operand, childClass);
                 }
             }
         }
         else {
             if (attributeOperator != null) {
                 /**
                  * TODO: Perhaps put this "if (attributeOperator != null)"
                  * block outside of this else block.  The same code
                  * is executed above also.
                  */
                 setAttributeOperatorPathAndValue(rowData, ol, classDescription,
                                                  attributeOperator);
             }
         }
 
         childRows.add(rowData);
 
         return(childRows);
     }
 
 
     /**
      * This method will set the attributeOperator, attributePath,
      * and attributeValue of the passed in rowData based on the
      * passed in values.
      */
     private static void setAttributeOperatorPathAndValue(RowData rowData,
         ArrayList<IExpression> operandList, ClassDescription classDescription,
         Operator attributeOperator) {
 
         rowData.setAttributeOperator(attributeOperator);
 
         /**
          * Convert the first (left) operand into a RowData
          * attributePath.
          */
         IExpression ex = operandList.get(0);
         setAttributePath(rowData, ex, classDescription);
 
         /**
          * Now handle the second (right) operand.
          */
         if ((attributeOperator != Operator.IS_NULL) &&
             (attributeOperator != Operator.IS_NOT_NULL) &&
             (attributeOperator != Operator.IS_TRUE) &&
             (attributeOperator != Operator.IS_FALSE)) {
             
             ILiteralValueExpression lve;
             lve = (ILiteralValueExpression)operandList.get(1);
             Attribute attribute = rowData.getChildmostAttribute();
             Object attributeValue = createAttributeValue(
                 lve, attribute.getType());
             rowData.setAttributeValue(attributeValue);
         }
     }
 
 
     private static void createAndAddChildRows(RowData rowData,
         IOperatorExpression oe, ClassDescription classDescription) {
 
         ArrayList<RowData> childRows = getChildRows(oe, classDescription);
         rowData.addChildRows(childRows);
     }
 
 
     /**
      * Set the value of the rowData's attributePath to be the
      * equivalent of the value in the Expression.
      *
      * @param rowData The RowData object whose attributePath we will set.
      *
      * @param ex The Expression that is the left operand of an operator.
      */
     private static void setAttributePath(RowData rowData, IExpression ex,
         ClassDescription classDescription) {
 
         /*
         System.out.println("Enter setAttributePath");
         System.out.println("rowData: "+rowData.getRowString());
         System.out.println("classDescription: "+classDescription);
         */
         ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
 
         appendToAttributePath(attributePath, ex, classDescription);
 
         for (Attribute attribute : attributePath) {
             rowData.addAttribute(attribute);
         }
     }
 
 
     /**
      * This returns true if the passed in IExpression is
      * a PER_USER Attribute like "keywords", "mykeywords", etc.
      */
     private static boolean isPerUserOperator(IExpression ex,
                                              ClassDescription cd) {
 
         if (ex instanceof IOperatorExpression) {
 
             IOperatorExpression oe = (IOperatorExpression)ex;
             String name = oe.getOperatorName();
 
             Attribute attribute = cd.getAttribute(name);
             if ((attribute != null) &&
                 (attribute.getType() == Type.PER_USER)) {
                 return(true);
             }
         }
 
         return(false);
     }
 
 
     /**
      * Append the passed in Expression to the passed in attributePath.
      * It converts the passed in Expression tree into Attributes
      * and adds them to the attributePath.
      *
      * Please note, this method calls itself recursively.
      * So, for each RowData object, this method is only called once
      * by another function, but after that initial call with
      * the "top" expression, this method calls itself recursively
      * to create the entire path.
      *
      * @return The ClassDescription of the leftmost Attribute
      * on which this method is currently working.  If the
      * Attribute is a primitive, (e.g. int, string), this returns null.
      */
     private static ClassDescription appendToAttributePath(
         ArrayList<Attribute> attributePath, IExpression ex,
         ClassDescription classDescription) {
 
         if ((ex instanceof IAttributeExpression) ||
             (isPerUserOperator(ex, classDescription))) {
 
             String name;
             if (ex instanceof IAttributeExpression) {
                 IAttributeExpression ae = (IAttributeExpression)ex;
                 name = ae.getAttributeName();
             }
             else {
                 IOperatorExpression oe = (IOperatorExpression)ex;
                 name = oe.getOperatorName();
             }
 
             Attribute attribute = getAttribute(name, classDescription);
 
             if (attribute == null) {
                 String s = "Attribute name \""+name+
                     "\" does not exist in class \""+classDescription.getName()+
                     "\"";
                 (new Exception(s)).printStackTrace();
             }
             else {
                 attributePath.add(attribute);
                 return(attribute.getClassDescription());
             }
         }
         else if (ex instanceof IOperatorExpression) {
 
             IOperatorExpression oe = (IOperatorExpression)ex;
             if (OE_DOT.equals(oe.getOperatorName())) {
 
                 IExpression op = oe.getOperandList().get(0);
                 ClassDescription childClass;
                 childClass = appendToAttributePath(attributePath, op,
                                                    classDescription);
 
                 if (oe.getOperandList().size() > 1) {
                     op = oe.getOperandList().get(1);
                     return(appendToAttributePath(attributePath, op,
                                                  childClass));
                 }
                 else {
                     /**
                      * This should never happen.  A dot operator must
                      * always have two operands.
                      */
                 }
             }
             else if (OE_COUNT.equals(oe.getOperatorName())) {
                 IExpression op = oe.getOperandList().get(0);
                 return(appendToAttributePath(attributePath, op,
                                              classDescription));
             }
             /*
             else if (OE_ANY.equals(oe.getOperatorName())) {
             }
             else if (OE_OR.equals(oe.getOperatorName())) {
             }
             else if (OE_ALL.equals(oe.getOperatorName())) {
             }
             */
             else {
                 String s = "Unhandled IOperatorExpression: "+
                     oe.getOperatorName();
                 (new Exception(s)).printStackTrace();
             }
         }
         else {
             (new Exception("Unhandled IExpression")).printStackTrace();
         }
         return(null);
     }
 
 
     private static Attribute getAttribute(String attributeName,
         ClassDescription classDescription) {
 
         Attribute attribute = classDescription.getAttribute(attributeName);
         
         return(attribute);
     }
 
 
     /**
      * Convert the passed in ILiteralValueExpression into an attributeValue
      * that the RowData object expects.
      *
      * In the case of an IInt32LiteralValueExpression we also need
      * the passed in Type to know what type of object to create.
      * This is because the Expression structure does not have a
      * IInt16LiteralValueExpression class.  (All integer values are
      * the same.)
      */
     private static Object createAttributeValue(ILiteralValueExpression lve,
                                                Type type) {
     
         Object attributeValue = null;
 
         /**
          * Some values in the Expression tree are already the
          * correct object type for the RowData object.
          *
          * But, Expression IInt32LiteralValueExpression values
          * need to compared to the DataModel to figure out
          * whether the attributeValue should be an Integer or a
          * Short object.  (INT_16 or INT_32)
          */
         if ((lve instanceof ITimeLiteralValueExpression) ||
             (lve instanceof IStringLiteralValueExpression) ||
             (lve instanceof IFloat64LiteralValueExpression)) {
             return(lve.getValue());
         }
         else if (lve instanceof IBooleanLiteralValueExpression) {
             /**
              * We should not be passed an IBooleanLiteralValueExpression
              * to turn into an attributeValue, because booleans are
              * handled via the special Operator.IS_TRUE
              * The caller should have figured that out and not called us.
              */
             (new Exception("Unhandled ILiteralValueExpression subclass")).
                 printStackTrace();
             return(null);
         }
         else if (lve instanceof IInt32LiteralValueExpression) {
             /**
              * Look at the DataModel to figure out whether this
              * should be a Short or an Integer.
              */
             if (type == Type.INT_16) {
                 int value = (Integer)lve.getValue();
                 return(new Short((short)value));
             }
             else {
                 /**
                  * The value is already an Integer object.
                  * (Or at least it should be!)
                  */
                 return(lve.getValue());
             }
         }
         else {
             (new Exception("Unhandled ILiteralValueExpression subclass")).
                 printStackTrace();
             return(new String("ERROR"));
         }
     }
 
 
     /*
     private static boolean isCollectionOperator(String operatorString) {
 
         if (OE_OR.equals(operatorString) ||
             OE_NOT.equals(operatorString) ||
 
     }
     */
 
     private static OperatorExpression getOEForCO(CollectionOperator co,
                                                  boolean isCompoundOperator) {
 
         if (isCompoundOperator) {
             switch (co) {
                 case ANY:
                     return(new OperatorExpression(OE_OR));
 
                 case ALL:
                     return(new OperatorExpression(OE_AND));
 
                 case NONE:
                     OperatorExpression notOE = new OperatorExpression(OE_NOT);
                     notOE.addOperand(new OperatorExpression(OE_OR));
                     return(notOE);
 
                 default:
                     String s = "Illegal collectionOperator in rowData?";
                     (new Exception(s)).printStackTrace();
                     return(null);
             }
         }
         else {
             switch (co) {
                 case ANY:
                     return(new OperatorExpression(OE_ANY));
 
                 case ALL:
                     return(new OperatorExpression(OE_ALL));
 
                 case NONE:
                     OperatorExpression notOE = new OperatorExpression(OE_NOT);
                     notOE.addOperand(new OperatorExpression(OE_ANY));
                     return(notOE);
 
                 case COUNT:
                     return(new OperatorExpression(OE_COUNT));
 
                 default:
                     String s = "Illegal collectionOperator in rowData?";
                     (new Exception(s)).printStackTrace();
                     return(null);
             }
         }
     }
 
 
     /**
      * Get the CollectionOperator that is equivalent to the passed
      * in OperatorExpression.
      *
      * If the passed in OperatorExpression cannot be mapped to a
      * CollectionOperator, this method returns null.
      */
     private static CollectionOperator getCOForOE(IOperatorExpression oe) {
 
         if ((OE_OR.equals(oe.getOperatorName())) ||
             (OE_ANY.equals(oe.getOperatorName()))) {
             return(CollectionOperator.ANY);
         }
         else if ((OE_AND.equals(oe.getOperatorName())) ||
                  (OE_ALL.equals(oe.getOperatorName()))) {
             return(CollectionOperator.ALL);
         }
         else if (OE_NOT.equals(oe.getOperatorName())) {
             oe = (IOperatorExpression)(oe.getOperandList().get(0));
             if (OE_OR.equals(oe.getOperatorName())) {
                 return(CollectionOperator.NONE);
             }
         }
         else if (OE_COUNT.equals(oe.getOperatorName())) {
             return(CollectionOperator.COUNT);
         }
 
         //System.err.println("ERROR:  ExpressionTranslator.getCOForOE()"+
         //    "\nCode must be updated to handle this type of expression.");
         return(null);
     }
 
 
     /**
      * Get the attribute Operator that is equivalent to the passed
      * in IOperatorExpression.
      *
      * If the passed in IOperatorExpression cannot be mapped to a
      * attribute Operator, this method returns null.
      * Note, that is not necessarily and error.
      */
     private static Operator getAOForOE(IOperatorExpression oe) {
 
         /**
          * Note there is no OE_IS_NOT_NULL, OE_IS_TRUE, OE_IS_FALSE.
          * Those values are handled differently in an Expression and
          * in a RowData.
          */
 
         if (OE_EQUALS.equals(oe.getOperatorName())) {
 
             IExpression ex = null;
             if (oe.getOperandList().size() > 1)
                 ex = oe.getOperandList().get(1);
 
             if (ex instanceof IBooleanLiteralValueExpression) {
 
                 IBooleanLiteralValueExpression blve;
                 blve = (IBooleanLiteralValueExpression)ex;
                 Boolean value = (Boolean)blve.getValue();
                 if (value.booleanValue() == true)
                     return(Operator.IS_TRUE);
                 else
                     return(Operator.IS_FALSE);
             }
             else {
                 return(Operator.EQUALS);
             }
         }
         else if (OE_NOT_EQUALS.equals(oe.getOperatorName())) {
             return(Operator.NOT_EQUALS);
         }
         else if (OE_LESS_THAN.equals(oe.getOperatorName())) {
             return(Operator.LESS_THAN);
         }
         else if (OE_GREATER_THAN.equals(oe.getOperatorName())) {
             return(Operator.GREATER_THAN);
         }
         else if (OE_LESS_THAN_EQUALS.equals(oe.getOperatorName())) {
             return(Operator.LESS_THAN_EQUALS);
         }
         else if (OE_GREATER_THAN_EQUALS.equals(oe.getOperatorName())) {
             return(Operator.GREATER_THAN_EQUALS);
         }
         else if (OE_MATCHES_CASE_SENSITIVE.equals(oe.getOperatorName())) {
             return(Operator.MATCHES_CASE_SENSITIVE);
         }
         else if (OE_MATCHES_CASE_INSENSITIVE.equals(oe.getOperatorName())) {
             return(Operator.MATCHES_CASE_INSENSITIVE);
         }
         else if (OE_DOES_NOT_MATCH_CASE_SENSITIVE.equals(
                  oe.getOperatorName())) {
             return(Operator.DOES_NOT_MATCH_CASE_SENSITIVE);
         }
         else if (OE_DOES_NOT_MATCH_CASE_INSENSITIVE.equals(
                  oe.getOperatorName())) {
             return(Operator.DOES_NOT_MATCH_CASE_INSENSITIVE);
         }
         else if (OE_IS_NULL.equals(oe.getOperatorName())) {
             return(Operator.IS_NULL);
         }
         else if (OE_NOT.equals(oe.getOperatorName())) {
             oe = (IOperatorExpression)(oe.getOperandList().get(0));
             if (OE_IS_NULL.equals(oe.getOperatorName())) {
                 return(Operator.IS_NOT_NULL);
             }
             // What if we get here?
         }
 
         /**
          * The passed in IOperatorExpression cannot be mapped to an
          * attribute Operator.
          */
         return(null);
     }
 
 
     /**
      * Create an Expression from the passed in root RowData.
      * This method should only be called if the passed in
      * RowData object is the rootRow of an expression tree.
      */
     public static ExpressionTree createExpressionTree(RowData rootRow) {
 
         if (rootRow == null) {
             //System.out.println("ExpressionTranslator.createExpressionTree() "+
             //                   "was passed a null rootRow parameter.");
             return(null);
         }
 
         if (rootRow.isRootRow() == false) {
             System.err.println("ERROR:  "+
                 "ExpressionTranslator.createExpressionTree() "+
                 "was passed a rootRow parameter that is not really the "+
                 "root of an expression tree.");
             return(null);
         }
 
         /**
          * This is the root Expression object.  I.e. the "top" node of the
          * expression tree.
          */
         OperatorExpression rootExpression;
 
         /**
          * In most cases, this will be set to point to the
          * same OpertorExpression object as rootExpression.
          * But, in the case of the None collection operator,
          * this will point to a different OpertorExpression
          * object.
          */
         OperatorExpression lastExpression;
 
         /**
          * Create the root expression.  If the root
          * row uses the None CollectionOperator, that
          * might actually create two operators.  I.e. not(or)
          */
         rootExpression = getOEForCO(rootRow.getCollectionOperator(), true);
         lastExpression = getLastOperator(rootExpression);
 
         /**
          * At this point, lastExpression is a reference to
          * the OperatorExpression that will have the
          * list of operands added to.
          *
          * Now add one IExpression operand to lastExpression for each RowData
          * that is a child of the rootRow.
          */
         for (RowData childRow : rootRow.getChildRows()) {
             lastExpression.addOperand(createExpression(childRow));
         }
 
         ExpressionTree expressionTree = new ExpressionTree(
             rootRow.getClassUnderQualification(), rootExpression);
         return(expressionTree);
     }
 
 
     /**
      * Create the operator, or operators, for the passed in
      * RowData object.
      *
      * Most of the time, only one operator needs to be created.
      * For example, the RowData attributeOperators "==", "any",
      * "is null", can all be represented by a single
      * OpertorExpression object.
      *
      * But, some operators need two OpertorExpression
      * objects to represent them.  For example, the "None"
      * CollectionOperator becomes the "not" operator with
      * the "or" operator as its only operand if the rowData
      * is the root row.  I.e. not(or())  It becomes not(any())
      * for rows other than the root row.
      * The "is not null" attribute operator becomes the
      * "not" operator with the "is null" operator as its
      * only operand.
      *
      * So, this method returns an OperatorExpression which
      * might have an operand that is another OperatorExpression.
      * The caller needs to check whether the returned
      * OperatorExpression has an operand.  If it does, then the
      * caller will want to hang the rest of the expression tree
      * of the returned OperatorExpression's first (and only)
      * operand returned from OperatorExpression.getOperandList().
      */
     private static OperatorExpression createOperators(RowData rowData) {
 
         OperatorExpression op1;
         OperatorExpression op2;
 
         Attribute childmostAttribute = rowData.getChildmostAttribute();
         CollectionOperator co = rowData.getCollectionOperator();
         Operator ao = rowData.getAttributeOperator();
 
         if (childmostAttribute == null) {
             return(getOEForCO(co, true));
         }
         else {
             if (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP) {
                 /**
                  * Handle the special case of the PER_USER_PARAMETERS_MAP
                  * type that always has the "any" operator.
                  * Note, in a future release of the GUI, this might change.
                  */
                 return(new OperatorExpression(OE_ANY));
             }
             else if (childmostAttribute.getType() == Type.PARAMETERS_MAP) {
                 return(new OperatorExpression(ao.toString()));
             }
         }
 
         /**
          * If we get here, rowData is not a Compound Row, but
          * it still could have a CollectionOperator.
          */
 
         if (ao == Operator.IS_TRUE) {
             /**
              * Note that the Operator.IS_TRUE and Operator.IS_FALSE
              * are not operators in the Expression tree.  In the
              * Expression tree we use the "==" operator and
              * a BooleanLiteralValueExpression.
              */
             op1 = new OperatorExpression(OE_EQUALS);
         }
         else if (ao == Operator.IS_FALSE) {
             op1 = new OperatorExpression(OE_EQUALS);
         }
         else if (ao == Operator.IS_NULL) {
             op1 = new OperatorExpression(OE_IS_NULL);
         }
         else if (ao == Operator.IS_NOT_NULL) {
             op1 = new OperatorExpression(OE_NOT);
             op2 = new OperatorExpression(OE_IS_NULL);
             op1.addOperand(op2);
         }
         else if (co != null) {
 
             if (co == CollectionOperator.COUNT) {
                 op1 = new OperatorExpression(ao.toString());
                 op2 = getOEForCO(co, false);
                 op1.addOperand(op2);
             }
             else {
                 op1 = getOEForCO(co, false);
             }
         }
         else {
             op1 = new OperatorExpression(ao.toString());
         }
 
         return(op1);
     }
 
 
     /**
      * This method is only meant to be used on an OpertorExpression
      * object that has just been created by the createOperators() method.
      * It either returns the passed in OpertorExpression object or
      * it returns the one and only operand in its operandList.
      */
     private static OperatorExpression getLastOperator(OperatorExpression
         op) {
 
         if (op.getOperandList().size() < 1) {
             return(op);
         }
         else {
             return((OperatorExpression)op.getOperandList().get(0));
         }
     }
 
 
     /**
      * Convert the passed in RowData's CollectionOperator enum value
      * to the string value PQL expects.
      */
     /*
     private static String getOperatorName(RowData rowData) {
 
         if (rowData.getCollectionOperator() != null) {
             return(getCollectionOperatorName(rowData));
         }
         else {
             if (rowData.getAttributeOperator() != null) {
                 return(rowData.getAttributeOperator().toString());
             }
         }
 
         return("ERROR");
     }
     */
 
 
     /**
      * Create an Expression from the passed in RowData object.
      * This method is NOT meant to handle the root RowData object.
      */
     private static OperatorExpression createExpression(RowData rowData) {
 
         OperatorExpression expression;
         OperatorExpression lastOperator;
 
         Attribute childmostAttribute = rowData.getChildmostAttribute();
 
         /**
          * Create the operator that is the "top" node that this
          * method will return.  (The returned OpertorExpression
          * will be placed in the operandList of whatever is
          * at the very top of the expression tree.  For example,
          * the very top of the expression tree might be a single
          * collection operator or the "not" operator with a child
          * operand that is the collection operator.
          */
         expression = createOperators(rowData);
         lastOperator = getLastOperator(expression);
 
         /**
          * At this point, lastOperator is either equal to the
          * same value that is in the expression variable,
          * or it is set to the one and only operand in expression's
          * operandList.
          */
 
         if ((childmostAttribute != null) &&
             (childmostAttribute.getType() == Type.PER_USER_PARAMETERS_MAP)) {
 
             /**
              * As of October 2011, there is only the "properties"
              * and "myproperties" attributes that are of this
              * PER_USER_PARAMETERS_MAP type.
              */
             createAndAddPerUserParametersMap(lastOperator, rowData);
         }
         else if ((childmostAttribute != null) &&
                  (childmostAttribute.getType() == Type.PARAMETERS_MAP)) {
 
             createAndAddParametersMap(lastOperator, rowData);
         }
         else if (rowData.getCollectionOperator() != null) {
 
             if (rowData.getCollectionOperator() == CollectionOperator.COUNT) {
                 /**
                  * This is a row with a CollectionOperator.COUNT like
                  * this:
                  *
                  *      responses Count == 27
                  */
                 lastOperator.addOperand(
                     createExpressionPath(rowData.getAttributePath(), rowData));
                 expression.addOperand(createLiteralValueExpression(
                     Type.INT_32, rowData));
             }
             else {
                 /**
                  * This is an Any/All/None row.
                  */
                 if (rowData.getAttributeCount() > 0) {
                     lastOperator.addOperand(
                         createExpressionPath(rowData.getAttributePath(),
                             rowData));
                 }
                 for (RowData childRow : rowData.getChildRows()) {
                     //System.out.println("Add an operand");
                     lastOperator.addOperand(createExpression(childRow));
                 } 
             }
         }
         else if (rowData.getAttributeOperator() != null) {
 
             lastOperator.addOperand(createExpressionPath(
                 rowData.getAttributePath(), rowData));
             if ((rowData.getAttributeOperator() != Operator.IS_NULL) &&
                 (rowData.getAttributeOperator() != Operator.IS_NOT_NULL)) {
                 lastOperator.addOperand(createLiteralValueExpression(rowData));
             }
         }
 
         if (expression == null) {
             String s = "expression == null, which means code is not finished.";
             (new Exception(s)).printStackTrace();
         }
 
         return(expression);
     }
 
 
     /**
      * Create and add the operands for a PER_USER expression.
      *
      * The comments use these example rowData values:
      *
      *      nextEpoch.nextEpoch.prevEpoch.My Keywords None
      *          uuid == "xyz"
      */
 /*
     private static void createAndAddPerUserExpression(OperatorExpression parent,
         RowData rowData) {
 
         parent.addOperand(createPerUserExpression(rowData));
 
         /**
          * Now create all the other operands.
          * For example, the "uuid == xyz" operand.
          */
 /*
         for (RowData childRow : rowData.getChildRows()) {
             parent.addOperand(createExpression(childRow));
         }
     }
 */
 
     private static OperatorExpression createPerUserExpression(RowData rowData) {
 
         Attribute childmostAttribute = rowData.getChildmostAttribute();
         OperatorExpression oe;
         oe = new OperatorExpression(childmostAttribute.getQueryName());
 
         if (rowData.getAttributeCount() < 2) {
             oe.addOperand(new AttributeExpression(AE_THIS));
         }
         else {
             /**
              * Add the expression that represents the
              * "nextEpoch.nextEpoch.prevEpoch" path.
              */
             createAndAddDotPath(oe, rowData);
         }
         return(oe);
     }
 
 
     private static void createAndAddParametersMap(
         OperatorExpression lastOperator, RowData rowData) {
 
         OperatorExpression dotOperand;
         IExpression valueOperand;
 
         dotOperand = new OperatorExpression(".");
         valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                     rowData);
         lastOperator.addOperand(dotOperand);
         lastOperator.addOperand(valueOperand);
 
         dotOperand.addOperand(createExpressionPath(rowData.getAttributePath(),
                               rowData));
         dotOperand.addOperand(new AttributeExpression(AE_VALUE));
     }
 
 
     /**
      * Create and add the Expressions for the PER_USER_PARAMETERS_MAP
      * type.  As of October 2011, only the "properties" and "myproperties"
      * Attributes are of this type.
      *
      * By the time this method is called, lastOperator has been set
      * to the OperatorExpression("any").
      * (This is always the case for an Attribute of type
      * PER_USER_PARAMETERS_MAP as far as I understand.)
      *
      * An example tree might look like this:
      *
      */
     private static void createAndAddPerUserParametersMap(
         OperatorExpression lastOperator, RowData rowData) {
 
         OperatorExpression op2;
         OperatorExpression elementsOfTypeOperator;
         IExpression valueOperand;
         OperatorExpression pupmOperator;  // Per User Properties Map Operator
 
         elementsOfTypeOperator = new OperatorExpression(OE_ELEMENTS_OF_TYPE);
         lastOperator.addOperand(elementsOfTypeOperator);
 
         op2 = new OperatorExpression(
             rowData.getAttributeOperator().toString());
         lastOperator.addOperand(op2);
 
         op2.addOperand(new AttributeExpression(AE_VALUE));
         valueOperand = createLiteralValueExpression(rowData.getPropType(),
                                                     rowData);
         op2.addOperand(valueOperand);
 
         Attribute attribute = rowData.getChildmostAttribute();
         pupmOperator = new OperatorExpression(attribute.getQueryName());
         elementsOfTypeOperator.addOperand(pupmOperator);
         elementsOfTypeOperator.addOperand(createClassLiteralValueExpression(
                                           rowData.getPropType()));
 
         /**
          * Create the two operands for the pupmOperator.
          * (Per User Properties Map Operator)
          * As of October 2011, only the "properties" and "myproperties"
          * attributes were of this type.
          *
          * The first operand is the qualifying path to the attribute,
          * if any.  We use "this" if no path exists.
          * The second operand is the property key the user entered.
          */
 
         if (rowData.getAttributeCount() < 2) {
             pupmOperator.addOperand(new AttributeExpression(AE_THIS));
         }
         else {
             createAndAddDotPath(pupmOperator, rowData);
         }
 
         pupmOperator.addOperand(new StringLiteralValueExpression(
             rowData.getPropName()));
     }
 
 
     /**
      * Look at the passed in rowData object and create a
      * "dot path" of all the Attributes in the rowData's
      * attributePath except for the last Attribute.
      * I.e. the attributePath qualifies what the last Attribute is.
      * Then add that Expression to the passed in parent Expression.
      *
      * For example, if the rowData's attributePath is:
      *
      *      nextEpoch.nextEpoch.prevEpoch.My Keywords
      *
      * and the parent is:
      *
      *    OperatorExpression(mykeywords)
      *
      * this method will add the Expression tree below to parent:
      *
      *      OperatorExpression(.)
      *        OperatorExpression(.)
      *          AttributeExpression(nextEpoch)
      *          AttributeExpression(nextEpoch)
      *        AttributeExpression(prevEpoch)
      *
      * to create:
      *
      *    OperatorExpression(mykeywords)
      *      OperatorExpression(.)
      *        OperatorExpression(.)
      *          AttributeExpression(nextEpoch)
      *          AttributeExpression(nextEpoch)
      *        AttributeExpression(prevEpoch)
      */
     private static final void createAndAddDotPath(OperatorExpression parent,
         RowData rowData) {
 
         List<Attribute> attributePath = rowData.getAttributePath();
 
         /**
          * We ignore the last Attribute in the path.
          */
         attributePath = attributePath.subList(0, attributePath.size()-1);
         if (attributePath.size() < 1)
             return;
 
         parent.addOperand(createExpressionPath(attributePath, rowData));
     }
 
 
     /**
      * Create a LiteralValueExpression of the appropriate subclass
      * based on the passed in rowData.
      */
     private static ILiteralValueExpression createLiteralValueExpression(
         RowData rowData) {
 
         return(createLiteralValueExpression(rowData.getChildmostAttribute().
                                             getType(), rowData));
     }
     
 
     /**
      * Create a LiteralValueExpression of the appropriate subclass
      * based on the passed in type.  The value of the LiteralValueExpression
      * comes from the passed in rowData.
      */
     private static ILiteralValueExpression createLiteralValueExpression(
         Type type, RowData rowData) {
 
         Attribute attribute = rowData.getChildmostAttribute();
         switch (type) {
 
             case BOOLEAN:
                 boolean value;
                 value = (rowData.getAttributeOperator() == Operator.IS_TRUE);
                 return(new BooleanLiteralValueExpression(value));
 
             case UTF_8_STRING:
                 return(new StringLiteralValueExpression(
                        rowData.getAttributeValue().toString()));
 
             case INT_16:
                 /**
                  * Note that we change the INT_16 to an INT_32.
                  * As of October 15, 2011, there is no
                  * Int16LiteralValueExpression.
                  */
                 return(new Int32LiteralValueExpression(
                        ((Integer)rowData.getAttributeValue()).intValue()));
 
             case INT_32:
                 return(new Int32LiteralValueExpression(
                        ((Integer)rowData.getAttributeValue()).intValue()));
 
             case FLOAT_64:
                 return(new Float64LiteralValueExpression(
                        ((Double)rowData.getAttributeValue()).doubleValue()));
 
             case DATE_TIME:
                 return(new TimeLiteralValueExpression(
                        ((Date)rowData.getAttributeValue())));
 
             case REFERENCE:
                 return(new ClassLiteralValueExpression(
                        attribute.getQueryName()));
 
             default:
                 System.err.println("ERROR:  ExpressionTranslator."+
                     "createLiteralValueExpression().  Unhandled type.\n"+
                     "Type = "+type+"\n"+
                     "rowData:\n"+rowData.getRowString());
                 (new Exception("Unhandled type")).printStackTrace();
                 return(null);
         }
     }
 
 
     private static IClassLiteralValueExpression
         createClassLiteralValueExpression(Type type) {
 
         String name = "ovation.";
 
         switch (type) {
 
             case BOOLEAN:
                 name += "BooleanValue";
             break;
 
             case UTF_8_STRING:
                 name += "StringValue";
             break;
 
             case INT_16:
                 name += "IntegerValue";
             break;
 
             case INT_32:
                 name += "IntegerValue";
             break;
 
             case FLOAT_64:
                 name += "FloatingPointValue";
             break;
 
             case DATE_TIME:
                 name += "DateValue";
             break;
 
             default:
                 System.err.println("ERROR:  ExpressionTranslator."+
                     "createClassLiteralValueExpression().  Unhandled type.\n"+
                     "Type = "+type);
                 return(null);
         }
 
         return(new ClassLiteralValueExpression(name));
     }
 
 
     /**
      * Create an Expression for the passed in attribute, (which is
      * of type PARAMETERS_MAP).
      *
      * For example:
      *
      *      protocolParamters.key(int) == 1
      *
      * becomes:
      *
      *      OperatorExpression(.)
      *        OperatorExpression(as)
      *          OperatorExpression(parameter)
      *            AttributeExpression(protocolParameters)
      *            StringLiteralValueExpression(key)
      *          OperatorExpression(ovation.IntegerValue)
      *        AttributeExpression(value)
      *
      * This method only returns the "as" sub-tree above.
      *
      * Please note, if the passed in attributePath is more than
      * just the one "protocolParameters" entry in the example above,
      * this method handles the nesting of the parent Attributes
      * differently than for other Attribute types.  For example:
      *
      * nextEpoch.nextEpoch.prevEpoch.protocolParameters.key(float) == "12.3"
      *
      * becomes:
      * 
      * Expression:
      * CUQ: Epoch
      * rootExpression:
      * OperatorExpression(and)
      *   OperatorExpression(==)
      *     OperatorExpression(.)
      *       OperatorExpression(as)
      *         OperatorExpression(parameter)
      *           OperatorExpression(.)
      *             OperatorExpression(.)
      *               OperatorExpression(.)
      *                 AttributeExpression(nextEpoch)
      *                 AttributeExpression(nextEpoch)
      *               AttributeExpression(prevEpoch)
      *             AttributeExpression(protocolParameters)
      *           StringLiteralValueExpression(key)
      *         ClassLiteralValueExpression(ovation.FloatingPointValue)
      *       AttributeExpression(value)
      *     Float64LiteralValueExpression(12.3)
      *
      * @return The "as" sub-tree described in the example above.
      * I.e. it returns an OperatorExpression(as) that has operands
      * similar to those described above.
      */
     private static IExpression createExpressionParametersMap(
         List<Attribute> attributePath, RowData rowData) {
 
         /**
          * Create the "as" operator and the AttributeExpression(value),
          * and add them as the left and right operands of the "." operator.
          */
         OperatorExpression asOperator = new OperatorExpression(OE_AS);
 
         /**
          * Create the "parameter" operator and the
          * ClassLiteralValueExpression(IntValue), and add them as the left
          * and right operands of the "as" operator.
          */
         OperatorExpression parameterOperator = new OperatorExpression(
             OE_PARAMETER);
         asOperator.addOperand(parameterOperator);
         asOperator.addOperand(createClassLiteralValueExpression(
                               rowData.getPropType()));
 
         /**
          * Create the left (i.e. first) operand for the 
          * OperatorExpression(parameter) operator.
          */
 
         Attribute lastAttribute = attributePath.get(attributePath.size()-1);
         IExpression aeQueryName = new AttributeExpression(
             lastAttribute.getQueryName());
         IExpression parameterLeftOperand;
         if (attributePath.size() > 1) {
             List<Attribute> allButLastAttribute = attributePath.subList(0,
                 attributePath.size()-1);
 
             IExpression ex = createExpressionPath(allButLastAttribute, rowData);
             OperatorExpression oeDot = new OperatorExpression(OE_DOT);
             oeDot.addOperand(ex);
             oeDot.addOperand(aeQueryName);
             parameterLeftOperand = oeDot;
         }
         else {
             parameterLeftOperand = aeQueryName;
         }
 
         /**
          * Now add the left operand we created above.
          */
         parameterOperator.addOperand(parameterLeftOperand);
 
         /**
          * Create and add the right (i.e. second) operand for the
          * OperatorExpression(parameter) operator.  This is the
          * "key" name that the user entered.
          */
         parameterOperator.addOperand(new StringLiteralValueExpression(
                                      rowData.getPropName()));
 
         return(asOperator);
     }
 
 
     /**
      * Create an expression based on the passed in attributePath and
      * RowData object.  This method calls itself recursively to build
      * up the Expression that represents the passed in attributePath.
      * We sometimes use the passed in rowData parameter to know the
      * property type and property name of the last attribute, if the
      * last attribute is of a type where those values are relevant.
      * For example, a PARAMETERS_MAP attribute type.
      *
      * Please note, although the passed in attributePath is "from"
      * the passed in RowData object, it is possibly a sub-list of
      * the RowData's full attributePath.  (This method calls itself
      * recursively, chopping off the last attribute from the
      * attributePath before calling itself.)  So, don't start
      * calling RowData.getAttributePath() unless you really are
      * sure that is what you want.
      *
      * Note, the syntax of the Expression tree is NOT consistent
      * for all data types.  For "generic" attributes, we create
      * a tree of nested OpertorExpressions.  For example:
      *
      * rootRow:
      * Epoch | Any
      *   Epoch | nextEpoch.nextEpoch.prevEpoch.protocolID == "Test 27"
      * 
      * Expression:
      * CUQ: Epoch
      * rootExpression:
      * OperatorExpression(or)
      *   OperatorExpression(==)
      *     OperatorExpression(.)
      *       OperatorExpression(.)
      *         OperatorExpression(.)
      *           AttributeExpression(nextEpoch)
      *           AttributeExpression(nextEpoch)
      *         AttributeExpression(prevEpoch)
      *       AttributeExpression(protocolID)
      *     StringLiteralValueExpression(Test 27)
      *
      * But for a PARAMETERS_MAP type, things are different.
      * Please see the createExpressionParametersMap() method for more info.
      *
      * @return The IExpression returned is, in the example above,
      * the first operand of the "==" operator subtree shown above.
      * I.e. the subtree that starts with the first OperatorExpression(.)
      */
     private static IExpression createExpressionPath(
         List<Attribute> attributePath, RowData rowData) {
 
         //System.out.println("Enter createExpressionPath(List<Attribute>)");
         //System.out.println("attributePath.size() = "+attributePath.size());
 
         if (attributePath.size() < 1) {
 
             /**
              * This should never happen.
              */
             String s = "attributePath.size() < 1";
             (new Exception(s)).printStackTrace();
             return(null);
         }
 
         /**
          * Throw away special Attributes suchs a "is null" and "is not null".
          */
         Attribute lastAttribute = attributePath.get(attributePath.size()-1);
         if ((attributePath.size() > 1) &&
             ((lastAttribute == Attribute.IS_NULL) ||
              (lastAttribute == Attribute.IS_NOT_NULL))) {
             attributePath = attributePath.subList(0, attributePath.size()-1);
             lastAttribute = attributePath.get(attributePath.size()-1);
         }
 
         /**
          * Handle the special cases:  PARAMETERS_MAP, PER_USER
          *
          * They don't handle "nesting" in the normal way because
          * they cannot be an operand of the dot operator.
          */
         if (lastAttribute.getType() == Type.PARAMETERS_MAP) {
             return(createExpressionParametersMap(attributePath, rowData));
         }
         else if (lastAttribute.getType() == Type.PER_USER) {
             return(createPerUserExpression(rowData));
         }
 
         /**
          * If the attributePath is, (or has been whittled down to),
          * one attribute long, create the Expression for that one
          * attribute.
          */
         if (attributePath.size() == 1) {
 
             Attribute attribute = attributePath.get(0);
 
             /**
              * Quick sanity check during development.
              */
             if (attribute.getType() == Type.PARAMETERS_MAP) {
                 /**
                  * We should never get here.
                  * This type should already have been handled
                  * above.
                  */
                 String s = "Got an Attribute of Type.PARAMETERS_MAP "+
                     "unexpectedly.  There is a problem in the code.";
                 (new Exception(s)).printStackTrace();
                 return(null);
             }
 
             if (attribute.getType() == Type.PER_USER) {
                 /**
                  * TODO:  I don't think we get here any more.
                  */
                 return(new OperatorExpression(
                        attribute.getQueryName()));
             }
             else {
                 return(new AttributeExpression(
                        attribute.getQueryName()));
             }
         }
 
         /**
          * If we get here, the attributePath is longer than one
          * attribute.  So, we will use the "." operator to concatenate
          * attributes on the attributePath.
          */
         OperatorExpression expression = new OperatorExpression(".");
 
         /**
          * Create and add the left operand.  Note, the left operand
          * is the sub-list of all attributes to the left of the the
          * rightmost attribute.  For example, if the attribute
          * path is:
          *
          *      epochGroup.source.label
          *
          * the left operand is made of "epochGroup.source" and the
          * right operand is "label".
          */
 
         IExpression operand;
         List<Attribute> subList;
         subList = attributePath.subList(0, attributePath.size()-1);
         operand = createExpressionPath(subList, rowData);
 
         //System.out.println("Add left operand.");
         expression.addOperand(operand);
 
         /**
          * Create and add the right operand.  The right operand
          * is the rightmost attribute in the passed in attributePath.
          */
 
         subList = attributePath.subList(attributePath.size()-1,
                                         attributePath.size());
         operand = createExpressionPath(subList, rowData);
 
         //System.out.println("Add right operand: ");
         expression.addOperand(operand);
 
         return(expression);
     }
 
 
     /**
      * A long list of tests.
      */
     public static void runAllTests() {
 
         RowData rootRow;
         RowData rowData;
         RowData rowData2;
         RowData rowData3;
         RowData rowData4;
         ExpressionTree expression;
 
         ClassDescription epochCD = DataModel.getClassDescription("Epoch");
         ClassDescription epochGroupCD = DataModel.getClassDescription(
             "EpochGroup");
         ClassDescription sourceCD = DataModel.getClassDescription("Source");
         ClassDescription responseCD = DataModel.getClassDescription("Response");
         ClassDescription resourceCD = DataModel.getClassDescription("Resource");
         ClassDescription derivedResponseCD =
             DataModel.getClassDescription("DerivedResponse");
         ClassDescription externalDeviceCD = 
             DataModel.getClassDescription("ExternalDevice");
 
         /**
          * Test the Any collection operator, (which becomes "or"),
          * and the String type and a couple attribute operators.
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("protocolID"));
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue("abc");
         rootRow.addChildRow(rowData);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("protocolID"));
         rowData.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData.setAttributeValue("xyz");
         rootRow.addChildRow(rowData);
         
         printResults("CollectionOperator.ANY With Two Operands", rootRow);
 
         /**
          * Test the All collection operator, (which becomes "and"),
          * and the Boolean type.
          *
          *      incomplete is true
          *
          * Note, the GUI does not accept:
          *
          *      incomplete == true
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("incomplete"));
         /**
          * This is the wrong way to handle booleans in the GUI.
          */
         //rowData.setAttributeOperator(Operator.EQUALS);
         //rowData.setAttributeValue(new Boolean(true));
         /**
          * This is the correct way.
          */
         rowData.setAttributeOperator(Operator.IS_TRUE);
 
         rootRow.addChildRow(rowData);
 
         printResults("Attribute Boolean Operator.IS_TRUE", rootRow);
 
         /**
          * Test an attribute path with two levels.
          * Also test using None collection operator which
          * gets turned into two operators:  "not" with
          * "or" as its only operand.
          *
          *      Epoch None
          *          epochGroup.label == "Test 27"
          *
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("epochGroup"));
         rowData.addAttribute(epochGroupCD.getAttribute("label"));
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue("Test 27");
         rootRow.addChildRow(rowData);
 
         printResults("Attribute Path Nested Twice", rootRow);
 
         /**
          * Test an attribute path with three levels:
          *
          *      epochGroup.source.label == "Test 27"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("epochGroup"));
         rowData.addAttribute(epochGroupCD.getAttribute("source"));
         rowData.addAttribute(sourceCD.getAttribute("label"));
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue("Test 27");
         rootRow.addChildRow(rowData);
 
         printResults("Attribute Path Nested Thrice", rootRow);
 
         /**
          * Test a reference value for null.
          *
          *      Epoch | All
          *        Epoch | owner is null
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("owner"));
         rowData.addAttribute(Attribute.IS_NULL);  // optional call
         rowData.setAttributeOperator(Operator.IS_NULL);
         rootRow.addChildRow(rowData);
 
         printResults("Reference Value Operator.IS_NULL", rootRow);
 
         /**
          * Test a reference value for not null.
          *
          *      Epoch | All
          *        Epoch | owner is not null
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("owner"));
         rowData.addAttribute(Attribute.IS_NOT_NULL);  // optional call
         rowData.setAttributeOperator(Operator.IS_NOT_NULL);
         rootRow.addChildRow(rowData);
 
         printResults("Reference Value Operator.IS_NOT_NULL", rootRow);
 
         /**
          * Test a compound row:
          *
          *      Epoch | All
          *        Epoch | responses None
          *          Response | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         printResults("Compound Row", rootRow);
 
         /**
          * Test a compound row:
          *
          *      Epoch | All
          *        Epoch | responses All
          *          Response | resources Any
          *            Epoch | protocolID != "Test 27"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("resources"));
         rowData2.setCollectionOperator(CollectionOperator.ANY);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.addAttribute(resourceCD.getAttribute("uuid"));
         rowData3.setAttributeOperator(Operator.NOT_EQUALS);
         rowData3.setAttributeValue("ID 27");
         rowData2.addChildRow(rowData3);
 
         printResults("Compound Row Nested Classes", rootRow);
 
         /**
          * Test a compound row that uses the Count collection operator:
          *
          *      Epoch | All
          *        Epoch | responses All
          *          Response | resources Count <= 5
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("resources"));
         rowData2.setCollectionOperator(CollectionOperator.COUNT);
         rowData2.setAttributeOperator(Operator.LESS_THAN_EQUALS);
         rowData2.setAttributeValue(new Integer(5));
         rowData.addChildRow(rowData2);
 
         printResults("Compound Row ALL COUNT", rootRow);
 
         /**
          * Test a compound row:
          *
          *      Epoch | Any
          *        Epoch | responses Any
          *          Response | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         printResults("Compound Row ANY ANY", rootRow);
 
         /**
          * Test a compound row:
          *
          *      Epoch | None
          *        Epoch | responses None
          *          Response | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("responses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(responseCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         printResults("Compound Row NONE NONE", rootRow);
 
         /**
          * Test a PER_USER attribute type.
          *
          *      Epoch | All
          *        Epoch | My keywords None
          *          KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("mykeywords"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         printResults("PER_USER", rootRow);
 
         /**
          * Test a PER_USER attribute type with Count.
          *
          *      Epoch | All
          *        Epoch | My keywords Count == 5
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("mykeywords"));
         rowData.setCollectionOperator(CollectionOperator.COUNT);
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue(new Integer(5));
         rootRow.addChildRow(rowData);
 
         printResults("PER_USER CollectionOperator.COUNT", rootRow);
 
         /**
          * Test a nested PER_USER attribute type.
          *
          *      Epoch | All
          *        Epoch | nextEpoch.All keywords All
          *          KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("keywords"));
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         printResults("PER_USER Nested Once CollectionOperator.ANY", rootRow);
 
         /**
          * Test a nested PER_USER attribute type.
          *
          *      Epoch | All
          *        Epoch | nextEpoch.All keywords All
          *          KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("keywords"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         printResults("PER_USER Nested Twice CollectionOperator.NONE", rootRow);
 
         /**
          * Test a nested PER_USER attribute type.
          *
          *      Epoch | All
          *        Epoch | nextEpoch.nextEpoch.prevEpoch.All keywords All
          *          KeywordTag | uuid == "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
         rowData.addAttribute(epochCD.getAttribute("keywords"));
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(epochCD.getAttribute("uuid"));
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue("xyz");
         rowData.addChildRow(rowData2);
 
         printResults("PER_USER Nested Thrice CollectionOperator.ALL", rootRow);
 
         /**
          * Test a PARAMETERS_MAP row of type time.
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
         rowData.setPropName("someTimeKey");
         rowData.setPropType(Type.DATE_TIME);
         rowData.setAttributeOperator(Operator.EQUALS);
        rowData.setAttributeValue(new Date());
         rootRow.addChildRow(rowData);
 
         printResults("PARAMETERS_MAP Type Date", rootRow);
 
         /**
          * Test a PARAMETERS_MAP row of type float, that
          * has an attributePath that is more than one level deep.
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
         rowData.addAttribute(epochCD.getAttribute("protocolParameters"));
         rowData.setPropName("someKey");
         rowData.setPropType(Type.FLOAT_64);
         rowData.setAttributeOperator(Operator.EQUALS);
         rowData.setAttributeValue(new Double(12.3));
         rootRow.addChildRow(rowData);
 
         printResults("PARAMETERS_MAP Nested", rootRow);
 
         /**
          * Test a PER_USER_PARAMETERS_MAP row.
          *
          *      Any properties.someKey(int) != "34"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("properties"));
         rowData.setPropName("someKey");
         rowData.setPropType(Type.INT_32);
         rowData.setAttributeOperator(Operator.NOT_EQUALS);
         rowData.setAttributeValue(new Integer(34));
         rootRow.addChildRow(rowData);
 
         printResults("PER_USER_PARAMETERS_MAP", rootRow);
 
         /**
          * Test a PER_USER_PARAMETERS_MAP row.
          *
          * nextEpoch.nextEpoch.prevEpoch.Any properties.someKey(int) != "34"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("prevEpoch"));
         rowData.addAttribute(epochCD.getAttribute("properties"));
         rowData.setPropName("someKey");
         rowData.setPropType(Type.INT_32);
         rowData.setAttributeOperator(Operator.NOT_EQUALS);
         rowData.setAttributeValue(new Integer(34));
         rootRow.addChildRow(rowData);
 
         printResults("PER_USER_PARAMETERS_MAP Nested", rootRow);
 
         /**
          * Test handling a reference.
          *
          *  nextEpoch.nextEpoch is null
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.setAttributeOperator(Operator.IS_NULL);
         rootRow.addChildRow(rowData);
 
         printResults("Reference isnull()", rootRow);
 
         /**
          * Test compound row.
          *
          *      All of the following
          *        None of the following
          *          protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData.addChildRow(rowData4);
 
         printResults("Compound Operators Nested", rootRow);
 
         /**
          * Test compound row.
          *
          *      Any of the following
          *        All of the following
          *          None of the following
          *            protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ALL);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.NONE);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         printResults("Compound Operators In Different Positions", rootRow);
 
         /**
          * Test compound row.
          *
          *      All of the following
          *        All of the following
          *          Any of the following
          *            None of the following
          *              protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.ALL);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ANY);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.NONE);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         printResults("Compound Operators In Different Positions", rootRow);
 
         /**
          * Test compound row.
          *
          *      Any of the following
          *        Any of the following
          *          All of the following
          *            None of the following
          *              protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.ANY);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ALL);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.NONE);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         printResults("Compound Operators In Different Positions", rootRow);
 
         /**
          * Test compound row.
          *
          *      None of the following
          *        None of the following
          *          All of the following
          *            Any of the following
          *              protocolID =~~ "xyz"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.NONE);
 
         rowData = new RowData();
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.setCollectionOperator(CollectionOperator.ALL);
         rowData.addChildRow(rowData2);
 
         rowData3 = new RowData();
         rowData3.setCollectionOperator(CollectionOperator.ANY);
         rowData2.addChildRow(rowData3);
 
         rowData4 = new RowData();
         rowData4.addAttribute(epochCD.getAttribute("protocolID"));
         rowData4.setAttributeOperator(Operator.MATCHES_CASE_INSENSITIVE);
         rowData4.setAttributeValue("xyz");
         rowData3.addChildRow(rowData4);
 
         printResults("Compound Operators In Different Positions", rootRow);
 
         /**
          * Test compound row with PARAMETERS_MAP and PER_USER child.
          *
          *  Modified rootRow:
          *  Epoch | Any
          *    Epoch | nextEpoch.My DerivedResponses None
          *      DerivedResponse | derivationParameters.somekey(boolean) is true
          *      DerivedResponse | My Keywords Count == "5"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute(
                               "derivationParameters"));
         rowData2.setPropName("someKey");
         rowData2.setPropType(Type.BOOLEAN);
         rowData2.setAttributeOperator(Operator.IS_TRUE);
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
         rowData2.setCollectionOperator(CollectionOperator.COUNT);
         rowData2.setAttributeValue(new Integer(5));
         rowData.addChildRow(rowData2);
 
         /**
          * Test compound row with PARAMETERS_MAP, PER_USER children.
          *
          *  Modified rootRow:
          *  Epoch | Any
          *    Epoch | nextEpoch.My DerivedResponses None
          *      DerivedResponse | derivationParameters.somekey(boolean) is true
          *      DerivedResponse | My Keywords Count == "5"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute(
                               "derivationParameters"));
         rowData2.setPropName("someKey");
         rowData2.setPropType(Type.BOOLEAN);
         rowData2.setAttributeOperator(Operator.IS_TRUE);
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
         rowData2.setCollectionOperator(CollectionOperator.COUNT);
         rowData2.setAttributeValue(new Integer(5));
         rowData.addChildRow(rowData2);
 
         printResults("Nested PER_USER With PARAMETERS_MAP & PER_USER Children",
                      rootRow);
 
         /**
          * Test compound row with PARAMETERS_MAP, PER_USER, and
          * PER_USER_PARAMETERS_MAP child.
          *
          *  Epoch | Any
          *    Epoch | nextEpoch.My DerivedResponses None
          *      DerivedResponse | derivationParameters.someKey(boolean) is true
          *      DerivedResponse | My Keywords Count == "5"
          *      DerivedResponse | externalDevice.My Property.someKey2(float) ==
          *                                                               "34.5"
          */
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("myderivedResponses"));
         rowData.setCollectionOperator(CollectionOperator.NONE);
         rootRow.addChildRow(rowData);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute(
                               "derivationParameters"));
         rowData2.setPropName("someKey");
         rowData2.setPropType(Type.BOOLEAN);
         rowData2.setAttributeOperator(Operator.IS_TRUE);
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute("mykeywords"));
         rowData2.setCollectionOperator(CollectionOperator.COUNT);
         rowData2.setAttributeValue(new Integer(5));
         rowData.addChildRow(rowData2);
 
         rowData2 = new RowData();
         rowData2.addAttribute(derivedResponseCD.getAttribute("externalDevice"));
         rowData2.addAttribute(externalDeviceCD.getAttribute("myproperties"));
         rowData2.setPropName("someKey2");
         rowData2.setPropType(Type.FLOAT_64);
         rowData2.setAttributeOperator(Operator.EQUALS);
         rowData2.setAttributeValue(new Double(34.5));
         rowData.addChildRow(rowData2);
 
         printResults("Nested PER_USER With PM, PU, and PUPM Children",
                      rootRow);
     }
 
 
     /**
      * This is just a single test of what I am working on right now.
      */
     public static void runOneTest() {
 
         RowData rowData;
         RowData rootRow;
         ExpressionTree expression;
 
         ClassDescription epochCD = DataModel.getClassDescription("Epoch");
         ClassDescription epochGroupCD = DataModel.getClassDescription(
             "EpochGroup");
         ClassDescription sourceCD = DataModel.getClassDescription("Source");
         ClassDescription responseCD = DataModel.getClassDescription("Response");
 
         rootRow = new RowData();
         rootRow.setClassUnderQualification(epochCD);
         rootRow.setCollectionOperator(CollectionOperator.ALL);
 
         rowData = new RowData();
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("nextEpoch"));
         rowData.addAttribute(epochCD.getAttribute("owner"));
         rowData.addAttribute(Attribute.IS_NULL);
         rowData.setAttributeOperator(Operator.IS_NULL);
         rootRow.addChildRow(rowData);
 
         System.out.println("\nRowData:\n"+rootRow);
         expression = ExpressionTranslator.createExpressionTree(rootRow);
         System.out.println("\nExpression:\n"+expression);
         rootRow = ExpressionTranslator.createRowData(expression);
         System.out.println("\nExpression Translated To RowData:\n"+rootRow);
     }
 
 
     private static void printResults(String label, RowData rootRow) {
 
         System.out.println("\n===== "+label+" =====");
 
         /*
         ExpressionBuilder.ReturnValue returnValue;
         returnValue = ExpressionBuilder.editExpression(rootRow);
         if (returnValue.status != ExpressionBuilder.RETURN_STATUS_OK)
             System.exit(returnValue.status);
         */
 
         System.out.println("\nOriginal RowData:\n"+rootRow);
 
         ExpressionTree expression = ExpressionTranslator.createExpressionTree(
             rootRow);
         System.out.println("\nTranslated To Expression:\n"+expression);
 
         //rootRow = ExpressionTranslator.createRowData(expression);
         //System.out.println("\nExpression Translated To RowData:\n"+rootRow);
     }
 
 
     /**
      * This is a simple test program for this class.
      */
     public static void main(String[] args) {
 
         System.out.println("ExpressionTranslator test is starting...");
 
         runAllTests();
         //runOneTest();
 
         System.out.println("\nExpressionTranslator test is ending.");
     }
 }
