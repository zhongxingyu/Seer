 /*
  * $Id$
  *
  * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
  * Santa Clara, California 95054, U.S.A. All rights reserved.
  */
 
 package org.jdesktop.swingx;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractButton;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ButtonModel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 
 /**
  * @author Amy Fowler
  * @version 1.0
  */
 
 public class JXRadioGroup extends JPanel {
 	private static final long serialVersionUID = 3257285842266567986L;
 	private ButtonGroup buttonGroup;
     private List<Object> values = new ArrayList<Object>();
     private ActionListener actionHandler;
     private List<ActionListener> actionListeners;
 
     public JXRadioGroup() {
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         buttonGroup = new ButtonGroup();
     }
 
     public JXRadioGroup(Object radioValues[]) {
         this();
         for(int i = 0; i < radioValues.length; i++) {
             add(radioValues[i]);
         }
     }
 
     public void add(Object radioValue) {
         values.add(radioValue);
         addButton(new JRadioButton(radioValue.toString()));
     }
 
     public void add(AbstractButton button) {
         values.add(button.getText());
         addButton(button);
     }
 
     private void addButton(AbstractButton button) {
         buttonGroup.add(button);
         super.add(button);
         if (actionHandler == null) {
             actionHandler = new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     fireActionEvent(e);
                 }
             };
         }
         button.addActionListener(actionHandler);
     }
 
     public AbstractButton getSelectedButton() {
         ButtonModel selectedModel = buttonGroup.getSelection();
         Component children[] = getComponents();
         for(int i = 0; i < children.length; i++) {
             AbstractButton button = (AbstractButton)children[i];
             if (button.getModel() == selectedModel) {
                 return button;
             }
         }
         return null;
     }
 
     private int getSelectedIndex() {
         ButtonModel selectedModel = buttonGroup.getSelection();
         Component children[] = getComponents();
         for (int i = 0; i < children.length; i++) {
             AbstractButton button = (AbstractButton) children[i];
             if (button.getModel() == selectedModel) {
                 return i;
             }
         }
         return -1;
     }
 
     public Object getSelectedValue() {
         int index = getSelectedIndex();
        return (index <= 0 || index >= values.size()) ? null : values.get(index);
     }
 
     public void setSelectedValue(Object value) {
         int index = values.indexOf(value);
         AbstractButton button = (AbstractButton)getComponent(index);
         button.setSelected(true);
     }
 
     public void addActionListener(ActionListener l) {
         if (actionListeners == null) {
             actionListeners = new ArrayList<ActionListener>();
         }
         actionListeners.add(l);
     }
 
     public void removeActionListener(ActionListener l) {
         if (actionListeners != null) {
             actionListeners.remove(l);
         }
     }
 
     public ActionListener[] getActionListeners() {
         if (actionListeners != null) {
             return (ActionListener[])actionListeners.toArray(new ActionListener[0]);
         }
         return new ActionListener[0];
     }
 
     protected void fireActionEvent(ActionEvent e) {
         if (actionListeners != null) {
             for (int i = 0; i < actionListeners.size(); i++) {
                 ActionListener l = (ActionListener) actionListeners.get(i);
                 l.actionPerformed(e);
             }
         }
     }
 }
