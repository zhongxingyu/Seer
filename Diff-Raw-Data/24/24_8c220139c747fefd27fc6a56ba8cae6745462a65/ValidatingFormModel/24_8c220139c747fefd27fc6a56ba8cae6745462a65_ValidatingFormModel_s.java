 /*
  * Copyright 2002-2004 the original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.springframework.binding.form.support;
 
 import java.beans.PropertyEditor;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.binding.MutablePropertyAccessStrategy;
 import org.springframework.binding.PropertyAccessStrategy;
 import org.springframework.binding.form.ValidationEvent;
 import org.springframework.binding.form.ValidationListener;
 import org.springframework.binding.value.PropertyEditorProvider;
 import org.springframework.binding.value.ValueChangeListener;
 import org.springframework.binding.value.ValueModel;
 import org.springframework.binding.value.support.TypeConverter;
 import org.springframework.binding.value.support.ValueModelWrapper;
 import org.springframework.rules.RulesProvider;
 import org.springframework.rules.constraint.bean.BeanPropertyConstraint;
 import org.springframework.rules.reporting.BeanValidationResultsCollector;
 import org.springframework.rules.reporting.PropertyResults;
 import org.springframework.rules.reporting.TypeResolvable;
 import org.springframework.util.Assert;
 import org.springframework.util.DefaultObjectStyler;
 
 /**
  * @author Keith Donald
  */
 public class ValidatingFormModel extends DefaultFormModel implements
         PropertyAccessStrategy {
     private Map validationErrors = new HashMap();
 
     private List validationListeners = new ArrayList();
 
     public ValidatingFormModel() {
     }
 
     public ValidatingFormModel(Object domainObject) {
         super(domainObject);
     }
 
     public ValidatingFormModel(ValueModel domainObjectHolder) {
         super(domainObjectHolder);
     }
 
     public ValidatingFormModel(
             MutablePropertyAccessStrategy domainObjectAccessStrategy) {
         super(domainObjectAccessStrategy);
     }
 
     public ValidatingFormModel(
             MutablePropertyAccessStrategy domainObjectAccessStrategy,
             boolean bufferChanges) {
         super(domainObjectAccessStrategy, bufferChanges);
     }
 
     public Object getPropertyValue(String propertyName) {
         return getValue(propertyName);
     }
 
     public Object getDomainObject() {
         return this;
     }
 
     public boolean getHasErrors() {
         return this.validationErrors.size() > 0;
     }
 
     public Map getErrors() {
         return Collections.unmodifiableMap(validationErrors);
     }
 
     protected void doClearErrors() {
         Iterator it = this.validationErrors.keySet().iterator();
         boolean hadErrorsBefore = getHasErrors();
         while (it.hasNext()) {
             BeanPropertyConstraint exp = (BeanPropertyConstraint)it.next();
             it.remove();
             fireConstraintSatisfied(exp);
         }
         Assert.isTrue(getHasErrors() == false);
         if (hadErrorsBefore) {
             firePropertyChange(HAS_ERRORS_PROPERTY, true, false);
         }
     }
 
     protected void doValidate() {
         for (Iterator i = valueModelIterator(); i.hasNext();) {
             ValidatingFormValueModel vm = (ValidatingFormValueModel)i.next();
             vm.validate();
         }
     }
 
     public int getFormPropertiesWithErrorsCount() {
         return validationErrors.size();
     }
 
     public int getTotalErrorCount() {
         Iterator it = validationErrors.values().iterator();
         int totalErrors = 0;
         while (it.hasNext()) {
             totalErrors += ((PropertyResults)it.next()).getViolatedCount();
         }
         return totalErrors;
     }
 
     public void addValidationListener(ValidationListener validationListener) {
         if (validationListener != null) {
             validationListeners.add(validationListener);
         }
     }
 
     public void removeValidationListener(ValidationListener validationListener) {
         if (validationListener != null) {
             validationListeners.remove(validationListener);
         }
     }
 
     protected ValueModel preProcessNewFormValueModel(
             String domainObjectProperty, ValueModel formValueModel) {
         if (!(formValueModel instanceof TypeConverter)) {
             if (getFormObject() instanceof PropertyEditorProvider) {
                 PropertyEditorProvider provider = (PropertyEditorProvider)getFormObject();
                PropertyEditor editor = provider
                        .getPropertyEditor(domainObjectProperty);
                formValueModel = installTypeConverter(formValueModel,
                        domainObjectProperty, editor);
             }
            else {
                PropertyEditor editor = getPropertyAccessStrategy()
                        .findCustomEditor(domainObjectProperty);
                formValueModel = installTypeConverter(formValueModel,
                        domainObjectProperty, editor);
             }
         }
         return new ValidatingFormValueModel(domainObjectProperty,
                 formValueModel, getValidationRule(domainObjectProperty));
     }
 
     private ValueModel installTypeConverter(ValueModel formValueModel,
             String domainObjectProperty, PropertyEditor editor) {
         if (editor != null) {
             TypeConverter converter = new TypeConverter(formValueModel, editor);
             if (logger.isDebugEnabled()) {
                 logger.debug("Installed type converter '" + converter
                         + "' with editor '" + editor + "' for property '"
                         + domainObjectProperty + "'");
             }
             return converter;
         }
         else {
             return formValueModel;
         }
     }
 
     protected void postProcessNewFormValueModel(String domainObjectProperty,
             ValueModel valueModel) {
         // trigger validation to catch initial form errors
         if (valueModel instanceof ValidatingFormValueModel && isEnabled()) {
             ((ValidatingFormValueModel)valueModel).validate();
         }
     }
 
     protected BeanPropertyConstraint getValidationRule(
             String domainObjectProperty) {
         BeanPropertyConstraint constraint = null;
         //@TODO if form object changes, rules aren't updated...introduces
         // subtle bugs...
         // ... for rules dependent on instance...
         if (getFormObject() instanceof RulesProvider) {
             constraint = ((RulesProvider)getFormObject())
                     .getRules(domainObjectProperty);
         }
         else {
             if (getRulesSource() != null) {
                 constraint = getRulesSource().getRules(getFormObjectClass(),
                         domainObjectProperty);
             }
             else {
                 logger
                         .info("No rules source has been configured; "
                                 + "please set a valid reference to enable rules-based validation.");
             }
         }
         return constraint;
     }
 
     private class ValidatingFormValueModel extends ValueModelWrapper {
         private BeanPropertyConstraint setterConstraint;
 
         private BeanPropertyConstraint validationRule;
 
         private boolean valueIsSetting;
 
         public ValidatingFormValueModel(String domainObjectProperty,
                 ValueModel model, BeanPropertyConstraint validationRule) {
             super(model);
             this.setterConstraint = new ValueSetterConstraint(
                     getWrappedModel(), domainObjectProperty);
             this.validationRule = validationRule;
             addValueChangeListener(new ValueChangeListener() {
                 public void valueChanged() {
                     // validatee any changes that didn't occur as the result
                     // of an explicit set(value) call...maybe value was updated
                     // underneath via another mechanism... this is kinda
                     // tricky...
                     if (!valueIsSetting && isEnabled()) {
                         validate();
                     }
                 }
             });
         }
 
         public void setValue(Object value) {
             try {
                 valueIsSetting = true;
                 if (!setterConstraint.test(value)) {
                     if (isEnabled()) {
                         PropertyResults results = new PropertyResults(
                                 setterConstraint.getPropertyName(), value,
                                 setterConstraint);
                         constraintViolated(setterConstraint, results);
                     }
                 }
                 else {
                     if (isEnabled()) {
                         constraintSatisfied(setterConstraint);
                         // we validate after a set attempt
                         validate();
                     }
                 }
             }
             finally {
                 valueIsSetting = false;
             }
         }
 
         public void validate() {
             if (validationRule == null) { return; }
             if (logger.isDebugEnabled()) {
                 logger.debug("[Validating domain object property '"
                         + validationRule.getPropertyName() + "']");
             }
             BeanValidationResultsCollector collector = new BeanValidationResultsCollector(
                     ValidatingFormModel.this);
             PropertyResults results = (PropertyResults)collector
                     .collectPropertyResults(validationRule);
             if (results == null) {
                 constraintSatisfied(validationRule);
             }
             else {
                 constraintViolated(validationRule, results);
             }
         }
 
     }
 
     private class ValueSetterConstraint implements BeanPropertyConstraint,
             TypeResolvable {
         private ValueModel valueModel;
 
         private String property;
 
         private String type = "typeMismatch";
 
         public ValueSetterConstraint(ValueModel valueModel, String property) {
             this.valueModel = valueModel;
             this.property = property;
         }
 
         public String getType() {
             return type;
         }
 
         public String getPropertyName() {
             return property;
         }
 
         public boolean test(Object value) {
             //@TODO this error handling needs work - message source resolvable?
             try {
                 if (logger.isDebugEnabled()) {
                     Class valueClass = (value != null ? value.getClass() : null);
                     logger.debug("Setting value to convert/validate '" + value
                             + "', class=" + valueClass);
                 }
                 valueModel.setValue(value);
                 return true;
             }
             catch (NullPointerException e) {
                 logger.warn("Null pointer exception occured setting value", e);
                 type = "required";
                 return false;
             }
             catch (IllegalArgumentException e) {
                 if (logger.isInfoEnabled()) {
                     logger
                             .info("Illegal argument exception occured setting value");
                 }
                 type = "typeMismatch";
                 return false;
             }
             catch (Exception e) {
                 logger.warn("Exception occured setting value", e);
                 type = "unknown";
                 return false;
             }
         }
     }
 
     protected void constraintSatisfied(BeanPropertyConstraint exp) {
         if (logger.isDebugEnabled()) {
             logger.debug("Value constraint '" + exp
                     + "' [satisfied] for value model '" + exp.getPropertyName()
                     + "']");
         }
         if (validationErrors.containsKey(exp)) {
             validationErrors.remove(exp);
             fireConstraintSatisfied(exp);
             if (!getHasErrors()) {
                 firePropertyChange(HAS_ERRORS_PROPERTY, true, false);
             }
         }
         if (logger.isDebugEnabled()) {
             logger.debug("Number of errors on form is now "
                     + validationErrors.size() + "; errors="
                     + DefaultObjectStyler.call(validationErrors));
         }
     }
 
     private void fireConstraintSatisfied(BeanPropertyConstraint constraint) {
         Iterator it = validationListeners.iterator();
         while (it.hasNext()) {
             ((ValidationListener)it.next())
                     .constraintSatisfied(new ValidationEvent(this, constraint));
         }
     }
 
     protected void constraintViolated(BeanPropertyConstraint exp,
             PropertyResults results) {
         if (logger.isDebugEnabled()) {
             logger.debug("Value constraint '" + exp + "' [rejected], results='"
                     + results + "']");
         }
         //@TODO should change publisher should only publish on results changes
         // this means results needs business identity...
         boolean hadErrorsBefore = getHasErrors();
         validationErrors.put(exp, results);
         fireConstraintViolated(exp, results);
         if (!hadErrorsBefore) {
             firePropertyChange(HAS_ERRORS_PROPERTY, false, true);
         }
         if (logger.isDebugEnabled()) {
             logger.debug("Number of errors on form is now "
                     + validationErrors.size() + "; errors="
                     + DefaultObjectStyler.call(validationErrors));
         }
     }
 
     private void fireConstraintViolated(BeanPropertyConstraint constraint,
             PropertyResults results) {
         Iterator it = validationListeners.iterator();
         while (it.hasNext()) {
             ((ValidationListener)it.next())
                     .constraintViolated(new ValidationEvent(this, constraint,
                             results));
         }
     }
 
 }
