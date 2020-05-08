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
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
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
