 package com.nebula2d.editor.ui;
 
import com.badlogic.gdx.Gdx;
 import com.nebula2d.editor.framework.assets.Texture;
 import com.nebula2d.editor.framework.components.Animation;
 import com.nebula2d.editor.framework.components.KeyFrameAnimation;
 import com.nebula2d.editor.framework.components.Renderer;
 
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 public class NewAnimationPopup extends JPopupMenu {
     private Renderer renderer;
     private DefaultListModel<Animation> listModel;
     private Texture tex;
 
    public NewAnimationPopup(Renderer renderer, DefaultListModel<Animation> listModel, final String texturePath) {
         this.renderer = renderer;
         this.listModel = listModel;

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                tex = new Texture(texturePath);
            }
        });

 
         JMenuItem kfAnimMenuItem = add("KeyFrameAnimation");
         kfAnimMenuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 KeyFrameAnimation kfAnim = new KeyFrameAnimation("", tex);
                 new NewAnimationDialog(kfAnim);
             }
         });
 
         addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 setVisible(false);
             }
         });
     }
 
     private class NewAnimationDialog extends JDialog {
         private Animation animation;
 
         public NewAnimationDialog(Animation animation) {
             this.animation = animation;
             final JLabel errorMsg = new JLabel("You must enter a valid name for the animation.");
             errorMsg.setForeground(Color.RED);
             errorMsg.setVisible(false);
             final JLabel nameLbl = new JLabel("Name:");
             final JTextField nameTf = new JTextField(20);
             final JPanel namePanel = new JPanel();
             namePanel.add(nameLbl);
             namePanel.add(nameTf);
 
             final JButton okBtn = new JButton("Ok");
             okBtn.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     String name = nameTf.getText();
                     if (!validateText(name)) {
                         errorMsg.setVisible(true);
                         return;
                     }
 
                     errorMsg.setVisible(false);
                     NewAnimationDialog.this.animation.setName(name);
                     renderer.addAnimation(NewAnimationDialog.this.animation);
                     listModel.addElement(NewAnimationDialog.this.animation);
                     dispose();
                 }
             });
             final JButton cancelBtn = new JButton("Cancel");
             cancelBtn.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     dispose();
                 }
             });
 
             final JPanel buttonPanel = new JPanel();
             buttonPanel.add(okBtn);
             buttonPanel.add(cancelBtn);
 
             add(errorMsg, BorderLayout.NORTH);
             add(buttonPanel, BorderLayout.SOUTH);
             add(namePanel);
             pack();
             setVisible(true);
         }
 
         private boolean validateText(String text) {
             return !text.trim().equals("");
         }
     }
 }
