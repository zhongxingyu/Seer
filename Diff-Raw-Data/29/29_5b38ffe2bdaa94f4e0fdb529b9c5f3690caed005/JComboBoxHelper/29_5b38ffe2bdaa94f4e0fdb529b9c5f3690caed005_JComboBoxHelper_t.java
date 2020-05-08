 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing.swing.implclasses;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Rectangle;
 import java.util.Arrays;
 
 import javax.accessibility.Accessible;
 import javax.accessibility.AccessibleContext;
 import javax.swing.ComboBoxEditor;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 
 /**
  * This is a helper class of <code>JComboBoxImplClass</code>.
  * @author BREDEX GmbH
  * @created 21.03.2005
  */
 public class JComboBoxHelper {
     /** The logger. */
     private static AutServerLogger log = new AutServerLogger(
             JComboBoxHelper.class);
 
     /** The Robot. */
     private final IRobot m_robot;
     /** The event thread queuer. */
     private final IEventThreadQueuer m_eventThreadQueuer;
     /** The JList helper. */
     private final JListHelper m_listHelper;
     /** the implementation class */
     private final JComboBoxImplClass m_implClass;
 
     /**
      * Creates a new instance.
      * @param implClass The implementation class of <code>JComboBox</code>.
      */
     public JComboBoxHelper(JComboBoxImplClass implClass) {
         Validate.notNull(implClass,
             "The JComboBox implementation class must not be null"); //$NON-NLS-1$
         m_robot = implClass.getRobot();
         m_eventThreadQueuer = implClass.getEventThreadQueuer();
         m_implClass = implClass;
         m_listHelper = new JListHelper(implClass);
     }
 
     /**
      * @param component
      *            the combobox
      * @return the editor used to render and edit the selected item in the
      *         JComboBox field.
      * @throws StepExecutionException
      *             if the editor comonent could not be found
      */
     private Component getComboBoxEditorComponent(JComboBox component)
         throws StepExecutionException {
 
         ComboBoxEditor cbe = component.getEditor();
         if (cbe == null) {
             throw new StepExecutionException("no ComboBoxEditor found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
         Component c = cbe.getEditorComponent();
         if (c == null) {
             throw new StepExecutionException("no EditorComponent found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
         return c;
     }
 
     /**
      * Tries to find the component in the component hierarchy
      * @param component where to search
      * @param c type of the component which should be found
      * @return the desired component
      */
     private Component getComponentViaHierarchy(Container component, Class c) {
         Component[] comps = component.getComponents();
         for (int i = 0; i < comps.length; i++) {
             if (c.isInstance(comps[i])) {
                 return comps[i];
             }
         }
         for (int i = 0; i < comps.length; i++) {
             if (comps[i] instanceof Container) {
                 Component ct = getComponentViaHierarchy((Container)comps[i], c);
                 if (ct != null) {
                     return ct;
                 }
             }
         }
         return null;
     }
 
     /**
      * Tries to find the popup menu from the combobox
      * @param component the combobox
      * @return the popup of the combobox
      * @throws StepExecutionException if the popup could not be found
      */
     private JPopupMenu getPopupMenu(JComboBox component)
         throws StepExecutionException {
 
         AccessibleContext ac = component.getAccessibleContext();
         for (int i = 0; i < ac.getAccessibleChildrenCount(); i++) {
             Accessible a = ac.getAccessibleChild(i);
             if (a instanceof JPopupMenu) {
                 return (JPopupMenu)a;
             }
         }
         throw new StepExecutionException("cannot find dropdown list", //$NON-NLS-1$
             EventFactory.createActionError(
                     TestErrorEvent.DROPDOWN_LIST_NOT_FOUND));
     }
 
     /**
      * Inputs <code>text</code> to <code>component</code>.<br>
      * @param text the text to type in
      * @param replace whether to rplace the text or not
      * @throws StepExecutionException if an error occurs during typing <code>text</code>
      * @throws IllegalArgumentException if <code>component</code> or <code>text</code> are null
      */
     public void input(String text, boolean replace)
         throws StepExecutionException, IllegalArgumentException {
 
         Validate.notNull(text, "text must not be null"); //$NON-NLS-1$
         Component editor = getComboBoxEditorComponent((JComboBox)m_implClass
                 .getComponent());
         if (editor == null) {
             throw new StepExecutionException("could not find editor", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
         if (replace) {
             selectAll();
         }
         m_robot.type(editor, text);
     }
 
     /**
      * @return true, if the popup of the combobox is visible
      */
     private boolean isPopupVisible() {
         Boolean visible = (Boolean)m_eventThreadQueuer.invokeAndWait(
             JComboBoxHelper.class.getName()
             + "isPopupVisible", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     JComboBox comboBox = (JComboBox)m_implClass.getComponent();
                     return comboBox.isPopupVisible()
                         ? Boolean.TRUE : Boolean.FALSE;
                 }
             });
         return visible.booleanValue();
     }
 
     /**
      * @return a rectangle, where the arrow icon is expected.
      */
     private Rectangle findArrowIconArea() {
         JComboBox comboBox = (JComboBox)m_implClass.getComponent();
         Component editor = getComboBoxEditorComponent(comboBox);
         Rectangle r = null;
         if (editor == null) {
             throw new StepExecutionException("could not find editor", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
         Rectangle ra[] =
             SwingUtilities.computeDifference(comboBox.getBounds(),
                 editor.getBounds());
         if ((ra == null) || (ra.length < 1)) {
             throw new StepExecutionException("could not arrow icon", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
         r = ra[0];
         // find the largest area of the returned rectangles.
         double bestAreaIndex = Double.MAX_VALUE;
         for (int i = 0; i < ra.length; i++) {
             if ((ra[i].height > 0) && (ra[i].width > 0)) {
                 double areaIndex = ((double)ra[i].width) / ra[i].height - 1.0;
                 if (areaIndex < 0) {
                     areaIndex *= (-1);
                 }
                 if (areaIndex < bestAreaIndex) {
                     bestAreaIndex = areaIndex;
                     r = ra[i];
                 }
             }
         }
         return r;
     }
 
     /**
      * Opens the combobox popup menu and returns the popup instance. May also be
      * called if the popup is already visible
      * @return The popup menu
      */
     private JPopupMenu openPopupMenu() {
         JComboBox comboBox = (JComboBox)m_implClass.getComponent();
         if (!isPopupVisible()) {
             Component c = getComponentViaHierarchy(comboBox, JButton.class);
             Rectangle r = null;
             if ((c == null) && (!comboBox.isEditable())) {
                 c = comboBox;
             } else if ((c == null) && (comboBox.isEditable())) {
                 c = comboBox;
                 r = findArrowIconArea();
             }
             if (log.isDebugEnabled()) {
                 log.debug("Opening popup by clicking on: " + c); //$NON-NLS-1$
             }
             m_robot.click(c, r);
         }
         if (!isPopupVisible()) {
             log.debug("Dropdown list still not visible, must be an error"); //$NON-NLS-1$
             throw new StepExecutionException("dropdown list not visible", //$NON-NLS-1$
                 EventFactory.createActionError(
                         TestErrorEvent.DROPDOWN_LIST_NOT_FOUND));
         }
         return getPopupMenu(comboBox);
     }
 
     /**
      * Finds the <code>JList</code> of the combobox.
      * @return The list
      */
     private JList findJList() {
         JList list = (JList)getComponentViaHierarchy(openPopupMenu(),
                 JList.class);
         if (list == null) {
             throw new StepExecutionException("list component not found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
         return list;
     }
 
     /**
      * Selects the specified item in the combobox.
      * @param values the values which should be (not) selected
      * @param operator if regular expressions are used
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param maxWidth the maximal width which is used to select the item
      * @throws StepExecutionException if an error occurs during selecting the item
      * @throws IllegalArgumentException if <code>component</code> or <code>text</code> are null
      */
     public void select(final String[] values, String operator,
         String searchType, double maxWidth)
         throws StepExecutionException, IllegalArgumentException {
         for (int i = 0; i < values.length; i++) {
             String text = values[i];
             Validate.notNull(text, "text must not be null"); //$NON-NLS-1$
         }
         JList list = findJList();
         Integer[] indices = m_listHelper.findIndicesOfValues(list, values,
                 operator, searchType);
         Arrays.sort(indices);
         if (indices.length == 0) {
             throw new StepExecutionException("Text '" + Arrays.asList(values).toString() //$NON-NLS-1$
                 + "' not found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         m_listHelper.clickOnIndex(list, indices[0], ClickOptions
                 .create().setClickCount(1), maxWidth);
     }
 
     /**
      * Selects the combobox element with the passed index.
      * @param index The index to select
      * @param maxWidth the maximal width which is used to select the item
      */
     public void select(int index, double maxWidth) {
         m_listHelper.clickOnIndex(findJList(), new Integer(index), ClickOptions
                 .create().setClickCount(1), maxWidth);
     }
 
     /**
      * select the whole text of  the textfield by clicking three times.
      */
     public void selectAll() {
         click(new Integer(1));
         m_robot.keyStroke(m_robot.getSystemModifierSpec() + " A"); //$NON-NLS-1$
     }
 
     /**
      * performs a <code>count</code> -click on the textfield.
      * @param count the number of clicks
      */
     public void click(Integer count) {
         Component editor = getComboBoxEditorComponent((JComboBox)m_implClass
                 .getComponent());
         if (editor == null) {
             throw new StepExecutionException("no editor found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
         m_robot.click(editor, null, ClickOptions.create().setClickCount(
                 count.intValue()));
     }
     /**
      * @param value The value to check
      * @param operator The operator used to verify
      * @return <code>true</code> if the combobox contains an element rendered with the passed value
      */
     public boolean containsValue(String value, String operator) {
         return m_listHelper.containsValue(findJList(), value, operator);
     }
 
     /**
      * @param value The value to check
      * @return <code>true</code> if the combobox contains an element rendered with the passed value
      */
     public boolean containsValue(String value) {
         return m_listHelper.containsValue(findJList(), value);
     }
 
     /**
      * @return If the combobox is editable
      */
     public boolean isEditable() {
         Boolean editable = (Boolean)m_eventThreadQueuer.invokeAndWait("isEditable", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     JComboBox comboBox = (JComboBox)m_implClass.getComponent();
                     // see findBugs
                     return comboBox.isEditable() ? Boolean.TRUE : Boolean.FALSE;
                 }
             });
         return editable.booleanValue();
     }
 
     /**
      * @return the String from the Cell Renderer
      */
     public String getText() {
        final JComboBox comboBox = ((JComboBox) m_implClass.getComponent());
        String comboBoxText;
        if (comboBox.isEditable()) {
            comboBoxText = m_implClass.getRenderedText(
                    getComboBoxEditorComponent(comboBox), true);
        } else {
            final int selIndex = comboBox.getSelectedIndex();
            if (selIndex == -1) {
                comboBoxText = String.valueOf(
                        comboBox.getSelectedItem());
            } else {
                final JList jlist = new JList(comboBox.getModel());
                Object o = m_eventThreadQueuer.invokeAndWait(
                        "getText", new IRunnable() { //$NON-NLS-1$
                            public Object run() {
                                Component disp = comboBox.getRenderer()
                                    .getListCellRendererComponent(jlist,
                                        jlist.getModel().getElementAt(selIndex),
                                        selIndex, true, comboBox.hasFocus());
                                return m_implClass.getRenderedText(disp, false);
                            }
                        });
                comboBoxText = String.valueOf(o);
            }
        }
        return comboBoxText;
     }
 }
