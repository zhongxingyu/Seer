 /*******************************************************************************
  * Copyright (c) 2008, 2012 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.kernel.userregion.internal.management;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
 
 public class JMXQuasiWire {
 
 	private final String namespace;
 	
 	private final long providerId;
 	
 	private final long requirerId;
 	
 	private final Map<String, String> bundleCapabilityAttributes;
 	
 	private final Map<String, String> bundleCapabilityDirectives;
 	
 	private final Map<String, String> bundleRequirementAttributes;
 	
 	private final Map<String, String> bundleRequirementDirectives;
 	
 	public JMXQuasiWire(QuasiImportPackage quasiImportPackage) {
 		this.namespace = BundleDescription.PACKAGE_NAMESPACE;
 		this.requirerId = quasiImportPackage.getImportingBundle().getBundleId();
 		QuasiExportPackage provider = quasiImportPackage.getProvider();
 		if(provider != null){
			this.providerId = quasiImportPackage.getProvider().getExportingBundle().getBundleId();
 			this.bundleCapabilityAttributes = this.stringifyMap(provider.getAttributes());
 			this.bundleCapabilityDirectives = this.stringifyMap(provider.getDirectives());
 		}else{
			this.providerId = -1l;
 			this.bundleCapabilityAttributes = new HashMap<String, String>();
 			this.bundleCapabilityDirectives = new HashMap<String, String>();
 		}
 		this.bundleRequirementAttributes = this.stringifyMap(quasiImportPackage.getAttributes());
 		this.bundleRequirementDirectives = this.stringifyMap(quasiImportPackage.getDirectives());
 	}
 	
 	public JMXQuasiWire(QuasiBundle provider, QuasiBundle requirer) {
 		this.namespace = BundleDescription.HOST_NAMESPACE;
 		this.providerId = provider.getBundleId();
 		this.requirerId = requirer.getBundleId();
 		this.bundleCapabilityAttributes = new HashMap<String, String>();
 		this.bundleCapabilityDirectives = new HashMap<String, String>();
 		this.bundleRequirementAttributes = this.getProperties(new String[]{"host", provider.getSymbolicName()}, new String[]{"bundle-version", provider.getVersion().toString()});
 		this.bundleRequirementDirectives = new HashMap<String, String>();
 	}
 
 	public JMXQuasiWire(QuasiRequiredBundle provider,QuasiBundle requirer) {
 		this.namespace = BundleDescription.BUNDLE_NAMESPACE;
 		QuasiBundle quasiProvider = provider.getProvider();
 		if(quasiProvider == null){
 			this.providerId = -1l;
 		}else{
 			this.providerId = quasiProvider.getBundleId();
 		}
 		this.requirerId = requirer.getBundleId();
 		this.bundleCapabilityAttributes = this.stringifyMap(provider.getAttributes());
 		this.bundleCapabilityDirectives = this.stringifyMap(provider.getDirectives());
 		this.bundleRequirementAttributes = this.getProperties(new String[]{"Required-Bundle", provider.getRequiredBundleName()}, new String[]{"Version-Constraint", provider.getVersionConstraint().toString()});
 		this.bundleRequirementDirectives = new HashMap<String, String>();
 	}
 
 	public final long getRequirerBundleId(){
 		return this.requirerId;
 	}
 	
 	public final long getProviderBundleId(){
 		return this.providerId;
 	}
 	
 	public final String getNamespace(){
 		return this.namespace;
 	}
 	
 	public final Map<String, String> getBundleCapabilityAttributes(){
 		return this.bundleCapabilityAttributes;
 	}
 
 	public final Map<String, String> getBundleCapabilityDirectives(){
 		return this.bundleCapabilityDirectives;
 	}
 	
 	public final Map<String, String> getBundleRequirementAttributes(){
 		return this.bundleRequirementAttributes;
 	}
 	
 	public final Map<String, String> getBundleRequirementDirectives(){
 		return this.bundleRequirementDirectives;
 	}
 	
 	private Map<String, String> stringifyMap(Map<String, Object> map){
 		Map<String, String> properties = new HashMap<String, String>();
 		for(Entry<String, Object> entry: map.entrySet()){
 			if(entry.getValue().getClass().isArray()){
 				Object[] valueArray = (Object[]) entry.getValue();
 				StringBuilder builder = new StringBuilder();
 				for (int i = 0; i < valueArray.length; i++) {
 					builder.append(valueArray[i].toString());
 					if(i < valueArray.length -1){
 						builder.append(", ");
 					}
 				}
 				properties.put(entry.getKey(), builder.toString());
 			}else{
 				properties.put(entry.getKey(), entry.getValue().toString());
 			}
 		}
 		return properties;
 	}
 	
 	private Map<String, String> getProperties(String[]... strings){
 		Map<String, String> properties = new HashMap<String, String>();
 		for (int i = 0; i < strings.length; i++) {
 			properties.put(strings[i][0], strings[i][1]);
 		}
 		return properties;
 	}
 	
 }
