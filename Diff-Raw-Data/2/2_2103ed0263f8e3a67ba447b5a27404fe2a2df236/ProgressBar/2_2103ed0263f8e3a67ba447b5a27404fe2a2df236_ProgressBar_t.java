 package ru.aifgi.recognizer.view.components;
 /*
  * Copyright 2012 Alexey Ivanov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import ru.aifgi.recognizer.api.ProgressListener;
 import ru.aifgi.recognizer.view.Bundle;
 import ru.aifgi.recognizer.view.ViewUtil;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * @author aifgi
  */
 
 public class ProgressBar extends JPanel implements ProgressListener {
     private class MyDialog extends JDialog {
         private final JPanel myContentPane;
 
         public MyDialog() {
             super(ViewUtil.getMainWindow(), Bundle.getString("background.task"), true);
 
             myContentPane = new JPanel(new BorderLayout(3, 7));
             myContentPane.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
             addComponents();
 
             getRootPane().setContentPane(myContentPane);
             setDefaultCloseOperation(HIDE_ON_CLOSE);
 
             addComponentListener(new ComponentAdapter() {
                 @Override
                 public void componentShown(final ComponentEvent e) {
                     addComponents();
                 }
 
                 @Override
                 public void componentHidden(final ComponentEvent e) {
                     ProgressBar.this.addComponents();
                     ProgressBar.this.setVisible(true);
                 }
             });
 
             pack();
         }
 
         private void addComponents() {
             myContentPane.add(myLabel, BorderLayout.CENTER);
             myContentPane.add(myProgressBar, BorderLayout.SOUTH);
         }
     }
 
     private final JProgressBar myProgressBar;
     private final JLabel myLabel;
     private JDialog myDialog;
 
     public ProgressBar() {
         super(new BorderLayout(2, 1));
 
         myLabel = new JLabel();
         myLabel.setText("Message: ");
 
         myProgressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
         myProgressBar.setStringPainted(true);
 
         addComponents();
 
         setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 10));
 
         myProgressBar.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseEntered(final MouseEvent e) {
                 if (isVisible()) {
                     myProgressBar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                 }
             }
 
             @Override
             public void mouseExited(final MouseEvent e) {
                 if (isVisible()) {
                     myProgressBar.setCursor(Cursor.getDefaultCursor());
                 }
             }
 
             @Override
             public void mouseClicked(final MouseEvent e) {
                 if (SwingUtilities.isLeftMouseButton(e) && isVisible()) {
                     if (myDialog == null) {
                         myDialog = new MyDialog();
                     }
                     setVisible(false);
                     myDialog.setVisible(true);
                 }
             }
         });
     }
 
     private void addComponents() {
         add(myLabel, BorderLayout.WEST);
         add(myProgressBar);
     }
 
     @Override
     public void started(final String message) {
         myProgressBar.setValue(0);
         setVisible(true);
     }
 
     @Override
     public void progress(final int percent) {
         myProgressBar.setValue(percent);
         myProgressBar.setString(percent + "%");
     }
 
     @Override
     public void progress(final int percent, final String taskText) {
         progress(percent);
         myLabel.setText(taskText);
     }
 
     @Override
     public void done(final String message) {
         JOptionPane.showMessageDialog(ViewUtil.getMainWindow(), "Finish");
        setVisible(false);
     }
 }
