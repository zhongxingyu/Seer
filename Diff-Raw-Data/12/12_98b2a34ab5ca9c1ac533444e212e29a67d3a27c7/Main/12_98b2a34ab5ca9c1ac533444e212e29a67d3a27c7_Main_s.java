 package com.silveregg.wrapper.test;
 
 /*
  * Copyright (c) 2001 Silver Egg Technology
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or 
  * sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following 
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 // $Log$
 // Revision 1.5  2002/05/17 09:52:42  mortenson
 // Add a Restart button to the TestWrapper application.
 //
 // Revision 1.4  2002/05/17 09:08:45  mortenson
 // Make the test shutdown correctly when shutdown from a shutdown hook.
 //
 // Revision 1.3  2002/05/16 03:34:32  mortenson
 // Added comments to the buttons that had no comments to make their use clear.
 //
 // Revision 1.2  2001/12/06 09:36:24  mortenson
 // Docs changes, Added sample apps, Fixed some problems with
 // relative paths  (See revisions.txt)
 //
 // Revision 1.1.1.1  2001/11/07 08:54:20  mortenson
 // no message
 //
 
 import com.silveregg.wrapper.WrapperManager;
 import com.silveregg.wrapper.WrapperListener;
 import java.awt.Button;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class Main implements WrapperListener {
     private MainFrame _frame;
     
     /**************************************************************************
      * Constructors
      *************************************************************************/
     private Main() {
     }
     
     private class MainFrame extends Frame implements ActionListener {
         MainFrame() {
             super("Wrapper Test Application");
             
             setLayout(new FlowLayout());
             
             Button exitButton = new Button("Exit");
             add(exitButton);
             exitButton.addActionListener(this);
             
             Button avButton = new Button("Access Violation");
             add(avButton);
             avButton.addActionListener(this);
             
             Button navButton = new Button("Native Access Violation");
             add(navButton);
             navButton.addActionListener(this);
             
            Button ahButton = new Button("Simmulate JVM Hang");
             add(ahButton);
             ahButton.addActionListener(this);
             
             Button seButton = new Button("System.exit(0)");
             add(seButton);
             seButton.addActionListener(this);
             
             Button rhButton = new Button("Runtime.getRuntime().halt(0)");
             add(rhButton);
             rhButton.addActionListener(this);
             
             Button rsButton = new Button("Request Restart");
             add(rsButton);
             rsButton.addActionListener(this);
             
             add(new Label("The Access Violation button only works with Sun JVMs."));
             add(new Label("Also try killing the JVM process or pressing CTRL-C in the console window."));
            add(new Label("Simmulate JVM Hang only has an effect when controlled by native Wrapper."));
             add(new Label("System.exit(0) should cause the Wrapper to exit."));
             add(new Label("Runtime.getRuntime().halt(0) should result in the JVM restarting."));
             add(new Label("Request Restart should cause the JVM to stop and be restarted cleanly."));
             
             setSize(new Dimension(512, 250));
         }
         
         public void actionPerformed(ActionEvent event) {
             String command = event.getActionCommand();
             if (command.equals("Exit")) {
                 WrapperManager.stop(0);
             } else if (command.equals("Access Violation")) {
                 WrapperManager.accessViolation();
             } else if (command.equals("Native Access Violation")) {
                 WrapperManager.accessViolationNative();
            } else if (command.equals("Simmulate JVM Hang")) {
                 WrapperManager.appearHung();
             } else if (command.equals("System.exit(0)")) {
                 System.exit(0);
             } else if (command.equals("Runtime.getRuntime().halt(0)")) {
                 Runtime.getRuntime().halt(0);
             } else if (command.equals("Request Restart")) {
                 WrapperManager.restart();
             }
         }
     }
     
     /**************************************************************************
      * WrapperListener Methods
      *************************************************************************/
     public Integer start(String[] args) {
         System.out.println("start()");
         
         _frame = new MainFrame();
         _frame.setVisible(true);
         
         return null;
     }
     
     public int stop(int exitCode) {
         System.out.println("stop(" + exitCode + ")");
         
         if (_frame != null) {
             if (!WrapperManager.hasShutdownHookBeenTriggered()) {
                 _frame.setVisible(false);
                 _frame.dispose();
             }
             _frame = null;
         }
         
         return exitCode;
     }
     
     public void controlEvent(int event) {
         System.out.println("controlEvent(" + event + ")");
         if (event == WrapperManager.WRAPPER_CTRL_C_EVENT) {
             WrapperManager.stop(0);
         }
     }
     
     /**************************************************************************
      * Main Method
      *************************************************************************/
     public static void main(String[] args) {
         System.out.println("Initializing...");
         
         // Start the application.  If the JVM was launched from the native
         //  Wrapper then the application will wait for the native Wrapper to
         //  call the application's start method.  Otherwise the start method
         //  will be called immediately.
         WrapperManager.start(new Main(), args);
     }
 }
 
