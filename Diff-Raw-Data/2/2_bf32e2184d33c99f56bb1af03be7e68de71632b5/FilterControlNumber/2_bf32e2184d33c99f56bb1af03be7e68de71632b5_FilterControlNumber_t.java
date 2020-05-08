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
 
 import java.math.BigInteger;
 import java.util.List;
 
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeDefinitionReal;
 import org.eclipse.rmf.reqif10.search.filter.IFilter;
 import org.eclipse.rmf.reqif10.search.filter.IFilter.Operator;
 import org.eclipse.rmf.reqif10.search.filter.NumberFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * This control handles Integers and Reals.
 
  * @author jastram
  */
 public class FilterControlNumber extends FilterControl {
 
 	private NumberControl control[];
 
 	public FilterControlNumber(FilterPanel parent,
 			AttributeDefinition attribute) {
 		super(parent, attribute);
 	}
 
 	public FilterControlNumber(FilterPanel parent, NumberFilter template) {
 		super(parent, template);
 	}
 	
 		
 	@Override
 	protected List<Operator> getOperators() {
 		return NumberFilter.SUPPORTED_OPERATORS.asList();	}
 
 	@Override
 	protected void updateValueControls(boolean initialize) {
 		if (getOperator() == Operator.IS_SET
 				|| getOperator() == Operator.IS_NOT_SET) {
 			showControl(0, false);
 			showControl(1, false);
 		}
 		else if (getOperator() == Operator.BETWEEN) {
 			showControl(0, true);
 			showControl(1, true);
 		} else {
 			showControl(0, true);
 			showControl(1, false);
 		}
 		
 		if (initialize) {
 			if (templateFilter.getFilterValue1() != null) {
 				control[0].setValue((Number) templateFilter.getFilterValue1());
 			}
 			if (templateFilter.getFilterValue2() != null) {
				control[1].setValue((Number) templateFilter.getFilterValue2());
 			}
 		}
 
 		layout();
 	}
 	
 	private void showControl(int controlId, boolean show) {
 		 if (control == null) control = new NumberControl[2];
 		 
 		if (show && control[controlId] == null) {
 			control[controlId] = new NumberControl(this, attribute instanceof AttributeDefinitionReal);
 			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 			control[controlId].getControl().setLayoutData(layoutData);
 		} 
 		if (! show && control[controlId] != null) {
 			control[controlId].getControl().dispose();
 			control[controlId] = null;
 		}
 	}
 
 	@Override
 	public IFilter getFilter() {
 		Number value1 = control[0] == null ? null : control[0].getNumber();
 		Number value2 = control[1] == null ? null : control[1].getNumber();
 		return new NumberFilter(getOperator(), value1, value2, (AttributeDefinition)attribute);
 	}
 }
 
 /**
  * We need to build our own control, rather than using {@link Spinner}, because
  * Spinner is a total hack with far too many limitations :-(
  * 
  * @author jastram
  */
 class NumberControl {
 
 	private boolean isReal;
 	private Text text;
 
 	public NumberControl(Composite parent, boolean isReal) {
 		text = new Text(parent, SWT.BORDER | SWT.FILL);
 		this.isReal = isReal;
 		text.setText("0");
 		addValidator();
 	}
 	
 	public Text getControl() {
 		return text;
 	}
 
 	private void addValidator() {
 		text.addVerifyListener(new VerifyListener() {
 			@Override
 			public void verifyText(VerifyEvent e) {
 				final String oldS = text.getText();
 				final String newS = oldS.substring(0, e.start) + e.text
 						+ oldS.substring(e.end);
 				
 				// We allow an empty String
 				if (newS.length() == 0) return;
 
 				try {
 					if (isReal) {
 						new Double(newS);
 					} else {
 						new BigInteger(newS);
 					}
 				} catch (final NumberFormatException numberFormatException) {
 					// value is not decimal
 					e.doit = false;
 				}
 			}
 		});
 	}
 
 	public Number getNumber() {
 		if (text.getText().length() == 0 ) text.setText("0");
 		return isReal ? new Double(text.getText()) : new BigInteger(text.getText());		
 	}
 
 	public void setValue(Number value) {
 		text.setText(value.toString());
 	}
 	
 }
