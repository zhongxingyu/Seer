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
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.web4thejob.command.Command;
 import org.web4thejob.command.CommandAware;
 import org.web4thejob.command.CommandEnum;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageArgEnum;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.message.MessageListener;
 import org.web4thejob.orm.ORMUtil;
 import org.web4thejob.orm.PanelDefinition;
 import org.web4thejob.setting.SettingEnum;
 import org.web4thejob.util.CoreUtil;
 import org.web4thejob.util.L10nMessages;
 import org.web4thejob.util.L10nString;
 import org.web4thejob.util.XMLUtil;
 import org.web4thejob.web.dialog.AboutDialog;
 import org.web4thejob.web.dialog.Dialog;
 import org.web4thejob.web.dialog.PasswordDialog;
 import org.web4thejob.web.dialog.SelectPanelDialog;
 import org.web4thejob.web.panel.base.AbstractBorderLayoutPanel;
 import org.web4thejob.web.util.ZkUtil;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zul.Messagebox;
 
 import java.util.*;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
@org.springframework.stereotype.Component
@Scope("prototype")
 public class DefaultDesktopLayoutPanel extends AbstractBorderLayoutPanel implements DesktopLayoutPanel, L10nMessages {
 // ------------------------------ FIELDS ------------------------------
 
     public static final L10nString L10N_QUESTION_LOGOUT = new L10nString(DefaultDesktopLayoutPanel.class, "question",
             "Are you sure you want to logout?");
 
 // --------------------- Interface CommandAware ---------------------
 
 
     @Override
     public Set<CommandEnum> getSupportedCommands() {
         Set<CommandEnum> supported = new HashSet<CommandEnum>(super.getSupportedCommands());
         supported.add(CommandEnum.USER_DROPDOWN);
         supported.add(CommandEnum.TOOLS_DROPDOWN);
         return Collections.unmodifiableSet(supported);
     }
 
 // --------------------- Interface EventListener ---------------------
 
 
 // --------------------- Interface Panel ---------------------
 
     @Override
     public void clearBusy() {
         Clients.clearBusy();
     }
 
     @Override
     public void showBusy() {
         Clients.showBusy(null);
     }
 
     @Override
     protected boolean isActive(org.web4thejob.web.panel.Panel panel) {
         return true;
     }
 
     @Override
     protected void processValidCommand(Command command) {
         if (CommandEnum.SESSION_INFO.equals(command.getId())) {
             SessionInfoPanel panel = ContextUtil.getDefaultPanel(SessionInfoPanel.class);
             dispatchMessage(ContextUtil.getMessage(MessageEnum.ADOPT_ME, panel));
         } else if (CommandEnum.DESIGN_PANEL_ENTITY_VIEW.equals(command.getId())) {
             displayPanelForDesign(ContextUtil.getDefaultPanel(MutableEntityViewPanel.class));
         } else if (CommandEnum.DESIGN_PANEL_LIST_VIEW.equals(command.getId())) {
             displayPanelForDesign(ContextUtil.getDefaultPanel(ListViewPanel.class));
         } else if (CommandEnum.DESIGN_PANEL_HTML_VIEW.equals(command.getId())) {
             displayPanelForDesign(ContextUtil.getDefaultPanel(HtmlViewPanel.class));
         } else if (CommandEnum.DESIGN_PANEL_IFRAME_VIEW.equals(command.getId())) {
             displayPanelForDesign(ContextUtil.getDefaultPanel(FramePanel.class));
         } else if (CommandEnum.DESIGN_PANEL_TABBED_VIEW.equals(command.getId())) {
             displayPanelForDesign(ContextUtil.getDefaultPanel(TabbedLayoutPanel.class));
         } else if (CommandEnum.DESIGN_PANEL_BORDERED_VIEW.equals(command.getId())) {
             displayPanelForDesign(ContextUtil.getDefaultPanel(BorderedLayoutPanel.class));
         } else if (CommandEnum.DESIGN_PANEL_OTHER.equals(command.getId())) {
             Dialog dialog = ContextUtil.getDefaultDialog(SelectPanelDialog.class);
             dialog.setInDesignMode(isInDesignMode());
             dialog.setL10nMode(getL10nMode());
             dialog.show(new SelectPanelResponse());
         } else if (CommandEnum.REFRESH_CONTEXT.equals(command.getId())) {
             ContextUtil.getSessionContext().refresh();
             ContextUtil.getDRS().evictCache();
             ReloadableResourceBundleMessageSource messageSource = ContextUtil.getBean
                     (ReloadableResourceBundleMessageSource.class);
             messageSource.clearCacheIncludingAncestors();
         } else if (CommandEnum.DESIGN_MODE.equals(command.getId())) {
             final boolean designMode = (Boolean) command.getValue();
             setInDesignMode(designMode);
             render();
         } else if (CommandEnum.LOCALIZATION_MODE.equals(command.getId())) {
             final boolean l12nMode = (Boolean) command.getValue();
             setL10nMode(l12nMode);
             render();
         } else if (CommandEnum.LOGOUT.equals(command.getId())) {
             Messagebox.show(L10N_QUESTION_LOGOUT.toString(), L10nMessages.L10N_MSGBOX_TITLE_QUESTION.toString(),
                     new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, Messagebox.QUESTION,
                     new EventListener<Messagebox.ClickEvent>() {
                         @Override
                         public void onEvent(Messagebox.ClickEvent event) throws Exception {
                             if (event.getButton() == Messagebox.Button.OK) {
                                 Executions.sendRedirect("j_spring_security_logout");
                             }
                         }
                     });
         } else if (CommandEnum.SAVE_DESKTOP.equals(command.getId())) {
             saveDesktop();
         } else if (CommandEnum.CHANGE_PASSWORD.equals(command.getId())) {
             changePassword();
         } else if (CommandEnum.ABOUT.equals(command.getId())) {
             AboutDialog aboutDialog = ContextUtil.getDefaultDialog(AboutDialog.class);
             aboutDialog.show(null);
         } else {
             super.processValidCommand(command);
         }
     }
 
     @Override
     public void render() {
         super.render();
         arrangeForState(PanelState.READY);
         activateCommands(true);
     }
 
     private void saveDesktop() {
         PanelDefinition panelDefinition = ORMUtil.getUserDesktop();
         if (panelDefinition != null) {
             if (ContextUtil.getMRS().deproxyEntity(panelDefinition.getOwner()).equals
                     (ContextUtil.getSessionContext()
                             .getSecurityContext().getUserIdentity())) {
                 ContextUtil.getDWS().delete(panelDefinition);
             } else {
                 updateBeanName("<temp>");
             }
         }
 
         String xml = toSpringXml();
         panelDefinition = ContextUtil.getEntityFactory().buildPanelDefinition();
         panelDefinition.setBeanId(XMLUtil.getRootElementId(xml));
         panelDefinition.setName(ContextUtil.getSessionContext().getSecurityContext().getUserIdentity()
                 .getCode() + "\\Desktop");
         panelDefinition.setType(CoreUtil.describeClass(getClass()));
         panelDefinition.setOwner(ContextUtil.getSessionContext().getSecurityContext().getUserIdentity());
         panelDefinition.setDefinition(XMLUtil.toSpringBeanXmlResource(xml));
         ContextUtil.getDWS().save(panelDefinition);
         updateBeanName(panelDefinition.getBeanId());
         ContextUtil.getSessionContext().refresh();
     }
 
     private void changePassword() {
         PasswordDialog passwordDialog = ContextUtil.getDefaultDialog(PasswordDialog.class,
                 ContextUtil.getSessionContext().getSecurityContext().getUserIdentity(), true);
         passwordDialog.setL10nMode(getL10nMode());
         passwordDialog.show(new MessageListener() {
             @Override
             public void processMessage(Message message) {
                 if (MessageEnum.AFFIRMATIVE_RESPONSE == message.getId()) {
                     String newPassword = message.getArg(MessageArgEnum.ARG_ITEM, String.class);
                     String oldPassword = message.getArg(MessageArgEnum.ARG_OLD_ITEM, String.class);
                     if (!ContextUtil.getSessionContext().getSecurityContext().renewPassword(oldPassword,
                             newPassword)) {
                         //huge fuckup, close down
                         ContextUtil.getSessionContext().getSecurityContext().clearContext();
                         Executions.sendRedirect(null);
                     }
                 }
             }
         });
     }
 
     @Override
     protected void registerCommands() {
         super.registerCommands();
 
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.USER_DROPDOWN, this));
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.TOOLS_DROPDOWN, this));
 
         if (getCommandRenderer() != null) {
             getCommandRenderer().setAlign("end");
         }
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         super.afterPropertiesSet();
 
         if (!isPersisted()) {
             setSettingValue(SettingEnum.CENTER_ENABLED, true);
             setSettingValue(SettingEnum.WEST_ENABLED, true);
             setSettingValue(SettingEnum.WEST_OPEN, true);
             setSettingValue(SettingEnum.WEST_SPLITTABLE, true);
             setSettingValue(SettingEnum.WEST_COLLAPSIBLE, true);
             setSettingValue(SettingEnum.WEST_WIDTH, "250px");
             setSettingValue(SettingEnum.NORTH_ENABLED, false);
             setSettingValue(SettingEnum.EAST_ENABLED, false);
             setSettingValue(SettingEnum.SOUTH_ENABLED, false);
             super.render();
 
             UserMenuPanel userMenuPanel = ContextUtil.getDefaultPanel(UserMenuPanel.class);
             setWest(userMenuPanel);
 
             TabbedLayoutPanel tabbedLayoutPanel = ContextUtil.getDefaultPanel(TabbedLayoutPanel.class);
             tabbedLayoutPanel.setSettingValue(SettingEnum.HONOR_ADOPTION_REQUEST, true);
             tabbedLayoutPanel.setSettingValue(SettingEnum.SCLASS, "w4tj-desktop-background");
             tabbedLayoutPanel.setSettingValue(SettingEnum.DISABLE_CROSS_TAB_BINDING, true);
             setCenter(tabbedLayoutPanel);
         }
     }
 
     private void displayPanelForDesign(org.web4thejob.web.panel.Panel panel) {
         dispatchMessage(ContextUtil.getMessage(MessageEnum.ADOPT_ME, panel));
         if (panel instanceof DesignModeAware) {
             ((DesignModeAware) panel).setInDesignMode(true);
         }
         if (panel instanceof I18nAware) {
             ((I18nAware) panel).setL10nMode(getL10nMode());
         }
         panel.render();
         if (panel instanceof CommandAware && !panel.isPersisted()) {
             Command command = ((CommandAware) panel).getCommand(CommandEnum.CONFIGURE_SETTINGS);
             if (command != null) {
                 command.process();
             }
         }
     }
 
     @Override
     public <T extends Panel> List<T> getActiveInstances(Class<T> panelType) {
         List<T> list = new ArrayList<T>();
         ZkUtil.appendPanelsOfType((Component) base, panelType, list);
         return list;
     }
 
     @Override
     public boolean addTab(Panel child) {
         Panel center = getCenter();
         if (center instanceof ParentCapable) {
             return ((ParentCapable) center).getSubpanels().add(child);
         }
 
         return false;
     }
 
 
 // -------------------------- INNER CLASSES --------------------------
 
     private class SelectPanelResponse implements MessageListener {
         @Override
         public void processMessage(Message message) {
             if (MessageEnum.AFFIRMATIVE_RESPONSE == message.getId()) {
                 org.web4thejob.web.panel.Panel panel = ContextUtil.getPanel(message.getArgs().get(MessageArgEnum
                         .ARG_ITEM)
                         .toString());
                 displayPanelForDesign(panel);
             }
         }
     }
 
 }
