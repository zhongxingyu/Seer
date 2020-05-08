 /*
  * $Id$
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
 import org.wings.event.SInternalFrameEvent;
 import org.wings.event.SInternalFrameListener;
 import org.wings.externalizer.AbstractExternalizeManager;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.io.Device;
 import org.wings.resource.DynamicResource;
 import org.wings.resource.FileResource;
 import org.wings.script.JavaScriptListener;
 import org.wings.script.ScriptListener;
 import org.wings.session.SessionManager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The Desktop example demonstrates the use of internal frames as well as
  * file upload and download.
  * SInternalFrames work very similar to their Swing pendants. The file upload
  * is left to the SFileChooser. Beware, that if you use one or more input
  * elements of type="file", you have to set the encoding of the surrounding form
  * to "multipart/form-data". This is a requirement of html. Download is a bit
  * tricky. The text has to be externalized for example by using the class
  * {@see org.wings.FileResource}. A JavaScriptListener, that is hooked to the
  * java script event "onload", is installed in the frame.
  * Look at the source, especially the method "save".
  * <p>
  * As of now, the menu item "save" in the "file" menu does not work as expected.
  * It is rendered as a href outside the form. Changes to text area don't take
  * effect. We could use javascript again, to trigger the required form submit.
  *
  * @author <a href="mailto:hengels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public class Editor
     extends Bird
     implements SInternalFrameListener
 {
     private DynamicResource saveResource;
     private SMenuBar menuBar;
     private SToolBar toolBar;
     private STextArea textArea;
 
     private String backup;
     private String clip;
 
     public Editor() {
         menuBar = createMenu();
         SContainer contentPane = getContentPane();
         contentPane.setLayout(new SBorderLayout());
         contentPane.add(menuBar, SBorderLayout.NORTH);
         toolBar = createToolBar();
 
         textArea = new STextArea();
         textArea.setColumns(80);
         textArea.setRows(12);
         textArea.setPreferredSize(SDimension.FULLWIDTH);
         
         SForm form = new SForm(new SFlowDownLayout());
         form.add(toolBar);
         form.add(textArea);
         contentPane.add(form, SBorderLayout.CENTER);
 
         saveResource = new EditorDynamicResource();
 
         SIcon icon = new SURLIcon("../icons/penguin.png");
         setIcon(icon);
         addInternalFrameListener(this);
     }
 
     protected SMenuBar createMenu() {
         SMenuItem saveItem = new SMenuItem("Save");
         saveItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 save();
             }
         });
         SMenuItem revertItem = new SMenuItem("Revert");
         revertItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 revert();
             }
         });
         SMenuItem closeItem = new SMenuItem("Close");
         closeItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 close();
             }
         });
 
         SMenu fileMenu = new SMenu("File");
         fileMenu.add(saveItem);
         fileMenu.add(revertItem);
         fileMenu.add(closeItem);
 
         SMenuItem cutItem = new SMenuItem("Cut");
         cutItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 cut();
             }
         });
         SMenuItem copyItem = new SMenuItem("Copy");
         copyItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 copy();
             }
         });
         SMenuItem pasteItem = new SMenuItem("Paste");
         pasteItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 paste();
             }
         });
 
         SMenu editMenu = new SMenu("Edit");
         editMenu.add(cutItem);
         editMenu.add(copyItem);
         editMenu.add(pasteItem);
 
         SMenuBar menuBar = new SMenuBar();
         menuBar.add(fileMenu);
         menuBar.add(editMenu);
 
         return menuBar;
     }
 
     protected SToolBar createToolBar() {
         try {
             SButton saveButton = new SButton(new SURLIcon("../icons/filesave.png"));
             saveButton.setToolTipText("save");
             saveButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     save();
                 }
             });
 
             SButton revertButton = new SButton(new SURLIcon("../icons/filerevert.png"));
             revertButton.setToolTipText("revert");
             revertButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     revert();
                 }
             });
             SButton closeButton = new SButton(new SURLIcon("../icons/fileclose.png"));
             closeButton.setToolTipText("close");
             closeButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     close();
                 }
             });
 
             SButton cutButton = new SButton(new SURLIcon("../icons/editcut.png"));
             cutButton.setToolTipText("cut");
             cutButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     cut();
                 }
             });
             SButton copyButton = new SButton(new SURLIcon("../icons/editcopy.png"));
             copyButton.setToolTipText("copy");
             copyButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     copy();
                 }
             });
             SButton pasteButton = new SButton(new SURLIcon("../icons/editpaste.png"));
             pasteButton.setToolTipText("paste");
             pasteButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     paste();
                 }
             });
 
             SToolBar toolBar = new SToolBar();
             toolBar.add(saveButton);
             toolBar.add(revertButton);
             toolBar.add(closeButton);
             toolBar.add(new SLabel("<html>&nbsp;"));
             toolBar.add(cutButton);
             toolBar.add(copyButton);
             toolBar.add(pasteButton);
 
             return toolBar;
         }
         catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
         }
         return new SToolBar();
     }
 
     public void setText(String text) {
         textArea.setText(text);
     }
     public String getText() { return textArea.getText(); }
 
     public void setBackup(String backup) {
         this.backup = backup;
     }
     public String getBackup() { return backup; }
 
     public void save() {
         try {
             //logger.info("save");
             // write editor content to a temporary file
             File file = File.createTempFile("wings", "txt");
             PrintWriter out = new PrintWriter(new FileOutputStream(file));
             out.print(textArea.getText());
             out.close();
 
             // create a file resource
             FileResource resource = new FileResource(file);
             // we only request this once, thats it.
             resource.setExternalizerFlags(resource.getExternalizerFlags()
                                           | ExternalizeManager.REQUEST);
             // advice the browser to pop the save dialog
             Map headers = new HashMap();
             headers.put("Content-Disposition", "attachment; filename=blub");
             resource.setHeaders(headers.entrySet());
 
             // java script, that is executed "onload"; loads the file resource and triggers
             // the named event "clear=X"
             RequestURL url = (RequestURL) resource.getURL();
             url.addParameter("clear=X");
 
             final ScriptListener script = new JavaScriptListener("onload", "parent.location='" + url + "'");
            parentFrame.addScriptListener(script);
 
             // register a request listener, that handles the named event "clear"
             getSession().getDispatcher().register(new LowLevelEventListener() {
                     public void processLowLevelEvent(String name, String[] values) {
                         //logger.info("remove java script");
                        parentFrame.removeScriptListener(script);
                     }
                     
                     public String getName() { return "clear"; }
 
                     public String getLowLevelEventId() { return ""; }
                     public String getEncodedLowLevelEventId() { return ""; }
                     
                     public void fireIntermediateEvents() {}
                     public void fireFinalEvents() {}
   		            public boolean isEnabled() { return true; }
 
                 public boolean checkEpoch() {
                     return true;
                 }
 
                 public boolean isEpochCheckEnabled() { return true; }
                 });
 
         }
         catch (IOException e) {
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
         }
     }
 
     public void revert() {
         textArea.setText(backup);
     }
 
     public void close() {
         super.dispose();
     }
 
     public void cut() {
         clip = textArea.getText();
         textArea.setText("");
     }
 
     public void copy() {
         clip = textArea.getText();
     }
 
     public void paste() {
         if (clip != null) {
             textArea.setText(textArea.getText() + clip);
         }
     }
 
     public void internalFrameClosed(SInternalFrameEvent e) {
         close();
     }
 
     public void internalFrameOpened(SInternalFrameEvent e) {}
     public void internalFrameIconified(SInternalFrameEvent e) {}
     public void internalFrameDeiconified(SInternalFrameEvent e) {}
     public void internalFrameMaximized(SInternalFrameEvent e) {
         textArea.setRows(24);
     }
     public void internalFrameUnmaximized(SInternalFrameEvent e) {
         textArea.setRows(12);
     }
 
     private class EditorDynamicResource extends DynamicResource
     {
         String id;
 
         public EditorDynamicResource() {
             super(null, "txt", "text/unknown");
         }
 
         public void write(Device out)
             throws IOException
         {
             out.print(textArea.getText());
         }
 
         public String getId() {
             if (id == null) {
                 ExternalizeManager ext = SessionManager.getSession().getExternalizeManager();
                 Map headers = new HashMap();
                 headers.put("Content-Disposition", "attachment; filename=blub");
                 id = ext.getId(ext.externalize(this, headers.entrySet(), AbstractExternalizeManager.REQUEST));
                 //logger.debug("new " + this.getClass().getName() + " with id " + id);
             }
             return id;
         }
     }
 }
