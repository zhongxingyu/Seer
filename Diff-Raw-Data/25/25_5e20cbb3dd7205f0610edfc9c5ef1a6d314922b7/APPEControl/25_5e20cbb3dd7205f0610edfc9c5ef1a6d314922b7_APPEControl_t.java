 package ch.hslu.appe.fs1303.gui.controls;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.forms.IMessageManager;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 import ch.hslu.appe.fs1303.gui.validators.iValidator;
 
 public abstract class APPEControl<T, H extends Control> {
 	
 	protected Label fLabel;
 	protected H fControl;
 	private boolean fNullable;
 	private iValidator fValidator;
 	
 	protected Object fModel;	
 	protected Method fGetter;
 	protected Method fSetter;
 	private String fField;
 	private IMessageManager fManager;
 	private String fMessage;
 
 	public APPEControl(Composite parent, FormToolkit toolkit, String labelText, int style) {
 		if (labelText != null) {
 			fLabel = toolkit.createLabel(parent, labelText);
 			fLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		}
 
 	    fControl = createControl(parent, toolkit, style);
 	    addModifyListener(new ModifyListener() {
 			
 			@Override
 			public void modifyText(ModifyEvent arg0) {
 				if (validate()) {
 					updateModel();
 					
 					if (fManager != null) {
 						fManager.removeMessage(fField);
 					}
 				} else {
 					if (fManager != null) {
 						fManager.addMessage(fField, fMessage, null, IMessageProvider.ERROR);
 					}
 				}
 			}
 		});
 	    
 	    fNullable = false;
 	}
 	
 	public abstract H createControl(Composite parent, FormToolkit toolkit, int style);
 	
 	public abstract void addModifyListener(ModifyListener listener);
 	
 	public abstract void removeModifyListener(ModifyListener listener);
 	
 	public abstract String getControlValue();
 	
 	public boolean validate() {
 		boolean success = true;
 		
		// No not validate Read-Only fields
		if ((fControl.getStyle() & SWT.READ_ONLY) > 0) return true;
			
 		if (getValidator() != null) {
 			success = getValidator().validate(getControlValue(), isNullable());
 			if (!success) {
 				fControl.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
 			} else {
 				fControl.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 			}
 		}
 		
 		return success;
 	}
 
 	public void bindModel(Object model, String field) {
 		fModel = model;
 		fField = field;
 		
 		String getter = "get" + field.substring(1);
 		String setter = "set" + field.substring(1);
 		try {
 			fGetter = model.getClass().getMethod(getter, new Class[0]);
 			fSetter = model.getClass().getMethod(setter, getFieldClass());
 		} catch (SecurityException e) {		
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		
 		updateFromModel();
 	}
 	
 	public abstract Class<?> getFieldClass();
 	
 	public abstract void setControlValue(String value);
 	
 	@SuppressWarnings("unchecked")
 	public void updateFromModel() {
 		try {
 			setControlValue(getDisplayValue((T)fGetter.invoke(fModel, new Object[0])));
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void updateModel() {
 		try {
 			fSetter.invoke(fModel, getValueForModel(getControlValue()));
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public abstract T getValueForModel(String value);
 	
 	public abstract String getDisplayValue(T value);
 	
 	public boolean isNullable() {
 		return fNullable;
 	}
 
 	public void setNullable(boolean nullable) {
 		fNullable = nullable;
 	}
 	
 	public void addValidationMessage(IMessageManager manager, String message) {
 		fManager = manager;
 		fMessage = message;		
 	}
 
 	public iValidator getValidator() {
 		return fValidator;
 	}
 
 	public void setValidator(iValidator validator) {
 		fValidator = validator;
 	}
 }
