 package controllers;
 
 import entities.F360QuestionDepartment;
 import controllers.util.JsfUtil;
 import controllers.util.PaginationHelper;
 import beans.F360QuestionDepartmentFacade;
 import entities.F360FeedbackDepartment;
 import entities.F360User;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.ResourceBundle;
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
 
 @Named("f360QuestionDepartmentController")
 @SessionScoped
 public class F360QuestionDepartmentController implements Serializable {
 
     private F360QuestionDepartment current;
     private DataModel items = null;
     @EJB
     private beans.F360QuestionDepartmentFacade ejbFacade;
     private PaginationHelper pagination;
     private int selectedItemIndex;
 
     public F360QuestionDepartmentController() {
     }
 
     public F360QuestionDepartment getSelected() {
         if (current == null) {
             current = new F360QuestionDepartment();
             selectedItemIndex = -1;
         }
         return current;
     }
 
     private F360QuestionDepartmentFacade getFacade() {
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
 
     public String prepareList() {
         recreateModel();
         return "List";
     }
 
     public String prepareView() {
         current = (F360QuestionDepartment) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "View";
     }
 
     public String prepareCreate() {
         current = new F360QuestionDepartment();
         selectedItemIndex = -1;
         return "Create";
     }
 
     public String create() {
         try {
             getFacade().create(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("F360QuestionDepartmentCreated"));
             return prepareCreate();
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String prepareEdit() {
         current = (F360QuestionDepartment) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "Edit";
     }
 
     public String update() {
         try {
             getFacade().edit(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("F360QuestionDepartmentUpdated"));
             return "View";
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String destroy() {
         current = (F360QuestionDepartment) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         performDestroy();
         recreatePagination();
         recreateModel();
         return "List";
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
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("F360QuestionDepartmentDeleted"));
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
         }
     }
    
    public String departmentQuestionnaire(){
        return "Theory.xhtml?faces-redirect=true";
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
     
     public DataModel getItemsNoPagination() {
         
         List<F360QuestionDepartment> list = getFacade().findAll();
         FacesContext facesContext = FacesContext.getCurrentInstance();
         F360UserController f360UserController = (F360UserController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "f360UserController");
         F360FeedbackDepartmentController f360FeedbackDepartmentController = (F360FeedbackDepartmentController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "f360FeedbackDepartmentController");
         String username = facesContext.getExternalContext().getRemoteUser();
         F360User uid = f360UserController.getF360User(Integer.parseInt(username));
         List<F360FeedbackDepartment> list2 = f360FeedbackDepartmentController.getItemsUser(f360UserController.getF360User(Integer.parseInt(username)));
         for(F360FeedbackDepartment item : list2) {
               list.get(list.indexOf(item.getQid())).setRating(item.getIdAns());
 
         }
         
         return new ListDataModel(list);
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
 
     public F360QuestionDepartment getF360QuestionDepartment(java.lang.Short id) {
         return ejbFacade.find(id);
     }
 
     @FacesConverter(forClass = F360QuestionDepartment.class)
     public static class F360QuestionDepartmentControllerConverter implements Converter {
 
         @Override
         public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
             if (value == null || value.length() == 0) {
                 return null;
             }
             F360QuestionDepartmentController controller = (F360QuestionDepartmentController) facesContext.getApplication().getELResolver().
                     getValue(facesContext.getELContext(), null, "f360QuestionDepartmentController");
             return controller.getF360QuestionDepartment(getKey(value));
         }
 
         java.lang.Short getKey(String value) {
             java.lang.Short key;
             key = Short.valueOf(value);
             return key;
         }
 
         String getStringKey(java.lang.Short value) {
             StringBuilder sb = new StringBuilder();
             sb.append(value);
             return sb.toString();
         }
 
         @Override
         public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
             if (object == null) {
                 return null;
             }
             if (object instanceof F360QuestionDepartment) {
                 F360QuestionDepartment o = (F360QuestionDepartment) object;
                 return getStringKey(o.getQid());
             } else {
                 throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + F360QuestionDepartment.class.getName());
             }
         }
     }
 }
