 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.editors;
 
 import com.google.common.eventbus.EventBus;
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
import com.jtbdevelopment.e_eye_o.entities.Observable;
import com.jtbdevelopment.e_eye_o.entities.Observation;
 import com.jtbdevelopment.e_eye_o.ria.events.IdObjectChanged;
 import com.vaadin.data.fieldgroup.BeanFieldGroup;
 import com.vaadin.data.fieldgroup.FieldGroup;
 import com.vaadin.event.ShortcutAction;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.Runo;
 import org.jsoup.helper.StringUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 
 /**
  * Date: 3/20/13
  * Time: 8:13 PM
  */
 public abstract class IdObjectEditorDialogWindow<T extends AppUserOwnedObject> extends Window {
     @Autowired
     protected EventBus eventBus;
 
     @Autowired
     protected ReadWriteDAO readWriteDAO;
 
     protected final Class<T> entityType;
 
     protected final BeanFieldGroup<T> entityBeanFieldGroup;
 
     protected final VerticalLayout mainLayout = new VerticalLayout();
 
     protected abstract Layout buildEditorLayout();
 
     protected abstract Focusable getInitialFocusComponent();
 
     public IdObjectEditorDialogWindow(final Class<T> entityType, final int width, final int height) {
         this.entityBeanFieldGroup = new BeanFieldGroup<>(entityType);
         this.entityType = entityType;
         setSizeUndefined();
         setModal(true);
         addStyleName(Runo.WINDOW_DIALOG);
         setWidth(width, Unit.EM);
         setHeight(height, Unit.EM);
         setContent(mainLayout);
     }
 
     @PostConstruct
     public void init() {
         buildMainLayout();
     }
 
     protected void buildMainLayout() {
         // the main layout and components will be created here
         mainLayout.setMargin(true);
         mainLayout.setSpacing(true);
         mainLayout.setImmediate(true);
 
         Layout editorRow = buildEditorLayout();
         mainLayout.addComponent(editorRow);
         mainLayout.setComponentAlignment(editorRow, Alignment.MIDDLE_CENTER);
 
         Layout buttons = buildButtonLayout();
         mainLayout.addComponent(buttons);
         mainLayout.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
     }
 
     private Layout buildButtonLayout() {
         HorizontalLayout buttons = new HorizontalLayout();
         Button save = new Button("Save");
         save.addStyleName(Runo.BUTTON_DEFAULT);
         save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
         buttons.addComponent(save);
         save.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(final Button.ClickEvent event) {
                 try {
                     entityBeanFieldGroup.commit();
                     T entity = entityBeanFieldGroup.getItemDataSource().getBean();
                     if (entity == null) {
                         return;
                     }
                     IdObjectChanged.ChangeType changeType;
                     if (StringUtil.isBlank(entity.getId())) {
                         entity = readWriteDAO.create(entityBeanFieldGroup.getItemDataSource().getBean());
                         changeType = IdObjectChanged.ChangeType.ADDED;
                     } else {
                         entity = readWriteDAO.update(entityBeanFieldGroup.getItemDataSource().getBean());
                         changeType = IdObjectChanged.ChangeType.MODIFIED;
                     }
                     eventBus.post(new IdObjectChanged<>(changeType, entity));
                    //  TODO - move this
                    if (entity instanceof Observation) {
                        final Observable observationSubject = readWriteDAO.get(Observable.class, ((Observation) entity).getObservationSubject().getId());
                        eventBus.post(new IdObjectChanged<>(IdObjectChanged.ChangeType.MODIFIED, observationSubject));
                    }
                     closeWindow();
                 } catch (FieldGroup.CommitException e) {
                     throw new RuntimeException(e);
                 }
             }
         });
         final Button cancel = new Button("Cancel");
         cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
         cancel.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(final Button.ClickEvent event) {
                 entityBeanFieldGroup.discard();
                 closeWindow();
             }
         });
         buttons.addComponent(cancel);
 
         final Button reset = new Button("Reset");
         reset.setClickShortcut(ShortcutAction.KeyCode.R, ShortcutAction.ModifierKey.ALT);
         reset.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(final Button.ClickEvent event) {
                 entityBeanFieldGroup.discard();
             }
         });
         buttons.addComponent(reset);
         return buttons;
     }
 
     private void closeWindow() {
         entityBeanFieldGroup.discard();
         getUI().removeWindow(this);
     }
 
     @Override
     public void attach() {
         super.attach();
         getUI().setFocusedComponent(getInitialFocusComponent());
     }
 
     public void setEntity(final T entity) {
         setCaption(entity.getSummaryDescription());
         removeAllValidators(this);
         mainLayout.setComponentError(null);
         entityBeanFieldGroup.setItemDataSource(entity);
     }
 
     //  Vaadin doesn't remove previous validators so they stack up
     protected void removeAllValidators(Component component) {
         if (component instanceof AbstractField) {
             ((AbstractField) component).removeAllValidators();
         }
         if (component instanceof HasComponents) {
             for (Component child : (HasComponents) component) {
                 removeAllValidators(child);
             }
         }
     }
 }
