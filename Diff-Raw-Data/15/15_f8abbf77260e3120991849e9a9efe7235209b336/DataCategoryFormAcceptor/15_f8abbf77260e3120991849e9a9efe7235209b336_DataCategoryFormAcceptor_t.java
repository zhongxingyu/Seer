 package com.amee.platform.service.v3.category;
 
 import com.amee.base.resource.MissingAttributeException;
 import com.amee.base.resource.NotFoundException;
 import com.amee.base.resource.RequestWrapper;
 import com.amee.base.resource.ResourceAcceptor;
 import com.amee.base.validation.ValidationException;
 import com.amee.domain.data.DataCategory;
 import com.amee.service.data.DataService;
 import com.amee.service.invalidation.InvalidationService;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Scope("prototype")
 public class DataCategoryFormAcceptor implements ResourceAcceptor {
 
     @Autowired
     private InvalidationService invalidationService;
 
     @Autowired
     private DataService dataService;
 
     @Autowired
     private DataCategoryValidationHelper validationHelper;
 
    @Transactional(rollbackFor = {ValidationException.class})
     public JSONObject handle(RequestWrapper requestWrapper) throws ValidationException {
         try {
             JSONObject o = new JSONObject();
             String categoryIdentifier = requestWrapper.getAttributes().get("categoryIdentifier");
             if (categoryIdentifier != null) {
                 DataCategory dataCategory = dataService.getDataCategoryByUid(categoryIdentifier);
                 if (dataCategory != null) {
                     validationHelper.setDataCategory(dataCategory);
                     if (validationHelper.isValid(requestWrapper.getFormParameters())) {
                         o.put("status", "OK");
                         invalidationService.beforeHandle();
                         dataService.invalidate(dataCategory);
                         invalidationService.afterHandle();
                     } else {
                         throw new ValidationException(validationHelper.getValidationResult());
                     }
                 } else {
                     throw new NotFoundException();
                 }
             } else {
                 throw new MissingAttributeException("categoryIdentifier");
             }
             return o;
         } catch (JSONException e) {
             throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
         }
     }
 }
