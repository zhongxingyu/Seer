 /*******************************************************************************
 * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.coconut.reuseextension.resource.rex.mopp;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.reuseware.coconut.resource.ReuseResources;
 import org.reuseware.coconut.reuseextension.ReuseExtension;
 import org.reuseware.coconut.reuseextension.resource.rex.IRexBuilder;
 import org.reuseware.coconut.reuseextension.resource.rex.RexEProblemType;
 
 /**
  * Builder for REX files that synchronizes the ID inside the REX file
  * with the ID in the repository, which is determined by the position
  * of the file in a store.
  */
 public class RexBuilder implements IRexBuilder {
 	
 	/**
 	 * @param uri of the REX file
 	 * 
 	 * @return true for all files
 	 */
 	public boolean isBuildingNeeded(URI uri) {
 		return true;
 	}
 	
 	/**
 	 * @param resource the REX file that is synchronized
 	 * @param monitor not used
 	 * 
 	 * @return org.eclipse.core.runtime.Status.OK_STATUS
 	 */
 	public IStatus build(RexResource resource, IProgressMonitor monitor) {
 		String warning = validateAndCorrect(resource);
 		if (warning != null) {
 			resource.addWarning(warning, RexEProblemType.BUILDER_ERROR, resource.getContents().get(0));
 		}
 		return org.eclipse.core.runtime.Status.OK_STATUS;
 	}
 	
 	private String validateAndCorrect(Resource resource) {
 		if (resource.getContents().isEmpty()) {
 			return null;
 		}
 		EObject root = resource.getContents().get(0);
 		if (!(root instanceof ReuseExtension)) {
 			return null;
 		}
 		ReuseExtension element = (ReuseExtension) root;
 
 		List<String> idOutsideModel = ReuseResources.INSTANCE.getID(resource.getURI());
 		if (idOutsideModel == null) {	
 			return "Place this specification into a fragment store to activate it";
 		}
 		
 		String warningMessage = null;
 		
 		List<String> idInsideModel = new ArrayList<String>(element.getRexNamespace());
 		idInsideModel.add(element.getRexName());
 		
 		if (!idOutsideModel.equals(idInsideModel)) {
 			//correct!
 			String name = idOutsideModel.remove(idOutsideModel.size() - 1);
 			
 			element.setRexName(name);
 			element.getRexNamespace().clear();
 			element.getRexNamespace().addAll(idOutsideModel);
 			
 			try {
 				ResourceSet rs = resource.getResourceSet();
 				Map<Object, Object> options = null;
 				if (rs != null) {
 					options = rs.getLoadOptions();
 				}
 				resource.save(options);
 			} catch (IOException e) {
 				RexPlugin.logError("Error during ID correction", e);
 			}
 			
 		}
 
 		return warningMessage;
 	}
	
 }
