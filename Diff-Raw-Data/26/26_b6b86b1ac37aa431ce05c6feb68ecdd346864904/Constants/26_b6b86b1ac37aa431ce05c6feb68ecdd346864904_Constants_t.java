 package org.jboss.pressgangccms.restserver.constants;
 
 public final class Constants
 {
	/** The base URL from which the REST interface can be accessed */
	public static final String BASE_REST_PATH = "/";
	
 	/* The following database key constants are defined in import.sql */
 	
 	/** The Content Specification tag ID */
 	public static final Integer CONTENT_SPEC_TAG_ID = 4;
 	/** The Concept tag ID */
 	public static final Integer CONCEPT_TAG_ID = 1;
 	/** The Concept tag name */
 	public static final String CONCEPT_TAG_NAME = "Concept";
 	/** The Task tag ID */
 	public static final Integer TASK_TAG_ID = 2;
 	/** The Task tag name */
 	public static final String TASK_TAG_NAME = "Task";
 	/** The Reference tag ID */
 	public static final Integer REFERENCE_TAG_ID = 3;
 	/** The Reference tag name */
 	public static final String REFERENCE_TAG_NAME = "Reference";	
 	/** The string constant that is used as a reference template */
 	public static final Integer REFERENCE_TOPIC_STRINGCONSTANTID = 2;
 	/** The string constant that is used as a task template */
 	public static final Integer TASK_TOPIC_STRINGCONSTANTID = 1;
 	/** The string constant that is used as a concept template */
 	public static final Integer CONCEPT_TOPIC_STRINGCONSTANTID = 3;
 	
 	/**
 	 * A Topic ID that no topic should ever match
 	 */
 	public static final String NULL_TOPIC_ID = "-1";
 
 	/**
 	 * The default size to use for batch fetching.
 	 */
 	public static final int DEFAULT_BATCH_SIZE = 15;
 	
 	
 	
 	public static final String PROPERTY_TAG_SELECT_ITEM_VALUE_PREFIX = "PropertyTag";
 	public static final String UNCATEGORISED_PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE = "UncategorisedPropertyTagCategory";
 	public static final String PROPERTY_TAG_CATEGORY_SELECT_ITEM_VALUE_PREFIX = "PropertyTagCategory";
 	public static final String UNCATEGORISED_PROPERTY_TAG_CATEGORY_SELECT_ITEM_LABEL = "Uncategorised";
 	public static final String PROPERTY_TAG_SELECT_LABEL_PREFIX = "- ";
 	
 	
	/** The URL variable the indicates the filter to be used */
	public static final String FILTER_ID = "filterId";
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
 	 * The default internal category logic state 
 	 */
 	public static final int CATEGORY_INTERNAL_DEFAULT_STATE = CATEGORY_INTERNAL_OR_STATE;
 	/** 
 	 * The default external category logic state 
 	 */
 	public static final int CATEGORY_EXTERNAL_DEFAULT_STATE = CATEGORY_EXTERNAL_AND_STATE;
 	
 	/** 
 	 * The SQL logic keyword to use when two conditions need to be and'ed 
 	 */
 	public static final String AND_LOGIC = "And";
 	/** 
 	 * The SQL logic keyword to use when two conditions need to be or'ed 
 	 */
 	public static final String OR_LOGIC = "Or";
 	/** 
 	 * The default logic to be applied to tags within a category 
 	 */
 	public static final String DEFAULT_INTERNAL_LOGIC = OR_LOGIC;
 	/** 
 	 * The default logic to be applied between categories 
 	 */
 	public static final String DEFAULT_EXTERNAL_LOGIC = AND_LOGIC;
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
 	 * The URL variable that defines the logic to be applied to the search
 	 * fields
 	 */
 	public static final String TOPIC_LOGIC_FILTER_VAR = "logic";
 	/** 
 	 * The default logic to be applied to the search fields 
 	 */
 	public static final String TOPIC_LOGIC_FILTER_VAR_DEFAULT_VALUE = "and";
 	/** The description the logic to be applied to the search fields */
 	public static final String TOPIC_LOGIC_FILTER_VAR_DESC = "Field Logic";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_HAS_OPEN_BUGZILLA_BUGS = "topicHasOpenBugzillaBugs";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_HAS_OPEN_BUGZILLA_BUGS_DESC = "Has Open Bugzilla Bugs";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_HAS_NOT_OPEN_BUGZILLA_BUGS = "topicHasNotOpenBugzillaBugs";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_HAS_NOT_OPEN_BUGZILLA_BUGS_DESC = "Doesn't have Open Bugzilla Bugs";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_HAS_BUGZILLA_BUGS = "topicHasBugzillaBugs";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_HAS_BUGZILLA_BUGS_DESC = "Has Bugzilla Bugs";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_HAS_NOT_BUGZILLA_BUGS = "topicHasNotBugzillaBugs";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_HAS_NOT_BUGZILLA_BUGS_DESC = "Doesn't have Bugzilla Bugs";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_HAS_RELATIONSHIPS = "topicHasRelationships";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_HAS_RELATIONSHIPS_DESC = "Has Relationships";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_HAS_NOT_RELATIONSHIPS = "topicHasNotRelationships";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_HAS_NOT_RELATIONSHIPS_DESC = "Doesn't have Relationships";
 	/**
 	 * The URL variable that defines the has incoming relationships search field
 	 */
 	public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS = "topicHasIncomingRelationships";
 	/** The description of the has incoming relationships search field */
 	public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS_DESC = "Has Incoming Relationships";
 	/**
 	 * The URL variable that defines the has incoming relationships search field
 	 */
 	public static final String TOPIC_HAS_NOT_INCOMING_RELATIONSHIPS = "topicHasNotIncomingRelationships";
 	/** The description of the has incoming relationships search field */
 	public static final String TOPIC_HAS_NOT_INCOMING_RELATIONSHIPS_DESC = "Doesn't Have Incoming Relationships";
 	/** The URL variable that defines the has related to search field */
 	public static final String TOPIC_RELATED_TO = "topicRelatedTo";
 	/** The description of the has related to search field */
 	public static final String TOPIC_RELATED_TO_DESC = "Related To";
 	/** The URL variable that defines the has related from search field */
 	public static final String TOPIC_RELATED_FROM = "topicRelatedFrom";
 	/** The description of the has related from search field */
 	public static final String TOPIC_RELATED_FROM_DESC = "Related From";
 	/** The URL variable that defines the has related to search field */
 	public static final String TOPIC_NOT_RELATED_TO = "topicNotRelatedTo";
 	/** The description of the has related to search field */
 	public static final String TOPIC_NOT_RELATED_TO_DESC = "Not Related To";
 	/** The URL variable that defines the has related from search field */
 	public static final String TOPIC_NOT_RELATED_FROM = "topicNotRelatedFrom";
 	/** The description of the has related from search field */
 	public static final String TOPIC_NOT_RELATED_FROM_DESC = "NotRelated From";
 	/** The URL variable that defines the has related from search field */
 	public static final String TOPIC_HAS_XML_ERRORS = "topicHasXMLErrors";
 	/** The description of the has related from search field */
 	public static final String TOPIC_HAS_XML_ERRORS_DESC = "Topic Has XML Errors";
 	/** The URL variable that defines the has related from search field */
 	public static final String TOPIC_HAS_NOT_XML_ERRORS = "topicHasNotXMLErrors";
 	/** The description of the has related from search field */
 	public static final String TOPIC_HAS_NOT_XML_ERRORS_DESC = "Topic doesn't have XML Errors";
 	/** The URL variable that defines the has related from search field */
 	public static final String TOPIC_EDITED_IN_LAST_DAYS = "topicEditedInLastDays";
 	/** The description of the has related from search field */
 	public static final String TOPIC_EDITED_IN_LAST_DAYS_DESC = "Topic Edited In Last Days";
 	/** The URL variable that defines the has related from search field */
 	public static final String TOPIC_NOT_EDITED_IN_LAST_DAYS = "topicNotEditedInLastDays";
 	/** The description of the has related from search field */
 	public static final String TOPIC_NOT_EDITED_IN_LAST_DAYS_DESC = "Topic Not Edited In Last Days";
 	/** The URL variable that defines the topic property tag */
 	public static final String TOPIC_PROPERTY_TAG = "propertyTag";
 	/** The description of the property tag search field */
 	public static final String TOPIC_PROPERTY_TAG_DESC = "Property Tag";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_IS_INCLUDED_IN_SPEC = "topicIncludedInSpec";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_IS_INCLUDED_IN_SPEC_DESC = "Topics Included In Spec";
 	/** The URL variable that defines the has relationships search field */
 	public static final String TOPIC_IS_NOT_INCLUDED_IN_SPEC = "topicNotIncludedInSpec";
 	/** The description of the has relationships search field */
 	public static final String TOPIC_IS_NOT_INCLUDED_IN_SPEC_DESC = "Topics Not Included In Spec";
 	
 	/** The URL variable that defines if translated topics should be filter to only include the latest copy */
 	public static final String LATEST_TRANSLATIONS_FILTER_VAR = "latestTranslations";
 	/** The description of the latest translated topic search field */
 	public static final String LATEST_TRANSLATIONS_FILTER_VAR_DESC = "Latest Translations";
 	/** The URL variable that defines if translated topics should be filter to only include the latest finished copy */
 	public static final String LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR = "latestCompletedTranslations";
 	/** The description of the latest completed translated topic search field */
 	public static final String LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR_DESC = "Latest Completed Translations";
 	/** The URL variable that defines if translated topics should be filter to only include the latest copy */
 	public static final String NOT_LATEST_TRANSLATIONS_FILTER_VAR = "notLatestTranslations";
 	/** The description of the latest translated topic search field */
 	public static final String NOT_LATEST_TRANSLATIONS_FILTER_VAR_DESC = "Not Latest Translations";
 	/** The URL variable that defines if translated topics should be filter to only include the latest finished copy */
 	public static final String NOT_LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR = "notLatestCompletedTranslations";
 	/** The description of the latest completed translated topic search field */
 	public static final String NOT_LATEST_COMPLETED_TRANSLATIONS_FILTER_VAR_DESC = "Not Latest Completed Translations";
 	
 	/** The URL variable that defines the topic IDs search field */
 	public static final String ZANATA_IDS_FILTER_VAR = "zanataIds";
 	/** The description of the topic IDs search field */
 	public static final String ZANATA_IDS_FILTER_VAR_DESC = "Zanata IDs";
 	/** The URL variable that defines the not topic IDs search field */
 	public static final String ZANATA_IDS_NOT_FILTER_VAR = "zanataNotIds";
 	/** The description of the not topic IDs search field */
 	public static final String ZANATA_IDS_NOT_FILTER_VAR_DESC = "Not Zanata IDs";
 	
 	/** The URL variable that defines the topic text search field */
 	public static final String TOPIC_TEXT_SEARCH_FILTER_VAR = "topicTextSearch";
 	/** The description of the topic text search field */
 	public static final String TOPIC_TEXT_SEARCH_FILTER_VAR_DESC = "Topic Text Search";
 	/** The URL variable that defines the topic IDs search field */
 	public static final String TOPIC_IDS_FILTER_VAR = "topicIds";
 	/** The description of the topic IDs search field */
 	public static final String TOPIC_IDS_FILTER_VAR_DESC = "Topic IDs";
 	/** The URL variable that defines the not topic IDs search field */
 	public static final String TOPIC_IDS_NOT_FILTER_VAR = "topicNotIds";
 	/** The description of the not topic IDs search field */
 	public static final String TOPIC_IDS_NOT_FILTER_VAR_DESC = "Not Topic IDs";
 	/** The URL variable that defines the topic title search field */
 	public static final String TOPIC_TITLE_FILTER_VAR = "topicTitle";
 	/** The description of the topic title search field */
 	public static final String TOPIC_TITLE_FILTER_VAR_DESC = "Title";
 	/** The URL variable that defines the not topic title search field */
 	public static final String TOPIC_TITLE_NOT_FILTER_VAR = "topicNotTitle";
 	/** The description of the not topic title search field */
 	public static final String TOPIC_TITLE_NOT_FILTER_VAR_DESC = "Not Title";
 	/** The URL variable that defines the topic description search field */
 	public static final String TOPIC_DESCRIPTION_FILTER_VAR = "topicText";
 	/** The description of the topic description search field */
 	public static final String TOPIC_DESCRIPTION_FILTER_VAR_DESC = "Description";
 	/** The URL variable that defines the not topic description search field */
 	public static final String TOPIC_DESCRIPTION_NOT_FILTER_VAR = "topicNotText";
 	/** The description of the not topic description search field */
 	public static final String TOPIC_DESCRIPTION_NOT_FILTER_VAR_DESC = "Not Description";
 	/** The URL variable that defines the topic xml search field */
 	public static final String TOPIC_XML_FILTER_VAR = "topicXml";
 	/** The description of the topic xml search field */
 	public static final String TOPIC_XML_FILTER_VAR_DESC = "XML";
 	/** The URL variable that defines the not topic xml search field */
 	public static final String TOPIC_XML_NOT_FILTER_VAR = "topicNotXml";
 	/** The description of the not topic xml search field */
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
 	/** The description of the end range for the topic create date search field */
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
 	 * The system property that defines the STOMP message queue that skynet should send topic rendering requests to
 	 */
 	public static final String STOMP_MESSAGE_SERVER_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY = "topicIndex.stompMessageServerRenderTopicQueue";
 	/** 
 	 * The system property that defines the STOMP message queue that skynet should send topic rendering requests to
 	 */
 	public static final String STOMP_MESSAGE_SERVER_TRANSLATED_TOPIC_RENDER_QUEUE_SYSTEM_PROPERTY = "topicIndex.stompMessageServerRenderTranslatedTopicQueue";
 	/**
 	 * The system property that determines if topics should be rendered into
 	 * HTML
 	 */
 	public static final String ENABLE_RENDERING_PROPERTY = "topicIndex.rerenderTopic";
 }
