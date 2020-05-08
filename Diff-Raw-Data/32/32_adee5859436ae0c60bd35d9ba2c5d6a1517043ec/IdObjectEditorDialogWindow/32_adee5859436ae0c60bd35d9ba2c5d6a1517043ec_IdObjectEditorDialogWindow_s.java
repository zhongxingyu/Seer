 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.editors;
 
 import com.google.common.eventbus.EventBus;
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.ria.events.IdObjectChanged;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.utils.ComponentUtils;
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
 //  TODO - be nice to recompute the caption at top as we go
 //  TODO - add delete button/workflow
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
 
     public IdObjectEditorDialogWindow(final Class<T> entityType, final float width, final float height) {
         this(entityType, width, Unit.EM, height, Unit.EM);
     }
 
     public IdObjectEditorDialogWindow(final Class<T> entityType, final float width, final Unit widthUnit, final float height, final Unit heightUnit) {
         this.entityBeanFieldGroup = new BeanFieldGroup<>(entityType);
         this.entityType = entityType;
         setSizeUndefined();
         setModal(true);
         addStyleName(Runo.WINDOW_DIALOG);
         setWidth(width, widthUnit);
         setHeight(height, heightUnit);
         setContent(mainLayout);
 
     }
 
     @PostConstruct
     public void init() {
         buildMainLayout();
         ComponentUtils.setImmediateForAll(this, true);
     }
 
     protected void buildMainLayout() {
         // the main layout and components will be created here
         mainLayout.setMargin(true);
         mainLayout.setSpacing(true);
 
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
                     save();
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
 
     protected T save() throws FieldGroup.CommitException {
         entityBeanFieldGroup.commit();
         T entity = entityBeanFieldGroup.getItemDataSource().getBean();
         if (entity == null) {
             return null;
         }
         IdObjectChanged.ChangeType changeType;
         if (StringUtil.isBlank(entity.getId())) {
             entity = writeNewObjectToDAO(entity);
             changeType = IdObjectChanged.ChangeType.ADDED;
         } else {
             entity = writeUpdateObjectToDAO(entity);
             changeType = IdObjectChanged.ChangeType.MODIFIED;
         }
         eventBus.post(new IdObjectChanged<>(changeType, entity));
         closeWindow();
         return entity;
     }
 
     protected T writeUpdateObjectToDAO(final T entity) {
         return readWriteDAO.update(entity);
     }
 
     protected T writeNewObjectToDAO(final T entity) {
         return readWriteDAO.create(entity);
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
         ComponentUtils.removeAllValidators(this);
         mainLayout.setComponentError(null);
         entityBeanFieldGroup.setItemDataSource(entity);
     }
 }
