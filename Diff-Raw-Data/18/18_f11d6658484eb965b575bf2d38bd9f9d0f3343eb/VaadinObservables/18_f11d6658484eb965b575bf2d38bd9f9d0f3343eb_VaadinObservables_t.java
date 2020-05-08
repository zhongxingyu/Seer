 /*******************************************************************************
  * Copyright (c) 2012 by committers of lunifera.org
 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Original source based on org.eclipse.jface.databinding.swt.SWTObservables (EPL)
  * 
  * Contributor:
  * 		Florian Pirchner - porting swt databinding to support vaadin
  * 
  *******************************************************************************/
 package org.lunifera.runtime.web.vaadin.databinding;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.eclipse.core.databinding.observable.Realm;
 
 import com.vaadin.data.Buffered;
 import com.vaadin.data.BufferedValidatable;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Validatable;
 import com.vaadin.server.Scrollable;
 import com.vaadin.server.Sizeable;
 import com.vaadin.ui.AbstractEmbedded;
 import com.vaadin.ui.AbstractField;
 import com.vaadin.ui.AbstractMedia;
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.AbstractSplitPanel;
 import com.vaadin.ui.AbstractTextField;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Component.Focusable;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout.MarginHandler;
 import com.vaadin.ui.Layout.SpacingHandler;
 import com.vaadin.ui.Link;
 import com.vaadin.ui.ListSelect;
 import com.vaadin.ui.MenuBar;
 import com.vaadin.ui.OptionGroup;
 import com.vaadin.ui.PopupDateField;
 import com.vaadin.ui.PopupView;
 import com.vaadin.ui.ProgressIndicator;
 import com.vaadin.ui.RichTextArea;
 import com.vaadin.ui.SingleComponentContainer;
 import com.vaadin.ui.Slider;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.Tree;
 import com.vaadin.ui.TreeTable;
 import com.vaadin.ui.TwinColSelect;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.Upload;
 import com.vaadin.ui.Video;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.components.colorpicker.ColorPickerGradient;
 import com.vaadin.ui.components.colorpicker.ColorPickerGrid;
 import com.vaadin.ui.components.colorpicker.ColorPickerHistory;
 import com.vaadin.ui.components.colorpicker.ColorPickerSelect;
 import com.vaadin.ui.components.colorpicker.ColorSelector;
 
 /**
  * A factory for creating observables for Vaadin Components
  */
 public class VaadinObservables {
 
 	private static java.util.List<UIRealm> realms = new ArrayList<UIRealm>();
 
 	/**
 	 * Returns the realm representing the UI thread for the given display.
 	 * 
 	 * @param ui
 	 * @return the realm representing the UI thread for the given display
 	 */
 	public static Realm getRealm(final UI ui) {
 		synchronized (realms) {
 			for (Iterator<UIRealm> it = realms.iterator(); it.hasNext();) {
 				UIRealm displayRealm = it.next();
 				if (displayRealm.ui == ui) {
 					return displayRealm;
 				}
 			}
 			UIRealm result = new UIRealm(ui);
 			realms.add(result);
 			return result;
 		}
 	}
 
 	/**
	 * Activates the realm for the current thread.
	 * 
	 * @param ui
	 */
	public static void activateRealm(final UI ui){
		UIRealm uiRealm =	(UIRealm) getRealm(ui);
		uiRealm.makeDefault();
	}

	/**
 	 * Returns the UI of the widget or the current UI.
 	 * 
 	 * @param widget
 	 * @return
 	 */
 	public static UI getUI(Component widget) {
 		UI ui = widget != null ? widget.getUI() : null;
 		return ui != null ? ui : UI.getCurrent();
 	}
 
 	/**
 	 * Returns the property type of the given field.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static Class<?> getPropertyType(Field<?> field) {
 		return getProperty(field).getType();
 	}
 
 	/**
 	 * Returns the property of the given field.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static Property<?> getProperty(Field<?> field) {
 		Property<?> property = field.getPropertyDataSource() != null ? field
 				.getPropertyDataSource() : field;
 		return property;
 	}
 
 	/**
 	 * Returns an observable value tracking the propertyset of the given item
 	 * notifier.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeItemPropertySetValue(
 			Item.PropertySetChangeNotifier notifier) {
 		return VaadinProperties.itemPropertysetValue().observe(notifier);
 	}
 
 	/**
 	 * Returns an observable value tracking the propertyset of the given item
 	 * notifier.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeItemPropertySetInfoValue(
 			Item.PropertySetChangeNotifier notifier) {
 		return VaadinProperties.itemPropertysetInfoValue().observe(notifier);
 	}
 
 	/**
 	 * Returns an observable value tracking the item set of the given item
 	 * notifier.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeContainerItemSetValue(
 			Container.ItemSetChangeNotifier notifier) {
 		return VaadinProperties.containerItemsetValue().observe(notifier);
 	}
 
 	/**
 	 * Returns an observable value tracking the container of the given viewer.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeContainerDatasource(
 			Container.Viewer viewer) {
 		return VaadinProperties.containerDatasource().observe(viewer);
 	}
 
 	/**
 	 * Returns an observable value tracking the container of the given viewer.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeItemDatasource(
 			Item.Viewer viewer) {
 		return VaadinProperties.itemDatasource().observe(viewer);
 	}
 
 	/**
 	 * Returns an observable value tracking the container of the given viewer.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeItemDatasource(
 			Property.Viewer viewer) {
 		return VaadinProperties.propertyDatasource().observe(viewer);
 	}
 
 	/**
 	 * Returns an observable value tracking the container of the given viewer.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeValue(
 			Property.ValueChangeNotifier notifier) {
 		return VaadinProperties.propertyValue().observe(notifier);
 	}
 
 	/**
 	 * Returns an observable value tracking the readonly state of the given
 	 * notifier.
 	 * 
 	 * @param notifier
 	 * @return
 	 */
 	public static IVaadinModelObservableValue observeReadonly(
 			Property.ReadOnlyStatusChangeNotifier notifier) {
 		return VaadinProperties.readonly().observe(notifier);
 	}
 
 	/**
 	 * Returns an observable value tracking the focus state of the given
 	 * focusable. Note that isFocus() can not be returned. You can only use
 	 * setFocus().
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeFocus(
 			Focusable focusable) {
 		return VaadinProperties.focus().observe(focusable);
 	}
 
 	/**
 	 * Returns an observable value tracking the caption of the given component.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeCaption(
 			Component component) {
 		return VaadinProperties.accessor(Component.class, "caption").observe(
 				component);
 	}
 
 	/**
 	 * Returns an observable value tracking the enabled state of the given
 	 * widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeEnabled(
 			Component component) {
 		return VaadinProperties.accessor(Component.class, "enabled").observe(
 				component);
 	}
 
 	/**
 	 * Returns an observable value tracking the enabled state of the given
 	 * widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeDescription(
 			Component component) {
 		return VaadinProperties.description().observe(component);
 	}
 
 	/**
 	 * Returns an observable value tracking the icon of the given widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeIcon(
 			Component component) {
 		return VaadinProperties.accessor(Component.class, "icon").observe(
 				component);
 	}
 
 	/**
 	 * Returns an observable value tracking the primary style name of the given
 	 * widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observePrimaryStyleName(
 			Component component) {
 		return VaadinProperties.accessor(Component.class, "primaryStyleName")
 				.observe(component);
 	}
 
 	/**
 	 * Returns an observable value tracking the styleName of the given widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeStyleName(
 			Component component) {
 		return VaadinProperties.accessor(Component.class, "styleName").observe(
 				component);
 	}
 
 	/**
 	 * Returns an observable value tracking the visible state of the given
 	 * widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeVisible(
 			Component component) {
 		return VaadinProperties.accessor(Component.class, "visible").observe(
 				component);
 	}
 
 	/**
 	 * Returns an observable value tracking the required state of the given
 	 * widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeRequired(Field<?> field) {
 		return VaadinProperties.accessor(Field.class, "required")
 				.observe(field);
 	}
 
 	/**
 	 * Returns an observable value tracking the "required error message" of the
 	 * given widget.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public static IVaadinComponentObservableValue observeRequiredError(
 			Field<?> field) {
 		return VaadinProperties.accessor(Field.class, "requiredError").observe(
 				field);
 	}
 
 	public static IVaadinComponentObservableValue observeAlternateText(
 			AbstractEmbedded component) {
 		return VaadinProperties.accessor(AbstractEmbedded.class,
 				"alternateText").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeSource(
 			AbstractEmbedded component) {
 		return VaadinProperties.accessor(AbstractEmbedded.class, "source")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeItemCaptionMode(
 			AbstractSelect component) {
 		return VaadinProperties.accessor(AbstractSelect.class,
 				"itemCaptionMode").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeItemCaptionPropertyId(
 			AbstractSelect component) {
 		return VaadinProperties.accessor(AbstractSelect.class,
 				"itemCaptionPropertyId").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeItemIconPropertyId(
 			AbstractSelect component) {
 		return VaadinProperties.accessor(AbstractSelect.class,
 				"itemIconPropertyId").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMultiSelect(
 			AbstractSelect component) {
 		return VaadinProperties.accessor(AbstractSelect.class, "multiSelect")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeNewItemHandler(
 			AbstractSelect component) {
 		return VaadinProperties
 				.accessor(AbstractSelect.class, "newItemHandler").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeNewItemsAllowed(
 			AbstractSelect component) {
 		return VaadinProperties.accessor(AbstractSelect.class,
 				"newItemsAllowed").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeNullSelectionAllowed(
 			AbstractSelect component) {
 		return VaadinProperties.accessor(AbstractSelect.class,
 				"nullSelectionAllowed").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeBuffered(
 			Buffered component) {
 		return VaadinProperties.accessor(Buffered.class, "buffered").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeInvalidCommitted(
 			BufferedValidatable component) {
 		return VaadinProperties.accessor(Buffered.class, "invalidCommitted")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeInvalidCommitted(
 			ComboBox component) {
 		return VaadinProperties.accessor(ComboBox.class, "pageLength").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeScrollToSelectedItem(
 			ComboBox component) {
 		return VaadinProperties
 				.accessor(ComboBox.class, "scrollToSelectedItem").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeTextInputAllowed(
 			ComboBox component) {
 		return VaadinProperties.accessor(ComboBox.class, "textInputAllowed")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeTemplateContents(
 			CustomLayout component) {
 		return VaadinProperties
 				.accessor(CustomLayout.class, "templateContents").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeTemplateNameProperty(
 			CustomLayout component) {
 		return VaadinProperties.accessor(CustomLayout.class,
 				"templateNameProperty").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeDateFormat(
 			DateField component) {
 		return VaadinProperties.accessor(DateField.class, "dateFormat")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeLenient(
 			DateField component) {
 		return VaadinProperties.accessor(DateField.class, "lenient").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeParseErrorMessage(
 			DateField component) {
 		return VaadinProperties.accessor(DateField.class, "parseErrorMessage")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeResolution(
 			DateField component) {
 		return VaadinProperties.accessor(DateField.class, "resolution")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeShowISOWeekNumbers(
 			DateField component) {
 		return VaadinProperties.accessor(DateField.class, "showISOWeekNumbers")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeTextFieldEnabled(
 			DateField component) {
 		return VaadinProperties.accessor(DateField.class, "textFieldEnabled")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeTimeZone(
 			DateField component) {
 		return VaadinProperties.accessor(DateField.class, "timeZone").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeValidationVisible(
 			AbstractField<?> component) {
 		return VaadinProperties.accessor(AbstractField.class,
 				"validationVisible").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumns(
 			GridLayout component) {
 		return VaadinProperties.accessor(GridLayout.class, "columns").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeCursorX(
 			GridLayout component) {
 		return VaadinProperties.accessor(GridLayout.class, "cursorX").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeCursorY(
 			GridLayout component) {
 		return VaadinProperties.accessor(GridLayout.class, "cursorY").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeRows(
 			GridLayout component) {
 		return VaadinProperties.accessor(GridLayout.class, "rows").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeRows(
 			ListSelect component) {
 		return VaadinProperties.accessor(ListSelect.class, "rows").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeRows(
 			MarginHandler component) {
 		return VaadinProperties.accessor(MarginHandler.class, "marginInfo")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeRows(
 			OptionGroup component) {
 		return VaadinProperties.accessor(OptionGroup.class,
 				"htmlContentAllowed").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeRows(
 			PopupDateField component) {
 		return VaadinProperties.accessor(PopupDateField.class, "inputPrompt")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeTextFieldEnabled(
 			PopupDateField component) {
 		return VaadinProperties.accessor(PopupDateField.class,
 				"textFieldEnabled").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeHeight(
 			Sizeable component) {
 		return VaadinProperties.height().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeWidth(
 			Sizeable component) {
 		return VaadinProperties.width().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeSpacing(
 			SpacingHandler component) {
 		return VaadinProperties.accessor(SpacingHandler.class, "spacing")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeFirstComponent(
 			AbstractSplitPanel component) {
 		return VaadinProperties.accessor(AbstractSplitPanel.class,
 				"firstComponent").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeLocked(
 			AbstractSplitPanel component) {
 		return VaadinProperties.accessor(AbstractSplitPanel.class, "locked")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMaxSplitPosition(
 			AbstractSplitPanel component) {
 		return VaadinProperties.maxSplitPosition().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMaxSplitPositionUnit(
 			AbstractSplitPanel component) {
 		return VaadinProperties.maxSplitPositionUnit().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMinSplitPosition(
 			AbstractSplitPanel component) {
 		return VaadinProperties.minSplitPosition().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMinSplitPositionUnit(
 			AbstractSplitPanel component) {
 		return VaadinProperties.minSplitPositionUnit().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeSplitPosition(
 			AbstractSplitPanel component) {
 		return VaadinProperties.splitPosition().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeSplitPositionUnit(
 			AbstractSplitPanel component) {
 		return VaadinProperties.splitPositionUnit().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeSeconComponent(
 			AbstractSplitPanel component) {
 		return VaadinProperties.accessor(AbstractSplitPanel.class,
 				"secondComponent").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeCache(Table component) {
 		return VaadinProperties.accessor(Table.class, "cache").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeCellStyleGenerator(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "cellStyleGenerator")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumnAlignments(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "columnAlignments")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumnCollapsingAllowed(
 			Table component) {
 		return VaadinProperties
 				.accessor(Table.class, "columnCollapsingAllowed").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumnHeaderMode(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "columnHeaderMode")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumnHeaders(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "columnHeaders").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumnIcons(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "columnIcons").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumnReorderingAllowed(
 			Table component) {
 		return VaadinProperties
 				.accessor(Table.class, "columnReorderingAllowed").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeCurrentPageFirstItemId(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "currentPageFirstItemId")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeCurrentPageFirstItemIndex(
 			Table component) {
 		return VaadinProperties.accessor(Table.class,
 				"currentPageFirstItemIndex").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeDropHandler(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "dropHandler").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeDragMode(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "dragMode").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeEditable(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "editable").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeTableFieldFactory(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "tableFieldFactory")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeFooterVisible(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "footerVisible").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeItemDescriptionGenerator(
 			Table component) {
 		return VaadinProperties.accessor(Table.class,
 				"itemDescriptionGenerator").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMultiSelectMode(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "multiSelectMode")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeRowGenerator(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "rowGenerator").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeRowHeaderMode(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "rowHeaderMode").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeSelectable(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "selectable").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeSortAscending(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "sortAscending").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeSortContainerPropertyId(
 			Table component) {
 		return VaadinProperties
 				.accessor(Table.class, "sortContainerPropertyId").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeSortEnabled(
 			Table component) {
 		return VaadinProperties.accessor(Table.class, "sortEnabled").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeVisible(Table component) {
 		return VaadinProperties.accessor(Table.class, "visible").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeSelectedTab(
 			TabSheet component) {
 		return VaadinProperties.selectedTab().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeTabIndex(
 			TabSheet component) {
 		return VaadinProperties.accessor(Table.class, "tabIndex").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeRows(TextArea component) {
 		return VaadinProperties.accessor(TextArea.class, "rows").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeWordWrap(
 			TextArea component) {
 		return VaadinProperties.accessor(TextArea.class, "wordWrap").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeTextChangeEventMode(
 			AbstractTextField component) {
 		return VaadinProperties.accessor(AbstractTextField.class,
 				"textChangeEventMode").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeTextChangeTimeout(
 			AbstractTextField component) {
 		return VaadinProperties.accessor(AbstractTextField.class,
 				"textChangeTimeout").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColumns(
 			AbstractTextField component) {
 		return VaadinProperties.accessor(AbstractTextField.class, "columns")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeCursorPosition(
 			AbstractTextField component) {
 		return VaadinProperties.accessor(AbstractTextField.class,
 				"cursorPosition").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeInputPrompt(
 			AbstractTextField component) {
 		return VaadinProperties
 				.accessor(AbstractTextField.class, "inputPrompt").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeMaxLength(
 			AbstractTextField component) {
 		return VaadinProperties.accessor(AbstractTextField.class, "maxLength")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeNullRepresentation(
 			AbstractTextField component) {
 		return VaadinProperties.accessor(AbstractTextField.class,
 				"nullRepresentation").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeNullSettingAllowed(
 			AbstractTextField component) {
 		return VaadinProperties.accessor(AbstractTextField.class,
 				"nullSettingAllowed").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeDragMode(Tree component) {
 		return VaadinProperties.accessor(Tree.class, "dragMode").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeDropHandler(
 			Tree component) {
 		return VaadinProperties.accessor(Tree.class, "dropHandler").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeItemDescriptionGenerator(
 			Tree component) {
 		return VaadinProperties
 				.accessor(Tree.class, "itemDescriptionGenerator").observe(
 						component);
 	}
 
 	public static IVaadinComponentObservableValue observeItemStyleGenerator(
 			Tree component) {
 		return VaadinProperties.accessor(Tree.class, "itemStyleGenerator")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMultiSelectMode(
 			Tree component) {
 		return VaadinProperties.accessor(Tree.class, "multiSelectMode")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeSelectable(
 			Tree component) {
 		return VaadinProperties.accessor(Tree.class, "selectable").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeSelectable(
 			TreeTable component) {
 		return VaadinProperties.accessor(TreeTable.class, "animationsEnabled")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeHierachyColumnId(
 			TreeTable component) {
 		return VaadinProperties.accessor(TreeTable.class, "hierachyColumnId")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeRows(
 			TwinColSelect component) {
 		return VaadinProperties.accessor(TwinColSelect.class, "rows").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeLeftColumnCaption(
 			TwinColSelect component) {
 		return VaadinProperties.accessor(TwinColSelect.class,
 				"leftColumnCaption").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeRightColumnCaption(
 			TwinColSelect component) {
 		return VaadinProperties.accessor(TwinColSelect.class,
 				"rightColumnCaption").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeInvalidAllowed(
 			Validatable component) {
 		return VaadinProperties.accessor(Validatable.class, "invalidAllowed")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeIndeterminate(
 			ProgressIndicator component) {
 		return VaadinProperties.accessor(ProgressIndicator.class,
 				"indeterminate").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observePollingInterval(
 			ProgressIndicator component) {
 		return VaadinProperties.accessor(ProgressIndicator.class,
 				"pollingInterval").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeNullRepresentation(
 			RichTextArea component) {
 		return VaadinProperties.accessor(RichTextArea.class,
 				"nullRepresentation").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeNullSettingAllowed(
 			RichTextArea component) {
 		return VaadinProperties.accessor(RichTextArea.class,
 				"nullSettingAllowed").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMax(Slider component) {
 		return VaadinProperties.accessor(Slider.class, "max")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMin(Slider component) {
 		return VaadinProperties.accessor(Slider.class, "min")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeOrientation(
 			Slider component) {
 		return VaadinProperties.accessor(Slider.class, "orientation").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeResolution(
 			Slider component) {
 		return VaadinProperties.accessor(Slider.class, "resolution").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeAltText(
 			AbstractMedia component) {
 		return VaadinProperties.accessor(AbstractMedia.class, "altText")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeAutoplay(
 			AbstractMedia component) {
 		return VaadinProperties.accessor(AbstractMedia.class, "autoplay")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeHtmlContentAllowed(
 			AbstractMedia component) {
 		return VaadinProperties.accessor(AbstractMedia.class,
 				"htmlContentAllowed").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMuted(
 			AbstractMedia component) {
 		return VaadinProperties.accessor(AbstractMedia.class, "muted").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeShowControls(
 			AbstractMedia component) {
 		return VaadinProperties.accessor(AbstractMedia.class, "showControls")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observePoster(Video component) {
 		return VaadinProperties.accessor(Video.class, "poster").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeScrollLeft(
 			Scrollable component) {
 		return VaadinProperties.accessor(Scrollable.class, "scrollLeft")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeScrollTop(
 			Scrollable component) {
 		return VaadinProperties.accessor(Scrollable.class, "scrollTop")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeClosable(
 			Window component) {
 		return VaadinProperties.accessor(Window.class, "closable").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeScrollTop(
 			Window component) {
 		return VaadinProperties.accessor(Window.class, "scrollTop").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeDraggable(
 			Window component) {
 		return VaadinProperties.accessor(Window.class, "draggable").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeModal(Window component) {
 		return VaadinProperties.accessor(Window.class, "modal").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeResizeable(
 			Window component) {
 		return VaadinProperties.accessor(Window.class, "resizable").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeResizeLazy(
 			Window component) {
 		return VaadinProperties.accessor(Window.class, "resizeLazy").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observePositionX(
 			Window component) {
 		return VaadinProperties.accessor(Window.class, "positionX").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observePositionY(
 			Window component) {
 		return VaadinProperties.accessor(Window.class, "positionY").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeColor(
 			ColorSelector component) {
 		return VaadinProperties.accessor(ColorSelector.class, "color").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeContent(
 			SingleComponentContainer component) {
 		return VaadinProperties.accessor(SingleComponentContainer.class,
 				"content").observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeLastHeartbeatTimestamp(
 			UI component) {
 		return VaadinProperties.accessor(UI.class, "lastHeartbeatTimestamp")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeNavigator(UI component) {
 		return VaadinProperties.accessor(UI.class, "navigator").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeResizeLazy(UI component) {
 		return VaadinProperties.accessor(UI.class, "resizeLazy").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeScrollLeft(UI component) {
 		return VaadinProperties.accessor(UI.class, "scrollLeft").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeScrollTop(UI component) {
 		return VaadinProperties.accessor(UI.class, "scrollTop").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeSession(UI component) {
 		return VaadinProperties.accessor(UI.class, "session")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeDisableOnClick(
 			Button component) {
 		return VaadinProperties.accessor(Button.class, "disableOnClick")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeHtmlContentAllowed(
 			Button component) {
 		return VaadinProperties.accessor(Button.class, "htmlContentAllowed")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColor(
 			ColorPickerGradient component) {
 		return VaadinProperties.color_ColorPickerGradient().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColor(
 			ColorPickerGrid component) {
 		return VaadinProperties.color_ColorPickerGrid().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColor(
 			ColorPickerHistory component) {
 		return VaadinProperties.color_ColorPickerHistory().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeColor(
 			ColorPickerSelect component) {
 		return VaadinProperties.color_ColorPickerSelect().observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeAlternateText(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "alternateText")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeArchive(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "archive").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeClassId(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "classId").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeCodebase(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "codebase").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeCodetype(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "codetype").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeMimeType(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "mimeType").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeSource(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "source").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeStandby(
 			Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "standby").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeType(Embedded component) {
 		return VaadinProperties.accessor(Embedded.class, "type").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeContentMode(
 			Label component) {
 		return VaadinProperties.accessor(Label.class, "contentMode").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeConverter(
 			Label component) {
 		return VaadinProperties.accessor(Label.class, "converter").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeResource(Link component) {
 		return VaadinProperties.accessor(Link.class, "resource").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeTargetBorder(
 			Link component) {
 		return VaadinProperties.accessor(Link.class, "targetBorder").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeTargetHeight(
 			Link component) {
 		return VaadinProperties.accessor(Link.class, "targetHeight").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeTargetName(
 			Link component) {
 		return VaadinProperties.accessor(Link.class, "targetName").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeTargetWidth(
 			Link component) {
 		return VaadinProperties.accessor(Link.class, "targetWidth").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeAutoOpen(
 			MenuBar component) {
 		return VaadinProperties.accessor(MenuBar.class, "autoOpen").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeHtmlContentAllowed(
 			MenuBar component) {
 		return VaadinProperties.accessor(MenuBar.class, "htmlContentAllowed")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeMoreMenuItem(
 			MenuBar component) {
 		return VaadinProperties.accessor(MenuBar.class, "moreMenuItem")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeContent(
 			PopupView component) {
 		return VaadinProperties.accessor(PopupView.class, "content").observe(
 				component);
 	}
 
 	public static IVaadinComponentObservableValue observeHideOnMouseOut(
 			PopupView component) {
 		return VaadinProperties.accessor(PopupView.class, "hideOnMouseOut")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observePopupVisible(
 			PopupView component) {
 		return VaadinProperties.accessor(PopupView.class, "popupVisible")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeButtonCaption(
 			Upload component) {
 		return VaadinProperties.accessor(PopupView.class, "buttonCaption")
 				.observe(component);
 	}
 
 	public static IVaadinComponentObservableValue observeReceiver(
 			Upload component) {
 		return VaadinProperties.accessor(PopupView.class, "receiver").observe(
 				component);
 	}
 
 	private static class UIRealm extends Realm {
 		private final UI ui;
 
 		/**
 		 * @param ui
 		 */
 		private UIRealm(UI ui) {
 			this.ui = ui;
 			setDefault(this);
 		}
 
 		@Override
 		public boolean isCurrent() {
 			return UI.getCurrent() == ui;
 		}
 
		/**
		 * Makes the realm to the thread default.
		 */
		public void makeDefault() {
			setDefault(this);
		}

 		@Override
 		public void asyncExec(final Runnable runnable) {
 			// Runnable safeRunnable = new Runnable() {
 			// public void run() {
 			// safeRun(runnable);
 			// }
 			// };
 			throw new UnsupportedOperationException("Not a valid call!");
 		}
 
 		@Override
 		public void timerExec(int milliseconds, final Runnable runnable) {
 			throw new UnsupportedOperationException("Not a valid call!");
 		}
 
 		@Override
 		public int hashCode() {
 			return (ui == null) ? 0 : ui.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			final UIRealm other = (UIRealm) obj;
 			if (ui == null) {
 				if (other.ui != null)
 					return false;
 			} else if (!ui.equals(other.ui))
 				return false;
 			return true;
 		}
 	}
 
 }
