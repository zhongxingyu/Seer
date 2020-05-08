 /*
  *                 Sun Public License Notice
  * 
  * The contents of this file are subject to the Sun Public License
  * Version 1.0 (the "License"). You may not use this file except in
  * compliance with the License. A copy of the License is available at
  * http://www.sun.com/
  * 
  * The Original Code is NetBeans. The Initial Developer of the Original
  * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
  * Microsystems, Inc. All Rights Reserved.
  */
 /*
  * LotsOfComponentsPanel.java
  *
  * Created on August 20, 2005, 12:33 PM
  */
 
 package org.netbeans.spi.wizard;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.Arrays;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.AbstractButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.colorchooser.DefaultColorSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 
 
 /**
  *
  * @author  tim
  */
 public class LotsOfComponentsPanel extends WizardPage {
     private final GenericListener gl;
     /** Creates new form LotsOfComponentsPanel */
     public LotsOfComponentsPanel() {
         super ("Page1", "Page 1", false);
         gl = new GenericListener(this);
         initComponents();
     }
     
     private Component getComponentByName(String name) {
         if (m == null) {
             init();
         }
         return (Component) m.get(name);
     }
     
     private String[] getComponentNames() {
         if (m == null) {
             init();
         }
         return names;
     }
     
     private Map m = null;
     private String[] names = null;
     private void init() {
         m = new HashMap();
         getComponentNames (this, m);
         names = new String[m.size()];
         names = (String[]) m.keySet().toArray(names);
     }
     
     private void getComponentNames(Container con, Map m) {
         Component[] c = con.getComponents();
         for (int i=0; i < c.length; i++) {
             if (c[i].getName() != null) {
                if (m.containsKey(c[i].getName())) {
                     throw new IllegalStateException ("Two components named " + c[i].getName());
                 }
                 m.put (c[i].getName(), c[i]);
             }
             if (c[i] instanceof Container) {
                 getComponentNames((Container) c[i], m);
             }
         }
     }
     
     public void tickleAll() {
         String[] names = getComponentNames();
         System.err.println("COMPONENT NAMES: " + Arrays.asList(names));
         for (int i=0; i < names.length; i++) {
             tickle (getComponentByName(names[i]));
         }
     }
     
     private void tickle (final Component c) {
         System.err.println("Tickle " + c.getClass());
         try {
             Thread.currentThread().sleep (500); //XXX for testing
         } catch (Exception exc) {}
         final boolean[] done = new boolean[] { false };
         final RuntimeException[] e = new RuntimeException[] { null };
         c.requestFocus();
         Runnable r = new Runnable() {
             public void run() {
                 try {
                     doTickle (c);
                 } catch (RuntimeException re) {
                     e[0] = re;
                 } finally {
                     done[0] = true;
                 }
             }
         };
         if (!SwingUtilities.isEventDispatchThread()) {
             SwingUtilities.invokeLater (r);
             while (!done[0]) {
                 try {
                     Thread.currentThread().sleep(100);
                 } catch (Exception ex) {
                     
                 }
             }
         } else {
             r.run();
         }
         if (e[0] != null) {
             throw e[0];
         } else {
             assertValueChanged(c);
         }
     }
     
     private void doTickle (Component c) {
         assert c != null;
         assert c.getName() != null;
         System.err.println("NOW TICKLING " + c.getName());
         if (c instanceof JTextComponent) {
             KeyEvent[] ke = createKeyEvents (c);
             for (int i=0; i < ke.length; i++) {
                 c.dispatchEvent(ke[i]);
             }
         } else if (c instanceof AbstractButton) {
             ((AbstractButton) c).doClick();
         } else if (c instanceof JComboBox) {
             KeyEvent[] ke = createArrowKeyEvents(c);
             for (int i=0; i < ke.length; i++) {
                 c.dispatchEvent(ke[i]);
             }
         } else if (c instanceof JTable) {
             KeyEvent[] ke = createArrowKeyEvents(c);
             for (int i=0; i < ke.length; i++) {
                 c.dispatchEvent(ke[i]);
             }
             JTable jt = (JTable) c;
             ke = createSpaceKeyEvents(c);
             for (int i=0; i < ke.length; i++) {
                 c.dispatchEvent(ke[i]);
             }
             Component comp = jt.getEditorComponent();
             if (comp == null || !comp.isShowing()) {
                 throw new Error ("Couldn't actually find the table cell editor component");
             }
             ke = createKeyEvents(comp);
             for (int i=0; i < ke.length; i++) {
                 comp.dispatchEvent(ke[i]);
             }
             ke = createEnterKeyEvents(comp);
             for (int i=0; i < ke.length; i++) {
                 comp.dispatchEvent(ke[i]);
             }
         } else if (c instanceof JSpinner) {
             JSpinner spin = (JSpinner) c;
             JComponent comp = spin.getEditor();
             KeyEvent[] ke = createNumberKeyEvents(comp);
             for (int i=0; i < ke.length; i++) {
                 comp.dispatchEvent(ke[i]);
             }            
             ke = createEnterKeyEvents(comp);
             for (int i=0; i < ke.length; i++) {
                 comp.dispatchEvent(ke[i]);
             }
         } else if (c instanceof JSlider) {
             JSlider slid = (JSlider) c;
             slid.setValue(slid.getValue() + 5);
             //Ugh, no other way to really do this
             ChangeListener[] l = slid.getChangeListeners();
             for (int i=0; i < l.length; i++) {
                 l[i].stateChanged(new ChangeEvent(c));
             }
         } else if (c instanceof JList) {
             JList jl = (JList) c;
             KeyEvent[] ke = createArrowKeyEvents(c);
             for (int i=0; i < ke.length; i++) {
                 c.dispatchEvent(ke[i]);
             }
         } else if (c instanceof JColorChooser) {
             JColorChooser ch = (JColorChooser) c;
             //Hmm, will this work
             ch.getSelectionModel().setSelectedColor(Color.BLUE);
             
         } else if (c instanceof JTree) {
             JTree jt = (JTree) c;
             MouseEvent[] me = createMouseEvents (c, 20, 60);
             for (int i=0; i < me.length; i++) {
                 c.dispatchEvent (me[i]);
             }
         }
     }
     
     Object evt = null;
     Component comp = null;
     protected String validateContents(Component component, Object event) {
         System.out.println("Got event " + event);
         evt = event;
         comp = component;
         return null;
     }
     
     private void assertValueChanged(Component c) {
         Object e = evt;
         Component comp = this.comp;
         this.comp = null;
         evt = null;
         if (comp == null && evt == null) {
 //            throw new Error ("ValidateContents was not called after an event on " + c.getClass());
             System.err.println("No event, no component");
         }
         if (comp != c && comp != null) {
 //            throw new Error ("ValidateContents was called for " + comp + " not " + c);
             System.err.println("ValidateContents called for wrong component " + comp + " expected " + c);
         }
 //        if (e instanceof EventObject) {
 //            if (((EventObject) e).getSource() != c) {
 //                if (!(((EventObject)e).getSource() instanceof DefaultColorSelectionModel)) {
 //                    //JColorChooser is not the sharpest tool in the shed...
 //                    throw new Error ("Event source for changed component not " + c.getClass() + " but " + ((EventObject) e).getSource().getClass());
 //                }
 //            }
 //        } else if (e instanceof DocumentEvent && c instanceof JTextComponent) {
 //            Document d = ((DocumentEvent) e).getDocument();
 //            Document d1 = ((JTextComponent) c).getDocument();
 //            if (d != d1) {
 //                throw new Error ("Change received from wrong document");
 //            }
 //        } else if (e == null) {
 ////            throw new Error ("No event was fired for " + c);
 //            System.err.println("NO EVENT FOR " + c.getClass());
 //        }
     }
     
     private MouseEvent[] createMouseEvents (Component c, int x, int y) {
         MouseEvent[] result = new MouseEvent[3];
         result[0] = new MouseEvent (c, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, x, y, 2, false, MouseEvent.BUTTON1);
         result[1] = new MouseEvent (c, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, x, y, 2, false, MouseEvent.BUTTON1);
         result[2] = new MouseEvent (c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, x, y, 2, false, MouseEvent.BUTTON1);
         return result;
     }
 //    
 //    public MouseEvent(Component source, int id, long when, int modifiers,
 //                      int x, int y, int clickCount, boolean popupTrigger,
 //                      int button)     
     
     private KeyEvent[] createKeyEvents(Component c) {
         KeyEvent[] result = new KeyEvent[6];
         result[0] = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_H, 'H', KeyEvent.KEY_LOCATION_STANDARD);
         result[1] = new KeyEvent(c, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_H, 'H', KeyEvent.KEY_LOCATION_STANDARD);
         result[2] = new KeyEvent(c, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'H', KeyEvent.KEY_LOCATION_UNKNOWN);
         
         result[3] = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_I, 'I', KeyEvent.KEY_LOCATION_STANDARD);
         result[4] = new KeyEvent(c, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_I, 'I', KeyEvent.KEY_LOCATION_STANDARD);
         result[5] = new KeyEvent(c, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'I', KeyEvent.KEY_LOCATION_UNKNOWN);
         return result;
     }
     
     private KeyEvent[] createArrowKeyEvents(Component c) {
         KeyEvent[] result = new KeyEvent[2];
         result[0] = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_STANDARD);
         result[1] = new KeyEvent(c, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_STANDARD);
         return result;
     }
     
     private KeyEvent[] createSpaceKeyEvents(Component c) {
         KeyEvent[] result = new KeyEvent[3];
         result[0] = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ', KeyEvent.KEY_LOCATION_STANDARD);
         result[1] = new KeyEvent(c, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ', KeyEvent.KEY_LOCATION_STANDARD);
         result[2] = new KeyEvent(c, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, ' ', KeyEvent.KEY_LOCATION_UNKNOWN);
         return result;
     }
     
     private KeyEvent[] createEnterKeyEvents(Component c) {
         KeyEvent[] result = new KeyEvent[3];
         result[0] = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n', KeyEvent.KEY_LOCATION_STANDARD);
         result[1] = new KeyEvent(c, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n', KeyEvent.KEY_LOCATION_STANDARD);
         result[2] = new KeyEvent(c, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '\n', KeyEvent.KEY_LOCATION_UNKNOWN);
         return result;
     }
     
     private KeyEvent[] createNumberKeyEvents(Component c) {
         KeyEvent[] result = new KeyEvent[3];
         result[0] = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_5, '5', KeyEvent.KEY_LOCATION_STANDARD);
         result[1] = new KeyEvent(c, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_5, '5', KeyEvent.KEY_LOCATION_STANDARD);
         result[2] = new KeyEvent(c, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, '5', KeyEvent.KEY_LOCATION_UNKNOWN);
         return result;
     }    
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         jToggleButton1 = new javax.swing.JToggleButton();
         jComboBox1 = new javax.swing.JComboBox();
         jPanel1 = new javax.swing.JPanel();
         jRadioButton1 = new javax.swing.JRadioButton();
         jCheckBox1 = new javax.swing.JCheckBox();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         jTextField1 = new javax.swing.JTextField();
         jScrollPane2 = new javax.swing.JScrollPane();
         jList1 = new javax.swing.JList();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTree1 = new javax.swing.JTree();
         jColorChooser1 = new javax.swing.JColorChooser();
         jSpinner1 = new javax.swing.JSpinner();
         jSlider1 = new javax.swing.JSlider();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
 
         setLayout(null);
 
         jToggleButton1.setText("jToggleButton1");
         jToggleButton1.setName("toggleButton");
         add(jToggleButton1);
         jToggleButton1.setBounds(10, 10, 124, 29);
 
         jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         jComboBox1.setSelectedItem("Hello there");
         jComboBox1.setName("\"combo\"");
         add(jComboBox1);
         jComboBox1.setBounds(140, 10, 240, 27);
 
         jPanel1.setLayout(null);
 
         jPanel1.setBackground(new java.awt.Color(255, 200, 200));
         jRadioButton1.setText("jRadioButton1");
         jRadioButton1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
         jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
         jRadioButton1.setName("radio");
         jPanel1.add(jRadioButton1);
         jRadioButton1.setBounds(10, 30, 140, 30);
 
         jCheckBox1.setText("jCheckBox1");
         jCheckBox1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
         jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
         jCheckBox1.setName("checkbox");
         jPanel1.add(jCheckBox1);
         jCheckBox1.setBounds(160, 30, 140, 30);
 
         add(jPanel1);
         jPanel1.setBounds(30, 60, 310, 90);
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jTextArea1.setName("textarea");
         jScrollPane1.setViewportView(jTextArea1);
 
         add(jScrollPane1);
         jScrollPane1.setBounds(20, 160, 220, 60);
 
         jTextField1.setText("jTextField1");
         add(jTextField1);
         jTextField1.setBounds(20, 240, 78, 22);
 
         jList1.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jList1.setName("list");
         jScrollPane2.setViewportView(jList1);
 
         add(jScrollPane2);
         jScrollPane2.setBounds(250, 160, 140, 100);
 
         jScrollPane3.setBackground(new java.awt.Color(200, 200, 255));
         jTree1.setName("tree");
         jScrollPane3.setViewportView(jTree1);
 
         add(jScrollPane3);
         jScrollPane3.setBounds(120, 270, 190, 140);
 
         jColorChooser1.setName("colors");
         add(jColorChooser1);
         jColorChooser1.setBounds(400, 100, 270, 240);
 
         jSpinner1.setName("spinner");
         add(jSpinner1);
         jSpinner1.setBounds(400, 40, 120, 24);
 
         jSlider1.setName("slider");
         add(jSlider1);
         jSlider1.setBounds(400, 360, 190, 29);
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {"A"},
                 {"B"},
                 {"C"},
                 {"D"}
             },
             new String [] {
                 "Title 1"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         jTable1.setName("table");
         jScrollPane4.setViewportView(jTable1);
 
         add(jScrollPane4);
         jScrollPane4.setBounds(50, 440, 454, 90);
 
     }
     // </editor-fold>//GEN-END:initComponents
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JColorChooser jColorChooser1;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JList jList1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JRadioButton jRadioButton1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JSlider jSlider1;
     private javax.swing.JSpinner jSpinner1;
     private javax.swing.JTable jTable1;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JToggleButton jToggleButton1;
     private javax.swing.JTree jTree1;
     // End of variables declaration//GEN-END:variables
     
 }
