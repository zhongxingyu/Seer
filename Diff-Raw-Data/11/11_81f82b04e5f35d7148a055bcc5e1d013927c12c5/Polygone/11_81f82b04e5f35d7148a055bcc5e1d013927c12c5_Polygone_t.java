 /*
 
     Copyright (C) 2010-2012  DAHMEN, Manuel, Daniel
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 
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
         int size = points.size();
         TRIObject to = new TRIObject();
         Point3D centre = new Point3D();
         for(int i=0; i<size;i++)
         {
             centre = centre.plus(points.get(i).mult(1.0/size));
         }
        for(int i=0; i<size;i++)
         {
             to.add(new TRI(points.get(i%size), points.get((i+1)%size), centre, couleur));
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
 
     public void setPoints(Point3D[] point3D) {
         for(int i=0; i<point3D.length; i++)
             points.add(point3D[i]);
     }
     
 }
