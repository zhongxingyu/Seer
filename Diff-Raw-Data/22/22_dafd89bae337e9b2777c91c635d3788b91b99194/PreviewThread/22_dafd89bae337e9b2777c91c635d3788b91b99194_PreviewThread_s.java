 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 package org.netbeans.modules.javafx.preview;
 
 import java.awt.BorderLayout;
 import java.awt.Window;
 import java.io.File;
 import java.lang.reflect.Method;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.text.StyledDocument;
 import javax.tools.Diagnostic.Kind;
 import org.netbeans.modules.javafx.editor.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import javax.swing.JComponent;
 
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.Document;
 import javax.swing.text.html.HTMLEditorKit;
 import org.openide.util.Exceptions;
 import javax.tools.Diagnostic;
 import javax.tools.JavaFileObject;
 import org.netbeans.modules.editor.NbEditorUtilities;
 import org.openide.cookies.EditorCookie;
 import org.openide.cookies.LineCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 import org.openide.text.Line;
 import org.openide.text.NbDocument;
         
 public class PreviewThread extends Thread {
     
     JPanel contentPanel = null;
     
     private class Hyperlink implements HyperlinkListener {
         private Vector<Object> foMap = new Vector<Object>();
         private Vector<Long> offsetMap = new Vector<Long>();
         
         void setMaps(Vector<Object> foMap, Vector<Long> offsetMap) {
             this.foMap = foMap;
             this.offsetMap = offsetMap;
         }
     
         private void goTo(final Document doc, final int offset) {
             LineCookie lc = NbEditorUtilities.getDataObject(doc).getCookie(LineCookie.class);
             int line = NbDocument.findLineNumber((StyledDocument) doc, offset);
             int lineOffset = NbDocument.findLineOffset((StyledDocument) doc,line);
             int column = offset - lineOffset;
 
             if (line != -1) {
                 Line l = lc.getLineSet().getCurrent(line);
 
                 if (l != null) {
                     l.show(Line.SHOW_TOFRONT, column);
                     ((JavaFXDocument)doc).getEditor().requestFocusInWindow();
                 }
             }
         }
         public void hyperlinkUpdate(HyperlinkEvent e) {
             if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
                 return;
             String href = e.getDescription();
             Object fo = foMap.elementAt(Integer.parseInt(href));
             Document doc = null;
             if (fo instanceof MemoryFileObject) {
                 doc = ((MemoryFileObject)fo).getDocument();
             } else {
                 try {
                     DataObject od = DataObject.find((FileObject)fo);
                     EditorCookie ec = od.getCookie(EditorCookie.class);
                     doc = ec.openDocument();                
                 } catch (Exception ex) {
                     return;
                 }
             }
             int offset = offsetMap.elementAt(Integer.parseInt(href)).intValue();
             goTo(doc, offset);
         }
     };
     
         
     private FXDocument doc;
     private Object context;
    
     public List <Diagnostic> compile() {
         try {
             context = CodeManager.compile(doc);
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
         return CodeManager.getDiagnostics();
     }
    
     public JComponent execute() {
         JComponent comp = null;
         List <Window> initialList = getOwnerlessWindowsList();
 
         if (!checkJavaVersion()) {
             comp = JavaFXDocument.getVrongVersion();
             return comp;
         }
        Object obj = null;
         try {
            obj = CodeManager.run(context);
         } catch (Exception ex) {
             Exceptions.printStackTrace(ex);
         }
 
         List <Window> suspectedList = getOwnerlessWindowsList();
         suspectedList.removeAll(initialList);
 
         if (obj != null) {
             comp = CodeManager.parseObj(obj);
         } else {
             if (!suspectedList.isEmpty()) {
                 comp = CodeManager.parseObj(suspectedList.get(0));
                 suspectedList.remove(0);
             } else
                 comp = null;
         }
         for (Window frame : suspectedList) {
             frame.dispose();
         }
         if (comp == null) {
             comp = JavaFXDocument.getNothingPane();
         }
 
         JPanel pane = new JPanel();
         pane.setLayout(new BorderLayout());
         pane.add(comp);
         comp = (JComponent) pane;
         
         return comp;
     }
 
     private List <Window> getOwnerlessWindowsList() {
         List <Window> list = new ArrayList<Window>();
 
         Method getOwnerlessWindows = null;
         Window windows[] = null;
         try {
             // to compille under JDK 1.5
             //windows = Window.getOwnerlessWindows();
             getOwnerlessWindows = Window.class.getDeclaredMethod("getOwnerlessWindows");
             windows = (Window[])getOwnerlessWindows.invoke(null);
         } catch (Exception ex) {
         }
         if (windows != null)
             for (Window window : windows) {
                 if (window instanceof JFrame)
                     list.add((JFrame)window);
             }
         return list;
     }
 
     private boolean checkJavaVersion() {
         String version = System.getProperty("java.runtime.version");
         if (!version.startsWith("1.6"))
             return false;
         else
             return true;
     }
 
     private JComponent processDiagnostic(List <Diagnostic> diagnostics) {
         JEditorPane pane = new JEditorPane();
         pane.setEditable(false);
         pane.setEditorKit(new HTMLEditorKit());
         Hyperlink hl = new Hyperlink();
         pane.addHyperlinkListener(hl);
         //pane.setFont(new FontUIResource("Monospaced", FontUIResource.PLAIN, 20));
         String text = "";
         int i = 0;
         Vector<Object> foMap = new Vector<Object>();
         Vector<Long> offsetMap = new Vector<Long>();
         for (Diagnostic diagnostic : diagnostics) {
             Object source = diagnostic.getSource();
             String name = "";
             if (diagnostic.getSource() != null)
             {
                 if (diagnostic.getSource() instanceof MemoryFileObject) {
                     MemoryFileObject mfo = (MemoryFileObject)source;
                     name = mfo.getFilePath();
                 } else {
                     JavaFileObject jFO = (JavaFileObject) source;
                     File file = new File(jFO.toUri());
                     FileObject regularFO = FileUtil.toFileObject(file);
                     name = regularFO.getPath();
                     source = regularFO;
                 }
                 foMap.add(source);
                 offsetMap.add(diagnostic.getPosition());
                 if (diagnostic.getKind() == Kind.WARNING) {
                     text+= "<a href=" + i + ">" + name + " : " + diagnostic.getLineNumber() + "</a>\n" + " " + "<font color=#540000>: warning: " + diagnostic.getMessage(null) + "</font>" + "<br>";
                 }else{
                     text+= "<a href=" + i + ">" + name + " : " + diagnostic.getLineNumber() + "</a>\n" + " " + "<font color=#a40000>" + diagnostic.getMessage(null) + "</font>" + "<br>";
                 }
                 i++;
             }
         }
         pane.setText(text);
         hl.setMaps(foMap, offsetMap);
         return pane;
     }
 
     public PreviewThread(FXDocument doc) {
         super();
         this.doc = doc;
     }
 
     @Override
     synchronized public void run() {
         ((JavaFXDocument)doc).setCompile();
         final List <Diagnostic> diagnostics = compile();
         if (!diagnostics.isEmpty()) {
             ((JavaFXDocument)doc).renderPreview(processDiagnostic(diagnostics));
         } else {
             MirroringPanel mirroringPanel = null;
             try {
                 mirroringPanel = new MirroringPanel(JavaFXModel.getProject(doc), UIManager.getLookAndFeel()) {
                     @Override
                     protected JPanel createMirroredPanel() {
                         return (JPanel)execute();
                     }
                 };
             } catch (Exception ex) {
                 ((JavaFXDocument)doc).renderPreview(JavaFXDocument.getErrorPane());
             }
             if (mirroringPanel!= null)
                 ((JavaFXDocument)doc).renderPreview(mirroringPanel);
         }
     }
 }
