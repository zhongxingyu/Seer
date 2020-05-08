 package com.sun.identity.admin.model;
 
 import com.icesoft.faces.context.effects.Effect;
 import com.sun.identity.admin.DeepCloneableArrayList;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.model.SelectItem;
 
 public class PolicyCreateWizardBean
         extends WizardBean
        implements Serializable, PolicyNameBean {
 
     private PrivilegeBean privilegeBean = new PrivilegeBean();
     
     private ViewApplicationsBean viewApplicationsBean;
     private Effect dropConditionEffect;
     private Effect dropSubjectContainerEffect;
     private Effect policyNameInputEffect;
     private Effect policyNameMessageEffect;
     private PolicyCreateSummary policyCreateSummary = new PolicyCreateSummary();
     private int advancedTabsetIndex = 0;
     private List<Resource> availableResources;
     private BooleanActionsBean booleanActionsBean = new BooleanActionsBean();
 
     public PolicyCreateWizardBean() {
         policyCreateSummary.setPolicyCreateWizardBean(this);
         booleanActionsBean.setActions(privilegeBean.getViewEntitlement().getActions());
     }
 
     @Override
     public void reset() {
         super.reset();
         privilegeBean = new PrivilegeBean();
     }
 
     public List<SelectItem> getViewApplicationItems() {
         List<SelectItem> items = new ArrayList<SelectItem>();
 
         for (ViewApplication va : getViewApplicationsBean().getViewApplications().values()) {
             items.add(new SelectItem(va.getName()));
         }
 
         return items;
     }
 
     public List<SelectItem> getAvailableResourceItems() {
         List<SelectItem> items = new ArrayList<SelectItem>();
 
         for (Resource r: availableResources) {
             items.add(new SelectItem(r, r.getName()));
         }
 
         return items;
     }
 
     public Effect getDropConditionEffect() {
         return dropConditionEffect;
     }
 
     public void setDropConditionEffect(Effect dropConditionEffect) {
         this.dropConditionEffect = dropConditionEffect;
     }
 
     public Effect getDropSubjectContainerEffect() {
         return dropSubjectContainerEffect;
     }
 
     public void setDropSubjectContainerEffect(Effect dropSubjectContainerEffect) {
         this.dropSubjectContainerEffect = dropSubjectContainerEffect;
     }
 
     public PolicyCreateSummary getPolicyCreateSummary() {
         return policyCreateSummary;
     }
 
     public int getAdvancedTabsetIndex() {
         return advancedTabsetIndex;
     }
 
     public void setAdvancedTabsetIndex(int advancedTabsetIndex) {
         this.advancedTabsetIndex = advancedTabsetIndex;
     }
 
     public PrivilegeBean getPrivilegeBean() {
         return privilegeBean;
     }
 
     public Effect getPolicyNameMessageEffect() {
         return policyNameMessageEffect;
     }
 
     public void setPolicyNameMessageEffect(Effect policyNameMessageEffect) {
         this.policyNameMessageEffect = policyNameMessageEffect;
     }
 
     public Effect getPolicyNameInputEffect() {
         return policyNameInputEffect;
     }
 
     public void setPolicyNameInputEffect(Effect policyNameInputEffect) {
         this.policyNameInputEffect = policyNameInputEffect;
     }
 
     public List<Resource> getAvailableResources() {
         return availableResources;
     }
 
     public void setAvailableResources(List<Resource> availableResources) {
         this.availableResources = availableResources;
     }
 
     public ViewApplication getViewApplication() {
         return privilegeBean.getViewEntitlement().getViewApplication();
     }
 
     public void setViewApplication(ViewApplication viewApplication) {
         privilegeBean.getViewEntitlement().setViewApplication(viewApplication);
 
         getPrivilegeBean().getViewEntitlement().getActions().addAll(new DeepCloneableArrayList<Action>(viewApplication.getActions()).deepClone());
         availableResources = new DeepCloneableArrayList<Resource>(viewApplication.getResources()).deepClone();
     }
 
     public void setViewApplicationsBean(ViewApplicationsBean viewApplicationsBean) {
         this.viewApplicationsBean = viewApplicationsBean;
     }
 
     public ViewApplicationsBean getViewApplicationsBean() {
         return viewApplicationsBean;
     }
 
     public BooleanActionsBean getBooleanActionsBean() {
         return booleanActionsBean;
     }
 }
