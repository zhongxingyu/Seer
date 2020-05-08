 package org.otherobjects.cms.validation;
 
 import java.util.Iterator;
 
 import org.otherobjects.cms.model.DynaNode;
 import org.otherobjects.cms.types.PropertyDef;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 import org.springmodules.validation.valang.ValangValidator;
 
 public class DynaNodeValidator implements Validator
 {
 
     private TypeService typeService;
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     @SuppressWarnings("unchecked")
     public boolean supports(Class clazz)
     {
         return DynaNode.class.isAssignableFrom(clazz);
     }
 
     public void validate(Object target, Errors errors)
     {
         Assert.isInstanceOf(DynaNode.class, target, "Can only validate DynaNodes");
 
         DynaNode valObj = (DynaNode) target;
         TypeDef typeDef = typeService.getType(valObj.getOoType());
         StringBuffer valangRules = new StringBuffer();
 
         for (Iterator<PropertyDef> it = typeDef.getProperties().iterator(); it.hasNext();)
         {
             PropertyDef propertyDef = it.next();
             Object value = errors.getFieldValue(propertyDef.getName());
             String fieldName = propertyDef.getName();
 
             if (propertyDef.isRequired())
                 ValidationUtils.rejectIfEmptyOrWhitespace(errors, fieldName, "field.required");
 
             if (propertyDef.getSize() > -1)
             {
                 if (value != null && value.toString().length() > propertyDef.getSize())
                     errors.rejectValue(fieldName, "field.valueTooLong");
             }
 
             // if we have a valang property, insert the fieldName into it and append it to the valang rules buffer
             if (StringUtils.hasText(propertyDef.getValang()))
             {
                valangRules.append(propertyDef.getValang().replaceAll("\\$THIS", fieldName));
             }
         }
 
         // if there were any valang rules create a valang validator from those
         if (valangRules.length() > 0)
         {
             ValangValidator val = new ValangValidator();
             val.setValang(valangRules.toString());
             try
             {
                 //FIXME this is not nice as it is implementation specific
                 val.afterPropertiesSet();
                 val.validate(target, errors);
             }
             catch (Exception e)
             {
                 // noop
             }
         }
 
     }
 
 }
