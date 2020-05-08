 package pl.psnc.dl.wf4ever.portal.components;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.authorization.Action;
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
 import org.apache.wicket.event.Broadcast;
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.purl.wf4ever.rosrs.client.Folder;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Resource;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 import org.purl.wf4ever.wf2ro.JobStatus;
 import org.purl.wf4ever.wf2ro.ServiceException;
 import org.purl.wf4ever.wf2ro.Wf2ROService;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.behaviors.WorkflowTransformationJobStatusUpdatingBehaviour;
 import pl.psnc.dl.wf4ever.portal.components.form.AuthenticatedAjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.WorkflowTransformClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.WorkflowTransformedEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.AggregationChangedEvent;
 import pl.psnc.dl.wf4ever.portal.model.ResourceType;
 import pl.psnc.dl.wf4ever.portal.model.wicket.ResourceTypeModel;
 
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * A panel that allows to transform a workflow. It is visible only if provided resource has a resource type 'workflow'.
  * 
  * @author piotrekhol
  * 
  */
 public class WorkflowTransformPanel extends Panel {
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(WorkflowTransformPanel.class);
 
 
     /**
      * The button.
      * 
      * @author piotrekhol
      * 
      */
     @AuthorizeAction(action = Action.RENDER, roles = { Roles.USER })
     private final class TransformButton extends AuthenticatedAjaxEventButton {
 
         /** id. */
         private static final long serialVersionUID = -993018287446638943L;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket ID
          * @param form
          *            for which will be validated
          */
         public TransformButton(String id, Form<?> form) {
             super(id, form, WorkflowTransformPanel.this, WorkflowTransformClickedEvent.class);
             setOutputMarkupPlaceholderTag(true);
         }
 
     }
 
 
     /** id. */
     private static final long serialVersionUID = -2277604858752974738L;
 
     /** Resource type model. */
     private ResourceTypeModel resourceTypeModel;
 
     /** Current folder model. */
     private IModel<Folder> folderModel;
 
     /** RO being transformed. */
     private ResearchObject researchObject;
 
     /** Current folder when the button is pressed. */
     private Folder folder;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param model
      *            the model of the value of the field
      * @param resourceTypeModel
      *            resource type model
      * @param folderModel
      *            current folder model
      */
     public WorkflowTransformPanel(String id, IModel<Resource> model, ResourceTypeModel resourceTypeModel,
             IModel<Folder> folderModel) {
         super(id, model);
         this.resourceTypeModel = resourceTypeModel;
         this.folderModel = folderModel;
         setOutputMarkupPlaceholderTag(true);
 
         Form<?> form = new Form<Void>("form");
         add(form);
         form.add(new TransformButton("button", form));
     }
 
 
     @Override
     protected void onConfigure() {
        setVisible(ResourceType.WORKFLOW.equals(resourceTypeModel.getObject()));
     }
 
 
     @Override
     public void onEvent(IEvent<?> event) {
         if (event.getPayload() instanceof WorkflowTransformClickedEvent) {
             onWorkflowTransformClicked((WorkflowTransformClickedEvent) event.getPayload());
         }
         if (event.getPayload() instanceof WorkflowTransformedEvent) {
             onWorkflowTransformed((WorkflowTransformedEvent) event.getPayload());
         }
     }
 
 
     /**
      * Start the transformation process.
      * 
      * @param event
      *            AJAX event
      */
     private void onWorkflowTransformClicked(WorkflowTransformClickedEvent event) {
         Resource resource = (Resource) WorkflowTransformPanel.this.getDefaultModelObject();
         researchObject = resource.getResearchObject();
         folder = folderModel.getObject();
         try {
             ClientResponse response = resource.getHead();
             MediaType contentType = response.getType();
             Wf2ROService service = MySession.get().getWf2ROService();
             try {
                 JobStatus status = service.transform(resource.getUri(), contentType.toString(), resource
                         .getResearchObject().getUri());
                 add(new WorkflowTransformationJobStatusUpdatingBehaviour(status));
                 event.getTarget().add(this);
             } catch (ServiceException e) {
                 error("Creating the transformation job returned an incorrect status. " + e.getMessage());
                 LOGGER.error("Creating the transformation job returned an incorrect status. ", e);
             }
         } catch (ROSRSException e) {
             error("Accessing the resource returned an incorrect status. " + e.getMessage());
             LOGGER.error("Accessing the resource returned an incorrect status. ", e);
         }
     }
 
 
     /**
      * Move new resources to the same folder as the original workflow.
      * 
      * @param event
      *            AJAX event
      */
     private void onWorkflowTransformed(WorkflowTransformedEvent event) {
         try {
             Set<Resource> oldResources = new HashSet<>(researchObject.getResources().values());
             researchObject.load();
             Set<Resource> newResources = new HashSet<>(researchObject.getResources().values());
             newResources.removeAll(oldResources);
             if (folder != null) {
                 for (Resource resource : newResources) {
                     folder.addEntry(resource, null);
                 }
             }
             send(getPage(), Broadcast.BREADTH, new AggregationChangedEvent(event.getTarget()));
         } catch (ROSRSException | ROException e) {
             LOGGER.error("Error when reloading the RO after workflow transformation", e);
             error("Error when reloading the RO after workflow transformation: " + e.getMessage());
         }
     }
 }
