package com.arcbees.ide.plugin.template.presenter{package};
 
 import javax.inject.Inject;
 
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtplatform.mvp.client.ViewImpl;
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 #if( $uihandlers )
 	#set( $extends = "ViewWithUiHandlers<${name}UiHandlers>" )
 #else 
 	#set( $extends = "ViewImpl" )
 #end
 
 public class ${name}View extends $extends implements ${name}Presenter.MyView {
     interface Binder extends UiBinder<Widget, ${name}View> {
     }
 
     @UiField
     SimplePanel main;
 
     @Inject
     ApplicationView(Binder uiBinder) {
         initWidget(uiBinder.createAndBindUi(this));
     }
 
     @Override
     public void setInSlot(Object slot, IsWidget content) {
         if (slot == ${name}Presenter.SLOT_${name}) {
             main.setWidget(content);
         } else {
             super.setInSlot(slot, content);
         }
     }
 }
