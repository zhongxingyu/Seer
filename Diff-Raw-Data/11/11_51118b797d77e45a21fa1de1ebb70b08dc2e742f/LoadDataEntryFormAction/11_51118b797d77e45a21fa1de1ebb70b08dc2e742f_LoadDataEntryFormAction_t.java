 
 package edu.common.dynamicextensions.ui.webui.action;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.LoadDataEntryFormProcessor;
 import edu.common.dynamicextensions.ui.webui.actionform.DataEntryForm;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.wustl.common.actionForm.AbstractActionForm;
 
 /**
  * @author sujay_narkar, chetan_patil
  *
  */
 public class LoadDataEntryFormAction extends BaseDynamicExtensionsAction
 {
 
 	/* (non-Javadoc)
 	 * @see org.apache.struts.actions.DispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		cacheCallBackURL(request);
		
 		ContainerInterface containerInterface = getConatinerInterface(request);
 
 		String recordId = request.getParameter("recordIdentifier");
 		if (recordId != null && !recordId.equals(""))
 		{
 			CacheManager.addObjectToCache(request, "rootRecordIdentifier", recordId);
 		}
 		else
 		{
 			recordId = (String) CacheManager.getObjectFromCache(request, "rootRecordIdentifier");
 			if (recordId == null)
 			{
 				recordId = "";
 			}
 		}
 
 		LoadDataEntryFormProcessor loadDataEntryFormProcessor = LoadDataEntryFormProcessor
 				.getInstance();
 		Map<AbstractAttributeInterface, Object> recordMap = loadDataEntryFormProcessor
 				.getValueMapFromRecordId(containerInterface.getEntity(), recordId);
 
 		Stack<ContainerInterface> containerStack = (Stack<ContainerInterface>) CacheManager
 				.getObjectFromCache(request, Constants.CONTAINER_STACK);
 		Stack<Map<AbstractAttributeInterface, Object>> valueMapStack = (Stack<Map<AbstractAttributeInterface, Object>>) CacheManager
 				.getObjectFromCache(request, Constants.VALUE_MAP_STACK);
 		if (containerStack == null)
 		{
 			containerStack = new Stack<ContainerInterface>();
 			CacheManager.addObjectToCache(request, Constants.CONTAINER_STACK, containerStack);
 			valueMapStack = new Stack<Map<AbstractAttributeInterface, Object>>();
 			CacheManager.addObjectToCache(request, Constants.VALUE_MAP_STACK, valueMapStack);
 			UserInterfaceiUtility.addContainerInfo(containerStack, containerInterface,
 					valueMapStack, recordMap);
 		}
 
 		DataEntryForm dataEntryForm = (DataEntryForm) form;
		
 		updateStacks(request, dataEntryForm, containerInterface, recordMap, containerStack,
 				valueMapStack);
 
 		if ((!containerStack.isEmpty()) && (!valueMapStack.isEmpty()))
 		{
 			String mode = request.getParameter(WebUIManagerConstants.MODE_PARAM_NAME);
 			if (mode == null || !mode.equals(""))
 			{
 				mode = dataEntryForm.getMode();
 			}
 
 			loadDataEntryFormProcessor.loadDataEntryForm((AbstractActionForm) form, containerStack
 					.peek(), valueMapStack.peek(), mode, recordId);
 		}
 
 		updateTopLevelEntitiyInfo(containerStack, dataEntryForm);
 
 		if (dataEntryForm.getErrorList().isEmpty())
 		{
 			clearFormValues(dataEntryForm);
 		}
 		return mapping.findForward("Success");
 	}
 
 	/**
 	 * This method returns the Container Identifier form the givaen request.
 	 * @param request HttpServletRequest
 	 * @return the Container Identifier
 	 */
 	private String getContainerId(HttpServletRequest request)
 	{
 		String id = "";
 		id = request.getParameter("containerIdentifier");
 		if (id == null || id.equals(""))
 		{
 			id = (String) request.getAttribute("containerIdentifier");
 		}
 		return id;
 	}
 
 	/**
 	 * This method flushes the values of the DataEntryForm ActionForm. 
 	 * @param form DataEntryForm ActionForm
 	 */
 	private void clearFormValues(ActionForm form)
 	{
 		DataEntryForm dataEntryForm = (DataEntryForm) form;
 		dataEntryForm.setChildRowId("");
 		dataEntryForm.setChildContainerId("");
 	}
 
 	/**
 	 * 
 	 * @param request
 	 */
 	private void cacheCallBackURL(HttpServletRequest request)
 	{
 		String callBackURL = request.getParameter(WebUIManagerConstants.CALLBACK_URL_PARAM_NAME);
 		if (callBackURL != null && !callBackURL.equals(""))
 		{
 			CacheManager.clearCache(request);
 			CacheManager.addObjectToCache(request, Constants.CALLBACK_URL, callBackURL);
 		}
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private ContainerInterface getConatinerInterface(HttpServletRequest request)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ContainerInterface containerInterface = (ContainerInterface) CacheManager
 				.getObjectFromCache(request, Constants.CONTAINER_INTERFACE);
 		String containerIdentifier = getContainerId(request);
 		if (containerIdentifier != null || containerInterface == null)
 		{
 			UserInterfaceiUtility.clearContainerStack(request);
 
 			containerInterface = DynamicExtensionsUtility
 					.getContainerByIdentifier(containerIdentifier);
 			CacheManager.addObjectToCache(request, Constants.CONTAINER_INTERFACE,
 					containerInterface);
 		}
 
 		return containerInterface;
 	}
 
 	/**
 	 * 
 	 * @param containerStack
 	 * @param dataEntryForm
 	 */
 	private void updateTopLevelEntitiyInfo(Stack<ContainerInterface> containerStack,
 			DataEntryForm dataEntryForm)
 	{
 		if (containerStack.size() > 1)
 		{
 			dataEntryForm.setIsTopLevelEntity(false);
 		}
 		else
 		{
 			dataEntryForm.setIsTopLevelEntity(true);
 		}
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param form
 	 * @param containerInterface
 	 * @param recordMap
 	 * @param containerStack
 	 * @param valueMapStack
 	 */
 	private void updateStacks(HttpServletRequest request, DataEntryForm dataEntryForm,
 			ContainerInterface containerInterface,
 			Map<AbstractAttributeInterface, Object> recordMap,
 			Stack<ContainerInterface> containerStack,
 			Stack<Map<AbstractAttributeInterface, Object>> valueMapStack)
 	{
 		String dataEntryOperation = dataEntryForm.getDataEntryOperation();
		
		
		
		if (dataEntryOperation != null && dataEntryOperation.equalsIgnoreCase("insertChildData") && (dataEntryForm.getErrorList().isEmpty()))
 		{
 			String childContainerId = dataEntryForm.getChildContainerId();
 			ContainmentAssociationControl associationControl = UserInterfaceiUtility
 					.getAssociationControl((ContainerInterface) containerStack.peek(),
 							childContainerId);
 
 			Map<AbstractAttributeInterface, Object> containerValueMap = valueMapStack.peek();
 			AssociationInterface association = (AssociationInterface) associationControl
 					.getAbstractAttribute();
 			List<Map<AbstractAttributeInterface, Object>> childContainerValueMapList = (List<Map<AbstractAttributeInterface, Object>>) containerValueMap
 					.get(association);
 
 			Map<AbstractAttributeInterface, Object> childContainerValueMap = null;
 			if (UserInterfaceiUtility.isCardinalityOneToMany(associationControl))
 			{
 				childContainerValueMap = childContainerValueMapList.get(Integer
 						.parseInt(dataEntryForm.getChildRowId()) - 1);
 			}
 			else
 			{
 				childContainerValueMap = childContainerValueMapList.get(0);
 			}
 
 			ContainerInterface childContainer = associationControl.getContainer();
 			UserInterfaceiUtility.addContainerInfo(containerStack, childContainer, valueMapStack,
 					childContainerValueMap);
 		}
 		else if (dataEntryOperation != null
 				&& dataEntryOperation.equalsIgnoreCase("insertParentData"))
 		{
 			List<String> errorList = dataEntryForm.getErrorList();
 			if (((errorList != null) && (errorList.isEmpty()))
 					&& (((containerStack != null) && !(containerStack.isEmpty())) && ((valueMapStack != null) && !(valueMapStack
 							.isEmpty()))))
 			{
 				UserInterfaceiUtility.removeContainerInfo(containerStack, valueMapStack);
 			}
 		}
 	}
 }
