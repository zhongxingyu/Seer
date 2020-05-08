 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2m.engine.core.masks;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.atos.optimus.m2m.engine.core.Activator;
 import net.atos.optimus.m2m.engine.core.logging.OptimusM2MEngineMessages;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 
 /**
  * A manager dedicated to transformation mask data sources
  * 
  * @author tnachtergaele <nachtergaele.thomas@gmail.com>
  * 
  *
  */
 
 public enum TransformationMaskDataSourceManager {
 
 	INSTANCE;
 
 	/** The name of the associated extension point */
 	public static final String EXTENSION_POINT_NAME = "TransformationMaskSources";
 
 	/** The name of the element in the extension */
 	public static final String EXTENSION_ELEMENT = "transformationMaskDataSource";
 	
 	/** The last known preferred mask preference */
 	public TransformationMaskReference preferredMaskReference;
 
 	/**
 	 * The key in the Optimus preference store associated to the transformation
 	 * mask choose in the preference
 	 */
 	private static final String PREFERED_MASK_STORE_KEY = "Transformation Mask Preference";
 
 	/** The name of the default mask */
 	public static final String DEFAULT_MASK_NAME = "Default all On mask";
 
 	private final List<TransformationMaskDataSource> transformationMaskDataSources;
 
 	private TransformationMaskDataSourceManager() {
 		OptimusM2MEngineMessages.MS01.log();
 
 		List<TransformationMaskDataSource> tempTransformationMaskDataSources = new LinkedList<TransformationMaskDataSource>();
 
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,
 				TransformationMaskDataSourceManager.EXTENSION_POINT_NAME);
 
 		for (IExtension extension : extensionPoint.getExtensions()) {
 			for (IConfigurationElement configurationElement : extension.getConfigurationElements()) {
 				String contributorName = configurationElement.getContributor().getName();
 				try {
 					if (TransformationMaskDataSourceManager.EXTENSION_ELEMENT.equals(configurationElement.getName())) {
 						String implementation = configurationElement.getAttribute("implementation");
 						if (implementation != null && implementation.length() > 0) {
 							Object o = configurationElement.createExecutableExtension("implementation");
 							if (o instanceof TransformationMaskDataSource) {
 								TransformationMaskDataSource dataSource = (TransformationMaskDataSource) o;
 								tempTransformationMaskDataSources.add(dataSource);
 								OptimusM2MEngineMessages.MS02
 										.log(dataSource.getName(), dataSource.getClass().getName());
 							} else {
 								OptimusM2MEngineMessages.MS03.log(o.getClass().getName());
 							}
 						} else {
 							OptimusM2MEngineMessages.MS04.log(contributorName);
 						}
 					}
 				} catch (CoreException ce) {
 					OptimusM2MEngineMessages.MS05.log(ce.getMessage(), contributorName);
 					Activator
 							.getDefault()
 							.getLog()
 							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 									"An error occurred when trying to load Transformation Mask Data Source", ce));
 				}
 			}
 		}
 		this.transformationMaskDataSources = Collections.unmodifiableList(tempTransformationMaskDataSources);
 		OptimusM2MEngineMessages.MS06.log();
 	}
 
 	/**
 	 * The list of the transformation mask data sources
 	 * 
 	 * @return the list of the transformation mask data sources .
 	 */
 	public List<TransformationMaskDataSource> getTransformationMaskDataSources() {
 		return transformationMaskDataSources;
 	}
 
 	/**
 	 * Returns the transformation mask reference selected via the preference
 	 * window or the filter on all if nothing was selected
 	 * 
 	 * @return the transformation mask reference selected via the preference
 	 *         window or the filter on all if nothing was selected.
 	 */
 	public TransformationMaskReference getPreferredTransformationMask() {
 		String maskName = Activator.getDefault().getPreferenceStore()
 				.getString(TransformationMaskDataSourceManager.PREFERED_MASK_STORE_KEY);
 		if(this.preferredMaskReference == null || !this.preferredMaskReference.getName().equals(maskName)){
 			this.preferredMaskReference = this.findTransformationMaskByName(maskName);
 			if(this.preferredMaskReference == null){
 				this.preferredMaskReference = this
 						.findTransformationMaskByName(TransformationMaskDataSourceManager.DEFAULT_MASK_NAME);
 				this.setPreferredTransformationMask(this.preferredMaskReference);
 			}
 		}
 		return this.preferredMaskReference; 
 	}
 
 	/**
 	 * Find a transformation mask with his name
 	 * 
 	 * @param maskName
 	 *            the mask name.
 	 * @return the transformation mask with the specified name if exists, null
 	 *         otherwise.
 	 */
 	public TransformationMaskReference findTransformationMaskByName(String maskName) {
 		for (TransformationMaskDataSource transformationMaskDataSource : TransformationMaskDataSourceManager.INSTANCE
 				.getTransformationMaskDataSources()) {
 			for (TransformationMaskReference transformationMaskReferenceFound : transformationMaskDataSource
 					.getAllMasks()) {
 				if (maskName.equals(transformationMaskReferenceFound.getName())) {
 					return transformationMaskReferenceFound;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Set the preferred transformation mask reference
 	 * 
 	 * @param transformationMaskReference
 	 *            the new preferred transformation mask reference.
 	 */
 	public void setPreferredTransformationMask(TransformationMaskReference transformationMaskReference) {
 		Activator
 				.getDefault()
 				.getPreferenceStore()
 				.putValue(TransformationMaskDataSourceManager.PREFERED_MASK_STORE_KEY,
 						transformationMaskReference.getName());
 	}
	
	public void reinitializePreferredTransformationMask(){
		Activator
		.getDefault()
		.getPreferenceStore()
		.putValue(TransformationMaskDataSourceManager.PREFERED_MASK_STORE_KEY,
				TransformationMaskDataSourceManager.DEFAULT_MASK_NAME);
	}
 
 }
