 
 package edu.common.dynamicextensions.ui.webui.action;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ApplyFormDefinitionProcessor;
 import edu.common.dynamicextensions.processor.ContainerProcessor;
 import edu.common.dynamicextensions.processor.EntityProcessor;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.webui.actionform.FormDefinitionForm;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.global.Constants;
 
 /**
  * This Action class handles two situations , 
  * 1. When user selects 'Next' from createForm.jsp. This time a call to ApplyFormDefinitionProcessor
  * will just create an entity and populate it with actionform's data. This entity is then saved to cache.
  * 2. When user selects 'Save' from createForm.jsp. This time a call to ApplyFormDefinitionProcessor 
  * will create an entity and will save it to database. This entity is then saved to cache.
  * The exception thrown can be of 'Application' type ,in this case the same Screen will be displayed  
  * added with error messages .
  * And The exception thrown can be of 'System' type, in this case user will be directed to Error Page.
  * @author deepti_shelar
  */
 public class ApplyFormDefinitionAction extends BaseDynamicExtensionsAction
 {
 
 	/**
 	 * This method will call ApplyFormDefinitionProcessor for actually updating the cache and then
 	 * forwards the action to either BuildForm.jsp or CreateForm.jsp depending on the Operation.
 	 *
 	 * @param mapping ActionMapping mapping
 	 * @param form ActionForm form
 	 * @param  request HttpServletRequest request
 	 * @param response HttpServletResponse response
 	 * @return ActionForward forward to next action
 	 * @throws Exception 
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		ActionForward actionForward = null;
 
 		String target = null;
 		FormDefinitionForm formDefinitionForm = (FormDefinitionForm) form;
 
 		String operationMode = formDefinitionForm.getOperationMode();
 		try
 		{
 			if (operationMode != null)
 			{
 				if (operationMode.equals(Constants.ADD_SUB_FORM_OPR))
 				{
 					addSubForm(request, formDefinitionForm);
 				}
 				else if (operationMode.equals(Constants.EDIT_SUB_FORM_OPR))
 				{
 					editSubForm(request, formDefinitionForm);
 				}
 				else
 				//called when "Add form" or "edit form" operation performed.
 				{
 					applyFormDefinition(request, formDefinitionForm);
 				}
 			}
 
 			String operation = formDefinitionForm.getOperation();
 			if (operation.equals(Constants.SAVE_FORM))
 			{
 				saveContainer(request, formDefinitionForm);
 				saveMessages(request, getSuccessMessage(formDefinitionForm));
 			}
 
 			target = findForwardTarget(operation);
 			String callbackURL = null;
 			if (target.equals(Constants.SHOW_DYNAMIC_EXTENSIONS_HOMEPAGE))
 			{
 				callbackURL = redirectCallbackURL(request, WebUIManagerConstants.SUCCESS);
				if (callbackURL != null && !callbackURL.equals(""))
 				{
 					response.sendRedirect(callbackURL);
 					target = null;
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			target = catchException(e, request);
 			if ((target == null) || (target.equals("")))
 			{
 				actionForward = mapping.getInputForward();
 			}
 		}
 
 		if (target != null && actionForward == null)
 		{
 			actionForward = mapping.findForward(target);
 		}
 		return actionForward;
 	}
 
 	/**
 	 * This method saves the Container in the Database, that is currently in the Cache.
 	 * @param request
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void saveContainer(HttpServletRequest request, FormDefinitionForm formDefinitionForm)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 
 		//ContainerInterface currentContainer = WebUIManager.getCurrentContainer(request);
 		ContainerInterface currentContainer = (ContainerInterface) CacheManager.getObjectFromCache(
 				request, Constants.CONTAINER_INTERFACE);
 		if (currentContainer != null)
 		{
 			containerProcessor.saveContainer(currentContainer);
 		}
 	}
 
 	/**
 	 * @param mapping 
 	 * @param operation
 	 * @return
 	 */
 	private String findForwardTarget(String operation)
 	{
 		if (operation != null)
 		{
 			if (operation.equals(Constants.BUILD_FORM))
 			{
 				return Constants.SHOW_BUILD_FORM_JSP;
 			}
 			else if (operation.equals(Constants.SAVE_FORM))
 			{
 				return Constants.SHOW_DYNAMIC_EXTENSIONS_HOMEPAGE;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void editSubForm(HttpServletRequest request, FormDefinitionForm formDefinitionForm)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if ((request != null) && (formDefinitionForm != null))
 		{
 			ContainerInterface currentContainer = WebUIManager.getCurrentContainer(request);
 			//update container
 			ContainerProcessor containerProcessor = ContainerProcessor.getInstance();
 			containerProcessor.populateContainerInterface(currentContainer, formDefinitionForm);
 
 			//update entity
 			EntityProcessor entityProcessor = EntityProcessor.getInstance();
 			entityProcessor.populateEntity(formDefinitionForm, currentContainer.getEntity());
 
 			//Update Associations
 			//Get parent container
 			ContainerInterface parentContainer = null;
 			String parentContainerName = formDefinitionForm.getCurrentContainerName();
 			if (parentContainerName != null)
 			{
 				parentContainer = (ContainerInterface) CacheManager.getObjectFromCache(request,
 						parentContainerName);
 				updateAssociation(parentContainer, currentContainer, formDefinitionForm);
 			}
 		}
 	}
 
 	/**
 	 * @param parentContainer
 	 * @param childContainer
 	 * @param formDefinitionForm 
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void updateAssociation(ContainerInterface parentContainer,
 			ContainerInterface childContainer, FormDefinitionForm formDefinitionForm)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if ((parentContainer != null) && (childContainer != null))
 		{
 			ContainmentAssociationControl containmentAssociationControl = UserInterfaceiUtility
 					.getAssociationControl(parentContainer, childContainer.getId().toString());
 			if (containmentAssociationControl != null)
 			{
 				AssociationInterface association = null;
 				AbstractAttributeInterface abstractAttributeInterface = containmentAssociationControl
 						.getAbstractAttribute();
 				if ((abstractAttributeInterface != null)
 						&& (abstractAttributeInterface instanceof AssociationInterface))
 				{
 					association = (AssociationInterface) abstractAttributeInterface;
 					ApplyFormDefinitionProcessor applyFormDefinitionProcessor = ApplyFormDefinitionProcessor
 							.getInstance();
 					association = applyFormDefinitionProcessor.associateEntity(association,
 							parentContainer, childContainer, formDefinitionForm);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param request
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void addSubForm(HttpServletRequest request, FormDefinitionForm formDefinitionForm)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface mainFormContainer = WebUIManager.getCurrentContainer(request);
 
 		ApplyFormDefinitionProcessor applyFormDefinitionProcessor = ApplyFormDefinitionProcessor
 				.getInstance();
 		ContainerInterface subFormContainer = applyFormDefinitionProcessor.getSubFormContainer(
 				formDefinitionForm, mainFormContainer);
 		AssociationInterface association = applyFormDefinitionProcessor.createAssociation();
 		association = applyFormDefinitionProcessor.associateEntity(association, mainFormContainer,
 				subFormContainer, formDefinitionForm);
 		applyFormDefinitionProcessor.addSubFormControlToContainer(mainFormContainer,
 				subFormContainer, association);
 
 		if (isNewEnityCreated(formDefinitionForm))
 		{
 			//if new entity is created, set its container id in form and container interface in cache.
 			if (subFormContainer != null)
 			{
 				applyFormDefinitionProcessor.associateParentGroupToNewEntity(subFormContainer,
 						mainFormContainer);
 				updateCacheReferences(request, subFormContainer);
 			}
 		}
 	}
 
 	/**
 	 * @param request
 	 * @param container
 	 */
 	private void updateCacheReferences(HttpServletRequest request, ContainerInterface container)
 	{
 		if (container != null)
 		{
 			CacheManager.addObjectToCache(request, Constants.CURRENT_CONTAINER_NAME, container
 					.getCaption());
 			CacheManager.addObjectToCache(request, container.getCaption(), container);
 		}
 	}
 
 	/**
 	 * @param formDefinitionForm 
 	 * @return
 	 */
 	private boolean isNewEnityCreated(FormDefinitionForm formDefinitionForm)
 	{
 		if (formDefinitionForm != null)
 		{
 			if (ProcessorConstants.CREATE_AS_NEW.equals(formDefinitionForm.getCreateAs()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param request
 	 * @param formDefinitionForm
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private void applyFormDefinition(HttpServletRequest request,
 			FormDefinitionForm formDefinitionForm) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface containerInterface = WebUIManager.getCurrentContainer(request);
 		EntityGroupInterface entityGroup = (EntityGroupInterface) CacheManager.getObjectFromCache(
 				request, Constants.ENTITYGROUP_INTERFACE);
 
 		ApplyFormDefinitionProcessor applyFormDefinitionProcessor = ApplyFormDefinitionProcessor
 				.getInstance();
 		containerInterface = applyFormDefinitionProcessor.addEntityToContainer(containerInterface,
 				formDefinitionForm, entityGroup);
 		updateCacheReferences(request, containerInterface);
 		if (CacheManager.getObjectFromCache(request, Constants.CONTAINER_INTERFACE) == null)
 		{
 			CacheManager.addObjectToCache(request, Constants.CONTAINER_INTERFACE,
 					containerInterface);
 		}
 	}
 
 	/**
 	 * 
 	 * @param formDefinitionForm actionform
 	 * @return ActionMessages Messages
 	 */
 	private ActionMessages getSuccessMessage(FormDefinitionForm formDefinitionForm)
 	{
 		ActionMessages actionMessages = new ActionMessages();
 		String formName = formDefinitionForm.getFormName();
 		actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
 				"app.entitySaveSuccessMessage", formName));
 		return actionMessages;
 	}
 
 	/**
 	 * This method gets the Callback URL from cahce, reforms it and redirect the response to it. 
 	 * @param request HttpServletRequest to obtain session
 	 * @param response HttpServletResponse to redirect the CallbackURL
 	 * @param recordIdentifier Identifier of the record to reconstruct the CallbackURL
 	 * @return true if CallbackURL is redirected, false otherwise
 	 * @throws IOException
 	 */
 	private String redirectCallbackURL(HttpServletRequest request, String webUIManagerConstant)
 			throws IOException
 	{
 		String calllbackURL = (String) CacheManager.getObjectFromCache(request,
 				Constants.CALLBACK_URL);
 		if (calllbackURL != null && !calllbackURL.equals(""))
 		{
 			calllbackURL = calllbackURL + "?" + WebUIManager.getOperationStatusParameterName()
 					+ "=" + webUIManagerConstant;
 			CacheManager.clearCache(request);
 		}
 		return calllbackURL;
 	}
 }
