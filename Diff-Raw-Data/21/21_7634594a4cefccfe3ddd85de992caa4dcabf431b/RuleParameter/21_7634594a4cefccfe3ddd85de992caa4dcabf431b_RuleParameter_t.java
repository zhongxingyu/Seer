 
 package edu.common.dynamicextensions.domain.validationrules;
 
 import java.io.Serializable;
 
 import edu.common.dynamicextensions.domain.DynamicExtensionBaseDomainObject;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 
 /**
  * This Rule 
  * @version 1.0
  * @created 28-Sep-2006 12:20:08 PM
  * @hibernate.class table="DYEXTN_RULE_PARAMETER"
  */
 public class RuleParameter extends DynamicExtensionBaseDomainObject
 		implements
 			Serializable,
 			RuleParameterInterface
 {
 
 	/**
 	 * Serial Version Unique Identifier
 	 */
 	private static final long serialVersionUID = 7900652797513990927L;
 
 	/**
 	 * Name of the rule parameter.
 	 */
 	protected String name;
 
 	/**
 	 * Value of the rule parameter.
 	 */
 	protected String value;
 
 	/**
 	 * @hibernate.id name="id" column="IDENTIFIER" type="long"
 	 * length="30" unsaved-value="null" generator-class="native"
 	 * @hibernate.generator-param name="sequence" value="DYEXTN_RULE_PARAMETER_SEQ"
 	 * @return Returns the id.
 	 */
 	public Long getId()
 	{
 		return id;
 	}
 
 	/**
 	 * @hibernate.property name="name" type="string" column="NAME" 
 	 * @return Returns the name.
 	 */
 	public String getName()
 	{
 		return name;
 	}
 
 	/**
 	 * @param name The name to set.
 	 */
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	/**
 	 * @hibernate.property name="value" type="string" column="VALUE" 
 	 * @return Returns the value.
 	 */
 	public String getValue()
 	{
 		return value;
 	}
 
 	/**
 	 * @param value The value to set.
 	 */
 	public void setValue(String value)
 	{
 		this.value = value;
 	}
 
 }
