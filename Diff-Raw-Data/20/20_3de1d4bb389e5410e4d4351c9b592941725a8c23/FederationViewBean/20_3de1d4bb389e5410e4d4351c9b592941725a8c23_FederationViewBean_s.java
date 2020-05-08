 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: FederationViewBean.java,v 1.3 2007-06-29 19:50:19 jonnelson Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.console.federation;
 
 import com.iplanet.jato.model.ModelControlException;
 import com.iplanet.jato.RequestManager;
 import com.iplanet.jato.RequestContext;
 import com.iplanet.jato.view.View;
 import com.iplanet.jato.view.event.DisplayEvent;
 import com.iplanet.jato.view.event.RequestInvocationEvent;
 import com.iplanet.jato.view.html.OptionList;
 
 import com.sun.identity.console.base.AMPropertySheet;
 import com.sun.identity.console.base.AMPrimaryMastHeadViewBean;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMModel;
 import com.sun.identity.console.base.model.AMModelBase;
 import com.sun.identity.console.base.model.AMPropertySheetModel;
 import com.sun.identity.console.components.view.html.SerializedField;
 import com.sun.identity.console.federation.model.EntityModel;
 import com.sun.identity.console.federation.model.EntityModelImpl;
 import com.sun.identity.console.federation.model.FSAuthDomainsModel;
 import com.sun.identity.console.federation.model.FSAuthDomainsModelImpl;
 import com.sun.identity.cot.CircleOfTrustDescriptor;
 import com.sun.identity.shared.datastruct.OrderedSet;
 
 import com.sun.web.ui.model.CCActionTableModel;
 import com.sun.web.ui.view.alert.CCAlert;
 import com.sun.web.ui.view.table.CCActionTable;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 public  class FederationViewBean
         extends AMPrimaryMastHeadViewBean 
 {
     public static final String DEFAULT_DISPLAY_URL =
         "/console/federation/Federation.jsp";
     public static final String MESSAGE_TEXT = "displayMessage";    
     protected static final String PROPERTY_ATTRIBUTE = "propertyAttributes";   
     private boolean tablePopulated = false;
     private boolean initialized = false; 
     
     private AMPropertySheetModel propertySheetModel;
     
     // cot table properties
     private static final String COT_TABLE = "cotTable";
     private static final String COT_NAME_VALUE = "cotNameValue";
     private static final String COT_NAME_HREF = "cotNameHref";
     private static final String COT_ENTITY_VALUE = "cotEntityValue";
     private static final String COT_REALM_VALUE = "realmValue";
     private static final String COT_STATUS_VALUE = "statusValue";
         
     // entity table properties
     private static final String ENTITY_TABLE = "entityTable";
     private static final String ENTITY_NAME_VALUE = "entityNameValue";
     private static final String ENTITY_NAME_HREF = "entityNameHref";
     private static final String ENTITY_PROTOCOL_VALUE = "protocolValue";
     private static final String ENTITY_ROLE_VALUE = "roleValue";
     private static final String ENTITY_LOCATION_VALUE = "locationValue";
     
     // SAML Configuration table
     private static final String SAML_TABLE = "samlTable";
     
     /**
      * Creates a authentication domains view bean.
      */
     public FederationViewBean() {
         super("Federation");
         setDefaultDisplayURL(DEFAULT_DISPLAY_URL);
     }
     
     protected void initialize()  {
         if (!initialized) {
             initialized = true;
             super.initialize();
             createPropertyModel();
             createCOTTable();
             createEntityTable();
             createSAMLTable();
             registerChildren();
         }
     }
     
     protected void registerChildren() {
         super.registerChildren();
         registerChild(PROPERTY_ATTRIBUTE, AMPropertySheet.class);
         propertySheetModel.registerChildren(this);     
     }
     
     protected View createChild(String name) {        
         if (!tablePopulated) {
             populateCOTTable();
             populateEntityTable();                     
             populateSAMLTable();
         }
         View view = null;
         
         if (name.equals(PROPERTY_ATTRIBUTE)) {
             view = new AMPropertySheet(this, propertySheetModel, name);
         } else if (propertySheetModel.isChildSupported(name)) {
             view = propertySheetModel.createChild(this, name, getModel());     
         } else {
             view = super.createChild(name);
         }
         return view;
     }
     
     public void beginDisplay(DisplayEvent event)
         throws ModelControlException 
     {
         super.beginDisplay(event);
         resetButtonState("deleteCOTButton");
         resetButtonState("deleteEntityButton");
         resetButtonState("deleteTPButton");
         
         AMPropertySheet ps =
             (AMPropertySheet)getChild(PROPERTY_ATTRIBUTE);
         
         populateCOTTable();
         populateEntityTable();
         populateSAMLTable();
 
 // TBD Enable this to display 'success' messages in the page
 // after and on object was created (entity provider, COT, SAML)
 //        String msg = (String)removePageSessionAttribute(MESSAGE_TEXT);
 //        if (msg != null) {
 //            setInlineAlertMessage(
 //                CCAlert.TYPE_INFO, "message.information", msg);
 //        }
     }
     
     protected AMModel getModelInternal() {
         HttpServletRequest req = getRequestContext().getRequest();
         return new FSAuthDomainsModelImpl(req, getPageSessionAttributes());
     }
     
     private EntityModel getEntityModel() {
         RequestContext rc = RequestManager.getRequestContext();
         HttpServletRequest req = rc.getRequest();
         return new EntityModelImpl(req, getPageSessionAttributes()  );
     }
     
     private void populateSAMLTable() { 
         tablePopulated=true;
         //TBD;
     }
     
     private void populateEntityTable() {
         tablePopulated=true;
         CCActionTableModel tableModel = (CCActionTableModel)
             propertySheetModel.getModel(ENTITY_TABLE);
         tableModel.clearAll();
 
         EntityModel eModel = getEntityModel();
         Map entities = Collections.EMPTY_MAP;
         try {
             entities = eModel.getEntities();
         } catch (AMConsoleException e) {
             debug.error("FederationViewBean.populateEntityTable", e);
             return;
         }
         
         List entityList = new ArrayList(entities.size()*2);
         boolean firstRow = true;
         for (Iterator i = entities.keySet().iterator(); i.hasNext();) {
             if (!firstRow) {
                 tableModel.appendRow();
             } else {
                 firstRow = false;
             }
             String name = (String)i.next();
             
             tableModel.setValue(ENTITY_NAME_HREF, name);
             tableModel.setValue(ENTITY_NAME_VALUE, name);
             
             Map data = (Map)entities.get(name);
             String protocol = (String)data.get(EntityModel.PROTOCOL);
             tableModel.setValue(ENTITY_PROTOCOL_VALUE, protocol);
             tableModel.setValue(
                 ENTITY_ROLE_VALUE, (String)data.get(eModel.ROLE));
             tableModel.setValue(
                 ENTITY_LOCATION_VALUE, (String)data.get(eModel.LOCATION));       
 
             entityList.add(name+"|"+protocol);
         }
                 
         // set the instances in the page session so when a request comes in
         // we can prepopulate the table model.
         setPageSessionAttribute(ENTITY_TABLE,(Serializable)entityList);
     }
     
     private void populateCOTTable() {
         tablePopulated=true;
         FSAuthDomainsModel model = (FSAuthDomainsModel)getModel();    
         Set CircleOfTrustDescriptors = model.getCircleOfTrustDescriptors();                
                 
         CCActionTableModel tableModel = (CCActionTableModel)
             propertySheetModel.getModel(COT_TABLE);        
         tableModel.clearAll();
         
         SerializedField szCache = (SerializedField)getChild(SZ_CACHE);        
         
         if ((CircleOfTrustDescriptors != null)
             && (!CircleOfTrustDescriptors.isEmpty())) 
         {
             List cache = new ArrayList(CircleOfTrustDescriptors.size());
             boolean first = true;            
             for (Iterator iter = CircleOfTrustDescriptors.iterator();
             iter.hasNext(); ) {
                 if (first) {
                     first = false;
                 } else {
                     tableModel.appendRow();
                 }                
                 CircleOfTrustDescriptor desc =
                         (CircleOfTrustDescriptor)iter.next();
                 String name = desc.getCircleOfTrustName();
                 tableModel.setValue(COT_NAME_VALUE, name);
                 tableModel.setValue(COT_NAME_HREF, name);
                 
                 // get entity/provider name                
                 Set entitySet = desc.getTrustedProviders();
                 if ((entitySet != null) && (!entitySet.isEmpty())) {
                     Iterator it = entitySet.iterator();
                     StringBuffer sb = new StringBuffer();                    
                     while (it.hasNext()) {
                         String entity = (String) it.next();
                         sb.append(entity).append("<br>");
                     }                    
                     tableModel.setValue(COT_ENTITY_VALUE, sb.toString());
                 } else {
                     tableModel.setValue(COT_ENTITY_VALUE, "");
                 }
                 
                 // get realm name
                 String realm = desc.getCircleOfTrustRealm();
                 tableModel.setValue(COT_REALM_VALUE, getPath(realm));
                                
                 // get cot status
                 String status = desc.getCircleOfTrustStatus();
                 tableModel.setValue(COT_STATUS_VALUE, status);                               
                 cache.add(name + "|" + realm);                
             }            
             szCache.setValue((ArrayList)cache);
         } else {
             szCache.setValue(null);
         }      
     }
     
     private void createPropertyModel() {
         propertySheetModel = new AMPropertySheetModel(
             getClass().getClassLoader().getResourceAsStream(
                 "com/sun/identity/console/propertyFederationView.xml"));       
         propertySheetModel.clear();
     }
     
     /*
      * Responsible for creating the circle of trust table.
      */
     private void createCOTTable() {
         CCActionTableModel tableModel = new CCActionTableModel(
             getClass().getClassLoader().getResourceAsStream(
                 "com/sun/identity/console/cotTable.xml"));        
         tableModel.setMaxRows(getModel().getPageSize());
         tableModel.setTitleLabel("label.items");
         tableModel.setActionValue("addCOTButton", "cot.new.button");
         tableModel.setActionValue("deleteCOTButton", "cot.delete.button");
         tableModel.setActionValue("cotNameColumn", "cot.name.column.label");
         tableModel.setActionValue("cotEntityColumn", "cot.entity.column.label");
         tableModel.setActionValue("realmColumn", "cot.realm.column.label");
         tableModel.setActionValue("statusColumn", "cot.status.column.label");        
         propertySheetModel.setModel(COT_TABLE, tableModel);
     }
     
     /*
      * Responsible for creating the entity table.
      */
     private void createEntityTable() {
         CCActionTableModel tableModel = new CCActionTableModel(
             getClass().getClassLoader().getResourceAsStream(
             "com/sun/identity/console/entityTable.xml"));
         
         tableModel.setMaxRows(getModel().getPageSize());
         tableModel.setTitleLabel("label.items");
         tableModel.setActionValue(
             "addEntityButton", "entity.new.button");        
         tableModel.setActionValue(
             "deleteEntityButton", "entity.delete.button");
         tableModel.setActionValue(
             "importEntityButton", "entity.import.button");
         tableModel.setActionValue(
             "entityNameColumn", "entity.name.column.label"); 
         tableModel.setActionValue(
             "roleColumn", "entity.role.column.label");
         tableModel.setActionValue(
             "protocolColumn", "entity.protocol.column.label");
         tableModel.setActionValue(
             "locationColumn", "entity.location.column.label");
         propertySheetModel.setModel(ENTITY_TABLE, tableModel);
     }
     
     /*
      * Responsible for creating the entity table.
      */
     private void createSAMLTable() {
         CCActionTableModel tableModel = new CCActionTableModel(
             getClass().getClassLoader().getResourceAsStream(
                 "com/sun/identity/console/samlTable.xml"));        
         tableModel.setMaxRows(getModel().getPageSize());
         tableModel.setTitleLabel("label.items");
         tableModel.setActionValue("addTPButton", "saml.new.button");
         tableModel.setActionValue("deleteTPButton", "saml.delete.button");
         tableModel.setActionValue("trustedPartnerColumn", 
             "saml.name.column.label");        
         propertySheetModel.setModel(SAML_TABLE, tableModel);
     }
     
     /*****************************************************************
      *
      * SAML Event Handlers.
      * There are four events which may be generated from the SAML section.
      * For the trusted partners there are new, delete, and edit requests.
      * There is also a handler for editing the local properties.
      *
      *****************************************************************/
     
     /**
      * Handles the new trusted partner request. There is no real processing
      * done here. We are just forwarding the request onto the view bean
      * which will do the real work.
      *
      * @param event Request Invocation Event.
      */
     public void handleAddTPButtonRequest(RequestInvocationEvent event) {
         forwardTo();
     }
     
     /**
      * Handles the delete trusted partner request. The items which are
      * selected in the SAML table will be removed. After the processing is
      * complete a message will be displayed indicating the process succeeded,
      * or what failed if it didn't succeed.
      *
      * @param event Request Invocation Event.
      */
     public void handleDeleteTPButtonRequest(RequestInvocationEvent event) {
         // TBD delete  the trusted partners selected in the SAML TABLE
         forwardTo();
     }
     
     public void handleLocalSitePropertiesRequest(RequestInvocationEvent event) {
         forwardTo();
     }
     
     /*****************************************************************
      *
      * Circle of Trust Event Handlers
      *
      *****************************************************************/  
     public void handleAddCOTButtonRequest(RequestInvocationEvent event) {        
         CreateCOTViewBean vb =
             (CreateCOTViewBean)getViewBean(CreateCOTViewBean.class);        
         unlockPageTrail();
         passPgSessionMap(vb);
         vb.forwardTo(getRequestContext());
     }
 
     public void handleDeleteCOTButtonRequest(RequestInvocationEvent event)
         throws ModelControlException 
     {        
         CCActionTable tbl = (CCActionTable)getChild(COT_TABLE);
         tbl.restoreStateData();           
         CCActionTableModel tableModel = (CCActionTableModel)
             propertySheetModel.getModel(COT_TABLE);        
         
         // get selected rows
         Integer[] selected = tableModel.getSelectedRows();        
         SerializedField szCache = (SerializedField)getChild(SZ_CACHE);
         List list = (List)szCache.getSerializedObj(); 
         
         FSAuthDomainsModel model = (FSAuthDomainsModel)getModel();
         StringBuffer successMessage = new StringBuffer();
         StringBuffer errorMessage  = new StringBuffer();
 
         // each COT is deleted separately as they can be in separate realms
         for ( int i = 0; i < selected.length; i++) {
             String str = (String)list.get(selected[i].intValue());               
             int pipeIndex = str.indexOf("|"); 
             String name = str.substring(0, pipeIndex);
             String realm = str.substring(pipeIndex+1);                 
             try {
                 model.deleteAuthenticationDomain(realm, name);  
                 if (successMessage.length() < 1) {
                     successMessage.append(
                         model.getLocalizedString("authDomain.message.deleted"))
                         .append("<ul>");                       
                 }
                 successMessage.append("<li>").append(name);
             } catch (AMConsoleException e) {        
                 if (errorMessage.length() < 1) {                                    
                     errorMessage.append(
                         model.getLocalizedString("general.error.message"))
                         .append("<ul>");
                 }                
                 errorMessage.append("<li>").append(e.getMessage());                   
             }    
         }
  
         successMessage.append("</ul>").append(errorMessage);        
         setInlineAlertMessage(CCAlert.TYPE_INFO, "message.information",
             successMessage.toString());
 
         forwardTo();        
     }
     
     public void handleCotNameHrefRequest(RequestInvocationEvent event)
         throws ModelControlException 
     {
         String name = (String)getDisplayFieldValue(COT_NAME_HREF);
         FSAuthDomainsEditViewBean vb = (FSAuthDomainsEditViewBean)
             getViewBean(FSAuthDomainsEditViewBean.class);
         unlockPageTrail();
         passPgSessionMap(vb);
         vb.setPageSessionAttribute(FSAuthDomainsModel.TF_NAME, name);
         vb.forwardTo(getRequestContext());
     }
     
     /*****************************************************************
      *
      * Entity Event handlers
      *
      ******************************************************************/    
     public void handleAddEntityButtonRequest(RequestInvocationEvent event) {
 // TBD enable when CreateEntityViewBean is done.
 //            CreateEntityViewBean vb = (CreateEntityViewBean)
 //                getViewBean(CreateEntityViewBean.class);
 //
 //            vb.forwardTo(getRequestContext());      
     }
        
     public void handleImportEntityButtonRequest(RequestInvocationEvent event) {
         ImportEntityViewBean vb = (ImportEntityViewBean)
             getViewBean(ImportEntityViewBean.class);
         unlockPageTrail();
         passPgSessionMap(vb);
         vb.forwardTo(getRequestContext()); 
     }
     
     public void handleDeleteEntityButtonRequest(RequestInvocationEvent event) 
         throws ModelControlException
     {
         CCActionTable table = (CCActionTable)getChild(ENTITY_TABLE);
         table.restoreStateData();
         
         CCActionTableModel m = (CCActionTableModel)
             propertySheetModel.getModel(ENTITY_TABLE);
         Integer[] selected = m.getSelectedRows();
         
         Map entries = new HashMap(selected.length*2);
         
         List l = (ArrayList)getPageSessionAttribute(ENTITY_TABLE);
         StringBuffer tmp = new StringBuffer(100);
       
         for (int i = 0; i < selected.length; i++) {
             String s = (String)l.get(selected[i].intValue());
            
             int pos = s.indexOf("|");
             String name = s.substring(0, pos);
             String protocol = s.substring(pos+1);            
             tmp.append(" ").append(name).append("; ");   
             Map x = new HashMap(6);
             
             entries.put(name, protocol);
         }   
         
         EntityModel model = getEntityModel();
         try {
             model.deleteEntities(entries);
             StringBuffer sb = new StringBuffer();
             sb.append(model.getLocalizedString("entity.deleted.message"));
             sb.append("<ul>");
             for (Iterator i = entries.keySet().iterator(); i.hasNext();) {
                 String key = (String)i.next();
                 sb.append("<li>").append(key);
             }
             setInlineAlertMessage(CCAlert.TYPE_INFO, "message.information",
                 sb.toString());
         } catch (AMConsoleException e) {
             setInlineAlertMessage(
                 CCAlert.TYPE_ERROR, "message.error", e.getMessage());
         }
         forwardTo();
     }
     
     public void handleEntityNameHrefRequest(RequestInvocationEvent event) {     
        EntityPropertiesViewBean vb = (EntityPropertiesViewBean)
            getViewBean(EntityPropertiesViewBean.class);
        
        String entity = (String)getDisplayFieldValue(ENTITY_NAME_HREF);
        setPageSessionAttribute("entityName", entity);        
        unlockPageTrail();
        passPgSessionMap(vb);
        vb.forwardTo(getRequestContext());
     }    
     
     /*
      * This handler is called when the dropdown menu is invoked in the 
      * entity provider table. The value in the <code>actionMenu</code>
      * field is the value selected and dictates what action should be 
      * taken.
      */
     public void handleBtnSearchRequest(RequestInvocationEvent event) {
         // use the action value to determine which view we will forward to
         String actionValue = (String)getDisplayFieldValue("actionMenu");
         if (actionValue.equals("")) {
             forwardTo();        
         }
     }
 }
