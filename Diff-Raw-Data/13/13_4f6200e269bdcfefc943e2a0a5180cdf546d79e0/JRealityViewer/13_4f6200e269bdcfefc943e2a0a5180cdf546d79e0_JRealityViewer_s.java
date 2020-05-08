 package de.tum.in.jrealityplugin;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import de.jreality.geometry.PointSetFactory;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.ui.viewerapp.ViewerApp;
 
 @SuppressWarnings("deprecation")
 public class JRealityViewer implements Cindy3DViewer {
     private ViewerApp viewer;
     
     private SceneGraphComponent sceneRoot; 
     
     // Point resources
     private SceneGraphComponent scenePoints;
     private PointSetFactory psf;
     private ArrayList<double[]> pointCoordinates;
     private ArrayList<Color> pointColors;
     
     public JRealityViewer() {
     	pointCoordinates = new ArrayList<double[]>();
     	pointColors = new ArrayList<Color>();
     	
         psf = new PointSetFactory();
 
         sceneRoot = new SceneGraphComponent();
         
         scenePoints = new SceneGraphComponent();
         scenePoints.setGeometry(psf.getGeometry());
         
         sceneRoot.addChild(scenePoints);
 
         viewer = new ViewerApp(sceneRoot);
         viewer.setAttachBeanShell(false);
         viewer.setAttachNavigator(true);
         viewer.setShowMenu(true);
         viewer.setBackgroundColor(Color.BLACK);
         viewer.update();
 
         viewer.getFrame().setSize(600, 600);
     }
 
     @Override
     public void begin() {
     	pointCoordinates.clear();
     	pointColors.clear();
     }
 
     @Override
     public void end() {
    	psf.setVertexCoordinates((double[][])pointCoordinates.toArray());
    	
    	psf.setVertexCount(pointCoordinates.size());
    	psf.update();
         viewer.getFrame().setVisible(true);
     }
 
     @Override
     public void addPoint(double x, double y, double z) {
     	pointCoordinates.add(new double[] {x, y, z});
     }
 
     @Override
     public void shutdown() {
         viewer.getFrame().setVisible(false);
     }
 
 }
