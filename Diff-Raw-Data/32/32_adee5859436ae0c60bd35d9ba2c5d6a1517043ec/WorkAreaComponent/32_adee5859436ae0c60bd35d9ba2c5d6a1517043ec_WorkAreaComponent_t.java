 package com.jtbdevelopment.e_eye_o.ria.vaadin.components;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 import com.jtbdevelopment.e_eye_o.ria.events.IdObjectRelatedSideTabClicked;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.workareas.*;
import com.jtbdevelopment.e_eye_o.ria.vaadin.utils.ComponentUtils;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.VerticalLayout;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.ConfigurableBeanFactory;
 import org.springframework.context.annotation.Scope;
 
 import javax.annotation.PostConstruct;
 
 /**
  * Date: 3/10/13
  * Time: 4:19 PM
  */
 @org.springframework.stereotype.Component
 @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
 public class WorkAreaComponent extends CustomComponent {
 
     private final VerticalLayout verticalLayout;
 
     @Autowired
     private StudentsWorkArea studentsWorkArea;
     @Autowired
     private ClassListsWorkArea classListsWorkArea;
     @Autowired
     private ObservationsWorkArea observationsWorkArea;
     @Autowired
     private PhotosWorkArea photosWorkArea;
     @Autowired
     private ObservationCategoriesWorkArea observationCategoriesWorkArea;
 
     @Autowired
     public WorkAreaComponent(final EventBus eventBus) {
         eventBus.register(this);
         verticalLayout = new VerticalLayout();
         verticalLayout.setSizeFull();
         setSizeFull();
         setCompositionRoot(verticalLayout);
 
     }
 
     @PostConstruct
     public void postConstruct() {
         verticalLayout.addComponent(studentsWorkArea);
         verticalLayout.addComponent(classListsWorkArea);
         verticalLayout.addComponent(observationCategoriesWorkArea);
         verticalLayout.addComponent(observationsWorkArea);
         verticalLayout.addComponent(photosWorkArea);
         photosWorkArea.setVisible(false);
         studentsWorkArea.setVisible(true);               //  TODO - make which one a default
         classListsWorkArea.setVisible(false);
         observationCategoriesWorkArea.setVisible(false);
         observationsWorkArea.setVisible(false);
     }
 
     @Subscribe
     @SuppressWarnings("unused")
     public void changeDataArea(final IdObjectRelatedSideTabClicked event) {
         Notification.show("Switching to " + event.getEntityType().getCaption());
        ComponentUtils.clearAllErrors(verticalLayout);
         for (Component child : verticalLayout) {
             child.setVisible(false);
         }
         switch (event.getEntityType()) {
             case Students:
                 studentsWorkArea.setVisible(true);
                 break;
             case Classes:
                 classListsWorkArea.setVisible(true);
                 break;
             case Observations:
                 observationsWorkArea.setVisible(true);
                 break;
             case Categories:
                 observationCategoriesWorkArea.setVisible(true);
                 break;
             case Photos:
                 photosWorkArea.setVisible(true);
                 break;
             default:
                 //  TODO - log or notify
         }
     }
 }
