 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation;
  * version 3.0 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  */
 package org.amanzi.awe.render.drive;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IStyleBlackboard;
 import net.refractions.udig.project.internal.render.Renderer;
 import net.refractions.udig.project.internal.render.impl.RendererImpl;
 import net.refractions.udig.project.render.RenderException;
 
 import org.amanzi.awe.catalog.neo.GeoConstant;
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.catalog.neo.GeoNeo.GeoNode;
 import org.amanzi.awe.neostyle.NeoStyle;
 import org.amanzi.awe.neostyle.NeoStyleContent;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * <p>
  * Renderer for GeoNeo with GisTypes==GisTypes.Tems
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.1.0
  */
 public class TemsRenderer extends RendererImpl implements Renderer {
     private MathTransform transform_d2w;
     private MathTransform transform_w2d;
     private AffineTransform base_transform = null;  // save original graphics transform for repeated re-use
     private Color drawColor = Color.BLACK;
     private Color fillColor = new Color(200, 128, 255, (int)(0.6*255.0));
     private Color labelColor = Color.DARK_GRAY;
     private Node aggNode;
     private static final Color COLOR_HIGHLIGHTED = Color.CYAN;
     private static final Color COLOR_SELECTED = Color.RED;
     private static final Color COLOR_LESS = Color.BLUE;
     private static final Color COLOR_MORE = Color.GREEN;
 
     @Override
     public void render(Graphics2D destination, IProgressMonitor monitor) throws RenderException {
         ILayer layer = getContext().getLayer();
         // Are there any resources in the layer that respond to the GeoNeo class (should be the case
         // if we found a Neo4J database with GeoNeo data)
         // TODO: Limit this to network data only
         IGeoResource resource = layer.findGeoResource(GeoNeo.class);
         if (resource != null) {
             renderGeoNeo(destination, resource, monitor);
         }
     }
 
     @Override
     public void render(IProgressMonitor monitor) throws RenderException {
         Graphics2D g = getContext().getImage().createGraphics();
         render(g, monitor);
     }
 
     private void setCrsTransforms(CoordinateReferenceSystem dataCrs) throws FactoryException {
         boolean lenient = true; // needs to be lenient to work on uDIG 1.1 (otherwise we get error:
         // bursa wolf parameters required
         CoordinateReferenceSystem worldCrs = context.getCRS();
         this.transform_d2w = CRS.findMathTransform(dataCrs, worldCrs, lenient);
        this.transform_w2d = CRS.findMathTransform(worldCrs, dataCrs, lenient); // could use
     }
 
     private Envelope getTransformedBounds() throws TransformException {
         ReferencedEnvelope bounds = getRenderBounds();
         if (bounds == null) {
             bounds = this.context.getViewportModel().getBounds();
         }
         Envelope bounds_transformed = null;
         if (bounds != null && transform_w2d != null) {
             bounds_transformed = JTS.transform(bounds, transform_w2d);
         }
         return bounds_transformed;
     }
 
     /**
      * This method is called to render data from the Neo4j 'GeoNeo' Geo-Resource.
      */
     private void renderGeoNeo(Graphics2D g, IGeoResource neoGeoResource, IProgressMonitor monitor) throws RenderException {
         if (monitor == null)
             monitor = new NullProgressMonitor();
         monitor.beginTask("render drive test data", IProgressMonitor.UNKNOWN);
         GeoNeo geoNeo = null;
 
         // Setup default drawing parameters and thresholds (to be modified by style if found)
         int maxSitesLabel = 30;
         int maxSitesFull = 100;
         int maxSitesLite = 1000;
         int maxSymbolSize = 40;
         int alpha = (int)(0.6*255.0);
         int drawSize = 3;
         Font font = g.getFont();
         int fontSize = font.getSize();
         IStyleBlackboard style = getContext().getLayer().getStyleBlackboard();
         NeoStyle neostyle = (NeoStyle)style.get(NeoStyleContent.ID);     
         if (neostyle!=null){
         	fillColor=neostyle.getFill();
         	drawColor=neostyle.getLine();
             alpha = 255 - (int)((double)neostyle.getSectorTransparency() / 100.0 * 255.0);
             try {
                 fillColor = neostyle.getFill();
                 drawColor = neostyle.getLine();
                 labelColor = neostyle.getLabel();
                 alpha = 255 - (int)((double)neostyle.getSectorTransparency() / 100.0 * 255.0);
                 //drawSize = neostyle.getSymbolSize();
                 drawSize = 3;
                 maxSitesLabel = neostyle.getLabeling();
                 maxSitesFull = neostyle.getSmallSymb();
                 maxSitesLite = neostyle.getSmallestSymb();
                 //scaleSectors = !neostyle.isFixSymbolSize();
                 maxSymbolSize = neostyle.getMaximumSymbolSize();
                 fontSize = neostyle.getFontSize();
                 //TODO: Remove these when defaults from style work property
                 maxSitesLabel = 50;
                 maxSitesLite = 500;
                 maxSitesFull = 50;
             } catch (Exception e) {
                 //TODO: we can get here if an old style exists, and we have added new fields
             }
         }
         g.setFont(font.deriveFont((float)fontSize));
         fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);
         int drawWidth = 1 + 2*drawSize;
         NeoService neo = NeoServiceProvider.getProvider().getService();
         Transaction tx = neo.beginTx();
         try {
             monitor.subTask("connecting");
             geoNeo = neoGeoResource.resolve(GeoNeo.class, new SubProgressMonitor(monitor, 10));
             String selectedProp = geoNeo.getPropertyName();
             aggNode = geoNeo.getAggrNode();
             Map<String, Object> selectionMap = (Map<String, Object>)geoNeo.getProperties(GeoNeo.DRIVE_INQUIRER);
             // Integer propertyAdjacency = geoNeo.getPropertyAdjacency();
             setCrsTransforms(neoGeoResource.getInfo(null).getCRS());
             Envelope bounds_transformed = getTransformedBounds();
             Envelope data_bounds = geoNeo.getBounds();
             boolean drawFull = true;
             boolean drawLite = true;
             boolean drawLabels = true;
             if (bounds_transformed == null) {
                 drawFull = false;
                 drawLite = false;
                 drawLabels = false;
             }else if (data_bounds != null && data_bounds.getHeight()>0 && data_bounds.getWidth()>0) {
                 double dataScaled = (bounds_transformed.getHeight() * bounds_transformed.getWidth())
                         / (data_bounds.getHeight() * data_bounds.getWidth());
                 double countScaled = dataScaled * geoNeo.getCount();
                 drawLabels = countScaled < maxSitesLabel;
                 drawFull = countScaled < maxSitesFull;
                 drawLite = countScaled < maxSitesLite;
             }
 
             g.setColor(drawColor);
             int count = 0;
             monitor.subTask("drawing");
             // single object for re-use in transform below (minimize object creation)
             Coordinate world_location = new Coordinate();
             java.awt.Point prev_p = null;
             java.awt.Point prev_l_p = null;
             java.awt.Point cached_l_p = null;
             GeoNode cached_node = null;  // for label positioning
             long startTime = System.currentTimeMillis();
             
             // First we find all selected points to draw with a highlight behind the main points
             ArrayList<Node> selectedPoints = new ArrayList<Node>();
             final Set<Node> selectedNodes = new HashSet<Node>(geoNeo.getSelectedNodes());
             // TODO refactor selection point (for example: in draws mp node add method
             // isSelected(node))
             if (selectionMap != null) {
                 Long beginTime = (Long)selectionMap.get(GeoConstant.Drive.BEGIN_TIME);
                 Long endTime = (Long)selectionMap.get(GeoConstant.Drive.END_TIME);
                 if (beginTime != null && endTime != null && beginTime <= endTime) {
                     for (GeoNode node : geoNeo.getGeoNodes(bounds_transformed)) {
                         Long time = NeoUtils.getNodeTime(node.getNode());
                         if (time != null && time >= beginTime && time <= endTime) {
                             selectedNodes.add(node.getNode());
                         }
                     }
                 }
             }
             for(Node node: selectedNodes) {
                 if("file".equals(node.getProperty("type",""))){
                     //Select all 'mp' nodes in that file
                     for (Node rnode:node.traverse(Traverser.Order.BREADTH_FIRST, new StopEvaluator(){
                             @Override
                             public boolean isStopNode(TraversalPosition currentPos) {
                                 return !currentPos.isStartNode() && "file".equals(currentPos.currentNode().getProperty("type", ""));
                             }}, new ReturnableEvaluator(){
         
                                 @Override
                                 public boolean isReturnableNode(TraversalPosition currentPos) {
                                     return "mp".equals(currentPos.currentNode().getProperty("type", ""));
                                 }}, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)){
                             selectedPoints.add(rnode);
                         }
                 } else {
                     //Traverse backwards on CHILD relations to closest 'mp' Point
                     for (@SuppressWarnings("unused")
                     Node rnode:node.traverse(Traverser.Order.DEPTH_FIRST, new StopEvaluator(){
                         @Override
                         public boolean isStopNode(TraversalPosition currentPos) {
                             return "mp".equals(currentPos.currentNode().getProperty("type", ""));
                         }}, new ReturnableEvaluator(){
     
                             @Override
                             public boolean isReturnableNode(TraversalPosition currentPos) {
                                 return "mp".equals(currentPos.currentNode().getProperty("type", ""));
                             }}, NetworkRelationshipTypes.CHILD, Direction.INCOMING)){
                         selectedPoints.add(rnode);
                         break;
                     }
                 }
             }
             // Now draw the selected points highlights
             for(Node rnode:selectedPoints){
                 GeoNode node = new GeoNode(rnode);
                 Coordinate location = node.getCoordinate();
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     continue;
                 }
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 if(prev_p != null && prev_p.x == p.x && prev_p.y == p.y) {
                     prev_p = p;
                     continue;
                 } else {
                     prev_p = p;
                 }
                 renderSelectedPoint(g, p, drawSize, drawFull, drawLite);
             }
             HashMap<String,Integer> colorErrors = new HashMap<String,Integer>();
             // Now draw the actual points
             for (GeoNode node : geoNeo.getGeoNodes(bounds_transformed)) {
                 Coordinate location = node.getCoordinate();
 
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     // JTS.transform(location, world_location, transform_w2d.inverse());
                 }
 
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 if(prev_p != null && prev_p.x == p.x && prev_p.y == p.y) {
                     prev_p = p;
                     continue;
                 } else {
                     prev_p = p;
                 }
 
                 Color nodeColor = fillColor;
                     try {
                         nodeColor = getNodeColor(node.getNode(), fillColor);
                         // nodeColor = getColorOfMpNode(select, node.getNode(), fillColor,
                         // selectedProp, redMinValue, redMaxValue,
                         // lesMinValue, moreMaxValue);
                     } catch (RuntimeException e) {
                         String errName = e.toString();
                         if(colorErrors.containsKey(errName)) {
                             colorErrors.put(errName, colorErrors.get(errName) + 1);
                         }else{
                             colorErrors.put(errName, 1);
                         }
                     }
                 Color borderColor = g.getColor();
                 if(selectedNodes.size() > 0) {
                     if (selectedNodes.contains(node.getNode())) {
                         borderColor = COLOR_HIGHLIGHTED;
                     }
                 }
                 renderPoint(g, p, borderColor, nodeColor, drawSize, drawWidth, drawFull, drawLite);
 
                 if (drawLabels) {
                     double theta = 0.0;
                     double dx = 0.0;
                     double dy = 0.0;
                     if (prev_l_p == null) {
                         prev_l_p = p;
                         cached_l_p = p;   // so we can draw first point using second point settings
                         cached_node = node;
                     } else {
                         try {
                             dx = p.x - prev_l_p.x;
                             dy = p.y - prev_l_p.y;
                             if (Math.abs(dx) < Math.abs(dy) / 2) {
                                 // drive goes north-south
                                 theta = 0;
                             } else if (Math.abs(dy) < Math.abs(dx) / 2) {
                                 // drive goes east-west
                                 theta = Math.PI / 2;
                             } else if (dx * dy < 0) {
                                 // drive has negative slope
                                 theta = -Math.PI / 4;
                             } else {
                                 theta = Math.PI / 4;
                             }
                         } catch (Exception e) {
                         }
                     }
                     if (Math.abs(dx) > 20 || Math.abs(dy) > 20) {
                         renderLabel(g, count, node, p, theta);
                         if(cached_node != null) {
                             renderLabel(g, 0, cached_node, cached_l_p, theta);   
                             cached_node = null;
                             cached_l_p = null;
                         }
                         prev_l_p = p;
                     }
                 }
                 if (base_transform != null) {
                     // recover the normal transform
                     g.setTransform(base_transform);
                     g.setColor(drawColor);
                     //base_transform = null;
                 }
                 monitor.worked(1);
                 count++;
                 if (monitor.isCanceled())
                     break;
             }
             for(String errName:colorErrors.keySet()){
                 int errCount = colorErrors.get(errName);
                 System.err.println("Error determining color of "+errCount+" nodes: "+errName);
             }
             System.out.println("Drive renderer took " + ((System.currentTimeMillis() - startTime) / 1000.0) + "s to draw " + count + " points");
             tx.success();
         } catch (TransformException e) {
             throw new RenderException(e);
         } catch (FactoryException e) {
             throw new RenderException(e);
         } catch (IOException e) {
             throw new RenderException(e); // rethrow any exceptions encountered
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             // if (geoNeo != null)
             // geoNeo.close();
             monitor.done();
             tx.finish();
         }
     }
 
     private void renderLabel(Graphics2D g, int count, GeoNode node, java.awt.Point p, double theta) {
         if (base_transform == null)
             base_transform = g.getTransform();
         g.setTransform(base_transform);
         g.translate(p.x, p.y);
         g.rotate(-theta);
         g.setColor(labelColor);
         //g.drawString(""+Integer.toString(count)+": "+node.toString(), 10, 5);
         g.drawString(node.toString(), 10, 5);
     }
 
     /**
      * gets sector color
      * 
      * @param child - sector node
      * @param defColor - default value
      * @return color
      */
     private Color getNodeColor(Node node, Color defColor) {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             if (aggNode == null) {
                 return defColor;
             }
             Node chartNode = NeoUtils.getChartNode(node, aggNode);
             if (chartNode == null) {
                 return defColor;
             }
             return new Color((Integer)chartNode.getProperty(INeoConstants.AGGREGATION_COLOR, defColor.getRGB()));
         } finally {
             tx.finish();
         }
     }
 
     // /**
     // * @param select
     // * @param moreMaxValue
     // * @param lesMinValue
     // * @param redMaxValue
     // * @param redMinValue
     // * @param selectedProp
     // * @param node
     // * @param fillColor2
     // * @return
     // */
     // private Color getColorOfMpNode(Select select, Node mpNode, Color defColor, String
     // selectedProp, Double redMinValue,
     // Double redMaxValue, Double lesMinValue, Double moreMaxValue) {
     // Color colorToFill = defColor;
     // switch (select) {
     // case AVERAGE:
     // case MAX:
     // case MIN:
     //
     // Double sum = new Double(0);
     // int count = 0;
     // Double min = null;
     // Double max = null;
     // Double average = null;
     // Double firstValue = null;
     // for (Relationship relation : mpNode.getRelationships(MeasurementRelationshipTypes.CHILD,
     // Direction.OUTGOING)) {
     // Node node = relation.getEndNode();
     // if (INeoConstants.HEADER_MS.equals(node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, ""))
     // && node.hasProperty(selectedProp)) {
     // double value = ((Number)node.getProperty(selectedProp)).doubleValue();
     // min = min == null ? value : Math.min(min, value);
     // max = max == null ? value : Math.max(max, value);
     // // TODO hande gets firstValue by other way
     // firstValue = firstValue == null ? value : firstValue;
     // sum = sum + value;
     // count++;
     // }
     // }
     // average = (double)sum / (double)count;
     // double checkValue = select == Select.MAX ? max : select == Select.MIN ? min : select ==
     // Select.AVERAGE ? average
     // : firstValue;
     //
     // if (checkValue < redMaxValue || checkValue == redMinValue) {
     // if (checkValue >= redMinValue) {
     // colorToFill = COLOR_SELECTED;
     // } else if (checkValue >= lesMinValue) {
     // colorToFill = COLOR_LESS;
     // }
     // } else if (checkValue < moreMaxValue) {
     // colorToFill = COLOR_MORE;
     // }
     // return colorToFill;
     // case EXISTS:
     // int priority = -1;
     // for (Relationship relation : mpNode.getRelationships(NetworkRelationshipTypes.CHILD,
     // Direction.OUTGOING)) {
     // Node child = relation.getEndNode();
     //
     // for (String key : child.getPropertyKeys()) {
     // if (selectedProp.equals(key)) {
     // double value = ((Number)child.getProperty(selectedProp)).doubleValue();
     // if (value < redMaxValue || value == redMinValue) {
     // if (value >= redMinValue) {
     // colorToFill = COLOR_SELECTED;
     // priority = 3;
     // } else if (value >= lesMinValue && (priority < 2)) {
     // colorToFill = COLOR_LESS;
     // priority = 1;
     //
     // }
     // } else if (value < moreMaxValue && priority < 3) {
     // colorToFill = COLOR_MORE;
     // priority = 2;
     // }
     // }
     // }
     // }
     // return colorToFill;
     // case FIRST:
     // Double result = null;
     // Iterator<Node> iterator = mpNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new
     // ReturnableEvaluator() {
     //
     // @Override
     // public boolean isReturnableNode(TraversalPosition currentPos) {
     // return !currentPos.currentNode().hasRelationship(GeoNeoRelationshipTypes.NEXT,
     // Direction.INCOMING);
     // }
     // }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
     // if (!iterator.hasNext()) {
     // return colorToFill;
     // }
     // Node node = iterator.next();
     // while (!node.hasProperty(selectedProp)) {
     // Relationship relation = node.getSingleRelationship(GeoNeoRelationshipTypes.NEXT,
     // Direction.OUTGOING);
     // if (relation == null) {
     // return colorToFill;
     // }
     // node = relation.getOtherNode(node);
     //
     // }
     // checkValue = ((Number)node.getProperty(selectedProp)).doubleValue();
     // if (checkValue < redMaxValue || checkValue == redMinValue) {
     // if (checkValue >= redMinValue) {
     // colorToFill = COLOR_SELECTED;
     // } else if (checkValue >= lesMinValue) {
     // colorToFill = COLOR_LESS;
     // }
     // } else if (checkValue < moreMaxValue) {
     // colorToFill = COLOR_MORE;
     // }
     // return colorToFill;
     // default:
     // break;
     // }
     // return defColor;
     // }
 
     /**
      * This one is very simple, just draw a rectangle at the point location.
      * 
      * @param g
      * @param p
      */
     private void renderPoint(Graphics2D g, java.awt.Point p, Color borderColor, Color fillColor, int drawSize, int drawWidth, boolean drawFull, boolean drawLite) {
         Color oldColor = g.getColor();
         if(drawFull) {
             g.setColor(fillColor);
             g.fillRect(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
             g.setColor(borderColor);
             g.drawRect(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
         } else if (drawLite) {
             g.setColor(fillColor);
             g.fillOval(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
         } else {
             g.setColor(fillColor);
             g.fillOval(p.x - 1, p.y - 1, 3, 3);
         }
         g.setColor(oldColor);
     }
 
     /**
      * This one is very simple, just draw a rectangle at the point location.
      * 
      * @param g
      * @param p
      */
     private void renderSelectedPoint(Graphics2D g, java.awt.Point p, int drawSize, boolean drawFull, boolean drawLite) {
         Color oldColor = g.getColor();
         if(drawFull) {
             renderSelectionGlow(g, p, drawSize * 3);
         } else if (drawLite) {
             renderSelectionGlow(g, p, drawSize * 2);
         } else {
             renderSelectionGlow(g, p, drawSize);
         }
         g.setColor(oldColor);
     }
 
     /**
      * This method draws a fading glow around a point for selected site/sectors
      *
      * @param g
      * @param p
      * @param drawSize
      */
     private void renderSelectionGlow(Graphics2D g, java.awt.Point p, int drawSize) {
         drawSize *= 3;
         Color highColor = new Color(COLOR_HIGHLIGHTED.getRed(), COLOR_HIGHLIGHTED.getGreen(), COLOR_HIGHLIGHTED.getBlue(), 8);
         g.setColor(highColor);
         for(;drawSize > 2; drawSize *= 0.8) {
             g.fillOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
         }
     }
 
     /**
      * <p>
      * TODO union with org.amanzi.awe.views.reuse.Select now simple copy enum from
      * org.amanzi.awe.views.reuse.Select
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.1.0
      */
     private enum Select {
         MAX("max"), MIN("min"), AVERAGE("average"), EXISTS("exists"), FIRST("first");
         private final String value;
 
         /**
          * Constructor
          * 
          * @param value - string value
          */
         private Select(String value) {
             this.value = value;
         }
 
         public static Select findSelectByValue(String value) {
             if (value == null) {
                 return null;
             }
             for (Select selection : Select.values()) {
                 if (selection.value.equals(value)) {
                     return selection;
                 }
             }
             return null;
         }
 
         public static String[] getEnumAsStringArray() {
             Select[] enums = Select.values();
             String[] result = new String[enums.length];
             for (int i = 0; i < enums.length; i++) {
                 result[i] = enums[i].value;
             }
             return result;
         }
 
         @Override
         public String toString() {
             return value;
         }
     }
 }
