 /*
  * Copyright (c) 2009-2012 Eike Stepper (Berlin, Germany) and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Eike Stepper - initial API and implementation
  *    
  *  Initial Publication:
  *    Eclipse Magazin - http://www.eclipse-magazin.de
  */
 package rcpmail.model;
 
 import org.eclipse.emf.cdo.CDOObject;
 
 /**
  * @author Eike Stepper
  */
 public interface ITransactionalOperation<T extends CDOObject> {
 	public Object execute(T object);
 }
