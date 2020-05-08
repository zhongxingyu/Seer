 //
 //  QTCubed.java
 //  QTCubed
 //
 //  Created by Chappell Charles on 10/02/19.
 //  Copyright (c) 2010 MC Cubed, Inc. All rights reserved.
 //
 //  This program is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation; either version 2 of the License, or
 //  (at your option) any later version.
 //  
 //  This program is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //  
 //  You should have received a copy of the GNU General Public License along
 //  with this program; if not, write to the Free Software Foundation, Inc.,
 //  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 //  
 //  
 //  This is also dual licensed as proprietary software, and may be used in
 //  commercial/proprietary software products by obtaining a license from:
 //  MC Cubed, Inc
 //  1-3-4 Kamikizaki, Urawa-ku
 //  Saitama, Saitama, 330-0071
 //  Japan
 package net.mc_cubed;
 
 import javax.swing.SwingUtilities;
 import net.mc_cubed.qtcubed.QTMovie;
 import net.mc_cubed.qtcubed.QTMovieView;
 import net.mc_cubed.qtcubed.QTCubedFactory;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.FileDialog;
 import java.awt.Frame;
 import java.awt.BorderLayout;
 import java.awt.Button;
 import java.awt.GridLayout;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.lang.reflect.Method;
 import java.io.File;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JPanel;
 import net.mc_cubed.qtcubed.QTKitCaptureDecompressedVideoOutput;
 import net.mc_cubed.qtcubed.QTKitCaptureDevice;
 import net.mc_cubed.qtcubed.QTKitCaptureDeviceInput;
 import net.mc_cubed.qtcubed.QTKitCaptureSession;
 import net.mc_cubed.qtcubed.QTKitCaptureView;
 import net.mc_cubed.qtcubed.QTKitFormatDescription;
 import net.mc_cubed.qtcubed.QTKitMediaType;
 
 /**
  * Starting point for the application. General initialization should be done inside
  * the ApplicationController's init() method. If certain kinds of non-Swing initialization
  * takes too long, it should happen in a new Thread and off the Swing event dispatch thread (EDT).
  * 
  * @author shadow
  */
 public class QTCubed extends Frame implements ActionListener {
 
     static final boolean hasQTKit;
 
     static {
         boolean qtKitLoaded = false;
         try {
             // Disable Cocoa Compatibility Mode
             System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
 
             Logger.getAnonymousLogger().log(Level.INFO, "Loading QTCubed Library");
             // Ensure native JNI library is loaded
             AccessController.doPrivileged(new PrivilegedAction() {
 
                 public Object run() {
                     // privileged code goes here
                     System.loadLibrary("QTCubed");
                     return null; // nothing to return
                 }
             });
 
             Logger.getAnonymousLogger().log(Level.INFO, "Successfully Loaded QTCubed Library!");
             qtKitLoaded = true;
         } catch (Throwable t1) {
             t1.printStackTrace();
             Logger.getAnonymousLogger().log(Level.INFO, "Cannot load QTCubed Library!");
             // Couldn't load the library, try QTJava instead
             try {
                 Logger.getAnonymousLogger().log(Level.INFO, "Trying to load QTJava Library.");
                 // Invoke QTSession.open() using reflection to avoid any class dependancies.
                 AccessController.doPrivileged(new PrivilegedAction() {
 
                     public Object run() {
                         // privileged code goes here
                         //				quicktime.QTSession.open();
                         try {
                             Class clazz = Class.forName("quicktime.QTSession");
                             Method m = clazz.getMethod("open");
                             m.invoke(null);
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                         return null; // nothing to return
                     }
                 });
                 Logger.getAnonymousLogger().log(Level.INFO, "Successfully loaded QTJava Library!");
             } catch (Throwable t2) {
                 t2.printStackTrace();
                 Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot load either QTCubed or QTJava libraries!  Quicktime Services will not be available!");
             }
 
         }
 
         hasQTKit = qtKitLoaded;
 
         // Reflexively call QTCubedJMFInitializer.init() to avoid classpath deps in case JMF is not present
         try {
             Class clazz = Class.forName("net_mc_cubed.QTCubedJMFInitializer");
             Method initMethod = clazz.getMethod("init");
             initMethod.invoke(null);
         } catch (Exception ex) {
             // Do nothing if we fail
             Logger.getAnonymousLogger().info("Could not initialize JMF features of the QTCubed Library");
         }
     }
 
     public static boolean usesQTKit() {
         return hasQTKit;
     }
     QTMovieView qtmv;
     QTKitCaptureView qtcv;
 
     public static void main(final String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 new QTCubed().setVisible(true);
             }
         });
 
         System.out.println("Reached end of main");
     }
 
     // No argument main constructor
     public QTCubed() {
         setLayout(new BorderLayout());
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new GridLayout(1, 2));
         Button b = new Button("Select Movie...");
         b.setActionCommand("MOVIE");
         b.addActionListener(this);
         buttonPanel.add(b);
 
         b = new Button("View Capture Device");
         b.setActionCommand("CAPTURE");
         b.addActionListener(this);
         buttonPanel.add(b);
         try {
             qtmv = QTCubedFactory.initQTMovieView();
             add(buttonPanel, BorderLayout.SOUTH);
             add(qtmv.getComponent(), BorderLayout.CENTER);
             qtcv = new QTKitCaptureView();
//            add(qtcv.getComponent(), BorderLayout.WEST);
             pack();
         } catch (InstantiationException ex) {
             ex.printStackTrace();
         }
 
         Logger.getAnonymousLogger().info("************** Attached capture device info follows **************");
         for (QTKitCaptureDevice device : QTKitCaptureDevice.inputDevices()) {
             Logger.getAnonymousLogger().info(" ----- " + device.uniqueId() + " (" + device.localizedDisplayName() + ") ----- ");
             for (QTKitFormatDescription format : device.getFormatDescriptions()) {
                 Logger.getAnonymousLogger().info("Format Type: " + format.getMediaType() + ": " + format.getFormatType());
                 Set<Entry<Object,Object>> props = format.getFormatDescriptionAttributes().entrySet();
                 for (Entry<Object,Object> prop: props) {
                     Logger.getAnonymousLogger().info("    " + prop.getKey() + ": " + prop.getValue());
                 }
             }
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         try {
             System.out.println("action performed");
 
             if (e.getActionCommand().equalsIgnoreCase("MOVIE")) {
                 FileDialog fd =
                         new FileDialog(this, "Select Movie...", FileDialog.LOAD);
                 fd.setVisible(true);
                 String fileName = fd.getFile();
                 if (fileName == null) {
                     return;
                 }
                 File f = new File(fd.getDirectory(), fd.getFile());
 
                 QTMovie movie = QTCubedFactory.initQTMovie(f);
                 System.out.println("Created movie");
                 this.remove(qtmv.getComponent());
                 this.remove(qtcv.getComponent());
 
                 this.add(qtmv.getComponent(), BorderLayout.CENTER);
                 // set movie on the view
                 qtmv.setMovie(movie);
                 System.out.println("Set movie on view");
 
                 qtmv.play();
             }
 
             // Copying this from the apple developer docs and switching to Java syntax
             if (e.getActionCommand().equalsIgnoreCase("CAPTURE")) {
                 final QTKitCaptureSession session = new QTKitCaptureSession();
 
                 QTKitCaptureDevice videoDevice = QTKitCaptureDevice.defaultInputDevice(QTKitMediaType.VIDEO);
                 try {
                     videoDevice.open();
                     QTKitCaptureDeviceInput videoDeviceInput = new QTKitCaptureDeviceInput(videoDevice);
                     session.addInput(videoDeviceInput);
                     session.addOutput(new QTKitCaptureDecompressedVideoOutput());
 
                     SwingUtilities.invokeLater(new Runnable() {
 
                         public void run() {
                             qtcv.setCaptureSession(session);
                         }
                     });
 
 
                     session.startRunning();
 
                     this.remove(qtmv.getComponent());
                     this.remove(qtcv.getComponent());
 
                     this.add(qtcv.getComponent(), BorderLayout.CENTER);
                     validate();
                     pack();
 
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         } catch (Exception ex) {
             // TODO- maybe a dialog
             ex.printStackTrace();
         }
     }
 }
