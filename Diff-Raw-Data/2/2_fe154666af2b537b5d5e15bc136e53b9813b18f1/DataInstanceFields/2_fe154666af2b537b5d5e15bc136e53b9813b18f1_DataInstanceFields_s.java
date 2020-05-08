 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.chime.client.widgets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.event.BoxComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.user.client.Element;
 import com.paxxis.chime.client.InstanceUpdateListener;
 import com.paxxis.chime.client.common.DataField;
 import com.paxxis.chime.client.common.DataInstance;
 import com.paxxis.chime.client.common.Shape;
 
 /**
  *
  * @author Robert Englander
  */
 public class DataInstanceFields extends LayoutContainer
 {
     private DataInstance _instance = null;
     private Shape _type = null;
     private InstanceUpdateListener _saveListener;
     private boolean pendingData = false;
     private DataInstance pendingInstance; 
     private Shape pendingType;
     private boolean pendingRefresh;
     private List<String> pendingExclusions = new ArrayList<String>();
 
     public DataInstanceFields(InstanceUpdateListener saveListener)
     {
         _saveListener = saveListener;
     }
     
     public void onRender(Element parent, int index) {
     	super.onRender(parent, index);
         init();
         if (pendingData) {
         	pendingData = false;
         	setDataInstance(pendingInstance, pendingType, pendingExclusions, pendingRefresh);
         }
     }
     
     private void init()
     {
         setLayout(new RowLayout());
         setStyleAttribute("backgroundColor", "white");
         setBorders(false);
         addListener(Events.Resize,
             new Listener<BoxComponentEvent>() {
 
                 public void handleEvent(BoxComponentEvent evt) {
                     layout(true);
                 }
 
             }
         );
     }
 
     public void setDataInstance(DataInstance instance, Shape type) {
         setDataInstance(instance, type, null, true);
     }
     
     public void setDataInstance(DataInstance instance, Shape type, List<String> excludedFields, boolean refresh) {
     	if (!isRendered()) {
     		pendingData = true;
     		pendingInstance = instance;
     		pendingType = type;
     		pendingRefresh = refresh;
     		if (excludedFields != null) {
     			pendingExclusions.addAll(excludedFields);
     		}
     		
     		return;
     	}
 
     	update(instance, _instance, type, excludedFields, refresh);
 
         _instance = instance;
         _type = type;
     }
 
     private void update(DataInstance newInstance, DataInstance oldInstance, Shape type, 
     		List<String> excludedFields, boolean refresh) {
 
         // we want to keep the fields that were also in the old instance; remove those
         // that don't exist anymore; and add the new ones.
         
         // TODO for now we aren't handling the case where the data type definition
         // has changed.  so for now there's no reason to remove the fields.  instead,
         // add them if this is the first time.  actually, if this is a different instance
         // then we do remove the fields and start over
        boolean startOver = oldInstance == null || newInstance.getId() != oldInstance.getId();
         if (startOver) {
             removeAll();
 
             boolean isFirst = true;
             List<DataField> fields = type.getFields();
             boolean shade = false;
             for (DataField field : fields) {
             	
                 if (!field.isPrivate() && !excludedFields.contains(field.getName())) {
                     if (isFirst) {
                         isFirst = false;
                     } else {
                         add(new Html("<hr COLOR=\"#f1f1f1\"/>"), new RowData(1, -1, new Margins(2, 0, 2, 0)));
                     }
 
                     DataInstanceField f = new DataInstanceField(newInstance, type, field, _saveListener);
                     if (shade) {
                         f.setStyleAttribute("backgroundColor", "#f1f1f1");
                     }
 
                     shade = !shade;
                     add(f, new RowData(1, -1, new Margins(2, 0, 2, 0)));
                 }
             }
         } else {
             for (Component comp : getItems()) {
                 if (comp instanceof DataInstanceField) {
                     DataInstanceField f = (DataInstanceField)comp;
                     f.updateDataInstance(newInstance, refresh);
                 }
             }
         }
         
         layout();
     }
     
 }
