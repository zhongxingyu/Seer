 /**
  * Copyright (c) 2010-2011, Jean-Francois Brazeau. All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  *  1. Redistributions of source code must retain the above copyright notice,
  *     this list of conditions and the following disclaimer.
  * 
  *  2. Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  * 
  *  3. The name of the author may not be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIEDWARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.emftools.emf2gv.processor.core;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.edit.provider.ComposedImage;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.emftools.emf2gv.graphdesc.GVFigureDescription;
 import org.emftools.emf2gv.util.EMFHelper;
 
 /**
  * EMF To Graphviz processor intended to be used form an Eclipse OSGI
  * environment.
  * <p>
  * If you plan to use the processor from a Java standalone application, it it is
  * better to use the <code>StandaloneProcessor</code>.
  * </p>
  */
 public class EclipseProcessor {
 
 	/**
 	 * Default logger implementation in an eclipse context.
 	 */
 	private static final ILogger DEFAULT_LOGGER = new ILogger() {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.emftools.emf2gv.processor.core.ILogger#logInfo(java.lang.String)
 		 */
 		public void logInfo(String info) {
 			Activator.getDefault().logInfo(info);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.emftools.emf2gv.processor.core.ILogger#logError(java.lang.String,
 		 * java.lang.Throwable)
 		 */
 		public void logError(String error, Throwable throwable) {
 			Activator.getDefault().logError(error, throwable);
 		}
 
 	};
 
 	/**
 	 * Converts a given model into a diagram file.
 	 * <p>
 	 * If the graphical description is omitted, a default one is automatically
 	 * generated.
 	 * </p>
 	 * 
 	 * @param modelPath
 	 *            the model path.
 	 * @param modelUriFragment
 	 *            the uri giving the model element to use as as root for the
 	 *            diagram generation.
 	 * @param graphDescPath
 	 *            the graphical description path.
 	 * @param targetImagePath
 	 *            the target image path.
 	 * @param processorCallback
 	 *            the processor call back allowing to interrupt the process if
 	 *            required.
 	 * @param dotCommand
 	 *            the graphviz dot utility command path.
 	 * @param addValidationDecorators
 	 *            a boolean indicating if validation decorators must be added.
 	 * @param keepGeneratedGvFile
 	 *            a boolean indicating if the generated Graphviz source file has
 	 *            to be kept.
 	 * @param gvSourceEnconding
 	 *            the encoding to use for the generated graphviz source file.
 	 * @param filters
 	 *            the boolean OCL expressions allowing to filter the nodes.
 	 * @param monitor
 	 *            a progress monitor.
 	 * 
 	 * @throws CoreException
 	 *             thrown if an unexpected error occurs.
 	 */
 	public static void process(IPath modelPath, String modelUriFragment,
 			IPath graphDescPath, IPath targetImagePath,
 			IProcessorCallback processorCallback, String dotCommand,
 			boolean addValidationDecorators, boolean keepGeneratedGvFile,
 			String gvSourceEnconding, List<OCLFilterExpression> filters,
 			IProgressMonitor monitor) throws CoreException {
 
 		/*
		 * Graphdesc file loading.
 		 */
 		GVFigureDescription figureDesc = null;
 		if (graphDescPath != null) {
 			Resource graphDescRes = EMFHelper.loadFileEMFResource(
 					new ResourceSetImpl(), graphDescPath, monitor);
 			figureDesc = (GVFigureDescription) graphDescRes.getContents()
 					.get(0);
 		}
 
 		/*
 		 * Model file loading
 		 */
 		ResourceSet rs = new ResourceSetImpl();
 		rs.setPackageRegistry(EPackage.Registry.INSTANCE);
 		Resource modelRes = EMFHelper.loadFileEMFResource(rs, modelPath,
 				monitor);
 		List<EObject> modelRoots = null;
 		if (modelUriFragment == null || "".equals(modelUriFragment.trim())) {
 			modelRoots = modelRes.getContents();
 		} else {
 			EObject eObject = modelRes.getEObject(modelUriFragment.trim());
 			if (eObject == null) {
 				throw new CoreException(new Status(IStatus.ERROR,
 						Activator.PLUGIN_ID, "Invalid URI fragment '"
 								+ modelUriFragment + "' for resource '"
 								+ modelRes.getURI().toString() + "'"));
 			}
 			modelRoots = Arrays.asList(new EObject[] { eObject });
 		}
 
 		/*
 		 * Generation folder creation
 		 */
 		IPath outputFolderPath = targetImagePath.addFileExtension("emf2gv");
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IFolder outputFolder = root.getFolder(outputFolderPath);
 		outputFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
 		if (!outputFolder.exists()) {
 			outputFolder.create(true, true, null);
 		}
 
 		// Icons provider building
 		List<EPackage> ePackages = null;
 		if (figureDesc != null) {
 			ePackages = figureDesc.getEPackages();
 		} else {
 			ePackages = new ArrayList<EPackage>();
 			for (EObject eObject : modelRoots) {
 				EPackage ePackage = eObject.eClass().getEPackage();
 				if (!ePackages.contains(ePackage)) {
 					ePackages.add(ePackage);
 				}
 			}
 		}
 		final AdapterFactory adapterFactory = EMFHelper
 				.getAdapterFactory(ePackages);
 		IEObjectIconProvider eObjectIconProvider = new IEObjectIconProvider() {
 			public URL getIcon(EObject eObject) {
 				URL result = null;
 				IItemLabelProvider labelProvider = (IItemLabelProvider) adapterFactory
 						.adapt(eObject, IItemLabelProvider.class);
 				if (labelProvider != null) {
 					Object image = labelProvider.getImage(eObject);
 					// If we meet a composed image, we get the first image
 					if (image instanceof ComposedImage) {
 						List<Object> images = ((ComposedImage) image).getImages();
 						if (images != null && images.size() > 0) {
 							image = images.get(0);
 						}
 					}
 					if (image instanceof URL) {
 						result = (URL) image;
 					}
 				}
 				return result;
 			}
 		};
 
 		// Diagram generation
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		IFile diagramFile = workspaceRoot.getFile(targetImagePath);
 		StandaloneProcessor.process(modelRoots, figureDesc, outputFolder
 				.getRawLocation().toFile(), diagramFile.getRawLocation()
 				.toOSString(), processorCallback, eObjectIconProvider,
 				dotCommand, addValidationDecorators, keepGeneratedGvFile,
 				gvSourceEnconding, filters, DEFAULT_LOGGER, monitor);
 
 		// Working directory deletion
 		if (!keepGeneratedGvFile) {
 			outputFolder.delete(true, false, null);
 		}
 
 		// Resource refresh
 		diagramFile.refreshLocal(IResource.DEPTH_ONE, null);
 		outputFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 	}
 
 }
