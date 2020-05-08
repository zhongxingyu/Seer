 package controllers;
 
 import entities.Feedback2013Student;
 import controllers.util.JsfUtil;
 import controllers.util.PaginationHelper;
 import beans.Feedback2013StudentFacade;
 import java.io.IOException;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.inject.Named;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.SelectItem;
 
 @Named("feedback2013StudentController")
 @SessionScoped
 public class Feedback2013StudentController implements Serializable {
 
     private Feedback2013Student current;
     private DataModel items = null;
     @EJB
     private beans.Feedback2013StudentFacade ejbFacade;
     private PaginationHelper pagination;
     private int selectedItemIndex;
 
     public Feedback2013StudentController() {
     }
 
     public Feedback2013Student getSelected() {
         if (current == null) {
             current = new Feedback2013Student();
             selectedItemIndex = -1;
         }
         return current;
     }
 
     private Feedback2013StudentFacade getFacade() {
         return ejbFacade;
     }
 
     public PaginationHelper getPagination() {
         if (pagination == null) {
             pagination = new PaginationHelper(10) {
                 @Override
                 public int getItemsCount() {
                     return getFacade().count();
                 }
 
                 @Override
                 public DataModel createPageDataModel() {
                     return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                 }
             };
         }
         return pagination;
     }
 
     public void setSelected(Feedback2013Student fs) {
         this.current = fs;
     }
 
     public Feedback2013Student getLoggedUser() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         String uid = facesContext.getExternalContext().getRemoteUser();
         //String uid = "100";
         return getFeedback2013Student(Integer.parseInt(uid));
 
     }
 
     public void prepareListUser() {
         current = getLoggedUser();
         if (current != null){
         current.setLoginTime(new Date());
         update(); }
         try {
             if(FacesContext.getCurrentInstance().getExternalContext().isUserInRole("staff"))
             {
                 FacesContext.getCurrentInstance().getExternalContext().redirect("/feedback/faces/f360/SelectSubject.xhtml");
             }
             else if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("superuser")) {
                FacesContext.getCurrentInstance().getExternalContext().redirect("/feedback/faces/admin/AdminIndex.xhtml");
             }
             else if (getLoggedUser().getLogoutTime() == null) {
                 if (!getLoggedUser().getLoginStatus()) {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("/feedback/faces/student/SelectBatch.xhtml");
 
                 } else {
                     FacultySubjectController fsc = (FacultySubjectController) FacesContext.getCurrentInstance().getApplication().getELResolver().getValue(FacesContext.getCurrentInstance().getELContext(), null, "facultySubjectController");
                     
                     fsc.prepareDetails(getLoggedUser().getBatch());
                    FacesContext.getCurrentInstance().getExternalContext().redirect("/feedback/faces/student/SelectSubject.xhtml");
 
                 }
             } else {
                FacesContext.getCurrentInstance().getExternalContext().redirect("/feedback/faces/common/errorRelogin.xhtml");
 
             }
         } catch (IOException ex) {
             Logger.getLogger(Feedback2013StudentController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public String prepareList() {
         recreateModel();
         return "List";
     }
 
     public String prepareView() {
         current = (Feedback2013Student) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "View";
     }
 
     public String prepareCreate() {
         current = new Feedback2013Student();
         selectedItemIndex = -1;
         return "Create";
     }
 
     public String create() {
         try {
             getFacade().create(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("Feedback2013StudentCreated"));
             return prepareCreate();
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String prepareEdit() {
         current = (Feedback2013Student) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "Edit";
     }
 
     public String update() {
         try {
             getFacade().edit(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("Feedback2013StudentUpdated"));
             return "View";
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String destroy() {
         current = (Feedback2013Student) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         performDestroy();
         recreatePagination();
         recreateModel();
         return "List";
     }
 
     public List<Feedback2013Student> findAll() {
         return getFacade().findAll();
     }
 
     public String destroyAndView() {
         performDestroy();
         recreateModel();
         updateCurrentItem();
         if (selectedItemIndex >= 0) {
             return "View";
         } else {
             // all items were removed - go back to list
             recreateModel();
             return "List";
         }
     }
 
     private void performDestroy() {
         try {
             getFacade().remove(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("Feedback2013StudentDeleted"));
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
         }
     }
 
     private void updateCurrentItem() {
         int count = getFacade().count();
         if (selectedItemIndex >= count) {
             // selected index cannot be bigger than number of items:
             selectedItemIndex = count - 1;
             // go to previous page if last page disappeared:
             if (pagination.getPageFirstItem() >= count) {
                 pagination.previousPage();
             }
         }
         if (selectedItemIndex >= 0) {
             current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
         }
     }
 
     public DataModel getItems() {
         if (items == null) {
             items = getPagination().createPageDataModel();
         }
         return items;
     }
 
     private void recreateModel() {
         items = null;
     }
 
     private void recreatePagination() {
         pagination = null;
     }
 
     public String next() {
         getPagination().nextPage();
         recreateModel();
         return "List";
     }
 
     public String previous() {
         getPagination().previousPage();
         recreateModel();
         return "List";
     }
 
     public SelectItem[] getItemsAvailableSelectMany() {
         return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
     }
 
     public SelectItem[] getItemsAvailableSelectOne() {
         return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
     }
 
     public Feedback2013Student getFeedback2013Student(java.lang.Integer id) {
         return ejbFacade.find(id);
     }
 
     @FacesConverter(forClass = Feedback2013Student.class)
     public static class Feedback2013StudentControllerConverter implements Converter {
 
         @Override
         public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
             if (value == null || value.length() == 0) {
                 return null;
             }
             Feedback2013StudentController controller = (Feedback2013StudentController) facesContext.getApplication().getELResolver().
                     getValue(facesContext.getELContext(), null, "feedback2013StudentController");
             return controller.getFeedback2013Student(getKey(value));
         }
 
         java.lang.Integer getKey(String value) {
             java.lang.Integer key;
             key = Integer.valueOf(value);
             return key;
         }
 
         String getStringKey(java.lang.Integer value) {
             StringBuilder sb = new StringBuilder();
             sb.append(value);
             return sb.toString();
         }
 
         @Override
         public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
             if (object == null) {
                 return null;
             }
             if (object instanceof Feedback2013Student) {
                 Feedback2013Student o = (Feedback2013Student) object;
                 return getStringKey(o.getUid());
             } else {
                 throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Feedback2013Student.class.getName());
             }
         }
     }
 }
