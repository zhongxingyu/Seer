 package com.atlassian.sal.crowd.license;
 
 import com.atlassian.sal.api.license.LicenseHandler;
 import com.atlassian.crowd.manager.license.CrowdLicenseManager;
 
 /**
  * Crowd license handler that stores the license
  *
 * @since 2.0.0
  */
 public class CrowdLicenseHandler implements LicenseHandler
 {
     private final CrowdLicenseManager licenseManager;
 
     public CrowdLicenseHandler(CrowdLicenseManager licenseManager)
     {
         this.licenseManager = licenseManager;
     }
 
     public void setLicense(String license)
     {
         licenseManager.storeLicense(license);
     }
 }
