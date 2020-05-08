 /*
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of the wingS demo (http://j-wings.org).
  *
  * The wingS demo is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package desktop;
 
 import org.wings.*;
 import org.wings.session.SessionManager;
 import org.wings.dnd.DragSource;
 import org.wings.resource.DefaultURLResource;
 import org.wings.header.Link;
 import org.wings.event.SContainerEvent;
 import org.wings.event.SContainerListener;
 import org.wings.style.CSSProperty;
 import org.wings.util.ComponentVisitor;
 import org.wings.event.SComponentDropListener;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.*;
 import java.io.File;
 import java.io.FileReader;
 import java.io.Reader;
 import java.io.StringWriter;
 /**
  * @author Holger Engels
  */
 public class Desktop
     implements SConstants
 {
     SFrame frame;
     DesktopPane desktop;
     DesktopPane feeds;
     SMenu windowMenu;
     SMenuBar menuBar;
     int editorNumber = 0;
 
     public Desktop() {
         frame = new SFrame("Desktop");
         frame.setAttribute(CSSProperty.MARGIN, "4px");
 
         menuBar = createMenu();
 
         
         desktop = new DesktopPane();
         
         // add the frames to the window-menu ..
         desktop.addContainerListener(new SContainerListener() {
                 public void componentAdded(SContainerEvent e) {
                     SInternalFrame frame = (SInternalFrame) e.getChild();
                     SMenuItem windowItem = new WindowMenuItem(desktop, frame);
                     menuBar.remove(windowMenu);
                     windowMenu.add(windowItem);
                     menuBar.add(windowMenu);
                 }
                 public void componentRemoved(SContainerEvent e) {
                     SInternalFrame frame = (SInternalFrame) e.getChild();
                     SMenuItem windowItem = new WindowMenuItem(frame);
                     menuBar.remove(windowMenu);
                     windowMenu.remove(windowItem);
                     menuBar.add(windowMenu);
                 }
             });
         
         desktop.setVerticalAlignment(SConstants.TOP_ALIGN);
 		
         feeds = new DesktopPane();
         
         feeds.addContainerListener(new SContainerListener() {
             public void componentAdded(SContainerEvent e) {
                 SInternalFrame frame = (SInternalFrame) e.getChild();
                 SMenuItem windowItem = new WindowMenuItem(feeds, frame);
                 menuBar.remove(windowMenu);
                 windowMenu.add(windowItem);
                 menuBar.add(windowMenu);
             }
             public void componentRemoved(SContainerEvent e) {
                 SInternalFrame frame = (SInternalFrame) e.getChild();
                 SMenuItem windowItem = new WindowMenuItem(frame);
                 menuBar.remove(windowMenu);
                 windowMenu.remove(windowItem);
                 menuBar.add(windowMenu);
             }
         });
         
         feeds.setVerticalAlignment(SConstants.TOP_ALIGN);
 
         SContainer contentPane = frame.getContentPane();
         contentPane.setLayout(new SGridBagLayout());
 
         GridBagConstraints c = new GridBagConstraints();
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.weightx = 1.0;
         contentPane.add(menuBar, c);
         c.gridwidth = GridBagConstraints.RELATIVE;
         c.weightx = 0.7;
         contentPane.add(desktop, c);
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.weightx = 0.3;
         contentPane.add(feeds, c);
 
         frame.setVisible(true);
         newEditor().setText(getStory());
         desktop.addNonFrameComponent(new DropLabel());
         feeds.add(new RSSPortlet("heise news ticker", "http://www.heise.de/english/newsticker/news.rdf"));
         feeds.add(new RSSPortlet("the server side", "http://www.theserverside.com/rss/theserverside-rss2.xml"));
         feeds.addNonFrameComponent(new DropLabel());
         
         
         frame.addHeader(new Link("stylesheet", null, "text/css", null, new DefaultURLResource("../desktop.css")));
         frame.show();
     }
 
     protected String getStory() {
         return "Ein Philosoph ist jemand, der in einem absolut dunklen Raum " +
             "mit verbundenen Augen nach einer schwarzen Katze sucht, die gar nicht " +
             "da ist. Ein Theologe ist jemand der genau das gleiche macht und ruft: " +
             "\"ich hab sie!\"";
     }
 
     protected SMenuBar createMenu() {
         SMenuItem newFeedItem = new SMenuItem("New News-Feed");
         newFeedItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 newFeed();
             }
         });
         SMenuItem newItem = new SMenuItem("New Editor");
         newItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 newEditor();
             }
         });
         SMenuItem openItem = new SMenuItem("Open File in Editor");
         openItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 openEditor();
             }
         });
 
         SMenu fileMenu = new SMenu("File");
         fileMenu.add(newFeedItem);
         fileMenu.add(newItem);
         fileMenu.add(openItem);
 
         windowMenu = new SMenu("Window");
 
         SMenuBar menuBar = new SMenuBar();
         menuBar.add(fileMenu);
         menuBar.add(windowMenu);
 
         return menuBar;
     }
 
     private void newFeed() {
         final STextField inputElement = new STextField("file:///home/hengels/IssueNavigator.jspa.xml");
         SOptionPane.showInputDialog(desktop, "URL of the News-Feed", "News-Feed", inputElement, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                feeds.add(new RSSPortlet("custom", inputElement.getText()), 0);
             }
         });
     }
 
     /**
      * Servletinfo
      */
     public String getServletInfo() {
         return "Desktop ($Revision$)";
     }
     
     /**
      * add a new frame. If there is a frame, that is maximized, then
      * move the maximization to the new frame.
      */
     public void addNewFrame(final SInternalFrame frame) {
         desktop.add(frame, 0);
         try {
             desktop.invite(new ComponentVisitor() {
                     public void visit(SComponent c) { /* ign */ }
                     public void visit(SContainer c) {
                         if (! (c instanceof SInternalFrame))
                             return;
                         SInternalFrame ff = (SInternalFrame) c;
                         if (ff != frame && ff.isMaximized()) {
                             ff.setMaximized(false);
                             // set _our_ frame maximized, then.
                             frame.setMaximized(true);
                         }
                     }
                 });
         }
         catch (Exception e) {
             System.err.println(e);
         }
     }
     public Editor newEditor() {
         Editor editor = new Editor();
         editor.setTitle("Editor [" + (++editorNumber) + "]");
         addNewFrame(editor);
         return editor;
     }
 
     public Editor openEditor() {
         final Editor editor = new Editor();
         addNewFrame(editor);
         
         final SFileChooser chooser = new SFileChooser();
         chooser.setColumns(20);
 
         final SOptionPane dialog = new SOptionPane();
         dialog.setEncodingType("multipart/form-data");
         dialog.showInput(editor, "Choose file", chooser, "Open file");
         dialog.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     if ( evt.getActionCommand()==SOptionPane.OK_ACTION ) {
                         try {
                             File file = chooser.getFile();
                             Reader reader = new FileReader(file);
                             StringWriter writer = new StringWriter();
                             
                             int b;
                             while ((b = reader.read()) >= 0)
                                 writer.write(b);
                             
                             editor.setText(writer.toString());
                             editor.setTitle(chooser.getFileName());
                             chooser.reset();
                         }
                         catch (Exception e) {
                             dialog.show(editor); // show again ..
                             // .. but first, show error-message on top ..
                             SOptionPane.showMessageDialog(editor, 
                                                       "Error opening file", 
                                                           e.getMessage());
                         }
                     }
                     else {
                         editor.close();
                     }
                 }
             });
 
         return editor;
     }
 
     /**
      * A menu item, that handles the position of an internal frame within
      * the desktop - whenever it is clicked, the frame is put on top. This
      * MenuItem is added in some component listener, that gets activated
      * whenever a window is added or removed from the desktop.
      */
     private static class WindowMenuItem extends SMenuItem {
         private final SInternalFrame frame;
         
         public WindowMenuItem(SInternalFrame f) {
             frame = f;
         }
 
         public WindowMenuItem(final SDesktopPane d, final SInternalFrame f) {
             frame = f;
             /*
              * when clicked, put that frame on top. If some other frame was
              * maximized at that point, then maximize _our_ frame instead.
              */
             addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         d.setPosition(f, 0);
                         if (f.isIconified()) {
                             f.setIconified(false);
                         }
                         /*
                          * if some other frame is maximized, then we want
                          * to toggle this maximization..
                          */
                         try {
                             d.invite(new ComponentVisitor() {
                                     public void visit(SComponent c) { /*ign*/ }
                                     public void visit(SContainer c) {
                                         if (! (c instanceof SInternalFrame))
                                             return;
                                         SInternalFrame ff = (SInternalFrame) c;
                                         if (ff != frame && ff.isMaximized()) {
                                             ff.setMaximized(false);
                                             // set _our_ frame maximized, then.
                                             frame.setMaximized(true);
                                         }
                                     }
                                 });
                         }
                         catch (Exception e) {
                             System.err.println(e);
                         }
                     }
                 });
         }
 
         /**
          * returns the title of the frame.
          */
         public String getText() {
             String title = frame.getTitle();
             return (title == null || title.length()==0) ? "[noname]" : title;
         }
         
         /**
          * remove menu item by frame ..
          */
         public boolean equals(Object o) {
             if (o instanceof WindowMenuItem) {
                 WindowMenuItem wme = (WindowMenuItem) o;
                 return (frame != null && wme.frame == frame);
             }
             return false;
         }
     }
 }
