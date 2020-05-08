 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) $date.year Jon Bonazza
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
 
 package com.nebula2d.editor.framework.components;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.google.common.base.Function;
 import com.nebula2d.editor.common.IRenderable;
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.ui.ComponentsDialog;
 import com.nebula2d.editor.ui.controls.N2DCheckBox;
 import com.nebula2d.editor.ui.controls.N2DLabel;
 import com.nebula2d.editor.ui.controls.N2DPanel;
 import com.nebula2d.editor.util.FloatValidatedDocumentListener;
 import com.nebula2d.editor.util.FullBufferedReader;
 import com.nebula2d.editor.util.FullBufferedWriter;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 /**
  *
  * Created by bonazza on 5/3/14.
  */
 public class RigidBody extends Component implements IRenderable {
 
     private boolean isKinematic;
     private boolean fixedRotation;
     private boolean isBullet;
     private float mass;
 
     private CollisionShape shape;
 
     public RigidBody(GameObject gameObject) {
        super("");
         shape = new BoundingBox(gameObject);
        componentType = ComponentType.RIGID_BODY;
     }
 
     public RigidBody(String name) {
         super(name);
     }
 
     @Override
     public void setParent(GameObject gameObject) {
         super.setParent(gameObject);
         if (shape != null)
             shape.setGameObject(gameObject);
     }
 
     public boolean isKinematic() {
         return isKinematic;
     }
 
     public boolean isFixedRotation() {
         return fixedRotation;
     }
 
     public boolean isBullet() {
         return isBullet;
     }
 
     public float getMass() {
         return mass;
     }
 
     public CollisionShape getShape() {
         return shape;
     }
 
     @Override
     public N2DPanel forgeComponentContentPanel(final ComponentsDialog parent) {
         final N2DLabel nameLbl = new N2DLabel("Name:");
         final JTextField nameTf = new JTextField(name, 20);
         final N2DCheckBox enabledCb = new N2DCheckBox("Enabled", enabled);
         final N2DCheckBox isKinematicCb = new N2DCheckBox("Kinematic", isKinematic);
         final N2DCheckBox fixedRotationCb = new N2DCheckBox("Fixed Rotation", fixedRotation);
         final N2DCheckBox isBulletCb = new N2DCheckBox("Bullet", isBullet);
         final N2DLabel massLbl = new N2DLabel("Mass: ");
         final JTextField massTf = new JTextField(Float.toString(mass), 20);
         final N2DLabel shapeLbl = new N2DLabel("Collision Shape: ");
         final JComboBox<CollisionShape.ShapeType> shapeCombo = new JComboBox<CollisionShape.ShapeType>(
                 CollisionShape.ShapeType.values());
 
         final N2DPanel shapePanel = new N2DPanel();
 
         final N2DPanel panel = new N2DPanel();
 
         enabledCb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 enabled = enabledCb.isSelected();
             }
         });
 
         isKinematicCb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 isKinematic = isKinematicCb.isSelected();
             }
         });
 
         fixedRotationCb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 fixedRotation = fixedRotationCb.isSelected();
             }
         });
 
         isBulletCb.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 isBullet = isBulletCb.isSelected();
             }
         });
 
         massTf.getDocument().addDocumentListener(new FloatValidatedDocumentListener(massTf,
                 new Function<Float, Void>() {
                     @Override
                     public Void apply(Float input) {
                         RigidBody.this.mass = input;
                         return null;
                     }
                 }));
 
         shapeCombo.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 CollisionShape.ShapeType shapeType = (CollisionShape.ShapeType) shapeCombo.getSelectedItem();
 
                 switch (shapeType) {
                     case BOX:
                        shape = new BoundingBox(RigidBody.this.parent);
                         break;
                     case CIRCLE:
                        shape = new Circle(RigidBody.this.parent);
                         break;
                 }
 
                 shapePanel.removeAll();
                 shapePanel.add(shape.createEditorPanel());
                 shapePanel.validate();
                 shapePanel.repaint();
             }
         });
 
         GroupLayout layout = new GroupLayout(panel);
         panel.setLayout(layout);
 
         layout.setAutoCreateContainerGaps(true);
         layout.setAutoCreateGaps(true);
 
         GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
         hGroup.addGroup(layout.createSequentialGroup().
                 addComponent(nameLbl).
                 addComponent(nameTf, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         hGroup.addGroup(layout.createSequentialGroup().
                 addComponent(enabledCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         hGroup.addGroup(layout.createSequentialGroup().
                 addComponent(isKinematicCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         hGroup.addGroup(layout.createSequentialGroup().
                 addComponent(fixedRotationCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         hGroup.addGroup(layout.createSequentialGroup().
                 addComponent(isBulletCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         hGroup.addGroup(layout.createSequentialGroup().
                 addComponent(massLbl).
                 addComponent(massTf, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         hGroup.addGroup(layout.createSequentialGroup().
                 addComponent(shapeLbl).
                 addComponent(shapeCombo, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         hGroup.addGroup(layout.createSequentialGroup().addComponent(shapePanel));
         layout.setHorizontalGroup(hGroup);
 
         GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLbl).
                 addComponent(nameTf));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(enabledCb));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(isKinematicCb));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fixedRotationCb));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(isBulletCb));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(massLbl).
                 addComponent(massTf));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(shapeLbl).
                 addComponent(shapeCombo));
         vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(shapePanel));
         layout.setVerticalGroup(vGroup);
 
         shapeCombo.setSelectedItem(shape.getShapeType());
 
         return panel;
     }
 
     @Override
     public void save(FullBufferedWriter fw) throws IOException {
         super.save(fw);
         fw.writeBoolLine(isKinematic);
         fw.writeBoolLine(fixedRotation);
         fw.writeBoolLine(isBullet);
 
         fw.writeFloatLine(mass);
 
         if (shape != null) {
             fw.writeBoolLine(true);
             fw.writeIntLine(shape.getShapeType().ordinal());
             shape.save(fw);
         } else {
             fw.writeBoolLine(false);
         }
     }
 
     @Override
     public void load(FullBufferedReader fr) throws IOException {
         isKinematic = fr.readBooleanLine();
         fixedRotation = fr.readBooleanLine();
         isBullet = fr.readBooleanLine();
         mass = fr.readFloatLine();
 
         if (fr.readBooleanLine()) {
             CollisionShape.ShapeType shapeType = CollisionShape.ShapeType.values()[fr.readIntLine()];
             switch(shapeType) {
                 case BOX:
                     shape = new BoundingBox(parent);
                     break;
                 case CIRCLE:
                     shape = new Circle(parent);
                     break;
             }
             shape.load(fr);
         }
     }
 
     @Override
     public void render(GameObject selectedObject, SpriteBatch batcher, Camera cam) {
         shape.render(selectedObject, batcher, cam);
     }
 }
