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
 import be.ibiiztera.pmatrix.extras.RepresentableConteneur;
 import java.awt.*;
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
     private Camera cameraC = new Camera();
     private BufferedImage bi;
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
     private Scene scene1;
 
     @Override
     public void suivante() {
         id++;
     }
 
     {
 
         COULEUR_FOND = new Color(255, 255, 255, 255);
 
     }
 
     @Override
     public void isometrique() {
         type_perspective = PERSPECTIVE_ISOM;
     }
     public void perspective() {
         type_perspective = PERSPECTIVE_OEIL;
     }
 
     @Override
     public void camera(Camera c) {
         this.cameraC = c;
         reglerCamera(c.angleX(), c.angleY());
     }
 
     @Override
     public Camera camera() {
         return this.cameraC;
     }
 
     private void reglerCamera(double angleX, double angleY) {
         //
     }
 
     @Override
     public Point coordonneesPoint2D(Point3D p)  {
         switch (type_perspective) {
             case PERSPECTIVE_ISOM:
                 return coordonneesPointEcranIsometrique(p);
             case PERSPECTIVE_OEIL:
                 return coordonneesPointEcranPerspective(p);
             default:
                 throw new UnsupportedOperationException("Type de perspective non reconnu");
         }
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
             double prof = distanceCamera(x3d);
             if (x >= 0 & x < la & y >= 0 & y < ha
                     & prof < ime.getElementProf(x, y)) {
                 ime.setElementID(x, y, id);
                 ime.setElementPoint(x, y, x3d);
                 ime.setElementCouleur(x, y, c);
                 ime.setProf(x, y, prof);
             }
 
         }
 
         public void testProf(Point3D x3d, Color c) {
             
                 Point ce = coordonneesPoint2D(x3d);
                 if(ce==null) return;
                 double prof = distanceCamera(x3d);
 
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
                 Point ce = coordonneesPoint2D(x3d);
                 if(ce==null) return;
                 double prof = -1000;
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
     private ImageMap ime;
     private Box2D box;
     private int ha;
     private int la;
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
 
     public double echelleEcran() {
         return box.echelleEcran();
     }
 
     public Image rendu() {
         return null;
     }
 
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
 
                     Point x1 = coordonneesPoint2D(s.getOrigine());
                     Point x2 = coordonneesPoint2D(s.getExtremite());
                     if(x1!=null && x2!=null)
                     {
                     double x = Math.max(x1.getX(), x2.getX());
                     double y = Math.max(x1.getY(), x2.getY());
 
                     double itere = Math.max(x, y) * 4;
                     for (int i = 0; i < itere; i++) {
                         Point3D p = s.getOrigine().mult(i / itere).plus(s.getExtremite().mult(1 - i / itere));
                         p.setC(s.getC());
                         ime.testProf(p, p.getC());
                     }
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
                     Point x1 = coordonneesPoint2D(s.getOrigine());
                     Point x2 = coordonneesPoint2D(s.getExtremite());
                     if(x1!=null && x2!=null)
                     {
                     double x = Math.max(x1.getX(), x2.getX());
                     double y = Math.max(x1.getY(), x2.getY());
 
                     double itere = Math.max(x, y) * 4;
                     for (int i = 0; i < itere; i++) {
                         Point3D p = s.getOrigine().mult(i / itere).plus(s.getExtremite().mult(1 - i / itere));
                         p.setC(s.getC());
                         ime.testProf(p, p.getC());
                     }
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
 
     public void dessinerSilhouette3D(Representable re) {
         
 
         Iterator<Representable> it = null;
         // COLLECTION
         if (re instanceof RepresentableConteneur) {
             RepresentableConteneur name = (RepresentableConteneur) re;
             it = name.getListRepresentable().iterator();
             while (it.hasNext()) {
                 dessinerSilhouette3D(it.next());
             }
         } else if (re instanceof Scene) {
             Scene scene = (Scene) re;
             it = scene.iterator();
             while (it.hasNext()) {
                 dessinerSilhouette3D(it.next());
             }
         } else if (re != null) {
             Representable r = re;
 
 
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
             } else if (r instanceof PGeneratorZ) {
                 PGeneratorZ p = (PGeneratorZ) r;
                 p.generate(this);
                 p.dessine(this);
             }
 
         }
 
     }
 
     @Override
     public void dessinerSilhouette3D() {
         id++;
         if (type_perspective == PERSPECTIVE_ISOM) {
             box = new Box2D();
         }
         dessinerSilhouette3D(scene1);
     }
 
     private void tracerAretes(Point3D point3d, Point3D point3d2, Color c) {
             Point p1 = coordonneesPoint2D(point3d);
             Point p2 = coordonneesPoint2D(point3d2);
             if(p1==null || p2==null)
                 return;
             double iteres = Math.abs(p1.getX() - p2.getX())
                     + Math.abs(p1.getY() - p2.getY());
             for (double a = 0; a < 1.0; a += 1 / iteres) {
                 Point pp = new Point(p1);
                 Point3D p = point3d.mult(a).plus(point3d2.mult(1 - a));
                 pp.setLocation(p1.getX() + (int) (a * (p2.getX() - p1.getX())),
                         p1.getY() + (int) (a * (p2.getY() - p1.getY())));
                 ime.testProf(p, c);
 
 
             }
 
     }
 
     private double maxDistance(Point p1, Point p2, Point p3) {
         return Math.max(
                 Math.max(
                 Point.distance(p1.x, p1.y, p2.x, p2.y),
                 Point.distance(p2.x, p2.y, p3.x, p3.y)),
                 Point.distance(p3.x, p3.y, p1.x, p1.y));
     }
 
     public void tracerTriangle(Point3D pp1, Point3D pp2, Point3D pp3, Color c) {
         Point p1, p2, p3;
             p1 = coordonneesPoint2D(pp1);
             p2 = coordonneesPoint2D(pp2);
             p3 = coordonneesPoint2D(pp3);
             if(p1==null || p2==null || p3==null)
                 return;
             double iteres1 = 1.0 / maxDistance(p1, p2, p3);
             for (double a = 0; a < 1.0; a += iteres1) {
                 Point3D p3d = pp1.plus(pp1.mult(-1).plus(pp2).mult(a));
                 double iteres2 = 1.0 / maxDistance(p1, p2, p3);
                 for (double b = 0; b < 1.0; b += iteres2) {
                     Point3D p = p3d.plus(p3d.mult(-1).plus(pp3).mult(b));
                     ime.testProf(p, c);
                 }
             }
     }
 
     public void tracerQuad(Point3D pp1, Point3D pp2, Point3D pp3, Color c) {
             Point p1 = coordonneesPoint2D(pp1);
             Point p2 = coordonneesPoint2D(pp2);
             Point p3 = coordonneesPoint2D(pp3);
             if(p1==null || p2==null || p3==null)
                 return;
             double iteres1 = 1.0 / (Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY()));
             for (double a = 0; a < 1.0; a += iteres1) {
                 Point3D p3d = pp1.plus(pp1.mult(-1).plus(pp2).mult(a));
                 Point pp = coordonneesPoint2D(p3d);
                 double iteres2 = 1.0 / (Math.abs(pp.getX() - p3.getX()) + Math.abs(pp.getY() - p3.getY()));
                 for (double b = 0; b < 1.0; b += iteres2) {
                     Point3D p = p3d.plus(p3d.mult(-1).plus(pp3).mult(b));
                     Point p22 = coordonneesPoint2D(p);
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
 
     public void plotPoint(Color color, Point3D p) {
         if(p!=null)
             ime.testProf(p, coordonneesPoint2D(p), color);
 
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
 
     public void ligne(Point3D p1, Point3D p2, Color c) {
             Point x1 = coordonneesPoint2D(p1);
             Point x2 = coordonneesPoint2D(p2);
             if(x1==null || x2==null)
                 return;
 
             double itere = Math.max(Math.abs(x1.getX() - x2.getX()), Math.abs(x1.getY() - x2.getY())) * 4;
             for (int i = 0; i < itere; i++) {
                 Point3D p = p1.mult(i / itere).plus(p2.mult(1 - i / itere));
                 p.setC(c);
                 ime.testProf(p, c);
             }
 
     }
 
     @Override
     public void plotPoint(Point3D p, Color c) {
         if(p!=null)
             ime.dessine(p, c);
     }
 
     public void plotPoint(Point3D p) {
         if(p!=null)
         ime.dessine(p, p.getC());
     }
 
     @Override
     public void testPoint(Point3D p, Color c) {
         if(p!=null)
         ime.testProf(p, c);
     }
 
     @Override
     public void testPoint(Point3D p) {
         if(p!=null)
         ime.testProf(p, p.getC());
     }
    protected double angleX = Math.PI / 6 / 2;
    protected double angleY = Math.PI / 6 / 2;
 
     public void setAngles(double angleXRad, double angleYRad) {
         this.angleX = angleXRad;
         this.angleY = angleYRad;
     }
 
     protected Point coordonneesPointEcranPerspective(Point3D x3d) {
         x3d = cameraC.calculerPointDansRepere(x3d);
        //camera = cameraC.calculerPointDansRepere(cameraC.position());
        //if (x3d.getZ() > planproj.getZ() && planproj.getZ() > 0) {
		if (x3d.getZ() > 0 && -angleX<Math.atan(x3d.getX()/x3d.getZ()) && Math.atan(x3d.getX()/x3d.getZ())<angleX &&
				-angleY<Math.atan(x3d.getY()/x3d.getZ()) && Math.atan(x3d.getY()/x3d.getZ())<angleY
		) {
            double scale = (1 / (x3d.getZ()));
             return new Point(
                     (int) (x3d.getX() * scale * angleX
                     * la
                     + la / 2),
                     (int) (x3d.getY() * scale * angleY
                     * ha
                     + ha / 2));
         }
         return null;
     }
 
     protected Point coordonneesPointEcranIsometrique(Point3D p) {
         p = cameraC.calculerPointDansRepere(p);
         java.awt.Point p2 = new java.awt.Point(
                 (int) (la / (box.getMaxx() - box.getMinx()) * (p.getX() - box.getMinx())),
                 ha - (int) (ha / (box.getMaxy() - box.getMiny()) * (p.getY() - box.getMiny())));
         if (p2.getX() >= 0.0 && p2.getX() < la && p2.getY() >= 0 && p2.getY() < ha) {
             return p2;
         } else {
             return null;
         }
     }
 
     @Override
     public double distanceCamera(Point3D x3d) {
         switch (type_perspective) {
             case PERSPECTIVE_ISOM:
                 return x3d.getZ();
             case PERSPECTIVE_OEIL:
                return x3d.norme();
             default:
                 throw new UnsupportedOperationException("Type de perspective non reconnu");
         }
 
     }
 }
