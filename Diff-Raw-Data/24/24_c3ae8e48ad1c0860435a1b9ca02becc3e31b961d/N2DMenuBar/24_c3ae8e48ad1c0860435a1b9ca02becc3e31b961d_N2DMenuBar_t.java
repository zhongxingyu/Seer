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
 
 import com.nebula2d.editor.framework.BaseSceneNode;
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.framework.Layer;
 import com.nebula2d.editor.framework.Project;
 import com.nebula2d.editor.util.ExitAction;
 import com.nebula2d.editor.util.PlatformUtil;
 import com.nebula2d.editor.util.ProjectBuilder;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.io.IOException;
 
 
 /**
  * Custom JMenuBar implementation
  */
 public class N2DMenuBar extends JMenuBar {
 
     private JMenu sceneMenu;
     private JMenu gameObjectMenu;
 
     private JMenuItem newMenuItem;
     private JMenuItem saveMenuItem;
     private JMenuItem openMenuItem;
     private JMenuItem settingsMenuItem;
     private JMenuItem buildMenuItem;
 
     private JMenuItem newSceneMenuItem;
     private JMenuItem changeSceneMenuItem;
     private JMenuItem renameSceneMenuItem;
     private JMenuItem newLayerMenuItem;
 
     private JMenuItem newEmptyGameObjectMenuItem;
 
     public N2DMenuBar() {
         JMenu fileMenu = new JMenu("File");
 
         newMenuItem = fileMenu.add("New Project");
         saveMenuItem = fileMenu.add("Save");
         saveMenuItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
         saveMenuItem.setEnabled(false);
         openMenuItem = fileMenu.add("Open Project");
         settingsMenuItem = fileMenu.add("Settings");
         buildMenuItem = fileMenu.add("Build");
 
         setBorder(BorderFactory.createEmptyBorder());
         //Don't need exit menu item on Mac
         if (!PlatformUtil.isMac())
             fileMenu.add(new ExitAction());
 
         sceneMenu = new JMenu("Scene");
 
         newSceneMenuItem = sceneMenu.add("New Scene");
         changeSceneMenuItem = sceneMenu.add("Change Scene");
         renameSceneMenuItem = sceneMenu.add("Rename Scene");
         newLayerMenuItem = sceneMenu.add("New Layer");
 
         gameObjectMenu = new JMenu("New GameObject");
 
         newEmptyGameObjectMenuItem = gameObjectMenu.add("Empty GameObject");
 
         sceneMenu.add(gameObjectMenu);
 
         buildMenuItem.setEnabled(false);
         gameObjectMenu.setEnabled(false);
         sceneMenu.setEnabled(false);
         add(fileMenu);
         add(sceneMenu);
         bindMenuItems();
     }
 
     private void bindMenuItems() {
         newMenuItem.addActionListener(e -> new NewProjectDialog());
 
         saveMenuItem.addActionListener(e -> {
             try {
                 MainFrame.getProject().saveProject();
                 System.out.println("Saved!");
             } catch (IOException e1) {
                 JOptionPane.showMessageDialog(null, "Failed to save project.");
             }
         });
 
         openMenuItem.addActionListener(e -> {
             JFileChooser fc = new JFileChooser();
             fc.setFileFilter(new FileNameExtensionFilter("Nebula2D Project File (*.n2d)", "n2d"));
             fc.setAcceptAllFileFilterUsed(false);
             if (fc.showOpenDialog(N2DMenuBar.this) == JFileChooser.APPROVE_OPTION) {
                 MainFrame.getSceneGraph().wipe();
                 MainFrame.setProject(new Project(fc.getSelectedFile().getAbsolutePath()));
 
                 try {
                     MainFrame.getProject().loadProject();
                     MainFrame.getSceneGraph().init();
                     MainFrame.getProject().loadCurrentScene();
                 } catch (IOException e1) {
                     MainFrame.setProject(null);
                     JOptionPane.showMessageDialog(N2DMenuBar.this, e1.getMessage());
                 }
             }
         });
 
         settingsMenuItem.addActionListener(e -> new SettingsDialog());
 
         buildMenuItem.addActionListener(e -> {
             try {
                // TODO: Make start scene configurable.
                Project p = MainFrame.getProject();
                new ProjectBuilder(p).build(p.getCurrentScene().getName(), ProjectBuilder.ProjectType.PC);
             } catch (Exception e1) {
                 JOptionPane.showMessageDialog(null, "Failed to build project.");
             }
         });
 
         newSceneMenuItem.addActionListener(e -> new NewSceneDialog());
 
         changeSceneMenuItem.addActionListener(e -> new ChangeSceneDialog());
         renameSceneMenuItem.addActionListener(e -> new RenameSceneDialog());
         newLayerMenuItem.addActionListener(e -> {
             Layer layer = new Layer("New Layer " + MainFrame.getSceneGraph().getLayerCount());
             MainFrame.getSceneGraph().addLayer(layer);
         });
 
         newEmptyGameObjectMenuItem.addActionListener(e -> {
             //If this menu item is enabled, we know 100% that something is selected, so no check is necessary. =)
             BaseSceneNode selectedNode = (BaseSceneNode) MainFrame.getSceneGraph().getLastSelectedPathComponent();
 
             GameObject go = new GameObject("Empty Game Object " + MainFrame.getSceneGraph().getGameObjectCount());
             selectedNode.addGameObject(go);
         });
     }
 
     public JMenuItem getBuildMenuItem() {
         return  buildMenuItem;
     }
 
     public JMenu getSceneMenu() {
         return sceneMenu;
     }
 
     public JMenu getGameObjectMenu() {
         return gameObjectMenu;
     }
 
     public JMenuItem getSaveMenuItem() {
         return saveMenuItem;
     }
 }
