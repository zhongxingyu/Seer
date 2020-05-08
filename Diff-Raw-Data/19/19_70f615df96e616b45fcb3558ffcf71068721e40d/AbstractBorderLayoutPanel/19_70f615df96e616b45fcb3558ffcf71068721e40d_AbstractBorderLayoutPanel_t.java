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
 
 import org.springframework.util.StringUtils;
 import org.web4thejob.command.Command;
 import org.web4thejob.command.CommandAware;
 import org.web4thejob.command.CommandMerger;
 import org.web4thejob.command.CommandRenderer;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.setting.SettingAware;
 import org.web4thejob.setting.SettingEnum;
 import org.web4thejob.web.panel.Attributes;
 import org.web4thejob.web.panel.*;
 import org.web4thejob.web.panel.Panel;
 import org.web4thejob.web.panel.base.zk.AbstractZkLayoutPanel;
 import org.web4thejob.web.util.ZkUtil;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.OpenEvent;
 import org.zkoss.zul.*;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 public abstract class AbstractBorderLayoutPanel extends AbstractZkLayoutPanel implements CommandMerger {
 // ------------------------------ FIELDS ------------------------------
 
     protected static final String REGION_SIZE = "25%";
 
     private static final int DEFAULT_CHILDREN_COUNT = 5;
     private static final String[] defaultRegionOrder = {Borderlayout.CENTER, Borderlayout.NORTH, Borderlayout.SOUTH,
             Borderlayout.WEST, Borderlayout.EAST};
 
     private final Borderlayout blayout = new Borderlayout();
    private final SelectEventHandler selectHandler = new SelectEventHandler();
 
     @Override
     public void dispatchMessage(Message message) {
         if (MessageEnum.TITLE_CHANGED == message.getId() && subpanels.contains(message.getSender())) {
             super.dispatchMessage(ContextUtil.getMessage(message.getId(), this, message.getArgs()));
         } else {
             super.dispatchMessage(message);
         }
     }
 // --------------------------- CONSTRUCTORS ---------------------------
 
     protected AbstractBorderLayoutPanel() {
         ZkUtil.setParentOfChild((Component) base, blayout);
         blayout.setWidth("100%");
         blayout.setVflex("true");
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
     @Override
     protected void beforePersistencePhase() {
         if (blayout.getNorth() != null) {
             setSettingValue(SettingEnum.NORTH_ENABLED, true);
             setSettingValue(SettingEnum.NORTH_OPEN, blayout.getNorth().isOpen());
             setSettingValue(SettingEnum.NORTH_COLLAPSIBLE, blayout.getNorth().isCollapsible());
             setSettingValue(SettingEnum.NORTH_SPLITTABLE, blayout.getNorth().isSplittable());
         } else {
             setSettingValue(SettingEnum.NORTH_ENABLED, false);
         }
 
         if (blayout.getSouth() != null) {
             setSettingValue(SettingEnum.SOUTH_ENABLED, true);
             setSettingValue(SettingEnum.SOUTH_OPEN, blayout.getSouth().isOpen());
             setSettingValue(SettingEnum.SOUTH_COLLAPSIBLE, blayout.getSouth().isCollapsible());
             setSettingValue(SettingEnum.SOUTH_SPLITTABLE, blayout.getSouth().isSplittable());
         } else {
             setSettingValue(SettingEnum.SOUTH_ENABLED, false);
         }
 
         if (blayout.getEast() != null) {
             setSettingValue(SettingEnum.EAST_ENABLED, true);
             setSettingValue(SettingEnum.EAST_OPEN, blayout.getEast().isOpen());
             setSettingValue(SettingEnum.EAST_COLLAPSIBLE, blayout.getEast().isCollapsible());
             setSettingValue(SettingEnum.EAST_SPLITTABLE, blayout.getEast().isSplittable());
         } else {
             setSettingValue(SettingEnum.EAST_ENABLED, false);
         }
 
         if (blayout.getWest() != null) {
             setSettingValue(SettingEnum.WEST_ENABLED, true);
             setSettingValue(SettingEnum.WEST_OPEN, blayout.getWest().isOpen());
             setSettingValue(SettingEnum.WEST_COLLAPSIBLE, blayout.getWest().isCollapsible());
             setSettingValue(SettingEnum.WEST_SPLITTABLE, blayout.getWest().isSplittable());
         } else {
             setSettingValue(SettingEnum.WEST_ENABLED, false);
         }
 
         if (blayout.getCenter() != null) {
             setSettingValue(SettingEnum.CENTER_ENABLED, true);
         } else {
             setSettingValue(SettingEnum.CENTER_ENABLED, false);
         }
 
 
     }
 
 
 // --------------------- Interface Panel ---------------------
 
     @Override
     public void render() {
         super.render();
 
         if (!hasNorth() && getSettingValue(SettingEnum.NORTH_ENABLED, false)) {
             setNorth(ContextUtil.getDefaultPanel(PlaceholderPanel.class));
             getNorth().render();
         } else if (hasNorth() && !getSettingValue(SettingEnum.NORTH_ENABLED, false)) {
             subpanels.remove(getNorth());
         }
 
         if (!hasSouth() && getSettingValue(SettingEnum.SOUTH_ENABLED, false)) {
             setSouth(ContextUtil.getDefaultPanel(PlaceholderPanel.class));
             getSouth().render();
         } else if (hasSouth() && !getSettingValue(SettingEnum.SOUTH_ENABLED, false)) {
             subpanels.remove(getSouth());
         }
 
         if (!hasWest() && getSettingValue(SettingEnum.WEST_ENABLED, false)) {
             setWest(ContextUtil.getDefaultPanel(PlaceholderPanel.class));
             getWest().render();
         } else if (hasWest() && !getSettingValue(SettingEnum.WEST_ENABLED, false)) {
             subpanels.remove(getWest());
         }
 
         if (!hasEast() && getSettingValue(SettingEnum.EAST_ENABLED, false)) {
             setEast(ContextUtil.getDefaultPanel(PlaceholderPanel.class));
             getEast().render();
         } else if (hasEast() && !getSettingValue(SettingEnum.EAST_ENABLED, false)) {
             subpanels.remove(getEast());
         }
 
         if (!hasCenter() && getSettingValue(SettingEnum.CENTER_ENABLED, false)) {
             setCenter(ContextUtil.getDefaultPanel(PlaceholderPanel.class));
             getCenter().render();
         } else if (hasCenter() && !getSettingValue(SettingEnum.CENTER_ENABLED, false)) {
             subpanels.remove(getCenter());
         }
 
         if (blayout.getNorth() != null) {
             blayout.getNorth().setOpen(getSettingValue(SettingEnum.NORTH_OPEN, true));
             blayout.getNorth().setCollapsible(getSettingValue(SettingEnum.NORTH_COLLAPSIBLE, true));
             blayout.getNorth().setSplittable(getSettingValue(SettingEnum.NORTH_SPLITTABLE, true));
             blayout.getNorth().setHeight(getSettingValue(SettingEnum.NORTH_HEIGHT, REGION_SIZE));
            if (!blayout.getNorth().isOpen()) {
                blayout.getNorth().addEventListener(Events.ON_CLICK, selectHandler);
            }
         }
 
         if (blayout.getSouth() != null) {
             blayout.getSouth().setOpen(getSettingValue(SettingEnum.SOUTH_OPEN, true));
             blayout.getSouth().setCollapsible(getSettingValue(SettingEnum.SOUTH_COLLAPSIBLE, true));
             blayout.getSouth().setSplittable(getSettingValue(SettingEnum.SOUTH_SPLITTABLE, true));
             blayout.getSouth().setHeight(getSettingValue(SettingEnum.SOUTH_HEIGHT, REGION_SIZE));
            if (!blayout.getSouth().isOpen()) {
                blayout.getSouth().addEventListener(Events.ON_CLICK, selectHandler);
            }
         }
 
         if (blayout.getWest() != null) {
             blayout.getWest().setOpen(getSettingValue(SettingEnum.WEST_OPEN, true));
             blayout.getWest().setCollapsible(getSettingValue(SettingEnum.WEST_COLLAPSIBLE, true));
             blayout.getWest().setSplittable(getSettingValue(SettingEnum.WEST_SPLITTABLE, true));
             blayout.getWest().setWidth(getSettingValue(SettingEnum.WEST_WIDTH, REGION_SIZE));
            if (!blayout.getWest().isOpen()) {
                blayout.getWest().addEventListener(Events.ON_CLICK, selectHandler);
            }
         }
 
         if (blayout.getEast() != null) {
             blayout.getEast().setOpen(getSettingValue(SettingEnum.EAST_OPEN, true));
             blayout.getEast().setCollapsible(getSettingValue(SettingEnum.EAST_COLLAPSIBLE, true));
             blayout.getEast().setSplittable(getSettingValue(SettingEnum.EAST_SPLITTABLE, true));
             blayout.getEast().setWidth(getSettingValue(SettingEnum.EAST_WIDTH, REGION_SIZE));
            if (!blayout.getEast().isOpen()) {
                blayout.getEast().addEventListener(Events.ON_CLICK, selectHandler);
            }
         }
 
         mergeCommands();
     }
 
 // --------------------- Interface ParentCapable ---------------------
 
     @Override
     public boolean accepts(Panel panel) {
         String region;
         if (!panel.hasAttribute(Attributes.ATTRIB_REGION)) {
             region = findNextAvailableRegion();
         } else {
             region = panel.getAttribute(Attributes.ATTRIB_REGION, findNextAvailableRegion());
         }
         return region != null;
     }
 
     @Override
     public void setChildren(Collection<Panel> panels) {
         if (panels.size() != DEFAULT_CHILDREN_COUNT && !panels.isEmpty())
             throw new IllegalArgumentException("panel list should be either empty or have exactly 5 items (Center," +
                     "" + "" + "North,South,West,East)");
 
         int i = -1;
         for (Panel panel : panels) {
             i++;
             if (!PlaceholderPanel.class.isInstance(panel)) {
                 panel.setAttribute(Attributes.ATTRIB_REGION, defaultRegionOrder[i]);
                 subpanels.add(panel);
             }
         }
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     protected void afterRemove(Panel panel) {
         super.afterRemove(panel);
         String region = (String) panel.removeAttribute(Attributes.ATTRIB_REGION);
         if (region != null) {
             if (region.equals(Borderlayout.CENTER)) setSettingValue(SettingEnum.CENTER_ENABLED, false);
             else if (region.equals(Borderlayout.NORTH)) setSettingValue(SettingEnum.NORTH_ENABLED, false);
             else if (region.equals(Borderlayout.SOUTH)) setSettingValue(SettingEnum.SOUTH_ENABLED, false);
             else if (region.equals(Borderlayout.EAST)) setSettingValue(SettingEnum.EAST_ENABLED, false);
             else if (region.equals(Borderlayout.WEST)) setSettingValue(SettingEnum.WEST_ENABLED, false);
         }
 
         if (getCommandRenderer() != null) {
             if (panel instanceof CommandAware) {
                 getCommandRenderer().removeCommandOwner((CommandAware) panel);
                 getCommandRenderer().reset();
             }
         }
     }
 
 
     @Override
     protected void afterAdd(Panel panel) {
         super.afterAdd(panel);
 
         if (getCommandRenderer() != null) {
             if (panel instanceof CommandAware) {
                 getCommandRenderer().addCommandOwner((CommandAware) panel);
                 getCommandRenderer().reset();
             }
         }
     }
 
     @Override
     protected void beforeAdd(Panel panel) {
         super.beforeAdd(panel);
 
         if (!panel.hasAttribute(Attributes.ATTRIB_REGION)) {
             panel.setAttribute(Attributes.ATTRIB_REGION, findNextAvailableRegion());
         }
 
         String name = panel.getAttribute(Attributes.ATTRIB_REGION).toString();
         Panel oldPanel = getPanelByRegionName(name);
         if (oldPanel != null) {
             subpanels.remove(oldPanel);
         }
 
         LayoutRegion region = getNewRegionByName(name);
         panel.attach(region);
         region.setAttribute(Attributes.ATTRIB_PANEL, panel);
     }
 
     private String findNextAvailableRegion() {
         for (String region : defaultRegionOrder) {
             if (isRegionEnabled(region)) {
                 if (getRegionByName(region) == null) return region;
             }
         }
         return null;
     }
 
     private boolean isRegionEnabled(String name) {
         if (name.equals(Borderlayout.CENTER)) return getSettingValue(SettingEnum.CENTER_ENABLED, false);
         else if (name.equals(Borderlayout.NORTH)) return getSettingValue(SettingEnum.NORTH_ENABLED, false);
         else if (name.equals(Borderlayout.SOUTH)) return getSettingValue(SettingEnum.SOUTH_ENABLED, false);
         else if (name.equals(Borderlayout.EAST)) return getSettingValue(SettingEnum.EAST_ENABLED, false);
         else if (name.equals(Borderlayout.WEST)) return getSettingValue(SettingEnum.WEST_ENABLED, false);
 
         return false;
     }
 
     private LayoutRegion getNewRegionByName(String name) {
         LayoutRegion region;
         if (name.equals(Borderlayout.CENTER)) {
             setSettingValue(SettingEnum.CENTER_ENABLED, true);
             region = new Center();
         } else if (name.equals(Borderlayout.NORTH)) {
             setSettingValue(SettingEnum.NORTH_ENABLED, true);
             region = new North();
         } else if (name.equals(Borderlayout.SOUTH)) {
             setSettingValue(SettingEnum.SOUTH_ENABLED, true);
             region = new South();
         } else if (name.equals(Borderlayout.EAST)) {
             setSettingValue(SettingEnum.EAST_ENABLED, true);
             region = new East();
         } else if (name.equals(Borderlayout.WEST)) {
             setSettingValue(SettingEnum.WEST_ENABLED, true);
             region = new West();
         } else throw new IllegalStateException("region name is invalid: " + name);
 
         region.setParent(blayout);
         region.setBorder("none");
         region.addEventListener(Events.ON_OPEN, new OpenEventHandler());
         return region;
     }
 
     @Override
     protected void beforeRemove(Panel panel) {
         super.beforeRemove(panel);
         getRegionByName(panel.getAttribute(Attributes.ATTRIB_REGION).toString()).detach();
     }
 
     @Override
     protected void beforeReplace(Panel oldItem, Panel newItem) {
         newItem.setAttribute(Attributes.ATTRIB_REGION, oldItem.getAttribute(Attributes.ATTRIB_REGION));
     }
 
     protected Panel getCenter() {
         return getPanelByRegionName(Borderlayout.CENTER);
     }
 
     private Panel getPanelByRegionName(String name) {
         LayoutRegion region = getRegionByName(name);
         if (region == null) return null;
         else return (Panel) region.getAttribute(Attributes.ATTRIB_PANEL);
     }
 
     protected Panel getEast() {
         return getPanelByRegionName(Borderlayout.EAST);
     }
 
     protected Panel getNorth() {
         return getPanelByRegionName(Borderlayout.NORTH);
     }
 
     @Override
     protected Collection<Panel> getRenderedOrderOfChildren() {
         final List<Panel> subpanels = new ArrayList<Panel>();
         for (String element : defaultRegionOrder) {
             final LayoutRegion region = getRegionByName(element);
             if (region != null) {
                 subpanels.add((Panel) region.getAttribute(Attributes.ATTRIB_PANEL));
             } else {
                 subpanels.add(ContextUtil.getDefaultPanel(PlaceholderPanel.class));
             }
         }
         return subpanels;
     }
 
     protected Panel getSouth() {
         return getPanelByRegionName(Borderlayout.SOUTH);
     }
 
     protected Panel getWest() {
         return getPanelByRegionName(Borderlayout.WEST);
     }
 
     protected boolean hasCenter() {
         return getRegionByName(Borderlayout.CENTER) != null;
     }
 
     protected boolean hasEast() {
         return getRegionByName(Borderlayout.EAST) != null;
     }
 
     protected boolean hasNorth() {
         return getRegionByName(Borderlayout.NORTH) != null;
     }
 
     protected boolean hasSouth() {
         return getRegionByName(Borderlayout.SOUTH) != null;
     }
 
     protected boolean hasWest() {
         return getRegionByName(Borderlayout.WEST) != null;
     }
 
     @Override
     protected boolean isActive(Panel panel) {
         LayoutRegion region = getRegionByPanel(panel);
         return region.isOpen();
     }
 
     protected LayoutRegion getRegionByPanel(Panel panel) {
         if (subpanels.contains(panel) && panel.hasAttribute(Attributes.ATTRIB_REGION)) {
             return getRegionByName(panel.getAttribute(Attributes.ATTRIB_REGION).toString());
         }
         return null;
     }
 
     private LayoutRegion getRegionByName(String name) {
         if (name == null || name.trim().length() == 0) throw new IllegalStateException("region name cannot be empty");
         if (name.equals(Borderlayout.CENTER)) return blayout.getCenter();
         else if (name.equals(Borderlayout.NORTH)) return blayout.getNorth();
         else if (name.equals(Borderlayout.SOUTH)) return blayout.getSouth();
         else if (name.equals(Borderlayout.EAST)) return blayout.getEast();
         else if (name.equals(Borderlayout.WEST)) return blayout.getWest();
         else throw new IllegalStateException("region name is invalid: " + name);
     }
 
     private int getIndexByName(String name) {
         if (name == null || name.trim().length() == 0) throw new IllegalStateException("region name cannot be empty");
         if (name.equals(Borderlayout.CENTER)) return getSettingValue(SettingEnum.CENTER_CHILD_INDEX, 1);
         else if (name.equals(Borderlayout.NORTH)) return getSettingValue(SettingEnum.NORTH_CHILD_INDEX, 2);
         else if (name.equals(Borderlayout.SOUTH)) return getSettingValue(SettingEnum.SOUTH_CHILD_INDEX, 3);
         else if (name.equals(Borderlayout.EAST)) return getSettingValue(SettingEnum.EAST_CHILD_INDEX, 4);
         else if (name.equals(Borderlayout.WEST)) return getSettingValue(SettingEnum.WEST_CHILD_INDEX, 5);
         else throw new IllegalStateException("region name is invalid: " + name);
     }
 
     @Override
     protected <T extends Serializable> void onSettingValueChanged(SettingEnum id, T oldValue, T newValue) {
         if (id.equals(SettingEnum.CENTER_CHILD_INDEX)) {
             if (hasCenter()) {
                 getCenter().setIndex((Integer) newValue);
             }
         } else if (id.equals(SettingEnum.NORTH_CHILD_INDEX)) {
             if (hasNorth()) {
                 getNorth().setIndex((Integer) newValue);
             }
         } else if (id.equals(SettingEnum.SOUTH_CHILD_INDEX)) {
             if (hasSouth()) {
                 getSouth().setIndex((Integer) newValue);
             }
         } else if (id.equals(SettingEnum.EAST_CHILD_INDEX)) {
             if (hasEast()) {
                 getEast().setIndex((Integer) newValue);
             }
         } else if (id.equals(SettingEnum.WEST_CHILD_INDEX)) {
             if (hasWest()) {
                 getWest().setIndex((Integer) newValue);
             }
         } else {
             super.onSettingValueChanged(id, oldValue, newValue);
         }
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         super.afterPropertiesSet();
         for (Panel panel : subpanels) {
             panel.setIndex(getIndexByName(panel.getAttribute(Attributes.ATTRIB_REGION).toString()));
         }
         subpanels.sort();
     }
 
     @Override
     public SortedSet<Command> getMergedCommands() {
         SortedSet<Command> mergedCommands = new TreeSet<Command>(super.getCommands());
 
         if (getCommandRenderer() == null) {
             mergeCommands();
         }
 
         for (CommandAware commandAware : getCommandRenderer().getCommandOwners()) {
             if (!getCommandRenderer().getPrimaryOwner().equals(commandAware)) {
                 SortedSet<Command> commands;
                 if (commandAware instanceof CommandMerger) {
                     commands = ((CommandMerger) commandAware).getMergedCommands();
                 } else {
                     commands = commandAware.getCommands();
                 }
                 for (Command command : commands) {
                     if (commandAware instanceof CommandMerger) {
                         //hinto for ToolbarRenderer so that in multiple command mergers scenario
                         //the last merger (panel) can be identified so that it can be correctly mapped
                         //to the correct region and this region settings can be applied (eg NORTH_EXCLUDE_CRUD_COMMANDS)
                         command.setArg(CommandMerger.ATTRIB_COMMAND_MERGER, commandAware);
                     }
                     mergedCommands.add(command);
                 }
             }
         }
 
         return Collections.unmodifiableSortedSet(mergedCommands);
     }
 
     protected void mergeCommands() {
         if (getCommandRenderer() == null) {
             initCommandRenderer();
         }
         getCommandRenderer().reset();
         for (Panel panel : subpanels) {
             if (panel instanceof CommandAware) {
                 if (shouldMergeCommands(panel)) {
                     ((CommandAware) panel).supressCommands(true);
                     getCommandRenderer().addCommandOwner((CommandAware) panel);
                 } else {
                     if (panel instanceof SettingAware) {
                         ((CommandAware) panel).supressCommands(((SettingAware) panel).getSettingValue(SettingEnum
                                 .SUPRESS_COMMANDS, false));
                     } else {
                         ((CommandAware) panel).supressCommands(false);
                     }
                     getCommandRenderer().removeCommandOwner((CommandAware) panel);
                 }
                 panel.setAttribute(CommandRenderer.ATTRIB_SUPPRESS_CRUD_COMMANDS, shouldSuppressCrudCommands(panel));
                 panel.render();
             }
         }
 
         getCommandRenderer().render();
     }
 
     private boolean shouldMergeCommands(Panel panel) {
         if (panel instanceof PlaceholderPanel) {
             return false;
         } else if (panel.equals(getNorth())) {
             return getSettingValue(SettingEnum.NORTH_MERGE_COMMANDS, true);
         } else if (panel.equals(getSouth())) {
             return getSettingValue(SettingEnum.SOUTH_MERGE_COMMANDS, true);
         } else if (panel.equals(getCenter())) {
             return getSettingValue(SettingEnum.CENTER_MERGE_COMMANDS, true);
         } else if (panel.equals(getWest())) {
             return getSettingValue(SettingEnum.WEST_MERGE_COMMANDS, true);
         } else if (panel.equals(getEast())) {
             return getSettingValue(SettingEnum.EAST_MERGE_COMMANDS, true);
         } else {
             return false;
         }
     }
 
     private boolean shouldSuppressCrudCommands(Panel panel) {
         if (panel instanceof PlaceholderPanel) {
             return false;
         } else if (panel.equals(getNorth())) {
             return getSettingValue(SettingEnum.NORTH_EXCLUDE_CRUD_COMMANDS, true);
         } else if (panel.equals(getSouth())) {
             return getSettingValue(SettingEnum.SOUTH_EXCLUDE_CRUD_COMMANDS, true);
         } else if (panel.equals(getCenter())) {
             return getSettingValue(SettingEnum.CENTER_EXCLUDE_CRUD_COMMANDS, true);
         } else if (panel.equals(getWest())) {
             return getSettingValue(SettingEnum.WEST_EXCLUDE_CRUD_COMMANDS, true);
         } else if (panel.equals(getEast())) {
             return getSettingValue(SettingEnum.EAST_EXCLUDE_CRUD_COMMANDS, true);
         } else {
             return false;
         }
     }
 
 /*
     private Class<? extends Entity> getSovereignTargetType() {
         for (Panel panel : subpanels) {
             if (panel instanceof TargetType) {
                 if (((TargetType) panel).hasTargetType()) {
                     return ((TargetType) panel).getTargetType();
                 }
             }
         }
 
         return null;
     }
 */
 
     @Override
     protected void registerSettings() {
         super.registerSettings();
 
         registerSetting(SettingEnum.NORTH_ENABLED, true);
         registerSetting(SettingEnum.NORTH_OPEN, true);
         registerSetting(SettingEnum.NORTH_COLLAPSIBLE, true);
         registerSetting(SettingEnum.NORTH_SPLITTABLE, true);
         registerSetting(SettingEnum.NORTH_HEIGHT, REGION_SIZE);
         registerSetting(SettingEnum.NORTH_MERGE_COMMANDS, true);
         registerSetting(SettingEnum.NORTH_EXCLUDE_CRUD_COMMANDS, false);
         registerSetting(SettingEnum.NORTH_CHILD_INDEX, 1);
 
         registerSetting(SettingEnum.SOUTH_ENABLED, true);
         registerSetting(SettingEnum.SOUTH_OPEN, true);
         registerSetting(SettingEnum.SOUTH_COLLAPSIBLE, true);
         registerSetting(SettingEnum.SOUTH_SPLITTABLE, true);
         registerSetting(SettingEnum.SOUTH_HEIGHT, REGION_SIZE);
         registerSetting(SettingEnum.SOUTH_MERGE_COMMANDS, true);
         registerSetting(SettingEnum.SOUTH_EXCLUDE_CRUD_COMMANDS, false);
         registerSetting(SettingEnum.SOUTH_CHILD_INDEX, 5);
 
         registerSetting(SettingEnum.WEST_ENABLED, true);
         registerSetting(SettingEnum.WEST_OPEN, true);
         registerSetting(SettingEnum.WEST_COLLAPSIBLE, true);
         registerSetting(SettingEnum.WEST_SPLITTABLE, true);
         registerSetting(SettingEnum.WEST_WIDTH, REGION_SIZE);
         registerSetting(SettingEnum.WEST_MERGE_COMMANDS, true);
         registerSetting(SettingEnum.WEST_EXCLUDE_CRUD_COMMANDS, false);
         registerSetting(SettingEnum.WEST_CHILD_INDEX, 2);
 
         registerSetting(SettingEnum.EAST_ENABLED, true);
         registerSetting(SettingEnum.EAST_OPEN, true);
         registerSetting(SettingEnum.EAST_COLLAPSIBLE, true);
         registerSetting(SettingEnum.EAST_SPLITTABLE, true);
         registerSetting(SettingEnum.EAST_WIDTH, REGION_SIZE);
         registerSetting(SettingEnum.EAST_MERGE_COMMANDS, true);
         registerSetting(SettingEnum.EAST_EXCLUDE_CRUD_COMMANDS, false);
         registerSetting(SettingEnum.EAST_CHILD_INDEX, 4);
 
         registerSetting(SettingEnum.CENTER_ENABLED, true);
         registerSetting(SettingEnum.CENTER_MERGE_COMMANDS, true);
         registerSetting(SettingEnum.CENTER_EXCLUDE_CRUD_COMMANDS, false);
         registerSetting(SettingEnum.CENTER_CHILD_INDEX, 3);
 
     }
 
     protected boolean setCenter(Panel panel) {
         panel.setAttribute(Attributes.ATTRIB_REGION, Borderlayout.CENTER);
         return subpanels.add(panel);
     }
 
     protected boolean setEast(Panel panel) {
         panel.setAttribute(Attributes.ATTRIB_REGION, Borderlayout.EAST);
         return subpanels.add(panel);
     }
 
     protected boolean setNorth(Panel panel) {
         panel.setAttribute(Attributes.ATTRIB_REGION, Borderlayout.NORTH);
         return subpanels.add(panel);
     }
 
     protected boolean setSouth(Panel panel) {
         panel.setAttribute(Attributes.ATTRIB_REGION, Borderlayout.SOUTH);
         return subpanels.add(panel);
     }
 
     protected boolean setWest(Panel panel) {
         panel.setAttribute(Attributes.ATTRIB_REGION, Borderlayout.WEST);
         return subpanels.add(panel);
     }
 
     private class OpenEventHandler implements EventListener<OpenEvent> {
 
         @Override
         public void onEvent(OpenEvent event) throws Exception {
             if (event.isOpen()) {
                 Panel panel = (Panel) event.getTarget().getAttribute(Attributes.ATTRIB_PANEL);
                 flushCache(panel);
                 event.getTarget().removeEventListener(Events.ON_CLICK, selectHandler);
             } else {
                 event.getTarget().addEventListener(Events.ON_CLICK, selectHandler);
             }
         }
     }
 
     private class SelectEventHandler implements EventListener<Event> {
 
         @Override
         public void onEvent(Event event) throws Exception {
             Panel panel = (Panel) event.getTarget().getAttribute(Attributes.ATTRIB_PANEL);
             flushCache(panel);
         }
     }
 
     @Override
     public Set<CommandAware> getMergedOwners() {
         Set<CommandAware> owners = new HashSet<CommandAware>();
         for (Panel panel : subpanels) {
             if (panel instanceof CommandAware && shouldMergeCommands(panel)) {
                 owners.add((CommandAware) panel);
             }
         }
         return owners;
     }
 
     @Override
     public String toString() {
         if (!subpanels.isEmpty() && subpanels.first() instanceof BindCapable && ((BindCapable) subpanels.first())
                 .hasTargetEntity()) {
             return super.toString() + ": " + ((BindCapable) subpanels.first()).getTargetEntity().toString();
         } else if (!subpanels.isEmpty() && subpanels.first() instanceof ParentCapable) {
             String activeEntity = subpanels.first().toString();
             if (StringUtils.hasText(activeEntity) && activeEntity.contains(":")) {
                 return super.toString() + ": " + activeEntity.split(":")[1].trim();
             }
             return super.toString();
 
         } else {
             return super.toString();
         }
 
     }
 
 
 }
