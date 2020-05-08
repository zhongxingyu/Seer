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
     private static class MyLabel extends JLabel {
         public MyLabel() {
             this("");
         }
 
         public MyLabel(final String text) {
             this(text, SwingConstants.LEFT);
         }
 
         public MyLabel(final String text, final int horizontalAlignment) {
             super(text, horizontalAlignment);
             setPreferredSize(new Dimension(150, getPreferredSize().height));
         }
 
         @Override
         public void setText(String text) {
             text = text.trim();
             if (!text.endsWith(":")) {
                 text += ":";
             }
             super.setText(text);
         }
     }
 
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
                     if (!done) {
                         ProgressBar.this.setVisible(true);
                     }
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
 
     private boolean done;
 
     public ProgressBar() {
         super(new BorderLayout(2, 1));
 
         myLabel = new MyLabel();
         myLabel.setText("Task: ");
 
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
 
         setBorder(BorderFactory.createEmptyBorder(2, 5, 3, 5));
     }
 
     private void addComponents() {
         add(myLabel, BorderLayout.WEST);
         add(myProgressBar);
     }
 
     @Override
     public void setIndeterminate(final boolean indeterminate) {
         myProgressBar.setIndeterminate(indeterminate);
         myProgressBar.setStringPainted(!indeterminate);
     }
 
     @Override
     public void started(final String message) {
         done = false;
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
         done = true;
         JOptionPane.showMessageDialog(ViewUtil.getMainWindow(), message);
         setVisible(false);
         if (myDialog != null) {
             myDialog.setVisible(false);
         }
         setIndeterminate(false);
     }
 }
