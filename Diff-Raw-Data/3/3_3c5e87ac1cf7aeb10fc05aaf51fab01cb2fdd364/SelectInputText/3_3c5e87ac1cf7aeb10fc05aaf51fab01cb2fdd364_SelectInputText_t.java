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
 
 package com.icesoft.faces.component.selectinputtext;
 
 import com.icesoft.faces.component.CSS_DEFAULT;
 import com.icesoft.faces.component.ext.HtmlInputText;
 import com.icesoft.faces.component.ext.KeyEvent;
 import com.icesoft.faces.component.ext.renderkit.FormRenderer;
 import com.icesoft.faces.component.ext.taglib.Util;
 import com.icesoft.faces.context.effects.JavascriptContext;
 import com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer;
 
 import org.icefaces.impl.component.SeriesStateHolder;
 
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.el.MethodBinding;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 
 /**
  * SelectInputText is a JSF component class that represents an ICEfaces
  * autocomplete input text. This component requires the application developer to
  * implement the matching list rearch algorithm in their backing bean.
  * <p/>
  * SelectInputText extends the ICEfaces extended HtmlInputText component.
  * <p/>
  * By default this component is rendered by the "com.icesoft.faces.SelectInputText"
  * renderer type.
  */
 public class SelectInputText extends HtmlInputText implements NamingContainer,
         SeriesStateHolder {
     public static final String COMPONENT_TYPE =
             "com.icesoft.faces.SelectInputText";
 
     //default style classes
 
     //private properties for style classes
     private String styleClass;
     public static final String RENDERER_TYPE = "com.icesoft.faces.SelectInputText";
 
     /**
      * A request-scope attribute under which the model data for the row selected
      * by the current value of the "rowIndex" property will be exposed.
      */
     private String listVar = null;
 
     /**
      * A state variable for number of rows to be matched.
      */
     private Integer rows;
     private final int DEFAULT_MAX_MATCHS = 10;
 
     /**
      * A state variable for width of both inputtext and dropdownlist
      */
     private String width;
     private final String DEFAULT_WIDTH = "150";
 
     /**
      * A property to store the selectedItem, after successfull match
      */
     private SelectItem selectedItem;
 
     /**
      * list of selectItems
      */
     private transient List itemList;
 
     private String options;
 
     private transient int index = -1;
     
     private MethodBinding textChangeListener;
 
     public SelectInputText() {
         super();
         setRendererType(RENDERER_TYPE);
         JavascriptContext.includeLib(JavascriptContext.ICE_EXTRAS,
                                      FacesContext.getCurrentInstance());
     }
 
     /*
     *  (non-Javadoc)
     * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
     */
     public void encodeBegin(FacesContext context) throws IOException {
         super.encodeBegin(context);
     }
 
     /*
     *  (non-Javadoc)
     * @see javax.faces.component.UIComponent#decode(javax.faces.context.FacesContext)
     */
     public void decode(FacesContext facesContext) {
         setSelectedItem(facesContext);
         super.decode(facesContext);
         if (Util.isEventSource(facesContext,this)) {
           queueEventIfEnterKeyPressed(facesContext);
          //    queueEvent(new ActionEvent(this));
         }
 
     }
 
     /**
      * this method is responsible to setting the seletecdItem on the component
      *
      * @param facesContext
      */
     private void setSelectedItem(FacesContext facesContext) {
         Map requestMap =
                 facesContext.getExternalContext().getRequestParameterMap();
         String clientId = getClientId(facesContext);
 //System.out.println("SelectInputText.setSelectedItem()  clientId: " + clientId);
         String value = (String) requestMap.get(clientId);
         if(value != null) {
             boolean changed = isPartialSubmitKeypress(requestMap, clientId);
 //System.out.println("SelectInputText.setSelectedItem()  changed: " + changed);
             if(changed) {
                 setChangedComponentId( clientId );
                 
 //System.out.println("SelectInputText.setSelectedItem()  textChangeListener: " + getTextChangeListener());
                 if( getTextChangeListener() != null ) {
 //System.out.println("SelectInputText.setSelectedItem()  submittedValue: " + value);
                     Object oldValue = getSubmittedValue();
                     if(oldValue == null) {
                         oldValue = DomBasicRenderer.converterGetAsString(
                             facesContext, this, getValue());
                     }
 //System.out.println("SelectInputText.setSelectedItem()  oldValue: " + oldValue);
                     TextChangeEvent event = new TextChangeEvent(this, oldValue, value);
                     event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                     queueEvent(event);
                 }
             }
         }
         String selIdxStr = (String) requestMap.get(
             clientId + SelectInputTextRenderer.AUTOCOMPLETE_INDEX);
 //System.out.println("SIT.decode()  selIdxStr: " + selIdxStr);
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
 
 
     /**
      * return true if I had focus when submitted
      *
      * @param facesContext
      * @return focus
      */
     private boolean hadFocus(FacesContext facesContext) {
         Object focusId = facesContext.getExternalContext()
                 .getRequestParameterMap().get(FormRenderer.getFocusElementId());
         boolean focus = false;
         if (focusId != null) {
             if (focusId.toString().equals(getClientId(facesContext))) {
                 focus = true;
             }
         }
         setFocus(focus);
         return focus;
     }
 
     //this list would be set in populateItemList()
 
     /**
      * <p>Return the value of the <code>itemList</code> property.</p>
      */
     public Iterator getItemList() {
         if (itemList == null) {
             return Collections.EMPTY_LIST.iterator();
         }
         return itemList.iterator();
     }
 
     /**
      * <p>Set the value of the <code>index</code> property.</p>
      */
     public void setIndex(int index) {
         this.index = index;
 
     }
 
     /**
      * <p>Return the value of the <code>clientId</code> property.</p>
      */
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
 
     /**
      * reset parent's and its children's ids
      */
     void resetId(UIComponent component) {
         String id = component.getId();
         component.setId(id); // Forces client id to be reset
         if (component.getChildCount() == 0)return;
         Iterator kids = component.getChildren().iterator();
         while (kids.hasNext()) {
             UIComponent kid = (UIComponent) kids.next();
             resetId(kid);
         }
 
     }
 
     //this method generating the list of selectItems "itemList",  which can be bounded 
     //with the bean, or could be static on jsf page 
     //matches list can be change after value change event, so we are calling
     //this method after value change event in broadcast(), where the method bounded 
     //with valueChangeListner is being called and updates the data model.
     void populateItemList() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         if (getSelectFacet() != null) {
             //facet being used on jsf page, so get the list from the value binding
             itemList = getListValue();
         } else {
             //selectItem or selectItems has been used on jsf page, so get the selectItems
             itemList = Util.getSelectItems(facesContext,this);
         }
     }
 
     /* (non-Javadoc)
     * @see javax.faces.component.UIComponent#broadcast(javax.faces.event.FacesEvent)
     */
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         //keyevent should be process by this component
         super.broadcast(event);
 //System.out.println("SelectInputText.broadcast()  clientId: " + getClientId(FacesContext.getCurrentInstance()));
 //System.out.println("SelectInputText.broadcast()  event: " + event);
         if (event instanceof TextChangeEvent) {
             MethodBinding mb = getTextChangeListener();
 //System.out.println("SelectInputText.broadcast()  TextChangeEvent  mb: " + mb);
             if(mb != null) {
                 mb.invoke( FacesContext.getCurrentInstance(),
                            new Object[] {event} );
             }
         }
 //        else if (event instanceof ValueChangeEvent) {
 //System.out.println("SelectInputText.broadcast()  ValueChangeEvent  old: " + ((ValueChangeEvent)event).getOldValue() + "new: " + ((ValueChangeEvent)event).getNewValue());
 //        }
     }
 
     /**
      * <p>Return the value of the <code>selectInputText</code> property.</p>
      */
     public UIComponent getSelectFacet() {
         return (UIComponent) getFacet("selectInputText");
     }
 
     protected void setSelectedIndex(int index) {
 //System.out.println("SIT.setSelectedIndex()  index: " + index);
         SelectItem selItm = null;
         if (index >= 0) {
             if (itemList == null) {
                 populateItemList();
             }
             if (itemList != null) {
                 if (index < itemList.size()) {
                     selItm = (SelectItem) itemList.get(index);
 //System.out.println("SIT.setSelectedIndex()  selItm: " + selItm);
                 }
             }
         }
         selectedItem = selItm;
     }
     
     /**
      * <p>Set the value of the <code>selectedItem</code> property. 
      * If there are multiple matches between the key parameter, and the 
      * data model's SelectItem objects' itemLabel, it won't match any, 
      * unless one of the matching SelectItem objects equals the last 
      * selectedItem. Note that this component can only keep a reference 
      * to the last selectedItem if its value field is Serializable.</p>
      */
     public void setSelectedItem(String key) {
 //System.out.println("SIT.setSelectedItem()  key: " + key);
         SelectItem selItm = null;
         if (key != null) {
             if (itemList == null) {
                 populateItemList();
             }
             if (itemList != null) {
                 SelectItem sticky = null;
                 boolean multipleMatches = false;
                 FacesContext facesContext = FacesContext.getCurrentInstance();
                 for (int i = 0; i < itemList.size(); i++) {
                     SelectItem item = (SelectItem) itemList.get(i);
                     String itemLabel = item.getLabel();
                     if (itemLabel == null) {
                         itemLabel = DomBasicRenderer.converterGetAsString(
                             facesContext, this, item.getValue());
                     }
                     if (key.equals(itemLabel)) {
 //System.out.println("SIT.setSelectedItem()  MATCHED with index: " + i);
                         if (selectedItem != null && selectedItem.equals(item)) {
 //System.out.println("SIT.setSelectedItem()  MATCHED with selectedItem");
                             sticky = item;
                         }
                         multipleMatches |= (selItm != null);
 //if(selItm != null)System.out.println("SIT.setSelectedItem()  MULTIPLE MATCHES - UNMATCHING  (if no sticky)");
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
 
     /**
      * <p>Return the value of the <code>selectedItem</code> property.</p>
      */
     public SelectItem getSelectedItem() {
         return selectedItem;
     }
 
     //property to set the max matches to be displayed
 
     /**
      * <p>Set the value of the <code>rows</code> property.</p>
      */
     public void setRows(int rows) {
         this.rows = new Integer(rows);
     }
 
     /**
      * <p>Return the value of the <code>rows</code> property.</p>
      */
     public int getRows() {
         if (rows != null) {
             // Should always return the original no. of rows. JIRA ICE-1320.
             return rows.intValue();
         }
 
         ValueBinding vb = getValueBinding("rows");
         return vb != null ?
                Integer.parseInt(vb.getValue(getFacesContext()).toString()) :
                DEFAULT_MAX_MATCHS;
     }
 
     /**
      * <p>Set the value of the <code>width</code> property.</p>
      */
     public void setWidth(String width) {
         this.width = width;
     }
 
     /**
      * <p>Set the value of the <code>listVar</code> property.</p>
      */
     public void setListVar(String listVar) {
         this.listVar = listVar;
     }
 
     /**
      * <p>Return the value of the <code>listVar</code> property.</p>
      */
     public String getListVar() {
         if (listVar != null) {
             return listVar;
         }
         ValueBinding vb = getValueBinding("listVar");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
     }
 
     /**
      * <p>Set the value of the <code>listValue</code> property.</p>
      */
     public void setListValue(List listValue) {
         this.itemList = listValue;
     }
 
     /**
      * <p>Return the value of the <code>listValue</code> property.</p>
      */
     public List getListValue() {
         ValueBinding vb = getValueBinding("listValue");
         return (List) vb.getValue(FacesContext.getCurrentInstance());
     }
 
     /**
      * <p>Return the value of the <code>width</code> property.</p>
      */
     public String getWidth() {
         if (width != null) {
             return width;
         }
         ValueBinding vb = getValueBinding("width");
         return vb != null ? vb.getValue(getFacesContext()).toString() :
                DEFAULT_WIDTH;
     }
 
     String getWidthAsStyle() {
         try {//no measurement unit defined, so add the px unit
             int width = Integer.parseInt(getWidth());
             return "width:" + width + "px;";
         } catch (NumberFormatException e) {
             return "width:" + getWidth().trim();
         }
     }
 
     /**
      * <p>Set the value of the <code>styleClass</code> property.</p>
      */
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     /**
      * <p>Return the value of the <code>styleClass</code> property.</p>
      */
     public String getStyleClass() {
         return Util.getQualifiedStyleClass(this,
                             styleClass,
                             CSS_DEFAULT.DEFAULT_SELECT_INPUT,
                             "styleClass",
                             isDisabled());
     }
 
     /**
      * <p>Return the value of the <code>inputTextClass</code> property.</p>
      */
     public String getInputTextClass() {
         return Util.getQualifiedStyleClass(this, 
                 CSS_DEFAULT.DEFAULT_SELECT_INPUT_TEXT_CLASS, isDisabled());
     }
 
 
     /**
      * <p>Return the value of the <code>listClass</code> property.</p>
      */
     public String getListClass() {
         return Util.getQualifiedStyleClass(this, 
                 CSS_DEFAULT.DEFAULT_SELECT_INPUT_LIST_CLASS, isDisabled());
     }
 
 
     /**
      * <p>Return the value of the <code>rowClass</code> property.</p>
      */
     public String getRowClass() {
         return Util.getQualifiedStyleClass(this, 
                 CSS_DEFAULT.DEFAULT_SELECT_INPUT_ROW_CLASS, isDisabled());
     }
 
 
     /**
      * <p>Return the value of the <code>selectedRowClass</code> property.</p>
      */
     public String getSelectedRowClass() {
         return Util.getQualifiedStyleClass(this, 
                 CSS_DEFAULT.DEFAULT_SELECT_INPUT_SELECTED_ROW_CLASS, isDisabled());
     }
 
     public String getOptions() {
         return options;
     }
 
     public void setOptions(String options) {
         this.options = options;
     }
     
     public MethodBinding getTextChangeListener() {
         return textChangeListener;
     }
     
     public void setTextChangeListener(MethodBinding mb) {
         textChangeListener = mb;
     }
     
     //the following code is a fix for iraptor bug 347
     //on first page submit, all input elements gets valueChangeEvent (null to ""), 
     //so component's ids can be more then one
     private transient List changedComponentIds;
 
     /**
      * <p>Set the value of the <code>selectedPanel</code> property.</p>
      */
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
 
     /**
      * <p>Return the value of the <code>selectedPanel</code> property.</p>
      */
     boolean hasChanged() {
         if (changedComponentIds == null) {
             return false;
         }
         return changedComponentIds
                 .contains(this.getClientId(FacesContext.getCurrentInstance()));
     }
 
     /**
      * queue the event if the enter key was pressed
      *
      * @param facesContext
      */
     private void queueEventIfEnterKeyPressed(FacesContext facesContext) {
         try {
             Map requestParemeterMap =  facesContext.getExternalContext()
                 .getRequestParameterMap();
             KeyEvent keyEvent =
                     new KeyEvent(this, requestParemeterMap);
 
             if (keyEvent.getKeyCode() == KeyEvent.CARRIAGE_RETURN) {
                 queueEvent(new ActionEvent(this));
             }
             if("true".equals(requestParemeterMap.get("ice.event.left"))){
                 queueEvent(new ActionEvent(this));                
            } else if("onclick".equals(requestParemeterMap.get("ice.event.type"))){
                 queueEvent(new ActionEvent(this));
             }
 
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 
 
     /**
      * <p>Gets the state of the instance as a <code>Serializable</code>
      * Object.</p>
      */
     public Object saveState(FacesContext context) {
         Object values[] = new Object[8];
         values[0] = super.saveState(context);
         values[1] = styleClass;
         values[2] = listVar;
         values[3] = rows;
         values[4] = width;
         values[5] = options;
         values[6] = saveAttachedState(context, textChangeListener);
         values[7] = (selectedItem == null ||
                      selectedItem.getValue() == null ||
                      selectedItem.getValue() instanceof Serializable)
                     ? selectedItem : null;
         return ((Object) (values));
     }
 
     /**
      * <p>Perform any processing required to restore the state from the entries
      * in the state Object.</p>
      */
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         styleClass = (String) values[1];
         listVar = (String) values[2];
         rows = (Integer) values[3];
         width = (String) values[4];
         options = (String)values[5];
         textChangeListener = (MethodBinding)
             restoreAttachedState(context, values[6]);
         selectedItem = (SelectItem) values[7];
     }
 
     public String getOnkeypress() {
         if (isDisabled() || isReadonly()) return "";
         return super.getOnkeypress();
     }
 
     public Object saveSeriesState(FacesContext facesContext) {
         Object values[] = new Object[6];
         values[0] = styleClass;
         values[1] = listVar;
         values[2] = rows;
         values[3] = width;
         values[4] = options;
         values[5] = (selectedItem == null ||
                      selectedItem.getValue() == null ||
                      selectedItem.getValue() instanceof Serializable)
                     ? selectedItem : null;
         return ((Object) (values));
     }
 
     public void restoreSeriesState(FacesContext facesContext, Object state) {
         Object values[] = (Object[]) state;
         styleClass = (String) values[0];
         listVar = (String) values[1];
         rows = (Integer) values[2];
         width = (String) values[3];
         options = (String)values[4];
         selectedItem = (SelectItem) values[5];
     }}
