 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.store;
 
 import org.apache.wicket.markup.html.form.Form;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CoverageStoreInfo;
 import org.geoserver.web.data.layer.NewLayerPage;
 
 /**
  * Supports coverage store configuration
  * 
  * @author Andrea Aime
  */
 public class CoverageStoreEditPage extends AbstractCoverageStorePage {
     Form paramsForm;
 
     /**
      * 
      * @param storeId
      *            the store id
      */
     public CoverageStoreEditPage(final String storeId) {
         Catalog catalog = getCatalog();
         CoverageStoreInfo store = catalog.getCoverageStore(storeId);
         if (store == null) {
             throw new IllegalArgumentException("Cannot find coverage store " + storeId);
         }
 
         initUI(store);
     }
 
     protected final void onSave(final CoverageStoreInfo info) {
         getCatalog().save(info);
         setResponsePage(new NewLayerPage(info.getId()));
     }
 
 }
