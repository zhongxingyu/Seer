 package org.eclipse.jst.jsf.common.runtime.internal.model.behavioural;
 
 import java.util.List;
 
import org.eclipse.jst.jsf.common.runtime.internal.model.ViewObject;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ValidatorDecorator;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ValueChangeListenerDecorator;
 
 /**
  * A design time analog for the JSF runtime EditableValueHolder interface.
  * 
  * @author cbateman
  * 
  */
 public interface IEditableValueHolderInfo extends IValueHolderInfo {
     /**
      * @return the flag indicating whether the value of the value holder should
      *         be evaluated in the apply request values phase instead of the
      *         process validations phase
      */
     boolean isImmediate();
 
     /**
      * @return true if the value has been set
      */
     boolean isLocalSetValue();
 
     /**
      * @return true if validation should consider this field required (must be
      *         non-empty).
      */
     boolean isRequired();
 
     /**
      * @return the submitted, unconverted value of the component.
      */
     Object getSubmittedValue();
 
     /**
      * @return false if validation has determined that the value property of
      *         this component is not valid.
      */
     boolean isValid();
 
     /**
      * <b> NOTE: this method is deprecated in the runtime spec and exists for
      * backward compatibility with JSF 1.1. You should avoid using it except in
      * conjunction with JSF 1.1 tooling support. This method will be deprecated
      * once the runtime spec removes the underlying API</b>
      * 
      * @return a method binding expression that refers to a validator method
      */
     String getValidator();
 
     /**
      * <b> NOTE: this method is deprecated in the runtime spec and exists for
      * backward compatibility with JSF 1.1. You should avoid using it except in
      * conjunction with JSF 1.1 tooling support. This method will be deprecated
      * once the runtime spec removes the underlying API</b>
      * 
      * @return a method binding expression that refers to a value change
      *         listener
      */
     String getValueChangeListener();
 
     /**
      * Add validator to the list of validators for this component. Should be
      * equivalent to
     * {@link ViewObject#addDecorator(org.eclipse.jst.jsf.common.runtime.internal.model.decorator.Decorator, Class)}.
      * 
      * @param validator
      */
     void addValidator(ValidatorDecorator validator);
 
     /**
      * @return the list of validators for this component. Should be considered
      *         unmodifiable (may throw exception).
      */
     List/* <ValidatorDecorator> */getValidators();
 
     /**
      * Add the value change listener to this component.
      * 
      * @param valueChangeListenerInfo
      */
     void addValueChangeListener(
             ValueChangeListenerDecorator valueChangeListenerInfo);
 
     /**
      * @return the list of value change listeners. Should be considered
      *         unmodifiable (may throw exception).
      */
     List/* <ValueChangeListenerDecorator> */getValueChangeListeners();
 }
