 /**
  * 
  */
 package org.iplantc.de.client.viewer.views.cells;
 
 import org.iplantc.de.client.models.viewer.VizUrl;
 import org.iplantc.de.commons.client.util.WindowUtil;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 
 /**
  * @author sriram
  * 
  */
 public class TreeUrlCell extends AbstractCell<VizUrl> {
 
     public TreeUrlCell() {
         super("click");
     }
 
     @Override
     public void render(com.google.gwt.cell.client.Cell.Context context, VizUrl model, SafeHtmlBuilder sb) {
         // TODO JDS We should use CssResource here
         sb.appendHtmlConstant("<div style=\"cursor:pointer;text-decoration:underline;white-space:pre-wrap;\">" //$NON-NLS-1$
                 + model.getUrl() + "</div>"); //$NON-NLS-1$
 
     }
 
     @Override
     public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent,
             VizUrl value, NativeEvent event, ValueUpdater<VizUrl> valueUpdater) {
 
         if (value == null) {
             return;
         }
         // Call the super handler, which handlers the enter key.
         super.onBrowserEvent(context, parent, value, event, valueUpdater);
        WindowUtil.open(value.getUrl(), "width=800,height=600"); //$NON-NLS-1$
     }
 
 }
