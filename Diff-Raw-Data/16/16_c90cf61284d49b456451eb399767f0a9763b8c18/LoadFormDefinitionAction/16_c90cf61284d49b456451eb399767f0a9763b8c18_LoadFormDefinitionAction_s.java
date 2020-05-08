 
 package edu.common.dynamicextensions.ui.webui.action;
 
 /**
  * This Action class Loads the Primary Information needed for CreateForm.jsp.
  * This will first check if the form object is already present in cache , If yes, it will update
  * the actionForm and If No, It will populate the actionForm with fresh data.  
  * The exception thrown can be of 'Application' type ,in this case the same Screen will be displayed  
  * added with error messages .
  * And The exception thrown can be of 'System' type, in this case user will be directed to Error Page.
  * @author deepti_shelar
  */
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.LoadFormDefinitionProcessor;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.webui.actionform.FormDefinitionForm;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.util.global.DEConstants;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 
 public class LoadFormDefinitionAction extends BaseDynamicExtensionsAction
 {
 
 	/**
 	 * This method will call LoadFormDefinitionProcessor to load all the information needed for the form.
 	 * It will then forward the action to CreateForm.jsp. 
 	 * 
 	 * @param mapping ActionMapping mapping
 	 * @param form ActionForm form
 	 * @param  request HttpServletRequest request
 	 * @param response HttpServletResponse response
 	 * @return ActionForward forward to next action
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		FormDefinitionForm formDefinitionForm = (FormDefinitionForm) form;
 
 		try
 		{
 			populateContainerInformation(request, formDefinitionForm);
 		}
 		catch (Exception e)
 		{
 			String actionForwardString = catchException(e, request);
 			if ((actionForwardString == null) || (actionForwardString.equals("")))
 			{
 				return mapping.getInputForward();
 			}
 			return mapping.findForward(actionForwardString);
 		}
 
 		return mapping.findForward(DEConstants.SUCCESS);
 	}
 
 	/**
 	 * @param request
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void populateContainerInformation(HttpServletRequest request,
 			FormDefinitionForm formDefinitionForm) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		LoadFormDefinitionProcessor loadFormDefinitionProcessor = LoadFormDefinitionProcessor
 				.getInstance();
 		ContainerInterface container = null;
 
 		EntityGroupInterface entityGroup = (EntityGroup) CacheManager.getObjectFromCache(request,
 				DEConstants.ENTITYGROUP_INTERFACE);
 
 		String operationMode = formDefinitionForm.getOperationMode();
 		if (operationMode == null && request.getAttribute("operationMode") != null)
 		{
 			operationMode = (String) request.getAttribute("operationMode");
 			formDefinitionForm.setOperationMode(operationMode);
 		}
 		String containerIdentifier = request.getParameter("containerIdentifier");
 		if (operationMode != null)
 		{
 			if (operationMode.equalsIgnoreCase(DEConstants.ADD_NEW_FORM))
 			{
 				loadFormDefinitionProcessor.populateContainerInformation(container,
 						formDefinitionForm, entityGroup);
 			}
 			else if (operationMode.equalsIgnoreCase(DEConstants.EDIT_FORM))
 			{
 				if (containerIdentifier != null)
 				{
 					container = loadFormDefinitionProcessor
 							.getContainerForEditing(containerIdentifier);
 				}
 				else
 				{
 					container = WebUIManager.getCurrentContainer(request);
 				}
 				loadFormDefinitionProcessor.populateContainerInformation(container,
 						formDefinitionForm, entityGroup);
 			}
 			else if (operationMode.equalsIgnoreCase(DEConstants.ADD_SUB_FORM_OPR))
 			{
 				loadFormDefinitionProcessor.initializeSubFormAttributes(formDefinitionForm,
 						entityGroup);
 			}
 			else if (operationMode.equalsIgnoreCase(DEConstants.EDIT_SUB_FORM_OPR))
 			{
 				container = WebUIManager.getCurrentContainer(request);
 				loadFormDefinitionProcessor.populateContainerInformation(container,
 						formDefinitionForm, entityGroup);
 
 				ContainerInterface parentContainer = null;
 
 				String parentContainerName = formDefinitionForm.getCurrentContainerName();
 				if (parentContainerName == null
 						&& request.getAttribute("currentContainerName") != null)
 				{
 					parentContainerName = (String) request.getAttribute("currentContainerName");
 					formDefinitionForm.setCurrentContainerName(parentContainerName);
 				}
 
 				if (parentContainerName != null)
 				{
 					parentContainer = (ContainerInterface) CacheManager.getObjectFromCache(request,
 							parentContainerName);
 					populateAssociationInformation(parentContainer, container, formDefinitionForm);
 				}
 			}
 		}
 		else
 		{
 			formDefinitionForm.setOperationMode("");
 			container = WebUIManager.getCurrentContainer(request);
 			if (container != null)
 			{
 				loadFormDefinitionProcessor.populateContainerInformation(container,
 						formDefinitionForm, entityGroup);
 			}
 		}
 
 		ContainerInterface cachedContainer = (ContainerInterface) CacheManager.getObjectFromCache(
 				request, DEConstants.CONTAINER_INTERFACE);
 		String currentContainerName = (String) CacheManager.getObjectFromCache(request,
 				DEConstants.CURRENT_CONTAINER_NAME);
 		loadFormDefinitionProcessor.initializeFormAttributes(entityGroup, cachedContainer,
 				currentContainerName, formDefinitionForm);
 
 		//Added container and its child container into cache.
 		populateChildFormMapInCache(request, cachedContainer);
 	}
 
 	/**
 	 * @param parentContainer
 	 * @param container
 	 * @param formDefinitionForm
 	 */
 	private void populateAssociationInformation(ContainerInterface parentContainer,
 			ContainerInterface childContainer, FormDefinitionForm formDefinitionForm)
 	{
 		if ((parentContainer != null) && (childContainer != null))
 		{
 			String childContainerId = "";
 			if (childContainer.getId() != null)
 			{
 				childContainerId = childContainer.getId().toString();
 			}
 			else
 			{
 				childContainerId = childContainer.getCaption();
 			}
 			AbstractContainmentControlInterface containmentAssociationControl = UserInterfaceiUtility
					.getAssociationControl(parentContainer, childContainerId + "");
 			if (containmentAssociationControl != null)
 			{
 				AssociationInterface association = null;
 				AbstractAttributeInterface abstractAttributeInterface = (AbstractAttributeInterface) containmentAssociationControl
 						.getBaseAbstractAttribute();
 				if ((abstractAttributeInterface != null)
 						&& (abstractAttributeInterface instanceof AssociationInterface))
 				{
 					association = (AssociationInterface) abstractAttributeInterface;
 					Cardinality cardinality = association.getTargetRole().getMaximumCardinality();
 					if (cardinality == Cardinality.MANY)
 					{
 						formDefinitionForm.setViewAs(ProcessorConstants.VIEW_AS_SPREADSHEET);
 					}
 					else
 					{
 						formDefinitionForm.setViewAs(ProcessorConstants.VIEW_AS_FORM);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method populates the child sub-form of the cached parent Container into Cache.
 	 * @param parentContainer
 	 */
 	private void populateChildFormMapInCache(HttpServletRequest request,
 			ContainerInterface parentContainer)
 	{
 		if (parentContainer != null)
 		{
 			String containerName = parentContainer.getCaption();
 			CacheManager.addObjectToCache(request, containerName, parentContainer);
 
 			Collection<ControlInterface> controlsCollection = parentContainer
 					.getControlCollection();
 			if ((controlsCollection != null) || (!controlsCollection.isEmpty()))
 			{
 				for (ControlInterface control : controlsCollection)
 				{
 					if ((control != null) && (control instanceof ContainmentAssociationControl))
 					{
 						ContainerInterface childContainer = ((ContainmentAssociationControl) control)
 								.getContainer();
 						populateChildFormMapInCache(request, childContainer);
 					}
 				}
 			}
 		}
 	}
 }
