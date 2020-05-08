 package com.sun.identity.admin.model;
 
 public class ConditionsPolicySummary extends PolicySummary {
 
     public ConditionsPolicySummary(PolicyWizardBean policyWizardBean) {
         super(policyWizardBean);
     }
 
     public String getLabel() {
         // TODO: localize
         return "Conditions";
     }
 
    private int getCount() {
        ViewCondition vc = getPolicyWizardBean().getPrivilegeBean().getViewCondition();
        if (vc == null) {
            return 0;
        }
        return vc.getSizeLeafs();
    }

     public String getValue() {
        int count = getCount();

        return Integer.toString(count);
     }
 
     public boolean isExpandable() {
        int count = getCount();
        return count > 0;
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
