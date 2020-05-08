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
 package org.amanzi.awe.render.network;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.font.TextLayout;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.core.Pair;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IStyleBlackboard;
 import net.refractions.udig.project.internal.render.impl.RendererImpl;
 import net.refractions.udig.project.render.RenderException;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.catalog.neo.GeoNeo.GeoNode;
 import org.amanzi.awe.neostyle.NeoStyle;
 import org.amanzi.awe.neostyle.NeoStyleContent;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.preferences.DataLoadPreferences;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
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
 
 public class NetworkRenderer extends RendererImpl {
     public static final String BLACKBOARD_NODE_LIST = "org.amanzi.awe.tool.star.StarTool.nodes";
     public static final String BLACKBOARD_START_ANALYSER = "org.amanzi.awe.tool.star.StarTool.analyser";
     private static final Color COLOR_SITE_SELECTED = Color.CYAN;
     private static final Color COLOR_SECTOR_SELECTED = Color.CYAN;
     private static final Color COLOR_SECTOR_STAR = Color.RED;
     private AffineTransform base_transform = null;  // save original graphics transform for repeated re-use
     private Color drawColor = Color.DARK_GRAY;
     private Color siteColor = new Color(128, 128, 128,(int)(0.6*255.0));
     private Color fillColor = new Color(255, 255, 128,(int)(0.6*255.0));
     private MathTransform transform_d2w;
     private MathTransform transform_w2d;
     private Color labelColor = Color.DARK_GRAY;
     private Color surroundColor = Color.WHITE;
     private Color lineColor;
     private Node aggNode;
     private String siteName;
     private boolean normalSiteName;
     private String sectorName;
     private boolean sectorLabeling;
     private void setCrsTransforms(CoordinateReferenceSystem dataCrs) throws FactoryException{
         boolean lenient = true; // needs to be lenient to work on uDIG 1.1 (otherwise we get error: bursa wolf parameters required
         CoordinateReferenceSystem worldCrs = context.getCRS();
         this.transform_d2w = CRS.findMathTransform(dataCrs, worldCrs, lenient);
         this.transform_w2d = CRS.findMathTransform(worldCrs, dataCrs, lenient); // could use transform_d2w.inverse() also
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
      * This method is called to render what it can. It is passed a graphics context
      * with which it can draw. The class already contains a reference to a RenderContext
      * from which it can obtain the layer and the GeoResource to render.
      * @see net.refractions.udig.project.internal.render.impl.RendererImpl#render(java.awt.Graphics2D, org.eclipse.core.runtime.IProgressMonitor)
      */
     @Override
     public void render( Graphics2D g, IProgressMonitor monitor ) throws RenderException {
         ILayer layer = getContext().getLayer();
         // Are there any resources in the layer that respond to the GeoNeo class (should be the case if we found a Neo4J database with GeoNeo data)
         IGeoResource resource = layer.findGeoResource(GeoNeo.class);
         if(resource != null){
             renderGeoNeo(g,resource,monitor);
         }
     }
 
     /**
      * This method is called to render data from the Neo4j 'GeoNeo' Geo-Resource.
      */
     private void renderGeoNeo( Graphics2D g, IGeoResource neoGeoResource, IProgressMonitor monitor ) throws RenderException {
         if (monitor == null)
             monitor = new NullProgressMonitor();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
         monitor.beginTask("render network sites and sectors: "+neoGeoResource.getIdentifier(), IProgressMonitor.UNKNOWN);    // TODO: Get size from info
 
         GeoNeo geoNeo = null;
 
         // Setup default drawing parameters and thresholds (to be modified by style if found)
         int drawSize=15;
         int alpha = (int)(0.6*255.0);
         int maxSitesLabel = 30;
         int maxSitesFull = 100;
         int maxSitesLite = 1000;
         int maxSymbolSize = 40;
         Font font = g.getFont();
         int fontSize = font.getSize();
         int sectorFontSize = font.getSize();
         boolean scaleSectors = true;
 
         IStyleBlackboard style = getContext().getLayer().getStyleBlackboard();
         NeoStyle neostyle = (NeoStyle)style.get(NeoStyleContent.ID );     
         siteName = NeoStyleContent.DEF_SITE_NAME;
         sectorName = NeoStyleContent.DEF_SECTOR_NAME;
         if (neostyle!=null){
             try {
                 siteColor = neostyle.getSiteFill();
                 fillColor=neostyle.getFill();
                 drawColor=neostyle.getLine();
                 labelColor=neostyle.getLabel();
                 float colSum = 0.0f;
                 for(float comp: labelColor.getRGBColorComponents(null)){
                     colSum += comp;
                 }
                 if(colSum>2.0) {
                     surroundColor=Color.DARK_GRAY;
                 } else {
                     surroundColor=Color.WHITE;
                 }
                 drawSize = neostyle.getSymbolSize();
                 alpha = 255 - (int)((double)neostyle.getSectorTransparency() / 100.0 * 255.0);
                 maxSitesLabel = neostyle.getLabeling();
                 maxSitesFull = neostyle.getSmallSymb();
                 maxSitesLite = neostyle.getSmallestSymb();
                 scaleSectors = !neostyle.isFixSymbolSize();
                 maxSymbolSize = neostyle.getMaximumSymbolSize();
                 fontSize = neostyle.getFontSize();
                 sectorFontSize = neostyle.getSectorFontSize();
                 siteName = neostyle.getSiteName();
                 sectorName = neostyle.getSectorName();
             } catch (Exception e) {
                 //TODO: we can get here if an old style exists, and we have added new fields
             }
         }
         normalSiteName = NeoStyleContent.DEF_SITE_NAME.equals(siteName);
         sectorLabeling = !NeoStyleContent.DEF_SECTOR_NAME.equals(sectorName);
         g.setFont(font.deriveFont((float)fontSize));
         lineColor = new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), alpha);
         siteColor = new Color(siteColor.getRed(), siteColor.getGreen(), siteColor.getBlue(), alpha);
         fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);
         Map<Node, java.awt.Point> nodesMap = new HashMap<Node, java.awt.Point>();
         Map<Node, java.awt.Point> sectorMap = new HashMap<Node, java.awt.Point>();
         Map<Point, String> labelsMap = new HashMap<Point, String>();
         NeoService neo = NeoServiceProvider.getProvider().getService();
         Transaction tx = neo.beginTx();
         try {
             monitor.subTask("connecting");
             geoNeo = neoGeoResource.resolve(GeoNeo.class, new SubProgressMonitor(monitor, 10));
             System.out.println("NetworkRenderer resolved geoNeo '"+geoNeo.getName()+"' from resource: "+neoGeoResource.getIdentifier());
             //IBlackboard blackboard = getContext().getMap().getBlackboard();
             String starProperty = getSelectProperty(geoNeo);
             Pair<Point, Long> starPoint = getStarPoint();
             Node starNode = null;
             if(starPoint != null) {
                 System.out.println("Have star selection: "+starPoint);
             }
             ArrayList<Pair<String,Integer>> multiOmnis = new ArrayList<Pair<String,Integer>>();
             aggNode = geoNeo.getAggrNode();
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
                 long count = geoNeo.getCount();
                 if (NeoLoaderPlugin.getDefault().getPreferenceStore().getBoolean(DataLoadPreferences.NETWORK_COMBINED_CALCULATION)) {
                     count = getAverageCount(monitor);
                 }
 
                 double countScaled = dataScaled * count;
                 drawLabels = countScaled < maxSitesLabel;
                 drawFull = countScaled < maxSitesFull;
                 drawLite = countScaled < maxSitesLite;
                 if (drawFull && scaleSectors) {
                     drawSize *= Math.sqrt(maxSitesFull) / (3 * Math.sqrt(countScaled));
                     drawSize = Math.min(drawSize, maxSymbolSize);
                 }
                 // expand the boundary to include sites just out of view (so partial sectors can be see)
                 bounds_transformed.expandBy(0.75 * (bounds_transformed.getHeight() + bounds_transformed.getWidth()));
             }
 
             g.setColor(drawColor);
             int count = 0;
             monitor.subTask("drawing");
             Coordinate world_location = new Coordinate(); // single object for re-use in transform below (minimize object creation)
             long startTime = System.currentTimeMillis();
             for(GeoNode node:geoNeo.getGeoNodes(bounds_transformed)) {
                 Coordinate location = node.getCoordinate();
 
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     //JTS.transform(location, world_location, transform_w2d.inverse());
                 }
 
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 Color borderColor = g.getColor();
                 boolean selected = false;
                 if (geoNeo.getSelectedNodes().contains(node.getNode())) {
                     borderColor = COLOR_SITE_SELECTED;
                     selected = true;
                 } else {
                     for (Node rnode:node.getNode().traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.BOTH)){
                         if (geoNeo.getSelectedNodes().contains(rnode)) {
                             selected = true;
                             break;
                         }
                     }
                     if(!selected) {
                         DELTA_LOOP: for (Node rnode:node.getNode().traverse(Traverser.Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.MISSING, Direction.INCOMING, NetworkRelationshipTypes.DIFFERENT, Direction.INCOMING)){
                             if (geoNeo.getSelectedNodes().contains(rnode)) {
                                 selected = true;
                                 break;
                             } else {
                                 for (Node xnode:rnode.traverse(Traverser.Order.BREADTH_FIRST, new StopEvaluator(){
 
                                     @Override
                                     public boolean isStopNode(TraversalPosition currentPos) {
                                         return "delta_report".equals(currentPos.currentNode().getProperty("type",""));
                                     }}, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.INCOMING)){
                                     if (geoNeo.getSelectedNodes().contains(xnode)) {
                                         selected = true;
                                         break DELTA_LOOP;
                                     }
                                 }
                             }
                         }
                     }
                 }
                 renderSite(g, p, borderColor, siteColor, drawSize, drawFull, drawLite, selected);
                 if (drawFull) {
                     int countOmnis = 0;
                     double[] label_position_angles = new double[] {0, 90};
                     try {
                         int s = 0;
                         for (Relationship relationship : node.getNode().getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                             Node child = relationship.getEndNode();
                             if (child.hasProperty("type") && child.getProperty("type").toString().equals("sector")) {
                                 // double azimuth = Double.NaN;
                                 double beamwidth = getDouble(child, "beamwidth", 360.0);
                                 Color colorToFill = getSectorColor(child, fillColor);
                                 double azimuth = getDouble(child, "azimuth", Double.NaN);
                                 if (azimuth == Double.NaN) {
                                     continue;
                                 }
                                 borderColor = drawColor;
                                 if (starPoint != null && starPoint.right().equals(child.getId())) {
                                     borderColor = COLOR_SECTOR_STAR;
                                     starNode = child;
                                 } else
                                 if (geoNeo.getSelectedNodes().contains(child)) {
                                     borderColor = COLOR_SECTOR_SELECTED;
                                 }
                                 // put sector information in to blackboard
 
                                 Pair<Point, Point> centerPoint = renderSector(g, p, azimuth, beamwidth, colorToFill, borderColor, drawSize);
                                 nodesMap.put(child, centerPoint.getLeft());
                                 if (sectorLabeling){
                                     sectorMap.put(child, centerPoint.getRight());
                                 }
                                 if (s < label_position_angles.length) {
                                     label_position_angles[s] = azimuth;
                                 }
                                 // g.setColor(drawColor);
                                 // g.rotate(-Math.toRadians(beamwidth/2));
                                 // g.drawString(sector.getString("name"),drawSize,0);
                                 if(beamwidth==360) countOmnis++;
                                 s++;
                             }
                         }
                     } finally {
                         if (base_transform != null) {
                             // recover the normal transform
                             g.setTransform(base_transform);
                             g.setColor(drawColor);
                         }
                     }
                     if (base_transform != null) {
                         g.setTransform(base_transform);
                     }
                     String drawString = getSiteName(node);
                     if (countOmnis>1) {
                         //System.err.println("Site "+node+" had "+countOmnis+" omni antennas");
                         multiOmnis.add(new Pair<String, Integer>(drawString, countOmnis));
                     }
                     if (drawLabels) {
                         labelsMap.put(p, drawString);
                     }
                 }
                 monitor.worked(1);
                 count++;
                 if (monitor.isCanceled())
                     break;
             }
             if (drawLabels && labelsMap.size() > 0) {
                 Set<Rectangle> labelRec = new HashSet<Rectangle>();
                 FontMetrics metrics = g.getFontMetrics(font);
                 // get the height of a line of text in this font and render context
                 int hgt = metrics.getHeight();
                 for(Point p: labelsMap.keySet()) {
                     String drawString = labelsMap.get(p);
                     int label_x = drawSize > 15 ? 15 : drawSize;
                     int label_y = hgt / 3;
                     p = new Point(p.x + label_x, p.y + label_y);
 
                     // get the advance of my text in this font and render context
                     int adv = metrics.stringWidth(drawString);
                     // calculate the size of a box to hold the text with some padding.
                     Rectangle rect = new Rectangle(p.x -1 , p.y - hgt + 1, adv + 2, hgt + 2);
                     boolean drawsLabel = findNonOverlapPosition(labelRec, hgt, p, rect);
                     if (drawsLabel) {
                         labelRec.add(rect);
                         drawLabel(g, p, drawString);
                     }
                 }
                 // draw sector name
                 if (sectorLabeling) {
                     Font fontOld = g.getFont();
                     Font fontSector = fontOld.deriveFont((float)sectorFontSize);
                     g.setFont(fontSector);
                     FontMetrics metric = g.getFontMetrics(fontSector);
                     hgt = metrics.getHeight();
                     int h = hgt / 3;
                     for (Node sector : nodesMap.keySet()) {
                         String name = getSectorName(sector);
                         if (name.isEmpty()) {
                             continue;
                         }
                         int w = metric.stringWidth(name);
                         Point pSector = nodesMap.get(sector);
                         Point endLine = sectorMap.get(sector);
                         // calculate p
                         int x = (endLine.x < pSector.x) ? endLine.x - w : endLine.x;
                         int y = (endLine.y < pSector.y) ? endLine.y - h : endLine.y;
                         Point p = new Point(x, y);
                         // get the advance of my text in this font and render context
                         int adv = metrics.stringWidth(name);
                         // calculate the size of a box to hold the text with some padding.
                         Rectangle rect = new Rectangle(p.x -1 , p.y - hgt + 1, adv + 2, hgt + 2);
                         boolean drawsLabel = findNonOverlapPosition(labelRec, hgt, p, rect);
                         if (drawsLabel) {
                             labelRec.add(rect);
                             drawLabel(g, p, name);
                         }
                     }
                     g.setFont(fontOld);
                 }
             }
             if(multiOmnis.size()>0){
                 //TODO: Move this to utility class
                 StringBuffer sb = new StringBuffer();
                 int llen=0;
                 for (Pair<String, Integer> pair : multiOmnis) {
                     if (sb.length() > 1)
                         sb.append(", ");
                     else
                         sb.append("\t");
                     if (sb.length() > 100 + llen) {
                         llen = sb.length();
                         sb.append("\n\t");
                     }
                     sb.append(pair.left()).append(":").append(pair.right());
                     if (sb.length() > 1000)
                         break;
                 }
                 System.err.println("There were "+multiOmnis.size()+" sites with more than one omni antenna: ");
                 System.err.println(sb.toString());
             }
             if (starNode != null && starProperty != null) {
                 drawAnalyser(g, starNode, starPoint.left(), starProperty, nodesMap);
             }
             String neiName = (String)geoNeo.getProperties(GeoNeo.NEIGH_NAME);
             if (neiName != null) {
                 Object properties = geoNeo.getProperties(GeoNeo.NEIGH_RELATION);
                 if (properties != null) {
                     drawRelation(g, (Relationship)properties, lineColor, nodesMap);
                 }
                 properties = geoNeo.getProperties(GeoNeo.NEIGH_MAIN_NODE);
                 if (properties != null) {
                     drawNeighbour(g, neiName, (Node)properties, lineColor, nodesMap);
                 }
             }
             System.out.println("Network renderer took " + ((System.currentTimeMillis() - startTime) / 1000.0) + "s to draw " + count + " sites from "+neoGeoResource.getIdentifier());
             tx.success();
         } catch (TransformException e) {
             throw new RenderException(e);
         } catch (FactoryException e) {
             throw new RenderException(e);
         } catch (IOException e) {
             throw new RenderException(e); // rethrow any exceptions encountered
         } finally {
             if (neoGeoResource != null) {
                 HashMap<Long,Point> idMap = new HashMap<Long,Point>();
                 for(Node node:nodesMap.keySet()){
                     idMap.put(node.getId(), nodesMap.get(node));
                 }
                 getContext().getMap().getBlackboard().put(BLACKBOARD_NODE_LIST, idMap);
             }
             // if (geoNeo != null)
             // geoNeo.close();
             monitor.done();
             tx.finish();
         }
     }
 
     @SuppressWarnings("unchecked")
     private Pair<Point, Long> getStarPoint() {
         Pair<Point,Long> starPoint = (Pair<Point,Long>)getContext().getLayer().getBlackboard().get(BLACKBOARD_START_ANALYSER);
         return starPoint;
     }
 
     private void drawLabel(Graphics2D g, Point p, String drawString) {
         TextLayout text = new TextLayout(drawString, g.getFont(), g.getFontRenderContext());
         AffineTransform at = AffineTransform.getTranslateInstance(p.x, p.y);
         Shape outline = text.getOutline(at);
         drawSoftSurround(g, outline);
         g.setPaint(surroundColor);
         g.fill(outline);
         g.draw(outline);
         g.setPaint(labelColor);
         text.draw(g, p.x, p.y);
     }
 
     private boolean findNonOverlapPosition(Set<Rectangle> labelRec, int hgt, Point p, Rectangle rect) {
         boolean drawLabel = true;
         if (!labelSafe(rect, labelRec)) {
             drawLabel = false;
             Rectangle tryRect = new Rectangle(rect);
             RECT: for (int tries : new int[] {1, -1}) {
                 for (int shift = 0; shift != tries * (2 * hgt); shift += tries) {
                     tryRect.setLocation(rect.x, rect.y + shift);
                     if (labelSafe(tryRect,labelRec)) {
                         drawLabel = true;
                         p.y = p.y + shift;
                         rect.setLocation(tryRect.getLocation());
                         break RECT;
                     }
                 }
             }
         }
         return drawLabel;
     }
 
     private double getDouble(Node node, String property, double def) {
         Object result = node.getProperty(property, def);
         if (result instanceof Integer) {
             return ((Integer)result).doubleValue();
         } else if (result instanceof Float) {
             return ((Float)result).doubleValue();
         } else if (result instanceof String) {
             return Double.parseDouble((String)result);
         } else {
             return (Double)result;
         }
     }
     
     /**
      * @param sector
      * @return
      */
     private String getSectorName(Node sector) {
         return sector.getProperty(sectorName, "").toString();
     }
 
     /**
      * Gets Site name
      * 
      * @param node geo node
      * @return site name
      */
     private String getSiteName(GeoNode node) {
         return normalSiteName ? node.toString() : node.getNode().getProperty(siteName, node.toString()).toString();
     }
 
     private void drawSoftSurround(Graphics2D g, Shape outline) {
         g.setPaint(new Color(surroundColor.getRed(),surroundColor.getGreen(),surroundColor.getBlue(),128));
         g.translate( 1, 0); g.fill(outline);g.draw(outline);
         g.translate(-1, 1); g.fill(outline);g.draw(outline);
         g.translate(-1,-1); g.fill(outline);g.draw(outline);
         g.translate( 1,-1); g.fill(outline);g.draw(outline);
         g.translate( 0, 1);
     }
 
     private boolean labelSafe(Rectangle rect, Set<Rectangle> labelRectangles) {
         for (Rectangle rectangle : labelRectangles) {
             if (rectangle.intersects(rect)) {
                 return false;
             }
         }
         return true;
     }
     
     /**
      * draws neighbour relations
      * 
      * @param g Graphics2D
      * @param neiName name of neighbour list
      * @param node serve node
      * @param lineColor - line color
      * @param nodesMap map of nodes
      */
     private void drawNeighbour(Graphics2D g, String neiName, Node node, Color lineColor, Map<Node, Point> nodesMap) {
         g.setColor(lineColor);
         Point point1 = nodesMap.get(node);
         if (point1 != null) {
             for (Relationship relation : NeoUtils.getNeighbourRelations(node, neiName)) {
                 Point point2 = nodesMap.get(relation.getOtherNode(node));
                 if (point2 != null) {
                     g.drawLine(point1.x, point1.y, point2.x, point2.y);
                 }
             }
         }
     }
 
     /**
      * draws neighbour relation
      * 
      * @param g Graphics2D
      * @param relation relation
      * @param lineColor - line color
      * @param nodesMap map of nodes
      */
     private void drawRelation(Graphics2D g, Relationship relation, Color lineColor, Map<Node, Point> nodesMap) {
         g.setColor(lineColor);
         Point point1 = nodesMap.get(relation.getStartNode());
         Point point2 = nodesMap.get(relation.getEndNode());
         if (point1 != null && point2 != null) {
             g.drawLine(point1.x, point1.y, point2.x, point2.y);
         }
     }
 
     /**
      * gets average count of geoNeo.getCount() from all resources in map
      * 
      * @return average count
      */
     private Long getAverageCount(IProgressMonitor monitor) {
         long result = 0;
         long count = 0;
         try {
             for (ILayer layer : getContext().getMap().getMapLayers()) {
                 if (layer.getGeoResource().canResolve(GeoNeo.class)) {
                     GeoNeo resource = layer.getGeoResource().resolve(GeoNeo.class, monitor);
                     if(resource.getGisType().equals(GisTypes.NETWORK)) {
                         result += resource.getCount();
                         count++;
                     }
                 }
             }
         } catch (IOException e) {
             // TODO Handle IOException
             NeoCorePlugin.error(e.getLocalizedMessage(), e);
             return null;
         }
         return count == 0 ? null : result / count;
     }
 
     /**
      * gets sector color
      * 
      * @param child - sector node
      * @param defColor - default value
      * @return color
      */
     private Color getSectorColor(Node node, Color defColor) {
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
 
     /**
      * Render the sector symbols based on the point and azimuth. We simply save the graphics
      * transform, then modify the graphics through the appropriate transformations (origin to site,
      * and rotations for drawing the lines and arcs).
      * 
      * @param g
      * @param p
      * @param azimuth
      */
     private Pair<java.awt.Point, java.awt.Point> renderSector(Graphics2D g, java.awt.Point p, double azimuth, double beamwidth,
             Color fillColor,
             Color borderColor, int drawSize) {
         Color oldColor = g.getColor();
         Pair<java.awt.Point, java.awt.Point> result = null;
         if(base_transform==null) base_transform = g.getTransform();
         if(beamwidth<10) beamwidth = 10;
         g.setTransform(base_transform);
         g.translate(p.x, p.y);
         int draw2 = drawSize + 3;
         if (beamwidth >= 360.0) {
             g.setColor(fillColor);
             g.fillOval(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize);
             g.setColor(borderColor);
             g.drawOval(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize);
             result = new Pair<java.awt.Point, java.awt.Point>(p, new java.awt.Point(p.x + draw2, p.y));
 
         } else {
             double angdeg = -90 + azimuth - beamwidth / 2.0;
             g.rotate(Math.toRadians(angdeg));
             g.setColor(fillColor);
             g.fillArc(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize, 0, -(int)beamwidth);
             // TODO correct gets point
 
             g.setColor(borderColor);
             g.drawArc(-drawSize, -drawSize, 2 * drawSize, 2 * drawSize, 0, -(int)beamwidth);
             g.drawLine(0, 0, drawSize, 0);
             g.rotate(Math.toRadians(beamwidth / 2));
             double xLoc = drawSize / 2;
             double yLoc = 0;
             AffineTransform transform = g.getTransform();
             int x = (int)(transform.getScaleX() * xLoc + transform.getShearX() * yLoc + transform.getTranslateX());
             int y = (int)(transform.getShearY() * xLoc + transform.getScaleY() * yLoc + transform.getTranslateY());
 
             int x2 = (int)(transform.getScaleX() * draw2 + transform.getShearX() * yLoc + transform.getTranslateX());
             int y2 = (int)(transform.getShearY() * draw2 + transform.getScaleY() * yLoc + transform.getTranslateY());
 
             g.rotate(Math.toRadians(beamwidth / 2));
             g.drawLine(0, 0, drawSize, 0);
             Point result1 = new java.awt.Point(x, y);
             Point result2 = new java.awt.Point(x2, y2);
             result = new Pair<java.awt.Point, java.awt.Point>(result1, result2);
 
             g.setColor(oldColor);
         }
         return result;
     }
 
     /**
      * This one is very simple, just draw a circle at the site location.
      * 
      * @param g
      * @param p
      * @param borderColor
      * @param drawSize 
      */
     private void renderSite(Graphics2D g, java.awt.Point p, Color borderColor, Color fillColor, int drawSize, boolean drawFull, boolean drawLite, boolean selected) {
         Color oldColor = g.getColor();
         if (drawFull) {
             if(selected) renderSelectionGlow(g, p, drawSize * 4);
             drawSize /= 4;
             if (drawSize < 2) drawSize = 2;
             g.setColor(fillColor);
             g.fillOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
             g.setColor(borderColor);
             g.drawOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
         } else if (drawLite) {
             if(selected) renderSelectionGlow(g, p, 20);
             g.setColor(borderColor);
             g.drawOval(p.x - 5, p.y - 5, 10, 10);
         } else {
             if(selected) renderSelectionGlow(g, p, 20);
             g.setColor(borderColor);
             g.drawRect(p.x - 1, p.y - 1, 3, 3);
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
         Color highColor = new Color(COLOR_SITE_SELECTED.getRed(), COLOR_SITE_SELECTED.getGreen(), COLOR_SITE_SELECTED.getBlue(), 8);
         g.setColor(highColor);
         for(;drawSize > 2; drawSize *= 0.8) {
             g.fillOval(p.x - drawSize, p.y - drawSize, 2 * drawSize, 2 * drawSize);
         }
     }
 
     /**
      * perform and draw star analyser
      * 
      * @param context context
      */
     private void drawAnalyser(Graphics2D g, Node mainNode, Point starPoint, String property, Map<Node, Point> nodesMap) {
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             if (aggNode == null) {
                 return;
             }
             Node chart = NeoUtils.getChartNode(mainNode, aggNode);
             if (chart == null) {
                 return;
             }
             Point point = nodesMap.get(mainNode);
             if (point != null) {
                 starPoint = point;
             }
             drawMainNode(g, mainNode, starPoint);
             for (Relationship relation : chart.getRelationships(NetworkRelationshipTypes.AGGREGATE, Direction.OUTGOING)) {
                 Node node = relation.getOtherNode(chart);
                 Point nodePoint = nodesMap.get(node);
                 if (nodePoint != null) {
                     g.setColor(getLineColor(mainNode, node));
                     g.drawLine(starPoint.x, starPoint.y, nodePoint.x, nodePoint.y);
                 }
             }
 
         } finally {
             tx.finish();
         }
 
     }
 
     /**
      * get Line color
      * 
      * @param mainNiode main node
      * @param node node
      * @return Line color
      */
     private Color getLineColor(Node mainNiode, Node node) {
         return Color.BLACK;
     }
 
     /**
      * get property to analyze
      * @param geoNeo 
      * 
      * @return property name
      */
     private String getSelectProperty(GeoNeo geoNeo) {
         String result = null;
         Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             result = geoNeo.getProperty(INeoConstants.PROPERTY_SELECTED_AGGREGATION, "").toString();
             transaction.success();
         } finally {
             transaction.finish();
         }
         return result == null || result.isEmpty() ? null : result;
     }
 
     /**
      * Draw selection of main node
      * 
      * @param context context
      * @param mainNiode main node
      */
     private void drawMainNode(Graphics2D g, Node mainNode, Point point) {
         g.setColor(Color.RED);
         g.fillOval(point.x - 4, point.y - 4, 10, 10);
     }
 
     @Override
     public void render( IProgressMonitor monitor ) throws RenderException {
         Graphics2D g = getContext().getImage().createGraphics();
         render(g, monitor);
     }
 
 }
