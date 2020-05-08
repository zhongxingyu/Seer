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
 
 package org.web4thejob.web.panel;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.util.StringUtils;
 import org.web4thejob.command.Command;
 import org.web4thejob.command.CommandDecorator;
 import org.web4thejob.command.CommandEnum;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageArgEnum;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.message.MessageListener;
 import org.web4thejob.orm.Entity;
 import org.web4thejob.orm.EntityMetadata;
 import org.web4thejob.orm.Path;
 import org.web4thejob.orm.PropertyMetadata;
 import org.web4thejob.orm.annotation.DefaultHolder;
 import org.web4thejob.orm.query.*;
 import org.web4thejob.orm.scheme.RenderElement;
 import org.web4thejob.orm.scheme.RenderScheme;
 import org.web4thejob.orm.scheme.RenderSchemeUtil;
 import org.web4thejob.orm.scheme.SchemeType;
 import org.web4thejob.print.Printer;
 import org.web4thejob.setting.SettingEnum;
 import org.web4thejob.util.CoreUtil;
 import org.web4thejob.util.L10nString;
 import org.web4thejob.web.dialog.EntityPersisterDialog;
 import org.web4thejob.web.dialog.QueryDialog;
 import org.web4thejob.web.dialog.RenderSchemeDialog;
 import org.web4thejob.web.panel.base.zk.AbstractZkBindablePanel;
 import org.web4thejob.web.util.ListboxRenderer;
 import org.web4thejob.web.util.ZkUtil;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.DropEvent;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listheader;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.event.ZulEvents;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 @org.springframework.stereotype.Component
 @Scope("prototype")
 public class DefaultListViewPanel extends AbstractZkBindablePanel implements ListViewPanel, EventListener<Event> {
     // ------------------------------ FIELDS ------------------------------
     public static final L10nString L10N_MSG_NO_ROWS_TO_DISPLAY = new L10nString(DefaultListViewPanel.class,
             "message_no_rows_to_display", "No data was found matching the criteria you set");
     public static final L10nString L10N_MSG_NO_QUERY_YET = new L10nString(DefaultListViewPanel.class,
             "message_no_query_yet", "No query has been executed yet");
 
     private static final String ON_DOUBLE_CLICK_ECHO = Events.ON_DOUBLE_CLICK + "Echo";
     private final Listbox listbox = new Listbox();
     private final DialogListener dialogListener = new DialogListener();
     private QueryDialog queryDialog;
     private Query activeQuery;
     private boolean inMemoryMode;
     private Entity targetEntity;
     private boolean refreshOnEverySave;
 
     // --------------------------- CONSTRUCTORS ---------------------------
 
     public DefaultListViewPanel() {
         ZkUtil.setParentOfChild((Component) base, listbox);
         //listbox.setHflex("1");
         listbox.setVflex("true");
         listbox.setSpan(true);
         //listbox.setSizedByContent(true);
         listbox.setItemRenderer(ContextUtil.getBean(ListboxRenderer.class));
         listbox.addEventListener(Events.ON_SELECT, this);
         listbox.addEventListener(Events.ON_DOUBLE_CLICK, this);
         listbox.addEventListener(ON_DOUBLE_CLICK_ECHO, this);
         listbox.setEmptyMessage(L10N_MSG_NO_QUERY_YET.toString());
         //listbox.addEventListener(Events.ON_, this);
     }
 
     // --------------------- GETTER / SETTER METHODS ---------------------
 
     @Override
     public boolean getInMemoryMode() {
         return inMemoryMode;
     }
 
     @Override
     public void setInMemoryMode(boolean inMemoryMode) {
         this.inMemoryMode = inMemoryMode;
     }
 
     @Override
     public Entity getTargetEntity() {
         return targetEntity;
     }
 
     // ------------------------ INTERFACE METHODS ------------------------
 
     // --------------------- Interface CommandAware ---------------------
 
     @Override
     public Set<CommandEnum> getSupportedCommands() {
         Set<CommandEnum> supported = new HashSet<CommandEnum>(super.getSupportedCommands());
         supported.add(CommandEnum.CONFIGURE_HEADERS);
         supported.add(CommandEnum.QUERY);
         supported.add(CommandEnum.REFRESH);
         supported.add(CommandEnum.ADDNEW);
         supported.add(CommandEnum.UPDATE);
         supported.add(CommandEnum.DELETE);
         supported.add(CommandEnum.PRINT);
         supported.add(CommandEnum.RELATED_PANELS);
         return Collections.unmodifiableSet(supported);
     }
 
     @Override
     protected void arrangeForState(PanelState newState) {
         super.arrangeForState(newState);
         activateCommand(CommandEnum.REFRESH, activeQuery != null);
     }
 
     // --------------------- Interface EventListener ---------------------
 
     @Override
     public void onEvent(Event event) throws Exception {
         if (Events.ON_SELECT.equals(event.getName())) {
             setTargetEntity((Entity) listbox.getModel().getElementAt(listbox.getSelectedIndex()));
         } else if (Events.ON_DOUBLE_CLICK.equals(event.getName())) {
             if (hasTargetEntity()) {
                 if (getSettingValue(SettingEnum.DISPATCH_DOUBLE_CLICK, false)) {
                     dispatchMessage(ContextUtil.getMessage(MessageEnum.ENTITY_ACCEPTED, this, MessageArgEnum.ARG_ITEM,
                             getTargetEntity()));
                } else if (hasCommand(CommandEnum.UPDATE)) {
                     Clients.showBusy(null);
                     Events.echoEvent(ON_DOUBLE_CLICK_ECHO, event.getTarget(), null);
                 }
             }
         } else if (ON_DOUBLE_CLICK_ECHO.equals(event.getName())) {
             Clients.clearBusy();
             Panel entityPanel = CoreUtil.getEntityViewPanel(getTargetEntity());
             if (entityPanel != null) {
                 dispatchMessage(ContextUtil.getMessage(MessageEnum.ADOPT_ME, entityPanel));
             }
         } else if (Events.ON_DROP.equals(event.getName())) {
             handleEntityDrop((DropEvent) event);
         } else if (ZulEvents.ON_PAGING.equals(event.getName())) {
             setTargetEntity(null);
         }
     }
 
 
     @SuppressWarnings("rawtypes")
     protected void handleEntityDrop(DropEvent event) {
         if (event.getDragged() instanceof Listitem && hasCommand(CommandEnum.ADDNEW)) {
 
             final Listitem draggedItem = (Listitem) event.getDragged();
             final ListModelList model = (ListModelList) draggedItem.getListbox().getModel();
             final Entity draggedEntity = (Entity) model.getElementAt(draggedItem.getIndex());
             Entity selectedEntity = null;
             if (!model.getSelection().isEmpty()) {
                 selectedEntity = (Entity) model.getSelection().iterator().next();
             }
 
             if (selectedEntity == null) {
                 draggedItem.setSelected(true);
             } else if (draggedEntity.equals(getTargetEntity())) {
                 return;
             } else if (draggedEntity.getEntityType().equals(getMasterType())) {
                 return;
             }
 
             Entity trgentity = prepareMutableInstance(MutableMode.INSERT);
             if (trgentity == null) {
                 return;
             }
             if (getTargetType().equals(draggedEntity.getEntityType())) {
                 trgentity = draggedEntity.clone();
             } else {
                 applyCurrentCritriaValues(getFinalQuery(), trgentity);
                 for (final PropertyMetadata propertyMetadata : ContextUtil.getMRS().getEntityMetadata(getTargetType()
                 ).getPropertiesMetadata()) {
                     if (propertyMetadata.isInsertable() && !propertyMetadata.getName().equals(getBindProperty()) &&
                             propertyMetadata.isAssociationType() && propertyMetadata.getAssociatedEntityMetadata()
                             .getEntityType().equals(draggedEntity.getEntityType())) {
                         propertyMetadata.setValue(trgentity, draggedEntity);
                         // break;
                     }
                 }
             }
 
             trgentity.setAsNew();
             EntityPersisterDialog dialog = ContextUtil.getDefaultDialog(EntityPersisterDialog.class, trgentity,
                     getSettings(), MutableMode.INSERT, inMemoryMode);
             dialog.setL10nMode(getL10nMode());
             dialog.show(dialogListener);
         }
     }
 
 
     // --------------------- Interface ListViewPanel ---------------------
 
     @Override
     @SuppressWarnings({"unchecked", "rawtypes"})
     public <E extends Entity> boolean add(E entity) {
         if (hasTargetType() && getTargetType().isInstance(entity)) {
             if (listbox.getModel() == null) {
                 listbox.setModel(new ListModelList());
                 resetSortingDirection(listbox);
             }
             ((ListModelList) listbox.getModel()).add(entity);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings("rawtypes")
     @Override
     public boolean removeSelected() {
         int index = listbox.getSelectedIndex();
         if (index >= 0) {
             ((ListModelList) listbox.getModel()).remove(index);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     @Override
     public boolean moveDownSelected() {
         int index = listbox.getSelectedIndex();
         if (index < listbox.getModel().getSize() - 1) {
             Entity entity = (Entity) listbox.getModel().getElementAt(index);
             ((ListModelList) listbox.getModel()).remove(index);
             ((ListModelList) listbox.getModel()).add(index + 1, entity);
             listbox.setSelectedIndex(index + 1);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     @Override
     public boolean moveUpSelected() {
         int index = listbox.getSelectedIndex();
         if (index > 0) {
             Entity entity = (Entity) listbox.getModel().getElementAt(index);
             ((ListModelList) listbox.getModel()).remove(index);
             ((ListModelList) listbox.getModel()).add(index - 1, entity);
             listbox.setSelectedIndex(index - 1);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     @Override
     public List<? extends Entity> getList() {
         if (listbox.getModel() != null) {
             return (ListModelList) listbox.getModel();
         }
         return null;
     }
 
     // --------------------- Interface MessageListener ---------------------
 
     @Override
     public void render() {
         super.render();
 
         if (getSettingValue(SettingEnum.RUN_QUERY_ON_STARTUP, false) && StringUtils.hasText(getSettingValue(SettingEnum
                 .PERSISTED_QUERY_NAME, (String) null)) && hasTargetType() && activeQuery == null && listbox.getPage
                 () != null) {
             Query q = CoreUtil.getQuery(getTargetType(), getSettingValue(SettingEnum
                     .PERSISTED_QUERY_NAME, (String) null));
             if (q != null) {
                 processMessage(ContextUtil.getMessage(MessageEnum.QUERY, this, MessageArgEnum.ARG_ITEM, q));
             }
         }
 
     }
 
     @Override
     public void processMessage(Message message) {
         switch (message.getId()) {
             case QUERY:
                 Query query = message.getArg(MessageArgEnum.ARG_ITEM, Query.class);
                 if (hasTargetType() && query != null && getTargetType().equals(query.getTargetType())) {
                     activeQuery = query;
                     refresh();
                 }
                 break;
             case ENTITY_INSERTED:
                 // lists process entity inserts sent from them only because this is
                 // the only
                 // way to make sure the added entity matches tne active query
                 break;
             case BIND_DIRECT:
                 bindEcho(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                 break;
             default:
                 super.processMessage(message);
                 break;
         }
     }
 
     // -------------------------- OTHER METHODS --------------------------
 
     @Override
     protected void arrangeForMasterEntity() {
         if (getMasterEntity() == null) {
             clear();
         } else if (getBindProperty() != null) {
             refresh();
         }
     }
 
     @SuppressWarnings({"rawtypes"})
     @Override
     public void clear() {
         listbox.setModel(new ListModelList());
         resetSortingDirection(listbox);
         setTargetEntity(null);
         setMasterEntity(null);
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     private void refresh() {
         listbox.setEmptyMessage(null);
         Query query = getFinalQuery();
         if (query != null) {
             setTargetEntity(null);
             listbox.setModel(new ListModelList(ContextUtil.getDRS().findByQuery(query)));
             if (listbox.getPaginal() != null) {
                 listbox.getPaginal().setActivePage(0);
             }
             resetSortingDirection(listbox);
             arrangeForState(PanelState.BROWSING);
 
             if (listbox.getModel() != null && listbox.getModel().getSize() == 0) {
                 listbox.setEmptyMessage(L10N_MSG_NO_ROWS_TO_DISPLAY.toString());
             }
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
                 for (Criterion criterion : activeQuery.getCriteria()) {
                     finalQuery.addCriterion(criterion);
                 }
                 for (OrderBy orderBy : activeQuery.getOrderings()) {
                     finalQuery.addOrderBy(orderBy);
                 }
 
                 finalQuery.addCriterion(new Path(getBindProperty()), Condition.EQ, getMasterEntity(), true);
 
                 return finalQuery;
             } else {
                 return activeQuery;
             }
         }
         return null;
     }
 
     private static void resetSortingDirection(Listbox listbox) {
         if (listbox.getListhead() != null) {
             for (Component item : listbox.getListhead().getChildren()) {
                 ((Listheader) item).setSortDirection("natural");
             }
         }
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     @Override
     protected void arrangeForTargetEntity(Entity targetEntity) {
         boolean hasChanged = (this.targetEntity != null && !this.targetEntity.equals(targetEntity)) || (targetEntity !=
                 null && !targetEntity.equals(this.targetEntity));
 
         if (hasChanged && this.targetEntity != null) {
             dispatchMessage(ContextUtil.getMessage(MessageEnum.ENTITY_DESELECTED, this, MessageArgEnum.ARG_ITEM,
                     this.targetEntity));
         }
 
         if (listbox.getModel() != null) {
             if (targetEntity == null) {
                 ((ListModelList) listbox.getModel()).clearSelection();
             } else {
                 if (!((ListModelList) listbox.getModel()).getSelection().contains(targetEntity)) {
                     ((ListModelList) listbox.getModel()).addToSelection(targetEntity);
                 }
             }
         }
 
         this.targetEntity = targetEntity;
         if (this.targetEntity != null) {
             arrangeForState(PanelState.FOCUSED);
             if (hasChanged) {
                 dispatchMessage(ContextUtil.getMessage(MessageEnum.ENTITY_SELECTED, this, MessageArgEnum.ARG_ITEM,
                         this.targetEntity));
             }
         } else {
             if (isMasterDetail() && !hasMasterEntity()) {
                 arrangeForState(PanelState.UNDEFINED);
             } else {
                 arrangeForState(PanelState.BROWSING);
             }
         }
 
         dispatchTitleChange();
     }
 
     @Override
     protected void arrangeForMasterType() {
         super.arrangeForMasterType();
         // bindCriterion=
     }
 
     @SuppressWarnings({"rawtypes"})
     @Override
     protected void arrangeForNullTargetType() {
         super.arrangeForNullTargetType();
         queryDialog = null;
         activeQuery = null;
         refreshOnEverySave = false;
         setTargetEntity(null);
         if (listbox.getListhead() != null) {
             listbox.getListhead().detach();
         }
         listbox.setModel(new ListModelList());
         resetSortingDirection(listbox);
         unregisterCommand(CommandEnum.CONFIGURE_HEADERS);
         unregisterCommand(CommandEnum.QUERY);
         unregisterCommand(CommandEnum.REFRESH);
         unregisterCommand(CommandEnum.ADDNEW);
         unregisterCommand(CommandEnum.UPDATE);
         unregisterCommand(CommandEnum.DELETE);
         unregisterCommand(CommandEnum.RELATED_PANELS);
 
         listbox.setDroppable("false");
         listbox.removeEventListener(Events.ON_DROP, this);
 
         arrangeForState(PanelState.UNDEFINED);
     }
 
     @Override
     protected void arrangeForTargetType() {
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.CONFIGURE_HEADERS, this));
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.QUERY, this));
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.REFRESH, this));
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.PRINT, this));
         if (!ZkUtil.isDialogContained(listbox)) {
             registerCommand(ContextUtil.getDefaultCommand(CommandEnum.RELATED_PANELS, this));
         }
         EntityMetadata entityMetadata = ContextUtil.getMRS().getEntityMetadata(getTargetType());
         if (!entityMetadata.isReadOnly()) {
             if (!entityMetadata.isDenyAddNew()) {
                 registerCommand(ContextUtil.getDefaultCommand(CommandEnum.ADDNEW, this));
             }
             if (!entityMetadata.isDenyUpdate()) {
                 registerCommand(ContextUtil.getDefaultCommand(CommandEnum.UPDATE, this));
             }
             if (!entityMetadata.isDenyDelete()) {
                 registerCommand(ContextUtil.getDefaultCommand(CommandEnum.DELETE, this));
             }
         }
 
         RenderScheme renderScheme = null;
         if (StringUtils.hasText(getSettingValue(SettingEnum.RENDER_SCHEME_FOR_VIEW, ""))) {
             renderScheme = RenderSchemeUtil.getRenderScheme(getSettingValue(SettingEnum
                     .RENDER_SCHEME_FOR_VIEW, ""),
                     getTargetType(), SchemeType.LIST_SCHEME);
         }
         if (renderScheme == null) {
             renderScheme = RenderSchemeUtil.getDefaultRenderScheme(getTargetType(), SchemeType.LIST_SCHEME);
         }
 
         ContextUtil.getBean(ListboxRenderer.class).arrangeForRenderScheme(listbox, renderScheme);
         monitorPagingEvents();
 
         if (hasCommand(CommandEnum.ADDNEW)) {
             listbox.setDroppable(getDroppableTypes());
             listbox.addEventListener(Events.ON_DROP, this);
         }
 
         refreshOnEverySave = !ContextUtil.getMRS().getAnnotationMetadata(getTargetType(),
                 DefaultHolder.class).isEmpty();
         arrangeForState(PanelState.READY);
     }
 
     private String getDroppableTypes() {
 
         List<String> types = new ArrayList<String>();
         RenderScheme renderScheme = (RenderScheme) listbox.getAttribute(ListboxRenderer.ATTRIB_RENDER_SCHEME);
         if (renderScheme != null) {
             for (RenderElement renderElement : renderScheme.getElements()) {
                 if (!renderElement.getPropertyPath().isMultiStep() && renderElement.getPropertyPath().getLastStep()
                         .isAssociationType()) {
                     Class<? extends Entity> entityType = renderElement.getPropertyPath().getLastStep()
                             .getAssociatedEntityMetadata().getEntityType();
                     if (isMasterDetail()) {
                         if (!entityType.equals(getMasterType())) {
                             types.add(entityType.getCanonicalName());
                         }
                     } else {
                         types.add(entityType.getCanonicalName());
                     }
                 }
             }
         }
 
         if (!types.isEmpty()) {
             return StringUtils.collectionToCommaDelimitedString(types);
         } else {
             return null;
         }
     }
 
 
     @Override
     protected <T extends Serializable> void onSettingValueChanged(SettingEnum id, T oldValue, T newValue) {
         if (SettingEnum.PERSISTED_QUERY_NAME == id || SettingEnum.PERSISTED_QUERY_DIALOG == id) {
             queryDialog = null;
         } else {
             super.onSettingValueChanged(id, oldValue, newValue);
         }
     }
 
     @Override
     protected boolean processEntityDeselection(Entity entity) {
         return !(isMasterDetail() && hasMasterEntity() && getMasterEntity().equals(entity)) || processEntityDeletion
                 (entity);
     }
 
     @SuppressWarnings({"rawtypes"})
     @Override
     protected boolean processEntityDeletion(Entity entity) {
         if (canBind(entity)) {
             if (hasMasterEntity() && getMasterEntity().equals(entity)) {
                 setMasterEntity(null);
                 return true;
             } else if (listbox.getModel() != null) {
                 int index = ((ListModelList) listbox.getModel()).indexOf(entity);
                 if (index >= 0) {
                     if (((ListModelList) listbox.getModel()).isSelected(entity)) {
                         setTargetEntity(null);
                     }
                     ((ListModelList) listbox.getModel()).remove(index);
                     return true;
                 }
             }
         }
         return false;
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     @Override
     protected boolean processEntityInsertion(Entity entity) {
         if (hasTargetType() && getTargetType().isInstance(entity)) {
             if (listbox.getModel() == null) {
                 listbox.setModel(new ListModelList());
                 resetSortingDirection(listbox);
             }
             ((ListModelList) listbox.getModel()).add(entity);
             setTargetEntity(entity);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     @Override
     protected boolean processEntityUpdate(Entity entity) {
         if (hasTargetType() && getTargetType().isInstance(entity)) {
             if (listbox.getModel() != null) {
                 int index = ((ListModelList) listbox.getModel()).indexOf(entity);
                 if (index >= 0) {
                     ((ListModelList) listbox.getModel()).set(index, entity);
                     if (((ListModelList) listbox.getModel()).isSelected(entity)) {
                         targetEntity = entity;
                         if (((ListModelList) listbox.getModel()).getSelection().contains(targetEntity)) {
                             dispatchTitleChange();
                         }
                     }
                     return true;
                 }
             }
         }
         return false;
     }
 
 
     @Override
     @SuppressWarnings("unchecked")
     protected void processValidCommand(Command command) {
         if (CommandEnum.QUERY.equals(command.getId())) {
             if (hasTargetType()) {
                 if (queryDialog == null) {
                     queryDialog = ContextUtil.getDefaultDialog(QueryDialog.class, getSettings(),
                             QueryResultMode.RETURN_QUERY);
                 }
                 queryDialog.setInDesignMode(isInDesignMode());
                 queryDialog.setL10nMode(getL10nMode());
                 queryDialog.show(dialogListener);
             }
         } else if (CommandEnum.ADDNEW.equals(command.getId())) {
 
             if (hasTargetType()) {
                 // mirror query criteria
                 Entity templEntity = prepareMutableInstance(MutableMode.INSERT);
                 applyCurrentCritriaValues(getFinalQuery(), templEntity);
 
                 EntityPersisterDialog dialog = ContextUtil.getDefaultDialog(EntityPersisterDialog.class,
                         templEntity, getSettings(), MutableMode.INSERT, inMemoryMode);
                 dialog.setL10nMode(getL10nMode());
                 dialog.show(dialogListener);
             }
         } else if (CommandEnum.UPDATE.equals(command.getId())) {
 
             if (hasTargetEntity()) {
                 EntityPersisterDialog dialog = ContextUtil.getDefaultDialog(EntityPersisterDialog.class,
                         prepareMutableInstance(MutableMode.UPDATE), getSettings(), MutableMode.UPDATE,
                         inMemoryMode);
                 dialog.setL10nMode(getL10nMode());
                 dialog.show(dialogListener);
             }
         } else if (CommandEnum.CONFIGURE_HEADERS.equals(command.getId())) {
 
             if (hasTargetType()) {
                 RenderScheme renderScheme = (RenderScheme) listbox.getAttribute(ListboxRenderer
                         .ATTRIB_RENDER_SCHEME);
                 RenderSchemeDialog dialog = ContextUtil.getDefaultDialog(RenderSchemeDialog.class, getSettings(),
                         SchemeType.LIST_SCHEME, renderScheme);
                 dialog.setL10nMode(getL10nMode());
                 dialog.show(dialogListener);
             }
         } else if (CommandEnum.REFRESH.equals(command.getId())) {
             refresh();
         } else if (CommandEnum.PRINT.equals(command.getId())) {
 
             if (listbox.getModel() != null || getFinalQuery() != null) {
                 String title = getSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata
                         (getTargetType()).getFriendlyName());
                 RenderScheme renderScheme = (RenderScheme) listbox.getAttribute(ListboxRenderer
                         .ATTRIB_RENDER_SCHEME);
                 Query query = getFinalQuery();
 
                 File file = ContextUtil.getBean(Printer.class).print(title, renderScheme, query,
                         (List) listbox.getModel());
                 ZkUtil.downloadCsv(file);
             }
         } else {
             super.processValidCommand(command);
         }
     }
 
     @Override
     public boolean hasTargetEntity() {
         return getTargetEntity() != null;
     }
 
     @Override
     protected void registerSettings() {
         registerSetting(SettingEnum.RENDER_SCHEME_FOR_VIEW, null);
         registerSetting(SettingEnum.RENDER_SCHEME_FOR_UPDATE, null);
         registerSetting(SettingEnum.RENDER_SCHEME_FOR_INSERT, null);
         registerSetting(SettingEnum.PERSISTED_QUERY_NAME, null);
         registerSetting(SettingEnum.PERSISTED_QUERY_DIALOG, null);
         registerSetting(SettingEnum.DISPATCH_DOUBLE_CLICK, false);
         registerSetting(SettingEnum.RUN_QUERY_ON_STARTUP, false);
         super.registerSettings();
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     private void replaceSelectedEntity(Entity entity) {
         // useful for in-memory editing where entities are new instances and
         // should be edited
         int index = listbox.getSelectedIndex();
         if (index >= 0) {
             ((ListModelList) listbox.getModel()).set(index, entity);
             targetEntity = entity;
         }
     }
 
 
     // -------------------------- INNER CLASSES --------------------------
 
     private class DialogListener implements MessageListener {
         @Override
         public void processMessage(Message message) {
             switch (message.getId()) {
                 case AFFIRMATIVE_RESPONSE:
                     if (QueryDialog.class.isInstance(message.getSender())) {
 
                         DefaultListViewPanel.this.processMessage(ContextUtil.getMessage(MessageEnum.QUERY, this,
                                 message.getArgs()));
 
                         Query query = message.getArg(MessageArgEnum.ARG_ITEM, Query.class);
                         Command command = getCommand(CommandEnum.QUERY);
                         if (command != null) {
                             command.dispatchMessage(ContextUtil.getMessage(MessageEnum.MARK_DIRTY, command,
                                     MessageArgEnum.ARG_ITEM, query.hasAttribute(CommandDecorator
                                     .ATTRIB_MODIFIED)));
                         }
 
                     } else if (EntityPersisterDialog.class.isInstance(message.getSender())) {
                         // for in-memory editing
                         Entity entity = message.getArg(MessageArgEnum.ARG_ITEM, Entity.class);
                         if (MutableMode.INSERT == ((EntityPersisterDialog) message.getSender()).getMutableMode()) {
                             processEntityInsertion(entity);
                             setTargetEntity(entity);
                         } else if (MutableMode.UPDATE == ((EntityPersisterDialog) message.getSender())
                                 .getMutableMode
                                         ()) {
                             replaceSelectedEntity(entity);
                             if (((EntityPersisterDialog) message.getSender()).isDirty()) {
                                 dispatchMessage(ContextUtil.getMessage(MessageEnum.VALUE_CHANGED,
                                         DefaultListViewPanel.this));
                             }
                         }
                     } else if (RenderSchemeDialog.class.isInstance(message.getSender())) {
                         if (hasCommand(CommandEnum.ADDNEW)) {
                             listbox.setDroppable(getDroppableTypes());
                         }
                         RenderScheme renderScheme = message.getArg(MessageArgEnum.ARG_ITEM, RenderScheme.class);
                         ContextUtil.getBean(ListboxRenderer.class).arrangeForRenderScheme(listbox,
                                 renderScheme);
                         monitorPagingEvents();
                         // force render so that style and format changes are
                         // re-applied
                         if (listbox.getModel() != null) {
                             listbox.setModel(listbox.getModel());
                         }
 
                         Command command = getCommand(CommandEnum.CONFIGURE_HEADERS);
                         if (command != null) {
                             command.dispatchMessage(ContextUtil.getMessage(MessageEnum.MARK_DIRTY, command,
                                     MessageArgEnum.ARG_ITEM, renderScheme.hasAttribute(CommandDecorator
                                     .ATTRIB_MODIFIED)));
                         }
 
                     }
                     break;
                 case ENTITY_UPDATED:
                     processEntityUpdate(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                     dispatchMessage(message);
                     if (refreshOnEverySave) {
                         refresh();
                     }
                     break;
                 case ENTITY_INSERTED:
                     processEntityInsertion(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                     //setTargetEntity(message.getArg(MessageArgEnum.ARG_ITEM, Entity.class));
                     //dispatchMessage(message);
                     if (refreshOnEverySave) {
                         refresh();
                     }
                     break;
             }
         }
     }
 
     private void monitorPagingEvents() {
         if (listbox.getPaginal() != null) {
             listbox.getPaginal().addEventListener(ZulEvents.ON_PAGING, this);
         }
     }
 
 }
