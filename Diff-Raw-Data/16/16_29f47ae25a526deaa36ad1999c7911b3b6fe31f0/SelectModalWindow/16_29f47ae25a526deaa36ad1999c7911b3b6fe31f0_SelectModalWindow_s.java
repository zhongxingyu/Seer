 package com.odea.components.modalWindow;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 
public abstract class SelectModalWindow extends modalWindow {
     public SelectModalWindow(String id) {
         super(id);
 
         setInitialWidth(300);
         setInitialHeight(80);
 
         setTitle("Notificacion");
 
         setContent(new SelectContentPanel(this.getContentId()){
             public void onCancel(AjaxRequestTarget target) {
                 SelectModalWindow.this.onCancel(target);
             }
         });
     }
 
     public abstract void onCancel(AjaxRequestTarget target);
 
 }
