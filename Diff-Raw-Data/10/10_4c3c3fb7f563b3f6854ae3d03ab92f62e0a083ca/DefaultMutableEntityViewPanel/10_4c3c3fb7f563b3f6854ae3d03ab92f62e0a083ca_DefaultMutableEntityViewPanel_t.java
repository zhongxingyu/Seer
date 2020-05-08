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
 import org.web4thejob.orm.Path;
 import org.web4thejob.orm.query.Condition;
 import org.web4thejob.orm.query.Criterion;
 import org.web4thejob.orm.query.OrderBy;
 import org.web4thejob.orm.query.Query;
 import org.web4thejob.orm.scheme.RenderScheme;
 import org.web4thejob.orm.scheme.RenderSchemeUtil;
 import org.web4thejob.orm.scheme.SchemeType;
 import org.web4thejob.print.Printer;
 import org.web4thejob.setting.SettingEnum;
 import org.web4thejob.web.dialog.RenderSchemeDialog;
 import org.web4thejob.web.panel.base.AbstractMutablePanel;
 import org.web4thejob.web.util.ZkUtil;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zkplus.databind.DataBinder;
 import org.zkoss.zul.Grid;
 
 import java.io.File;
import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 @org.springframework.stereotype.Component
 @Scope("prototype")
 public class DefaultMutableEntityViewPanel extends AbstractMutablePanel implements MutableEntityViewPanel,
         MutablePanel {
 // ------------------------------ FIELDS ------------------------------
 
     private final Grid grid;
     private RenderScheme renderScheme;
 
 // -------------------------- STATIC METHODS --------------------------
 
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public DefaultMutableEntityViewPanel() {
         this(MutableMode.READONLY);
     }
 
     public DefaultMutableEntityViewPanel(MutableMode mutableMode) {
         super(mutableMode);
         grid = buildGrid();
         ZkUtil.setParentOfChild((Component) base, grid);
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface CommandAware ---------------------
 
     @Override
     public Set<CommandEnum> getSupportedCommands() {
         Set<CommandEnum> supported = new HashSet<CommandEnum>(super.getSupportedCommands());
         supported.add(CommandEnum.CONFIGURE_HEADERS);
         supported.add(CommandEnum.RELATED_PANELS);
         return Collections.unmodifiableSet(supported);
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     protected void afterSettingsSet() {
         super.afterSettingsSet();
         arrangeForRenderScheme();
     }
 
     @Override
     public void arrangeForNullTargetType() {
         super.arrangeForNullTargetType();
         clear();
         renderScheme = null;
     }
 
     protected void clear() {
         grid.getRows().getChildren().clear();
     }
 
     @Override
     protected void arrangeForTargetType() {
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.CONFIGURE_HEADERS, this));
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.PRINT, this));
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.RELATED_PANELS, this));
         super.arrangeForTargetType();
         arrangeForRenderScheme();
     }
 
     protected void arrangeForRenderScheme() {
         if (!hasTargetType()) {
             return;
         }
 
         if (renderScheme == null) {
             if (getMutableMode() == MutableMode.READONLY && StringUtils.hasText(getSettingValue(SettingEnum
                     .RENDER_SCHEME_FOR_VIEW, ""))) {
                 renderScheme = RenderSchemeUtil.getRenderScheme(getSettingValue(SettingEnum.RENDER_SCHEME_FOR_VIEW,
                         ""), getTargetType(), SchemeType.ENTITY_SCHEME);
             } else if (getMutableMode() == MutableMode.UPDATE && StringUtils.hasText(getSettingValue(SettingEnum
                     .RENDER_SCHEME_FOR_UPDATE, ""))) {
                 renderScheme = RenderSchemeUtil.getRenderScheme(getSettingValue(SettingEnum.RENDER_SCHEME_FOR_UPDATE,
                         ""), getTargetType(), SchemeType.ENTITY_SCHEME);
             } else if (getMutableMode() == MutableMode.INSERT && StringUtils.hasText(getSettingValue(SettingEnum
                     .RENDER_SCHEME_FOR_INSERT, ""))) {
                 renderScheme = RenderSchemeUtil.getRenderScheme(getSettingValue(SettingEnum.RENDER_SCHEME_FOR_INSERT,
                         ""), getTargetType(), SchemeType.ENTITY_SCHEME);
             }
 
             if (renderScheme == null) {
                 renderScheme = RenderSchemeUtil.getDefaultRenderScheme(getTargetType(), SchemeType.ENTITY_SCHEME);
             }
         }
 
         dataBinder = new DataBinder();
         super.arrangeForRenderScheme(grid, renderScheme);
     }
 
 
     @Override
     protected void processValidCommand(Command command) {
         if (CommandEnum.PRINT.equals(command.getId())) {
             if (hasTargetEntity()) {
                 String title = getSettingValue(SettingEnum.PANEL_NAME, ContextUtil.getMRS().getEntityMetadata
                         (getTargetType()).getFriendlyName());
                 Query query = null;
                 if (isMasterDetail() && hasMasterEntity()) {
                     query = ContextUtil.getEntityFactory().buildQuery(getTargetType());
                     query.addCriterion(new Path(getBindProperty()), Condition.EQ, getMasterEntity(), true, true);
                 }
 
                 File file = ContextUtil.getBean(Printer.class).print(title, renderScheme, query, getTargetEntity());
                 ZkUtil.downloadCsv(file);
             }
         } else if (CommandEnum.CONFIGURE_HEADERS.equals(command.getId())) {
             if (hasTargetType()) {
                 RenderSchemeDialog dialog = ContextUtil.getDefaultDialog(RenderSchemeDialog.class, getSettings(),
                         SchemeType.ENTITY_SCHEME, renderScheme);
                 dialog.setL10nMode(getL10nMode());
                 dialog.show(new RenderSchemeDialogListener());
             }
         } else {
             super.processValidCommand(command);
         }
     }
 
     @Override
     protected void registerSettings() {
         registerSetting(SettingEnum.RENDER_SCHEME_FOR_VIEW, null);
         registerSetting(SettingEnum.RENDER_SCHEME_FOR_UPDATE, null);
         registerSetting(SettingEnum.RENDER_SCHEME_FOR_INSERT, null);
         registerSetting(SettingEnum.PERSISTED_QUERY_NAME, null);
         registerSetting(SettingEnum.PERSISTED_QUERY_DIALOG, null);
         registerSetting(SettingEnum.RUN_QUERY_ON_STARTUP, false);
         super.registerSettings();
     }
 
 // -------------------------- INNER CLASSES --------------------------
 
     private class RenderSchemeDialogListener implements MessageListener {
         @Override
         public void processMessage(Message message) {
             switch (message.getId()) {
                 case AFFIRMATIVE_RESPONSE:
                     if (RenderSchemeDialog.class.isInstance(message.getSender())) {
                         renderScheme = message.getArg(MessageArgEnum.ARG_ITEM, RenderScheme.class);
                         arrangeForRenderScheme();
                         if (hasTargetEntity()) {
                             bind(getTargetEntity());
                         }
 
                         Command command = getCommand(CommandEnum.CONFIGURE_HEADERS);
                         if (command != null) {
                             command.dispatchMessage(ContextUtil.getMessage(MessageEnum.MARK_DIRTY, command,
                                     MessageArgEnum.ARG_ITEM, renderScheme.hasAttribute(CommandDecorator
                                     .ATTRIB_MODIFIED)));
                         }
 
                     }
                     break;
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
 
    @Override
    protected <T extends Serializable> void onSettingValueChanged(SettingEnum id, T oldValue, T newValue) {
        //needed for issue #3
        if (id == SettingEnum.RENDER_SCHEME_FOR_INSERT || id == SettingEnum.RENDER_SCHEME_FOR_UPDATE) {
            renderScheme = null;
        }
        super.onSettingValueChanged(id, oldValue, newValue);
    }
 }
