 package com.nebula2d.editor.ui;
 import com.badlogic.gdx.Gdx;
 import com.nebula2d.editor.framework.Project;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 public class MainFrame extends JFrame {
 
     //region static members
     private static RenderCanvas renderCanvas = new RenderCanvas(new RenderAdapter());
     private static SceneGraph sceneGraph = new SceneGraph();
     private static N2DToolbar toolbar = new N2DToolbar();
     private static N2DMenuBar menuBar;
     private static Project project;
     //endregion
 
     //region constructor
     public MainFrame() {
         super("Nebula2D");
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         JScrollPane sp = new JScrollPane(sceneGraph);
         sp.setPreferredSize(new Dimension(300, 600));
 
         getContentPane().add(toolbar, BorderLayout.NORTH);
         getContentPane().add(sp, BorderLayout.WEST);
         getContentPane().add(renderCanvas.getCanvas());
 
         sceneGraph.setEnabled(false);
         renderCanvas.setEnabled(false);
 
         menuBar = new N2DMenuBar();
         setJMenuBar(menuBar);
 
         setSize(1200, 768);
         setVisible(true);
     }
     //endregion
 
     //region static accessors
     public static RenderCanvas getRenderCanvas() {
         return renderCanvas;
     }
 
     public static SceneGraph getSceneGraph() {
         return sceneGraph;
     }
 
     public static Project getProject() {
         return project;
     }
 
     public static N2DMenuBar getN2DMenuBar() {
         return menuBar;
     }
 
     public static N2DToolbar getToolbar() {
         return toolbar;
     }
 
     public static void setProject(Project project) {
         MainFrame.project = project;
         sceneGraph.setEnabled(true);
         renderCanvas.setEnabled(true);
         menuBar.getSceneMenu().setEnabled(true);
         toolbar.setRendererWidgetsEnabled(true);
     }
     //endregion
 }
