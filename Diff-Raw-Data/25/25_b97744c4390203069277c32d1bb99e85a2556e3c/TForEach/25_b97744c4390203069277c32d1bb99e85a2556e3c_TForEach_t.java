 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.component;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIComponentBase;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.el.VariableResolver;
 import javax.faces.internal.ComponentStates;
 import javax.faces.internal.ComponentStatesHolder;
 import javax.faces.internal.FacesMessageResource;
 import javax.faces.internal.FacesMessageUtil;
 import javax.faces.internal.NamingContainerUtil;
 import javax.faces.internal.SavedState;
 
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.PropertyDesc;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 import org.seasar.framework.log.Logger;
 import org.seasar.framework.util.AssertionUtil;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.framework.util.FieldUtil;
 import org.seasar.framework.util.MethodUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.util.BindingUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 
 /**
  * @author higa
  * @author manhole
  * @author shot
  */
 public class TForEach extends UIComponentBase implements NamingContainer,
         ComponentStatesHolder {
 
     public static final String COMPONENT_TYPE = "org.seasar.teeda.extension.ForEach";
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.ForEach";
 
     private static final String DEFAULT_RENDERER_TYPE = "org.seasar.teeda.extension.ForEach";
 
     private static final Object[] EMPTY_ITEMS = new Object[0];
 
     private static final String INDEX_SUFFIX = "Index";
 
     public static final int INITIAL_ROW_INDEX = -1;
 
     protected static final Method IS_PARAMETERIZED_METHOD = getIsParameterizedMethod();
 
     protected static final Method GET_ELEMENT_CLASS_METHOD = getGetElementClassMethod();
 
     private String tagName;
 
     private String pageName;
 
     private String itemsName;
 
     private Boolean omittag;
 
     private List bindingPropertyNames = new ArrayList();
 
     private int rowIndex = INITIAL_ROW_INDEX;
 
     private int rowSize;
 
     private final ComponentStates componentStates = new ComponentStates();
 
     private static final Logger logger = Logger.getLogger(TForEach.class);
 
     public TForEach() {
         setRendererType(DEFAULT_RENDERER_TYPE);
     }
 
     public String getFamily() {
         return COMPONENT_FAMILY;
     }
 
     public void setId(final String id) {
         super.setId(id);
         NamingContainerUtil.refreshDescendantComponentClientId(this);
     }
 
     public String getClientId(final FacesContext context) {
         final String clientId = super.getClientId(context);
         final int rowIndex = getRowIndex();
         if (rowIndex == INITIAL_ROW_INDEX) {
             return clientId;
         }
         return clientId + NamingContainer.SEPARATOR_CHAR + rowIndex;
     }
 
     public String getTagName() {
         return tagName;
     }
 
     public void setTagName(String tagName) {
         this.tagName = tagName;
     }
 
     public String getItemsName() {
         return itemsName;
     }
 
     public void setItemsName(final String itemsName) {
         if ((itemsName != null) &&
                 !itemsName.endsWith(ExtensionConstants.ITEMS_SUFFIX)) {
             throw new IllegalArgumentException(itemsName);
         }
         this.itemsName = itemsName;
     }
 
     public String getItemName() {
         if (itemsName == null) {
             return null;
         }
         return itemsName.substring(0, itemsName.length() -
                 ExtensionConstants.ITEMS_SUFFIX.length());
     }
 
     public String getIndexName() {
         final String itemName = getItemName();
         if (itemName == null) {
             return null;
         }
         return itemName + INDEX_SUFFIX;
     }
 
     public String getPageName() {
         return pageName;
     }
 
     public void setPageName(final String pageName) {
         this.pageName = pageName;
     }
 
     public boolean isOmittag() {
         if (omittag != null) {
             return omittag.booleanValue();
         }
         ValueBinding vb = getValueBinding("omittag");
         Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         if (v != null) {
             return v.booleanValue();
         }
         return JsfConstants.DIV_ELEM.equalsIgnoreCase(tagName) ||
                 JsfConstants.SPAN_ELEM.equalsIgnoreCase(tagName);
     }
 
     public void setOmittag(boolean omittag) {
         this.omittag = new Boolean(omittag);
     }
 
     public String[] getBindingPropertyNames() {
         return (String[]) bindingPropertyNames
                 .toArray(new String[bindingPropertyNames.size()]);
     }
 
     public void setValueBindingAttribute(String name, String value) {
         BindingUtil.setValueBinding(this, name, value);
         bindingPropertyNames.add(name);
     }
 
     public int getRowIndex() {
         return rowIndex;
     }
 
     public void setRowIndex(final int rowIndex) {
         this.rowIndex = rowIndex;
     }
 
     public void processDecodes(FacesContext context) {
         AssertionUtil.assertNotNull("context", context);
         if (!isRendered()) {
             return;
         }
         try {
             decode(context);
         } catch (RuntimeException e) {
             context.renderResponse();
             throw e;
         }
     }
 
     public void processValidators(final FacesContext context) {
         if (context == null) {
             throw new NullPointerException("context");
         }
         if (!isRendered()) {
             return;
         }
         processValidatorsAllRows(context, this);
     }
 
     protected void processValidatorsAllRows(final FacesContext context,
             final UIComponent base) {
         final String pageName = getPageName();
         final String id = getId();
         final String expression = BindingUtil.getExpression(pageName, id);
         for (int i = 0; i < rowSize; ++i) {
             final Integer savedIndex = bindRowIndex(context, new Integer(i));
             enterRow(context, i, base);
             for (Iterator itr = base.getFacetsAndChildren(); itr.hasNext();) {
                 final UIComponent component = (UIComponent) itr.next();
                 component.processValidators(context);
             }
             final String parentClientId = getClientId(context);
             if (!FacesMessageResource.hasMessages(expression)) {
                 processEachRowValidation(context, i, parentClientId);
             }
             leaveRow(context, base);
             bindRowIndex(context, savedIndex);
         }
         aggregateErrorMessageIfNeed(context, expression);
     }
 
     protected void processEachRowValidation(final FacesContext context,
             final int row, String parentClientId) {
         if (FacesMessageUtil.hasMessages(context)) {
             appendLineInfoToErrorMessage(context, parentClientId, row);
         }
     }
 
     protected void aggregateErrorMessageIfNeed(FacesContext context,
             String expression) {
         if (FacesMessageResource.hasMessages(expression)) {
             final String parentRowClientId = super.getClientId(context);
             final FacesMessage aggregateMessage = FacesMessageResource
                     .getFacesMessage(expression);
             aggregateErrorMessage(aggregateMessage, context, parentRowClientId);
         }
     }
 
     protected boolean aggregateErrorMessage(FacesMessage aggregateMessage,
             FacesContext context, String parentClientId) {
         boolean shouldAggregate = false;
         Map map = new HashMap();
         String saveClientId = null;
         Set clientIds = new HashSet();
         for (final Iterator itr = context.getClientIdsWithMessages(); itr
                 .hasNext();) {
             final String clientId = (String) itr.next();
             clientIds.add(clientId);
             if (!clientId.startsWith(parentClientId)) {
                 Iterator messages = context.getMessages(clientId);
                 map.put(clientId, messages);
                 continue;
             }
             if (!shouldAggregate) {
                 saveClientId = clientId;
                 shouldAggregate = true;
             }
         }
         if (shouldAggregate) {
             BeanDesc bd = BeanDescFactory.getBeanDesc(context.getClass());
             Field field = bd.getField("messages");
             FieldUtil.set(field, context, null);
             for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
                 Map.Entry entry = (Entry) itr.next();
                 //String clientId = (String) entry.getKey();
                 for (Iterator messages = (Iterator) entry.getValue(); messages
                         .hasNext();) {
                     FacesMessage fm = (FacesMessage) messages.next();
                     context.addMessage(null, fm);
                 }
             }
             context.addMessage(saveClientId, aggregateMessage);
             final ExternalContext externalContext = context
                     .getExternalContext();
             final Map requestMap = externalContext.getRequestMap();
             requestMap.put(
                     ExtensionConstants.TEEDA_EXTENSION_MESSAGE_CLIENTIDS,
                     clientIds);
         }
         return shouldAggregate;
     }
 
     protected void appendLineInfoToErrorMessage(final FacesContext context,
             final String parentClientId, final int row) {
         for (final Iterator itr = context.getClientIdsWithMessages(); itr
                 .hasNext();) {
             final String clientId = (String) itr.next();
            if (clientId == null || !clientId.startsWith(parentClientId)) {
                 continue;
             }
             for (final Iterator messages = context.getMessages(clientId); messages
                     .hasNext();) {
                 final FacesMessage fm = (FacesMessage) messages.next();
                 final String summary = fm.getSummary();
                 final String detail = fm.getDetail();
                 //TODO : maybe need to be flexible for line output.
                 if (summary == null && detail == null) {
                     continue;
                 }
                 final String lineErrorMessage = getLineErrorMessage(context,
                         row + 1);
                 if (summary != null) {
                     if (lineErrorMessage != null) {
                         fm.setSummary(summary + lineErrorMessage);
                     } else {
                         fm
                                 .setSummary(summary +
                                         ExtensionConstants.VALIDATION_ERROR_LINE_PREFIX +
                                         (row + 1) +
                                         ExtensionConstants.VALIDATION_ERROR_LINE_SUFFIX);
                     }
                 }
                 if (detail != null) {
                     if (lineErrorMessage != null) {
                         fm.setDetail(detail + lineErrorMessage);
                     } else {
                         fm
                                 .setDetail(detail +
                                         ExtensionConstants.VALIDATION_ERROR_LINE_PREFIX +
                                         (row + 1) +
                                         ExtensionConstants.VALIDATION_ERROR_LINE_SUFFIX);
                     }
                 }
             }
         }
 
     }
 
     protected String getLineErrorMessage(final FacesContext context,
             int linenumber) {
         String message = FacesMessageUtil.getSummary(context,
                 ExtensionConstants.VALIDATION_ERROR_LINE_MESSAGE,
                 new Object[] { new Integer(linenumber) });
         return message;
     }
 
     //fix for https://www.seasar.org/issues/browse/TEEDA-146
     protected Object createNewInstance(final FacesContext context,
             Class itemType) {
         AssertionUtil.assertNotNull("itemType", itemType);
         Object o = null;
         if (itemType.isPrimitive()) {
             itemType = ClassUtil.getWrapperClassIfPrimitive(itemType);
         }
         if (itemType == Integer.class) {
             return new Integer("0");
         } else if (itemType == Long.class) {
             return new Long("0");
         } else if (itemType == Double.class) {
             return new Double("0");
         } else if (itemType == Float.class) {
             return new Float("0");
         } else if (itemType == Short.class) {
             return new Short("0");
         } else if (itemType == BigDecimal.class) {
             return new BigDecimal("0");
         } else if (itemType == BigInteger.class) {
             return new BigInteger("0");
         } else if (itemType == Boolean.class) {
             return Boolean.FALSE;
         } else if (itemType == Calendar.class) {
             final Locale locale = context.getViewRoot().getLocale();
             return Calendar.getInstance(locale);
         } else if (itemType.isArray()) {
             Class cType = itemType.getComponentType();
             return Array.newInstance(cType, 0);
         } else if (List.class.isAssignableFrom(itemType)) {
             return new ArrayList();
         } else {
             o = ClassUtil.newInstance(itemType);
             return o;
         }
     }
 
     public void processUpdates(final FacesContext context) {
         if (context == null) {
             throw new NullPointerException("context");
         }
         if (!isRendered()) {
             return;
         }
         processUpdatesAllRows(context, this);
     }
 
     public void processUpdatesAllRows(final FacesContext context,
             final UIComponent base) {
         final Object page = getPage(context);
         final Class pageClass = page.getClass();
         final BeanDesc pageBeanDesc = BeanDescFactory.getBeanDesc(pageClass);
 
         final String itemsName = getItemsName();
         final PropertyDesc itemsPd = pageBeanDesc.getPropertyDesc(itemsName);
         if (!itemsPd.isWritable()) {
             throw new IllegalStateException("class [" +
                     pageBeanDesc.getBeanClass().getName() +
                     "] should have writeMethod for [" +
                     itemsPd.getPropertyName() + "]");
         }
         final Class itemsClass = itemsPd.getPropertyType();
         final Class itemClass;
         if (itemsClass.isArray()) {
             itemClass = itemsClass.getComponentType();
         } else if (List.class.isAssignableFrom(itemsClass) &&
                 isParameterized(itemsPd)) {
             itemClass = getElementClass(itemsPd);
         } else {
             logger
                     .debug("class [" + itemsClass.getName() +
                             "] should be array type or parameterized List, so no update.");
             return;
         }
 
         final Object items = itemsPd.getValue(page);
         final List itemList;
         if (items != null) {
             if (itemsClass.isArray()) {
                 final Object[] array = (Object[]) items;
                 final int length = array.length;
                 if (length >= rowSize) {
                     itemList = Arrays.asList(array);
                 } else {
                     final Object[] newArray = (Object[]) Array.newInstance(
                             itemClass, rowSize);
                     for (int i = 0; i < rowSize; ++i) {
                         if (i < length) {
                             newArray[i] = array[i];
                         } else {
                             newArray[i] = createNewInstance(context, itemClass);
                         }
                     }
                     itemsPd.setValue(page, newArray);
                     itemList = Arrays.asList(newArray);
                 }
             } else {
                 itemList = (List) items;
                 for (int i = itemList.size(); i < rowSize; i++) {
                     itemList.add(createNewInstance(context, itemClass));
                 }
             }
             rowSize = itemList.size();
         } else {
             if (itemsClass.isArray()) {
                 final Object[] array = (Object[]) Array.newInstance(itemClass,
                         rowSize);
                 for (int i = 0; i < array.length; i++) {
                     array[i] = createNewInstance(context, itemClass);
                 }
                 itemList = Arrays.asList(array);
                 itemsPd.setValue(page, array);
             } else {
                 itemList = new ArrayList(rowSize);
                 for (int i = 0; i < rowSize; i++) {
                     itemList.add(createNewInstance(context, itemClass));
                 }
                 itemsPd.setValue(page, itemList);
             }
         }
 
         final BeanDesc itemBeanDesc = BeanDescFactory.getBeanDesc(itemClass);
         for (int i = 0; i < itemList.size(); ++i) {
             final Object item = itemList.get(i);
             if (item == null) {
                 continue;
             }
             final Integer savedIndex = bindRowIndex(context, new Integer(i));
             final Map savedValues = itemToPage(pageBeanDesc, page, item);
             enterRow(context, i, base);
 
             for (Iterator itr = base.getFacetsAndChildren(); itr.hasNext();) {
                 final UIComponent component = (UIComponent) itr.next();
                 component.processUpdates(context);
             }
 
             leaveRow(context, base);
             pageToItem(page, pageBeanDesc, item, itemBeanDesc, savedValues);
             bindRowIndex(context, savedIndex);
         }
     }
 
     public void enterRow(final FacesContext context, final int rowIndex,
             final UIComponent base) {
         setRowIndex(rowIndex);
         componentStates.restoreDescendantState(context, base);
     }
 
     public void leaveRow(final FacesContext context, final UIComponent base) {
         componentStates.saveDescendantComponentStates(context, base);
         setRowIndex(INITIAL_ROW_INDEX);
     }
 
     public Integer bindRowIndex(final FacesContext context,
             final Integer rowIndex) {
         final Object page = getPage(context);
         final Class pageClass = page.getClass();
         final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(pageClass);
         final String indexName = getIndexName();
         final Map savedValues = new HashMap();
         setValue(beanDesc, page, indexName, rowIndex, savedValues);
         return (Integer) savedValues.get(indexName);
     }
 
     public Object getPage(final FacesContext context) {
         final VariableResolver variableResolver = context.getApplication()
                 .getVariableResolver();
         return variableResolver.resolveVariable(context, getPageName());
     }
 
     public Object saveState(final FacesContext context) {
         final Object[] values = new Object[6];
         values[0] = super.saveState(context);
         values[1] = tagName;
         values[2] = pageName;
         values[3] = itemsName;
         values[4] = omittag;
         values[5] = bindingPropertyNames;
         return values;
     }
 
     public void restoreState(final FacesContext context, final Object state) {
         final Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         tagName = (String) values[1];
         pageName = (String) values[2];
         itemsName = (String) values[3];
         omittag = (Boolean) values[4];
         bindingPropertyNames = (List) values[5];
     }
 
     public void save(final SavedState state) {
         state.setLocalValue(new Integer(rowSize));
     }
 
     public void restore(final SavedState state) {
         Integer value = (Integer) state.getLocalValue();
         rowSize = value == null ? 0 : value.intValue();
     }
 
     public int getRowSize() {
         return rowSize;
     }
 
     public void setRowSize(final int rowCount) {
         rowSize = rowCount;
     }
 
     public Object[] getItems(final FacesContext context) {
         final Object page = getPage(context);
         final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(page.getClass());
         final PropertyDesc pd = beanDesc.getPropertyDesc(getItemsName());
         final Object items = pd.getValue(page);
         if (items == null) {
             return EMPTY_ITEMS;
         }
         if (items instanceof Collection) {
             return ((Collection) items).toArray();
         }
         if (items.getClass().isArray()) {
             return (Object[]) items;
         }
         throw new IllegalStateException(items.getClass().toString());
     }
 
     public Map itemToPage(final BeanDesc pageBeanDesc, final Object page,
             final Object item) {
         if (item == null) {
             return Collections.EMPTY_MAP;
         }
         final String itemName = getItemName();
         setValue(pageBeanDesc, page, itemName, item, null);
         final Map savedValues = new HashMap();
         if (item instanceof Map) {
             processMapItem(pageBeanDesc, page, (Map) item, savedValues);
         } else {
             processBeanItem(pageBeanDesc, page, item, savedValues);
         }
         return savedValues;
     }
 
     public void pageToItem(final Object page, final BeanDesc pageBeanDesc,
             final Object item, final BeanDesc itemBeanDesc,
             final Map savedValues) {
         for (final Iterator it = savedValues.entrySet().iterator(); it
                 .hasNext();) {
             Entry entry = (Entry) it.next();
             String propertyName = (String) entry.getKey();
             Object savedPageValue = entry.getValue();
             final PropertyDesc pagePd = pageBeanDesc
                     .getPropertyDesc(propertyName);
             if (!pagePd.isReadable()) {
                 continue;
             }
             final Object pageValue = pagePd.getValue(page);
             if (pagePd.isWritable()) {
                 pagePd.setValue(page, savedPageValue);
             }
             if (item instanceof Map) {
                 ((Map) item).put(propertyName, pageValue);
             } else {
                 final PropertyDesc itemPd = itemBeanDesc
                         .getPropertyDesc(propertyName);
                 if (itemPd.isWritable()) {
                     itemPd.setValue(item, pageValue);
                 }
             }
         }
     }
 
     protected void processBeanItem(final BeanDesc pageBeanDesc,
             final Object page, final Object item, final Map savedValues) {
         final BeanDesc itemBeanDesc = BeanDescFactory.getBeanDesc(item
                 .getClass());
         for (int i = 0; i < itemBeanDesc.getPropertyDescSize(); i++) {
             final PropertyDesc pd = itemBeanDesc.getPropertyDesc(i);
             final String propertyName = pd.getPropertyName();
             if (pageBeanDesc.hasPropertyDesc(propertyName) &&
                     pageBeanDesc.getPropertyDesc(propertyName).isWritable()) {
                 final Object value = getValue(itemBeanDesc, item, propertyName);
                 setValue(pageBeanDesc, page, propertyName, value, savedValues);
             }
         }
     }
 
     protected void processMapItem(final BeanDesc pageBeanDesc,
             final Object page, final Map item, final Map savedValues) {
         for (final Iterator it = item.entrySet().iterator(); it.hasNext();) {
             Entry entry = (Entry) it.next();
             String propertyName = (String) entry.getKey();
             Object value = entry.getValue();
             if (pageBeanDesc.hasPropertyDesc(propertyName) &&
                     pageBeanDesc.getPropertyDesc(propertyName).isWritable()) {
                 setValue(pageBeanDesc, page, propertyName, value, savedValues);
             }
         }
     }
 
     protected Object getValue(final BeanDesc beanDesc, final Object page,
             final String propertyName) {
         if (beanDesc.hasPropertyDesc(propertyName)) {
             final PropertyDesc pd = beanDesc.getPropertyDesc(propertyName);
             if (pd.isReadable()) {
                 return pd.getValue(page);
             }
         }
         return null;
     }
 
     protected void setValue(final BeanDesc beanDesc, final Object page,
             final String propertyName, final Object value, final Map savedValues) {
         if (beanDesc.hasPropertyDesc(propertyName)) {
             final PropertyDesc pd = beanDesc.getPropertyDesc(propertyName);
             Class pdClass = pd.getPropertyType();
             if (pdClass.isPrimitive()) {
                 pdClass = ClassUtil.getWrapperClass(pdClass);
             }
             final Class valueClass = (value != null) ? value.getClass() : null;
             if (pd.isWritable() && isRelatedClass(pdClass, valueClass)) {
                 if (savedValues != null && pd.isReadable()) {
                     savedValues.put(propertyName, pd.getValue(page));
                 }
                 pd.setValue(page, value);
             }
         }
     }
 
     protected static boolean isRelatedClass(final Class pdClass,
             final Class valueClass) {
         if (valueClass == null) {
             return true;
         }
         return (pdClass == valueClass) ||
                 (pdClass.isAssignableFrom(valueClass));
     }
 
     protected static Method getIsParameterizedMethod() {
         try {
             return PropertyDesc.class.getMethod("isParameterized", null);
         } catch (final Exception e) {
             return null;
         }
     }
 
     protected static Method getGetElementClassMethod() {
         try {
             return PropertyDesc.class.getMethod("getElementClassOfCollection",
                     null);
         } catch (final Exception e) {
             return null;
         }
     }
 
     protected boolean isParameterized(final PropertyDesc pd) {
         if (IS_PARAMETERIZED_METHOD == null) {
             return false;
         }
         return ((Boolean) MethodUtil.invoke(IS_PARAMETERIZED_METHOD, pd, null))
                 .booleanValue();
     }
 
     protected Class getElementClass(final PropertyDesc pd) {
         if (GET_ELEMENT_CLASS_METHOD == null) {
             return null;
         }
         return (Class) MethodUtil.invoke(GET_ELEMENT_CLASS_METHOD, pd, null);
     }
 
 }
