 
 package edu.common.dynamicextensions.domain.databaseproperties;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintKeyPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 
 /**
  * @version 1.0
  * @created 28-Sep-2006 12:20:07 PM
  * @hibernate.joined-subclass table="DYEXTN_CONSTRAINT_PROPERTIES"
  * @hibernate.joined-subclass-key column="IDENTIFIER"
  */
 public class ConstraintProperties extends DatabaseProperties
 		implements
 			ConstraintPropertiesInterface
 {
 
 	/**
 	 * The source entity key through which constraint is related.
 	 * e.g. Used in case of foreign key constraint in one to many relation.
 	 */
 	protected Collection<ConstraintKeyPropertiesInterface> srcEntityConstraintKeyPropertiesCollection = new HashSet<ConstraintKeyPropertiesInterface>();
 	/**
 	 * The target entity key through which constraint is related.
 	 * Used in case of many to many relation.Both source and target entity key is entered in the intermediate table.
 	 */
 	protected Collection<ConstraintKeyPropertiesInterface> tgtEntityConstraintKeyPropertiesCollection = new HashSet<ConstraintKeyPropertiesInterface>();
 
 	/**
 	 * The name of the Constraint  
 	 */
 	protected String ConstraintName;
 
 	/**
 	 * It will return the srcEntityConstraintCollection on which the constraint depends
 	 * @hibernate.set name="srcEntityConstraintKeyPropertiesCollection" table="DYEXTN_CONSTRAINTKEY_PROP"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="SRC_CONSTRAINT_KEY_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.databaseproperties.ConstraintKeyProperties"
 	 * @return Returns the srcEntityConstraintKeyPropertiesCollection.
 	 * @return
 	 */
 	public Collection<ConstraintKeyPropertiesInterface> getSrcEntityConstraintKeyPropertiesCollection()
 	{
 		return srcEntityConstraintKeyPropertiesCollection;
 	}
 
 	/**
 	 * It will set the srcEntityConstraintCollection
 	 * @param srcEntityConstraint
 	 */
 	public void setSrcEntityConstraintKeyPropertiesCollection(
 			Collection<ConstraintKeyPropertiesInterface> srcEntityConstraintKeyPropertiesCollection)
 	{
 		this.srcEntityConstraintKeyPropertiesCollection = srcEntityConstraintKeyPropertiesCollection;
 	}
 
 	/**
 	 * @hibernate.set name="tgtEntityConstraintKeyPropertiesCollection" table="DYEXTN_CONSTRAINTKEY_PROP"
 	 * cascade="all-delete-orphan" inverse="false" lazy="false"
 	 * @hibernate.collection-key column="TGT_CONSTRAINT_KEY_ID"
 	 * @hibernate.cache  usage="read-write"
 	 * @hibernate.collection-one-to-many class="edu.common.dynamicextensions.domain.databaseproperties.ConstraintKeyProperties"
 	 * @return tgtEntityConstraintKeyPropertiesCollection
 	 */
 	public Collection<ConstraintKeyPropertiesInterface> getTgtEntityConstraintKeyPropertiesCollection()
 	{
 		return tgtEntityConstraintKeyPropertiesCollection;
 	}
 
 	/**
 	 * It will set the srcEntityConstraintCollection
 	 * @param tgtEntityConstraint
 	 */
 	public void setTgtEntityConstraintKeyPropertiesCollection(
 			Collection<ConstraintKeyPropertiesInterface> tgtEntityConstraintCollection)
 	{
 		this.tgtEntityConstraintKeyPropertiesCollection = tgtEntityConstraintCollection;
 	}
 
 	/**
 	 * It will clear the srcEntityConstraintCollection and then add add the srcEntityConstraint properties 
 	 */
 	public void setSrcEntityConstraintKeyProp(ConstraintKeyPropertiesInterface srcEntityConstraint)
 	{
 		if (srcEntityConstraintKeyPropertiesCollection == null)
 		{
 			srcEntityConstraintKeyPropertiesCollection = new HashSet();
 		}
 		else
 		{
 			srcEntityConstraintKeyPropertiesCollection.clear();
 		}
 		this.srcEntityConstraintKeyPropertiesCollection.add(srcEntityConstraint);
 	}
 
 	/**
 	 * It will clear the tgtEntityConstraintCollection and then add add the tgtEntityConstraint properties
 	 */
 	public void setTgtEntityConstraintKeyProp(ConstraintKeyPropertiesInterface tgtEntityConstraint)
 	{
 		if (tgtEntityConstraintKeyPropertiesCollection == null)
 		{
 			tgtEntityConstraintKeyPropertiesCollection = new HashSet();
 		}
 		else
 		{
 			tgtEntityConstraintKeyPropertiesCollection.clear();
 		}
 
 		this.tgtEntityConstraintKeyPropertiesCollection.add(tgtEntityConstraint);
 
 	}
 
 	/**
 	 * It will retrieve the constraintKeyProperties from srcEntityConstraintCollection
 	 * @return srcEntityConstraintKeyProperties
 	 */
 	public ConstraintKeyPropertiesInterface getSrcEntityConstraintKeyProperties()
 	{
 		ConstraintKeyPropertiesInterface cnstrKeyProp = null;
 		if (srcEntityConstraintKeyPropertiesCollection != null
 				&& !srcEntityConstraintKeyPropertiesCollection.isEmpty())
 		{
 			Iterator srcEntityConstraintIterator = srcEntityConstraintKeyPropertiesCollection
 					.iterator();
 			cnstrKeyProp = (ConstraintKeyPropertiesInterface) srcEntityConstraintIterator.next();
 		}
 		return cnstrKeyProp;
 
 	}
 
 	/**
 	 * it will retrieve the tgtEntityConstraint From tgtEntityConstraintCollection
 	 * @return
 	 */
 	public ConstraintKeyPropertiesInterface getTgtEntityConstraintKeyProperties()
 	{
 		ConstraintKeyPropertiesInterface cnstrKeyProp = null;
 		if (tgtEntityConstraintKeyPropertiesCollection != null
 				&& !tgtEntityConstraintKeyPropertiesCollection.isEmpty())
 		{
 			Iterator tgtEntityConstraintIterator = tgtEntityConstraintKeyPropertiesCollection
 					.iterator();
 			cnstrKeyProp = (ConstraintKeyPropertiesInterface) tgtEntityConstraintIterator.next();
 		}
 		return cnstrKeyProp;
 
 	}
 
 	/**
 	 * It will set the name of the foreign key constraint
 	 * @hibernate.property name="ConstraintName" type="string" column="CONSTRAINT_NAME"
 	 * @return
 	 */
 	public String getConstraintName()
 	{
 		return ConstraintName;
 	}
 
 	/**
 	 * It will return the name of the foreign key constraint
 	 */
 	public void setConstraintName(String constraintName)
 	{
 		ConstraintName = constraintName;
 	}
 
 	/**
 	 * It will add the srcCnstrKeyProp in the srcEntityConstraintCollection
 	 * @param srcCnstrKeyProp
 	 */
 	public void addSrcConstaintKeyProperties(ConstraintKeyPropertiesInterface srcCnstrKeyProp)
 	{
 		if (srcEntityConstraintKeyPropertiesCollection == null)
 		{
 			srcEntityConstraintKeyPropertiesCollection = new HashSet();
 		}
 		srcEntityConstraintKeyPropertiesCollection.add(srcCnstrKeyProp);
 
 	}
 
 	/**
 	 * It will add the srcCnstrKeyProp in the srcEntityConstraintCollection
 	 * @param srcCnstrKeyProp
 	 */
 	public void addTgtConstraintKeyProperties(ConstraintKeyPropertiesInterface tgtCnstrKeyProp)
 	{
 		if (tgtEntityConstraintKeyPropertiesCollection == null)
 		{
 			tgtEntityConstraintKeyPropertiesCollection = new HashSet();
 		}
 		tgtEntityConstraintKeyPropertiesCollection.add(tgtCnstrKeyProp);
 	}
 
 }
