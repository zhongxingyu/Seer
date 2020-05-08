 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.base;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.etrice.core.room.RoomModel;
 
 /**
  * a class that computes dependencies of ROOM models.
  * 
  * All models contained in the generator model are looked at.
  * Then the paths of the associated eResource are searched upward
  * for a marker file (in the case of an Eclipse project this would
  * be <code>.project</code>).</br></br>
  * 
  * If this path differs from the supplied main path then the
  * model is mapped as referenced model to a relative path from
  * the main path to the referenced project.
  * 
  * @author Henrik Rentz-Reichert
  *
  */
 public class DependencyManager {
 
 	private HashMap<String, String> refmodel2relpath = new HashMap<String, String>();
 	
 	/**
 	 * calls {@link #computeDependencies(List, URI, String)} with '.project' as
 	 * project marker file.
 	 * 
 	 * @param models a list of all ROOM models
 	 * @param main
 	 */
 	public void computeProjectDependencies(List<RoomModel> models, URI main) {
 		computeDependencies(models, main, ".project");
 	}
 	
 	/**
 	 * see the description of the {@link DependencyManager} class
 	 * 
 	 * @param models a list of all ROOM models
 	 * @param main
 	 * @param projectMarker
 	 */
 	public void computeDependencies(List<RoomModel> models, URI main, String projectMarker) {
 		for (RoomModel model : models) {
 			URI uri = FileSystemHelpers.getMarkerFileURI(model, projectMarker);
			if (uri!=null && !uri.equals(main)) {
 				String relativePath = FileSystemHelpers.getRelativePath(main, uri, true);
 				refmodel2relpath.put(model.getName(), relativePath);
 			}
 		}
 	}
 	
 	/**
 	 * @return the map of referenced models to relative paths
 	 */
 	public Map<String, String> getReferencedModelToRelPath() {
 		return refmodel2relpath;
 	}
 	
 	/**
 	 * @return all referenced model names
 	 */
 	public Set<String> getReferencedModels() {
 		return refmodel2relpath.keySet();
 	}
 	
 	/**
 	 * @return all relative paths of referenced models
 	 */
 	public Collection<String> getReferencedModelRelativePaths() {
 		return refmodel2relpath.values();
 	}
 	
 	/**
 	 * @param refmodel the name of a referenced model
 	 * @return the relative path of the referenced model' project
 	 */
 	public String getRelativePath(String refmodel) {
 		return refmodel2relpath.get(refmodel);
 	}
 }
