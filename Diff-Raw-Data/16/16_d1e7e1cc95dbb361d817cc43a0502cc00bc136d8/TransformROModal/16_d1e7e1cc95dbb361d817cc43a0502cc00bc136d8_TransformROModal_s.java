 package pl.psnc.dl.wf4ever.portal.modals;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
 import org.apache.wicket.event.Broadcast;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.Folder;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Resource;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import pl.psnc.dl.wf4ever.portal.events.WorkflowTransormRequestEvent;
 
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * A modal for adding resources to the RO.
  * 
  * 
  */
 public class TransformROModal extends AbstractModal {
 
 	/** id. */
 	private static final long serialVersionUID = 2193789648186156745L;
 	private boolean scriptsCheckBoxState = true;
 	private boolean nestedWfCheckboxState = true;
 	private boolean webservicesCheckboxState = true;
 	private AjaxCheckBox checkBoxScripts;
 	private AjaxCheckBox checkBoxNestedWf;
 	private AjaxCheckBox checkBoxWebsServices;
 	private IModel<ResearchObject> resarchObjectModel;
 	private List<String> extractToFoldersList;
 	private List<String> scriptsToFoldersList;
 	private List<String> nestedWfToFoldersList;
 	private List<String> webservicesToFoldersList;
 	private String selectedExractTo;
 	private String selectedScriptsTo;
 	private String selectedNestedWfTo;
 	private String selectedWebservicesTo;
 	private static final String ROOT_FOLDER = "/";
 	private DropDownChoice<String> dropDownChoiceExtractTo;
 	private DropDownChoice<String> dropDownChoiceScriptsTo;
 	private DropDownChoice<String> dropDownChoiceNestedWfTo;
 	private DropDownChoice<String> dropDownChoiceWebservicesTo;
 	IModel<Folder> folderModel;
 	WebMarkupContainer infoPanel;
 	WebMarkupContainer actionPanel;
 	boolean transformable;
 	IModel<Resource> resourceModel;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param id
 	 *            wicket id
 	 * @param toDelete
 	 *            ROs to delete
 	 */
 	public TransformROModal(String id, IModel<ResearchObject> researchObjectModel,
 			IModel<Folder> folderModel, IModel<Resource> resourceModel) {
 		super(id, "delete-ro-modal", "Annotate & Transform");
 		infoPanel = new WebMarkupContainer("info-panel");
 		actionPanel = new WebMarkupContainer("action-panel");
 		extractToFoldersList = new ArrayList<String>();
 		scriptsToFoldersList = new ArrayList<String>();
 		nestedWfToFoldersList = new ArrayList<String>();
 		webservicesToFoldersList = new ArrayList<String>();
 		this.resarchObjectModel = researchObjectModel;
 		this.folderModel = folderModel;
 		this.resourceModel = resourceModel;
 		this.transformable = false;
 
 		try {
 			if (resourceModel.getObject() != null) {
 				ClientResponse response = resourceModel.getObject().getHead();
 				MediaType contentType = response.getType();
 				if (contentType.toString().equals("application/vnd.taverna.t2flow+xml")) {
 					transformable = true;
 				}
 			}
 		} catch (ROSRSException e) {
 			e.printStackTrace();
 		}
 
 		for (URI key : researchObjectModel.getObject().getFolders().keySet()) {
 			String path = researchObjectModel.getObject().getFolder(key).getPath().toString();
 			extractToFoldersList.add(path);
 			scriptsToFoldersList.add(path);
 			nestedWfToFoldersList.add(path);
 			webservicesToFoldersList.add(path);
 			if (path.equals("config/scripts/")) {
 				selectedScriptsTo = path;
 			} else if (path.equals("workflows/nested/")) {
 				selectedNestedWfTo = path;
 			} else if (path.equals("config/web services/")) {
 				selectedWebservicesTo = path;
 			}
 		}
 		extractToFoldersList.add(0, ROOT_FOLDER);
 		scriptsToFoldersList.add(0, ROOT_FOLDER);
 		nestedWfToFoldersList.add(0, ROOT_FOLDER);
 		webservicesToFoldersList.add(0, ROOT_FOLDER);
 
 		if (folderModel.getObject() != null) {
 			selectedExractTo = folderModel.getObject().getPath();
 		} else {
 			selectedExractTo = ROOT_FOLDER;
 		}
 		if (selectedScriptsTo == null) {
 			selectedScriptsTo = ROOT_FOLDER;
 		}
 		if (selectedNestedWfTo == null) {
 			selectedNestedWfTo = ROOT_FOLDER;
 		}
 		if (selectedWebservicesTo == null) {
 			selectedWebservicesTo = ROOT_FOLDER;
 		}
 
 		this.resarchObjectModel = researchObjectModel;
 
 		checkBoxScripts = new AjaxCheckBox("checkBoxScripts", new PropertyModel<Boolean>(this,
 				"scriptsCheckBoxState")) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				dropDownChoiceScriptsTo.setEnabled(scriptsCheckBoxState);
 			}
 		};
 
 		checkBoxNestedWf = new AjaxCheckBox("checkBoxNestedWf", new PropertyModel<Boolean>(this,
 				"nestedWfCheckboxState")) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				dropDownChoiceNestedWfTo.setEnabled(nestedWfCheckboxState);
 			}
 		};
 
 		checkBoxWebsServices = new AjaxCheckBox("checkBoxWebsServices", new PropertyModel<Boolean>(
 				this, "webservicesCheckboxState")) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				dropDownChoiceWebservicesTo.setEnabled(webservicesCheckboxState);
 			}
 		};
 
 		actionPanel.add(checkBoxScripts);
 		actionPanel.add(checkBoxNestedWf);
 		actionPanel.add(checkBoxWebsServices);
 
 		dropDownChoiceExtractTo = new DropDownChoice<String>("extractToFoldersList",
 				new PropertyModel<String>(this, "selectedExractTo"), extractToFoldersList);
 		dropDownChoiceScriptsTo = new DropDownChoice<String>("scriptsToFoldersList",
 				new PropertyModel<String>(this, "selectedScriptsTo"), scriptsToFoldersList);
 		dropDownChoiceNestedWfTo = new DropDownChoice<String>("nestedWfToFoldersList",
 				new PropertyModel<String>(this, "selectedNestedWfTo"), nestedWfToFoldersList);
 		dropDownChoiceWebservicesTo = new DropDownChoice<String>("webservicesToFoldersList",
 				new PropertyModel<String>(this, "selectedWebservicesTo"), webservicesToFoldersList);
 
 		actionPanel.add(dropDownChoiceExtractTo);
 		actionPanel.add(dropDownChoiceScriptsTo);
 		actionPanel.add(dropDownChoiceNestedWfTo);
 		actionPanel.add(dropDownChoiceWebservicesTo);
 		modal.add(actionPanel);
 		modal.add(infoPanel);
 		actionPanel.setVisible(transformable);
 		infoPanel.setVisible(!transformable);
 	}
 
 	@Override
 	public void onOk(AjaxRequestTarget target) {
 		URI sendScrtipsToUri = scriptsCheckBoxState ? getFolderUri(selectedScriptsTo) : null;
 		URI sendNestedWfToUri = nestedWfCheckboxState ? getFolderUri(selectedNestedWfTo) : null;
 		URI sendWebSerbicvesTo = webservicesCheckboxState ? getFolderUri(selectedWebservicesTo)
 				: null;
 
 		send(getPage(), Broadcast.BREADTH, new WorkflowTransormRequestEvent(target,
 				getFolderUri(selectedExractTo), sendScrtipsToUri, sendNestedWfToUri,
 				sendWebSerbicvesTo));
 		hide(target);
 	}
 
 	public boolean isScriptsCheckBoxState() {
 		return scriptsCheckBoxState;
 	}
 
 	public void setScriptsCheckBoxState(boolean scriptsCheckBoxState) {
 		this.scriptsCheckBoxState = scriptsCheckBoxState;
 	}
 
 	public boolean isNestedWfCheckboxState() {
 		return nestedWfCheckboxState;
 	}
 
 	public void setNestedWfCheckboxState(boolean nestedWfCheckboxState) {
 		this.nestedWfCheckboxState = nestedWfCheckboxState;
 	}
 
 	public boolean isWebservicesCheckboxState() {
 		return webservicesCheckboxState;
 	}
 
 	public void setWebservicesCheckboxState(boolean webservicesCheckboxState) {
 		this.webservicesCheckboxState = webservicesCheckboxState;
 	}
 
 	private URI getFolderUri(String path) {
 		if (path.equals(ROOT_FOLDER)) {
 			return resarchObjectModel.getObject().getUri();
 		}
 		for (URI uri : resarchObjectModel.getObject().getFolders().keySet()) {
 			Folder folder = resarchObjectModel.getObject().getFolder(uri);
 			if (folder.getPath().equals(path)) {
 				return folder.getUri();
 			}
 		}
 		return null;
 	}
 }
