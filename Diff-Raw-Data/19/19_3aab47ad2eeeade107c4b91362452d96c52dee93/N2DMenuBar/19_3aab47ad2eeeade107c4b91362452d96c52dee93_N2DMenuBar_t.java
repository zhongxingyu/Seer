 package com.nebula2d.editor.ui;
 
 import com.nebula2d.editor.framework.BaseSceneNode;
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.framework.Layer;
 import com.nebula2d.editor.util.PlatformUtil;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 /**
  * Custome JMenuBar implementation
  */
 public class N2DMenuBar extends JMenuBar {
 
     private JMenu sceneMenu;
     private JMenu gameObjectMenu;
 
     private JMenuItem newMenuItem;
     private JMenuItem openMenuItem;
     private JMenuItem exitMenuItem;
 
     private JMenuItem newSceneMenuItem;
     private JMenuItem changeSceneMenuItem;
     private JMenuItem newLayerMenuItem;
 
     private JMenuItem newEmptyGameObjectMenuItem;
 
    public N2DMenuBar() {
         JMenu fileMenu = new JMenu("File");
 
         newMenuItem = fileMenu.add("New Project");
         openMenuItem = fileMenu.add("Open Project");
 
         //Don't need exit menu item on Mac
         if (!PlatformUtil.isMac())
             exitMenuItem = fileMenu.add("Exit Nebula2D");
 
         sceneMenu = new JMenu("Scene");
 
         newSceneMenuItem = sceneMenu.add("New Scene");
         changeSceneMenuItem = sceneMenu.add("Change Scene");
         newLayerMenuItem = sceneMenu.add("New Layer");
 
         gameObjectMenu = new JMenu("New GameObject");
 
         newEmptyGameObjectMenuItem = gameObjectMenu.add("Empty GameObject");
 
         sceneMenu.add(gameObjectMenu);
         gameObjectMenu.setEnabled(false);
         sceneMenu.setEnabled(false);
         add(fileMenu);
         add(sceneMenu);
         bindMenuItems();
     }
 
     /**
      * binds click events to menu items
      */
     private void bindMenuItems() {
         newMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                new NewProjectDialog();
             }
         });
 
         openMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO: implement
             }
         });
 
         if (exitMenuItem != null) {
             exitMenuItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     //TODO: Prompt on exit
                     System.exit(0);
                 }
             });
         }
 
         newSceneMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO: implement
             }
         });
 
         changeSceneMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO: implement
             }
         });
 
         newLayerMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Layer layer = new Layer("New Layer " + MainFrame.getSceneGraph().getLayerCount());
                 MainFrame.getSceneGraph().addLayer(layer);
             }
         });
 
         newEmptyGameObjectMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //If this memnu item is enabled, we know 100% that something is selected, so no check is necessary. =)
                 BaseSceneNode selectedNode = (BaseSceneNode) MainFrame.getSceneGraph().getLastSelectedPathComponent();
 
                 GameObject go = new GameObject("Empty Game Object " + MainFrame.getSceneGraph().getGameObjectCount());
                 selectedNode.addGameObject(go);
             }
         });
     }
 
     //region accessors
     public JMenu getSceneMenu() {
         return sceneMenu;
     }
 
     public JMenu getGameObjectMenu() {
         return gameObjectMenu;
     }
     //endregion
 }
