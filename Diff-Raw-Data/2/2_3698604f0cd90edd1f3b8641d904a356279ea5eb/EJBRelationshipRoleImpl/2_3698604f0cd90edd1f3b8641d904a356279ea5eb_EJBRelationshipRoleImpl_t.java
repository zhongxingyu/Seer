 /*******************************************************************************
  * Copyright (c) 2001, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.ejb.internal.impl;
 
 import java.lang.reflect.Constructor;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.common.Description;
 import org.eclipse.jst.j2ee.ejb.CMPAttribute;
 import org.eclipse.jst.j2ee.ejb.CMRField;
 import org.eclipse.jst.j2ee.ejb.CommonRelationship;
 import org.eclipse.jst.j2ee.ejb.CommonRelationshipRole;
 import org.eclipse.jst.j2ee.ejb.ContainerManagedEntity;
 import org.eclipse.jst.j2ee.ejb.EJBRelation;
 import org.eclipse.jst.j2ee.ejb.EJBRelationshipRole;
 import org.eclipse.jst.j2ee.ejb.EjbPackage;
 import org.eclipse.jst.j2ee.ejb.IRoleShapeStrategy;
 import org.eclipse.jst.j2ee.ejb.MultiplicityKind;
 import org.eclipse.jst.j2ee.ejb.RoleSource;
 import org.eclipse.wst.common.internal.emf.utilities.IDUtil;
 /**
  * The ejb-relationship-role element describes a role within a relationship.
  * There are two roles in each relationship. The ejb-relationship-role element contains an optional description; an optional name for the relationship role; a specification of the multiplicity of the role; an optional specification of cascade-delete functionality for the role; the role source; and a declaration of the cmr-field, if any, by means of which the other side of the relationship is accessed from the perspective of the role source. The multiplicity and relationship-role-source element are mandatory. The relationship-role-source element designates an entity-bean by means of an ejb-name element. For bidirectional relationships, both roles of a relationship must declare a relationship-role-source element that specifies a cmr-field in terms of which the relationship is accessed. The lack of a cmr-field element in an ejb-relationship-role specifies that the relationship is unidirectional in navigability and that entity bean that participates in the relationship is "not aware" of the relationship.
  * 
  * @invariant multiplicity != null
  * @invariant roleSource != null
  * @invariant Cascade delete can only be specified in an EJBRelationshipRole element in which the roleSource element specifies a dependent object class. 
  * @invariant Cascade delete can only be specified for an EJBRelationshipRole contained in an EJBRelation in which the other EJBRelationshipRole element specifies a multiplicity of One.
  * 
  * Example:
  * <ejb-relation>
  *   <ejb-relation-name>Product-LineItem<//ejb-relation-name>
  *   <ejb-relationship-role>
  *     <ejb-relationship-role-name>product-has-lineitems<//ejb-relationship-role-name>
  *     <multiplicity>One<//multiplicity>
  *     <relationship-role-source>
  *       <ejb-name>ProductEJB<//ejb-name>
  *     <//relationship-role-source>
  *   <//ejb-relationship-role>
  * ...
  */
 public class EJBRelationshipRoleImpl extends EObjectImpl implements EJBRelationshipRole, EObject, CommonRelationshipRole {
 
 
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	protected static final EList EMPTY_ELIST = new org.eclipse.emf.common.util.BasicEList(0);
 
 	// Need to cache the computed attributes so that they maintain identity
 	protected EList fAttributes = null;
 	protected String fName = null;
 	protected IRoleShapeStrategy roleShapeStrategy;
	private static final String ROLE_STRATEGY_CLASS_NAME = "org.eclipse.jst.j2ee.ejb.internal.impl.EJB20FlattenedRoleShapeStrategy"; //$NON-NLS-1$
 	private static final String COMMON_ROLE_CLASS_NAME = "org.eclipse.jst.j2ee.ejb.CommonRelationshipRole"; //$NON-NLS-1$
 	private static Class COMMON_ROLE_CLASS;
 	private static Class ROLE_STRATEGY_CLASS;
 	private static Constructor ROLE_STRATEGY_CONSTRUCTOR;
 	private static final String NAME_SUFFIX = "Inverse"; //$NON-NLS-1$
     
 	private boolean toStringGuard;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getRoleName() <em>Role Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRoleName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ROLE_NAME_EDEFAULT = null;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected String roleName = ROLE_NAME_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getMultiplicity() <em>Multiplicity</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMultiplicity()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final MultiplicityKind MULTIPLICITY_EDEFAULT = MultiplicityKind.ONE_LITERAL;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected MultiplicityKind multiplicity = MULTIPLICITY_EDEFAULT;
 	/**
 	 * This is true if the Multiplicity attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean multiplicityESet = false;
 
 	/**
 	 * The default value of the '{@link #isCascadeDelete() <em>Cascade Delete</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isCascadeDelete()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean CASCADE_DELETE_EDEFAULT = false;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected boolean cascadeDelete = CASCADE_DELETE_EDEFAULT;
 	/**
 	 * This is true if the Cascade Delete attribute has been set.
 	 * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
   protected boolean cascadeDeleteESet = false;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected RoleSource source = null;
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected CMRField cmrField = null;
 	/**
 	 * The cached value of the '{@link #getDescriptions() <em>Descriptions</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescriptions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList descriptions = null;
 
 	private boolean wasForward = false;
 	private boolean wasMany = false;
     
 	private boolean isKeySet = false;
 	private boolean key = false;
 	
 	private transient Boolean required = null;
 
 	public EJBRelationshipRoleImpl() {
 		super();
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return EjbPackage.eINSTANCE.getEJBRelationshipRole();
 	}
 
 	/**
 	 * Return the persistentAttributes from my Entity that are used
 	 * by this role.
 	 */
 	
 	public EList getAttributes() {
 		if (fAttributes == null) {
 			fAttributes = new BasicEList(1);
 		}
 		if (fAttributes.isEmpty()) {
 			getRoleShapeStrategy().reconcileAttributes(fAttributes);
 		}
 		return fAttributes;
 	}
 	/**
 	 * Return our parent relationship as a CommonRelationship
 	 */
 
 	public CommonRelationship getCommonRelationship() {
 		return getRelationship();
 	}
 	protected IRoleShapeStrategy getDefaultRoleShapeStrategy() {
 		try {
 			return (IRoleShapeStrategy) getRoleStrategyConstructor(this).newInstance(new Object[] { this });
 		} catch (Throwable t) {
 			Logger.getLogger().logError("EJBRelationshipRoleImpl:Could not instantiate role shape strategy; this requires extensions"); //$NON-NLS-1$
 			Logger.getLogger().logError(t);
 			return null;
 		}
 
 	}
 
 
 	/**
 	 * @return The logical name which can be used for naming this role.
 	 * Required to be unique within a JAR.
 	 * If a cmr-field is defined, then a name in the form %source-ejb-name%_%cmr-field-name% is computed.
 	 * Otherwise a default ID is generated.
 	 */
 	public String getLogicalName() {
 		String result = null;
 		String qualifier = getCmrField().getName();
 		if (qualifier != null) {
 			result = getSource().getEntityBean().getName() + "_" + qualifier; //$NON-NLS-1$
 		} else {
 			// Apply a default ID
 			result = IDUtil.getOrAssignID(this);
 		}
 		return result;
 	}
 	/**
 	 * @return The name for this role, derived from the CMR-field name
 	 * Used to determine the name used in querying this role as well as the accessor method names which implement it.
 	 * Note: may return the XMI ID for non-navigable roles, ie - where there is no CMR-field.
 	 */
 	public String getName() {
 		if (fName == null) {
 			if (getCmrField() != null)
 				fName = getCmrField().getName();
 			else if (getOpposite() != null) {
 				EJBRelationshipRole op = getOpposite();
 				if (op.getCmrField() != null)
 					fName = op.getCmrField().getName() + op.getSourceEntity().getName() + NAME_SUFFIX;
 			} else {
 				if (eGetId() == null) 
 					eSetId();
 				fName = eGetId();
 			}
 		}
 		return fName;
 	}
 	/**
 	 * Return the other role.
 	 */
 	public EJBRelationshipRole getOpposite() {
 		if (getRelationship() != null)
 			return getRelationship().getOppositeRole(this);
 		return null;
 	}
 	/**
 	 * Return the other role.
 	 */
 	public CommonRelationshipRole getOppositeAsCommonRole() {
 		return getOpposite();
 	}
 
 	/**
 	 * Insert the method's description here.
 	 * Creation date: (11/15/2000 6:50:30 PM)
 	 * @return com.ibm.ejs.models.base.extensions.ejbext.impl.IRoleShapeStrategy
 	 */
 	public IRoleShapeStrategy getRoleShapeStrategy() {
 		if (roleShapeStrategy == null)
 			roleShapeStrategy = getDefaultRoleShapeStrategy();
 		return roleShapeStrategy;
 	}
 	/**
 	 * @return The Source ejb
 	 * Compute the ContainerManagedEntity which is the source of a role that participates in a relationship. 
 	 */
 	public ContainerManagedEntity getSourceEntity() {
 		if(null == getSource()) {
 			return null;
 		}
 		return getSource().getEntityBean();
 	}
 	/**
 	* @return The computed value of the isForward attribute
 	* Specifies whether this role should be mapped to a persistent reference, such as a foreign key.
 	* The current implementation is:
 	* - if the role is many, it cannot be forward
 	* - if the role is single and not navigable, it cannot be forward
 	* - all other things being equal, the first role is considered to be forward
 	* - clients can manipulate this by setting an ambiguous role to be fwd (by manipulating the role order under the covers).
 	*/
 	public boolean isForward() {
 		if (this.isMany())
 			return false;
 		if (getOppositeAsCommonRole() == null)
 			return wasForward;
 		if (getOppositeAsCommonRole().isMany())
 			return true;
 	   // if (!this.isNavigable())  removed to allow non-navigable role in key
 	   //    return false;
 		if (getRelationship() == null)
 			return wasForward;
 		return (getRelationship().getFirstRole() == this);
 	}
 	/**
 	* @return The computed value isKey
 	*/
 	public boolean isKey() {
 		if (isKeySet)
 			return key;
 		return isKeyFromAttributes();
 	}
     
 	protected boolean isKeyFromAttributes() {
 		List list = getAttributes();
 		if (list.isEmpty())
 			return false;
 		Iterator it = list.iterator();
 		CMPAttribute attribute;
 		while (it.hasNext()) {
 			attribute = (CMPAttribute) it.next();
 			if (!attribute.isKey())
 				return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * This allows you to set the Role as being key so it will
 	 * not traverse it attributes to determine if it is key.
 	 */
 	public void setKey(boolean aBoolean) {
 		key = aBoolean;
 		isKeySet = true;
 	} 
  
 	/**
 	 * Handwritten version of isMany():
 	 */
  
  
 	public boolean isMany()  { 
 		if (getOpposite() == null) 
 			 return wasMany;  
 		return getOpposite().getMultiplicity() == MultiplicityKind.MANY_LITERAL;  
 	} 
  
          
 
 	/**
 	* @return The computed value of the isNavigable attribute
 	* Specifies that accessors should be generated for this relationship role.
 	* Current implementation is to test whether or not a CMR field exists.
 	*/
 	public boolean isNavigable() {
 		return getCmrField() != null;
 	}
 
 
 	public void reconcileAttributes() {
 		if (fAttributes != null)
 			getRoleShapeStrategy().reconcileAttributes(fAttributes);
 	}
 
 
 	/**
 	 * Set this role to be the forward role.
 	 */
 	public void setFoward() {
 		getRelationship().setFoward(this);
 	}
 	public void setName(java.lang.String uniqueName) {
 		fName = uniqueName;
 	}
 	public String toString() {
 		if (toStringGuard) return super.toString();
 		try {
 			toStringGuard = true;
 			String oppositeType = getOpposite() != null ? getTypeEntity().getName() : "<<unknown>>"; //$NON-NLS-1$
 			return getName() + "(" + oppositeType + ")" + (isMany() ? " *" : " 1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 		} catch (Exception e) {
 			return e.toString();
 		} finally {
 			toStringGuard = false;
 		}
 	}
 
 	private static Class getCommonRoleClass(Object caller) throws ClassNotFoundException {
 
 		if (COMMON_ROLE_CLASS == null) {
 			COMMON_ROLE_CLASS = loadClass(COMMON_ROLE_CLASS_NAME, caller);
 		}
 		return COMMON_ROLE_CLASS;
 	}
 
 	private static Class loadClass(String name, Object caller) throws ClassNotFoundException {
 		ClassLoader cl = caller.getClass().getClassLoader();
 		if (cl != null)
 			return cl.loadClass(name);
 		return Class.forName(name);
 	}
 
 	private static Class getRoleStrategyClass(Object caller) throws ClassNotFoundException {
 		if (ROLE_STRATEGY_CLASS == null)
 			ROLE_STRATEGY_CLASS = loadClass(ROLE_STRATEGY_CLASS_NAME, caller);
 		return ROLE_STRATEGY_CLASS;
 	}
 
 	private static Constructor getRoleStrategyConstructor(Object caller) throws ClassNotFoundException, NoSuchMethodException {
 		if (ROLE_STRATEGY_CONSTRUCTOR == null) {
 			Class[] parmTypes = new Class[] { getCommonRoleClass(caller)};
 			ROLE_STRATEGY_CONSTRUCTOR = getRoleStrategyClass(caller).getConstructor(parmTypes);
 		}
 		return ROLE_STRATEGY_CONSTRUCTOR;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 * The description is used by the ejb-jar file producer to provide text describing 
 	 * the ejb relationship role.
 	 * 
 	 * The description should include any information that the ejb-jar file producer
 	 * wants to provide to the consumer of the ejb-jar file (i.e. to the Deployer).
 	 * Typically, the tools used by the ejb-jar file consumer will display the
 	 * description when processing the list of dependents.
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 * Defines a name for a role that is unique within an ejb-relation. Different
 	 * relationships can use the same name for a role.
 
 	 */
 	public String getRoleName() {
 		return roleName;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setRoleName(String newRoleName) {
 		String oldRoleName = roleName;
 		roleName = newRoleName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__ROLE_NAME, oldRoleName, roleName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MultiplicityKind getMultiplicity() {
 		return multiplicity;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMultiplicity(MultiplicityKind newMultiplicity) {
 		MultiplicityKind oldMultiplicity = multiplicity;
 		multiplicity = newMultiplicity == null ? MULTIPLICITY_EDEFAULT : newMultiplicity;
 		boolean oldMultiplicityESet = multiplicityESet;
 		multiplicityESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__MULTIPLICITY, oldMultiplicity, multiplicity, !oldMultiplicityESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetMultiplicity() {
 		MultiplicityKind oldMultiplicity = multiplicity;
 		boolean oldMultiplicityESet = multiplicityESet;
 		multiplicity = MULTIPLICITY_EDEFAULT;
 		multiplicityESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, EjbPackage.EJB_RELATIONSHIP_ROLE__MULTIPLICITY, oldMultiplicity, MULTIPLICITY_EDEFAULT, oldMultiplicityESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetMultiplicity() {
 		return multiplicityESet;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 * Specifies that, within a particular relationship, the lifetime of one or more
 	 * entity beans is dependent upon the lifetime of another entity bean. The
 	 * cascade-delete element can Cascade delete can only be specified for an
 	 * EJBRelationshipRole contained in an EJBrelation in which the other
 	 * EJBRelationshipRole element specifies a multiplicity of One.
 	 * 
 	 * 
 
 	 */
 	public boolean isCascadeDelete() {
 		return cascadeDelete;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setCascadeDelete(boolean newCascadeDelete) {
 		boolean oldCascadeDelete = cascadeDelete;
 		cascadeDelete = newCascadeDelete;
 		boolean oldCascadeDeleteESet = cascadeDeleteESet;
 		cascadeDeleteESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__CASCADE_DELETE, oldCascadeDelete, cascadeDelete, !oldCascadeDeleteESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
 	 * @generated
 	 */
   public void unsetCascadeDelete() {
 		boolean oldCascadeDelete = cascadeDelete;
 		boolean oldCascadeDeleteESet = cascadeDeleteESet;
 		cascadeDelete = CASCADE_DELETE_EDEFAULT;
 		cascadeDeleteESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, EjbPackage.EJB_RELATIONSHIP_ROLE__CASCADE_DELETE, oldCascadeDelete, CASCADE_DELETE_EDEFAULT, oldCascadeDeleteESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
 	 * @generated
 	 */
   public boolean isSetCascadeDelete() {
 		return cascadeDeleteESet;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 */
 	public EJBRelation getRelationship() {
 		if (eContainerFeatureID != EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP) return null;
 		return (EJBRelation)eContainer;
 	}
 
 	/**
 	 * This field/method will be replaced during code generation.
 	 */
 	public void setRelationship(EJBRelation newContainer) {
 		if (newContainer == null && getRelationship() != null) {
 			updateCachedSettings();
 			EJBRelationshipRoleImpl roleOpposite = (EJBRelationshipRoleImpl) getOpposite();
 			if (roleOpposite != null)
 				roleOpposite.updateCachedSettings();
 		} else
 			wasForward = false;
 		setRelationshipGen(newContainer);
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.ejb.CommonRelationshipRole#getTargetAttributeName(org.eclipse.jst.j2ee.internal.ejb.CMPAttribute)
 	 */
 	public String getTargetAttributeName(CMPAttribute roleAttribute) {
 		int roleNameSize = getName().length();
 		return roleAttribute.getName().substring(roleNameSize + 1);
 	}
 	/**
 	 * Method updateCachedSettings.
 	 */
 	protected void updateCachedSettings() {
 		wasForward = isForward();
 		wasMany = isMany();
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 * Designates the source of a role that participates in a relationship. A
 	 * relationship-role-source element uniquely identifies an entity bean.
 	 * @migration EJB1.1: was ibmejbext::EjbRelationshipRole::sourceEJBName, but that
 	 * may not be sufficient, as this can now refer to dependent class names or ejb
 	 * refs
 	 */
 	public RoleSource getSource() {
 		return source;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSource(RoleSource newSource, NotificationChain msgs) {
 		RoleSource oldSource = source;
 		source = newSource;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE, oldSource, newSource);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setSource(RoleSource newSource) {
 		if (newSource != source) {
 			NotificationChain msgs = null;
 			if (source != null)
 				msgs = ((InternalEObject)source).eInverseRemove(this, EjbPackage.ROLE_SOURCE__ROLE, RoleSource.class, msgs);
 			if (newSource != null)
 				msgs = ((InternalEObject)newSource).eInverseAdd(this, EjbPackage.ROLE_SOURCE__ROLE, RoleSource.class, msgs);
 			msgs = basicSetSource(newSource, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE, newSource, newSource));
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 * A declaration of the CMRField, if any, by means of which the other side of the
 	 * relationship is accessed from the perspective of the role source
 	 * 
 	 * @migration EJB1.1: Used to be handled via ibmejbext::EjbRole::attributes list
 	 */
 	public CMRField getCmrField() {
 		return cmrField;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetCmrField(CMRField newCmrField, NotificationChain msgs) {
 		CMRField oldCmrField = cmrField;
 		cmrField = newCmrField;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD, oldCmrField, newCmrField);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setCmrField(CMRField newCmrField) {
 		if (newCmrField != cmrField) {
 			NotificationChain msgs = null;
 			if (cmrField != null)
 				msgs = ((InternalEObject)cmrField).eInverseRemove(this, EjbPackage.CMR_FIELD__ROLE, CMRField.class, msgs);
 			if (newCmrField != null)
 				msgs = ((InternalEObject)newCmrField).eInverseAdd(this, EjbPackage.CMR_FIELD__ROLE, CMRField.class, msgs);
 			msgs = basicSetCmrField(newCmrField, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD, newCmrField, newCmrField));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getDescriptions() {
 		if (descriptions == null) {
 			descriptions = new EObjectContainmentEList(Description.class, this, EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTIONS);
 		}
 		return descriptions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP:
 					if (eContainer != null)
 						msgs = eBasicRemoveFromContainer(msgs);
 					return eBasicSetContainer(otherEnd, EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP, msgs);
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE:
 					if (source != null)
 						msgs = ((InternalEObject)source).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE, null, msgs);
 					return basicSetSource((RoleSource)otherEnd, msgs);
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD:
 					if (cmrField != null)
 						msgs = ((InternalEObject)cmrField).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD, null, msgs);
 					return basicSetCmrField((CMRField)otherEnd, msgs);
 				default:
 					return eDynamicInverseAdd(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		if (eContainer != null)
 			msgs = eBasicRemoveFromContainer(msgs);
 		return eBasicSetContainer(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP:
 					return eBasicSetContainer(null, EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP, msgs);
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE:
 					return basicSetSource(null, msgs);
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD:
 					return basicSetCmrField(null, msgs);
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTIONS:
 					return ((InternalEList)getDescriptions()).basicRemove(otherEnd, msgs);
 				default:
 					return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		return eBasicSetContainer(null, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eBasicRemoveFromContainer(NotificationChain msgs) {
 		if (eContainerFeatureID >= 0) {
 			switch (eContainerFeatureID) {
 				case EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP:
 					return eContainer.eInverseRemove(this, EjbPackage.EJB_RELATION__RELATIONSHIP_ROLES, EJBRelation.class, msgs);
 				default:
 					return eDynamicBasicRemoveFromContainer(msgs);
 			}
 		}
 		return eContainer.eInverseRemove(this, EOPPOSITE_FEATURE_BASE - eContainerFeatureID, null, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTION:
 				return getDescription();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__ROLE_NAME:
 				return getRoleName();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__MULTIPLICITY:
 				return getMultiplicity();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CASCADE_DELETE:
 				return isCascadeDelete() ? Boolean.TRUE : Boolean.FALSE;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP:
 				return getRelationship();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE:
 				return getSource();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD:
 				return getCmrField();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTIONS:
 				return getDescriptions();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__ROLE_NAME:
 				return ROLE_NAME_EDEFAULT == null ? roleName != null : !ROLE_NAME_EDEFAULT.equals(roleName);
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__MULTIPLICITY:
 				return isSetMultiplicity();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CASCADE_DELETE:
 				return isSetCascadeDelete();
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP:
 				return getRelationship() != null;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE:
 				return source != null;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD:
 				return cmrField != null;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTIONS:
 				return descriptions != null && !descriptions.isEmpty();
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTION:
 				setDescription((String)newValue);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__ROLE_NAME:
 				setRoleName((String)newValue);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__MULTIPLICITY:
 				setMultiplicity((MultiplicityKind)newValue);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CASCADE_DELETE:
 				setCascadeDelete(((Boolean)newValue).booleanValue());
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP:
 				setRelationship((EJBRelation)newValue);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE:
 				setSource((RoleSource)newValue);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD:
 				setCmrField((CMRField)newValue);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTIONS:
 				getDescriptions().clear();
 				getDescriptions().addAll((Collection)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__ROLE_NAME:
 				setRoleName(ROLE_NAME_EDEFAULT);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__MULTIPLICITY:
 				unsetMultiplicity();
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CASCADE_DELETE:
 				unsetCascadeDelete();
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP:
 				setRelationship((EJBRelation)null);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__SOURCE:
 				setSource((RoleSource)null);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__CMR_FIELD:
 				setCmrField((CMRField)null);
 				return;
 			case EjbPackage.EJB_RELATIONSHIP_ROLE__DESCRIPTIONS:
 				getDescriptions().clear();
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public String toStringGen() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (description: ");
 		result.append(description);
 		result.append(", roleName: ");
 		result.append(roleName);
 		result.append(", multiplicity: ");
 		if (multiplicityESet) result.append(multiplicity); else result.append("<unset>");
 		result.append(", cascadeDelete: ");
 		if (cascadeDeleteESet) result.append(cascadeDelete); else result.append("<unset>");
 		result.append(')');
 		return result.toString();
 	}
 
 	/*
 	 * @see CommonRelationshipRole#getTypeEntity()
 	 */
 	public ContainerManagedEntity getTypeEntity() {
 		if (getOpposite() != null)
 			return getOpposite().getSourceEntity();
 		return null;
 	}
 
 	/**
 	 * @see CommonRelationshipRole#isRequired()
 	 */
 	public boolean isRequired() {
 		if (required!=null)
 			return required.booleanValue();
 		return isKey();
 	}
 	
 	/**
 	 * Set the isRequired flag for bottom up
 	 */
 	public void setRequired(Boolean isRequired) {
 		required = isRequired;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setRelationshipGen(EJBRelation newRelationship) {
 		if (newRelationship != eContainer || (eContainerFeatureID != EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP && newRelationship != null)) {
 			if (EcoreUtil.isAncestor(this, newRelationship))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eContainer != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newRelationship != null)
 				msgs = ((InternalEObject)newRelationship).eInverseAdd(this, EjbPackage.EJB_RELATION__RELATIONSHIP_ROLES, EJBRelation.class, msgs);
 			msgs = eBasicSetContainer((InternalEObject)newRelationship, EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.EJB_RELATIONSHIP_ROLE__RELATIONSHIP, newRelationship, newRelationship));
 	}
 
 	/**
 	 * @see org.eclipse.jst.j2ee.internal.ejb.CommonRelationshipRole#setLower(int)
 	 */
 	public void setLower(int lowerBound) {
 		// Do nothing...  Only upperbound is relevant here
 	}
 
 	/**
 	 * @see org.eclipse.jst.j2ee.internal.ejb.CommonRelationshipRole#setUpper(int)
 	 */
 	public void setUpper(int upperBound) {
 		switch (upperBound) {
 			case MultiplicityKind.ONE :
 				setMultiplicity(MultiplicityKind.ONE_LITERAL);
 				break;
 			default :
 				setMultiplicity(MultiplicityKind.MANY_LITERAL);
 				break;
 		}
 	}
 	
 	/**
 	  Gets the MOF XMI ref id for this object.
 	 */
 	private String eGetId() {
 		XMIResource res = (XMIResource)eResource();
 		if (res == null)
 			return null;
 		return res.getID(this);
 	}
 
 	
 	/**
 	 * Set and return The id of this element
 	 */
 	private String eSetId() {
 
 
 		IDUtil.getOrAssignID(this);
 
 		return eGetId();
 	}
 
 }
 
