 package org.jenkinsci.modules.slave_installer.impl;
 
 import hudson.FilePath;
 import hudson.Util;
 import hudson.remoting.Callable;
 import hudson.remoting.Engine;
 import hudson.remoting.jnlp.MainDialog;
 import hudson.remoting.jnlp.MainMenu;
 import hudson.slaves.SlaveComputer;
 import org.jenkinsci.modules.slave_installer.InstallationException;
 import org.jenkinsci.modules.slave_installer.LaunchConfiguration;
 import org.jenkinsci.modules.slave_installer.SlaveInstaller;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 
 import static javax.swing.JOptionPane.*;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 class InstallerGui implements Callable<Void,IOException> {
     private final SlaveInstaller installer;
     private final FilePath slaveRoot;
     private final String jnlpMac;
 
     private transient Engine engine;
     private transient MainDialog dialog;
 
     InstallerGui(SlaveInstaller installer, SlaveComputer sc) {
         this.installer = installer;
         this.slaveRoot = sc.getNode().getRootPath();
         jnlpMac = sc.getJnlpMac();
     }
 
     /**
      * To be executed on each slave JVM.
      */
     public Void call() throws IOException {
         dialog = MainDialog.get();
         if(dialog==null)     return null;    // can't find the main window. Maybe not running with GUI
 
         // capture the engine
         engine = Engine.current();
         if(engine==null)     return null;    // Ditto
 
         final URL jnlpUrl = new URL(engine.getHudsonUrl(),"computer/"+ Util.rawEncode(engine.slaveName)+"/slave-agent.jnlp");
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 MainMenu mainMenu = dialog.getMainMenu();
                 JMenu m = mainMenu.getFileMenu();
                 JMenuItem menu = new JMenuItem(installer.getDisplayName());
                 menu.addActionListener(new ActionListener() {
                     private Exception problem;
 
                     public void actionPerformed(ActionEvent e) {
                         try {
                             // final confirmation before taking an action
                             int r = JOptionPane.showConfirmDialog(dialog,
                                     installer.getConfirmationText(),
                                     installer.getDisplayName(), OK_CANCEL_OPTION);
                             if(r!=JOptionPane.OK_OPTION)    return;
 
                             dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
                             Thread t = new Thread("installer") {
                                 @Override
                                 public void run() {
                                     problem = null;
                                     LaunchConfiguration config = LAUNCH_CONFIG;
                                     if (config == null) {
                                         assert !slaveRoot.isRemote();
                                         config = new JnlpLaunchConfiguration(jnlpUrl, new File(slaveRoot.getRemote()), jnlpMac);
                                     }
                                     try {
                                         installer.install(config, new SwingPrompter());
                                     } catch (Exception e) {
                                         problem = e;
                                     }
                                 }
                             };
                            t.run();
                             t.join();
                             if (problem!=null)  throw problem;
                         } catch (InstallationException t) {
                             JOptionPane.showMessageDialog(dialog,t.getMessage(),"Error", ERROR_MESSAGE);
                         } catch (Exception t) {// this runs as a JNLP app, so if we let an exception go, we'll never find out why it failed
                             StringWriter sw = new StringWriter();
                             t.printStackTrace(new PrintWriter(sw));
                             JOptionPane.showMessageDialog(dialog,sw.toString(),"Error", ERROR_MESSAGE);
                         }
                     }
                 });
                 m.add(menu);
                 mainMenu.commit();
             }
         });
 
         return null;
     }
 
     private static final long serialVersionUID = 1L;
 
     /**
      * {@link LaunchConfiguration} that controls what process will be run under the service wrapper
      * when the slave installation happens through GUI.
      *
      * Conceptually, this can be thought of as a recovered memory of how this slave JVM has been started.
      * This is "recovered", because we can't really reliably tell from within the slave itself, but
      * nonetheless it's a piece of information scoped to the slave JVM. Hence singleton.
      */
     // XXX what is this for? no one ever writes to it
     public static LaunchConfiguration LAUNCH_CONFIG;
 }
