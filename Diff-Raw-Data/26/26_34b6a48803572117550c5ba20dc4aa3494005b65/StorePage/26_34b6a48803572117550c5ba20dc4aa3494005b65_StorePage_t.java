 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.store;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.geoserver.web.GeoServerSecuredPage;
 
 /**
  * Page listing all the available stores. Follows the usual filter/sort/page approach, provides ways
  * to bulk delete stores and to add new ones
  */
 @SuppressWarnings("serial")
 public class StorePage extends GeoServerSecuredPage {
     StoreProvider provider = new StoreProvider();
 
     StorePanel table;
 
     public StorePage() {
         // the action buttons
         add(new BookmarkablePageLink("addNew", NewDataPage.class));
         add(removeSelectedLink());
         table = new StorePanel("table", provider);
         table.setOutputMarkupId(true);
         add(table);
 
         // the workspaces drop down
         //add(workspacesDropDown());
     }
 
     Component removeSelectedLink() {
         return new AjaxLink("removeSelected") {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 System.out.println("IMPLEMENT ME!");
             }
         };
     }
 }
