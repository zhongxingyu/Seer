 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nodes;
 
 import controlP5.Controller;
 import java.util.ArrayList;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PMatrix3D;
 /**
  *
  * @author kdbanman
  */
 public class GraphElement<T> extends Controller<T> {
 
     int hoverCol;
     int selectCol;
     int defaultCol;
     
     // currentCol responds to mouse hover
     int currentCol;
     
     float size;
     
     float labelSize;
     PFont labelFont;
     ArrayList<String> labelText;
     String constructedLabel;
     
     boolean displayLabel;
     int labelW;
     int labelH;
     int charW;
     int charH;
     
     Graph graph;
     UnProjector proj;
     PApplet pApp;
     Selection selection;
 
     //public GraphElement(ControlP5 cp5, String name, UnProjector unProj, PApplet pApplet) {
     public GraphElement(Graph parentGraph, String name) {
         super(parentGraph.cp5, name);
         
         graph = parentGraph;
         proj = parentGraph.proj;
         pApp = parentGraph.pApp;
         selection = parentGraph.selection;
         
         hoverCol = 0xFF5FEA6D;
         selectCol = 0xFFEA5F84;
     
         defaultCol = 0xFF1A4969;
     
         currentCol = defaultCol;
         
         size = 10;
         
         labelText = new ArrayList<>();
         constructedLabel = "";
         
        labelSize = 12;
        labelFont = pApp.createFont("cour.ttf", labelSize);
         charW = labelFont.getGlyph('A').width;
         charH = labelFont.getGlyph('A').height;
         
         labelW = charW;
         labelH = charH;
         
         displayLabel = false;
     }
     
     public void setColor(int col) {
       defaultCol = col;
     }
 
     public T setSize(final int s) {
         size = s;
         return setSize(s, s);
     }
 
     @Override
     protected void onEnter() {
         currentCol = hoverCol;
     }
 
     @Override
     protected void onLeave() {
         currentCol = defaultCol;
     }
 
     @Override
     protected void mouseReleasedOutside() {
         currentCol = defaultCol;
     }
     
     public boolean selected() {
         return selection.contains(this);
     }
     
     public void setDisplayLabel(boolean setVal) {
         displayLabel = setVal;
     }
     
     public void setLabelSize(int s) {
         labelSize = s;
        labelFont = pApp.createFont("cour.ttf", labelSize);
        charW = labelFont.getGlyph('A').width;
        charH = labelFont.getGlyph('A').height;
     }
     
     public void recalculateLabelDim() {
        labelH = charH + (labelText.size() - 1) * charH * 5 / 3;
         labelW = 0;
         for (String line : labelText) {
            labelW = PApplet.max(labelW, line.length() * charW);
         }
     }
     
     /**
      * converts labelText list of lines into a single label.
      * AFFECTS STATE: constructedLabel field
      */
     public void constructLabel() {
         constructedLabel = "";
         for (String line : labelText) {
             constructedLabel += line + "\n";
         }
     }
     
     /**
      * attempts to convert each line in labelText to a prefixed uri.
      * AFFECTS STATE: strings within Set labelText
      */
     public void prefixLabel() {
         for (String line : labelText) {
             line = graph.prefixed(line);
         }
     }
     
     /**
      * recalculates label dimensions and reconstructs the raw string for rendering.
      * attempts to prefix each line in the label.
      * 
      */
     public void updateLabel() {
         // order of the next three calls is critical
         prefixLabel();
         constructLabel();
         recalculateLabelDim();
     }
     
     public void displayLabel() {
         // set transform matrix for spherical billboard
         float[] tmp = new float[16];
         pApp.getMatrix().get(tmp);
         
         for (int i=0; i < 3; i++) {
             for (int j=0; j < 3; j++) {
                 tmp[i*4 + j] = i==j ? 1 : 0;
             }
         }
         PMatrix3D billboarded = new PMatrix3D();
         billboarded.set(tmp);
         
         pApp.pushMatrix();
         pApp.setMatrix(billboarded);
         
         pApp.fill(0xFF333333);
         
         pApp.pushMatrix();
         pApp.translate(0,0,size);
         pApp.rect(size - 3, -3, labelW + 6, labelH + 6, 2);
         pApp.popMatrix();
 
         //DEBUG
         //pApp.textSize(labelSize);
         pApp.fill(0xFF999999);
         pApp.textFont(labelFont);
         
         // translate() already called within display() function, so
         // text position spaced relative to GraphElement for separation/alignment
         pApp.text(constructedLabel, size, charH, size);
 
         pApp.popMatrix();
     }
 
     @Override
     public boolean equals(Object e) {
         if (!(e instanceof GraphElement)) {
             return false;
         }
 
         return getName().equals(((GraphElement) e).getName());
     }
 
     @Override
     public int hashCode() {
         return getName().hashCode();
     }
 }
