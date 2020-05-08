 package pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.list.PropertyListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.resource.CssResourceReference;
 import org.purl.wf4ever.rosrs.client.Resource;
 
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.behaviours.IAjaxLinkListener;
 
 /**
  * List of files displayed as tails.
  * 
  * @author pejot
  * 
  */
 public class FilesPanel extends Panel {
 
     /** List of files. */
     private ListView<Resource> listView;
     /** Serialization. */
     private static final long serialVersionUID = 1L;
     /** CSS tail resource reference. */
     private CssResourceReference cssResourceReference = new CssResourceReference(FilesPanel.class, "tails.css");
     /** Listeners for the selected resource. */
     private List<IAjaxLinkListener> listeners;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      */
     public FilesPanel(String id, IModel<List<Resource>> foldersModel) {
         super(id, new Model<Resource>());
         setOutputMarkupId(true);
         listeners = new ArrayList<IAjaxLinkListener>();
         listView = new PropertyListView<Resource>("filesListView", foldersModel) {
 
             private static final long serialVersionUID = -6310254217773728128L;
 
 
             @Override
             protected void populateItem(final ListItem<Resource> item) {
 
                 final Resource thing = item.getModelObject();
                 final Label label = new Label("tailLabel", item.getModelObject().getName());
                 final AjaxLink<Object> link = new AjaxLink<Object>("tailLink", new Model()) {
 
                     /** Serialization */
                     private static final long serialVersionUID = 6984606437114241889L;
 
 
                     @Override
                     public void onClick(AjaxRequestTarget target) {
                         if (thing.equals(getSelectedFile())) {
                             unselect();
                         } else {
                             setSelectedFile(thing);
                         }
                         for (IAjaxLinkListener listener : listeners) {
                             listener.onAjaxLinkClicked(target);
                         }
                     }
                 };
 
                 link.add(AttributeModifier.replace("uri", item.getModelObject().getUri()));
                 item.add(link);
                 link.add(label);
             }
 
         };
         add(listView);
     }
 
 
     /**
      * Add new link listener.
      * 
      * @param listener
      *            link listener
      */
     public void addLinkListeners(IAjaxLinkListener listener) {
         listeners.add(listener);
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         response.renderCSSReference(cssResourceReference);
         super.renderHead(response);
     }
 
 
     @SuppressWarnings("unchecked")
     public IModel<Resource> getModel() {
         return (IModel<Resource>) this.getDefaultModel();
     }
 
 
     public void setSelectedFile(Resource resource) {
         getModel().setObject(resource);
     }
 
 
     public Resource getSelectedFile() {
         return getModel().getObject();
     }
 
 
     /**
      * Unselect selected file from outside (For example on node click).
      */
     public void unselect() {
         setSelectedFile(null);
     }
 
 }
