 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.exporter.services;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 import org.eclipse.mylyn.docs.intent.markup.gen.services.ImageServices;
 
 /**
  * Utility class allowing to determine the images associated to some EObject, and copy them inside the
  * exported documentation.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class CopyImageUtils {
 
 	private static AdapterFactoryItemDelegator itemDelegator;
 
 	private static ResourceSetImpl resourceSet;
 
 	/**
 	 * Determines the image associated to the given EObject, copies it inside the exported documentation and
 	 * 
 	 * @param any
 	 *            the eobject to get the image from
 	 * @param repositoryAdapter
 	 *            the repository adapter to use to access to informations (traceability index...)
 	 * @param outputFolder
 	 * @return the image associated to the given EObject
 	 */
 	public static String copyImageAndGetImageID(EObject any, RepositoryAdapter repositoryAdapter,
 			File outputFolder) {
 		String qualifiedImageID = "";
 		// Step 1: getting the image URL thanks to the item delegator
 		Object imageURL = getImageURL(any);
 
 		if (imageURL instanceof URL) {
 			try {
 				// Step 2: resolve URL
 				URL resolvedURL = FileLocator.resolve((URL)imageURL);
 				EClassifier classifier = null;
 				if (any instanceof EClassifier) {
 					classifier = (EClassifier)any;
 				} else {
 					classifier = any.eClass();
 				}
 
 				// If default "Item" image has been
 				if (resolvedURL.toString().contains("org.eclipse.emf.edit")
 						&& resolvedURL.toString().endsWith("/icons/full/obj16/Item.gif")) {
 					// we search for an "edit" plugin in the workspace
 					if ((imageURL = getImageFromWorkspace(classifier, repositoryAdapter)) != null) {
 						resolvedURL = (URL)imageURL;
 					}
 				}
 
 				// Step 3: copy image in the exported documentation
				if (resolvedURL != null) {
					qualifiedImageID = copyImageIfNeeded(classifier, outputFolder, resolvedURL,
							resolvedURL.openStream());
				}
 			} catch (IOException e) {
 				IntentUiLogger.logError(e);
 			}
 		}
 		return qualifiedImageID;
 	}
 
 	/**
 	 * Searches for an edit plugin corresponding to the given EObject and tries to retrieve its associated
 	 * image.
 	 * 
 	 * @param repositoryAdapter
 	 *            the repository adapter to use to access to informations (traceability index...)
 	 * @param the
 	 *            eobject to get the image from
 	 * @return the URL of the image corresponding to the given EObject, null if none found
 	 */
 	private static URL getImageFromWorkspace(EClassifier any, RepositoryAdapter repositoryAdapter) {
 		// If the core.resource plugin is not available, we return null
 		if (EMFPlugin.IS_RESOURCES_BUNDLE_AVAILABLE) {
 			TraceabilityIndex projectTraceabilityIndex = IntentAcceleoServices
 					.getTraceabilityIndex(repositoryAdapter);
 			if (projectTraceabilityIndex != null) {
 				for (TraceabilityIndexEntry entry : projectTraceabilityIndex.getEntries()) {
 					if (any.eResource().getURI().toString().contains(entry.getGeneratedResourcePath())) {
 						Object metamodelURI = entry.getResourceDeclaration().getUri();
 						if (metamodelURI != null
 								&& metamodelURI.toString().replace("\"", "").endsWith("ecore")) {
 							URI genModelURI = URI.createURI(metamodelURI.toString().replace("\"", "")
 									.replace("ecore", "genmodel"));
 							try {
 								Resource resource = getResourceSet().getResource(genModelURI, true);
 								if (!resource.getContents().isEmpty()
 										&& resource.getContents().iterator().next() instanceof GenModel) {
 									String editIconsDirectory = ((GenModel)resource.getContents().iterator()
 											.next()).getEditIconsDirectory();
 									IFolder folder = ResourcesPlugin.getWorkspace().getRoot()
 											.getFolder(new Path(editIconsDirectory + "/full/obj16"));
 									if (folder.exists()) {
 										for (String imageExtension : new String[] {"gif", "png"
 										}) {
 											if (folder.getFile(any.getName() + "." + imageExtension).exists()) {
 												return new URL(
 														"file:/"
 																+ folder.getFile(
 																		any.getName() + "." + imageExtension)
 																		.getLocation().toString());
 											}
 										}
 									}
 								}
 							} catch (RuntimeException e) {
 								IntentUiLogger.logInfo("Cannot find genmodel at " + genModelURI
 										+ ". Default image will be use to display " + any.getName());
 							} catch (MalformedURLException e) {
 								IntentUiLogger.logError(e);
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private static Object getImageURL(EObject any) {
 		Object imageURL = null;
 		// Step 1: getting the image URL thanks to the item delegator
 		if (any instanceof EClass && ((EClassifier)any).getEPackage() != null) {
 			EObject instance = ((EClassifier)any).getEPackage().getEFactoryInstance().create((EClass)any);
 			imageURL = getItemDelegator(any).getImage(instance);
 		} else {
 			imageURL = getItemDelegator(any).getImage(any);
 		}
 
 		// Step 2: convert image location into URL format if needed
 		if (!(imageURL instanceof URL)) {
 			if (imageURL instanceof URI) {
 				try {
 					imageURL = new URL(imageURL.toString());
 				} catch (MalformedURLException e) {
 					// silent catch
 				}
 			} else {
 				if (imageURL instanceof org.eclipse.emf.edit.provider.ComposedImage) {
 					imageURL = ((org.eclipse.emf.edit.provider.ComposedImage)imageURL).getImages().iterator()
 							.next();
 				}
 			}
 		}
 		return imageURL;
 	}
 
 	private static String copyImageIfNeeded(EClassifier classifier, File outputFolder, URL imageURL,
 			InputStream sourceStream) throws IOException {
 		String packageName = "";
 		if (classifier.getEPackage() != null) {
 			packageName = classifier.getEPackage().getName();
 		}
 		File targetFile = new File(outputFolder.getAbsolutePath() + "/icons/generated/" + packageName
 				+ imageURL.getFile().substring(imageURL.getFile().lastIndexOf('/')));
 		// create folders if needed :
 		new File(outputFolder.getAbsolutePath() + "/icons/generated/" + packageName).mkdirs();
 		if (!targetFile.exists()) {
 			ImageServices.copyFile(sourceStream, targetFile);
 		}
 		String copiedImagePath = targetFile.getAbsolutePath().toString();
 		String outputFolderPath = outputFolder.getAbsolutePath();
 		return "../" + copiedImagePath.substring(outputFolderPath.length());
 	}
 
 	/**
 	 * Returns the {@link AdapterFactoryItemDelegator} to use for getting images associated to the given
 	 * EObject.
 	 * 
 	 * @param any
 	 *            the EObject the EObject to get the image from
 	 * @return the {@link AdapterFactoryItemDelegator} to use for getting images associated to the given
 	 *         EObject
 	 */
 	private static AdapterFactoryItemDelegator getItemDelegator(EObject any) {
 		if (itemDelegator == null) {
 			ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
 					ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 			adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 			adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 			itemDelegator = new AdapterFactoryItemDelegator(adapterFactory);
 		}
 		return itemDelegator;
 	}
 
 	private static ResourceSet getResourceSet() {
 		if (resourceSet == null) {
 			resourceSet = new ResourceSetImpl();
 		}
 		return resourceSet;
 	}
 
 	public static void dispose() {
 		if (itemDelegator != null) {
 			((ComposedAdapterFactory)itemDelegator.getAdapterFactory()).dispose();
 		}
 		resourceSet = null;
 		itemDelegator = null;
 	}
 
 }
