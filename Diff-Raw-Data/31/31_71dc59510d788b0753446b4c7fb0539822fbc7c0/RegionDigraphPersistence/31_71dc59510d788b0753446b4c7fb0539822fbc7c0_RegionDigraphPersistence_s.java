 /*******************************************************************************
  * Copyright (c) 2011 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.osgi.region;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * A region digraph persistence is used to persist the state of a {@link RegionDigraph}.
  */
 public interface RegionDigraphPersistence {
 
     /**
      * Creates a new digraph and reads the content of the digraph from the provided input. The provided input must have
     * been persisted using the {@link #load(InputStream)} method.
      * <p />
      * The specified stream remains open after this method returns.
      * 
     * @param input
      * @return
      * @throws IOException if error occurs reading the digraph.
      */
     RegionDigraph load(InputStream input) throws IOException;
 
     /**
     * Writes the specified {@link RegionDigraph} to the provided output in a formate suitable for using the
      * {@link #load(InputStream)} method.
      * 
     * @param output
      * @throws IOException if error occurs writing the digraph.
      */
     void save(RegionDigraph digraph, OutputStream output) throws IOException;
 }
