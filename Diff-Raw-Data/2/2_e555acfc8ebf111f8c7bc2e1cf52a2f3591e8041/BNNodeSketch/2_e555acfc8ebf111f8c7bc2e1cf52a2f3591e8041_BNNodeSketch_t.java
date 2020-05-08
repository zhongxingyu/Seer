 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.pitt.isp.sverchkov.bnvis;
 
 import java.util.*;
 import processing.core.PApplet;
 
 /**
  *
  * @author YUS24
  */
 public class BNNodeSketch extends AbstractProcessingDrawable implements ProcessingDrawable {
     
     public static final float SPACING = 3;
     public static final float BARHEIGHT = 10;
     public static final float TITLESIZE = 20;
     public static final float VLABELSIZE = 10;
     
     private final Map<String,BNNodeSketch> parents = new HashMap<>();
     private final Map<String,Float> outHandleXOs = new HashMap<>(); // X offset of outgoing handles
     private final BNNodeModel model;
     
     private float x,y,width = 50,height = 50;
     private float oldX, oldY;
     private boolean dragging = false;
     private boolean expanded = true;
     
     public BNNodeSketch( float x, float y, BNNodeModel m ){
         this.x = x;
         this.y = y;
         this.model = m;
     }
     
     public void addParentNodes( BNNodeSketch ... parentNodes ){
         for( BNNodeSketch node : parentNodes )
             parents.put( node.model.name(), node );
     }
     
     public void addParentNodes( Collection<BNNodeSketch> parentNodes ){
         addParentNodes( parentNodes.toArray( new BNNodeSketch[parentNodes.size()] ) );
     }
     
     @Override
     public boolean isMouseOver( float mouseX, float mouseY ){
         return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
     }
 
     @Override
     public void draw() {
                 
         if( expanded ){
             
             // Compute width
             {
                 float valwidth = 10;
                 for( String value : model.values() )
                     valwidth = Math.max( valwidth, p.textWidth( value ) );
                 width = model.values().size() * (valwidth + SPACING);
             }
 
             float yc = y; // c for "cursor"
 
             // NodeTitle
             p.fill(0);
             p.textAlign( PApplet.CENTER );
             p.textSize(TITLESIZE);
             p.text(model.name(), x + width/2, yc );
             
             yc += SPACING;
             
             // Draw CPT Bars
             for( CPTRow row : model.activeCPTS() ){
                 int i = 0;
                 float xc = x;
                 for( double cp : row ){ // cp is "conditional probability"
                     
                     // Draw the bar
                     setColorForValue( i++ );
                     float w = (float) cp*width;
                     p.rect( xc, yc, w, BARHEIGHT );
                     xc += w;
                     
                     // Link to parent
                     p.stroke(0,0,0,128);
                     for( Map.Entry<String,String> parent: row.parentAssignment().entrySet() )
                         DrawingHelpers.arrow(p,
                             parents.get(parent.getKey()).OutHandleXFor(parent.getValue()),
                             parents.get(parent.getKey()).OutHandleY(),
                             x-SPACING,
                             yc + BARHEIGHT/2 );
                 }
                 yc += BARHEIGHT + SPACING;
             }
             
             // Draw value labels
             {
                 p.textSize(VLABELSIZE);
                 yc += VLABELSIZE;
                 float w = width/model.values().size(), wc = w/2;
                 int i = 0;
                 for( String value : model.values() ){
                     setColorForValue( i++ );
                     p.text(value, x+wc, yc );
                     outHandleXOs.put( value, wc );
                     wc += w;
                 }
             }
             
             height = yc-y;
             
         }else{        
             // p.rectMode(PApplet.CENTER);
             p.rect(x,y,width,height);
             for( BNNodeSketch parent : parents.values() ){
                 DrawingHelpers.arrow(p, parent.x, parent.y, x, y );
             }        
         }
         
     }
 
     @Override
     public void handleMouse(float mouseX, float mouseY, float pmouseX, float pmouseY, boolean mousePressed) {
        if ( dragging && mousePressed ){
             x = oldX + mouseX - pmouseX;
             y = oldY + mouseY - pmouseY;            
         }
         oldX = x;
         oldY = y;
         dragging = mousePressed;
     }
     
     private void setColorForValue( int value ){
         int color = (value%2)*50;
         p.noStroke();
         p.fill( color );
     }
     
     public float OutHandleXFor( String value ){
         return x + outHandleXOs.get(value);
     }
     
     public float OutHandleY(){
         return y + height;
     }
 }
