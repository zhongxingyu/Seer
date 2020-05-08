 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.core.database.nodes;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.exception.LoopInCellReferencesException;
 import org.amanzi.neo.core.enums.CellRelationTypes;
 import org.amanzi.neo.core.enums.SplashRelationshipTypes;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * Wrapper of Spreadsheet Cell Node
  * 
  * @author Lagutko_N
  */
 
 public class CellNode extends AbstractNode {
     
     
     /*
      * Cell Definition property.
      */
     private static final String CELL_DEFINITION = "cell_definition";
 
     /*
      * Name of Value property
      */    
     private static final String CELL_VALUE = "value";
     
     /*
      * Name of Date Value property
      */
     private static final String CELL_DATE_VALUE = "date_value";
     
     /*
      * Name of Script URI property
      */
     
     private static final String SCRIPT_URI = "script_uri";
     
     /*
      * Type of this Node
      */
     private static final String CELL_NODE_TYPE = "spreadsheet_cell";
     
     /*
      * Name of Cyclic Dependencies property
      */
     private static final String CELL_CYCLIC = "Cell cyclic dependencies";
     
     /*
      * Index of Cell's Column
      */
     public static final String CELL_COLUMN = "column_index";
     
     /*
      * Index of Cell's Row
      */
     public static final String CELL_ROW = "row_index";
     
     /*
      * Name of 'Spreadsheet ID' property 
      */
     public static final String SPREADSHEET_ID = "spreadsheet_id";
     
     /**
      * Constructor. Wraps a Node from database and sets type and name of Node
      * 
      * @param node database node
      */
     public CellNode(Node node) {
         super(node);        
         setParameter(INeoConstants.PROPERTY_TYPE_NAME, CELL_NODE_TYPE);
     }
     
     public void addSplashFormat(SplashFormatNode sfNode){
     	addRelationship(SplashRelationshipTypes.SPLASH_FORMAT, sfNode.getUnderlyingNode());
     }
     
     public SplashFormatNode getSplashFormat(){
         //Lagutko, 27.10.2009, Cell can have no splash format
         Iterator<Relationship> relationships = node.getRelationships(SplashRelationshipTypes.SPLASH_FORMAT, Direction.INCOMING).iterator();
         if (relationships.hasNext()) {
             return SplashFormatNode.fromNode(relationships.next().getStartNode());            
         }
         else {
             return null;
         }
     }
     
     /**
      * Sets the value of Cell
      *
      * @param value value of Cell
      */
     
     public void setValue(Object value) {
         if (value instanceof Date) {
             setDateValue((Date)value);
         }
         else {
             setObjectValue(value);
         }
     }
     
     /**
      * Sets a Date value of Cell
      *
      * @param value Date Value of Cell
      */
     private void setDateValue(Date value) {
         if (node.hasProperty(CELL_VALUE)) {
             node.removeProperty(CELL_VALUE);
         }
         setParameter(CELL_DATE_VALUE, value.getTime());
     }
     
     /**
      * Sets a Object value of Cell
      *
      * @param value Object Value of Cell
      */
     private void setObjectValue(Object value) {
         if (node.hasProperty(CELL_DATE_VALUE)) {
             node.removeProperty(CELL_DATE_VALUE);
         }
         setParameter(CELL_VALUE, value);
     }
      
     /**
      * Returns the value of Cell
      *
      * @return value of Cell
      */
     
     public Object getValue() {
         Date date = getDateValue();
         if (date == null) {
             return (Object)getParameter(CELL_VALUE);
         }
         else {
             return date;
         }
     }
     
     /**
      * Returns a Date value of Cell (if it was set)
      *
      * @return Date value of Cell
      */
     private Date getDateValue() {
         Object dateValue = getParameter(CELL_DATE_VALUE);
         if (dateValue != null) {
             return new Date((Long)dateValue);
         }
         return null;
     }
     
     /**
      * Returns Definition of Cell
      *
      * @return cell's definition
      */
     public String getDefinition() {
         return (String)getParameter(CELL_DEFINITION);
     }
     
     /**
      * Sets Definition for Cell
      *
      * @param definition cell's definition
      */
     public void setDefinition(String definition) {
         setParameter(CELL_DEFINITION, definition);
     }
     
   
     /**
      * Sets Script URI of Cell
      *
      * @param scriptURI
      */    
     public void setScriptURI(URI scriptURI) {
         if (scriptURI != null) {
             
             setParameter(SCRIPT_URI, scriptURI.toString());
         }
     }
     
     /**
      * Sets is this cell has cyclic dependencies
      *
      * @param isCyclic
      */
     public void setCyclic(boolean isCyclic) {
         setParameter(CELL_CYCLIC, isCyclic);
     }
     
     /**
      * Is this Cell has Cyclic dependencies?
      *
      * @return
      */
     public boolean isCyclic() {
         if (!(Boolean)node.hasProperty(CELL_CYCLIC)) {
             return false;
         }
         return (Boolean)getParameter(CELL_CYCLIC);
     }
     
     /**
      * Returns Script URI for Cell
      *
      * @return cell's script URI
      */
     public URI getScriptURI() {
         String uri = (String)getParameter(SCRIPT_URI);
         if (uri != null) {
             try {
                 return new URI(uri);
             }
             catch (URISyntaxException e) {
                 NeoCorePlugin.error(null, e);
                 return null;
             }
         }
         return null;
     }
     
     /**
      * Adds a RFD Cell
      *
      * @param rfdNode wrapped RFD Cell
      */    
     public void addDependedNode(CellNode rfdNode) throws LoopInCellReferencesException {
         Relationship relationship = null;
         
         try {
             relationship = addRelationship(CellRelationTypes.REFERENCED, rfdNode.getUnderlyingNode());
             checkLoops(rfdNode);
         }
         catch (IllegalArgumentException e) {
             //Lagutko: the IllegalArgumentException will be thrown if we try to create Relationship to same Node
             throw new LoopInCellReferencesException(new CellID(rfdNode.getCellRow(), rfdNode.getCellColumn()));
         }
         catch (LoopInCellReferencesException e) {
             //Lagutko: LoopInCellReferencesException will be thrown 
             relationship.delete();
             throw e;
         }
     }
     
     /**
      * Checks a Loops in Cell's References
      *
      * @param newDependedNode
      * @throws LoopInCellReferencesException
      */
     private void checkLoops(CellNode newDependedNode) throws LoopInCellReferencesException {
         Iterator<CellNode> lastNode = new LoopDependencyIterator(newDependedNode);
         
         if (lastNode.hasNext()) {
             throw new LoopInCellReferencesException(new CellID(newDependedNode.getCellRow(), newDependedNode.getCellColumn()));
         }
     }
     
     /**
      * Delete a list of Cells from dependent
      *
      * @param rfdNodes list of Cells
      */
     public void deleteReferenceFromNode(List<CellNode> rfdNodes) {
         ArrayList<Node> underlyingNodes = new ArrayList<Node>(rfdNodes.size());
         for (CellNode node : rfdNodes) {
             underlyingNodes.add(node.getUnderlyingNode());
         }
         
         Iterator<Relationship> relationships = node.getRelationships(CellRelationTypes.REFERENCED, Direction.OUTGOING).iterator();
         
         while (relationships.hasNext()) {
             Relationship relationship = relationships.next();
             
             if (underlyingNodes.contains(relationship.getEndNode())) {
                 relationship.delete();
             }
         }
     }
     
     /**
      * Returns Iterator for Cells that depended on this Cell
      *
      * @return iterator of depended Cells
      */
     public Iterator<CellNode> getDependedNodes() {        
         return new DependedNodesIterator();
     }
     
     /**
      * Returns Iterator for Cells that references on this Cell
      *
      * @return iterator of referenced Cells
      */
     public Iterator<CellNode> getReferencedNodes() {        
         return new ReferencedNodesIterator();
     }
     
     /**
      * Iterator for RFD cells;
      * 
      * @author Lagutko_N
      */
     private class DependedNodesIterator extends AbstractIterator<CellNode> {
         
         public DependedNodesIterator() {
             this.iterator = node.traverse(Traverser.Order.BREADTH_FIRST,
                                           StopEvaluator.DEPTH_ONE,
                                           ReturnableEvaluator.ALL_BUT_START_NODE,
                                           CellRelationTypes.REFERENCED,
                                           Direction.INCOMING).iterator();
         }
 
         @Override
         protected CellNode wrapNode(Node node) {
             return new CellNode(node);
         }
     }
     
     /**
      * Iterator for RFD cells;
      * 
      * @author Lagutko_N
      */
     private class ReferencedNodesIterator extends AbstractIterator<CellNode> {
         
         public ReferencedNodesIterator() {
             this.iterator = node.traverse(Traverser.Order.BREADTH_FIRST,
                                           StopEvaluator.DEPTH_ONE,
                                           ReturnableEvaluator.ALL_BUT_START_NODE,
                                           CellRelationTypes.REFERENCED,
                                           Direction.OUTGOING).iterator();
         }
 
         @Override
         protected CellNode wrapNode(Node node) {
             return new CellNode(node);
         }
     }
     
     /**
      * Special Traverser for detecting Loops in Cell References
      * 
      * @author Lagutko_N
      */
     private class LoopDependencyIterator extends AbstractIterator<CellNode> {
         
         
         public LoopDependencyIterator(CellNode endNode) {
             final Node end = endNode.getUnderlyingNode();
             
             this.iterator = node.traverse(Traverser.Order.DEPTH_FIRST, 
                                           StopEvaluator.END_OF_GRAPH,
                                           new ReturnableEvaluator(){
                                               
                                             
                                               public boolean isReturnableNode(TraversalPosition arg0) {
                                                   if (arg0.depth() > 0) {
                                                       return arg0.currentNode().equals(end);
                                                   }
                                                   else {
                                                       return false;
                                                   }
                                               }
                                           },
                                           CellRelationTypes.REFERENCED,
                                           Direction.INCOMING).iterator();
         }
 
         @Override
         protected CellNode wrapNode(Node node) {
             return new CellNode(node);
         }
         
     }
     /**
      * Use factory method to ensure clear API different to normal constructor.
      *
      * @param node representing an existing cell
      * @return CellNode from existing Node
      */
     public static CellNode fromNode(Node node) {
         return new CellNode(node);
     }
 
     /**
      * Sets index of Cell's column
      * 
      * @param columnIndex index of column
      */
     public void setCellColumn(Integer columnIndex) {
         setParameter(CELL_COLUMN, columnIndex);
     }
     
     /**
      * Sets index of Cell's row
      * 
      * @param rowIndex index of row
      */
     public void setCellRow(Integer rowIndex) {
         setParameter(CELL_ROW, rowIndex);
     }
     
     /**
      * Returns index of Cell's Column
      *
      * @return index of Cell's Column
      */
     public Integer getCellColumn() {
         return (Integer)getParameter(CELL_COLUMN) - 1;
     }
     
     /**
      * Returns index of Cell's Row
      *
      * @return index of Cell's Row
      */
     public Integer getCellRow() {
         return (Integer)getParameter(CELL_ROW) - 1;
     }
     
     /**
      * Returns all Cells from this to Cell in Row
      *
      * @param maxRowIndex index of last Cell
      * @return list of Cell from this to last
      */
     public ArrayList<CellNode> getNextCellsInColumn(int maxRowIndex) {
         return getNextCells(maxRowIndex, CELL_ROW, SplashRelationshipTypes.NEXT_CELL_IN_COLUMN);
     }
     
     /**
      * Returns all Cells from this to Cell in Column
      *
      * @param maxRowIndex index of last Cell
      * @return list of Cell from this to last
      */
     public ArrayList<CellNode> getNextCellsInRow(int maxRowIndex) {
         return getNextCells(maxRowIndex, CELL_COLUMN, SplashRelationshipTypes.NEXT_CELL_IN_ROW);
     }
     
     public CellNode getNextCellInColumn() {
         Iterator<Relationship> relationships = node.getRelationships(SplashRelationshipTypes.NEXT_CELL_IN_COLUMN, Direction.OUTGOING).iterator();
         
         if (relationships.hasNext()) {
             return CellNode.fromNode(relationships.next().getEndNode());
         }
         else {
             return null;
         }
     }
     
     /**
      * Returns a list of next Cells
      *
      * @param maxCellIndex index of last Cell
      * @param propertyName name of property to check index
      * @param relationshipType type of relationship to traverse
      * @return list of Cells
      */
     protected ArrayList<CellNode> getNextCells(int maxCellIndex, String propertyName, RelationshipType relationshipType) {
         ArrayList<CellNode> result = new ArrayList<CellNode>();
         
         NextCellsIterator nextCellsIterator = new NextCellsIterator(maxCellIndex, propertyName, relationshipType);
         
         while (nextCellsIterator.hasNext()) {
             result.add(nextCellsIterator.next());
         }
         
         return result;
     }
     
     public ArrayList<CellNode> getAllCellsFromThis(RelationshipType relationshipType, boolean returnFirst) {
         ArrayList<CellNode> result = new ArrayList<CellNode>();
         
         AllCellsIterator allCellsIterator = new AllCellsIterator(relationshipType, returnFirst);
         
         while (allCellsIterator.hasNext()) {
             result.add(allCellsIterator.next());
         }
         
         return result;
     }
     
     
     /**
      * Iterator to get next Cell from current
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     private class NextCellsIterator extends AbstractIterator<CellNode> {
         
         /**
          * Creates an iterator
          *
          * @param maxIndex index of last Cell
          * @param propertyName name of property to check index
          * @param relationshipType type of relationship to traverse
          */
         public NextCellsIterator(final int maxIndex, final String propertyName, RelationshipType relationshipType) {
             this.iterator = node.traverse(Order.DEPTH_FIRST, 
                                           new StopEvaluator(){
                                             
                                             @Override
                                             public boolean isStopNode(TraversalPosition currentPos) {
                                                 return ((Integer)currentPos.currentNode().getProperty(propertyName)) > maxIndex;
                                             }
                                           }, 
                                           new ReturnableEvaluator(){
                                             
                                             @Override
                                             public boolean isReturnableNode(TraversalPosition currentPos) {
                                                 return true;
                                             }
                                           },
                                           relationshipType,
                                           Direction.OUTGOING).iterator();
         }
 
         @Override
         protected CellNode wrapNode(Node node) {
             return CellNode.fromNode(node);
         }
     }
     
     protected class AllCellsIterator extends AbstractIterator<CellNode> {
         /**
          * Creates an iterator
          *
          * @param maxIndex index of last Cell
          * @param propertyName name of property to check index
          * @param relationshipType type of relationship to traverse
          */
         public AllCellsIterator(RelationshipType relationshipType, final boolean returnFirst) {
             this.iterator = node.traverse(Order.DEPTH_FIRST, 
                                           StopEvaluator.END_OF_GRAPH, 
                                           new ReturnableEvaluator(){
                                             
                                             @Override
                                             public boolean isReturnableNode(TraversalPosition currentPos) {
                                                 if (!returnFirst && (currentPos.depth() == 0)) {
                                                     return false;
                                                 }
                                                 return true;
                                             }
                                           },
                                           relationshipType,
                                           Direction.OUTGOING).iterator();
         }
 
         @Override
         protected CellNode wrapNode(Node node) {
             return CellNode.fromNode(node);
         }
     }
     
     public void setSpreadsheetId(long spreadsheetId) {
         setParameter(SPREADSHEET_ID, spreadsheetId);
     }
     
     public Long getSpreadsheetId() {
         return (Long)getParameter(SPREADSHEET_ID);
     }
 }
