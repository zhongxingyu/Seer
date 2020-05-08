 package org.jbpm.datamodeler.editor.client.editors;
 
 import com.github.gwtbootstrap.client.ui.Breadcrumbs;
 import com.github.gwtbootstrap.client.ui.NavLink;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Widget;
 import org.jbpm.datamodeler.editor.events.DataModelerEvent;
 import org.jbpm.datamodeler.editor.events.DataObjectSelectedEvent;
 import org.jbpm.datamodeler.editor.model.DataModelTO;
 import org.jbpm.datamodeler.editor.model.DataObjectTO;
 
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DataObjectBreadcrums extends Breadcrumbs {
 
     int size = 5;
     
     private List<BufferElement> buffer = new ArrayList<BufferElement>();
 
     @Inject
     private Event<DataModelerEvent> dataModelerEvent;
     
     private DataModelTO dataModel;
 
     public DataObjectBreadcrums(int size) {
         super();
         this.size = size;
     }
 
     public DataObjectBreadcrums() {
         super();
     }
 
     public void setSize(int size) {
         this.size = size;
     }
 
     @Override
     public void clear() {
         super.clear();
         buffer.clear();
     }
 
     public void add(final DataObjectTO dataObject) {
 
         //scan the buffer to see if the item is in the breadcrumb
         int currentPosition = -1;
         int j = 0;
         boolean addElement = false;
 
         for (BufferElement bufferedElement : buffer) {
             if (bufferedElement.getDataObject().equals(dataObject)) {
                 // the object to be inserted is already in the buffer
                 currentPosition = j;
                 break;
             }
             j++;
         }
 
         if (currentPosition >= 0) {
             if (currentPosition < (buffer.size()-1)) {
                 //the element exists in the buffer and isn't the last.
                 //remove it from current positon.
                 buffer.remove(currentPosition);
                 addElement = true;
             } else {
                 //the elements is allready in the last position
                 addElement = false;
             }
         } else {
             addElement = true;
         }
 
         if (addElement) {
 
             if (buffer.size() >= size) {
                 //remove first element
                 buffer.remove(0);
             }
 
             //create the new widget
             NavLink navLink = new NavLink(dataObject.getName());
             navLink.addClickHandler(new ClickHandler() {
                 @Override
                 public void onClick(ClickEvent event) {
                     notifyObjectSelected(dataObject);
                 }
             });
             buffer.add(new BufferElement(dataObject, navLink));
 
             rebuild();
         }
     }
 
     private void rebuild() {
         super.clear();
         for (BufferElement element : buffer) {
              add(element.getWidget());
         }
     }
 
     class BufferElement {
 
         DataObjectTO dataObject;
         Widget widget;
 
         BufferElement(DataObjectTO dataObject, Widget widget) {
             this.dataObject = dataObject;
             this.widget = widget;
         }
 
         public DataObjectTO getDataObject() {
             return dataObject;
         }
 
         public void setDataObject(DataObjectTO dataObject) {
             this.dataObject = dataObject;
         }
 
         public Widget getWidget() {
             return widget;
         }
 
         public void setWidget(Widget widget) {
             this.widget = widget;
         }
     }
 
     public DataModelTO getDataModel() {
         return dataModel;
     }
 
     public void setDataModel(DataModelTO dataModel) {
         this.dataModel = dataModel;
     }
 
     // Event Observers
     private void onDataObjectSelected(@Observes DataObjectSelectedEvent event) {
 
         if (event.isFrom(getDataModel())) {
             if (event.getCurrentDataObject() != null) {
                 if (event.isFrom(DataModelerEvent.DATA_OBJECT_BROWSER)) {
                     //it's a type selection in the editor
                     //locate the object and select my row, but it's not needed to fire selection
                     add(event.getCurrentDataObject());
                 } else if (event.isFrom(DataModelerEvent.DATA_MODEL_BROWSER)) {
                     clear();
                     add(event.getCurrentDataObject());
                 }
             }
         }
     }
 
     // Event notifications
     private void notifyObjectSelected(DataObjectTO dataObject) {
         dataModelerEvent.fire(new DataObjectSelectedEvent(DataModelerEvent.DATA_MODEL_BREAD_CRUMB, getDataModel(), dataObject));
     }
 
 }
