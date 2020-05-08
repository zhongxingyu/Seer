 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.autocompleteentry;
 
 import org.icefaces.impl.component.SeriesStateHolder;
 import javax.faces.component.NamingContainer;
 import javax.faces.model.SelectItem;
 import javax.faces.component.UISelectItem;
 import javax.faces.component.UISelectItems;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.el.MethodExpression;
 import javax.el.ValueExpression;
 import javax.el.ELContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 public class AutoCompleteEntry extends AutoCompleteEntryBase implements NamingContainer {
 
     private transient List<Object> itemList;
     private transient int index = -1;
 	private Object selectedItem;
 	private transient List changedComponentIds;
 	
     public void encodeBegin(FacesContext context) throws IOException {
         super.encodeBegin(context);
     }
 	
     public void decode(FacesContext facesContext) {
         setSelectedItem(facesContext);
         super.decode(facesContext);
 		
         boolean isEventSource = false;
 		Object componenetId = facesContext.getExternalContext()
                 .getRequestParameterMap().get("ice.event.captured");
         if (componenetId != null) {
             if (componenetId.toString().equals(getClientId(facesContext))) {
                 isEventSource = true;
             }
         }
         if (isEventSource) {
           queueEventIfEnterKeyPressed(facesContext);
         }
     }
 	
     private void setSelectedItem(FacesContext facesContext) {
         Map requestMap =
                 facesContext.getExternalContext().getRequestParameterMap();
         String clientId = getClientId(facesContext);
         String value = (String) requestMap.get(clientId);
 		String oldValue = (String) getValue();
 		if (value.equals("") && oldValue == null) return;
         if(value != null) {
             if(!value.equalsIgnoreCase(oldValue)) {
                 setChangedComponentId( clientId );
             }
 			setSubmittedValue(value);
         }
         String selIdxStr = (String) requestMap.get(
             clientId + AutoCompleteEntryRenderer.AUTOCOMPLETE_INDEX);
         if (selIdxStr != null && selIdxStr.trim().length() > 0) {
             int selIdx = Integer.parseInt(selIdxStr);
             setSelectedIndex(selIdx);
             setChangedComponentId( clientId );
         }
         else {
             setSelectedItem(value);
         }
     }
 	
     private static boolean isPartialSubmitKeypress(Map requestMap, String clientId) {
         String target = (String) requestMap.get("ice.event.target");
         String captured = (String) requestMap.get("ice.event.captured");
         if(target == null)
             target = "";
         if(captured == null)
             captured = "";
         if( !target.equals(clientId) && !captured.equals(clientId) )
             return false;
         String type = (String) requestMap.get("ice.event.type");
         String partialSubmit = (String) requestMap.get("ice.submit.partial");
         if(type == null || type.length() == 0 ||
            partialSubmit == null || partialSubmit.length() == 0)
         {
             return false;
         }
         if( partialSubmit.equalsIgnoreCase("true") &&
             (type.equalsIgnoreCase("onundefined") || type.equalsIgnoreCase("onunknown") ||
              type.equalsIgnoreCase("onkeypress") ||
              type.equalsIgnoreCase("onkeydown")) )
         {
             return true;
         }
         return false;
     }
 	
     public Iterator getItemList() {
         if (itemList == null) {
             return Collections.EMPTY_LIST.iterator();
         }
         return itemList.iterator();
     }
 	
     public void setIndex(int index) {
         this.index = index;
 
     }
 	
     public String getClientId(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         String baseClientId = super.getClientId(context);
         if (index >= 0) {
             return (baseClientId + NamingContainer.SEPARATOR_CHAR + index++);
         } else {
             return (baseClientId);
         }
     }
 	
     public void resetId(UIComponent component) {
         String id = component.getId();
         component.setId(id); // Forces client id to be reset
         if (component.getChildCount() == 0)return;
         Iterator kids = component.getChildren().iterator();
         while (kids.hasNext()) {
             UIComponent kid = (UIComponent) kids.next();
             resetId(kid);
         }
 
     }
 	
     void populateItemList() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         if (getSelectFacet() != null) {
             //facet being used on jsf page, so get the list from the value binding
             itemList = getListValue();
         } else {
             //selectItem or selectItems has been used on jsf page, so get the selectItems
             itemList = getSelectItems(facesContext,this);
         }
     }
 	
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         //keyevent should be process by this component
         super.broadcast(event);
     }
 	
     public UIComponent getSelectFacet() {
         return (UIComponent) getFacet("row");
     }
 
     protected void setSelectedIndex(int index) {
         Object selItm = null;
         if (index >= 0) {
             if (itemList == null) {
                 populateItemList();
             }
             if (itemList != null) {
                 if (index < itemList.size()) {
                     selItm = itemList.get(index);
                 }
             }
         }
         selectedItem = selItm;
     }
 	
     public void setSelectedItem(String key) {
         Object selItm = null;
         if (key != null) {
             if (itemList == null) {
                 populateItemList();
             }
             if (itemList != null) {
                 Object sticky = null;
                 boolean multipleMatches = false;
                 FacesContext facesContext = FacesContext.getCurrentInstance();
                 for (int i = 0; i < itemList.size(); i++) {
 					// SelectItem item = (SelectItem) itemList.get(i);
                     Object item = itemList.get(i);
                     String itemLabel;
 					if (item instanceof SelectItem) {
 						itemLabel = ((SelectItem) item).getLabel();
 						if (itemLabel == null) {
 							/*itemLabel = DomBasicRenderer.converterGetAsString(
 								facesContext, this, item.getValue());*/
 								itemLabel = ((SelectItem) item).getValue().toString();
 						}
 					} else {
 						itemLabel = getMainValue(item);
 					}
                     if (key.equals(itemLabel)) {
                         if (selectedItem != null && selectedItem.equals(item)) {
                             sticky = item;
                         }
                         multipleMatches |= (selItm != null);
                         selItm = item;
                     }
                 }
                 if (sticky != null) {
                     selItm = sticky;
                 }
                 else if (multipleMatches) {
                     selItm = null;
                 }
             }
         }
         selectedItem = selItm;
     }
 	
 	private String getMainValue(Object o) {
 		FacesContext context = FacesContext.getCurrentInstance();
 		ELContext elContext = context.getELContext();
 		String listVar = getListVar();
 		context.getExternalContext().getRequestMap().put(listVar, o);
 		Object value = getValueExpression("filterBy").getValue(elContext);
 		context.getExternalContext().getRequestMap().remove(listVar);
 
 		if (value == null) return "null";
 		return (String) value;
 	}
 
     /**
      * <p>Return the value of the <code>selectedItem</code> property.</p>
      */
     public Object getSelectedItem() {
         return selectedItem;
     }
 	
     void setChangedComponentId(Object id) {
         if (id == null) {
             if (changedComponentIds != null) {
                 changedComponentIds.clear();
             }
         } else {
             if (changedComponentIds == null) {
                 changedComponentIds = new ArrayList(6);
             }
             changedComponentIds.add(id);
         }
     }
 	
     boolean hasChanged() {
         if (changedComponentIds == null) {
             return false;
         }
         return changedComponentIds
                 .contains(this.getClientId(FacesContext.getCurrentInstance()));
     }
 	
     private void queueEventIfEnterKeyPressed(FacesContext facesContext) {
         try {
             Map requestParemeterMap =  facesContext.getExternalContext()
                 .getRequestParameterMap();
             KeyEvent keyEvent =
                     new KeyEvent(this, requestParemeterMap);
 
             if (keyEvent.getKeyCode() == KeyEvent.CARRIAGE_RETURN) {
                 setChangedComponentId(null); // do not populate list
                 queueEvent(new ActionEvent(this));
             }
             if("true".equals(requestParemeterMap.get("ice.event.left"))){
                 setChangedComponentId(null); // do not populate list
                 queueEvent(new ActionEvent(this));                
            } else if("onclick".equals(requestParemeterMap.get("ice.event.type"))){
                 setChangedComponentId(null); // do not populate list
                 queueEvent(new ActionEvent(this));
             }
 
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 	
     public String getOnkeypress() {
         if (isDisabled() || isReadonly()) return "";
         return super.getOnkeypress();
     }
 	
     public static List getSelectItems(FacesContext context,
                                       UIComponent uiComponent) {
 
         List selectItems = new ArrayList();
         if (uiComponent.getChildCount() == 0) return selectItems;
         Iterator children = uiComponent.getChildren().iterator();
         while (children.hasNext()) {
             UIComponent nextSelectItemChild = (UIComponent) children.next();
             if (nextSelectItemChild instanceof UISelectItem) {
                 Object selectItemValue =
                         ((UISelectItem) nextSelectItemChild).getValue();
                 if (selectItemValue != null &&
                     selectItemValue instanceof SelectItem) {
                     selectItems.add(selectItemValue);
                 } else {
                     selectItems.add(
                             new SelectItem(
                                     ((UISelectItem) nextSelectItemChild).getItemValue(),
                                     ((UISelectItem) nextSelectItemChild).getItemLabel(),
                                     ((UISelectItem) nextSelectItemChild).getItemDescription(),
                                     ((UISelectItem) nextSelectItemChild).isItemDisabled()));
                 }
             } else if (nextSelectItemChild instanceof UISelectItems) {
                 Object selectItemsValue =
                         ((UISelectItems) nextSelectItemChild).getValue();
 
                 if (selectItemsValue != null) {
                     if (selectItemsValue instanceof SelectItem) {
                         selectItems.add(selectItemsValue);
                     } else if (selectItemsValue instanceof Collection) {
                         Iterator selectItemsIterator =
                                 ((Collection) selectItemsValue).iterator();
                         while (selectItemsIterator.hasNext()) {
                             selectItems.add(selectItemsIterator.next());
                         }
                     } else if (selectItemsValue instanceof SelectItem[]) {
                         SelectItem selectItemArray[] =
                                 (SelectItem[]) selectItemsValue;
                         for (int i = 0; i < selectItemArray.length; i++) {
                             selectItems.add(selectItemArray[i]);
                         }
                     } else if (selectItemsValue instanceof Map) {
                         Iterator selectItemIterator =
                                 ((Map) selectItemsValue).keySet().iterator();
                         while (selectItemIterator.hasNext()) {
                             Object nextKey = selectItemIterator.next();
                             if (nextKey != null) {
                                 Object nextValue =
                                         ((Map) selectItemsValue).get(nextKey);
                                 if (nextValue != null) {
                                     selectItems.add(
                                             new SelectItem(
                                                     nextValue.toString(),
                                                     nextKey.toString()));
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return selectItems;
     }
 }
