 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.aggregator.util;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.Aggregation;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.ValidationSet;
 import org.eclipse.b3.aggregator.p2.util.MetadataRepositoryResourceImpl;
 import org.eclipse.b3.cli.HeadlessActivator;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * Utilities for managing aggregator specific resources
  * 
  * @author filip.hrbek@cloudsmith.com
  */
 public class ResourceUtils {
 	/**
 	 * Cleans up unreferenced resources on demand
 	 * 
 	 * @param aggregator
 	 */
 	public static void cleanUpResources(Aggregation aggregator) {
 		cleanUpResources(aggregator, true);
 	}
 
 	/**
 	 * Cleans up unreferenced resources on demand and possibly updates error markers
 	 * 
 	 * @param aggregator
 	 * @param updateMarkers
 	 */
 	public static void cleanUpResources(Aggregation aggregator, boolean updateMarkers) {
 		Resource topResource = ((EObject) aggregator).eResource();
		if(topResource == null)
			return;

 		ResourceSet topSet = topResource.getResourceSet();
 
 		synchronized(topSet) {
 			Set<Resource> referencedResources = new HashSet<Resource>();
 			referencedResources.add(topResource);
 			for(ValidationSet vs : aggregator.getValidationSets()) {
 				for(Contribution contribution : vs.getContributions()) {
 					for(MappedRepository mappedRepository : contribution.getRepositories()) {
 						if(mappedRepository.isBranchEnabled()) {
 							if(mappedRepository.getResolvedLocation() != null) {
 								org.eclipse.emf.common.util.URI repoURI = MetadataRepositoryResourceImpl.getResourceUriForNatureAndLocation(
 									mappedRepository.getNature(), mappedRepository.getResolvedLocation());
 								referencedResources.add(topSet.getResource(repoURI, false));
 							}
 						}
 						else {
 							// avoid notification recursion - set to null only if it is not null yet
 							if(mappedRepository.getMetadataRepository(false) != null)
 								mappedRepository.setMetadataRepository(null);
 						}
 					}
 				}
 				for(MetadataRepositoryReference repoRef : vs.getValidationRepositories()) {
 					if(repoRef.isBranchEnabled()) {
 						if(repoRef.getResolvedLocation() != null) {
 							org.eclipse.emf.common.util.URI repoURI = MetadataRepositoryResourceImpl.getResourceUriForNatureAndLocation(
 								repoRef.getNature(), repoRef.getResolvedLocation());
 							referencedResources.add(topSet.getResource(repoURI, false));
 						}
 					}
 					else
 					// avoid notification recursion - set to null only if it is not null yet
 					if(repoRef.getMetadataRepository(false) != null)
 						repoRef.setMetadataRepository(null);
 				}
 			}
 			Iterator<Resource> allResources = topSet.getResources().iterator();
 
 			while(allResources.hasNext()) {
 				Resource res = allResources.next();
 				if(!referencedResources.contains(res)) {
 					if(res instanceof MetadataRepositoryResourceImpl) {
 						((MetadataRepositoryResourceImpl) res).cancelLoadingJob();
 						allResources.remove();
 					}
 				}
 			}
 		}
 
 		if(updateMarkers && !HeadlessActivator.getInstance().isHeadless())
 			((AggregatorResourceImpl) topResource).analyzeResource();
 	}
 
 	/**
 	 * Returns the main aggregator node
 	 * 
 	 * @param resourceSet
 	 * @return the aggregator instance, or null if it is not available
 	 */
 	public static Aggregation getAggregation(ResourceSet resourceSet) {
 		if(resourceSet == null)
 			return null;
 
 		EList<Resource> resources = resourceSet.getResources();
 		Resource aggregatorResource = null;
 		for(Resource resource : resources)
 			if(resource instanceof AggregatorResourceImpl) {
 				aggregatorResource = resource;
 				break;
 			}
 		return aggregatorResource == null
 				? null
 				: (Aggregation) aggregatorResource.getContents().get(0);
 	}
 
 	/**
 	 * Tries to get metadata repository from mapped repository. If it fails to load, an exception is thrown.
 	 * 
 	 * @param repoRef
 	 * @return
 	 * @throws CoreException
 	 */
 	public static MetadataRepository getMetadataRepository(MetadataRepositoryReference repoRef) throws CoreException {
 		MetadataRepository mdr = repoRef.getMetadataRepository();
 
 		if(mdr == null) {
 			Resource resource = ((EObject) repoRef).eResource();
 			if(resource != null && resource instanceof MetadataRepositoryResourceImpl &&
 					((MetadataRepositoryResourceImpl) resource).getLastException() != null)
 				throw ExceptionUtils.wrap(((MetadataRepositoryResourceImpl) resource).getLastException());
 
 			throw ExceptionUtils.fromMessage("Error loading repository " + repoRef.getResolvedLocation());
 		}
 
 		return mdr;
 	}
 
 	/**
 	 * Gets resource namespace
 	 * 
 	 * @param resourceURI
 	 * @param resourceTopNodeName
 	 * @param resourceNSAttributeName
 	 * @return
 	 */
 	public static String getResourceXMLNS(URI resourceURI, final String resourceTopNodeName,
 			final String resourceNSAttributeName) {
 
 		class XMLNSHandler extends DefaultHandler {
 			private String xmlns;
 
 			public String getXMLNS() {
 				return xmlns;
 			}
 
 			@Override
 			public void startElement(String nsURI, String strippedName, String tagName, Attributes attributes)
 					throws SAXException {
 				if(tagName.equalsIgnoreCase(resourceTopNodeName)) {
 					xmlns = attributes.getValue(resourceNSAttributeName);
 					throw new SAXException("XMLNS is read");
 				}
 			}
 		}
 
 		XMLReader parser;
 		XMLNSHandler xmlnsHandler = null;
 		try {
 			parser = XMLReaderFactory.createXMLReader();
 			xmlnsHandler = new XMLNSHandler();
 			parser.setContentHandler(xmlnsHandler);
 
 			// this is needed for parser to provide even "xmlns*" attributes
 			parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
 			parser.parse(new InputSource(new ExtensibleURIConverterImpl().createInputStream(resourceURI)));
 		}
 		catch(SAXException e) {
 			if(xmlnsHandler != null)
 				return xmlnsHandler.getXMLNS();
 		}
 		catch(IOException e) {
 			// do not care
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if resourceURI points to a resource with the up-to-date model
 	 * 
 	 * @param resourceURI
 	 * @return
 	 */
 	public static boolean isCurrentModel(URI resourceURI) {
 		String topElement = AggregatorPackage.eNS_PREFIX + ":" + AggregatorPackage.eINSTANCE.getAggregation().getName();
 		String nsAttribute = XMLResource.XML_NS + ":" + AggregatorPackage.eNAME;
 
 		String xmlns = getResourceXMLNS(resourceURI, topElement, nsAttribute);
 
 		return AggregatorPackage.eNS_URI.equals(xmlns);
 	}
 
 	/**
 	 * Loads resource for specified repository
 	 * 
 	 * @param repoRef
 	 */
 	public static void loadResourceForMappedRepository(MetadataRepositoryReference repoRef) {
 		repoRef.startRepositoryLoad(false);
 	}
 }
