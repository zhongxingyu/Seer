 /*
  *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
  *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
  * 
  *  MONGKIE is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  MONGKE is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  * 
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mongkie.exporter.plugins.graph;
 
 import java.awt.Color;
 import java.awt.geom.Rectangle2D;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import static kobic.prefuse.Constants.EDGES;
 import static kobic.prefuse.Constants.NODES;
 import kobic.prefuse.data.io.SerializableTable;
 import org.mongkie.exporter.spi.AbstractGraphExporter;
 import org.mongkie.visualization.color.ColorController;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import prefuse.Visualization;
 import static prefuse.Visualization.AGGR_ITEMS;
 import prefuse.data.Graph;
 import prefuse.data.Table;
 import prefuse.data.Tuple;
 import prefuse.util.ColorLib;
 import prefuse.visual.*;
 import static prefuse.visual.VisualItem.*;
 
 /**
  *
  * @author Yeongjun Jang <yjjang@kribb.re.kr>
  */
 public class ExporterVizGraph extends AbstractGraphExporter {
 
     @Override
     public boolean execute() {
         ObjectOutputStream oos = null;
         try {
             oos = new ObjectOutputStream(out);
             Graph g = display.getGraph();
             // Node and Edge table
             SerializableTable serializableNodeTable = new SerializableTable(g.getNodeTable());
             oos.writeObject(serializableNodeTable);
             SerializableTable serializableEdgeTable = new SerializableTable(g.getEdgeTable());
             String nodeKey = g.getNodeKeyField();
             String sourceKey = g.getEdgeSourceField();
             String targetKey = g.getEdgeTargetField();
            //// Rows of source and target nodes must be reassigned to serialized values
             if (nodeKey == null) {
                 for (Iterator<Tuple> edgesIter = serializableEdgeTable.getTable().tuples(); edgesIter.hasNext();) {
                     Tuple edge = edgesIter.next();
                     edge.setInt(sourceKey, serializableNodeTable.getRow(edge.getInt(sourceKey)));
                     edge.setInt(targetKey, serializableNodeTable.getRow(edge.getInt(targetKey)));
                 }
             }
            oos.writeObject(serializableEdgeTable);
            // Direction
            oos.writeBoolean(g.isDirected());
            // Node key field
            oos.writeObject(nodeKey);
            // Edge source and target field
            oos.writeObject(sourceKey);
            oos.writeObject(targetKey);
             // Label field for nodes and edges
             oos.writeObject(g.getNodeLabelField());
             oos.writeObject(g.getEdgeLabelField());
             // Visual table for nodes and edges
             SerializableTable serializableNodeItemTable = new SerializableTable((VisualTable) display.getVisualization().getVisualGroup(NODES));
             reassignVisualFields(serializableNodeItemTable);
             oos.writeObject(serializableNodeItemTable);
             SerializableTable serializableEdgeItemTable = new SerializableTable((VisualTable) display.getVisualization().getVisualGroup(EDGES));
             reassignVisualFields(serializableEdgeItemTable);
             oos.writeObject(serializableEdgeItemTable);
             // Aggregate table for groups
             AggregateTable aggregateTable = (AggregateTable) display.getVisualization().getVisualGroup(AGGR_ITEMS);
             SerializableTable serializableAggrTable = new SerializableTable(aggregateTable);
             reassignVisualFields(serializableAggrTable);
             oos.writeObject(serializableAggrTable);
             //// Aggregate(group) id and its member(node) rows need to be serialized
             Map<Integer, List<Integer>> aggregateId2NodeItemRows = new HashMap<Integer, List<Integer>>();
             for (Iterator<AggregateItem> aggregates = aggregateTable.tuples(); aggregates.hasNext();) {
                 AggregateItem aggr = aggregates.next();
                 List<Integer> nodeItemRows = new ArrayList<Integer>();
                 for (Iterator<NodeItem> nodeItemsIter = aggr.items(); nodeItemsIter.hasNext();) {
                     nodeItemRows.add(serializableNodeItemTable.getRow(nodeItemsIter.next().getRow()));
                 }
                 if (!nodeItemRows.isEmpty()) {
                     aggregateId2NodeItemRows.put(aggr.getInt(AggregateItem.AGGR_ID), nodeItemRows);
                 }
             }
             oos.writeObject(aggregateId2NodeItemRows);
         } catch (IOException ex) {
             Exceptions.printStackTrace(ex);
             throw new RuntimeException("An error occured while exporting a visual graph", ex);
         } finally {
             if (oos != null) {
                 try {
                     oos.flush();
                     oos.close();
                 } catch (IOException ex) {
                     Exceptions.printStackTrace(ex);
                     return false;
                 }
             }
         }
         Logger.getLogger(getClass().getName()).log(Level.INFO, "Visual graph is exported successfully");
         return true;
     }
 
     /**
      * When exporting a visual graph, some visual fields need to be reassigned
      *
      * @param serializable a {@link SerializableTable}, base table of which is a {@link VisualTable}
      */
     private void reassignVisualFields(SerializableTable serializable) {
         Table derived = serializable.getTable();
         // Color fields reassigned to values get from ColorController
         ColorController cc = Lookup.getDefault().lookup(ColorController.class);
         // Move visual items so that center of visualization bounds is at the (0,0)
         Rectangle2D vizBounds = display.getVisualization().getBounds(Visualization.ALL_ITEMS);
         for (Iterator<VisualItem> itemsIter = ((VisualTable) serializable.getBaseTable()).tuples(); itemsIter.hasNext();) {
             VisualItem item = itemsIter.next();
             int row = serializable.getRow(item.getRow());
             // Reassign color fields
             Color c = cc.getFillColor(item);
             if (c != null) {
                 derived.set(row, FILLCOLOR, ColorLib.color(c));
             }
             c = cc.getStrokeColor(item);
             if (c != null) {
                 derived.set(row, STROKECOLOR, ColorLib.color(c));
             }
             c = cc.getTextColor(item);
             if (c != null) {
                 derived.set(row, TEXTCOLOR, ColorLib.color(c));
             }
             // Reassign item locations
             derived.set(row, STARTX, item.getStartX() - vizBounds.getCenterX());
             derived.set(row, STARTY, item.getStartY() - vizBounds.getCenterY());
             derived.set(row, ENDX, item.getEndX() - vizBounds.getCenterX());
             derived.set(row, ENDY, item.getEndY() - vizBounds.getCenterY());
             derived.set(row, X, item.getX() - vizBounds.getCenterX());
             derived.set(row, Y, item.getY() - vizBounds.getCenterY());
         }
     }
 
     @Override
     public boolean cancel() {
         return false;
     }
 
     @Override
     public boolean supportsSelectionOnly() {
         return false;
     }
 }
