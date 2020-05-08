 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.tomography;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.tomography.parameters.Parameters;
 import uk.ac.gda.tomography.parameters.TomoExperiment;
 import uk.ac.gda.tomography.parameters.TomoParametersFactory;
 import uk.ac.gda.tomography.parameters.TomoParametersPackage;
 import uk.ac.gda.tomography.parameters.util.TomoParametersResourceFactoryImpl;
 
 /**
  */
 public class TomographyResourceUtil {
 	//
 	private static final Logger logger = LoggerFactory.getLogger(TomographyResourceUtil.class);
 
 	private static final String TOMOPARAMETERS = "tomoparameters";
 
 	private ResourceSet resourceSet;
 
 	public Resource getResource(ResourceSet resourceSet, String fileLocation, boolean shouldCreate) {
 		final URI tomoConfigUri = URI.createFileURI(fileLocation);
 		Resource res = null;
 
 		boolean fileExists = new File(fileLocation).exists();
 		if (!fileExists && !shouldCreate) {
 			return null;
 		}
 
 		if (!fileExists) {
 
 			final Resource[] resources = new Resource[1];
 			if (resourceSet != null) {
 				resources[0] = createResource(resourceSet, tomoConfigUri);
 			} else {
 				resources[0] = createResource(tomoConfigUri);
 			}
 			TomoExperiment experiment = TomoParametersFactory.eINSTANCE.createTomoExperiment();
 			Parameters parameters = TomoParametersFactory.eINSTANCE.createParameters();
 			experiment.setParameters(parameters);
 			resources[0].getContents().add(experiment);
 			Map<Object, Object> options = new HashMap<Object, Object>();
 			options.put(XMLResource.OPTION_ENCODING, "UTF-8");
 			try {
 				resources[0].save(options);
 			} catch (IOException e) {
 				logger.error("Exception saving the configuration model", e);
 			}
 			res = resources[0];
 		} else {
 			if (resourceSet != null) {
 				res = resourceSet.getResource(tomoConfigUri, true);
 			} else {
 				res = getResourceSet().getResource(tomoConfigUri, true);
 			}
 		}
 		return res;
 
 	}
 
 	private Resource createResource(ResourceSet resourceSet, URI tomoConfigUri) {
 		if (resourceSet != null) {
 			return resourceSet.createResource(tomoConfigUri);
 		}
 		return getResourceSet().createResource(tomoConfigUri);
 	}
 
 	/**
 	 * @param tomoConfigUri
 	 * @return {@link Resource}
 	 */
 	private Resource createResource(URI tomoConfigUri) {
 		return createResource(null, tomoConfigUri);
 	}
 
 	public Resource getResource(String fileLocation, boolean shouldCreate) {
 		return getResource(null, fileLocation, shouldCreate);
 	}
 
 	public void saveResource(TomoExperiment experiment) {
 		Map<Object, Object> options = new HashMap<Object, Object>();
 		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
 		try {
 			experiment.eResource().save(options);
 		} catch (IOException e) {
 			logger.error("Exception saving the configuration model", e);
 		}
 	}
 
 	/**
 	 * @return {@link ResourceSet}
 	 */
 	protected ResourceSet getResourceSet() {
 		if (resourceSet == null) {
 
 			resourceSet = new ResourceSetImpl();
 			// To initialize the resourceset resource factory registry with the excalibur config package
 			EPackage.Registry.INSTANCE.put(TomoParametersPackage.eNS_URI, TomoParametersPackage.eINSTANCE);
 			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(TOMOPARAMETERS,
 					new TomoParametersResourceFactoryImpl());
 		}
 		return resourceSet;
 	}
 
 	public void reloadResource(Resource resource) {
 		reloadResource(null, resource);
 	}
 
 	public void reloadResource(ResourceSet resourceSet, Resource resource) {
 		ResourceSet rSet = resourceSet;
 		if (rSet == null) {
 			rSet = getResourceSet();
 		}
 
 		resource.unload();
 		try {
 			resource.load(rSet.getLoadOptions());
 		} catch (IOException e) {
			logger.error("TODO put description of error here", e);
 		}
 	}
 
 }
