 package com.amee.platform.resource.itemdefinition;
 
 import com.amee.base.validation.BaseValidator;
 import com.amee.base.validation.ValidationSpecification;
 import com.amee.domain.data.ItemDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 @Service
 @Scope("prototype")
 public class ItemDefinitionValidator extends BaseValidator {
 
     public ItemDefinitionValidator() {
         super();
         addName();
         addDrillDown();
         addUsages();
     }
 
     public boolean supports(Class clazz) {
         return ItemDefinition.class.isAssignableFrom(clazz);
     }
 
     private void addName() {
         add(new ValidationSpecification()
                 .setName("name")
                 .setMinSize(ItemDefinition.NAME_MIN_SIZE)
                 .setMaxSize(ItemDefinition.NAME_MAX_SIZE)
         );
     }
 
     private void addDrillDown() {
         add(new ValidationSpecification()
                 .setName("drillDown")
                 .setMinSize(ItemDefinition.DRILL_DOWN_MIN_SIZE)
                 .setMaxSize(ItemDefinition.DRILL_DOWN_MAX_SIZE)
                 .setAllowEmpty(true)
         );
     }
 
     private void addUsages() {
         add(new ValidationSpecification()
                .setName("usagesString")
                 .setMinSize(ItemDefinition.USAGES_MIN_SIZE)
                 .setMaxSize(ItemDefinition.USAGES_MAX_SIZE)
                 .setAllowEmpty(true)
         );
     }
 }
