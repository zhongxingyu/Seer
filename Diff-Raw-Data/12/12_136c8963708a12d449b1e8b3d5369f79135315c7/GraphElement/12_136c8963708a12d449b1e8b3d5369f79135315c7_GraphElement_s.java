 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nodes;
 
 import controlP5.Controller;
 import java.util.ArrayList;
 
 import processing.core.PFont;
 import processing.core.PMatrix3D;
 import processing.core.PVector;
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
     
     int labelSize;
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
     Nodes pApp;
     Selection selection;
 
     //public GraphElement(ControlP5 cp5, String name, UnProjector unProj, Nodes pApplet) {
     public GraphElement(Graph parentGraph, String name) {
         super(parentGraph.cp5, name);
         
         graph = parentGraph;
         proj = parentGraph.proj;
         pApp = parentGraph.pApp;
         selection = parentGraph.getSelection();
         
         hoverCol = 0xFF5FEA6D;
     
         defaultCol = 0xFF1A4969;
     
         currentCol = defaultCol;
         
         size = 10;
         
         labelText = new ArrayList<>();
         constructedLabel = "";
         
         labelSize = 12;
         setLabelSize(labelSize);
         
         displayLabel = false;
     }
     
     public void setColor(int col) {
       defaultCol = col;
       currentCol = col;
     }
 
     public T setSize(final int s) {
         size = s;
         return setSize(s, s);
     }
     
     public float getSize() {
         return size;
     }
     
     public int getLabelSize() {
         return labelSize;
     }
     
     public int getCol() {
         return defaultCol;
     }
 
     @Override
     protected void onEnter() {
         pApp.mouseContent = this;
         pApp.addToHovered(this);
         
         currentCol = hoverCol;
     }
 
     @Override
     protected void onLeave() {
         notHovered();
     }
     
     @Override
     protected void onReleaseOutside() {
         notHovered();
     }
     
     public void notHovered() {
         pApp.mouseContent = null;
         currentCol = defaultCol;
     }
     
     public boolean selected() {
         return selection.contains(this);
     }
     
     /**
      * for drag and drop movement that is agnostic to selection status. called
      * within onDrag, and is semantically different for Edges (hence the override)
      */
     protected void moveIfNotSelected(PVector horiz, PVector vert) {
         if (!selected()) {
             getPosition().add(horiz);
             getPosition().add(vert);
         }
     }
     
     public void setDisplayLabel(boolean setVal) {
         displayLabel = setVal;
     }
     
     public final void setLabelSize(int s) {
         labelSize = s;
         
         try {
            labelFont = pApp.createFont("labelFont.ttf", labelSize);
             charW = labelFont.getGlyph('A').width;
             charH = labelFont.getGlyph('A').height;
         } catch (Exception e) {
             System.out.println("ERROR: font not loaded.  ensure labelFont.tiff is in program directory.");
             labelFont = graph.cp5.getFont().getFont();
         }
         
         charW = labelFont.getGlyph('A').width;
         charH = labelFont.getGlyph('A').height;
         
         calculateLabelDim();
         
         pApp.textFont(labelFont);
     }
     
     public void calculateLabelDim() {
         labelH = charH + (labelText.size() - 1) * charH * 5 / 3;
         labelW = 0;
         for (String line : labelText) {
             labelW = Nodes.max(labelW, line.length() * charW);
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
     public void prefixLabel()  {
         int idx = 0;
         for (String line : labelText) {
             labelText.set(idx, graph.prefixed(line));
             idx++;
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
         calculateLabelDim();
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
         pApp.rect(-labelW / 2 - 3, -3, labelW + 6, labelH + 6, 2);
         pApp.popMatrix();
         
         pApp.fill(0xFFCCCCCC);
         pApp.text(constructedLabel, -labelW / 2, charH, size);
 
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
