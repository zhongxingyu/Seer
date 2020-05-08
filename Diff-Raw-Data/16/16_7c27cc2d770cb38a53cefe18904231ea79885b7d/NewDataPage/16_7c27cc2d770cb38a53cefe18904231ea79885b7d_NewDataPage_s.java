 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.store;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.geoserver.web.CatalogIconFactory;
 import org.geoserver.web.GeoServerSecuredPage;
 import org.geotools.coverage.grid.io.GridFormatFinder;
 import org.geotools.data.DataAccessFactory;
 import org.geotools.data.DataAccessFinder;
 import org.opengis.coverage.grid.Format;
 
 /**
  * Page that presents a list of vector and raster store types available in the classpath in order to
  * choose what kind of data source to create, as well as which workspace to create the store in.
  * <p>
  * Meant to be called by {@link DataPage} when about to add a new datastore or coverage.
  * </p>
  * 
  * @author Gabriel Roldan
  */
 public class NewDataPage extends GeoServerSecuredPage {
 
     // do not access directly, it is transient and the instance can be the de-serialized version
     private transient Map<String, DataAccessFactory> dataStores = getAvailableDataStores();
 
     // do not access directly, it is transient and the instance can be the de-serialized version
     private transient Map<String, Format> coverages = getAvailableCoverageStores();
 
     /**
      * Creates the page components to present the list of available vector and raster data source
      * types
      * 
      * @param workspaceId
      *            the id of the workspace to attach the new resource store to.
      */
     @SuppressWarnings("serial")
     public NewDataPage() {
 
         final Form storeForm = new Form("storeForm");
         add(storeForm);
 
         final ArrayList<String> sortedDsNames = new ArrayList<String>(getAvailableDataStores()
                 .keySet());
         Collections.sort(sortedDsNames);
 
         final CatalogIconFactory icons = CatalogIconFactory.get();
         final ListView dataStoreLinks = new ListView("vectorResources", sortedDsNames) {
             @Override
             protected void populateItem(ListItem item) {
                 final String dataStoreFactoryName = item.getModelObjectAsString();
                 final DataAccessFactory factory = getAvailableDataStores()
                         .get(dataStoreFactoryName);
                 final String description = factory.getDescription();
                 SubmitLink link;
                 link = new SubmitLink("resourcelink") {
                     @Override
                     public void onSubmit() {
                         setResponsePage(new DataAccessNewPage(dataStoreFactoryName));
                     }
                 };
                 link.add(new Label("resourcelabel", dataStoreFactoryName));
                 item.add(link);
                 item.add(new Label("resourceDescription", description));
                item.add(new Image("storeIcon", icons.getStoreIcon(factory.getClass())));
             }
         };
 
         final List<String> sortedCoverageNames = new ArrayList<String>();
         sortedCoverageNames.addAll(getAvailableCoverageStores().keySet());
         Collections.sort(sortedCoverageNames);
 
         final ListView coverageLinks = new ListView("rasterResources", sortedCoverageNames) {
             @Override
             protected void populateItem(ListItem item) {
                 final String coverageFactoryName = item.getModelObjectAsString();
                 final Map<String, Format> coverages = getAvailableCoverageStores();
                 Format format = coverages.get(coverageFactoryName);
                 final String description = format.getDescription();
                 SubmitLink link;
                 link = new SubmitLink("resourcelink") {
                     @Override
                     public void onSubmit() {
                         setResponsePage(new CoverageStoreNewPage(coverageFactoryName));
                     }
                 };
                 link.add(new Label("resourcelabel", coverageFactoryName));
                 item.add(link);
                 item.add(new Label("resourceDescription", description));
                item.add(new Image("storeIcon", icons.getStoreIcon(format.getClass())));
             }
         };
 
         storeForm.add(dataStoreLinks);
         storeForm.add(coverageLinks);
     }
 
     /**
      * @return the name/description set of available datastore factories
      */
     private Map<String, DataAccessFactory> getAvailableDataStores() {
         // dataStores is transient, a back button may get us to the serialized version so check for
         // it
         if (dataStores == null) {
             final Iterator<DataAccessFactory> availableDataStores;
             availableDataStores = DataAccessFinder.getAvailableDataStores();
 
             Map<String, DataAccessFactory> storeNames = new HashMap<String, DataAccessFactory>();
 
             while (availableDataStores.hasNext()) {
                 DataAccessFactory factory = availableDataStores.next();
                 storeNames.put(factory.getDisplayName(), factory);
             }
             dataStores = storeNames;
         }
         return dataStores;
     }
 
     /**
      * @return the name/description set of available raster formats
      */
     private Map<String, Format> getAvailableCoverageStores() {
         if (coverages == null) {
             Format[] availableFormats = GridFormatFinder.getFormatArray();
             Map<String, Format> formatNames = new HashMap<String, Format>();
             for (Format format : availableFormats) {
                 formatNames.put(format.getName(), format);
             }
             coverages = formatNames;
         }
         return coverages;
     }
 
 }
