 package com.odea.components.modalWindow;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 
public abstract class SelectModalWindow extends ModalWindow {
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
