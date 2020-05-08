 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
 * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.editor.mecontrols;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.conversion.IConverter;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.databinding.EMFDataBindingContext;
 import org.eclipse.emf.databinding.edit.EMFEditObservables;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.jface.databinding.swt.ISWTObservableValue;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * A control used for primitive types that may be represented by a textual representation. 
  * 
  * @author helming
  * @author emueller
  *
  * @param <T> the actual primitive type
  */
 public abstract class MEPrimitiveAttributeControl<T> extends AbstractMEControl {
 	
 	public final static Map<Class<?>, Class<?>> primitives = new HashMap<Class<?>, Class<?>>();
 	
 	static {
 	    primitives.put(Boolean.class, boolean.class);
 	    primitives.put(Byte.class, byte.class);
 	    primitives.put(Short.class, short.class);
 	    primitives.put(Character.class, char.class);
 	    primitives.put(Integer.class, int.class);
 	    primitives.put(Long.class, long.class);
 	    primitives.put(Float.class, float.class);
 	    primitives.put(Double.class, double.class);
 	}
 
 	private Text text;
 	private boolean doVerify;
 	private EAttribute attribute;
 	private Composite composite;
 	private Label labelWidgetImage;  //Label for diagnostic image
 	private ControlDecoration controlDecoration;
 	private EMFDataBindingContext dbc;
 
 	/**
 	 * Returns the priority by which the control should be rendered.
 	 * The priority determines which control will be used to render
 	 * a specific type since multiple controls may be registered to 
 	 * render the same type.
 	 * 
 	 * @return an integer value representing the priority by which the control gets rendered
 	 */
 	protected abstract int getPriority();
 	
 	/**
 	 * Converts the string value to the actual model value.
 	 * 
 	 * @param s the string value to be converted
 	 * @return the model value of type <code>T</code>
 	 */
 	protected abstract T convertStringToModel(String s);
 	
 	/**
 	 * Converts the actual model value to its textual representation.
 	 * 
 	 * @param modelValue 
 	 * 			the model value which needs to be converted to its textual representation 
 	 * @return the string representation of the converted model value
 	 */
 	protected abstract String convertModelToString(T modelValue);
 	
 	/**
 	 * Returns a default value if the SWT Text control should be empty. 
 	 * 
 	 * @return a default value
 	 */
 	protected abstract T getDefaultValue();
 
 	/**
 	 * Called when the SWT Text control loses its focus.  This
 	 * is useful for cases where {@link #validateString(String)} returns true
 	 * but the value entered by the user is invalid.  For instance, this might be the case 
 	 * when the user enters a number that would cause a overflow.  In such cases the number 
 	 * should be set to max value that can represented by the type in question. 
 	 * 
 	 * @param text the string value currently entered in the SWT Text control
 	 * 			
 	 */
 	protected abstract void postValidate(String text);
 	
 	
 	/**
 	 * Validates the string entered in the text field.
 	 * This method is executed whenever the user modifies the text 
 	 * contained in the SWT Text control. 
 	 * 
 	 * @param text 
 	 * 			the text to be validated
 	 * @return true if <code>text</code> is a valid model value
 	 */
 	protected abstract boolean validateString(String text);
 	
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.ecp.editor.mecontrols.AbstractMEControl#canRender(org.eclipse.emf.edit.provider.IItemPropertyDescriptor, org.eclipse.emf.ecore.EObject)
 	 */
 	@Override
 	public int canRender(IItemPropertyDescriptor itemPropertyDescriptor, EObject modelElement) {
 		Object feature = itemPropertyDescriptor.getFeature(modelElement);
 		
 		if (feature instanceof EAttribute && (((EAttribute) feature).getEType().getInstanceClass().equals(primitives.get(getClassType()))
 			|| ((EAttribute) feature).getEType().getInstanceClass().equals((getClassType())))
 			&& !((EAttribute) feature).isMany()) {
 
 			return getPriority();
 		}
 		
 		return AbstractMEControl.DO_NOT_RENDER;
 	}
 	
 	/**
 	 * Sets the content of the SWT text control to the given string without
 	 * calling {@link #validateString(String)}.
 	 * @param string 
 	 * 			the content of the SWT Text control
 	 */
 	protected void setUnvalidatedString(String string) {
 		boolean oldDoVerify = doVerify;
 		doVerify = false;
 		text.setText(string);
 		doVerify = oldDoVerify;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Class<T> getClassType() {
 		Class<?> clazz = getClass();
 
 		while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
 			clazz = clazz.getSuperclass();
 		}
 		
 		Type[] actualTypeArguments = ((ParameterizedType) clazz.getGenericSuperclass())
 				.getActualTypeArguments();
 		return (Class<T>) actualTypeArguments[0];
 	}
 	
 	@Override
 	protected Control createControl(Composite parent, int style) {
 		// TODO: activate verification once again
 		doVerify = false;
 		Object feature = getItemPropertyDescriptor().getFeature(getModelElement());
 		this.attribute = (EAttribute) feature;
 		
 		composite = getToolkit().createComposite(parent, style);
 		composite.setBackgroundMode(SWT.INHERIT_FORCE);
 		GridLayoutFactory.fillDefaults().numColumns(2).spacing(2, 0).applyTo(composite);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
 
 		labelWidgetImage = getToolkit().createLabel(composite, "    ");
 		labelWidgetImage.setBackground(parent.getBackground());
 
 		text = getToolkit().createText(composite, new String(), style | SWT.SINGLE | SWT.BORDER);
 		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		if (!getItemPropertyDescriptor().canSetProperty(getModelElement())) {
 			text.setEditable(false);
 		}
 		
 		controlDecoration = new ControlDecoration(text, SWT.RIGHT | SWT.TOP);
 		controlDecoration.setDescriptionText("Invalid input");
 		controlDecoration.setShowHover(true);
 		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
 		         FieldDecorationRegistry.DEC_ERROR);
 		controlDecoration.setImage(fieldDecoration.getImage());
 		
 		IObservableValue model = EMFEditObservables.observeValue(getEditingDomain(), getModelElement(), attribute);
 		dbc = new EMFDataBindingContext();
 		ISWTObservableValue observeText = SWTObservables.observeText(text, SWT.FocusOut);
 		dbc.bindValue(observeText, model, getTargetToModelStrategy(), getModelToTargetStrategy());
 		
 		text.addVerifyListener(new VerifyListener() {
 
 			public void verifyText(VerifyEvent e) {
 				if (doVerify) {
 					
 					final String oldText = text.getText();
 			        String newText = oldText.substring(0, e.start) + e.text + oldText.substring(e.end);	
 					if (!validateString(newText)) {
 						e.doit = false;
 						return;
 					}
 				}
 			}
 		});
 		
 		text.addFocusListener(new FocusListener() {
 			
 			public void focusLost(FocusEvent e) {
 				if (text.getText().equals("")) {
 					setUnvalidatedString(convertModelToString(getDefaultValue()));
 				} else {
 					postValidate(text.getText());
 				}
 				
 				dbc.updateModels();
 				dbc.updateTargets();
 			}
 			
 			public void focusGained(FocusEvent e) {
 				// do nothing
 			}
 		});
 			
 		return composite;
 	}
 
 	/**
 	 * Returns the strategy that is used to convert the string to the model value.
 	 * @return the target to model update value strategy
 	 */
 	protected UpdateValueStrategy getTargetToModelStrategy() {
 		// Define a validator to check that only numbers are entered
 		IValidator validator = new IValidator() {
 			public IStatus validate(Object value) {
 				boolean valid = validateString(text.getText());
 				
 				if (valid) {
 					controlDecoration.hide();
 					return ValidationStatus.ok();
 				}
 				
 				controlDecoration.show();
 				return ValidationStatus.error("Not a double.");
 			}
 		};
 		
 		UpdateValueStrategy strategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
 		strategy.setAfterGetValidator(validator);
 		strategy.setConverter(getConverter());
 
 		return strategy;
 	}
 	
 	/**
 	 * Returns the strategy that is used to convert the model value to the string.
 	 * @return the model to string update value strategy
 	 */
 	protected UpdateValueStrategy getModelToTargetStrategy() {
 		UpdateValueStrategy updateValueStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
 		updateValueStrategy.setConverter(new IConverter() {
 			
 			public Object getToType() {
 				return String.class;
 			}
 			
 			public Object getFromType() {
 				return getClassType();
 			}
 			
 			public Object convert(Object fromObject) {
 				@SuppressWarnings("unchecked") T val = (T) fromObject;
 				return convertModelToString(val);
 			}
 		});
 		return updateValueStrategy;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void applyCustomLayoutData() {
 		GridDataFactory.fillDefaults().grab(true, false).hint(250, 16).align(SWT.FILL, SWT.TOP).applyTo(text);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 **/
 	public void handleValidation(Diagnostic diagnostic) {
 		if (diagnostic.getSeverity() == Diagnostic.ERROR || diagnostic.getSeverity() == Diagnostic.WARNING) {
 			Image image = org.eclipse.emf.ecp.editor.Activator.getImageDescriptor("icons/validation_error.png").createImage();
 			this.labelWidgetImage.setImage(image);
 			this.labelWidgetImage.setToolTipText(diagnostic.getMessage());
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 **/
 	public void resetValidation() {
 		this.labelWidgetImage.setImage(null);
 		this.labelWidgetImage.setToolTipText("");
 	}
 
 	/**
 	 * This sets the keyboard focus in Text control.
 	 **/
 	public void setFocus() {
 		text.setFocus();
 	}
 	
 	protected IConverter getConverter() {
 		IConverter converter = new IConverter() {
 			
 			public Object getToType() {
 				return getClassType();
 			}
 			
 			public Object getFromType() {
 				return String.class;
 			}
 			
 			public Object convert(Object fromObject) {
 				T convertedValue = convertStringToModel((String) fromObject);
 				return convertedValue;
 			}
 		};
 		
 		return converter;
 	}
 }
