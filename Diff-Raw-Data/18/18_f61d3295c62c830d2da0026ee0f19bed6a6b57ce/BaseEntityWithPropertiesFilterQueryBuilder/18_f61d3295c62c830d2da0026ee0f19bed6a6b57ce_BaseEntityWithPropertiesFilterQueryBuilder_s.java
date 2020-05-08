 package org.jboss.pressgang.ccms.filter.base;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.Subquery;
 
 import com.google.code.regexp.Matcher;
 import com.google.code.regexp.Pattern;
 import org.jboss.pressgang.ccms.model.base.ToPropertyTag;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Provides the query elements required by Filter.buildQuery() to get a list of Entities that have extended properties.
  *
  * @param <T> The Type of entity that should be returned by the query builder.
  * @param <U> The Type of the entity to PropertyTag mapping.
  */
 public abstract class BaseEntityWithPropertiesFilterQueryBuilder<T, U extends ToPropertyTag<U>> extends BaseFilterQueryBuilder<T> {
     private static final Logger log = LoggerFactory.getLogger(BaseEntityWithPropertiesFilterQueryBuilder.class);
 
     protected BaseEntityWithPropertiesFilterQueryBuilder(Class<T> clazz, EntityManager entityManager) {
         super(clazz, entityManager);
     }
 
     @Override
     public void processFilterString(final String fieldName, final String fieldValue) {
        if (fieldName.startsWith(CommonFilterConstants.PROPERTY_TAG)) {
            final Integer propertyTagId = getPropertyTagId(CommonFilterConstants.PROPERTY_TAG, fieldName);

            if (propertyTagId != null && fieldValue != null) {
                addExistsCondition(getPropertyTagSubquery(propertyTagId, fieldValue));
            }
        } else if (fieldName.equals(CommonFilterConstants.PROPERTY_TAG_EXISTS)) {
             final Integer propertyTagId = getPropertyTagId(CommonFilterConstants.PROPERTY_TAG_EXISTS, fieldName);
             final Boolean fieldValueBoolean = Boolean.parseBoolean(fieldValue);
             if (propertyTagId != null && fieldValueBoolean) {
                 addFieldCondition(getCriteriaBuilder().exists(getPropertyTagExistsSubquery(propertyTagId)));
             }
        } else if (fieldName.equals(CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS)) {
             final Integer propertyTagId = getPropertyTagId(CommonFilterConstants.PROPERTY_TAG_NOT_EXISTS, fieldName);
             final Boolean fieldValueBoolean = Boolean.parseBoolean(fieldValue);
             if (propertyTagId != null && fieldValueBoolean) {
                 addFieldCondition(getCriteriaBuilder().not(getCriteriaBuilder().exists(getPropertyTagExistsSubquery(propertyTagId))));
             }
         } else {
             super.processFilterString(fieldName, fieldValue);
         }
     }
 
     /**
      * Get the PropertyTag ID from a field name
      *
      * @param fieldName The field name
      * @param value The value to get the Property Tag ID from.
      * @return The ID of the PropertyTag
      */
     private Integer getPropertyTagId(String fieldName, String value) {
         try {
             final Pattern pattern = Pattern.compile("^" + fieldName + "(?<PropertyTagID>\\d+)$");
             final Matcher matcher = pattern.matcher(value);
 
             while (matcher.find()) {
                 final String propertyTagIdString = matcher.group("PropertyTagID");
 
                 if (propertyTagIdString != null) {
                     final Integer propertyTagIdInt = Integer.parseInt(propertyTagIdString);
                     return propertyTagIdInt;
                 }
 
                 /* should only match once */
                 break;
             }
         } catch (final NumberFormatException ex) {
             /*
              * could not parse integer, so fail. this shouldn't happen though, as the string is matched by a regex that will
              * only allow numbers
              */
             log.debug("Malformed Filter query parameter for the \"{}\" parameter. Value = {}", fieldName, fieldName);
         }
 
         return null;
     }
 
     /**
      * Create a Subquery to check if a topic has a property tag with a specific value.
      *
      * @param propertyTagId    The ID of the property tag to be checked.
      * @param propertyTagValue The Value that the property tag should have.
      * @return A subquery that can be used in an exists statement to see if a topic has a property tag with the specified value.
      */
     protected abstract Subquery<U> getPropertyTagSubquery(final Integer propertyTagId, final String propertyTagValue);
 
     /**
      * Create a Subquery to check if a entity has a property tag exists.
      *
      * @param propertyTagId The ID of the property tag to be checked.
      * @return A subquery that can be used in an exists statement to see if a topic has a property tag.
      */
     protected abstract Subquery<U> getPropertyTagExistsSubquery(final Integer propertyTagId);
 }
