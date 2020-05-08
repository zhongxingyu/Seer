 /**
  * Copyright(C) 2013 XTeam. All rights reserved.
  */
 /**
  * LoginFrame.java
  * Original Author : shixingxing@ruijie.com.cn, 2013-09-03
  *
  * Login frame.
  *
  * History
  *   v1.0            shixingxing@ruijie.com.cn, 2013-09-03
  *                          initial version
  */
 package com.xms.gui.widgets;
 import org.eclipse.swt.widgets.*;
import javax.swing.*;
 
 public class LoginFrame {
     public LoginFrame() {
         Display display;
         Shell shell;
         
         display = new Display();
         shell = new Shell(display);
         shell.setText("ʹϵͳ");
         shell.open ();
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch()) {
                 display.sleep();
             }
         }
         display.dispose();
     }
 }
