 /*******************************************************************************
  * Copyright (c) 2013 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.e4.tools.event.spy.ui;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.e4.tools.event.spy.model.CapturedEventFilter;
 import org.eclipse.e4.tools.event.spy.model.ItemToFilter;
 import org.eclipse.e4.tools.event.spy.model.Operator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolTip;
 
 public class CapturedEventFilters {
 	private final static String NOT_SELECTED_VALUE = "-- expected value --";
 
	private Composite control;
 
 	private Text valueText;
 
 	private Combo itemToFilterCombo;
 
 	private Combo operatorCombo;
 
 	private ToolTip validationErrorToolTip;
 
 	private List filters;
 
 	private Map<Integer, CapturedEventFilter> rawFilters;
 
 
 	/* Layout scheme:
 	 *
 	 * +-- control --------------------------------------------+
 	 * | +-- New filter group -------------------------------+ |
 	 * | |                                                   | |
 	 * | | Capture event when|combo|combo|text|add filter    | |
 	 * | |                                                   | |
 	 * | +---------------------------------------------------+ |
 	 * | +-- Defined filter group ---------------------------+ |
 	 * | |                                                   | |
 	 * | | List   |   +-- composite -----------------+       | |
 	 * | |        |   | remove selected | remove all |       | |
 	 * | |        |   +------------------------------+       | |
 	 * | |                                                   | |
 	 * | +---------------------------------------------------+ |
 	 * +-------------------------------------------------------+
 	 *
 	 * */
 
 	//TODO: Fix layout data for groups
 	public CapturedEventFilters(Composite outer) {
 		control = new Composite(outer, SWT.NONE);
 		RowLayout layout = new RowLayout(SWT.VERTICAL);
 		layout.marginLeft = 0;
 		layout.fill = true;
 		control.setLayout(layout);
 
 		createNewFilterGroup(control);
 
 		createDefinedFiltersGroup(control);
 	}
 
 	private void createNewFilterGroup(Composite parent) {
 		Group newFilterGroup = new Group(parent, SWT.NONE);
 		newFilterGroup.setText("New filter:");
 		newFilterGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
 
 		Label label = new Label(newFilterGroup, SWT.CENTER);
 		label.setText("Capture event when:");
 
 		itemToFilterCombo = new Combo(newFilterGroup, SWT.READ_ONLY);
 		for (ItemToFilter item: ItemToFilter.values()) {
 			itemToFilterCombo.add(item.toString());
 		}
 		itemToFilterCombo.select(0);
 
 		operatorCombo = new Combo(newFilterGroup, SWT.READ_ONLY);
 		for (Operator operator: Operator.values()) {
 			operatorCombo.add(operator.toString());
 		}
 		operatorCombo.select(0);
 
 		valueText = new Text(newFilterGroup, SWT.BORDER);
 		valueText.setLayoutData(new RowData(130, SWT.DEFAULT));
 		valueText.setText(NOT_SELECTED_VALUE);
 		valueText.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
				if (valueText.getText().trim().isEmpty()) {
 					valueText.setText(NOT_SELECTED_VALUE);
 				}
 			}
 			public void focusGained(FocusEvent e) {
 				if (NOT_SELECTED_VALUE.equals(valueText.getText())) {
 					valueText.setText("");
 				}
 			}
 		});
 
 		Link link = new Link(newFilterGroup, SWT.NONE);
 		link.setText("<a>Add filter</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				addNewFilter();
 			}
 		});
 	}
 
 	private void createDefinedFiltersGroup(Composite parent) {
 		Group definedFiltersGroup = new Group(parent, SWT.NONE);
 		definedFiltersGroup.setText("Defined filters (relation between filters is AND):");
 		definedFiltersGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
 
 		filters = new List(definedFiltersGroup, SWT.BORDER);
 		filters.setLayoutData(new RowData(403, 84));
 
 
 		Composite composite = new Composite(definedFiltersGroup, SWT.NONE);
 		composite.setLayout(new RowLayout(SWT.VERTICAL));
 
 		Link link = new Link(composite, SWT.NONE);
 		link.setText("<a>Remove selected</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				removeFilterAt(filters.getSelectionIndex());
 			}
 		});
 
 		link = new Link(composite, SWT.NONE);
 		link.setText("<a>Remove all</a>");
 		link.addListener (SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				removeAllFilters();
 			}
 		});
 	}
 
 	public Control getControl() {
 		return control;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Collection<CapturedEventFilter> getFilters() {
 		return rawFilters == null || rawFilters.isEmpty()? Collections.EMPTY_LIST: rawFilters.values();
 	}
 
 	public boolean hasFilters() {
 		return rawFilters != null && !rawFilters.isEmpty();
 	}
 
 	public int getFiltersCount() {
 		return rawFilters == null? 0: rawFilters.size();
 	}
 
 	private void addNewFilter() {
 		ItemToFilter selectedItemToFilter =
 			ItemToFilter.toItem(itemToFilterCombo.getItem(itemToFilterCombo.getSelectionIndex()));
 		if (ItemToFilter.NotSelected.equals(selectedItemToFilter)) {
 			getTooltip().setText(String.format("%s is not selected", getFieldName(ItemToFilter.NotSelected)));
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		Operator selectedOperator = Operator.toOperator(operatorCombo.getItem(operatorCombo.getSelectionIndex()));
 		if (Operator.NotSelected.equals(selectedOperator)) {
 			getTooltip().setText(String.format("%s is not selected", getFieldName(Operator.NotSelected)));
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		String value = valueText.getText();
		if (value.isEmpty() || value.equals(NOT_SELECTED_VALUE)) {
 			getTooltip().setText(String.format("%s is empty", getFieldName(NOT_SELECTED_VALUE)));
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		CapturedEventFilter eventFilter = null;
 		try {
 			eventFilter = new CapturedEventFilter(selectedItemToFilter, selectedOperator, value);
 		} catch(IllegalArgumentException exc) {
 			getTooltip().setText(exc.getMessage());
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		if (rawFilters == null) {
 			rawFilters = new HashMap<Integer, CapturedEventFilter>();
 		}
 
 		String filterAsString = eventFilter.toString();
 		if (rawFilters.containsKey(filterAsString.hashCode())) {
 			getTooltip().setText(String.format("Filter has been already added: %s", filterAsString));
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		filters.add(filterAsString);
 		rawFilters.put(filterAsString.hashCode(), eventFilter);
 
 		itemToFilterCombo.select(0);
 		operatorCombo.select(0);
 		valueText.setText(NOT_SELECTED_VALUE);
 	}
 
 	private ToolTip getTooltip() {
 		if (validationErrorToolTip == null) {
 			validationErrorToolTip = new ToolTip(Display.getCurrent().getActiveShell(), SWT.BALLOON | SWT.ICON_WARNING);
 		}
 		return validationErrorToolTip;
 	}
 
 	private void removeFilterAt(int index) {
 		if (index < 0) {
 			getTooltip().setText("Filter to remove is not selected");
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		String filterAsString = filters.getItem(index);
 		filters.remove(index);
 		rawFilters.remove(filterAsString.hashCode());
 	}
 
 	private void removeAllFilters() {
 		if (rawFilters == null || rawFilters.isEmpty()) {
 			getTooltip().setText("Filter list is empty");
 			getTooltip().setVisible(true);
 			return;
 		}
 
 		filters.removeAll();
 		rawFilters.clear();
 	}
 
 	private String getFieldName(Object notSelectedName) {
 		String fieldName = notSelectedName.toString().replaceAll("-", "").trim();
 		return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
 	}
 }
