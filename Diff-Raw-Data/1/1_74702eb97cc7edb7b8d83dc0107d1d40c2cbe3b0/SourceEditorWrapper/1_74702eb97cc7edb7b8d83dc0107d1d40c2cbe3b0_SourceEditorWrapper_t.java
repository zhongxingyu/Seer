 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package org.netbeans.modules.javafx.fxd.composer.source;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.util.logging.Logger;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import org.netbeans.editor.EditorUI;
 import org.netbeans.editor.StatusBar;
 import org.netbeans.editor.Utilities;
 import org.openide.text.CloneableEditorSupport;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.AbstractLookup;
 import org.openide.util.lookup.InstanceContent;
 import org.openide.windows.CloneableTopComponent;
 
 /**
  *
  * @author Pavel Benes
  */
 public class SourceEditorWrapper extends JPanel implements CloneableEditorSupport.Pane {
     public static final String CELL_ERROR = "error"; // NOI18N
     
     private SourceTopComponent m_delegate = null;
 
     private final static class EditorLookupWrapper extends JPanel implements Lookup.Provider {
         private final SourceTopComponent m_stc;
         private final JComponent         m_editor;
         private final Lookup             m_lookup;
         
         public EditorLookupWrapper( SourceTopComponent stc) {
             assert stc != null;
             m_stc = stc;
             //force the creation of the editor pane
            m_stc.componentShowing();
             JEditorPane pane = m_stc.getEditorPane();
             if (pane != null) {
                 addErrorStatusBarCell(pane);
                 Container c = pane;
                 Container parent;
                 while ((parent = c.getParent()) != m_stc) {
                     c = parent;
                 }
                 m_editor = (JComponent) c;
             } else {
                 Logger.getLogger(SourceEditorWrapper.class.getName()).warning(
                         "UNEXPECTED null returned by CloneableEditor.getEditorPane()");
                 JEditorPane p = new JEditorPane();
                 p.setEnabled(false);
                 m_editor = (JComponent) p;
             }
 
             m_stc.remove(m_editor);
             assert m_editor != null;
             setLayout(null);
             setBorder(null);
             add(m_editor);
             
             InstanceContent ic = new InstanceContent ();
             m_lookup = new AbstractLookup (ic);
             ic.add ( stc.getEditorSupport());
         }
         
         @Override
         public Dimension getPreferredSize() {
             return m_editor.getPreferredSize();
         }
 
         protected void showContent() {
             setLocation(0, 0);
             setVisible(true);
             findEditorPane(m_editor).requestFocusInWindow();
         }
 
         protected void hideContent() {
             setVisible(false);
             setSize(0, 0);
         }
         
         public Lookup getLookup() {
             return m_lookup;
         }
         
         @Override
         public void setSize( int w, int h) {
             m_editor.setSize(w, h);
             super.setSize( w, h);
         }  
         
         private static JEditorPane findEditorPane(Component component) {
             JEditorPane result = null;
             
             if ( component instanceof JEditorPane) {
                 result = (JEditorPane) component;
             } else if ( component instanceof Container) {
                 for ( Component c : ((Container)component).getComponents()) {
                     if ( (result=findEditorPane(c)) != null) {
                         break;
                     }
                 }
             } 
             return result;
         }
     }
     
     public SourceEditorWrapper() {
         setLayout( null);
     }
     
     private EditorLookupWrapper findWrapper( SourceTopComponent tc, boolean hideOthers) {
         Component [] comps = getComponents();
         
         EditorLookupWrapper wrapper = null;
         for ( Component c : comps) {
             EditorLookupWrapper w = (EditorLookupWrapper) c;
             if ( w.m_stc == tc) {
                 wrapper = w;
             } else if ( hideOthers) {
                 w.hideContent();
             }
         }
         return wrapper;
     }
     
     public void wrap( SourceTopComponent tc) {
         m_delegate = tc;
         EditorLookupWrapper wrapper = findWrapper(tc, true);        
         if ( wrapper == null) {
             add( wrapper = new EditorLookupWrapper(tc));
         }
         tc.componentShowing();
         wrapper.showContent();
         wrapper.invalidate();
         validate();
         repaint();
     }
 
     @Override
     public Dimension getPreferredSize() {
         return m_delegate == null ? null : m_delegate.getPreferredSize();
     }
     
     public JEditorPane getEditorPane() {
         return m_delegate.getEditorPane();
     }
 
     public CloneableTopComponent getComponent() {
         return m_delegate.getComponent();
     }
 
     public void updateName() {
         m_delegate.updateName();
     }
 
     public void ensureVisible() {
         setVisible(true);
         m_delegate.ensureVisible();
     }
     
     @Override
     public void setPreferredSize( Dimension dim) {
         super.setPreferredSize(dim);
     }
     
     @Override
     public void setSize( Dimension dim) {
         // an attempt to fix http://javafx-jira.kenai.com/browse/RT-4600
         EditorLookupWrapper elw = findWrapper(m_delegate, false);
         if ( elw != null) {
             elw.setSize(dim.width, dim.height);
         }
         super.setSize(dim);
     }
 
     @Override
     public void setBounds( Rectangle r) {
         // an attempt to fix http://javafx-jira.kenai.com/browse/RT-4600
         EditorLookupWrapper elw = findWrapper(m_delegate, false);
         if ( elw != null) {
             elw.setSize(r.width, r.height);
         }
         super.setBounds(r);
     }
 
     @Override
     public void setBounds( int x, int y, int w, int h) {
         // an attempt to fix http://javafx-jira.kenai.com/browse/RT-4600
         EditorLookupWrapper elw = findWrapper(m_delegate, false);
         if ( elw != null) {
             elw.setSize(w, h);
         }
         super.setBounds(x, y, w, h);
     }    
     
     public static void addErrorStatusBarCell( JEditorPane pane) {
         EditorUI eui = Utilities.getEditorUI(pane);
         StatusBar sBar = eui == null ? null : eui.getStatusBar();
         if (sBar != null && sBar.getCellByName(CELL_ERROR) == null) {
             StringBuilder sb = new StringBuilder();
             for (int i = 0; i < 60; i++) {
                 sb.append( 'A');
             }
             sBar.addCell( CELL_ERROR, new String [] { sb.toString()});
         }
     }    
 }
