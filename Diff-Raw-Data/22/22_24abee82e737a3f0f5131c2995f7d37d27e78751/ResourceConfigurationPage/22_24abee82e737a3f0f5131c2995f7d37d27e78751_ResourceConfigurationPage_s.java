 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
 import org.apache.wicket.extensions.markup.html.tabs.ITab;
 import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.web.GeoServerApplication;
 import org.geoserver.web.GeoServerSecuredPage;
 import org.geoserver.web.data.layer.LayerPage;
 import org.geoserver.web.publish.LayerConfigurationPanel;
 import org.geoserver.web.publish.LayerConfigurationPanelInfo;
 
 /**
  * Page allowing to configure a layer and its resource.
  * <p>
  * The page is completely pluggable, the UI will be made up by scanning the
  * Spring context for implementations of {@link ResourceConfigurationPanel} and
  * {@link LayerConfigurationPanel}.
  * <p>
  * WARNING: one crucial aspect of this page is its ability to not loose edits
  * when one switches from one tab to the other. I did not find any effective way
  * to unit test this, so _please_, if you do modify anything in this class
  * (especially the models), manually retest that the edits are not lost on tab
  * switch.
  */
 @SuppressWarnings("serial")
 public class ResourceConfigurationPage extends GeoServerSecuredPage {
 
     private IModel myResourceModel;
 
     private IModel myLayerModel;
 
     private boolean isNew;
 
     public ResourceConfigurationPage(String layerName) {
         LayerInfo layer = getCatalog().getLayerByName(layerName);
         setup(layer.getResource(), layer);
         this.isNew = false;
         initComponents();
     }
 
     public ResourceConfigurationPage(ResourceInfo info, boolean isNew) {
         setup(info, getCatalog().getLayers(info).get(0));
         this.isNew = isNew;
         initComponents();
     }
 
     public ResourceConfigurationPage(LayerInfo info, boolean isNew) {
         setup(info.getResource(), info);
         this.isNew = isNew;
         initComponents();
     }
 
     private void setup(ResourceInfo resource, LayerInfo layer) {
         myResourceModel = new CompoundPropertyModel(new ResourceModel(resource));
         myLayerModel = new CompoundPropertyModel(new LayerModel(layer));
     }
 
     private void initComponents() {
         add(new Label("resourcename", getResourceInfo().getPrefixedName()));
         Form theForm = new Form("resource", myResourceModel);
         add(theForm);
         List<ITab> tabs = new ArrayList<ITab>();
         tabs.add(new AbstractTab(new Model("Data")) {
             public Panel getPanel(String panelID) {
                 return new ListPanel(panelID,
                         new ResourceConfigurationSectionListView("theList"));
             }
         });
         tabs.add(new AbstractTab(new Model("Publishing")) {
             public Panel getPanel(String panelID) {
                 return new ListPanel(panelID,
                         new LayerConfigurationSectionListView("theList"));
             }
         });
         // we need to override with submit links so that the various form
         // element
         // will validate and write down into their
         theForm.add(new TabbedPanel("tabs", tabs) {
             @Override
             protected WebMarkupContainer newLink(String linkId, final int index) {
                 return new SubmitLink(linkId) {
                     @Override
                     public void onSubmit() {
                         setSelectedTab(index);
                     }
                 };
             }
         });
         theForm.add(new Button("saveButton") {
             public void onSubmit() {
                if (isNew) {
                    getCatalog().add(getResourceInfo());
                    getCatalog().add(getLayerInfo());
                } else {
                    getCatalog().save(getResourceInfo());
                    getCatalog().save(getLayerInfo());
                 }
                setResponsePage(LayerPage.class);
             }
         });
     }
 
     private List<ResourceConfigurationPanelInfo> filterResourcePanels(
             List<ResourceConfigurationPanelInfo> list) {
         for (int i = 0; i < list.size(); i++) {
             if (!list.get(i).canHandle(getResourceInfo())) {
                 list.remove(i);
                 i--;
             }
         }
         return list;
     }
 
     private List<LayerConfigurationPanelInfo> filterLayerPanels(
             List<LayerConfigurationPanelInfo> list) {
         for (int i = 0; i < list.size(); i++) {
             if (!list.get(i).canHandle(getLayerInfo())) {
                 list.remove(i);
                 i--;
             }
         }
         return list;
     }
 
     private class ResourceConfigurationSectionListView extends ListView {
         private static final long serialVersionUID = -6575960326680386479L;
 
         public ResourceConfigurationSectionListView(String id) {
             super(
                     id,
                     filterResourcePanels(((GeoServerApplication) getGeoServerApplication())
                             .getBeansOfType(ResourceConfigurationPanelInfo.class)));
             // do this or die on validation (the form element contents will
             // reset, the edit will be lost)
             setReuseItems(true);
         }
 
         @Override
         protected void populateItem(ListItem item) {
             ResourceConfigurationPanelInfo panelInfo = (ResourceConfigurationPanelInfo) item
                     .getModelObject();
             try {
                 final Class<ResourceConfigurationPanel> componentClass = panelInfo
                         .getComponentClass();
                 final Constructor<ResourceConfigurationPanel> constructor;
                 constructor = componentClass.getConstructor(String.class,
                         IModel.class);
                 ResourceConfigurationPanel panel = constructor.newInstance(
                         "content", myResourceModel);
                 item.add((Component) panel);
             } catch (Exception e) {
                 throw new WicketRuntimeException(
                         "Failed to add pluggable resource configuration panels",
                         e);
             }
         }
     }
 
     private class LayerConfigurationSectionListView extends ListView {
         private static final long serialVersionUID = -6575960326680386479L;
 
         public LayerConfigurationSectionListView(String id) {
             super(
                     id,
                     filterLayerPanels(((GeoServerApplication) getGeoServerApplication())
                             .getBeansOfType(LayerConfigurationPanelInfo.class)));
             // do this or die on validation (the form element contents will
             // reset, the edit will be lost)
             setReuseItems(true);
         }
 
         @Override
         protected void populateItem(ListItem item) {
             LayerConfigurationPanelInfo panelInfo = (LayerConfigurationPanelInfo) item
                     .getModelObject();
             try {
                 LayerConfigurationPanel panel = panelInfo.getComponentClass()
                         .getConstructor(String.class, IModel.class)
                         .newInstance("content", myLayerModel);
                 item.add((Component) panel);
             } catch (Exception e) {
                 throw new WicketRuntimeException(
                         "Failed to add pluggable layer configuration panels", e);
             }
         }
     }
 
     protected ResourceInfo getResourceInfo() {
         return (ResourceInfo) myResourceModel.getObject();
     }
 
     protected LayerInfo getLayerInfo() {
         return (LayerInfo) myLayerModel.getObject();
     }
 }
