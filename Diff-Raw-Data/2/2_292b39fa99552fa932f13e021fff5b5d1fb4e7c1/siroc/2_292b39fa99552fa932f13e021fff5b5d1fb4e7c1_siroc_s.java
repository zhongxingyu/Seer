 import processing.core.*; import controlP5.*; import geomerative.*; import name.reid.mark.geovex.*; import java.util.Vector; import java.applet.*; import java.awt.*; import java.awt.image.*; import java.awt.event.*; import java.io.*; import java.net.*; import java.text.*; import java.util.*; import java.util.zip.*; import javax.sound.midi.*; import javax.sound.midi.spi.*; import javax.sound.sampled.*; import javax.sound.sampled.spi.*; import java.util.regex.*; import javax.xml.parsers.*; import javax.xml.transform.*; import javax.xml.transform.dom.*; import javax.xml.transform.sax.*; import javax.xml.transform.stream.*; import org.xml.sax.*; import org.xml.sax.ext.*; import org.xml.sax.helpers.*; public class siroc extends PApplet {
 
 
 
 /**
  * <p>
  * Visualisation of the relationship between Statistical Information
  * and ROC curves.
  * </p><p>
  * The left graph shows curves on a Statistical Information plot.
  * The right graph shows the same curves converted to an ROC representation
  * with \u03c0 = 0.5.
  * </p><p>
  * Move your mouse over the S.I. window to see the corresponding line in the
  * ROC graph and <i>vice versa</i>.
  * </p>
  * <p>
  * The duality relationships are computed using the 
 * <a href="http://gihub.com/mreid/geovex">geovex</a> Java library while the visualisation
  * is done with the standard Processing library and the 
  * <a href="http://www.ricardmarxer.com/processing/geomerative/documentation/">geomerative</a> 
  * library.
  * </p>
  */
 PlotView rocView = new PlotView(0.0f, 0.0f, 1.0f, 1.0f);
 PlotView siView  = new PlotView(0.0f, 0.0f, 1.0f, 0.5f);
 
 SIROCConverter sirocConvert = new SIROCConverter();
 ROCSIConverter rocsiConvert = new ROCSIConverter();
 
 SpecPoint siCursor = new SpecPoint(0.0f, 0.0f);
 SpecPoint rocCursor = new SpecPoint(0.0f, 0.0f);
 GLine rocDualCursor = new DualLine(siCursor, sirocConvert);
 GLine siDualCursor = new DualLine(rocCursor, rocsiConvert); 
 
 PFont titleFont = createFont("Arial", 16);
 PFont tickFont  = createFont("Arial", 12);
 
 float prior = 0.5f;
 
 public void setup(){
   size(700,400,JAVA2D);
   frameRate(20);
   background(255);
 
   siView.setView(30, 30, 300, 300);
   siView.setTitle("Stat. Info. (\u03c0 = 0.5)");
   
   rocView.setView(360, 30, 300, 300);
   rocView.setTitle("ROC");
 
   SpecCurve siTent = new SpecCurve();
   siTent.add(0.0f, 0.0f);
   siTent.add(0.5f, 0.25f);
   siTent.add(1.0f, 0.0f);
 
   SpecCurve si = new SpecCurve();
   si.add(0.0f, 0.0f);
   si.add(0.25f, 0.125f);
   si.add(0.5f, 0.15f);
   si.add(0.75f, 0.125f);
   si.add(1.0f, 0.0f);
 
   int grey = color(160,160,160);
   int black = color(0,0,0);
 
   siView.add(siTent, grey);
   siView.add(si, black);
 
   rocView.add(sirocConvert.toCurve(siTent), grey);
   rocView.add(sirocConvert.toCurve(si), black);  
 
   // Controls for prior value
 //  ControlP5 priorControl = new ControlP5(this);
 //  Slider s = priorControl.addSlider("priorSlider", 0.0, 1.0, 0.5, 250, 350, 200, 20);
 
   smooth();
 }
 
 /*
 void priorSlider(float value) {
   prior = value;
   sirocConvert.setPrior(prior);
   rocsiConvert.setPrior(prior);
 }
 */
 
 public void draw(){
   // Clear the screen
   background(255);
   
   siView.draw(g);
   rocView.draw(g);
 
   if(siView.active()) {
     siCursor.setX(siView.viewToModelX(mouseX));
     siCursor.setY(siView.viewToModelY(mouseY));
     
     stroke(200,0,0);
     siView.drawPoint(siCursor);
     rocView.drawLine(rocDualCursor);
   }
   
   if(rocView.active()) {
     rocCursor.setX(rocView.viewToModelX(mouseX));
     rocCursor.setY(rocView.viewToModelY(mouseY)); 
 
     stroke(0,0,200);    
     rocView.drawPoint(rocCursor);
     siView.drawLine(siDualCursor);
   }
 }
 
 
 
 class CurveView {
   GCurve curve;
   int  col;  
   CurveView(GCurve curve, int col) { this.curve = curve; this.col = col; }
 }
 
 class LineView {
   GLine line;
   int  col;  
   LineView(GLine line, int col) { this.line = line; this.col = col; }
 }
 
 class PointView {
   GPoint point;
   int  col;  
   PointView(GPoint point, int col) { this.point = point; this.col = col; }
 }
 
 class PlotView {
 
   Vector curves = new Vector();
   Vector points = new Vector();  
   Vector lines  = new Vector();
   
   String title = "[Plot Title]";
   int vxStart, vyStart;
   int vxEnd, vyEnd;
   float xStart, xEnd;
   float yStart, yEnd;
   
   GBounds bounds;
   
   PlotView(
     int vx0, int vy0, int vWidth, int vHeight,
     float x0, float y0, float x1, float y1
   ) {
     vxStart = vx0; vyStart = vy0; vxEnd = vx0 + vWidth; vyEnd = vy0 + vHeight;
     xStart = x0; yStart = y0; xEnd = x1; yEnd = y1;
     bounds = new GBounds(xStart, yStart, xEnd, yEnd);
   }
 
   PlotView(float x0, float y0, float x1, float y1) { this(0, 0, 100, 100, x0, y0, x1, y1); }
   PlotView() { this(0.0f, 0.0f, 1.0f, 1.0f); }
   
   public void add(GCurve curve, int col) { curves.add(new CurveView(curve, col)); }
   public void add(GPoint point, int col) { points.add(new PointView(point, col)); }
   public void add(GLine line, int col) { lines.add(new LineView(line, col)); }
   
   public void setTitle(String title) { this.title = title; }
   public void setViewStart(int vx, int vy) { vxStart = vx; vyStart = vy; }
   public void setViewEnd(int vx, int vy) { vxEnd = vx; vyEnd = vy; }
   public void setView(int vx0, int vy0, int vWidth, int vHeight) { 
     setViewStart(vx0, vy0); 
     setViewEnd(vx0 + vWidth, vHeight); 
   }
   
   public int viewWidth() { return vxEnd - vxStart; }
   public int viewHeight() { return vyEnd - vyStart; }
   
   public float xRange() { return xEnd - xStart; }
   public float yRange() { return yEnd - yStart; }
   
   public float xResolution() { return xRange() / viewWidth(); }
   public float yResolution() { return yRange() / viewHeight(); }
   
   public float viewToModelX(float vx) { return xStart + xRange() * (vx - vxStart) / viewWidth(); }
   public float viewToModelY(float vy) { return yStart - yRange() * (vy - vyEnd) / viewHeight(); }
   
    public boolean active() {
      return (mouseX > vxStart && mouseX < vxEnd && mouseY > vyStart && mouseY < vyEnd);
    }
     
    public void preDraw() {
      drawTitle();
      drawBounds();
      
      pushMatrix();
      
      // Set up viewing transformations
      translate(vxStart, vyStart);
      translate(-xStart, -yStart);
      scale(viewWidth()/xRange(),-viewHeight()/yRange());
      translate(0,-yEnd);
      strokeWeight(min(xResolution(), yResolution()));     
    }
    
    public void postDraw() {
      popMatrix(); 
    }
   
    public void drawTitle() {
      fill(0);
      textFont(titleFont);
      
      textAlign(CENTER, BOTTOM);
      text(title, vxStart + viewWidth()/2, vyStart - 1);
    }
   
    public void drawBounds() {
      fill(0);
      textFont(tickFont);
 
      textAlign(LEFT, TOP);
      text(nf(xStart, 1, 1), vxStart, vyEnd + 1);
 
      textAlign(RIGHT, TOP);
      text(nf(xEnd, 1, 1), vxEnd, vyEnd + 1);
      
      textAlign(RIGHT, BOTTOM);
      text(nf(yStart, 1, 1), vxStart - 1, vyEnd);     
 
      textAlign(RIGHT, TOP);
      text(nf(yEnd, 1, 1), vxStart - 1, vyStart);     
    }
   
    public void draw(PGraphics g) {
      preDraw();
      
      // Outline
      noFill();
      stroke(200);
      rect(xStart,yStart,xRange(),yRange());
 
      // Curves
      for(int i = 0 ; i < curves.size() ; i++) {
        CurveView v = (CurveView) curves.get(i);
        stroke(v.col);
        viewCurve(v.curve).draw(g);
      }
      
      // Points
      for(int i = 0 ; i < points.size() ; i++) {
        PointView v = (PointView) points.get(i);
        stroke(v.col);
        viewPoint(v.point);
      }
      
      // Lines
      for(int i = 0 ; i < lines.size() ; i++) {
        LineView v = (LineView) lines.get(i);
        stroke(v.col);
        viewLine(v.line).draw(g);
      }
      
      postDraw();
    }
    
    public void drawPoint(GPoint p) {
      preDraw();
      viewPoint(p);
      postDraw();     
    }
    
    public void drawLine(GLine l) {
      preDraw();
      viewLine(l).draw(g);
      postDraw(); 
    }
    
    public RContour viewCurve(GCurve curve) {
      RContour c = new RContour();
      for(int i = 0 ; i < curve.size() ; i++) {
        GPoint p = curve.getPoint(i);
        c.addPoint(p.getX(), p.getY()); 
      }
      
      return c;
    }
    
    public void viewPoint(GPoint point) {
      float x = point.getX();
      float y = point.getY();
      
      float xRes = 2*xResolution();
      float yRes = 2*yResolution();
      
      line(x - xRes, y - yRes, x + xRes, y + yRes);
      line(x + yRes, y - yRes, x - xRes, y + yRes);
    }
    
    public RContour viewLine(GLine line) {
      GSegment s = bounds.clip(line);
      RContour l = new RContour();
      l.addPoint(s.start.getX(), s.start.getY());
      l.addPoint(s.end.getX(), s.end.getY());
      return l;
    }
 }
 
   static public void main(String args[]) {     PApplet.main(new String[] { "siroc" });  }}
