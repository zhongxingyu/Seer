 package com.sun.identity.admin.model;
 
 import com.icesoft.faces.context.effects.Effect;
 import com.sun.identity.admin.DeepCloneableArrayList;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import javax.faces.model.SelectItem;
 
 public class PolicyWizardBean
         extends WizardBean
         implements Serializable, PolicyNameBean, PolicyResourcesBean,
         PolicySubjectsBean, PolicyConditionsBean, PolicySummaryBean {
 
     private PrivilegeBean privilegeBean = new PrivilegeBean();
     private ViewApplicationsBean viewApplicationsBean;
     private Effect dropConditionEffect;
     private Effect dropSubjectContainerEffect;
     private Effect policyNameInputEffect;
     private Effect policyNameMessageEffect;
     private int advancedTabsetIndex = 0;
     private boolean policyNameEditable = false;
     private List<ConditionType> conditionTypes;
     private List<SubjectType> subjectTypes;
 
     public PolicyWizardBean() {
         // nothing
     }
 
     @Override
     public void reset() {
         super.reset();
         setPrivilegeBean(new PrivilegeBean());
 
         ConditionType oct = getConditionType("or");
         privilegeBean.setViewCondition(oct.newViewCondition());
         SubjectType ost = getSubjectType("or");
         privilegeBean.setViewSubject(ost.newViewSubject());
     }
 
     public List<SelectItem> getViewApplicationNameItems() {
         List<SelectItem> items = new ArrayList<SelectItem>();
 
         for (ViewApplication va : getViewApplicationsBean().getViewApplications().values()) {
             items.add(new SelectItem(va.getName(), va.getTitle()));
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
 
     public ViewApplication getViewApplication() {
         if (getPrivilegeBean().getViewEntitlement().getViewApplication() == null) {
             resetViewApplication();
         }
         return getPrivilegeBean().getViewEntitlement().getViewApplication();
     }
 
     public void setViewApplicationName(String viewApplicationName) {
         if (getViewApplicationName() == null || !viewApplicationName.equals(getViewApplicationName())) {
            reset();

             ViewApplication va = viewApplicationsBean.getViewApplications().get(viewApplicationName);
             getPrivilegeBean().getViewEntitlement().setViewApplication(va);
 
             getPrivilegeBean().getViewEntitlement().getBooleanActionsBean().getActions().clear();
             getPrivilegeBean().getViewEntitlement().getBooleanActionsBean().getActions().addAll(new DeepCloneableArrayList<Action>(va.getActions()).deepClone());
         }
     }
 
     private void resetViewApplication() {
         Map<String, ViewApplication> viewApplicationMap = viewApplicationsBean.getViewApplications();
         Collection<ViewApplication> viewApplications = (Collection<ViewApplication>) viewApplicationMap.values();
         if (viewApplications != null && viewApplications.size() > 0) {
             setViewApplicationName(viewApplications.iterator().next().getName());
         }
     }
 
     public void setViewApplicationsBean(ViewApplicationsBean viewApplicationsBean) {
         this.viewApplicationsBean = viewApplicationsBean;
         resetViewApplication();
     }
 
     public ViewApplicationsBean getViewApplicationsBean() {
         return viewApplicationsBean;
     }
 
     public void setPrivilegeBean(PrivilegeBean privilegeBean) {
         this.privilegeBean = privilegeBean;
     }
 
     public String getViewApplicationName() {
         if (getPrivilegeBean().getViewEntitlement().getViewApplication() == null) {
             return null;
         }
         return getPrivilegeBean().getViewEntitlement().getViewApplication().getName();
     }
 
     public boolean isPolicyNameEditable() {
         return policyNameEditable;
     }
 
     public void setPolicyNameEditable(boolean policyNameEditable) {
         this.policyNameEditable = policyNameEditable;
     }
 
     public void setSubjectTypes(List<SubjectType> subjectTypes) {
         this.subjectTypes = subjectTypes;
     }
 
     public SubjectType getSubjectType(String name) {
         for (SubjectType st: subjectTypes) {
             if (st.getName().equals(name)) {
                 return st;
             }
         }
         return null;
     }
 
     public void setConditionTypes(List<ConditionType> conditionTypes) {
         this.conditionTypes = conditionTypes;
     }
 
     public ConditionType getConditionType(String name) {
         for (ConditionType ct: conditionTypes) {
             if (ct.getName().equals(name)) {
                 return ct;
             }
         }
         return null;
     }
 }
