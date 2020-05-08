 package fr.def.iss.vd2.lib_v3d.gui;
 
 import org.fenggui.Widget;
 import org.fenggui.binding.render.Graphics;
 import org.fenggui.util.Dimension;
 
 import fr.def.iss.vd2.lib_v3d.V3DColor;
 
 public class V3DGuiLine extends V3DGuiComponent {
 
     private Widget widget;
     Dimension size = new Dimension(0, 0);
     V3DColor color = null;
     private int xPos = 0;
     private int yPos = 0;
     private float borderWidth = 2;
 
     public V3DGuiLine() {
         
         widget = new Widget() {
             @Override
             public void paint(Graphics g) {
                 
                 if(color != null) {
                     g.setColor(color.toColor());
                     g.setLineWidth(borderWidth);
                     g.drawLine(0, 0,size.getWidth(), size.getHeight());
                 }
             }
 
             @Override
             public int getX() {
                 return xPos;
             }
 
             @Override
             public int getY() {
                 return yPos;
             }
             
             @Override
             public Dimension getSize() {
                return new Dimension(Math.max(1, Math.abs(size.getWidth())), Math.max(1,Math.abs(size.getHeight())));
             }
             
         };
     }
     
     public void setColor(V3DColor color) {
         this.color = color;
     }
     
     public void setSize(int x, int y) {
         size = new Dimension(x, y);
     }
 
     @Override
     public Widget getFenGUIWidget() {
         
         return widget;
     }
 
     @Override
     public boolean containsPoint(int i, int i0) {
         return false;
     }
 
     @Override
     public void repack() {
         if(parent != null) {
             xPos = 0;
             yPos = 0;
             if(xAlignment == GuiXAlignment.LEFT) {
                 xPos = x;
             } else {
                 xPos = parent.getWidth() - size.getWidth() - x;
             }
 
             if(yAlignment == GuiYAlignment.BOTTOM) {
                 yPos = y;
             } else {
                 yPos = parent.getHeight() - size.getHeight() - y;
             }
 
             widget.setXY(xPos,  yPos);
         }
         
         
         
         widget.updateMinSize();
         widget.setSizeToMinSize();
         widget.layout();
     }
 
     public void setBorderWidth(float borderWidth) {
         this.borderWidth = borderWidth;
     }
     
 }
