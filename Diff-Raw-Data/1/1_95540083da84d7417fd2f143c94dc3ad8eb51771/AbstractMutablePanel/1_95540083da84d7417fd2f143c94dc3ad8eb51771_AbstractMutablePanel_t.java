 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.web.panel.base;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.orm.hibernate4.HibernateObjectRetrievalFailureException;
 import org.springframework.util.StringUtils;
 import org.web4thejob.command.Command;
 import org.web4thejob.command.CommandDecorator;
 import org.web4thejob.command.CommandEnum;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageArgEnum;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.message.MessageListener;
 import org.web4thejob.orm.*;
 import org.web4thejob.orm.annotation.*;
 import org.web4thejob.orm.query.Condition;
 import org.web4thejob.orm.query.Query;
 import org.web4thejob.orm.query.QueryResultMode;
 import org.web4thejob.orm.scheme.RenderElement;
 import org.web4thejob.orm.scheme.RenderScheme;
 import org.web4thejob.setting.SettingEnum;
 import org.web4thejob.util.CoreUtil;
 import org.web4thejob.util.L10nString;
 import org.web4thejob.util.L10nUtil;
 import org.web4thejob.web.controller.ComponentController;
 import org.web4thejob.web.dialog.EntityPersisterDialog;
 import org.web4thejob.web.dialog.QueryDialog;
 import org.web4thejob.web.panel.*;
 import org.web4thejob.web.panel.base.zk.AbstractZkBindablePanel;
 import org.web4thejob.web.util.ZkUtil;
 import org.web4thejob.web.zbox.PropertyBox;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.HtmlBasedComponent;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zkplus.databind.Binding;
 import org.zkoss.zkplus.databind.DataBinder;
 import org.zkoss.zul.*;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import java.util.*;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 public abstract class AbstractMutablePanel extends AbstractZkBindablePanel implements MutablePanel {
     // ------------------------------ FIELDS ------------------------------
     public static final L10nString L10N_COLUMN_ATTRIBUTE = new L10nString("column_attribute", "Attribute");
     public static final L10nString L10N_COLUMN_VALUE = new L10nString("column_value", "Value");
     public static final L10nString L10N_MSG_REFRESH_FAILED = new L10nString(AbstractMutablePanel.class,
             "msg_refresh_failed", "The entity does not exist any more. Maybe it has been deleted by another user.");
     protected static final String ATTRIB_PATH_META = PathMetadata.class.getName();
     protected static final String DEFAULT_BEAN_ID = "entity";
     private static final String[] MONITOR_EVENTS = {Events.ON_CHANGE, Events.ON_CHANGING, Events.ON_CHECK};
     protected DataBinder dataBinder;
     private final DialogListener dialogListener = new DialogListener();
 
     private final MutableMode mutableMode;
     private Entity targetEntity;
     private Entity targetEntityOrig;
     private QueryDialog queryDialog;
     private Boolean dirty = null;
     private ChangeMonitor changeMonitor = new ChangeMonitor();
     private List<DirtyListener> dirtyListeners = new ArrayList<DirtyListener>(1);
     private PropertyMetadata statusHolderProp;
     protected Query activeQuery;
 
     // --------------------------- CONSTRUCTORS ---------------------------
 
     protected AbstractMutablePanel(MutableMode mutableMode) {
         this.mutableMode = mutableMode;
     }
 
     // --------------------- GETTER / SETTER METHODS ---------------------
 
     @Override
     public MutableMode getMutableMode() {
         return mutableMode;
     }
 
     @Override
     public Entity getTargetEntity() {
         if (dataBinder != null && targetEntity != null) {
             dataBinder.saveAll();
         }
         return targetEntity;
     }
 
     protected Entity getTargetEntityDirect() {
         return targetEntity;
     }
 
     @Override
     public boolean isDirty() {
         return dirty;
     }
 
     // ------------------------ INTERFACE METHODS ------------------------
 
     // --------------------- Interface CommandAware ---------------------
 
     @Override
     public Set<CommandEnum> getSupportedCommands() {
         Set<CommandEnum> supported = new HashSet<CommandEnum>(super.getSupportedCommands());
         supported.add(CommandEnum.QUERY);
         supported.add(CommandEnum.REFRESH);
         supported.add(CommandEnum.ADDNEW);
         supported.add(CommandEnum.UPDATE);
         supported.add(CommandEnum.DELETE);
         supported.add(CommandEnum.PRINT);
         return Collections.unmodifiableSet(supported);
     }
 
     // --------------------- Interface I18nAware ---------------------
 
     @Override
     public void setL10nMode(boolean l10nMode) {
         super.setL10nMode(l10nMode);
         if (dataBinder != null) {
             for (Binding binding : dataBinder.getAllBindings()) {
                 final Component comp = binding.getComponent();
                 if (comp instanceof I18nAware) {
                     ((I18nAware) comp).setL10nMode(l10nMode);
                 }
             }
         }
     }
 
     // --------------------- Interface MessageListener ---------------------
 
     @Override
     public void render() {
         super.render();
 
         if (getSettingValue(SettingEnum.RUN_QUERY_ON_STARTUP, false) && StringUtils.hasText(getSettingValue(SettingEnum
                 .PERSISTED_QUERY_NAME, (String) null)) && hasTargetType() && activeQuery == null && ((Component)
                 base).getPage() != null && getMutableMode() == MutableMode.READONLY) {
             Query q = CoreUtil.getQuery(getTargetType(), getSettingValue(SettingEnum.PERSISTED_QUERY_NAME,
                     (String) null));
             if (q != null) {
                 List<Entity> list = ContextUtil.getDRS().findByQuery(q);
                 if (!list.isEmpty()) {
                     bind(list.get(0));
                     activeQuery = q;
                 }
             }
         }
 
     }
 
     @Override
     public void processMessage(Message message) {
         switch (message.getId()) {
             case ENTITY_INSERTED:
                 if (this.equals(message.getSender()) || canBind(message.getArg(MessageArgEnum.ARG_ITEM,
                         Entity.class))) {
                     processEntityInsertion(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                 }
                 break;
             case BIND_DIRECT:
                 bindEcho(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                 break;
             case PARENT_CHANGED:
                 if (getParent() != null && dataBinder != null) {
                     //DataBinder does not work with detached components so we need this
                     //call in order to cover the case where panel's bind() is called prior to
                     //setting a parent.
                     dataBinder.loadAll();
                 }
                 break;
             default:
                 super.processMessage(message);
                 break;
         }
     }
 
     // --------------------- Interface MutablePanel ---------------------
 
     protected void ensureVisible(Component comp) {
         Clients.scrollIntoView(comp);
     }
 
     @Override
     public Set<ConstraintViolation<Entity>> validate() {
         if (hasTargetEntity()) {
 
             for (Binding binding : dataBinder.getAllBindings()) {
                 if (binding.getComponent() != null) {
                     Clients.clearWrongValue(binding.getComponent());
                 }
             }
 
             dataBinder.saveAll();
             Set<ConstraintViolation<Entity>> violations = getTargetEntity().validate();
             for (final ConstraintViolation<Entity> violation : violations) {
                 final PathMetadata pathMetadata = ContextUtil.getMRS().getPropertyPath(getTargetType(),
                         StringUtils.delimitedListToStringArray(violation.getPropertyPath().toString(), "."));
 
                 final Component comp = getBoundComponent(pathMetadata);
                 if (comp != null) {
                     ensureVisible(comp);
                     Clients.wrongValue(comp, violation.getMessage());
                 }
             }
             dataBinder.loadAll();
             return violations;
         }
 
         return Collections.emptySet();
     }
 
 
     private void checkInsertableIdentifiers() {
         if (getMutableMode() == MutableMode.INSERT) {
             EntityMetadata entityMetadata = ContextUtil.getMRS().getEntityMetadata(getTargetType());
             PropertyMetadata propertyMetadata = entityMetadata.getPropertyMetadata(entityMetadata.getIdentifierName());
             if (!propertyMetadata.isIdentityIdentifier()) {
 
                 //user entered pks should be tested for uniquness, otherwise hibernate will
                 //update an existing entry instead of inserting a new one.
                 Query query = ContextUtil.getEntityFactory().buildQuery(getTargetType());
                 query.addCriterion(new Path(propertyMetadata), Condition.EQ,
                         propertyMetadata.getValue(getTargetEntity()));
 
                 if (ContextUtil.getDRS().findUniqueByQuery(query) != null) {
                     Component component = getBoundComponent(ContextUtil.getMRS().getPropertyPath(getTargetType(),
                             new Path(propertyMetadata.getName())));
                     if (component != null) {
                         Clients.wrongValue(component, AbstractZkBindablePanel.L10N_MSG_UNIQUE_KEY_VIOLATION.toString
                                 (propertyMetadata.getFriendlyName()));
                     } else {
                         displayMessage(AbstractZkBindablePanel.L10N_MSG_UNIQUE_KEY_VIOLATION.toString
                                 (propertyMetadata.getFriendlyName()), true);
                     }
 
                     throw new ConstraintViolationException(Collections.<ConstraintViolation<?>>emptySet());
                 }
             }
         }
     }
 
     protected void persistLocal() throws Exception {
         ContextUtil.getDWS().save(getTargetEntity());
        dataBinder.loadAll();
         targetEntityOrig = targetEntity.clone();
     }
 
     @Override
     public void persist() throws Exception {
         if (hasTargetEntity()) {
             Set<ConstraintViolation<Entity>> violations = validate();
             if (violations.isEmpty()) {
                 Entity entity = getTargetEntity();
                 try {
 
                     checkInsertableIdentifiers();
 
                     persistLocal();
                     setDirty(false);
                     bindEcho(entity);
 
                 } catch (DataIntegrityViolationException e) {
 
                     //hibernate changes version even if flash fails, we need to revert it so that user can do
                     //multiple change/save cycles on the same view
                     if (!targetEntity.isNewInstance()) {
                         EntityMetadata entityMetadata = ContextUtil.getMRS().getEntityMetadata(getTargetType());
                         if (entityMetadata.isVersioned()) {
                             entityMetadata.setVersionValue(targetEntity, entityMetadata.getVersionValue
                                     (targetEntityOrig));
                         }
                     }
 
                     boolean uniqueViolation = false;
                     for (UniqueKeyConstraint constraint : ContextUtil.getMRS().getEntityMetadata(entity.getEntityType
                             ()).getUniqueConstraints()) {
 
                         //raise violations only for the updated properties, so that user will not get confused
                         boolean notChanged = true;
                         if (!targetEntity.isNewInstance()) {
                             for (PropertyMetadata propertyMetadata : constraint.getPropertyMetadatas()) {
                                 notChanged &= new EqualsBuilder().append(propertyMetadata.getValue(entity),
                                         propertyMetadata.getValue(targetEntityOrig)).isEquals();
                             }
                             if (notChanged) continue;
                         }
 
 
                         if (constraint.isViolated(entity)) {
                             for (PropertyMetadata propertyMetadata : constraint.getPropertyMetadatas()) {
                                 Component component = getBoundComponent(ContextUtil.getMRS().getPropertyPath
                                         (constraint.getEntityMetadata().getEntityType(),
                                                 new Path(propertyMetadata.getName())));
                                 if (component != null) {
                                     Clients.scrollIntoView(component);
                                     Clients.wrongValue(component,
                                             AbstractZkBindablePanel.L10N_MSG_UNIQUE_KEY_VIOLATION.toString(constraint
                                                     .getFriendlyName()));
                                     uniqueViolation = true;
                                 }
                             }
                         }
                     }
                     if (uniqueViolation) {
                         throw new ConstraintViolationException(Collections.<ConstraintViolation<?>>emptySet());
                     } else {
                         throw e;
                     }
                 }
             } else {
                 throw new ConstraintViolationException(Collections.<ConstraintViolation<?>>emptySet());
             }
         } else {
             throw new IllegalStateException("cannot persist when no current entity exist");
         }
     }
 
     @Override
     public void addDirtyListener(DirtyListener dirtyListener) {
         if (!dirtyListeners.contains(dirtyListener)) {
             dirtyListeners.add(dirtyListener);
         }
     }
 
     // -------------------------- OTHER METHODS --------------------------
 
     @Override
     protected void arrangeForMasterEntity() {
         if (getMasterEntity() == null) {
             setTargetEntity(null);
         } else if (getBindProperty() != null) {
             Entity trgEntity = ContextUtil.getDRS().findById(getTargetType(), getMasterEntity().getIdentifierValue());
             setTargetEntity(trgEntity);
         }
     }
 
     protected void setDataBinderBeans() {
         dataBinder.bindBean(DEFAULT_BEAN_ID, this.targetEntity);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     protected void arrangeForTargetEntity(Entity targetEntity) {
         boolean hasChanged = (this.targetEntity != null && !this.targetEntity.equals(targetEntity)) || (targetEntity !=
                 null && !targetEntity.equals(this.targetEntity));
 
         if (hasChanged && this.targetEntity != null) {
             dispatchMessage(ContextUtil.getMessage(MessageEnum.ENTITY_DESELECTED, this, MessageArgEnum.ARG_ITEM,
                     this.targetEntity));
         }
 
         this.targetEntity = targetEntity;
         if (dataBinder != null) {
             setDataBinderBeans();
             loadBinderProxySafe();
         }
 
         if (this.targetEntity != null) {
             this.targetEntityOrig = targetEntity.clone();
             arrangeForState(PanelState.FOCUSED);
             if (hasChanged) {
                 dispatchMessage(ContextUtil.getMessage(MessageEnum.ENTITY_SELECTED, this, MessageArgEnum.ARG_ITEM,
                         this.targetEntity));
             }
 
             for (DirtyListener listener : dirtyListeners) {
                 if (listener instanceof ComponentController) {
                     ((ComponentController) listener).setEntity(this.targetEntity);
                 }
             }
 
             //this mechanism is used for forcing screen refresh for calculated fields
             if (getMutableMode() != MutableMode.READONLY) {
                 this.targetEntity.addDirtyListener(new DirtyListener() {
                     @Override
                     public void onDirty(boolean dirty) {
                         if (dataBinder != null) {
                             dataBinder.loadAll();
                             setDirty(true);
                         }
                     }
                 });
             }
 
         } else {
             this.targetEntityOrig = null;
             if (isMasterDetail() && !hasMasterEntity()) {
                 arrangeForState(PanelState.UNDEFINED);
             } else {
                 arrangeForState(PanelState.READY);
             }
         }
 
 
         dispatchTitleChange();
     }
 
     protected void loadBinderProxySafe() {
 
         if (getMutableMode() != MutableMode.READONLY) {
             monitorComponents(false);
         }
 
         if (dataBinder != null) {
             if (targetEntity != null) {
                 for (Binding binding : dataBinder.getAllBindings()) {
                     PathMetadata pathMetadata = (PathMetadata) binding.getComponent().getAttribute(ATTRIB_PATH_META);
 
                     if (pathMetadata.getLastStep().isAssociationType()) {
                         pathMetadata.getLastStep().deproxyValue(targetEntity);
                     }
                 }
             }
 
             dataBinder.loadAll();
 
             if (targetEntity != null) {
                 for (Binding binding : dataBinder.getAllBindings()) {
                     if (statusHolderProp != null) {
 
                         final Component comp = binding.getComponent();
                         if (comp instanceof HtmlBasedComponent) {
                             ZkUtil.setInactive((HtmlBasedComponent) comp,
                                     statusHolderProp.getValue(targetEntity).equals(statusHolderProp.getAnnotation
                                             (StatusHolder.class).InactiveWhen()));
                         }
                     }
                 }
             }
 
             if (getMutableMode() != MutableMode.READONLY) {
                 monitorComponents(true);
             }
         }
     }
 
 
     @Override
     protected void arrangeForNullTargetType() {
         super.arrangeForNullTargetType();
         statusHolderProp = null;
         queryDialog = null;
         dataBinder = null;
         activeQuery = null;
         unregisterCommand(CommandEnum.QUERY);
         unregisterCommand(CommandEnum.REFRESH);
         unregisterCommand(CommandEnum.ADDNEW);
         unregisterCommand(CommandEnum.UPDATE);
         unregisterCommand(CommandEnum.DELETE);
         setTargetEntity(null);
         arrangeForState(PanelState.UNDEFINED);
     }
 
     @Override
     protected void arrangeForTargetType() {
         EntityMetadata entityMetadata = ContextUtil.getMRS().getEntityMetadata(getTargetType());
         statusHolderProp = entityMetadata.getAnnotatedProperty(StatusHolder
                 .class);
 
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.REFRESH, this));
 
         if (!isMasterDetail()) {
             registerCommand(ContextUtil.getDefaultCommand(CommandEnum.QUERY, this));
         }
 
         if (!ContextUtil.getMRS().getEntityMetadata(getTargetType()).isReadOnly()) {
             if (!entityMetadata.isDenyUpdate()) {
                 registerCommand(ContextUtil.getDefaultCommand(CommandEnum.UPDATE, this));
             }
             if (!entityMetadata.isTableSubset()) {
                 if (!entityMetadata.isDenyAddNew()) {
                     registerCommand(ContextUtil.getDefaultCommand(CommandEnum.ADDNEW, this));
                 }
                 if (!entityMetadata.isDenyDelete()) {
                     registerCommand(ContextUtil.getDefaultCommand(CommandEnum.DELETE, this));
                 }
             }
         }
 
         if (!isMasterDetail()) {
             arrangeForState(PanelState.READY);
         } else {
             arrangeForState(PanelState.UNDEFINED);
         }
     }
 
     @Override
     public void setDirty(boolean dirty) {
         //always notify component controllers
         if (dirty) {
             for (DirtyListener dirtyListener : dirtyListeners) {
                 if (dirtyListener instanceof ComponentController) {
                     dirtyListener.onDirty(true);
                 }
             }
         }
 
         if (this.dirty != null && this.dirty == dirty) {
             return;
         }
         this.dirty = dirty;
         monitorComponents(!this.dirty);
 
         for (DirtyListener dirtyListener : dirtyListeners) {
             if (!(dialogListener instanceof ComponentController)) {
                 dirtyListener.onDirty(this.dirty);
             }
         }
     }
 
     protected Component getBoundComponent(PathMetadata matchPath) {
         if (dataBinder == null || matchPath == null) {
             return null;
         }
 
         for (Binding binding : dataBinder.getAllBindings()) {
             final Component comp = binding.getComponent();
             final PathMetadata pathMetadata = (PathMetadata) comp.getAttribute(ATTRIB_PATH_META);
 
             if (pathMetadata.getPath().equals(matchPath.getPath())) {
                 return comp;
             }
         }
         return null;
     }
 
     protected void monitorComponents(boolean monitor) {
         for (Binding binding : dataBinder.getAllBindings()) {
             final Component comp = binding.getComponent();
             for (String event : MONITOR_EVENTS) {
                 if (monitor) {
                     comp.addEventListener(event, changeMonitor);
                 } else {
                     comp.removeEventListener(event, changeMonitor);
                 }
             }
         }
     }
 
     @Override
     protected void processValidCommand(Command command) {
         if (CommandEnum.QUERY.equals(command.getId())) {
             if (hasTargetType()) {
                 if (queryDialog == null) {
                     queryDialog = ContextUtil.getDefaultDialog(QueryDialog.class, getSettings(),
                             QueryResultMode.RETURN_ONE);
                 }
                 queryDialog.setInDesignMode(isInDesignMode());
                 queryDialog.setL10nMode(getL10nMode());
                 queryDialog.show(dialogListener);
             }
         } else if (CommandEnum.REFRESH.equals(command.getId())) {
             if (hasTargetEntity()) {
                 Entity refreshedEntity = null;
                 try {
                     refreshedEntity = ContextUtil.getDRS().refresh(getTargetEntity());
                 } catch (HibernateObjectRetrievalFailureException e) {
                     e.printStackTrace();
                     displayMessage(L10N_MSG_REFRESH_FAILED.toString(), true);
                 }
                 if (refreshedEntity != null) {
                     bind(refreshedEntity);
                 } else {
                     setTargetEntity(null);
                 }
             }
         } else if (CommandEnum.ADDNEW.equals(command.getId())) {
             if (hasTargetType()) {
                 // mirror query criteria
                 Entity templEntity = prepareMutableInstance(MutableMode.INSERT);
                 applyCurrentCritriaValues(getFinalQuery(), templEntity);
 
                 EntityPersisterDialog dialog = ContextUtil.getDefaultDialog(EntityPersisterDialog.class,
                         templEntity, getSettings(), MutableMode.INSERT);
                 dialog.setL10nMode(getL10nMode());
                 dialog.setMutableType(getMutableType());
                 dialog.show(dialogListener);
             }
         } else if (CommandEnum.UPDATE.equals(command.getId())) {
             if (hasTargetEntity()) {
                 EntityPersisterDialog dialog = ContextUtil.getDefaultDialog(EntityPersisterDialog.class,
                         prepareMutableInstance(MutableMode.UPDATE), getSettings(), MutableMode.UPDATE);
                 dialog.setL10nMode(getL10nMode());
                 dialog.setMutableType(getMutableType());
                 dialog.show(dialogListener);
             }
         } else {
             super.processValidCommand(command);
         }
     }
 
     protected Class<? extends MutablePanel> getMutableType() {
         return MutableEntityViewPanel.class;
     }
 
     @Override
     public boolean hasTargetEntity() {
         return getTargetEntity() != null;
     }
 
     // -------------------------- INNER CLASSES --------------------------
 
     private class ChangeMonitor implements EventListener<Event> {
         @Override
         public void onEvent(Event event) throws Exception {
             setDirty(true);
         }
     }
 
     private class DialogListener implements MessageListener {
         @Override
         public void processMessage(Message message) {
             switch (message.getId()) {
                 case AFFIRMATIVE_RESPONSE:
                     if (QueryDialog.class.isInstance(message.getSender())) {
                         Entity entity = message.getArg(MessageArgEnum.ARG_ITEM, Entity.class);
                         if (entity != null) {
                             bind(entity);
                             activeQuery = ((QueryDialog) message.getSender()).getQuery();
 
                             Command command = getCommand(CommandEnum.QUERY);
                             if (command != null) {
                                 command.dispatchMessage(ContextUtil.getMessage(MessageEnum.MARK_DIRTY, command,
                                         MessageArgEnum.ARG_ITEM, activeQuery.hasAttribute(CommandDecorator
                                         .ATTRIB_MODIFIED)));
                             }
 
                         }
                     } else if (EntityPersisterDialog.class.isInstance(message.getSender())) {
                         Entity entity = message.getArg(MessageArgEnum.ARG_ITEM, Entity.class);
                         if (entity != null) {
                             bind(entity);
                         }
                     }
                     break;
                 case ENTITY_UPDATED:
                     processEntityUpdate(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                     dispatchMessage(message);
                     break;
                 case ENTITY_INSERTED:
                     processEntityInsertion(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                     //dispatchMessage(ContextUtil.getMessage(message.getId(), this, message.getArgs()));
                     break;
                 case NEGATIVE_RESPONSE:
                     if (QueryDialog.class.isInstance(message.getSender())) {
                         activeQuery = ((QueryDialog) message.getSender()).getQuery();
                     }
                     break;
             }
         }
     }
 
     @Override
     protected void registerSettings() {
         super.registerSettings();
         registerSetting(SettingEnum.ASSUME_DETAIL_BEHAVIOR, false);
     }
 
     @Override
     protected boolean processEntityDeselection(Entity entity) {
         return (isMasterDetail() || getSettingValue(SettingEnum.ASSUME_DETAIL_BEHAVIOR,
                 false)) && processEntityDeletion(entity);
     }
 
     @Override
     protected boolean processEntityDeletion(Entity entity) {
         if (hasMasterEntity() && getMasterEntity().equals(entity)) {
             setMasterEntity(null);
             return true;
         } else if (hasTargetEntity() && getTargetEntity().equals(entity)) {
             setTargetEntity(null);
             return true;
         }
         return false;
     }
 
     @Override
     protected boolean processEntityInsertion(Entity entity) {
         if (canBind(entity)) {
             bindEcho(entity);
             return true;
         }
         return false;
     }
 
     @Override
     protected boolean processEntityUpdate(Entity entity) {
         if (hasTargetEntity() && getTargetEntity().equals(entity)) {
             setTargetEntity(entity);
             return true;
         }
         return false;
     }
 
     protected static Component buildViewer(String clazzName) {
         try {
             return (Component) Class.forName(clazzName).newInstance();
         } catch (Exception e) {
             e.printStackTrace();
             throw new IllegalArgumentException(clazzName + " not a valid zk component");
         }
     }
 
     protected static Grid buildGrid() {
         Grid grid = new Grid();
         grid.setVflex("true");
 //        grid.setWidth("100%");
         grid.setSpan(true);
         new Columns().setParent(grid);
         new Rows().setParent(grid);
         grid.getColumns().setSizable(true);
         Column col;
         col = new Column(L10N_COLUMN_ATTRIBUTE.toString());
         col.setParent(grid.getColumns());
         col.setWidth("30%");
         col = new Column(L10N_COLUMN_VALUE.toString());
         col.setParent(grid.getColumns());
 
         return grid;
     }
 
     protected void arrangeForRenderScheme(Grid grid, RenderScheme renderScheme) {
         arrangeForRenderScheme(grid, renderScheme, DEFAULT_BEAN_ID);
     }
 
     @SuppressWarnings("unchecked")
     protected void arrangeForRenderScheme(Grid grid, RenderScheme renderScheme, String BEAN_ID) {
         grid.getRows().getChildren().clear();
         for (Iterator<DirtyListener> iter = dirtyListeners.iterator(); iter.hasNext(); ) {
             if (iter.next() instanceof ComponentController) {
                 iter.remove();
             }
         }
 
 
         for (RenderElement element : renderScheme.getElements()) {
             if (element.getPropertyPath().isMultiStep() || element.getPropertyPath().getLastStep().isOneToManyType()
                     || element.getPropertyPath().getLastStep().isOneToOneType() || (MutableMode.READONLY ==
                     getMutableMode() && element.getPropertyPath().getLastStep().isAnnotatedWith(Encrypted.class))) {
                 continue;
             }
 
             Row row = new Row();
             row.setParent(grid.getRows());
 
             Html html = new Html(element.getFriendlyName());
             html.setZclass("z-label");
             html.setParent(row);
             if (getMutableMode() == MutableMode.UPDATE || getMutableMode() == MutableMode.INSERT) {
                 if (!element.getPropertyPath().getLastStep().isOptional()) {
                     StringBuilder sb = new StringBuilder();
                     sb.append(element.getFriendlyName());
                     sb.append(" ");
                     sb.append("<span title=\"");
                     sb.append(L10nUtil.getMessage("javax.validation.constraints.NotNull", "message",
                             "The field is mandatory"));
                     sb.append("\" style=\"color:red\">*</span>");
                     html.setContent(sb.toString());
                 }
             }
 
 
             Component comp;
             if (MutableMode.READONLY == getMutableMode() || element.getPropertyPath().getLastStep().getName().equals
                     (getBindProperty()) ||
                     (MutableMode.INSERT == getMutableMode() && !element.getPropertyPath().getLastStep().isInsertable
                             ()) ||
                     (MutableMode.UPDATE == getMutableMode() && !element.getPropertyPath().getLastStep().isUpdateable
                             ()) || element.isReadOnly()) {
                 if (!element.getPropertyPath().getLastStep().isAnnotatedWith(PropertyViewer.class)) {
                     comp = new PropertyBox(element);
                 } else {
                     comp = buildViewer(element.getPropertyPath().getLastStep().getAnnotation(PropertyViewer.class)
                             .className());
                 }
             } else {
                 if (!element.getPropertyPath().getLastStep().isAnnotatedWith(PropertyEditor.class)) {
                     comp = ZkUtil.getEditableComponentForRenderElement(element);
                 } else {
                     comp = buildViewer(element.getPropertyPath().getLastStep().getAnnotation(PropertyEditor.class)
                             .className());
                 }
 
                 //look for component controllers
                 if (element.getPropertyPath().getLastStep().isAnnotatedWith(ControllerHolder.class)) {
                     ComponentController controller = ContextUtil.getComponentController(getTargetType(),
                             comp.getClass());
                     if (controller != null) {
                         controller.setComponent(comp);
                         controller.setBinder(dataBinder);
                         addDirtyListener(controller);
                     }
                 }
             }
 
             if (comp instanceof HtmlBasedComponent) {
                 if (StringUtils.hasText(element.getWidth())) {
                     ((HtmlBasedComponent) comp).setWidth(element.getWidth());
                 }
                 if (StringUtils.hasText(element.getHeight())) {
                     ((HtmlBasedComponent) comp).setHeight(element.getHeight());
                 }
             }
 
             comp.setParent(row);
             ZkUtil.addBinding(dataBinder, comp, BEAN_ID, element.getPropertyPath().getPath());
             comp.setAttribute(ATTRIB_PATH_META, element.getPropertyPath());
         }
 
     }
 
     protected void arrangeForState(PanelState newState) {
         super.arrangeForState(newState);
 
         if (state == PanelState.FOCUSED && isMasterDetail()) {
             activateCommand(CommandEnum.ADDNEW, false);
         }
     }
 
     private Query getFinalQuery() {
         if (activeQuery == null) {
             activeQuery = getPersistedQuery();
             if (activeQuery == null && isMasterDetail() && hasMasterEntity()) {
                 activeQuery = ContextUtil.getEntityFactory().buildQuery(getTargetType());
             }
         }
 
         if (activeQuery != null) {
             Query finalQuery;
             if (isMasterDetail()) {
                 if (!hasMasterEntity()) {
                     return null;
                 }
 
                 finalQuery = (Query) activeQuery.clone();
                 finalQuery.addCriterion(new Path(getBindProperty()), Condition.EQ, getMasterEntity(), true);
 
                 return finalQuery;
             } else {
                 return activeQuery;
             }
         }
         return null;
     }
 
 }
