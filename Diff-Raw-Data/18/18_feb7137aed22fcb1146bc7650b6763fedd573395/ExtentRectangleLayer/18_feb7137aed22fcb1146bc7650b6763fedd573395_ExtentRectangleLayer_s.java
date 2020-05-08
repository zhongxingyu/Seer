 /*
  * ExtentRectangleLayer.java
  *
  * Created on September 11, 2007, 2:55 PM
  *
  * Applied Science Associates, Inc.
  * Copyright 2007.  All rights reserved.
  */
 package com.asascience.openmap.layer;
 
 import java.awt.Container;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import ucar.unidata.geoloc.LatLonRect;
 
 import com.asascience.openmap.mousemode.InformationMouseMode;
 import com.bbn.openmap.LatLonPoint;
 import com.bbn.openmap.event.MapMouseListener;
 import com.bbn.openmap.layer.OMGraphicHandlerLayer;
 import com.bbn.openmap.omGraphics.DrawingAttributes;
 import com.bbn.openmap.omGraphics.OMGraphic;
 import com.bbn.openmap.omGraphics.OMGraphicList;
 import com.bbn.openmap.omGraphics.OMRect;
 
 /**
  * 
  * @author CBM
  */
 public class ExtentRectangleLayer extends OMGraphicHandlerLayer implements MapMouseListener, ActionListener {
 
   private String name;
   private OMGraphicList omgraphics;
   private OMGraphic selectedGraphic;
   private LatLonRect extentRect;
   protected DrawingAttributes drawingAttributes;
   private boolean allowMulti;
 
   /**
    * Creates a new instance of ExtentRectangleLayer
    *
    * @param name
    * @param allowMulti
    */
   public ExtentRectangleLayer(String name, boolean allowMulti) {
     this.name = name;
     setAllowMulti(allowMulti);
     omgraphics = new OMGraphicList();
     this.setList(omgraphics);
   }
 
   public void setAllowMulti(boolean allowMulti) {
     this.allowMulti = allowMulti;
   }
 
   public boolean isAllowMulti() {
     return allowMulti;
   }
 
   /**
    *
    * @param prefix
    * @param props
    */
   @Override
   public void setProperties(String prefix, Properties props) {
     super.setProperties(prefix, props);
 
     drawingAttributes = new DrawingAttributes(prefix, props);
   }
 
   public void setDrawingAttributes(DrawingAttributes da) {
     drawingAttributes = da;
   }
 
   public DrawingAttributes getDrawingAttributes() {
     return drawingAttributes;
   }
 
   public LatLonRect getExtentRectangle() {
     return extentRect;
   }
 
   public LatLonPoint getExtentCenterPoint() {
     LatLonPoint llp = new LatLonPoint();
     double clon = this.getExtentRectangle().getCenterLon();
     double clat = (this.getExtentRectangle().getLatMax() + this.getExtentRectangle().getLatMin()) / 2;
 
     llp.setLatLon((float) clat, (float) clon);
 
     return llp;
   }
 
   public void addExtentRectangle(LatLonRect llr) {
     // set the currentExtentRectangle
     extentRect = llr;
 
     // see the OpenMap "Restrictions" on OMPolygons and OMLines
     if (llr.getWidth() >= 180 & llr.getWidth() < 355) {
       if (getList() != null && getList().size() > 0) {
         getList().clear();
       }
      System.err.println("Extent too big for single rectangle:");
      System.err.println("  orig: " + llr.toString2());
       List<LatLonRect> rects = quarterExtent(llr);
       // List<LatLonRect> rects = halveExtent(llr);
       allowMulti = true;// temporarily allow multiple rectangles so that
       // the user-drawn rectangle can be sufficiently
       // large
       int q = 1;
       for (Iterator i = rects.iterator(); i.hasNext();) {
         llr = (LatLonRect) i.next();
        System.err.println("  quad " + q + ":" + llr.toString2());
         createRectangle(llr.getLatMax(), llr.getLonMin(), llr.getLatMin(), llr.getLonMax());
         q++;
       }
       allowMulti = false;// turn allow multiple rectangles back off
     } else if (llr.getWidth() >= 355) {// when close to "full" global
       // coverage
       if (getList() != null && getList().size() > 0) {
         getList().clear();
       }
      System.err.println("Extent nearly global:");
      System.err.println("  orig: " + llr.toString2());
       List<LatLonRect> rects = quarterExtent(llr);
       // List<LatLonRect> rects = halveExtent(llr);
       allowMulti = true;// temporarily allow multiple rectangles so that
       // the user-drawn rectangle can be sufficiently
       // large
       int q = 1, sub = 1;
       LatLonRect llr2;
       List<LatLonRect> rects2;
       LatLonRect llr3;
       for (Iterator i = rects.iterator(); i.hasNext();) {
         llr2 = (LatLonRect) i.next();
        System.err.println("  quad " + q + ":" + llr2.toString2());
         rects2 = quarterExtent(llr2);
         sub = 1;
         for (Iterator i2 = rects2.iterator(); i2.hasNext();) {
           llr3 = (LatLonRect) i2.next();
          System.err.println("    sub-quad " + q + "-" + sub + ":" + llr3.toString2());
           createRectangle(llr3.getLatMax(), llr3.getLonMin(), llr3.getLatMin(), llr3.getLonMax());
           sub++;
         }
         q++;
       }
       allowMulti = false;// turn allow multiple rectangles back off
     } else {
       createRectangle(llr.getLatMax(), llr.getLonMin(), llr.getLatMin(), llr.getLonMax());
     }
   }
 
   private List<LatLonRect> halveExtent(LatLonRect llr) {
     List<LatLonRect> llrects = new ArrayList();
 
     double dLon, corr, width, height;
     // corr = 0.01;
     corr = 0;
     width = llr.getWidth();
     height = llr.getLatMax() - llr.getLatMin();
 
     dLon = (width / 2) - corr;
 
     LatLonRect llr1 = new LatLonRect(llr.getUpperRightPoint(), -height, dLon);
     LatLonRect llr2 = new LatLonRect(llr.getUpperRightPoint(), -height, -dLon);
     llrects.add(llr1);
     llrects.add(llr2);
 
     return llrects;
   }
 
   private List<LatLonRect> quarterExtent(LatLonRect llr) {
     List<LatLonRect> llrects = new ArrayList();
 
     double dLat, dLon, corr, width, height;
     corr = 0;// helps with the funkyness when a global coverage is displayed
     // corr = 0;
     width = llr.getWidth();
     height = llr.getLatMax() - llr.getLatMin();
 
     dLon = (width / 2) - corr;
     dLat = (height / 2) - corr;
 
     /**
      * All calculations are made from the given corner towards the center
      * need to account for when it crosses the map seam (180)
      */
     // System.err.println("cross?="+llr.crossDateline());
     LatLonRect llr1, llr2, llr3, llr4;
     // if(!llr.crossDateline()){
     llr1 = new LatLonRect(llr.getUpperLeftPoint(), -dLat, dLon);// the
     // upper-left
     // quadrant
     llrects.add(llr1);
     llr2 = new LatLonRect(llr.getUpperRightPoint(), -dLat, -dLon);// upper-right
     // quadrant
     llrects.add(llr2);
     llr3 = new LatLonRect(llr.getLowerRightPoint(), dLat, -dLon);// lower-right
     // quadrant
     llrects.add(llr3);
     llr4 = new LatLonRect(llr.getLowerLeftPoint(), dLat, dLon);// lower-left
     // quadrant
     llrects.add(llr4);
     // }else{
     // llr1 = new LatLonRect(llr.getUpperLeftPoint(), -dLat, -dLon);//the
     // upper-left quadrant
     // llrects.add(llr1);
     // llr2 = new LatLonRect(llr.getUpperRightPoint(), -dLat,
     // dLon);//upper-right quadrant
     // llrects.add(llr2);
     // llr3 = new LatLonRect(llr.getLowerRightPoint(), dLat,
     // dLon);//lower-right quadrant
     // llrects.add(llr3);
     // llr4 = new LatLonRect(llr.getLowerLeftPoint(), dLat,
     // -dLon);//lower-left quadrant
     // llrects.add(llr4);
     // }
 
     return llrects;
   }
 
   public void addExtentRectangle(LatLonPoint upperLeft, LatLonPoint lowerRight) {
     createRectangle(upperLeft.getLatitude(), lowerRight.getLongitude(), lowerRight.getLatitude(), upperLeft.getLongitude());
   }
 
   public void addExtentRectangle(float northernExtent, float easternExtent, float southernExtent, float westernExtent) {
     createRectangle(northernExtent, easternExtent, southernExtent, westernExtent);
   }
 
   private void createDrawing(int y1, int x1, int y2, int x2) {
     OMRect rect = new OMRect(x1, y1, x2, y2);
     rect.setAppObject(name);
     OMGraphicList omgl = this.getList();
     if (!allowMulti) {
       omgl.clear();
     }
     omgl.addOMGraphic(rect);
 
     this.doPrepare();
   }
 
   private void createRectangle(double lat1, double lon1, double lat2, double lon2) {
     // /**
     // System.err.println("lon1=" + lon1 + "  lon2=" + lon2);
     // if(lon2 > lon1){
     // float tmp = lon1;
     // lon1 = lon2;
     // lon2 = tmp;
     // }
     // if((lon1 > 180 | lon2 > 180) & !(lon1 < 0 | lon2 < 0)){
     // System.err.println("inside fix");
     // float tmp = lon1;
     // // lon1 = lon2;
     // // lon2 = tmp;
     // lon1 -= 180;
     // lon2 -= 180;
     // }
     // */
     OMRect rect = new OMRect((float) lat1, (float) lon1, (float) lat2, (float) lon2, OMGraphic.LINETYPE_RHUMB);
     rect.setAppObject(name);
     OMGraphicList omgl = this.getList();
     if (!allowMulti) {
       if (omgl == null) {
         omgl = new OMGraphicList();
         this.setList(omgl);
       }
       omgl.clear();
     }
 
     omgl.addOMGraphic(rect);
 
     this.doPrepare();
   }
 
   /**
    *
    * @return
    */
   @Override
   public OMGraphicList prepare() {
     OMGraphicList list = this.getList();
     OMGraphic g;
     for (int i = 0; i < list.size(); i++) {
       g = list.getOMGraphicAt(i);
       g.setLinePaint(drawingAttributes.getLinePaint());
       g.setFillPaint(drawingAttributes.getFillPaint());
       g.setSelectPaint(drawingAttributes.getSelectPaint());
     }
     list.generate(this.getProjection());
     return list;
   }
 
   /**
    *
    * @return
    */
   @Override
   public MapMouseListener getMapMouseListener() {
     return this;
   }
 
   public String[] getMouseModeServiceList() {
     String[] ret = new String[1];
     // ret[0] = SelectMouseMode.modeID;
     ret[0] = InformationMouseMode.modeID;
     return ret;
   }
 
   public boolean mousePressed(MouseEvent e) {
     return false;
   }
 
   public boolean mouseReleased(MouseEvent e) {
     return false;
   }
 
   public boolean mouseClicked(MouseEvent e) {
     if (selectedGraphic != null) {
       String n = (String) selectedGraphic.getAppObject();
       if (n != null) {
         // this.fireRequestInfoLine("Layer Name: " + n);
       }
     }
     return false;
   }
 
   public void mouseEntered(MouseEvent e) {
   }
 
   public void mouseExited(MouseEvent e) {
   }
 
   public boolean mouseDragged(MouseEvent e) {
     return false;
   }
 
   public boolean mouseMoved(MouseEvent e) {
     OMGraphic newSelGraphic = this.getList().selectClosest(e.getX(), e.getY(), 5.0f);
     if (newSelGraphic == null) {
       this.fireRequestInfoLine("");
       repaint();
     } else {// if(newSelGraphic != selectedGraphic){
       selectedGraphic = newSelGraphic;
       this.fireRequestInfoLine("Layer Name: " + (String) selectedGraphic.getAppObject());
       repaint();
     }
     return true;
   }
 
   public void mouseMoved() {
     // this.getList().deselectAll();
     // repaint();
   }
 
   /**
    * Called when the Layer is removed from the MapBean, giving an opportunity
    * to clean up.
    *
    * @param cont
    */
   @Override
   public void removed(Container cont) {
     OMGraphicList list = this.getList();
     if (list != null) {
       list.clear();
       list = null;
     }
   }
 }
