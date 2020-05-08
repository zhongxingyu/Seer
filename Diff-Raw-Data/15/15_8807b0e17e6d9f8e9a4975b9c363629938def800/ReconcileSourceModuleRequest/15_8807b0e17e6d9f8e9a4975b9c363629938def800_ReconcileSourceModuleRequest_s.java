 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.index2;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.index2.IIndexer;
 import org.eclipse.dltk.core.index2.ProjectIndexer2;
 import org.eclipse.dltk.internal.core.util.Util;
 
 /**
  * Request for reconciling source module. Obsolete source module is removed
  * first from the index, then new copy is inserted.
  * 
  * @author michael
  * 
  */
 public class ReconcileSourceModuleRequest extends AddSourceModuleRequest {
 
 	public ReconcileSourceModuleRequest(ProjectIndexer2 indexer,
 			ISourceModule sourceModule, ProgressJob progressJob) {
 		super(indexer, sourceModule, progressJob);
 	}
 
 	protected void run() throws CoreException, IOException {
 		IIndexer indexer = IndexerManager.getIndexer();
 		if (indexer == null) {
 			return;
 		}
		IModelElement projectFragment = sourceModule
				.getAncestor(IModelElement.PROJECT_FRAGMENT);
		IPath containerPath = projectFragment.getPath();
 		String relativePath = Util.relativePath(sourceModule.getPath(),
 				containerPath.segmentCount());
 		indexer.removeDocument(containerPath, relativePath);
 
 		// Now index from scratch:
 		super.run();
 	}
 
 	public boolean equals(Object obj) {
 		if (obj instanceof ReconcileSourceModuleRequest) {
 			return super.equals(obj);
 		}
 		return false;
 	}
 }
