 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.romaframework.aspect.validation;
 
 import java.util.Iterator;
 
 import org.romaframework.aspect.validation.feature.ValidationActionFeatures;
 import org.romaframework.aspect.validation.feature.ValidationFieldFeatures;
 import org.romaframework.core.Roma;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.SchemaActionListener;
 import org.romaframework.core.schema.FeatureRegistry;
 import org.romaframework.core.schema.FeatureType;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaElement;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaHelper;
 
 public class BasicValidationModule extends ValidationAspectAbstract implements SchemaActionListener {
 
 	public BasicValidationModule() {
 		Controller.getInstance().registerListener(SchemaActionListener.class, this);
 	}
 
 	public void validate(Object iObject) {
 		MultiValidationException exc = validateAndCollectExceptions(iObject);
 		if (exc != null && !exc.isEmpty())
 			throw exc;
 	}
 
 	/**
 	 * Validate any pojo.
 	 * 
 	 * @param pojo
 	 * @return a MultiValidationException object with the sum of exceptions, if any
 	 */
 	public MultiValidationException validateAndCollectExceptions(Object pojo) throws ValidationException, MultiValidationException {
 		MultiValidationException multiException = new MultiValidationException();
 		validate(pojo, Roma.schema().getSchemaClass(pojo), multiException);
 		return multiException;
 	}
 
 	protected void validate(Object pojo, SchemaClassDefinition schemaObject, MultiValidationException multiException) {
 		if (pojo == null)
 			return;
 
 		SchemaClassDefinition schemaInstance = Roma.session().getSchemaObject(pojo);
 
 		if (schemaInstance != null)
 			validateFields(pojo, multiException, schemaInstance);
 		else {
 			if (SchemaHelper.isMultiValueObject(pojo)) {
 				// VALIDATE EVERY SINGLE ITEM OF COLLECTION/ARRAY/MAP
 				for (Object o : SchemaHelper.getObjectArrayForMultiValueObject(pojo)) {
 					validate(o, schemaInstance, multiException);
 				}
 			}
 		}
 
 		if (pojo != null && CustomValidation.class.isAssignableFrom(pojo.getClass())) {
 			try {
 				Roma.context().create();
 				// CALL CUSTOM VALIDATION ROUTINE
 				((CustomValidation) pojo).validate();
 			} catch (MultiValidationException me) {
 				// HANDLE MULTI VALIDATION EXCEPTION
 				for (Iterator<ValidationException> it = me.getDetailIterator(); it.hasNext();) {
 					ValidationException ve = it.next();
 					handleValidationException(ve.getObject(), multiException, ve.getFieldName(), ve.getMessage(), ve.getRefValue());
 				}
 			} catch (ValidationException ve) {
 				handleValidationException(ve.getObject(), multiException, ve.getFieldName(), ve.getMessage(), ve.getRefValue());
 			} catch (Exception ex) {
 				handleValidationException(pojo, multiException, null, ex.toString(), null);
 			} finally {
 				Roma.context().destroy();
 			}
 		}
 	}
 
 	protected void validateFields(Object pojo, MultiValidationException multiException, SchemaClassDefinition schemaInstance) {
 		SchemaField fieldInfo;
 
 		for (Iterator<SchemaField> it = schemaInstance.getFieldIterator(); it.hasNext();) {
 			// SET FIELD'S COMPONENT
 			fieldInfo = it.next();
 
 			if (!fieldInfo.getFeature(ValidationFieldFeatures.ENABLED))
 				continue;
 
 			Object fieldValue = SchemaHelper.getFieldValue(fieldInfo, pojo);
 
 			if (SchemaHelper.isMultiValueObject(fieldValue)) {
 				validateFieldComponent(pojo, multiException, fieldInfo, fieldValue);
 
 				// VALIDATE EVERY SINGLE ITEM OF COLLECTION/ARRAY/MAP
 				for (Object o : SchemaHelper.getObjectArrayForMultiValueObject(fieldValue)) {
 					validate(o, Roma.schema().getSchemaClass(o), multiException);
 				}
 			} else if (fieldValue != null && !SchemaHelper.isJavaType(fieldValue.getClass())) {
 
 				// EMBEDDED FORM: CALL ITS VALIDATION
 				if (fieldValue instanceof CustomValidation) {
 					validate(fieldValue, Roma.schema().getSchemaClass(fieldValue), multiException);
 				} else {
 					validateFields(fieldValue, multiException, Roma.schema().getSchemaClass(fieldValue));
 				}
 			} else
 				validateFieldComponent(pojo, multiException, fieldInfo, fieldValue);
 		}
 	}
 
 	private void validateFieldComponent(Object pojo, MultiValidationException multiException, SchemaField fieldInfo, Object fieldValue) {
 
 		String name = fieldInfo.getName();
 
 		final boolean required = fieldInfo.getFeature(ValidationFieldFeatures.REQUIRED);
 		final Integer annotationMin = fieldInfo.getFeature(ValidationFieldFeatures.MIN);
 		final Integer annotationMax = fieldInfo.getFeature(ValidationFieldFeatures.MAX);
 		Class<?> fieldClass = (Class<?>) fieldInfo.getClassInfo().getLanguageType();
 
 		if (String.class.isAssignableFrom(fieldClass))
 			validateString(pojo, multiException, fieldInfo, (String) fieldValue, name, required, annotationMin, annotationMax);
 		else if (Number.class.isAssignableFrom(fieldClass))
 			validateNumber(pojo, multiException, (Number) fieldValue, name, required, annotationMin, annotationMax);
 		else if (SchemaHelper.isMultiValueObject(fieldValue))
 			validateMultiValue(pojo, multiException, fieldValue, name, required, annotationMin, annotationMax, SchemaHelper.getSizeForMultiValueObject(fieldValue), fieldInfo);
 	}
 
 	public void validateNumber(Object pojo, MultiValidationException iMultiException, Number fieldValue, String name, boolean required, Integer annotationMin, Integer annotationMax) {
 		if (required && fieldValue == null)
 			handleValidationException(pojo, iMultiException, name, "$validation.required", null);
 
 		if (annotationMin != null && fieldValue != null && fieldValue.intValue() < annotationMin)
 			handleValidationException(pojo, iMultiException, name, "$validation.minLength", String.valueOf(annotationMin));
 
 		if (annotationMax != null && fieldValue != null && fieldValue.intValue() > annotationMax)
 			handleValidationException(pojo, iMultiException, name, "$validation.maxLength", String.valueOf(annotationMax));
 	}
 
 	@SuppressWarnings("unchecked")
 	public void validateMultiValue(Object pojo, MultiValidationException iMultiException, Object fieldValue, String name, boolean required, Integer annotationMin,
 			Integer annotationMax, int length, SchemaElement iElement) {
 		if (required) {
 			// TODO: RESOLVE THIS DEPENDENCY IN MORE FAIR WAY
 			final String selectionField = (String) iElement.getFeature(FeatureRegistry.getFeature("view", FeatureType.FIELD, "selectionField"));
 
 			// CHECK IF THE SELECTION IS NULL
 			if (fieldValue == null || selectionField == null || SchemaHelper.getFieldValue(pojo, selectionField) == null)
 				handleValidationException(pojo, iMultiException, name, "$validation.required", null);
 		}
 
 		if (annotationMin != null && length < annotationMin)
 			handleValidationException(pojo, iMultiException, name, "$validation.minLength", String.valueOf(annotationMin));
 
 		if (annotationMax != null && length > annotationMax)
 			handleValidationException(pojo, iMultiException, name, "$validation.maxLength", String.valueOf(annotationMax));
 	}
 
 	public void validateString(Object pojo, MultiValidationException iMultiException, SchemaField fieldInfo, Object fieldValue, String name, boolean required, Integer annotationMin,
 			Integer annotationMax) {
 		String stringValue = (String) fieldValue;
 		if (required && (stringValue == null || stringValue.length() == 0))
 			handleValidationException(pojo, iMultiException, name, "$validation.required", null);
 
		if (annotationMin != null && stringValue != null && stringValue.length() < annotationMin)
 			handleValidationException(pojo, iMultiException, name, "$validation.minLength", String.valueOf(annotationMin));
 
 		if (annotationMax != null && stringValue != null && stringValue.length() > annotationMax)
 			handleValidationException(pojo, iMultiException, name, "$validation.maxLength", String.valueOf(annotationMax));
 	}
 
 	protected void handleValidationException(Object pojo, MultiValidationException multiException, String iFieldName, String iRule, String iRefValue) throws ValidationException {
 		ValidationException e = new ValidationException(pojo, iFieldName, iRule, iRefValue);
 		multiException.addException(e);
 	}
 
 	public void onAfterAction(Object iContent, SchemaAction iAction, Object returnedValue) {
 
 	}
 
 	public boolean onBeforeAction(Object iContent, SchemaAction iAction) {
 		// INVOKE THE ACTION
 		Boolean val = (Boolean) iAction.getFeature(ValidationActionFeatures.VALIDATE);
 		if (val != null && val)
 			validate(iContent);
 
 		return true;
 	}
 
 	public void onExceptionAction(Object iContent, SchemaAction iAction, Exception exception) {
 	}
 
 }
