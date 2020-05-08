 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.metadataprocessors.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.InvalidRegistryObjectException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.metadataprocessors.IType;
 
 /**
  * Registry of <code>AbstractMetaDataEnabledType</code>s loaded from 
  * the <code>MetaDataEnabledFeatures</code> extension point
  * 
  * A map of features keyed by type id
  *
  */
 public class MetaDataEnabledFeatureRegistry{
 	private static final String EXTPTID = "MetaDataEnabledFeatures"; //$NON-NLS-1$
 	private Map<String, List<IMetaDataEnabledFeatureExtension>> featuresMap;
 	private Map<String, Class> typeCacheMap;
 	private static MetaDataEnabledFeatureRegistry INSTANCE;
 	
 	/**
 	 * @return the singleton instance of the MetaDataEnabledFeatureRegistry
 	 */
 	public static synchronized MetaDataEnabledFeatureRegistry getInstance(){
 		if (INSTANCE == null){
 			INSTANCE = new MetaDataEnabledFeatureRegistry();	
 		}
 		return INSTANCE;
 	}
 	
 	private MetaDataEnabledFeatureRegistry(){
 		featuresMap = new HashMap<String, List<IMetaDataEnabledFeatureExtension>>();
 		typeCacheMap = new HashMap<String, Class>();
 		readRegistry();		
 	}
 	
 	/**
 	 * Reads the MetaDataEnabledFeatures extensions into a registry
 	 */
 	protected void readRegistry() {
 		try {
 			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(JSFCorePlugin.PLUGIN_ID, EXTPTID);
 			IExtension[] extensions = point.getExtensions();
 			for (int i=0;i < extensions.length;i++){
 				IExtension ext = extensions[i];
 				for (int j=0;j < ext.getConfigurationElements().length;j++){
 					final String bundleId = ext.getConfigurationElements()[j].getContributor().getName();
 					final String id = ext.getConfigurationElements()[j].getAttribute("typeid"); //$NON-NLS-1$
 					final String klass = ext.getConfigurationElements()[j].getAttribute("class"); //$NON-NLS-1$
 					registerFeature(bundleId, id, klass);
 				}
 			}
 		} catch (InvalidRegistryObjectException e) {
 			JSFCorePlugin.log(e, "Unable to read " + JSFCorePlugin.PLUGIN_ID + EXTPTID + " registry"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 	
 	/**
 	 * Create {@link IMetaDataEnabledFeatureExtension}s and add to registry
 	 * @param bundleID
 	 * @param typeId
 	 * @param klass
 	 */
 	protected void registerFeature(String bundleID, String typeId, String klass){
 		IMetaDataEnabledFeatureExtension aFeature = new MetaDataEnabledFeatureExtension(bundleID, typeId, klass);
 		if (canCreateTypeForFeatureExtension(aFeature)){
 			if (!featuresMap.containsKey(typeId)){
 				List list = new ArrayList();
 				list.add(aFeature);
 				featuresMap.put(typeId, list);
 			}
 			else {
 				List list = featuresMap.get(typeId);
 				list.add(aFeature);
 			}
 		}
 	}
 
 
 	private boolean canCreateTypeForFeatureExtension(IMetaDataEnabledFeatureExtension feature) {
 		if (! typeCacheMap.containsKey(feature.getTypeID())){
 			IType type = AttributeValueRuntimeTypeRegistry.getInstance().getType(feature.getTypeID());
 			if (type != null){
 				Class typeClass = AttributeValueRuntimeTypeFactory.getInstance().getClassForType(type);
 				typeCacheMap.put(feature.getTypeID(), typeClass);
 			}
 			else
 				return false;
 		}
 		return typeCacheMap.get(feature.getTypeID()) != null;
 	}
 
 	/**
 	 * @param typeId
 	 * @return List of <code>AbstractMetaDataEnabledRuntimeTypeExtensions</code>
 	 * for a given by type id
 	 * 
 	 * TODO: make more efficient... no need to keep calculating features for subtypes. 
 	 */
 	public List<IMetaDataEnabledFeatureExtension> getFeatures(String typeId) {
 		
 		if (!featuresMap.containsKey(typeId))
 			featuresMap.put(typeId,new ArrayList());
 		
 		//copy current featuresMapped to typeId into return list
 		List<IMetaDataEnabledFeatureExtension> srcList = featuresMap.get(typeId);
 		List<IMetaDataEnabledFeatureExtension> ret = new ArrayList<IMetaDataEnabledFeatureExtension>(srcList.size());
 		copy(ret, srcList);	
 		
 		List subs = getFeatureExtensionsForMatchingSubclass(typeId);
 		for (Iterator<IMetaDataEnabledFeatureExtension> it=subs.iterator();it.hasNext();){
 			IMetaDataEnabledFeatureExtension featureExt = it.next();
 			if (!ret.contains(featureExt))
 				ret.add(featureExt);
 		}
 		return ret;
 		
 	}
 	
 	private void copy(List<IMetaDataEnabledFeatureExtension> destList,
 			List<IMetaDataEnabledFeatureExtension> srcList) {
 		for (Iterator<IMetaDataEnabledFeatureExtension> it=srcList.iterator();it.hasNext();){
 			destList.add(it.next());
 		}
 	}
 
 	/**
 	 * If the feature adapter is mapped to a type which is a superclass of the type of interest, then the feature adapter is an extension of that type
 	 * @param typeId
 	 * @return list of IMetaDataEnabledFeatureExtension
 	 */
 	private List<IMetaDataEnabledFeatureExtension> getFeatureExtensionsForMatchingSubclass(String typeId) {	
 		IType type = AttributeValueRuntimeTypeRegistry.getInstance().getType(typeId);
 		Class typeClass = AttributeValueRuntimeTypeFactory.getInstance().getClassForType(type);
 
 		List<IMetaDataEnabledFeatureExtension> ret = new ArrayList<IMetaDataEnabledFeatureExtension>();
 		// loop thru all of the type classes mapped to feature adapters that are subclasses of the type
 		for (Iterator it=typeCacheMap.keySet().iterator();it.hasNext();){
 			String featureTypeId = (String)it.next();
 			Class featureTypeClass = typeCacheMap.get(featureTypeId);
			try {
//				if (featureTypeClass.equals(typeClass)){
//					ret.add(featureTypeClass);
//				}
//				else 
					if (typeClass.asSubclass(featureTypeClass) != null)	{	
					ret.addAll(featuresMap.get(featureTypeId));
				}						
			} catch (ClassCastException e) {//
 			}
			
 		}
 		return ret;
 	}
 	
 
 }
