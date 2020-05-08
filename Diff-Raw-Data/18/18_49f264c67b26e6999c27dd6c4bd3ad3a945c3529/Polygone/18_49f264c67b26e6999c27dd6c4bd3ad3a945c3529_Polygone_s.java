 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package be.ibiiztera.md.pmatrix.pushmatrix;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  *
  * @author Mary
  */
 public class Polygone implements Representable, TRIGenerable{
     private ArrayList<Point3D> points = new ArrayList<Point3D>();
     private Color couleur;
 
     public Polygone() {
     }
 
     public Polygone(Color couleur) {
         this.couleur = couleur;
     }
 
     public Color getCouleur() {
         return couleur;
     }
 
     public void setCouleur(Color couleur) {
         this.couleur = couleur;
     }
 
     public ArrayList<Point3D> getPoints() {
         return points;
     }
 
     public void setPoints(ArrayList<Point3D> points) {
         this.points = points;
     }
     
     @Override
     public String id() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setId(String id) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Representable place(MODObjet aThis) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public TRIObject generate() {
         TRIObject to = new TRIObject();
         Point3D centre = new Point3D();
        for(int i=0; i<points.size()-1;i++)
         {
            to.add(new TRI(points.get(i), points.get(i+1), null, couleur));
            centre = centre.plus(points.get(i));
         }
        centre = centre.plus(points.get(points.size()-1));
        for(int i=0; i<to.getTriangles().size();i++)
         {
            to.getTriangles().get(i).getSommet()[2] = centre;
         }
        
        
         return to;
     }
 
     @Override
     public String toString() {
         String t = "poly (\n\t(";
         Iterator<Point3D> it = points.iterator();
         while(it.hasNext())
             t += "\n\t\t"+it.next().toString();
         t += "\n\t)\n\t"+CouleurOutils.toStringColor(couleur)+"\n)";
         return t;
      }
     
 }
