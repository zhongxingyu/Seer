 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.base;
 
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.etrice.generator.etricegen.IDiagnostician;
 
 import com.google.inject.Binder;
 import com.google.inject.Module;
 import com.google.inject.Singleton;
 
 /**
  * @author hrentz
  *
  */
 public class GeneratorBaseModule implements Module {
 
 	/* (non-Javadoc)
 	 * @see com.google.inject.Module#configure(com.google.inject.Binder)
 	 */
 	@Override
 	public void configure(Binder binder) {
 		binder.bind(ResourceSet.class).to(ResourceSetImpl.class);
 
 		binder.bind(Logger.class).in(Singleton.class);
 		binder.bind(ILineOutputLogger.class).to(Logger.class);
 		
 		binder.bind(Diagnostician.class).in(Singleton.class);
 		binder.bind(IDiagnostician.class).to(Diagnostician.class);
 	}
 
 }
