 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *
  * Contributors:
 *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.services;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.util.ImportManager;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 
 /**
  * This class is used to implement Ecore related services.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  * @since 1.1
  */
 public class EcoreService {
 
 	/**
 	 * Retrieves the Ecore genModel.
 	 * 
 	 * @param eo
 	 *            any EObject.
 	 * @return the Ecore genModel
 	 */
 	public GenModel getEcoreGenModel(EObject eo) {
 		Resource genModelResource = new ResourceSetImpl().getResource(
 				URI.createURI("platform:/plugin/org.eclipse.emf.ecore/model/Ecore.genmodel"), true);
 		if (genModelResource != null) {
 			GenModel genModel = (GenModel)genModelResource.getContents().get(0);
 
 			// WARNING: the gen model needs an import manager to make getters usable. So we set a dummy one.
 			genModel.setImportManager(new ImportManager("dummy"));
 
 			return genModel;
 		}
 		throw new RuntimeException("Unable to retrieve Ecore gen model from " + eo);
 	}
 }
