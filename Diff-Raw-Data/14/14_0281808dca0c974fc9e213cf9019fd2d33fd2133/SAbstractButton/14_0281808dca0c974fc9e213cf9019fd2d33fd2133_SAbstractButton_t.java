 /*
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
 package org.wings;
 
 import org.wings.plaf.ButtonCG;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 /**
  * Base class for components with button functionality, ie. the need to handle ActionListener notification.
  *
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  */
 public abstract class SAbstractButton
         extends SAbstractIconTextCompound
         implements LowLevelEventListener {
     public static final String SUBMIT_BUTTON = "submit";
     public static final String RESET_BUTTON = "reset";
     public static final String IMAGE_BUTTON = "image";
     public static final String CHECKBOX = "checkbox";
     public static final String RADIOBUTTON = "radio";
 
     private String type = SUBMIT_BUTTON;
 
     private SButtonGroup buttonGroup;
 
     protected String actionCommand;
 
     private String eventTarget;
 
     private Action action;
 
     private PropertyChangeListener actionPropertyChangeListener;
 
     private String mnemonic;
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     private boolean epochCheckEnabled = true;
 
     private boolean wordWrap = false;
 
     /**
      * Create a button with given text.
      *
      * @param text the button text
      */
     public SAbstractButton(String text) {
         super(text);
     }
 
     public SAbstractButton(Action action) {
         setAction(action);
     }
 
     /**
      * Creates a new Button with the given Text and the given Type.
      *
      * @param text the button text
      * @param type the button type
      * @see #setType
      */
     public SAbstractButton(String text, String type) {
         this(text);
         setType(type);
     }
 
     /**
      * Creates a new submit button
      */
     public SAbstractButton() {
     }
 
     /**
      * Sets the action command for this button.
      *
      * @param ac the action command for this button
      */
     public void setActionCommand(String ac) {
         actionCommand = ac;
     }
 
     /**
      * Returns the action command for this button.
      *
      * @return the action command for this button
      */
     public final String getActionCommand() {
         return actionCommand;
     }
 
 
     /**
      * Return the Button group where this button lies in
      *
      * @return Button Group or null if not in a group
      */
     public final SButtonGroup getGroup() {
         return buttonGroup;
     }
 
     protected void setParentFrame(SFrame f) {
        if (f == getParentFrame())
            return;

         if (buttonGroup != null && getSession().getDispatcher() != null) {
             getSession().getDispatcher().removeLowLevelEventListener(this, buttonGroup.getComponentId());
         } // end of if ()
 
         super.setParentFrame(f);
 
        if (buttonGroup != null && f != null && getSession().getDispatcher() != null) {
             getSession().getDispatcher().addLowLevelEventListener(this, buttonGroup.getComponentId());
         } // end of if ()
     }
 
     /**
      * Add this button to a button group. This influences the event-prefix
      * this button reports to the request dispatcher: it will change to
      * the button group's prefix.
      */
     protected void setGroup(SButtonGroup g) {
         if (isDifferent(buttonGroup, g)) {
            // Do no longer react on  events from old button group
             if (buttonGroup != null && getSession().getDispatcher() != null) {
                 getSession().getDispatcher().removeLowLevelEventListener(this, buttonGroup.getComponentId());
             } // end of if ()
             buttonGroup = g;
            // Button Group changed but button already registered via parent frame?
            if (buttonGroup != null && getParentFrame() != null && getSession().getDispatcher() != null) {
                getSession().getDispatcher().addLowLevelEventListener(this, buttonGroup.getComponentId());
             } // end of if ()
             reload();
         }
     }
 
     /**
      * Adds an ActionListener to the button.
      *
      * @param listener the ActionListener to be added
      */
     public void addActionListener(ActionListener listener) {
         addEventListener(ActionListener.class, listener);
     }
 
     /**
      * Removes the supplied Listener from the listener list
      */
     public void removeActionListener(ActionListener listener) {
         removeEventListener(ActionListener.class, listener);
     }
 
     /**
      * Returns an array of all the <code>ActionListener</code>s added
      * to this AbstractButton with addActionListener().
      *
      * @return all of the <code>ActionListener</code>s added or an empty
      *         array if no listeners have been added
      */
     public ActionListener[] getActionListeners() {
         return (ActionListener[]) (getListeners(ActionListener.class));
     }
 
     /**
      * Fire an ActionEvent at each registered listener.
      *
      * @param event supplied ActionEvent
      */
     protected void fireActionPerformed(ActionEvent event) {
         // Guaranteed to return a non-null array
         Object[] listeners = getListenerList();
         ActionEvent e = null;
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ActionListener.class) {
                 if (e == null) {
                     String actionCommand = event.getActionCommand();
                     if (actionCommand == null) {
                         actionCommand = getActionCommand();
                     }
                     e = new ActionEvent(SAbstractButton.this,
                             ActionEvent.ACTION_PERFORMED,
                             actionCommand,
                             event.getWhen(),
                             event.getModifiers());
                 }
                 ((ActionListener) listeners[i + 1]).actionPerformed(e);
             }
         }
     }
 
 
     /**
      * Sets the button type. Use one of the following types:
      * <UL>
      * <LI> {@link #SUBMIT_BUTTON}
      * <LI> {@link #RESET_BUTTON}
      * <LI> {@link #CHECKBOX}
      * <LI> {@link #RADIOBUTTON}
      * </UL>
      */
     public void setType(String t) {
         if (isDifferent(type, t)) {
             type = t;
             reload();
         }
     }
 
     /**
      * Delifers the Button Type
      *
      * @return Button Type
      */
     public final String getType() {
         return type;
     }
 
     /**
      * Simulates an click on the Button
      */
     public void doClick() {
         setSelected(!isSelected());
 
         fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
     }
 
     /**
      * Sets the state of the button.      *
      *
      * @param b true if the button is selected, otherwise false
      */
     public void setSelected(boolean b) {
         if (isSelected() != b) {
             if (buttonGroup != null) {
                 buttonGroup.setSelected(this, b);
             }
             super.setSelected(b);
         }
     }
 
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
 
         delayEvents(true);
 
         boolean requestSelection = isSelected();
 
         int eventCount = 0;
 
         if (buttonGroup != null) {
             // button group prefix is shared, so maybe more than one value is
             // delivered in a form
             for (int i = 0; i < values.length; i++) {
 
                 // with button group the value has a special encoding...
                 // this is because in a form the name of a parameter for
                 // buttons in a buttongroup must be the same...
                 String value = values[i];
 
                 // illegal format
                 if (value.length() < 3) { continue;  }
 
                 // no uid DIVIDER
                 // value.charAt(value.length()-2)!=UID_DIVIDER ) { break; }
 
                 // not for me
                 if (!value.startsWith(super.getLowLevelEventId())) { continue; }
 
                 // last character is indicator, if button should be
                 // selected or not
                 switch (value.charAt(value.length() - 1)) {
                     case '1':
                         requestSelection = true;
                         ++eventCount;
                         break;
                     case '0':
                         requestSelection = false;
                         ++eventCount;
                         break;
                 }
             }
         } else {
             for (int i = 0; i < values.length; i++) {
                 requestSelection = parseSelectionToggle(values[0]);
                 ++eventCount;
             }
         }
 
         /*
          * Checkboxes in HTML-forms write two form components:
          * one hidden input, containing the deselect-command (value='0'),
          * and one <input type="checkbox".. value="1">.
          * This is, because browsers send the checkbox-variable
          * only if it is set, not if it is not set.
          * So, if we get two events with the same name, then the checkbox
          * actually got selected; if we only got one event (from the hidden
          * input), then it was a deselect-event (select event value = '1',
          * deselect value = '0').
          * This is just in case, the browser sends the both events
          * in the wrong order (select and then deselect).
          */
         if (eventCount == 2) {
             requestSelection = true;
         }
 
         if (isSelected() != requestSelection) {
             if (buttonGroup != null) {
                 buttonGroup.setDelayEvents(true);
                 setSelected(requestSelection);
                 buttonGroup.setDelayEvents(false);
             } else {
                 setSelected(requestSelection);
             }
 
             SForm.addArmedComponent(this);
         }
 
         delayEvents(false);
     }
 
     public void fireIntermediateEvents() {
         super.fireIntermediateEvents();
         if (buttonGroup != null) {
             buttonGroup.fireDelayedIntermediateEvents();
         }
     }
 
     public void fireFinalEvents() {
         super.fireFinalEvents();
         fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand()));
         if (buttonGroup != null)
             buttonGroup.fireDelayedFinalEvents();
     }
 
     public final String getEventTarget() {
         return eventTarget;
     }
 
 
     public void setEventTarget(String target) {
         if (isDifferent(eventTarget, target)) {
             eventTarget = target;
             reload();
         }
     }
 
     protected boolean parseSelectionToggle(String toggleParameter) {
         if ("1".equals(toggleParameter))
             return true;
         else if ("0".equals(toggleParameter))
             return false;
 
         // don't change...
         return isSelected();
     }
 
     public String getToggleSelectionParameter() {
         return isSelected() ? getDeselectionParameter() : getSelectionParameter();
     }
 
     public String getSelectionParameter() {
         return "1";
     }
 
     public String getDeselectionParameter() {
         return "0";
     }
 
 
     /**
      * Sets the action for the ActionEvent source.
      * the new action code will replace the old one but not the one bound to the actionListener
      *
      * @param a the Action for the AbstractButton,
      */
 
     public void setAction(Action a) {
         Action oldValue = getAction();
         if (action == null || !action.equals(a)) {
             action = a;
             if (oldValue != null) {
                 removeActionListener(oldValue);
                 oldValue.removePropertyChangeListener(actionPropertyChangeListener);
                 actionPropertyChangeListener = null;
             }
             configurePropertiesFromAction(action);
             if (action != null) {
                 // Don't add if it is already a listener
                 if (!isListener(ActionListener.class, action)) {
                     addActionListener(action);
                 }
                 // Reverse linkage:
                 actionPropertyChangeListener = createActionPropertyChangeListener(action);
                 action.addPropertyChangeListener(actionPropertyChangeListener);
             }
             reload();
         }
     }
 
     private boolean isListener(Class c, ActionListener a) {
         boolean isListener = false;
         Object[] listeners = getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == c && listeners[i + 1] == a) {
                 isListener = true;
             }
         }
         return isListener;
     }
 
     /**
      * Returns the action for this ActionEvent source, or <code>null</code>
      * if no <code>Action</code> is set.
      *
      * @return the <code>Action</code> for this <code>ActionEvent</code>
      *         source, or <code>null</code>
      */
     public Action getAction() {
         return action;
     }
 
     public void setCG(ButtonCG cg) {
         super.setCG(cg);
     }
 
     protected void configurePropertiesFromAction(Action a) {
         // uncomment if compiled against < jdk 1.3
         //	setActionCommand((a != null
         //                  ? (String)a.getValue(Action.ACTION_COMMAND_KEY)
         //                  : null));
         setText((a != null ? (String) a.getValue(Action.NAME) : null));
         setIcon((a != null ? (SIcon) a.getValue(Action.SMALL_ICON) : null));
         setEnabled((a != null ? a.isEnabled() : true));
         setToolTipText((a != null ? (String) a.getValue(Action.SHORT_DESCRIPTION) : null));
     }
 
     protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
         return new ButtonActionPropertyChangeListener(this, a);
     }
 
     private static class ButtonActionPropertyChangeListener
             extends AbstractActionPropertyChangeListener {
         ButtonActionPropertyChangeListener(SAbstractButton b, Action a) {
             super(b, a);
         }
 
         public void propertyChange(PropertyChangeEvent e) {
             String propertyName = e.getPropertyName();
             SAbstractButton button = (SAbstractButton) getTarget();
             if (button == null) {
                 Action action = (Action) e.getSource();
                 action.removePropertyChangeListener(this);
             } else {
                 if (e.getPropertyName().equals(Action.NAME)) {
                     String text = (String) e.getNewValue();
                     button.setText(text);
                 } else if (e.getPropertyName().equals(Action.SHORT_DESCRIPTION)) {
                     String text = (String) e.getNewValue();
                     button.setToolTipText(text);
                 } else if (propertyName.equals("enabled")) {
                     Boolean enabled = (Boolean) e.getNewValue();
                     button.setEnabled(enabled.booleanValue());
                 } else if (e.getPropertyName().equals(Action.SMALL_ICON)) {
                     SIcon icon = (SIcon) e.getNewValue();
                     button.setIcon(icon);
                 }
                 // uncomment if compiled against jdk < 1.3
                 /*                else if (e.getPropertyName().equals(Action.ACTION_COMMAND_KEY)) {
                                   String actionCommand = (String)e.getNewValue();
                                   button.setActionCommand(actionCommand);
                                   }*/
             }
         }
     }
 
     public void setMnemonic(String mnemonic) {
         reloadIfChange(this.mnemonic, mnemonic);
         this.mnemonic = mnemonic;
     }
 
     public String getMnemonic() {
         return mnemonic;
     }
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     public boolean isEpochCheckEnabled() {
         return epochCheckEnabled;
     }
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     public void setEpochCheckEnabled(boolean epochCheckEnabled) {
         this.epochCheckEnabled = epochCheckEnabled;
     }
 
     /**
      * Determiens if the label text word wrap inside the browser. Defaults to <code>false</code> (Swing).
      * @return <code>false</code> if the label should not word wrap an be in line as in Swing.
      */
     public boolean isWordWrap() {
         return wordWrap;
     }
 
     /**
      * Defines if the label is allowed to wrap.
      * @param wordWrap Set to <code>true</code> if you want labels to allow to break into more lines than passed.
      */
     public void setWordWrap(boolean wordWrap) {
         if (this.wordWrap != wordWrap) {
             this.wordWrap = wordWrap;
             reload();
         }
     }
 }
