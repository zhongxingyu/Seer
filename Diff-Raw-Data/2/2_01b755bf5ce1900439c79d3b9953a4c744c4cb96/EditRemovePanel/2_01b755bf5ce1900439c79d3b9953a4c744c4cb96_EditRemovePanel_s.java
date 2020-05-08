 package org.geoserver.web.data.tree;
 
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.geoserver.web.GeoServerApplication;
 
 /**
 * A simple component that lines up
  * 
  * @author aaime
  * 
  */
 public class EditRemovePanel extends Panel {
 
     private AbstractCatalogNode node;
 
     public EditRemovePanel(String id, AbstractCatalogNode node) {
         super(id);
         this.node = node;
 
         AjaxLink link = new AjaxLink("edit") {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 onEditClick(target);
             }
         };
         link.add(new Image("editIcon", new ResourceReference(
                 GeoServerApplication.class,
                 "img/icons/silk/pencil.png")));
         add(link);
 
         link = new AjaxLink("remove") {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 onRemoveClick(target);
             }
         };
         link.add(new Image("removeIcon", new ResourceReference(
                 GeoServerApplication.class,
                 "img/icons/silk/delete.png")));
         add(link);
     }
 
     protected void onRemoveClick(AjaxRequestTarget target) {
         System.out.println("Removed!");
 
     }
 
     protected void onEditClick(AjaxRequestTarget target) {
         System.out.println("Edit!");
 
     }
 }
