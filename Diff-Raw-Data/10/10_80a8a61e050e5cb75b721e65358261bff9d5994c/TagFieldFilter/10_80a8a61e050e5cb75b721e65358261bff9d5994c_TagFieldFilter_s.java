 package org.jboss.pressgang.ccms.filter;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.filter.base.BaseMultiFieldFilter;
 import org.jboss.pressgang.ccms.filter.structures.FilterFieldBooleanMapData;
 import org.jboss.pressgang.ccms.filter.structures.FilterFieldStringData;
 import org.jboss.pressgang.ccms.filter.structures.FilterFieldStringMapData;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class TagFieldFilter extends BaseMultiFieldFilter {
     private static final Logger log = LoggerFactory.getLogger(TagFieldFilter.class);
 
     /**
      * A map of the base filter field names that can not have multiple mappings
      */
     private static final Map<String, String> filterNames = Collections.unmodifiableMap(new HashMap<String, String>() {
         private static final long serialVersionUID = 4454656533723964663L;
 
         {
             put(CommonFilterConstants.TAG_IDS_FILTER_VAR, CommonFilterConstants.TAG_IDS_FILTER_VAR_DESC);
             put(CommonFilterConstants.TAG_NAME_FILTER_VAR, CommonFilterConstants.TAG_NAME_FILTER_VAR_DESC);
             put(CommonFilterConstants.TAG_DESCRIPTION_FILTER_VAR, CommonFilterConstants.TAG_DESCRIPTION_FILTER_VAR_DESC);
         }
     });
 
     private FilterFieldStringData tagIds;
     private FilterFieldStringData tagName;
     private FilterFieldStringData tagDescription;
     private FilterFieldStringMapData propertyTags;
     private FilterFieldBooleanMapData propertyTagExists;
     private FilterFieldBooleanMapData propertyTagNotExists;
 
     public TagFieldFilter() {
         resetAllValues();
     }
 
     @Override
     protected void resetAllValues() {
         super.resetAllValues();
 
         tagIds = new FilterFieldStringData(CommonFilterConstants.TAG_IDS_FILTER_VAR, CommonFilterConstants.TAG_IDS_FILTER_VAR_DESC);
         tagName = new FilterFieldStringData(CommonFilterConstants.TAG_NAME_FILTER_VAR, CommonFilterConstants.TAG_NAME_FILTER_VAR_DESC);
         tagDescription = new FilterFieldStringData(CommonFilterConstants.TAG_DESCRIPTION_FILTER_VAR,
                 CommonFilterConstants.TAG_DESCRIPTION_FILTER_VAR_DESC);
         propertyTags = new FilterFieldStringMapData(CommonFilterConstants.PROPERTY_TAG, CommonFilterConstants.PROPERTY_TAG_DESC);
         propertyTagExists = new FilterFieldBooleanMapData(CommonFilterConstants.PROPERTY_TAG_EXISTS,
                 CommonFilterConstants.PROPERTY_TAG_EXISTS_DESC);
         propertyTagNotExists = new FilterFieldBooleanMapData(CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS,
                 CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS_DESC);
 
         addFilterVar(tagIds);
         addFilterVar(tagName);
         addFilterVar(tagDescription);
 
         addMultiFilterVar(propertyTags);
         addMultiFilterVar(propertyTagExists);
         addMultiFilterVar(propertyTagNotExists);
     }
 
     @Override
     public Map<String, String> getFieldNames() {
         final Map<String, String> retValue = super.getFieldNames();
         retValue.putAll(filterNames);
         retValue.put(CommonFilterConstants.PROPERTY_TAG + "\\d+", CommonFilterConstants.PROPERTY_TAG_DESC);
         retValue.put(CommonFilterConstants.PROPERTY_TAG_EXISTS + "\\d+", CommonFilterConstants.PROPERTY_TAG_EXISTS_DESC);
         retValue.put(CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS + "\\d+", CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS_DESC);
 
         return retValue;
     }
 
     @Override
     public Map<String, String> getBaseFieldNames() {
        final Map<String, String> retValue = super.getFieldNames();
         retValue.putAll(filterNames);
         retValue.put(CommonFilterConstants.PROPERTY_TAG, CommonFilterConstants.PROPERTY_TAG_DESC);
         retValue.put(CommonFilterConstants.PROPERTY_TAG_EXISTS, CommonFilterConstants.PROPERTY_TAG_EXISTS_DESC);
         retValue.put(CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS, CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS_DESC);
 
         return retValue;
     }
 }
