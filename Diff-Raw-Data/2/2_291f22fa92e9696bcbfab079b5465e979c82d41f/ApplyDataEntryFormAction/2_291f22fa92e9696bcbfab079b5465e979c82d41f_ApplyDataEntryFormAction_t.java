 
 package edu.common.dynamicextensions.ui.webui.action;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.upload.FormFile;
 
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CheckBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ComboBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainmentAssociationControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.FileUploadInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.SelectInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextFieldInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ApplyDataEntryFormProcessor;
 import edu.common.dynamicextensions.ui.webui.actionform.DataEntryForm;
 import edu.common.dynamicextensions.ui.webui.util.CacheManager;
 import edu.common.dynamicextensions.ui.webui.util.UserInterfaceiUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManagerConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 
 /**
  * It populates the Attribute values entered in the dynamically generated controls. * 
  * @author chetan_patil
  */
 public class ApplyDataEntryFormAction extends BaseDynamicExtensionsAction
 {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.apache.struts.actions.DispatchAction#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 	{
 		ActionForward actionForward = null;
 		boolean isCallbackURL = false;
 		List<String> errorList = null;
 
 		Stack<ContainerInterface> containerStack = (Stack<ContainerInterface>) CacheManager
 				.getObjectFromCache(request, Constants.CONTAINER_STACK);
 		Stack<Map<AbstractAttributeInterface, Object>> valueMapStack = (Stack<Map<AbstractAttributeInterface, Object>>) CacheManager
 				.getObjectFromCache(request, Constants.VALUE_MAP_STACK);
 		if ((containerStack != null && !containerStack.isEmpty())
 				&& (valueMapStack != null || !valueMapStack.isEmpty()))
 		{
 			try
 			{
 				DataEntryForm dataEntryForm = (DataEntryForm) form;
 				String mode = dataEntryForm.getMode();
 				if ((mode != null) && (mode.equals("edit")))
 				{
 					populateAndValidateValues(containerStack, valueMapStack, request, dataEntryForm);
 					errorList = dataEntryForm.getErrorList();
 				}
 
 				actionForward = getMappingForwardAction(mapping, dataEntryForm, errorList, mode);
 				if ((actionForward != null && actionForward.getName().equals(
 						"showDynamicExtensionsHomePage"))
 						&& (mode != null && mode.equals("cancel")))
 				{
 					String recordIdentifier = dataEntryForm.getRecordIdentifier();
 					isCallbackURL = redirectCallbackURL(request, response, recordIdentifier);
 				}
				else if (actionForward == null && errorList!=null && errorList.isEmpty())
 				{
 					String recordIdentifier = dataEntryForm.getRecordIdentifier();
 					recordIdentifier = storeParentContainer(valueMapStack, containerStack, request,
 							recordIdentifier);
 					isCallbackURL = redirectCallbackURL(request, response, recordIdentifier);
 				}
 			}
 			catch (Exception exception)
 			{
 				exception.printStackTrace();
 				return getExceptionActionForward(exception, mapping, request);
 			}
 		}
 
 		if (isCallbackURL)
 		{
 			actionForward = null;
 		}
 		else if (actionForward == null)
 		{
 			if (errorList.isEmpty())
 			{
 				UserInterfaceiUtility.clearContainerStack(request);
 			}
 			actionForward = mapping.findForward(Constants.SUCCESS);
 		}
 		return actionForward;
 	}
 
 	/**
 	 * This method gets the Callback URL from cahce, reforms it and redirect the response to it. 
 	 * @param request HttpServletRequest to obtain session
 	 * @param response HttpServletResponse to redirect the CallbackURL
 	 * @param recordIdentifier Identifier of the record to reconstruct the CallbackURL
 	 * @return true if CallbackURL is redirected, false otherwise
 	 * @throws IOException
 	 */
 	private boolean redirectCallbackURL(HttpServletRequest request, HttpServletResponse response,
 			String recordIdentifier) throws IOException
 	{
 		boolean isCallbackURL = false;
 		String calllbackURL = (String) CacheManager.getObjectFromCache(request,
 				Constants.CALLBACK_URL);
 		if (calllbackURL != null && !calllbackURL.equals(""))
 		{
 			calllbackURL = calllbackURL + "?" + WebUIManager.getRecordIdentifierParameterName()
 					+ "=" + recordIdentifier + "&" + WebUIManager.getOperationStatusParameterName()
 					+ "=" + WebUIManagerConstants.SUCCESS;
 			CacheManager.clearCache(request);
 			response.sendRedirect(calllbackURL);
 			isCallbackURL = true;
 		}
 		return isCallbackURL;
 	}
 
 	/**
 	 * This method gets the ActionForwad on the Exception.
 	 * @param exception Exception instance
 	 * @param mapping ActionMapping to get ActionForward
 	 * @param request HttpServletRequest to save error messages in.
 	 * @return Appropriate ActionForward.
 	 */
 	private ActionForward getExceptionActionForward(Exception exception, ActionMapping mapping,
 			HttpServletRequest request)
 	{
 		ActionForward exceptionActionForward = null;
 		String actionForwardString = catchException(exception, request);
 		if ((actionForwardString == null) || (actionForwardString.equals("")))
 		{
 			exceptionActionForward = mapping.getInputForward();
 		}
 		else
 		{
 			exceptionActionForward = mapping.findForward(actionForwardString);
 		}
 		return exceptionActionForward;
 	}
 
 	/**
 	 * This method sets dataentry operations parameters and returns the appropriate 
 	 * ActionForward depending on the "mode" of the operation and validation errors.
 	 * @param mapping ActionMapping to get the ActionForward
 	 * @param dataEntryForm ActionForm
 	 * @param errorList List of validation error messages generated.
 	 * @param mode Mode of the operation viz., edit, view, cancel
 	 * @return ActionForward
 	 */
 	private ActionForward getMappingForwardAction(ActionMapping mapping,
 			DataEntryForm dataEntryForm, List<String> errorList, String mode)
 	{
 		ActionForward actionForward = null;
 		String dataEntryOperation = dataEntryForm.getDataEntryOperation();
 		if (dataEntryOperation != null)
 		{
 			if (errorList == null)
 			{
 				dataEntryForm.setErrorList(new ArrayList<String>());
 			}
 
 			if (dataEntryOperation.equals("insertChildData"))
 			{
 				if ((errorList != null) && !(errorList.isEmpty()))
 				{
 					dataEntryForm.setDataEntryOperation("insertParentData");
 					actionForward = mapping.findForward("loadParentContainer");
 				}
 				else if ((mode != null) && (mode.equals("cancel")))
 				{
 					dataEntryForm.setMode("edit");
 					dataEntryForm.setDataEntryOperation("insertParentData");
 					actionForward = mapping.findForward("loadParentContainer");
 				}
 				else
 				{
 					actionForward = mapping.findForward("loadChildContainer");
 				}
 			}
 			else if (dataEntryOperation.equals("insertParentData"))
 			{
 				if ((errorList != null) && !(errorList.isEmpty()))
 				{
 					dataEntryForm.setDataEntryOperation("insertChildData");
 					actionForward = mapping.findForward("loadChildContainer");
 				}
 				else if ((mode != null) && (mode.equals("cancel")))
 				{
 					actionForward = mapping.findForward("showDynamicExtensionsHomePage");
 				}
 				else
 				{
 					actionForward = mapping.findForward("loadParentContainer");
 				}
 			}
 		}
 		return actionForward;
 	}
 
 	/**
 	 * This method returns messages on successful saving of an Entity
 	 * @return ActionMessages ActionMessages
 	 */
 	private ActionMessages getMessageString(String messageKey)
 	{
 		ActionMessages actionMessages = new ActionMessages();
 		actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(messageKey));
 		return actionMessages;
 	}
 
 	/**
 	 * This method gathers the values form the Dynamic UI and validate them using Validation framework 
 	 * @param containerStack Stack of Container which has the current Container at its top.
 	 * @param valueMapStack Stack of Map of Attribute-Value pair which has Map for current Container at its top.  
 	 * @param request HttpServletRequest which is required to collect the values from UI form.
 	 * @param dataEntryForm 
 	 * @param errorList List to store the validation error/warning messages which will be displayed on the UI.
 	 * @throws FileNotFoundException if improper value is entered for FileUpload control.
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws IOException
 	 */
 	private void populateAndValidateValues(Stack<ContainerInterface> containerStack,
 			Stack<Map<AbstractAttributeInterface, Object>> valueMapStack,
 			HttpServletRequest request, DataEntryForm dataEntryForm) throws FileNotFoundException,
 			DynamicExtensionsSystemException, IOException
 	{
 		ContainerInterface containerInterface = (ContainerInterface) containerStack.peek();
 		Map<AbstractAttributeInterface, Object> valueMap = (Map<AbstractAttributeInterface, Object>) valueMapStack
 				.peek();
 		valueMap = generateAttributeValueMap(containerInterface, request, dataEntryForm, "",
 				valueMap, true);
 
 		//List<String> errorList = ValidatorUtil.validateEntity(valueMap);
 
 		List<String> errorList = new ArrayList<String>();
 		//saveErrors(request, getErrorMessages(errorList));
 		dataEntryForm.setErrorList(errorList);
 	}
 
 	/**
 	 * 
 	 * @param container
 	 * @param request
 	 * @param dataEntryForm
 	 * @return
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	private Map<AbstractAttributeInterface, Object> generateAttributeValueMap(
 			ContainerInterface containerInterface, HttpServletRequest request,
 			DataEntryForm dataEntryForm, String rowId,
 			Map<AbstractAttributeInterface, Object> attributeValueMap, Boolean processOneToMany)
 			throws FileNotFoundException, IOException, DynamicExtensionsSystemException
 	{
 		//Collection<ControlInterface> controlCollection = containerInterface.getControlCollection();
 
 		Collection<ControlInterface> controlCollection = containerInterface.getAllControls();
 		for (ControlInterface control : controlCollection)
 		{
 			if (control != null)
 			{
 				Integer controlSequenceNumber = control.getSequenceNumber();
 				if (controlSequenceNumber != null)
 				{
 					String controlSequence = control.getParentContainer().getIncontextContainer()
 							.getId()
 							+ "_"
 							+ control.getParentContainer().getId()
 							+ "_"
 							+ controlSequenceNumber;
 
 					if (rowId != null && !rowId.equals(""))
 					{
 						controlSequence = controlSequence + "_" + rowId;
 					}
 
 					AbstractAttributeInterface abstractAttribute = control.getAbstractAttribute();
 					if (abstractAttribute instanceof AttributeInterface)
 					{
 						collectAttributeValues(request, dataEntryForm, controlSequence, control,
 								attributeValueMap);
 					}
 					else if (abstractAttribute instanceof AssociationInterface)
 					{
 						collectAssociationValues(request, dataEntryForm, controlSequence, control,
 								attributeValueMap, processOneToMany);
 					}
 				}
 			}
 		}
 
 		return attributeValueMap;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param dataEntryForm
 	 * @param sequence
 	 * @param control
 	 * @param attributeValueMap
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws IOException 
 	 * @throws FileNotFoundException 
 	 */
 	private void collectAssociationValues(HttpServletRequest request, DataEntryForm dataEntryForm,
 			String sequence, ControlInterface control,
 			Map<AbstractAttributeInterface, Object> attributeValueMap, Boolean processOneToMany)
 			throws DynamicExtensionsSystemException, FileNotFoundException, IOException
 	{
 		AbstractAttributeInterface abstractAttribute = control.getAbstractAttribute();
 		List<Map<AbstractAttributeInterface, Object>> associationValueMapList = (List<Map<AbstractAttributeInterface, Object>>) attributeValueMap
 				.get(abstractAttribute);
 
 		if (associationValueMapList == null)
 		{
 			associationValueMapList = new ArrayList<Map<AbstractAttributeInterface, Object>>();
 		}
 
 		if (control instanceof ContainmentAssociationControlInterface && processOneToMany)
 		{
 			ContainmentAssociationControlInterface containmentAssociationControlInterface = (ContainmentAssociationControlInterface) control;
 			ContainerInterface targetContainer = ((ContainmentAssociationControlInterface) control)
 					.getContainer();
 			if (containmentAssociationControlInterface.isCardinalityOneToMany())
 			{
 				associationValueMapList = collectOneToManyContainmentValues(request, dataEntryForm,
 						targetContainer.getId().toString(), control, associationValueMapList);
 			}
 			else
 			{
 				Map<AbstractAttributeInterface, Object> oneToOneValueMap = null;
 
 				if (associationValueMapList.size() > 0 && associationValueMapList.get(0) != null)
 				{
 					oneToOneValueMap = associationValueMapList.get(0);
 				}
 				else
 				{
 					oneToOneValueMap = new HashMap<AbstractAttributeInterface, Object>();
 					associationValueMapList.add(oneToOneValueMap);
 				}
 
 				generateAttributeValueMap(targetContainer, request, dataEntryForm, "",
 						oneToOneValueMap, false);
 			}
 
 			attributeValueMap.put(abstractAttribute, associationValueMapList);
 		}
 		else if (control instanceof SelectInterface)
 		{
 			List<Long> valueList = new ArrayList<Long>();
 			if (control instanceof ListBoxInterface)
 			{
 				String[] selectedValues = (String[]) request.getParameterValues("Control_"
 						+ sequence);
 				if (selectedValues != null)
 				{
 					for (String id : selectedValues)
 					{
 						Long identifier = new Long(id.trim());
 						valueList.add(identifier);
 					}
 				}
 			}
 			else if (control instanceof ComboBoxInterface)
 			{
 				String selectedValue = request.getParameter("Control_" + sequence);
 				if (selectedValue != null)
 				{
 					valueList.add(new Long(selectedValue.trim()));
 				}
 			}
 			if (!valueList.isEmpty())
 			{
 				attributeValueMap.put(abstractAttribute, valueList);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param dataEntryForm
 	 * @param sequence
 	 * @param control
 	 * @param attributeValueMap
 	 * @throws IOException 
 	 * @throws DynamicExtensionsSystemException 
 	 * @throws FileNotFoundException 
 	 */
 	private List<Map<AbstractAttributeInterface, Object>> collectOneToManyContainmentValues(
 			HttpServletRequest request, DataEntryForm dataEntryForm, String containerId,
 			ControlInterface control,
 			List<Map<AbstractAttributeInterface, Object>> oneToManyContainmentValueList)
 			throws FileNotFoundException, DynamicExtensionsSystemException, IOException
 	{
 		ContainmentAssociationControl containmentAssociationControl = (ContainmentAssociationControl) control;
 		int currentSize = oneToManyContainmentValueList.size();
 
 		String parameterString = containerId + "_rowCount";
 		String rowCountString = request.getParameter(parameterString);
 		int rowCount = Integer.parseInt(rowCountString);
 
 		for (int counter = 0; counter < rowCount; counter++)
 		{
 			Map<AbstractAttributeInterface, Object> attributeValueMapForSingleRow = null;
 
 			String counterStr = String.valueOf(counter + 1);
 			if (counter < currentSize)
 			{
 				attributeValueMapForSingleRow = oneToManyContainmentValueList.get(counter);
 			}
 			else
 			{
 				attributeValueMapForSingleRow = new HashMap<AbstractAttributeInterface, Object>();
 				oneToManyContainmentValueList.add(attributeValueMapForSingleRow);
 			}
 			generateAttributeValueMap(containmentAssociationControl.getContainer(), request,
 					dataEntryForm, counterStr, attributeValueMapForSingleRow, false);
 		}
 
 		return oneToManyContainmentValueList;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param dataEntryForm
 	 * @param sequence
 	 * @param control
 	 * @param attributeValueMap
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void collectAttributeValues(HttpServletRequest request, DataEntryForm dataEntryForm,
 			String sequence, ControlInterface control,
 			Map<AbstractAttributeInterface, Object> attributeValueMap)
 			throws FileNotFoundException, IOException
 	{
 		AbstractAttributeInterface abstractAttribute = control.getAbstractAttribute();
 		Object attributeValue = null;
 
 		if (control instanceof ListBoxInterface)
 		{
 			List<String> valueList = new ArrayList<String>();
 			String[] selectedListValues = (String[]) request.getParameterValues("Control_"
 					+ sequence);
 
 			if (selectedListValues != null)
 			{
 				for (int counter = 0; counter < selectedListValues.length; counter++)
 				{
 					valueList.add(selectedListValues[counter]);
 				}
 			}
 			attributeValue = valueList;
 			attributeValueMap.put(abstractAttribute, attributeValue);
 		}
 		else if (control instanceof FileUploadInterface)
 		{
 			FormFile formFile = null;
 			formFile = (FormFile) dataEntryForm.getValue("Control_" + sequence);
 			if (formFile.getFileName() != null && !formFile.getFileName().equals(""))
 			{
 				FileAttributeRecordValue fileAttributeRecordValue = new FileAttributeRecordValue();
 				fileAttributeRecordValue.setFileContent(formFile.getFileData());
 				fileAttributeRecordValue.setFileName(formFile.getFileName());
 				fileAttributeRecordValue.setContentType(formFile.getContentType());
 				attributeValue = fileAttributeRecordValue;
 				attributeValueMap.put(abstractAttribute, attributeValue);
 			}
 
 		}
 		else
 		{
 			String value = request.getParameter("Control_" + sequence);
 			if (control instanceof CheckBoxInterface)
 			{
 				if (value == null || !value.trim().equals("checked"))
 				{
 					value = "unchecked";
 				}
 			}
 			else if (control instanceof TextFieldInterface)
 			{
 				AttributeTypeInformationInterface attributeTypeInformationInterface = ((AttributeInterface) abstractAttribute)
 						.getAttributeTypeInformation();
 				if (attributeTypeInformationInterface instanceof DoubleAttributeTypeInformation)
 				{
 					value = rectifyNumberPrecision(
 							(DoubleAttributeTypeInformation) attributeTypeInformationInterface,
 							value);
 				}
 			}
 			attributeValue = value;
 			attributeValueMap.put(abstractAttribute, attributeValue);
 		}
 
 	}
 
 	/**
 	 * This method returns the String representaion of a number as per the given format of the corresponding Number Control.
 	 * @param doubleAttributeTypeInformation
 	 * @param value
 	 * @return
 	 */
 	private String rectifyNumberPrecision(
 			DoubleAttributeTypeInformation doubleAttributeTypeInformation, String value)
 	{
 		if (DynamicExtensionsUtility.isNumeric(value) && (value.indexOf(".") != -1))
 		{
 			Integer decimalPlaces = doubleAttributeTypeInformation.getDecimalPlaces();
 			StringBuffer decimalFormat = new StringBuffer("#");
 			if (decimalPlaces > 0)
 			{
 				decimalFormat.append(".");
 				while (decimalPlaces > 0)
 				{
 					decimalFormat.append("#");
 					decimalPlaces--;
 				}
 			}
 
 			NumberFormat numberFormat = new DecimalFormat(decimalFormat.toString());
 			value = numberFormat.format(Double.parseDouble(value));
 		}
 		return value;
 	}
 
 	/**
 	 * This method stores the container in the database. It updates the existing record or inserts a new record 
 	 * depending upon the availability of the record identifier variable.
 	 * @param valueMapStack Stack storing the Map of Attributes and their corresponding values. 
 	 * @param containerStack Stack having Container at its top that is to be stored in database.
 	 * @param request HttpServletRequest to store the operation message.
 	 * @param recordIdentifier Identifier of the record in database that is to be updated.
 	 * @return New identifier for a record if record is inserted otherwise the passed record identifier is returned. 
 	 * @throws NumberFormatException If record identifier is not a numeric value.
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String storeParentContainer(
 			Stack<Map<AbstractAttributeInterface, Object>> valueMapStack,
 			Stack<ContainerInterface> containerStack, HttpServletRequest request,
 			String recordIdentifier) throws NumberFormatException,
 			DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Map<AbstractAttributeInterface, Object> rootValueMap = (Map<AbstractAttributeInterface, Object>) valueMapStack
 				.firstElement();
 		ContainerInterface rootContainerInterface = (ContainerInterface) containerStack
 				.firstElement();
 		ApplyDataEntryFormProcessor applyDataEntryFormProcessor = ApplyDataEntryFormProcessor
 				.getInstance();
 
 		if ((rootValueMap == null) || rootValueMap.isEmpty())
 		{
 			saveMessages(request, getMessageString("app.noDataMessage"));
 		}
 		else
 		{
 			String messageKey = "app.successfulDataInsertionMessage";
 			if (recordIdentifier != null && !recordIdentifier.equals(""))
 			{
 				Boolean edited = applyDataEntryFormProcessor.editDataEntryForm(
 						rootContainerInterface, rootValueMap, Long.valueOf(recordIdentifier));
 				if (edited.booleanValue())
 				{
 					saveMessages(request, getMessageString(messageKey));
 				}
 			}
 			else
 			{
 				recordIdentifier = applyDataEntryFormProcessor.insertDataEntryForm(
 						rootContainerInterface, rootValueMap);
 				saveMessages(request, getMessageString(messageKey));
 			}
 		}
 		return recordIdentifier;
 	}
 
 }
