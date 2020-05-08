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
 
 import java.util.List;
 
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeDefinitionString;
 import org.eclipse.rmf.reqif10.AttributeDefinitionXHTML;
 import org.eclipse.rmf.reqif10.search.filter.AbstractTextFilter;
 import org.eclipse.rmf.reqif10.search.filter.AbstractTextFilter.InternalAttribute;
 import org.eclipse.rmf.reqif10.search.filter.IFilter;
 import org.eclipse.rmf.reqif10.search.filter.IFilter.Operator;
 import org.eclipse.rmf.reqif10.search.filter.StringFilter;
 import org.eclipse.rmf.reqif10.search.filter.XhtmlFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Used for plain text and XHTML
  * 
  * @author jastram
  */
 public class FilterControlString extends FilterControl {
 
 	private Text text;
 	private Button caseSensitive;
 
 	public FilterControlString(FilterPanel parent, StringFilter.InternalAttribute attribute) {
 		super(parent, attribute);
 	}
 
 	public FilterControlString(FilterPanel parent,
 			AttributeDefinition attribute) {
 		super(parent, attribute);
 	}
 
 	public FilterControlString(FilterPanel parent, AbstractTextFilter template) {
 		super(parent, template);
 	}
 
 	@Override
 	protected List<Operator> getOperators() {
 		if (attribute instanceof AttributeDefinitionXHTML) {
 			return XhtmlFilter.SUPPORTED_OPERATORS.asList();
 		} else {
 			return StringFilter.SUPPORTED_OPERATORS.asList();
 		}
 	}
 
 	@Override
 	protected void updateValueControls(boolean initialize) {
 		if (getOperator() == Operator.IS_SET
 				|| getOperator() == Operator.IS_NOT_SET) {
 			showControl(false);
 		} else {
 			showControl(true);
 		}
 		if (initialize) {
 			if (text != null) {
				caseSensitive.setSelection(((StringFilter) templateFilter)
 						.isCaseSensitive());
 				text.setText((String) templateFilter.getFilterValue1());
 	
 			}
 		}
 	}
 
 	private void showControl(boolean show) {
 		if (show && text == null) {
 			caseSensitive = new Button(this, SWT.CHECK);
 			caseSensitive.setText("Aa");
 			caseSensitive.setToolTipText("Case Sensitive");
 			GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 			caseSensitive.setLayoutData(layoutData);
 			
 			text = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.FILL);
 			layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 			text.setLayoutData(layoutData);
 		}
 		if (!show && text != null) {
 			text.dispose();
 			text = null;
 			caseSensitive.dispose();
 			caseSensitive = null;
 		}
 	}
 
 	public IFilter getFilter() {
 		String value = text == null ? null : text.getText();
 		boolean cv = caseSensitive == null ? false : caseSensitive.getSelection();
 		
 		if (attribute instanceof InternalAttribute) {
 			return new StringFilter(getOperator(), value, (InternalAttribute) attribute, cv);
 		} else if (attribute instanceof AttributeDefinitionString) {
 			return new StringFilter(getOperator(), value, (AttributeDefinitionString) attribute, cv);
 		} else if (attribute instanceof AttributeDefinitionXHTML) {
 			return new XhtmlFilter(getOperator(), value, (AttributeDefinitionXHTML) attribute, cv);
 		} else {
 			throw new IllegalStateException("Can't handle: " + attribute);
 		}
 	}
 
 }
