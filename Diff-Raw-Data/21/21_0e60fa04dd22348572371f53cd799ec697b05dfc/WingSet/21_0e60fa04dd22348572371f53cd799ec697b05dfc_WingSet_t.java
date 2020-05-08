 /*
  * $Id$
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
 
 
 import org.wings.*;
 import org.wings.style.CSSProperty;
 import org.wings.header.Link;
 import org.wings.resource.DefaultURLResource;
 import org.wings.session.WingsStatistics;
 import org.wings.session.Browser;
 import org.wings.session.BrowserType;
 import org.wings.util.TimeMeasure;
 
 import java.io.FileWriter;
 import java.io.Serializable;
 import java.io.File;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class WingSet implements Serializable {
     private final static SIcon JAVA_CUP_ICON = new SResourceIcon("org/wings/icons/JavaCup.gif");
     private final static SIcon SMALL_COW_ICON = new SURLIcon("../icons/cowSmall.gif");
     private final static SURLIcon STANDARD_TAB_BACKGROUND = new SURLIcon("../icons/ButtonsBackground.gif");
     private final static SURLIcon SELECTED_TAB_BACKGROUND = new SURLIcon("../icons/ButtonsBackgroundHighlighted.gif");
 
     static final boolean SHOW_STATISTICS = false;
     static final long birthday = System.currentTimeMillis();
     static FileWriter infoWriter;
     static final Timer timer = new Timer();
     static long oldRequestCount = 0;
     static long oldSessionCount = 0;
 
     private SFrame frame;
     // time measurement (a little hacky)
     private SLabel timeMeasure;
     private final TimeMeasure stopWatch;
 
     static final TimerTask infoTask = new TimerTask() {
         public void run() {
             StringBuffer result = new StringBuffer();
             long totalmem = Runtime.getRuntime().totalMemory();
             long freemem = Runtime.getRuntime().freeMemory();
 
             WingsStatistics stats = WingsStatistics.getStatistics();
 
             result.append(System.currentTimeMillis()).append(' ')
                     .append(stats.getUptime()).append(' ')
                     .append(stats.getOverallSessionCount()).append(' ')
                     .append(stats.getOverallSessionCount() - oldSessionCount).append(' ')
                     .append(stats.getActiveSessionCount()).append(' ')
                     .append(stats.getAllocatedSessionCount()).append(' ')
                     .append(stats.getRequestCount()).append(' ')
                     .append(stats.getRequestCount() - oldRequestCount).append(' ')
                     .append(totalmem).append(' ')
                     .append(freemem).append(' ')
                     .append(totalmem - freemem).append('\n');
 
             oldRequestCount = stats.getRequestCount();
             oldSessionCount = stats.getOverallSessionCount();
 
             try {
                 infoWriter.write(result.toString());
                 infoWriter.flush();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     };
 
     static {
         if (SHOW_STATISTICS) {
             try {
                 infoWriter = new FileWriter(File.createTempFile("wingsmemory","log"), false);
 
                 StringBuffer result = new StringBuffer();
                 result.append("timestamp").append(' ')
                         .append("uptime").append(' ')
                         .append("overall_sessions").append(' ')
                         .append("new_sessions").append(' ')
                         .append("active_sessions").append(' ')
                         .append("allocated_sessions").append(' ')
                         .append("overall_processed requests").append(' ')
                         .append("processed_requests").append(' ')
                         .append("total_memory").append(' ')
                         .append("free_memory").append(' ')
                         .append("used_memory").append('\n');
                 infoWriter.write(result.toString());
                 infoWriter.flush();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
 
             timer.scheduleAtFixedRate(infoTask,0,10 * 1000);
         }
     }
 
     public WingSet() {
         frame = new SFrame("WingSet");
         frame.setTitle("WingSet Demo");
         frame.setAttribute(CSSProperty.MARGIN, "8px !important");
 
         // Register our user style sheet
         final Browser browser = frame.getSession().getUserAgent();
         String cssURL = "../wingset-gecko.css"; //"../wingset-" + browser.getBrowserType().getShortName() + ".css";
         if (browser.getBrowserType().equals(BrowserType.IE))
             cssURL = "../wingset-msie.css";
         frame.addHeader(new Link("stylesheet", null, "text/css", null, new DefaultURLResource(cssURL)));
 
         stopWatch = new TimeMeasure(new MessageFormat("<html><b>{0}</b>: {1} (<i>x {2}</i>)<br/>"));
         timeMeasure = new SLabel();
 
         SContainer contentPane = frame.getContentPane();
         try {
             URL templateURL = frame.getSession().getServletContext().getResource("/templates/ExampleFrame.thtml");
             if (templateURL != null) {
                 SRootLayout layout = new SRootLayout(templateURL);
                 frame.setLayout(layout);
             }
         } catch (java.io.IOException except) {
             except.printStackTrace();
         }
         
         final STabbedPane tab = new STabbedPane();
         tab.setName("examples");
         tab.setTabPlacement(SConstants.TOP);
 
         tab.setAttribute(STabbedPane.SELECTOR_UNSELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, STANDARD_TAB_BACKGROUND);
         tab.setAttribute(STabbedPane.SELECTOR_SELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, SELECTED_TAB_BACKGROUND);
 
         tab.add(new WingsImage(), "wingS!");
         tab.add(new LabelExample(), "Label");
         tab.add(new BorderExample(), "Border");
         tab.add(new TextComponentExample(), "Text Component");
         tab.addTab("Tree", JAVA_CUP_ICON, new TreeExample(), "Tree Tool Tip");
         tab.add(new OptionPaneExample(), "OptionPane");
         tab.add(new TableExample(), "Table");
         tab.add(new ListExample(), "List");
         tab.add(new ButtonExample(), "Button");
         tab.add(new ToggleButtonExample(), "ToggleButton");
         tab.add(new CheckBoxExample(), "CheckBox");
         tab.add(new RadioButtonExample(), "RadioButton");
         tab.add(new Faces(), "Faces");
         tab.add(new FileChooserExample(), "FileChooser");
         tab.add(new ScrollPaneExample(), "ScrollPane");
         tab.add(new PageScrollerExample(), "PageScroller");
         tab.add(new MenuExample(), "Menu");
         tab.add(new TabbedPaneExample(), "Tabbed Pane");
         tab.addTab("Template Layout", SMALL_COW_ICON, new TemplateExample(), "Template Layout Manager");
         tab.add(new InteractiveTemplateExample(), "Interactive Template");
         tab.add(new ProgressBarExample(), "ProgressBar");
         tab.add(new MemUsageExample(), "Memory Usage");
         tab.add(new JavaScriptListenerExample(), "Script Listener");
         tab.add(new PopupExample(), "Popup Menu");
         tab.add(new KeyboardBindingsExample(), "Keyboard Bindings");
         tab.add(new DynamicLayoutExample(), "Dynamic Layouts");
         tab.add(new BackButtonExample(),"Browser Back");
         tab.add(new DesktopPaneExample(),"DesktopPane");
         tab.add(new DragAndDropExample(),"Drag and Drop");
        tab.add(new ErrorPageExample(),"Error Page");
 
         contentPane.add(tab, "WingSetApp");
         contentPane.add(timeMeasure, "TimeLabel");
 
         frame.show();
 
         /*  for testing purpose, to get information what events are requested
 
         final SLabel paramLabel = new SLabel();
         contentPane.add(paramLabel, "ParamLabel");
 
         SessionManager.getSession().addRequestListener(new SRequestListener() {
                 public void processRequest(SRequestEvent e) {
                     if ( e.getType()==SRequestEvent.DISPATCH_START ) {
                         StringBuffer label = new StringBuffer();
 
                         HttpServletRequest req = 
                             SessionManager.getSession().getServletRequest();
                         
                         for (Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
                             String paramName = (String)en.nextElement();
                             String[] value = req.getParameterValues(paramName);
 
                             label.append(paramName).append(": ").
                                 append(value[0]).append("<br>");
                         }
 
                         paramLabel.setText(label.toString());
                     }
                 }
             });
         */
 
     }
 }
 
 
