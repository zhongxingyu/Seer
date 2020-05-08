 package org.iplantc.core.uicommons.client.widgets;
 
 import com.google.gwt.event.logical.shared.HasResizeHandlers;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.form.error.SideErrorHandler;
 
 public class IPlantSideErrorHandler extends SideErrorHandler implements ResizeHandler {
 
     public IPlantSideErrorHandler(Widget target) {
         super(target);
        setAdjustTargetWidth(false);
         if (target instanceof HasResizeHandlers) {
             ((HasResizeHandlers)target).addResizeHandler(this);
         }
     }
 
     @Override
     public void onResize(ResizeEvent event) {
         if (!isShowing()) {
             return;
         }
         alignErrorIcon();
     }
 
     public boolean isShowing() {
         return (errorIcon != null) && errorIcon.isVisible();
     }
 
 }
