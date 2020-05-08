 /*******************************************************************************
  * Copyright (c) 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.jubula.rc.common.adapter;
 
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JRadioButton;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTree;
 import javax.swing.text.JTextComponent;
 
 import org.eclipse.jubula.rc.common.adaptable.IAdapterFactory;
 import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IComponent;
 import org.eclipse.jubula.rc.swing.tester.adapter.AbstractButtonAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JComboBoxAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JLabelAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JListAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JMenuBarAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JMenuItemAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JPopupMenuAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JTabbedPaneAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JTableAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JTextComponentAdapter;
 import org.eclipse.jubula.rc.swing.tester.adapter.JTreeAdapter;
 /**
  * This is the adapter factory for all swing components. It is
  * creating the specific adapter for a swing component.
  * 
  * Since we are using adapter here, it is a adapter factory. But this must not
  * be the case. It is only relevant that the object is implementing the
  * specific interface.
  * @author BREDEX GmbH
  *
  */
 public class SwingAdapterFactory implements IAdapterFactory {
     /**
      * the supported classes
      */
     private static final Class[] SUPPORTEDCLASSES = new Class[] {
         AbstractButton.class, JButton.class, JCheckBox.class,
         JRadioButton.class,
         JMenuBar.class, JMenuItem.class, JTree.class,
         JCheckBox.class, JRadioButton.class, JTable.class,
         JPopupMenu.class,
         JList.class, JTextComponent.class, JComboBox.class,
        JLabel.class, JTabbedPane.class };
    
     /** {@inheritDoc} */
     public Class[] getSupportedClasses() {
 
         return SUPPORTEDCLASSES;
     }
     /** {@inheritDoc} */
     public Object getAdapter(Class targetedClass, Object objectToAdapt) {
         if (targetedClass.isAssignableFrom(IComponent.class)) {
 
             IComponent returnvalue = null;
             if (objectToAdapt instanceof JButton) {
                 returnvalue = new AbstractButtonAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JRadioButton) {
                 returnvalue = new AbstractButtonAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JCheckBox) {
                 returnvalue = new AbstractButtonAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JMenuBar) {        
                 returnvalue = new JMenuBarAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JMenuItem) {
                 returnvalue = new JMenuItemAdapter(objectToAdapt);            
             } else if (objectToAdapt instanceof JTree) {
                 returnvalue = new JTreeAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JTable) {
                 returnvalue = new JTableAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JList) {
                 returnvalue = new JListAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JPopupMenu) {
                 returnvalue = new JPopupMenuAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JTextComponent) {
                 returnvalue = new JTextComponentAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JComboBox) {
                 returnvalue = new JComboBoxAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JLabel) {
                 returnvalue = new JLabelAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof JTabbedPane) {
                 returnvalue = new JTabbedPaneAdapter(objectToAdapt);
             } else if (objectToAdapt instanceof AbstractButton) { 
                 // pick up every button which is not specialized
                 returnvalue = new AbstractButtonAdapter(objectToAdapt);
             }
             
             return returnvalue;
         }
         return null;
     }
 
 }
