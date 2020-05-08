 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.bridge.genmodel;
 
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.URI;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 
 /**
  * @author artem
  *
  */
 public class RuntimeGenModelAccess extends BasicGenModelAccess {
 
 	private GenPackage genPackage;
 
 	public RuntimeGenModelAccess() {
 		super(NotationPackage.eINSTANCE);
 		registerLocation(fromExtpoint());
		// hack
		registerLocation(fromExtpoint("http://www.eclipse.org/gmf/1.5.0/Notation")); // as in plugin.xml at the moment %-)
		// uglier 
		registerLocation(constructDefaultFromModel());
		// the ugliest
		IPath workspaceCopy = new Path("/org.eclipse.gmf.runtime.notation/model/notation.genmodel");
		if (ResourcesPlugin.getWorkspace().getRoot().getFile(workspaceCopy).exists()) {
			registerLocation(URI.createPlatformResourceURI(workspaceCopy.toPortableString()));
		}
		registerLocation(URI.createURI("platform:/plugin/org.eclipse.gmf.runtime.notation/model/notation.genmodel"));  
 	}
 
 	/**
 	 * Make sure genmodel is initialized prior to calling this method.
 	 * @return
 	 */
 	public GenPackage genPackage() {
 		if (genPackage == null) {
 		// XXX perhaps, different logic could be here to 
 		// workaround elements from same metamodel are not equal case  
 			genPackage = model().findGenPackage(NotationPackage.eINSTANCE);
 		}
 		return genPackage;
 	}
 }
