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
 
 import be.ibiiztera.md.pmatrix.pushmatrix.math.matrix.Matrix;
 
 public class Point3D implements Representable {
     public static final Point3D X = new Point3D(1,0,0);
    public static final Point3D Y = new Point3D(1,0,0);
    public static final Point3D Z = new Point3D(1,0,0);
     
     private String id;
     private double[] x;
     private Color c = Color.green;
 
     public Point3D() {
         x = new double[3];
     }
 
     public void set(int i, double d) {
         x[i] = d;
     }
 
     public Point3D(Point3D p0) {
         x = new double[3];
         x[0] = p0.getX();
         x[1] = p0.getY();
         x[2] = p0.getZ();
     }
 
     public Point3D(double x0, double y0, double z0) {
         x = new double[3];
         x[0] = x0;
         x[1] = y0;
         x[2] = z0;
     }
 
     @Override
     public Object clone() {
         return new Point3D(x[0], x[1], x[2]);
     }
 
     public double get(int i) {
         return x[i];
     }
 
     public double getY() {
         return x[1];
     }
 
     public double getZ() {
         return x[2];
     }
 
     public double getX() {
         return x[0];
     }
 
     public void setX(double x0) {
         x[0] = x0;
     }
 
     public void setY(double x0) {
         x[1] = x0;
     }
 
     public void setZ(double x0) {
         x[2] = x0;
     }
 
     public Point3D mult(double xDIFF) {
         Point3D p = new Point3D(this);
         p.setX(p.getX() * xDIFF);
         p.setY(p.getY() * xDIFF);
         p.setZ(p.getZ() * xDIFF);
         return p;
     }
 
     public double prodScalaire(Point3D p2) {
         return x[0] * p2.getX() + x[1] * p2.getY() + x[2] * p2.getZ();
     }
 
     public Point3D plus(double i) {
         Point3D p = new Point3D(this);
         p.setX(p.getX() + i);
         p.setY(p.getY() + i);
         p.setZ(p.getZ() + i);
         return p;
     }
 
     public Point3D plus(Point3D p) {
         Point3D p1 = new Point3D(this);
         p1.setX(p1.getX() + p.getX());
         p1.setY(p1.getY() + p.getY());
         p1.setZ(p1.getZ() + p.getZ());
         return p1;
     }
 
     public Point3D moins(Point3D p) {
         Point3D p1 = new Point3D(this);
         p1.setX(p1.getX() - p.getX());
         p1.setY(p1.getY() - p.getY());
         p1.setZ(p1.getZ() - p.getZ());
         return p1;
     }
 
     public static double distance(Point3D p1, Point3D p2) {
         return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX())
                 + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY())
                 + (p1.getZ() - p2.getZ()) * (p1.getZ() - p2.getZ()));
     }
 
     public double norme() {
         return Math.sqrt((getX()) * (getX())
                 + (getY()) * (getY()) + (getZ())
                 * (getZ()));
     }
 
     public Point3D prodVect(Point3D p1) {
         return new Point3D(p1.getY() * getZ() + p1.getZ() * getY(), p1.getZ()
                 * getX() + p1.getX() * getZ(), p1.getX() * getY() + p1.getY()
                 * getX());
     }
 
     public Point3D norme1() {
         return mult(1 / norme());
     }
     /*
      * public Point3D rotation(Axe axe, double angle) { return rotation(axe,
      * angle); }
      */
 
     @Override
     public String toString() {
         return "( " + x[0] + " , " + x[1] + " , " + x[2] + " ) ";
     }
 
     public String toLongString() {
         return "p ( \n\t(" + x[0] + " , " + x[1] + " , " + x[2] + " )\n\t(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")\n)\n";
     }
 
     public Color getC() {
         return c;
     }
 
     public void setC(Color c) {
         this.c = c;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = ID.GEN(this);
     }
 
     @Override
     public String id() {
         return id;
     }
 
     public Point3D rotatePoint(Axe axe, double angle) {
         Point3D src = new Point3D(this);
 
         // TODO CHECK ALGOs
         Point3D vectAxe = axe.getVectAxe();
 
         /**
          * p := (p1+(p2-p1)*a)
          *
          * (src-p).(p2-p1) = 0
          *
          *
          * (p1+(p2-p1)*a).vectAxe = src.vectAxe (src-p1).vectAxe =
          * (vectAxe.vectAxe)*a a = (src-p1).vectAxe / |vectAxe|�
          *
          */
         double a = (src.plus(axe.getP1().mult(-1)).prodScalaire(vectAxe))
                 / (vectAxe.prodScalaire(vectAxe));
 
         Point3D p = axe.getP1().plus(vectAxe.mult(a));
 
         Point3D vectX = p.mult(-1).plus(src).norme1();
 
         Point3D vectY = vectX.prodVect(vectAxe).norme1();
 
         Point3D vectZ = vectX.prodVect(vectY);
 
         Matrix m = new Matrix(vectX, vectY, vectZ).tild();
 
         Point3D pPrim = m.mult(src.plus(p.mult(-1))).plus(p);
 
         src.setX(pPrim.getX());
         src.setY(pPrim.getY());
         src.setZ(pPrim.getZ());
         return src;
     }
 
     /**
      * @param pb
      * @return
      */
     public static Point3D vecteur(Point3D pa, Point3D pb) {
 
         return pb.plus(pa.mult(-1));
     }
 
     public Point3D modificateurs(MODRotation r, MODTranslation t, MODHomothetie h) {
         return movePoint(rotation(r.matrice(), r.centre(), homothetie(h.centre(), h.facteur(), (Point3D) clone())), t.vecteur());
     }
 
     public Point3D movePoint(Point3D translation) {
         return new Point3D(this).plus(translation);
     }
 
     public Point3D movePoint(Point3D translation, Point3D p) {
         return new Point3D(p).plus(translation);
     }
     /*
      * public Point3D rotation(Axe axe, double angle) { Point3D src = new
      * Point3D(this); // TODO CHECK ALGOs Point3D vectAxe =
      * axe.getP2().plus(axe.getP1().mult(-1));
      *
      * double a =
      * (src.plus(axe.getP1().mult(-1)).prodScalaire(vectAxe))/(vectAxe.prodScalaire(vectAxe));
      *
      * Point3D p =
      * axe.getP1().plus(axe.getP1().mult(-1).plus(axe.getP2()).mult(a));
      *
      * Point3D vectX = p.mult(-1).plus(src).norme1();
      *
      * Point3D vectY = vectX.prodVect(vectAxe).norme1();
      *
      * Point3D vectZ = vectX.prodVect(vectY);
      *
      * Matrix m = new Matrix(vectX, vectY, vectZ);
      *
      * Point3D pPrim = m.mult(src.plus(p.mult(-1))).plus(p);      *
      * return pPrim;
 	}
      */
 
     public Point3D rotation(Matrix33 m, Point3D centre) {
         Point3D p = new Point3D(this);
         return m.mult(new Point3D(this).moins(centre)).plus(centre);
     }
 
     public Point3D rotation(Matrix33 m, Point3D centre, Point3D p) {
         return m.mult(new Point3D(p).moins(centre)).plus(centre);
     }
 
     public Point3D homothetie(Point3D c, double d, Point3D p) {
         return new Point3D(p).moins(c).mult(d).plus(c);
     }
 
     public Point3D homothetie(Point3D c, double d) {
         Point3D p = new Point3D(this);
         return new Point3D(this).moins(c).mult(d).plus(c);
     }
 
     /**
      * *
      *
      * Place le point dans l'espace.
      *
      * @param mod modificateurs
      * @return
      */
     @Override
     public Representable place(MODObjet mod) {
         Point3D p = this;
         p = p.homothetie(mod.homothetie().centre(), mod.homothetie().facteur());
         Point3D r = mod.rotation().centre().homothetie(mod.homothetie().centre(), mod.homothetie().facteur());
         p = p.rotation(mod.rotation().matrice(), r);
         p = p.plus(mod.translation().vecteur());
 
         return p;
     }
 }
