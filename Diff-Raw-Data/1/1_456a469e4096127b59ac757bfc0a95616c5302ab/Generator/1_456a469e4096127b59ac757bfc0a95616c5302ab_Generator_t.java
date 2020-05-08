 /**
  * Copyright (c) 2011, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.util;
 
 import org.eclipse.uomo.core.UOMoException;
 
 /**
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  *
  * @param <M> the model
  * @param <O> the output
 * @deprecated currently unused
  */
 public interface Generator<M, O> {
 	public O generate(M model) throws UOMoException;
 }
