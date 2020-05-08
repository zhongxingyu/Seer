 package com.sun.identity.admin.model;
 
 public class ConditionsPolicySummary extends PolicySummary {
 
     public ConditionsPolicySummary(PolicyWizardBean policyWizardBean) {
         super(policyWizardBean);
     }
 
     public String getLabel() {
         // TODO: localize
         return "Conditions";
     }
 
     public String getValue() {
        return Integer.toString(getPolicyWizardBean().getPrivilegeBean().getViewCondition().getSizeLeafs());
     }
 
     public boolean isExpandable() {
        return getPolicyWizardBean().getPrivilegeBean().getViewCondition().getSizeLeafs() > 0;
     }
 
     public String getIcon() {
         return "../image/and.png";
     }
 
     public String getTemplate() {
         return "/admin/facelet/template/policy-summary-conditions.xhtml";
     }
 
     public PolicyWizardStep getGotoStep() {
         return PolicyWizardStep.ADVANCED;
     }
 
     @Override
     public PolicyWizardAdvancedTabIndex getAdvancedTabIndex() {
         return PolicyWizardAdvancedTabIndex.CONDITIONS;
     }
 }
