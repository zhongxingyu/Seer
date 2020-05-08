 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.aggregator.transformer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.util.AggregatorResourceImpl;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 
 /**
  * @author filip.hrbek@cloudsmith.com
  * 
  */
 public class TransformationManager {
 	public interface ContributorListener {
 		void contributorFound(IConfigurationElement config, TransformerContextContributor contributor)
 				throws CoreException;
 	}
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_SOURCE_ECORE = "sourceEcoreUri";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_TARGET_NS = "targetNS";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_TARGET_ECORE = "targetEcoreUri";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_CLASS = "class";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_SOURCE_NS = "sourceNS";
 
 	public static List<IConfigurationElement> resolveTransformationSequence(IConfigurationElement[] transformations,
 			String requiredSourceNS, String requiredTargetNS, List<IConfigurationElement> transformerSequence) {
 
 		for(IConfigurationElement transformation : transformations) {
 			String srcNS = transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_NS);
 			String trgtNS = transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_TARGET_NS);
 
 			if(requiredSourceNS.equals(srcNS)) {
 
 				List<IConfigurationElement> newTransformerSequence = new ArrayList<IConfigurationElement>();
 				newTransformerSequence.addAll(transformerSequence);
 				newTransformerSequence.add(transformation);
 
 				if(requiredTargetNS.equals(trgtNS)) {
 					return newTransformerSequence;
 				}
 				else {
 					List<IConfigurationElement> result = resolveTransformationSequence(
 						transformations, trgtNS, requiredTargetNS, newTransformerSequence);
 					if(result != null)
 						return result;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private boolean srcNamespaceFound;
 
 	private List<IConfigurationElement> transformationSequence;
 
 	private List<TransformerContextContributor> contextContributors = new ArrayList<TransformerContextContributor>();
 
 	private static final String LEGACY_TRANSFORMATION_ID = "org.eclipse.b3.aggregator.legacy_transformation";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_SOURCE_TOP_ELEMENT = "sourceTopElement";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_SOURCE_NS_ATTRIBUTE = "sourceNSAttribute";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_CONTEXT_CONTRIBUTOR = "contextContributor";
 
 	private URI srcResourceURI;
 
 	public TransformationManager(URI srcResourceURI) {
 		this(srcResourceURI, null);
 	}
 
 	public TransformationManager(URI srcResourceURI, ContributorListener contributorListener) {
 		this.srcResourceURI = srcResourceURI;
 
 		Set<String> nsPaths = new HashSet<String>();
 
 		IConfigurationElement[] transformations = Platform.getExtensionRegistry().getConfigurationElementsFor(
 			LEGACY_TRANSFORMATION_ID);
 
 		String xmlns = null;
 		String topElement = null;
 		String nsAttribute = null;
 		int i = 0;
 
 		while(xmlns == null && i < transformations.length) {
 			topElement = transformations[i].getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_TOP_ELEMENT);
 			nsAttribute = transformations[i].getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_NS_ATTRIBUTE);
 			i++;
 
 			if(StringUtils.trimmedOrNull(topElement) != null && StringUtils.trimmedOrNull(nsAttribute) != null &&
 					!nsPaths.contains(topElement + "/" + nsAttribute)) {
 				xmlns = ResourceUtils.getResourceXMLNS(srcResourceURI, topElement, nsAttribute);
 				nsPaths.add(topElement + "/" + nsAttribute);
 			}
 		}
 
 		srcNamespaceFound = xmlns != null;
 		if(srcNamespaceFound) {
 
 			String requiredSourceNS = xmlns;
 
 			String requiredTargetNS = AggregatorPackage.eNS_URI;
 
 			transformationSequence = TransformationManager.resolveTransformationSequence(
 				transformations, requiredSourceNS, requiredTargetNS, new ArrayList<IConfigurationElement>());
 
 			for(IConfigurationElement transformation : transformationSequence) {
 				TransformerContextContributor contextContributor = null;
 				try {
 					if(transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_CONTEXT_CONTRIBUTOR) != null) {
 						contextContributor = (TransformerContextContributor) transformation.createExecutableExtension(LEGACY_TRANSFORMATION_ATTR_CONTEXT_CONTRIBUTOR);
 
 						if(contributorListener != null)
 							contributorListener.contributorFound(transformation, contextContributor);
 					}
 				}
 				catch(CoreException e) {
 					throw new RuntimeException(
						"Deprecated resource was not transformed - transformation wizard cannot be started", e);
 				}
 				if(contextContributor != null)
 					contextContributors.add(contextContributor);
 			}
 		}
 	}
 
 	/**
 	 * @return the contextContributors
 	 */
 	public final List<TransformerContextContributor> getContextContributors() {
 		return contextContributors;
 	}
 
 	/**
 	 * @return the transformationSequence
 	 */
 	public final List<IConfigurationElement> getTransformationSequence() {
 		return transformationSequence;
 	}
 
 	/**
 	 * @return the srcNamespaceFound
 	 */
 	public final boolean isSrcNamespaceFound() {
 		return srcNamespaceFound;
 	}
 
 	public Resource transformResource() throws IOException, CoreException {
 
 		Map<String, Object> context = new HashMap<String, Object>();
 
 		for(TransformerContextContributor contextContributor : contextContributors)
 			contextContributor.contributeToContext(context);
 
 		ResourceSet ecoreRs01 = null;
 		Resource ecoreRes01 = null;
 		EPackage package01 = null;
 		ResourceSet rs01 = null;
 		Resource res01 = null;
 
 		ResourceSet ecoreRs02 = null;
 		Resource ecoreRes02 = null;
 		EPackage package02 = null;
 		ResourceSet rs02 = null;
 		Resource res02 = null;
 
 		int idx = 0;
 		for(IConfigurationElement transformation : transformationSequence) {
 
 			if(idx == 0) {
 				ecoreRs01 = new ResourceSetImpl();
 				ecoreRs01.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
 					"ecore", new EcoreResourceFactoryImpl());
 				ecoreRes01 = ecoreRs01.getResource(
 					URI.createURI(transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_SOURCE_ECORE)), true);
 				package01 = (EPackage) ecoreRes01.getContents().get(0);
 
 				rs01 = new ResourceSetImpl();
 				rs01.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
 				rs01.getPackageRegistry().put(package01.getNsURI(), package01);
 
 				res01 = rs01.getResource(srcResourceURI, true);
 				rs01.getResources().add(res01);
 			}
 			else {
 				ecoreRs01 = ecoreRs02;
 				ecoreRes01 = ecoreRes02;
 				package01 = package02;
 				rs01 = rs02;
 				res01 = res02;
 			}
 
 			idx++;
 
 			File tempFile = File.createTempFile("temp", ".b3aggr");
 			tempFile.deleteOnExit();
 
 			rs02 = new ResourceSetImpl();
 			rs02.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
 
 			if(AggregatorPackage.eNS_URI.equals(transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_TARGET_NS))) {
 				package02 = AggregatorPackage.eINSTANCE;
 
 				res02 = new AggregatorResourceImpl(URI.createURI(tempFile.toURI().toString()));
 				rs02.getResources().add(res02);
 			}
 			else {
 				ecoreRs02 = new ResourceSetImpl();
 				ecoreRs02.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
 					"ecore", new EcoreResourceFactoryImpl());
 				ecoreRes02 = ecoreRs02.getResource(
 					URI.createURI(transformation.getAttribute(LEGACY_TRANSFORMATION_ATTR_TARGET_ECORE)), true);
 				package02 = (EPackage) ecoreRes02.getContents().get(0);
 
 				res02 = rs02.createResource(URI.createURI(tempFile.toURI().toString()));
 			}
 
 			rs02.getPackageRegistry().put(package02.getNsURI(), package02);
 
 			ITransformer transformer = (ITransformer) transformation.createExecutableExtension(LEGACY_TRANSFORMATION_ATTR_CLASS);
 
 			transformer.initTransformer(res01, res02, package02, context);
 			transformer.startTransformation();
 		}
 
 		return res02;
 	}
 }
