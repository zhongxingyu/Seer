 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package roman.cwk.managedbeans;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import roman.cwk.bus.BusinessException;
 import roman.cwk.bus.OrganizationService;
 import roman.cwk.entity.Organization;
 
 /**
  * Controller, managed bean for organization and registration of new user. Adds
  * error messages to the view if necessary.
  *
  * @author Roman Macor
  */
 @ManagedBean
 //@RequestScoped
 @SessionScoped
 public class OrganizationBean extends BaseBean {
 
     private Organization organization;
     private String repeatPassword;
     private String currentPassword;
     @EJB
     private OrganizationService ejbOrganizationService;
 
     /**
      * Creates a new instance of OrganizationBean
      */
     public OrganizationBean() {
     }
 
     public String getCurrentPassword() {
         return currentPassword;
     }
 
     public void setCurrentPassword(String currentPassword) {
         this.currentPassword = currentPassword;
     }
 
     public Organization getOrganization() {
         if (organization == null) {
             organization = new Organization();
         }
         return organization;
     }
 
     public void setOrganization(Organization organization) {
         this.organization = organization;
     }
 
     public String getRepeatPassword() {
         return repeatPassword;
     }
 
     public void setRepeatPassword(String repeatPassword) {
         this.repeatPassword = repeatPassword;
     }
 
     /**
      * Register a new user. (Create organization) according to user input.
      *
      * @return String redirecting to confirmation page.
      */
     public String register() {
         try {
             ejbOrganizationService.register(organization);
         } catch (BusinessException ex) {
             Logger.getLogger(OrganizationBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "confirmation";
     }
 
     /**
      * Prepare view for editing organization details.
      *
      * @return String that redirects to edit organization page
      */
     public String prepareEditOrganization() {
 
         String organizationName = this.getLogedUsername();
         try {
             organization = ejbOrganizationService.prepareEditOrganization(organizationName);
         } catch (BusinessException ex) {
             Logger.getLogger(OrganizationBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "/project/edit_organization";
     }
 
     /**
      * Edit organization details.
      *
      * @return String that redirects to index page for logged users.
      */
     public String edit() {
 
         organization = ejbOrganizationService.edit(organization);
 
         return "/project/index";
     }
 
     /**
      * Changes password.
      *
      * @return String that redirects to index page for logged users.
      */
     public String changePassword() {
         try {
             ejbOrganizationService.changePassword(currentPassword, organization);
         } catch (BusinessException ex) {
             Logger.getLogger(OrganizationBean.class.getName()).log(Level.SEVERE, null, ex);
             addErrorMessage(ex.getMessage());
             return "";
         }
         return "/project/index";
     }
 
     /**
      * Validator method that checks whether the password and the repeat password
      * are the same.
      *
      * @param context current instance of FacesContext
      * @param toValidate component to be validated.
      * @param value value of the component.
      */
     public void checkPasswordsEquality(FacesContext context,
             UIComponent toValidate, Object value) {
 
         UIInput passwordComponent = (UIInput) toValidate.getAttributes().get("password");
         String password = (String) passwordComponent.getValue();
 
         String repeatedPassword = (String) value;
 
         if (!repeatedPassword.equals(password)) {
             ((UIInput) toValidate).setValid(false);
 
             context.addMessage(toValidate.getClientId(context),
                     new FacesMessage("Passwords don't match."));
         }
     }
 }
