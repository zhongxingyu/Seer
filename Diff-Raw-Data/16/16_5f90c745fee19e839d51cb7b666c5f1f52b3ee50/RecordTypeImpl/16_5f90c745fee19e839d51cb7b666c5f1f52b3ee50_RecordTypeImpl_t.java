 /**
  * Copyright (c) 2011 NumberFour AG
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  */
 package org.eclipse.dltk.javascript.typeinfo.model.impl;
 
 import java.util.Collection;
 
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.RecordType;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelPackage;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Record Type</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.dltk.javascript.typeinfo.model.impl.RecordTypeImpl#getMembers <em>Members</em>}</li>
  *   <li>{@link org.eclipse.dltk.javascript.typeinfo.model.impl.RecordTypeImpl#getTypeName <em>Type Name</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class RecordTypeImpl extends EObjectImpl implements RecordType {
 	/**
      * The cached value of the '{@link #getMembers() <em>Members</em>}' containment reference list.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see #getMembers()
      * @generated
      * @ordered
      */
     protected EList<Member> members;
     /**
      * The default value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see #getTypeName()
      * @generated
      * @ordered
      */
     protected static final String TYPE_NAME_EDEFAULT = null;
     /**
      * The cached value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see #getTypeName()
      * @generated
      * @ordered
      */
     protected String typeName = TYPE_NAME_EDEFAULT;
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     protected RecordTypeImpl() {
         super();
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     protected EClass eStaticClass() {
         return TypeInfoModelPackage.Literals.RECORD_TYPE;
     }
 
 	/**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
 	public EList<Member> getMembers() {
         if (members == null) {
             members = new EObjectContainmentEList<Member>(Member.class, this, TypeInfoModelPackage.RECORD_TYPE__MEMBERS);
         }
         return members;
     }
 
 	/**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     public String getTypeName() {
         return typeName;
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     public void setTypeName(String newTypeName) {
         String oldTypeName = typeName;
         typeName = newTypeName;
         if (eNotificationRequired())
             eNotify(new ENotificationImpl(this, Notification.SET, TypeInfoModelPackage.RECORD_TYPE__TYPE_NAME, oldTypeName, typeName));
     }
 
     /**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public String getName() {
 		if (typeName != null) {
 			return typeName;
 		}
 		final StringBuilder sb = new StringBuilder();
 		sb.append('{');
		if (members != null) {
			for (Member member : members) {
				if (sb.length() > 1) {
					sb.append(',');
				}
				sb.append(member.getName());
				if (member.getType() != null) {
					sb.append(':').append(member.getType().getName());
				}
 			}
 		}
 		sb.append('}');
 		return sb.toString();
 	}
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
         switch (featureID) {
             case TypeInfoModelPackage.RECORD_TYPE__MEMBERS:
                 return ((InternalEList<?>)getMembers()).basicRemove(otherEnd, msgs);
         }
         return super.eInverseRemove(otherEnd, featureID, msgs);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public Object eGet(int featureID, boolean resolve, boolean coreType) {
         switch (featureID) {
             case TypeInfoModelPackage.RECORD_TYPE__MEMBERS:
                 return getMembers();
             case TypeInfoModelPackage.RECORD_TYPE__TYPE_NAME:
                 return getTypeName();
         }
         return super.eGet(featureID, resolve, coreType);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @SuppressWarnings("unchecked")
     @Override
     public void eSet(int featureID, Object newValue) {
         switch (featureID) {
             case TypeInfoModelPackage.RECORD_TYPE__MEMBERS:
                 getMembers().clear();
                 getMembers().addAll((Collection<? extends Member>)newValue);
                 return;
             case TypeInfoModelPackage.RECORD_TYPE__TYPE_NAME:
                 setTypeName((String)newValue);
                 return;
         }
         super.eSet(featureID, newValue);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public void eUnset(int featureID) {
         switch (featureID) {
             case TypeInfoModelPackage.RECORD_TYPE__MEMBERS:
                 getMembers().clear();
                 return;
             case TypeInfoModelPackage.RECORD_TYPE__TYPE_NAME:
                 setTypeName(TYPE_NAME_EDEFAULT);
                 return;
         }
         super.eUnset(featureID);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public boolean eIsSet(int featureID) {
         switch (featureID) {
             case TypeInfoModelPackage.RECORD_TYPE__MEMBERS:
                 return members != null && !members.isEmpty();
             case TypeInfoModelPackage.RECORD_TYPE__TYPE_NAME:
                 return TYPE_NAME_EDEFAULT == null ? typeName != null : !TYPE_NAME_EDEFAULT.equals(typeName);
         }
         return super.eIsSet(featureID);
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     @Override
     public String toString() {
         if (eIsProxy()) return super.toString();
 
         StringBuffer result = new StringBuffer(super.toString());
         result.append(" (typeName: "); //$NON-NLS-1$
         result.append(typeName);
         result.append(')');
         return result.toString();
     }
 
 } //RecordTypeImpl
