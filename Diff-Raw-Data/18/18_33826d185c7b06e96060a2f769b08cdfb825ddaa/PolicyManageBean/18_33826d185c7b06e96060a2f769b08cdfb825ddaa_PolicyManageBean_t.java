 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.dao.PolicyDao;
 import java.io.Serializable;
 import java.util.List;
 
 public class PolicyManageBean implements Serializable {
 
     private List<PrivilegeBean> privilegeBeans;
     private PolicyDao policyDao;
     private PolicyManageTableBean policyManageTableBean;
     private boolean viewOptionsPopupVisible = false;
     private String searchFilter;
 
     public List<PrivilegeBean> getPrivilegeBeans() {
         return privilegeBeans;
     }
 
     public void reset() {
         privilegeBeans = policyDao.getPrivilegeBeans();
         policyManageTableBean = new PolicyManageTableBean(privilegeBeans);
     }
 
     public void setPolicyDao(PolicyDao policyDao) {
         this.policyDao = policyDao;
         privilegeBeans = policyDao.getPrivilegeBeans();
         policyManageTableBean = new PolicyManageTableBean(privilegeBeans);
     }
 
     public PolicyManageTableBean getPolicyManageTableBean() {
         return policyManageTableBean;
     }
 
     public boolean isViewOptionsPopupVisible() {
         return viewOptionsPopupVisible;
     }
 
     public void setViewOptionsPopupVisible(boolean viewOptionsPopupVisible) {
         this.viewOptionsPopupVisible = viewOptionsPopupVisible;
     }
 
     public String getSearchFilter() {
         return searchFilter;
     }
 
     public List<String> getPrivilegeNames() {
         return getPrivilegeNames(null);
     }
 
     public List<String> getPrivilegeNames(String filter) {
         return policyDao.getPrivilegeNames(filter);
     }
 
     public void setSearchFilter(String searchFilter) {
        if (!searchFilter.equals(this.searchFilter)) {
            this.searchFilter = searchFilter;
            privilegeBeans = policyDao.getPrivilegeBeans(searchFilter);
        }
     }
 }
