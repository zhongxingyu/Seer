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
 
 package com.orangeleap.tangerine.service.customization;
 
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
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import net.sf.ehcache.Cache;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Resource;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 @Service("customFieldMaintenanceService")
 @Transactional
 public class CustomFieldMaintenanceServiceImpl extends AbstractTangerineService implements CustomFieldMaintenanceService {
 
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "tangerineUserHelper")
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
 
         // Field definitions
         
         FieldDefinition fieldDefinition = getFieldDefinition(false, false, customFieldRequest, site);
         pageCustomizationService.maintainFieldDefinition(fieldDefinition);
 
         FieldDefinition readOnlyFieldDefinition = getFieldDefinition(true, false, customFieldRequest, site);
         pageCustomizationService.maintainFieldDefinition(readOnlyFieldDefinition);
 
         if (isDistributionLines(customFieldRequest.getEntityType())) {
             FieldDefinition adjustmentFieldDefinition = getFieldDefinition(false, true, customFieldRequest, site);
             pageCustomizationService.maintainFieldDefinition(adjustmentFieldDefinition);
             FieldDefinition readOnlyAdjustmentFieldDefinition = getFieldDefinition(true, true, customFieldRequest, site);
             pageCustomizationService.maintainFieldDefinition(readOnlyAdjustmentFieldDefinition);
         }
 
         if (isPicklist(customFieldRequest)) {
             createPicklist(fieldDefinition);
         }
 
         if (isRelationship(customFieldRequest)) {
             createRelationship(customFieldRequest, fieldDefinition);
         }
         
         // Section Fields
         
         String sPageType = getPageType(customFieldRequest.getEntityType());
         
         // Main page
         PageType editPage = PageType.valueOf(sPageType);
         addSectionFieldsAndValidations(editPage, customFieldRequest, fieldDefinition, site);
 
         if (hasViewPage(customFieldRequest.getEntityType())) {
         	
         	boolean isWritableViewPage = customFieldRequest.getEntityType().equals("communicationHistory");
             PageType viewPage = PageType.valueOf(sPageType + "View");
             addSectionFieldsAndValidations(viewPage, customFieldRequest, isWritableViewPage?fieldDefinition:readOnlyFieldDefinition, site);  
             
         }
         
         if (hasPaidPostedPage(customFieldRequest.getEntityType())) {
             PageType paidPage = PageType.valueOf(sPageType + "Paid");
             addSectionFieldsAndValidations(paidPage, customFieldRequest, fieldDefinition, site);
 
             PageType postedPage = PageType.valueOf(sPageType + "Posted");
             addSectionFieldsAndValidations(postedPage, customFieldRequest, readOnlyFieldDefinition, site);
 
         }
         
         if (hasAdjustmentPage(customFieldRequest.getEntityType())) {
             String pageName = StringUtils.capitalize(sPageType);
 
             PageType adjustedPage = PageType.valueOf("adjusted" + pageName);
             addSectionFieldsAndValidations(adjustedPage, customFieldRequest, fieldDefinition, site);
 
             PageType adjustedPaidPage = PageType.valueOf("adjusted" + pageName + "Paid");
             addSectionFieldsAndValidations(adjustedPaidPage, customFieldRequest, readOnlyFieldDefinition, site);
 
             PageType adjustedPostedPage = PageType.valueOf("adjusted" + pageName + "Posted");
             addSectionFieldsAndValidations(adjustedPostedPage, customFieldRequest, readOnlyFieldDefinition, site);
             
         }
         
         if (hasCombinedDistroLinesPage(customFieldRequest.getEntityType())) {
 
             PageType combinedDistroLinesPage = PageType.giftCombinedDistributionLines;
             addSectionFieldsAndValidations(combinedDistroLinesPage, customFieldRequest, fieldDefinition, site);
             
         }        
 
         updateTheGuru(customFieldRequest);
 
         // Flush section/field definition cache for all tomcat instances
         pageCustomizationCache.removeAll();
         cacheGroupDao.updateCacheGroupTimestamp(CacheGroupType.PAGE_CUSTOMIZATION);
 
     }
 
     private boolean hasAdjustmentPage(String entityType) {
        return "gift".equals(entityType) || "gift.distributionLine".equals(entityType);
     }
 
     private boolean hasCombinedDistroLinesPage(String entityType) {
         return "gift.distributionLine".equals(entityType);
     }
 
     private boolean hasViewPage(String entityType) {
        return !("constituent".equals(entityType) || "giftInKind".equals(entityType) || StringConstants.GIFT.equals(entityType));
     }
 
     private boolean hasPaidPostedPage(String entityType) {
         return StringConstants.GIFT.equals(entityType);
     }
     
     private FieldDefinition getFieldDefinition(boolean readOnly, boolean distributionLine, CustomFieldRequest customFieldRequest, Site site) {
     	
         String id = getFieldDefinitionId(readOnly, distributionLine, customFieldRequest, site);
         FieldDefinition existing = fieldDao.readFieldDefinition(id);
         if (existing != null) {
         	if (distributionLine) return existing; // these are shared
         	logger.debug("Field ["+id+"] already exists.");
         	throw new RuntimeException("Field already exists.");
         }
         
         FieldDefinition newFieldDefinition = new FieldDefinition();
         newFieldDefinition.setId(id);
         newFieldDefinition.setSite(site);
         newFieldDefinition.setDefaultLabel(customFieldRequest.getLabel());
         
         if (customFieldRequest.getEntityType().equals("constituent")) {
             String constituentType = customFieldRequest.getConstituentType();
             if (constituentType.equals("both")) constituentType = "individual,organization";
             newFieldDefinition.setEntityAttributes(constituentType);
         }
         
         if (isReferenceType(customFieldRequest)) {
             newFieldDefinition.setReferenceType(ReferenceType.constituent);
         }
 
         newFieldDefinition.setFieldType(getFieldType(readOnly, customFieldRequest.getFieldType()));
         
         newFieldDefinition.setEntityType(getEntityType(distributionLine, customFieldRequest.getEntityType()));
 
         if (!distributionLine && isDistributionLines(customFieldRequest.getEntityType())) {
         	newFieldDefinition.setFieldName(DISTRIBUTION_LINES);
         } else {
         	newFieldDefinition.setFieldName(getFieldName(readOnly, customFieldRequest));
         }
         
         return newFieldDefinition;
     }
     
     private static String READ_ONLY = "ReadOnly";
     
     private String getFieldName(boolean readOnly, CustomFieldRequest customFieldRequest) {
         return "customFieldMap[" + customFieldRequest.getFieldName() + "]";
     }
     
     private String getROFieldName(boolean readOnly, CustomFieldRequest customFieldRequest) {
         return "customFieldMap[" + customFieldRequest.getFieldName() + (readOnly?READ_ONLY:"") + "]";
     }
     
     private String getFieldDefinitionId(boolean readOnly, boolean distributionLine, CustomFieldRequest customFieldRequest, Site site) {
         String result =  site.getName() + "-";
         String entityType = customFieldRequest.getEntityType();
         if (distributionLine) {
             result += DISTRIBUTION_LINES;
         } else if (isDistributionLines(entityType)) {
             result += entityType.substring(0, entityType.indexOf('.')) + "." + DISTRIBUTION_LINES;
         } else {
         	result = result + entityType;
         } 
         result = result + "." + getROFieldName(readOnly, customFieldRequest);
         return result;
     }
     
     private FieldType getFieldType(boolean readOnly, FieldType fieldType) {
     	if (readOnly) {
     		if (fieldType.equals(FieldType.TEXT)) return FieldType.READ_ONLY_TEXT;
     	}
     	return fieldType;
     }
     
     private static String DISTRIBUTION_LINE = "distributionLine";
     private static String DISTRIBUTION_LINES = "distributionLines";
     
     private static boolean isDistributionLines(String entityType) {
     	return entityType.contains(DISTRIBUTION_LINE);
 
     }
     
     private EntityType getEntityType(boolean distributionLine, String entityType) {
     	if (distributionLine) {
     		entityType = DISTRIBUTION_LINE;
     	} else if (isDistributionLines(entityType)) {
     		entityType = entityType.substring(0, entityType.indexOf('.'));
     	}
     	return EntityType.valueOf(entityType);
     }
     
     private String getPageType(String entityType) {
     	if (isDistributionLines(entityType)) {
     		entityType = entityType.substring(0, entityType.indexOf('.'));
     	}
     	return entityType;
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
         if (isMultiValued(fieldDefinition) && isMultiValued(otherFieldDefinition))
             relationshipType = RelationshipType.MANY_TO_MANY;
         if (!isMultiValued(fieldDefinition) && !isMultiValued(otherFieldDefinition))
             relationshipType = RelationshipType.ONE_TO_ONE;
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
         return StringUtils.trimToNull(customFieldRequest.getRelateToField()) != null && customFieldRequest.getEntityType().equals("constituent");
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
         String lookupSectionName = sectionDefinition.getPageType() + ".constituent.";
         if (isDistributionLines(customFieldRequest.getEntityType())) {
         	lookupSectionName += this.getCustomFieldName(fieldDefinition.getId());
         } else {
         	lookupSectionName += fieldDefinition.getCustomFieldName();
         }
 
         QueryLookup queryLookup = new QueryLookup();
         queryLookup.setFieldDefinition(fieldDefinition);
         queryLookup.setEntityType(EntityType.constituent);
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
             addQuerySectionField(customFieldRequest, lookupSectionDefinition, "constituent.organizationName", 1000);
         }
         if (isIndividual || isBoth) {
             addQuerySectionField(customFieldRequest, lookupSectionDefinition, "constituent.lastName", 1000);
             addQuerySectionField(customFieldRequest, lookupSectionDefinition, "constituent.firstName", 2000);
         }
 
     }
 
     private void addQueryLookupParam(QueryLookup queryLookup, String param) {
         QueryLookupParam queryLookupParam = new QueryLookupParam();
         queryLookupParam.setName(param);
         queryLookupParam.setQueryLookupId(queryLookup.getId());
         pageCustomizationService.maintainQueryLookupParam(queryLookupParam);
     }
 
     private void addQuerySectionField(CustomFieldRequest customFieldRequest,SectionDefinition lookupSectionDefinition, String fieldDefintionId, int fieldOrder) {
         SectionField sectionField = new SectionField();
         sectionField.setSectionDefinition(lookupSectionDefinition);
         sectionField.setFieldOrder(fieldOrder);
         FieldDefinition fieldDefinition = new FieldDefinition();
         fieldDefinition.setId(fieldDefintionId);
         sectionField.setFieldDefinition(fieldDefinition);
         pageCustomizationService.maintainSectionField(sectionField);
     }
 
     private void createPicklist(FieldDefinition fieldDefinition) {
     	String name = fieldDefinition.getFieldName();
     	if (name.equals(DISTRIBUTION_LINES)) {
     		name = getCustomFieldName(fieldDefinition.getId());
     	}
         Picklist picklist = new Picklist();
         picklist.setSite(fieldDefinition.getSite());
         picklist.setPicklistDesc(fieldDefinition.getDefaultLabel());
         picklist.setPicklistName(name);
         picklist.setPicklistNameId(name);
         if (null != picklistItemService.getPicklist(picklist.getPicklistNameId())) throw new RuntimeException("A picklist with this name already exists.");
         picklistItemService.maintainPicklist(picklist);
     }
     
     private String getCustomFieldName(String s) {
     	int i = s.indexOf(".customFieldMap[");
     	if (i > -1) s = s.substring(i+1);
     	return s;
     }
 
     // Modify the guru elements to support new custom field
     private void updateTheGuru(CustomFieldRequest customFieldRequest) {
         pageCustomizationService.maintainCustomFieldGuruData(customFieldRequest);
     }
 
     private void addSectionFieldsAndValidations(PageType pageType, CustomFieldRequest customFieldRequest, FieldDefinition fieldDefinition, Site site) {
     	List<SectionDefinition> sectionDefinitions = new ArrayList<SectionDefinition>();
     	if (pageType == PageType.giftCombinedDistributionLines) {
     		sectionDefinitions = getCombinedDistroLineSections();
     	} else if (isDistributionLines(customFieldRequest.getEntityType())) {
     		sectionDefinitions = getDistroLineSections(pageType);
     	} else {
     		// Default to add to first section.
     		sectionDefinitions = getDefaultSections(pageType);
     	}
     	for (SectionDefinition sectionDefinition : sectionDefinitions) {
     		addSectionFieldAndValidation(sectionDefinition, pageType, customFieldRequest, fieldDefinition, site);
     	}
     }
     
     private void addSectionFieldAndValidation(SectionDefinition sectionDefinition, PageType pageType, CustomFieldRequest customFieldRequest, FieldDefinition fieldDefinition, Site site) {
     	
     	// Default placement after last field.
         int fieldOrder = getNextFieldOrder(sectionDefinition);
 
         SectionField sectionField = getSectionField(fieldOrder, sectionDefinition, fieldDefinition, site);
         FieldDefinition secondaryFieldDefinition = null;
         if (isDistributionLines(customFieldRequest.getEntityType())) {
             secondaryFieldDefinition = new FieldDefinition();
             secondaryFieldDefinition.setId(site.getName() + "-" + fieldDefinition.getId().substring(fieldDefinition.getId().indexOf('.')+1));
         	sectionField.setSecondaryFieldDefinition(secondaryFieldDefinition);
         }
         pageCustomizationService.maintainSectionField(sectionField);
 
        FieldValidation fieldValidation = getFieldValidation(customFieldRequest, fieldDefinition, secondaryFieldDefinition, sectionDefinition);
 
        if ( StringUtils.trimToNull(fieldValidation.getRegex()) != null && !isReadOnly(fieldDefinition.getId()) ) {
            pageCustomizationService.maintainFieldValidation(fieldValidation);
         }
 
         if (isReferenceType(customFieldRequest) && !isReadOnly(fieldDefinition.getId())) {
         	createLookupScreenDefsAndQueryLookups(customFieldRequest, fieldDefinition, sectionDefinition);
         }
 
     }
     
     private boolean isReadOnly(String fieldDefinitionId) {
     	return fieldDefinitionId.endsWith(READ_ONLY+"]");
     }
 
     private SectionField getSectionField(int fieldOrder, SectionDefinition sectionDefinition, FieldDefinition fieldDefinition, Site site) {
         SectionField sectionField = new SectionField();
         sectionField.setFieldOrder(fieldOrder);
         sectionField.setSectionDefinition(sectionDefinition);
         sectionField.setSite(site);
         sectionField.setFieldDefinition(fieldDefinition);
         return sectionField;
     }
 
     private FieldValidation getFieldValidation(CustomFieldRequest customFieldRequest, FieldDefinition fieldDefinition, FieldDefinition secondaryFieldDefinition, SectionDefinition sectionDefinition) {
         FieldValidation fieldValidation = new FieldValidation();
         fieldValidation.setFieldDefinition(fieldDefinition);
         if (secondaryFieldDefinition != null) {
         	fieldValidation.setSecondaryFieldDefinition(secondaryFieldDefinition);
         }
         fieldValidation.setSectionName(sectionDefinition.getSectionName());
         String regex = null;
         String type = customFieldRequest.getValidationType();
         if (type.equals("email")) regex = "extensions:isEmail";
         if (type.equals("url")) regex = "extensions:isUrl";
         if (type.equals("numeric")) regex = "^[0-9]*$";
         if (type.equals("alphanumeric")) regex = "^[a-zA-Z0-9]*$";
         if (type.equals("money")) regex = "^[0-9]*(\\.[0-9][0-9])?$";
         if (type.equals("regex")) regex = customFieldRequest.getRegex().trim();
         fieldValidation.setRegex(regex);
         return fieldValidation;
     }
     
     // Get first section (for all roles).
     private List<SectionDefinition> getDefaultSections(PageType pageType) {
         List<SectionDefinition> result = new ArrayList<SectionDefinition>();
         List<SectionDefinition> definitions = pageCustomizationService.readSectionDefinitionsByPageType(pageType);
         Collections.sort(definitions, new Comparator<SectionDefinition>() {
             @Override
             public int compare(SectionDefinition o1, SectionDefinition o2) {
                 return o1.getSectionOrder().compareTo(o2.getSectionOrder());
             }
         });
         if (definitions.size() == 0)
             throw new RuntimeException("Default page for " + pageType.getName() + " has no sections");
         // Get all sections with a section order that match the lowest section order
         for (SectionDefinition sectionDefinition : definitions) {
         	if (sectionDefinition.getSectionOrder().equals(definitions.get(0).getSectionOrder())) result.add(sectionDefinition);
         }
         return result;
     }
 
     private List<SectionDefinition> getCombinedDistroLineSections() {
         List<SectionDefinition> result = new ArrayList<SectionDefinition>();
         List<SectionDefinition> definitions = pageCustomizationService.readSectionDefinitionsByPageType(PageType.giftCombinedDistributionLines);
 		for (SectionDefinition sd  : definitions) {
 			if (LayoutType.GRID_HIDDEN_ROW.equals(sd.getLayoutType())) {
 				result.add(sd);
 			}
 		}
 	    if (result.size() == 0) throw new RuntimeException("Default page for " + PageType.giftCombinedDistributionLines.getName() + " has no distibution line sections");
 	    return result;
     }
 
     private List<SectionDefinition> getDistroLineSections(PageType pageType) {
         List<SectionDefinition> result = new ArrayList<SectionDefinition>();
         List<SectionDefinition> definitions = pageCustomizationService.readSectionDefinitionsByPageType(pageType);
 		for (SectionDefinition sd  : definitions) {
 			if (LayoutType.GRID_HIDDEN_ROW.equals(sd.getLayoutType())) {
 				result.add(sd);
 			}
 		}
 	    if (result.size() == 0) throw new RuntimeException("Default page for " + pageType.getName() + " has no distibution line sections");
 	    return result;
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
             fieldOrder = sectionFields.get(sectionFields.size() - 1).getFieldOrder().intValue() + 1000;
         }
         return fieldOrder;
     }
 
 
 }
