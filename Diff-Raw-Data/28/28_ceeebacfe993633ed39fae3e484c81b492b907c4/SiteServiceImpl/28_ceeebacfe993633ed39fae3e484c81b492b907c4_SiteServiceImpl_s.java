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
 
 package com.orangeleap.tangerine.service.impl;
 
 import com.orangeleap.tangerine.dao.SiteDao;
 import com.orangeleap.tangerine.domain.AbstractCustomizableEntity;
 import com.orangeleap.tangerine.domain.AbstractEntity;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.domain.customization.EntityDefault;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.domain.customization.FieldRequired;
 import com.orangeleap.tangerine.domain.customization.FieldValidation;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.security.TangerineAuthenticationDetails;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.ErrorLogService;
 import com.orangeleap.tangerine.service.SiteService;
 import com.orangeleap.tangerine.service.VersionService;
 import com.orangeleap.tangerine.service.customization.FieldService;
 import com.orangeleap.tangerine.service.customization.MessageService;
 import com.orangeleap.tangerine.service.customization.PageCustomizationService;
 import com.orangeleap.tangerine.service.exception.ConstituentValidationException;
 import com.orangeleap.tangerine.type.LayoutType;
 import com.orangeleap.tangerine.type.MessageResourceType;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.type.EntityType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import org.apache.commons.collections.map.ListOrderedMap;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.validator.GenericValidator;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.GrantedAuthorityImpl;
 import org.springframework.security.providers.cas.CasAuthenticationToken;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindException;
 
 import javax.annotation.Resource;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 @Service("siteService")
 public class SiteServiceImpl extends AbstractTangerineService implements SiteService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "constituentService")
     private ConstituentService constituentService;
 
     @Resource(name = "fieldService")
     private FieldService fieldService;
 
     @Resource(name = "messageService")
     private MessageService messageService;
 
     @Resource(name = "pageCustomizationService")
     private PageCustomizationService pageCustomizationService;
 
     @Resource(name = "siteDAO")
     private SiteDao siteDao;
 
     @Resource(name = "versionService")
     private VersionService versionService;
 
     @Resource(name = "errorLogService")
     private ErrorLogService errorLogService;
 
     @Resource(name = "tangerineUserHelper")
     private TangerineUserHelper tangerineUserHelper;
 
     private static final int OLDEST_LOG_MESSAGE_DAYS = 31;  // TODO make configurable by site
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public synchronized Site createSiteAndUserIfNotExist(String siteName) {
 		if (logger.isTraceEnabled()) {
 		    logger.trace("createSiteAndUserIfNotExist: siteName = " + siteName);
 		}
 
 		versionService.checkOrangeLeapSchemaCompatible();
 
 		Site site = siteDao.readSite(siteName);
 		if (site == null) {
 			site = siteDao.createSite(new Site(siteName));
 		}
 
 		CasAuthenticationToken authentication = tangerineUserHelper.getToken();
 
 		if (tangerineUserHelper.lookupUserId() == null) {
 			String name = tangerineUserHelper.lookupUserName();
 			logger.debug("Looking up login record " + name);
 			Constituent constituent = constituentService.readConstituentByLoginId(name);
 			if (constituent == null) {
 				logger.debug("Login record " + name + " not found, creating.");
 				try {
 					constituent = createConstituent(authentication, siteName);
 				}
 				catch (Exception e) {
 					logger.error(e);
 					e.printStackTrace();
 					throw new RuntimeException("Unable to create new user record.");
 				}
 			}
 			TangerineAuthenticationDetails details = (TangerineAuthenticationDetails) authentication.getDetails();
 			details.setConstituentId(constituent.getId());
 		}
 		return site;
 	}
 
 	// Create a Constituent object row corresponding to the login user.
 	private Constituent createConstituent(CasAuthenticationToken authentication, String siteName)  throws ConstituentValidationException, BindException, javax.naming.NamingException {
 		TangerineAuthenticationDetails details = (TangerineAuthenticationDetails)authentication.getDetails();
 	    logger.info("Creating user for login id: "+details.getUserName());
 	    Constituent constituent = constituentService.createDefaultConstituent();
 	    constituent.setFirstName(details.getFirstName());
 	    constituent.setLastName(details.getLastName());
 	    constituent.setConstituentIndividualRoles("user");
 	    constituent.setLoginId(details.getUserName());
 	    constituent = constituentService.maintainConstituent(constituent);
 	    logger.info("Created user for login id: "+details.getUserName());
 	    return constituent;
 	}
 
     @Override
     public List<Site> readSites() {
         return siteDao.readSites();
     }
 
     @Override
     public Site readSite(String site) {
         return siteDao.readSite(site);
     }
 
 	/**
 	 * In addition to resolving the standard bean and map properties in a bean, also resolves indexed grid array values
 	 * @param pageType
 	 * @param roles
 	 * @param locale
 	 * @param entity
 	 */
     @Override
     public void readFieldInfo(PageType pageType, List<String> roles, Locale locale, AbstractEntity entity) {
 		Map<String, String> labelMap = new HashMap<String, String>();
 		Map<String, Object> valueMap = new HashMap<String, Object>();
 		Map<String, FieldDefinition> typeMap = new HashMap<String, FieldDefinition>();
 
         List<SectionField> sectionFields = getSectionFields(pageType, roles);
     	if (sectionFields != null) {
             BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(entity);
 
             for (SectionField secFld : sectionFields) {
 	            getFieldLabel(secFld, locale, labelMap);
                 getFieldType(secFld, typeMap);
 
 	            if (LayoutType.isGridType(secFld.getSectionDefinition().getLayoutType())) {
 		            getArrayFieldValues(secFld, valueMap, bean);
 	            }
 	            else {
 		            getFieldValue(secFld, valueMap, bean);
 	            }
             }
             valueMap.put(StringConstants.ID, bean.getPropertyValue(StringConstants.ID));
     	}
 
 		entity.setFieldLabelMap(labelMap);
 		entity.setFieldValueMap(valueMap);
 		entity.setFieldTypeMap(typeMap);
     }
 
 	protected void getArrayFieldValues(SectionField sectionField, Map<String, Object> fieldValues, BeanWrapper bean) {
 		String collectionFieldName = sectionField.getFieldDefinition().getFieldName();
 
 		if (bean.isReadableProperty(collectionFieldName) && bean.getPropertyValue(collectionFieldName) instanceof Collection) {
 			Collection coll = (Collection) bean.getPropertyValue(collectionFieldName);
 
 			for (int x = 0; x < coll.size(); x++) {
 				StringBuilder key = new StringBuilder(collectionFieldName);
 				key.append("[").append(x).append("]");
 
 				if (sectionField.getSecondaryFieldDefinition() != null) {
 				    key.append(".").append(sectionField.getSecondaryFieldDefinition().getFieldName());
 				}
 				getFieldValue(sectionField, key.toString(), fieldValues, bean);
 			}
 		}
 	}
 
     private String getFieldName(SectionField sectionField) {
         StringBuilder key = new StringBuilder(sectionField.getFieldDefinition().getFieldName());
         if (sectionField.getSecondaryFieldDefinition() != null) {
             key.append(".").append(sectionField.getSecondaryFieldDefinition().getFieldName());
         }
     	return key.toString();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Map<String, FieldRequired> readRequiredFields(PageType pageType, List<String> roles) {
         Map<String, FieldRequired> returnMap = new ListOrderedMap(); // ListOrderedMap used to ensure the order of the fields is maintained
         List<SectionField> fields = getSectionFields(pageType, roles);
         if (fields != null) {
             for (SectionField sectionField : fields) {
                 FieldRequired fieldRequired = fieldService.lookupFieldRequired(sectionField);
                 String key = getFieldName(sectionField);
                 if (fieldRequired != null && fieldRequired.isRequired()) {
                     returnMap.put(key, fieldRequired);
                 }
             }
         }
         return returnMap;
     }
 
     @Override
     public Map<String, String> readFieldLabels(PageType pageType, List<String> roles, Locale locale) {
         Map<String, String> returnMap = new HashMap<String, String>();
         List<SectionField> sfs = getSectionFields(pageType, roles);
         if (sfs != null) {
             for (SectionField sectionField : sfs) {
             	getFieldLabel(sectionField, locale, returnMap);
             }
         }
         return returnMap;
     }
 
     private void getFieldLabel(SectionField sectionField, Locale locale, Map<String, String> fieldLabels) {
         String labelText = null;
         // TODO: find out how to get current locale
         if (locale != null) {
             labelText = messageService.lookupMessage(MessageResourceType.FIELD_LABEL, sectionField.getFieldLabelName(), locale);
         }
         if (GenericValidator.isBlankOrNull(labelText)) {
             if (!sectionField.isCompoundField()) {
                 labelText = sectionField.getFieldDefinition().getDefaultLabel();
             } else {
                 labelText = sectionField.getSecondaryFieldDefinition().getDefaultLabel();
             }
         }
         String key = getFieldName(sectionField);
         fieldLabels.put(key, labelText);
     }
 
     @Override
     public Map<String, FieldValidation> readFieldValidations(PageType pageType, List<String> roles) {
         Map<String, FieldValidation> returnMap = new HashMap<String, FieldValidation>();
         List<SectionField> fields = getSectionFields(pageType, roles);
         if (fields != null) {
             for (SectionField sectionField : fields) {
                 FieldValidation fieldValidation = fieldService.lookupFieldValidation(sectionField);
                 String key = getFieldName(sectionField);
                 if (fieldValidation != null && !StringUtils.isEmpty(fieldValidation.getRegex())) {
                     returnMap.put(key, fieldValidation);
                 }
             }
         }
         return returnMap;
     }
 
     @Override
     public List<SectionField> getSectionFields(PageType pageType, List<String> roles) {
         List<SectionField> fields = new ArrayList<SectionField>();
         List<SectionDefinition> sectionDefinitions = pageCustomizationService.readSectionDefinitionsByPageTypeRoles(pageType, roles);
         if (sectionDefinitions != null) {
             for (SectionDefinition sectionDefinition : sectionDefinitions) {
                 List<SectionField> currentSectionFields = pageCustomizationService.readSectionFieldsBySection(sectionDefinition);
                 if (currentSectionFields != null) {
                     fields.addAll(currentSectionFields);
                 }
             }
         }
         return fields;
     }
 
     @Override
     public Map<String, Object> readFieldValues(PageType pageType, List<String> roles, Object object) {
         Map<String, Object> returnMap = new HashMap<String, Object>();
         List<SectionField> sfs = getSectionFields(pageType, roles);
         if (sfs != null) {
             BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(object);
             for (SectionField sectionField : sfs) {
             	getFieldValue(sectionField, returnMap, bean);   // TODO: configure for grids?
 	            
             }
             returnMap.put(StringConstants.ID, bean.getPropertyValue(StringConstants.ID));
         }
         return returnMap;
     }
 
     private void getFieldValue(SectionField sectionField, Map<String, Object> returnMap, BeanWrapper bean) {
         String key = getFieldName(sectionField);
 	    getFieldValue(sectionField, key, returnMap, bean);
     }
 
 	private void getFieldValue(SectionField sectionField, String key, Map<String, Object> returnMap, BeanWrapper bean) {
 		try {
 			Object value = null;
 			if (bean.isReadableProperty(key)) {
 				value = bean.getPropertyValue(key);
 				if (value instanceof CustomField) {
 					value = bean.getPropertyValue(key + StringConstants.DOT_VALUE);
 				}
 			}
 			// If there is no value, try to find the default value
 			if (value == null) {
 				EntityDefault entityDefault = readEntityDefaultByTypeNameSectionDef(sectionField);
 				value = getDefaultValue(entityDefault, bean, key);
 			}
 			returnMap.put(key, value);
 		}
 		catch (BeansException e) {
 			// it is ok if the property doesn't exist
 		}
 	}
 
 	@Override
 	public Object getDefaultValue(EntityDefault entityDefault, BeanWrapper bean, String key) {
 		Object value = null;
 
 		if (entityDefault != null && org.springframework.util.StringUtils.hasText(entityDefault.getDefaultValue())) {
 			String defaultValue = null;
 
 			String conditionExp = entityDefault.getConditionExp();
 			if (org.springframework.util.StringUtils.hasText(conditionExp)) {
 				// Evaluate the condition if one exists to see if we can set the default value
 
 				String[] conditions = conditionExp.split("==");
 				if (conditions != null && conditions.length == 2) {
 					String conditionFieldName = conditions[0].trim();
 					String conditionValue = conditions[1].trim();
 					if (conditionValue.startsWith("'") && conditionValue.endsWith("'")) {
 						conditionValue = conditionValue.substring(1, conditionValue.length() - 1);
 					}
 
 					if (bean.isReadableProperty(conditionFieldName) &&
 							bean.getPropertyValue(conditionFieldName) != null &&
 							conditionValue.equals(bean.getPropertyValue(conditionFieldName).toString())) {
 						defaultValue = entityDefault.getDefaultValue();
 					}
 				}
 			}
 			else {
 				defaultValue = entityDefault.getDefaultValue();
 			}
 			if (defaultValue != null && org.springframework.util.StringUtils.hasText(defaultValue)) {
 				if (defaultValue.startsWith(StringConstants.BEAN_PREFIX)) {
 					String propertyName = defaultValue.replaceFirst(StringConstants.BEAN_PREFIX, StringConstants.EMPTY);
 					if (bean.isReadableProperty(propertyName)) {
 						value = bean.getPropertyValue(propertyName);
 					}
 					if (value != null && value instanceof CustomField) {
 						value = bean.getPropertyValue(propertyName + StringConstants.DOT_VALUE);
 					}
 				}
 				else if (StringConstants.NOW_COLON.equals(defaultValue)) {
 					value = Calendar.getInstance(Locale.getDefault()).getTime();
 				}
 				else {
 					value = defaultValue;
 				}
 			}
 		}
 		if (value != null && key != null && bean.isWritableProperty(key)) {
 			boolean isCustomField = false;
 			if (bean.getPropertyValue(key) instanceof CustomField) {
 				key += StringConstants.DOT_VALUE;
 				isCustomField = true;
 			}
 
 			/* Only set the bean property default value if it is the same class */
 			Class thisClazz = bean.getPropertyType(key);
 
 			if (isCustomField && value.getClass().equals(Date.class)) {
 				/* For customFields, need to format dates to the standard format */
 				SimpleDateFormat sdf = new SimpleDateFormat(AbstractCustomizableEntity.FMT);
 				bean.setPropertyValue(key, sdf.format(value));
 			}
 			else if (String.class.equals(thisClazz) && !value.getClass().equals(String.class)) {
 				bean.setPropertyValue(key, value.toString());
 			}
 			else if (thisClazz.equals(value.getClass())){
 				bean.setPropertyValue(key, value);
 			}
 		}
 		return value;
 	}
 
 	@Override
 	public EntityDefault readEntityDefaultByTypeNameSectionDef(SectionField sectionField) {
 	    if (logger.isTraceEnabled()) {
 	        logger.trace("readEntityDefaultByTypeNameSectionDef: sectionField = " + sectionField);
 	    }
 		FieldDefinition thisFieldDef = sectionField.getSecondaryFieldDefinition() != null ?
 						sectionField.getSecondaryFieldDefinition() : sectionField.getFieldDefinition();
 		return readEntityDefaultByTypeNameSectionDef(thisFieldDef.getEntityType(), thisFieldDef.getFieldName(),
 				sectionField.getSectionDefinition());
 	}
 
 	@Override
 	public EntityDefault readEntityDefaultByTypeNameSectionDef(EntityType entityType, String fieldName, SectionDefinition sectionDef) {
 		if (logger.isTraceEnabled()) {
 		    logger.trace("readEntityDefaultByTypeNameSectionDef: entityType = " + entityType + " fieldName = " + fieldName + " sectionDefinition = " + sectionDef);
 		}
 		return siteDao.readEntityDefaultByTypeNameSectionDef(entityType, fieldName, sectionDef);
 	}
 
 	@Override
 	public void setEntityDefaults(AbstractEntity entity, EntityType entityType, SectionDefinition sectionDefinition) {
 		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
 
 		List<EntityDefault> defaults = siteDao.readEntityDefaults(entityType, sectionDefinition);
 		if (defaults != null && !defaults.isEmpty()) {
 			for (EntityDefault aDefault : defaults) {
 				getDefaultValue(aDefault, beanWrapper, aDefault.getEntityFieldName());
 			}
 		}
 	}
 
     @Override
     public Map<String, FieldDefinition> readFieldTypes(PageType pageType, List<String> roles) {
         Map<String, FieldDefinition> returnMap = new HashMap<String, FieldDefinition>();
         List<SectionField> sfs = getSectionFields(pageType, roles);
         if (sfs != null) {
             for (SectionField sectionField : sfs) {
             	getFieldType(sectionField, returnMap);
             }
         }
         return returnMap;
     }
 
     private void getFieldType(SectionField sectionField, Map<String, FieldDefinition> fieldTypes) {
         fieldTypes.put(getFieldName(sectionField), sectionField.getFieldDefinition());
     }
 
     @Override
     public GrantedAuthority[] readDistinctRoles() {
     	List<String> roles = pageCustomizationService.readDistintSectionDefinitionsRoles();
     	List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
     	for (String s: roles) {
     		if (s!= null && s.length() > 0) {
 				list.add(new GrantedAuthorityImpl(s));
 			}
     	}
     	return list.toArray(new GrantedAuthority[list.size()]);
     }
 
     @Override
     public AbstractEntity populateDefaultEntityEditorMaps(AbstractEntity entity) {

    	PageType pageType = PageType.valueOf(StringUtils.uncapitalize(entity.getClass().getSimpleName()));
         List<String> roles = tangerineUserHelper.lookupUserRoles();
 
         Map<String, String> fieldLabelMap = readFieldLabels(pageType, roles, Locale.getDefault());
         entity.setFieldLabelMap(fieldLabelMap);
 
         Map<String, Object> valueMap = readFieldValues(pageType, roles, entity);
         entity.setFieldValueMap(valueMap);
 
         Map<String, FieldDefinition> typeMap = readFieldTypes(pageType, roles);
         entity.setFieldTypeMap(typeMap);
 
     	return entity;
     }
 
 
 }
