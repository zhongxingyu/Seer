 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package wingset;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.script.JavaScriptEvent;
 import org.wings.script.JavaScriptListener;
 import org.wings.session.SessionManager;
 import org.wings.border.SEmptyBorder;
 import org.wings.header.Link;
 import org.wings.resource.DefaultURLResource;
 import org.wings.style.CSSProperty;
 
 import java.io.*;
 import java.net.URL;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * The root of the WingSet demo application.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @author <a href="mailto:B.Schmid@eXXcellent.de">Benjamin Schmid</a>
  */
 public class WingSet implements Serializable {
     /**
      * Jakarta commons logger.
      */
     private final static Log log = LogFactory.getLog(WingSet.class);
 
     /**
      * If true then use {@link StatisticsTimerTask} to log statistics on a regular basis to a logging file.
      * (Typically a file named wings-statisticsxxxlog placed in jakarta-tomcat/temp directory)
      */
     private static final boolean LOG_STATISTICS_TO_FILE = true;
 
     /**
      * Optional external custom CSS stylesheet to style your application according to your needs.
      */
     private Link customStyleSheetLink;
 
     static {
         if (LOG_STATISTICS_TO_FILE) {
             StatisticsTimerTask.startStatisticsLogging(60);
         }
     }
 
     /**
      * The root frame of the WingSet application.
      */
     private final SFrame frame;
 
     private final STabbedPane tab;
 
     private boolean customStyleApplied;
 
     /**
      * Constructor of the wingS application.
      * <p/>
      * <p>This class is referenced in the <code>web.xml</code> as root entry point for the wingS application.
      * For every new client an new {@link org.wings.session.Session} is created which constructs a new instance of this class.
      */
     public WingSet() {
         // Create root frame
         frame = new SFrame("WingSet Demo");
 
         // Create the tabbed pane containing all the wingset example tabs
         tab = new STabbedPane(SConstants.TOP);
         tab.setName("examples");
         tab.setPreferredSize(new SDimension("100%", "580px"));
 
         tab.add(new WingsImage(), "wingS!");
 
         String dirName = SessionManager.getSession().getServletContext().getRealPath("/WEB-INF/classes/wingset");
         File dir = new File(dirName);
 
         List classFileNames = new ArrayList();
 
         String[] exampleClassFileNames = dir.list(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                //return name.endsWith("Example.class");
                return name.equals("XColorPickerExample.class") ||
                    name.equals("XCalendarExample.class") ||
                    name.equals("XSuggestExample.class") ||
                    name.equals("XInplaceEditorExample.class") ||
                    name.equals("PopupExample.class") ||
                    name.equals("SpinnerExample.class") ||
                    name.equals("ButtonExample.class") ||
                    name.equals("XTableExample.class");
             }
         });
         Arrays.sort(exampleClassFileNames);
         classFileNames.addAll(Arrays.asList(exampleClassFileNames));
 
         if ("TRUE".equalsIgnoreCase((String)SessionManager.getSession().getProperty("wingset.include.tests"))) {
             String[] testClassFileNames = dir.list(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith("Test.class");
                 }
             });
             Arrays.sort(testClassFileNames);
             classFileNames.addAll(Arrays.asList(testClassFileNames));
         }
 
         if ("TRUE".equalsIgnoreCase((String)SessionManager.getSession().getProperty("wingset.include.experiments"))) {
             String[] experimentClassFileNames = dir.list(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith("Experiment.class");
                 }
             });
             Arrays.sort(experimentClassFileNames);
             classFileNames.addAll(Arrays.asList(experimentClassFileNames));
         }
 
         for (Iterator iterator = classFileNames.iterator(); iterator.hasNext();) {
             String classFileName = (String)iterator.next();
             String className = "wingset." + classFileName.substring(0, classFileName.length() - ".class".length());
             try {
                 Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                 WingSetPane example = (WingSetPane)clazz.newInstance();
                 tab.add(example, example.getExampleName());
                 if (example.getClass().getName().endsWith("Experiment"))
                     tab.setForegroundAt(tab.getTabCount() - 1, Color.GRAY);
             }
             catch (Throwable e) {
                 System.err.println("Could not load plugin: " + className);
                 e.printStackTrace();
             }
         }
 
         // Add component to content pane using a layout constraint (
         frame.getContentPane().add(tab);
 
         SButton switchStyleButton = new SButton("Toggle WingSet styling");
         switchStyleButton.setShowAsFormComponent(false);
         switchStyleButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (customStyleApplied)
                     unstyleWingsetApp();
                 else
                     styleWingsetApp();
             }
         });
 
         SButton switchDebugViewButton = new SButton("Toggle AJAX debug view");
         switchStyleButton.setShowAsFormComponent(false);
         switchDebugViewButton.addScriptListener(new JavaScriptListener(
                 JavaScriptEvent.ON_CLICK,
                 "wingS.ajax.toggleDebugView(); return false;"
         ));
 
         SPanel south = new SPanel();
         south.setBorder(new SEmptyBorder(5, 0, 5, 0));
         south.add(switchStyleButton);
         south.add(new SLabel("  |  "));
         south.add(switchDebugViewButton);
 
         styleWingsetApp();
 
         frame.getContentPane().add(south, SBorderLayout.SOUTH);
         frame.getContentPane().setPreferredSize(SDimension.FULLAREA);
 
         frame.show();
     }
 
     /**
      * This method demonstrates some mehtods to influence the style of an wingS application
      */
     private void styleWingsetApp() {
         // 1) Apply custom HTML layout template as template for the root frame of the application
         try {
             URL templateURL = frame.getSession().getServletContext().getResource("/templates/ExampleFrame.thtml");
             if (templateURL != null) {
                 SRootLayout layout = new SRootLayout(templateURL);
                 frame.setLayout(layout);
             }
         } catch (java.io.IOException except) {
             log.warn("Exception", except);
         }
 
         // 2) Include an application specific CSS stylesheet to extend/overwrite the default wingS style set.
         customStyleSheetLink = new Link("stylesheet", null, "text/css", null, new DefaultURLResource("../css/wingset.css"));
         frame.addHeader(customStyleSheetLink);
 
         customStyleApplied = true;
     }
 
     /**
      * Revert styling done in {@link #styleWingsetApp()}
      */
     private void unstyleWingsetApp() {
         frame.setLayout(new SRootLayout());
         frame.removeHeader(customStyleSheetLink);
         tab.setAttribute(STabbedPane.SELECTOR_UNSELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, (SIcon) null);
         tab.setAttribute(STabbedPane.SELECTOR_SELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, (SIcon) null);
         customStyleApplied = false;
     }
 }
