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
 
 import java.util.Arrays;
 
 import javax.swing.JComponent;
 import javax.swing.JList;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.DragAndDropHelper;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.IndexConverter;
 import org.eclipse.jubula.rc.common.implclasses.ListSelectionVerifier;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.swing.swing.interfaces.IJListImplClass;
 import org.eclipse.jubula.rc.swing.utils.SwingUtils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.StringParsing;
 
 
 /**
  * The implementation class for <code>JList</code>.
  * @author BREDEX GmbH
  * @created 11.08.2005
  */
 public class JListImplClass extends AbstractSwingImplClass 
     implements IJListImplClass {
 
     /** The <code>JList</code> component. */
     private JList m_list;
     /** The <code>JList</code> helper. */
     private JListHelper m_listHelper;
 
     /**
      * {@inheritDoc}
      */
     public JComponent getComponent() {
         return m_list;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setComponent(Object graphicsComponent) {
         m_list = (JList)graphicsComponent;
         m_listHelper = new JListHelper(this);
     }
 
     /**
      * Splits the enumeration of values.
      * @param values The values to split
      * @param separator The separator, may be <code>null</code>
      * @return The array of values
      */
     private String[] split(String values, String separator) {
         String[] list = StringParsing.splitToArray(values, ((separator == null)
             || (separator.length() == 0) ? INDEX_LIST_SEP_CHAR
                 : separator.charAt(0)),
             TestDataConstants.ESCAPE_CHAR_DEFAULT);
         list = StringUtils.stripAll(list);
         return list;
     }
 
     /**
      * Parses the enumeration of indices.
      * @param indexList The enumeration of indices
      * @return The array of parsed indices
      */
     private int[] parseIndices(String indexList) {
         String[] list = StringParsing.splitToArray(indexList,
                 INDEX_LIST_SEP_CHAR, TestDataConstants.ESCAPE_CHAR_DEFAULT);
         int[] indices = new int[list.length];
         for (int i = 0; i < list.length; i++) {
             indices[i] = IndexConverter.intValue(list[i]);
         }
 
         return indices;
     }
 
     /**
      * @return The array of selected indices
      * @throws StepExecutionException If there are no indices selected
      */
     private int[] getCheckedSelectedIndices() throws StepExecutionException {
         int[] selected = m_listHelper.getSelectedIndices(m_list);
         if (selected.length == 0) {
             throw new StepExecutionException("No list element selected", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NO_SELECTION));
         }
         return selected;
     }
 
     /**
      * @param indices The indices to select
      * @param co the click options to use
      * @param isExtendSelection Whether this selection extends a previous 
      *                          selection. If <code>true</code>, the first 
      *                          element will be selected with CONTROL as a 
      *                          modifier.
      */
     private void selectIndices(int[] indices, ClickOptions co, 
             boolean isExtendSelection) {
       
         if (indices.length > 0) {
             try {
                 if (isExtendSelection) {
                     getRobot().keyPress(m_list,
                             SwingUtils.getSystemDefaultModifier());
                 }
 
                 // first selection
                 m_listHelper.clickOnIndex(m_list, new Integer(indices[0]), co);
             } finally {
                 if (isExtendSelection) {
                     getRobot().keyRelease(m_list,
                             SwingUtils.getSystemDefaultModifier());
                 }
             }
         }
         try {
             getRobot().keyPress(m_list,
                     SwingUtils.getSystemDefaultModifier());
            // following selections
             for (int i = 1; i < indices.length; i++) {
                 m_listHelper.clickOnIndex(m_list, new Integer(indices[i]), co);
             }
         } finally {
             getRobot().keyRelease(m_list,
                     SwingUtils.getSystemDefaultModifier());
         }
     }
 
     /**
      * Verifies if the passed index or enumeration of indices is selected. The
      * enumeration must be separated by <code>,</code>, e.g. <code>1, 3,6</code>.
      * @param indexList The index or indices to verify
      * @param isSelected Whether the index or indices should be selected or not.
      */
     public void gdVerifySelectedIndex(String indexList, boolean isSelected) {
         int[] selected = getCheckedSelectedIndices();
         int[] expected = parseIndices(indexList);
         int[] implExpected = IndexConverter.toImplementationIndices(expected);
         if (isSelected) {
             Verifier.equals(implExpected.length, selected.length);
         }
         for (int i = 0; i < implExpected.length; i++) {
             int index = implExpected[i];
             Verifier.equals(index, selected[i], isSelected);
         }
     }
 
     /**
      * Verifies if the passed value or enumeration of values is selected. By
      * default, the enumeration separator is <code>,</code>
      * @param valueList The value or list of values to verify
      */
     public void gdVerifySelectedValue(String valueList) {
         gdVerifySelectedValue(valueList, MatchUtil.DEFAULT_OPERATOR, true);
     }
 
     /**
      * Verifies if the passed value or enumeration of values is selected. By
      * default, the enumeration separator is <code>,</code>, but may be
      * changed by <code>separator</code>.
      * @param valueList The value or list of values to verify
      * @param operator
      *  The operator to use when comparing the expected and actual values.
      *  @param isSelected if the value(s) should be selected or not.
      */
     public void gdVerifySelectedValue(String valueList, String operator,
             boolean isSelected) {
 
         gdVerifySelectedValue(valueList, operator,
             String.valueOf(VALUE_SEPARATOR), isSelected);
     }
 
 
     /**
      * Verifies if the passed value or enumeration of values is selected. By
      * default, the enumeration separator is <code>,</code>, but may be
      * changed by <code>separator</code>.
      * @param valueList The value or list of values to verify
      * @param operator
      *            The operator to use when comparing the expected and
      *            actual values.
      * @param separator The separator
      * @param isSelected if the value(s) should be selected or not.
      */
     public void gdVerifySelectedValue(String valueList, String operator,
             String separator, boolean isSelected) {
 
         final String[] current = m_listHelper.getSelectedValues(m_list);
         final ListSelectionVerifier listSelVerifier =
             new ListSelectionVerifier();
         for (int i = 0; i < current.length; i++) {
             listSelVerifier.addItem(i, current[i], true);
         }
         listSelVerifier.verifySelection(valueList, operator, !isSelected,
                 isSelected);
     }
 
     /**
      * Verifies if all selected elements of a list match a text.
      * @param text The text to verify
      * @param operator The operator used to verify
      */
     public void gdVerifyText(String text, String operator) {
         String[] selected = m_listHelper.getSelectedValues(m_list);
         final int selCount = selected.length;
         if (selCount < 1) {
             throw new StepExecutionException("No selection", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NO_SELECTION));
         }
         for (int i = 0; i < selCount; i++) {
             Verifier.match(selected[i], text, operator);
         }
     }
 
     /**
      * Selects the passed index or enumeration of indices. The enumeration must
      * be separated by <code>,</code>, e.g. <code>1, 3,6</code>.
      * @param indexList The index or indices to select
      * @param extendSelection Whether this selection extends a previous 
      *                        selection.
      * @param button what mouse button should be used
      */
     public void gdSelectIndex(String indexList, final String extendSelection,
             int button) {
         final boolean isExtendSelection = extendSelection
                 .equals(CompSystemConstants.EXTEND_SELECTION_YES);
         selectIndices(IndexConverter
                 .toImplementationIndices(parseIndices(indexList)), ClickOptions
                 .create().setClickCount(1).setMouseButton(button),
                 isExtendSelection);
     }
 
     /**
      * Selects the passed value or enumeration of values. By default, the
      * enumeration separator is <code>,</code>.
      * @param valueList The value or list of values to select
      * @param operator If regular expressions are used
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param isExtendSelection Whether this selection extends a previous 
      *                          selection. If <code>true</code>, the first 
      *                          element will be selected with CONTROL as a 
      *                          modifier.
      * @param button what mouse button should be used
      */
     public void gdSelectValue(String valueList, String operator,
         final String searchType, final String isExtendSelection, int button) {
         selectValue(valueList, String.valueOf(VALUE_SEPARATOR), operator,
                 searchType, ClickOptions.create().setClickCount(1)
                         .setMouseButton(button), isExtendSelection);
     }
 
     /**
      * Drags the passed value.
      *
      * @param mouseButton the mouseButton.
      * @param modifier the modifier.
      * @param value The value to drag
      * @param operator If regular expressions are used
      * @param searchType Determines where the search begins ("relative" or "absolute")
      */
     public void gdDragValue(int mouseButton, String modifier, String value,
             String operator, final String searchType) {
 
         DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
 
         Integer [] indices = m_listHelper.findIndicesOfValues(
             m_list, new String [] {value}, operator, searchType);
         selectIndices(ArrayUtils.toPrimitive(indices), 
                 ClickOptions.create().setClickCount(0), false);
 
         pressOrReleaseModifiers(modifier, true);
         getRobot().mousePress(null, null, mouseButton);
     }
 
     /**
      * Drops on the passed value.
      *
      * @param value The value on which to drop
      * @param operator If regular expressions are used
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      */
     public void gdDropValue(String value, String operator,
         final String searchType, int delayBeforeDrop) {
 
         DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         try {
             Integer [] indices = m_listHelper.findIndicesOfValues(
                 m_list, new String [] {value}, operator, searchType);
             selectIndices(ArrayUtils.toPrimitive(indices), 
                     ClickOptions.create().setClickCount(0), false);
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
 
     /**
      * Drags the passed index.
      *
      * @param mouseButton the mouseButton.
      * @param modifier the modifier.
      * @param index The index to drag
      */
     public void gdDragIndex(final int mouseButton, final String modifier,
             int index) {
 
         DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
 
         selectIndices(
                 new int [] {IndexConverter.toImplementationIndex(index)}, 
                 ClickOptions.create().setClickCount(0), false);
 
         pressOrReleaseModifiers(modifier, true);
         getRobot().mousePress(null, null, mouseButton);
     }
 
     /**
      * Drops onto the passed index.
      *
      * @param index The index on which to drop
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      */
     public void gdDropIndex(final int index, int delayBeforeDrop) {
 
         DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
 
         try {
             selectIndices(
                     new int [] {IndexConverter.toImplementationIndex(index)}, 
                     ClickOptions.create().setClickCount(0), false);
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
 
     /**
      * Selects the passed value or enumeration of values. By default, the
      * enumeration separator is <code>,</code>, but may be changed by
      * <code>separator</code>.
      * @param valueList The value or list of values to select
      * @param separator The separator, optional
      * @param operator If regular expressions are used
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param clickCount the amount of clicks to use
      * @param extendSelection Whether this selection extends a previous 
      *                        selection.
      */
     public void gdSelectValue(String valueList, String separator,
             String operator, final String searchType, int clickCount, 
             final String extendSelection) {
         selectValue(valueList, separator, operator, searchType, ClickOptions
                 .create().setClickCount(clickCount), extendSelection);
     }
     
     /**
      * Selects the passed value or enumeration of values. By default, the
      * enumeration separator is <code>,</code>, but may be changed by
      * <code>separator</code>.
      * @param valueList The value or list of values to select
      * @param separator The separator, optional
      * @param operator If regular expressions are used
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param co the click options to use
      * @param extendSelection Whether this selection extends a previous 
      *                        selection.
      */
     private void selectValue(String valueList, String separator,
             String operator, final String searchType, ClickOptions co, 
             final String extendSelection) {
 
         String[] values = null;
         final boolean isExtendSelection = 
             extendSelection.equals(CompSystemConstants.EXTEND_SELECTION_YES);
         if (StringConstants.EMPTY.equals(valueList)) {
             values = new String[1];
             values[0] = StringConstants.EMPTY;
         } else {
             values = split(valueList, separator);
         }
         Integer[] indices = m_listHelper.findIndicesOfValues(m_list, values,
                 operator, searchType);
         Arrays.sort(indices);
         if (!operator.equals(MatchUtil.NOT_EQUALS) 
                 && (indices.length < values.length)) {
             throw new StepExecutionException("One or more values not found of set: " //$NON-NLS-1$
                 + Arrays.asList(values).toString(),
                 EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         selectIndices(ArrayUtils.toPrimitive(indices), co, isExtendSelection);
     }
 
     /**
      * Verifies if the list contains an element that renderes <code>value</code>.
      * @param value The text to verify
      */
     public void gdVerifyContainsValue(String value) {
         Verifier.equals(true, m_listHelper.containsValue(m_list, value));
     }
 
     /**
      * Verifies if the list contains an element that renderes <code>value</code>.
      * @param value The text to verify
      * @param operator The operator used to verify
      * @param exists if the wanted value should exist or not.
      */
     public void gdVerifyContainsValue(String value, String operator,
             boolean exists) {
 
         Verifier.equals(exists, m_listHelper.containsValue(m_list, value,
             operator));
     }
 
     /**
      * Action to read the value of the current selected item of the JList
      * to store it in a variable in the Client
      * @param variable the name of the variable
      * @return the text value.
      */
     public String gdReadValue(String variable) {
         String[] selected = m_listHelper.getSelectedValues(m_list);
         if (selected.length > 0) {
             return selected[0];
         }
         throw new StepExecutionException("No list item selected", //$NON-NLS-1$
             EventFactory.createActionError(TestErrorEvent.NO_SELECTION));
     }
 
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         return null;
     }
 
     /**
      * @see org.eclipse.jubula.rc.swing.swing.implclasses.AbstractSwingImplClass#getText()
      * @return {@link JListHelper#getText} value
      */
     protected String getText() {
         // FIXME
         return null;
     }
 
 
 }
