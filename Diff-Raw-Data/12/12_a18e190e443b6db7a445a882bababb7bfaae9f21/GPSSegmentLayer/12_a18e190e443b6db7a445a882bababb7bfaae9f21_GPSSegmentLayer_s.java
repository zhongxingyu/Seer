 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.fub.agg2graphui.layers;
 
 import com.infomatiq.jsi.rtree.RTree;
 import de.fub.agg2graph.structs.GPSPoint;
 import de.fub.agg2graph.structs.GPSSegment;
 import de.fub.agg2graph.structs.ILocation;
 import de.fub.agg2graph.ui.gui.RenderingOptions;
 import de.fub.agg2graphui.controller.AbstractLayer;
 import de.fub.mapviewer.ui.MapViewer;
 import gnu.trove.TIntProcedure;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.Area;
 import java.awt.geom.Rectangle2D;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Properties;
 import org.openstreetmap.gui.jmapviewer.Coordinate;
 
 /**
  *
  * @author Serdar
  */
 public class GPSSegmentLayer extends AbstractLayer<GPSSegment> {
 
     private RTree rtree;
     private final HashMap<Integer, GPSSegment> META_DATA = new HashMap<Integer, GPSSegment>();
 
     public GPSSegmentLayer(String name, RenderingOptions renderingOptions) {
         super(name, renderingOptions);
         reset();
     }
 
     public GPSSegmentLayer(String name, String description, RenderingOptions renderingOptions) {
         super(name, description, renderingOptions);
         reset();
     }
 
     private void reset() {
         META_DATA.clear();
         Properties p = new Properties();
         p.setProperty("MinNodeEntries", "10");
         p.setProperty("MaxNodeEntries", "50");
         rtree = new RTree();
         rtree.init(p);
     }
 
     @Override
     public void add(GPSSegment item) {
         super.add(item); //To change body of generated methods, choose Tools | Templates.
         if (!META_DATA.containsKey(item.hashCode())) {
             com.infomatiq.jsi.Rectangle boundingBox = getBoundingBox(item);
             rtree.add(boundingBox, item.hashCode());
             META_DATA.put(item.hashCode(), item);
         }
     }
 
     @Override
     public void addAll(Collection<GPSSegment> items) {
         super.addAll(items);
         for (GPSSegment item : items) {
             if (!META_DATA.containsKey(item.hashCode())) {
                 com.infomatiq.jsi.Rectangle boundingBox = getBoundingBox(item);
                 rtree.add(boundingBox, item.hashCode());
                 META_DATA.put(item.hashCode(), item);
             }
         }
     }
 
     @Override
     public void remove(GPSSegment item) {
         super.remove(item);
         if (META_DATA.containsKey(item.hashCode())) {
             com.infomatiq.jsi.Rectangle boundingBox = getBoundingBox(item);
             rtree.delete(boundingBox, item.hashCode());
         }
     }
 
     @Override
     public void clearRenderObjects() {
         super.clearRenderObjects(); //To change body of generated methods, choose Tools | Templates.
         reset();
     }
 
     @Override
     protected void drawDrawables(Graphics2D graphics, Rectangle rectangle) {
         MapViewer mapViewer = getLayerManager().getMapViewer();
         Dimension size = mapViewer.getSize();
         Coordinate position = mapViewer.getPosition(0, 0);
         Coordinate position1 = mapViewer.getPosition(size.width, size.height);
        com.infomatiq.jsi.Rectangle rectangle1 = new com.infomatiq.jsi.Rectangle(
                (float) position.getLat(),
                 (float) position.getLon(),
                 (float) position1.getLat(),
                 (float) position1.getLon());
         rtree.intersects(rectangle1, new GPSSegmentSeachProcedure());
     }
 
     private com.infomatiq.jsi.Rectangle getBoundingBox(GPSSegment segment) {
         Area area = new Area();
         for (ILocation location : segment) {
             // the added shape must have at least a size > 0
             area.add(new Area(new Rectangle2D.Double(location.getLon(), location.getLat(), 0.0000000001, 0.0000000001)));
         }
         Rectangle2D boundingBox = area.getBounds2D();
         float minLong = (float) boundingBox.getMinX();
         float maxLat = (float) boundingBox.getMinY();
         float maxLong = (float) boundingBox.getMaxX();
         float minLat = (float) boundingBox.getMaxY();
        com.infomatiq.jsi.Rectangle rectangle = new com.infomatiq.jsi.Rectangle(minLong, maxLat, maxLong, minLat);
         return rectangle;
     }
 
     private class GPSSegmentSeachProcedure implements TIntProcedure {
 
         private HashSet<GPSSegment> segments = new HashSet<GPSSegment>(100);
 
         public GPSSegmentSeachProcedure() {
         }
 
         @Override
         public boolean execute(int value) {
             GPSSegment segment = META_DATA.get(value);
             GPSPoint lastPoint = null;
             for (GPSPoint point : segment) {
                 if (getLayerManager().getMapViewer().getMapPosition(point.getLat(), point.getLon()) != null) {
                     if (lastPoint != null) {
 
                         drawLine(lastPoint, point, getRenderingOptions());
                     }
                     drawPoint(point, getRenderingOptions());
                 }
                 lastPoint = point;
             }
             return true;
         }
 
         public Collection<GPSSegment> getSegments() {
             return segments;
         }
     }
 }
