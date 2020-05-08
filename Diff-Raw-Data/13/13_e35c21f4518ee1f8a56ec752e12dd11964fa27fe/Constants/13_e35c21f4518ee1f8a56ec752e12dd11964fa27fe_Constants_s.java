 package org.jboss.pressgang.ccms.contentspec.client.constants;
 
 import java.io.File;
 
 public class Constants {
     public static final String FILE_ENCODING = "UTF-8";
     public static final String PROGRAM_NAME = "csprocessor";
 
     // Default client settings
     public static final String HOME_LOCATION = System.getProperty("user.home");
     public static final String CONFIG_FILENAME = "csprocessor.ini";
     public static final String DEFAULT_CONFIG_LOCATION = HOME_LOCATION + File.separator + ".config" + File.separator + CONFIG_FILENAME;
     public static final String DEFAULT_SERVER_NAME = "default";
     public static final String PRODUCTION_SERVER_NAME = "production";
     public static final String TEST_SERVER_NAME = "test";
     public static final String DEFAULT_CONFIG_ZIP_LOCATION = "assembly" + File.separator;
     public static final String DEFAULT_CONFIG_PUBLICAN_LOCATION = "assembly" + File.separator + "publican" + File.separator;
     public static final String DEFAULT_CONFIG_JDOCBOOK_LOCATION = "assembly" + File.separator + "jDocbook" + File.separator;
     public static final String DEFAULT_PUBLICAN_OPTIONS = "--langs=en-US --formats=html-single";
     public static final String DEFAULT_PUBLICAN_FORMAT = "html-single";
     public static final String DEFAULT_JDOCBOOK_OPTIONS = "clean compile";
     public static final String DEFAULT_JDOCBOOK_FORMAT = "html_single";
     public static final String DEFAULT_SNAPSHOT_LOCATION = "snapshots";
     public static final String FILENAME_EXTENSION = "contentspec";
     public static final String DEFAULT_CONFIG_PUBLICAN_BUILD_POSTFIX = "-publican";
     public static final String DEFAULT_CONFIG_JDOCBOOK_BUILD_POSTFIX = "-jDocbook";
 
     // Version Constants
     public static final String VERSION_PROPERTIES_FILENAME = "version.properties";
     public static final String VERSION_PROPERTY_NAME = "cspclient.version";
     public static final String BUILDDATE_PROPERTY_NAME = "cspclient.builddate";
 
     // Options that need configuring for a build
     public static final String DEFAULT_PROD_SERVER = "";
     public static final String DEFAULT_TEST_SERVER = "";
     public static final String DEFAULT_KOJIHUB_URL = "";
     public static final String DEFAULT_PUBLISH_COMMAND = "";
     public static final String DEFAULT_ZANATA_SERVER = "";
     public static final String DEFAULT_ZANATA_SERVER_NAME = "";
     public static final String DEFAULT_ZANATA_PROJECT = "";
     public static final String DEFAULT_ZANATA_VERSION = "";
    public static final String KOJI_NAME = "brew";
     public static final String KOJI_HUB_NAME = KOJI_NAME + "hub";
 
     // Server based settings
     public static final Integer MAX_LIST_RESULT = 50;
 
     // Command Name Constants
     public static final String ADD_REVISION_COMMAND_NAME = "add-revision";
     public static final String ASSEMBLE_COMMAND_NAME = "assemble";
     public static final String BUILD_COMMAND_NAME = "build";
     public static final String CHECKOUT_COMMAND_NAME = "checkout";
     public static final String CREATE_COMMAND_NAME = "create";
     public static final String CHECKSUM_COMMAND_NAME = "checksum";
     public static final String CLONE_COMMAND_NAME = "clone";
     public static final String INFO_COMMAND_NAME = "info";
     public static final String INSTALL_COMMAND_NAME = "install";
     public static final String LIST_COMMAND_NAME = "list";
     public static final String PREVIEW_COMMAND_NAME = "preview";
     public static final String PUBLISH_COMMAND_NAME = "publish";
     public static final String PULL_COMMAND_NAME = "pull";
     public static final String PULL_SNAPSHOT_COMMAND_NAME = "pull-snapshot";
     public static final String PUSH_COMMAND_NAME = "push";
     public static final String PUSH_TRANSLATION_COMMAND_NAME = "push-translation";
     public static final String REVISIONS_COMMAND_NAME = "revisions";
     public static final String SEARCH_COMMAND_NAME = "search";
     public static final String SETUP_COMMAND_NAME = "setup";
     public static final String SNAPSHOT_COMMAND_NAME = "snapshot";
     public static final String STATUS_COMMAND_NAME = "status";
     public static final String SYNC_TRANSLATION_COMMAND_NAME = "sync-translation";
     public static final String TEMPLATE_COMMAND_NAME = "template";
     public static final String VALIDATE_COMMAND_NAME = "validate";
 
     // Parameter names
     public static final String CONTENT_SPEC_LONG_PARAM = "--content-spec";
     public static final String CONTENT_SPEC_SHORT_PARAM = "-c";
 
     public static final String SNAPSHOT_LONG_PARAM = "--snapshot";
     public static final String SNAPSHOT_SHORT_PARAM = "--s";
 
     public static final String TOPIC_LONG_PARAM = "--topic";
     public static final String TOPIC_SHORT_PARAM = "-t";
 
     public static final String USERNAME_LONG_PARAM = "--username";
     public static final String USERANME_SHORT_PARAM = "-u";
 
     public static final String SERVER_LONG_PARAM = "--host";
     public static final String SERVER_SHORT_PARAM = "-H";
 
     public static final String OUTPUT_LONG_PARAM = "--output";
     public static final String OUTPUT_SHORT_PARAM = "-o";
 
     public static final String FORCE_LONG_PARAM = "--force";
     public static final String FORCE_SHORT_PARAM = "-f";
 
     public static final String XML_LONG_PARAM = "--xml";
     public static final String XML_SHORT_PARAM = "-x";
 
     public static final String HTML_LONG_PARAM = "--html";
     public static final String HTML_SHORT_PARAM = "-h";
 
     public static final String REVISION_LONG_PARAM = "--revision";
     public static final String REVISION_SHORT_PARAM = "-r";
 
     public static final String MAX_TOPIC_REVISION_LONG_PARAM = "--max-topic-revision";
 
     public static final String OVERRIDE_LONG_PARAM = "--override";
 
     public static final String PUBLICAN_CFG_OVERRIDE_LONG_PARAM = "--publican.cfg-override";
 
     public static final String HIDE_ERRORS_LONG_PARAM = "--hide-errors";
 
     public static final String SHOW_CONTENT_SPEC_LONG_PARAM = "--show-contentspec";
 
     public static final String INLINE_INJECTION_LONG_PARAM = "--hide-injections";
 
     public static final String INJECTION_TYPES_LONG_PARAM = "--injection-types";
 
     public static final String EXEC_TIME_LONG_PARAM = "--exec-time";
 
     public static final String HELP_LONG_PARAM = "--help";
 
     public static final String CONFIG_LONG_PARAM = "--config";
 
     public static final String PRE_LONG_PARAM = "--pre";
 
     public static final String BUG_REPORTING_LONG_PARM = "--hide-bug-links";
 
     public static final String FORCE_BUG_REPORTING_LONG_PARM = "--force-bug-links";
 
     public static final String POST_LONG_PARAM = "--post";
 
     public static final String NO_BUILD_LONG_PARAM = "--no-build";
 
     public static final String NO_ASSEMBLE_LONG_PARAM = "--no-assemble";
 
     public static final String LIMIT_LONG_PARAM = "--limit";
 
     public static final String HIDE_OUTPUT_LONG_PARAM = "--hide-output";
 
     public static final String NO_CREATE_CSPROCESSOR_CFG_LONG_PARAM = "--no-csprocessor-cfg";
 
     public static final String DEBUG_LONG_PARAM = "--debug";
 
     public static final String COMMENTED_LONG_PARAM = "--commented";
 
     public static final String VERSION_LONG_PARAM = "--version";
 
     public static final String EMPTY_LEVELS_LONG_PARAM = "--empty-levels";
 
     public static final String LOCALE_LONG_PARAM = "--lang";
 
     public static final String LOCALES_LONG_PARAM = "--langs";
 
     public static final String EDITOR_LINKS_LONG_PARAM = "--editor-links";
 
     public static final String FETCH_PUBSNUM_LONG_PARAM = "--fetch-pubsnum";
 
     public static final String NO_PUBLICAN_BUILD_LONG_PARAM = "--no-publican-build";
 
     public static final String ZANATA_PROJECT_LONG_PARAM = "--zanata-project";
 
     public static final String ZANATA_PROJECT_VERSION_LONG_PARAM = "--zanata-version";
 
     public static final String ZANATA_SERVER_LONG_PARAM = "--zanata-server";
 
     public static final String PUBLISH_MESSAGE_LONG_PARAM = "--pub-message";
 
     public static final String SHOW_REPORT_LONG_PARAM = "--show-report";
 
     public static final String COMMON_CONTENT_LONG_PARAM = "--common-content";
 
     public static final String TARGET_LANG_LONG_PARAM = "--target-lang";
 
     public static final String UPDATE_LONG_PARAM = "--latest";
 
     public static final String PUSH_ONLY_LONG_PARAM = "--push-only";
 
     public static final String DRAFT_LONG_PARAM = "--draft";
     public static final String DRAFT_SHORT_PARAM = "-d";
 
     public static final String SHOW_REMARKS_LONG_PARAM = "--show-remarks";
 
     public static final String REV_MESSAGE_LONG_PARAM = "--rev-message";
 
     public static final String TOPICS_ONLY_LONG_PARAM = "--topics-only";
 
     public static final String CONTENT_SPEC_ONLY_LONG_PARAM = "--contentspec-only";
 
     public static final String PATH_LONG_PARAM = "--path";
 
     public static final String NEW_LONG_PARAM = "--new";
 
     public static final String FLATTEN_TOPICS_LONG_PARAM = "--flatten-topics";
     public static final String FLATTEN_LONG_PARAM = "--flatten";
 
     public static final String YES_SHORT_PARAM = "-y";
     public static final String YES_LONG_PARAM = "--yes";
 
     public static final String NO_SHORT_PARAM = "-n";
     public static final String NO_LONG_PARAM = "--no";
 
     public static final String MESSAGE_SHORT_PARAM = "-m";
     public static final String MESSAGE_LONG_PARAM = "--message";
     public static final String REVISION_MESSAGE_FLAG_LONG_PARAMETER = "--rev-history";
 
     public static final String STRICT_TITLES_LONG_PARAM = "--strict-titles";
 
     public static final String FORMAT_LONG_PARAM = "--format";
 
     public static final String FIRST_NAME_LONG_PARAM = "--firstname";
 
     public static final String SURNAME_LONG_PARAM = "--surname";
 
     public static final String EMAIL_LONG_PARAM = "--email";
 
     public static final String REVNUMBER_LONG_PARAM = "--revnumber";
 
     public static final String DATE_LONG_PARAM = "--date";
 
     // Exit statuses
     public static final int EXIT_SUCCESS = 0;
     public static final int EXIT_FAILURE = -1;
     public static final int EXIT_NO_SERVER = 1;
     public static final int EXIT_UNAUTHORISED = 2;
     public static final int EXIT_INTERNAL_SERVER_ERROR = 3;
     public static final int EXIT_CONFIG_ERROR = 4;
     public static final int EXIT_ARGUMENT_ERROR = 5;
     public static final int EXIT_FILE_NOT_FOUND = 6;
     public static final int EXIT_TOPIC_VALID = 7;
     public static final int EXIT_TOPIC_INVALID = 8;
     public static final int EXIT_OUT_OF_DATE = 9;
     public static final int EXIT_SHUTDOWN_REQUEST = 10;
     public static final int EXIT_UPGRADE_REQUIRED = 11;
 }
