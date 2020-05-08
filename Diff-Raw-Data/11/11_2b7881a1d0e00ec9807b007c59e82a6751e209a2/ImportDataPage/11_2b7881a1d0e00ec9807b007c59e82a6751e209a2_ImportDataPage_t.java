 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.opengeo.data.importer.web;
 
 import static org.opengeo.data.importer.web.ImporterWebUtils.importer;
 
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.Session;
 import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.time.Duration;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.NamespaceInfo;
 import org.geoserver.catalog.StoreInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.web.GeoServerApplication;
 import org.geoserver.web.GeoServerBasePage;
 import org.geoserver.web.data.store.StoreChoiceRenderer;
 import org.geoserver.web.data.store.StoreModel;
 import org.geoserver.web.data.store.StoresModel;
 import org.geoserver.web.data.workspace.WorkspaceChoiceRenderer;
 import org.geoserver.web.data.workspace.WorkspaceDetachableModel;
 import org.geoserver.web.data.workspace.WorkspacesModel;
 import org.geoserver.web.wicket.GeoServerDialog;
 import org.geoserver.web.wicket.GeoServerDialog.DialogDelegate;
 import org.geoserver.web.wicket.ParamResourceModel;
 import org.geotools.util.logging.Logging;
 import org.opengeo.data.importer.ImportContext;
 import org.opengeo.data.importer.ImportData;
 import org.opengeo.data.importer.Importer;
 
 /**
  * First page of the import wizard.
  * 
  * @author Andrea Aime - OpenGeo
  * @author Justin Deoliveira, OpenGeo
  */
 @SuppressWarnings("serial")
 public class ImportDataPage extends GeoServerBasePage {
 
     static Logger LOGGER = Logging.getLogger(ImportDataPage.class);
 
     ListView<Source> sourceList;
     WebMarkupContainer sourcePanel;
     
     WorkspaceDetachableModel workspace;
     DropDownChoice workspaceChoice;
     
     StoreModel store;
     DropDownChoice storeChoice;
     
     String storeName;
     TextField storeNameTextField;
     
     GeoServerDialog dialog;
     
     public ImportDataPage(PageParameters params) {
         Form form = new Form("form");
         add(form);
         
         sourceList = new ListView<Source>("sources", Arrays.asList(Source.values())) {
             @Override
             protected void populateItem(final ListItem<Source> item) {
                 final Source source = (Source) item.getModelObject();
                 AjaxLink link = new AjaxLink("link") {
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         updateSourcePanel(source);
                         updateModalLinks(this, target);
                         target.addComponent(sourcePanel);
                     }
                 };
                 link.setOutputMarkupId(true);
                 
                 link.add(new Label("name", source.getName(ImportDataPage.this)));
                 if(item == sourceList.get(0)) {
                     link.add(new AttributeAppender("class", true, new Model("selected"), " "));
                 }
                 item.add(link);
                 
                 item.add(new Label("description", source .getDescription(ImportDataPage.this)));
                 
                 Image icon = new Image("icon", source.getIcon());
                 icon.add(
                     new AttributeModifier("alt", true, source.getDescription(ImportDataPage.this)));
                 item.add(icon);
             }
             
         };
         form.add(sourceList);
         
         sourcePanel = new WebMarkupContainer("panel");
         sourcePanel.setOutputMarkupId(true);
         form.add(sourcePanel);
         
         Catalog catalog = GeoServerApplication.get().getCatalog();
         
         // workspace chooser
         workspace = new WorkspaceDetachableModel(catalog.getDefaultWorkspace());
         workspaceChoice = new DropDownChoice("workspace", workspace, new WorkspacesModel(), 
             new WorkspaceChoiceRenderer());
         workspaceChoice.setOutputMarkupId(true);
         workspaceChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 store.setObject(GeoServerApplication.get().getCatalog()
                     .getDefaultDataStore((WorkspaceInfo) workspace.getObject()));
                 target.addComponent(storeChoice);
             }
         });
         form.add(workspaceChoice);
         
         //store chooser
         //store = new StoreModel(catalog.getDefaultDataStore((WorkspaceInfo) workspace.getObject()));
         store = new StoreModel(null);
        storeChoice = new DropDownChoice("store", store, new StoresModel(
            new PropertyModel<WorkspaceInfo>(this, "workspace.object")), new StoreChoiceRenderer());
         storeChoice.setOutputMarkupId(true);
 //        storeChoice.setEnabled(false);
 //        storeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
 //            @Override
 //            protected void onUpdate(AjaxRequestTarget target) {
 //                storeNameTextField.setEnabled(store.getObject() == null);
 //                target.addComponent(storeNameTextField);
 //            }
 //        });
         storeChoice.setNullValid(true);
         form.add(storeChoice);
         
         //form.add(storeNameTextField = new TextField("storeName", new PropertyModel(this, "storeName")));
         //storeNameTextField.setOutputMarkupId(true);
         
         // new workspace
         form.add(new AjaxLink("newWorkspace") {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 dialog.setTitle(new ParamResourceModel("newWorkspace", ImportDataPage.this));
                 dialog.setInitialWidth(400);
                 dialog.setInitialHeight(150);
                 dialog.setMinimalHeight(150);
                 
                 dialog.showOkCancel(target, new DialogDelegate() {
                     String wsName;
 
                     @Override
                     protected boolean onSubmit(AjaxRequestTarget target, Component contents) {
                         try {
                             Catalog catalog = GeoServerApplication.get().getCatalog();
                             
                             NewWorkspacePanel panel = (NewWorkspacePanel) contents;
                             wsName = panel.workspace;
                             
                             WorkspaceInfo ws = catalog.getFactory().createWorkspace();
                             ws.setName(wsName);
                             
                             NamespaceInfo ns = catalog.getFactory().createNamespace();
                             ns.setPrefix(wsName);
                             ns.setURI("http://opengeo.org/#" + URLEncoder.encode(wsName, "ASCII"));
                             
                             catalog.add( ws );
                             catalog.add( ns );

                             return true;
                         } catch(Exception e) {
                             e.printStackTrace();
                             return false;
                         }
                     }
                     
                     @Override
                     public void onClose(AjaxRequestTarget target) {
                         Catalog catalog = GeoServerApplication.get().getCatalog();
                         workspace = new WorkspaceDetachableModel(catalog.getWorkspaceByName(wsName));
                         workspaceChoice.setModel(workspace);
                         target.addComponent(workspaceChoice);
                        target.addComponent(storeChoice);
                     }
                     
                     @Override
                     protected Component getContents(String id) {
                         return new NewWorkspacePanel(id);
                     }
                 });
                 
             }
         });
 
         form.add(new AjaxSubmitLink("next", form) {
 
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedbackPanel);
             }
             
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 
                 //first update the button to indicate we are working
                 add(new AttributeAppender("class", true, new Model("button-working icon"), " "));
                 setEnabled(false);
                 get(0).setDefaultModelObject("Working");
 
                 target.addComponent(this);
 
                 //start a timer to actually do the work, which will allow the link to update 
                 // while the context is created
                 final AjaxSubmitLink self = this;
                 this.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(100)) {
                     @Override
                     protected void onTimer(AjaxRequestTarget target) {
                         ImportSourcePanel panel = (ImportSourcePanel) sourcePanel.get("content");
                         ImportData source = panel.createImportSource();
                         WorkspaceInfo targetWorkspace = (WorkspaceInfo) 
                             (workspace.getObject() != null ? workspace.getObject() : null);
                         StoreInfo targetStore = (StoreInfo) (store.getObject() != null ? store
                                 .getObject() : null);
 
                         Importer importer = ImporterWebUtils.importer();
                         try {
                             ImportContext imp = importer.createContext(source, targetWorkspace,
                                     targetStore);
                             PageParameters pp = new PageParameters();
                             pp.put("id", imp.getId());
 
                             setResponsePage(ImportPage.class, pp);
                         } catch (Exception e) {
                             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                             error(e);
 
                             target.addComponent(feedbackPanel);
 
                             //update the button back to original state
                             self.add(new AttributeModifier("class", true, new Model("")));
                             self.setEnabled(true);
                             self.get(0).setDefaultModelObject("Next");
                             target.addComponent(self);
                         }
                         finally {
                             stop();
                         }
                     }
                 });
 //                ImportSourcePanel panel = (ImportSourcePanel) sourcePanel.get("content");
 //                ImportData source = panel.createImportSource();
 //                WorkspaceInfo targetWorkspace = 
 //                    (WorkspaceInfo) (workspace.getObject() != null ? workspace.getObject() : null);    
 //                StoreInfo targetStore = 
 //                    (StoreInfo) (store.getObject() != null ? store.getObject() : null);
 //                
 //                Importer importer = ImporterWebUtils.importer();
 //                try {
 //                    ImportContext imp = importer.createContext(source, targetWorkspace, targetStore);
 //                    PageParameters pp = new PageParameters();
 //                    pp.put("id", imp.getId());
 //                    
 //                    setResponsePage(ImportPage.class, pp);
 //                }
 //                catch(Exception e) {
 //                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
 //                    error(e);
 //                }
             }
         }.add(new Label("message", new Model("Next"))));
 
         ImportContextTable importTable = 
             new ImportContextTable("imports", new ImportContextProvider() {
                 @Override
                 protected List<org.geoserver.web.wicket.GeoServerDataProvider.Property<ImportContext>> getProperties() {
                     return Arrays.asList(ID, CREATED, STATE);
                 }
         });
         importTable.setFilterable(false);
         importTable.setSortable(false);
         form.add(importTable);
 
         add(dialog = new GeoServerDialog("dialog"));
         
         updateSourcePanel(Source.SPATIAL_FILES);
     }
     
     void updateSourcePanel(Source source) {
         Panel old = (Panel) sourcePanel.get(0);
         if (old != null) {
             sourcePanel.remove(old);
         }
 
         Panel p = source.createPanel("content");
         sourcePanel.add(p);
     }
     
     void updateModalLinks(AjaxLink selected, AjaxRequestTarget target) {
         int n = sourceList.getModelObject().size();
         for (int i = 0; i < n; i++) {
             AjaxLink link = (AjaxLink) ((ListItem)sourceList.get(i)).get("link");
             if (link == selected) {
                 link.add(new AttributeAppender("class", new Model("selected"), " "));
             }
             else {
                 link.add(new SimpleAttributeModifier("class", ""));
             }
             target.addComponent(link);
         }
     }
 
     /**
      * A type data source.
      */
     enum Source {
         SPATIAL_FILES(DataIcon.FOLDER) {
             @Override
             ImportSourcePanel createPanel(String panelId) {
                 return new SpatialFilePanel(panelId);
             }
         },
         POSTGIS(DataIcon.POSTGIS) {
             @Override
             ImportSourcePanel createPanel(String panelId) {
                 return new PostGISPanel(panelId);
             }  
         };
         
 //        directory(new ResourceReference(GeoServerApplication.class, "img/icons/silk/folder.png"),
 //                DirectoryPage.class, "org.geotools.data.shapefile.ShapefileDataStoreFactory"), // 
 //        postgis(new ResourceReference(GeoServerApplication.class,
 //                "img/icons/geosilk/database_vector.png"), PostGISPage.class,
 //                "org.geotools.data.postgis.PostgisNGDataStoreFactory"), //
 //        oracle(new ResourceReference(GeoServerApplication.class,
 //                "img/icons/geosilk/database_vector.png"), OraclePage.class,
 //                "org.geotools.data.oracle.OracleNGDataStoreFactory"), //
 //        sqlserver(new ResourceReference(GeoServerApplication.class,
 //                "img/icons/geosilk/database_vector.png"), SQLServerPage.class,
 //                "org.geotools.data.sqlserver.SQLServerDataStoreFactory"), //
 //        arcsde(new ResourceReference(GeoServerApplication.class,
 //                "img/icons/geosilk/database_vector.png"), ArcSDEPage.class,
 //                "org.geotools.arcsde.ArcSDEDataStoreFactory");
 
         DataIcon icon;
 
         //Class<? extends Page> destinationPage;
 
         //String factoryClassName;
 
         Source(DataIcon icon) {
             this.icon = icon;
             //this.destinationPage = destinationPage;
             //this.factoryClassName = factoryClassName;
         }
 
         IModel getName(Component component) {
             return new ParamResourceModel(this.name().toLowerCase() + "_name", component);
         }
 
         IModel getDescription(Component component) {
             return new ParamResourceModel(this.name().toLowerCase() + "_description", component);
         }
 
         ResourceReference getIcon() {
             return icon.getIcon();
         }
         
         abstract ImportSourcePanel createPanel(String panelId);
 
 //        Class<? extends Page> getDestinationPage() {
 //            return destinationPage;
 //        }
 //
 //        /**
 //         * Checks whether the datastore is installed and available (e.g., all the extra libraries it
 //         * requires are there)
 //         * 
 //         * @return
 //         */
 //        boolean isAvailable() {
 //            try {
 //                Class<?> clazz = Class.forName(factoryClassName);
 //                DataStoreFactorySpi factory = (DataStoreFactorySpi) clazz.newInstance();
 //                return factory.isAvailable();
 //            } catch (Exception e) {
 //                return false;
 //            }
 //        }
 //
 //        /**
 //         * Returns the list of stores that are known to be available
 //         * 
 //         * @return
 //         * @see #isAvailable()
 //         */
 //        static List<Store> getAvailableStores() {
 //            List<Store> stores = new ArrayList<Store>(Arrays.asList(values()));
 //            for (Iterator<Store> it = stores.iterator(); it.hasNext();) {
 //                Store store = it.next();
 //                if (!store.isAvailable())
 //                    it.remove();
 //            }
 //
 //            return stores;
 //        }
     }
     
     
 }
