 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphview.shapes;
 
 import geometry.Rect;
 import geometry.Vec2;
 import java.awt.BasicStroke;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.font.FontRenderContext;
 
 /**
  *
  * @author Kirill
  */
 public class TextShape extends BoxShape{
     FontRenderContext frc=new FontRenderContext(null, true,true);
     Rect bounds=null;
     
     FontProperty fontProp=null;
     StringProperty textProp=null;
     
     public TextShape(String text_)
     {
         super(0,0,10,10);
         
         textProp=propCreate("Text", new String(text_));
         fontProp=propCreate("Font", new Font("Arial",Font.PLAIN,15));  
         
         updateTextBounds();
 
         //bMoveable=false;
         //bResizeable=false;
         bReceiveMouseDrag=false;
         bReceiveMousePress=false;
         //bReceiveMouseClick=false;
         
         this.aspectType=eNodeAspectType.TEXT;
     };
     
     public void updateProperties(boolean bUpdateToProp)
     {
         if(!bUpdateToProp) updateTextBounds();
         super.updateProperties(bUpdateToProp);
     };
     
     public String getText()
     {
         return new String(textProp.getProp());
     };
     
     public void setText(String txt)
     {
         textProp.setProp(txt);
        updateTextBounds();
     };
     
     public void updateTextBounds()
     {
         bounds=Rect.fromRectangle2D(fontProp.getProp().
                 getStringBounds(textProp.getProp(), frc));
         setSize(bounds.getSize());
         if(parent!=null) ((NodeAspect) parent).updateContainer();
     };
     
     @Override
     public void draw(Graphics2D g) {
         g.setFont(fontProp.getProp());
         
         
         Rect globalPlace=getGlobalRectangle();
         g.setColor(color.getProp());
         g.drawString(textProp.getProp(), globalPlace.left, globalPlace.bottom-bounds.bottom);
         
         if(bSelected) drawGrip(g);
     }
     
     @Override
     public Rect getContainRect() {
         Rect r=new Rect(0,0,getRectangle().getSize().x,getRectangle().getSize().y);
         return r.getReduced(containerOffset);
     }
 }
