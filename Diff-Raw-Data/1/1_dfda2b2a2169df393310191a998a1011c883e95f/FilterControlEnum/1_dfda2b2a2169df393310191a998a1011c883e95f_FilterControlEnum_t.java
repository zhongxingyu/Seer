 /*******************************************************************************
  * Copyright (c) 2014 Formal Mind GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.search.filter.ui;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.rmf.reqif10.AttributeDefinitionEnumeration;
 import org.eclipse.rmf.reqif10.EnumValue;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.EnumSelector;
 import org.eclipse.rmf.reqif10.search.filter.EnumFilter;
 import org.eclipse.rmf.reqif10.search.filter.IFilter;
 import org.eclipse.rmf.reqif10.search.filter.IFilter.Operator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Used for plain text and XHTML
  * 
  * @author jastram
  */
 public class FilterControlEnum extends FilterControl {
 	
 	private Button valueControl;
 	private Combo attr;
 	private AttributeDefinitionEnumeration attribute;
 	private EnumFilter templateFilter;
 	private Collection<EnumValue> items;
 
 	public FilterControlEnum(FilterPanel parent,
 			AttributeDefinitionEnumeration attribute) {
 		super(parent, SWT.FLAT);
 		this.attribute = attribute;
 		init();
 	}
 
 	public FilterControlEnum(FilterPanel parent, EnumFilter template) {
 		super(parent, SWT.FLAT);
 		this.attribute = (AttributeDefinitionEnumeration) template.getAttribute();
 		this.templateFilter = template;			
 		init();		
 	}
 
 	private void init() {
 		if (!(attribute instanceof AttributeDefinitionEnumeration)) {
 			throw new IllegalArgumentException("Not allowed: " + attribute);
 		}
 		setLayout(new GridLayout(2, false));
 		if (templateFilter != null) {
 			items = templateFilter.getFilterValue1();
 		} else {
 			items = new HashSet<EnumValue>();
 		}
 		createOperators();
 		createValueControl();
 		updateItems(items);
 	}
 
 	private void createValueControl() {
 		valueControl = new Button(this, SWT.PUSH | SWT.FLAT | SWT.WRAP);
 		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		layoutData.widthHint = 200;
 		valueControl.setLayoutData(layoutData);
 		valueControl.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				popupSelector(valueControl);
 			}
 		});
 	}
 
 	protected void popupSelector(Control control) {
 		Shell shell = EnumSelector.createShell(control);
 		EnumSelector selector = new EnumSelector(attribute.getType()
 				.getSpecifiedValues(), items, shell, SWT.BORDER);
 		int status = selector.showEnumSelector(shell);
 		if (status == SWT.OK) {
 			updateItems(selector.getItems());
 		}
 	}
 
 	private void updateItems(Collection<EnumValue> items) {
 		this.items = items;
 		StringBuilder sb = new StringBuilder();
 		for (Iterator<EnumValue> i = items.iterator(); i.hasNext();) {
 			EnumValue value = i.next();
 
 			// In case items have been removed
 			if (! attribute.getType().getSpecifiedValues().contains(value)) continue;
 
 			String label = value.getLongName() != null ? value.getLongName() : value.getIdentifier(); 
 			sb.append(label);
 			if (i.hasNext()) sb.append(", ");
 		}
 		valueControl.setText(sb.toString());
		layout();
 		getParent().layout();
 		getParent().getParent().layout();
 	}
 
 	// TODO use correct operators.
 	private void createOperators() {
 		attr = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
 		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		attr.setLayoutData(layoutData);
 		for (Operator operator : EnumFilter.SUPPORTED_OPERATORS) {
 			attr.add(getString(operator.toString()));			
 		}
 		attr.select(0);
 		if (templateFilter != null)
 			attr.select(EnumFilter.SUPPORTED_OPERATORS.asList().indexOf(
 					templateFilter.getOperator()));
 	}
 	
 	public IFilter getFilter() {
 		Operator operator = EnumFilter.SUPPORTED_OPERATORS.asList().get(attr.getSelectionIndex());
 		return new EnumFilter(operator, items, attribute);
 	}
 }
