 package de.hswt.hrm.catalog.ui.event;
 
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.List;
 
 public class CatalogMatchingEventHandler {
 
     public void onAvailableTargetMouseDoubleClick(Event event) {
 
         List l = (List) event.widget;
        String s = l.getItem(l.getSelectionIndex());
         l.remove(s);
         List l2 = (List) XWT.findElementByName(l, "matchedTarget");
         l2.add(s);
 
     }
 
     public void onMatchedTargetMouseDoubleClick(Event event) {
 
         List l = (List) event.widget;
        String s = l.getItem(l.getSelectionIndex());
         l.remove(s);
         List l2 = (List) XWT.findElementByName(l, "availableTarget");
         l2.add(s);
         String[] items = l2.getItems();
         java.util.Arrays.sort(items);
         l2.setItems(items);
 
     }
 }
