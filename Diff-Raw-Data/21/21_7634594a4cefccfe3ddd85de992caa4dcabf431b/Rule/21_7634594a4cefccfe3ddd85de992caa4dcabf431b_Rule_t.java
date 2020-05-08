 
 package edu.common.dynamicextensions.domain.validationrules;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 
 import edu.common.dynamicextensions.domain.DynamicExtensionBaseDomainObject;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:08 PM
  * @hibernate.class table="DYEXTN_RULE"
  */
 public class Rule extends DynamicExtensionBaseDomainObject implements RuleInterface
 {
 
 	/**
 	 * Serial Version Unique Identifier
 	 */
 	private static final long serialVersionUID = 6495330944005526400L;
 
 	/**
 	 * Name of the rule.
 	 */
 	protected String name;
 
 	/**
 	 * The Collection of RuleParameter.
 	 */
 	protected Collection<RuleParameterInterface> ruleParameterCollection = new HashSet<RuleParameterInterface>();
 
 	/**
 	 * 
 	 */
 	protected Boolean isImplicitRule = Boolean.TRUE;
 
 	/**
 	 * This method returns the Unique Identifier of the Object.
 	 * @hibernate.id name="id" column="IDENTIFIER" type="long"
 	 * length="30" unsaved-value="null" generator-class="native"
 	 * @hibernate.generator-param name="sequence" value="DYEXTN_RULE_SEQ"
 	 * @return the Unique Identifier of the Object.
 	 */
 	public Long getId()
 	{
 		return id;
 	}
 
 	/**
 	 * This method returns the name of the Rule.
 	 * @hibernate.property name="name" type="string" column="NAME" 
 	 * @return the name of the Rule.
 	 */
 	public String getName()
 	{
 		return name;
 	}
 
 	/**
 	 * This method sets the name of the Rule.
 	 * @param name the name to be set.
 	 */
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	/**
 	 * This method returns the Collection of RuleParameters.
 	 * @hibernate.set name="ruleParameterCollection" table="DYEXTN_RULE_PARAMETER"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="RULE_ID"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.validationrules.RuleParameter"
 	 * @return the Collection of RuleParameters.
 	 */
 	public Collection<RuleParameterInterface> getRuleParameterCollection()
 	{
 		return ruleParameterCollection;
 	}
 
 	/**
 	 * This method sets ruleParameterCollection to the Collection of RuleParameters.
 	 * @param ruleParameterCollection the the Collection of RuleParameters to be set.
 	 */
 	public void setRuleParameterCollection(
 			Collection<RuleParameterInterface> ruleParameterCollection)
 	{
 		this.ruleParameterCollection = ruleParameterCollection;
 	}
 
 	/**
 	 * This method adds a RuleParameter to the Collection of RuleParameter.
 	 * @param ruleParameter RuleParameter to be added.
 	 */
 	public void addRuleParameter(RuleParameterInterface ruleParameter)
 	{
 		if (ruleParameterCollection == null)
 		{
 			ruleParameterCollection = new LinkedHashSet<RuleParameterInterface>();
 		}
 		ruleParameterCollection.add(ruleParameter);
 	}
 
 	/**
 	 * @hibernate.property name="isImplicitRule" type="boolean" column="IS_IMPLICIT"
 	 * @return the isImplicitRule 
 	 */
 	public Boolean getIsImplicitRule()
 	{
 		return isImplicitRule;
 	}
 
 	/**
 	 * @param isImplicitRule the isImplicitRule to set
 	 */
 	public void setIsImplicitRule(Boolean isImplicitRule)
 	{
 		this.isImplicitRule = isImplicitRule;
 	}
 
 }
