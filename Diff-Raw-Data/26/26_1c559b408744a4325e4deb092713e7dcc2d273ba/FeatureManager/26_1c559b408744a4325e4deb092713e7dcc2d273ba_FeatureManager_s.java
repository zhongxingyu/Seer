 /**
  * Copyright (c) 2010 Sierra Wireless Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  *   Contributors:
  *      Benjamin Cabe, Sierra Wireless - initial API and implementation
  */
 package org.eclipse.e4.opensocial.container.internal.features;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IRegistryEventListener;
 import org.eclipse.e4.opensocial.container.resolver.ModuleResolver;
 import org.eclipse.e4.opensocial.model.Module;
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.osgi.service.resolver.PlatformAdmin;
 import org.eclipse.osgi.service.resolver.State;
 
 public class FeatureManager implements ModuleResolver, IRegistryEventListener {
 	private static final String FEATURES_EXT_POINT_ID = "org.eclipse.e4.opensocial.container.features";
 	private static final String FEATURE_ELEM = "feature";
 	private static final String FEATURE_ATTR_NAME = "name";
 	private static final String FEATURE_ATTR_VERSION = "version";
 	private PlatformAdmin _platformAdmin;
 	private IExtensionRegistry _extensionRegistry;
 	private Map<String, Feature> _availableFeatures = new HashMap<String, Feature>();
 
 	private State _featuresState;
 
 	protected void activate() {
 	}
 
 	protected void deactivate() {
 		_extensionRegistry.removeListener(this);
 	}
 
 	protected void setPlatformAdmin(PlatformAdmin platformAdmin) {
 		_platformAdmin = platformAdmin;
 		_featuresState = _platformAdmin.getFactory().createState(true);
 
 		_featuresState.addBundle(_platformAdmin.getFactory()
 				.createBundleDescription(1, "opensocial-0.8", null, null, null,
 						null, null, null, false, false, false, null, null,
 						null, null, null));
 	}
 
 	protected void setExtensionRegistry(IExtensionRegistry extensionRegistry) {
 		_extensionRegistry = extensionRegistry;
 		_extensionRegistry.addListener(this, FEATURES_EXT_POINT_ID);
 
 		for (IConfigurationElement ice : _extensionRegistry
 				.getConfigurationElementsFor(FEATURES_EXT_POINT_ID)) {
 			if (FEATURE_ELEM.equals(ice.getName())) {
 				addFeature(ice);
 			} else {
 				// no other top-level elements at the moment
 			}
 		}
 	}
 
 	protected void unsetExtensionRegistry(IExtensionRegistry extensionRegistry) {
 		_extensionRegistry.removeListener(this);
 		_extensionRegistry = null;
 	}
 
 	@Override
 	public void added(IExtension[] extensions) {
 		for (IExtension ext : extensions) {
 			for (IConfigurationElement ice : ext.getConfigurationElements()) {
 				if (FEATURE_ELEM.equals(ice.getName())) {
 					addFeature(ice);
 				}
 			}
 		}
 
 	}
 
 	private void addFeature(IConfigurationElement ice) {
 		String name = ice.getAttribute(FEATURE_ATTR_NAME);
 		String version = ice.getAttribute(FEATURE_ATTR_VERSION);
 		Feature f = new Feature(name, version, ice);
 		_availableFeatures.put(name, f);
 		_featuresState.addBundle(FeatureUtils
				.featureExtensionToBundleDescription(f, _featuresState
						.getFactory()));
 	}
 
 	@Override
 	public void removed(IExtension[] extensions) {
 		for (IExtension ext : extensions) {
 			for (IConfigurationElement ice : ext.getConfigurationElements()) {
 				if (FEATURE_ELEM.equals(ice.getName())) {
 					removeFeature(ice);
 				}
 			}
 		}
 	}
 
 	private void removeFeature(IConfigurationElement ice) {
 		String name = ice.getAttribute(FEATURE_ATTR_NAME);
 		Feature oldFeature = _availableFeatures.remove(name);
 		_featuresState.removeBundle(oldFeature.hashCode());
 	}
 
 	@Override
 	public void added(IExtensionPoint[] extensionPoints) {
 	}
 
 	@Override
 	public void removed(IExtensionPoint[] extensionPoints) {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.e4.opensocial.container.resolver.ModuleResolver#resolve(org
 	 * .eclipse.e4.opensocial.model.Module)
 	 */
 	@Override
 	public List<Feature> resolve(Module module) throws UnresolvedException {
 		List<Feature> features = new ArrayList<Feature>();
 		BundleDescription moduleDesc = FeatureUtils.moduleToBundleDescription(
 				module, _platformAdmin.getFactory());
		// !!! WAITING FOR FIX OF BUG 308738 !!!
 		State temporaryState = _platformAdmin.getFactory().createState(
 				_featuresState);
 		// !!!!!
 		temporaryState.setResolver(_platformAdmin.createResolver());
 		temporaryState.addBundle(moduleDesc);
 		temporaryState.resolve();
 
 		if (!moduleDesc.isResolved()) {
			throw new UnresolvedException(module, temporaryState
					.getResolverErrors(moduleDesc));
 		}
 
 		BundleDescription[] bundles = temporaryState.getStateHelper()
 				.getPrerequisites(new BundleDescription[] { moduleDesc });
 		temporaryState.getStateHelper().sortBundles(bundles);
 		for (BundleDescription bundle : bundles) {
 			if (bundle.getUserObject() instanceof Feature) {
 				Feature f = (Feature) bundle.getUserObject();
 				features.add(f);
 			}
 		}
 
 		return features;
 	}
 }
