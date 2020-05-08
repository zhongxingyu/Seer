 /**
  *
  */
 
 package edu.common.dynamicextensions.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import edu.common.dynamicextensions.bizlogic.BizLogicFactory;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.userinterface.AbstractContainmentControl;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domain.userinterface.ContainmentAssociationControl;
 import edu.common.dynamicextensions.domain.userinterface.Control;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.CheckBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ComboBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.DatePickerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.FileUploadInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.RadioButtonInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextAreaInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextFieldInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerExceptionConstantsInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.common.dynamicextensions.util.global.Constants.InheritanceStrategy;
 import edu.common.dynamicextensions.validation.RangeValidator;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.bizlogic.AbstractBizLogic;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.util.CVSTagReader;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author chetan_patil
  *
  */
 public class DynamicExtensionsUtility
 {
 
 	/**
 	 * This method fetches the Control instance from the Database given the corresponding Control Identifier.
 	 * @param controlIdentifier The Idetifier of the Control.
 	 * @return the ControlInterface
 	 * @throws DynamicExtensionsSystemException on System exception
 	 * @throws DynamicExtensionsApplicationException on Application exception
 	 */
 	public static ControlInterface getControlByIdentifier(String controlIdentifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ControlInterface controlInterface = null;
 		controlInterface = (ControlInterface) getObjectByIdentifier(ControlInterface.class.getName(), controlIdentifier);
 		return controlInterface;
 	}
 
 	/**
 	 * This method fetches the Container instance from the Database given the corresponding Container Identifier.
 	 * @param containerIdentifier The Idetifier of the Container.
 	 * @return the ContainerInterface
 	 * @throws DynamicExtensionsSystemException on System exception
 	 * @throws DynamicExtensionsApplicationException on Application exception
 	 */
 	public static EntityGroupInterface getEntityGroupByIdentifier(String entityGroupIdentifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		EntityGroupInterface entityGroupInterface = null;
 		entityGroupInterface = (EntityGroupInterface) getObjectByIdentifier(EntityGroupInterface.class.getName(), entityGroupIdentifier);
 		return entityGroupInterface;
 	}
 
 	/**
 	 * This method fetches the Container instance from the Database given the corresponding Container Identifier.
 	 * @param containerIdentifier The Idetifier of the Container.
 	 * @return the ContainerInterface
 	 * @throws DynamicExtensionsSystemException on System exception
 	 * @throws DynamicExtensionsApplicationException on Application exception
 	 */
 	public static ContainerInterface getContainerByIdentifier(String containerIdentifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		ContainerInterface containerInterface = null;
 		containerInterface = (ContainerInterface) getObjectByIdentifier(ContainerInterface.class.getName(), containerIdentifier);
 		return containerInterface;
 	}
 
 	/**
 	 * This method fetches the Container instance from the Database given the corresponding Container Identifier.
 	 * @param containerIdentifier The Idetifier of the Container.
 	 * @return the ContainerInterface
 	 * @throws DynamicExtensionsSystemException on System exception
 	 * @throws DynamicExtensionsApplicationException on Application exception
 	 */
 	public static AttributeInterface getAttributeByIdentifier(String attributeIdentifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AttributeInterface attributeInterface = null;
 		attributeInterface = (AttributeInterface) getObjectByIdentifier(AttributeInterface.class.getName(), attributeIdentifier);
 		return attributeInterface;
 	}
 
 	/**
 	 * This method returns object for a given class name and identifer
 	 * @param objectName  name of the class of the object
 	 * @param identifier identifier of the object
 	 * @return  obejct
 	 * @throws DynamicExtensionsSystemException on System exception
 	 * @throws DynamicExtensionsApplicationException on Application exception
 	 */
 	private static Object getObjectByIdentifier(String objectName, String identifier) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		AbstractBizLogic bizLogic = BizLogicFactory.getDefaultBizLogic();
 		Object object = null;
 		if (objectName == null || identifier == null || identifier.trim().length() == 0)
 		{
 			return null;
 		}
 		try
 		{
 			//			After moving to MYSQL 5.2 the type checking is strict so changing the identifier to Long
 			List objectList = bizLogic.retrieve(objectName, Constants.ID, new Long(identifier));
 
 			if (objectList == null || objectList.isEmpty())
 			{
 				throw new DynamicExtensionsSystemException("OBJECT_NOT_FOUND");
 			}
 
 			object = objectList.get(0);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 		return object;
 	}
 
 	/**
 	 * This method clears data value for all controls within container
 	 * @param baseContainer
 	 */
 	public static void cleanContainerControlsValue(ContainerInterface baseContainer)
 	{
 
 		while (baseContainer != null)
 		{
 			Collection controlCollection = baseContainer.getControlCollection();
 
 			for (Iterator iterator = controlCollection.iterator(); iterator.hasNext();)
 			{
 				Control objControl = (Control) iterator.next();
 				if (objControl instanceof AbstractContainmentControl)
 				{
 					ContainerInterface subContainer = ((AbstractContainmentControl) objControl).getContainer();
 					if (subContainer != null)
 					{
 						subContainer.getContainerValueMap().clear();
 						cleanContainerControlsValue(subContainer);
 					}
 
 				}
 				objControl.setValue(null);
 			}
 			baseContainer = baseContainer.getBaseContainer();
 		}
 
 	}
 
 	/**
 	 * @param controlInterface ControlInterface
 	 * @return String ControlName
 	 */
 	public static String getControlName(ControlInterface controlInterface)
 	{
 		if (controlInterface != null)
 		{
 			if (controlInterface instanceof TextFieldInterface)
 			{
 				return ProcessorConstants.TEXT_CONTROL;
 			}
 			else if (controlInterface instanceof ComboBoxInterface)
 			{
 				return ProcessorConstants.COMBOBOX_CONTROL;
 			}
 			else if (controlInterface instanceof ListBoxInterface)
 			{
 				return ProcessorConstants.COMBOBOX_CONTROL;
 			}
 			else if (controlInterface instanceof DatePickerInterface)
 			{
 				return ProcessorConstants.DATEPICKER_CONTROL;
 			}
 			else if (controlInterface instanceof TextAreaInterface)
 			{
 				return ProcessorConstants.TEXT_CONTROL;
 			}
 			else if (controlInterface instanceof RadioButtonInterface)
 			{
 				return ProcessorConstants.RADIOBUTTON_CONTROL;
 			}
 			else if (controlInterface instanceof CheckBoxInterface)
 			{
 				return ProcessorConstants.CHECKBOX_CONTROL;
 			}
 			else if (controlInterface instanceof FileUploadInterface)
 			{
 				return ProcessorConstants.FILEUPLOAD_CONTROL;
 			}
 			if (controlInterface instanceof AbstractContainmentControl)
 			{
 				return ProcessorConstants.ADD_SUBFORM_CONTROL;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 *
 	 * @param controlCollectio
 	 * @param sequenceNumber
 	 * @return
 	 */
 	public static ControlInterface getControlBySequenceNumber(Collection controlCollection, int sequenceNumber)
 	{
 		ControlInterface controlInterface = null;
 		if (controlCollection != null)
 		{
 			Iterator controlIterator = controlCollection.iterator();
 			while (controlIterator.hasNext())
 			{
 				controlInterface = (ControlInterface) controlIterator.next();
 				if (controlInterface.getSequenceNumber() != null && controlInterface.getSequenceNumber() == sequenceNumber
 				/*&& !controlInterface.getSequenceNumberChanged()*/)
 				{
 					controlInterface.setSequenceNumberChanged(true);
 					return controlInterface;
 				}
 			}
 		}
 		return null;
 	}
 
 	public static ControlInterface getControlBySequenceNumber(ControlInterface[] controlCollection, int sequenceNumber)
 	{
 		ControlInterface controlInterface = null;
 		if (controlCollection != null)
 		{
 			int noOfControls = controlCollection.length;
 			for (int i = 0; i < noOfControls; i++)
 			{
 				controlInterface = controlCollection[i];
 				if (controlInterface.getSequenceNumber() != null && controlInterface.getSequenceNumber() == sequenceNumber)
 				{
 					controlInterface.setSequenceNumberChanged(true);
 					return controlInterface;
 				}
 				else
 				{
 					controlInterface = null;
 				}
 			}
 		}
 		return controlInterface;
 	}
 
 	/**
 	 *
 	 */
 	public static void initialiseApplicationVariables()
 	{
 		try
 		{
 			DBUtil.currentSession();
 			//			DBUtil.closeSession();
 		}
 		catch (Exception e)
 		{
 			throw new RuntimeException(e);
 		}
 
 		if (Logger.out == null)
 		{
 			Logger.configure("");
 		}
 
 		if (!Variables.databaseName.equals(Constants.MYSQL_DATABASE))
 		{
 			//set string/function for oracle
 
 			Variables.datePattern = "mm-dd-yyyy";
 			Variables.timePattern = "hh24-mi-ss";
 			Variables.dateFormatFunction = "TO_CHAR";
 			Variables.timeFormatFunction = "TO_CHAR";
 			Variables.dateTostrFunction = "TO_CHAR";
 			Variables.strTodateFunction = "TO_DATE";
 		}
 		else
 		{
 			Variables.datePattern = "%m-%d-%Y";
 			Variables.timePattern = "%H:%i:%s";
 			Variables.dateFormatFunction = "DATE_FORMAT";
 			Variables.timeFormatFunction = "TIME_FORMAT";
 			Variables.dateTostrFunction = "TO_CHAR";
 			Variables.strTodateFunction = "STR_TO_DATE";
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static void initialiseApplicationInfo()
 	{
 
 		String fileName = Variables.dynamicExtensionsHome + System.getProperty("file.separator")
 				+ ApplicationProperties.getValue("application.version.file");
 		CVSTagReader cvsTagReader = new CVSTagReader();
 		String cvsTag = cvsTagReader.readTag(fileName);
 		Variables.applicationCvsTag = cvsTag;
 		Logger.out.info("========================================================");
 		Logger.out.info("Application Information");
 		Logger.out.info("Name: " + Variables.applicationName);
 		Logger.out.info("Version: " + Variables.applicationVersion);
 		Logger.out.info("CVS TAG: " + Variables.applicationCvsTag);
 		Logger.out.info("Path: " + Variables.applicationHome);
 		Logger.out.info("Database Name: " + Variables.databaseName);
 		Logger.out.info("========================================================");
 
 		try
 		{
 			Logger.out.info("Preloading the DE metadata....This may take a few minutes");
 			EntityManager.getInstance().getAllContainers();
 		}
 		catch (Exception e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static AttributeTypeInformationInterface getAttributeTypeInformation(AbstractAttributeInterface abstractAttributeInterface)
 	{
 		AttributeTypeInformationInterface attributeTypeInformation = null;
 		if (abstractAttributeInterface != null)
 		{
 			if (abstractAttributeInterface instanceof AttributeInterface)
 			{
 				attributeTypeInformation = ((AttributeInterface) abstractAttributeInterface).getAttributeTypeInformation();
 			}
 		}
 		return attributeTypeInformation;
 	}
 
 	/**
 	 * This method converts stack trace to the string representation
 	 * @param aThrowable   throwable object
 	 * @return String representation  of the stack trace
 	 */
 	public static String getStackTrace(Throwable throwable)
 	{
 		final Writer result = new StringWriter();
 		final PrintWriter printWriter = new PrintWriter(result);
 		throwable.printStackTrace(printWriter);
 		return result.toString();
 	}
 
 	/**
 	 * Converts string to integer
 	 * @param string
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public static int convertStringToInt(String string) throws DynamicExtensionsApplicationException
 	{
 		int intValue = 0;
 		if (string != null)
 		{
 			try
 			{
 				if (string.trim().equals(""))
 				{
 					intValue = 0; //Assume 0 for blank values
 				}
 				else
 				{
 					intValue = Integer.parseInt(string);
 				}
 			}
 			catch (NumberFormatException e)
 			{
 				throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 			}
 		}
 		return intValue;
 	}
 
 	/**
 	 * Checks that the input String contains only numeric digits.
 	 * @param numString The string whose characters are to be checked.
 	 * @return Returns false if the String contains any alphabet else returns true.
 	 * */
 	public static boolean isNaturalNumber(String numString)
 	{
 		boolean isNaturalNumber = true;
 		try
 		{
 			double doubleValue = Double.parseDouble(numString);
 			if (doubleValue < 0)
 			{
 				isNaturalNumber = false;
 			}
 		}
 		catch (NumberFormatException exp)
 		{
 			isNaturalNumber = false;
 		}
 		return isNaturalNumber;
 	}
 
 	/**
 	 *
 	 * @param numString
 	 * @return
 	 */
 	public static boolean isNumeric(String numString)
 	{
 		boolean isNumeric = true;
 		BigDecimal bigDecimal = null;
 		try
 		{
 			bigDecimal = new BigDecimal(numString);
 			if (bigDecimal == null)
 			{
 				isNumeric = false;
 			}
 		}
 		catch (NumberFormatException exp)
 		{
 			isNumeric = false;
 		}
 		return isNumeric;
 	}
 
 	public static int getCurrentDay()
 	{
 		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
 	}
 
 	public static int getCurrentMonth()
 	{
 		return (Calendar.getInstance().get(Calendar.MONTH) + 1);
 	}
 
 	public static int getCurrentYear()
 	{
 		return Calendar.getInstance().get(Calendar.YEAR);
 	}
 
 	public static int getCurrentHours()
 	{
 		return Calendar.getInstance().get(Calendar.HOUR);
 	}
 
 	public static int getCurrentMinutes()
 	{
 		return Calendar.getInstance().get(Calendar.MINUTE);
 	}
 
 	/**
 	 *
 	 * @param originalObject Object
 	 * @return Object
 	 */
 	public static Object cloneObject(Object originalObject)
 	{
 		Object clonedObject = null;
 		try
 		{
 			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
 			objectOutputStream.writeObject(originalObject);
 			//retrieve back
 			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
 			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
 			clonedObject = objectInputStream.readObject();
 		}
 		catch (IOException ioe)
 		{
 
 			ioe.printStackTrace();
 		}
 		catch (ClassNotFoundException cnfe)
 		{
 			cnfe.printStackTrace();
 		}
 
 		return clonedObject;
 	}
 
 	/**
 	 * @param string : string to be checked
 	 * @param list: List that is to be checked if string is contained
 	 * @return check if a string is contained in the passed list and return true if yes
 	 */
 	public static boolean isStringInList(String string, List<String> list)
 	{
 		boolean isContainedInList = false;
 		if ((string != null) && (list != null))
 		{
 			String listString = null;
 			Iterator<String> iterator = list.iterator();
 			while (iterator.hasNext())
 			{
 				listString = iterator.next();
 				if (string.equals(listString))
 				{
 					isContainedInList = true;
 					break;
 				}
 			}
 		}
 		return isContainedInList;
 	}
 
 	/**
 	 *
 	 * @param list list of NameValueBeanObjects
 	 */
 	public static void sortNameValueBeanListByName(List<NameValueBean> list)
 	{
 		Collections.sort(list, new Comparator()
 		{
 
 			public int compare(Object o1, Object o2)
 			{
 				String s1 = "";
 				String s2 = "";
 				if (o1 != null)
 				{
 					s1 = ((NameValueBean) o1).getName();
 				}
 				if (o2 != null)
 				{
 					s2 = ((NameValueBean) o2).getName();
 				}
 				return s1.compareTo(s2);
 			}
 		});
 	}
 
 	public static EntityGroupInterface getEntityGroup(EntityInterface entity)
 	{
 		EntityGroupInterface entityGroup = null;
 		if (entity != null)
 		{
 			//			Collection<EntityGroupInterface> entityGroupCollection = entity
 			//					.getEntityGroupCollection();
 			//			if (entityGroupCollection != null)
 			//			{
 			//				Iterator<EntityGroupInterface> entityGroupIter = entityGroupCollection.iterator();
 			//				if (entityGroupIter.hasNext())
 			//				{
 			entityGroup = entity.getEntityGroup();
 			//				}
 			//			}
 		}
 		return entityGroup;
 	}
 
 	/**
 	 * @param controlsSeqNumbers : String of controls sequence numbers
 	 * @param delimiter Delimiter used in string
 	 * @return
 	 */
 	public static Integer[] convertToIntegerArray(String controlsSeqNumbers, String delimiter)
 	{
 		ArrayList<Integer> integerList = new ArrayList<Integer>();
 		if (controlsSeqNumbers != null)
 		{
 			String str = null;
 			Integer integer = null;
 			StringTokenizer strTokenizer = new StringTokenizer(controlsSeqNumbers, delimiter);
 			if (strTokenizer != null)
 			{
 				while (strTokenizer.hasMoreElements())
 				{
 					str = strTokenizer.nextToken();
 					if (str != null)
 					{
 						try
 						{
 							integer = new Integer(str);
 							integerList.add(integer);
 						}
 						catch (NumberFormatException e)
 						{
 							Logger.out.error(e);
 						}
 					}
 				}
 			}
 		}
 		return integerList.toArray(new Integer[integerList.size()]);
 	}
 
 	/**
 	 * validate the entity for
 	 * 1. Name - should not contain any special characters, should not be empty,null
 	 * 2. Description - should be less than 1000 characters.
 	 *
 	 * @param entity
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public static void validateEntityForSaving(EntityInterface entity) throws DynamicExtensionsApplicationException
 	{
 
 		validateName(entity.getName());
 		Collection<AbstractAttributeInterface> collection = entity.getAbstractAttributeCollection();
 		if (collection != null && !collection.isEmpty())
 		{
 			Iterator iterator = collection.iterator();
 			while (iterator.hasNext())
 			{
 				AbstractMetadataInterface abstractMetadataInterface = (AbstractMetadataInterface) iterator.next();
 				validateName(abstractMetadataInterface.getName());
 			}
 		}
 
 		if (entity.getDescription() != null && entity.getDescription().length() > 1000)
 		{
 			throw new DynamicExtensionsApplicationException("Entity description size exceeded ", null,
 					EntityManagerExceptionConstantsInterface.DYEXTN_A_004);
 		}
 		//This validation is already in place in ApplyFormControlsProcessor
 		//(entity, entity.getName());
 
 		if (entity.getInheritanceStrategy().equals(InheritanceStrategy.TABLE_PER_HEIRARCHY) && entity.getParentEntity() != null)
 		{
 			if (entity.getDiscriminatorColumn() == null || entity.getDiscriminatorColumn().equals(""))
 			{
 				throw new DynamicExtensionsApplicationException("Discriminator Column and value is required for TABLE_PER_HEIRARCHY strategy", null,
 						EntityManagerExceptionConstantsInterface.DYEXTN_A_012);
 
 			}
 
 			if (entity.getDiscriminatorValue() == null || entity.getDiscriminatorValue().equals(""))
 			{
 				throw new DynamicExtensionsApplicationException("Discriminator Column and value is required for TABLE_PER_HEIRARCHY strategy", null,
 						EntityManagerExceptionConstantsInterface.DYEXTN_A_012);
 			}
 		}
 		return;
 	}
 
 	public static void validateDuplicateNamesWithinEntity(EntityInterface entity, String attributeName) throws DynamicExtensionsApplicationException
 	{
 		Collection<AbstractAttributeInterface> collection = entity.getAbstractAttributeCollection();
 		if (collection != null || !collection.isEmpty())
 		{
 			for (AbstractAttributeInterface attribute : collection)
 			{
 				if (attribute.getName().equals(attributeName))
 				{
 					throw new DynamicExtensionsApplicationException("Attribute names should be unique for the entity ", null,
 							EntityManagerExceptionConstantsInterface.DYEXTN_A_006);
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param name
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public static void validateName(String name) throws DynamicExtensionsApplicationException
 	{
 		/**
 		 * Constant representing valid names
 		 */
 		final String VALIDCHARSREGEX = "[^\\\\/:*?\"<>&;|']*";
 
 		if (name == null || name.trim().length() == 0 || !name.matches(VALIDCHARSREGEX))
 		{
 			throw new DynamicExtensionsApplicationException("Object name invalid", null, EntityManagerExceptionConstantsInterface.DYEXTN_A_003);
 		}
 		if (name.trim().length() > 1000)
 		{
 			throw new DynamicExtensionsApplicationException("Object name exceeds maximum limit", null,
 					EntityManagerExceptionConstantsInterface.DYEXTN_A_007);
 		}
 	}
 
 	/**
 	 * @param association
 	 * @param entitySet
 	 */
 	public static void updateEntityReferences(AbstractAttributeInterface abstractAttribute)
 	{
 
 		if (abstractAttribute instanceof AttributeInterface)
 		{
 			return;
 		}
 		Set<EntityInterface> entitySet = new HashSet<EntityInterface>();
 		entitySet.add(abstractAttribute.getEntity());
 		getAssociatedEntities(abstractAttribute.getEntity(), entitySet);
 		List<EntityInterface> entityList = new ArrayList<EntityInterface>(entitySet);
 
 		AssociationInterface association = (AssociationInterface) abstractAttribute;
 		EntityInterface targetEntity = association.getTargetEntity();
 		if (entityList.contains(targetEntity))
 		{
 			association.setTargetEntity((EntityInterface) entityList.get(entityList.indexOf(targetEntity)));
 			return;
 		}
 		for (AssociationInterface tagretEntityAssociation : targetEntity.getAssociationCollection())
 		{
 			EntityInterface entity = tagretEntityAssociation.getTargetEntity();
 			if (entityList.contains(entity))
 			{
 				tagretEntityAssociation.setTargetEntity((EntityInterface) entityList.get(entityList.indexOf(entity)));
 			}
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param entitySet
 	 */
 	public static void getAssociatedEntities(EntityInterface entity, Set<EntityInterface> entitySet)
 	{
 		Collection<AssociationInterface> associationCollection = entity.getAssociationCollection();
 		for (AssociationInterface associationInterface : associationCollection)
 		{
 			EntityInterface targetEntity = associationInterface.getTargetEntity();
 			if (!entitySet.contains(targetEntity))
 			{
 				entitySet.add(targetEntity);
 				getAssociatedEntities(targetEntity, entitySet);
 			}
 		}
 	}
 
 	/**
 	 * @param entity
 	 * @param entitySet
 	 */
 	public static List getUnsavedEntities(EntityGroupInterface entityGroup)
 	{
 		List<EntityInterface> entityList = new ArrayList<EntityInterface>();
 		Collection<EntityInterface> entityCollection = entityGroup.getEntityCollection();
 		for (EntityInterface entity : entityCollection)
 		{
 			if (entity.getId() == null)
 			{
 				entityList.add(entity);
 			}
 		}
 		return entityList;
 	}
 
 	/**
 	 * @param entity
 	 * @param entitySet
 	 */
 	public static List getSavedEntities(EntityGroupInterface entityGroup)
 	{
 		List<EntityInterface> entityList = new ArrayList<EntityInterface>();
 		Collection<EntityInterface> entityCollection = entityGroup.getEntityCollection();
 		for (EntityInterface entity : entityCollection)
 		{
 			if (entity.getId() != null)
 			{
 				entityList.add(entity);
 			}
 		}
 		return entityList;
 	}
 
 	/**
 	 * @param processedEntityList
 	 * @param entity
 	 * @return
 	 */
 	private static boolean isEntityPresent(List<EntityInterface> entityList, EntityInterface entity)
 	{
 		for (EntityInterface entityInterface : entityList)
 		{
 			if (entityInterface.getName().equalsIgnoreCase(entity.getName()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * This method checks if the date string is as per the given format or not.
 	 * @param dateFormat Format of the date (e.g. MM-DD-YYYY)
 	 * @param strDate Date value in String.
 	 * @return true if date is valid, false otherwise
 	 */
 	public static boolean isDateValid(String dateFormat, String strDate)
 	{
 		boolean isDateValid = false;
 		Date date = null;
 
 		if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT))
 		{
 			strDate = formatMonthAndYearDate(strDate);
 			//09-12-2007 0:0
 		}
 		if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT))
 		{
 			strDate = formatYearDate(strDate);
 			//09-12-2007 0:0
 		}
 
 		try
 		{
 			date = Utility.parseDate(strDate, dateFormat);
 			if (date != null)
 			{
 				isDateValid = true;
 			}
 		}
 		catch (ParseException parseException)
 		{
 			isDateValid = false;
 		}
 
 		return isDateValid;
 	}
 
 	/**
 	 * @param strDate
 	 * @return
 	 */
 	public static String formatMonthAndYearDate(String strDate)
 	{
 		String month = strDate.substring(0, 2);
 		String year = strDate.substring(3, strDate.length());
 		// For MySQL5, do not add time to date for YYYY and MM-YYYY formats.
 		if (Variables.databaseName.equals(Constants.MYSQL_DATABASE))
 		{
 			return month + "-" + "01" + "-" + year;
 		}
 		else
 		{
 			return month + "-" + "01" + "-" + year + " 0:0";
 		}
 	}
 
 	/**
 	 * @param strDate
 	 * @return
 	 */
 	public static String formatYearDate(String strDate)
 	{
 		String year = strDate;
 		// For MySQL5, do not add time to date for YYYY and MM-YYYY formats.
 		if (Variables.databaseName.equals(Constants.MYSQL_DATABASE))
 		{
 			return "01" + "-" + "01" + "-" + year;
 		}
 		else
 		{
 			return "01" + "-" + "01" + "-" + year + " 0:0";
 		}
 	}
 
 	/**
 	 * This method determines whether the checkbox is to be checked or not.
 	 * @param value the value particular to database
 	 * for e.g. oracle - "1" or "0"
 	 * mysql "true" or "false"
 	 * @return true if checked , false otherwise
 	 */
 	public static boolean isCheckBoxChecked(String value)
 	{
 		boolean isChecked = false;
 		if (value != null && value.trim().length() > 0)
 		{
 			if(value.equals("1") || value.equals("true"))
 			{
 				isChecked = true;
 			}
 		}
 		return isChecked;
 	}
 
 	/**
 	 * This method returns the value for checkbox depending on database.
 	 * for e.g. oracle - "1" or "0"
 	 * mysql "true" or "false"
 	 * @param ischecked
 	 * @return string value which is assigned to checkbox value
 	 */
 	public static String getValueForCheckBox(boolean ischecked)
 	{
 		String checkboxValue = "";
 		if (Variables.databaseName.equals(Constants.ORACLE_DATABASE))
 		{
 			if (ischecked)
 			{
 				checkboxValue = "1";
 			}
 			else
 			{
 				checkboxValue = "0";
 			}
 		}
 		else if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) || Variables.databaseName.equals(Constants.POSTGRESQL_DATABASE))
 		{
 			if (ischecked)
 			{
 				checkboxValue = "true";
 			}
 			else
 			{
 				checkboxValue = "false";
 			}
 		}
 		return checkboxValue;
 	}
 
 	/**
 	 * This method returns the html keyword checked for checkbox selection
 	 * @param ischecked
 	 * @return 'checked' string or empty string
 	 */
 	public static String getCheckboxSelectionValue(String value)
 	{
 		String checkboxValue = "";
 		if (value != null && value.trim().length() > 0)
 		{
 			if (value.equalsIgnoreCase(getValueForCheckBox(true)))
 			{
 				checkboxValue = "checked";
 			}
 		}
 		return checkboxValue;
 	}
 
 	/**
 	 * This method compares the two date strings.
 	 * @param date1 the first date value.
 	 * @param date2 the second date value.
 	 * @param dateFormat the format of both date.
 	 * @return -1 if date1 is lesser than date2
 	 * 			0 if date1 is equals to date2
 	 * 			1 if date1 is greater than date2.
 	 */
 	public static int compareDates(String date1, String date2, String dateFormat)
 	{
 		int result = 0;
 
 		if (areBothDatesOfSameFormat(date1, date2))
 		{
 			result = 1;
 			return result;
 		}
 
 		if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT))
 		{
 			if (Integer.parseInt(date1.substring(3, date1.length()).trim()) > Integer.parseInt(date2.substring(3, date2.length()).trim()))
 			{
 				result = 1;
 				return result;
 			}
 			date1 = formatMonthAndYearDate(date1);
 			date2 = formatMonthAndYearDate(date2);
 			//09-12-2007 0:0
 		}
 
 		if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT))
 		{
 			//date1 = formatYearDate(date1);
 			//date2 = formatYearDate(date2);
 			if (Integer.parseInt(date1) > Integer.parseInt(date2))
 			{
 				result = 1;
 				return result;
 			}
 			//09-12-2007 0:0
 		}
 
 		try
 		{
 			Date firstDate = Utility.parseDate(date1, "MM-dd-yyyy");
 			Date secondDate = Utility.parseDate(date2, "MM-dd-yyyy");
 			if (firstDate.after(secondDate))
 			{
 				result = 1;
 			}
 			else if (firstDate.before(secondDate))
 			{
 				result = -1;
 			}
 		}
 		catch (ParseException parseException)
 		{
 			result = -2;
 		}
 
 		return result;
 	}
 
 	public static boolean areBothDatesOfSameFormat(String date1, String date2)
 	{
 		if (date1.length() != date2.length())
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * This method returns the format of the date depending upon the the type of the format selected on UI.
 	 * @param format Selected format
 	 * @return date format
 	 */
 	public static String getDateFormat(String format)
 	{
 		String dateFormat = ProcessorConstants.DATE_ONLY_FORMAT;
 		if (format != null && format.equals(ProcessorConstants.DATE_FORMAT_OPTION_DATEANDTIME))
 		{
 			dateFormat = ProcessorConstants.DATE_TIME_FORMAT;
 		}
 		if (format != null && format.equals(ProcessorConstants.DATE_FORMAT_OPTION_MONTHANDYEAR))
 		{
 			dateFormat = ProcessorConstants.MONTH_YEAR_FORMAT;
 		}
 		if (format != null && format.equals(ProcessorConstants.DATE_FORMAT_OPTION_YEARONLY))
 		{
 			dateFormat = ProcessorConstants.YEAR_ONLY_FORMAT;
 		}
 
 		return dateFormat;
 	}
 
 	/**
 	 * This method returns the sql format of the date depending upon the the type of the format of the Date Attribute.
 	 * @param dateFormat format of the Date Attribute
 	 * @return SQL date format
 	 */
 	public static String getSQLDateFormat(String dateFormat)
 	{
 		String sqlDateFormat = Variables.datePattern;
 		if (dateFormat != null && dateFormat.equals(ProcessorConstants.DATE_TIME_FORMAT))
 		{
 			sqlDateFormat = sqlDateFormat + " " + Variables.timePattern;
 		}
 		return sqlDateFormat;
 	}
 
 	/**
 	 * @param caption
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static ContainerInterface getContainerByCaption(String caption) throws DynamicExtensionsSystemException
 	{
 		DefaultBizLogic defaultBizLogic = BizLogicFactory.getDefaultBizLogic();
 		List objectList = new ArrayList();
 		ContainerInterface containerInterface = null;
 		if (caption == null || caption.trim().length() == 0)
 		{
 			return null;
 		}
 		try
 		{
 			objectList = defaultBizLogic.retrieve(Container.class.getName(), "caption", caption);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(e.getMessage(), e);
 		}
 
 		if (objectList.size() > 0)
 		{
 			containerInterface = (ContainerInterface) objectList.get(0);
 		}
 
 		return containerInterface;
 	}
 
 	/**
 	 * @param containerColl
 	 * @return
 	 */
 	public static List<String> getMainContainerNamesList(Collection<ContainerInterface> containerColl)
 	{
 		List<String> mainContainerNames = new ArrayList<String>();
 		for (ContainerInterface container : containerColl)
 		{
 			mainContainerNames.add(container.getCaption());
 		}
 		return mainContainerNames;
 	}
 
 	/**
 	 * This method corrects cardinalities such that max cardinality  < minimum cardinality ,otherwise it throws exception
 	 * @param entity
 	 */
 	private static void correctCardinalities(EntityInterface entity) throws DynamicExtensionsApplicationException
 	{
 		Collection associationCollection = entity.getAssociationCollection();
 		if (associationCollection != null && !associationCollection.isEmpty())
 		{
 			Iterator iterator = associationCollection.iterator();
 			while (iterator.hasNext())
 			{
 				Association association = (Association) iterator.next();
 				swapCardinality(association.getSourceRole());
 				swapCardinality(association.getTargetRole());
 			}
 		}
 	}
 
 	/**
 	 * @param role
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private static void swapCardinality(RoleInterface role) throws DynamicExtensionsApplicationException
 	{
 		// make Min cardinality < Max cardinality
 		if (role.getMinimumCardinality().equals(Cardinality.MANY) || role.getMaximumCardinality().equals(Cardinality.ZERO))
 		{
 			Cardinality e = role.getMinimumCardinality();
 			role.setMinimumCardinality(role.getMaximumCardinality());
 			role.setMaximumCardinality(e);
 		}
 
 		if (role.getMaximumCardinality().equals(Cardinality.ZERO))
 		{
 			throw new DynamicExtensionsApplicationException("Cardinality constraint violated", null,
 					EntityManagerExceptionConstantsInterface.DYEXTN_A_005);
 		}
 	}
 
 	/**
 	 * This method processes entity before saving it to databse.
 	 * <li> It validates entity for duplicate name of entity,attributes and association
 	 * <li> It sets created and updated date-time.
 	 *
 	 * @param entity entity
 	 */
 	public static void validateEntity(EntityInterface entity) throws DynamicExtensionsApplicationException
 	{
 		validateEntityForSaving(entity);// chk if entity is valid or not.
 
 		correctCardinalities(entity); // correct the cardinality if max cardinality  < min cardinality
 
 		//        if (entity.getId() != null)
 		//        {
 		//            entity.setLastUpdated(new Date());
 		//        }
 		//        else
 		//        {
 		//            entity.setCreatedDate(new Date());
 		//            entity.setLastUpdated(entity.getCreatedDate());
 		//        }
 	}
 
 	public static String getTableName(AssociationInterface association)
 	{
 		String tableName = "";
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 		Cardinality sourceMaxCardinality = sourceRole.getMaximumCardinality();
 		Cardinality targetMaxCardinality = targetRole.getMaximumCardinality();
 		if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.MANY)
 		{
 			tableName = association.getConstraintProperties().getName();
 		}
 		else if (sourceMaxCardinality == Cardinality.MANY && targetMaxCardinality == Cardinality.ONE)
 		{
 			tableName = association.getEntity().getTableProperties().getName();
 		}
 		else
 		{
 			tableName = association.getTargetEntity().getTableProperties().getName();
 		}
 		return tableName;
 	}
 
 	/**
 	 * Retrieve entity group by its name from database.
 	 * @param name name of category
 	 * @return entity group
 	 */
 	public static EntityGroupInterface retrieveEntityGroup(String name)
 	{
 		DefaultBizLogic bizlogic = new DefaultBizLogic();
 		Collection<EntityGroupInterface> entityGroupCollection = new HashSet<EntityGroupInterface>();
 		EntityGroupInterface entityGroup = null;
 
 		try
 		{
 			// Fetch the entity group from the database.
 			entityGroupCollection = bizlogic.retrieve(EntityGroup.class.getName(), "shortName", name);
 
 			if (entityGroupCollection != null && entityGroupCollection.size() > 0)
 			{
 				entityGroup = (EntityGroupInterface) entityGroupCollection.iterator().next();
 			}
 		}
 		catch (DAOException e)
 		{
 			e.printStackTrace();
 
 		}
 
 		return entityGroup;
 	}
 
 	/**
 	 * This method will update the cache on server startup time
 	 */
 	public static void updateDynamicExtensionsCache() throws DynamicExtensionsSystemException
 	{
 
 		Map containerMap = null;
 		try
 		{
 			AbstractBizLogic bizLogic = BizLogicFactory.getDefaultBizLogic();
 			List containerList = bizLogic.retrieve(ContainerInterface.class.getName());
 
 			containerMap = new HashMap();
 			for (int cnt = 0; cnt < containerList.size(); cnt++)
 			{
 				ContainerInterface objContainer = (ContainerInterface) containerList.get(cnt);
 				containerMap.put(objContainer.getId(), objContainer);
 
 			}
 
 			// getting instance of catissueCoreCacheManager and adding containerMap to cache
 			DynamicExtensionsCacheManager deCacheManager = DynamicExtensionsCacheManager.getInstance();
 			deCacheManager.removeObjectFromCache(Constants.LIST_OF_CONTAINER);
 			deCacheManager.addObjectToCache(Constants.LIST_OF_CONTAINER, (HashMap) containerMap);
 			System.out.println("ON Startup caching containers.Size of Container ----------" + containerList.size());
 
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug("Exception occured while creating instance of DynamicExtensionsCacheManager");
 			throw new DynamicExtensionsSystemException(e.getMessage());
 
 		}
 	}
 
 	/**
 	 * This method updates the DynamicExtensions cache of all container within Entitygroup
 	 *
 	 */
 	public static void updateDynamicExtensionsCache(Long entityGroupId) throws DynamicExtensionsSystemException
 	{
 
 		try
 		{
 			// getting instance of DynamicExtensionsCacheManager and adding containerMap to cache
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			ArrayList containerSet = (ArrayList) entityManager.getAllContainersByEntityGroupId(entityGroupId);
 			Iterator itr = containerSet.iterator();
 			while (itr.hasNext())
 			{
 				ContainerInterface objContainer = (Container) itr.next();
 				DynamicExtensionsUtility.updateDynamicExtensionsCache(objContainer);
 			}
 
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug("Exception occured while creating instance of DynamicExtensionsCacheManager");
 			throw new DynamicExtensionsSystemException(e.getMessage());
 		}
 	}
 
 	/**
 	 * This method updates the DynamicExtensions cache with updated container
 	 * @param updatedContainer
 	 */
 	public static void updateDynamicExtensionsCache(ContainerInterface updatedContainer) throws DynamicExtensionsSystemException
 	{
 
 		try
 		{
 			// getting instance of DynamicExtensionsCacheManager and adding containerMap to cache
 			DynamicExtensionsCacheManager deCacheManager = DynamicExtensionsCacheManager.getInstance();
 			Map containerMap = new HashMap();
 			containerMap = (HashMap) deCacheManager.getObjectFromCache(Constants.LIST_OF_CONTAINER);
 			if (containerMap != null)
 			{
 				containerMap.put(updatedContainer.getId(), updatedContainer);
 			}
 			deCacheManager.removeObjectFromCache(Constants.LIST_OF_CONTAINER);
 			deCacheManager.addObjectToCache(Constants.LIST_OF_CONTAINER, (HashMap) containerMap);
 
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug("Exception occured while creating instance of DynamicExtensionsCacheManager");
 			throw new DynamicExtensionsSystemException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Method to check if data type is numeric i.e long,integer,short,float,double
 	 */
 	public static boolean isDataTypeNumeric(String dataType)
 	{
 		boolean isDataTypeNumber = false;
 		if (dataType.equals(ProcessorConstants.DATATYPE_SHORT) || dataType.equals(ProcessorConstants.DATATYPE_INTEGER)
 				|| dataType.equals(ProcessorConstants.DATATYPE_LONG) || dataType.equals(ProcessorConstants.DATATYPE_FLOAT)
 				|| dataType.equals(ProcessorConstants.DATATYPE_DOUBLE) || dataType.equals(ProcessorConstants.DATATYPE_NUMBER))
 		{
 			isDataTypeNumber = true;
 		}
 
 		return isDataTypeNumber;
 	}
 
 	/**
 	 * @param categoryEntity
 	 * @param objCategoryMap
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void getUnsavedCategoryEntityList(CategoryEntityInterface categoryEntity, HashMap<String, CategoryEntityInterface> objCategoryMap)
 			throws DynamicExtensionsSystemException
 	{
 
 		if (categoryEntity != null)
 		{
 			if (objCategoryMap.containsKey(categoryEntity.getName()))
 			{
 				return;
 			}
 			CategoryEntity objCategoryEntity = (CategoryEntity) categoryEntity;
 			if (objCategoryEntity.getParentCategoryEntity() != null
 					&& !objCategoryMap.containsKey(objCategoryEntity.getParentCategoryEntity().getName()))
 			{
 				if (((CategoryEntity) objCategoryEntity.getParentCategoryEntity()).isCreateTable())
 					getUnsavedCategoryEntityList(objCategoryEntity.getParentCategoryEntity(), objCategoryMap);
 			}
 			if (!objCategoryMap.containsKey(categoryEntity.getName()) && objCategoryEntity.isCreateTable())
 			{
 				if (objCategoryEntity.getId() == null)
 				{
 					//Only includes those category entity for which table is required to be created
 					if (objCategoryEntity.isCreateTable())
 						objCategoryMap.put(categoryEntity.getName(), objCategoryEntity);
 				}
 			}
 			else
 			{
 				return;
 			}
 			for (CategoryAssociationInterface categoryAssociationInterface : categoryEntity.getCategoryAssociationCollection())
 			{
 				CategoryEntity objCEntity = (CategoryEntity) categoryAssociationInterface.getTargetCategoryEntity();
 				if (objCEntity != null && objCEntity.isCreateTable())
 					if (!objCategoryMap.containsKey(categoryAssociationInterface.getTargetCategoryEntity().getName()))
 						getUnsavedCategoryEntityList(categoryAssociationInterface.getTargetCategoryEntity(), objCategoryMap);
 			}
 		}
 	}
 
 	/**
 	 * @param categoryEntity
 	 * @param objCategoryMap
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void getSavedCategoryEntityList(CategoryEntityInterface categoryEntity, HashMap<String, CategoryEntityInterface> objCategoryMap)
 			throws DynamicExtensionsSystemException
 	{
 
 		if (categoryEntity != null)
 		{
 			if (objCategoryMap.containsKey(categoryEntity.getName()))
 			{
 				return;
 			}
 			CategoryEntity objCategoryEntity = (CategoryEntity) categoryEntity;
 			if (categoryEntity.getParentCategoryEntity() != null && !objCategoryMap.containsKey(categoryEntity.getParentCategoryEntity().getName()))
 			{
 				if (((CategoryEntity) objCategoryEntity.getParentCategoryEntity()).isCreateTable())
 					getSavedCategoryEntityList(categoryEntity.getParentCategoryEntity(), objCategoryMap);
 			}
 			if (!objCategoryMap.containsKey(categoryEntity.getName()))
 			{
 				if (objCategoryEntity.getId() != null && objCategoryEntity.isCreateTable())
 				{
 					objCategoryMap.put(categoryEntity.getName(), categoryEntity);
 				}
 			}
 			else
 			{
 				return;
 			}
 			for (CategoryAssociationInterface categoryAssociationInterface : categoryEntity.getCategoryAssociationCollection())
 			{
 				CategoryEntity objCEntity = (CategoryEntity) categoryAssociationInterface.getTargetCategoryEntity();
 				if (objCEntity != null && objCEntity.isCreateTable() && objCEntity.getId() != null)
 					if (!objCategoryMap.containsKey(categoryAssociationInterface.getTargetCategoryEntity().getName()))
 						getSavedCategoryEntityList(categoryAssociationInterface.getTargetCategoryEntity(), objCategoryMap);
 			}
 		}
 	}
 
 	/**
 	 * This method sets the source entity key or target entity key as null depending upon
 	 * whether the association is one-to-one, one-to-many or many-to-one.
 	 * @param association
 	 * @return ConstraintPropertiesInterface
 	 */
 	public static ConstraintPropertiesInterface getConstraintProperties(AssociationInterface association)
 	{
 		ConstraintPropertiesInterface constraintProperties = association.getConstraintProperties();
 
 		if (association.getSourceRole().getMaximumCardinality() == Cardinality.MANY
 				&& association.getTargetRole().getMaximumCardinality() == Cardinality.ONE)
 		{
 			constraintProperties.setTargetEntityKey(null);
 		}
 		else if (association.getSourceRole().getMaximumCardinality() == Cardinality.ONE
 				&& association.getTargetRole().getMaximumCardinality() == Cardinality.MANY
 				|| association.getSourceRole().getMaximumCardinality() == Cardinality.ONE
 				&& association.getTargetRole().getMaximumCardinality() == Cardinality.ONE)
 		{
 			constraintProperties.setSourceEntityKey(null);
 		}
 
 		return constraintProperties;
 	}
 
 	/**
 	 *
 	 * @param controlCollection
 	 * @param sequenceNumber
 	 * @return
 	 */
 	public static List<Long> getDeletedAssociationIds(ControlInterface[] controlCollection, Integer[] sequenceNumbers)
 	{
 		List<Long> listOfIds = new ArrayList<Long>();
 		boolean isPresent = false;
 		if (controlCollection != null)
 		{
 			for (ControlInterface control : controlCollection)
 			{
 				isPresent = false;
 				if (control instanceof ContainmentAssociationControl)
 				{
 					ContainmentAssociationControl containmentAssociationControl = (ContainmentAssociationControl) control;
 					if (sequenceNumbers != null)
 					{
 						for (Integer sequenceNumber : sequenceNumbers)
 						{
 							if (containmentAssociationControl.getSequenceNumber() != null
 									&& containmentAssociationControl.getSequenceNumber().equals(sequenceNumber))
 							{
 								isPresent = true;
 							}
 						}
 						if (!isPresent)
 						{
 							AssociationInterface associationInterface = (AssociationInterface) containmentAssociationControl
 									.getBaseAbstractAttribute();
 							listOfIds.add(associationInterface.getId());
 						}
 					}
 				}
 			}
 		}
 		return listOfIds;
 	}
 
 	/**
 	 * @param attribute
 	 * @param defaultValue
 	 * @param min
 	 * @param max
 	 * @param attributeName
 	 * @throws DynamicExtensionsValidationException
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	public static void verifyDefaultValueIsInRange(AbstractAttributeInterface attribute, String defaultValue, String min, String max,
 			String attributeName) throws DynamicExtensionsValidationException, DataTypeFactoryInitializationException
 	{
 		if (defaultValue != null && min != null && max != null)
 		{
 			Map<String, String> parameterMap = new HashMap<String, String>();
 			parameterMap.put("min", min);
 			parameterMap.put("max", max);
 
 			RangeValidator rangeValidator = new RangeValidator();
 			rangeValidator.validate((AttributeMetadataInterface) attribute, defaultValue, parameterMap, attributeName);
 		}
 	}
 
 	/**
 	 * This method used to replace escape characters such as singel and double quote
 	 * @param str
 	 * @param one
 	 * @param another
 	 * @return
 	 */
 	public static String replaceUtil(String str, String one, String another)
 	{
 		if (str == null)
 			return str;
 		//    	 In a string replace one substring with another
 		if (str.equals(""))
 			return "";
 		String res = "";
 		int i = str.indexOf(one, 0);
 		int lastpos = 0;
 		while (i != -1)
 		{
 			res += str.substring(lastpos, i) + another;
 			lastpos = i + one.length();
 			i = str.indexOf(one, lastpos);
 		}
 		res += str.substring(lastpos);
 		return res;
 	}
 }
