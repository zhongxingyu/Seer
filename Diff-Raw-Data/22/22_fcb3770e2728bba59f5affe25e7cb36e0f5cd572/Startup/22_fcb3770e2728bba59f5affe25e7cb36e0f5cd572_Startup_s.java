 /**
  * <copyright>
  *
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   IBM - Initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 
 package org.ow2.fractal.mind.emf.validation.adapter;
 
import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.ui.IStartup;
import adl.AdlPackage;
 
 
 
 /**
  * Installs an EMF <code>EValidator</code> on the Library package when we
  * start up.  This validator adapts EMF's <code>EValidator</code> API to the
  * EMF Model Validation Service API.
  */
 public class Startup
 	implements IStartup {
 
 	/**
 	 * Initializes me.
 	 */
 	public Startup() {
 		super();
 	}
 
 	/**
 	 *Constraint provider must be activated, force activation
 	 */
 	public void earlyStartup() {
 	}
 }
