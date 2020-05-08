 package org.otherobjects.cms.dao;
 
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.jcr.GenericJcrDaoJackrabbit;
 import org.otherobjects.cms.model.DynaNode;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.validation.DynaNodeValidator;
 import org.springframework.util.Assert;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 
 public class DynaNodeDaoJackrabbit extends GenericJcrDaoJackrabbit<DynaNode> implements DynaNodeDao
 {
     private TypeService typeService;
     private DynaNodeValidator dynaNodeValidator;
 
     public DynaNodeDaoJackrabbit()
     {
         super(DynaNode.class);
     }
 
     public DynaNode create(String typeName)
     {
         Assert.notNull(typeName, "typeName must not be null.");
         return new DynaNode(typeService.getType(typeName));
     }
 
     public TypeService getTypeService()
     {
         return typeService;
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
 	@Override
 	public DynaNode save(DynaNode object, boolean validate) {
 		if(validate)
 		{
 			Errors errors = new BeanPropertyBindingResult(object, "target");
			dynaNodeValidator.validate(object, errors);
 			if(errors.getErrorCount() > 0)
 				throw new OtherObjectsException("DynaNode " + object.getLabel() + "couldn't be validated and therefore didn't get saved");
 		}
 		
 		return super.save(object, false);
 	}
 
 	public void setDynaNodeValidator(DynaNodeValidator dynaNodeValidator) {
 		this.dynaNodeValidator = dynaNodeValidator;
 	}
     
     
 }
