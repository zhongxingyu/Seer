 /*
  * ItemPanel.java
  *
  * Created on October 18, 2010, 1:03 PM
  * @author jaycverg
  */
 
 package com.rameses.rcp.util;
 
 import com.rameses.rcp.constant.UIConstants;
 import com.rameses.rcp.control.XLabel;
 import com.rameses.rcp.ui.ActiveControl;
 import com.rameses.rcp.ui.ControlProperty;
 import com.rameses.util.ValueUtil;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.BorderFactory;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 
 class ItemPanel extends JPanel {
     
     /**
      * wrapper is usually JScrollPane
      */
     private Component editorWrapper;
     private Component editor;
     private XLabel label;
     private ControlProperty property;
     private FormPanel formPanel;
     
     
     ItemPanel(FormPanel parent, Component editor) {
         JScrollPane container = null;
         if ( editor instanceof JTextArea || editor instanceof JEditorPane ) {
             container = new JScrollPane();
             container.setViewportView(editor);
         }
         initComponents(parent, editor, container);
     }
     
     ItemPanel(FormPanel parent, Component editor, Component container) {
         initComponents(parent, editor, container);
     }
     
     //<editor-fold defaultstate="collapsed" desc="  initComponents  ">
     private void initComponents(FormPanel parent, Component editor, Component container) {
         this.formPanel = parent;
         this.editor = editor;
         this.editorWrapper = container;
         
         ActiveControl con = (ActiveControl) editor;
         property = con.getControlProperty();
         
         setOpaque(false);
         setLayout(new ItemPanelLayout(property));
         
        label = new XLabel(true);
         label.setLabelFor(editor);
         label.setAddCaptionColon(parent.isAddCaptionColon());
         label.setFont(parent.getCaptionFont());
         label.setForeground(parent.getCaptionForeground());
         
         if ( !ValueUtil.isEmpty(label.getText()) )
             label.setBorder(parent.getCaptionBorder());
         
         add(label, "label");
         
         if ( container != null ) {
             add(container, "editor");
         } else {
             add(editor, "editor");
         }
         
         PropertyChangeListener listener = new ContainablePropetyListener(this);
         property.addPropertyChangeListener(listener);
     }
     //</editor-fold>
     
     public boolean match(Component editor) {
         if (editor == null) return false;
         
         return (this.editor == editor);
     }
     
     public Component getEditorComponent() { return editor; }
     public Component getEditorWrapper() { return editorWrapper; }
     public XLabel getLabelComponent() { return label; }
     public ControlProperty getControlProperty() { return property; }
     
     
     //<editor-fold defaultstate="collapsed" desc=" ContainablePropetyListener (Class) ">
     private class ContainablePropetyListener implements PropertyChangeListener {
         
         private ItemPanel panel;
         
         ContainablePropetyListener(ItemPanel panel) {
             this.panel = panel;
         }
         
         public void propertyChange(PropertyChangeEvent evt) {
             String propName = evt.getPropertyName();
             Object value = evt.getNewValue();
             
             if ( "captionWidth".equals(propName) ) {
                 panel.revalidate();
             } else if ( "showCaption".equals(propName)) {
                 panel.revalidate();
             } else if ( "caption".equals(propName) ) {
                 Border b = panel.getLabelComponent().getBorder();
                 if ( ValueUtil.isEmpty(value) && !(b instanceof EmptyBorder)) {
                     panel.getLabelComponent().setBorder(BorderFactory.createEmptyBorder());
                 } else if ( !ValueUtil.isEqual(b, formPanel.getCaptionBorder()) ) {
                     panel.getLabelComponent().setBorder(formPanel.getCaptionBorder());
                 }
             }
         }
         
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc=" ItemPanelLayout (Class) ">
     private class ItemPanelLayout implements LayoutManager {
         
         private Component label;
         private Component editor;
         private ControlProperty property;
         
         ItemPanelLayout(ControlProperty property) {
             this.property = property;
         }
         
         public void addLayoutComponent(String name, Component comp) {
             if ("label".equals(name)) label = comp;
             else if ("editor".equals(name)) editor = comp;
         }
         
         public void removeLayoutComponent(Component comp) {
             if (comp == null) ;
             else if (label == comp) label = comp;
             else if (editor == comp) editor = comp;
         }
         
         public Dimension preferredLayoutSize(Container parent) {
             return getLayoutSize(parent);
         }
         
         public Dimension minimumLayoutSize(Container parent) {
             Dimension dim = getLayoutSize(parent);
             return new Dimension(100, dim.height);
         }
         
         public void layoutContainer(Container parent) {
             synchronized (parent.getTreeLock()) {
                 Insets margin = parent.getInsets();
                 int x = margin.left;
                 int y = margin.top;
                 int w = parent.getWidth() - (margin.left + margin.right);
                 int h = parent.getHeight() - (margin.top + margin.bottom);
                 int captionWidth = getPreferredCaptionWidth();
                 String orient = formPanel.getCaptionOrientation();
                 
                 if ( UIConstants.LEFT.equals(orient) || UIConstants.TOP.equals(orient) ) {
                     Rectangle rec = layoutLabel(x, y, w, h, captionWidth);
                     layoutEditor(rec.x, rec.y, rec.width, rec.height);
                 } else {
                     Rectangle rec = layoutEditor(x, y, w, h);
                     layoutLabel(rec.x, rec.y, rec.width, rec.height, captionWidth);
                 }
             }
         }
         
         //<editor-fold defaultstate="collapsed" desc="  layout helper  ">
         private Rectangle layoutLabel(int x, int y, int w, int h, int captionWidth) {
             if (label != null && isShowCaption()) {
                 int cw = captionWidth;
                 Dimension dim = label.getPreferredSize();
                 String captionOrientation = formPanel.getCaptionOrientation();
                 
                 if ( (UIConstants.TOP.equals(captionOrientation) || UIConstants.BOTTOM.equals(captionOrientation)) && editor != null )
                     cw = editor.getPreferredSize().width;
                 else if (cw <= 0)
                     cw = dim.width;
                 
                label.setBounds(x, y, cw, h);
                 
                 if ( UIConstants.TOP.equals(captionOrientation) ) {
                     y += dim.height;
                     h -= dim.height;
                 } else {
                     x += cw;
                     w -= cw;
                 }
             }
             return new Rectangle(x, y, w, h);
         }
         
         private Rectangle layoutEditor(int x, int y, int w, int h) {
             if (editor != null) {
                 Dimension dim = editor.getPreferredSize();
                 String captionOrientation = formPanel.getCaptionOrientation();
                 
                 int cw = dim.width;
                 if ( cw > w || cw <= 0 ) {
                     cw = w;
                 }
                 editor.setBounds(x, y, cw, dim.height);
                 if ( UIConstants.BOTTOM.equals(captionOrientation) ) {
                     y += dim.height;
                     h -= dim.height;
                 } else {
                     x += cw;
                     w -= cw;
                 }
             }
             return new Rectangle(x, y, w, h);
         }
         //</editor-fold>
         
         public Dimension getLayoutSize(Container parent) {
             synchronized (parent.getTreeLock()) {
                 int w=0, h=0;
                 int captionWidth = getPreferredCaptionWidth();
                 String orient = formPanel.getCaptionOrientation();
                 
                 if (label != null) {
                     if ( isShowCaption() ) {
                         applyCaptionStyles((JLabel) label);
                         Dimension dim = label.getPreferredSize();
                         
                         if ( UIConstants.TOP.equals(orient) || UIConstants.BOTTOM.equals(orient) ) {
                             h += dim.height;
                             w = Math.max(w, dim.width);
                         } else {
                             int cw = captionWidth;
                             if (cw <= 0) cw = dim.width;
                             w += cw;
                             h = Math.max(h, dim.height);
                         }
                         
                         label.setVisible(true);
                     } else {
                         label.setVisible(false);
                     }
                 }
                 
                 if (editor != null) {
                     Dimension dim = editor.getPreferredSize();
                     if ( UIConstants.TOP.equals(orient) || UIConstants.BOTTOM.equals(orient) ) {
                         h += dim.height;
                         w = Math.max(w, dim.width);
                     } else {
                         w += dim.width;
                         h = Math.max(h, dim.height);
                     }
                 }
                 
                 Insets margin = parent.getInsets();
                 w += (margin.left + margin.right);
                 h += (margin.top + margin.bottom);
                 return new Dimension(w, h);
             }
         }
         
         private void applyCaptionStyles(JLabel label) {
             //vertical alignment
             String valign = formPanel.getCaptionVAlignment();
             if ( UIConstants.CENTER.equals(valign) )
                 label.setVerticalAlignment(SwingConstants.CENTER);
             else if ( UIConstants.BOTTOM.equals(valign) )
                 label.setVerticalAlignment(SwingConstants.BOTTOM);
             else
                 label.setVerticalAlignment(SwingConstants.TOP);
             
             //horizontal alignment
             String halign = formPanel.getCaptionHAlignment();
             if ( UIConstants.CENTER.equals(halign) )
                 label.setHorizontalAlignment(SwingConstants.CENTER);
             else if ( UIConstants.RIGHT.equals(halign) )
                 label.setHorizontalAlignment(SwingConstants.RIGHT);
             else
                 label.setHorizontalAlignment(SwingConstants.LEFT);
             
             Insets captionPadding = formPanel.getCaptionPadding();
             if ( captionPadding != null )
                 ((XLabel) label).setPadding(captionPadding);
         }
         
         private int getPreferredCaptionWidth() {
             return property.getCaptionWidth() <= 0? formPanel.getCaptionWidth(): property.getCaptionWidth();
         }
         
         private boolean isShowCaption() {
             return property.isShowCaption();
         }
     }
     //</editor-fold>
     
 }
