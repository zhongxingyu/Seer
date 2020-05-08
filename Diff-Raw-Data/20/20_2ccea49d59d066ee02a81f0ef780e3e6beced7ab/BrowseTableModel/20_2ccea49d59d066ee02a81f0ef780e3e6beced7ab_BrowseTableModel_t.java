 package com.fs.humanResources.browse.model;
 
 import com.fs.humanResources.browse.view.BrowseViewBean;
 import com.fs.humanResources.dto.employee.EmployeeDTO;
 import com.fs.humanResources.employee.view.employee.EmployeeViewBean;
 import com.fs.humanResources.service.HumanResourcesService;
 import org.apache.log4j.Logger;
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Stateful;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 @Stateful
 @SessionScoped
 public class BrowseTableModel implements Serializable {
 
     Logger log = Logger.getLogger(BrowseTableModel.class);
 
     @Inject
     private HumanResourcesService humanResourcesService;
 
     private List<BrowseViewBean> lazyLoadedData;
 
     private LazyDataModel<BrowseViewBean> dataModel;
 
     @PostConstruct
     public void init() {
         lazyLoadedData = new ArrayList<BrowseViewBean>();
         dataModel = buildDataModel();
     }
 
    public void clearDataModel() {
       init();
    }

     public LazyDataModel<BrowseViewBean> getDataModel(){
         return dataModel;
     }
 
     private LazyDataModel<BrowseViewBean> buildDataModel() {
         return new LazyDataModel<BrowseViewBean>() {
 
             @Override
             public BrowseViewBean getRowData(String rowKey) {
                 for(BrowseViewBean browseViewBean : lazyLoadedData) {
                     String id = browseViewBean.getEmployee().getEmployeeId().toString();
 
                     if(id.equals(rowKey)) {
                         return browseViewBean;
                     }
                 }
 
                 return null;
             }
 
             @Override
             public Object getRowKey(BrowseViewBean browseViewBean) {
                 return browseViewBean.getEmployee().getEmployeeId();
             }
 
             @Override
             public List<BrowseViewBean> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
                 lazyLoadedData.clear();
 
                 List<EmployeeDTO> employeeDTOList = humanResourcesService.findEmployees(first, pageSize);
 
                 for(EmployeeDTO employeeDTO : employeeDTOList) {
                     log.info("adding "+employeeDTO);
                     EmployeeViewBean employeeViewBean = new EmployeeViewBean(employeeDTO);
                     BrowseViewBean browseViewBean = new BrowseViewBean(employeeViewBean);
                     lazyLoadedData.add(browseViewBean);
                 }
 
                 setRowCount(humanResourcesService.findTotalEmployeeCount());
 
                 return lazyLoadedData;
             }
         };
     }
 }
