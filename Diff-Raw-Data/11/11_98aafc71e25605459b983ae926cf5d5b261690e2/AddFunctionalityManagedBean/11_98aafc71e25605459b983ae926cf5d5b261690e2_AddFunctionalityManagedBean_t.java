 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commonInfrastructure.ERMS.managedbean;
 
 import ERMS.entity.FunctionalityEntity;
 import ERMS.session.FunctionalitySessionBean;
 import java.io.IOException;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 /**
  *
  * @author liuxian
  */
 @ManagedBean
 @RequestScoped
 public class AddFunctionalityManagedBean {
 
     /**
      * Creates a new instance of AddEmployeeManagedBean
      */
     @EJB
     FunctionalitySessionBean functionalityManager;
     private FunctionalityEntity functionality;
     private boolean show = false;
 
     public AddFunctionalityManagedBean() {
         functionality = new FunctionalityEntity();
     }
 
     public void saveNewFunctionality(ActionEvent event) {
         show = true;
         try {
             functionalityManager.addFunctionality(getFunctionality());
         } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding new functionality", ""));
             return;
         }
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "New Functionality Saved.", ""));
     }
 
     public FunctionalityEntity getFunctionality() {
         return functionality;
     }
 
     public void setFunctionality(FunctionalityEntity functionality) {
         this.functionality = functionality;
     }
 
     public void oneMore(ActionEvent event) throws IOException {
         FacesContext.getCurrentInstance().getExternalContext().redirect("addFunctionality.xhtml");
     }
 
     /**
      * @return the show
      */
     public boolean isShow() {
         return show;
     }
 
     /**
      * @param show the show to set
      */
     public void setShow(boolean show) {
         this.show = show;
     }
 }
