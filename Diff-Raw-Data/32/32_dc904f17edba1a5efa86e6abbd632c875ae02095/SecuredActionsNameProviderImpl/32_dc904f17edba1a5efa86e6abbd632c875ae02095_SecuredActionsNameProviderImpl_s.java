 package com.clouway.jobex.shared;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Adelin Ghanayem adelin.ghanaem@clouway.com
  */
 public class SecuredActionsNameProviderImpl implements SecuredActionsNamesProvider {
 
   @Override
   public List<String> getUserActions() {
     ArrayList<String> actionsNames = new ArrayList<String>();
     setCommonLinks(actionsNames);
     actionsNames.add(Permissions.PREVIEW_CV);
     actionsNames.add(Permissions.CREATE_CV);
     actionsNames.add(Permissions.EDIT_CV);
     actionsNames.add(Permissions.APPLY_FOR_JOB);
     return actionsNames;
   }
 
   private void setCommonLinks(ArrayList<String> actionsNames) {
    actionsNames.add(Permissions.NEW_REGISTRATION);
     actionsNames.add(Permissions.HOME);
   }
 
   @Override
   public List<String> getCompanyActions() {
     ArrayList<String> actionsNames = new ArrayList<String>();
     setCommonLinks(actionsNames);
     actionsNames.add(Permissions.ANNOUNCE_JOB);
     actionsNames.add(Permissions.APPROVE_CV);
     actionsNames.add(Permissions.PREVIEW_APPLIED_CVS);
     actionsNames.add(Permissions.PREVIEW_JOBS);
     return actionsNames;
   }
 
 
 
 }
