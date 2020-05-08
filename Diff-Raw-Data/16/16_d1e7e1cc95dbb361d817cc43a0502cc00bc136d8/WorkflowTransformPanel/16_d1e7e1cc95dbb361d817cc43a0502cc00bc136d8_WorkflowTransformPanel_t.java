 package pl.psnc.dl.wf4ever.portal.components;
 
 import java.net.URI;
 
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
 import org.purl.wf4ever.rosrs.client.FolderEntry;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Resource;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 import org.purl.wf4ever.wf2ro.JobStatus;
 import org.purl.wf4ever.wf2ro.ServiceException;
 import org.purl.wf4ever.wf2ro.Wf2ROService;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.behaviors.WorkflowTransformationJobStatusUpdatingBehaviour;
 import pl.psnc.dl.wf4ever.portal.components.form.AuthenticatedAjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.WorkflowTransformClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.WorkflowTransformedEvent;
 import pl.psnc.dl.wf4ever.portal.events.WorkflowTransormRequestEvent;
 import pl.psnc.dl.wf4ever.portal.events.aggregation.AggregationChangedEvent;
 import pl.psnc.dl.wf4ever.portal.modals.TransformROModal;
 import pl.psnc.dl.wf4ever.portal.model.ResourceType;
 import pl.psnc.dl.wf4ever.portal.model.wicket.ResourceTypeModel;
 
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * A panel that allows to transform a workflow. It is visible only if provided
  * resource has a resource type 'workflow'.
  * 
  * 
  */
 public class WorkflowTransformPanel extends Panel {
 	Folder extractToFolder;
 	Folder extractToScript;
 	Folder extractToNested;
 	Folder extractToWS;
 
 	/** Logger. */
 	private static final Logger LOGGER = Logger.getLogger(WorkflowTransformPanel.class);
 
 	/**
 	 * The button.
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
 	// private Folder folder;
 	TransformROModal transformROModal;
 	IModel<Resource> model;
 	IModel<ResearchObject> roModel;
 	boolean transformable;
 
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
 	public WorkflowTransformPanel(String id, IModel<Resource> model,
 			ResourceTypeModel resourceTypeModel, IModel<Folder> folderModel,
 			IModel<ResearchObject> roModel) {
 		super(id, model);
 		this.model = model;
 		this.roModel = roModel;
 		this.resourceTypeModel = resourceTypeModel;
 		this.folderModel = folderModel;
 		setOutputMarkupPlaceholderTag(true);
		//ClientResponse response;
 
 		transformROModal = new TransformROModal("transform-modal", roModel, folderModel, model);
 		add(transformROModal);
 		Form<?> form = new Form<Void>("form");
 		add(form);
 		form.add(new TransformButton("button", form));
 	}
 
 	@Override
 	protected void onConfigure() {
 		setVisible(resourceTypeModel.getObject().contains(ResourceType.WORKFLOW));
 	}
 
 	@Override
 	public void onEvent(IEvent<?> event) {
 		if (event.getPayload() instanceof WorkflowTransformClickedEvent) {
 			onWorkflowTransformClicked((WorkflowTransformClickedEvent) event.getPayload());
 		}
 		if (event.getPayload() instanceof WorkflowTransformedEvent) {
 			onWorkflowTransformed((WorkflowTransformedEvent) event.getPayload());
 		}
 		if (event.getPayload() instanceof WorkflowTransormRequestEvent) {
 			onWorkflowTransformRequest((WorkflowTransormRequestEvent) event.getPayload());
 		}
 	}
 
 	/**
 	 * Start the transformation process.
 	 * 
 	 * @param event
 	 *            AJAX event
 	 */
 	private void onWorkflowTransformClicked(WorkflowTransformClickedEvent event) {
 		TransformROModal transformROModal2 = new TransformROModal("transform-modal", roModel,
 				folderModel, model);
 		transformROModal.replaceWith(transformROModal2);
 		transformROModal = transformROModal2;
 		transformROModal.show(event.getTarget());
 	}
 
 	private void onWorkflowTransformRequest(WorkflowTransormRequestEvent event) {
 
 		URI extractToFolderUri = event.getExtractToFolderUri();
 		URI nestedRoToFolderUri = event.getNestedRoToFolderUri();
 		URI webservicesToFolderUri = event.getWebservicesToFolderUri();
 		URI scriptsToFolderUri = event.getScriptsToFolderUri();
		
		System.out.println(extractToFolderUri!=null ? extractToFolderUri.toString() : "null");
		System.out.println(nestedRoToFolderUri!=null ? nestedRoToFolderUri.toString() : "null");
		System.out.println(webservicesToFolderUri!=null? webservicesToFolderUri.toString() : "null");
		System.out.println(scriptsToFolderUri!=null ? scriptsToFolderUri.toString() : "null");
		
		
 
 		Resource resource = (Resource) WorkflowTransformPanel.this.getDefaultModelObject();
 		researchObject = resource.getResearchObject();
 
 		try {
 			ClientResponse response = resource.getHead();
 			MediaType contentType = response.getType();
 			Wf2ROService service = MySession.get().getWf2ROService();
 			extractToFolder = researchObject.getFolder(event.getExtractToFolderUri());
 			extractToNested = researchObject.getFolder(event.getNestedRoToFolderUri());
 			extractToWS = researchObject.getFolder(event.getWebservicesToFolderUri());
 			extractToScript = researchObject.getFolder(event.getScriptsToFolderUri());
 
 			try {
 				JobStatus status = service.transform(resource.getUri(), contentType.toString(),
 						resource.getResearchObject().getUri(), extractToFolderUri,
 						nestedRoToFolderUri, scriptsToFolderUri, webservicesToFolderUri
 
 				);
 				add(new WorkflowTransformationJobStatusUpdatingBehaviour(status));
 				event.getTarget().add(this);
 			} catch (ServiceException e) {
 				error("Creating the transformation job returned an incorrect status. "
 						+ e.getMessage());
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
 			researchObject.load();
 			if (extractToFolder != null) {
 				extractToFolder.load();
 				for (FolderEntry entry : extractToFolder.getFolderEntries().values()) {
 					researchObject.addFolderEntry(entry);
 				}
 			}
 			if (extractToNested != null) {
 				extractToNested.load();
 				for (FolderEntry entry : extractToNested.getFolderEntries().values()) {
 					researchObject.addFolderEntry(entry);
 				}
 			}
 			if (extractToWS != null) {
 				extractToWS.load();
 				for (FolderEntry entry : extractToWS.getFolderEntries().values()) {
 					researchObject.addFolderEntry(entry);
 				}
 			}
 			if (extractToScript != null) {
 				extractToScript.load();
 				for (FolderEntry entry : extractToScript.getFolderEntries().values()) {
 					researchObject.addFolderEntry(entry);
 				}
 			}
 
 			send(getPage(), Broadcast.BREADTH, new AggregationChangedEvent(event.getTarget()));
 
 		} catch (Exception e) { // TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
