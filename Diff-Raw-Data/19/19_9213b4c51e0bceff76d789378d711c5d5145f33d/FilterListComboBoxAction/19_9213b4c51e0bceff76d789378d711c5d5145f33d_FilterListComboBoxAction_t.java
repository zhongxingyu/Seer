 /*
  * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
  *
  * license to be determined.
  */
 package com.echologic.xfiles;
 
 import java.util.Iterator;
 import javax.swing.JComponent;
 
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.DefaultActionGroup;
 import com.intellij.openapi.actionSystem.Presentation;
 import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.project.Project;
 
 /**
  * This class represents a menu of available filter configurations to select from.
  *
  * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
  */
 public class FilterListComboBoxAction extends ComboBoxAction {
 
     private Logger log = Logger.getInstance(getClass().getName());
 
     private Project project;
     private RefreshAction refreshAction;
     private XFilesConfiguration configuration;
 
     // TODO: we should have a class to hold an action and it's associated filter
     // perhaps we could hold the group in this class too?
 
     private SelectFilterAction[] actions;
     private XFilesVirtualFileFilter[] filters;
     private DefaultActionGroup group;
 
     private Presentation presentation;
 
     public FilterListComboBoxAction(Project project, RefreshAction refreshAction) {
         this.project = project;
         this.refreshAction = refreshAction;
         this.group = new DefaultActionGroup();
     }
 
     public void setConfiguration(XFilesConfiguration configuration) {
         this.configuration = configuration;
         configuration.setListener(this);
 
         log.debug("initializing configuration");
         log.debug("selected " + configuration.SELECTED_FILTER);
         log.debug("scroll to source " + configuration.SCROLL_TO_SOURCE);
         log.debug("scroll from source " + configuration.SCROLL_FROM_SOURCE);
         log.debug(configuration.CONFIGURED_FILTERS.size() + " configured filters");
 
         for (Iterator iterator = configuration.CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
             XFilesFilterConfiguration filter = (XFilesFilterConfiguration) iterator.next();
             log.debug("filter " + filter.NAME);
         }
 
         configurePopupActionGroup();
     }
 
     public void setSelected(AnActionEvent event, int index) {
         presentation.setText(actions[index].getName());
         refreshAction.setFilter(filters[index]);
         refreshAction.actionPerformed(event);
         configuration.SELECTED_FILTER = index;
     }
 
     /**
      * This method is called shortly before createPopupActionGroup to create the component
      * which is passed to createPopupActionGroup. It is overridden here so we can grab the
      * presentation and use it for changing the displayed text when the filter selection
      * changes.
      */
     public JComponent createCustomComponent(Presentation presentation) {
         this.presentation = presentation;
         // TODO: probably need something sensible here
         presentation.setText("filter");
         presentation.setDescription("change/configure filter selections");
         return super.createCustomComponent(presentation);
     }
 
     /**
      * Note that the only way the list of available SelectFilterAction's can change
      * is through the EditConfigurationsAction. When some SelectFilterAction is
      * performed we need to update the presentation text.
      */
     protected DefaultActionGroup createPopupActionGroup(JComponent component) {
         log.debug("createPopupActionGroup");
         return group;
     }
 
     /**
      * This should be called when the configuration changes
      */
     public void configurePopupActionGroup() {
         log.debug("configurePopupActionGroup");
 
         int size = 0;
         if (configuration != null) size = configuration.CONFIGURED_FILTERS.size();
 
         if (size > 0) {
             actions = new SelectFilterAction[size];
             filters = new XFilesVirtualFileFilter[size];
 
             for (int i=0; i<size; i++) {
                 XFilesFilterConfiguration config = (XFilesFilterConfiguration) configuration.CONFIGURED_FILTERS.get(i);
                 SelectFilterAction action = new SelectFilterAction(this, config.NAME, i);
                 actions[i] = action;
 
                 XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
                 filter.setConfiguration(config);
                 filters[i] = filter;
 
                 log.debug("added filter " + filter.getName());
             }
 
         } else {
             actions = new SelectFilterAction[1];
             filters = new XFilesVirtualFileFilter[1];
 
             String name = "default filter";
 
             SelectFilterAction action = new SelectFilterAction(this, name, 0);
             actions[0] = action;
 
             XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
             filter.setName(name);
             filters[0] = filter;
 
             log.debug("added filter " + filter.getName());
 
             configuration.SELECTED_FILTER = 0;
         }
 
         group.removeAll();
         for (int i=0; i<actions.length; i++) {
             group.add(actions[i]);
         }
 
         XFilesVirtualFileFilter selected = filters[configuration.SELECTED_FILTER];
         refreshAction.setFilter(selected);
 
        if (presentation != null) presentation.setText(selected.getName());
     }
 
 }
