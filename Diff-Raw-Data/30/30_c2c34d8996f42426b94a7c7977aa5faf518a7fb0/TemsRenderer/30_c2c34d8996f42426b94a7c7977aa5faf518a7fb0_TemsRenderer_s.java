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
 import java.awt.Graphics2D;
 import java.io.IOException;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IStyleBlackboard;
 import net.refractions.udig.project.internal.render.Renderer;
 import net.refractions.udig.project.internal.render.impl.RendererImpl;
 import net.refractions.udig.project.render.RenderException;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.catalog.neo.GeoNeo.GeoNode;
 import org.amanzi.awe.neostyle.NeoStyle;
 import org.amanzi.awe.neostyle.NeoStyleContent;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.MeasurementRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
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
     // private AffineTransform base_transform = null;
     private Color drawColor = Color.BLACK;
    private Color fillColor = new Color(200, 128, 255);
     private int drawSize = 3;
     private int drawWidth = 1 + 2*drawSize;
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
         // transform_d2w.inverse()
         // also
     }
 
     private Envelope getTransformedBounds() throws TransformException {
         ReferencedEnvelope bounds = getRenderBounds();
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
 
         //TODO: Get the symbol size, transparency and color values from a preference dialog or style dialog
        int transparency = (int)(0.6*255.0);
        fillColor = new Color(200, 128, 255, transparency);
         drawSize = 3;
         drawWidth = 1 + 2*drawSize;
         IStyleBlackboard style = getContext().getLayer().getStyleBlackboard();
         NeoStyle neostyle = (NeoStyle)style.get(NeoStyleContent.ID);     
         if (neostyle!=null){
         	fillColor=neostyle.getFill();
         	drawColor=neostyle.getLine();
         }
         try {
             monitor.subTask("connecting");
             geoNeo = neoGeoResource.resolve(GeoNeo.class, new SubProgressMonitor(monitor, 10));
             String selectedProp = geoNeo.getPropertyName();
             Double redMinValue = geoNeo.getPropertyValueMin();
             Double redMaxValue = geoNeo.getPropertyValueMax();
             Double lesMinValue = geoNeo.getMinPropertyValue();
             Double moreMaxValue = geoNeo.getMaxPropertyValue();
             Select select = Select.findSelectByValue(geoNeo.getSelectName());
             // Integer propertyAdjacency = geoNeo.getPropertyAdjacency();
             setCrsTransforms(neoGeoResource.getInfo(null).getCRS());
             Envelope bounds_transformed = getTransformedBounds();
 
             g.setColor(drawColor);
             int count = 0;
             monitor.subTask("drawing");
             Coordinate world_location = new Coordinate(); // single object for re-use in transform
             // below (minimize object creation)
             for (GeoNode node : geoNeo.getGeoNodes()) {
             	System.out.println("in");
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
 
                 Color nodeColor = fillColor;
                 if (selectedProp != null) {
                     nodeColor = getColorOfMpNode(select, node.getNode(), fillColor, selectedProp, redMinValue, redMaxValue,
                             lesMinValue, moreMaxValue);
 
                 }
                 renderPoint(g, p, nodeColor);
                 monitor.worked(1);
                 count++;
                 if (monitor.isCanceled())
                     break;
             }
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
         }
     }
 
     /**
      * @param select
      * @param moreMaxValue
      * @param lesMinValue
      * @param redMaxValue
      * @param redMinValue
      * @param selectedProp
      * @param node
      * @param fillColor2
      * @return
      */
     private Color getColorOfMpNode(Select select, Node mpNode, Color defColor, String selectedProp, Double redMinValue,
             Double redMaxValue, Double lesMinValue, Double moreMaxValue) {
         Color colorToFill = defColor;
         switch (select) {
         case AVERAGE:
         case MAX:
         case MIN:
         case FIRST:
             Double sum = new Double(0);
             int count = 0;
             Double min = null;
             Double max = null;
             Double average = null;
             Double firstValue = null;
             for (Relationship relation : mpNode.getRelationships(MeasurementRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 Node node = relation.getEndNode();
                 if (INeoConstants.HEADER_MS.equals(node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, ""))
                         && node.hasProperty(selectedProp)) {
                     double value = ((Number)node.getProperty(selectedProp)).doubleValue();
                     min = min == null ? value : Math.min(min, value);
                     max = max == null ? value : Math.max(max, value);
                     // TODO hande gets firstValue by other way
                     firstValue = firstValue == null ? value : firstValue;
                     sum = sum + value;
                     count++;
                 }
             }
             average = (double)sum / (double)count;
             double checkValue = select == Select.MAX ? max : select == Select.MIN ? min : select == Select.AVERAGE ? average
                     : firstValue;
 
             if (checkValue < redMaxValue || checkValue == redMinValue) {
                 if (checkValue >= redMinValue) {
                     colorToFill = COLOR_SELECTED;
                 } else if (checkValue >= lesMinValue) {
                     colorToFill = COLOR_LESS;
                 }
             } else if (checkValue < moreMaxValue) {
                 colorToFill = COLOR_MORE;
             }
             return colorToFill;
         case EXISTS:
             int priority = -1;
            mainLoop: for (Relationship relation : mpNode.getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 Node child = relation.getEndNode();
 
                 for (String key : child.getPropertyKeys()) {
                     if (selectedProp.equals(key)) {
                        Object property = child.getProperty(key);
                         double value = ((Number)child.getProperty(selectedProp)).doubleValue();
                         if (value < redMaxValue || value == redMinValue) {
                             if (value >= redMinValue) {
                                 colorToFill = COLOR_SELECTED;
                                 priority = 3;
                             } else if (value >= lesMinValue && (priority < 2)) {
                                 colorToFill = COLOR_LESS;
                                 priority = 1;
 
                             }
                         } else if (value < moreMaxValue && priority < 3) {
                             colorToFill = COLOR_MORE;
                             priority = 2;
                         }
                     }
                 }
             }
             return colorToFill;
         default:
             break;
         }
         return defColor;
     }
 
     /**
      * This one is very simple, just draw a rectangle at the point location.
      * 
      * @param g
      * @param p
      */
     private void renderPoint(Graphics2D g, java.awt.Point p, Color fillColor) {
         g.setColor(fillColor);
         g.fillRect(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
         g.setColor(drawColor);
         g.drawRect(p.x - drawSize, p.y - drawSize, drawWidth, drawWidth);
 
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
