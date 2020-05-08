 /**
  * <copyright>
  *
  * Copyright (c) 2012 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: EMFModelConverter.java,v 1.23 2011/08/29 05:16:04 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.json;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.component.TexoComponent;
 import org.eclipse.emf.texo.model.ModelObject;
 import org.eclipse.emf.texo.resolver.DefaultObjectResolver;
 import org.eclipse.emf.texo.resolver.ObjectResolver;
 import org.eclipse.emf.texo.utils.ModelUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Base class for converting {@link JSONObject} instances to a model object.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  * @see ModelObject
  */
 public abstract class BaseJSONModelConverter<T extends Object> implements TexoComponent {
 
   private Map<String, T> resolvedObjects = new HashMap<String, T>();
 
   private ObjectResolver objectResolver = ComponentProvider.getInstance().newInstance(DefaultObjectResolver.class);
 
   private JSONValueConverter jsonValueConverter = (JSONValueConverter) ComponentProvider.getInstance().newInstance(
       getValueConversionClass());
 
   protected Class<?> getValueConversionClass() {
     return JSONValueConverter.class;
   }
 
   /**
    * Converts a JSONArray to a list of equivalent model objects.
    * 
    * @param jsonArray
    *          the jsonArray to convert
    * @return the model object
    */
   public List<T> convert(final JSONArray jsonArray) {
     resolvedObjects.clear();
     return doConvert(jsonArray);
   }
 
   protected List<T> doConvert(final JSONArray jsonArray) {
     try {
       final List<T> result = new ArrayList<T>();
       for (int i = 0; i < jsonArray.length(); i++) {
         final Object o = jsonArray.get(i);
         if (o instanceof JSONArray) {
           result.addAll(convert((JSONArray) o));
         } else if (o instanceof JSONObject) {
           result.add(doConvert((JSONObject) o));
         }
       }
       return result;
     } catch (JSONException e) {
       throw new RuntimeException(e);
     }
   }
 
   /**
    * Converts a JSONObject to its equivalent model object.
    * 
    * @param jsonObject
    *          the jsonObject to convert
    * @return the model object
    */
   public T convert(final JSONObject jsonObject) {
     resolvedObjects.clear();
     return doConvert(jsonObject);
   }
 
   protected T doConvert(JSONObject source) {
     final T target = resolveObject(source);
     convertContent(source, target);
     return target;
   }
 
   protected T resolveObject(JSONObject jsonObject) {
     try {
       String uriString = null;
       if (jsonObject.has(ModelJSONConstants.URI_PROPERTY)) {
         uriString = jsonObject.getString(ModelJSONConstants.URI_PROPERTY);
         if (resolvedObjects.containsKey(uriString)) {
           return resolvedObjects.get(uriString);
         }
         final T object = fromUri(uriString);
         if (object != null) {
           resolvedObjects.put(uriString, object);
           return object;
         }
       }
       if (jsonObject.has(ModelJSONConstants.ECLASS_PROPERTY)) {
         final EClass eClass = ModelUtils.getEClassFromQualifiedName(jsonObject
             .getString(ModelJSONConstants.ECLASS_PROPERTY));
         if (eClass == null) {
           throw new IllegalArgumentException("No eclass found for jsonObject " + jsonObject); //$NON-NLS-1$
         }
         if (hasValue(jsonObject, ModelJSONConstants.ID_PROPERTY)) {
           final String idString = jsonObject.getString(ModelJSONConstants.ID_PROPERTY);
           final URI uri = getUriResolver().toURI(eClass, idString);
           uriString = uri.toString();
           if (resolvedObjects.containsKey(uriString)) {
             return resolvedObjects.get(uriString);
           }
 
           final T object = fromUri(uriString);
           if (object != null) {
             resolvedObjects.put(uriString, object);
             return object;
           }
         }
         final T object = create(eClass, uriString);
         if (uriString != null) {
           resolvedObjects.put(uriString, object);
         }
         return object;
       }
       throw new IllegalArgumentException("No eClass property in jsonObject " + jsonObject); //$NON-NLS-1$
     } catch (JSONException e) {
       throw new RuntimeException(e);
     }
   }
 
   protected abstract T fromUri(String uriString);
 
   protected abstract T create(EClass eClass, String uriString);
 
   protected abstract EClass eClass(T target);
 
   protected abstract void eSet(T target, EStructuralFeature eFeature, Object value);
 
   protected abstract Object eGet(T target, EStructuralFeature eFeature);
 
   protected abstract Object createFeatureMapEntry(T target, EStructuralFeature eFeature, JSONObject value);
 
   protected abstract void eRemoveFrom(T target, EStructuralFeature eFeature, Object value);
 
   protected abstract void eAddTo(T target, EStructuralFeature eFeature, Object value);
 
   private boolean hasValue(JSONObject jsonObject, String propertyName) throws JSONException {
     if (!jsonObject.has(propertyName)) {
       return false;
     }
     final Object id = jsonObject.get(propertyName);
     if (id == null || id == JSONObject.NULL) {
       return false;
     }
     if (id instanceof String && ((String) id).trim().length() == 0) {
       return false;
     }
     return true;
   }
 
   protected void convertContent(JSONObject source, T target) {
     @SuppressWarnings("unchecked")
     final Iterator<Object> keyIterator = source.keys();
     try {
       while (keyIterator.hasNext()) {
         final String key = (String) keyIterator.next();
         final Object value = source.get(key);
 
         final EStructuralFeature eStructuralFeature = getEFeatureFromPropertyName(eClass(target), key);
 
         // there can be many properties in a jsonobject, just ignore the illegal ones
         if (eStructuralFeature == null) {
           continue;
         }
 
         if (!eStructuralFeature.isChangeable() || eStructuralFeature.isVolatile()) {
           continue;
         }
 
         if (FeatureMapUtil.isFeatureMap(eStructuralFeature)) {
           convertFeatureMap(source, value, target, eStructuralFeature);
         } else if (eStructuralFeature.isMany()) {
           if (eStructuralFeature instanceof EAttribute) {
             final EAttribute eAttribute = (EAttribute) eStructuralFeature;
             convertManyEAttribute(source, value, target, eAttribute);
           } else {
             final EReference eReference = (EReference) eStructuralFeature;
             convertManyEReference(source, value, target, eReference);
           }
         } else {
           if (eStructuralFeature instanceof EAttribute) {
             final EAttribute eAttribute = (EAttribute) eStructuralFeature;
             convertSingleEAttribute(source, value, target, eAttribute);
           } else {
             final EReference eReference = (EReference) eStructuralFeature;
             convertSingleEReference(source, value, target, eReference);
           }
         }
       }
     } catch (JSONException e) {
       throw new RuntimeException(e);
     }
   }
 
   protected EStructuralFeature getEFeatureFromPropertyName(EClass eClass, String propertyName) {
     return eClass.getEStructuralFeature(propertyName);
   }
 
   /**
    * Converts the value of an EReference with isMany==false
    * 
    * @param source
    *          the JSONObject from which the value is read
    * @param target
    *          the Object in which the value is to be set
    * @param eReference
    *          the eReference which is converted
    */
   protected void convertSingleEReference(final JSONObject jsonObject, Object value, T target,
       final EReference eReference) {
     // bidirectional one-to-many are always set from the many side to preserve the order
     // this only works if the other side is also part of this conversion thread, is it??
     if (eReference.getEOpposite() != null && eReference.getEOpposite().isMany()) {
       return;
     }
     if (value == null || value == JSONObject.NULL) {
       eSet(target, eReference, null);
     } else {
       final JSONObject jsonValue = (JSONObject) value;
       addEClassProperty(eReference, jsonValue);
       eSet(target, eReference, doConvert((JSONObject) value));
     }
   }
 
   /**
    * Converts the value of an EReference with isMany==true, the values of the collection are converted to Objects and
    * added to the list in the correct feature in the {@link ModelObject}.
    * 
    * @param source
    *          the JSONObject from which the value is read
    * @param value
    *          the value in the JSONObject, must be a JSONArray
    * @param target
    *          the Object in which the value is set
    * @param eReference
    *          the eReference which is converted
    */
   @SuppressWarnings("unchecked")
   protected void convertManyEReference(final JSONObject source, Object featureValue, T target,
       final EReference eReference) {
     final JSONArray jValues = (JSONArray) featureValue;
     if (ModelUtils.isEMap(eReference)) {
       final EClass mapEClass = eReference.getEReferenceType();
       final EObject mapEntryEObject = EcoreUtil.create(mapEClass);
       final EStructuralFeature valueFeature = mapEntryEObject.eClass().getEStructuralFeature("value"); //$NON-NLS-1$
       final EStructuralFeature keyFeature = mapEntryEObject.eClass().getEStructuralFeature("key"); //$NON-NLS-1$
 
       final Object mValues = eGet(target, eReference);
 
       // clear as there can be current values if the target is read from the db
       if (mValues instanceof Map<?, ?>) {
         ((Map<Object, Object>) mValues).clear();
       } else {
         ((Collection<Object>) mValues).clear();
       }
 
       try {
         for (int i = 0; i < jValues.length(); i++) {
           if (jValues.isNull(i)) {
             continue;
           }
           final JSONObject jValue = jValues.getJSONObject(i);
           final Object jEntryKey = jValue.get(ModelJSONConstants.KEY_PROPERTY);
           final Object jEntryValue = jValue.get(ModelJSONConstants.VALUE_PROPERTY);
 
           // key and value can also be an EObject
           final Object key;
           if (jEntryKey instanceof JSONObject) {
             addEClassProperty(keyFeature, (JSONObject) jEntryKey);
             key = doConvert((JSONObject) jEntryKey);
           } else {
             key = jsonValueConverter.fromJSON(target, jEntryKey, ((EAttribute) keyFeature).getEAttributeType());
           }
           final Object value;
           if (jEntryValue instanceof JSONObject) {
             addEClassProperty(valueFeature, (JSONObject) jEntryValue);
             value = doConvert((JSONObject) jEntryValue);
           } else {
             value = jsonValueConverter.fromJSON(target, jEntryValue, ((EAttribute) valueFeature).getEAttributeType());
           }
 
           if (mValues instanceof Map<?, ?>) {
             ((Map<Object, Object>) mValues).put(key, value);
           } else {
             final EObject mapEObject = EcoreUtil.create(mapEClass);
             mapEObject.eSet(keyFeature, key);
             mapEObject.eSet(valueFeature, value);
             ((Collection<Object>) mValues).add(mapEObject);
           }
         }
       } catch (JSONException e) {
         throw new RuntimeException(e);
       }
     } else {
       final Collection<?> mValues = (Collection<?>) eGet(target, eReference);
 
       // clear as there can be current values if the target is read from the db
       // use forloop as the collection can be unmodifiable
       for (Object o : new ArrayList<Object>(mValues)) {
         eRemoveFrom(target, eReference, o);
       }
 
       for (int i = 0; i < jValues.length(); i++) {
         if (jValues.isNull(i)) {
           mValues.add(null);
           continue;
         }
         try {
           if (jValues.isNull(i) || jValues.get(i) == null) {
             eAddTo(target, eReference, null);
             continue;
           }
 
           // special case, queries can return arrays of data
           if (jValues.get(i) instanceof JSONArray) {
             final JSONArray objects = (JSONArray) jValues.get(i);
             final Object[] array = new Object[objects.length()];
             for (int j = 0; j < objects.length(); j++) {
               final Object object = objects.get(j);
               if (object instanceof JSONObject) {
                 array[j] = doConvert((JSONObject) object);
               } else {
                 array[j] = object;
               }
             }
             eAddTo(target, eReference, array);
             continue;
           }
 
           final JSONObject jValue = jValues.getJSONObject(i);
 
           addEClassProperty(eReference, jValue);
 
           final T mValue = doConvert(jValue);
 
           // first add to the many reference
           eAddTo(target, eReference, mValue);
 
           // add to the other side, this is needed because the bi-directional
           // api is not always generated
           if (setBidirectionalReferences() && eReference.getEOpposite() != null && !eReference.getEOpposite().isMany()) {
             // first check if the otherside is already set
             final Object otherSide = eGet(mValue, eReference.getEOpposite());
             if (otherSide != target) {
               eSet(mValue, eReference.getEOpposite(), target);
             }
           }
         } catch (JSONException e) {
           throw new RuntimeException(e);
         }
       }
     }
   }
 
   protected boolean setBidirectionalReferences() {
     return false;
   }
 
   private void addEClassProperty(EStructuralFeature eFeature, JSONObject value) {
     if (eFeature instanceof EAttribute) {
       return;
     }
 
     final EReference eReference = (EReference) eFeature;
     // if not set then do our best to set it... note that hopefully the ereference type is not
     // abstract or so...
     if (!value.has(ModelJSONConstants.URI_PROPERTY) && !value.has(ModelJSONConstants.ECLASS_PROPERTY)) {
       final String eClassUri = ModelUtils.getQualifiedNameFromEClass(eReference.getEReferenceType());
       try {
         value.put(ModelJSONConstants.ECLASS_PROPERTY, eClassUri);
       } catch (JSONException e) {
         throw new Error(e);
       }
     }
 
   }
 
   /**
    * Converts the value of an EAttribute with isMany==false, the value is converted (
    * {@link #convertPrimitiveValue(Object, EDataType)}) and set in the correct feature in the {@link ModelObject}.
    * 
    * @param source
    *          the JSONObject from which the value is read
    * @param value
    *          the value in the JSONObject
    * @param target
    *          the object in which the value is to be set
    * @param eAttribute
    *          the EAttribute which is converted
    * @see #convertPrimitiveValue(Object, EDataType)
    */
   protected void convertSingleEAttribute(final JSONObject jsonObject, Object value, T target,
       final EAttribute eAttribute) {
     eSet(target, eAttribute, jsonValueConverter.fromJSON(target, value, eAttribute.getEAttributeType()));
   }
 
   /**
    * Converts the values of an FeatureMap, the values of the collection are converted to and added to the list in the
    * correct feature in the modelObject.
    * 
    * @param source
    *          the JSONObject from which the value is read
    * @param value
    *          the value
    * @param target
    *          the object in which the value is to be set
    * @param eFeature
    *          the eFeature which is converted
    */
   protected void convertFeatureMap(final JSONObject jsonObject, Object value, T target,
       final EStructuralFeature eFeature) {
     final JSONArray jValues = (JSONArray) value;
 
     @SuppressWarnings("unchecked")
     final Collection<Object> values = (Collection<Object>) eGet(target, eFeature);
 
     // clear as the object may have been read from the db
     values.clear();
 
     try {
       for (int i = 0; i < jValues.length(); i++) {
         final JSONObject jValue = jValues.getJSONObject(i);
         values.add(createFeatureMapEntry(target, eFeature, jValue));
       }
     } catch (JSONException e) {
       throw new RuntimeException(e);
     }
   }
 
   /**
    * Converts the value of an EAttribute with isMany==true, the values of the collection are converted to and added to
    * the list in the correct feature in the eObject.
    * 
    * @param source
    *          the JSONObject from which the value is read
    * @param value
    *          the value to convert
    * @param target
    *          the object in which the value is to be set
    * @param eAttribute
    *          the EAttribute which is converted
    * @see #convertPrimitiveValue(Object, EDataType)
    */
   protected void convertManyEAttribute(final JSONObject source, Object value, T target, final EAttribute eAttribute) {
     final JSONArray jValues = (JSONArray) value;
     final EDataType eDataType = eAttribute.getEAttributeType();
 
     @SuppressWarnings("unchecked")
     final Collection<Object> values = (Collection<Object>) eGet(target, eAttribute);
     // clear as the target may have been read from the db
     for (Object val : new ArrayList<Object>(values)) {
       eRemoveFrom(target, eAttribute, val);
     }
 
     try {
       for (int i = 0; i < jValues.length(); i++) {
         values.add(jsonValueConverter.fromJSON(target, jValues.get(i), eDataType));
       }
     } catch (JSONException e) {
       throw new RuntimeException(e);
     }
   }
 
  public ObjectResolver getObjectResolver() {
     return objectResolver;
   }
 
  /**
   * @deprecated use {@link #getObjectResolver()}
   * @return
   */
  public ObjectResolver getUriResolver() {
    return getObjectResolver();
  }

   public void setObjectResolver(ObjectResolver uriResolver) {
     this.objectResolver = uriResolver;
   }
 
   public Map<String, T> getResolvedObjects() {
     return resolvedObjects;
   }
 
   public void setResolvedObjects(Map<String, T> resolvedObjects) {
     this.resolvedObjects = resolvedObjects;
   }
 
   /**
    * Note is not called internally in this class, so don't override to replace the implementation of the
    * {@link JSONValueConverter} with your own, use the {@link ComponentProvider} concept for that.
    */
   protected JSONValueConverter getJSONValueConverter() {
     return jsonValueConverter;
   }
 }
