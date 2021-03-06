 /*
  * Copyright (c) 2013 OBiBa. All rights reserved.
  *
  * This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.obiba.opal.web.gwt.app.client.i18n;
 
 import java.util.Map;
 
 import com.google.gwt.i18n.client.Constants;
 import com.google.gwt.i18n.client.LocalizableResource.Generate;
 import com.google.gwt.i18n.client.LocalizableResource.GenerateKeys;
 
 /**
  * Programmatically available localised text strings. This interface will be bound to localised properties files found
  * in the {@code com.google.gwt.i18n.client} package.
  */
 @SuppressWarnings("OverlyComplexClass")
 @GenerateKeys
 @Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat", locales = { "default" })
 public interface Translations extends Constants {
 
   @Description("Error dialog title")
   @DefaultStringValue("Errors")
   String errorDialogTitle();
 
   @Description("Error dialog title when used to display warnings")
   @DefaultStringValue("Warnings")
   String warningDialogTitle();
 
   @Description("Error dialog title when used to display information")
   @DefaultStringValue("Information")
   String infoDialogTitle();
 
   @Description("Report template create dialog title")
   @DefaultStringValue("Add Report Template")
   String addReportTemplateDialogTitle();
 
   @Description("Report template edit dialog title")
   @DefaultStringValue("Edit Report Template")
   String editReportTemplateDialogTitle();
 
   @Description("Name label")
   @DefaultStringValue("Name")
   String nameLabel();
 
   @Description("Tags label")
   @DefaultStringValue("Tags")
   String tagsLabel();
 
   @Description("Namespace label")
   @DefaultStringValue("Namespace")
   String namespaceLabel();
 
   @Description("Value Type label")
   @DefaultStringValue("Value Type")
   String valueTypeLabel();
 
   @Description("Label label")
   @DefaultStringValue("Label")
   String labelLabel();
 
   @Description("Original Label label")
   @DefaultStringValue("Original Label")
   String originalLabelLabel();
 
   @Description("ID label")
   @DefaultStringValue("ID")
   String idLabel();
 
   @Description("Type label")
   @DefaultStringValue("Type")
   String typeLabel();
 
   @Description("User label")
   @DefaultStringValue("User")
   String userLabel();
 
   @Description("Project label")
   @DefaultStringValue("Project")
   String projectLabel();
 
   @Description("Start label")
   @DefaultStringValue("Start")
   String startLabel();
 
   @Description("End label")
   @DefaultStringValue("End")
   String endLabel();
 
   @Description("Status label")
   @DefaultStringValue("Status")
   String statusLabel();
 
   @Description("Status map")
   @DefaultStringMapValue({ "NOT_STARTED", "Not Started", //
       "IN_PROGRESS", "In Progress", //
       "SUCCEEDED", "Succeeded", //
       "FAILED", "Failed", //
       "CANCEL_PENDING", "Cancel Pending", //
       "CANCELED", "Cancelled" //
   })
   Map<String, String> statusMap();
 
   @Description("Actions label")
   @DefaultStringValue("Actions")
   String actionsLabel();
 
   @Description("Action map")
   @DefaultStringMapValue({ "Log", "Log", //
       "Cancel", "Cancel", //
       "Delete", "Delete", //
       "Edit", "Edit", //
       "Copy", "Copy", //
       "Test", "Test", //
       "Download", "Download", //
       "DownloadCertificate", "Download Certificate", //
       "Index now", "Index now", //
       "Clear", "Clear",//
       "View", "View",//
       "Remove", "Remove",//
       "Publish methods", "Publish methods",
       "Permissions", "Permissions",
       "CommitDiff", "Diff",
       "DiffWithCurrent", "Diff with current"
   })
   Map<String, String> actionMap();
 
   @Description("Permission map")
   @DefaultStringMapValue({ //
       "DATASOURCE_ALL", "Administrate", //
       "CREATE_TABLE", "Add Table", //
       "CREATE_VIEW", "Add View", //
       "TABLE_ALL", "Administrate", //
       "TABLE_READ", "View variables and summaries", //
       "TABLE_VALUES", "View values", //
       "TABLE_EDIT", "Edit variables", //
       "VIEW_ALL", "Administrate", //
       "VIEW_READ", "View variables and summaries", //
       "VIEW_VALUES", "View values", //
       "VIEW_EDIT", "Edit with summaries", //
       "VIEW_VALUES_EDIT", "Edit with values", //
       "VARIABLE_READ", "View with summary", //
       "DATABASES_ALL", "Administrate", //
       "R_SESSION_ALL", "Use", //
       "DATASHIELD_ALL", "Administrate", //
       "DATASHIELD_SESSION_ALL", "Use", //
       "REPORT_TEMPLATE_ALL", "Administrate", //
       "REPORT_TEMPLATE_READ", "View reports" })
   Map<String, String> permissionMap();
 
   @Description("Permission explanation map")
   @DefaultStringMapValue({ //
       "datasource", "Specify the access rights to the datasource and its content.", //
       "table",
       "Specify the access rights to the table and its content. Induces the visibility of the parent datasource.", //
       "view", "Specify the access rights to the view and its content. Induces the visibility of the parent datasource.",
       //
       "variable",
       "Specify the access rights to the variable. Induces the visibility of the parent table and datasource.", //
       "databases", "Specify the access rights to the databases configuration.",//
       "datashield", "Specify the access rights to the DataShield services.",//
       "r", "Specify the access rights to the R services."//
   })
   Map<String, String> permissionExplanationMap();
 
   @Description("Table Comparison Result map")
   @DefaultStringMapValue({ "CREATION", "Table to be created", //
       "MODIFICATION", "Table to be modified", //
       "CONFLICT", "Conflicting table modifications", //
       "SAME", "No table modifications", //
       "FORBIDDEN", "Table modifications not permitted" //
   })
   Map<String, String> comparisonResultMap();
 
   @Description("Size label")
   @DefaultStringValue("Size")
   String sizeLabel();
 
   @Description("Last modified label")
   @DefaultStringValue("Last Modified")
   String lastModifiedLabel();
 
   @Description("Date label")
   @DefaultStringValue("Date")
   String dateLabel();
 
   @Description("Message label")
   @DefaultStringValue("Message")
   String messageLabel();
 
   @Description("Job label")
   @DefaultStringValue("Job")
   String jobLabel();
 
   @Description("Jobs menu item")
   @DefaultStringValue("Jobs")
   String jobsLabel();
 
   @Description("File system label")
   @DefaultStringValue("File System")
   String fileSystemLabel();
 
   @Description("Entity type label")
   @DefaultStringValue("Entity Type")
   String entityTypeLabel();
 
   @Description("Referenced Entity type label")
   @DefaultStringValue("Referenced Entity Type")
   String referencedEntityTypeLabel();
 
   @Description("Entity type column label")
   @DefaultStringValue("Entity Type column")
   String entityTypeColumnLabel();
 
   @Description("Tables label")
   @DefaultStringValue("Tables")
   String tablesLabel();
 
   @Description("Table label")
   @DefaultStringValue("Table")
   String tableLabel();
 
   @Description("Entities label")
   @DefaultStringValue("Entities")
   String entitiesLabel();
 
   @Description("Variables label")
   @DefaultStringValue("Variables")
   String variablesLabel();
 
   @Description("Variable label")
   @DefaultStringValue("Variable")
   String variableLabel();
 
   @Description("Unit label")
   @DefaultStringValue("Unit")
   String unitLabel();
 
   @Description("Attribute name required label")
   @DefaultStringValue("An attribute name is required.")
   String attributeNameRequired();
 
   @Description("Attribute name already exists label")
   @DefaultStringValue("The specified attribute name already exists.")
   String attributeNameAlreadyExists();
 
   @Description("Attribute value required label")
   @DefaultStringValue(
       "Provide a value for the attribute (either localised, or not localised).")
   String attributeValueRequired();
 
   @Description("Category name already exists label")
   @DefaultStringValue("The specified category name already exists.")
   String categoryNameAlreadyExists();
 
   @Description("Provide a name for this category label")
   @DefaultStringValue("Provide a name for this category.")
   String categoryNameRequired();
 
   @Description("Category label required label")
   @DefaultStringValue("Provide a label for the this category.")
   String categoryLabelRequired();
 
   @Description("Category dialog name required label")
   @DefaultStringValue("A category name is required.")
   String categoryDialogNameRequired();
 
   @Description("Category name duplicated")
   @DefaultStringValue("Duplicated category name {0}.")
   String categoryNameDuplicated();
 
   @Description("User message map")
   @DefaultStringMapValue({ //
       "VariableNameNotUnique", "The specified variable name already exists.", //
       "jobCancelled", "Job cancelled.", //
       "jobDeleted", "Job deleted.", //
       "completedJobsDeleted", "All completed jobs deleted.", //
       "SetCommandStatus_NotFound", "Job could not be cancelled (not found).", //
       "SetCommandStatus_BadRequest_IllegalStatus", "Job status cannot be set to the specified value.", //
       "SetCommandStatus_BadRequest_NotCancellable", "Job has completed and has already been cancelled.", //
       "DeleteCommand_NotFound", "Job could not be deleted (not found).", //
       "DeleteCommand_BadRequest_NotDeletable", "Job is currently running and therefore cannot be deleted at this time.",
       //
       "cannotCreateFolderPathAlreadyExist",
       "Could not create the folder, a folder or a file exist with that name at the specified path.", //
       "cannotCreateFolderParentIsReadOnly", "Could create the following folder because its parent folder is read-only.",
       //
       "cannotCreatefolderUnexpectedError", "There was an unexpected error while creating the folder.", //
       "cannotDeleteNotEmptyFolder", "This folder contains one or many file(s) and as a result cannot be deleted.", //
       "cannotDeleteReadOnlyFile", "Could delete the  file or folder because it is read-only.", //
       "couldNotDeleteFileError", "There was an error while deleting the file or folder.", //
       "datasourceMustBeSelected", "You must select a datasource.", //
       "fileReadError", "The file could not be read.", //
       "ViewNameRequired", "You must provide a name for the view.", //
       "ViewAlreadyExists", "A view with the same name already exists.", //
       "TableSelectionRequired", "You must select at least one table.", //
       "TableEntityTypesDoNotMatch", "The selected tables must all have the same entity type.", //
       "VariableDefinitionMethodRequired", "You must indicate how the view's variables are to be defined.", //
       "DatasourceNameRequired", "You must provide a name for the datasource.", //
       "DatasourceAlreadyExistsWithThisName", "A datasource already exists with this name.", //
       "ExcelFileRequired", "An Excel file is required.", "ExcelFileSuffixInvalid", //
       "Invalid Excel file suffix: .xls or .xlsx are expected.", //
       "ViewMustBeAttachedToExistingOrNewDatasource",
       "The view must be attached to either an existing datasource or a new one.", //
       "DuplicateDatasourceName", "The datasource name is already in use. Please choose another.", //
       "UnknownError", "An unknown error has occurred.", //
       "InternalError", "An internal error has occurred. Please contact technical support.", //
       "DatasourceNameDisallowedChars", "Datasource names cannot contain colon or period characters.", //
       "ViewNameDisallowedChars", "View names cannot contain colon or period characters.", //
       "CSVFileRequired", "A CSV file is required.", //
       "XMLFileRequired", "An XML file is required.", //
       "XMLFileSuffixInvalid", "Invalid XML file suffix: .xml is expected.", //
       "ZipFileRequired", "A Zip file is required.", //
       "SpssFileRequired", "An SPSS file is required.", //
       "ZipFileSuffixInvalid", "Invalid Zip file suffix: .zip is expected.",//
       "InvalidFileType", "Invalid file type. Supported file types are: xls, xlsx, and sav.",//
       "ReportTemplateWasNotFound", "The specified report template could not be found.",//
       "ReportJobStarted", "Report task has been launched.  You can follow its progress in the task list.",//
       "ReportTemplateAlreadyExistForTheSpecifiedName", "A report template already exist with the specified name.",//
       "BirtReportDesignFileIsRequired", "A BIRT Design File must be selected.",//
       "CronExpressionIsRequired", "A schedule expression must be specified.",//
       "NotificationEmailsAreInvalid", "One or more of the notifications emails specified are invalid.",//
       "ReportTemplateNameIsRequired", "A name is required for the report template.",//
       "OccurrenceGroupIsRequired", "An Occurence Group must be specified for Repeatable variables.",//
       "NewVariableNameIsRequired", "A name is required for the new variable to be created.",//
       "ScriptIsRequired", "A script is required.",//
       "JavascriptError", "Error in script '{0}': {1} ({2}:{3})",//
       "CopyFromVariableNameIsRequired",
       "You must enter the name of a variable from which the new variable will be created from.",//
       "cannotSwitchTabBecauseOfUnsavedChanges",
       "You have unsaved changes. You need to press Save Changes before you can select another tab.",//
       "UrlRequired", "You must provide the database's URL.",//
       "UsernameRequired", "You must indicate the user name to be used for the database connection.",//
       "PasswordRequired", "You must indicate the password to be used for the database connection.", //
       "MustIndicateWhetherJdbcDatasourceShouldUseMetadataTables",
       "You must indicate whether meta-data tables are to be used or not.",//
       "RowMustBePositiveInteger", "Row must be a positive integer (greater than or equal to 1).",//
       "SpecificCharsetNotIndicated",
       "You have selected to use a specific character set but have not indicated which one.",//
       "NoDataFileSelected", "You must select a data file.",//
       "NoDataToCopy", "No data to copy to the current destination.",//
       "NoFileSelected", "You must select a file.",//
       "CharsetNotAvailable", "The character set you have specified is not available.",//
       "FieldSeparatorRequired", "The field separator is required.",//
       "QuoteSeparatorRequired", "The quote separator is required.",//
       "NotIgnoredConflicts", "Some conflicts were detected. Ignore modifications before applying changes.",//
       "NoVariablesToBeImported", "No variables are to be imported.",//
       "DataImportFailed", "The data importation has failed: {0}",//
       "FunctionalUnitAlreadyExistWithTheSpecifiedName", "A unit with the same name already exists.",//
       "FunctionalUnitNameIsRequired", "Unit name is required.",//
       "DuplicateFunctionalUnitNames", "Duplicate Unit names.",//
       "KeyPairAliasIsRequired", "Alias is required",//
       "KeyPairAlgorithmIsRequired", "Algorithm is required.",//
       "KeyPairKeySizeIsRequired", "Size is required.",//
       "KeyPairPrivateKeyPEMIsRequired", "Private Key in PEM format is required.", //
       "KeyPairFirstAndLastNameIsRequired", "First and Last Name is required.",//
       "KeyPairOrganizationalUnitIsRequired", "Organizational Unit is required.",//
       "KeyPairOrganizationNameIsRequired", "Organization Name is required.",//
       "KeyPairCityNameIsRequired", "City or Locality Name is required.",//
       "KeyPairStateNameIsRequired", "State or Province Name is required.",//
       "KeyPairCountryCodeIsRequired", "Country Code is required.",//
       "KeyPairPublicKeyPEMIsRequired", "Public Key in PEM format is required.",//
       "DestinationFileIsMissing", "Destination File is required.", //
       "ExportDataMissingTables", "At least one table is required.",//
       "ExportDataDuplicateTableNames", "At least two tables have the name '{0}'. Export cannot be completed.",//
       "IdentifiersGenerationCompleted", "Identifiers generation completed.",//
       "NoIdentifiersGenerated", "No Identifiers generated.",//
       "IdentifiersGenerationFailed", "Identifiers generation has failed.",//
       "IdentifiersGenerationPending", "An Identifiers generation task is currently running.",//
       "MappedUnitsCannotBeIdentified", "Units to be mapped cannot be identified.",//
       "TwoMappedUnitsExpected", "Exactly two Units to be mapped are expected.",//
       "DataShieldMethodAlreadyExistWithTheSpecifiedName", "A method already exists with the specified name.",//
       "DataShieldPackageAlreadyExistWithTheSpecifiedName", "A package already exists with the specified name.",//
       "DataShieldMethodNameIsRequired", "A name is required.",//
       "DataShieldRScriptIsRequired", "A R script is required.",//
       "DataShieldRFunctionIsRequired", "A R function is required.",//
       "DataShieldPackageNameIsRequired", "A name is required",//
       "RIsAlive", "R server is alive.",//
       "RConnectionFailed", "Connection with R server failed.", //
       "UnauthorizedOperation", "You are not allowed to perform this operation.",//
       "CannotWriteToView", "Cannot modify a View using this operation. Use the View editor.",//
       "DatesRangeInvalid", "The range of dates is invalid.",//
       "CouldNotCreateReportTemplate", "Could not create the Report Template.",//
       "ReportTemplateCannotBeFound", "The Report Template cannot be found.",//
       "DatasourceCreationFailed", "The datasource creation has failed: {0}",//
       "DatasourceReadFailed", "The datasource cannot be read: {0}",//
       "DestinationTableRequired", "The destination table is required.",//
       "DestinationTableNameInvalid", "The destination table name is not valid (must not contain '.' or ':').",//
       "DestinationTableEntityTypeRequired", "The destination table entity type is required.",//
       "DestinationTableCannotBeView", "The destination table cannot be a view.",//
       "DataImportationProcessLaunched", "The data importation process can be followed using the Job ID: {0}",//
       "DataExportationProcessLaunched",
       "The data exportation process can be followed using the Job ID: {0}. Files will be exported to: {1}",//
       "DatabaseAlreadyExists", "A database with this name already exists.",//
       "DatabaseConnectionOk", "Connection successful.",//
       "DatabaseConnectionFailed", "Failed to connect: {0}.",//
      "FailedToConnectToDatabase", "Failed to connect to database '{0}'.",//
       "DatabaseIsNotEditable", "Database is used by a Datasource and is not editable",//
       "CannotFindDatabase", "Cannot find database named {0}",//
       "NameIsRequired", "A name is required.",//
       "DriverIsRequired", "A driver is required.",//
       "DefaultEntityTypeIsRequired", "Default Entity Type is required for Custom SQL schema.",//
       "DatabaseUsageIsRequired", "Database usage is required.",//
       "SQLSchemaIsRequired", "SQL schema is required.",//
       "LimeSurveyDatabaseIsRequired", "LimeSurvey database is required.",//
       "UrlIsRequired", "A url is required.",//
       "UsernameIsRequired", "A username is required.",//
       "TableSelectionIsRequired", "At least one table must be selected.",//
       "IdentifiersImportationCompleted", "Identifiers importation completed.",//
       "IdentifiersImportationFailed", "Identifiers importation failed: {0}.",//
       "IndexClearSelectAtLeastOne", "Select at least one index to clear.",//
       "IndexScheduleSelectAtLeastOne", "Select at least one index to schedule.",//
       "PasswordIsRequired", "A password is required.",//
       "UsageIsRequired", "Database usage is required.",//
       "SqlSchemaIsRequired", "Database SQL schema is required.",//
       "OpalURLIsRequired", "Opal address is required.",//
       "RemoteDatasourceIsRequired", "Remote datasource name is required.",//
       "TableSelectionIsRequired", "At least one table must be selected.",//
       "IdentifiersImportationCompleted", "Identifiers importation completed.",//
       "IdentifiersImportationFailed", "Identifiers importation failed: {0}.",//
       "DataWriteNotAuthorized", "You are not allowed to write in datasource: {0}.",//
       "AccessDeniedToTableValues", "You are not allowed to view the values of table: {0}.",//
       "NoTablesForEntityIdType", "Failed to retrieve tables for entity {0} and type {1}",//
       "NoVariablesFound", "Failed to retrieve the list of variables",//
       "NoVariableValuesFound", "Failed to retrieve the list of variable values",//
       "EntityIdentifierNotFound", "{0} identifier {1} could not be found in table {2}",//
       "ParticipantIdentifiersAlreadyGenerated", "Participant identifiers have already been generated for the Unit {0}",
       "FunctionalUnitCreationFailed", "Failed to create functional unit: {0}",//
       "RPackageInstalledButNotFound",
       "Package was probably successfully installed in R but cannot be found. Restarting R server might solve this issue.",
       "InvalidLocaleName", "Invalid locale name '{0}'. Please choose a valid locale name from the list.",//
       "CopyVariableSelectAtLeastOne", "Select at least one variable to add.",//
       "CopyVariableCurrentView", "The view cannot be the current view. Please select another destination view.",//
       "CopyVariableNameRequired", "Variable name cannot be empty.",//
       "CopyVariableNameColon", "Variable name '{0}' cannot contain ':'.",//
       "CopyVariableNameAlreadyExists", "Duplicate variable name: {0}.",//
       "CopyVariableIncompatibleEntityType", "Incompatible entity types: {0} / {1}",//
       "SearchServiceUnavailable", "Search operation failed. Please make sure the service is started.",//
       "UserStatusChangedOk", "User {0} has been successfully {1}.",//
       "UserUpdatedOk", "User {0} has been successfully updated.",//
       "UserCreatedOk", "User {0} has been successfully added.",//
       "UserPasswordLengthError", "Password must contain at least {0} characters.",//
       "UserPasswordMatchError", "Passwords do not match.",//
       "UserDeletedOk", "User {0} has been successfully deleted.", //
       "UserAlreadyExists", "User name already exists.", //
       "UserNameRequiredError", "User name is required.",//
       "UserPasswordRequiredError", "Password is required.",//
       "GroupDeletedOk", "Group {0} has been successfully deleted.", //
       "GroupAlreadyExists", "Group already exists.", //
       "ProjectNameRequired", "Project name is required.", //
       "ProjectNameMustBeUnique", "A project already exists with this name.",//
       "ProjectCreationFailed", "The datasource creation has failed: {0}", //
       "FileNotFound", "File not found: {0}",//
       "FileNotAccessible", "File not accessible: {0}",//
       "MultipleIdentifiersDatabase", "Database for identifiers already exists: {0}", //
       "DatabaseAlreadyExists", "Database named {0} already exists", //
       "VcsScriptContentInfo", "The retrieved script content is from '{0}' committed by '{1}'.", //
       "GeneralConfigSaved", "Opal general configuration was successfully saved." })
   Map<String, String> userMessageMap();
 
   @Description("You must select a file message")
   @DefaultStringValue("You must select a file.")
   String fileMustBeSelected();
 
   @Description("Yes label")
   @DefaultStringValue("Yes")
   String yesLabel();
 
   @Description("No label")
   @DefaultStringValue("No")
   String noLabel();
 
   @Description("Missing label")
   @DefaultStringValue("Missing")
   String missingLabel();
 
   @Description("Categories label")
   @DefaultStringValue("Categories")
   String categoriesLabel();
 
   @Description("Category label")
   @DefaultStringValue("Category")
   String categoryLabel();
 
   @Description("No Categories label")
   @DefaultStringValue("No Categories")
   String noCategoriesLabel();
 
   @Description("Attributes label")
   @DefaultStringValue("Attributes")
   String attributesLabel();
 
   @Description("No Attributes label")
   @DefaultStringValue("No Attributes")
   String noAttributesLabel();
 
   @Description("Language label")
   @DefaultStringValue("Language")
   String languageLabel();
 
   @Description("Value label")
   @DefaultStringValue("Value")
   String valueLabel();
 
   @Description("Original Value label")
   @DefaultStringValue("Original Value")
   String originalValueLabel();
 
   @Description("New Value label")
   @DefaultStringValue("New Value")
   String newValueLabel();
 
   @Description("Code label")
   @DefaultStringValue("Code")
   String codeLabel();
 
   @Description("Mime Type label")
   @DefaultStringValue("Mime Type")
   String mimeTypeLabel();
 
   @Description("Repeatable label")
   @DefaultStringValue("Repeatable")
   String repeatableLabel();
 
   @Description("Occurrence Group label")
   @DefaultStringValue("Occurrence Group")
   String occurrenceGroupLabel();
 
   @Description("Multiple table selection instructions")
   @DefaultStringValue("Select one or more tables:")
   String multipleTableSelectionInstructionsLabel();
 
   @Description("Single table selection instructions")
   @DefaultStringValue("Select one table:")
   String singleTableSelectionInstructionsLabel();
 
   @Description("Datasource label")
   @DefaultStringValue("Datasource")
   String datasourceLabel();
 
   @Description("Table selector title")
   @DefaultStringValue("Table selector")
   String tableSelectorTitle();
 
   @Description("Select all label")
   @DefaultStringValue("select all")
   String selectAllLabel();
 
   @Description("File Selector title")
   @DefaultStringValue("File Selector")
   String fileSelectorTitle();
 
   @Description("Log label")
   @DefaultStringValue("Log")
   String logLabel();
 
   @Description("Delete attribute label")
   @DefaultStringValue("Delete Attribute")
   String deleteAttribute();
 
   @Description("Delete category label")
   @DefaultStringValue("Delete Category")
   String deleteCategory();
 
   @Description("Confirmation title map")
   @DefaultStringMapValue({ //
       "deleteVariableTitle", "Delete Variable", //
       "deleteTable", "Delete Table", //
       "clearJobsList", "Clear Jobs List", //
       "cancelJob", "Cancel Job", //
       "replaceExistingFile", "Replace File", //
       "deleteFile", "Delete File", //
       "deleteKeyPair", "Delete Key Pair", //
       "removeProject", "Remove Project",//
       "removeDatasource", "Remove Datasource",//
       "removeReportTemplate", "Remove Report Template",//
       "removeFunctionalUnit", "Remove Unit",//
       "generateFunctionalUnitIdentifiers", "Generate Unit Identifiers",//
       "overwriteVariable", "Overwrite Variable",//
       "overwriteView", "Overwrite View",//
       "createView", "Create View",//
       "removeView", "Remove View",//
       "removeTable", "Remove Table",//
       "deleteDataShieldAggregateMethod", "Delete Aggregating Method",//
       "deleteDataShieldAssignMethod", "Delete Assigning Method",//
       "deleteDataShieldPackage", "Delete Package",//
       "publishDataShieldMethods", "Publish Package Methods",//
       "deleteDatabase", "Delete Database",//
       "removeGroup", "Remove Group",//
       "removeUser", "Remove User"//
   })
   Map<String, String> confirmationTitleMap();
 
   @Description("Confirmation message map")
   @DefaultStringMapValue({ //
       "confirmVariableDelete", "Delete the currently displayed variable?", //
       "removingTablesFromViewMayAffectVariables",
       "Removing tables from the view will have an impact on which Variables can be defined.", //
       "confirmClearJobsList",
       "All the completed jobs (succeeded, failed or cancelled) will be removed from the jobs list. Currently running jobs will be unaffected.<br /><br />Please confirm that you want to clear the jobs list.",
       //
       "confirmCancelJob",
       "The task will be cancelled. Changes will be rolled back as much as possible: although cancelled, a task might be partially completed.<br /><br />Please confirm that you want cancel this task.",
       //
       "confirmReplaceExistingFile",
       "The file that you are uploading already exist in the file system.<br /><br />Please confirm that you want to replace the existing file.",
       //
       "confirmDeleteFile", "The file(s) will be removed from the file system. Please confirm.", //
       "confirmDeleteKeyPair",
       "Please confirm that you want to remove the Key Pair. All associated encrypted material will not be accessible anymore",
 //
       "confirmRemoveProject",
       "Please confirm that you want to remove permanently the current project and all associated data.", //
       "confirmRemoveDatasource",
       "Please confirm that you want to remove the current datasource from Opal configuration.",//
       "confirmDeleteReportTemplate",
       "Please confirm that you want to remove the current Report Template from Opal configuration (report design and generated reports will not be affected).",
 //
       "confirmDeleteFunctionalUnit",
       "Please confirm that you want to remove the current Unit from Opal configuration. All encrypted material will not be accessible anymore",
 //
       "confirmOverwriteVariable",
       "A variable with the same name already exists. Please confirm that you want to overwrite this variable.",//
       "confirmOverwriteView",
       "A view with the same name already exists. Please confirm that you want to overwrite this view.",//
       "confirmCreateView", "Please confirm that you want to create a new view.",//
       "confirmRemoveView", "Please confirm that you want to remove the current view.",//
       "confirmRemoveTable",
       "Please confirm that you want to remove the current table. This cannot be undone and all data associated with this table will be lost.",
 //
       "confirmDeleteDataShieldAssignMethod", "Please confirm that you want to remove this assigning method.",//
       "confirmDeleteDataShieldAggregateMethod", "Please confirm that you want to remove this aggregating method.",//
       "confirmDeleteDataShieldPackage", "Please confirm that you want to remove this package and all its methods.",//
       "confirmPublishDataShieldMethods", "Please confirm that you want to publish this package methods.",//
       "confirmDeleteDatabase", "Please confirm that you want to remove this database.",//
       "confirmRemoveGroup", "Please confirm that you want to remove the group {0}.",//
       "confirmRemoveUser", "Please confirm that you want to remove the user {0}."//
   })
   Map<String, String> confirmationMessageMap();
 
   @Description("A name is required when creating a new folder")
   @DefaultStringValue("You must specify a folder name")
   String folderNameIsRequired();
 
   @Description("Dot names are not permitted")
   @DefaultStringValue("The names '.' and '..' are not permitted.")
   String dotNamesAreInvalid();
 
   @Description("Data copy instructions")
   @DefaultStringValue("Select the tables to be copied.")
   String dataCopyInstructions();
 
   @Description("Data copy pending conclusion")
   @DefaultStringValue("Data copy task is being launched.")
   String dataCopyPendingConclusion();
 
   @Description("Data copy completed conclusion")
   @DefaultStringValue("Data copy task was successfully launched.")
   String dataCopyCompletedConclusion();
 
   @Description("Data copy failed conclusion")
   @DefaultStringValue("Data copy task launch failed.")
   String dataCopyFailedConclusion();
 
   @Description("Data copy destination")
   @DefaultStringValue("Select the destination of the copy.")
   String dataCopyDestination();
 
   @Description("Data export instructions")
   @DefaultStringValue("Select the tables to be exported.")
   String dataExportInstructions();
 
   @Description("Data export pending conclusion")
   @DefaultStringValue("Data export task is being launched.")
   String dataExportPendingConclusion();
 
   @Description("Data export completed conclusion")
   @DefaultStringValue("Data export task was successfully launched.")
   String dataExportCompletedConclusion();
 
   @Description("Data export failed conclusion")
   @DefaultStringValue("Data export task launch failed.")
   String dataExportFailedConclusion();
 
   @Description("Data export options")
   @DefaultStringValue("Select the export options.")
   String dataExportOptions();
 
   @Description("Data export destination")
   @DefaultStringValue("Select the destination of the exportation.")
   String dataExportDestination();
 
   @Description("Data export unit")
   @DefaultStringValue("Select the participant identifiers to be exported.")
   String dataExportUnit();
 
   @Description("Data import instructions")
   @DefaultStringValue(
       "Select the file to be imported and the destination datasource.")
   String dataImportInstructions();
 
   @Description("Data import Compared Datasources Report instructions")
   @DefaultStringValue(
       "Review the data dictionary that will be imported.")
   String dataImportComparedDatasourcesReportStep();
 
   @Description("Data import Values instructions")
   @DefaultStringValue("Review the data that will be imported.")
   String dataImportValuesStep();
 
   @Description("Data import instructions conclusion")
   @DefaultStringValue("Data import task is launched.")
   String dataImportInstructionsConclusion();
 
   @Description("Identifiers Map File Step")
   @DefaultStringValue("Select the file of identifiers to be mapped.")
   String identifiersMapFileStep();
 
   @Description("Identifiers Map Unit Step")
   @DefaultStringValue(
       "Select which unit is to be used for retrieving the participants to be mapped.")
   String identifiersMapUnitStep();
 
   @Description("Identifier map pending conclusion")
   @DefaultStringValue("Identifier mapping task is being launched.")
   String identifierMapPendingConclusion();
 
   @Description("Identifier map completed conclusion")
   @DefaultStringValue(
       "Identifier mapping task completed successfully.")
   String identifierMapCompletedConclusion();
 
   @Description("Identifier map update count")
   @DefaultStringValue("Number of Participants updated")
   String identifierMapUpdateCount();
 
   @Description("Identifier map failed conclusion")
   @DefaultStringValue("Identifier mapping task failed.")
   String identifierMapFailedConclusion();
 
   @Description("Identifiers Import File Step")
   @DefaultStringValue("Select the file of identifiers to be imported.")
   String identifiersImportFileStep();
 
   @Description("Identifier import pending conclusion")
   @DefaultStringValue("Identifier import task is being launched.")
   String identifierImportPendingConclusion();
 
   @Description("Identifier import completed conclusion")
   @DefaultStringValue(
       "Identifier import task completed successfully.")
   String identifierImportCompletedConclusion();
 
   @Description("Identifier import failed conclusion")
   @DefaultStringValue("Identifier import task failed.")
   String identifierImportFailedConclusion();
 
   @Description("Export to Excel icon title")
   @DefaultStringValue("Export to Excel file")
   String exportToExcelTitle();
 
   @Description("Download View XML menu item")
   @DefaultStringValue("Download View XML")
   String downloadViewXML();
 
   @Description("Csv label")
   @DefaultStringValue("CSV")
   String csvLabel();
 
   @Description("Excel label")
   @DefaultStringValue("Excel")
   String excelLabel();
 
   @Description("Opal XML label")
   @DefaultStringValue("Opal XML")
   String opalXmlLabel();
 
   @Description("SPSS label")
   @DefaultStringValue("SPSS")
   String spssLabel();
 
   @Description("Opal REST label")
   @DefaultStringValue("Opal")
   String opalRestLabel();
 
   @Description("Limesurvey label")
   @DefaultStringValue("LimeSurvey")
   String limesurveyLabel();
 
   @Description("Health Canada label")
   @DefaultStringValue("Health Canada")
   String healthCanadaLabel();
 
   @Description("Geonames Postal Codes label")
   @DefaultStringValue("Postal Codes")
   String geonamesPostalCodesLabel();
 
   @Description("Select file and data format label")
   @DefaultStringValue("Select a file and data format")
   String selectFileAndDataFormatLabel();
 
   @Description("Row must be integer message")
   @DefaultStringValue("Row must be an integer.")
   String rowMustBeIntegerMessage();
 
   @Description("Row must be positive message")
   @DefaultStringValue("Row must must be a positive value.")
   String rowMustBePositiveMessage();
 
   @Description("Charset must not be null message")
   @DefaultStringValue("The character set must not be empty.")
   String charsetMustNotBeNullMessage();
 
   @Description("Charset does not exist message")
   @DefaultStringValue("The specified character set could not be found.")
   String charsetDoesNotExistMessage();
 
   @Description("Sheet label")
   @DefaultStringValue("Sheet")
   String sheetLabel();
 
   @Description("Row number label")
   @DefaultStringValue("Row Number")
   String rowNumberLabel();
 
   @Description("Error label")
   @DefaultStringValue("Error")
   String errorLabel();
 
   @Description("Datasource parsing error map")
   @DefaultStringMapValue({ //
       "CategoryNameRequired", "[{0}:{1}] Category name required: table '{2}', variable '{3}'", //
       "CategoryVariableNameRequired", "[{0}:{1}] Category variable name required: table '{2}'", //
       "DuplicateCategoryName", "[{0}:{1}] Duplicate category name: table '{2}', variable '{3}', category '{4}'", //
       "DuplicateColumns", "[{0}:{1}] Duplicate columns: table '{2}', column '{3}'", //
       "DuplicateVariableName", "[{0}:{1}] Duplicate variable name: table '{2}', variable '{3}'", //
       "TableDefinitionErrors", "Table definition errors", //
       "UnexpectedErrorInCategory", "[{0}:{1}] Unexpected error in category definition: table '{2}', variable '{3}'", //
       "UnexpectedErrorInVariable", "[{0}:{1}] Unexpected error in variable definition: table '{2}'", //
       "UnidentifiedVariableName", "[{0}:{1}] Unidentified variable name: table '{2}', variable '{3}'", //
       "UnknownValueType", "[{0}:{1}] Unknown value type: table '{2}', variable '{3}', type '{4}'", //
       "VariableCategoriesDefinitionErrors", "Variable categories definition errors", //
       "VariableNameCannotContainColon", "[{0}:{1}] Variable name cannot contain colon: table '{2}', variable '{3}'", //
       "VariableNameRequired", "[{0}:{1}] Variable name required: table '{2}'", //
       "CsvInitialisationError", "Error occurred initialising csv datasource", //
       "CsvVariablesHeaderMustContainName", "The variables.csv header must contain 'name'", //
       "CsvVariablesHeaderMustContainValueType", "The variables.csv header must contain 'valueType'.", //
       "CsvVariablesHeaderMustContainEntityType", "The variables.csv header must contain 'entityType'.", //
       "CsvCannotCreateWriter", "Cannot create writer", //
       "CsvCannotSetVariableHeader", "Cannot set variables header", //
       "CsvCannotObtainWriter", "Cannot get csv writer", //
       "CsvCannotObtainReader", "Cannot get csv reader", //
       "LimeDuplicateVariableName", "[{0}] Survey contains duplicated variable name: {1}",//
       "InvalidCharsetCharacter",
       "File contains invalid characters at row '{0}' in string '{1}'. Please make sure the file is a valid SPSS file and that you have chosen the correct character set.",
 //
       "InvalidCategoryCharsetCharacter",
       "File contains invalid characters at row '{0}' for variable category '{1}' in string '{2}'. Please make sure the file is a valid SPSS file and that you have chosen the correct character set.",
 //
       "SpssFailedToLoadMetadata",
       "Failed to load metadata from file '{0}'. Please make sure you have chosen the correct character set. ",//
       "SpssFailedToLoadData",
       "Failed to load data from file '{0}'. Please make sure you have chosen the correct character set.",//
       "SpssDuplicateEntity",
       "Duplicate entity identifier '{0}' at row '{1}'. Please make sure that the variable '{2}' representing entities has unique values.",
 //
       "FailedToOpenFile", "Failed to open file '{0}'. Please make sure you have chosen the correct character set." })
   Map<String, String> datasourceParsingErrorMap();
 
   @Description("Datasource comparison error map")
   @DefaultStringMapValue({ "IncompatibleValueType", "Incompatible value types: {0} / {1}", //
       "IncompatibleEntityType", "Incompatible entity types: {0} / {1}", //
       "CsvVariableMissing",
       "Variable name exists in csv data file, but no Variable associated with this name exists in the destination table" })
   Map<String, String> datasourceComparisonErrorMap();
 
   @Description("New variables label")
   @DefaultStringValue("New Variables")
   String newVariablesLabel();
 
   @Description("Unmodified variables label")
   @DefaultStringValue("Unmodified Variables")
   String unmodifiedVariablesLabel();
 
   @Description("Modified variables label")
   @DefaultStringValue("Modified Variables")
   String modifiedVariablesLabel();
 
   @Description("Conflicted variables label")
   @DefaultStringValue("Conflicts")
   String conflictedVariablesLabel();
 
   @Description("No data available label")
   @DefaultStringValue("No data available")
   String noDataAvailableLabel();
 
   @Description("No summary data available label")
   @DefaultStringValue("No summary data available")
   String noSummaryDataAvailableLabel();
 
   @Description("Summary preview pending on label")
   @DefaultStringValue("Summary preview pending on")
   String summaryPreviewPendingLabel();
 
   @Description("/{0} entities label")
   @DefaultStringValue("/{0} entities")
   String summaryTotalEntitiesLabel();
 
   @Description("Full summary pending... label")
   @DefaultStringValue("Full summary pending...")
   String summaryFullPendingLabel();
 
   @Description("This is a summary preview on label")
   @DefaultStringValue("This is a summary preview on")
   String summaryPreviewOnLabel();
 
   @Description("Fetch summary preview on label")
   @DefaultStringValue("Fetch summary preview on")
   String summaryFetchSummaryLabel();
 
   @Description("Remove label")
   @DefaultStringValue("Remove")
   String removeLabel();
 
   @Description("View label")
   @DefaultStringValue("View")
   String viewLabel();
 
   @Description("Add View label")
   @DefaultStringValue("Add View")
   String addViewLabel();
 
   @Description("Add Update Tables label")
   @DefaultStringValue("Add/Update Tables")
   String addUpdateTablesLabel();
 
   @Description("Create Datasource Completed summary")
   @DefaultStringValue("The datasource was successfully created.")
   String datasourceCreationCompleted();
 
   @Description("Create Datasource Failed summary")
   @DefaultStringValue("The datasource creation has failed.")
   String datasourceCreationFailed();
 
   @Description("Item label")
   @DefaultStringValue("Item")
   String itemLabel();
 
   @Description("Script label")
   @DefaultStringValue("Script")
   String scriptLabel();
 
   @Description("Script Evaluation label")
   @DefaultStringValue("Script Evaluation")
   String scriptEvaluationLabel();
 
   @Description("Line label")
   @DefaultStringValue("Line")
   String lineLabel();
 
   @Description("Add new category title")
   @DefaultStringValue("Add New Category")
   String addNewCategory();
 
   @Description("Edit category title")
   @DefaultStringValue("Edit Category")
   String editCategory();
 
   @Description("Add new attribute title")
   @DefaultStringValue("Add New Attribute")
   String addNewAttribute();
 
   @Description("Edit attribute title")
   @DefaultStringValue("Edit Attribute")
   String editAttribute();
 
   @Description("Report produced date")
   @DefaultStringValue("Produced Date")
   String producedDate();
 
   @Description("Run label")
   @DefaultStringValue("Run")
   String runLabel();
 
   @Description("Download Report Design label")
   @DefaultStringValue("Download Report Design")
   String downloadReportDesignLabel();
 
   @Description("Paging of label")
   @DefaultStringValue("to")
   String toLabel();
 
   @Description("Values label")
   @DefaultStringValue("Values")
   String valuesLabel();
 
   @Description("Copy of label")
   @DefaultStringValue("Copy_of_")
   String copyOf();
 
   @Description("Script contains errors and was not saved")
   @DefaultStringValue(
       "The script contains errors and was not saved. Click 'Test' to execute the script and see a detailed report of the errors.")
   String scriptContainsErrorsAndWasNotSaved();
 
   @Description("Create Datasource Step summary")
   @DefaultStringValue("Select the type of datasource to be created.")
   String createDatasourceStepSummary();
 
   @Description("Datasource Options label")
   @DefaultStringValue("Provide datasource type specific options.")
   String datasourceOptionsLabel();
 
   @Description("Create Datasource Process summary")
   @DefaultStringValue(
       "The datasource is in the process of being created.")
   String createDatasourceProcessSummary();
 
   @Description("Edit View Type Step")
   @DefaultStringValue("Define the type of view to be added.")
   String editViewTypeStep();
 
   @Description("Edit View Tables Step")
   @DefaultStringValue("Select the tables to be included in the view.")
   String editViewTablesStep();
 
   @Description("No locale label")
   @DefaultStringValue("no locale")
   String noLocale();
 
   @Description("Add Unit label")
   @DefaultStringValue("Add Unit")
   String addUnit();
 
   @Description("Edit Unit label")
   @DefaultStringValue("Edit Unit")
   String editUnit();
 
   @Description("Download Identifiers label")
   @DefaultStringValue("Export Identifiers")
   String downloadUnitIdentifiers();
 
   @Description("Export Identifiers to Excel label")
   @DefaultStringValue("Export Identifiers Mapping")
   String exportUnitIdentifiersToExcel();
 
   @Description("Add Cryptographic Key label")
   @DefaultStringValue("Add Cryptographic Key")
   String addAddCryptographicKey();
 
   @Description("Add Cryptographic Key label")
   @DefaultStringValue("Add Cryptographic Key")
   String addCryptoKey();
 
   @Description("Generate Identifiers label")
   @DefaultStringValue("Generate Identifiers")
   String generateUnitIdentifiers();
 
   @Description("Import Unit Identifiers From Data label")
   @DefaultStringValue("Import Identifiers from Data File")
   String importUnitIdentifiersFromData();
 
   @Description("Import Mapped Unit Identifiers label")
   @DefaultStringValue("Add/Update Identifiers Mapping")
   String importMappedUnitIdentifiers();
 
   @Description("Alias label")
   @DefaultStringValue("Name")
   String aliasLabel();
 
   @Description("Select Key Type Step label")
   @DefaultStringValue(
       "Provide a name and a type for the cyrptographic key to add to this Unit.")
   String keyTypeStep();
 
   @Description("Import Certificate label")
   @DefaultStringValue(
       "Provide the Public Certificate by pasting it here (PEM format).")
   String importCertificateStep();
 
   @Description("Private Key Step label")
   @DefaultStringValue(
       "Select how to add the private key of the key pair (create a new one or import an existing one)")
   String privateKeyStep();
 
   @Description("Public Key Step label")
   @DefaultStringValue("Provide the Public Certificate definition.")
   String publicKeyStep();
 
   @Description("Keystore label")
   @DefaultStringValue("Keystore")
   String keystoreLabel();
 
   @Description("Properties label")
   @DefaultStringValue("Properties")
   String propertiesLabel();
 
   @Description("Cancel label")
   @DefaultStringValue("Cancel")
   String cancelLabel();
 
   @Description("Close label")
   @DefaultStringValue("Close")
   String closeLabel();
 
   @Description("Finish label")
   @DefaultStringValue("Finish")
   String finishLabel();
 
   @Description("Next label")
   @DefaultStringValue("Next >")
   String nextLabel();
 
   @Description("Previous label")
   @DefaultStringValue("< Previous")
   String previousLabel();
 
   @Description("Help label")
   @DefaultStringValue("Help")
   String helpLabel();
 
   @Description("KeyType map")
   @DefaultStringMapValue({ "KEY_PAIR", "Key Pair", "CERTIFICATE", "Certificate" })
   Map<String, String> keyTypeMap();
 
   @Description("Paste Private Key PEM label")
   @DefaultStringValue("(paste private key in PEM format)")
   String pastePrivateKeyPEM();
 
   @Description("Paste Public Key PEM label")
   @DefaultStringValue("(paste public certificate in PEM format)")
   String pastePublicKeyPEM();
 
   @Description("Data import title")
   @DefaultStringValue("Import Data")
   String importData();
 
   @Description("Data export title")
   @DefaultStringValue("Export Data")
   String exportData();
 
   @Description("Data copy title")
   @DefaultStringValue("Copy Data")
   String copyData();
 
   @Description("Variables Import File Selection Step")
   @DefaultStringValue(
       "Select an Excel or SPSS variables file for batch edition of tables and variables.")
   String variablesImportFileSelectionStep();
 
   @Description("Variables Import Compare Step")
   @DefaultStringValue("Review the modifications before applying them.")
   String variablesImportCompareStep();
 
   @Description("Variables Import Pending")
   @DefaultStringValue("Importing Variables...")
   String variablesImportPending();
 
   @Description("Add View Pending")
   @DefaultStringValue("View is being created ...")
   String addViewPending();
 
   @Description("Add View Success")
   @DefaultStringValue("View successfully created.")
   String addViewSuccess();
 
   @Description("Update View Success")
   @DefaultStringValue("View successfully updated.")
   String updateViewSuccess();
 
   @Description("Add View Failed")
   @DefaultStringValue("View creation failed.")
   String addViewFailed();
 
   @Description("No format options step")
   @DefaultStringValue(
       "No format options are available for the selected file format")
   String noFormatOptionsStep();
 
   @Description("CSV format options step")
   @DefaultStringValue("CSV format options step")
   String csvFormatOptionsStep();
 
   @Description("Datasource Type map")
   @DefaultStringMapValue({ "mongodb", "MongoDB", //
       "hibernate", "Opal SQL", //
       "jdbc", "Custom SQL", //
       "fs", "XML", //
       "csv", "CSV", //
       "excel", "Excel", //
       "null", "None" })
   Map<String, String> datasourceTypeMap();
 
   @Description("Data Import Format Step")
   @DefaultStringValue("Select the format of data you wish to import.")
   String dataImportFormatStep();
 
   @Description("Data Import File Step")
   @DefaultStringValue("Select the source to be imported.")
   String dataImportFileStep();
 
   @Description("Data Import Unit Step")
   @DefaultStringValue("Specify how the participant are identified.")
   String dataImportUnitStep();
 
   @Description("Data Import Configuration Step")
   @DefaultStringValue("Configure data import")
   String configureDataImport();
 
   @Description("Data Import Archive Step")
   @DefaultStringValue("Specify whether the data file is to be archived.")
   String dataImportArchiveStep();
 
   @Description("Data Import Destination Step")
   @DefaultStringValue("Select the destination of the import.")
   String dataImportDestinationStep();
 
   @Description("Data Import Pending Validation")
   @DefaultStringValue("Data to import are being validated...")
   String dataImportPendingValidation();
 
   @Description("Data Import Completed Validation")
   @DefaultStringValue("Data import validation completed.")
   String dataImportCompletedValidation();
 
   @Description("Data Import Failed Validation")
   @DefaultStringValue("Data import validation failed.")
   String dataImportFailedValidation();
 
   @Description("Import Unit Identifiers Instructions")
   @DefaultStringValue("First column MUST be identifiers from unit")
   String importUnitIdentifiersInstructions();
 
   @Description("Import Opal Identifiers Instructions")
   @DefaultStringValue("First column MUST be Opal identifiers.")
   String importOpalIdentifiersInstructions();
 
   @Description("Package description label")
   @DefaultStringValue("Package Description")
   String dataShieldPackageDescription();
 
   @Description("Add DataShield package label")
   @DefaultStringValue("Add DataSHIELD Package")
   String addDataShieldPackage();
 
   @Description("Add DataShield method label")
   @DefaultStringValue("Add Method")
   String addDataShieldMethod();
 
   @Description("Edit DataShield method label")
   @DefaultStringValue("Edit Method")
   String editDataShieldMethod();
 
   @Description("R Script label")
   @DefaultStringValue("R Script")
   String rScriptLabel();
 
   @Description("R Function label")
   @DefaultStringValue("R Function")
   String rFunctionLabel();
 
   @Description("Who label")
   @DefaultStringValue("Who")
   String whoLabel();
 
   @Description("Subject Type map")
   @DefaultStringMapValue({ "USER", "User Name", //
       "GROUP", "Group Name" })
   Map<String, String> subjectTypeMap();
 
   @Description("Derive label")
   @DefaultStringValue("Derive")
   String deriveLabel();
 
   @Description("Derive Categorize label")
   @DefaultStringValue("Categorize")
   String deriveCategorizeLabel();
 
   @Description("Derive Custom label")
   @DefaultStringValue("Custom")
   String deriveCustomLabel();
 
   @Description("Derive This to Another label")
   @DefaultStringValue("This variable to another")
   String deriveThisVariableToAnotherLabel();
 
   @Description("Derive Another to This label")
   @DefaultStringValue("Another variable to this")
   String deriveAnotherVariableToThisLabel();
 
   @Description("Invalid Destination View label")
   @DefaultStringValue("Not a valid destination view.")
   String invalidDestinationView();
 
   @Description("Derived Variable Name Required label")
   @DefaultStringValue("Derived variable name is required.")
   String derivedVariableNameRequired();
 
   @Description("Destination View Name Required label")
   @DefaultStringValue("Destination view name is required.")
   String destinationViewNameRequired();
 
   @Description("Add Derived Variable To View Only label")
   @DefaultStringValue(
       "A derived variable can only be added to a view.")
   String addDerivedVariableToViewOnly();
 
   @Description("Derived Variable Evaluation label")
   @DefaultStringValue(
       "Review the summary and the values of the derived variable.")
   String derivedVariableEvaluation();
 
   @Description("Save Derived Variable label")
   @DefaultStringValue(
       "Name the derived variable and select the view in which it will appear.")
   String saveDerivedVariable();
 
   @Description("Recode Categorie Step title")
   @DefaultStringValue(
       "Recode categories and observed distinct values to new values.")
   String recodeCategoriesStepTitle();
 
   @Description("Recode Boolean Step title")
   @DefaultStringValue("Recode logical values to new values.")
   String recodeBooleanStepTitle();
 
   @Description("Derive From Variable Step title")
   @DefaultStringValue(
       "Select from which variable derivation should be performed")
   String deriveFromVariableStepTitle();
 
   @Description("Empty Value label")
   @DefaultStringValue("Empty value")
   String emptyValuesLabel();
 
   @Description("Other Value label")
   @DefaultStringValue("Other value")
   String otherValuesLabel();
 
   @Description("True label")
   @DefaultStringValue("True")
   String trueLabel();
 
   @Description("False label")
   @DefaultStringValue("False")
   String falseLabel();
 
   @Description("Recode Temporal Method Step title")
   @DefaultStringValue(
       "Dates and times can be grouped together using the following methods:")
   String recodeTemporalMethodStepTitle();
 
   @Description("Recode Temporal Map Step title")
   @DefaultStringValue("Map each time range to a new value.")
   String recodeTemporalMapStepTitle();
 
   @Description("Recode Numerical Method Step title")
   @DefaultStringValue(
       "Numerical values can be grouped together using the following methods:")
   String recodeNumericalMethodStepTitle();
 
   @Description("Recode Numerical Map Step title")
   @DefaultStringValue("Map each range or discrete value to a new value.")
   String recodeNumericalMapStepTitle();
 
   @Description("Recode Open Textual Method Step Title")
   @DefaultStringValue(
       "Group the values using the following method.")
   String recodeOpenTextualMethodStepTitle();
 
   @Description("Recode Open Textual Map Step Title")
   @DefaultStringValue("Map original values to new values.")
   String recodeOpenTextualMapStepTitle();
 
   @Description("Recode Custom Step Title")
   @DefaultStringValue("Specify the custom derivation script.")
   String recodeCustomDeriveStepTitle();
 
   @Description("Time map")
   @DefaultStringMapValue({ //
       "Hour", "Hour", //
       "Monday", "Monday", //
       "Tuesday", "Tuesday", //
       "Wednesday", "Wednesday", //
       "Thursday", "Thursday", //
       "Friday", "Friday", //
       "Saturday", "Saturday", //
       "Sunday", "Sunday", //
       "MONDAY", "Monday", //
       "TUESDAY", "Tuesday", //
       "WEDNESDAY", "Wednesday", //
       "THURSDAY", "Thursday", //
       "FRIDAY", "Friday", //
       "SATURDAY", "Saturday", //
       "SUNDAY", "Sunday", //
       "January", "January", //
       "February", "February", //
       "March", "March", //
       "April", "April", //
       "May", "May", //
       "June", "June", //
       "July", "July", //
       "August", "August", //
       "September", "September", //
       "October", "October", //
       "November", "November", //
       "December", "December", //
       "Second", "Second", //
       "Minute", "Minute", //
       "Hour", "Hour", //
       "Day", "Day", //
       "Week", "Week", //
       "Month", "Month", //
       "Quarter", "Quarter", //
       "Semester", "Semester", //
       "Year", "Year", //
       "Lustrum", "Lustrum", //
       "Decade", "Decade", //
       "Century", "Century", //
       "Millenium", "Millenium", //
       "Era", "Era" //
   })
   Map<String, String> timeMap();
 
   @Description("Time Group map")
   @DefaultStringMapValue({ //
       "HOUR_OF_DAY", "Hour of Day", //
       "DAY_OF_WEEK", "Day of Week", //
       "DAY_OF_MONTH", "Day of Month", //
       "DAY_OF_YEAR", "Day of Year", //
       "WEEK_OF_MONTH", "Week of Month", //
       "WEEK_OF_YEAR", "Week of Year", //
       "MONTH", "Month", //
       "MONTH_OF_YEAR", "Month of Year", //
       "QUARTER_OF_YEAR", "Quarter of Year", //
       "QUARTER", "Quarter", //
       "SEMESTER_OF_YEAR", "Semester of Year", //
       "SEMESTER", "Semester", //
       "YEAR", "Year", //
       "LUSTRUM", "Lustrum (5 years period)", //
       "DECADE", "Decade (10 years period)", //
       "CENTURY", "Century" //
   })
   Map<String, String> timeGroupMap();
 
   @Description("DataSHIELD Labels")
   @DefaultStringMapValue({ //
       "Aggregate", "Aggregate",//
       "Assign", "Assign" //
   })
   Map<String, String> dataShieldLabelsMap();
 
   @Description("Project Tab Names")
   @DefaultStringMapValue({ "HOME", "Home", //
       "TABLES", "Tables", //
       "FILES", "Files", //
       "VISUALISATION", "Data visualization", //
       "REPORTS", "Reports", //
       "TASKS", "Tasks", //
       "PERMISSIONS", "Permissions", //
       "ADMINISTRATION", "Administration" //
   })
   Map<String, String> projectTabNameMap();
 
   @Description("Lower Value Limit Required label")
   @DefaultStringValue("Lower value limit is required.")
   String lowerValueLimitRequired();
 
   @Description("Upper Value Limit Required label")
   @DefaultStringValue("Upper value limit is required.")
   String upperValueLimitRequired();
 
   @Description("Lower Limit Greater Than Upper Limit label")
   @DefaultStringValue(
       "Lower value limit cannot be greater than upper value limit.")
   String lowerLimitGreaterThanUpperLimit();
 
   @Description("Ranges Length Required label")
   @DefaultStringValue("Ranges Length is required.")
   String rangesLengthRequired();
 
   @Description("Ranges Count Required label")
   @DefaultStringValue("Number of ranges is required.")
   String rangesCountRequired();
 
   @Description("Value is not a category label")
   @DefaultStringValue("This value does not correspond to any category")
   String valueIsNotACategory();
 
   @Description("Ranges Overlap label")
   @DefaultStringValue("Range is overlapping another range.")
   String rangeOverlap();
 
   @Description("Value Map Already Added label")
   @DefaultStringValue("This value is already mapped.")
   String valueMapAlreadyAdded();
 
   @Description("Frequency label")
   @DefaultStringValue("Frequency")
   String frequency();
 
   @Description("Create a Coding View")
   @DefaultStringValue("Create a Coding View")
   String createCodingView();
 
   @Description("Script Evaluation Failed Label")
   @DefaultStringValue(
       "Script evaluation failed: check if value type is correct.")
   String scriptEvaluationFailed();
 
   @Description("Property label")
   @DefaultStringValue("Property")
   String property();
 
   @Description("Value label")
   @DefaultStringValue("Value")
   String value();
 
   @Description("Participant label")
   @DefaultStringValue("Participant")
   String participant();
 
   @Description("Download label")
   @DefaultStringValue("Download")
   String downloadLabel();
 
   @Description("Show label")
   @DefaultStringValue("Show")
   String showLabel();
 
   @Description("Hide label")
   @DefaultStringValue("Hide")
   String hideLabel();
 
   @Description("Add Database")
   @DefaultStringValue("Add Database")
   String addDatabase();
 
   @Description("Edit Database")
   @DefaultStringValue("Edit Database")
   String editDatabase();
 
   @Description("Username label")
   @DefaultStringValue("Username")
   String usernameLabel();
 
   @Description("Password label")
   @DefaultStringValue("Password")
   String passwordLabel();
 
   @Description("Driver label")
   @DefaultStringValue("Driver")
   String driverLabel();
 
   @Description("Url label")
   @DefaultStringValue("URL")
   String urlLabel();
 
   @Description("Usage label")
   @DefaultStringValue("Usage")
   String usageLabel();
 
   @Description("SQL Schema label")
   @DefaultStringValue("SQL Schema")
   String sqlSchemaLabel();
 
   @Description("SQL label")
   @DefaultStringValue("SQL")
   String sqlLabel();
 
   @Description("MongoDB label")
   @DefaultStringValue("MongoDB")
   String mongoDbLabel();
 
   @Description("Default Entity Type label")
   @DefaultStringValue("Default Entity Type")
   String defaultEntityTypeLabel();
 
   @Description("Key/Value label")
   @DefaultStringValue("key=value")
   String keyValueLabel();
 
   @Description("Identifiers Sync Count label")
   @DefaultStringValue("Identifiers / Total")
   String identifiersSyncCountLabel();
 
   @Description("Identifiers Sync Datasource Step")
   @DefaultStringValue(
       "Some datasources could have entities with identifiers unknown in the identifiers database (usually after an additional database has been connected to Opal). This operation will import them in the identifiers database.")
   String identifiersSyncDatasourceStep();
 
   @Description("Identifiers Sync Table Step")
   @DefaultStringValue(
       "Preview the count of identifiers that can be imported and select from which table identifiers will be extracted.")
   String identifiersSyncTableStep();
 
   @Description("Information label")
   @DefaultStringValue("Information")
   String infoLabel();
 
   @Description("Created label")
   @DefaultStringValue("Created")
   String createdLabel();
 
   @Description("Created On label")
   @DefaultStringValue("Created on {0}")
   String createdOnLabel();
 
   @Description("Last Update label")
   @DefaultStringValue("Last Update")
   String lastUpdateLabel();
 
   @Description("Last Update On label")
   @DefaultStringValue("Last update {0}")
   String lastUpdateOnLabel();
 
   @Description("Table last update")
   @DefaultStringValue("Table last update")
   String tableLastUpdateLabel();
 
   @Description("Index last update")
   @DefaultStringValue("Index last update")
   String indexLastUpdateLabel();
 
   @Description("Schedule")
   @DefaultStringValue("Schedule")
   String scheduleLabel();
 
   @Description("Manually")
   @DefaultStringValue("Manually")
   String manuallyLabel();
 
   @Description("Every 5 minutes")
   @DefaultStringValue("Every 5 minutes")
   String minutes5Label();
 
   @Description("Every 15 minutes")
   @DefaultStringValue("Every 15 minutes")
   String minutes15Label();
 
   @Description("Every 30 minutes")
   @DefaultStringValue("Every 30 minutes")
   String minutes30Label();
 
   @Description("Every hour")
   @DefaultStringValue("Every hour")
   String hourlyLabel();
 
   @Description("Every hour at")
   @DefaultStringValue("Every hour at {0} minutes")
   String hourlyAtLabel();
 
   @Description("Every day")
   @DefaultStringValue("Every day")
   String dailyLabel();
 
   @Description("Every day at")
   @DefaultStringValue("Every day at {0}:{1}")
   String dailyAtLabel();
 
   @Description("Every week")
   @DefaultStringValue("Every week")
   String weeklyLabel();
 
   @Description("Every week at")
   @DefaultStringValue("Every week on {0} at {1}:{2}")
   String weeklyAtLabel();
 
   @Description("Edit schedule")
   @DefaultStringValue("Edit schedule")
   String editScheduleLabel();
 
   @Description("minutes")
   @DefaultStringValue("minutes")
   String minutesLabel();
 
   @Description("Index outdated")
   @DefaultStringValue("Indexation outdated and scheduled")
   String indexOutdatedScheduled();
 
   @Description("Index outdated and not scheduled")
   @DefaultStringValue("Indexation outdated and not scheduled")
   String indexOutdatedNotScheduled();
 
   @Description("Index up-to-date")
   @DefaultStringValue("Indexation up-to-date")
   String indexUpToDate();
 
   @Description("Indexation not scheduled")
   @DefaultStringValue("Indexation not scheduled")
   String indexNotScheduled();
 
   @Description("Indexation in progress")
   @DefaultStringValue("Indexation on progress")
   String indexInProgress();
 
   @Description("Table index is out-of-date")
   @DefaultStringValue("Table index is out-of-date")
   String indexStatusOutOfDate();
 
   @Description("Indexing of this table is in progress")
   @DefaultStringValue("Indexing of this table is in progress")
   String indexStatusInProgress();
 
   @Description("Values of this table are not indexed")
   @DefaultStringValue("Values of this table are not indexed")
   String indexStatusNotIndexed();
 
   @Description("Table index is up-to-date")
   @DefaultStringValue("Indices")
   String indicesLabel();
 
   @Description("Indice label")
   @DefaultStringValue("Indice")
   String indiceLabel();
 
   @Description("Indices label")
   @DefaultStringValue("Table index is up-to-date")
   String indexAlertUpToDate();
 
   @Description("Select some Tables label")
   @DefaultStringValue("Select some tables...")
   String selectSomeTables();
 
   @Description("File based datasources label")
   @DefaultStringValue("Files")
   String fileBasedDatasources();
 
   @Description("Remote server based datasources label")
   @DefaultStringValue("Servers")
   String remoteServerBasedDatasources();
 
   @Description("Public datasources label")
   @DefaultStringValue("Public Datasources")
   String publicDatasources();
 
   @Description("Elastic Search Configuration")
   @DefaultStringValue("Elastic search configuration")
   String esConfigurationLabel();
 
   @Description("Specify Generate Functional Unit Identifier")
   @Constants.DefaultStringValue(
       "{0} new identifier will be generated for this unit. To customize the identifier format modify the settings below.")
   String specifyGenerateFunctionalUnitIdentifier();
 
   @Description("Specify Generate Functional Unit Identifiers")
   @Constants.DefaultStringValue(
       "{0} new identifiers will be generated for this unit. To customize the identifier format modify the settings below.")
   String specifyGenerateFunctionalUnitIdentifiers();
 
   @Description("Generate Identifiers button label")
   @DefaultStringValue("Generate")
   String generateIdentifiersButton();
 
   @Description("Generate Identifiers size help")
   @DefaultStringValue("Identifier size must be {0} to {1} digits long")
   String generateIdentifiersSizeHelp();
 
   @Description("Title label")
   @DefaultStringValue("Title")
   String titleLabel();
 
   @Description("Version label")
   @DefaultStringValue("Version")
   String versionLabel();
 
   @Description("All N items on this page are selected label")
   @DefaultStringValue("All {0} {1} on this page are selected.")
   String allNItemsSelected();
 
   @Description("N item this page is selected label")
   @DefaultStringValue("{0} {1} on this page is selected.")
   String NItemSelected();
 
   @Description("N items on this page are selected label")
   @DefaultStringValue("{0} {1} on this page are selected.")
   String NItemsSelected();
 
   @Description("Select all N items label")
   @DefaultStringValue("Select all {0} {1}")
   String selectAllNItems();
 
   @Description("Clear selection label")
   @DefaultStringValue("Clear selection")
   String clearSelection();
 
   @Description("All N items are selected label")
   @DefaultStringValue("All {0} {1} are selected.")
   String allItemsSelected();
 
   @Description("Add selected variables to view label")
   @DefaultStringValue("Add selected variables to view")
   String addVariablesToViewTitle();
 
   @Description("Add variables to view... label")
   @DefaultStringValue("Add Variables to View...")
   String addVariablesToView();
 
   @Description("Add variable to view... label")
   @DefaultStringValue("Add Variable to View...")
   String addVariableToView();
 
   @Description("Original variable label")
   @DefaultStringValue("Original variable")
   String originalVariable();
 
   @Description("Index Action Index Now")
   @Constants.DefaultStringValue("Index now")
   String indexActionIndexNow();
 
   @Description("Index Action Clear")
   @Constants.DefaultStringValue("Clear")
   String indexActionClear();
 
   @Description("Index Action Schedule Indexing")
   @Constants.DefaultStringValue("Schedule indexing")
   String indexActionScheduleIndexing();
 
   @Description("Summary Frequency Plot")
   @Constants.DefaultStringValue("Frequency Plot")
   String summaryFrequencyPlot();
 
   @Description("Filter Variable label")
   @Constants.DefaultStringValue("Filter variables...")
   String filterVariables();
 
   @Description("Clear filter label")
   @DefaultStringValue("Clear filter")
   String clearFilter();
 
   @Description("Page Administration title")
   @DefaultStringValue("Administration")
   String pageAdministrationTitle();
 
   @Description("Page Dashboard title")
   @DefaultStringValue("Dashboard")
   String pageDashboardTitle();
 
   @Description("Page Datasources title")
   @DefaultStringValue("Datasources")
   String pageDatasourcesTitle();
 
   @Description("Page Databases title")
   @DefaultStringValue("Databases")
   String pageDatabasesTitle();
 
   @Description("Page DataSHIELD title")
   @DefaultStringValue("DataSHIELD")
   String pageDataShieldTitle();
 
   @Description("Page Search Index title")
   @DefaultStringValue("Index")
   String pageSearchIndexTitle();
 
   @Description("Page R Admin title")
   @DefaultStringValue("R")
   String pageRConfigTitle();
 
   @Description("Page Files title")
   @DefaultStringValue("Files")
   String pageFileExplorerTitle();
 
   @Description("Page Functional Unit title")
   @DefaultStringValue("Units")
   String pageFunctionalUnitTitle();
 
   @Description("Page Report Template title")
   @DefaultStringValue("Reports")
   String pageReportTemplatePage();
 
   @Description("Page Jobs title")
   @DefaultStringValue("Tasks")
   String pageJobsTitle();
 
   @Description("Page Users and Groups title")
   @DefaultStringValue("Users and Groups")
   String pageUsersAndGroupsTitle();
 
   @Description("Page Projects title")
   @DefaultStringValue("Projects")
   String pageProjectsTitle();
 
   @Description("Page Configuration title")
   @DefaultStringValue("Configuration")
   String pageConfigurationTitle();
 
   @Description("Page Taxonomies title")
   @DefaultStringValue("Taxonomies")
   String pageTaxonomiesTitle();
 
   @Description("Add Taxonomy")
   @DefaultStringValue("Add Taxonomy")
   String addTaxonomy();
 
   @Description("Add Vocabulary")
   @DefaultStringValue("Add Vocabulary")
   String addVocabulary();
 
   @Description("Page Java title")
   @DefaultStringValue("Java Virtual Machine")
   String pageJVMTitle();
 
   @Description("User Name label")
   @DefaultStringValue("Name")
   String userNameLabel();
 
   @Description("User Groups label")
   @DefaultStringValue("Groups")
   String userGroupsLabel();
 
   @Description("User Status label")
   @DefaultStringValue("Enabled")
   String userStatusLabel();
 
   @Description("Group Name label")
   @DefaultStringValue("Name")
   String groupNameLabel();
 
   @Description("Group Users label")
   @DefaultStringValue("Users")
   String groupUsersLabel();
 
   @Description("Enabled label")
   @DefaultStringValue("enabled")
   String enabledLabel();
 
   @Description("Disabled label")
   @DefaultStringValue("disabled")
   String disabledLabel();
 
   @Description("Add User label")
   @DefaultStringValue("Add User")
   String addUserLabel();
 
   @Description("Edit User label")
   @DefaultStringValue("Edit User")
   String editUserLabel();
 
   @Description("Add Project label")
   @DefaultStringValue("Add Project")
   String addProject();
 
   @Description("All Tables label")
   @DefaultStringValue("All tables")
   String allTablesLabel();
 
   @Description("Create Folder Modal title")
   @DefaultStringValue("Create Folder")
   String createFolderModalTitle();
 
   @Description("Upload File Modal title")
   @DefaultStringValue("File Upload")
   String uploadFileModalTitle();
 
   @Description("Entity Details Modal title")
   @DefaultStringValue("Entity Details")
   String entityDetailsModalTitle();
 
   @Description("Configure View Modal title")
   @DefaultStringValue("Configure View")
   String configureViewModalTitle();
 
   @Description("Add derived variable Modal title")
   @DefaultStringValue("Add Derived Variable")
   String addDerivedVariableModalTitle();
 
   @Description("Vcs Commit History Modal title")
   @DefaultStringValue("Commit Details")
   String vcsCommitHistoryModalTitle();
 
   @Description("Vcs Commit History Empty")
   @DefaultStringValue("No comment history available")
   String noVcsCommitHistoryAvailable();
 
   @Description("VCS Commit Info Labels")
   @DefaultStringMapValue({ "id", "ID",//
       "Author", "Author",//
       "Date", "Date",//
       "Comment", "Comment"
   })
   Map<String, String> commitInfoMap();
 
   @Description("Script Update Default Prefix Label")
   @DefaultStringValue("Update")
   String scriptUpdateDefaultPrefixLabel();
 
   @Description("User message map")
   @DefaultStringMapValue({ //
       "FailedToRetrieveVariableCommitInfos", "Failed to retrieve all variable commit information.", //
       "FailedToRetrieveVariableContent", "Failed to retrieve variable content for the given id.", //
       "FailedToRetrieveVariableCommitInfo", "Failed to retrieve variable commit information for the given id." })//
   Map<String, String> vcsResourceErrorMap();
 
   @Description("Edit label")
   @DefaultStringValue("Edit")
   String editLabel();
 
   @Description("Save label")
   @DefaultStringValue("Save")
   String saveLabel();
 
   @Description("History label")
   @DefaultStringValue("History")
   String historyLabel();
 
   @Description("Server running title")
   @DefaultStringValue("Server has been running for {0}.")
   String serverRunningFor();
 
   @Description("Statistics Related Labels")
   @DefaultStringMapValue({ "PLOT", "Plot",//
       "STATISTICS", "Statistics",//
       "DESC_STATISTICS", "Descriptive Statistic",//
       "VALUE", "Value",//
       "N", "N", //
       "MIN", "Min", //
       "MAX", "Max", //
       "MEAN", "Mean", //
       "MEDIAN", "Median", //
       "STD_DEVIATION", "Standard Deviation", //
       "VARIANCE", "Variance", //
       "SKEWNESS", "Skewness", //
       "KURTOSIS", "Kurtosis", //
       "SUM", "Sum", //
       "SUM_OF_SQUARES", "Sum of squares", //
       "HISTOGRAM", "Histogram", //
       "DENSITY", "Density",//
       "NORMAL_PROB", "Normal Probability",//
       "THEORETHICAL_QUANTILES", "Theroretical Quantiles",//
       "SAMPLE_QUANTILES", "Sample Quantiles",//
       "MODE", "Mode",//
       "CATEGORY", "Category",//
       "FREQUENCY", "Frequency"//
 
   })
   Map<String, String> statsMap();
 
   @Description("JVM Labels")
   @DefaultStringMapValue({ "OPAL_VERSION", "Opal Version",//
       "JAVA_VERSION", "Java Version", //
       "VM_NAME", "VM Name", //
       "VM_VENDOR", "VM Vendor", //
       "VM_VERSION", "VM Version", //
       "MEM_HEAP", "Memory Heap", //
       "MEGABYTES", "MegaBytes (Mb)", //
       "COMMITTED", "Committed", //
       "USED", "Used", //
       "MEM_NON_HEAP", "Memory Non-Heap", //
       "THREADS", "Threads", //
       "COUNT", "Count", //
       "PEAK", "Peak", //
       "CURRENT", "Current", //
       "GC_DELTA", "Garbage Collectors (delta)", //
       "DELTA", "Delta", //
       "TIME_MS", "Time (ms)", //
       "GC_COUNT", "GC Count" //
   })
   Map<String, String> jvmMap();
 
   @Description("System Configuration page title")
   @DefaultStringValue("Configuration")
   String pageServerConfigurationTitle();
 
   @Description("Select languages label")
   @DefaultStringValue("Select languages...")
   String selectLanguages();
 
   @Description("Locale Labels")
   @DefaultStringMapValue({//
       "ar", "Arabic",//
       "be", "Belarusian", //
       "bg", "Bulgarian", //
       "ca", "Catalan", //
       "cs", "Czech", //
       "da", "Danish", //
       "de", "German", //
       "el", "Greek", //
       "en", "English", //
       "es", "Spanish", //
       "et", "Estonian", //
       "fi", "Finnish", //
       "fr", "French", //
       "ga", "Irish", //
       "hi", "Hindi", //
       "hr", "Croatian", //
       "hu", "Hungarian", //
       "in", "Indonesian", //
       "is", "Icelandic", //
       "it", "Italian", //
       "iw", "Hebrew", //
       "ja", "Japanese", //
       "ko", "Korean", //
       "lt", "Lithuanian", //
       "lv", "Latvian", //
       "mk", "Macedonian", //
       "ms", "Malay", //
       "mt", "Maltese", //
       "nl", "Dutch", //
       "no", "Norwegian", //
       "pl", "Polish", //
       "pt", "Portuguese", //
       "ro", "Romanian", //
       "ru", "Russian", //
       "sk", "Slovak", //
       "sl", "Slovenian", //
       "sq", "Albanian", //
       "sr", "Serbian", //
       "sv", "Swedish", //
       "th", "Thai", //
       "tr", "Turkish", //
       "uk", "Ukrainian", //
       "vi", "Vietnamese", //
       "zh", "Chinese"//
   })
   Map<String, String> localeMap();
 
   @Description("Table References label")
   @DefaultStringValue("Table References")
   String tableReferencesLabel();
 
   @Description("No Tables label")
   @DefaultStringValue("No tables")
   String noTablesLabel();
 
   @Description("No Variables label")
   @DefaultStringValue("No variables")
   String noVariablesLabel();
 
   @Description("No Entities label")
   @DefaultStringValue("No entities")
   String noEntitiesLabel();
 
   @Description("Edit categories label")
   @DefaultStringValue("Edit categories")
   String editCategories();
 
   @Description("Edit <Variable> categories label")
   @DefaultStringValue("Edit {0} categories")
   String editVariableCategories();
 
   @Description("Edit properties label")
   @DefaultStringValue("Edit properties")
   String editProperties();
 
   @Description("Edit <Variable> properties label")
   @DefaultStringValue("Edit {0} properties")
   String editVariableProperties();
 
   @Description("Table count label")
   @DefaultStringValue("1 table")
   String tableCountLabel();
 
   @Description("Variable count label")
   @DefaultStringValue("1 variable")
   String variableCountLabel();
 
   @Description("Entity count label")
   @DefaultStringValue("1 entity")
   String entityCountLabel();
 
   @Description("Tables count label")
   @DefaultStringValue("{0} tables")
   String tablesCountLabel();
 
   @Description("Variables count label")
   @DefaultStringValue("{0} variables")
   String variablesCountLabel();
 
   @Description("Entities count label")
   @DefaultStringValue("{0} entities")
   String entitiesCountLabel();
 
   @Description("Last Update Ago label")
   @DefaultStringValue("Last update {0}")
   String lastUpdateAgoLabel();
 
   @Description("Variables count property label")
   @DefaultStringValue("Variables Count")
   String variablesCountProperty();
 
   @Description("Entities count property label")
   @DefaultStringValue("Entities Count")
   String entitiesCountProperty();
 
   @Description("Report templates header")
   @DefaultStringValue("{0} Reports")
   String reportTemplatesHeader();
 
   @Description("Import label")
   @DefaultStringValue("Import")
   String importLabel();
 
   @Description("Storage label")
   @DefaultStringValue("Storage")
   String storageLabel();
 
   @Description("Export label")
   @DefaultStringValue("Export")
   String exportLabel();
 
   @Description("Opal SQL label")
   @DefaultStringValue("Opal SQL")
   String hibernateDatasourceLabel();
 
   @Description("Custom SQL label")
   @DefaultStringValue("Custom SQL")
   String jdbcDatasourceLabel();
 
   @Description("Update {0} categories label")
   @DefaultStringValue("Update {0} categories")
   String updateVariableCategories();
 
   @Description("Update {0} properties label")
   @DefaultStringValue("Update {0} properties")
   String updateVariableProperties();
 
   @Description("Moment with ago label")
   @DefaultStringValue("{0} ({1})")
   String momentWithAgo();
 
   @Description("Required label")
   @DefaultStringValue("Required")
   String required();
 
   @Description("Authentication Failed message")
   @DefaultStringValue("Authentication failed")
   String authFailed();
 
   @Description("Default storage label")
   @DefaultStringValue("Default storage")
   String defaultStorage();
 
   @Description("Select database label")
   @DefaultStringValue("Select database...")
   String selectDatabase();
 
   @Description("None label")
   @DefaultStringValue("None")
   String none();
 }
