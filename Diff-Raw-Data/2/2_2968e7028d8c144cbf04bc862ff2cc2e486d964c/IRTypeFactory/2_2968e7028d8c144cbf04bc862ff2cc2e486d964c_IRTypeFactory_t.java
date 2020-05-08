 /*******************************************************************************
  * Copyright (c) 2011 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.typeinfo;
 
 import org.eclipse.dltk.annotations.ConfigurationElement;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 
 /**
  * Factory to create runtime type instance for the specified model object
  */
 @ConfigurationElement("runtimeTypeFactory")
 public interface IRTypeFactory {
 
	JSType2 create(Type type);
 
 }
