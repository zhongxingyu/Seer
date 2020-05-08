 package org.jboss.pressgang.ccms.utils.constants;
 
 public class CommonFilterConstants {
     /**
      * The Filter logic keyword to use when two conditions need to be and'ed
      */
     public static final String AND_LOGIC = "And";
     /**
      * The Filter logic keyword to use when two conditions need to be or'ed
      */
     public static final String OR_LOGIC = "Or";
     /**
      * The url variable prefix to indicate that a tag needs to be matched
      */
     public static final String MATCH_TAG = "tag";
     /**
      * The url variable prefix to indicate that a tag will be used for grouping
      */
     public static final String GROUP_TAG = "grouptab";
     /**
      * The url variable prefix to indicate that a locale needs to be matched
      */
     public static final String MATCH_LOCALE = "locale";
     /**
      * The url variable prefix to indicate that a locale will be used for grouping
      */
     public static final String GROUP_LOCALE = "grouplocale";
     /**
      * The URL variable prefix to indicate the internal logic of a category (and
      * optionally also specify a project)
      */
     public static final String CATEORY_INTERNAL_LOGIC = "catint";
     /**
      * The URL variable prefix to indicate the external logic of a category (and
      * optionally also specify a project)
      */
     public static final String CATEORY_EXTERNAL_LOGIC = "catext";
     /**
      * The URL variable the indicates the filter to be used
      */
     public static final String FILTER_ID = "filterId";
     /**
      * The value (as used in the FilterTag database TagState field) the
      * indicates that a tag should be matched
      */
     public static final int MATCH_TAG_STATE = 1;
     /**
      * The value (as used in the FilterTag database TagState field) the
      * indicates that a tag should be excluded
      */
     public static final int NOT_MATCH_TAG_STATE = 0;
     /**
      * The value (as used in the FilterTag database TagState field) the
      * indicates that a tag should be excluded
      */
     public static final int GROUP_TAG_STATE = 2;
 
     /**
      * The value (as used in the FilterLocale database LocaleState field) the
      * indicates that a tag should be matched
      */
     public static final int MATCH_LOCALE_STATE = 1;
     /**
      * The value (as used in the FilterLocale database LocaleState field) the
      * indicates that a locale should be included
      */
     public static final int NOT_MATCH_LOCALE_STATE = 0;
     /**
      * The value (as used in the FilterLocale database LocaleState field) the
      * indicates that a locale should be grouped
      */
     public static final int GROUP_LOCALE_STATE = 2;
 
     /**
      * The value (as used in the FilterCategory database CategoryState field)
      * the indicates that a category has an internal "and" state
      */
     public static final int CATEGORY_INTERNAL_AND_STATE = 0;
     /**
      * The value (as used in the FilterCategory database CategoryState field)
      * the indicates that a category has an internal "or" state
      */
     public static final int CATEGORY_INTERNAL_OR_STATE = 1;
     /**
      * The value (as used in the FilterCategory database CategoryState field)
      * the indicates that a category has an external "and" state
      */
     public static final int CATEGORY_EXTERNAL_AND_STATE = 2;
     /**
      * The value (as used in the FilterCategory database CategoryState field)
      * the indicates that a category has an external "or" state
      */
     public static final int CATEGORY_EXTERNAL_OR_STATE = 3;
 
     /**
      * The URL variable that defines the topic text search field
      */
     public static final String TOPIC_TEXT_SEARCH_FILTER_VAR = "topicTextSearch";
     /**
      * The description of the topic text search field
      */
     public static final String TOPIC_TEXT_SEARCH_FILTER_VAR_DESC = "Topic Text Search";
     /**
      * The URL variable that defines the topic IDs search field
      */
     public static final String TOPIC_IDS_FILTER_VAR = "topicIds";
     /**
      * The description of the topic IDs search field
      */
     public static final String TOPIC_IDS_FILTER_VAR_DESC = "Topic IDs";
     /**
      * The URL variable that defines the not topic IDs search field
      */
     public static final String TOPIC_IDS_NOT_FILTER_VAR = "topicNotIds";
     /**
      * The description of the not topic IDs search field
      */
     public static final String TOPIC_IDS_NOT_FILTER_VAR_DESC = "Not Topic IDs";
     /**
      * The URL variable that defines the topic title search field
      */
     public static final String TOPIC_TITLE_FILTER_VAR = "topicTitle";
     /**
      * The description of the topic title search field
      */
     public static final String TOPIC_TITLE_FILTER_VAR_DESC = "Title";
     /**
      * The URL variable that defines the not topic title search field
      */
     public static final String TOPIC_TITLE_NOT_FILTER_VAR = "topicNotTitle";
     /**
      * The description of the not topic title search field
      */
     public static final String TOPIC_TITLE_NOT_FILTER_VAR_DESC = "Not Title";
     /**
      * The URL variable that defines the topic description search field
      */
     public static final String TOPIC_DESCRIPTION_FILTER_VAR = "topicText";
     /**
      * The description of the topic description search field
      */
     public static final String TOPIC_DESCRIPTION_FILTER_VAR_DESC = "Description";
     /**
      * The URL variable that defines the not topic description search field
      */
     public static final String TOPIC_DESCRIPTION_NOT_FILTER_VAR = "topicNotText";
     /**
      * The description of the not topic description search field
      */
     public static final String TOPIC_DESCRIPTION_NOT_FILTER_VAR_DESC = "Not Description";
     /**
      * The URL variable that defines the topic xml search field
      */
     public static final String TOPIC_XML_FILTER_VAR = "topicXml";
     /**
      * The description of the topic xml search field
      */
     public static final String TOPIC_XML_FILTER_VAR_DESC = "XML";
     /**
      * The URL variable that defines the not topic xml search field
      */
     public static final String TOPIC_XML_NOT_FILTER_VAR = "topicNotXml";
     /**
      * The description of the not topic xml search field
      */
     public static final String TOPIC_XML_NOT_FILTER_VAR_DESC = "Not XML";
     /**
      * The URL variable that defines the start range for the topic create date
      * search field
      */
     public static final String TOPIC_STARTDATE_FILTER_VAR = "startDate";
     /**
      * The description of the start range for the topic create date search field
      */
     public static final String TOPIC_STARTDATE_FILTER_VAR_DESC = "Min Creation Date";
     /**
      * The URL variable that defines the end range for the topic create date
      * search field
      */
     public static final String TOPIC_ENDDATE_FILTER_VAR = "endDate";
     /**
      * The description of the end range for the topic create date search field
      */
     public static final String TOPIC_ENDDATE_FILTER_VAR_DESC = "Max Creation Date";
     /**
      * The URL variable that defines the start edit range for the topic create
      * date search field
      */
     public static final String TOPIC_STARTEDITDATE_FILTER_VAR = "startEditDate";
     /**
      * The description of the start edit range for the topic create date search
      * field
      */
     public static final String TOPIC_STARTEDITDATE_FILTER_VAR_DESC = "Min Edited Date";
     /**
      * The URL variable that defines the end edit range for the topic create
      * date search field
      */
     public static final String TOPIC_ENDEDITDATE_FILTER_VAR = "endEditDate";
     /**
      * The description of the end edit range for the topic create date search
      * field
      */
     public static final String TOPIC_ENDEDITDATE_FILTER_VAR_DESC = "Max Edited Date";
     /**
      * The URL variable that defines the logic to be applied to the search
      * fields
      */
     public static final String LOGIC_FILTER_VAR = "logic";
     /**
      * The description the logic to be applied to the search fields
      */
     public static final String LOGIC_FILTER_VAR_DESC = "Field Logic";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_HAS_OPEN_BUGZILLA_BUGS = "topicHasOpenBugzillaBugs";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_HAS_OPEN_BUGZILLA_BUGS_DESC = "Has Open Bugzilla Bugs";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_HAS_NOT_OPEN_BUGZILLA_BUGS = "topicHasNotOpenBugzillaBugs";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_HAS_NOT_OPEN_BUGZILLA_BUGS_DESC = "Doesn't have Open Bugzilla Bugs";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_HAS_BUGZILLA_BUGS = "topicHasBugzillaBugs";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_HAS_BUGZILLA_BUGS_DESC = "Has Bugzilla Bugs";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_HAS_NOT_BUGZILLA_BUGS = "topicHasNotBugzillaBugs";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_HAS_NOT_BUGZILLA_BUGS_DESC = "Doesn't have Bugzilla Bugs";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_HAS_RELATIONSHIPS = "topicHasRelationships";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_HAS_RELATIONSHIPS_DESC = "Has Relationships";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_HAS_NOT_RELATIONSHIPS = "topicHasNotRelationships";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_HAS_NOT_RELATIONSHIPS_DESC = "Doesn't have Relationships";
     /**
      * The URL variable that defines the has incoming relationships search field
      */
     public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS = "topicHasIncomingRelationships";
     /**
      * The description of the has incoming relationships search field
      */
     public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS_DESC = "Has Incoming Relationships";
     /**
      * The URL variable that defines the has incoming relationships search field
      */
     public static final String TOPIC_HAS_NOT_INCOMING_RELATIONSHIPS = "topicHasNotIncomingRelationships";
     /**
      * The description of the has incoming relationships search field
      */
     public static final String TOPIC_HAS_NOT_INCOMING_RELATIONSHIPS_DESC = "Doesn't Have Incoming Relationships";
     /**
      * The URL variable that defines the has related to search field
      */
     public static final String TOPIC_RELATED_TO = "topicRelatedTo";
     /**
      * The description of the has related to search field
      */
     public static final String TOPIC_RELATED_TO_DESC = "Related To";
     /**
      * The URL variable that defines the has related from search field
      */
     public static final String TOPIC_RELATED_FROM = "topicRelatedFrom";
     /**
      * The description of the has related from search field
      */
     public static final String TOPIC_RELATED_FROM_DESC = "Related From";
     /**
      * The URL variable that defines the has related to search field
      */
     public static final String TOPIC_NOT_RELATED_TO = "topicNotRelatedTo";
     /**
      * The description of the has related to search field
      */
     public static final String TOPIC_NOT_RELATED_TO_DESC = "Not Related To";
     /**
      * The URL variable that defines the has related from search field
      */
     public static final String TOPIC_NOT_RELATED_FROM = "topicNotRelatedFrom";
     /**
      * The description of the has related from search field
      */
     public static final String TOPIC_NOT_RELATED_FROM_DESC = "NotRelated From";
     /**
      * The URL variable that defines the has related from search field
      */
     public static final String TOPIC_HAS_XML_ERRORS = "topicHasXMLErrors";
     /**
      * The description of the has related from search field
      */
     public static final String TOPIC_HAS_XML_ERRORS_DESC = "Topic Has XML Errors";
     /**
      * The URL variable that defines the has related from search field
      */
     public static final String TOPIC_HAS_NOT_XML_ERRORS = "topicHasNotXMLErrors";
     /**
      * The description of the has related from search field
      */
     public static final String TOPIC_HAS_NOT_XML_ERRORS_DESC = "Topic doesn't have XML Errors";
     /**
      * The URL variable that defines the has related from search field
      */
     public static final String TOPIC_EDITED_IN_LAST_DAYS = "topicEditedInLastDays";
     /**
      * The description of the has related from search field
      */
     public static final String TOPIC_EDITED_IN_LAST_DAYS_DESC = "Topic Edited In Last Days";
     /**
      * The URL variable that defines the has related from search field
      */
     public static final String TOPIC_NOT_EDITED_IN_LAST_DAYS = "topicNotEditedInLastDays";
     /**
      * The description of the has related from search field
      */
     public static final String TOPIC_NOT_EDITED_IN_LAST_DAYS_DESC = "Topic Not Edited In Last Days";
     /**
      * The URL variable that defines the topic property tag
      */
     public static final String PROPERTY_TAG = "propertyTag";
     /**
      * The description of the property tag search field
      */
     public static final String PROPERTY_TAG_DESC = "Property Tag";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_IS_INCLUDED_IN_SPEC = "topicIncludedInSpec";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_IS_INCLUDED_IN_SPEC_DESC = "Topics Included In Spec";
     /**
      * The URL variable that defines the has relationships search field
      */
     public static final String TOPIC_IS_NOT_INCLUDED_IN_SPEC = "topicNotIncludedInSpec";
     /**
      * The description of the has relationships search field
      */
     public static final String TOPIC_IS_NOT_INCLUDED_IN_SPEC_DESC = "Topics Not Included In Spec";
 
     /**
      * The URL variable that defines if translated topics should be filter to only include the latest copy
      */
     public static final String TOPIC_LATEST_TRANSLATIONS_FILTER_VAR = "latestTranslations";
     /**
      * The description of the latest translated topic search field
      */
     public static final String TOPIC_LATEST_TRANSLATIONS_FILTER_VAR_DESC = "Latest Translations";
     /**
      * The URL variable that defines if translated topics should be filter to only include the latest finished copy
      */
     public static final String TOPIC_LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR = "latestCompletedTranslations";
     /**
      * The description of the latest completed translated topic search field
      */
     public static final String TOPIC_LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR_DESC = "Latest Completed Translations";
     /**
      * The URL variable that defines if translated topics should be filter to only include the latest copy
      */
     public static final String TOPIC_NOT_LATEST_TRANSLATIONS_FILTER_VAR = "notLatestTranslations";
     /**
      * The description of the latest translated topic search field
      */
     public static final String TOPIC_NOT_LATEST_TRANSLATIONS_FILTER_VAR_DESC = "Not Latest Translations";
     /**
      * The URL variable that defines if translated topics should be filter to only include the latest finished copy
      */
     public static final String TOPIC_NOT_LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR = "notLatestCompletedTranslations";
     /**
      * The description of the latest completed translated topic search field
      */
     public static final String TOPIC_NOT_LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR_DESC = "Not Latest Completed Translations";
 
     /**
      * The URL variable that defines the topic IDs search field
      */
     public static final String ZANATA_IDS_FILTER_VAR = "zanataIds";
     /**
      * The description of the topic IDs search field
      */
     public static final String ZANATA_IDS_FILTER_VAR_DESC = "Zanata IDs";
     /**
      * The URL variable that defines the not topic IDs search field
      */
     public static final String ZANATA_IDS_NOT_FILTER_VAR = "zanataNotIds";
     /**
      * The description of the not topic IDs search field
      */
     public static final String ZANATA_IDS_NOT_FILTER_VAR_DESC = "Not Zanata IDs";
 
     /* Tag Filter Constants */
     public static final String TAG_IDS_FILTER_VAR = "tagIds";
     public static final String TAG_IDS_FILTER_VAR_DESC = "Tag IDs";
 
     public static final String TAG_NAME_FILTER_VAR = "tagName";
     public static final String TAG_NAME_FILTER_VAR_DESC = "Tag Name";
 
     public static final String TAG_DESCRIPTION_FILTER_VAR = "tagDesc";
     public static final String TAG_DESCRIPTION_FILTER_VAR_DESC = "Tag Description";
 
     /* Category Filter Constants */
     public static final String CATEGORY_IDS_FILTER_VAR = "catIds";
     public static final String CATEGORY_IDS_FILTER_VAR_DESC = "Category IDs";
 
     public static final String CATEGORY_NAME_FILTER_VAR = "catName";
     public static final String CATEGORY_NAME_FILTER_VAR_DESC = "Category Name";
 
     public static final String CATEGORY_DESCRIPTION_FILTER_VAR = "catDesc";
     public static final String CATEGORY_DESCRIPTION_FILTER_VAR_DESC = "Category Description";
 
     public static final String CATEGORY_IS_MUTUALLY_EXCLUSIVE_VAR = "isMutuallyExclusive";
     public static final String CATEGORY_IS_MUTUALLY_EXCLUSIVE_VAR_DESC = "Is Mutually Exclusive";
     public static final String CATEGORY_IS_NOT_MUTUALLY_EXCLUSIVE_VAR = "notMutuallyExclusive";
     public static final String CATEGORY_IS_NOT_MUTUALLY_EXCLUSIVE_VAR_DESC = "Is Not Mutually Exclusive";
 
     /* User Filter Constants */
     public static final String USER_IDS_FILTER_VAR = "userIds";
     public static final String USER_IDS_FILTER_VAR_DESC = "User IDs";
 
     public static final String USER_NAME_FILTER_VAR = "username";
     public static final String USER_NAME_FILTER_VAR_DESC = "Username";
 
     public static final String USER_DESCRIPTION_FILTER_VAR = "userDesc";
     public static final String USER_DESCRIPTION_FILTER_VAR_DESC = "User Description";
 
     /* Image Filter Constants */
     public static final String IMAGE_IDS_FILTER_VAR = "imageIds";
     public static final String IMAGE_IDS_FILTER_VAR_DESC = "Image IDs";
 
     public static final String IMAGE_DESCRIPTION_FILTER_VAR = "imageDesc";
     public static final String IMAGE_DESCRIPTION_FILTER_VAR_DESC = "Image Description";
 
     public static final String IMAGE_ORIGINAL_FILENAME_FILTER_VAR = "imageOrigName";
     public static final String IMAGE_ORIGINAL_FILENAME_FILTER_VAR_DESC = "Image Original Filename";
 
     /* Project Filter Constant */
     public static final String PROJECT_IDS_FILTER_VAR = "projectIds";
     public static final String PROJECT_IDS_FILTER_VAR_DESC = "Project IDs";
 
     public static final String PROJECT_NAME_FILTER_VAR = "projectName";
     public static final String PROJECT_NAME_FILTER_VAR_DESC = "Project Name";
 
     public static final String PROJECT_DESCRIPTION_FILTER_VAR = "projectDesc";
     public static final String PROJECT_DESCRIPTION_FILTER_VAR_DESC = "Project Description";
 
     /* Filter Constants */
     public static final String FILTER_IDS_FILTER_VAR = "filterIds";
     public static final String FILTER_IDS_FILTER_VAR_DESC = "Filter IDs";
 
     public static final String FILTER_NAME_FILTER_VAR = "filterName";
     public static final String FILTER_NAME_FILTER_VAR_DESC = "Filter Name";
 
     public static final String FILTER_DESCRIPTION_FILTER_VAR = "filterDesc";
     public static final String FILTER_DESCRIPTION_FILTER_VAR_DESC = "Filter Description";
 
     /* PropertyTag Constants */
     public static final String PROP_TAG_IDS_FILTER_VAR = "propTagIds";
     public static final String PROP_TAG_IDS_FILTER_VAR_DESC = "Property Tag IDs";
 
     public static final String PROP_TAG_NAME_FILTER_VAR = "propTagName";
     public static final String PROP_TAG_NAME_FILTER_VAR_DESC = "Property Tag Name";
 
     public static final String PROP_TAG_DESCRIPTION_FILTER_VAR = "propTagDesc";
     public static final String PROP_TAG_DESCRIPTION_FILTER_VAR_DESC = "Property Tag Description";
 
     /* BlobConstant Constants */
     public static final String BLOB_CONSTANT_IDS_FILTER_VAR = "blobConstantIds";
     public static final String BLOB_CONSTANT_IDS_FILTER_VAR_DESC = "Blob Constant IDs";
 
     public static final String BLOB_CONSTANT_NAME_FILTER_VAR = "blobConstantName";
     public static final String BLOB_CONSTANT_NAME_FILTER_VAR_DESC = "Blob Constant Name";
 
     /* StringConstant Constants */
     public static final String STRING_CONSTANT_IDS_FILTER_VAR = "stringConstantIds";
     public static final String STRING_CONSTANT_IDS_FILTER_VAR_DESC = "String Constant IDs";
 
     public static final String STRING_CONSTANT_NAME_FILTER_VAR = "stringConstantName";
     public static final String STRING_CONSTANT_NAME_FILTER_VAR_DESC = "String Constant Name";
 
     public static final String STRING_CONSTANT_VALUE_FILTER_VAR = "stringConstantValue";
     public static final String STRING_CONSTANT_VALUE_FILTER_VAR_DESC = "String Constant Value";
 
     /* Role Constants */
     public static final String ROLE_IDS_FILTER_VAR = "roleIds";
     public static final String ROLE_IDS_FILTER_VAR_DESC = "Role IDs";
 
     public static final String ROLE_NAME_FILTER_VAR = "roleName";
     public static final String ROLE_NAME_FILTER_VAR_DESC = "Role Name";
 
     public static final String ROLE_DESCRIPTION_FILTER_VAR = "roleDesc";
     public static final String ROLE_DESCRIPTION_FILTER_VAR_DESC = "Role Description";
 
     /* IntegerConstant Constants */
     public static final String INTEGER_CONSTANT_IDS_FILTER_VAR = "integerConstantIds";
     public static final String INTEGER_CONSTANT_IDS_FILTER_VAR_DESC = "Integer Constant IDs";
 
     public static final String INTEGER_CONSTANT_NAME_FILTER_VAR = "integerConstantName";
     public static final String INTEGER_CONSTANT_NAME_FILTER_VAR_DESC = "Integer Constant Name";
 
     public static final String INTEGER_CONSTANT_VALUE_FILTER_VAR = "integerConstantValue";
     public static final String INTEGER_CONSTANT_VALUE_FILTER_VAR_DESC = "Integer Constant Value";
 
     /* PropertyTagCategory Constants */
     public static final String PROP_CATEGORY_IDS_FILTER_VAR = "propCategoryIds";
     public static final String PROP_CATEGORY_IDS_FILTER_VAR_DESC = "Property Tag Category IDs";
 
     public static final String PROP_CATEGORY_NAME_FILTER_VAR = "propCategoryName";
     public static final String PROP_CATEGORY_NAME_FILTER_VAR_DESC = "Property Tag Category Name";
 
     public static final String PROP_CATEGORY_DESCRIPTION_FILTER_VAR = "propCategoryDesc";
     public static final String PROP_CATEGORY_DESCRIPTION_FILTER_VAR_DESC = "Property Tag Category Description";
 
     /* ContentSpec Constants */
     public static final String CONTENT_SPEC_IDS_FILTER_VAR = "contentSpecIds";
     public static final String CONTENT_SPEC_IDS_FILTER_VAR_DESC = "Content Specification IDs";
 
     public static final String CONTENT_SPEC_TITLE_FILTER_VAR = "contentSpecTitle";
     public static final String CONTENT_SPEC_TITLE_FILTER_VAR_DESC = "Content Specification Title";
 
     public static final String CONTENT_SPEC_PRODUCT_FILTER_VAR = "contentSpecProduct";
     public static final String CONTENT_SPEC_PRODUCT_FILTER_VAR_DESC = "Content Specification Product";
 
     public static final String CONTENT_SPEC_VERSION_FILTER_VAR = "contentSpecVersion";
     public static final String CONTENT_SPEC_VERSION_FILTER_VAR_DESC = "Content Specification Version";
 
     /* CSNode Constants */
     public static final String CONTENT_SPEC_NODE_IDS_FILTER_VAR = "csNodeIds";
     public static final String CONTENT_SPEC_NODE_IDS_FILTER_VAR_DESC = "Content Specification Node IDs";
 
     public static final String CONTENT_SPEC_NODE_TITLE_FILTER_VAR = "csNodeTitle";
     public static final String CONTENT_SPEC_NODE_TITLE_FILTER_VAR_DESC = "Content Specification Node Title";
 
     public static final String CONTENT_SPEC_NODE_TYPE_FILTER_VAR = "csNodeType";
     public static final String CONTENT_SPEC_NODE_TYPE_FILTER_VAR_DESC = "Content Specification Node Type";
 
    /* Translated ContentSpec Constants */
    public static final String TRANSLATED_CONTENT_SPEC_IDS_FILTER_VAR = "translatedContentSpecIds";
    public static final String TRANSLATED_CONTENT_SPEC_IDS_FILTER_VAR_DESC = "Translated Content Specification IDs";

    /* TranslatedCSNode Constants */
    public static final String CONTENT_SPEC_TRANSLATED_NODE_IDS_FILTER_VAR = "translatedCSNodeIds";
    public static final String CONTENT_SPEC_TRANSLATED_NODE_IDS_FILTER_VAR_DESC = "Translated Content Specification Node IDs";
 
     /* CSMetaData Constants */
     public static final String CONTENT_SPEC_META_DATA_IDS_FILTER_VAR = "csMetaDataIds";
     public static final String CONTENT_SPEC_META_DATA_IDS_FILTER_VAR_DESC = "Content Specification IDs";
 
     public static final String CONTENT_SPEC_META_DATA_TITLE_FILTER_VAR = "csMetaDataTitle";
     public static final String CONTENT_SPEC_META_DATA_TITLE_FILTER_VAR_DESC = "Content Specification Meta Data Title";
 }
