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
 
 import nu.xom.Element;
 import org.springframework.context.annotation.Scope;
 import org.springframework.core.NestedRuntimeException;
 import org.springframework.util.StringUtils;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.orm.PanelDefinition;
 import org.web4thejob.orm.Path;
 import org.web4thejob.orm.query.Condition;
 import org.web4thejob.orm.query.Query;
 import org.web4thejob.security.UnauthorizedResourceException;
 import org.web4thejob.util.XMLUtil;
 import org.web4thejob.web.panel.base.zk.AbstractZkContentPanel;
 import org.web4thejob.web.util.ZkUtil;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zul.*;
 
 /**
  * @author Veniamin Isaias
  * @since 3.2.1
  */
 
 @org.springframework.stereotype.Component
 @Scope("prototype")
 public class DefaultUserMenuPanel extends AbstractZkContentPanel implements UserMenuPanel, EventListener<Event> {
     private static final String ON_PANEL_LOAD = "onPanelLoad";
     private final Tree treeMenu = new Tree();
     private boolean rendered;
 
     public DefaultUserMenuPanel() {
         ZkUtil.setParentOfChild((Component) base, treeMenu);
         treeMenu.setSclass("w4tj-desktop-menu");
         //treeMenu.setHflex("true");
         treeMenu.setVflex("true");
         treeMenu.setSpan(true);
 
         Treecols treecols = new Treecols();
         treecols.setSizable(true);
         treecols.setParent(treeMenu);
         new Treecol().setParent(treecols);
         new Treechildren().setParent(treeMenu);
     }
 
     @Override
     public void render() {
         super.render();
 
         if (rendered) return;
         rendered = true;
 
         treeMenu.getTreechildren().getChildren().clear();
         Treeitem rootItem = new Treeitem();
         rootItem.setParent(treeMenu.getTreechildren());
         rootItem.setImage("img/ROOT.png");
         rootItem.setLabel(ContextUtil.getSessionContext().getSecurityContext().getUserIdentity().getFirstName() + " "
                 + ContextUtil.getSessionContext().getSecurityContext().getUserIdentity().getLastName());
 
         Element root = XMLUtil.getRootElement(ContextUtil.getSessionContext().getSecurityContext()
                 .getAuthorizationMenu());
         for (int i = 0; i < root.getChildElements().size(); i++) {
             Treeitem item = appendTreeNode(root.getChildElements().get(i), rootItem);
             if (i == 0 && item != null) {
                 item.setOpen(true);
             }
         }
 
         rootItem.setOpen(true);
     }
 
     private Treeitem appendTreeNode(Element node, Treeitem parentItem) {
         Treeitem item = null;
         if (DefaultMenuAuthorizationPanel.ELEMENT_MENU.equals(node.getLocalName())) {
             item = renderMenuItem(parentItem, XMLUtil.getTextualValue(node));
         } else if (DefaultMenuAuthorizationPanel.ELEMENT_PANEL.equals(node.getLocalName())) {
             String sid = XMLUtil.getTextualValue(node);
             if (ContextUtil.getSessionContext().getSecurityContext().isAccessible(sid)) {
                 Query query = ContextUtil.getEntityFactory().buildQuery(PanelDefinition.class);
                 query.addCriterion(new Path(PanelDefinition.FLD_BEANID), Condition.EQ, XMLUtil.getTextualValue(node));
                 PanelDefinition panelDefinition = ContextUtil.getDRS().findUniqueByQuery(query);
                 if (panelDefinition == null) {
                     return null;
                 }
                 item = renderPanelItem(parentItem, panelDefinition);
             }
         } else {
             return null;
         }
 
         if (item != null) {
             for (int i = 0; i < node.getChildElements().size(); i++) {
                 appendTreeNode(node.getChildElements().get(i), item);
             }
         }
 
         return item;
     }
 
     private Treeitem renderMenuItem(Treeitem parent, String name) {
         Treeitem item = new Treeitem();
         if (parent.getTreechildren() == null) {
             new Treechildren().setParent(parent);
         }
         item.setParent(parent.getTreechildren());
 
         if (item.getTreerow() == null) {
             new Treerow().setParent(item);
         }
         Treecell cell = new Treecell(name, "img/FOLDER.png");
         cell.setParent(item.getTreerow());
         cell.setStyle("white-space:nowrap;");
         item.setTooltiptext(name);
         item.setAttribute(DefaultMenuAuthorizationPanel.ELEMENT_MENU, name);
 
         return item;
     }
 
     private Treeitem renderPanelItem(Treeitem parent, PanelDefinition panelDefinition) {
         Treeitem item = new Treeitem();
         if (parent.getTreechildren() == null) {
             new Treechildren().setParent(parent);
         }
         item.setParent(parent.getTreechildren());
 
         if (item.getTreerow() == null) {
             new Treerow().setParent(item);
         }
         Treecell cell = new Treecell(panelDefinition.getName(), StringUtils.hasText(panelDefinition.getImage()) ?
                 panelDefinition.getImage() : "img/PANEL.png");
         cell.setParent(item.getTreerow());
         cell.setStyle("white-space:nowrap;");
         item.setTooltiptext(panelDefinition.getName());
 
         parent.setOpen(false);
         item.setParent(parent.getTreechildren());
         item.setAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL, panelDefinition.getBeanId());
         item.addEventListener(Events.ON_CLICK, this);
         item.addEventListener(ON_PANEL_LOAD, this);
         return item;
     }
 
     @Override
    public void clearBusy() {
        super.clearBusy();
        Clients.clearBusy();
    }

    @Override
    public void showBusy() {
        super.showBusy();
        Clients.showBusy(null);
    }

    @Override
     public void onEvent(Event event) throws Exception {
         if (Events.ON_CLICK.equals(event.getName()) && event.getTarget().hasAttribute(DefaultMenuAuthorizationPanel
                 .ELEMENT_PANEL)) {
             showBusy();
             Events.echoEvent(ON_PANEL_LOAD, event.getTarget(), null);
         } else if (ON_PANEL_LOAD.equals(event.getName())) {
             clearBusy();
             try {
                 org.web4thejob.web.panel.Panel panel = ContextUtil.getPanel(event.getTarget().getAttribute
                         (DefaultMenuAuthorizationPanel
                                 .ELEMENT_PANEL).toString());
 
                 dispatchMessage(ContextUtil.getMessage(MessageEnum.ADOPT_ME, panel));
 
             } catch (Exception e) {
                 if (e instanceof NestedRuntimeException && ((NestedRuntimeException) e).contains
                         (UnauthorizedResourceException.class)) {
                     displayMessage(L10N_UNAUTHORIZED_ACCESS.toString(), true);
                 } else {
                     displayMessage(L10N_UNEXPECTED_ERROR.toString(), true);
                 }
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void processMessage(Message message) {
         if (message.getId() == MessageEnum.PARENT_CHANGED) {
             rendered = false;
         }
         super.processMessage(message);
     }
 
     @Override
     public String findNextPanel(String beanid) {
         boolean found = false;
 
         for (Component component : treeMenu.getItems()) {
             if (component.hasAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL)) {
                 if (found) {
                     return (String) component.getAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL);
                 }
 
                 found = beanid.equals(component.getAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL));
             }
         }
 
         return null;
     }
 
     @Override
     public String findPreviousPanel(String beanid) {
         String previd = null;
 
         for (Component component : treeMenu.getItems()) {
             if (component.hasAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL)) {
                 if (beanid.equals(component.getAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL)) && previd !=
                         null) {
                     return previd;
 
                 }
 
                 previd = (String) component.getAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL);
             }
         }
 
         return null;
     }
 
     @Override
     public boolean select(String beanid) {
         for (Component component : treeMenu.getItems()) {
             if (beanid.equals(component.getAttribute(DefaultMenuAuthorizationPanel.ELEMENT_PANEL)) || beanid.equals
                     (component.getAttribute(DefaultMenuAuthorizationPanel.ELEMENT_MENU))) {
                 ((Treeitem) component).setSelected(true);
                 if (((Treeitem) component).getParentItem() != null) {
                     ((Treeitem) component).getParentItem().setOpen(true);
                 }
                 return true;
             }
         }
         return false;
     }
 }
