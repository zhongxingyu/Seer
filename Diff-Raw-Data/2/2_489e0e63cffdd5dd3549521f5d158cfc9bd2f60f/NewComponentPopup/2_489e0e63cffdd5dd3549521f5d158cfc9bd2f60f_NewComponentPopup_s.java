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
 
 
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.framework.components.Behaviour;
 import com.nebula2d.editor.framework.components.Collider;
 import com.nebula2d.editor.framework.components.Component;
 import com.nebula2d.editor.framework.components.MusicSource;
 import com.nebula2d.editor.framework.components.RigidBody;
 import com.nebula2d.editor.framework.components.SoundEffectSource;
 import com.nebula2d.editor.framework.components.SpriteRenderer;
 import com.nebula2d.editor.framework.components.TileMapRenderer;
 import com.nebula2d.editor.ui.controls.N2DLabel;
 import com.nebula2d.editor.ui.controls.N2DPanel;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 public class NewComponentPopup extends JPopupMenu {
 
     private GameObject gameObject;
     private DefaultListModel<Component> listModel;
     private JList<Component> list;
 
     public NewComponentPopup(GameObject gameObject, JList<Component> list) {
         this.list = list;
         this.gameObject = gameObject;
         this.listModel = (DefaultListModel<Component>) list.getModel();
 
         create();
     }
 
     private void create() {
         JMenu rendererMenu = new JMenu("Renderer");
        JMenuItem spriteRendererMenuItem = rendererMenu.add("SpriteAnimatedRenderer");
         JMenuItem tileMapRendererMenuItem = rendererMenu.add("TileMapRenderer");
 
         JMenu audioMenu = new JMenu("Audio");
         JMenuItem musicSourceMenuItem = audioMenu.add("MusicSource");
         JMenuItem soundEffectSourceMenuItem = audioMenu.add("SoundEffectSource");
 
         JMenuItem behaviorMenuItem = new JMenuItem("Behavior");
 
         JMenu physicsMenu = new JMenu("Physics");
         JMenuItem rigidBodyMenuItm = physicsMenu.add("RigidBody");
         JMenuItem collider = physicsMenu.add("Collider");
 
         behaviorMenuItem.addActionListener(e -> {
             Behaviour behaviour = new Behaviour();
             new NewComponentDialog(behaviour);
         });
 
         musicSourceMenuItem.addActionListener(e -> {
             MusicSource musicSource = new MusicSource("");
             new NewComponentDialog(musicSource);
         });
 
         soundEffectSourceMenuItem.addActionListener(e -> {
             SoundEffectSource soundEffectSource = new SoundEffectSource("");
             new NewComponentDialog(soundEffectSource);
         });
 
         spriteRendererMenuItem.addActionListener(e -> {
 
             if (gameObject.getRenderer() != null) {
                 JOptionPane.showMessageDialog(NewComponentPopup.this, "This GameObject already has a renderer attached.");
                 return;
             }
 
             new NewComponentDialog(new SpriteRenderer(""));
         });
 
         tileMapRendererMenuItem.addActionListener(e -> {
             if (gameObject.getRenderer() != null) {
                 JOptionPane.showMessageDialog(NewComponentPopup.this, "This GameObject already has a renderer attached.");
                 return;
             }
 
             new NewComponentDialog(new TileMapRenderer(""));
         });
 
         rigidBodyMenuItm.addActionListener(e -> {
             if (gameObject.getRigidBody() != null) {
                 JOptionPane.showMessageDialog(NewComponentPopup.this, "This GameObject already has a RigidBody attached.");
                 return;
             }
 
             new NewComponentDialog(new RigidBody(gameObject));
         });
 
         collider.addActionListener(e -> new NewComponentDialog(new Collider(gameObject)));
 
         add(rendererMenu);
         add(audioMenu);
         add(behaviorMenuItem);
         add(physicsMenu);
 
         addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 setVisible(false);
             }
         });
     }
 
     private class NewComponentDialog extends JDialog {
         private Component component;
 
         public NewComponentDialog(Component component) {
             this.component = component;
             final N2DLabel errorMessage = new N2DLabel("You must enter a valid name for the component.");
             errorMessage.setForeground(Color.red);
             errorMessage.setVisible(false);
 
             final N2DLabel nameLbl = new N2DLabel("Name:");
             final JTextField nameTf = new JTextField(20);
             N2DPanel namePanel = new N2DPanel();
             namePanel.add(nameLbl);
             namePanel.add(nameTf);
 
             JButton okBtn = new JButton("Ok");
             okBtn.addActionListener(e -> {
                 String name = nameTf.getText();
                 if (!validateText(name)) {
                     errorMessage.setVisible(true);
                     return;
                 }
 
                 errorMessage.setVisible(false);
                 NewComponentDialog.this.component.setName(name);
                 gameObject.addComponent(NewComponentDialog.this.component);
 
                 listModel.addElement(NewComponentDialog.this.component);
                 dispose();
                 list.setSelectedValue(NewComponentDialog.this.component, true);
 
 
             });
             JButton cancelBtn = new JButton("Cancel");
             cancelBtn.addActionListener(e -> dispose());
             N2DPanel buttonPanel = new N2DPanel(new FlowLayout(FlowLayout.LEFT));
             buttonPanel.add(okBtn);
             buttonPanel.add(cancelBtn);
 
             add(errorMessage, BorderLayout.NORTH);
             add(buttonPanel, BorderLayout.SOUTH);
             add(namePanel);
             pack();
             setLocationRelativeTo(null);
             setVisible(true);
         }
 
         private boolean validateText(String text) {
             return !text.trim().equals("");
         }
     }
 }
