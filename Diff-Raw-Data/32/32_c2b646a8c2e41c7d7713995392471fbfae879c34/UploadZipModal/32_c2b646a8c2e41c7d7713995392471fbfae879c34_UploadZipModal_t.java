 package pl.psnc.dl.wf4ever.portal.modals;
 
 import java.net.URI;
 
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.lang.Bytes;
 
 import pl.psnc.dl.wf4ever.portal.components.form.RequiredURITextField;
 import pl.psnc.dl.wf4ever.portal.events.CancelClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.OkClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.ros.ZipAddClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.ros.ZipAddReadyEvent;
 import pl.psnc.dl.wf4ever.portal.model.ResourceLocalRemote;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * A modal for uploading a zip archive.
  * 
  * @author piotrekhol
  * 
  */
 public class UploadZipModal extends AbstractModal {
 
     /** id. */
     private static final long serialVersionUID = -7754788822535330561L;
 
     /** Type of currently added resource. */
     private ResourceLocalRemote resourceType = ResourceLocalRemote.LOCAL;
 
     /** Resource URI. */
     private URI resourceURI;
 
     /** Div with remote resource URI. */
     private final WebMarkupContainer resourceDiv;
 
     /** Div for uploading files. */
     private final WebMarkupContainer fileDiv;
 
     /** Component for the uploaded file. */
     private FileUploadField fileUpload;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param eventBusModel
      *            event bus
      */
     public UploadZipModal(String id, final IModel<EventBus> eventBusModel) {
         super(id, eventBusModel, "upload-zip-modal", "Create an RO from ZIP");
 
         // Enable multipart mode (needed for uploading files)
         form.setMultiPart(true);
 
         resourceDiv = new WebMarkupContainer("resourceURIDiv");
         resourceDiv.setOutputMarkupId(true);
         resourceDiv.setOutputMarkupPlaceholderTag(true);
         modal.add(resourceDiv);
         fileDiv = new WebMarkupContainer("fileUploadDiv");
         fileDiv.setOutputMarkupId(true);
         fileDiv.setOutputMarkupPlaceholderTag(true);
        modal.add(fileDiv);
 
         RadioGroup<ResourceLocalRemote> radioGroup = new RadioGroup<ResourceLocalRemote>("radioGroup",
                 new PropertyModel<ResourceLocalRemote>(this, "resourceType"));
         modal.add(radioGroup);
         Radio<ResourceLocalRemote> local = new Radio<ResourceLocalRemote>("local", new Model<ResourceLocalRemote>(
                 ResourceLocalRemote.LOCAL));
         local.add(new AjaxEventBehavior("onclick") {
 
             /** id. */
             private static final long serialVersionUID = -1653173329010286091L;
 
 
             @Override
             protected void onEvent(AjaxRequestTarget target) {
                 resourceDiv.setVisible(false);
                 fileDiv.setVisible(true);
                 target.add(resourceDiv);
                 target.add(fileDiv);
             }
 
         });
         radioGroup.add(local);
         Radio<ResourceLocalRemote> remote = new Radio<ResourceLocalRemote>("remote", new Model<ResourceLocalRemote>(
                 ResourceLocalRemote.REMOTE));
         remote.add(new AjaxEventBehavior("onclick") {
 
             /** id. */
             private static final long serialVersionUID = -1689888759359590693L;
 
 
             @Override
             protected void onEvent(AjaxRequestTarget target) {
                 resourceDiv.setVisible(true);
                 fileDiv.setVisible(false);
                 target.add(resourceDiv);
                 target.add(fileDiv);
             }
 
         });
         radioGroup.add(remote);
 
         // max upload size, 500MB
         form.setMaxSize(Bytes.megabytes(500));
         fileUpload = new FileUploadField("fileUpload") {
 
             /** id. */
             private static final long serialVersionUID = 7183569296311894186L;
 
 
             @Override
             protected boolean forceCloseStreamsOnDetach() {
                 return false;
             }
         };
         fileDiv.add(fileUpload);
 
        resourceDiv.add(new RequiredURITextField("resourceURI", new PropertyModel<URI>(this, "resourceURI")));
     }
 
 
     /**
      * Show itself.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onAddZipClicked(ZipAddClickedEvent event) {
         show(event.getTarget());
     }
 
 
     /**
      * Post an event and hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onOk(OkClickedEvent event) {
         switch (resourceType) {
             case LOCAL:
             default:
                 final FileUpload uploadedFile = fileUpload.getFileUpload();
                 if (uploadedFile != null) {
                     eventBusModel.getObject().post(new ZipAddReadyEvent(event.getTarget(), uploadedFile));
                     hide(event.getTarget());
                 }
                 break;
             case REMOTE:
                 eventBusModel.getObject().post(new ZipAddReadyEvent(event.getTarget(), resourceURI));
                 hide(event.getTarget());
                 break;
         }
         event.getTarget().add(feedbackPanel);
     }
 
 
     /**
      * Hide.
      * 
      * @param event
      *            AJAX event
      */
     @Subscribe
     public void onCancel(CancelClickedEvent event) {
         hide(event.getTarget());
     }
 
 
     @Override
     protected void onConfigure() {
         super.onConfigure();
         switch (resourceType) {
             case LOCAL:
                 resourceDiv.setVisible(false);
                 fileDiv.setVisible(true);
                 break;
             case REMOTE:
                 resourceDiv.setVisible(true);
                 fileDiv.setVisible(false);
                 break;
             default:
                 break;
         }
     }
 
 
     public URI getResourceURI() {
         return resourceURI;
     }
 
 
     public void setResourceURI(URI resourceURI) {
         this.resourceURI = resourceURI;
     }
 
 
     public ResourceLocalRemote getResourceType() {
         return resourceType;
     }
 
 
     public void setResourceType(ResourceLocalRemote resourceType) {
         this.resourceType = resourceType;
     }
 }
