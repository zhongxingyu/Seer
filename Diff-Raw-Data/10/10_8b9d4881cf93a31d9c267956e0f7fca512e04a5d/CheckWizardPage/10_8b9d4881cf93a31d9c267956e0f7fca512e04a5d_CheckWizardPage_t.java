 package org.dawb.common.ui.wizard;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.util.GridUtils;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * A page for editing a list of booleans.
  * @author fcp94556
  *
  */
 public class CheckWizardPage extends WizardPage implements SelectionListener{
 
 
 	private Map<String, Boolean> values;
 	private Map<String, String>  stringValues;
 	private Collection<String>   listValues;
 	private Map<String, Control> textCache;
 	private Map<String, Button>  buttonCache;
 	private boolean isDisposed=false;
 	private boolean validationActive=true;
 
 	/**
 	 * The map is a map of labels to values.
 	 * 
 	 * @param pageName
 	 * @param values
 	 */
 	public CheckWizardPage(String pageName, Map<String,Boolean> values) {
 		super(pageName);
 		this.values = values;
 	}
 
 
 	public void setStringValues(Map<String,String> values) {
 		this.stringValues = values;
 	}
 
 	/**
 	 * Sets default value of string
 	 * @param label
 	 * @param stringValue
 	 */
 	public void setStringValue(String label, String stringValue) {
 		if (stringValue==null) {
 			if (stringValues==null) return;
 			stringValues.remove(label);
 			if (textCache!=null) {
 				Control widget = textCache.get(label);
 				if (widget!=null) {
 					GridUtils.setVisible(widget, false);
 					widget.getParent().layout(new Control[]{widget});
 				}
 				textCache.remove(label);
 			}
 			
 		} else {
 			if (stringValues==null) stringValues = new HashMap<String, String>();
 			stringValues.put(label, stringValue);
 			if (textCache!=null) {
 				Widget widget = textCache.get(label);
 				if (widget!=null) {
 					if (widget instanceof Text) {
 						((Text)widget).setText(stringValue);
 					} else if (widget instanceof CCombo) {
 						final List<String> items = Arrays.asList(((CCombo)widget).getItems());
 						((CCombo)widget).select(items.indexOf(stringValue));
 					}
 				}
 			}
 		}
 		validate();
 	}
 	
 	/**
 	 * Sets list of values to edit with the string. Changes the
 	 * editor to a combo if the UI has not been created as yet.
 	 * @param label
 	 * @param choices
 	 */
 	public void setStringValues(String label, List<String> choices) {
 		if (stringValues==null) stringValues = new HashMap<String, String>();
 		stringValues.put(label, choices.get(0));
 		if (listValues==null) listValues = new ArrayList<String>(7);
 		listValues.add(label);
 		if (textCache!=null) {
 			Widget widget = textCache.get(label);
 			if (widget!=null) {
 	            CCombo combo = (CCombo)widget;
 	            combo.setItems(choices.toArray(new String[choices.size()]));
 			    combo.select(0);
 			}
 			validate();
 		}
 	}
 
 
 	@Override
 	public void createControl(Composite parent) {
 		final Composite content = new Composite(parent, SWT.NONE);
 		content.setLayout(new GridLayout(1, false));
 		
 		for (final String label : values.keySet()) {
 		    if (buttonCache==null) buttonCache = new HashMap<String, Button>(7);
 			Button button = new Button(content, SWT.CHECK);
 			buttonCache.put(label, button);
 			
 			button.setText(label);
 			button.setSelection(values.get(label));
 			if (stringValues!=null && stringValues.containsKey(label)) {
 			    button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 			    
 			    if (listValues!=null && listValues.contains(label)) {
 				    final CCombo combo = new CCombo(content, SWT.READ_ONLY|SWT.BORDER);
 				    combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 				    combo.setEnabled(values.get(label));
 				    combo.addSelectionListener(new SelectionAdapter() {
 				    	public void widgetSelected(SelectionEvent e) {
 							stringValues.put(label, combo.getItems()[combo.getSelectionIndex()]);
 							validate();
 				    	}
 				    });
 				   
 				    if (textCache==null) textCache = new HashMap<String, Control>(7);
 				    textCache.put(label, combo);
 				    combo.select(0);
 			    	
 			    } else {
 				    final Text text = new Text(content, SWT.BORDER);
 				    text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 				    text.setEnabled(values.get(label));
 				    text.setText(stringValues.get(label));
 				    text.addModifyListener(new ModifyListener() {					
 						@Override
 						public void modifyText(ModifyEvent e) {
							final String textValue = text.getText();
							stringValues.put(label, textValue);
 							validate();
 							text.setFocus();
 						}
 					});
 				    if (textCache==null) textCache = new HashMap<String, Control>(7);
 				    textCache.put(label, text);
 			    }
 			    
 			} else {
 			    button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 			}
 			button.addSelectionListener(this);
 		}
 		setControl(content);
 		validate();
 	}
 
 	/**
 	 * 
 	 * @return true if valid
 	 */
 	private void validate() {
 		
 		if (!validationActive) return;
 		try {
 			validationActive = false;
 			for (String label : values.keySet()) {
 				if (stringValues!=null&&stringValues.containsKey(label) && values.get(label)) {
 					String value = stringValues.get(label);
 					if (value==null || value.equals("")) {
 						setErrorMessage("Please set a value for '"+label+"'.");
 						if (isPageComplete()) setPageComplete(false);
 						return;
 					}
 				}
 			}
 			setErrorMessage(null);
 			if (!isPageComplete()) setPageComplete(true);
 			return;
 		} finally {
 			validationActive = true;
 		}
 	}
 
     public void setOptionEnabled(String label, boolean isEnabled) {
 		values.put(label, isEnabled);
 		if (buttonCache!=null && buttonCache.containsKey(label)) {
 			buttonCache.get(label).setEnabled(isEnabled);
 			buttonCache.get(label).setSelection(isEnabled);
 		}
 		if (textCache!=null && textCache.containsKey(label)) {
 			textCache.get(label).setEnabled(isEnabled);
 			if (textCache.get(label) instanceof Text) {
 				((Text)textCache.get(label)).setText("");
 			}
 		}
 	}
 
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (isDisposed) return;
 		final Button button = (Button)e.getSource();
 		final String label  = button.getText();
 		values.put(label, button.getSelection());
 		if (textCache!=null && textCache.get(label)!=null) {
 			textCache.get(label).setEnabled(button.getSelection());
 		}
 		validate();
 	}
 	@Override
 	public void dispose() {
 		isDisposed = true;
 		if (buttonCache!=null) buttonCache.clear();
 		if (textCache!=null)   textCache.clear();
 		super.dispose();
 	}
 	
 	public boolean is(String propertyName) {
 		return values.get(propertyName);
 	}
 
 	public String getString(String propertyName) {
 		try {
 		    return stringValues.get(propertyName);
 		} catch (Throwable ne) {
 			return null;
 		}
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
