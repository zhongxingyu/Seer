 package org.otherobjects.cms.validation;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.types.PropertyDef;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 import org.springmodules.validation.valang.ValangValidator;
 
 /**
  * Validates objects using their TypeDef.
  *
  */
 @SuppressWarnings("unchecked")
 public class TypeDefConfiguredValidator implements Validator
 {
     private final Logger logger = LoggerFactory.getLogger(TypeDefConfiguredValidator.class);
     
     @Resource
     private TypeService typeService;
     
     public boolean supports(Class clazz)
     {
         return typeService.getType(clazz) != null || BaseNode.class.isAssignableFrom(clazz);
     }
 
     public void validate(Object target, Errors errors)
     {
         TypeDef typeDef;
         if (target instanceof BaseNode)
             typeDef = ((BaseNode)target).getTypeDef();
         else
             typeDef = typeService.getType(target.getClass());
         
         StringBuffer valangRules = new StringBuffer();
 
         for (PropertyDef propertyDef : typeDef.getProperties())
         {
             String fieldName = propertyDef.getFieldName();
             Object value = errors.getFieldValue(fieldName);
 
             if (propertyDef.getType().equals(PropertyDef.LIST))
             {
                 String collectionElementType = propertyDef.getCollectionElementType();
                 Assert.isTrue(collectionElementType != null, "If this property is a collection the collectionElementType needs to have been set");
                 if (collectionElementType.equals(PropertyDef.COMPONENT))
                 {
                     if (value != null && List.class.isAssignableFrom(value.getClass()))
                     {
                         int i = 0;
                         List<Object> objectList = (List<Object>) value;
                         for (Object object : objectList)
                         {
                             validateComponent(fieldName + "[" + i + "]", propertyDef, object, errors);
                             i++;
                         }
                     }
                 }
             }
             else if (propertyDef.getType().equals(PropertyDef.COMPONENT))
             {
                 validateComponent(fieldName, propertyDef, value, errors);
             }
             else
             {
 
                 if (propertyDef.isRequired())
                     ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, "field.required");
 
                 int size = propertyDef.getSize();
                if (size > -1 && value != null)
                 {
                     int actualSize = value.toString().length();
                     if (value != null && actualSize > size)
                         errors.rejectValue(fieldName, "field.value.too.long", new Object[]{size, actualSize}, "Value too long. Must be less that {0}");
                 }
 
                 // if we have a valang property, insert the fieldName into it and append it to the valang rules buffer
                 if (StringUtils.hasText(propertyDef.getValang()))
                 {
                     // Only replace first one as a convenience. Note ? may appear in function arguments esp in matches(regexp)
                     valangRules.append(propertyDef.getValang().replaceFirst("\\?", fieldName));
                 }
             }
         }
 
         // if there were any valang rules create a valang validator from those
         if (valangRules.length() > 0)
         {
             try
             {
                 //FIXME this is not nice as it is implementation specific
                 ValangValidator val = new ValangValidator();
                 String rules = valangRules.toString();
                // if(logger.isDebugEnabled())
                     logger.debug("Valang rules for " + typeDef.getName() + ": " + rules);
                 val.setValang(rules);
                 val.afterPropertiesSet();
                 val.validate(target, errors);
             }
             catch (Throwable e)
             {
                 throw new OtherObjectsException("Incorrect validation rules on: "+ typeDef.getName(), e);
             }
         }
 
     }
 
     private void validateComponent(String fieldName, PropertyDef propertyDef, Object valueObject, Errors errors)
     {
         if (valueObject == null)
         {
             if (propertyDef.isRequired())
                 errors.rejectValue(fieldName, "field.required");
             return;
         }
         Assert.notNull(typeService.getType(valueObject.getClass()), "No TypeDef for valueObject of property " + fieldName + ". Perhaps there is a conflicting parameter in the request?");
         try
         {
             errors.pushNestedPath(fieldName);
             ValidationUtils.invokeValidator(this, valueObject, errors);
         }
         finally
         {
             errors.popNestedPath();
         }
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
 }
