 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.generatedcolumns;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 import com.google.common.eventbus.EventBus;
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.IdObjectFactory;
 import com.jtbdevelopment.e_eye_o.entities.Observable;
 import com.jtbdevelopment.e_eye_o.entities.Observation;
 import com.jtbdevelopment.e_eye_o.ria.events.IdObjectChanged;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.ObservationTable;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.utils.ComponentUtils;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.Runo;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Date: 3/31/13
  * Time: 7:50 AM
  */
 public class ObservationFollowUpButtons implements Table.ColumnGenerator {
     private final ObservationTable observationTable;
 
     private final EventBus eventBus;
 
     private final BeanItemContainer<Observation> entities;
 
     private final ReadWriteDAO readWriteDAO;
 
     private final IdObjectFactory idObjectFactory;
 
     public ObservationFollowUpButtons(final IdObjectFactory idObjectFactory, final ReadWriteDAO readWriteDAO, final EventBus eventBus, final ObservationTable observationTable, final BeanItemContainer<Observation> entities) {
         this.observationTable = observationTable;
         this.eventBus = eventBus;
         this.entities = entities;
         this.readWriteDAO = readWriteDAO;
         this.idObjectFactory = idObjectFactory;
     }
 
     @Override
     public Object generateCell(final Table source, final Object itemId, final Object columnId) {
         final Observation entity = entities.getItem(itemId).getBean();
         GridLayout layout = new GridLayout(3, 1);
         layout.setSizeUndefined();
         final List<Observation> allObservationFollowups = readWriteDAO.getAllObservationFollowups(entity);
         Button addFollowUp = new Button("New");
         addFollowUp.setDescription("This starts a new observation.");
         addFollowUp.addStyleName(Runo.BUTTON_SMALL);
         addFollowUp.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(final Button.ClickEvent event) {
                 observationTable.showEntityEditor(idObjectFactory.newObservation(entity.getAppUser())).setFollowUpFor(entity);
             }
         });
         layout.addComponent(addFollowUp, 0, 0);
         Button linkFollowUp = new Button("Link");
         linkFollowUp.setDescription("This lets you pick an existing observation as the follow-up.");
         linkFollowUp.addStyleName(Runo.BUTTON_SMALL);
         linkFollowUp.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(final Button.ClickEvent event) {
                 //  Might have changed since initial check
                 final Collection<Observation> allObservationFollowups = Collections2.filter(
                         readWriteDAO.getAllObservationsForEntity(entity.getObservationSubject()),
                         //  TODO- also filter on matching categories
                         new Predicate<Observation>() {
                             @Override
                             public boolean apply(@Nullable final Observation input) {
                                 return input != null
                                         && input.getFollowUpForObservation() == null
                                         && !input.equals(entity)
                                         && input.getObservationTimestamp().compareTo(entity.getObservationTimestamp()) > 0
                                         && !Sets.intersection(input.getCategories(), entity.getCategories()).isEmpty();
                             }
                         }
                 );
                 if (allObservationFollowups.isEmpty()) {
                     Notification.show("Nothing available to link to.");
                     return;
                 }
                 ObservationPicker picker = new ObservationPicker(allObservationFollowups, new OKCallback() {
                     @Override
                     public void onOK(final Observation pickedObservation) {
                         Observation updatedFollowup = readWriteDAO.linkFollowUpObservation(entity, pickedObservation);
                         Observable observable = readWriteDAO.get(Observable.class, updatedFollowup.getObservationSubject().getId());
                        Observation updatedInitialObservation = readWriteDAO.get(Observation.class, updatedFollowup.getFollowUpForObservation().getId());
                         eventBus.post(new IdObjectChanged<>(IdObjectChanged.ChangeType.MODIFIED, updatedFollowup));
                        eventBus.post(new IdObjectChanged<>(IdObjectChanged.ChangeType.MODIFIED, updatedInitialObservation));
                         eventBus.post(new IdObjectChanged<>(IdObjectChanged.ChangeType.MODIFIED, observable));
                     }
                 });
                 UI.getCurrent().addWindow(picker);
             }
         });
         layout.addComponent(linkFollowUp, 1, 0);
 
 
         if (!allObservationFollowups.isEmpty()) {
             Button showFollowUpButton = new Button((allObservationFollowups.size() > 1 ? "See Follow Ups" : "See Follow Up") + " (" + allObservationFollowups.size() + ")");
             showFollowUpButton.addStyleName(Runo.BUTTON_SMALL);
             showFollowUpButton.addClickListener(new Button.ClickListener() {
                 @Override
                 public void buttonClick(final Button.ClickEvent event) {
                     //  Might have changed since initial check
                     final List<Observation> allObservationFollowups = readWriteDAO.getAllObservationFollowups(entity);
                     if (allObservationFollowups.size() == 1) {
                         observationTable.showEntityEditor(allObservationFollowups.get(0));
                     } else {
                         ObservationPicker picker = new ObservationPicker(allObservationFollowups, new OKCallback() {
                             @Override
                             public void onOK(final Observation pickedObservation) {
                                 observationTable.showEntityEditor(pickedObservation);
                             }
                         });
                         UI.getCurrent().addWindow(picker);
                     }
                 }
             });
             layout.addComponent(showFollowUpButton, 2, 0);
         }
         return layout;
     }
 
     private interface OKCallback {
         void onOK(final Observation pickedObservation);
     }
 
     private class ObservationPicker extends Window {
         public ObservationPicker(final Collection<Observation> pickSet, final OKCallback callback) {
             setModal(true);
             addStyleName(Runo.WINDOW_DIALOG);
             setWidth(30, Unit.EM);
             setHeight(30, Unit.EM);
             VerticalLayout mainLayout = new VerticalLayout();
             mainLayout.setSizeFull();
             //  TODO - pick up double click?
             final ListSelect select = new ListSelect();
             select.setMultiSelect(false);
             select.setNullSelectionAllowed(false);
             select.setRows(5);
             BeanItemContainer<Observation> observations = new BeanItemContainer<>(Observation.class, pickSet);
             select.setContainerDataSource(observations);
             select.setItemCaptionPropertyId("summaryDescription");
             mainLayout.addComponent(select);
             HorizontalLayout buttons = new HorizontalLayout();
             Button ok = new Button("OK");
             ok.addClickListener(new Button.ClickListener() {
                 @Override
                 public void buttonClick(final Button.ClickEvent event) {
                     final Object value = select.getValue();
                     if (value == null) {
                         Notification.show("Please pick an observation.", Notification.Type.HUMANIZED_MESSAGE);
                         return;
                     }
                     getUI().removeWindow(ObservationPicker.this);
                     callback.onOK((Observation) value);
                 }
             });
             buttons.addComponent(ok);
             Button cancel = new Button("Cancel");
             cancel.addClickListener(new Button.ClickListener() {
                 @Override
                 public void buttonClick(final Button.ClickEvent event) {
                     getUI().removeWindow(ObservationPicker.this);
                 }
             });
             buttons.addComponent(cancel);
             mainLayout.addComponent(buttons);
             mainLayout.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
             mainLayout.setComponentAlignment(select, Alignment.MIDDLE_CENTER);
             setCaption("Pick An Observation");
             setContent(mainLayout);
             ComponentUtils.setImmediateForAll(this, true);
         }
     }
 }
 
