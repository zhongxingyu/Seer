 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphview;
 
 import geometry.Intersect;
 import geometry.Rect;
 import geometry.Vec2;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.geom.Ellipse2D;
 
 /**
  *
  * @author Kirill
  */
 public class EllipseShape extends BaseShape{
     Ellipse2D.Float ell=null;
     
     public EllipseShape(Rect rect)
     {
         setLocalPlacement(rect);
         ell=new Ellipse2D.Float(rect.left,rect.top,rect.getSize().x,rect.getSize().y);
     };
     
     public EllipseShape(float posX,float posY, float sizeX, float sizeY)
     {
         setLocalPlacement(new Rect(posX,posY,posX+sizeX,posY+sizeY));
         ell=new Ellipse2D.Float(posX,posY,sizeX,sizeY);
     };
     
     public EllipseShape(Vec2 position, float radius)
     {
         Rect r=new Rect(position.x-radius,position.y-radius,position.x+radius,position.y+radius);
         setLocalPlacement(r);
         ell=new Ellipse2D.Float(r.left,r.top,r.getSize().x,r.getSize().y);
     };
     
     @Override
     public void draw(Graphics2D g) {
         Rect globalPlace=getGlobalPlacement();
         g.setColor(color);
         g.fillOval((int)globalPlace.left, (int)globalPlace.top, (int)globalPlace.getSize().x, (int)globalPlace.getSize().y);
         g.setColor(Color.black);
         
         if(bSelected)
         {
             Stroke oldStroke=g.getStroke();
             BasicStroke stroke=new BasicStroke(3,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{9}, 0);
             g.setStroke(stroke);
             g.drawOval((int)globalPlace.left, (int)globalPlace.top, (int)globalPlace.getSize().x, (int)globalPlace.getSize().y);
             g.setStroke(oldStroke);
             
         }
         else g.drawOval((int)globalPlace.left, (int)globalPlace.top, (int)globalPlace.getSize().x, (int)globalPlace.getSize().y);
         
         super.draw(g);
     }
     
     @Override
     public boolean isIntersects(Vec2 pt) {
         ell.x=getLocalPlacement().left;
         ell.y=getLocalPlacement().top;
         ell.width=getLocalPlacement().getSize().x;
         ell.height=getLocalPlacement().getSize().y;
         return ell.contains(pt.toPoint());
     }
 
     @Override
     public boolean isIntersects(Rect r) {
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Vec2 getPortPoint(Vec2 from) {
         Vec2 v1=new Vec2();
         Vec2 v2=new Vec2();
         
         Intersect.line_ellipsecenter(from,getGlobalPlacement(),v1,v2);
         
         if(from.getDistance(v1)<from.getDistance(v2))
             return v1;
         return v2;
     }
     
 }
