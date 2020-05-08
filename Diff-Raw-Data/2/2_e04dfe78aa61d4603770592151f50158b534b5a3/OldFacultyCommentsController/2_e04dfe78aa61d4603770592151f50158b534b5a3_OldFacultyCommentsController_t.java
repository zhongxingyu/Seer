 package controllers;
 
 import entities.OldFacultyComments;
 import controllers.util.JsfUtil;
 import controllers.util.PaginationHelper;
 import beans.OldFacultyCommentsFacade;
 import entities.OldFbPi;
 
 import java.io.Serializable;
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
 
 @Named("oldFacultyCommentsController")
 @SessionScoped
 public class OldFacultyCommentsController implements Serializable {
 
     private OldFacultyComments current;
     private DataModel items = null;
     @EJB
     private beans.OldFacultyCommentsFacade ejbFacade;
     private PaginationHelper pagination;
     private int selectedItemIndex;
     private DataModel detailsByFS;
     private String subID;
     private String div;
     private short ftype;
     private short batch;
     private String userName;
 
     public OldFacultyCommentsController() {
     }
 
     public OldFacultyComments getSelected() {
         if (current == null) {
             current = new OldFacultyComments();
             selectedItemIndex = -1;
         }
         return current;
     }
 
     private OldFacultyCommentsFacade getFacade() {
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
         current = (OldFacultyComments) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "View";
     }
 
     public String prepareCreate() {
         current = new OldFacultyComments();
         selectedItemIndex = -1;
         return "Create";
     }
 
     public String create() {
         try {
             getFacade().create(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("OldFacultyCommentsCreated"));
             return prepareCreate();
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String prepareEdit() {
         current = (OldFacultyComments) getItems().getRowData();
         selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
         return "Edit";
     }
 
     public String update() {
         try {
             getFacade().edit(current);
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("OldFacultyCommentsUpdated"));
             return "View";
         } catch (Exception e) {
             JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
             return null;
         }
     }
 
     public String destroy() {
         current = (OldFacultyComments) getItems().getRowData();
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
             JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("OldFacultyCommentsDeleted"));
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
 
     public String getFeedbackComments(OldFbPi item) {
         this.userName = item.getOldFbPiPK().getFacId();
         this.subID = item.getOldFbPiPK().getSubId();
         this.div = item.getOldFbPiPK().getDivision();
         this.ftype = item.getFtype();
         this.batch = item.getOldFbPiPK().getBatch();
         recreateModel();
           return "FeedbackComments?faces-redirect=true";
 
     }
     public String getFeedbackCommentsAdmin(OldFbPi item) {
         this.userName = item.getOldFbPiPK().getFacId();
         this.subID = item.getOldFbPiPK().getSubId();
         this.div = item.getOldFbPiPK().getDivision();
         this.ftype = item.getFtype();
         this.batch = item.getOldFbPiPK().getBatch();
         recreateModel();
         return null;
     }
 
     public DataModel getDetailsByFS() {
         detailsByFS = new ListDataModel(getFacade().getByFS(userName, div, ftype, batch, subID));
         return detailsByFS;
     }
 
     private void recreateModel() {
         items = null;
         detailsByFS = null;
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
 
     public OldFacultyComments getOldFacultyComments(java.lang.Integer id) {
         return ejbFacade.find(id);
     }
 
     @FacesConverter(forClass = OldFacultyComments.class)
     public static class OldFacultyCommentsControllerConverter implements Converter {
 
         @Override
         public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
             if (value == null || value.length() == 0) {
                 return null;
             }
             OldFacultyCommentsController controller = (OldFacultyCommentsController) facesContext.getApplication().getELResolver().
                     getValue(facesContext.getELContext(), null, "oldFacultyCommentsController");
             return controller.getOldFacultyComments(getKey(value));
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
             if (object instanceof OldFacultyComments) {
                 OldFacultyComments o = (OldFacultyComments) object;
                 return getStringKey(o.getId());
             } else {
                 throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + OldFacultyComments.class.getName());
             }
         }
     }
 }
