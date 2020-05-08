 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.common.metadata.internal;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.xml.type.AnyType;
 import org.eclipse.emf.ecore.xml.type.SimpleAnyType;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.common.metadata.internal.provisional.Trait;
 import org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.provisional.ListOfValues;
 import org.eclipse.jst.jsf.contentmodel.annotation.internal.Messages;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Experimental/prototype class to assist with decoding trait values
  */
 public class TraitValueHelper {
 	
 	public static EClass getValueType(Trait trait){
 		if (trait.getValue() != null)
 			return trait.getValue().eClass();
 		return null;
 	}
 	
 	public static Object getValue(Trait trait){
 		if (trait == null)
 			return null;
 		if (trait.getValue() == null)
 			return null;
 		
 		if (trait.getValue() instanceof SimpleAnyType){
 			return ((SimpleAnyType)trait.getValue()).getRawValue();
 		}
 		else if (trait.getValue() instanceof AnyType){
 			AnyType any = (AnyType)trait.getValue();
 			FeatureMap map = any.getMixed();			
 			return getTextValueFromFeatureMap(map);
 		}
 		else if ( trait.getValue().eIsProxy() && trait.getValue() instanceof BasicEObjectImpl){
 			BasicEObjectImpl o = (BasicEObjectImpl)trait.getValue();
 			return o.eProxyURI().toString();
 		}
 		return trait.getValue();
 	}
 	
 	private static String getTextValueFromFeatureMap(FeatureMap map) {
 		for (Iterator it=map.iterator();it.hasNext();){
 			FeatureMap.Entry entry = (FeatureMap.Entry)it.next();
 			if (entry.getEStructuralFeature().getName().equals("text"))		
 				return (String)entry.getValue();
 		}
 		return null;
 	}
 
 	public static String getValueAsString(Trait trait){
 		Object val = getValue(trait);
 		if (val != null && val instanceof String){			
 			String result = getNLSValue(trait, (String)val);
 			return result;
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param trait whose value a {@link ListOfValues} or is a single string
 	 * @return List of Strings
 	 */
 	public static List getValueAsListOfStrings(Trait trait){
 		//PROTO ONLY!!! Need to make WAY more robust!
 		List ret = new ArrayList();
 		if (trait.getValue() instanceof ListOfValues) {
 			for(Iterator it=trait.getValue().eContents().iterator();it.hasNext();){
 				Object o = it.next();				
 				if (o instanceof SimpleAnyType){
 					SimpleAnyType sat = (SimpleAnyType)o;
 					String rawValue = getTextValueFromFeatureMap(sat.getMixed());
 					String nlsValue = getNLSValue(trait, rawValue);
 					
 					ret.add(nlsValue);
 				}	
 			}
 		} 
 		else {
 			//may be single value
 			String o = getValueAsString(trait);
 			if (o != null)
 				ret.add(o);
 		}
 		return ret;
 	}
 	
 	/**
 	 * Looks for '%' (and not '%%') at beginning of rawValue.   If found, looks to the
 	 * traits sourceModelProvider for resource bundle to resolve the key after 
 	 * stripping the '%' sign.
 	 * @param trait
 	 * @param rawValue of string in from metadata
 	 * @return the NLS Value or rawValue if it cannot be located
 	 */
 	public static String getNLSValue(Trait trait, String rawValue) {
 		String result = rawValue;
 		if (rawValue.startsWith("%") && !rawValue.startsWith("%%")){ 
 			String key = rawValue.substring(1);
 			result = getNLSPropertyValue(trait, key);	
 			if (rawValue == null){
 				result = rawValue;
 			}
 		}
		return result;
 	}
 
 	//will return null if there is an IOException with ResourceBundle
 	private static String getNLSPropertyValue(Trait trait, String key){
 		String NOT_FOUND = Messages.CMAnnotationMap_key_not_found;//FIX ME
 		try{
 			IMetaDataSourceModelProvider provider = trait.getSourceModelProvider();
 			IResourceBundleProvider resourceBundleProvider = (IResourceBundleProvider)provider.getAdapter(IResourceBundleProvider.class);		
 			if (resourceBundleProvider != null){
 				ResourceBundle resourceBundle_ = resourceBundleProvider.getResourceBundle();				
 				if (resourceBundle_ != null){
 					String replVal = resourceBundle_.getString(key);
 					return replVal;
 				}				
 			}
 			//return original string 
 			return key; 
 //		} catch (IOException e) {
 //			JSFCommonPlugin.log(e, NLS.bind(Messages.CMAnnotationMap_IOException, new String[]{val}));
 //			return null;
 		} catch (MissingResourceException e){
 			//fall thru
 			JSFCommonPlugin.log(e,  NLS.bind(Messages.CMAnnotationMap_MissingResource_exception, new String[]{key}));
 		}
 		return key + NOT_FOUND;
 	}
 
 	/**
 	 * Will take get the value as a String and attempt to coerce to boolean.
 	 * Will return 'false' if coercion fails.
 	 * @param trait
 	 * @return true or false 
 	 */
 	public static boolean getValueAsBoolean(Trait trait) {
 		String val = getValueAsString(trait);
 		if (val == null)
 			return false;
 		
 		return Boolean.valueOf(val).booleanValue();
 
 	}
 	
 	/**
 	 * Caller must know to expect a Boolean object trait type.  Null will be returned if it is not. 
 	 * @param trait
 	 * @return Boolean or null
 	 */
 	public static Boolean getValueAsBooleanObject(Trait trait) {
 
 		Object val = getValue(trait);
 		if (val == null)
 			return null;
 		
 //		if ()
 			return null;
 //		return Boolean.valueOf(val).booleanValue();
 
 	}
 
 
 }
