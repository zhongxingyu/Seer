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
 
 import org.eclipse.rmf.reqif10.AttributeDefinitionString;
 import org.eclipse.rmf.reqif10.search.filter.AbstractTextFilter.InternalAttribute;
 import org.eclipse.rmf.reqif10.search.filter.IFilter;
 import org.eclipse.rmf.reqif10.search.filter.IFilter.Operator;
 import org.eclipse.rmf.reqif10.search.filter.StringFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Text;
 
 public class FilterControlString extends FilterControl {
 	
 	private Text text;
 	private Combo attr;
 	private Button caseSensitive;
 	private Object attribute;
 	private StringFilter templateFilter;
 
 	public FilterControlString(FilterPanel parent, StringFilter.InternalAttribute attribute) {
 		this(parent, (StringFilter)null);
 		this.attribute = attribute;
 	}
 
 	public FilterControlString(FilterPanel parent,
 			AttributeDefinitionString attribute) {
 		this(parent, (StringFilter)null);
 		this.attribute = attribute;
 	}
 
 	public FilterControlString(FilterPanel parent, StringFilter template) {
 		super(parent, SWT.FLAT);
 		if (template != null) {
 			this.attribute = template.getAttribute();
 			this.templateFilter = template;			
 		}
 		setLayout(new GridLayout(3, false));
 		createOperators();
 		createCaseSensitive();
 		createText();		
 	}
 
 	private void createText() {
 		text = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.FILL);
 		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		text.setLayoutData(layoutData);
		if (templateFilter != null) text.setText(templateFilter.getFilterValue1());
 	}
 
 	private void createCaseSensitive() {
 		caseSensitive = new Button(this, SWT.CHECK);
 		caseSensitive.setText("Aa");
 		caseSensitive.setToolTipText("Case Sensitive");
 		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		caseSensitive.setLayoutData(layoutData);
 		if (templateFilter != null) caseSensitive.setSelection(templateFilter.isCaseSensitive());
 	}
 
 	private void createOperators() {
 		attr = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
 		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		attr.setLayoutData(layoutData);
 		for (Operator operator : StringFilter.SUPPORTED_OPERATORS) {
 			attr.add(getString(operator.toString()));			
 		}
 		attr.select(0);
 		if (templateFilter != null)
 			attr.select(StringFilter.SUPPORTED_OPERATORS.asList().indexOf(
 					templateFilter.getOperator()));
 	}
 	
 	public IFilter getFilter() {
 		Operator operator = StringFilter.SUPPORTED_OPERATORS.asList().get(attr.getSelectionIndex());
 		String value = text.getText();
 		if (attribute instanceof InternalAttribute) {
 			return new StringFilter(operator, value, (InternalAttribute) attribute, caseSensitive.getSelection());
 		} else if (attribute instanceof AttributeDefinitionString) {
 			return new StringFilter(operator, value, (AttributeDefinitionString) attribute, caseSensitive.getSelection());
 		} else {
 			throw new IllegalStateException("Can't handle: " + attribute);
 		}
 	}
 }
