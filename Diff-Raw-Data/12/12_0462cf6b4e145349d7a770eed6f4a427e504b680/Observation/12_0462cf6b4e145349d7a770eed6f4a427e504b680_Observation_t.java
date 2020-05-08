 package com.jtbdevelopment.e_eye_o.entities;
 
 import com.jtbdevelopment.e_eye_o.entities.annotations.IdObjectEntitySettings;
 import com.jtbdevelopment.e_eye_o.entities.annotations.IdObjectFieldSettings;
 import com.jtbdevelopment.e_eye_o.entities.validation.NoNullsInCollectionCheck;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.joda.time.LocalDateTime;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.util.Collection;
 import java.util.Set;
 
 /**
  * Date: 11/25/12
  * Time: 3:15 PM
  */
 @IdObjectEntitySettings(defaultPageSize = 25, defaultSortField = "observationTimestamp", defaultSortAscending = false,
         singular = "Observation", plural = "Observations",
         viewFieldOrder = {"observationSubject", "observationTimestamp", "comment", "categories", "significant", "modificationTimestamp", "archived"},
         editFieldOrder = {"comment", "categories", IdObjectEntitySettings.SECTION_BREAK, "observationSubject", "significant", "observationTimestamp"})
 public interface Observation extends AppUserOwnedObject {
 
     public final static String OBSERVATION_COMMENT_CANNOT_BE_BLANK_OR_NULL_ERROR = "Observation.comment" + CANNOT_BE_BLANK_OR_NULL_ERROR;
     public final static String OBSERVATION_CATEGORIES_CANNOT_CONTAIN_NULL_ERROR = "Observation.categories" + CANNOT_CONTAIN_NULL_ERROR;
     public final static String OBSERVATION_CATEGORIES_CANNOT_BE_NULL_ERROR = "Observation.categories" + CANNOT_BE_NULL_ERROR;
     public final static String OBSERVATION_OBSERVATION_TIMESTAMP_CANNOT_BE_NULL_ERROR = "Observation.observationTimestamp" + CANNOT_BE_NULL_ERROR;
     public final static String OBSERVATION_OBSERVATION_SUBJECT_CANNOT_BE_NULL_ERROR = "Observation.observationSubject" + CANNOT_BE_NULL_ERROR;
     public final static int MAX_COMMENT_SIZE = 5000;
     public final static String OBSERVATION_COMMENT_SIZE_ERROR = "Observation.comment cannot be longer than " + MAX_COMMENT_SIZE + " characters.";
 
    public final static String DEFAULT_SIGNIFICANT_SETTING = "entity.observation.defaultsignificant";

     @NotNull(message = OBSERVATION_OBSERVATION_SUBJECT_CANNOT_BE_NULL_ERROR)
     @IdObjectFieldSettings(label = "Observation For", fieldType = IdObjectFieldSettings.DisplayFieldType.SINGLE_SELECT_LIST, alignment = IdObjectFieldSettings.DisplayAlignment.LEFT)
     Observable getObservationSubject();
 
     void setObservationSubject(final Observable observationSubject);
 
     @NotNull(message = OBSERVATION_OBSERVATION_TIMESTAMP_CANNOT_BE_NULL_ERROR)
     @IdObjectFieldSettings(label = "Observation Time", fieldType = IdObjectFieldSettings.DisplayFieldType.LOCAL_DATE_TIME, alignment = IdObjectFieldSettings.DisplayAlignment.CENTER)
     LocalDateTime getObservationTimestamp();
 
     void setObservationTimestamp(final LocalDateTime observationDate);
 
    @IdObjectFieldSettings(label = "Significant?", fieldType = IdObjectFieldSettings.DisplayFieldType.CHECKBOX, alignment = IdObjectFieldSettings.DisplayAlignment.CENTER, defaultValueSetting = DEFAULT_SIGNIFICANT_SETTING)
     boolean isSignificant();
 
     void setSignificant(final boolean significant);
 
     @NotNull(message = OBSERVATION_CATEGORIES_CANNOT_BE_NULL_ERROR)
     @NoNullsInCollectionCheck(message = OBSERVATION_CATEGORIES_CANNOT_CONTAIN_NULL_ERROR)
     @IdObjectFieldSettings(label = "Categories", height = 7, fieldType = IdObjectFieldSettings.DisplayFieldType.MULTI_SELECT_PICKER, alignment = IdObjectFieldSettings.DisplayAlignment.CENTER)
     Set<ObservationCategory> getCategories();
 
     void setCategories(final Set<ObservationCategory> categories);
 
     void addCategory(final ObservationCategory observationCategory);
 
     void addCategories(final Collection<ObservationCategory> observationCategories);
 
     void removeCategory(final ObservationCategory observationCategory);
 
     @NotEmpty(message = OBSERVATION_COMMENT_CANNOT_BE_BLANK_OR_NULL_ERROR)
     @Size(max = MAX_COMMENT_SIZE, message = OBSERVATION_COMMENT_SIZE_ERROR)
     @IdObjectFieldSettings(label = "Comment", width = 40, height = 9, fieldType = IdObjectFieldSettings.DisplayFieldType.MULTI_LINE_TEXT, alignment = IdObjectFieldSettings.DisplayAlignment.LEFT)
     String getComment();
 
     void setComment(final String comment);
 }
