 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.util;
 import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.ext.*;
 import java.util.prefs.Preferences;
 import java.util.*;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Persists user preferences using os specific mechanism. 
  * 
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>, Anwar Ahmad
  */
 public class UserPreferences {
 
   Preferences prefs = Preferences.userRoot().node("UMLLOADER");
   Set<UserPreferencesListener> userPrefsListeners = new HashSet(); 
   private UserSelections userSelections = UserSelections.getInstance();
   private static UserPreferences instance = new UserPreferences();
 
   private CadsrModule privateCadsrModule, publicCadsrModule;
 
   private List<CadsrModuleListener> cadsrModuleListeners;
 
   private Logger logger = Logger.getLogger(UserPreferences.class.getName());
 
 
   private UserPreferences() {}
   public static UserPreferences getInstance() {
     return instance;
   }
 
   public void setPrivateCadsrModule(CadsrModule m) {
     privateCadsrModule = m;
   }
   public void setPublicCadsrModule(CadsrModule m) {
     publicCadsrModule = m;
   }
 
   
   public boolean isUsePrivateApi() {
     return new Boolean(prefs.get("isUsePrivateApi", "false"));
   }
 
   public void setUsePrivateApi(boolean b) 
   {
     prefs.put("isUsePrivateApi", b?"true":"false");
     
     logger.info("Will be using the " + (b?"private":"public") + " api from now on");
 
     if(cadsrModuleListeners != null)
       for(CadsrModuleListener l : cadsrModuleListeners) {
         l.setCadsrModule(b?privateCadsrModule:publicCadsrModule);
       }
   }
   
   public int getEvsResultsPerPage() 
   {
     String s = prefs.get("EvsResults", "5");
     try
     {
       int n = Integer.decode(s);
       return n;
     }
     catch (NumberFormatException e)
     {}
     return 5;
     
     
   }
   
   public void setEvsResultsPerPage(int value) 
   {
     prefs.put("EvsResults", new Integer(value).toString());
   }
   
   public int getCadsrResultsPerPage() 
   {
     String s = prefs.get("CadsrResults", "25");
     try
     {
       int n = Integer.decode(s);
       return n;
     }
     catch (NumberFormatException e)
     {}
     return 25;
     
     
   }
   
   public void setCadsrResultsPerPage(int value) 
   {
     prefs.put("CadsrResults", new Integer(value).toString());
   }
   
   public String getOrderOfConcepts() 
   {
     return prefs.get("OrderOfConcepts", "last");
   }
   
   public void setOrderOfConcepts(String value) 
   {
     prefs.put("OrderOfConcepts", value);
     UserPreferencesEvent event = new UserPreferencesEvent(UserPreferencesEvent.ORDER_CONCEPTS, value);
     fireUserPreferencesEvent(event);
   }
 
   public String getViewAssociationType() 
   {
     return prefs.get("ViewAssociations", "false");
   }
   
   public void setViewAssociationType(String value) 
   {
     prefs.put("ViewAssociations", value);
     UserPreferencesEvent event = new UserPreferencesEvent(UserPreferencesEvent.VIEW_ASSOCIATION, value);
     fireUserPreferencesEvent(event);
   }
 
   public String getUmlDescriptionOrder() 
   {
     return prefs.get("UmlDescriptionOrder", "first");
   }
   
   public void setUmlDescriptionOrder(String value) 
   {
     prefs.put("UmlDescriptionOrder", value);
     UserPreferencesEvent event = new UserPreferencesEvent(UserPreferencesEvent.UML_DESCRIPTION, value);
     fireUserPreferencesEvent(event);
   }
   
   public boolean getShowInheritedAttributes() 
   {
     return prefs.getBoolean("showInheritedAttributes", false);
   }
   
   public void setShowInheritedAttributes(boolean show) 
   {
    prefs.putBoolean("showInheritedAttributes", show);
     UserPreferencesEvent event = 
       new UserPreferencesEvent(UserPreferencesEvent.SHOW_INHERITED_ATTRIBUTES,(Boolean.valueOf(show)).toString());
     fireUserPreferencesEvent(event);
   }
 
   public boolean getEvsAutoSearch() 
   {
     return prefs.getBoolean("evsAutoSearch", true);
   }
   
   public void setEvsAutoSeatch(boolean autoSearch) 
   {
     prefs.putBoolean("evsAutoSearch", autoSearch);
 //     UserPreferencesEvent event = new UserPreferencesEvent(UserPreferencesEvent.UML_DESCRIPTION, value);
 //     fireUserPreferencesEvent(event);
   }
 
   
   public String getModeSelection() 
   {
     return prefs.get("ModeSelection", RunMode.GenerateReport.toString());    
   }
   
   public void setModeSelection(String value) 
   {
     prefs.put("ModeSelection", value);
   }
 
   public String getRecentDir() {
     UserSelections selections = UserSelections.getInstance();
     RunMode runMode = (RunMode)(selections.getProperty("MODE"));
 
     return prefs.get(runMode.toString() + "-recentDir","/");
   }
 
   public void setRecentDir(String dir) {
     UserSelections selections = UserSelections.getInstance();
     RunMode runMode = (RunMode)(selections.getProperty("MODE"));
 
     prefs.put(runMode.toString() + "-recentDir",dir);
   }
 
   public List<String> getRecentFiles() {
     UserSelections selections = UserSelections.getInstance();
     RunMode runMode = (RunMode)(selections.getProperty("MODE"));
 
     if(runMode == null) {
       return new ArrayList();
     }
     String s = prefs.get(runMode.toString() + "-recentFiles", "");
     if(StringUtil.isEmpty(s))
       return new ArrayList();
     
     return new ArrayList(Arrays.asList(s.split("\\$\\$")));
 
   }
 
   public void addRecentFile(String filePath) {
     UserSelections selections = UserSelections.getInstance();
     RunMode runMode = (RunMode)(selections.getProperty("MODE"));
 
     List<String> files = getRecentFiles();
     
     if(!files.contains(filePath)) {
       if(files.size() > 4)
         files.remove(0);
       files.add(filePath);
     } else {
       if(files.size() > 4)
         files.remove(0);
       files.remove(filePath);
       files.add(filePath);
     }
     
     StringBuilder sb = new StringBuilder();
     for(String s : files) {
       if(sb.length() > 0)
         sb.append("$$");
       sb.append(s);
     }
     prefs.put(runMode.toString() + "-recentFiles", sb.toString());
   }
 
   private void fireUserPreferencesEvent(UserPreferencesEvent event) 
   {
     for(UserPreferencesListener l : userPrefsListeners)
       l.preferenceChange(event);
   }
 
   public void addUserPreferencesListener(UserPreferencesListener listener) 
   {
     userPrefsListeners.add(listener);
   }
 
   public void setUserSelections(UserSelections us) {
     userSelections = us;
   }
 
   
   public void setCadsrModuleListeners(List<CadsrModuleListener> listeners ) {
     cadsrModuleListeners = listeners;
   }
 
 }
