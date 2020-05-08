 package com.atlassian.sal.jira.license;
 
 import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
 import com.atlassian.jira.util.I18nHelper;
 import com.atlassian.sal.api.license.LicenseHandler;
 
 import java.util.Locale;
 
 /**
  * Jira implementation of license handler
  */
 public class JiraLicenseHandler implements LicenseHandler
 {
    private JiraLicenseUpdaterService jiraLicenseUpdaterService;
     private I18nHelper.BeanFactory i18nBeanFactory;
 
    public JiraLicenseHandler(JiraLicenseUpdaterService jiraLicenseUpdaterService, I18nHelper.BeanFactory i18nBeanFactory)
     {
        this.jiraLicenseUpdaterService = jiraLicenseUpdaterService;
         this.i18nBeanFactory = i18nBeanFactory;
     }
     /**
      * Sets the license, going through the regular validation steps as if you used the web UI
      *
      * @param license The license string
      */
     public void setLicense(String license)
     {
        JiraLicenseService.ValidationResult validationResult = jiraLicenseUpdaterService.validate(i18nBeanFactory.getInstance(Locale.getDefault()), license);
         if (validationResult.getErrorCollection().hasAnyErrors())
         {
             throw new IllegalArgumentException("Specified license was invalid.");
         }
        jiraLicenseUpdaterService.setLicense(validationResult);
     }
 }
