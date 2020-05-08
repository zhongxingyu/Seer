 package de.hswt.hrm.catalog.ui.event;
 
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Text;
 
 import de.hswt.hrm.catalog.model.ICatalogItem;
 
 public class CatalogMatchingEventHandler {
 
     private List targets;
     private List currents;
     private List activities;
 
     /**
      * This event occurs whenever an entry in the upper table is selected
      * 
      * @param event
      */
     public void onMatchedTargetMouseDoubleClick(Event event) {
 
         List matched = (List) event.widget;
         int i = matched.getSelectionIndex();
 
         if (i == -1) {
             return;
         }
 
         String s = matched.getItem(i);
         matched.remove(s);
         List l2 = (List) XWT.findElementByName(matched, "availableTarget");
         l2.add(s);
         String[] items = l2.getItems();
         java.util.Arrays.sort(items);
         l2.setItems(items);
 
         if (matched.getItemCount() == 1) {
             currents = (List) XWT.findElementByName(matched, "availableCurrent");
             currents.setEnabled(true);

         }
 
         else if (matched.getItemCount() == 0) {
             currents = (List) XWT.findElementByName(matched, "availableCurrent");
             currents.setEnabled(false);
             targets = (List) XWT.findElementByName(matched, "availableTarget");
             targets.setEnabled(true);
         }
 
     }
 
     public void onMouseDown(Event event) {
 
         TableViewer tv = (TableViewer) XWT.findElementByName(event.widget, "availableTarget");
 
         handleSelection(tv);
 
     }
 
     public void onMouseDown_1(Event event) {
 
         TableViewer tv = (TableViewer) XWT.findElementByName(event.widget, "availableCurrent");
 
         handleSelection(tv);
 
     }
 
     public void onMouseDown_2(Event event) {
 
         TableViewer tv = (TableViewer) XWT.findElementByName(event.widget, "availableActivity");
 
         handleSelection(tv);
 
     }
 
     private void handleSelection(TableViewer tv) {
 
         // obtain the place in the column where the doubleClick happend
         ICatalogItem selectedEntry = (ICatalogItem) tv.getElementAt(tv.getTable()
                 .getSelectionIndex());
         if (selectedEntry == null) {
             return;
         }
 
         Text desc = (Text) XWT.findElementByName(tv, "desc");
         desc.setText(selectedEntry.getText());
 
         Text name = (Text) XWT.findElementByName(tv, "name");
         name.setText(selectedEntry.getName());
     }
 
     public void onMouseDoubleClick(Event event) {
 
         TableViewer tv = (TableViewer) XWT.findElementByName(event.widget, "availableTarget");
         ICatalogItem item = (ICatalogItem) tv.getElementAt(tv.getTable().getSelectionIndex());
 
         if (item == null) {
             return;
         }
 
         List matchedTarget = (List) XWT.findElementByName(tv, "matchedTarget");
         matchedTarget.add(item.getName());
         tv.remove(item);

        tv = (TableViewer) XWT.findElementByName(tv, "availableCurrent");
        tv.getTable().setEnabled(true);
 
     }
 }
