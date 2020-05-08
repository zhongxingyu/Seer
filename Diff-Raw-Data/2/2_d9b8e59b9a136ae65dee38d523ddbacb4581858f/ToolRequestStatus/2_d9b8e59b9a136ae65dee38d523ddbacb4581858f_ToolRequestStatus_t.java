 package org.iplantc.de.client.notifications.models.payload;
 
 import org.iplantc.de.client.I18N;
 
 /**
  * Tool Request Status values with associated help text.
  * 
  * @author psarando
  * 
  */
 public enum ToolRequestStatus {
     Submitted(I18N.HELP.toolRequestStatusSubmittedHelp()),
     Pending(I18N.HELP.toolRequestStatusPendingHelp()),
     Evaluation(I18N.HELP.toolRequestStatusEvaluationHelp()),
     Installation(I18N.HELP.toolRequestStatusInstallationHelp()),
     Validation(I18N.HELP.toolRequestStatusValidationHelp()),
    Completion(I18N.HELP.toolRequestStatusCompleteHelp()),
     Failed(I18N.HELP.toolRequestStatusFailedHelp());
 
     private String helpText;
 
     ToolRequestStatus(String helpText) {
         this.helpText = helpText;
     }
 
     public String getHelpText() {
         return helpText;
     }
 }
