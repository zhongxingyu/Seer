 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.web.common;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.service.customization.PageCustomizationService;
 import com.orangeleap.tangerine.type.AccessType;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.util.HttpUtil;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.ExtTypeHandler;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandler;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.FieldHandlerHelper;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.stereotype.Component;
 import org.springframework.web.util.WebUtils;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 @Component("tangerineListHelper")
 public class TangerineListHelper {
 
     @Resource(name = "pageCustomizationService")
     protected PageCustomizationService pageCustomizationService;
 
     @Resource(name = "fieldHandlerHelper")
     protected FieldHandlerHelper fieldHandlerHelper;
 
     @Resource(name = "tangerineUserHelper")
     protected TangerineUserHelper tangerineUserHelper;
 
     public static final String SORT_KEY_PREFIX = "a";
 
     @SuppressWarnings("unchecked")
     // TODO: move to annotation
     public boolean isAccessAllowed(HttpServletRequest request, PageType pageType) {
         Map<String, AccessType> pageAccess = (Map<String, AccessType>) WebUtils.getSessionAttribute(request, "pageAccess");
         return pageAccess.get(pageType.getPageName()) == AccessType.ALLOWED;
     }
 
     // TODO: move to annotation
     public void checkAccess(HttpServletRequest request, PageType pageType) {
         if ( ! isAccessAllowed(request, pageType)) {
             throw new RuntimeException("You are not authorized to access this page");
         }
     }
 
     public void addListFieldsToMap(HttpServletRequest request, List<SectionField> sectionFields, List entities,
                                    List<Map<String, Object>> paramMapList, boolean useAliasName, boolean useAliasId) {
         int sequence = 0;
         if (entities != null) {
             for (Object thisEntity : entities) {
                 BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(thisEntity);
 
                 /**
                  * If useAliasId == true, set the aliasId to the ID and reset to ID to the next sequence number
                  */
                 if (useAliasId && beanWrapper.isWritableProperty(StringConstants.ALIAS_ID) && beanWrapper.isReadableProperty(StringConstants.ID)) {
                     beanWrapper.setPropertyValue(StringConstants.ALIAS_ID, beanWrapper.getPropertyValue(StringConstants.ID));
                     beanWrapper.setPropertyValue(StringConstants.ID, sequence++);
                 }
 
                 Map<String, Object> paramMap = new HashMap<String, Object>();
 
                 int x = 0;
                 for (SectionField field : sectionFields) {
                     String fieldPropertyName = field.getFieldPropertyName();
                     if (beanWrapper.isReadableProperty(fieldPropertyName)) {
                         FieldHandler handler = fieldHandlerHelper.lookupFieldHandler(field.getFieldType());
 
                         Object displayValue = StringConstants.EMPTY;
                         if (beanWrapper.isReadableProperty(field.getFieldPropertyName())) {
                             Object fieldValue = beanWrapper.getPropertyValue(field.getFieldPropertyName());
                             if (fieldValue instanceof CustomField) {
                                 fieldValue = ((CustomField) fieldValue).getValue();
                             }
                             if (field.getFieldPropertyName().equals(StringConstants.ID) || field.getFieldPropertyName().equals(StringConstants.ALIAS_ID)) {
                                 displayValue = fieldValue;
                             }
                             else {
                                 displayValue = handler.resolveDisplayValue(request, PropertyAccessorFactory.forBeanPropertyAccess(thisEntity), field, fieldValue);
                             }
                         }
                         if (displayValue instanceof String) {
                             displayValue = HttpUtil.escapeDoubleQuoteReturns((String) displayValue);
 
                             String extType = ExtTypeHandler.findExtDataType(beanWrapper.getPropertyType(field.getFieldPropertyName()));
                             if (ExtTypeHandler.EXT_BOOLEAN.equals(extType) && ("Y".equalsIgnoreCase((String) displayValue) ||
                                     "yes".equalsIgnoreCase((String) displayValue) ||
                                     "T".equalsIgnoreCase((String) displayValue) ||
                                     "true".equalsIgnoreCase((String) displayValue))) {
                                 displayValue = "true";
                             }
                         }
                        String key = useAliasName && ! StringConstants.ID.equals(fieldPropertyName) && ! StringConstants.ALIAS_ID.equals(fieldPropertyName)
                                ? new StringBuilder(SORT_KEY_PREFIX).append(x++).toString() : TangerineForm.escapeFieldName(fieldPropertyName);
                         paramMap.put(key, displayValue);
                     }
                 }
                 paramMapList.add(paramMap);
             }
         }
     }
 
     public Map<String, Object> initMetaData(int start, int limit) {
         final Map<String, Object> metaDataMap = new LinkedHashMap<String, Object>();
         metaDataMap.put(StringConstants.ID_PROPERTY, StringConstants.ID);
         metaDataMap.put(StringConstants.ROOT, StringConstants.ROWS);
         metaDataMap.put(StringConstants.TOTAL_PROPERTY, StringConstants.TOTAL_ROWS);
         metaDataMap.put(StringConstants.SUCCESS_PROPERTY, StringConstants.SUCCESS);
         metaDataMap.put(StringConstants.START, start);
         metaDataMap.put(StringConstants.LIMIT, limit);
         return metaDataMap;
     }
 
     public List<SectionField> findSectionFields(String listPageName) {
         return pageCustomizationService.readSectionFieldsByPageTypeRoles(PageType.valueOf(listPageName), tangerineUserHelper.lookupUserRoles());
     }
 }
