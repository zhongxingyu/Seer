 package com.amee.platform.resource.itemdefinition;
 
 import com.amee.base.validation.ValidationHelper;
 import com.amee.domain.data.ItemDefinition;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.validation.Validator;
 
 import java.util.HashSet;
 import java.util.Set;
 
 @Service
 @Scope("prototype")
 public class ItemDefinitionValidationHelper extends ValidationHelper {
 
     @Autowired
     private ItemDefinitionValidator itemDefinitionValidator;
 
     private ItemDefinition itemDefinition;
     private Set<String> allowedFields;
 
     @Override
     public Object getObject() {
         return itemDefinition;
     }
 
     @Override
     protected Validator getValidator() {
         return itemDefinitionValidator;
     }
 
     @Override
     public String getName() {
         return "itemDefinition";
     }
 
     @Override
     public String[] getAllowedFields() {
         if (allowedFields == null) {
             allowedFields = new HashSet<String>();
             allowedFields.add("name");
             allowedFields.add("drillDown");
            allowedFields.add("usages");
         }
         return allowedFields.toArray(new String[]{});
     }
 
     public ItemDefinition getItemDefinition() {
         return itemDefinition;
     }
 
     public void setItemDefinition(ItemDefinition itemDefinition) {
         this.itemDefinition = itemDefinition;
     }
 }
