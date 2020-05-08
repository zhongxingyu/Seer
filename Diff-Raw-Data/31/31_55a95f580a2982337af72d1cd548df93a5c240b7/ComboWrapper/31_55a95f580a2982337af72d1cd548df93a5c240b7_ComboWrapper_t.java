 /*-
 * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.richbeans.components.wrappers;
 
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.jface.fieldassist.AutoCompleteField;
 import org.eclipse.jface.fieldassist.ComboContentAdapter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 
 import swing2swt.layout.BorderLayout;
 import uk.ac.gda.richbeans.beans.IFieldWidget;
 import uk.ac.gda.richbeans.components.ButtonComposite;
 import uk.ac.gda.richbeans.event.ValueEvent;
 
 /**
  * Class designed to wrap combo and allow BeanUI to talk to the class
  * to build up XML files from the values and link them back to the bean.
  * 
  * You have to be a widget (even though not needed) so that RCP developer
  * can deal with using the class. Therefore in inherits from Composite.
  */
 public class ComboWrapper extends ButtonComposite implements IFieldWidget{
 
 	
 	protected final Combo combo;
 	protected AutoCompleteField autoComplete;
 	protected Map<String, ?> itemMap;
 	private SelectionAdapter selectionListener;
 	private ModifyListener modifyListener;
 	
 	/**
 	 * Construct combo.
 	 * @param parent
 	 * @param style
 	 */
 	public ComboWrapper(final Composite parent, int style) {
 		super(parent, SWT.NONE);
 		setLayout(new BorderLayout());
 		
 		final int textStyle = style==SWT.NONE?SWT.BORDER:style;
 		combo = new Combo(this, textStyle);
 		combo.setLayoutData(BorderLayout.CENTER);
 		mainControl = combo;
 		
 		this.selectionListener = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				fireValueListeners();
 			}
 		};
 		combo.addSelectionListener(selectionListener);
 		
 		this.modifyListener = new ModifyListener() {
 			private List<String> items;
 			@Override
 			public void modifyText(ModifyEvent e) {
 				final String txt = combo.getText();
 				if (items==null) items = Arrays.asList(combo.getItems());
 				if (items.contains(txt)) {
 					fireValueListeners();
 				}						
 			}
 		};
 		combo.addModifyListener(modifyListener);
 	
 	}
 	
 	/**
 	 * @param fieldName
 	 */
 	@Override
 	public void setFieldName(String fieldName) {
 		super.setFieldName(fieldName);
 		if (combo!=null&&!combo.isDisposed()) combo.setToolTipText(fieldName);
 	}
 	
 	@Override
 	public void dispose() {
 		if (combo!=null&&!combo.isDisposed()) {
 			combo.removeSelectionListener(selectionListener);
 			combo.removeModifyListener(modifyListener);
 		}
 		super.dispose();
 	}
 	
 	/**
 	 * Can be called to update things from linkUI();
 	 */
 	@Override
 	public void fireValueListeners() {
 		final ValueEvent evt = new ValueEvent(combo,getFieldName());
 		evt.setDoubleValue(combo.getSelectionIndex());
 		evt.setValue(getValue());
 		eventDelegate.notifyValueListeners(evt);
 	}
 
 	/**
 	 * If the visible items and the value in the data are not equal.
 	 * @param itemMap
 	 */
 	public void setItems(final Map<String,?> itemMap) {
 		setItems(itemMap.keySet().toArray(new String [itemMap.size()]));
 		this.itemMap = itemMap;
 	}
 	
 	/**
 	 * @param items
 	 */
 	public void setItems(final String [] items) {
 		combo.setItems(items);
 		// Auto-complete in Jface not working well.
 		if (autoComplete==null) autoComplete = new AutoCompleteField(combo, new ComboContentAdapter(), items) ;
 		autoComplete.setProposals(items);
 	}
 
 	@Override
 	public Object getValue() {
 		if (combo==null) return null;
 		if (combo.isDisposed()) return null;
 		final String text = combo.getText();
 		final int index   = combo.getSelectionIndex();
 		if (index<0&&text!=null) {
 			return itemMap!=null ? itemMap.get(text) : text;
 		} else if (index>-1) {
 			return itemMap!=null ? itemMap.get(combo.getItem(index)) : combo.getItem(index);
 		}
 		return null;
 	}
 	
 	@Override
 	public void setValue(Object value) {
		if (value == null) {
 			combo.clearSelection();
 			if (combo.getSelectionIndex() != -1) {
				throw new IllegalArgumentException("Attempted to clearSelection of Read Only ComboWrapper "
						+ getFieldName());
 			}
 			return;
 		}

 		String textValue = itemMap != null ? getKeyForValue(value) : value.toString();
		if (textValue == null && itemMap != null && !itemMap.isEmpty())
			textValue = itemMap.keySet().iterator().next();
		if (textValue != null) {
			final int index = combo.indexOf(textValue);
			if (index < 0) {
 				combo.clearSelection();
				throw new IllegalArgumentException("String '" + textValue + "' was not found for ComboBox");
 			}
			combo.select(index);
 		}
 	}
 
 	private String getKeyForValue(final Object value) {
 		if (itemMap==null) return null;
 		if (value  ==null) return null;
 		for (Entry<String,?> entry : itemMap.entrySet()) {
 			if (value.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	/*******************************************************************/
 	/**        This section will be the same for many wrappers.       **/
 	/*******************************************************************/
 
 	/**
 	 * @param active the active to set
 	 */
 	@Override
 	public void setActive(boolean active) {
 		super.setActive(active);
 		combo.setVisible(active);
 	}
 
 	/**
 	 * @param i
 	 */
 	public void setTextLimit(int i) {
 		combo.setTextLimit(i);
 	}
 
 	/**
 	 * @param indexOf
 	 */
 	public void select(int indexOf) {
 		combo.select(indexOf);
 	}
 
 	/**
 	 * @param systemColor
 	 */
 	@Override
 	public void setForeground(Color systemColor) {
 		if (systemColor.isDisposed()) return;
 		combo.setForeground(systemColor);
 	}
 
 	/**
 	 * @return i
 	 */
 	public int getSelectionIndex() {
 		return combo.getSelectionIndex();
 	}
 
 	/**
 	 * @param index
 	 * @return i
 	 */
 	public String getItem(int index) {
 		if (itemMap!=null) throw new RuntimeException("Cannot use getItem() when values are mapped!");
 		return combo.getItem(index);
 	}
 
 	/**
 	 * 
 	 * @return items
 	 */
 	public String[] getItems() {
 		if (itemMap!=null) throw new RuntimeException("Cannot use getItems() when values are mapped!");
 		return combo.getItems();
 	}
 
 	/**
 	 * Creates a map to choose integers while viewing strings
 	 * @param items
 	 * @return maps
 	 */
 	public static Map<String, Integer> getItemMap(String[] items) {
 		final Map<String,Integer> ret = new LinkedHashMap<String,Integer>(items.length);
 		for (int i = 0; i < items.length; i++) {
 			ret.put(items[i], i);
 		}
 		return ret;
 	}
 	
 	/*******************************************************************/
 
 }
 
 	
