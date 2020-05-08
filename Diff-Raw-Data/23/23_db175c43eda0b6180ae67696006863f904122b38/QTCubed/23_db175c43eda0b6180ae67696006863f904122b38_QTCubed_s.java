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
 //
 //  Email: info@mc-cubed.net
 //  Website: http://www.mc-cubed.net/
 package net.mc_cubed;
 
 import javax.swing.SwingUtilities;
 import net.mc_cubed.qtcubed.QTMovie;
 import net.mc_cubed.qtcubed.QTMovieView;
 import net.mc_cubed.qtcubed.QTCubedFactory;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import javax.swing.JFrame;
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
 import net.mc_cubed.qtcubed.QTCaptureDecompressedVideoOutput;
 import net.mc_cubed.qtcubed.QTCaptureDevice;
 import net.mc_cubed.qtcubed.QTCaptureDeviceInput;
 import net.mc_cubed.qtcubed.QTCaptureSession;
 import net.mc_cubed.qtcubed.QTCaptureView;
 import net.mc_cubed.qtcubed.QTFormatDescription;
 import net.mc_cubed.qtcubed.QTMediaType;
 import net.mc_cubed.qtcubed.QTPixelFormat;
 
 /**
  * Starting point for the application. General initialization should be done inside
  * the ApplicationController's init() method. If certain kinds of non-Swing initialization
  * takes too long, it should happen in a new Thread and off the Swing event dispatch thread (EDT).
  * 
  * @author shadow
  */
 public class QTCubed extends JFrame implements ActionListener {
 
     static final boolean hasQTKit;
     static final long taskRef;
     QTCaptureSession session;
 
     static {
         boolean qtKitLoaded = false;
         long localTaskRef = 0;
         try {
             // Disable Cocoa Compatibility Mode
             System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
 
             Logger.getAnonymousLogger().log(Level.INFO, "Loading QTCubed Library");
             // Ensure native JNI library is loaded
             Long innerTaskRef = (Long) AccessController.doPrivileged(new PrivilegedAction() {
 
                 public Object run() {
                     // privileged code goes here
                     System.loadLibrary("QTCubed");
                     return startEncodingServer();
                 }
             });
 
             localTaskRef = innerTaskRef.longValue();
 
             Logger.getAnonymousLogger().log(Level.INFO, "Successfully Loaded QTCubed Library!");
             qtKitLoaded = true;
         } catch (Throwable t1) {
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
                             // This is the same as:
                             //QTSession.open();
                             // but links at runtime to avoid classpath dependencies.
                             Class clazz = Class.forName("quicktime.QTSession");
                             Method m = clazz.getMethod("open");
                             m.invoke(null);
 
                             // create shutdown handler to cleanup Quicktime automatically
                             Thread shutdownHook = new Thread() {
 
                                 @Override
                                 public void run() {
                                     try {
                                         // This is the same as:
                                         //QTSession.close();
                                         // but links at runtime to avoid classpath dependencies.
                                         Class clazz = Class.forName("quicktime.QTSession");
                                         Method m = clazz.getMethod("close");
                                         m.invoke(null);
                                     } catch (Exception ex) {
                                         Logger.getLogger(QTCubed.class.getName()).log(Level.SEVERE, null, ex);
                                     }
                                 }
                             };
                             Runtime.getRuntime().addShutdownHook(shutdownHook);
                         } catch (Exception ex) {
                             Logger.getLogger(QTCubed.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         return null; // nothing to return
                     }
                 });
                 Logger.getLogger(QTCubed.class.getName()).log(Level.INFO, "Successfully loaded QTJava Library!");
             } catch (Throwable t2) {
                 Logger.getLogger(QTCubed.class.getName()).log(Level.SEVERE, "Cannot load either QTCubed or QTJava libraries!  Quicktime Services will not be available!",t2);
             }
 
         }
 
         hasQTKit = qtKitLoaded;
         taskRef = localTaskRef;
 
         QTPixelFormat pf;
         QTPixelFormat.values();
 
         // Reflexively call QTCubedJMFInitializer.init() to avoid classpath deps in case JMF is not present
         try {
             Class clazz = Class.forName("net.mc_cubed.QTCubedJMFInitializer");
             Method initMethod = clazz.getMethod("init");
             initMethod.invoke(null);
         } catch (Throwable ex) {
             // Do nothing if we fail
             Logger.getLogger(QTCubed.class.getName()).log(Level.INFO,"Could not initialize JMF features of the QTCubed Library",ex);
         }
     }
 
     public static boolean usesQTKit() {
         return hasQTKit;
     }
     QTMovieView qtmv;
     QTCaptureView qtcv;
 
     public static void main(final String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 new QTCubed().setVisible(true);
             }
         });
 
         System.out.println("Reached end of main");
     }
 
     // No argument main constructor
     @SuppressWarnings("LeakingThisInConstructor")
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
         setDefaultCloseOperation(EXIT_ON_CLOSE);
 
         try {
             qtmv = QTCubedFactory.initQTMovieView();
             add(buttonPanel, BorderLayout.SOUTH);
             add(qtmv.getComponent(), BorderLayout.CENTER);
             qtcv = QTCubedFactory.initQTCaptureView();
 //            add(qtcv.getComponent(), BorderLayout.WEST);
             pack();
         } catch (InstantiationException ex) {
             Logger.getLogger(QTCubed.class.getName()).log(Level.SEVERE,"Unable to initialize movie view!",ex);
         }
 
         Logger.getAnonymousLogger().info("************** Attached capture device info follows **************");
         for (QTCaptureDevice device : QTCubedFactory.captureDevices()) {
             Logger.getLogger(QTCubed.class.getName()).log(Level.INFO, " ----- {0} ({1}) ----- ", new Object[]{device.uniqueId(), device.localizedDisplayName()});
             for (QTFormatDescription format : device.getFormatDescriptions()) {
                 Logger.getLogger(QTCubed.class.getName()).log(Level.INFO, "Format Type: {0}: {1}", new Object[]{format.getMediaType(), format.getFormatType()});
                 Set<Entry<Object, Object>> props = format.getFormatDescriptionAttributes().entrySet();
                 for (Entry<Object, Object> prop : props) {
                     Logger.getLogger(QTCubed.class.getName()).log(Level.INFO, "    {0}: {1}", new Object[]{prop.getKey(), prop.getValue()});
                 }
             }
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         try {
             System.out.println("action performed");
 
             if (e.getActionCommand().equalsIgnoreCase("MOVIE")) {
                 if (session != null) {
                     session.stopRunning();
                 }
 
                 FileDialog fd =
                         new FileDialog(this, "Select Movie...", FileDialog.LOAD);
                 fd.setVisible(true);
                 String fileName = fd.getFile();
                 if (fileName == null) {
                     if (session != null) {
                         session.startRunning();
                     }
                     return;
                 }
                 File f = new File(fd.getDirectory(), fd.getFile());
 
                 QTMovie movie = QTCubedFactory.initQTMovie(f);
                 System.out.println("Created movie");
                 remove(qtmv.getComponent());
                remove(qtcv.getComponent());
 
                 add(qtmv.getComponent(), BorderLayout.CENTER);
                 // set movie on the view
                 qtmv.setMovie(movie);
                 System.out.println("Set movie on view");
 
                 validateTree();
                 pack();
 
                 qtmv.play();
             }
 
             // Copying this from the apple developer docs and switching to Java syntax
             if (e.getActionCommand().equalsIgnoreCase("CAPTURE")) {
                 if (session != null) {
                     if (session.isRunning()) {
                         session.stopRunning();
                     }
                 }
                 session = QTCubedFactory.initQTCaptureSession();
 
                 QTCaptureDevice videoDevice = QTCubedFactory.defaultCaptureDevice(QTMediaType.VIDEO);
                 try {
                     videoDevice.open();
                     QTCaptureDeviceInput videoDeviceInput = QTCubedFactory.initQTCaptureDeviceInput(videoDevice);
                     session.addInput(videoDeviceInput);
                     QTCaptureDecompressedVideoOutput videoOutput = QTCubedFactory.initQTCaptureDecompressedVideoOutput();
                     videoOutput.setFrameRate(30.0f);
                     videoOutput.setSize(new Dimension(320, 240));
                     session.addOutput(videoOutput);
                     System.out.println("Video Format: " + videoOutput.getPixelFormat());
                     System.out.println("Video Size: " + videoOutput.getSize());
 
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
                     Logger.getLogger(QTCubed.class.getName()).log(Level.SEVERE,"Unable to open the default video capture device",ex);
                 }
             }
         } catch (Exception ex) {
             // TODO- maybe a dialog
             Logger.getLogger(QTCubed.class.getName()).log(Level.SEVERE,"Unable to perform action",ex);
         }
     }
 
     native static private long startEncodingServer();
 
     native static private void _shutdown(long taskRef);
 
     @Override
     @SuppressWarnings("FinalizeDeclaration")
     protected void finalize() throws Throwable {
         super.finalize();
         _shutdown(taskRef);
     }
 }
