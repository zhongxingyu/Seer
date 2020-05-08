 
 package edu.common.dynamicextensions.domain;
 
 import edu.common.dynamicextensions.domaininterface.BooleanTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BooleanValueInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:06 PM
  * @hibernate.joined-subclass table="DYEXTN_BOOLEAN_TYPE_INFO" 
  * @hibernate.joined-subclass-key column="IDENTIFIER" 
  */
 public class BooleanAttributeTypeInformation extends AttributeTypeInformation
 		implements
 			BooleanTypeInformationInterface
 {
 
 	/**
 	 * Empty Constructor.
 	 */
 	public BooleanAttributeTypeInformation()
 	{
 
 	}

 	/** 
 	 * @see edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface#getDataType()
 	 */
 	public String getDataType()
 	{
		return EntityManagerConstantsInterface.BOOL_ATTR_TYPE;
 	}
 
 	/**
 	 * 
 	 */
	public PermissibleValueInterface getPermissibleValueForString(String value)
	{
		DomainObjectFactory factory = DomainObjectFactory.getInstance();
		BooleanValueInterface booleanValue = factory.createBooleanValue();
		booleanValue.setValue(new Boolean(value));
 		
		return booleanValue;
 	}
 
 }
