 /*
  * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
  *
  * license to be determined.
  */
 package com.echologic.xfiles;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 import com.intellij.openapi.actionSystem.ActionManager;
 import com.intellij.openapi.actionSystem.ActionToolbar;
 import com.intellij.openapi.actionSystem.DefaultActionGroup;
 import com.intellij.openapi.components.ProjectComponent;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.options.Configurable;
 import com.intellij.openapi.project.Project;
 
 /**
  * This configuration editor panel is modeled after the JUnit run configuration editor.
  *
  * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
  */
 public class XFilesConfigurable implements Configurable, ProjectComponent {
 
     private Logger log = Logger.getInstance(getClass().getName());
 
     private Project project;
 
     private XFilesConfiguration configuration;
 
     private JPanel panel;
 
     private DefaultListModel filterListModel;
     private JList filterList;
 
     private DefaultListModel testListModel;
     private JList testList;
 
     private JTextField filterNameField;
     private ConfigurationTable statusTable;
     private ConfigurationTable typeTable;
     private ConfigurationTable vcsTable;
     private ConfigurationTable moduleTable;
     //private ConfigurationTable globTable;
     private ConfigurationTable otherTable;
 
     private CountingFilterListener counts;
 
     private ActionToolbar actions;
     private CommandAction add = new CommandAction("Add", "Add Filter", XFilesIcons.ADD_ICON);
     private CommandAction remove = new CommandAction("Remove", "Remove Filter", XFilesIcons.REMOVE_ICON);
     private CommandAction copy = new CommandAction("Copy", "Copy Filter", XFilesIcons.COPY_ICON);
     private CommandAction moveUp = new CommandAction("Move Up", "Move Filter Up", XFilesIcons.UP_ICON);
     private CommandAction moveDown = new CommandAction("Move Down", "Move Filter Down", XFilesIcons.DOWN_ICON);
 
     private Command addCommand = new Command() {
         public void execute() {
             addFilter();
         }
     };
 
     private Command removeCommand = new Command() {
         public void execute() {
             removeFilter();
         }
     };
 
     private Command copyCommand = new Command() {
         public void execute() {
             copyFilter();
         }
     };
 
     private Command moveUpCommand = new Command() {
         public void execute() {
             moveFilterUp();
         }
     };
 
     private Command moveDownCommand = new Command() {
         public void execute() {
             moveFilterDown();
         }
     };
 
     private ListSelectionListener selectionListener = new ListSelectionListener() {
 
         public void valueChanged(ListSelectionEvent e) {
             if (!e.getValueIsAdjusting()) {
 
                 int selected = filterList.getSelectedIndex();
                 int first = 0;
                 int last = filterListModel.getSize() - 1;
 
                 remove.setEnabled(selected >= first);
                 copy.setEnabled(selected >= first);
                 moveUp.setEnabled(selected > first);
                 moveDown.setEnabled(selected < last);
 
                 if (selected >= first) {
                     ConfigurableFilterModel model = (ConfigurableFilterModel) filterListModel.getElementAt(selected);
 
                     filterNameField.setText(model.name);
 
                     statusTable.setModel(model.statusModel);
                     typeTable.setModel(model.typeModel);
                     vcsTable.setModel(model.vcsModel);
                     moduleTable.setModel(model.moduleModel);
                     //globTable.setModel(model.globModel);
                     otherTable.setModel(model.otherModel);
                 }
             }
         }
 
     };
 
     private FocusListener nameListener = new FocusListener() {
         public void focusGained(FocusEvent e) {}
 
         public void focusLost(FocusEvent e) {
             int selected = filterList.getSelectedIndex();
 
             if (selected >= 0) {
                 ConfigurableFilterModel model = (ConfigurableFilterModel) filterListModel.getElementAt(selected);
                 model.name = filterNameField.getText();
             }
         }
 
     };
 
 
     public XFilesConfigurable(Project project) {
         this.project = project;
 
         ActionManager actionManager = ActionManager.getInstance();
 
         add.setEnabled(true);
         add.setCommand(addCommand);
         remove.setCommand(removeCommand);
         copy.setCommand(copyCommand);
         moveUp.setCommand(moveUpCommand);
         moveDown.setCommand(moveDownCommand);
 
         DefaultActionGroup group = new DefaultActionGroup("xfiles configuration group", false);
         group.add(add);
         group.add(remove);
         group.add(copy);
         group.add(moveUp);
         group.add(moveDown);
 
         actions = actionManager.createActionToolbar("XFilesConfigurationToolbar", group, true);
     }
 
     // Configurable methods
 
     public String getDisplayName() {
         return "XFiles";
     }
 
     public Icon getIcon() {
         return XFilesIcons.XFILES_ICON;
     }
 
     public String getHelpTopic() {
         return null;
     }
 
     /**
      * Create the configuration component.
      */
     public JComponent createComponent() {
         panel = new JPanel();
         panel.setLayout(new GridBagLayout());
 
         GridBagConstraints constraints = new GridBagConstraints();
 
         JComponent toolbar = actions.getComponent();
 
         filterListModel = new DefaultListModel();
         filterList = new JList(filterListModel);
 
         testListModel = new DefaultListModel();
         testList = new JList(testListModel);
 
 
         filterNameField = new JTextField();
         statusTable = new ConfigurationTable();
         typeTable = new ConfigurationTable();
         vcsTable = new ConfigurationTable();
         moduleTable = new ConfigurationTable();
         //globTable = new ConfigurationTable();
         otherTable = new ConfigurationTable();
 
         ListSelectionModel selection = filterList.getSelectionModel();
         selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         selection.addListSelectionListener(selectionListener);
 
         filterNameField.addFocusListener(nameListener);
 
         ///////////////////////////
 
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridwidth = 1;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.anchor = GridBagConstraints.NORTHWEST;
         constraints.fill = GridBagConstraints.BOTH;
 
         panel.add(toolbar, constraints);
 
         constraints.gridy = 1;
         constraints.weighty = 10.0;
 
         panel.add(new ListScrollPane(filterList), constraints);
 
         ///////////////////////////
 
         JLabel filterNameLabel = new JLabel("Filter Name:");
 
         constraints.gridx = 1;
         constraints.gridy = 0;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
 
         panel.add(filterNameLabel, constraints);
 
         constraints.gridx = 2;
         constraints.weightx = 10.0;
 
         panel.add(filterNameField, constraints);
 
         JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
         tabs.add("status", new TableScrollPane(statusTable));
         tabs.add("type", new TableScrollPane(typeTable));
         tabs.add("vcs", new TableScrollPane(vcsTable));
         tabs.add("module", new TableScrollPane(moduleTable));
         //tabs.add("glob", new TableScrollPane(globTable));
         tabs.add("other", new TableScrollPane(otherTable));
 
         constraints.gridy = 1;
         constraints.weightx = 10.0;
         constraints.weighty = 10.0;
 
         panel.add(tabs, constraints);
 
         ///////////////////////////
 
         JButton testButton = new JButton("Test Filter");
 
         constraints.gridx = 3;
         constraints.gridy = 0;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
 
         panel.add(testButton, constraints);
 
         constraints.gridy = 1;
         constraints.weighty = 10.0;
 
         panel.add(new ListScrollPane(testList), constraints);
         return panel;
     }
 
 
     /**
      * Compare the configuration values against those in the configuration editor.
      *
      * This method must return true when the configuration has changed to enable the apply button
      * on the configuration panel.
      *
      * @return true if the configuration has changed
      */
     public boolean isModified() {
         if (configuration.CONFIGURED_FILTERS.size() != filterListModel.size()) {
             log.debug("size modified");
             return true;
         }
 
         for (int i=0; i<filterListModel.size(); i++) {
             XFilesFilterConfiguration externalizable = (XFilesFilterConfiguration) configuration.CONFIGURED_FILTERS.get(i);
             ConfigurableFilterModel configurable = (ConfigurableFilterModel) filterListModel.get(i);
             if (!equals(externalizable, configurable)) {
                 log.debug("configuration " + externalizable.NAME + " modified");
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Apply (store) values to the saved configuration.
      */
     public void apply() {
         configuration.CONFIGURED_FILTERS = new ArrayList();
 
         for (int i=0; i<filterListModel.size(); i++) {
             ConfigurableFilterModel configurable = (ConfigurableFilterModel) filterListModel.get(i);
             XFilesFilterConfiguration externalizable = new XFilesFilterConfiguration();
             configurable.apply(externalizable);
             configuration.CONFIGURED_FILTERS.add(externalizable);
         }
 
         // TODO: sort out this (and other) bidirectional dependency
         configuration.getListener().configurePopupActionGroup();
     }
 
     /**
      * Reset (load) values from those in the saved configuration.
      */
     public void reset() {
         XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
 
         counts = new CountingFilterListener();
         filter.setListener(counts);
 
         XFilesContentIterator content = new XFilesContentIterator(project);
         content.setFilter(filter);
         content.iterate();
 
         log.debug("reset: " + content + counts);
 
         counts.log();
 
         filterListModel.clear();
 
         for (Iterator iterator = configuration.CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
             XFilesFilterConfiguration filterConfig = (XFilesFilterConfiguration) iterator.next();
 
             ConfigurableFilterModel filterModel = new ConfigurableFilterModel(counts);
             filterModel.reset(filterConfig);
 
             filterListModel.addElement(filterModel);
         }
 
         // select the first filter...
         // TODO: make sure that there is at least one filter
         // TODO: initially select the currently active filter?
         // could use the selected name from the configuration
 
         if (!filterListModel.isEmpty())
             filterList.setSelectedIndex(0);
     }
 
     public void disposeUIResources() {
         panel = null;
     }
 
     ////////////////////////////////
 
 
     // ProjectComponent methods
 
     public String getComponentName() {
         return "XFilesConfigurable";
     }
 
     public void initComponent() {
     }
 
     public void disposeComponent() {
     }
 
     public void projectOpened() {
        configuration = project.getComponent(XFilesConfiguration.class);
     }
 
     public void projectClosed() {
         configuration = null;
     }
 
     ////////////////////////////////
 
     public void addFilter() {
         ConfigurableFilterModel filter = new ConfigurableFilterModel(counts);
         filter.name = "<unnamed>";
         int index = filterList.getSelectedIndex()+1;
         filterListModel.add(index, filter);
         filterList.setSelectedIndex(index);
     }
 
     public void removeFilter() {
         int index = filterList.getSelectedIndex();
         if (index >= 0) {
             filterListModel.removeElementAt(index);
         }
 
         if (index >= filterListModel.size()) {
             index = filterListModel.size()-1;
         }
 
         if (index >= 0) {
             filterList.setSelectedIndex(index);
         } else {
             // TODO: may need to clear configuration tables if there are no remaining filters
         }
     }
 
     public void copyFilter() {
         int index = filterList.getSelectedIndex();
         if (index >= 0) {
             ConfigurableFilterModel filter = (ConfigurableFilterModel) filterListModel.getElementAt(index);
             filter = new ConfigurableFilterModel(filter);
             filter.name = "<unnamed>";
             filterListModel.add(index, filter);
             filterList.setSelectedIndex(index);
         }
     }
 
     private void swap(int index1, int index2) {
         Object element1 = filterListModel.getElementAt(index1);
         Object element2 = filterListModel.getElementAt(index2);
 
         filterListModel.setElementAt(element1, index2);
         filterListModel.setElementAt(element2, index1);
 
         filterList.setSelectedIndex(index2);
     }
 
     public void moveFilterUp() {
         int index = filterList.getSelectedIndex();
         swap(index, index - 1);
     }
 
     public void moveFilterDown() {
         int index = filterList.getSelectedIndex();
         swap(index, index + 1);
     }
 
     private boolean equals(String name, Object a, Object b) {
         if (a.equals(b)) return true;
         log.debug(name + " differs");
         log.debug(a.toString());
         log.debug(b.toString());
         return false;
     }
 
     private boolean equals(XFilesFilterConfiguration externalizable, ConfigurableFilterModel configurable) {
         if (!equals("name", externalizable.NAME, configurable.name)) return false;
 
         if (!equals("status", externalizable.ACCEPTED_STATUS_NAMES,  configurable.statusModel.getSelectedItems())) return false;
         if (!equals("type", externalizable.ACCEPTED_TYPE_NAMES, configurable.typeModel.getSelectedItems())) return false;
         if (!equals("vcs", externalizable.ACCEPTED_VCS_NAMES, configurable.vcsModel.getSelectedItems())) return false;
         if (!equals("module", externalizable.ACCEPTED_MODULE_NAMES, configurable.moduleModel.getSelectedItems())) return false;
         // (!equals("glob", externalizable.ACCEPTED_GLOB_NAMES, configurable.globModel.getSelectedItems())) return false;
         if (!equals("other", externalizable.ACCEPTED_OTHERS, configurable.otherModel.getSelectedItems())) return false;
 
         // TODO: it would be helpful to have a list of the various lists
 
         return true;
     }
 
     // RANDOM THOUGHT
     // - ExternalizableFilterConfiguration
     // - ConfigurableFilterConfiguration
     // both extending FilterConfiguration which implements .equals()
 
     private class ConfigurationItem implements Comparable {
         private Boolean included;
         private String text;
         private Integer count;
 
         public ConfigurationItem(boolean included, String text, int count) {
             this.included = Boolean.valueOf(included);
             this.text = text;
            this.count = Integer.valueOf(count);
         }
 
         public int compareTo(Object o) {
             ConfigurationItem that = (ConfigurationItem) o;
             return this.text.compareTo(that.text);
         }
     }
 
     private class ConfigurationTableModel extends AbstractTableModel {
 
         private String name;
         private List items = new ArrayList();
 
         public ConfigurationTableModel() {
         }
 
         public ConfigurationTableModel(ConfigurationTableModel that) {
             this.name = that.name;
             this.items.addAll(that.items);
         }
 
         /**
          * specify the name of items in this model
          * i.e. vcs, status, type, etc.
          *
          * @param map
          */
         public ConfigurationTableModel(VirtualFileCounterMap map) {
             this.name = map.getName();
 
             for (Iterator iterator = map.getCounters().iterator(); iterator.hasNext();) {
                 VirtualFileCounter counter = (VirtualFileCounter) iterator.next();
                 items.add(new ConfigurationItem(false, counter.getName(), counter.getCount()));
             }
 
             // sorted so that comparisons work properly
             Collections.sort(items);
         }
 
         public String getName() {
             return name;
         }
 
         public int getRowCount() {
             return items.size();
         }
 
         public int getColumnCount() {
             return 3;
         }
 
         public String getColumnName(int column) {
             switch (column) {
                 case 0: return "include";
                 case 1: return name;
                 case 2: return "count";
                 default: return null; // exception!
             }
         }
 
         public Class getColumnClass(int column) {
             switch (column) {
                 case 0: return Boolean.class;
                 case 1: return String.class;
                 case 2: return Integer.class;
                 default: return null; // exception!
             }
         }
 
         public boolean isCellEditable(int row, int column) {
             return column == 0;
         }
 
         public Object getValueAt(int row, int column) {
             ConfigurationItem item = (ConfigurationItem) items.get(row);
             switch (column) {
                 case 0: return item.included;
                 case 1: return item.text;
                 case 2: return item.count;
                 default: return null; // exception!
             }
         }
 
         public void setValueAt(Object value, int row, int column) {
             Boolean included = (Boolean) value;
             ConfigurationItem item = (ConfigurationItem) items.get(row);
             switch (column) {
                 case 0: item.included = included;
                 default: ; // exception!
             }
         }
 
         /**
          * Get a list of the selected item names
          */
         public List getSelectedItems() {
             List selected = new ArrayList();
             for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                 ConfigurationItem item = (ConfigurationItem) iterator.next();
                 if (item.included.booleanValue())
                     selected.add(item.text);
             }
             return selected;
         }
 
         /**
          * Set the list of selected item names. This sets the selection on existing items included
          * in the selected list and then adds any items in the selected list.
          *
          * Alternatively we could hold items in a map keyed by name with selected/count values.
          * This would allow for iterating over the selected list and simply checking the map
          * for existing values. 
          *
          * @param selected
          */
         public void setSelectedItems(List selected) {
             List temp = new ArrayList();
             temp.addAll(selected);
 
             for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                 ConfigurationItem item = (ConfigurationItem) iterator.next();
 
                 if (temp.remove(item.text))
                     item.included = Boolean.TRUE;
                 else
                     item.included = Boolean.FALSE;
             }
 
             for (Iterator iterator = temp.iterator(); iterator.hasNext();) {
                 String text = (String) iterator.next();
                 ConfigurationItem item = new ConfigurationItem(true, text, 0);
                 items.add(item);
             }
 
             Collections.sort(items);
         }
 
     }
 
     private class ConfigurationTable extends JTable {
         public ConfigurationTable() {
             super(new ConfigurationTableModel());
             setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
         }
     }
 
     private class TableScrollPane extends JScrollPane {
         public TableScrollPane(JTable table) {
             super(table, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
         }
     }
 
     private class ListScrollPane extends JScrollPane {
         public ListScrollPane(JList list) {
             super(list, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
         }
     }
 
     /**
      * This class holds entries in the list of configurable filters that's being edited
      */
     private class ConfigurableFilterModel {
 
         private String name;
 
         // TODO: we may want to hold these in an array
         private ConfigurationTableModel statusModel;
         private ConfigurationTableModel typeModel;
         private ConfigurationTableModel vcsModel;
         private ConfigurationTableModel moduleModel;
         //private ConfigurationTableModel globModel;
         private ConfigurationTableModel otherModel;
 
         /**
          * Copy constructor
          *
          * @param that
          */
         public ConfigurableFilterModel(ConfigurableFilterModel that) {
             this.name = that.name;
             this.statusModel = new ConfigurationTableModel(that.statusModel);
             this.typeModel = new ConfigurationTableModel(that.typeModel);
             this.vcsModel = new ConfigurationTableModel(that.vcsModel);
             this.moduleModel = new ConfigurationTableModel(that.moduleModel);
             //this.globModel = new ConfigurationTableModel(that.globModel);
             this.otherModel = new ConfigurationTableModel(that.otherModel);
         }
 
         public ConfigurableFilterModel(CountingFilterListener listener) {
             statusModel = new ConfigurationTableModel(listener.getStatusMap());
             typeModel = new ConfigurationTableModel(listener.getTypeMap());
             vcsModel = new ConfigurationTableModel(listener.getVcsMap());
             moduleModel = new ConfigurationTableModel(listener.getModuleMap());
             //globModel = new ConfigurationTableModel(listener.getGlobMap());
             otherModel = new ConfigurationTableModel(listener.getOtherMap());
         }
 
         public void reset(XFilesFilterConfiguration configuration) {
             name = configuration.NAME;
             statusModel.setSelectedItems(configuration.ACCEPTED_STATUS_NAMES);
             typeModel.setSelectedItems(configuration.ACCEPTED_TYPE_NAMES);
             vcsModel.setSelectedItems(configuration.ACCEPTED_VCS_NAMES);
             moduleModel.setSelectedItems(configuration.ACCEPTED_MODULE_NAMES);
             //globModel.setSelectedItems(configuration.ACCEPTED_NAME_GLOBS);
             otherModel.setSelectedItems(configuration.ACCEPTED_OTHERS);
         }
 
         public void apply(XFilesFilterConfiguration configuration) {
             log.debug("saving filter " + name);
             configuration.log();
             configuration.NAME = name;
 
             configuration.ACCEPTED_STATUS_NAMES.clear();
             configuration.ACCEPTED_TYPE_NAMES.clear();
             configuration.ACCEPTED_VCS_NAMES.clear();
             configuration.ACCEPTED_MODULE_NAMES.clear();
             configuration.ACCEPTED_NAME_GLOBS.clear();
             configuration.ACCEPTED_OTHERS.clear();
 
             configuration.ACCEPTED_STATUS_NAMES.addAll(statusModel.getSelectedItems());
             configuration.ACCEPTED_TYPE_NAMES.addAll(typeModel.getSelectedItems());
             configuration.ACCEPTED_VCS_NAMES.addAll(vcsModel.getSelectedItems());
             configuration.ACCEPTED_MODULE_NAMES.addAll(moduleModel.getSelectedItems());
             //configuration.ACCEPTED_NAME_GLOBS.addAll(globModel.getSelectedItems());
             configuration.ACCEPTED_OTHERS.addAll(otherModel.getSelectedItems());
             configuration.log();
         }
 
         public String toString() {
             return name;
         }
     }
 
 }
