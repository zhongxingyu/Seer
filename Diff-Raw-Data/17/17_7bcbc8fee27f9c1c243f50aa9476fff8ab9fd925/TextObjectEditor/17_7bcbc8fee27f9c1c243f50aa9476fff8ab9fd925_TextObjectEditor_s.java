 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc.
  *  All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  *
  * @author Bob Brodt
  ******************************************************************************/
 
 package org.eclipse.bpmn2.modeler.core.merrimac.dialogs;
 
 import org.eclipse.bpmn2.modeler.core.adapters.AdapterUtil;
 import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
 import org.eclipse.bpmn2.modeler.core.merrimac.clad.AbstractDetailComposite;
 import org.eclipse.bpmn2.modeler.core.utils.ErrorUtils;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * @author Bob Brodt
  *
  */
 public class TextObjectEditor extends ObjectEditor {
 
 	protected Text text;
 	protected boolean multiLine = false;
 	protected boolean testMultiLine = true;
 	
 	/**
 	 * @param parent
 	 * @param object
 	 * @param feature
 	 */
 	public TextObjectEditor(AbstractDetailComposite parent, EObject object, EStructuralFeature feature) {
 		super(parent, object, feature);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.bpmn2.modeler.ui.property.editors.ObjectEditor#createControl(org.eclipse.swt.widgets.Composite, java.lang.String)
 	 */
 	@Override
 	protected Control createControl(Composite composite, String label, int style) {
 		createLabel(composite,label);
 
 		if (testMultiLine && ModelUtil.isMultiLine(object,feature)) {
 			multiLine = true;
 			style |= SWT.MULTI | SWT.V_SCROLL;
 		}
 
 		text = getToolkit().createText(composite, "", style | SWT.BORDER);
 		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		if (multiLine) {
 			data.heightHint = 100;
 		}
 		text.setLayoutData(data);
 		text.addTraverseListener(new TraverseListener() {
 
 			@Override
 			public void keyTraversed(TraverseEvent e) {
 				if (e.detail == SWT.TRAVERSE_RETURN && multiLine)
 					e.doit = false;
 			}
 			
 		});
		setText(ModelUtil.getDisplayName(object, feature));
 
 		IObservableValue textObserver = SWTObservables.observeText(text, SWT.Modify);
 		textObserver.addValueChangeListener(new IValueChangeListener() {
 
 			@Override
 			public void handleValueChange(final ValueChangeEvent e) {
 				setValue(e.diff.getNewValue());
 			}
 		});
 		
 		text.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent e) {
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				ErrorUtils.showErrorMessage(null);
 			}
 		});
 
 		// ask the object if this feature is read-only
 		ExtendedPropertiesAdapter adapter = (ExtendedPropertiesAdapter) AdapterUtil.adapt(object, ExtendedPropertiesAdapter.class);
		if (adapter!=null) {
 			Object result = adapter.getProperty(feature, ExtendedPropertiesAdapter.UI_CAN_EDIT);
 			if (result instanceof Boolean)
 				setEditable((Boolean)result);
 		}
 
 		return text;
 	}
 	
 	public void setMultiLine(boolean multiLine) {
 		testMultiLine = false;
 		this.multiLine = multiLine;
 
 	}
 	
 	public void setEditable(boolean editable) {
 		text.setEditable(editable);
 	}
 	
 	@Override
 	public void setObject(EObject object) {
 		super.setObject(object);
 		updateText();
 	}
 	
 	@Override
 	public void setObject(EObject object, EStructuralFeature feature) {
 		super.setObject(object, feature);
 		updateText();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.bpmn2.modeler.core.merrimac.dialogs.ObjectEditor#setValue(java.lang.Object)
 	 */
 	@Override
 	protected boolean setValue(final Object result) {
 		if (super.setValue(result)) {
 			updateText();
 			return true;
 		}
 		// revert the change on error
		text.setText(ModelUtil.getDisplayName(object, feature));
 		return false;
 	}
 	
 	/**
 	 * Update the text field widget after its underlying value has changed.
 	 */
 	protected void updateText() {
 		if (!text.getText().equals(getText())) {
 			int pos = text.getCaretPosition();
 			setText(getText());
 			text.setSelection(pos, pos);
 		}
 	}
 	
 	/**
 	 * Set the text field with the given value
 	 * 
 	 * @param value - new value for the text field
 	 */
 	protected void setText(String value) {
 		if (value==null)
 			value = "";
 		if (!value.equals(text.getText()))
 				text.setText(value);
 	}
 	
 	/**
 	 * Returns the string representation of the given value used for
 	 * display in the text field. The default implementation correctly
 	 * handles structureRef values (proxy URIs from a DynamicEObject)
 	 * and provides reasonable behavior for EObject values.
 	 * 
 	 * @param value - new object value. If null is passed in, the implementation
 	 * should substitute the original value of the EObject's feature.
 	 * 
 	 * @return string representation of the EObject feature's value.
 	 */
 	protected String getText() {
 		return ModelUtil.getDisplayName(object, feature);
 	}
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		if (this.object == notification.getNotifier()) {
 			if (notification.getFeature() instanceof EStructuralFeature) {
 				EStructuralFeature f = (EStructuralFeature)notification.getFeature();
 				if (f!=null && (f.getName().equals(this.feature.getName()) ||
 						f.getName().equals("mixed")) ) { // handle the case of FormalExpression.body
 					updateText();
 					super.notifyChanged(notification);
 				}
 			}
 		}
 	}
 	
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		text.setVisible(visible);
 		GridData data = (GridData)text.getLayoutData();
 		data.exclude = !visible;
 	}
 	
 	public void dispose() {
 		super.dispose();
 		if (text!=null && !text.isDisposed()) {
 			text.dispose();
 			text = null;
 		}
 	}
 	
 	public Control getControl() {
 		return text;
 	}
 
 	@Override
 	public Object getValue() {
 		return getText();
 	}
 }
