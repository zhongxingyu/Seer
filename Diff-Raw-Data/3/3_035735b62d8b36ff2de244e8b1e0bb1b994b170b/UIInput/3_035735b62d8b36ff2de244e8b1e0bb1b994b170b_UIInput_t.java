 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package javax.faces.component;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.el.EvaluationException;
 import javax.faces.el.MethodBinding;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.event.ValueChangeListener;
 import javax.faces.internal.ConverterResource;
 import javax.faces.internal.FacesMessageUtil;
 import javax.faces.internal.UIInputUtil;
 import javax.faces.internal.ValidatorResource;
 import javax.faces.render.Renderer;
 import javax.faces.validator.Validator;
 import javax.faces.validator.ValidatorException;
 
 import org.seasar.framework.util.AssertionUtil;
 
 /**
  * @author shot
  * @author manhole
  */
 public class UIInput extends UIOutput implements EditableValueHolder {
 
     public static final String COMPONENT_FAMILY = "javax.faces.Input";
 
     public static final String COMPONENT_TYPE = "javax.faces.Input";
 
     public static final String CONVERSION_MESSAGE_ID = "javax.faces.component.UIInput.CONVERSION";
 
     public static final String REQUIRED_MESSAGE_ID = "javax.faces.component.UIInput.REQUIRED";
 
     private Object submittedValue = null;
 
     private boolean localValueSet = false;
 
     private boolean required = false;
 
     private boolean requiredSet = false;
 
     private boolean valid = false;
 
     private boolean validSet = false;
 
     private boolean immediate = false;
 
     private boolean immediateSet = false;
 
     private MethodBinding validatorBinding = null;
 
     private MethodBinding valueChangeMethod = null;
 
     private List validators = null;
 
     private static final String DEFAULT_RENDER_TYPE = "javax.faces.Text";
 
     private static final Validator[] EMPTY_VALIDATOR_ARRAY = new Validator[0];
 
     public UIInput() {
         setRendererType(DEFAULT_RENDER_TYPE);
     }
 
     public String getFamily() {
         return COMPONENT_FAMILY;
     }
 
     public Object getSubmittedValue() {
         return submittedValue;
     }
 
     public void setSubmittedValue(Object submittedValue) {
         this.submittedValue = submittedValue;
     }
 
     public void setValue(Object value) {
         if ("".equals(value)) {
             value = null;
         }
         super.setValue(value);
         setLocalValueSet(true);
     }
 
     public boolean isLocalValueSet() {
         return localValueSet;
     }
 
     public void setLocalValueSet(boolean localValueSet) {
         this.localValueSet = localValueSet;
     }
 
     public boolean isRequired() {
         if (requiredSet) {
             return required;
         }
         Boolean value = (Boolean) ComponentUtil_.getValueBindingValue(this,
                 "required");
         return (value != null) ? Boolean.TRUE.equals(value) : required;
     }
 
     public void setRequired(boolean required) {
         this.required = required;
         requiredSet = true;
     }
 
     public boolean isValid() {
         if (validSet) {
             return valid;
         }
         Boolean value = (Boolean) ComponentUtil_.getValueBindingValue(this,
                 "valid");
         return (value != null) ? Boolean.TRUE.equals(value) : valid;
     }
 
     public void setValid(boolean valid) {
         this.valid = valid;
         validSet = true;
     }
 
     public boolean isImmediate() {
         if (immediateSet) {
             return immediate;
         }
         Boolean value = (Boolean) ComponentUtil_.getValueBindingValue(this,
                 "immediate");
         return (value != null) ? Boolean.TRUE.equals(value) : immediate;
     }
 
     public void setImmediate(boolean immediate) {
         this.immediate = immediate;
         immediateSet = true;
     }
 
     public MethodBinding getValidator() {
         return validatorBinding;
     }
 
     public void setValidator(MethodBinding validatorBinding) {
         this.validatorBinding = validatorBinding;
     }
 
     public MethodBinding getValueChangeListener() {
         return valueChangeMethod;
     }
 
     public void setValueChangeListener(MethodBinding valueChangeMethod) {
         this.valueChangeMethod = valueChangeMethod;
     }
 
     public void processDecodes(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         super.processDecodes(context);
         if (isImmediate()) {
             executeValidate(context);
         }
     }
 
     public void processValidators(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         super.processValidators(context);
         if (!isImmediate()) {
             executeValidate(context);
         }
     }
 
     public void processUpdates(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         try {
             updateModel(context);
         } catch (RuntimeException e) {
             context.renderResponse();
             throw e;
         }
         renderResponseIfNotValid(context);
     }
 
     public void decode(FacesContext context) {
         setValid(true);
         super.decode(context);
     }
 
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         super.broadcast(event);
         if (event instanceof ValueChangeEvent) {
             MethodBinding valueChangeListenerBinding = getValueChangeListener();
             if (valueChangeListenerBinding != null) {
                 try {
                     valueChangeListenerBinding.invoke(getFacesContext(),
                             new Object[] { event });
                 } catch (EvaluationException e) {
                     Throwable cause = e.getCause();
                     if (cause != null
                             && cause instanceof AbortProcessingException) {
                         throw (AbortProcessingException) cause;
                     } else {
                         throw e;
                     }
                 }
             }
         }
     }
 
     public void updateModel(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isValid() || !isLocalValueSet()) {
             return;
         }
         final ValueBinding valueBinding = getValueBinding("value");
         if (valueBinding == null) {
             return;
         }
         try {
             valueBinding.setValue(context, getLocalValue());
             setValue(null);
             setLocalValueSet(false);
         } catch (RuntimeException e) {
             Object[] args = { getId() };
             context.getExternalContext().log(e.getMessage(), e);
             FacesMessageUtil.addErrorMessage(context, this,
                     CONVERSION_MESSAGE_ID, args);
             setValid(false);
         }
     }
 
     public void validate(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         Object submittedValue = getSubmittedValue();
         if (submittedValue == null) {
             return;
         }
         Object convertedValue = getConvertedValue(context, submittedValue);
         validateValue(context, convertedValue);
         if (!isValid()) {
             return;
         }
         Object previous = getValue();
         setValue(convertedValue);
         setSubmittedValue(null);
         if (compareValues(previous, convertedValue)) {
             queueEvent(new ValueChangeEvent(this, previous, convertedValue));
         }
     }
 
     protected Object getConvertedValue(FacesContext context,
             Object submittedValue) throws ConverterException {
         Object value = submittedValue;
         try {
             value = convertFromAnnotation(context, submittedValue);
             if (value != null) {
                 return value;
             }
             Renderer renderer = getRenderer(context);
             if (renderer != null) {
                 value = convertFromRenderer(context, submittedValue, renderer);
             } else {
                 value = convertFromType(context, submittedValue);
             }
         } catch (ConverterException e) {
             handleConverterException(context, e);
         }
         return value;
     }
 
     private void handleConverterException(FacesContext context,
             ConverterException e) {
         setValid(false);
         FacesMessage facesMessage = e.getFacesMessage();
         if (facesMessage != null) {
             context.addMessage(getClientId(context), facesMessage);
         } else {
             Object[] args = new Object[] { getId() };
             FacesMessageUtil.addErrorMessage(context, this,
                     CONVERSION_MESSAGE_ID, args);
         }
     }
 
     protected Object convertFromAnnotation(FacesContext context,
             Object submittedValue) {
         ValueBinding vb = getValueBinding("value");
         if (vb != null) {
             String expression = vb.getExpressionString();
             Converter converter = ConverterResource.getConverter(expression);
             if (converter != null) {
                 try {
                     return converter.getAsObject(context, this,
                             (String) submittedValue);
                 } catch (ConverterException e) {
                     handleConverterException(context, e);
                 }
             }
         }
         return null;
     }
 
     protected Object convertFromRenderer(FacesContext context,
             Object submittedValue, Renderer renderer) throws ConverterException {
         return renderer.getConvertedValue(context, this, submittedValue);
     }
 
     protected Object convertFromType(FacesContext context, Object submittedValue)
             throws ConverterException {
         if (submittedValue instanceof String) {
             Converter converter = getConverterWithType(context);
             if (converter != null) {
                 return converter.getAsObject(context, this,
                         (String) submittedValue);
             }
         }
         return submittedValue;
     }
 
     protected void validateValue(FacesContext context, Object newValue) {
         if (isValid() && isRequired() && UIInputUtil.isEmpty(newValue)) {
             Object[] args = new Object[] { getId() };
             FacesMessageUtil.addErrorMessage(context, this,
                     REQUIRED_MESSAGE_ID, args);
             setValid(false);
         }
         if (isValid() && !UIInputUtil.isEmpty(newValue)) {
             validateFromAddedValidator(context, newValue);
             validateFromBinding(context, newValue);
         }
         validateFromAnnotation(context, newValue);
     }
 
     protected boolean compareValues(Object previous, Object value) {
         if (previous == null && value == null) {
             return false;
         }
         if (previous == null || value == null) {
             return true;
         }
         return !previous.equals(value);
     }
 
     public void addValidator(Validator validator) {
         AssertionUtil.assertNotNull("validator", validator);
         if (validators == null) {
             validators = new ArrayList();
         }
         validators.add(validator);
     }
 
     public Validator[] getValidators() {
         if (validators == null) {
             return EMPTY_VALIDATOR_ARRAY;
         }
         return ((Validator[]) validators.toArray(new Validator[validators
                 .size()]));
     }
 
     public void removeValidator(Validator validator) {
         if (validators != null) {
             validators.remove(validator);
         }
     }
 
     public void addValueChangeListener(ValueChangeListener listener) {
         AssertionUtil.assertNotNull("listener", listener);
         addFacesListener(listener);
     }
 
     public ValueChangeListener[] getValueChangeListeners() {
         return (ValueChangeListener[]) getFacesListeners(ValueChangeListener.class);
     }
 
     public void removeValueChangeListener(ValueChangeListener listener) {
         removeFacesListener(listener);
     }
 
     public Object saveState(FacesContext context) {
         Object values[] = new Object[10];
         values[0] = super.saveState(context);
         values[1] = localValueSet ? Boolean.TRUE : Boolean.FALSE;
         values[2] = required ? Boolean.TRUE : Boolean.FALSE;
         values[3] = requiredSet ? Boolean.TRUE : Boolean.FALSE;
         values[4] = valid ? Boolean.TRUE : Boolean.FALSE;
         values[5] = immediate ? Boolean.TRUE : Boolean.FALSE;
         values[6] = immediateSet ? Boolean.TRUE : Boolean.FALSE;
         values[7] = saveAttachedState(context, validators);
         values[8] = saveAttachedState(context, validatorBinding);
         values[9] = saveAttachedState(context, valueChangeMethod);
         return values;
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         localValueSet = ((Boolean) values[1]).booleanValue();
         required = ((Boolean) values[2]).booleanValue();
         requiredSet = ((Boolean) values[3]).booleanValue();
         valid = ((Boolean) values[4]).booleanValue();
         immediate = ((Boolean) values[5]).booleanValue();
         immediateSet = ((Boolean) values[6]).booleanValue();
         List restoredValidators = (List) restoreAttachedState(context,
                 values[7]);
         if (restoredValidators != null) {
             if (validators != null) {
                 for (Iterator itr = restoredValidators.iterator(); itr
                         .hasNext();) {
                     validators.add(itr.next());
                 }
             } else {
                 validators = restoredValidators;
             }
         }
         validatorBinding = (MethodBinding) restoreAttachedState(context,
                 values[8]);
         valueChangeMethod = (MethodBinding) restoreAttachedState(context,
                 values[9]);
     }
 
     private void executeValidate(FacesContext context) {
         try {
             validate(context);
         } catch (RuntimeException e) {
             context.renderResponse();
             throw e;
         }
         renderResponseIfNotValid(context);
     }
 
     private Converter getConverterWithType(FacesContext context) {
         Converter converter = getConverter();
         if (converter != null) {
             return converter;
         }
         Class type = ComponentUtil_.getValueBindingType(this, "value");
         if (ComponentUtil_.isPerformNoConversion(type)) {
             return null;
         }
         try {
             return ComponentUtil_.createConverter(context, type);
         } catch (Exception ignore) {
             return null;
         }
     }
 
     private void renderResponseIfNotValid(FacesContext context) {
         if (!isValid()) {
             context.renderResponse();
         }
     }
 
     protected void validateFromAddedValidator(FacesContext context, Object value) {
         if (validators == null) {
             return;
         }
         for (Iterator itr = validators.iterator(); itr.hasNext();) {
             Validator validator = (Validator) itr.next();
             try {
                 validator.validate(context, this, value);
             } catch (ValidatorException e) {
                 handleValidationException(context, e);
             }
         }
     }
 
     protected void validateFromBinding(FacesContext context, Object value) {
         if (validatorBinding == null) {
             return;
         }
         try {
             validatorBinding.invoke(context, new Object[] { context, this,
                     value });
         } catch (EvaluationException e) {
             Throwable cause = e.getCause();
             if (cause instanceof ValidatorException) {
                 ValidatorException ve = (ValidatorException) e.getCause();
                 handleValidationException(context, ve);
             } else {
                 throw e;
             }
         }
     }
 
     protected void validateFromAnnotation(FacesContext context, Object value) {
         ValueBinding vb = getValueBinding("value");
         if (vb != null) {
             String expression = vb.getExpressionString();
             Validator validator = ValidatorResource.getValidator(expression);
             if (validator != null) {
                 try {
                     validator.validate(context, this, value);
                 } catch (ValidatorException e) {
                     handleValidationException(context, e);
                 }
             }
         }
     }
 
     protected void handleValidationException(FacesContext context,
             ValidatorException e) {
         setValid(false);
         FacesMessage message = e.getFacesMessage();
         if (message != null) {
             message.setSeverity(FacesMessage.SEVERITY_ERROR);
             context.addMessage(getClientId(context), message);
         }
     }
 }
