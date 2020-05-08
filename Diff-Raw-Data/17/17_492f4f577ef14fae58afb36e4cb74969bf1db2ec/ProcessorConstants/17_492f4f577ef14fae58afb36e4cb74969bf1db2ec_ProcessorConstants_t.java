 package org.jboss.pressgang.ccms.contentspec.processor.constants;
 
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 
 public class ProcessorConstants {
     /**
      * The minimum value that designates if two strings match.
      */
     public static final double MIN_MATCH_SIMILARITY = 0.8;
 
     public static final String RELEASE_CATEGORY_NAME = "Release";
     public static final int ASSIGNED_WRITER_CATEGORY_ID = 12;
     public static final String ASSIGNED_WRITER_CATEGORY_NAME = "Assigned Writer";
 
     public static final String TOPIC_ID_CONTENTS = "TopicID";
     public static final String BRACKET_CONTENTS = "Brackets";
     public static final String BRACKET_NAMED_PATTERN = "(?<!\\\\)\\%c(?<" + BRACKET_CONTENTS + ">(.|\n)*?)(?<!\\\\)\\%c";
     public static final String BRACKET_PATTERN = "(?<!\\\\)\\%c((.|\n)*?)(?<!\\\\)\\%c";
     public static final String BRACKET_VALIDATE_REGEX = ".*%s[ ]*$"; // ".*%s(([ ]*$)|([ ]*#.*$))" For use to allow comments at the end
     // of a line
     public static final String PRODUCT_VERSION_VALIDATE_REGEX = "(([0-9]+)|([0-9]+.[0-9]+)|([0-9]+.[0-9]+.[0-9]+))([\\.\\-]?[A-Za-z]+)?";
     public static final String VERSION_VALIDATE_REGEX = "([0-9]+)|([0-9]+.[0-9]+)|([0-9]+.[0-9]+.[0-9]+)";
     public static final String VERSION_EPOCH_VALIDATE_REGEX = "(" + VERSION_VALIDATE_REGEX + ")(-[0-9]+)?";
     public static final String COPYRIGHT_YEAR_VALIDATE_REGEX = "^\\d+[\\s\\-\\d,]*$";
 
     public static final String RELATED_REGEX = "^(R|RELATED-TO|REFER-TO)[ ]*:(.|(\r?\n))*$";
     public static final String PREREQUISITE_REGEX = "^(P|PREREQUISITE)[ ]*:(.|(\r?\n))*$";
     public static final String LINK_LIST_REGEX = "^(L|LINK-LIST)[ ]*:(.|(\r?\n))*$";
     public static final String NEXT_REGEX = "^NEXT[ ]*:(.|(\r?\n))*$";
     public static final String PREV_REGEX = "^PREV[ ]*:(.|(\r?\n))*$";
     public static final String TARGET_BASE_REGEX = "T(([0-9]+)|(\\-[ ]*[A-Za-z0-9\\-_]+))";
     public static final String TARGET_REGEX = "^" + TARGET_BASE_REGEX + "$";
     public static final String EXTERNAL_TARGET_REGEX = "^E" + TARGET_BASE_REGEX + "$";
     public static final String EXTERNAL_CSP_REGEX = "^CS[0-9]+[ ]*(:[ ]*[0-9]+)?$";
 
     public static final String CSP_TITLE_REGEX = "^[0-9a-zA-Z_\\-\\.\\+\\s]+$";
     public static final String CSP_PRODUCT_REGEX = "^[0-9a-zA-Z_\\-\\.\\+\\s]+$";
 
     public static final String RELATION_ID_REGEX = "^(" + TARGET_BASE_REGEX + ")|(N?[0-9]+)$";
     public static final String RELATION_ID_LONG_REGEX = "^.*\\[((" + TARGET_BASE_REGEX + ")|(N?[0-9]+))\\]$";
     public static final String RELATION_ID_LONG_PATTERN = "^(?<TopicTitle>.*)[ ]*\\[(?<TopicID>(" + TARGET_BASE_REGEX + ")|(N?[0-9]+))\\]$";
 
     public static final String FILE_ID_REGEX = "^[0-9]+$";
     public static final String FILE_ID_LONG_REGEX = "^.*\\[[0-9]+([ ]*,[ ]*rev:[ ]+[0-9]+)?\\]$";
     public static final String FILE_ID_LONG_PATTERN = "^(?<Title>.*)[ ]*\\[(?<ID>[0-9]+)([ ]*,[ ]*rev:[ ]+(?<REV>[0-9]+))?\\]$";
 
     public static final String VALID_BOOK_TYPE_REGEX = "^(BOOK|ARTICLE)(-DRAFT)?$";
 
     public static final String LINE = "Line %d: ";
     public static final String INVALID_CS = "Invalid Content Specification!";
     public static final String INVALID_TOPIC = "Invalid Topic!";
     public static final String INVALID_RELATIONSHIP = "Invalid Relationship!";
     public static final String AMBIGUOUS_RELATIONSHIP = "Ambiguous Relationship!";
     public static final String INVALID_FILE = "Invalid Additional File!";
     public static final String INVALID_PROCESS = "Invalid Process!";
     public static final String GENERIC_INVALID_LEVEL = "Invalid Chapter/Section/Appendix!";
     public static final String NEW_LINE_SPACER = "\n       -> ";
     public static final String CSLINE_MSG = NEW_LINE_SPACER + "%s";
 
     public static final String CREATED_BY = "CSProcessor";
 
     public static final String SUCCESSFUL_PUSH_ID_MSG = "Content Specification ID: %d";
     public static final String SUCCESSFUL_PUSH_REV_MSG = "Revision: %d";
     public static final String SUCCESSFUL_PUSH_MSG = SUCCESSFUL_PUSH_ID_MSG + "\n" + SUCCESSFUL_PUSH_REV_MSG;
     public static final String ERROR_DATABASE_ERROR_MSG = "An error occurred when inserting into the database please try again.";
     public static final String ERROR_PROCESSING_ERROR_MSG = "An error occurred during processing please try again and if another failure " +
             "occurs please file a bug.";
     public static final String WARN_EDIT_INFO_MSG = "Note: All descriptions, tags, source urls and writers will be ignored for existing "
             + "Topics.";
 
     // Content Spec Errors
     public static final String ERROR_CS_INVALID_ID_MSG = INVALID_CS + " The specified ID doesn't exist in the database.";
     public static final String ERROR_CS_NO_TITLE_MSG = INVALID_CS + " No Title.";
     public static final String ERROR_CS_INVALID_TITLE_MSG = INVALID_CS + " Invalid Title. The title can only contain plus (+), " +
             "hyphen (-), dot(.) or alpha numeric characters.";
     public static final String ERROR_CS_NO_PRODUCT_MSG = INVALID_CS + " No Product specified.";
     public static final String ERROR_CS_NO_VERSION_MSG = INVALID_CS + " No Version specified.";
     public static final String ERROR_CS_NO_DTD_MSG = INVALID_CS + " No DTD specified.";
     public static final String ERROR_CS_INVALID_DTD_MSG = INVALID_CS + " DTD specified is unsupported. Docbook 4.5 is the only currently " +
             "supported DTD.";
     public static final String ERROR_CS_NO_CHECKSUM_MSG = INVALID_CS + " \"CHECKSUM\" or \"ID\" attribute not found in the Content " +
             "Specification.";
     public static final String ERROR_CS_NONMATCH_SPEC_REVISION_MSG = INVALID_CS + " Revisions must match to be edited." + NEW_LINE_SPACER
             + "SpecRevision: %d" + NEW_LINE_SPACER + "Server Revision: %d\n";
     public static final String ERROR_CS_INVALID_SPEC_REVISION_MSG = LINE + INVALID_CS + " SpecRevision attribute is present, " +
             "and is not allowed when pushing a new Content Specification.";
     public static final String ERROR_CS_NONMATCH_CHECKSUM_MSG = INVALID_CS + " Checksums must match to be edited." + NEW_LINE_SPACER +
             "Local Checksum: %s" + NEW_LINE_SPACER + "Server Checksum: %s\n";
     public static final String ERROR_CS_INVALID_CHECKSUM_MSG = LINE + INVALID_CS + " Checksum attribute is present, " +
             "and is not allowed when pushing a new Content Specification.";
     public static final String ERROR_INVALID_PUBLICAN_CFG_MSG = LINE + INVALID_CS + " Incorrect publican.cfg input." + CSLINE_MSG;
     public static final String ERROR_INVALID_TAG_ATTRIB_FORMAT_MSG = LINE + INVALID_CS + " Incorrect tag attribute format." + CSLINE_MSG;
     public static final String ERROR_INVALID_ATTRIB_FORMAT_MSG = LINE + INVALID_CS + " Incorrect attribute format." + CSLINE_MSG;
     public static final String ERROR_INVALID_METADATA_FORMAT_MSG = LINE + INVALID_CS + " Incorrect metadata format." + CSLINE_MSG;
     public static final String ERROR_MISSING_ATTRIB_FORMAT_MSG = LINE + INVALID_CS + " Missing attribute detected." + CSLINE_MSG;
     public static final String ERROR_TAG_DUPLICATED_MSG = LINE + INVALID_CS + " Tag is duplicated." + CSLINE_MSG;
     public static final String ERROR_TAG_NONEXIST_MSG = LINE + INVALID_CS + " Tag \"%s\" doesn't exist." + CSLINE_MSG;
     public static final String ERROR_MULTI_TAG_DUPLICATED_MSG = LINE + INVALID_CS + " One or more tags are duplicated." + CSLINE_MSG;
     public static final String ERROR_CS_EMPTY_MSG = LINE + INVALID_CS + " The content specification can't be empty.";
     public static final String ERROR_CS_SECTION_NO_CHAPTER_MSG = LINE + INVALID_CS + " A Section can't be outside of a Chapter, Preface, " +
             "Article or Appendix." + CSLINE_MSG;
     public static final String ERROR_CS_NESTED_CHAPTER_MSG = LINE + INVALID_CS + " A Chapter must be within a \"Part\" or have no " +
             "indentation." + CSLINE_MSG;
     public static final String ERROR_CS_NESTED_PREFACE_MSG = LINE + INVALID_CS + " A Preface must be within a \"Part\" or have no " +
             "indentation." + CSLINE_MSG;
     public static final String ERROR_CS_NESTED_APPENDIX_MSG = LINE + INVALID_CS + " An Appendix must be within a \"Part\" or have no " +
             "indentation." + CSLINE_MSG;
     public static final String ERROR_CS_NESTED_PART_MSG = LINE + INVALID_CS + " A Part must have no indentation." + CSLINE_MSG;
     public static final String ERROR_CS_NESTED_ARTICLE_MSG = LINE + INVALID_CS + " An Article must have no indentation." + CSLINE_MSG;
     public static final String ERROR_CS_NO_COPYRIGHT_MSG = INVALID_CS + " A Copyright Holder must be specified.";
     public static final String ERROR_INVALID_CS_COPYRIGHT_YEAR_MSG = INVALID_CS + " The Copyright Year is invalid. It must be in the " +
             "format \"<YEAR>, <YEAR>\" or \"<YEAR>-<YEAR>\".";
     public static final String ERROR_INVALID_INJECTION_MSG = LINE + INVALID_CS + " The setting for inline injection must be On or Off." +
             CSLINE_MSG;
     public static final String ERROR_INVALID_INJECTION_TYPE_MSG = INVALID_CS + " The injection type \"%s\" doesn't exist or isn't a Type.";
     public static final String ERROR_DUPLICATE_ID_MSG = LINE + INVALID_CS + " Duplicate topic ID ( %s )." + CSLINE_MSG;
     public static final String ERROR_INVALID_BUG_LINKS_MSG = LINE + INVALID_CS + " The setting for bug links must be On or Off." +
             CSLINE_MSG;
     public static final String ERROR_INVALID_BUGZILLA_ASSIGNEE_MSG = LINE + INVALID_CS + " The setting for Bugzilla assignee must be On " +
             "or Off." + CSLINE_MSG;
     public static final String ERROR_CS_READ_ONLY_MSG = INVALID_CS + " The content specification is read-only.";
     public static final String ERROR_INVALID_SURVEY_LINKS_MSG = LINE + INVALID_CS + " The setting for survey links must be On or Off." +
             CSLINE_MSG;
     public static final String ERROR_INVALID_BOOK_TYPE_MSG = INVALID_CS + " The specified book type is not valid. Please choose either " +
             "\"Book\" or \"Article\".";
     public static final String ERROR_INVALID_ARTICLE_STRUCTURE = INVALID_CS + " Articles must contain an \"Article\" chapter and cannot " +
             "contain chapters or parts.";
     public static final String ERROR_CS_APPENDIX_STRUCTURE_MSG = LINE + INVALID_CS + " An Appendix must be at the end of the content " +
             "specification." + CSLINE_MSG;
     public static final String ERROR_INVALID_OPTION_MSG = LINE + INVALID_CS + " Unknown metadata tag found. \"condition\", " +
             "\"Description\", \"URL\" and \"Writer\" are currently the only supported metadata." + CSLINE_MSG;
     public static final String ERROR_INVALID_CONDITION_MSG = LINE + INVALID_CS + " The condition statement must be a valid regular " +
             "expression string." + CSLINE_MSG;
     public static final String ERROR_TOPIC_WITH_DIFFERENT_REVS_MSG = INVALID_CS + " Topic %d has two or more different revisions included" +
             " in the Content Specification. The topic is located at:";
     public static final String ERROR_TOPIC_WITH_DIFFERENT_REVS_REV_MSG = "Revision %s, lines(s) %s.";
     public static final String ERROR_INVALID_VERSION_NUMBER_MSG = "Invalid %s specified. The value must be a valid version.";
     public static final String ERROR_INVALID_FILES_MSG = LINE + INVALID_CS + " Incorrect additional files input." + CSLINE_MSG;
     public static final String ERROR_INVALID_FILE = LINE + INVALID_FILE + " Invalid format.";
 
     // Article based level errors
     public static final String ERROR_ARTICLE_CHAPTER_MSG = LINE + INVALID_CS + " Chapters can't be used in Articles." + CSLINE_MSG;
     public static final String ERROR_ARTICLE_PART_MSG = LINE + INVALID_CS + " Parts can't be used in Articles." + CSLINE_MSG;
     public static final String ERROR_ARTICLE_PREFACE_MSG = LINE + INVALID_CS + " Prefaces can't be used in Articles." + CSLINE_MSG;
     public static final String ERROR_ARTICLE_PROCESS_MSG = LINE + INVALID_CS + " Processes can't be used in Articles." + CSLINE_MSG;
     public static final String ERROR_ARTICLE_NESTED_APPENDIX_MSG = LINE + INVALID_CS + " An Appendix must have no indentation." +
             CSLINE_MSG;
     public static final String ERROR_ARTICLE_SECTION_MSG = LINE + INVALID_CS + " A Section must be within another section." + CSLINE_MSG;
 
     public static final String ERROR_INCORRECT_TOPIC_ID_LOCATION_MSG = LINE + INVALID_CS + " Topic ID specified in the wrong location." +
             CSLINE_MSG;
 
     public static final String ERROR_NO_BRACKET_MSG = LINE + INVALID_CS + " Missing brackets %c%c detected.";
     public static final String ERROR_NO_ENDING_BRACKET_MSG = LINE + INVALID_CS + " Missing ending bracket (%c) detected.";
     public static final String ERROR_NO_OPENING_BRACKET_MSG = LINE + INVALID_CS + " Missing opening bracket (%c) detected.";
     public static final String ERROR_MISSING_SEPARATOR_MSG = LINE + INVALID_CS + " Missing separator(%c) detected.";
     public static final String ERROR_INCORRECT_INDENTATION_MSG = LINE + INVALID_CS + " Indentation is invalid." + CSLINE_MSG;
     public static final String ERROR_RELATIONSHIP_BASE_LEVEL_MSG = LINE + INVALID_CS + " Relationships can't be at the base level." +
             CSLINE_MSG;
 
     public static final String ERROR_INCORRECT_EDIT_MODE_MSG = "Invalid Operation! The Content Specification appears to be new " +
             "and you cannot update a new Content Specification. Please make sure the " + CSConstants.CHECKSUM_TITLE + " and " + CSConstants
             .ID_TITLE + " exists.";
     public static final String ERROR_INCORRECT_NEW_MODE_MSG = "Invalid Operation! The Content Specification appears to be an existing " +
             "Content Specification and you cannot create a new Content Specification, from an exiting Content Specification.";
     public static final String ERROR_NONEXIST_CS_TYPE_MSG = "No processing type specified! Please specify whether to process as a " +
             "Content Specification or a Setup Processor.";
     public static final String ERROR_NONEXIST_CS_MODE_MSG = "No processing mode specified! Please specify whether to process as a New or " +
             "Edited Content Specification.";
     public static final String ERROR_INVALID_CS_ID_MSG = "The Content Specification ID doesn't exist in the database." + CSLINE_MSG;
     public static final String ERROR_INVALID_CS_ID_FORMAT_MSG = "The Content Specification ID is not valid." + CSLINE_MSG;
    public static final String ERROR_INCORRECT_FILE_FORMAT_MSG = INVALID_CS + " Incorrect file format. A Content Specification must start" +
            " with either a Title or a CHECKSUM and ID.";
     public static final String ERROR_DUPLICATED_RELATIONSHIP_TYPE_MSG = LINE + "Duplicated bracket types found." + CSLINE_MSG;
 
     public static final String INFO_VALID_CS_MSG = "The Content Specification is valid.";
     public static final String INFO_SUCCESSFUL_SAVE_MSG = "The Content Specification saved successfully.";
     public static final String ERROR_INVALID_CS_MSG = "The Content Specification is not valid.";
 
     public static final String ERROR_LEVEL_NO_TITLE_MSG = LINE + "Invalid %s! No title." + CSLINE_MSG;
     public static final String ERROR_LEVEL_NO_TOPICS_MSG = LINE + "Invalid %s! No topics or levels in this %s." + CSLINE_MSG;
     public static final String ERROR_LEVEL_NO_CHILD_LEVELS_MSG = LINE + "Invalid %s! No levels in this %s." + CSLINE_MSG;
     public static final String ERROR_LEVEL_FORMAT_MSG = LINE + GENERIC_INVALID_LEVEL + " Incorrect format." + CSLINE_MSG;
     public static final String ERROR_LEVEL_RELATIONSHIP_MSG = LINE + "Invalid %s! Relationships can't be used for a %s." + CSLINE_MSG;
     public static final String ERROR_LEVEL_TOPIC_TITLES_NONMATCH_MSG = LINE + "Invalid %s! The topic title doesn't match." +
             CSLINE_MSG + CSLINE_MSG;
 
     public static final String ERROR_INVALID_NUMBER_MSG = LINE + "Number expected but the value specified is not a valid number." +
             CSLINE_MSG;
     public static final String WARN_EMPTY_BRACKETS_MSG = LINE + "Empty brackets found.";
 
     // Topic errors
     public static final String ERROR_INVALID_TOPIC_FORMAT_MSG = LINE + INVALID_TOPIC + " Incorrect topic format." + CSLINE_MSG;
     public static final String ERROR_TYPE_NONEXIST_MSG = LINE + INVALID_TOPIC + " Type doesn't exist." + CSLINE_MSG;
     public static final String ERROR_INVALID_TITLE_ID_MSG = LINE + INVALID_TOPIC + " Title and ID must be specified." + CSLINE_MSG;
     public static final String ERROR_INVALID_TYPE_TITLE_ID_MSG = LINE + INVALID_TOPIC + " Title, " +
             "Type and ID must be specified." + CSLINE_MSG;
     public static final String ERROR_WRITER_NONEXIST_MSG = LINE + INVALID_TOPIC + " Writer name doesn't exist." + CSLINE_MSG;
     public static final String ERROR_NO_WRITER_MSG = LINE + INVALID_TOPIC + " No writer specified." + CSLINE_MSG;
     public static final String ERROR_INVALID_WRITER_MSG = LINE + INVALID_TOPIC + " The writer specified is not an Assigned Writer." +
             CSLINE_MSG;
     public static final String ERROR_INVALID_TOPIC_ID_MSG = LINE + INVALID_TOPIC + " Incorrect ID format." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NO_TITLE_MSG = LINE + INVALID_TOPIC + " No Title." + CSLINE_MSG;
     public static final String ERROR_INVALID_TOPIC_TITLE_MSG = LINE + INVALID_TOPIC + " The Topic title is not valid." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NO_TYPE_MSG = LINE + INVALID_TOPIC + " No Type." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NO_TECH_AND_RELEASE_MSG = LINE + INVALID_TOPIC + " A Technology and Release tag has not been " +
             "set." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NO_TECH_OR_RELEASE_MSG = LINE + INVALID_TOPIC + " A Technology or Release tag has not been set" +
             "." + CSLINE_MSG;
     public static final String ERROR_TOPIC_TOO_MANY_CATS_MSG = LINE + INVALID_TOPIC + " Too many tags for the category: %s." + CSLINE_MSG;
     public static final String ERROR_TOPIC_ID_NONEXIST_MSG = LINE + INVALID_TOPIC + " ID doesn't exist in the database." + CSLINE_MSG;
     public static final String ERROR_TOPIC_TITLES_NONMATCH_MSG = LINE + INVALID_TOPIC + " Existing topic title doesn't match." +
             CSLINE_MSG + CSLINE_MSG;
     public static final String ERROR_TOPIC_TYPE_NONMATCH_MSG = LINE + INVALID_TOPIC + " Existing topic type doesn't match." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NONEXIST_MSG = LINE + INVALID_TOPIC + " Existing topic specified doesn't exist." + CSLINE_MSG;
     public static final String ERROR_TOPIC_OUTSIDE_CHAPTER_MSG = LINE + INVALID_TOPIC + " A topic must be inside of another element, " +
             "it can't be at the base level." + CSLINE_MSG;
     public static final String ERROR_TOPIC_DUPLICATE_CLONES_MSG = LINE + INVALID_TOPIC + " A Duplicate clone topic can only be used when " +
             "the clone is exclusively used inside a Content Specification." + CSLINE_MSG;
     public static final String ERROR_TOPIC_WRITER_AS_TAG_MSG = LINE + INVALID_TOPIC + " A writer cannot be specified as a tag." +
             CSLINE_MSG;
     public static final String ERROR_TOPIC_TYPE_AS_TAG_MSG = LINE + INVALID_TOPIC + " A topic type cannot be specified as a tag." +
             CSLINE_MSG;
     public static final String ERROR_TOPIC_NEXT_PREV_MSG = LINE + INVALID_TOPIC + " Next and Previous relationships can't be used " +
             "directly. If you wish to use next/previous then please use a Process." + CSLINE_MSG;
     public static final String ERROR_TOPIC_BRANCH_OUTSIDE_PROCESS = LINE + INVALID_TOPIC + " A Branch cannot exist outside of a Process."
             + CSLINE_MSG;
     public static final String ERROR_TOPIC_EXISTING_TOPIC_CANNOT_REMOVE_TAGS = LINE + INVALID_TOPIC + " An Existing Topic cannot have " +
             "tags removed." + CSLINE_MSG;
     public static final String ERROR_TOPIC_EXISTING_TOPIC_CANNOT_ADD_SOURCE_URLS = LINE + INVALID_TOPIC + " An Existing Topic cannot have" +
             " have source urls added." + CSLINE_MSG;
     public static final String ERROR_TOPIC_EXISTING_BAD_OPTIONS = LINE + INVALID_TOPIC + " An Existing topic cannot have a new Writer, " +
             "Description or Source URLs." + CSLINE_MSG;
     public static final String ERROR_TOPIC_INLINE_TOPIC_MUST_BE_FIRST = LINE + INVALID_TOPIC + " An inline topic must be the first topic " +
             "in a Chapter/Section/Appendix." + CSLINE_MSG;
     public static final String ERROR_TOPIC_INLINE_RELATIONSHIPS = LINE + INVALID_TOPIC + " An inline topic can't have any relationships."
             + CSLINE_MSG;
     public static final String ERROR_TOPIC_TAG_DUPLICATED_MSG = LINE + INVALID_TOPIC + " Tag exists twice inside of the database." +
             CSLINE_MSG;
     public static final String ERROR_TOPIC_NO_NEW_TOPIC_BUILD = LINE + INVALID_TOPIC + " New topics aren't allowed to be created during a" +
             " build." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NO_NEW_TRANSLATION_TOPIC = LINE + INVALID_TOPIC + " New topics aren't allowed to be created " +
             "for translated topics." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NO_TAGS_TRANSLATION_TOPIC = LINE + INVALID_TOPIC + " Tags aren't allowed to be added to " +
             "translated topics." + CSLINE_MSG;
     public static final String ERROR_TOPIC_INVALID_REVISION_FORMAT = LINE + INVALID_TOPIC + " Revision attribute must be a valid number." +
             CSLINE_MSG;
     public static final String ERROR_TOPIC_NOT_IN_PART_INTRO_MSG = LINE + INVALID_TOPIC + " A topic must be before any chapters inside of" +
             " a part." + CSLINE_MSG;
     public static final String ERROR_TOPIC_HAS_RELATIONSHIPS_MSG = LINE + INVALID_TOPIC + " The Topic has relationships, " +
             "but isn't allowed any." + CSLINE_MSG;
     public static final String ERROR_INVALID_TYPE_MSG = LINE + INVALID_TOPIC + " Invalid Type." + CSLINE_MSG;
     public static final String ERROR_TOPIC_NOT_ALLOWED_MSG = LINE + INVALID_TOPIC + " The Topic is not allowed in the structure of a book" +
             " because it is not a normal Topic." + CSLINE_MSG;
     public static final String ERROR_LEGAL_NOTICE_TOPIC_TYPE_INCORRECT = LINE + INVALID_TOPIC + " The Topic specified is not a Legal " +
             "Notice Topic." + CSLINE_MSG;
     public static final String ERROR_REV_HISTORY_TOPIC_TYPE_INCORRECT = LINE + INVALID_TOPIC + " The Topic specified is not a Revision " +
             "History Topic." + CSLINE_MSG;
 
     // Warnings
     public static final String WARN_DESCRIPTION_IGNORE_MSG = LINE + "%s topics can't have a description, " +
             "so the description will be ignored.";
     public static final String WARN_TYPE_IGNORE_MSG = LINE + "%s topics can't have a type, so the type will be ignored.";
     public static final String WARN_WRITER_IGNORE_MSG = LINE + "%s topics can't be assigned a new writer, so the writer will be ignored.";
     public static final String WARN_TAGS_IGNORE_MSG = LINE + "%s topics can't have tags, so the tags will be ignored.";
     public static final String WARN_DEBUG_IGNORE_MSG = "Invalid debug setting. Debug must be set to 0, " +
             "" + "1 or 2! So debug will be off by default.";
     public static final String WARN_IGNORE_INFO_MSG = LINE + "All descriptions, tags, source urls and writers will be ignored for " +
             "existing Topics." + CSLINE_MSG;
     public static final String WARN_IGNORE_DUP_INFO_MSG = LINE + "All types, descriptions, source urls and writers will be ignored for " +
             "existing Topics." + CSLINE_MSG;
     public static final String WARN_INTERNAL_TOPIC_MSG = LINE + "The topic is an internal-only topic and contains sensitive information. " +
             "Ensure you are not publishing this publicly." + CSLINE_MSG;
     public static final String WARN_CS_NO_SUBTITLE_MSG = "No Subtitle specified, so a default will be used instead.";
     public static final String WARN_CS_NO_ABSTRACT_MSG = "No Abstract specified, so a default will be used instead.";
     public static final String ERROR_NEW_TOPIC_DISABLED_MESSAGE = LINE + "Creating new topics via Content Specification is not supported " +
             "on this server" + CSLINE_MSG;
     public static final String WARN_LEVEL_TOPIC_TITLES_NONMATCH_MSG = LINE + "Possible Invalid %s! The topic title doesn't match." +
             CSLINE_MSG + CSLINE_MSG;
 
     // Process Errors
     public static final String ERROR_PROCESS_NONEXIST_MSG = LINE + INVALID_PROCESS + " Topic %s doesn't exist in the database." +
             CSLINE_MSG;
     public static final String ERROR_PROCESS_INVALID_TOPIC_MSG = LINE + INVALID_PROCESS + " Only existing topics can be used in a process" +
             "." + CSLINE_MSG;
     public static final String ERROR_PROCESS_INVALID_TYPES_MSG = LINE + INVALID_PROCESS + " Processes can't have tags, " +
             "relationships or targets." + CSLINE_MSG;
     public static final String ERROR_PROCESS_OUTSIDE_CHAPTER_MSG = LINE + INVALID_PROCESS + " A process must be inside of a chapter or be" +
             " a chapter." + CSLINE_MSG;
     public static final String ERROR_PROCESS_NO_TOPICS_MSG = LINE + INVALID_PROCESS + " No Topics found. A process must contain at least " +
             "one topic." + CSLINE_MSG;
     public static final String ERROR_PROCESS_DUPLICATE_TOPICS_MSG = LINE + INVALID_PROCESS + " Topic %s is duplicated. A Process must not" +
             " have duplicate topics." + CSLINE_MSG;
     public static final String ERROR_PROCESS_HAS_LEVELS_MSG = LINE + INVALID_PROCESS + " A process cannot contain " +
             "Chapters/Sections/Appendixes or other Processes." + CSLINE_MSG;
 
     //Relationship Errors
     public static final String ERROR_DUPLICATE_TARGET_ID_MSG = "Target ID is duplicated. Target ID's must be unique." + NEW_LINE_SPACER +
             LINE + " %s" + NEW_LINE_SPACER + LINE + " %s";
     public static final String ERROR_TARGET_NONEXIST_MSG = LINE + INVALID_RELATIONSHIP + " The Target specified (%s) doesn't exist in the" +
             " content specification." + CSLINE_MSG;
     public static final String ERROR_RELATED_TOPIC_NONEXIST_MSG = LINE + INVALID_RELATIONSHIP + " The related topic specified (%s) " +
             "doesn't exist in the content specification." + CSLINE_MSG;
     public static final String ERROR_INVALID_RELATIONSHIP_MSG = LINE + AMBIGUOUS_RELATIONSHIP + " Topic %s is included on lines %s of the" +
             " Content Specification. To relate to one of these topics please use a Target." + CSLINE_MSG;
     public static final String ERROR_TOO_MANY_NEXTS_MSG = LINE + INVALID_RELATIONSHIP + " A topic may only have one next Topic." +
             CSLINE_MSG;
     public static final String ERROR_TOO_MANY_PREVS_MSG = LINE + INVALID_RELATIONSHIP + " A topic may only have one previous Topic." +
             CSLINE_MSG;
     public static final String ERROR_NEXT_RELATED_LEVEL_MSG = LINE + INVALID_RELATIONSHIP + " Next relationships must target a topic." +
             CSLINE_MSG;
     public static final String ERROR_PREV_RELATED_LEVEL_MSG = LINE + INVALID_RELATIONSHIP + " Previous relationships must target a topic" +
             "." + CSLINE_MSG;
     public static final String ERROR_INVALID_DUPLICATE_RELATIONSHIP_MSG = LINE + AMBIGUOUS_RELATIONSHIP + " The link target is ambiguous," +
             " please use an explicit link target ID. Add [T<uniqueID>] to the instance you want to relate to, " +
             "and use that as the link target." + CSLINE_MSG;
     public static final String ERROR_TOPIC_RELATED_TO_ITSELF_MSG = LINE + INVALID_RELATIONSHIP + " You can't relate a topic to itself." +
             CSLINE_MSG;
     public static final String ERROR_RELATED_TITLE_NO_MATCH_MSG = LINE + INVALID_RELATIONSHIP + " The topic/target relationship title " +
             "specified doesn't match the actual topic/target title." + NEW_LINE_SPACER + "Specified: %s" + NEW_LINE_SPACER + "Actual:    " +
             "%s";
     public static final String ERROR_INVALID_REFERS_TO_RELATIONSHIP = LINE + INVALID_RELATIONSHIP + " Invalid Refers-To Relationship " +
             "format";
     public static final String ERROR_INVALID_PREREQUISITE_RELATIONSHIP = LINE + INVALID_RELATIONSHIP + " Invalid Prerequisite " +
             "Relationship format";
     public static final String ERROR_INVALID_LINK_LIST_RELATIONSHIP = LINE + INVALID_RELATIONSHIP + " Invalid Link-List Relationship " +
             "format";
 
     // Level Regex Constants
     public static final String CHAPTER_REGEX = "^CHAPTER[ ]*((:.*)|$)";
     public static final String SECTION_REGEX = "^SECTION[ ]*((:.*)|$)";
     public static final String APPENDIX_REGEX = "^APPENDIX[ ]*((:.*)|$)";
     public static final String PART_REGEX = "^PART[ ]*((:.*)|$)";
     public static final String PREFACE_REGEX = "^PREFACE[ ]*((:.*)|$)";
     public static final String PROCESS_REGEX = "^PROCESS[ ]*((:.*)|$)";
     public static final String LEVEL_REGEX = "^(CHAPTER|SECTION|APPENDIX|PART|PREFACE|PROCESS)[ ]*((:.*)|$)";
 }
