 package jmonkey.office.jwp;
 
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyVetoException;
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultDesktopManager;
 import javax.swing.DesktopManager;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JInternalFrame;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import jmonkey.export.Registry;
 import jmonkey.export.Runtime;
 import jmonkey.office.jwp.DynamicFileFilter;
 import jmonkey.office.jwp.support.Code;
 import jmonkey.office.jwp.support.Editor;
 import jmonkey.office.jwp.support.FileActionListener;
 import jmonkey.office.jwp.support.Mime;
 
 /**
  * This class manages the active document frames in the JWP application.
  */
 public final class DocumentManager extends DefaultDesktopManager implements
     DesktopManager, FileActionListener {
     
   private JWP p = null;
   private Registry ry = null;
   private DocumentFrame cd = null;
   private String cdir = null;
   
   private final String ndt;    
 
   public DocumentManager(JWP parent) {
     super();
	if(parent == null){
		throw new IllegalArgumentException();
	}
     p = parent;
     ry = parent.getRegistry();
     ndt = ry.getString("MAIN", "new.document.title");
     cdir = ry.getString("USER", "default.documents.directory");
     Runtime.ensureDirectory(cdir);
     init();
   }
 
   public void editorNew() {
     Code.debug("editorNew");
     createDocumentFrame();
   }
 
   public void editorOpen(File file) {
     Code.debug("editorOpen(File)");
     if (file != null) {
       String mime = Mime.findContentType(file);
       try {
         DocumentFrame doc = createDocumentFrame(null, file.getName(), mime);
         Editor ed = doc.getEditor();
         ed.read(file);
         ed.setFile(file);
       }
       catch (IOException ioe0) {
         showException(ioe0);
       }
     }
   }
 
   public void editorOpen() {
     Code.debug("editorOpen");
 
     JFileChooser chooser = new JFileChooser();
     chooser.setDialogTitle(JWP.getMessage("dialog.open.title"));
     chooser.setDialogType(JFileChooser.OPEN_DIALOG);
     chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
     FileFilter filter = null;
     filter = new DynamicFileFilter("java c cc cpp h txt text",
         "filter.plain.label");
     chooser.addChoosableFileFilter(filter);
     filter = new DynamicFileFilter("rtf", "filter.rtf.label");
     chooser.addChoosableFileFilter(filter);
     filter = new DynamicFileFilter("htm html shtml", "filter.html.label");
     chooser.addChoosableFileFilter(filter);
     filter = chooser.getAcceptAllFileFilter();
     chooser.addChoosableFileFilter(filter);
 
     chooser.setCurrentDirectory(new File(cdir));
 
     chooser.showOpenDialog(p);
     editorOpen(chooser.getSelectedFile());
     
     cdir = chooser.getCurrentDirectory().getAbsolutePath();
   }
 
   public void editorOpenAs() {
     Code.debug("editorOpenAs");
 
     JFileChooser chooser = new JFileChooser();
     chooser.setDialogTitle(JWP.getMessage("dialog.openas.title"));
     chooser.setDialogType(JFileChooser.OPEN_DIALOG);
     chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
     FileFilter plainFilter =
       new DynamicFileFilter("*", "filter.any.plain.label");
     chooser.addChoosableFileFilter(plainFilter);
     FileFilter rtfFilter = 
       new DynamicFileFilter("*", "filter.any.rtf.label");
     chooser.addChoosableFileFilter(rtfFilter);
     FileFilter htmlFilter = 
       new DynamicFileFilter("*", "filter.any.html.label");
     chooser.addChoosableFileFilter(htmlFilter);
     FileFilter allFilter = chooser.getAcceptAllFileFilter();
     chooser.addChoosableFileFilter(allFilter);
 
     chooser.setCurrentDirectory(new File(cdir));
     chooser.showOpenDialog(p);
     cdir = chooser.getCurrentDirectory().getAbsolutePath();
     
     File fileToOpen = chooser.getSelectedFile();
     if (fileToOpen != null) {
       FileFilter filterUsed = chooser.getFileFilter();
       String mime = (filterUsed == plainFilter) ? "text/plain" :
         (filterUsed == rtfFilter) ? "text/rtf" :
         (filterUsed == htmlFilter) ?"text/html" : "text/plain";
       try {
         DocumentFrame df = 
           createDocumentFrame(null, fileToOpen.getName(), mime);
         df.getEditor().read(fileToOpen);
         p.addToFileHistory(fileToOpen);
       }
       catch (IOException ioe0) {
         showException(ioe0);
       }
     }
   }
 
   public void editorRevert(Editor editor) {
     Code.debug("editorRevert");
     if (editor.getFile() == null) {
       String msg = JWP.getMessage("dialog.revert.warning.0");
       JOptionPane.showMessageDialog(p, msg, "Bad State", 
                                     JOptionPane.ERROR_MESSAGE);
     }
     else {
       String msg = JWP.getMessage("dialog.revert.warning.1");
       String query = JWP.getMessage("dialog.revert.query");
       int answer = JOptionPane.showConfirmDialog(getParent(), msg,
           query, JOptionPane.YES_NO_CANCEL_OPTION,
           JOptionPane.WARNING_MESSAGE);
       if (answer == JOptionPane.YES_OPTION) {
         if (!editor.isEmpty()) {
           editor.getTextComponent().setText("");
         }
         try {
           editor.read(editor.getFile());
         }
         catch (IOException ioe0) {
           showException(ioe0);
         }
       }
     }
   }
 
   public void editorSave(Editor editor) {
     Code.debug("editorSave");
     try {
       if (editor.getFile() == null) {
         JFileChooser chooser = 
           new JFileChooser(JWP.getMessage("dialog.save.title"));
         chooser.setDialogType(JFileChooser.SAVE_DIALOG);
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);        
         chooser.setCurrentDirectory(new File(cdir));  
         
         String ct = editor.getContentType();
         configureFileType(chooser, ct);
         chooser.showSaveDialog(p);
         cdir = chooser.getCurrentDirectory().getAbsolutePath();
         
         File fileToOpen = chooser.getSelectedFile();
         String mime = Mime.findContentType(fileToOpen);
         
         if (!fileToOpen.getName().startsWith("*")) {
           if (!ct.equals(mime)) {
             String msg = JWP.getMessage("dialog.save.warning.0");
             String query = JWP.getMessage("dialog.save.query");
             int answer = JOptionPane.showConfirmDialog(p, msg, 
                 query, JOptionPane.YES_NO_CANCEL_OPTION,
                 JOptionPane.WARNING_MESSAGE);
             if (answer == JOptionPane.YES_OPTION) {
               editor.write(fileToOpen);
               editor.setFile(fileToOpen);
               cd.setTitle(fileToOpen.getName());
             }
           }
           else {
             editor.write(fileToOpen);
             editor.setFile(fileToOpen);
             cd.setTitle(fileToOpen.getName());
           }
         }
       }
       else if (editor.isNew() || editor.isChanged()) {
         editor.write(editor.getFile());
       }
     }
     catch (IOException ioe0) {
       showException(ioe0);
     }
   }
 
   public void editorSaveAs(Editor editor) {
     Code.debug("editorSaveAs");
 
     JFileChooser chooser = new JFileChooser();
     chooser.setDialogTitle(JWP.getMessage("dialog.saveas.title"));
     chooser.setDialogType(JFileChooser.SAVE_DIALOG);
     chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
     chooser.setCurrentDirectory(new File(cdir));
 
     String ct = editor.getContentType();
     configureFileType(chooser, ct);
 
     chooser.showSaveDialog(p);
     cdir = chooser.getCurrentDirectory().getAbsolutePath();
     File fileToOpen = chooser.getSelectedFile();
 
     String mime = Mime.findContentType(fileToOpen);
     if (!fileToOpen.getName().startsWith("*")) {
       try {
         if (!ct.equals(mime)) {
           String msg = JWP.getMessage("dialog.save.warning.0");
           String query = JWP.getMessage("dialog.save.query");
           int answer = JOptionPane.showConfirmDialog(p, msg,
               query, JOptionPane.YES_NO_CANCEL_OPTION,
               JOptionPane.WARNING_MESSAGE);
           if (answer == JOptionPane.YES_OPTION) {
             editor.write(fileToOpen);
             editor.setFile(fileToOpen);
             cd.setTitle(fileToOpen.getName());
           }
         }
         else {
           editor.write(fileToOpen);
           editor.setFile(fileToOpen);
           cd.setTitle(fileToOpen.getName());
         }
       }
       catch (IOException ioe0) {
         showException(ioe0);
       }
     }
   }
   
   private void configureFileType(JFileChooser chooser, String ct) {
     String defaultExt = "*.*";
     FileFilter filter;
     if (ct.equals("text/rtf")) {
       filter = new DynamicFileFilter("rtf", "filter.rtf.label");
       defaultExt = "*.rtf";
     }
     else if (ct.equals("text/html")) {
       filter = new DynamicFileFilter("htm html shtml", 
                                      "filter.html.label");
       defaultExt = "*.html";
     }
     else if (ct.equals("text/plain")) {
       filter = new DynamicFileFilter("java c cc cpp h txt text",
                                      "filter.plain.label");
       defaultExt = "*.txt";
     }
     else {
       filter = chooser.getAcceptAllFileFilter();
       defaultExt = "*.*";
     }
     chooser.setFileFilter(filter);
 
     chooser.setSelectedFile(new File(defaultExt));
   }
 
   public void editorSaveCopy(Editor editor) {
     Code.debug("editorSaveCopy");
 
     JFileChooser chooser = new JFileChooser();
     chooser.setDialogTitle(JWP.getMessage("dialog.savecopy.title"));
     chooser.setDialogType(JFileChooser.SAVE_DIALOG);
     chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
     chooser.setCurrentDirectory(new File(cdir));
 
     String ct = editor.getContentType();
     configureFileType(chooser, ct);
 
     chooser.showSaveDialog(p);
     cdir = chooser.getCurrentDirectory().getAbsolutePath();
     File fileToOpen = chooser.getSelectedFile();
 
     String mime = Mime.findContentType(fileToOpen);
     if (!fileToOpen.getName().startsWith("*")) {
       try {
         if (!ct.equals(mime)) {
           String msg = JWP.getMessage("dialog.save.warning.0");
           String query = JWP.getMessage("dialog.save.query");
           int answer = JOptionPane.showConfirmDialog(p, msg,                
               query, JOptionPane.YES_NO_CANCEL_OPTION,
               JOptionPane.WARNING_MESSAGE);
           if (answer == JOptionPane.YES_OPTION) {
             editor.write(fileToOpen);
           }
         }
         else {
           editor.write(fileToOpen);
         }
       }
       catch (IOException ioe0) {
         showException(ioe0);
       }
     }
   }
 
   protected final DocumentFrame 
       createDocumentFrame(File file, String title, String contentType) {
     Code.debug("Creating New Document: " + title);
     DocumentFrame doc = new DocumentFrame(p, contentType);
     doc.setIconifiable(true);
     doc.setResizable(true);
     doc.setMaximizable(true);
     doc.setTitle(title + " [" + contentType + "]");
     JWP.getDesktop().add(doc, doc.getName());
     cascade(doc);
     cd = doc;
     cd.activate();
     p.updateOpenWindowsMenu();
     if (file != null) {
       if (file.exists() & file.isFile()) {
         editorOpen(file);
       }
     }
     doc.setVisible(true);
     return doc;
   }
 
 
   protected final DocumentFrame createDocumentFrame(File file) {
     return createDocumentFrame(file, 
        ndt + p.getDocumentNumber(), 
        ry.getString("MAIN", "default.content.type"));
   }
 
   protected final DocumentFrame createDocumentFrame() {
     return createDocumentFrame(
         null,
         ndt + (p.getDocumentNumber() + 1),
         ry.getString("MAIN", "default.content.type"));
   }
 
   public final String[] openDocumentList() {
     Code.debug("Getting Open Document List...");
     Vector v = new Vector();
     Component[] comps = JWP.getDesktop().getComponents();
     for (int i = 0; i < comps.length; i++) {
       try {
         if (comps[i] instanceof JInternalFrame) {
           v.addElement(((JInternalFrame) comps[i]).getTitle());
         }
       }
       catch (java.lang.ClassCastException cEX) {
       }
 
     }
     String[] names = new String[v.size()];
     v.copyInto(names);
     return names;
 
   }
 
   public final DocumentFrame getOpenDocument(String name) {
     Component[] comps = JWP.getDesktop().getComponents();
     for (int i = 0; i < comps.length; i++) {
 
       if (comps[i] instanceof JInternalFrame) {
         if (((JInternalFrame) comps[i]).getTitle().equals(name)) {
           return (DocumentFrame) ((JInternalFrame) comps[i]);
         }
       }
     }
     throw new IllegalStateException("The document " + name
         + " does not exist, or no longer exists.");
   }
 
   public void activateFrame(javax.swing.JInternalFrame f) {
     cd = (DocumentFrame) f;
     if (p != null) {
       switchedDocument(cd, false);
     }
     super.activateFrame(f);
     cd.activate();
   }
 
 
   protected final DocumentFrame active() {
     return cd;
   }
 
 
 
   private void init() {
   }
 
   protected final void switchedDocument(DocumentFrame frame,
       boolean textSelected) {
     p.switchedDocumentFrame(frame, textSelected);
   }
 
   protected final void cascade(DocumentFrame dframe) {
     Dimension dsize = JWP.getDesktop().getSize();
     int targetWidth = 3 * dsize.width / 4;
     int targetHeight = 3 * dsize.height / 4;
     int nextX = 0;
     int nextY = 0;
     if (cd != null) {
       if (cd.isMaximum()) {
         try {
           dframe.setMaximum(true);
         }
         catch (java.beans.PropertyVetoException pve0) {
         }
         return;
       }
       java.awt.Point p = cd.getLocation();
       nextX = p.x;
       nextY = p.y;
 
       nextX += 24;
       nextY += 24;
     }
 
     if ((nextX + targetWidth > dsize.width)
         || (nextY + targetHeight > dsize.height)) {
       nextX = 0;
       nextY = 0;
     }
     JWP.getDesktop().getDesktopManager().setBoundsForFrame(dframe, nextX, nextY,
         targetWidth, targetHeight);
   }
 
 
   protected final class CascadeAction extends AbstractAction {
     public CascadeAction() {
       super("Cascade Windows");
     }
 
     public void actionPerformed(ActionEvent e) {
       cascadeAll();
     }
   }
 
   protected final Action getCascadeAction() {
     return new CascadeAction();
   }
 
 
   protected final void cascadeAll() {
     Component[] comps = JWP.getDesktop().getComponents();
     Dimension dsize = JWP.getDesktop().getSize();
     int targetWidth = 3 * dsize.width / 4;
     int targetHeight = 3 * dsize.height / 4;
     int nextX = 0;
     int nextY = 0;
     for (int i = 0; i < comps.length; i++) {
       if (comps[i] instanceof JInternalFrame & comps[i].isVisible()
           & !((comps[i] instanceof JInternalFrame.JDesktopIcon))) { //fixed if it is an icon, comps[i] cant be case to JInternalFrame
     	  											 //As it is a JInternalFrame.JDesktopIcon
     	  											 //JInternalFrame) comps[i]).isIcon()->comps[i] instanceof JInternalFrame.JDesktopIcon
         if ((nextX + targetWidth > dsize.width)
             || (nextY + targetHeight > dsize.height)) {
           nextX = 0;
           nextY = 0;
         }
         JWP.getDesktop().getDesktopManager().setBoundsForFrame(
             (JComponent) comps[i], nextX, nextY, targetWidth, targetHeight);
         ((JInternalFrame) comps[i]).toFront();
         nextX += 24;
         nextY += 24;
       }
     }
   }
 
 
   protected final class CloseAction extends AbstractAction {
     public CloseAction() {
       super("Close");
     }
 
     public void actionPerformed(ActionEvent e) {
       if(closeActiveDocument()){//fixed
     	  
           DocumentFrame actOnDoc= active();
 
           actOnDoc.dispose();
     	  
       }
       
     }
   }
 
   protected final Action getCloseAction() {
     return new CloseAction();
   }
 
 
   protected final boolean closeActiveDocument() {
     Editor ed = active().getEditor();
     if (ed.isChanged()) {
       switch (JOptionPane.showConfirmDialog(p,
           "Document Changed!\n\"" + active().getTitle()
               + "\"\nDo you want to save the changes?", "Save Changes?",
           JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
       case JOptionPane.YES_OPTION:
         if (ed.isNew()) {
           editorSaveAs(ed);
         }
         else {
           editorSave(ed);
         }
         return !ed.isChanged();
       case JOptionPane.NO_OPTION:
         return true;
       default:
         return false;
       }
     }
     else {
       //System.exit(0);
       return true;
     }
   }
 
   protected final class CloseAllAction extends AbstractAction {
     public CloseAllAction() {
       super("Close All");
     }
 
     public void actionPerformed(ActionEvent e) {
       closeAllDocuments();
     }
   }
 
   protected final Action getCloseAllAction() {
     return new CloseAllAction();
   }
 
 
   protected final void closeAllDocuments() {
     Component[] comps = JWP.getDesktop().getComponents();
     for (int i = 0; i < comps.length; i++) {
       if (comps[i] instanceof DocumentFrame & 
           comps[i].isVisible() & 
           ((JInternalFrame) comps[i]).isClosable()) {        
 
         DocumentFrame actOnDoc = ((DocumentFrame) comps[i]);
         actOnDoc.activate();
         try {
           actOnDoc.setClosed(true);
         }
         catch (Throwable t) {
         }
       }
     }
     cd = null;
   }
 
 
   protected final class MinimizeAction extends AbstractAction {
     public MinimizeAction() {
       super("Minimize Windows");
     }
 
     public void actionPerformed(ActionEvent e) {
       minimizeAll();
     }
   }
 
   protected final Action getMinimizeAction() {
     return new MinimizeAction();
   }
 
 
   protected final void minimizeAll() {
     Component[] comps = JWP.getDesktop().getComponents();
     for (int i = 0; i < comps.length; i++) {
       if (comps[i] instanceof JInternalFrame && comps[i].isVisible()
           && !((JInternalFrame) comps[i]).isIcon()
           && ((JInternalFrame) comps[i]).isIconifiable()) {
         try {
           ((JInternalFrame) comps[i]).setIcon(true);
         }
         catch (java.beans.PropertyVetoException pve0) {
         }
       }
     }
   }
 
 
   protected final class TileAction extends AbstractAction {
     public TileAction() {
       super("Tile Windows");
     }
 
     public void actionPerformed(ActionEvent e) {
       tileAll();
     }
   }
 
   protected final Action getTileAction() {
     return new TileAction();
   }
 
 
   protected final void tileAll() {
     if (JWP.getDesktop().getDesktopManager() == null) {
       return;
     }
     Component[] comps = JWP.getDesktop().getComponents();
     Component comp;
     int count = 0;
 
     for (int i = 0; i < comps.length; i++) {
       comp = comps[i];
       if (comp instanceof JInternalFrame && comp.isVisible()
           && !((JInternalFrame) comp).isIcon()) {
         count++;
       }
     }
     if (count != 0) {
       double root = Math.sqrt(count);
       int rows = (int) root;
       int columns = count / rows;
       int spares = count - (columns * rows);
       Dimension paneSize = JWP.getDesktop().getSize();
       int columnWidth = paneSize.width / columns;
 
       int availableHeight = paneSize.height - 48;
       int mainHeight = availableHeight / rows;
       int smallerHeight = availableHeight / (rows + 1);
       int rowHeight = mainHeight;
       int x = 0;
       int y = 0;
       int thisRow = rows;
       int normalColumns = columns - spares;
       for (int i = comps.length - 1; i >= 0; i--) {
         comp = comps[i];
         if (comp instanceof JInternalFrame && comp.isVisible() &&
             !((JInternalFrame) comp).isIcon()) {
           DesktopManager dm = JWP.getDesktop().getDesktopManager();
           dm.setBoundsForFrame((JComponent) comp, x, y, 
                                columnWidth, rowHeight);
           y += rowHeight;
           if (--thisRow == 0) {
             y = 0;
             x += columnWidth;
 
             if (--normalColumns <= 0) {
               thisRow = rows + 1;
               rowHeight = smallerHeight;
             }
             else {
               thisRow = rows;
             }
           }
         }
       }
     }
   }
 
   private void showException(Exception ex) {
     String msg = "Exception\n" + ex.getMessage();
     JOptionPane.showMessageDialog(p, msg, "Exception", 
                                   JOptionPane.ERROR_MESSAGE);
   }
   
   private JWP getParent() {
     return p;
   }
 }
 
