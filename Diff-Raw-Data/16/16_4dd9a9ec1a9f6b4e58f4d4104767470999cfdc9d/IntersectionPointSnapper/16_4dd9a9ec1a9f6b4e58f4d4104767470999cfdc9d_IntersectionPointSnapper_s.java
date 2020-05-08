 package com.iver.cit.gvsig.project.documents.view.snapping.snappers;
 
 import java.awt.Graphics;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.core.FArc2D;
 import com.iver.cit.gvsig.fmap.core.FCircle2D;
 import com.iver.cit.gvsig.fmap.core.FEllipse2D;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.FSpline2D;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.project.documents.view.snapping.AbstractSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapperGeometriesVectorial;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.LineSegment;
 
 
 /**
  * Intersection point snapper.
  *
  * @author Vicente Caballero Navarro
  */
 public class IntersectionPointSnapper extends AbstractSnapper
     implements ISnapperGeometriesVectorial {
     private List geometries=new ArrayList<IGeometry>();
     public IntersectionPointSnapper() {
         System.err.println("Construido IntersectionPoinSnapper");
     }
 
     /* (non-Javadoc)
      * @see com.iver.cit.gvsig.gui.cad.snapping.ISnapper#getSnapPoint(Point2D point,
      * IGeometry geom,double tolerance, Point2D lastPointEntered)
      */
     public Point2D getSnapPoint(Point2D point, IGeometry geom,
         double tolerance, Point2D lastPointEntered) {
    	if (geom.getInternalShape() instanceof FCircle2D ||
    			geom.getInternalShape() instanceof FArc2D ||
    			geom.getInternalShape() instanceof FEllipse2D ||
    			geom.getInternalShape() instanceof FSpline2D){
     				return null;
     	}
     	Point2D result = null;
     	geometries.add(geom);
 //        if (geometries == null) {
 //            return null;
 //        }
 
         for (int i = 0; i < geometries.size(); i++) {
         	IGeometry auxGeom=(IGeometry)geometries.get(i);
         	if (!geom.equals(auxGeom)){
         		Point2D r = intersects2(geom, auxGeom, point, tolerance);
             	if (r != null) {
             		geometries.clear();
             		return r;
 //                	result = r;
             	}
         	}
         }
         if (geometries.size()>5)
         	geometries.clear();
         return result;
     }
     private Point2D intersects2(IGeometry g1, IGeometry g2, Point2D point,
             double tolerance) {
     	Geometry intersection=g1.toJTSGeometry().intersection(g2.toJTSGeometry());
     	IGeometry g=FConverter.jts_to_igeometry(intersection);
     	if (g!=null && g.getGeometryType()==FShape.POINT){
     		return g.getHandlers(IGeometry.SELECTHANDLER)[0].getPoint();
     	}
     	return null;
     }
 
 
     /**
      * DOCUMENT ME!
      *
      * @param g1 DOCUMENT ME!
      * @param g2 DOCUMENT ME!
      * @param point DOCUMENT ME!
      * @param tolerance DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private Point2D intersects(IGeometry g1, IGeometry g2, Point2D point,
         double tolerance) {
         Point2D resul = null;
         Coordinate c = new Coordinate(point.getX(), point.getY());
         PathIterator theIterator = g1.getPathIterator(null, FConverter.FLATNESS);
         double[] theData = new double[6];
         Coordinate from = null;
         Coordinate first = null;
         LineSegment[] lines = getLines(g2);
         while (!theIterator.isDone()) {
         	int theType = theIterator.currentSegment(theData);
 
             switch (theType) {
             case PathIterator.SEG_MOVETO:
                 from = new Coordinate(theData[0], theData[1]);
                 first = from;
 
                 break;
 
             case PathIterator.SEG_LINETO:
 
                 Coordinate to = new Coordinate(theData[0], theData[1]);
                 LineSegment segmentLine = new LineSegment(from,to);
                 for (int i = 0; i < lines.length; i++) {
 //                    if (lines[i].equals(segmentLine)) {
 //                        continue;
 //                    }
                     Coordinate intersects = segmentLine.intersection(lines[i]);
                     if (intersects == null || lines[i].equals(segmentLine)) {
                         continue;
                     }
 
                     double dist = c.distance(intersects);
 
                     if ((dist < tolerance)) {
                         resul = new Point2D.Double(intersects.x, intersects.y);
                         return resul;
                     }
                 }
 
                 from = to;
 
                 break;
 
             case PathIterator.SEG_CLOSE:
             	 LineSegment segment = new LineSegment(from,first);
 
             	for (int i = 0; i < lines.length; i++) {
 //                    if (lines[i].equals(segment)) {
 //                        continue;
 //                    }
 
                     Coordinate intersects = segment.intersection(lines[i]);
 
                     if (intersects == null) {
                         continue;
                     }
 
                     double dist = c.distance(intersects);
 
                     if ((dist < tolerance)) {
                         resul = new Point2D.Double(intersects.x, intersects.y);
                         return resul;
                     }
                 }
 
                 from = first;
 
                 break;
             } //end switch
 
             theIterator.next();
         }
         return resul;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param g DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private LineSegment[] getLines(IGeometry g) {
         ArrayList lines = new ArrayList();
         PathIterator theIterator = g.getPathIterator(null, FConverter.FLATNESS);
         double[] theData = new double[6];
         Coordinate from = null;
         Coordinate first = null;
 
         while (!theIterator.isDone()) {
             //while not done
             int theType = theIterator.currentSegment(theData);
 
             switch (theType) {
             case PathIterator.SEG_MOVETO:
                 from = new Coordinate(theData[0], theData[1]);
                 first = from;
 
                 break;
 
             case PathIterator.SEG_LINETO:
 
                 Coordinate to = new Coordinate(theData[0], theData[1]);
                 LineSegment line = new LineSegment(from, to);
                 lines.add(line);
                 from = to;
 
                 break;
 
             case PathIterator.SEG_CLOSE:
                 line = new LineSegment(from, first);
                 lines.add(line);
                 from = first;
 
                 break;
             } //end switch
 
             theIterator.next();
         }
 
         return (LineSegment[]) lines.toArray(new LineSegment[0]);
     }
 
     /* (non-Javadoc)
      * @see com.iver.cit.gvsig.gui.cad.snapping.ISnapper#draw(java.awt.Graphics, java.awt.geom.Point2D)
      */
     public void draw(Graphics g, Point2D pPixels) {
         g.setColor(getColor());
 
         int half = getSizePixels() / 2;
         int x1 = (int) (pPixels.getX() - half);
         int x2 = (int) (pPixels.getX() + half);
         int y1 = (int) (pPixels.getY() - half);
         int y2 = (int) (pPixels.getY() + half);
 
         g.drawLine(x1, y1, x2, y2);
         g.drawLine(x1, y2, x2, y1);
     }
     /**
      * DOCUMENT ME!
      *
      * @param geoms DOCUMENT ME!
      */
     public void setGeometries(List geoms) {
         this.geometries = geoms;
     }
 
     /* (non-Javadoc)
      * @see com.iver.cit.gvsig.gui.cad.snapping.ISnapper#getToolTipText()
      */
     public String getToolTipText() {
         return PluginServices.getText(this, "intersection_point");
     }
 }
