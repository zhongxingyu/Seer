 
 package edu.common.dynamicextensions.processor;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.interfaces.AbstractAttributeUIBeanInterface;
 import edu.common.dynamicextensions.ui.interfaces.ControlUIBeanInterface;
 import edu.common.dynamicextensions.ui.util.ControlsUtility;
 import edu.common.dynamicextensions.ui.webui.util.WebUIManager;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 
 /**
  * @author preeti_munot
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class ApplyFormControlsProcessor extends BaseDynamicExtensionsProcessor
 {
 	/**
 	 * Constructor
 	 */
 	protected ApplyFormControlsProcessor()
 	{
 
 	}
 
 	/**
 	 * this method gets the new instance of the ControlProcessor to the caller.
 	 * @return ControlProcessor ControlProcessor instance
 	 */
 	public static ApplyFormControlsProcessor getInstance()
 	{
 		return new ApplyFormControlsProcessor();
 	}
 
 	/**
 	 * @param container : Container object
 	 * @param controlsForm : UI Information as form object
 	 * @throws DynamicExtensionsSystemException dynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException DynamicExtensionsApplicationException
 	 */
 	public void addControlToForm(ContainerInterface container, ControlUIBeanInterface controlUIBean, AbstractAttributeUIBeanInterface attrUIBean,
 			EntityGroupInterface entityGroup) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if ((container != null) && (controlUIBean != null) && (attrUIBean != null))
 		{
 			EntityInterface entity = (EntityInterface) container.getAbstractEntity();
 			ControlProcessor controlProcessor = ControlProcessor.getInstance();
 			AttributeProcessor attributeProcessor = AttributeProcessor.getInstance();
 			AbstractAttributeInterface abstractAttribute = null;
 			ControlInterface control = null;
 
 			//Check for operation
 			String controlOperation = controlUIBean.getControlOperation();
 
 			//Default is add
 			if ((controlOperation == null) || (controlOperation.trim().equals("")))
 			{
 				controlOperation = ProcessorConstants.OPERATION_ADD;
 			}

			if ((attrUIBean.getDataType() != null) && (attrUIBean.getDataType().equals(ProcessorConstants.DATATYPE_NUMBER)))
 			{
 				initializeMeasurementUnits(attrUIBean);
 			}
 			ControlsUtility.reinitializeSequenceNumbers(container.getControlCollection(), controlUIBean.getControlsSequenceNumbers());
 
 			//Add new control
 			if (controlOperation.equalsIgnoreCase(ProcessorConstants.OPERATION_ADD))
 			{
 				//Set Name of the attribute in controlsForm.
 				//It is not accepted from UI. It has to be derived from caption
 				String attributeName = deriveAttributeNameFromCaption(controlUIBean.getCaption());
 
 				//Validate attribute name
 				DynamicExtensionsUtility.validateName(attributeName);
 				DynamicExtensionsUtility.validateDuplicateNamesWithinEntity(entity, attributeName);
 				controlUIBean.setName(attributeName);
 
 				//Create Attribute  
 				abstractAttribute = attributeProcessor.createAndPopulateAttribute(controlUIBean.getUserSelectedTool(), attrUIBean, entityGroup);
 
 				//Set permissible values
 				setPermissibleValues(attributeProcessor, abstractAttribute, controlUIBean, attrUIBean);
 
 				//Set attribute in controlInformationInterface object(controlsForm)
 				controlUIBean.setAbstractAttribute(abstractAttribute);
 
 				//Control Interface : Add control
 				control = controlProcessor.createAndPopulateControl(controlUIBean.getUserSelectedTool(), controlUIBean, entityGroup);
 				control.setSequenceNumber(WebUIManager.getSequenceNumberForNextControl(container));
 
 				//Entity Interface  : Add attribute
 				if ((entity != null) && (abstractAttribute != null))
 				{
 					entity.addAbstractAttribute(abstractAttribute);
 					abstractAttribute.setEntity(entity);
 				}
 				//DynamicExtensionsUtility.updateEntityReferences(abstractAttributeInterface);
 
 				//Container : Add control and entity
 				container.addControl(control);
 				container.setAbstractEntity(entity);
 				//entityInterface.setContainer((Container) containerInterface);
 			}
 			else if (controlOperation.equalsIgnoreCase(ProcessorConstants.OPERATION_EDIT))
 			{
 				//Set Name of the attribute in controlsForm.
 				//It is not accepted from UI. It has to be derived from caption
 				String attributeName = deriveAttributeNameFromCaption(controlUIBean.getCaption());
 				controlUIBean.setName(attributeName);
 				//Get the control from container
 				String selectedControlSeqNumber = controlUIBean.getSelectedControlId();
 				control = container.getControlInterfaceBySequenceNumber(selectedControlSeqNumber);
 
 				/*
 				 * CODE COMMENTED BY PREETI : 24 Nov 2006
 				 * The attribute reference need not be removed. Its type information needs to be updated as per the 
 				 * new design 
 				 * 
 				 * //Remove old references : From Entity
 				 entityInterface.removeAbstractAttribute(controlInterface.getAbstractAttribute());
 
 				 //Create new abstract attribute interface with new datatype
 				 abstractAttributeInterface = attributeProcessor.createAndPopulateAttribute(controlsForm);
 
 				 //update in entity
 				 entityInterface.addAbstractAttribute(abstractAttributeInterface);
 				 */
 
 				//***********New Code starts here*********
 				abstractAttribute = (AbstractAttributeInterface) control.getBaseAbstractAttribute();
 				abstractAttribute = attributeProcessor.updateAttributeInformation(controlUIBean.getUserSelectedTool(), abstractAttribute, attrUIBean, entityGroup);
 				setPermissibleValues(attributeProcessor, abstractAttribute, controlUIBean, attrUIBean);
 
 				//update in control interface
 				control.setBaseAbstractAttribute(abstractAttribute);
 				//abstractAttributeInterface.setControl((Control) controlInterface);
 
 				controlUIBean.setAbstractAttribute(abstractAttribute);
 
 				String oldControlType = DynamicExtensionsUtility.getControlName(control);
 				String newControlType = controlUIBean.getUserSelectedTool();
 				ControlInterface newControl = null;
 
 				if (!oldControlType.equals(newControlType))
 				{
 					newControl = controlProcessor.createAndPopulateControl(newControlType, controlUIBean, entityGroup);
 				}
 				else
 				{
 					newControl = controlProcessor.populateControlInterface(controlUIBean.getUserSelectedTool(), control, controlUIBean, entityGroup);
 				}
 
 				//update control
 				//If new control interface is same as old one, do nothing. Else remove old references from container and add new one
 				if ((newControl != null) && (!newControl.equals(control)))
 				{
 					//Remove control from container
 					container.removeControl(control);
 
 					//Set Sequence number
 					newControl.setSequenceNumber(control.getSequenceNumber());
 
 					//Set abstract attribute
 					newControl.setBaseAbstractAttribute(abstractAttribute);
 					//abstractAttributeInterface.setControl((Control) newControlInterface);
 					//add to container
 					container.addControl(newControl);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param abstractAttribute
 	 * @param controlsForm
 	 * @throws DynamicExtensionsApplicationException :Exception
 	 */
 	private void setPermissibleValues(AttributeProcessor attributeProcessor, AbstractAttributeInterface abstractAttribute,
 			ControlUIBeanInterface controlUIBean, AbstractAttributeUIBeanInterface attributeUIBean) throws DynamicExtensionsApplicationException
 	{
 		//if combobox/optionbutton control has been selected then set the DataElement object for set of permissible values
 		String userSelectedControl = controlUIBean.getUserSelectedTool();
 		if (userSelectedControl != null)
 		{
 			if ((userSelectedControl.equalsIgnoreCase(ProcessorConstants.COMBOBOX_CONTROL))
 					|| (userSelectedControl.equalsIgnoreCase(ProcessorConstants.RADIOBUTTON_CONTROL)))
 			{
 				AttributeTypeInformationInterface attributeTypeInformation = DynamicExtensionsUtility.getAttributeTypeInformation(abstractAttribute);
 				if (attributeTypeInformation != null)
 				{
 					attributeTypeInformation.setDataElement(attributeProcessor.getDataElementInterface(attributeUIBean));
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param caption :Caption of the attribute
 	 * @return : Name of the attribute derived from the caption. 
 	 * The name has all spaces removed and first letter of every word capitalized
 	 */
 	private String deriveAttributeNameFromCaption(String caption)
 	{
 		String attributeName = "";
 		if (caption != null)
 		{
 			String[] wordsInCaption = caption.split(" ");
 			String word = null;
 			if (wordsInCaption != null)
 			{
 				int noOfWords = wordsInCaption.length;
 				for (int i = 0; i < noOfWords; i++)
 				{
 					word = wordsInCaption[i];
 					if ((word != null) && (!word.trim().equalsIgnoreCase("")))
 					{
 						attributeName = attributeName + word;
 					}
 				}
 			}
 		}
 
 		return attributeName;
 	}
 
 	/**
 	 * This method initializes MeasurementUnits needed for the actionForm
 	 * @param controlsForm actionForm
 	 */
 	private void initializeMeasurementUnits(AbstractAttributeUIBeanInterface attrUIBean)
 	{
 		//Handle special case of measurement units
 		//If measurement unit is other, value of measurement unit is value of txtMeasurementUnit.
 		if ((attrUIBean.getAttributeMeasurementUnits() != null)
 				&& (attrUIBean.getAttributeMeasurementUnits().equalsIgnoreCase(ProcessorConstants.MEASUREMENT_UNIT_OTHER)))
 		{
 			attrUIBean.setAttributeMeasurementUnits(attrUIBean.getMeasurementUnitOther());
 		}
 	}
 }
