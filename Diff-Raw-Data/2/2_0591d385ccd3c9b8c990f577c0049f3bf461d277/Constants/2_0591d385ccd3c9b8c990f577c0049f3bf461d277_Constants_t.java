 package com.redhat.contentspec.client.constants;
 
 import java.io.File;
 
 public class Constants {
 	
 	public static final String PROGRAM_NAME = "csprocessor";
 	
 	// Default client settings
 	public static final String HOME_LOCATION = System.getProperty("user.home");
 	public static final String CONFIG_FILENAME = "csprocessor.ini";
 	public static final String DEFAULT_CONFIG_LOCATION = HOME_LOCATION + "/.config/" + CONFIG_FILENAME;
 	public static final String DEFAULT_SERVER_NAME = "default";
 	public static final String PRODUCTION_SERVER_NAME = "production";
 	public static final String TEST_SERVER_NAME = "test";
 	public static final String DEFAULT_CONFIG_ZIP_LOCATION = "assembly" + File.separator;
 	public static final String DEFAULT_CONFIG_PUBLICAN_LOCATION = "assembly" + File.separator + "publican" + File.separator;
 	public static final String DEFAULT_PUBLICAN_OPTIONS = "--langs=en-US --format=html-single";
 	public static final String DEFAULT_PUBLICAN_FORMAT= "html-single";
 	public static final String DEFAULT_SNAPSHOT_LOCATION = "snapshots";
 	public static final String FILENAME_EXTENSION = "contentspec";
	public static final String BUILD = "0.25.0";
 	
 	// Server based settings
 	public static final Integer MAX_LIST_RESULT = 50;
 	
 	// General Messages
 	public static final String BUILD_MSG			= "CSProcessor client version: %s";
 	public static final String WEBSERVICE_MSG		= "Connecting to Skynet server: %s";
 	public static final String CONFIG_LOADING_MSG 	= "Loading configuration from %s";
 	public static final String CSP_CONFIG_LOADING_MSG 	= "Loading project configuration from csprocessor.cfg";
 	public static final String CONFIG_CREATING_MSG	= "Creating the default configuration file: %s";
 	public static final String ZIP_SAVED_MSG		= "INFO: Content Specification successfully built!";
 	
 	// Command Name Constants
 	public static final String ASSEMBLE_COMMAND_NAME = "assemble";
 	public static final String BUILD_COMMAND_NAME = "build";
 	public static final String CHECKOUT_COMMAND_NAME = "checkout";
 	public static final String CREATE_COMMAND_NAME = "create";
 	public static final String CHECKSUM_COMMAND_NAME = "checksum";
 	public static final String INFO_COMMAND_NAME = "info";
 	public static final String LIST_COMMAND_NAME = "list";
 	public static final String PREVIEW_COMMAND_NAME = "preview";
 	public static final String PULL_COMMAND_NAME = "pull";
 	public static final String PULL_SNAPSHOT_COMMAND_NAME = "pull-snapshot";
 	public static final String PUSH_COMMAND_NAME = "push";
 	public static final String REVISIONS_COMMAND_NAME = "revisions";
 	public static final String SEARCH_COMMAND_NAME = "search";
 	public static final String SETUP_COMMAND_NAME = "setup";
 	public static final String STATUS_COMMAND_NAME = "status";
 	public static final String TEMPLATE_COMMAND_NAME = "template";
 	public static final String VALIDATE_COMMAND_NAME = "validate";
 	
 	// Error Messages
 	public static final String INVALID_ARG_MSG				= "Invalid argument!";
 	public static final String INI_NOT_FOUND_MSG			= "The configuration file does not exist in the specified location!";
 	public static final String PROCESSING_CONFIG_ERROR_MSG 	= "An error occurred while reading the configuration file please try again.";
 	public static final String NO_WRITE_INI_MSG				= "Cannot write csprocessor.ini to " + DEFAULT_CONFIG_LOCATION + "! Please check the file permissions!";
 	public static final String NO_DEFAULT_SERVER_FOUND		= "No default server was found in the %s configuration file. Perhaps you need to uncomment a default?";
 	public static final String NO_SERVER_FOUND_FOR_DEFAULT_SERVER = "No server was found for the specified default.";
 	public static final String NO_SERVER_USERNAME_MSG		= "No Username was specified for the \"%s\" server in the configuration files.";
 	public static final String NO_SERVER_URL_MSG			= "No Server URL was specified for the \"%s\" server in the configuration files.";
 	public static final String UNABLE_TO_FIND_SERVER_MSG	= "Cannot connect to the server, as the server address can't be resolved.";
 	public static final String FILE_EXISTS_OVERWRITE_MSG 	= "%s already exists! Overwrite existing file (y/n)? ";
 	public static final String ERROR_FAILED_SAVING			= "An error occured while trying to save the file.";
 	public static final String ERROR_FAILED_SAVING_FILE		= "An error occured while trying to save %s.";
 	public static final String LIST_ERROR_MSG				= "There are %s Content Specs on this server. You should probably use \"csprocessor search\" if you have an idea what you are looking for. Otherwise, rerun the list command, and this time use the --limit <NUMBER>";
 	public static final String NO_SNAPSHOT_NAME_MSG			= "No snapshot name was specified. A name must be specified for a snapshot.";
 	public static final String LIST_SNAPSHOT_ERROR_MSG		= "There are %s Snapshots on this server. You should probably use \"csprocessor search -s\" if you have an idea what you are looking for. Otherwise, rerun the list command, and this time use the --limit<NUMBER>";
 	public static final String NO_FILE_FOUND_FOR_CONFIG		= "The file \"%s\" was not found in the current directory.";
 	
 	public static final String ERROR_UNAUTHORISED			= "Unauthorised Request! Please check your username and the server URL is correct.";
 	public static final String ERROR_INTERNAL_ERROR			= "Internal processing error!";
 	public static final String ERROR_NO_REV_ID_FOUND_MSG	= "No data was found for the specified ID and revision!";
 	public static final String ERROR_NO_ID_FOUND_MSG		= "No data was found for the specified ID!";
 	public static final String ERROR_NO_FILE_MSG			= "No file was found for the specified file name!";
 	public static final String ERROR_EMPTY_FILE_MSG			= "The specified file was empty!";
 	public static final String ERROR_UNABLE_TO_FIND_ZIP_MSG	= "Unable to assemble the Content Specification because the \"%s\" file couldn't be found.";
 	public static final String ERROR_FAILED_TO_ASSEMBLE_MSG = "The content specification failed to be assembled.";
 	public static final String ERROR_RUNNING_PUBLICAN_MSG	= "Unable to assemble the Content Specification because an error occured while running Publican.";
 	public static final String ERROR_UNABLE_TO_FIND_HTML_SINGLE_MSG	= "Unable to preview the Content Specification because the \"%s\" file couldn't be found.";
 	public static final String ERROR_UNABLE_TO_OPEN_FILE_MSG		= "Unable to open the \"%s\" file.";
 	public static final String ERROR_UNSUPPORTED_FORMAT				= "\"%s\" is not currently supported as a preview format.";
 	public static final String ERROR_INVALID_CSPROCESSOR_CFG_MSG	= "The csprocessor.cfg file doesn't have an ID specified.";
 	public static final String ERROR_CONTENT_SPEC_EXISTS_MSG		= "A directory already exists for the Content Specification. Please check the \"%s\" directory first and if it's correct, then use the --force option.";
 	public static final String ERROR_NO_SERVER_FOUND_MSG			= "No credentials are setup for the \"%s\" server specified in the csprocessor.cfg file. Please setup the server in your csprocessor.ini configuration file.";
 	public static final String ERROR_NO_ID_MSG						= "No ID was specified by the command line or a csprocessor.cfg file.";
 	public static final String ERROR_MULTIPLE_ID_MSG				= "Multiple ID's specified. Please only specify one ID.";
 	public static final String ERROR_FAILED_CREATING_CONFIG_MSG		= "Failed to create the default configuration file.";
 	public static final String ERROR_OUT_OF_DATE_MSG				= "The local copy of the Content Specification is out of date. Please use \"csprocessor pull\" to download the latest copy.";
 	public static final String ERROR_LOCAL_COPY_UPDATED_MSG			= "The local copy of the Content Specification has been updated and is out of sync with the server. Please use \"csprocessor push\" to update the server copy.";
 	public static final String ERROR_LOCAL_COPY_AND_SERVER_UPDATED_MSG		= "The local copy and server copy of the Content Specification has been updated. Please use \"csprocessor pull\" to update your local copy. Your unsaved local changes will be saved as %s.backup.";
 	public static final String ERROR_NO_FILE_OUT_OF_DATE_MSG		= "The \"%s\" file couldn't be found. This could mean the title has changed on the server or the ID is wrong.";
 	public static final String ERROR_NO_USERNAME					= "No username was specified for the server. Please check your configuration files and make sure a username exists.";
 	public static final String ERROR_PULL_SNAPSHOT_INVALID 			= "The revision of the Content Specification is invalid and as such the snapshot couldn't be pulled.";
 	
 	// Info Messages
 	public static final String ZIP_SAVED_ERRORS_MSG			= "Content Specification successfully built with %s Errors and %s Warnings";
 	public static final String EXEC_TIME_MSG				= "Request processed in %dms";
 	public static final String OUTPUT_SAVED_MSG 			= "Output saved to: %s";
 	public static final String SUCCESSFUL_PUSH_MSG			= "Content Specification ID: %d\nRevision: %d";
 	public static final String CSP_CONFIG_SAVED_MSG 		= "csprocessor.cfg saved to: %s";
 	public static final String NO_CS_FOUND_MSG				= "INFO:  No Content Specifications were found on the Server.";
 	public static final String SUCCESSFUL_ASSEMBLE_MSG		= "Content Specification successfully assembled at %s";
 	public static final String SUCCESSFUL_UNZIP_MSG			= "Content Specification build unzipped to %s";
 	public static final String UP_TO_DATE_MSG				= "The local copy of the Content Specification is up to date.";
 	public static final String SETUP_CONFIG_MSG				= "Edit your configuration file to configure your username(s) and default server.";
 	public static final String SUCCESSFUL_SETUP_MSG			= "Configuration settings successfully setup.";
 	public static final String CSP_ID_MSG					= "Content Specification ID: %d";
 	public static final String CSP_REVISION_MSG				= "Content Specification Revision: %d";
 	public static final String CSP_TITLE_MSG				= "Content Specification Title: %s";
 	public static final String CSP_COMPLETION_MSG			= "Total Number of Topics: %d\nNumber of Topics with XML: %d\nPercentage Complete: %.2f%%";
 	
 	// Start Messages
 	public static final String STARTING_ASSEMBLE_MSG		= "Starting to assemble...";
 	public static final String STARTING_BUILD_MSG			= "Starting to build...";
 	public static final String STARTING_PUBLICAN_BUILD_MSG	= "Starting the publican build...";
 	public static final String STARTING_VALIDATE_MSG		= "Starting to validate...";
 	
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
 	
 	public static final String PERMISSIVE_LONG_PARAM = "--permissive";
 	public static final String PERMISSIVE_SHORT_PARAM = "-p";
 	
 	public static final String FORCE_LONG_PARAM = "--force";
 	public static final String FORCE_SHORT_PARAM = "--f";
 	
 	public static final String XML_LONG_PARAM = "--xml";
 	public static final String XML_SHORT_PARAM = "-x";
 	
 	public static final String HTML_LONG_PARAM = "--html";
 	public static final String HTML_SHORT_PARAM = "-h";
 	
 	public static final String REVISION_LONG_PARAM = "--revision";
 	public static final String REVISION_SHORT_PARAM = "-r";
 	
 	public static final String OVERRIDE_LONG_PARAM = "--override";
 	
 	public static final String HIDE_ERRORS_LONG_PARAM = "--hide-errors";
 	
 	public static final String SHOW_CONTENT_SPEC_LONG_PARAM = "--show-contentspec";
 	
 	public static final String INLINE_INJECTION_LONG_PARAM = "--hide-injections";
 	
 	public static final String INJECTION_TYPES_LONG_PARAM = "--injection-types";
 	
 	public static final String EXEC_TIME_LONG_PARAM = "--exec-time";
 	
 	public static final String HELP_LONG_PARAM = "--help";
 	
 	public static final String CONFIG_LONG_PARAM = "--config";
 	
 	public static final String PRE_LONG_PARAM = "--pre";
 	
 	public static final String BUG_REPORTING_LONG_PARM = "--hide-bug-links";
 	
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
 }
