/* $Id: PhysicalUnnest.java,v 1.4 2002/10/31 05:51:45 vpapad Exp $ */
 package niagara.query_engine;
 
 import org.w3c.dom.*;
 import java.util.ArrayList;
 import niagara.utils.*;
 import niagara.xmlql_parser.op_tree.*;
 import niagara.xmlql_parser.syntax_tree.*;
 
 import niagara.logical.Unnest;
 import niagara.optimizer.colombia.*;
 
 public class PhysicalUnnest extends PhysicalOperator {
     // No blocking inputs
     private static final boolean[] blockingSourceStreams = { false };
 
     // Optimization-time attributes
     /**  The path expression to scan */
     private regExp path;
     /** The attribute on which the scan is to be performed */
     private Attribute root;
     /** The resulting variable */
     private Attribute variable;
 
     // Runtime attributes
     private PathExprEvaluator pev;
     private NodeVector elementList;
     private int scanField;
 
     public PhysicalUnnest() {
         setBlockingSourceStreams(blockingSourceStreams);
     }
 
     /**
      * Initializes from the appropriate logical operator
      *
      * @param logicalOperator The logical operator that this operator implements
      */
     public void initFrom(LogicalOp logicalOperator) {
         Unnest logicalUnnest = (Unnest) logicalOperator;
         this.path = logicalUnnest.getPath();
         this.root = logicalUnnest.getRoot();
         this.variable = logicalUnnest.getVariable();
     }
 
     /**
      * This function processes a tuple element read from a source stream
      * when the operator is non-blocking. This over-rides the corresponding
      * function in the base class.
      *
      * @param inputTuple The tuple element read from a source stream
      * @param streamId The source stream from which the tuple was read
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
 
     protected void nonblockingProcessSourceTupleElement(
         StreamTupleElement inputTuple,
         int streamId)
         throws ShutdownException, InterruptedException {
 
         // Get the attribute to scan on
         Node attribute = inputTuple.getAttribute(scanField);
 
         // Get the nodes reachable using the path expression scanned
         pev.getMatches(attribute, elementList);
 
         // Append all the nodes returned to the inputTuple and add these
         // to the result
         int numNodes = elementList.size();
 
         for (int node = 0; node < numNodes; ++node) {
             // Clone the input tuple to create an output tuple
             // Append a reachable node to the output tuple
             // and put the tuple in the output stream
             StreamTupleElement outputTuple =
                 (StreamTupleElement) inputTuple.clone();
             outputTuple.appendAttribute(elementList.get(node));
             putTuple(outputTuple, 0);
         }
         elementList.clear();
     }
 
     public boolean isStateful() {
         return false;
     }
 
     // get the physical prop according to the order of the collection
     public PhysicalProperty findPhysProp(PhysicalProperty[] input_phys_props) {
         return input_phys_props[0].copy();
     }
 
     public boolean equals(Object o) {
         if (o == null || !(o instanceof PhysicalUnnest))
             return false;
         if (o.getClass() != getClass())
             return o.equals(this);
 
         PhysicalUnnest op = (PhysicalUnnest) o;
         return (path.equals(op.path) && variable.equals(op.variable));
     }
 
     public int hashCode() {
         // XXX vpapad: regExp.hashCode is Object.hashCode
         return path.hashCode() ^ variable.hashCode();
     }
 
     /**
      * @see niagara.optimizer.colombia.PhysicalOp#FindLocalCost(ICatalog, LogicalProperty, LogicalProperty[])
      */
     public Cost findLocalCost(
         ICatalog catalog,
         LogicalProperty[] InputLogProp) {
         double inputCard = InputLogProp[0].getCardinality();
         double outputCard = logProp.getCardinality();
         return new Cost(
             inputCard * catalog.getDouble("tuple_reading_cost")
                 + outputCard * catalog.getDouble("dom_unnesting_cost")
                 + (outputCard - inputCard)
                     * catalog.getDouble("tuple_construction_cost"));
     }
 
     /**
      * @see niagara.optimizer.colombia.Op#copy()
      */
     public Op copy() {
         PhysicalUnnest op = new PhysicalUnnest();
         op.path = path;
         op.root = root;
         op.variable = variable;
         return op;
     }
 
     /**
      * @see niagara.query_engine.PhysicalOperator#opInitialize()
      */
     protected void opInitialize() {
         scanField = inputTupleSchemas[0].getPosition(root.getName());
         pev = new PathExprEvaluator(path);
         elementList = new NodeVector();
     }
 
     /**
      * This function processes a punctuation element read from a source stream
      * when the operator is non-blocking. This over-rides the corresponding
      * function in the base class.
      *
      * Punctuations can simply be sent to the next operator from Scan
      *
      * @param inputTuple The tuple element read from a source stream
      * @param streamId The source stream from which the tuple was read
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
     protected void processPunctuation(
         StreamPunctuationElement inputTuple,
         int streamId)
         throws ShutdownException, InterruptedException {
 
         try {
             // Get the attribute to scan on
             Node attribute = inputTuple.getAttribute(scanField);
 
             // Get the nodes reachable using the path expression scanned
             pev.getMatches(attribute, elementList);
 
             // Append all the nodes returned to the inputTuple and add these
             // to the result
             int numNodes = elementList.size();
 
             if (numNodes != 0) {
                 for (int node = 0; node < numNodes; ++node) {
                     // Clone the input tuple to create an output tuple
                     // Append a reachable node to the output tuple
                     // and put the tuple in the output stream
                     StreamPunctuationElement outputTuple =
                         (StreamPunctuationElement) inputTuple.clone();
                     outputTuple.appendAttribute(elementList.get(node));
                     putTuple(outputTuple, 0);
                 }
             } else {
                 //I still want the punctuation to live on, even if it doesn't
                 // have the element we're scanning for.
                 putTuple(inputTuple, streamId);
             }
             elementList.clear();
         } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
             //the scan field doesn't exist for this punctuation. We
             // still want the tuple to live on.
             putTuple(inputTuple, streamId);
         }
     }
 }
