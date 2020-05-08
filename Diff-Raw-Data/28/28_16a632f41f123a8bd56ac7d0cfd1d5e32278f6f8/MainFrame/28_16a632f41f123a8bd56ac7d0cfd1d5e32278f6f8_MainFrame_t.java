 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.editor.ui;

 import com.badlogic.gdx.Gdx;
 import com.nebula2d.editor.framework.Project;
 import com.nebula2d.editor.framework.assets.AssetManager;
 import com.nebula2d.editor.settings.N2DSettings;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 
 public class MainFrame extends JFrame {
     private static MainFrame instance;
     private static RenderCanvas renderCanvas = new RenderCanvas(new RenderAdapter());
     private static SceneGraph sceneGraph = new SceneGraph();
     private static N2DToolbar toolbar = new N2DToolbar();
     private static N2DMenuBar menuBar;
     private static Project project;
     private static N2DSettings settings = new N2DSettings();
 
     public MainFrame() {
         super("Nebula2D");
         instance = this;
         try {
             settings.loadFromProperties();
         } catch (IOException e) {
             JOptionPane.showMessageDialog(this, "Failed to load configuration.");
             System.exit(1);
         }
         setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
 
         validate();
         setLocationRelativeTo(null);
         setVisible(true);
         renderCanvas.initCamera(renderCanvas.getCanvas().getWidth(), renderCanvas.getCanvas().getHeight());
 
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 if (JOptionPane.showConfirmDialog(getParent(), "Are you sure you want to exit?", "Are you sure?",
                         JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 
                     try {
                         settings.saveProperties();
                     } catch (IOException e1) {
                         JOptionPane.showMessageDialog(MainFrame.this, "Failed to save settings.");
                     }
                     AssetManager.getInstance().cleanup();

                    Gdx.app.postRunnable(() -> {
                        renderCanvas.stop();
                        SwingUtilities.invokeLater(MainFrame.this::dispose);
                    });

//                    Gdx.app.exit();
//                    dispose();
                 }
             }
         });
     }
 
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
 
     public static N2DSettings getSettings() {
         return settings;
     }
 
     public static void setProject(Project project) {
         MainFrame.project = project;
         sceneGraph.setEnabled(project != null);
         renderCanvas.setEnabled(project != null);
         menuBar.getBuildMenuItem().setEnabled(project != null);
         menuBar.getSceneMenu().setEnabled(project != null);
         toolbar.setRendererWidgetsEnabled(project != null);
         menuBar.getSaveMenuItem().setEnabled(project != null);
 
         if (project != null) {
             sceneGraph.init();
             instance.setTitle("Nebula2D - " + project.getNameWithoutExt());
         } else {
             instance.setTitle("Nebual2D");
         }
     }
 }
