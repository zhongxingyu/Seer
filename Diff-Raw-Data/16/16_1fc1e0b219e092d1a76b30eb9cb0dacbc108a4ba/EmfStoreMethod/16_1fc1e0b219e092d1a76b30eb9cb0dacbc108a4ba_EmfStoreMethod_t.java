 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.core.helper;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Marks methods that implement EmfStore methods.
  * 
  * @author boehlke
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface EmfStoreMethod {
 	/**
 	 * the operation methodId, e.g. getproject
 	 */
 	public enum MethodId {
 		/**
 		 * The method types.
 		 */
		GETPROJECTLIST, GETPROJECT, CREATEVERSION, RESOLVEVERSIONSPEC, GETCHANGES, GETHISTORYINFO, ADDTAG, REMOVETAG, CREATEEMPTYPROJECT, CREATEPROJECT, DELETEPROJECT, RESOLVEUSER, IMPORTPROJECTHISTORYTOSERVER, EXPORTPROJECTHISTORYFROMSERVER, UPLOADFILECHUNK, DOWNLOADFILECHUNK, TRANSMITPROPERTY, SETEMFPROPERTIES, GETEMFPROPERTIES, REGISTEREPACKAGE, GETBRANCHES
 	}
 
 	/**
 	 * the implemented method.
 	 */
 	MethodId value();
 }
