 package com.orangeleap.tangerine.service.customization;
 
 import java.util.Comparator;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import net.sf.ehcache.Cache;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.orangeleap.tangerine.controller.customField.CustomFieldRequest;
 import com.orangeleap.tangerine.dao.CacheGroupDao;
 import com.orangeleap.tangerine.dao.FieldDao;
 import com.orangeleap.tangerine.domain.QueryLookup;
 import com.orangeleap.tangerine.domain.QueryLookupParam;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.domain.customization.FieldRelationship;
 import com.orangeleap.tangerine.domain.customization.FieldValidation;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.SectionDefinition;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.service.impl.AbstractTangerineService;
 import com.orangeleap.tangerine.type.CacheGroupType;
 import com.orangeleap.tangerine.type.EntityType;
 import com.orangeleap.tangerine.type.FieldType;
 import com.orangeleap.tangerine.type.LayoutType;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.type.ReferenceType;
 import com.orangeleap.tangerine.type.RelationshipType;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 
import java.util.Collections;
 
 @Service("customFieldMaintenanceService")
 @Transactional
 public class CustomFieldMaintenanceServiceImpl extends AbstractTangerineService implements CustomFieldMaintenanceService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name="tangerineUserHelper")
     protected TangerineUserHelper tangerineUserHelper;
 
     @Resource(name = "cacheGroupDAO")
     private CacheGroupDao cacheGroupDao;
 
     @Resource(name = "fieldDAO")
     private FieldDao fieldDao;
 
         
     @Resource(name = "pageCustomizationService")
     private PageCustomizationService pageCustomizationService;
     
     @Resource(name = "pageCustomizationCache")
     private Cache pageCustomizationCache;
     
     @Resource(name = "picklistItemService")
     private PicklistItemService picklistItemService;
     
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
 	public void addCustomField(CustomFieldRequest customFieldRequest) {
     	
     		if (StringUtils.trimToNull(customFieldRequest.getLabel()) == null || StringUtils.trimToNull(customFieldRequest.getFieldName()) == null) {
     			throw new RuntimeException("Field name and label are required");
     		}
     		if (isRelationship(customFieldRequest) && !isReferenceType(customFieldRequest)) {
     			throw new RuntimeException("Only constituent reference field types can be part of a relationship.");
     		}
     		
             Site site = new Site(tangerineUserHelper.lookupUserSiteName());
 
             FieldDefinition fieldDefinition = getFieldDefinition(customFieldRequest, site);
             pageCustomizationService.maintainFieldDefinition(fieldDefinition);
             
             if (isPicklist(customFieldRequest)) {
             	createPicklist(fieldDefinition);
             }
             
     		PageType editPage = PageType.valueOf(customFieldRequest.getEntityType());
     		SectionDefinition sectionDefinition = addSectionDefinitionsAndValidations(editPage, customFieldRequest, fieldDefinition, site);
     		SectionDefinition sectionDefinitionView = null;
 
     		if (hasViewPage(customFieldRequest.getEntityType())) {
     			PageType viewPage = PageType.valueOf(customFieldRequest.getEntityType()+"View");
     			sectionDefinitionView = addSectionDefinitionsAndValidations(viewPage, customFieldRequest, fieldDefinition, site);
     		}
     		
             if (isReferenceType(customFieldRequest)) {
             	createLookupScreenDefsAndQueryLookups(customFieldRequest, fieldDefinition, sectionDefinition);
             	if (sectionDefinitionView != null) createLookupScreenDefsAndQueryLookups(customFieldRequest, fieldDefinition, sectionDefinitionView);
             }
             
             if (isRelationship(customFieldRequest)) {
             	createRelationship(customFieldRequest, fieldDefinition);
             }
             
     		updateTheGuru(customFieldRequest);
     		
     		// Flush section/field definition cache for all tomcat instances
             pageCustomizationCache.removeAll();
             cacheGroupDao.updateCacheGroupTimestamp(CacheGroupType.PAGE_CUSTOMIZATION);
             
 	}
 
     private boolean hasViewPage(String entityType) {
             return !("person".equals(entityType) || "giftInKind".equals(entityType));
     }
     
     private void createRelationship(CustomFieldRequest customFieldRequest, FieldDefinition fieldDefinition) {
 
     	FieldDefinition otherFieldDefinition;
     	if (customFieldRequest.getRelateToField().equals("_self")) {
     		otherFieldDefinition = fieldDefinition;
     	} else {
     		otherFieldDefinition = fieldDao.readFieldDefinition(customFieldRequest.getRelateToField());
     	}
     	
     	FieldRelationship fr = new FieldRelationship();
     	fr.setSite(fieldDefinition.getSite());
 
     	RelationshipType relationshipType = RelationshipType.ONE_TO_MANY;
     	if (isMultiValued(fieldDefinition) && isMultiValued(otherFieldDefinition)) relationshipType = RelationshipType.MANY_TO_MANY;
     	if (!isMultiValued(fieldDefinition) && !isMultiValued(otherFieldDefinition)) relationshipType = RelationshipType.ONE_TO_ONE;
     	fr.setRelationshipType(relationshipType);
     	
     	FieldDefinition master;
     	FieldDefinition detail;
     	if (isMultiValued(fieldDefinition)) {
         	master = fieldDefinition;
             detail = otherFieldDefinition;
     	} else {
         	master = otherFieldDefinition;
             detail = fieldDefinition;
     	}
     	fr.setMasterRecordField(master);
     	fr.setDetailRecordField(detail);
     	
     	fieldDao.maintainFieldRelationship(fr);
     }
     
     private boolean isMultiValued(FieldDefinition fieldDefinition) {
     	return fieldDefinition.getFieldType() == FieldType.MULTI_QUERY_LOOKUP;
     }
     
     private boolean isPicklist(CustomFieldRequest customFieldRequest) {
     	return customFieldRequest.getFieldType().equals(FieldType.PICKLIST) || customFieldRequest.getFieldType().equals(FieldType.MULTI_PICKLIST);
     }
     
     private boolean isRelationship(CustomFieldRequest customFieldRequest) {
     	return StringUtils.trimToNull(customFieldRequest.getRelateToField()) != null && customFieldRequest.getEntityType().equals("person");
     }
     
     private boolean isReferenceType(CustomFieldRequest customFieldRequest) {
     	return customFieldRequest.getFieldType().equals(FieldType.QUERY_LOOKUP) || customFieldRequest.getFieldType().equals(FieldType.MULTI_QUERY_LOOKUP);
     }
     
     // Creates a constituent lookup (not gift etc. lookup) field.
     private void createLookupScreenDefsAndQueryLookups(CustomFieldRequest customFieldRequest, FieldDefinition fieldDefinition, SectionDefinition sectionDefinition) {
     	
     	boolean isOrganization = customFieldRequest.getReferenceConstituentType().equals("organization");
     	boolean isIndividual = customFieldRequest.getReferenceConstituentType().equals("individual");
     	boolean isBoth = customFieldRequest.getReferenceConstituentType().equals("both");
 
     	// Create queries
     	String lookupSectionName = sectionDefinition.getPageType() + ".person."+fieldDefinition.getCustomFieldName();
     	
     	QueryLookup queryLookup = new QueryLookup();
     	queryLookup.setFieldDefinition(fieldDefinition);
     	queryLookup.setEntityType(EntityType.person);
     	queryLookup.setSite(fieldDefinition.getSite());
     	String sqlWhere = "";
     	if (isOrganization) {
     		sqlWhere += "constituent_type = 'organization'";
     	} 
     	if (isIndividual) {
     		sqlWhere += "constituent_type = 'individual'";
     	}
     	queryLookup.setSqlWhere(sqlWhere);
     	queryLookup.setSectionName(lookupSectionName);
     	queryLookup = pageCustomizationService.maintainQueryLookup(queryLookup); 
     	
     	if (isOrganization || isBoth) {
         	addQueryLookupParam(queryLookup, "organizationName");
     	} 
     	if (isIndividual || isBoth) {
         	addQueryLookupParam(queryLookup, "firstName");
         	addQueryLookupParam(queryLookup, "lastName");
     	}
     
     	// Create lookup screen defs
     	
     	SectionDefinition lookupSectionDefinition = new SectionDefinition();
     	lookupSectionDefinition.setLayoutType(LayoutType.GRID);
     	lookupSectionDefinition.setPageType(PageType.queryLookup);
     	lookupSectionDefinition.setSectionName(lookupSectionName);
     	lookupSectionDefinition.setDefaultLabel("");
     	lookupSectionDefinition.setRole("ROLE_USER");
     	lookupSectionDefinition.setSectionOrder(1);
     	lookupSectionDefinition.setSite(fieldDefinition.getSite());
     	lookupSectionDefinition = pageCustomizationService.maintainSectionDefinition(lookupSectionDefinition); 
     	
     	if (isOrganization || isBoth) {
     		addSectionField(lookupSectionDefinition, "person.organizationName", 1000);
     	} 
     	if (isIndividual || isBoth) {
     		addSectionField(lookupSectionDefinition, "person.lastName", 1000);
     		addSectionField(lookupSectionDefinition, "person.firstName", 2000);
     	}
     
     }
     
     private void addQueryLookupParam(QueryLookup queryLookup, String param) {
     	QueryLookupParam queryLookupParam = new QueryLookupParam();
     	queryLookupParam.setName(param);
     	queryLookupParam.setQueryLookupId(queryLookup.getId());
     	pageCustomizationService.maintainQueryLookupParam(queryLookupParam); 
     }
     
     private void addSectionField(SectionDefinition lookupSectionDefinition, String fieldDefintionId, int fieldOrder) {
     	SectionField sectionField = new SectionField();
     	sectionField.setSectionDefinition(lookupSectionDefinition);
     	sectionField.setFieldOrder(fieldOrder);
     	FieldDefinition fieldDefinition = new FieldDefinition();
     	fieldDefinition.setId(fieldDefintionId);
     	sectionField.setFieldDefinition(fieldDefinition);
     	pageCustomizationService.maintainSectionField(sectionField); 
     }
     
     private void createPicklist(FieldDefinition fieldDefinition) {
     	Picklist picklist = new Picklist();
     	picklist.setSite(fieldDefinition.getSite());
     	picklist.setPicklistDesc(fieldDefinition.getDefaultLabel());
     	picklist.setPicklistName(fieldDefinition.getFieldName());
     	picklist.setPicklistNameId(fieldDefinition.getFieldName());
     	picklistItemService.maintainPicklist(picklist);
     }
     
 	// Modify the guru elements to support new custom field
     private void updateTheGuru(CustomFieldRequest customFieldRequest) {
     	pageCustomizationService.maintainCustomFieldGuruData(customFieldRequest);
     }
     
     private SectionDefinition addSectionDefinitionsAndValidations(PageType pageType, CustomFieldRequest customFieldRequest, FieldDefinition fieldDefinition, Site site) {
 
         // Default to add to first section after last field.
     	SectionDefinition sectionDefinition = getDefaultSection(pageType);
 		int fieldOrder = getNextFieldOrder(sectionDefinition);
         
         SectionField sectionField = getSectionField(fieldOrder, sectionDefinition, fieldDefinition, site);
         pageCustomizationService.maintainSectionField(sectionField);
         
         FieldValidation fieldValidation = getFieldValidation(customFieldRequest, fieldDefinition, sectionDefinition);
         
         if (StringUtils.trimToNull(fieldValidation.getRegex()) != null) {
         	pageCustomizationService.maintainFieldValidation(fieldValidation);
         }
         
         return sectionDefinition;
 
     }
     
     private SectionField getSectionField(int fieldOrder, SectionDefinition sectionDefinition, FieldDefinition fieldDefinition, Site site) {
         SectionField sectionField = new SectionField();
         sectionField.setFieldOrder(fieldOrder);
         sectionField.setSectionDefinition(sectionDefinition);
         sectionField.setSite(site);
         sectionField.setFieldDefinition(fieldDefinition);
         return sectionField;
     }
     
     private FieldValidation getFieldValidation(CustomFieldRequest customFieldRequest, FieldDefinition fieldDefinition, SectionDefinition sectionDefinition) {
         FieldValidation fieldValidation = new FieldValidation();
         fieldValidation.setFieldDefinition(fieldDefinition);
         fieldValidation.setSectionName(sectionDefinition.getSectionName());
         String regex = null;
     	String type = customFieldRequest.getValidationType();
     	if (type.equals("email")) regex = "extensions:isEmail";
     	if (type.equals("url")) regex = "extensions:isUrl";
     	if (type.equals("numeric")) regex = "^[0-9]*$";
     	if (type.equals("alphanumeric")) regex = "^[a-zA-Z0-9]*$";
     	if (type.equals("regex")) regex = customFieldRequest.getRegex().trim();
         fieldValidation.setRegex(regex);
         return fieldValidation;
     }
     
     private FieldDefinition getFieldDefinition(CustomFieldRequest customFieldRequest, Site site) {
         FieldDefinition newFieldDefinition = new FieldDefinition();
         newFieldDefinition.setDefaultLabel(customFieldRequest.getLabel());
         newFieldDefinition.setEntityType(EntityType.valueOf(customFieldRequest.getEntityType()));
         if (customFieldRequest.getEntityType().equals("person")) newFieldDefinition.setEntityAttributes(customFieldRequest.getConstituentType());
         newFieldDefinition.setFieldType(customFieldRequest.getFieldType());
         newFieldDefinition.setSite(site);
         String fieldName = "customFieldMap["+ customFieldRequest.getFieldName()+"]";
         newFieldDefinition.setFieldName(fieldName);
         String id = site.getName() + "-" + customFieldRequest.getEntityType() + "." + fieldName;
         newFieldDefinition.setId(id);
         if (isReferenceType(customFieldRequest)) {
         	newFieldDefinition.setReferenceType(ReferenceType.person);
         }
         return newFieldDefinition;
     }
     
     private SectionDefinition getDefaultSection(PageType pageType) {
         List<SectionDefinition> definitions = pageCustomizationService.readSectionDefinitionsByPageTypeRoles(pageType, tangerineUserHelper.lookupUserRoles());
         Collections.sort(definitions, new Comparator<SectionDefinition>() {
 			@Override
 			public int compare(SectionDefinition o1, SectionDefinition o2) {
 				return o1.getSectionOrder().compareTo(o2.getSectionOrder());
 			}
         });
         if (definitions.size() == 0) throw new RuntimeException("Default page for "+pageType.getName()+" has no sections");
         return definitions.get(0);
     }
     
     // Get next field on page from end
     private int getNextFieldOrder(SectionDefinition sectionDefinition) {
     	List<SectionField> sectionFields = pageCustomizationService.readSectionFieldsBySection(sectionDefinition);
         Collections.sort(sectionFields, new Comparator<SectionField>() {
 			@Override
 			public int compare(SectionField o1, SectionField o2) {
 				return o1.getFieldOrder().compareTo(o2.getFieldOrder());
 			}
         });
         int fieldOrder = 0;
         if (sectionFields.size() > 0) {
         	fieldOrder = sectionFields.get(sectionFields.size()-1).getFieldOrder().intValue() + 1000;
         }
         return fieldOrder;
     }
 		
 
 }
