 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets;
 
 import org.eclipse.core.databinding.observable.list.IObservableList;
 
 /**
  * A ridget with two areas. The master area shows a table of objects, from which
  * one can be selected. The details are allows the user to edit some details of
  * the currently selected object/row.
  * <p>
  * This ridget is an {@link IComplexRidget} an {@link ITableRidget} to show the
  * available objects and several {@link IActionRidget}s to add, delete, update
  * the row elements.
  * <p>
  * The UI of the details area is created by implementing an
  * MasterDetailComposite. The binding between UI and ridgets is done by
  * implementing an {@link IMasterDetailsDelegate} and introducing it to this
  * ridget via {@link #setDelegate(IMasterDetailsDelegate)}.
  */
 public interface IMasterDetailsRidget extends IComplexRidget {
 
 	/**
 	 * Binds the table to the model data.
 	 * 
 	 * @param rowObservables
 	 *            An observable list of objects (non-null).
 	 * @param rowClass
 	 *            The class of the objects in the list.
 	 * @param columnPropertyNames
 	 *            The list of property names that are to be displayed in the
 	 *            columns. One property per column. Each object in
 	 *            rowObservables must have a corresponding getter. This
 	 *            parameter must be a non-null String array.
 	 * @param columnHeaders
 	 *            The titles of the columns to be displayed in the header. May
 	 *            be null if no headers should be shown for this table.
 	 *            Individual array entries may be null, which will show an empty
 	 *            title in the header of that column.
 	 * @throws RuntimeException
 	 *             when columnHeaders is non-null and the the number of
 	 *             columnHeaders does not match the number of
 	 *             columnPropertyNames
 	 */
 	void bindToModel(IObservableList rowObservables, Class<? extends Object> rowClass, String[] columnPropertyNames,
 			String[] columnHeaders);
 
 	/**
 	 * @param listHolder
 	 *            An object that has a property with a list of objects.
 	 * @param listPropertyName
 	 *            Property for accessing the list of objects.
 	 * @param rowClass
 	 *            Property for accessing the list of objects.
 	 * @param columnPropertyNames
 	 *            The list of property names that are to be displayed in the
 	 *            columns. One property per column. Each object in
 	 *            rowObservables must have a corresponding getter. This
 	 *            parameter must be a non-null String array.
 	 * @param headerNames
 	 *            The titles of the columns to be displayed in the header. May
 	 *            be null if no headers should be shown for this table.
 	 *            Individual array entries may be null, which will show an empty
 	 *            title in the header of that column.
 	 */
 	void bindToModel(Object listHolder, String listPropertyName, Class<? extends Object> rowClass,
 			String[] columnPropertyNames, String[] headerNames);
 
 	/**
 	 * This method checks if the details can be overwritten by
 	 * {@link #suggestNewEntry(Object)}. If the details are dirty, the user will
 	 * be asked to discard changes (via a blocking dialog).
 	 * 
 	 * @return true if the details or not dirty or can be overwritten (@see
 	 *         {@link #suggestNewEntry(Object)}, false otherwise
 	 * @since 3.0
 	 */
 	boolean canSuggest();
 
 	/**
 	 * Return the current {@link IMasterDetailsDelegate} or null, if none has
 	 * (yet) been set.
 	 * 
 	 * @return the current {@link IMasterDetailsDelegate} or null
 	 * @since 2.0
 	 */
 	IMasterDetailsDelegate getDelegate();
 
 	/**
 	 * Returns the currently object corresponding to the currently selected row
 	 * in the ridget.
 	 * 
 	 * @return the actual selection in the ridget or null (if nothing is
 	 *         selected)
 	 */
 	Object getSelection();
 
 	/**
 	 * Return true if the 'Apply' action enables on the condition that all
 	 * ridgets in the Details have <b>no</b> error markers.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @return true if the 'Apply' action enables on the condition that all
 	 *         ridgets in the Details have <b>no</b> error markers; otherwise
 	 *         false
 	 * 
 	 * @since 2.0
 	 */
 	boolean isApplyRequiresNoErrors();
 
 	/**
 	 * Return true if the 'Apply' action enables on the condition that all
 	 * ridgets in the Details are have <b>no</b> mandatory markers.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @return true if the 'Apply' action enables on the condition that all
 	 *         ridgets in the Details are have <b>no</b> mandatory markers;
 	 *         otherwise false
 	 * 
 	 * @since 2.0
 	 */
 	boolean isApplyRequiresNoMandatories();
 
 	/**
 	 * Return true, if a sucessfull 'Apply' triggers the 'New' action. If the
 	 * 'New' action is unavailable, the method returns false regardless of the
 	 * setting.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @return Return true, if a sucessfull 'Apply' triggers the 'New' action. *
 	 *         If the 'New' action is unavailable, the method returns false
 	 *         regardless of the setting.
 	 * 
 	 * @since 2.0
 	 */
 	boolean isApplyTriggersNew();
 
 	/**
 	 * When direct writing is enabled, changes in the details area will be
 	 * immediately and automatically applied back to the model. When adding new
 	 * rows, these will be immediately added to the table. Since there is no
 	 * need to apply, the 'Apply' action will not be shown.
 	 * <p>
 	 * The default setting for direct writing is false.
 	 * 
 	 * @return true if 'directWriting' is enabled; otherwise false
 	 * 
 	 * @since 1.2
 	 */
 	boolean isDirectWriting();
 
 	/**
 	 * When set to true, mandatory and error markers in the details area will
 	 * <b>initially</b> not be shown on new and suggested entries (i.e. when the
 	 * user invokes the 'New' action or the entry is suggested via the
 	 * {@link #suggestNewEntry()} methods). These markers will be displayed once
 	 * the user modifies at least one value in the details area.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @return true if this option is enabled, false otherwise
 	 * @since 3.0
 	 */
 	boolean isHideMandatoryAndErrorMarkersOnNewEntries();
 
 	/**
 	 * When set to true, the 'Remove' action can be used to abort editing of a
 	 * new entry. When aborting, the entry selected before pressing New will be
 	 * selected. If the 'Remove' action is unavailable, the method returns false
 	 * regardless of the setting.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @return true if the Remove action can be used to aborting editing of a
 	 *         new entry, false otherwise.
 	 * 
 	 * @since 3.0
 	 */
 	boolean isRemoveCancelsNew();
 
 	/**
 	 * Return true, if a sucessfull 'Remove' triggers the 'New' action. If the
 	 * 'Remove' action is unavailable, the method returns false regardless of
 	 * the setting.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @return Return true, if a sucessfull 'Remove' triggers the 'New' action.
 	 *         * If the 'Remove' action is unavailable, the method returns false
 	 *         regardless of the setting.
 	 * 
 	 * @since 3.0
 	 */
 	boolean isRemoveTriggersNew();
 
 	/**
 	 * When set to true, the 'Apply' action will only enable when all ridgets in
 	 * the Details area have <b>no</b> error markers.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @param requiresNoErrors
 	 *            The new setting for this option.
 	 * 
 	 * @since 1.2
 	 */
 	void setApplyRequiresNoErrors(boolean requiresNoErrors);
 
 	/**
 	 * When set to true, the 'Apply' action will only enable when all ridgets in
 	 * the Details area have <b>no</b> mandatory markers.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @param requiresNoMandatories
 	 *            The new setting for this option.
 	 * 
 	 * @since 2.0
 	 */
 	void setApplyRequiresNoMandatories(boolean requiresNoMandatories);
 
 	/**
 	 * When set to true, a sucessfull 'Apply' will trigger a 'New'. This will
 	 * only happen if the 'New' action is available.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @param triggersNew
 	 *            The new settings for this option.
 	 * 
 	 * @since 2.0
 	 */
 	void setApplyTriggersNew(boolean triggersNew);
 
 	/**
 	 * Adjust the column widths of the ridget's table control according to the
 	 * information in the {@code widths} array.
 	 * <p>
 	 * When running on SWT: {@code widths} may only contain subclasses of
 	 * ColumnLayoutData. The following layout managers are supported:
 	 * TableLayout, TableColumnLayout, other. See ColumnUtils for implementation
 	 * details.
 	 * 
 	 * @param widths
 	 *            an Array with width information, one instance per column. The
 	 *            array may be null, in that case the available width is
 	 *            distributed equally to all columns.
 	 * 
 	 * @since 1.2
 	 */
 	void setColumnWidths(Object[] widths);
 
 	/**
 	 * Provide this ridget with an {@link IMasterDetailsDelegate} instance,
 	 * which will manage the content of details area.
 	 * 
 	 * @param delegate
 	 *            an {@link IMasterDetailsDelegate}; never null
 	 */
 	void setDelegate(IMasterDetailsDelegate delegate);
 
 	/**
 	 * When direct writing is enabled, changes in the details area will be
 	 * immediately and automatically applied back to the model. When adding new
 	 * rows, these will be immediately added to the table. Since there is no
 	 * need to apply, the 'Apply' action will not be shown.
 	 * <p>
 	 * The default setting for direct writing is false.
 	 * 
 	 * @param directWriting
 	 *            The new direct writing setting.
 	 * 
 	 * @since 1.2
 	 */
 	void setDirectWriting(boolean directWriting);
 
 	/**
 	 * When set to true, mandatory and error markers in the details area will
 	 * <b>initially</b> not be shown on new and suggested entries (i.e. when the
 	 * user invokes the 'New' action or the entry is suggested via the
 	 * {@link #suggestNewEntry()} methods). These markers will be displayed once
 	 * the user modifies at least one value in the details area.
 	 * <p>
 	 * The default setting for this option is false.
 	 * <p>
 	 * Notes:
 	 * <ul>
 	 * <li>when
 	 * {@link IMasterDetailsDelegate#isValidMaster(IMasterDetailsRidget)} fails
 	 * a special marker is added to all ridgets. This marker is always shown
 	 * regardless of the value of this option</li>
 	 * <li>consider enabling {@link ITextRidget}
 	 * {@link #setDirectWriting(boolean)} for text ridgets in the details area,
 	 * so that the markers are re-displayed after the first keystroke</li>
 	 * <li>when this options is enabled, the visual feedback for some markers
 	 * will be disabled. However the markers are never removed or readded from
 	 * the ridgets in the details area (to avoid ambiguity about the marker
 	 * state of those ridgets)</li>
 	 * </ul>
 	 * 
 	 * @since 3.0
 	 */
 	void setHideMandatoryAndErrorMarkersOnNewEntries(boolean hideMarkers);
 
 	/**
 	 * When set to true, the 'Remove' action can be used to abort editing of a
 	 * new entry. When aborting, the entry selected before pressing New will be
 	 * selected.
 	 * <p>
 	 * 
 	 * 
 	 * @param cancelsNew
 	 *            the new setting for this option
 	 * 
 	 * @since 3.0
 	 */
 	void setRemoveCancelsNew(boolean cancelsNew);
 
 	/**
 	 * When set to true, a sucessfull 'Remove' will trigger a 'New'. This will
 	 * only happen if the 'Remove' action is available.
 	 * <p>
 	 * The default setting for this option is false.
 	 * 
 	 * @param triggersNew
 	 *            The new settings for this option.
 	 * 
 	 * @since 3.0
 	 */
 	void setRemoveTriggersNew(boolean triggersNew);
 
 	/**
 	 * Set the new selection. This behaves exactly like an interactive selection
 	 * change (i.e. the user selecting another value).
 	 * 
 	 * @param newSelection
 	 *            the newly selected value of this ridget, or null to clear the
 	 *            selection
 	 */
 	void setSelection(final Object newSelection);
 
 	/**
 	 * Suggest a blank object &ndash; obtained from
 	 * {@link IMasterDetailsDelegate#createWorkingCopy()}) &ndash; as a 'new'
 	 * entry. It will be made editable in the details area. The details area
 	 * will <b>not</b> be considered dirty. The apply action will be
 	 * <b>disabled</b>, until the user edits the contents.
 	 * 
 	 * @since 3.0
 	 */
 	void suggestNewEntry();
 
 	/**
 	 * Suggest the given object as a 'new' entry. It will be made editable in
 	 * the details area. The details area will be considered dirty and the apply
 	 * action will be enabled (unless direct writing is enabled).
 	 * 
 	 * @param entry
 	 *            a non-null object. Note that when the user invokes the 'Apply'
 	 *            operation, the {@code entry} will be updated with the contents
 	 *            from the details area and then added into the master table of
 	 *            objects. For this reason you should use a <b>new</b>
 	 *            {@code entry} instance for each call to this method.
 	 * @since 2.0
 	 */
 	void suggestNewEntry(Object entry);
 
 	/**
 	 * Suggest the given object as a 'new' entry. It will be made editable in
 	 * the details area. The details area may or not be considered dirty,
 	 * depending on the {@code treatAsDirty} argument. Likewise, the apply
 	 * action may or may not be enabled depending on the {@code treatAsDirty}
 	 * argument.
 	 * <p>
 	 * Note: if direct writing is enabled, the details area / apply button will
 	 * be considered as not dirty regardless of the {@code treatAsDirty} value.
 	 * 
 	 * @param entry
 	 *            a non-null object. Note that when the user invokes the 'Apply'
 	 *            operation, the {@code entry} will be updated with the contents
 	 *            from the details area and then added into the master table of
 	 *            objects. For this reason you should use a <b>new</b>
 	 *            {@code entry} instance for each call to this method.
 	 * @param treatAsDirty
 	 *            true, to treat the details area as dirty and enable the apply
 	 *            button. False to treat the details area as initially
 	 *            unmodified and disable the apply button (until the content is
 	 *            modified).
 	 * 
 	 * @since 3.0
 	 */
 	void suggestNewEntry(Object entry, boolean treatAsDirty);
 
 	/**
 	 * Toggles the 'Apply' action state, according to the presence of changes in
 	 * the details area. It will use delegate.isChanged(...) to compare the data
 	 * in the details area with the original and enable the 'Apply' action if it
 	 * differs.
 	 * <p>
 	 * Use this method when the data in the details has been modified without
 	 * sending a notification to the IMasterDetailsRidget (for example, in a
 	 * separate dialog that was opened).
 	 * 
 	 * @since 2.0
 	 */
 	void updateApplyButton();
 
 	/**
 	 * Set to false to handle the enabled state of the remove ridget manually
 	 * from outside. Otherwise the ridget´s enabled state is controlled by the
 	 * {@link IMasterDetailsRidget}
 	 * 
 	 * @param autoEnableRemove
 	 *            false for manual control over the enabled state of remove
 	 *            ridget
 	 */
 	void setAutoEnableRemove(final boolean autoEnableRemove);
 }
