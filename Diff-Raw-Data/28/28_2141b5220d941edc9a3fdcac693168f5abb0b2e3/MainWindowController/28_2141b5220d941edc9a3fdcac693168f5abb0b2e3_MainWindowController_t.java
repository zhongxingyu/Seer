 package com.qsoft.controller;
 
 import com.qsoft.ui.MainWindow;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * User: luult
  * Date: 8/8/13
  * Time: 9:14 AM
  */
 public class MainWindowController implements ActionListener
 {
     private MainWindow mainWindow;
 
 
     public MainWindowController(MainWindow mainWindow)
     {
         this.mainWindow = mainWindow;
     }
 
     @Override
     public void actionPerformed(ActionEvent e)
     {
         if (e.getActionCommand().equals("Start"))
         {
             mainWindow.getLbStatus().setText("Started");
             mainWindow.resetMainPanel();
         }
         else if (e.getActionCommand().equals("Stop"))
         {
             mainWindow.getLbStatus().setText("Ended");
            mainWindow.disableAllButtonGame();
         }
     }
 }
