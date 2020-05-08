 package org.otherobjects.cms.validation;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.otherobjects.cms.beans.JcrBeanService;
 import org.otherobjects.cms.binding.BindService;
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
 
 /**
  * Validator is a simple {@link Validator } implementation to validate DynaNodes (usually happens after binding by {@link BindService} and must 
  * happen only after the DynaNode has been beanified by {@link JcrBeanService#createCustomDynaNodeClass(DynaNode)}).
  * Uses the following three properties specified in {@link PropertyDef} for validation:
  * <ul>
  * 	<li>{@link PropertyDef#isRequired()}</li>
  *  <li>{@link PropertyDef#getSize()}</li>
  *  <li>{@link PropertyDef#getValang()}</li>
  * </ul>
  * 
  * @author joerg
  */
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
             String fieldName = propertyDef.getName();
             Object value = errors.getFieldValue(fieldName);
             
             if(propertyDef.getCollectionType() != null && propertyDef.getCollectionType().equals(PropertyDef.LIST))
             {
             	if(propertyDef.getType().equals(PropertyDef.COMPONENT))
             	{
             		if(value != null && List.class.isAssignableFrom(value.getClass()))
             		{
             			int i = 0;
             			List<DynaNode> dynaNodeList = (List<DynaNode>) value;
             			for(DynaNode dynaNode: dynaNodeList)
             			{
            				validateComponent(fieldName + "[" + i + "]", dynaNode, errors);
                             i++;
             			}
             		}
             	}
             }
             else if (propertyDef.getType().equals(PropertyDef.COMPONENT))
             {
                validateComponent(fieldName, value, errors);
             }
             else
             {
 
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
                     valangRules.append(propertyDef.getValang().replaceAll("\\?", fieldName));
                 }
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
     
    private void validateComponent(String fieldName, Object dynaNode, Errors errors)
     {
     	Assert.isTrue(DynaNode.class.isAssignableFrom(dynaNode.getClass()), "Value of property " + fieldName + " is not a dynaNode. Perhaps there is a conflicting parameter in the request?");
     	try
         {
             errors.pushNestedPath(fieldName);
             ValidationUtils.invokeValidator(this, dynaNode, errors);
         }
         finally
         {
             errors.popNestedPath();
         }
     }
     
 }
