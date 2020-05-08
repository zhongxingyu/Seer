 
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
 			
 package com.adito.properties.forms;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.struts.Globals;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 
 import com.adito.boot.PropertyClass;
 import com.adito.boot.PropertyDefinition;
 import com.adito.core.CoreException;
 import com.adito.core.CoreUtil;
 import com.adito.core.EntryIterator;
 import com.adito.core.forms.CoreForm;
 import com.adito.properties.PropertyItem;
 import com.adito.properties.PropertyItemImpl;
 
 /**
  * @author Brett Smith
  */
 public class AbstractPropertiesForm extends CoreForm implements PropertiesForm {
 
     static Log log = LogFactory.getLog(AbstractPropertiesForm.class);
 
     private PropertyItem[] propertyItems;
     private List<PropertyClass> propertyClasses;
     private String forwardTo;
     private int parentCategory;
     private String input;
     private boolean redirect;
     private String updateAction;
     private List categoryDefinitions;
     private int selectedCategory;
     private HashMap store;
     private int newSelectedCategory;
     private List subCategories;
     private List path;
     private boolean supportsReplacementVariables;
 
     public AbstractPropertiesForm(List<PropertyClass> propertyClasses, boolean supportsReplacementVariables) {
         this();
         this.propertyClasses = propertyClasses;
         this.supportsReplacementVariables = supportsReplacementVariables;
     }
 
     public AbstractPropertiesForm() {
         super();
         store = new HashMap();
         path = new ArrayList();
     }
 
     public boolean isSupportsReplacementVariables() {
         return supportsReplacementVariables;
     }
 
     public boolean getEnabled() {
         return true;
     }
 
     public String getForwardTo() {
         return forwardTo;
     }
 
     public void setForwardTo(String forwardTo) {
         this.forwardTo = forwardTo;
     }
 
     public String getInput() {
         return input;
     }
 
     public void setInput(String input) {
         this.input = input;
     }
 
     public void reset(ActionMapping mapping, javax.servlet.http.HttpServletRequest request) {
         if (log.isDebugEnabled())
             log.debug("Reseting properties form");
         super.reset(mapping, request);
         if (propertyItems != null) {
             for (int i = 0; i < propertyItems.length; i++) {
                 if (propertyItems[i].getDefinition().getType() == PropertyDefinition.TYPE_BOOLEAN) {
                     propertyItems[i].setValue(Boolean.FALSE.toString());
                 } else if (propertyItems[i].getDefinition().getType() == PropertyDefinition.TYPE_LIST) {
                     propertyItems[i].setValue(propertyItems[i].getDefinition().getDefaultValue());
                 }
             }
         }
     }
 
     public String getUpdateAction() {
         return updateAction;
     }
 
     public PropertyItem[] getPropertyItems() {
         return propertyItems;
     }
 
     public void setCategoryDefinitions(List categoryDefinitions) {
         this.categoryDefinitions = categoryDefinitions;
     }
 
     public void setPropertyItems(PropertyItemImpl[] propertyItems) {
         this.propertyItems = propertyItems;
     }
 
     public void setPropertyItem(int idx, PropertyItemImpl item) {
         propertyItems[idx] = item;
     }
 
     public PropertyItem getPropertyItem(int idx) {
         return propertyItems[idx];
     }
 
     public void setPropertyClasses(List<PropertyClass> propertyClasses) {
         this.propertyClasses = propertyClasses;
     }
 
     public List<PropertyClass> getPropertyClasses() {
         return propertyClasses;
     }
 
     public void setParentCategory(int parentCategory) {
         this.parentCategory = parentCategory;
     }
 
     public int getParentCategory() {
         return parentCategory;
     }
 
     public void setRedirect(boolean redirect) {
         this.redirect = redirect;
     }
 
     public boolean isRedirect() {
         return redirect;
     }
 
     public void setUpdateAction(String updateAction) {
         this.updateAction = updateAction;
     }
 
     public List getCategoryDefinitions() {
         return categoryDefinitions;
     }
 
     public void setSelectedCategory(int selectedCategory) {
         this.selectedCategory = selectedCategory;
     }
 
     public int getSelectedCategory() {
         return selectedCategory;
     }
 
     public PropertyItemImpl retrieveItem(String name, PropertyItemImpl defaultValue) {
         PropertyItemImpl val = (PropertyItemImpl) store.get(name);
         return val == null ? defaultValue : val;
     }
 
     public void clearValues() {
         store.clear();
     }
 
     public int getNewSelectedCategory() {
         return newSelectedCategory;
     }
 
     public void setNewSelectedCategory(int newSelectedCategory) {
         this.newSelectedCategory = newSelectedCategory;
     }
 
     public List getSubCategories() {
         return subCategories;
     }
 
     public void setSubCategories(List subCategories) {
         this.subCategories = subCategories;
     }
 
     public void pushCategory(int parentCategory) {
         path.add(new Integer(parentCategory));
     }
 
     public int popCategory() {
         if (path.size() > 0) {
             int parentCategory = ((Integer) path.remove(path.size() - 1)).intValue();
             return parentCategory;
         }
         return -1;
     }
 
     public void storeItems() {
 
         PropertyItem[] items = getPropertyItems();
         for (int i = 0; i < items.length; i++) {
             PropertyDefinition def = items[i].getDefinition();
             if (def.getCategory() == getSelectedCategory()) {
                 storeItem(items[i]);
             }
         }
 
     }
 
     public void storeItem(PropertyItem item) {
         store.put(item.getName(), item);
     }
 
     public Iterator storedItems() {
         return new EntryIterator(store);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping,
      *      javax.servlet.http.HttpServletRequest)
      */
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         if (isCommiting()) {
             ActionErrors errs = null;
             storeItems();
             for (Iterator i = storedItems(); i.hasNext();) {
                 PropertyItem item = (PropertyItem) i.next();
                 PropertyDefinition def = item.getDefinition();
                 try {
                     Object propertyValue = item.getPropertyValue();
                    Object value = item.getValue();
                     if (propertyValue instanceof String) {
                         propertyValue = String.valueOf(propertyValue).trim();
                    }
                    if (value instanceof String) {
                        value = String.valueOf(value).trim();
                        item.setValue(value);
                     }
                     def.validate(String.valueOf(propertyValue), getClass().getClassLoader());
                 } catch (CoreException ce) {
                     ce.getBundleActionMessage().setArg3(
                         CoreUtil.getMessageResources(request.getSession(), def.getMessageResourcesKey()).getMessage(
                             (Locale) request.getSession().getAttribute(Globals.LOCALE_KEY), def.getName() + ".name"));
                     if (errs == null) {
                         errs = new ActionErrors();
                     }
                     errs.add(Globals.ERROR_KEY, ce.getBundleActionMessage());
                 } catch (Exception e) {
                     errs.add(Globals.ERROR_KEY, new ActionMessage("properties.error.failedToValidate", e.getMessage()));
                 }
             }
             if (errs != null)
                 return errs;
 
         }
         return super.validate(mapping, request);
     }
 }
