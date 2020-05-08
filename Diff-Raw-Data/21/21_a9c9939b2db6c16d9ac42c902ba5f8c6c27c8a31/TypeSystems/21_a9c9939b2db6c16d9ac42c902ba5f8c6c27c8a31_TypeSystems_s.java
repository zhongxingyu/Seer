 /*******************************************************************************
  * Copyright (c) 2012 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.core;
 
 import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
 
 public class TypeSystems {
 
 	public static final ITypeSystem DELEGATING_TYPE_SYSTEM = new DelegatingTypeSystem();
 
 	public static final ITypeSystem WEAK_TYPE_SYSTEM = new StaticTypeSystem(
 			"WEAK");
 
	public static final ITypeSystem GLOABL = new StaticTypeSystem("GLOBAL");
 
 }
