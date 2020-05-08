 package org.iplantc.de.client;
 
 import org.iplantc.core.uicommons.client.CommonUIDisplayStrings;
 
 /**
  * Constants used by the client as display text.
  * 
  * All values present here are subject to translation for internationalization.
  */
 public interface DEDisplayStrings extends CommonUIDisplayStrings {
 
     /**
      * Message shown when a file successfully uploads.
      * 
      * @return a string representing the localized text.
      */
     String fileUploadSuccess(String filename);
 
     /**
      * Caption to file upload status messages.
      * 
      * @return a string representing the localized text.
      */
     String fileUpload();
 
     /**
      * Formats a message indicating the path of the folder receiving the files on upload.
      * 
      * @param folderPath the path to the folder
      * 
      * @return the formatted message
      */
     String fileUploadFolder(String folderPath);
 
     /**
      * File upload maximum size message.
      * 
      * @return a string representing the localized text.
      */
     String fileUploadMaxSizeWarning();
 
     /**
      * Localized display text for download buttons.
      * 
      * @return a string representing the localized text.
      */
     String download();
 
     /**
      * Localized display text for displaying the files in the status bar.
      * 
      * @return a string representing the localized text.
      */
     String files();
 
     /**
      * Localized display text for displaying a folders label.
      * 
      * @return a string representing the localized text.
      */
     String folders();
 
     /**
      * Localized display text for caption of folder creation dialog.
      * 
      * @return a string representing the localized text.
      */
     String newFolder();
 
     /**
      * Localized display text for displaying "Folder Name".
      * 
      * @return a string representing the localized text.
      */
     String folderName();
 
     /**
      * Localized display text used for the 'Uploaded' column in the data browser grid.
      * 
      * @return a string representing the localized text.
      */
     String uploaded();
 
     /**
      * Localized display text used as a caption in the Job Launch dialog.
      * 
      * @return a string representing the localized text.
      */
     String jobname();
 
     /**
      * Localized display text used for both the My Data window caption and shortcut.
      * 
      * @return a string representing the localized text.
      */
     String myData();
 
     /**
      * Localized display text used anywhere 'Import' is needed.
      * 
      * @return a string representing the localized text.
      */
     String tagImport();
 
     /**
      * Localized display text used for displaying 'Save Description'.
      * 
      * @return a string representing the localized text.
      */
     String saveDescription();
 
     /**
      * Localized display text used as a tab caption in the viewer.
      * 
      * @return a string representing the localized text.
      */
     String raw();
 
     /**
      * Localized display text used as an error in Independent Contrast.
      * 
      * @return a string representing the localized text.
      */
     String getListOfTreesError();
 
     /**
      * Localized display text used as a warning in data management.
      * 
      * @return a string representing the localized text.
      */
     String folderDeleteWarning();
 
     /**
      * Localized text for display when asking for search information from the user.
      * 
      * @return a string representing the localized text.
      */
     String search();
 
     /**
      * Localized text for display as the title of the web application.
      * 
      * In a raw HTML perspective, this text is displayed inside the HTML title element in the head of the
      * document.
      * 
      * @return a string representing the localized text.
      */
     String rootApplicationTitle();
 
     /**
      * Localized text for display as the title for a delete files confirmation dialog.
      * 
      * @return a string representing the localized text.
      */
     String deleteFilesTitle();
 
     /**
      * Localized text for display as a confirmation message when user's delete files.
      * 
      * @return a string representing the localized text.
      */
     String deleteFilesMsg();
 
     /**
      * Localized text for display as button text for the execution of a job.
      * 
      * @return a string representing the localized text.
      */
     String launchJob();
 
     /**
      * Localized text for display as button text for viewing data or results.
      * 
      * @return a string representing the localized text.
      */
     String view();
 
     /**
      * Localized text for display in a file selection dialog as a field label.
      * 
      * @return a string representing the localized text.
      */
     String selectedFile();
 
     /**
      * Localized text for display in a folder selection dialog as a field label.
      * 
      * @return a string representing the localized text.
      */
     String selectedFolder();
 
     /**
      * Localized text displayed as the default name in the job name textfield.
      * 
      * @return a string representing the localized text.
      */
     String defaultJobName();
 
     /**
      * Localized text for display when a user is about to close a window that contained changes that have
      * not been saved.
      * 
      * The word 'dirty' refers to changes appearing that have not been persisting.
      * 
      * @return a string representing the localized text.
      */
     String closeDirtyWindow();
 
     /**
      * Localized text for display to provide context for import of data from a URL.
      * 
      * @return a string representing the localized text.
      */
     String urlImport();
 
     /**
      * Localized text for display as button text for the import operation.
      * 
      * @return a string representing the localized text.
      */
     String importLabel();
 
     /**
      * Localized text for display as a label for the URL field in an import form.
      * 
      * @return a string representing the localized text.
      */
     String urlPrompt();
 
     /**
      * Localized text for display as a label in a URL import form.
      * 
      * This text is also used as an identifier for that form field.
      * 
      * @return a string representing the localized text.
      */
     String url();
 
     /**
      * Localized text for display as a tab heading for a user-provided file description.
      * 
      * @return a string representing the localized text.
      */
     String fileDescription();
 
     /**
      * Localized text that provides a description on how account activation and password reset operations
      * will be handled..
      * 
      * @return a string representing the localized text.
      */
     String emailInfo();
 
     /**
      * Localized text for display as a label for the newsletter checkbox.
      * 
      * @return a string representing the localized text.
      */
     String newsLetterSubscribe();
 
     /**
      * Localized text for display as a hyperlink for user's preferences.
      * 
      * @return a string representing the localized text.
      */
     String preferences();
 
     /**
      * Localized text for display when launch of a job is successful.
      * 
      * @return a string representing the localized text.
      */
     String launchSuccess();
 
     /**
      * Localized display text as a busy status message when importing a file.
      * 
      * @return a string representing the localized text.
      */
     String fileImportStatus();
 
     /**
      * Localized display text for a stop button that allows a user to stop a job.
      * 
      * This text appears in the toolbar of the job status grid.
      * 
      * @return a string representing the localized text.
      */
     String stop();
 
     /**
      * Localized display text for a message shown when a user preference is successfully saved.
      * 
      * @return a string representing the localized text.
      */
     String userPrefSaveSuccess();
 
     /**
      * Localized display text for a message shown with a user makes a request import file data from a
      * URL.
      * 
      * @return a string representing the localized text.
      */
     String importRequestSubmit(String filename);
 
     /**
      * Caption to My Notifications window.
      * 
      * @return a string representing the localized text.
      */
     String myNotifications();
 
     /**
      * Caption use as the header for About Discovery Environment window.
      * 
      * @return a string representing the localized text.
      */
     String aboutDiscoveryEnvironment();
 
     /**
      * Localized display text for when there are no notifications to display.
      * 
      * @return a string representing the localized text.
      */
     String noNotifications();
 
     /**
      * Localized display text for column header within notification grid.
      * 
      * @return a string representing the localized text.
      */
     String messagesGridHeader();
 
     /**
      * Localized display text for column header within notification grid.
      * 
      * @return a string representing the localized text.
      */
     String createdDateGridHeader();
 
     /**
      * Text used as the title bar heading for the User Manual documentation window.
      * 
      * @return a string representing the localized text.
      */
     String userManualHeader();
 
     /**
      * Localized text to display when a job is launching but RPC has not returned.
      * 
      * @return a string representing the localized text.
      */
     String launchingJob();
 
     /**
      * The title of the logout message window.
      * 
      * @return a string representing the localized text.
      */
     String logoutMessageTitle();
 
     /**
      * The text message to display when users are logging out.
      * 
      * @param logoutUrl the URL to redirect the browser to when logging out.
      * @return a string representing the localized text.
      */
     String logoutMessageText(String logoutUrl);
 
     /**
      * The file type selection field in File Upload dialog
      * 
      * @return a string representing the localized text.
      */
     String selectFileType();
 
     /**
      * Display text for select items menu item.
      * 
      * @return a string representing the text.
      */
     String selectItems();
 
     /**
      * Display text for my analysis panel
      * 
      * @return a string representing the text.
      */
     String analysisOverview();
 
     /**
      * Display text for create the "create" button
      * 
      * @return a string representing the text.
      */
     String create();
 
     /**
      * Display text for Run
      * 
      * @return a string representing the text.
      */
     String run();
 
     /**
      * Display text for view output
      * 
      * @return a string representing the text.
      */
     String viewOutput();
 
     /**
      * Display text for warning message when a user tries to delete Analyses.
      * 
      * 
      * 
      * @return a String representing the text.
      */
     String analysesExecDeleteWarning();
 
     /**
      * Display text for the warning message when a user tries to cancel a running job.
      * 
      * @return a String representing the text
      */
     String analysesCancelJobWarning();
 
     /**
      * Display text for remove.
      * 
      * @return a String representing the text.
      */
     String remove();
 
     /**
      * Display text for start date.
      * 
      * @return a String representing the text.
      */
     String startDate();
 
     /**
      * Display text for end date.
      * 
      * @return a String representing the text.
      */
     String endDate();
 
     /**
      * The text to display when we're unable to retrieve the list of analyses for the user.
      * 
      * @return a string containing the localized text.
      */
     String analysesRetrievalFailure();
 
     /**
      * Localized display text used as a tab caption in the viewer.
      * 
      * @return a string representing the localized text.
      */
     String image();
 
     /**
      * Localized display text used as a tab caption in the viewer.
      * 
      * @return a string representing the localized text.
      */
     String pdf();
 
     /**
      * Localized text for display as link text for viewing tree data.
      * 
      * @return a string representing the localized text.
      */
     String viewTreeViewer();
 
     /**
      * Localized text for display as a heading for tree URLs.
      * 
      * @return a string representing the localized text.
      */
     String treeUrl();
 
     /**
      * Localized text for display as a heading for the file preview tab.
      * 
      * @return a string representing the localized text.
      */
     String filePreviewNotice();
 
     /**
      * Localized display text for grid headers that require a size column.
      * 
      * @return a string representing the localized text.
      */
     String size();
 
     /**
      * Localized display text for grid headers that require a last modified column.
      * 
      * @return a string representing the localized text.
      */
     String lastModified();
 
     String catalog();
 
     String up();
 
     String idParentInvalid();
 
     /**
      * Localized display text for a size measured in bytes.
      * 
      * @param n number of bytes
      * @return a string representing the localized text.
      */
     String nBytes(String n);
 
     /**
      * Localized display text for a size measured in KBytes.
      * 
      * @param n number of KBytes
      * @return a string representing the localized text.
      */
     String nKilobytes(String n);
 
     /**
      * Localized display text for a size measured in MBytes.
      * 
      * @param n number of MBytes
      * @return a string representing the localized text.
      */
     String nMegabytes(String n);
 
     /**
      * Localized display text for a size measured in GBytes.
      * 
      * @param n number of GBytes
      * @return a string representing the localized text.
      */
     String nGigabytes(String n);
 
     /**
      * Localized display text for the heading on the Data Navigation Panel.
      * 
      * @return a string representing the localized text.
      */
     String navigation();
 
     /**
      * Localized display text for the "from data source" menu item.
      * 
      * @return a string representing the localized text.
      */
     String fromDataSource();
 
     /**
      * Localized display text for the resource selection dialog.
      * 
      * @return a string representing the localized text.
      */
     String selectedResource();
 
     /**
      * Localized display text for the DE catalog
      * 
      * @return a string representing the localized text.
      */
     String deCatalog();
 
     /**
      * Localized display text for the DE analysis
      * 
      * @return a string representing the localized text.
      */
     String analyses();
 
     /**
      * Localized display text for create new Analysis
      * 
      * @return a string representing the localized text.
      */
     String createNewAnalysis();
 
     /**
      * Localized display text for create new Workflow
      * 
      * @return a string representing the localized text.
      */
     String createNewWorkflow();
 
     /**
      * Localized text for notifications
      * 
      * @return string representing the text
      */
     String notifications();
 
     /**
      * Localized text for data
      * 
      * @return string representing the text
      */
     String data();
 
     /**
      * Localized text for analysis
      * 
      * @return string representing the text
      */
     String analysis();
 
     /**
      * Localized text for applications
      * 
      * @return string representing the text
      */
     String applications();
 
     /**
      * Localized text for applications
      * 
      * @return string representing the text
      */
     String close();
 
     /**
      * Localized text for applications
      * 
      * @return string representing the text
      */
     String maximize();
 
     /**
      * Localized text for applications
      * 
      * @return string representing the text
      */
     String minimize();
 
     /**
      * Localized text for applications
      * 
      * @return string representing the text
      */
     String restore();
 
     /**
      * Localized text for applications
      * 
      * @return string representing the text
      */
     String show();
 
     /**
      * Localized text for applications
      * 
      * @return string representing the text
      */
     String hide();
 
     /**
      * Localized text for all
      * 
      * @return string representing the text
      */
     String all();
 
     /**
      * Localized text for mark as Favourite
      * 
      * @return string representing the text
      */
     String markFav();
 
     /**
      * Localized text for remove from favourites
      * 
      * @return string representing the text
      */
     String removeFav();
 
     /**
      * Localized text for delete rating
      * 
      * @return string representing the text
      */
     String unrate();
 
     /**
      * localized text for enabling debug for jobs
      * 
      * @return string representing the text
      */
     String debug();
 
     /**
      * Localized display text for a "Detailed View" label.
      * 
      * @return a string representing the localized text.
      */
     String detailView();
 
     /**
      * Localized display text for a "Basic View" label.
      * 
      * @return a string representing the localized text.
      */
     String basicView();
 
     /**
      * Localized error message for invalid import url
      * 
      * @return string representing the text
      */
     String invalidImportUrl();
 
     /**
      * Localized text for permission error message dialog title
      * 
      * @return string representing the text
      */
     String permissionErrorTitle();
 
     /**
      * Localized text for permission error message
      * 
      * @return string representing the text
      */
     String permissionErrorMessage();
 
     /**
      * Localized text for permission
      * 
      * @return string representing the text
      */
     String permissions();
 
     /**
      * Localized text for read only permission
      * 
      * @return string representing the text
      */
     String readOnly();
 
     /**
      * Localized text for read write permission
      * 
      * @return string representing the text
      */
     String readWrite();
 
     /**
      * Localized text for email job notifications
      * 
      * @return string representing the text
      */
     String notifyemail();
 
     /**
      * Localized text for app name label.
      * 
      * @return a string representing the localized text.
      */
     String appName();
 
     /**
      * Localized text for analyses filter prompt.
      * 
      * @return a string representing the localized text.
      */
     String filterAnalysesList();
 
     /**
      * Localized text for analyses grid tool tip
      * 
      * @return a string representing the localized text.
      */
     String analysesGridToolTip();
 
     /**
      * Localized text for analyses delete message
      * 
      * @return a string representing the localized text.
      */
     String analysesNotDeleted();
 
     /**
      * Localized text for app delete warning
      * 
      * @return a string representing the localized text.
      */
     String appDeleteWarning();
 
     /**
      * Localized text for Data Management window Drag-n-Drop feedback text: "X" items selected.
      * 
      * @return a string representing the localized text.
      */
     String dataDragDropStatusText();
 
     /**
      * Localized text for java error
      * 
      * @return a string representing the localized text.
      */
     String javaError();
 
     /**
      * Localized text for the Simple Upload Form button launcher.
      * 
      * @return a string representing the localized text.
      */
     String simpleUploadForm();
 
     /**
      * Localized text for the Simple Download Panel button launcher.
      * 
      * @return a string representing the localized text.
      */
     String simpleDownloadForm();
 
     /**
      * Localized text for manage data metadata menu item
      * 
      * @return a string representing the localized text.
      */
     String metadata();
 
     /**
      * Localized text for the iDrop Lite Applet window close-confirmation-prompt title.
      * 
      * @return a string representing the localized text.
      */
     String idropLiteCloseConfirmTitle();
 
     /**
      * Localized text for the iDrop Lite Applet window close-confirmation-prompt message.
      * 
      * @return a string representing the localized text.
      */
     String idropLiteCloseConfirmMessage();
 
     /**
      * Localized text for the iDrop Lite Applet window download mode notice.
      * 
      * @return a string representing the localized text.
      */
     String idropLiteDownloadNotice();
 
     /**
      * Localized text for the App Comment dialog title
      * 
      * @return a string representing the localized text.
      */
     String appCommentDialogTitle();
 
     /**
      * Localized text for the text appearing the App Comment dialog before the text area.
      * 
      * @param appName name of the app
      * @return a string representing the localized text.
      */
     String appCommentExplanation(String appName);
 
     /**
      * Localized text for job output destination folder selection
      * 
      * @param defaultPath default output folder for analyses
      * 
      * @return a string representing the localized text.
      */
     String selectJobOutputDir(String defaultPath);
 
     /**
      * Localized text for analysis submit confirmation dialog
      * 
      * @return a string representing the localized text.
      */
     String analysisSubmitted();
 
     /**
      * Localized text for analysis submitted message
      * 
      * 
      * @return a string representing the localized text.
      */
     String analysisSubmittedMsg();
 
     /**
      * Localized text for view parameters panel
      * 
      * @param analysisName name of the analysis
      * 
      * @return a string representing the localized text.
      */
     String viewParameters(String analysisName);
 
     /**
      * Localized text for param name
      * 
      * 
      * @return a string representing the localized text.
      */
     String paramName();
 
     /**
      * Localized text for param type
      * 
      * 
      * @return a string representing the localized text.
      */
     String paramType();
 
     /**
      * Localized text for param value
      * 
      * @return a string representing the localized text.
      */
     String paramValue();
 
     /**
      * Localized text for the subject line in rating notifications.
      * 
      * @param appName the name of the app
      * @return a string representing the localized text.
      */
     String ratingEmailSubject(String appName);
 
     /**
      * Localized text for the email body in rating notifications.
      * 
      * @param appName the name of the app
      * @return a string representing the localized text.
      */
     String ratingEmailText(String appName);
 
     /**
      * Localized text empty parameter grid
      * 
      * 
      * @return a string representing the localized text.
      */
     String noParameters();
 
     /**
      * Localized text for view parameters button
      * 
      * 
      * @return a string representing the localized text.
      */
     String viewParamLbl();
 
     /**
      * Localized text for form reset
      * 
      * 
      * @return a string representing the localized text.
      */
     String reset();
 
     /**
      * Localized text for instructions for viewing folder contents.
      * 
      * @return a string representing the localized text.
      */
     String selectFolderToViewContents();
 
     /**
      * Localized text for a UI element that lets the user switch text wrapping on or off.
      * 
      * @return a string representing the localized text.
      */
     String wrap();
 
     /**
      * Localized text for a dialog asking the user if they want to potentially override data in the
      * output folder.
      * 
      * @return a string representing the localized text.
      */
     String confirmOutputFolder();
 
     /**
      * Localized text for a "Create Apps" title.
      * 
      * @return a string representing the localized text.
      */
     String createApps();
 
     /**
      * Localized text for unsaved tito work warning
      * 
      * @return a string representing the localized text.
      */
 
     String navigateWarning();
 
     /**
      * Localized text for a UI button that lets user to create workflow
      * 
      * @return a string representing the localized text.
      */
     String pipeline();
 
     /**
      * Localized text for a user state saving message.
      * 
      * @return a string representing the localized text.
      */
     String savingSession();
 
     /**
      * Localized text for a user state saving wait message.
      * 
      * @return a string representing the localized text.
      */
     String savingSessionWaitNotice();
 
     /**
      * Localized text for a user state loading message.
      * 
      * @return a string representing the localized text.
      */
     String loadingSession();
 
     /**
      * Localized text for a user state loading wait message.
      * 
      * @return a string representing the localized text.
      */
     String loadingSessionWaitNotice();
 
     /**
      * Localized display text for bulk download actions.
      * 
      * @return a string representing the localized text.
      */
     String bulkDownload();
 
     /**
      * Localized display text for simple download actions.
      * 
      * @return a string representing the localized text.
      */
     String simpleDownload();
 
     /**
      * Localized display text for simple download window instructions.
      * 
      * @return a string representing the localized text.
      */
     String simpleDownloadNotice();
 
     /**
      * Localized display text for prompting user for remember file selector path
      * 
      * @return a string representing the localized text.
      */
     String rememberFileSectorPath();
 
     /**
      * Localized display text for a bulk upload label.
      * 
      * @return a string representing the localized text.
      */
     String bulkUploadFromDesktop();
 
     /**
      * Localized display text for a simple upload label.
      * 
      * @return a string representing the localized text.
      */
     String simpleUploadFromDesktop();
 
     /**
      * Localized display text for share
      * 
      * @return a string representing the localized text.
      */
     String share();
 
     /**
      * Localized display text for file owner
      * 
      * @return a string representing the localized text.
      */
     String owner();
 
     /**
      * Localized display text for settings
      * 
      * @return a string representing the localized text.
      */
     String settings();
 
     /**
      * Localized display text for collaborators
      * 
      * @return a string representing the localized text.
      */
     String collaborators();
 
     /**
      * Localized display text for search collaborators
      * 
      * @return a string representing the localized text.
      */
     String collabSearchPrompt();
 
     /**
      * Localized display text for searching status
      * 
      * @return a string representing the localized text.
      */
     String searching();
 
     /**
      * Localized display text for no collaborators
      * 
      * @return a string representing the localized text.
      */
 
     String noCollaborators();
 
     /**
      * Localized display text for showing current collaborators list
      * 
      * @return a string representing the localized text.
      */
     String currentCollabList();
 
     /**
      * Localized display text for 'done'
      * 
      * @return a string representing the localized text.
      */
     String done();
 
     /**
      * Localized display text for sharing grid
      * 
      * @return a string representing the localized text.
      */
     String sharePanelEmptyText();
 
     /**
      * Localized display text for unshare
      * 
      * @return a string representing the localized text.
      */
     String unshare();
 
     /**
      * Localized display text for read
      * 
      * @return a string representing the localized text.
      */
     String read();
 
     /**
      * Localized display text for write
      * 
      * @return a string representing the localized text.
      */
     String write();
 
     /**
      * Localized display text for own
      * 
      * @return a string representing the localized text.
      */
     String own();
 
     /**
      * Localized display text for unshare prompt
      * 
      * @return a string representing the localized text.
      */
     String unsharePrompt(String name);
 
     /**
      * Localized display text search collaborators
      * 
      * @return a string representing the localized text.
      */
     String searchCollab();
 
     /**
      * Display text for the actions panel.
      * 
      * @return a String representing the text.
      */
     String actions();
 
     /**
      * The display text for the view raw menu item.
      * 
      * @return the localized display text
      */
     String viewRaw();
 
     /**
      * The display text for current collaborators
      * 
      * @return the localized display text
      */
     String currentCollaborators();
 
     /**
      * save session prompt
      * 
      * @return the localized display text
      */
     String saveSession();
 
     /**
      * default job output folder
      * 
      * @return the localized display text
      */
     String defaultOutputFolder();
 
     /**
      * enabled prompt
      * 
      * @return the localized display text
      */
     String enabled();
 
     /**
      * disabled prompt
      * 
      * @return the localized display text
      */
     String disabled();
 
     /**
      * 
      * @return
      */
     String collaboratorRemoved();
 
     /**
      * 
      * @return
      */
     String collaboratorAdded();
 
     /**
      * 
      * @return
      */
     String collaboratorAddConfirm(String name);
 
     /**
      * 
      * @return
      */
     String collaboratorRemoveConfirm(String name);
 
     /**
      * The display text for a View Deployed Components label.
      * 
      * @return the localized display text
      */
     String viewDeployedComponents();
 
     /**
      * 
      * @return
      */
     String noNewNotifications();
 
     /**
      * 
      * @return
      */
     String allNotifications();
 }
