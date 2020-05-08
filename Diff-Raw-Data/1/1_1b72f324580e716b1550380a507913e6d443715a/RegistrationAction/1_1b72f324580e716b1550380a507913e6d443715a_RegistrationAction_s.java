 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.web.action.registration;
 
 import gov.nih.nci.caintegrator2.application.registration.RegistrationRequest;
 import gov.nih.nci.caintegrator2.application.registration.RegistrationService;
 import gov.nih.nci.caintegrator2.common.ConfigurationHelper;
 import gov.nih.nci.caintegrator2.common.ConfigurationParameter;
 import gov.nih.nci.caintegrator2.security.SecurityManager;
 import gov.nih.nci.security.exceptions.internal.CSInternalConfigurationException;
 import gov.nih.nci.security.exceptions.internal.CSInternalInsufficientAttributesException;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.Preparable;
 import com.opensymphony.xwork2.validator.annotations.VisitorFieldValidator;
 
 /**
  * Action for registering a user to cai2.
  */
 @SuppressWarnings("PMD.ReplaceHashtableWithMap") // LDAPHelper.authenticate uses a Hashtable.
 public class RegistrationAction extends ActionSupport implements Preparable {
 
     private static final long serialVersionUID = 1L;
     
     private SecurityManager securityManager;
     private RegistrationService registrationService;
     private ConfigurationHelper configurationHelper;
     
     private final Map<String, String> ldapContextParams = new HashMap<String, String>();
     private RegistrationRequest registrationRequest = new RegistrationRequest();
     private Boolean ldapAuthenticate;
     private String password;
     
     /**
      * {@inheritDoc}
      */
     public void prepare() {
         ldapAuthenticate = Boolean.TRUE;
         ldapContextParams.putAll(registrationService.getLdapContextParams());
     }
 
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         if (isLdapInstall() && ldapAuthenticate) {
             if (!validateLoginName()) {
                 return;
             }
             if (StringUtils.isBlank(password)) {
                 addFieldError("password", "Must enter a password for LDAP authentication.");
                 return;
             }
             validateLdap();
         }
     }
 
     
     private boolean validateLoginName() {
         if (StringUtils.isBlank(registrationRequest.getLoginName())) {
             return false;
         }
         if (securityManager.doesUserExist(registrationRequest.getLoginName())) {
            addFieldError("registrationRequest.loginName", "Login name is already in use.");
            return false;
         }
         return true;
     }
 
 
     private void validateLdap() {
         try {
             if (!registrationService.ldapAuthenticate(ldapContextParams, registrationRequest
                     .getLoginName(), password)) {
                 addActionError("LDAP Authentication Failure.  " 
                              + "Please verify that your LDAP username and password are correct.");
             }
         } catch (CSInternalConfigurationException e) {
             addActionError(e.getMessage());
         } catch (CSInternalInsufficientAttributesException e) {
             addActionError(e.getMessage());
         }
     }
 
     /**
      * Action to actually save the registration with authentication.
      * @return struts result.
      */
     public String save() {
         try {
             registrationRequest.setUptUrl(configurationHelper.getString(ConfigurationParameter.UPT_URL));
             registrationService.registerUser(registrationRequest);
             return SUCCESS;
         } catch (MessagingException e) {
             addActionError("Failed to send email due to the following: " + e.getMessage());
             return INPUT;
         }
     }
     
     /**
      * If user wishes to cancel.
      * @return cancel string.
      */
     public String cancel() {
         return "cancel";
     }
 
     /**
      * @return the registrationRequest
      */
     @VisitorFieldValidator(message = "")
     public RegistrationRequest getRegistrationRequest() {
         return registrationRequest;
     }
 
     /**
      * @param registrationRequest the registrationRequest to set
      */
     public void setRegistrationRequest(RegistrationRequest registrationRequest) {
         this.registrationRequest = registrationRequest;
     }
 
     /**
      * @return the registrationService
      */
     public RegistrationService getRegistrationService() {
         return registrationService;
     }
 
     /**
      * @param registrationService the registrationService to set
      */
     public void setRegistrationService(RegistrationService registrationService) {
         this.registrationService = registrationService;
     }
 
     /**
      * @return the securityManager
      */
     public SecurityManager getSecurityManager() {
         return securityManager;
     }
 
     /**
      * @param securityManager the securityManager to set
      */
     public void setSecurityManager(SecurityManager securityManager) {
         this.securityManager = securityManager;
     }
 
     /**
      * @return is ldap install?
      */
     public boolean isLdapInstall() {
         return !ldapContextParams.isEmpty();
     }
 
     /**
      * @return the ldapAuthenticate
      */
     public Boolean getLdapAuthenticate() {
         return ldapAuthenticate;
     }
 
     /**
      * @param ldapAuthenticate the ldapAuthenticate to set
      */
     public void setLdapAuthenticate(Boolean ldapAuthenticate) {
         this.ldapAuthenticate = ldapAuthenticate;
     }
 
     /**
      * @return the password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * @param password the password to set
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * @return the configurationHelper
      */
     public ConfigurationHelper getConfigurationHelper() {
         return configurationHelper;
     }
 
 
     /**
      * @param configurationHelper the configurationHelper to set
      */
     public void setConfigurationHelper(ConfigurationHelper configurationHelper) {
         this.configurationHelper = configurationHelper;
     }
 
 }
