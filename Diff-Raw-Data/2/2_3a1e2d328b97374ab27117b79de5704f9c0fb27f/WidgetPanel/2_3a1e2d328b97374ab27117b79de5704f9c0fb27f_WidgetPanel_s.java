 package org.iplantc.core.tito.client.panels;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.iplantc.core.metadata.client.JSONMetaDataObject;
 import org.iplantc.core.metadata.client.property.Property;
 import org.iplantc.core.metadata.client.property.PropertyData;
 import org.iplantc.core.metadata.client.property.groups.PropertyGroup;
 import org.iplantc.core.metadata.client.property.groups.PropertyGroupContainer;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.events.CommandLineArgumentChangeEvent;
 import org.iplantc.core.tito.client.events.CommandLineArgumentChangeEventHandler;
 import org.iplantc.core.tito.client.events.ExecutableChangeEvent;
 import org.iplantc.core.tito.client.events.ExecutableChangeEventHandler;
 import org.iplantc.core.tito.client.events.NavigationTreeAddEvent;
 import org.iplantc.core.tito.client.events.NavigationTreeAddEventHandler;
 import org.iplantc.core.tito.client.events.NavigationTreeBeforeSelectionEvent;
 import org.iplantc.core.tito.client.events.NavigationTreeBeforeSelectionEventHandler;
 import org.iplantc.core.tito.client.events.NavigationTreeDeleteEvent;
 import org.iplantc.core.tito.client.events.NavigationTreeDeleteEventHandler;
 import org.iplantc.core.tito.client.events.NavigationTreeSelectionChangeEvent;
 import org.iplantc.core.tito.client.events.NavigationTreeSelectionChangeEventHandler;
 import org.iplantc.core.tito.client.events.TemplateNameChangeEvent;
 import org.iplantc.core.tito.client.events.TemplateNameChangeEventHandler;
 import org.iplantc.core.tito.client.models.MetaDataTreeModel;
 import org.iplantc.core.tito.client.models.Template;
 import org.iplantc.core.tito.client.utils.PropertyGroupContainerUtil;
 import org.iplantc.core.uicommons.client.events.EventBus;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Element;
 
 /**
  * 
  * A panel used to build and display property group container
  * 
  * @author sriram
  *
  */
 public class WidgetPanel extends ContentPanel {
     private final PropertyGroupContainer container;
     private LayoutContainer parameterPanel;
     private NavigationTreePanel west;
     private ContentPanel center;
     private TextArea txtCmdLinePreview;
     private String executableName;
 
     public WidgetPanel(PropertyGroupContainer container) {
         this.container = container;
 
         init();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int pos) {
         super.onRender(parent, pos);
         setProperties();
     }
 
     private void init() {
         setHeaderVisible(false);
         initListeners();
         executableName = I18N.DISPLAY.executableNameDefault();
         compose();
         updateCmdLinePreview();
     }
 
     private void compose() {
         add(buildCmdLinePanel());
         parameterPanel = new LayoutContainer(new BorderLayout());
        parameterPanel.setHeight(400);
         add(parameterPanel);
     }
     
     private LayoutContainer buildCmdLinePanel() {
         LayoutContainer panel = new LayoutContainer();
         panel.setHeight(170);
         panel.setStyleAttribute("background-color", "#EDEDED"); //$NON-NLS-1$ //$NON-NLS-2$
         panel.setStyleAttribute("padding-left", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
         panel.setStyleAttribute("padding-top", "3px"); //$NON-NLS-1$ //$NON-NLS-2$
 
         // add command line scratch area
         TextArea cmdLineField = new TextArea();
         cmdLineField.setWidth(750);
         panel.add(new Label(I18N.DISPLAY.enterCmdLineArgs()));
         panel.add(cmdLineField);
         
         // add command line preview area
         txtCmdLinePreview = new TextArea();
         txtCmdLinePreview.setWidth(750);
         txtCmdLinePreview.setReadOnly(true);
         panel.add(new Label(I18N.DISPLAY.cmdLinePreview()));
         panel.add(txtCmdLinePreview);
         return panel;
     }
     
     public void setProperties() {
         buildNavTree();
         replaceCenterPanel(new PropertyGroupContainerEditorPanel(container));
     }
 
     private void initListeners() {
         EventBus eventbus = EventBus.getInstance();
 
         eventbus.addHandler(NavigationTreeBeforeSelectionEvent.TYPE,
                 new NavigationTreeBeforeSelectionEventHandlerImpl());
         eventbus.addHandler(NavigationTreeSelectionChangeEvent.TYPE,
                 new NavigationTreeSelectionChangeEventHandlerImpl());
         eventbus.addHandler(NavigationTreeAddEvent.TYPE,
                 new NavigationTreeMenuSelectionEventHandlerImpl());
         eventbus.addHandler(TemplateNameChangeEvent.TYPE, new TemplateNameChangeEventHandlerImpl());
         eventbus.addHandler(NavigationTreeDeleteEvent.TYPE, new NavigationTreeDeleteEventHandlerImpl());
         eventbus.addHandler(CommandLineArgumentChangeEvent.TYPE, new CommandLineArgumentChangeEventHandlerImpl());
         eventbus.addHandler(ExecutableChangeEvent.TYPE, new ExecutableChangeEventHandlerImpl());
     }
 
     /**
      * Replace the contents of the center panel.
      * 
      * @param view a new component to set in the center of the BorderLayout.
      */
     public void replaceCenterPanel(final ContentPanel view) {
         if (center != null) {
             parameterPanel.remove(center);
         }
         
         center = view;
 
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
         data.setMargins(new Margins(0));
 
         if (center != null) {
             parameterPanel.add(center, data);
             center.setScrollMode(Scroll.AUTOY);
         }
 
         updateCmdLinePreview();
         layout();
     }
 
     /**
      * Replace the contents of the west panel.
      * 
      * @param view a new component to set in the center of the BorderLayout.
      */
     protected void buildNavTree() {
         if (west != null) {
             parameterPanel.remove(west);
         }
 
         west = new NavigationTreePanel(container);
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 200);
         data.setMargins(new Margins(0));
         data.setSplit(true);
 
         parameterPanel.add(west, data);
     }
 
     /**
      * represent a PropertyGroupContainer as JSONObject
      * 
      * @return a PropertyGroupContainer represented as JSONObject
      */
     public JSONObject toJson() {
         JSONObject json = new JSONObject();
 
         json.put(Template.GROUPS, container.toJson());
 
         return json;
     }
 
     /**
      * get list of all properties
      * 
      * @return a list containing all the properties of the PropertyGroupContainer
      */
     public List<Property> getProperties() {
         return container.getProperties();
     }
 
     private class NavigationTreeBeforeSelectionEventHandlerImpl implements
             NavigationTreeBeforeSelectionEventHandler {
         @Override
         public void beforeSelectionChange(NavigationTreeBeforeSelectionEvent event) {
             if (!validate()) {
                 event.setCancelled(true);
             }
         }
     }
 
     private class NavigationTreeSelectionChangeEventHandlerImpl implements
             NavigationTreeSelectionChangeEventHandler {
         @Override
         public void onSelectionChange(NavigationTreeSelectionChangeEvent event) {
             if (container != null) {
                 MetaDataTreeModel model = event.getModel();
                 if (model != null) {
                     JSONMetaDataObject obj = model.getObject();
 
                     if (obj != null) {
                         if (PropertyGroupContainerUtil.isInstanceOfProperty(obj)) {
                             Property property = (Property)obj;
                             if (NavigationTreePanel.TYPE_STATIC_TEXT.equals(property.getType())) {
                                 // static text
                                 replaceCenterPanel(new StaticTextEditorPanel(property));
                             } else {
                                 // property
                                 replaceCenterPanel(new PropertyEditorPanel(container, property));
                             }
                         } else if (PropertyGroupContainerUtil.isInstanceOfPropertyGroupContainer(obj)
                                 && !PropertyGroupContainerUtil.isInstanceOfPropertyGroup(obj)) {
                             // property group container
                             replaceCenterPanel(new PropertyGroupContainerEditorPanel(
                                     (PropertyGroupContainer)obj));
                         } else {
                             // property group
                             replaceCenterPanel(new PropertyGroupEditorPanel((PropertyGroup)obj));
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Updates the label on the PropertyGroupContainer when a TemplateNameChangeEvent is received
      */
     private class TemplateNameChangeEventHandlerImpl implements TemplateNameChangeEventHandler {
         @Override
         public void onSelectionChange(TemplateNameChangeEvent event) {
             container.setLabel(event.getNewValue());
         }
     }
 
     /**
      * Listens for NavigationTreeDeleteEvents (from the tree panel) and deletes the corresponding
      * property or group when a tree element is deleted.
      * 
      * @author hariolf
      * 
      */
     private class NavigationTreeDeleteEventHandlerImpl implements NavigationTreeDeleteEventHandler {
         @Override
         public void onDelete(NavigationTreeDeleteEvent event) {
             doDelete(event);
             updateCmdLinePreview();
         }
     }
 
     private class CommandLineArgumentChangeEventHandlerImpl implements CommandLineArgumentChangeEventHandler {
         @Override
         public void onChange(CommandLineArgumentChangeEvent event) {
             updateCmdLinePreview();
         }
     }
     
     private class ExecutableChangeEventHandlerImpl implements ExecutableChangeEventHandler {
         @Override
         public void onChange(ExecutableChangeEvent event) {
             executableName = event.getExecutable();
             updateCmdLinePreview();
         }
     }
     
     private void doDelete(NavigationTreeDeleteEvent event) {
         JSONMetaDataObject obj = event.getSelectedItem().getObject();
         JSONMetaDataObject parent = event.getParent().getObject();
 
         if (PropertyGroupContainerUtil.isInstanceOfPropertyGroupContainer(parent)) {
             if (PropertyGroupContainerUtil.isInstanceOfPropertyGroup(parent)) {
                 PropertyGroup group = (PropertyGroup)parent;
                 group.remove(obj);
             } else {
                 PropertyGroupContainer container = (PropertyGroupContainer)parent;
                 PropertyGroup child = (PropertyGroup)obj;
                 container.remove(child);
             }
         }
     }
 
     private class NavigationTreeMenuSelectionEventHandlerImpl implements NavigationTreeAddEventHandler {
         @Override
         public void onAdd(NavigationTreeAddEvent event) {
             JSONMetaDataObject addedObject = event.getNewObject();
 
             if (container != null) {
                 JSONMetaDataObject obj = event.getSelectedElement().getObject();
 
                 if (obj != null) {
                     if (PropertyGroupContainerUtil.isInstanceOfPropertyGroupContainer(obj)) {
                         if (PropertyGroupContainerUtil.isInstanceOfPropertyGroup(obj)) {
                             PropertyGroup group = (PropertyGroup)obj;
                             group.add(addedObject);
                         } else {
                             PropertyGroupContainer container = (PropertyGroupContainer)obj;
                             container.add((PropertyGroup)addedObject);
                         }
                     }
                 }
             }
             
             updateCmdLinePreview();
         }
     }
 
     private void updateCmdLinePreview() {
         if (west == null) {
             return;
         }
 
         // build the cmd line from the executable name and all properties
         ArrayList<PropertyData> arguments = new ArrayList<PropertyData>();
         for (Property property : west.getPropertyGroupContainer().getProperties()) {
             if (property.getOrder() > 0) {
                 arguments.add(new PropertyData(property));
             }
         }
 
         Collections.sort(arguments);
 
         StringBuilder cmdLine = new StringBuilder(executableName);
 
         for (PropertyData property : arguments) {
             cmdLine.append(" "); //$NON-NLS-1$
             cmdLine.append(property.getLabel());
         }
 
         txtCmdLinePreview.setValue(cmdLine.toString());
     }
     
     /**
      * 
      * clean up all the event handlers
      */
     public void cleanup() {
         EventBus e = EventBus.getInstance();
         e.removeHandlers(NavigationTreeSelectionChangeEvent.TYPE);
         e.removeHandlers(NavigationTreeAddEvent.TYPE);
         e.removeHandlers(NavigationTreeDeleteEvent.TYPE);
         e.removeHandlers(TemplateNameChangeEvent.TYPE);
     }
 
     /**
      * Validates all input fields and highlights any invalid ones.
      * 
      * @return true for pass, false for fail
      */
     public boolean validate() {
         if (center instanceof StaticTextEditorPanel) {
             return ((StaticTextEditorPanel)center).validate();
         }
         return true;
     }
 }
