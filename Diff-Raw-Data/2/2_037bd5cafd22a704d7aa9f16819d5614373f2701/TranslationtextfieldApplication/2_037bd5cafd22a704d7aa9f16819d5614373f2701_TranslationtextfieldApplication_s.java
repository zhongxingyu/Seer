 package com.github.wolfie.translationtextfield;
 
 import java.util.Locale;
 
 import com.vaadin.Application;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Window;
 
 public class TranslationtextfieldApplication extends Application {
   private static final long serialVersionUID = -356123802955365660L;
   
   @Override
   public void init() {
     final Window mainWindow = new Window("Multitextfield Application");
     final Label label = new Label("Hello Vaadin user");
     mainWindow.addComponent(label);
     setMainWindow(mainWindow);
     
     final DateField dateField = new DateField();
     dateField.setImmediate(true);
     dateField.setValue("foo");
     mainWindow.addComponent(dateField);
     
     final TranslationTextField translationTextField = new TranslationTextField(
         getString());
     mainWindow.addComponent(translationTextField);
   }
   
   private TranslatedString getString() {
     return TranslatedString //
         .with(new Locale("fi"), "Terve, maailma") //
        .and(new Locale("sv"), "Hej, vrlden") //
         .and(new Locale("en"), "Hello World") //
         .create();
     
   }
 }
