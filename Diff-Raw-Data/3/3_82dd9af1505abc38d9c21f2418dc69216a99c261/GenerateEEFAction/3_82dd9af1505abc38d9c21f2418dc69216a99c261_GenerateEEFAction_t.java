 /*******************************************************************************
  * Copyright (c) 2008-2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.ui.generators.actions;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.eef.EEFGen.EEFGenModel;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class GenerateEEFAction extends AbstractGenerateEEFAction {
 
 	/**
 	 * Constructor for Action1.
 	 */
 	public GenerateEEFAction() {
 		super();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.codegen.ui.generators.actions.AbstractGenerateEEFAction#initEEFGenModel()
 	 */
 	protected List<EEFGenModel> initEEFGenModel() throws IOException {
 		if (!selectedFiles.isEmpty()) {
 			for (IFile selectedFile : selectedFiles) {
				ResourceSet resourceSet = new ResourceSetImpl();
 				URI modelURI = URI.createPlatformResourceURI(selectedFile.getFullPath().toString(), true);
 				String fileExtension = modelURI.fileExtension();
 				if (fileExtension == null || fileExtension.length() == 0) {
 					fileExtension = Resource.Factory.Registry.DEFAULT_EXTENSION;
 				}
 				final Resource.Factory.Registry registry = Resource.Factory.Registry.INSTANCE;
 				final Object resourceFactory = registry.getExtensionToFactoryMap().get(fileExtension);
 				if (resourceFactory != null) {
 					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 							resourceFactory);
 				} else {
 					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 							new XMIResourceFactoryImpl());
 				}
 				Resource res = resourceSet.createResource(modelURI);
 				res.load(Collections.EMPTY_MAP);
 				EcoreUtil.resolveAll(resourceSet);
 				if (res.getContents().size() > 0) {
 					EObject object = res.getContents().get(0);
 					if (object instanceof EEFGenModel) {
 						eefGenModels.add((EEFGenModel)object);
 					}
 				}
 			}
 		}
 		return eefGenModels;
 	}
 
 }
