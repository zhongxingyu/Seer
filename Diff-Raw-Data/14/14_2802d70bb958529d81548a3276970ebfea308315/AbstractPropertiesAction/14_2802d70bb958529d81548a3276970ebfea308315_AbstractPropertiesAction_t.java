 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package com.adito.properties.actions;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import com.adito.boot.AbstractPropertyKey;
 import com.adito.boot.PropertyClass;
 import com.adito.boot.PropertyDefinition;
 import com.adito.boot.PropertyDefinitionCategory;
 import com.adito.boot.PropertyList;
 import com.adito.core.CoreUtil;
 import com.adito.core.actions.AuthenticatedDispatchAction;
 import com.adito.properties.Property;
 import com.adito.properties.PropertyItem;
 import com.adito.properties.PropertyItemImpl;
 import com.adito.properties.forms.AbstractPropertiesForm;
 import com.adito.properties.forms.PropertiesForm;
 import com.adito.security.LogonControllerFactory;
 import com.adito.security.SessionInfo;
 import com.adito.security.User;
 
 /**
  */
 public abstract class AbstractPropertiesAction extends AuthenticatedDispatchAction {
     static Log log = LogFactory.getLog(AbstractPropertiesAction.class);
 
     /**
      *
      */
     public AbstractPropertiesAction() {
         super();
     }
 
     /**
      * Reset the properties
      *
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return ActionForward
      * @throws Exception
      */
     public ActionForward reset(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
                     throws Exception {
         AbstractPropertiesForm f = (AbstractPropertiesForm) form;
         f.clearValues();
         User user = isSetupMode() ? null : LogonControllerFactory.getInstance().getUser((HttpServletRequest) request);
         return rebuildItems(mapping, f.getParentCategory(), f, request, user);
     }
 
     public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
         // Initialise form
         AbstractPropertiesForm pf = (AbstractPropertiesForm) form;
         pf.clearValues();
         pf.setUpdateAction(mapping.getPath() + ".do");
         pf.setInput(mapping.getInput());
 
         // Now try the struts supplied action mapping parameter
         if (mapping.getParameter() != null && !mapping.getParameter().equals("")) {
             PropertyList pl = new PropertyList(mapping.getParameter());
             Properties pr = pl.getAsNameValuePairs();
             BeanUtils.populate(pf, pr);
         }
 
         if ("changeSelectedCategory".equalsIgnoreCase(pf.getActionTarget())) {
             pf.setSelectedCategory(pf.getNewSelectedCategory());
         }
 
         // Build and display
         return rebuildItems(mapping, pf.getParentCategory(), pf, request, getSessionInfo(request).getUser());
     }
 
     /**
      * Change the selected category
      *
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return ActionForward
      * @throws Exception
      */
     public ActionForward changeSelectedCategory(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
         if (log.isDebugEnabled())
             log.debug("Storing properties");
         AbstractPropertiesForm f = (AbstractPropertiesForm) form;
         f.storeItems();
         User user = isSetupMode() ? null : LogonControllerFactory.getInstance().getUser((HttpServletRequest) request);
         f.setSelectedCategory(f.getNewSelectedCategory());
         f.setNewSelectedCategory(-1);
         return rebuildItems(mapping, f.getParentCategory(), f, request, user);
     }
 
     public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
                     throws Exception {
         AbstractPropertiesForm pf = (AbstractPropertiesForm) form;
         User user = isSetupMode() ? null : LogonControllerFactory.getInstance().getUser((HttpServletRequest) request);
         pf.setSelectedCategory(-1);
         pf.clearValues();
         int newCategory = pf.popCategory();
         pf.setParentCategory(newCategory);
         ActionForward fwd = rebuildItems(mapping, newCategory, pf, request, user);
         ActionForward cancel = mapping.findForward("cancel");
         return cancel != null ? cancel : fwd;
     }
 
     /**
      * Display category
      *
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return ActionForward
      * @throws Exception
      */
     public ActionForward displayCategory(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {
         AbstractPropertiesForm pf = (AbstractPropertiesForm) form;
         User user = isSetupMode() ? null : LogonControllerFactory.getInstance().getUser((HttpServletRequest) request);
         pf.setSelectedCategory(-1);
         pf.pushCategory(pf.getParentCategory());
         pf.setParentCategory(pf.getNewSelectedCategory());
         pf.setNewSelectedCategory(-1);
         return rebuildItems(mapping, pf.getParentCategory(), pf, request, user);
     }
 
     /**
      * Commit any changed properties.
      *
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return ActionForward
      * @throws Exception
      */
     public ActionForward commit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
                     throws Exception {
         SessionInfo sessionInfo = getSessionInfo(request);
         boolean restartRequired = false;
 
         // Temporarily store the properties
         if (log.isDebugEnabled())
             log.debug("Commiting properties");
         AbstractPropertiesForm f = (AbstractPropertiesForm) form;
         f.storeItems();
 
         // Check for save ability
         if (!((PropertiesForm) form).getEnabled()) {
             throw new Exception("Disabled.");
         }
 
         // Set the properties
         String oldVal, newVal;
         PropertyDefinition def;
         for (Iterator i = f.storedItems(); i.hasNext();) {
             PropertyItem item = (PropertyItem) i.next();
             def = item.getDefinition();
            newVal = String.valueOf(item.getPropertyValue());
             if (log.isDebugEnabled())
                 log.debug("Setting '" + def.getName() + "' to '" + newVal + "'");
             oldVal = Property.setProperty(createKey(def, f, sessionInfo), newVal, sessionInfo);
             if ((oldVal == null && newVal != null) || !oldVal.equals(newVal)) {
                 if (def.isRestartRequired()) {
                     restartRequired = true;
                 }
             }
         }
         CoreUtil.resetMainNavigation(request.getSession());
 
         // Clean up and forward
         f.clearValues();
         ActionForward fwd;
         if (f.getForwardTo() != null && !f.getForwardTo().equals("")) {
             fwd = new ActionForward(f.getForwardTo(), f.isRedirect());
         } else {
             fwd = cancel(mapping, form, request, response);
         }
         if (restartRequired) {
             String orig = fwd.getPath();
             fwd = mapping.findForward("restartRequired");
             fwd = CoreUtil.addParameterToForward(fwd, "no", orig);
         }
         return fwd;
     }
 
     protected String getMethodName(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                    HttpServletResponse response, String parameter) throws Exception {
         return request.getParameter("actionTarget");
     }
 
     protected ActionForward rebuildItems(ActionMapping mapping, int parentCategory, AbstractPropertiesForm pf,
                                          HttpServletRequest request, User user) throws Exception {
         SessionInfo sessionInfo = getSessionInfo(request);
         List<PropertyDefinitionCategory> categoryDefinitions = new ArrayList<PropertyDefinitionCategory>();
         List<PropertyDefinitionCategory> subCategories = new ArrayList<PropertyDefinitionCategory>();
         Collection<PropertyDefinitionCategory> sourceCategories = null;
         List<PropertyItemImpl> propertyItemImpls = new ArrayList<PropertyItemImpl>();
         for (PropertyClass propertyClass : pf.getPropertyClasses()) {
 
             /*
              * If no parent category is supplied, then assume all categories in
              * the class, otherwise get all the child categories of the supplied
              * one
              */
             if (parentCategory == 0) {
                 sourceCategories = propertyClass.getCategories();
             } else {
                 PropertyDefinitionCategory category = propertyClass.getPropertyDefinitionCategory(parentCategory);
                 if (category != null) {
                     sourceCategories = category.getCategories();
                 } else {
                     sourceCategories = null;
                 }
             }
 
             if (sourceCategories != null) {
                 for (PropertyDefinitionCategory def : sourceCategories) {
                     if (def.isEnabled()) {
                         if (def.size() > 0) {
                             if (!subCategories.contains(def)) {
                                 // Only add the subcategory if it has at least
                                 // one enabled subcategory within it
                                 for (PropertyDefinitionCategory subcat : def.getCategories()) {
                                     if (subcat.isEnabled()) {
                                         subCategories.add(def);
                                         break;
                                     }
                                 }
                             }
                         } else {
                             if (!categoryDefinitions.contains(def))
                                 for (PropertyDefinition propertyDefinition : propertyClass.getDefinitions()) {
                                     if (!propertyDefinition.isHidden() && includePropertyDefinition(propertyDefinition, request)
                                                     && propertyDefinition.getCategory() == def.getId()) {
                                         categoryDefinitions.add(def);
                                         if (pf.getSelectedCategory() == -1) {
                                             pf.setSelectedCategory(def.getId());
                                         }
                                         break;
                                     }
                                 }
                         }
                     }
                 }
             }
 
             for (PropertyDefinition propertyDefinition : propertyClass.getDefinitions()) {
                 if (!propertyDefinition.isHidden() && propertyDefinition.getCategory() == pf.getSelectedCategory()) {
                     if (includePropertyDefinition(propertyDefinition, request)) {
                         propertyItemImpls.add(pf.retrieveItem(propertyDefinition.getName(), new PropertyItemImpl(request,
                                         propertyDefinition, Property.getProperty(createKey(propertyDefinition, pf, sessionInfo)))));
                     }
                 }
             }
             pf.setParentCategory(parentCategory);
             pf.setSubCategories(subCategories);
             pf.setCategoryDefinitions(categoryDefinitions);
             Collections.sort(propertyItemImpls);
             pf.setPropertyItems(propertyItemImpls.toArray(new PropertyItemImpl[propertyItemImpls.size()]));
         }
         if (propertyItemImpls.size() != 0 || subCategories.size() != 0) {
             return mapping.findForward("display");
         } else {
             log.warn("No categories or definitions to display. May be the result of a session timeout.");
             return mapping.findForward("home");
         }
     }
 
     protected boolean includePropertyDefinition(PropertyDefinition definition, HttpServletRequest request) {
         return true;
     }
 
     public abstract AbstractPropertyKey createKey(PropertyDefinition definition, AbstractPropertiesForm form,
                                                   SessionInfo sessionInfo);
 }
