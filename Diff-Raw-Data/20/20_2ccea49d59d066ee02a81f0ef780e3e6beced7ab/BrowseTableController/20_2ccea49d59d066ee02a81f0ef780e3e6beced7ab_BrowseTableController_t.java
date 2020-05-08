 package com.fs.humanResources.browse.controller;
 
 import com.fs.humanResources.browse.model.BrowseTableModel;
 import com.fs.humanResources.browse.view.BrowseViewBean;
 import org.primefaces.model.LazyDataModel;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 @Named
 @RequestScoped
 public class BrowseTableController {
 
     @Inject
     BrowseTableModel browseTableModel;
 
     BrowseViewBean selectedViewBean;
 
     public BrowseViewBean getSelectedEmployee() {
         return selectedViewBean;
     }
 
     public void setSelectedEmployee(BrowseViewBean selectedViewBean) {
         this.selectedViewBean = selectedViewBean;
     }
 
     public LazyDataModel getLazyModel() {
         return browseTableModel.getDataModel();
     }
 
    public void clearDataModel() {
        browseTableModel.clearDataModel();
    }

 }
