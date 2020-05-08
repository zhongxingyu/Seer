 /*
  * Created on Jan 27, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.vfny.geoserver.form.global;
 
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.vfny.geoserver.config.ContactConfig;
 import org.vfny.geoserver.config.GlobalConfig;
 import org.vfny.geoserver.global.dto.GeoServerDTO.Defaults;

 import java.util.logging.Level;
 import javax.servlet.http.HttpServletRequest;
 
 
 /**
  * GeoServerConfigurationForm purpose.
  * <p>
  * Description of GeoServerConfigurationForm ...
  * </p>
  *
  * <p>
  * Capabilities:
  * </p>
  * <ul>
  * <li>
  * Feature: description
  * </li>
  * </ul>
  * <p>
  * Example Use:
  * </p>
  * <pre><code>
  * GeoServerConfigurationForm x = new GeoServerConfigurationForm(...);
  * </code></pre>
  *
  * @author User, Refractions Research, Inc.
  * @author $Author: cholmesny $ (last modification)
  * @version $Id$
  */
 public class GeoServerConfigurationForm extends ActionForm {
     private int maxFeatures;
     private boolean verbose;
     private int numDecimals;
     private String charset;
     private String proxyBaseUrl;
     private String schemaBaseURL;
     private String loggingLevel;
     private String adminUserName;
     private String adminPassword;
     private boolean verboseExceptions;
 
     /** The name of the contact person */
     private String contactPerson;
 
     /** The name of the organization with which the contact is affiliated. */
     private String contactOrganization;
 
     /** The position of the contact within their organization. */
     private String contactPosition;
 
     /** The type of address specified, such as postal. */
     private String addressType;
 
     /** The actual street address. */
     private String address;
 
     /** The city of the address. */
     private String addressCity;
 
     /** The state/prov. of the address. */
     private String addressState;
 
     /** The postal code for the address. */
     private String addressPostalCode;
 
     /** The country of the address. */
     private String addressCountry;
 
     /** The contact phone number. */
     private String contactVoice;
 
     /** The contact Fax number. */
     private String contactFacsimile;
 
     /** The contact email address. */
     private String contactEmail;
     private boolean verboseChecked;
     private boolean verboseExceptionsChecked;
 
     /** log to disk ? **/
     private boolean loggingToFile;
     private boolean loggingToFileChecked;
     private String logLocation;
     private double jaiMemoryCapacity;
     private double jaiMemoryThreshold;
     private int jaiTileThreads;
     private int jaiTilePriority;
     private boolean jaiRecycling;
     private boolean jaiRecyclingChecked;
     private boolean imageIOCache;
     private boolean imageIOCacheChecked;
     private boolean jaiJPEGNative;
     private boolean jaiJPEGNativeChecked;
     private boolean jaiPNGNative;
     private boolean jaiPNGNativeChecked;
 
     public void reset(ActionMapping arg0, HttpServletRequest request) {
         super.reset(arg0, request);
 
         GlobalConfig globalConfig = (GlobalConfig) getServlet().getServletContext()
                                                        .getAttribute(GlobalConfig.CONFIG_KEY);
 
         maxFeatures = globalConfig.getMaxFeatures();
         verbose = globalConfig.isVerbose();
         verboseExceptions = globalConfig.isVerboseExceptions();
         verboseChecked = false;
         verboseExceptionsChecked = false;
         numDecimals = globalConfig.getNumDecimals();
         charset = globalConfig.getCharSet().name();
         proxyBaseUrl = globalConfig.getProxyBaseUrl();
         schemaBaseURL = globalConfig.getSchemaBaseUrl();
         adminUserName = globalConfig.getAdminUserName();
         adminPassword = globalConfig.getAdminPassword();
 
         if (globalConfig.getLoggingLevel() == null) {
             //@TODO - shouldn't have to do this.. should never return null
             loggingLevel = Level.OFF.getName();
         } else {
             loggingLevel = globalConfig.getLoggingLevel().getName();
         }
 
         loggingToFile = globalConfig.getLoggingToFile();
         loggingToFileChecked = false;
         logLocation = globalConfig.getLogLocation();
 
         jaiMemoryCapacity = globalConfig.getJaiMemoryCapacity();
         jaiMemoryThreshold = globalConfig.getJaiMemoryThreshold();
         jaiTileThreads = globalConfig.getJaiTileThreads();
         jaiTilePriority = globalConfig.getJaiTilePriority();
         jaiRecycling = globalConfig.isJaiRecycling();
         jaiRecyclingChecked = false;
         imageIOCache = globalConfig.isImageIOCache();
         imageIOCacheChecked = false;
         jaiJPEGNative = globalConfig.isJaiJPEGNative();
         jaiJPEGNativeChecked = false;
         jaiPNGNative = globalConfig.isJaiPNGNative();
         jaiPNGNativeChecked = false;
 
         ContactConfig contactConfig = globalConfig.getContact();
         contactPerson = contactConfig.getContactPerson();
         contactOrganization = contactConfig.getContactOrganization();
         contactPosition = contactConfig.getContactPosition();
 
         addressType = contactConfig.getAddressType();
         address = contactConfig.getAddress();
         addressCity = contactConfig.getAddressCity();
         addressCountry = contactConfig.getAddressCountry();
         addressPostalCode = contactConfig.getAddressPostalCode();
         addressState = contactConfig.getAddressState();
 
         contactVoice = contactConfig.getContactVoice();
         contactFacsimile = contactConfig.getContactFacsimile();
         contactEmail = contactConfig.getContactEmail();
     }
 
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         ActionErrors errors = new ActionErrors();
 
         final long maxMemoryAvailable = Runtime.getRuntime().maxMemory() - (4 * 1024 * 1024);
         if ((jaiMemoryCapacity < 0) || (jaiMemoryCapacity > Defaults.JaiMemoryCapacity)) {
             errors.add("jaiMemCapacity",
                new ActionError("error.geoserver.JAIMemCapacity", Defaults.JaiMemoryCapacity));
         }
 
         if ((jaiMemoryThreshold < 0.0) || (jaiMemoryThreshold > 1.0)) {
             errors.add("jaiMemThreshold", new ActionError("error.geoserver.JAIMemThreshold"));
         }
 
         if ((jaiTileThreads < 0) || (jaiTileThreads > 100)) {
             errors.add("jaiTileThreads", new ActionError("error.geoserver.JAITileThreads"));
         }
 
         if ((jaiTilePriority < 1) || (jaiTilePriority > 10)) {
             errors.add("jaiTilePriority", new ActionError("error.geoserver.JAITilePriority"));
         }
 
         return errors;
     }
 
     /**
      * Access maxFeatures property.
      *
      * @return Returns the maxFeatures.
      */
     public int getMaxFeatures() {
         return maxFeatures;
     }
 
     /**
      * Set maxFeatures to maxFeatures.
      *
      * @param maxFeatures The maxFeatures to set.
      */
     public void setMaxFeatures(int maxFeatures) {
         this.maxFeatures = maxFeatures;
     }
 
     /**
      * Access verbose property.
      *
      * @return Returns the verbose.
      */
     public boolean isVerbose() {
         return verbose;
     }
 
     /**
      * Set verbose to verbose.
      *
      * @param verbose The verbose to set.
      */
     public void setVerbose(boolean verbose) {
         verboseChecked = true;
         this.verbose = verbose;
     }
 
     /**
              * Access verboseChecked property.
              *
              * @return Returns the verboseChecked.
              */
     public boolean isVerboseChecked() {
         return verboseChecked;
     }
 
     /**
      * Set verboseChecked to verboseChecked.
      *
      * @param verboseChecked The verboseChecked to set.
      */
     public void setVerboseChecked(boolean verboseChecked) {
         this.verboseChecked = verboseChecked;
     }
 
     /**
              * Access verboseChecked property.
              *
              * @return Returns the verboseChecked.
              */
     public boolean isVerboseExceptionsChecked() {
         return verboseExceptionsChecked;
     }
 
     /**
      * Set verboseChecked to verboseChecked.
      *
      * @param verboseChecked The verboseChecked to set.
      */
     public void setVerboseExceptionsChecked(boolean verboseExceptionsChecked) {
         this.verboseExceptionsChecked = verboseExceptionsChecked;
     }
 
     /**
      * Access verboseExceptions property.
      *
      * @return Returns the verboseExceptions.
      */
     public boolean isVerboseExceptions() {
         return verboseExceptions;
     }
 
     /**
      * Set verboseExceptions to verboseExceptions.
      *
      * @param verboseExceptions The verboseExceptions to set.
      */
     public void setVerboseExceptions(boolean verboseExceptions) {
         verboseExceptionsChecked = true;
         this.verboseExceptions = verboseExceptions;
     }
 
     /**
      * Access numDecimals property.
      *
      * @return Returns the numDecimals.
      */
     public int getNumDecimals() {
         return numDecimals;
     }
 
     /**
      * Set numDecimals to numDecimals.
      *
      * @param numDecimals The numDecimals to set.
      */
     public void setNumDecimals(int numDecimals) {
         this.numDecimals = numDecimals;
     }
 
     /**
      * Access charset property.
      *
      * @return Returns the charset.
      */
     public String getCharset() {
         return charset;
     }
 
     /**
      * Set charset to charset.
      *
      * @param charset The charset to set.
      */
     public void setCharset(String charset) {
         this.charset = charset;
     }
 
     /**
      * Access baseURL property.
      *
      * @return Returns the baseURL.
      */
     public String getProxyBaseUrl() {
         return "".equals(proxyBaseUrl) ? null : proxyBaseUrl;
     }
 
     /**
      * Set baseURL to baseURL.
      *
      * @param baseURL The baseURL to set.
      */
     public void setProxyBaseUrl(String baseURL) {
         this.proxyBaseUrl = baseURL;
     }
 
     /**
      * Access schemaBaseURL property.
      *
      * @return Returns the schemaBaseURL.
      */
     public String getSchemaBaseURL() {
         return schemaBaseURL;
     }
 
     /**
      * Set schemaBaseURL to schemaBaseURL.
      *
      * @param schemaBaseURL The schemaBaseURL to set.
      */
     public void setSchemaBaseURL(String schemaBaseURL) {
         this.schemaBaseURL = schemaBaseURL;
     }
 
     /**
      * Access loggingLevel property.
      *
      * @return Returns the loggingLevel.
      */
     public String getLoggingLevel() {
         return loggingLevel;
     }
 
     /**
      * Set loggingLevel to loggingLevel.
      *
      * @param loggingLevel The loggingLevel to set.
      */
     public void setLoggingLevel(String loggingLevel) {
         this.loggingLevel = loggingLevel;
     }
 
     /**
      * Access address property.
      *
      * @return Returns the address.
      */
     public String getAddress() {
         return address;
     }
 
     /**
      * Set address to address.
      *
      * @param address The address to set.
      */
     public void setAddress(String address) {
         this.address = address;
     }
 
     /**
      * Access addressCity property.
      *
      * @return Returns the addressCity.
      */
     public String getAddressCity() {
         return addressCity;
     }
 
     /**
      * Set addressCity to addressCity.
      *
      * @param addressCity The addressCity to set.
      */
     public void setAddressCity(String addressCity) {
         this.addressCity = addressCity;
     }
 
     /**
      * Access addressCountry property.
      *
      * @return Returns the addressCountry.
      */
     public String getAddressCountry() {
         return addressCountry;
     }
 
     /**
      * Set addressCountry to addressCountry.
      *
      * @param addressCountry The addressCountry to set.
      */
     public void setAddressCountry(String addressCountry) {
         this.addressCountry = addressCountry;
     }
 
     /**
      * Access addressPostalCode property.
      *
      * @return Returns the addressPostalCode.
      */
     public String getAddressPostalCode() {
         return addressPostalCode;
     }
 
     /**
      * Set addressPostalCode to addressPostalCode.
      *
      * @param addressPostalCode The addressPostalCode to set.
      */
     public void setAddressPostalCode(String addressPostalCode) {
         this.addressPostalCode = addressPostalCode;
     }
 
     /**
      * Access addressState property.
      *
      * @return Returns the addressState.
      */
     public String getAddressState() {
         return addressState;
     }
 
     /**
      * Set addressState to addressState.
      *
      * @param addressState The addressState to set.
      */
     public void setAddressState(String addressState) {
         this.addressState = addressState;
     }
 
     /**
      * Access addressType property.
      *
      * @return Returns the addressType.
      */
     public String getAddressType() {
         return addressType;
     }
 
     /**
      * Set addressType to addressType.
      *
      * @param addressType The addressType to set.
      */
     public void setAddressType(String addressType) {
         this.addressType = addressType;
     }
 
     /**
      * Access contactEmail property.
      *
      * @return Returns the contactEmail.
      */
     public String getContactEmail() {
         return contactEmail;
     }
 
     /**
      * Set contactEmail to contactEmail.
      *
      * @param contactEmail The contactEmail to set.
      */
     public void setContactEmail(String contactEmail) {
         this.contactEmail = contactEmail;
     }
 
     /**
      * Access contactFacsimile property.
      *
      * @return Returns the contactFacsimile.
      */
     public String getContactFacsimile() {
         return contactFacsimile;
     }
 
     /**
      * Set contactFacsimile to contactFacsimile.
      *
      * @param contactFacsimile The contactFacsimile to set.
      */
     public void setContactFacsimile(String contactFacsimile) {
         this.contactFacsimile = contactFacsimile;
     }
 
     /**
      * Access contactOrganization property.
      *
      * @return Returns the contactOrganization.
      */
     public String getContactOrganization() {
         return contactOrganization;
     }
 
     /**
      * Set contactOrganization to contactOrganization.
      *
      * @param contactOrganization The contactOrganization to set.
      */
     public void setContactOrganization(String contactOrganization) {
         this.contactOrganization = contactOrganization;
     }
 
     /**
      * Access contactPerson property.
      *
      * @return Returns the contactPerson.
      */
     public String getContactPerson() {
         return contactPerson;
     }
 
     /**
      * Set contactPerson to contactPerson.
      *
      * @param contactPerson The contactPerson to set.
      */
     public void setContactPerson(String contactPerson) {
         this.contactPerson = contactPerson;
     }
 
     /**
      * Access contactPosition property.
      *
      * @return Returns the contactPosition.
      */
     public String getContactPosition() {
         return contactPosition;
     }
 
     /**
      * Set contactPosition to contactPosition.
      *
      * @param contactPosition The contactPosition to set.
      */
     public void setContactPosition(String contactPosition) {
         this.contactPosition = contactPosition;
     }
 
     /**
      * Access contactVoice property.
      *
      * @return Returns the contactVoice.
      */
     public String getContactVoice() {
         return contactVoice;
     }
 
     /**
      * Set contactVoice to contactVoice.
      *
      * @param contactVoice The contactVoice to set.
      */
     public void setContactVoice(String contactVoice) {
         this.contactVoice = contactVoice;
     }
 
     //No sets yet, they will be needed for login config page though.
     public String getAdminUserName() {
         return adminUserName;
     }
 
     public String getAdminPassword() {
         return adminPassword;
     }
 
     /**
      * @return The string representation of the path on disk in which the
      * server logs to.
      */
     public String getLogLocation() {
         return logLocation;
     }
 
     /**
      * @param logLocation The string representation of the path on disk in which
      * the server logs to.
      */
     public void setLogLocation(String logLocation) {
         this.logLocation = logLocation;
     }
 
     /**
      * Set loggingToFile to loggingToFile.
      *
      * @param verbose The loggingToFile to set.
      */
     public void setLoggingToFile(boolean loggingToFile) {
         loggingToFileChecked = true;
         this.loggingToFile = loggingToFile;
     }
 
     /**
      * Access loggingToFile property.
      *
      * @return Returns the loggingToFile.
      */
     public boolean isLoggingToFile() {
         return loggingToFile;
     }
 
     /**
      * Access loggingToFileChecked property.
      *
      * @return Returns the loggingToFileChecked.
      */
     public boolean isLoggingToFileChecked() {
         return loggingToFileChecked;
     }
 
     /**
      * Set loggingToFileChecked to loggingToFileChecked.
      *
      * @param loggingToFileChecked The loggingToFileChecked to set.
      */
     public void setLoggingToFileChecked(boolean loggingToFileChecked) {
         this.loggingToFileChecked = loggingToFileChecked;
     }
 
     public double getJaiMemoryCapacity() {
         return jaiMemoryCapacity;
     }
 
     public void setJaiMemoryCapacity(double jaiMemoryCapacity) {
         this.jaiMemoryCapacity = jaiMemoryCapacity;
     }
 
     public boolean getJaiRecycling() {
         return jaiRecycling;
     }
 
     public void setJaiRecycling(boolean jaiRecycling) {
         jaiRecyclingChecked = true;
         this.jaiRecycling = jaiRecycling;
     }
 
     public boolean getJaiJPEGNative() {
         return jaiJPEGNative;
     }
 
     public void setJaiJPEGNative(boolean jaiJPEGNative) {
         jaiJPEGNativeChecked = true;
         this.jaiJPEGNative = jaiJPEGNative;
     }
 
     public boolean getJaiPNGNative() {
         return jaiPNGNative;
     }
 
     public void setJaiPNGNative(boolean jaiPNGNative) {
         jaiPNGNativeChecked = true;
         this.jaiPNGNative = jaiPNGNative;
     }
 
     /**
      * Access recyclingChecked property.
      *
      * @return Returns the recyclingChecked.
      */
     public boolean isJaiRecyclingChecked() {
         return jaiRecyclingChecked;
     }
 
     /**
      * Access nativeChecked property.
      *
      * @return Returns the nativeChecked.
      */
     public boolean isJaiJPEGNativeChecked() {
         return jaiJPEGNativeChecked;
     }
 
     /**
      * Access nativeChecked property.
      *
      * @return Returns the nativeChecked.
      */
     public boolean isJaiPNGNativeChecked() {
         return jaiPNGNativeChecked;
     }
 
     /**
      * Set recyclingChecked to recyclingChecked.
      *
      * @param recyclingChecked The recyclingChecked to set.
      */
     public void setJaiRecyclingChecked(boolean jaiRecyclingChecked) {
         this.jaiRecyclingChecked = jaiRecyclingChecked;
     }
 
     /**
      * Set nativeChecked to nativeChecked.
      *
      * @param nativeChecked The nativeChecked to set.
      */
     public void setJaiJPEGNativeChecked(boolean jaiJPEGNativeChecked) {
         this.jaiJPEGNativeChecked = jaiJPEGNativeChecked;
     }
 
     /**
      * Set nativeChecked to nativeChecked.
      *
      * @param nativeChecked The nativeChecked to set.
      */
     public void setJaiPNGNativeChecked(boolean jaiPNGNativeChecked) {
         this.jaiPNGNativeChecked = jaiPNGNativeChecked;
     }
 
     public boolean getImageIOCache() {
         return imageIOCache;
     }
 
     public void setImageIOCache(boolean imageIOCache) {
         imageIOCacheChecked = true;
         this.imageIOCache = imageIOCache;
     }
 
     /**
      * Access verboseChecked property.
      *
      * @return Returns the verboseChecked.
      */
     public boolean isImageIOCacheChecked() {
         return imageIOCacheChecked;
     }
 
     /**
      * Set verboseChecked to verboseChecked.
      *
      * @param verboseChecked The verboseChecked to set.
      */
     public void setImageIOCacheChecked(boolean imageIOCacheChecked) {
         this.imageIOCacheChecked = imageIOCacheChecked;
     }
 
     public double getJaiMemoryThreshold() {
         return jaiMemoryThreshold;
     }
 
     public void setJaiMemoryThreshold(double jaiMemoryThreshold) {
         this.jaiMemoryThreshold = jaiMemoryThreshold;
     }
 
     public int getJaiTilePriority() {
         return jaiTilePriority;
     }
 
     public void setJaiTilePriority(int jaiTilePriority) {
         this.jaiTilePriority = jaiTilePriority;
     }
 
     public int getJaiTileThreads() {
         return jaiTileThreads;
     }
 
     public void setJaiTileThreads(int jaiTileThreads) {
         this.jaiTileThreads = jaiTileThreads;
     }
 }
