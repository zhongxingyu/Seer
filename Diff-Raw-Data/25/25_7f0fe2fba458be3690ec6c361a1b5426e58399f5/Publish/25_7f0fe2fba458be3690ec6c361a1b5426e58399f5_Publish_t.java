 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.communication.publisher;
 
 /**
  * 
  * scribble Publish.service("IDataService").usingUrl("/DataService").withProcol(
  * "hessian").AndStart();
  * Publish.allServices().filter("riena.remote").AndStart();
  */
 public final class Publish {
 
 	private Publish() {
 	}
 
 	public static SingleServicePublisher service(String name) {
 		return new SingleServicePublisher(name);
 	}
 
	public static SingleServicePublisher service(Class<?> clazz) {
 		return new SingleServicePublisher(clazz.getName());
 	}
 
 	public static MultiServicePublisher allServices() {
 		return new MultiServicePublisher();
 	}
 }
