 package com.amee.platform.resource.profileitem.v_3_6;
 
 import com.amee.base.domain.Since;
 import com.amee.base.validation.BaseValidator;
 import com.amee.base.validation.ValidationSpecification;
 import com.amee.domain.DataItemService;
 import com.amee.domain.Metadata;
 import com.amee.domain.ProfileItemService;
 import com.amee.domain.data.ItemValueDefinition;
 import com.amee.domain.item.profile.ProfileItem;
 import com.amee.domain.item.profile.ProfileItemTextValue;
 import com.amee.platform.resource.StartEndDateEditor;
 import com.amee.platform.resource.itemvaluedefinition.ItemValueEditor;
 import com.amee.platform.resource.profileitem.ProfileItemResource;
 import com.amee.platform.science.AmountUnit;
 import com.amee.platform.science.StartEndDate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.validation.Errors;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 @Service
 @Scope("prototype")
 @Since("3.6.0")
 public class ProfileItemValidator_3_6_0 extends BaseValidator implements ProfileItemResource.ProfileItemValidator {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     ProfileItemService profileItemService;
 
     protected ProfileItem profileItem;
     protected Set<String> allowedFields = new HashSet<String>();
 
     // IS0 8601 duration. (https://secure.wikimedia.org/wikipedia/en/wiki/ISO_8601#Durations)
     private static final String DURATION_PATTERN_STRING = "^P\\d*Y?\\d*M?\\d*D?T?\\d*H?\\d*M?\\d*S?";
 
     @Override
     public void initialise() {
         addName();
         addDates();
         addValues();
         addNote();
         addUnit();
         addPerUnit();
     }
     
     @Override
     public String getName() {
         return "profileItem";
     }
 
     @Override
     public ProfileItem getObject() {
         return profileItem;
     }
 
     @Override
     public void setObject(ProfileItem profileItem) {
         this.profileItem = profileItem;
     }
 
     /**
      * Setter used by unit tests.
      *
      * @param profileItemService a (probably mocked) ProfileItemService.
      */
     @Override
     public void setProfileItemService(ProfileItemService profileItemService) {
         this.profileItemService = profileItemService;
     }
 
     @Override
     public boolean supports(Class<?> clazz) {
         return ProfileItem.class.isAssignableFrom(clazz);
     }
 
     @Override
     public String[] getAllowedFields() {
         return allowedFields.toArray(new String[allowedFields.size()]);
     }
 
     /**
      * Override validate to perform global object validation.
      *
      * @param object to validate
      * @param errors to store validation errors
      */
     @Override
     public void validate(Object object, Errors errors) {
         super.validate(object, errors);
 
         // Check for duplicates (as long as we don't have errors already)
         if (!errors.hasErrors() && !profileItemService.isUnique(profileItem)) {
             errors.reject("duplicate");
         }
     }
 
     protected void addName() {
         allowedFields.add("name");
         add(new ValidationSpecification()
             .setName("name")
             .setMaxSize(ProfileItem.NAME_MAX_SIZE)
             .setAllowEmpty(true));
     }
 
     protected void addDates() {
 
         // Start date
         allowedFields.add("startDate");
         add(Date.class, "startDate", new StartEndDateEditor(new Date()));
         add(new ValidationSpecification()
             .setName("startDate")
             .setAllowEmpty(true)
             .setCustomValidation(
                 new ValidationSpecification.CustomValidation() {
                     @Override
                     public int validate(Object object, Object value, Errors errors) {
                         ProfileItem thisProfileItem = (ProfileItem) object;
                         if (thisProfileItem != null) {
 
                             // Date must be in allowed range.
                             if (thisProfileItem.getStartDate().compareTo(DataItemService.EPOCH) <= 0) {
                                 errors.rejectValue("startDate", "epoch.startDate");
                             }
                             if (thisProfileItem.getStartDate().compareTo(DataItemService.Y2038) >= 0) {
                                 errors.rejectValue("startDate", "end_of_epoch.startDate");
                             }
                         }
                         return ValidationSpecification.CONTINUE;
                     }
                 }
             )
         );
 
         // End date
         allowedFields.add("endDate");
         add(Date.class, "endDate", new StartEndDateEditor(null));
         add(new ValidationSpecification()
             .setName("endDate")
             .setAllowEmpty(true)
             .setCustomValidation(
                 new ValidationSpecification.CustomValidation() {
                     @Override
                     public int validate(Object object, Object value, Errors errors) {
                         ProfileItem thisProfileItem = (ProfileItem) object;
                         if (thisProfileItem != null && thisProfileItem.getEndDate() != null) {
 
                             // End date must be after start date
                             if (thisProfileItem.getEndDate().before(thisProfileItem.getStartDate())) {
                                 errors.rejectValue("endDate", "end_before_start.endDate");
                             }
 
                             // Date must be in allowed range (don't need to check < EPOCH as previous test will catch)
                             if (thisProfileItem.getEndDate().compareTo(DataItemService.Y2038) >= 0) {
                                 errors.rejectValue("endDate", "end_of_epoch.endDate");
                             }
                         }
                         return ValidationSpecification.CONTINUE;
                     }
                 }
             )
         );
         
         // Duration (period)
         allowedFields.add("duration");
         add(new ValidationSpecification()
             .setName("duration")
             .setAllowEmpty(true)
             .setFormat(DURATION_PATTERN_STRING)
             .setCustomValidation(
                 new ValidationSpecification.CustomValidation() {
                     @Override
                     public int validate(Object object, Object value, Errors errors) {
                         ProfileItem thisProfileItem = (ProfileItem) object;
                         if (thisProfileItem != null && thisProfileItem.getDuration() != null) {
                             StartEndDate endDate = thisProfileItem.getStartDate().plus((String) value);
 
                             // End date must be after start date
                             if (endDate.before(thisProfileItem.getStartDate())) {
                                 errors.rejectValue("duration", "end_before_start.endDate");
                             }
 
                             // Date must be in allowed range.
                             if (endDate.compareTo(DataItemService.Y2038) >= 0) {
                                 errors.rejectValue("duration", "end_of_epoch.endDate");
                             }
 
                             thisProfileItem.setEndDate(endDate);
                         }
                         return ValidationSpecification.CONTINUE;
                     }
                 }
             ));
     }
     
     protected void addValues() {
         for (ItemValueDefinition ivd : profileItem.getItemDefinition().getActiveItemValueDefinitions()) {
             if (ivd.isFromProfile()) {
                 String paramName = "values." + ivd.getPath();
                 
                 // Allow this field.
                 allowedFields.add(paramName);
                 
                 if (ivd.isDouble()) {
                     
                     // Validation spec.
                     add(new ValidationSpecification()
                         .setName(paramName)
                         .setDoubleNumber(true)
                         .setAllowEmpty(true));
                     
                     // Property editor.
                     add(Double.class, paramName, new ItemValueEditor(ivd));
                 } else if (ivd.isInteger()) {
                     add(new ValidationSpecification()
                         .setName(paramName)
                         .setIntegerNumber(true)
                         .setAllowEmpty(true));
                     
                     add(Integer.class, paramName, new ItemValueEditor(ivd));
                 } else {
                     add(new ValidationSpecification()
                         .setName(paramName)
                         .setMaxSize(ProfileItemTextValue.VALUE_SIZE)
                         .setAllowEmpty(true));
                     
                     add(String.class, paramName, new ItemValueEditor(ivd));
                 }
             }
         }
     }
     
     protected void addNote(){
     	allowedFields.add("note");
     	add(new ValidationSpecification()
     		.setName("note")
     		.setMaxSize(Metadata.VALUE_MAX_SIZE)
     		.setAllowEmpty(true));
     }
 
     protected void addUnit() {
         for (ItemValueDefinition ivd : profileItem.getItemDefinition().getActiveItemValueDefinitions()) {
             if (ivd.isFromProfile()) {
                 final String unitName = "units." + ivd.getPath();
 
                 // Allow this field.
                 allowedFields.add(unitName);
                 add(new ValidationSpecification()
                     .setName(unitName)
                     .setAllowEmpty(true)
                     .setCustomValidation(
                         new ValidationSpecification.CustomValidation() {
                             @Override
                             public int validate(Object object, Object value, Errors errors) {
                                 
                                 String unit = (String) value;
 
                                 // Ensure unit is valid
                                 if (unit != null) {
                                     try {
                                         AmountUnit.valueOf(unit);
                                     } catch (IllegalArgumentException e) {
                                         errors.rejectValue(unitName, "format");
                                     }
                                 }
 
                                 return ValidationSpecification.CONTINUE;
                             }
                         }
                     )
                 );
             }
         }
     }
     
     protected void addPerUnit() {
         for (ItemValueDefinition ivd : profileItem.getItemDefinition().getActiveItemValueDefinitions()) {
             if (ivd.isFromProfile()) {
                 final String perUnitName = "perUnits." + ivd.getPath();
 
                 // Allow this field
                 allowedFields.add(perUnitName);
                 add(new ValidationSpecification()
                     .setName(perUnitName)
                     .setAllowEmpty(true)
                     .setCustomValidation(
                         new ValidationSpecification.CustomValidation() {
                             @Override
                             public int validate(Object object, Object value, Errors errors) {
 
                                 String perUnit = (String) value;
 
                                 // Ensure perUnit is valid
                                 if (perUnit != null) {
                                     try {
                                         AmountUnit.valueOf(perUnit);
                                     } catch (IllegalArgumentException e) {
                                         errors.rejectValue(perUnitName, "format");
                                     }
                                 }
 
                                 return ValidationSpecification.CONTINUE;
                             }
                         }
                     )
                 );
             }
         }
     }
 }
