 /*
  * FirstTest.java
  *
  * Created on 11. Februar 2005, 16:26
  */
 
 package de.cismet.tools.mapping;
 
 
 import java.awt.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.*;
 import java.util.*;
 
 import org.postgis.*;
 
 import edu.umd.cs.piccolox.event.*;
 import edu.umd.cs.piccolo.*;
 import edu.umd.cs.piccolo.nodes.*;
 import edu.umd.cs.piccolox.util.*;
 import edu.umd.cs.piccolo.util.*;
 
import org.deegree_impl.graphics.transformation.WorldToScreenTransform;
 
 /**
  *
  * @author  hell
  */
 public class FirstTest extends javax.swing.JFrame {
 //    PCanvas pc=new PCanvas();
 //         double y_screen=400;
 //        double x_screen=400;
 //        Vector pols=new Vector();
 //        
 //   /** Creates new form FirstTest */
 //    public FirstTest() {
 //        pc.setPreferredSize(new Dimension((int)x_screen,(int)y_screen));
 //        getContentPane().setLayout(new BorderLayout());
 //        getContentPane().add("Center", pc);
 //        initComponents();
 //        showPolygons();
 //
 //        
 //        pc.removeInputEventListener(pc.getPanEventHandler());
 //        pc.removeInputEventListener(pc.getZoomEventHandler());
 //
 //        try {
 //            Connection conn;
 //            System.out.println("Creating JDBC connection...");
 //            Class.forName("org.postgresql.Driver");
 //            String url = "jdbc:postgresql://flexo.cismet.de:5432/verdis_beta";
 //            conn = DriverManager.getConnection(url, "postgres","x");
 //            System.out.println("Adding geometric type entries...");
 //
 //            ((org.postgresql.PGConnection)conn).addDataType("geometry","org.postgis.PGgeometry");
 //            ((org.postgresql.PGConnection)conn).addDataType("box3d","org.postgis.PGbox3d");
 //
 //            Statement s = conn.createStatement();
 //            System.out.println("Creating table with geometric types...");
 //
 //            System.out.println("Querying table...");
 //            ResultSet r = s.executeQuery("select geo_field,geom.id from flaechen,flaeche,flaecheninfo,geom where flaechen.flaeche=flaeche.id and flaeche.flaecheninfo=flaecheninfo.id and flaecheninfo.geometrie=geom.id and kassenzeichen_reference=6000467");
 //            //ResultSet r = s.executeQuery("select geo_field,geom.id from geom where id<3");
 //            while( r.next() ) 
 //            {
 //                    PGgeometry mp=(PGgeometry)r.getObject(1);
 //                    System.out.println(mp);
 //                    MultiPolygon pp=(MultiPolygon)mp.getGeometry();
 //                     pols.add(pp.getPolygon(0));
 ////                    pp.getPolygon(1).getRing(1).getPoint(1).getX();
 //                    
 //                    System.out.println(pp);
 //            }
 //            s.close();
 //            conn.close();
 //        }
 //        catch( Exception e ) {
 //                e.printStackTrace();
 //        }
 //
 //
 //        // Create a selection event handler
 //        PSelectionEventHandler selectionEventHandler = new PSelectionEventHandler(pc.getLayer(), pc.getLayer());
 //        pc.addInputEventListener(selectionEventHandler);
 //        pc.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);
 //
 //        PNotificationCenter.defaultCenter().addListener(this, 
 //                                                               "selectionChanged", 
 //                                                               PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION, 
 //                                                               selectionEventHandler);
 //       
 //        
 //    }
 //    
 //    
 //     
 //    private void showPolygons() {
 //        pc.getLayer().removeAllChildren();
 //        PNode root =new PNode();
 //        this.x_screen=pc.getWidth();
 //        this.y_screen=pc.getHeight();
 //
 //        
 //        Iterator it=pols.iterator();
 //        while (it.hasNext()) {
 //            org.postgis.Polygon poly=(org.postgis.Polygon)it.next();
 //            org.postgis.Point[] p=poly.getRing(0).points;
 //            float[] xp=new float[p.length];
 //            float[] yp=new float[p.length];
 //            for (int i=0; i<p.length;++i) {
 //                xp[i]=(float)p[i].getX();
 //                yp[i]=(float)p[i].getY();
 //            }
 //        
 //            PPath pp=PPath.createPolyline(xp,yp);
 //            root.addChild(pp);
 //        }
 //        
 //        
 //        System.out.println(root.getFullBounds());
 //        
 //        PBounds ext=root.getFullBounds();
 //
 //        double y_real=ext.getY()+ext.getHeight();
 //        double x_real=ext.getX()+ext.getWidth();
 //        
 //        double clip_height;
 //        double clip_width;
 //        double clip_offset_x;
 //        double clip_offset_y;
 //                
 //        
 //        if (x_real/x_screen>=y_real/y_screen) { //X ist Bestimmer d.h. x wird nicht ver\u00E4ndert
 //            // X ist Bestimmer
 //            clip_height=x_screen*y_real/x_real;
 //            clip_width=x_screen;
 //            clip_offset_y=(y_screen-clip_height)/2;
 //            clip_offset_x=0;
 //        }
 //        else {
 //            // Y ist Bestimmer
 //            clip_height=y_screen;
 //            clip_width=y_screen*x_real/y_real;
 //            clip_offset_y=0;
 //            clip_offset_x=(x_screen-clip_width)/2;
 //        }
 //        
 //        System.out.println("WorldToScreenTransform: "+ext.getX()+","+ext.getY()+","+x_real+","+y_real);
 //        
 //        WorldToScreenTransform wtst= new WorldToScreenTransform(ext.getX(),ext.getY(),x_real,y_real,0,0,clip_width,clip_height);
 //        
 //        it=pols.iterator();
 //        while (it.hasNext()) {
 //            org.postgis.Polygon poly=(org.postgis.Polygon)it.next();
 //            org.postgis.Point[] p=poly.getRing(0).points;
 //            float[] xp=new float[p.length];
 //            float[] yp=new float[p.length];
 //            for (int i=0; i<p.length;++i) {
 //                xp[i]=(float)(wtst.getDestX(p[i].getX())+clip_offset_x);
 //                yp[i]=(float)(wtst.getDestY(p[i].getY())+clip_offset_y);
 //            }
 //        
 //            PPath pp=PPath.createPolyline(xp,yp);
 //            pp.setPaint(Color.blue);
 //            pc.getLayer().addChild(pp);
 //        }
 //        System.out.println("Clip:"+clip_width+","+clip_height);
 //        System.out.println("BB:"+wtst.getSourceX(0)+","+wtst.getSourceY(clip_height)+","+wtst.getSourceX(clip_width)+","+wtst.getSourceY(0));
 //       
 //        
 //       pc.repaint();
 //    }
 //    
 //    
 //   
 //    
 //    /** This method is called from within the constructor to
 //     * initialize the form.
 //     * WARNING: Do NOT modify this code. The content of this method is
 //     * always regenerated by the Form Editor.
 //     */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         jPanel1 = new javax.swing.JPanel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 formComponentResized(evt);
             }
         });
         addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 formMouseClicked(evt);
             }
         });
 
         getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
 
         pack();
     }
     // </editor-fold>//GEN-END:initComponents
 //
     private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
 //showPolygons();        // TODO add your handling code here:
     }//GEN-LAST:event_formComponentResized
 
     private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        
     }//GEN-LAST:event_formMouseClicked
     
 //    /**
 //     * @param args the command line arguments
 //     */
 //    public static void main(String args[]) {
 //        java.awt.EventQueue.invokeLater(new Runnable() {
 //            public void run() {
 //                new FirstTest().setVisible(true);
 //            }
 //        });
 //    }
 //    
 //    public void selectionChanged(PNotification notfication) {
 //            System.out.println("selection changed");
 //    }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel jPanel1;
     // End of variables declaration//GEN-END:variables
     
     
 }
