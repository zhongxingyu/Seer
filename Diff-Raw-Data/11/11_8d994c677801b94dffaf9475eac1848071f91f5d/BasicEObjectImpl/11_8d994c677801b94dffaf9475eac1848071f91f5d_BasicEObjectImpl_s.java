 /**
  * <copyright>
  *
  * Copyright (c) 2002-2005 IBM Corporation and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: 
  *   IBM - Initial API and implementation
  *
  * </copyright>
  *
 * $Id: BasicEObjectImpl.java,v 1.11 2005/11/18 19:43:09 emerks Exp $
  */
 package org.eclipse.emf.ecore.impl;
 
 
 import java.util.Iterator;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.notify.impl.BasicNotifierImpl;
 import org.eclipse.emf.common.notify.impl.NotificationChainImpl;
 import org.eclipse.emf.common.util.AbstractTreeIterator;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EContentsEList;
 import org.eclipse.emf.ecore.util.ECrossReferenceEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 
 /**
  * An implementation of the model object '<em><b>EObject</b></em>'.
  */
 public class BasicEObjectImpl extends BasicNotifierImpl implements EObject, InternalEObject
 {
   /**
    * An internal interface for holding less frequently members variables.
    */
   protected interface EPropertiesHolder extends EStructuralFeature.Internal.DynamicValueHolder
   {
     EClass getEClass();
     void setEClass(EClass eClass);
 
     URI getEProxyURI();
     void setEProxyURI(URI eProxyURI);
 
     Resource.Internal getEResource();
     void setEResource(Resource.Internal eResource);
 
     EList getEContents();
     void setEContents(EList eContents);
 
     EList getECrossReferences();
     void setECrossReferences(EList eCrossReferences);
 
     boolean hasSettings();
     void allocateSettings(int dynamicFeatureCount);
   }
 
   /**
    * An internal class for holding less frequently members variables.
    */
   protected static class EPropertiesHolderImpl implements EPropertiesHolder
   {
     protected EClass eClass;
     protected URI eProxyURI;
     protected Resource.Internal eResource;
     protected EList eContents;
     protected EList eCrossReferences;
     protected Object [] eSettings;
 
     protected static final Object [] NO_SETTINGS = new Object [0];
 
     public EClass getEClass()
     {
       return eClass;
     }
 
     public void setEClass(EClass eClass)
     {
       this.eClass = eClass;
     }
 
     public URI getEProxyURI()
     {
       return eProxyURI;
     }
 
     public void setEProxyURI(URI eProxyURI)
     {
       this.eProxyURI = eProxyURI;
     }
 
     public Resource.Internal getEResource()
     {
       return eResource;
     }
 
     public void setEResource(Resource.Internal eResource)
     {
       this.eResource = eResource;
     }
 
     public EList getEContents()
     {
       return eContents;
     }
 
     public void setEContents(EList eContents)
     {
       this.eContents = eContents;
     }
 
     public EList getECrossReferences()
     {
       return eCrossReferences;
     }
 
     public void setECrossReferences(EList eCrossReferences)
     {
       this.eCrossReferences = eCrossReferences;
     }
 
     public boolean hasSettings()
     {
       return eSettings != null;
     }
 
     public void allocateSettings(int dynamicFeatureCount)
     {
       eSettings = dynamicFeatureCount == 0 ? NO_SETTINGS : new Object [dynamicFeatureCount];
     }
 
     public Object dynamicGet(int dynamicFeatureID)
     {
       return eSettings[dynamicFeatureID];
     }
 
     public void dynamicSet(int dynamicFeatureID, Object value)
     {
       eSettings[dynamicFeatureID] = value;
     }
 
     public void dynamicUnset(int dynamicFeatureID)
     {
       eSettings[dynamicFeatureID] = null;
     }
   }
 
   /**
    * Creates a basic EObject.
    */
   protected BasicEObjectImpl() 
   {
     super();
   }
 
   protected int eStaticFeatureCount()
   {
     return eStaticClass().getFeatureCount();
   }
 
   protected EPropertiesHolder eProperties()
   {
     throw new UnsupportedOperationException();
     // if (eProperties == null)
     // {
       // eProperties = new EPropertiesHolderImpl();
     // }
     // return eProperties;
   }
 
   protected EPropertiesHolder eBasicProperties()
   {
     throw new UnsupportedOperationException();
     // return eProperties;
   }
 
   protected boolean eHasSettings()
   {
     EPropertiesHolder eProperties = eBasicProperties();
     return eProperties != null && eProperties.hasSettings();
   }
 
   protected EStructuralFeature.Internal.DynamicValueHolder eSettings()
   {
     if (!eHasSettings())
     {
       int size =  eClass().getFeatureCount() - eStaticFeatureCount();
       eProperties().allocateSettings(size);
     }
 
     return eBasicProperties();
   }
 
   protected int eDynamicFeatureID(EStructuralFeature eStructuralFeature)
   {
     return eClass().getFeatureID(eStructuralFeature) - eStaticFeatureCount();
   }
 
   protected EStructuralFeature eDynamicFeature(int dynamicFeatureID)
   {
     return eClass().getEStructuralFeature(dynamicFeatureID + eStaticFeatureCount());
   }
 
   public String eURIFragmentSegment(EStructuralFeature eStructuralFeature, EObject eObject)
   {
     if (eStructuralFeature == null)
     {
       for (EContentsEList.FeatureIterator crossReferences = 
              (EContentsEList.FeatureIterator)((InternalEList)eCrossReferences()).basicIterator(); 
            crossReferences.hasNext(); )
       {
         EObject crossReference = (EObject)crossReferences.next();
         if (crossReference == eObject)
         {
           eStructuralFeature = crossReferences.feature();
         }
       }
     }
 
     if (eStructuralFeature instanceof EAttribute)
     {
       FeatureMap featureMap = (FeatureMap)eGet(eStructuralFeature, false);
       for (int i = 0, size = featureMap.size(); i < size; ++i)
       {
         if (featureMap.getValue(i) == eObject)
         {
           EStructuralFeature entryFeature = featureMap.getEStructuralFeature(i);
           if (entryFeature instanceof EReference && ((EReference)entryFeature).isContainment())
           {
             return '@' + eStructuralFeature.getName() + '.' + i;
           }
         }
       }
       return '@' + eStructuralFeature.getName() + ".-1";
     }
     else if (eStructuralFeature.isMany())
     {
       EList eList = (EList)eGet(eStructuralFeature, false);
       int index = eList.indexOf(eObject);
       return '@' + eStructuralFeature.getName() + '.' + index;
     }
     else
     {
       return '@' + eStructuralFeature.getName();
     }
   }
 
   public EObject eObjectForURIFragmentSegment(String uriFragmentSegment)
   {
     int dotIndex = uriFragmentSegment.indexOf(".");
     if (dotIndex == -1)
     {
       String name = uriFragmentSegment.substring(1);
       EStructuralFeature eStructuralFeature = eClass().getEStructuralFeature(name);
       if (eStructuralFeature == null)
       {
         throw new IllegalArgumentException("The feature '" + name + "' is not a valid feature");
       }
       return (EObject)eGet(eStructuralFeature, false);
     }
     else
     {
       String name = uriFragmentSegment.substring(1, dotIndex);
       EStructuralFeature eStructuralFeature = eClass().getEStructuralFeature(name);
       if (eStructuralFeature == null)
       {
         throw new IllegalArgumentException("The feature '" + name + "' is not a valid feature");
       }
       EList eList = (EList)eGet(eStructuralFeature, false);
       int position = 0;
       try
       {
         position = Integer.parseInt(uriFragmentSegment.substring(dotIndex + 1));
       }
       catch (NumberFormatException exception)
       {
         throw new WrappedException(exception);
       }
       if (position < eList.size())
       {
         Object result = eList.get(position);
         if (result instanceof FeatureMap.Entry)
         {
           result = ((FeatureMap.Entry)result).getValue();
         }
         return (EObject)result;
       }
       else
       {
         return null;
       }
     }
   }
 
   public boolean eContains(EObject eObject)
   {
     return EcoreUtil.isAncestor(this, eObject);
   }
 
   public EObject eContainer()
   {
     InternalEObject result = eInternalContainer();
     if (result != null) 
     {
       int eContainerFeatureID = eContainerFeatureID();
       if (result.eIsProxy())
       {
         EObject resolved = eResolveProxy(result);
         if (resolved != result)
         {
           NotificationChain notificationChain = eBasicRemoveFromContainer(null);
           eBasicSetContainer((InternalEObject)resolved, eContainerFeatureID);
           if (notificationChain != null)
           {
             notificationChain.dispatch();
           }
           if (eNotificationRequired() && eContainerFeatureID > EOPPOSITE_FEATURE_BASE)
           {
             eNotify(new ENotificationImpl(this, Notification.RESOLVE, eContainerFeatureID, result, resolved));
           }
           return resolved;
         }
       }
     }
     return result;
   }
 
   public InternalEObject eInternalContainer()
   {
     throw new UnsupportedOperationException();
     //return eContainer;
   }
 
   public int eContainerFeatureID()
   {
     throw new UnsupportedOperationException();
     // return eContainerFeatureID;
   }
 
   protected void eBasicSetContainer(InternalEObject newContainer, int newContainerFeatureID)
   {
     throw new UnsupportedOperationException();
     // eContainer = newContainer;
     // eContainerFeatureID = newContainerFeatureID;
   }
 
   public EList eContents()
   {
     EList result = eProperties().getEContents();
     if (result == null)
     {
       eBasicProperties().setEContents(result = new EContentsEList(this));
     }
 
     return result;
   }
 
   public EList eCrossReferences()
   {
     EList result = eProperties().getECrossReferences();
     if (result == null)
     {
       eBasicProperties().setECrossReferences(result = new ECrossReferenceEList(this));
     }
 
     return result;
   }
 
   public TreeIterator eAllContents()
   {
     return 
       new AbstractTreeIterator(this, false)
       {
         public Iterator getChildren(Object object)
         {
           return ((EObject)object).eContents().iterator();
         }
       };
   }
 
   public EReference eContainmentFeature()
   {
     return eContainmentFeature(this, eInternalContainer(), eContainerFeatureID());
   }
   
   protected static EReference eContainmentFeature(EObject eObject, EObject eContainer, int eContainerFeatureID)
   {
     if (eContainer == null)
     {
       return null;
     }
     else
     {
       if (eContainerFeatureID <= EOPPOSITE_FEATURE_BASE)
       {
         EStructuralFeature eFeature =  eContainer.eClass().getEStructuralFeature(EOPPOSITE_FEATURE_BASE - eContainerFeatureID);
         if (eFeature instanceof EReference)
         {
           return (EReference)eFeature;
         }
         else
         {
           FeatureMap featureMap = (FeatureMap)eContainer.eGet(eFeature);
           for (int i = 0, size = featureMap.size(); i < size; ++i)
           {
             if (featureMap.getValue(i) == eObject)
             {
               EStructuralFeature entryFeature = featureMap.getEStructuralFeature(i);
               if (entryFeature instanceof EReference)
               {
                 EReference entryReference = (EReference)entryFeature;
                 if (entryReference.isContainment())
                 {
                   return entryReference;
                 }
               }
             }
           }
           throw new IllegalStateException("The containment feature could not be located");
         }
       }
       else
       {
         return ((EReference)eObject.eClass().getEStructuralFeature(eContainerFeatureID)).getEOpposite();
       }
     }
   }
 
   public EStructuralFeature eContainingFeature()
   {
     EObject eContainer = eInternalContainer();
     if (eContainer == null)
     {
       return null;
     }
     else
     {
       int eContainerFeatureID = eContainerFeatureID();
       return
         eContainerFeatureID <= EOPPOSITE_FEATURE_BASE ?
           eContainer.eClass().getEStructuralFeature(EOPPOSITE_FEATURE_BASE - eContainerFeatureID) :
           ((EReference)eClass().getEStructuralFeature(eContainerFeatureID)).getEOpposite();
     }
   }
 
   public Resource.Internal eDirectResource()
   {
     return eBasicProperties() == null ? null : eBasicProperties().getEResource();
   }
 
   public Resource eResource()
   {
     return eInternalResource();
   }
 
   public Resource.Internal eInternalResource()
   {
     Resource.Internal result = eDirectResource();
     if (result == null) 
     {
       InternalEObject eContainer = eInternalContainer();
       if (eContainer != null)
       {
         result = eContainer.eInternalResource();
       }
     }
     return result;
   }
 
   public NotificationChain eSetResource(Resource.Internal resource, NotificationChain notifications)
   {
     Resource oldResource = eDirectResource();
     if (oldResource != null)
     {
       notifications = ((InternalEList)oldResource.getContents()).basicRemove(this, notifications);
     }
     InternalEObject oldContainer = eInternalContainer();
     if (oldContainer != null)
     {
       if (eContainmentFeature().isResolveProxies())
       {
         Resource.Internal oldContainerResource = oldContainer.eInternalResource();
         if (oldContainerResource != null)
         {
           oldContainerResource.detached(this);
         }
       }
       else
       {
         notifications = eBasicRemoveFromContainer(notifications);
         notifications = eBasicSetContainer(null, -1, notifications);
       }
     }
 
     eSetDirectResource(resource);
 
     return notifications;
   }
 
   protected void eSetDirectResource(Resource.Internal resource)
   {
     eProperties().setEResource(resource);
   }
 
   public Object eGet(EStructuralFeature eFeature)
   {
     return eGet(eFeature, true);
   }
 
   public Object eGet(EStructuralFeature eFeature, boolean resolve)
   {
     return eDynamicGet(eFeature, resolve);
   }
 
   public Object eDynamicGet(EStructuralFeature eFeature, boolean resolve)
   {
     int dynamicFeatureID = eDynamicFeatureID(eFeature);
     return
       dynamicFeatureID <= -1 ?
         eOpenGet(eFeature, resolve) :
         eSettingDelegate(eFeature).dynamicGet(this, eSettings(), dynamicFeatureID, resolve);
   }
 
   public Object eOpenGet(EStructuralFeature eFeature, boolean resolve)
   {
     EStructuralFeature openFeature = ExtendedMetaData.INSTANCE.getAffiliation(eClass(), eFeature);
     if (openFeature != null)
     {
       if (!FeatureMapUtil.isFeatureMap(openFeature))
       {
         openFeature = ExtendedMetaData.INSTANCE.getGroup(openFeature);
       }
       FeatureMap featureMap = (FeatureMap)eGet(openFeature);
       return ((FeatureMap.Internal)featureMap).get(eFeature, resolve);
     }
     else
     {
       throw new IllegalArgumentException("The feature '" + eFeature.getName() + "' is not a valid feature");
     }
   }
 
   public void eSet(EStructuralFeature eFeature, Object newValue) 
   {
     eDynamicSet(eFeature, newValue);
   }
 
   public void eDynamicSet(EStructuralFeature eFeature, Object newValue) 
   {
     if (!eFeature.isChangeable())
     {
       throw new IllegalArgumentException("The feature '" + eFeature.getName() + "' is not a valid changeable feature");
     }
     int dynamicFeatureID = eDynamicFeatureID(eFeature);
     if (dynamicFeatureID <= -1)
     {
       eOpenSet(eFeature, newValue);
     }
     else
     {
       eSettingDelegate(eFeature).dynamicSet(this, eSettings(), dynamicFeatureID, newValue);
     }
   }
 
   public void eOpenSet(EStructuralFeature eFeature, Object newValue) 
   {
     EStructuralFeature openFeature = ExtendedMetaData.INSTANCE.getAffiliation(eClass(), eFeature);
     if (openFeature != null)
     {
       if (!FeatureMapUtil.isFeatureMap(openFeature))
       {
         openFeature = ExtendedMetaData.INSTANCE.getGroup(openFeature);
       }
       FeatureMap featureMap = (FeatureMap)eGet(openFeature);
       ((FeatureMap.Internal)featureMap).set(eFeature, newValue);
     }
     else
     {
       throw new IllegalArgumentException("The feature '" + eFeature.getName() + "' is not a valid changeable feature");
     }
   }
 
   public void eUnset(EStructuralFeature eFeature) 
   {
     eDynamicUnset(eFeature);
   }
 
   public void eDynamicUnset(EStructuralFeature eFeature) 
   {
     if (!eFeature.isChangeable())
     {
       throw new IllegalArgumentException("The feature '" + eFeature.getName() + "' is not a valid changeable feature");
     }
     int dynamicFeatureID = eDynamicFeatureID(eFeature);
     if (dynamicFeatureID <= -1)
     {
       eOpenUnset(eFeature);
     }
     else
     {
       eSettingDelegate(eFeature).dynamicUnset(this, eSettings(), dynamicFeatureID);
     }
   }
 
   public void eOpenUnset(EStructuralFeature eFeature) 
   {
     EStructuralFeature openFeature = ExtendedMetaData.INSTANCE.getAffiliation(eClass(), eFeature);
     if (openFeature != null)
     {
       if (!FeatureMapUtil.isFeatureMap(openFeature))
       {
         openFeature = ExtendedMetaData.INSTANCE.getGroup(openFeature);
       }
       FeatureMap featureMap = (FeatureMap)eGet(openFeature);
       ((FeatureMap.Internal)featureMap).unset(eFeature);
     }
     else
     {
       throw new IllegalArgumentException("The feature '" + eFeature.getName() + "' is not a valid changeable feature");
     }
   }
 
   public boolean eIsSet(EStructuralFeature eFeature) 
   {
     return eDynamicIsSet(eFeature);
   }
 
   public boolean eDynamicIsSet(EStructuralFeature eFeature) 
   {
     int dynamicFeatureID = eDynamicFeatureID(eFeature);
     return
       dynamicFeatureID <= -1 ?
         eOpenIsSet(eFeature) :
         eHasSettings() && eSettingDelegate(eFeature).dynamicIsSet(this, eSettings(), dynamicFeatureID);
   }
 
   public boolean eOpenIsSet(EStructuralFeature eFeature) 
   {
     EStructuralFeature openFeature = ExtendedMetaData.INSTANCE.getAffiliation(eClass(), eFeature);
     if (openFeature != null)
     {
       if (!FeatureMapUtil.isFeatureMap(openFeature))
       {
         openFeature = ExtendedMetaData.INSTANCE.getGroup(openFeature);
       }
       FeatureMap featureMap = (FeatureMap)eGet(openFeature);
       return ((FeatureMap.Internal)featureMap).isSet(eFeature);
     }
     else
     {
       throw new IllegalArgumentException("The feature '" + eFeature.getName() + "' is not a valid feature");
     }
   }
 
   public NotificationChain eBasicSetContainer(InternalEObject newContainer, int newContainerFeatureID, NotificationChain msgs)
   {
     InternalEObject oldContainer = eInternalContainer();
     Resource.Internal oldResource = this.eDirectResource();
     Resource.Internal newResource = null;
     if (oldResource != null)
     {
       if (!eContainmentFeature(this, newContainer, newContainerFeatureID).isResolveProxies())
       {
         msgs = ((InternalEList)oldResource.getContents()).basicRemove(this, msgs);
         eSetDirectResource(null);
         if (newContainer != null)
         {
           newResource = newContainer.eInternalResource();
         }
       }
       oldResource = null;
     }
    else if (oldContainer != null)
     {
      oldResource = oldContainer.eInternalResource();
       if (newContainer != null)
       {
         newResource = newContainer.eInternalResource();
       }
     }
 
     if (oldResource != newResource && oldResource != null)
     {
       oldResource.detached(this);
     }
 
     int oldContainerFeatureID = eContainerFeatureID();
     eBasicSetContainer(newContainer, newContainerFeatureID);
 
     if (oldResource != newResource && newResource != null)
     {
       newResource.attached(this);
     }
 
     if (eNotificationRequired())
     {
       if (msgs == null) msgs = new NotificationChainImpl(4);
       if (oldContainer != null && oldContainerFeatureID >=0 && oldContainerFeatureID != newContainerFeatureID)
       {
         msgs.add
           (new ENotificationImpl
             (this,
              Notification.SET,
              oldContainerFeatureID, 
              oldContainer,
              null));
       }
       if (newContainerFeatureID >= 0)
       {
         msgs.add
           (new ENotificationImpl
             (this,
              Notification.SET,
              newContainerFeatureID, 
              oldContainerFeatureID == newContainerFeatureID ? oldContainer : null,
              newContainer));
       }
     }
     return msgs;
   }
 
   public NotificationChain eBasicRemoveFromContainer(NotificationChain msgs)
   {
     int eContainerFeatureID = eContainerFeatureID();
     if (eContainerFeatureID >= 0)
     {
       return eDynamicBasicRemoveFromContainer(msgs);
     }
     else 
     {
       return eInternalContainer().eInverseRemove(this, EOPPOSITE_FEATURE_BASE - eContainerFeatureID, null, msgs);
     }
   }
 
   public NotificationChain eDynamicBasicRemoveFromContainer(NotificationChain msgs)
   {
     EReference inverseFeature = ((EReference)eClass().getEStructuralFeature(eContainerFeatureID())).getEOpposite();
     return eInternalContainer().eInverseRemove(this, inverseFeature.getFeatureID(), inverseFeature.getContainerClass(), msgs);
   }
 
   public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs)
   {
     if (featureID >= 0)
     {
       return eDynamicInverseAdd(otherEnd, featureID, baseClass, msgs);
     }
     else
     {
       if (eInternalContainer() != null)
       {
         msgs = eBasicRemoveFromContainer(msgs);
       }
       return eBasicSetContainer(otherEnd, featureID, msgs);
     }
   }
 
   public NotificationChain eDynamicInverseAdd(InternalEObject otherEnd, int featureID, Class inverseClass, NotificationChain msgs)
   {
     EStructuralFeature.Internal feature = (EStructuralFeature.Internal)eClass().getEStructuralFeature(featureID);
     return 
       feature.getSettingDelegate().dynamicInverseAdd
         (this, eSettings(), featureID - eStaticFeatureCount(), otherEnd, msgs);
   }
 
 
   public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs)
   {
     if (featureID >= 0)
     {
       return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
     }
     else
     {
       return eBasicSetContainer(null, featureID, msgs);
     }
   }
 
   public NotificationChain eDynamicInverseRemove(InternalEObject otherEnd, int featureID, Class inverseClass, NotificationChain msgs)
   {
     EStructuralFeature.Internal feature = (EStructuralFeature.Internal)eClass().getEStructuralFeature(featureID);
     return feature.getSettingDelegate().dynamicInverseRemove
       (this, eSettings(), featureID - eStaticFeatureCount(), otherEnd, msgs);
   }
 
   public URI eProxyURI()
   {
     return eBasicProperties() == null ? null : eBasicProperties().getEProxyURI();
   }
 
   public void eSetProxyURI(URI uri)
   {
     eProperties().setEProxyURI(uri);
   }
 
   public EObject eResolveProxy(InternalEObject proxy)
   {
     return EcoreUtil.resolve(proxy, this);
   }
 
   public boolean eIsProxy()
   {
     return eBasicProperties() != null && eBasicProperties().getEProxyURI() != null;
   }
 
   public int eBaseStructuralFeatureID(int derivedFeatureID, Class baseClass)
   {
     return derivedFeatureID;
   }
 
   public int eDerivedStructuralFeatureID(int baseFeatureID, Class baseClass)
   {
     return baseFeatureID;
   }
 
   public int eDerivedStructuralFeatureID(EStructuralFeature eStructuralFeature)
   {
     Class containerClass = eStructuralFeature.getContainerClass();
     return 
       containerClass == null ? 
         eClass().getFeatureID(eStructuralFeature) : 
         eDerivedStructuralFeatureID(eStructuralFeature.getFeatureID(), containerClass);
   }
 
   public EClass eClass()
   {
     if (eBasicProperties() != null)
     {
       EClass result = eBasicProperties().getEClass();
       if (result != null)
       {
         return result;
       }
     }
     return eStaticClass();
   }
 
   // Subclasses MUST override this function
   protected EClass eStaticClass()
   {
     return EcorePackage.eINSTANCE.getEObject();
   }
 
   protected EClass eDynamicClass()
   {
     return 
       eBasicProperties() == null ?
         null : 
         eBasicProperties().getEClass();
 
   }
 
   public void eSetClass(EClass eClass)
   {
     eProperties().setEClass(eClass);
   }
 
   protected EStructuralFeature.Internal.SettingDelegate eSettingDelegate(EStructuralFeature eFeature)
   {
     return ((EStructuralFeature.Internal)eFeature).getSettingDelegate();
   }
 
   public EStructuralFeature.Setting eSetting(final EStructuralFeature eFeature)
   {
     int index = eClass().getFeatureID(eFeature);
     int dynamicIndex = eStaticFeatureCount();
     if (index >= dynamicIndex)
     {
       return eSettingDelegate(eFeature).dynamicSetting(this, eSettings(), index - dynamicIndex);
     }
     else if (index <= -1)
     {
       throw new IllegalArgumentException("The feature '" + eFeature.getName() + "' is not a valid feature");
     }
     else if (eFeature.isMany())
     {
       return (EStructuralFeature.Setting)eGet(eFeature, false);
     }
     else
     {
       EStructuralFeature.Setting setting =
         new EStructuralFeature.Setting()
         {
           public EObject getEObject()
           {
             return BasicEObjectImpl.this;
           }
 
           public EStructuralFeature getEStructuralFeature()
           {
             return eFeature;
           }
 
           public Object get(boolean resolve)
           {
             return BasicEObjectImpl.this.eGet(eFeature, resolve);
           }
 
           public void set(Object newValue)
           {
             BasicEObjectImpl.this.eSet(eFeature, newValue);
           }
 
           public boolean isSet()
           {
             return BasicEObjectImpl.this.eIsSet(eFeature);
           }
 
           public void unset()
           {
             BasicEObjectImpl.this.eUnset(eFeature);
           }
         };
       return setting;
     }
   }
 
   public InternalEObject.EStore eStore()
   {
     return null;
   }
 
   public void eSetStore(InternalEObject.EStore store)
   {
     throw new UnsupportedOperationException();
   }
 
   protected Object[] eVirtualValues()
   {
     throw new UnsupportedOperationException();
     //return eVirtualValues;
   }
 
   protected void eSetVirtualValues(Object[] newValues)
   {
     throw new UnsupportedOperationException();
     //eVirtualValues = newValues;
   }
 
   protected static int eVirtualBitCount(int value)
   {
     value -= value >>> 1 & 0x55555555;
     value = (value & 0x33333333) + (value >>> 2 & 0x33333333);
     value = (value + (value >>> 4)) & 0x0F0F0F0F;
     value += value >>> 8;
     value += value >>> 16;
     return value & 0x3F;
   }
 
   protected static final int EVIRTUAL_SET = 0;
 
   protected static final int EVIRTUAL_UNSET = 1;
 
   protected static final int EVIRTUAL_GET = 2;
 
   protected static final int EVIRTUAL_IS_SET = 3;
 
   protected int eVirtualIndexBits(int offset)
   {
     throw new UnsupportedOperationException();
     //return eVirtualIndexBits[offset];
   }
 
   protected void eSetVirtualIndexBits(int offset, int newIndexBits)
   {
     throw new UnsupportedOperationException();
     //eVirtualIndexBits[offset] = newIndexBits;
   }
 
   protected int eVirtualIndex(int eDerivedStructuralFeatureID, int action)
   {
     int offset = eDerivedStructuralFeatureID >>> 5;
     int bits = eVirtualIndexBits(offset);
     int bitIndex = eDerivedStructuralFeatureID & 31;
     int bit = bits >>> bitIndex & 1;
 
     switch (action)
     {
       case EVIRTUAL_IS_SET:
       {
         return bit;
       }
       case EVIRTUAL_GET:
       case EVIRTUAL_UNSET:
       {
         if (bit == 0)
         {
           return 0;
         }
       }
       case EVIRTUAL_SET:
       default:
       {
         if (bit == action)
         {
           eSetVirtualIndexBits(offset, bits ^ (1 << bitIndex));
         }
 
         int result = eVirtualBitCount(bits << 31 - bitIndex);
 
         for (int i = 0; i < offset; i++)
         {
           result += eVirtualBitCount(eVirtualIndexBits(i));
         }
 
         return (result << 1) - bit;
       }
     }
   }
 
   protected Object eVirtualGet(int eDerivedStructuralFeatureID)
   {
     int index = eVirtualIndex(eDerivedStructuralFeatureID, EVIRTUAL_GET);
     return (index & 1) == 0 ? null : eVirtualValues()[index >>> 1];
   }
 
   protected boolean eVirtualIsSet(int eDerivedStructuralFeatureID)
   {
     return (eVirtualIndex(eDerivedStructuralFeatureID, EVIRTUAL_IS_SET) & 1) != 0;
   }
 
   protected static final Object EVIRTUAL_NO_VALUE = new Object();
 
   protected Object eVirtualSet(int eDerivedStructuralFeatureID, Object value)
   {
     int index = eVirtualIndex(eDerivedStructuralFeatureID, EVIRTUAL_SET);
     int position = index >>> 1;
     Object[] values = eVirtualValues();
 
     if ((index & 1) != 0)
     {
       Object oldValue = values[position];
       values[position] = value;
       return oldValue;
     }
     else
     {
       if (values == null)
       {
         eSetVirtualValues(new Object []{ value });
       }
       else
       {
         int length = values.length;
         Object[] newValues = new Object [length + 1];
 
         if (position > 0)
         {
           System.arraycopy(values, 0, newValues, 0, position);
         }
 
         if (position < length)
         {
           System.arraycopy(values, position, newValues, position + 1, length - position);
         }
 
         newValues[position] = value;
         eSetVirtualValues(newValues);
       }
 
       return EVIRTUAL_NO_VALUE;
     }
   }
 
   protected Object eVirtualUnset(int eDerivedStructuralFeatureID)
   {
     int index = eVirtualIndex(eDerivedStructuralFeatureID, EVIRTUAL_UNSET);
 
     if ((index & 1) == 0)
     {
       return EVIRTUAL_NO_VALUE;
     }
     else
     {
       int position = index >>> 1;
       Object[] values = eVirtualValues();
       Object oldValue = values[position];
       int length = values.length - 1;
 
       if (length == 0)
       {
         eSetVirtualValues(null);
       }
       else
       {
         Object[] newValues = new Object [length];
 
         if (position > 0)
         {
           System.arraycopy(values, 0, newValues, 0, position);
         }
 
         if (position < length)
         {
           System.arraycopy(values, position + 1, newValues, position, length - position);
         }
 
         eSetVirtualValues(newValues);
       }
 
       return oldValue;
     }
   }
 
   public String toString()
   {
     // Should use the following code to improve debuggability. Will need to
     // update testcase baselogs before this change can be made.
 
     StringBuffer result = new StringBuffer(getClass().getName());
     result.append('@');
     result.append(Integer.toHexString(hashCode()));
 
     if (eIsProxy())
     {
       result.append(" (eProxyURI: ");
       result.append(eProxyURI());
       if (eDynamicClass() != null)
       {
         result.append(" eClass: ");
         result.append(eDynamicClass());
       }
       result.append(')');
     }
     else if (eDynamicClass() != null)
     {
       result.append(" (eClass: ");
       result.append(eDynamicClass());
       result.append(')');
     }
 
     return result.toString();
   }
 }
