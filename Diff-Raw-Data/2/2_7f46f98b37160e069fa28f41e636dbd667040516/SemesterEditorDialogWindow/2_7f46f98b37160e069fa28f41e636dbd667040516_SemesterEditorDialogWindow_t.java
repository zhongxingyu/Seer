 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.editors;
 
 import com.jtbdevelopment.e_eye_o.entities.Semester;
 import org.springframework.beans.factory.config.ConfigurableBeanFactory;
 import org.springframework.context.annotation.Scope;
 
 /**
  * Date: 11/11/13
  * Time: 6:33 PM
  */
 @org.springframework.stereotype.Component
 @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
 public class SemesterEditorDialogWindow extends GeneratedEditorDialogWindow<Semester> {
     public SemesterEditorDialogWindow() {
        super(Semester.class, 50, 11.5f);
     }
 }
 
