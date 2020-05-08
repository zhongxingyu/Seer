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
 
 import be.ibiiztera.md.pmatrix.pushmatrix.extras.SimpleSphere;
 import be.ibiiztera.md.pmatrix.pushmatrix.generator.TRIObjetGenerateurAbstract;
 import be.ibiiztera.md.pmatrix.pushmatrix.gui.ZBufferInfo;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * @author MANUEL DAHMEN
  *
  * dev
  *
  * 15 oct. 2011
  *
  */
 public class ZBufferImpl implements ZBuffer {
 
     private Point3D INFINI = new Point3D(0, 0, 1100000);
     private Color COULEUR_FOND = Color.WHITE;
     public static final int PERSPECTIVE_ISOM = 0;
     public static final int PERSPECTIVE_OEIL = 1;
     public int type_perspective = PERSPECTIVE_OEIL;
     protected Point3D planproj = null;
     protected Point3D camera = null;
     private Camera cameraC = null;
 
     @Override
     public void suivante() {
         id++;
     }
 
     {
 
         COULEUR_FOND = new Color(255, 255, 255, 255);
 
     }
 
     @Override
     public void perspective(double camera, double planproj) {
         type_perspective = PERSPECTIVE_OEIL;
         this.camera = new Point3D(0, 0, camera);
         this.planproj = new Point3D(0, 0, planproj);
     }
 
     @Override
     public void isometrique() {
         //type_perspective = PERSPECTIVE_ISOM;
     }
 
     @Override
     public void camera(Camera c) {
         type_perspective = PERSPECTIVE_OEIL;
         this.cameraC = c;
     }
 
     @Override
     public Camera camera() {
         return this.cameraC;
     }
 
     public class Box2DPerspective {
 
         public float d = -10.0f;
         public float w = 10.0f;
         public float h = w * la / ha;
 
         /**
          * @param scene
          */
         public Box2DPerspective(Scene scene) {
         }
     }
     private int id = 0;
     private ZBufferInfo infos = new ZBufferInfo();
     // PARAMETRES
     private float zoom = 1.05f;
     private int dimx;
     private int dimy;
     private Point3D[][] Sx;
     private Color[][] Sc;
     private int[][] Simeid;
     private float[][] Simeprof;
     public double INFINI_PROF = 1000000;
 
     @Override
     public int resX() {
         return dimx;
     }
 
     @Override
     public int resY() {
         return dimy;
     }
 
     public void couleurDeFond(Color c) {
         COULEUR_FOND = c;
     }
 
     public void zoom(float z) {
         if (z > 0) {
             zoom = z;
         }
     }
 
     public class ImageMapElement {
 
         private ImageMapElement instance;
 
         public ImageMapElement() {
             Sx = new Point3D[la][ha];
             Sc = new Color[la][ha];
             Simeid = new int[la][ha];
             Simeprof = new float[la][ha];
             for (int i = 0; i < la; i++) {
                 for (int j = 0; j < ha; j++) {
                     Simeprof[i][j] = (float) INFINI.getZ();
                 }
             }
         }
 
         public ImageMapElement getInstance(int x, int y) {
             if (instance == null) {
                 instance = new ImageMapElement();
             }
             return instance;
         }
 
         public void setElementPoint(int x, int y, Point3D px) {
             if (checkCoordonnees(x, y)) {
                 setElementID(x, y, id);
                 Sx[x][y] = px;
             }
         }
 
         public void setElementCouleur(int x, int y, Color pc) {
 
             if (checkCoordonnees(x, y)) {
                 setElementID(x, y, id);
                 Sc[x][y] = pc;
             }
         }
 
         public Color getElementCouleur(int x, int y) {
             if (checkCoordonnees(x, y)) {
                 if (Simeid[x][y] == id) {
                     return Sc[x][y];
                 } else {
                     return COULEUR_FOND;
                 }
             }
             return COULEUR_FOND;
         }
 
         public Point3D getElementPoint(int x, int y) {
             if (checkCoordonnees(x, y)) {
                 if (Simeid[x][y] == id) {
                     return Sx[x][y];
                 } else {
                     return INFINI;
                 }
             } else {
                 return INFINI;
             }
         }
 
         public void setElementID(int x, int y, int id) {
             if (checkCoordonnees(x, y)) {
                 Simeid[x][y] = id;
             }
         }
 
         public int getElementID(int x, int y) {
             if (checkCoordonnees(x, y)) {
                 return Simeid[x][y];
             } else {
                 return -1;
             }
         }
 
         public boolean checkCoordonnees(int x, int y) {
             return x >= 0 && x < resX() && y >= 0 && y < resY() ? true : false;
         }
 
         public boolean checkID(int x, int y, int id2) {
             if (checkCoordonnees(x, y)) {
                 if (Simeid[x][y] == id2) {
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         }
 
         private double getElementProf(int x, int y) {
             if (checkCoordonnees(x, y)) {
                 return Simeprof[x][y];
             } else {
                 return INFINI_PROF;
             }
         }
 
         private void setProf(int x, int y, double d) {
             if (checkCoordonnees(x, y)) {
                 Simeprof[x][y] = (float) d;
             }
         }
     }
 
     public class ImageMap {
 
         private ImageMapElement ime;
 
         public ImageMap(int x, int y) {
             dimx = x;
             dimy = y;
             ime = new ImageMapElement();
             for (int i = 0; i < x; i++) {
                 for (int j = 0; j < y; j++) {
                     ime.setElementID(x, y, id);
                     ime.setElementPoint(x, y, INFINI);
                     ime.setElementCouleur(x, y, Color.white);
                 }
             }
         }
 
         public ImageMapElement getIME() {
             return ime;
         }
 
         public void reinit() {
             id++;
         }
 
         public int getDimx() {
             return dimx;
         }
 
         public int getDimy() {
             return dimy;
         }
 
         private boolean checkCoordinates(int x, int y) {
             if (x >= 0 & x < la & y >= 0 & y < ha) {
                 return true;
             }
             return false;
         }
 
         public void setIME(int x, int y) {
             ime.setElementID(x, y, id);
         }
 
         public void testProf(Point3D x3d, Point coordonneesPointEcra, Color c) {
             int x = (int) coordonneesPointEcra.getX();
             int y = (int) coordonneesPointEcra.getY();
             double prof = 0;
             if (type_perspective == PERSPECTIVE_ISOM) {
                 prof = x3d.getZ();
             } else {
                 prof = camera.moins(x3d).norme();
             }
             if (x >= 0 & x < la & y >= 0 & y < ha
                     & prof < ime.getElementProf(x, y)) {
                 ime.setElementID(x, y, id);
                 ime.setElementPoint(x, y, x3d);
                 ime.setElementCouleur(x, y, c);
                 ime.setProf(x, y, prof);
             }
 
         }
 
         public void testProf(Point3D x3d, Color c) {
             Point ce;
             double prof = 0;
             if (type_perspective == PERSPECTIVE_ISOM) {
                 ce = coordonneesPointEcran(x3d);
                 prof = x3d.getZ();
             } else {
                 ce = coordonneesPointEcranPerspective(x3d);
                 prof = camera.moins(x3d).norme();
 
             }
             int x = (int) ce.getX();
             int y = (int) ce.getY();
             if (x >= 0 & x < la & y >= 0 & y < ha
                     && prof < ime.getElementProf(x, y)) {
                 ime.setElementID(x, y, id);
                 ime.setElementPoint(x, y, x3d);
                 ime.setElementCouleur(x, y, c);
                 ime.setProf(x, y, prof);
 
             }
         }
 
         public void dessine(Point3D x3d, Color c) {
             Point ce;
             double prof = 0;
             if (type_perspective == PERSPECTIVE_ISOM) {
                 ce = coordonneesPointEcran(x3d);
                 prof = -1000;
             } else {
                 ce = coordonneesPointEcranPerspective(x3d);
                 prof = camera.moins(x3d).norme();
             }
             int x = (int) ce.getX();
             int y = (int) ce.getY();
             if (x >= 0 & x < la & y >= 0 & y < ha) {
                 ime.setElementID(x, y, id);
                 ime.setElementPoint(x, y, x3d);
                 ime.setElementCouleur(x, y, c);
                 ime.setProf(x, y, prof);
             }
         }
     }
 
     public class Box2D {
 
         private double minx = 1000000;
         private double miny = 1000000;
         private double minz = 1000000;
         private double maxx = -1000000;
         private double maxy = -1000000;
         private double maxz = -1000000;
         private boolean notests = false;
 
         private void test(Point3D p) {
             if (p.getX() < minx) {
                 minx = p.getX();
             }
             if (p.getY() < miny) {
                 miny = p.getY();
             }
             if (p.getZ() < minz) {
                 minz = p.getZ();
             }
             if (p.getX() > maxx) {
                 maxx = p.getX();
             }
             if (p.getY() > maxy) {
                 maxy = p.getY();
             }
             if (p.getZ() > maxz) {
                 maxz = p.getZ();
             }
 
         }
 
         public Box2D() {
             SceneCadre cadre = scene1.getCadre();
             if (cadre.isCadre()) {
                 for (int i = 0; i < 4; i++) {
                     if (cadre.get(i) != null) {
                         test(cadre.get(i));
                     }
                 }
                 if (!cadre.isExterieur()) {
                     notests = true;
                 }
             }
 
             if (!notests) {
                 Iterator<Representable> it = scene1.iterator();
                 while (it.hasNext()) {
                     Representable r = it.next();
                     // GENERATORS
                     if (r instanceof TRIGenerable) {
                         r = ((TRIGenerable) r).generate();
                     } else if (r instanceof PGenerator) {
                         r = ((PGenerator) r).generatePO();
                     } else if (r instanceof TRIConteneur) {
                         r = ((TRIConteneur) r).getObj();
                     }
                     // OBJETS
                     if (r instanceof TRIObject) {
                         TRIObject o = (TRIObject) r;
                         Iterator<TRI> ts = o.triangles.iterator();
                         while (ts.hasNext()) {
                             TRI t = ts.next();
                             for (Point3D p : t.getSommet()) {
                                 test(p);
                             }
                         }
                     } else if (r instanceof Point3D) {
                         Point3D p = (Point3D) r;
                         test(p);
                     } else if (r instanceof SegmentDroite) {
                         SegmentDroite p = (SegmentDroite) r;
                         test(p.getOrigine());
                         test(p.getExtremite());
                     } else if (r instanceof TRI) {
                         TRI t = (TRI) r;
                         test(t.getSommet()[0]);
                         test(t.getSommet()[1]);
                         test(t.getSommet()[2]);
                     } else if (r instanceof BSpline) {
                         BSpline b = (BSpline) r;
                         Iterator<Point3D> ts = b.iterator();
                         while (ts.hasNext()) {
                             Point3D p = ts.next();
                             test(p);
                         }
                     } else if (r instanceof BezierCubique) {
                         BezierCubique b = (BezierCubique) r;
                         Iterator<Point3D> ts = b.iterator();
                         while (ts.hasNext()) {
                             Point3D p = ts.next();
                             test(p);
                         }
                     } else if (r instanceof POConteneur) {
                         for (Point3D p : ((POConteneur) r).iterable()) {
                             test(p);
                         }
 
                     } else if (r instanceof PObjet) {
                         for (Point3D p : ((PObjet) r).iterable()) {
                             test(p);
                         }
 
                     } else if (r instanceof SimpleSphere) {
                         test(((SimpleSphere) r).CoordPoint(0, 0));
                         test(((SimpleSphere) r).CoordPoint(Math.PI, 0));
                         test(((SimpleSphere) r).CoordPoint(0, Math.PI));
                         test(((SimpleSphere) r).CoordPoint(Math.PI, Math.PI));
                     } else if (r instanceof TRIObjetGenerateurAbstract) {
                         TRIObjetGenerateurAbstract t = (TRIObjetGenerateurAbstract) r;
                         TRI[] ts = new TRI[2];
                         ts[0] = new TRI(INFINI, INFINI, INFINI);
                         ts[1] = new TRI(INFINI, INFINI, INFINI);
                         for (int x = 0; x < t.getMaxX() - 1; x++) {
                             for (int y = 0; y < t.getMaxY() - 1; y++) {
                                 t.getTris(x, y, ts);
                                 for (int i = 0; i < 2; i++) {
                                     for (int j = 0; j < 3; j++) {
                                         test(ts[i].getSommet()[j]);
                                     }
                                 }
                             }
                         }
 
                     }
                 }
             }
             // Adapter en fonction du ratio largeur/hauteur
             double ratioEcran = 1.0 * la / ha;
             double ratioBox = (maxx - minx) / (maxy - miny);
             double minx2 = minx, miny2 = miny, maxx2 = maxx, maxy2 = maxy;
             if (ratioEcran > ratioBox) {
                 // Ajouter des points en x
                 minx2 = minx - (1 / ratioBox * ratioEcran / 2) * (maxx - minx);
                 maxx2 = maxx + (1 / ratioBox * ratioEcran / 2) * (maxx - minx);
             } else if (ratioEcran < ratioBox) {
                 // Ajouter des points en y
                 miny2 = miny - (ratioBox / ratioEcran / 2) * (maxy - miny);
                 maxy2 = maxy + (ratioBox / ratioEcran / 2) * (maxy - miny);
 
             }
             minx = minx2;
             miny = miny2;
             maxx = maxx2;
             maxy = maxy2;
 
             double ajuste = zoom - 1.0;
             minx2 = minx - ajuste * (maxx - minx);
             miny2 = miny - ajuste * (maxy - miny);
             maxx2 = maxx + ajuste * (maxx - minx);
             maxy2 = maxy + ajuste * (maxy - miny);
             minx = minx2;
             miny = miny2;
             maxx = maxx2;
             maxy = maxy2;
 
             //System.out.println("BOX2D : minx" + minx + " maxx " + maxx + " miny " + miny + " maxy " + maxy);
 
         }
 
         public Rectangle rectangle() {
             return new Rectangle((int) minx, (int) miny, (int) maxx, (int) maxy);
         }
 
         public boolean checkPoint(Point3D p) {
             if (p.getX() > minx & p.getX() < maxx & p.getY() > miny
                     & p.getY() < maxy) {
                 return true;
             } else {
                 return false;
             }
         }
 
         public double getMinx() {
             return minx;
         }
 
         public void setMinx(double minx) {
             this.minx = minx;
         }
 
         public double getMiny() {
             return miny;
         }
 
         public void setMiny(double miny) {
             this.miny = miny;
         }
 
         public double getMaxx() {
             return maxx;
         }
 
         public void setMaxx(double maxx) {
             this.maxx = maxx;
         }
 
         public double getMaxy() {
             return maxy;
         }
 
         public void setMaxy(double maxy) {
             this.maxy = maxy;
         }
 
         public double getMinz() {
             return minz;
         }
 
         public void setMinz(double minz) {
             this.minz = minz;
         }
 
         public double getMaxz() {
             return maxz;
         }
 
         public void setMaxz(double maxz) {
             this.maxz = maxz;
         }
 
         public double echelleEcran() {
             return (box.getMaxx() - box.getMinx()) / la;
         }
     }
     /*
      * public class TrianglePix {
      *
      * public class PP {
      *
      * public Point p; public Point3D p3d;
      *
      * public PP(Point p, Point3D p3d) { this.p = p; this.p3d = p3d; } } private
      * HashMap<Integer, ArrayList<PP>> po2 = new HashMap<Integer,
      * ArrayList<PP>>(); private Point3D p3dMin; private Point3D p3dMax;
      *
      * public boolean put(Integer arg0, PP arg1) { if (po2.get(arg0) == null) {
      * po2.put(arg0, new ArrayList<ZBufferImpl.TrianglePix.PP>()); } return
      * po2.get(arg0).add(arg1); }
      *
      * public ArrayList<PP> get(Object arg0) { return po2.get(arg0); }
      *
      * public int getMinY() { Iterator<Integer> it = po2.keySet().iterator();
      * int min = 10000; while (it.hasNext()) { Integer n = it.next(); if (n <
      * min) { min = n; }
      *
      * }
      * return min; }
      *
      * public int getMaxY() { Iterator<Integer> it = po2.keySet().iterator();
      * int max = -10000; while (it.hasNext()) { Integer n = it.next();
      *
      * if (n > max) { max = n; }
      *
      * }
      * return max; }
      *
      * public int getMinX(int y) { if (po2.get(y) == null) { return -1; }
      * Iterator<PP> it = po2.get(y).iterator(); int minx = 10000; while
      * (it.hasNext()) { PP n = it.next(); // System.out.println("N y= " + y + "
      * :: x= " + n.getX()); if (n.p.getX() < minx) { minx = (int) n.p.getX(); }
      *
      * }
      * return minx; }
      *
      * public int getMaxX(int y) { if (po2.get(y) == null) { return -1; }
      * Iterator<PP> it = po2.get(y).iterator(); int maxx = -10000; while
      * (it.hasNext()) { PP n = it.next(); // System.out.println("N y= " + y + "
      * :: x= " + n.getX()); if (n.p.getX() > maxx) { maxx = (int) n.p.getX(); }
      *
      * }
      * return maxx; }
      *
      * public void calculer3D(int y) { if (po2.get(y) == null) { p3dMin = new
      * Point3D(0, 0, -2000); p3dMax = new Point3D(0, 0, -2000);
      *
      * return; } double minx = 10000; double maxx = -10000; for (int i = 0; i <
      * po2.get(y).size(); i++) {
      *
      * PP pp = po2.get(y).get(i); Point n = pp.p; Point3D n3d = pp.p3d;
      *
      * if (n.getX() < minx) { minx = n.getX(); p3dMin = n3d; } if (n.getX() <
      * maxx) { maxx = n.getX(); p3dMax = n3d; } } }
      *
      * public Point3D get3DXMin() { return p3dMin; }
      *
      * public Point3D get3DXMax() { return p3dMax; } }
      *
      *
      */
     private ImageMap ime;
     private Box2D box;
     private int ha;
     private int la;
     //private Color couleurCourante;
     private Box2DPerspective boxPerspect;
 
     public ZBufferImpl(int l, int h) {
         la = l;
         ha = h;
         dimx = la;
         dimy = ha;
         ime = new ImageMap(l, h);
     }
 
     @Override
     public ZBuffer getInstance(int x, int y) {
         return new ZBufferImpl(x, y);
     }
 
     @Override
     public java.awt.Point coordonneesPointEcran(Point3D p) {
         Point p2 = new java.awt.Point(
                 (int) (la / (box.getMaxx() - box.getMinx()) * (p.getX() - box.getMinx())),
                 ha - (int) (ha / (box.getMaxy() - box.getMiny()) * (p.getY() - box.getMiny())));
         if (p2.getX() >= 0.0 && p2.getX() < la && p2.getY() >= 0 && p2.getY() < ha) {
             return p2;
         } else {
             return new Point(la / 2, ha / 2);
         }
     }
 
     public double echelleEcran() {
         return box.echelleEcran();
     }
 
     public Image rendu() {
         return null;
     }
     private Scene scene1;
 
     @Override
     public Scene scene() {
         return scene1;
     }
 
     @Override
     public void scene(Scene s) {
         this.scene1 = s;
     }
 
     @Override
     public void dessinerContours() {
         Scene scene = scene1;
         id++;
         box = new Box2D();
         Iterator<Representable> it = scene.iterator();
         while (it.hasNext()) {
             Representable r = it.next();
             if (r instanceof TRIGenerable) {
                 r = ((TRIGenerable) r).generate();
             }
             if (r instanceof TRIConteneur) {
                 r = ((TRIConteneur) r).getObj();
             }
             if (r instanceof TRIObject) {
                 TRIObject o = (TRIObject) r;
                 Iterator<TRI> ts = o.triangles.iterator();
                 while (ts.hasNext()) {
                     // System.out.println("Triangle suivant");
 
                     TRI t = ts.next();
 
                     tracerTriangle(t.getSommet()[0], t.getSommet()[1], t.getSommet()[2], t.getCouleur());
                 }
             } else if (r instanceof BSpline) {
                 BSpline b = (BSpline) r;
                 int nt = 100;
                 for (double t = 0; t < 1.0; t += 1.0 / nt) {
                     try {
                         Point3D p3d = b.calculerPoint3D(t);
                         ime.testProf(p3d, b.getColor());
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             } else if (r instanceof Point3D) {
                 Point3D p = (Point3D) r;
                 ime.testProf(p, p.getC());
             } else if (r instanceof SegmentDroite) {
                 SegmentDroite s = (SegmentDroite) r;
                 Point x1 = coordonneesPointEcran(s.getOrigine());
                 Point x2 = coordonneesPointEcran(s.getExtremite());
 
                 double x = Math.max(x1.getX(), x2.getX());
                 double y = Math.max(x1.getY(), x2.getY());
 
                 double itere = Math.max(x, y) * 4;
                 for (int i = 0; i < itere; i++) {
                     Point3D p = s.getOrigine().mult(i / itere).plus(s.getExtremite().mult(1 - i / itere));
                     p.setC(s.getC());
                     ime.testProf(p, p.getC());
                 }
 
             } else if (r instanceof BezierCubique) {
                 BezierCubique b = (BezierCubique) r;
                 int nt = 100;
                 for (double t = 0; t < 1.0; t += 1.0 / nt) {
                     try {
                         Point3D p3d = b.calculerPoint3D(t);
                         ime.testProf(p3d, b.getColor());
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
 
             } else if (r instanceof PObjet) {
                 PObjet b = (PObjet) r;
                 for (Point3D p : b.getPoints()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof POConteneur) {
                 POConteneur c = (POConteneur) r;
                 for (Point3D p : c.iterable()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof TRIConteneur) {
                 for (TRI t : ((TRIConteneur) r).iterable()) {
                     Color c = t.getCouleur();
 
                     tracerAretes(t.getSommet()[0], t.getSommet()[1], c);
                     tracerAretes(t.getSommet()[1], t.getSommet()[2], c);
                     tracerAretes(t.getSommet()[2], t.getSommet()[0], c);
 
                 }
 
             }
         }
     }
 
     @Override
     public void dessinerSilhouette() {
         Scene scene = scene1;
         id++;
         box = new Box2D();
         Iterator<Representable> it = scene.iterator();
         while (it.hasNext()) {
             Representable r = it.next();
             if (r instanceof TRIGenerable) {
                 r = ((TRIGenerable) r).generate();
             }
             if (r instanceof TRIConteneur) {
                 r = ((TRIConteneur) r).getObj();
             }
             if (r instanceof TRIObject) {
                 TRIObject o = (TRIObject) r;
                 Iterator<TRI> ts = o.triangles.iterator();
                 while (ts.hasNext()) {
                     // System.out.println("Triangle suivant");
 
                     TRI t = ts.next();
 
                     tracerTriangle(t.getSommet()[0], t.getSommet()[1], t.getSommet()[2], t.getCouleur());
                 }
 
             } else if (r instanceof Point3D) {
                 Point3D p = (Point3D) r;
                 ime.testProf(p, p.getC());
             } else if (r instanceof SegmentDroite) {
                 SegmentDroite s = (SegmentDroite) r;
                 Point x1 = coordonneesPointEcran(s.getOrigine());
                 Point x2 = coordonneesPointEcran(s.getExtremite());
 
                 double x = Math.max(x1.getX(), x2.getX());
                 double y = Math.max(x1.getY(), x2.getY());
 
                 double itere = Math.max(x, y) * 4;
                 for (int i = 0; i < itere; i++) {
                     Point3D p = s.getOrigine().mult(i / itere).plus(s.getExtremite().mult(1 - i / itere));
                     p.setC(s.getC());
                     ime.testProf(p, p.getC());
                 }
 
             } else if (r instanceof BezierCubique) {
                 BezierCubique b = (BezierCubique) r;
                 int nt = 100;
                 for (double t = 0; t < 1.0; t += 1.0 / nt) {
                     try {
                         Point3D p3d = b.calculerPoint3D(t);
                         ime.testProf(p3d, b.getColor());
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
 
             } else if (r instanceof PObjet) {
                 PObjet b = (PObjet) r;
                 for (Point3D p : b.getPoints()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof POConteneur) {
                 POConteneur c = (POConteneur) r;
                 for (Point3D p : c.iterable()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof TRIConteneur) {
                 for (TRI t : ((TRIConteneur) r).iterable()) {
                     //pi = new TrianglePix();
                     tracerAretes(t.getSommet()[0], t.getSommet()[1], t.getCouleur());
                     tracerAretes(t.getSommet()[1], t.getSommet()[2], t.getCouleur());
                     tracerAretes(t.getSommet()[2], t.getSommet()[0], t.getCouleur());
                     //tracerTriangle(pi);
 
                 }
 
             }
         }
     }
 
     @Override
     public void dessinerSilhouette3D() {
         System.out.println("RENDERING");
         Scene scene = scene1;
         id++;
         box = new Box2D();
         Iterator<Representable> it = scene.iterator();
         while (it.hasNext()) {
             Representable r = it.next();
 
             // GENERATORS
             if (r instanceof TRIGenerable) {
                 r = ((TRIGenerable) r).generate();
             } else if (r instanceof PGenerator) {
                 r = ((PGenerator) r).generatePO();
             }
             if (r instanceof TRIConteneur) {
                 r = ((TRIConteneur) r).getObj();
             }
 
             // OBJETS
             if (r instanceof TRIObject) {
                 TRIObject o = (TRIObject) r;
                 Iterator<TRI> ts = o.triangles.iterator();
                 while (ts.hasNext()) {
                     // System.out.println("Triangle suivant");
 
                     TRI t = ts.next();
 
                     tracerTriangle(t.getSommet()[0], t.getSommet()[1],
                             t.getSommet()[2], t.getCouleur());
 
                 }
             } else if (r instanceof Point3D) {
                 Point3D p = (Point3D) r;
                 ime.testProf(p, p.getC());
             } else if (r instanceof SegmentDroite) {
                 SegmentDroite s = (SegmentDroite) r;
                 ligne(s.getOrigine(), s.getExtremite(), s.getC());
             } else if (r instanceof TRI) {
                 TRI t = (TRI) r;
                 tracerTriangle(t.getSommet()[0], t.getSommet()[1],
                         t.getSommet()[2], t.getCouleur());
             } else if (r instanceof BSpline) {
                 BSpline b = (BSpline) r;
                 int nt = 100;
                 for (double i = 0; i < nt; i++) {
                     try {
                         Point3D p3d = b.calculerPoint3D(3.0 + 1.0 * nt / 200);
                         ime.testProf(p3d, b.getColor());
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             } else if (r instanceof BezierCubique) {
                 BezierCubique b = (BezierCubique) r;
                 int nt = largeur() / 10;
                 Point3D p0 = b.calculerPoint3D(0.0);
                 for (double t = 0; t < 1.0; t += 1.0 / nt) {
                     try {
                         Point3D p1 = b.calculerPoint3D(t);
                         ligne(p0, p1, b.getColor());
                         p0 = p1;
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
 
             } else if (r instanceof PObjet) {
                 PObjet b = (PObjet) r;
                 for (Point3D p : b.getPoints()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof POConteneur) {
                 POConteneur c = (POConteneur) r;
                 for (Point3D p : c.iterable()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof TRIConteneur) {
                 for (TRI t : ((TRIConteneur) r).iterable()) {
                     tracerTriangle(t.getSommet()[0], t.getSommet()[1],
                             t.getSommet()[2], t.getCouleur());
 
                 }
 
             } else if (r instanceof TRIObjetGenerateurAbstract) {
                 TRIObjetGenerateurAbstract to = (TRIObjetGenerateurAbstract) r;
                 to.draw(this);
                 /*
                  * TRIObjetGenerateurAbstract to = (TRIObjetGenerateurAbstract)
                  * r; TRI[] tris = new TRI[2]; tris[0] = new TRI(INFINI, INFINI,
                  * INFINI); tris[1] = new TRI(INFINI, INFINI, INFINI); for (int
                  * numX = 0; numX < to.getMaxX() - 1; numX++) { for (int numY =
                  * 0; numY < to.getMaxY() - 1; numY++) { to.getTris(numX, numY,
                  * tris);
                  *
                  * double incrMax = 1.0; for (int t = 0; t < 2; t++) { for (int
                  * c = 0; c < 3; c++) { Point p1 =
                  * coordonneesPointEcran(tris[t].getSommet()[c]); Point p2 =
                  * coordonneesPointEcran(tris[t].getSommet()[(c + 1) % 3]);
                  * double incr = 1.0 / (Math.abs(p1.getX() - p2.getX()) +
                  * Math.abs(p1.getY() - p2.getY())); if (incr < incrMax) {
                  * incrMax = incr; } } }
                  *
                  * for (double rx = 0; rx < 1.0; rx += incrMax) { for (double ry
                  * = 0; ry < 1.0; ry += incrMax) { testPoint(to.getPoint3D(tris,
                  * numX, numY, rx, ry)); } }
                  *
                  * }
                  * }
                  */
             } else if (r instanceof PGeneratorZ) {
                 PGeneratorZ p = (PGeneratorZ) r;
                 p.generate(this);
                 p.dessine(this);
             }
         }
 
     }
 
     public void dessinerSilhouette3DPerspective(Scene scene) {
         id++;
         boxPerspect = new Box2DPerspective(scene);
         Iterator<Representable> it = scene.iterator();
         while (it.hasNext()) {
             Representable r = it.next();
 
             // GENERATORS
             if (r instanceof TRIGenerable) {
                 r = ((TRIGenerable) r).generate();
             } else if (r instanceof PGenerator) {
                 r = ((PGenerator) r).generatePO();
             }
             if (r instanceof TRIConteneur) {
                 r = ((TRIConteneur) r).getObj();
             }
 
             // OBJETS
             if (r instanceof TRIObject) {
                 TRIObject o = (TRIObject) r;
                 Iterator<TRI> ts = o.triangles.iterator();
                 while (ts.hasNext()) {
                     // System.out.println("Triangle suivant");
 
                     TRI t = ts.next();
 
                     tracerTriangle(t.getSommet()[0], t.getSommet()[1],
                             t.getSommet()[2], t.getCouleur());
 
                 }
             } else if (r instanceof Point3D) {
                 Point3D p = (Point3D) r;
                 ime.testProf(p, p.getC());
             } else if (r instanceof BSpline) {
                 BSpline b = (BSpline) r;
                 int nt = 100;
                 for (double i = 0; i < nt; i++) {
                     try {
                         Point3D p3d = b.calculerPoint3D(3.0 + 1.0 * nt / 200);
                         ime.testProf(p3d, b.getColor());
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             } else if (r instanceof BezierCubique) {
                 BezierCubique b = (BezierCubique) r;
                 int nt = largeur() / 10;
                 Point3D p0 = b.calculerPoint3D(0.0);
                 for (double t = 0; t < 1.0; t += 1.0 / nt) {
                     try {
                         Point3D p1 = b.calculerPoint3D(t);
                         ligne(p0, p1, b.getColor());
                         p0 = p1;
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
 
             } else if (r instanceof PObjet) {
                 PObjet b = (PObjet) r;
                 for (Point3D p : b.getPoints()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof POConteneur) {
                 POConteneur c = (POConteneur) r;
                 for (Point3D p : c.iterable()) {
                     ime.testProf(p, p.getC());
                 }
             } else if (r instanceof TRIConteneur) {
                 for (TRI t : ((TRIConteneur) r).iterable()) {
                     tracerTrianglePerspect(t.getSommet()[0], t.getSommet()[1],
                             t.getSommet()[2], t.getCouleur());
 
                 }
 
             } else if (r instanceof PGeneratorZ) {
                 PGeneratorZ p = (PGeneratorZ) r;
                 p.generate(this);
                 p.dessine(this);
             }
         }
 
     }
 
     /**
      * @param p
      * @return
      *//*
      * private java.awt.Point coordonneesPointEcranPerspect(Point3D p) {
      *
      * return new java.awt.Point((int)(la / 2 - p.getX() / (boxPerspect.d -
      * p.getZ()) / boxPerspect.w * la), (int)(ha / 2 - p.getY() / (boxPerspect.d
      * - p.getZ()) / boxPerspect.h * ha)); }
      */
 
     private void tracerAretes(Point3D point3d, Point3D point3d2, Color c) {
         Point p1 = coordonneesPointEcran(point3d);
         Point p2 = coordonneesPointEcran(point3d2);
 
         // System.out.println("Tracer arete");
         // System.out.println("p1 : x " + p1.getX() + " y " + p1.getY());
         // System.out.println("p2 : x " + p2.getX() + " y " + p2.getY());
         double iteres = Math.abs(p1.getX() - p2.getX())
                 + Math.abs(p1.getY() - p2.getY());
         for (double a = 0; a < 1.0; a += 1 / iteres) {
             // System.out.println("a = " + a);
             Point pp = new Point(p1);
             Point3D p = point3d.mult(a).plus(point3d2.mult(1 - a));
             pp.setLocation(p1.getX() + (int) (a * (p2.getX() - p1.getX())),
                     p1.getY() + (int) (a * (p2.getY() - p1.getY())));
             // System.out.println("pp :  x " + pp.getX() + "  y " + pp.getY());
             //plotPoint(pp);
             ime.testProf(p, c);
 
         }
     }
 
     /**
      * @param point3d
      * @param point3d2
      * @param point3d3
      */
     private void tracerTrianglePerspect(Point3D point3d, Point3D point3d2,
             Point3D point3d3, Color c) {
         /*
          * pi = new TrianglePix(); int ymin = tpix.getMinY(); int ymax =
          * tpix.getMaxY();
          *
          * for (int y = ymin; y < ymax; y++) { int xmin = tpix.getMinX(y); int
          * xmax = tpix.getMaxX(y); // System.out.println(". y min : " + ymin + "
          * max : " + ymax // + "XX min :" + xmin + "max :" + xmax); if (xmin !=
          * -1 && xmax != -1) { for (int x = xmin; x <= xmax; x++) {
          * ime.dessine(new Point3D(), new Point(x, y), couleurCourante); //
          * System.out.println("."); } } }
          */
     }
     //TrianglePix pi;
     private BufferedImage bi;
     private ArrayList<Representable> repr;
     private Perspective perspective = Perspective.getInstance(Perspective.P_CUBIQUE_LINEAIRE);
     /*
      * public void tracerTriangle3D(TrianglePix tpix) { int ymin =
      * tpix.getMinY(); int ymax = tpix.getMaxY();
      *
      * for (int y = ymin; y < ymax; y++) { int xmin = tpix.getMinX(y); int xmax
      * = tpix.getMaxX(y); tpix.calculer3D(y); Point3D pp1 = tpix.get3DXMin();
      * Point3D pp2 = tpix.get3DXMin(); double itere = 0.25 / (Math.abs(xmax -
      * xmin)); if (xmin != -1 && xmax != -1 && xmin < xmax) { for (double a = 0;
      * a < 1; a += itere) { // int x = (int) (xmin + a * (xmax - xmin)); Point3D
      * pp = pp1.plus(pp1.mult(-1).plus(pp2).mult(a)); ime.testProf(pp,
      * coordonneesPointEcran(pp), couleurCourante); } } } }
      */
     /*
      * public void tracerArete(Point3D pp1, Point3D pp2) { Point p1 =
      * coordonneesPointEcran(pp1); Point p2 = coordonneesPointEcran(pp2); double
      * iteres = 0.25 / (Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() -
      * p2.getY())); for (double a = 0; a < 1.0; a += iteres) { Point3D p3d =
      * pp1.plus(pp1.mult(-1).plus(pp2).mult(a)); Point pp =
      * coordonneesPointEcran(p3d)/* new Point( (int) (p1.getX() + (a *
      * (p2.getX() - p1.getX()))), (int) (p1.getY() + (a * (p2.getY() -
      * p1.getY())))) ; pi.put((int) pp.getY(), pi.new PP(pp, p3d)); //
      * System.out.println(p3d.getZ()); }
      *
      * }
      */
 
     public void tracerTriangle(Point3D pp1, Point3D pp2, Point3D pp3, Color c) {
         Point p1, p2, p3;
         if (type_perspective == PERSPECTIVE_ISOM) {
             p1 = coordonneesPointEcran(pp1);
             p2 = coordonneesPointEcran(pp2);
             p3 = coordonneesPointEcran(pp3);
 
         } else {
             p1 = coordonneesPointEcranPerspective(pp1);
             p2 = coordonneesPointEcranPerspective(pp2);
             p3 = coordonneesPointEcranPerspective(pp3);
 
         }
 
         double iteres1 = 1.0 / (Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY()));
         for (double a = 0; a < 1.0; a += iteres1) {
             Point3D p3d = pp1.plus(pp1.mult(-1).plus(pp2).mult(a));
             Point pp = coordonneesPointEcran(p3d);
             double iteres2 = 1.0 / (Math.abs(pp.getX() - p3.getX()) + Math.abs(pp.getY() - p3.getY()));
             for (double b = 0; b < 1.0; b += iteres2) {
                 Point3D p = p3d.plus(p3d.mult(-1).plus(pp3).mult(b));
                 Point p22 = coordonneesPointEcran(p);
                 ime.testProf(p, c);
                 // System.out.println("TRIABGLE"+p.getZ());
             }
         }
 
     }
 
     public void tracerQuad(Point3D pp1, Point3D pp2, Point3D pp3, Color c) {
         Point p1 = coordonneesPointEcran(pp1);
         Point p2 = coordonneesPointEcran(pp2);
         Point p3 = coordonneesPointEcran(pp3);
         double iteres1 = 1.0 / (Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY()));
         for (double a = 0; a < 1.0; a += iteres1) {
             Point3D p3d = pp1.plus(pp1.mult(-1).plus(pp2).mult(a));
             Point pp = coordonneesPointEcran(p3d);
             double iteres2 = 1.0 / (Math.abs(pp.getX() - p3.getX()) + Math.abs(pp.getY() - p3.getY()));
             for (double b = 0; b < 1.0; b += iteres2) {
                 Point3D p = p3d.plus(p3d.mult(-1).plus(pp3).mult(b));
                 Point p22 = coordonneesPointEcran(p);
                 ime.testProf(p, c);
                 // System.out.println("TRIABGLE"+p.getZ());
             }
         }
 
     }
 
     @Override
     public BufferedImage image() {
         BufferedImage bi2 = new BufferedImage(la, ha, BufferedImage.TYPE_INT_RGB);
         for (int i = 0; i < la; i++) {
             for (int j = 0; j < ha; j++) {
                 Graphics g = bi2.getGraphics();
                 g.setColor(ime.getIME().getElementCouleur(i, j));
                 g.drawLine(i, j, i, j);
             }
         }
         System.out.println("NEW IMAGE");
         this.bi = bi2;
         return bi2;
 
     }
 
     public Graphics postPaint() {
         return bi.getGraphics();
     }
 
     public double[][] map() {
         double[][] Map = new double[la][ha];
         for (int i = 0; i < la; i++) {
             for (int j = 0; j < ha; j++) {
                 if (ime.getIME().getElementPoint(i, j) != null) {
                     Map[i][j] = ime.getIME().getElementPoint(i, j).getZ();
                 } else {
                     Map[i][j] = -2000;
                 }
             }
         }
         return Map;
 
     }
 
     public ZBufferInfo getInfos() {
         return infos;
     }
 
     /**
      * @param p
      * @return
      */
     public java.awt.Point point(Point3D p0) {
         return perspective.coordonneeEcran(p0);
     }
 
     public void plotPoint(Color color, Point3D p) {
         ime.testProf(p, coordonneesPointEcran(p), color);
     }
 
     /**
      * @return
      */
     public int hauteur() {
         return ha;
     }
 
     /**
      * @return
      */
     public int largeur() {
         return la;
     }
 
     /**
      * @param perspective
      */
     public void perspective(Perspective p) {
         this.perspective = p;
 
     }
 
     public void ligne(Point3D p1, Point3D p2, Color c) {
         Point x1 = coordonneesPointEcran(p1);
         Point x2 = coordonneesPointEcran(p2);
         double itere = Math.max(Math.abs(x1.getX() - x2.getX()), Math.abs(x1.getY() - x2.getY())) * 4;
         for (int i = 0; i < itere; i++) {
             Point3D p = p1.mult(i / itere).plus(p2.mult(1 - i / itere));
             p.setC(c);
             ime.testProf(p, c);
         }
     }
 
     @Override
     public void plotPoint(Point3D p, Color c) {
         ime.dessine(p, c);
     }
 
     public void plotPoint(Point3D p) {
         ime.dessine(p, p.getC());
     }
 
     @Override
     public void testPoint(Point3D p, Color c) {
         ime.testProf(p, c);
     }
 
     @Override
     public void testPoint(Point3D p) {
         ime.testProf(p, p.getC());
     }
 
     @Override
     public Point coordonneesPointEcranPerspective(Point3D x3d) {
         x3d = cameraC.calculerPointDansRepere(x3d);
         planproj = cameraC.calculerPointDansRepere(cameraC.pointFocal());
         camera = cameraC.calculerPointDansRepere(cameraC.position());
         double scale = ((planproj.getZ()) / (x3d.getZ()));
         return new Point(
                 (int) ((x3d.getX())
                / (box.getMaxx() - box.getMinx()) * la
                 * scale
                 + la / 2),
                 (int) ((x3d.getY())
                / (box.getMaxy() - box.getMiny()) * ha
                 * scale
                 + ha / 2));
     }
 }
